 /**
  * @author Peter Maidens
  * 
  * This Activity is the activity that sends out an email to a entered email.
  * When this activity is started, it must have an activity that contains the Recipe
  * that the user desires to email. The intent that is passed to SendEmailActivity
  * MUST CONTAINT a recipe that has been turned to a string WITH THE KEY="recipe".
  * If there is no key="recipe", the Activity will send an email with an error message
  * in the text area.
  * 
  * NOTE: THIS ACTIVITY WILL BE UNABLE TO SEND AN EMAIL IN THE EMULATOR! IT MUST BE
  * 			RUN ON A REAL ANDROID DEVICE. This is because it uses an external email
  * 			client to send the email.
  */
 
 
 package ca.ualberta.cs.oneclick_cookbook;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class SendEmailActivity extends Activity {
 	
 	String recipeString;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_send_email);
 		
 		Intent intent = getIntent();
 		Bundle b = intent.getExtras();
		if(b!=null){
			this.recipeString= (String) b.getString("recipe", "There seems to be an error... " +
 					"Your friend wanted to send you a delicious recipe, but something along " +
 					"the way has gone wrong. OneClick Cookbook is sorry for the inconvenience. " +
 					"Please feel free to contact OneClick Cookbook Support at: " +
					"cookbooksupport@oneclick.com");
		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.send_email, menu);
 		return true;
 	}
 
 	/**
 	 * Function that handles and directs clicks from the user.
 	 * 
 	 * @param v
 	 *            The view of the button that was clicked
 	 */
 	public void clickHandler(View v) {
 		switch (v.getId()) {
 		case R.id.bEmailReturn:
 			finish();
 			break;
 		case R.id.bEmailSend:
 			EditText emailTo = (EditText) findViewById(R.id.tEmailSend);
 			if(emailTo.getText().toString()!=null){
 				sendEmail(recipeString);
 			}
 			break;
 		}
 	}
 	
 	/**
 	 * sendMail(String) is responsible for sending the email to the inputed email. It
 	 * 	relies on an external email client to actually send the email, but it will insert
 	 * 	all the fields into their appropriate place and the user just needs to hit "Send".
 	 * 
 	 * @param r A passed String of the recipe that will be sent via email.
 	 */
 	private void sendEmail(String r){
 		Intent email = new Intent(Intent.ACTION_SEND);
 		EditText emailTo = (EditText) findViewById(R.id.tEmailSend);
 		String emailString = emailTo.getText().toString();
 		email.putExtra(Intent.EXTRA_EMAIL, new String[]{emailString});
		email.putExtra(Intent.EXTRA_SUBJECT, new String[]{"Your Friend has Sent You a Recipe!"});
 		email.putExtra(Intent.EXTRA_TEXT, new String[]{r});
 		email.setType("message/rfc822");
 		try{
 			startActivity(Intent.createChooser(email, "Choose an Email App"));
 		}catch(android.content.ActivityNotFoundException e){
 			Toast.makeText(SendEmailActivity.this, "No Email Clients Found", Toast.LENGTH_SHORT).show();
 			}
 	}
 
 }
