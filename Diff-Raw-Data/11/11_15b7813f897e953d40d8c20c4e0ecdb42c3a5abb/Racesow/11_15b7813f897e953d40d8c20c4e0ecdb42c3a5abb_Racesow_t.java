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
 import org.racenet.framework.Sound;
 import org.racenet.framework.XMLParser;
 import org.racenet.helpers.IsServiceRunning;
 import org.racenet.racesow.GameScreen.GameState;
 import org.racenet.racesow.models.Database;
 import org.racenet.racesow.threads.HttpLoaderTask;
 import org.racenet.racesow.threads.MusicThread;
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
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Process;
 import android.os.StrictMode;
 import android.text.InputType;
 import android.text.method.PasswordTransformationMethod;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * Main Activity of the game
  * 
  * @author so#zolex
  *
  */
 public class Racesow extends GLGame implements HttpCallback {
 	
 	public static boolean IN_GAME = false;
 	ProgressDialog pd;
 	SubmitScoresTask task = null;
 	public static MusicThread bg;
 	public static SharedPreferences prefs;
 	String pendingName;
 	String initialName;
 	public static Sound back;
 	public static Sound click;
 	public static Typeface font;
 	public static TextView raceTime, ups, fps, tutorial, centertext1, centertext2, centertext3;
 	
 	/**
 	 * Create the activity
 	 */
 	public void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 
 		//deleteDatabase("org.racenet.racesow.db");
 		
 		if (android.os.Build.VERSION.SDK_INT > 9) {
 			
 			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
 			StrictMode.setThreadPolicy(policy);
 	    }
 		
 		Database.setupInstance(this.getApplicationContext());
 		FileIO.getInstance().createDirectory("racesow" + File.separator + "downloads");
 		
 		font = FileIO.getInstance().getFont("font.ttf");
 		
 		click = Audio.getInstance().newSound("sounds/ok.wav");
 		back = Audio.getInstance().newSound("sounds/back.wav");
 		prefs = getSharedPreferences("racesow", Context.MODE_PRIVATE);
 		
 		startMusic();
 		
 		// when no nickname ist set, ask the user to do so
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
 						initialName = task.currentPlayer.name;
 						showLogin("There are local scores for '"+ task.currentPlayer.name +"' which have not been submitted yet. As the name is registered you may login for it, change the name or cancel the submission.");
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
 	 * Play the click-sound
 	 */
 	public static void clickSound() {
 		
		if (click != null) {
			
			click.play(0.25f);
		}
 	}
 	
 	/**
 	 * Play the back-sound
 	 */
 	public static void backSound() {
 		
		if (back != null) {
		
			back.play(0.6f);
		}
 	}
 	
 	/**
 	 * Start the menu music if it's not yet playing
 	 */
 	public static void startMusic() {
 		
 		if (!prefs.getBoolean("bg", true)) {
 			
 			return;
 		}
 		
 		if (bg == null) {
 			
 			bg = new MusicThread();
 			bg.start();
 			
 		} else {
 			
 			if (!bg.music.isPlaying()) {
 				
 				bg.music.play();
 			}
 		}
 	}
 	
 	/**
 	 * Stop the menu music if it's playing
 	 */
 	public static void stopMusic() {
 		
 		if (bg != null) {
 			
 			bg.stop = true;
 			bg = null;
 		}
 	}
 	
 	/**
 	 * Pause the menu music if it's playing
 	 */
 	public static void pauseMusic() {
 		
 		if (bg != null) {
 			
 			if (bg.music.isPlaying()) {
 				
 				bg.music.pause();
 			}
 		}
 	}
 	
 	/**
 	 * Resume the menu music from pause state
 	 */
 	public static void resumeMusic() {
 		
 		if (bg != null) {
 			
 			bg.music.play();
 		}
 	}
 	
 	public void onPause() {
 		
 		super.onPause();
 		pauseMusic();
 	}
 	
 	public void onResume() {
 		
 		super.onResume();
 		resumeMusic();
 		
 		raceTime = (TextView)findViewById(R.id.time);
 		raceTime.setTypeface(font);
 		raceTime.setTextColor(Color.GREEN);
 		
 		ups = (TextView)findViewById(R.id.ups);
 		ups.setTypeface(font);
 		ups.setTextColor(Color.GREEN);
 		
 		fps = (TextView)findViewById(R.id.fps);
 		fps.setTypeface(font);
 		fps.setTextColor(Color.GREEN);
 		
 		tutorial = (TextView)findViewById(R.id.tutorial);
 		tutorial.setTypeface(font);
 		tutorial.setTextColor(Color.GREEN);
 		
 		centertext1 = (TextView)findViewById(R.id.centertext1);
 		centertext1.setTypeface(font);
 		centertext1.setTextColor(Color.GREEN);
 		
 		centertext2 = (TextView)findViewById(R.id.centertext2);
 		centertext2.setTypeface(font);
 		centertext2.setTextColor(Color.GREEN);
 		
 		centertext3 = (TextView)findViewById(R.id.centertext3);
 		centertext3.setTypeface(font);
 		centertext3.setTextColor(Color.GREEN);
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
 		
 		// name availability check response
 		NodeList checks = parser.doc.getElementsByTagName("checkname");
 		if (checks.getLength() == 1) {
 			
 			try {
 				
 				int available = Integer.parseInt(parser.getValue((Element)checks.item(0), "available"));
 				if (available == 1) {
 					
 					Database.getInstance().updateRacesPlayer(initialName, pendingName);
 					task.currentPlayer.name = pendingName;
 					task.submitCurrent("");
 					
 				} else {
 					
 					task.currentPlayer.name = pendingName;
 					showLogin("The name '"+ task.currentPlayer.name +"' is also registered you may login for it, change the name or cancel the submission.");
 				}
 				
 			} catch (NumberFormatException e) {}
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
 					Database.getInstance().setSession(task.currentPlayer.name, session);
 					task.submitCurrent(session);
 					
 				} else {
 					
 					showLogin("Wrong password for '"+ task.currentPlayer.name +"'. Please try again, chage the name or skip the submission.");
 				}
 				
 			} catch (NumberFormatException e) {}
 			return;
 		}
 		
 		// session response
 		NodeList sessions = parser.doc.getElementsByTagName("checksession");
 		if (sessions.getLength() == 1) {
 			
 			try {
 				
 				Element session = (Element)sessions.item(0);
 				int result = Integer.parseInt(parser.getValue(session, "result"));
 				if (result == 1) {
 					
 					Database db = Database.getInstance();
 					String newSesssion = parser.getValue(session, "session");
 					db.setSession(pendingName, newSesssion);
 					db.updateRacesPlayer(initialName, pendingName);
 					task.currentPlayer.name = pendingName;
 					task.submitCurrent(newSesssion);
 					
 				} else {
 					
 					task.currentPlayer.name = pendingName;
 					showLogin("The sesion for '"+ pendingName +"' has expired.\nPlease enter the Password.");
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
 							showLogin("There are local scores for '"+ task.currentPlayer.name +"' which have not been submitted yet. As the name is registered you may login for it, change the name or cancel the submission.");
 						}
 					});
 					pd.show();
 				}
 			})
 			.setNegativeButton("Cancel", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					
 					askSkip();
 				}
 			})
 			.setNeutralButton("Change name", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					
 					showChangeName();
 				}
 			})
 			.create();
 		
 		login.setOnCancelListener(new OnCancelListener() {
 			
 			public void onCancel(DialogInterface dialog) {
 				
 				askSkip();
 			}
 		});
 		
 		login.show();
 	}
 	
 	/**
 	 * Ask the user to skip the submission or dismiss it
 	 */
 	public void askSkip() {
 		
 		new AlertDialog.Builder(Racesow.this)
 		.setCancelable(false)
         .setMessage("Do you want to skip the submission to execute it later or completely dismiss it?")
         .setPositiveButton("Skip", new OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				
 				task.prepareNext();
 			}
 		})
 		.setNegativeButton("Dismiss", new OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				
 				int length = task.currentPlayer.races.size();
 				Database db = Database.getInstance();
 				for (int i = 0; i < length; i++) {
 				
 					long id = task.currentPlayer.races.get(i).id;
 					db.flagRaceSubmitted(id, null);
 				}
 				
 				task.prepareNext();
 			}
 		})
 		.show();
 	}
 	
 	/**
 	 * Show a dialog to allow the user to change
 	 * the name from a protected one to a new one
 	 */
 	public void showChangeName() {
 		
 		final EditText name = new EditText(Racesow.this);
 		AlertDialog change = new AlertDialog.Builder(Racesow.this)
 			.setCancelable(true)
 			.setView(name)
 	        .setMessage("Change the name for '"+ task.currentPlayer.name + "'")
 	        .setPositiveButton("Change", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					
 					pendingName = name.getText().toString().trim();
 					
 					String session = Database.getInstance().getSession(pendingName);
 					if (!session.equals("")) {
 						
 						String url = "http://racesow2d.warsow-race.net/accounts.php";
 						List<NameValuePair> values = new ArrayList<NameValuePair>();
 						values.add(new BasicNameValuePair("action", "session"));
 						values.add(new BasicNameValuePair("id", session));
 						final HttpLoaderTask task = new HttpLoaderTask(Racesow.this);
 						task.execute(url, values);
 						
 						pd = new ProgressDialog(Racesow.this);
 						pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 						pd.setMessage("Checking session...");
 						pd.setCancelable(true);
 						pd.setOnCancelListener(new OnCancelListener() {
 							
 							public void onCancel(DialogInterface dialog) {
 
 								task.cancel(true);
 								showChangeName();
 							}
 						});
 						pd.show();
 						
 					} else {
 					
 						String url = "http://racesow2d.warsow-race.net/accounts.php";
 						List<NameValuePair> values = new ArrayList<NameValuePair>();
 						values.add(new BasicNameValuePair("action", "check"));
 						values.add(new BasicNameValuePair("name", pendingName));
 						final HttpLoaderTask task = new HttpLoaderTask(Racesow.this);
 						task.execute(url, values);
 						
 						pd = new ProgressDialog(Racesow.this);
 						pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 						pd.setMessage("Checking name...");
 						pd.setCancelable(true);
 						pd.setOnCancelListener(new OnCancelListener() {
 							
 							public void onCancel(DialogInterface dialog) {
 	
 								task.cancel(true);
 								showChangeName();
 							}
 						});
 						pd.show();
 					}
 				}
 			})
 			.setNegativeButton("Cancel", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					
 					showLogin("There are local scores for '"+ task.currentPlayer.name +"' which have not been submitted yet. As the name is registered you may login for it, change the name or cancel the submission.");
 				}
 			})
 			.create();
 		
 		change.setOnCancelListener(new OnCancelListener() {
 			
 			public void onCancel(DialogInterface dialog) {
 			
 				showLogin("There are local scores for '"+ task.currentPlayer.name +"' which have not been submitted yet. As the name is registered you may login for it, change the name or cancel the submission.");
 			}
 		});
 		
 		change.show();
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
     	
     		return new SplashScreen(this);
     		
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
     			
     			backSound();
     			Racesow.IN_GAME = false;
     			startMusic();
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
                        
                     	backSound();
                     	Racesow.IN_GAME = false;
                     	startMusic();
                     	Racesow.this.setScreen(new MapsScreen(Racesow.this));
                     }
                 });
     		}
     	
     	// return to main menu
     	} else if (screenName.endsWith("MapsScreen")) {
     		
 			this.glView.queueEvent(new Runnable() {
 
                 public void run() {
                    
                 	backSound();
                 	Racesow.this.setScreen(new MenuScreen(Racesow.this));
                 }
             });
     	
 		// quit the application
     	} else if (screenName.endsWith("LoadingScreen")) {
     		
     		backSound();
     		
     		// if no demo is loading we come from the mapsScreen
     		if (((LoadingScreen)screen).demoFile == null) {
     			
 				this.glView.queueEvent(new Runnable() {
 	
 	                public void run() {
 	                   
 	                	Racesow.IN_GAME = false;
 	                	startMusic();
 	                	Racesow.this.setScreen(new MapsScreen(Racesow.this));
 	                }
 	            });
 			
 			// if a demoFile is loading, quit the activity
 			// as it was started additionally to the main instance.
 			// will return to the previous activity = DemoList
     		} else {
 				
     			Racesow.IN_GAME = false;
     			startMusic();
     			this.finish();
             	this.overridePendingTransition(0, 0);
     		}
     	
 		// quit the application
     	} else {
     		
     		backSound();
     		this.finish();
         	this.overridePendingTransition(0, 0);
         	
         	Audio.getInstance().stopThread();
         	
         	// If I decide to not kill the process anymore, don't
         	// forget to restart the SoundThread
         	Process.killProcess(Process.myPid());
     	}
     }
 }
