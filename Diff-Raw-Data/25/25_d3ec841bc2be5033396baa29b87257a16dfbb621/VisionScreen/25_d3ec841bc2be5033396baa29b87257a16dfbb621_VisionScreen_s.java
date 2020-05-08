 package no.hist.gruppe5.pvu.visionshooter;
 
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenManager;
 import aurelienribon.tweenengine.equations.Quint;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField;
 import com.badlogic.gdx.utils.TimeUtils;
 import java.util.ArrayList;
 import java.util.Random;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.GameScreen;
 import no.hist.gruppe5.pvu.PVU;
 import no.hist.gruppe5.pvu.ScoreHandler;
 import no.hist.gruppe5.pvu.visionshooter.entity.*;
 import no.hist.gruppe5.pvu.sound.Sounds;
 
 public class VisionScreen extends GameScreen {
 
     private int points = 0;
     private Sprite mVisionDocument;
     private ShooterShip mVisionShooterShip;
     private ArrayList<Bullet> shipProjectiles;
     private long mLastBulletShot = 0;
     private ArrayList<ShooterElement> elements;
     private int[] noElements;//Number of elements
     private ShooterElement[] allElements = new ShooterElement[3];
     private long lastElementSpawned = 0;
     private Random random = new Random();
     private Label pointTextLabel;
     private Label pointValueLabel;
     private String pointText = "Points: ";
     private String pointValue;
     private Button button;
     private Skin textboxskin;
     private Texture tex;
     private Skin skin = new Skin();
     private TextField.TextFieldStyle textfieldstyle;
     private Sounds sound;
     private TweenManager mTweenManager;
 
     public VisionScreen(PVU game) {
         super(game);
         mVisionShooterShip = new ShooterShip();
         shipProjectiles = new ArrayList();
         elements = new ArrayList();
         allElements[0] = new ShooterFacebook(0);
         allElements[1] = new ShooterYoutube(0);
         allElements[2] = new ShooterDokument(0);
 
         noElements = new int[3];
 
         noElements[0] = 5;//Dokument
         noElements[1] = 7;//Facebook
         noElements[2] = 8;//Youtube
 
 
         LabelStyle pointStyle = new LabelStyle(Assets.primaryFont10px, Color.BLACK);
         pointTextLabel = new Label(pointText, pointStyle);
         pointTextLabel.setFontScale(0.8f);
         pointTextLabel.setPosition((PVU.GAME_WIDTH * 0.9f) - pointTextLabel.getPrefWidth(), PVU.GAME_HEIGHT * 0.05f);
 
         pointValue = "" + points;
         pointValueLabel = new Label(pointValue, pointStyle);
         pointValueLabel.setFontScale(0.8f);
         pointValueLabel.setPosition((PVU.GAME_WIDTH) * 0.87f, PVU.GAME_HEIGHT * 0.05f);
 
         sound = new Sounds();
 
         mTweenManager = new TweenManager();
         Tween.registerAccessor(ShooterElement.class, new ShooterElemementAccessor());
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(1, 1, 1, 1);
 
         batch.begin();
         batch.draw(Assets.visionShooterRegion, 0, 0, PVU.GAME_WIDTH, PVU.GAME_HEIGHT);
 
         if (!shipProjectiles.isEmpty()) {
             for (int i = 0; i < shipProjectiles.size(); i++) {
                 shipProjectiles.get(i).draw(batch);
             }
         }
         if (!elements.isEmpty()) {
             for (int i = 0; i < elements.size(); i++) {
                 elements.get(i).draw(batch);
             }
         }
 
         mVisionShooterShip.draw(batch);
         pointTextLabel.draw(batch, 1f);
         pointValueLabel.draw(batch, 1f);
 
         batch.end();
     }
 
     @Override
     protected void update(float delta) {
         mVisionShooterShip.update(delta);
         if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
             if ((TimeUtils.millis() - mLastBulletShot) > 800L) {
                 Bullet vB = new Bullet();
                 vB.setProjectileY(mVisionShooterShip.getShipY() + (mVisionShooterShip.getShipHeight() / 2));
                 vB.setProjectileX(mVisionShooterShip.getShipX());
                 shipProjectiles.add(vB);
                 mLastBulletShot = TimeUtils.millis();
                 sound.playSound(0);
             }
         }
 
         for (int i = 0; i < shipProjectiles.size(); i++) {
             if (shipProjectiles.get(i).getProjectileX() < 196) {
                 shipProjectiles.get(i).update(delta);
             } else {
                 shipProjectiles.remove(i);
             }
         }
         for (int i = 0; i < elements.size(); i++) {
             for (int j = 0; j < shipProjectiles.size();) {
                 if (shipProjectiles.get(j).getBulletSprite().getBoundingRectangle().overlaps(elements.get(i).getElementSprite().getBoundingRectangle())) {
                     if (elements.get(i) instanceof ShooterDokument) {
                        points -= 60;
                     }
                     sound.playSound(1);
                     shipProjectiles.remove(j);
                     elements.remove(i);
                     i--;
                     break;
                 } else {
                     j++;
                 }
 
             }
         }
         for (int i = 0; i < elements.size(); i++) {
             if (elements.get(i) instanceof ShooterDokument) {
                 if (elements.get(i).getElementSprite().getBoundingRectangle().overlaps(mVisionShooterShip.getShipSprite().getBoundingRectangle())) {
                     points += 40;
                     elements.remove(i);
                 }
             } else {
                 if (elements.get(i).getElementSprite().getBoundingRectangle().overlaps(mVisionShooterShip.getShipSprite().getBoundingRectangle())) {
                    points -= 40;
                    elements.remove(i);
                 }
             }
         }
 
 
         if ((TimeUtils.millis() - lastElementSpawned) > 1500L) {
             int index = random.nextInt(3);
             ShooterElement i = allElements[index];
             ShooterElement help = null;
             if (i instanceof ShooterFacebook && (noElements[1] > 0)) {
                 help = new ShooterFacebook(allElements[index].getElementY());
                 elements.add(help);
                 noElements[1]--;
             } else if (i instanceof ShooterYoutube && (noElements[2] > 0)) {
                 help = new ShooterYoutube(allElements[index].getElementY());
                 elements.add(help);
                 noElements[2]--;
             } else if (noElements[0] > 0) {
                 help = new ShooterDokument(allElements[index].getElementY());
                 elements.add(help);
                 noElements[0]--;
             }
 
             if (help != null) {
                 help.setElementY(random.nextInt(Math.round(PVU.GAME_HEIGHT - help.getElementHeight())));
                 help.setElementX(200f);
                 Tween.to(help, ShooterElemementAccessor.POS_X, 1f)
                         .target(160f)
                         .ease(Quint.IN)
                         .start(mTweenManager);
             }
 
             lastElementSpawned = TimeUtils.millis();
         }
 
         for (int i = 0; i < elements.size(); i++) {
             if (elements.get(i).getElementX() > 0) {
                 elements.get(i).update(delta);
            } else {
                 points -= 20;
                 elements.remove(i);
             }
         }
         pointValue = "" + points;
         pointValueLabel.setText(pointValue);
 
         if (elements.isEmpty() && finish()) {
             ScoreHandler.updateScore(0, points);
             pointValueLabel.setFontScale(2f);
             pointTextLabel.setFontScale(2f);
             pointValueLabel.setPosition(pointTextLabel.getX() + pointTextLabel.getPrefWidth(), PVU.GAME_HEIGHT / 2);
             pointTextLabel.setPosition((PVU.GAME_WIDTH / 2) - pointTextLabel.getPrefWidth() / 2, PVU.GAME_HEIGHT / 2);
 
         }
 
         if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
             game.setScreen(PVU.MAIN_SCREEN);
         }
 
         mTweenManager.update(delta);
 
     }
 
     @Override
     protected void cleanUp() {
     }
 
     private boolean finish() {
         for (int i = 0; i < 3; i++) {
             if (noElements[i] > 0) {
                 return false;
             }
         }
         return true;
     }
 }
