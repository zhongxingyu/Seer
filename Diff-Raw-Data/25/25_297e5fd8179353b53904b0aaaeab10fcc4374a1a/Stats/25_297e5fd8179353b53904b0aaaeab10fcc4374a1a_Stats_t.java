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
 
 import org.araqne.logdb.LogMap;
 import org.araqne.logdb.LogQueryCommand;
 import org.araqne.logdb.ObjectComparator;
 import org.araqne.logdb.query.aggregator.AggregationField;
 import org.araqne.logdb.query.aggregator.AggregationFunction;
 import org.araqne.logdb.sort.CloseableIterator;
 import org.araqne.logdb.sort.Item;
 import org.araqne.logdb.sort.ParallelMergeSorter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Stats extends LogQueryCommand {
 	private final Logger logger = LoggerFactory.getLogger(Stats.class);
 	private int inputCount;
 	private List<AggregationField> fields;
 	private List<String> clauses;
 	private final int clauseCount;
 	private final List<Object> EMPTY_KEY;
 
 	// clone template
 	private AggregationFunction[] funcs;
 
 	private ParallelMergeSorter sorter;
 
 	private Map<List<Object>, AggregationFunction[]> buffer;
 
 	public Stats(List<AggregationField> fields, List<String> clause) {
 		this.EMPTY_KEY = new ArrayList<Object>(0);
 		this.clauses = clause;
 		this.clauseCount = clauses.size();
 		this.sorter = new ParallelMergeSorter(new ItemComparer());
 		this.buffer = new HashMap<List<Object>, AggregationFunction[]>();
 		this.fields = fields;
 		this.funcs = new AggregationFunction[fields.size()];
 
 		// prepare template functions
 		for (int i = 0; i < fields.size(); i++)
 			this.funcs[i] = fields.get(i).getFunction();
 	}
 
 	public List<AggregationField> getAggregationFields() {
 		return fields;
 	}
 
 	public List<String> getClauses() {
 		return clauses;
 	}
 
 	@Override
 	public void init() {
 		super.init();
 
 		for (AggregationFunction f : funcs)
 			f.clean();
 	}
 
 	@Override
 	public void push(LogMap m) {
 		List<Object> keys = EMPTY_KEY;
 		if (clauseCount > 0) {
 			keys = new ArrayList<Object>(clauses.size());
 
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
 			if (buffer.keySet().size() > 50000)
 				flush();
 
 		} catch (IOException e) {
 			throw new IllegalStateException("sort failed, query " + logQuery, e);
 		}
 	}
 
 	private void flush() throws IOException {
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
 	public void eof(boolean canceled) {
 		this.status = Status.Finalizing;

 		logger.debug("araqne logdb: stats sort input count [{}]", inputCount);
 		CloseableIterator it = null;
 		try {
 			// last flush
 			flush();
 
 			// reclaim buffer (GC support)
 			buffer = null;
 
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
 					if (logger.isDebugEnabled() && lastKeys != null)
 						logger.debug("araqne logdb: stats key compare [{}] != [{}]", lastKeys[0], ((Object[]) item.getKey())[0]);
 
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
 			throw new IllegalStateException("sort failed, query " + logQuery, e);
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
 
 			super.eof(canceled);
 		}
 	}
 
 	private void pass(AggregationFunction[] fs, Object[] keys) {
 		Map<String, Object> m = new HashMap<String, Object>();
 
 		for (int i = 0; i < clauses.size(); i++)
 			m.put(clauses.get(i), keys[i]);
 
 		for (int i = 0; i < funcs.length; i++)
 			m.put(fields.get(i).getName(), fs[i].eval());
 
 		write(new LogMap(m));
 	}
 
 	private static class ItemComparer implements Comparator<Item> {
 		private ObjectComparator cmp = new ObjectComparator();
 
 		@Override
 		public int compare(Item o1, Item o2) {
			boolean o1null = o1 == null;
			boolean o2null = o2 == null;
			if (o1null && o2null)
				return 0;
			if (o1null)
				return 1;
			if (o2null)
				return -1;
 			return cmp.compare(o1.getKey(), o2.getKey());
 		}
 
 	}
 }
