 package org.eclipse.jetty.nosql.kvs;
 
 //========================================================================
 //Copyright (c) 2011 Intalio, Inc.
 //------------------------------------------------------------------------
 //All rights reserved. This program and the accompanying materials
 //are made available under the terms of the Eclipse Public License v1.0
 //and Apache License v2.0 which accompanies this distribution.
 //The Eclipse Public License is available at
 //http://www.eclipse.org/legal/epl-v10.html
 //The Apache License v2.0 is available at
 //http://www.opensource.org/licenses/apache2.0.php
 //You may elect to redistribute this code under either of these licenses.
 //========================================================================
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.SessionManager;
 import org.eclipse.jetty.server.handler.ContextHandler;
 import org.eclipse.jetty.server.session.AbstractSessionIdManager;
 import org.eclipse.jetty.server.session.SessionHandler;
 import org.eclipse.jetty.util.log.Log;
 import org.eclipse.jetty.util.log.Logger;
 
 /**
  * Based partially on the jdbc session id manager...
  * 
  * Theory is that we really only need the session id manager for the local
  * instance so we have something to scavenge on, namely the list of known ids
  * 
  * this class has a timer that runs at the scavenge delay that runs a query for
  * all id's known to this node and that have and old accessed value greater then
  * the scavengeDelay.
  * 
  * these found sessions are then run through the invalidateAll(id) method that
  * is a bit hinky but is supposed to notify all handlers this id is now DOA and
  * ought to be cleaned up. this ought to result in a save operation on the
  * session that will change the valid field to false (this conjecture is
  * unvalidated atm)
  */
 public abstract class KeyValueStoreSessionIdManager extends AbstractSessionIdManager {
	private final static Logger log = Log.getLogger("org.eclipse.jetty.nosql.memcached.KeyValueStoreSessionIdManager");
 
 	protected Server _server;
 
 	protected long _scavengePeriod = TimeUnit.MINUTES.toMillis(30); // 30 minutes
 
 	protected final Set<String> _sessions = Collections.synchronizedSet(new LinkedHashSet<String>());
 
 	protected String _keyPrefix = "";
 	protected String _keySuffix = "";
 	protected IKeyValueStoreClient _client = null;
 	protected String _serverString = "";
 	protected int _timeoutInMs = 1000;
 
 	/* ------------------------------------------------------------ */
 	public KeyValueStoreSessionIdManager(Server server, String serverString) throws IOException {
 		super(new Random());
 		this._server = server;
 //		this._client = newClient(serverString); // will be initialized on startup
 		this._serverString = serverString;
 	}
 
 	/* ------------------------------------------------------------ */
 	/**
 	 * @deprecated
 	 * Scavenge is a process that periodically checks the tracked session ids of
 	 * this given instance of the session id manager to see if they are past the
 	 * point of expiration.
 	 */
 	protected void scavenge() {
 		return;
 	}
 
 	/* ------------------------------------------------------------ */
 	/**
 	 * @deprecated
 	 * ScavengeFully is a process that periodically checks the tracked session
 	 * ids of this given instance of the session id manager to see if they are
 	 * past the point of expiration.
 	 * 
 	 * NOTE: this is potentially devastating and may lead to serious session
 	 * coherence issues, not to be used in a running cluster
 	 */
 	protected void scavengeFully() {
 		return;
 	}
 
 	/* ------------------------------------------------------------ */
 	/**
 	 * @deprecated
 	 * Purge is a process that cleans the memcached cluster of old sessions that
 	 * are no longer valid.
 	 * 
 	 * There are two checks being done here:
 	 * 
 	 * - if the accessed time is older then the current time minus the purge
 	 * invalid age and it is no longer valid then remove that session - if the
 	 * accessed time is older then the current time minus the purge valid age
 	 * then we consider this a lost record and remove it
 	 * 
 	 * NOTE: if your system supports long lived sessions then the purge valid
 	 * age should be set to zero so the check is skipped.
 	 * 
 	 * The second check was added to catch sessions that were being managed on
 	 * machines that might have crashed without marking their sessions as
 	 * 'valid=false'
 	 */
 	protected void purge() {
 		return;
 	}
 
 	/* ------------------------------------------------------------ */
 	/**
 	 * @deprecated
 	 * Purge is a process that cleans the memcached cluster of old sessions that
 	 * are no longer valid.
 	 * 
 	 */
 	protected void purgeFully() {
 		return;
 	}
 
 	protected abstract AbstractKeyValueStoreClient newClient(String serverString);
 
 	/* ------------------------------------------------------------ */
 	/**
 	 * @deprecated
 	 */
 	public boolean isPurgeEnabled() {
 		return false;
 	}
 
 	/* ------------------------------------------------------------ */
 	/**
 	 * @deprecated
 	 */
 	public void setPurge(boolean purge) {
 		return;
 	}
 
 	/* ------------------------------------------------------------ */
 	/**
 	 * @deprecated
 	 * sets the scavengeDelay
 	 */
 	public void setScavengeDelay(long scavengeDelay) {
 		this._scavengePeriod = scavengeDelay;
 	}
 
 	/* ------------------------------------------------------------ */
 	public void setScavengePeriod(long scavengePeriod) {
 		this._scavengePeriod = scavengePeriod;
 	}
 
 	/* ------------------------------------------------------------ */
 	/**
 	 * @deprecated
 	 */
 	public void setPurgeDelay(long purgeDelay) {
 		return;
 	}
 
 	/* ------------------------------------------------------------ */
 	/**
 	 * @deprecated
 	 */
 	public long getPurgeInvalidAge() {
 		return -1;
 	}
 
 	/* ------------------------------------------------------------ */
 	/**
 	 * @deprecated
 	 * sets how old a session is to be persisted past the point it is no longer
 	 * valid
 	 */
 	public void setPurgeInvalidAge(long purgeValidAge) {
 		return;
 	}
 
 	/* ------------------------------------------------------------ */
 	/**
 	 * @deprecated
 	 */
 	public long getPurgeValidAge() {
 		return -1;
 	}
 
 	/* ------------------------------------------------------------ */
 	/**
 	 * @deprecated
 	 * sets how old a session is to be persist past the point it is considered
 	 * no longer viable and should be removed
 	 * 
 	 * NOTE: set this value to 0 to disable purging of valid sessions
 	 */
 	public void setPurgeValidAge(long purgeValidAge) {
 		return;
 	}
 
 	/* ------------------------------------------------------------ */
 	@Override
 	protected void doStart() throws Exception {
 		log.info("starting...");
 		super.doStart();
 		_client = newClient(_serverString);
 		if (_client == null) {
 			throw new IllegalStateException("newClient(" + _serverString + ") returns null.");
 		}
 		_client.establish();
 		log.info("started.");
 	}
 
 	/* ------------------------------------------------------------ */
 	@Override
 	protected void doStop() throws Exception {
 		log.info("stopping...");
 		if (_client != null) {
 			_client.shutdown();
 			_client = null;
 		}
 		super.doStop();
 		log.info("stopped.");
 	}
 
 	/* ------------------------------------------------------------ */
 	/**
 	 * is the session id known to memcached, and is it valid
 	 */
 	public boolean idInUse(String idInCluster) {
 		byte[] dummy = idInCluster.getBytes(); // dummy string for reserving key
 		return ! addKey(idInCluster, dummy);
 	}
 
 	/* ------------------------------------------------------------ */
 	public void addSession(HttpSession session) {
 		if (session == null) {
 			return;
 		}
 
 		log.debug("addSession:" + session.getId());
 
 		_sessions.add(session.getId());
 	}
 
 	/* ------------------------------------------------------------ */
 	public void removeSession(HttpSession session) {
 		if (session == null) {
 			return;
 		}
 
 		_sessions.remove(session.getId());
 	}
 
 	/* ------------------------------------------------------------ */
 	public void invalidateAll(String sessionId) {
 		_sessions.remove(sessionId);
 		// tell all contexts that may have a session object with this id to
 		// get rid of them
 		Handler[] contexts = _server.getChildHandlersByClass(ContextHandler.class);
 		for (int i = 0; contexts != null && i < contexts.length; i++) {
			SessionHandler sessionHandler = (SessionHandler) ((ContextHandler) contexts[i])
					.getChildHandlerByClass(SessionHandler.class);
 			if (sessionHandler != null) {
 				SessionManager manager = sessionHandler.getSessionManager();
 				if (manager != null && manager instanceof KeyValueStoreSessionManager) {
 					((KeyValueStoreSessionManager) manager).invalidateSession(sessionId);
 				}
 			}
 		}
 	}
 
 	/* ------------------------------------------------------------ */
 	public String getClusterId(String nodeId) {
 		if (nodeId == null) {
 			return null;
 		}
 		int dot = nodeId.lastIndexOf('.');
 		return (dot > 0) ? nodeId.substring(0, dot) : nodeId;
 	}
 
 	/* ------------------------------------------------------------ */
 	public String getNodeId(String clusterId, HttpServletRequest request) {
 		if (clusterId == null) {
 			return null;
 		}
 		if (_workerName != null) {
 			return clusterId + '.' + _workerName;
 		}
 		return clusterId;
 	}
 
 	protected String mangleKey(String key) {
 		return _keyPrefix + key + _keySuffix;
 	}
 
 	protected byte[] getKey(String idInCluster) {
 		log.debug("get: id=" + idInCluster);
 		byte[] raw = null;
 		try {
 			raw = _client.get(mangleKey(idInCluster));
 		} catch (KeyValueStoreClientException error) {
 			log.warn("unable to get key: id=" + idInCluster, error);
 		}
 		return raw;
 	}
 
 	protected boolean setKey(String idInCluster, byte[] raw) {
 		return setKey(idInCluster, raw, getDefaultExpiry());
 	}
 
 	protected boolean setKey(String idInCluster, byte[] raw, int expiry) {
 		if (expiry < 0) {
 			expiry = 0; // 0 means forever
 		}
 		log.debug("set: id=" + idInCluster + ", expiry=" + expiry);
 		boolean result = false;
 		try {
 			result = _client.set(mangleKey(idInCluster), raw, expiry);
 		} catch (KeyValueStoreClientException error) {
 			log.warn("unable to set key: id=" + idInCluster, error);
 		}
 		return result;
 	}
 
 	protected boolean addKey(String idInCluster, byte[] raw) {
 		return addKey(idInCluster, raw, getDefaultExpiry());
 	}
 
 	protected boolean addKey(String idInCluster, byte[] raw, int expiry) {
 		if (expiry < 0) {
 			expiry = 0; // 0 means forever
 		}
 		log.debug("add: id=" + idInCluster + ", expiry=" + expiry);
 		boolean result = false;
 		try {
 			result = _client.add(mangleKey(idInCluster), raw, expiry);
 		} catch (KeyValueStoreClientException error) {
 			log.warn("unable to add key: id=" + idInCluster, error);
 		}
 		return result;
 	}
 
 	protected boolean deleteKey(String idInCluster) {
 		log.debug("delete: id=" + idInCluster);
 		boolean result = false;
 		try {
 			result = _client.delete(mangleKey(idInCluster));
 		} catch (KeyValueStoreClientException error) {
 			log.warn("unable to delete key: id=" + idInCluster, error);
 		}
 		return result;
 	}
 
 	protected Set<String> getSessions() {
 		return Collections.unmodifiableSet(_sessions);
 	}
 
 	public int getDefaultExpiry() {
 		return (int) TimeUnit.MILLISECONDS.toSeconds(_scavengePeriod);
 	}
 
 	public void setDefaultExpiry(int defaultExpiry) {
 		this._scavengePeriod = TimeUnit.SECONDS.toMillis(defaultExpiry);
 	}
 
 	public String getKeyPrefix() {
 		return _keyPrefix;
 	}
 
 	public void setKeyPrefix(String keyPrefix) {
 		this._keyPrefix = keyPrefix;
 	}
 
 	public String getKeySuffix() {
 		return _keySuffix;
 	}
 
 	public void setKeySuffix(String keySuffix) {
 		this._keySuffix = keySuffix;
 	}
 
 	public String getServerString() {
 		return _serverString;
 	}
 
 	public void setServerString(String serverString) {
 		this._serverString = serverString;
 	}
 
 	public int getTimeoutInMs() {
 		return _timeoutInMs;
 	}
 
 	public void setTimeoutInMs(int timeoutInMs) {
 		this._timeoutInMs = timeoutInMs;
 	}
 }
