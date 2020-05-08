 package org.araqne.logstorage.engine;
 
 import java.io.IOException;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.araqne.logstorage.LogFileService;
 import org.araqne.logstorage.LogFileServiceRegistry;
 import org.araqne.logstorage.LogStorage;
 import org.araqne.logstorage.LogTableRegistry;
 import org.araqne.logstorage.LogWriterStatus;
 import org.araqne.logstorage.TableSchema;
 import org.araqne.logstorage.dump.DumpDriver;
 import org.araqne.logstorage.dump.DumpManifest;
 import org.araqne.logstorage.dump.DumpService;
 import org.araqne.logstorage.dump.ExportRequest;
 import org.araqne.logstorage.dump.ExportTabletTask;
 import org.araqne.logstorage.dump.ExportTask;
 import org.araqne.logstorage.dump.ExportWorker;
 import org.araqne.logstorage.dump.ImportRequest;
 import org.araqne.logstorage.dump.ImportTask;
 import org.araqne.logstorage.dump.ImportWorker;
 import org.araqne.storage.api.FilePath;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component(name = "logstorage-dump-service")
 @Provides
 public class DumpServiceImpl implements DumpService {
 	private static final Logger slog = LoggerFactory.getLogger(DumpServiceImpl.class);
 
 	@Requires
 	private LogTableRegistry tableRegistry;
 
 	@Requires
 	private LogStorage storage;
 
 	@Requires
 	private LogFileServiceRegistry logFileServiceRegistry;
 
 	private ConcurrentHashMap<String, DumpDriver> drivers = new ConcurrentHashMap<String, DumpDriver>();
 	private ConcurrentHashMap<String, ExportWorker> exportWorkers = new ConcurrentHashMap<String, ExportWorker>();
 	private ConcurrentHashMap<String, ImportWorker> importWorkers = new ConcurrentHashMap<String, ImportWorker>();
 
	@Invalidate
	public void stop() {
		for (ExportWorker worker : exportWorkers.values()) {
			worker.getTask().setCancelled();
		}

		for (ImportWorker worker : importWorkers.values()) {
			worker.getTask().setCancelled();
		}

		exportWorkers.clear();
		importWorkers.clear();
		drivers.clear();
	}

 	@Override
 	public List<ExportTask> getExportTasks() {
 		List<ExportTask> tasks = new ArrayList<ExportTask>();
 		for (ExportWorker worker : exportWorkers.values()) {
 			tasks.add(worker.getTask().clone());
 		}
 		return tasks;
 	}
 
 	@Override
 	public ExportTask getExportTask(String guid) {
 		ExportWorker worker = exportWorkers.get(guid);
 		if (worker == null)
 			return null;
 
 		return worker.getTask().clone();
 	}
 
 	@Override
 	public List<ImportTask> getImportTasks() {
 		List<ImportTask> tasks = new ArrayList<ImportTask>();
 		for (ImportWorker worker : importWorkers.values()) {
 			tasks.add(worker.getTask().clone());
 		}
 		return tasks;
 	}
 
 	@Override
 	public ImportTask getImportTask(String guid) {
 		ImportWorker worker = importWorkers.get(guid);
 		if (worker == null)
 			return null;
 
 		return worker.getTask().clone();
 	}
 
 	@Override
 	public String beginExport(ExportRequest req) {
 		DumpDriver driver = ensureDriver(req.getDriverType());
 		ExportWorker worker = driver.newExportWorker(req);
 		ExportWorker old = exportWorkers.putIfAbsent(req.getGuid(), worker);
 		if (old != null)
 			throw new IllegalStateException("duplicated export job guid: " + req.getGuid());
 
 		new SafeExportWorker(worker).start();
 
 		return req.getGuid();
 	}
 
 	@Override
 	public void cancelExport(String guid) {
 		ExportWorker worker = exportWorkers.get(guid);
 		if (worker == null)
 			throw new IllegalStateException("export job not found: " + guid);
 
 		worker.getTask().setCancelled();
 	}
 
 	@Override
 	public DumpManifest readManifest(String driverType, Map<String, String> params) throws IOException {
 		DumpDriver driver = ensureDriver(driverType);
 		return driver.readManifest(params);
 	}
 
 	private DumpDriver ensureDriver(String type) {
 		if (type == null)
 			throw new IllegalArgumentException("driver name should not be null");
 
 		DumpDriver driver = drivers.get(type);
 		if (driver == null)
 			throw new IllegalStateException("unsupported driver: " + type);
 
 		return driver;
 	}
 
 	@Override
 	public String beginImport(ImportRequest req) {
 		DumpDriver driver = ensureDriver(req.getDriverType());
 		ImportWorker worker = driver.newImportWorker(req);
 		ImportWorker old = importWorkers.putIfAbsent(req.getGuid(), worker);
 		if (old != null)
 			throw new IllegalStateException("duplicated import job guid: " + req.getGuid());
 
 		new SafeImportWorker(worker).start();
 
 		return req.getGuid();
 	}
 
 	@Override
 	public void cancelImport(String guid) {
 		ImportWorker worker = importWorkers.get(guid);
 		if (worker == null)
 			throw new IllegalStateException("import job not found: " + guid);
 
 		worker.getTask().setCancelled();
 	}
 
 	@Override
 	public List<DumpDriver> getDumpDrivers() {
 		return new ArrayList<DumpDriver>(drivers.values());
 	}
 
 	@Override
 	public DumpDriver getDumpDriver(String name) {
 		return drivers.get(name);
 	}
 
 	@Override
 	public void registerDriver(DumpDriver driver) {
 		DumpDriver old = drivers.putIfAbsent(driver.getType(), driver);
 		if (old != null)
 			throw new IllegalStateException("duplicated dump driver: " + driver.getType());
 	}
 
 	@Override
 	public void unregisterDriver(DumpDriver driver) {
 		drivers.remove(driver.getType(), driver);
 	}
 
 	@Override
 	public List<ExportTabletTask> estimate(ExportRequest req) {
 		List<ExportTabletTask> tasks = new ArrayList<ExportTabletTask>();
 
 		List<LogWriterStatus> memoryBuffers = storage.getWriterStatuses();
 		for (String tableName : req.getTableNames())
 			countFiles(tableName, req.getFrom(), req.getTo(), memoryBuffers, tasks);
 
 		return tasks;
 	}
 
 	private int getMemoryCount(List<LogWriterStatus> memoryBuffers, String tableName, Date day) {
 		for (LogWriterStatus buffer : memoryBuffers)
 			if (buffer.getTableName().equals(tableName) && buffer.getDay().equals(day))
 				return buffer.getBufferSize();
 
 		return 0;
 	}
 
 	private void countFiles(String tableName, Date from, Date to, List<LogWriterStatus> memoryBuffers,
 			List<ExportTabletTask> tasks) {
 		TableSchema schema = tableRegistry.getTableSchema(tableName, true);
 		String fileType = schema.getPrimaryStorage().getType();
 		FilePath dir = storage.getTableDirectory(tableName);
 		countFiles(tableName, fileType, dir, from, to, memoryBuffers, tasks);
 	}
 
 	private void countFiles(String tableName, String type, FilePath dir, Date from, Date to, List<LogWriterStatus> memoryBuffers,
 			List<ExportTabletTask> tasks) {
 		FilePath[] files = dir.listFiles();
 		if (files == null)
 			return;
 
 		LogFileService fileService = logFileServiceRegistry.getLogFileService(type);
 		if (fileService == null)
 			return;
 
 		ArrayList<FilePath> paths = new ArrayList<FilePath>();
 		for (FilePath f : files)
 			if (f.isFile())
 				paths.add(f);
 
 		Collections.sort(paths);
 
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 		for (FilePath path : paths) {
 			if (path.getName().endsWith(".idx")) {
 				long count = fileService.count(path);
 				Date day = df.parse(path.getName().substring(0, path.getName().length() - 4), new ParsePosition(0));
 				if (day == null)
 					continue;
 
 				if (from != null && day.before(from))
 					continue;
 
 				if (to != null && day.after(to))
 					continue;
 
 				count += getMemoryCount(memoryBuffers, tableName, day);
 				TableSchema schema = tableRegistry.getTableSchema(tableName);
 				ExportTabletTask task = new ExportTabletTask(tableName, day, schema.getId());
 				task.setEstimatedCount(count);
 				tasks.add(task);
 			}
 		}
 	}
 
 	private class SafeImportWorker extends Thread {
 		private ImportWorker worker;
 
 		public SafeImportWorker(ImportWorker worker) {
 			super("Table Importer [" + worker.getTask().getGuid() + "]");
 			this.worker = worker;
 		}
 
 		@Override
 		public void run() {
 			ImportTask task = worker.getTask();
 			try {
 				worker.run();
 			} catch (Throwable t) {
 				slog.error("araqne logstorage: import job [" + task.getGuid() + "] failed", t);
 				task.setCancelled();
 			} finally {
 				task.setCompleted();
 				importWorkers.remove(task.getGuid());
 			}
 		}
 	}
 
 	private class SafeExportWorker extends Thread {
 		private ExportWorker worker;
 
 		public SafeExportWorker(ExportWorker worker) {
 			super("Table Exporter [" + worker.getTask().getGuid() + "]");
 			this.worker = worker;
 		}
 
 		@Override
 		public void run() {
 			ExportTask task = worker.getTask();
 			try {
 				worker.run();
 			} catch (Throwable t) {
 				slog.error("araqne logstorage: export job [" + task.getGuid() + "] failed", t);
 				task.setCancelled();
 			} finally {
 				task.setCompleted();
 				exportWorkers.remove(task.getGuid());
 			}
 		}
 	}
 }
