 package com.egeniq.app;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.FrameLayout;
 
 /**
  * Controller base class.
  * 
  * Controllers are a lot similar to fragments with the following main differences:
  * - Controllers can be nested inside other controllers.
  * - Controllers don't store their state (e.g. the fragment is responsible for (re-)creating them and storing state).
  */
 public abstract class Controller {
     private String TAG = Controller.class.getName();
     private boolean DEBUG = false;
     
     public enum State { INITIAL, ATTACHED, CREATED, VIEW_CREATED, STARTED, RESUMED, PAUSED, STOPPED, VIEW_DESTROYED, DESTROYED, DETACHED };    
 
     private final Context _context;
     private Bundle _arguments;
     private Fragment _fragment = null;
     private Controller _parentController = null;
     private final ArrayList<Controller> _childControllers = new ArrayList<Controller>();
     private State _state = State.INITIAL;
     private View _innerView;
     private FrameLayout _stubView;
     
     /**
      * Constructor.
      * 
      * @param context
      * @param widget
      */
     public Controller(Context context) {
         _context = context;
     }
     
     /**
      * Returns the controller arguments.
      * 
      * @return Arguments. 
      */
     public Bundle getArguments() {
         return _arguments;
     }
     
     /**
      * Sets the controller arguments.
      * 
      * @param arguments Arguments.
      */
     public void setArguments(Bundle arguments) {
         _arguments = arguments;
     }
     
     /**
      * Returns the fragment this controller is part of.
      * 
      * The fragment is based on the root controllers fragment.
      * 
      * @return fragment
      */
     public Fragment getFragment() {
         return isRootController() ? _fragment : getRootController().getFragment();
     }
     
     /**
      * Sets the fragment this controller is part of.
      * 
      * @param fragment
      */
     public void setFragment(Fragment fragment) {
         _fragment = fragment;
     }
     
     /**
      * Is root controller?
      * 
      * @return Is root controller?
      */
     public boolean isRootController() {
         return getParentController() == null;
     }
     
     /**
      * Returns the root controller.
      * 
      * @return Controller.
      */
     public Controller getRootController() {
         if (isRootController()) {
             return this;
         } else {
             return getParentController().getRootController();
         }
     }
     
     /**
      * Returns the parent controller.
      * 
      * @param controller Parent controller.
      */
     public Controller getParentController() {
         return _parentController;
     }
     
     /**
      * Sets the parent controller.
      * 
      * @param controller Parent controller.
      */
     public void setParentController(Controller controller) {
         _parentController = controller;
     }
     
     /**
      * Add child controller.
      * 
      * @param controller child controller
      */
     public void addChildController(Controller controller) {
         _childControllers.add(controller);
         controller.setParentController(this);
         controller.transitionTo(_state);
     }
     
     /**
      * Remove child controller.
      * 
      * @param childController child controller
      */
     public void removeChildController(Controller childController) {
         childController.transitionTo(State.DESTROYED); 
         childController.setParentController(null);
         _childControllers.remove(childController);
     }
 
     /**
      * Returns the context.
      * 
      * @return context
      */
     public Context getContext() {
         return _context;
     }
     
     /**
      * Returns the current state.
      * 
      * @return state
      */
     public State getState() {
         return _state;
     }
     
     /**
      * Transition to the given state.
      * 
      * @param state New state.
      */
     public void transitionTo(State state) {
         int currentIndex = Arrays.binarySearch(State.values(), getState());
         int targetIndex = Arrays.binarySearch(State.values(), state);
         
         if (currentIndex == targetIndex) {
             return;
         }
         
         if (currentIndex < Arrays.binarySearch(State.values(), State.STARTED) && targetIndex >= Arrays.binarySearch(State.values(), State.STOPPED)) {
             return;
         } else if (getState() == State.STOPPED && targetIndex < currentIndex && targetIndex >= Arrays.binarySearch(State.values(), State.STARTED)) {
             _restart();
             _state = State.STARTED;
         } else if (getState() == State.PAUSED && targetIndex < currentIndex && targetIndex >= Arrays.binarySearch(State.values(), State.RESUMED)) {
             _resume();
             _state = State.RESUMED;
         } else if (getState() == State.VIEW_DESTROYED && targetIndex < currentIndex && targetIndex >= Arrays.binarySearch(State.values(), State.VIEW_CREATED)) {
             _createView();
             _state = State.VIEW_CREATED;
         }
 
         currentIndex = Arrays.binarySearch(State.values(), getState());
         
         if (targetIndex < currentIndex) {
             if (DEBUG) {
                 Log.e(TAG, "Target state " + state + " is invalid for current state " + getState());
             }
             
             return;
         }
 
         for (int i = currentIndex + 1; i <= targetIndex; i++) {
             switch (State.values()[i]) {
                 case ATTACHED:
                     _attach();
                     break;
                 case CREATED:
                     _create();
                     break;
                 case VIEW_CREATED:
                     _createView();
                     break;
                 case STARTED:
                     _start();
                     break;
                 case RESUMED:
                     _resume();
                     break;
                 case PAUSED:
                     _pause();
                     break;
                 case STOPPED:
                     _stop();
                     break;
                 case VIEW_DESTROYED:
                     _destroyView();
                     break;
                 case DESTROYED:
                     _destroy();
                     break;
                 case DETACHED:
                     _detach();
                     break;                    
                 default:
                     break;
             }
         }  
         
         _state = state;
         
         for (Controller childController : _childControllers) {
             childController.transitionTo(state);
         }
     }
     
     /**
      * Returns the view for this controller. 
      * 
      * This is not the view created in _onCreateView, use getInnerView() instead!
      * 
      * @return component view
      */
     public final View getView() {
         if (_innerView != null) {
             return _innerView;
         } else if (_stubView == null) {
             _stubView = new FrameLayout(getContext());
         }
         
         return _stubView;
     }
     
     /**
      * Returns the inner view for this controller.
      * 
      * This is the view created in _onCreateView.
      * 
      * @return controller view
      */
     public final View getInnerView() {
         return _innerView;
     }
     
     /**
      * Find (sub-)view.
      * 
      * @param id resource id
      * 
      * @return view
      */
     public View findViewById(int id) {
         if (getInnerView() == null) {
             return null;
         } else {
             return getInnerView().findViewById(id);
         }
     }    
     
     /**
      * Attach.
      */
     private void _attach() {
         _onAttach();
     }
     
     /**
      * On attach.
      */ 
     protected void _onAttach() {
         
     }    
     
     /**
      * Create.
      */
     private void _create() {
         _onCreate();
     }
     
     /**
      * On create.
      */ 
     protected void _onCreate() {
         
     }
     
     /**
      * Create the inner view for this controller.
      */
     private void _createView() {
         _innerView = _onCreateView(LayoutInflater.from(getContext()));
         
         if (_stubView != null && _stubView.getParent() instanceof ViewGroup) {
             ViewGroup parent = (ViewGroup)_stubView.getParent();
             int index = parent.indexOfChild(_stubView);
             ViewGroup.LayoutParams params = _stubView.getLayoutParams();
             parent.removeViewInLayout(_stubView);
             parent.addView(_innerView, index, params);
             _stubView = null;            
         }
         
         _onViewCreated(_innerView);
     }
 
     /**
      * Create the inner view for this controller. 
      * 
      * @param inflater  Layout inflater.
      * 
      * @return Controller view.
      */
     protected abstract View _onCreateView(LayoutInflater inflater);
     
     /**
      * Inner view created.
      * 
      * @param view The inner controller view.
      */
     protected void _onViewCreated(View view) {
         
     }
     
     /**
      * Start.
      */
     private void _start() {
         _onStart();
     }    
 
     /**
      * On start.
      */
     protected void _onStart() {
         
     }    
     
     /**
      * Resume.
      */
     private void _resume() {
         _onResume();
     }    
 
     /**
      * On resume.
      */
     protected void _onResume() {
         
     }    
     
     /**
      * Pause.
      */
     private void _pause() {
         _onPause();
     }    
 
     /**
      * On pause.
      */
     protected void _onPause() {
         
     }   
     
     /**
      * Stop.
      */
     private void _stop() {
         _onStop();
     }    
 
     /**
      * On stop.
      */
     protected void _onStop() {
         
     }   
     
     /**
      * Restart.
      */
     private void _restart() {    
         _onRestart();
         _onStart();
     }    
 
     /**
      * On restart.
      */
     protected void _onRestart() {
         
     }     
     
     /**
      * Destroy view.
      */
     private void _destroyView() {   
         _onDestroyView();
         _innerView = null;
         _stubView = null;
     }
     
     /**
      * Destroy view.
      * 
      * Override this method to do some clean-up if the view
      * is being destroyed.
      */
     protected void _onDestroyView() {
         
     }   
     
     /**
      * Destroy.
      */
     private void _destroy() {
         _onDestroy();
     }    
 
     /**
      * On destroy.
      */
     protected void _onDestroy() {
         
     }   
     
     /**
      * Detach.
      */
     private void _detach() {
         _onDetach();
     }    
 
     /**
      * On destroy.
      */
     protected void _onDetach() {
         
     }     
 }
