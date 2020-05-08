 package com.pwr.zpi.database.entity;
 
 import java.util.List;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 import com.pwr.zpi.listeners.IOnNextActionListener;
 import com.pwr.zpi.utils.TimeFormatter;
 
 public class Workout implements Parcelable {
 	public final static String TAG = "Workout";
 	public static final String LIST_TAG = "List";
 	private long ID;
 	private String name;
 	private List<WorkoutAction> actions;
 	private boolean isWarmUp;
 	private int repeatCount;
 	private IOnNextActionListener onNextActionListener;
 	
 	// progressing workout fields
 	private int currentAction;
 	private double lastActionDistance;
 	private long lastActionTime;
 	// distance to virtual partner or time or distance for current action
 	private double howMuchLeft;
 	
 	public Workout() {
 		this.currentAction = 0;
 		this.lastActionDistance = 0;
 		this.lastActionTime = 0;
 		this.howMuchLeft = 0;
 		this.ID = -1;
 	}
 	
 	public Workout(Parcel in) {
 		readFromParcel(in);
 	}
 	
 	public long getID() {
 		return ID;
 	}
 	
 	public void setID(long iD) {
 		ID = iD;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public List<WorkoutAction> getActions() {
 		return actions;
 	}
 	
 	public void setActions(List<WorkoutAction> actions) {
 		this.actions = actions;
 		initHowMuchLeft(actions, 0, 0, 0);
 	}
 	
 	private void initHowMuchLeft(List<WorkoutAction> actions, int position, double deltaDistance, double deltaTime) {
 		if (actions != null && actions.size() != position) {
 			WorkoutAction action = actions.get(position);
 			switch (action.getActionType()) {
 				case WorkoutAction.ACTION_SIMPLE:
 					howMuchLeft = ((WorkoutActionSimple) action).getValue();
 					switch (((WorkoutActionSimple) action).getValueType()) {
 						case WorkoutAction.ACTION_SIMPLE_VALUE_TYPE_DISTANCE:
 							howMuchLeft -= deltaDistance;
 							break;
 						case WorkoutAction.ACTION_SIMPLE_VALUE_TYPE_TIME:
 							howMuchLeft -= deltaTime;
 							break;
 						default:
 							break;
 					}
 					break;
 				case WorkoutAction.ACTION_ADVANCED:
 					howMuchLeft = ((WorkoutActionAdvanced) action).getDistance();
 					howMuchLeft -= deltaDistance;
 					break;
 				default:
 					break;
 			}
 			
 		}
 	}
 	
 	public boolean isWarmUp() {
 		return isWarmUp;
 	}
 	
 	public void setWarmUp(boolean isWarmUp) {
 		this.isWarmUp = isWarmUp;
 	}
 	
 	public int getRepeatCount() {
 		return repeatCount;
 	}
 	
 	public void setRepeatCount(int repeatCount) {
 		this.repeatCount = repeatCount;
 	}
 	
 	// progressing actions methods
 	public void progressWorkout(double distance, Long time) {
 		double currentDistance = distance - lastActionDistance;
 		long currentTime = time - lastActionTime;
 		
 		WorkoutAction action = getActions().get(currentAction);
 		
 		switch (action.getActionType()) {
 			case WorkoutAction.ACTION_SIMPLE:
 				WorkoutActionSimple simple = (WorkoutActionSimple) action;
 				progressSimpleAction(simple, currentDistance, currentTime);
 				break;
 			case WorkoutAction.ACTION_ADVANCED:
 				WorkoutActionAdvanced advanced = (WorkoutActionAdvanced) action;
 				progressAdvancedAction(advanced, currentDistance, currentTime);
 				break;
 			case WorkoutAction.ACTION_WARM_UP:
 				WorkoutActionWarmUp warmUp = (WorkoutActionWarmUp) action;
 				progressWarmUpAction(warmUp, currentTime);
 				break;
 			default:
 				break;
 		}
 	}
 	
 	private void progressSimpleAction(WorkoutActionSimple simple, double currentDistance, long currentTime) {
 		boolean actionDone = false;
 		double deltaDistance = 0;
 		double deltaTime = 0;
 		switch (simple.getValueType()) {
 			case WorkoutAction.ACTION_SIMPLE_VALUE_TYPE_DISTANCE:
 				double actionDistanceToCover = simple.getValue();
 				actionDone = currentDistance >= actionDistanceToCover;
 				if (actionDone) {
 					deltaDistance = currentDistance - actionDistanceToCover;
 				}
 				else {
 					this.howMuchLeft = actionDistanceToCover - currentDistance;
 				}
 				break;
 			case WorkoutAction.ACTION_SIMPLE_VALUE_TYPE_TIME:
 				double actionTimeToCover = simple.getValue();
 				actionDone = currentTime >= actionTimeToCover;
 				if (actionDone) {
 					deltaTime = currentTime - actionTimeToCover;
 				}
 				else {
 					this.howMuchLeft = actionTimeToCover - currentTime;
 				}
 				break;
 			default:
 				break;
 		}
 		if (actionDone) {
 			currentAction++;
 			lastActionDistance += currentDistance - deltaDistance;
 			lastActionTime += currentTime - deltaTime;
 			initHowMuchLeft(getActions(), currentAction, deltaDistance, deltaTime);
 			if (hasNextAction() && hasOnNextActionListener()) {
 				WorkoutAction action = getActions().get(currentAction);
 				notifyListeners(action);
 			}
 		}
 	}
 	
 	private boolean hasOnNextActionListener() {
 		return onNextActionListener != null;
 	}
 	
 	private void progressAdvancedAction(WorkoutActionAdvanced advanced, double currentDistance, long currentTime) {
 		double virtualPartnerVelocity = advanced.getDistance() / advanced.getTime(); // km / milisecond
 		double virtualPartnerDistance = virtualPartnerVelocity * currentTime;
 		double distanceBetweenUserAndVirutalPartner = currentDistance - virtualPartnerDistance;
 		
 		if (virtualPartnerDistance >= advanced.getDistance()) {
 			double deltaDistance = virtualPartnerDistance - advanced.getDistance();;
 			
 			currentAction++;
 			lastActionDistance += currentDistance - deltaDistance;
 			lastActionTime += currentTime;
 			initHowMuchLeft(getActions(), currentAction, deltaDistance, 0);
 			if (hasNextAction() && hasOnNextActionListener()) {
 				WorkoutAction action = getActions().get(currentAction);
 				notifyListeners(action);
 			}
 		}
 		else {
 			this.howMuchLeft = distanceBetweenUserAndVirutalPartner;
 		}
 	}
 	
 	private void progressWarmUpAction(WorkoutActionWarmUp warmUp, long currentTime) {
 		double warmUpTime = warmUp.getWorkoutTime() * 60 * 1000;
 		if (warmUpTime <= currentTime) {
 			Log.i(TAG, warmUpTime + " " + currentTime);
 			currentAction++;
 			lastActionTime = 0;
 			initHowMuchLeft(getActions(), currentAction, 0, 0);
 			if (hasNextAction() && hasOnNextActionListener()) {
 				WorkoutAction action = getActions().get(currentAction);
 				notifyListeners(action);
 			}
		} else {
 			this.howMuchLeft = warmUpTime - currentTime;
 		}
 	}
 	
 	public void notifyListeners(WorkoutAction action) {
 		Log.i(TAG, "Notifing listeners");
 		switch (action.getActionType()) {
 			case WorkoutAction.ACTION_SIMPLE:
 				onNextActionListener.onNextActionSimple((WorkoutActionSimple) action);
 				break;
 			case WorkoutAction.ACTION_ADVANCED:
 				onNextActionListener.onNextActionAdvanced((WorkoutActionAdvanced) action);
 				break;
 			case WorkoutAction.ACTION_WARM_UP:
 				Log.i(TAG, "warm up listener say!");
 				onNextActionListener.onNextActionWarmUP((WorkoutActionWarmUp) action);
 				break;
 			default:
 				break;
 		}
 	}
 	
 	public int getCurrentAction() {
 		return currentAction;
 	}
 	
 	public double getHowMuchLeft() {
 		return howMuchLeft;
 	}
 	
 	public CharSequence getHowMuchLeftCurrentActionStringWithUnits() {
 		WorkoutAction currentAction = getActions().get(getCurrentAction());
 		return formatActionValue(currentAction, getHowMuchLeft());
 	}
 	
 	public static CharSequence formatActionValue(WorkoutAction action, Double value) {
 		StringBuilder sb = new StringBuilder();
 		boolean getValue = value == null;
 		if (action.isAdvanced()) {
 			if (getValue) {
 				value = ((WorkoutActionAdvanced) action).getDistance();
 			}
 			sb.append(String.format("%.0f", value));
 			sb.append("m");
 		}
 		else if (action.isSimple()) {
 			WorkoutActionSimple simple = (WorkoutActionSimple) action;
 			if (getValue) {
 				value = simple.getValue();
 			}
 			if (simple.getValueType() == WorkoutAction.ACTION_SIMPLE_VALUE_TYPE_DISTANCE) {
 				sb.append(String.format("%.0f", value));
 				sb.append("m");
 			}
 			else {
 				sb.append(TimeFormatter.formatTimeHHMMSS(value.longValue()));
 				sb.append("h");
 			}
		} else if (action.isWarmUp()) {
 			WorkoutActionWarmUp warmUp = (WorkoutActionWarmUp) action;
 			if (getValue) {
 				value = (double) (warmUp.getWorkoutTime() * 60 * 1000);
 			}
 			sb.append(TimeFormatter.formatTimeHHMMSS(value.longValue()));
 			sb.append("h");
 		}
 		return sb.toString();
 	}
 	
 	public boolean hasNextAction() {
 		return currentAction < getActions().size();
 	}
 	
 	public IOnNextActionListener getOnNextActionListener() {
 		return onNextActionListener;
 	}
 	
 	public void setOnNextActionListener(IOnNextActionListener onNextActionListener) {
 		this.onNextActionListener = onNextActionListener;
 	}
 	
 	@Override
 	public int describeContents() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	@Override
 	public void writeToParcel(Parcel out, int flag) {
 		out.writeLong(ID);
 		out.writeString(name);
 		out.writeList(actions);
 		out.writeByte((byte) (isWarmUp ? 1 : 0));
 		out.writeInt(repeatCount);
 		out.writeParcelable(onNextActionListener, flag);
 		out.writeInt(currentAction);
 		out.writeDouble(howMuchLeft);
 		out.writeDouble(lastActionDistance);
 		out.writeLong(lastActionTime);
 	}
 	
 	public static final Parcelable.Creator<Workout> CREATOR = new Parcelable.Creator<Workout>() {
 		@Override
 		public Workout createFromParcel(Parcel in) {
 			return new Workout(in);
 		}
 		
 		@Override
 		public Workout[] newArray(int size) {
 			return new Workout[size];
 		}
 	};
 	
 	public void updateWorkoutData(Workout newWorkout) {
 		this.actions = newWorkout.getActions();
 		this.currentAction = newWorkout.getCurrentAction();
 		this.howMuchLeft = newWorkout.getHowMuchLeft();
 		this.lastActionDistance = newWorkout.getLastActionDistance();
 		this.lastActionTime = newWorkout.getLastActionTime();
 	}
 	
 	public double getLastActionDistance() {
 		return lastActionDistance;
 	}
 	
 	public long getLastActionTime() {
 		return lastActionTime;
 	}
 	
 	public void readFromParcel(Parcel in) {
 		ID = in.readLong();
 		name = in.readString();
 		actions = in.readArrayList(WorkoutAction.class.getClassLoader());
 		isWarmUp = in.readByte() == 1;
 		repeatCount = in.readInt();
 		onNextActionListener = in.readParcelable(IOnNextActionListener.class.getClassLoader());
 		currentAction = in.readInt();
 		howMuchLeft = in.readDouble();
 		lastActionDistance = in.readDouble();
 		lastActionTime = in.readLong();
 	}
 	
 }
