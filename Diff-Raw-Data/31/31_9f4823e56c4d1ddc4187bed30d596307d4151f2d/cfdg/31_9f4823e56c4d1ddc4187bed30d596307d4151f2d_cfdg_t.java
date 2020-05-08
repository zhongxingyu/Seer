 /**
  *
  * The purpose of this library is to allow the pixellation of images in
  * processing Copyright (C) 2012 Martin Prout This library is free software; you
  * can redistribute it and/or modify it under the terms of the GNU Lesser
  * General Public License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * Obtain a copy of the license at http://www.gnu.org/licenses/lgpl-2.1.html
  */
 package pde2cfdg;
 
 import processing.core.*;
 
 /**
  *
  * @author Martin Prout
  */
 public class cfdg extends PApplet {
 
     ProcessingToCF cfdg;
     String input;
     int dotSize = 3;
 
     /**
      * Processing setup
      */
     @Override
     public void setup() {
         size(1280, 1024);
         background(255);
         colorMode(HSB, 1.0f);
         cfdg = new ProcessingToCF(this);
         cfdg.setDotSize(dotSize);
        cfdg.setPathToCFDG("/home/tux/CF3/cfdg");
         cfdg.posterize(2);
        cfdg.getInput();
         cfdg.writeCFDG();
     }
 
     /**
      * Processing draw loop
      */
     @Override
     public void draw() {
     }
 
     /**
     * Reset dot size
      */
     @Override
     public void keyReleased() {
         switch (key) {
             case 'r':
                if (cfdg.ready()) {
                    cfdg.setDotSize(dotSize);
                    cfdg.writeCFDG();
                    break;
                }
             case '1':
             case '2':
             case '3':
             case '4':
             case '5':
             case '6':
             case '7':
             case '8':
             case '9':
                 dotSize = key - 48;
                 System.out.println(dotSize);
                 break;
         }
     }
 
     /**
      *
      * @param args
      */
     public static void main(String args[]) {
         PApplet.main(new String[]{"pde2cfdg.cfdg"});
     }
 }
