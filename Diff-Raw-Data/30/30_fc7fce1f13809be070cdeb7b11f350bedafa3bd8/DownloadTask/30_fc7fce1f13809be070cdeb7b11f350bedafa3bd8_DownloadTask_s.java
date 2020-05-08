 package no.hials.muldvarp.v2.utility;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.support.v4.content.LocalBroadcastManager;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.hials.muldvarp.v2.MuldvarpService;
 import no.hials.muldvarp.v2.database.MuldvarpDataSource;
 import no.hials.muldvarp.v2.domain.Course;
 import no.hials.muldvarp.v2.domain.Domain;
 import no.hials.muldvarp.v2.domain.Programme;
 import org.json.JSONException;
 
 /**
  *
  * @author mikael
  */
 public class DownloadTask extends AsyncTask<String, Void, Boolean> {
     Intent intent;
     Context ctx;
     MuldvarpService.DataTypes type;
     MuldvarpDataSource mds;
 
     public DownloadTask(Context ctx, Intent intent, MuldvarpService.DataTypes type) {
         this.intent = intent;
         this.ctx = ctx;
         this.type = type;
         mds = new MuldvarpDataSource(ctx);
     }
 
     @Override
     protected Boolean doInBackground(String... params) {
         mds.open();
         try {
             String json = JSONUtilities.getData(params[0]);
             List<Domain> d = JSONUtilities.JSONtoList(json, type);
             switch(type) {
                 case COURSES:
                     for(int i = 0; i < d.size(); i++) {
                         mds.insertCourse((Course)d.get(i));
                     }
                     break;
                 case PROGRAMS:
//                    List<Domain> oldProgs = mds.getAllProgrammes();
//                    for(int i = 0; i < oldProgs.size(); i++) {
//                        Domain oldProg = oldProgs.get(i);
//                        if(!d.contains(oldProg)) {
//                            mds.deleteProgramme((Programme)oldProg);
//                        }
//                    }
                     for(int i = 0; i < d.size(); i++) {
                         mds.insertProgramme((Programme)d.get(i));
                     }
                     break;
             }
             mds.close();
             return true;
         } catch (JSONException ex) {
             Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
         }
         mds.close();
         return false;
     }
 
     @Override
     protected void onPostExecute(Boolean retVal) {
         if(retVal) {
             LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
         }
     }
 
     public static List<Domain> compareLists(List<Domain> list1, List<Domain> list2) {
         Set<Domain> intersect = new HashSet<Domain>(list1);
         intersect.retainAll(list2);
         return null;
     }
 }
