 package com.MusicalSketches;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.MusicalSketches.datarep.Library;
 import com.MusicalSketches.datarep.Note;
 import com.MusicalSketches.datarep.NoteFrequencies;
 import com.MusicalSketches.datarep.Song;
 
 public class MusicalLibrary extends Activity {
 	/** Called when the activity is first created. */
 	private ArrayAdapter<String> arrayAdapter;
 	private Library library;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.userlibrary);
 		setupFakeSongs();
 
 		TextView text = (TextView) findViewById(R.id.textView1);
 		text.setText("User Library");
 		ListView list1 = (ListView) findViewById(R.id.listView1);
 
 		arrayAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, songs);
 
 		list1.setAdapter(arrayAdapter);
 
 		list1.setTextFilterEnabled(true);
 
 		list1.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				// When clicked, show a toast with the TextView text
 				Toast.makeText(getApplicationContext(),
 						((TextView) view).getText(), Toast.LENGTH_SHORT).show();
 				Intent next = new Intent(MusicalLibrary.this, EditMode.class);
 				next.putExtra("song object",
 						library.getSong("" + ((TextView) view).getText()));
 				startActivityForResult(next, 0);
 			}
 		});
 		Button newComp = (Button) findViewById(R.id.textView2);
 		newComp.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Intent next = new Intent(MusicalLibrary.this, SongSelect.class);
 				startActivity(next);
 			}
 		});
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == 0) {
			if (data == null) {
				return;
			}
 			Song s = (Song) data.getSerializableExtra("song object");
 			library.remove(s.getTitle());
 			library.addSong(s);
 			Log.d("", "should have updated song");
 			String titles = "";
 			for (Song s1 : library.getSongs()) {
 				titles = s1.getTitle() + "; ";
 			}
 			Log.d("", titles);
 		}
 	}
 
 	public void setupFakeSongs() {
 		library = new Library();
 		Song s1 = new Song();
 		Log.d("", "new song make");
 		s1.setTitle("My Favorite Song");
 		Log.d("", "new song title");
 		s1.addNote(new Note(NoteFrequencies.getFrequency("e4"), 0.125,"e4"));
 		Log.d("", "new note");
 		s1.addNote(new Note(NoteFrequencies.getFrequency("e5"), 0.25,"e5"));
 		s1.addNote(new Note(NoteFrequencies.getFrequency("a4"), 0.5,"a4"));
 		s1.addNote(new Note(NoteFrequencies.getFrequency("b4"), 0.125,"b4"));
 		Log.d("", "all notes");
 		library.addSong(s1);
 
 		for (Song s : library.getSongs()) {
 			songs.add(s.getTitle());
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(Menu.NONE, DELETE, Menu.NONE, "DELETE");
 		menu.add(Menu.NONE, SORT, Menu.NONE, "SORT");
 		menu.add(Menu.NONE, HELP, Menu.NONE, "HELP!!!");
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case DELETE:
 			break;
 		case SORT:
 			Toast.makeText(this, "Sorting...", Toast.LENGTH_SHORT).show();
 			String[] s = (String[]) songs.toArray();
 			java.util.Arrays.sort(s);
 			songs = new ArrayList<String>();
 			for (String s1 : s) {
 				songs.add(s1);
 			}
 			ListView list1 = (ListView) findViewById(R.id.listView1);
 			arrayAdapter = new ArrayAdapter<String>(this,
 					android.R.layout.simple_list_item_1, songs);
 			list1.setAdapter(arrayAdapter);
 			break;
 		case HELP:
 			createHelpDialog();
 			break;
 		}
 		return false;
 	}
 
 	public void createHelpDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("I have no help for you here.")
 				.setCancelable(true)
 				.setPositiveButton("Sorry!",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	public static final int DELETE = 1;
 	public static final int SORT = 2;
 	public static final int HELP = 3;
 	public ArrayList<String> songs = new ArrayList<String>();
 }
