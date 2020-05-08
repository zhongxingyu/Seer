 package ru.spbau.opeykin.drunkard.game;
 
 import ru.spbau.opeykin.drunkard.game.objects.*;
 
 /**
  * User: Alexander Opeykin
  * Date: 3/17/12
  */
 public class RectangularFieldFiller {
     private static final int fieldWidth = 15;
     private static final int fieldHeight = 15;
    private static final int drunkardCreatingPeriod = 20;
     private static final int policemanCreatingLocationX = 15;
     private static final int policemanCreatingLocationY = 4;
     private static final int lampLightRadius = 3;
 
     private static final int drunkardCreatingLocationX = 10;
     private static final int drunkardCreatingLocationY = 1;
 
 
     public static void fill (RectangularBasedField field) {
         if (field.height - 2 != fieldHeight || field.width - 2 != fieldWidth) {
             throw new IllegalArgumentException("wrong field size");
         }
 
         Position [][] positions = field.getAllPositions();
 
         int pY = policemanCreatingLocationY;
         int pX = policemanCreatingLocationX;
 
         positions[4][16].releaseHost(); // destroy wall
         PoliceDepartment policeDepartment = new PoliceDepartment(
                 positions[4][16], positions[pY][pX]);
 
         int dY = drunkardCreatingLocationY;
         int dX = drunkardCreatingLocationX;
 
         positions[0][10].releaseHost(); // destroy wall
         new BarrelHouse(positions[0][10], positions[dY][dX], drunkardCreatingPeriod);
 
         Position polePosition = positions[8][8];
         new Pole(polePosition);
 
         Position lampPosition = positions[4][11];
         new Lamp(lampPosition, lampLightRadius).addListener(policeDepartment);
 
         positions[16][4].releaseHost(); // destroy wall
         new BottleBase(positions[16][4], positions[15][5]);
 
         new Beggar(positions[15][5], positions[16][4], 40);
 
         new Bottle(positions[13][1]);
     }
 }
