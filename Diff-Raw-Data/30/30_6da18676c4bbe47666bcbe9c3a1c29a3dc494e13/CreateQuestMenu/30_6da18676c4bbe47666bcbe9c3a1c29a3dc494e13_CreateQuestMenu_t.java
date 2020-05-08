 package rpisdd.rpgme.activities;
 
 import org.joda.time.DateTime;
 import org.joda.time.ReadableDateTime;
 import org.joda.time.format.DateTimeFormat;
 
 import rpisdd.rpgme.R;
 import rpisdd.rpgme.gamelogic.player.Player;
 import rpisdd.rpgme.gamelogic.player.StatType;
 import rpisdd.rpgme.gamelogic.quests.DateFormatter;
 import rpisdd.rpgme.gamelogic.quests.Quest;
 import rpisdd.rpgme.gamelogic.quests.QuestDifficulty;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
import android.widget.ScrollView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 public class CreateQuestMenu extends Fragment implements OnClickListener {
 	
	private final String noDateSet = "(Not Set)";
 	
	private Button confirmCreate;
	private Button cancel;
	private Button clearDateTime;
	private Button changeDateTime;
	
	private TextView deadlineView;
 
	private DateTime deadline;
 	
 	public CreateQuestMenu(){}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// Inflate the layout for this fragment
 
 		View v = inflater.inflate(R.layout.create_quest_menu, container, false);
 
 		confirmCreate = (Button) v.findViewById(R.id.confirmCreateQuest);
 		confirmCreate.setOnClickListener(this);
 		cancel = (Button) v.findViewById(R.id.cancelCreateQuest);
 		cancel.setOnClickListener(this);
 		clearDateTime = (Button) v.findViewById(R.id.removeQuestDeadline);
 		clearDateTime.setOnClickListener(this);
 		changeDateTime = (Button) v.findViewById(R.id.changeQuestDeadline);
 		changeDateTime.setOnClickListener(this);
 		deadlineView = (TextView) v.findViewById(R.id.createQuestDeadline);
 		return v;
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.confirmCreateQuest: {
 			// Verified quest: Switch back to Quest Menu
 			if (verifyAndCreate()) {
 				((MainActivity) getActivity()).changeFragment(new QuestMenu());
 			}
			//Scroll the view all the way up so user sees the error
			((ScrollView)getView().findViewById(R.id.createQuestScrollView)).smoothScrollTo(0,0);
 			break;
 		}
 		case R.id.cancelCreateQuest: {
 			((MainActivity) getActivity()).changeFragment(new QuestMenu());
 			break;
 		}
 		case R.id.removeQuestDeadline: {
			deadlineView.setText(noDateSet);
 			break;
 		}
 		case R.id.changeQuestDeadline: {
 			setDeadlinePopup();
 			break;
 		}
 		default:
 			break;
 		}
 	}
 
 	public void setDeadlinePopup() {
 
 		AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
 		builder1.setMessage("Set Deadline");
 		builder1.setCancelable(true);
 		View changeDeadline = LayoutInflater.from(getActivity()).inflate(
 				R.layout.datetime_chooser, null);
 
 		final DatePicker datePicker = (DatePicker) changeDeadline
 				.findViewById(R.id.questDeadlineDatePicker);
 		final TimePicker timePicker = (TimePicker) changeDeadline
 				.findViewById(R.id.questDeadlineTimePicker);
 
 		builder1.setView(changeDeadline);
 
 		builder1.setPositiveButton("Set",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 
 						// Note: 0 is January, 11 is December
 						deadline = new DateTime(datePicker.getYear(),
 								datePicker.getMonth() + 1, datePicker
 										.getDayOfMonth(), timePicker
 										.getCurrentHour(), timePicker
 										.getCurrentMinute());
 
 						// Log.i("Debug","Set is " +
 						// DateTimeFormat.forPattern(getResources().getString(R.string.dateFormat)).print(date));
 						// Log.i("Debug","Now is " +
 						// DateTimeFormat.forPattern(getResources().getString(R.string.dateFormat)).print(DateTime.now()));
 
 						if (isBadTime(deadline)) {
 							badTimePopup();
 						} else {
 							deadlineView.setText(DateFormatter.formatDate(deadline));
 							dialog.cancel();
 						}
 					}
 				});
 
 		builder1.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 
 		AlertDialog alert11 = builder1.create();
 		alert11.show();
 	}
 
 	public boolean isBadTime(DateTime date) {
 		return date != null && date.isBefore(DateTime.now());
 	}
 
 	public void badTimePopup() {
 
 		// Bring up a confirmation prompt first
 		AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
 		builder2.setMessage("Error: The deadline you entered has already passed!");
 		builder2.setCancelable(true);
 		builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 			}
 		});
 
 		AlertDialog alert2 = builder2.create();
 		alert2.show();
 	}
 
 	// Return true if the quest was verified, false otherwise.
 	public boolean verifyAndCreate() {
 
 		Player player = Player.getPlayer();
 
 		EditText name = (EditText) getView().findViewById(R.id.enterQuestName);
 		EditText desc = (EditText) getView().findViewById(R.id.enterQuestDesc);
 		Spinner tag = (Spinner) getView().findViewById(R.id.statDropDown);
 		Spinner diff = (Spinner) getView()
 				.findViewById(R.id.difficultyDropDown);
 
 		if (name.getText().toString().equals("")) {
 			TextView error = (TextView) getView().findViewById(
 					R.id.createQuestError);
 			error.setText("Error: Enter in a name");
 			error.setVisibility(View.VISIBLE);
 			return false;
 		} else if (player.questManager.isNameUsed(name.getText().toString())) {
 			TextView error = (TextView) getView().findViewById(
 					R.id.createQuestError);
 			error.setVisibility(View.VISIBLE);
 			error.setText("Error: That name is used by another quest");
 			return false;
		} else if ( !((String)deadlineView.getText()).equals(noDateSet) && isBadTime(deadline)) {
 			badTimePopup();
 			return false;
 		}
 
 		StatType type = StatType.stringToType(tag.getSelectedItem().toString());
 		QuestDifficulty quest_diff = QuestDifficulty.stringToDifficulty(diff
 				.getSelectedItem().toString());
 		String deadlineStr = (String) deadlineView.getText();
 
 		Quest newQuest = null;
 
		if (deadlineStr.equals(noDateSet)) {
 			newQuest = new Quest(name.getText().toString(), desc.getText()
 					.toString(), quest_diff, type);
 			Log.i("Debug", "Created non-timed quest");
 		} else {
 			newQuest = new Quest(name.getText().toString(), desc.getText()
 					.toString(), quest_diff, type, deadline);
 			Log.i("Debug", "Created timed quest with time " + deadline);
 		}
 
 		player.questManager.addQuest(newQuest);
 
 		return true;
 
 	}
 }
