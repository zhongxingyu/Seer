 package dacus.core;
 
 import org.springframework.stereotype.Component;
 
import deus.model.common.user.id.UserId;
import deus.model.common.user.id.UserUrl;

 
 @Component
 public class UserUrlParser {
 
 	public UserId parseUserUrl(String userUrlString) {
 		return new UserUrl("alice", "http://www.dacus.org");
 	}
 	
 }
