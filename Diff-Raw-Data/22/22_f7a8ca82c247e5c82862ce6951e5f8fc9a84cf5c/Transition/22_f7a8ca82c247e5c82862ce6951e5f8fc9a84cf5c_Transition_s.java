 package statemachine.cos.com;
 
 import java.lang.Object;
 import java.util.ArrayList;
 
 import android.util.Log;
 
 public class Transition extends Object {
     private final static String TAG = "StateMachineActivity";
     private String mName;
     private State mSource;
     private State mTarget;
     private OnTransitionListener mListener = null;
 
     public Transition(State source, State target, String name) {
 	mSource = source;
 	mTarget = target;
 	mName = name;
 
 	mSource.addTransition(this);
     }
 
     public State getTarget() {
 	return mTarget;
     }
 
     public void setOnTransitionListener(OnTransitionListener listener) {
	Log.d(TAG, "listener = "+listener);
 	mListener = listener;
	Log.d(TAG, "listener = "+mListener);
     }
 
     protected void onTransition(Event event) {
 	Log.d(TAG, this + "onTransition()");
     }
 
     final void action(Event event) {
 	Log.d(TAG, this + "action(), listener="+mListener);
 	if (mListener != null) {
 	    mListener.onTransition(event, this);
 	}
 	onTransition(event);
 	// TODO: trigger
     }
 
     protected boolean eventTest(Event event) {
 	// TODO:
 	return true;
     }
 
     protected void reset() {
     }
 
     @Override public String toString() {
 	return getClass().getName() + "[" +
 	    "name=\"" + mName + "\"]";
     }
 
     public interface OnTransitionListener {
 	public void onTransition(Event event, Transition transition);
     }
 }
