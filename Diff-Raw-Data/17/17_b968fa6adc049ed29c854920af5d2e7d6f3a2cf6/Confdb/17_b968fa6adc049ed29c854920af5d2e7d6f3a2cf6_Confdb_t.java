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
 package org.araqne.logdb.query.command;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.confdb.CommitLog;
 import org.araqne.confdb.Config;
 import org.araqne.confdb.ConfigCollection;
 import org.araqne.confdb.ConfigDatabase;
 import org.araqne.confdb.ConfigIterator;
 import org.araqne.confdb.ConfigService;
 import org.araqne.logdb.DriverQueryCommand;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.Row;
 
 /**
  * @since 2.1.2
  * @author xeraph
 * 
  */
 public class Confdb extends DriverQueryCommand {
 	private ConfigService conf;
 	private ConfdbOptions options;
 
 	public Confdb(ConfigService conf, ConfdbOptions options) {
 		this.conf = conf;
 		this.options = options;
 	}
 
 	@Override
 	public String getName() {
 		return "confdb";
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void run() {
 		if (options.op == Confdb.Op.DATABASES) {
 			for (String name : conf.getDatabaseNames()) {
 				ConfigDatabase db = conf.getDatabase(name);
				long commitCount = 0;

				List<CommitLog> logs = null;
				try {
					commitCount = db.getCommitCount();
					logs = db.getCommitLogs(0, 1);
				} catch (Throwable t) {
				}

 				long rev = 0;
 				String lastMsg = null;
 				Date lastCommit = null;
				if (logs != null && !logs.isEmpty()) {
 					CommitLog last = logs.get(0);
 					rev = last.getRev();
 					lastCommit = last.getCreated();
 					lastMsg = last.getMessage();
 				}
 
 				Row row = new Row();
 				row.put("name", name);
 				row.put("rev", rev);
 				row.put("commits", commitCount);
 				row.put("last_msg", lastMsg);
 				row.put("last_commit", lastCommit);
 				pushPipe(row);
 			}
 		} else if (options.op == Op.COLS) {
 			ConfigDatabase db = conf.getDatabase(options.dbName);
 			if (db == null)
 				return;
 
 			for (String colName : db.getCollectionNames()) {
 				Row row = new Row();
 				row.put("name", colName);
 				pushPipe(row);
 			}
 		} else if (options.op == Op.DOCS) {
 			ConfigDatabase db = conf.getDatabase(options.dbName);
 			if (db == null)
 				return;
 
 			ConfigCollection col = db.getCollection(options.colName);
 			if (col == null)
 				return;
 
 			ConfigIterator it = col.findAll();
 			try {
 				while (it.hasNext()) {
 					Config c = it.next();
 
 					Row row = new Row();
 					row.put("_doc_id", c.getId());
 					row.put("_doc_prev", c.getPrevRevision());
 					row.put("_doc_rev", c.getRevision());
 
 					if (c.getDocument() != null && c.getDocument() instanceof Map) {
 						row.map().putAll((Map<String, Object>) c.getDocument());
 					} else {
 						row.put("_doc", c.getDocument());
 					}
 
 					pushPipe(row);
 				}
 			} finally {
 				it.close();
 			}
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "confdb " + options;
 	}
 
 	public static enum Op {
 		DATABASES, COLS, DOCS;
 
 		public static Op parse(String s) {
 			for (Op p : values()) {
 				if (p.name().toLowerCase().equals(s))
 					return p;
 			}
 
 			throw new QueryParseException("invalid-confdb-op", -1, s);
 		}
 	};
 
 	public static class ConfdbOptions {
 
 		private Op op;
 		private String dbName;
 		private String colName;
 
 		public Op getOp() {
 			return op;
 		}
 
 		public void setOp(Op op) {
 			this.op = op;
 		}
 
 		public String getDbName() {
 			return dbName;
 		}
 
 		public void setDbName(String dbName) {
 			this.dbName = dbName;
 		}
 
 		public String getColName() {
 			return colName;
 		}
 
 		public void setColName(String colName) {
 			this.colName = colName;
 		}
 
 		@Override
 		public String toString() {
 			String s = op.name().toLowerCase();
 			if (op == Op.COLS)
 				s += " " + dbName;
 			else if (op == Op.DOCS)
 				s += " " + dbName + " " + colName;
 
 			return s;
 		}
 
 	}
 }
