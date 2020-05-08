 package oop.JavaGrinder.cc;
 
 /**
  * Your Special Friend
  */
 public class ccHelper {
 
 	public static String convertType(String type){
 		if(type.matches("byte"))			return "int8_t";
 		else if(type.matches("short"))		return "int16_t";
 		else if(type.matches("int"))		return "int32_t";
		else if(type.matches("float"))		return "int32_t";
 		else if(type.matches("long"))		return "int64_t";
		else if(type.matches("double"))		return "int64_t";
 		else if(type.matches("boolean"))	return "bool";
 		else								return type;
 	}
 }
