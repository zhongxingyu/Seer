 package com.justcloud.dynamy.deployer;
 
 import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
 import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
 import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
 
 import java.io.IOException;
 import java.nio.file.FileSystems;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.WatchEvent;
 import java.nio.file.WatchEvent.Kind;
 import java.nio.file.WatchKey;
 import java.nio.file.WatchService;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Logger;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.launch.Framework;
 import org.osgi.framework.wiring.BundleRevision;
 
 public class AutoDeployService {
 
 	private final Logger logger = Logger.getLogger(AutoDeployService.class
 			.getName());
 	private final ExecutorService executor;
 
 	private final Path path;
 	private final WatchKey key;
 	private final WatchService watcher;
 	private final Framework framework;
 	private final Map<String, Bundle> bundles;
 	private boolean cancel;
 
 	public AutoDeployService(Framework f, String stringPath) throws IOException {
 		framework = f;
 		path = Paths.get(stringPath);
 		watcher = FileSystems.getDefault().newWatchService();
 		key = path.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
 		executor = Executors.newCachedThreadPool();
 		cancel = false;
 		bundles = new HashMap<>();
 		startup();
 	}
 
 	private void startup() throws IOException {
 
 		installCurrent();
 		
 		executor.submit(new Runnable() {
 
 			@Override
 			public void run() {
 				while (!cancel) {
 					for (WatchEvent<?> evt : key.pollEvents()) {
 						@SuppressWarnings("unchecked")
 						WatchEvent<Path> event = (WatchEvent<Path>) evt;
 						Kind<Path> kind = event.kind();
 						Path name = event.context();
 						Path child = path.resolve(name);
 						
 						logger.fine("Found a path " + child);
 
 						if (kind == ENTRY_CREATE) {
 							try {
 								processInstall(child);
 							} catch (BundleException e) {
 								logger.warning(e.getMessage());
 							}
 						} else if(kind == ENTRY_MODIFY) {
 							try {
 								processUpdate(child);
 							} catch (BundleException e) {
 								logger.warning(e.getMessage());
 							}
 						} else if(kind == ENTRY_DELETE) {
 							try {
 								processDelete(child);
 							} catch (BundleException e) {
 								logger.warning(e.getMessage());
 							}
 						}
 					}
 				}
 			}
 
 		});
 
 	}
 
 	private void installCurrent() throws IOException {
		final List<Bundle> startBundles = new ArrayList<>();

 		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
 			@Override
 			public FileVisitResult visitFile(Path file,
 					BasicFileAttributes attrs) throws IOException {
 				if(attrs.isDirectory()) {
 					return FileVisitResult.SKIP_SUBTREE;
 				}
 				
 				try {
					BundleContext context = framework.getBundleContext();
					Bundle bundle = context.installBundle("file://" + file.toAbsolutePath().toString());
					bundles.put(file.toAbsolutePath().toString(), bundle);
					logger.info("Installed the bundle " + bundle);
					BundleRevision revision = (BundleRevision)bundle.adapt(BundleRevision.class);
					if ((revision.getTypes() & BundleRevision.TYPE_FRAGMENT) == 0) {
						startBundles.add(bundle);
					}
 				} catch (BundleException e) {
 					logger.warning(e.getMessage());
 				}
 				
 				return FileVisitResult.CONTINUE;
 			}
 		});

		logger.info("Starting bundles");
		for(Bundle bundle : startBundles) {
			try {
				bundle.start();
				logger.info("Started bundle " + bundle);
			} catch(BundleException e) {
				logger.warning(e.getMessage());
			}
		}

 	}
 
 	private void processInstall(Path child) throws BundleException {
 		if(bundles.containsKey(child.toAbsolutePath().toString())) {
 			return;
 		}
 		logger.fine("Installing the bundle " + child.toAbsolutePath());
 		BundleContext context = framework.getBundleContext();
 		Bundle bundle = context.installBundle("file://" + child.toAbsolutePath().toString());
 		bundles.put(child.toAbsolutePath().toString(), bundle);
 		logger.info("Installed the bundle " + bundle);
 		BundleRevision revision = (BundleRevision)bundle.adapt(BundleRevision.class);
 		if ((revision.getTypes() & BundleRevision.TYPE_FRAGMENT) == 0) {
 			logger.fine("Starting the bundle " + bundle);
 			bundle.start();
 			logger.info("Bundle started");
 		}
 	}
 	
 	private void processUpdate(Path child) throws BundleException {
 		Bundle bundle = bundles.get(child.toAbsolutePath().toString());
 		bundle.update();
 	}
 	
 	private void processDelete(Path child) throws BundleException {
 		Bundle bundle = bundles.get(child.toAbsolutePath().toString());
 		bundle.uninstall();
 	}
 
 	public void stop() {
 		cancel = true;
 		executor.shutdown();
 	}
 
 }
