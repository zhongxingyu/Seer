 package com.ps.sleek.adapter;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PaintFlagsDrawFilter;
 import android.graphics.PixelFormat;
 import android.graphics.Rect;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.PaintDrawable;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.ps.sleek.R;
 import com.ps.sleek.model.App;
 import com.ps.sleek.utils.AppsLayoutUtils;
 import com.ps.sleek.utils.DimensionUtils;
 
 public class AppsAdapter extends ArrayAdapter<App> implements OnItemClickListener {
 	private ArrayList<App> mApplications;
 	private Context activity;
 
     public AppsAdapter(Context context, ArrayList<App> applications) {
     	super(context, 0, applications);
     	this.activity = context;
     	this.mApplications = applications;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         final App app = mApplications.get(position);
 
         if (convertView == null) {
             final LayoutInflater inflater = LayoutInflater.from(getContext());
             convertView = inflater.inflate(R.layout.view_application, parent, false);
             convertView.setLayoutParams(new AbsListView.LayoutParams(AppsLayoutUtils.getColumnPixels(activity), AppsLayoutUtils.getRowPixels(activity)));
         }
 
         Drawable icon = app.icon;
 
         int padding = DimensionUtils.getPixelForDp(activity, 5);
 		int width = AppsLayoutUtils.getColumnPixels(activity) - 2 * padding;
         int height = width;
 
         final int iconWidth = icon.getIntrinsicWidth();
         final int iconHeight = icon.getIntrinsicHeight();
         
         Log.d("ps", "App:" + app.name + " Icon:" + iconWidth + "x" + iconHeight);
         
         if (icon instanceof PaintDrawable) {
             PaintDrawable painter = (PaintDrawable) icon;
             painter.setIntrinsicWidth(width);
             painter.setIntrinsicHeight(height);
         }
 
         if (width != Math.max(iconWidth, iconHeight)) {
             final float ratio = (float) iconWidth / iconHeight;
 
             if (iconWidth > iconHeight) {
                 height = (int) (width / ratio);
             } else if (iconHeight > iconWidth) {
                 width = (int) (height * ratio);
             }
             
             Log.d("ps", "In:" + app.name + " " + width + "x" + height);
 
             final Bitmap.Config c =
                     icon.getOpacity() != PixelFormat.OPAQUE ?
                         Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
             final Bitmap thumb = Bitmap.createBitmap(width, height, c);
             
             final Canvas canvas = new Canvas(thumb);
             canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));
             // Copy the old bounds to restore them later
             // If we were to do oldBounds = icon.getBounds(),
             // the call to setBounds() that follows would
             // change the same instance and we would lose the
             // old bounds
             Rect mOldBounds = new Rect();
             mOldBounds.set(icon.getBounds());
             icon.setBounds(0, 0, width, height);
             icon.draw(canvas);
             icon.setBounds(mOldBounds);
             icon = app.icon = new BitmapDrawable(thumb);
         }
         
         final TextView textView = (TextView) convertView.findViewById(R.id.label);
         textView.setText(app.name);
         
         ImageView imageView = (ImageView) convertView.findViewById(R.id.icon_image);
         int viewEdge = Math.max(height, width) + 2 * padding;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(viewEdge, viewEdge - padding);
 		imageView.setLayoutParams(layoutParams);
 		imageView.setScaleType(ScaleType.CENTER_INSIDE);
 		imageView.setPadding(padding, padding, padding, padding);
         imageView.setImageDrawable(icon);
         
         return convertView;
     }
     
     /**
      * Starts the selected activity/application in the grid view.
      */
 	@Override
 	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
 		activity.startActivity(mApplications.get(position).intent);
 	}
 }
