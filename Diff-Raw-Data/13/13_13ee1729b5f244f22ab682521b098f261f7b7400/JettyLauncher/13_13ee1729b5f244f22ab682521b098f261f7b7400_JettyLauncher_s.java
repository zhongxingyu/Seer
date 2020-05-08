 package infrastructure;
 
 import static java.lang.Integer.parseInt;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.timgroup.tucker.info.Stoppable;
 import com.timgroup.tucker.info.component.AvailableComponent;
 import com.timgroup.tucker.info.component.JarVersionComponent;
 import com.timgroup.tucker.info.servlet.ApplicationInformationServlet;
 import com.timgroup.tucker.info.status.StatusPageGenerator;
 
 public class JettyLauncher {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(JettyLauncher.class);
 
     private final Server server;
     private final ServletContextHandler context = new ServletContextHandler();
     private final AvailableComponent availableComponent = new AvailableComponent();
 
     public JettyLauncher(int port) {
         this.server = new Server(port);
     }
 
     private void setupWebApp() throws IOException {
         context.setContextPath("/");
         context.setMaxFormContentSize(0);
 
         StatusPageGenerator statusPage = new StatusPageGenerator("reference-implementation", new JarVersionComponent(StatusPageGenerator.class));
         statusPage.addComponent(availableComponent);
 
        ApplicationInformationServlet statusPageServlet = new ApplicationInformationServlet(statusPage, Stoppable.ALWAYS_STOPPABLE);
 
         context.getServletHandler().setStartWithUnavailable(false);
         context.addServlet(new ServletHolder(statusPageServlet), "/info/*");
         context.addServlet(new ServletHolder(new StopServlet()), "/stop");
         context.addServlet(new ServletHolder(new MakeAvailableServlet()), "/makeavailable");
         context.addServlet(new ServletHolder(new MakeUnvailableServlet()), "/makeunavailable");
         server.setHandler(context);
     }
 
     public void sink() throws Exception {
         server.stop();
     }
 
     public void launch() throws Exception {
         LOGGER.info("Setting up web application");
         setupWebApp();
 
         try {
             LOGGER.info("Starting server");
             server.start();
             LOGGER.info("Started server");
         } catch (Exception e) {
             LOGGER.error("Couldn't start Jetty", e);
             stopQuietly();
             throw e;
         }
     }
 
     private void stopQuietly() {
         try {
             server.stop();
         } catch (InterruptedException e) {
             LOGGER.info("Told to stop serving");
         } catch (Exception e) {
             LOGGER.error("Couldn't stop Jetty either", e);
         }
     }
 
     public static void main(String[] args) throws Exception {
         new JettyLauncher(parseInt(args[0])).launch();
     }
 
     private class MakeAvailableServlet extends HttpServlet {
         private static final long serialVersionUID = 1L;
 
         @Override
         protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
             availableComponent.makeAvailable();
         }
     }
 
     private class MakeUnvailableServlet extends HttpServlet {
         private static final long serialVersionUID = 1L;
 
         @Override
         protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
             availableComponent.makeUnavailable();
         }
     }
 
     private class StopServlet extends HttpServlet {
         private static final long serialVersionUID = 1L;
 
         @Override
         protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
             stopQuietly();
         }
     }
 
 }
