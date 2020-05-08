 /*
  * Copyright 2013 Eediom Inc.
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
 package org.araqne.logdb.impl;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.api.PrimitiveConverter;
 import org.araqne.confdb.Config;
 import org.araqne.confdb.ConfigCollection;
 import org.araqne.confdb.ConfigDatabase;
 import org.araqne.confdb.ConfigService;
 import org.araqne.confdb.Predicates;
 import org.araqne.logdb.AccountService;
 import org.araqne.logdb.ExternalAuthService;
 import org.araqne.logdb.Permission;
 import org.araqne.logdb.Privilege;
 import org.araqne.logdb.Session;
 import org.araqne.logdb.SessionEventListener;
 import org.araqne.logstorage.LogTableEventListener;
 import org.araqne.logstorage.LogTableRegistry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component(name = "logdb-account")
 @Provides(specifications = { AccountService.class })
 public class AccountServiceImpl implements AccountService, LogTableEventListener {
 	private final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
 	private static final String DB_NAME = "araqne-logdb";
 	private static final String DEFAULT_MASTER_ACCOUNT = "araqne";
 	private static final char[] SALT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
 
 	@Requires
 	private ConfigService conf;
 
 	@Requires
 	private LogTableRegistry tableRegistry;
 
 	private ConcurrentMap<String, Session> sessions;
 	private ConcurrentMap<String, Account> localAccounts;
 	private ConcurrentMap<String, ExternalAuthService> authServices;
 
 	private String selectedExternalAuth;
 	private CopyOnWriteArraySet<SessionEventListener> sessionListeners;
 
 	public AccountServiceImpl() {
 		sessions = new ConcurrentHashMap<String, Session>();
 		localAccounts = new ConcurrentHashMap<String, Account>();
 		authServices = new ConcurrentHashMap<String, ExternalAuthService>();
 		sessionListeners = new CopyOnWriteArraySet<SessionEventListener>();
 	}
 
 	@Validate
 	public void start() {
 		tableRegistry.addListener(this);
 		sessions.clear();
 		localAccounts.clear();
 
 		// load accounts
 		ConfigDatabase db = conf.ensureDatabase(DB_NAME);
 		for (Account account : db.findAll(Account.class).getDocuments(Account.class)) {
 			localAccounts.put(account.getLoginName(), account);
 		}
 
 		// generate default 'araqne' account if not exists
 		if (!localAccounts.containsKey(DEFAULT_MASTER_ACCOUNT)) {
 			String salt = randomSalt(10);
 			Account account = new Account(DEFAULT_MASTER_ACCOUNT, salt, Sha1.hash(salt));
 			db.add(account);
 			localAccounts.put(DEFAULT_MASTER_ACCOUNT, account);
 		}
 
 		// load external auth service config
 		ConfigCollection col = db.ensureCollection("global_config");
 		Config c = col.findOne(null);
 		if (c != null) {
 			@SuppressWarnings("unchecked")
 			Map<String, Object> m = (Map<String, Object>) c.getDocument();
 			selectedExternalAuth = (String) m.get("external_auth");
 		}
 	}
 
 	@Invalidate
 	public void stop() {
 		if (tableRegistry != null)
 			tableRegistry.removeListener(this);
 	}
 
 	@Override
 	public List<Session> getSessions() {
 		return new ArrayList<Session>(sessions.values());
 	}
 
 	@Override
 	public Session getSession(String guid) {
 		return sessions.get(guid);
 	}
 
 	@Override
 	public boolean isAdmin(String loginName) {
 		Account account = ensureAccount(loginName);
 		return account.isAdmin();
 	}
 
 	@Override
 	public void grantAdmin(Session session, String loginName) {
 		if (!session.isAdmin())
 			throw new IllegalStateException("no permission");
 
 		Account account = ensureAccount(loginName);
 		account.setAdmin(true);
 		updateAccount(account);
 	}
 
 	@Override
 	public void revokeAdmin(Session session, String loginName) {
 		if (!session.isAdmin())
 			throw new IllegalStateException("no permission");
 
 		if (session.getLoginName().equals(loginName))
 			throw new IllegalStateException("cannot revoke current admin session");
 
 		Account account = ensureAccount(loginName);
 		account.setAdmin(false);
 		updateAccount(account);
 	}
 
 	@Override
 	public List<Privilege> getPrivileges(Session session, String loginName) {
 		verifyNotNull(session, "session");
 
 		if (!sessions.containsKey(session.getGuid()))
 			throw new IllegalStateException("invalid session");
 
 		// allow own info check or master admin only
 		if (!checkOwner(session, loginName))
 			throw new IllegalStateException("no permission");
 
 		List<Privilege> privileges = new ArrayList<Privilege>();
 		if (loginName != null) {
 			checkAccountIncludingExternal(loginName);
 
 			Account account = ensureAccount(loginName);
 			for (String tableName : account.getReadableTables()) {
 				privileges.add(new Privilege(loginName, tableName, Arrays.asList(Permission.READ)));
 			}
 		} else {
 			ConfigDatabase db = conf.ensureDatabase(DB_NAME);
 			for (Account account : db.findAll(Account.class).getDocuments(Account.class))
 				for (String tableName : account.getReadableTables())
 					privileges.add(new Privilege(account.getLoginName(), tableName, Arrays.asList(Permission.READ)));
 
 		}
 		return privileges;
 	}
 
 	private boolean checkOwner(Session session, String loginName) {
 		Account account = ensureAccount(session.getLoginName());
 		if (account.isAdmin())
 			return true;
 
 		if (loginName == null)
 			return false;
 
 		return loginName.equals(session.getLoginName());
 	}
 
 	@Override
 	public Set<String> getAccountNames() {
 		return new HashSet<String>(localAccounts.keySet());
 	}
 
 	@Override
 	public boolean verifyPassword(String loginName, String password) {
 		verifyNotNull(loginName, "login name");
 		verifyNotNull(password, "password");
 
 		Account account = localAccounts.get(loginName);
 		// try local login first
 		if (account != null && account.getAuthServiceName() == null) {
 			// salted hash
 			String hash = account.getPassword();
 			String salt = account.getSalt();
 
 			return hash.equals(Sha1.hash(password + salt));
 		} else if (selectedExternalAuth != null) {
 			// try external login
 			ExternalAuthService auth = authServices.get(selectedExternalAuth);
 			if (auth == null)
 				throw new IllegalStateException("logdb external auth service is not loaded: " + selectedExternalAuth);
 
 			return auth.verifyPassword(loginName, password);
 		} else
 			throw new IllegalStateException("account not found");
 	}
 
 	@Override
 	public Session login(String loginName, String hash, String nonce) {
 		verifyNotNull(loginName, "login name");
 		verifyNotNull(hash, "hash");
 		verifyNotNull(nonce, "nonce");
 
 		Account account = localAccounts.get(loginName);
 		if (account == null)
 			throw new IllegalStateException("account-not-found");
 
 		String password = account.getPassword();
 		if (!hash.equals(Sha1.hash(password + nonce))) {
 			throw new IllegalStateException("invalid-password");
 		}
 
 		return registerSession(account);
 	}
 
 	@Override
 	public Session login(String loginName, String password) {
 		if (!verifyPassword(loginName, password))
 			throw new IllegalStateException("invalid password");
 
 		Account account = ensureAccount(loginName);
 		return registerSession(account);
 	}
 
 	private Session registerSession(Account account) {
 		String guid = UUID.randomUUID().toString();
 		Session session = new SessionImpl(guid, account.getLoginName(), account.isAdmin());
 		sessions.put(guid, session);
 
 		// invoke callbacks
 		for (SessionEventListener listener : sessionListeners) {
 			try {
 				listener.onLogin(session);
 			} catch (Throwable t) {
 				logger.warn("araqne logdb: session event listener should not throw any exception", t);
 			}
 		}
 
 		return session;
 	}
 
 	@Override
 	public void logout(Session session) {
 		if (!sessions.remove(session.getGuid(), session))
 			throw new IllegalStateException("session not found: " + session.getGuid());
 
 		// invoke callbacks
 		for (SessionEventListener listener : sessionListeners) {
 			try {
 				listener.onLogout(session);
 			} catch (Throwable t) {
 				logger.warn("araqne logdb: session event listener should not throw any exception", t);
 			}
 		}
 	}
 
 	@Override
 	public void createAccount(Session session, String loginName, String password) {
 		verifyNotNull(session, "session");
 		verifyNotNull(loginName, "login name");
 		verifyNotNull(password, "password");
 
 		if (localAccounts.containsKey(loginName))
 			throw new IllegalStateException("duplicated login name");
 
 		if (!sessions.containsKey(session.getGuid()))
 			throw new IllegalStateException("invalid session");
 
 		if (!session.isAdmin())
 			throw new IllegalStateException("no permission");
 
 		// check database
 		ConfigDatabase db = conf.ensureDatabase(DB_NAME);
 		Config c = db.findOne(Account.class, Predicates.field("login_name", loginName));
 		if (c != null)
 			throw new IllegalStateException("duplicated login name");
 
 		String salt = randomSalt(10);
 		String hash = Sha1.hash(password + salt);
 		Account account = new Account(loginName, salt, hash);
 
 		Account old = localAccounts.putIfAbsent(account.getLoginName(), account);
 		if (old != null && old.getAuthServiceName() == null)
 			throw new IllegalStateException("duplicated login name");
 
 		db.add(account);
 	}
 
 	@Override
 	public void changePassword(Session session, String loginName, String password) {
 		verifyNotNull(session, "session");
 		verifyNotNull(loginName, "login name");
 		verifyNotNull(password, "password");
 
 		if (!localAccounts.containsKey(loginName))
 			throw new IllegalStateException("account not found");
 
 		if (!sessions.containsKey(session.getGuid()))
 			throw new IllegalStateException("invalid session");
 
 		// check if owner or master
 		if (!checkOwner(session, loginName))
 			throw new IllegalStateException("no permission");
 
 		Account account = localAccounts.get(loginName);
 		String hash = Sha1.hash(password + account.getSalt());
 		account.setPassword(hash);
 
 		updateAccount(account);
 	}
 
 	private void updateAccount(Account account) {
 		ConfigDatabase db = conf.ensureDatabase(DB_NAME);
 		Config c = db.findOne(Account.class, Predicates.field("login_name", account.getLoginName()));
 		if (c != null) {
 			c.setDocument(PrimitiveConverter.serialize(account));
 			c.update();
 		} else {
 			db.add(account);
 		}
 	}
 
 	@Override
 	public void removeAccount(Session session, String loginName) {
 		verifyNotNull(session, "session");
 		verifyNotNull(loginName, "login name");
 
 		if (!localAccounts.containsKey(loginName))
 			throw new IllegalStateException("account not found");
 
 		if (!sessions.containsKey(session.getGuid()))
 			throw new IllegalStateException("invalid session");
 
		if (session.getLoginName().equals(loginName))
			throw new IllegalStateException("cannot delete your own account");

 		// master admin only
 		if (!session.isAdmin())
 			throw new IllegalStateException("no permission");
 
 		localAccounts.remove(loginName);
 
 		// drop all sessions
 		for (Session s : new ArrayList<Session>(sessions.values())) {
 			if (s.getLoginName().equals(loginName))
 				sessions.remove(s.getGuid());
 		}
 
 		// delete from database
 		ConfigDatabase db = conf.ensureDatabase(DB_NAME);
 		Config c = db.findOne(Account.class, Predicates.field("login_name", loginName));
 		if (c != null)
 			c.remove();
 	}
 
 	@Override
 	public boolean checkPermission(Session session, String tableName, Permission permission) {
 		verifyNotNull(session, "session");
 		verifyNotNull(tableName, "table name");
 		verifyNotNull(permission, "permission");
 
 		if (permission != Permission.READ)
 			throw new UnsupportedOperationException();
 
 		Account account = ensureAccount(session.getLoginName());
 
 		if (session.getLoginName().equals("araqne"))
 			return true;
 
 		return account.getReadableTables().contains(tableName);
 	}
 
 	@Override
 	public void grantPrivilege(Session session, String loginName, String tableName, Permission... permissions) {
 		verifyNotNull(session, "session");
 		verifyNotNull(loginName, "login name");
 		verifyNotNull(tableName, "table name");
 
 		if (permissions.length == 0)
 			return;
 
 		checkAccountIncludingExternal(loginName);
 
 		if (!sessions.containsKey(session.getGuid()))
 			throw new IllegalStateException("invalid session");
 
 		// master admin only
 		if (!session.isAdmin())
 			throw new IllegalStateException("no permission");
 
 		if (!tableRegistry.exists(tableName))
 			throw new IllegalStateException("table not found");
 
 		Account account = ensureAccount(loginName);
 		if (account.getReadableTables().contains(tableName))
 			return;
 
 		account.getReadableTables().add(tableName);
 		updateAccount(account);
 	}
 
 	@Override
 	public void revokePrivilege(Session session, String loginName, String tableName, Permission... permissions) {
 		verifyNotNull(session, "session");
 		verifyNotNull(loginName, "login name");
 		verifyNotNull(tableName, "table name");
 
 		if (permissions.length == 0)
 			return;
 
 		checkAccountIncludingExternal(loginName);
 
 		if (!sessions.containsKey(session.getGuid()))
 			throw new IllegalStateException("invalid session");
 
 		// master admin only
 		if (!session.isAdmin())
 			throw new IllegalStateException("no permission");
 
 		if (!tableRegistry.exists(tableName))
 			throw new IllegalStateException("table not found");
 
 		Account account = ensureAccount(loginName);
 		if (!account.getReadableTables().contains(tableName))
 			return;
 
 		account.getReadableTables().remove(tableName);
 		updateAccount(account);
 	}
 
 	private Account ensureAccount(String loginName) {
 		Account account = localAccounts.get(loginName);
 		if (account != null)
 			return account;
 
 		if (selectedExternalAuth != null) {
 			ExternalAuthService auth = authServices.get(selectedExternalAuth);
 			if (auth != null && auth.verifyUser(loginName)) {
 				account = new Account();
 				account.setLoginName(loginName);
 				account.setAuthServiceName(selectedExternalAuth);
 				return account;
 			}
 		}
 
 		throw new IllegalStateException("account not found: " + loginName);
 	}
 
 	private void checkAccountIncludingExternal(String loginName) {
 		if (!localAccounts.containsKey(loginName)) {
 			if (selectedExternalAuth != null) {
 				ExternalAuthService auth = authServices.get(selectedExternalAuth);
 				if (auth != null && auth.verifyUser(loginName))
 					return;
 			}
 
 			throw new IllegalStateException("account not found");
 		}
 	}
 
 	private void verifyNotNull(Object o, String name) {
 		if (o == null)
 			throw new IllegalArgumentException(name + " should not be null");
 	}
 
 	private String randomSalt(int saltLength) {
 		StringBuilder salt = new StringBuilder(saltLength);
 		Random rand = new Random();
 		for (int i = 0; i < saltLength; i++)
 			salt.append(SALT_CHARS[rand.nextInt(SALT_CHARS.length)]);
 		return salt.toString();
 	}
 
 	@Override
 	public ExternalAuthService getUsingAuthService() {
 		if (selectedExternalAuth == null)
 			return null;
 
 		return authServices.get(selectedExternalAuth);
 	}
 
 	@Override
 	public void useAuthService(String name) {
 		if (name != null && !authServices.containsKey(name))
 			throw new IllegalStateException("external auth service not found: " + name);
 
 		ConfigDatabase db = conf.ensureDatabase(DB_NAME);
 		ConfigCollection col = db.ensureCollection("global_config");
 		Config c = col.findOne(null);
 
 		if (c != null) {
 			@SuppressWarnings("unchecked")
 			Map<String, Object> doc = (Map<String, Object>) c.getDocument();
 			doc.put("external_auth", name);
 			c.setDocument(doc);
 			c.update();
 		} else {
 			Map<String, Object> doc = new HashMap<String, Object>();
 			doc.put("external_auth", name);
 			col.add(doc);
 		}
 
 		selectedExternalAuth = name;
 	}
 
 	@Override
 	public List<ExternalAuthService> getAuthServices() {
 		return new ArrayList<ExternalAuthService>(authServices.values());
 	}
 
 	@Override
 	public ExternalAuthService getAuthService(String name) {
 		return authServices.get(name);
 	}
 
 	@Override
 	public void registerAuthService(ExternalAuthService auth) {
 		ExternalAuthService old = authServices.putIfAbsent(auth.getName(), auth);
 		if (old != null)
 			throw new IllegalStateException("duplicated logdb auth service name: " + auth.getName());
 	}
 
 	@Override
 	public void unregisterAuthService(ExternalAuthService auth) {
 		authServices.remove(auth.getName(), auth);
 	}
 
 	@Override
 	public void addListener(SessionEventListener listener) {
 		sessionListeners.add(listener);
 	}
 
 	@Override
 	public void removeListener(SessionEventListener listener) {
 		sessionListeners.remove(listener);
 	}
 
 	@Override
 	public void onCreate(String tableName, Map<String, String> tableMetadata) {
 	}
 
 	@Override
 	public void onDrop(String tableName) {
 		// remove all granted permissions for this table
 		for (Account account : localAccounts.values()) {
 			if (account.getReadableTables().contains(tableName)) {
 				ArrayList<String> copy = new ArrayList<String>(account.getReadableTables());
 				copy.remove(tableName);
 				account.setReadableTables(copy);
 				updateAccount(account);
 			}
 		}
 	}
 }
