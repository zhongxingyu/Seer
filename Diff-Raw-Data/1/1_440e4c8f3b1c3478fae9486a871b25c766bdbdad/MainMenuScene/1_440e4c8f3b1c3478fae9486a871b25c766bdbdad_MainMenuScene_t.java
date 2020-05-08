 package com.testgame.scene;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.UUID;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
 import org.andengine.entity.sprite.ButtonSprite;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.util.GLState;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.util.Log;
 
 import com.example.testgame.MainActivity;
 import com.facebook.Request;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.model.GraphUser;
 import com.parse.FindCallback;
 import com.parse.ParseException;
 import com.parse.LogInCallback;
 import com.parse.ParseFacebookUtils;
 import com.parse.ParseInstallation;
 import com.parse.ParseObject;
 import com.parse.ParsePush;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.PushService;
 import com.testgame.scene.SceneManager.SceneType;
 import com.testgame.sprite.GameDialogBox;
 
 public class MainMenuScene extends BaseScene implements IOnMenuItemClickListener {
 
 	private static MenuScene menuChildScene;
 	private final int MENU_LOGIN = 0;
 	private final int MENU_PLAY = 1;
 	private final int MENU_HOWTOPLAY = 2;
 	private final int MENU_LOGOUT = 3;
 	private final int MENU_QUIT = 4;
 	private static IMenuItem loginMenuItem;
 	private static IMenuItem playMenuItem;
 	private static IMenuItem quitMenuItem;
 	private static String name;
 	private static IMenuItem logoutMenuItem;
 	private static List<String> userslist = new ArrayList<String>();
 	private AlertDialog dialog;
 	private AlertDialog quitDialog;
 	private static AlertDialog loading;
 	private static AlertDialog invitation;
 	private static AlertDialog gameOptionsDialog;
 	private static AlertDialog textDialog;
 	private static AlertDialog acceptDialog;
 	private static AlertDialog mapDialog;
 	private static Map<String, String> usernames;
 	private static String selectedMapName = "Default"; 
 	
 	
 	@Override
 	public void createScene() {
 		createBackground();
 		createMenuChildScene();
 		usernames = new HashMap<String, String>();
 		resourcesManager.menu_background_music.play();
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		//PushService.unsubscribe(activity, resourcesManager.userString);
 		//loggedin = false;
 		resourcesManager.menu_background_music.pause();
 		//Session.getActiveSession().closeAndClearTokenInformation();
 		//System.exit(0);
 
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		return SceneType.SCENE_MENU;
 	}
 
 	@Override
 	public void disposeScene() {
 		// Nothing to do here since we never want to unload game resources unless quitting the game.
 	}
 	
 	private void createBackground()
 	{
 	    attachChild(new Sprite(240, 400, resourcesManager.menu_background_region, vbom)
 	    {
 	        @Override
 	        protected void preDraw(GLState pGLState, Camera pCamera) 
 	        {
 	            super.preDraw(pGLState, pCamera);
 	            pGLState.enableDither();
 	        }
 	    });
 	}
 
 	private void createMenuChildScene()
 	{
 	    menuChildScene = new MenuScene(camera);
 	    menuChildScene.setPosition(240, 400);
 	    
 	    logoutMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_LOGOUT, resourcesManager.logout_region, vbom), 1.2f, 1);
 	    loginMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_LOGIN, resourcesManager.login_region, vbom), 1.2f, 1);
 	    playMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_PLAY, resourcesManager.newgame_region, vbom), 1.2f, 1);
 	    quitMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_QUIT, resourcesManager.quit_region, vbom), 1.2f, 1);
 	    final IMenuItem conintueMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_HOWTOPLAY, resourcesManager.howtoplay_region, vbom), 1.2f, 1);
 	    
 	    menuChildScene.addMenuItem(loginMenuItem);
 	    menuChildScene.addMenuItem(playMenuItem);
 	    menuChildScene.addMenuItem(logoutMenuItem);
 	    menuChildScene.addMenuItem(conintueMenuItem);
 	    menuChildScene.addMenuItem(quitMenuItem);
 	    
 	    menuChildScene.buildAnimations();
 	    menuChildScene.setBackgroundEnabled(false);
 	    
 	    loginMenuItem.setPosition(0, 175); 
 	    playMenuItem.setPosition(0, 75);
 	    conintueMenuItem.setPosition(0, -25);
 	    quitMenuItem.setPosition(0, -125);
 	    logoutMenuItem.setPosition(0, -325); // place log out all the way at the bottom.
 	    logoutMenuItem.setVisible(false);
 	    //optionsMenuItem.disabled(true);
 	    
 	    menuChildScene.setOnMenuItemClickListener(this);
 	    setChildScene(menuChildScene, false, false, false);
 	}
 	
 	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY)
 	{
 			resourcesManager.select_sound.play();
 		
 	        switch(pMenuItem.getID())
 	        {
 	        
 	        case MENU_QUIT:
 	        	activity.runOnUiThread(new Runnable() {
 	        	    @Override
 	        	    public void run() {
 	        	    	 createQuit();
 	          			 
 	        	    }
 	        	});
	        	return true;
 	        
 	        case MENU_LOGIN:
 	        	activity.runOnUiThread(new Runnable() {
 	        	    @Override
 	        	    public void run() {
 	        	    	 createLoad();
 	          			 
 	        	    }
 	        	});
 	        	usernames.clear();
 	        	userslist.clear();
 	        	ParseFacebookUtils.logIn(activity, new LogInCallback() {
 	        		  @Override
 	        		  public void done(ParseUser user, ParseException err) {
 	        			 
 	        		    if (user == null) {
 	        		      
 	        		      loading.dismiss();
 	        		      
 	        		    
 	        		    } else if (user.isNew()) {
 	        		      
 	        		      resourcesManager.userString = "user_"+ParseUser.getCurrentUser().getObjectId();
 	        		      resourcesManager.deviceID = ParseInstallation.getCurrentInstallation().getInstallationId();
 	        		     
 	        		      PushService.subscribe(activity, resourcesManager.userString, MainActivity.class);
 	        		      getFacebookIdInBackground();
 	        		      
 	        		     
 	        		    } else {
 	        		     
 	        		      resourcesManager.userString = "user_"+ParseUser.getCurrentUser().getObjectId();
 	        		      
 	        		      resourcesManager.deviceID = ParseInstallation.getCurrentInstallation().getInstallationId();
 	        		     
 	        		      PushService.subscribe(activity, resourcesManager.userString, MainActivity.class);
 	        		      getFacebookIdInBackground();
 	        		     
 	        		    }
 	        		  }
 	        		});
 	        	
 	        	
 	        	
 	            return true;
 	        case MENU_PLAY:
 	        	activity.runOnUiThread(new Runnable() {
 	        	    @Override
 	        	    public void run() {
 	        	    	 gameOptions();
 	        	    }
 	        	});
 	       
 	        	 
 	        	
 	            return true;
 	        
 	        case MENU_HOWTOPLAY:
 	        	SceneManager.getInstance().previousScene = this.getSceneType();
 	        	SceneManager.getInstance().loadTutorialScene(engine);
 	        	//SceneManager.getInstance().loadSetupScene(engine);
 	        	return true;
 	        	
 	        case MENU_LOGOUT:
 	        	if(!logoutMenuItem.isVisible()){
 	        		return true;
 	        	}
 
 	        	PushService.unsubscribe(activity, resourcesManager.userString);
 	    		Session.getActiveSession().closeAndClearTokenInformation();
 	    		logoutMenuItem.setVisible(false);
 	        	return true;
 	        	
 	        	
 	        default:
 	            return false;
 	    }
 	}
 	
 	// NOTE!: I removed static here so that I can popup up a message dialog. Will put it back in if this breaks networking.
 	private void getFacebookIdInBackground() {
 		
 		  Request.executeMeRequestAsync(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
 		    @Override
 		    public void onCompleted(GraphUser user, Response response) {
 		      if (user != null) {
 		    	 
 		    	ParseUser.getCurrentUser().put("Name", user.getName());
 		        ParseUser.getCurrentUser().put("fbId", user.getId());
 		        ParseUser.getCurrentUser().saveInBackground();
 		        name =  user.getName();
 		        welcomeDialog();
 
 		      }
 		      Request.executeMyFriendsRequestAsync(ParseFacebookUtils.getSession(), new Request.GraphUserListCallback() {
 
 				  @Override
 				  public void onCompleted(final List<GraphUser> users, Response response) {
 				    if (users != null) {
 				      List<String> friendsList = new ArrayList<String>();
 				      for (GraphUser user : users) {
 				        friendsList.add(user.getId());
 				      }
 				      //Log.d("Friends", friendsList.toString());
 
 				      // Construct a ParseUser query that will find friends whose
 				      // facebook IDs are contained in the current user's friend list.
 				     
 				      ParseQuery query = ParseUser.getQuery();
 				      query.whereContainedIn("fbId", friendsList);
 				      query.findInBackground(new FindCallback() {
 				          public void done(List<ParseObject> friendUsers, ParseException e) {
 				            
 				        	  if (e == null) {
 				            	  for(ParseObject u : friendUsers){
 				            		  userslist.add(u.getString("Name"));
 				            		  usernames.put(u.getString("Name"), u.getObjectId());
 				            		  
 				            	  }
 				            	  loading.dismiss();
 				                 
 				              } else {
 				                 
 				                  loading.dismiss();
 				              }
 				          }
 				      });
 				    }
 				  }
 				});
 		     
 		    }
 		  });
 		  
 		  
 		 
 		}
 	
 	private void welcomeDialog() {
 		camera.setHUD(new HUD());
 		logoutMenuItem.setVisible(true);
 		new GameDialogBox(camera.getHUD(), "Welcome \n"+name+"!", ((ButtonSprite[]) null));
 	}
 	
 	private void showDialog(){
 		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 		builder.setTitle("Friends");
 		//userslist.add("TestUser");
 		final CharSequence[] de = userslist.toArray(new CharSequence[userslist.size()]);	
 		 builder.setItems(de, new  DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int pos) {
                 resourcesManager.opponent = (String) de[pos];
                 resourcesManager.opponentString = usernames.get((String)de[pos]);
                 try {
 					JSONObject data = new JSONObject("{\"alert\": \"Invitation to Game\", \"action\": \"com.testgame.INVITE\", \"deviceId\": \""+resourcesManager.deviceID+"\", \"name\": \""+ParseUser.getCurrentUser().getString("Name")+"\", \"map\": \""+resourcesManager.mapString+"\", \"userid\": \""+ParseUser.getCurrentUser().getObjectId()+"\"}");
 					 ParsePush push = new ParsePush();
 		             push.setChannel("user_"+resourcesManager.opponentString);
 		             push.setData(data);
 		             push.sendInBackground();
                 } catch (JSONException e) {
 					e.printStackTrace();
 				}
                
                 
                 
 
          }});
 		 
 		 builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
             	 dialog.dismiss();
              }
          });
 		dialog = builder.create();
 		((AlertDialog) dialog).getListView().setFastScrollEnabled(true);
         
 		dialog.setCanceledOnTouchOutside(false);
         
 		dialog.show();
 	}
 	
 	private void createLoad(){
 		final AlertDialog.Builder load = new AlertDialog.Builder(activity);
 		load.setTitle("Please Wait Logging In.");
 		loading = load.create();
 		loading.setCanceledOnTouchOutside(false);
 		loading.show();
 	}
 	
 	public void gameOptions() {
 		final AlertDialog.Builder gameOptions = new AlertDialog.Builder(activity);
 		gameOptions.setTitle("Would you like to play an online or local game?");
 		
 		gameOptions.setNegativeButton("Local", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// launch a local game.	
 				
 				resourcesManager.isLocal = true;
 				gameOptionsDialog.dismiss();
 				activity.runOnUiThread(new Runnable () {
 					@Override
 					public void run() {
 						createMapDialog();
 					}
 				});
 				
 			}
 		});
 
 		gameOptions.setNeutralButton("Online", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// Launch an online game.
 				
 				gameOptionsDialog.dismiss();
 				activity.runOnUiThread(new Runnable () {
 					@Override
 					public void run() {
 						createMapDialog();
 					}
 				});
 			}
 		});
 		
 		gameOptionsDialog = gameOptions.create();
 		gameOptionsDialog.setCanceledOnTouchOutside(false);
 		gameOptionsDialog.show();
 	}
 	
 	public void createInvite(final JSONObject object){
 		final AlertDialog.Builder invite = new AlertDialog.Builder(activity);
 		try {
 			invite.setTitle(object.getString("name")+" wants to play!");
 		} catch (JSONException e) {
 			
 			e.printStackTrace();
 		}
 		invite.setNegativeButton("No", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
             	try {
 					JSONObject data = new JSONObject("{\"alert\": \"Invitation Denied\", \"action\": \"com.testgame.CANCEL\", \"name\": \""+ParseUser.getCurrentUser().getString("Name")+"\"}");
 					 ParsePush push = new ParsePush();
 		             push.setChannel("user_"+object.getString("userid")); 
 		             push.setData(data);
 		             push.sendInBackground();
                 } catch (JSONException e) { 
 					e.printStackTrace();
 				}	
            	 invitation.dismiss();
             }
         });
 		
 		invite.setNeutralButton("Yes", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
             				try {
 								resourcesManager.opponentDeviceID = object.getString("deviceId");
 								Log.d("Map", object.getString("map"));
 								resourcesManager.setMap(object.getString("map"));
 							} catch (JSONException e) {
 								
 								e.printStackTrace();
 							}
             				resourcesManager.isLocal = false;
             				resourcesManager.inGame = true;
         		        	resourcesManager.gameId = UUID.randomUUID().toString();
         		        	Random rand = new Random();
         	            	boolean h;
         	            	if (rand.nextDouble() > 0.5)
         	        			h = true;
         	        		else{
         	        			h = false;
         	        			resourcesManager.turn = true;
         	        		}
         	            	
         	            	try {
         	            		resourcesManager.opponent = object.getString("name");
         						resourcesManager.opponentString = object.getString("userid");
         					} catch (JSONException e1) {
         						
         						e1.printStackTrace();
         					}
         	            	try {
         						JSONObject data = new JSONObject("{\"alert\": \"Invitation Accepted\", \"action\": \"com.testgame.ACCEPT\", \"GameId\": \""+resourcesManager.gameId+"\", \"deviceId\": \""+resourcesManager.deviceID+"\", \"turn\": \""+h+"\", \"name\": \""+ParseUser.getCurrentUser().getString("Name")+"\"}");
         						 ParsePush push = new ParsePush();
         			             push.setChannel("user_"+object.getString("userid"));
         			             push.setData(data);
         			             push.sendInBackground();
         	                } catch (JSONException t) {
         						t.printStackTrace();
         					}	 
         	            	
         	           	 invitation.dismiss();
         	           	 SceneManager.getInstance().loadSetupScene(engine);
 		          
 
             }
         });
 		
 		
 		
 		invitation = invite.create();
 		invitation.setCanceledOnTouchOutside(false);
 		invitation.show();
 	}
 	
 	public void createDialog(String text){
 		
 		final AlertDialog.Builder dia = new AlertDialog.Builder(activity);
 		dia.setTitle(text);
 		dia.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {	
            	 textDialog.dismiss();
             }
         });
 		
 		textDialog = dia.create();
 		textDialog.setCanceledOnTouchOutside(false);
 		textDialog.show();
 		
 		
 	}
 	
 	public void createAcceptDialog(final JSONObject object){
 		final AlertDialog.Builder dia = new AlertDialog.Builder(activity);
 		try {
 			dia.setTitle(object.getString("name")+ " accepted the invitation!");
 		} catch (JSONException e) {
 			
 			e.printStackTrace();
 		}
 		dia.setNeutralButton("Start Game", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) { 
         		try {
 					
         			if(object.getString("turn").equals("true")){
 						resourcesManager.turn = true;
 					}
 					else 
 						resourcesManager.turn = false;
 			
 		
     				resourcesManager.gameId = object.getString("GameId");
     				resourcesManager.opponentDeviceID = object.getString("deviceId");
     				resourcesManager.isLocal = false;
     				resourcesManager.inGame = true;
 		        	acceptDialog.dismiss();
 		           	SceneManager.getInstance().loadSetupScene(engine);
         		} catch (JSONException e) {
 					
 					e.printStackTrace();
 				}
             }
         });
 		
 		acceptDialog = dia.create();
 		acceptDialog.setCanceledOnTouchOutside(false);
 		acceptDialog.show();
 	}
 	
 	public void createQuit(){
 		final AlertDialog.Builder dia = new AlertDialog.Builder(activity);
 		
 			dia.setTitle("Are you sure you wish to quit?");
 		
 		dia.setNeutralButton("Yes", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) { 
         		if(logoutMenuItem.isVisible()){
         			PushService.unsubscribe(activity, resourcesManager.userString);
     	    		Session.getActiveSession().closeAndClearTokenInformation();
         		}
         		System.exit(0);
             }
         });
 		dia.setNegativeButton("No", new DialogInterface.OnClickListener(){
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				quitDialog.dismiss();
 				
 			}
 			
 		});
 		
 		quitDialog = dia.create();
 		quitDialog.setCanceledOnTouchOutside(false);
 		quitDialog.show();
 	}
 	
 	public void createMapDialog(){
 		final AlertDialog.Builder dia = new AlertDialog.Builder(activity);
 		dia.setTitle("Selected Map:");
 		dia.setSingleChoiceItems(resourcesManager.maps(), 0, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) { 
 				selectedMapName = (resourcesManager.maps()[whichButton]).toString();
 				resourcesManager.setMap(selectedMapName);
 				
 				mapDialog.dismiss();
 				
 				if (!resourcesManager.isLocal) {
 					activity.runOnUiThread(new Runnable () {
 						@Override
 						public void run() {
 							showDialog();
 						}
 					});
 				}
 				else
 					SceneManager.getInstance().loadSetupScene(engine);
 			}
 		});
 		
 		mapDialog = dia.create();
 		mapDialog.setCanceledOnTouchOutside(false);
 		mapDialog.show();
 		
 	}
 
 	@Override
 	public void onHomeKeyPressed() {
 		resourcesManager.pause_music();
 	}
 
 }
