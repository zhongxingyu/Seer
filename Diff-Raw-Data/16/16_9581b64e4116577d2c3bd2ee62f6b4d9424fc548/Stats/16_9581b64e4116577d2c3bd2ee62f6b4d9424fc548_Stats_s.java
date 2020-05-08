 /*
  * Copyright 2012 Future Systems
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
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.araqne.logdb.FieldOrdering;
 import org.araqne.logdb.ObjectComparator;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryStopReason;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.RowBatch;
 import org.araqne.logdb.query.aggregator.AggregationField;
 import org.araqne.logdb.query.aggregator.AggregationFunction;
 import org.araqne.logdb.sort.CloseableIterator;
 import org.araqne.logdb.sort.Item;
 import org.araqne.logdb.sort.ParallelMergeSorter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Stats extends QueryCommand implements FieldOrdering {
 	private final Logger logger = LoggerFactory.getLogger(Stats.class);
 	private final Logger compareLogger = LoggerFactory.getLogger("stats-key-compare");
 	private int inputCount;
 	private final List<AggregationField> fields;
 	private final List<String> clauses;
 	private final int clauseCount;
 	private final boolean useClause;
 	private final List<Object> EMPTY_KEY;
 
 	// clone template
 	private AggregationFunction[] funcs;
 
 	private ParallelMergeSorter sorter;
 
 	private ConcurrentMap<List<Object>, AggregationFunction[]> buffer;
 
 	private ArrayList<String> fieldOrder;
 
 	public Stats(List<AggregationField> fields, List<String> clause) {
 		this.EMPTY_KEY = new ArrayList<Object>(0);
 		this.clauses = clause;
 		this.clauseCount = clauses.size();
 		this.useClause = clauseCount > 0;
 		this.sorter = new ParallelMergeSorter(new ItemComparer());
 		this.buffer = new ConcurrentHashMap<List<Object>, AggregationFunction[]>();
 		this.fields = fields;
 		this.funcs = new AggregationFunction[fields.size()];
 		this.fieldOrder = new ArrayList<String>(clauses);
 
 		// prepare template functions
 		for (int i = 0; i < fields.size(); i++) {
 			AggregationField f = fields.get(i);
 			this.funcs[i] = f.getFunction();
 			this.fieldOrder.add(f.getName());
 		}
 	}
 
 	@Override
 	public String getName() {
 		return "stats";
 	}
 
 	@Override
 	public List<String> getFieldOrder() {
 		return new ArrayList<String>(fieldOrder);
 	}
 
 	public List<AggregationField> getAggregationFields() {
 		return fields;
 	}
 
 	public List<String> getClauses() {
 		return clauses;
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		for (AggregationFunction f : funcs)
 			f.clean();
 	}
 
 	@Override
 	public void onPush(RowBatch rowBatch) {
 		List<Object> keys = EMPTY_KEY;
 
 		if (useClause)
 			keys = new ArrayList<Object>(clauseCount);
 
 		if (rowBatch.selectedInUse) {
 			for (int index = 0; index < rowBatch.size; index++) {
 				keys.clear();
 				Row row = rowBatch.rows[rowBatch.selected[index]];
 				if (useClause) {
 					boolean isNullGroup = false;
 					for (String clause : clauses) {
 						Object keyValue = row.get(clause);
						if (keyValue == null) {
 							isNullGroup = true;
 							break;
 						}
 
 						keys.add(keyValue);
 					}
 
 					if (isNullGroup)
 						continue;
 				}
 
 				inputCount++;
 
 				AggregationFunction[] fs = buffer.get(keys);
 				if (fs == null) {
 					fs = new AggregationFunction[funcs.length];
 					for (int i = 0; i < fs.length; i++)
 						fs[i] = funcs[i].clone();
 
 					buffer.put(new ArrayList<Object>(keys), fs);
 				}
 
 				for (AggregationFunction f : fs)
 					f.apply(row);
 			}
 		} else {
 			for (Row m : rowBatch.rows) {
 				if (useClause) {
 					keys.clear();
 					boolean isNullGroup = false;
 					for (String clause : clauses) {
 						Object keyValue = m.get(clause);
						if (keyValue == null) {
 							isNullGroup = true;
 							break;
 						}
 
 						keys.add(keyValue);
 					}
 
 					if (isNullGroup)
 						continue;
 				}
 
 				inputCount++;
 
 				AggregationFunction[] fs = buffer.get(keys);
 				if (fs == null) {
 					fs = new AggregationFunction[funcs.length];
 					for (int i = 0; i < fs.length; i++)
 						fs[i] = funcs[i].clone();
 
 					buffer.put(new ArrayList<Object>(keys), fs);
 				}
 
 				for (AggregationFunction f : fs)
 					f.apply(m);
 			}
 		}
 
 		try {
 			// flush
 			if (buffer.size() > 50000)
 				flush();
 		} catch (IOException e) {
 			throw new IllegalStateException("stats failed, query " + query, e);
 		}
 	}
 
 	@Override
 	public void onPush(Row m) {
 		List<Object> keys = EMPTY_KEY;
 		if (clauseCount > 0) {
 			keys = new ArrayList<Object>(clauseCount);
 
 			for (String clause : clauses) {
 				Object keyValue = m.get(clause);
				if (keyValue == null)
 					return;
 
 				keys.add(keyValue);
 			}
 		}
 
 		try {
 			inputCount++;
 
 			AggregationFunction[] fs = buffer.get(keys);
 			if (fs == null) {
 				fs = new AggregationFunction[funcs.length];
 				for (int i = 0; i < fs.length; i++)
 					fs[i] = funcs[i].clone();
 
 				buffer.put(keys, fs);
 			}
 
 			for (AggregationFunction f : fs)
 				f.apply(m);
 
 			// flush
 			if (buffer.size() > 50000)
 				flush();
 
 		} catch (IOException e) {
 			throw new IllegalStateException("stats failed, query " + query, e);
 		}
 	}
 
 	private void flush() throws IOException {
 		if (logger.isDebugEnabled())
 			logger.debug("araqne logdb: flushing stats buffer, [{}] keys", buffer.keySet().size());
 
 		for (List<Object> keys : buffer.keySet()) {
 			AggregationFunction[] fs = buffer.get(keys);
 			Object[] l = new Object[fs.length];
 			int i = 0;
 			for (AggregationFunction f : fs)
 				l[i++] = f.serialize();
 
 			sorter.add(new Item(keys.toArray(), l));
 		}
 
 		buffer.clear();
 	}
 
 	@Override
 	public boolean isReducer() {
 		return true;
 	}
 
 	@Override
 	public void onClose(QueryStopReason reason) {
 		this.status = Status.Finalizing;
 
 		logger.debug("araqne logdb: stats sort input count [{}]", inputCount);
 		CloseableIterator it = null;
 		try {
 			// last flush
 			flush();
 
 			// reclaim buffer (GC support)
 			buffer = new ConcurrentHashMap<List<Object>, AggregationFunction[]>();
 
 			// sort
 			it = sorter.sort();
 
 			Object[] lastKeys = null;
 			AggregationFunction[] fs = null;
 			Item item = null;
 			int count = 0;
 			while (it.hasNext()) {
 				item = (Item) it.next();
 				count++;
 
 				// first record or need to change merge set?
 				if (lastKeys == null || !Arrays.equals(lastKeys, (Object[]) item.getKey())) {
 					if (compareLogger.isDebugEnabled() && lastKeys != null)
 						compareLogger.debug("araqne logdb: stats key compare [{}] != [{}]", lastKeys[0],
 								((Object[]) item.getKey())[0]);
 
 					// finalize last record (only if changing set)
 					if (fs != null) {
 						pass(fs, lastKeys);
 					}
 
 					// load new record
 					fs = new AggregationFunction[funcs.length];
 					int i = 0;
 					Object[] rawFuncs = (Object[]) item.getValue();
 					for (Object rawFunc : rawFuncs) {
 						Object[] l = (Object[]) rawFunc;
 						AggregationFunction f = funcs[i].clone();
 						f.deserialize(l);
 						fs[i++] = f;
 					}
 				} else {
 					// merge
 					int i = 0;
 					for (AggregationFunction f : fs) {
 						Object[] l = (Object[]) ((Object[]) item.getValue())[i];
 						AggregationFunction f2 = funcs[i].clone();
 						f2.deserialize(l);
 						f.merge(f2);
 						i++;
 					}
 				}
 
 				lastKeys = (Object[]) item.getKey();
 			}
 
 			// write last merge set
 			if (item != null)
 				pass(fs, lastKeys);
 
 			// write result for empty data set (only for no group clause)
 			if (inputCount == 0 && clauses.size() == 0) {
 				// write initial function values
 				pass(funcs, null);
 			}
 
 			logger.debug("araqne logdb: sorted stats input [{}]", count);
 		} catch (IOException e) {
 			throw new IllegalStateException("sort failed, query " + query, e);
 		} finally {
 			if (it != null) {
 				try {
 					// close and delete final sorted run file
 					it.close();
 				} catch (IOException e) {
 				}
 			}
 
 			// support sorter cache GC when query processing is ended
 			sorter = null;
 		}
 	}
 
 	private void pass(AggregationFunction[] fs, Object[] keys) {
 		Map<String, Object> m = new HashMap<String, Object>();
 
 		for (int i = 0; i < clauses.size(); i++)
 			m.put(clauses.get(i), keys[i]);
 
 		for (int i = 0; i < funcs.length; i++)
 			m.put(fields.get(i).getName(), fs[i].eval());
 
 		pushPipe(new Row(m));
 	}
 
 	private static class ItemComparer implements Comparator<Item> {
 		private ObjectComparator cmp = new ObjectComparator();
 
 		@Override
 		public int compare(Item o1, Item o2) {
 			return cmp.compare(o1.getKey(), o2.getKey());
 		}
 	}
 
 	@Override
 	public String toString() {
 		String aggregation = "";
 		int i = 0;
 		for (AggregationField f : this.fields) {
 			if (i++ != 0)
 				aggregation += ",";
 			aggregation += " " + f.toString();
 		}
 
 		String clause = "";
 		if (!clauses.isEmpty()) {
 			clause = " by";
 			i = 0;
 			for (String c : clauses) {
 				if (i++ != 0)
 					clause += ",";
 				clause += " " + c;
 			}
 		}
 
 		return "stats" + aggregation + clause;
 	}
 
 }
