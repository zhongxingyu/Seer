 package com.thoughtworks.orteroid.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.GradientDrawable;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.thoughtworks.orteroid.Callback;
 import com.thoughtworks.orteroid.R;
 import com.thoughtworks.orteroid.constants.Constants;
 import com.thoughtworks.orteroid.models.Board;
 import com.thoughtworks.orteroid.models.Point;
 import com.thoughtworks.orteroid.repositories.BoardRepository;
 import com.thoughtworks.orteroid.utilities.ColorSticky;
 import com.thoughtworks.orteroid.utilities.Font;
 
 public class EditIdeaActivity extends Activity {
 
     private Point point;
     private String message;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.edit_idea);
         Intent intent = getIntent();
         this.point = intent.getParcelableExtra(Constants.SELECTED_POINT);
         setupText();
         setBackgroundLayout();
     }
 
     public void editIdea(View view){
         EditText editText = (EditText) findViewById(R.id.editIdea);
         message = editText.getText().toString();
         sendEditedIdea();
     }
 
     public void deleteCurrentIdea(View view){
         generateConfirmationMessage();
     }
 
     private void generateConfirmationMessage() {
         AlertDialog.Builder builder =
                 new AlertDialog.Builder(this)
                         .setTitle("Are you sure you want to delete this Idea?")
                         .setNegativeButton("No", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 dialog.dismiss();
                             }
                         })
                         .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 dialog.dismiss();
                                 deleteIdea();
                             }
                         });
         AlertDialog dialog = builder.create();
         dialog.show();
     }
 
     private void deleteIdea() {
         Callback<Boolean> callback = deleteIdeaCallback();
         BoardRepository.getInstance().deletePoint(point, callback);
     }
 
     private void sendEditedIdea() {
         Callback<Boolean> callback = editIdeaCallback();
         BoardRepository.getInstance().editIdea(message, point.id(), callback);
     }
 
     private Callback<Boolean> editIdeaCallback() {
         return new Callback<Boolean>() {
             @Override
             public void execute(Boolean result) {
                 if (result != null) {
                     generateSuccessToast();
                 }
                 else generateFailureNotification();
             }
         };
     }
 
     private Callback<Boolean> deleteIdeaCallback() {
         return new Callback<Boolean>() {
             @Override
             public void execute(Boolean response) {
                 if(response != null) {
                     migrateToViewBoardActivity();
                 }
                 connectionIssueNotification();
             }
         };
     }
 
     private void generateFailureNotification() {
         AlertDialog.Builder builder =
                 new AlertDialog.Builder(this)
                         .setTitle("The following idea failed :\n " + message)
                         .setNegativeButton("Try Resending", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 sendEditedIdea();
                                 dialog.dismiss();
                             }
                         })
                         .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 dialog.dismiss();
                             }
                         });
         AlertDialog dialog = builder.create();
         dialog.show();
     }
 
     private void generateSuccessToast() {
         LayoutInflater inflater = getLayoutInflater();
         View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_layout_root));
 
         TextView text = (TextView) layout.findViewById(R.id.text);
         text.setText("Idea posted successfully");
         Toast toast = new Toast(getApplicationContext());
         toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
         toast.setDuration(Toast.LENGTH_LONG);
         toast.setView(layout);
         toast.show();
         migrateToViewBoardActivity();
     }
 
     private void migrateToViewBoardActivity() {
         Intent intent = getIntent();
         Board board= intent.getParcelableExtra(Constants.BOARD);
         Integer selectedIndex = Integer.valueOf(intent.getStringExtra(Constants.SELECTED_POSITION));
         Intent migrationIntent = new Intent(this, ViewBoardActivity.class);
         migrationIntent.putExtra(Constants.BOARD_KEY, board.name().replace(" ", "%20"));
         migrationIntent.putExtra(Constants.BOARD_ID, board.id().toString());
         migrationIntent.putExtra(Constants.SELECTED_POSITION, selectedIndex.toString());
         startActivity(migrationIntent);
     }
 
     private void setupText() {
         EditText editText = (EditText) findViewById(R.id.editIdea);
         editText.setText(point.message());
         editText.setTypeface(Font.setFont(this));
     }
 
     private void setBackgroundLayout() {
         EditText editText = (EditText) findViewById(R.id.editIdea);
         editText.setBackgroundResource(R.drawable.sticky);
         GradientDrawable drawable = (GradientDrawable) editText.getBackground();
         drawable.setColor(Color.parseColor(ColorSticky.getColorCode(point.sectionId())));
         editText.invalidate();
     }
 
     private void connectionIssueNotification() {
         AlertDialog.Builder builder =
                 new AlertDialog.Builder(this)
                        .setTitle("Failed to connect to the internet")
                         .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 Intent intent = new Intent(EditIdeaActivity.this, MainActivity.class);
                                 dialog.dismiss();
                                 startActivity(intent);
                             }
                         });
         AlertDialog dialog = builder.create();
         dialog.show();
     }
 }
