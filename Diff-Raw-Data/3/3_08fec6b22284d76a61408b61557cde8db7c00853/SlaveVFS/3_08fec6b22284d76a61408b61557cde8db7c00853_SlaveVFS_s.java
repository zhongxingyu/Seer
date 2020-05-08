 package edu.berkeley.grippus.fs;
 
 
 import edu.berkeley.grippus.Errno;
 import edu.berkeley.grippus.server.NodeMasterRPC;
 import edu.berkeley.grippus.util.Periodic;
 
 public class SlaveVFS extends VFS {
 	private final NodeMasterRPC master;
	DFile root = new VirtualDDirectory("%TEMPROOT%", new EveryonePermissions());
 	private final Periodic updater = new Periodic(500, "VFS update thread") {
 		@Override protected void fire() {
 			root = master.downloadMetadata();
 		}
 	};
 	public SlaveVFS(NodeMasterRPC master) {
 		this.master = master;
 	}
 	@Override
 	public Errno mkdir(DFileSpec dir, Permission perm) {
 		return master.mkdir(dir, perm);
 	}
 	@Override
 	public Errno mount(DFileSpec where, String realPath, Permission perm) {
 		return Errno.ERROR_NOT_SUPPORTED; // TODO implement
 	}
 	@Override
 	protected DFile getRoot() {
 		return root;
 	}
 }
