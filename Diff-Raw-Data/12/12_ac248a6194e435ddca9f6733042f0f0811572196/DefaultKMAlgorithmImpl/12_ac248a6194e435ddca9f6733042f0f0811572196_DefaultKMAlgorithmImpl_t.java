 /*
  * Created on Oct 12, 2004
  */
 package gov.nih.nci.caintegrator.plots.kaplanmeier;
 import gov.nih.nci.caintegrator.plots.kaplanmeier.dto.KMSampleDTO;
 import gov.nih.nci.caintegrator.plots.kaplanmeier.model.XYCoordinate;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 
 import org.apache.log4j.Logger;
 
 import weka.core.Statistics;
 
 
 /**
  * @author caIntegrator Team
  * Default algorithm used to plot coordinates in a km plot. THIS IMPLEMENTATION
  * assumes KM to be derived from survival data
  */
 
 
 @SuppressWarnings("unchecked")
 public class DefaultKMAlgorithmImpl implements KMAlgorithm {
     private static Logger logger = Logger.getLogger(DefaultKMAlgorithmImpl.class);
 
     public DefaultKMAlgorithmImpl() {}
 
     //createPlot drawing data points for any set of sample
     public Collection<XYCoordinate> getPlottingCoordinates(Collection<KMSampleDTO> sampleColleciton) {
         ArrayList<KMSampleDTO> samples = new ArrayList<KMSampleDTO>(sampleColleciton);
         Collections.sort(samples, new KaplanMeierSampleComparator());
         float surv = 1;
         float prevSurvTime = 0;
         float curSurvTime = 0;
         int d = 0; // Num Dead
         int c = 0; // Num Censored
         int r = samples.size();
         int left = samples.size();
         ArrayList<XYCoordinate> points = new ArrayList<XYCoordinate>();
        logger.debug(new String("Sorted input data: "));
         for (int i = 0; i < samples.size(); i++) {
             curSurvTime = samples.get(i).getSurvivalLength();
            logger.debug(new String("Survival survivalLength: " + curSurvTime + "\tcensor:" + (samples.get(i)).getCensor()));
             if (curSurvTime > prevSurvTime) {
                 if (c>0) {
                     points.add(new XYCoordinate(new Float(prevSurvTime), new Float(surv), true));
                 }
                 if (d>0) {
                     points.add(new XYCoordinate(new Float(prevSurvTime), new Float(surv), false));
                     surv = surv * (r-d)/r;
                     points.add(new XYCoordinate(new Float(prevSurvTime), new Float(surv), false));
                 } 
                 prevSurvTime = curSurvTime;
                 d = 0;
                 c = 0;
                 r = left;
             }
             if ((samples.get(i)).getCensor()) { // Dead
                 d++;
             } else { // Censored
                 c++;
             }
             left--;
         }
         if (c>0) {
             points.add(new XYCoordinate(new Float(prevSurvTime), new Float(surv), true));
         }
         if (d>0) {
             points.add(new XYCoordinate(new Float(prevSurvTime), new Float(surv), false));
             surv = surv * (r-d)/r;
             points.add(new XYCoordinate(new Float(prevSurvTime), new Float(surv), false));
         } 
         return points;
     }
     
     
 
 
     //compute the p-value between two sample series
     public Double getLogRankPValue(Collection<KMSampleDTO> group1, Collection<KMSampleDTO> group2) {
         //need to
         ArrayList<KMSampleDTO> samples = new ArrayList<KMSampleDTO>();
         samples.addAll(group1);
         samples.addAll(group2);
         Collections.sort(samples, new KaplanMeierSampleComparator());
         float u = 0;
         float v = 0;
         float a = 0;
         float b = 0;
         float c = (float) group1.size();
         float d = (float) group2.size();
         float t = 0;
         for (int i = 0; i < samples.size(); i++) {
             KMSampleDTO event = samples.get(i);
             if (event.getSurvivalLength() > t) {
                 if (a+b > 0) {
                     u += a - (a + b) * (a + c) / (a + b + c + d);
                     v += (a + b)
                             * (c + d)
                             * (a + c)
                             * (b + d)
                             / ((a + b + c + d - 1) *
                                     (Math.pow((a + b + c + d), 2)));
                 }
                 a = 0;
                 b = 0;
                 t = event.getSurvivalLength();
             }
             if (group1.contains(event)) {
                 if (event.getCensor()) {
                     a += 1;
                 }
                 c-=1;
             } else {
                 if (event.getCensor()) {
                     b+=1;
                 }
                 d-=1;
             }
         }
 
         if ( (a > 0 | b > 0) & (a+b+c+d-1>0) ) {
             u += a - (a + b) * (a + c) / (a + b + c + d);
             v += (a + b)
                     * (c + d)
                     * (a + c)
                     * (b + d)
                     / ((a + b + c + d - 1) *
                             (Math.pow((a + b + c + d), 2)));
         }
 
         if (v > 0) {
             return new Double(Statistics.chiSquaredProbability(Math.pow(u, 2.0) / v, 1.0));
         } else {
             return new Double(UNKNOWN_PVALUE);
         }
     }
 
     private static class KaplanMeierSampleComparator implements Comparator{
         public int compare(Object o1, Object o2) throws ClassCastException {
             int val;
             float i1 = ((KMSampleDTO) o1).getSurvivalLength();
             float i2 = ((KMSampleDTO) o2).getSurvivalLength();
             if (i1 > i2) {
                 val = 1;
             } else if (i1 == i2) {
                 val = 0;
             } else {
                 val = -1;
             }
             return val;
         }
     }
 }
