 package com.pathfindersdk.coins;
 
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
    if(number < 0)
      throw new IllegalArgumentException("number can't be negative");
     
     this.number = number;
   }
   
   public Piece add(int number)
   {
     this.number += number;
     return this;
   }
   
   public abstract int getValue();
   
 }
