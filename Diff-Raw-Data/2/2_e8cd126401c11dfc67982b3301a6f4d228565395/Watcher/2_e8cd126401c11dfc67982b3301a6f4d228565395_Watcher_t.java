 package io.skas.melbjvm.nio2;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.nio.file.*;
 
 /**
  * @author Szymon Szukalski [szymon.szukalski@gmail.com]
  */
 public class Watcher {
 
     private static final Logger LOG = LoggerFactory.getLogger(Watcher.class);
 
     public void watchDirectory(Path directory) throws IOException, InterruptedException {
 
         // New try with resources
         try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
 
             final WatchKey key = directory.register(
                     watchService,
                     StandardWatchEventKinds.ENTRY_CREATE,
                     StandardWatchEventKinds.ENTRY_MODIFY,
                     StandardWatchEventKinds.ENTRY_DELETE);
 
             while (true) {
 
                 // retrieve and remove the next watch key (waits)
                 watchService.take();
 
                 // get list of events for the key
                 for (WatchEvent<?> watchEvent : key.pollEvents()) {
 
                     // get the event kind
                     final WatchEvent.Kind<?> kind = watchEvent.kind();
 
                     // get the filename for the event
                     final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
                     final Path filename = watchEventPath.context();
 
                     LOG.debug("event: {} filename: {}", kind, filename);
 
                     // handle OVERFLOW event
                     if (kind == StandardWatchEventKinds.OVERFLOW) {
                         continue;
                     }
 
                     // handle CREATE event
                     if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        new AsynchronousFileReader(directory.resolve(filename));
                     }
                 }
 
                 // reset the key
                 boolean valid = key.reset();
 
                 // exit loop if the key is not valid (if the directory was deleted, for example)
                 if (!valid) {
                     break;
                 }
             }
         }
     }
 }
