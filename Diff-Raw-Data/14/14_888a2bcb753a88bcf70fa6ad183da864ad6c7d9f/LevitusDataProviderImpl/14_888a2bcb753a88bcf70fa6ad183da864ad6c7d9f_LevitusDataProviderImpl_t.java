 package org.esa.beam.waterradiance.levitus;
 
 import org.esa.beam.framework.datamodel.Band;
 import org.esa.beam.framework.datamodel.GeoCoding;
 import org.esa.beam.framework.datamodel.GeoPos;
 import org.esa.beam.framework.datamodel.PixelPos;
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.framework.datamodel.ProductData;
 import org.esa.beam.util.math.MathUtils;
 import org.esa.beam.waterradiance.AuxdataProvider;
 
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * @author Marco Peters
  */
 public class LevitusDataProviderImpl implements AuxdataProvider {
 
     public static final int LEVITUS_CENTER_DAY = 16;
     private GeoCoding salinityGeoCoding;
     private GeoCoding temperatureGeoCoding;
     private final Product salinityProduct;
     private Product tempProduct;
 
     /**
      * Creates an instance of this <code></>LevitusDataProvider</code> implementation.
      *
      * @param salinityProduct the product containing the salinity data
      * @param tempProduct     the product containing the temperature data
      */
     public LevitusDataProviderImpl(Product salinityProduct, Product tempProduct) {
         this.salinityProduct = salinityProduct;
         this.tempProduct = tempProduct;
         salinityGeoCoding = this.salinityProduct.getGeoCoding();
         temperatureGeoCoding = this.tempProduct.getGeoCoding();
     }
 
    private static boolean productContainsPixel(Product product, int x, int y) {
        return x >= 0 && x < product.getSceneRasterWidth() &&
                y >= 0 && y < product.getSceneRasterHeight();
    }

     public double getSalinity(Date date, double lat, double lon) throws Exception {
         DateDependentValues dateDependentValues = new DateDependentValues(date);
 
         PixelPos pixelPos = salinityGeoCoding.getPixelPos(new GeoPos((float) lat, (float) lon), null);
         int x = MathUtils.floorInt(pixelPos.x);
         int y = MathUtils.floorInt(pixelPos.y);
        if (!productContainsPixel(salinityProduct, x, y)) {
            return Double.NaN;
        }
         Band lowerBand = salinityProduct.getBandAt(dateDependentValues.lowerMonth);
         Band upperBand = salinityProduct.getBandAt(dateDependentValues.upperMonth);
         double[] lowPixel = new double[1];
         double[] upperPixel = new double[1];
         lowerBand.readPixels(x, y, 1, 1, lowPixel);
         upperBand.readPixels(x, y, 1, 1, upperPixel);
         return interpolate(lowPixel[0], upperPixel[0], dateDependentValues.linearFraction);
     }
 
     public double getTemperature(Date date, double lat, double lon) throws Exception {
         DateDependentValues dateDependentValues = new DateDependentValues(date);
 
         PixelPos pixelPos = temperatureGeoCoding.getPixelPos(new GeoPos((float) lat, (float) lon), null);
         int x = MathUtils.floorInt(pixelPos.x);
         int y = MathUtils.floorInt(pixelPos.y);
        if (!productContainsPixel(tempProduct, x, y)) {
            return Double.NaN;
        }

         Band lowerBand = tempProduct.getBandAt(dateDependentValues.lowerMonth);
         Band upperBand = tempProduct.getBandAt(dateDependentValues.upperMonth);
         double[] lowPixel = new double[1];
         double[] upperPixel = new double[1];
         lowerBand.readPixels(x, y, 1, 1, lowPixel);
         upperBand.readPixels(x, y, 1, 1, upperPixel);
         return interpolate(lowPixel[0], upperPixel[0], dateDependentValues.linearFraction);
     }
 
     static double interpolate(double lowerValue, double upperValue, double fraction) {
         return (lowerValue * (1 - fraction) + upperValue * fraction);
     }
 
     static double calculateLinearFraction(Calendar calendar) {
         double day = calendar.get(Calendar.DAY_OF_MONTH);
         if (day < LEVITUS_CENTER_DAY) {
             day = day + (LEVITUS_CENTER_DAY - 1);
         } else if (day >= LEVITUS_CENTER_DAY) {
             day = day - LEVITUS_CENTER_DAY;
         }
         return day / calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
     }
 
     static int calculateUpperMonth(int day, int month) {
         int upperMonth = day >= 16 ? month + 1 : month;
         return upperMonth > 11 ? 0 : upperMonth;
     }
 
     static int calculateLowerMonth(int day, int month) {
         int lowerMonth = day < 16 ? month - 1 : month;
         return lowerMonth < 0 ? 11 : lowerMonth;
     }
 
     private static class DateDependentValues {
 
         private double linearFraction;
         private final int lowerMonth;
         private final int upperMonth;
 
         private DateDependentValues(Date date) {
             Calendar calendar = ProductData.UTC.create(date, 0).getAsCalendar();
             linearFraction = calculateLinearFraction(calendar);
             int day = calendar.get(Calendar.DAY_OF_MONTH);
             int month = calendar.get(Calendar.MONTH);
             lowerMonth = calculateLowerMonth(day, month);
             upperMonth = calculateUpperMonth(day, month);
         }
     }
 
 }
