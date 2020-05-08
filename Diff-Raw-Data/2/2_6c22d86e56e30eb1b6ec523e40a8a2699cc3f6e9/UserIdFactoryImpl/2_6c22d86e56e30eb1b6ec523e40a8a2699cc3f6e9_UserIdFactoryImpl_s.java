 package deus.gatekeeper.impl;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import deus.gatekeeper.UserIdFactory;
 import deus.model.user.id.UserId;
 import deus.storage.user.UserMetadataDao;
 
@Component
 public class UserIdFactoryImpl implements UserIdFactory {
 
 	@Autowired
 	private UserMetadataDao userMetadataDao;
 	
 	@Override
 	public UserId createUserId(String localUsername) {
 		return userMetadataDao.getUserId(localUsername);
 	}
 
 }
