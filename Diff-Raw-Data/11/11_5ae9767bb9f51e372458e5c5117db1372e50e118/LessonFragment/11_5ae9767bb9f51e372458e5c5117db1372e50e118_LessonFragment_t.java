 package com.jas.devotional;
 
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class LessonFragment extends Fragment  {
 
 	private static int currentLesson = 1;
 
 	private MediaPlayer mediaPlayer;
 	
 
 	@TargetApi(11)
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		//setHasOptionsMenu(true);
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle b) {
 
 		Log.d(Constants.DEVICE_DEBUG_APP_CODE,
 				"Creating view of lessons screen...");
 		View v = inflater.inflate(R.layout.fragment_lessons, vg, false);
 		
 		final Button play = (Button) v.findViewById(R.id.playPause);
 
 		play.setBackgroundResource(R.drawable.play_button);
 		play.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				if (currentLesson == 1) {
 					Log.d(Constants.DEVICE_DEBUG_APP_CODE,
 							"Playing audio file...");
 
 					if (mediaPlayer == null) {
 						mediaPlayer = MediaPlayer.create(
 								LessonFragment.this.getActivity(),
 								R.raw.guruwheel);
 						mediaPlayer.start(); // no need to call prepare();
 												// create() does that for you
 						
 						play.setBackgroundResource(R.drawable.pause_btn);
 
 					} else if (mediaPlayer.isPlaying()) {
 						mediaPlayer.pause();
 						play.setBackgroundResource(R.drawable.play_button);
 					} else if (!mediaPlayer.isPlaying()) {
 						mediaPlayer.start();
 						play.setBackgroundResource(R.drawable.pause_btn);
 
 					}
 
 				} else if (currentLesson == 2) {
 					Log.d(Constants.DEVICE_DEBUG_APP_CODE,
 							"Playing video file...");
 					if (mediaPlayer != null) {
 						mediaPlayer.stop();
 						mediaPlayer.release();
 						mediaPlayer = null;
 					}
 
 					// Uri data =
 					// Uri.parse("android.resource://com.jas.devotional/"+R.raw.gupanishad);
 					// Intent i = new Intent(Intent.ACTION_VIEW);
 					// i.setDataAndType(data, "video/*");
 
 					// startActivity(i);
 
 					Intent videoIntent = new Intent(getActivity(),
 							VideoPlayerActivity.class);
 
 					videoIntent.putExtra("videoFileName", "phonetics");
 					startActivity(videoIntent);
 				} else {
 					Log.d(Constants.DEVICE_DEBUG_APP_CODE, "Showing alert.");
 
 					// 1. Instantiate an AlertDialog.Builder with its
 					// constructor
 					AlertDialog.Builder builder = new AlertDialog.Builder(
 							getActivity());
 
 					// Add the buttons
 					builder.setPositiveButton(R.string.ok,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									// User clicked OK button
 								}
 							});
 					// 2. Chain together various setter methods to set the
 					// dialog characteristics
 					String message = getString(R.string.dialog_message,
 							currentLesson - 1, currentLesson);
 					builder.setMessage(message).setTitle(R.string.dialog_title);
 
 					// 3. Get the AlertDialog from create()
 					AlertDialog dialog = builder.create();
 					dialog.show();
 
 				}
 
 			}
 		});
 
 		Button previous = (Button) v.findViewById(R.id.previous);
 
 		previous.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (currentLesson > 1) {
 					currentLesson--;
 
 					TextView lesson = (TextView) LessonFragment.this
 							.getActivity().findViewById(R.id.lessonNumber);
 					lesson.setText("Lesson#" + currentLesson);
 					TextView pageCounter = (TextView) LessonFragment.this
 							.getActivity().findViewById(R.id.pageCounter);
 					pageCounter.setText(currentLesson + "/10");
 
 				}
 				if (mediaPlayer != null && mediaPlayer.isPlaying()) {
 					mediaPlayer.pause();
 					play.setBackgroundResource(R.drawable.play_button);
 				}
 			}
 		});
 
 		Button next = (Button) v.findViewById(R.id.next);
 
 		next.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (currentLesson < 10) {
 					currentLesson++;
 
 					TextView lesson = (TextView) LessonFragment.this
 							.getActivity().findViewById(R.id.lessonNumber);
 					lesson.setText("Lesson#" + currentLesson);
 					TextView pageCounter = (TextView) LessonFragment.this
 							.getActivity().findViewById(R.id.pageCounter);
 					pageCounter.setText(currentLesson + "/10");
 
 				}
 
 				if (mediaPlayer != null && mediaPlayer.isPlaying()) {
 					mediaPlayer.pause();
 					play.setBackgroundResource(R.drawable.play_button);
 				}
 
 			}
 		});
 
 		TextView lessonName = (TextView) v.findViewById(R.id.lessonNumber);
 		lessonName.setText("Lesson#" + currentLesson);
 
 		TextView pageCounter = (TextView) v.findViewById(R.id.pageCounter);
 		pageCounter.setText(currentLesson + "/10");
 
 		return v;
 
 	}
 
 	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			
		}
		
		super.onPause();
	}
 	    
 	    
 	  
 
 }
