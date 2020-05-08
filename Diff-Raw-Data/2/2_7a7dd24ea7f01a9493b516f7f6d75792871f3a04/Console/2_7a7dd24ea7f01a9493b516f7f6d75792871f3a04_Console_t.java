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
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.api.ScriptContext;
 import org.araqne.logdb.AccountService;
 import org.araqne.logdb.LogQuery;
 import org.araqne.logdb.LogQueryCommand;
 import org.araqne.logdb.LogQueryService;
 import org.araqne.logdb.LogResultSet;
 import org.araqne.logdb.Permission;
 import org.araqne.logdb.Session;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Console {
 	private final Logger logger = LoggerFactory.getLogger(Console.class);
 	private ScriptContext context;
 	private LogQueryService queryService;
 	private AccountService accountService;
 	private Session session;
 
 	public Console(ScriptContext context, AccountService accountService, LogQueryService queryService) {
 		this.context = context;
 		this.accountService = accountService;
 		this.queryService = queryService;
 	}
 
 	public void run(String loginName) {
 		try {
 			context.print("password? ");
 			String password = context.readPassword();
 			session = accountService.login(loginName, password);
 
 			context.println("Araqne LogDB Console");
 			context.println("Type \"help\" for more information");
 
 			while (true) {
 				context.print(session.getLoginName() + "@logdb> ");
 				String line = context.readLine();
 				if (line.trim().equals("quit") || line.trim().equals("exit"))
 					break;
 
 				handle(line);
 			}
 		} catch (Throwable t) {
 			context.println(t.getMessage());
 		} finally {
 			if (session != null) {
 				accountService.logout(session);
 				context.println("logout");
 			}
 		}
 	}
 
 	private void handle(String line) {
 		if (line.trim().isEmpty())
 			return;
 
 		try {
 			String[] args = line.split(" ");
 			String command = args[0].trim();
 
 			if (command.equals("help")) {
 				help();
 			} else if (command.equals("create_account")) {
 				createAccount(args[1]);
 			} else if (command.equals("remove_account")) {
 				removeAccount(args[1]);
 			} else if (command.equals("passwd")) {
 				changePassword(args[1]);
 			} else if (command.equals("queries")) {
 				queries();
 			} else if (command.equals("query")) {
 				query(line.substring("query".length()).trim());
 			} else if (command.equals("create_query")) {
 				createQuery(line.substring("create_query".length()).trim());
 			} else if (command.equals("start_query")) {
 				startQuery(Integer.valueOf(args[1]));
 			} else if (command.equals("stop_query")) {
 				stopQuery(Integer.valueOf(args[1]));
 			} else if (command.equals("remove_query")) {
 				removeQuery(Integer.valueOf(args[1]));
 			} else if (command.equals("remove_all_queries")) {
 				removeAllQueries();
 			} else if (command.equals("fetch")) {
 				int id = Integer.valueOf(args[1]);
 				long offset = Long.valueOf(args[2]);
 				long limit = Long.valueOf(args[3]);
 				fetch(id, offset, limit);
 			} else if (command.equals("grant")) {
 				grantPrivilege(args[1], args[2]);
 			} else if (command.equals("revoke")) {
 				revokePrivilege(args[1], args[2]);
 			} else {
 				context.println("invalid syntax");
 			}
 		} catch (Throwable t) {
 			context.println(t.getMessage());
 			logger.error("araqne logdb: console fail", t);
 		}
 	}
 
 	private void help() {
 		context.println("queries");
 		context.println("\tprint all queries initiated by this session");
 
 		context.println("query <query string>");
 		context.println("\tcreate, start and fetch query result at once");
 
 		context.println("create_query <query string>");
 		context.println("\tcreate query with specified query string, and return allocated query id");
 
 		context.println("start_query <query id>");
 		context.println("\tstart query");
 
 		context.println("stop_query <query_id>");
 		context.println("\tstop running query");
 
 		context.println("remove_query <query_id>");
 		context.println("\tstop and remove query");
 
 		context.println("fetch <query_id> <offset> <limit>");
 		context.println("\tfetch result set of specified window. you can fetch partial result before query is ended");
 
 		context.println("grant <account> <table>");
 		context.println("\tgrant read table permission to specified account");
 
 		context.println("revoke <account> <table>");
 		context.println("\trevoke read table permission from specified account");
 	}
 
 	private void createAccount(String loginName) throws InterruptedException {
 		context.print("New password: ");
 		String password = context.readPassword();
 		accountService.createAccount(session, loginName, password);
 		context.println("created " + loginName);
 	}
 
 	private void removeAccount(String loginName) {
 		accountService.removeAccount(session, loginName);
 		context.println("removed " + loginName);
 	}
 
 	private void changePassword(String loginName) throws InterruptedException {
 		context.println("Changing password for user " + loginName);
 		if (!session.getLoginName().equals("araqne")) {
 			context.print("(current) password: ");
 			String current = context.readPassword();
 
 			if (!accountService.verifyPassword(loginName, current)) {
 				context.println("password mismatch");
 				return;
 			}
 		}
 
 		context.print("New password: ");
 		String password = context.readPassword();
 
 		context.print("Retype new password: ");
 		String rePassword = context.readPassword();
 
 		if (!password.equals(rePassword)) {
 			context.println("Sorry, passwords do not match");
 			return;
 		}
 
 		accountService.changePassword(session, loginName, rePassword);
 		context.println("password changed");
 	}
 
 	private void queries() {
 		context.println("Log Queries");
 		context.println("-------------");
 		ArrayList<LogQuery> queries = new ArrayList<LogQuery>(queryService.getQueries());
 		Collections.sort(queries, new Comparator<LogQuery>() {
 			@Override
 			public int compare(LogQuery o1, LogQuery o2) {
 				return o1.getId() - o2.getId();
 			}
 		});
 
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
 		for (LogQuery query : queries) {
 			String when = " \t/ not started yet";
 			if (query.getLastStarted() != null) {
 				long sec = new Date().getTime() - query.getLastStarted().getTime();
 				when = String.format(" \t/ %s, %d seconds ago", sdf.format(query.getLastStarted()), sec / 1000);
 			}
 
 			context.println(String.format("[%d] %s%s", query.getId(), query.getQueryString(), when));
 
 			if (query.getCommands() != null) {
 				for (LogQueryCommand cmd : query.getCommands()) {
 					context.println(String.format("    [%s] %s \t/ passed %d data to next query", cmd.getStatus(),
 							cmd.getQueryString(), cmd.getPushCount()));
 				}
 			} else
 				context.println("    null");
 		}
 	}
 
 	private void query(String queryString) throws IOException {
 		long begin = System.currentTimeMillis();
 		LogQuery lq = queryService.createQuery(session, queryString);
 		queryService.startQuery(lq.getId());
 
 		do {
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 			}
 		} while (!lq.isEnd());
 
 		long count = 0;
 		LogResultSet rs = null;
 		try {
 			rs = lq.getResult();
 			while (rs.hasNext()) {
 				printMap(rs.next());
 				count++;
 			}
 		} finally {
 			if (rs != null)
 				rs.close();
 		}
 
 		queryService.removeQuery(lq.getId());
 		context.println(String.format("total %d rows, elapsed %.1fs", count, (System.currentTimeMillis() - begin) / (double) 1000));
 	}
 
 	private void createQuery(String queryString) {
 		LogQuery q = queryService.createQuery(session, queryString);
 		context.println("created query " + q.getId());
 	}
 
 	private void startQuery(int id) {
 		LogQuery q = queryService.getQuery(id);
 		if (q == null) {
 			context.println("query not found");
 			return;
 		}
 
 		queryService.startQuery(q.getId());
 		context.println("started query " + id);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void printMap(Map<String, Object> m) {
 		boolean start = true;
 		context.print("{");
 		List<String> keySet = new ArrayList<String>(m.keySet());
 		Collections.sort(keySet);
 		for (String key : keySet) {
 			if (start)
 				start = false;
 			else
 				context.print(", ");
 
 			context.print(key + "=");
 			Object value = m.get(key);
 			if (value instanceof Map)
 				printMap((Map<String, Object>) value);
 			else if (value == null)
 				context.print("null");
 			else if (value.getClass().isArray())
 				context.print(Arrays.toString((Object[]) value));
 			else
 				context.print(value.toString());
 		}
 		context.println("}");
 	}
 
 	private void stopQuery(int id) {
 		LogQuery q = queryService.getQuery(id);
 		if (q != null) {
 			q.cancel();
 			context.println("stopped");
 		} else {
 			context.println("query not found: " + id);
 		}
 	}
 
 	private void removeQuery(int id) {
 		queryService.removeQuery(id);
		context.println("removed query " + id);
 	}
 
 	private void fetch(int id, long offset, long limit) throws IOException {
 		LogQuery q = queryService.getQuery(id);
 		if (q == null) {
 			context.println("query not found");
 			return;
 		}
 
 		LogResultSet result = q.getResult();
 		result.skip(offset);
 		for (long i = 0; result.hasNext() && i < limit; i++)
 			printMap(result.next());
 	}
 
 	private void removeAllQueries() {
 		for (LogQuery q : queryService.getQueries()) {
 			int id = q.getId();
 			queryService.removeQuery(id);
 			context.println("removed query " + id);
 		}
 		context.println("cleared all queries");
 	}
 
 	private void grantPrivilege(String loginName, String tableName) {
 		accountService.grantPrivilege(session, loginName, tableName, Permission.READ);
 		context.println("granted");
 	}
 
 	private void revokePrivilege(String loginName, String tableName) {
 		accountService.revokePrivilege(session, loginName, tableName, Permission.READ);
 		context.println("revoked");
 	}
 }
