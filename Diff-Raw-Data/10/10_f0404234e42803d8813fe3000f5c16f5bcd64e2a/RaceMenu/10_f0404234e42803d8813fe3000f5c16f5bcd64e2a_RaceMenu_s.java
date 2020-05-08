 package com.yarakyo.cadebuildorders;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.yarakyo.cadebuildorders.ActionList.Race;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.text.Editable;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.AbsListView.LayoutParams;
 
 public class RaceMenu extends Activity {
 
 	// UI variables
 	RadioGroup radioGroupBuild;
 	ScrollView scrollViewBuilds;
 	LinearLayout scrollViewLinearLayoutVertical;
 	TextView textViewBuilds;
 	Button buttonAddBuild;
 	Button buttonEditBuild;
 	Button buttonDeleteBuild;
 	Button buttonBack;
 	Button buttonRun;
 	String currentRace;
 
 	// Build Variables
 	List<ActionList> allBuilds;
 	List<ActionList> selectedBuilds;
 	String race;
 
 	// Setters
 
 	public void setAllBuildsList(List<ActionList> allBuilds) {
 		this.allBuilds = allBuilds;
 	}
 
 	public void setSelectedBuildsList(List<ActionList> selectedBuilds) {
 		this.selectedBuilds = selectedBuilds;
 	}
 
 	public void setRadioGroup(RadioGroup radioGroupBuild) {
 		this.radioGroupBuild = radioGroupBuild;
 	}
 
 	// Getters
 	public RadioGroup getRadioGroup() {
 		return this.radioGroupBuild;
 	}
 
 	public String getRace() {
 		return this.race;
 	}
 
 	public List<ActionList> getAllBuildsList() {
 		return this.allBuilds;
 	}
 
 	public List<ActionList> getSelectedBuildsList() {
 		return this.selectedBuilds;
 	}
 
 	private String convertRaceToRaceString(Race race) {
 		String raceString = "";
 		if (race.equals(Race.Terran))
 			raceString = "Terran";
 		if (race.equals(Race.Protoss))
 			raceString = "Protoss";
 		if (race.equals(Race.Zerg))
 			raceString = "Zerg";
 		return raceString;
 	}
 
 	private void populateBuildsForThisMatchUp() {
 		try {
 			FileInputStream fis = openFileInput("saveFile");
 			ObjectInputStream in = new ObjectInputStream(fis);
 			List<ActionList> tempAllBuilds = (List<ActionList>) in.readObject();
 			List<ActionList> selectedBuilds = new ArrayList<ActionList>();
 			for (ActionList checkTheRace : tempAllBuilds) {
 				if (convertRaceToRaceString(checkTheRace.getRace()).equals(
 						this.race)) {
 					selectedBuilds.add(checkTheRace);
 				}
 			}
 			setSelectedBuildsList(selectedBuilds);
 			setAllBuildsList(tempAllBuilds);
 			in.close();
 		} catch (Exception e) {
 
 		}
 
 	}
 
 	private void saveBuildsToFile() {
 		try {
 			boolean fileDeleted = deleteFile("saveFile");
 			if (fileDeleted == true) {
 				FileOutputStream fos = openFileOutput("saveFile",
 						Context.MODE_WORLD_WRITEABLE);
 				ObjectOutputStream out = new ObjectOutputStream(fos);
 				out.writeObject(allBuilds);
 				out.flush();
 				out.close();
 			}
 		} catch (Exception e) {
 
 		}
 	}
 
 	// Methods
 	private void setUpTextViews() {
 		Intent race = getIntent();
 		String raceName = race.getStringExtra("race");
 		currentRace = raceName;
 		textViewBuilds = (TextView) findViewById(R.id.textViewBuilds);
 		textViewBuilds.setText(raceName + " Builds");
 	}
 
 	private void deleteBuild() {
 		// Get build to delete from selected then find hash
 		RadioGroup temp = RaceMenu.this.getRadioGroup();
 		int radioButtonDeleteId = temp.getCheckedRadioButtonId();
 		ActionList toDelete = selectedBuilds.get(radioButtonDeleteId);
 		String hashToDelete = toDelete.getHash();
 
 		// Delete the build from all builds by skipping over it when making new
 		// list
 		List<ActionList> newBOList = new ArrayList<ActionList>();
 		for (ActionList tempActionList : allBuilds) {
 			if (tempActionList.getHash().equals(hashToDelete)) {
 				continue;
 			}
 			newBOList.add(tempActionList);
 		}
 
 		setAllBuildsList(newBOList);
 
 		// Clear Radio Selected
 		temp = RaceMenu.this.getRadioGroup();
 		temp.check(-1);
 		RaceMenu.this.setRadioGroup(temp);
 
 		// Save to new list to file
 		saveBuildsToFile();
 		refreshRadioGroup();
 	}
 
 	private Intent selectBuildForEdit(int editBuildID) {
 		// Pack it
 		List<ActionList> tempSelectedBuilds = getSelectedBuildsList();
 		ActionList passedBuild = tempSelectedBuilds.get(editBuildID);
 		Bundle bundle = new Bundle();
 		bundle.putSerializable("BuildOrder", passedBuild);
 
 		Intent editBuild = new Intent(RaceMenu.this, Build.class);
 		editBuild.putExtra("BuildName", passedBuild.getBuildName().toString());
 		editBuild.putExtra("IntentType", "EditBuild");
 		editBuild.putExtra("race", getRace());
 		editBuild.putExtras(bundle);
 		return editBuild;
 	}
 
 	private Intent selectBuildForRun(int runBuildID) {
 		List<ActionList> tempSelectedBuilds = getSelectedBuildsList();
 		ActionList passedBuild = tempSelectedBuilds.get(runBuildID);
 		Bundle bundle = new Bundle();
 		bundle.putSerializable("RunBuild", passedBuild);
 
 		Intent runIntent = new Intent(RaceMenu.this, RunBuild.class);
 		runIntent.putExtras(bundle);
 		runIntent.putExtra("BuildName", passedBuild.getBuildName().toString());
 
 		return runIntent;
 	}
 
 	private boolean checkIfBuildIsSelected() {
 		boolean selected = false;
 		RadioGroup temp = getRadioGroup();
 		int selectedID = temp.getCheckedRadioButtonId();
 		if (selectedID == -1) {
 			selected = false;
 		} else {
 			selected = true;
 		}
 		return selected;
 	}
 
 	private void alertNoBuildSelected() {
 		final AlertDialog.Builder alertNotSelected = new AlertDialog.Builder(
 				RaceMenu.this);
 		final AlertDialog alertConfirmDeleteDialog = alertNotSelected.create();
 		alertNotSelected.setTitle("Build not selected.");
 		alertNotSelected.setMessage("Please select a build.");
 		alertNotSelected.setPositiveButton("OK",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface arg0, int arg1) {
 					}
 				});
 		alertNotSelected.show();
 	}
 
 	private void setUpListenersAndVariables() {
 
 		// Initialise All Builds List
 		this.allBuilds = new ArrayList<ActionList>();
 		this.selectedBuilds = new ArrayList<ActionList>();
 
 		// Set Build Race
 		Intent buildIntent = getIntent();
 		this.race = buildIntent.getStringExtra("race");
 
 		// Set RadioGroup to Scroll view and populate with existing builds
 		scrollViewLinearLayoutVertical = (LinearLayout) findViewById(R.id.scrollViewLinearLayoutVertical);
 		radioGroupBuild = new RadioGroup(this);
 		scrollViewLinearLayoutVertical.addView(radioGroupBuild);
 		refreshRadioGroup();
 
 		// Add Button
 		buttonAddBuild = (Button) findViewById(R.id.buttonAddBuild);
 		buttonAddBuild.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 
 				// Set up alert dialog box
 				final AlertDialog.Builder alertBuildName = new AlertDialog.Builder(
 						RaceMenu.this);
 				final AlertDialog alertBuildNameDialog = alertBuildName
 						.create();
 				final EditText inputBuildName = new EditText(RaceMenu.this);
 				alertBuildName.setTitle("Add Build");
 				alertBuildName.setMessage("Enter new build name");
 				alertBuildName.setView(inputBuildName);
 				alertBuildName.setPositiveButton("Add",
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								Editable buildName = inputBuildName
 										.getEditableText();
 								Intent AddBuild = new Intent(RaceMenu.this,
 										Build.class);
 								AddBuild.putExtra("BuildName",
 										buildName.toString());
 								AddBuild.putExtra("race", getRace());
 								AddBuild.putExtra("IntentType", "AddBuild");
 								startActivityForResult(AddBuild, 1);
 							}
 						});
 
 				alertBuildName.setNegativeButton("Cancel",
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								dialog.cancel();
 							}
 						});
 
 				alertBuildName.show();
 			}
 		});
 
 		// Edit Button
 		buttonEditBuild = (Button) findViewById(R.id.buttonEditBuild);
 		buttonEditBuild.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 
 				// See if a build is selected
 				if (checkIfBuildIsSelected() == false) {
 					alertNoBuildSelected();
 					return;
 				}
 
 				// Get the selected build
 				RadioGroup temp = RaceMenu.this.getRadioGroup();
 				int editBuildID = temp.getCheckedRadioButtonId();
 
 				Intent editBuild = selectBuildForEdit(editBuildID);
 				// Start Intent
 				startActivityForResult(editBuild, 2);
 			}
 		});
 
 		// Delete Button
 		buttonDeleteBuild = (Button) findViewById(R.id.buttonDeleteBuild);
 		buttonDeleteBuild.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 
 				// See if a build is selected
 				if (checkIfBuildIsSelected() == false) {
 					alertNoBuildSelected();
 					return;
 				}
 
 				// Ask if they are sure
 				final AlertDialog.Builder alertConfirmDelete = new AlertDialog.Builder(
 						RaceMenu.this);
 				final AlertDialog alertConfirmDeleteDialog = alertConfirmDelete
 						.create();
 				alertConfirmDelete.setTitle("Delete Build");
 				alertConfirmDelete
 						.setMessage("Are you sure you want to delete this build?");
 				alertConfirmDelete.setPositiveButton("Yes",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// TODO Auto-generated method stub
 								deleteBuild();
 								refreshRadioGroup();
 							}
 						});
 				alertConfirmDelete.setNegativeButton("No",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// TODO Auto-generated method stub
 
 							}
 						});
 				alertConfirmDelete.show();
 			}
 		});
 		// Back Button
 		buttonBack = (Button) findViewById(R.id.buttonBack);
 		buttonBack.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				finish();
 			}
 		});
 
 		// Run Button
 		buttonRun = (Button) findViewById(R.id.buttonRun);
 		buttonRun.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 
 				// Get the selected build
 				RadioGroup temp = RaceMenu.this.getRadioGroup();
 				int runBuildID = temp.getCheckedRadioButtonId();
 
 				// See if a build is selected
 				if (checkIfBuildIsSelected() == false) {
 					alertNoBuildSelected();
 					return;
 				}
 
 				Intent runBuild = selectBuildForRun(runBuildID);
 				startActivityForResult(runBuild, 3);
 			}
 		});
 
 	}
 
 	private void refreshRadioGroup() {
 		radioGroupBuild.removeAllViews();
 		populateBuildsForThisMatchUp();
 		Iterator<ActionList> selectedBuildsIterator = selectedBuilds.iterator();
 		int radioIndex = 0;
 		while (selectedBuildsIterator.hasNext()) {
 			RadioButton radioButton = new RadioButton(this);
 			ActionList temp = selectedBuildsIterator.next();
 
 			radioButton.setId(radioIndex);
 			radioButton.setText(temp.getBuildName());
 			radioGroupBuild.addView(radioButton);
 			radioIndex++;
 		}

 	}
 
 	private void handleAddBuild(Intent savedIntent) {
 		Bundle bundle = savedIntent.getExtras();
 		ActionList tempActionList = (ActionList) bundle.get("BuildOrder");
 		List<ActionList> tempAllBuilds = getAllBuildsList();
 		tempAllBuilds.add(tempActionList);
 		setAllBuildsList(tempAllBuilds);
 	}
 
 	private void handleEditbuild(Intent savedIntent) {
 		Bundle bundle = savedIntent.getExtras();
 		ActionList newBuildActionList = (ActionList) bundle.get("BuildOrder");
 		String nameOfBuildToReplace = newBuildActionList.getBuildName();
 
 		// Find build and replace
 		List<ActionList> tempAllBuilds = getAllBuildsList();
 		Iterator<ActionList> tempAllBuildsIterator = tempAllBuilds.iterator();
 		int indexOfBuildToReplace = 0;
 		while (tempAllBuildsIterator.hasNext()) {
 			ActionList tempActionList = tempAllBuildsIterator.next();
 			String buildName = tempActionList.getBuildName();
 			if (buildName.equals(nameOfBuildToReplace)) {
 				tempAllBuilds.set(indexOfBuildToReplace, newBuildActionList);
 				break;
 			}
 			indexOfBuildToReplace++;
 		}
 
 		setAllBuildsList(tempAllBuilds);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent savedIntent) {
 		if (requestCode == 1) {
 			if (resultCode == RESULT_OK) {
 				handleAddBuild(savedIntent);
 				saveBuildsToFile();
 				refreshRadioGroup();
 			}
 		}
 
 		if (requestCode == 2) {
 			if (resultCode == RESULT_OK) {
 				handleEditbuild(savedIntent);
 				saveBuildsToFile();
 				refreshRadioGroup();
 			}
 		}
 
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_race_menu);
 		setUpListenersAndVariables();
 		setUpTextViews();
 		populateBuildsForThisMatchUp();
 		refreshRadioGroup();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_race_menu, menu);
 		return true;
 	}
 
 }
