 package pl.byd.promand.Team1;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Kasia
  * Date: 14.03.13
  * Time: 13:34
  * To change this template use File | Settings | File Templates.
  */
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.app.*;
 import android.widget.ListView;
 import com.promand.Team1.R;
 
 public class SettingsActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.options);
 
       Button newImage = (Button) findViewById(R.id.bNewImage);
       Button saveImage = (Button) findViewById(R.id.bSaveImage);
       Button loadImage = (Button) findViewById(R.id.bLoadImage);
       Button cameraShot = (Button) findViewById(R.id.bCameraShot);
       Button share = (Button) findViewById(R.id.bShare);
       Button back = (Button) findViewById(R.id.bBack);
       final Context context = this;
 
       newImage.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
 
               final Dialog dialog = new Dialog(context);
               dialog.setContentView(R.layout.new_image);
               dialog.setTitle("Choose the folder");
 
               Button no = (Button) dialog.findViewById(R.id.bNo);
               Button yes = (Button) dialog.findViewById(R.id.bYes);
 
               no.setOnClickListener(new View.OnClickListener() {
 
                   public void onClick(View v) {
                       dialog.dismiss();
                   }
               });
 
               yes.setOnClickListener(new View.OnClickListener(){
                   public void onClick(View v) {
                       //*********************************
                       //New image
                       //**********************************
                   }
 
               });
               dialog.show();
           }
       });
 
 
       saveImage.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 
                 final Dialog dialog = new Dialog(context);
                 dialog.setContentView(R.layout.save);
                 dialog.setTitle("Choose the folder");
 
                 ListView folderSave = (ListView) dialog.findViewById(R.id.lFolderSave);
                 Button save = (Button) dialog.findViewById(R.id.bSave);
                Button exit = (Button) dialog.findViewById(R.id.bExit);
 
                 exit.setOnClickListener(new View.OnClickListener() {
 
                     public void onClick(View v) {
                         dialog.dismiss();
                     }
                 });
 
                 save.setOnClickListener(new View.OnClickListener(){
                     public void onClick(View v) {
                        //*********************************
                        //Saving image
                        //**********************************
                     }
 
                 });
             dialog.show();
             }
         });
 
 
         loadImage.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 
                 final Dialog dialog = new Dialog(context);
                 dialog.setContentView(R.layout.load);
                 dialog.setTitle("Choose the folder");
 
                 ListView folderLoad = (ListView) dialog.findViewById(R.id.lFolderLoad);
                 Button load = (Button) dialog.findViewById(R.id.bLoad);
                 Button exit = (Button) dialog.findViewById(R.id.bExit1);
 
                 exit.setOnClickListener(new View.OnClickListener() {
 
                     public void onClick(View v) {
                         dialog.dismiss();
                     }
                 });
 
                 load.setOnClickListener(new View.OnClickListener(){
                     public void onClick(View v) {
                         //*********************************
                         //loading image
                         //**********************************
                     }
 
                 });
             dialog.show();
             }
         });
 
         cameraShot.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 
 

             }
         });
 
 
         share.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 
 
 
             }
         });
 
 
 
         back.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {



             }
         });
 
 
 
 
     }
 }
