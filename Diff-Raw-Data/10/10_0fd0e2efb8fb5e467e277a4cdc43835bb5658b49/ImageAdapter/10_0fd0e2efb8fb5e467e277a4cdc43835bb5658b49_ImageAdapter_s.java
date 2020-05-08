 package ca.ualberta.cs.completemytask;
 
import java.util.ArrayList;
import java.util.List;
 
 import android.content.Context;
import android.graphics.Bitmap;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
import android.widget.Gallery;
 import android.widget.ImageView;
 
 
 /**
  * A modified ImageAdapter for displaying task images in the Gallery view.
  * Able to add images to the gallery by calling 'add(bitmap)' on this class 
  * 
  * @author Devon Waldon
  *
  */
 public class ImageAdapter extends BaseAdapter {
 	private Context context;
 	private Task task = null;
 	//private List<Bitmap> bitmaps = new ArrayList<Bitmap>();
 
 	public ImageAdapter(Context c) {
 		this.context = c;
 	}
 	
 	public ImageAdapter(Context c, Task task) {
 		this.context = c;
 		this.task = task;
 	}
 
 	public int getCount() {
 		
 		if (task == null) {
 			return 0;
 		}
 		
 		return task.getNumberOfPhotos();
 	}
 
 	public Object getItem(int position) {
 		return position;
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ImageView i = new ImageView(context);
 		
 		i.setImageBitmap(task.getPhotoAt(position).getContent());
 		i.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
 
 		return i;
 	}
 	/*
 	public void addBitmap(Bitmap b){
 		bitmaps.add(b);
 	}
 	*/
 }
