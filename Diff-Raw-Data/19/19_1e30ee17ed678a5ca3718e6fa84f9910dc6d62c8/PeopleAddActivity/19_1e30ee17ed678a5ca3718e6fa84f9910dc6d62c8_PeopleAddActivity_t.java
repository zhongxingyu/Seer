 package com.caseynbrown.moneymanager;
 
 import static com.caseynbrown.moneymanager.ConstantsDB.AMOUNT_PEOPLE;
 import static com.caseynbrown.moneymanager.ConstantsDB.NAME_PEOPLE;
 import static com.caseynbrown.moneymanager.ConstantsDB.TABLE_NAME_PEOPLE;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PeopleAddActivity extends Activity {
 	private DBData db;
 	private EditText nameEdit, amountEdit;
 	private Button doneButton;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.peopleadd);
 
 		this.nameEdit = ((EditText) findViewById(R.id.peopleaddName));
 		this.amountEdit = ((EditText) findViewById(R.id.peopleaddBal));
 		this.doneButton = ((Button) findViewById(R.id.peopleAddDone));
 
 		this.db = new DBData(this);
 
 		this.doneButton.setOnClickListener(new Button.OnClickListener() {
 
 			@Override
 			/*
 			 * If user clicks on Done, check for input. If no input exists, show
 			 * error toast Otherwise, add a new user to the DB and return to the
 			 * previous activity
 			 */
 			public void onClick(View v) {
 				if (emptyName()) {
 					makeToastAdd();
 				} else {
 					String amountVal = amountEdit.getText().toString();
 					int newAmount;
 					if (amountVal.equals("")) {
 						newAmount = HelperMethods.dollarToInt("+0");

 					} else {
 						newAmount = HelperMethods.dollarToInt(amountVal);
 					}
 
 					addUser(nameEdit.getText().toString(), newAmount);
 					Intent returnIntent = new Intent();
 					setResult(Activity.RESULT_OK, returnIntent);
 					finish();
 				}
 			}
 		});
 
 		/* Display modal amount pop up when user clicks the amount box */
 		this.amountEdit.setOnClickListener(new Button.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showModal();
 			}
 		});
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		/* Close DB connection */
 		this.db.close();
 	}
 
 	/* Displays a Modal amount box */
 	public void showModal() {
 		ModalAmount d = new ModalAmount(this, new OnReadyListener());
 		d.show();
 	}
 
 	/* Get the amount back from the Modal Listener */
 	private class OnReadyListener implements ModalAmount.ReadyListener {
 
 		@Override
 		public void ready(boolean negative, String amount) {
 			HelperMethods.updateAmountBox(amountEdit, negative, amount);
 		}
 	}
 
 	/* Add the new user to the DB */
 	private void addUser(String name, int amount) {
 		SQLiteDatabase writable = db.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(NAME_PEOPLE, name);
 		values.put(AMOUNT_PEOPLE, amount);
 		writable.insertOrThrow(TABLE_NAME_PEOPLE, null, values);
 	}
 
 	/* Check if the name input box is empty */
 	private boolean emptyName() {
 		if (this.nameEdit.getText().toString().equals("")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/* Display error toast */
 	private void makeToastAdd() {
 		String errorMessage = "Please provide a Name";
 		Toast t = new Toast(this);
 		TextView error = new TextView(this);
 		error.setText(errorMessage);
 		error.setTextColor(Color.WHITE);
 		error.setBackgroundColor(Color.BLACK);
 		t.setView(error);
 		t.show();
 	}
 }
