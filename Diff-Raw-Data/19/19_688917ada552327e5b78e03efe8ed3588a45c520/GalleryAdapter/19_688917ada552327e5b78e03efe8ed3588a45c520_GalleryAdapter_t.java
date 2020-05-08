 package com.github.groupENIGMA.journalEgocentrique;
 
 import java.util.Calendar;
 import java.util.List;
 
 import android.content.Context;
 import android.net.Uri;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 
 import com.github.groupENIGMA.journalEgocentrique.model.DB;
 import com.github.groupENIGMA.journalEgocentrique.model.Photo;
 
 public class GalleryAdapter extends BaseAdapter{
     private Context mContext;
     private List<Photo> photos;
     private DB database;
 
     public GalleryAdapter(Context c, String filter) {
         mContext = c;
         database = new DB(c);
         database.open();
         
         // Get only the filtered images...
         if( filter != null ){
         	Calendar today = Calendar.getInstance();
             Calendar from = Calendar.getInstance();   
             
             // Choose the right filter
             if(filter.equals("3Days")){
             	from.add(Calendar.DAY_OF_WEEK, -3);
             }
             if(filter.equals("Week")){
         		from.add(Calendar.WEEK_OF_YEAR, -1);
         	}
             if(filter.equals("Month")){
         		from.add(Calendar.MONTH, -1);
         	}
            if(filter.equals("None")){
            	photos = database.getPhotos();
            }
            else{
            	// Get only the filtered photos list
                photos = database.getPhotos(from, today);  
            }   	
         }
         // ... or get all the photos
         else {
         	photos = database.getPhotos();
         }
     }
 
     public int getCount() {
         return photos.size();
     }
 
     public Photo getItem(int position){
         return photos.get(position);
     }
 
     // create a new ImageView for each item referenced by the Adapter
     public View getView(int position, View convertView, ViewGroup parent) {
         ImageView imageView;
         if (convertView == null) {  // if it's not recycled, initialize some attributes
             imageView = new ImageView(mContext);
             imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
             imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
             imageView.setPadding(8, 8, 8, 8);
         } else {
             imageView = (ImageView) convertView;
         }
         String path = photos.get(position).getPathThumb();
         if(path != null)
         	imageView.setImageURI(Uri.parse(path));
         return imageView;
     }
 
 	@Override
 	public long getItemId(int arg0) {
 		return photos.get(arg0).hashCode();
 	}
 
 
 }
