 package objects;
 
 import java.util.Vector;
 
 public class Item extends AbstractObject implements ObjectInterface{
 	
 	private String type;
 	private String id;
	private String name;
 	
 	private String visualName;
 	private String visualDescription;
 	
 	private String auralName;
 	private String auralDescription;
 	
 	private Vector<String> properties;
 	
 	public Item(){
 		type = "item";
 		id = "";
 		visualName = "";
 		visualDescription = "";
 		auralName = "";
 		auralDescription = "";
 		properties = new Vector<String>();
 	}
 	
 	public Item(String id, String visName, String visDesc, String aurName, String aurDesc, Vector<String> props){
 		type = "item";
 		this.id = id;
 		visualName = visName;
 		visualDescription = visDesc;
 		auralName = aurName;
 		auralDescription = aurDesc;
 		properties = props;
 	}
 
 // constructor for creating an Item with only an ID.
 // I don't know if this is necessary.
 	public Item(String id){
 		type = "item";
 		this.id = id;
 		visualName = null;
 		visualDescription = null;
 		auralName = null;
 		auralDescription = null;
 		properties = null;
 		
 	}
 
 	public void addProperty(String property){
 		//Check to make sure scene does not already contain the item
 		if(!properties.contains(property)){
 			properties.add(property);
 		}
 		else
 			System.out.println(this.id+" already contains "+property);
 	}
 	
 	//We'll do this for now, may change later
 	public void removeProperty(String property){
 		int i=0;
 		//Search for first occurrence of "property" and removes it from array. We are guaranteed a single occurrence of
 		//a property by addProperty(String)
 		while(!properties.get(i).equalsIgnoreCase(property)){
 			i++;
 			if(i==properties.size()){
 				System.out.println("Property "+property+" was not found.");
 				return;
 			}
 		}
 		properties.remove(property);
 	}
 	public String getType() {
 		return type;
 	}
 
 	public String getId() {
 		return id;
 	}
 	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
 	public void setId(String id){
 		this.id = id;
 	}
 
 	public String getVisualName() {
 		return visualName;
 	}
 
 	public void setVisualName(String visualName) {
 		this.visualName = visualName;
 	}
 
 	public String getVisualDescription() {
 		return visualDescription;
 	}
 
 	public void setVisualDescription(String visualDescription) {
 		this.visualDescription = visualDescription;
 	}
 
 	public String getAuralName() {
 		return auralName;
 	}
 
 	public void setAuralName(String auralName) {
 		this.auralName = auralName;
 	}
 
 	public String getAuralDescription() {
 		return auralDescription;
 	}
 
 	public void setAuralDescription(String auralDescription) {
 		this.auralDescription = auralDescription;
 	}
 
 	public Vector<String> getProperties() {
 		return properties;
 	}
 	
 }
