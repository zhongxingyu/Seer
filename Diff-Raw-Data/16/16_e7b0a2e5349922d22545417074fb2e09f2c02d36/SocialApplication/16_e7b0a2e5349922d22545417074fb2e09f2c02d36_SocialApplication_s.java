 package domain;
 
 import models.UserEntity;
 import play.Logger;
 import play.modules.facebook.FbGraph;
 import play.mvc.Scope.Session;
 
 import com.restfb.FacebookClient;
 import com.restfb.types.User;
 
 public enum SocialApplication
 {
 	FACEBOOK {
 		@Override
 		public boolean loggedIn(Session currentSession)
 		{
 			return currentSession.contains( sessionIdKey() );
 		}			
 		
 		@Override
 		public boolean login(Session currentSession)
 		{
 			try {
 				FacebookClient fbClient = FbGraph.getFacebookClient();
 				User p = fbClient.fetchObject("me", com.restfb.types.User.class);
 				
 				if( p != null ){
 					currentSession.put(sessionIdKey(), p.getId());
 					
					UserEntity ue = UserEntity.valueOf(p.getId(), p.getFirstName(), p.getLastName(), "http://grytsenko.com.ua/images/news/5541.jpg");
					
					ue.save();
 					
 				}
 			} catch (Exception e) {
 				
 				Logger.warn(e, "Current user is not logged in %s", this.name());
 				
 				return false;
 			}
 			
 			return true;
 		}
 
 		@Override
 		public String sessionIdKey()
 		{
 			return "facebook-user-id";
 		}
 	};
 
 	public abstract boolean loggedIn(Session currentSession);
 	
 	public abstract boolean login(Session currentSession);
 
 	public abstract String sessionIdKey();
 }
