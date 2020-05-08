 package org.dotplot.image;
 
 import org.apache.log4j.Logger;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.dotplot.fmatrix.ITypeTableNavigator;
 import org.dotplot.fmatrix.Match;
 import org.dotplot.ui.configuration.GlobalConfiguration;
 
 /**
  * Provides the Information Mural algorithm. It can be used for efficient scaling and saving memory.
  */
 class InformationMural
 {
    private final static Logger logger = Logger.getLogger(InformationMural.class.getName());
 
    static void getMural(ITypeTableNavigator navData, Dimension _targetSize, ImageCallback image)
    {
       Dimension originalSize = navData.getSize();
       int minLen = Math.min(_targetSize.width, _targetSize.height);
       Dimension targetSize = new Dimension(minLen, minLen);
       QImageConfiguration config = (QImageConfiguration) GlobalConfiguration.getInstance().get(
             GlobalConfiguration.KEY_IMG_CONFIGURATION);
 
       logger.debug("size " + originalSize.width + "->" + targetSize.width);
 
       // initialize the mural array
       int[][] mural_array = initMuralArray(targetSize);
 
       // fill mural array including scaling (on-the-fly)
       float max_mural_array_value = fillMuralArray(targetSize, originalSize, navData, mural_array);
 
       logger.debug("MaxMural: " + max_mural_array_value);
 
 //      transferToCSV(mural_array, new File("infomural.csv"));
 
       // fill image with mural values
       transferToImage(mural_array, image, max_mural_array_value, config);
    }
 
    /**
     * Exports the values from the mural_array into a file, each value separated by comma.
     * Used for debugging only.
     */
    private static void transferToCSV(int[][] mural_array, File target)
    {
       try
       {
          OutputStream fout = new FileOutputStream(target);
          for (int x = 0; x < mural_array.length; x++)
          {
             for (int y = 0; y < mural_array[x].length; y++)
             {
                fout.write((mural_array[x][y] + ",").getBytes());
             }
             fout.write('\n');
          }
          fout.flush();
          fout.close();
       }
       catch (FileNotFoundException e)
       {
          e.printStackTrace();
       }
       catch (IOException e)
       {
          e.printStackTrace();
       }
    }
 
    private static void transferToImage(
          int[][] mural_array, ImageCallback image, float max_mural_array_value, QImageConfiguration config)
    {
       logger.debug("converting to image...");
       int col;
       float maxColVal = Util.COLOR_COUNT_PER_BAND - 1;
 
       // cached values to improve performance
       int colBackground = config.getLutBackground().getRGB();
       int colForeground = config.getLutForeground().getRGB();
       int[][] lut = config.getLut();
 
       for (int x = 0; x < mural_array.length; x++)
       {
          for (int y = 0; y < mural_array[x].length; y++)
          {
             if (mural_array[x][y] == 0)
             {
                image.setPixel(x, y, colBackground);
             }
             else if (mural_array[x][y] == max_mural_array_value)
             {
                image.setPixel(x, y, colForeground);
             }
             else
             {
                col = (int) ((float) mural_array[x][y] / (float) max_mural_array_value * (float) maxColVal);
                image.setPixel(x, y, new Color(lut[0][col], lut[1][col], lut[2][col]).getRGB());
             }
          }
 
          if ((x % 100) == 0)
          {
             logger.debug("(" + x + "/" + mural_array.length + ")...");
          }
       }
    }
 
    private static float fillMuralArray(
          Dimension targetSize, Dimension originalSize, ITypeTableNavigator navData, int[][] mural_array)
    {
       logger.debug("reading data...");
       long counter = 0;
       long divider = (originalSize.width * originalSize.height) / 100;
 
       Match p;
       int mural_x, mural_y;
       float max_mural_array_value = 0;
 
       float xFactor = (float) targetSize.width / (float) originalSize.width;
       float yFactor = (float) targetSize.height / (float) originalSize.height;
 
       while ((p = navData.getNextMatch()) != null)
       {
          // calculate target indices
          mural_x = (int) ((float) p.getX() * xFactor);
          mural_y = (int) ((float) p.getY() * yFactor);
 
          // algorithm uses boolean values
          mural_array[mural_x][mural_y]++;
 
          // update max_value
          if (mural_array[mural_x][mural_y] > max_mural_array_value)
          {
             max_mural_array_value = mural_array[mural_x][mural_y];
          }
 
          counter++;
         if (logger.isDebugEnabled() && ((counter % divider) == 0))
          {
             logger.debug("matches processed: " + counter);
          }
       }
       return max_mural_array_value;
    }
 
    private static int[][] initMuralArray(Dimension targetSize)
    {
       logger.debug("init with size " + targetSize);
       int[][] mural_array = new int[targetSize.width][targetSize.height];
       for (int i = 0; i < targetSize.width; i++)
       {
          for (int j = 0; j < targetSize.height; j++)
          {
             mural_array[i][j] = 0;
          }
       }
       return mural_array;
    }
 }
