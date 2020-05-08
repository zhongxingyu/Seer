 package com.quiz.flag;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 /**
 *
 * Custom ImageView class for drawing bitmap on images
 * @author Muammil
 */
 
 public class CustomImageView extends ImageView {
   private static Bitmap thumpsUp;
   private static Bitmap thumpsDown;
   private boolean isSelected;
   private boolean isCorrect;
   private float xPos;
   private float yPos;
   private final SoundPool soundPool;
  private final int correct;
  private final int wrong;
 
   public CustomImageView(Context context, AttributeSet attrs) {
     super(context, attrs);
     thumpsUp = BitmapFactory.decodeResource(getResources(), R.drawable.thumps_up_imageview);
     thumpsDown = BitmapFactory.decodeResource(getResources(), R.drawable.thumps_down_imageview);
     soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
     correct = soundPool.load(getContext(), R.raw.right, 1);
     wrong = soundPool.load(getContext(), R.raw.fail, 0);
   }
 
   @Override
   protected void onDraw(Canvas canvas) {
     super.onDraw(canvas);
     if(isSelected) {
       if(isCorrect) {
         canvas.drawBitmap(thumpsUp, xPos/2 - thumpsUp.getWidth()/2, yPos/2 - thumpsUp.getHeight()/2, 
             null);
         soundPool.play(correct, 1, 1, 0, 0, 1);
       } else {
         canvas.drawBitmap(thumpsDown, xPos/2 - thumpsDown.getWidth()/2, yPos/2 - thumpsDown.getHeight()/2, 
             null);
         soundPool.play(wrong, 1, 1, 0, 0, 1);
       }
     }
   }
 
   public void onClickImage(boolean isCorrect, float xPos, float yPos) {
     isSelected = true;
     this.isCorrect = isCorrect;
     this.xPos = xPos;
     this.yPos = yPos;
     invalidate();
   }
 
   public void clearSelection() {
     isSelected = false;
   }
 }
