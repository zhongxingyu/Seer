 import java.awt.Image;
 import javax.swing.ImageIcon;
 
 
 public class Block
 {
   //variables needed for the identification fase
   private String Method;
   private int MethodType, MethodId;
  //variables needed for the implementation fase
   private ImageIcon blockImage;
   private int nextMethodId;
   private double posX, posY;
   
   
   //*******************
   //Constructor
   //**********************  
   public Block(int requiredId, String requiredMethod, int requiredMethodType, 
                double requiredPosX, double requiredPosY, String imagePath)
   {
     MethodId = requiredId;
     Method = requiredMethod;
     MethodType = requiredMethodType;
     posX = requiredPosX;
     posY = requiredPosY;
     blockImage = new ImageIcon(this.getClass().getResource("block.png"));
   }
   
   //*************************
   //Setters and getters
   //*******************************
   public String toString()
   {
     return Method;
   }
   
   public int getType()
   {
     return MethodType;
   }
   
   public void setNextMethod(int requiredMethodId)
   {
     nextMethodId = requiredMethodId;
   }
   
   public int getNext()
   {
     return nextMethodId;
   }
   
   
 
 }
   
