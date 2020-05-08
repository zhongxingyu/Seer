 package com.patrykkwiatkowski.simplenotepad;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.ClipboardManager;
 import android.text.Layout;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Main activity.
  * 
  * The user may manage the list of notes and create the new one.
  */
 public class Main extends Activity {
 	private Button createButton;
 	private ListView notesListView;
 	private ArrayList<Note> notes;
 	private Note selectedNote;
 	public static final int noteEditorRequest = 1;
 	public static final int noteCreateReqeust = 2;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		notes = NoteFileAdapter.getNotes();
 		selectedNote = null;
 
 		if (notes == null) {
 			Toast.makeText(this, R.string.err_read, Toast.LENGTH_LONG).show();
 			notes = new ArrayList<Note>();
 		}
 
 		createButton = (Button) findViewById(R.id.mainCreateButton);
 		createButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(Main.this, NoteEditor.class);
 				intent.putExtra("request", noteCreateReqeust);
 				startActivityForResult(intent, noteCreateReqeust);
 			}
 		});
 
 		notesListView = (ListView) findViewById(R.id.mainNotesListView);
 		notesListView.setAdapter(new MainNoteAdapter(this, notes));
 		notesListView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				selectedNote = (Note) notesListView.getItemAtPosition(position);
 
 				if (selectedNote.getCollapsed() == 1) {
 					notes.get(notes.indexOf(selectedNote)).setCollapsed(0);
 				}
 				else {
 					notes.get(notes.indexOf(selectedNote)).setCollapsed(1);
 				}
 
 				((MainNoteAdapter) notesListView.getAdapter()).setItems(notes);
 			}
 		});
 		registerForContextMenu(notesListView);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 		selectedNote = (Note) notesListView.getItemAtPosition(info.position);
 
 		menu.setHeaderTitle(R.string.contextmenu_title);
 		menu.add(R.string.contextmenu_copy).setOnMenuItemClickListener(
 				new OnMenuItemClickListener() {
 					@Override
 					public boolean onMenuItemClick(MenuItem item) {
 						ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 						clipboardManager.setText(selectedNote.getTextContent());
 						return true;
 					}
 				});
 		
 		menu.add(R.string.contextmenu_show).setOnMenuItemClickListener(
 				new OnMenuItemClickListener() {
 					@Override
 					public boolean onMenuItemClick(MenuItem item) {
 						Intent intent = new Intent(Main.this, NoteViewer.class);
 						intent.putExtra("note", selectedNote);
 						startActivity(intent);
 						return true;
 					}
 				});
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		switch (requestCode) {
 			case (noteCreateReqeust): {
 				if (resultCode == Activity.RESULT_OK) {
 					Note note = (Note) data.getParcelableExtra("note");
 					notes.add(0, note);
 					((MainNoteAdapter) notesListView.getAdapter()).setItems(notes);
 				}
 			}
 			break;
 
 			case (noteEditorRequest): {
 				if (resultCode == Activity.RESULT_OK) {
 					Note note = data.getParcelableExtra("note");
 					notes.get(notes.indexOf(note)).setTextContent(note.getTextContent());
 					((MainNoteAdapter) notesListView.getAdapter()).setItems(notes);
 				}
 			}
 			break;
 		}
 	}
 
 	private class MainNoteAdapter extends NoteListViewAdapter {
 		public MainNoteAdapter(Context context, ArrayList<Note> notes) {
 			super(context, notes);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = super.getView(position, convertView, parent);
 
 			if (v != null) {
 				if (position >= 0 && position < notes.size()) {
 					Note note = notes.get(position);
 
 					TextView collapse = (TextView) v.findViewById(R.id.listItemCollapseTextView);
 					TextView content = (TextView) v.findViewById(R.id.listItemContentTextView);
 					Layout l = content.getLayout();
 
 					if ( l != null) {
 						if (note.getCollapsed() == 1) {
 							collapse.setVisibility(View.VISIBLE);
							if (l.getLineCount() > 2) {
 								content.setMaxLines(1);
 							}
 						}
 						else {
 							collapse.setVisibility(View.INVISIBLE);
 							content.setMaxLines(l.getLineCount());
 						}
 					}
 
 					Button edit = (Button) v.findViewById(R.id.listItemEditButton);
 					edit.setTag(note);
 					edit.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							Note note = (Note) v.getTag();
 							Intent intent = new Intent(Main.this, NoteEditor.class);
 							intent.putExtra("request", noteEditorRequest);
 							intent.putExtra("note", note);
 							startActivityForResult(intent, noteEditorRequest);
 						}
 					});
 
 					Button delete = (Button) v.findViewById(R.id.listItemDeleteButton);
 					delete.setTag(note);
 					delete.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							final Note note = (Note) v.getTag();
 
 							new AlertDialog.Builder(Main.this)
 									.setTitle(R.string.dialog_title)
 									.setMessage(R.string.dialog_question)
 									.setNegativeButton(R.string.dialog_no, null)
 									.setPositiveButton(R.string.dialog_yes,
 											new Dialog.OnClickListener() {
 												@Override
 												public void onClick(DialogInterface dialog,
 														int which) {
 
 													if (!NoteFileAdapter.deleteNote(note)) {
 														Toast.makeText(Main.this,
 																R.string.err_delete,
 																Toast.LENGTH_LONG).show();
 														return;
 													}
 													notes.remove(note);
 													notifyDataSetChanged();
 												}
 
 											})
 									.show();
 						}
 					});
 				}
 			}
 			return v;
 		}
 	}
 }
