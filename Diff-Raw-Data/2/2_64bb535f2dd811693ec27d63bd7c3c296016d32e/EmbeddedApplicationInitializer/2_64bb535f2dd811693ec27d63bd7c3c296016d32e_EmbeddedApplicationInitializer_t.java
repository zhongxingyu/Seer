 package pt.ist.bennu.vaadin;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 
 import myorg._development.PropertiesManager;
import pt.ist.bennu.vaadin.errorHandling.VirtualHostAwareErrorHandler;
 import pt.ist.vaadinframework.ApplicationErrorListener;
 import pt.ist.vaadinframework.EmbeddedApplication;
 import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
 import pt.utl.ist.fenix.tools.util.FileUtils;
 import vaadin.annotation.EmbeddedAnnotationProcessor;
 import vaadin.annotation.EmbeddedComponent;
 
 @SuppressWarnings("serial")
 public class EmbeddedApplicationInitializer extends HttpServlet {
     private static final Set<Class<? extends EmbeddedComponentContainer>> embeddedComponentClasses = new HashSet<Class<? extends EmbeddedComponentContainer>>();
 
     @Override
     public void init(ServletConfig config) throws ServletException {
 	super.init(config);
 
 	loadEmbeddedComponentsFromFile(embeddedComponentClasses);
 	for (Class<? extends EmbeddedComponentContainer> embeddedComponentClass : embeddedComponentClasses) {
 	    EmbeddedComponent embeddedComponent = embeddedComponentClass.getAnnotation(EmbeddedComponent.class);
 	    if (embeddedComponent == null) {
 		continue;
 	    }
 
 	    String[] paths = embeddedComponent.path();
 
 	    for (String path : paths) {
 		try {
 		    EmbeddedApplication.addResolutionPattern(Pattern.compile(path), embeddedComponentClass);
 		} catch (PatternSyntaxException e) {
 		    throw new Error("Error interpreting pattern: " + path, e);
 		}
 	    }
 	}
 
 	initErrorListener();
     }
 
     private void loadEmbeddedComponentsFromFile(final Set<Class<? extends EmbeddedComponentContainer>> embeddedComponentClasses) {
 	final InputStream inputStream = this.getClass().getResourceAsStream("/" + EmbeddedAnnotationProcessor.LOG_FILENAME);
 	if (inputStream != null) {
 	    try {
 		final String contents = FileUtils.readFile(inputStream);
 		for (final String classname : contents.split(EmbeddedAnnotationProcessor.ENTRY_SEPERATOR)) {
 		    try {
 			ClassLoader loader = Thread.currentThread().getContextClassLoader();
 			Class<? extends EmbeddedComponentContainer> type = (Class<? extends EmbeddedComponentContainer>) loader
 				.loadClass(classname);
 			embeddedComponentClasses.add(type);
 		    } catch (final ClassNotFoundException e) {
 			e.printStackTrace();
 		    }
 		}
 	    } catch (final IOException e) {
 		e.printStackTrace();
 	    }
 	} else {
 	    throw new Error("Error opening file: " + EmbeddedAnnotationProcessor.LOG_FILENAME);
 	}
     }
 
     private void initErrorListener() {
 	final String errorListenerClassName = PropertiesManager.getProperty("error.listener.vaadin.class.name");
 	final ApplicationErrorListener errorListener;
 	if (errorListenerClassName == null || errorListenerClassName.isEmpty()) {
 	    System.out.println("Init virtual host aware...");
 	    errorListener = new VirtualHostAwareErrorHandler();
 	} else {
 	    try {
 		final Class errorListenerClass = Class.forName(errorListenerClassName);
 		errorListener = (ApplicationErrorListener) errorListenerClass.newInstance();
 	    } catch (final ClassNotFoundException e) {
 		throw new Error(e);
 	    } catch (final InstantiationException e) {
 		throw new Error(e);
 	    } catch (final IllegalAccessException e) {
 		throw new Error(e);
 	    }
 	}
 	EmbeddedApplication.registerErrorListener(errorListener);
     }
 
 }
