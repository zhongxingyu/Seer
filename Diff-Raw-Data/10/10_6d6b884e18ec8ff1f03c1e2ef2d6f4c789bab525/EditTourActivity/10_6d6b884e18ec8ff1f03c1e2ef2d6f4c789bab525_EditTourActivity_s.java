 package com.iCompute.tour;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.Toast;
 import com.iCompute.tour.Tour;
 import com.iCompute.tour.ToursList;
 
 //TODO figure out how to distinguish between an add and an update
 //TODO When we're updating, pass in the ID of the tour via the intent
 
 public class EditTourActivity extends Activity implements View.OnClickListener{
 	
 	private int tourID;
 	private ToursList tours;
 	private boolean updating;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.edit_tour_layout);
 		//get a reference to the tours list
 		tours = ((TourApplication)getApplication()).tours;
 		
 		/*
 		 * if we're updating, check the id of the tour passed via the intent,
 		 * pull the relevant tour from the list by its id and auto populate the fields
 		 * also pass in a flag so we can call the appropriate function when we hit 'save'
 		 */
 		Intent i = getIntent();
 		if(i.getBooleanExtra("isUpdate",false)){
 			updating = true;
 			tourID = i.getIntExtra("tourID", -1);
 			Tour tour = tours.get(tourID);
 			((EditText)findViewById(R.id.nameEditTourEditText)).setText(tour.getTitle().toString());
 			((EditText)findViewById(R.id.descriptionEditTourEditText)).setText(tour.getDescription().toString());
 			((EditText)findViewById(R.id.tagsEditTourEditText)).setText(tour.getTags().toString());
 			if(tour.isWalkable()){
 				((RadioButton)findViewById(R.id.walkingEditTourRadioButton)).setChecked(true);
 			}
 		}
 		
 		
 		((Button) findViewById(R.id.saveEditTourButton)).setOnClickListener(this);
 		((Button) findViewById(R.id.discardEditTourButton)).setOnClickListener(this);
 		((Button) findViewById(R.id.addStopEditTourButton)).setOnClickListener(this);
 		((Button) findViewById(R.id.imagesEditTourButton)).setOnClickListener(this);
 		((Button) findViewById(R.id.editStopsEditTourButton)).setOnClickListener(this);
 		
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int i)
 	{
 		Dialog dialog;
 		
 		switch(i){
 		case 0:
 			dialog = saveButtonClick();
 			break;
 		case 1:
 			dialog = discardButtonClick();
 			break;
 		default:
 			dialog = null;
 			
 		}
 		return dialog;
 	}
 	
 	public void onClick(View v)
 	{
 		switch(v.getId()){
 		case R.id.saveEditTourButton:
 			//For now, we'll add the tour into the global list here
 			String name = ((EditText)findViewById(R.id.nameEditTourEditText)).getText().toString();
 			String description = ((EditText)findViewById(R.id.descriptionEditTourEditText)).getText().toString();
 			String tags = ((EditText)findViewById(R.id.tagsEditTourEditText)).getText().toString();
 			boolean isWalk = ((RadioButton)findViewById(R.id.walkingEditTourRadioButton)).isChecked();
 			
 			if(updating){
 				Tour t = tours.get(tourID);
 				t.update(name, description, tags, isWalk);
 			}
 			else{
 				boolean added = tours.add(new Tour(name,description,tags,isWalk));
 			}
 			//TODO after saving, we should bounce back to the main screen instead of staying in this activity.
 			//showDialog(0);
 			break;
 		case R.id.discardEditTourButton:
 			showDialog(1);
 			break;
 		case R.id.addStopEditTourButton:
 			addTourStop();
 			break;
 		case R.id.imagesEditTourButton:
 			addMedia();
 			break;
 		case R.id.editStopsEditTourButton:
 			editStops();
 			break;
 		default:
 			break;
 		
 		}
 	}
 	
 	private void saveTour(){
 		
 	}
 	
 	private AlertDialog saveButtonClick()
 	{
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Save Tour");
 		builder.setMessage("Would you like to save locally or save locally and publish?");
 		
 		builder.setPositiveButton("Save Local", new Dialog.OnClickListener(){
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				saveTourLocal();
 				EditTourActivity.this.finish();
 			}
 		});
 		builder.setNeutralButton("Publish", new Dialog.OnClickListener(){
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				publishTour();
 				EditTourActivity.this.finish();
 			}
 		});
 		//builder.setCancelable(false);
 		builder.setNegativeButton("Cancel", new Dialog.OnClickListener(){
 			@Override
 			public void onClick(DialogInterface dialog, int which){
 				dialog.cancel();
 			}
 		});
 		
 		return builder.create();
 	}
 
 	
 	private void editStops()
 	{
 		//TODO send the id of the tour we're editing in the intent
 		startActivity(new Intent(this, EditTourStopsListActivity.class));
 	}
 	
 	private void addMedia()
 	{
 		startActivity(new Intent(this, ImageSelectorActivity.class));
 	}
 	
 	private AlertDialog discardButtonClick()
 	{
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Discard Changes?");
 		
 		builder.setPositiveButton("Discard", new Dialog.OnClickListener(){
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				discardTourChanges();
 				EditTourActivity.this.finish();
 			}
 		});
 		builder.setNegativeButton("Cancel", new Dialog.OnClickListener(){
 			@Override
 			public void onClick(DialogInterface dialog, int which){
 				dialog.cancel();
 			}
 		});
 		
 		return builder.create();
 	}
 	
 	private void addTourStop()
 	{
 		//my location or on map dialog
 		startActivity(new Intent(getBaseContext(), EditTourStopActivity.class));
 	}
 	
 	private void saveTourLocal()
 	{
 		//TODO save the tour locally somehow
 		Toast.makeText(getBaseContext(), "Save Tour Locally", Toast.LENGTH_SHORT).show();
 	}
 	private void publishTour()
 	{
 		//TODO push the tour to the server
 		Toast.makeText(getBaseContext(), "Publish Tour", Toast.LENGTH_SHORT).show();
 		saveTourLocal();
 	}
 	private void discardTourChanges()
 	{
 		Toast.makeText(getBaseContext(), "Discarded Changes", Toast.LENGTH_SHORT).show();
 	}
 }
