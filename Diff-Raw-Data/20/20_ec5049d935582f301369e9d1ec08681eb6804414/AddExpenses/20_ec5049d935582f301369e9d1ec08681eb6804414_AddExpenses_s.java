 package com.app.settleexpenses;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
import android.widget.TextView;
 import android.widget.Toast;
 
 import com.app.settleexpenses.domain.Expense;
 import com.app.settleexpenses.domain.Participant;
 import com.app.settleexpenses.service.DbAdapter;
 
 public class AddExpenses extends Activity {
 
 	private static final int PICK_CONTACT = 0;
 	private static final int PICK_PAID_BY = 1;
 
 	private EditText expenseTitleText;
 	private EditText expenseAmount;
 
 	private final Activity currentActivity = this;
 	private final ContactsAdapter contacts = new ContactsAdapter(this);
 
 	protected boolean[] selections;
 	protected boolean[] newSelections;
 	private ArrayList<String> allParticipantNames;
 	private List<Participant> allParticipants;
 	private Participant paidBy;
 
	private TextView participantsText;
 	private Button paidByButton;
 	private Button editParticipantList;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_expenses);
 		setTitle("Add Expense");
 
 		expenseTitleText = (EditText) findViewById(R.id.title);
 		expenseAmount = (EditText) findViewById(R.id.amount);
 
 		Button confirmButton = (Button) findViewById(R.id.confirm);
 		Button calculateButton = (Button) findViewById(R.id.calculate);
 		Button addParticipantButton = (Button) findViewById(R.id.add_participant);
 
 		final long eventId = getIntent().getLongExtra(DbAdapter.EVENT_ID, -1);
 		DbAdapter dbAdapter = new DbAdapter(this, contacts);
 		allParticipants = dbAdapter.getEventById(eventId).getParticipants();
 		allParticipantNames = participantNames(allParticipants);
 
 		selections = new boolean[allParticipants.size()];
 		for (int i = 0; i < allParticipants.size(); i++) {
 			selections[i] = false;
 		}
 
		participantsText = (TextView) findViewById(R.id.participants);
 		updateParticipantList();
 
 		editParticipantList = (Button) findViewById(R.id.edit_participants);
 		editParticipantList.setOnClickListener(new ButtonClickHandler());
 
 		if (allParticipants.size() == 0)
 			editParticipantList.setEnabled(false);
 
 		paidByButton = (Button) findViewById(R.id.paid_by);
 
 		confirmButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View view) {
 
 				if (isInValid())
 					return;
 
 				DbAdapter dbAdapter = new DbAdapter(view.getContext(), new ContactsAdapter(currentActivity));
 
 				float amount = Float.parseFloat(expenseAmount.getText().toString());
 				ArrayList<Participant> participants = selectedParticipants();
 
 				Expense expense = new Expense(expenseTitleText.getText()
 						.toString(), amount, eventId, paidBy, participants);
 
 				dbAdapter.createExpense(expense);
 				Toast toast = Toast.makeText(view.getContext(), getString(R.string.expense_created_successfully), 2);
 				toast.show();
 				finish();
 				startActivity(currentActivity.getIntent());
 			}
 
 		});
 
 		addParticipantButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(Intent.ACTION_PICK,
 						ContactsContract.Contacts.CONTENT_URI);
 				startActivityForResult(intent, PICK_CONTACT);
 			}
 		});
 
 		paidByButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(Intent.ACTION_PICK,
 						ContactsContract.Contacts.CONTENT_URI);
 				startActivityForResult(intent, PICK_PAID_BY);
 			}
 		});
 
 		calculateButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View view) {
 				Intent addExpensesIntent = new Intent(view.getContext(),
 						ShowSettlements.class);
 				addExpensesIntent.putExtra(DbAdapter.EVENT_ID, eventId);
 				startActivityForResult(addExpensesIntent, 1);
 			}
 		});
 	}
 
 	private boolean isInValid() {
 		if (isInValid(expenseTitleText.getText().toString())
 				|| isInValid(expenseAmount.getText().toString())) {
 			showMessage(getString(R.string.required_field_validation));
 			return true;
 		}
 		if (paidBy == null) {
 			showMessage(getString(R.string.payer_validation));
 			return true;
 		}
 
 		if (selectedParticipants().size() == 0) {
 			showMessage(getString(R.string.no_participant_validation));
 			return true;
 		}
 		return false;
 	}
 
 	private void showMessage(String message) {
 		Toast toast = Toast.makeText(currentActivity, message,
 				Toast.LENGTH_LONG);
 		toast.show();
 	}
 
 	private boolean isInValid(String value) {
 		return value != null && value.trim().length() == 0;
 	}
 
 	private ArrayList<String> participantNames(List<Participant> allParticipants) {
 		ArrayList<String> result = new ArrayList<String>();
 		for (Participant participant : allParticipants) {
 			result.add(participant.getName());
 		}
 		return result;
 	}
 
 	private ArrayList<Participant> selectedParticipants() {
 		ArrayList<Participant> participants = new ArrayList<Participant>();
 		for (int i = 0; i < selections.length; i++) {
 			if (selections[i]) {
 				participants.add(allParticipants.get(i));
 			}
 		}
 		return participants;
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Log.d("addExpense", allParticipantNames.toString());
 		String[] participants = allParticipantNames
 				.toArray(new String[allParticipantNames.size()]);
 		newSelections = selections;
 		return new AlertDialog.Builder(this).setTitle("Participants")
 				.setMultiChoiceItems(participants, newSelections,
 						new DialogInterface.OnMultiChoiceClickListener() {
 							public void onClick(
 									DialogInterface dialogInterface, int i,
 									boolean b) {
 							}
 						}).setPositiveButton("OK",
 						new DialogButtonClickHandler()).create();
 	}
 
 	private void updateParticipantList() {
		String selectedParticipants = participantNames(selectedParticipants())
				.toString();
		participantsText.setText(selectedParticipants.substring(1,
				selectedParticipants.length() - 1));
 	}
 
 	public class DialogButtonClickHandler implements
 			DialogInterface.OnClickListener {
 		public void onClick(DialogInterface dialog, int clicked) {
 			removeDialog(0);
 			switch (clicked) {
 			case DialogInterface.BUTTON_POSITIVE:
 				selections = newSelections;
 				editParticipantList.setEnabled(true);
 				editParticipantList.setText("Edit participants");
 				updateParticipantList();
 				break;
 			}
 		}
 	}
 
 	public class ButtonClickHandler implements View.OnClickListener {
 		public void onClick(View view) {
 			showDialog(0);
 		}
 	}
 
 	@Override
 	public void onActivityResult(int reqCode, int resultCode, Intent data) {
 		super.onActivityResult(reqCode, resultCode, data);
 
 		switch (reqCode) {
 		case (PICK_CONTACT):
 			if (resultCode == Activity.RESULT_OK) {
 				Participant participant = fetchParticipantFromResult(data);
 				if (participant != null) {
 					if (selectedParticipants().contains(participant)) {
 						Toast toast = Toast.makeText(currentActivity,
 								getString(R.string.duplicate_participant_validation),
 								Toast.LENGTH_LONG);
 						toast.show();
 					} else {
 						int index = allParticipants.indexOf(participant);
 						if (index >= 0) {
 							selections[index] = true;
 						} else {
 							allParticipantNames.add(participant.getName());
 							allParticipants.add(participant);
 							boolean[] result = new boolean[selections.length + 1];
 							System.arraycopy(selections, 0, result, 0,
 									selections.length);
 							result[result.length - 1] = true;
 							selections = result;
 							editParticipantList.setEnabled(true);
 							editParticipantList.setText("Edit participants");
 						}
 						updateParticipantList();
 					}
 				}
 			}
 			break;
 		case (PICK_PAID_BY):
 			if (resultCode == Activity.RESULT_OK) {
 				Participant participant = fetchParticipantFromResult(data);
 				if (participant != null) {
 					paidBy = participant;
 					paidByButton.setText(participant.getName());
 				}
 			}
 		}
 	}
 
 	private Participant fetchParticipantFromResult(Intent data) {
 		Uri contactData = data.getData();
 		Cursor c = managedQuery(contactData, null, null, null, null);
 		if (c.moveToFirst()) {
 			String name = c
 					.getString(c
 							.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
 			String id = c.getString(c
 					.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
 			return new Participant(id, name);
 		}
 		return null;
 	}
 }
