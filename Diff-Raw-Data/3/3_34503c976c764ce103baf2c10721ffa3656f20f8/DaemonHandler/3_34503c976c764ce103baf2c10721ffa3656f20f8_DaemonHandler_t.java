 package capstone.daemon;
 
 import capstone.wrapper.*;
 
 import java.util.HashMap;
 import java.net.URLDecoder;
 
 import io.netty.buffer.*;
 import io.netty.util.CharsetUtil;
 import io.netty.channel.*;
 import io.netty.handler.codec.http.*;
 
 import static io.netty.handler.codec.http.HttpHeaders.Names.*;
 import static io.netty.handler.codec.http.HttpHeaders.*;
 import static io.netty.handler.codec.http.HttpResponseStatus.*;
 import static io.netty.handler.codec.http.HttpVersion.*;
 
 public class DaemonHandler extends ChannelInboundMessageHandlerAdapter<DefaultFullHttpRequest>
 {
     /**
      * Format for the key is: "userId_debuggerId"
      */
     static HashMap<String,Wrapper> wrapperMap = new HashMap<String,Wrapper>();
     static HashMap<String,String> sessionKeyMap = new HashMap<String,String>();
 
     @Override
     public void endMessageReceived(ChannelHandlerContext ctx) throws Exception
     {
         ctx.flush();
     }
 
     @Override
     public void messageReceived(
                       ChannelHandlerContext ctx
                     , DefaultFullHttpRequest request)
     {
         System.out.println("[Daemon] Connection started");
 
         String body = request.data().toString(CharsetUtil.UTF_8);
         
         try 
         { 
             HashMap<String, String> args = parseBody(body);
 
             // TODO add session tokens
             // TODO add specifier of C++ vs Python
 
             String userId = args.get("usrid");
             String debuggerId = args.get("dbgid");
             String wrapperKey = userId + "_" + debuggerId;
 
             String commandString = args.get("call");
             DebuggerCommand command = DebuggerCommand.fromString(commandString);
 
             String data = args.get("data");
             if (data == null)
             {
                 data = "";
             }
 
             DebuggerRequest debuggerRequest = new DebuggerRequest(command, data);
 
             // TODO determine the cases when we want to create a new one
             Wrapper wrapper = wrapperMap.get(wrapperKey); // FIXME synchronize on the session key
             if (wrapper == null)
             {
                 // TODO switch off of language here
                 wrapper = new GdbWrapper(Integer.parseInt(userId), Integer.parseInt(debuggerId));
                 wrapper.start();
                 wrapperMap.put(wrapperKey, wrapper);  // FIXME data race here
             }
 
             System.out.println("[daemon] Submitting a request...");
             if (debuggerRequest.command == DebuggerCommand.GIVEINPUT)
             {
                 wrapper.provideInput(debuggerRequest.data);
                 debuggerRequest.result = "";
             }
             else
             {
                 System.out.println("[daemon] Waiting on the monitor...");
                 synchronized (debuggerRequest.monitor)
                 {
                    wrapper.submitRequest(debuggerRequest);
                     debuggerRequest.monitor.wait();
                 }
                 System.out.println("[daemon] Woke up!");
             }
             System.out.println("[daemon] Got result: " + debuggerRequest.result);
             
             FullHttpResponse response = new DefaultFullHttpResponse(
                 HTTP_1_1, OK, Unpooled.copiedBuffer(debuggerRequest.result , CharsetUtil.UTF_8));
 
             response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
             response.headers().set(CONTENT_LENGTH, debuggerRequest.result.length());
             response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
 
             ctx.nextOutboundMessageBuffer().add(response);
             ctx.flush().addListener(ChannelFutureListener.CLOSE);
 
         }
         catch(Exception e) 
         {
             System.out.println("[daemon] Got an exception. Printing the stack trace:");
             e.printStackTrace();
         }
     }
 
     private HashMap<String, String> parseBody(String body)
     throws Exception // FIXME change to right exception type
     {
         HashMap<String, String> args = new HashMap<String, String>();
         if(body.length() > 0)
         {
             System.out.println("[daemon] parsing: " + body);
             String[] tokens = body.split("&");
             for(String token : tokens)
             {
                 String[] kvp = token.split("=",2);
                 args.put(URLDecoder.decode(kvp[0], "UTF-8")
                         ,URLDecoder.decode(kvp[1], "UTF-8"));
             }
         }
         return args;
     }
 
     @Override
     public void exceptionCaught(
                       ChannelHandlerContext ctx
                     , Throwable cause)
     {
         cause.printStackTrace();
         ctx.close();
     }
 }
