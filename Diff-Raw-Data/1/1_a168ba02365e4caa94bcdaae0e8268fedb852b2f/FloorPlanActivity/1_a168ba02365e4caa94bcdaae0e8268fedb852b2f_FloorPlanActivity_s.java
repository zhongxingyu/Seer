 package drexel.dragonmap;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import drexel.dragonmap.R;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class FloorPlanActivity extends Activity
 {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    
 	    setContentView(R.layout.floorplan);
 	    
 	    String POIName = getIntent().getStringExtra("POI");
 	    final POI myPOI = DBAccessor.getInstance().getData().getPOIByName(POIName);
 	    
 	    
 	    final Gallery gallery = (Gallery) findViewById(R.id.gallery);
 	    gallery.setAdapter(new ImageAdapter(this, myPOI.getFloorList().getImageSrcs()));
 	    
 	    TextView buildingTitle = (TextView) findViewById(R.id.floor_title);
 	    buildingTitle.setText( POIName );
 	    
 	    
 	    
 	    final TextView floorLabel = (TextView) findViewById(R.id.floor_label);
 	    floorLabel.setText( "Floor 1 of " + myPOI.getFloors() );
 	    
 	    
 	    final MapView floorpic = new MapView( getApplicationContext() );
     	// Make it the current view
 	    
 	    Bitmap first = (Bitmap)gallery.getAdapter().getItem(0);
         floorpic.setImageBitmap( first );
         floorpic.setMaxZoom(4f);
         floorpic.setAdjustViewBounds(true);
         
         RelativeLayout rl = (RelativeLayout) findViewById(R.id.floor_view);
         
         RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
             RelativeLayout.LayoutParams.WRAP_CONTENT,
             RelativeLayout.LayoutParams.WRAP_CONTENT);
         lp.addRule(RelativeLayout.BELOW, R.id.floor_label);
        //lp.setMargins(0, 0, 0, 20);
         lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
         
         rl.addView(floorpic, lp);
 
 	    
         
 	    
 	    gallery.setOnItemClickListener(new OnItemClickListener() {
 	        public void onItemClick(AdapterView parent, View v, int position, long id)
 	        {
 	        	floorLabel.setText( "Floor " + (position + 1) + " of " + myPOI.getFloors() );
 	        	floorpic.setImageBitmap( (Bitmap)gallery.getAdapter().getItem(position) );
 	        }
 	    });
 	}
 }
 
 
 class ImageAdapter extends BaseAdapter
 {
     int mGalleryItemBackground;
     private Context mContext;
 
     private Bitmap[] images;
     
     public ImageAdapter(Context c, String[] imgs)
     {
         mContext = c;
         setBitmaps( c, imgs );
         TypedArray attr = mContext.obtainStyledAttributes(R.styleable.FloorPlan);
         mGalleryItemBackground = attr.getResourceId(
                 R.styleable.FloorPlan_android_galleryItemBackground, 0);
         attr.recycle();
         
     }
     
     
 
     
     private Bitmap decodeFile(Context c, String src)
     {
         Bitmap b = null;
         InputStream is = null;
         int IMAGE_MAX_SIZE = 1000;
         try
         {
         	is = c.getAssets().open(src);
             //Decode image size
             BitmapFactory.Options o = new BitmapFactory.Options();
             o.inJustDecodeBounds = true;
 
             
             BitmapFactory.decodeStream(is, null, o);
            
 
             int scale = 1;
             if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                 scale = (int)Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
             }
 
             //Decode with inSampleSize
             BitmapFactory.Options o2 = new BitmapFactory.Options();
             o2.inSampleSize = scale;
             b = BitmapFactory.decodeStream(is, null, o2);
             
             is.close();
         }
         catch (IOException e)
         {
         	e.printStackTrace();
         }
         
         return b;
     }
     
     public void setBitmaps( Context c, String[] srcs )
     {
     	images = new Bitmap[srcs.length];
     	for (int i=0; i<srcs.length; i++)
     	{
     		images[i] = decodeFile(c, srcs[i]);
     	}
     }
 
     public int getCount()
     {
         return images.length;
     }
 
     public Bitmap getItem(int position)
     {
         return images[position];
     }
 
     public long getItemId(int position)
     {
         return position;
     }
 
     public View getView(int position, View convertView, ViewGroup parent)
     {
         ImageView imageView = new ImageView(mContext);
 
         imageView.setImageBitmap(images[position]);
         imageView.setLayoutParams(new Gallery.LayoutParams(150, 100));
         imageView.setScaleType(ImageView.ScaleType.FIT_XY);
         imageView.setBackgroundResource(mGalleryItemBackground);
 
         return imageView;
     }
 }
