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
 package org.araqne.logdb.query.command;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.log.api.LogParser;
 import org.araqne.log.api.LogParserInput;
 import org.araqne.log.api.LogParserOutput;
 import org.araqne.logdb.FieldOrdering;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.RowBatch;
 import org.araqne.logdb.ThreadSafe;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @since 1.6.6
  * @author xeraph
  * 
  */
 public class Parse extends QueryCommand implements ThreadSafe, FieldOrdering {
 	private final Logger logger = LoggerFactory.getLogger(Parse.class);
 	private final int parserVersion;
 	private final String parserName;
 	private final LogParser parser;
 	private final boolean overlay;
 
 	public Parse(String parserName, LogParser parser, boolean overlay) {
 		this.parserName = parserName;
 		this.parser = parser;
 		this.parserVersion = parser.getVersion();
 		this.overlay = overlay;
 	}
 
 	@Override
 	public String getName() {
 		return "parse";
 	}
 
 	@Override
 	public List<String> getFieldOrder() {
 		if (parser instanceof FieldOrdering)
 			return ((FieldOrdering) parser).getFieldOrder();
 		return null;
 	}
 
 	@Override
 	public void onPush(RowBatch rowBatch) {
 		// TODO: boost v2 performance
 		if (parserVersion == 2) {
 			if (rowBatch.selectedInUse) {
 				for (int i = 0; i < rowBatch.size; i++) {
 					Row row = rowBatch.rows[rowBatch.selected[i]];
 					onPush(row);
 				}
 			} else {
 				for (int i = 0; i < rowBatch.size; i++) {
 					Row row = rowBatch.rows[i];
 					onPush(row);
 				}
 			}
 
 			return;
 		}
 
 		int n = 0;
 		if (rowBatch.selectedInUse) {
 			for (int i = 0; i < rowBatch.size; i++) {
 				int p = rowBatch.selected[i];
 				Row row = rowBatch.rows[p];
				try {
					Row parsed = parseV1(row);
					if (parsed != null) {
						rowBatch.selected[n] = p;
						rowBatch.rows[p] = parsed;
						n++;
					}
				} catch (Throwable t) {
					if (logger.isDebugEnabled())
						logger.debug("araqne logdb: cannot parse " + row.map() + ", query - " + toString(), t);
 				}
 			}
 		} else {
 			rowBatch.selected = new int[rowBatch.size];
 			for (int i = 0; i < rowBatch.size; i++) {
 				Row row = rowBatch.rows[i];
				try {
					Row parsed = parseV1(row);
					if (parsed != null) {
						rowBatch.selected[n] = i;
						rowBatch.rows[i] = parsed;
						n++;
					}
				} catch (Throwable t) {
					if (logger.isDebugEnabled())
						logger.debug("araqne logdb: cannot parse " + row.map() + ", query - " + toString(), t);
 				}
 			}
 		}
 
 		if (!rowBatch.selectedInUse && rowBatch.size != n)
 			rowBatch.selectedInUse = true;
 
 		rowBatch.size = n;
 
 		pushPipe(rowBatch);
 	}
 
 	@Override
 	public void onPush(Row row) {
 		try {
 			LogParserInput input = new LogParserInput();
 
 			if (parserVersion == 2) {
 				Object table = row.get("_table");
 				Object time = row.get("_time");
 				Object id = row.get("_id");
 
 				if (time != null && time instanceof Date)
 					input.setDate((Date) time);
 				else
 					input.setDate(null);
 
 				if (table != null && table instanceof String)
 					input.setSource((String) table);
 				else
 					input.setSource(null);
 
 				input.setData(row.map());
 
 				LogParserOutput output = parser.parse(input);
 				if (output != null) {
 					for (Map<String, Object> out : output.getRows()) {
 						if (id != null && !out.containsKey("_id"))
 							out.put("_id", id);
 						if (time != null && !out.containsKey("_time"))
 							out.put("_time", row.get("_time"));
 						if (table != null && !out.containsKey("_table"))
 							out.put("_table", row.get("_table"));
 
 						if (overlay) {
 							Map<String, Object> source = new HashMap<String, Object>(row.map());
 							source.putAll(out);
 							pushPipe(new Row(source));
 						} else {
 							pushPipe(new Row(out));
 						}
 					}
 				}
 			} else {
 				Row parsed = parseV1(row);
 				if (parsed != null)
 					pushPipe(parsed);
 			}
 		} catch (Throwable t) {
 			if (logger.isDebugEnabled())
 				logger.debug("araqne logdb: cannot parse " + row.map() + ", query - " + toString(), t);
 		}
 	}
 
 	private Row parseV1(Row row) {
 		Map<String, Object> parsed = parser.parse(row.map());
 		if (parsed == null)
 			return null;
 
 		Object id = row.get("_id");
 		Object time = row.get("_time");
 		Object table = row.get("_table");
 
 		if (id != null && !parsed.containsKey("_id"))
 			parsed.put("_id", id);
 
 		if (time != null && !parsed.containsKey("_time"))
 			parsed.put("_time", time);
 
 		if (table != null && !parsed.containsKey("_table"))
 			parsed.put("_table", table);
 
 		if (overlay) {
 			Map<String, Object> source = new HashMap<String, Object>(row.map());
 			source.putAll(parsed);
 			return new Row(source);
 		} else
 			return new Row(parsed);
 	}
 
 	@Override
 	public String toString() {
 		if (parser instanceof ParseWithAnchor) {
 			return ((ParseWithAnchor) parser).toQueryCommandString();
 		} else {
 			return "parse " + parserName;
 		}
 	}
 }
