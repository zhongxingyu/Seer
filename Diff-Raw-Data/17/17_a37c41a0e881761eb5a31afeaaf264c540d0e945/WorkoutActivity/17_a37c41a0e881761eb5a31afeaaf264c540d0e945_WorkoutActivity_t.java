 package com.pwr.zpi;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.GestureDetector;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
import android.widget.TextView;
 
 import com.pwr.zpi.adapters.WorkoutActionsAdapter;
 import com.pwr.zpi.database.Database;
 import com.pwr.zpi.database.entity.Workout;
 import com.pwr.zpi.database.entity.WorkoutAction;
 import com.pwr.zpi.dialogs.MyDialog;
 import com.pwr.zpi.listeners.GestureListener;
 import com.pwr.zpi.listeners.MyGestureDetector;
 
 public class WorkoutActivity extends Activity implements GestureListener, OnItemClickListener {
 	
 	private GestureDetector gestureDetector;
 	private MyGestureDetector myGestureDetector;
 	private ListView actionsListView;
 	private Workout workout;
 	private WorkoutActionsAdapter actionsAdapter;
 	private View mCurrent;
 	private View.OnTouchListener gestureListener;
 	private Button addThisWorkoutButton;
	private TextView workoutNameTextView;
 	private AdapterContextMenuInfo info;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setContentView(R.layout.workout_activity);
 		prepareGestureListener();
 		initFields();
 		addListeners();
 		super.onCreate(savedInstanceState);
 	}
 	
 	private void initFields()
 	{
 		actionsListView = (ListView) findViewById(R.id.listViewWokoutActions);
 		long ID = getIntent().getLongExtra(PlaningActivity.ID_TAG, -1);
 		workout = readData(ID);
 		actionsAdapter = new WorkoutActionsAdapter(this, R.layout.workouts_action_list_item, workout.getActions());
 		actionsListView.setAdapter(actionsAdapter);
 		addThisWorkoutButton = (Button) findViewById(R.id.ButtonChooseWorkout);
		workoutNameTextView = (TextView) findViewById(R.id.textViewWorkoutName);
		workoutNameTextView.setText(workout.getName());
 		registerForContextMenu(actionsListView);
 	}
 	
 	private void prepareGestureListener() {
 		// Gesture detection
 		myGestureDetector = new MyGestureDetector(this,
 			false, false, false, true);
 		gestureDetector = new GestureDetector(this, myGestureDetector);
 		gestureListener = new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				mCurrent = v;
 				return gestureDetector.onTouchEvent(event);
 			}
 		};
 	}
 	
 	private void addListeners()
 	{
 		addThisWorkoutButton.setOnTouchListener(gestureListener);
 		actionsListView.setOnTouchListener(gestureListener);
 		actionsListView.setOnItemClickListener(this);
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		return gestureListener.onTouch(null, event);
 	}
 	
 	private Workout readData(long ID)
 	{
 		Workout workout;
 		Intent i = getIntent();
 		if (i.getBooleanExtra(PlaningActivity.IS_NEW_TAG, false))
 		{
 			workout = new Workout();
 			ArrayList<WorkoutAction> actions = i.getParcelableArrayListExtra(NewWorkoutActivity.LIST_TAG);
 			workout.setActions(actions);
 			
 			workout.setRepeatCount(i.getIntExtra(NewWorkoutActivity.REPEAT_TAG, 1));
 			workout.setName(i.getStringExtra(NewWorkoutActivity.NAME_TAG));
 			workout.setWarmUp(i.getBooleanExtra(NewWorkoutActivity.WORMUP_TAG, false));
 			
 		}
 		else
 		{
 			Database database = new Database(this);
 			workout = database.getWholeSingleWorkout(ID);
 			database.close();
 			if (workout.getActions() == null)
 			{
 				ArrayList<WorkoutAction> emptyList = new ArrayList<WorkoutAction>();
 				workout.setActions(emptyList);
 			}
 		}
 		return workout;
 		
 	}
 	
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		overridePendingTransition(R.anim.in_left_anim, R.anim.out_left_anim);
 	}
 	
 	@Override
 	public void onLeftToRightSwipe() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void onRightToLeftSwipe() {
 		finish();
 		overridePendingTransition(R.anim.in_left_anim, R.anim.out_left_anim);
 	}
 	
 	@Override
 	public void onUpToDownSwipe() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void onDownToUpSwipe() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void onSingleTapConfirmed(MotionEvent e) {
 		int ID = mCurrent.getId();
 		switch (ID)
 		{
 			case R.id.ButtonChooseWorkout:
 				//TODO check GPS like in MainScreen 
 				Intent i = new Intent(WorkoutActivity.this, ActivityActivity.class);
 				i.putExtra(PlaningActivity.ID_TAG, workout.getID());
 				ArrayList<WorkoutAction> actions = (ArrayList<WorkoutAction>) workout.getActions();
 				i.putParcelableArrayListExtra(NewWorkoutActivity.LIST_TAG, actions);
 				i.putExtra(NewWorkoutActivity.WORMUP_TAG, workout.isWarmUp());
 				i.putExtra(NewWorkoutActivity.REPEAT_TAG, workout.getRepeatCount());
 				i.putExtra(NewWorkoutActivity.NAME_TAG, workout.getName());
 				startActivity(i);
 				break;
 			default:
 				break;
 		}
 		
 	}
 	
 	@Override
 	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
 		if (!myGestureDetector.isFlingDetected())
 		{
 			//TODO edit action
 		}
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.delete_action_menuitem:
 				info = (AdapterContextMenuInfo) item.getMenuInfo();
 				
 				if (actionsAdapter != null) {
 					MyDialog dialog = new MyDialog();
 					DialogInterface.OnClickListener positiveButtonHandler = new DialogInterface.OnClickListener() {
 						
 						// romove
 						@Override
 						public void onClick(DialogInterface dialog, int id) {
 							
 							WorkoutAction toDelete = actionsAdapter.getItem(info.position);
 							actionsAdapter.remove(toDelete);
 							Database db = new Database(WorkoutActivity.this);
 							db.deleteWorkoutAction(workout.getID(), toDelete.getID());
 							db.close();
 						}
 					};
 					dialog.showAlertDialog(this, R.string.dialog_message_remove_action,
 						R.string.empty_string, android.R.string.yes,
 						android.R.string.no, positiveButtonHandler, null);
 					
 				}
 				break;
 			default:
 				break;
 		}
 		return super.onContextItemSelected(item);
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 		ContextMenuInfo menuInfo) {
 		switch (v.getId()) {
 			case R.id.listViewWokoutActions:
 				MenuInflater inflater = getMenuInflater();
 				inflater.inflate(R.menu.context_menu, menu);
 				menu.setHeaderTitle(R.string.menu_ctx_actions);
 				break;
 			default:
 				break;
 		}
 		super.onCreateContextMenu(menu, v, menuInfo);
 	}
 }
