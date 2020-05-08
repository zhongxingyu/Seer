 package h2ointeractions;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Random;
 
 /**
  * @author Shawn Tyler Schwartz
  * @class Period 5, Honors Chemistry 10B
  * @school High Tech Los Angeles
  * @advisors Wun Chiou & Daniel Perahya
  * @fileName "H2OInteractions.java" (Main Class)
  * @version 1.0 - 2013 LA County Science Fair Edition
  **/
 
 public class H2OInteractions {
     
     /* REACTION MAP:
      * Hydrogen-Hydrogen Bonds:
      *  1. leftHydrogenOnex1y1 --> leftHydrogenTwox2y2
      *  2. leftHydrogenOnex1y1 --> rightHydrogenTwox2y2
      *  3. rightHydrogenOnex1y1 --> leftHydrogenTwox2y2
      *  4. rightHydrogenOnex1y1 --> rightHydrogenTwox2y2
      * 
      * Oxygen-Oxygen Bond:
      *  5. oxygenOnex1y1 --> oxygenTwox2y2
      * 
      * Hydrogen-Oxygen Bonds:
      *  6. leftHydrogenOnex1y1 --> oxygenOnex1y1
      *  7. rightHydrogenOnex1y1 --> oxygenTwox2y2
      *  8. leftHydrogenTwox2y2 --> oxygenOnex1y1
      *  9. rightHydrogenTwox2y2 --> oxygenOnex1y1
      */
         
     //Coordinate Arrays
     public static double initialLeftHydrogenOneCoord[];
     public static double initialRightHydrogenOneCoord[];
     public static double initialOxygenOneCoord[];
     public static double initialOxygenTwoCoord[];
     public static double initialLeftHydrogenTwoCoord[];
     public static double initialRightHydrogenTwoCoord[];
     public static double finalTranslatedArrayOne[][];
     public static double finalTranslatedArrayTwo[][];
     
     //Variables for Calculations
     public static double pointChargeEnergy;
     public static double totalPointChargeEnergy;
     
     //Iteration Energy Storing Variables
     public static double firstCalcIteration; //leftHydrogenOnex1y1 --> leftHydrogenTwox2y2
     public static double secondCalcIteration; //leftHydrogenOnex1y1 --> rightHydrogenTwox2y2
     public static double thirdCalcIteration; //rightHydrogenOnex1y1 --> leftHydrogenTwox2y2
     public static double fourthCalcIterartion; //rightHydrogenOnex1y1 --> rightHydrogenTwox2y2
     public static double fifthCalcIteration; //oxygenOnex1y1 --> oxygenTwox2y2
     public static double sixthCalcIteration; //leftHydrogenOnex1y1 --> oxygenOnex1y1
     public static double seventhCalcIteration; //rightHydrogenOnex1y1 --> oxygenTwox2y2
     public static double eighthCalcIteration; //leftHydrogenTwox2y2 --> oxygenOnex1y1
     public static double ninthCalcIteration; //rightHydrogenTwox2y2 --> oxygenOnex1y1
     
     //Rotated Molecule Coordinate Arrays
     public static double finalRotatedXArrayOne[][];
     public static double finalRotatedYArrayOne[][];
     public static double finalRotatedZArrayOne[][];
     public static double finalRotatedXArrayTwo[][];
     public static double finalRotatedYArrayTwo[][];
     public static double finalRotatedZArrayTwo[][];
     
     //Rotated Molecule Variables to Store Coordinate Array Values
         //Molecule One
         public static double finalRotLeftHydrogenOneXPos;
         public static double finalRotLeftHydrogenOneYPos;
         public static double finalRotLeftHydrogenOneZPos;
         public static double finalRotOxygenOneXPos;
         public static double finalRotOxygenOneYPos;
         public static double finalRotOxygenOneZPos;
         public static double finalRotRightHydrogenOneXPos;
         public static double finalRotRightHydrogenOneYPos;
         public static double finalRotRighyHydrogenOneZPos;
         
         //Molecule Two
         public static double finalRotLeftHydrogenTwoXPos;
         public static double finalRotLeftHydrogenTwoYPos;
         public static double finalRotLeftHydrogenTwoZPos;
         public static double finalRotOxygenTwoXPos;
         public static double finalRotOxygenTwoYPos;
         public static double finalRotOxygenTwoZPos;
         public static double finalRotRightHydrogenTwoXPos;
         public static double finalRotRightHydrogenTwoYPos;
         public static double finalRotRightHydrogenTwoZPos;
     
     //Translated Molecule Variables to Store Coordinate Array Values
         //Molecule One
         public static double finalTransLeftHydrogenOneXPos;
         public static double finalTransLeftHydrogenOneYPos;
         public static double finalTransLeftHydrogenOneZPos;
         public static double finalTransOxygenOneXPos;
         public static double finalTransOxygenOneYPos;
         public static double finalTransOxygenOneZPos;
         public static double finalTransRightHydrogenOneXPos;
         public static double finalTransRightHydrogenOneYPos;
         public static double finalTransRightHydrogenOneZPos;
         
         //Molecule Two
         public static double finalTransLeftHydrogenTwoXPos;
         public static double finalTransLeftHydrogenTwoYPos;
         public static double finalTransLeftHydrogenTwoZPos;
         public static double finalTransOxygenTwoXPos;
         public static double finalTransOxygenTwoYPos;
         public static double finalTransOxygenTwoZPos;
         public static double finalTransRightHydrogenTwoXPos;
         public static double finalTransRightHydrogenTwoYPos;
         public static double finalTransRightHydrogenTwoZPos;
     
     //Randomly Generated Numbers
     public static double randomTheta;
     public static double randomTranslationConstantMolONE;
     public static double randomTranslationConstantMolTWO;
     
     public static void main(String[] args) throws IOException {
         Date date = new Date() ;
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
         File file = new File(dateFormat.format(date) + ".txt") ;
         FileOutputStream fis = new FileOutputStream(file);
         PrintStream out = new PrintStream(fis);  
         System.setOut(out);
 
         System.out.println(date);
         double rangeStartRandomTheta = 0;
         double rangeEndRandomTheta = Math.toRadians(360);
         Random randomGenRandomTheta = new Random();
         randomThetaGenerator(rangeStartRandomTheta, rangeEndRandomTheta, randomGenRandomTheta);
 //        System.out.println("\n\nRandom Angle: " + randomTheta);
         rotationAboutXYZMatrix(randomTheta); //(theta)
 
 
         double rangeStartRandomTranslationConstant = 3;
         double rangeEndRandomTranslationConstant = 350;
         Random randomGenTranslationConstant = new Random();
         randomTranslationConstantGeneratorONE(rangeStartRandomTranslationConstant, rangeEndRandomTranslationConstant,randomGenTranslationConstant);
         randomTranslationConstantGeneratorTWO(rangeStartRandomTranslationConstant, rangeEndRandomTranslationConstant, randomGenTranslationConstant);
 
         randomTranslateMolOneXYZ();
         randomTranslateMolTwoXYZ();
 
         finalRotLeftHydrogenOneXPos = finalRotatedZArrayOne[0][0];
         finalRotLeftHydrogenOneYPos = finalRotatedZArrayOne[0][1];
         finalRotLeftHydrogenOneZPos = finalRotatedZArrayOne[0][2];
         finalRotOxygenOneXPos = finalRotatedZArrayOne[1][0];
         finalRotOxygenOneYPos = finalRotatedZArrayOne[1][1];
         finalRotOxygenOneZPos = finalRotatedZArrayOne[1][2];
         finalRotRightHydrogenOneXPos = finalRotatedZArrayOne[2][0];
         finalRotRightHydrogenOneYPos = finalRotatedZArrayOne[2][1];
         finalRotRighyHydrogenOneZPos = finalRotatedZArrayOne[2][2];
 
         finalRotLeftHydrogenTwoXPos = finalRotatedZArrayTwo[0][0];
         finalRotLeftHydrogenTwoYPos = finalRotatedZArrayTwo[0][1];
         finalRotLeftHydrogenTwoZPos = finalRotatedZArrayTwo[0][2];
         finalRotOxygenTwoXPos = finalRotatedZArrayTwo[1][0];
         finalRotOxygenTwoYPos = finalRotatedZArrayTwo[1][1];
         finalRotOxygenTwoZPos = finalRotatedZArrayTwo[1][2];
         finalRotRightHydrogenTwoXPos = finalRotatedZArrayTwo[2][0];
         finalRotRightHydrogenTwoYPos = finalRotatedZArrayTwo[2][1];
         finalRotRightHydrogenTwoZPos = finalRotatedZArrayTwo[2][2];
 
         finalTransLeftHydrogenOneXPos = finalTranslatedArrayOne[0][0];
         finalTransLeftHydrogenOneYPos = finalTranslatedArrayOne[0][1];
         finalTransLeftHydrogenOneZPos = finalTranslatedArrayOne[0][2];
         finalTransOxygenOneXPos = finalTranslatedArrayOne[1][0];
         finalTransOxygenOneYPos = finalTranslatedArrayOne[1][1];
         finalTransOxygenOneZPos = finalTranslatedArrayOne[1][2];
         finalTransRightHydrogenOneXPos = finalTranslatedArrayOne[2][0];
         finalTransRightHydrogenOneYPos = finalTranslatedArrayOne[2][1];
         finalTransRightHydrogenOneZPos = finalTranslatedArrayOne[2][2];
 
         finalTransLeftHydrogenTwoXPos = finalTranslatedArrayTwo[0][0];
         finalTransLeftHydrogenTwoYPos = finalTranslatedArrayTwo[0][1];
         finalTransLeftHydrogenTwoZPos = finalTranslatedArrayTwo[0][2];
         finalTransOxygenTwoXPos = finalTranslatedArrayTwo[1][0];
         finalTransOxygenTwoYPos = finalTranslatedArrayTwo[1][1];
         finalTransOxygenTwoZPos = finalTranslatedArrayTwo[1][2];
         finalTransRightHydrogenTwoXPos = finalTranslatedArrayTwo[2][0];
         finalTransRightHydrogenTwoYPos = finalTranslatedArrayTwo[2][1];
         finalTransRightHydrogenTwoZPos = finalTranslatedArrayTwo[2][2];
 
         initialLeftHydrogenOneCoord = new double[3];
         initialRightHydrogenOneCoord = new double[3];
         initialOxygenOneCoord = new double[3];
         initialOxygenTwoCoord = new double[3];
         initialLeftHydrogenTwoCoord = new double[3];
         initialRightHydrogenTwoCoord = new double[3];
 
         //Initial Position of First Water Molecule in System
         initialLeftHydrogenOneCoord[0] = -24;
         initialLeftHydrogenOneCoord[1] = 0;
         initialLeftHydrogenOneCoord[2] = 93;
 
         initialOxygenOneCoord[0] = 0;
         initialOxygenOneCoord[1] = 0;
         initialOxygenOneCoord[2] = 0;
 
         initialRightHydrogenOneCoord[0] = 96;
         initialRightHydrogenOneCoord[1] = 0;
         initialRightHydrogenOneCoord[2] = 0;
 
         firstCalcIteration = calculatePointCharge(finalTransLeftHydrogenOneXPos, finalTransLeftHydrogenOneXPos, finalTransLeftHydrogenOneYPos, finalTransLeftHydrogenTwoXPos, finalTransLeftHydrogenOneZPos, finalTransLeftHydrogenTwoZPos);
         secondCalcIteration = calculatePointCharge(finalTransLeftHydrogenOneXPos, finalTransLeftHydrogenOneYPos, finalTransLeftHydrogenOneZPos, finalTransRightHydrogenTwoXPos, finalTransRightHydrogenTwoYPos, finalTransRightHydrogenTwoZPos);
         thirdCalcIteration = calculatePointCharge(finalTransRightHydrogenOneXPos, finalTransRightHydrogenOneYPos, finalTransRightHydrogenOneZPos, finalTransLeftHydrogenTwoXPos, finalTransLeftHydrogenTwoYPos, finalTransLeftHydrogenTwoZPos);
         fourthCalcIterartion = calculatePointCharge(finalTransRightHydrogenOneXPos, finalTransRightHydrogenOneZPos, finalTransRightHydrogenOneYPos, finalTransRightHydrogenTwoXPos, finalTransRightHydrogenTwoYPos, finalTransRightHydrogenTwoZPos);
         fifthCalcIteration = calculatePointCharge(finalTransOxygenOneXPos, finalTransOxygenOneYPos, finalTransOxygenOneZPos, finalTransOxygenTwoXPos, finalTransOxygenTwoYPos, finalTransOxygenTwoZPos);
         sixthCalcIteration = calculatePointCharge(finalTransLeftHydrogenOneXPos, finalTransLeftHydrogenOneYPos, finalTransLeftHydrogenOneZPos, finalTransOxygenOneXPos, finalTransOxygenOneYPos, finalTransOxygenOneZPos);
         seventhCalcIteration = calculatePointCharge(finalTransRightHydrogenOneXPos, finalTransRightHydrogenOneYPos, finalTransRightHydrogenOneZPos, finalTransOxygenTwoXPos, finalTransOxygenTwoYPos, finalTransOxygenTwoZPos);
         eighthCalcIteration = calculatePointCharge(finalTransLeftHydrogenTwoXPos, finalTransLeftHydrogenTwoYPos, finalTransLeftHydrogenTwoZPos, finalTransOxygenOneXPos, finalTransOxygenOneYPos, finalTransOxygenOneZPos);
         ninthCalcIteration = calculatePointCharge(finalTransRightHydrogenOneXPos, finalTransRightHydrogenOneYPos, finalTransRightHydrogenOneZPos, finalTransOxygenOneXPos, finalTransOxygenOneYPos, finalTransOxygenOneZPos);
         totalPointChargeEnergy = firstCalcIteration+secondCalcIteration+thirdCalcIteration+fourthCalcIterartion+fifthCalcIteration+sixthCalcIteration
                 +seventhCalcIteration+eighthCalcIteration+ninthCalcIteration;
 
         System.out.println("\n\n\nSigma Point Charge Calculation: " + totalPointChargeEnergy);
     }
     
     private static void sopl(String userInput){
         System.out.println(userInput);
     }
     
     public static double calculatePointCharge(double moleculeOneX1, double moleculeOneY1, double moleculeOneZ1, double moleculeTwoX2, double moleculeTwoY2, double moleculeTwoZ2) {
         double distance = Math.sqrt(Math.sqrt(Math.pow(moleculeTwoX2-moleculeOneX1, 2) + Math.pow(moleculeTwoY2-moleculeOneY1, 2) + Math.pow(moleculeTwoZ2-moleculeOneZ1, 2)));
         if(moleculeOneX1 == finalTransLeftHydrogenOneXPos && moleculeTwoX2 == finalTransLeftHydrogenTwoXPos) { //For first iteration
             pointChargeEnergy = Constants.kConstant*Constants.KhydrogenPointCharge*Constants.KhydrogenPointCharge/Math.abs(distance);
             return pointChargeEnergy;
         } else if(moleculeOneX1 == finalTransLeftHydrogenOneXPos && moleculeTwoX2 == finalTransRightHydrogenTwoXPos) { //For second iteration
             pointChargeEnergy = Constants.kConstant*Constants.KhydrogenPointCharge*Constants.KhydrogenPointCharge/Math.abs(distance);
             return pointChargeEnergy;
         } else if(moleculeOneX1 == finalTransRightHydrogenOneXPos && moleculeTwoX2 == finalTransLeftHydrogenTwoXPos) { //For third iteration
             pointChargeEnergy = Constants.kConstant*Constants.KhydrogenPointCharge*Constants.KhydrogenPointCharge/Math.abs(distance);
             return pointChargeEnergy;
         } else if(moleculeOneX1 == finalTransOxygenOneXPos && moleculeTwoX2 == finalTransOxygenTwoXPos) { //For fourth iteration
             pointChargeEnergy = Constants.kConstant*Constants.KoxygenPointCharge*Constants.KoxygenPointCharge/Math.abs(distance);
             return pointChargeEnergy;
         } else if(moleculeOneX1 == finalTransOxygenOneXPos && moleculeTwoX2 == finalTransOxygenTwoXPos) { //For fifth iteration
             pointChargeEnergy = Constants.kConstant*Constants.KoxygenPointCharge*Constants.KoxygenPointCharge/Math.abs(distance);
             return pointChargeEnergy;
         } else if(moleculeOneX1 == finalTransLeftHydrogenOneXPos && moleculeTwoX2 == finalTransOxygenOneXPos) { //For sixth iteration
             pointChargeEnergy = Constants.kConstant*Constants.KhydrogenPointCharge*Constants.KoxygenPointCharge/Math.abs(distance);
             return pointChargeEnergy;
         } else if(moleculeOneX1 == finalTransRightHydrogenOneXPos && moleculeTwoX2 == finalTransOxygenTwoXPos) { //For seventh iteration
             pointChargeEnergy = Constants.kConstant*Constants.KhydrogenPointCharge*Constants.KoxygenPointCharge/Math.abs(distance);
             return pointChargeEnergy;
         } else if(moleculeOneX1 == finalTransLeftHydrogenTwoXPos && moleculeTwoX2 == finalTransOxygenOneXPos) { //For eighth iteration
             pointChargeEnergy = Constants.kConstant*Constants.KhydrogenPointCharge*Constants.KoxygenPointCharge/Math.abs(distance);
             return pointChargeEnergy;
         } else if(moleculeOneX1 == finalTransRightHydrogenOneXPos && moleculeTwoX2 == finalTransOxygenOneXPos) { //For ninth iteration
             pointChargeEnergy = Constants.kConstant*Constants.KhydrogenPointCharge*Constants.KoxygenPointCharge/Math.abs(distance);
             return pointChargeEnergy;
         } else {
             return 0;
         }
     }
     
     public static double caclulateDipoleMoment(double momentOne, double momentTwo, double distance) {
         return 0;
     }
     
     public static void rotationAboutXYZMatrix(double theta) {    
         double cosTheta = Math.cos(theta);
         double sinTheta = Math.sin(theta);
         double xRotationArrayConstants[][] = {{1,0,0},{0,cosTheta,-sinTheta},{0,sinTheta,cosTheta}}; //TOPtoBOTTOM
         double initialPositionsArraybeforeXRot[][] = {{-24,0,93},{0,0,0},{96,0,0}}; //xyz
         double yRotationArrayConstants[][] = {{cosTheta,0,-sinTheta},{0,1,0},{sinTheta,0,cosTheta}}; //TOPtoBOTTOM
         double zRotationArrayConstants[][] = {{cosTheta,sinTheta,0},{-sinTheta,cosTheta,0},{0,0,1}}; //TOPtoBOTTOM
         finalRotatedXArrayOne = new double[3][3];
         finalRotatedYArrayOne = new double[3][3];
         finalRotatedZArrayOne = new double[3][3];
 
         finalRotatedXArrayTwo = new double[3][3];
         finalRotatedYArrayTwo = new double[3][3];
         finalRotatedZArrayTwo = new double[3][3];
 
         //START X ROTATION MATRIX Molecule One
         int x = 3;
         System.out.println("\nRotate (X) Molecule One Constants Matrix: ");
         for(int i = 0; i < x; i++) {
             for(int j = 0; j < x; j++) {
                 System.out.print(" "+ xRotationArrayConstants[i][j]);
             }
         System.out.println();
         }  
         int y = 3;
         System.out.println("\nRotate (X) Molecule One Matrix Coordinates: ");
         for(int i = 0; i < y; i++) {
             for(int j = 0; j < y; j++) {
             System.out.print(" "+initialPositionsArraybeforeXRot[i][j]);
         }  
             System.out.println();
         }
         for(int i = 0; i < x; i++) {
             for(int j = 0; j < y; j++) {
                 for(int k = 0; k < y; k++){
                     finalRotatedXArrayOne[i][j] += xRotationArrayConstants[i][k]*initialPositionsArraybeforeXRot[k][j];
                 }
             }  
         }
         System.out.println("\nCalculated Rotated (X) Molecule One Matrix Coordinates: ");
             for(int i = 0; i < x; i++) {
                 for(int j = 0; j < y; j++) {
                     System.out.print(" "+finalRotatedXArrayOne[i][j]);
                 }  
                 System.out.println();
             }
         //END X ROTATION MATRIX Molecule One
         //START Y ROTATION MATRIX Molecule One
         System.out.println("\nRotate (Y) Molecule One Constants Matrix: ");
         for(int i = 0; i < x; i++) {
             for(int j = 0; j < x; j++) {
                 System.out.print(" "+ yRotationArrayConstants[i][j]);
             }
         System.out.println();
         }  
         System.out.println("\nRotate (Y) Molecule One Matrix Coordinates: ");
         for(int i = 0; i < y; i++) {
             for(int j = 0; j < y; j++) {
             System.out.print(" "+finalRotatedXArrayOne[i][j]);
         }  
             System.out.println();
         }
         for(int i = 0; i < x; i++) {
             for(int j = 0; j < y; j++) {
                 for(int k = 0; k < y; k++){
                     finalRotatedYArrayOne[i][j] += yRotationArrayConstants[i][k]*finalRotatedXArrayOne[k][j];
                 }
             }  
         }
         System.out.println("\nCalculated Rotated (Y) Molecule One Matrix Coordinates: ");
             for(int i = 0; i < x; i++) {
                 for(int j = 0; j < x; j++) {
                     System.out.print(" "+finalRotatedYArrayOne[i][j]);
                 }  
                 System.out.println();
             }
         //END Y ROTATION MATRIX Molecule One
         //START Z ROTATION MATRIX Molecule One
         System.out.println("\nRotate (Z) Molecule One Constants Matrix: ");
         for(int i = 0; i < x; i++) {
             for(int j = 0; j < x; j++) {
                 System.out.print(" "+ zRotationArrayConstants[i][j]);
             }
         System.out.println();
         }  
         System.out.println("\nRotate (Z) Molecule One Matrix Coordinates: ");
         for(int i = 0; i < y; i++) {
             for(int j = 0; j < y; j++) {
             System.out.print(" "+finalRotatedYArrayOne[i][j]);
         }  
             System.out.println();
         }
         for(int i = 0; i < x; i++) {
             for(int j = 0; j < y; j++) {
                 for(int k = 0; k < y; k++){
                     finalRotatedZArrayOne[i][j] += zRotationArrayConstants[i][k]*finalRotatedYArrayOne[k][j];
                 }
             }  
         }
         System.out.println("\n\nFinal Calculated Rotated (Z) Molecule One Matrix Coordinates: ");
             for(int i = 0; i < x; i++) {
                 for(int j = 0; j < y; j++) {
                     System.out.print(" "+finalRotatedZArrayOne[i][j]);
                 }  
                 System.out.println();
             }
         //END Z ROTATION MATRIX Molecule One
             
         //START X ROTATION MATRIX Molecule Two
         System.out.println("\nRotate (X) Molecule Two Constants Matrix: ");
         for(int i = 0; i < x; i++) {
             for(int j = 0; j < x; j++) {
                 System.out.print(" "+ xRotationArrayConstants[i][j]);
             }
         System.out.println();
         }  
         System.out.println("\nRotate (X) Molecule Two Matrix Coordinates: ");
         for(int i = 0; i < y; i++) {
             for(int j = 0; j < y; j++) {
             System.out.print(" "+initialPositionsArraybeforeXRot[i][j]);
         }  
             System.out.println();
         }
         for(int i = 0; i < x; i++) {
             for(int j = 0; j < y; j++) {
                 for(int k = 0; k < y; k++){
                     finalRotatedXArrayTwo[i][j] += xRotationArrayConstants[i][k]*initialPositionsArraybeforeXRot[k][j];
                 }
             }  
         }
         System.out.println("\nCalculated Rotated (X) Molecule Two Matrix Coordinates: ");
             for(int i = 0; i < x; i++) {
                 for(int j = 0; j < y; j++) {
                     System.out.print(" "+finalRotatedXArrayTwo[i][j]);
                 }  
                 System.out.println();
             }
         //END X ROTATION MATRIX Molecule Two
         //START Y ROTATION MATRIX Molecule Two
         System.out.println("\nRotate (Y) Molecule Two Constants Matrix: ");
         for(int i = 0; i < x; i++) {
             for(int j = 0; j < x; j++) {
                 System.out.print(" "+ yRotationArrayConstants[i][j]);
             }
         System.out.println();
         }  
         System.out.println("\nRotate (Y) Molecule Two Matrix Coordinates: ");
         for(int i = 0; i < y; i++) {
             for(int j = 0; j < y; j++) {
             System.out.print(" "+finalRotatedXArrayTwo[i][j]);
         }  
             System.out.println();
         }
         for(int i = 0; i < x; i++) {
             for(int j = 0; j < y; j++) {
                 for(int k = 0; k < y; k++){
                     finalRotatedYArrayTwo[i][j] += yRotationArrayConstants[i][k]*finalRotatedXArrayTwo[k][j];
                 }
             }  
         }
         System.out.println("\nCalculated Rotated (Y) Molecule Two Matrix Coordinates:");
             for(int i = 0; i < x; i++) {
                 for(int j = 0; j < x; j++) {
                     System.out.print(" "+finalRotatedYArrayTwo[i][j]);
                 }  
                 System.out.println();
             }
         //END Y ROTATION MATRIX Molecule Two
         //START Z ROTATION MATRIX Molecule Two
         System.out.println("\nRotate (Z) Molecule Two Constants Matrix: ");
         for(int i = 0; i < x; i++) {
             for(int j = 0; j < x; j++) {
                 System.out.print(" "+ zRotationArrayConstants[i][j]);
             }
         System.out.println();
         }  
         System.out.println("\nRotate (Z) Molecule Two Matrix Coordinates: ");
         for(int i = 0; i < y; i++) {
             for(int j = 0; j < y; j++) {
             System.out.print(" "+finalRotatedYArrayTwo[i][j]);
         }  
             System.out.println();
         }
         for(int i = 0; i < x; i++) {
             for(int j = 0; j < y; j++) {
                 for(int k = 0; k < y; k++){
                     finalRotatedZArrayTwo[i][j] += zRotationArrayConstants[i][k]*finalRotatedYArrayTwo[k][j];
                 }
             }  
         }
         System.out.println("\n\nFinal Calculated Rotated (Z) Molecule Two Matrix Coordinates: ");
             for(int i = 0; i < x; i++) {
                 for(int j = 0; j < y; j++) {
                     System.out.print(" "+finalRotatedZArrayTwo[i][j]);
                 }  
                 System.out.println();
             }
         //END Z ROTATION MATRIX Molecule Two
     }
     
     public static void randomTranslateMolOneXYZ() {
         int m, n, c, d;
         m = 3; //column definition
         n = 3; //row definition
         
         double first[][] = new double[m][n];
         finalTranslatedArrayOne = new double[m][n];
 
         for (  c = 0 ; c < m ; c++ ) {
            for ( d = 0 ; d < n ; d++ ) {
               first[c][d] = randomTranslationConstantMolONE;
            }
         }
 
         for ( c = 0 ; c < m ; c++ ) {
            for ( d = 0 ; d < n ; d++ ) {
                finalTranslatedArrayOne[c][d] = first[c][d] + finalRotatedZArrayOne[c][d];  //replace '+' with '-' to subtract matrices
            }
         }
 
         System.out.println("\n\nFinal Molecule One Coordinates: "); //Sum of matrices
 
         for ( c = 0 ; c < m ; c++ ) {
            for ( d = 0 ; d < n ; d++ ) {
               System.out.print(finalTranslatedArrayOne[c][d]+"\t");
            }
            System.out.println();
         }
     }
    
     public static void randomTranslateMolTwoXYZ() {
         int m, n, c, d;
         m = 3; //column definition
         n = 3; //row definition
 
         double first[][] = new double[m][n];
         finalTranslatedArrayTwo = new double[m][n];
 
         for (  c = 0 ; c < m ; c++ ) {
            for ( d = 0 ; d < n ; d++ ) {
               first[c][d] = randomTranslationConstantMolTWO;
            }
         }
 
         for ( c = 0 ; c < m ; c++ ) {
            for ( d = 0 ; d < n ; d++ ) {
                finalTranslatedArrayTwo[c][d] = first[c][d] + finalRotatedZArrayTwo[c][d];  //replace '+' with '-' to subtract matrices
            }
         }
 
         System.out.println("\n\nFinal Molecule Two Coordinates: "); //Sum of matrices
 
         for ( c = 0 ; c < m ; c++ ) {
            for ( d = 0 ; d < n ; d++ ) {
               System.out.print(finalTranslatedArrayTwo[c][d]+"\t");
            }
            System.out.println();
         }
     }
     
      public static void randomThetaGenerator(double rangeStart, double rangeEnd, Random thetaRandom) {
         if(rangeStart > rangeEnd) {
             throw new IllegalArgumentException("Start cannot exceed End.");
         }
         //get the range, casting to long to avoid overflow problems
         long range = (long)rangeEnd - (long)rangeStart + 1;
         // compute a fraction of the range, 0 <= frac < range
         long fraction = (long)(range * thetaRandom.nextDouble());
         randomTheta =  (double)(fraction + rangeStart);    
         sopl("\n\nRandom Angle: " + randomTheta);
     }
      
      public static void randomTranslationConstantGeneratorONE(double rangeStart, double rangeEnd, Random thetaRandom) {
         if(rangeStart > rangeEnd) {
             throw new IllegalArgumentException("Start cannot exceed End.");
         }
         //get the range, casting to long to avoid overflow problems
         long range = (long)rangeEnd - (long)rangeStart + 1;
         // compute a fraction of the range, 0 <= frac < range
         long fraction = (long)(range * thetaRandom.nextDouble());
         randomTranslationConstantMolONE =  (double)(fraction + rangeStart);    
         sopl("\n\nRandom Translation Constant MOLECULE ONE (1): " + randomTranslationConstantMolONE);
     }
      
      public static void randomTranslationConstantGeneratorTWO(double rangeStart, double rangeEnd, Random thetaRandom) {
         if(rangeStart > rangeEnd) {
             throw new IllegalArgumentException("Start cannot exceed End.");
         }
         //get the range, casting to long to avoid overflow problems
         long range = (long)rangeEnd - (long)rangeStart + 1;
         // compute a fraction of the range, 0 <= frac < range
         long fraction = (long)(range * thetaRandom.nextDouble());
         randomTranslationConstantMolTWO =  (double)(fraction + rangeStart);    
         sopl("\nRandom Translation Constant MOLECULE TWO (2): " + randomTranslationConstantMolTWO);
     }
      
 }
