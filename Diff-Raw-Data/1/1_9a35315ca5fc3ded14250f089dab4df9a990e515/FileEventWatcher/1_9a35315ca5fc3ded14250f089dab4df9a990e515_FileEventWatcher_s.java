 /**
  * Copyright 2014 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.log.api.nio;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.FileSystem;
 import java.nio.file.FileSystems;
 import java.nio.file.FileVisitResult;
 import java.nio.file.FileVisitor;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardWatchEventKinds;
 import java.nio.file.WatchEvent;
 import java.nio.file.WatchEvent.Kind;
 import java.nio.file.WatchKey;
 import java.nio.file.WatchService;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author xeraph
  */
 public class FileEventWatcher {
 	private final Logger slog = LoggerFactory.getLogger(FileEventWatcher.class);
 	private static final Kind<?>[] EVENTS = new Kind[] { StandardWatchEventKinds.ENTRY_CREATE,
 			StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE };
 
 	private final Pattern fileNamePattern;
 	private final boolean recursive;
 
 	private Matcher fileNameMatcher;
 	private String basePath;
 	private WatchService ws;
 	private FileSystem fs;
 	private Path root;
 
 	private Map<String, WatchItem> watchPaths = new HashMap<String, WatchItem>();
 	private Set<FileEventListener> listeners = new HashSet<FileEventListener>();
 
 	public FileEventWatcher(String basePath, Pattern fileNamePattern, boolean recursive) throws IOException {
 		this.basePath = basePath;
 		this.fileNamePattern = fileNamePattern;
 		this.recursive = recursive;
 		this.fs = FileSystems.getDefault();
 		this.root = fs.getPath(basePath);
 		this.ws = fs.newWatchService();
 
 		if (fileNamePattern != null)
 			fileNameMatcher = fileNamePattern.matcher("");
 		Files.walkFileTree(root, new DirectoryRegister());
 	}
 
 	public void poll(int millis) throws IOException {
 		// base directory is removed and can be regenerated
 		if (!watchPaths.containsKey(root.toFile().getAbsolutePath())) {
 			try {
 				ws.close();
 			} catch (IOException e) {
 			}
 
 			this.ws = fs.newWatchService();
 			Files.walkFileTree(root, new DirectoryRegister());
 		}
 
 		WatchKey wk = null;
 		try {
 			while ((wk = ws.poll(millis, TimeUnit.MILLISECONDS)) != null) {
 				for (WatchEvent<?> evt : wk.pollEvents()) {
 					if (slog.isDebugEnabled())
 						slog.debug("araqne-logapi-nio: watchable [{}] context [{}] kind [{}] valid [{}]",
 								new Object[] { wk.watchable(), evt.context(), evt.kind(), wk.isValid() });
 
 					Path p = fs.getPath(wk.watchable().toString(), evt.context().toString());
 					File f = p.toFile();
 
 					if (evt.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
 						if (Files.isDirectory(p)) {
 							try {
 								WatchKey newKey = p.register(ws, EVENTS);
 								watchPaths.put(f.getAbsolutePath(), new WatchItem(newKey));
 								slog.debug("araqne-logapi-nio: adding watch path [{}]", f.getAbsolutePath());
 							} catch (IOException e) {
 								slog.error("araqne-logapi-nio: failed to watching directory [{}]", f.getAbsolutePath());
 							}
 						} else {
 							if (slog.isDebugEnabled())
 								slog.debug("araqne-logapi-nio: path [{}] is not directory", f.getAbsolutePath());
 						}
 
 						if (isTargetFile(f)) {
 							WatchItem item = watchPaths.get(f.getParent());
 							item.files.add(f.getName());
 
 							for (FileEventListener listener : listeners) {
 								try {
 									listener.onCreate(f);
 								} catch (Throwable t) {
 									slog.warn("araqne-logapi-nio: file event listener should not throw any exception", t);
 								}
 							}
 						}
 					} else if (evt.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
 						if (slog.isDebugEnabled())
 							slog.debug("araqne-logapi-nio: modified path [{}]", f.getAbsolutePath());
 
 						if (isTargetFile(f)) {
 							for (FileEventListener listener : listeners) {
 								try {
 									listener.onModify(f);
 								} catch (Throwable t) {
 									slog.warn("araqne-logapi-nio: file event listener should not throw any exception", t);
 								}
 							}
 						}
 					} else if (evt.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
 						slog.debug("araqne-logapi-nio: checking remove target path [{}]", f.getAbsolutePath());
 
 						if (watchPaths.containsKey(f.getAbsolutePath())) {
 							invokeUnregisterRecursively(f.getAbsolutePath());
 						} else if (isTargetFile(f, true)) {
 							slog.debug("araqne-logapi-nio: checking remove target path [{}]", f.getAbsolutePath());
 							WatchItem item = watchPaths.get(f.getParent());
 							if (item != null) {
 								item.files.remove(f.getName());
 							} else {
 								slog.debug("araqne-logapi-nio: item not found for [{}]", f.getAbsolutePath());
 							}
 
 							for (FileEventListener listener : listeners) {
 								try {
 									listener.onDelete(f);
 								} catch (Throwable t) {
 									slog.warn("araqne-logapi-nio: file event listener should not throw any exception", t);
 								}
 							}
 						}
 					}
 				}
 
 				wk.reset();
 			}
 		} catch (InterruptedException e) {
 		}
 	}
 
 	private void invokeUnregisterRecursively(String path) {
 		Set<File> files = new HashSet<File>();
 
 		for (String dir : new ArrayList<String>(watchPaths.keySet())) {
 			if (dir.startsWith(path)) {
 				WatchItem item = watchPaths.remove(dir);
 				item.key.cancel();
 				slog.debug("araqne-logapi-nio: cancel watch path [{}]", dir);
 
 				for (String file : item.files) {
 					files.add(new File(dir, file));
 				}
 			}
 		}
 
 		for (File f : files) {
 			for (FileEventListener listener : listeners) {
 				try {
 					listener.onDelete(f);
 				} catch (Throwable t) {
 					slog.warn("araqne-logapi-nio: file event listener should not throw any exception", t);
 				}
 			}
 		}
 	}
 
 	private boolean isTargetFile(File f) {
 		return isTargetFile(f, false);
 	}
 
 	private boolean isTargetFile(File f, boolean noFileCheck) {
 		if (fileNameMatcher == null)
 			return noFileCheck || f.isFile();
 
 		fileNameMatcher.reset(f.getName());
 		return (noFileCheck || f.isFile()) && fileNameMatcher.matches();
 	}
 
 	public void addListener(FileEventListener listener) {
 		listeners.add(listener);
 	}
 
 	public void removeListener(FileEventListener listener) {
 		listeners.remove(listener);
 	}
 
 	public void close() {
 		try {
 			ws.close();
 		} catch (Throwable t) {
 		}
 
 		try {
 			fs.close();
 		} catch (Throwable t) {
 		}
 	}
 
 	private class DirectoryRegister implements FileVisitor<Path> {
 		@Override
 		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
 			return FileVisitResult.CONTINUE;
 		}
 
 		@Override
 		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
 			if (!recursive && !dir.equals(root))
 				return FileVisitResult.SKIP_SUBTREE;
 
 			WatchKey wk = dir.register(ws, EVENTS);
 			watchPaths.put(dir.toFile().getAbsolutePath(), new WatchItem(wk));
 
 			return FileVisitResult.CONTINUE;
 		}
 
 		@Override
 		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
 			File f = file.toFile();
 			if (isTargetFile(f)) {
 				WatchItem item = watchPaths.get(f.getParent());
 				item.files.add(f.getName());
 
 				for (FileEventListener listener : listeners) {
 					try {
 						listener.onCreate(f);
 					} catch (Throwable t) {
 						slog.warn("araqne-logapi-nio: file event listener should not throw any exception", t);
 					}
 				}
 			}
 
 			return FileVisitResult.CONTINUE;
 		}
 
 		@Override
 		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
 			return FileVisitResult.CONTINUE;
 		}
 	}
 
 	private class WatchItem {
 		private WatchKey key;
 		private Set<String> files = new HashSet<String>();
 
 		public WatchItem(WatchKey key) {
 			this.key = key;
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "file watcher: base path [" + basePath + "], recursive [" + recursive + "], file name pattern [" + fileNamePattern
 				+ "]";
 	}
 }
