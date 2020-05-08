 package com.CMPUT301F12T07.crowdsource.viewupdatetask;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.CMPUT301F12T07.crowdsource.ChoosePictureActivity;
 import com.CMPUT301F12T07.crowdsource.EmailActivity;
 import com.CMPUT301F12T07.crowdsource.R;
 import com.CMPUT301F12T07.crowdsource.RecordAudioActivity;
 import com.CMPUT301F12T07.crowdsource.TakePhotoActivity;
 import com.CMPUT301F12T07.crowdsource.taskmodeldb.DBHandler;
 import com.CMPUT301F12T07.crowdsource.taskmodeldb.Task;
 
 public class ViewOtherTaskActivity extends Activity {
 
 
 	private Task currentTask;
 	private TextView taskTitle;
 	private TextView startDate;
 	private TextView endDate;
 	private TextView taskVisibility;
 	private TextView taskDesc;
 	private TextView taskQuantity;
 	private TextView taskFollowers;
 	private DBHandler db;
 
 	private Button followTask;
 	private Button fulfillTask;
 	
 	final Context context = this;
 	
 	private static final int RETURN_PHOTO_CODE = 1;
 	private static final int RETURN_AUDIO_CODE = 2;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_view_other_task);
         
         db = new DBHandler(this);
         Long taskID = getIntent().getExtras().getLong("taskID");
 		this.currentTask = db.getTask(taskID.toString(), DBHandler.LOCAL_FLAG);
         
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
         // Getting the task followers field
         this.taskFollowers = (TextView) findViewById(R.id.textViewFollowers);
         
         this.followTask = (Button) findViewById(R.id.buttonFollow);
         if(currentTask.get_followed() == 1) {
         	followTask.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_button_followed, 0, 0, 0);
         }
         else {
         	followTask.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_button_unfollowed, 0, 0, 0);
         }
         
         
         launchListeners();
         
 		ImageView imageName = (ImageView) findViewById(R.id.imageView1);
         
         if (currentTask.get_type().equals("Audio")){
         	imageName.setImageResource(R.drawable.ic_tasktype_audio_lg);
         } else if (currentTask.get_type().equals("Photo")){
         	imageName.setImageResource(R.drawable.ic_tasktype_photo_lg);
         } else {
         	imageName.setImageResource(R.drawable.ic_tasktype_text_lg);
         }
     }
 
 	private void launchListeners()
 	{
 
 		followTask.setOnClickListener(new View.OnClickListener()
 		{
 
 			public void onClick(View v)
 			{
 
 				if (currentTask.get_followed() == 0)
 				{
 					followTask.setCompoundDrawablesWithIntrinsicBounds(
 							R.drawable.ic_button_followed, 0, 0, 0);
 					currentTask.set_followed(1);
 					currentTask.set_num_followed(currentTask.get_num_followed() + 1);
 				} else
 				{
 					followTask.setCompoundDrawablesWithIntrinsicBounds(
 							R.drawable.ic_button_unfollowed, 0, 0, 0);
 					currentTask.set_followed(0);
 					currentTask.set_num_followed(currentTask.get_num_followed() - 1);
 				}
 			}
 		});
 		this.fulfillTask = (Button) findViewById(R.id.buttonFulfill);
 		fulfillTask.setOnClickListener(new OnClickListener()
 		{
 
 			public void onClick(View v)
 			{
 
 				AlertDialog.Builder builder = new AlertDialog.Builder(v
 						.getContext());
 				builder.setMessage("Choose an option.");
 				final String type = currentTask.get_type();
 				String neutral = null;
 				if (type.equals("Photo"))
 					neutral = "Capture, and send";
 				else if (type.equals("Audio"))
 					neutral = "Record, and send";
 				else
 					neutral = "Send Text";
 				if (type.equals("Photo"))
 				{
 					builder.setPositiveButton("Choose, and send",
 							new DialogInterface.OnClickListener()
 							{
 
 								public void onClick(DialogInterface dialog,
 										int which)
 								{
 
 									Intent intent = new Intent(
 											ViewOtherTaskActivity.this,
 											ChoosePictureActivity.class);
 									startActivityForResult(intent,
 											RETURN_PHOTO_CODE);
 								}
 							});
 				}
 				builder.setNeutralButton(neutral,
 						new DialogInterface.OnClickListener()
 						{
 
 							public void onClick(DialogInterface dialog,
 									int which)
 							{
 
 								if (type.equals("Photo"))
 								{
 									Intent intent = new Intent(
 											ViewOtherTaskActivity.this,
 											TakePhotoActivity.class);
 									startActivityForResult(intent,
 											RETURN_PHOTO_CODE);
 								} else if (type.equals("Audio"))
 								{
 									Intent intent = new Intent(
 											ViewOtherTaskActivity.this,
 											RecordAudioActivity.class);
 									startActivityForResult(intent,
 											RETURN_AUDIO_CODE);
 								} else
 								{
 									Intent intent = new Intent(
 											ViewOtherTaskActivity.this,
 											EmailActivity.class);
 									intent.putExtra("type", type);
 									intent.putExtra("data", "n/a");
 									currentTask.set_followed(1);
 									currentTask.set_num_followed(currentTask
 											.get_num_followed() + 1);
 									startActivity(intent);
 								}
 							}
 						});
 				builder.setNegativeButton("Cancel",
 						new DialogInterface.OnClickListener()
 						{
 
 							public void onClick(DialogInterface dialog,
 									int which)
 							{
 
 								dialog.cancel();
 							}
 						});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}
 		});
 	}
 
     /**
      * Takes input type and fulfillment and transfers its data to EmailActivity
      * 
      * @param type	The type of the media. (Audio, Photo)
      * @param data	The Uri data in string form
      */
     private void sendMedia(String type, String data) {
 		Intent intent = new Intent(ViewOtherTaskActivity.this, EmailActivity.class);
 		
 		
 		intent.putExtra("email", currentTask.get_user_email());
 		
 		intent.putExtra("type",	type);
 		intent.putExtra("data", data);
 		startActivity(intent);
     }
     
     
     /**
      * This method is only called when a fulfillment activity is activated.
      * This method either captures RESULT_OK signal sends the data and type to 
      * sendMedia, or it captures an RESULT_CANCEL and just returns and does nothing.
      */
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
     	super.onActivityResult(requestCode, resultCode, data);
     	
     	try{
 	    	if (resultCode == RESULT_CANCELED) {finish(); return;}
 	    	
 	    	setFollowed();
 	    	
 			switch (requestCode) {
 	    		case RETURN_PHOTO_CODE:
 	    			String image = data.getStringExtra("Photo");
 	    			sendMedia("Photo", image);
 	    			break;
 	    		
 	    		case RETURN_AUDIO_CODE:
 	    			String audio = data.getStringExtra("Audio");
 	    			sendMedia("Audio", audio);
 	    			break;
 	
 	    		default:
 	    			Log.v("default", "default case in ViewTaskActivity");
 	    	}
     	} catch (Exception e) {
     		Log.v("ViewTaskActivity", "Error in ViewTaskActivity");
     	}
     }
 
 	private void setFollowed()
 	{
 		currentTask.set_followed(1);
 		currentTask.set_num_followed(currentTask.get_num_followed() + 1);
 	}
 
     @Override
     public void onResume() {
     	super.onResume();
 
     	Long taskID = getIntent().getExtras().getLong("taskID");
 		this.currentTask = db.getTask(taskID.toString(), DBHandler.LOCAL_FLAG);
     	
     	taskTitle.setText(currentTask.get_title());
     	startDate.setText(currentTask.get_dateCreate());
     	endDate.setText(currentTask.get_dateDue());
     	taskQuantity.setText(Integer.toString(currentTask.get_qty_filled())+" of "+Integer.toString(currentTask.get_quantity())+" fulfilled");
     	taskFollowers.setText(Integer.toString(currentTask.get_num_followed()));
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
