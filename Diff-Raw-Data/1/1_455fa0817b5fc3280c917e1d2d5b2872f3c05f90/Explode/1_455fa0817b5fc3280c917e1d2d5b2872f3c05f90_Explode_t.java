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
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.RowBatch;
 
 public class Explode extends QueryCommand {
 	private final String arrayFieldName;
 
 	public Explode(String arrayFieldName) {
 		this.arrayFieldName = arrayFieldName;
 	}
 
 	@Override
 	public String getName() {
 		return "explode";
 	}
 
 	@Override
 	public void onPush(Row row) {
 		Object o = row.get(arrayFieldName);
 		if (o == null)
 			return;
 
 		if (o instanceof Collection) {
 			Collection<?> c = (Collection<?>) o;
 			ArrayList<Row> rows = new ArrayList<Row>(c.size());
 			for (Object e : c) {
 				Row copy = new Row(row.map());
 				copy.put(arrayFieldName, e);
 				rows.add(copy);
 			}
 
 			RowBatch batch = new RowBatch();
 			batch.size = rows.size();
			batch.rows = new Row[batch.size];
 
 			int i = 0;
 			for (Row r : rows)
 				batch.rows[i++] = r;
 
 			pushPipe(batch);
 		} else {
 			pushPipe(row);
 		}
 	}
 
 	@Override
 	public void onPush(RowBatch rowBatch) {
 		int count = 0;
 
 		// count batch size
 		if (rowBatch.selectedInUse) {
 			for (int i = 0; i < rowBatch.size; i++) {
 				int p = rowBatch.selected[i];
 				Row row = rowBatch.rows[p];
 
 				Object o = row.get(arrayFieldName);
 				if (o == null)
 					continue;
 
 				if (o instanceof Collection) {
 					Collection<?> c = (Collection<?>) o;
 					count += c.size();
 				} else
 					count++;
 			}
 
 		} else {
 			for (Row r : rowBatch.rows) {
 				Object o = r.get(arrayFieldName);
 				if (o == null)
 					continue;
 
 				if (o instanceof Collection) {
 					Collection<?> c = (Collection<?>) o;
 					count += c.size();
 				} else
 					count++;
 			}
 		}
 
 		Row[] exploded = new Row[count];
 		RowBatch explodedBatch = new RowBatch();
 		explodedBatch.size = exploded.length;
 		explodedBatch.rows = exploded;
 		int index = 0;
 
 		// generate rows
 		if (rowBatch.selectedInUse) {
 			for (int i = 0; i < rowBatch.size; i++) {
 				int p = rowBatch.selected[i];
 				Row row = rowBatch.rows[p];
 
 				Object o = row.get(arrayFieldName);
 				if (o == null)
 					continue;
 
 				if (o instanceof Collection) {
 					Collection<?> c = (Collection<?>) o;
 					for (Object e : c) {
 						Row copy = new Row(row.map());
 						copy.put(arrayFieldName, e);
 						exploded[index++] = copy;
 					}
 				} else {
 					exploded[index++] = row;
 				}
 			}
 		} else {
 			for (Row row : rowBatch.rows) {
 				Object o = row.get(arrayFieldName);
 				if (o == null)
 					continue;
 
 				if (o instanceof Collection) {
 					Collection<?> c = (Collection<?>) o;
 					for (Object e : c) {
 						HashMap<String, Object> copyMap = new HashMap<String, Object>(row.map());
 						Row copy = new Row(copyMap);
 						copy.put(arrayFieldName, e);
 						exploded[index++] = copy;
 					}
 				} else {
 					exploded[index++] = row;
 				}
 			}
 		}
 
 		pushPipe(explodedBatch);
 	}
 
 	@Override
 	public String toString() {
 		return "explode " + arrayFieldName;
 	}
 }
