 package com.chromabits.ugaacm.Warped;
 
 import com.chromabits.ugaacm.WarpDrive.control.Game;
 import com.chromabits.ugaacm.WarpDrive.control.Screen;
 
 public class Warped implements Game {
 
     @Override
    public void setScreen(Screen screen) {

    }

    @Override
    public Screen getCurrentScreen() {
        return null;
    }

    @Override
     public Screen getInitScreen() {
         return new LoadingScreen(this);
     }
 
 }
