 package ch.bergturbenthal.raoa.server.watcher;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Path;
 import java.nio.file.StandardWatchEventKinds;
 import java.nio.file.WatchEvent;
 import java.nio.file.WatchKey;
 import java.nio.file.WatchService;
 import java.util.concurrent.ExecutorService;
 
 import javax.annotation.PostConstruct;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class FileWatcher implements Closeable {
 	private static Logger logger = LoggerFactory.getLogger(FileWatcher.class);
 
 	private final File basePath;
 	@Autowired
 	private ExecutorService executorService;
 	@Autowired
 	private DirectoryNotificationService notificationService;
 	private Thread watcherThread;
 
 	private WatchService watchService;
 
	public static FileWatcher createWatcher(final File basePath) {
		return new FileWatcher(basePath);
	}

 	public FileWatcher(final File basePath) {
 		this.basePath = basePath;
 	}
 
 	@Override
 	public void close() {
 		watcherThread.interrupt();
 	}
 
 	@PostConstruct
 	public void init() {
 		try {
 			final Path path = basePath.toPath();
 
 			watchService = path.getFileSystem().newWatchService();
 			path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
 
 			final Runnable watchRunnable = new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 						for (;;) {
 							final WatchKey watchKey;
 							try {
 								watchKey = watchService.take();
 							} catch (final InterruptedException e) {
 								return;
 							}
 							try {
 								for (final WatchEvent<?> event : watchKey.pollEvents()) {
 									final Path changedPath = (Path) event.context();
 									final File addedDirectory = path.resolve(changedPath).toFile();
 									processDirectory(addedDirectory);
 								}
 							} catch (final Throwable t) {
 								logger.warn("Cannot process watch-event", t);
 							}
 							final boolean valid = watchKey.reset();
 							if (!valid) {
 								break;
 							}
 
 						}
 					} finally {
 						try {
 							watchService.close();
 						} catch (final IOException e) {
 							logger.error("Cannot close watcher for " + basePath, e);
 						}
 					}
 				}
 			};
 			watcherThread = new Thread(watchRunnable, "FileWatcher " + basePath);
 			watcherThread.start();
 			executorService.submit(new Runnable() {
 
 				@Override
 				public void run() {
 					for (final File existingFile : basePath.listFiles()) {
 						processDirectory(existingFile);
 					}
 				}
 			});
 
 		} catch (final IOException e) {
			throw new RuntimeException("Cannot watch " + basePath, e);
 		}
 
 	}
 
 	private void processDirectory(final File directory) {
 		notificationService.notifyDirectory(directory);
 	}
 }
