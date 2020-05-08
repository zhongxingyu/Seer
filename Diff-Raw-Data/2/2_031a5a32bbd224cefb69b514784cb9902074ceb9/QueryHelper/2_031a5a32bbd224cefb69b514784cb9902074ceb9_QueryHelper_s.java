 /*
  * Copyright 2011 Future Systems
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
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.logdb.QueryService;
 import org.araqne.logdb.QueryResultSet;
 import org.araqne.logdb.Query;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.RunMode;
 import org.araqne.logdb.Session;
 import org.araqne.logdb.query.command.Fields;
 
 public class QueryHelper {
 	private static final int GENERAL_QUERY_FAILURE_CODE = 1;
 
 	private QueryHelper() {
 	}
 
 	public static void setJoinAndUnionDependencies(List<QueryCommand> commands) {
 		List<QueryCommand> joinCmds = new ArrayList<QueryCommand>();
 		List<QueryCommand> unionCmds = new ArrayList<QueryCommand>();
 
 		for (QueryCommand cmd : commands) {
 			if (cmd.getName().equals("join"))
 				joinCmds.add(cmd);
 
 			if (cmd.getName().equals("union"))
 				unionCmds.add(cmd);
 		}
 
 		for (QueryCommand cmd : commands) {
 			if (cmd.isDriver() && !cmd.getName().equals("join") && cmd.getMainTask() != null) {
 				for (QueryCommand join : joinCmds)
 					cmd.getMainTask().addDependency(join.getMainTask());
 			}
 
 			if (cmd.isDriver() && !cmd.getName().equals("join") && !cmd.getName().equals("union") && cmd.getMainTask() != null) {
 				for (QueryCommand union : unionCmds)
 					union.getMainTask().addDependency(cmd.getMainTask());
 			}
 		}
 
 		QueryCommand prevUnion = null;
 		for (QueryCommand union : unionCmds) {
 			if (prevUnion != null)
 				union.getMainTask().addDependency(prevUnion.getMainTask());
 
 			prevUnion = union;
 		}
 	}
 
 	public static List<Object> getQueries(Session session, QueryService service) {
 		List<Object> result = new ArrayList<Object>();
 		for (Query lq : service.getQueries(session)) {
 			result.add(getQuery(lq));
 		}
 		return result;
 	}
 
 	public static Map<String, Object> getQuery(Query q) {
 		Long msec = null;
 		if (q.isStarted()) {
 			if (q.isFinished())
 				msec = q.getFinishTime() - q.getStartTime();
 			else
 				msec = System.currentTimeMillis() - q.getStartTime();
 		}
 
 		List<Object> commands = new ArrayList<Object>();
 
 		if (q.getCommands() != null) {
 			for (QueryCommand cmd : q.getCommands()) {
 				Map<String, Object> c = new HashMap<String, Object>();
 				c.put("command", cmd.toString());
 				c.put("status", cmd.getStatus().toString());
 				c.put("push_count", cmd.getOutputCount());
 				commands.add(c);
 			}
 		}
 
 		List<Object> subQueries = new ArrayList<Object>();
 		for (Query subQuery : q.getContext().getQueries())
 			if (q != subQuery)
 				subQueries.add(serializeSubQuery(subQuery));
 
 		Map<String, Object> m = new HashMap<String, Object>();
 		m.put("id", q.getId());
 		m.put("query_string", q.getQueryString());
 		m.put("is_end", q.isFinished());
 		m.put("is_eof", q.isFinished());
 		m.put("is_cancelled", q.isCancelled());
 		m.put("start_time", q.getStartTime());
 		m.put("finish_time", q.getFinishTime());
 		m.put("last_started", new Date(q.getStartTime()));
 		m.put("elapsed", msec);
 		m.put("background", q.getRunMode() == RunMode.BACKGROUND);
 		m.put("commands", commands);
 		m.put("sub_queries", subQueries);
 		m.put("stamp", q.getNextStamp());
 
 		// @since 2.2.17
 		if (q.getCause() != null) {
 			m.put("error_code", GENERAL_QUERY_FAILURE_CODE);
 			m.put("error_detail", q.getCause().getMessage() != null ? q.getCause().getMessage() : q.getCause().getClass()
 					.getName());
 		}
 
 		return m;
 	}
 
 	private static Map<String, Object> serializeSubQuery(Query q) {
 		List<Object> commands = new ArrayList<Object>();
 		for (QueryCommand cmd : q.getCommands())
 			commands.add(serializeQueryCommand(cmd));
 
 		Map<String, Object> m = new HashMap<String, Object>();
 		m.put("id", q.getId());
 		if (commands.size() > 0)
 			m.put("commands", commands);
 		return m;
 	}
 
 	private static Map<String, Object> serializeQueryCommand(QueryCommand cmd) {
 		Map<String, Object> m = new HashMap<String, Object>();
 		List<Object> l = new ArrayList<Object>();
 		for (QueryCommand c : cmd.getNestedCommands()) {
 			l.add(serializeQueryCommand(c));
 		}
 
 		m.put("name", cmd.getName());
 		m.put("command", cmd.toString());
 		m.put("push_count", cmd.getOutputCount());
		m.put("status", cmd.getStatus());
 
 		if (l.size() > 0)
 			m.put("commands", l);
 		return m;
 	}
 
 	public static Map<String, Object> getResultData(QueryService qs, int id, int offset, int limit) throws IOException {
 		Query query = qs.getQuery(id);
 		if (query != null) {
 			Map<String, Object> m = new HashMap<String, Object>();
 
 			m.put("result", getPage(query, offset, limit));
 			m.put("count", query.getResultCount());
 
 			Fields fields = null;
 			for (QueryCommand command : query.getCommands()) {
 				if (command instanceof Fields) {
 					if (!((Fields) command).isSelector())
 						fields = (Fields) command;
 				}
 			}
 			if (fields != null)
 				m.put("fields", fields.getFields());
 
 			return m;
 		}
 		return null;
 	}
 
 	private static List<Object> getPage(Query query, int offset, int limit) throws IOException {
 		List<Object> l = new LinkedList<Object>();
 		QueryResultSet rs = null;
 		try {
 			rs = query.getResultSet();
 			rs.skip(offset);
 
 			long count = 0;
 			while (rs.hasNext()) {
 				if (count >= limit)
 					break;
 
 				l.add(rs.next());
 				count++;
 			}
 		} finally {
 			if (rs != null)
 				rs.close();
 		}
 		return l;
 	}
 
 }
