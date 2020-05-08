 package doo.daba.java.util;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Properties;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 
 
 
 /**
  * Esta clase se encarga de reemplazar el placeholder configurer de Spring
  * para interceptar el metodo de carga de propiedades cargadas via archivo
  * properties.
  * 
  * @author Gerardo Aquino
  * @since 09/03/2011
  * @version 1.0
  */
 public class Propiedades extends PropertyPlaceholderConfigurer {
 
 
    static {
        mapaDePropiedades = new HashMap<String, String> ();
    }


 	/**
 	 * Propiedad que almacena las propiedades leidas del archivo properties
 	 */
 	private static Map<String, String> mapaDePropiedades;
 
 
 
     /**
      * Metodo encargado de realizar la carga del archivo de propiedades.
      * 
      * Este metodo esta sobreescrito para reemplazarlo de su clase padre
      * {@code PropertyPlaceholderConfigurer} y garantizar la carga de 
      * propiedades en la propiedad {@link #mapaDePropiedades}
      * 
      * @see PropertyPlaceholderConfigurer
      */
 	@Override
 	@SuppressWarnings("deprecation")
     protected void processProperties (ConfigurableListableBeanFactory beanFactory, Properties props) {
 		super.processProperties (beanFactory, props);
 
 		for (Object key : props.keySet ()) {
 			String keyStr = key.toString ();
 			mapaDePropiedades.put (
 					keyStr,
 					parseStringValue (
                             props.getProperty (keyStr),
                             props, new HashSet<String> ()));
 		}
 	}
 
 
 
 	public static String obtener (String name, Object ... params) {
 
         return (params == null || params.length == 0) ?
                 mapaDePropiedades.get(name).toString()
                 :
                 String.format(mapaDePropiedades.get(name).toString(), params);
 
     }
 	
 }
