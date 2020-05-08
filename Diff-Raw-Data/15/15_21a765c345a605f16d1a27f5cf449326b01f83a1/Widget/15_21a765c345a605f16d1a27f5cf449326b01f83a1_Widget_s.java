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
 import java.util.List;
 import java.util.UUID;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 
 
 import play.db.ebean.Model;
 import server.ResMessages;
 import server.ServerException;
 import server.Utils;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 
 import controllers.WidgetAdmin;
 
 /**
  * This class represents a widget metadata and relates to a specific {@link User}.
  * See {@link WidgetAdmin#createNewWidget(String, String, String, String, String, String, String, String, String)}
  * 
  * @author Igor Goldenberg
  * @see WidgetAdmin
  */
 @Entity
 @XStreamAlias("widget")
 @SuppressWarnings("serial")
 public class Widget
 	extends Model
 {
 	@Id
 	@XStreamAsAttribute
	private String id;
 
 	@XStreamAsAttribute
 	private String userName;
 	
 	@XStreamAsAttribute
 	private String productName;
 
 	@XStreamAsAttribute
 	private String providerURL;
 
 	@XStreamAsAttribute
 	private String productVersion;
 	
 	@XStreamAsAttribute
 	private String title;
 	
 	@XStreamAsAttribute
 	private String youtubeVideoUrl;
 
 	@XStreamAsAttribute
 	private String recipeURL;
 	
 	@XStreamAsAttribute
 	private Boolean allowAnonymous;
 
 	@XStreamAsAttribute
 	private String apiKey;
 
 	@XStreamAsAttribute
 	private Integer launches;
 	
 	@XStreamAsAttribute
 	private Boolean enabled;
 	
 	@XStreamAsAttribute
 	private String consoleName;
 	
 	@XStreamAsAttribute
 	private String consoleURL;
 
 	@OneToMany(cascade=CascadeType.ALL) 
 	private List<WidgetInstance> instances;
 
 	public static Finder<Long,Widget> find = new Finder<Long,Widget>(Long.class, Widget.class); 
 
 	/** This class serves as status of the widget instance  */
 	@XStreamAlias("status")
 	final static public class Status
 	{
 		public final static String STATE_STOPPED = "stopped";
 		public final static String STATE_RUNNING = "running";
 		
 		private String state;
 		private List<String> output;
 		private Integer timeleft;
 		
 		public Status( String state, String...messages )
 		{
 			this.state = state;
 			output = new ArrayList<String>();
 			for( String msg : messages )
 				output.add( msg );
 		}
 		
 		public Status( String state, List<String> output, int timeleftMin )
 		{
 			this.state = state;
 			this.output = output;
 			
 			if ( output.isEmpty() )
 				output.add("Please wait, prepare enviroment...");
 				
 			this.timeleft = timeleftMin == 0 ? 1 : timeleftMin; // since we show in minutes, the latest minute show always 1 minute.
 		}
 	}
 	
 	public Widget( String productName, String productVersion, String title, String youtubeVideoUrl,
 					String providerURL, String recipeURL, String consoleName, String consoleURL )
 	{
 		this.productName = productName;
 		this.title = title;
 		this.productVersion = productVersion;
 		this.youtubeVideoUrl = youtubeVideoUrl;
 		this.providerURL = providerURL;
 		this.recipeURL  = recipeURL;
 		this.consoleName = consoleName;
 		this.consoleURL = consoleURL;
 		this.enabled = true;
 		this.launches = 0;
 		this.apiKey = UUID.randomUUID().toString();
 	}
 	
 	public WidgetInstance addWidgetInstance( String instanceId, String publicIP )
 	{
 		WidgetInstance wInstance = new WidgetInstance( instanceId, publicIP, consoleName, consoleURL );
 		
 		if (instances == null)
 			instances = new ArrayList<WidgetInstance>();
 
 		instances.add( wInstance );
 		
 		save();
 		
 		return wInstance;
 	}
 
 	/** @return the widget by apiKey or null  */
 	static public Widget getWidgetByApiKey( String apiKey )
 	{
 		Widget widget = Widget.find.where().eq("apiKey", apiKey).findUnique();
 
 		if ( widget == null )
 		    throw new ServerException(ResMessages.getFormattedString("widget_apikey_not_valid", apiKey));
 		
 		return widget;
 	}
 	
 	static public Widget regenerateApiKey( String oldApiKey )
 	{
 		Widget widget = getWidgetByApiKey( oldApiKey );
 		widget.apiKey = UUID.randomUUID().toString();
 		widget.save();
 		
 		widget.setInstances(null);
 		
 		return widget;
 	}
 	
	public String getId()
 	{
 		return id;
 	}
 
	public void setId(String id)
 	{
 		this.id = id;
 	}
 
 	public String getUserName()
 	{
 		return userName;
 	}
 
 	public void setUserName(String userName)
 	{
 		this.userName = userName;
 	}
 
 	
 	public String getApiKey()
 	{
 		return apiKey;
 	}
 
 	public void setApiKey(String apiKey)
 	{
 		this.apiKey = apiKey;
 	}
 
 	public List<WidgetInstance> getInstances()
 	{
 		return instances;
 	}
 
 	public void setInstances(List<WidgetInstance> instances)
 	{
 		this.instances = instances;
 	}
 
 	public int getLaunches()
 	{
 		return launches;
 	}
 
 	public void setLaunches(int launches)
 	{
 		this.launches = launches;
 	}
 
 	public String getRecipeURL()
 	{
 		return recipeURL;
 	}
 
 	public void setRecipeURL(String recipeURL)
 	{
 		this.recipeURL = recipeURL;
 	}
 
 	public String getProductName()
 	{
 		return productName;
 	}
 
 	public void setProductName(String productName)
 	{
 		this.productName = productName;
 	}
 
 	public String getProviderURL()
 	{
 		return providerURL;
 	}
 
 	public void setProviderURL(String providerURL)
 	{
 		this.providerURL = providerURL;
 	}
 
 	public String getProductVersion()
 	{
 		return productVersion;
 	}
 
 	public void setProductVersion(String productVersion)
 	{
 		this.productVersion = productVersion;
 	}
 
 	public String getTitle()
 	{
 		return title;
 	}
 
 	public void setTitle(String title)
 	{
 		this.title = title;
 	}
 
 	public String getYoutubeVideoUrl()
 	{
 		return youtubeVideoUrl;
 	}
 
 	public void setYoutubeVideoUrl(String youtubeVideoUrl)
 	{
 		this.youtubeVideoUrl = youtubeVideoUrl;
 	}
 
 	public Boolean getAllowAnonymous()
 	{
 		return allowAnonymous;
 	}
 
 	public void setAllowAnonymous(Boolean allowAnonymous)
 	{
 		this.allowAnonymous = allowAnonymous;
 	}
 
 	public void setLaunches(Integer launches)
 	{
 		this.launches = launches;
 	}
 	
 	public Boolean isEnabled()
 	{
 		return enabled;
 	}
 
 	public void setEnabled(Boolean enabled)
 	{
 		this.enabled = enabled;
 		save();
 	}
 	
 	public void countLaunch()
 	{
 		launches++;
 		save();
 	}
 	
 	@Override
 	public String toString()
 	{
 		return Utils.reflectedToString(this);
 	}
 }
