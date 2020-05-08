 package eu.neq.mais.connector;
 
 import eu.neq.mais.Main;
 import eu.neq.mais.connector.impl.GNUHealthConnectorImpl;
 import eu.neq.mais.connector.impl.TestDataConnectorImpl;
 import eu.neq.mais.domain.gnuhealth.PatientGnu;
 
 /**
  * Loads a back-end connector based no the configuration of the mais.
  * @author seba
  *
  */
 public abstract class ConnectorFactory {
 	
 
 	public static Connector getConnector(BackendType type) {
 		
		BackendType tt = BackendType.gnuhealth;
 		
 		switch(type){
 			case gnuhealth: return GNUHealthConnectorImpl.getInstance();
 			case testdata: return TestDataConnectorImpl.getInstance();
 		default: return TestDataConnectorImpl.getInstance();
 		}
 	}
 
 
 }
