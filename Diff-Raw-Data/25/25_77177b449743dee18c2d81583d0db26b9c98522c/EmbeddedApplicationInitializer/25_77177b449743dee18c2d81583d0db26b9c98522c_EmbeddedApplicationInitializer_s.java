 package pt.ist.bennu.vaadin;
 
 import java.io.IOException;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 
 import pt.ist.vaadinframework.EmbeddedApplication;
 import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
 
 @SuppressWarnings("serial")
 public class EmbeddedApplicationInitializer extends HttpServlet {
     @Override
     public void init(ServletConfig config) throws ServletException {
 	super.init(config);
 	try {
 	    Properties properties = new Properties();
	    properties.load(getClass().getResourceAsStream("/embeddedcomponent.properties"));
	    for (Entry<Object, Object> entry : properties.entrySet()) {
		try {
		    Class<? extends EmbeddedComponentContainer> type = (Class<? extends EmbeddedComponentContainer>) Class
			    .forName((String) entry.getValue());
		    EmbeddedApplication.addResolutionPattern(Pattern.compile((String) entry.getKey()), type);
		} catch (PatternSyntaxException e) {
		    throw new Error("Error interpreting pattern: " + entry.getKey(), e);
		} catch (ClassNotFoundException e) {
		    throw new Error("Class: " + entry.getValue() + " not found for pattern: " + entry.getKey(), e);
 		}
 	    }
 	} catch (IOException e) {
 	    e.printStackTrace();
 	    throw new Error("Could not read property file: embeddedcomponent.properties");
 	}
     }
 }
