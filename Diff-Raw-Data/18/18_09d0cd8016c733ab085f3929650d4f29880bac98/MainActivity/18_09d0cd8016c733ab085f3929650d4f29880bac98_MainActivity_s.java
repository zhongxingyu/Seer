 package net.briangbutterfield.twittersearchapp;
 
 import java.util.Arrays;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 
 public class MainActivity extends Activity
 {
 
 	private final String SAVED_SEARCHES_NAME = "searches";
 
 	private SharedPreferences _savedSearches = null;
 
 	private TableLayout _tagListTableLayout;
 	private Button _saveButton;
 	private Button _clearTagsButton;
 	private EditText _queryEditText;
 	private EditText _tagEditText;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		// Call super class.
 		super.onCreate(savedInstanceState);
 		// Associate the activity with the layout.
 		setContentView(R.layout.main);
 
 		// Retrieve any existing saved searches from user preferences.
 		_savedSearches = getSharedPreferences(SAVED_SEARCHES_NAME, MODE_PRIVATE);
 
 		// Get references to the views.
 		_queryEditText = (EditText) findViewById(R.id.queryEditText);
 		_tagEditText = (EditText) findViewById(R.id.tagEditText);
 		_saveButton = (Button) findViewById(R.id.saveButton);
 		_clearTagsButton = (Button) findViewById(R.id.clearTagsButton);
 		_tagListTableLayout = (TableLayout) findViewById(R.id.tagListTableLayout);
 
 		// Register event listeners for the buttons.
 		_saveButton.setOnClickListener(saveButtonListener);
 		_clearTagsButton.setOnClickListener(clearTagsButtonListener);

 		refreshTagList(null);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	public OnClickListener saveButtonListener = new OnClickListener()
 	{
 
 		@Override
 		public void onClick(View v)
 		{
 			String query = _queryEditText.getText().toString();
 			String tag = _tagEditText.getText().toString();
 
 			// Store Tag if query and tag values are not empty.
 			if (query.length() > 0 && tag.length() > 0)
 			{
 				storeTag(tag, query);
 
 				// Clear the contents so the user can enter a new tag value.
 				_tagEditText.setText("");
 				_queryEditText.setText("");
 			}
 			else
 			{
 				// Build and display an alert to the user.
 				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
 				alertBuilder.setTitle(R.string.missingTitle);
 				alertBuilder.setMessage(R.string.missingMessage);
 				alertBuilder.setPositiveButton(R.string.OK, null);
 
 				// Show the dialog to the user.
 				AlertDialog alert = alertBuilder.create();
 				alert.show();
 
 			}
 		}
 	};
 
 	// Prompt the user to delete all tags from saved preferences.
 	public OnClickListener clearTagsButtonListener = new OnClickListener()
 	{
 
 		@Override
 		public void onClick(View v)
 		{
 
 			// Alert the user with a dialog box confirming the clear.
 			// The setPositiveButton has a listener event that is registered to
 			// handle the clear.
 			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
 			alertBuilder.setTitle(R.string.confirmTitle);
 			alertBuilder.setMessage(R.string.confirmMessage);
 			alertBuilder.setCancelable(true);
 			alertBuilder.setPositiveButton(R.string.yes, deleteAllTagsConfirmButtonListener);
 			alertBuilder.setNegativeButton(R.string.cancel, null);
 
 			// Show the alert.
 			AlertDialog alert = alertBuilder.create();
 			alert.show();
 
 		}
 	};
 
 	// Clear all tags from saved searches based on confirmation.
 	public DialogInterface.OnClickListener deleteAllTagsConfirmButtonListener = new DialogInterface.OnClickListener()
 	{
 
 		@Override
 		public void onClick(DialogInterface dialog, int which)
 		{
 
 			// Delete all of the tag buttons.
 			deleteAllTags();
 
 			// Remove all of the saved tags from preferences.
 			_savedSearches.edit().clear().apply();
 		}
 	};
 
 	// Execute the saved tagged query and display the results in a web browser.
 	public OnClickListener queryButtonListener = new OnClickListener()
 	{
 
 		@Override
 		public void onClick(View v)
 		{
 			// Assign the saved tag text from the button to a string.
 			String tag = ((Button) v).getText().toString();
 
 			// Use tag to query the search text.
 			String searchText = _savedSearches.getString(tag, null);
 
 			// Build the search URL to pass to the intent.
			String url = getString(R.string.searchURL) + searchText;
 
 			// Sample URL:
 			// http://twitter.com/search?q=iqmetrix
 			// NOTE: search.twitter.com is not longer available.

 			// Create Intent and pass URL to launch a web browser.
 			Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 
 			// Execute the Intent.
 			startActivity(webIntent);
 
 		}
 	};
 
 	// Edit the tag name or search text of the touched item.
 	public OnClickListener editButtonListener = new OnClickListener()
 	{
 
 		@Override
 		public void onClick(View v)
 		{
 
 			// Get an instance of the "tag_row_view" which contains
 			// the tag and edit buttons.
 			TableRow tagTableRow = (TableRow) v.getParent();
 
 			// Get an instance of the tag button to retrieve
 			// the tag itself.
 			Button tagButton = (Button) tagTableRow.findViewById(R.id.tagButton);
 
 			// Now get the text value of the tag button.
 			String tag = tagButton.getText().toString();
 
 			// Populate the tag and query edit text views.
 			_tagEditText.setText(tag);
 			_queryEditText.setText(_savedSearches.getString(tag, null));
 
 		}
 	};
 
 	private void refreshTagList(String newTag)
 	{
 		// Retrieve stored tags from preferences.
 		String[] savedTags = _savedSearches.getAll().keySet().toArray(new String[0]);
 		// Sort tags.
 		Arrays.sort(savedTags, String.CASE_INSENSITIVE_ORDER);
 
 		// If new tag, insert into TableLayout at the appropriate index.
 		if (newTag != null)
 		{
 			addTagToTableLayout(newTag, Arrays.binarySearch(savedTags, newTag));
 		}
 		else
 		{
 			// Else, add all of the stored tags to the TableLayout in order.
 			for (int index = 0; index < savedTags.length; index++)
 			{
 				addTagToTableLayout(savedTags[index], index);
 			}
 		}
 
 	}
 
 	private void storeTag(String tag, String query)
 	{
 
 		// Set string to null if the tag is not found.
 		String existingQuery = _savedSearches.getString(tag, null);
 
 		// By tag, overwrite existing query or add new tag and query.
 		SharedPreferences.Editor preferenceEditor = _savedSearches.edit();
 		preferenceEditor.putString(tag, query);
 		preferenceEditor.apply();
 
 		// Tag not found, so add it to the list.
 		if (existingQuery == null)
 		{
 			refreshTagList(tag);
 		}
 	}
 
 	private void addTagToTableLayout(String tag, int index)
 	{
 
 		// Get reference to Layout Inflator in this context.
 		LayoutInflater inflator = getLayoutInflater();
 
 		View tagRowView = inflator.inflate(R.layout.tag_row_view, null);
 
 		// Set Tag button text and register event listener.
 		Button tagButton = (Button) tagRowView.findViewById(R.id.tagButton);
 		tagButton.setText(tag);
 		tagButton.setOnClickListener(queryButtonListener);
 
 		// Establish Edit button and register event listener.
 		Button editButton = (Button) tagRowView.findViewById(R.id.editTagButton);
 		editButton.setOnClickListener(editButtonListener);

 		// Add Tag and Edit buttons to the queryTableLayout.
 		_tagListTableLayout.addView(tagRowView, index);
 
 	}
 
 	private void deleteAllTags()
 	{
 		// Remove all saved search tags/buttons.
 		_tagListTableLayout.removeAllViews();
 	}
 }
