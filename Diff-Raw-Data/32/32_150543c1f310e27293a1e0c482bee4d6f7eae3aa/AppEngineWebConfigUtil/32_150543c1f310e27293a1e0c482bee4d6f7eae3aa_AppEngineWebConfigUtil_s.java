 package net.sue445.s3tiger.internal;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Map;
 
 import org.slim3.util.WrapRuntimeException;
 
 public final class AppEngineWebConfigUtil {
 	private static final String FILE_APPENGINE_WEB_XML = "war/WEB-INF/appengine-web.xml";
 	private static final String XPATH_SYSTEM_PROPERTIES = "/appengine-web-app/system-properties/property";
 
 
 	private AppEngineWebConfigUtil(){
 
 	}
 
 	/**
 	 * get system properties from appengine-web.xml
 	 * @return
 	 */
 	public static Map<String, String> getSystemProperties(){
 		return readSystemPropertiesMap(FILE_APPENGINE_WEB_XML);
 	}
 
 	/**
 	 * @param filepath
 	 * @return
 	 */
 	private static Map<String, String> readSystemPropertiesMap(String filepath){
 		return readXmlBase(
 				filepath,
 				new Callback<Map<String, String>>() {
 					@Override
 					public Map<String, String> execute(XPathEvaluator evalutor) {
						return evalutor.getMap(XPATH_SYSTEM_PROPERTIES, null);
 					}
 				});
 	}
 
 	/**
 	 * @author sue445
 	 * @param <T>
 	 */
 	public interface Callback<T>{
 		/**
 		 * read xml
 		 * @param evalutor
 		 * @return
 		 */
 		T execute(XPathEvaluator evalutor);
 	}
 
 	/**
 	 *
 	 * @param filepath
 	 * @param callback
 	 * @return
 	 */
 	private static <T> T readXmlBase(String filepath, Callback<T> callback) {
 		InputStream stream = null;
 
 		try {
 			stream = new BufferedInputStream(new FileInputStream(filepath));
 			XPathEvaluator evalutor = new XPathEvaluator(stream);
 			return callback.execute(evalutor);
 
 		} catch(Exception e){
 			throw new WrapRuntimeException(e);
 
 		} finally{
 			if(stream != null){
 				try {
 					stream.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 }
