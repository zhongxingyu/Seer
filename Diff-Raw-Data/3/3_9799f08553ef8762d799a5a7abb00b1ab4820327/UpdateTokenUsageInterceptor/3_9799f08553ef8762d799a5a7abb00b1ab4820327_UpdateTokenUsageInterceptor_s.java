 package com.tvelykyy.afeeder.webservice.interceptor;
 
 import java.util.List;
 import java.util.TreeMap;
 
 import org.apache.cxf.interceptor.Fault;
 import org.apache.cxf.message.Message;
 import org.apache.cxf.phase.AbstractPhaseInterceptor;
 import org.apache.cxf.phase.Phase;
 import org.springframework.beans.factory.annotation.Autowired;
 
import com.tvelykyy.afeeder.domain.User;
 import com.tvelykyy.afeeder.service.UserService;
 import com.tvelykyy.afeeder.webservice.Const;
 
 public class UpdateTokenUsageInterceptor extends AbstractPhaseInterceptor<Message> {
 	@Autowired
 	UserService userService;
 	
 	public UpdateTokenUsageInterceptor() {
 		//Interceptor will be called after method invocation
         super(Phase.POST_INVOKE);
     }
 	@Override
 	public void handleMessage(Message message) throws Fault {
 		String httpRequestMethod = (String) message.get(Message.HTTP_REQUEST_METHOD);
 		
 		//requesting wsdl goes through GET, so there is no need to check authority here
 		if (httpRequestMethod.equals("POST")) {
 			List<Object> tokenList = ((TreeMap<String, List<Object>>)message.get(Message.PROTOCOL_HEADERS))
 				.get(Const.AUTH_TOKEN);
 			
			
 			long userId = new Long(tokenList.get(0).toString());
 			userService.updateTokenUsage(userId);
 		}
 	}
 }
