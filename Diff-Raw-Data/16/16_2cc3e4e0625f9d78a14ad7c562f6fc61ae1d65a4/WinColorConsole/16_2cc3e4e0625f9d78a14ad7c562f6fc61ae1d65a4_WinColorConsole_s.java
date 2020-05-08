 package com.piaction.tools.ant.listener;
 
 public interface WinColorConsole
 {
 
   void cls();
 
   void setCursorPosition(int xCoord, int yCoord);
 
   void keepColors();
 
   void restoreColors();
 
   void setColor(int color);
 
 }
