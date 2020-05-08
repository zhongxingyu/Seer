 // Written by Lucas Neves Martins - Snowniak - v0.0.1.10 - Some Rigths Reserved  - 2008 - Date: 5/11/2008 17:57:42
 // You can use this code not comercially without authorization, but I will be glad to authorize commercial use for free too.
 // * This code will soon be under BSD license, but not yet. (cosider it as a Creative Commons for now)
 
 import javax.swing.*;
 
 // The code is not so pretty because I've lost the sources once, so I decompiled it... :D
 
 public class LabelDemo
 {
 
     public LabelDemo()
     {
     }
 
     public static void main(String args[])
     {
        ImageIcon labelIcon = new ImageIcon("Img7.png");
         JLabel southLabel = new JLabel(labelIcon);
         JFrame application = new JFrame();
         application.setDefaultCloseOperation(3);
         application.add(southLabel, "South");
         application.setSize(160, 154);
         application.setLocation(500, 300);
         application.setVisible(true);
     }
 }
