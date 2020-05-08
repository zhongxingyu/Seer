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
 
 import javax.persistence.*;
 
 import beans.Recipe;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonProperty;
 import play.db.ebean.Model;
import utils.Utils;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 import controllers.Application;
 
 /**
  * This class represents a widget instance with a deployment metadata and instantiated on {@link Application#start(String, String, String)} }
  * The metadata contains on which server the widget instance has been deployed.
  * 
  * @author Igor Goldenberg
  * @see Application
  */
 @Entity
 @XStreamAlias("instance")
 public class WidgetInstance
 	extends Model
 {
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@XStreamOmitField
 	private Long id;
 
     @JsonIgnore
     @OneToOne( mappedBy = "widgetInstance" )
     private ServerNode serverNode;
 
 
     @Enumerated(EnumType.STRING)
     private Recipe.Type recipeType;
 
     @JsonIgnore
     @ManyToOne
     private Widget widget;
 
 	
 	public static Finder<Long,WidgetInstance> find = new Finder<Long,WidgetInstance>(Long.class, WidgetInstance.class); 
 
 	private static final String HOST_TOKEN = "$HOST";
 
     final static public class ConsoleLink
 	{
         @JsonProperty
 		public String title;
 
         @JsonProperty
 		public String url;
 
 
         public ConsoleLink() {
         }
 
         public ConsoleLink setTitle(String title) {
             this.title = title;
             return this;
         }
 
         public ConsoleLink setUrl(String url) {
             this.url = url;
             return this;
         }
     }
 
 	
 	public WidgetInstance( ) {	}
 	
     public ConsoleLink getLink() {
         if ( serverNode != null && widget != null ){
             String consoleName = widget.getConsoleName();
             String consoleURL = widget.getConsoleURL();
             String publicIP = serverNode.getPublicIP();
             return new ConsoleLink( ).setTitle(consoleName).setUrl(consoleURL.replace(HOST_TOKEN,publicIP));
         }
         return null;
     }
 
     public static WidgetInstance findByServerNode(ServerNode server) {
         return find.where().eq("serverNode", server).findUnique();
     }
 
     public static WidgetInstance findByInstanceId(String instanceId) {
         return find.where().eq("instanceId", instanceId).findUnique();
     }
 
     public Widget getWidget() {
         return widget;
     }
 
     @JsonProperty("publicIP")
     @Transient
     public String getPublicIp(){
         return serverNode != null ? serverNode.getPublicIP() : null;
     }
 
     // guy - this should hide the bug that instances are not connected to server nodes.
     // we should reveal this internally and not concern the users.
     @Transient
     public boolean isCorrupted(){
         return serverNode == null;
     }
 
     @JsonProperty("instanceId")
     @Transient
     public String getInstanceId(){
         return serverNode != null ? serverNode.getNodeId() : null;
     }
 
     public void setServerNode(ServerNode serverNode) {
         this.serverNode = serverNode;
     }
 
     public void setWidget(Widget widget) {
         this.widget = widget;
     }
 
     public ServerNode getServerNode() {
         return serverNode;
     }
 
     public Recipe.Type getRecipeType() {
         return recipeType;
     }
 
     public void setRecipeType(Recipe.Type recipeType) {
         this.recipeType = recipeType;
     }
 
     @Override
	public String toString()
	{
		return Utils.reflectedToString(this);
	}
 }
