 package Markov2;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import com.db4o.Db4o;
 import com.db4o.ObjectContainer;
 import com.db4o.ObjectSet;
 
 import java.util.TimerTask;
 import java.util.LinkedList;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 /**
  * 
  * @author Andrew
  */
 public class MarkovString extends TimerTask {
 
 	// needed to save the changes to the graph without causing a performance hit
 	private Timer t = new Timer();
 	// the database file
 	private ObjectContainer database;
 	// a list of updated nodes that need saving
 	private LinkedList<MarkovNode> updated = new LinkedList<MarkovNode>();
 	private LinkedList<MarkovNode> saving = null;
 	// a list of cahced nodes for fast lookup
 	private HashMap<String, MarkovNode> cache = new HashMap<String, MarkovNode>();
 
 	// private static final String REGEX =
 	// "[a-z][a-z]*\\.[a-z][a-z]*(\\.[a-z][a-z]*)*|[a-z]+((-|')[a-z]+)*";
 	private static final String REGEX = "((ht|f)tp(s?)\\:\\/\\/|~/|/)?([\\w]+:\\w+@)?([a-zA-Z]{1}([\\w\\-]+\\.)+([\\w]{2,5}))(:[\\d]{1,5})?((/?\\w+/)+|/?)(\\w+\\.[\\w]{3,4})?((\\?\\w+=\\w+)?(&\\w+=\\w+)*)"
 			// urls
 			+ "|" + "([a-z0-9]+([-:|'][a-z0-9]+)*)" // word optionaly seperated
 			// with -:|' i.e. hello
 			// hello-world
 			// hello-cruel-world
			+ "|" + "((:\\))|(:\\()|(\\(:)|(\\):)|(:p))"; // Smilies
 			// :), :(,
 			// ):, (:,
 			// :p
			//+ "|" + "!!!|!|?"; // bob learn him some punctuation
 	private Pattern p = Pattern.compile(REGEX);
 
 	private static final int MAX_SENTANCE_LENGTH = 30;
 
 	private class SaveThread extends Thread {
 		public SaveThread(LinkedList<MarkovNode> list) {
 			super(saveGroup, "Save Thread");
 			this.list = list;
 		}
 
 		private LinkedList<MarkovNode> list;
 
 		@Override
 		public void run() {
 			System.out.println("Saving to database on another thread "
 					+ this.getId());
 			save();
 		}
 	}
 
 	private ThreadGroup saveGroup = new ThreadGroup("Save Threads");
 
 	public MarkovString() {
 		// remove the shutdown hook as we have our own in the main bot
 		Db4o.configure().automaticShutDown(false);
 		// set up indexing
 		Db4o.configure().objectClass(MarkovNode.class).objectField("word")
 				.indexed(true);
 		// set it up to update the lists properly
 		Db4o.configure().objectClass(MarkovNode.class).updateDepth(3);
 		// and activate the lists far enough
 		Db4o.configure().objectClass(MarkovNode.class)
 				.minimumActivationDepth(3);
 		// open the database file
 		database = Db4o.openFile("Markov2.db4o");
 		// get a list of all nodes
 		ObjectSet<MarkovNode> set = database.get(MarkovNode.class);
 		// if we dont have any, we have an empty database and need to start
 		// learning
 		if (set.size() == 0) {
 			database.set(new MarkovNode("["));
 			database.set(new MarkovNode("]"));
 		} else {
 			for (MarkovNode n : set)
 				cache.put(n.getWord(), n);
 		}
 		// schedule the saves for 1 minute intervals
 		t.schedule(this, 0, 60000);
 	}
 
 	@Deprecated
 	public int getWordCount() {
 		return database.get(MarkovNode.class).size();
 	}
 
 	@Deprecated
 	public int getConnectionCount() {
 		int ret = 0;
 		ObjectSet<MarkovNode> set = database.get(MarkovNode.class);
 		for (MarkovNode n : set)
 			ret += n.getConnectionCount();
 		return ret;
 	}
 
 	public synchronized int[] getStats() {
 		int ret[] = new int[2];
 
 		// ObjectSet<MarkovNode> set = database.get(MarkovNode.class);
 		// ret[0] = set.size();
 		// for (MarkovNode n : set)
 		// ret[1] += n.getConnectionCount();
 
 		ret[0] = cache.size();
 		for (MarkovNode n : cache.values())
 			ret[1] += n.getConnectionCount();
 		return ret;
 	}
 
 	public String Generate() {
 		StringBuffer sb = new StringBuffer();
 		// get the beginning node
 		MarkovNode current = getNode("[");
 		// loop through until we hit the end
 		for (int i = 0; i < MAX_SENTANCE_LENGTH
 				&& !current.getWord().equals("]"); i++) {
 			// get a random next node
 			MarkovNode newNode = current.GetRandomNode();
 			// if its null we need to add a new join to the end
 			if (newNode == null) {
 				MarkovNode end = getNode("]");
 				current.AddChild(end);
 				if (!updated.contains(current))
 					updated.add(current);
 				newNode = end;
 			}
 			current = newNode;
 
 			// append the word at the new nodes
 			sb.append(current.getWord());
 			// append a space
 			sb.append(" ");
 		}
 		// return the whole string
 		return sb.toString().replace("]", " ").trim();
 	}
 
 	private ArrayList<String> split(String sentence) {
 		ArrayList<String> strings = new ArrayList<String>();
 		java.util.regex.Matcher m = p.matcher(sentence);
 		String f = null;
 		while (m.find()) {
 			strings.add(m.group());
 		}
 		return strings;
 	}
 
 	public void Learn(String sentence) {
 		synchronized (updated) {
 			MarkovNode n, parent;
 			parent = getNode("[");
 			ArrayList<String> words = split(sentence.toLowerCase());
 			for (String word : words) {
 				// if the word is blank, ignore it
 				if (word.trim().equals(""))
 					continue;
 				// get the word from the database if we already have it
 
 				MarkovNode query = getNode(word);
 				if (query == null) {
 					// if we dont have it, add it
 					n = new MarkovNode(word);
 					updated.add(n);
 					cache.put(word, n);
 				} else {
 					n = query;
 				}
 
 				// add to the parent node
 				parent.AddChild(n);
 				if (!updated.contains(parent))
 					updated.add(parent);
 
 				// move to the next node
 				parent = n;
 			}
 
 			// not sure why this'd ever be null ...
 			if (parent != null) {
 				// add the end marker at the end
 				parent.AddChild(getNode("]"));
 				if (!updated.contains(parent))
 					updated.add(parent);
 			}
 		}
 	}
 
 	private MarkovNode getNode(String word) {
 		if (cache.containsKey(word)) {
 			return cache.get(word);
 		} else {
 			ObjectSet<MarkovNode> query = database.get(new MarkovNode(word,
 					true));
 			if (query.size() == 0)
 				return null;
 			else {
 				MarkovNode n = query.get(0);
 				cache.put(n.getWord(), n);
 				return n;
 			}
 		}
 	}
 
 	public void save() {
 		synchronized (updated) {
 			saving = updated;
 			updated = new LinkedList<MarkovNode>();
 		}
 		save(updated);
 	}
 
 	public void save(LinkedList<MarkovNode> listToSave) {
 		for (MarkovNode n : listToSave) {
 			database.set(n);
 		}
 		database.commit();
 	}
 
 	public void run() {
 		System.out.println("Active save threads:" + saveGroup.activeCount());
 		if (database != null && updated.size() > 0
 				&& saveGroup.activeCount() == 0) {
 			SaveThread savethread = new SaveThread(
 					(LinkedList<MarkovNode>) updated.clone());
 			// updated.clear();
 			savethread.start();
 		}
 	}
 
	public void cleanup()  {
 		t.cancel();
 		while (saveGroup.activeCount() > 0) {
			try {Thread.sleep(1000);}
			catch (InterruptedException ex) {}
 		}
 		save();
 		database.close();
 	}
 }
