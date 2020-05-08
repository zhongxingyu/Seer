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
 
 package org.cipango.servlet;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.sip.Address;
 import javax.servlet.sip.Proxy;
 import javax.servlet.sip.ServletParseException;
 import javax.servlet.sip.SipApplicationSession;
 import javax.servlet.sip.SipServletRequest;
 import javax.servlet.sip.SipServletResponse;
 import javax.servlet.sip.SipSession;
 import javax.servlet.sip.SipSessionAttributeListener;
 import javax.servlet.sip.SipSessionBindingEvent;
 import javax.servlet.sip.SipSessionBindingListener;
 import javax.servlet.sip.SipSessionEvent;
 import javax.servlet.sip.SipSessionListener;
 import javax.servlet.sip.SipURI;
 import javax.servlet.sip.TooManyHopsException;
 import javax.servlet.sip.UAMode;
 import javax.servlet.sip.URI;
 import javax.servlet.sip.ar.SipApplicationRoutingRegion;
 
 import org.cipango.Call;
 import org.cipango.NameAddr;
 import org.cipango.Server;
 import org.cipango.SipException;
 import org.cipango.SipHeaders;
 import org.cipango.SipMessage;
 import org.cipango.SipMethods;
 import org.cipango.SipParams;
 import org.cipango.SipRequest;
 import org.cipango.SipResponse;
 import org.cipango.Call.TimerTask;
 import org.cipango.servlet.Session.ServerInvite.ReliableContext;
 import org.cipango.sip.ClientTransaction;
 import org.cipango.sip.ClientTransactionListener;
 import org.cipango.sip.ServerTransaction;
 import org.cipango.sip.ServerTransactionListener;
 import org.cipango.sip.SipConnectors;
 import org.cipango.sip.Transaction;
 import org.cipango.sipapp.SipAppContext;
 import org.cipango.sipapp.SipXmlConfiguration;
 import org.cipango.util.ID;
 import org.cipango.util.ReadOnlyAddress;
 import org.cipango.util.concurrent.AppSessionLockProxy;
 import org.mortbay.log.Log;
 import org.mortbay.util.LazyList;
 
 public class Session implements SessionIf, ClientTransactionListener, ServerTransactionListener, Serializable, Cloneable
 {	
 	private static final long serialVersionUID = 1L;
     
 	public enum Role { UNDEFINED, UAC, UAS, PROXY };
 	
     private long _created = System.currentTimeMillis();
     private long _accessed = _created;
     private String _id;
     
     private Role _role = Role.UNDEFINED;
     private State _state = State.INITIAL;
     
     private boolean _valid = true;
 
     private NameAddr _localParty;
     private NameAddr _remoteParty;
     
     private URI _remoteTarget;
     private String _callId;
     private long _localCSeq = 1;
     private long _remoteCSeq = -1;
     private LinkedList _routeSet;
     private boolean _secure;
     private boolean _invalidateWhenReady;
     // true if it is acting as a non-record-routing proxy or if the SipSession
     // acting as a UAC transitions from the EARLY state back to the INITIAL 
     // state on account of receiving a non-2xx final response.
     // 'May' term refers to the fact that there could be pending transactions
     private boolean _mayReadyToInvalidate = false;
     private boolean _invokingServlet = false;
     
     private int _rseq = 1;
     
     private Map<String, Object> _attributes;
     
     private AppSession _appSession;
     private String _linkedSessionId;
     private transient SipServletHolder _handler;
     private String _handlerName;
 
     private transient Object _invites;
     private transient Object _cinvites;
     
     private URI _subscriberURI;
     private SipApplicationRoutingRegion _region;
     
 	public Session(AppSession appSession, String id)
     {
         _appSession = appSession;
         _id = id;
         _invalidateWhenReady = appSession.getContext().getSpecVersion() != SipXmlConfiguration.VERSION_10; // TODO
 	}
 
 	public Session(AppSession appSession, String id, String callId, NameAddr local, NameAddr remote) 
     {
 		this(appSession, id);
         _role = Role.UAC;
         
         _callId = callId;
         _localParty = local;
 		_remoteParty = remote;
 	} 
 	
 	/**
 	 * @see SipSession#createRequest(java.lang.String)
 	 */
 	public SipServletRequest createRequest(String method) 
     {
 		checkValid();
 		
 		if (method.equals(SipMethods.ACK) || method.equals(SipMethods.CANCEL))
 			throw new IllegalArgumentException("Method " + method + " not allowed here");
 		
 		// TODO throws java.lang.IllegalStateException - if this SipSession is in the INITIAL 
 		// state and there is an ongoing transaction 
 		if (_state == State.TERMINATED)
 			throw new IllegalStateException("In state TERMINATED");
 		
 		return createRequest(method, _localCSeq++);
 	}
 	
 	/**
 	 * @see SipSession#getApplicationSession()
 	 */
 	public SipApplicationSession getApplicationSession()
     {
 		return new AppSessionLockProxy(_appSession);
 	}
 	
 	/**
 	 * @see SipSession#getAttribute(java.lang.String)
 	 */
 	public Object getAttribute(String name) 
     {
 		checkValid();
 		if (name == null)
 			throw new NullPointerException("Name is null");
 		if (_attributes == null) 
 			return null;
 		return _attributes.get(name);
 	}
 
 	/**
 	 * @see SipSession#getAttributeNames()
 	 */
 	public Enumeration<String> getAttributeNames() 
     {
 		checkValid();
 		if (_attributes == null) 
 		{
 			List<String> list = Collections.emptyList();
 			return Collections.enumeration(list);
 		}
 		return Collections.enumeration(_attributes.keySet());
 	}
 
 	/**
 	 * @see SipSession#getCallId()
 	 */
 	public String getCallId() 
     {
 		return _callId;
 	}
 
 	/**
 	 * @see SipSession#getCreationTime()
 	 */
 	public long getCreationTime() 
     {
 		checkValid();
 		return _created;
 	}
 	
 	/**
 	 * @see SipSession#getId()
 	 */
 	public String getId() 
     {
 		return _id;
 	}
 
 	/**
 	 * @see SipSession#getLocalParty()
 	 */
 	public Address getLocalParty() 
     {
 		return new ReadOnlyAddress(_localParty);
 	}
 
 	/**
 	 * @see SipSession#getRemoteParty()
 	 */
 	public Address getRemoteParty() 
     {
 		return new ReadOnlyAddress(_remoteParty); 
 	}
 	
 	/**
 	 * @see SipSession#getLastAccessedTime()
 	 */
 	public long getLastAccessedTime() 
     {
 		return _accessed;
 	}
 
 	/**
 	 * @see SipSession#getState()
 	 */
 	public State getState() 
     {
 		checkValid();
 		return _state;
 	}
 
 	/**
 	 * @see javax.servlet.sip.SipSession#invalidate()
 	 */
 	public void invalidate() 
     {
 		checkValid();
 		_valid = false;
 		_appSession.removeSession(this);
 	}
 	
 
 	/**
 	 * @see SipSession#removeAttribute(java.lang.String)
 	 */
 	public void removeAttribute(String name) 
     {
 		checkValid();
 		
 		if (_attributes == null) 
 			return;
 		
 		Object oldValue = _attributes.remove(name);
 		if (oldValue != null)
 		{
 			unbindValue(name, oldValue);
 			
 			SipSessionAttributeListener[] listeners = _appSession.getContext().getSessionAttributeListeners();
 			if (listeners.length > 0)
 			{
 				SipSessionBindingEvent event = 
 					new SipSessionBindingEvent(this, name);
 				for (int i = 0; i < listeners.length; i++)
 					listeners[i].attributeRemoved(event);
 			}
 		}
 	}
 	
 	/**
 	 * @see SipSession#setAttribute(String, Object)
 	 */
 	public void setAttribute(String name, Object value) 
     {
 		checkValid();
 		
 		if (name == null || value == null)
 			throw new NullPointerException("Name or value is null");
 		
 		if (_attributes == null) 
 			_attributes = new HashMap<String, Object>();
 		
 		Object oldValue = _attributes.put(name, value);
 		
 		if (oldValue == null || !value.equals(oldValue))
 		{
 			unbindValue(name, oldValue);
 			bindValue(name, value);
 			
 			SipSessionAttributeListener[] listeners = _appSession.getContext().getSessionAttributeListeners();
 			if (listeners.length > 0)
 			{
 				SipSessionBindingEvent event = 
 					new SipSessionBindingEvent(this, name);
 				for (int i = 0; i < listeners.length; i++)
 				{
 					if (oldValue == null)
 						listeners[i].attributeAdded(event);
 					else
 						listeners[i].attributeReplaced(event);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @see SipSession#setHandler(String)
 	 */
 	public void setHandler(String name) throws ServletException 
     {
 		checkValid();
 		
         SipServletHolder handler = findHandler(name);
         if (handler == null)
             throw new ServletException("No handler named " + name);
         
         setHandler(handler);
 	}
 	
 	/**
 	 * @see SipSession#getRegion()
 	 */
 	public SipApplicationRoutingRegion getRegion()
 	{
 		checkValid();
 		return _region; // TODO received only
 	}
 
 	/**
 	 * @see SipSession#getServletContext()
 	 */
 	public ServletContext getServletContext()
 	{
 		return _appSession.getContext().getServletContext();
 	}
 
 	/**
 	 * @see SipSession#getSubscriberURI()
 	 */
 	public URI getSubscriberURI()
 	{
 		checkValid();
 		return _subscriberURI; // TODO received only
 	}
 	
 	/**
 	 * @see SipSession#isValid()
 	 */
 	public boolean isValid()
 	{
 		return _valid;
 	}
 
 	/**
 	 * @see SipSession#setOutboundInterface(InetAddress)
 	 */
 	public void setOutboundInterface(InetAddress address)
 	{
 		checkValid();
 		if (address == null)
 			throw new NullPointerException("Null address");
 		// TODO 
 	}
 
 	/**
 	 * @see SipSession#setOutboundInterface(InetSocketAddress)
 	 */
 	public void setOutboundInterface(InetSocketAddress address)
 	{
 		checkValid();
 		if (address == null)
 			throw new NullPointerException("Null address");
 		// TODO
 	}
 	
 	/**
 	 * @see SipSession#isReadyToInvalidate()
 	 */
 	public boolean isReadyToInvalidate()
 	{
 		return isReadyToInvalidate(true);
 	}
 	
 	/**
 	 * @see SipSession#getInvalidateWhenReady()
 	 */
 	public boolean getInvalidateWhenReady()
 	{
 		checkValid();
 		return _invalidateWhenReady;
 	}
 
 	/**
 	 * @see SipSession#setInvalidateWhenReady(boolean)
 	 */
 	public void setInvalidateWhenReady(boolean invalidateWhenReady)
 	{
 		checkValid();
 		_invalidateWhenReady = invalidateWhenReady;
 	}
 	
     // ======================= non-API methods ============================
 	
 	public Call getCall()
     {
         return _appSession.getCall();
     }
 	
 	protected void setId(String id)
 	{
 		if (_id != null) throw new IllegalStateException("id != null");
 		_id = id;
 	}
     
     public void setLocalCSeq(long cseq)
     {
         _localCSeq = cseq;
     }
 
 	private void checkValid() 
 	{
 		if (!_valid) 
 			throw new IllegalStateException("Session has been invalidated");
 	}
 	
 	private SipServletHolder findHandler(String name)
 	{
 		SipAppContext context = _appSession.getContext();
         SipServletHolder handler = context.getSipServletHandler().getHolder(name);
         return handler;
 	}
    
 	protected void bindValue(String name, Object value)
 	{
 		if (value != null && value instanceof SipSessionBindingListener)
 			((SipSessionBindingListener) value).valueBound(new SipSessionBindingEvent(this, name));
 	}
 	
 	protected void unbindValue(String name, Object value)
 	{
 		if (value != null && value instanceof SipSessionBindingListener)
 			((SipSessionBindingListener) value).valueUnbound(new SipSessionBindingEvent(this, name));
 	}
 	
     public Address getContact()
     {
         Address addr = getServer().getTransportManager().getContact(SipConnectors.TCP_ORDINAL);
         //((SipURI) addr.getURI()).setParameter("transport", "tcp");
         return addr;
     }
     
 	public Role getRole() 
     {
 		return _role;
 	}
 	
 	public boolean isProxy() 
     {
         return _role == Role.PROXY;
     }
 	
 	public boolean isUA() 
     {
         return _role == Role.UAC || _role == Role.UAS;
     }
 	
 	public ClientTransaction sendRequest(SipRequest request, ClientTransactionListener listener) 
     {
         // TODO
 		//System.out.println("Sending request");
 		request.setCommitted(true);
 		access();
 		if (request.isAck())
 		{
 			ClientInvite invite = getClientInvite(request.getCSeq().getNumber(), false);
 			ClientTransaction ctx = getServer().getTransactionManager().sendRequest(request, listener);
 			if (invite != null)
 				invite.setAck(ctx);
 			return ctx;
 		}
 		else 
 		{
 			return getServer().getTransactionManager().sendRequest(request, listener);
 		}
     }
 	
 	public void sendResponse(SipResponse response, ServerTransaction tx, boolean reliable) throws IOException
     {
 		if (tx.getState() >= ServerTransaction.STATE_COMPLETED && !response.isSendOutsideTx()) 
             throw new IllegalStateException("Transaction terminated " + tx);
     	
 		int status = response.getStatus();
         
         SipRequest request = (SipRequest) response.getRequest();
         
         if (request.isInitial())
         {
             if (status > 100)
             {
                 String rtag = response.to().getParameter(SipParams.TAG);
                 if (rtag == null) 
                 {
                     String tag = _localParty.getParameter(SipParams.TAG);
                     if (tag == null) 
                     {
                         tag = ID.newTag();
                         _localParty.setParameter(SipParams.TAG, tag);
                     }
                     response.to().setParameter(SipParams.TAG, tag);
                 }
             }
             
             if (status > 100 && _role == Role.UNDEFINED) // TODO virtual branches
                 setRole(Role.UAS);
             
             if (request.isInvite() || request.isSubscribe())
             {
                 if (_state == State.INITIAL)
                 {
                     if (status < 200)
                         setState(State.EARLY);
                     else if (status < 300)
                     	setState(State.CONFIRMED);
                     else
                     	setState(State.TERMINATED);
                     
                     if (_state == State.EARLY || _state == State.CONFIRMED)
                     {
                         _remoteCSeq = request.getCSeq().getNumber();
                         _secure = request.isSecure() && request.getRequestURI().getScheme().equals("sips");
         
                         setRemoteTarget(request);
                         
                         ListIterator it = request.getFields().getValues(SipHeaders.RECORD_ROUTE_BUFFER);
                         while (it.hasNext())
 						{
                         	if (_routeSet == null)
                         		_routeSet = new LinkedList();
                         	_routeSet.addLast(it.next().toString());
 						}
                         //_upstreamId = response.getDialogId(true);
                     } 
                 }
                 else if (_state == State.EARLY)
                 {
                     if (200 <= status && status < 300)
                     	setState(State.CONFIRMED);
                     else if (status >= 300)
                     	setState(State.TERMINATED);
                 }
             }
         }
         else if (response.isBye())
 			setState(State.TERMINATED);
         
         if (response.needsContact()) 
         {
             Address contact = getServer().getTransportManager().getContact(
                     SipConnectors.getOrdinal(SipConnectors.TCP));
             response.setContact(contact);
         }
         
         if (response.isInvite())
         {
             long cseq = response.getCSeq().getNumber();
             
             if ((200 <= status) && (status < 300))
             {
                 ServerInvite invite = getServerInvite(cseq, true);
                 invite.set2xx(response);
             }
             else if ((100 < status) && (status < 200) && reliable)
             {
                 ServerInvite invite = getServerInvite(cseq, true);
                 
                 int rseq = _rseq++;
                 response.getFields().addString(SipHeaders.REQUIRE, SipParams.REL_100);
                 response.setRSeq(rseq);
                 
                 invite.addReliable1xx(response);
             }
             else if (status >= 300)
             {
                 removeServerInvite(cseq);
             }
         }
         
         if (response.isSendOutsideTx())
         	getServer().getTransportManager().send(response, (SipRequest) response.getRequest());
         else
         	tx.send(response);
 	}
 	
 	private ClientInvite getClientInvite(long cseq, boolean create)
 	{
 		for (int i = LazyList.size(_cinvites); i-->0;)
         {
             ClientInvite invite = (ClientInvite) LazyList.get(_cinvites, i);
             if (invite.getCSeq() == cseq)
                 return invite;
         }
         if (create)
         {
         	final ClientInvite invite = new ClientInvite(cseq);
             _cinvites = LazyList.add(_cinvites, invite);
             getCall().schedule(new Runnable() {
 
 				public void run()
 				{
 					_cinvites = LazyList.remove(_cinvites, invite);
 				}
 				
 			}, 64 * Transaction.__T1);
             return invite;     
         }
         return null;
 	}
 	
     private ServerInvite getServerInvite(long cseq, boolean create)
     {
         for (int i = LazyList.size(_invites); i-->0;)
         {
             ServerInvite invite = (ServerInvite) LazyList.get(_invites, i);
             if (invite.getCSeq() == cseq)
                 return invite;
         }
         if (create)
         {
             final ServerInvite invite = new ServerInvite(cseq);
             _invites = LazyList.add(_invites, invite);
             getCall().schedule(new Runnable() {
 
 				public void run()
 				{
 					_invites = LazyList.remove(_invites, invite);
 				}
 				
 			}, 64 * Transaction.__T1);
             return invite;     
         }
         return null;
     }
     
     private ServerInvite removeServerInvite(long cseq)
     {
         for (int i = LazyList.size(_invites); i-->0;)
         {
             ServerInvite invite = (ServerInvite) LazyList.get(_invites, i);
             if (invite.getCSeq() == cseq)
             {
                 _invites = LazyList.remove(_invites, i);
                 return invite;
             }
         }
         return null;
     }
     
 	private void access() 
     {
         _accessed = System.currentTimeMillis();
         _appSession.access(_accessed);
     }
 
 	public void setRole(Role newRole) 
     {
         if (_role != Role.UNDEFINED) 
             throw new IllegalStateException("Role is already defined " + this);
         
         _role = newRole;
         
         if (_role == Role.PROXY)
         {
         	NameAddr tmp = _remoteParty;
         	_remoteParty = _localParty;
         	_localParty = tmp;
         }
     }
 	
 	public void setState(State newState) 
     {
         if (Log.isDebugEnabled())
             Log.debug("{} -> {}", this, newState);
 		_state = newState;
 	}
 	
 	public String getRemoteTag()
 	{
 		return _remoteParty.getParameter(SipParams.TAG);
 	}
 	
 	public String getLocalTag()
 	{
 		return _localParty.getParameter(SipParams.TAG);
 	}
 
 	public SipRequest createRequest(String method, long cseq) 
     {
 		SipRequest request = new SipRequest();
 		request.setSession(this);
 		request.setMethod(method);
         request.getFields().setAddress(SipHeaders.FROM, (NameAddr) _localParty.clone()); // TODO clone ?
         request.getFields().setAddress(SipHeaders.TO, (NameAddr) _remoteParty.clone());
 		
 		if (_remoteTarget != null) 
 			request.setRequestURI((URI) _remoteTarget.clone());
 		else 
 			request.setRequestURI(request.getTo().getURI());
 		
 		if (_routeSet != null)
 		{
 			Iterator it = _routeSet.iterator();
 			while (it.hasNext())
 				request.getFields().addString(SipHeaders.ROUTE, it.next().toString());	
 		}
 		
 		request.getFields().setString(SipHeaders.CALL_ID_BUFFER, _callId);
 		request.getFields().setString(SipHeaders.CSEQ_BUFFER, cseq + " " + method); // TODO buf
         request.getFields().setString(SipHeaders.MAX_FORWARDS, "70");
 		// TODO
         
         if (request.needsContact())
             request.getFields().setAddress(SipHeaders.CONTACT, (NameAddr) getContact());
 		
 		return request;
     }
 	
 	public void handleRequest(SipRequest request) throws SipException
     { 
 		if (request.isInitial())
 		{
 			_localParty = (NameAddr) request.to().clone(); // TODO clone ?
 			_remoteParty = (NameAddr) request.from().clone();
 			
 			_callId = request.getCallId();
 		}    
 	
 		if (request.isInitial()) 
         {
 			if (Log.isDebugEnabled())
                 Log.debug("{} initial request {}", this, request);
 			
 			invokeServlet(request);
 		} 
         else 
         {
             if (Log.isDebugEnabled())
                 Log.debug("{} subsequent request {}", this, request);
             
 			access();
             
 			Proxy proxy = null;
 			
 			if (isUA()) 
             {
 				if (request.getCSeq().getNumber() <= _remoteCSeq &&
 						!request.isAck() && !request.isCancel()) 
                 {
                     throw new SipException(
                             SipServletResponse.SC_SERVER_INTERNAL_ERROR, 
                             "Out of order request");
 				}
 				_remoteCSeq = request.getCSeq().getNumber();
 				if (request.isInvite())  // TODO target refresh
 					setRemoteTarget(request);
                 
                 if (request.isPrack())
                 {
                     String s = request.getHeader(SipHeaders.RACK);
                     
                     if (s == null)
                     {
                         try 
                         {
                             request.createResponse(SipServletResponse.SC_BAD_REQUEST, 
                                     "Out of order request").send(); // TODO ex
                         } 
                         catch (Throwable _) { }
                         return;
                     }
                     
                     int index = s.indexOf(' ');
                     int rack = Integer.parseInt(s.substring(0, index));
                     
                     int index2 = s.indexOf(' ', index + 1);
                     int cseq = Integer.parseInt(s.substring(index + 1, index2));
 
                     ServerInvite invite = getServerInvite(cseq, false);
                     ReliableContext reliable = null;
                     if (invite != null)
                         reliable = invite.removeReliable(rack);
                     
                     if (reliable != null)
                         reliable.prack();
                     else 
                         throw new SipException(SipServletResponse.SC_CALL_LEG_DONE, "No matching provisional response");
                 }
                 else if (request.isAck())
                 {
                     ServerInvite invite = removeServerInvite(request.getCSeq().getNumber());
                     if (invite != null)
                         invite.ack();
                 }
 			} 
             else if (isProxy())
             {
 				try 
                 {
 					proxy = request.getProxy();
 				} 
                 catch (TooManyHopsException e) 
                 {
 					// TODO send sip ex
 					try 
                     {
 						request.createResponse(SipServletResponse.SC_TOO_MANY_HOPS).send();
 					} 
                     catch (Exception e2) 
                     {
 						Log.ignore(e2);
 					}
 				}
 			}
 			
 			invokeServlet(request);
 			
 			if (proxy != null && !request.isCancel()) // TODO CANCEL ???
 				proxy.proxyTo(request.getRequestURI());
 		}
 	}
 	
     public void handleCancel(ServerTransaction tx, SipRequest cancel)
     {
     	try 
     	{
     		// On CANCEL reception, the session is in the state "ready to invalidate" before 
     		// invoking servlet. Set _invokingServlet to true here prevent invalidation too early.
     		_invokingServlet = true;
 	        cancel.setSession(this);
 	        synchronized (tx) 
 	        {
 	            if (tx.getState() > Transaction.STATE_PROCEEDING) 
 	            {
 	                Log.debug("Late CANCEL, ignoring"); // TODO invoke ?
 	            } 
 	            else 
 	            {
 	                try 
 	                {
 	                    tx.getRequest().createResponse(SipServletResponse.SC_REQUEST_TERMINATED).send();
 	                } 
 	                catch (Exception e) 
 	                {
 	                    Log.debug("Failed to cancel request", e);
 	                }
 	                setState(State.TERMINATED);
 	            }
 	        }
 	        
 	        try 
 	        {
 	            invokeServlet(cancel);
 	        } 
 	        catch (Exception e)
 	        {
 	            Log.debug(e);
 	        }
     	}
     	finally
     	{
     		_invokingServlet = false;
     	}
     } 
 
 	public void handleResponse(SipResponse response) 
     {
         response.setSession(this); 
         access();
         
         int status = response.getStatus();
         
         if (status == 100) return;
         
         SipRequest request = (SipRequest) response.getRequest();
         
         if (request != null && (request.isInvite() || request.isSubscribe()))
         {
             if (_state == State.INITIAL)
             {
                 if (status < 200)
                 	setState(State.EARLY);
                 else if (status < 300)
                 	setState(State.CONFIRMED);
                 else if (status >= 300) 
                 {
                 	setState(State.INITIAL);
                     _remoteTarget = _remoteParty.getURI();
                     _remoteParty.setParameter(SipParams.TAG, null);
                     _remoteCSeq = -1;
                     _routeSet = null;
                     _secure = false;
                 }
                 
                 if (_state == State.EARLY || _state == State.CONFIRMED)
                 {
                     String tag = response.getTo().getParameter(SipParams.TAG);
                     if (tag == null); // TODO throw
                     
                     _remoteParty.setParameter(SipParams.TAG, tag);
                     //_downstreamId = response.getDialogId(false);
                     
                     ListIterator it = response.getFields().getValues(SipHeaders.RECORD_ROUTE_BUFFER);          
                     while (it.hasNext())
 					{
                     	if (_routeSet == null)
                     		_routeSet = new LinkedList();
                     	_routeSet.addFirst(it.next().toString());
                     }
                 }
             }
             else if (_state == State.EARLY)
             {
                 if (200 <= status && status < 300)
                 	setState(State.CONFIRMED);
                 else if (status >= 300) 
                 {
                 	setState(State.INITIAL);
                     _remoteTarget = _remoteParty.getURI();
                     _remoteParty.setParameter(SipParams.TAG, null);
                     _remoteCSeq = -1;
                     _routeSet = null;
                     _secure = false;
 
                     setMayReadyToInvalidate(true);
                 }
             }
         }
         
         if (status < 300 && (response.isInvite() || response.isSubscribe())) // TODO check
             setRemoteTarget(response);
                 
         if (response.isInvite() && (status >= 200 && status < 300))
         { // TODO on top ?
         	long cseq = response.getCSeq().getNumber();
         	ClientInvite invite = getClientInvite(cseq, false);
         	if (invite == null)
         	{
         		invite = getClientInvite(cseq, true);
         		invite.set2xx(response);
         	}
         	else
         	{
         	//System.out.println("INVITE client: " + invite);
 	        	if (invite.getAck() != null)
 	        	{
 	        		try 
 	        		{
 	        			ClientTransaction ct = invite.getAck();
 	        			getServer().getTransportManager().send(ct.getRequest(), ct.getTransport(), ct.getAddress(), ct.getPort());
 	        		}
 	        		catch (Exception e)
 	        		{
 	        			Log.warn(e);
 	        		}
 	        	}
 	        	return;
         	}
         }
         else if (!response.isReliable1xx())
         	response.setCommitted(true);
         
         if (response.isBye())
 			setState(State.TERMINATED);
         
         if (isValid())
         	invokeServlet(response);
 	}
 	
 	protected void setRemoteTarget(SipMessage message) 
     {
 		try 
         {
 			Iterator it = message.getAddressHeaders(SipHeaders.CONTACT);
 			if (!it.hasNext()) 
 				throw new IllegalArgumentException("No Contact"); // TODO ?? BadRequest
 			
 			Address contact = (Address) it.next();
 			
 			if (it.hasNext()) 
 				throw new IllegalArgumentException("Multiple Contact"); // TODO BadRequest
 			
 			_remoteTarget = contact.getURI();			
 		} 
         catch (ServletParseException e) 
         {
 			throw new IllegalArgumentException("Invalid Contact " + e.getMessage());
 		}
 	}
 
     public void invokeServlet(SipResponse response)
     {
         try 
         {
         	_invokingServlet = true;
             //getServer().handle(response);
         	_appSession.getContext().handle(response);
         } 
         catch (Exception e)
         {
             Log.debug(e);
         }
         finally
         {
         	_invokingServlet = false;
             checkReadyToInvalidate();
         }
 
     }
 	
 	public void invokeServlet(SipRequest request) throws SipException
     { 
 		try 
         {
			_appSession.getContext().handle(request);
 			_invokingServlet = true;
 			//getServer().handle(request);
 		} 
         catch (TooManyHopsException e) 
         {
             throw new SipException(SipServletResponse.SC_TOO_MANY_HOPS, e);
 		} 
         catch (Throwable t) 
         {
             throw new SipException(SipServletResponse.SC_SERVER_INTERNAL_ERROR, t);
 		}
         finally
         {
         	_invokingServlet = false;
             checkReadyToInvalidate();
         }
 	}
 	
 	public SipServletHolder getHandler() 
     {
 		if (_handler == null && _handlerName != null)
 			_handler = findHandler(_handlerName);
 		return _handler;
 	}
 	
 	public void setHandler(SipServletHolder handler) 
     {
 		this._handler = handler;
 		_handlerName = _handler.getName();
 	}
 	
 	public AppSession appSession()
 	{
 		return _appSession;
 	}
 	
 	public Server getServer()
 	{
 		return _appSession.getCall().getServer();
 	}
 	
 	public String toString() 
     {
 		return _id + "/" + _role + "/" + _state;
 	}
     
 	public boolean equals(Object o)
 	{
 		if (o == null || !(o instanceof SessionIf))
 			return false;
 		return _id.equals(((SessionIf) o).getId());
 	}
 	
 	public List<SipServletResponse> getUncommitted200(UAMode mode)
 	{
 		List<SipServletResponse> list = null;
 		if (mode == UAMode.UAS)
 		{
 			for (int i = LazyList.size(_invites); i-->0;)
 	        {
 	            ServerInvite invite = (ServerInvite) LazyList.get(_invites, i);
 	            if (!invite._2xx.isCommitted())
 	            {
 	            	if (list == null)
 	            		list = new ArrayList<SipServletResponse>();
 	            	list.add(invite._2xx);
 	            }
 	        }
 		}
 		else
 		{
 			for (int i = LazyList.size(_cinvites); i-->0;)
 	        {
 	            ClientInvite invite = (ClientInvite) LazyList.get(_cinvites, i);
 	            if (invite._2xx != null && !invite._2xx.isCommitted())
 	            {
 	            	if (list == null)
 	            		list = new ArrayList<SipServletResponse>();
 	            	list.add(invite._2xx);
 	            }
 	        }
 		}
 
         return list;
 	}
 	
 
 	public void setSubscriberURI(URI uri)
 	{
 		_subscriberURI = uri;
 	}
 
 	public void setRegion(SipApplicationRoutingRegion region)
 	{
 		_region = region;
 	}
 	
 	public boolean isReadyToInvalidate(boolean checkLinkedSession)
 	{
 		checkValid();
 
 		Session linkedSession = getLinkedSession();
 		if ((_state == State.TERMINATED || _mayReadyToInvalidate) 
 				&& !getCall().hasRunningTransactions(this)
 				&& (!checkLinkedSession || linkedSession == null || linkedSession.isReadyToInvalidate(false)))
 			return true;
 		
 		return false;
 	}
 
 	public void checkReadyToInvalidate()
 	{
 		if (isValid() && _invalidateWhenReady && isReadyToInvalidate() && !_invokingServlet)
 		{
 			Session linkedSession = getLinkedSession();
 			
 			SipSessionListener[] listeners = appSession().getContext().getSipSessionListeners();
 			if (listeners.length > 0)
 				appSession().fireEvent(listeners, AppSession.__sessionReadyToInvalidate, new SipSessionEvent(this));
 			
 			if (_invalidateWhenReady && isValid())
 				invalidate();
 			
 			if (linkedSession != null)
 				linkedSession.checkReadyToInvalidate();
 		}
 	}
 	
 	public void setMayReadyToInvalidate(boolean ready)
 	{
 		_mayReadyToInvalidate = ready;
 	}
 	
 	public Session getLinkedSession()
 	{
 		if (_linkedSessionId == null)
 			return null;
 		return (Session) _appSession.getSipSession(_linkedSessionId);
 	}
 
 	public void setLinkedSession(SessionIf linkedSession)
 	{
 		_linkedSessionId = linkedSession == null ? null : linkedSession.getId();
 	}
 
 	public NameAddr localParty()
 	{
 		return _localParty;
 	}
 	
 	public void registerProxy(SipResponse response)
 	{
 		_remoteParty = (NameAddr) response.to().clone(); // TODO clone()?
 	}
 	
 	public NameAddr remoteParty()
 	{
 		return _remoteParty;
 	}
 	
 	public Session getSession()  
 	{
 		return this;
 	}
 	
 	@Override
 	public Session clone()
 	{
 		Session clone;
 		try
 		{
 			clone = (Session) super.clone();
 		}
 		catch (CloneNotSupportedException e)
 		{
 			throw new RuntimeException(e);
 		}
 		clone._localParty.removeParameter(SipParams.TAG);
 		clone._id = null;
 		return clone;
 	}
 	
 	class ClientInvite implements Serializable
 	{
 
 		private ClientTransaction _ack;
 
 		private SipResponse _2xx;
 
 		
 		private long _cseq = -1;
 		
 		public ClientInvite(long cseq)
 		{
 			_cseq = cseq;
 		}
 		
 		public long getCSeq()
 		{
 			return _cseq;
 		}
 		
 		public ClientTransaction getAck()
 		{
 			return _ack;
 		}
 
 				
 		public void setAck(ClientTransaction ack)
 		{
 			_ack = ack;
 		}
 		
 		public void set2xx(SipResponse response)
 		{
 			_2xx = response;
 		}
 		
 		public SipServletResponse get2xx()
 		{
 			return _2xx;
 		}
 		
 	}
 	
     class ServerInvite implements Serializable
     {
         private long _cseq = -1;
         private Object _reliables;
         
         private SipResponse _2xx;
         
         private long _timerValue = Transaction.__T1;
         private long _startRetransmit;
         
         private TimerTask _2xxTimer;
         
         class ReliableContext implements Serializable
         {
             private SipResponse _response;
             private long _timerValue = Transaction.__T1;
             
             private TimerTask _retransTimer;
             private TimerTask _prackTimer;
             
             public ReliableContext(SipResponse response)
             {
                 _response = response;
             }
             
             public void startTimer()
             {
                 _retransTimer = getCall().schedule(new RetransReliableTask(this), _timerValue);
                 _prackTimer = getCall().schedule(new WaitPrackTask(this), 64 * Transaction.__T1);
             }
             
             public void prack()
             {
                stopRetrans();
                if (_prackTimer != null)
                    getCall().cancel(_prackTimer);
             }
             
             public void stopRetrans()
             {
                 _response = null;
                 if (_retransTimer != null)
                 	getCall().cancel(_retransTimer);
             }
             
             public void noPrack()
             {
                 if (_retransTimer != null)
                     getCall().cancel(_retransTimer);
                 
                 if (_response != null)
                 {
                     removeReliable(_response.getRSeq());             
                     _appSession.noPrack(_response.getRequest(), _response);
                     _response = null;
                 }
             }
             
             public void retransmit()
             {
                 System.out.println(new Date() + "  >>timeout");
                 if (_response != null)
                 {
                     ServerTransaction stx = (ServerTransaction) _response.getTransaction();
                     if (stx.getState() == Transaction.STATE_PROCEEDING)
                     {
                         stx.send(_response);
                         _timerValue = _timerValue * 2;
                         
                         _retransTimer = getCall().schedule(new RetransReliableTask(this), _timerValue);
                         System.out.println("next in " + _timerValue);
                     }
                 }
             }
         }
         
         public ServerInvite(long cseq)
         {
             _cseq = cseq;
         }
         
         public long getCSeq()
         {
             return _cseq;
         }
         
         public void addReliable1xx(SipResponse response)
         {
             ReliableContext reliable = new ReliableContext(response);
             _reliables = LazyList.add(_reliables, reliable);
             reliable.startTimer();
         }
         
         public void set2xx(SipResponse response)
         {
             _2xx = response;
             
             for (int i = LazyList.size(_reliables); i-->0;)
             {
                 ReliableContext reliable = (ReliableContext) LazyList.get(_reliables, i);
                 reliable.stopRetrans();
             }
             
             _startRetransmit = System.currentTimeMillis();           
             _2xxTimer = getCall().schedule(new Retrans2xxTask(this), _timerValue);
         }
         
         public void retransmit2xx()
         {
             if (_2xx != null)
             {
                 long now = System.currentTimeMillis();
                 if (now - _startRetransmit >= 64 * Transaction.__T1)
                 {
                     removeServerInvite(_cseq);
                     _appSession.noAck(_2xx.getRequest(), _2xx);
                 }
                 else 
                 {
                     try 
                     {
                         getServer().getTransportManager().send(_2xx, (SipRequest) _2xx.getRequest());
                     }
                     catch (Exception e)
                     {
                         Log.warn(e);
                     }
                     _timerValue = Math.min(_timerValue * 2, Transaction.__T2);
                     _timerValue = Math.min(_timerValue, _startRetransmit + 64 * Transaction.__T1 - now);
                     
                     _2xxTimer = getCall().schedule(new Retrans2xxTask(this), _timerValue);
                 }
             }
         }
         
         public void ack()
         {
             _2xx = null;
             if (_2xxTimer != null)
             {
                 getCall().cancel(_2xxTimer);
                 _2xxTimer = null;
             }
         }
         
         public ReliableContext removeReliable(int rseq)
         {
             for (int i = LazyList.size(_reliables); i-->0;)
             {
                 ReliableContext reliable = (ReliableContext) LazyList.get(_reliables, i);
                 SipResponse response = reliable._response;
                 
                 if (response.getRSeq() == rseq)
                 {
                     _reliables = LazyList.remove(_reliables, i);
                     return reliable;
                 }
             }
             return null;
         }
     }
 
     class Retrans2xxTask implements Runnable, Serializable
     {
         private ServerInvite _invite;
          
         public Retrans2xxTask(ServerInvite invite)
         {
             _invite = invite;
         }
         
         public void run()
         {
             _invite.retransmit2xx();
         }
     }
 	
     class RetransReliableTask implements Runnable, Serializable
     {
         private ReliableContext _reliable;
         
         public RetransReliableTask(ReliableContext reliable)
         {
             _reliable = reliable;
         }
         
         public void run()
         {
             _reliable.retransmit();
         }
     }
     
     class WaitPrackTask implements Runnable, Serializable
     {
         private ReliableContext _reliable;
         
         public WaitPrackTask(ReliableContext reliable)
         {
             _reliable = reliable;
         }
         
         public void run()
         {
             _reliable.noPrack();
         }
     }
 
 
 	
 }
