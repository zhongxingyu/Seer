 package com.github.mobileartisans.bawall;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.*;
 import com.github.mobileartisans.bawall.component.ItemSelectedListener;
 import com.github.mobileartisans.bawall.component.ProgressAsyncTask;
 import com.github.mobileartisans.bawall.domain.AkhadaClient;
 import com.github.mobileartisans.bawall.domain.Issue;
 import com.github.mobileartisans.bawall.domain.Transition;
 import com.github.mobileartisans.bawall.domain.UserPreference;
 
 import java.util.List;
 
 public class IssueViewActivity extends Activity {
     public static final String ISSUE_KEY = "issueKey";
     private String issueKey;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.issue);
         issueKey = getIntent().getExtras().getString(ISSUE_KEY);
         setTitle("Loading issue details");
         new IssueDetailsTask(this).execute(issueKey);
     }
 
     public class IssueDetailsTask extends ProgressAsyncTask<String, Void, Issue> {
 
         protected IssueDetailsTask(Context context) {
             super(context);
         }
 
         @Override
         protected Issue doInBackground(String... issueKeys) {
             UserPreference.Preference preference = new UserPreference(IssueViewActivity.this).getPreference();
             return new AkhadaClient(preference).getIssue(issueKeys[0]);
         }
 
         @Override
         protected void onPostExecute(Issue issue) {
             super.onPostExecute(issue);
             Button issueAssignee = (Button) findViewById(R.id.issueAssignee);
             TextView issueSummary = (TextView) findViewById(R.id.issueSummary);
             Spinner issueTransitions = (Spinner) findViewById(R.id.issueTransitions);
             issueTransitions.setOnItemSelectedListener(new UpdateIssueStatus(issueTransitions.getSelectedItemPosition()));
             issueAssignee.setText(issue.getAssignee());
             issueSummary.setText(issue.getSummary());
            setTitle(issue.getKey());
             SpinnerAdapter adapter = new ArrayAdapter<Transition>(IssueViewActivity.this, android.R.layout.simple_spinner_dropdown_item, issue.getTransitions());
             issueTransitions.setAdapter(adapter);
         }
     }
 
     public void onListAssignee(View view) {
         new IssueAssigneeTask(this).execute(issueKey);
     }
 
     public class IssueAssigneeTask extends ProgressAsyncTask<String, Void, List<String>> {
 
         protected IssueAssigneeTask(Context context) {
             super(context);
         }
 
         @Override
         protected List<String> doInBackground(String... issueKeys) {
             UserPreference.Preference preference = new UserPreference(IssueViewActivity.this).getPreference();
             return new AkhadaClient(preference).getAssignees(issueKeys[0]).getUsers();
         }
 
         @Override
         protected void onPostExecute(List<String> strings) {
             super.onPostExecute(strings);
             SpinnerAdapter adapter = new ArrayAdapter<String>(IssueViewActivity.this, android.R.layout.simple_spinner_dropdown_item, strings);
             Spinner assigneeList = (Spinner) findViewById(R.id.issueAssigneeList);
             assigneeList.setOnItemSelectedListener(new UpdateIssueAssignee(assigneeList.getSelectedItemPosition()));
             assigneeList.setAdapter(adapter);
             assigneeList.performClick();
         }
     }
 
     private class UpdateIssueAssignee extends ItemSelectedListener {
         protected UpdateIssueAssignee(int selectedItem) {
             super(selectedItem);
         }
 
         @Override
         public void onSelectItem(AdapterView<?> parentView, View selectedItemView, int position, long id) {
             String selectedAssignee = (String) parentView.getItemAtPosition(position);
             Button assignee = (Button) findViewById(R.id.issueAssignee);
             assignee.setText(selectedAssignee);
             Toast.makeText(IssueViewActivity.this, selectedAssignee, Toast.LENGTH_SHORT).show();
         }
 
     }
 
     private class UpdateIssueStatus extends ItemSelectedListener {
         protected UpdateIssueStatus(int selectedItem) {
             super(selectedItem);
         }
 
         @Override
         public void onSelectItem(AdapterView<?> parentView, View selectedItemView, int position, long id) {
             Transition selectedState = (Transition) parentView.getItemAtPosition(position);
             new UpdateIssueTask(IssueViewActivity.this).execute(selectedState);
             Toast.makeText(IssueViewActivity.this, selectedState.getName(), Toast.LENGTH_SHORT).show();
         }
 
     }
 
     private class UpdateIssueTask extends ProgressAsyncTask<Transition, Void, Void> {
         protected UpdateIssueTask(Context context) {
             super(context);
         }
 
         @Override
         protected Void doInBackground(Transition... status) {
             UserPreference.Preference preference = new UserPreference(IssueViewActivity.this).getPreference();
             new AkhadaClient(preference).updateStatus(issueKey, status[0]);
             return null;
         }
     }
 }
