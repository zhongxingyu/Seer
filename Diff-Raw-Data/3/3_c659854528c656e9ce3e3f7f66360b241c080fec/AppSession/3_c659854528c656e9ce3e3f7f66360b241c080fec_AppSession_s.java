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
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpSession;
 
 import javax.servlet.sip.*;
 
 import org.cipango.server.ID;
 import org.cipango.server.SipMessage;
 import org.cipango.sip.NameAddr;
 import org.cipango.sipapp.SipAppContext;
 import org.cipango.util.TimerTask;
 
 import org.eclipse.jetty.util.log.Log;
 import org.eclipse.jetty.util.LazyList;
 
 public class AppSession implements AppSessionIf
 {	
 	public static final String APP_ID_PREFIX = ";" + ID.APP_SESSION_ID_PARAMETER + "=";
 	
 	enum State { VALID, EXPIRED, INVALIDATING, INVALID }
 		
 	protected static Method __noAck;
     protected static Method __noPrack;
     protected static Method __appSessionCreated;
     protected static Method __appSessionReadyToInvalidate;
     protected static Method __appSessionExpired;
     protected static Method __appSessionDestroyed;
     protected static Method __timerExpired;
     protected static Method __sessionCreated;
     protected static Method __sessionReadyToInvalidate;
     protected static Method __sessionDestroyed;
     
     static 
     {
         try 
         {
             __noAck = SipErrorListener.class.getMethod("noAckReceived", SipErrorEvent.class);
             __noPrack = SipErrorListener.class.getMethod("noPrackReceived", SipErrorEvent.class);
             __appSessionCreated = SipApplicationSessionListener.class.getMethod("sessionCreated", SipApplicationSessionEvent.class);
             __appSessionReadyToInvalidate = 
             	SipApplicationSessionListener.class.getMethod("sessionReadyToInvalidate", SipApplicationSessionEvent.class);
             __appSessionExpired = SipApplicationSessionListener.class.getMethod("sessionExpired", SipApplicationSessionEvent.class);
             __appSessionDestroyed = SipApplicationSessionListener.class.getMethod("sessionDestroyed", SipApplicationSessionEvent.class);
             __timerExpired = TimerListener.class.getMethod("timeout", ServletTimer.class);
             __sessionCreated = SipSessionListener.class.getMethod("sessionCreated", SipSessionEvent.class);
             __sessionReadyToInvalidate = SipSessionListener.class.getMethod("sessionReadyToInvalidate", SipSessionEvent.class);
             __sessionDestroyed = SipSessionListener.class.getMethod("sessionDestroyed", SipSessionEvent.class);
         } 
         catch (NoSuchMethodException e)
         {
             throw new ExceptionInInitializerError(e);
         }
     }
     
     private String _appId; 
 	private State _state = State.VALID;
     
 	protected List<Session> _sessions = new ArrayList<Session>(1);
 	private Object _otherSessions;
 	
     private CallSession _callSession;
     protected SipAppContext _context;
     
     protected Map<String, Object> _attributes;
 
     protected long _created = System.currentTimeMillis();
     protected long _lastAccessed;
     protected int _expiryDelay;
     
     private List<ServletTimer> _timers;
     protected TimerTask _expiryTimer;
     
     protected boolean _invalidateWhenReady = true;
 	
     public AppSession(CallSession callSession, String id)
     {
         _callSession = callSession;
         _appId = id;
     }
     
     /**
      * @see SipApplicationSession#getCreationTime()
      */
 	public long getCreationTime()
 	{
 		checkValid();
 		return _created;
 	}
 
 	/**
 	 * @see SipApplicationSession#getLastAccessedTime()
 	 */
 	public long getLastAccessedTime()
 	{
 		return _lastAccessed;
 	}
 
 	/**
 	 * @see SipApplicationSession#getId()
 	 */
 	public String getId()
 	{
 		return _callSession.getId() + ";" + _appId;
 	}
 
 	/**
 	 * @see SipApplicationSession#setExpires(int)
 	 */
 	public int setExpires(int deltaMinutes)
 	{
 		if (!(_state == State.VALID || _state == State.EXPIRED))
 			throw new IllegalStateException();
 		
 		if (_expiryTimer != null)
 		{
 			_callSession.cancel(_expiryTimer);
 			_expiryTimer = null;
 		}
 		
 		_expiryDelay = deltaMinutes;
 		
 		if (_expiryDelay > 0)
 		{
 			long delayMs = _expiryDelay * 60000l;
 			_expiryTimer = _callSession.schedule(new ExpiryTimeout(), delayMs);
 			return _expiryDelay;
 		}
 		else
 			return Integer.MAX_VALUE;
 	}
 	
 	/**
 	 * @see SipApplicationSession#invalidate()
 	 */
 	public void invalidate()
 	{
 		checkValid();
 		
 		if (Log.isDebugEnabled())
 			Log.debug("invalidating SipApplicationSession: " + this);
 			
 		try 
 		{
 			if (_expiryTimer != null)
 			{
 				_callSession.cancel(_expiryTimer);
 				_expiryTimer = null;
 			}
 			
 			synchronized (this)
 			{			
 				for (int i = _sessions.size(); i-->0;)
 				{
 					_sessions.get(i).invalidate();
 				}
 				_sessions.clear();
 				
 				for (int i = LazyList.size(_otherSessions); i-->0;)
 				{
 					Object session = LazyList.get(_otherSessions, i);
 					if (session instanceof HttpSession)
 						((HttpSession) session).invalidate();
 				}
 				_otherSessions = null;
 				
 				if (_timers != null)
 				{
 					Iterator<ServletTimer> it2 = getTimers().iterator();
 					while (it2.hasNext())
 					{
 						it2.next().cancel();
 					}
 				}
 			}
 			getCallSession().removeSession(this);
 			
 			if (getContext() != null)
 				getContext().updateNbSessions(false);
 			
 			SipApplicationSessionListener[] listeners = getContext().getSipApplicationSessionListeners();
 			if (listeners.length > 0)
 				getContext().fire(listeners, __appSessionDestroyed, new SipApplicationSessionEvent(this));
 			
 			SipApplicationSessionAttributeListener[] attrListeners = getContext().getSipApplicationSessionAttributeListeners();
 	        while (_attributes!=null && _attributes.size()>0)
 	        {
 	            ArrayList<String> keys;
 	            synchronized (this)
 	            {
 	                keys=new ArrayList<String>(_attributes.keySet());
 	            }
 	
 	            Iterator<String> iter = keys.iterator();
 	            while (iter.hasNext())
 	            {
 	                String key = iter.next();
 	
 	                Object value;
 	                synchronized (this)
 	                {
 	                    value=_attributes.remove(key);
 	                }
 	                unbindValue(key,value);
 	
 	                if (attrListeners!=null)
 	                {
 	                	SipApplicationSessionBindingEvent event 
 							= new SipApplicationSessionBindingEvent(this, key);
 						for (int i = 0; i < attrListeners.length; i++)
 							attrListeners[i].attributeRemoved(event);
 		            }
 	            }
 	        }
 		}
 		finally
 		{
 			_state = State.INVALID;
 		}
 	}
 	
 	protected void expired()
 	{
 		if (_state == State.VALID)
 		{
 			_state = State.EXPIRED;
 			
 			SipApplicationSessionListener[] listeners = getContext().getSipApplicationSessionListeners();
 			if (listeners.length > 0)
 				getContext().fire(listeners, __appSessionExpired, new SipApplicationSessionEvent(this));
 			
 			if (_state == State.EXPIRED)
 			{
 				if (getExpirationTime() != Long.MIN_VALUE)
 					_state = State.VALID;
 				else
 					invalidate();
 			}
 		}
 	}
 	
 	public synchronized Session getSession(SipMessage message)
 	{
 		String ftag = message.from().getParameter("tag");
 		String ttag = message.to().getParameter("tag");
 		
 		for (int i = 0; i < _sessions.size(); i++)
 		{
 			Session session = _sessions.get(i);
 			if (session.isDialog(ftag, ttag))
 				return session;
 		}
 		return null;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public synchronized Iterator<?> getSessions()
 	{
 		checkValid();
 		
 		List<Object> list = new ArrayList<Object>(_sessions);
 		list.addAll(LazyList.getList(_otherSessions));
 		
 		return list.iterator();
 	}
 
 	public synchronized Iterator<?> getSessions(String protocol)
 	{
 		checkValid();
 		
 		if (protocol == null)
 			throw new NullPointerException("null protocol");
 		
 		if ("sip".equalsIgnoreCase(protocol))
 			return _sessions.iterator();
 		
 		if ("http".equalsIgnoreCase(protocol))
 		{
 			List<HttpSession> sessions = new ArrayList<HttpSession>();
 			for (int i = LazyList.size(_otherSessions); i-->0;)
 			{
 				Object session = LazyList.get(_otherSessions, i);
 				if (session instanceof HttpSession)
 					sessions.add((HttpSession) session);
 			}
 			return sessions.iterator();
 		}
 		throw new IllegalArgumentException("Unknown protocol " + protocol);
 	}
 
 	public synchronized ServletTimer getTimer(String id)
 	{
 		checkValid();
 		
 		if (_timers != null)
 		{
 			for (int i = 0; i < _timers.size(); i++)
 			{
 				ServletTimer timer = _timers.get(i);
 				if (timer.getId().equals(id))
 					return timer;
 			}
 		}
 		return null;
 	}
 
 	public void encodeURI(URI uri) 
 	{
 		checkValid();
 		uri.setParameter(ID.APP_SESSION_ID_PARAMETER, getId());
 	}
 
 	public synchronized Object getAttribute(String name) 
 	{
 		checkValid();
 		if (_attributes == null) 	
 			return null;
 		
 		return _attributes.get(name);
 	}
 
 	@SuppressWarnings("unchecked")
 	public synchronized Iterator<String> getAttributeNames() 
 	{
 		checkValid();
 		if (_attributes == null) 
 			return Collections.EMPTY_LIST.iterator();
 		
 		return _attributes.keySet().iterator();
 	}
 
 	public synchronized void setAttribute(String name, Object value) 
 	{
 		checkValid();
 		
 		if (value == null || name == null)
 			throw new NullPointerException("Name or attribute is null");
 
 		if (_attributes == null)
 			_attributes = new HashMap<String, Object>();
 	
 		Object oldValue = _attributes.put(name, value);
 
 		if (oldValue == null || !value.equals(oldValue))
 		{
 			unbindValue(name, oldValue);
 			bindValue(name, value);
 		
 			SipApplicationSessionAttributeListener[] listeners = getContext().getSipApplicationSessionAttributeListeners();
 			if (listeners.length > 0)
 			{
 				SipApplicationSessionBindingEvent event = 
 					new SipApplicationSessionBindingEvent(this, name);
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
 
 	public synchronized void removeAttribute(String name)
 	{
 		checkValid();
 		
 		if (_attributes == null)
 			return;
 		
 		Object oldValue = _attributes.remove(name);
 		
 		if (oldValue != null)
 		{
 			unbindValue(name, oldValue);
 			
 			SipApplicationSessionAttributeListener[] listeners = getContext().getSipApplicationSessionAttributeListeners();
 			if (listeners != null)
 			{
 				SipApplicationSessionBindingEvent event 
 					= new SipApplicationSessionBindingEvent(this, name);
 				for (int i = 0; i < listeners.length; i++)
 					listeners[i].attributeRemoved(event);
 			}
 		}
 	}
 
 	public Collection<ServletTimer> getTimers()
 	{
 		checkValid();
 		if (_timers == null)
 			return Collections.emptyList();
 		
 		return new ArrayList<ServletTimer>(_timers);
 	}
 
 	public void unbindValue(String name, Object value)
 	{
 		if (value != null && value instanceof SipApplicationSessionBindingListener)
 			((SipApplicationSessionBindingListener) value).valueUnbound(new SipApplicationSessionBindingEvent(this, name));
 	}
 	
 	public void bindValue(String name, Object value)
 	{
 		if (value != null && value instanceof SipApplicationSessionBindingListener)
 			((SipApplicationSessionBindingListener) value).valueBound(new SipApplicationSessionBindingEvent(this, name));
 	}
 	
     public void setCallSession(CallSession callSession)
     {
         _callSession = callSession;
     }
     
     public void access(long accessed)
     {
         _lastAccessed = accessed;
         
         if (_expiryTimer != null)
         {
         	_callSession.cancel(_expiryTimer);
         	_expiryTimer = null;
         }
         
         if (_expiryDelay > 0) // TODO refactor
         	_expiryTimer = _callSession.schedule(new ExpiryTimeout(), _expiryDelay * 60000l);
     }
     
 	private void checkValid() 
 	{
 		if (!isValid())
 			throw new IllegalStateException("SipApplicationSession has been invalidated");
 	}
 	
 	public boolean isValid()
 	{
 		return (!(_state == State.INVALID));
 	}
 
 	public URL encodeURL(URL url)
 	{
 		checkValid();
 		
 		try {
 			String sUrl = url.toExternalForm();
 			String id= getId().replace(";", "%3B");
 			int prefix=sUrl.indexOf(APP_ID_PREFIX);
 	        if (prefix!=-1)
 	        {
 	            int suffix=sUrl.indexOf("?",prefix);
 	            if (suffix<0)
 	                suffix=sUrl.indexOf("#",prefix);
 	
 	            if (suffix<=prefix)
 	                return new URL(sUrl.substring(0, prefix + APP_ID_PREFIX.length()) + id);
 	            return new URL(sUrl.substring(0, prefix + APP_ID_PREFIX.length()) + id + sUrl.substring(suffix));
 	        }
 	
 	        // edit the session
 	        int suffix=sUrl.indexOf('?');
 	        if (suffix<0)
 	            suffix=sUrl.indexOf('#');
 	        if (suffix<0)
 	            return new URL(sUrl+APP_ID_PREFIX+id);
 	        return new URL(sUrl.substring(0,suffix) + APP_ID_PREFIX + id + sUrl.substring(suffix));
 		} catch (Exception e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	public String getApplicationName()
 	{
 		return _context.getName();
 	}
 
 	public long getExpirationTime()
 	{
 		checkValid();
 		
 		if (_expiryTimer == null)
 			return 0;
 		else
 		{
 			long expirationTime = _expiryTimer.getExecutionTime();
 			if (expirationTime <= System.currentTimeMillis())
 				return Long.MIN_VALUE;
 			else
 				return expirationTime;
 		}
 	}
 
 	/**
 	 * @see SipApplicationSession#getSession(String, javax.servlet.sip.SipApplicationSession.Protocol)
 	 */
 	public synchronized Object getSession(String id, Protocol protocol)
 	{
 		checkValid();
 		
 		if (id == null || protocol == null)
 			throw new NullPointerException((id == null) ? "null id" : "null protocol");
 		
 		if (protocol == Protocol.SIP)
 		{
 			for (Session session : _sessions)
 			{
 				if (session.getId().equals(id))
 					return session;
 			}
 		}
 		else if (protocol == Protocol.HTTP)
 		{
 			for (int i = LazyList.size(_otherSessions); i-->0;)
 			{
 				Object session = LazyList.get(_otherSessions, i);
 				if (session instanceof HttpSession && ((HttpSession) session).getId().equals(id))
 					return session;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see SipApplicationSession#getSipSession(String)
 	 */
 	public SipSession getSipSession(String id)
 	{
 		return ((SipSession) getSession(id, Protocol.SIP));
 	}
 
 	public boolean isReadyToInvalidate()
 	{
 		checkValid();
 		
 		if (_lastAccessed == 0)
 			return false;
 		
 		for (int i = 0; i < _sessions.size(); i++)
 		{
 			Session session = _sessions.get(i);
 			if (!session.isReadyToInvalidate())
 				return false;
 		}
 		return (_timers == null || _timers.isEmpty());
 	}
 	
 	public void invalidateIfReady()
 	{
 		for (int i = 0; i < _sessions.size(); i++)
 		{
 			Session session = _sessions.get(i);
 			session.invalidateIfReady();
 		}
 		
		if (isValid() && getInvalidateWhenReady() && isReadyToInvalidate())
 		{
 			SipApplicationSessionListener[] listeners = getContext().getSipApplicationSessionListeners();
 			if (listeners.length >0)
 				getContext().fire(listeners, __appSessionReadyToInvalidate, new SipApplicationSessionEvent(this));
 			
 			if (getInvalidateWhenReady() && isValid())
 				invalidate();
 		}
 	}
 
 	public void setInvalidateWhenReady(boolean invalidateWhenReady)
 	{
 		checkValid();
 		_invalidateWhenReady = invalidateWhenReady;
 	}
 	
 	public boolean getInvalidateWhenReady()
 	{
 		checkValid();
 		return _invalidateWhenReady;
 	}
 	
 	public SipAppContext getContext()
 	{
 		return _context;
 	}
 		
 	public void setContext(SipAppContext context) 
 	{
 		if (_context != null)
 			throw new IllegalStateException("context != null");
 
 		_context = context;
 
 		_context.updateNbSessions(true);
 		
 		if (_context.getSpecVersion() == SipAppContext.VERSION_10)
 			_invalidateWhenReady = false;
 		
 		SipApplicationSessionListener[] listeners = _context.getSipApplicationSessionListeners();
 		if (listeners.length > 0)
 			getContext().fire(listeners, __appSessionCreated, new SipApplicationSessionEvent(this));
 
 		setExpires(_context.getSessionTimeout());
 	}
 	
     public Session createSession()
     {
         Session session = new Session(this, ID.newSessionId());
         session.setInvalidateWhenReady(_invalidateWhenReady);
         addSession(session);
         return session;
     }
     
     public Session createUacSession(String callId, NameAddr from, NameAddr to)
     {
         Session session = new Session(this, ID.newSessionId(), callId, from, to);
         session.setInvalidateWhenReady(_invalidateWhenReady);
         addSession(session);
         session.createUA(UAMode.UAC);
         return session;
     }
     
     public Session createDerivedSession(Session session)
     {
     	if (session.appSession() != this)
     		throw new IllegalArgumentException("SipSession " + session.getId() +  " does not belong to SipApplicationSession " + getId());
     	
     	Session derived = new Session(ID.newSessionId(), session);
     	derived.setInvalidateWhenReady(_invalidateWhenReady);
     	addSession(derived);
     	return derived;
     }
     
 	public void addSession(Object session)
 	{
 		if (session instanceof Session)
 		{
 			_sessions.add((Session) session);
 			
 			SipSessionListener[] listeners = getContext().getSipSessionListeners();
 			if (listeners.length > 0)
 				getContext().fire(listeners, __sessionCreated, new SipSessionEvent((SipSession) session));
 		}
 		else
 		{
 			_otherSessions = LazyList.add(_otherSessions, session);
 		}
 	}
 	
 	public void removeSession(Object session)
 	{
 		if (session instanceof Session)
 		{
 			_sessions.remove((Session) session);
 			
 			SipSessionListener[] listeners = getContext().getSipSessionListeners();
 			if (listeners.length > 0)
 				getContext().fire(listeners, __sessionDestroyed, new SipSessionEvent((SipSession) session));
 		}
 		else
 		{
 			_otherSessions = LazyList.remove(_otherSessions, session);
 		}
 	}
 	
 	public CallSession getCallSession() 
 	{
 		return _callSession;
 	}
 	
 	public String getAppId()
 	{
 		return _appId;
 	}
     
 	public void noAck(SipServletRequest request, SipServletResponse response)
 	{
 		SipErrorListener[] listeners = getContext().getSipErrorListeners();
 		if (listeners.length > 0)
 			getContext().fire(listeners, __noAck, new SipErrorEvent(request, response));
 	}
 	
 	public void noPrack(SipServletRequest request, SipServletResponse response)
 	{
 		SipErrorListener[] listeners = getContext().getSipErrorListeners();
 		if (listeners.length > 0)
 			getContext().fire(listeners, __noPrack, new SipErrorEvent(request, response));
 	}
 	
     public ServletTimer newTimer(long delay, boolean persistent, Serializable info)
     {
         checkValid();
         return new Timer(delay, persistent, info);
     }
     
     public ServletTimer newTimer(long delay, long period, boolean fixedDelay, boolean isPersistent, Serializable info)
     {
         checkValid();
         return new Timer(delay, period, fixedDelay, isPersistent, info);
     }
     
     private void addTimer(Timer timer)
     {
     	if (_timers == null)
     		_timers = new ArrayList<ServletTimer>(1);
     	
     	_timers.add(timer);
     }
     
     private void removeTimer(Timer timer)
     {
     	if (_timers != null)
     		_timers.remove(timer);
     }
     
 	public AppSession getAppSession()
 	{
 		return this;
 	}
     
     public String toString()
     {
     	return _appId + "/" + getApplicationName() + "(" + _sessions.size() + ")";
     }
     
     public boolean equals(Object o)
     {
     	if (o == null || !(o instanceof AppSessionIf))
 			return false;
     	AppSession session = ((AppSessionIf) o).getAppSession();
     	return this == session;
     }
     
     public void save(DataOutputStream out)  throws IOException 
     {
     	out.writeUTF(_appId);
     }
     
     public class ExpiryTimeout implements Runnable
     {
 		public void run()
     	{
     		expired();
     	}
     	
     	public String toString()
     	{
     		return "session-timer";
     	}
     }
     
     public class Timer implements ServletTimer, Runnable
     {
 		private Serializable _info;
         private long _period = -1;
         private TimerTask _timerTask;
         private long _executionTime;
         private boolean _persistent;
         
         private String _id = ID.newID(4);
         
         public Timer(long delay, boolean persistent, Serializable info)
         {
             addTimer(this);
             _info = info;
             _executionTime = System.currentTimeMillis() + delay;
             _timerTask = getCallSession().schedule(this, delay);
             _persistent = persistent;
         }
         
         public Timer(long delay, long period, boolean fixedDelay, boolean isPersistent, Serializable info)
         {
             addTimer(this);
             _info = info;
             _period = period;
             _executionTime = System.currentTimeMillis() + delay;
             _timerTask = getCallSession().schedule(this, delay);
             _persistent = isPersistent;
         }
         
         public Timer(long delay, long period, boolean fixedDelay, boolean isPersistent, Serializable info, String id)
         {
             addTimer(this);
             _info = info;
             _period = period;
             _executionTime = System.currentTimeMillis() + delay;
             _timerTask = getCallSession().schedule(this, delay);
             _persistent = isPersistent;
             _id = id;
         }
         
         public SipApplicationSession getApplicationSession()
         {
             return AppSession.this;
         }
 
         public Serializable getInfo()
         {
             return _info;
         }
 
         public long scheduledExecutionTime()
         {
             return _executionTime;
         }
 
         public String getId()
 		{
 			return _id;
 		}
 
 		public long getTimeRemaining()
 		{
 			return _executionTime - System.currentTimeMillis();
 		}
 		
 		public long getPeriod()
 		{
 			return _period;
 		}
 		
 		public boolean isPersistent()
 		{
 			return _persistent;
 		}
 		
         public void cancel()
         {
         	if (_timerTask != null)
         		getCallSession().cancel(_timerTask);
         	_timerTask = null;
         	removeTimer(this);
            _period = -1;
         }
         
         public void run()
         {
         	TimerListener[] listeners = getContext().getTimerListeners();
         	if (listeners.length > 0)
         		getContext().fire(listeners, __timerExpired, this);
 
         	if (_period != -1)
             {
             	_executionTime = System.currentTimeMillis() + _period;
                 _timerTask = getCallSession().schedule(this, _period);
             }
             else
             {
                removeTimer(this);
             }
         }
     }
 }
