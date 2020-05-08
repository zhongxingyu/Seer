 package bode.moritz.footfinger;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 import org.cocos2d.layers.CCColorLayer;
 import org.cocos2d.layers.CCLayer;
 import org.cocos2d.layers.CCScene;
 import org.cocos2d.menus.CCMenu;
 import org.cocos2d.menus.CCMenuItem;
 import org.cocos2d.menus.CCMenuItemImage;
 import org.cocos2d.nodes.CCDirector;
 import org.cocos2d.nodes.CCLabel;
 import org.cocos2d.nodes.CCSprite;
 import org.cocos2d.sound.SoundEngine;
 import org.cocos2d.types.CGPoint;
 import org.cocos2d.types.CGSize;
 import org.cocos2d.types.ccColor3B;
 import org.cocos2d.types.ccColor4B;
 
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.view.inputmethod.InputMethodManager;
 import bode.moritz.footfinger.network.NetworkControllerClient;
 
 public class ShooterConnectLayer extends CCColorLayer {
 
 	private String IP_TO_CONNECT_TO = null;
 	private final int PORT_TO_CONNECT_TO = 8080;
 	private NetworkControllerClient networClient;
 	private CGSize winSize;
	private StringBuilder input = new StringBuilder("Enter IP");
 
 	private OnKeyListener keyListener = new OnKeyListener() {
 
 		boolean firstInput = true;
 
 		@Override
 		public boolean onKey(View v, int keyCode, KeyEvent event) {
 			if (event.getAction() == KeyEvent.ACTION_DOWN) {
 				if (keyCode >= KeyEvent.KEYCODE_0
 						&& keyCode <= KeyEvent.KEYCODE_9) {
 					
 					if (firstInput) {
 						input = new StringBuilder();
 						firstInput = false;
 					}
 					String toAdd = null;
 					// check for number keys
 
 					switch (keyCode) {
 					case KeyEvent.KEYCODE_0:
 						toAdd = "0";
 						break;
 					case KeyEvent.KEYCODE_1:
 						toAdd = "1";
 						break;
 					case KeyEvent.KEYCODE_2:
 						toAdd = "2";
 						break;
 					case KeyEvent.KEYCODE_3:
 						toAdd = "3";
 						break;
 					case KeyEvent.KEYCODE_4:
 						toAdd = "4";
 						break;
 					case KeyEvent.KEYCODE_5:
 						toAdd = "5";
 						break;
 					case KeyEvent.KEYCODE_6:
 						toAdd = "6";
 						break;
 					case KeyEvent.KEYCODE_7:
 						toAdd = "7";
 						break;
 					case KeyEvent.KEYCODE_8:
 						toAdd = "8";
 						break;
 					case KeyEvent.KEYCODE_9:
 						toAdd = "9";
 						break;
 					default:
 						break;
 					}
 					this.makeStringOutOfInput(toAdd);
 					information.setString(input);
 					return true;
 				}else if(keyCode == KeyEvent.KEYCODE_PERIOD){
 					this.addDot();
 				}else if(keyCode == KeyEvent.KEYCODE_DEL){
 					this.deleteInput();
 				}else if (keyCode == KeyEvent.KEYCODE_ENTER) {
 					IP_TO_CONNECT_TO = input.toString();
 					FootFingerActivity.hideSoftInput();
 					connectToServer();
 					return true;
 				}
 			}
 			return false;
 		}
 
 		private void addDot() {
 			if(input.length()<15){
 				input.append('.');
 				information.setString(input);
 			}
 		}
 
 		private void deleteInput() {
			if(input.length()>0){
 				input.setLength(input.length()-1);
 //				if(input.length()==4||input.length()==8||input.length()==12){
 //					input.setLength(input.length()-1);
 //				}
 				input.trimToSize();
 				if(input.length()==0){
					input.append("Enter IP");
 					firstInput = true;
 				}
 				information.setString(input);
 			}
 		}
 
 		private void makeStringOutOfInput(String toAdd) {
 			if(input.length()<15){
 //				if(input.length()==3||input.length()==7||input.length()==11){
 //					input.append('.');
 //				}
 				input.append(toAdd);
 				
 			}
 		}
 	};
 
 	private CCLabel information;
 	private InputMethodManager inputMethodManager;
 
 	protected ShooterConnectLayer(ccColor4B color) {
 		super(color);
 
 		this.setIsTouchEnabled(true);
 		float winScaleWidthFactor = (float) (CCDirector.sharedDirector()
 				.displaySize().getWidth() / 960.0f);
 		winSize = CCDirector.sharedDirector().winSize();
 		CCSprite background = CCSprite.sprite("intro/intro_bg.png");
 
 		background.setScale(winScaleWidthFactor);
 		background.setAnchorPoint(CGPoint.ccp(0f, 0f));
 
 		addChild(background);
 
 		CCSprite text = CCSprite.sprite("selection/shooter_txt.png");
 		text.setScale(winScaleWidthFactor);
 		text.setPosition(CGPoint.ccp(CCDirector.sharedDirector().displaySize()
 				.getWidth() / 2, 650.0f));
 		addChild(text);
 
 		information = CCLabel
 				.makeLabel(this.input.toString(), "DroidSans", 30f);
 		information.setPosition(CGPoint.ccp(winSize.getWidth() / 2f - 20f,
 				winSize.getHeight() / 2f ));
 		information.setColor(ccColor3B.ccBLACK);
 
 		CCMenuItem shooterItem = CCMenuItemImage.item("enterip/shooter.png",
 				"enterip/shooter.png", this, "enterIP");
 		CCMenuItem backItem = CCMenuItemImage.item("selection/prev_btn.png",
 				"selection/prev_btn_p.png", this, "prevClick");
 		backItem.setScale(winScaleWidthFactor);
 		shooterItem.setScale(winScaleWidthFactor);
 
 		backItem.setScale(winScaleWidthFactor);
 		backItem.setPosition(0f, -300f);
 
 		CCMenu menu = CCMenu.menu(shooterItem, backItem);
 		// menu.alignItemsVertically(300f);
 
 		addChild(menu);
 		addChild(information);
 	}
 
 	public void enterIP(Object sender) {
 		FootFingerActivity.showSoftInput(keyListener);
 	}
 	
 	private void connectToServer(){
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				try {
 					NetworkControllerClient networClient = NetworkControllerClient
 							.getInstance();
 					Log.e("connectTO", IP_TO_CONNECT_TO);
 					networClient.connectSocket(IP_TO_CONNECT_TO,
 							PORT_TO_CONNECT_TO);
 					CCDirector.sharedDirector().replaceScene(
 							ShooterLayer.scene(FootFingerActivity.getVibrator()));
 				} catch (UnknownHostException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}).start();
 		
 	}
 
 	public void prevClick(Object sender) {
 		CCDirector.sharedDirector().replaceScene(MainMenuLayer.scene());
 	}
 
 	public static CCScene scene() {
 		CCScene scene = CCScene.node();
 		CCLayer layer = new ShooterConnectLayer(ccColor4B.ccc4(255, 255, 255,
 				255));
 
 		scene.addChild(layer);
 
 		return scene;
 	}
 
 }
