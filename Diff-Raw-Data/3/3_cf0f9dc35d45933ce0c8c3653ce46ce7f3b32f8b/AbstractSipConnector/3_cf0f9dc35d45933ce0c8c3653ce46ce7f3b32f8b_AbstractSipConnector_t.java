 // ========================================================================
 // Copyright 2008-2009 NEXCOM Systems
 // ------------------------------------------------------------------------
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at 
 // http://www.apache.org/licenses/LICENSE-2.0
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 // ========================================================================
 
 package org.cipango.sip;
 
 import java.io.IOException;
 import java.net.InetAddress;
 
 import javax.servlet.sip.SipURI;
 
 import org.cipango.Server;
 import org.cipango.SipHandler;
 import org.cipango.SipHeaders;
 import org.cipango.SipMessage;
 import org.cipango.SipRequest;
 import org.cipango.SipResponse;
 import org.cipango.SipURIImpl;
 import org.cipango.SipVersions;
 import org.cipango.Via;
 import org.cipango.io.SipBuffer;
 import org.mortbay.component.AbstractLifeCycle;
 import org.mortbay.component.LifeCycle;
 import org.mortbay.io.Buffer;
 import org.mortbay.io.ByteArrayBuffer;
 import org.mortbay.io.BufferCache.CachedBuffer;
 import org.mortbay.log.Log;
 import org.mortbay.thread.ThreadPool;
 
 public abstract class AbstractSipConnector extends AbstractLifeCycle implements SipConnector 
 {
     public static String __localhost;
     
     static
     {
         try
         {
             __localhost = InetAddress.getLocalHost().getHostAddress();
         }
         catch (Exception e)
         {
             Log.ignore(e);
             __localhost = "127.0.0.1";
         }
     }
     
     private int _port;
     private String _host;
     private String _name;
     
     private SipURI _sipUri;
     private Via _via;
     
     private int _acceptors = 1;
     private Thread[] _acceptorThread;
     
     private SipHandler _handler;
     private Server _server;
     private ThreadPool _threadPool;
 
     private boolean _transportParam = false;
     
     Object _statsLock = new Object();
     transient long _statsStartedAt = -1;
     transient long _connectionsOpen;
     transient long _connectionsOpenMax;
     transient long _nbParseErrors;
    
     public AbstractSipConnector() 
     {
         _port = getDefaultPort();
         setHost( __localhost);
         
         updateURI();
     }
     
     public void setPort(int port) 
     {
     	if (isRunning())
     		throw new IllegalStateException("running");
     	
     	if (port == -1)
     		port = getDefaultPort();
     	
         _port = port;
         
         updateURI();
     }
     
     public int getPort() 
     {
         return _port;
     }
     
     public void setHost(String host) 
     {
     	if (isRunning())
     		throw new IllegalStateException();
     	
     	if (host == null)
     		host = __localhost;
     	
     	if (host.contains(":") && !host.contains("["))
     		_host = "[" + host + "]";
     	else
             _host = host;
         
         updateURI();
     }
     
     public String getHost() 
     {
         return _host;
     }
     
     public void setName(String name) 
     {
     	if (isRunning())
     		throw new IllegalStateException();
         _name = name;
         
         updateURI();
     }
     
     public String getName() 
     {
         return _name;
     }
     
     public Via getVia() 
     {
         return _via;
     }
 
     public void setTransportParam(boolean b) 
     {
     	if (isRunning())
     		throw new IllegalStateException();
     	_transportParam = b;
     }
     
     public String getTransport() 
     {
         return SipConnectors.getName(getTransportOrdinal());
     }
     
     protected void updateURI()
     {
         _sipUri = new SipURIImpl(_name, _host, _port);
     }
     
     protected void doStart() throws Exception 
     {
          if (_transportParam)
              _sipUri.setTransportParam(getTransport().toLowerCase());
         
         _via = new Via(
                 SipVersions.SIP_2_0,
                 getTransport().toUpperCase(),
                 _host,
                 _port);
         
         if (_threadPool == null && getServer() != null)
         	_threadPool = getServer().getSipThreadPool();
         
         if (_threadPool instanceof LifeCycle)
         {
         	if (getServer() == null || _threadPool != getServer().getSipThreadPool())
         		((LifeCycle) _threadPool).start();
         }
 
         open();
         
         //_acceptor = new Thread(new Acceptor());
         //_acceptor.start();
         
         synchronized(this)
         {
             _acceptorThread = new Thread[getAcceptors()];
 
             for (int i = 0; i < _acceptorThread.length; i++)
             {
                 if (!_threadPool.dispatch(new Acceptor(i)))
                 {
                     Log.warn("insufficient maxThreads configured for {}", this);
                     break;
                 }
             }
         }
         
         Log.info("Started {}", this);
     }
         
     protected void doStop() throws Exception 
     {
     	try { close(); } catch(IOException e) { Log.warn(e); }
         
         if (_server != null && _threadPool == _server.getSipThreadPool())
             _threadPool = null;
         if (_threadPool instanceof LifeCycle)
             ((LifeCycle) _threadPool).stop();
         
         super.doStop();
         
         Thread[] acceptors = null;
         synchronized(this)
         {
             acceptors = _acceptorThread;
             _acceptorThread = null;
         }
         if (acceptors != null)
         {
             for (int i = 0; i < acceptors.length; i++)
             {
                 Thread thread = acceptors[i];
                 if (thread != null)
                     thread.interrupt();
             }
         }	
     }
     
     public abstract void accept(int acceptorID) throws IOException, InterruptedException;
     
     public void process(SipMessage message)
     {
    	if (!isRunning())
    		return;
    	
     	if (!getThreadPool().dispatch(new MessageTask(message)))
 		{
     		Log.warn("No threads to dispatch message from {}:{}",
 					message.getRemoteAddr(), message.getRemotePort());
 		}
     }
     
     public void setHandler(SipHandler handler)
     {
     	_handler = handler;
     }
     
     public SipHandler getHandler()
     {
     	return _handler;
     }
     
     public void setServer(Server server) 
     {
         _server = server;
     }
     
     public Server getServer()
     {
         return _server;
     }
     
     public ThreadPool getThreadPool()
     {
         return _threadPool;
     }
     
     public void setThreadPool(ThreadPool threadPool)
     {
     	_threadPool = threadPool;
     }
     
     public void setAcceptors(int acceptors)
     {
     	_acceptors = acceptors;
     }
     
     public int getAcceptors()
     {
     	return _acceptors;
     }
     
     public SipURI getSipUri()
     {
         return _sipUri;
     }
     
     public void send(Buffer buffer, SipEndpoint endpoint) throws IOException
     {
     	throw new UnsupportedOperationException();
     }
     
     public String toString() 
     {
     	/*
         return SipConnectors.getName(_type) + " @ " + 
             (getAddr() == null ? _host : getAddr().getHostAddress()) +
             ":" + _port;
             */
         String name = this.getClass().getName();
         int dot = name.lastIndexOf('.');
         if (dot>0)
             name=name.substring(dot+1);
         
         return name+"@"+(getHost()==null?"0.0.0.0":getHost())+":"+(getLocalPort()<=0?getPort():getLocalPort());
     }
     
     public long getNbParseError()
     {
 		return _nbParseErrors;
 	}
 	
 	public void statsReset() 
 	{
 		_statsStartedAt = _statsStartedAt == -1 ? -1 : System.currentTimeMillis();
 		_nbParseErrors = 0;
 		_connectionsOpen = 0;
 		_connectionsOpenMax = 0;
 	}
 	
 	public void setStatsOn(boolean on) 
 	{
         if (on && _statsStartedAt != -1) 
         	return;
         
         statsReset();
         _statsStartedAt = on ? System.currentTimeMillis() : -1;
     }
 
 	public long getConnectionsOpen()
 	{
 		return _connectionsOpen;
 	}
 
 	public long getConnectionsOpenMax()
 	{
 		return _connectionsOpenMax;
 	}
 	
     class Acceptor implements Runnable 
     {
     	int _acceptor = 0;
         
         Acceptor(int id)
         {
             _acceptor = id;
         }
         
         public void run() 
         {
         	Thread current = Thread.currentThread();
             synchronized(AbstractSipConnector.this)
             {
                 if (_acceptorThread == null)
                     return;
                 _acceptorThread[_acceptor] = current;
             }
             String name = _acceptorThread[_acceptor].getName();
             current.setName(name + " - Acceptor" + _acceptor + " " + AbstractSipConnector.this);
             int old_priority = current.getPriority();
             
             try 
             {
                 while (isRunning() && getConnection() != null) 
                 {
                     try 
                     {
                         accept(_acceptor);
                     } 
                     catch (IOException ioe) 
                     {
                         Log.ignore(ioe);
                     } 
                     catch (Exception e) 
                     {
                         Log.warn(e);
                     }
                 }
             } 
             finally 
             {
             	current.setPriority(old_priority);
                 current.setName(name);
                 try
                 {
                     if (_acceptor == 0)
                         close();
                 }
                 catch (IOException e)
                 {
                     Log.warn(e);
                 }
                 
                 synchronized(AbstractSipConnector.this)
                 {
                     if (_acceptorThread != null)
                         _acceptorThread[_acceptor] = null;
                 }
             }
         }
     }
     
     class MessageTask implements Runnable
     {
     	private SipMessage _message;
     	
     	public MessageTask(SipMessage message)
     	{
     		_message = message;
     	}
     	
     	public void run()
     	{
     		try 
     		{
     			getHandler().handle(_message);
     		}
     		catch (Exception e)
     		{
     			Log.warn(e);
     		}
     	}
     }
     	
 	public static class EventHandler extends SipParser.EventHandler
 	{
 		public static final String UTF_8 = "UTF-8";
 
 		private SipMessage msg;
 		private Exception exception;
 		
 		public void startRequest(Buffer method, Buffer uri, Buffer version) throws IOException
 		{
 			try
 			{
 				SipRequest request = new SipRequest();
 	            if (!(method instanceof CachedBuffer))
 	            {
 	            	if (Log.isDebugEnabled())
 	            		Log.debug("Unknown method: " + method);
 	            }
 	            
 				request.setMethod(method.toString());
 				request.setRequestURI(uri);
 				msg = request;
 			} 
 			catch (Exception e)
 			{
 				exception = e;
 				throw new IOException("Parsing error");
 			}
 		}
 		
 		public void startResponse(Buffer version, int status, Buffer reason) throws IOException 
 		{
 			SipResponse response = new SipResponse();
 			response.setStatus(status, reason.toString());
 			msg = response;
 		}
 		
 		public SipMessage getMessage() 
 		{
 			return msg;
 		}
 
 		public Exception getException() 
 		{
 			return exception;
 		}
 		
 		public boolean hasException()
 		{
 			return exception != null;
 		}
 		
 		public void header(Buffer name, Buffer value) throws IOException
 		{
 			if (msg == null) 
 				throw new IOException("!status line");
 			//msg.getFields().add(name, new SipBuffer(value.asArray()), false);
 			//msg.getFields().add(name, value, false);
 			add(name, value);
 		}
 		
 		public void content(Buffer buffer) throws IOException
 		{
 			if (buffer.length() > 0)
 				msg.setRawContent(buffer.asArray()); // TODO buffer
 		}
 		
 		public void reset()
 		{
 			msg = null;
 			exception = null;
 		}
 		
 		private byte[] asArray(byte[] src, int index, int length)
 		{
 			byte[] bytes = new byte[length];
 			System.arraycopy(src, index, bytes, 0, length);
 			return bytes;
 		}
 		
 		public void add(Buffer name, Buffer value) 
 	    {
 			if (!name.isImmutable())
 				name = new SipBuffer(name.asArray());
 			
 	        if (SipHeaders.getType(name).isList())
 	        {
 	            boolean quote = false;
 	    
 	            int start = value.getIndex();
 	            int end = value.putIndex();
 	            byte[] b = value.array();
 	            
 	            //if (value == null) value = _value.asArray();
 	            
 	            int startValue = start;
 	            int endValue = end;
 	            
 	            while (end > start && b[end -1] <= ' ') end--;
 	                    
 	            for (int i = start; i < end; i++)
 	            {
 	                int c = b[i];
 	                if (c == '"') quote = !quote;
 	    
 	                if (c == ',' && !quote)
 	                {
 	                    endValue = i;
 	                    while (endValue > start && b[endValue -1] <= ' ') endValue--;
 	                    
 	                    while (startValue < endValue && b[startValue] <= ' ') startValue++;
 	                    
 	                    byte[] bValue = asArray(b, startValue, endValue - startValue);
 	                    
 	                    //Buffer buffer = new View(value, startValue, startValue, endValue, Buffer.READONLY);
 	                    msg.getFields().addBuffer(name, new ByteArrayBuffer(bValue));
 	                    
 	                    //value = new View(value, i + 1, i + 1, end, Buffer.READONLY);
 	                    
 	                    startValue = i + 1;
 	                }
 	            }
 	            while (startValue < end && b[startValue] <= ' ') startValue++;
 	            
 	            byte[] bValue = asArray(b, startValue, end - startValue);
 	            msg.getFields().addBuffer(name, new ByteArrayBuffer(bValue));
 	            
 	            //value = new View(value, startValue, startValue, end, Buffer.READONLY);
 	        }
 	        else
 	        {	
 	        	msg.getFields().addBuffer(name, new ByteArrayBuffer(value.asArray())); 
 	        }
 		}
 		
 	}
 
 	
 }
