 package com.thoughtworks.orteroid.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.*;
 import android.widget.*;
 //import com.google.analytics.tracking.android.EasyTracker;
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
     public static final int REQUEST_CODE = 0;
     private CustomActionBar customActionBar;
     private Board board;
     private String boardKey;
     private String boardId;
     private RelativeLayout selectedIdea;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.view_board);
         customActionBar = new CustomActionBar(this, R.id.spinnerForSections, actionBarCallback());
         Intent intent = getIntent();
         String urlOfBoard = intent.getDataString();
         setParameters(intent, urlOfBoard);
 
         if (board == null) {
             ProgressDialog dialog = ProgressDialog.show(ViewBoardActivity.this, null, "Fetching details of " + decodeBoardKey() + " board", true);
             dialog.show();
             BoardRepository.getInstance().retrieveBoard(boardKey, boardId, viewBoardCallback(dialog));
         } else {
             BoardRepository.getInstance().retrievePoints(boardKey, boardId, viewPointsCallback());
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (data != null) {
             int selectedPosition = data.getIntExtra(Constants.SELECTED_POSITION, customActionBar.selectedIndex());
             customActionBar.updateSelectedIndex(selectedPosition);
         }
         refresh(null);
     }
 
     public void refresh(View view) {
         ImageButton refreshButton = (ImageButton) findViewById(R.id.refreshButton);
         refreshButton.setVisibility(View.GONE);
         ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
         progressBar.setVisibility(View.VISIBLE);
         BoardRepository.getInstance().retrievePoints(boardKey, boardId, viewPointsCallback());
     }
 
     @Override
     public void onStart() {
         super.onStart();
     }
 
     @Override
     public void onStop() {
         super.onStop();
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
         startActivityForResult(intent, REQUEST_CODE);
     }
 
     public void editIdea(View view) {
         Intent intent = new Intent(this, EditIdeaActivity.class);
         Point selectedPoint = null;
             Button selectedButton;
             if (selectedIdea == null) selectedButton = (Button) view;
             else selectedButton = (Button) selectedIdea.findViewById(R.id.row_text);
             String message = selectedButton.getText().toString();
             selectedPoint = board.getPointFromMessage(message, customActionBar.selectedIndex());
             intent.putExtra(Constants.SELECTED_POINT, selectedPoint);
             intent.putExtra(Constants.BOARD, board);
             intent.putExtra(Constants.SELECTED_POSITION, customActionBar.selectedIndex().toString());
             startActivityForResult(intent, REQUEST_CODE);
     }
 
 
     public void voteForIdea(View view) {
         Button button = (Button) selectedIdea.findViewById(R.id.row_text);
         View viewForOptionMenu = selectedIdea.findViewById(R.id.menu_options);
         viewForOptionMenu.setVisibility(View.GONE);
         String message = button.getText().toString();
         Point selectedPoint = board.getPointFromMessage(message, customActionBar.selectedIndex());
         Callback<Boolean> callback = voteIdeaCallback(view);
         BoardRepository.getInstance().voteForIdea(selectedPoint, callback);
         generateToastForVote();
     }
 
     private void generateToastForVote() {
         LayoutInflater inflater = getLayoutInflater();
         View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_layout_root));
 
         TextView text = (TextView) layout.findViewById(R.id.text);
         text.setText("Voting...");
         Toast toast = new Toast(getApplicationContext());
         toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
         toast.setDuration(Toast.LENGTH_LONG);
         toast.setView(layout);
         toast.show();
     }
 
     private Callback<Boolean> voteIdeaCallback(final View view) {
         return new Callback<Boolean>() {
             @Override
             public void execute(Boolean object) {
                 refresh(view);
             }
         };
     }
 
     public void deleteIdea(View view) {
         View view1 = selectedIdea.findViewById(R.id.idea_menu);
         view1.setVisibility(View.GONE);
         Button button = (Button) selectedIdea.findViewById(R.id.row_text);
         String message = button.getText().toString();
         Point selectedPoint = board.getPointFromMessage(message, customActionBar.selectedIndex());
         Callback<Boolean> callback = deleteIdeaCallback(view);
         showDeletionToast();
         BoardRepository.getInstance().deletePoint(selectedPoint, callback);
     }
 
     private void showDeletionToast() {
         LayoutInflater inflater = getLayoutInflater();
         View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_layout_root));
         TextView text = (TextView) layout.findViewById(R.id.text);
         text.setText("deleting idea");
         Toast toast = new Toast(getApplicationContext());
         toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
         toast.setDuration(Toast.LENGTH_LONG);
         toast.setView(layout);
         toast.show();
     }
 
     private Callback<Boolean> deleteIdeaCallback(final View view) {
         return new Callback<Boolean>() {
             @Override
             public void execute(Boolean result) {
                 if (result != null) {
                     refresh(view);
                 } else {
                     connectionIssueNotification();
                 }
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
         if (selectedIdea == null) {
             Intent intent = new Intent(this, MainActivity.class);
             startActivity(intent);
            finish();
             super.onBackPressed();
         } else {
             View menuOptionView = selectedIdea.findViewById(R.id.menu_options);
             selectedIdea = null;
             menuOptionView.setVisibility(View.GONE);
         }
     }
 
     private Callback<Board> viewBoardCallback(final ProgressDialog dialog) {
         final Context context = this;
         return new Callback<Board>() {
             @Override
             public void execute(Board board) {
                 if (board != null) {
                     dialog.dismiss();
                     ViewBoardActivity.this.board = board;
                     customActionBar.setActionBar(board, context);
                 } else {
                     connectionIssueNotification();
                 }
             }
         };
     }
 
     private Callback<List<Point>> viewPointsCallback() {
         final Context context = this;
         return new Callback<List<Point>>() {
             @Override
             public void execute(List<Point> points) {
                 if (points != null) {
                     ImageButton refreshButton = (ImageButton) findViewById(R.id.refreshButton);
                     refreshButton.setVisibility(View.VISIBLE);
                     ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
                     progressBar.setVisibility(View.GONE);
                     ViewBoardActivity.this.board.update(points);
                     customActionBar.setActionBar(board, context);
                 } else {
                     connectionIssueNotification();
                 }
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
                 if (selectedIdea != null) {
                     View menuOptionView = selectedIdea.findViewById(R.id.menu_options);
                     menuOptionView.setVisibility(View.GONE);
 
                 }
                 int firstVisiblePosition = listView.getFirstVisiblePosition();
                 int wantedPosition = index - firstVisiblePosition;
                 if ((wantedPosition >= 0) && (wantedPosition <= listView.getChildCount())) {
                     selectedIdea = (RelativeLayout) listView.getChildAt(wantedPosition);
                 } else {
                     selectedIdea = (RelativeLayout) listView.getChildAt(index);
                 }
 
                 View menuOptionView = selectedIdea.findViewById(R.id.menu_options);
                 menuOptionView.setVisibility(View.VISIBLE);
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
