 package org.genericsystem.core;
 
 import java.io.ByteArrayOutputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.nio.channels.OverlappingFileLockException;
 import java.text.ParseException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.NavigableMap;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 import org.genericsystem.core.AbstractWriter.AbstractLoader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Nicolas Feybesse
  * @author Michael Ory
  */
 public class Archiver {
 
 	private static final Logger log = LoggerFactory.getLogger(Archiver.class);
 
 	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
 
 	private Engine engine;
 	private File directory;
 
 	private FileOutputStream lockFile;
 
 	public Archiver(Engine engine, String directoryPath) {
 		this.engine = engine;
 		prepareAndLockDirectory(directoryPath);
 		String snapshotPath = getSnapshotPath();
 		if (snapshotPath != null)
 			new ZipLoader().loadSnapshot(snapshotPath);
 		else
 			((EngineImpl) engine).restoreEngine();
 	}
 
 	private void prepareAndLockDirectory(String directoryPath) {
 		if (directoryPath == null)
 			return;
 		File directory = new File(directoryPath);
 		if (directory.exists()) {
 			if (!directory.isDirectory())
 				throw new IllegalStateException("Datasource path : " + directoryPath + " is not a directory");
 		} else if (!directory.mkdirs())
 			throw new IllegalStateException("Can't make directory : " + directoryPath);
 		try {
 			lockFile = new FileOutputStream(directoryPath + File.separator + Statics.LOCK_FILE_NAME);
 			lockFile.getChannel().tryLock();
 			this.directory = directory;
 		} catch (OverlappingFileLockException e) {
 			throw new IllegalStateException("Locked directory : " + directoryPath);
 		} catch (Exception e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	private String getSnapshotPath() {
 		if (lockFile != null) {
 			NavigableMap<Long, File> snapshotsMap = snapshotsMap(directory);
 			if (!snapshotsMap.isEmpty())
 				return directory.getAbsolutePath() + File.separator + Statics.getFilename(snapshotsMap.lastKey());
 		}
 		return null;
 	}
 
 	private static NavigableMap<Long, File> snapshotsMap(File directory) {
 		NavigableMap<Long, File> snapshotsMap = new TreeMap<Long, File>();
 		for (File file : directory.listFiles()) {
 			String filename = file.getName();
 			if (!file.isDirectory() && filename.endsWith(Statics.ZIP_EXTENSION)) {
 				filename = filename.substring(0, filename.length() - Statics.ZIP_EXTENSION.length());
 				if (filename.matches(Statics.MATCHING_REGEX))
 					try {
 						snapshotsMap.put(getTimestamp(filename), file);
 					} catch (ParseException pe) {
 						throw new IllegalStateException(pe);
 					}
 			}
 		}
 		return snapshotsMap;
 	}
 
 	private static long getTimestamp(final String filename) throws ParseException {
 		return Long.parseLong(filename.substring(filename.lastIndexOf("---") + 3));
 	}
 
 	public void startScheduler() {
 		if (lockFile != null)
 			if (Statics.SNAPSHOTS_PERIOD > 0L) {
 				scheduler.scheduleAtFixedRate(new Runnable() {
 					@Override
 					public void run() {
 						new ZipWriter(directory).doSnapshot();
 					}
 				}, Statics.SNAPSHOTS_INITIAL_DELAY, Statics.SNAPSHOTS_PERIOD, TimeUnit.MILLISECONDS);
 			}
 	}
 
 	public void close() {
 		if (lockFile != null) {
 			scheduler.shutdown();
 			new ZipWriter(directory).doSnapshot();
 			try {
 				lockFile.close();
 				lockFile = null;
 			} catch (IOException e) {
 				log.error(e.getMessage(), e);
 			}
 		}
 	}
 
 	public class ZipWriter extends AbstractWriter {
 		private ObjectOutputStream formalOutputStream;
 		private ObjectOutputStream contentOutputStream;
 
 		private String path;
 		private String fileName;
 
 		public ZipWriter(File directory) {
 			path = directory.getAbsolutePath() + File.separator;
 			fileName = Statics.getFilename(engine.pickNewTs());
 		}
 
 		@Override
 		public ObjectOutputStream getFormalOutputStream() {
 			return formalOutputStream;
 		}
 
 		@Override
 		public ObjectOutputStream getContentOutputStream() {
 			return contentOutputStream;
 		}
 
 		public void doSnapshot() {
			engine.newCache().start();
 			ByteArrayOutputStream bufferTemp = new ByteArrayOutputStream();
 			try (FileOutputStream fileOutputStream = new FileOutputStream(path + fileName + Statics.ZIP_EXTENSION + Statics.PART_EXTENSION);) {
 				ZipOutputStream zipOutput = new ZipOutputStream(fileOutputStream);
 				zipOutput.putNextEntry(new ZipEntry(fileName + Statics.CONTENT_EXTENSION));
 				this.formalOutputStream = new ObjectOutputStream(bufferTemp);
 				this.contentOutputStream = new ObjectOutputStream(zipOutput);
 
 				writeGenericsAndFlush();
 				zipOutput.closeEntry();
 
 				zipOutput.putNextEntry(new ZipEntry(fileName + Statics.FORMAL_EXTENSION));
 				zipOutput.write(bufferTemp.toByteArray());
 				zipOutput.closeEntry();
 
 				zipOutput.flush();
 				contentOutputStream.close();
 				formalOutputStream.close();
 				zipOutput.close();
 
 				new File(path + fileName + Statics.ZIP_EXTENSION + Statics.PART_EXTENSION).renameTo(new File(path + fileName + Statics.ZIP_EXTENSION));
 				manageOldSnapshots();
 			} catch (Exception e) {
 				log.error(e.getMessage(), e);
 			}
 		}
 
 		@Override
 		public void writeGenerics() throws IOException {
 			Map<Long, HomeTreeNode> homeTreeMap = new HashMap<>();
 			for (Generic orderGeneric : Transaction.orderDependencies(engine))
 				writeGeneric(((GenericImpl) orderGeneric), homeTreeMap);
 		}
 
 		private void manageOldSnapshots() {
 			NavigableMap<Long, File> snapshotsMap = snapshotsMap(directory);
 			long lastTs = snapshotsMap.lastKey();
 			long firstTs = snapshotsMap.firstKey();
 			long ts = firstTs;
 			for (long snapshotTs : new TreeSet<Long>(snapshotsMap.keySet()))
 				if (snapshotTs != lastTs && snapshotTs != firstTs)
 					if ((snapshotTs - ts) < minInterval((lastTs - snapshotTs)))
 						removeSnapshot(snapshotsMap, snapshotTs);
 					else
 						ts = snapshotTs;
 		}
 
 		private long minInterval(long periodNumber) {
 			return (long) Math.floor(periodNumber / Statics.ARCHIVER_COEFF);
 		}
 
 		private void removeSnapshot(NavigableMap<Long, File> snapshotsMap, long ts) {
 			snapshotsMap.get(ts).delete();
 			snapshotsMap.remove(ts);
 		}
 
 	}
 
 	public class ZipLoader extends AbstractLoader {
 		private ObjectInputStream contentInputStream;
 		private ObjectInputStream formalInputStream;
 
 		@Override
 		public ObjectInputStream getContentInputStream() {
 			return contentInputStream;
 		}
 
 		@Override
 		public ObjectInputStream getFormalInputStream() {
 			return formalInputStream;
 		}
 
 		public void loadSnapshot(String path) {
 			try {
 				contentInputStream = new ObjectInputStream(readContent(path));
 				formalInputStream = new ObjectInputStream(readFormal(path));
 				Map<Long, HomeTreeNode> homeTreeMap = new HashMap<>();
 				Map<Long, Generic> genericMap = new HashMap<>();
 				engine = restoreEngine(homeTreeMap, genericMap);
 				for (;;)
 					loadGeneric(engine, homeTreeMap, genericMap);
 			} catch (EOFException ignore) {
 			} catch (Exception e) {
 				throw new IllegalStateException(e);
 			}
 		}
 
 		private ZipInputStream readFormal(String path) throws IOException {
 			ZipInputStream inputStream = new ZipInputStream(new FileInputStream(new File(path + Statics.ZIP_EXTENSION)));
 			inputStream.getNextEntry();
 			inputStream.getNextEntry();
 			return inputStream;
 		}
 
 		private ZipInputStream readContent(String path) throws IOException {
 			ZipInputStream inputStream = new ZipInputStream(new FileInputStream(new File(path + Statics.ZIP_EXTENSION)));
 			inputStream.getNextEntry();
 			return inputStream;
 		}
 
 		protected Engine restoreEngine(Map<Long, HomeTreeNode> homeTreeMap, Map<Long, Generic> genericMap) throws IOException, ClassNotFoundException {
 			long[] ts = loadTs();
 			long homeTreeNodeTs = getContentInputStream().readLong();
 			getContentInputStream().readLong();
 			getContentInputStream().readObject();
 			((EngineImpl) engine).restoreEngine(homeTreeNodeTs, ts[0], ts[1], ts[2], ts[3]);
 			genericMap.put(ts[0], engine);
 			homeTreeMap.put(homeTreeNodeTs, ((EngineImpl) engine).homeTreeNode());
 			return engine;
 		}
 
 		@Override
 		protected void plug(GenericImpl generic) {
 			generic.plug();
 		}
 
 		@Override
 		protected Generic loadAncestor(Engine engine, long ts, Map<Long, Generic> genericMap) {
 			return genericMap.get(ts);
 		}
 	}
 
 }
