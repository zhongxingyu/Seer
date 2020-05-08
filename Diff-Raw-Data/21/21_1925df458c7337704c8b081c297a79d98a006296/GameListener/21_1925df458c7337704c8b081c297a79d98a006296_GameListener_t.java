 package com.imie.morpion.view;
 
 import com.imie.morpion.model.SquareState;
 
 /**
  * @author Marc-Antoine Perennou<Marc-Antoine@Perennou.com>
  */
 
 public interface GameListener {
 
    void onSquaresUpdate(SquareState[][] squares);
 
   void onStateUpdate(int scoreMe, int scoreOther);
 }
