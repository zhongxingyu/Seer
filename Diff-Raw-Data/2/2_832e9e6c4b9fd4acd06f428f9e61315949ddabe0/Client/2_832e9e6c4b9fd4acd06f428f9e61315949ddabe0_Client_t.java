 /**
  * Copyright 2010 The University of Nottingham
  * 
  * This file is part of GenericAndroidClient.
  *
  *  GenericAndroidClient is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  GenericAndroidClient is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with GenericAndroidClient.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package uk.ac.horizon.ug.exploding.client;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 
 import uk.ac.horizon.ug.exploding.client.model.Member;
 import uk.ac.horizon.ug.exploding.client.model.ModelUtils;
 import uk.ac.horizon.ug.exploding.client.model.Player;
 import uk.ac.horizon.ug.exploding.client.model.Position;
 import uk.ac.horizon.ug.exploding.client.model.Zone;
 
 //import org.json.JSONArray;
 //import org.json.JSONException;
 //import org.json.JSONObject;
 
 //import uk.ac.horizon.ug.bluetoothex.testclient.Main;
 //import uk.ac.horizon.ug.exserver.clientapi.JsonUtils;
 //import uk.ac.horizon.ug.exserver.clientapi.protocol.Message;
 //import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageStatusType;
 //import uk.ac.horizon.ug.exserver.clientapi.protocol.MessageType;
 
 import android.util.Log;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 
 /** Client stub
  * 
  * @author cmg
  *
  */
 public class Client {
 	private static final String TAG = "Client";
 	//static Logger logger = Logger.getLogger(Client.class.getName());
 	protected URI conversationUrl;
 	protected String clientId;
 	protected HttpClient httpClient;
 	/**
 	 * @param conversationUrl
 	 * @param clientId
 	 */
 	public Client(HttpClient httpClient, URI conversationUrl, String clientId) {
 		super();
 		this.httpClient = httpClient;
 		this.conversationUrl = conversationUrl;
 		this.clientId = clientId;
 	}
 	/**
 	 * @param conversationUrl
 	 * @param clientId
 	 * @throws MalformedURLException 
 	 * @throws URISyntaxException 
 	 */
 	public Client(HttpClient httpClient, String conversationUrl, String clientId) throws MalformedURLException, URISyntaxException {
 		super();
 		this.httpClient = httpClient;
 		this.conversationUrl = new URI(conversationUrl);
 		this.clientId = clientId;
 	}
 	
 	public URI getConversationUrl() {
 		return conversationUrl;
 	}
 	public String getClientId() {
 		return clientId;
 	}
 
 	protected int ackSeq = 0;
 	protected int seqNo = 1;
 	
 	/** connect */
 //	public boolean connect(/*List<String> classNames*/) throws /*JSONException,*/ IOException {
 //		
 //		List<Message> messages = new LinkedList<Message>();
 //		// we are a content display device!
 //		for (String className : classNames) {
 //			int ix = className.lastIndexOf('.');
 //			String namespace = ix>=0 ? className.substring(0, ix) : null;
 //			String typeName = className.substring(ix+1);
 //			JSONObject val = new JSONObject();
 //			val.put("typeName", typeName);
 //			if(namespace!=null)
 //				val.put("namespace", namespace);
 //			val.put("id", clientId);
 //			
 //			messages.add(addFactMessage(val.toString()));
 //		}
 //		sendMessages(messages);
 //		return true;
 //	}	
 	/** add fact message */
 	public Message addFactMessage(Object object) {
 		Message msg = new Message();
 		msg.setType(MessageType.ADD_FACT.name());
 		msg.setSeqNo(seqNo++);
 		msg.setNewVal(object);
 		return msg;
 	}
 	/** add fact message */
 	public Message updateFactMessage(Object oldVal, Object newVal) {
 		Message msg = new Message();
 		msg.setType(MessageType.UPD_FACT.name());
 		msg.setSeqNo(seqNo++);
 		msg.setOldVal(oldVal);
 		msg.setNewVal(newVal);
 		return msg;
 	}
 	/** internal async send */
 	public List<Message> sendMessage(Message msg) throws IOException {
 		List<Message> messages = new LinkedList<Message>();
 		messages.add(msg);
 		return sendMessages(messages);
 	}
 	/** internal async send */
	public synchronized List<Message> sendMessages(List<Message> messages) throws IOException {
 		HttpPost request  = new HttpPost(conversationUrl);
 		Log.i(TAG,"SendMessages to "+request.getURI()+", requestline="+request.getRequestLine());
 		request.setHeader("Content-Type", "application/xml");
 		//HttpURLConnection conn = (HttpURLConnection) conversationUrl.openConnection();
 		XStream xs = new XStream(/*new DomDriver()*/);
 		xs.alias("list", LinkedList.class);    	
 		xs.alias("message", Message.class);
 
 		// game specific
 		ModelUtils.addAliases(xs);
 		
 		String xml = xs.toXML(messages);
 		Log.d(TAG, "Sent: "+xml);
 		request.setEntity(new StringEntity(xml));
 		HttpResponse reply = httpClient.execute(request);
 
 		StatusLine statusLine = reply.getStatusLine();
 		Log.d(TAG, "Http status on login: "+statusLine);
 		int status = statusLine.getStatusCode();
 		if (status!=200) {
 			if (reply.getEntity()!=null)
 				reply.getEntity().consumeContent();
 			throw new IOException("Error response ("+status+") from server: "+statusLine.getReasonPhrase());
 		}
 		BufferedInputStream in = new BufferedInputStream(reply.getEntity().getContent());
 		Log.d(TAG,"Reading HTTP response -> XML");
 		messages = (List<Message>)xs.fromXML(in);
 		reply.getEntity().consumeContent();
 
 		Log.i(TAG, "Response "+messages.size()+" messages: "+messages);
 
 		// check status(es)
 		for (Message response : messages) {
 			if (response.getType().equals(MessageType.ERROR.name())) {
 				throw new IOException("Error response "+response.getStatus()+": "+response.getErrorMsg()+" for request "+response.getAckSeq());
 			}
 		}
 
 		return messages;
 	}
 	/** class name -> id -> fact */
 	protected HashMap<String,HashMap<Object,Object>> facts = new HashMap<String,HashMap<Object,Object>> ();
 	/** lock facts before using is a good idea */
 	public HashMap<String,HashMap<Object,Object>> getFacts() {
 		return facts;
 	}
 	public List<Object> getFacts(String typeName) {
 		
 		synchronized (facts) {
 			LinkedList<Object> fs = new LinkedList<Object>();
 			HashMap<Object,Object> typeFacts = facts.get(typeName);
 			if (typeFacts!=null) {
 				fs.addAll(typeFacts.values());
 			}
 			return fs;
 		}
 	}
 	/** poll 
 	 * @throws JSONException */
 	public List<Message> poll() throws IOException {
 		Message msg = new Message();
 		msg.setSeqNo(seqNo++);
 		msg.setType(MessageType.POLL.name());
 		//msg.setToFollow(0);
 		msg.setAckSeq(ackSeq);
 		
 		List<Message> messages = sendMessage(msg);
 		if (messages==null)
 			return messages;
 		Set<String> changedTypes = new HashSet<String>();
 		synchronized (facts) {
 			for (Message message : messages) {
 				if (message.getSeqNo()>0 && message.getSeqNo()>ackSeq)
 					ackSeq = message.getSeqNo();
 				MessageType messageType = MessageType.valueOf(message.getType());
 				if (messageType==MessageType.FACT_EX || messageType==MessageType.FACT_ADD) {
 					Object val = message.getNewVal();
 					String typeName = val.getClass().getName();
 					HashMap<Object,Object> typeFacts = facts.get(typeName);
 					if (typeFacts==null) {
 						typeFacts = new HashMap<Object,Object>();
 						facts.put(typeName,typeFacts);
 					}
 					typeFacts.put(getFactID(val), val);
 					Log.d(TAG,"Add fact "+val);
 					if (!changedTypes.contains(typeName))
 						changedTypes.add(typeName);
 				} else if (messageType==MessageType.FACT_UPD || messageType==MessageType.FACT_DEL) {
 					Object val = message.getOldVal();
 					String typeName = val.getClass().getName();
 					HashMap<Object,Object> typeFacts = facts.get(typeName);
 					if (typeFacts==null) {
 						typeFacts = new HashMap<Object,Object>();
 						facts.put(typeName,typeFacts);
 					}
 					Object key = getFactID(val);
 					boolean found = typeFacts.containsKey(key);
 					if (found) {
 						typeFacts.remove(key);
 						Log.i(TAG,"Removing/update old fact "+val);
 						if (!changedTypes.contains(typeName))
 							changedTypes.add(typeName);						
 					}
 					else
 						Log.i(TAG, "Did not find old fact to remove: "+val);
 					if (messageType==MessageType.FACT_UPD) {
 						val = message.getNewVal();
 						if (!val.getClass().getName().equals(typeName))
 							Log.e(TAG, "Nominal Update from class "+typeName+" to "+val.getClass().getName());
 						typeFacts = facts.get(typeName);
 						if (typeFacts==null) {
 							typeFacts = new HashMap<Object,Object>();
 							facts.put(typeName,typeFacts);
 						}
 						Object newKey = getFactID(val);
 						if (!key.equals(newKey)) 
 							Log.e(TAG,"Nominal Update from ID "+key+" to "+newKey+" for "+val);
 						typeFacts.put(newKey, val);
 						Log.i(TAG,"Update new fact "+val);
 						if (!changedTypes.contains(typeName))
 							changedTypes.add(typeName);
 					}
 				}
 			}
 		}
 		BackgroundThread.cachedStateChanged(this, changedTypes);
 		return messages;
 	}
 	/** remove a Fact from the cache without signalling state change */
 	public void removeFactSilent(Object fact) {
 		if (fact==null)
 			return;
 		synchronized(facts) {
 			String typeName = getFactType(fact);
 			HashMap<Object,Object> typeFacts = facts.get(typeName);
 			if (typeFacts!=null) {
 				Object id = getFactID(fact);
 				if (id!=null) {
 					if (typeFacts.remove(id)==null)
 						Log.w(TAG, "removeFactSilent could not find "+typeName+" "+id);
 				} 
 				else
 					Log.w(TAG,"removeFactSilent called with object without id: "+fact);
 			}
 			else
 				Log.w(TAG, "removeFactSilent could not find any "+typeName);
 		}
 	}
 	private static String getFactType(Object o) {
 		return o.getClass().getName();		
 	}
 	/** HACK application specific */
 	private static Object getFactID(Object o) {
 		if (o instanceof Player) {
 			Player p = (Player)o;
 			return p.getID();
 		}
 		if (o instanceof uk.ac.horizon.ug.exploding.client.model.Message) {
 			uk.ac.horizon.ug.exploding.client.model.Message p = (uk.ac.horizon.ug.exploding.client.model.Message)o;
 			return p.getID();
 		}
 		if (o instanceof Member) {
 			Member p = (Member)o;
 			return p.getID();
 		}
 		if (o instanceof Zone) {
 			Zone p = (Zone)o;
 			return p.getID();
 		}
 		Log.d(TAG,"getFactID for unknown class "+o.getClass().getName());
 		return o; //!?!
 	}
 }
