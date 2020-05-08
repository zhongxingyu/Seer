 package topshelf.utils.web.bindery;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import org.apache.log4j.Logger;
 
 import com.google.web.bindery.requestfactory.server.DefaultExceptionHandler;
 import com.google.web.bindery.requestfactory.server.RequestFactoryServlet;
 import com.google.web.bindery.requestfactory.server.ServiceLayerDecorator;
 import com.google.web.bindery.requestfactory.shared.ServerFailure;
 
 /**
 * Use our Guice enabled ServiceLayerDecorator to create the servlet that
  * backs all of our client side RequestFactory services.
  * 
  * @author bloo
  *
  */
 @SuppressWarnings("serial")
 @Singleton
 public class GuiceRequestFactoryServlet extends RequestFactoryServlet {
 
 	@Inject
 	public GuiceRequestFactoryServlet(final ServiceLayerDecorator sld) {
 		super(new EH(), sld);
 	}
 	
 	static class EH extends DefaultExceptionHandler {
 		  
 		Logger logger = Logger.getLogger(GuiceRequestFactoryServlet.class);
 		
 		@Override
 		public ServerFailure createServerFailure(Throwable throwable) {
 			logger.error("Failed to process GWT Request", throwable);
 			return super.createServerFailure(throwable);
 		}
 	}
 }
