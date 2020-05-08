 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package woodcock;
 
 import java.util.Random;
 
 /**
  *
  * @author Z98
  */
 public class Patch {
     public int x, y;
     public int landCover, waterDepth, roadLength, soil, canopy, age, landing;
     public double[] trees = null;
     public static Random rand = null;
     
     double[][] growthMatrix = null;
     double ingrowthMatrix;
     
     double baselArea;
     
     public Patch(int x, int y)
     {
         this.x = x;
         this.y = y;
         trees = new double[12];
         if(rand == null) rand = new Random();
     }
     
     public Patch(int x, int y, int landCover, int waterDepth, int roadLength, int soil, int canopy)
     {
         this(x, y);
         this.landCover = landCover;
         this.waterDepth = waterDepth;
         this.soil = soil;
         this.canopy = canopy;
     }
     
     public void generateTrees(int diameter)
     {
         double targetBasel = rand.nextInt(51) + 60;
         double currentBasel = 0;
         /*
          * Reenable after we get that negative exponential
          * diameter code implemented.
          */
         //int normOrNot = rand.nextInt(2);
         int normOrNot = 1;
         while(currentBasel < targetBasel)
         {
             if(normOrNot == 1)
             {
                 double x = (double)rand.nextInt(101) / 100.0;
                 double y = (double)rand.nextInt(101) / 100.0;
                 double sD = x/(x+y) * diameter;
                int treeSize = rand.nextInt(13);
                 ++trees[treeSize];
                 int treeDia = (treeSize + 1) * 2;
                 currentBasel += Calculation.BA * Math.pow(treeDia, 2);
             }
             else
             {
                 
             }
         }
         
         baselArea = currentBasel;
     }
     
     public void growTrees()
     {
         if(growthMatrix == null)
         {
             switch(landCover)
             {
                 case 141:
                     growthMatrix = Calculation.decGrowthMatrix;
                     ingrowthMatrix = Calculation.decIngrowth;
                     break;
                 case 142:
                     growthMatrix = Calculation.conGrowthMatrix;
                     ingrowthMatrix = Calculation.conIngrowth;
                     break;
                 case 143:
                     growthMatrix = Calculation.mixGrowthMatrix;
                     ingrowthMatrix = Calculation.mixIngrowth;
             }
         }
         
         double nextGrowth[] = new double[12];
         for(int y = 0; y < 12; y++)
         {
             nextGrowth[y] = 0;
             for(int x = 0; x < 12; x++)
             {
                 nextGrowth[y] += trees[x] * growthMatrix[y][x];
             }
             
             if(y == 0) nextGrowth[y] += ingrowthMatrix;
         }
         
         baselArea = 0;
         
         trees = nextGrowth;
         for(int index = 0; index < 12; index++)
         {
             int treeDia = (index + 1) * 2;
             baselArea += Calculation.BA * Math.pow(treeDia, 2) * trees[index];
         }
     }
     
     public double calcValue()
     {
         double totalVolume = 0;
         double mBF = 0;
         int cutOff;
         int endIndex;
         if(landCover == 142)
         {
             cutOff = 8;
             endIndex = cutOff / 2;
         }
         else
         {
             cutOff = 10;
             endIndex = cutOff / 2;
         }
         for(int index = endIndex; index < 12; index++)
         {
             int diameter;
             double tValue;
             double height;
             double volume = 0;
             switch (landCover) {
                 case 141:
                     diameter = (4 + 1) * 2;
                     if (diameter > 10) {
                         tValue = 1.00001 - (9 / diameter);
                     } else {
                         tValue = 1.00001 - (5 / diameter);
                     }
                     height =
                             4.5 + 6.43
                             * Math.pow(1 - Math.exp(-.24 * diameter), 1.34)
                             * Math.pow(Calculation.sConstant, .47)
                             * Math.pow(tValue, .73)
                             * Math.pow(baselArea, .08);
                     volume = 2.706 + 0.002 * Math.pow(diameter, 2) * height;
                     totalVolume += volume;
                     break;
                 case 142:
                     diameter = (4 + 1) * 2;
                     if (diameter > 8) {
                         tValue = 1.00001 - (9 / diameter);
                     } else {
                         tValue = 1.00001 - (5 / diameter);
                     }
                     height =
                             4.5 + 5.32
                             * Math.pow(1 - Math.exp(-.23 * diameter), 1.15)
                             * Math.pow(Calculation.sConstant, .54)
                             * Math.pow((tValue), 0.83)
                             * Math.pow(baselArea, .06);
                     volume = 1.375 + 0.002 * Math.pow(diameter, 2) * height;
                     totalVolume += volume;
                     break;
                 case 143:
                     diameter = (4 + 1) * 2;
                     if (diameter > 10) {
                         tValue = 1.00001 - (9 / diameter);
                     } else {
                         tValue = 1.00001 - (5 / diameter);
                     }
                     height =
                             4.5 + 7.19
                             * Math.pow(1 - Math.exp(-.28 * diameter), 1.44)
                             * Math.pow(Calculation.sConstant, .39)
                             * Math.pow((tValue), 0.83)
                             * Math.pow(baselArea, .11);
                     volume = 0.002 * Math.pow(diameter, 2) * height;
                     totalVolume += volume;
                     break;
             }
             
             double factor = 0;
             
             if(landCover == 142) //evergreen/con
             {
                 switch(index)
                 {
                     case 4:
                         factor = 0.783;
                         break;
                     case 5:
                         factor = 0.829;
                         break;
                     case 6:
                         factor = 0.858;
                         break;
                     case 7:
                         factor = 0.878;
                         break;
                     case 8:
                         factor = 0.895;
                         break;
                     case 9:
                         factor = 0.908;
                         break;
                     case 10:
                         factor = 0.917;
                         break;
                     case 11:
                         factor = 0.924;
                         break;
                 }
             }
             else
             {
                 switch(index)
                 {
                     case 5:
                         factor = 0.832;
                         break;
                     case 6:
                         factor = 0.861;
                         break;
                     case 7:
                         factor = 0.883;
                         break;
                     case 8:
                         factor = 0.9;
                         break;
                     case 9:
                         factor = 0.913;
                         break;
                     case 10:
                         factor = 0.924;
                         break;
                     case 11:
                         factor = 0.933;
                         break;
                 }
             }
             
             mBF += (volume * factor)/12;
         }
         
         return mBF;
     }
 }
