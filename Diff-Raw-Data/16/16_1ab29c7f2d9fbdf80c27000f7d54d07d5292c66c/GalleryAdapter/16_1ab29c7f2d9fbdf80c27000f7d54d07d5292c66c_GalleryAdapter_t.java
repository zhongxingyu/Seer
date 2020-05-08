 package com.github.groupENIGMA.journalEgocentrique;
 
 import java.util.List;
 
 import android.content.Context;
 import android.net.Uri;
import android.util.Log;
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
 
     public GalleryAdapter(Context c) {
         mContext = c;
         database = new DB(c);
         database.open();
         photos = database.getPhotos();
        Log.d("Photos is empty", photos.isEmpty()+"");
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
 
        imageView.setImageURI(Uri.parse(photos.get(position).getPath()));//dovrebbe essere getPathThumb ma non e' ancora implementato, pero' gia cosi' fa il resize da solo
         return imageView;
     }
 
 	@Override
 	public long getItemId(int arg0) {
 		return photos.get(arg0).hashCode();
 	}
 
 
 }
