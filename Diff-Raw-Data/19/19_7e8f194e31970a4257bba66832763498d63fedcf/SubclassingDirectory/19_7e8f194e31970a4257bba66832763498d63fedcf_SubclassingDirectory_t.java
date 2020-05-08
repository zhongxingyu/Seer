 package queryfs;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import fuse.FuseException;
 
 import queryfs.QueryGroup.Type;
 
 import static queryfs.Util.*;
 
 public class SubclassingDirectory extends RootDirectory {
 	private final Directory parent;
 	private final QueryGroup group;
 
 	public SubclassingDirectory(QueryBackend backend, Directory parent, QueryGroup group) {
 		super(backend);
 		this.parent = parent;
 		this.group = group;
 		groups.addAll(parent.getQueryGroups());
 		groups.add(group);
 	}
 
 	public Set<Node> getNodes() {
 		return backend.query(groups);
 	}
 
 	public void delete() throws FuseException {
 		Set<QueryGroup> del = new HashSet<QueryGroup>();
 		del.add(group);
 		for (String ref : views.keySet()) {
 			View v = views.get(ref);
 			if (v instanceof Node) {
 				Node n = (Node)v;
 				n.changeQueryGroups(null, del);
 			}
 		}
 		return;
 	}
 
 	protected void populateSelf() {
 		super.populateSelf();
 		for (Node node : backend.query(groups))
 			if (node.visible())
 				register(node.getName(), node);
 	}
 
	private boolean fromEqualsThis(String from) {
		Set<QueryGroup> remove = new HashSet<QueryGroup>();
		for (String tag : tagsOf(from))
			remove.add(QueryGroup.create(tag, Type.TAG));
		return remove.equals(groups);
	}

 	public int rename(String from, String to, View target) throws FuseException {
		assert fromEqualsThis(from);
 		if (target != null)
 			return fuse.Errno.EPERM;
 		if (group.getType() == Type.MIME)
 			return fuse.Errno.EPERM;
 		Set<QueryGroup> add = new HashSet<QueryGroup>();
 		for (String tag : tagsOf(to))
 			add.add(QueryGroup.create(tag, Type.TAG));
 		for (Node n : getNodes())
 			n.changeQueryGroups(add, groups);
 		return 0;
 	}
 
 	public QueryGroup getGroup() {
 		return group;
 	}
 
 	public Directory getParent() {
 		return parent;
 	}
 
 	protected void update() {
 		if (!group.stampValid(stamp))
 			populated = false;
 		super.update();
 	}
 
 	public synchronized Map<String,View> list() {
 		update();
 		return views;
 	}
 }
