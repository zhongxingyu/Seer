 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.wicketstuff.push.timer;
 
 import java.io.Serializable;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.apache.wicket.Application;
 import org.apache.wicket.Component;
 import org.apache.wicket.MetaDataKey;
 import org.apache.wicket.WicketRuntimeException;
 import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.util.time.Duration;
 import org.apache.wicket.util.time.Time;
 import org.wicketstuff.push.IChannelService;
 import org.wicketstuff.push.IPushTarget;
 
 /**
  * Behavior used to enqueue triggers and send them to the client using timer 
  * based polling.
  * <p>
  * The polling interval is configured in the constructor. The more frequent
  * is the polling, the more quickly your client will be updated, but also the
  * more you will load your server and your network.
  * <p>
  * A timeout can also be configured to indicate when the behavior should consider
  * the page has been disconnected. This is important to clean appropriately the 
  * resources associated with the page.
  * 
  * @author Xavier Hanin
  * 
  * @see IChannelService
  * @see TimerChannelService
  * @see TimerPushService
  */
 public class TimerChannelBehavior extends AbstractAjaxTimerBehavior 
 	implements Serializable
 {
 	private static final long serialVersionUID = 1L;
 	
 	private static final AtomicLong COUNTER = new AtomicLong();
 	
 	private static Method[] methods;
 
 	private static final int ADD_COMPONENT_METHOD = 0;
 	
 	private static final int ADD_COMPONENT_WITH_MARKUP_ID_METHOD = 1;
 
 	private static final int APPEND_JAVASCRIPT_METHOD = 2;
 
 	private static final int PREPEND_JAVASCRIPT_METHOD = 3;
 
 	private static final int FOCUS_COMPONENT_METHOD = 4;
 
 	/**
 	 * The default margin after a polling interval to consider the page is disconnected
 	 */
 	private static final Duration TIMEOUT_MARGIN = Duration.seconds(5);
 	
 	static {
 		try
 		{
 			methods = new Method[] {
 				AjaxRequestTarget.class.getMethod("addComponent", new Class[] {Component.class}),
 				AjaxRequestTarget.class.getMethod("addComponent", new Class[] {Component.class, String.class}),
 				AjaxRequestTarget.class.getMethod("appendJavascript", new Class[] {String.class}),
 				AjaxRequestTarget.class.getMethod("prependJavascript", new Class[] {String.class}),
 				AjaxRequestTarget.class.getMethod("focusComponent", new Class[] {Component.class}),
 			};
 		}
 		catch (Exception e)
 		{
 			throw new WicketRuntimeException("Unable to initialize DefaultAjaxPushBehavior", e);
 		}
 	}
 	
 	/**
 	 * This class is used to store a list of delayed method calls.
 	 * 
 	 * The method calls are actually calls to methods on {@link AjaxRequestTarget},
 	 * which are invoked when the client polls the server.
 	 * 
 	 * @author Xavier Hanin
 	 */
 	private static class DelayedMethodCallList implements Serializable {
 		private static final long serialVersionUID = 1L;
 
 		/**
 		 * Used to store a method and its parameters to be later invoked on an object.
 		 * 
 		 * @author Xavier Hanin
 		 */
 		private static class DelayedMethodCall implements Serializable {
 			private static final long serialVersionUID = 1L;
 			
 			/**
 			 * The index of the method to invoke
 			 * We store only an index to avoid serialization issues
 			 */
 			private final int m;
 			/**
 			 * the parameters to use when the method is called
 			 */
 			private final Object[] parameters;
 
 			/**
 			 * Construct.
 			 * @param m the index of the method to be called
 			 * @param parameters the parameters to use when the method is called
 			 */
 			public DelayedMethodCall(int m, Object[] parameters)
 			{
 				this.m = m;
 				this.parameters = parameters;
 			}
 
 			/**
 			 * Invokes the method with the parameters on the given object.
 			 * 
 			 * @see java.lang.reflect.Method#invoke(Object, Object[])
 			 * @param o the object on which the method should be called
 			 * @throws IllegalArgumentException
 			 * @throws IllegalAccessException
 			 * @throws InvocationTargetException
 			 */
 			public void invoke(Object o) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
 			{
 				methods[m].invoke(o, parameters);
 			}
 		}
 		/**
 		 * stores the list of {@link DelayedMethodCall} to invoke
 		 */
 		private final List<DelayedMethodCall> calls;
 		
 		/**
 		 * Construct.
 		 */
 		public DelayedMethodCallList()
 		{
 			calls = new ArrayList<DelayedMethodCall>();
 		}
 
 		/**
 		 * Construct a copy of the given {@link DelayedMethodCallList}.
 		 * @param dmcl
 		 */
 		public DelayedMethodCallList(DelayedMethodCallList dmcl)
 		{
 			calls = new ArrayList<DelayedMethodCall>(dmcl.calls);
 		}
 		
 		/**
 		 * Add a {@link DelayedMethodCall} to the list
 		 * @param m the index of the method to be later invoked
 		 * @param parameters the parameters to use when the method will be invoked
 		 */
 		public void addCall(int m, Object[] parameters) {
 			calls.add(new DelayedMethodCall(m, parameters));
 		}
 		
 		/**
 		 * Invokes all the {@link DelayedMethodCall} in the list on the given Object
 		 * 
 		 * @see java.lang.reflect.Method#invoke(Object, Object[])
 		 * @param o the object on which delayed methods should be called
 		 * @throws IllegalArgumentException
 		 * @throws IllegalAccessException
 		 * @throws InvocationTargetException
 		 */
 		public void invoke(Object o) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
 			for (Iterator iter = calls.iterator(); iter.hasNext();)
 			{
 				DelayedMethodCall dmc = (DelayedMethodCall)iter.next();
 				dmc.invoke(o);
 			}
 		}
 
 		/**
 		 * Indicates if this list is empty or not
 		 * @return true if this list is empty, false otherwise
 		 */
 		public boolean isEmpty()
 		{
 			return calls.isEmpty();
 		}
 
 		/**
 		 * Used to remove all the delayed methods from this list
 		 */
 		public void clear()
 		{
 			calls.clear();
 		}
 	}
 	
 	/**
 	 * An {@link IPushTarget} implementation which enqueue {@link DelayedMethodCallList},
 	 * also called triggers, for a {@link TimerChannelBehavior} identified by its id.
 	 * 
 	 * TimerPushTarget are thread safe, and can be used from any thread. 
 	 * Since it is not serializable, it is not intended to be stored in a wicket component.
 	 * 
 	 * @author Xavier Hanin
 	 */
 	public static class TimerPushTarget implements IPushTarget {
 		/**
 		 * A trigger currently being constructed, waiting for a call to trigger to go
 		 * to the triggers list.
 		 */
 		private final DelayedMethodCallList currentTrigger = new DelayedMethodCallList();
 		/**
 		 * The Wicket Application in which this target is used
 		 */
 		private final Application application;
 		/**
 		 * The id of the behavior to which this target corresponds
 		 */
 		private final String id;
 		/**
 		 * The duration to wait before considering that a page is not connected any more
 		 * This is usually set to the polling interval + a safety margin
 		 */
 		private final Duration timeout;
 		/**
 		 * Target creation time
 		 */
 		private final Time timestamp;
 
 
 		public TimerPushTarget(Application application, String id, Duration timeout) 
 		{
 			super();
 			this.application = application;
 			this.id = id;
 			this.timeout = timeout;
 			this.timestamp = Time.now();
 		}
 
 		/**
 		 * @see IAjaxPushBehavior#addComponent(Component)
 		 */
 		public void addComponent(Component component)
 		{
 			synchronized(currentTrigger) { 
 				currentTrigger.addCall(ADD_COMPONENT_METHOD, new Object[] {component});
 			}
 		}
 
 		/**
 		 * @see IAjaxPushBehavior#addComponent(Component, String)
 		 */
 		public void addComponent(Component component, String markupId)
 		{
 			synchronized(currentTrigger) { 
 				currentTrigger.addCall(ADD_COMPONENT_WITH_MARKUP_ID_METHOD, new Object[] {component, markupId});
 			}
 		}
 
 		/**
 		 * @see IAjaxPushBehavior#appendJavascript(String)
 		 */
 		public void appendJavascript(String javascript)
 		{
 			synchronized(currentTrigger) { 
 				currentTrigger.addCall(APPEND_JAVASCRIPT_METHOD, new Object[] {javascript});
 			}
 		}
 
 		/**
 		 * @see IAjaxPushBehavior#focusComponent(Component)
 		 */
 		public void focusComponent(Component component)
 		{
 			synchronized(currentTrigger) { 
 				currentTrigger.addCall(FOCUS_COMPONENT_METHOD, new Object[] {component});
 			}
 		}
 
 		/**
 		 * @see IAjaxPushBehavior#prependJavascript(String)
 		 */
 		public void prependJavascript(String javascript)
 		{
 			synchronized(currentTrigger) { 
 				currentTrigger.addCall(PREPEND_JAVASCRIPT_METHOD, new Object[] {javascript});
 			}
 		}
 
 		/**
 		 * @see IAjaxPushBehavior#trigger()
 		 */
 		public void trigger()
 		{
 			DelayedMethodCallList trigger = null;
 			synchronized(currentTrigger) {
 				if (currentTrigger.isEmpty()) {
 					return;
 				}
 				trigger = new DelayedMethodCallList(currentTrigger);
 				currentTrigger.clear();
 			}
 			List<DelayedMethodCallList> triggers = getTriggers();
 			synchronized (triggers)
 			{
 				triggers.add(trigger);
 			}
 		}
 		
 		public boolean isConnected() 
 		{
 			Time time = TimerChannelBehavior.getLastPollEvent(application, id);
 			boolean isConnected;
 			if (time == null) 
 			{
 				// the behavior has not been polled yet (or maybe it has been cleaned)
 				// we can't know exactly the reason, so we check the timeout against this 
 				// PushTarget creation timestamp
 				time = timestamp;
 			}
 			isConnected = time.elapsedSince().compareTo(timeout) < 0;
 			if (!isConnected) 
 			{
 				// timeout expired, the page is probably not connected anymore
 
 				// we clean the metadata to avoid memory leak
 				TimerChannelBehavior.cleanMetadata(application, id);
 			}
 			return isConnected;
 		}
 
 		/**
 		 * Methods used to access the triggers queued for the the behavior to which this target corresponds.
 		 * 
 		 * @return a List of triggers queued for the current component
 		 */
 		private List<DelayedMethodCallList> getTriggers() 
 		{
 			return TimerChannelBehavior.getTriggers(application, id);
 		}
 	}
 	
 	private final String id;
 	private final Duration updateInterval;
 	private final Duration timeout;
 
 	/**
 	 * Construct a TimerChannelBehavior which actually refreshes the clients
 	 * by polling the server for changes at the given duration.
 	 * 
 	 * @param updateInterval the interval at which the server should be polled for changes
 	 */
 	public TimerChannelBehavior(Duration updateInterval)
 	{
 		this(updateInterval, updateInterval.add(TIMEOUT_MARGIN));
 	}
 
 	/**
 	 * Construct a TimerChannelBehavior which actually refreshes the clients
 	 * by polling the server for changes at the given duration.
 	 * 
 	 * @param updateInterval the interval at which the server should be polled for changes
 	 */
 	public TimerChannelBehavior(Duration updateInterval, Duration timeout)
 	{
 		super(updateInterval);
 		id = String.valueOf(COUNTER.incrementAndGet());
 		this.timeout = timeout;
 		this.updateInterval = updateInterval;
 	}
 	
 	
 
 	/**
 	 * @see AbstractAjaxTimerBehavior#onTimer(AjaxRequestTarget)
 	 */
 	protected void onTimer(AjaxRequestTarget target)
 	{
 		touch(getComponent().getApplication(), id);
 		List<DelayedMethodCallList> triggers = getTriggers(getComponent().getApplication(), id);
 		List<DelayedMethodCallList> triggersCopy;
 		synchronized (triggers)
 		{
 			if (triggers.isEmpty()) {
 				return;
 			}
 			triggersCopy = new ArrayList<DelayedMethodCallList>(triggers);
 			triggers.clear();
 		}
 		for (Iterator iter = triggersCopy.iterator(); iter.hasNext();)
 		{
 			DelayedMethodCallList dmcl = (DelayedMethodCallList)iter.next();
 			try
 			{
 				dmcl.invoke(target);
 			}
 			catch (Exception e)
 			{
 				throw new WicketRuntimeException("a problem occured while adding events to AjaxRequestTarget", e);
 			}
 		}
 	}
 	
 	/**
 	 * Creates a new push target to which triggers can be sent
 	 * @return an IPushTarget to which triggers can be sent in any thread.
 	 */
 	public IPushTarget newPushTarget() {
 		return new TimerPushTarget(getComponent().getApplication(), id, timeout);
 	}
 
 	
 	@Override
 	public void renderHead(IHeaderResponse response) {
 		String timerChannelPageId = getComponent().getPage().getId()+":updateInterval:"+updateInterval;
 		if (!response.wasRendered(timerChannelPageId)) 
 		{
 			super.renderHead(response);
 			setRedirectId(getComponent().getApplication(), timerChannelPageId, id);
 			response.markRendered(timerChannelPageId);
 		} else {
 			/*
 			 * A similar behavior has already been rendered, we have no need to render ourself
 			 * All we need is redirect our own behavior id to the id of the behavior which has 
 			 * been rendered. 
 			 */
 			String redirectedId = getPageId(getComponent().getApplication(), timerChannelPageId);
 			setRedirectId(getComponent().getApplication(), id, redirectedId);
 		}
 	}
 
 	/**
 	 * Meta data key for queued triggers, stored by page behavior id
 	 */
 	static final MetaDataKey TRIGGERS_KEY = new MetaDataKey(Map.class)
 	{
 		private static final long serialVersionUID = 1L;
 	};
 
 	/**
 	 * Meta data key for poll events time, stored by page behavior id
 	 */
 	static final MetaDataKey EVENTS_KEY = new MetaDataKey(Map.class)
 	{
 		private static final long serialVersionUID = 1L;
 	};
 	
 	/**
 	 * Meta data key for page behavior ids, stored by behavior id
 	 */
 	static final MetaDataKey PAGE_ID_KEY = new MetaDataKey(Map.class)
 	{
 		private static final long serialVersionUID = 1L;
 	};
 	
 	/**
 	 * Methods used to access the triggers queued for the behavior
 	 * 
 	 * The implementation uses a Map stored in the application, where the behavior id is the key,
 	 * because these triggers cannot be stored in component instance or the behavior itself,
 	 * since they may be serialized and deserialized.
 	 * @param application the application in which the triggers are stored
 	 * @param id the id of the behavior
 	 * 
 	 * @return a List of triggers queued for the component
 	 */
 	private static List<DelayedMethodCallList> getTriggers(Application application, String id) 
 	{
 		String realId = id;
 		id = getPageId(application, id);
 		ConcurrentMap<String, List<DelayedMethodCallList>> triggersById;
 		synchronized (application) 
 		{
 			triggersById = 
 					(ConcurrentMap<String, List<DelayedMethodCallList>>) application.getMetaData(TRIGGERS_KEY);
 			if (triggersById == null) 
 			{
 				triggersById = new ConcurrentHashMap<String, List<DelayedMethodCallList>>();
 				application.setMetaData(TRIGGERS_KEY, (Serializable) triggersById);
 			}
 		}
 		List<DelayedMethodCallList> triggers = triggersById.get(id);
 		if (triggers == null) 
 		{
 			triggersById.putIfAbsent(id, new ArrayList<DelayedMethodCallList>());
 			triggers = triggersById.get(id);
 		}
 		return triggers;
 	}
 	
 	/**
 	 * Cleans the metadata (triggers, poll time) associated with a given behavior id
 	 * @param application the application in which the metadata are stored
 	 * @param id the id of the behavior
 	 */
 	private static void cleanMetadata(Application application, String id)
 	{
 		id = getPageId(application, id);
 		ConcurrentMap<String, List<DelayedMethodCallList>> triggersById = null;
 		ConcurrentMap<String, Time> eventsTimeById = null;
 		ConcurrentMap<String, String> pageIdsById = null;
 		synchronized (application) 
 		{
 			triggersById = 
 					(ConcurrentMap<String, List<DelayedMethodCallList>>) application.getMetaData(TRIGGERS_KEY);
 			eventsTimeById = 
 				(ConcurrentMap<String, Time>) application.getMetaData(EVENTS_KEY);
 			pageIdsById = 
 				(ConcurrentMap<String, String>) application.getMetaData(PAGE_ID_KEY);
 		}
 		if (triggersById != null) 
 		{
 			List<DelayedMethodCallList> triggers = triggersById.remove(id);
 			if (triggers != null) 
 			{
 				synchronized (triggers)
 				{
 					triggers.clear();
 				}
 			}
 		}
 		if (eventsTimeById != null) 
 		{
 			eventsTimeById.remove(id);
 		}
 		if (pageIdsById != null)
 		{
 			pageIdsById.remove(id);
 		}
 	}
 	
 	private static void touch(Application application, String id)
 	{
 		id = getPageId(application, id);
 		ConcurrentMap<String, Time> eventsTimeById;
 		synchronized (application) 
 		{
 			eventsTimeById = 
 					(ConcurrentMap<String, Time>) application.getMetaData(EVENTS_KEY);
 			if (eventsTimeById == null) 
 			{
 				eventsTimeById = new ConcurrentHashMap<String, Time>();
 				application.setMetaData(EVENTS_KEY, (Serializable) eventsTimeById);
 			}
 		}
 		eventsTimeById.put(id, Time.now());
 	}
 	
 	private static Time getLastPollEvent(Application application, String id) 
 	{
 		String realId = id;
 		id = getPageId(application, id);
 		ConcurrentMap<String, Time> eventsTimeById;
 		synchronized (application) 
 		{
 			eventsTimeById = 
 					(ConcurrentMap<String, Time>) application.getMetaData(EVENTS_KEY);
 			if (eventsTimeById == null) 
 			{
 				return null;
 			}
 		}
 		Time time = eventsTimeById.get(id);
 		return time;
 	}
 	
 	/**
 	 * Returns the page behavior id corresponding the given behavior id.
 	 * Only one behavior is actually rendered on a page for the same updateInterval,
 	 * to optimize the number of requests. Therefore all timer channel behaviors 
 	 * of the same page are redirected to the same id, using this method.
 	 * 
 	 * @param application the wicket application to which the behavior belong
 	 * @param id the id of the behavior for which the page behavior id should be found
 	 * @return the page behavior id corresponding the given behavior id.
 	 */
 	private static String getPageId(Application application, String id) {
 		ConcurrentMap<String, String> pageIdsById;
 		synchronized (application) 
 		{
 			pageIdsById = 
 					(ConcurrentMap<String, String>) application.getMetaData(PAGE_ID_KEY);
 			if (pageIdsById == null) 
 			{
 				return id;
 			}
 		}
 		String pageId = pageIdsById.get(id);
 		return pageId == null?id:pageId;
 	}
 
 	private static void setRedirectId(Application application, String id, String redirectedId) {
 		ConcurrentMap<String, String> pageIdsById;
 		synchronized (application) 
 		{
 			pageIdsById = 
 					(ConcurrentMap<String, String>) application.getMetaData(PAGE_ID_KEY);
 			if (pageIdsById == null) 
 			{
 				pageIdsById = new ConcurrentHashMap<String, String>();
 				application.setMetaData(PAGE_ID_KEY, (Serializable) pageIdsById);
 			}
 		}
 		String oldRedirectedId = pageIdsById.put(id, redirectedId);
 		if (!redirectedId.equals(oldRedirectedId)) 
 		{
 			/*
 			 * The id was not already redirected to the redirectedId, we need
 			 * to merge the information before redirection with information 
 			 * after redirection
 			 */
 			String idToRedirect = oldRedirectedId == null?id:oldRedirectedId;
 			redirect(application, idToRedirect, redirectedId);
 		}
 	}
 
 	private static void redirect(Application application, String idToRedirect,
 			String redirectedId) {
 		ConcurrentMap<String, List<DelayedMethodCallList>> triggersById = null;
 		ConcurrentMap<String, Time> eventsTimeById = null;
 		synchronized (application) 
 		{
 			triggersById = 
 					(ConcurrentMap<String, List<DelayedMethodCallList>>) application.getMetaData(TRIGGERS_KEY);
 			eventsTimeById = 
 				(ConcurrentMap<String, Time>) application.getMetaData(EVENTS_KEY);
 		}
 		if (triggersById != null) 
 		{
 			List<DelayedMethodCallList> triggersToRedirect = triggersById.remove(idToRedirect);
 			if (triggersToRedirect != null) 
 			{
 				// we redirect triggers to the new list, in two steps, to avoid acquiring 
 				// locks on two triggers simultaneously, which would be a source of risk of 
 				// dead locks
 				List<DelayedMethodCallList> triggersToRedirectCopy;
 				synchronized (triggersToRedirect)
 				{
 					triggersToRedirectCopy = new ArrayList<DelayedMethodCallList>(triggersToRedirect);
 					triggersToRedirect.clear();
 				}
 				if (!triggersToRedirectCopy.isEmpty()) {
 					List<DelayedMethodCallList> triggers = getTriggers(application, redirectedId);
 					synchronized (triggers) 
 					{
 						triggers.addAll(triggersToRedirectCopy);
 					}
 				}
 			}
 		}
 		if (eventsTimeById != null) 
 		{
 			eventsTimeById.remove(idToRedirect);
 			/* 
 			 * we don't need to merge touch information, since merged behaviors
 			 * always have the same touch rates
 			 */ 
 		}
 	}
 
 	public Duration getUpdateInterval() {
 		return updateInterval;
 	}
 }
