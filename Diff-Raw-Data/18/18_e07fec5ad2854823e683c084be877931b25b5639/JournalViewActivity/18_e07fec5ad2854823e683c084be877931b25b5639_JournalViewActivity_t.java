 package mtu.notes;
 
 import java.io.File;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.os.Build;
 
 public class JournalViewActivity extends Activity {
 
 	//List of the journals
 	List<String> journalList;
 	//List of the notes in a specific journal
 	List<String> noteList;
 	//Map of journals with notes names as their values
 	Map<String, List<String>> journals;
 	ExpandableListView expList;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.journal_view);
 		
 		//Create the list of journals
 		try{
 			createGroupList();
 		}catch(FileNotFoundException e){
 			System.out.println("Journals file not found.");
 		}
 		
 		//Create the map of journals and notes
 		createCollection();
 		
 		expList = (ExpandableListView) findViewById(R.id.expList);
 		final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(
 				this, journalList, journals);
 		expList.setAdapter(expListAdapter);
 		
 		expList.setOnChildClickListener(new OnChildClickListener(){
 			public boolean onChildClick(ExpandableListView parent, View view, int groupPosition,
 					int childPosition, long id){
 				final String selected = (String) expListAdapter.getChild(groupPosition, childPosition);
 				Toast.makeText(getBaseContext(), selected, Toast.LENGTH_LONG).show();
 				return true;
 			}
 			
 		});
 		
 		// Show the Up button in the action bar.
 		setupActionBar();
 	}
 	
 	/**
 	 * Get all the journal names and add them to a list
 	 * @throws FileNotFoundException 
 	 */
 	private void createGroupList() throws FileNotFoundException{
 		journalList = new ArrayList<String>();
 		File file = new File(Environment.getExternalStorageDirectory() + "/category.txt");
 		if(file.exists()){
 			FileReader reader = new FileReader(file);
 			Scanner in = new Scanner(reader);
 			while(in.hasNext()){
 				journalList.add(in.next());
 			}
 		}
 		
 	}
 	
 	/**
 	 * Create the hashmap with journal names as keys and note names as values
 	 */
	public void createCollection(){
 		journals = new LinkedHashMap<String, List<String>>();
 		for(String journalName : journalList){
 			loadChild(journalName);
 			journals.put(journalName, noteList);
 		}
 	}
 	
 	/**
 	 * Gets all the note names based on the journal name
 	 * @param journalName
 	 */
 	private void loadChild(String journalName){
 		noteList = new ArrayList<String>();
 		File journalFolder = new File(Environment.getExternalStorageDirectory() + "/" + journalName);
 		for(File note : journalFolder.listFiles()){
 			noteList.add(note.getName());
 		}
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.journal_view, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void newCategory(View view)
 	{
 		EditText editText = (EditText) findViewById(R.id.newJournal);
 		if (editText.getText().toString().isEmpty())
 		{
 			Context context = getApplicationContext();
 			CharSequence text = "No journal name.";
 			int duration = Toast.LENGTH_SHORT;
 
 			Toast toast = Toast.makeText(context, text, duration);
 			toast.show();
 		}
 		else
 		{
 			//check if journal name folder exist. if so, toast journal already exists. if not, create and toast that it was created
 			File folder = new File(Environment.getExternalStorageDirectory() + "/" + editText.getText().toString());
 			boolean success;
 			if (!folder.exists()) {
 				success = folder.mkdir();
 				if (success) {
 					Toast.makeText(getApplicationContext(), "Directory Created", Toast.LENGTH_SHORT).show();
 					String msg = editText.getText().toString() + "\n";
 					//Write folder/journal name to file to load journals later.
 					File file = new File(Environment.getExternalStorageDirectory() + "/category.txt");
 					if(file.exists())
 					{
 						try
 						{
 							FileOutputStream out = new FileOutputStream(file,true);
 							out.write(msg.getBytes());
 							out.close();
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 					else
 					{
 						try
 						{
 							FileOutputStream out = new FileOutputStream(file);
 							out.write(msg.getBytes());
 							out.close();
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				} else {
 					Toast.makeText(getApplicationContext(), "Failed - Error", Toast.LENGTH_SHORT).show();
 				}
 			}
 			else
 			{
 				Toast.makeText(getApplicationContext(), "Folder Already Exists", Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 	
 	/**public void importPDF(View view)
 	{
 		Intent intent = new Intent(this,ImportPdfActivity.class);
 		startActivity(intent);
 	}**/
 }
