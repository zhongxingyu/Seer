 package org.thedoug.farkle.player;
 
public abstract class AbstractPlayer implements Player {
     @Override
     public String toString() {
         return getClass().getSimpleName();
     }
 }
