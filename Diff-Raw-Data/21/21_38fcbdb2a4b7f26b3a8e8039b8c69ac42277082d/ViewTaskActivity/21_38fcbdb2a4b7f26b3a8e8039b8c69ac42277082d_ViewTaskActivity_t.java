 package com.CMPUT301F12T07.crowdsource;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.CMPUT301F12T07.crowdsource.taskmodeldb.LocalDB;
 import com.CMPUT301F12T07.crowdsource.taskmodeldb.Task;
 
 public class ViewTaskActivity extends Activity {
 
 
 	private Task currentTask;
 	private TextView taskTitle;
 	private TextView startDate;
 	private TextView endDate;
 	private TextView taskVisibility;
 	private TextView taskDesc;
 	private TextView taskQuantity;
 	private LocalDB db;
 
 	private Button updateTask;
 	private Button deleteTask;
 	private Button fulfillTask;
 	
 	final Context context = this;
 	
 	private static final int RETURN_PHOTO_CODE = 1;
 	private static final int RETURN_AUDIO_CODE = 2;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_view_task);
         
         db = new LocalDB(this);
         this.currentTask = db.getTask(getIntent().getExtras().getInt("taskObject"));
         db.close();
         
         // Getting the task title field
         this.taskTitle = (TextView) findViewById(R.id.textViewTitle);
         // Getting the start Date field
         this.startDate = (TextView) findViewById(R.id.textViewCreatedDate);
         // Getting the end Date field
         this.endDate = (TextView) findViewById(R.id.textViewDueDate);
         // Getting the task visibility level
         this.taskVisibility = (TextView) findViewById(R.id.textViewVisibility);
         // Getting the task description field
         this.taskDesc = (TextView) findViewById(R.id.textViewDescription);
         // Getting the task quantity field
         this.taskQuantity = (TextView) findViewById(R.id.textViewQuantity);
         
         this.updateTask = (Button) findViewById(R.id.buttonUpdate);
         updateTask.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	Intent intent = new Intent(ViewTaskActivity.this, UpdateTaskActivity.class);
         		intent.putExtra("taskID", currentTask.get_tid());
         		startActivityForResult(intent,1);
             }
         });
         
         this.deleteTask = (Button) findViewById(R.id.buttonDelete);
         deleteTask.setOnClickListener(new OnClickListener() {
         	public void onClick(View v) {
         		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 
         		// set title
         		builder.setTitle("Delete");
 
         		// set dialog message
         		builder.setMessage("Are you sure you want to delete this task?");
         		builder.setCancelable(false);
         		builder.setPositiveButton(R.string.Delete, new DialogInterface.OnClickListener() {
         			public void onClick(DialogInterface dialog,int id) {
         				// if this button is clicked, delete the task and leave the activity
         				db.deleteTask(currentTask.get_tid());
         				finish();
         			}
         		});
         		builder.setNegativeButton(R.string.Cancel,new DialogInterface.OnClickListener() {
         			public void onClick(DialogInterface dialog,int id) {
         				// if this button is clicked, just close
         				// the dialog box and do nothing
         				dialog.cancel();
         			}
         		});
 
         		// create alert dialog
         		AlertDialog alertDialog = builder.create();
 
         		// show it
         		alertDialog.show();
         	}
 
         });
 
         this.fulfillTask = (Button) findViewById(R.id.buttonFulfill);
 //        fulfillTask.setOnClickListener(new OnClickListener() {
 //			public void onClick(View v) {
 //				AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
 //				builder.setMessage("Choose an option.");
 //				
 //				builder.setPositiveButton("Send/Fulfill Directly", 
 //						new DialogInterface.OnClickListener() {
 //							public void onClick(DialogInterface dialog, int which) {
 //								Intent intent = new Intent(ViewTaskActivity.this, EmailActivity.class);
 //								startActivity(intent);
 //							}
 //					});
 //				builder.setNeutralButton("Record Audio", 
 //						new DialogInterface.OnClickListener() {
 //							public void onClick(DialogInterface dialog, int which) {
 //								Intent intent = new Intent(ViewTaskActivity.this, RecordAudioActivity.class);
 //								startActivity(intent);
 //							}
 //					});
 //				builder.setNegativeButton("Take Photo", 
 //						new DialogInterface.OnClickListener() {
 //							public void onClick(DialogInterface dialog, int which) {
 //								Intent intent = new Intent(ViewTaskActivity.this, TakePhotoActivity.class);
 //								startActivity(intent);
 //							}
 //						});
 //				
 //				AlertDialog alert = builder.create();
 //				alert.show();
 //				
 //			}
 //        });
 //        
 
         fulfillTask.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
 				builder.setMessage("Choose an option.");
 				
 				final String type = currentTask.get_type();
 				String neutral = null;
 				
 				if (type.equals("Photo")) 		neutral = "Capture, and send";
 				else if (type.equals("Audio")) 	neutral = "Record, and send";
 				else 							neutral = "Send Text";
 				
 				if (type.equals("Photo")) {
 					builder.setPositiveButton("Choose, and send", 
 							new DialogInterface.OnClickListener() {
 						
 								public void onClick(DialogInterface dialog, int which) {
 									Intent intent = new Intent(ViewTaskActivity.this, ChoosePictureActivity.class);
 									startActivityForResult(intent,RETURN_PHOTO_CODE);
 								}
 								
 						});
 				}
 				
 				builder.setNeutralButton(neutral, 
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 								//audio/photo/text --> email
 								// if (type) elseif() else()
 								if (type.equals("Photo")) {
 									Intent intent = new Intent(ViewTaskActivity.this, TakePhotoActivity.class);
									//startActivity(intent);
									startActivityForResult(intent,RETURN_PHOTO_CODE);
 								} else if (type.equals("Audio")) {
 									Intent intent = new Intent(ViewTaskActivity.this, RecordAudioActivity.class);
 //									startActivity(intent);
 									
 									startActivityForResult(intent,RETURN_AUDIO_CODE);
 								} else {
 									Intent intent = new Intent(ViewTaskActivity.this, EmailActivity.class);
 									intent.putExtra("type",	type);
 									intent.putExtra("data", "n/a");
 									startActivity(intent);
 								}
 							}
 					});
 				builder.setNegativeButton("Cancel", 
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 								dialog.cancel();
 							}
 						});
 				
 				AlertDialog alert = builder.create();
 				alert.show();
 				
 			}
         });
         
         
         
         ImageView imageName = (ImageView) findViewById(R.id.imageView1);
         
         if (currentTask.get_type().equals("Audio")){
         	imageName.setImageResource(R.drawable.ic_tasktype_audio_lg);
         } else if (currentTask.get_type().equals("Photo")){
         	imageName.setImageResource(R.drawable.ic_tasktype_photo_lg);
         } else {
         	imageName.setImageResource(R.drawable.ic_tasktype_text_lg);
         }
     }
 
     private void sendMedia(String type, String data) {
     	// data can be converted to Uri
 		Intent intent = new Intent(ViewTaskActivity.this, EmailActivity.class);
 		intent.putExtra("type",	type);
 		intent.putExtra("data", data);
 		startActivity(intent);
     }
     
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
     	super.onActivityResult(requestCode, resultCode, data);
     	
     	if (resultCode != RESULT_OK) finish();
     	
     	if (data.getStringExtra("result").compareTo("fail") == 0) finish();
     	
     	Log.v("result", ""+resultCode);
     	Log.v("result", "result");
     	
     	switch (requestCode) {
     		case RETURN_PHOTO_CODE:
     			String image = data.getStringExtra("Photo");
     			Log.v("switch", "switch");
     			sendMedia("Photo", image);
     			
     			break;
     		
     		case RETURN_AUDIO_CODE:
     			Log.v("switch", "audio");
     			String audio = data.getStringExtra("Audio");
     			sendMedia("Audio", audio);
     			
     			break;
 
     		default:
     			Log.v("Error", "default case in ViewTaskActivity");
     	}
     }
     
     @Override
     public void onResume() {
     	super.onResume();
 
     	this.currentTask = db.getTask(getIntent().getExtras().getInt("taskObject"));
     	
     	taskTitle.setText(currentTask.get_title());
     	startDate.setText(currentTask.get_dateCreate());
     	endDate.setText(currentTask.get_dateDue());
     	taskQuantity.setText(Integer.toString(currentTask.get_quantity()));
     	if(currentTask.get_visibility() == 1)
     		taskVisibility.setText("Private");
     	else
     		taskVisibility.setText("Public");
     	taskDesc.setText(currentTask.get_description());
     }
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	getMenuInflater().inflate(R.menu.activity_view_task, menu);
     	return true;
     }
 }
