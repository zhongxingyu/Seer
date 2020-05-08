import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiError;
import com.wordnik.swagger.annotations.ApiOperation;

 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 
 /**
  * A simple resource that sends a greeting to
  * an XMPP user via the SayHelloBot
  */
@Path("/user")
@Api(value = "/user", description = "say things to users")
 public class SayHelloResource {
 
     private final SayHelloBot helloBot;
 
     public SayHelloResource(SayHelloBot helloBot) {
         this.helloBot = helloBot;
     }
 
     @POST
    @Path("/hello/{jid}/")
    @ApiOperation(value = "say hello")
     public void sayHello(@PathParam("jid") String toJid) {
         helloBot.sayHello(toJid);
     }
 }
