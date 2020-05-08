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
 
 import com.avaje.ebean.ExpressionList;
 import com.avaje.ebean.Junction;
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 import org.jclouds.openstack.nova.v2_0.domain.Address;
 import org.jclouds.openstack.nova.v2_0.domain.Server;
 import play.db.ebean.Model;
 import utils.Utils;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.OneToOne;
 import java.util.Collection;
 import java.util.LinkedList;
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
 
 	@XStreamAsAttribute
 	private boolean stopped = false;
 	
 	@XStreamAsAttribute
 	private boolean remote = false;
 
     @OneToOne( cascade = CascadeType.REMOVE )
     WidgetInstance widgetInstance = null;
 	
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
 
 	public String getNodeId() // guy - it is dangerous to call this getId as it looks like the getter of "long id"
 	{
 		return serverId;
 	}
 
 	public String getPrivateIP()
 	{
 		return privateIP;
 	}
 
     public Long getId() {
         return id;
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
 
     static public List<ServerNode> findByCriteria( QueryConf conf) {
         ExpressionList<ServerNode> where = find.where();
         Junction<ServerNode> disjunction = where.disjunction();
 
         for (Criteria criteria : conf.criterias) {
             ExpressionList<ServerNode> conjuction = disjunction.conjunction();
             if (criteria.busy != null) {
                 conjuction.eq("busy", criteria.busy);
             }
             if (criteria.remote != null) {
                 conjuction.eq("remote", criteria.remote);
             }
             if (criteria.stopped != null) {
                 conjuction.eq("stopped", criteria.stopped);
             }
 
             if ( criteria.serverIdIsNull != null ){
                 if ( criteria.serverIdIsNull ){
                     conjuction.isNull("serverId");
                 }else{
                     conjuction.isNotNull("serverId");
                 }
             }
         }
 
         if ( conf.maxRows > 0 ){
             where.setMaxRows( conf.maxRows );
         }
 
         return where.findList();
     }
 
 	static public ServerNode getServerNode( String serverId )
 	{
 		return ServerNode.find.where().eq("serverId", serverId).findUnique();
 	}
 
 	static public void deleteServer( String serverId )
 	{
 		ServerNode server = getServerNode( serverId );
 		if ( server != null ){
 			server.delete();
         }
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
 
 	public boolean isStopped() {
 		return stopped;
 	}
 
 	public void setStopped(final boolean stopped) {
 		this.stopped = stopped;
 	}
 
 	public boolean isRemote() {
 		return remote;
 	}
 
 	public void setRemote(boolean remote) {
 		this.remote = remote;
 	}
 
     public void setWidgetInstance(WidgetInstance widgetInstance) {
         this.widgetInstance = widgetInstance;
     }
 
 
     // guy - todo - formalize this for reuse.
     public static class QueryConf {
         public int maxRows;
         public List<Criteria> criterias = new LinkedList<Criteria>();
 
         public QueryConf setMaxRows(int maxRows) {
             this.maxRows = maxRows;
             return this;
         }
 
         public Criteria criteria(){
             Criteria c = new Criteria(this);
             criterias.add(c);
             return c;
         }
 
     }
     public static class Criteria{
         public Boolean remote = null;
         public Boolean stopped = null;
         public Boolean busy = null;
         public String nodeId = null;
         private QueryConf conf;
         private Boolean serverIdIsNull;
 
         public Criteria(QueryConf conf) {
             this.conf = conf;
         }
 
         public Criteria setRemote(Boolean remote) {
             this.remote = remote;
             return this;
         }
 
         public QueryConf done(){
             return conf;
         }
 
         public Criteria setStopped(Boolean stopped) {
             this.stopped = stopped;
             return this;
         }
 
         public Criteria setBusy(Boolean busy) {
             this.busy = busy;
             return this;
         }
 
         public Criteria setNodeId(String nodeId) {
             this.nodeId = nodeId;
             return this;
         }
 
 
         public Criteria setServerIdIsNull(boolean serverIdIsNull) {
             this.serverIdIsNull = serverIdIsNull;
             return this;
         }
     }
 }
