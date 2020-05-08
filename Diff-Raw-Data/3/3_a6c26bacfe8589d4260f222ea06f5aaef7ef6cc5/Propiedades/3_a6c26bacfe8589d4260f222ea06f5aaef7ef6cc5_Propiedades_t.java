 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cl.ufro.infocleta.core.conf;
 
 import static cl.ufro.infocleta.core.conf.ConfUtils.stringToLaf;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Properties;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * <p>
  * </p>
  * 
  * @author kristian
  */
 public class Propiedades {
 
 	private static final String NOMBRE_ARCHIVO = "rc.conf";
 	private static final String KEY_LAF = "aplicacion.laf";
 	private Logger LOG = LoggerFactory.getLogger(Propiedades.class);
 	private Properties prop;
 
 	public Propiedades() {
 		FileInputStream file = null;
 		URL root = getClass().getProtectionDomain().getCodeSource()
 		        .getLocation();
 		URL filePath = null;
 
 		prop = new Properties();
 		try {
 			filePath = new URL(root, NOMBRE_ARCHIVO);
                        file = new FileInputStream(filePath.getPath());
 			prop.load(file);
 			file.close();
 		} catch (IOException e) {
 			LOG.error("# Error al leer el archivo de configuración", e);
 		}
 	}
 
 	/**
 	 * <p>
 	 * Obtiene el <code>LookAndFeel</code> establecido en las propiedades.
 	 * </p>
 	 * <b>LAF getLookAndFeel()</b>
 	 * 
 	 * @return
 	 */
 	public Laf getLookAndFeel() {
 		Laf laf = Laf.SYSTEM;
 		if (prop == null)
 			return laf;
 
 		String property = prop.getProperty(KEY_LAF);
 
 		if (property == null) {
 			return laf;
 		}
 		laf = stringToLaf(property);
 		return laf;
 	}
 }
