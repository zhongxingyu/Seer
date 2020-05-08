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
 
 package org.cipango;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletInputStream;
 import javax.servlet.ServletRequest;
 import javax.servlet.sip.Address;
 import javax.servlet.sip.AuthInfo;
 import javax.servlet.sip.B2buaHelper;
 import javax.servlet.sip.Proxy;
 import javax.servlet.sip.ServletParseException;
 import javax.servlet.sip.SipServletRequest;
 import javax.servlet.sip.SipServletResponse;
 import javax.servlet.sip.SipURI;
 import javax.servlet.sip.TooManyHopsException;
 import javax.servlet.sip.URI;
 import javax.servlet.sip.ar.SipApplicationRouterInfo;
 import javax.servlet.sip.ar.SipApplicationRoutingDirective;
 import javax.servlet.sip.ar.SipApplicationRoutingRegion;
 
 import org.cipango.SessionManager.SessionTransaction;
 import org.cipango.security.AuthInfoImpl;
 import org.cipango.security.Authenticate;
 import org.cipango.security.Authorization;
 import org.cipango.security.AuthInfoImpl.AuthElement;
 import org.cipango.servlet.Session;
 import org.cipango.sip.ClientTransaction;
 import org.cipango.sip.ServerTransaction;
 import org.cipango.util.LazyMap;
 import org.mortbay.io.Buffer;
 import org.mortbay.log.Log;
 
 public class SipRequest extends SipMessage implements SipServletRequest
 {
     private String _method;
     private URI _requestUri;
     private String _sRequestUri;
     private boolean _initial = false;
     private SipProxy _proxy;
     
     private Address _poppedRoute;
     private Address _initialPoppedRoute;
 
     private Serializable _stateInfo;
     private SipApplicationRouterInfo _nextRouterInfo;
 
     private B2bHelper _b2bHelper;
     
     private SipApplicationRoutingDirective _directive;
     private SipApplicationRoutingRegion _region;
     private URI _subscriberURI;
     
     private boolean _nextHopStrictRouting = false;
    
 	private Object _handlerAttributes;
     
 	public SipRequest() { }
 	
 	/**
 	 * @see SipServletRequest#createCancel()
 	 */
 	public SipServletRequest createCancel() throws IllegalStateException 
     {
 		if (getTransaction().isCompleted())
 			throw new IllegalStateException("Transaction has completed");
 		
 		return createRequest(SipMethods.CANCEL);
 	}
 	
 	/**
 	 * @see SipServletRequest#createResponse(int)
 	 */
 	public SipServletResponse createResponse(int status) 
     { 
         return createResponse(status, null); 
     }
  
 	/**
 	 * @see SipServletRequest#createResponse(int, java.lang.String)
 	 */
     public SipServletResponse createResponse(int status, String reason) 
     {
     	if (isAck()) 
     		throw new IllegalStateException("Cannot create response on ACK");
     	
     	if (!(getTransaction() instanceof ServerTransaction))
     		throw new IllegalStateException("Cannot create response if not in UAS mode");
     	
     	if (getTransaction().isCompleted()) 
     		throw new IllegalStateException("Cannot create response if final response has been sent");
     	
     	/*if (session != null && session.getRole() == Session.ROLE_PROXY)
     		throw new IllegalStateException("Session is proxy");*/
     	
     	return new SipResponse(this, status, reason);
     }
     
     /**
      * @see SipServletRequest#getInputStream()
      */
     public ServletInputStream getInputStream() { return null; }
     
     /**
      * @see SipServletRequest#getMaxForwards()
      */
     public int getMaxForwards() 
     {
     	return (int) _fields.getLong(SipHeaders.MAX_FORWARDS_BUFFER);
     }
     
     /**
      * @see SipServletRequest#getProxy()
      */
     public Proxy getProxy() throws TooManyHopsException 
     {
     	return getProxy(true);
     }
     
     /**
      * @see SipServletRequest#getProxy(boolean)
      */
     public Proxy getProxy(boolean create) throws TooManyHopsException 
     {
     	if (_proxy != null || !create) 
     		return _proxy;
     	
     	if (!isServer()) 
     		throw new IllegalStateException("Not a received request");
     	
     	if (_session.getRole() == Session.Role.UNDEFINED) 
     		_session.setRole(Session.Role.PROXY);
     	else if (_session.getRole() != Session.Role.PROXY) 
     		throw new IllegalStateException(
     				"Session is " + 
     				_session.getRole());
 
     	if (_proxy == null) 
     	{
     		_proxy = new SipProxy(this);
     		_proxy.setProxyTimeout(appSession().getContext().getProxyTimeout());
     	}
     	return _proxy;
     }
     
     /**
      * @see SipServletRequest#getReader()
      */
     public BufferedReader getReader() {return null;}
     
     /**
      * @see SipServletRequest#getRequestURI()
      */
     public URI getRequestURI() 
     {
     	if (_requestUri == null)
     	{
 			try
 			{
 				_requestUri = URIFactory.parseURI(_sRequestUri);
 				_sRequestUri = null;
 			}
 			catch (ServletParseException e)
 			{
 				Log.info("Received bad request: " + e.getMessage());
 			}
     	}
 		return _requestUri;
 	}
     
     /**
      * @see SipServletRequest#isInitial()
      */
     public boolean isInitial() 
     {
 		return _initial;
 	}
     
     /**
      * @see SipServletRequest#pushRoute(javax.servlet.sip.SipURI)
      */
     public void pushRoute(SipURI route) 
     {
     	pushRoute(new NameAddr(route));
     }
 
 	public void pushRoute(Address route)
 	{
 		if (!route.getURI().isSipURI())
     		throw new IllegalArgumentException("Only routes with a SIP URI may be pushed"); 
 		
 		boolean strictRouting = !((SipURI) route.getURI()).getLrParam();
 		
 		if (isNextHopStrictRouting())
 		{
 			if (strictRouting)
 			{
 				_fields.addAddress(SipHeaders.ROUTE_BUFFER, new NameAddr(getRequestURI()), true);
 				setRequestURI(route.getURI());
 			}
 			else
 			{
 				Address lastRoute = removeLastRoute();
 				_fields.addAddress(SipHeaders.ROUTE_BUFFER, new NameAddr(getRequestURI()), true);	
 				setRequestURI(lastRoute.getURI());
 				_fields.addAddress(SipHeaders.ROUTE_BUFFER, (NameAddr) route, true);
 			}
 		}
 		else if (strictRouting)
 		{
 			_fields.addAddress(SipHeaders.ROUTE_BUFFER, new NameAddr(getRequestURI()), false);
 			setRequestURI(route.getURI());
 		}
 		else
 		{
 			_fields.addAddress(SipHeaders.ROUTE_BUFFER, (NameAddr) route, true);
 		}
 		setNextHopStrinctRouting(strictRouting);
 	}   
 	
 	public Address getLastRoute()
 	{
 		ListIterator<Address> routes = _fields.getAddressValues(SipHeaders.ROUTE_BUFFER);
 		Address lastRoute = null;
 		while (routes.hasNext())
 			lastRoute = routes.next();
 		return lastRoute;
 	}
 	
 	public void setNextHopStrinctRouting(boolean nextHopStrictRouting)
 	{
 		_nextHopStrictRouting = nextHopStrictRouting;
 	}
 	
 	public boolean isNextHopStrictRouting()
 	{
 		return _nextHopStrictRouting;
 	}
     
 	public void pushPath(Address path)
 	{
 		// TODO Auto-generated method stub
 		
 	}
     
     /**
      * @see SipServletRequest#send()
      */
     public void send() throws IOException
     {
     	if (isCommitted())
             throw new IllegalStateException("Request is already commited");
         if (getTransaction() != null && !(getTransaction() instanceof ClientTransaction))
         	throw new IllegalStateException("Can send request only in UAC mode");
     	setCommitted(true);
     	
     	SessionManager csm = getCallSession().getServer().getSessionManager();
     	
     	SessionTransaction workUnit = csm.begin(getCallSession());
     	try
     	{
 	    	if (isCancel())
 	    		((ClientTransaction) getTransaction()).cancel(this);
 	    	else
 	    		getCallSession().getServer().sendRequest(this, _session);
     	} 
     	finally
     	{
     		workUnit.done();
     	}
     }
     
     /**
      * @see SipServletRequest#setMaxForwards(int)
      */
     public void setMaxForwards(int maxForwards) 
     {
     	if (maxForwards < 0 || maxForwards > 255) 
     		throw new IllegalArgumentException("Max-Forwards should be between 0 and 255");
     
     	_fields.setString(SipHeaders.MAX_FORWARDS_BUFFER, Long.toString(maxForwards));
     }
     
     /**
      * @see SipServletRequest#setRequestURI(javax.servlet.sip.URI)
      */
     public void setRequestURI(URI uri)
     {
     	if (uri == null)
     		throw new NullPointerException("Null uri");
 		this._requestUri = uri;
 	}
     
 	public boolean isRequest() 
     {
 		return true;
 	}
 	
 	public String getMethod() 
     {
 		return _method;
 	}
 	
 	/**
 	 * @see javax.servlet.ServletRequest#getLocale()
 	 */
 	public Locale getLocale() 
     {
 		return getAcceptLanguage();
 	}
 	
 	/**
 	 * @see javax.servlet.ServletRequest#getLocales()
 	 */
 	public Enumeration getLocales() 
     {
 		final Iterator it = getAcceptLanguages();
 		return new Enumeration() 
         {
 			public boolean hasMoreElements() 
             {
 				return it.hasNext();
 			}
 
 			public Object nextElement() 
             {
 				return it.next();
 			}
 		};
 	}
 	
 	/**
 	 * @see javax.servlet.ServletRequest#getLocalName()
 	 */
 	public String getLocalName() 
     {
		return getLocalAddr();
 	}
 	
 	/**
 	 * @see ServletRequest#getParameter(String)
 	 */
 	public String getParameter(String name) 
     {
 		SipURI paramUri = getParamUri();
 		
 		if (paramUri == null) 
 			return null;
 		
 		return paramUri.getParameter(name);
 	}
 	
 	/**
 	 * @see javax.servlet.ServletRequest#getParameterMap()
 	 */
 	public Map getParameterMap() 
     {
 		Map map = new HashMap();
 		
 		SipURI paramUri = getParamUri();
 		
 		if (paramUri != null) 
         {
 			Iterator it = paramUri.getParameterNames();
 			while (it.hasNext()) 
             {
 				String key = (String) it.next();
 				map.put(key, new String[] {paramUri.getParameter(key)});
 			}
 		}
 		return Collections.unmodifiableMap(map);
 	}
 	
 	/**
 	 * @see javax.servlet.ServletRequest#getParameterNames()
 	 */
 	public Enumeration getParameterNames() 
     {
 		SipURI paramUri = getParamUri();
 		
 		if (paramUri == null) 
 			return Collections.enumeration(Collections.EMPTY_LIST);
 		
 		return new IteratorToEnum(paramUri.getParameterNames());
 	}
 	
 	/**
 	 * @see ServletRequest#getParameterValues(String)
 	 */
 	public String[] getParameterValues(String name) 
     {
 		String value = getParameter(name);
 		if (value == null) 
 			return null;
 		
 		return new String[] {value};
 	}
 
 	/**
 	 * @see ServletRequest#getRealPath(String)
 	 */
 	public String getRealPath(String path) {return null;}
 
 	/**
 	 * @see ServletRequest#getRequestDispatcher(String)
 	 */
 	public RequestDispatcher getRequestDispatcher(String path) 
     {
 		throw new UnsupportedOperationException("Not Applicable");
 	}
 	
 	/**
 	 * @see ServletRequest#getRemoteHost()
 	 */
 	public String getRemoteHost() 
     {
 		return getRemoteAddr();
 	}
 
 	/**
 	 * @see ServletRequest#getScheme()
 	 */
 	public String getScheme() 
     {
 		return _requestUri.getScheme();
 	}
 	
 	/**
 	 * @see javax.servlet.ServletRequest#getServerName()
 	 */
 	public String getServerName()
     {
 		return getLocalName();
 	}
 	
 	/**
 	 * @see ServletRequest#getServerPort()
 	 */
 	public int getServerPort() 
     {
 		return getLocalPort();
 	}
 	
 	
 	public Object getAttribute(String name) 
 	{
 		if ("javax.sip.request.poppedRoute".equals(name))
 		{
 			return getPoppedRoute();
 		}
 		else 
 		{
 			return super.getAttribute(name);
 		}
 	}
 		
 	// -- 
 	
 	public SipURI getParamUri()
 	{
 		if (_poppedRoute != null)
 			return (SipURI) _poppedRoute.getURI();
 		else if (_requestUri.isSipURI())
 			return (SipURI) _requestUri;
 		else
 			return null;
 	}
 	
 	public void addRecordRoute(NameAddr route) 
     {
 		_fields.addAddress(SipHeaders.RECORD_ROUTE_BUFFER, route, true);
 	}
 	
 	public void pushVia(Via via) 
     {
 		_fields.addVia(via, true);
 	}
 	
 	protected SipProxy getProxyImpl() 
     {
 		return _proxy;
 	}
 	
 	protected void setProxyImpl(SipProxy proxy)
 	{
 		_proxy = proxy;
 	}
 	
 	public SipRequest createRequest(String method) 
     {
 		SipRequest request = new SipRequest();
 		
 		request._session = _session;
         request._callSession = _callSession;
         request.setTransaction(getTransaction());
 		request._fields.setAddress(SipHeaders.FROM, (NameAddr) getFrom().clone());
 		request._fields.setAddress(SipHeaders.TO, (NameAddr) getTo().clone());
 		
 		request.setMethod(method);
 		request.setRequestURI(getRequestURI());
 		request._fields.copy(_fields, SipHeaders.CALL_ID_BUFFER);
 		
         request._fields.setString(SipHeaders.CSEQ_BUFFER, getCSeq().getNumber() + " " + method);
            
 		request._fields.addVia(getTopVia(), true);
 		request._fields.copy(_fields, SipHeaders.MAX_FORWARDS_BUFFER);
         request._fields.copy(_fields, SipHeaders.ROUTE_BUFFER);
 		
 		return request;
 	}
 	
 	protected boolean canSetContact() 
     {
         return isRegister();
     }
 	
 	public Address getTopRoute() 
     {
 		return _fields.getAddress(SipHeaders.ROUTE_BUFFER);
 	}
 	
 	public boolean isServer() 
     {
 		return getTransaction().isServer();
 	}
 	
 	public boolean needsContact() 
     {
     	return isInvite() || isSubscribe() || isNotify() || isRefer() || isUpdate();
     }
 	
 	public Address removeTopRoute() 
     {
 		Address topRoute = _fields.getAddress(SipHeaders.ROUTE_BUFFER);
 		_fields.removeFirst(SipHeaders.ROUTE_BUFFER);
 		return topRoute;
 	}
 	
 	// For strict routing
 	public Address removeLastRoute() 
     {
 		Iterator<Address> it =  _fields.getAddressValues(SipHeaders.ROUTE_BUFFER);
 		List<Address> list = new ArrayList<Address>();
 		Address lastRoute = null;
 		while (it.hasNext())
 		{
 			Address route = it.next();
 			if (it.hasNext())
 				list.add(route);
 			else
 				lastRoute = route;
 		}
 		it = list.iterator();
 		if (it.hasNext())
 			_fields.setAddress(SipHeaders.ROUTE_BUFFER, (NameAddr) it.next());
 		else
 			return removeTopRoute();
 		
 		while (it.hasNext())
 		{
 			NameAddr route = (NameAddr) it.next();
 			_fields.addAddress(SipHeaders.ROUTE_BUFFER, route, false);
 		}
 		return lastRoute;
 	}
 	
 	public void setInitial(boolean b) 
     {
 		_initial = b;
 	}
 	
 	public void setMethod(String method) 
     {
 		this._method = method;
 	}
  
     public void setPoppedRoute(Address route)
     {
         _poppedRoute = route;
         if (_initialPoppedRoute == null)
         	_initialPoppedRoute = route;
     }
     
     public Address getPoppedRoute()
     {
         return _poppedRoute; 
     }
     
     public void setRequestURI(Buffer buffer)
     {
     	_sRequestUri = buffer.toString();
 	}
     
     public String getRequestURIAsString()
     {
     	if (_requestUri != null)
     		return _requestUri.toString();
     	else
     		return _sRequestUri;
     }
     
     // 
 	public void addAuthHeader(SipServletResponse challengeResponse, AuthInfo authInfo)
 	{
 		ListIterator<String> authenticates = challengeResponse.getHeaders(SipHeaders.WWW_AUTHENTICATE);
 		while (authenticates.hasNext())
 		{
 			Authenticate authenticate = new Authenticate(authenticates.next());
 			AuthElement element = ((AuthInfoImpl) authInfo).getAuthElement(challengeResponse.getStatus(), authenticate.getRealm());
 			Authorization authorization = 
 				new Authorization(authenticate, element.getUsername(), element.getPassword(), getRequestURIAsString(), getMethod());
 			addHeader(SipHeaders.AUTHORIZATION, authorization.toString());
 		}
 		authenticates = challengeResponse.getHeaders(SipHeaders.PROXY_AUTHENTICATE);
 		while (authenticates.hasNext())
 		{
 			Authenticate authenticate = new Authenticate(authenticates.next());
 			AuthElement element = ((AuthInfoImpl) authInfo).getAuthElement(challengeResponse.getStatus(), authenticate.getRealm());
 			Authorization authorization = 
 				new Authorization(authenticate, element.getUsername(), element.getPassword(), getRequestURIAsString(), getMethod());
 			addHeader(SipHeaders.PROXY_AUTHORIZATION, authorization.toString());
 		}
 	}
 	
 	public void addAuthHeader(SipServletResponse challengeResponse, String username, String password)
 	{
 		ListIterator<String> authenticates = challengeResponse.getHeaders(SipHeaders.WWW_AUTHENTICATE);
 		while (authenticates.hasNext())
 		{
 			Authenticate authenticate = new Authenticate(authenticates.next());
 			Authorization authorization = 
 				new Authorization(authenticate, username, password, getRequestURIAsString(), getMethod());
 			addHeader(SipHeaders.AUTHORIZATION, authorization.toString());
 		}
 		authenticates = challengeResponse.getHeaders(SipHeaders.PROXY_AUTHENTICATE);
 		while (authenticates.hasNext())
 		{
 			Authenticate authenticate = new Authenticate(authenticates.next());
 			Authorization authorization = 
 				new Authorization(authenticate, username, password, getRequestURIAsString(), getMethod());
 			addHeader(SipHeaders.PROXY_AUTHORIZATION, authorization.toString());
 		}
 	}
 
 	public B2buaHelper getB2buaHelper()
 	{
 		if (_proxy != null)
 			throw new IllegalStateException("getProxy() had already been called");
 		// TODO change session mode	
 		if (_b2bHelper == null)
 			_b2bHelper = new B2bHelper(this);
 		return _b2bHelper;
 	}
 	
 	public void setB2bHelper(B2bHelper b2bHelper)
 	{
 		_b2bHelper = b2bHelper;
 	}
 
 	public Address getInitialPoppedRoute()
 	{
 		return _initialPoppedRoute;
 	}
 	
 	public void setInitialPoppedRoute(Address route)
 	{
 		_initialPoppedRoute = route;
 	}
 
 	public SipApplicationRoutingRegion getRegion()
 	{
 		return _region;
 	}
 
 	public URI getSubscriberURI()
 	{
 		if (!isInitial())
 			throw new IllegalStateException("SipServletRequest is not initial");
 		return _subscriberURI;
 	}
 	
 	public void setRegion(SipApplicationRoutingRegion region)
 	{
 		_region = region;
 	}
 	
 	public Object clone() 
 	{
 		SipRequest clone = (SipRequest) super.clone(); 
 		clone._region = null;
 		clone._subscriberURI = null;
 		clone._b2bHelper = null;
 		clone._proxy = null;
 		clone._initialPoppedRoute = null;
 		return clone;
 	}
 	
 	
 	public void setSubscriberURI(URI uri)
 	{
 		_subscriberURI = uri;
 	}
 	
 	public SipApplicationRoutingDirective getRoutingDirective() throws IllegalStateException
 	{
 		if (!isInitial())
 			throw new IllegalStateException("SipServletRequest is not initial");
 		return _directive;
 	}
 
 	public void setRoutingDirective(SipApplicationRoutingDirective directive, SipServletRequest origRequest)
 			throws IllegalStateException
 	{
 		if (!isInitial())
 			throw new IllegalStateException("SipServletRequest is not initial");
 		if (directive != SipApplicationRoutingDirective.NEW && 
 				(origRequest == null || !origRequest.isInitial()))
 			throw new IllegalStateException("origRequest is not initial");
 		if (isCommitted())
 			throw new IllegalStateException("SipServletRequest is committed");
 		_directive = directive;
 	}
 	
 	public Serializable getStateInfo()
 	{
 		return _stateInfo;
 	}
 	
 	public void setStateInfo(Serializable stateInfo)
 	{
 		_stateInfo = stateInfo;
 	}
 	
 	public SipApplicationRouterInfo peekRouterInfo()
 	{
 		return _nextRouterInfo;
 	}
 	
 	public SipApplicationRouterInfo popRouterInfo()
 	{
 		SipApplicationRouterInfo info = _nextRouterInfo;
 		_nextRouterInfo = null;
 		return info;
 	}
 
 	public void pushRouterInfo(SipApplicationRouterInfo routerInfo)
 	{
 		_nextRouterInfo = routerInfo;
 	}
 	
 	public Object getHandlerAttribute(String name)
     {
     	return LazyMap.get(_handlerAttributes, name);
     }
     
     public void addHandlerAttribute(String name, Object value)
     {
     	_handlerAttributes = LazyMap.add(_handlerAttributes, name, value);
     }
     
     public String getRequestLine()
     {
     	return _method + " " + getRequestURI().toString(); 
     }
     
 	class IteratorToEnum  implements Enumeration<String>
 	{
 		private Iterator<String> _it;
 		public IteratorToEnum(Iterator<String> it)
 		{
 			_it = it;
 		}
 
 		public boolean hasMoreElements()
 		{
 			return _it.hasNext();
 		}
 
 		public String nextElement()
 		{
 			return _it.next();
 		}
 	}
 }
