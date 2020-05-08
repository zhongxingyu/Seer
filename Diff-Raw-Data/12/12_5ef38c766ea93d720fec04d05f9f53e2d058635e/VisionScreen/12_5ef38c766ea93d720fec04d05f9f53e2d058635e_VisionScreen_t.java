 package no.hist.gruppe5.pvu.visionshooter;
 
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenManager;
 import aurelienribon.tweenengine.equations.Quint;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.utils.TimeUtils;
 import java.util.ArrayList;
 import java.util.Random;
 import no.hist.gruppe5.pvu.*;
 import no.hist.gruppe5.pvu.sound.Sounds;
 import no.hist.gruppe5.pvu.umlblocks.ScrollingBackground;
 import no.hist.gruppe5.pvu.visionshooter.entity.*;
 
 public class VisionScreen extends GameScreen {
 
     private ShooterShip mVisionShooterShip = new ShooterShip();
     private ScrollingBackground mBackground;
     //Spawning of elements
     private ArrayList<Bullet> mShipProjectiles = new ArrayList<Bullet>();
     private long mLastBulletShot = 0;
     private ArrayList<Element> mElements = new ArrayList<Element>();
    private int[] mNoElements = {7, 8, 8};//Number of elements: dokument, facebook, youtube
     private int[] mElementsGot = {0, 0, 0}; // TODO set to 0
     private Element[] mAllElements = {new Facebook(0), new Youtube(0), new Document(0)};//Used for adding mRandom elements to mElements
     private long mLastElementSpawned = 0;
     private Random mRandom = new Random();
     public int mPoints = 0;
     private Label mPointTextLabel;
     private Label mPointValueLabel;
     private String mPointText = "Poeng: ";
     private Sounds mSound;
     private TweenManager mTweenManager;
     private static int MAX_POINTS = 200;
 
     public VisionScreen(PVU game) {
         super(game);
         LabelStyle pointStyle = new LabelStyle(Assets.primaryFont10px, Color.BLACK);
         mSound = new Sounds();
 
         mPointTextLabel = new Label(mPointText, pointStyle);
         mPointTextLabel.setPosition((PVU.GAME_WIDTH * 0.9f) - mPointTextLabel.getPrefWidth(), PVU.GAME_HEIGHT * 0.05f);
         mPointTextLabel.setFontScale(0.8f);
 
         mPointValueLabel = new Label(String.valueOf(mPoints), pointStyle);
         mPointValueLabel.setPosition((PVU.GAME_WIDTH) * 0.87f, PVU.GAME_HEIGHT * 0.05f);
         mPointValueLabel.setFontScale(0.8f);
 
         mBackground = new ScrollingBackground(Assets.visionShooterRegion, 50);
 
         mTweenManager = new TweenManager();
         Tween.registerAccessor(Element.class, new ElementAccessor());
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(1, 1, 1, 1);
 
         batch.begin();
         mBackground.draw(batch);
         drawBullets();
         drawElements();
         drawElementsGui();
         mVisionShooterShip.draw(batch);
         mPointTextLabel.draw(batch, 1f);
         mPointValueLabel.draw(batch, 1f);
         batch.end();
 
     }
 
     @Override
     protected void update(float delta) {
         mVisionShooterShip.update(delta);
         if (Input.continuousAction()) {
             shootBullet();
         }
         mBackground.update(delta);
         updateBulletPos(delta);
         updateElementPos(delta);
 
         checkBulletHit();
         checkShipHit();
 
         if ((TimeUtils.millis() - mLastElementSpawned) > 700L) {
             addElement();
         }
 
 
         checkFinish();
         mTweenManager.update(delta);
     }
 
     @Override
     protected void cleanUp() {
     }
 
     private boolean finish() {
         for (int i = 0; i < 3; i++) {
             if (mNoElements[i] > 0) {
                 return false;
             }
         }
         return true;
     }
 
     private void shootBullet() {
         if ((TimeUtils.millis() - mLastBulletShot) > 400L) {
             Bullet vB = new Bullet();
             vB.setProjectileY(mVisionShooterShip.getShipY() + (mVisionShooterShip.getShipHeight() / 2));
             vB.setProjectileX(mVisionShooterShip.getShipX());
             mShipProjectiles.add(vB);
             mLastBulletShot = TimeUtils.millis();
             mSound.playSound(0);
         }
 
     }
 
     private void updateBulletPos(float delta) {
         for (int i = 0; i < mShipProjectiles.size(); i++) {
             if (mShipProjectiles.get(i).getProjectileX() < 196) {
                 mShipProjectiles.get(i).update(delta);
             } else {
                 mShipProjectiles.remove(i);
             }
         }
     }
 
     private void updateElementPos(float delta) {
         for (int i = 0; i < mElements.size(); i++) {
             if (mElements.get(i).getElementX() > - 30f) {
                 mElements.get(i).update(delta);
             } else if (mElements.get(i) instanceof Document) {
                 mPoints -= 20;
                 mElements.remove(i);
             } else   {
                 mPoints -= 10;
                 mElements.remove(i);
             }
         }
 
         if(mPoints < 0) mPoints = 0;
     }
 
     private void drawBullets() {
         if (!mShipProjectiles.isEmpty()) {
             for (int i = 0; i < mShipProjectiles.size(); i++) {
                 mShipProjectiles.get(i).draw(batch);
             }
         }
     }
 
     private void drawElements() {
         if (!mElements.isEmpty()) {
             for (int i = 0; i < mElements.size(); i++) {
                 mElements.get(i).draw(batch);
             }
         }
     }
 
     private void drawElementsGui() {
         for(int i = 0; i < mElementsGot[0]; i++) {
             batch.draw(Assets.visionShooterDocumentRegion, 1 + (2 * (i * 4)), PVU.GAME_HEIGHT - 9, 7, 8);
         }
 
         for(int i = 0; i < mElementsGot[1]; i++) {
             batch.draw(Assets.visionShooterFacebookRegion, (PVU.GAME_WIDTH - 9) - (2 * (i * 4)), PVU.GAME_HEIGHT - 9, 7, 8);
         }
 
         for(int i = 0; i < mElementsGot[2]; i++) {
             batch.draw(Assets.visionShooterYoutubeRegion, (PVU.GAME_WIDTH - 9) - (2 * (i * 4)), PVU.GAME_HEIGHT - 18, 7, 8);
         }
     }
 
     private void addElement() {//adds element to the arrray-list
         int index = mRandom.nextInt(3);
         Element i = mAllElements[index];
         Element help = null;
         if (i instanceof Facebook && (mNoElements[1] > 0)) {
             help = new Facebook(mAllElements[index].getElementY());
             mElements.add(help);
             mNoElements[1]--;
         } else if (i instanceof Youtube && (mNoElements[2] > 0)) {
             help = new Youtube(mAllElements[index].getElementY());
             mElements.add(help);
             mNoElements[2]--;
         } else if (mNoElements[0] > 0) {
             help = new Document(mAllElements[index].getElementY());
             mElements.add(help);
             mNoElements[0]--;
         }
 
         if (help != null) {
             help.setElementY(mRandom.nextInt(Math.round(PVU.GAME_HEIGHT - help.getElementHeight())));
             help.setElementX(210f);
             Tween.to(help, ElementAccessor.POS_X, 1f)
                     .target(160f)
                     .ease(Quint.IN)
                     .start(mTweenManager);
         }
 
         mLastElementSpawned = TimeUtils.millis();
     }
 
     private void checkBulletHit() {
         for (int i = 0; i < mElements.size(); i++) {
             for (int j = 0; j < mShipProjectiles.size();) {
                 if (mShipProjectiles.get(j).getBulletSprite().getBoundingRectangle().overlaps(mElements.get(i).getElementSprite().getBoundingRectangle())) {
                     if (mElements.get(i) instanceof Document) {
                         // Do not kill document!
                         mPoints -= 20;
                     } else {
                         // Killed bad activity, more points!
                         mPoints += 10;
                     }
 
                     if(mElements.get(i) instanceof Facebook)
                         mElementsGot[1]++;
                     else if(mElements.get(i) instanceof Youtube)
                         mElementsGot[2]++;
 
 
                     mSound.playSound(1);
                     mShipProjectiles.remove(j);
                     mElements.remove(i);
 
                     i--;
                     break;
                 } else {
                     j++;
                 }
 
             }
         }
         mPointValueLabel.setText(String.valueOf(mPoints));
 
     }
 
     private void checkShipHit() {
         for (int i = 0; i < mElements.size(); i++) {
             if (mElements.get(i) instanceof Document) {
                 if (mElements.get(i).getElementSprite().getBoundingRectangle().overlaps(mVisionShooterShip.getShipSprite().getBoundingRectangle())) {
                     mPoints += 40;
                     mElements.remove(i);
                     mElementsGot[0]++;
                 }
             } else {
                 if (mElements.get(i).getElementSprite().getBoundingRectangle().overlaps(mVisionShooterShip.getShipSprite().getBoundingRectangle())) {
                     if (mPoints - 40 > 0) {
                         mPoints -= 40;
                         mElements.remove(i);
 
                     } else {
                         mPoints = 0;
                         mElements.remove(i);
 
                     }
                 }
             }
         }
         mPointValueLabel.setText(String.valueOf(mPoints));
     }
 
     private void checkFinish() {
         if (mElements.isEmpty() && finish()) {
             ScoreHandler.updateScore(ScoreHandler.VISION, (float)mPoints/(float)MAX_POINTS);
             game.setScreen(new VisionEndScreen(game,mPoints, mElementsGot));
 
         }
     }
 }
