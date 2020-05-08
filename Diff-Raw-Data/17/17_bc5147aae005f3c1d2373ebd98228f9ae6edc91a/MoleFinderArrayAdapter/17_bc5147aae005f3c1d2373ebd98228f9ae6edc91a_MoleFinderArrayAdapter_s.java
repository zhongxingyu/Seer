 package adapter.classes;
 
 import java.io.File;
 import java.util.List;
 
 import model.classes.ConditionEntry;
 import model.classes.DatabaseEntry;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class MoleFinderArrayAdapter extends ArrayAdapter<DatabaseEntry> {
     Activity context;
     List<DatabaseEntry> items;
     int layoutId;			// layout file
     int imageId;			// ImageView to display thumbnail in
     int titleTextId;		// TextView to display title in
     int commentTextid;		// TextView to display comment in
  
     public MoleFinderArrayAdapter(Activity context, int layoutId, int imageId, int titleTextId, int commentTextId,
     		List<DatabaseEntry> items) {
         
     	super(context, layoutId, items); 
         this.context = context;
         this.items = items;
         this.layoutId = layoutId;
         this.imageId = imageId;
         this.titleTextId = titleTextId;
         this.commentTextid = commentTextId;
     }
     
     public MoleFinderArrayAdapter(Activity context, int layoutId, int titleTextId, int commentTextId,
     		List<DatabaseEntry> items) {
         
     	super(context, layoutId, items); 
         this.context = context;
         this.items = items;
         this.layoutId = layoutId;
         this.titleTextId = titleTextId;
         this.commentTextid = commentTextId;
     }
  
     public View getView(int pos, View convertView, ViewGroup parent)
     {
     	// current row
         LayoutInflater inflater = context.getLayoutInflater();
         View row = inflater.inflate(layoutId, null);
         ImageView thumbnail = (ImageView)row.findViewById(imageId);
         TextView title = (TextView)row.findViewById(titleTextId);
         TextView comment = (TextView) row.findViewById(commentTextid);
  
       
        File imgFile = new File("/sdcard/MoleFinderPics/" + ((ConditionEntry) items.get(pos)).getImage() + ".jpg");
        if(imgFile.exists()){
        	
        	Bitmap image = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        	Bitmap scaledImage = Bitmap.createScaledBitmap(image, 100, 100, false);
        	thumbnail.setImageBitmap(scaledImage);
         }
         // set text
         title.setText(items.get(pos).getTitle());
         comment.setText(items.get(pos).getDescription());
  
         return(row);
     }
 } 
