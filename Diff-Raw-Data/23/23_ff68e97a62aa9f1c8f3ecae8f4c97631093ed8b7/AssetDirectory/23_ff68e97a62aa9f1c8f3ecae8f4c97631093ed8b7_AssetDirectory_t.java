 package net.rptools.maptool.client.ui.assetpanel;
 
 import java.awt.Dimension;
 import java.awt.Transparency;
 import java.awt.image.BufferedImage;
 import java.beans.PropertyChangeEvent;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import net.rptools.lib.image.ThumbnailManager;
 import net.rptools.maptool.client.AppUtil;
 
 public class AssetDirectory extends Directory {
 
     public static final String PROPERTY_IMAGE_LOADED = "imageLoaded";
     
     // TODO: make this configurable
     private static final Dimension THUMBNAIL_SIZE = new Dimension (100, 100);
 
     private static ThumbnailManager thumbnailManager = new ThumbnailManager(AppUtil.getAppHome("imageThumbs"), THUMBNAIL_SIZE); 
     
	private Map<File, FutureTask<BufferedImage>> imageMap = new HashMap<File, FutureTask<BufferedImage>>();
 
 	private static final BufferedImage INVALID_IMAGE = new BufferedImage(1, 1, Transparency.OPAQUE);
 	
 	private static ExecutorService imageLoaderService = Executors.newFixedThreadPool(3);
     
     private AtomicBoolean continueProcessing = new AtomicBoolean(true);
     
 	public AssetDirectory(File directory, FilenameFilter fileFilter) {
 		super(directory, fileFilter);
 	}
 
 	@Override
 	public void refresh() {
 		imageMap.clear();
         
         // Tell any in-progress processing to stop
         AtomicBoolean oldBool = continueProcessing;
         continueProcessing = new AtomicBoolean(true);
         oldBool.set(false);
         
 		super.refresh();
 	}
 	
 	/**
 	 * Returns the asset associated with this file, or null if the
 	 * file has not yet been loaded as an asset
 	 * @param imageFile
 	 * @return
 	 */
 	public BufferedImage getImageFor(File imageFile) {
 
		FutureTask<BufferedImage> future = imageMap.get(imageFile);
 		if (future != null) {
 			if (future.isDone()) {
 				try {
 					return future.get() != INVALID_IMAGE ? future.get() : null;
 				} catch (InterruptedException e) {
                     // TODO: need to indicate a broken image
 					return null;
 				} catch (ExecutionException e) {
                     // TODO: need to indicate a broken image
 					return null;
 				} 
 			}
 			
 			// Not done loading yet, don't block
 			return null; 
 		}
 		
 		// load the asset in the background
		future = new FutureTask<BufferedImage>(new ImageLoader(imageFile)){
			@Override
			protected void done() {
	            firePropertyChangeEvent(new PropertyChangeEvent(AssetDirectory.this, PROPERTY_IMAGE_LOADED, false, true));
			}
		};
		imageLoaderService.execute(future);
 		imageMap.put(imageFile, future);
 		return null;
 	}
 	
 	@Override
 	protected Directory newDirectory(File directory, FilenameFilter fileFilter) {
 		return new AssetDirectory(directory, fileFilter);
 	}
 	
 	private class ImageLoader implements Callable<BufferedImage> {
 		
 		private File imageFile;
 		
 		public ImageLoader (File imageFile) {
 			this.imageFile = imageFile;
 		}
 		
 		public BufferedImage call() throws Exception {
 
             // Have we been orphaned ?
             if (!continueProcessing.get()) {
                 return null;
             }
             
             // Load it up
 			BufferedImage thumbnail = null;
 			try {
 				thumbnail = thumbnailManager.getThumbnail(imageFile);
 			} catch (Throwable t) {
                 t.printStackTrace();
 				thumbnail = INVALID_IMAGE;
 			}

 			return thumbnail;
 		}
		
		
 	}
 }
