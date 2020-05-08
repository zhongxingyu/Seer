 package com.thoughtworks.orteroid.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import com.thoughtworks.orteroid.Callback;
 import com.thoughtworks.orteroid.R;
 import com.thoughtworks.orteroid.constants.Constants;
 import com.thoughtworks.orteroid.models.Board;
 import com.thoughtworks.orteroid.models.Point;
 import com.thoughtworks.orteroid.repositories.BoardRepository;
 import com.thoughtworks.orteroid.utilities.ColorSticky;
 import com.thoughtworks.orteroid.utilities.CustomActionBar;
 import com.thoughtworks.orteroid.utilities.SectionListAdapter;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.List;
 
 public class ViewBoardActivity extends Activity {
     private CustomActionBar customActionBar;
     private Board board;
     private String boardKey;
     private String boardId;
     private RelativeLayout selectedIdea;
     private ImageButton deleteButton;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.view_board);
         customActionBar = new CustomActionBar(this, R.id.spinnerForSections, actionBarCallback());
         Intent intent = getIntent();
         String urlOfBoard = intent.getDataString();
         setParameters(intent, urlOfBoard);
         ProgressDialog dialog = ProgressDialog.show(ViewBoardActivity.this, null, "Fetching details of " + decodeBoardKey() + " board", true);
         dialog.show();
 
         if(board == null){
             BoardRepository.getInstance().retrieveBoard(boardKey, boardId, viewBoardCallback(dialog));
         } else {
             BoardRepository.getInstance().retrievePoints(boardKey, boardId, viewPointsCallback(dialog));
         }
     }
 
     private Callback<Integer> actionBarCallback() {
         return new Callback<Integer>() {
             @Override
             public void execute(Integer selectedSection) {
                 setPoints(board, selectedSection);
             }
         };
     }
 
     public void addIdea(View view) {
         Intent intent = new Intent(this, AddIdeaActivity.class);
         intent.putExtra(Constants.SELECTED_POSITION, customActionBar.selectedIndex().toString());
         intent.putExtra(Constants.BOARD, this.board);
         startActivity(intent);
     }
 
     public void editIdea(View view) {
         Intent intent = new Intent(this, EditIdeaActivity.class);
         Button selectedButton = (Button) view;
         String message = selectedButton.getText().toString();
         Point selectedPoint = board.getPointFromMessage(message, customActionBar.selectedIndex());
         intent.putExtra(Constants.SELECTED_POINT, selectedPoint);
         intent.putExtra(Constants.BOARD, board);
         intent.putExtra(Constants.SELECTED_POSITION, customActionBar.selectedIndex().toString());
         startActivity(intent);
     }
 
     public void deleteIdea(View view){
         Button button = (Button)selectedIdea.findViewById(R.id.row_text);
         String message = button.getText().toString();
         Point selectedPoint = board.getPointFromMessage(message, customActionBar.selectedIndex());
         Callback<Boolean> callback = deleteIdeaCallback();
         BoardRepository.getInstance().deletePoint(selectedPoint, callback);
     }
 
     private Callback<Boolean> deleteIdeaCallback() {
         return new Callback<Boolean>() {
             @Override
             public void execute(Boolean result) {
                 if(result != null) {
                     Intent migrationIntent = new Intent(ViewBoardActivity.this, ViewBoardActivity.class);
                     migrationIntent.putExtra(Constants.BOARD_KEY, board.name().replace(" ", "%20"));
                     migrationIntent.putExtra(Constants.BOARD_ID, board.id().toString());
                     migrationIntent.putExtra(Constants.SELECTED_POSITION, customActionBar.selectedIndex().toString());
                     startActivity(migrationIntent);
                 }
                 connectionIssueNotification();
             }
         };
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 Intent intent = new Intent(this, MainActivity.class);
                 startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onBackPressed() {
         Intent intent = new Intent(this, MainActivity.class);
         startActivity(intent);
         finish();
         super.onBackPressed();
     }
 
     private Callback<Board> viewBoardCallback(final ProgressDialog dialog) {
         final Context context = this;
         return new Callback<Board>() {
             @Override
             public void execute(Board board) {
                 if(board != null) {
                     dialog.dismiss();
                     ViewBoardActivity.this.board = board;
                     customActionBar.setActionBar(board, context);
                 }
                 connectionIssueNotification();
             }
         };
     }
 
     private Callback<List<Point>> viewPointsCallback(final ProgressDialog dialog) {
         final Context context = this;
         return new Callback<List<Point>>() {
             @Override
             public void execute(List<Point> points) {
                 if(points != null) {
                     dialog.dismiss();
                     ViewBoardActivity.this.board.update(points);
                     customActionBar.setActionBar(board, context);
                 }
                 connectionIssueNotification();
             }
         };
     }
 
     private void setParameters(Intent intent, String urlOfBoard) {
         if (urlOfBoard == null) {
             boardKey = intent.getStringExtra(Constants.BOARD_KEY);
             boardId = intent.getStringExtra(Constants.BOARD_ID);
             int selectedIndex;
             if (intent.getStringExtra(Constants.SELECTED_POSITION) != null) {
                 selectedIndex = Integer.parseInt(intent.getStringExtra(Constants.SELECTED_POSITION));
             } else {
                 selectedIndex = 0;
             }
             customActionBar.updateSelectedIndex(selectedIndex);
         } else {
 
             boardId = extractURLFragment(urlOfBoard);
             urlOfBoard = urlOfBoard.substring(0, urlOfBoard.lastIndexOf('/'));
             boardKey = extractURLFragment(urlOfBoard);
         }
     }
 
     private String decodeBoardKey() {
         try {
             return URLDecoder.decode(boardKey, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
     }
 
     private String extractURLFragment(String url) {
         int lastIndex = url.lastIndexOf('/');
         return url.substring(lastIndex + 1, url.length());
     }
 
     private void setPoints(Board board, final int selectedItem) {
         String colourCode = ColorSticky.getColorCode(selectedItem);
         SectionListAdapter sectionListAdapter = new SectionListAdapter(this, board.pointsOfSection(selectedItem), colourCode);
         final ListView listView = (ListView) findViewById(android.R.id.list);
         listView.setAdapter(sectionListAdapter);
         listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
             @Override
             public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long l) {
                 int firstVisiblePosition = listView.getFirstVisiblePosition();
                 int wantedPosition = index - firstVisiblePosition;
                 if ((wantedPosition >= 0) && (wantedPosition <= listView.getChildCount())) {
                     selectedIdea = (RelativeLayout) listView.getChildAt(wantedPosition);
                 } else {
                     selectedIdea = (RelativeLayout) listView.getChildAt(index);
                 }
                 if (deleteButton != null) deleteButton.setVisibility(View.INVISIBLE);
                 deleteButton = (ImageButton) selectedIdea.findViewById(R.id.deleteButton);
                 deleteButton.setVisibility(View.VISIBLE);
                 return true;
             }
         });
     }
 
     private void connectionIssueNotification() {
         AlertDialog.Builder builder =
                 new AlertDialog.Builder(this)
                        .setTitle("Failed to connect to the board")
                         .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 Intent intent = new Intent(ViewBoardActivity.this, MainActivity.class);
                                 startActivity(intent);
                                 dialog.dismiss();
                             }
                         });
         AlertDialog dialog = builder.create();
         dialog.show();
     }
 
 
 }
