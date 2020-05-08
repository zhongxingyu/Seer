 package at.blooo.tetris;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.andengine.entity.Entity;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.AlphaModifier;
 import org.andengine.entity.modifier.IEntityModifier;
 import org.andengine.entity.modifier.MoveByModifier;
 import org.andengine.entity.modifier.ParallelEntityModifier;
 import org.andengine.entity.modifier.RotationByModifier;
 import org.andengine.entity.modifier.ScaleModifier;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.texture.region.ITextureRegion;
 
 import android.util.Log;
 import at.blooo.MainActivity;
 import at.blooo.minigame.MiniGameManager;
 
 public class Tetris extends Entity {
 
   final int COLUMNS = 10;
   final int ROWS = 25;
   final int VISIBLE_ROWS = 20;
   final int mPartSize = 64;
 
   Entity[][] mField = new Entity[COLUMNS][ROWS];
   MainActivity mMainActivity;
   Stone mStone;
   MiniGameManager mMiniGameManager;
 
   public Tetris(MainActivity mainActivity, MiniGameManager mgm) {
     mMainActivity = mainActivity;
     mMiniGameManager = mgm;
     mStone = new Stone(this, mPartSize);
 
     ITextureRegion mTetrisBackground = mainActivity.createTetrisBG();
     mTetrisBackground.setTextureSize(COLUMNS * mPartSize, VISIBLE_ROWS
         * mPartSize);
     Entity bg = new Sprite(0, 0, mTetrisBackground,
         mMainActivity.getVertexBufferObjectManager()) {
       public boolean onAreaTouched(
           org.andengine.input.touch.TouchEvent pSceneTouchEvent,
           float pTouchAreaLocalX, float pTouchAreaLocalY) {
         if (pSceneTouchEvent.isActionDown()) {
           Tetris.this.rotateRight();
         }
         return true;
       };
     };
     bg.setPosition((COLUMNS * mPartSize) / 2, (VISIBLE_ROWS * mPartSize) / 2);
 
     mMainActivity.registerTouchArea(bg);
     this.attachChild(bg);
 
   }
 
   public synchronized void initField() {
 
     for (int r = 0; r < ROWS; r++) {
       for (int c = 0; c < COLUMNS; c++) {
         if (mField[c][r] != null) {
           final Entity e = mField[c][r];
           mMainActivity.detachFromScene(e);
           mField[c][r] = null;
         }
       }
     }
 
     mStone.setStone(mMiniGameManager.getNext());
   }
 
   public synchronized void moveLeft() {
     mStone.moveLeft();
   }
 
   public synchronized void moveRight() {
     mStone.moveRight();
   }
 
   public synchronized void rotateRight() {
     mStone.rotateRight();
   }
 
   public synchronized boolean moveDown() {
     ArrayList<Entity> croppedBricks;
     int croppedLinesCount = 0;
 
     if (mStone.collidesOnNextDrop()) {
       croppedBricks = new ArrayList<Entity>();
 
       mStone.freeze();
       croppedLinesCount = cropFullLines(croppedBricks);
 
       destroyBricks(croppedBricks);
 
       mStone.setStone(mMiniGameManager.getNext());
 
       if (mStone.collides()) {
         Log.d("Tetris", "You Lost!");
         // todo: do something useful. Just restart for now
         initField();
       }
       return false;
     } else {
       mStone.moveDown();
       return true;
     }
   }
 
   private void destroyBricks(ArrayList<Entity> croppedBricks) {
     Random rng = new Random();
     for (final Entity e : croppedBricks) {
       IEntityModifier modifier = new ParallelEntityModifier(
           new ScaleModifier(0.5f, 1.0f, 0.2f), 
           new RotationByModifier(0.5f, 200),
           new MoveByModifier(0.5f, rng.nextInt(400) - 200, rng.nextInt(400) - 200),
           new AlphaModifier(0.5f, 0, 255)) {
         @Override
         protected void onModifierFinished(IEntity pItem) {
           mMainActivity.detachFromScene(e);
           super.onModifierFinished(pItem);
           // todo: recycling e would be fine.
         }
       };
       e.registerEntityModifier(modifier);
     }
 
   }
 
   private int cropFullLines(ArrayList<Entity> list) {
     int r, c;
     int lineCount = 0;
 
     for (r = 0; r < ROWS; r++) {
       boolean line_full = true;
       for (c = 0; c < COLUMNS; c++) { // Identify a full line
         if (mField[c][r] == null) {
           line_full = false;
           break;
         }
       }
       if (line_full) { // drop every row above by one
         lineCount++;
         for (int r2 = r; r2 < ROWS - 1; r2++) {
           for (int c2 = 0; c2 < COLUMNS; c2++) {
 
             if (mField[c2][r2] != null) {
               list.add(mField[c2][r2]);
             }
 
             mField[c2][r2] = mField[c2][r2 + 1];
             if (mField[c2][r2] != null) {
               updatePartPos(c2, r2, mField[c2][r2]);
             }
 
             mField[c2][r2 + 1] = null;
           }
         }
       }
     }
     return lineCount;
   }
 
   void updatePartPos(int c, int r, Entity e) {
     e.setPosition(c * mPartSize + mPartSize / 2, r * mPartSize + mPartSize / 2);
   }
 
   public synchronized void moveToBottom() {
     while (moveDown())
       ;
   }
 
 }
