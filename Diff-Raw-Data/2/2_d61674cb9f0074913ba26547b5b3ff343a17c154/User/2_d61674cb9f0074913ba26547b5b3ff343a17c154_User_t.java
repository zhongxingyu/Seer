 /*******************************************************************************
  * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package models;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 import controllers.WidgetAdmin;
 import org.apache.commons.lang.BooleanUtils;
 import org.apache.commons.lang3.StringUtils;
 import play.cache.Cache;
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 import play.i18n.Messages;
 import play.mvc.Http;
 import server.ApplicationContext;
 import server.exceptions.ServerException;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 /**
  * This class creates on sign-up, serves for authentication and keeps information about login and user's widgets.
  * 
  * @author Igor Goldenberg
  * @see WidgetAdmin
  */
 @Entity
 @XStreamAlias("user")
 public class User 
 	extends Model
 {
 	private static final long serialVersionUID = 1L;
 
     /** instantiated on user's sign-in/sign-out */
 	@XStreamAlias("session")
 	final public static class Session
 	{
 		@XStreamAsAttribute
 		private String authToken;
 		
 		@XStreamAsAttribute
 		private String expires;
 
 		@XStreamAsAttribute
 		private Boolean admin;
 		
 		private Session( String authToken, String expires, Boolean admin )
 		{
 			this.authToken = authToken;
 			this.expires = expires;
 			this.admin = admin;
 		}
 		
 		public String getAuthToken()
 		{
 			return authToken;
 		}
 
 		public String getExpires()
 		{
 			return expires;
 		}
 		
 		public boolean isAdmin()
 		{
 			return BooleanUtils.isTrue( admin );
 		}
 	}
 	
 	@Id
 	@XStreamOmitField
 	private Long id;
 	
 	@XStreamOmitField
 	private String firstName;
 	
 	@XStreamOmitField
 	private String lastName;
 
 	@Required
     @Column( unique = true )
 	@XStreamAsAttribute
 	private String email;
 	
 	@Required
 	@XStreamOmitField
 	private String password;
 	
 	private String authToken; 
 	
 	private String expires;
 	
 	@XStreamOmitField
 	private Boolean admin;
 
 	@OneToMany(cascade= CascadeType.ALL, mappedBy = "user")
 	private List<Widget> widgets;
 
     public static enum Role{
         ADMIN, USER
     }
 	
 	public static Finder<Long,User> find = new Finder<Long,User>(Long.class, User.class);
 
 
 
 	public User(String email, String password)
 	{
 		this(null, null, email, password);
 	}
 
     public String getFullName(){
         StringBuilder b = new StringBuilder(  );
         boolean hasFirstName = false;
         if ( !StringUtils.isEmpty(firstName) ){
             b.append( firstName );
             hasFirstName = true;
         }
 
         if ( !StringUtils.isEmpty( lastName )){
             if ( hasFirstName ){
                 b.append( " " );
             }
             b.append( lastName );
         }
 
         return b.length() == 0 ? email : b.toString();
     }
 	
 	public User(String firstName, String lastName, String email, String password)
 	{
 		this.firstName = firstName;
 		this.lastName = lastName;
 		this.email = email;
		encryptAndSetPassword( password );
 		this.admin = false;
 	}
 	
 	public Widget createNewWidget( String productName, String productVersion, String title,
 								   String youtubeVideoUrl, String providerURL,
 								   String recipeURL, String consolename, String consoleURL, String recipeRootPath )
 	{
 		Widget widget = new Widget( productName, productVersion, title, youtubeVideoUrl, providerURL, recipeURL, consolename, consoleURL, recipeRootPath );
 
         // guy - removing "setUsername" - it is unclear what that was..
         // if we want Widget to refer to a user, we should use a foreign key..
 
 
 		if (widgets == null)
 			widgets = new ArrayList<Widget>();
 
 		widgets.add( widget );
 		
 		save();
 		
 		return widget;
 	}
 
 	
 	/**
 	 * Create new account.
 	 * 
 	 * @return An authenticator key or error if user already exists.
 	 */
 	static public User newUser( String firstName, String lastName, String email, String password )
 	{
 		User user = find.where().ilike("email", email).findUnique();
 		
 		if ( user == null )
 		{
 			user = new User( firstName, lastName, email, password );
 			
 			// TODO real expiration
 			createAuthToken(user);
 
 			user.save();
 		}
 		else
 			throw new ServerException( Messages.get( "user.already.exists", email ));
 			
 		return user;
 	}
 	
 	
 	// TODO create a real authToken mechanism with expiration: currTime + 1 hour
 	static private void createAuthToken( User user )
 	{
 		user.renewAuthToken();
 		user.setExpires(new Date().toString());
 	}
 
     private void renewAuthToken(){
 
         setAuthToken( UUID.randomUUID().toString() );
     }
 
     public boolean comparePassword( String password ){
 
         if ( StringUtils.equals( password, this.password )){ // backward compatibility..
             return true;
         }
         return ApplicationContext.get().getHmac().compare( this.password, password );
     }
 
     public void encryptAndSetPassword( String unencryptedPassword ){
         password = ApplicationContext.get().getHmac().sign( unencryptedPassword );
     }
 
 
 
     public static User findById( Long pi )
     {
         return User.find.where().eq( "id", pi ).findUnique();
     }
 
     static public Session authenticate( String email, String password )
 	{
 		User user = find.where().eq("email", email).findUnique();
 
 		if ( user == null )
         {
             String msg = Messages.get( "invalid.username.password" );
             throw new ServerException( msg ).getResponseDetails().setError( msg ).done();
         }
 
         if ( StringUtils.equals( user.getPassword(), password ) ){ // we should encrypt
             user.encryptAndSetPassword( password );
             user.save(  );
         }
         else if ( !user.comparePassword( password )){
             String msg = Messages.get( "invalid.username.password" );
             throw new ServerException( msg ).getResponseDetails().setError( msg ).done();
         }
 
         Session session = user.getSession();
 
         if ( ApplicationContext.get().conf().settings.expireSession ){
             user.renewAuthToken();
             user.save();
         }
 
         prolongSession( session.authToken, user.getId() );
         return session;
 	}
 
 	static public User validateAuthToken( String authToken )
 	{
 		return validateAuthToken( authToken, false );
 	}
 
     // guy - temporarily, the "session" is artificially made with expiring the authtoken.
     // however, this should not be the case.. We need to create another controller to serve our GUI
     // which will be separated from the REST API - like all REST clients..
     // unlike all the rest APIs we have the ability yo skip the network overhead, and communicate directly with the REST API.
     // so we can simply invoke methods. However, the "session" should be handled by the GUI controller and not Javascript.
     static public User validateAuthToken( String authToken, boolean silent )
     {
         Long userId = null;
         if ( ApplicationContext.get().conf().settings.expireSession ) {
             userId = ( Long ) Cache.get( authToken ); // for now, lets use the cache as session. we should implement an encrypted cookie.
         } else {
             User u = User.find.where().eq( "authToken", authToken ).findUnique();
             if ( u != null ) {
                 userId = u.id;
             }
         }
 
         if ( userId == null ){
             Http.Context.current().response().setHeader( "session-expired", "session-expired" );
             throw new ServerException( Messages.get("session.expired" ) ).getResponseDetails().setHeaderKey( "session-expired" ).setError( "Session Expired" ).done();
         }
 
 
         User user = User.find.byId( userId );
         if ( user == null && !silent ) {
             throw new ServerException( Messages.get( "auth.token.not.valid", authToken ) );
         }
 
         Http.Context.current().session().put("authToken", authToken );
         prolongSession( authToken, user.id );
 
         return user;
     }
 
     // makes the session longer. expiration is only if user is idle.
     private static void prolongSession( String authToken, Long userId )
     {
         Cache.set( authToken, userId, ( int ) (ApplicationContext.get().conf().server.sessionTimeoutMillis / 1000) ); // cache for one hour
     }
 
 	static public List<User> getAllUsers()
 	{
 		return find.all();
 	}
 	
 	public String getEmail()
 	{
 		return email;
 	}
 
 	public void setEmail(String email)
 	{
 		this.email = email;
 	}
 
 	public String getPassword()
 	{
 		return password;
 	}
 
 	static public List<User> all()
 	{
 		return find.all();
 	}
 
 	public Long getId()
 	{
 		return id;
 	}
 
 	public void setId(Long id)
 	{
 		this.id = id;
 	}
 
 	public List<Widget> getWidgets()
 	{
 		return widgets;
 	}
 
 	public void setWidgets(List<Widget> widgets)
 	{
 		this.widgets = widgets;
 	}
 	
 	public Session getSession()
 	{
 		return new Session(authToken, expires, admin);
 	}
 
 	public String getAuthToken()
 	{
 		return authToken;
 	}
 
 	public void setAuthToken(String authToken)
 	{
 		this.authToken = authToken;
 	}
 
 	public String getExpires()
 	{
 		return expires;
 	}
 
 	public void setExpires(String expires)
 	{
 		this.expires = expires;
 	}
 
 	public boolean isAdmin()
 	{
 		return BooleanUtils.isTrue( admin );
 	}
 
 	public void setAdmin(Boolean admin)
 	{
 		this.admin = admin;
 	}
 
     public String toDebugString()
     {
         return email + " (" + id + ")";
     }
 
 	/*
 	@Override
 	public String toString()
 	{
 		return super();// Utils.reflectedToString(this);
 	}
 	*/
 }
