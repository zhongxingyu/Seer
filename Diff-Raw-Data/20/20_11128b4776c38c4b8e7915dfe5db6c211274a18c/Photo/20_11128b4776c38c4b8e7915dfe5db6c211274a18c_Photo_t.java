 package com.github.groupENIGMA.journalEgocentrique.model;
 
 import java.io.File;
 import java.io.FileOutputStream;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Environment;
 import com.github.groupENIGMA.journalEgocentrique.AppConstants;
 
 
 public class Photo implements PhotoInterface {
     
     private String path;
     private String thumb_path;
     
     public Photo(String path){
         this.path = path;
         thumb_path = createThumb();
     }
 
     /**
      * Create the thumbnail of the Photo
      * This method is called when it's created a new Photo object
      * 
      * @return the path of thumbnail of this one
      */
     private String createThumb(){
        if(getPath() != null){
    	Bitmap img = BitmapFactory.decodeFile(this.getPath());
         Bitmap thumb = Bitmap.createScaledBitmap(img, 85, 85, false);
 
         //Gets the path and the directory name where the Photo is going to be saved
         String path = Environment.getExternalStorageDirectory().getAbsolutePath();
         File photoDir = new File(
                 path + File.separator + AppConstants.EXTERNAL_STORAGE_PHOTO_DIR
         );
         //Creates the file
         String fileName = getPath().substring(0, getPath().length() - 4) + "thumb.jpeg";
         File file = new File (fileName);
         //Delete if already exists
         if (file.exists ()) {
             file.delete ();
         }
         if (! photoDir.exists()){
             photoDir.mkdirs();
         }
         //Writes the file with the picture in the selected path
         try {
             file.createNewFile();
             FileOutputStream out = new FileOutputStream(file);
             thumb.compress(Bitmap.CompressFormat.JPEG, 90, out);
             out.flush();
             out.close();
 
         } catch (Exception e) {
                e.printStackTrace();
         }
         return file.getAbsolutePath();
        }
        return null;
     }
 
     @Override
     public boolean canBeUpdated() {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean canBeDeleted() {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public String getPath() {
         
         return path;
     }
 
     @Override
     public String getPathThumb() {
         // TODO Auto-generated method stub
         return thumb_path;
     }
 }
