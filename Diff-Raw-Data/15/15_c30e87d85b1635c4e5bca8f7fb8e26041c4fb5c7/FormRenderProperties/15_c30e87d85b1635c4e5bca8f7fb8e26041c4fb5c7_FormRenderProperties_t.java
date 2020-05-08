 package ar.com.cuyum.cnc.utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.ApplicationScoped;
 
 import org.apache.log4j.Logger;
 
 import ar.com.cuyum.cnc.exceptions.TechnicalException;
 
 
 @SuppressWarnings("serial")
 @ApplicationScoped
 public class FormRenderProperties extends Properties {
 
 	public static final String DEFAULT_FILE_NAME = "formrender.properties";
 	
 	static Logger log = Logger.getLogger(FormRenderProperties.class);
 
 	@PostConstruct
 	protected void init() {
 
 		try {
 			InputStream is = this.getClass().getClassLoader()
 					.getResourceAsStream(DEFAULT_FILE_NAME);
 			this.load(is);
 		} catch (IOException e) {
 			log.error(e.getMessage());
 			throw new TechnicalException (
 					"No puedo cargar el arhivo properties " + DEFAULT_FILE_NAME,
 					e);
 		}
 	}
 	
 	public String getDestinationXml(){
 		return this.getProperty("xmlForms.destination");
 	}
 	
 	public String getRemoteListHost(){
 		return this.getProperty("list.remote.host")+":"+this.getProperty("list.remote.port");
 	}
	
	public String getRemoteSubmissionHost(){
		String rsh = this.getProperty("submit.remote.host")+":"+this.getProperty("submit.remote.port");
		log.info(rsh);
		return rsh;
	}
 }
