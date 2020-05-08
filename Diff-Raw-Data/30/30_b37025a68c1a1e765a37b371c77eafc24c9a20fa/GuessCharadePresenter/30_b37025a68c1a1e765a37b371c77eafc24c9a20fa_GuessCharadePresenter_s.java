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
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.os.AsyncTask;
 import android.os.CountDownTimer;
 import android.os.Environment;
 import android.util.Log;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.VideoView;
 
 import com.example.wecharades.model.DatabaseException;
 import com.example.wecharades.model.Turn;
 import com.example.wecharades.views.GuessCharadeActivity;
 
 public class GuessCharadePresenter extends Presenter {
 
 	private GuessCharadeActivity activity;
 	private DownloadVideo download;
 	private VideoView videoView;
 	private final String SAVE_PATH = Environment.getExternalStorageDirectory().getPath()+"/PresentVideo.mp4";//TODO: Fix ftp storage
 	private Turn turn;
 	public String currentWord;
 	public CountDownTimer timer;
 	private TextView timerView;
 	private static final int NO_DOWNLOAD = 0;
 	public static final int DOWNLOAD_FINISHED = 1;
 	public int downloadState = NO_DOWNLOAD;
 	
 	public GuessCharadePresenter(GuessCharadeActivity activity) {
 		super(activity);
 		this.activity = activity;
 
 	}
 	public void updateModel(){
 		try {
 			dc.updateTurn(turn);
 		} catch (DatabaseException e) {
 			Log.e("GuessCharadePresenter", e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Creates a timer to control the gameTime
 	 * @param timerView
 	 */
 	public void initializeTimer (final TextView timerView){
 		this.timerView = timerView;
 		timer = new CountDownTimer(30000, 1000) {
 
 			public void onTick(long millisUntilFinished) {
 				timerView.setText("Seconds remaining: " + millisUntilFinished / 1000);
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
 	public void downloadVideo(Context context, VideoView videoView, Turn turn){
 		this.videoView = videoView;
 		this.turn = turn;
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
 		int number = r.nextInt(high-low) + low;
 		return number;
 	}
 	/**
 	 * Checks if the users word matches the current word.
 	 * @param answerWord
 	 * @return True if answerWord matches currentWord.
 	 */
 	public boolean checkRightWord(EditText answerWord){
		if(answerWord.getText().toString().equalsIgnoreCase(currentWord) ){
			return true;
		}
		else{
			return false;
		}
 	}
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
 
 		@Override
 		protected void onPreExecute(){
 			downloadState = NO_DOWNLOAD;
 			mDialog = new ProgressDialog(mContext);
 			mDialog.setTitle("Downloading Charade");
 			mDialog.setMessage("Please Wait");
 			mDialog.show();
 		}
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			FTPClient con = null;
 			try{
 				con = new FTPClient();
 				con.connect("ftp.mklcompetencia.se", 21);
 				if (con.login("mklcompetencia.se", "ypkq4w")){
 					con.enterLocalPassiveMode(); // important!
 					System.out.println(turn.getVideoLink());
 					con.setFileType(FTP.BINARY_FILE_TYPE);
 					OutputStream out = new FileOutputStream(new File(SAVE_PATH));
 					boolean result = con.retrieveFile(turn.getVideoLink(), out);// Todo: server path. //"/APP/PresentVideo.mp4"
 					out.close();
 					if (result) {
 						Log.v("download result", "succeeded");
 						//con.deleteFile(turn.getVideoLink());
 					}						
 					con.logout();
 					con.disconnect();
 				}
 			}
 			catch (SocketException e){
 				Log.v("download result", e.getMessage());
 			}
 			catch (UnknownHostException e){
 				Log.v("download result", e.getMessage());
 			}
 			catch (FTPConnectionClosedException e){
 				Log.v("download result", e.getMessage());
 			}
 			catch (CopyStreamException e){
 				Log.v("download result", e.getMessage());
 			}
 			catch (IOException e){
 				Log.v("download result", e.getMessage());
 			}
 			catch (Exception e){
 				Log.v("download result","failed " + e.getMessage());
 			}
 			return null;	
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result){
 			if(mDialog.isShowing()){
 				mDialog.setMessage("Download Success!");
 				mDialog.dismiss();
 				activity.showMessage(shuffleWord().toUpperCase());
 				downloadState = DOWNLOAD_FINISHED;
 				playVideo();
 				timer.start();
 				timerView.setVisibility(0);
 
 			}
 		}
 	}
 }
