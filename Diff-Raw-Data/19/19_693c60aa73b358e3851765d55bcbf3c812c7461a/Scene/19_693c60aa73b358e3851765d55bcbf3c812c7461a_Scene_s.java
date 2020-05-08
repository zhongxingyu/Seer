 package com.android.game.IG;
 
 import android.graphics.Typeface;
 import android.util.Log;
 
 import com.android.object.drawable.BaseDrawableObject;
 import com.android.opengl.BaseGLSurfaceView;
 import com.android.opengl.texture.BitmapTextureArrayHolder;
 import com.android.opengl.texture.BitmapTextureHolder;
 import com.android.opengl.texture.text.StringTextureHolder;
 import com.android.utils.FPS;
 import com.android.utils.FormatFloat;
 
 public class Scene extends BaseDrawableObject {
 
     private StringTextureHolder textTexture;
     private BitmapTextureHolder bitmapTexture;
     private BitmapTextureHolder bitmapTexture2;
     private BitmapTextureHolder zooTexture;
     private BitmapTextureArrayHolder bitmapTexture3;
     private BitmapTextureArrayHolder bitmapTexture4;
 
     private final FPS myFPS = new FPS();
 
     @Override
     public void onDraw() {
         // Log.d("Scene", "draw");
         myFPS.tick();
         bitmapTexture.draw(100, 50, 100.0f, 100.0f);
 
         bitmapTexture.draw(0, 0, 400.0f, 100.0f);
 
         textTexture.draw(300, 50);
 
         textTexture.setText(FormatFloat.toString3(myFPS.getFPS()))
                 .updateBitmap();
         textTexture.draw(300, 100, 2f);
 
         bitmapTexture2.draw(100, 100, 356, 247);
 
         bitmapTexture.draw(400, 100, 100.0f, 100.0f);
 
         bitmapTexture.draw(500, 200, 100.0f, 100.0f);
 
         zooTexture.draw(300, 200, 200, 200);
         zooTexture.draw(400, 150, 200, 200);
 
         bitmapTexture3.index(8).draw(200, 200, 100, 100);
 
        // bitmapTexture4.index(0).draw(300, 0, 400, 400);
 
         bitmapTexture.draw(600, 300, 100.0f, 100.0f);
     }
 
     @Override
     public void initDrawable() {
         Log.d("Scene", "initTexture");
 
         textTexture = new StringTextureHolder(myView.mRenderer, "abogt", 64,
                 Typeface.DEFAULT, 128, 0, 255, 0);
         bitmapTexture = new BitmapTextureHolder(myView.mRenderer,
                 myView.myContext, R.drawable.smilebox);
         bitmapTexture2 = new BitmapTextureHolder(myView.mRenderer,
                myView.myContext, R.drawable.pic3);
         zooTexture = new BitmapTextureHolder(myView.mRenderer,
                 myView.myContext, R.drawable.zoo);
 
         bitmapTexture3 = new BitmapTextureArrayHolder(myView.mRenderer,
                 myView.myContext, R.drawable.streetsand160, 5, 19);
         bitmapTexture4 = new BitmapTextureArrayHolder(myView.mRenderer,
                myView.myContext, R.drawable.streetsand160, 2, 4);
 
     }
 
     public Scene(BaseGLSurfaceView pView) {
         super(pView);
     }
 }
