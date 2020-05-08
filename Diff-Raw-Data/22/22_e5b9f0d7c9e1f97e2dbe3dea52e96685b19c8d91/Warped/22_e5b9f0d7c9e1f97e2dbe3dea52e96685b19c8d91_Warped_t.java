 package com.chromabits.ugaacm.Warped;
 
 import com.chromabits.ugaacm.WarpDrive.control.Game;
 import com.chromabits.ugaacm.WarpDrive.control.Screen;
 
 public class Warped implements Game {
 
    private Screen curScreen;

     @Override
     public void setScreen(Screen screen) {
        this.curScreen = screen;
     }
 
     @Override
     public Screen getCurrentScreen() {
        return this.curScreen;
     }
 
     @Override
     public Screen getInitScreen() {
         return new LoadingScreen(this);
     }
 
 }
