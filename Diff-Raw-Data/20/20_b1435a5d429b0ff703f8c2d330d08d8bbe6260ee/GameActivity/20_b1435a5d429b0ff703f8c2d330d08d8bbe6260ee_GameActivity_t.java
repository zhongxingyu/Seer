 package com.cronlygames.cocos2d.template;
 //Yeah baby ! ! !
 //You touch my tralala 
 import org.box2d.common.BBVec2;
 import org.cocos2d.events.CCTouchDispatcher;
 import org.cocos2d.grid.CCTiledGrid3D;
 import org.cocos2d.layers.CCLayer;
 import org.cocos2d.layers.CCScene;
 import org.cocos2d.nodes.CCDirector;
 import org.cocos2d.nodes.CCLabel;
 import org.cocos2d.nodes.CCSprite;
 import org.cocos2d.nodes.CCTextureCache;
 import org.cocos2d.nodes.CCTileMapAtlas;
 import org.cocos2d.opengl.CCGLSurfaceView;
 import org.cocos2d.types.CGPoint;
 import org.cocos2d.types.CGRect;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class GameActivity extends Activity {
 	private CCGLSurfaceView mGLSurfaceView;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         // set the window status, no tile, full screen and don't sleep
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                 WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
         mGLSurfaceView = new CCGLSurfaceView(this);
         
         setContentView(mGLSurfaceView);
         
         // attach the OpenGL view to a window
         CCDirector.sharedDirector().attachInView(mGLSurfaceView);
 
         // no effect here because device orientation is controlled by manifest
         CCDirector.sharedDirector().setDeviceOrientation(CCDirector.kCCDeviceOrientationPortrait);
         
         // show FPS
         // set false to disable FPS display, but don't delete fps_images.png!!
         //CCDirector.sharedDirector().setDisplayFPS(true);
 
         CCDirector.sharedDirector().setScreenSize(480, 800);
         
         // frames per second
         CCDirector.sharedDirector().setAnimationInterval(1.0f / 45);
 
         CCScene scene = TemplateLayer.scene();
         
         // Make the Scene active
         CCDirector.sharedDirector().runWithScene(scene);
     }
     
     @Override
     public void onStart() {
         super.onStart();        
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         CCDirector.sharedDirector().onPause();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         
         CCDirector.sharedDirector().onResume();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         //CCTextureCache.sharedTextureCache().removeAllTextures();
         CCDirector.sharedDirector().end();
     }
     
     
     static class TemplateLayer extends CCLayer {
         CCLabel lbl;
         CCSprite balle;
         private CCSprite barre;
         private BBVec2 direction;
         private BBVec2 directionBarre;
         private CCSprite table;
         private CCSprite frize;
         private int pos = -1;
         private CGPoint convertedLocation;
         private CCSprite barreComputer;
         private BBVec2 directionBarreComputer;
         private int col = 11;
         private int returned = 11;
         private float diffX;
         private float diffY;
         private static float MAXSPEED = 15;
         
     	public static CCScene scene() {
     		CCScene scene = CCScene.node();
     		CCLayer layer = new TemplateLayer();
     		
     		scene.addChild(layer);
     		
     		return scene;
     	}
 
         protected TemplateLayer() {
             
         	this.setIsTouchEnabled(true);
 
         
             lbl = CCLabel.makeLabel("Hello World!", "DroidSans", 24);
 
             addChild(lbl, 0);
             lbl.setPosition(CGPoint.ccp(160, 240));
             
             balle = CCSprite.sprite("balle.png");
            
             balle.setPosition(200, 300);
             balle.setScale(1.6f);
             direction = new BBVec2(0, -1);
             addChild(balle, 1);
       
             directionBarre = new BBVec2(0, 0);
             barre = CCSprite.sprite("main.png");
             barre.setPosition(250, 100);
             barre.setScale(2);
             addChild(barre, 1);
 
             directionBarreComputer = new BBVec2(5, 0);
             barreComputer = CCSprite.sprite("main.png");
             barreComputer.setPosition(CCDirector.sharedDirector().winSize().width/2, 570);
             barreComputer.setScale(2);
             barreComputer.setRotation(180f);
             addChild(barreComputer, 1);
             
             table = CCSprite.sprite("table.png");
             table.setPosition(CCDirector.sharedDirector().winSize().width/2, CCDirector.sharedDirector().winSize().height/2);
             table.setScaleY(2.2f);
             addChild(table, 0);
             
             frize = CCSprite.sprite("frize.png");
             frize.setPosition(table.getPosition().x + table.getContentSize().width*table.getScaleX()*0.8f/2, table.getPosition().y + table.getContentSize().height*table.getScaleY()*0.9f/2);
             frize.setScale(0.4f);
             
             addChild(frize, 0);
             schedule("update", 1.0f / 60);
             schedule("updateComputer", 1.0f / 60);
             //new CCTileMapAtlas()
             
 
         }
         
         
         public void updateComputer(float dt) {
            
             
             if(barreComputer.getPosition().x <= 100)
             {
                 directionBarreComputer.x = -directionBarreComputer.x;
                 barreComputer.setPosition(barreComputer.getPosition().x + directionBarreComputer.x + 1f, barreComputer.getPosition().y + directionBarreComputer.y);
             }
             else if(barreComputer.getPosition().x >= CCDirector.sharedDirector().winSize().width - 100)
             {
                 directionBarreComputer.x = -directionBarreComputer.x;
                 barreComputer.setPosition(barreComputer.getPosition().x + directionBarreComputer.x - 1f, barreComputer.getPosition().y + directionBarreComputer.y);
             }
             
             float dx = balle.getPosition().x - barreComputer.getPosition().x;
             float dy = balle.getPosition().y - barreComputer.getPosition().y;
             float radii = (balle.getContentSize().getWidth()* balle.getScaleX())/2 + (barreComputer.getContentSize().getWidth()* barreComputer.getScaleX())/2;
             if ( ( dx * dx )  + ( dy * dy ) < radii * radii
                     || ( (dx + direction.x)  * (dx  + direction.x) )  + ((dy + direction.y)  * (dy  + direction.y) ) < radii * radii  
                 )
             {
                 directionBarreComputer.y = dy*0.1f;
                 directionBarreComputer.x = dx*0.1f;
                 direction.set(dx*0.1f + direction.x, dy*0.1f + direction.y);
                 col  = 0;
             }
             
         
             if(col > 10 && returned > 10)
                 barreComputer.setPosition(barreComputer.getPosition().x + directionBarreComputer.x, 570f);
             else if(col < 11 )
             {
                 barreComputer.setPosition(barreComputer.getPosition().x + directionBarreComputer.x, barreComputer.getPosition().y + directionBarreComputer.y);
                 ++col;
             }
             else
             {
                 barreComputer.setPosition(barreComputer.getPosition().x + directionBarreComputer.x, barreComputer.getPosition().y - directionBarreComputer.y);
                 ++returned; 
             }
             
            
             
             if(col == 10)
                 returned = 0;
         }
         
         public boolean checkDiff(float pos1, float pos2, float diff) {
             if(pos1 == pos2 || (pos1 >= pos2 && pos1 <= pos2 + diff) || (pos1 <= pos2 && pos1 >= pos2 - diff))
                 return true;
             return false;
         }
         
         public void update(float dt) {
             balle.setPosition(balle.getPosition().x + direction.x, balle.getPosition().y + direction.y);
            // while(barre.getPosition())
             
             
           //compare the distance to combined radii
             float dx = balle.getPosition().x - barre.getPosition().x;
             float dy = balle.getPosition().y - barre.getPosition().y;
             float radii = (balle.getContentSize().getWidth()* balle.getScaleX())/2 + (barre.getContentSize().getWidth()* barre.getScaleX())/2;
             if ( ( dx * dx )  + ( dy * dy ) < radii * radii
 //                    || ( (dx - direction.x)  * (dx  - direction.x) )  + ((dy - direction.y)  * (dy  - direction.y) ) < radii * radii 
 //                    || ( (dx + direction.x)  * (dx  + direction.x) )  + ((dy + direction.y)  * (dy  + direction.y) ) < radii * radii 
 //                    || ( (dx - directionBarre.x)  * (dx - directionBarre.x) )  + ((dy - directionBarre.y)  * (dy  - directionBarre.y) ) < radii * radii 
                     //|| ( (dx + direction.x - directionBarre.x)  * (dx + direction.x - directionBarre.x) )  + ((dy + direction.y - directionBarre.y)  * (dy + direction.y  - directionBarre.y) ) < radii * radii 
                 )
                 direction.set(dx*0.1f + direction.x, dy*0.1f + direction.y);
             else if(!checkDiff(barre.getPosition().x, convertedLocation.x, 1) && !checkDiff(barre.getPosition().y, convertedLocation.y, 1))
                 barre.setPosition(barre.getPosition().x + diffX, barre.getPosition().y + diffY);
                 
             
             
             
             if(direction.x > MAXSPEED)
                 direction.x = MAXSPEED;
             else if(direction.x < -MAXSPEED)
                 direction.x = -MAXSPEED;
             
             if(direction.y > MAXSPEED)
                 direction.y = MAXSPEED;
             else if(direction.y < -MAXSPEED)
                 direction.y = -MAXSPEED;
             
            
             if(balle.getPosition().y - balle.getTexture().getHeight()*balle.getScaleY()/2 < 0)
            {
                 direction.y = -direction.y + 0.1f;
                balle.setPosition(balle.getPosition().x + direction.x, balle.getPosition().y + direction.y + 1f);
            }
             
             if(balle.getPosition().x - balle.getTexture().getWidth()*balle.getScaleY()/2 < 0)
            {
                 direction.x = -direction.x + 0.1f;
                balle.setPosition(balle.getPosition().x + direction.x + 1f, balle.getPosition().y + direction.y);
            }
             
             if(balle.getPosition().x + balle.getTexture().getWidth()*balle.getScaleX()/2 > CCDirector.sharedDirector().winSize().getWidth())
            {
                 direction.x = -direction.x - 0.1f;
                balle.setPosition(balle.getPosition().x + direction.x - 1f, balle.getPosition().y + direction.y);
            }
             if(balle.getPosition().y + balle.getTexture().getWidth()*balle.getScaleX()/2 > CCDirector.sharedDirector().winSize().getHeight())
            {
                 direction.y = -direction.y - 0.1f;
                balle.setPosition(balle.getPosition().x + direction.x + 1f, balle.getPosition().y + direction.y - 1f);
            }
           
             direction.set(direction.x*0.99f, direction.y*0.99f);
             
             
 //            if(frize.getTextureRect().contains(balle.getPosition().x, balle.getPosition().y))
 //                frize.setVisible(false);
                 //balle.setPosition(200, 300);
             //if(barre.getPosition().x)
             //if(CGRect.containsRect(balle.getBoundingBox(), barre.getBoundingBox()));
                 //balle.set
             //balle.setPosition(x, y)
         }
 
 //        @Override
 //        public boolean ccTouchesBegan(MotionEvent event) {
 //            CGPoint convertedLocation = CCDirector.sharedDirector()
 //            	.convertToGL(CGPoint.make(event.getX(), event.getY()));
 //
 //            String title = String.format("touch at point(%.2f, %.2f)",
 //            			convertedLocation.x, convertedLocation.y);
 //
 //            if (lbl != null) {
 //            	lbl.setString(title);
 //            }
 //            
 //            return CCTouchDispatcher.kEventHandled;
 //        }
 
         @Override
         public boolean ccTouchesMoved(MotionEvent event) {
             convertedLocation = CCDirector.sharedDirector()
                 .convertToGL(CGPoint.make(event.getX(), event.getY()));
             convertedLocation.y += 50; 
             directionBarre.set(event.getX() - directionBarre.x, event.getY() - directionBarre.y);
             diffX = (convertedLocation.x - barre.getPosition().x)*0.2f;
             diffY = (convertedLocation.y - barre.getPosition().y)*0.2f;
             return CCTouchDispatcher.kEventHandled;
         }
         
     }
 
 }
