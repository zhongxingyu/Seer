 package net.rymate.notes.fragments;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import net.rymate.notes.R;
 
 import net.rymate.notes.activities.NotesListActivity;
 import net.rymate.notes.database.NotesDbAdapter;
 
 /**
  * Created by Ryan on 07/08/13.
  */
 public class NoteEditFragment extends SherlockFragment {
 
     private EditText mTitleText;
     private EditText mBodyText;
     private Long mRowId;
     private NotesDbAdapter mDbHelper;
     private boolean newNote;
 
     public NoteEditFragment(boolean b) {
         this.newNote = b;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mDbHelper = new NotesDbAdapter(this.getActivity());
         mDbHelper.open();
 
        if (!newNote) {
            mRowId = (savedInstanceState == null) ? null :
                    (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
            if (mRowId == null) {
                Bundle extras = getActivity().getIntent().getExtras();
                mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                        : null;
            }
 
            if (mRowId == null) {
                NotesListActivity list = (NotesListActivity) getActivity();
                mRowId = list.getID();
             }
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
         View rootView = inflater.inflate(R.layout.fragment_note_edit, container, false);
 
         mTitleText = (EditText) rootView.findViewById(R.id.title);
         mBodyText = (EditText) rootView.findViewById(R.id.note_body);
 
         populateFields();
 
         return rootView;
     }
 
     private void populateFields() {
         if (mRowId != null) {
             Cursor note = mDbHelper.fetchNote(mRowId);
             getActivity().startManagingCursor(note);
 
             mTitleText.setText(note.getString(
                     note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
             mBodyText.setText(note.getString(
                     note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
 
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         saveState();
         outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         saveState();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         populateFields();
     }
 
     public void saveState() {
         String title = mTitleText.getText().toString();
         String body = mBodyText.getText().toString();
         boolean saved;
 
         if ((title != "") || (body != "")) {
             if (mRowId == null) {
                 long id = mDbHelper.createNote(title, body);
                 if (id > 0) {
                     mRowId = id;
                     saved = true;
                 } else {
                     saved = false;
                 }
             } else {
                 saved = mDbHelper.updateNote(mRowId, title, body);
             }
 
             Context context = getActivity().getApplicationContext();
             int duration = Toast.LENGTH_SHORT;
             int durationFailed = Toast.LENGTH_LONG;
 
             if (saved) {
                 Toast toast = Toast.makeText(context, R.string.note_saved, duration);
                 toast.show();
             } else {
                 Toast toast = Toast.makeText(context, R.string.note_failed, durationFailed);
                 toast.show();
 
             }
         }
 
     }
 }
