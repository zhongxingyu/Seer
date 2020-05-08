 package com.github.groupENIGMA.journalEgocentrique;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 
 import com.github.groupENIGMA.journalEgocentrique.model.DB;
 import com.github.groupENIGMA.journalEgocentrique.model.Entry;
 import com.github.groupENIGMA.journalEgocentrique.model.Note;
 
 public class ShareActivity extends Activity {
 
 	private Entry entry;
 	private DB db;
 	private Note note;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_share);
 		db = new DB(getApplicationContext());
 		db.open();
 		
 		Intent received = getIntent();
 		entry = db.getEntry(received.getLongExtra("EntryId", 0));
 		note = null;
 		
 		displayNotes();
 		createCustomPhoto();
 	}
 	
 	// Create the union of mood and the photo
 	private void createCustomPhoto(){
 		// Per ora mette la foto del giorno
 		ImageView img = (ImageView)findViewById(R.id.photoComposite);
 		img.setImageURI(Uri.parse(entry.getPhoto().getPath()));
 	 	Display display = getWindowManager().getDefaultDisplay();
     	int width = display.getWidth();
     	int height = display.getHeight();
     	RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)img.getLayoutParams();
     	params.height = height/2;
     	params.width = width/2;
     	img.setLayoutParams(params);
 	}
 	
 	// Creates the list of the notes. Only the selected will be sended.
 	private void displayNotes(){
 	 	Display display = getWindowManager().getDefaultDisplay();
     	int width = display.getWidth();
     	int height = display.getHeight();
 		List<Note> notes = entry.getNotes();
         ArrayAdapter<Note> arrayAdapter = new ArrayAdapter<Note>(
                 this, R.layout.row, R.id.textViewList, notes
         );
         ListView list = (ListView)findViewById(R.id.notes);
         list.setAdapter(arrayAdapter);
         RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)list.getLayoutParams();
         params.width = width /2;
         list.setLayoutParams(params);
 
         // Add the onLongClickListener that permits to choose the
         // note that will be sent
         OnItemClickListener clickListener = new OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> adapter, View view,
                 int position, long id) {
                 // Set the correct note that will be sent
                 note = (Note) adapter.getItemAtPosition(position);
             }
         };
         list.setOnItemClickListener(clickListener);
     }
 
 	// Start the intent for sharing the composite photo and the selected note.
 	// This method is invocated when the user press on SHARE! button
 	public void share(View view){
 		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("*/*");
 		if(note != null)
 			share.putExtra(Intent.EXTRA_TEXT, note.getText());
 		share.putExtra(Intent.EXTRA_STREAM, Uri.parse(entry.getPhoto().getPath()));// per ora ho messo la photo poi vediamo di cambiare con la custom photo
 		startActivity(Intent.createChooser(share, "Share to..."));
 	}
 }
