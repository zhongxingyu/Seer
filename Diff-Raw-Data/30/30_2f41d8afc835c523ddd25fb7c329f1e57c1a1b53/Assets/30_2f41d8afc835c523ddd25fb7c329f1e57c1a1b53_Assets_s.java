 package no.hist.gruppe5.pvu;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.utils.Array;
 
 /**
  * Created with IntelliJ IDEA.
  * User: karl
  * Date: 8/28/13
  * Time: 10:06 AM
  */
 public class Assets {
 
     public static TextureRegion testRegion;
     public static TextureRegion visionShooterRegion; 
     public static TextureRegion visionShooterDocumentRegion; 
     public static TextureRegion visionShooterShipRegion;
     public static TextureRegion visionShooterBullet; 
     public static TextureRegion visionShooterFacebookRegion;
     public static TextureRegion visionShooterYoutubeRegion;
     
     public static TextureRegion[] mainAvatar; 
     
     public static final int SECONDARY_AVATAR_LEFT_1 = 0;
     public static final int SECONDARY_AVATAR_LEFT_1_FRAME_2 = 1;
     public static final int SECONDARY_AVATAR_LEFT_2 = 2;
     public static final int SECONDARY_AVATAR_LEFT_2_FRAME_2 = 3;
     public static final int SECONDARY_AVATAR_RIGHT_1 = 4;
     public static final int SECONDARY_AVATAR_RIGHT_1_FRAME_2 = 5;
     
     public static final int SECONDARY_AVATAR_RIGHT_2 = 6;
     public static final int SECONDARY_AVATAR_RIGHT_2_FRAME_2 = 7;
     public static final int SECONDARY_AVATAR_RIGHT_3 = 8; 
     public static final int SECONDARY_AVATAR_RIGHT_3_FRAME_2 = 9; 
     
     public static final int MAIN_AVATAR_SITTING = 10;   
     
     public static final int MAIN_AVATAR_FRONT = 11;
     public static final int MAIN_AVATAR_FRONT_FRAME_2 = 12;
     public static final int MAIN_AVATAR_FRONT_FRAME_3 = 13;
     
     public static final int MAIN_AVATAR_SIDE_RIGHT = 14;
     public static final int MAIN_AVATAR_STEP_RIGHT = 15;
     public static final int MAIN_AVATAR_STEP_RIGHT_FRAME_2 = 16;
     
     public static final int MAIN_AVATAR_SIDE_LEFT = 17;
     public static final int MAIN_AVATAR_STEP_LEFT = 18;
     public static final int MAIN_AVATAR_STEP_LEFT_FRAME_2 = 19;
     
     public static final int MAIN_AVATAR_BACK = 20;
     public static final int MAIN_AVATAR_BACK_FRAME_2 = 21;
     public static final int MAIN_AVATAR_BACK_FRAME_3 = 22;
   
     
    
            
   
        
              
     public static BitmapFont primaryFont10px;
 
     // MAIN SCREEN
     public static TextureRegion msBackground;
     public static TextureRegion[] msBurndownCarts;
 
 
     public static void load() {
         primaryFont10px = new BitmapFont(
                 Gdx.files.internal("data/LucidaBitmap10px.fnt"),
                 Gdx.files.internal("data/LucidaBitmap10px_0.png"), false);
         primaryFont10px.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
 
         TextureAtlas visionShooterAtlas = new TextureAtlas(Gdx.files.internal("data/VisionShooterTexture.pack"));
         TextureAtlas mainAvatarAtlas = new TextureAtlas(Gdx.files.internal("data/Avatar.pack"));
         
         visionShooterRegion = visionShooterAtlas.findRegion("VisionShooterTexture"); 
         visionShooterDocumentRegion = visionShooterAtlas.findRegion("Document");
         visionShooterShipRegion = visionShooterAtlas.findRegion("VisionShooterShip");
         visionShooterBullet = visionShooterAtlas.findRegion("Bullet"); 
         visionShooterFacebookRegion = visionShooterAtlas.findRegion("Facebook");
         visionShooterYoutubeRegion = visionShooterAtlas.findRegion("YouTube");
         
         Array mainAvatarArray = mainAvatarAtlas.findRegions("Avatar");
         mainAvatar = new TextureRegion[mainAvatarArray.size];
         for(int i = 0; i < mainAvatarArray.size;i++){
             mainAvatar[i] = (TextureRegion)mainAvatarArray.get(i);
         }
 
         // MAIN SCREEN
         TextureAtlas msAtlas = new TextureAtlas(Gdx.files.internal("data/main_room.pack")) ;
 
         msBackground = msAtlas.findRegion("main_room");
 
         Array carts = msAtlas.findRegions("chart");
         msBurndownCarts = new TextureRegion[carts.size];
         for (int i = 0; i < carts.size; i++) {
             msBurndownCarts[i] = (TextureRegion) carts.get(i);
         }
     }
 
     public static TextureRegion getAvatarRegion(int region) {
         if (region > mainAvatar.length) return null;
         return mainAvatar[region];
     }
 
     public static void dispose() {
     }
 }
