 package warrenfalk.meldfs;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.file.DirectoryStream;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.LinkOption;
 import java.nio.file.OpenOption;
 import java.nio.file.Path;
 import java.nio.file.StandardOpenOption;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import warrenfalk.fuselaj.DirBuffer;
 import warrenfalk.fuselaj.Errno;
 import warrenfalk.fuselaj.FileInfo;
 import warrenfalk.fuselaj.FilesystemException;
 import warrenfalk.fuselaj.FuselajFs;
 import warrenfalk.fuselaj.Stat;
 
 public class MeldFsFuseAdapter extends FuselajFs {
 	MeldFs meldfs;
 	String[] fuseArgs;
 
 	final Path rootPath = nfs.getPath(".").normalize();
 	
 	/** Initialize a MeldFs instance
 	 * @param mountLoc
 	 * @param sources
 	 * @param debug
 	 * @param options
 	 * @throws IOException 
 	 */
 	public MeldFsFuseAdapter(Path mountLoc, boolean debug, String options) throws IOException {
 		super(true);
 		meldfs = new MeldFs();
 		ArrayList<String> arglist = new ArrayList<String>();
 		if (debug)
 			arglist.add("-d");
 		arglist.add(mountLoc.toAbsolutePath().toString());
 		if (options != null && options.length() > 0) {
 			arglist.add("-o");
 			arglist.add(options);
 		}
 		fuseArgs = arglist.toArray(new String[arglist.size()]);
 	}
 	
 	/** Find the parent path of the given path.
 	 * Note: this is distinct from path.getParent() in that it returns the rootPath instead of null
 	 * @param path
 	 * @return the parent of path
 	 */
 	private Path parentOf(Path path) {
 		Path parent = path.getParent();
 		if (parent == null)
 			parent = rootPath;
 		return parent;
 	}
 	
 	/** Starts the fuse main loop
 	 * @return the exit value of the fuse main loop
 	 */
 	int run() {
 		return run(fuseArgs);
 	}
 
 	/** Entry point for the MeldFs process
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		boolean debug = false;
 		try {
 			HashSet<String> fuseOptions = new HashSet<>();
 			fuseOptions.add("big_writes");
 			fuseOptions.add("direct_io");
 			// fuseOptions.add("use_ino"); <- not in the meldfs model because there is no persistently consistent inode 
 			// fuseOptions.add("allow_other"); <- allowed, but should be user-specified
 			
 			Path mountPoint = null;
 			for (int i = 0; i < args.length; i++) {
 				String arg = args[i];
 				if (arg.startsWith("-")) {
 					if (arg.startsWith("-o")) {
 						String optionString = null;
 						if (arg.length() > 2)
 							optionString = arg.substring(2);
 						else if ((i + 1) < args.length)
 							optionString = args[++i];
 						else {
 							throw new RuntimeException("Switch -o requires an argument");
 						}
 						String[] options = optionString.split(",");
 						for (String option : options) {
 							if (option.startsWith("meld_")) {
 								String[] optval = option.split("=", 2);
 								String name = optval[0];
 								String value = (optval.length >= 2) ? optval[1] : "";
 								if ("meld_debug".equals(name)) {
 									debug = !"false".equals(value);
 								}
 								else {
 									throw new RuntimeException("Unknown meld option: " + option);
 								}
 							}
 							else {
 								fuseOptions.add(option);
 							}
 						}
 					}
 					else {
 						throw new RuntimeException("Unknown switch: " + arg);
 					}
 				}
 				else if (mountPoint == null) {
 					mountPoint = FileSystems.getDefault().getPath(arg);
 				}
 				else {
 					throw new RuntimeException("Unexpected argument: " + arg);
 				}
 			}
 			
 			if (mountPoint == null) {
 				throw new RuntimeException("No mount point specified");
 			}
 			
 			MeldFsFuseAdapter mfs = new MeldFsFuseAdapter(mountPoint, debug, join(",", fuseOptions));
 			int exitCode = mfs.run();
 			System.exit(exitCode);
 		}
 		catch (Exception e) {
 			System.err.println(e.getMessage());
 			if (debug)
 				e.printStackTrace();
 		}
 	}
 	
 	private static <T> String join(String separator, Iterable<T> elements) {
 		StringBuilder sb = new StringBuilder();
 		Iterator<T> i = elements.iterator();
 		if (!i.hasNext())
 			return "";
 		sb.append(i.next());
 		while (i.hasNext())
 			sb.append(separator).append(i.next());
 		return sb.toString();
 	}
 
 	@Override
 	protected void getattr(Path path, Stat stat) throws FilesystemException {
 		// attempt to find entry with that name
 		Path file = meldfs.getRealPath(path);
 		if (file == null)
 			throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 		os_lstat(file, stat);
 	}
 	
 	@Override
 	protected void mkdir(Path path, int mode) throws FilesystemException {
 		// find which device contains the parent directory and create there
 		// if more than one device contains the parent, create on the device with the most recently modified
 		Path dir = meldfs.getRealPath(path);
 		if (dir != null)
 			throw new FilesystemException(Errno.FileExists);
 		Path parent = parentOf(path);
 		Path parentDir = meldfs.getRealPath(parent);
 		if (parentDir == null)
 			throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 		dir = parentDir.resolve(path.getFileName());
 		os_mkdir(dir, mode);
 	}
 	
 	@Override
 	protected void rmdir(final Path path) throws FilesystemException {
 		final AtomicInteger found = new AtomicInteger(0);
 		final AtomicInteger deleted = new AtomicInteger(0);
 		meldfs.runMultiSourceOperation(new SourceOp() {
 			@Override
 			public void run(int index, SourceFs source) {
 				Path sourceLoc = source.root.resolve(path);
 				if (Files.exists(sourceLoc)) {
 					found.incrementAndGet();
 					try {
 						os_rmdir(sourceLoc);
 						deleted.incrementAndGet();
 					}
 					catch (FilesystemException fs) {
 						// TODO: figure out what to do here, this should have some sort of transactional capability
 						// We've tried to remove one of the instances of the directory, but at least one failed for some reason
 					}
 				}
 			}
 		});
 		if (found.intValue() == 0)
 			throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 		// TODO: we should probably return the error message from the os_rmdir that failed
 		if (found.intValue() != deleted.intValue())
 			throw new FilesystemException(Errno.IOError);
 	}
 	
 	@Override
 	protected void opendir(Path path, FileInfo fi) throws FilesystemException {
 		Path dir = meldfs.getRealPath(path);
 		if (!Files.isDirectory(dir))
 			throw new FilesystemException(Errno.NotADirectory);
 		FileHandle.open(fi, path);
 	}
 	
 	@Override
 	protected void readdir(Path path, DirBuffer dirBuffer, FileInfo fileInfo) throws FilesystemException {
 		FileHandle fh = FileHandle.get(fileInfo.getFileHandle());
 		String[] names;
 		if (fh.data instanceof Path) {
 			final Path dirpath = (Path)fh.data;
 			final HashSet<String> items = new HashSet<String>();
 			
 			meldfs.runMultiSourceOperation(new SourceOp() {
 				public void run(int index, SourceFs source) {
 					try {
 						Path p = source.root.resolve(dirpath);
 						if (Files.isDirectory(p)) {
 							try (DirectoryStream<Path> stream = Files.newDirectoryStream(p)) {
 								synchronized (items) {
 									items.add(".");
 									items.add("..");
 								}
 								for (Path item : stream) {
 									String itemName = item.getFileName().toString();
 									synchronized (items) {
 										items.add(itemName);
 									}
 								}
 							}
 						}
 					}
 					catch (IOException ioe) {
 						source.onIoException(ioe);
 					}
 				}
 			});
 			
 			names = items.toArray(new String[items.size()]);
 			Arrays.sort(names);
 			fh.data = names;
 		}
 		else {
 			names = (String[])fh.data;
 		}
 		for (long i = dirBuffer.getPosition(); i < names.length; i++) {
 			if (dirBuffer.putDir(names[(int)i], i + 1))
 				return;
 		}
 	}
 	
 	@Override
 	protected void releasedir(Path path, FileInfo fi) throws FilesystemException {
 		FileHandle.release(fi);
 	}
 	
 	@Override
 	protected Path readlink(Path path) throws FilesystemException {
 		Path realPath = meldfs.getRealPath(path);
 		if (realPath == null)
 			throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 		return os_readlink(realPath);
 	}
 	
 	@Override
 	protected void symlink(Path targetOfLink, Path pathOfLink) throws FilesystemException {
 		Path realPath = meldfs.getRealPath(parentOf(pathOfLink));
 		if (realPath == null)
 			throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 		os_symlink(targetOfLink, realPath.resolve(pathOfLink.getFileName()));
 	}
 	
 	@Override
 	protected void link(final Path from, Path to) throws FilesystemException {
 		// get the current "from" file
 		final Path[] files = new Path[meldfs.getSourceCount()];
 		final long[] modTimes = new long[meldfs.getSourceCount()];
 		meldfs.getAllRealPaths(from, files, modTimes);
 		int index = MeldFs.freshest(files, modTimes);
 		if (index == -1)
 			throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 		Path realFrom = files[index];
 		SourceFs fromSource = meldfs.getSource(index);
 		Path realTo = fromSource.root.resolve(to);
 		Path realToParent = parentOf(realTo);
 		// Sorry, can't figure out a way to do that consistently, the directory currently has to already exist on the same source fs
 		if (!Files.isDirectory(realToParent))
 			throw new FilesystemException(Errno.CrossDeviceLink);
 		os_link(realFrom, realTo);
 	}
 	
 	@Override
 	protected void rename(final Path from, final Path to) throws FilesystemException {
 		final Path toParent = parentOf(to);
 		final Path fromParent = parentOf(from);
 		// let's see if this is a simple rename
 		if (fromParent.equals(toParent)) {
 			// TODO: the following operation needs to be have some sort of transactional capability because if one of the operations fail, the rest need to be rolled back
 			meldfs.runMultiSourceOperation(new SourceOp() {
 				public void run(int index, SourceFs source) {
 					Path sourceLoc = source.root.resolve(from);
 					Path targetLoc = source.root.resolve(to);
 					try {
 						if (Files.exists(sourceLoc))
 							os_rename(sourceLoc, targetLoc);
 					}
 					catch (FilesystemException fse) {
 						// TODO: handle this, see TODO above about transactions
 					}
 				}
 			});
 		}
 		else {
 			// when we have to move from directory to directory, it can become complicated
 			// because the target directory may exist somewhere while not existing on all
 			// of the sources that contain the from file.  If this happens, we need to create
 			// the target directories first.  This is complicated by the fact that we need
 			// to copy the permissions and times of the current target directories.
 			
 			// so first we find if the from and target actually exist somewhere
 			final AtomicInteger targetCount = new AtomicInteger(0);
 			final AtomicInteger fromCount = new AtomicInteger(0);
 			final Path[] files = new Path[meldfs.getSourceCount()];
 			meldfs.runMultiSourceOperation(new SourceOp() {
 				public void run(int index, SourceFs source) {
 					Path sourceLoc = source.root.resolve(from);
 					if (Files.exists(sourceLoc)) {
 						fromCount.incrementAndGet();
 						files[index] = sourceLoc;
 					}
 					sourceLoc = source.root.resolve(toParent);
 					if (Files.exists(sourceLoc))
 						targetCount.incrementAndGet();
 				}
 			});
 			if (fromCount.intValue() == 0 || targetCount.intValue() == 0)
 				throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 			
 			// since the from and target exist somewhere, go ahead and rename all froms to the targets
 			// note that we may have to create the target directory structure in some cases
 			// TODO: the following operation needs to be have some sort of transactional capability because if one of the operations fail, the rest need to be rolled back
 			meldfs.runMultiSourceOperation(files, new SourceOp() {
 				public void run(int index, SourceFs source) {
 					Path realFrom = files[index];
 					Path realTo = source.root.resolve(to);
 					Path realTarget = parentOf(realTo);
 					try {
 						if (!Files.exists(realTarget, LinkOption.NOFOLLOW_LINKS)) {
 							// TODO: when creating realTarget directories, copy permissions and times from current versions
 							Files.createDirectories(realTarget);
 						}
 						os_rename(realFrom, realTo);
 					}
 					catch (IOException | FilesystemException ioe) {
 						// TODO: handle this, see transaction note further up
 					}
 				}
 			});
 		}
 	}
 	
 	@Override
 	protected void open(Path path, FileInfo fileInfo) throws FilesystemException {
 		Path realPath = meldfs.getRealPath(path);
 		if (realPath == null)
 			throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 		try {
 			FileChannel channel = FileChannel.open(realPath, getJavaOpenOpts(fileInfo.getOpenFlags()));
 			FileHandle.open(fileInfo, channel);
 		}
 		catch (IOException ioe) {
 			throw new FilesystemException(ioe);
 		}
 	}
 	
 	@Override
 	protected void create(final Path path, int mode, FileInfo fi) throws FilesystemException {
 		// when creating a file, first find the existing file if any
 		// if it exists, and if this is a create_new, then fail, otherwise overwrite that one
 		// if it doesn't exist, get the youngest parent and create one there
 		final Path parent = parentOf(path);
 		final Path[] files = new Path[meldfs.getSourceCount()];
 		final Path[] parents = new Path[meldfs.getSourceCount()];
 		final long[] modTimes = new long[meldfs.getSourceCount()];
 		meldfs.runMultiSourceOperation(new SourceOp() {
 			public void run(int index, SourceFs source) {
 				Path sourceLoc = source.root.resolve(path);
 				if (Files.exists(sourceLoc)) {
 					try {
 						modTimes[index] = Files.getLastModifiedTime(sourceLoc).toMillis();
 						files[index] = sourceLoc;
 					}
 					catch (IOException ioe) {
 						source.onIoException(ioe);
 					}
 				}
 				sourceLoc = source.root.resolve(parent);
 				if (Files.exists(sourceLoc)) {
 					try {
 						modTimes[index] = Files.getLastModifiedTime(sourceLoc).toMillis();
 						parents[index] = sourceLoc;
 					}
 					catch (IOException ioe) {
 						source.onIoException(ioe);
 					}
 				}
 			}
 		});
 
 		// find the youngest existing file (if any), first
 		Path realPath = MeldFs.freshestFile(files, modTimes);
 		if (realPath != null) {
 			if (0 != (fi.getOpenFlags() & FileInfo.O_EXCL))
 				throw new FilesystemException(Errno.FileExists);
 		}
 		else {
 			// no existing file, find the youngest parent
 			Path openDir = MeldFs.freshestFile(parents, modTimes);
 			if (openDir == null)
 				throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 			realPath = openDir.resolve(path.getFileName());
 		}
 		try {
 			FileChannel channel = FileChannel.open(realPath, getJavaOpenOpts(fi.getOpenFlags()));
 			FileHandle.open(fi, channel);
 		}
 		catch (IOException ioe) {
 			throw new FilesystemException(ioe);
 		}
 	}
 
 	/**
 	 * Get a set of Java OpenOption flags which correspond to the FUSE/Linux O_FLAGS bit mask  
 	 * @param openFlags
 	 * @return
 	 */
 	private Set<? extends OpenOption> getJavaOpenOpts(int openFlags) {
 		HashSet<StandardOpenOption> set = new HashSet<StandardOpenOption>();
 		switch (openFlags & FileInfo.O_ACCMODE) {
 		case FileInfo.O_RDONLY:
 			set.add(StandardOpenOption.READ);
 			break;
 		case FileInfo.O_WRONLY:
 			set.add(StandardOpenOption.WRITE);
 			break;
 		case FileInfo.O_RDWR:
 			set.add(StandardOpenOption.READ);
 			set.add(StandardOpenOption.WRITE);
 			break;
 		}
 		if (0 != (openFlags & FileInfo.O_APPEND))
 			set.add(StandardOpenOption.APPEND);
 		if (0 != (openFlags & FileInfo.O_TRUNC))
 			set.add(StandardOpenOption.TRUNCATE_EXISTING);
 		if (0 != (openFlags & FileInfo.O_CREAT))
 			set.add(0 != (openFlags & FileInfo.O_EXCL) ? StandardOpenOption.CREATE_NEW : StandardOpenOption.CREATE);
 		if (0 != (openFlags & FileInfo.O_SYNC))
 			set.add(StandardOpenOption.SYNC);
 		if (0 != (openFlags & FileInfo.O_DSYNC))
 			set.add(StandardOpenOption.DSYNC);
 		return set;
 	}
 	
 	@Override
 	protected void read(Path path, FileInfo fileInfo, ByteBuffer buffer, long position) throws FilesystemException {
 		FileHandle fh = FileHandle.get(fileInfo.getFileHandle());
 		FileChannel channel = (FileChannel)fh.data;
 		try {
 			channel.read(buffer, position);
 		}
 		catch (IOException e) {
 			throw new FilesystemException(e);
 		}
 	}
 	
 	@Override
 	protected void write(Path path, FileInfo fi, ByteBuffer bb, long offset) throws FilesystemException {
 		FileHandle fh = FileHandle.get(fi.getFileHandle());
 		FileChannel channel = (FileChannel)fh.data;
 		try {
 			channel.write(bb, offset);
 		}
 		catch (IOException e) {
 			throw new FilesystemException(e);
 		}
 	}
 	
 	@Override
 	protected void ftruncate(Path path, long size, FileInfo fi) throws FilesystemException {
 		FileHandle fh = FileHandle.get(fi.getFileHandle());
 		FileChannel channel = (FileChannel)fh.data;
 		try {
 			channel.truncate(size);
 		}
 		catch (IOException e) {
 			throw new FilesystemException(e);
 		}
 	}
 
 	@Override
 	protected void fsync(Path path, boolean isdatasync, FileInfo fi) throws FilesystemException {
 		FileHandle fh = FileHandle.get(fi.getFileHandle());
 		FileChannel channel = (FileChannel)fh.data;
 		try {
 			channel.force(!isdatasync);
 		}
 		catch (IOException e) {
 			throw new FilesystemException(e);
 		}
 	}
 	
 	@Override
 	protected void unlink(final Path path) throws FilesystemException {
 		final AtomicInteger deleted = new AtomicInteger(0);
 		final AtomicInteger found = new AtomicInteger(0);
 		meldfs.runMultiSourceOperation(new SourceOp() {
 			@Override
 			public void run(int index, SourceFs source) {
 				Path sourceLoc = source.root.resolve(path);
				if (Files.exists(sourceLoc, LinkOption.NOFOLLOW_LINKS)) {
 					found.incrementAndGet();
 					try {
 						Files.delete(sourceLoc);
 						deleted.incrementAndGet();
 					}
 					catch (IOException e) {
 						source.onIoException(e);
 					}
 				}
 			}
 		});
 		if (found.intValue() == 0)
 			throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 		// TODO: try to throw the actual error that resulted
 		if (deleted.intValue() < found.intValue())
 			throw new FilesystemException(Errno.IOError);
 	}
 	
 	@Override
 	protected void release(Path path, FileInfo fi) throws FilesystemException {
 		FileHandle.release(fi);
 	}
 	
 	@Override
 	protected void truncate(Path path, long size) throws FilesystemException {
 		Path realPath = meldfs.getRealPath(path);
 		if (realPath == null)
 			throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 		os_truncate(realPath, size);
 	}
 	
 	@Override
 	protected void chown(Path path, int uid, int gid) throws FilesystemException {
 		Path realPath = meldfs.getRealPath(path);
 		if (realPath == null)
 			throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 		os_chown(realPath, uid, gid);
 	}
 	
 	@Override
 	protected void chmod(Path path, int mode) throws FilesystemException {
 		Path realPath = meldfs.getRealPath(path);
 		if (realPath == null)
 			throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 		os_chmod(realPath, mode);
 	}
 	
 	@Override
 	protected void utimens(Path path, long accessSeconds, long accessNanoseconds, long modSeconds, long modNanoseconds) throws FilesystemException {
 		Path realPath = meldfs.getRealPath(path);
 		if (realPath == null)
 			throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 		os_utimensat(realPath, accessSeconds, accessNanoseconds, modSeconds, modNanoseconds);
 	}
 	
 }
