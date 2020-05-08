 package com.kii.demo.cloudstorage.api;
 
 import java.util.List;
 
 import android.widget.Toast;
 
 import com.kii.cloud.storage.KiiClient;
 import com.kii.cloud.storage.KiiObject;
 import com.kii.cloud.storage.callback.KiiObjectCallBack;
 import com.kii.cloud.storage.query.KiiQuery;
 import com.kii.cloud.storage.query.KiiQueryResult;
 import com.kii.demo.cloudstorage.activities.ConstantValues;
 import com.kii.demo.cloudstorage.activities.ShowInfo;
 import com.kii.demo.cloudstorage.notepad.NoteItemListActivity;
 
 public class KiiNoteOperation {
 
     public final static String PROPERTY_TITLE = "title";
     public final static String PROPERTY_CONTENT = "content";
     
     public static final String KII_OBJECT_TYPE = "test_note2";
    
     public static List<KiiObject> mObjects;
     public static boolean isMore;
     private static KiiQuery mNextQuery;
 
     static {
        KiiClient.initialize(AppInfo.APP_ID, AppInfo.APP_KEY, AppInfo.BASE_URL);
     }
     
     private static String[] getTitles(){
         int length = mObjects.size();
         String[] res = new String[isMore?length + 1:length];
         for(int i = 0; i< length; i++){
             res[i] = mObjects.get(i).getString(PROPERTY_TITLE, "NO TITLE");
         }
         
         if(isMore){
             res[length] = "More...";
         }
         
         return res;
     }
 
     // ------------ Sync Methods ----------------
     public static void getAllNote(NoteItemListActivity activity) {
         try {
             KiiQueryResult<KiiObject> result = KiiObject.query(KII_OBJECT_TYPE, null);
             mObjects = result.getResult();
             isMore = false;
             activity.rebuildTitle(getTitles());
         } catch (Exception e) {
             ShowInfo.showException(activity, e);
         }
     }
 
     
     // --------------- Async Method -------------------------
     
     public static int asyncGetAllNote(final NoteItemListActivity activity) {
         int token = KiiObject.query(new KiiObjectCallBack(){
 
             @Override
             public void onQueryCompleted(int token, boolean success,
                     KiiQueryResult<KiiObject> objects, Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if(success){
                     mObjects = objects.getResult();
                     isMore = false;
                     activity.rebuildTitle(getTitles());
                 } else{
                     ShowInfo.showException(activity, exception);
                 }
             }
             
         }, KII_OBJECT_TYPE, null);
                 
 
         return token;
 
     }
     
     public static int asyncQueryNote(final NoteItemListActivity activity, KiiQuery query) {
         int token = KiiObject.query(new KiiObjectCallBack(){
 
             @Override
             public void onQueryCompleted(int token, boolean success,
                     KiiQueryResult<KiiObject> objects, Exception exception) {
                 ShowInfo.closeProgressDialog();
                 
                 if(success){
                     mObjects = objects.getResult();
                     isMore = objects.hasNext();
                     mNextQuery = objects.getNextKiiQuery();
                     activity.rebuildTitle(getTitles());
                     if(mObjects.size() == 0){
                         Toast.makeText(activity,
                                 "no result", Toast.LENGTH_SHORT).show();
                     }
                 } else{
                     ShowInfo.showException(activity, exception);
                 }
             }
             
         }, KII_OBJECT_TYPE, null);
 
         return token;
 
     }
     
     public static int asyncQueryMore(final NoteItemListActivity activity) {
         int token = KiiObject.query(new KiiObjectCallBack(){
 
             @Override
             public void onQueryCompleted(int token, boolean success,
                     KiiQueryResult<KiiObject> objects, Exception exception) {
                 ShowInfo.closeProgressDialog();
                 
                 if(success){
                     mObjects.addAll(objects.getResult());
                     isMore = objects.hasNext();
                     mNextQuery = objects.getNextKiiQuery();
                     activity.rebuildTitle(getTitles());
                     if(mObjects.size() == 0){
                         Toast.makeText(activity,
                                 "no result", Toast.LENGTH_SHORT).show();
                     }
                 } else{
                     ShowInfo.showException(activity, exception);
                 }
             }
             
         }, KII_OBJECT_TYPE, mNextQuery);
 
         return token;
         
     }
 
     public static int asyncUpdateNote(final NoteItemListActivity activity, int postion, String content) {
         KiiObject object = mObjects.get(postion);
         object.set(PROPERTY_CONTENT, content);
         
         int token = object.save(new KiiObjectCallBack(){
 
             @Override
             public void onSaveCompleted(int token, boolean success,
                     KiiObject object, Exception exception) {
                 ShowInfo.closeProgressDialog();
                 
                 if(success){
                     Toast.makeText(activity,
                             "Note saved", Toast.LENGTH_SHORT).show();
                 } else{
                     ShowInfo.showException(activity, exception);
                 }
             }
             
         });
 
         return token;
     }
 
     public static int asyncDeleteNote(final NoteItemListActivity activity, final int position) {
         
         KiiObject object = mObjects.get(position);
         int token = object.delete(new KiiObjectCallBack(){
 
             @Override
             public void onDeleteCompleted(int token, boolean success,
                     Exception exception) {
                 if(success){
                     if(position > -1){
                         mObjects.remove(position);
                         activity.rebuildTitle(getTitles());
                     }
                 }else{
                     ShowInfo.showException(activity, exception);
                 }
             }
             
         });
                 
         return token;
     }
 
     public static int asyncCreateNote(final NoteItemListActivity activity, String title, String content) {
 
         String creator = KiiClient.getCurrentUser().toUri().toString();
         KiiObject newobject = new KiiObject(KII_OBJECT_TYPE);
         newobject.set(PROPERTY_TITLE, title);
         newobject.set(PROPERTY_CONTENT, content);
         newobject.set(ConstantValues.FIELDNAME_CREATOR, creator);
         newobject.set("my_number", 3);
 
         int token = newobject.save(new KiiObjectCallBack(){
 
             @Override
             public void onSaveCompleted(int token, boolean success,
                     KiiObject object, Exception exception) {
                 ShowInfo.closeProgressDialog();
                 
                 if(success){
                     mObjects.add(0, object);
                     activity.rebuildTitle(getTitles());
                 } else{
                     ShowInfo.showException(activity, exception);
                 }
             }
             
         });
         
         return token;
 
     }
 }
