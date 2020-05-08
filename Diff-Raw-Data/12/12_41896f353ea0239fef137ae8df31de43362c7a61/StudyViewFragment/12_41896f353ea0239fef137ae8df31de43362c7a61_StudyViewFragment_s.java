 package com.cs371m.notesync;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Currency;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.MediaController;
 import android.widget.Toast;
 
 /**
  * Fragment for Study View
  * @author ctang
  *
  */
 public class StudyViewFragment extends Fragment { //vs static inner class?
 
 	//Global ArrayList to store all x,y coords of long presses
 	//public static ArrayList<point> lpressLocs=new ArrayList();
 	public static int numlpress;
 
 	public static final String ARG_IMAGE_PATH = "image_path";
 	public static final String ARG_RECORDING_PATH = "recording_path";
 	public static final String STUDY_VIEW_LOG_TAG = "StudyViewFragment";
 	public MyImageView image;
 
 
 	MediaController mediaController;
 	PlaybackController pController;
 	Note note;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		MainActivity activity = (MainActivity) getActivity();
 
 		Log.v(STUDY_VIEW_LOG_TAG, "in onCreateView");
 		mediaController = new MediaController(getActivity());
 		pController = new PlaybackController(activity.mBoundPlayService);
 
 		View rootView = inflater.inflate(R.layout.fragment_study_view, container, false);
 		//Set up mediaController
 		if (activity.mCurrentNote != null)
 		{
 			note = activity.mCurrentNote;
 			pController.setMediaPath(activity.mCurrentNote.recording);
 		}
 		mediaController.setMediaPlayer(pController);
 
 		//Set the prev next listeners to seek note audio bookmarks
 		mediaController.setPrevNextListeners(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				//next button clicked
 				long pos = (long) pController.getCurrentPosition();
 				if (note != null) {
 					for (int i=0; i < note.timestamps.size(); i++) {
 						long ts = note.timestamps.get(i);
 						if (ts > pos) {
 							pController.seekTo((int)ts);
 							return;
 						}
 					}
 				}
 			}
 		}, new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				//previous button clicked
 				long pos = (long) pController.getCurrentPosition();
 				if (note != null) {
 					for (int i=note.timestamps.size() - 1; i>=0; i--)
 					{
 						long ts = note.timestamps.get(i);
 						if (ts < pos - 2000) {
 							pController.seekTo((int)ts);
 							return;
 						}
 					}
 					pController.seekTo(0);
 				}
 			}
 		});
 
 		image = (MyImageView) rootView.findViewById(R.id.imgDisplay);
 		//Set mediaControllers, playbackController
 		image.setControllers(mediaController, pController);
 
 		//? arguments Bundle vs savedInstanceState? vs using mainActivity state?
 		//external useful for testing
 		//String externalDirectory = rootView.getContext().getExternalFilesDir(null).getAbsolutePath(); //storage/sdcard0/Android/data/com.cs371m.notesync/files
 		//String appDirectory = c.getFilesDir().getAbsolutePath(); //getPath? //getFilesDir gives hidden internal directory /data/data
 		//String imgDirectory = c.getDir("img", Context.MODE_PRIVATE).getAbsolutePath(); //app_img
 		Log.v(STUDY_VIEW_LOG_TAG, "about to update study view");
 
 		//mediaController = (MediaController) rootView.findViewById(R.id.mediaController); //DOES NOT WORK!
 
 
 		mediaController.setAnchorView(image);
 		/*mediaController.setOnClickListener( new View.OnClickListener() {
 			@Override
 			public void onClick(View view) //onPlay...
 			{
 
 
 			}
 		}); */
 
 		updateStudyView();
 
 		//Add Long press gesture for annotation
 		//Init number of long presses so far
 		numlpress=0;
 		/*
 		Log.v(STUDY_VIEW_LOG_TAG, "declare StudyViewFrag gesture fields");
         final GestureDetector gesture = new GestureDetector(
 				getActivity(),  new GestureDetector.SimpleOnGestureListener()
 				{
 
 					MainActivity main = ((MainActivity) getActivity());
 
 					//Always include onDown for any gesture
 					@Override
 					public boolean onDown(MotionEvent e)
 					{
 						Log.v(STUDY_VIEW_LOG_TAG, "down tapped");
 
 						return true;
 					}
 					@Override
 					public boolean onSingleTapUp(MotionEvent e)
 					{
 						Log.v(STUDY_VIEW_LOG_TAG, "single Tap up");
 						//mediaController.show();
 						return true;
 					}
 					@Override
 					public boolean onDoubleTap (MotionEvent e)
 					{
 
 						Log.v(STUDY_VIEW_LOG_TAG, "double tapped");
 						//mediaController.show();
 						return true;
 					}
 //					@Override
 //					public void onLongPress (MotionEvent e)
 //					{
 //						//Retrieve coords
 //						//if NUMBER OF long presses so far  <= Total number of notes in arraylist
 //						//Update "Current" Note's list of bookmarks
 //						if (main.mCurrentNote!=null)
 //						{
 //							Point perPt = new Point(e.getX(),e.getY());
 //							if (numlpress < main.mCurrentNote.timestamps.size())
 //							{
 //								Log.v(STUDY_VIEW_LOG_TAG, "main.mcurr " + main.mCurrentNote);
 //								if (main.mCurrentNote.bookmarks!=null)
 //								{
 //									main.mCurrentNote.bookmarks.add(perPt);
 //									Log.v(STUDY_VIEW_LOG_TAG, "added "+(numlpress+1)+ "th  x: "+perPt.x+" y: "+ perPt.y);
 //								}
 //								else
 //								{
 //									main.mCurrentNote.bookmarks=new ArrayList<Point>();
 //									main.mCurrentNote.bookmarks.add(perPt);
 //									Log.v(STUDY_VIEW_LOG_TAG, "added 1st x:"+perPt.x+" y: "+ perPt.y);
 //								}
 //							}
 //
 //							numlpress++;
 //						}
 //						else
 //						{
 //							Log.v(STUDY_VIEW_LOG_TAG,"Exceeded "+ main.mCurrentNote.timestamps.size()+" Presses\n");
 //						}
 //					}
 //
 				});
 //
 		rootView.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				return gesture.onTouchEvent(event);
 			}
 		});
 		*/
 //		Toast.makeText(getActivity().getApplicationContext(), "Lpress enabled: "+ gesture.isLongpressEnabled(), Toast.LENGTH_LONG).show();
 		return rootView;
 	}
 
 	public void updateStudyView()
 	{
 		MainActivity activity = (MainActivity) getActivity();
 
 
 		//set up media controller
 		if (activity.mCurrentNote != null)
 			pController.setMediaPath(activity.mCurrentNote.recording); //TESTME: non-null?
 
 		//updates image
 
 		if (activity.mCurrentNote != null ) {
 			if (activity.mCurrentNote.image != null) {
 				drawImage(activity.mCurrentNote.image, image); //TODO: add pinch-zoom ///data/data/com.cs371m.notesync/files/IMG_20131104_102808.jpg
 				note = activity.mCurrentNote;
 			} else
 				image.setImageResource(R.drawable.no_associated_image); 
 
 			//TODO: test playback unaffected when returning to view
 			//MOVED TO SERVICE code
 			//if (activity.mCurrentNote.recording != null && mediaPlayer == null)
 			//	playRecording(activity.mCurrentNote.recording, rootView.getContext());
 		} else
 			image.setImageResource(R.drawable.no_note_selected); 
 
 
 	}
 
 	private void drawImage(String path, ImageView image) {
 		//TODO: validate correct path?
 		try {
 			if (!(new File(path).exists()))
 				throw new Exception("no such file exists");
 			Bitmap bmap = BitmapFactory.decodeFile(path);
 			if (bmap == null)
 				throw new Exception("decoding file failed");
 			image.setImageBitmap(bmap); //works for external storage
 
 		} catch (Exception e) {
 			Log.e(STUDY_VIEW_LOG_TAG, "Failed to display image at path: " + path + " with error: " + e.getMessage());
 			image.setImageResource(R.drawable.no_associated_image); //TODO: make error message
 		}
 	}
 
 }
