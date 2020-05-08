 package uk.co.unclealex.music.sync;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FilenameUtils;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.co.unclealex.music.Device;
 import uk.co.unclealex.music.DeviceService;
 import uk.co.unclealex.music.DeviceVisitor;
 import uk.co.unclealex.music.FileService;
 import uk.co.unclealex.music.FileSystemDevice;
 import uk.co.unclealex.music.IpodDevice;
 import uk.co.unclealex.music.MtpDevice;
 import uk.co.unclealex.music.PlaylistService;
 import uk.co.unclealex.process.NamedRunnable;
 import uk.co.unclealex.process.ProcessService;
 
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
 import com.google.common.base.Joiner;
 import com.google.common.base.Predicates;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.common.io.ByteStreams;
 import com.google.common.io.Closeables;
 import com.google.common.io.Files;
 import com.google.inject.Inject;
 
 public class DeviceSynchroniser implements Synchroniser {
 
 	private static final long MILLISECONDS_IN_HOUR = 60 * 60 * 1000;
 
 	protected Logger log = LoggerFactory.getLogger(getClass());
 	
 	private DeviceService i_deviceService;
 	private FileService i_fileService;
 	private DeviceImplementation i_deviceImplementation;
 	private ProcessService i_processService;
 	private PlaylistService i_playlistService;
 	private ExecutorService i_executorService;
 	
 	@Inject
 	public DeviceSynchroniser(ExecutorService executorService, DeviceService deviceService, FileService fileService, ProcessService processService, PlaylistService playlistService, Device device) {
 		super();
 		i_executorService = executorService;
 		i_deviceService = deviceService;
 		i_fileService = fileService;
 		i_processService = processService;
 		i_playlistService = playlistService;
 		DeviceVisitor<DeviceImplementation> visitor = new DeviceVisitor<DeviceSynchroniser.DeviceImplementation>() {
 			@Override
 			public DeviceImplementation visit(IpodDevice ipodDevice) {
 				return new IpodDeviceImplementation(ipodDevice);
 			}
 			@Override
 			public DeviceImplementation visit(FileSystemDevice fileSystemDevice) {
 				try {
 					return new FileSystemDeviceImplementation(fileSystemDevice);
 				}
 				catch (IOException e) {
 					throw new IllegalStateException(e);
 				}
 			}
 			@Override
 			public DeviceImplementation visit(MtpDevice mtpDevice) {
 				return new MtpDeviceImplementation(mtpDevice);
 			}
 		};
 		i_deviceImplementation = device.accept(visitor);
 	}
 
 	@Override
 	public void synchronise() throws IOException {
 		DeviceImplementation deviceImplementation = getDeviceImplementation();
 		Device device = deviceImplementation.getDevice();
 		try {
 			deviceImplementation.initialiseDevice();
 			Set<DeviceFile> deviceFiles = deviceImplementation.listDeviceFiles();
 			SortedSet<LocalFile> localFiles = listLocalFiles();
 			Map<String, DeviceFile> deviceFilesByRelativePath = mapByRelativePath(deviceFiles);
 			Map<String, LocalFile> localFilesByRelativePath = mapByRelativePath(localFiles);
 			SortedSet<DeviceFile> deviceFilesToRemove = new TreeSet<DeviceFile>();
 			SortedSet<LocalFile> localFilesToAdd = new TreeSet<LocalFile>();
 			for (Map.Entry<String, LocalFile> entry : localFilesByRelativePath.entrySet()) {
 				String relativePath = entry.getKey();
 				LocalFile localFile = entry.getValue();
 				DeviceFile deviceFile = deviceFilesByRelativePath.get(relativePath);
 				if (deviceFile == null || laterThan(localFile.getLastModified(), deviceFile.getLastModified())) {
 					if (deviceFile != null) {
 						deviceFilesToRemove.add(deviceFile);
 						deviceFilesByRelativePath.remove(relativePath);
 					}
 					localFilesToAdd.add(localFile);
 				}
 				else if (deviceFile != null) {
 					log.info(String.format("File %s will be kept.", deviceFile));
 					deviceFilesByRelativePath.remove(relativePath);
 				}
 			}
 			deviceFilesToRemove.addAll(deviceFilesByRelativePath.values());
 			for (DeviceFile deviceFile : deviceFilesToRemove) {
 				log.info("Removing " + deviceFile.getRelativePath());
 				deviceImplementation.remove(deviceFile);
 			}
 			for (LocalFile localFile : localFilesToAdd) {
 				String relativePath = localFile.getRelativePath();
 				log.info("Adding " + relativePath);
 				deviceImplementation.add(localFile);
 			}
 			log.info("Removing all playlists.");
 			deviceImplementation.removePlaylists();
 			
 			deviceFilesByRelativePath = mapByRelativePath(deviceImplementation.listDeviceFiles());
 			Map<String, Iterable<String>> playlists = getPlaylistService().createPlaylists(device.getOwner(), device.getEncoding());
 			final Function<String, DeviceFile> deviceFileFunction = Functions.forMap(deviceFilesByRelativePath);
 			Function<Iterable<String>, Iterable<DeviceFile>> deviceFilesFunction = new Function<Iterable<String>, Iterable<DeviceFile>>() {
 				@Override
 				public Iterable<DeviceFile> apply(Iterable<String> relativePaths) {
 					return Iterables.transform(relativePaths, deviceFileFunction);
 				}
 			};
 			Map<String, Iterable<DeviceFile>> deviceFilesPlaylists = Maps.transformValues(playlists, deviceFilesFunction);
 			for (Entry<String, Iterable<DeviceFile>> entry : deviceFilesPlaylists.entrySet()) {
 				String playlistName = entry.getKey();
 				Iterable<DeviceFile> playlistDeviceFiles = entry.getValue();
 				log.info("Adding playlist " + playlistName);
 				deviceImplementation.addPlaylist(playlistName, playlistDeviceFiles);
 			}
 		}
 		catch (RuntimeException e) {
 			log.error("There was an unexpected error trying to synchronise " + device, e);
 		}
 		finally {
 			log.info("Closing " + device);
 			deviceImplementation.closeDevice();
 		}
 	}
 	
 	protected Map<LocalFile, DeviceFile> mapDeviceFilesByLocalFile(Map<String, LocalFile> localFilesByRelativePath,
 			Map<String, DeviceFile> deviceFilesByRelativePath) {
 		Map<LocalFile, DeviceFile> deviceFilesByLocalFile = new HashMap<LocalFile, DeviceFile>();
 		for (Entry<String, DeviceFile> entry : deviceFilesByRelativePath.entrySet()) {
 			String relativePath = entry.getKey();
 			DeviceFile deviceFile = entry.getValue();
 			LocalFile localFile = localFilesByRelativePath.get(relativePath);
 			if (localFile != null) {
 				deviceFilesByLocalFile.put(localFile, deviceFile);
 			}
 		}
 		return deviceFilesByLocalFile;
 	}
 
 	/**
 	 * Test if the lhs is later than the rhs but also that there is not exactly an hour's difference.
 	 * @param lhs
 	 * @param rhs
 	 * @return
 	 */
 	protected boolean laterThan(long lhs, long rhs) {
 		if (Math.abs(lhs - rhs) == MILLISECONDS_IN_HOUR) {
 			return false;
 		}
 		else {
 			return lhs > rhs;
 		}
 	}
 
 	protected SortedSet<LocalFile> listLocalFiles() throws IOException {
 		RelativePathFileFactory<LocalFile> factory = new RelativePathFileFactory<LocalFile>() {
 			@Override
 			public LocalFile createRelativeFilePath(String relativePath, File f) throws IOException {
 				return new LocalFile(f.getCanonicalFile(), relativePath);
 			}
 		};
 		return listRelativePathFiles(getDeviceService().getDeviceDirectory(getDeviceImplementation().getDevice()), factory);
 	}
 	
 	protected <R extends RelativePathFile<R>> SortedSet<R> listRelativePathFiles(
 			File directory, RelativePathFileFactory<R> factory) throws IOException {
 		SortedSet<R> relativePathFiles = new TreeSet<R>();
 		Device device = getDeviceImplementation().getDevice();
 		String extension = device.getEncoding().getExtension();
 		for (File file : directory.listFiles()) {
 			listRelativePathFiles(relativePathFiles, file, "", extension, factory);
 		}
 		return relativePathFiles;
 	}
 
 	protected <R extends RelativePathFile<R>> void listRelativePathFiles(
 			SortedSet<R> relativePathFiles, File f, String path, String extension, RelativePathFileFactory<R> factory) throws IOException {
 		String name = f.getName();
 		if (f.isDirectory()) {
 			path += name + File.separatorChar;
 			for (File child : f.listFiles()) {
 				listRelativePathFiles(relativePathFiles, child, path, extension, factory);
 			}
 		}
 		else if (extension.equals(FilenameUtils.getExtension(name))) {
 			relativePathFiles.add(factory.createRelativeFilePath(path + name, f));
 		}
 	}
 
 	protected interface RelativePathFileFactory<R extends RelativePathFile<R>> {
 		public R createRelativeFilePath(String relativePath, File f) throws IOException;
 	}
 	
 	protected <R extends RelativePathFile<R>> Map<String, R> mapByRelativePath(Set<R> relativePathFiles) {
 		Map<String, R> filesByRelativePath = new TreeMap<String, R>();
 		for (R file : relativePathFiles) {
 			filesByRelativePath.put(file.getRelativePath(), file);
 		}
 		return filesByRelativePath;
 	}
 
 	protected interface DeviceImplementation {
 		public void initialiseDevice() throws IOException;
 
 		public Set<DeviceFile> listDeviceFiles() throws IOException;
 
 		public void add(LocalFile localFile) throws IOException;
 
 		public void remove(DeviceFile deviceFile) throws IOException;
 
 		public void removePlaylists() throws IOException;
 		
 		public void addPlaylist(String playlistName, Iterable<DeviceFile> deviceFiles) throws IOException;
 		
 		public void closeDevice() throws IOException;
 		
 		public Device getDevice();
 	}
 	
 	protected abstract class AbstractDeviceImplementation<D extends Device> implements DeviceImplementation {
 		
 		private D i_device;
 
 		protected AbstractDeviceImplementation(D device) {
 			super();
 			i_device = device;
 		}
 		
 		@Override
 		public void removePlaylists() throws IOException {
 			log.warn("Playlists are not supported on " + getDevice());
 		}
 		
 		@Override
 		public void addPlaylist(String playlistName, Iterable<DeviceFile> deviceFiles) throws IOException {
 			log.warn("Playlists are not supported on " + getDevice());
 		}
 		
 		public D getDevice() {
 			return i_device;
 		}
 	}
 	protected abstract class AbstractFileSystemDeviceImplementation<D extends Device> extends AbstractDeviceImplementation<D> {
 
 		private File i_deviceRoot;
 		
 		protected AbstractFileSystemDeviceImplementation(D device) {
 			super(device);
 		}
 
 		@Override
 		public Set<DeviceFile> listDeviceFiles() throws IOException {
 			RelativePathFileFactory<DeviceFile> factory = new RelativePathFileFactory<DeviceFile>() {
 				@Override
 				public DeviceFile createRelativeFilePath(String relativePath, File f) throws IOException {
 					return createDeviceFile(relativePath, f);
 				}
 			};
 			return listRelativePathFiles(getDeviceRoot(), factory);
 		}
 
 		@Override
 		public void remove(DeviceFile deviceFile) throws IOException {
 			File file = new File(getDeviceRoot(), deviceFile.getId());
 			file.delete();
 		}
 
 		@Override
 		public void add(LocalFile localFile) throws IOException {
 			String remoteRelativeFilePath = createRemoteRelativeFilePath(localFile);
 			File targetFile = new File(getDeviceRoot(), remoteRelativeFilePath);
 			targetFile.getParentFile().mkdirs();
 			FileInputStream in = null;
 			FileOutputStream out = null;
 			try {
 				File sourceFile = localFile.getFile();
 				in = new FileInputStream(sourceFile);
 				out = new FileOutputStream(targetFile);
 				in.getChannel().transferTo(0, sourceFile.length(), out.getChannel());
 			}
 			finally {
 				Closeables.closeQuietly(in);
 				Closeables.closeQuietly(out);
 			}
 		}
 
 		@Override
 		public void closeDevice() throws IOException {
 			D device = getDevice();
 			try {
 				removeEmptyDirectories(getDeviceRoot(), false, device.getEncoding().getExtension());
 			}
 			finally {
 				try {
 					disconnect();
 				}
 				catch (Throwable t) {
 					log.warn("Could not disconnect device " + device, t);
 				}
 			}
 		}
 
 		protected void removeEmptyDirectories(File dir, boolean deleteDirectory, String extension) {
 			for (File child : dir.listFiles()) {
 				if (child.isDirectory()) {
 					removeEmptyDirectories(child, true, extension);
 				}
 				else if (!extension.equals(FilenameUtils.getExtension(child.getName()))) {
 					child.delete();
 				}
 			}
 			if (dir.listFiles().length == 0) {
 				dir.delete();
 			}
 		}
 
 		protected abstract DeviceFile createDeviceFile(String relativePath, File f);
 
 		protected abstract String createRemoteRelativeFilePath(LocalFile localFile);
 
 		protected abstract void disconnect() throws IOException;
 
 		public File getDeviceRoot() {
 			return i_deviceRoot;
 		}
 
 		public void setDeviceRoot(File deviceRoot) {
 			i_deviceRoot = deviceRoot;
 		}
 	}
 
 	public class FileSystemDeviceImplementation extends AbstractFileSystemDeviceImplementation<FileSystemDevice> {
 
 		private File i_mountPoint;
 		private File i_musicRoot;
 		
 		protected FileSystemDeviceImplementation(FileSystemDevice device) throws IOException {
 			super(device);
 			File mountPoint = device.getMountPoint();
 			i_mountPoint = mountPoint;
 			String relativeMusicRoot = device.getRelativeMusicRoot();
 			i_musicRoot = relativeMusicRoot==null?mountPoint:new File(mountPoint, relativeMusicRoot);
 		}
 
 		@Override
 		public void initialiseDevice() throws IOException {
 			setDeviceRoot(getMusicRoot());
 		}
 
 		@Override
 		protected DeviceFile createDeviceFile(String relativePath, File f) {
 			return new DeviceFile(relativePath, relativePath, f.lastModified());
 		}
 
 		@Override
 		protected String createRemoteRelativeFilePath(LocalFile localFile) {
 			return localFile.getRelativePath();
 		}
 
 		@Override
 		protected void disconnect() throws IOException {
 			FileSystemDevice device = getDevice();
 			if (device.isRemovable()) {
 				ProcessBuilder processBuilder = new ProcessBuilder("pumount", getMountPoint().getCanonicalPath());
 				getProcessService().run(processBuilder, false);
 			}
 		}
 		
 		public File getMountPoint() {
 			return i_mountPoint;
 		}
 		
 		public File getMusicRoot() {
 			return i_musicRoot;
 		}
 	}
 	
 	protected class MtpDeviceImplementation extends AbstractFileSystemDeviceImplementation<MtpDevice> {
 
 		private Pattern i_deviceFilePattern;
 		private File i_mountPoint;
 
 		protected MtpDeviceImplementation(MtpDevice device) {
 			super(device);
 		}
 		
 		@Override
 		public void initialiseDevice() throws IOException {
 			setDeviceFilePattern(Pattern.compile("(.+?)\\.([0-9]+)\\." + Pattern.quote(getDevice().getEncoding().getExtension())));
 			File tempDir = new File(System.getProperty("java.io.tmpdir"));
 			File mountPoint;
 			long time = System.currentTimeMillis();
 			while ((mountPoint = new File(tempDir, "sync" + time)).exists()) {
 				time++;
 			}
 			mountPoint.mkdir();
 			setMountPoint(mountPoint);
 			mountPoint.deleteOnExit();
 			setDeviceRoot(new File(mountPoint, "Music"));
 			ProcessBuilder processBuilder = new ProcessBuilder("mtpfs", mountPoint.getAbsolutePath());
 			processBuilder.start();
 		}
 
 		@Override
 		protected DeviceFile createDeviceFile(String relativePath, File f) {
 			String deviceRelativePath;
 			long lastModified;
 			Matcher matcher = getDeviceFilePattern().matcher(relativePath);
 			if (matcher.matches()) {
 				deviceRelativePath = matcher.group(1) + "." + getDevice().getEncoding().getExtension();
 				lastModified = Long.parseLong(matcher.group(2));
 			}
 			else {
 				deviceRelativePath = relativePath;
 				lastModified = 0;
 			}
 			return new DeviceFile(relativePath, deviceRelativePath, lastModified);
 		}
 
 		@Override
 		protected String createRemoteRelativeFilePath(LocalFile localFile) {
 			String extension = "." + getDevice().getEncoding().getExtension();
 			long lastModified = localFile.getLastModified();
 			return localFile.getRelativePath().replace(extension, "." + Long.toString(lastModified) + extension);
 		}
 
 		@Override
 		protected void disconnect() throws IOException {
 			ProcessBuilder processBuilder = new ProcessBuilder("fusermount", "-u", getMountPoint().getAbsolutePath());
 			try {
 				processBuilder.start().waitFor();
 			}
 			catch (InterruptedException e) {
 				throw new IOException(e);
 			}
 			getMountPoint().delete();
 		}
 
 		public Pattern getDeviceFilePattern() {
 			return i_deviceFilePattern;
 		}
 
 		public void setDeviceFilePattern(Pattern deviceFilePattern) {
 			i_deviceFilePattern = deviceFilePattern;
 		}
 
 		public File getMountPoint() {
 			return i_mountPoint;
 		}
 
 		public void setMountPoint(File mountPoint) {
 			i_mountPoint = mountPoint;
 		}
 	}
 	
 	public abstract class ProcessDeviceImplementation<D extends Device> extends AbstractDeviceImplementation<D> {
 
 		private Process i_process;
 		private PrintWriter i_stdin;
 		private BufferedReader i_stdout;
 		private Future<?> i_stderrFuture;
 		private File i_commandFile;
 		
 		protected ProcessDeviceImplementation(D device) {
 			super(device);
 		}
 
 		@Override
 		public void initialiseDevice() throws IOException {
 			List<String> command = new ArrayList<String>();
 			File commandFile = File.createTempFile("sync-", "");
 			Files.touch(commandFile);
 			commandFile.deleteOnExit();
 			setCommandFile(commandFile);
 			InputStream in = null;
 			OutputStream out = null;
 			try {
 				in = getCommandAsStream();
 				if (in == null) {
 					throw new NullPointerException("Cannot find a synchronising process file");
 				}
 				out = new FileOutputStream(commandFile);
 				ByteStreams.copy(in, out);
 			}
 			finally {
 				Closeables.closeQuietly(in);
 				Closeables.closeQuietly(out);
 			}
 			commandFile.setExecutable(true);
 			command.add(commandFile.getCanonicalPath());
 			command.addAll(Arrays.asList(getCommandArguments()));
 			ProcessBuilder builder = new ProcessBuilder(command);
 			Process process = builder.start();
 			setProcess(process);
 			setStdin(new PrintWriter(process.getOutputStream()));
 			setStdout(new BufferedReader(new InputStreamReader(process.getInputStream())));
 			final BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
 			NamedRunnable runnable = new NamedRunnable() {
 				@Override
 				public void runAfterName() throws IOException {
 					String line;
 					while ((line = stderr.readLine()) != null) {
 						log.info("PROCESS: " + line);
 					}
 				}
 				public void onError(Throwable t) {
 					log.error(t.getMessage(), t);
 				}
 				@Override
 				public void andFinally() {
 					Closeables.closeQuietly(stderr);
 				}
 				@Override
 				public String getThreadName() {
 					return getDevice().getName() + " process";
 				}
 			};
 			Future<?> stderrFuture = getExecutorService().submit(runnable);
 			setStderrFuture(stderrFuture);
 		}
 
 		protected abstract InputStream getCommandAsStream() throws IOException;
 
 		protected abstract String[] getCommandArguments();
 
 		protected Function<String, DeviceFile> deviceFileParserFunction() {
 			return new Function<String, DeviceFile>() {
 				@Override
 				public DeviceFile apply(String str) {
 					if (str.trim().isEmpty() || str.startsWith("**")) {
 						return null;
 					}
 					else {
 						DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinuteSecond();
 						List<String> deviceFileParts = Lists.newArrayList(Splitter.on('|').split(str));
 						DateTime dateTime = formatter.parseDateTime(deviceFileParts.get(2));
 						return new DeviceFile(deviceFileParts.get(0), deviceFileParts.get(1), dateTime.getMillis());
 					}
 				}
 			};
 		}
 		
 		@Override
 		public Set<DeviceFile> listDeviceFiles() throws IOException {
 			List<String> deviceFileStrings = executeCommand("LIST");
 			return Sets.newTreeSet(Iterables.filter(Iterables.transform(deviceFileStrings, deviceFileParserFunction()), Predicates.notNull()));
 		}	
 
 		@Override
 		public void add(LocalFile localFile) throws IOException {
 			executeCommand("ADD", localFile.getRelativePath(), localFile.getFile().getCanonicalPath());
 		}
 
 		@Override
 		public void remove(DeviceFile deviceFile) throws IOException {
 			executeCommand("REMOVE", deviceFile.getId());
 		}
 
 		@Override
 		public void removePlaylists() throws IOException {
 			executeCommand("REMOVEPLAYLISTS");
 		}
 		
 		@Override
 		public void addPlaylist(String playlistName, Iterable<DeviceFile> deviceFiles) throws IOException {
 			Function<DeviceFile, String> idFunction = new Function<DeviceFile, String>() {
 				@Override
 				public String apply(DeviceFile deviceFile) {
 					return deviceFile.getId();
 				}
 			};
 			executeCommand(
 				Iterables.toArray(
 					Iterables.concat(Arrays.asList(new String[] { "ADDPLAYLIST", playlistName }), Iterables.transform(deviceFiles, idFunction)),
 					String.class));
 		}
 		
 		@Override
 		public void closeDevice() throws IOException {
 			try {
 				executeCommand("QUIT");
 			}
 			finally {
 				Closeables.closeQuietly(getStdin());
 				Closeables.closeQuietly(getStdout());
 				try {
 					getStderrFuture().get(5, TimeUnit.SECONDS);
 				}
 				catch (InterruptedException e) {
 					log.error("stderr logging did not close properly.", e);
 				}
 				catch (ExecutionException e) {
 					log.error("stderr logging did not close properly.", e);
 				}
 				catch (TimeoutException e) {
 					log.error("stderr logging did not close properly.", e);
 				}
 				finally {
 					getProcess().destroy();
 					getCommandFile().delete();
 				}
 			}
 		}
 
 		protected List<String> executeCommand(String... command) throws IOException {
 			String fullCommand = Joiner.on('|').join(command);
 			getStdin().println(fullCommand);
 			getStdin().flush();
 			List<String> results = new ArrayList<String>();
 			String line;
 			boolean commandFinished = false;
 			while (!commandFinished && ((line = getStdout().readLine()) != null)) {
 				line = line.trim();
 				if ("OK".equals(line)) {
 					commandFinished = true;
 				}
 				else {
 					results.add(line);
 				}
 			}
 			if (!commandFinished) {
 				throw new IOException("Command " + fullCommand + " failed");
 			}
 			return results;
 		}
 
 		public Process getProcess() {
 			return i_process;
 		}
 
 		public void setProcess(Process process) {
 			i_process = process;
 		}
 
 		public PrintWriter getStdin() {
 			return i_stdin;
 		}
 
 		public void setStdin(PrintWriter stdin) {
 			i_stdin = stdin;
 		}
 
 		public BufferedReader getStdout() {
 			return i_stdout;
 		}
 
 		public void setStdout(BufferedReader stdout) {
 			i_stdout = stdout;
 		}
 
 		public Future<?> getStderrFuture() {
 			return i_stderrFuture;
 		}
 
 		public void setStderrFuture(Future<?> stderrFuture) {
 			i_stderrFuture = stderrFuture;
 		}
 
 		public File getCommandFile() {
 			return i_commandFile;
 		}
 
 		public void setCommandFile(File commandFile) {
 			i_commandFile = commandFile;
 		}		
 	}
 	
 	protected abstract class PythonDeviceImplementation<D extends Device> extends ProcessDeviceImplementation<D> {
 
 		protected PythonDeviceImplementation(D device) {
 			super(device);
 		}
 
 		@Override
 		protected InputStream getCommandAsStream() throws IOException {
 			return getClass().getResourceAsStream("sync.py");
 		}
 	}
 
 	protected class IpodDeviceImplementation extends PythonDeviceImplementation<IpodDevice> {
 
 		protected IpodDeviceImplementation(IpodDevice device) {
 			super(device);
 		}
 
 		@Override
 		protected String[] getCommandArguments() {
 			return new String[] { "ipod", getDevice().getMountPoint().getPath() };
 		}
 
 	}
 	public DeviceService getDeviceService() {
 		return i_deviceService;
 	}
 
 	public FileService getFileService() {
 		return i_fileService;
 	}
 
 	public DeviceImplementation getDeviceImplementation() {
 		return i_deviceImplementation;
 	}
 
 	public ProcessService getProcessService() {
 		return i_processService;
 	}
 
 	public PlaylistService getPlaylistService() {
 		return i_playlistService;
 	}
 
 	public ExecutorService getExecutorService() {
 		return i_executorService;
 	}
 
 	public void setExecutorService(ExecutorService executorService) {
 		i_executorService = executorService;
 	}
 }
