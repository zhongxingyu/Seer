 package org.utn.proyecto.helpful.integrart.integrar_t_android.activities.conociendoacali;
 
 import org.utn.proyecto.helpful.integrart.integrar_t_android.R;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.domain.User;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.services.DataStorageService;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.utils.GiftCount;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.utils.GiftPopup;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.ContentView;
 import roboguice.inject.InjectView;
 import android.app.AlertDialog;
 import android.app.Dialog;
import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Gallery;
 import android.widget.ImageView;
 
 import com.google.inject.Inject;
 
 @ContentView(R.layout.cac_selecciondepersonaje)
 public class ConociendoACaliActivity extends RoboActivity {
 
     @Inject
     private DataStorageService db;
     
     @Inject
     private User user;
     
     @InjectView(R.id.cac_picture)
     private ImageView imagen;
     
    @InjectView(R.id.cac_gallery)
     private Gallery galeria;
     
     private PictureAdapter adaptadorDeImagen;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         adaptadorDeImagen = new PictureAdapter(this);
         
         galeria.setAdapter(adaptadorDeImagen);
         
         setCarrusel();
         
       //set long click listener for each gallery thumbnail item
              galeria.setOnItemLongClickListener(new OnItemLongClickListener() {
             //handle long clicks
             @Override
             public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                 /*
                 //take user to choose an image
                 //update the currently selected position so that we assign the imported bitmap to correct item
                 currentPic = position;
                 //take the user to their chosen image selection app (gallery or file manager)
                 Intent pickIntent = new Intent();
                 pickIntent.setType("image/*");
                 pickIntent.setAction(Intent.ACTION_GET_CONTENT);
                 //we will handle the returned data in onActivityResult
                 startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), PICKER);
                 */
                 
                 showDialog(position);
                 
                 return true;
             }
         });
              
         galeria.setOnItemClickListener(new OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               //set the larger image view to display the chosen bitmap calling method of adapter class
                 imagen.setImageResource(adaptadorDeImagen.getPic(position));
             }
         });
         
         
     }
     
     @Override
     protected Dialog onCreateDialog(final int pos){
         Dialog dialog;
         AlertDialog.Builder builder;
         
         builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.cac_mensaje)
             .setCancelable(false)
             .setPositiveButton(R.string.cac_si, new DialogInterface.OnClickListener() {
                 
                 @Override
                 public void onClick(DialogInterface arg, int which) {
                     // TODO Auto-generated method stub
                     writeOnPreferenceCharacter(pos);
                     showGifts(GiftCount.THREE, new DialogInterface.OnDismissListener() {
 						@Override
 						public void onDismiss(DialogInterface dialog) {
 							finish();
 						}
 					});
                 }
             });
         builder.setTitle(R.string.cac_titulo);
         builder.setNegativeButton(R.string.cac_no, new DialogInterface.OnClickListener() {
             
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 showGifts(GiftCount.ONE);
             }
         });
         
         dialog = builder.create();
         return dialog;
     }
     
     private void showGifts(GiftCount value){
     	showGifts(value, null);
     }
     
     private void showGifts(GiftCount value, DialogInterface.OnDismissListener listener){
         user.addGifts(value==GiftCount.THREE ? 3 : 1);
         db.put("currentUser", user);
         Dialog dialog = new GiftPopup(this, user.getGifts(),value);
         if(listener!=null) dialog.setOnDismissListener(listener);
         dialog.show();
     }
     
     public void setCarrusel(){
         //TODO double check how to move the carrousel to the selected character;
         int id;
         //SharedPreferences prefs = this.getSharedPreferences("cac_personaje", Context.MODE_PRIVATE);
         
        if(db.contain(user.getUserName()+".cac_personaje_seleccionado"))
        {
            id = db.get(user.getUserName()+ ".cac_personaje_seleccionado",int.class);
            adaptadorDeImagen.moveCarruselPosition(id);
        }
     }
     
     public void writeOnPreferenceCharacter(int position)
     {
         //Write on preferences
         
         //SharedPreferences prefs = this.getSharedPreferences("cac_personaje", Context.MODE_PRIVATE);
         //prefs.edit().putInt("cac_personaje_seleccionado", adaptadorDeImagen.getDrawbleResourceSelected()).commit();
         
         db.put(user.getUserName() + ".cac_personaje_seleccionado" , adaptadorDeImagen.getDrawbleResourceSelected());
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == RESULT_OK) {
             //check if we are returning from picture selection
             /*if (requestCode == PICKER) {
                     //import the image
                  
                 //the returned picture URI
                 Uri pickedUri = data.getData();
 
                 //declare the bitmap
                 Bitmap pic = null;
                 //declare the path string
                 String imgPath = "";
 
                 //retrieve the string using media data
                 String[] medData = { MediaStore.Images.Media.DATA };
                 //query the data
                 Cursor picCursor = managedQuery(pickedUri, medData, null, null, null);
                 if(picCursor!=null)
                 {
                     //get the path string
                     int index = picCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                     picCursor.moveToFirst();
                     imgPath = picCursor.getString(index);
                 }
                 else
                     imgPath = pickedUri.getPath();
                 
                 //if and else handle both choosing from gallery and from file manager
 
                 //if we have a new URI attempt to decode the image bitmap
                 if(pickedUri!=null) {
 
                     //set the width and height we want to use as maximum display
                     int targetWidth = 600;
                     int targetHeight = 400;
                     
                     //sample the incoming image to save on memory resources
                     
                     //create bitmap options to calculate and use sample size
                     BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
                     
                     //first decode image dimensions only - not the image bitmap itself
                     bmpOptions.inJustDecodeBounds = true;
                     BitmapFactory.decodeFile(imgPath, bmpOptions);
 
                     //work out what the sample size should be
 
                     //image width and height before sampling
                     int currHeight = bmpOptions.outHeight;
                     int currWidth = bmpOptions.outWidth;
                     
                     //variable to store new sample size
                     int sampleSize = 1;
 
                     //calculate the sample size if the existing size is larger than target size
                     if (currHeight>targetHeight || currWidth>targetWidth) 
                     {
                         //use either width or height
                         if (currWidth>currHeight)
                             sampleSize = Math.round((float)currHeight/(float)targetHeight);
                         else 
                             sampleSize = Math.round((float)currWidth/(float)targetWidth);
                     }
                     //use the new sample size
                     bmpOptions.inSampleSize = sampleSize;
 
                     //now decode the bitmap using sample options
                     bmpOptions.inJustDecodeBounds = false;
                     
                     //get the file as a bitmap
                     pic = BitmapFactory.decodeFile(imgPath, bmpOptions);
                     
                     //pass bitmap to ImageAdapter to add to array
                     imgAdapt.addPic(pic);
                     //redraw the gallery thumbnails to reflect the new addition
                     picGallery.setAdapter(imgAdapt);
                     
                     //display the newly selected image at larger size
                     picView.setImageBitmap(pic);
                     //scale options
                     picView.setScaleType(ImageView.ScaleType.FIT_CENTER);
             }*/
         }
         //superclass method
         super.onActivityResult(requestCode, resultCode, data);
     }
 
 }
