 package configuration;
 
public class Property {
 	private Object data;
 	private Usage type;
 	
 	public Property(Usage type, Object initialData){
 		this.type = type;
 		data = initialData;
 	}
 	
 	public Property(Usage type){
 		this(type, null);
 	}
 	
 	public Usage getUsage(){
 		return type;
 	}
 	
 	//care about them changing stuff? can that even be solved?
 	public Object getValue(){
 		return data;
 	}
 }
