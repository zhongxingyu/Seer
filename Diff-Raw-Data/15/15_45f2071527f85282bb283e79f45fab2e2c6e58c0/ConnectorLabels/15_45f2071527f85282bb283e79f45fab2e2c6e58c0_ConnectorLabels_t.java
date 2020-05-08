 package ussr.builder.labels;
 
 import ussr.comm.Packet;
 import ussr.model.Connector;
 
 /**
  * The main responsibility of this class is to provide support for handling labels
  * previously assigned to the connector;
  * @author Konstantinas
  */
 public class ConnectorLabels extends EntityLabels {
 	
 	/**
 	 * The connector as an entity in simulation.
 	 */
 	private Connector connector;	
 	
 	/**
 	 * Handles the labels assigned to the connector.	
 	 * @param connector, the connector
 	 */
 	public ConnectorLabels(Connector connector){
 	this.connector = connector;	
 	}
 	
 	/**
 	 * Checks if the connector was assigned the label passed as a string.
 	 * NOTE: IT IS SIMPLE CONTAINS CHECK FOR NOW.
 	 * It is the method following STRATEGY pattern.
 	 * @param label, the label name to check;
 	 * @return true, if passed label was assigned to the connector, false - if not. 
 	 */
 	@Override
 	public boolean has(String label) {		
 		if (getEntityLabels(connector).contains(label)){
 			return true;
 		}
 		return false;		
 	}
 
	// @Override JDK 1.5
 	public void sendMessage(int connectorNr, Packet packet) {
 		// TODO Auto-generated method stub
 		
 	}
 
	// @Override JDK 1.5
 	public String receiveMessage(int connectorNr) {
 		return null;
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public String getLabels() {
 		return getEntityLabels(connector);		
 	}		
 	
 }
