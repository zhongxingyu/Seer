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
 package org.araqne.logdb.query.engine;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.araqne.logdb.Query;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryCommand.Status;
 import org.araqne.logdb.QueryStatusCallback;
 import org.araqne.logdb.QueryStopReason;
 import org.araqne.logdb.QueryTask;
 import org.araqne.logdb.QueryTimelineCallback;
 import org.araqne.logdb.QueryTask.TaskStatus;
 import org.araqne.logdb.QueryTaskEvent;
 import org.araqne.logdb.QueryTaskListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class QueryTaskScheduler implements Runnable {
 	private final Logger logger = LoggerFactory.getLogger(QueryTaskScheduler.class);
 
 	private boolean started;
 
 	// regardless of canceled or not, just finished
 	private boolean finished;
 
 	private long startTime;
 	private long finishTime;
 
 	private Query query;
 
 	// logical query command pipe
 	private List<QueryCommand> pipeline = new ArrayList<QueryCommand>();
 
 	// monitor task complete and start new ready tasks
 	private QueryTaskTracer tracer = new QueryTaskTracer();
 
 	public QueryTaskScheduler(Query query, List<QueryCommand> pipeline) {
 		this.query = query;
 		this.pipeline = pipeline;
 	}
 
 	public Query getQuery() {
 		return query;
 	}
 
 	public boolean isStarted() {
 		return started;
 	}
 
 	public long getStartTime() {
 		return startTime;
 	}
 
 	public boolean isFinished() {
 		return finished;
 	}
 
 	public long getFinishTime() {
 		return finishTime;
 	}
 
 	@Override
 	public void run() {
 		started = true;
 		startTime = System.currentTimeMillis();
 
 		for (QueryCommand cmd : pipeline) {
 			cmd.setStatus(Status.Running);
 
 			QueryTask mainTask = cmd.getMainTask();
 			if (mainTask != null) {
 				tracer.addDependency(mainTask);
 				mainTask.addListener(tracer);
 			}
 		}
 
 		startReadyTasks();
 	}
 
 	public void stop(QueryStopReason reason) {
 		for (QueryCommand cmd : pipeline) {
 			if (cmd.getMainTask() != null)
 				stopRecursively(cmd.getMainTask());
 		}
 
 		stopRecursively(tracer);
 	}
 
 	private synchronized void startReadyTasks() {
 		// later task runner can be completed before tracer.run(), and can cause
 		// duplicated query finish callback
 		boolean finished = tracer.isRunnable() || tracer.getStatus() == TaskStatus.CANCELED;
 
 		for (QueryCommand cmd : pipeline) {
 			QueryTask mainTask = cmd.getMainTask();
 			if (mainTask != null)
 				startRecursively(mainTask);
 		}
 
 		// all main task completed?
 		if (finished)
 			tracer.run();
 
 	}
 
 	private void startRecursively(QueryTask task) {
 		if (task.isRunnable()) {
 			// prevent duplicated run caused by late thread start
 			task.setStatus(TaskStatus.RUNNING);
 			new QueryTaskRunner(this, task).start();
 		} else {
			if (logger.isDebugEnabled() && task.getStatus() == TaskStatus.INIT) {
				StringBuilder sb = new StringBuilder();
				for (QueryTask d : task.getDependencies())
					sb.append(d + " " + d.getStatus() + "\n");

				logger.debug("araqne logdb: task [{}] is not runnable. dependencies => [{}]", task, sb.toString());
			}
 		}
 
 		for (QueryTask subTask : task.getSubTasks())
 			startRecursively(subTask);
 	}
 
 	private void stopRecursively(QueryTask task) {
 		if (task.getStatus() != TaskStatus.COMPLETED) {
 			task.setStatus(TaskStatus.CANCELED);
 			logger.debug("araqne logdb: canceled query task [{}]", task);
 		}
 
 		for (QueryTask subTask : task.getSubTasks())
 			stopRecursively(subTask);
 	}
 
 	private class QueryTaskTracer extends QueryTask implements QueryTaskListener {
 
 		@Override
 		public void run() {
 			if (logger.isDebugEnabled())
 				logger.debug("araqne logdb: all query [{}] task completed", query.getId());
 
 			query.postRun();
 			finished = true;
 			finishTime = System.currentTimeMillis();
 
 			// send final timeline callback
 			for (QueryTimelineCallback c : query.getCallbacks().getTimelineCallbacks()) {
 				try {
 					c.notifyTimeline();
 				} catch (Throwable t) {
 					logger.warn("araqne logdb: timeline callback should not throw any exception", t);
 				}
 			}
 
 			// notify finish immediately
 			for (QueryStatusCallback c : query.getCallbacks().getStatusCallbacks()) {
 				try {
 					c.onChange(query);
 				} catch (Throwable t) {
 					logger.warn("araqne logdb: change callback should not throw any exception", t);
 				}
 			}
 		}
 
 		@Override
 		public void onStart(QueryTaskEvent event) {
 		}
 
 		@Override
 		public void onComplete(QueryTaskEvent event) {
 			event.setHandled(true);
 
 			if (logger.isDebugEnabled())
 				logger.debug("araqne logdb: query task [{}] completed", event.getTask());
 		}
 
 		@Override
 		public void onCleanUp(QueryTaskEvent event) {
 			startReadyTasks();
 		}
 	}
 }
