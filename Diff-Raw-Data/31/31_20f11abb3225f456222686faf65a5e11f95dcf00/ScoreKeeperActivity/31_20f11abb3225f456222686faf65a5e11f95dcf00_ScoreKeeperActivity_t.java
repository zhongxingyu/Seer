 
 /*
  * ScoreKepper activity.
  * Expanded upon an original by: lisah0 on 2012-02-24
  * 
  * @author mix256
  *
  */
 package com.widepixelgames.scorekeeper;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import com.widepixelgames.scorekeeper.camera.CameraPreview;
 import com.widepixelgames.scorekeeper.connection.WebConnection;
 import com.widepixelgames.scorekeeper.connection.WebConnectionResponseListener;
 import com.widepixelgames.scorekeeper.inpututils.NumberTextWatcher;
 import com.widepixelgames.scorekeeper.logging.RegisterLogger;
 import com.widepixelgames.scorekeeper.properties.GlobalProperties;
 import com.widepixelgames.scorekeeper.qr.QrFormatHandler;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 
 import android.text.InputType;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.FrameLayout;
 
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.PreviewCallback;
 import android.hardware.Camera.AutoFocusCallback;
 import android.hardware.Camera.Size;
 
 import android.widget.TextView;
 
 import net.sourceforge.zbar.ImageScanner;
 import net.sourceforge.zbar.Image;
 import net.sourceforge.zbar.Symbol;
 import net.sourceforge.zbar.SymbolSet;
 import net.sourceforge.zbar.Config;
 
 public class ScoreKeeperActivity extends Activity
 {
	private static final long TIME_TO_SAVE_PASSWORD_MILLIS = 5 * 60 * 1000;
 	public static final int SPLASH_TIME_SECONDS = 3;

 	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;
 
	private ImageScanner scanner;
 
	private boolean previewing = false;
 	private GlobalProperties handler;
 	private QrFormatHandler qrFormatHandlerEntry;
 	private QrFormatHandler qrFormatHandlerGame;
 	private QrFormatHandler qrFormatHandlerPlayer;
 	private ProgressDialog pd;
 	private WakeLock wakeLock;
 	private String helpText;
 	private boolean showingMenu;
 
 	private String resolvedGameText = "";
 	private String resolvedPlayerText = "";
 	private String scanText = "";

 	private String username;
 	private String password;
 	private boolean loggedIn;

 	private boolean flashLight;

	static {
		System.loadLibrary("iconv");
	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
 		autoFocusHandler = new Handler();
 		mCamera = getCameraInstance();
 
 		/* Instance barcode scanner */
 		scanner = new ImageScanner();
 		scanner.setConfig(0, Config.X_DENSITY, 3);
 		scanner.setConfig(0, Config.Y_DENSITY, 3);
 
 		// don't sleep
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "CameraTestActivity");
 		wakeLock.acquire();
 		
 		// splash screen
 		setContentView(R.layout.splash);
 		
 		// initial text at bottom of preview
 		setDefaultScanCodeText();
 		
 		// load help text
 		BufferedReader helpReader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.help_text)));
 		helpText = "";
 		boolean done = false;
 		while(!done){
 			try {
 				String line = helpReader.readLine();
 				if(line == null){
 					done = true;
 				} else {
 					helpText += line + "\n";
 				}
 
 			} catch (IOException e) {
 			}
 		}
 	
 		// read config(s)
 		handler = GlobalProperties.getInstance();
 		handler.load(getResources().openRawResource(R.raw.connection));
 
 		// add handlers for Qr scan formats
 		String qrFormatEntry = handler.getString("${qr_scan_format_entry}", "not_found:qr_scan_format_entry");
 		qrFormatHandlerEntry = new QrFormatHandler(qrFormatEntry);
 
 		String qrFormatGame = handler.getString("${qr_scan_format_game}", "not_found:qr_scan_format_entry");
 		qrFormatHandlerGame = new QrFormatHandler(qrFormatGame);
 
 		String qrFormatPlayer = handler.getString("${qr_scan_format_player}", "not_found:qr_scan_format_entry");
 		qrFormatHandlerPlayer = new QrFormatHandler(qrFormatPlayer);
 
 		// set up the progress-wheel
 		pd = new ProgressDialog(this);
 		pd.setMessage(getString(R.string.please_wait));
 		
 		// Start timer and launch main activity
 		IntentLauncher launcher = new IntentLauncher();
 		launcher.start();
 	}
 	
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 		wakeLock.release();
 	}
 
 	// Menu button only used for showing help text
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 	    if (keyCode == KeyEvent.KEYCODE_MENU) {
 	    	if(showingMenu){
 	    		preview();
 	    		showingMenu = false;
 	    	} else {
 		    	setContentView(R.layout.menu);
 		    	((EditText)findViewById(R.id.help_text)).setText(helpText);
 				((Button)findViewById(R.id.help_ok_button)).setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						preview();
 			    		showingMenu = false;
 					}
 				});
 
 		    	showingMenu = true;
 	    	}
 	        return true;
 	    } else {
 	        return super.onKeyUp(keyCode, event);
 	    }
 	}
 	
 	private void setDefaultScanCodeText(){
 		scanText = getResources().getString(R.string.scan_or_press_here);
 	}
 
 	// TOOD: impl proper states now that the bare bones are up and running
 	@Override
 	public void onBackPressed() {
 	
 		if(!previewing && loggedIn){
 			resolvedGameText = "";
 			resolvedPlayerText = "";
 			setDefaultScanCodeText();
 			preview();
 		} else if(loggedIn){
 
 			showYesNoAlert(getString(R.string.really_quit), new YesNoListener() {
 				@Override
 				public void handle(boolean yesPressed) {
 					if(yesPressed){
 						showYesNoAlert(getString(R.string.clear_saved_password), new YesNoListener() {
 							@Override
 							public void handle(boolean yesPressed) {
 								if(yesPressed){
 									password = "";
 								}
 								savePrefs(password);
 								
 								finish();
 							}
 						});
 					}
 				}
 			});
 		} else {
 			if(showingMenu){
 				showingMenu = false;
 				login();
 			}
 			finish();
 		}
 	}
 	
 	// progress wheel on/off
 	public void showProgress(){
 		
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				pd.show();		
 			}
 		});		
 	}
 
 	public void hideProgress(){
 		
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				pd.hide();
 			}
 		});		
 	}
 	
 	// setup and show the login screen
 	public void login() {
 		setContentView(R.layout.login);
 		
 		SharedPreferences settings = getSharedPreferences("usernfo", 0);
 		long time = settings.getLong("time", 0);
 		username = settings.getString("usr", "");
 		
 		System.out.println("username read " + username);
 		
 		if(System.currentTimeMillis() - time < TIME_TO_SAVE_PASSWORD_MILLIS){
 			password = settings.getString("pw", "");
 			System.out.println("password read ******");
 		} else {
 			System.out.println("no pw read since it has timed out");
 		}
 
 		((EditText)findViewById(R.id.editText1)).setText(username);
 		((EditText)findViewById(R.id.editText2)).setText(password);
 		
 		((Button)findViewById(R.id.login_button)).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 			
 				username = ((EditText)findViewById(R.id.editText1)).getText().toString();
 				password = ((EditText)findViewById(R.id.editText2)).getText().toString();
 
 				if(username.length() == 0 || password.length() == 0){
 					showAlert(getString(R.string.auth_failed));
 					return;
 				}
 				
 				showProgress();
 				
 				WebConnection wc = new WebConnection();
 				wc.login(username, password, new WebConnectionResponseListener() {
 					
 					@Override
 					public void done(String result, String originalMessage) {
 						
 						hideProgress();
 
 						System.out.println("WebConnectionResponseListener " + result);
 						
 						if(!"statusCode=0".equals(getStatusCodeString(result))){
 							if("statusCode=1".equals(getStatusCodeString(result))){
 								showAlert("Authentication Failed");
 							} else {
 								showAlert("Connection Error");
 							}
 						} else {
 							
 							loggedIn = true;
 							savePrefs(password);
 
 							System.out.println("username written " + username);
 							System.out.println("password written ******");
 							
 							runOnUiThread(new Runnable() {
 								@Override
 								public void run() {
 									preview(false, "");
 								}
 							});
 						}
 					}
 				});				
 			}
 		});
 		
 		String state = RegisterLogger.getStorageState();
 		if(!Environment.MEDIA_MOUNTED.equals(state)){
 			showAlert(getString(R.string.error_writable_media) + " " + state + " ." + getString(R.string.app_will_close), new YesNoListener() {
 				@Override
 				public void handle(boolean yesPressed) {
 					finish();
 				}
 			});
 		}
 	}
 	
 	public void savePrefs(String newPassword){
 		
 		SharedPreferences settings = getSharedPreferences("usernfo", MODE_MULTI_PROCESS | MODE_PRIVATE);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("usr", username);
 		editor.putString("pw", password);
 		editor.putLong("time", System.currentTimeMillis());			
 		editor.commit();		
 	}
 	
 	public void showAlert(final String message){
 
 		showAlert(message, new YesNoListener() {
 			@Override
 			public void handle(boolean yesPressed) {
 			}
 		});
 	}
 
 	public void showAlert(final String message, final YesNoListener yesNoListener){
 
 		final Activity a = this;
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				new AlertDialog.Builder(a)
 			    .setTitle("")
 			    .setMessage(message)
 			    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
 			        public void onClick(DialogInterface dialog, int which) { 
 			        	yesNoListener.handle(true);
 			        }
 			     })
 			     .show();		
 			}
 		});
 	}	
 	public void showYesNoAlert(final String message, final YesNoListener yesNoListener){
 
 		final Activity a = this;
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				new AlertDialog.Builder(a)
 			    .setTitle("")
 			    .setMessage(message)
 			    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
 			        public void onClick(DialogInterface dialog, int which) {
 			        	yesNoListener.handle(true);
 			        }
 			     })
 			    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
 			        public void onClick(DialogInterface dialog, int which) { 
 			        	yesNoListener.handle(false);
 			        }
 			     })
 			     .show();		
 			}
 		});
 	}
 	
 	// setup and show the camera preview screen
 	public void preview(){
 		if(loggedIn){
 			preview(false, "");
 		} else {
 			login();
 		}
 	}
 	
 	// setup and show the camera preview screen	
 	public void preview(boolean shouldShowAlert, String alertMessage) {
 		
 		setContentView(R.layout.preview);
 		
 		mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
 		FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
 		preview.addView(mPreview);
 		previewing = true;
 		TextView scanTextField = ((TextView)findViewById(R.id.scanText));
 		scanTextField.setText(scanText);
 		scanTextField.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showRegistrationPage(true, getResources().getString(R.string.press_to_enter), getResources().getString(R.string.press_to_enter), false);
 			}
 		});
 
 		((CheckBox)findViewById(R.id.flashlight_button)).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if(flashLight){
 					flashLightOff();
 				} else {
 					flashLightOn();
 				}
 			}
 		});
 
 		((CheckBox)findViewById(R.id.autofocus_button)).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				autoFocusOn = !autoFocusOn;
 			}
 		});
 		
 		if(shouldShowAlert){
 			showAlert(alertMessage);
 		}
 	}
     
 	@Override
 	protected void onResume(){
 	    super.onResume();
 	    try{
 	    	if(mCamera == null){
 				mCamera = getCameraInstance();
 				resolvedGameText = "";
 				resolvedPlayerText = "";
 				setDefaultScanCodeText();
 	    		preview(false, "");
 	    	}
 	    } catch (Exception e){
 	    }
 	}
 	
 	@Override
     public void onPause() {
         super.onPause();
         releaseCamera();
     }
 
     /** A safe way to get an instance of the Camera object. */
     public static Camera getCameraInstance(){
         Camera c = null;
         try {
             c = Camera.open();
         } catch (Exception e){
         	e.printStackTrace();
         }
         return c;
     }
 
     private void releaseCamera() {
         if (mCamera != null && previewing) {
             previewing = false;
 			flashLightOff();
             mCamera.setPreviewCallback(null);
             mPreview.getHolder().removeCallback(mPreview);
             mCamera.release();
             mCamera = null;
         }
     }
 	private boolean autoFocusOn = true;
 
 	private Runnable doAutoFocus = new Runnable() {
 
 		public void run() {
 			if (previewing){
 				if(autoFocusOn){
 					mCamera.autoFocus(autoFocusCB);
 				}
 			}
 		}
 	};
 
 	// preview found QR and scanned it
 	PreviewCallback previewCb = new PreviewCallback() {
 		public void onPreviewFrame(byte[] data, Camera camera) {
 			Camera.Parameters parameters = camera.getParameters();
 			Size size = parameters.getPreviewSize();
 
 			Image barcode = new Image(size.width, size.height, "Y800");
 			barcode.setData(data);
 
 			int result = scanner.scanImage(barcode);
 
 			if (result != 0 && scanner.getResults().size() == 1) {
 
 				previewing = false;
 				flashLightOff();
 				mCamera.setPreviewCallback(null);
 				mCamera.stopPreview();
 
 				SymbolSet syms = scanner.getResults();
 
 				for (Symbol sym : syms) {
 					
 					setContentView(R.layout.splash);
 
 					String symData = sym.getData();
 					boolean isEntry = qrFormatHandlerEntry.isOfFormat(symData);
 					if(isEntry){
 						EntryResolver.resolve(qrFormatHandlerEntry, symData);
 						entryQrScanned();
 						return;
 					}
 					boolean isGame = qrFormatHandlerGame.isOfFormat(symData);
 					if(isGame){
 						EntryResolver.resolve(qrFormatHandlerGame, symData);
 						boolean showAlert = gameQrScanned();
 						if(showAlert){
 							showAlert("Game info: " + resolvedGameText, new YesNoListener() {
 								@Override
 								public void handle(boolean yesPressed) {
 									preview(false, "");
 								}
 							});
 						}
 						return;
 					}
 					boolean isPlayer = qrFormatHandlerPlayer.isOfFormat(symData);
 					if(isPlayer){
 						EntryResolver.resolve(qrFormatHandlerPlayer, symData);
 						boolean showAlert = playerQrScanned();
 						if(showAlert){
 							showAlert("Player info: " + resolvedPlayerText, new YesNoListener() {
 								@Override
 								public void handle(boolean yesPressed) {
 									preview(false, "");
 								}
 							});
 						}
 						return;
 					}
 					showAlert("Format of scanned code is not known!");
 					return;
 				}
 			}
 		}
 	};
 	
 	// game scanned
 	private boolean gameQrScanned() {
 
 		String gameInfoRawText = GlobalProperties.getInstance().getString("${registerscore_game_text_format}", "not_found:registerscore_game_text_format");
 		String gameInfoResolvedText = GlobalProperties.getInstance().resolve(gameInfoRawText);
 		resolvedGameText = gameInfoResolvedText;
 		scanText = resolvedGameText;
 		if(!"".equals(resolvedPlayerText)){
 			gameAndPlayerQrsScanned();
 			return false;
 		}
 		return true;
 	}
 	
 	// player scanned
 	private boolean playerQrScanned() {
 
 		String playerInfoRawText = GlobalProperties.getInstance().getString("${registerscore_player_text_format}", "not_found:registerscore_player_text_format");
 		String playerInfoResolvedText = GlobalProperties.getInstance().resolve(playerInfoRawText);
 		resolvedPlayerText = playerInfoResolvedText;
 		scanText = resolvedPlayerText;
 		if(!"".equals(resolvedGameText)){
 			gameAndPlayerQrsScanned();
 			return false;
 		}
 		return true;
 	}
 
 	// both game and player are scanned and we're ready to enter the score
 	private void gameAndPlayerQrsScanned(){
 		showRegistrationPage(false, resolvedPlayerText, resolvedGameText, false);
 	}
 
 	// entry reg scanned
 	private void entryQrScanned() {
 		String playerInfoRawText = GlobalProperties.getInstance().getString("${registerscore_player_text_format}", "not_found:registerscore_player_text_format");
 		String gameInfoRawText = GlobalProperties.getInstance().getString("${registerscore_game_text_format}", "not_found:registerscore_game_text_format");
 		
 		String playerInfoResolvedText = GlobalProperties.getInstance().resolve(playerInfoRawText);
 		String gameInfoResolvedText = GlobalProperties.getInstance().resolve(gameInfoRawText);
 		
 		showRegistrationPage(false, playerInfoResolvedText, gameInfoResolvedText, true);
 	}
 
 	// show registration screen
 	private void showRegistrationPage(boolean shouldTextFieldsBeClickable, String playerInfoResolvedText, String gameInfoResolvedText, final boolean completeEntry) {
 
 		previewing = false;
 
 		setContentView(R.layout.register);
 		
 		if(playerInfoResolvedText.indexOf("not_found") >= 0 ||
 				gameInfoResolvedText.indexOf("not_found") >= 0){
 			((Button)findViewById(R.id.register_button)).setEnabled(false);
 			((Button)findViewById(R.id.voidbutton)).setEnabled(false);
 			playerInfoResolvedText = getString(R.string.error_in_scanned_data);
 			gameInfoResolvedText = "";
 		} else {
 			((Button)findViewById(R.id.register_button)).setEnabled(true);
 			((Button)findViewById(R.id.voidbutton)).setEnabled(true);
 		}
 
 		final Context activity = this;
 		final TextView player = ((TextView)findViewById(R.id.player));
 		player.setText(playerInfoResolvedText);
 
 		final TextView game = ((TextView)findViewById(R.id.game));
 		game.setText(gameInfoResolvedText);
 		
 		if(shouldTextFieldsBeClickable){
 			player.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					final EditText input = new EditText(activity);
 					input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
 					new AlertDialog.Builder(activity)
 				    .setTitle("")
 				    .setMessage("Ender Player Id:")
 				    .setView(input)
 				    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				        public void onClick(DialogInterface dialog, int whichButton) {
 				        	String playerIdE = input.getText().toString();
 				        	player.setText(playerIdE); 
 				        	String pidKey = GlobalProperties.getInstance().getString("${manual_player_key}", "not_found:manual_player_key");
 				        	GlobalProperties.getInstance().put(pidKey, playerIdE);
 				        }
 				    }).show();			
 				}
 			});
 			game.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					final EditText input = new EditText(activity);
 					input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
 					new AlertDialog.Builder(activity)
 				    .setTitle("")
 				    .setMessage("Enter Game Id:")
 				    .setView(input)
 				    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				        public void onClick(DialogInterface dialog, int whichButton) {
 				        	String gameIdE = input.getText().toString();
 				        	game.setText(gameIdE);
 				        	String gidKey = GlobalProperties.getInstance().getString("${manual_game_key}", "not_found:manual_game_key");
 				        	GlobalProperties.getInstance().put(gidKey, gameIdE);
 				        }
 				    }).show();			
 				}
 			});
 		}
 
 		EditText editText = (EditText)findViewById(R.id.register_score_edit);
 		editText.addTextChangedListener(new NumberTextWatcher(editText));
 
 		((Button)findViewById(R.id.cancel_button)).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				resolvedGameText = "";
 				resolvedPlayerText = "";
 				setDefaultScanCodeText();
 				preview(false, "");
 			}
 		});
 
 		((Button)findViewById(R.id.voidbutton)).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showYesNoAlert(getString(R.string.really_void), new YesNoListener() {
 					@Override
 					public void handle(boolean yesPressed) {
 						if(yesPressed){
 							postsResultToServer(true, "0", completeEntry);
 						}
 					}
 				});
 			}
 		});
 		
 		((Button)findViewById(R.id.register_button)).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				prepareToPostResultToServer(completeEntry);
 			}
 		});
 	}
 	
 	// validate data entry before posting to server
 	public void prepareToPostResultToServer(final boolean completeEntry){
 
 		final String score = ((EditText)findViewById(R.id.register_score_edit)).getText().toString();
 		if(score.length() == 0 || "0".equals(score)){
 			showAlert(getString(R.string.invalid_score));
 			return;
 		}
 
 		showYesNoAlert(getString(R.string.post_score) + score + "?", new YesNoListener() {
 			@Override
 			public void handle(boolean yesPressed) {
 				if(yesPressed){
 					postsResultToServer(false, score, completeEntry);
 				}
 			}
 		});
 	}
 
 	// post to server
 	public void postsResultToServer(boolean voidEntry, String score, boolean completeEntry){
 
 		savePrefs(password);
 		
 		showProgress();
 		
 		WebConnection wc = new WebConnection();
 		wc.registerWithEntry(username, score, voidEntry, completeEntry, new WebConnectionResponseListener() {
 			
 			@Override
 			public void done(String result, String originalMessage) {
 
 				hideProgress();
 
 				System.out.println("Result = " + result + " for " + originalMessage);
 				RegisterLogger.log(username, "Result = " + result + " for " + originalMessage);					
 				
 				if(!"statusCode=0".equals(getStatusCodeString(result))){
 					if("statusCode=1".equals(getStatusCodeString(result))){
 						showAlert(getString(R.string.auth_failed));
 					} else if("statusCode=2".equals(getStatusCodeString(result))){
 						showAlert(getString(R.string.entry_validation_error));
 					} else if("statusCode=3".equals(getStatusCodeString(result))){
 						showAlert(getString(R.string.entry_is_voided));
 					} else if("statusCode=4".equals(getStatusCodeString(result))){
 						showAlert(getString(R.string.entry_has_score));
 					} else {
 						showAlert(getString(R.string.connection_error));
 					}
 				} else {
 		
 					runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							resolvedGameText = "";
 							resolvedPlayerText = "";
 							setDefaultScanCodeText();
 							preview(true, getString(R.string.result_posted));
 						}
 					});
 				}
 			}
 		});
 		
 	}
 	
 	// Mimic continuous auto-focusing
 	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
 		public void onAutoFocus(boolean success, Camera camera) {
 			autoFocusHandler.postDelayed(doAutoFocus, 1000);
 		}
 	};
 	
 	// delay start of app to show splash
 	private class IntentLauncher extends Thread {
 		@Override
 		/**
 		 * Sleep for some time and than start new activity.
 		 */
 		public void run() {
 			try {
 				Thread.sleep(SPLASH_TIME_SECONDS * 1000);
 			} catch (Exception e) {
 			}
 
 			runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					if(mCamera == null){
 						showAlert(getString(R.string.no_backfacing_camera) + " " + getString(R.string.app_will_close), new YesNoListener() {
 							@Override
 							public void handle(boolean yesPressed) {
 								finish();
 							}
 						});
 					} else {
 						login();
 					}
 				}
 			});
 		}
 	}
 	
 	// parse the statuscode-string from server
 	public String getStatusCodeString(String result){
 
 		if(result == null || result.length() == 0){
 			return "no status code found";
 		}
 		
 		String statusCodeS = "statusCode=";
 		int a = result.indexOf(statusCodeS);
 		if(a >= 0){
 			return result.substring(a, a + statusCodeS.length() + 1);
 		}
 		return "no status code found";
 	}
 	
 	// flashlight!
 	public void flashLightOn() {
 		flashLight = true;
 		try {
 			if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
 				Parameters p = mCamera.getParameters();
 				p.setFlashMode(Parameters.FLASH_MODE_TORCH);
 				mCamera.setParameters(p);
 			} else {
 				System.out.println("NO FEATURE_CAMERA_FLASH");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void flashLightOff() {
 		flashLight = false;
 		try {
 			if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
 				Parameters p = mCamera.getParameters();
 				p.setFlashMode(Parameters.FLASH_MODE_OFF);
 				mCamera.setParameters(p);
 			} else {
 				System.out.println("NO FEATURE_CAMERA_FLASH");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
