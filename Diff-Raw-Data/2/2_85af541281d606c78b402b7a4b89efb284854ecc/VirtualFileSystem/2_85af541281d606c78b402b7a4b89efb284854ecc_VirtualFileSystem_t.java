 package nl.rug.peerbox.logic.filesystem;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.FileSystem;
 import java.nio.file.FileSystems;
 import java.nio.file.Path;
 import java.nio.file.StandardWatchEventKinds;
 import java.nio.file.WatchEvent;
 import java.nio.file.WatchKey;
 import java.nio.file.WatchService;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import nl.rug.peerbox.logic.Context;
 import nl.rug.peerbox.logic.PeerHost;
 import nl.rug.peerbox.logic.PeerListener;
 import nl.rug.peerbox.logic.messaging.Message;
 import nl.rug.peerbox.logic.messaging.Message.Command;
 import nl.rug.peerbox.logic.messaging.Message.Key;
 
 import org.apache.log4j.Logger;
 
 public class VirtualFileSystem implements PeerListener {
 
 	private Filelist filelist;
 	private final List<VFSListener> listeners = new ArrayList<VFSListener>();
 	private final Context ctx;
 	private static final Logger logger = Logger
 			.getLogger(VirtualFileSystem.class);
 
 	private VirtualFileSystem(final Context ctx) {
 		this.ctx = ctx;
 		ctx.addPeerListener(this);
 
 	}
 
 	public static VirtualFileSystem initVirtualFileSystem(Context ctx) {
 		File folder = new File(ctx.getPathToPeerbox());
 		if (!folder.exists()) {
 			folder.mkdirs();
 		}
 
 		VirtualFileSystem vfs = new VirtualFileSystem(ctx);
 
 		vfs.filelist = new Filelist();
 
 		String path = ctx.getPathToPeerbox();
 		File directory = new File(path);
 		String datafile = ctx.getDatafileName();
 
 		File f = new File(directory.getAbsolutePath()
 				+ System.getProperty("file.separator") + datafile);
 		if (f.exists()) {
 			vfs.filelist = Filelist.deserialize(datafile, path);
 		}
 
 		if (directory.isDirectory()) {
 			for (File file : directory.listFiles()) {
 				if (file.isFile() && !file.isHidden()) {
 					String filename = file.getName();
 					PeerboxFile pbf = new PeerboxFile(filename,
 							ctx.getLocalPeer(), file);
 					if (!filename.equals(datafile)) {
 						vfs.addFile(pbf);
 					}
 				}
 			}
 		}
 		vfs.filelist.serialize(datafile, path);
 
 		Thread peerboxObserver = new Thread(new PeerboxPathWatcher(ctx, vfs));
 		peerboxObserver.setDaemon(true);
 		peerboxObserver.start();
 
 		return vfs;
 	}
 
 	public void addFile(PeerboxFile file) {
 		if (!filelist.containsKey(file.getUFID())) {
 			for (PeerboxFile pbf : filelist.values()) {
 				if (! file.getOwner().equals(ctx.getLocalPeer())) {
 					if (file.getFilename().equals(pbf.getFilename())) {
 						if (file.getChecksum().equals(pbf.getChecksum())) {
 							//same file
 							return;
 						} else {
 							file.setFilename(file.getOwner().getName()+"_"+file.getFilename());
 							//different files
 						}
 					}
 				}
 			}
 			filelist.put(file.getUFID(), file);
 			notifyAboutAddedFile(file);
 		}
 	}
 
 	public PeerboxFile removeFile(UFID ufid) {
 		PeerboxFile f = filelist.remove(ufid);
 		logger.info("Remove " + ufid + " from VFS");
 		if (f != null) {
 			logger.info("Actually removed");
 			notifyAboutDeletedFile(f);
 		}
 		return f;
 	}
 
 	public void addVFSListener(VFSListener l) {
 		listeners.add(l);
 	}
 
 	public void removeVFSListener(VFSListener l) {
 		listeners.remove(l);
 	}
 
 	public Collection<PeerboxFile> getFileList() {
 		return new ArrayList<PeerboxFile>(filelist.values());
 	}
 
 	private void notifyAboutAddedFile(PeerboxFile f) {
 		for (VFSListener l : listeners) {
 			l.added(f);
 		}
 	}
 
 	private void notifyAboutDeletedFile(PeerboxFile f) {
 		for (VFSListener l : listeners) {
 			l.deleted(f);
 		}
 	}
 
 	@Override
 	public void deleted(PeerHost ph) {
 		for (PeerboxFile f : filelist.values()) {
 			if (f.getOwner().equals(ph.getPeer())) {
 				removeFile(f.getUFID());
 			}
 		}
 	}
 
 	@Override
 	public void joined(PeerHost peerHost) {
 		refresh();
 	}
 
 	@Override
 	public void updated(PeerHost ph) {
 	}
 
 	public void refresh() {
 		Message askForFiles = new Message();
 		askForFiles.put(Key.Command, Command.Refresh);
 		askForFiles.put(Key.Peer, ctx.getLocalPeer());
 		ctx.getMulticastGroup().announce(askForFiles.serialize());
 
 	}
 
 	private static final class PeerboxPathWatcher implements Runnable {
 		private final Context ctx;
 		private final VirtualFileSystem vfs;
 
 		public PeerboxPathWatcher(Context ctx, VirtualFileSystem vfs) {
 			this.ctx = ctx;
 			this.vfs = vfs;
 		}
 
 		@Override
 		public void run() {
 			FileSystem fs = FileSystems.getDefault();
 			Path peerboxPath = fs.getPath(ctx.getPathToPeerbox());
 			WatchService watcher;
 			try {
 				watcher = fs.newWatchService();
 				peerboxPath.register(watcher,
 						StandardWatchEventKinds.ENTRY_CREATE,
 						StandardWatchEventKinds.ENTRY_DELETE,
 						StandardWatchEventKinds.ENTRY_MODIFY);
 			} catch (IOException e) {
 				return;
 			}
 			try {
 				while (true) {
 					WatchKey watckKey = watcher.take();
 					List<WatchEvent<?>> events = watckKey.pollEvents();
 					for (WatchEvent<?> event : events) {
 						if (!(event.context() instanceof Path)) {
 							continue;
 						}
 
 						Path path = (Path) event.context();
 
 						String filename = path.toString();
 						File file = new File(ctx.getPathToPeerbox(), filename);
 						if (file.isDirectory() || file.isHidden()) {
 							continue;
 						}
 
 						if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
 							if (file.isFile()) {
 								logger.info("Detected file created event "
 										+ path.toString());
 								PeerboxFile pbf = new PeerboxFile(
 										file.getName(), ctx.getLocalPeer(),
 										file);
 								boolean remoteFile = false;
 								synchronized (VirtualFileSystem.class) {
 									for (PeerboxFile f : vfs.filelist.values()) {
 										if (file.equals(f.getFile())) {
 											remoteFile = true;
 											break;
 										}
 									}
 								}
 								if (!remoteFile) {
 									vfs.addFile(pbf);
 									Message update = new Message();
 									update.put(Key.Command, Command.Created);
 									update.put(Key.Peer, ctx.getLocalPeer());
									update.put(Key.File, pbf);
 									ctx.getMulticastGroup().announce(
 											update.serialize());
 								}
 							}
 
 						}
 						if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
 							logger.info("Detect file deleted event "
 									+ path.toString());
 							PeerboxFile pbf = new PeerboxFile(filename,
 									ctx.getLocalPeer());
 							boolean remoteFile = false;
 							for (PeerboxFile f : vfs.filelist.values()) {
 								if (file.equals(f.getFile())) {
 									f.setFile(null);
 									remoteFile = true;
 									break;
 								}
 							}
 							if (!remoteFile
 									&& vfs.removeFile(pbf.getUFID()) != null) {
 
 								Message update = new Message();
 								update.put(Key.Command, Command.Deleted);
 								update.put(Key.Peer, ctx.getLocalPeer());
 								update.put(Key.FileId, pbf.getUFID());
 								ctx.getMulticastGroup().announce(
 										update.serialize());
 							}
 						}
 					}
 					watckKey.reset();
 				}
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 }
