 /**
  * 
  */
package resources;
 
 /**
  * @author TDEWEERD
  *
  */
 public class LocateResource {
 
 	/**
 	 * Retrieves a resource path + filename
 	 * @param filename filename to retrieve
 	 * @return path + filename
 	 */
 	public static String getResource(String filename) {
		return ClassLoader.getSystemClassLoader().getResource(filename).toString();
 	}
 }
