 package org.thedoug.farkle.player;
 
abstract class AbstractPlayer implements Player {
     @Override
     public String toString() {
         return getClass().getSimpleName();
     }
 }
