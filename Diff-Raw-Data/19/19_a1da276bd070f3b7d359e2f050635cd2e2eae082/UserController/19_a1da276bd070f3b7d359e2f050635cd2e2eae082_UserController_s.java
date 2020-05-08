 package cl.own.usi.gateway.netty.controller;
 
 import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static cl.own.usi.gateway.netty.ResponseHelper.writeResponseWithoutClosing;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CREATED;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.handler.codec.http.HttpMethod;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.util.CharsetUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import cl.own.usi.gateway.client.WorkerClient;
 import cl.own.usi.json.UserRequest;
 
 /**
  * Controller that create the {@link User}
  * 
  * @author bperroud
  * @author nicolas
  */
 @Component
 public class UserController extends AbstractController {
 
 	@Autowired
 	private WorkerClient workerClient;
 	
 	
 	private final ObjectMapper jsonObjectMapper = new ObjectMapper();
 	
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
 			throws Exception {
 		HttpRequest request = (HttpRequest) e.getMessage();
 		
 		if (request.getMethod() == HttpMethod.POST) {
 
 			final UserRequest userRequest = jsonObjectMapper.readValue(
 					request.getContent().toString(CharsetUtil.UTF_8),
 					UserRequest.class);
 
 			boolean inserted = workerClient.insertUser(
 					userRequest.getMail(), userRequest.getPassword(),
 					userRequest.getFirstname(),
 					userRequest.getLastname());
 
 			if (inserted) {
 				writeResponse(e, CREATED);
 			} else {
 				writeResponse(e, BAD_REQUEST);
 			}
 		} else {
 			writeResponse(e, NOT_IMPLEMENTED);
 		}
 		
 	}
 
 }
