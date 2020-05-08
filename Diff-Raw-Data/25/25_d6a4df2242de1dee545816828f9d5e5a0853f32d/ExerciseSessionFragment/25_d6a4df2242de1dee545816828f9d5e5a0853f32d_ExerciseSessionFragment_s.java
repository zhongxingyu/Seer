 package com.pixeltreelabs.lift.android.ui;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.pixeltreelabs.lift.android.LiftApplication;
 import com.pixeltreelabs.lift.android.R;
 import com.pixeltreelabs.lift.android.event.ExerciseSessionFinishedEvent;
 import com.pixeltreelabs.lift.android.event.ViewMoreSessionsClickedEvent;
 import com.pixeltreelabs.lift.android.model.Exercise;
 import com.pixeltreelabs.lift.android.model.ExerciseSession;
 import com.pixeltreelabs.lift.android.model.ExerciseSessionStore;
 import com.pixeltreelabs.lift.android.model.ExerciseSet;
 import com.squareup.otto.Bus;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import butterknife.InjectView;
 import butterknife.Views;
 import timber.log.Timber;
 
 public class ExerciseSessionFragment extends Fragment {
     public static final String ARG_EXERCISE = "exercise";
     public static final String ARG_EXERCISE_SESSION = "exercise_session";
 
     private Exercise exercise;
     private ExerciseSession exerciseSession;
 
     @Inject Bus bus;
     @Inject Timber timber;
     @Inject ExerciseSessionStore exerciseSessionStore;
 
     @InjectView(R.id.new_set_list) LinearLayout newSetList;
     @InjectView(R.id.new_set) View newSet;
     @InjectView(R.id.complete_session) View completeSession;
     @InjectView(R.id.new_notes) EditText newNotes;
     @InjectView(R.id.last_set_list) LinearLayout lastSetList;
     @InjectView(R.id.more_sessions) TextView moreSessions;
 
     @Override public void onAttach(Activity activity) {
         super.onAttach(activity);
         ((LiftApplication) activity.getApplication()).inject(this);
     }
 
     @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
         final View view = inflater.inflate(R.layout.fragment_exercise_session, container, false);
         Views.inject(this, view);
 
         exercise = getArguments().getParcelable(ARG_EXERCISE);
         exerciseSession = getArguments().getParcelable(ARG_EXERCISE_SESSION);
         if (exercise == null) throw new RuntimeException();
 
         if (exerciseSession == null) {
             // Add the initial row.
             View exerciseSetRow = inflater.inflate(R.layout.new_set_item, container, false);
             newSetList.addView(exerciseSetRow);
         } else {
             List<ExerciseSet> sets = exerciseSession.getExerciseSets();
             for (ExerciseSet set : sets) {
                 View exerciseSetRow = inflater.inflate(R.layout.new_set_item, container, false);
 
                 EditText weightEditText = (EditText) exerciseSetRow.findViewById(R.id.weight);
                 EditText repsEditText = (EditText) exerciseSetRow.findViewById(R.id.reps);
 
                 weightEditText.setText(String.valueOf(set.getWeight()));
                 repsEditText.setText(String.valueOf(set.getNumReps()));
 
                 newSetList.addView(exerciseSetRow);
             }
 
             newNotes.setText(exerciseSession.getNotes());
         }
 
         newSet.setOnClickListener(new View.OnClickListener() {
             @Override public void onClick(View v) {
                 View exerciseSetRow = inflater.inflate(R.layout.new_set_item, container, false);
                 newSetList.addView(exerciseSetRow);
             }
         });
 
         completeSession.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 List<ExerciseSet> sets = new ArrayList<ExerciseSet>();
 
                 int childCount = newSetList.getChildCount();
                 for (int i = 0; i < childCount; i++) {
                     LinearLayout setLinearLayout = (LinearLayout) newSetList.getChildAt(i);
                     EditText weightEditText = (EditText) setLinearLayout.findViewById(R.id.weight);
                     EditText repsEditText = (EditText) setLinearLayout.findViewById(R.id.reps);
 
                     String weightString = weightEditText.getText().toString();
                     String repsString = repsEditText.getText().toString();
 
                     if (TextUtils.isEmpty(weightString) || TextUtils.isEmpty(repsString)) {
                         continue;
                     }
 
                     int weight = Integer.parseInt(weightEditText.getText().toString());
                     int reps = Integer.parseInt(repsEditText.getText().toString());
 
                     ExerciseSet set = new ExerciseSet(weight, reps);
                     timber.d("New exercise set created: %s", set);
                     sets.add(set);
                 }
 
                 String notes = newNotes.getText().toString();
 
                 ExerciseSession session = new ExerciseSession(exercise, sets, notes, new Date());
 
                 timber.d("Saving exercise session: %s", session);
                 exerciseSessionStore.save(session);
 
                 bus.post(new ExerciseSessionFinishedEvent());
             }
         });
 
         moreSessions.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 bus.post(new ViewMoreSessionsClickedEvent(exercise));
             }
         });
 
         // Populate last set from DAO.
         if (exercise == null) throw new RuntimeException();
         List<ExerciseSession> sessions = exerciseSessionStore.get(exercise);
 
        int currentSessionIndex = sessions.indexOf(exerciseSession);
        timber.d("Current session index is " + currentSessionIndex);
        if (currentSessionIndex != -1 && currentSessionIndex != sessions.size()-1) {
            ExerciseSession lastSession = sessions.get(currentSessionIndex + 1);
             List<ExerciseSet> sets = lastSession.getExerciseSets();
             for (ExerciseSet set : sets) {
                 View setRow = inflater.inflate(R.layout.last_set_item, container, false);
 
                 TextView weightEditText = (TextView) setRow.findViewById(R.id.weight);
                 TextView repsEditText = (TextView) setRow.findViewById(R.id.reps);
 
                 weightEditText.setText(getString(R.string.weight_x, set.getWeight()));
                 repsEditText.setText(getString(R.string.reps_x, set.getNumReps()));
 
                 lastSetList.addView(setRow);
 
                 // Add divider
                 View divider = inflater.inflate(R.layout.divider, container, false);
                 if (set != sets.get(sets.size() - 1)) {
                     lastSetList.addView(divider);
                 }
             }
         }
         return view;
     }
 
     @Override
     public void onStart() {
         super.onStart();
         bus.register(this);
 
         // Update the action bar.
         getActivity().getActionBar().setTitle(exercise.getName());
         getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     @Override
     public void onStop() {
         super.onStop();
         bus.unregister(this);
     }
 }
