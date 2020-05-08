 import java.util.Collection;
 
 public class Utility {
 	public static <T> String join(Collection<T> collection, String separator) {
 		String out = "";
 
 		Boolean firstElement = true;
 		for (T t : collection) {
			out += t;
 			if (!firstElement)
 				out += separator;
 			firstElement = false;
 		}
 
 		return out;
 	}
 }
