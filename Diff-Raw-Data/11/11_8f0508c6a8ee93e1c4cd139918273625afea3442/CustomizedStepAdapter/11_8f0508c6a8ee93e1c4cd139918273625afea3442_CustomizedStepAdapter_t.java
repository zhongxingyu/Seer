 package mit.edu.stemplusplus;
 
 import java.util.List;
 
 import mit.edu.stemplusplus.helper.Step;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.BaseAdapter;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class CustomizedStepAdapter extends BaseAdapter {
     private Activity activity;
     /** The collections of sellable objects */
     private List<Step> data;
     /** To inflate the view from an xml file */
     private LayoutInflater inflater = null;
 
     public CustomizedStepAdapter(Activity a, List<Step> steps) {
         activity = a;
         data = steps;
         inflater = (LayoutInflater) activity
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 
     @Override
     public int getCount() {
         // TODO Auto-generated method stub
         return data.size();
     }
 
     @Override
     public Object getItem(int position) {
         // TODO Auto-generated method stub
         return data.get(position);
     }
 
     @Override
     public long getItemId(int position) {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
 
         final ViewHolder holder;
         if (convertView == null) {
             holder = new ViewHolder();
             convertView = inflater.inflate(R.layout.step_display, null);
             holder.stepIcon = (ImageButton) convertView
                     .findViewById(R.id.step_icon_project_description);
             Log.d("get TextView", "ok");
             holder.stepImage = (ImageView) convertView
                     .findViewById(R.id.step_image_project_description);
 
             holder.stepDescription = (TextView) convertView
                     .findViewById(R.id.step_name_project_description);
             convertView.setTag(holder);
         }
         // picture is already there
         else {
             holder = (ViewHolder) convertView.getTag();
 
         }
 
         Step step = data.get(position);
 
         Log.d("get project", "ok");
         holder.stepDescription.setText(step.getDescription());
         Log.d("set Text", "ok");
         if (step.getMediaPath() != null) {
            
             holder.stepImage.setImageBitmap(BitmapFactory.decodeFile(step
                     .getMediaPath()));
         } else if (step.getMedia() != null)
             holder.stepImage.setImageBitmap((Bitmap) step.getMedia());
         else {
        	 holder.stepImage.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.test_image));
         }
         Log.d("set Image", "ok");
         holder.stepImage.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
 
             }
 
         });
 
         return convertView;
     }
     public class ViewHolder {
         private ImageButton stepIcon;
         private TextView stepDescription;
         private ImageView stepImage;
         private int id;
 
         public void setId(int id) {
             this.id = id;
         }
     }
    
 }
