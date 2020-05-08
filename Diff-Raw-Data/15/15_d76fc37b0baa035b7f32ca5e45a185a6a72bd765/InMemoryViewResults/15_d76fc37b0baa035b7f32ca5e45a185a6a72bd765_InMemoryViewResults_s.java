 /*
 * Copyright 2008 The MemeDB Contributors (see CONTRIBUTORS)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
 
 package memedb.views;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.List;
 import java.util.Iterator;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentSkipListMap;
 import java.util.concurrent.ConcurrentNavigableMap;
 import java.util.concurrent.locks.ReentrantLock;
 
 import memedb.MemeDB;
 import memedb.document.Document;
 import memedb.state.StateEvent;
 import memedb.utils.Logger;
 import memedb.utils.FileUtils;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.JSONException;
 
 /**
 * InMemoryViewResults are stored in RAM until the server shuts down,
 * at which point they are serialized to disk.
 * @author Russell Weir
 **/
 public class InMemoryViewResults implements Serializable, ViewResults, MapResultConsumer {
 	static final long serialVersionUID = -8013847062047838865L;
 
 	private class ResultsUpdater extends Thread {
 		boolean stop = false;
 		final private View view;
 		final private InMemoryViewResults vr;
 		final private Logger log;
 		final private String db;
 		final private long maxSequence;
 		final private ReentrantLock lock;
 		private String id;
 
 		public ResultsUpdater(String db, String docName, String functionName, View view, InMemoryViewResults viewResults, long seqNo, ReentrantLock lock)
 		{
 			this.view = view;
 			this.vr = viewResults;
 			this.db = db;
 			this.lock = lock;
 			this.id = db+"/"+docName+"/"+functionName;
 			this.maxSequence = seqNo;
 			log = Logger.get("viewResultsUpdater:"+id);
 		}
 
 		public long syncingTo() {
 			return maxSequence;
 		}
 
 		@Override
 		public void run() {
 			log.debug("////////////// Started view results updater thread {} {}/{}", id, vr.getCurrentSequenceNumber(), maxSequence);
 			if(view == null || vr == null) return;
 			lock.lock();
 			try {
 				long vrseq = vr.getCurrentSequenceNumber();
 				//Iterator<Document> it = null;
 				if(vrseq < 0) {
 					for(Document doc: memeDB.getBackend().allDocuments(db)) {
 						if(!vr.isAlive())
 							throw new Exception();
 						if(doc.getSequence() > maxSequence) {
 							log.debug("\t{} SeqNo {} higher than {}", id, doc.getSequence(), maxSequence);
 							continue;
 						}
 						if(stop) {
 							// here we have no way of knowing what docs
 							// are indexed on next load. The results
 							// must be cleared
 							vr.clearFromUpdater();
 							throw new Exception();
 						}
 						vr.doMap(doc);
 					}
 				}
 				else {
 					long max = memeDB.getState().getCurrentSequenceNumber() - vrseq;
 					for(StateEvent se: memeDB.getState().eventsFromSequence(vrseq, max)) {
 						if(!vr.isAlive() || stop)
 							throw new Exception();
 						if(!se.isForDatabase(db)) continue;
 						if(se.isDatabaseEvent()) {
 							vr.clearFromUpdater();
 							continue;
 						}
 						if(se.isDocumentDelete()) {
 							vr.removeResult(se.getDocumentId(), se.getSequence());
 						}
 						else {
 							Document doc = se.getDocumentAtRevision(memeDB.getBackend());
 							doc.setSequence(se.getSequence());
 							vr.doMap(doc);
 							log.debug("\t{} mapped {} seq {}",id,doc.getId(),doc.getSequence());
 						}
 					}
 				}
 			}
 			catch(Exception e) {}
 			finally {
 				lock.unlock();
 			}
 			log.debug("////////////// View results updater thread complete "+id);
 		}
 
 		@Override
 		public void interrupt() {
 			this.stop = true;
 			super.interrupt();
 		}
 	};
 
 	private final class MapResultEntry {
 		public final Document doc;
 		public final long seqNo;
 		public final long timestamp;
 
 		private JSONArray results;
 		private boolean hasResult = false;
 		public MapResultEntry(Document doc) {
 			this.doc = doc;
 			this.seqNo = doc.getSequence();
 			results = null;
 			hasResult = false;
 			this.timestamp = new java.util.Date().getTime();
 		}
 		final public JSONArray getResults() {
 			return results;
 		}
 		final public boolean hasResults() {
 			return hasResult;
 		}
 		final public void setResults(JSONArray r) {
 			results = r;
 			hasResult = true;
 		}
 	};
 
 	/*
 	* Now why would it be that ArrayList.removeRange is protected?
 	*/
 	private class ArrayListExp<T> extends ArrayList<T> {
 		private static final long serialVersionUID = -5912244629083505854L;
 		public void rangeRemove(int fromIndex, int toIndex) {
 			int l = size();
 			if (l != 0) {
 				if(fromIndex < l ) {
 					if(toIndex > l)
 						toIndex = l;
 					removeRange(fromIndex, toIndex);
 				}
 			}
 		}
 	}
 	
 	protected final static String PATH_PROPERTY		= "viewresults.inmemory.path";
 	protected final static String VIEW_RESULTS_NAME = "view.res";
 
 	// id -> ArrayList<JSON data> results from each doc map
 	protected ConcurrentHashMap<String,ArrayList<JSONObject>>
 		idMap = new ConcurrentHashMap<String,ArrayList<JSONObject>>();
 
 	/*
 	// key -> ListSet[ JSON Data] results for each key
 	private ConcurrentSkipListMap<Object, CopyOnWriteArrayList<JSONObject>>
 		keyMap = new ConcurrentSkipListMap<Object, CopyOnWriteArrayList<JSONObject>>(new Collate());
 
 	// key -> Reduce result Object
 	private ConcurrentSkipListMap<Object, Object>
 		reduceMap = new ConcurrentSkipListMap<Object, Object>(new Collate());
 	*/
 	private ConcurrentSkipListMap<Object, KeyValueEntries>
 		veMap = new ConcurrentSkipListMap<Object, KeyValueEntries>(new Collate());
 
 	// the highest sequence number yet indexed
 	private long currentSequenceNumber;
 	final private String db;
 	final private String docName;
 	final private String functionName;
 	final private String id;
 	private File baseDir;
 	private Object mgrObject;
 	private View view;
 	private String map_src;
 	private String reduce_src;
 
 	transient protected MemeDB memeDB;
 	transient protected Logger log;
 	transient private boolean shutdown;
 	transient private boolean destroy;
 	transient private ResultsUpdater updater;
 	transient private boolean isAlive;
 	transient private ReentrantLock lock; // results lock for updater thread
 	transient private ConcurrentSkipListMap<Long, MapResultEntry> expectedResults;
 
 	///////////////////////////////////////
 	//       Java deserializtion         //
 	///////////////////////////////////////
 	private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException {
 		in.defaultReadObject();
 		log = Logger.get(InMemoryViewResults.class);
 		shutdown = false;
 		destroy = false;
 		isAlive = false;
 		lock = new ReentrantLock(true);
 		this.expectedResults = new ConcurrentSkipListMap<Long, MapResultEntry>();
 	}
 
 	private InMemoryViewResults() {
 		db = null;
 		docName = null;
 		functionName = null;
 		id = null;
 		lock = new ReentrantLock(true);
 	}
 
 	public InMemoryViewResults(String db, String docName, String functionName, View view)
 	{
 		this.db = db;
 		this.docName = docName;
 		this.functionName = functionName;
 		this.view = view;
 		this.map_src = view.getMapSrc();
 		this.reduce_src = view.getReduceSrc();
 		this.currentSequenceNumber = -1;
 		this.id = "InMemoryViewResults ("+db+"/"+docName+"/"+functionName+")";
 		this.isAlive = false;
 		this.lock = new ReentrantLock(true);
 		this.expectedResults = new ConcurrentSkipListMap<Long, MapResultEntry>();
 	}
 
 	/**
 	* Point at which a document is actually passed to the View for processing.
 	* Exposed for ResultsUpdater thread use
 	*/
 	public void doMap(Document doc) {
 		log.debug("doMap {}", doc.getId());
 		if(doc.getId() == null) {
 			log.error("Document with null id field {}", doc.toString());
 			return;
 		}
 		expectedResults.put(new Long(doc.getSequence()), new MapResultEntry(doc));
 		view.map(doc, this, memeDB.getFulltextEngine());
 	}
 
 	///////////////////////////////////////
 	//      ViewResults Interface        //
 	///////////////////////////////////////
 	public void addResult(Document doc) {
 		log.debug("\t{} addResult {} {}", id, doc.getSequence(),currentSequenceNumber);
 		if(doc == null)
 			throw new NullPointerException();
 
 		boolean haveLock = false;
 		try {
 			if(lock.tryLock()) {
 				haveLock = true;
 				doMap(doc);
 			} else {
 				synchronized(this) {
 					if(!updater.isAlive()) {
 						updater = new ResultsUpdater(db, docName, functionName, view, this, doc.getSequence(), lock);
 						updater.start();
 					}
 				}
 			}
 		} finally {
 			if(haveLock)
 				lock.unlock();
 		}
 	}
 
 	public List<JSONObject> all() {
 		ArrayList<JSONObject> rv = new ArrayList<JSONObject>();
 		for(KeyValueEntries kve: veMap.values()) {
 			rv.addAll(kve.getAll());
 		}
 		return rv;
 	}
 
 	public void destroyResults() {
 		log.info("{} destroying...", id);
 		isAlive = false;
 		if(updater != null)
 			updater.interrupt();
		lock.lock(); // never released. Deadlock, sure.
 		synchronized(this) {
 			try {
 				recursivelyDeleteFiles(baseDir);
 			} catch(Exception e) {
 				log.error("{} Error removing view ", id, e);
 			}
 		}
 		log.info("{} destroying complete.", id);
 	}
 
 	public void clear() {
 		if(updater != null)
 			updater.interrupt();
 		lock.lock();
 		try {
 			synchronized(this) {
 				idMap.clear();
 				for(KeyValueEntries kve: veMap.values()) {
 					kve.clear();
 				}
 				currentSequenceNumber = -1;
 			}
 		} finally {
 			lock.unlock();
 		}
 	}
 
 	public void clearFromUpdater() {
 		idMap.clear();
 		for(KeyValueEntries kve: veMap.values()) {
 			kve.clear();
 		}
 	}
 
 	public long getCurrentSequenceNumber() {
 		return currentSequenceNumber;
 	}
 
 	/*
 	* Returns the arbitrary object attached to the ViewResults instance
 	* @see setManagerObject
 	**/
 	public Object getManagerObject() {
 		return mgrObject;
 	}
 
 	/*
 	* Returns a full path to locate any existing instance of InMemoryViewResults
 	*/
 	public File getInstanceFile()
 	{
 		File f = new File(
 			getInstancePath(memeDB, db, docName, functionName),
 			VIEW_RESULTS_NAME);
 		return f;
 	}
 
 	public void init(MemeDB memedb) {
 		log = Logger.get(InMemoryViewResults.class);
 		log.debug("InMemoryViewResults.init");
 		this.memeDB = memedb;
 		if(memedb == null)
 			throw new RuntimeException("memedb null instance");
 		baseDir = getInstancePath(memedb, db, docName, functionName);
 		if (!baseDir.exists()) {
 			baseDir.mkdirs();
 		}
 		shutdown = false;
 		destroy = false;
 		for(Object key: veMap.keySet()) {
 			KeyValueEntries kve = veMap.get(key);
 			kve.init(view);
 		}
 		if(currentSequenceNumber == -1) {
 			Long v = memeDB.getBackend().getDatabaseCreationSequenceNumber(db);
 			if(v != null) {
 				currentSequenceNumber = v.longValue();
 				if(currentSequenceNumber < 0)
 					currentSequenceNumber = -1;
 			}
 		}
 		log.info("{} Initialized at seq {} {}", id, currentSequenceNumber, view.getMapSrc());
 	}
 
 	public View getView() {
 		return view;
 	}
 
 	public boolean isAlive() {
 		return isAlive;
 	}
 
 	/**
 	 * Creates a map of result arrays from the provided view options. The
 	 * only keys processed by this method are startkey, startkey_inclusive,
 	 * endkey and endkey_inclusive
 	 * @param options View options like startkey, endkey etc.
 	 * @return
 	 */
 	protected ConcurrentNavigableMap<Object, KeyValueEntries>
 			makeKeySet(Map<String,String> options)
 	{
 		Object key = makeKeyMapKey("key", options);
 		Object startkey = makeKeyMapKey("startkey", options);
 		Object endkey = makeKeyMapKey("endkey", options);
 		boolean startInclusive = "false".equals(options.get("startkey_inclusive")) ? false : true;
 		boolean endInclusive = "false".equals(options.get("endkey_inclusive")) ? false : true;
 
 		ConcurrentNavigableMap<Object, KeyValueEntries>
 			sm = veMap;
 
 		if(key != null) {
 			startkey = key;
 			endkey = key;
 			startInclusive = true;
 			endInclusive = true;
 			sm = veMap.subMap(startkey, startInclusive, endkey, endInclusive);
 		}
 		else {
 			if(startkey != null) {
 				if(endkey != null) {
 					sm = veMap.subMap(startkey, startInclusive, endkey, endInclusive);
 				} else {
 					sm = veMap.tailMap(startkey, startInclusive);
 				}
 			}
 			else if(endkey != null) {
 				sm = veMap.headMap(endkey, endInclusive);
 			}
 		}
 		return sm;
 	}
 
 	public Object reduce(Map<String,String> options) throws ViewException {
 //		log.debug("reduce {} keySet: {}", options, sm.keySet());
 		if(this.reduce_src == null)
 			throw new ViewException("No reduce function");
 		ConcurrentNavigableMap<Object,KeyValueEntries>
 			sm = makeKeySet(options);
 		Object res = null;
 		JSONArray ja = new JSONArray();
 		synchronized(this) {
 			for(KeyValueEntries kve: sm.values()) {
 				Object rmv = kve.reduced();
 				try {
 					if(rmv != null) {
 						JSONObject o = new JSONObject();
 						o.put("key", JSONObject.NULL);
 						o.put("value", rmv);
 						ja.put(o);
 					}
 				}
 				catch(JSONException e) {
 					log.warn("Error making reduce object {}", e);
 				}
 			}
 		}
 		res = view.reduce(ja);
 		log.debug("FINAL RESULT: {}", res);
 		return res;
 	}
 		
 	protected void removeResult(String id) {
 		ArrayList<JSONObject> ar = idMap.remove(id);
 //		log.debug("removeResult id {} idMap entry:{}", id, ar);
 		if(ar == null)
 			return;
 		int max = ar.size();
 		synchronized(this) {
 			for(int i=0; i<max; i++) {
 				JSONObject o = ar.get(i);
 				if(o == null)
 					continue;
 				Object key = o.opt("key");
 				if(key == null) {
 					log.warn("fetched object had no key field : {}", o.toString());
 					continue;
 				}
 
 				KeyValueEntries kve = veMap.get(key);
 				if(kve == null) {
 					log.warn("veMap had no entry for key {}", key);
 					continue;
 				}
 				kve.remove(o);
 				if(kve.length() == 0) {
 					veMap.remove(key);
 				}
 			}
 		}
 //		log.debug("idMap {}", idMap);
 //		log.debug("veMap {}", veMap);
 	}
 
 	public void removeResult(String id, long seqNo) {
 		boolean haveLock = false;
 		try {
 			if(lock.tryLock()) {
 				haveLock = true;
 				removeResult(id);
 			} else {
 				synchronized(this) {
 					if(!updater.isAlive()) {
 						updater = new ResultsUpdater(db, docName, functionName, view, this, seqNo, lock);
 						updater.start();
 					}
 				}
 			}
 		} catch(Exception e) {
 			if(!e.getMessage().equals("no_instance"))
 				log.error("{} error removing result: {}",id,e);
 		} finally {
 			if(haveLock)
 				lock.unlock();
 		}
 	}
 
 	/*
 	* sets the arbitrary object attached to the ViewResults instance
 	* @see setManagerObject
 	**/
 	public void setManagerObject(Object o) {
 		mgrObject = o;
 	}
 
 	/*
 	* Validate that the source has not changed from the provided view
 	**/
 	public void setView(View view) throws ViewException {
 		if(view == null)
 			throw new RuntimeException("view should not be null");
 		if(view.getMapSrc() == null)
 			throw new RuntimeException("view.getMapSrc() should not be null");
 
 		if(map_src == null || !map_src.equals(view.getMapSrc()))
 			throw new ViewException("Map source changed.");
 
 		String rs = view.getReduceSrc();
 		if((reduce_src == null && rs != null) ||
 			rs == null || !reduce_src.equals(rs)) {
 			reduce_src = rs;
 		}
 	}
 
 	public void start() {
 		synchronized(this) {
 			isAlive = true;
 /*
 			log.debug("{}", db);
 			log.debug("{}", docName);
 			log.debug("{}", functionName);
 			log.debug("view null {}", view == null);
 			log.debug("memedb null {}", memeDB == null);
 			log.debug("state null {}", memeDB.getState() == null);
 			log.debug("{}", memeDB.getState().getCurrentSequenceNumber());
 			log.debug("{}", lock == null);
 */
 			updater = new ResultsUpdater(
 				db,
 				docName,
 				functionName,
 				view,
 				this,
 				memeDB.getState().getCurrentSequenceNumber(),
 				lock);
 			updater.start();
 		}
 	}
 
 	public void shutdown() {
 		isAlive = false;
 		synchronized(this) {
 			isAlive = false;
 			try {
 				save();
 			} catch(ViewException e) {
 				log.error("{} Error saving {} : {}", id, baseDir.toString(), e.getMessage());
 			}
 		}
 	}
 
 	/**
 	* In order of how they are applied
 	* <ul>
 	* <li>key - single key filter. If specified, no startkey or endkey matters
 	* <li>startkey - starting key
 	* <li>startkey_inclusive - starting key included in results (default true)
 	* <li>endkey - ending key
 	* <li>endkey_inclusive - ending key included in result (default true)
 	* <li>descending - descending results (default false)
 	* <li>skip - number of results to ignore
 	* <li>count - maximum number of rows to return
 	* <li>
 	*/
 	public ArrayList<JSONObject> subList(Map<String,String> options) {
 		Long skip = null;
 		try {
 			skip = new Long(options.get("skip"));
 		} catch ( Exception e ) {}
 		Long count = null;
 		try {
 			count = new Long(options.get("count"));
 		} catch ( Exception e ) {}
 
 
 		ArrayListExp<JSONObject> rv = new ArrayListExp<JSONObject>();
 		if(count != null && count.longValue() == 0)
 			return rv;
 		ConcurrentNavigableMap<Object,KeyValueEntries>
 			sm = makeKeySet(options);
 
 		boolean descending = "true".equals(options.get("descending"));
 		if(descending)
 			sm = sm.descendingMap();
 
 		for(KeyValueEntries kve: sm.values()) {
 			ArrayList<JSONObject> ar = kve.getAll();
 			if(descending) {
 				for(int i=ar.size() - 1; i >= 0; i--) {
 					JSONObject j = ar.get(i);
 					if(j == null)
 						continue;
 					try {
 						rv.add(j);
 					} catch(JSONException e) {}
 				}
 			}
 			else {
 				for(int i=0; i < ar.size(); i++) {
 					JSONObject j = ar.get(i);
 					if(j == null)
 						continue;
 					try {
 						rv.add(j);
 					} catch(JSONException e) {}
 				}
 			}
 		}
 
 		if(skip != null) {
 			int start = skip.intValue();
 			rv.rangeRemove(0, start);
 		}
 
 		if(count != null) {
 			int end = count.intValue();
 			try {
 				rv.rangeRemove(end, rv.size());
 			} catch( Exception e ) {}
 		}
 		return rv;
 	}
 
 	public int writeRows(Writer writer, Map<String,String> options) {
 		int skip = 0;
 		try {
 			skip = new Integer(options.get("skip")).intValue();
 		} catch ( Exception e ) {}
 		Integer count = null;
 		try {
 			count = new Integer(options.get("count"));
 		} catch ( Exception e ) {}
 
 		ArrayListExp<JSONObject> rv = new ArrayListExp<JSONObject>();
 		if(count != null && count.longValue() == 0)
 			return 0;
 		ConcurrentNavigableMap<Object,KeyValueEntries>
 			sm = makeKeySet(options);
 
 		boolean descending = "true".equals(options.get("descending"));
 		if(descending)
 			sm = sm.descendingMap();
 
 		int sent = 0;
 		int start = 0;
 		int end = Integer.MAX_VALUE;
 		start += skip;
 		if(count != null)
 			end = start + count.intValue();
 		int pos = 0;
 
 		if(start < 0)
 			start = 0;
 		if(start >= end)
 			return 0;
 		for(KeyValueEntries kve: sm.values()) {
 			ArrayList<JSONObject> ar = kve.getAll();
 			if(descending) {
 				for(int i=ar.size() - 1; i >= 0; i--) {
 					if(pos >= end)
 						break;
 					if(pos++ < start)
 						continue;
 					JSONObject j = ar.get(i);
 					if(j == null)
 						continue;
 					try {
 						if(sent++ != 0)
 							writer.write(",");
 						j.write(writer);
 					} catch(IOException e) {
 						return sent;
 					}
 				}
 			}
 			else {
 				for(int i=0; i < ar.size(); i++) {
 					if(pos >= end)
 						break;
 					if(pos++ < start)
 						continue;
 					JSONObject j = ar.get(i);
 					if(j == null)
 						continue;
 					try {
 						if(sent++ != 0)
 							writer.write(",");
 						j.write(writer);
 					} catch(IOException e) {
 						return sent;
 					}
 				}
 			}
 			if(pos >= end)
 				break;
 		}
 		return sent;
 	}
 
 	///////////////////////////////////////
 	//  MapResultConsumer Interface      //
 	///////////////////////////////////////
 	public void onMapResult(Document doc, JSONArray ja) {
 //		log.debug("onMapResult docId:{} docSeq:{} expecting:{} json:{}", doc.getId(), doc.getSequence(), expectedResults.keySet(), ja);
 		MapResultEntry mre = expectedResults.get(new Long(doc.getSequence()));
 		if(mre == null)
 			return;
 		if(ja == null)
 			ja = new JSONArray();
 		mre.setResults(ja);
 		processMapResults();
 	}
 
 	private synchronized void processMapResults() {
 		for(MapResultEntry mre: expectedResults.values()) {
 			if(!mre.hasResult) {
 				log.debug("mre {} has no result", mre.seqNo);
 				// todo timeout value of 5 seconds, tunable?
 				if(new java.util.Date().getTime() > mre.timestamp + 5000) {
 					doMap(mre.doc);
 				}
 				break;
 			}
 
 			Document doc = mre.doc;
 			String docId = doc.getId();
 			this.removeResult(docId);
 
 			JSONArray ja = mre.getResults();
 			JSONObject o = null;
 			ArrayList<JSONObject> newIdMapList = new ArrayList<JSONObject>();
 			ArrayList<JSONObject> idList = idMap.putIfAbsent(docId, newIdMapList);
 			if(idList == null)
 				idList = newIdMapList;
 
 			if(ja.length() == 0)
 				log.debug("\tNo result for view {} {}", id, view.getMapSrc());
 			else
 				log.debug("\tResult for view {} {}", id, view.getMapSrc());
 			for(int i=0; i<ja.length(); i++) {
 				try {
 					// results from mapper are
 					// [[key,value],[key,value],[key,value]...]
 					JSONArray kva = ja.getJSONArray(i);
 					Object key = null;
 					try {
 						key = kva.get(0);
 						o = new JSONObject();
 						o.put("key", key);
 						o.put("value", kva.get(1));
 					} catch( JSONException e ) {
 						log.debug("JSON error {}", e);
 						continue;
 					}
 					idList.add(o);
 
 					KeyValueEntries newKVE =
 						new KeyValueEntries(key, view);
 					KeyValueEntries set =
 						veMap.putIfAbsent(key, newKVE);
 					if(set == null) {
 						set = newKVE;
 					}
 					set.add(o);				
 					if(doc.getSequence() > currentSequenceNumber)
 						currentSequenceNumber = doc.getSequence();
 				}
 				catch(JSONException e) {
 					log.warn("{} JSONException {} adding {}", id, e.getMessage(),ja.toString());
 				}
 			}
 			expectedResults.remove(new Long(mre.seqNo));
 		}
 //		log.debug("VIEWRESULTS idMap {}", idMap);
 //		log.debug("VIEWRESULTS veMap {}", veMap);
 	}
 
 
 	///////////////////////////////////////
 	//         Protected Methods         //
 	///////////////////////////////////////
 	protected File getInstancePath(MemeDB memedb, String db, String docName, String functionName)
 	{
 		File d = new File(memedb.getProperty(PATH_PROPERTY));
 		if (d==null) {
 			throw new RuntimeException("Could not open InMemoryViewResults path ("+PATH_PROPERTY+")");
 		}
 		d = new File(d, FileUtils.fsEncode(db));
 		d = new File(d, FileUtils.fsEncode(docName));
 		d = new File(d, FileUtils.fsEncode(functionName));
 		return d;
 	}
 
 	protected void recursivelyDeleteFiles(File file) {
 		if (file.isDirectory()) {
 			for (File f:file.listFiles()) {
 				recursivelyDeleteFiles(f);
 			}
 		}
 		file.delete();
 	}
 
 	private final void save() throws ViewException {
 		if (!baseDir.exists()) {
 			baseDir.mkdirs();
 		}
 		ObjectOutputStream oos =null;
 		try {
 			oos = new ObjectOutputStream(new FileOutputStream(new File(baseDir,VIEW_RESULTS_NAME)));
 			oos.writeObject(this);
 			oos.close();
 		} catch (FileNotFoundException e) {
 			throw new ViewException(e);
 		} catch (IOException e) {
 			throw new ViewException(e);
 		} finally {
 			if (oos!=null) {
 				try {
 					oos.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	static private Object makeKeyMapKey(String opt, Map<String,String> options) {
 		if(!options.containsKey(opt))
 			return null;
 		try {
 			String val = options.get(opt);
 			return makeKey(val).get("key");
 		} catch (Exception e) {
 		}
 		return null;
 	}
 
 	/**
 	* Makes a JSONObject key structure from the String provided. The String
 	* must be valid JSON, that is strings wrapped with quotations, arrays
 	* in brackets, ie. ["one", 2, [3, "four"]]
 	*/
 	static public JSONObject makeKey(String value) {
 		return new JSONObject("{ key: " +value+ "}");
 	}
 }
 
