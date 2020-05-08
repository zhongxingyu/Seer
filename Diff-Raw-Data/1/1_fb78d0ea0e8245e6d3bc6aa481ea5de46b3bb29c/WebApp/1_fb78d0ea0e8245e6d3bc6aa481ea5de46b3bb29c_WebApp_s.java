 package prj.httpApplication.app;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import prj.httpApplication.RawHTTPResponse;
 import prj.httpApplication.connection.HTTPSocket;
 import prj.httpApplication.connection.HTTPSocketListener;
 import prj.httpApplication.utils.ConcurrencyUtils;
 import prj.httpparser.httpparser.RawHTTPRequest;
 
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 public class WebApp
 {
     private static final int SOCKET_TIMEOUT_IN_SECONDS = 10;
     public Router _router;
     private Logger _logger = LoggerFactory.getLogger(WebApp.class);
 
     public WebApp(Router router)
     {
         _router = router;
     }
 
     public void registerHandler(String pattern, HTTPRequestHandler handler)
     {
         _router.addRouting(pattern, handler);
     }
 
     public void clientConnected(final HTTPSocket httpSocket)
     {
         final ScheduledFuture<?> timeoutFuture = setTimeoutForSocket(httpSocket);
         httpSocket.addListener(new HTTPSocketListener()
         {
             @Override
             public void onRequestArrived(final RawHTTPRequest request)
             {
                 timeoutFuture.cancel(true);
                 HTTPRequestHandler handler = _router.getHandler(request.getResourceAddress());
                 switch (request.getRequestType())
                 {
                     case GET:
                         httpSocket.send(handler.get(request).toString());
                         closeSocket(httpSocket);
                         break;
                     case POST:
                         httpSocket.send(handler.post(request).toString());
                         closeSocket(httpSocket);
                         break;
                 }
             }
 
             @Override
             public void onRequestError()
             {
                 httpSocket.send(new RawHTTPResponse("HTTP/1.1", 400, "Bad Request").toString());
             }
 
             @Override
             public void onSocketClosed()
             {
                 httpSocket.removeListener(this);
             }
         });
     }
 
     private ScheduledFuture<?> setTimeoutForSocket(final HTTPSocket httpSocket)
     {
         Runnable runnable = new Runnable()
         {
             @Override
             public void run()
             {
                 _logger.warn("Closing socket due to timeout {}", httpSocket);
                 closeSocket(httpSocket);
             }
         };
         ConcurrencyUtils concurrencyUtils = ConcurrencyUtils.getInstance();
         return concurrencyUtils.scheduleRunnable(runnable, SOCKET_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
     }
 
     private void closeSocket(HTTPSocket httpSocket)
     {
         httpSocket.close();
     }
 }
