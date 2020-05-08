 package no.hist.gruppe5.pvu.intro;
 
 import aurelienribon.tweenengine.Timeline;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenManager;
 import aurelienribon.tweenengine.equations.*;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.GameScreen;
 import no.hist.gruppe5.pvu.PVU;
 import no.hist.gruppe5.pvu.Settings;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created with IntelliJ IDEA.
  * User: karl
  * Date: 9/3/13
  * Time: 1:17 PM
  */
 public class IntroScreen extends GameScreen {
 
     private TweenManager mManager;
     private Sprite mLogo;
     private Sprite mLogoGroup;
     private Sprite mPressSpace;
     private Sprite mBackground;
 
     private BitmapFont mFont;
     private final String TOP_TEXT = "Hva heter du?";
     private final String BOT_TEXT = "Trykk på space for å begynne";
 
     private final int MAX_NAME_CHAR = 6;
     private StringBuilder mCurrentInput;
 
     private boolean mHasPressed = false;
     private boolean mInputName = false;
 
     public IntroScreen(PVU game) {
         super(game);
 
         mFont = Assets.primaryFont10px;
 
         mManager = new TweenManager();
 
         mLogo = new Sprite(Assets.introMainLogo);
         mLogoGroup = new Sprite(Assets.introTeamLogo);
         mPressSpace = new Sprite(Assets.introPressSpace);
         mBackground = new Sprite(Assets.msBackground);
         mLogo.setPosition(-100f, (PVU.GAME_HEIGHT / 2f) - (mLogo.getHeight() / 2f));
         mLogoGroup.setPosition(-100f, (PVU.GAME_HEIGHT / 2f) - (mLogoGroup.getHeight() / 2f));
         mPressSpace.setPosition(-100, 10f);
 
         Timeline.createSequence()
             .push(Tween.set(mBackground, SpriteAccessor.OPACITY)
                     .target(0f))
             .push(Tween.to(mLogo, SpriteAccessor.POS_XY, 1f)
                     .target((PVU.GAME_WIDTH / 2) - (mLogo.getWidth() / 2f), mLogo.getY())
                     .ease(Quint.OUT))
             .pushPause(1f)
             .push(Tween.to(mLogo, SpriteAccessor.POS_XY, 1f)
                     .target(200f, mLogo.getY())
                     .ease(Expo.IN))
             .push(Tween.to(mLogoGroup, SpriteAccessor.POS_XY, 1f)
                     .target((PVU.GAME_WIDTH / 2) - (mLogoGroup.getWidth() / 2f), mLogoGroup.getY())
                     .ease(Quint.OUT))
             .pushPause(1f)
             .push(Tween.to(mLogoGroup, SpriteAccessor.POS_XY, 1f)
                     .target(200f, mLogoGroup.getY())
                     .ease(Expo.IN))
             .beginParallel()
             .push(Tween.set(mLogo, SpriteAccessor.POS_XY)
                     .target(200f, 45f))
             .push(Tween.to(mPressSpace, SpriteAccessor.POS_XY, 1f)
                     .target((PVU.GAME_WIDTH / 2) - (mPressSpace.getWidth() / 2), 10f)
                     .ease(Elastic.OUT))
                 .push(Tween.to(mLogo, SpriteAccessor.POS_XY, 1f)
                         .target((PVU.GAME_WIDTH / 2f) - (mLogo.getWidth() / 2f), 45f)
                         .ease(Elastic.OUT))
                 .push(Tween.to(mBackground, SpriteAccessor.OPACITY, 2f)
                         .target(1f)
                         .ease(Quint.INOUT))
             .end()
             .start(mManager);
 
     }
     private void recieveInput() {
         if(Gdx.input.isKeyPressed(Input.Keys.SPACE) && !mHasPressed) {
             mHasPressed = true;
             Tween.to(mBackground, SpriteAccessor.OPACITY, 0.5f)
                     .target(1f)
                     .ease(Quint.INOUT)
                     .start(mManager);
             Tween.to(mLogo, SpriteAccessor.POS_XY, 3f)
                     .target(90, 300)
                     .ease(Quint.IN)
                     .start(mManager);
 
 
             Gdx.input.setInputProcessor(new InputName());
             mCurrentInput = new StringBuilder();
             mInputName = true;
             System.out.println("Input mode!");
         }
     }
 
 
     private void startGame() {
         Settings.setPlayerName(mCurrentInput.toString());
         game.setScreen(PVU.MAIN_SCREEN);
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(1f, 1f, 1f, 1f);
         batch.begin();
         mBackground.draw(batch);
         if(!mHasPressed || !mInputName) {
             mLogo.draw(batch);
             mLogoGroup.draw(batch);
             mPressSpace.draw(batch);
         } else if (mInputName) {
             mFont.setColor(new Color(Color.RED));
             mFont.draw(batch, TOP_TEXT,
                     (PVU.GAME_WIDTH / 2f) - (mFont.getBounds(TOP_TEXT).width / 2f),
                     (PVU.GAME_HEIGHT / 2f) + 20f);
             mFont.setScale(1.5f);
             mFont.draw(batch, mCurrentInput,
                     (PVU.GAME_WIDTH / 2f) - (mFont.getBounds(mCurrentInput.toString()).width / 2f),
                     PVU.GAME_HEIGHT / 2f);
             mFont.setScale(0.7f);
             mFont.draw(batch, BOT_TEXT,
                     (PVU.GAME_WIDTH / 2f) - (mFont.getBounds(BOT_TEXT).width / 2f),
                     (PVU.GAME_HEIGHT / 2f) - 20f);
             mFont.setScale(1f);
         }
         batch.end();
     }
 
     @Override
     protected void update(float delta) {
         mManager.update(delta);
 
         recieveInput();
     }
 
     @Override
     protected void cleanUp() {
     }
 
     private class InputName implements InputProcessor {
 
         @Override
         public boolean keyTyped(char character) {
             if((int) character == 8 && mCurrentInput.length() > 0) {
                 mCurrentInput.deleteCharAt(mCurrentInput.length() - 1);
             } else if(mCurrentInput.length() < MAX_NAME_CHAR) {
                 if(((Character) character).toString().matches("^[\\p{L}0-9]*$"))
                     mCurrentInput.append(character);
             }
             return false;
         }
 
         @Override
         public boolean keyDown(int keycode) {
            if ((keycode == Input.Keys.SPACE) && mCurrentInput.length() > 0)
                 startGame();
             return false;
         }
 
         @Override
         public boolean keyUp(int keycode) {
             return false;
         }
 
         @Override
         public boolean touchDown(int screenX, int screenY, int pointer, int button) {
             return false;
         }
 
         @Override
         public boolean touchUp(int screenX, int screenY, int pointer, int button) {
             return false;
         }
 
         @Override
         public boolean touchDragged(int screenX, int screenY, int pointer) {
             return false;
         }
 
         @Override
         public boolean mouseMoved(int screenX, int screenY) {
             return false;
         }
 
         @Override
         public boolean scrolled(int amount) {
             return false;
         }
     }
 }
