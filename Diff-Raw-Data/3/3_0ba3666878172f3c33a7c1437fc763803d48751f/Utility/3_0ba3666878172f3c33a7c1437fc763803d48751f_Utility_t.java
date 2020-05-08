 import java.util.Collection;
 
 public class Utility {
 	public static <T> String join(Collection<T> collection, String separator) {
 		String out = "";
 
 		Boolean firstElement = true;
 		for (T t : collection) {
 			if (!firstElement)
 				out += separator;
			out += t;
 			firstElement = false;
 		}
 
 		return out;
 	}
 }
