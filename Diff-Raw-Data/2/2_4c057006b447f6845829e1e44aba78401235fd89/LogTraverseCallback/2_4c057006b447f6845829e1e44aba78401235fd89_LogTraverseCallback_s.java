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
 
 package org.araqne.logstorage;
 
 import java.util.List;
 
 public abstract class LogTraverseCallback {
 	private final Synchronizer synchronizer;
 	abstract public void interrupt();
 	abstract public boolean isInterrupted();
 	
 	public boolean isEof() {
 		return synchronizer.isEof();
 	}
 	
 	public LogTraverseCallback(Synchronizer synchronizer) {
 		this.synchronizer = synchronizer;
 	}
 	
 	public void writeLogs(List<Log> logs) {
 		synchronizer.write(filter(logs));
 	}
 	
 	abstract protected List<Log> filter(List<Log> logs);
 
 	public static abstract class Synchronizer {
 		private final long offset;
 		private final long limit;
 		private long curr;
 		private boolean eof;
 		
 		public Synchronizer(long offset, long limit) {
 			this.offset = offset;
 			this.limit = limit;
 			this.curr = 0;
 			this.eof = false;
 		}
 		
 		public boolean isEof() {
 			return eof;
 		}
 		
 		// returns whether result is end or not
 		public boolean write(List<Log> logs) {
 			if (logs.isEmpty())
 				return !eof;
 			
 			long start = curr;
 			curr += logs.size();
 			
 			if (eof)
 				return false;
 			
 			if (offset > 0 && curr <= offset)
 				return true;
 			
 			int processBegin = 0;
 			int processEnd = logs.size();
 			
			if (start <= offset) {
 				processBegin = (int)(offset - start);
 			}
 			
 			if (limit > 0 && curr >= offset + limit) {
 				processEnd = (int)(offset + limit - start);
 				eof = true;
 			}
 			
 			if (processBegin == 0 && processEnd == logs.size())
 				processLogs(logs);
 			else
 				processLogs(logs.subList(processBegin, processEnd));
 			
 			return !eof;
 		}
 		
 		protected abstract void processLogs(List<Log> logs);
 	}
 	
 }
