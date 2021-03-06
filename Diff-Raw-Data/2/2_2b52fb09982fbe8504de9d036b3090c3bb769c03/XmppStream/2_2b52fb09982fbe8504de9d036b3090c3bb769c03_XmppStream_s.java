 /*
  * XmppStream.java
  * Created on 23.12.2011
  *
  * Copyright (c) 2005-2011, Eugene Stahov (evgs@bombus-im.org), 
  * http://bombus-im.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * You can also redistribute and/or modify this program under the
  * terms of the Psi License, specified in the accompanied COPYING
  * file, as published by the Psi Project; either dated January 1st,
  * 2005, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 
 package org.bombusim.xmpp;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.bombusim.lime.Lime;
 import org.bombusim.lime.logger.LimeLog;
 import org.bombusim.lime.logger.LoggerData;
 import org.bombusim.lime.logger.LoggerEvent;
 import org.bombusim.networking.NetworkDataStream;
 import org.bombusim.networking.NetworkSocketDataStream;
 import org.bombusim.xml.Attributes;
 import org.bombusim.xml.XMLException;
 import org.bombusim.xml.XMLParser;
 import org.bombusim.xmpp.exception.XmppException;
 import org.bombusim.xmpp.exception.XmppTerminatedException;
 import org.bombusim.xmpp.handlers.IqPing;
 import org.bombusim.xmpp.handlers.IqRoster;
 import org.bombusim.xmpp.handlers.IqTimeReply;
 import org.bombusim.xmpp.handlers.IqVersionReply;
 import org.bombusim.xmpp.handlers.MessageDispatcher;
 import org.bombusim.xmpp.handlers.PresenceDispatcher;
 import org.bombusim.xmpp.stanza.Iq;
 import org.bombusim.xmpp.stanza.Presence;
 import org.xbill.DNS.Lookup;
 import org.xbill.DNS.Record;
 import org.xbill.DNS.SRVRecord;
 import org.xbill.DNS.Type;
 
 import android.content.Context;
 import android.content.Intent;
 
 /**
  * The stream to a jabber server.
  */
 
 public class XmppStream extends XmppParser {
     
     private static final long KEEPALIVE_PERIOD = 15*60*1000; //15 minutes
 
 	public static final int KEEP_ALIVE_TYPE_PING = 3;
 	public static final int KEEP_ALIVE_TYPE_IQ   = 2;
 	public static final int KEEP_ALIVE_TYPE_CHAR = 1;
 
 	public static final int KEEP_ALIVE_TYPE_NONE = 0;
 
 	String sessionId;
     
     final XmppAccount account;
     
     public String jid;        //bareJid, should be used to refer account 
     public String jidSession; //binded JID, should be used instead of account data 
     
     private String server;
     private String host;  //evaluated from SRV record or specified manually in account
     private int port;	  //evaluated from SRV record or specified manually in account
 
     private String lang;
     
     public boolean pingSent;
     
     //TODO: state machine:{offline, connecting, logged in} 
     public boolean loggedIn;
 
 	protected int keepAliveType = KEEP_ALIVE_TYPE_CHAR;
     
     //private int status = Presence.PRESENCE_INVISIBLE; //our status code
     private int status = Presence.PRESENCE_ONLINE; //our status code
     private String statusMessage = "hello, jabber world!";
     
     private NetworkDataStream dataStream;
 
 	private boolean xmppV1;
     
     //TODO: synchronized
 	private ArrayList<XmppObjectListener> dispatcherQueue;
 
 	private ArrayList<XmppObject> incomingQueue;
 
 	
 	private Timer keepAliveTimer;
 	
     /**
      * Constructor. Connects to the server and sends the jabber welcome message.
      *
      */
     
 	
     public XmppStream( XmppAccount account) {
     	
     	this.account = account;
 
 		server = new XmppJid(account.userJid).getServer();
 		
 		jid = account.userJid;
 		jidSession = jid + '/' + account.resource;
     	
     	dispatcherQueue = new ArrayList<XmppObjectListener>();
     	
     }
 
     public void setLocaleLang(String lang) {
     	this.lang = lang;
     }
     /*
     public void connect() {
     	
     } catch( Exception e ) {
     	//TODO: handle exceptions
     	
         System.out.println("Exception in parser:");
         e.printStackTrace();
         broadcastTerminatedConnection(e);
     };
     
     loggedIn = false;
     	
     }
     */
 
     
     public void initiateStream() throws IOException {
         
         //sendQueue=new Vector();
         
         StringBuilder header=new StringBuilder("<stream:stream to='" )
             .append( server )
             .append( "' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams'");
         
         if (xmppV1) header.append(" version='1.0'");
         
         if (lang!=null) {
             header.append(" xml:lang='").append(lang).append("'");
         }
         header.append( '>' );
         send(header.toString());
     }
 
     @Override
     public boolean tagStart(String name, Attributes attributes) {
         if (name.equals( "stream:stream" ) ) {
             sessionId = attributes.getValue("id");
             String version= attributes.getValue("version");
             xmppV1 = ("1.0".equals(version));
             
             //dispatcher.broadcastBeginConversation();
             
             //XEP-0078 (Obsolete) Non-SASL Authentication
             if (!xmppV1) {
             	NonSASLAuth nsa = new NonSASLAuth();
                 addBlockListener(nsa);
                 addBlockListener(new AuthFallback());
                 nsa.jabberIqAuth(NonSASLAuth.AUTH_GET, this);
             }
             
             return false;
         }
         
         return super.tagStart(name, attributes);
     }
 
     
     public void tagEnd(String name) throws XMLException {
         if (currentBlock == null) {
             if (name.equals( "stream:stream" ) ) {
                 //dataStream.closeConnection();
                 throw new XMLException("Normal stream shutdown");
             }
             return;
         }
         
         if (currentBlock.getParent() == null) {
 
             if (currentBlock.getTagName().equals("stream:error")) {
                 XmppError xe = XmppError.decodeStreamError(currentBlock);
 
                 //dataStream.closeConnection();
                 throw new XMLException(xe.toString());
                 
             }
         }
         
         super.tagEnd(name);
     }
 
     protected void dispatchXmppStanza(XmppObject currentBlock) {
     	incomingQueue.add(currentBlock);
     }
 
     private void dispatchIncomingQueue() throws IOException, XmppException {
     	incomingLoop: for (int index = 0; index < incomingQueue.size(); index++) {
     		
     		XmppObject currentBlock=incomingQueue.get(index);
     		
     		for (XmppObjectListener dispatcher : dispatcherQueue) {
         		int result=dispatcher.blockArrived(currentBlock, this);
         		switch (result) {
     			case XmppObjectListener.BLOCK_PROCESSED: 
     				continue incomingLoop;
     			case XmppObjectListener.NO_MORE_BLOCKS:
     				dispatcherQueue.remove(dispatcher);
     				continue incomingLoop;
     			}
         	}
     		
     	}
     	
     	incomingQueue.clear();
     }
     
     /*public void startKeepAliveTask(){
         Account account=StaticData.getInstance().account;
         if (account.keepAliveType==0) return;
         keepAlive=new TimerTaskKeepAlive(account.keepAlivePeriod, account.keepAliveType);
     }*/
     
     public void connect() throws UnknownHostException, IOException, XMLException, XmppException {
     	xmppV1 = true;
     	
         loggedIn = false;
 
 		addBlockListener( new StartTLS() );
 		addBlockListener( new StreamCompression() );
 		addBlockListener( new SASLAuth() );
 		
 		//TODO: uncomment if old non-xmppv1 compliant server will be found 
 		//addBlockListener( new NonSASLAuth() );
 		
 		addBlockListener( new AuthFallback() );
 		
 		incomingQueue = new ArrayList<XmppObject>();
         
     	if (host == null) {
     		
     		if (account.specificHostPort) {
     			host = account.xmppHost;
     			port = account.xmppPort;
     		} else {
     		
         		// workaround for android 2.2 and org.xbill.DNS
         		// java.net.SocketException: Bad address family
     			// see http://stackoverflow.com/questions/2879455/android-2-2-and-bad-address-family-on-socket-connect
     			// see http://code.google.com/p/android/issues/detail?id=9431
         		// android 2.3: ok
     			
     			if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.FROYO) {
     				//TODO: check if IPv6 enabled kernel
     				java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
     				java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
     			}
         	
     			String srvRecord = "_xmpp-client._tcp." + server;
     			
     			LimeLog.i("SRV", "Lookup for " + srvRecord, null);
     			
     			Lookup l = new Lookup(srvRecord, Type.SRV);
         	
     			//TODO: caching SRV requests
         	
     			Record [] records = l.run();
     			
     			switch (l.getResult()) {
     			case Lookup.HOST_NOT_FOUND:
     				throw new UnknownHostException("Host not found: "+server);
     				
     			case Lookup.TYPE_NOT_FOUND:
     				LimeLog.i("SRV", server + " has no SRV record", null);
     				host = server;
     				port = 5222;
     				break;
     				
     			case Lookup.TRY_AGAIN:
     				throw new IOException("Network is down during SRV lookup");
     				
     			case Lookup.UNRECOVERABLE:
     				throw new IOException("Network is down during SRV lookup (unrecoverable)");
     				
     			case Lookup.SUCCESSFUL:
     			default:
         			if (records != null) {
         				host = ((SRVRecord)records[0]).getTarget().toString();
         				port = ((SRVRecord)records[0]).getPort();
         			}
     			}
         	
         	
     		}
     		
     		if (host==null) {
 				LimeLog.i("SRV", "Assuming host = "+ server, null);
 		
     			host = server;
     		}
     		
     		if (port == 0) port = XmppAccount.DEFAULT_XMPP_PORT;
     		if (port == XmppAccount.DEFAULT_XMPP_PORT  && account.secureConnection == XmppAccount.SECURE_CONNECTION_LEGACY_SSL) {
     			port = XmppAccount.DEFAULT_SECURE_XMPP_PORT;
     		}
     	}
     	
     	dataStream = new NetworkSocketDataStream(host, port);
     	
     	if (account.secureConnection == XmppAccount.SECURE_CONNECTION_LEGACY_SSL) {
     		((NetworkSocketDataStream)dataStream).setTLS();
     	}
     	
         XMLParser parser = new XMLParser( this );
         
         initiateStream();
         
         byte cbuf[]=new byte[4096];
         
         while (true) {
 
         	//blocking operation
         	int length = 0;
         	try {
         		length=dataStream.read(cbuf);
         	} catch (NullPointerException npe) {
         		//TODO: Exception in ZLIB (check why)
         		throw new IOException("Unexpected end of packed stream");
         	}
             
             if (length==0) {
                 try { Thread.sleep(100); } catch (Exception e) {};
                 continue;
             }
 
             if (length == -1) 
             	throw new IOException("(-1) End of stream reached");
             
         	Lime.getInstance().getLog().addLogStreamingEvent(LoggerEvent.XMLIN, jid, cbuf, length);
         	if (Lime.getInstance().localXmlEnabled)   sendBroadcast(LoggerData.UPDATE_LOG);
             
             parser.parse(cbuf, length);
             
             dispatchIncomingQueue();
         }
             
             //dispatcher.broadcastTerminatedConnection( null );
     }
     
     /**
      * Method to close the connection to the server 
      */
     
     public void close() {
     	
     	cancelKeepAliveTimer();
         //if (keepAlive!=null) keepAlive.destroyTask();
         
     	//cancelling all XmppObjectListeners
         dispatcherQueue.clear();
         
         try {
             send( "</stream:stream>" );
             //a chance to gracefully close xmpp streams
             try {  Thread.sleep(500); } catch (Exception e) {};
         } catch( IOException e ) {
             // Ignore an IO Exceptions because they mean that the stream is
             // unavailable, which is irrelevant.
         } 
         
         try {
             dataStream.closeConnection();
         } catch (IOException e) {
 			// TODO: handle exception
         	e.printStackTrace();
 		} catch (NullPointerException e) { 
 			// ignoring - socket was not opened  
 		}
         broadcastTerminatedConnection(new XmppTerminatedException("Connection closed"));
     }
 
 	private void cancelKeepAliveTimer() {
 		if (keepAliveTimer != null) {
     		keepAliveTimer.cancel();
     		keepAliveTimer = null;
     	}
 	}
     
 	private void startKeepAliveTimer() {
 		//cancel old timer
 		cancelKeepAliveTimer();
 		//start new timer
 		keepAliveTimer = new Timer();
 		
 		keepAliveTimer.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				try {
 					sendKeepAlive(keepAliveType);
 					LimeLog.i("KeepAlive", "sent", null);
 					
 				} catch (IOException e) {
 					cancel();
 					LimeLog.e("KeepAlive", "IOException", e.toString());
 				}
 				
 			}
 		}, KEEPALIVE_PERIOD, KEEPALIVE_PERIOD);
 	}
 	
     private void broadcastTerminatedConnection(Exception exception) {
 		// TODO Auto-generated method stub
     	System.out.println("Broadcasted exception <Close>");
 		exception.printStackTrace();
 	}
 
 
     
     
     /**
      * Method of sending data to the server.
      *
      * @param The data to send to the server.
      */
     public void sendKeepAlive(int type) throws IOException {
         switch (type){
             case KEEP_ALIVE_TYPE_PING:
                 ping();
                 break;
             case KEEP_ALIVE_TYPE_IQ:
                 send("<iq/>");
                 break;
             case KEEP_ALIVE_TYPE_CHAR:
                 send(" ");
         }
     }
     
     private void ping() {
         XmppObject ping=new Iq(server, Iq.TYPE_GET, "ping");
         //ping.addChildNs("query", "jabber:iq:version");
         ping.addChildNs("ping", "urn:xmpp:ping");
         pingSent=true;
         send(ping);
     }
 
   
     public synchronized void send(byte[] data, int length) throws IOException {
     	Lime.getInstance().getLog().addLogStreamingEvent(LoggerEvent.XMLOUT, jid, data, length);
     	if (Lime.getInstance().localXmlEnabled)   sendBroadcast(LoggerData.UPDATE_LOG);
     	
     	if (dataStream == null) throw new IOException("Writing to closed stream");
     	dataStream.write(data, length);
     }
     
     public void send( String data ) throws IOException {
     	byte[] bytes = data.getBytes(); 
     	send(bytes, bytes.length);
     }
     
     /**
      * Method of sending a Jabber datablock to the server.
      *
      * @param block The data block to send to the server.
      */
     
     public void send( XmppObject block )  {
     	
     	StringBuilder data=new StringBuilder(4096);
     	block.constructXML(data);
     	
    		ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
    		int dp=0;
     		
    		while (dp<data.length()) {
     		baos.write((byte)data.charAt(dp++));
     	}
     		
    		byte[] bytes = baos.toByteArray();
    		int length = baos.size();
     		
     	try {
     		send(bytes, length);
     	} catch (Exception e) {
     		//TODO: verify action on error
     		e.printStackTrace();
     		close();
 		}
     }
     
     /**
      * Set the listener to this stream.
      */
     
     public void addBlockListener(XmppObjectListener listener) { 
         dispatcherQueue.add(listener);
     }
     public void cancelBlockListener(XmppObjectListener listener) {
     	dispatcherQueue.remove(listener);
     }
     
     @SuppressWarnings("rawtypes")
 	public void cancelBlockListenerByClass(Class removeClass) {
     	int i=0;
     	while (i<dispatcherQueue.size()) {
     		if (dispatcherQueue.get(i).getClass().equals(removeClass)) {
     			dispatcherQueue.remove(i);
     			continue;
     		}
     		i++;
     	}
     }
     
     public boolean isXmppV1() { return xmppV1; }
 
     public String getSessionId() { return sessionId; }
 
 
     public void setZlibCompression() throws IOException {
         ((NetworkSocketDataStream)dataStream).setCompression();
     }
     public void setTLS() throws IOException {
         ((NetworkSocketDataStream)dataStream).setTLS();
     }
     
     public boolean isSecured() {
     	return ((NetworkSocketDataStream)dataStream).isSecure();
     }
 
     void loginSuccess() {
     	
     	//remove all auth listeners
     	dispatcherQueue.clear();
     	
     	loggedIn=true;
     	
     	IqRoster iqroster=new IqRoster();
     	addBlockListener(iqroster);
 
     	addBlockListener(new IqVersionReply());
     	
     	addBlockListener(new IqTimeReply());
     	
     	addBlockListener(new PresenceDispatcher());
     	
     	addBlockListener(new MessageDispatcher());
     	
    	addBlockListener(new IqPing);
     	
     	iqroster.queryRoster(this);
 
     	
     	Presence online = new Presence(status, account.priority, statusMessage, "evgs");
     	//offline messages will be delivered after this presence
     	send(online);
     	
     	startKeepAliveTimer();
 
     }
     
     private Context serviceContext;
     
 	public void setContext(Context context) {
 		// TODO Auto-generated method stub
 		serviceContext = context;
 	}
 
 	public void sendBroadcast(String message) {
 		serviceContext.sendBroadcast(new Intent(message));
 	}
 	
 }
