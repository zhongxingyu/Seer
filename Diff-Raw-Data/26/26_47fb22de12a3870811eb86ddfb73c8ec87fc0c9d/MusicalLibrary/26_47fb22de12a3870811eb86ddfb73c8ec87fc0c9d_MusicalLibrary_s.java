 package com.MusicalSketches;
 
 import java.io.FileInputStream;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.MusicalSketches.datarep.Library;
 import com.MusicalSketches.datarep.Note;
 import com.MusicalSketches.datarep.NoteFrequencies;
 import com.MusicalSketches.datarep.Song;
 
 public class MusicalLibrary extends Activity {
 	/** Called when the activity is first created. */
 	private ArrayAdapter<String> arrayAdapter;
 	private Library library;
 	private ListView list1;
 	private TextView text;
 
 	public MusicalLibrary() {
 		this.library = new Library();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.userlibrary);
 
 		text = (TextView) findViewById(R.id.textView1);
 		text.setText("User Library");
 		list1 = (ListView) findViewById(R.id.listView1);
 
 		regenerateStoredLibrary();
 		if (library.getSongs().size() == 0) {
 			text.setText("User Library (empty)");
 		}
 		updateView();
 		registerForContextMenu(list1);
 		list1.setTextFilterEnabled(true);
 
 		list1.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				// When clicked, show a toast with the TextView text
 				Intent next = new Intent(MusicalLibrary.this,
 						EditModeLegacy.class);
 				next.putExtra("song object",
 						library.getSong("" + ((TextView) view).getText()));
 				Song s = library.getSong("" + ((TextView) view).getText());
 				Log.d("",
 						"Opening song: " + s.getTitle() + ", length: "
 								+ s.size());
 				startActivityForResult(next, 0);
 			}
 		});
 		Button newComp = (Button) findViewById(R.id.textView2);
 		newComp.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Intent next = new Intent(MusicalLibrary.this, SongSelect.class);
 				startActivityForResult(next, 0);
 			}
 		});
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == 0) {
 			if (data == null) {
 				return;
 			}
 			Song s = (Song) data.getSerializableExtra("song object");
 			library.remove(s.getTitle());
 			library.addSong(s);
 			Log.d("", "updating song, length: " + s.size());
 			Log.d("", "should have updated song");
 			updateView();
 		} else if (resultCode == 1) {
 			// delete
 			if (data == null) {
 				return;
 			}
 			Song s = (Song) data.getSerializableExtra("song object");
 			library.remove(s.getTitle());
 			updateView();
 		}
 	}
 
 	public void setupFakeSongs() {
 		library = new Library();
 		Song s1 = new Song();
 		Log.d("", "new song make");
 		s1.setTitle("My Favorite Song");
 		Log.d("", "new song title");
 		s1.addNote(new Note(NoteFrequencies.getFrequency("e4"), 0.125, "e4"));
 		Log.d("", "new note");
 		s1.addNote(new Note(NoteFrequencies.getFrequency("e5"), 0.25, "e5"));
 		s1.addNote(new Note(NoteFrequencies.getFrequency("a4"), 0.5, "a4"));
 		s1.addNote(new Note(NoteFrequencies.getFrequency("b4"), 0.125, "b4"));
 		Log.d("", "all notes");
 		library.addSong(s1);
 
 		for (Song s : library.getSongs()) {
 			songs.add(s.getTitle());
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(Menu.NONE, DELETE, Menu.NONE, "Delete All");
 		menu.add(Menu.NONE, SORT, Menu.NONE, "Sort");
 		menu.add(Menu.NONE, HELP, Menu.NONE, "Help?");
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case DELETE:
 			createAreYouSure();
 			break;
 		case SORT:
 			Collections.sort(songs);
 			Log.d("", "songs: " + songs.size());
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
 
 	public void createAreYouSure() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Are you sure??")
 				.setCancelable(true)
 				.setPositiveButton("Yes!",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								library = new Library();
 								updateView();
 							}
 						})
 				.setNegativeButton("No!",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 
 		AlertDialog alert = builder.create();
 		alert.show();
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
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		Log.d("", "resuming musical library");
 		updateView();
 	}
 
 	public void updateView() {
 		if (this.library == null) {
 			this.library = new Library();
 		}
 		songs = new ArrayList<String>();
 		for (Song i : this.library.getSongs()) {
 			songs.add(i.getTitle());
 		}
 		arrayAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, songs);
 		list1.setAdapter(arrayAdapter);
 		if (library.getSongs().size() == 0) {
 			text.setText("User Library (empty)");
 		} else {
 			text.setText("User Library");
 		}
 		Collections.sort(songs);
 		Log.d("", "songs: " + songs.size());
 		arrayAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, songs);
 		list1.setAdapter(arrayAdapter);
 		persistStorage();
 	}
 
 	public void persistStorage() {
 		String FILENAME = "library_file";
 		try {
 			FileOutputStream fos;
 			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
 			ObjectOutputStream out = new ObjectOutputStream(fos);
 			out.writeObject(this.library);
 			out.close();
 			fos.close();
 		} catch (Exception e) {
 			Log.wtf("", "persistent storage error");
 		}
 	}
 
 	public void regenerateStoredLibrary() {
 		try {
 			FileInputStream fileIn = openFileInput("library_file");
 			ObjectInputStream in = new ObjectInputStream(fileIn);
 			library = (Library) in.readObject();
 			in.close();
 			fileIn.close();
 		} catch (Exception i) {
 			// i.printStackTrace();
 			Log.wtf("", "error reading file");
 		}
 		if (library == null) {
 			library = new Library();
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		persistStorage();
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 	                                ContextMenuInfo menuInfo) {
 	    super.onCreateContextMenu(menu, v, menuInfo);
 	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
 	    menu.setHeaderTitle(songs.get(info.position));
 	    Song s = this.library.getSong(songs.get(info.position));
 	    if (s.getClef()==0) {
 	    	menu.add(Menu.NONE, 0, Menu.NONE, "Clef: Bass");
 	    } else {
 	    	menu.add(Menu.NONE, 0, Menu.NONE, "Clef: Treble");
 	    }
 		menu.add(Menu.NONE, 1, Menu.NONE, "Meter: " + s.getMeterTop() + "/" + s.getMeterBottom());
 		menu.add(Menu.NONE, 2, Menu.NONE, "Tempo: " + s.getTempo());
 		menu.add(Menu.NONE, 2, Menu.NONE, "Length: " + s.size() + " (notes)");
 	}
 }
