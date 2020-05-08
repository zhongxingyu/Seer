 package com.tzapps.tzpalette.data;
 
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.util.Log;
 
 import com.tzapps.tzpalette.algorithm.ClusterCenter;
 import com.tzapps.tzpalette.algorithm.ClusterPoint;
 import com.tzapps.tzpalette.algorithm.KMeansProcessor;
 import com.tzapps.utils.BitmapUtils;
 import com.tzapps.utils.ColorUtils;
 
 public class PaletteDataHelper
 {
     private static final String TAG = "PaletteDataHelper";
     
     private static final int THUMB_MAX_WIDTH  = 500;
     private static final int THUMB_MAX_HEIGHT = 500;
      
     public enum PaletteDataHelper_DataType
     {
         ColorToRGB,
         ColorToHSV,
         ColorToHSL
     };
     
     private static PaletteDataHelper instance;
     
     private Context mContext;
     private PaletteDataSource mDataSource;
     
     private PaletteDataHelper(Context context)
     {
         mContext = context;
         mDataSource = new PaletteDataSource(context);
     };
     
     public static PaletteDataHelper getInstance(Context context)
     {
         if (instance == null)
             instance = new PaletteDataHelper(context);
         
         return instance;
     }
     
     public void delete(PaletteData data)
     {
         mDataSource.open(true);
         
         mDataSource.delete(data);
         
         mDataSource.close();
     }
     
     public void delete(long id)
     {
         mDataSource.open(true);
         
         mDataSource.delete(id);
         
         mDataSource.close();
     }
     
     public void add(PaletteData data)
     {
         mDataSource.open(true);
         
        mDataSource.save(data);
         
         mDataSource.close();
     }
     
     public void update(PaletteData data, boolean updateThumb)
     {
         mDataSource.open(true);
         
         mDataSource.update(data, updateThumb);
         
         mDataSource.close();
     }
     
     public List<PaletteData> getAllData()
     {
         List<PaletteData> dataList = null;
         
         mDataSource.open(false);
         
         dataList = mDataSource.getAllPaletteData();
         
         mDataSource.close();
         
         return dataList;
     }
 
     public void analysis(PaletteData data, boolean reset)
     {
         analysis(data, reset, 8, 5, PaletteDataHelper_DataType.ColorToHSL);
     }
     
     /**
      * Analysis the thumb in palette data to pick up its main colors
      * automatically.
      * 
      * @param data          the PaletteData data to analysis
      * @param reset         flag to indicate if remove the existing colors
      * @param numOfColors   number of colors to pick up (default value is 10)
      * @param deviation     deviation to control how precise the analysis it is (the default value is 5)
      * @param dataType      the color type (RGB or HSV) when do the color analysis
      */
     public void analysis(PaletteData data, boolean reset, int numOfColors, int deviation, PaletteDataHelper_DataType dataType)
     {
         Log.d(TAG, "palette data analysis()");
         
         Bitmap bitmap = data.getThumb();
         
         assert(bitmap != null);
         
         if (bitmap == null)
         {
             Log.e(TAG, "palette data doesn't have a thumb!");
             return;
         }
         
         if (reset)
             data.clearColors();
         
         // initialization the pixel data
         int width = bitmap.getWidth();
         int height = bitmap.getHeight();
         
         // scale the bitmap to limit the k-mean processor with a reasonable process time
         if (width > THUMB_MAX_WIDTH || height > THUMB_MAX_HEIGHT)
         {
             bitmap = BitmapUtils.resizeBitmapToFitFrame(bitmap, THUMB_MAX_WIDTH, THUMB_MAX_HEIGHT);
             width = bitmap.getWidth();
             height = bitmap.getHeight();
         }
         
         int[] inPixels = new int[width*height];
         bitmap.getPixels(inPixels, 0, width, 0, 0, width, height);
         
         ClusterPoint[] points = new ClusterPoint[inPixels.length];
         for (int i = 0; i < inPixels.length; i++)
             points[i] = new ClusterPoint(convertColor(inPixels[i], dataType));
         
         KMeansProcessor proc = new KMeansProcessor(numOfColors, deviation, /*maxRound*/99);   
         proc.processKMean(points);
         
         for (ClusterCenter center : proc.getClusterCenters())
         {
             int[] values = center.getValues();
             
             int color = 0;
             
             switch(dataType)
             {
                 case ColorToRGB:
                     color = ColorUtils.rgbToColor(values);
                     break;
                 
                 case ColorToHSV:
                     color = ColorUtils.hsvToColor(values);
                     break;
                     
                 case ColorToHSL:
                     color = ColorUtils.hslToColor(values);
                     break;
             }
             
             data.addColor(color);
         }
     }
     
     /**
      * Convert the color value into Cluster point values
      */
     private int[] convertColor(int color, PaletteDataHelper_DataType type)
     {
         switch(type)
         {
             case ColorToRGB:
                 return ColorUtils.colorToRGB(color);
                 
             case ColorToHSV:
                 return ColorUtils.colorToHSV(color);
                 
             case ColorToHSL:
                 return ColorUtils.colorToHSL(color);
                
             default:
                 int[] values = new int[1];
                 values[0] = color;
                 return values;
         }
     }
     
 }
