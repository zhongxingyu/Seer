 package cat.mobilejazz.database;
 
import com.google.compatibility.gson.JsonElement;
 
 public interface DataParser<T> {
 
 	T parse(JsonElement input);
 
 }
