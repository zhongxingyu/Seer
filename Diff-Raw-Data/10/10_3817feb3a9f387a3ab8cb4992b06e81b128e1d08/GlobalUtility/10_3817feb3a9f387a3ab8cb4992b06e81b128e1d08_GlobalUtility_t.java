 package dk.dtu.imm.distributedsystems.projects.sensornetwork.common;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.util.Properties;
 
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.channels.Channel;
 
 /**
  * The Class GlobalUtility.
  *
  * @author Maciej Kucharek <a href="mailto:s091828 (at) student.dtu.dk">s091828 (at) student.dtu.dk</a>
  */
 public class GlobalUtility {
 
 	/** The Constant ACK_TIMEOUT_MS. */
 	public static final int ACK_TIMEOUT_MS = 1000;
 
 	/** The Constant UDP_PACKET_SIZE. */
 	public static final int UDP_PACKET_SIZE = 512;
 	
 	public static final String TEMPLATE_PROPERTIES_FILE_NAME = "template.properties";
 	
 	public static final String VALUE_DELIMITER = ";";
 	
 	public static final String PACKET_DEFAULT_VALUE="";
 
 	/**
 	 * Gets the properties.
 	 *
 	 * @param filename the filename
 	 * @return the properties
 	 * @throws IOException 
 	 * @throws FileNotFoundException the file not found exception
 	 * @throws URISyntaxException 
 	 */
 	public static Properties getPropertiesFromClasspath(String filename) throws FileNotFoundException, IOException, URISyntaxException {
 
 		InputStream is;
 		Properties properties = new Properties();
 
 		is = GlobalUtility.class.getClassLoader().getResourceAsStream(filename);
 		properties.load(is);
		
		is.close();
 
 		return properties;
 	}
 	
 	public static Properties getProperties(String filename) throws FileNotFoundException, IOException, URISyntaxException {
 
	        InputStream is;
 		Properties properties = new Properties();
 		
		is = new FileInputStream(filename);
		properties.load(is);
		
		is.close();
 
 		return properties;
 	}
 	
 	public static Channel[] getChannelArray(String[] ids, String[] ips, String[] portNumbers) {
 		
 		if(ids.length != ips.length || ips.length != portNumbers.length) {
 			throw new IllegalStateException("Cannot construct Channel array - different number of provided parameters");
 		}
 		
 		if("".equals(ids[0]) && "".equals(ips[0]) && "".equals(portNumbers[0])) {
 			// no right channels specified
 			return new Channel[0];
 		}
 		
 		Channel[] channels = new Channel[ids.length];
 		
 		for(int i=0; i<ids.length; ++i) {
 			
 			// renaming localhost to 127.0.0.1 in order to enable sender id resolution
 			if(ips[i].equals("localhost")) {
 				ips[i] = "127.0.0.1";
 			}
 			
 			channels[i] = new Channel(ids[i], 
     				ips[i], 
     				Integer.parseInt(portNumbers[i]));
     	}
 		
 		return channels;
    
 	}
 
 	/**
 	 * Convert string arrayto int array.
 	 *
 	 * @param sarray the sarray
 	 * @return the int[]
 	 */
 	public static int[] convertStringArraytoIntArray(String[] sarray) {
 		int intarray[] = new int[sarray.length];
 
 		if (sarray[0].length() == 0) {
 			intarray[0] = -1;
 		} else {
 			for (int i = 0; i < sarray.length; i++) {
 				intarray[i] = Integer.parseInt(sarray[i]);
 			}
 		}
 
 		return intarray;
 	}
 
 }
