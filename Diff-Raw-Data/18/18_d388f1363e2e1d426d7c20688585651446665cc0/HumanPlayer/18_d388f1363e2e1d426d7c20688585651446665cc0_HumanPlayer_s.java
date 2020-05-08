 /***************************************************************************
  *                                                                         *
  *   This program is free software; you can redistribute it and/or modify  *
  *   it under the terms of the GNU General Public License as published by  *
  *   the Free Software Foundation; either version 2 of the License, or     *
  *   (at your option) any later version.                                   *
  *                                                                         *
  ***************************************************************************
  *
  * HumanPlayer.java
  *
  * Created on 28. September 2004, 17:19
  */
 
 import java.awt.Color;
 
 /** for humanplayer 
  * @see Chronos for chronical-order-implementation
  *
  * @author  peter
  */
 public class HumanPlayer extends Player
 {
     
     /** Creates a new instance of HumanPlayer */
     public HumanPlayer(Color color, String namePlayer,int no)
     { super(Const.HUMAN_PLAYER,color, namePlayer, no,true);
     }    
     
     public Path moves()
     {  EbbeFlut.chronical.newTurn(EbbeFlut.chronical.fromBeginning());
      
     while(!EbbeFlut.chronical.isTurnFinished() && (!EbbeFlut.chronical.isGameFinished() || EbbeFlut.chronical.lastMoves()) );          
 
        return path;
     }
     
 }
