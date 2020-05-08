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
 
 		String loginName = req.getString("login_name");
 		String password = req.getString("password");
 
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
 			accountService.logout(dbSession);
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
 		if (dbSession != null)
 			accountService.logout(dbSession);
 	}
 
 	@MsgbusMethod
 	public void listAccounts(Request req, Response resp) {
 		org.araqne.logdb.Session session = checkPermission(req);
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
 		org.araqne.logdb.Session session = checkPermission(req);
 		String loginName = req.getString("login_name");
 		String password = req.getString("password");
 		accountService.changePassword(session, loginName, password);
 	}
 
 	@MsgbusMethod
 	public void createAccount(Request req, Response resp) {
 		org.araqne.logdb.Session session = checkPermission(req);
 		String loginName = req.getString("login_name");
 		String password = req.getString("password");
 		accountService.createAccount(session, loginName, password);
 	}
 
 	@MsgbusMethod
 	public void removeAccount(Request req, Response resp) {
 		org.araqne.logdb.Session session = checkPermission(req);
 		String loginName = req.getString("login_name");
 		accountService.removeAccount(session, loginName);
 	}
 
 	@MsgbusMethod
 	public void grantPrivilege(Request req, Response resp) {
 		org.araqne.logdb.Session session = checkPermission(req);
 		String loginName = req.getString("login_name");
 		String tableName = req.getString("table_name");
 		accountService.grantPrivilege(session, loginName, tableName, Permission.READ);
 	}
 
 	@MsgbusMethod
 	public void revokePrivilege(Request req, Response resp) {
 		org.araqne.logdb.Session session = checkPermission(req);
 		String loginName = req.getString("login_name");
 		String tableName = req.getString("table_name");
 		accountService.revokePrivilege(session, loginName, tableName, Permission.READ);
 	}
 
 	@MsgbusMethod
 	public void listTables(Request req, Response resp) {
 		checkPermission(req);
 
 		Map<String, Object> tables = new HashMap<String, Object>();
 		for (String tableName : tableRegistry.getTableNames()) {
 			tables.put(tableName, getTableMetadata(tableName));
 		}
 
 		resp.put("tables", tables);
 	}
 
 	@MsgbusMethod
 	public void getTableInfo(Request req, Response resp) {
 		checkPermission(req);
 
 		String tableName = req.getString("table");
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
 		checkPermission(req);
 
 		String tableName = req.getString("table");
 		Map<String, Object> metadata = (Map<String, Object>) req.get("metadata");
 
 		for (String key : metadata.keySet()) {
 			Object value = metadata.get(key);
 			tableRegistry.setTableMetadata(tableName, key, value == null ? null : value.toString());
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@MsgbusMethod
 	public void unsetTableMetadata(Request req, Response resp) {
 		checkPermission(req);
 
 		String tableName = req.getString("table");
 		List<Object> keys = (List<Object>) req.get("keys");
 
 		for (Object key : keys) {
 			tableRegistry.unsetTableMetadata(tableName, key.toString());
 		}
 	}
 
 	@MsgbusMethod
 	public void createTable(Request req, Response resp) {
 		checkPermission(req);
 		String tableName = req.getString("table");
		storage.createTable(tableName);
 	}
 
 	@MsgbusMethod
 	public void dropTable(Request req, Response resp) {
 		checkPermission(req);
 		String tableName = req.getString("table");
 		storage.dropTable(tableName);
 	}
 
 	private org.araqne.logdb.Session checkPermission(Request req) {
 		org.araqne.logdb.Session session = (org.araqne.logdb.Session) req.getSession().get("araqne_logdb_session");
 		if (session != null && !session.getLoginName().equals("araqne"))
 			throw new SecurityException("logdb management is not allowed to " + session.getLoginName());
 		return session;
 	}
 }
