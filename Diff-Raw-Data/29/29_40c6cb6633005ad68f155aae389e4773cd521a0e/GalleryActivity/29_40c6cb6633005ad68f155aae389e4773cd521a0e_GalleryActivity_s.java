 package com.autburst.picture;
 
 import java.io.File;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Gallery;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.SlidingDrawer;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.autburst.picture.facebook.FacebookActivity;
 import com.autburst.picture.gallery.GalleryImageAdapter;
 
 public class GalleryActivity extends Activity {
 	
 	private static final String TAG = GalleryActivity.class.getSimpleName();
 	private static final int DIALOG_POST_OR_PREVIEW_ID = 1;
 	private static final int DIALOG_ERROR_UPLOADING_ID = 2;
 	
 	private Gallery gallery;
 	private ImageView galleryImageView;
 	private GalleryImageAdapter adapter;
 	
 	//toolBox
 	private RelativeLayout toolBar;
 	private TextView picDate;
 	private ImageButton deleteButton;
 	private ImageButton clearButton;
 	
 	private ImageButton playButton;
 	private ImageButton postButton;
 	private LinearLayout controlPanel;
 	
 	private String albumName;
 	
 	private ProgressDialog pd;
 	
 	private Handler handler;
 	
 	//sliding drawer
 	private SlidingDrawer drawer;
 	private ImageView handle;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.gallery);
         
         albumName = getIntent().getStringExtra("albumName");
         
         //sliding drawer
         handle = (ImageView) findViewById(R.id.handle);
         drawer = (SlidingDrawer) findViewById(R.id.drawer);
         drawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
 			
 			@Override
 			public void onDrawerOpened() {
 				// change background resource of handle to expanded
 				handle.setBackgroundResource(R.drawable.bottom_switcher_expanded_background);
 			}
 		});
         
         drawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
 			
 			@Override
 			public void onDrawerClosed() {
 				// change background resource of handle to collapsed
 				handle.setBackgroundResource(R.drawable.bottom_switcher_collapsed_background);
 			}
 		});
         
         //gallery view
         gallery = (Gallery) findViewById(R.id.gallery);
         galleryImageView = (ImageView) findViewById(R.id.galleryImageView);
         
         toolBar = (RelativeLayout) findViewById(R.id.toolBar);
         picDate = (TextView) findViewById(R.id.date);
         deleteButton = (ImageButton) findViewById(R.id.deletePic);
         clearButton = (ImageButton) findViewById(R.id.clearPic);
         
         handler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				pd.dismiss();
 				
 				switch (msg.what) {
 				case Utilities.MSG_ERROR_UPLOADING:
 					showDialog(DIALOG_ERROR_UPLOADING_ID);
 					break;
 				case Utilities.MSG_FINISHED_UPLOADING:
 					showDialog(DIALOG_POST_OR_PREVIEW_ID);
 					break;
 				default:
 					break;
 				}
 			}
 		};
         
         controlPanel = (LinearLayout) findViewById(R.id.controlPanel);
         postButton = (ImageButton) findViewById(R.id.postButton);
         postButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 
 				pd = ProgressDialog.show(GalleryActivity.this, "Working..", "Uploading images to server", true,
 						false);
 				Log.d(TAG, "start thread");
 				Thread thread = new Thread(new Uploader(handler, albumName, getSharedPreferences(Utilities.PIC_STORE, 0)));
 				thread.start();
 
 			}
 		});
 		
 		playButton = (ImageButton) findViewById(R.id.play_button);
 		playButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				Intent intent = new Intent(GalleryActivity.this, PlayMovieActivity.class);
     			intent.putExtra("albumName", albumName);
     			startActivity(intent);
 			}
 		});
 		enablePlayButton();
 
         gallery.setOnItemClickListener(new OnItemClickListener() {
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {	    
 		    Log.d(TAG, "You have selected picture: " + arg2 + " AlbumName: " + albumName);
 		    
 		    disablePlayButton();
 		    
 		    File albumDirectory = Utilities.getAlbumDirectory(albumName);
 		    final File selectedPic = albumDirectory.listFiles()[arg2];
 		    Date date = new Date(selectedPic.lastModified());
 		    
 		           					
 //		    File file = new File(, Utilities.g[arg2]);
 		    Bitmap bm = Bitmap.createBitmap(BitmapFactory.decodeFile(selectedPic.getAbsolutePath()));
 		    galleryImageView.setImageBitmap(bm);
 		    
 //		    Toast.makeText(getBaseContext(), DateFormat.format("dd.MM.yyyy", date), 
 //			        Toast.LENGTH_LONG).show();
 		    picDate.setText(DateFormat.format("dd.MM.yyyy kk:mm", date));
 		    deleteButton.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					Log.d(TAG, "Delete image file " + selectedPic.getAbsolutePath());
 					selectedPic.delete();
 					galleryImageView.setImageBitmap(null);
 					toolBar.setVisibility(View.GONE);
 					adapter.notifyDataSetChanged();
 					
 					enablePlayButton();
 				}
 			});
 		    
 		    clearButton.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View arg0) {
 					Log.d(TAG, "clear view" );
 					
 					galleryImageView.setImageBitmap(null);
 					toolBar.setVisibility(View.GONE);
 					
 					enablePlayButton();
 				}
 			});
 		    
 		    toolBar.setVisibility(View.VISIBLE);
 		}
 		});
         
         adapter = new GalleryImageAdapter(this, Utilities.getAlbumDirectory(albumName));
 		gallery.setAdapter(adapter);
     }
     
     protected Dialog onCreateDialog(int id) {
         final Dialog dialog;
         final Context mContext = this;
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         
         switch(id) {
         case DIALOG_ERROR_UPLOADING_ID:
         	builder.setTitle("Service...");
        	builder.setMessage("... ist nicht erreichbar. Bitte versuche es sp�ter nochmal!");
         	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                 	dialog.dismiss();
                 }
             });
         	dialog = builder.create();
         	break;
         case DIALOG_POST_OR_PREVIEW_ID:
        	builder.setTitle("W�hle...");
         	final String[] options = new String[] { "Preview Video", "Post to Facebook" };
         	builder.setItems(options, new DialogInterface.OnClickListener() {
         	    public void onClick(DialogInterface dialog, int item) {
 //        	        //Toast.makeText(getApplicationContext(), albums[item], Toast.LENGTH_SHORT).show();
 //        	    	//open gallery
 //        	    	Intent intent = new Intent(mContext, GalleryActivity.class);
 //        			intent.putExtra("albumName", albums[item]);
 //        			startActivity(intent);
         	    	switch (item) {
 					case 0: {
 						//Preview Video
 						Intent intent = new Intent(mContext, VideoDemoActivity.class);
 	        			intent.putExtra("albumName", albumName);
 	        			startActivity(intent);
 						break;
 					}
 					case 1: {
 						postToFacebook();
 						break;
 					}
 					default:
 						break;
 					}
         	    }
         	});
         	dialog = builder.create();
         	break;
         default:
             dialog = null;
         }
     
         return dialog;
     }
     
     private void postToFacebook() {
 		String savedVideoId = getSharedPreferences(Utilities.PIC_STORE, 0).getString(albumName + ".videoId", null);
 		if (savedVideoId == null) {
 			Toast.makeText(this, "Video-URL ungültig.", Toast.LENGTH_SHORT).show();
 		}
 		else {
 			boolean isPortrait = getSharedPreferences(Utilities.PIC_STORE, 0).getBoolean(albumName + ".portrait", true);
 			String videoUrl = "http://server.autburst.com/ApicAday/" + (isPortrait?"portrait":"landscape") + ".html?videoId=" + savedVideoId + "&album=" + albumName;
 
 			Log.d(TAG, "videoUrl for FB: " + videoUrl);
 
 			Intent intent = new Intent(GalleryActivity.this, FacebookActivity.class);
 			intent.putExtra("application_id", "126691144034061");
 			intent.putExtra("message", "I have created a video from my album: " + albumName);
 			intent.putExtra("link", videoUrl);
 			intent.putExtra("name", "A Picture A Day");
 			intent.putExtra("description", "");
 			intent.putExtra("picture", "https://github.com/images/modules/header/logov3.png");
 			startActivity(intent);
 
 		}
     }
     
     public void enablePlayButton() {
     	controlPanel.setVisibility(View.VISIBLE);
     	galleryImageView.setVisibility(View.GONE);
     }
     
     public void disablePlayButton() {
     	controlPanel.setVisibility(View.GONE);
     	galleryImageView.setVisibility(View.VISIBLE);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	new MenuInflater(this).inflate(R.menu.gallerymenu, menu);
     	return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     		case R.id.playMovieMenuItem: 
     			Intent intent = new Intent(this, PlayMovieActivity.class);
     			intent.putExtra("albumName", albumName);
     			startActivity(intent);
     			return true;
     	}
     	return super.onOptionsItemSelected(item);
     }
 }
