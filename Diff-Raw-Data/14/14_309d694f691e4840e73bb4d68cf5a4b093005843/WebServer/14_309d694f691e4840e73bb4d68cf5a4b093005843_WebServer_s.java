 package fr.ybonnel.codestory;
 
 import fr.ybonnel.codestory.util.LogUtil;
 import fr.ybonnel.codestory.path.PathType;
 import fr.ybonnel.codestory.query.QueryType;
 import fr.ybonnel.codestory.util.Chrono;
 import org.apache.commons.io.IOUtils;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.handler.AbstractHandler;
 import sun.misc.Signal;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class WebServer extends AbstractHandler {
 
     public static final String QUERY_PARAMETER = "q";
     public static final String HEADER_SERVER = "Server";
     public static final String SERVER_NAME = "YboServer";
     public static final String DEFAULT_CONTENT_TYPE = "text/html;charset=utf-8";
     public static final int DEFAULT_PORT = 10080;
 
 
     @Override
     public void handle(String target,
                        HttpServletRequest request,
                        HttpServletResponse httpResponse,
                        int dispatch)
             throws IOException, ServletException {
 
         Chrono chronoWithNetwork = new Chrono().start();
         String payLoad = getPayload(request);
 
         LogUtil.logRequestUrl(request);
 
         Chrono chronoWithoutNetwork = new Chrono().start();
         WebServerResponse response = processRequest(request, payLoad);
         chronoWithoutNetwork.stop();
 
 
         fillHttpResponse(httpResponse, response);
 
         chronoWithNetwork.stop();
 
        LogUtil.logHttpRequest(request, payLoad, chronoWithNetwork.getTimeInNs(), chronoWithNetwork.getTimeInNs(), response);
     }
 
     /**
      * Fill httpResponse with content of response.
      */
     private void fillHttpResponse(HttpServletResponse httpResponse, WebServerResponse response) throws IOException {
         httpResponse.setHeader(HEADER_SERVER, SERVER_NAME);
         httpResponse.setStatus(response.getStatusCode());
         if (response.getContentType() != null) {
             httpResponse.setContentType(response.getContentType());
         } else {
             httpResponse.setContentType(DEFAULT_CONTENT_TYPE);
         }
         PrintWriter writer = httpResponse.getWriter();
         writer.print(response.getResponse());
         writer.close();
     }
 
     /**
      * Process the request.
      */
     private WebServerResponse processRequest(HttpServletRequest request, String payLoad) {
         WebServerResponse response;
 
         try {
             String query = request.getParameter(QUERY_PARAMETER);
             if (query != null) {
                 response = QueryType.getResponse(query);
             } else {
                 response = PathType.getResponse(request, payLoad);
             }
         } catch (Exception e) {
             e.printStackTrace();
             response = new WebServerResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
         }
         return response;
     }
 
     /**
      * Get the content of payload.
      */
     private String getPayload(HttpServletRequest request) throws IOException {
         String payLoad;
         try {
             payLoad = IOUtils.toString(request.getInputStream());
         } catch (Exception exception) {
             payLoad = IOUtils.toString(request.getReader());
         }
         return payLoad;
     }
 
 
     public static void main(String... args) throws Exception {
         LogUtil.logMessage("CodeStory starting");
         int port = DEFAULT_PORT;
         if (args.length == 1) {
             port = Integer.parseInt(args[0]);
         }
 
         Server server = new Server(port);
         Signal.handle(new Signal("TERM"), new StopHandler(server));
         server.setHandler(new WebServer());
         server.start();
         server.join();
 
         LogUtil.logMessage("CodeStory stopped");
     }
 
 }
