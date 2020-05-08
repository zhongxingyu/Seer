 package cat.mobilejazz.database;
 
import com.google.gson.JsonElement;
 
 /** just a placeholder **/
 public final class IdentityParser implements DataParser<Object> {
 
 	private IdentityParser() {
 	}
 
 	@Override
 	public Object parse(JsonElement input) {
 		return input;
 	}
 
 }
