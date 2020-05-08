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
 
 package org.cipango.server.session;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.sip.Address;
 import javax.servlet.sip.Proxy;
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
 
 import org.cipango.server.ID;
 import org.cipango.server.Server;
 import org.cipango.server.SipConnectors;
 import org.cipango.server.SipMessage;
 import org.cipango.server.SipRequest;
 import org.cipango.server.SipResponse;
 import org.cipango.server.session.scope.ScopedAppSession;
 import org.cipango.server.transaction.ClientTransaction;
 import org.cipango.server.transaction.ClientTransactionListener;
 import org.cipango.server.transaction.ServerTransaction;
 import org.cipango.server.transaction.ServerTransactionListener;
 import org.cipango.server.transaction.Transaction;
 import org.cipango.servlet.SipServletHolder;
 import org.cipango.sip.NameAddr;
 import org.cipango.sip.RAck;
 import org.cipango.sip.SipException;
 import org.cipango.sip.SipFields;
 import org.cipango.sip.SipHeaders;
 import org.cipango.sip.SipMethods;
 import org.cipango.sip.SipParams;
 import org.cipango.sipapp.SipAppContext;
 import org.cipango.util.ReadOnlyAddress;
 
 import org.cipango.util.TimerTask;
 
 import org.eclipse.jetty.util.LazyList;
 import org.eclipse.jetty.util.log.Log;
 
 public class Session implements SessionIf
 {
 	protected String _id;
 	private AppSession _appSession;
 	protected boolean _invalidateWhenReady = true;
 	
 	protected State _state = State.INITIAL;
 	private boolean _valid = true;
 	
 	protected long _created = System.currentTimeMillis();
 	protected long _lastAccessed;
 	
 	private SipServletHolder _handler;
 	protected SipApplicationRoutingRegion _region;
 	protected URI _subscriberURI;
 	
 	protected Map<String, Object> _attributes;
 	
 	protected String _callId;
 	protected NameAddr _localParty;
 	protected NameAddr _remoteParty;
 	
 	public enum Role { UNDEFINED, UAC, UAS, PROXY };
 	
 	protected Role _role = Role.UNDEFINED;
 	protected UA _ua;
 	
 	protected String _linkedSessionId;
 	
 	public Session(AppSession appSession, String id)
 	{
 		_appSession = appSession;
 		_id = id;
 	}
 	
 	public Session(AppSession appSession, String id, String callId, NameAddr local, NameAddr remote)
 	{
 		this(appSession, id);
 		
 		_callId = callId;
 		_localParty = local;
 		_remoteParty = remote;
 	}
 		
 	public Session(String id, Session other)
 	{
 		this(other._appSession, id);
 		_invalidateWhenReady = other._invalidateWhenReady;
 		_handler = other._handler;
 		
 		_callId = other._callId;
 		_localParty = (NameAddr) other._localParty.clone();
 		_remoteParty = (NameAddr) other._remoteParty.clone();
 		
 		_role = other._role;
 		
 		if (_role == Role.UAS)
 			_localParty.removeParameter(SipParams.TAG);
 		else
 			_remoteParty.removeParameter(SipParams.TAG);
 		
 		if (other._ua != null)
 			_ua = new UA(other._ua);
 		
 		if (other._attributes != null)
 		{
 			_attributes = newAttributeMap();
 			_attributes.putAll(other._attributes);
 		}
 	}
 	
 	/**
 	 * @see SessionIf#getSession()
 	 */
 	public Session getSession() 
 	{
 		return this;
 	}
 
 	/**
 	 * @see SipSession#createRequest(java.lang.String)
 	 */
 	public SipServletRequest createRequest(String method) 
 	{
 		checkValid();
 		if (!isUA())
 			throw new IllegalStateException("session is " + _role);
 	
 		return _ua.createRequest(method);
 	}
 
 	/**
 	 * @see SipSession#getApplicationSession()
 	 */
 	public SipApplicationSession getApplicationSession() 
 	{
 		return new ScopedAppSession(_appSession);
 	}
 
 	/**
 	 * @see SipSession#getAttribute(java.lang.String)
 	 */
 	public Object getAttribute(String name) 
 	{
 		checkValid();
 		if (name == null)
 			throw new NullPointerException("Attribute name is null");
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
 		List<String> names;
 		if (_attributes == null)
 			names = Collections.emptyList();
 		else
 			names = new ArrayList<String>(_attributes.keySet());
 		return Collections.enumeration(names);
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
 	 * @see SipSession#getInvalidateWhenReady()
 	 */
 	public boolean getInvalidateWhenReady() 
 	{
 		checkValid();
 		return _invalidateWhenReady;
 	}
 
 	/**
 	 * @see SipSession#getLastAccessedTime()
 	 */
 	public long getLastAccessedTime() 
 	{
 		return _lastAccessed;
 	}
 
 	/**
 	 * @see SipSession#getLocalParty()
 	 */
 	public Address getLocalParty() 
 	{
 		return new ReadOnlyAddress(_localParty);
 	}
 
 	/**
 	 * @see SipSession#getRegion()
 	 */
 	public SipApplicationRoutingRegion getRegion() 
 	{
 		checkValid();
 		return _region;
 	}
 
 	/**
 	 * @see SipSession#getRemoteParty()
 	 */
 	public Address getRemoteParty() 
 	{
 		return new ReadOnlyAddress(_remoteParty);
 	}
 
 	/**
 	 * @see SipSession#getServletContext()
 	 */
 	public ServletContext getServletContext() 
 	{
 		return _appSession.getContext().getServletContext();
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
 	 * @see SipSession#getSubscriberURI()
 	 */
 	public URI getSubscriberURI() 
 	{
 		checkValid();
 		return _subscriberURI;
 	}
 
 	/**
 	 * @see SipSession#invalidate()
 	 */
 	public void invalidate() 
 	{
 		checkValid();
 		
 		if (Log.isDebugEnabled())
 			Log.debug("invalidating SipSession " + this);
 		
 		_valid = false;
 		_appSession.removeSession(this);
 	}
 
 	/**
 	 * @see SipSession#isReadyToInvalidate()
 	 */
 	public boolean isReadyToInvalidate() 
 	{
 		checkValid();
 		
 		if (_lastAccessed == 0)	
 			return false;
 		
 		if (_state == State.TERMINATED)
 			return true;
 		else if (isUA() && _state == State.INITIAL)
 			return !hasTransactions();
 		
 		// TODO proxy
 		
 		return false;
 	}
 
 	/**
 	 * @see SipSession#isValid()
 	 */
 	public boolean isValid() 
 	{
 		return _valid;
 	}
 
 	/**
 	 * @see SipSession#removeAttribute(String)
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
 				SipSessionBindingEvent e = new SipSessionBindingEvent(this, name);
 				for (SipSessionAttributeListener listener : listeners)
 					listener.attributeRemoved(e);
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
 			throw new NullPointerException("name or value is null");
 		
 		if (_attributes == null)
 			_attributes = newAttributeMap();
 		
 		Object oldValue = _attributes.put(name, value);
 		
 		if (oldValue == null || !value.equals(oldValue))
 		{
 			unbindValue(name, oldValue);
 			bindValue(name, value);
 			
 			SipSessionAttributeListener[] listeners = _appSession.getContext().getSessionAttributeListeners();
 			if (listeners.length > 0)
 			{
 				SipSessionBindingEvent e = new SipSessionBindingEvent(this, name);
 				for (SipSessionAttributeListener listener : listeners)
 				{
 					if (oldValue == null)
 						listener.attributeAdded(e);
 					else
 						listener.attributeReplaced(e);
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
 	 * @see SipSession#setInvalidateWhenReady(boolean)
 	 */
 	public void setInvalidateWhenReady(boolean b) 
 	{
 		checkValid();
 		_invalidateWhenReady = b;
 	}
 
 	/**
 	 * @see SipSession#setOutboundInterface(InetSocketAddress)
 	 */
 	public void setOutboundInterface(InetSocketAddress address) 
 	{
 		checkValid();
 		if (address == null)
 			throw new NullPointerException("Null address");
 	}
 
 	/**
 	 * @see SipSession#setOutboundInterface(InetAddress)
 	 */
 	public void setOutboundInterface(InetAddress address) 
 	{
 		checkValid();
 		if (address == null)
 			throw new NullPointerException("Null address");
 	}
 	
 	// =====
 	
 	public void sendResponse(SipResponse response, ServerTransaction tx, boolean reliable) throws IOException
     {
 		if (_role == Role.UNDEFINED)
 			createUA(UAMode.UAS);
 		
 		if (isUA())
 			_ua.sendResponse(response, reliable);
 		else
 			sendVirtual(response);
     }
 	
 	public void sendVirtual(SipResponse response) throws IOException
 	{
 		// TODO to be completed
 		_role = Role.UAS;
 		_ua = new UA();
 		
 		NameAddr tmp = _remoteParty;
 		_remoteParty = _localParty;
 		_localParty = tmp;
 		
 		_ua.sendResponse(response, false);
 	}
 	
 	// =====
 	
 	public void handleRequest(SipRequest request) throws SipException, IOException
 	{
 		accessed();
 		
 		Proxy proxy = null;
 		
 		if (request.isInitial())
 		{
 			if (Log.isDebugEnabled())
 				Log.debug("initial request {} for session {}", request.getRequestLine(), this);
 			
 			_localParty = (NameAddr) request.to().clone();
 			_remoteParty = (NameAddr) request.from().clone();
 			_callId = request.getCallId();
 		}
 		else
 		{
 			if (Log.isDebugEnabled())
 				Log.debug("subsequent request {} for session {}", request.getRequestLine(), this);
 			
 			if (isUA())
 			{
 				_ua.handleRequest(request);
 				if (request.isHandled())
 					return;
 			}
 			else if (isProxy())
 			{
 				try
 				{
 					proxy = request.getProxy();
 				}
 				catch (TooManyHopsException e)
 				{
 					throw new SipException(SipServletResponse.SC_TOO_MANY_HOPS);
 				}
 			}
 		}
 		invokeServlet(request);
 	
 		if (proxy != null && !request.isCancel())
 			proxy.proxyTo(request.getRequestURI());	
 	}
 	
 	public ClientTransaction sendRequest(SipRequest request, ClientTransactionListener listener) throws IOException
 	{
 		accessed();
 		
 		Server server = getServer();
 		server.customizeRequest(request);
 		
 		request.setCommitted(true);
 		
 		return server.getTransactionManager().sendRequest(request, listener);
 	}
 	
 	public ClientTransaction sendRequest(SipRequest request) throws IOException
 	{
 		if (!isUA())
 			throw new IllegalStateException("Session is not UA");
 		
 		ClientTransaction tx = sendRequest(request, _ua);
 		_ua.requestSent(request);
 		return tx;
 	}
 	
 	public void invokeServlet(SipRequest request) throws SipException
 	{
 		try
 		{
 			_appSession.getContext().handle(request);
 		}
 		catch (TooManyHopsException e)
 		{
 			throw new SipException(SipServletResponse.SC_TOO_MANY_HOPS);
 		}
 		catch (Throwable t)
 		{
 			throw new SipException(SipServletResponse.SC_SERVER_INTERNAL_ERROR, t);
 		}
 	}
 	
 	public void invokeServlet(SipResponse response)
 	{
 		try
 		{
 			_appSession.getContext().handle(response);
 		}
 		catch (Throwable t)
 		{
 			Log.debug(t);
 		}
 	}
 	
 	private void accessed()
 	{
 		_lastAccessed = System.currentTimeMillis();
 		_appSession.access(_lastAccessed);
 	}
 	
 	public void setState(State newState) 
     {
         if (Log.isDebugEnabled())
             Log.debug("{} -> {}", this, newState);
 		_state = newState;
 	}
 	
 	public void updateState(SipResponse response, boolean uac)
 	{
 		SipRequest request = (SipRequest) response.getRequest();
 		int status = response.getStatus();
 				
 		if (request.isInitial() && (request.isInvite() || request.isSubscribe()))
 		{
 			switch (_state)
 			{
 			case INITIAL:
 				if (status < 300)
 				{
 					if (_ua != null)
 						_ua.createDialog(response, uac);
 					else if (isProxy())
 						createProxyDialog(response);
 					
 					if (status < 200)
 						setState(State.EARLY);
 					else
 						setState(State.CONFIRMED);
 				}
 				else
 				{
 					if (uac)
 					{
 						_ua.resetDialog();
 						setState(State.INITIAL);
 					}
 					else
 					{
 						setState(State.TERMINATED);
 					}
 				}
 				break;
 			case EARLY:
 				if (200 <= status && status < 300)
 				{
 					setState(State.CONFIRMED);
 				}
 				else if (status >= 300)
 				{
 					if (uac)
 						setState(State.INITIAL);
 					else
 						setState(State.TERMINATED);
 				}
 				break;
 			}
 		}
 		else if (request.isBye())
 		{
 			setState(State.TERMINATED);
 		}
 	}
 	
 	protected void createProxyDialog(SipResponse response)
 	{
 		String tag = response.to().getParameter(SipParams.TAG);
         _remoteParty.setParameter(SipParams.TAG, tag);
 	}
 	
 	public void invalidateIfReady()
 	{
 		if (isValid() && getInvalidateWhenReady() && isReadyToInvalidate())
 		{
 			SipAppContext context = _appSession.getContext();
 			SipSessionListener[] listeners = context.getSipSessionListeners();
 			if (listeners.length > 0)
 				context.fire(listeners, AppSession.__sessionReadyToInvalidate, new SipSessionEvent(this));
 			
 			if (isValid() && getInvalidateWhenReady())
 				invalidate();
 		}
 	}
 	
 	private void checkValid()
 	{
 		if (!_valid)
 			throw new IllegalStateException("Session has been invalidated");
 	}
 	
 	public boolean isUA()
 	{
 		return _ua != null;
 	}
 	
 	public UA getUA()
 	{
 		return _ua;
 	}
 	
 	public void createUA(UAMode mode)
 	{
 		if (_role != Role.UNDEFINED)
 			throw new IllegalStateException("Session is " + _role);
 		
 		_role = mode == UAMode.UAC ? Role.UAC : Role.UAS;
 		_ua = new UA();
 	}
 	
 	public boolean isProxy()
 	{
 		return _role == Role.PROXY;
 	}
 	
 	public void setProxy()
 	{
 		if (isUA())
 			throw new IllegalStateException("session is " + _role);
 		
 		NameAddr tmp = _remoteParty;
 		_remoteParty = _localParty;
 		_localParty = tmp;
 		
 		_role = Role.PROXY;
 	}
 	
 	public boolean isDialog(String fromTag, String toTag)
 	{
 		String localTag = _localParty.getParameter(SipParams.TAG);
 		String remoteTag = _remoteParty.getParameter(SipParams.TAG);
 		
 		if (fromTag.equals(localTag) && toTag.equals(remoteTag))
 			return true;
 		if (toTag.equals(localTag) && fromTag.equals(remoteTag))
 			return true;
 		return false;
 	}
 	
 	public boolean isSameDialog(SipResponse response)
 	{
 		String remoteTag = _remoteParty.getParameter(SipParams.TAG);
 		if (remoteTag != null)
 		{
 			String responseTag = response.to().getParameter(SipParams.TAG);
 			if (responseTag != null && !remoteTag.equalsIgnoreCase(responseTag))
 				return false;
 		}
 		return true;
 	}
 		
 	public SipServletHolder getHandler()
 	{
 		return _handler;
 	}
 	
 	public void setHandler(SipServletHolder handler)
 	{
 		_handler = handler;
 	}
 	
 	public void setSubscriberURI(URI uri)
 	{
 		_subscriberURI = uri;
 	}
 	
 	public void setRegion(SipApplicationRoutingRegion region)
 	{
 		_region = region;
 	}
 	
 	protected HashMap<String, Object> newAttributeMap()
 	{
 		return new HashMap<String, Object>(3);
 	}
 	
 	private void bindValue(String name, Object value)
 	{
 		if (value != null && value instanceof SipSessionBindingListener)
 			((SipSessionBindingListener) value).valueBound(new SipSessionBindingEvent(this, name));
 	}
 	
 	private void unbindValue(String name, Object value)
 	{
 		if (value != null && value instanceof SipSessionBindingListener)
 			((SipSessionBindingListener) value).valueUnbound(new SipSessionBindingEvent(this, name));
 	}
 	
 	public AppSession appSession()
 	{
 		return _appSession;
 	}
 	
 	public CallSession getCallSession()
 	{
 		return _appSession.getCallSession();
 	}
 	
 	public Server getServer()
 	{
 		return _appSession.getCallSession().getServer();
 	}
 	
 	private boolean hasTransactions()
 	{
 		return true; // TODO
 	}
 	
 	public Address getContact()
 	{
 		Address address = getServer().getConnectorManager().getContact(SipConnectors.TCP_ORDINAL);
 		address.getURI().setParameter(ID.APP_SESSION_ID_PARAMETER, _appSession.getAppId());
 		return address;
 	}
 	
 	@Override
 	public Session clone()
 	{
 		return this; // TODO
 	}
 	
 	public String toString()
 	{
 		return "[" + _id + ",state=" + _state + ", _role = " + _role + "]";
 	}
 	
 	@Override
 	public boolean equals(Object o)
 	{
 		if (!(o instanceof SessionIf))
 			return false;
 		return super.equals(((SessionIf) o).getSession());
 	}
 	
 	public void setLinkedSession(Session session) 
 	{ 
 		_linkedSessionId = session != null ? session.getId() : null;
 	}
 	
 	public Session getLinkedSession() 
 	{ 
 		return _linkedSessionId != null ? (Session) _appSession.getSipSession(_linkedSessionId) : null; 
 	}
 	
 	public boolean isTerminated()
 	{
 		return _state == State.TERMINATED || !_valid;
 	}
 	
 	public class UA implements ClientTransactionListener, ServerTransactionListener
 	{
 		private UAMode _mode;
 		
 		protected long _localCSeq = 1;
 		protected long _remoteCSeq = -1;
 		protected URI _remoteTarget;
 		protected LinkedList<String> _routeSet;
 		protected boolean _secure = false;
 		
 		private Object _serverInvites;
 		private Object _clientInvites;
 		
 		protected long _remoteRSeq = -1;
 		protected long _localRSeq = 1;
 		 
 		protected UA() { }
 		
 		protected UA(UA other)
 		{
 			_localCSeq = other._localCSeq;
 		}
 		
 		public SipRequest createRequest(SipRequest srcRequest)
 		{
 			SipRequest request = (SipRequest) srcRequest.clone();
             
             request.getFields().remove(SipHeaders.RECORD_ROUTE_BUFFER);
             request.getFields().remove(SipHeaders.VIA_BUFFER);
             request.getFields().remove(SipHeaders.CONTACT_BUFFER);
             
             setDialogHeaders(request, _localCSeq++);
             		
 			//request.setInitial(true);
 			request.setSession(Session.this);
 			
 			return request;
 		}
 		
 		public SipServletRequest createRequest(String method)
 		{
 			if (method.equalsIgnoreCase(SipMethods.ACK) || method.equalsIgnoreCase(SipMethods.CANCEL))
 				throw new IllegalArgumentException("Forbidden request method " + method);
 		
 			if (_state == State.TERMINATED)
 				throw new IllegalStateException("Cannot create request in TERMINATED state");
 			else if (_state == State.INITIAL && _role == Role.UAS)
 				throw new IllegalStateException("Cannot create request in INITIAL state and UAS mode");
 			
 			return createRequest(method, _localCSeq++);
 		}
 		
 		public SipServletRequest createAck()
 		{
 			return createRequest(SipMethods.ACK, _localCSeq);
 		}
 		
 		public SipServletRequest createRequest(String method, long cseq)
 		{
 			SipRequest request = new SipRequest();
 			request.setMethod(method.toUpperCase());
 			
 			setDialogHeaders(request, cseq);
 			
 			request.setSession(Session.this);
 			return request;
 		}
 		
 		protected void setDialogHeaders(SipRequest request, long cseq)
 		{
 			SipFields fields = request.getFields();
 			
 			fields.setAddress(SipHeaders.FROM_BUFFER, _localParty);
 			fields.setAddress(SipHeaders.TO_BUFFER, _remoteParty);
 			
 			if (_remoteTarget != null)
 				request.setRequestURI((URI) _remoteTarget.clone());
			else
 				request.setRequestURI(request.to().getURI());
 			
 			if (_routeSet != null)
 			{
 				fields.remove(SipHeaders.ROUTE_BUFFER);
 				
 				for (String route: _routeSet)
 				{
 					fields.addString(SipHeaders.ROUTE_BUFFER, route);
 				}
 			}
 			fields.setString(SipHeaders.CALL_ID_BUFFER, _callId);
 			fields.setString(SipHeaders.CSEQ_BUFFER, cseq + " " + request.getMethod());
 			fields.setString(SipHeaders.MAX_FORWARDS_BUFFER, "70");
 			
 			if (request.needsContact())
 				fields.setAddress(SipHeaders.CONTACT_BUFFER, getContact());
 		}
 		
 		public void handleRequest(SipRequest request) throws IOException, SipException
 		{
 			if (request.getCSeq().getNumber() <= _remoteCSeq && !request.isAck() && !request.isCancel())
 				throw new SipException(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "Out of order request");
 			
 			_remoteCSeq = request.getCSeq().getNumber();
 			if (request.isInvite())
 				setRemoteTarget(request);
 			
 			if (request.isAck())
 			{
 				ServerInvite invite = getServerInvite(_remoteCSeq, false);
 				if (invite == null)
 				{
 					if (Log.isDebugEnabled())
 						Log.debug("dropping ACK without INVITE context");
 					request.setHandled(true);
 				}
 				else
 				{
 					if (invite.getResponse() != null)
 						invite.ack();
 					else // retrans or late
 						request.setHandled(true);
 				}
 			}
 			else if (request.isPrack())
 			{
 				RAck rack = null;
 				
 				try 
 				{
 					rack = request.getRAck();
 				}
 				catch (Exception e)
 				{
 					throw new SipException(SipServletResponse.SC_BAD_REQUEST, e.getMessage());
 				}
 				
 				ServerInvite invite = getServerInvite(rack.getCSeq(), false);
 				
 				if (invite == null || !invite.prack(rack.getRSeq()))
 					throw new SipException(SipServletResponse.SC_CALL_LEG_DONE, "No matching 100 rel for RAck " + rack);
 				 
 			}
 		}
 		
 		public void handleCancel(ServerTransaction transaction, SipRequest cancel) throws IOException 
 		{
 			cancel.setSession(Session.this);
 			if (transaction.isCompleted())
 			{
 				Log.debug("ignoring late cancel {}", transaction);
 			}
 			else
 			{
 				try
 				{
 					transaction.getRequest().createResponse(SipServletResponse.SC_REQUEST_TERMINATED).send();
 					setState(State.TERMINATED);
 				}
 				catch (Exception e)
 				{
 					Log.debug("failed to cancel request", e);
 				}
 			}
 			invokeServlet(cancel);
 		}
 		
 		public void handleResponse(SipResponse response)
 		{
 			if (response.getStatus() == 100)
 				return;
 			
 			if (!isSameDialog(response))
 			{
 				Session derived = _appSession.getSession(response);
 				if (derived == null)
 					derived = _appSession.createDerivedSession(Session.this);
 				derived._ua.handleResponse(response);
 				return;
 			}
 			
 			response.setSession(Session.this);
 			
 			accessed();
 			
 			if (response.isInvite() && response.is2xx())
 			{
 				long cseq = response.getCSeq().getNumber();
 				ClientInvite invite = getClientInvite(cseq, true);
 				
 				if (invite._2xx != null || invite._ack != null)
 				{
 					if (invite._ack != null)
 					{
 						try
 						{
 							ClientTransaction tx = (ClientTransaction) invite._ack.getTransaction();
 							getServer().getConnectorManager().send(invite._ack, tx.getConnection());
 						}
 						catch (Exception e)
 						{
 							Log.ignore(e);
 						}
 					}
 					return;
 				}
 				else
 				{
 					invite._2xx = response;
 				}
 			}
 			else if (response.isReliable1xx())
 			{
 				long rseq = response.getRSeq();
 				if (_remoteRSeq != -1 && (_remoteRSeq + 1 != rseq))
 				{
 					if (Log.isDebugEnabled())
 						Log.debug("Dropping 100rel with rseq {} since expecting {}", rseq, _remoteRSeq+1);
 					return;
 				}
 				else
 					_remoteRSeq = rseq;
 			}
 			else
 				response.setCommitted(true);
 			
 			updateState(response, true);
 			
 			if (response.getStatus() < 300 && (response.isInvite() || response.isSubscribe()))
 				setRemoteTarget(response);
 			
 			if (isValid())
 				invokeServlet(response);
 		}
 		
 		public void sendResponse(SipResponse response, boolean reliable) throws IOException
 		{
 			ServerTransaction tx = (ServerTransaction) response.getTransaction();
 			
 			if (tx != null)
 			{
 				if (tx.isCompleted())
 					throw new IllegalStateException("transaction terminated for response " + response.getRequestLine());
 			
 				tx.setListener(this);
 			}
 			
 			updateState(response, false);
 			
 			SipRequest request = (SipRequest) response.getRequest();
 
 			if (request.isInitial() && (response.to().getParameter(SipParams.TAG) == null))
 			{
 				String tag = _localParty.getParameter(SipParams.TAG);
 				if (tag == null)
 					tag = ID.newTag();
 				response.to().setParameter(SipParams.TAG, tag);
 			}
 			
 			if (request.isInvite() || request.isSubscribe())
 				setRemoteTarget(request);
 			
 			if (request.isInvite())
 			{
 				int status = response.getStatus();
 				long cseq = response.getCSeq().getNumber();
 				
 				if (200 <= status && (status < 300))
 				{
 					ServerInvite invite = getServerInvite(cseq, true);
 					invite.set2xx(response);
 				}
 				else if ((100 < status) && (status < 200)  && reliable)
 				{
 					ServerInvite invite = getServerInvite(cseq, true);
 					
 					long rseq = _localRSeq++;
 					response.getFields().addString(SipHeaders.REQUIRE_BUFFER, SipParams.REL_100);
 					response.setRSeq(rseq);
 					
 					invite.addReliable1xx(response);
 				}
 				else if (status >= 300)
 				{
 					ServerInvite invite = getServerInvite(cseq, false);
 					if (invite != null)
 						invite.stop1xxRetrans();
 				}
 			}
 			if (tx != null)
 				tx.send(response);
 			else
 				getServer().getConnectorManager().sendResponse(response);
 		}
 		
 		public void requestSent(SipRequest request)
 		{
 			if (request.isAck())
 			{
 				ClientInvite invite = getClientInvite(request.getCSeq().getNumber(), false);
 				if (invite != null)
 				{
 					invite._2xx = null;
 					invite._ack = request;
 				}
 			}
 		}
 		
 		protected void resetDialog()
 		{
 			_remoteTarget = _remoteParty.getURI();
 			_remoteParty.setParameter(SipParams.TAG, null);
 			_remoteCSeq = -1;
 			_routeSet = null;
 			_secure = false;
 			
 			_remoteRSeq = -1;
 			_localRSeq = 1;
 		}
 		
 		protected void createDialog(SipResponse response, boolean uac)
 		{
 			if (uac)
 			{
 				String tag = response.to().getParameter(SipParams.TAG);
                 _remoteParty.setParameter(SipParams.TAG, tag);
                 
                 //System.out.println("Created dialog: " + tag);
                 setRoute(response, true);
 			}
 			else
 			{
 				String tag = ID.newTag();
 				_localParty.setParameter(SipParams.TAG, tag);
 				
 				/*String rtag = response.to().getParameter(SipParams.TAG);
                 if (rtag == null) 
                 {
                     String tag = _localParty.getParameter(SipParams.TAG);
                     if (tag == null) 
                     {
                         tag = ID.newTag();
                         _localParty.setParameter(SipParams.TAG, tag);
                     }
                     response.to().setParameter(SipParams.TAG, tag);
                 }*/
                 
                 SipRequest request = (SipRequest) response.getRequest();
     			
 				_remoteCSeq = request.getCSeq().getNumber();
 				_secure = request.isSecure() && request.getRequestURI().getScheme().equals("sips");
 				
 				setRoute(request, false);
 			}
 		}
 		
 		protected void setRemoteTarget(SipMessage message) 
 		{
 			Address contact = message.getFields().getAddress(SipHeaders.CONTACT_BUFFER);
 			if (contact != null)
 				_remoteTarget = contact.getURI();
 		}
 		
 		protected void setRoute(SipMessage message, boolean reverse)
 		{
 			ListIterator<String> routes = message.getFields().getValues(SipHeaders.RECORD_ROUTE_BUFFER);
 			_routeSet = new LinkedList<String>();
 			while (routes.hasNext())
 			{
 				if (reverse)
 					_routeSet.addFirst(routes.next());
 				else
 					_routeSet.addLast(routes.next());
 			}
 		}
 		
 		public boolean isSecure()
 		{
 			return _secure;
 		}
 		
 		public void transactionTerminated(Transaction transaction) 
 		{ 
 			if (transaction.isServer() && transaction.isInvite())
 			{
 				long cseq = transaction.getRequest().getCSeq().getNumber();
 				removeServerInvite(cseq);
 			}
 		}
 		
 		private ServerInvite getServerInvite(long cseq, boolean create)
 		{
 			for (int i = LazyList.size(_serverInvites); i-->0;)
 			{
 				ServerInvite invite = (ServerInvite) LazyList.get(_serverInvites, i);
 	            if (invite.getSeq() == cseq)
 	            	return invite;
 			}
 			if (create)
 			{
 				ServerInvite invite = new ServerInvite(cseq);
 				_serverInvites = LazyList.add(_serverInvites, invite);
 				
 				if (Log.isDebugEnabled())
 					Log.debug("added server invite context with cseq " + cseq);
 				
 				return invite;
 			}
 			return null;
 		}
 	
 		private ServerInvite removeServerInvite(long cseq)
 		{
 			for (int i = LazyList.size(_serverInvites); i-->0;)
 			{
 				ServerInvite invite = (ServerInvite) LazyList.get(_serverInvites, i);
 				if (invite.getSeq() == cseq)
 				{
 					_serverInvites = LazyList.remove(_serverInvites, i);
             	
 					if (Log.isDebugEnabled())
 						Log.debug("removed server invite context for cseq " + cseq);
 					return invite;
 				}
 			}
 			return null;
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
 				ClientInvite invite = new ClientInvite(cseq);
 				_clientInvites = LazyList.add(_clientInvites, invite);
 				
 				if (Log.isDebugEnabled())
 					Log.debug("added client invite context with cseq " + cseq);
 				return invite;
 			}
 			return null;
 		}
 		
 		public List<SipServletResponse> getUncommitted2xx(UAMode mode)
 		{
 			List<SipServletResponse> list = null;
 			if (mode == UAMode.UAS)
 			{
 				for (int i = LazyList.size(_serverInvites); i-->0;)
 				{
 					ServerInvite invite = (ServerInvite) LazyList.get(_serverInvites, i);
 					SipResponse response = invite.getResponse();
 					if (response != null && !response.isCommitted())
 					{
 						if (list == null)
 							list = new ArrayList<SipServletResponse>();
 						list.add(response);
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
 			if (list == null)
 				return Collections.emptyList();
 			else
 				return list;
 		}
 		
 		class ClientInvite
 		{
 			private long _cseq;
 			private SipRequest _ack;
 			private SipResponse _2xx;
 		
 			public ClientInvite(long cseq) { _cseq = cseq; }
 			public long getCSeq() { return _cseq; }
 		}
 		
 		abstract class ReliableResponse
 		{
 			private static final int TIMER_RETRANS = 0;
 			private static final int TIMER_WAIT_ACK = 1;
 			
 			private long _seq;
 			protected SipResponse _response;
 			private TimerTask[] _timers;
 			private long _retransDelay = Transaction.__T1;
 			
 			public ReliableResponse(long seq) { _seq = seq; }
 			
 			public long getSeq() { return _seq; }
 			public SipResponse getResponse() { return _response; }
 			
 			public void startRetrans(SipResponse response)
 			{
 				_response = response;
 				
 				_timers = new TimerTask[2];
 				_timers[TIMER_RETRANS] = getCallSession().schedule(new Timer(TIMER_RETRANS), _retransDelay);
 				_timers[TIMER_WAIT_ACK] = getCallSession().schedule(new Timer(TIMER_WAIT_ACK), 64*Transaction.__T1);
 			}
 			
 			public void stopRetrans()
 			{
 				cancelTimer(TIMER_RETRANS);
 				_response = null;
 			}
 			
 			public void ack()
 			{
 				stopRetrans();
 				cancelTimer(TIMER_WAIT_ACK);
 			}
 			
 			private void cancelTimer(int id)
 			{
 				TimerTask timer = _timers[id];
 				if (timer != null)
 					getCallSession().cancel(timer);
 				_timers[id] = null;
 			}
 			
 			/** 
 			 * @return the delay for the next retransmission, -1 to stop retransmission
 			 */
 			public abstract long retransmit(long delay);
 			public abstract void noAck();
 			
 			protected void timeout(int id)
 			{
 				switch(id)
 				{
 				case TIMER_RETRANS:
 					if (_response != null)
 					{
 						_retransDelay = retransmit(_retransDelay);
 						if (_retransDelay > 0)
 							_timers[TIMER_RETRANS] = getCallSession().schedule(new Timer(TIMER_RETRANS), _retransDelay);
 					}
 					break;
 				case TIMER_WAIT_ACK:
 					cancelTimer(TIMER_RETRANS);
 					if (_response != null)
 					{
 						noAck();
 						_response = null;
 					}
 					break;
 				default:
 					throw new IllegalArgumentException("unknown id " + id);
 				}
 				
 			}
 			
 			class Timer implements Runnable
 			{
 				private int _id;
 				
 				public Timer(int id) { _id = id; }
 				public void run() { timeout(_id); }
 				@Override public String toString() { return _id == TIMER_RETRANS ? "retrans" : "wait-ack"; }
 			}
 		}
 		
 		class ServerInvite extends ReliableResponse
 		{
 			private Object _reliable1xxs;
 			
 			public ServerInvite(long cseq) { super(cseq); }
 			
 			public void set2xx(SipResponse response)
 			{
 				stop1xxRetrans();
 				startRetrans(response);
 			}
 			
 			public void addReliable1xx(SipResponse response)
 			{
 				Reliable1xx reliable1xx = new Reliable1xx(response.getRSeq());
 				_reliable1xxs = LazyList.add(_reliable1xxs, reliable1xx);
 				reliable1xx.startRetrans(response);
 			}
 			
 			public boolean prack(long rseq)
 			{
 				for (int i = LazyList.size(_reliable1xxs); i-->0;)
 				{
 					Reliable1xx reliable1xx = (Reliable1xx) LazyList.get(_reliable1xxs, i);
 					if (reliable1xx.getSeq() == rseq)
 					{
 						reliable1xx.ack();
 						_reliable1xxs = LazyList.remove(_reliable1xxs, i);
 						return true;
 					}
 				}
 				return false;
 			}
 			
 			public void stop1xxRetrans()
 			{
 				for (int i = LazyList.size(_reliable1xxs); i-->0;)
 				{
 					Reliable1xx reliable1xx = (Reliable1xx) LazyList.get(_reliable1xxs, i);
 					reliable1xx.stopRetrans();
 				}
 			}
 			
 			public void noAck() 
 			{
 				_appSession.noAck(getResponse().getRequest(), getResponse());
 			}
 
 			public long retransmit(long delay) 
 			{
 				ServerTransaction tx = (ServerTransaction) getResponse().getTransaction();
 				tx.send(getResponse());
 				return Math.min(delay*2, Transaction.__T2);
 			}
 			
 			class Reliable1xx extends ReliableResponse
 			{
 				public Reliable1xx(long rseq) { super(rseq); }
 				
 				public long retransmit(long delay)
 				{
 					ServerTransaction tx = (ServerTransaction) getResponse().getTransaction();
 					if (tx.getState() == Transaction.STATE_PROCEEDING)
 					{
 						tx.send(getResponse());
 						return delay*2;
 					}
 					return -1;
 				}
 				
 				public void noAck()
 				{
 					_appSession.noPrack(getResponse().getRequest(), getResponse());
 				}
 			}
 		}
 	}
 }
