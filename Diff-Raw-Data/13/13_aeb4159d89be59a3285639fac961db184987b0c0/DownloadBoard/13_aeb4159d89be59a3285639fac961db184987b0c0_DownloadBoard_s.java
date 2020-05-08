 package fi.mikuz.boarder.gui.internet;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.regex.Pattern;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.thoughtworks.xstream.XStream;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import fi.mikuz.boarder.R;
 import fi.mikuz.boarder.component.internet.InternetFullBoard;
 import fi.mikuz.boarder.connection.ConnectionErrorResponse;
 import fi.mikuz.boarder.connection.ConnectionListener;
 import fi.mikuz.boarder.connection.ConnectionManager;
 import fi.mikuz.boarder.connection.ConnectionSuccessfulResponse;
 import fi.mikuz.boarder.connection.ConnectionUtils;
 import fi.mikuz.boarder.gui.SoundboardMenu;
 import fi.mikuz.boarder.util.TimeoutProgressDialog;
 
 /**
  * 
  * @author Jan Mikael Lindlf
  */
 public class DownloadBoard extends Activity implements ConnectionListener {
 	private static final String TAG = "InternetDownloadBoard";
 	
 	public static final int SHOW_INTERNET_BOARD = 0;
 	public static final int SHOW_PREVIEW_BOARD = 1;
 	public static final String SHOW_KEY = "showKey";
 	private int mAction;
 	
 	final Handler mHandler = new Handler();
 	TimeoutProgressDialog mWaitDialog;
 	private String mResponse = "";
 	
 	InternetFullBoard mBoard;
 	public static final String BOARD_KEY = "boardKey";
 	private int mBoardId;
 	
 	public static final String JSON_KEY = "jsonKey";
 	
 	private boolean mLoggedIn;
 	private String mUserId;
 	private String mSessionToken;
 	
 	Button mFavorite;
 	ImageView mThumbUpImage;
 	ImageView mThumbDownImage;
 	ImageView mScreenshot;
 	Drawable mScreenshotImage;
 
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		
 		mWaitDialog = new TimeoutProgressDialog(DownloadBoard.this, "Waiting for response", TAG, true);
 		
 		Bundle extras = getIntent().getExtras();
 		mAction = extras.getInt(DownloadBoard.SHOW_KEY);
 		
 		if (mAction == SHOW_INTERNET_BOARD) {
 			mBoardId = extras.getInt(InternetMenu.BOARD_ID_KEY);
 			mLoggedIn = extras.getBoolean(DownloadBoardList.LOGGED_IN_KEY);
 			
 			if (mLoggedIn) {
 				mUserId = extras.getString(InternetMenu.USER_ID_KEY);
 				mSessionToken = extras.getString(InternetMenu.SESSION_TOKEN_KEY);
 			}
 			
 			getBoard();
 		} else if (mAction == SHOW_PREVIEW_BOARD) {
 			mBoardId = -1;
 			mLoggedIn = false;
 			
 			XStream xstream = new XStream();
 			JSONObject fakeMessage = (JSONObject) xstream.fromXML(extras.getString(DownloadBoard.JSON_KEY));
 			String fakeMessageString = fakeMessage.toString();
 			
 			try {
 				
 				if (SoundboardMenu.mGlobalSettings.getSensitiveLogging()) Log.v(TAG, "Got a preview: "+fakeMessageString);
 				ConnectionSuccessfulResponse fakeResponse = new ConnectionSuccessfulResponse(new JSONObject(fakeMessageString), InternetMenu.mGetBoardURL);
 				onConnectionSuccessful(fakeResponse);
 			} catch (JSONException e) {
 				Log.e(TAG, "Error reading fake json message", e);
 			}
 		} else {
 			throw new IllegalArgumentException("No proper action defined, action: " + mAction);
 		}
 		
 	}
 
 	private class DownloadScreenshot extends AsyncTask<URL, Integer, Void> {
 		protected Void doInBackground(URL... urls) {
 			try {
 				InputStream is = (InputStream) urls[0].getContent();
 				mScreenshotImage = Drawable.createFromStream(is, "src");
 				
 				runOnUiThread(new Runnable() {
 					public void run() {
 						mScreenshot.setImageDrawable(mScreenshotImage);
 						
 						Animation myFadeInAnimation = AnimationUtils.loadAnimation(
 								DownloadBoard.this.getApplicationContext(), R.anim.fadein);
 						mScreenshot.startAnimation(myFadeInAnimation);
 						
 						mScreenshot.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								
 								LinearLayout fullscreenImageLayout = (LinearLayout) findViewById(R.id.fullscreenImageLayout);
 								LinearLayout normalLayout = (LinearLayout) findViewById(R.id.normalLayout);
 								
 								if (normalLayout.getVisibility() == View.GONE) {
 									normalLayout.setVisibility(View.VISIBLE);
 									fullscreenImageLayout.setVisibility(View.GONE);
 									
 									mScreenshot = (ImageView) findViewById(R.id.screenshot);
 									mScreenshot.setOnClickListener(this);
 								} else {
 									normalLayout.setVisibility(View.GONE);
 									fullscreenImageLayout.setVisibility(View.VISIBLE);
 									
 									mScreenshot = (ImageView) findViewById(R.id.fullImage);
 									mScreenshot.setOnClickListener(this);
 									
 									if (mScreenshotImage.getIntrinsicWidth() > mScreenshotImage.getIntrinsicHeight()) {
 										Bitmap bmpOriginal = ((BitmapDrawable)mScreenshotImage).getBitmap();
 										Matrix matrix = new Matrix();
 								        matrix.postRotate(90);
 										Bitmap bmLandscape = Bitmap.createBitmap(bmpOriginal, 0, 0, bmpOriginal.getWidth(), bmpOriginal.getHeight(), matrix, true);
 										mScreenshot.setImageBitmap(bmLandscape);
 									} else {
 										mScreenshot.setImageDrawable(mScreenshotImage);
 									}
 							        
 									Animation myFadeInAnimation = AnimationUtils.loadAnimation(
 											DownloadBoard.this.getApplicationContext(), R.anim.fadein);
 									mScreenshot.startAnimation(myFadeInAnimation);
 								}
 								
 							}
 						});
 						
 						setProgressBarIndeterminateVisibility(false);
 					}
 				});
 		        return null;
 			} catch (IOException e) {
 				Log.e(TAG, "Error", e);
 				return null;
 			}
 		}
 	}
 
 	
 	final Runnable updateUI = new Runnable() {
 		public void run() {
 			mWaitDialog.dismiss();
 			
 			if (Pattern.matches("rate=.*", mResponse)) {
 				int response = Integer.valueOf(mResponse.substring(mResponse.indexOf("=")+1)).intValue();
 				if (response == 0) {
 					mThumbUpImage.setImageResource(R.drawable.thumb_up_blank);
 					mThumbDownImage.setImageResource(R.drawable.thumb_down_color);
 				} else if (response == 1) {
 					mThumbUpImage.setImageResource(R.drawable.thumb_up_color);
 					mThumbDownImage.setImageResource(R.drawable.thumb_down_blank);
 				}  else if (response == 2) {
 					mThumbUpImage.setImageResource(R.drawable.thumb_up_blank);
 					mThumbDownImage.setImageResource(R.drawable.thumb_down_blank);
 				}
 			} else if (Pattern.matches("Rated succesfully.*", mResponse)) {
 				Toast.makeText(DownloadBoard.this, "Rated succesfully", Toast.LENGTH_SHORT).show();
 				if (mResponse.substring(mResponse.length()-1).equals("1")) {
 					mThumbUpImage.setImageResource(R.drawable.thumb_up_color);
 					mThumbDownImage.setImageResource(R.drawable.thumb_down_blank);
 				} else if (mResponse.substring(mResponse.length()-1).equals("0")) {
 					mThumbUpImage.setImageResource(R.drawable.thumb_up_blank);
 					mThumbDownImage.setImageResource(R.drawable.thumb_down_color);
 				}
 			} else {
 				Toast.makeText(DownloadBoard.this, mResponse, Toast.LENGTH_LONG).show();
 			}
 		}
 	};
 	
 	private void getBoard() {
 		setProgressBarIndeterminateVisibility(true);
 		HashMap<String, String> sendList = new HashMap<String, String>();
 		sendList.put(InternetMenu.BOARD_ID_KEY, Integer.toString(mBoardId));
 		if (mLoggedIn) {
 			sendList.put(InternetMenu.USER_ID_KEY, mUserId);
 			sendList.put(InternetMenu.SESSION_TOKEN_KEY, mSessionToken);
 		}
 		new ConnectionManager(DownloadBoard.this, InternetMenu.mGetBoardURL, sendList);
 	}
 	
 	private void fillBoard(String favoriteText) {
 		setContentView(R.layout.internet_download_board);
 		DownloadBoard.this.setTitle(mBoard.getUploaderUsername() + " - " + mBoard.getBoardName());
 
 		TextView description = (TextView) findViewById(R.id.descriptionText);
 		description.setText(mBoard.getDescription()+"\n\n");
 		
 		final TextView version = (TextView) findViewById(R.id.versionText);
 		final String versionStr = "Version " + mBoard.getBoardVersion()+"\n";
 		
 		if (mBoard.getBoardVersion() > mBoard.getFavoriteBoardVersion() && mBoard.getFavoriteBoardVersion() != -1) {
 			version.setText(versionStr+"Your version is " + mBoard.getFavoriteBoardVersion());
 			final Button versionButton = (Button) findViewById(R.id.versionButton);
 			versionButton.setVisibility(View.VISIBLE);
 			versionButton.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					HashMap<String, String> sendList = new HashMap<String, String>();
 					sendList.put(InternetMenu.USER_ID_KEY, mUserId);
 					sendList.put(InternetMenu.SESSION_TOKEN_KEY, mSessionToken);
 					sendList.put(InternetMenu.BOARD_ID_KEY, Integer.toString(mBoard.getBoardId()));
 					new ConnectionManager(DownloadBoard.this, InternetMenu.mUpdateFavoriteBoardURL, sendList);
 					
 					version.setText(versionStr);
 					versionButton.setVisibility(View.GONE);
 				}
 			});
 		} else {
 			version.setText(versionStr);
 		}
 
 		Button comments = (Button) findViewById(R.id.comments);
 		comments.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent i = new Intent(DownloadBoard.this, DownloadBoardComments.class);
 				XStream xstream = new XStream();
 				i.putExtra(BOARD_KEY, xstream.toXML(mBoard));
 				i.putExtra(DownloadBoardList.LOGGED_IN_KEY, mLoggedIn);
 
 				if (mLoggedIn) {
 					i.putExtra(InternetMenu.USER_ID_KEY, mUserId);
 					i.putExtra(InternetMenu.SESSION_TOKEN_KEY, mSessionToken);
 				}
 
 				startActivity(i);
 			}
 		});
 		
 		mFavorite = (Button) findViewById(R.id.favorite);
 		if (favoriteText != null) {
 			mFavorite.setText(favoriteText);
 		}
 		mFavorite.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (mLoggedIn) {
 					HashMap<String, String> sendList = new HashMap<String, String>();
 					sendList.put(InternetMenu.USER_ID_KEY, mUserId);
 					sendList.put(InternetMenu.SESSION_TOKEN_KEY, mSessionToken);
 					sendList.put(InternetMenu.BOARD_ID_KEY, Integer.toString(mBoard.getBoardId()));
 					new ConnectionManager(DownloadBoard.this, InternetMenu.mFavoriteURL, sendList);
 				} else {
 					Toast.makeText(DownloadBoard.this, "Please login to favorite", Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 
 		LinearLayout buttonLayout = (LinearLayout) findViewById(R.id.buttonLayout);
 		for (final String boardUrl : mBoard.getUrlList()) {
 			if (!boardUrl.equals("")) {
 				Button boardUrlBtn = new Button(DownloadBoard.this);
 				boardUrlBtn.setText(boardUrl);
 				boardUrlBtn.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(boardUrl));
						startActivity(browserIntent);
 					}
 				});
 				buttonLayout.addView(boardUrlBtn);
 			}
 		}
 
 		mScreenshot = (ImageView) findViewById(R.id.screenshot);
 
 		try {
 			URL screenshotUrl = new URL(mBoard.getScreenshot0Url());
 			new DownloadScreenshot().execute(screenshotUrl);
 		} catch (MalformedURLException e) {
 			Log.e(TAG, "Error downloading screenshot " + e.getMessage());
 			setProgressBarIndeterminateVisibility(false);
 		}
 
 		mThumbUpImage = (ImageView) findViewById(R.id.thumbUp);
 		mThumbDownImage = (ImageView) findViewById(R.id.thumbDown);
 
 		if (mLoggedIn) {
 
 			HashMap<String, String> sendList = new HashMap<String, String>();
 			sendList.put(InternetMenu.USER_ID_KEY, mUserId);
 			sendList.put(InternetMenu.SESSION_TOKEN_KEY, mSessionToken);
 			sendList.put(InternetMenu.BOARD_ID_KEY, Integer.toString(mBoard.getBoardId()));
 			new ConnectionManager(DownloadBoard.this, InternetMenu.mGetBoardThumbStatusURL, sendList);
 
 			mThumbUpImage.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					HashMap<String, String> sendList = new HashMap<String, String>();
 					sendList.put(InternetMenu.USER_ID_KEY, mUserId);
 					sendList.put(InternetMenu.SESSION_TOKEN_KEY, mSessionToken);
 					sendList.put(InternetMenu.BOARD_ID_KEY, Integer.toString(mBoard.getBoardId()));
 					sendList.put(InternetMenu.RATE_GOOD_KEY, "1");
 					new ConnectionManager(DownloadBoard.this, InternetMenu.mRateBoardURL, sendList);
 				}
 			});
 
 			mThumbDownImage.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					HashMap<String, String> sendList = new HashMap<String, String>();
 					sendList.put(InternetMenu.USER_ID_KEY, mUserId);
 					sendList.put(InternetMenu.SESSION_TOKEN_KEY, mSessionToken);
 					sendList.put(InternetMenu.BOARD_ID_KEY, Integer.toString(mBoard.getBoardId()));
 					sendList.put(InternetMenu.RATE_GOOD_KEY, "0");
 					new ConnectionManager(DownloadBoard.this, InternetMenu.mRateBoardURL, sendList);
 				}
 			});
 		} else {
 			mWaitDialog.dismiss();
 
 			mThumbUpImage.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					Toast.makeText(DownloadBoard.this, "Please login to vote", Toast.LENGTH_LONG).show();
 				}
 			});
 
 			mThumbDownImage.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					Toast.makeText(DownloadBoard.this, "Please login to vote", Toast.LENGTH_LONG).show();
 				}
 			});
 		}
 	}
 
 	@Override
 	public void onConnectionSuccessful(ConnectionSuccessfulResponse connectionSuccessfulResponse) throws JSONException {
 		ConnectionUtils.connectionSuccessful(DownloadBoard.this, connectionSuccessfulResponse);
 		mWaitDialog.dismiss();
 		
 		if (ConnectionUtils.checkConnectionId(connectionSuccessfulResponse, InternetMenu.mGetBoardURL)) {
 			JSONArray jBoards = connectionSuccessfulResponse.getJSONObject().getJSONArray(ConnectionUtils.returnData);
 			mBoard = new InternetFullBoard(jBoards.getJSONObject(0));
 			String favoriteText = null;
 			if (!connectionSuccessfulResponse.getJSONObject().isNull("favorite")) {
 				favoriteText = connectionSuccessfulResponse.getJSONObject().getString("favorite");
 			}
 			fillBoard(favoriteText);
 		} else if (ConnectionUtils.checkConnectionId(connectionSuccessfulResponse, InternetMenu.mGetBoardThumbStatusURL)) {
 			int rate = connectionSuccessfulResponse.getJSONObject().getInt(ConnectionUtils.returnData);
 			if (rate == 0) {
 				mThumbUpImage.setImageResource(R.drawable.thumb_up_blank);
 				mThumbDownImage.setImageResource(R.drawable.thumb_down_color);
 			} else if (rate == 1) {
 				mThumbUpImage.setImageResource(R.drawable.thumb_up_color);
 				mThumbDownImage.setImageResource(R.drawable.thumb_down_blank);
 			}  else if (rate == 2) {
 				mThumbUpImage.setImageResource(R.drawable.thumb_up_blank);
 				mThumbDownImage.setImageResource(R.drawable.thumb_down_blank);
 			}
 		} else if (ConnectionUtils.checkConnectionId(connectionSuccessfulResponse, InternetMenu.mRateBoardURL)) {
 			int rateGood = connectionSuccessfulResponse.getJSONObject().getInt(ConnectionUtils.returnData);
 			if (rateGood == 1) {
 				mThumbUpImage.setImageResource(R.drawable.thumb_up_color);
 				mThumbDownImage.setImageResource(R.drawable.thumb_down_blank);
 			} else if (rateGood == 0) {
 				mThumbUpImage.setImageResource(R.drawable.thumb_up_blank);
 				mThumbDownImage.setImageResource(R.drawable.thumb_down_color);
 			}
 		} else if (ConnectionUtils.checkConnectionId(connectionSuccessfulResponse, InternetMenu.mFavoriteURL)) {
 			mFavorite.setText(connectionSuccessfulResponse.getJSONObject().getString(ConnectionUtils.returnData));
 		} else {
 			Log.e(TAG, "No id matched with " + connectionSuccessfulResponse.getConnectionId());
 		}
 	}
 
 	@Override
 	public void onConnectionError(ConnectionErrorResponse connectionErrorResponse) {
 		ConnectionUtils.connectionError(this, connectionErrorResponse, TAG);
 	}
 	
 }
