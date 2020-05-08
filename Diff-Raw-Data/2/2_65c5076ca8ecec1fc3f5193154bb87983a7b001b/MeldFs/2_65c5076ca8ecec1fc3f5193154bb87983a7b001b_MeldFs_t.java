 package warrenfalk.meldfs;
 
 import java.io.IOException;
 import java.nio.file.DirectoryStream;
 import java.nio.file.FileSystem;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import warrenfalk.fuselaj.DirBuffer;
 import warrenfalk.fuselaj.Errno;
 import warrenfalk.fuselaj.FileInfo;
 import warrenfalk.fuselaj.Filesystem;
 import warrenfalk.fuselaj.FilesystemException;
 import warrenfalk.fuselaj.Stat;
 
 public class MeldFs extends Filesystem {
 	SourceFs[] sources;
 	String[] fuseArgs;
 	ExecutorService threadPool;
 	
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
 		Path[] devices = new Path[] {
 				fs.getPath("/media/aacbd496-ae23-4be4-8f12-ffb733c1dbfa/test/0"),
 				fs.getPath("/media/aacbd496-ae23-4be4-8f12-ffb733c1dbfa/test/1"),
 				fs.getPath("/media/aacbd496-ae23-4be4-8f12-ffb733c1dbfa/test/2"),
 				fs.getPath("/media/aacbd496-ae23-4be4-8f12-ffb733c1dbfa/test/3"),
 				fs.getPath("/media/aacbd496-ae23-4be4-8f12-ffb733c1dbfa/test/4"),
 				fs.getPath("/media/aacbd496-ae23-4be4-8f12-ffb733c1dbfa/test/5"),
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
 	protected void getattr(String path, Stat stat) throws FilesystemException {
 		try {
 			// attempt to find entry with that name
 			Path file = getFile(path);
 			if (file == null)
 				throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 			os_stat(file.toAbsolutePath().toString(), stat);
 		}
 		catch (InterruptedException ie) {
 			throw new FilesystemException(Errno.InterruptedSystemCall);
 		}
 	}
 	
 	@Override
 	protected void opendir(String path, FileInfo fi) throws FilesystemException {
 		try {
 			Path dir = getFile(path);
 			if (!Files.isDirectory(dir))
 				throw new FilesystemException(Errno.NotADirectory);
 			FileHandle.open(fi, path);
 		}
 		catch (InterruptedException e) {
 			throw new FilesystemException(Errno.InterruptedSystemCall);
 		}
 	}
 	
 	@Override
 	protected void readdir(String path, DirBuffer dirBuffer, FileInfo fileInfo) throws FilesystemException {
 		FileHandle fh = FileHandle.get(fileInfo.getFileHandle());
 		String[] names;
 		if (fh.data instanceof String) {
			final String dirpath = ((String)fh.data).substring(1);
 			final HashSet<String> items = new HashSet<String>();
 			final AtomicInteger sync = new AtomicInteger(sources.length);
 			for (int i = 0; i < sources.length; i++) {
 				final int index = i;
 				threadPool.execute(new Runnable() {
 					@Override
 					public void run() {
 						SourceFs source = sources[index];
 						try {
 							Path p = source.root.resolve(dirpath);
 							if (Files.isDirectory(p)) {
 								try (DirectoryStream<Path> stream = Files.newDirectoryStream(p)) {
 									for (Path item : stream) {
 										String itemName = item.getFileName().toString();
 										synchronized (items) {
 											items.add(itemName);
 										}
 									}
 								}
 							}
 							if (0 == sync.decrementAndGet()) {
 								synchronized (sync) {
 									sync.notify();
 								}
 							}
 						}
 						catch (IOException ioe) {
 							source.onIoException(ioe);
 						}
 					}
 				});
 			}
 			try {
 				synchronized (sync) {
 					sync.wait();
 				}
 			}
 			catch (InterruptedException e) {
 				throw new FilesystemException(Errno.InterruptedSystemCall);
 			}
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
 	protected void releasedir(String path, FileInfo fi) throws FilesystemException {
 		FileHandle.release(fi);
 	}
 	
 	private Path getFile(final String path) throws InterruptedException {
 		final Path[] files = new Path[sources.length];
 		final long[] modTimes = new long[sources.length];
 		final AtomicInteger sync = new AtomicInteger(sources.length);
 		for (int i = 0; i < sources.length; i++) {
 			final int index = i;
 			threadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					SourceFs source = sources[index];
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
 					if (0 == sync.decrementAndGet()) {
 						synchronized (sync) {
 							sync.notify();
 						}
 					}
 						
 				}
 			});
 		}
 		synchronized (sync) {
 			sync.wait();
 		}
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
