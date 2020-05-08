 package fi.testbed2.task;
 
 import fi.testbed2.R;
 import fi.testbed2.activity.AnimationActivity;
 import fi.testbed2.app.MainApplication;
 import fi.testbed2.data.TestbedMapImage;
 import fi.testbed2.exception.DownloadTaskException;
 import fi.testbed2.result.DownloadImagesTaskResult;
 import fi.testbed2.result.TaskResultType;
 
 import java.util.List;
 
 /**
  * Task which downloads all map images and reloads the animation view.
  */
 public class DownloadImagesTask extends AbstractTask<DownloadImagesTaskResult> {
 
     AnimationActivity activity;
 
 	public DownloadImagesTask(final AnimationActivity activity) {
         super(activity);
         this.activity = activity;
 	}
 
     @Override
     protected void onTaskEnd() {
         activity.onAllImagesDownloaded();
     }
 
     @Override
 	protected DownloadImagesTaskResult doInBackground(Void... params) {
 
         try {
             return processImages();
         } catch(DownloadTaskException e) {
             return new DownloadImagesTaskResult(TaskResultType.ERROR, e.getMessage());
         }
 
     }
 
     private DownloadImagesTaskResult processImages() throws DownloadTaskException {
 
         List<TestbedMapImage> testbedMapImages = MainApplication.getTestbedParsedPage().getAllTestbedImages();
         int totalImagesNotDownloaded = MainApplication.getTestbedParsedPage().getNotDownloadedCount();
         int i= 1;
         for(TestbedMapImage image : testbedMapImages) {
 
             if (!image.hasBitmapDataDownloaded()) {
                 String progressText = activity.getString(R.string.progress_anim_downloading,
                         i, totalImagesNotDownloaded);
                 this.activity.updateDownloadProgressInfo(progressText);
             }
 
             if (isAbort()) {
                 doCancel();
                 return null;
             }
             image.downloadAndCacheImage();
            i++;
 
         }
 
         return new DownloadImagesTaskResult(TaskResultType.OK, "All images downloaded");
 
     }
 
     @Override
     protected void saveResultToApplication(DownloadImagesTaskResult result) {
         // No need to save result, task just loads the TestbedMapImages already saved to the application
     }
 }
