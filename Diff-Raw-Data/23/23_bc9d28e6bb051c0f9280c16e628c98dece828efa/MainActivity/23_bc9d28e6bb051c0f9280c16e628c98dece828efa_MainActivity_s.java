 package labs.brian.brianstetris;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.stmt.PreparedQuery;
 
 import labs.brian.beans.HighScoreInfo;
 import labs.brian.customviews.TetrisGrid;
 import labs.brian.customviews.TetrisGrid.onEndGameListener;
 import labs.brian.database.DatabaseHelper;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements onEndGameListener{
 
 	private static final String PREFS_NAME = "MyPrefsFile";
 	private TetrisGrid board;
 	private TextView score;
 	
 	private Handler handler;
 	private GestureDetector gesture;
 	
 	private boolean gameRunning = true;
 	
 	private TetrisMusic music;
 
 	private String currentName;
 	private EditText nameInput;
 	
 	private DatabaseHelper databaseHelper = null;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.activity_main);
         
         board = (TetrisGrid)findViewById(R.id.tetrisboard);
         score = (TextView)findViewById(R.id.textView1);
         
         board.setOnEndGameListener(this);
         handler = new Handler(){
         	@Override
         	public void handleMessage(Message m){
         		board.performMove();
         		score.setText(String.valueOf(board.getGameScore()));
         	}
         };
         
         //will control the board
         gesture = new GestureDetector(this,new OnGestureListener(){
 
 			@Override
 			public boolean onDown(MotionEvent arg0) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 
 			@Override
 			public boolean onFling(MotionEvent arg0, MotionEvent arg1,
 					float arg2, float velY) {
 				if(velY > 50)
 					board.sendCurrentPieceToBottom();
 				return true;
 			}
 
 			@Override
 			public void onLongPress(MotionEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public boolean onScroll(MotionEvent e1, MotionEvent e2,
 					float distanceX, float distanceY) {
 				if(distanceX > 15){
 					board.translatePiece(-1);
 				}else if(distanceX < -15){
 					board.translatePiece(1);
 				}
 				return true;
 			}
 
 			@Override
 			public void onShowPress(MotionEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			//if tap on left side of screen rotate ccw, right side cw
 			@Override
 			public boolean onSingleTapUp(MotionEvent event) {
 				board.rotatePiece(TetrisGrid.ROTATE_CLOCKWISE);
 				return true;
 			}});
         music = TetrisMusic.getInstance(this);
         
         currentName = getSharedPreferences(PREFS_NAME, 0).getString("currentname", "");
         
         createNameDialog();
        
        startGame();
     }
 
     private void createNameDialog(){
     	 AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setTitle("Name of Player");
          View v = LayoutInflater.from(this).inflate(R.layout.enternamelayout,null,false);
          nameInput = ((EditText)v.findViewById(R.id.edittextname));
          nameInput.setText(currentName);
          builder.setView(v);
          builder.setPositiveButton("Enter", new OnClickListener(){
  			@Override
  			public void onClick(DialogInterface dialog, int which) {
  				currentName = nameInput.getText().toString();
  				dialog.dismiss();
  			}
          });
          builder.setCancelable(false);
          builder.show();
     }
     
     private void startGame(){
     	music.playMusic();
     	Thread t = new Thread(new Runnable(){
 			@Override
 			public void run() {
 				Looper.prepare();
 				while(gameRunning){
 					handler.sendEmptyMessage(0);
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
     	});
     	t.start();
     }
     
     @Override
     public void onPause(){
     	super.onPause();
     	if(music.isPlaying())
     		music.pauseMusic();
     }
     
     @Override
     public void onResume(){
     	super.onResume();
     	if(music.isPaused())
     		music.playMusic();
     }
     
     @Override
     public void onDestroy(){
     	super.onDestroy();
     	music.stopMusic();
     	if (databaseHelper != null) {
 			OpenHelperManager.releaseHelper();
 			databaseHelper = null;
 		}
     }
     
     private DatabaseHelper getHelper() {
 		if (databaseHelper == null) {
 			databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
 		}
 		return databaseHelper;
 	}
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
 	@Override
 	public void onEndGame() {
 		gameRunning = false;
 		music.stopMusic();
 		
 		saveScoreIfHigher();
 		
 		finish();
 	}
 	
 	private void saveScoreIfHigher(){
 		getSharedPreferences(PREFS_NAME, 0).edit().putString("name", currentName).commit();
 		try {
 			List<HighScoreInfo> scoreDBdata = getHelper().getSimpleDataDao().queryForEq("name",currentName);
 			HighScoreInfo hsi = null;
 			if(scoreDBdata.size() > 0){
 				hsi = scoreDBdata.get(0);
 				Integer newScore = Integer.parseInt((String)score.getText());
 				if(hsi.score < newScore){
 					hsi.score = newScore;
 					getHelper().getSimpleDataDao().update(hsi);
 					Toast.makeText(this, "Thats a new high score", Toast.LENGTH_LONG).show();
 				}
 			}else{
 				hsi = new HighScoreInfo(currentName,Integer.parseInt((String)score.getText()));
 				getHelper().getSimpleDataDao().create(hsi);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event){
 		return gesture.onTouchEvent(event);
 	}
 
 }
