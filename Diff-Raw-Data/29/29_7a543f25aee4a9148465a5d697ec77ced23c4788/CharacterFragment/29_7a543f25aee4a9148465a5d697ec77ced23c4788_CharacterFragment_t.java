 package edu.mines.alterego;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.support.v4.app.Fragment;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 //import android.widget.ArrayAdapter;
 //import android.widget.ListView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 //import java.util.ArrayList;
 
 import edu.mines.alterego.CharacterDBHelper;
 
 public class CharacterFragment extends Fragment {
 
     //int mCharId = -1;
     CharacterData mChar;
     int mGameId = -1;
     View mainView;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         mGameId = getArguments().getInt((String) getResources().getText(R.string.gameid), -1);
         int mCharId = getArguments().getInt((String) getResources().getText(R.string.charid), -1);
 
         // Inflate the layout for this fragment
         View character_view = inflater.inflate(R.layout.character_view, container, false);
         mainView = character_view;
 
         if (mCharId >= 0) {
             CharacterDBHelper dbHelper = new CharacterDBHelper(getActivity());
             mChar = dbHelper.getCharacter(mCharId);
             showCharacter();
         } else {
             // Make the no-char layout visible
             LinearLayout nochar_ll = (LinearLayout) character_view.findViewById(R.id.nochar_layout);
             nochar_ll.setVisibility(0);
 
             Log.i("AlterEgo::CharFrag::Init", "Binding the click listener for create-character button");
             // Bind the new-character button to it's appropriate action
             Button new_char = (Button) character_view.findViewById(R.id.nochar_button);
             new_char.setOnClickListener( new Button.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     // Spawn the create-character dialog
 
                     AlertDialog.Builder charBuilder = new AlertDialog.Builder(v.getContext());
                     LayoutInflater inflater = getActivity().getLayoutInflater();
 
                     charBuilder.setView(inflater.inflate(R.layout.new_char_dialog, null))
                         .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int id) {
                                 AlertDialog thisDialog = (AlertDialog) dialog;
 
                                 EditText nameInput = (EditText) thisDialog.findViewById(R.id.char_name);
                                 EditText descInput = (EditText) thisDialog.findViewById(R.id.char_desc);
 
                                 String name = nameInput.getText().toString();
                                 String desc = descInput.getText().toString();
 
                                 CharacterDBHelper dbHelper = new CharacterDBHelper(getActivity());
                                 CharacterData newChar = dbHelper.addCharacter(mGameId, name, desc);
 
                                 mChar = newChar;
                                 //mCharId = nChar.id;
 
                                 showCharacter();
                             }
                         })
                         .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                             // Negative button just closes the dialog
                             @Override
                             public void onClick(DialogInterface dialog, int id) { dialog.dismiss(); }
                         });
                     charBuilder.create().show();
                 }
             });
         }
 
         return character_view;
     }
 
     public void showCharacter() {
         // Make the no-char layout invisible
         LinearLayout nochar_ll = (LinearLayout) mainView.findViewById(R.id.nochar_layout);
         nochar_ll.setVisibility(View.GONE);
 
         // Make the character-viewing area visible
         LinearLayout char_layout = (LinearLayout) mainView.findViewById(R.id.haschar_layout);
         char_layout.setVisibility(View.VISIBLE);
 
         // Show the character name and description
         TextView cName = (TextView) mainView.findViewById(R.id.char_name);
         TextView cDesc = (TextView) mainView.findViewById(R.id.char_desc);
 
         cName.setText(mChar.name);
         cDesc.setText(mChar.description);
 
         // Show all the character's attributes/skills/complications
     }
 
 }
