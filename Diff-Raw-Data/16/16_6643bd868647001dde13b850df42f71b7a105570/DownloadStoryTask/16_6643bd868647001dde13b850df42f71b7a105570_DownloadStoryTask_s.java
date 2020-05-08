 
 package ualberta.g12.adventurecreator.tasks;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.widget.Toast;
 
 import ualberta.g12.adventurecreator.controllers.StoryListController;
 import ualberta.g12.adventurecreator.data.AdventureCreator;
 import ualberta.g12.adventurecreator.data.OfflineIOHelper;
 import ualberta.g12.adventurecreator.data.Story;
 
 public class DownloadStoryTask extends AsyncTask<Story, Void, String> {
 
     private Context context;
     private Story s;
 
     public DownloadStoryTask(Context c) {
         this.context = c;
     }
 
     @Override
     protected String doInBackground(Story... story) {
 
         // TODO: Actually download the story
        s = story[0];
 
        if (true) {
             return String.format("%s Download complete", story[0].getTitle());
         } else {
             s = null;
             return String.format("%s Download failed", story[0].getTitle());
         }
 
     }
 
     @Override
     protected void onPostExecute(String result) {
         /*
          * This has to be in onPostExecute because it ends up calling Update in
          * Other activities which means that we're going to be teling a ListView
          * adapter to change, and UI changes can only be done on the main UI
          * Thread.
          */
         if (s != null) {
             StoryListController slc = AdventureCreator.getStoryListController();
             slc.addStory(s);
 
             OfflineIOHelper offlineHelper = AdventureCreator.getOfflineIOHelper();
             offlineHelper.saveOfflineStories(AdventureCreator.getStoryList());
         }
         Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
         super.onPostExecute(result);
     }
 
 }
