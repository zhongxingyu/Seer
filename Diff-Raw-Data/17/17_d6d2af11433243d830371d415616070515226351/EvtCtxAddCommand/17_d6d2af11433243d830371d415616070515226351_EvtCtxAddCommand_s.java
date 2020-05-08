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
 package org.araqne.logdb.cep.query;
 
 import java.util.Date;
 
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.TimeSpan;
 import org.araqne.logdb.cep.EventContext;
 import org.araqne.logdb.cep.EventContextStorage;
 import org.araqne.logdb.cep.EventKey;
 import org.araqne.logdb.query.expr.Expression;
 
 public class EvtCtxAddCommand extends QueryCommand {
 
 	private EventContextStorage storage;
 	private String topic;
 	private String keyField;
 	private TimeSpan expire;
 	private TimeSpan timeout;
 	private int threshold;
 	private int maxRows;
 	private Expression matcher;
 
 	// host field for external clock
 	private String hostField;
 
 	public EvtCtxAddCommand(EventContextStorage storage, String topic, String keyField, TimeSpan expire, TimeSpan timeout,
 			int threshold, int maxRows, Expression matcher, String hostField) {
 		this.storage = storage;
 		this.topic = topic;
 		this.keyField = keyField;
 		this.expire = expire;
 		this.timeout = timeout;
 		this.threshold = threshold;
 		this.maxRows = maxRows;
 		this.matcher = matcher;
 		this.hostField = hostField;
 	}
 
 	@Override
 	public String getName() {
 		return "evtctxadd";
 	}
 
 	@Override
 	public void onPush(Row row) {
 		boolean matched = true;
 
 		Object o = matcher.eval(row);
 		if (o == null)
 			matched = false;
 
 		if (o instanceof Boolean && !(Boolean) o)
 			matched = false;
 
 		Object k = row.get(keyField);
 		if (k == null)
 			matched = false;
 
 		// extract host for log tick
		String host = null;
 		Object h = null;
 
 		if (hostField != null)
 			h = row.get(hostField);
 
 		if (h != null)
			host = h.toString();
 
 		// extract log time
 		long created = 0;
 		Date logTime = null;
 		Object t = row.get("_time");
		if (t instanceof Date) {
 			logTime = (Date) t;
 			created = logTime.getTime();
 		} else {
 			created = System.currentTimeMillis();
 		}
 
 		if (matched) {
 			String key = k.toString();
 			EventKey eventKey = new EventKey(topic, key);
 
 			long expireTime = 0;
 			if (expire != null)
 				expireTime = created + expire.unit.getMillis() * expire.amount;
 
 			long timeoutTime = 0;
 			if (timeout != null)
 				timeoutTime = created + timeout.unit.getMillis() * timeout.amount;
 
			EventContext ctx = new EventContext(eventKey, created, expireTime, timeoutTime, threshold, maxRows, (String) host);
 			ctx = storage.addContext(ctx);
 			ctx.getCounter().incrementAndGet();
 
 			// extend timeout
 			ctx.setTimeoutTime(timeoutTime);
 			ctx.addRow(row);
 		}
 
		if (host != null && logTime != null) {
 			Object date = row.get("_time");
 			if (date instanceof Date)
				storage.advanceTime(host, logTime.getTime());
 		}
 
 		pushPipe(row);
 	}
 
 	@Override
 	public String toString() {
 		String s = "evtctxadd topic=" + topic + " key=" + keyField;
 
 		if (threshold != 0)
 			s += " threshold=" + threshold;
 
 		if (expire != null)
 			s += " expire=" + expire;
 
 		if (timeout != null)
 			s += " timeout=" + timeout;
 
 		if (maxRows != 10)
 			s += " maxrows=" + maxRows;
 
 		if (hostField != null)
 			s += " logtick=" + hostField;
 
 		s += " " + matcher;
 
 		return s;
 	}
 }
