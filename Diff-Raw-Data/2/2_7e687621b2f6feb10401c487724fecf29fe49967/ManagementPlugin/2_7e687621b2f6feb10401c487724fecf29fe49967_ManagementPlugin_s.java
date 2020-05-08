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
 package org.araqne.logdb.msgbus;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.araqne.api.PrimitiveConverter;
 import org.araqne.logdb.AccountService;
 import org.araqne.logdb.Permission;
 import org.araqne.logdb.Privilege;
 import org.araqne.logstorage.LogStorage;
 import org.araqne.logstorage.LogTableRegistry;
 import org.araqne.logstorage.UnsupportedLogFileTypeException;
 import org.araqne.msgbus.MessageBus;
 import org.araqne.msgbus.MsgbusException;
 import org.araqne.msgbus.Request;
 import org.araqne.msgbus.Response;
 import org.araqne.msgbus.Session;
 import org.araqne.msgbus.handler.AllowGuestAccess;
 import org.araqne.msgbus.handler.CallbackType;
 import org.araqne.msgbus.handler.MsgbusMethod;
 import org.araqne.msgbus.handler.MsgbusPlugin;
 
 @Component(name = "logdb-management-msgbus")
 @MsgbusPlugin
 public class ManagementPlugin {
 
 	@Requires
 	private AccountService accountService;
 
 	@Requires
 	private LogTableRegistry tableRegistry;
 
 	@Requires
 	private MessageBus msgbus;
 
 	@Requires
 	private LogStorage storage;
 
 	@AllowGuestAccess
 	@MsgbusMethod
 	public void login(Request req, Response resp) {
 		Session session = req.getSession();
 
 		if (session.get("araqne_logdb_session") != null)
 			throw new MsgbusException("logdb", "already-logon");
 
 		String loginName = req.getString("login_name", true);
 		String password = req.getString("password", true);
 
 		org.araqne.logdb.Session dbSession = accountService.login(loginName, password);
 
 		if (session.getOrgDomain() == null && session.getAdminLoginName() == null) {
 			session.setProperty("org_domain", "localhost");
 			session.setProperty("admin_login_name", loginName);
 			session.setProperty("auth", "logdb");
 		}
 
 		session.setProperty("araqne_logdb_session", dbSession);
 	}
 
 	@MsgbusMethod
 	public void logout(Request req, Response resp) {
 		Session session = req.getSession();
 		org.araqne.logdb.Session dbSession = (org.araqne.logdb.Session) session.get("araqne_logdb_session");
 		if (dbSession != null) {
 			try {
 				accountService.logout(dbSession);
 			} catch (Throwable t) {
 			}
 
 			session.unsetProperty("araqne_logdb_session");
 		}
 
 		String auth = session.getString("auth");
 		if (auth != null && auth.equals("logdb"))
 			msgbus.closeSession(session);
 	}
 
 	@MsgbusMethod(type = CallbackType.SessionClosed)
 	public void onClose(Session session) {
 		if (session == null)
 			return;
 
 		org.araqne.logdb.Session dbSession = (org.araqne.logdb.Session) session.get("araqne_logdb_session");
 		if (dbSession != null) {
 			try {
 				accountService.logout(dbSession);
 			} catch (Throwable t) {
 			}
 
 			session.unsetProperty("araqne_logdb_session");
 		}
 	}
 
 	@MsgbusMethod
 	public void listAccounts(Request req, Response resp) {
 		org.araqne.logdb.Session session = ensureAdminSession(req);
 		List<Object> accounts = new ArrayList<Object>();
 		for (String loginName : accountService.getAccountNames()) {
 			List<Privilege> privileges = accountService.getPrivileges(session, loginName);
 
 			Map<String, Object> m = new HashMap<String, Object>();
 			m.put("login_name", loginName);
 			m.put("privileges", serialize(privileges));
 			accounts.add(m);
 		}
 
 		resp.put("accounts", accounts);
 	}
 
 	private List<Object> serialize(List<Privilege> privileges) {
 		List<Object> l = new ArrayList<Object>();
 		for (Privilege p : privileges)
 			l.add(PrimitiveConverter.serialize(p));
 		return l;
 	}
 
 	@MsgbusMethod
 	public void changePassword(Request req, Response resp) {
 		org.araqne.logdb.Session session = ensureAdminSession(req);
 		String loginName = req.getString("login_name", true);
 		String password = req.getString("password", true);
 		accountService.changePassword(session, loginName, password);
 	}
 
 	@MsgbusMethod
 	public void createAccount(Request req, Response resp) {
 		org.araqne.logdb.Session session = ensureAdminSession(req);
 		String loginName = req.getString("login_name", true);
 		String password = req.getString("password", true);
 		accountService.createAccount(session, loginName, password);
 	}
 
 	@MsgbusMethod
 	public void removeAccount(Request req, Response resp) {
 		org.araqne.logdb.Session session = ensureAdminSession(req);
 		String loginName = req.getString("login_name", true);
 		accountService.removeAccount(session, loginName);
 	}
 
 	@MsgbusMethod
 	public void getPrivileges(Request req, Response resp) {
 		org.araqne.logdb.Session session = ensureDbSession(req);
 		String loginName = req.getString("login_name", true);
 
 		List<Privilege> privileges = accountService.getPrivileges(session, loginName);
 
 		Map<String, List<String>> m = new HashMap<String, List<String>>();
 		List<String> l = Arrays.asList(Permission.READ.toString());
 		for (Privilege p : privileges)
 			m.put(p.getTableName(), l);
 
 		resp.putAll(m);
 	}
 
 	@SuppressWarnings("unchecked")
 	@MsgbusMethod
 	public void setPrivileges(Request req, Response resp) {
 		org.araqne.logdb.Session session = ensureAdminSession(req);
 		String loginName = req.getString("login_name", true);
 		List<String> tableNames = (List<String>) req.get("table_names", true);
 
 		List<Privilege> privileges = new ArrayList<Privilege>();
 		for (String tableName : tableNames)
 			privileges.add(new Privilege(loginName, tableName, Permission.READ));
 
 		accountService.setPrivileges(session, loginName, privileges);
 	}
 
 	@MsgbusMethod
 	public void grantPrivilege(Request req, Response resp) {
 		org.araqne.logdb.Session session = ensureAdminSession(req);
 		String loginName = req.getString("login_name", true);
 		String tableName = req.getString("table_name", true);
 		accountService.grantPrivilege(session, loginName, tableName, Permission.READ);
 	}
 
 	@SuppressWarnings("unchecked")
 	@MsgbusMethod
 	public void grantPrivileges(Request req, Response resp) {
 		org.araqne.logdb.Session session = ensureAdminSession(req);
 		String loginName = req.getString("login_name", true);
 		List<String> tableNames = (List<String>) req.get("table_names", true);
 
 		for (String tableName : tableNames)
 			accountService.grantPrivilege(session, loginName, tableName, Permission.READ);
 	}
 
 	@MsgbusMethod
 	public void revokePrivilege(Request req, Response resp) {
 		org.araqne.logdb.Session session = ensureAdminSession(req);
 		String loginName = req.getString("login_name", true);
 		String tableName = req.getString("table_name", true);
 		accountService.revokePrivilege(session, loginName, tableName, Permission.READ);
 	}
 
 	@SuppressWarnings("unchecked")
 	@MsgbusMethod
 	public void revokePrivileges(Request req, Response resp) {
 		org.araqne.logdb.Session session = ensureAdminSession(req);
 		String loginName = req.getString("login_name", true);
 		List<String> tableNames = (List<String>) req.get("table_names");
 		for (String tableName : tableNames)
 			accountService.revokePrivilege(session, loginName, tableName, Permission.READ);
 	}
 
 	@MsgbusMethod
 	public void listTables(Request req, Response resp) {
 		org.araqne.logdb.Session session = ensureDbSession(req);
 
 		Map<String, Object> tables = new HashMap<String, Object>();
 
 		if (session.isAdmin()) {
 			for (String tableName : tableRegistry.getTableNames()) {
 				tables.put(tableName, getTableMetadata(tableName));
 			}
 		} else {
 			List<Privilege> privileges = accountService.getPrivileges(session, session.getLoginName());
 			for (Privilege p : privileges) {
 				if (p.getPermissions().size() > 0 && tableRegistry.exists(p.getTableName())) {
 					tables.put(p.getTableName(), getTableMetadata(p.getTableName()));
 				}
 			}
 		}
 
 		resp.put("tables", tables);
 	}
 
 	@MsgbusMethod
 	public void getTableInfo(Request req, Response resp) {
 		String tableName = req.getString("table", true);
 		checkTableAccess(req, tableName, Permission.READ);
 
 		resp.put("table", getTableMetadata(tableName));
 
 	}
 
 	private Map<String, Object> getTableMetadata(String tableName) {
 		Map<String, Object> metadata = new HashMap<String, Object>();
 		for (String key : tableRegistry.getTableMetadataKeys(tableName)) {
 			metadata.put(key, tableRegistry.getTableMetadata(tableName, key));
 		}
 		return metadata;
 	}
 
 	@SuppressWarnings("unchecked")
 	@MsgbusMethod
 	public void setTableMetadata(Request req, Response resp) {
 		ensureAdminSession(req);
 
 		String tableName = req.getString("table", true);
 		Map<String, Object> metadata = (Map<String, Object>) req.get("metadata", true);
 
 		for (String key : metadata.keySet()) {
 			Object value = metadata.get(key);
 			tableRegistry.setTableMetadata(tableName, key, value == null ? null : value.toString());
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@MsgbusMethod
 	public void unsetTableMetadata(Request req, Response resp) {
 		ensureAdminSession(req);
 
 		String tableName = req.getString("table", true);
 		List<Object> keys = (List<Object>) req.get("keys", true);
 
 		for (Object key : keys) {
 			tableRegistry.unsetTableMetadata(tableName, key.toString());
 		}
 	}
 
 	private void checkTableAccess(Request req, String tableName, Permission permission) {
 		org.araqne.logdb.Session session = (org.araqne.logdb.Session) req.getSession().get("araqne_logdb_session");
 		if (session == null)
 			throw new MsgbusException("logdb", "no-logdb-session");
 
 		boolean allowed = session.isAdmin() || accountService.checkPermission(session, tableName, permission);
 		if (!allowed)
 			throw new MsgbusException("logdb", "no-permission");
 	}
 
 	@MsgbusMethod
 	public void createTable(Request req, Response resp) {
 		ensureAdminSession(req);
 		String tableName = req.getString("table", true);
 
 		@SuppressWarnings("unchecked")
		Map<String, String> metadata = (Map<String, String>) req.get("metadata", true);
 		try {
 			storage.createTable(tableName, "v3p", metadata);
 		} catch (UnsupportedLogFileTypeException e) {
 			storage.createTable(tableName, "v2", metadata);
 		}
 	}
 
 	@MsgbusMethod
 	public void dropTable(Request req, Response resp) {
 		ensureAdminSession(req);
 		String tableName = req.getString("table", true);
 		storage.dropTable(tableName);
 	}
 
 	private org.araqne.logdb.Session ensureAdminSession(Request req) {
 		org.araqne.logdb.Session session = (org.araqne.logdb.Session) req.getSession().get("araqne_logdb_session");
 		if (session != null && !session.isAdmin())
 			throw new SecurityException("logdb management is not allowed to " + session.getLoginName());
 		return session;
 	}
 
 	private org.araqne.logdb.Session ensureDbSession(Request req) {
 		org.araqne.logdb.Session session = (org.araqne.logdb.Session) req.getSession().get("araqne_logdb_session");
 		if (session == null)
 			throw new SecurityException("logdb session not found: " + req.getSession().getAdminLoginName());
 		return session;
 	}
 }
