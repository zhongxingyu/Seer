 package org.racenet.racesow;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.racenet.framework.Audio;
 import org.racenet.framework.FileIO;
 import org.racenet.framework.GLGame;
 import org.racenet.framework.Screen;
 import org.racenet.framework.XMLParser;
 import org.racenet.helpers.IsServiceRunning;
 import org.racenet.racesow.GameScreen.GameState;
 import org.racenet.racesow.models.Database;
 import org.racenet.racesow.threads.HttpLoaderTask;
 import org.racenet.racesow.threads.SubmitScoresTask;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Process;
 import android.text.InputType;
 import android.text.method.PasswordTransformationMethod;
 import android.widget.EditText;
 
 /**
  * Main Activity of the game
  * 
  * @author so#zolex
  *
  */
 public class Racesow extends GLGame implements HttpCallback {
 	
 	public static boolean LOOPER_PREPARED = false;
 	public static boolean IN_GAME = false;
 	ProgressDialog pd;
 	SubmitScoresTask task = null;
 	String name;
 	
 	/**
 	 * Create the activity
 	 */
 	public void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 
 		//deleteDatabase("org.racenet.racesow.db");
 		
 		Database.setupInstance(this.getApplicationContext());
 		FileIO.getInstance().createDirectory("racesow" + File.separator + "downloads");
 		
 		// when no nickname ist set, ask the user to do so
 		SharedPreferences prefs = getSharedPreferences("racesow", Context.MODE_PRIVATE);
 		if (prefs.getString("name", "").equals("")) {
 			
 			new AlertDialog.Builder(this)
 		        .setMessage("Please set a nickname for the highscores")
 		        .setPositiveButton("OK", new OnClickListener() {
 					
 					public void onClick(DialogInterface arg0, int arg1) {
 						
 						Intent i = new Intent((Activity)Racesow.this, Settings.class);
 						i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 						i.putExtra("setNick", true);
 					    ((Activity)Racesow.this).startActivity(i);
 					}
 				})
 	           .show();
 			
 		} else {
 			
 			if (!IsServiceRunning.check("org.racenet.racesow.PullService", getApplicationContext())) {
         		
         		startService(new Intent(getApplicationContext(), PullService.class));
         	}
 		}
 		
 		// if there are new local scores, try to submit them
 		task = new SubmitScoresTask(new Handler() {
 			
 			@Override
 	        public void handleMessage(Message msg) {
 				
 				switch (msg.what) {
 				
 					case 0:
						showLogin("There are local scores which have not been yubmitted yet.\nPlease enter the password for '"+ task.currentPlayer.name +"'");
 						break;
 						
 					case 1:
 						showEarnedPoints();
 						break;
 				}
 			}
 		});
 		
 		task.execute();
 	}
 	
 	/**
 	 * Called by HttpLoaderTask
 	 * 
 	 * @param InputStream xmlStream
 	 */
 	public void httpCallback(InputStream xmlStream) {
 		
 		pd.dismiss();
 		
 		// not online, just use the nick for now without checking it
 		if (xmlStream == null) {
 			
 			return;
 		}
 		
 		XMLParser parser = new XMLParser();
 		parser.read(xmlStream);
 		
 		// error response
 		NodeList errors = parser.doc.getElementsByTagName("error");
 		if (errors.getLength() == 1) {
 		
 			String message = parser.getNodeValue(errors.item(0));
 			showError(message, null);
 			return;
 		}
 		
 		// login response
 		NodeList auths = parser.doc.getElementsByTagName("auth");
 		if (auths.getLength() == 1) {
 			
 			try {
 
 				Element auth = (Element)auths.item(0);
 				int result = Integer.parseInt(parser.getValue(auth, "result"));
 				if (result == 1) {
 					
 					String session = parser.getValue(auth, "session");
 					task.submitCurrent(session);
 					
 				} else {
 					
 					showLogin("Wrong password. Please try again.");
 				}
 				
 			} catch (NumberFormatException e) {}
 			return;
 		}
 	}
 	
 	/**
 	 * Show how many points a player earned
 	 */
 	public void showEarnedPoints() {
 		
 		AlertDialog login = new AlertDialog.Builder(Racesow.this)
 		.setCancelable(true)
         .setMessage(task.currentPlayer.name + " earned " + task.currentPlayer.points +" point" + (task.currentPlayer.points == 1 ? "" : "s") + ".")
         .setPositiveButton("OK", new OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 
 				task.prepareNext();
 			}
 		})
 		.create();
 	
 		login.setOnCancelListener(new OnCancelListener() {
 			
 			public void onCancel(DialogInterface dialog) {
 				
 				task.prepareNext();
 			}
 		});
 		
 		login.show();
 	}
 	
 	/**
 	 * Show the login dialog. Will fetch a new session
 	 * and call the submitCurrent() or prepareNext()
 	 * methods of SubmitScoresTask depending on the
 	 * user's coice.
 	 * 
 	 * @param String message
 	 */
 	public void showLogin(String message) {
 		
 		final EditText pass = new EditText(Racesow.this);
 		pass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
 		pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
 		AlertDialog login = new AlertDialog.Builder(Racesow.this)
 			.setCancelable(true)
 			.setView(pass)
 	        .setMessage(message)
 	        .setPositiveButton("Login", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 
 					String password = pass.getText().toString();
 					
 					String url = "http://racesow2d.warsow-race.net/accounts.php";
 					List<NameValuePair> values = new ArrayList<NameValuePair>();
 					values.add(new BasicNameValuePair("action", "auth"));
 					values.add(new BasicNameValuePair("name", task.currentPlayer.name));
 					values.add(new BasicNameValuePair("pass", password));
 					final HttpLoaderTask task2 = new HttpLoaderTask(Racesow.this);
 					task2.execute(url, values);
 					
 					pd = new ProgressDialog(Racesow.this);
 					pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 					pd.setMessage("Loggin in...");
 					pd.setCancelable(true);
 					pd.setOnCancelListener(new OnCancelListener() {
 						
 						public void onCancel(DialogInterface dialog) {
 
 							task2.cancel(true);
 							task.prepareNext();
 						}
 					});
 					pd.show();
 				}
 			})
 			.setNegativeButton("Skip", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					
 					task.prepareNext();
 				}
 			})
 			.setNeutralButton("Dismiss scores", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					
 					int length = task.currentPlayer.races.size();
 					Database db = Database.getInstance();
 					for (int i = 0; i < length; i++) {
 					
 						long id = task.currentPlayer.races.get(i).id;
 						db.flagRaceSubmitted(id);
 					}
 					
 					task.prepareNext();
 				}
 			})
 			.create();
 		
 		login.setOnCancelListener(new OnCancelListener() {
 			
 			public void onCancel(DialogInterface dialog) {
 				
 				task.prepareNext();
 			}
 		});
 		
 		login.show();
 	}
 	
 	/**
 	 * Show an error message
 	 * 
 	 * @param String message
 	 */
 	private void showError(String message, final String returnTo) {
 		
 		new AlertDialog.Builder(Racesow.this)
 			.setCancelable(false)
 	        .setMessage("Error: " + message)
 	        .setNeutralButton("OK", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					
 				}
 			})
 	        .show();
 	}
 	
 	/**
 	 * Initially show the main menu
 	 */
     public Screen getStartScreen() {
     	
     	String screen = getIntent().getStringExtra("screen");
     	if (screen == null) {
     	
     		return new MenuScreen(this);
     		
     	} else if (screen.equals("demo")) {
     		
     		return new LoadingScreen(this, null, getIntent().getStringExtra("demo"));
     	}
     	
     	return null;
     }
     
     /**
      * Handle the back-button in different situations
      */
     public void onBackPressed() {
     	
     	Screen screen = this.getCurrentScreen();
     	String screenName = screen.getClass().getName();
     	
     	// if we are "inGame"
     	if (screenName.endsWith("GameScreen")) {
     		
     		GameScreen gameScreen = (GameScreen)screen;
     		
     		if (gameScreen.demoParser != null) {
     			
     			this.finish();
     	    	this.overridePendingTransition(0, 0);
     		}
     		
     		// restart the race
     		else if (gameScreen.map.inRace() || gameScreen.map.raceFinished()) {
     		
     			if (!gameScreen.map.inFinishSequence) {
     				
     				gameScreen.state = GameState.Running;
     				gameScreen.map.restartRace(gameScreen.player);
     			}
     			
     		// return to maps menu
     		} else {
     			
     			this.glView.queueEvent(new Runnable() {
 
                     public void run() {
                        
                     	Racesow.this.setScreen(new MapsScreen(Racesow.this));
                     }
                 });
     		}
     	
     	// return to main menu
     	} else if (screenName.endsWith("MapsScreen")) {
     		
 			this.glView.queueEvent(new Runnable() {
 
                 public void run() {
                    
                 	Racesow.this.setScreen(Racesow.this.getStartScreen());
                 }
             });
     	
 		// quit the application
     	} else if (screenName.endsWith("LoadingScreen")) {
     		
     		// if no demo is loading we come from the mapsScreen
     		if (((LoadingScreen)screen).demoFile == null) {
     			
 				this.glView.queueEvent(new Runnable() {
 	
 	                public void run() {
 	                   
 	                	Racesow.this.setScreen(new MapsScreen(Racesow.this));
 	                }
 	            });
 			
 			// if a demoFile is loading, quit the activity
 			// as it was started additionally to the main instance.
 			// will return to the previous activity = DemoList
     		} else {
 					
     			this.finish();
             	this.overridePendingTransition(0, 0);
     		}
     	
 		// quit the application
     	} else {
     		
     		this.finish();
         	this.overridePendingTransition(0, 0);
         	
         	Audio.getInstance().stopThread();
         	
         	// If I decide to not kill the process anymore, don't
         	// forget to restart the SoundThread and set this flag
         	// LOOPER_PREPARED = false;
         	Process.killProcess(Process.myPid());
     	}
     }
 }
