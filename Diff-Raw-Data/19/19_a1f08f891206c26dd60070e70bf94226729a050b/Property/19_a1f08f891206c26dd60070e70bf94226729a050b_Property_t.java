 package configuration;
 
import java.io.Serializable;

public class Property implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7819349848595979245L;
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
