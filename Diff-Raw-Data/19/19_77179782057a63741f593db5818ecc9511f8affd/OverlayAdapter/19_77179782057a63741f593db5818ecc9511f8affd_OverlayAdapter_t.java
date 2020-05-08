 package com.hipsterrific.app;
 
 import java.util.ArrayList;
 
 import android.content.Context;
import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.ColorDrawable;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
import com.googlecode.javacv.cpp.opencv_videostab.IFrameSource;
 import com.hipsterrific.R;
 import com.hipsterrific.FaceDetector.OverlaySpec;
 
 public class OverlayAdapter extends ArrayAdapter<OverlaySpec> {
 
 	
 	private ArrayList<OverlaySpec> overlaySpecs;
	
 	public OverlayAdapter(Context context, int resourceId, ArrayList<OverlaySpec> overlaySpecs) {
 		super(context, resourceId, overlaySpecs);
 		
 		this.overlaySpecs = overlaySpecs;
 	}
 
 	@Override
 	public View getView(int index, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = (LayoutInflater) this.getContext()
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			convertView = inflater.inflate(R.layout.overlay_list_item, parent, false);
			
 		OverlaySpec spec = this.overlaySpecs.get(index);
 
 		if (spec != null) {
 			TextView textView = (TextView) convertView.findViewById(R.id.overlay_name);
			Log.d("OverlayAdapter", spec.getClass().getName() + " bmp is "+(spec.getBitmap() == null ? "null" : "not null"));
 			if (textView != null){
 				if(spec.getBitmap() != null){
 					//we don't have a resource or a filepath, so we need to use the deprecated constructor :-(
 					convertView.setBackgroundDrawable(new BitmapDrawable(spec.getBitmap()));
 				}else{
 					textView.setText(spec.getName());
 				}
 			}
 		}
 
 		return convertView;
 	}
 
    @Override public OverlaySpec getItem(int arg0) { return this.overlaySpecs.get(arg0); }
    @Override public int getCount() { return this.overlaySpecs.size(); }
 }
