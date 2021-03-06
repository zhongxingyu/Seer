 package hs.mediasystem.beans;
 
 import hs.mediasystem.util.ImageCache;
 import hs.mediasystem.util.ImageHandle;
 import hs.subtitle.DefaultThreadFactory;
 
 import java.io.ByteArrayInputStream;
 import java.util.concurrent.Executor;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javafx.application.Platform;
 import javafx.beans.property.ObjectProperty;
 import javafx.beans.property.SimpleObjectProperty;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.scene.image.Image;
 
 public class AsyncImageProperty extends SimpleObjectProperty<Image> {
   private static final ThreadPoolExecutor SLOW_EXECUTOR = new ThreadPoolExecutor(2, 2, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory("AsyncImageProperty[Slow]", true));
   private static final ThreadPoolExecutor FAST_EXECUTOR = new ThreadPoolExecutor(3, 3, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory("AsyncImageProperty[Fast]", true));
 
   private static final Image NULL_IMAGE = new Image(new ByteArrayInputStream(new byte[0]));
 
   static {
     SLOW_EXECUTOR.allowCoreThreadTimeOut(true);
     FAST_EXECUTOR.allowCoreThreadTimeOut(true);
   }
 
   private final ObjectProperty<ImageHandle> imageHandle = new SimpleObjectProperty<>();
 
   private boolean taskQueued;
 
   public AsyncImageProperty() {
    set(NULL_IMAGE);  // WORKAROUND for JavaFX Jira Issue RT-23974; should be null, but that causes an Exception in ImageView code

     imageHandle.addListener(new ChangeListener<ImageHandle>() {
       @Override
       public void changed(ObservableValue<? extends ImageHandle> observable, ImageHandle oldValue, ImageHandle value) {
         loadImageInBackground(imageHandle.getValue());
       }
     });
   }
 
   public ObjectProperty<ImageHandle> imageHandleProperty() {
     return imageHandle;
   }
 
   private void loadImageInBackground(final ImageHandle imageHandle) {
     set(NULL_IMAGE);  // WORKAROUND for JavaFX Jira Issue RT-23974; should be null, but that causes an Exception in ImageView code
 
     synchronized(FAST_EXECUTOR) {
       if(!taskQueued && imageHandle != null) {
         Executor chosenExecutor = imageHandle.isFastSource() ? FAST_EXECUTOR : SLOW_EXECUTOR;
 
         chosenExecutor.execute(new Runnable() {
           @Override
           public void run() {
             try {
              Image image = NULL_IMAGE;
 
               try {
                 image = ImageCache.loadImageUptoMaxSize(imageHandle, 1920, 1200);
               }
               catch(Exception e) {
                 System.out.println("[WARN] AsyncImageProperty - Exception while loading " + imageHandle + " in background: " + e);
                 e.printStackTrace(System.out);
               }
 
               final Image finalImage = image;
 
               Platform.runLater(new Runnable() {
                 @Override
                 public void run() {
                   set(finalImage);
 
                   ImageHandle handle = AsyncImageProperty.this.imageHandle.get();
 
                   if(handle == null || !handle.equals(imageHandle)) {
                     loadImageInBackground(handle);
                   }
                 }
               });
             }
             finally {
               synchronized(FAST_EXECUTOR) {
                 taskQueued = false;
               }
             }
           }
         });
 
         taskQueued = true;
       }
     }
   }
 }
