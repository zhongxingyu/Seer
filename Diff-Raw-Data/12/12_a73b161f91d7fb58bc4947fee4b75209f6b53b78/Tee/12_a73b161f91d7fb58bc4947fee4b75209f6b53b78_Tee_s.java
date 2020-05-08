 /*
  * Created on 7-giu-2005
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package it.javalinux.tee.mbean;
 
 import it.javalinux.tee.event.Event;
 import it.javalinux.tee.event.NullEvent;
 import it.javalinux.tee.misc.SpecificationDigester;
 import it.javalinux.tee.misc.TeeHelper;
 import it.javalinux.tee.specification.EventSpec;
 import it.javalinux.tee.specification.HandlerSpec;
 import it.javalinux.tee.specification.TransportSpec;
 import it.javalinux.tee.specification.UnknownEventSpec;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Iterator;
 
 import javax.transaction.Status;
 import javax.transaction.SystemException;
 import javax.transaction.Transaction;
 import javax.transaction.TransactionManager;
 
 import org.jboss.aspects.Injected;
 import org.jboss.aspects.asynch.Asynchronous;
 import org.jboss.logging.Logger;
 import org.jboss.system.ServiceMBeanSupport;
 
 /**
  * @author oracle
  *
  * To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 public class Tee extends ServiceMBeanSupport implements TeeMBean  {
     
 
     
 	private String teeName;
     
     private SpecificationDigester specDigester = null;
 	
	private Long numberOfEventProcessed;
	private Long numberOfEventTransformed;
	private Long numberOfEventFailed;
	private Long totalProcessingTime;
 	
 	@Injected TransactionManager tm;
 	
 	/* (non-Javadoc)
 	 * @see org.jboss.system.ServiceMBeanSupport#createService()
 	 */
 	protected void createService() throws Exception {
 		super.createService();
 		Logger.getLogger(this.getClass()).debug("createService");
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.jboss.system.ServiceMBeanSupport#destroyService()
 	 */
 	protected void destroyService() throws Exception {
 		super.destroyService();
 		Logger.getLogger(this.getClass()).debug("destroyService");
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.jboss.system.ServiceMBeanSupport#startService()
 	 */
 	protected void startService() throws Exception {
 		super.startService();
 		Logger.getLogger(this.getClass()).debug("startService");
 		specDigester = new SpecificationDigester();
         specDigester.readSpecification(teeName);
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.jboss.system.ServiceMBeanSupport#stopService()
 	 */
 	protected void stopService() throws Exception {
 		super.stopService();
 		Logger.getLogger(this.getClass()).debug("stopService");
 	}
 
 	
 	
 
     /**
      * @param event
      * 
      */
 	@Asynchronous
 	public void process(Event event) {
 		Transaction motherTransaction = null;
 		try {
 			if (tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
 				Logger.getLogger(this.getClass()).debug("suspend old transaction");
 				motherTransaction = tm.suspend();
 			}
 			Logger.getLogger(this.getClass()).debug("Starting transaction");
 			tm.begin();
 			
 		    Logger.getLogger(this.getClass()).info("Processing event of class "+event.getClass().getName());
 			//get handler and/or transport right for this event 
 			// and invoke them passing event and log option
 		    TeeHelper helper = new TeeHelper();
 			
 			if (event instanceof NullEvent) {
 				Logger.getLogger(this.getClass()).info("Nothing to do, null event received: "+event);
 			} else {
 			    Object obj = specDigester.getEventSpecMap().get(event.getClass().getName());
 		        if (obj!=null) {
 		            EventSpec eventSpec = (EventSpec)obj;
 		            if (eventSpec.getHandlerSpecList().size()>0 || eventSpec.getTransportSpecList().size()>0) {
 		                for (Iterator it = eventSpec.getHandlerSpecList().iterator(); it.hasNext(); ) {
 							long startProcessingTime = System.currentTimeMillis();
 		                    helper.processWithHandler(event, (HandlerSpec)it.next());
 							this.numberOfEventProcessed = new Long(this.numberOfEventProcessed.longValue() + 1);
 							this.totalProcessingTime = this.totalProcessingTime + (System.currentTimeMillis() - startProcessingTime);
 		                }
 		                for (Iterator it = eventSpec.getTransportSpecList().iterator(); it.hasNext(); ) {
 		                    helper.processWithTransport(event, (TransportSpec)it.next());
 							this.numberOfEventTransformed = new Long(this.numberOfEventTransformed.longValue() + 1);
 		                }
 		            } else {
 		                Logger.getLogger(this.getClass()).info("No handler or transport found for events of class "+event.getClass());
 		            }
 		        } else {
 	                UnknownEventSpec unknownEventSpec = specDigester.getUnknownEventSpec();
 		            //unknown event
	                if (unknownEventSpec.getTransformer()!=null) {
	                    event = helper.transformEvent(event, unknownEventSpec.getTransformer());
	                }
 		            if (unknownEventSpec.getHandlerSpecList().size()>0 || unknownEventSpec.getTransportSpecList().size()>0) {
 		                for (Iterator it = unknownEventSpec.getHandlerSpecList().iterator(); it.hasNext(); ) {
 		                    helper.processWithHandler(event, (HandlerSpec)it.next());
 		                }
 		                for (Iterator it = unknownEventSpec.getTransportSpecList().iterator(); it.hasNext(); ) {
 		                    helper.processWithTransport(event, (TransportSpec)it.next());
 		                }
 		            } else {
 		                Logger.getLogger(this.getClass()).info("No handler or transport found for events of class "+event.getClass());
 		            }
 		        }
 			}
 
 	    } catch (Exception e) {
 	        Logger.getLogger(this.getClass()).error("Error processing event of class "+event.getClass());
 			StringWriter sw = new StringWriter();
     		e.printStackTrace(new PrintWriter(sw));
     		Logger.getLogger(this.getClass()).error(sw.toString());
 			this.numberOfEventFailed = new Long(this.numberOfEventFailed.longValue() + 1);
 			try {
 				tm.setRollbackOnly();
 			} catch (IllegalStateException e1) {
 				e1.printStackTrace();
 			} catch (SystemException e1) {
 				e1.printStackTrace();
 			}
 	    }
         
         try {
     		if (tm.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
     			Logger.getLogger(this.getClass()).debug("rollback transaction");
     			tm.rollback();
     		} else {
     			Logger.getLogger(this.getClass()).debug("commit transaction");
     			tm.commit();
     		}
     		
     		if (motherTransaction != null ) {
     			Logger.getLogger(this.getClass()).debug("resume old transaction");
     			tm.resume(motherTransaction);
     		}
     	} catch (Exception e) {
     		// TODO Qui bisogna salvare da qualche parte l'evento.
     		e.printStackTrace();
     	} 
 		
 	}
 	
 	
 	/**
 	 * @return Returns the TeeName.
 	 */
 	public String getTeeName() {
 		return teeName;
 	}
 	
 	
 	/**
 	 * @param teeName The TeeName to set.
 	 */
 	public void setTeeName(String teeName) {
 		this.teeName = teeName;
 	}
 	
 	@Injected 
 	void setTransactionManager(TransactionManager tm) {
 		this.tm = tm;
 	}
 	
 
 
 
 	public Double getAvarageProcessingTime() {
 		return new Double((double)this.totalProcessingTime/(double)this.numberOfEventProcessed);
 	}
 	
 
 
 	public Long getNumberOfEventTransformed() {
 		return numberOfEventTransformed;
 	}
 
 
 	public Long getNumberOfEventFailed() {
 		return numberOfEventFailed;
 	}
 	
 
 
 	public Long getNumberOfEventProcessed() {
 		return numberOfEventProcessed;
 	}
 	
 	
 	
 }
