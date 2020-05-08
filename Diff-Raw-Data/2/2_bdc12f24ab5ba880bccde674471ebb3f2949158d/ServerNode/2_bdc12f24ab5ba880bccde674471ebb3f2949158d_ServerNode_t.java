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
 import org.jclouds.openstack.nova.v2_0.domain.Address;
 import org.jclouds.openstack.nova.v2_0.domain.Server;
 import play.db.ebean.Model;
 import utils.Utils;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
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
 	private Long id;
 
 	@XStreamAsAttribute
 	private String serverId;
 
 	@XStreamAsAttribute
 	private Long expirationTime;
 
 	@XStreamAsAttribute
 	private String publicIP;  // todo : change case to Ip
 
 	@XStreamAsAttribute
 	private String privateIP;  // todo : change case to Ip
 
 	@XStreamAsAttribute
	private Boolean busy = false;
 
 	@XStreamAsAttribute
 	private String privateKey;
 
 	@XStreamAsAttribute
 	private String userName;
 
 	@XStreamAsAttribute
 	private String apiKey;
 
 	public static Finder<Long,ServerNode> find = new Finder<Long,ServerNode>(Long.class, ServerNode.class); 
 
 	public ServerNode( ) {
 
 	} 
 
 	public ServerNode( Server srv )
 	{
 		this.serverId  = srv.getId();
         Collection<Address> aPrivate = srv.getAddresses().get("private");
         Address[] addresses = aPrivate.toArray(new Address[aPrivate.size()]);
         this.privateIP = addresses[0].getAddr();
 		this.publicIP  = addresses[1].getAddr();
 		this.expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis( 30 ); // default unless configured otherwise
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
 
 	public void setPublicIP(String publicIP) {
 		this.publicIP = publicIP;
 	}
 
 	public Long getExpirationTime()
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
         return Math.max(  expirationTime - System.currentTimeMillis(), 0 );
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
 
 	public String getPrivateKey() {
 		return privateKey;
 	}
 
 	public void setPrivateKey(final String privateKey) {
 		this.privateKey = privateKey;
 	}
 
 	public String getApiKey() {
 		return apiKey;
 	}
 
 	public void setApiKey(final String apiKey) {
 		this.apiKey = apiKey;
 	}
 
 	public String getUserName() {
 		return userName;
 	}
 
 	public void setUserName(final String userName) {
 		this.userName = userName;
 	}
 }
