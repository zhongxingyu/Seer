 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.core.dispatch;
 
 import net.jini.core.lookup.ServiceItem;
 import net.jini.core.transaction.TransactionException;
 import sorcer.core.Dispatcher;
 import sorcer.core.Provider;
 import sorcer.core.exertion.Jobs;
 import sorcer.core.exertion.NetJob;
 import sorcer.core.exertion.NetTask;
 import sorcer.core.provider.ServiceProvider;
 import sorcer.core.signature.NetSignature;
 import sorcer.falcon.base.Conditional;
 import sorcer.river.Filters;
 import sorcer.service.*;
 
 import java.rmi.RemoteException;
 import java.util.Set;
 
 abstract public class CatalogExertDispatcher extends ExertDispatcher {
 
 	private final static int SLEEP_TIME = 20;
 	
 	public CatalogExertDispatcher(Job job, Set<Context> sharedContext,
                                   boolean isSpawned, Provider provider) throws Throwable {
 		super(job, sharedContext, isSpawned, provider);
 		dThread = new DispatchThread();
 		try {
 			dThread.start();
 			dThread.join();
 		} catch (InterruptedException ie) {
 			ie.printStackTrace();
 			state = FAILED;
 		}
 	}
 
 	protected void preExecExertion(Exertion exertion) throws ExertionException,
 			SignatureException {
 		// If Job, new dispatcher will update inputs for it's Exertion
 		// in catalog dispatchers, if it is a job, then new dispatcher is
 		// spawned
 		// and the shared contexts are passed. So the new dispatcher will update
 		// inputs
 		// of tasks inside the jobExertion. But in space, all inputs to a new
 		// job are
 		// to be updated before dropping.
 		try {
 			exertion.getControlContext().appendTrace(provider.getProviderName() 
 					+ " dispatcher: " + getClass().getName());
 		} catch (RemoteException ignored) {
 			// ignore it, local call		
 		}
 		logger.finest("preExecExertions>>>...UPDATING INPUTS...");
 		if (exertion.isTask()) {
 				updateInputs(exertion);
 			
 		}
 		((ServiceExertion) exertion).startExecTime();
 		((ServiceExertion) exertion).setStatus(RUNNING);
 	}
 
 	// Parallel
 	protected ExertionThread runExertion(ServiceExertion ex) {
 		ExertionThread eThread = new ExertionThread(ex, this);
 		eThread.start();
 		return eThread;
 	}
 
 	// Sequential
 	protected Exertion execExertion(Exertion ex) throws SignatureException,
 			ExertionException {
 		// set subject before task goes out.
 		// ex.setSubject(subject);
 		ServiceExertion result = null;
 		try {
 			preExecExertion(ex);
 			if (ex instanceof Conditional) {
 				result = (ServiceExertion) execConditional(ex);
 			} else if (ex.isTask()) {
 				//logger.info("CONTEXT BEFORE: " + ex.getDataContext());
 				result = execTask((Task) ex);
 				//logger.info("CONTEXT AFTER: " + ex.getDataContext());
 			} else if (ex.isJob()) {
 				result = execJob((Job) ex);
 			} else {
 				logger.warning("Unknown ServiceExertion");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			// return original exertion with exception
 			result = (ServiceExertion) ex;
 			result.getControlContext().addException(e);
 			result.setStatus(FAILED);
 			setState(ExecState.FAILED);
 			return result;
 		}
 		// set subject after result is received
 		// result.setSubject(subject);
 		postExecExertion(ex, result);
 		return result;
 	}
 
 	protected void postExecExertion(Exertion ex, Exertion result)
 			throws SignatureException, ExertionException {
 		ServiceExertion ser = (ServiceExertion) result;
 		((Job)xrt).setExertionAt(result, ex.getIndex());
 		if (ser.getStatus() > FAILED && ser.getStatus() != SUSPENDED) {
 			ser.setStatus(DONE);
 			if (xrt.getControlContext().isNodeReferencePreserved())
 				try {
 					Jobs.preserveNodeReferences(ex, result);
 				} catch (ContextException ce) {
 					ce.printStackTrace();
 					throw new ExertionException("ContextException caught: "
 							+ ce.getMessage());
 				}
 			// update all outputs from sharedcontext only for tasks. For jobs,
 			// spawned dispatcher does it.
 			if (result.isTask()) {
 				collectOutputs(result);
 			}
 			notifyExertionExecution(ex, result);
 		}
 	}
 
 	/**
 	 * Executes the Conditional exertions to the appropriate providers
 	 * 
 	 * @param exertion
 	 *            Exertion
 	 * @return Exertion
 	 * @throws ExertionException
 	 */
 	private Exertion execConditional(Exertion exertion)
 			throws ExertionException {
 
         Service provider = (Service) Accessor.getService(exertion.getProcessSignature());
 
 		try {
 			return provider.service(exertion, null);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 			throw new ExertionException(
 					"Remote Exception while executing Conditional exertion");
 		} catch (TransactionException e) {
 			e.printStackTrace();
 			throw new ExertionException(
 					"Transaction Exception while executing Conditional exertion");
 		}
 	}
 
 	protected Task execTask(Task task) throws ExertionException,
 			SignatureException, RemoteException {
 //		 try {
 //		 ObjectLogger.persist("tmp.task", task);
 //		 } catch (IOException e) {
 //		 e.printStackTrace();
 //		 }
  
 		if (task instanceof NetTask) {
 			return execServiceTask(task);
 		} else {
 			return task.doTask();
 		}
 	}
 
 	protected Task execServiceTask(Task task) throws ExertionException,
 			SignatureException {
 		Task result;
 		String url = ((NetSignature) task.getProcessSignature()).getPortalURL();
 		try {
 			if (url != null && url.length() != 0) {
 				logger.finer("\n*** getting result from URL... ***\n");
 				result = httpExportTask(task);
 				result.getControlContext().appendTrace("from URL: " + url);
 			} else if (((NetSignature) task.getProcessSignature())
 					.getServicer() == provider) {
 				logger.finer("\n*** getting result from delegate of "
 						+ provider.getProviderName() + "... ***\n");
 				result = ((ServiceProvider) provider).getDelegate().doTask(
 						task, null);
 				result.getControlContext().appendTrace(
 						"delegate of: " + this.provider.getProviderName()
 								+ "=>" + this.getClass().getName());
 			} else {
 				NetSignature sig = (NetSignature) task.getProcessSignature();
 				// retrieve the codebase from the ServiceTask's ServiceMethod
 				// logger.finest("task codebase: " + codebase);
 
 				// Catalog lookup or use Lookup Service for the particular
 				// service
                 // Switched to another method that uses the Cataloger service
 				//Provider provider = ProviderAccessor.getService(
 				//		sig.getProviderName(), sig.getServiceType(), codebase);
                 Service service = (Service) Accessor.getService(sig);
                 //Provider provider = ProviderAccessor.getService(
                 //       		sig.getProviderName(), sig.getServiceType());
 
 				if (service== null) {
 					String msg;
 					// get the PROCESS Method and grab provider name + interface
 					msg = "No Provider Available\n" + "Provider Name:      "
 							+ sig.getProviderName() + "\n"
 							+ "Provider Interface: " + sig.getServiceType();
 							//+ "\n" + "Codebase: " + codebase;
 
 					logger.info(msg);
 					throw new ExertionException(msg, task);
 				} else {
 					// setTaskProvider(task, provider.getProviderName());
 					task.setServicer(service);
 					// client security
 					/*
 					ClientSubject cs = null;
 					 * try{ // //cs =
 					 * (ClientSubject)ServerContext.getServerContextElement
 					 * (ClientSubject.class); }catch (Exception ex){
 					 * Util.debug(this, ">>>No Subject in the server call");
 					 * cs=null; } Subject client = null; if(cs!=null){
 					 * client=cs.getClientSubject(); Util.debug(this,
 					 * "Abhijit::>>>>> CS was not null"); if(client!=null){
 					 * Util.debug(this,"Abhijit::>>>>> Client Subject was not
 					 * null"+client); }else{ Util.debug(this,"Abhijit::>>>>>>
 					 * CLIENT SUBJECT WAS
 					 * NULL!!"); } }else{ Util.debug(this, "OOPS! NULL CS"); }
 					 * if(client!=null&&task.getPrincipal()!=null){
 					 * Util.debug(this,"Abhijit:: >>>>>--------------Inside
 					 * Client!=null, PRINCIPAL != NULL, subject="+client);
 					 * result = (RemoteServiceTask)provider.service(task);
 					 * }else{ Util.debug(this,"Abhijit::
 					 * >>>>>--------------Inside null Subject"); result =
 					 * (RemoteServiceTask)provider.service(task); }
 					 */
 					logger.finer("\n*** getting result from provider... ***\n");
 					result = (NetTask) service.service(task, null);
 
 					if (result!=null)
                         result.getControlContext().appendTrace(
                                 ((Provider)service).getProviderName() + " dispatcher: "
 									+ getClass().getName());
 				}
 			}
 			logger.finer("\n*** got result: ***\n" + result);
 		} catch (Exception re) {
 			task.reportException(re);
 			throw new ExertionException("Dispatcher failed for task: "
 					+ xrt.getName(), re);
 		}
 		return result;
 	}
 	
 	private Job execJob(Job job)
 			throws DispatcherException, InterruptedException,
 			ClassNotFoundException, ExertionException, RemoteException {
 
 		try {
 
 			/*
 			 * check if there is any available jobber in the network and
 			 * delegate the inner job to the available Jobber. In the future, a
 			 * efficient load balancing algorithm should be implemented for
 			 * dispatching inner jobs. Currently, it only does round robin.
 			 */
/*
            // this didn't work before we fixed searching the providers.
             if (job instanceof NetJob) {
                 ServiceItem[] jobbers = Accessor.getServiceItems(Jobber.class, Filters.not(Filters.serviceId(provider.getProviderID())));
                 for (int i = 0; i < jobbers.length; i++) {
                     ServiceItem jobberSInte = jobbers[i];
                     if (jobberSInte != null) {
                         logger.finest("Jobber: " + i + " ServiceID: " + jobberSInte.serviceID);
                         Provider rjobber = (Provider) jobberSInte.service;
 
                         return (Job) rjobber.service(job, null);
                     }
                 }
             }
*/
 
             return execJobLocally(job);
 		} catch (RemoteException re) {
 			re.printStackTrace();
 			throw re;
/*
 		} catch (ExertionException ee) {
 			ee.printStackTrace();
 			throw ee;
*/
 		} catch (DispatcherException de) {
 			de.printStackTrace();
 			throw de;
 		} catch (InterruptedException ie) {
 			ie.printStackTrace();
 			throw ie;
/*
 		} catch (TransactionException te) {
 			te.printStackTrace();
 			throw new ExertionException("transaction failure", te);
*/
 		}
 	}
 
     /*
      * Create a new dispatcher thread for the inner job, if no available
      * Jobber is found in the network
      */
     private Job execJobLocally(Job job) throws DispatcherException, InterruptedException, RemoteException {
         Dispatcher dispatcher;
         runningExertionIDs.addElement(job.getId());
 
         // create a new instance of a dispatcher
         dispatcher = ExertionDispatcherFactory.getFactory()
                 .createDispatcher(job, sharedContexts, true, provider);
         // wait until serviceJob is done by dispatcher
         while (dispatcher.getState() != DONE
                 && dispatcher.getState() != FAILED) {
             Thread.sleep(SLEEP_TIME);
         }
         Job out = (Job) dispatcher.getExertion();
         out.getControlContext().appendTrace(provider.getProviderName()
                 + " dispatcher: " + getClass().getName());
         return out;
     }
 
     protected class ExertionThread extends Thread {
 
 		private Exertion ex;
 
 		private Exertion result;
 
         public ExertionThread(ServiceExertion exertion,
 				ExertDispatcher dispatcher) {
 			ex = exertion;
             if (isMonitored)
 				dispatchers.put(xrt.getId(), dispatcher);
 		}
 
 		public void run() {
 			try {
 				result = execExertion(ex);
 			} catch (ExertionException ee) {
 				ee.printStackTrace();
 				result = ex;
 				((ServiceExertion) result).setStatus(FAILED);
 			} catch (SignatureException eme) {
 				eme.printStackTrace();
 				result = ex;
 				((ServiceExertion) result).setStatus(FAILED);
 			}
 			dispatchers.remove(xrt.getId());
 		}
 
 		public Exertion getExertion() {
 			return ex;
 		}
 
 		public Exertion getResult() {
 			return result;
 		}
 
 	}
 
 }
