 /*
  * Copyright © 2013 Volker Knollmann
  * 
  * This work is free. You can redistribute it and/or modify it under the
  * terms of the Do What The Fuck You Want To Public License, Version 2,
  * as published by Sam Hocevar. See the COPYING file or visit
  * http://www.wtfpl.net/ for more details.
  * 
  * This program comes without any warranty. Use it at your own risk or
  * don't use it at all.
  */
 
 package org.nodomain.volkerk.PinkDotRemover;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import org.nodomain.volkerk.LoggingLib.LoggingClass;
 import org.nodomain.volkerk.SimpleTIFFlib.ImageFileDirectory;
 import org.nodomain.volkerk.SimpleTIFFlib.TIFFhandler;
 
 /**
  * Removes the AF dots in Magic Lantern's raw video frames for the Canon 650D
  */
 public class PinkDotRemover extends LoggingClass {
     
     /**
      * the DNG file to modify
      */
     protected String srcFileName;
     
     /**
      * the image handler for the input file
      */
     protected TIFFhandler srcDng;
     
     /**
      * an image handler for the output file -- will be initialized from the input file
      */
     protected TIFFhandler dstDng;
     
     /**
      * Constructor. Checks for a valid file name and tries to open the file
      * 
      * @param fName the name / path of the DNG file
      */
     public PinkDotRemover(String fName)
     {
         preLog(LVL_DEBUG, "Trying to instanciate File for ", fName);
         File src = new File(fName);
         
         if (!(src.exists()))
         {
             resultLog(LOG_FAIL + ": got null pointer");
             throw new IllegalArgumentException("File " + fName + " does not exist!");
         }
         resultLog(LOG_OK);
         
         logPush("Instanciating TIFF handlers for ", fName);
         try
         {
             
             logPush("Instanciating source TIFF handler with string arg");
             srcDng = new TIFFhandler(fName);
             logPop("Done");
             
             logPush("Instanciating destination TIFF handler with string arg");
             dstDng = new TIFFhandler(fName);
             logPop("Done");
         }
         catch (Exception e)
         {
             failed(e.getMessage());
             throw new IllegalArgumentException("Baaaaad file: " + e.getMessage());
         }
         logPop("Done");
         
         srcFileName = fName;
     }
     
     /**
      * Removes the pink dots from the target file
      * 
      * @return true if the dots could be removed, false in case of errors
      */
     public boolean doRemovalInMemory()
     {
         // prepare access to the image data
         // we assume that the TIFF file contains exactly one RAW image...
         logPush("Retrieving CFA IFDs");
         
         ImageFileDirectory ifdSrc = srcDng.getFirstIFDwithCFA();
         if (ifdSrc == null) dbg("Got null for srcDng");
         
         ImageFileDirectory ifdDst = dstDng.getFirstIFDwithCFA();
         if (ifdDst == null) dbg("Got null for srcDng");
         
         logPop("Done");
         
         ifdSrc.dumpInfo();
         
         int w = (int) ifdSrc.imgWidth();
         int h = (int) ifdSrc.imgLen();
         
         // the coordinates were manually picked from a demosaiced file.
         // If the DNG has a border, this border was cropped by the demosaicer
         // and we have to add the cropped border to the coordinates
         int xOffset = (int) ifdSrc.DNG_ActiveArea()[0];
         int yOffset = (int) ifdSrc.DNG_ActiveArea()[1];
         
         // prepare a list of x,y-values representing the distorted dots
         ArrayList<int[]> dotList;
         
         // step 1: get the empirically determined dot coordinates
         logPush("Getting empiric dot pattern");
         dotList = getEmpiricDotPattern(w, h);
         logPop("Done");
         if (dotList == null)
         {
             failed("No empiric dot pattern for image " + w + "x" + h + " available!");
             return false;
         }
         logPush("Starting interpolation of empiric dots");
         interpolPixel(ifdSrc, ifdDst, dotList, 0, 0);
         logPop("Done");
         
         dotList.clear();
         // step 2: get the "regular" or "grid" dot pattern
         logPush("Getting grid dot pattern");
         dotList = getGridDotPattern(w, h);
         logPop("Done");
         if (dotList == null)
         {
             failed("No grid dot pattern for image " + w + "x" + h + " available!");
             return false;
         }
         logPush("Starting interpolation of grid dots");
         interpolPixel(ifdSrc, ifdDst, dotList,  xOffset, yOffset);
         logPop("Done");
         return true;
     }
     
     
     /**
      * Replaces a pixel intensity with an interpolation of the "X"-like neighboring pixels
      * Pixels closer than 2 pixel to the image border can't be interpolated and remain unmodified.
      * 
      * @param ifdSrc ImageFileHandler for the distorted source image data (read)
      * @param ifdDst ImageFileHandler for the improved image data (write)
      * @param dotList a list of x,y-coordinates of the dots to fix
      * @param xOffset offset between the x-coordinates reported from the dot list and the coordinates in the file; compensates for borders
      * @param yOffset offset between the y-coordinates reported from the dot list and the coordinates in the file; compensates for borders
      */
     protected void interpolPixel(ImageFileDirectory ifdSrc, ImageFileDirectory ifdDst, ArrayList<int[]> dotList, int xOffset, int yOffset)
     {
         int w = (int) ifdSrc.imgWidth();
         int h = (int) ifdSrc.imgLen();
         
         for (int[] dot : dotList)
         {
             int x = dot[0] + xOffset;
             int y = dot[1] + yOffset;
             
             // don't fix pixel on image borders
             if ((x < 2) || (x > (w - 3)) || (y < 2) || (y > (h - 3))) continue;
             
             // determine intensity gradients in all four directions
             int g1 = ifdSrc.CFA_getPixel(x, y - 2) - ifdSrc.CFA_getPixel(x, y + 2); // top-down
             int g2 = ifdSrc.CFA_getPixel(x - 2, y) - ifdSrc.CFA_getPixel(x + 2, y); // left-right
             int g3 = ifdSrc.CFA_getPixel(x - 2, y - 2) - ifdSrc.CFA_getPixel(x + 2, y + 2); // top-left, down-right
             int g4 = ifdSrc.CFA_getPixel(x + 2, y - 2) - ifdSrc.CFA_getPixel(x - 2, y + 2); // top-right, down-left
             
             // find the minimum gradient
             g1 = Math.abs(g1);
             g2 = Math.abs(g2);
             g3 = Math.abs(g3);
             g4 = Math.abs(g4);
             
             int minG = Math.min(g1, g2);
             minG = Math.min(minG, g3);
             minG = Math.min(minG, g4);
             
             // use the minimum gradient for interpolation
             double newVal;
             if (minG == g1)
             {
                 newVal = (ifdSrc.CFA_getPixel(x, y - 2) + ifdSrc.CFA_getPixel(x, y + 2)) * 0.5;
             }
             else if (minG == g2)
             {
                 newVal = (ifdSrc.CFA_getPixel(x-2, y) + ifdSrc.CFA_getPixel(x+2, y)) * 0.5;
             }
             else if (minG == g3)
             {
                 newVal = (ifdSrc.CFA_getPixel(x-2, y-2) + ifdSrc.CFA_getPixel(x+2, y+2)) * 0.5;
             }
             else
             {
                 newVal = (ifdSrc.CFA_getPixel(x+2, y-2) + ifdSrc.CFA_getPixel(x-2, y+2)) * 0.5;
             }
 
 
             ifdDst.CFA_setPixel(x, y, (int) newVal);
         }
         
     }
     
     
     /**
      * Writes the contents of the destination image to a DNG file. The filename
      * is constructed from the original filename plus a leading underscore.
      * Existing files will be overwritten.
      * 
      * @return the name and (possibly) path of the destination file
      */
     public String writeResultToFile()
     {
         preLog(LVL_DEBUG, "Trying to instanciate Path for ", srcFileName);
         Path srcPath = Paths.get(srcFileName);
         if (srcPath == null) resultLog((LOG_FAIL));
         else resultLog(LOG_OK);
         
         String fName = srcPath.getFileName().toString();
         dbg("fName = ", fName);
        String pName = srcPath.getParent().normalize().toString();
         dbg("pName = ", pName);
         
         preLog(LVL_DEBUG, "Trying to instanciate Path for destination file");
         Path dstPath = Paths.get(pName, "_" + fName);
         if (dstPath == null) resultLog((LOG_FAIL));
         else resultLog(LOG_OK);
                 
         logPush("Calling dstDng.saveAs() with Path parameter ", dstPath);
         dstDng.saveAs(dstPath);
         logPop("Done");
         
         dbg("File saved successfully");
         
         return dstPath.toString();
     }
     
     /**
      * Retrieves the coordinates for the regular, grid-like dots for a specific image size
      * 
      * @param w image width
      * @param h image height
      * @return a list of (x, y)-coordinates or null if no data for the requested image size is available
      */
     protected ArrayList<int[]> getGridDotPattern(int w, int h)
     {
         int[][] gridData = null;
         
         if ((w == 1280) && (h == 720))
         {
             gridData = new int[][] {
                 {511, 213, 767, 263, 8, 10},
                 {507, 219, 763, 269, 8, 10},
                 {504, 234, 760, 304, 8, 10},
                 {504, 334, 760, 404, 8, 10},
                 {508, 338, 764, 408, 8, 10},
                 {511, 413, 767, 463, 8, 10},
                 {507, 419, 763, 469, 8, 10},
                 {504, 434, 760, 504, 8, 10},
                 {508, 238, 764, 308, 8, 10},
                 {312, 274, 496, 284, 8, 10},
                 {316, 278, 500, 288, 8, 10},
                 {768, 274, 952, 284, 8, 10},
                 {768, 284, 956, 288, 8, 10},
                 {319, 313, 359, 323, 8, 10},
                 {315, 319, 955, 329, 8, 10},
                 {511, 333, 767, 363, 8, 10},
                 {507, 339, 763, 369, 8, 10},
                 
                 {312, 354, 496, 364, 8, 10},
                 {316, 358, 500, 368, 8, 10},
                 {312, 394, 496, 404, 8, 10},
                 {316, 398, 500, 408, 8, 10},
                 {312, 434, 496, 444, 8, 10},
                 {316, 438, 500, 448, 8, 10},
                 {319, 433, 503, 443, 8, 10},
                 {315, 439, 499, 449, 8, 10},
                 
                 {312+456, 354, 496+456, 364, 8, 10},
                 {316+456, 358, 500+456, 368, 8, 10},
                 {312+456, 394, 496+456, 404, 8, 10},
                 {316+456, 398, 500+456, 408, 8, 10},
                 {312+456, 434, 496+456, 444, 8, 10},
                 {316+456, 438, 500+456, 448, 8, 10},
                 {319+456, 433, 503+456, 443, 8, 10},
                 {315+456, 439, 499+456, 449, 8, 10},
                 {316+456, 278, 500+456, 288, 8, 10},
                 
                 {508, 438, 764, 508, 8, 10},
                 {367, 313, 959, 323, 8, 10},
                 {775, 353, 959, 363, 8, 10},
                 {771, 359, 955, 369, 8, 10},
                 
                 {319, 273, 959, 283, 8, 10},
                 {315, 279, 955, 289, 8, 10},
                 
                 {511, 293, 767, 303, 8, 10},
                 {507, 299, 763, 309, 8, 10},
                 
                 {319, 393, 959, 403, 8, 10},
                 {315, 399, 955, 409, 8, 10},
                 
                 {511, 493, 767, 503, 8, 10},
                 {507, 499, 763, 509, 8, 10},
                 
                 {319, 353, 503, 363, 8, 10},
                 {315, 359, 499, 369, 8, 10},
                 
                 {504, 214, 760, 224, 8, 10},
                 {508, 218, 764, 228, 8, 10},
                 
                 {312, 314, 952, 324, 8, 10},
                 {308, 318, 956, 328, 8, 10},
                 
                 {496, 414, 760, 424, 8, 10},
                 {516, 418, 764, 428, 8, 10},
                 
                 {511, 373, 767, 383, 8, 10},
                 {507, 379, 763, 389, 8, 10},
                 
                 {511, 473, 767, 483, 8, 10},
                 {507, 479, 763, 489, 8, 10}
             };
         }
         
         if ((w == 1808) && (h == 727))
         {
             gridData = new int[][] {
                 {740, 262, 996, 430, 8, 12},
                 {736, 268, 992, 436, 8, 12},
                 //{739, 299, 995, 371, 8, 12},
                 {739, 299, 995, 311, 8, 12},
                 {739, 359, 995, 371, 8, 12},
                 {735, 305, 991, 317, 8, 12},
                 {735, 365, 991, 377, 8, 12},
                 
                 {548, 310, 732, 310, 8, 12},
                 {544, 316, 728, 316, 8, 12},
                 {547, 311, 731, 311, 8, 12},
                 {543, 317, 727, 317, 8, 12},
                 
                 {548, 334, 732, 358, 8, 12},
                 {544, 340, 728, 364, 8, 12},
                 
                 {548, 382, 1188, 382, 8, 12},
                 {544, 388, 1184, 388, 8, 12},
                 
                 {548+456, 310, 732+456, 310, 8, 12},
                 {544+456, 316, 728+456, 316, 8, 12},
                 {547+456, 311, 731+456, 311, 8, 12},
                 {543+456, 317, 727+456, 317, 8, 12},
                 
                 {548+456, 334, 732+456, 358, 8, 12},
                 {544+456, 340, 728+456, 364, 8, 12},
                 
                 {547, 335, 1187, 359, 8, 12},
                 {543, 341, 1183, 365, 8, 12},
                 
                 {739, 263, 995, 287, 8, 12},
                 {735, 269, 991, 293, 8, 12},
                 {739, 323, 995, 323, 8, 12},
                 {735, 329, 991, 329, 8, 12},
                 
                 {547, 383, 1187, 383, 8, 12},
                 {543, 389, 1183, 398, 8, 9},
                 
                 {735, 401, 991, 437, 8, 12},
                 
                 {739, 395, 995, 431, 8, 12},
                 
                 
                 
             };
         
         }
         
         // convert the grid data into single dot coordinates
         if (gridData == null) return null;
         
         ArrayList<int[]> result = new ArrayList();
         for (int[] gridBlock : gridData)
         {
             int minX = gridBlock[0];
             int minY = gridBlock[1];
             int maxX = gridBlock[2];
             int maxY = gridBlock[3];
             int stepX = gridBlock[4];
             int stepY = gridBlock[5];
 
             // "unwrap" the gridd
             for (int y = minY; y <= maxY; y += stepY)
             {
                 for (int x = minX; x <= maxX; x += stepX)
                 {
                     // add the regular coordinates to the empiric coordinates from before
                     result.add(new int[] {x, y});
                 }
             }
         }
             
         return result;
     }
     
     /**
      * Reads "empiric" dot coordinates stored in a text file in the CLASSPATH
      * 
      * @param w image width
      * @param h image height
      * @return a list of (x, y)-coordinates or null if no data for the requested image size is available
      */
     protected ArrayList<int[]> getEmpiricDotPattern(int w, int h)
     {
         ArrayList<int[]> result = null;
         
         String resName = "";
         
         
         preLog(LVL_DEBUG, "Trying to determine resource name");
         if ((w == 1280) && (h == 720))
         {
             resName = "res/pixCoord_threshold2068.txt";
         }
         else if ((w == 1808) && (h == 727))
         {
             resName = "res/pixCoord_SilentPic_threshold2085.txt";
         }
         
         if (resName.length() == 0)
         {
             resultLog(LOG_FAIL);
             return null;
         }
         
         resultLog(LOG_OK);
         dbg("Resource name is ", resName);
             
         logPush("Reading and parsing resource");
         try
         {
             // open the dot data file as a resource (e. g. in the JAR file)
             preLog(LVL_DEBUG, "getResourceAsStream");
             InputStream in = this.getClass().getResourceAsStream(resName);
             if (in == null) resultLog(LOG_FAIL);
             else resultLog(LOG_OK);
             
             preLog(LVL_DEBUG, "instanciate InputStreamReader");
             InputStreamReader ir = new InputStreamReader(in);
             if (ir == null) resultLog(LOG_FAIL);
             else resultLog(LOG_OK);
             
             preLog(LVL_DEBUG, "instanciate BufferedReader");
             BufferedReader b  = new BufferedReader(ir);
             if (b == null) resultLog(LOG_FAIL);
             else resultLog(LOG_OK);
 
             result = new ArrayList();
 
             // read the file line-by-line and convert the ASCII-text into numbers
             String line;
             while ((line = b.readLine()) != null)
             {
                 int x = Integer.parseInt(line.split(",")[0].trim());
                 int y = Integer.parseInt(line.split(",")[1].trim());
                 result.add(new int[] {x, y});
             }
         }
         catch (Exception e)
         {
             failed("Exception in getEmpiricDotPattern: ", e.getMessage());
         }
         logPop("Done");
             
         return result;
     }
     
 }
