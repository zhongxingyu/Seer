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
 import javax.servlet.sip.TooManyHopsException;
 import javax.servlet.sip.UAMode;
 import javax.servlet.sip.URI;
 import javax.servlet.sip.ar.SipApplicationRoutingRegion;
 
 import org.cipango.CallSession;
 import org.cipango.NameAddr;
 import org.cipango.Server;
 import org.cipango.SipException;
 import org.cipango.SipHeaders;
 import org.cipango.SipMessage;
 import org.cipango.SipMethods;
 import org.cipango.SipParams;
 import org.cipango.SipRequest;
 import org.cipango.SipResponse;
 import org.cipango.sip.ClientTransaction;
 import org.cipango.sip.ClientTransactionListener;
 import org.cipango.sip.ServerTransaction;
 import org.cipango.sip.ServerTransactionListener;
 import org.cipango.sip.SipConnectors;
 import org.cipango.sip.Transaction;
 import org.cipango.sipapp.SipAppContext;
 import org.cipango.util.ID;
 import org.cipango.util.ReadOnlyAddress;
 import org.cipango.util.TimerTask;
 import org.cipango.util.concurrent.AppSessionLockProxy;
 import org.mortbay.log.Log;
 import org.mortbay.util.LazyList;
 
 public class Session implements SessionIf, ClientTransactionListener, ServerTransactionListener, Cloneable
 {	    
 	public enum Role { UNDEFINED, UAC, UAS, PROXY };
 	
 	protected long _created = System.currentTimeMillis();
 	protected long _accessed;
 	protected String _id;
     
 	protected Role _role = Role.UNDEFINED;
 	protected State _state = State.INITIAL;
     
     private boolean _valid = true;
     protected boolean _invalidateWhenReady = true;
 
     protected String _callId;
     protected NameAddr _localParty;
     protected NameAddr _remoteParty;
 
     protected URI _remoteTarget;
     protected long _localCSeq = 1;
     protected long _remoteCSeq = -1;
     protected LinkedList _routeSet;
     protected boolean _secure = false;
     
     protected int _rseq = 0;
     
     private AppSession _appSession;
     protected String _linkedSessionId;
     private SipServletHolder _handler;
     
     protected URI _subscriberURI;
     protected SipApplicationRoutingRegion _region;
     
     protected Map<String, Object> _attributes;
     
     private Object _serverInvites;
     private Object _clientInvites;
     
 	public Session(AppSession appSession, String id)
     {
         _appSession = appSession;
         _id = id;
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
 			throw new IllegalArgumentException("Request method " + method + " is not allowed");
 		
 		// TODO throw java.lang.IllegalStateException - if this SipSession is in the INITIAL 
 		// state and there is an ongoing transaction 
 		if (_state == State.TERMINATED)
 			throw new IllegalStateException("Cannot create request in terminated state");
 		
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
 		
 		if (Log.isDebugEnabled())
 			Log.debug("invalidating SipSession: " + this);
 		
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
 		
 		SipAppContext context = _appSession.getContext();
 		SipServletHolder handler = context.getSipServletHandler().getHolder(name);
 		
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
 		checkValid();
 
 		if (_state == State.TERMINATED)
 			return true;
 		else if (isUA() && _state == State.INITIAL)
 			return !hasTransactions();
 
 		return false;
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
 	
 	public CallSession getCallSession()
     {
         return _appSession.getCallSession();
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
         Address addr = getServer().getConnectorManager().getContact(SipConnectors.TCP_ORDINAL); // TODO
         addr.getURI().setParameter(ID.APP_SESSION_ID_PARAMETER, _appSession.getAppId());
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
                 
                 int rseq = ++_rseq;
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
         	getServer().getConnectorManager().sendResponse(response);
         else
         	tx.send(response);
 	}
 	
 	private ClientInvite getClientInvite(long cseq, boolean create)
 	{
 		for (int i = LazyList.size(_clientInvites); i-->0;)
         {
             ClientInvite invite = (ClientInvite) LazyList.get(_clientInvites, i);
             if (invite.getCSeq() == cseq)
                 return invite;
         }
         if (create)
         {
         	final ClientInvite invite = new ClientInvite(cseq);
         	_clientInvites = LazyList.add(_clientInvites, invite);
             getCallSession().schedule(new Runnable() {
 
 				public void run()
 				{
 					_clientInvites = LazyList.remove(_clientInvites, invite);
 				}
 				
 			}, 64 * Transaction.__T1);
             return invite;     
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
 		access();
 		
 		if (request.isInitial())
 		{
 			_localParty = (NameAddr) request.to().clone(); // TODO clone ?
 			_remoteParty = (NameAddr) request.from().clone();
 			
 			_callId = request.getCallId();
 		}    
 	
 		if (request.isInitial()) 
         {
 			if (Log.isDebugEnabled())
                 Log.debug("initial request {} for session {}", request.getRequestLine(), this);
 			
 			invokeServlet(request);
 		} 
         else 
         {
             if (Log.isDebugEnabled())
                 Log.debug("subsequent request {} for session {}", request.getRequestLine(), this);
             
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
                 {/*
                     String s = request.getHeader(SipHeaders.RACK);
                     
                     if (s == null)
                     {
                         try 
                         {
                             request.createResponse(SipServletResponse.SC_BAD_REQUEST, "Missing Rack header").send(); // TODO ex
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
                		*/
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
 	        			getServer().getConnectorManager().send(ct.getRequest(), ct.getConnection());
 	        		}
 	        		catch (Exception e)
 	        		{
 	        			Log.warn(e);
 	        		}
 	        	}
 	        	return;
         	}
         }
         else if (response.isReliable1xx())
         {
         	int rseq = response.getRSeq();
         	if (_rseq == 0)
         		_rseq = rseq;
         	else if (_rseq + 1 != rseq)
         	{
         		Log.debug("Ignore reliable provisional response: RSeq is {} when expect {}", rseq , _rseq + 1);
         		return;
         	}
         	else
         		_rseq = rseq;
         }
         else
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
         	_appSession.getContext().handle(response);
         } 
         catch (Exception e)
         {
             Log.debug(e);
         }
     }
 	
 	public void invokeServlet(SipRequest request) throws SipException
     { 
 		try 
         {
 			_appSession.getContext().handle(request);
 		} 
         catch (TooManyHopsException e) 
         {
             throw new SipException(SipServletResponse.SC_TOO_MANY_HOPS, e);
 		} 
         catch (Throwable t) 
         {
             throw new SipException(SipServletResponse.SC_SERVER_INTERNAL_ERROR, t);
 		}
 	}
 	
 	public SipServletHolder getHandler() 
     {
 		return _handler;
 	}
 	
 	public void setHandler(SipServletHolder handler) 
     {
 		_handler = handler;
 	}
 	
 	public AppSession appSession()
 	{
 		return _appSession;
 	}
 	
 	public Server getServer()
 	{
 		return _appSession.getCallSession().getServer();
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
 			for (int i = LazyList.size(_serverInvites); i-->0;)
 	        {
 	            ServerInvite invite = (ServerInvite) LazyList.get(_serverInvites, i);
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
 			for (int i = LazyList.size(_clientInvites); i-->0;)
 	        {
 	            ClientInvite invite = (ClientInvite) LazyList.get(_clientInvites, i);
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
 	
 	public boolean hasTransactions()
 	{
 		return getCallSession().hasActiveTransactions(this);
 	}
 
 	public void invalidateIfReady()
 	{
		if (getInvalidateWhenReady() && isValid() && isReadyToInvalidate() && (getLastAccessedTime() > 0))
 		{			
 			SipAppContext context = _appSession.getContext();
 			SipSessionListener[] listeners = context.getSipSessionListeners();
 			if (listeners.length > 0)
 				context.fire(listeners, AppSession.__sessionReadyToInvalidate, new SipSessionEvent(this));
 			
 			if (isValid() && getInvalidateWhenReady())
 				invalidate();
 		}
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
 	
 	 private ServerInvite getServerInvite(long cseq, boolean create)
 	    {
 	    	for (int i = 0; i < LazyList.size(_serverInvites); i++)
 	    	{
 	    		ServerInvite invite = (ServerInvite) LazyList.get(_serverInvites, i);
 	    		if (invite.getCSeq() == cseq)
 	    			return invite;
 	    	}
 	    	if (create)
 	    	{
 	    		ServerInvite invite = new ServerInvite(cseq);
 	    		_serverInvites = LazyList.add(_serverInvites, invite);
 	    		
 	    		if (Log.isDebugEnabled())
 	    			Log.debug("added server invite context for cseq: " + cseq);
 	    		return invite;
 	    	}
 	    	return null;    
 	    }
 	    
 	    private ServerInvite removeServerInvite(long cseq)
 	    {
 	        for (int i = LazyList.size(_serverInvites); i-->0;)
 	        {
 	            ServerInvite invite = (ServerInvite) LazyList.get(_serverInvites, i);
 	            if (invite.getCSeq() == cseq)
 	            {
 	            	_serverInvites = LazyList.remove(_serverInvites, i);
 	            	
 	            	if (Log.isDebugEnabled())
 	            		Log.debug("removed server invite context for cseq: " + cseq);
 	                return invite;
 	            }
 	        }
 	        return null;
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
 	
 	class ServerInvite
 	{
 		private static final int TIMER_RETRANS_2XX = 0;
 		private static final int TIMER_WAIT_ACK = 1;
 			    
 	    private long _cseq;
 		private SipResponse _2xx;
 		private Object _reliable1xxs;
 		
         private long _timer2xxValue = Transaction.__T1;
 
 		private TimerTask[] _timers = new TimerTask[2];
 		
 		public ServerInvite(long cseq)
 		{
 			_cseq = cseq;
 		}
 		
 		public void set2xx(SipResponse response)
 		{
 			_2xx = response;
 			
 			for (int i = 0; i < LazyList.size(_reliable1xxs); i++)
 			{
 				Reliable1xx reliable1xx = (Reliable1xx) LazyList.get(_reliable1xxs, i);
 				reliable1xx.stopRetrans();
 				
 			}
 			_timers[TIMER_RETRANS_2XX] = getCallSession().schedule(new Timer(TIMER_RETRANS_2XX), _timer2xxValue);
 			_timers[TIMER_WAIT_ACK] = getCallSession().schedule(new Timer(TIMER_WAIT_ACK), 64 * Transaction.__T1);
 		}
 		
 		public void ack()
         {
             _2xx = null;
             cancelTimer(TIMER_RETRANS_2XX);
             cancelTimer(TIMER_WAIT_ACK);
         }
 		
 		protected void cancelTimer(int id)
 		{
 			TimerTask timer = _timers[id];
             if (timer != null)
             	getCallSession().cancel(timer);
             _timers[id] = null;
 		}
 		
 		public void addReliable1xx(SipResponse response)
 		{
 			Reliable1xx reliable1xx = new Reliable1xx(response);
 			_reliable1xxs = LazyList.add(_reliable1xxs, reliable1xx);
 			reliable1xx.start();
 		}
 		
 		public void removeReliable1xx(Reliable1xx reliable1xx)
 		{
 			_reliable1xxs = LazyList.remove(_reliable1xxs, reliable1xx);
 		}
 		
 		public long getCSeq()
 		{
 			return _cseq;
 		}
 		
 		public void timeout(int id)
 		{
 			switch (id) 
 			{
 			case TIMER_RETRANS_2XX:
 				if (_2xx != null)
 				{
 					try
 					{
 						getServer().getConnectorManager().sendResponse(_2xx);
 					}
 					catch (Exception e)
 					{
 						Log.debug(e);
 					}
 					_timer2xxValue = Math.min(_timer2xxValue*2, Transaction.__T2);
 					_timers[TIMER_RETRANS_2XX] = getCallSession().schedule(new Timer(TIMER_RETRANS_2XX), _timer2xxValue);
 				}
 				break;
 				
 			case TIMER_WAIT_ACK:
 				cancelTimer(TIMER_RETRANS_2XX);
 				// remove server invite
 				_appSession.noAck(_2xx.getRequest(), _2xx);
 				break;
 			default:
 				throw new IllegalStateException("unknown timer " + id);
 			}
 		}
 		
 		class Timer implements Runnable
 		{
 			private int _id;
 			
 			public Timer(int id) { _id = id; }
 			public void run() { timeout(_id); }
 			public String toString() { return _id == TIMER_RETRANS_2XX ? "retrans-2xx" : "wait-ack"; }
 		}
 		
 		class Reliable1xx
 		{
 			private static final int TIMER_RETRANS_1XX = 0;
 			private static final int TIMER_WAIT_PRACK = 1;
 						
 			private TimerTask[] _timers = new TimerTask[2];
 			
 			private SipResponse _1xx;
 			private long _1xxRetransDelay = Transaction.__T1;
 			
 			public Reliable1xx(SipResponse response)
 			{
 				_1xx = response;
 			}
 			
 			public void start()
 			{
 				_timers[TIMER_RETRANS_1XX] = getCallSession().schedule(new Timer(TIMER_RETRANS_1XX), _1xxRetransDelay);
 				_timers[TIMER_WAIT_PRACK] = getCallSession().schedule(new Timer(TIMER_WAIT_PRACK), 64 * Transaction.__T1);
 			}
 			
 			public void stopRetrans()
 			{
 				cancelTimer(TIMER_RETRANS_1XX);
 			}
 			
 			public void prack()
 			{
 				_1xx = null;
 				cancelTimer(TIMER_RETRANS_1XX);
 				cancelTimer(TIMER_WAIT_PRACK);
 			}
 			
 			protected void cancelTimer(int id)
 			{
 				TimerTask timer = _timers[id];
 	            if (timer != null)
 	            	getCallSession().cancel(timer);
 	            _timers[id] = null;
 			}
 			
 			protected void timeout(int id)
 			{
 				switch (id)
 				{
 				case TIMER_RETRANS_1XX:
 					if (_1xx != null)
 					{
 						ServerTransaction transaction = (ServerTransaction) _1xx.getTransaction();
 						if (transaction.getState() == Transaction.STATE_PROCEEDING)
 						{
 							transaction.send(_1xx);
 							_1xxRetransDelay = _1xxRetransDelay * 2;
 							_timers[TIMER_RETRANS_1XX] = getCallSession().schedule(new Timer(TIMER_RETRANS_1XX), _1xxRetransDelay);
 						}
 					}
 					break;
 				case TIMER_WAIT_PRACK:
 					cancelTimer(TIMER_WAIT_PRACK);
 					if (_1xx != null)
 					{
 						// remove reliable
 						_appSession.noPrack(_1xx.getRequest(), _1xx);
 						_1xx = null;
 					}
 				}
 			}
 			
 			class Timer implements Runnable
 			{
 				private int _id;
 				
 				public Timer(int id) { _id = id; }
 				public void run() { timeout(_id); }
 				public String toString() { return _id == TIMER_RETRANS_1XX ? "retrans-1xx" : "wait-prack"; }
 			}
 		}
 	}
 }
