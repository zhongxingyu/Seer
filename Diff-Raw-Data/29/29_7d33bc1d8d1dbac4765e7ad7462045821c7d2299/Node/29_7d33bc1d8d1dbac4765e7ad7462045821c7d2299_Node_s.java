 package queryfs;
 
 import java.nio.ByteBuffer;
 
 import java.util.Set;
 
 import com.sun.security.auth.module.UnixSystem;
 
 import fuse.FuseException;
 import fuse.FuseFtype;
 import fuse.FuseGetattrSetter;
 
 public abstract class Node implements View {
 	protected QueryBackend backend;
 	protected final static int UID = (int)new UnixSystem().getUid();
 	protected final static int GID = (int)new UnixSystem().getGid();
 	protected Set<QueryGroup> groups;
 	protected String name = "__undefined__";
 
 	public Node(QueryBackend backend, Set<QueryGroup> groups) {
 		this.backend = backend;
 		this.groups = groups;
 	}
 
	public boolean visible() {
		return true;
	}

 	public int getFType() {
 		return FuseFtype.TYPE_FILE;
 	}
 
 	public Set<QueryGroup> getQueryGroups() {
 		return groups;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public boolean permanent() {
 		return false;
 	}
 
 	public void setPermanent(boolean b) {}
 
 	public abstract void stat(FuseGetattrSetter setter);
 
 	public abstract void setModified(long mtime) throws FuseException;
 
 	public abstract void changeQueryGroups(Set<QueryGroup> add, Set<QueryGroup> remove) throws FuseException;
 
 	public abstract void setName(String name) throws FuseException;
 
 	public abstract void unlink() throws FuseException;
 
 	public abstract void open(int flags) throws FuseException;
 
 	public abstract void close() throws FuseException;
 
 	public abstract void read(ByteBuffer buf, long offset) throws FuseException;
 
 	public abstract void write(ByteBuffer buf, long offset) throws FuseException;
 
 	public abstract void truncate(long size) throws FuseException;
 }
