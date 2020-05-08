 /*
  * Copyright 2014 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.cep.engine;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.cron.AbstractTickTimer;
 import org.araqne.cron.TickService;
 import org.araqne.logdb.cep.Event;
 import org.araqne.logdb.cep.EventCause;
 import org.araqne.logdb.cep.EventClock;
 import org.araqne.logdb.cep.EventContext;
 import org.araqne.logdb.cep.EventContextListener;
 import org.araqne.logdb.cep.EventContextService;
 import org.araqne.logdb.cep.EventContextStorage;
 import org.araqne.logdb.cep.EventKey;
 import org.araqne.logdb.cep.EventSubscriber;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component(name = "mem-event-ctx-storage")
 public class MemoryEventContextStorage implements EventContextStorage, EventContextListener {
 	private final Logger slog = LoggerFactory.getLogger(MemoryEventContextStorage.class);
 
 	@Requires
 	private EventContextService eventContextService;
 
 	@Requires
 	private TickService tickService;
 
 	private ConcurrentHashMap<EventKey, EventContext> contexts;
 
 	// topic to subscribers
 	private ConcurrentHashMap<String, CopyOnWriteArraySet<EventSubscriber>> subscribers;
 
 	// log tick host to aging context mappings
 	private ConcurrentHashMap<String, EventClock> logClocks;
 	private EventClock realClock;
 
 	private RealClockTask realClockTask = new RealClockTask();
 
 	@Override
 	public String getName() {
 		return "mem";
 	}
 
 	@Validate
 	public void start() {
 		contexts = new ConcurrentHashMap<EventKey, EventContext>();
 		subscribers = new ConcurrentHashMap<String, CopyOnWriteArraySet<EventSubscriber>>();
 		logClocks = new ConcurrentHashMap<String, EventClock>();
 		realClock = new EventClock(this, "real", System.currentTimeMillis(), 10000);
 
 		tickService.addTimer(realClockTask);
 		eventContextService.registerStorage(this);
 	}
 
 	@Invalidate
 	public void stop() {
 		if (eventContextService != null) {
 			eventContextService.unregisterStorage(this);
 		}
 
 		tickService.removeTimer(realClockTask);
 		contexts.clear();
 		subscribers.clear();
 	}
 
 	@Override
 	public Set<String> getHosts() {
 		return new HashSet<String>(logClocks.keySet());
 	}
 
 	@Override
 	public EventClock getClock(String host) {
 		return logClocks.get(host);
 	}
 
 	@Override
 	public Set<EventKey> getContextKeys() {
 		return new HashSet<EventKey>(contexts.keySet());
 	}
 
 	@Override
 	public Set<EventKey> getContextKeys(String topic) {
 		HashSet<EventKey> keys = new HashSet<EventKey>();
 
 		for (EventKey key : contexts.keySet()) {
 			if (key.getTopic().equals(topic))
 				keys.add(key);
 		}
 
 		return keys;
 	}
 
 	@Override
 	public EventContext getContext(EventKey key) {
 		return contexts.get(key);
 	}
 
 	@Override
 	public EventContext addContext(EventContext ctx) {
 		EventContext old = contexts.putIfAbsent(ctx.getKey(), ctx);
 		if (old == null) {
 			ctx.getListeners().add(this);
 
 			if (ctx.getHost() != null) {
 				EventClock logClock = ensureClock(logClocks, ctx.getHost(), ctx.getCreated());
 				logClock.add(ctx);
 			} else {
 				realClock.add(ctx);
 			}
 
 			return ctx;
 		}
 		return old;
 	}
 
 	private EventClock ensureClock(ConcurrentHashMap<String, EventClock> clocks, String host, long time) {
 		EventClock clock = new EventClock(this, host, time, 11);
 		EventClock old = clocks.putIfAbsent(host, clock);
 		if (old != null)
 			return old;
 		return clock;
 	}
 
 	@Override
 	public void removeContext(EventKey key, EventCause cause) {
 		EventContext ctx = contexts.remove(key);
 		if (ctx != null) {
 			ctx.getListeners().remove(this);
 
 			if (key.getHost() != null) {
 				EventClock logClock = ensureClock(logClocks, ctx.getHost(), ctx.getCreated());
 				logClock.remove(ctx);
 			} else {
 				realClock.remove(ctx);
 			}
 
 			generateEvent(ctx, cause);
 		}
 	}
 
 	@Override
 	public void advanceTime(String host, long logTime) {
 		EventClock logClock = ensureClock(logClocks, host, logTime);
 		logClock.setTime(logTime, false);
 	}
 
 	@Override
 	public void clearClocks() {
 		realClock = new EventClock(this, "real", System.currentTimeMillis(), 10000);
 		logClocks = new ConcurrentHashMap<String, EventClock>();
 	}
 
 	@Override
 	public void clearContexts() {
 		clearContexts(null);
 	}
 
 	@Override
 	public void clearContexts(String topic) {
 		for (EventKey key : getContextKeys()) {
 			if (topic == null || key.getTopic().equals(topic))
 				contexts.remove(key);
 		}
 	}
 
 	@Override
 	public void addSubscriber(String topic, EventSubscriber subscriber) {
 		CopyOnWriteArraySet<EventSubscriber> s = new CopyOnWriteArraySet<EventSubscriber>();
 		CopyOnWriteArraySet<EventSubscriber> old = subscribers.putIfAbsent(topic, s);
 		if (old != null)
 			s = old;
 
 		s.add(subscriber);
 	}
 
 	@Override
 	public void removeSubscriber(String topic, EventSubscriber subscriber) {
 		CopyOnWriteArraySet<EventSubscriber> s = subscribers.get(topic);
 		if (s != null)
 			s.remove(subscriber);
 	}
 
 	private class RealClockTask extends AbstractTickTimer {
 		@Override
 		public int getInterval() {
 			return 100;
 		}
 
 		@Override
 		public void onTick() {
			realClock.setTime(System.currentTimeMillis(), false);
 		}
 	}
 
 	private void generateEvent(EventContext ctx, EventCause cause) {
 		if (slog.isDebugEnabled())
 			slog.debug("araqne logdb cep: generate event ctx [{}] cause [{}]", ctx.getKey(), cause);
 
 		Event ev = new Event(ctx.getKey(), cause);
 		ev.getRows().addAll(ctx.getRows());
 
 		CopyOnWriteArraySet<EventSubscriber> s = subscribers.get(ctx.getKey().getTopic());
 		if (s != null) {
 			for (EventSubscriber subscriber : s) {
 				try {
 					subscriber.onEvent(ev);
 				} catch (Throwable t) {
 					slog.error("araqne logdb cep: subscriber should not throw any exception", t);
 				}
 			}
 		}
 
 		// for wild subscriber
 		s = subscribers.get("*");
 		if (s != null) {
 			for (EventSubscriber subscriber : s) {
 				try {
 					subscriber.onEvent(ev);
 				} catch (Throwable t) {
 					slog.error("araqne logdb cep: subscriber should not throw any exception", t);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onUpdateTimeout(EventContext ctx) {
 		// update per-host case only
 		if (ctx.getHost() == null)
 			return;
 
 		EventClock clock = ensureClock(logClocks, ctx.getHost(), ctx.getCreated());
 		if (clock == null)
 			return;
 
 		clock.updateTimeout(ctx);
 	}
 }
