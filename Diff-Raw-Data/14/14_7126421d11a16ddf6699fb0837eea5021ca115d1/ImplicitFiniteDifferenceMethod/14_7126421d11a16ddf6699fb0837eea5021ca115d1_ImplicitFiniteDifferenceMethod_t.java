 package ua.pp.fland.labs.identif.lab6.model;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ua.pp.fland.labs.identif.lab6.model.beans.DirectFlowData;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Maxim Bondarenko
  * @version 1.0 11/7/11
  */
 
 public class ImplicitFiniteDifferenceMethod {
 
     private static final Logger log = LoggerFactory.getLogger(ImplicitFiniteDifferenceMethod.class);
 
     private final static double DOUBLE_PRECISION = 0.0001;
 
     private final Map<BigDecimal, Double> xStartTemp;
 
     private final double xStep;
 
     private final long n;
 
     private final Double timeStep;
 
     private final double xi;
 
     private final int xValuesScale;
 
     private final double x0;
 
     private final double endTime;
 
    public ImplicitFiniteDifferenceMethod(Map<BigDecimal, Double> xStartTemp, double xStep, double timeStep,
                                          double endTime, double x0, double xi, int xValuesScale) {
         this.xStartTemp = xStartTemp;
         this.xStep = xStep;
         this.timeStep = timeStep;
         this.endTime = endTime;
         this.xi = xi;
         this.x0 = x0;
         this.xValuesScale = xValuesScale;
         n = Math.round(Collections.max(xStartTemp.keySet()).doubleValue() / xStep);
     }
 
     public Map<Double, Map<BigDecimal, Double>> calculate() {
         Map<Double, Map<BigDecimal, Double>> calculatedTemp;
         calculatedTemp = new HashMap<Double, Map<BigDecimal, Double>>();
 
         double startTimeSec = 0d;
         calculatedTemp.put(startTimeSec, xStartTemp);
         double currTimeSec = startTimeSec + timeStep;
         double endTimeSec = endTime;
         Map<BigDecimal, Double> timeTemp = new HashMap<BigDecimal, Double>(getTempAtTime(xStartTemp, x0, xi));
         calculatedTemp.put(currTimeSec, timeTemp);
         currTimeSec = currTimeSec + timeStep;
         for (; currTimeSec < endTimeSec; currTimeSec = currTimeSec + timeStep) {
 //            log.debug("Curr time: " + currTimeSec);
             timeTemp = new HashMap<BigDecimal, Double>(getTempAtTime(timeTemp, x0, xi));
             calculatedTemp.put(currTimeSec, timeTemp);
         }
 
         return calculatedTemp;
     }
 
     private Map<BigDecimal, Double> getTempAtTime(Map<BigDecimal, Double> prevTimeXValues, double u0j, double xi) {
         Map<BigDecimal, Double> res = new HashMap<BigDecimal, Double>();
 
         double startChi = 0;
         double startNu = u0j;
 
         Double currX = 0.0d;
         Map<Long, DirectFlowData> directFlowData = new HashMap<Long, DirectFlowData>();
         double lambda = timeStep / (xStep * xStep);
         directFlowData.put(1l, new DirectFlowData(startChi, startNu));
 
         currX = currX + xStep;
         for (long i = 2; i < n; i++) {
             /*log.debug("Curr i: " + i);
             log.debug("Curr x: " + currX);*/
             DirectFlowData prevDirectFlowData = directFlowData.get(i - 1);
             double chi = calculateChi(lambda, prevDirectFlowData.getChi());
             BigDecimal currXBigDecimal = new BigDecimal(currX);
             double nu = calculateNu(prevTimeXValues.get(currXBigDecimal.setScale(xValuesScale, RoundingMode.HALF_UP)),
                     prevDirectFlowData.getNu(), lambda, prevDirectFlowData.getChi());
             directFlowData.put(i, new DirectFlowData(chi, nu));
             currX = currX + xStep;
         }
 
         BigDecimal lastX = new BigDecimal(currX);
         res.put(lastX.setScale(xValuesScale, RoundingMode.HALF_UP), xi);
         currX = currX - xStep;
         for (long i = n; i > 1; i--) {
 //            log.debug("Curr i: " + i);
             DirectFlowData prevDirectFlowData = directFlowData.get(i - 1);
             BigDecimal nextCurrXBigDecimal = new BigDecimal(currX + xStep);
             double temp = prevDirectFlowData.getChi() *
                     res.get(nextCurrXBigDecimal.setScale(xValuesScale, RoundingMode.HALF_UP)) + prevDirectFlowData.getNu();
             BigDecimal currXBigDecimal = new BigDecimal(currX);
             res.put(currXBigDecimal.setScale(xValuesScale, RoundingMode.HALF_UP), temp);
             currX = currX - xStep;
         }
 
         return res;
     }
 
     private double calculateChi(double lambda, double chiPrev) {
         return lambda / (1 + 2 * lambda - (lambda * chiPrev));
     }
 
     private double calculateNu(double uPrevTime, double nuPrev, double lambda, double chiPrev) {
         return (uPrevTime + lambda * nuPrev) / (1 + 2 * lambda - (lambda * chiPrev));
     }
 }
