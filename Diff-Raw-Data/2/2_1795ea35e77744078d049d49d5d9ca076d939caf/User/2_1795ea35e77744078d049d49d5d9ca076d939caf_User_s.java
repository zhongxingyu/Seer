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
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 
 
 import org.apache.commons.lang.BooleanUtils;
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 import server.ResMessages;
 import server.ServerException;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 import controllers.WidgetAdmin;
 
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
 	@XStreamAsAttribute
 	private String email;
 	
 	@Required
 	@XStreamAsAttribute
 	private String password;
 	
 	private String authToken; 
 	
 	private String expires;
 	
 	@XStreamOmitField
 	private Boolean admin;
 	
 	@OneToMany(cascade=CascadeType.ALL) 
 	private List<Widget> widgets;
 	
 	public static Finder<Long,User> find = new Finder<Long,User>(Long.class, User.class); 
 
 	public User(String email, String password)
 	{
 		this(null, null, email, password);
 	}
 	
 	public User(String firstName, String lastName, String email, String password)
 	{
 		this.firstName = firstName;
 		this.lastName = lastName;
 		this.email = email;
 		this.password = password;
 		this.admin = false;
 	}
 	
 	public Widget createNewWidget( String productName, String productVersion, String title,
 								   String youtubeVideoUrl, String providerURL,
 								   String recipeURL, String consolename, String consoleURL )
 	{
 		Widget widget = new Widget( productName, productVersion, title, youtubeVideoUrl, providerURL, recipeURL, consolename, consoleURL );
 		widget.setUserName(email);
 		
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
 			throw new ServerException(ResMessages.getFormattedString("user_already_exists", email));
 			
 		return user;
 	}
 	
 	
 	// TODO create a real authToken mechanism with expiration: currTime + 1 hour
 	static private void createAuthToken( User user )
 	{
 		user.setAuthToken(UUID.randomUUID().toString());
 		user.setExpires(new Date().toString());
 	}
 	
 	static public Session authenticate( String email, String password )
 	{
 		User user = find.where().eq("email", email).eq("password", password).findUnique();
 		
 		if ( user == null )
 			throw new ServerException( ResMessages.getString("invalid_username_password") );
 		
 		return user.getSession();
 	}
 
 	static public User validateAuthToken( String authToken )
 	{
 		return validateAuthToken( authToken, false );
 	}
 
     static public User validateAuthToken( String authToken, boolean silent )
     {
         User user = User.find.where().eq( "authToken", authToken ).findUnique();
         if ( user == null && !silent ) {
             throw new ServerException( ResMessages.getFormattedString( "auth_token_not_valid", authToken ) );
         }
 
         return user;
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
 
 	public void setPassword(String password)
 	{
 		this.password = password;
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
		return admin;
 	}
 
 	public void setAdmin(Boolean admin)
 	{
 		this.admin = admin;
 	}
 	
 	/*
 	@Override
 	public String toString()
 	{
 		return super();// Utils.reflectedToString(this);
 	}
 	*/
 }
