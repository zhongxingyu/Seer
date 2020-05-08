 package com.icbat.game.tradesong.stages;
 
 import com.badlogic.gdx.scenes.scene2d.Stage;
 
 public abstract class AbstractStage extends Stage {
     public abstract void layout();
 
     @Override
     public String toString() {
         return this.getClass().getSimpleName();
     }

    /**
     * Universal handle for things that must happen on parent screen's hide. Defaults to nothing.
     * */
    public void onHide() {}
 }
