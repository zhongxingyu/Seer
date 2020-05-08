 package pvm.KernelProducts;
 
 import org.apache.commons.lang3.Validate;
 
 import java.security.InvalidParameterException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Andrei
  * Date: 1/4/13
  * Time: 7:22 PM
  * To change this template use File | Settings | File Templates.
  */
 public class KernelProductPoly extends KernelProduct {
 
     static int powerIntInitialCenter = 3;
     static int powerIntInitialRange = 2;
     static int powerIntMaxSteps = 5;
 
     static double productOffsetInitialCenter = 0.5;
     static double productOffsetInitialRange = 0.5;
     static int productOffsetMaxSteps = 2;
 
     int iPow;
     double prodOffset;
     KernelProductScalar kerScalar;
 
     public KernelProductPoly(){
         kerType = KernelProductManager.KerType.KERPOLY;
         kerScalar = new KernelProductScalar();
     }
 
     public double computeKerProd(double [] x0, double [] x1){
         double scalar = kerScalar.computeKerProd(x0, x1);
 
         return Math.pow(scalar + prodOffset, iPow);
     }
 
     public void setParamInt(int paramInt){
         Validate.isTrue(paramInt > 0, "The power should always be positive");
         iPow = paramInt;
     }
     public void setParamDouble(double paramDouble){
         Validate.isTrue(paramDouble >= 0, "The scalar product offset should always be positive");
         prodOffset = paramDouble;
     }
 
     public int getPreferedCenterInt(){return powerIntInitialCenter;}
     public double getPreferedCenterDouble(){return productOffsetInitialCenter;}
 
     public int getLowerBoundInt(int centerParamInt){
        int ret = centerParamInt - powerIntInitialRange / refinementLvl;
        if (ret < 1)
            ret = 1;

        return ret;
     }
     public int getUpperBoundInt(int centerParamInt){
        int ret = centerParamInt + powerIntInitialRange / refinementLvl;

        if (ret > powerIntInitialCenter + powerIntInitialRange)
            ret = powerIntInitialCenter + powerIntInitialRange;

        return ret;
     }
 
     public double getLowerBoundDouble(double centerParamDouble){
         double ret = centerParamDouble - productOffsetInitialRange / refinementLvl;
         if (ret < 0)
             ret = 0;
         if (ret > 1)
             ret = 1;
         return ret;
     }
     public double getUpperBoundDouble(double centerParamDouble){
         double ret = centerParamDouble + productOffsetInitialRange / refinementLvl;
         if (ret < 0)
             ret = 0;
         if (ret > 1)
             ret = 1;
         return ret;
     }
 
     public int getMaxStepsInt(){
         return powerIntMaxSteps / refinementLvl;
     }
     public int getMaxStepsDouble(){
         return productOffsetMaxSteps / refinementLvl;
     }
 
     public int getParamValueInt(int lowerBound, int upperBound, int maxSteps, int stepIndex){
         double interval = (double)(upperBound - lowerBound);
 
         if (maxSteps < 2)
             return lowerBound;
 
         Validate.isTrue(stepIndex < maxSteps, "StepIndex should not be equal or greater to maxSteps");
 
         interval /= (double)(maxSteps - 1);
         return  (int)(lowerBound + interval * stepIndex + 0.5);
     }
     public double getParamValueDouble(double lowerBound, double upperBound, int maxSteps, int stepIndex){
         double interval = upperBound - lowerBound;
 
         if (maxSteps < 2)
             return lowerBound;
 
         Validate.isTrue(stepIndex < maxSteps, "StepIndex should not be equal or greater to maxSteps");
 
         interval /= (double)(maxSteps - 1);
         return  lowerBound + (interval * stepIndex);
     }
 }
