 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 
 /**
  * A simple resource that sends a greeting to
  * an XMPP user via the SayHelloBot
  */
@Path("/user/{jid}/")
 public class SayHelloResource {
 
     private final SayHelloBot helloBot;
 
     public SayHelloResource(SayHelloBot helloBot) {
         this.helloBot = helloBot;
     }
 
     @POST
    @Path("hello")
     public void sayHello(@PathParam("jid") String toJid) {
         helloBot.sayHello(toJid);
     }
 }
