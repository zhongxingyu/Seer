 package fr.esipe.game.escapeir;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class Builder extends Activity {
  
  private Bitmap yourSelectedImage=null;
  private static final int SELECT_PHOTO = 100;
 @Override 
 protected void onCreate(Bundle savedInstanceState) {
 
 	 super.onCreate(savedInstanceState);
 		setContentView(R.layout.menu_builder);
 		OnClickListener ocl = new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				switch (v.getId()) {
 				case R.id.button1:
 					Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
 					intent.setType("image/*");
 					//intent.setAction(Intent.ACTION_GET_CONTENT);//
 					startActivityForResult(intent,SELECT_PHOTO);
 					break;
 				case R.id.button2:
 					Mysave();break;
 
 				default:
 					break;
 				}
 				
 			}
 		};
 		findViewById(R.id.button1).setOnClickListener(ocl);
 findViewById(R.id.button2).setOnClickListener(ocl);
 
 }
 private void Mysave(){
 	EditText name = (EditText) findViewById(R.id.editText1);
 	EditText time = (EditText) findViewById(R.id.editText2);
 	ImageView iv = (ImageView) findViewById(R.id.imageView1);
 	View wrong= findViewById(R.id.textView4);
 	wrong.setVisibility(View.GONE);
 	if(yourSelectedImage==null|iv.getDrawable()==null|!(name.getText().toString().trim().length()>0)|!(time.getText().toString().trim().length()>0)){
 		wrong.setVisibility(View.VISIBLE);
 	}else{
 		File level = new File(getDir("level",Context.MODE_PRIVATE).getAbsolutePath() + File.separator + name.getText().toString());
 		if (!level.exists()) {
 			level.mkdir(); //On crée le répertoire (s'il n'existe pas!!)
 		}
 		//iv.setDrawingCacheEnabled(true);
 		//Bitmap bitmap = iv.getDrawingCache();
 		File img= new File(level.getAbsolutePath()+File.separator+"map.png");
 		try 
         {
 			img.createNewFile();
             FileOutputStream ostream = new FileOutputStream(img);
            yourSelectedImage.compress(CompressFormat.PNG, 100, ostream);
             ostream.close();
         } 
         catch (Exception e) 
         {
             e.printStackTrace();
         }
 		Intent i= new Intent(Builder.this, MapBuilder.class);
 		i.putExtra("mappath", level.getAbsolutePath());
 		startActivity(i);
		
 	}
 }
 @Override
 protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
     super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 
 
     switch(requestCode) { 
     case SELECT_PHOTO:
         if(resultCode == RESULT_OK){  
             Uri selectedImage = imageReturnedIntent.getData();
             InputStream imageStream;
 			try {
 				imageStream = getContentResolver().openInputStream(selectedImage);
 	             yourSelectedImage = BitmapFactory.decodeStream(imageStream);
 	            ((ImageView) findViewById(R.id.imageView1)).setImageBitmap(yourSelectedImage);
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
         }
     }
 }
 	@Override
 		public void onBackPressed() {
 			finish();
 		}
 }
