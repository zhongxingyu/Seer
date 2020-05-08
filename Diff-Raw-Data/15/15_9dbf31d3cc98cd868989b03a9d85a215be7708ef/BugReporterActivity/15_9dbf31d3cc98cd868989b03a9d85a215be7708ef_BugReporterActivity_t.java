 /*
  *  Copyright 2010 Heroic Efforts, LLC
  *  
  *  This file is part of Viable.
  *
  *  Viable is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Viable is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Viable.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.heroicefforts.viable.android;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import net.heroicefforts.viable.android.content.IssueContentAdapter;
 import net.heroicefforts.viable.android.content.Issues;
 import net.heroicefforts.viable.android.dao.BugContext;
 import net.heroicefforts.viable.android.dao.Comment;
 import net.heroicefforts.viable.android.dao.Issue;
 import net.heroicefforts.viable.android.dist.BugReportIntent;
 import net.heroicefforts.viable.android.rep.IssueResource;
 import net.heroicefforts.viable.android.rep.Repository;
 import net.heroicefforts.viable.android.rep.RepositoryFactory;
 import net.heroicefforts.viable.android.rep.ServiceException;
 
 import org.apache.http.HttpStatus;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 /**
  * This class handles the collection and submission of user issues.  The issue submitted may be due to an application crash or
  * independently submitted by the user.
  * 
  * @author jevans
  *
  */
 public class BugReporterActivity extends Activity
 {
 	private static final String TAG = "BugReporterActivity";
 	private static final String EOL = System.getProperty("line.separator");
 	
 	private Spinner typeSpinner;
 	private Spinner appNameSpinner;
 	private TextView summaryText;
 	private TextView descriptionText;
 	private TextView detailsTextView;
 	private Button detailsButton;
 	private View detailsScroll;
 	
 	private RepositoryFactory factory;
 	private Intent handled;
 	private boolean comment;
 	private Issue curIssue;
 	private String stacktrace;
 
 	
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.bug_report_activity);
             	
         ((Button) findViewById(R.id.ReportBugButton)).setOnClickListener(clickListener);
         ((Button) findViewById(R.id.DetailsButton)).setOnClickListener(clickListener);
 
         appNameSpinner = (Spinner) findViewById(R.id.AppNameSpinner);    	
     	typeSpinner = (Spinner) findViewById(R.id.IssueTypeSpinner); 
 		summaryText = (TextView) findViewById(R.id.BugSummaryEditText);
 		descriptionText = (TextView) findViewById(R.id.BugDescriptionEditText);
 		detailsTextView = (TextView) findViewById(R.id.BugDetailsTextView);
         detailsButton = (Button) findViewById(R.id.DetailsButton);
         detailsScroll = (View) findViewById(R.id.BugDetailsScrollView);
         
         factory = new RepositoryFactory(this);
     	
 		List<String> appNames = new ArrayList<String>();
 		appNames.add(getString(R.string.choose_one));
 		appNames.addAll(factory.getApplicationNames());		
     	appNameSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, appNames));
     	appNameSpinner.setOnItemSelectedListener(appNameSelected);
     	
     	Set<? extends IssueResource> resources = factory.getRepository(appNames.get(1)).getDefaultStates();
 		typeSpinner.setAdapter(new IssueSelectionAdapter(this, resources)); 
 		
 		detailsButton.setVisibility(View.INVISIBLE);		
     }        
     
     @Override
     public void onStart()
     {
     	super.onStart();
     	
     	Intent intent;
     	if(getParent() != null && (getParent() instanceof IssueTabsActivity))
     		intent = getParent().getIntent();
     	else
     		intent = getIntent();
Log.d(TAG, "Handled:" + handled + "=" + intent);    	
     	if(handled != intent)
     	{
     		try
 			{
 				BugReportIntent bri = new BugReportIntent(intent);
 				if(bri.isValid())
				{
Log.d(TAG, "Bri is valid.");					
 					setDefectState(bri.getAppName(), bri.getStacktrace());
				}
				else
Log.d(TAG, "Bri is not valid.");					
 				handled = intent;
 			}
 			catch (ServiceException e)
 			{
 				Log.e(TAG, "Error initiating Viable bug reporter.", e);
 			}
     	}    	
     }
 
 	@SuppressWarnings("unchecked")
 	private void setDefectState(String appName, String stacktrace) 
 		throws ServiceException
 	{		
 		this.stacktrace = stacktrace;
 		appNameSpinner.setSelection(((ArrayAdapter<String>)appNameSpinner.getAdapter()).getPosition(appName));
 		appNameSpinner.setEnabled(false);		
     	Set<? extends IssueResource> resources = factory.getRepository(appName).getDefaultDefectStates();
 		typeSpinner.setAdapter(new IssueSelectionAdapter(this, resources));  
Log.d(TAG, "" + resources.size() + " resources.");
 		BugContext ctx = new BugContext();
 		ctx.setAppName(appName);
 		ctx.setStacktrace(stacktrace);
 		
 		duplicateIssueCheck(ctx);
 		
 		detailsTextView.setText(formatDetails(ctx));
 		((Button) findViewById(R.id.DetailsButton)).setVisibility(View.VISIBLE);
 	}
 
 	/**
 	 * Check if the defect has already been reported to the application developers.
 	 * 
 	 * @param ctx the defect
 	 * @throws ServiceException if there's an error contacting the remote repository.
 	 */
 	private void duplicateIssueCheck(Issue ctx) 
 		throws ServiceException
 	{
     	Repository rep = factory.getRepository(ctx.getAppName());
     	if(rep == null)
     		abort("Application '" + ctx.getAppName() + "' is not properly configured...see errors above.  Shutting down Viable.");
 
 		Issue issue = rep.exists(ctx);
 		curIssue = issue;
     	if(issue != null) //issue recorded remotely
     	{
     		Cursor cur = this.getContentResolver().query(Issues.CONTENT_URI, new String[] { Issues.APP_NAME, Issues.ISSUE_ID, Issues.SUMMARY }, Issues.APP_NAME + " = ? and " + Issues.ISSUE_ID + " = ?", new String[] { ctx.getAppName(), issue.getIssueId() }, Issues.DEFAULT_SORT_ORDER);
     		if(cur.moveToFirst()) //issue encountered locally
     			abort("The user has already encountered issue '" + issue.getIssueId() + "'.  Closing the activity.");
     		else
     			showSupplementDialog(issue);        			
     	}    		
 	}
 	
 	/**
 	 * Abort the application launch.
 	 * 
 	 * @param msg the message to log.
 	 */
 	private void abort(String msg)
 	{
 		Log.d(TAG, msg);
 		if(getParent() != null)
 			getParent().finish();
 		else
 			finish();
 	}
 
 	/**
 	 * Issue already exists.  Now prompt the user whether they want to add an additional comment.
 	 *  
 	 * @param issue the defect
 	 */
 	private void showSupplementDialog(Issue issue)
 	{
 		Log.d(TAG, "Prompting user to comment on existing issue '" + issue.getIssueId() + "'.");
 		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(R.string.bug_comment);
 		builder.setCancelable(true);
 		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog, int which) {
 		    	comment = true;
 		    }
 		});
 		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog, int which) {
 		    	finish();
 		    }
 		});
 		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
 		    public void onCancel(DialogInterface dialog) {
 		    	finish();
 		    }
 		});
 		builder.setMessage(getString(R.string.bug_comment_prompt).replaceAll("\\{appName\\}", issue.getAppName()));
 		builder.create().show();
 	}
  
 	/**
 	 * Dumps the issue details collected by the system to a string.  Allow the user to see what will be submitted.
 	 * @param issue the defect
 	 * @return a pretty printed string of issue details.
 	 */
     protected String formatDetails(Issue issue)
     {
     	final String EOL = System.getProperty("line.separator");
     	StringBuilder buf = new StringBuilder();
     	buf.append(getString(R.string.app_label)).append(issue.getAppName()).append(EOL);
     	if(issue instanceof BugContext)
     	{
 	    	if(issue.getAffectedVersions().length > 0)
 	    		buf.append(getString(R.string.version_name)).append(issue.getAffectedVersions()[0]).append(EOL);
 	    	buf.append(getString(R.string.phone_model)).append(Build.MODEL).append(EOL);
 	    	buf.append(getString(R.string.phone_device)).append(Build.DEVICE).append(EOL);
 	    	buf.append(getString(R.string.phone_sdk)).append(Build.VERSION.SDK_INT).append(EOL);
     	}
     	buf.append(getString(R.string.error)).append(issue.getStacktrace());
     	
     	return buf.toString();
     }
     
 	private OnItemSelectedListener appNameSelected = new OnItemSelectedListener() {
 
 		public void onItemSelected(AdapterView<?> v, View arg1, int position, long id)
 		{
			if(position > 0 && v.isEnabled())
 			{
 				String appName = (String) v.getAdapter().getItem(v.getLastVisiblePosition()); 
 		    	Set<? extends IssueResource> resources = factory.getRepository(appName).getDefaultStates();
 				typeSpinner.setAdapter(new IssueSelectionAdapter(BugReporterActivity.this, resources));
 			}
 		}
 
 		public void onNothingSelected(AdapterView<?> arg0)
 		{
 			//empty
 		}
 		
 	};
     
 	private OnClickListener clickListener = new OnClickListener()
 	{
 
 		public void onClick(View v)
 		{
 			if(v.getId() == R.id.ReportBugButton)
 			{
 				if(appNameSpinner.getLastVisiblePosition() == 0)
 				{
 					appNameSpinner.requestFocus();
 					return;
 				}
 				
 				String appName = (String) appNameSpinner.getAdapter().getItem(appNameSpinner.getLastVisiblePosition());
 				String summary = summaryText.getText().toString();
 		        String desc = descriptionText.getText().toString();
 
 		        if(TextUtils.isEmpty(summary))
 		        {
 					summaryText.requestFocus();
 					return;
 		        }
 
 		        int position = typeSpinner.getLastVisiblePosition();
 		        IssueResource resource = (IssueResource) typeSpinner.getAdapter().getItem(position);
 		        if(comment)
 		        	reportComment(appName, summary, desc);
 		        else
 		        	reportBug(appName, summary, desc, resource);
 			        
 				Activity parent = BugReporterActivity.this.getParent();
 				if(parent != null && parent instanceof IssueTabsActivity)
 					((IssueTabsActivity) parent).selectMyIssuesTab();			        
 			}
 			else if(v.getId() == R.id.DetailsButton)
 			{
 				if(detailsScroll.getVisibility() == View.VISIBLE)
 				{
 					detailsButton.setText(getText(R.string.show_details));
 					detailsScroll.setVisibility(View.INVISIBLE);
 				}
 				else
 				{
 					detailsButton.setText(getText(R.string.hide_details));
 					detailsScroll.setVisibility(View.VISIBLE);
 				}
 				
 			}
 		}
 
 	};
 
 	private void reportComment(String appName, String summary, String desc)
 	{			
 		Comment comment = new Comment(summary + EOL + EOL + desc);
 		new ReportCommentTask().execute(comment);
 	}
 
 	private void reportBug(String appName, String summary, String desc, IssueResource resource)
 	{
 		curIssue = new BugContext();
 		resource.setState(curIssue);
 		curIssue.setAppName(appName);
 		curIssue.setSummary(summary);
 		curIssue.setDescription(desc);
 		curIssue.setStacktrace(stacktrace);
 		curIssue.setAffectedVersions(new String[] { factory.getApplicationVersion(appName) });
 		
 		new ReportBugTask().execute(curIssue);  						
 	}
 
 	/**
 	 * Asynchronous task for submitting the issue comment to the remote repository. 
 	 */
 	private class ReportCommentTask extends AsyncTask<Comment, Void, ServiceException>
 	{
 		@Override
 		protected ServiceException doInBackground(Comment... params)
 		{
 			try
 			{
 				Comment comment = params[0];
 				int responseCode = factory.getRepository(curIssue.getAppName()).postIssueComment(curIssue, comment);
 				saveMyIssue(responseCode, curIssue);
 				BugReporterActivity.this.comment = false;
 				curIssue = null;
 				return null;
 			}
 			catch (ServiceException e)
 			{
 				return e;
 			}			
 		}
 		
 		protected void onPostExecute(ServiceException e)
 		{
 			if(e == null)
 				clearState();
 			else
 				Error.handle(BugReporterActivity.this, e);				
 		}
 		
 	}	
 
 	/**
 	 * Save the issue to the local content repository.
 	 * @param responseCode the post response code.
 	 * @param curIssue the issue to save
 	 */
 	private void saveMyIssue(int responseCode, Issue curIssue)
 	{
 		if(HttpStatus.SC_CREATED == responseCode || HttpStatus.SC_OK == responseCode)
 		{			
 			ContentValues values = new IssueContentAdapter(curIssue).toContentValues();
 			BugReporterActivity.this.getContentResolver().insert(Issues.CONTENT_URI, values);
 		}
 	}
 	
 	/**
 	 * Asynchronous task for submitting the issue to the remote repository. 
 	 */
 	private class ReportBugTask extends AsyncTask<Issue, Void, ServiceException> 
 	{
 		@Override
 		protected ServiceException doInBackground(Issue... params)
 		{
 			try {
 				Issue curIssue = params[0];
 				int responseCode = factory.getRepository(curIssue.getAppName()).postIssue(curIssue);
 				saveMyIssue(responseCode, curIssue);
 				curIssue = null;
 				
 				return null;
 			}
 			catch(ServiceException e)
 			{
 				return e;
 			}
 		}
 
 		protected void onPostExecute(ServiceException e)
 		{
 			if(e == null)
 				clearState();				
 			else
 				Error.handle(BugReporterActivity.this, e);				
 		}
 
 	}
 
 	/**
 	 * Reset the view state after the issue has been successfully submitted.
 	 */
 	private void clearState()
 	{
 		appNameSpinner.setEnabled(true);
 		summaryText.setText("");
 		descriptionText.setText("");
 		detailsTextView.setText("");
 		detailsButton.setText(getText(R.string.show_details));
 		detailsScroll.setVisibility(View.INVISIBLE);
         stacktrace = null;		
 	}
 		
 }
