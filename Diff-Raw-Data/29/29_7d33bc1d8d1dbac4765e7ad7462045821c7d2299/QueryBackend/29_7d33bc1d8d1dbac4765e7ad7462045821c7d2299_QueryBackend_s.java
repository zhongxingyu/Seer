 package queryfs;
 
 import java.io.File;
 import java.io.IOException;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import fuse.FuseException;
 
 import queryfs.QueryGroup.Type;
 
 import static queryfs.Util.*;
 
 class QueryBackend {
 	public final File root;
 	private Set<Node> nodes = new HashSet<Node>();
 	private Set<QueryGroup> flagged = new HashSet<QueryGroup>();
 	private QueryCache cache = new QueryCache();
 	private boolean rebuild_root = false;
 
 	public QueryBackend(File origin) {
 		assert origin.isDirectory();
 		root = origin;
 		scan(root, true);
 	}
 
 	private class QueryCache {
 		// keyindex is shared by nodecache and groupcache
 		private Map<QueryGroup,Set<Set<QueryGroup>>> keyIndex = new HashMap<QueryGroup,Set<Set<QueryGroup>>>();
 		private Map<Set<QueryGroup>,Set<Node>> nodeCache = new HashMap<Set<QueryGroup>,Set<Node>>();
 		private Map<Set<QueryGroup>,Set<QueryGroup>> groupCache = new HashMap<Set<QueryGroup>,Set<QueryGroup>>();
 
 		public Set<Node> getNodes(Set<QueryGroup> groups) {
 			if (groups.isEmpty())
 				return nodes;
 			return nodeCache.get(groups);
 		}
 
 		public Set<QueryGroup> getGroups(Set<QueryGroup> groups) {
 			if (groups.isEmpty() && rebuild_root) {
 				groupCache.remove(groups);
 				rebuild_root = false;
 			}
 			return groupCache.get(groups);
 		}
 
 		public void putNodes(Set<QueryGroup> key, Set<Node> value) {
 			if (key.isEmpty()) // handled specially
 				return;
 			assert maxOneMimeGroup(key);
 			nodeCache.put(key, value);
 			for (QueryGroup group : key) {
 				Set<Set<QueryGroup>> related = keyIndex.get(group);
 				if (related == null) {
 					related = new HashSet<Set<QueryGroup>>();
 					keyIndex.put(group, related);
 				}
 				related.add(key);
 			}
 		}
 
 		public void putGroups(Set<QueryGroup> key, Set<QueryGroup> value) {
 			assert maxOneMimeGroup(key);
 			groupCache.put(key, value);
 			for (QueryGroup group : key) {
 				Set<Set<QueryGroup>> related = keyIndex.get(group);
 				if (related == null) {
 					related = new HashSet<Set<QueryGroup>>();
 					keyIndex.put(group, related);
 				}
 				related.add(key);
 			}
 		}
 
 		public void drop(Set<QueryGroup> groups) {
 			for (QueryGroup group : groups) {
 				Set<Set<QueryGroup>> related = keyIndex.get(group);
 				if (related != null) {
 					for (Set<QueryGroup> key : related) {
 						nodeCache.remove(key);
 						groupCache.remove(key);
 					}
 				}
 				keyIndex.remove(group);
 			}
 		}
 	}
 
 	private Set<Set<QueryGroup>> broader(Set<QueryGroup> input) {
 		Set<Set<QueryGroup>> output = new HashSet<Set<QueryGroup>>();
 		for (QueryGroup q : input) {
 			Set<QueryGroup> t = new HashSet<QueryGroup>(input);
 			t.remove(q);
 			output.add(t);
 		}
 		return output;
 	}
 
 	private boolean maxOneMimeGroup(Set<QueryGroup> groups) {
 		int count = 0;
 		for (QueryGroup q : groups)
 			if (q.getType() == Type.MIME)
 				count++;
 		return count <= 1;
 	}
 
 	public Set<Node> query(Set<QueryGroup> groups) {
 		if (groups.isEmpty())
 			return nodes;
 		assert maxOneMimeGroup(groups);
 		Set<Node> output = cache.getNodes(groups);
 		if (output == null) {
 			output = new HashSet<Node>();
 			// looking for a cached query 1 level up
 			// (optimized for ever narrowing queries)
 			Set<QueryGroup> selection = null;
 			for (Set<QueryGroup> sel : broader(groups)) {
 				selection = sel;
 				if (cache.getNodes(sel) != null)
 					break; // yes!
 			}
 			Set<Node> pool = query(selection);
 			for (Node node : pool)
 				if (node.getQueryGroups().containsAll(groups))
 					output.add(node);
 			cache.putNodes(groups, output);
 		}
 		return output;
 	}
 
 	public Set<QueryGroup> subclass(Set<QueryGroup> groups) {
 		assert maxOneMimeGroup(groups);
 		for (QueryGroup q : groups)
 			if (q.getType() == Type.MIME)
 				return new HashSet<QueryGroup>();
 		Set<QueryGroup> output = cache.getGroups(groups);
 		if (output == null) {
 			Set<Node> pool = query(groups);
 			Map<QueryGroup,Integer> gcount = new HashMap<QueryGroup,Integer>();
 			for (Node node : pool) {
 				for (QueryGroup group : node.getQueryGroups()) {
 					Integer n = gcount.get(group);
 					gcount.put(group, n == null ? 1 : n + 1);
 				}
 			}
 			output = new HashSet<QueryGroup>();
 			for (QueryGroup g : gcount.keySet())
 				if (!groups.contains(g) && (groups.isEmpty() || gcount.get(g) < pool.size()))
 					output.add(g);
 			cache.putGroups(groups, output);
 		}
 		return output;
 	}
 
 	public void flag(QueryGroup updated) {
 		flagged.add(updated);
 	}
 
 	public void flush() {
 		cache.drop(flagged);
 		for (QueryGroup q : flagged)
 			q.touch();
 		flagged.clear();
 	}
 
 	private void scan(File dir, boolean root) {
 		for (File child : dir.listFiles()) {
 			if (!child.canRead())
 				continue;
 			if (child.isDirectory()) {
 				if (!child.getName().startsWith("."))
 					scan(child, false);
 			} else if (!root) {
 				nodes.add(fileToNode(child));
 			}
 		}
 	}
 
 	private Node fileToNode(File file) {
 		assert !file.isDirectory();
		String path = file.getAbsolutePath().substring(root.getAbsolutePath().length() + 1);
 		Set<String> tags = new HashSet<String>(
 			Arrays.asList(path.split("/"))
 		);
		tags.remove(file.getName());
 		tags.remove("");
 		Set<QueryGroup> groups = new HashSet<QueryGroup>();
 		for (String tag : tags)
 			groups.add(QueryGroup.create(tag, Type.TAG));
 		groups.add(QueryGroup.create(extensionOf(file), Type.MIME));
 		assert maxOneMimeGroup(groups);
 		return new FileNode(this, file, groups);
 	}
 
 	public void create(Set<QueryGroup> groups, String name) throws FuseException {
 		File file = null;
 		try {
 			file = getDestination(newPath(root, groups), name);
 			file.createNewFile();
 		} catch (IOException e) {
 			throw new FuseException(e.getMessage()).initErrno(FuseException.EIO);
 		}
 		assert maxOneMimeGroup(groups);
 		flagged.addAll(groups);
 		flush();
 		nodes.add(new FileNode(this, file, groups));
 		checkRootAdd(groups);
 	}
 
 	public void checkRoot(Set<QueryGroup> removed) {
 		Set<QueryGroup> p = new HashSet<QueryGroup>();
 		for (QueryGroup q : removed) {
 			p.clear();
 			p.add(q);
 			if (query(p).isEmpty()) {
 				rebuild_root = true;
 				QueryGroup.touchRoot();
 				flush();
 				return;
 			}
 		}
 	}
 
 	public void checkRootAdd(Set<QueryGroup> added) {
 		Set<QueryGroup> p = new HashSet<QueryGroup>();
 		for (QueryGroup q : added) {
 			p.clear();
 			p.add(q);
 			if (query(p).size() == 1) {
 				rebuild_root = true;
 				QueryGroup.touchRoot();
 				flush();
 				return;
 			}
 		}
 	}
 
 	public void unref(Node node) {
 		nodes.remove(node);
 	}
 }
