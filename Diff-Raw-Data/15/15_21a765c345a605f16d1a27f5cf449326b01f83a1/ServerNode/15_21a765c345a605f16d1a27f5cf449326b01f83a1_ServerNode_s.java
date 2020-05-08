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
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 
 import org.jclouds.openstack.nova.v2_0.domain.Address;
 import org.jclouds.openstack.nova.v2_0.domain.Server;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 import play.db.ebean.Model;
 import server.Utils;
 
 /**
  * The ServerNode keeps all metadata of all created and available/busy servers.
  * 
  * @author Igor Goldenberg
  * @see beans.ServerBootstrapperImpl
  */
 @Entity
 @SuppressWarnings("serial")
 @XStreamAlias("server")
 public class ServerNode
 	extends Model
 {
 	@Id
 	@XStreamOmitField
	private String id;
 	
 	@XStreamAsAttribute
 	private String serverId;
 	
 	@XStreamAsAttribute
 	private Long expirationTime;
 	
 	@XStreamAsAttribute
 	private String publicIP;
 	
 	@XStreamAsAttribute
 	private String privateIP;
 	
 	@XStreamAsAttribute
 	private Boolean busy;
 	
 	public static Finder<Long,ServerNode> find = new Finder<Long,ServerNode>(Long.class, ServerNode.class); 
 
 	
 	public ServerNode( Server srv )
 	{
 		this.serverId  = srv.getId();
 		this.privateIP = srv.getAddresses().get("private").toArray(new Address[0])[0].getAddr();
 		this.publicIP  = srv.getAddresses().get("private").toArray(new Address[0])[1].getAddr();
 		this.expirationTime = Long.MAX_VALUE;
 		busy = false;
 	}
 
 	public String getId()
 	{
 		return serverId;
 	}
 	
 	public String getPrivateIP()
 	{
 		return privateIP;
 	}
 
 	public String getPublicIP()
 	{
 		return publicIP;
 	}
 	
 	/** return <code>true</code> if this server has an expiration time to destroy */
 	public boolean isTimeLimited()
 	{
 		return expirationTime != Long.MAX_VALUE;
 	}
 	
 	public long getExpirationTime()
 	{
 		return expirationTime;
 	}
 	
 	public void setExpirationTime(Long expirationTime)
 	{
 		this.expirationTime = expirationTime;
 		save();
 	}
 	
 	public long getElapsedTime()
 	{
 		// server never expires
 		if ( !isTimeLimited() )
 			return Long.MAX_VALUE;
 		
 		long elapsedTime = expirationTime - System.currentTimeMillis();
 		if ( elapsedTime <=0 )
 			return 0;
 		else
 			return elapsedTime;
 	}
 
 	public boolean isExpired()
 	{
 	    return getElapsedTime() < 0;
 	}
 	
 	public boolean isBusy()
 	{
 		return busy;
 	}
 	
 	public void setBusy( boolean isBusy )
 	{
 		this.busy = isBusy;
 		save();
 	}
 	
 	static public int count()
 	{
 		return find.findRowCount();
 	}
 	
 	static public List<ServerNode> all()
 	{
 		return find.all();
 	}
 
 	static public ServerNode getFreeServer()
 	{
 		return ServerNode.find.where().eq("busy", "false").setMaxRows(1).findUnique();
 	}
 	
 	static public ServerNode getServerNode( String serverId )
 	{
 		return ServerNode.find.where().eq("serverId", serverId).findUnique();
 	}
 	
 	static public void deleteServer( String serverId )
 	{
 		ServerNode server = find.where().eq("serverId", serverId).findUnique();
 		if ( server != null )
 			server.delete();
 	}
 
     public String toDebugString() {
         return String.format("ServerNode{id='%s\', serverId='%s\', expirationTime=%d, publicIP='%s\', privateIP='%s\', busy=%s}", id, serverId, expirationTime, publicIP, privateIP, busy);
     }
    @Override
 	public String toString()
 	{
 		return Utils.reflectedToString(this);
 	}
 }
