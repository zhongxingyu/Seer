 package com.github.mobileartisans.bawall;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
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
         bindBackButton();
     }
 
     public class IssueDetailsTask extends ProgressAsyncTask<String, Void, Issue> {
 
         protected IssueDetailsTask(Context context) {
             super(context);
         }
 
         @Override
         protected Issue process(String... issueKeys) {
             UserPreference.Preference preference = new UserPreference(IssueViewActivity.this).getPreference();
             return new AkhadaClient(preference).getIssue(issueKeys[0]);
         }
 
         @Override
         protected void onSuccess(Issue issue) {
             Button issueAssignee = (Button) findViewById(R.id.issueAssignee);
             TextView issueSummary = (TextView) findViewById(R.id.issueSummary);
             TextView issueTitle = (TextView) findViewById(R.id.issueTitle);
             Spinner issueTransitions = (Spinner) findViewById(R.id.issueTransitions);
             issueTransitions.setOnItemSelectedListener(new UpdateIssueStatus(issueTransitions.getSelectedItemPosition()));
             issueAssignee.setText(issue.getAssignee());
             issueSummary.setText(issue.getSummary());
             issueTitle.setText(issue.getKey());
             SpinnerAdapter adapter = new ArrayAdapter<Transition>(IssueViewActivity.this, android.R.layout.simple_spinner_dropdown_item, issue.getTransitions());
             issueTransitions.setAdapter(adapter);
         }
 
         @Override
         protected void onNotFound() {
            Toast.makeText(context, issueKey + " not found", Toast.LENGTH_SHORT).show();
             startActivity(new Intent(IssueViewActivity.this, HomeActivity.class));
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
         protected List<String> process(String... issueKeys) {
             UserPreference.Preference preference = new UserPreference(IssueViewActivity.this).getPreference();
             return new AkhadaClient(preference).getAssignees(issueKeys[0]).getUsers();
         }
 
         @Override
         protected void onSuccess(List<String> strings) {
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
             new UpdateIssueAssigneeTask(IssueViewActivity.this).execute(selectedAssignee);
             Toast.makeText(IssueViewActivity.this, selectedAssignee, Toast.LENGTH_SHORT).show();
         }
 
     }
 
     private class UpdateIssueAssigneeTask extends ProgressAsyncTask<String, Void, Void> {
 
         protected UpdateIssueAssigneeTask(Context context) {
             super(context);
         }
 
         @Override
         protected Void process(String... assignees) {
             UserPreference.Preference preference = new UserPreference(IssueViewActivity.this).getPreference();
             new AkhadaClient(preference).updateAssignee(issueKey, assignees[0]);
             return null;
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
         protected Void process(Transition... status) {
             UserPreference.Preference preference = new UserPreference(IssueViewActivity.this).getPreference();
             new AkhadaClient(preference).updateStatus(issueKey, status[0]);
             return null;
         }
     }
 
     private void bindBackButton() {
         ImageView backButton = (ImageView) this.findViewById(R.id.backButton);
         backButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 IssueViewActivity.this.finish();
             }
         });
     }
 }
