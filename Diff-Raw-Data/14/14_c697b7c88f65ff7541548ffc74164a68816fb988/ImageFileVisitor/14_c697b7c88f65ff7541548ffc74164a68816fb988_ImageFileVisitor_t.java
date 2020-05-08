 package at.subera.memento.indexer;
 
 import java.io.IOException;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Path;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.WatchEvent;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.List;
 
 import at.subera.fs.indexer.index.listener.IndexListenable;
 import at.subera.fs.indexer.index.visitor.Filter;
 import at.subera.fs.indexer.watchdog.listener.Watchable;
 import org.apache.log4j.Logger;
 
 import at.subera.memento.rest.bean.Image;
 import at.subera.memento.rest.service.ImageService;
 
 import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
 import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
 import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
 
 /**
  * ImageFileVisitor
  * 
  * checks Files if they are Images and add them into the imageService
  */
 public class ImageFileVisitor extends SimpleFileVisitor<Path> implements IndexListenable, Watchable {
 	protected ImageService imageService;
 	
 	protected Filter filter = new Filter();
 
 	private static final Logger logger = Logger
 			.getLogger(ImageFileVisitor.class);
 
 	public ImageFileVisitor() {
 	}
 
 	public void setImageService(ImageService imageService) {
 		this.imageService = imageService;
 	}
 
 	@Override
 	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
 			throws IOException {
 		if (!filter.matchPath(file)) {
 			return FileVisitResult.CONTINUE;
 		}
 
 		if (logger.isInfoEnabled()) {
 			logger.info("Working on File: " + file.toString());
 		}
 
 		imageService.add(file);
 
 		return FileVisitResult.CONTINUE;
 	}
 
 	@Override
 	public void preIndex(String directory) {}
 
 	@Override
 	public void postIndex(String directory) {
 		if (logger.isInfoEnabled()) {
 			List<Image> images = imageService.get();
 			logger.info("Collect End: Found " + images.size() + "images");
 		}
 	}
 
     @Override
     public void visitFile(Path child, WatchEvent.Kind<?> kind) {
         if (kind == ENTRY_DELETE) {
             imageService.remove(child);
 //            albumService.remove(child);
             return;
         }
         if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
             imageService.add(child);
//            return;
         }
     }

    @Override
    public void visitDirectory(Path child, WatchEvent.Kind<?> kind) {
        // do nothing
    }
 }
