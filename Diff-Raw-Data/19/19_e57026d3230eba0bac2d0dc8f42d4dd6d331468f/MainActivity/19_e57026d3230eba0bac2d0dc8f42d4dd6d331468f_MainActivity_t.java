 package com.pixeltreelabs.lift.android.ui;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.MenuItem;
 
 import com.pixeltreelabs.lift.android.LiftApplication;
 import com.pixeltreelabs.lift.android.R;
 import com.pixeltreelabs.lift.android.event.ExerciseSelectedEvent;
 import com.pixeltreelabs.lift.android.event.ExerciseSessionFinishedEvent;
 import com.pixeltreelabs.lift.android.event.ExerciseSessionSelectedEvent;
 import com.pixeltreelabs.lift.android.event.NewExerciseClickedEvent;
 import com.pixeltreelabs.lift.android.event.ViewMoreSessionsClickedEvent;
 import com.pixeltreelabs.lift.android.model.Exercise;
 import com.pixeltreelabs.lift.android.model.ExerciseSession;
 import com.squareup.otto.Bus;
 import com.squareup.otto.Subscribe;
 import com.squareup.seismic.ShakeDetector;
 
 import javax.inject.Inject;
 
 import timber.log.Timber;
 
 public class MainActivity extends Activity implements ShakeDetector.Listener {
     @Inject Bus bus;
     @Inject Timber timber;
 
    private ShakeDetector shakeDetector;

     @Override protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         ((LiftApplication) getApplication()).inject(this);
 
         ExerciseListFragment exerciseListFragment = new ExerciseListFragment();
         getFragmentManager().beginTransaction().add(R.id.fragment_container, exerciseListFragment).commit();
     }
 
     @Override protected void onStart() {
         super.onStart();
         bus.register(this);

        // Shake detection
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(this);
        shakeDetector.start(sensorManager);
     }
 
     @Override protected void onStop() {
         super.onStop();
         bus.unregister(this);

        shakeDetector.stop();
     }
 
     @Override public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 getFragmentManager().popBackStack();
                 return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     @Override public void hearShake() {
         // Remove any currently showing lift stats dialogs.
         FragmentTransaction transaction = getFragmentManager().beginTransaction();
         Fragment prev = getFragmentManager().findFragmentByTag("lift_stats_dialog");
         if (prev != null) {
             transaction.remove(prev);
         }
         transaction.commit();
 
         // Create and show the new dialog.
         LiftStatsDialogFragment liftStatsDialogFragment = new LiftStatsDialogFragment();
         liftStatsDialogFragment.show(getFragmentManager(), "lift_stats_dialog");
     }
 
     @Subscribe public void onExerciseSelected(ExerciseSelectedEvent event) {
         timber.d("onExerciseSelected called");
         Exercise exercise = event.getExercise();
 
         // Construct the bundle to send to the fragment.
         Bundle args = new Bundle();
         args.putParcelable(ExerciseSessionFragment.ARG_EXERCISE, exercise);
 
         ExerciseSessionFragment exerciseSetFragment = new ExerciseSessionFragment();
         exerciseSetFragment.setArguments(args);
 
         FragmentTransaction transaction = getFragmentManager().beginTransaction();
         transaction.replace(R.id.fragment_container, exerciseSetFragment);
         transaction.addToBackStack(null);
         transaction.commit();
     }
 
     @Subscribe public void onExerciseSessionFinished(ExerciseSessionFinishedEvent e) {
         timber.d("onExerciseSessionFinished called");
 
         getFragmentManager().popBackStack();
     }
 
     @Subscribe public void onViewMoreSessionsClicked(ViewMoreSessionsClickedEvent e) {
         timber.d("onViewMoreSessionsClicked called");
         Exercise exercise = e.getExercise();
 
         // Construct the bundle to send to the fragment.
         Bundle args = new Bundle();
         args.putParcelable(ExerciseSessionListFragment.ARG_EXERCISE, exercise);
 
         ExerciseSessionListFragment exerciseSessionListFragment = new ExerciseSessionListFragment();
         exerciseSessionListFragment.setArguments(args);
 
         FragmentTransaction transaction = getFragmentManager().beginTransaction();
         transaction.replace(R.id.fragment_container, exerciseSessionListFragment);
         transaction.addToBackStack(null);
         transaction.commit();
     }
 
     @Subscribe public void onNewExerciseClicked(NewExerciseClickedEvent e) {
         timber.d("onNewExerciseClicked called");
 
         // Remove any currently showing new exercise dialogs.
         FragmentTransaction transaction = getFragmentManager().beginTransaction();
         Fragment prev = getFragmentManager().findFragmentByTag("new_exercise_dialog");
         if (prev != null) {
             transaction.remove(prev);
         }
         transaction.addToBackStack(null);
         transaction.commit();
 
         // Create and show the new dialog.
         NewExerciseDialogFragment newExerciseDialogFragment = new NewExerciseDialogFragment();
         newExerciseDialogFragment.show(getFragmentManager(), "new_exercise_dialog");
     }
 
     @Subscribe public void onExerciseSessionSelected(ExerciseSessionSelectedEvent e) {
         timber.d("onExerciseSessionSelected called");
 
         ExerciseSession session = e.getExerciseSession();
         Exercise exercise = session.getExercise();
 
         // Construct the bundle to send to the fragment.
         Bundle args = new Bundle();
         args.putParcelable(ExerciseSessionFragment.ARG_EXERCISE, exercise);
         args.putParcelable(ExerciseSessionFragment.ARG_EXERCISE_SESSION, session);
 
         ExerciseSessionFragment sessionFragment = new ExerciseSessionFragment();
         sessionFragment.setArguments(args);
 
         FragmentTransaction transaction = getFragmentManager().beginTransaction();
         transaction.replace(R.id.fragment_container, sessionFragment);
         transaction.addToBackStack(null);
         transaction.commit();
     }
 }
