 package ca.ualberta.cmput301project;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class FulfillTask extends Activity implements OnClickListener {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_fulfilltask);
 
         Task oldtask = (Task) getIntent().getSerializableExtra("task");
         
         TextView requirements = (TextView) findViewById(R.id.question_text);
         requirements.setText(oldtask.getDescription());
         
         //DEBUG: answer is saved
         EditText answerBox = (EditText) findViewById(R.id.answer_text);
         answerBox.setText(oldtask.getResAnswer());
         //Note from Gabe: this commented part below was added before a commit I made that just obtains a task
         //from the caller; I commented it so that whoever made this change didn't lose their progress!
 
         //If image/audio required, make button clickable.
         boolean requestPhotos = oldtask.getReqPhoto();
         boolean requestAudio = oldtask.getReqAudio();
 
         Button photoButton = (Button) findViewById(R.id.get_image);
<<<<<<< HEAD
         Button audioButton = (Button) findViewById(R.id.get_audio);
         Button doneButton = (Button) findViewById(R.id.taskdone);
         Button draftButton = (Button) findViewById(R.id.save_draft);
=======
         //Button audioButton = (Button) findViewById(R.id.get_audio);
>>>>>>> 6fca6961ce10d9acaf18d8d8246569422d8f3b84
         photoButton.setClickable(false);
         //audioButton.setClickable(false);
         //Logic to gray-out Button so it's non-selectable
         if (requestPhotos){
         	photoButton.setOnClickListener(this);
         	photoButton.setClickable(true);
         } else {
         	photoButton.setTextColor(getResources().getColor(R.color.White));
         }
         if (requestAudio){
         //	audioButton.setOnClickListener(this);
         //	audioButton.setClickable(true);
         } else {
<<<<<<< HEAD
         	audioButton.setTextColor(getResources().getColor(R.color.White));
         }
        //Butons to exit activity
         doneButton.setOnClickListener(this);
         draftButton.setOnClickListener(this);
=======
        //	audioButton.setTextColor(getResources().getColor(R.color.White));
        } 
>>>>>>> 6fca6961ce10d9acaf18d8d8246569422d8f3b84
     }
     
     public void onClick(View v){
     	//Note from Gabe to Simon/whoever works on this: because of the way that saving serialized objects works,
     	//there is no possible way to add an object to the middle of a file that contains other objects. so if
     	//a user opens a local task, modifies it, and then saves the result, the object would have to be first
     	//deleted and then saved. please ensure that, whenever a user wants to save their progress, there is a
     	//deleteLocalTask(task) call followed by a saveLocalTask(task) call. this will change the order of tasks
     	//in the ViewLocalTask activity, but there's no way to get around it :/
     	Task oldtask = (Task) getIntent().getSerializableExtra("task");
     	String photofile = oldtask.getResPhotoName();
     	String audiofile = oldtask.getResAudioName();
     	
     	Task newtask = oldtask.cloneTask();
     	
     	Intent intent;
     	switch (v.getId()){
     		case R.id.get_audio:
     			intent = new Intent();
     			startActivity(intent);
     			break;
     		case R.id.get_image:
     			intent = new Intent(this, TakePhoto.class);
     			startActivity(intent);
     			break;
     		case R.id.taskdone:
     			EditText answerBox = (EditText) findViewById(R.id.answer_text);
     			String answer = answerBox.getText().toString();
     			newtask.setComplete(true);
    			newtask.setResult(answer, "gfgd", "dhdfhfh");
     			LocalTaskManager.replaceLocalTask(oldtask, newtask, this);
     			//LocalTaskManager.saveLocalTask(newtask, this);
     			finish();
     		case R.id.save_draft:
     			finish();
     	}
     	
     }
 }
