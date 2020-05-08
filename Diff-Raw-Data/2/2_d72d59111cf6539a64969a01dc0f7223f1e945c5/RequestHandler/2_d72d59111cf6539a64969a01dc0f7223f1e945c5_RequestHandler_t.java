 package ch.rollis.emma;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import ch.rollis.emma.contenthandler.ContentHandler;
 import ch.rollis.emma.contenthandler.ContentHandlerFactory;
 import ch.rollis.emma.context.ServerContext;
 import ch.rollis.emma.context.ServerContextManager;
 import ch.rollis.emma.request.HttpProtocolException;
 import ch.rollis.emma.request.HttpProtocolParser;
 import ch.rollis.emma.request.Request;
 import ch.rollis.emma.response.Response;
 import ch.rollis.emma.response.ResponseFactory;
 import ch.rollis.emma.response.ResponseStatus;
 import ch.rollis.emma.util.DateConverter;
 import ch.rollis.emma.util.DateConverterException;
 
 
 /**
  * The request handler has the responsibility to handle a client request and
  * dispatch the request to an appropriate content handler.
  * <p>
  * The request handler reads in the client's request data, transforms this data
  * to request by using the HttpProtocolParser and then dispatches the request to
  * an appropriate content handler. Finally after the content handler returns the
  * response the request handler writes the response to the output stream and
  * therefore back to client.
  * 
  * @author mrolli
  */
 public class RequestHandler implements Runnable {
     /**
      * Communication socket this request originates from.
      */
     private final Socket comSocket;
 
     /**
      * Flag denotes if connection is SSL secured.
      */
     private final boolean sslSecured;
 
     /**
      * Manager to get ServerContexts for the given request from.
      */
     private final ServerContextManager scm;
 
     /**
      * Logger instance this handler shall log its messages to.
      */
     private final Logger logger;
 
     /**
      * Class constructor that generates a request handler that handles a HTTP
      * request initiated by a client.
      * 
      * @param socket
      *            The socket the connection has been established
      * @param sslFlag
      *            Flag that denotes if connection is SSL secured
      * @param loggerInstance
      *            Global logger to log exception to
      * @param contextManager
      *            SeverContextManager to get the server context of for the
      *            request
      */
     public RequestHandler(final Socket socket, final boolean sslFlag, final Logger loggerInstance,
             final ServerContextManager contextManager) {
         comSocket = socket;
         sslSecured = sslFlag;
         scm = contextManager;
         logger = loggerInstance;
     }
 
     @Override
     public void run() {
         logger.log(Level.INFO, Thread.currentThread().getName() + " started.");
 
         InetAddress client = comSocket.getInetAddress();
 
         try {
             InputStream input = comSocket.getInputStream();
             OutputStream output = comSocket.getOutputStream();
 
             while (!Thread.currentThread().isInterrupted()) {
                 HttpProtocolParser parser = new HttpProtocolParser(input);
                 try {
                     Request request;
                     // setup request timer to handle situations where the client
                     // does not send anything
                     Thread timer = new Thread(new RequestHandlerTimeout(comSocket, 15000, logger));
                     timer.start();
                     try {
                         request = parser.parse();
                     } catch (SocketException e) {
                         throw new RequestTimeoutException(e);
                     }
                     timer.interrupt();
 
                     request.setPort(comSocket.getLocalPort());
                     request.setIsSslSecured(sslSecured);
                     ServerContext context = scm.getContext(request);
                     ContentHandler handler = new ContentHandlerFactory().getHandler(request);
                     Response response = handler.process(request, context);
                     response.send(output);
                     context.log(Level.INFO, getLogMessage(client, request, response));
 
                     // break if not HTTP/1.1 and keep-alive is not set or if an
                     // error occurred
                     if (!request.getProtocol().equals("HTTP/1.1")
                             || response.getStatus().getCode() >= 400) {
                         break;
                     }
                 } catch (HttpProtocolException e) {
                     logger.log(Level.WARNING, "HTTP protocol violation", e);
                     Response response = new ResponseFactory()
                     .getResponse(ResponseStatus.BAD_REQUEST);
                     response.send(output);
                     break;
                 }
             }
         } catch (RequestTimeoutException e) {
            logger.log(Level.INFO, "Request timeout reached", e);
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Error in RequestHandler", e);
             // try to gracefully inform the client
             if (!comSocket.isOutputShutdown()) {
                 Response response = new ResponseFactory()
                 .getResponse(ResponseStatus.INTERNAL_SERVER_ERROR);
                 try {
                     response.send(comSocket.getOutputStream());
                 } catch (IOException ioe) {
                     // do nothing
                 }
             }
         } finally {
             if (comSocket != null && !comSocket.isClosed()) {
                 try {
                     comSocket.close();
                 } catch (IOException e) {
                     logger.log(Level.SEVERE, "Error while closing com socket");
                 }
             }
         }
 
         logger.log(Level.INFO, Thread.currentThread().getName() + " ended.");
     }
 
     /**
      * Returns a string representation of a request by a client and its response
      * that then can be i.e. logged.
      * 
      * @param client
      *            InetAddres representing the client of the request
      * @param request
      *            The request received
      * @param response
      *            The response to the request received
      * @return The string representation
      */
     private String getLogMessage(final InetAddress client, final Request request,
             final Response response) {
         String date = DateConverter.formatLog(new Date());
         String requestDate = response.getHeader("Date");
         try {
 
             if (requestDate != null) {
                 date = DateConverter.formatLog(DateConverter.dateFromString(requestDate));
             }
         } catch (DateConverterException e) {
             // do nothing
             logger.log(Level.WARNING, "Invalid date encountered: " + requestDate.toString());
         }
 
         String logformat = "%s [%s] \"%s %s %s\" %s %s";
         return String.format(logformat, client.getHostAddress(), date,
                 request.getMethod(), request.getRequestURI().toString(),
                 request.getProtocol(), response.getStatus().getCode(), response
                 .getHeader("Content-Length"));
     }
 }
 
