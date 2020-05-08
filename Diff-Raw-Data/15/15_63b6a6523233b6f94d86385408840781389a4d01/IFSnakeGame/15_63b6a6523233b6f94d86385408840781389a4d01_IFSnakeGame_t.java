 import java.util.Scanner;
 
 class IFSnakeGame
 {
     public static void main(String [] args)
     {
         System.out.println("You are a snake, and you are hungry for apples.");
         SnakeHead snake = new SnakeHead(0, 0);
         Scanner sc = new Scanner(System.in);
         int roomw = 8;
         int roomh = 8;
         Apple apple = new Apple(roomw, roomh, "snake");
         while(true)
         {
             String cmd = sc.nextLine();
             if(cmd.equalsIgnoreCase("length"))
                 System.out.println("You are " + snake.getLength() + " feet long.");
             else if(cmd.equalsIgnoreCase("forward"))
             {
                 if(snake.move(apple))
                 {
                     System.out.println("Mmm! You eat the apple.");
                     System.out.println("You feel yourself growing longer.");
                     apple.randomize(roomw, roomh);
                 }
                 else
                     System.out.println("You slither forward.");
             }
             else if(cmd.equalsIgnoreCase("left"))
             {
                 snake.turnLeft();
                 if(snake.move(apple))
                 {
                     System.out.println("Mmm! There was an apple to your left!");
                     System.out.println("You feel yourself growing longer.");
                     apple.randomize(roomw, roomh);
                 }
                 else
                     System.out.println("You turn to your left and slither forward a bit.");
             }
             else if(cmd.equalsIgnoreCase("right"))
             {
                 snake.turnRight();
                 if(snake.move(apple))
                 {
                     System.out.println("Mmm! There was an apple to your right!");
                     System.out.println("You feel yourself growing longer.");
                     apple.randomize(roomw, roomh);
                 }
                 else
                     System.out.println("You turn to your right and slither forward a bit.");
             }
             else if(cmd.equalsIgnoreCase("quit"))
             {
                 System.out.println("Goodbye.");
                 return;
             }
             else
             {
                 System.out.println("I don't know that command.");
                 continue;
             }
             if(snake.getX() < 0 || snake.getX() >= roomw || snake.getY() < 0 || snake.getY() >= roomh)
             {
                 System.out.println("Ouch! You ran headfirst into a wall and died.");
                 System.out.println("You were " + snake.getLength() + " feet long when you died.");
                 return;
             }
             if(snake.bitYourself())
             {
                 System.out.println("Ouch! You bit your own tail!");
                 System.out.println("The poison quickly sets in and you die.");
                 System.out.println("You were " + snake.getLength() + " feet long when you died.");
                 return;
             }
             System.out.println(snake.printInfo(roomw, roomh, apple));
             System.out.println(snake.printBoard(roomw, roomh, apple));
         }
     }
 }
 
 class Apple
 {
     protected int x;
     protected int y;
     protected PseudoRandom rand;
     public Apple(int boardx, int boardy, String seed)
     {
         super();
         rand = new PseudoRandom(seed);
         randomize(boardx, boardy);
     }
     public int getX()
         {return x;}
     public int getY()
         {return y;}
     public void randomize(int boardx, int boardy)
     {
         x = rand.randint(0, boardx - 1);
         y = rand.randint(0, boardy - 1);
     }
 }
 
 class PseudoRandom
 {
     protected int counter;
     public PseudoRandom(String seed)
     {
         super();
         seed = seed.toUpperCase();
         int c;
         int i;
         int product = 1;
         counter = 0;
         for(i = 0; i < seed.length(); i++)
         {   // accepts 32 to 95
             c = seed.charAt(i) - 32;
             if(c < 0 || c >= 64)
             {
                 System.out.println("Invalid hash character '" + seed.charAt(i) + "', ignoring...");
                 continue;
             }
             counter += product * c;
             product *= 64;
         }
     }
 
     public int randint(int from, int to)
     {  // inclusive
         int toR = (int)((to - from) * Math.abs(_noise(counter))) + from;
         counter++;
         return toR;
     }
 
     public double _noise(int x)
     {
         x = (x<<13) ^ x;
         return (1.0 - ( (x * (x * x * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0);
     }
 }
 
 class SnakeBody
 {
     protected SnakeBody rest;
     protected int x;
     protected int y;
     public SnakeBody(int x, int y, SnakeBody rest)
     {
         super();
         this.x = x;
         this.y = y;
         this.rest = rest;
     }
     public int getLength()
     {
         if(rest == null)
             return 1;
         return 1 + rest.getLength();
     }
     public int getX()
         {return x;}
     public int getY()
         {return y;}
 }
 
 class SnakeHead extends SnakeBody
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
         int applex = apple.getX();
         int appley = apple.getY();
         _move();  // guarantees that you grew a guy
         if(x == applex && y == appley)
         {   // congrats
             length++;
             return true;
         }
         SnakeBody sn = rest;
         if(sn.rest == null)
         {
             rest = null;  // you have no body
             return false;
         }
         for(; sn.rest.rest != null; sn = sn.rest);  // tricky
         sn.rest = null;
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
         SnakeBody bittest = rest;
         for(; bittest != null; bittest = bittest.rest)
             if(bittest.x == x && bittest.y == y)
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
 
     public String printInfo(int width, int height, Apple apple)
     {
         SnakeHead head = this;
         int appleX = apple.getX();
         int appleY = apple.getY();
         int direction = head.getFacing();
         int diffX = head.getX() - appleX;
         int diffY = head.getY() - appleY;  // positive: head is below apple
         int newDiffX, newDiffY;
         int minDiff = width+height;
         String toR = "";
         SnakeBody sn;
         switch(direction)
         {
             case 1: // up
                 if(diffY > 0 && Math.abs(diffY) > Math.abs(diffX))
                 {
                     if(diffX == 0)
                         toR += "You see an apple " + Math.abs(diffY) + " feet straight ahead!\n";
                     else if(diffX < 0)
                         toR += "You see an apple " + Math.abs(diffY) + " feet ahead and to your right!\n";
                     else
                         toR += "You see an apple " + Math.abs(diffY) + " ahead and to your left!\n";
                 }
                 sn = head;
                 for(;sn != null; sn = sn.rest)
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
             case 2: // right ======================================================
                 if(diffX < 0 && Math.abs(diffX) > Math.abs(diffY))
                 {
                     if(diffY == 0)
                         toR += "You see an apple " + Math.abs(diffX) + " feet straight ahead!\n";
                    else if(diffY > 0)
                        toR += "You see an apple " + Math.abs(diffX) + " feet ahead and to your left!\n";
                     else
                        toR += "You see an apple " + Math.abs(diffX) + " ahead and to your right!\n";
                 }
                 sn = head;
 
                 for(;sn != null; sn = sn.rest)
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
             case 4: // down =======================================================
                 if(diffY < 0 && Math.abs(diffY) > Math.abs(diffX))
                 {
                     if(diffX == 0)
                         toR += "You see an apple " + Math.abs(diffY) + " feet straight ahead!\n";
                     else if(diffX < 0)
                         toR += "You see an apple " + Math.abs(diffY) + " feet ahead and to your left!\n";
                     else
                         toR += "You see an apple " + Math.abs(diffY) + " ahead and to your right!\n";
                 }
                 sn = head;
                 for(;sn != null; sn = sn.rest)
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
             case 8: // left
                 if(diffX > 0 && Math.abs(diffX) > Math.abs(diffY))
                 {
                     if(diffY == 0)
                         toR += "You see an apple " + Math.abs(diffX) + " feet straight ahead!\n";
                    else if(diffY > 0)
                        toR += "You see an apple " + Math.abs(diffX) + " feet ahead and to your right!\n";
                     else
                        toR += "You see an apple " + Math.abs(diffX) + " ahead and to your left!\n";
                 }
                 sn = head;
                 for(;sn != null; sn = sn.rest)
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
         String toR = "";
         int i;
         int j;
         int applex = apple.getX();
         int appley = apple.getY();
         SnakeBody sn = rest;
         for(i=0; i<boardy; i++)
         {
             for(j=0; j<boardx; j++)
                 if(i == appley && j == applex)
                     toR += "a";
                 else
                     toR += ".";
             toR += "\n";
         }
         int replace = y * (boardx + 1) + x;
         toR = toR.substring(0, replace) + "O" + toR.substring(replace + 1);
         for(; sn != null; sn = sn.rest)
         {
             replace = sn.y * (boardx + 1) + sn.x;
             toR = toR.substring(0, replace) + "#" + toR.substring(replace + 1);
         }
         return toR;
     }
 }
