 package it.geosolutions.geobatch.destination.action.changevalue;
 
 import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
 import it.geosolutions.geobatch.actions.ds2ds.util.FeatureConfigurationUtil;
 import it.geosolutions.geobatch.annotations.Action;
 import it.geosolutions.geobatch.annotations.CheckConfiguration;
 import it.geosolutions.geobatch.destination.ingestion.ChangeValueProcess;
 import it.geosolutions.geobatch.flow.event.action.ActionException;
 import it.geosolutions.geobatch.flow.event.action.BaseAction;
 
 import java.io.IOException;
 import java.util.EventObject;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.Transaction;
 import org.geotools.jdbc.JDBCDataStore;
 
 @Action(configurationClass = ChangeValueConfiguration.class)
 public class ChangeValueAction extends BaseAction<EventObject> {
 
 
 	public ChangeValueAction(ChangeValueConfiguration actionConfiguration) throws IOException {
 		super(actionConfiguration);
 	}
 
 	@Override
     @CheckConfiguration
     public boolean checkConfiguration() {
         if(getConfiguration().isFailIgnored()) {
             LOGGER.warn("FailIgnored is true. This is a multi-step action, and can't proceed when errors are encountered");
             return false;
         }
 
         return true;
     }
 	
 	public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {
         listenerForwarder.setTask("Check config");
         if (getConfiguration() == null) {
             throw new IllegalStateException("ActionConfig is null.");
         }
         
         final LinkedList<EventObject> ret = new LinkedList<EventObject>();
         try {
 			listenerForwarder.started();
 			while (!events.isEmpty()) {
 				EventObject event = events.poll();
 				
 				ChangeValueConfiguration configuration = (ChangeValueConfiguration)getConfiguration();
 				FeatureConfiguration featureConfiguration = configuration.getOutputFeature();
 				DataStore ds = FeatureConfigurationUtil
 						.createDataStore(featureConfiguration);
 				if (ds == null) {
 					throw new ActionException(this, "Can't find datastore ");
 				}
 				Transaction transaction = new DefaultTransaction();
 				try {
 					if (!(ds instanceof JDBCDataStore)) {
 						throw new ActionException(this, "Bad Datastore type "
 								+ ds.getClass().getName());
 					}
 					
 					ChangeValueProcess changeValueProcess = new ChangeValueProcess(ds, transaction, featureConfiguration.getTypeName());
 					changeValueProcess.execute(configuration.getFilter(),configuration.getAttribute(),configuration.getValue());
 					transaction.commit();
 					listenerForwarder.progressing(100, "Completed");
 					// pass the feature config to the next action
 					ret.add(event);
 				} catch (Exception ex) {
 					LOGGER.error(ex.getMessage(),ex);
 					transaction.rollback();
 					listenerForwarder.progressing(100, "Completed with errors");
 					throw ex;
 				} finally {
					transaction.close();
 					ds.dispose();
 				}
 				
 			}
 			listenerForwarder.completed();
 	        return ret;
         } catch (Exception t) {
 			listenerForwarder.failed(t);
 			throw new ActionException(this, t.getMessage(), t);
 		}
     }
 	
 }
