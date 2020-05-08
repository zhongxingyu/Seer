 package net.ildn;
 
 import net.ildn.fedorait.R;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.LinearGradient;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Shader.TileMode;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.Toast;
 
 public class Splash extends Activity {
 	
 	private String portaledefault = "";
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		 super.onCreate(savedInstanceState);
 		 SharedPreferences settings = getSharedPreferences(
 				this.getString(R.string.ildnPreference), 0);
 		 portaledefault = settings.getString("portaledefault",
 				"nessuno");
 		 
 		 if (!portaledefault.equalsIgnoreCase("nessuno")) {
 			 Intent mainIntent = new Intent("net.ildn.SexySplash");
 			 startActivity(mainIntent);
 			 finish();
 		 }
 		 
 		 setContentView(R.layout.cover);
 	     CoverFlow coverFlow = (CoverFlow)findViewById(R.id.coverflow);
 	     coverFlow.setAdapter(new ImageAdapter(this));
 	     ImageAdapter coverImageAdapter =  new ImageAdapter(this);
	     //coverImageAdapter.createReflectedImages();
 	     coverFlow.setAdapter(coverImageAdapter);
	     coverFlow.setSpacing(-45);
 	     coverFlow.setSelection(1, true);
 	     coverFlow.setAnimationDuration(1000);
 
 	     coverFlow.setOnItemClickListener(new OnItemClickListener() {
 	    	 public void onItemClick(AdapterView parent, View v, int position, long id) {
 	            Intent mainIntent = null;
 	            switch (position) {
 				case 5:
 					Toast.makeText(Splash.this, "Portale non ancora attivo", Toast.LENGTH_SHORT).show();
 					break;
 				case 3:
 					mainIntent = new Intent(getString(R.string.portalemandriva));
 					startActivity(mainIntent);
 					finish();
 					break;
 				case 0:
 					mainIntent = new Intent(getString(R.string.portaledebian));
 					startActivity(mainIntent);
 					finish();
 					break;
 				case 1:
 					mainIntent = new Intent(getString(R.string.portalefedora));
 					startActivity(mainIntent);
 					finish();
 					break;
 				case 4:
 					Toast.makeText(Splash.this, "Portale non ancora attivo", Toast.LENGTH_SHORT).show();
 					break;
 				case 2:
 					mainIntent = new Intent(getString(R.string.portalesuse));
 					startActivity(mainIntent);
 					finish();
 					break;
 				default:
 					mainIntent = new Intent(getString(R.string.portalefedora));
 					startActivity(mainIntent);
 					finish();
 					break;
 				} 
 	         }
 		});
 	}
 	
 	 public class ImageAdapter extends BaseAdapter {
 	     int mGalleryItemBackground;
 	     private Context mContext;
 	        
 	     private Integer[] mImageIds = {
 	             R.drawable.debian,
 	             R.drawable.fedora,
 	             R.drawable.suse,
 	             R.drawable.mandriva,
 	             R.drawable.gentoo,
 	    		 R.drawable.slack
 	     };
 	
 	     private ImageView[] mImages;
 	     
 	     public ImageAdapter(Context c) {
 	      mContext = c;
 	      mImages = new ImageView[mImageIds.length];
 	     }
 	  public boolean createReflectedImages() {
 	          //The gap we want between the reflection and the original image
 	          final int reflectionGap = 4;
 	          
 	          
 	          int index = 0;
 	          for (int imageId : mImageIds) {
 	        Bitmap originalImage = BitmapFactory.decodeResource(getResources(), 
 	          imageId);
 	           int width = originalImage.getWidth();
 	           int height = originalImage.getHeight();
 	           
 	     
 	           //This will not scale but will flip on the Y axis
 	           Matrix matrix = new Matrix();
 	           matrix.preScale(1, -1);
 	           
 	           //Create a Bitmap with the flip matrix applied to it.
 	           //We only want the bottom half of the image
 	           Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height/2, width, height/2, matrix, false);
 	           
 	               
 	           //Create a new bitmap with same width but taller to fit reflection
 	           Bitmap bitmapWithReflection = Bitmap.createBitmap(width 
 	             , (height + height/2), Config.ARGB_8888);
 	         
 	          //Create a new Canvas with the bitmap that's big enough for
 	          //the image plus gap plus reflection
 	          Canvas canvas = new Canvas(bitmapWithReflection);
 	          //Draw in the original image
 	          canvas.drawBitmap(originalImage, 0, 0, null);
 	          //Draw in the gap
 	          Paint deafaultPaint = new Paint();
 	          canvas.drawRect(0, height, width, height + reflectionGap, deafaultPaint);
 	          //Draw in the reflection
 	          canvas.drawBitmap(reflectionImage,0, height + reflectionGap, null);
 	          
 	          //Create a shader that is a linear gradient that covers the reflection
 	          Paint paint = new Paint(); 
 	          LinearGradient shader = new LinearGradient(0, originalImage.getHeight(), 0, 
 	            bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff, 
 	            TileMode.CLAMP); 
 	          //Set the paint to use this shader (linear gradient)
 	          paint.setShader(shader); 
 	          //Set the Transfer mode to be porter duff and destination in
 	          paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN)); 
 	          //Draw a rectangle using the paint with our linear gradient
 	          canvas.drawRect(0, height, width, 
 	            bitmapWithReflection.getHeight() + reflectionGap, paint); 
 	          
 	          ImageView imageView = new ImageView(mContext);
 	          imageView.setImageBitmap(bitmapWithReflection);
 	          imageView.setLayoutParams(new CoverFlow.LayoutParams(120, 180));
 	          imageView.setScaleType(ScaleType.MATRIX);
 	          mImages[index++] = imageView;
 	          
 	          }
 	       return true;
 	  }
 	
 	     public int getCount() {
 	         return mImageIds.length;
 	     }
 	
 	     public Object getItem(int position) {
 	         return position;
 	     }
 	
 	     public long getItemId(int position) {
 	         return position;
 	     }
 	
 	     public View getView(int position, View convertView, ViewGroup parent) {
	
 	      //Use this code if you want to load from resources
 	         ImageView i = new ImageView(mContext);
 	         i.setImageResource(mImageIds[position]);
 	         i.setLayoutParams(new CoverFlow.LayoutParams(130, 130));
 	         i.setScaleType(ImageView.ScaleType.CENTER_INSIDE); 
 	         
 	         //Make sure we set anti-aliasing otherwise we get jaggies
 	         BitmapDrawable drawable = (BitmapDrawable) i.getDrawable();
 	         drawable.setAntiAlias(true);
 	         return i;
	      
	      //return mImages[position];
 	     }
 	   /** Returns the size (0.0f to 1.0f) of the views 
 	      * depending on the 'offset' to the center. */ 
 	      public float getScale(boolean focused, int offset) { 
 	        /* Formula: 1 / (2 ^ offset) */ 
 	          return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset))); 
 	      } 
 	
 	 }
 
 
 }
