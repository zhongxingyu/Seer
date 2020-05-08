 package utility;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import server.ServerProperties;
 
 public class PropertiesLoader {
 	
 	
 	private static final String SERVERCONFIG_XML = "./config/server.config";
 
	
	
	public static void main(String[] args) throws FileNotFoundException, JAXBException {
		System.out.println(PropertiesLoader.Load().getIP().equals(""));
	}
	
	
 	public static ServerProperties Load() throws JAXBException, FileNotFoundException {
 		JAXBContext context = JAXBContext.newInstance(ServerProperties.class);
 	    // get variables from our xml file, created before
 	    System.out.println();
 	    System.out.println("Output from our XML File: ");
 	    Unmarshaller um = context.createUnmarshaller();
 	    ServerProperties sProp = (ServerProperties) um.unmarshal(new FileReader(SERVERCONFIG_XML));
 	    System.out.println("IP: " + sProp.getIP() + " port: "
 	          + sProp.getPort());
 	    return sProp;
 	}
 
 }
