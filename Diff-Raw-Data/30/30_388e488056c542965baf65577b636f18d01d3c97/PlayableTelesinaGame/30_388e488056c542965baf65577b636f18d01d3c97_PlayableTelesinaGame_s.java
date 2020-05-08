 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.juhokall.telesina.io;
 
 import com.google.inject.ImplementedBy;
 import com.juhokall.telesina.game.TelesinaGame;
 import com.juhokall.telesina.model.Situation;
 import com.juhokall.telesina.model.TelesinaHand;
 
 /**
  *
  * @author juho
  */
 @ImplementedBy(PlayableTelesinaGameImpl.class)
 public interface PlayableTelesinaGame extends TelesinaGame {
 	public void dealNextStreet();	
 	public int dealHoleCard(int playerNumber);
 	public int[] getPlayersCards(int playerNumber);
 	public TelesinaHand getPlayersHand(int playerNumber);
 	public int getActivePlayer ();
 }
