 package mtu.notes;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.LineNumberReader;
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.SlidingDrawer;
 import android.widget.Spinner;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Build;
 
 public class NewNoteActivity extends Activity {
 	
 	private ToggleButton toggle;
 
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.new_note);
 		Spinner spinner = (Spinner) findViewById(R.id.journalspinner);
 		
 		//Initially bring the notetext to the front, then put the drawer in front of that
 		EditText notetext = (EditText) findViewById(R.id.notetext);
 		notetext.bringToFront();
 		SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
 		drawer.bringToFront();
 		
 		//When the toggle button is clicked
 		toggle = (ToggleButton) findViewById(R.id.drawToggle);
 		toggle.setOnClickListener(new OnClickListener(){
 			public void onClick(View v){
 				SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
 				//Bring scribbleView to the front
 				if(toggle.isChecked()){
 					ScribbleView scribbles = (ScribbleView) findViewById(R.id.scribbles);
 					scribbles.bringToFront();
 					drawer.bringToFront();
 				}
 				//Bring noteView to the front
 				else{
 					EditText notetext = (EditText) findViewById(R.id.notetext);
 					notetext.bringToFront();
 					drawer.bringToFront();
 				}
 			}
 		});
 		
 		ArrayList<String> list = new ArrayList<String>();
 		File file = new File(Environment.getExternalStorageDirectory() + "/category.txt");
 		if(file.exists())
 		{
 			String read;
 			try {
 				LineNumberReader in = new LineNumberReader(new FileReader(Environment.getExternalStorageDirectory() +"/category.txt"));
 				while((read = in.readLine()) != null)
 				{
 					System.out.println(read);
 					list.add(read);
 				}
 				in.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		list.add("None");
 		
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list);
 		// Specify the layout to use when the list of choices appears
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		// Apply the adapter to the spinner
 		spinner.setAdapter(adapter);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		
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
 		getMenuInflater().inflate(R.menu.new_note, menu);
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
 
 	public void save(View view)
 	{
 		EditText text = (EditText) findViewById(R.id.notetext);
		ScribbleView scribble = (ScribbleView) findViewById(R.id.scribbles);
 		Spinner cat = (Spinner) findViewById(R.id.journalspinner);
 		if(text != null) {
 			EditText editText = (EditText)findViewById(R.id.titletext);
			scribble.Save(editText, cat, getApplicationContext());
 			if(!editText.getText().toString().isEmpty())
 			{
 				int count = -1;
 				String filename = editText.getText().toString() + ".txt";
 				String path;
 				if(cat.getSelectedItem().toString().equals("None"))
 				{
 					path = Environment.getExternalStorageDirectory().getPath();
 				}
 				else
 				{
 					path = Environment.getExternalStorageDirectory().getPath() + "/" + cat.getSelectedItem().toString();
 				}
 				File file = new File(path, filename);
 				while(file.exists())
 				{
 					count++;
 					filename = editText.getText().toString() + count + ".txt";
 					file = new File(Environment.getExternalStorageDirectory().getPath(), filename);
 				}
 				try {
 					FileOutputStream out = new FileOutputStream(file);
 					out.write(text.getText().toString().getBytes());
 					out.close();
 					Context context = getApplicationContext();
 					CharSequence toastText = filename + " saved.";
 					int duration = Toast.LENGTH_SHORT;
 
 					Toast toast = Toast.makeText(context, toastText, duration);
 					toast.show();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			else
 			{
 				int count = 0;
 				String filename = "note" + count + ".txt";
 				File file = new File(Environment.getExternalStorageDirectory().getPath(), filename);
 				while(file.exists())
 				{
 					count++;
 					filename = "note" + count + ".txt";
 					file = new File(Environment.getExternalStorageDirectory().getPath(), filename);
 				}
 
 				try {
 					FileOutputStream out = new FileOutputStream(file);
 					out.write(text.getText().toString().getBytes());
 					out.close();
 					Context context = getApplicationContext();
 					CharSequence toastText = filename + " saved.";
 					int duration = Toast.LENGTH_SHORT;
 
 					Toast toast = Toast.makeText(context, toastText, duration);
 					toast.show();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public void highlighterYellow(View view){
 		ScribbleView.highlightColor(Color.YELLOW);
 	}
 	
 	public void highlighterBlue(View view){
 		ScribbleView.highlightColor(Color.BLUE);
 	}
 	
 	public void highlighterGreen(View view){
 		ScribbleView.highlightColor(Color.GREEN);
 	}
 	
 	//Maybe or maybe not this one
 	public void highlighterPink(View view){
 		ScribbleView.highlightColor(Color.MAGENTA);
 	}
 	
 	public void penBlack(View view){
 		ScribbleView.penColor(Color.BLACK);
 	}
 	
 	public void penRed(View view){
 		ScribbleView.penColor(Color.RED);
 	}
 	
 	public void penBlue(View view){
 		ScribbleView.penColor(Color.BLUE);
 	}
 	
 	public void penGreen(View view){
 		ScribbleView.penColor(Color.GREEN);
 	}
 	
 	
 	
 
 }
