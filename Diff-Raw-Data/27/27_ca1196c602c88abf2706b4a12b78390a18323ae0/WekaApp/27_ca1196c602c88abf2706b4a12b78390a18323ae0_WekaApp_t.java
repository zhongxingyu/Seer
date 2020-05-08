 package wekaapp;
 
 import wekaapp.ui.Main;

 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import javax.swing.*;
 
 /**
  *
  * @author sgolebiewski
  * Dont forget about me: Marcin S. Narowski <axxxon.pl@gmail.com>
  */
 public class WekaApp 
 {
     public static void main(String[] args)
     {
         Main main = new Main();
         main.setVisible(true);
         main.show();
     }
 }
