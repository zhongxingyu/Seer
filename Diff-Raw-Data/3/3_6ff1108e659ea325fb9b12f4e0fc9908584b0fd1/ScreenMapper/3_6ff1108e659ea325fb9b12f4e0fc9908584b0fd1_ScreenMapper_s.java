 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.xml.verifier;
 
 import java.awt.Container;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.List;
 import java.awt.MediaTracker;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 
 /**
  *
  * @author devadas
  */
 public class ScreenMapper extends Container
 {
     ArrayList gadgetMap;
 
     public void displayScreen(Object obj)
     {
         System.out.println("setting mapper obj");
 
         this.gadgetMap = (ArrayList) obj;
         System.out.println("On setting....gadgetMap = " + gadgetMap);
         Collections.sort(gadgetMap);
         System.out.println("after sort....gadgetMap = " + gadgetMap);
 
 //        repaint();
 //        repaint(.getBounds());
     }
 
     @Override
     public void paint(Graphics g)
     {
         super.paint(g);
         ArrayList listOfStringLocations = new ArrayList();
         Iterator iterator = gadgetMap.iterator();
         while (iterator.hasNext())
         {
             GadgetDisplayElement gadgetElement = (GadgetDisplayElement) iterator.next();
             g.setColor(GadgetConfig.getGadgetColor(gadgetElement.getGadgetType()));
             g.drawRect(gadgetElement.getBounds().x, gadgetElement.getBounds().y,
                     gadgetElement.getBounds().width, gadgetElement.getBounds().height);
             if (null != gadgetElement.getBackgroundImagePath())
             {
                 Image image = gadgetElement.getBackgroundImage();
                 if (null != image)
                 {
                     g.drawImage(image, gadgetElement.getBounds().x, gadgetElement.getBounds().y,
                             gadgetElement.getBounds().width, gadgetElement.getBounds().height, this);
                 }
             }
 
             int stringXPos = gadgetElement.getBounds().x + 20;
             int stringYPos = gadgetElement.getBounds().y;
 //            Point stringLocation = new Point (stringXPos, stringYPos);
             int index = 1;
             Point stringLocation;
             while (true)
             {
                 stringLocation = new Point(stringXPos, stringYPos + (index * 20))
                 {
                     /**
                      * Make sure strings are not drawn together
                      */
                     public boolean equals(Object obj)
                     {
                         if (obj instanceof Point)
                         {
                             Point pointToCompare = (Point) obj;
                             int verticalDifference = this.y - pointToCompare.y;
                            if (verticalDifference < 19 && verticalDifference > -19)
                             {
                                 return true;
                             }
                             else
                             {
                                 return false;
                             }
                         }
                         return false;
                     }
                 };
                 if (!listOfStringLocations.contains(stringLocation))
                 {
                     listOfStringLocations.add(stringLocation);
                     break;
                 }
                 index++;
             }
 
             g.drawString(gadgetElement.getGadgetName(), stringLocation.x, stringLocation.y);
         }
     }
 }
