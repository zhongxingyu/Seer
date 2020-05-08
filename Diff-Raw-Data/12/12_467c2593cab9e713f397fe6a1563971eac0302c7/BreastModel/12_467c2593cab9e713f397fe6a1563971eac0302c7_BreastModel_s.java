 package ch.almana.android.stillmeter.model;
 
 import android.content.Context;
 import android.os.Bundle;
 import ch.almana.android.stillmeter.log.Logger;
 
 public class BreastModel {
 
 	private static final String BUNDLE_POSITION = "BUNDLE_POSITION";
 	private static final String BUNDLE_STILL_TIME_MODEL = "BUNDLE_STILL_TIME_MODEL";
 
 	public enum Position {
 		none, left, right
 	};
 
 	private final Position position;
 	private StillTimeModel currentStillTimeModel = null;
 
 	private long totalTime = 0;
 	private final long session;
 
 	public BreastModel(Position position, long session) {
 		super();
 		this.session = session;
 		this.position = position;
 	}
 
 	public BreastModel(long session, Bundle state) {
 		super();
 		this.session = session;
 		position = Position.valueOf(state.getString(BUNDLE_POSITION));
 		Bundle bundle = state.getBundle(BUNDLE_STILL_TIME_MODEL);
 		if (bundle != null) {
 			currentStillTimeModel = new StillTimeModel(bundle);
 		}
 	}
 
 	public Bundle getBundle() {
 		Bundle state = new Bundle();
 		state.putString(BUNDLE_POSITION, position.toString());
 		if (currentStillTimeModel != null) {
 			state.putBundle(BUNDLE_STILL_TIME_MODEL, currentStillTimeModel.getBundle());
 		}
 		return state;
 	}
 
 	public void start(Context ctx) {
 		currentStillTimeModel = new StillTimeModel(ctx, position, session, System.currentTimeMillis());
 	}
 
 	public void end(Context ctx) {
 		if (currentStillTimeModel == null) {
 			Logger.w("BreastModel does not have a StillTimeModel");
 			return;
 		}
 		currentStillTimeModel.setEndTime(ctx, System.currentTimeMillis());
 		totalTime += currentStillTimeModel.getStillTime();
 		currentStillTimeModel = null;
 	}
 
 	public long getTime() {
 		long t = totalTime;
 		if (currentStillTimeModel != null) {
 			t += currentStillTimeModel.getStillTime();
 		}
 		return t;
 	}
 
 
 }
