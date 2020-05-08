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
 import no.hist.gruppe5.pvu.quiz.QuizHandler;
 import no.hist.gruppe5.pvu.sound.Sounds;
 import no.hist.gruppe5.pvu.visionshooter.entity.*;
 
 public class VisionScreen extends GameScreen {
 
     private ShooterShip mVisionShooterShip = new ShooterShip();
     //Spawning of elements
     private ArrayList<Bullet> mShipProjectiles = new ArrayList<Bullet>();
     private long mLastBulletShot = 0;
     private ArrayList<ShooterElement> mElements = new ArrayList<ShooterElement>();
     private int[] mNoElements = {5, 7, 8};//Number of elements: dokument, facebook, youtube
     private ShooterElement[] mAllElements = {new ShooterFacebook(0), new ShooterYoutube(0), new ShooterDokument(0)};//Used for adding mRandom elements to mElements
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
 
 
         mTweenManager = new TweenManager();
         Tween.registerAccessor(ShooterElement.class, new ShooterElemementAccessor());
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(1, 1, 1, 1);
 
         batch.begin();
         batch.draw(Assets.visionShooterRegion, 0, 0, PVU.GAME_WIDTH, PVU.GAME_HEIGHT);
         drawBullets();
         drawElements();
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
             if (mElements.get(i).getElementX() > 0) {
                 mElements.get(i).update(delta);
             } else if ((mPoints - 20) > 0) {
                 mPoints -= 20;
                 mElements.remove(i);
             } else {
                 mPoints = 0;
                 mElements.remove(i);
             }
         }
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
 
     private void addElement() {//adds element to the arrray-list
         int index = mRandom.nextInt(3);
         ShooterElement i = mAllElements[index];
         ShooterElement help = null;
         if (i instanceof ShooterFacebook && (mNoElements[1] > 0)) {
             help = new ShooterFacebook(mAllElements[index].getElementY());
             mElements.add(help);
             mNoElements[1]--;
         } else if (i instanceof ShooterYoutube && (mNoElements[2] > 0)) {
             help = new ShooterYoutube(mAllElements[index].getElementY());
             mElements.add(help);
             mNoElements[2]--;
         } else if (mNoElements[0] > 0) {
             help = new ShooterDokument(mAllElements[index].getElementY());
             mElements.add(help);
             mNoElements[0]--;
         }
 
         if (help != null) {
             help.setElementY(mRandom.nextInt(Math.round(PVU.GAME_HEIGHT - help.getElementHeight())));
             help.setElementX(200f);
             Tween.to(help, ShooterElemementAccessor.POS_X, 1f)
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
                     if (mElements.get(i) instanceof ShooterDokument) {
                         if ((mPoints - 60) > 0) {
                             mPoints -= 60;
                         } else {
                             mPoints = 0;
                         }
                     }
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
             if (mElements.get(i) instanceof ShooterDokument) {
                 if (mElements.get(i).getElementSprite().getBoundingRectangle().overlaps(mVisionShooterShip.getShipSprite().getBoundingRectangle())) {
                     mPoints += 40;
                     mElements.remove(i);
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
             QuizHandler.updateFinishedMiniGame();
             game.setScreen(new VisionEndScreen(game,mPoints));
 
         }
     }
 }
