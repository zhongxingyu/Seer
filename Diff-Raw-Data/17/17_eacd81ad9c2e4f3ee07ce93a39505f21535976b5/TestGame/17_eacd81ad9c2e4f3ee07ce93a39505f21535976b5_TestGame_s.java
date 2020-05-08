 package com.jdrago.game;
 
 import android.content.Context;
 
 public class TestGame extends Game
 {
     public Sprite s_;
 
     public TestGame(Context context)
     {
         super(context);
     }
 
     public boolean initGfx()
     {
         s_ = new Sprite(this)
                .load(R.raw.test)
                 .pos(width() / 2, height() / 2)
                 .rot(35);
         return true;
     }
 
     public void render()
     {
         renderBegin(0.3f, 0.3f, 0.3f);
         prepare2D();
 
         s_.draw();
 
         renderEnd();
     }
 
     public void onTouch(int x, int y, boolean first)
     {
         //if(first)
         {
             s_.pos(x, y);
         }
     }
 }
