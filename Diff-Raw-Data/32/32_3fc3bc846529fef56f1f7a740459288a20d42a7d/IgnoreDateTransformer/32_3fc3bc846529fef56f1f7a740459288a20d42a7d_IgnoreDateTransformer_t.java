 package rjson.domain;
 
import java.lang.reflect.Field;
 
import rjson.transformer.tojson.FieldBasedTransformer;

public class IgnoreDateTransformer extends FieldBasedTransformer {
 	public boolean canConvertToJson(Object object) {
		if (object instanceof rjson.domain.Account)
 			return true;
 		return false;
 	}
	
	public boolean exclude(Field field) {
		if(field.getName().equals("lastUpdate")) {
			return true;
		}
		return false;
 	}
 }
