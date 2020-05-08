 package ussr.builder.labels;
 
 import ussr.comm.Packet;
 import ussr.model.Sensor;
 
 /**
  * The main responsibility of this class is to provide support for handling labels
  * previously assigned to the sensors of the module.
  * @author Konstantinas
  *
  */
 public class SensorLabels extends EntityLabels {
 	
 	/**
 	 * The sensor as an entity in simulation.
 	 */
 	private Sensor sensor;
 	
 	/**
 	 * Handles the labels assigned to the module.
 	 * @param sensor, the sensor
 	 */
 	public SensorLabels(Sensor sensor){
 	this.sensor = sensor;	
 	}
 
 	/**
 	 * Checks if the sensor was assigned the label passed as a string.
 	 * NOTE: IT IS SIMPLE CONTAINS CHECK FOR NOW.
 	 * It is the method following STRATEGY pattern.
 	 * @param label, the label name to check;
 	 * @return true, if passed label was assigned to the sensor, false - if not. 
 	 */
 	@Override
 	public boolean has(String label) {		
 		if (getEntityLabels(sensor).contains(label)){
 			return true;
 		}
 		return false;		
 	}
 
	@Override
 	public void sendMessage(int connectorNr, Packet packet) {
 		
 	}
 
	@Override
 	public String receiveMessage(int connectorNr) {
 		return null;
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public String getLabels() {	
 		return  getEntityLabels(sensor);
 	}	
 	
 }
