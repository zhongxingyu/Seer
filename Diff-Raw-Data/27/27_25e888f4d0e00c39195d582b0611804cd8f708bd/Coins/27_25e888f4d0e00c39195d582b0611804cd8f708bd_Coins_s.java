 package com.pathfindersdk.coins;
 
 import java.util.ArrayList;
 import java.util.List;
 
 final public class Coins
 {
   private Piece cp = new CopperPiece(0);
   private Piece sp = new SilverPiece(0);
   private Piece gp = new GoldPiece(0);
   private Piece pp = new PlatinumPiece(0);
   
  public Coins add(Piece piece)
   {
     int totalValue = getValue() + piece.getValue();
     
     cp.setNumber(totalValue % 10);
     totalValue /= 10;
     
     sp.setNumber(totalValue % 10);
     totalValue /= 10;
     
     gp.setNumber(totalValue % 10);
     totalValue /= 10;
     
     pp.setNumber(totalValue);
     
     return this;
   }
   
   private int getValue()
   {
     return cp.getValue() + sp.getValue() + gp.getValue() + pp.getValue();
   }
 
   @Override
   public String toString()
   {
     List<String> list = new ArrayList<String>();
     
     if(pp.getNumber() > 0)
       list.add(pp.toString());
     if(gp.getNumber() > 0)
       list.add(gp.toString());
     if(sp.getNumber() > 0)
       list.add(sp.toString());
     if(cp.getNumber() > 0)
       list.add(cp.toString());
     
     if(list.isEmpty())
       return cp.toString();
     else
     {
       String out = list.get(0);
       
       for(int i = 1; i < list.size(); i++)
       {
         out += ", " + list.get(i);
       }
       
       return out;
     }
   }
 }
