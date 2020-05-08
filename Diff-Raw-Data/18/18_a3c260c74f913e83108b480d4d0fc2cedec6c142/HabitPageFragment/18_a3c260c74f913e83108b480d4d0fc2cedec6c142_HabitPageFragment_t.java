 package com.tknilsson.habitboss.ui;
 
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.tknilsson.habitboss.R;
 import com.tknilsson.habitboss.model.Habit;
 import com.tknilsson.habitboss.model.HabitsManager;
 
 import org.joda.time.DateTime;
 
 public class HabitPageFragment extends Fragment {
 
     private Habit.TimeWindow timeWindow;
 
     public String getPageName(){
         int actionCount = HabitsManager.countActionable(timeWindow);
         if(actionCount == 0){
             return timeWindow.toString();
         } else {
             return timeWindow.toString()+" ("+actionCount+")";
         }
     }
 
     public HabitPageFragment() {
 
     }
 
     public HabitPageFragment(Habit.TimeWindow timeWindow) {
         this.timeWindow = timeWindow;
     }
 
    private static String TIMEWINDOW_KEY = "timeWindow";

     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            String savedTimewindow = savedInstanceState.getString(TIMEWINDOW_KEY);
            if (savedTimewindow != null){
                timeWindow = Habit.TimeWindow.valueOf(savedTimewindow);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TIMEWINDOW_KEY, timeWindow.toString());
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         return inflater.inflate(R.layout.habit_page, container, false);
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         initUI();
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
     }
 
     HabitListAdapter doHabitAdapter = null;
 
     public void initUI(){
         doHabitAdapter = HabitsManager.getListAdapter(getView().getContext(), timeWindow);
         ListView doListView = (ListView) getView().findViewById(R.id.habit_making_list);
         doListView.setAdapter(doHabitAdapter);
         updateEditContextAwareElements(MainActivity.editingHabits);
     }
 
     public void updateEditContextAwareElements(boolean editingHabits){
         if (getView() != null && getView().findViewById(R.id.habit_adding) != null){
             if(!editingHabits){
                 getView().findViewById(R.id.habit_adding).setVisibility(View.GONE);
             } else {
                 getView().findViewById(R.id.habit_adding).setVisibility(View.VISIBLE);
             }
             doHabitAdapter.markHabitsChanged();
         }
     }
 
     public void addHabit(View view) {
         EditText newHabitText = ((EditText)getView().findViewById(R.id.new_habit_text));
         Habit habit = new Habit(timeWindow, newHabitText.getEditableText().toString());
         doHabitAdapter.addNewHabit(habit);
         newHabitText.setText("");
 
         String feedback = view.getContext().getString(R.string.new_habit_added)+ " ";
         if(timeWindow.equals(Habit.TimeWindow.DAILY)){
            feedback = feedback + view.getContext().getString(R.string.daily_habit_added_feedback);
         } else if(timeWindow.equals(Habit.TimeWindow.WEEKLY)){
             feedback = feedback + view.getContext().getString(R.string.weekly_habit_added_feedback);
         } else if(timeWindow.equals(Habit.TimeWindow.MONTHLY)){
             feedback = feedback + view.getContext().getString(R.string.monthly_habit_added_feedback);
         } else {
             throw new RuntimeException("Unexpected timewindow type");
         }
 
         Toast.makeText(getActivity(), feedback, Toast.LENGTH_LONG).show();
     }
 
 }
