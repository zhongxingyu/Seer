 /**
  * 
  */
 package ch.rollis.emma;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import ch.rollis.emma.contenthandler.ContentHandler;
 import ch.rollis.emma.contenthandler.ContentHandlerFactory;
 import ch.rollis.emma.context.ServerContext;
 import ch.rollis.emma.context.ServerContextManager;
 import ch.rollis.emma.request.Request;
 import ch.rollis.emma.response.Response;
 import ch.rollis.emma.response.ResponseFactory;
 import ch.rollis.emma.response.ResponseStatus;
 import ch.rollis.emma.util.DateConverter;
 import ch.rollis.emma.util.DateConverterException;
 
 
 /**
  * @author mrolli
  *
  */
 public class RequestHandler implements Runnable {
     private final Socket comSocket;
     private final ServerContextManager scm;
     private final Logger logger;
 
     public RequestHandler(Socket socket, Logger logger, ServerContextManager contextManager) {
         this.comSocket = socket;
         this.scm = contextManager;
         this.logger = logger;
     }
     @Override
     public void run() {
         logger.log(Level.INFO, Thread.currentThread().getName() + " started.");
 
         InetAddress client = comSocket.getInetAddress();
 
         try {
             InputStream input = comSocket.getInputStream();
             OutputStream output = comSocket.getOutputStream();
 
             HttpProtocolParser parser = new HttpProtocolParser(input);
             try {
                 Request request = parser.parse();
                 ServerContext context = scm.getContext(request);
                 ContentHandler handler = new ContentHandlerFactory().getHandler(request);
                 Response response = handler.process(request, context);
                 response.send(output);
                 log(logger, client, request, response);
             } catch (HttpProtocolException e) {
                 logger.log(Level.WARNING, "HTTP protocol violation", e);
                 Response response = new ResponseFactory().getResponse(ResponseStatus.BAD_REQUEST);
                 response.send(output);
             } catch (Exception e) {
                 logger.log(Level.SEVERE, "RequestHandler cannot handle request", e);
                 Response response = new ResponseFactory()
                 .getResponse(ResponseStatus.INTERNAL_SERVER_ERROR);
                 response.send(output);
             } finally {
                 if (comSocket != null && !comSocket.isClosed()) {
                     comSocket.close();
                 }
             }
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Error in RequestHandler", e);
         }
 
         logger.log(Level.INFO, Thread.currentThread().getName() + " ended.");
     }
 
     private void log(Logger logger, InetAddress client, Request request, Response response) {
        String date;
         try {
            date = DateConverter.formatLog(DateConverter.dateFromString(response.getHeader("Date")));
         } catch (DateConverterException e) {
            date = DateConverter.formatLog(new Date());
         }
 
         String logformat = "%s [%s] \"%s %s %s\" %s %s";
         logger.log(Level.INFO, String.format(logformat, client.getHostAddress(), date,
                 request.getMethod(), request.getRequestURI().toString(),
                 request.getProtocol(), response.getStatus().getCode(), response
                 .getHeader("Content-Length")));
     }
 }
