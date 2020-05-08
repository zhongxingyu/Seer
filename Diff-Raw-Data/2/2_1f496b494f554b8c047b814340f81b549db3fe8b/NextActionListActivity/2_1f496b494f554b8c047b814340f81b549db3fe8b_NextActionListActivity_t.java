 package net.fibulwinter.gtd.presentation;
 
 import android.app.Activity;
 import android.content.ContentUris;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import net.fibulwinter.gtd.R;
 import net.fibulwinter.gtd.domain.*;
 import net.fibulwinter.gtd.infrastructure.TaskTableColumns;
 import net.fibulwinter.gtd.service.TaskListService;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import static com.google.common.collect.FluentIterable.from;
 import static com.google.common.collect.Lists.newArrayList;
 
 public class NextActionListActivity extends Activity {
 
     private TaskListService taskListService;
     private TextView todayCounter;
     private TextView overdueCounter;
     private Spinner contextSpinner;
     private TaskItemAdapter taskItemAdapter;
     private Context context = Context.ANY;
     private TaskItemAdapterConfig taskItemAdapterConfig;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.next_action_list);
         ListView taskList = (ListView) findViewById(R.id.taskList);
         todayCounter = (TextView) findViewById(R.id.dueTodayCounter);
         overdueCounter = (TextView) findViewById(R.id.overdueCounter);
         contextSpinner = (Spinner) findViewById(R.id.context_spinner);
 
         ContextRepository contextRepository = new ContextRepository();
         TaskRepository taskRepository = new TaskRepository(new TaskDAO(getContentResolver(), contextRepository));
         TaskUpdateListener taskUpdateListener = TaskUpdateListenerFactory.simple(this, taskRepository);
         taskListService = new TaskListService(taskRepository);
         taskItemAdapterConfig = new TaskItemAdapterConfig();
         taskItemAdapter = new TaskItemAdapter(this, taskUpdateListener, taskItemAdapterConfig);
         taskList.setAdapter(taskItemAdapter);
 
         SpinnerUtils.setupContextSpinner(this, contextRepository, contextSpinner, new SpinnerUtils.ContextSpinnerListener() {
             @Override
             public void onSelectedContext(Context context) {
                 NextActionListActivity.this.context = context;
                 fillData();
             }
         });
 
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         fillData();
     }
 
     private void fillData() {
         SpinnerUtils.setSelection(contextSpinner, context);
         Iterable<Task> tasks = from(taskListService.getNextActions()).filter(new Predicate<Task>() {
             @Override
             public boolean apply(Task task) {
                 return context.match(task);
             }
         });
         ArrayList<Task> taskArrayList = newArrayList(tasks);
         Collections.sort(taskArrayList, new Comparator<Task>() {
             @Override
             public int compare(Task task, Task task1) {
                 return task.getText().compareTo(task1.getText());
             }
         });
         taskItemAdapterConfig.setShowContext(context.isSpecial());
         taskItemAdapter.setData(taskArrayList);
         int todaySize = Iterables.size(taskListService.getTodayActions());
         todayCounter.setVisibility(todaySize > 0 ? View.VISIBLE : View.GONE);
         todayCounter.setText("" + todaySize + " action(s) need to be done today");
         int overdueSize = Iterables.size(taskListService.getOverdueActions());
         overdueCounter.setVisibility(overdueSize > 0 ? View.VISIBLE : View.GONE);
        overdueCounter.setText("" + overdueSize + " action(s) are overdue");
     }
 
     public void onOverdueCounter(View view) {
         context = Context.OVERDUE;
         fillData();
     }
 
     public void onDueTodayCounter(View view) {
         context = Context.TODAY;
         fillData();
     }
 
     public void onNewTask(View view) {
         Uri uri = ContentUris.withAppendedId(TaskTableColumns.CONTENT_URI, -1);
         Intent intent = new Intent("edit", uri, this, TaskEditActivity.class);
         intent.putExtra(TaskEditActivity.TYPE, TaskStatus.NextAction);
         startActivity(intent);
     }
 
 
 }
