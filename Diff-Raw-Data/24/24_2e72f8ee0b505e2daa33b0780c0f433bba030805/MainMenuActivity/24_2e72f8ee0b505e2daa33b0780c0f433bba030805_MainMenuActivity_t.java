 package com.example.financialrecorder;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class MainMenuActivity extends Activity implements OnClickListener {
 
 	private static final int REQUEST_CODE_CHANGE_BUTTON = 1;
 
 	private Button mRoommateButton;
 	private Button mExpenseButton;
 	private Button mReminderButton;
	private Button mLogoutButton;
 	private final String LLOM = "LLOM";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main_menu);
 
 		mRoommateButton = (Button) findViewById(R.id.roommates_button);
 		mExpenseButton = (Button) findViewById(R.id.expenses_button);
 		mReminderButton = (Button) findViewById(R.id.reminders_button);
		mLogoutButton = (Button) findViewById(R.id.logout_button);
		
 		mRoommateButton.setOnClickListener(this);
 		mExpenseButton.setOnClickListener(this);
 		mReminderButton.setOnClickListener(this);
		mLogoutButton.setOnClickListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == R.id.roommates_button) {
 			Log.d(LLOM, "Roommate button clicked");
 			Intent roommateButtonIntent = new Intent(this,
 					RoommateActivity.class);
 			// changeNBIntent.putExtra();
 			startActivityForResult(roommateButtonIntent,
 					REQUEST_CODE_CHANGE_BUTTON);
 		} else if (v.getId() == R.id.expenses_button) {
 			Log.d(LLOM, "Expense button clicked");
 			Intent expenseButtonIntent = new Intent(this,
 					ExpenseActivity.class);
 			// changeNBIntent.putExtra();
 			startActivityForResult(expenseButtonIntent,
 					REQUEST_CODE_CHANGE_BUTTON);
 		} else if (v.getId() == R.id.reminders_button) {
 			Log.d(LLOM, "Reminder button clicked");
 			Intent reminderButtonIntent = new Intent(this,
 					RemindersActivity.class);
 			// changeNBIntent.putExtra();
 			startActivityForResult(reminderButtonIntent,
 					REQUEST_CODE_CHANGE_BUTTON);
		} else if (v.getId() == R.id.logout_button) {
			Log.d(LLOM, "Logout button clicked");
			Intent result = new Intent();
			setResult(Activity.RESULT_OK, result); 
			finish();
 		} else {
 			Log.d(LLOM, "Wat");
 		}
 
 	}
 
 }
