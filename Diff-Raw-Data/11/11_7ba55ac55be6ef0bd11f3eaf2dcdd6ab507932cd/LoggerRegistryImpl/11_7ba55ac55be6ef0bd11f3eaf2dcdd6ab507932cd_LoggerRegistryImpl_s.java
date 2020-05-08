 /*
  * Copyright 2010 NCHOVY
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
 package org.araqne.log.api.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.confdb.Config;
 import org.araqne.confdb.ConfigDatabase;
 import org.araqne.confdb.ConfigService;
 import org.araqne.confdb.Predicates;
 import org.araqne.log.api.Log;
 import org.araqne.log.api.LogPipe;
 import org.araqne.log.api.Logger;
 import org.araqne.log.api.LoggerEventListener;
 import org.araqne.log.api.LoggerFactory;
 import org.araqne.log.api.LoggerFactoryEventListener;
 import org.araqne.log.api.LoggerFactoryRegistryEventListener;
 import org.araqne.log.api.LoggerRegistry;
 import org.araqne.log.api.LoggerRegistryEventListener;
 import org.araqne.log.api.LoggerStopReason;
 
 @Component(name = "logger-registry")
 @Provides(specifications = { LoggerRegistry.class })
 public class LoggerRegistryImpl implements LoggerRegistry, LoggerFactoryRegistryEventListener, LoggerFactoryEventListener,
 		LogPipe {
 	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerRegistryImpl.class.getName());
 
 	@Requires
 	private ConfigService conf;
 
 	private ConcurrentMap<String, Logger> loggers;
 	private Set<LoggerRegistryEventListener> callbacks;
 	private ConcurrentMap<String, Set<LogPipe>> pipeMap;
 	private boolean isOpen = false;
 
 	private Map<String, Set<String>> dependencies;
 	private DependencyResolver resolver;
 
 	public LoggerRegistryImpl() {
 		loggers = new ConcurrentHashMap<String, Logger>();
 		callbacks = Collections.newSetFromMap(new ConcurrentHashMap<LoggerRegistryEventListener, Boolean>());
 		pipeMap = new ConcurrentHashMap<String, Set<LogPipe>>();
 		dependencies = new HashMap<String, Set<String>>();
 		resolver = new DependencyResolver();
 	}
 
 	@Override
 	public boolean isOpen() {
 		return isOpen;
 	}
 
 	@Validate
 	public void start() {
 		callbacks.clear();
 		dependencies.clear();
 
 		ConfigDatabase db = conf.ensureDatabase("araqne-log-api");
 		Collection<LoggerDependency> docs = db.findAll(LoggerDependency.class).getDocuments(LoggerDependency.class);
 
 		synchronized (dependencies) {
 			for (LoggerDependency d : docs) {
 				Set<String> s = dependencies.get(d.getSource());
 				if (s == null) {
 					s = new HashSet<String>();
 					dependencies.put(d.getSource(), s);
 				}
 				s.add(d.getLogger());
 			}
 		}
 		addListener(resolver);
 
 		isOpen = true;
 	}
 
 	@Invalidate
 	public void stop() {
 		removeListener(resolver);
 
 		isOpen = false;
 		callbacks.clear();
 		dependencies.clear();
 	}
 
 	@Override
 	public void factoryAdded(LoggerFactory loggerFactory) {
 		loggerFactory.addListener(this);
 	}
 
 	@Override
 	public void factoryRemoved(LoggerFactory loggerFactory) {
 		loggerFactory.removeListener(this);
 
 		// remove all related loggers
 		List<Logger> removeList = new ArrayList<Logger>();
 		for (Logger logger : loggers.values())
 			if (logger.getFactoryFullName().equals(loggerFactory.getFullName()))
 				removeList.add(logger);
 
 		for (Logger logger : removeList) {
 			try {
 				// logger stop event caused by factory removal will not be sent.
 				logger.clearEventListeners();
 				logger.stop(LoggerStopReason.FACTORY_DEPENDENCY);
 				removeLogger(logger);
 			} catch (Exception e) {
 				log.warn("araqne-log-api: logger remove error", e);
 			}
 		}
 	}
 
 	@Override
 	public Collection<Logger> getLoggers() {
 		return new ArrayList<Logger>(loggers.values());
 	}
 
 	@Override
 	public Logger getLogger(String fullName) {
 		return loggers.get(fullName);
 	}
 
 	@Override
 	public Logger getLogger(String namespace, String name) {
 		return loggers.get(namespace + "\\" + name);
 	}
 
 	@Override
 	public void addLogger(Logger logger) {
 		log.debug("araqne log api: adding logger [{}]", logger.getFullName());
 		Logger old = loggers.putIfAbsent(logger.getFullName(), logger);
 		if (old != null)
 			throw new IllegalStateException("logger already exists: " + logger.getFullName());
 
 		// connect pipe
 		logger.addLogPipe(this);
 		logger.addEventListener(resolver);
 
 		log.debug("araqne log api: logger [{}] added", logger.getFullName());
 
 		// invoke logger event callbacks
 		for (LoggerRegistryEventListener callback : callbacks) {
 			try {
 				callback.loggerAdded(logger);
 			} catch (Exception e) {
 				log.warn("araqne-log-api: logger registry callback should not throw exception", e);
 			}
 		}
 	}
 
 	@Override
 	public void removeLogger(Logger logger) {
 		if (logger == null)
 			throw new IllegalArgumentException("logger must not be null");
 
 		log.debug("araqne log api: removing logger [{}]", logger.getFullName());
 
 		if (logger.isRunning())
 			throw new IllegalStateException("logger is still running");
 
 		loggers.remove(logger.getFullName());
 
 		// disconnect pipe
 		logger.removeLogPipe(this);
 		logger.removeEventListener(resolver);
 
 		log.debug("araqne log api: logger [{}] removed", logger.getFullName());
 
 		// invoke logger event callbacks
 		for (LoggerRegistryEventListener callback : callbacks) {
 			try {
 				callback.loggerRemoved(logger);
 			} catch (Exception e) {
 				log.warn("araqne-log-api: logger registry callback should not throw exception", e);
 			}
 		}
 	}
 
 	@Override
 	public void loggerCreated(LoggerFactory factory, Logger logger, Map<String, String> config) {
 		addLogger(logger);
 	}
 
 	@Override
 	public void loggerDeleted(LoggerFactory factory, Logger logger) {
 		if (logger != null)
 			removeLogger(logger);
 	}
 
 	@Override
 	public void addLogPipe(String loggerFactoryName, LogPipe pipe) {
 		Set<LogPipe> pipes = Collections.newSetFromMap(new ConcurrentHashMap<LogPipe, Boolean>());
 		Set<LogPipe> oldPipes = pipeMap.putIfAbsent(loggerFactoryName, pipes);
 		if (oldPipes != null)
 			pipes = oldPipes;
 
 		pipes.add(pipe);
 	}
 
 	@Override
 	public void removeLogPipe(String loggerFactoryName, LogPipe pipe) {
 		Set<LogPipe> pipes = pipeMap.get(loggerFactoryName);
 		if (pipes == null)
 			return;
 
 		pipes.remove(pipe);
 	}
 
 	@Override
 	public void addListener(LoggerRegistryEventListener callback) {
 		if (callback == null)
 			throw new IllegalArgumentException("callback must not be null");
 
 		callbacks.add(callback);
 	}
 
 	@Override
 	public void removeListener(LoggerRegistryEventListener callback) {
 		if (callback == null)
 			throw new IllegalArgumentException("callback must not be null");
 
 		callbacks.remove(callback);
 	}
 
 	@Override
 	public Set<String> getDependencies(String fullName) {
 		Set<String> s = Collections.emptySet();
 		synchronized (dependencies) {
 			Set<String> d = dependencies.get(fullName);
 			if (d != null) {
 				s = new HashSet<String>(d);
 			}
 		}
 		return s;
 	}
 
 	@Override
 	public boolean hasDependency(String fullName, String sourceFullName) {
 		synchronized (dependencies) {
 			Set<String> s = dependencies.get(fullName);
 			return s.contains(sourceFullName);
 		}
 	}
 
 	@Override
 	public void addDependency(String fullName, String sourceFullName) {
 		if (fullName == null)
 			throw new IllegalArgumentException("fullname should not be null");
 
 		if (sourceFullName == null)
 			throw new IllegalArgumentException("source fullname should not be null");
 
 		synchronized (dependencies) {
 			Set<String> s = dependencies.get(sourceFullName);
 			if (s == null) {
 				s = new HashSet<String>();
 				dependencies.put(sourceFullName, s);
 			}
 
 			if (!s.add(fullName))
 				return;
 
 			LoggerDependency dependency = new LoggerDependency(fullName, sourceFullName);
 			ConfigDatabase db = conf.ensureDatabase("araqne-log-api");
 			Config c = db.findOne(LoggerDependency.class, Predicates.field(dependency.marshal()));
 			if (c != null)
 				return;
 
 			db.add(dependency);
 		}
 	}
 
 	@Override
 	public void removeDependency(String fullName, String sourceFullName) {
 		if (fullName == null) {
 			throw new IllegalArgumentException("fullname should not be null");
 		}
 		if (sourceFullName == null) {
 			throw new IllegalArgumentException("source fullname should not be null");
 		}
 		synchronized (dependencies) {
 			Set<String> s = dependencies.get(sourceFullName);
 			if (s != null) {
 				s.remove(fullName);
 			}
 
 			Logger sourceLogger = loggers.get(sourceFullName);
 			if (sourceLogger != null)
 				sourceLogger.removeUnresolvedLogger(fullName);
 
 			LoggerDependency dependency = new LoggerDependency(fullName, sourceFullName);
 			ConfigDatabase db = conf.ensureDatabase("araqne-log-api");
 			Config c = db.findOne(LoggerDependency.class, Predicates.field(dependency.marshal()));
 			if (c == null) {
 				return;
 			}
 			db.remove(c);
 		}
 	}
 
 	@Override
 	public void onLog(Logger logger, Log log) {
 		Set<LogPipe> pipes = pipeMap.get(logger.getFactoryName());
 		if (pipes == null)
 			return;
 
 		for (LogPipe pipe : pipes) {
 			pipe.onLog(logger, log);
 		}
 	}
 
 	@Override
 	public void onLogBatch(Logger logger, Log[] logs) {
 		Set<LogPipe> pipes = pipeMap.get(logger.getFactoryName());
 		if (pipes == null)
 			return;
 
 		for (LogPipe pipe : pipes) {
 			pipe.onLogBatch(logger, logs);
 		}
 	}
 
 	private class DependencyResolver implements LoggerEventListener, LoggerRegistryEventListener {
 		private DependencyResolver() {
 		}
 
 		public void loggerAdded(org.araqne.log.api.Logger logger) {
 			log.debug("aranqe log api: dependency resolver detected new logger [{}]", logger.getFullName());
 
 			// stopped logger should be resolved immediately (do not interfere
 			// source logger start)
 			if (logger.isEnabled())
 				return;
 
 			for (org.araqne.log.api.Logger l : loggers.values()) {
 				if ((l != logger) && (!l.getFullName().equals(logger.getFullName()))) {
 					l.removeUnresolvedLogger(logger.getFullName());
 				}
 			}
 		}
 
 		public void loggerRemoved(Logger logger) {
 			log.debug("aranqe log api: dependency resolver detected logger [{}] removal", logger.getFullName());
 			synchronized (dependencies) {
 				for (Entry<String, Set<String>> e : dependencies.entrySet()) {
 					String source = (String) e.getKey();
 					Set<String> depends = (Set<String>) e.getValue();
 
 					// cannot determine permanent or transient here. factory
 					// should remove unresolved logger from source logger using
 					// removeDependency()
 					if (depends.contains(logger.getFullName())) {
 						Logger sourceLogger = (Logger) loggers.get(source);
 						sourceLogger.addUnresolvedLogger(logger.getFullName());
 					}
 				}
 			}
 		}
 
 		public void onStart(Logger logger) {
 			log.debug("aranqe log api: dependency resolver detected logger [{}] start", logger.getFullName());
			for (org.araqne.log.api.Logger l : loggers.values()) {
 				if ((l != logger) && (!l.getFullName().equals(logger.getFullName()))) {
 					l.removeUnresolvedLogger(logger.getFullName());
 				}
 			}
 		}
 
 		public void onStop(Logger logger, LoggerStopReason reason) {
 			log.debug("aranqe log api: dependency resolver detected logger [{}] stop", logger.getFullName());
 		}
 
 		public void onSetTimeRange(Logger logger) {
 		}
 
 		public void onUpdated(Logger logger, Map<String, String> config) {
 		}
 	}
 }
