 /*
  * Copyright (C) 2014 David McInnis
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ewucalligraphy.image;
 
 import static java.util.Arrays.copyOf;
 import static java.util.Arrays.sort;
 
 /**
  *
  * @author David McInnis <davidm@eagles.ewu.edu>
  */
 
 
 public class Statistics
 {
      private Row[] horRow, vertRow;
     
     private int[] sortedGlobal;
     private int[] topLeftCorner;
     private int   medVal;
     
     
     
     
    //NOte, this is a bit inefficiet, because I create a lot of
     //temporary copies of parts of the image.
     
     public Statistics(int[][] imG)
     {
            buildStatistics(imG, 0, 0, imG.length, imG[0].length);
     }
     
     public Statistics(int[][] imG, int startX, int startY, int endX, int endY)
     {
         buildStatistics(imG, startX, startY, endX, endY);
     }
     
     private void buildStatistics(int[][] imG, int startX, int startY, int endX, int endY)
     {
         topLeftCorner = new int[2];
         topLeftCorner[0] = startX;
         topLeftCorner[1] = startY;
         
         
         int distX = endX - startX;
         int distY = endY - startY;
       
 
         int iMax = distX * distY;
         
         vertRow = new Row[distY];
         horRow  = new Row[distX];
         
         int x, y;
         int[] tempHorRow, tempVertRow;
         
         tempHorRow = new int[distY];
         tempVertRow = new int[distX];
     
         sortedGlobal = new int[iMax];
 
 
         int cntr = 0;
         int Xcntr = 0;
         int Ycntr = 0;
 
         for(x = startX; x < endX; ++x)
         {
             Ycntr = 0;
             for(y = startY; y < endY; ++y)
             {
                 tempHorRow[Ycntr] = imG [x][y];
                 sortedGlobal[cntr] = imG[x][y];
                 ++cntr; ++Ycntr;
             }
             horRow[Xcntr] = new Row(tempHorRow);
             ++Xcntr;
         }
         Xcntr = 0;
                 
         for(x = startY; x < endY; ++x)
         {
             Ycntr = 0;
             for(y = startX; y < endX; ++y)
             {
                 tempVertRow[Ycntr] = imG [y][x];
                 ++Ycntr;
             }
             vertRow[Xcntr] = new Row(tempVertRow);
             ++Xcntr;
         }
          
         sort(sortedGlobal);
         medVal = sortedGlobal[iMax / 2];
     }
     
     public int getMedian()
     {
         return medVal;
     }
    
 
       
     public String getGnuPlotHorizontalRows()
     {
         String outPut = "# horizontal row statistics for image\n";
         outPut += "# row     min     median     max\n";
         
         for(int x = 0; x < horRow.length; ++x)
         {
             outPut += "\n";
             outPut += x;
             outPut += " ";
             outPut += horRow[x].getMin();
             outPut += " ";
             outPut += horRow[x].getMedian();
             outPut += " ";
             outPut += horRow[x].getMax();
         }
         return outPut;
     }
     public String getGnuPlotVerticalRows()
     {
         String outPut = "# vertical row statistics for image\n";
         outPut += "# row     min     median     max\n";
         
         for(int x = 0; x < vertRow.length; ++x)
         {
             outPut += "\n";
             outPut += x;
             outPut += " ";
             outPut += vertRow[x].getMin();
             outPut += " ";
             outPut += vertRow[x].getMedian();
             outPut += " ";
             outPut += vertRow[x].getMax();
         }
         return outPut;
     }
 
     public int GetSmallestMedian(ImgDir horizOrVert)
     {
         int targetPos = 0;
         int minMedian = 255;
         int edgeOffset = 10;
         
         assert(horizOrVert == ImgDir.VERTICAL || horizOrVert == ImgDir.HORIZONTAL); 
         
         if(horizOrVert == ImgDir.HORIZONTAL)
         {
             for(int x = edgeOffset; x < vertRow.length - edgeOffset; ++x)
             {
                 if(vertRow[x].getMedian() < minMedian)
                 {
                     minMedian = vertRow[x].getMedian();
                     targetPos = x;
                 }
             }
         }
         else
         {
             for(int x = edgeOffset; x < horRow.length - edgeOffset; ++x)
             {
                 if(horRow[x].getMedian() < minMedian)
                 {
                     minMedian = horRow[x].getMedian();
                     targetPos = x;
                 }
             }
         }
         return targetPos;
     }
     
     public int growTillTargetMedian(ImgDir startPosition, int targetMedian)
     {
         assert(targetMedian >= 0 && targetMedian <= 255);
         assert(startPosition != ImgDir.HORIZONTAL && startPosition != ImgDir.VERTICAL);
         
         int cntr, newOffset;
         newOffset = 0;
         
         switch(startPosition)
         {
             case  BOTTOM:
                 cntr = vertRow.length - 1;
                 while(targetMedian > vertRow[cntr].getMedian() && cntr > 0)
                 {
                     --cntr;
                 }
                 newOffset = topLeftCorner[1] + cntr;
                 break;
             case TOP:
                 cntr = 0;
                 while(targetMedian > vertRow[cntr].getMedian() && cntr < vertRow.length - 1)
                 {
                     ++cntr;
                 }
                 newOffset = topLeftCorner[1] + cntr;
                 break;
             case RIGHT:
                 cntr = 0;
                 while(targetMedian > horRow[cntr].getMedian() && cntr < horRow.length - 1)
                 {
                     ++cntr;
                 }
                 newOffset = topLeftCorner[0] + cntr;
                 break;
             case LEFT:
                 cntr = horRow.length - 1;
                 while(targetMedian > horRow[cntr].getMedian() && cntr > 0)
                 {
                     --cntr;
                 }
                 newOffset = topLeftCorner[0] + cntr;
                 break;
         }
         return newOffset;
     }
     
     
     private class Row
     {
         private final int[] sortedRow;
         private final int min, median, max;
         
         public Row(int[] inRow)
         {
             int rowLength;
             
             rowLength = inRow.length;
             
             sortedRow = copyOf(inRow, rowLength);
             
             sort(sortedRow); //Sorts the array
             
             min    = sortedRow[0];
             median = sortedRow[rowLength / 2];
             max    = sortedRow[rowLength - 1];
             
         }
               
         public int getMin()
         {
             return min;
         }
         
         public int getMedian()
         {
             return median;
         }
         
         public int getMax()
         {
             return max;
         }
         
     }
     
    
 }
