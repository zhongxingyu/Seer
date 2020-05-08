import joos.lib.*;
 
 public class SnakeHead extends SnakeBody
 {
     protected int length;
     protected int facing;  // 1^ 2> 4v 8<
     public SnakeHead(int x, int y)
     {
         super(x, y, null);
         facing = 2;
         length = 1;
     }
     public int getFacing()
         {return facing;}
     public int getLength()
         {return length;}
     public boolean move(Apple apple)
     {   // returns if you ate an apple
         int applex;
         int appley;
         SnakeBody sn;
         applex = apple.getX();
         appley = apple.getY();
         this._move();  // guarantees that you grew a guy
         if(x == applex && y == appley)
         {   // congrats
             length++;
             return true;
         }
         sn = rest;
         if(sn.getRest() == null)
         {
             rest = null;  // you have no body
             return false;
         }
         for(; sn.getRest().getRest() != null; sn = sn.getRest());  // tricky
         sn.setRest(null);
         return false;
     }
     public void _move()
     {   // makes you longer
         rest = new SnakeBody(x, y, rest);
         if(facing == 1)
             y--;
         else if(facing == 2)
             x++;
         else if(facing == 4)
             y++;
         else if(facing == 8)
             x--;
     }
     public boolean bitYourself()
     {
         SnakeBody bittest;
         bittest = rest;
         for(; bittest != null; bittest = bittest.getRest())
             if(bittest.getX() == x && bittest.getY() == y)
                 return true;
         return false;
     }
     public void turnLeft()
     {
         facing /= 2;
         if(facing == 0)
             facing = 8;
     }
     public void turnRight()
     {
         facing *= 2;
         if(facing == 16)
             facing = 1;
     }
 
     public int abs(int value)
     {
         if(value < 0)
             return -value;
         return value;
     }
 
     public String printInfo(int width, int height, Apple apple)
     {
         SnakeHead head;
         int appleX;
         int appleY;
         int direction;
         int diffX;
         int diffY;
         int newDiffX, newDiffY;
         int minDiff;
         String toR;
         SnakeBody sn;
 
         head = this;
         appleX = apple.getX();
         appleY = apple.getY();
         direction = head.getFacing();
         diffX = head.getX() - appleX;
         diffY = head.getY() - appleY;  // positive: head is below apple
         minDiff = width+height;
         toR = "";
         if(direction == 1)
         {
             if(diffY > 0 && this.abs(diffY) > this.abs(diffX))
             {
                 if(diffX == 0)
                     toR += "You see an apple " + this.abs(diffY) + " feet straight ahead!\n";
                 else if(diffX < 0)
                     toR += "You see an apple " + this.abs(diffY) + " feet ahead and to your right!\n";
                 else
                     toR += "You see an apple " + this.abs(diffY) + " ahead and to your left!\n";
             }
             sn = head;
             for(; sn != null; sn = sn.getRest())
             {
                 newDiffX = head.getX() - sn.getX();
                 newDiffY = head.getY() - sn.getY();
                 if(newDiffX == 0 && newDiffY > 0)
                 {
                     if(newDiffY < minDiff)
                         minDiff = newDiffY;
                 }
             }
             if(minDiff != width+height)
                 toR += "You see your own body " + minDiff + " feet in front of you!";
             if(toR == "")
                 toR = "There's a wall " + (head.getY()+1) + " feet in front of you.";
             return toR;
         }
         else if(direction == 2) // right ======================================================
         {
             if(diffX < 0 && this.abs(diffX) > this.abs(diffY))
             {
                 if(diffY == 0)
                     toR += "You see an apple " + this.abs(diffX) + " feet straight ahead!\n";
                 else if(diffY > 0)
                     toR += "You see an apple " + this.abs(diffX) + " feet ahead and to your left!\n";
                 else
                     toR += "You see an apple " + this.abs(diffX) + " ahead and to your right!\n";
             }
             sn = head;
 
             for(;sn != null; sn = sn.getRest())
             {
                 newDiffX = head.getX() - sn.getX();
                 newDiffY = head.getY() - sn.getY();
                 if(newDiffY == 0 && newDiffX < 0)
                 {
                     if((-1*newDiffX) < minDiff)
                         minDiff = (-1*newDiffX);
                 }
             }
             if(minDiff != width+height)
                 toR += "You see your own body " + minDiff + " feet in front of you!";
             if(toR == "")
                 toR = "There's a wall " + (width-head.getX()) + " feet in front of you.";
             return toR;
         }
         else if(direction == 4) // down =======================================================
         {
             if(diffY < 0 && this.abs(diffY) > this.abs(diffX))
             {
                 if(diffX == 0)
                     toR += "You see an apple " + this.abs(diffY) + " feet straight ahead!\n";
                 else if(diffX < 0)
                     toR += "You see an apple " + this.abs(diffY) + " feet ahead and to your left!\n";
                 else
                     toR += "You see an apple " + this.abs(diffY) + " ahead and to your right!\n";
             }
             sn = head;
             for(;sn != null; sn = sn.getRest())
             {
                 newDiffX = head.getX() - sn.getX();
                 newDiffY = head.getY() - sn.getY();
                 if(newDiffX == 0 && newDiffY < 0)
                 {
                     if((-1*newDiffY) < minDiff)
                         minDiff = (-1*newDiffY);
                 }
             }
             if(minDiff != width+height)
                 toR += "You see your own body " + minDiff + " feet in front of you!";
             if(toR == "")
                 toR = "There's a wall " + (height - head.getY()) + " feet in front of you.";
             return toR;
         }
         else if(direction == 8) // left
         {
             if(diffX > 0 && this.abs(diffX) > this.abs(diffY))
             {
                 if(diffY == 0)
                     toR += "You see an apple " + this.abs(diffX) + " feet straight ahead!\n";
                 else if(diffY > 0)
                     toR += "You see an apple " + this.abs(diffX) + " feet ahead and to your right!\n";
                 else
                     toR += "You see an apple " + this.abs(diffX) + " ahead and to your left!\n";
             }
             sn = head;
             for(; sn != null; sn = sn.getRest())
             {
                 newDiffX = head.getX() - sn.getX();
                 newDiffY = head.getY() - sn.getY();
                 if(newDiffY == 0 && newDiffX > 0)
                 {
                     if(newDiffX < minDiff)
                         minDiff = newDiffX;
                 }
             }
             if(minDiff != width+height)
                 toR += "You see your own body " + minDiff + " feet in front of you!";
             if(toR == "")
                 toR = "There's a wall " + (head.getX()+1) + " feet in front of you.";
             return toR;
         }
         return "";
     }
 
     public String printBoard(int boardx, int boardy, Apple apple)
     {
         String toR;
         int i;
         int j;
         int applex;
         int appley;
         SnakeBody sn;
         int replace;
         toR = "";
         applex = apple.getX();
         appley = apple.getY();
         sn = rest;
         for(i=0; i<boardy; i++)
         {
             for(j=0; j<boardx; j++)
                 if(i == appley && j == applex)
                     toR += "a";
                 else
                     toR += ".";
             toR += "\n";
         }
         replace = y * (boardx + 1) + x;
         toR = toR.substring(0, replace) + "O" + toR.substring(replace + 1, toR.length());
         for(; sn != null; sn = sn.getRest())
         {
             replace = sn.getY() * (boardx + 1) + sn.getX();
             toR = toR.substring(0, replace) + "#" + toR.substring(replace + 1, toR.length());
         }
         return toR;
     }
 }
