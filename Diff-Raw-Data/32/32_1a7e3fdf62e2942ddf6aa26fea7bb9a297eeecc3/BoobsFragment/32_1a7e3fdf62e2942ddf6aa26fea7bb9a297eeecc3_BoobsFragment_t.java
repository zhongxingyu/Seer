 package com.bytopia.oboobs.fragments;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.ImageView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.internal.widget.IcsProgressBar;
 import com.bytopia.oboobs.BoobsFragmentHolder;
 import com.bytopia.oboobs.DownloadService;
 import com.bytopia.oboobs.ImageReceiver;
 import com.bytopia.oboobs.OboobsApp;
 import com.bytopia.oboobs.R;
 import com.bytopia.oboobs.model.Boobs;
 
 public class BoobsFragment extends SherlockFragment {
 
 	public static final String INIT_BOOBS = "init_boobs";
 	
 	private ImageView imageView;
 	private IcsProgressBar progressBar;
 	OboobsApp app;
 
 	BoobsFragmentHolder boobsFragmentHolder;
 
 	boolean fs = false;
 	
 	Boobs initBoos;
 
	public int SENDER_TYPE = 23;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 		initBoos = (Boobs) getArguments().get(INIT_BOOBS);
 	}
 	
 	@Override
 	public void onStart(){
 		super.onStart();
 
		app.addImageReceiver(mImageReceiver);
		
 		if(initBoos!=null){
 			setBoobs(initBoos);
 			initBoos = null;
 		}
 	}
	
	@Override
	public void onStop() {
		app.removeImageReciever(mImageReceiver);
		super.onStop();
	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		app = (OboobsApp) activity.getApplication();
 		boobsFragmentHolder = (BoobsFragmentHolder) activity;
 	}
 
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.boobs_fragment_layout, container,
 				false);
 
 		imageView = (ImageView) view.findViewById(R.id.image);
 		progressBar = (IcsProgressBar) view.findViewById(R.id.progressBar);
 
 		imageView.setOnClickListener(new OnClickListener() {
 
 			@TargetApi(11)
 			@Override
 			public void onClick(View v) {
 				fs = !fs;
 				updateFullscreen();
 			}
 
 		});
 
 		updateFullscreen();
 
 		return view;
 	}
 
 	@TargetApi(11)
 	private void updateFullscreen() {
 		if (fs) {
 			getActivity().getWindow().setFlags(
 					WindowManager.LayoutParams.FLAG_FULLSCREEN,
 					WindowManager.LayoutParams.FLAG_FULLSCREEN);
 			getSherlockActivity().getSupportActionBar().hide();
 			if (Build.VERSION.SDK_INT > 10) {
 				imageView
 						.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
 			}
 		} else {
 			getActivity().getWindow().clearFlags(
 					WindowManager.LayoutParams.FLAG_FULLSCREEN);
 			getSherlockActivity().getSupportActionBar().show();
 			if (Build.VERSION.SDK_INT > 10) {
 				imageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
 			}
 		}
 	}
 
 	public void setBoobs(final Boobs boobs) {
 		new AsyncTask<Boobs, Void, Bitmap>() {
 
 			@Override
 			protected Bitmap doInBackground(Boobs... params) {
 				Boobs b = params[0];
 				if (b.hasFavoritedFile()) {
 					try {
 						InputStream is = new FileInputStream(b.getSavedFile());
 						Bitmap bm = BitmapFactory.decodeStream(is);
 						return bm;
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 					}
 
 				}
 				return null;
 			}
 
 			protected void onPostExecute(Bitmap bitmap) {
 				if (bitmap != null) {
 					setImage(bitmap);
 				} else {
 					DownloadService.requestImage(getActivity(), SENDER_TYPE,
 							boobs, false, 0, 0);
 				}
 			};
 
 		}.execute(boobs);
 	}
 
 	private ImageReceiver mImageReceiver = new ImageReceiver() {
 
 		@Override
 		public void receiveImage(int imageId, Bitmap bitmap) {
 			setImage(bitmap);
 		}
 
 		@Override
 		public int getSenderType() {
 			return SENDER_TYPE;
 		}
 	};
 
 	private void setImage(Bitmap bitmap) {
 		imageView.setImageBitmap(bitmap);
 		progressBar.setVisibility(View.GONE);
 		boobsFragmentHolder.imageReceived(bitmap);
 	}
 
 }
