 package ua.pp.fland.yearly.essay.identif.task3.model;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ua.pp.fland.yearly.essay.identif.task3.model.beans.DirectFlowData;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Maxim Bondarenko
  * @version 1.0 11/24/11
  */
 
 public class ImplicitFiniteDifferenceMethod {
 
     private static final Logger log = LoggerFactory.getLogger(ImplicitFiniteDifferenceMethod.class);
 
     private final double startTemp;
 
     private final double environmentTemp;
 
     private final double wallThickness;
 
     private final double heatIrradiationCoeff;
 
     private final double xStep;
 
     private final double timeStep;
 
     private final double endTime;
 
     private final static int X_STEP_SCALE = 4;
 
     private final double lambda;
 
     private final double a;
 
     private final long n;
 
    private final static double DOUBLE_DELTA = 0.00001;
 
     public ImplicitFiniteDifferenceMethod(double startTemp, double environmentTemp, double wallThickness,
                                           double heatIrradiationCoeff, double xStep, double a, double lambda,
                                           double timeStep, double endTime) {
         this.startTemp = startTemp;
         this.environmentTemp = environmentTemp;
         this.wallThickness = wallThickness;
         this.heatIrradiationCoeff = heatIrradiationCoeff;
 
         this.xStep = xStep;
         this.a = a;
         this.lambda = lambda;
 
         this.timeStep = timeStep;
         this.endTime = endTime;
 
         n = Math.round(wallThickness / xStep);
     }
 
     public Map<Double, Map<BigDecimal, Double>> calculate() {
        log.debug("Calculating started...");

         Map<Double, Map<BigDecimal, Double>> calculatedTemp = new HashMap<Double, Map<BigDecimal, Double>>();
         Map<BigDecimal, Double> calculatedXTemp = new HashMap<BigDecimal, Double>(getStartTimeTemp(startTemp));
         double currTime = 0.0;
         calculatedTemp.put(currTime, calculatedXTemp);
         currTime = currTime + timeStep;
 
         final double s = (xStep * xStep) / (timeStep * a);
 
        for (; currTime <= endTime; currTime = currTime + timeStep) {
             calculatedXTemp = new HashMap<BigDecimal, Double>(getNextTimeTemp(calculatedXTemp, s));
             calculatedTemp.put(currTime, calculatedXTemp);
         }
 
        log.debug("Calculating finished.");

         return calculatedTemp;
     }
 
     private Map<BigDecimal, Double> getNextTimeTemp(Map<BigDecimal, Double> prevTimeTemp, final double s) {
         Map<BigDecimal, Double> calculatedXTemp = new HashMap<BigDecimal, Double>();
 
         Map<Long, DirectFlowData> directFlowDataMap = new HashMap<Long, DirectFlowData>();
         double a = 1;
         double b = -1;
         double d = 0;
         double c = 0;
         double f = -a / b;
         double g = -d / b;
         directFlowDataMap.put(0L, new DirectFlowData(c, b, d, f, g));
 
         double currX = 0.0 + xStep;
         long i;
         for (i = 1; i < n; i++) {
             BigDecimal currXBigDecimal = new BigDecimal(currX).setScale(X_STEP_SCALE, RoundingMode.HALF_UP);
             c = 1;
             a = 1;
             b = -(2 + s);
             d = s * prevTimeTemp.get(currXBigDecimal);
             double prevF = directFlowDataMap.get(i - 1).getF();
             double prevG = directFlowDataMap.get(i - 1).getG();
 
             f = -a / (c * prevF + b);
             g = -(d + c * prevG) / (c * prevF + b);
             directFlowDataMap.put(i, new DirectFlowData(c, b, d, f, g));
             currX = currX + xStep;
         }
 //        i = i + 1;
         b = 1 + (heatIrradiationCoeff * xStep) / lambda;
         a = 0;
         c = -1;
         d = -environmentTemp * heatIrradiationCoeff * xStep / lambda;
         double prevF = directFlowDataMap.get(i - 1).getF();
         double prevG = directFlowDataMap.get(i - 1).getG();
         f = -a / (c * prevF + b);
         g = -(d + c * prevG) / (c * prevF + b);
         directFlowDataMap.put(i, new DirectFlowData(c, b, d, f, g));
 
         currX = wallThickness;
         BigDecimal currXBigDecimal = new BigDecimal(currX).setScale(X_STEP_SCALE, RoundingMode.HALF_UP);
         c = directFlowDataMap.get(i).getC();
         b = directFlowDataMap.get(i).getB();
         d = directFlowDataMap.get(i).getD();
         g = directFlowDataMap.get(i - 1).getG();
         f = directFlowDataMap.get(i - 1).getF();
         double prevTemp = -(c * g + d) / (b + c * f);
         calculatedXTemp.put(currXBigDecimal, prevTemp);
         currX = currX - xStep;
        i = i - 1;
        for (; i >= 0; i--) {
             currXBigDecimal = new BigDecimal(currX).setScale(X_STEP_SCALE, RoundingMode.HALF_UP);
             f = directFlowDataMap.get(i).getF();
             g = directFlowDataMap.get(i).getG();
             double currTemp = f * prevTemp + g;
             calculatedXTemp.put(currXBigDecimal, currTemp);
             prevTemp = currTemp;
             currX = currX - xStep;
         }
 
         return calculatedXTemp;
     }
 
     private Map<BigDecimal, Double> getStartTimeTemp(double startTemp) {
         Map<BigDecimal, Double> startXTemp = new HashMap<BigDecimal, Double>();
 
         double currXPos = 0;
         for (; currXPos <= wallThickness + DOUBLE_DELTA; currXPos = currXPos + xStep) {
             BigDecimal bigDecimalXPos = new BigDecimal(currXPos).setScale(X_STEP_SCALE, RoundingMode.HALF_UP);
             startXTemp.put(bigDecimalXPos, startTemp);
         }
 
         return startXTemp;
     }
 }
