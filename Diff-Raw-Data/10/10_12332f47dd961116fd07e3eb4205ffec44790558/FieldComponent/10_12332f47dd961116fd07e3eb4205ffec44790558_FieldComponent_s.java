 package ru.nsu.vakhrushev.pusher.viewer;
 
 import ru.nsu.vakhrushev.pusher.model.Model;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Vakhrushev Maxim
  * Date: 09.04.13
  * Time: 22:46
  */
 
 public class FieldComponent extends JPanel {
 
     private Model model = null;
     private int frameWidth = 0;
     private int frameHeight = 0;
     private BufferedImage imgBox ;
     private BufferedImage imgHero;
     private BufferedImage imgBorder;
     private BufferedImage imgPlace;
     private BufferedImage imgFloor;
 
     public FieldComponent(Model newModel, Dimension d)
     {
         try
         {
             imgBox = ImageIO.read(new File("images/cube2.jpg"));
             imgHero = ImageIO.read(new File("images/duck2.jpg"));
             imgBorder = ImageIO.read(new File("images/wall.jpg"));
             imgPlace = ImageIO.read(new File("images/place2.jpg"));
             imgFloor = ImageIO.read(new File("images/floor.jpg"));
             frameWidth = d.width;
             frameHeight = d.height;
             model = newModel;
         }
         catch (IOException e)
         {
             e.printStackTrace();
             System.err.println("Error while loading image");
             System.exit(1);
         }
     }
 
     public void paintComponent (Graphics g)
     {
 
         int imgWidth = frameWidth / model.getWidth();
         int imgHeight = frameHeight / model.getHeight();
 
         for (int i = 0; i < model.getHeight(); i++)
         {
             for (int j = 0; j < model.getWidth(); j++)
             {
                 if (model.isBorder(i, j))
                 {
                     g.drawImage(imgBorder, j * imgWidth, i * imgHeight, imgWidth, imgHeight, this);
                 }
                 else if (model.isFloor(i, j) && !model.isPlace(i, j))
                 {
                     g.drawImage(imgFloor, j * imgWidth, i * imgHeight, imgWidth, imgHeight, this);
                 }
                 else if (model.isPlace(i, j) && !model.isBox(i, j) && !model.isHero(i, j))
                 {
                     g.drawImage(imgPlace, j * imgWidth, i * imgHeight, imgWidth, imgHeight, this);
                 }
                 else if (model.isBox(i, j))
                 {
                     g.drawImage(imgBox, j * imgWidth, i * imgHeight, imgWidth, imgHeight, this);
                 }
                else if (model.isHero(i, j))
                 {
                     g.drawImage(imgHero, j * imgWidth, i * imgHeight, imgWidth, imgHeight, this);
                 }
             }
         }
     }
 }
