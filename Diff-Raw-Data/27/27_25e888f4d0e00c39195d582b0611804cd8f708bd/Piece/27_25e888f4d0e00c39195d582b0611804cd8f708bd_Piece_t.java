 package com.pathfindersdk.coins;
 
import com.pathfindersdk.utils.ArgChecker;

 public abstract class Piece
 {
   private int number;
   
   public Piece(int number)
   {
     setNumber(number);
   }
   
   public int getNumber()
   {
     return number;
   }
   
   public void setNumber(int number)
   {
    ArgChecker.checkIsPositive(number);
     
     this.number = number;
   }
   
   public Piece add(int number)
   {
     this.number += number;
     return this;
   }
   
   public abstract int getValue();
   
 }
