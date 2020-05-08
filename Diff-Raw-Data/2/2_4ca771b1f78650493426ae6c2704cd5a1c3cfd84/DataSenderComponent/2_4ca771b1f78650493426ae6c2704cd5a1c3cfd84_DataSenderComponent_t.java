 package atl.space.entities;
 
 import java.util.List;
 
 public class DataSenderComponent extends Component{
 	//this might wanna be abstract actually
 	
 	//sends data
 	//Not sure in what form.
 	//A list of entities perhaps. I feel like maybe an extension for Entity would be in order,
 	//like an IncompleteEntity, which can have indefinite components to account for sensors
 	//not being 100% accurate or not providing every little bit of info about an object
 	
 	//Scanners
 	//Sensors
 	//Communications
 	
 	
 	public DataSenderComponent(){
 		id = "datasender";
 	}
 	public DataSenderComponent(DataSenderComponent dsc){
 		id = dsc.getId();
 	}
 	
 	/*
	 * public SOMETYPEOFDATAMAYBEANARRAYLIST<IncompleteEntity> getData(List<Entity> entitiesInTheWorld){
 	 *	//HEY GUY! HEY!	
 	 * 
 	 * }
 	*/
 	@Override
 	public Component clone() {
 		Component temp = new DataSenderComponent(this);
 		return temp;
 	}
 
 	@Override
 	public void update(int delta, List<Entity> entities) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Component getStepped(int delta, List<Entity> entities) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	//Data
 	
 	
 }
