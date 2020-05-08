 package org.amoeba.examples.inputexample;
 
 import org.amoeba.activity.GameActivity;
 import org.amoeba.engine.service.input.InputEvent;
 import org.amoeba.entity.sprite.TextSprite;
 import org.amoeba.graphics.camera.Camera;
 import org.amoeba.graphics.texture.TextOptions;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.graphics.Paint.Align;
 import android.graphics.Typeface;
 import android.media.MediaPlayer;
 import android.opengl.GLES20;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class InputExample extends GameActivity
 {
 	private static final String TAG = "AmoebaEngine.InputExample";
 
     private List<TextSprite> information;
     private TextSprite eventList;
 
     @Override
     public void onCreate(final Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         information = new ArrayList<TextSprite>();
 
        TextOptions informationOptions = new TextOptions(24, Color.BLACK, Align.CENTER, Typeface.DEFAULT, true);
         information.add(getGraphicsService().getTextFactory().createTextSprite(
             "Touch the screen to see data", informationOptions));
         information.add(getGraphicsService().getTextFactory().createTextSprite(
             "on your inputs. Use the HOME", informationOptions));
         information.add(getGraphicsService().getTextFactory().createTextSprite(
             "key to exit the application.", informationOptions));
 
        TextOptions eventListOptions = new TextOptions(36, Color.BLACK, Align.CENTER, Typeface.DEFAULT, true);
         eventList = getGraphicsService().getTextFactory().createTextSprite("Some Event", eventListOptions);
     }
 
     @Override
     public void onSurfaceChanged(final int width, final int height)
     {
        GLES20.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);

         int informationSpriteHeight = 10;
         for (TextSprite informationText : information) {
             informationText.setPosition(width/2, informationSpriteHeight);
            informationSpriteHeight += informationText.getHeight();
         }
         
        eventList.setPosition(width/2, height/4);
     }
 
     @Override
     public void onDraw(final Camera camera)
     {
         super.onDraw(camera);
     }
 
     @Override
 	public void onInputEvent(final InputEvent event)
 	{
 
 	}
 
     @Override
     public void onBackPressed() {
         MediaPlayer smbCoinSound = MediaPlayer.create(this, R.raw.smw_coin);
         smbCoinSound.start();
     }
    
 }
