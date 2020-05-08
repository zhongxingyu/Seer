 package no.hist.gruppe5.pvu.visionshooter;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField;
 import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
 import com.badlogic.gdx.utils.TimeUtils;
 import java.util.ArrayList;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.GameScreen;
 import no.hist.gruppe5.pvu.PVU;
 import no.hist.gruppe5.pvu.visionshooter.entity.*;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import java.util.Random;
import no.hist.gruppe5.pvu.mainroom.ScoreHandler;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import no.hist.gruppe5.sounds.Sounds;
 
 public class VisionScreen extends GameScreen {
 
     int points = 0;
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
     private Stage stage;
 
     public VisionScreen(PVU game) {
         super(game);
         makeButton();
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
         stage.draw();
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
                     System.out.println("Points" + points);
                     elements.remove(i);
                 }
             } else {
                 if (elements.get(i).getElementSprite().getBoundingRectangle().overlaps(mVisionShooterShip.getShipSprite().getBoundingRectangle())) {
                     points -= 40;
                     System.out.println("Points" + points);
                     elements.remove(i);
                 }
             }
         }
 
 
         if ((TimeUtils.millis() - lastElementSpawned) > 1500L) {
             int index = random.nextInt(3);
             ShooterElement i = allElements[index];
             if (i instanceof ShooterFacebook && (noElements[1] > 0)) {
                 ShooterFacebook help = new ShooterFacebook(allElements[index].getElementY());
                 help.setElementY(random.nextInt(90));
                 help.setElementX(180f);
                 elements.add(help);
                 noElements[1]--;
             } else if (i instanceof ShooterYoutube && (noElements[2] > 0)) {
                 ShooterYoutube help = new ShooterYoutube(allElements[index].getElementY());
                 help.setElementY(random.nextInt(90));
                 help.setElementX(180f);
                 elements.add(help);
                 noElements[2]--;
             } else if (noElements[0] > 0) {
                 ShooterDokument help = new ShooterDokument(allElements[index].getElementY());
                 help.setElementY(random.nextInt(90));
                 help.setElementX(180f);
                 elements.add(help);
                 noElements[0]--;
             }
 
             lastElementSpawned = TimeUtils.millis();
         }
 
         for (int i = 0; i < elements.size(); i++) {
             if (elements.get(i).getElementX() > 0) {
                 elements.get(i).update(delta);
             } else {
                 points -= 20;
                 System.out.println("Points:" + points);
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
 
     public void makeButton() {
         stage = new Stage(192f,116f, false);
         Gdx.input.setInputProcessor(stage);
         tex = new Texture(Gdx.files.internal("data/DialogTexture.png"));
         textboxskin = new Skin();
         textfieldstyle = new TextField.TextFieldStyle();
         textboxskin.add("textfieldback", new TextureRegion(tex, 1, 1, 190, 56));
         Drawable d = textboxskin.getDrawable("textfieldback");
         button = new Button(d);
         stage.addActor(button);
         button.setHeight(15f);
         button.setWidth(15f);
         button.setPosition(PVU.GAME_WIDTH - 15, PVU.GAME_HEIGHT - 15);
         button.setSkin(skin);
         button.addListener(new ClickListener() {
             @Override
             public void clicked(InputEvent event, float x, float y) {
                 ScoreHandler.updateScore(0, points);
                 Gdx.app.exit();
             }
         });
 
 
 
     }
 }
