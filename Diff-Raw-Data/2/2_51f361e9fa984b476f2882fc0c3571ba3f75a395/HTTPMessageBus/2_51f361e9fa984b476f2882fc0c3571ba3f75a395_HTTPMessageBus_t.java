 package juglr.net;
 
 import juglr.*;
 
 import java.io.*;
 import java.nio.channels.SocketChannel;
 import java.util.Iterator;
 
 /**
  * FIXME: This class is incomplete
  * TODO:
  *   * Expose all message bus functionality remotely:
  *     - Remote address allocations (named and unique)
  *     - Remote lookup()
  *     - Remote start()
  *   * Implement a HTTPMessageBusProxy which is a MessageBus that talks
  *     to a remote HTTPMessageBus
  *
  * @see TCPServerActor
  * @see MessageBus
  */
 public class HTTPMessageBus extends MessageBus {
 
     private Actor server;
 
     public HTTPMessageBus(int port) throws IOException {
         server = new TCPServerActor(port, new ConnectionStrategy(), this);
         server.start();
     }
 
     public HTTPMessageBus() throws IOException {
         this(getBusPort());
     }
 
     private static int getBusPort() {
         String busPort = System.getProperty("juglr.busport", "4567");
         try {
             return Integer.parseInt(busPort);
         } catch (NumberFormatException e) {
             throw new EnvironmentError(
                             "Unable to detemine message bus port from '"
                             + busPort + "': " + e.getMessage(), e);
         }
     }
 
     private static enum Handler {
         ACTOR,
         LIST,
         PING,
         UNKNOWN;
 
         /**
          * Optimized parsing of a byte array to a Handler value
          * @param buf the byte array to parse the handler value from
          * @param offset where to start reading from
          * @param len max number of bytes to read from {@param buf}
          * @return a {@link Handler} value
          */
         static Handler parse(byte[] buf, int offset, int len) {
             if (len  < 5) {
                 return Handler.UNKNOWN;
             }
 
             byte b0 = buf[offset], b1 = buf[offset + 1], b2 = buf[offset + 2],
                  b3 = buf[offset + 3], b4 = buf[offset + 4];
             switch (b0) {
                 case 'a':
                     if (b1 == 'c' && b2 == 't' && b3 == 'o' &&
                         b4 == 'r' && len >= 6 && buf[offset + 5] == '/') {
                         return Handler.ACTOR;
                     }
                     break;
                 case 'l':
                     if (b1 == 'i' && b2 == 's' && b3 == 't' && b4 == '/') {
                         return Handler.LIST;
                     }
                     break;
                 case 'p':
                     if (b1 == 'i' && b2 == 'n' && b3 == 'g' && b4 == '/') {
                         return Handler.PING;
                     }
                     break;
                 default:
                     return Handler.UNKNOWN;
             }
             return Handler.UNKNOWN;
         }
 
         static int length(Handler h) {
             switch (h) {
                 case ACTOR:
                     return 5;
                 case LIST:
                 case PING:
                     return 4;
                 default:
                     throw new RuntimeException(
                         "Internal error: Calculated length of unknown handler");
             }
         }
     }
 
     /**
      * This class is really just a factory for closures dispatching
      * HTTP messages to the recipient actor.
      */
     class ConnectionStrategy implements TCPChannelStrategy {
 
         public TCPChannelActor accept(SocketChannel channel) {
             return new TCPChannelActor(channel) {
                 // FIXME: Use ThreadLocals here to save memory
                 JSonBoxParser msgParser = new JSonBoxParser();
                 HTTPRequestReader req = new HTTPRequestReader(channel);
                 HTTPResponseWriter resp = new HTTPResponseWriter(channel);
 
                 /* The backend actor should send a SM back to us */
                 @Override
                 public void react(Message msg) {
                     if (!(msg instanceof Box)) {
                         throw new MessageFormatException(
                                                   "Expected Box");
                     }
 
                     Box box = (Box)msg;
                     HTTP.Status status;
                     if (box.getType() == Box.Type.MAP &&
                         box.has("__httpStatusCode__")) {
                         status = HTTP.Status.fromHttpOrdinal(
                                                            (int)box.getLong());
                          box.getMap().remove("__httpStatusCode__");
                     } else {
                         status = HTTP.Status.OK;
                     }
 
                     try {
                         respond(status, box);
                     } catch (IOException e) {
                         // FIXME
                         e.printStackTrace();
                     }
                 }
 
                 @Override
                 public void start() {
                     try {
                         // FIXME: Allocation should be thread local
                         byte[] buf = new byte[1024];
                         HTTP.Method method = req.readMethod();                        
 
                         int uriLength = req.readURI(buf);
                         Handler handler = Handler.parse(buf, 1, uriLength);
                         if (handler == Handler.UNKNOWN) {
                             respondError(HTTP.Status.NotFound,
                                          "No handler for path "
                                          + new String(buf, 0, uriLength));
                             return;
                         }
 
                         // The arguments are the rest of the
                         // uri string after the handler
                         int hlength = Handler.length(handler);
                         String args = new String(
                                    buf, hlength + 1, uriLength - hlength - 1);
                         HTTP.Version ver = req.readVersion();
 
                         while (req.readHeaderField(buf) > 0) {
                             // Skip HTTP headers
                         }
 
                         int bodyLength = req.readBody(buf);
                         InputStream bodyStream = new ByteArrayInputStream(
                                                             buf, 0, bodyLength);
                         Reader bodyReader = new InputStreamReader(bodyStream);
 
                         dispatch(method, handler, args, bodyReader);
                     } catch (IOException e) {
                         // Error writing response
                         // FIXME: Handle this gracefully
                         System.err.println("Error writing response");
                         e.printStackTrace();
                         freeAddress(getAddress());
                     }
                 }
 
                 /* Send a response msg to the client and shut down the actor */
                 private void respond(HTTP.Status status, Box msg)
                                                             throws IOException {
                     try{
                         String msgString = msg.toString();
 
                         resp.writeVersion(HTTP.Version.ONE_ZERO);
                         resp.writeStatus(status);
                         resp.writeHeader("Content-Length", "" + msgString.length());
                         resp.writeHeader("Server", "juglr");
                         resp.startBody();
                         resp.writeBody(msgString);
                     } finally {
                         resp.close();
                         getBus().freeAddress(getAddress());
                     }
                 }
 
                 /* Send an error to the client and shut down the actor */
                 private void respondError(HTTP.Status status, String msg)
                                                             throws IOException {
                     Box resp = Box.newMap();
                     respond(status, resp.put("error", msg));
                 }
 
                 private void dispatch(HTTP.Method method,
                             Handler handler, String args, Reader msgBody)
                                                             throws IOException {
                     // The first char is a '/' so skip that and find the next
                     int argSplit = args.indexOf('/', 1);
                     String arg0;
                     if (argSplit >= 0) {
                         arg0 = args.substring(0, argSplit);
                         args = args.substring(argSplit);
                     } else {
                         arg0 = args;
                         args = "";
                     }
 
                     Address recipient;
                     switch (handler) {
                         case ACTOR:
                             if (! checkMethodType(
                                     method, HTTP.Method.POST, Handler.ACTOR)) {
                                 return;
                             }
                             recipient = lookup(arg0);
                             if (recipient == null) {
                                 respondError(HTTP.Status.NotFound,
                                              "No such actor " + arg0);
                                 return;
                             }
 
                             Box msg;
                             try {
                                 msg = msgParser.parse(msgBody);
                             } catch (MessageFormatException e) {
                                 respondError(HTTP.Status.BadRequest,
                                   "Illegal JSON POST data. " + e.getMessage());
                                 return;
                             }
 
                             // Recipent should respond with a SM to sender
                             // FIXME: We need a timeout to avoid leaking SocketChannels
                             super.send(msg, recipient);
                             break;
                         case LIST:
                             if (! checkMethodType(
                                     method, HTTP.Method.GET, Handler.LIST)) {
                                 return;
                             }
                             Box ls = Box.newList();
                             Iterator<Address> iter = list();
                             while (iter.hasNext()) {
                                 ls.add(iter.next().externalize());
                             }
                             respond(HTTP.Status.OK, ls);
                             break;
                         case PING:
                             if (! checkMethodType(
                                     method, HTTP.Method.GET, Handler.PING)) {
                                 return;
                             }
                             recipient = lookup(arg0);
                             if (recipient == null) {
                                 respondError(HTTP.Status.NotFound,
                                              "No such actor " + arg0);
                                 return;
                             } else {
                                 respond(HTTP.Status.OK,
                                         new Box(
                                                 recipient + " says hi!"));
                             }
                             break;
                         case UNKNOWN:
                             respondError(HTTP.Status.NotFound,
                                          "Request to unknown handler " + args);
                             break;
                     }
                 }
 
                 private boolean checkMethodType(
                             HTTP.Method method, HTTP.Method expected, Handler h)
                                                             throws IOException {
                     if (method != expected) {
                         respondError(HTTP.Status.MethodNotAllowed,
                                 String.format(
                                  "Only %s allowed on %s, got %s",
                                  expected, h.toString().toLowerCase(), method));
                         return false;
                     }
                     return true;
                 }
             };
         }
     }
 
 }
