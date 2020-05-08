 /**
  * 
  */
 package cz.kalcik.vojta.terraingis.layer;
 
 import android.graphics.Color;
 import android.graphics.Paint;
 
 /**
  * @author jules
  *
  */
 public class DefaultPaints
 {
     // constants ===================================================================
     private static final float LINE_WIDTH = 2;
     private static final float HUE_STEP = 80;
     private static final float VALUE_STEP = 0.2f;
     private static final float MIN_VALUE = 0.3f;
    private static final float[] DEFAULT_HSV = {0, 1, 1};
     
     // static atributes ============================================================
    private static float[] mCurrentHSV = DEFAULT_HSV.clone();
     
     // public static methods =======================================================
     /**
      * default paint for line
      * @return
      */
     public static Paint getLine()
     {
         Paint result = commonAttrs();
         result.setStrokeWidth(LINE_WIDTH);
         
         return result;
     }
     
     /**
      * default paint for polygon
      * @return
      */
     public static Paint getPolygon()
     {
         Paint result = commonAttrs();
         
         return result;
     }
        
     /**
      * default paint for point
      * @return
      */
     public static Paint getPoint()
     {
         Paint result = commonAttrs();
         
         return result;
    }
    
    /**
     * set default colors
     */
    public static void resetColors()
    {
        mCurrentHSV = DEFAULT_HSV.clone();
    }
     // private static methods ======================================================
     /**
      * common settings of paint
      * @param paint
      * @return
      */
     private static Paint commonAttrs()
     {
         Paint result = new Paint();
         result.setAntiAlias(true);
         result.setColor(getColor());
         
         return result;
     }
     
     /**
      * change color
      * @return
      */
     private static int getColor()
     {
         int result = Color.HSVToColor(mCurrentHSV);
         
         mCurrentHSV[0] += HUE_STEP;
         if(mCurrentHSV[0] > 359f)
         {
             mCurrentHSV[0] -= 360;
             mCurrentHSV[2] -= VALUE_STEP;
             if(mCurrentHSV[2] < MIN_VALUE)
             {
                 mCurrentHSV[2] = 1;
             }
         }
         
         return result;
     }
 }
