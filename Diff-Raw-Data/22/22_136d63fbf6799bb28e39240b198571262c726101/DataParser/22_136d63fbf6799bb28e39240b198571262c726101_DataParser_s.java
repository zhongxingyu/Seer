 package cat.mobilejazz.database;
 
import com.google.gson.JsonElement;
 
 public interface DataParser<T> {
 
 	T parse(JsonElement input);
 
 }
