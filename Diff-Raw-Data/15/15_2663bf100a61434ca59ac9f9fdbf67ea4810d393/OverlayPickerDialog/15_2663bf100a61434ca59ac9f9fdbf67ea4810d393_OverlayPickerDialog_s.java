 package com.hipsterrific.app;
 
 import java.util.ArrayList;
 
 import android.annotation.SuppressLint;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.hipsterrific.R;
 import com.hipsterrific.FaceDetector.OverlaySpec;
 
 @SuppressLint("ValidFragment")
 public class OverlayPickerDialog extends DialogFragment implements OnItemClickListener {
 
 	private ListView overlayList;
 	ArrayList<OverlaySpec> overlaySpecs;
 	OverlayAdapter adapter;
 	private boolean appliedOverlays;
 	
 	OnOverlaySelectedListener selectedListener;
 	
 	public interface OnOverlaySelectedListener {
 		public void onOverlaySelected(OverlaySpec overlaySpec);
 	}
 	
 	public void setOnOverlaySelectedListener(OnOverlaySelectedListener selectedListener){
 		this.selectedListener = selectedListener;
 	}
 	
 	public OverlayPickerDialog(ArrayList<OverlaySpec> overlaySpecs, boolean appliedOverlays) {
 		this.overlaySpecs = overlaySpecs;
 		this.appliedOverlays = appliedOverlays;
 	}
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.overlay_picker, container);
         this.overlayList = (ListView) view.findViewById(R.id.overlay_list);
         
         if (this.appliedOverlays)
         	this.getDialog().setTitle(R.string.overlay_picker_applied_overlays);
         else
         	this.getDialog().setTitle(R.string.overlay_picker_title);
         
         this.overlayList.setOnItemClickListener(this);
    	this.adapter = new OverlayAdapter(getActivity(), R.id.overlay_list, overlaySpecs);
         this.overlayList.setAdapter(adapter);
         
         return view;
     }
 
 	@Override
 	public void onItemClick(AdapterView<?> adapterView, View v, int index, long id) {
 		Log.i("Overlaypicker","height of item clicked: "+PxtoDP(v.getHeight())+"w: "+PxtoDP(v.getWidth()));
 			//Toast.makeText(getActivity().getApplicationContext(), this.overlaySpecs.get(position).getName(), Toast.LENGTH_SHORT).show();		
 		if (this.selectedListener != null){
 			this.selectedListener.onOverlaySelected(this.overlaySpecs.get(index));
 		}
 		else {
 			this.dismiss();
 		}
 	 }
 	
 	public int PxtoDP(int px){
 		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
 		return (int) ((px/displayMetrics.density)+0.5);
 	}
 }
