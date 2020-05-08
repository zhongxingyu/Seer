 package queryfs;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import fuse.FuseException;
 import fuse.FuseFtype;
 import fuse.FuseGetattrSetter;
 
 import queryfs.QueryGroup.Type;
 
 public class RootDirectory implements Directory {
 	protected QueryBackend backend;
 	protected long time = System.currentTimeMillis();
 	protected long stamp = System.nanoTime();
 	protected boolean populated;
 	protected Set<QueryGroup> groups;
 	protected Map<String,View> views = new HashMap<String,View>();
 
 	public RootDirectory(QueryBackend backend) {
 		this.backend = backend;
 		groups = new HashSet<QueryGroup>();
 	}
 
 	protected void register(String key, View value) {
 		if (views.containsKey(key)) {
 			int num = 0;
 			while (views.containsKey(key + "." + num))
 				num++;
 			key += "." + num;
 		}
 		views.put(key, value);
 	}
 
 	public Set<Node> getNodes() {
 		return new HashSet<Node>();
 	}
 
 	public int getFType() {
 		return FuseFtype.TYPE_DIR;
 	}
 
 	public void setModified(long mtime) throws FuseException {
 		time = mtime;
 	}
 
 	public int rename(String from, String to, View v) throws FuseException {
 		throw new FuseException("cannot move /").initErrno(FuseException.EPERM);
 	}
 
 	public void delete() throws FuseException {
 		throw new FuseException("cannot delete /").initErrno(FuseException.EPERM);
 	}
 
 	protected void populateSelf() {
 		views.clear();
 		for (QueryGroup group : backend.subclass(groups)) {
 			if (group.getType() == Type.MIME)
 				register("." + group.getValue(), new SubclassingDirectory(backend, this, group));
			else {
				String value = group.getValue();
				if (value.startsWith("."))
					value = "dot" + value;
				register(value, new SubclassingDirectory(backend, this, group));
			}
 		}
 	}
 
 	public View get(String name) {
 		update();
 		return views.get(name);
 	}
 
 	public Directory getDir(String name) {
 		View v = get(name);
 		if (v != null && v instanceof Directory)
 			return (Directory)v;
 		return null;
 	}
 
 	public void stat(FuseGetattrSetter setter) {
 		update();
 		int mtime = (int)(time / 1000L);
 		setter.set(
 			0, // inode
 			FuseFtype.TYPE_DIR | 0755,
 			1, // nlink
 			UID,
 			GID,
 			0, // rdev
 			0,
 			0,
 			mtime, mtime, mtime // atime, mtime, ctime
 		);
 	}
 
 	public QueryGroup getGroup() {
 		return null;
 	}
 
 	public Set<QueryGroup> getQueryGroups() {
 		return groups;
 	}
 
 	public Directory getParent() {
 		return null;
 	}
 
 	protected void update() {
 		if (!QueryGroup.allValid(stamp))
 			populated = false;
 		if (!populated) {
 			populateSelf();
 			populated = true;
 			time = System.currentTimeMillis();
 			stamp = System.nanoTime();
 		}
 	}
 
 	public synchronized Map<String,View> list() {
 		update();
 		return views;
 	}
 }
