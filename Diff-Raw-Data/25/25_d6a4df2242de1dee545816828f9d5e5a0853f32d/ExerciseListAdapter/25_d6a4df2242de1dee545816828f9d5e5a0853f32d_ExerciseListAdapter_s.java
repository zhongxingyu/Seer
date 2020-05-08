 package com.pixeltreelabs.lift.android.ui;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.pixeltreelabs.lift.android.R;
 import com.pixeltreelabs.lift.android.model.Exercise;
 import com.pixeltreelabs.lift.android.model.ExerciseSession;
 import com.pixeltreelabs.lift.android.model.ExerciseSessionStore;
 import com.pixeltreelabs.lift.android.model.ExerciseSet;
 
 import java.util.List;
 
 import butterknife.InjectView;
 import butterknife.Views;
 
 public class ExerciseListAdapter extends ArrayAdapter<Exercise> {
     private final ExerciseSessionStore exerciseSessionStore;
 
     public ExerciseListAdapter(Context context, List<Exercise> exercises, ExerciseSessionStore exerciseSessionStore) {
         super(context, R.layout.list_item_exercise, exercises);
         this.exerciseSessionStore = exerciseSessionStore;
     }
 
     @Override public View getView(int position, View convertView, ViewGroup parent) {
         ViewHolder holder;
         if (convertView == null) {
             LayoutInflater inflater = LayoutInflater.from(getContext());
             convertView = inflater.inflate(R.layout.list_item_exercise, parent, false);
             holder = new ViewHolder(convertView);
             convertView.setTag(holder);
         } else {
             holder = (ViewHolder) convertView.getTag();
         }
 
         Exercise exercise = getItem(position);
         holder.name.setText(exercise.getName());
 
         if (position == getCount() - 1) {
             holder.name.setText("New Exercise");
             holder.weight.setText("-");
             holder.reps.setText("-");
         } else {
             List<ExerciseSession> exerciseSessions = exerciseSessionStore.get(exercise);
             if (!exerciseSessions.isEmpty()) {
                 ExerciseSession lastSession = exerciseSessions.get(0);
                 List<ExerciseSet> sets = lastSession.getExerciseSets();
                 int highestWeight = 0;
                 int associatedReps = 0;
                 for (ExerciseSet exerciseSet : sets) {
                     int weight = exerciseSet.getWeight();
                     int reps = exerciseSet.getNumReps();
                     if (weight > highestWeight || (weight == highestWeight && reps > associatedReps)) {
                         highestWeight = weight;
                         associatedReps = reps;
                     }
                 }
 
                 holder.weight.setText(getContext().getString(R.string.x_pounds, highestWeight));
                 holder.reps.setText(getContext().getString(R.string.x_reps, associatedReps));
             } else {
                 holder.weight.setText("-");
                 holder.reps.setText("-");
             }
         }
 
         return convertView;
    }
 
     static class ViewHolder {
         @InjectView(R.id.name) TextView name;
         @InjectView(R.id.weight) TextView weight;
         @InjectView(R.id.reps) TextView reps;
 
         public ViewHolder(View v) {
             Views.inject(ViewHolder.this, v);
         }
     }
 }
