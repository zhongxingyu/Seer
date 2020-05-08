 package com.example.wecharades.presenter;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPConnectionClosedException;
 import org.apache.commons.net.io.CopyStreamException;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.os.AsyncTask;
 import android.os.CountDownTimer;
 import android.os.Environment;
 import android.util.Log;
 import android.widget.EditText;
 import android.widget.VideoView;
 
 import com.example.wecharades.model.DatabaseException;
 import com.example.wecharades.model.Game;
 import com.example.wecharades.model.Turn;
 import com.example.wecharades.views.GuessCharadeActivity;
 
 public class GuessCharadePresenter extends Presenter {
 
 	private GuessCharadeActivity activity;
 	private DownloadVideo download;
 	private VideoView videoView;
 	private final String SAVE_PATH = Environment.getExternalStorageDirectory().getPath()+"/PresentVideo.mp4";//TODO: Fix ftp storage
 //	private File SAVE_PATHTWO = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
 //	private String SAVE_PATHTHREE = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString() + "/PresentVideo.mp4";
 	private Turn turn;
 	public String currentWord;
 	public CountDownTimer timer;
 	public static final int NO_DOWNLOAD = 0;
 	public static final int DOWNLOAD_FINISHED = 1;
 	public int downloadState = NO_DOWNLOAD;
 
 	public GuessCharadePresenter(GuessCharadeActivity activity, Turn turn) {
 		super(activity);
 		this.turn = turn;
 		this.activity = activity;
 	}
 
	public void initialize() {
 		initializeTimer();
 		downloadVideo(activity, videoView);
 	}
 
 	
 
 	public void updateModel(){
 			dc.updateTurn(turn);
 	}
 
 	/**
 	 * Creates a timer to control the gameTime
 	 * @param timerView
 	 */
 
 	 public void initializeTimer (){
 		 timer = new CountDownTimer(30000, 100) {
 
 			 public void onTick(long millisUntilFinished) {
 				 activity.setTime(String.valueOf(millisUntilFinished / 1000));
 //				 if (millisUntilFinished>10000){
 //					 if (millisUntilFinished%1000 == 0)
 //						 activity.setTime(String.valueOf(millisUntilFinished / 1000));
 //				 }
 //				 else{
 //					 activity.setTime((millisUntilFinished / 1000 + "." + (millisUntilFinished%1000)/100));
 //				 }	 
 			 }
 
 			 public void onFinish() {
 				 activity.gameState = GuessCharadeActivity.GAME_FINISHED;
 				 videoView.stopPlayback();
 				 turn.setRecPlayerScore(0);
 				 turn.setAnsPlayerScore(0);
 				 turn.setState(Turn.FINISH);
 				 updateModel();
 				 activity.finishDialog();
 			 }
 		 };
 	 }
 	 /**
 	  * Call this method to initiate video download from server.
 	  * @param context
 	  * @param videoView
 	  */
 	 public void downloadVideo(Context context, VideoView videoView){
 		 this.videoView = videoView;
 		 download = new DownloadVideo(context, SAVE_PATH);
 		 download.execute();
 	 }
 	 /**
 	  * Sets video specifications and initiates the video.
 	  */
 	 public void playVideo() {
 		 try {
 			 videoView.setOnPreparedListener(new OnPreparedListener() {
 
 				 @Override
 				 public void onPrepared(MediaPlayer mp) {
 					 mp.setLooping(true);          
 				 }
 			 });
 			 videoView.setVideoPath(SAVE_PATH);
 			 videoView.start();
 		 } catch (Exception e) {
 			 Log.e("Video", "error: " + e.getMessage(), e);
 		 }
 	 }
 	 /**
 	  * Adds a random number of random letters to a String object.
 	  * @param turn
 	  * @return the shuffled string
 	  */
 	 private String shuffleWord(){
 		 currentWord = turn.getWord(); //TODO: Replace
 		 String alphabet = "abcdefghijklmnopqrstuvwxyz";
 		 alphabet = shuffle(alphabet);
 		 alphabet = alphabet.substring(alphabet.length() - randomNumber(7,4));
 		 String guessWord = currentWord + alphabet;    
 		 guessWord = shuffle(guessWord);
 		 return guessWord;
 	 }
 	 /**
 	  * Shuffles a String
 	  * @param input
 	  * @return The shuffled string
 	  */
 	 private String shuffle(String input){
 		 List<Character> characters = new ArrayList<Character>();
 		 for(char c:input.toCharArray()){
 			 characters.add(c);
 		 }
 		 StringBuilder output = new StringBuilder(input.length());
 		 while(characters.size()!=0){
 			 int randPicker = (int)(Math.random()*characters.size());
 			 output.append(characters.remove(randPicker));
 		 }
 		 return output.toString();
 	 }
 	 /**
 	  * Randoms a number between two limits.
 	  * @param high Upper limit.
 	  * @param low Lower limit.
 	  * @return the random number.
 	  */
 	 private int randomNumber(int high, int low){
 		 Random r = new Random();
 		 return (r.nextInt(high-low) + low);
 	 }
 	 /**
 	  * Checks if the users word matches the current word.
 	  * @param answerWord
 	  * @return True if answerWord matches currentWord.
 	  */
 	 public boolean checkRightWord(EditText answerWord){
 		 return answerWord.getText().toString().equalsIgnoreCase(currentWord);
 	 }
 
 	 public Game getGame(){
 		 return dc.getGame(turn.getGameId());
 	 }//TODO: Oklar metod?
 
 	 /**
 	  *
 	  * @author Adam
 	  *
 	  */
 	 private class DownloadVideo extends AsyncTask<Void, Long, Boolean> {
 
 		 private ProgressDialog mDialog;
 		 Context mContext;
 		 private String SAVE_PATH;
 
 		 public DownloadVideo(Context context,String path) {
 			 mContext=context;
 			 SAVE_PATH = path;
 		 }
 
 		 private void setReadable(File file){
 			 if (file.exists()) {
 				 System.out.println("in SetReadAble");
 				 file.setReadable(true,false);
 			 }
 		 }
 
 		 @Override
 		 protected void onPreExecute(){
 			 downloadState = NO_DOWNLOAD;
 			 mDialog = new ProgressDialog(mContext);
 			 mDialog.setTitle("Downloading Charade");
 			 mDialog.setMessage("Please Wait");
 			 mDialog.setCancelable(false);
 			 mDialog.setCanceledOnTouchOutside(false);
 			 mDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel", new DialogInterface.OnClickListener() {
 
 				 @Override
 				 public void onClick(DialogInterface dialog, int which) {
 					 dialog.dismiss();
 					 AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 					 builder.setTitle("Downloading Charade")
 					 .setMessage("Download canceled!")
 					 .setCancelable(false)
 					 .setPositiveButton("Go back and download later", new DialogInterface.OnClickListener() {
 						 public void onClick(DialogInterface dialog, int id) {
 							 dialog.cancel();
 							 activity.finish();
 
 						 }
 					 })
 					 .setNegativeButton("Download now", new DialogInterface.OnClickListener() {
 						 public void onClick(DialogInterface dialog, int id) {
 							 dialog.cancel();
 							 new DownloadVideo(mContext,SAVE_PATH).execute();
 						 }
 					 });
 					 AlertDialog alert = builder.create();
 					 alert.show();
 
 				 }
 			 });
 			 mDialog.show();
 		 }
 
 		 @Override
 		 protected Boolean doInBackground(Void... params) {
 			 boolean result = false;
 			 FTPClient con = null;
 			 try{
 				 con = new FTPClient();
 				 con.connect("ftp.mklcompetencia.se", 21);
 				 if (con.login("mklcompetencia.se", "ypkq4w")){
 					 con.enterLocalPassiveMode(); // important!
 					 System.out.println(turn.getVideoLink());
 					 con.setFileType(FTP.BINARY_FILE_TYPE);
					 File file = new File(SAVE_PATH);
					 OutputStream out = new FileOutputStream(file);
 					 result = con.retrieveFile(turn.getVideoLink(), out);
 					 out.close();
 					 if (result) {
 						 Log.v("download result", "succeeded");
						 setReadable(file);
 					 }                                              
 					 con.logout();
 					 con.disconnect();
 				 }
 			 }
 			 catch (SocketException e){
 				 Log.v("download result Socket", e.getMessage());
 				 cancel(true);
 			 }
 			 catch (UnknownHostException e){
 				 Log.v("download result Unknown", e.getMessage());
 				 cancel(true);
 			 }
 			 catch (FTPConnectionClosedException e){
 				 Log.v("download result FTP CONNECTIONCLOSED", e.getMessage());
 				 cancel(true);
 			 }
 			 catch (CopyStreamException e){
 				 Log.v("download result COPYSTREAM", e.getMessage());
 				 cancel(true);
 			 }
 			 catch (IOException e){
 				 Log.v("download result IOE", e.getMessage());
 				 cancel(true);
 			 }
 			 catch (Exception e){
 				 Log.v("download result just exception","failed " + e.getMessage());
 				 cancel(true);
 			 }
 			 return null;  
 		 }
 
 		 @Override
 		 protected void onPostExecute(Boolean result){
 			 if(mDialog.isShowing()){
 				 mDialog.setMessage("Download Success!");
 				 mDialog.dismiss();
 				 activity.setPossibleLetters(shuffleWord().toUpperCase());
 				 downloadState = DOWNLOAD_FINISHED;
 				 timer.start();
 				 playVideo();
 			 }
 		 }
 
 		 @Override
 		 protected void onCancelled(Boolean result){
 			 if(mDialog.isShowing()){
 				 mDialog.dismiss();
 				 AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 				 builder.setTitle("Downloading Charade")
 				 .setMessage("Download failed, try again!")
 				 .setCancelable(false)
 				 .setPositiveButton("Retry later", new DialogInterface.OnClickListener() {
 					 public void onClick(DialogInterface dialog, int id) {
 						 dialog.cancel();
 						 //                                              Intent intent = new Intent(activity.getApplicationContext(),GameDashboardActivity.class);
 						 //                                              intent.putExtra(Database.TURN, turn);
 						 //                                              activity.startActivity(intent);
 						 activity.finish();
 
 					 }
 				 })
 				 .setNegativeButton("Retry now", new DialogInterface.OnClickListener() {
 					 public void onClick(DialogInterface dialog, int id) {
 						 dialog.cancel();
 						 new DownloadVideo(mContext,SAVE_PATH).execute();
 
 					 }
 				 });
 				 AlertDialog alert = builder.create();
 				 alert.show();
 			 }
 		 }
 	 }
 }
