 package com.pwr.zpi.adapters;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.pwr.zpi.R;
 import com.pwr.zpi.database.entity.Workout;
 import com.pwr.zpi.database.entity.WorkoutAction;
 import com.pwr.zpi.database.entity.WorkoutActionSimple;
 
 public class DrawerWorkoutsAdapter extends ArrayAdapter<WorkoutAction> {
 	
 	private static final int ACTIVE_HEIGHT = 200;
 	private static final int NOT_ACTIVE_HEIGHT = 100;
 	
 	private final int layoutResourceID;
 	private final Workout workout;
 	
 	public DrawerWorkoutsAdapter(Context context, int layoutResourceID, List<WorkoutAction> actions, Workout workout) {
 		super(context, layoutResourceID, actions);
 		this.layoutResourceID = layoutResourceID;
 		this.workout = workout;
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View row = convertView;
 		RowHolder rowHolder;
 		//	if (row == null) {
 		LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
 		row = inflater.inflate(layoutResourceID, parent, false);
 		
 		rowHolder = new RowHolder();
 		rowHolder.actionType = (ImageView) row.findViewById(R.id.imageViewState);
 		rowHolder.actionTextAdvanced = (TextView) row.findViewById(R.id.textViewWorkoutActionText);
 		rowHolder.actionTextSimple = (TextView) row.findViewById(R.id.textViewWorkoutActionSimpleText);
 		rowHolder.actionTypeSimple = (TextView) row.findViewById(R.id.textViewWorkoutActionSimpleTypeText);
 		rowHolder.actionPointInTime = (TextView) row.findViewById(R.id.textViewState);
 		row.setTag(rowHolder);
 		//	}
 		//	else {
 		//		rowHolder = (RowHolder) row.getTag();
 		//	}
 		
 		int textColor = android.R.color.white;
 		WorkoutAction action = getItem(position);
 		switch (action.getActionType()) {
 			case WorkoutAction.ACTION_SIMPLE:
 				rowHolder.actionTextSimple.setVisibility(View.VISIBLE);
 				rowHolder.actionTypeSimple.setVisibility(View.VISIBLE);
 				rowHolder.actionTextAdvanced.setVisibility(View.GONE);
 				int speedType = ((WorkoutActionSimple) action).getSpeedType();
 				switch (speedType)
 				{
 					case WorkoutAction.ACTION_SIMPLE_SPEED_FAST:
 						rowHolder.actionTypeSimple.setText(R.string.fast);
 						
 						break;
 					case WorkoutAction.ACTION_SIMPLE_SPEED_STEADY:
 						rowHolder.actionTypeSimple.setText(R.string.steady);
 						break;
 					case WorkoutAction.ACTION_SIMPLE_SPEED_SLOW:
 						rowHolder.actionTypeSimple.setText(R.string.slow);
 						break;
 				}
 				
 				break;
 			case WorkoutAction.ACTION_ADVANCED:
				
 				rowHolder.actionTextSimple.setVisibility(View.INVISIBLE);
 				rowHolder.actionTypeSimple.setVisibility(View.INVISIBLE);	//not gone because we still wont it to determine height
 				rowHolder.actionTextAdvanced.setVisibility(View.VISIBLE);
 				
 				if (workout.getHowMuchLeft() > 0) {
 					textColor = R.color.workout_action_text_good;
 				}
 				else {
 					textColor = R.color.workout_action_text_bad;
 				}
 				break;
			case WorkoutAction.ACTION_WARM_UP:
				rowHolder.actionTextSimple.setVisibility(View.INVISIBLE);
				rowHolder.actionTypeSimple.setVisibility(View.INVISIBLE);	//not gone because we still wont it to determine height
				rowHolder.actionTextAdvanced.setVisibility(View.VISIBLE);
 				break;
 			default:
 				break;
 		}
 		
 		if (position < workout.getCurrentAction()) {
 			textColor = R.color.white;
 			//rowHolder.layout.setBackgroundColor(getContext().getResources().getColor(R.color.workout_drawer_not_active));
 			rowHolder.actionType.setVisibility(View.VISIBLE);
 			rowHolder.actionPointInTime.setVisibility(View.VISIBLE);
 			rowHolder.actionType.setImageResource(R.drawable.done);
 			rowHolder.actionPointInTime.setText(R.string.done);
 			if (action.isSimple()) {
 				rowHolder.actionTextSimple.setText(Workout.formatActionValue(action, null));
 			}
 			else {
 				rowHolder.actionTextAdvanced.setText(Workout.formatActionValue(action, null));
 			}
 		}
 		else if (position == workout.getCurrentAction()) {
 			rowHolder.actionType.setImageResource(R.drawable.now);//setText(Workout.formatActionValue(action, null));
 			rowHolder.actionPointInTime.setText(R.string.now);
 			rowHolder.actionType.setVisibility(View.VISIBLE);
 			rowHolder.actionPointInTime.setVisibility(View.VISIBLE);
 			if (action.isSimple()) {
 				rowHolder.actionTextSimple.setText(workout.getHowMuchLeftCurrentActionStringWithUnits());
 			}
 			else {
 				rowHolder.actionTextAdvanced.setText(workout.getHowMuchLeftCurrentActionStringWithUnits());
 			}
 			//	row.setMinimumHeight(ACTIVE_HEIGHT);
 		}
 		else {
 			textColor = R.color.white;
 			if (action.isSimple()) {
 				rowHolder.actionTextSimple.setText(Workout.formatActionValue(action, null));
 			}
 			else {
 				rowHolder.actionTextAdvanced.setText(Workout.formatActionValue(action, null));
 			}		//rowHolder.layout
 			//	.setBackgroundColor(getContext().getResources().getColor(R.color.workout_drawer_not_active));
 			
 			//	row.setMinimumHeight(NOT_ACTIVE_HEIGHT);
 		}
 		rowHolder.actionTextAdvanced.setTextColor(getContext().getResources().getColor(textColor));
 		return row;
 	}
 	
 	private class RowHolder {
 		protected TextView actionTextSimple;
 		protected TextView actionTypeSimple;
 		protected TextView actionTextAdvanced;
 		protected TextView actionPointInTime;
 		protected ImageView actionType;
 	}
 }
