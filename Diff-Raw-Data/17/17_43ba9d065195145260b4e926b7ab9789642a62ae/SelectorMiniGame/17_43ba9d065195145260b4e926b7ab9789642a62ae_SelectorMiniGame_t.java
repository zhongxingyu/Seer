 package at.blooo.minigame;
 
 import java.util.Random;
 
 import org.andengine.entity.Entity;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.ui.activity.BaseGameActivity;
 
 import android.util.Log;
 import at.blooo.MainActivity;
 
 public class SelectorMiniGame extends Entity implements MiniGame {
 
   MainActivity mMainActivity;
 
   Entity FigT;
   Entity FigL1;
   Entity FigL2;
   Entity FigZ1;
   Entity FigZ2;
   Entity FigSquare;
   Entity FigI;
 
   int selected = -1;
 
   public SelectorMiniGame(MainActivity mainActivity) {
     mMainActivity = mainActivity;
   }
 
   @Override
   public boolean[][] getField() {
 
     boolean field[][] = new boolean[MainActivity.FIGURE_SIZE][MainActivity.FIGURE_SIZE];
 
     switch (selected) {
     case 0:// T
       field[1][2] = true;
       field[2][2] = true;
       field[2][3] = true;
       field[3][2] = true;
       break;
     case 1: // L1
       field[1][2] = true;
       field[2][2] = true;
       field[3][2] = true;
       field[3][3] = true;
       break;
     case 2:// L2
       field[1][2] = true;
       field[1][3] = true;
       field[2][2] = true;
       field[3][2] = true;
       break;
     case 3:// Z1
       field[1][2] = true;
       field[2][2] = true;
       field[2][1] = true;
       field[3][1] = true;
       break;
     case 4:// Z2
       field[1][1] = true;
       field[2][1] = true;
       field[2][2] = true;
       field[3][2] = true;
       break;
     case 5:// I
       field[1][2] = true;
       field[2][2] = true;
       field[3][2] = true;
       field[4][2] = true;
       break;
     case 6:// square
       field[1][1] = true;
       field[1][2] = true;
       field[2][1] = true;
       field[2][2] = true;
       break;
     default:
       field[0][0] = true;
       field[0][1] = true;
       field[0][2] = true;
       field[0][3] = true;
       field[0][4] = true;
       field[1][0] = true;
       field[1][4] = true;
       field[2][0] = true;
       field[2][4] = true;
       field[3][0] = true;
       field[3][4] = true;
       field[4][0] = true;
       field[4][1] = true;
       field[4][2] = true;
       field[4][3] = true;
     }
 
     return field;
   }
 
   @Override
   public void quit() {
     // nothing to do here
 
   }
 
   private void resetButtons() {
     FigT.setScale(1.f);
     FigL1.setScale(1.f);
     FigL2.setScale(1.f);
     FigZ1.setScale(1.f);
     FigZ2.setScale(1.f);
     FigSquare.setScale(1.f);
     FigI.setScale(1.f);
   }
 
   @Override
   public void start() {
 
     FigT = new Rectangle(this.mWidth / 4, 1 * this.mHeight / 8, 100, 100,
         mMainActivity.getVertexBufferObjectManager()) {
       public boolean onAreaTouched(
           org.andengine.input.touch.TouchEvent pSceneTouchEvent,
           float pTouchAreaLocalX, float pTouchAreaLocalY) {
         Log.e("area touched", "yeah, you heard me!!");
         if (pSceneTouchEvent.isActionDown()) {
           resetButtons();
           FigT.setScale(1.4f);
           selected = 0;
         }
         return true;
       };
     };
     FigT.setColor(1.0f, 0.5f, 0.0f);
     mMainActivity.registerTouchArea(FigT);
     this.attachChild(FigT);
 
     FigL1 = new Rectangle(this.mWidth / 4, 3 * this.mHeight / 8, 100, 100,
         mMainActivity.getVertexBufferObjectManager()) {
       public boolean onAreaTouched(
           org.andengine.input.touch.TouchEvent pSceneTouchEvent,
           float pTouchAreaLocalX, float pTouchAreaLocalY) {
         if (pSceneTouchEvent.isActionDown()) {
           resetButtons();
           FigL1.setScale(1.4f);
           selected = 1;
         }
         return true;
       };
     };
     FigL1.setColor(1.0f, 0.0f, 1.0f);
     mMainActivity.registerTouchArea(FigL1);
     this.attachChild(FigL1);
 
     FigL2 = new Rectangle(this.mWidth / 4, 5 * this.mHeight / 8, 100, 100,
         mMainActivity.getVertexBufferObjectManager()) {
       public boolean onAreaTouched(
           org.andengine.input.touch.TouchEvent pSceneTouchEvent,
           float pTouchAreaLocalX, float pTouchAreaLocalY) {
         if (pSceneTouchEvent.isActionDown()) {
           resetButtons();
           FigL2.setScale(1.4f);
           selected = 2;
         }
         return true;
       };
     };
 
     mMainActivity.registerTouchArea(FigL2);
     FigL2.setColor(0.0f, 0.0f, 1.0f);
     this.attachChild(FigL2);
 
     FigZ1 = new Rectangle(this.mWidth / 4, 7 * this.mHeight / 8, 100, 100,
         mMainActivity.getVertexBufferObjectManager()) {
       public boolean onAreaTouched(
           org.andengine.input.touch.TouchEvent pSceneTouchEvent,
           float pTouchAreaLocalX, float pTouchAreaLocalY) {
         if (pSceneTouchEvent.isActionDown()) {
           resetButtons();
           FigZ1.setScale(1.4f);
           selected = 4;
         }
         return true;
       };
     };
 
     mMainActivity.registerTouchArea(FigZ1);
     FigZ1.setColor(0.0f, 1.0f, 1.0f);
     this.attachChild(FigZ1);
 
    FigZ2 = new Rectangle(3 * this.mWidth / 4, 2 * this.mHeight / 8, 100, 100,
         mMainActivity.getVertexBufferObjectManager()) {
       public boolean onAreaTouched(
           org.andengine.input.touch.TouchEvent pSceneTouchEvent,
           float pTouchAreaLocalX, float pTouchAreaLocalY) {
         if (pSceneTouchEvent.isActionDown()) {
           resetButtons();
           FigZ2.setScale(1.4f);
           selected = 5;
         }
         return true;
       };
     };
 
     mMainActivity.registerTouchArea(FigZ2);
     FigZ2.setColor(1.0f, 1.0f, 1.0f);
     this.attachChild(FigZ2);
 
    FigI = new Rectangle(3 * this.mWidth / 4, 4 * this.mHeight / 8, 100, 100,
         mMainActivity.getVertexBufferObjectManager()) {
       public boolean onAreaTouched(
           org.andengine.input.touch.TouchEvent pSceneTouchEvent,
           float pTouchAreaLocalX, float pTouchAreaLocalY) {
         if (pSceneTouchEvent.isActionDown()) {
           resetButtons();
           FigI.setScale(1.4f);
           selected = 6;
         }
         return true;
       };
     };
 
     mMainActivity.registerTouchArea(FigI);
     FigI.setColor(1.0f, 1.0f, 0.0f);
     this.attachChild(FigI);
 
    FigSquare = new Rectangle(3 * this.mWidth / 4, 6 * this.mHeight / 8, 100,
         100, mMainActivity.getVertexBufferObjectManager()) {
       public boolean onAreaTouched(
           org.andengine.input.touch.TouchEvent pSceneTouchEvent,
           float pTouchAreaLocalX, float pTouchAreaLocalY) {
         if (pSceneTouchEvent.isActionDown()) {
           resetButtons();
           FigSquare.setScale(1.4f);
           selected = 7;
         }
         return true;
       };
     };
 
     mMainActivity.registerTouchArea(FigSquare);
     FigSquare.setColor(1.0f, 0.0f, 0.0f);
     this.attachChild(FigSquare);
   }
 }
