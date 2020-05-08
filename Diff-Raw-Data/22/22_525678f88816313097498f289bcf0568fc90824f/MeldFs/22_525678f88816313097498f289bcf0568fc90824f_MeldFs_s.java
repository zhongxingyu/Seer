 package warrenfalk.meldfs;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.file.DirectoryStream;
 import java.nio.file.FileSystem;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.OpenOption;
 import java.nio.file.Path;
 import java.nio.file.StandardOpenOption;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
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
 
 public class MeldFs extends FuselajFs {
 	SourceFs[] sources;
 	String[] fuseArgs;
 	ExecutorService threadPool;
 	
 	final Path rootPath = nfs.getPath(".").normalize();
 	
 	public MeldFs(Path mountLoc, Path[] sources, boolean debug, String options) {
 		super(true);
 		this.sources = SourceFs.fromPaths(sources);
 		ArrayList<String> arglist = new ArrayList<String>();
 		if (debug)
 			arglist.add("-d");
 		arglist.add(mountLoc.toAbsolutePath().toString());
 		if (options != null && options.length() > 0) {
 			arglist.add("-o");
 			arglist.add(options);
 		}
 		fuseArgs = arglist.toArray(new String[arglist.size()]);
 		threadPool = Executors.newCachedThreadPool();
 	}
 	
 	private Path parentOf(Path path) {
 		Path parent = path.getParent();
 		if (parent == null)
 			parent = rootPath;
 		return parent;
 	}
 	
 	int run() {
 		return run(fuseArgs);
 	}
 
 	/** Entry point for the MeldFs process
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		// TODO: Debugging code follows
 		boolean debug = true;
 		FileSystem fs = FileSystems.getDefault();
 		Path mountPoint = fs.getPath("/dev/shm/test");
 		if (!Files.exists(mountPoint))
 			Files.createDirectories(mountPoint);
 		Path[] devices = new Path[] {
 				fs.getPath("/home/warren/test/0"),
 				fs.getPath("/home/warren/test/1"),
 				fs.getPath("/home/warren/test/2"),
 				fs.getPath("/home/warren/test/3"),
 				fs.getPath("/home/warren/test/4"),
 				fs.getPath("/home/warren/test/5"),
 		};
 		//String options = "allow_other,use_ino,big_writes";
 		String options = "allow_other,big_writes";
 		for (Path device : devices) {
 			if (!Files.exists(device))
 				Files.createDirectories(device);
 		}
 		
 		MeldFs mfs = new MeldFs(mountPoint, devices, debug, options);
 		int exitCode = mfs.run();
 		System.exit(exitCode);
 	}
 	
 	@Override
 	protected void getattr(Path path, Stat stat) throws FilesystemException {
 		try {
 			// attempt to find entry with that name
 			Path file = getLatestFile(path);
 			if (file == null)
 				throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 			os_stat(file.toAbsolutePath(), stat);
 		}
 		catch (InterruptedException ie) {
 			throw new FilesystemException(ie);
 		}
 	}
 	
 	@Override
 	protected void mkdir(Path path, int mode) throws FilesystemException {
 		// find which device contains the parent directory and create there
 		// if more than one device contains the parent, create on the device with the most recently modified
 		try {
 			Path dir = getLatestFile(path);
 			if (dir != null)
 				throw new FilesystemException(Errno.FileExists);
 			Path parent = parentOf(path);
 			Path parentDir = getLatestFile(parent);
 			if (parentDir == null)
 				throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 			dir = parentDir.resolve(path.getFileName());
 			os_mkdir(dir, mode);
 		}
 		catch (InterruptedException ie) {
 			throw new FilesystemException(ie);
 		}
 	}
 	
 	@Override
 	protected void rmdir(final Path path) throws FilesystemException {
 		final AtomicInteger found = new AtomicInteger(0);
 		final AtomicInteger deleted = new AtomicInteger(0);
 		runMultiSourceOperation(new SourceOp() {
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
 						// TODO: figure out what to do here.
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
 		try {
 			Path dir = getLatestFile(path);
 			if (!Files.isDirectory(dir))
 				throw new FilesystemException(Errno.NotADirectory);
 			FileHandle.open(fi, path);
 		}
 		catch (InterruptedException e) {
 			throw new FilesystemException(e);
 		}
 	}
 	
 	void runMultiSourceOperation(SourceOp operation) throws FilesystemException {
 		final AtomicInteger sync = new AtomicInteger(sources.length);
		for (int i = 0; i < sources.length; i++)
			threadPool.execute(new SourceOpRunner(operation, i, sources[i], sync));
 		try {
 			synchronized (sync) {
 				sync.wait();
 			}
 		}
 		catch (InterruptedException e) {
 			throw new FilesystemException(e);
 		}
 	}
 	
 	@Override
 	protected void readdir(Path path, DirBuffer dirBuffer, FileInfo fileInfo) throws FilesystemException {
 		FileHandle fh = FileHandle.get(fileInfo.getFileHandle());
 		String[] names;
 		if (fh.data instanceof Path) {
 			final Path dirpath = (Path)fh.data;
 			final HashSet<String> items = new HashSet<String>();
 			
 			runMultiSourceOperation(new SourceOp() {
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
 	protected void open(Path path, FileInfo fileInfo) throws FilesystemException {
 		Path realPath;
 		try {
 			realPath = getLatestFile(path);
 		}
 		catch (InterruptedException e) {
 			throw new FilesystemException(e);
 		}
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
 		final Path[] files = new Path[sources.length];
 		final Path[] parents = new Path[sources.length];
 		final long[] modTimes = new long[sources.length];
 		runMultiSourceOperation(new SourceOp() {
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
 		long max = 0L;
 		Path realPath = null;
 		for (int i = 0; i < sources.length; i++) {
 			Path file = files[i];
 			if (file == null)
 				continue;
 			long mod = modTimes[i];
 			if (mod > max) {
 				realPath = file;
 				max = mod;
 			}
 		}
 		if (realPath != null) {
 			if (0 != (fi.getOpenFlags() & FileInfo.O_EXCL))
 				throw new FilesystemException(Errno.FileExists);
 		}
 		else {
 			// no existing file, find the youngest parent
 			max = 0L;
 			Path openDir = null;
 			for (int i = 0; i < sources.length; i++) {
 				Path dir = parents[i];
 				if (dir == null)
 					continue;
 				long mod = modTimes[i];
 				if (mod > max) {
 					openDir = dir;
 					max = mod;
 				}
 			}
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
 	protected void unlink(final Path path) throws FilesystemException {
 		final AtomicInteger deleted = new AtomicInteger(0);
 		final AtomicInteger found = new AtomicInteger(0);
 		runMultiSourceOperation(new SourceOp() {
 			@Override
 			public void run(int index, SourceFs source) {
 				Path sourceLoc = source.root.resolve(path);
 				if (Files.exists(sourceLoc)) {
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
 	protected void chown(Path path, int uid, int gid) throws FilesystemException {
 		// TODO Implement
 		throw new FilesystemException(Errno.FunctionNotImplemented);
 	}
 	
 	@Override
 	protected void chmod(Path path, int mode) throws FilesystemException {
 		// TODO Implement
 		throw new FilesystemException(Errno.FunctionNotImplemented);
 	}
 	
 	/** Return the real path the the file if on one device, or the path to the most recently
 	 * modified version of the file if on multiple devices
 	 * @param path
 	 * @return
 	 * @throws InterruptedException
 	 * @throws FilesystemException 
 	 */
 	private Path getLatestFile(final Path path) throws InterruptedException, FilesystemException {
 		final Path[] files = new Path[sources.length];
 		final long[] modTimes = new long[sources.length];
 		runMultiSourceOperation(new SourceOp() {
 			@Override
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
 			}
 		});
 		long max = 0L;
 		Path result = null;
 		for (int i = 0; i < sources.length; i++) {
 			Path file = files[i];
 			if (file == null)
 				continue;
 			long mod = modTimes[i];
 			if (mod > max) {
 				result = file;
 				max = mod;
 			}
 		}
 		return result;
 	}
 	
 }
