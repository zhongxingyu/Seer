 package com.schema.bro.nova;
 
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 
 import com.schema.bro.R;
 import com.schema.bro.ks.TextLoaderAnimator;
 
 public class NovaFragment extends Fragment{
 
 	private View view;
 	private Bitmap novaImage;
 	private TextLoaderAnimator animator = null;
	private boolean ready = false;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		view = inflater.inflate(R.layout.nova_fragment, container, false);
 		return view;
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		if(animator == null){
 			final ImageView imageView = (ImageView) view.findViewById(R.id.novaFragmentImageView);
 			animator = new TextLoaderAnimator(getActivity(), imageView, "Laddar", 32);
			ready = true;
 		}
 	}
 		
 	@Override
	public void onStop() {
		super.onStop();
		ready = false;
	}

	@Override
 	public void onDestroy() {
 		super.onDestroy();
		ready = false;
 		if(novaImage != null)
 			novaImage.recycle();
 		novaImage = null;
 	}
 
	public void notifyDownload(){
		if(ready){
			animator.setScaleType(ScaleType.CENTER_INSIDE);
			animator.changeText("Laddar");
		}
	}
	
 	public void setImageView(Bitmap bitmap){
 		if(bitmap == null){
 			Log.e("NovaFragment", "Bitmap is null");
 			animator.setScaleType(ScaleType.CENTER_INSIDE);
 			animator.changeText("Kan inte ladda bild");
 			animator.stop();
 			return;
 		}
 		
 		novaImage = Bitmap.createBitmap(bitmap);
 		
 		if(getView() == null){
 			Log.e("NovaFragment", "View is null");
 			return;
 		}
 		
 		final ImageView image = (ImageView) getView().findViewById(R.id.novaFragmentImageView);
 		if(image != null){
 			image.setScaleType(ScaleType.FIT_XY);
 			image.setImageBitmap(novaImage);
 		}else{
 			Log.e("NovaFragment", "ImageView is null");
 		}
 	}
 
 }
