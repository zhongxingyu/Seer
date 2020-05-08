 /**
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
 
 import org.araqne.logdb.Query;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryStopReason;
 import org.araqne.logdb.QueryTask;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @since 2.2.13
  * @author xeraph
  */
 public class Union extends QueryCommand {
 	private final Logger slog = LoggerFactory.getLogger(Union.class);
 	private Query subQuery;
 
 	private SubQueryTask subQueryTask = new SubQueryTask();
 
 	public Union() {
 	}
 
 	@Override
 	public QueryTask getMainTask() {
 		return subQueryTask;
 	}
 
 	public void setSubQuery(Query subQuery) {
 		this.subQuery = subQuery;
 
 		for (QueryCommand cmd : subQuery.getCommands()) {
 			if (cmd.getMainTask() != null) {
 				subQueryTask.addDependency(cmd.getMainTask());
 				subQueryTask.addSubTask(cmd.getMainTask());
 			}
 		}
 	}
 
 	@Override
 	public String getName() {
 		return "union";
 	}
 
 	@Override
 	public void onStart() {
 		subQuery.preRun();
 	}
 
 	@Override
 	public void onClose(QueryStopReason reason) {
 		try {
 			subQuery.stop(reason);
 		} catch (Throwable t) {
 			slog.error("araqne logdb: cannot stop union subquery [" + subQuery.getQueryString() + "]", t);
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "union [ " + subQuery.getQueryString() + " ]";
 	}
 
 	private class SubQueryTask extends QueryTask {
 
 		@Override
 		public void run() {
 			slog.debug("araqne logdb: union subquery end, main query [{}] sub query [{}]", query.getId(), subQuery.getId());
 			subQuery.postRun();
 		}
 	}
 }
