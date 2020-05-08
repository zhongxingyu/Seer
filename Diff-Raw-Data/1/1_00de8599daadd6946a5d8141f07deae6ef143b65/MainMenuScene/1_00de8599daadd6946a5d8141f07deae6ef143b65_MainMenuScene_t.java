 package com.testgame.scene;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
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
 
 public class MainMenuScene extends BaseScene implements IOnMenuItemClickListener {
 
 	private static MenuScene menuChildScene;
 	private final int MENU_LOGIN = 0;
 	private final int MENU_PLAY = 1;
 	private final int MENU_CONTINUE = 2;
 	private static IMenuItem playMenuItem;
 	private static List<String> userslist = new ArrayList<String>();
 	private AlertDialog dialog;
 	private static AlertDialog loading;
 	private static AlertDialog invitation;
 	private static AlertDialog textDialog;
 	private static AlertDialog acceptDialog;
 	private static Map<String, String> usernames;
 	private static boolean loggedin = false;
 	
 
 	
 	@Override
 	public void createScene() {
 		createBackground();
 		createMenuChildScene();
 		usernames = new HashMap<String, String>();
 
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		loggedin = false;
 		Session.getActiveSession().closeAndClearTokenInformation();
 		//System.exit(0);
 
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		return SceneType.SCENE_MENU;
 	}
 
 	@Override
 	public void disposeScene() {
 		// TODO Auto-generated method stub
 
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
 	    
 	    final IMenuItem loginMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_LOGIN, resourcesManager.login_region, vbom), 1.2f, 1);
 	    playMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_PLAY, resourcesManager.newgame_region, vbom), 1.2f, 1);
 	    final IMenuItem conintueMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_CONTINUE, resourcesManager.options_region, vbom), 1.2f, 1);
 	    
 	    menuChildScene.addMenuItem(loginMenuItem);
 	    menuChildScene.addMenuItem(playMenuItem);
 	    menuChildScene.addMenuItem(conintueMenuItem);
 	    
 	    menuChildScene.buildAnimations();
 	    menuChildScene.setBackgroundEnabled(false);
 	    
 	    loginMenuItem.setPosition(0, 175); 
 	    playMenuItem.setPosition(0, 75);
 	    conintueMenuItem.setPosition(0, -25);
 	    //optionsMenuItem.disabled(true);
 	   
 	    
 	    menuChildScene.setOnMenuItemClickListener(this);
 	    
 	    setChildScene(menuChildScene);
 	}
 	
 	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY)
 	{
 	        switch(pMenuItem.getID())
 	        {
 	        case MENU_LOGIN:
 	        	activity.runOnUiThread(new Runnable() {
 	        	    @Override
 	        	    public void run() {
 	        	    	 createLoad();
 	          			 
 	        	    }
 	        	});
 	        	usernames.clear();
 	        	userslist.clear();
 	        	//SceneManager.getInstance().loadUnitSelectionScene(engine);
 	        	ParseFacebookUtils.logIn(activity, new LogInCallback() {
 	        		  @Override
 	        		  public void done(ParseUser user, ParseException err) {
 	        			 
 	        		    if (user == null) {
 	        		      Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
 	        		      loading.dismiss();
 	        		      Log.d("MyApp", err.getMessage());
 	        		    
 	        		    } else if (user.isNew()) {
 	        		      Log.d("MyApp", "User signed up and logged in through Facebook!");
 	        		      resourcesManager.userString = "user_"+ParseUser.getCurrentUser().getObjectId();
 	        		      Log.d("Push", resourcesManager.userString);
 	        		      Log.d("Installation", ParseInstallation.getCurrentInstallation().getInstallationId());
 	        		      PushService.subscribe(activity, resourcesManager.userString, MainActivity.class);
 	        		      getFacebookIdInBackground();
 	        		      
 	        		     
 	        		    } else {
 	        		      Log.d("MyApp", "User logged in through Facebook!");
 	        		      resourcesManager.userString = "user_"+ParseUser.getCurrentUser().getObjectId();
 	        		      Log.d("Push", resourcesManager.userString);
 	        		      Log.d("Installation", ParseInstallation.getCurrentInstallation().getInstallationId());
 	        		      PushService.subscribe(activity, resourcesManager.userString, MainActivity.class);
 	        		      getFacebookIdInBackground();
 	        		     
 	        		    }
 	        		  }
 	        		});
 	        	
 	        	
 	        	
 	            return true;
 	        case MENU_PLAY:
 	        	if(!loggedin){
 	        		activity.runOnUiThread(new Runnable() {
 		        	    @Override
 		        	    public void run() {
 		        	        createDialog("Please Log In!");
 		        	    }
 		        	});
	        		return true;
 	        	}
 	        		
 	        	activity.runOnUiThread(new Runnable() {
 	        	    @Override
 	        	    public void run() {
 	        	        showDialog();
 	        	    }
 	        	});
 	        	 
 	        	
 	            return true;
 	        
 	        case MENU_CONTINUE:
 	        	SceneManager.getInstance().previousScene = this.getSceneType();
 	        	SceneManager.getInstance().loadTutorialScene(engine);
 	        	//SceneManager.getInstance().loadSetupScene(engine);
 	        	return true;
 	        default:
 	            return false;
 	    }
 	}
 	
 	private static void getFacebookIdInBackground() {
 		
 		  Request.executeMeRequestAsync(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
 		    @Override
 		    public void onCompleted(GraphUser user, Response response) {
 		      if (user != null) {
 		    	 Log.d("Facebook", "Completed id lookup");
 		    	ParseUser.getCurrentUser().put("Name", user.getName());
 		        ParseUser.getCurrentUser().put("fbId", user.getId());
 		        ParseUser.getCurrentUser().saveInBackground();
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
 				            	  loggedin = true;
 				            	  loading.dismiss();
 				                  Log.d("friends", friendUsers.toString());
 				              } else {
 				                  Log.d("score", "Error: " + e.getMessage());
 				              }
 				          }
 				      });
 				    }
 				  }
 				});
 		     
 		    }
 		  });
 		  
 		  
 		 
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
 					JSONObject data = new JSONObject("{\"alert\": \"Invitation to Game\", \"action\": \"com.testgame.INVITE\", \"name\": \""+ParseUser.getCurrentUser().getString("Name")+"\", \"userid\": \""+ParseUser.getCurrentUser().getObjectId()+"\"}");
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
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
             	try {
 					JSONObject data = new JSONObject("{\"alert\": \"Invitation Accepted\", \"action\": \"com.testgame.ACCEPT\", \"turn\": \""+h+"\", \"name\": \""+ParseUser.getCurrentUser().getString("Name")+"\"}");
 					 ParsePush push = new ParsePush();
 		             push.setChannel("user_"+object.getString("userid"));
 		             push.setData(data);
 		             push.sendInBackground();
                 } catch (JSONException e) {
 					e.printStackTrace();
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
 	
 	public void createAcceptDialog(JSONObject object){
 		final AlertDialog.Builder dia = new AlertDialog.Builder(activity);
 		try {
 			dia.setTitle(object.getString("name")+ " accepted the invitation!");
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		dia.setNeutralButton("Start Game", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
            	 acceptDialog.dismiss();
            	SceneManager.getInstance().loadSetupScene(engine);
             }
         });
 		
 		acceptDialog = dia.create();
 		acceptDialog.setCanceledOnTouchOutside(false);
 		acceptDialog.show();
 	}
 
 	@Override
 	public void onHomeKeyPressed() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 	
 }
