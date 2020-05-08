 package org.pillarone.riskanalytics.application.util;
 
 import org.codehaus.groovy.grails.commons.ApplicationHolder;
 import org.pillarone.riskanalytics.core.output.PostSimulationCalculation;
 import org.pillarone.riskanalytics.core.output.SimulationRun;
 import org.pillarone.riskanalytics.application.ui.result.model.ResultTableTreeNode;
 import org.pillarone.riskanalytics.core.dataaccess.PostSimulationCalculationAccessor;
 import umontreal.iro.lecuyer.probdist.NormalDist;
 
 import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
 
 
 public class JEstimator {
     protected static double threshold = 4.0;  //used for speeding up pdf calculations for graphical representations.
     protected static double CONST2 = 1d / Math.sqrt(2d * Math.PI);
 
 
     private static void addXYValues(List<List<Double>> list, double x, double y) {
         List<Double> xy = new ArrayList<Double>();
         xy.add(x);
         xy.add(y);
         list.add(xy);
     }
 
     public static List gaussKernelBandwidthPdf(List<Double> observations, double t, boolean dataExportMode) {
         List<List<Double>> pdfValues = new ArrayList<List<Double>>();
         List<Double> plotMesh = JMeshCalculations.getUniformMeshWithModifiedEdges(t, dataExportMode, observations);
         double norm = 0d;
         double meshPoint = plotMesh.get(0);
         double lastMeshPoint = meshPoint;
         double lastY = 0;
         addXYValues(pdfValues, meshPoint, lastY);
 
         for (int i = 1; i < plotMesh.size(); i++) {
             // we start to plot before the min value of the obervations because a continuous pdf starts at value =0
             lastMeshPoint = meshPoint;
             meshPoint = plotMesh.get(i);
             double y = JEstimator._gaussKernelBandwidthPdf(meshPoint, observations, t);
             norm += y * (meshPoint - lastMeshPoint);
             addXYValues(pdfValues, meshPoint, y);
         }
 
         if (Math.abs(norm - 1d) > 1e-6) {
             double factor = 1d / norm;
             for (List<Double> xy : pdfValues) {
                 xy.set(1, xy.get(1) * factor);
             }
         }
         return pdfValues;
     }
 
 
     public static double calcBandwidthForGaussKernelEstimate(double stdDev, double IQR, double n) {
         if (IQR > 0) {
             return Estimator.CONST * Math.min(stdDev, IQR) * Math.pow(n, (-1 / 5));
         } else {
             return Estimator.CONST * stdDev * Math.pow(n, (-1 / 5));
         }
     }
 
     protected static double _gaussKernelBandwidthPdf(double x, List<Double> observations, double t) {
         double sum = 0;
         double iStart = 0;
         int n = observations.size();
         double scalingFactor = calculateScalingFactor(n, t);
         double thresholdT = threshold * t;
 
         int i = (int) iStart;
         while ((i < n) && (x - observations.get(i) > thresholdT)) i++;
         iStart = i;
         while ((i < n) && (Math.abs(x - observations.get(i)) < thresholdT)) {
             sum += Math.exp(-(x - observations.get(i)) * (x - observations.get(i)) / (2 * t * t));
             i++;
         }
 
         return scalingFactor * sum;
     }
 
     protected static double calculateScalingFactor(double n, double t) {
         return 1 / (n * Math.sqrt(2 * Math.PI) * t);
     }
 
     public static List adaptiveKernelBandwidthPdf(List<Double> observations, double priorBandwidth, boolean dataExportMode) {
         List<List<Double>> pdfValues = new ArrayList<List<Double>>();
         List<Double> plotMesh = JMeshCalculations.getUniformMeshWithModifiedEdges(priorBandwidth, dataExportMode, observations);
         List<Integer> meshCounts = JMeshCalculations.calculateMeshCount(plotMesh, observations);
         List<Double> bandwidths = computeBandwidths(plotMesh, meshCounts, priorBandwidth, observations, dataExportMode);
         double norm = 0d;
         double meshPoint = plotMesh.get(0);
         double lastMeshPoint = meshPoint;
         double lastY = JEstimator._adaptiveKernelBandwidthPdf(meshPoint, priorBandwidth, observations, dataExportMode, plotMesh, meshCounts, bandwidths);  // this mightnot be 0, but setting it hard at some xvalue to zero looks odd, for obs.size large it is ok
         addXYValues(pdfValues, meshPoint, lastY);
 
         for (int i = 1; i < plotMesh.size(); i++) {
             // we start to plot before the min value of the obervations because a continuous pdf starts at value =0
             lastMeshPoint = meshPoint;
             meshPoint = plotMesh.get(i);
             double y = JEstimator._adaptiveKernelBandwidthPdf(meshPoint, priorBandwidth, observations, dataExportMode, plotMesh, meshCounts, bandwidths);
             norm += y * (meshPoint - lastMeshPoint);
             addXYValues(pdfValues, meshPoint, y);
 
         }
 
         if (Math.abs(norm - 1d) > 1e-6) {
             double factor = 1d / norm;
             for (List<Double> xy : pdfValues) {
                 xy.set(1, xy.get(1) * factor);
             }
         }
         return pdfValues;
     }
 
     protected static double _adaptiveKernelBandwidthPdf(double x, double priorBandwidth, List observations, boolean dataExportMode, List<Double> meshMidPoints, List<Integer> meshCounts, List<Double> bandwidths) {
         double freq = 0;
         double norm = 0;
 
         int n = meshMidPoints.size();
 
         for (int i = 0; i < n; i++) {
             double bw = bandwidths.get(i);
             if (bw > 0 && Math.abs(x - meshMidPoints.get(i)) < threshold * bw) {
                 freq += meshCounts.get(i) * Math.exp(-(x - meshMidPoints.get(i)) * (x - meshMidPoints.get(i)) / (2 * bw * bw)) / bw;
             }
             norm += meshCounts.get(i);
         }
         return CONST2 * freq / norm;
     }
 
     protected static List<Double> computeBandwidths(List<Double> meshMidPoints, List<Integer> meshCounts, double priorBandwidth, List observations, boolean dataExportMode) {
         List<Double> bws = new ArrayList<Double>();
         int n = meshMidPoints.size();
         double norm = 0.0;
         for (int i = 0; i < n; i++) {
             double density = JEstimator.adaptiveKernelBandwidthPriorPdf(meshMidPoints.get(i), priorBandwidth, observations, dataExportMode, meshMidPoints, meshCounts);
             norm += density > 0 ? Math.log(density) : 0;
             bws.add(density);
         }
         norm = Math.exp(norm / n);
         for (int i = 0; i < n; i++) {
             double bw = bws.get(i);
             bw = Math.sqrt(norm / bw) * priorBandwidth;
             bws.set(i, bw);
         }
         return bws;
     }
 
     protected static double adaptiveKernelBandwidthPriorPdf(double x, double priorBandwidth, List observations, boolean dataExportMode, List<Double> meshMidPoints, List<Integer> meshCounts) {
         double freq = 0;
         double norm = 0;
         int n = meshMidPoints.size();
         double thresholdBW = threshold * priorBandwidth;
 
         for (int i = 0; i < n; i++) {
             if (Math.abs(x - meshMidPoints.get(i)) < thresholdBW) {
                 freq += meshCounts.get(i) * Math.exp(-(x - meshMidPoints.get(i)) * (x - meshMidPoints.get(i)) / (2 * priorBandwidth * priorBandwidth));
             }
             norm += meshCounts.get(i);
         }
         return CONST2 / priorBandwidth * freq / norm;
     }
 
 
     public static List gaussKernelBandwidthCdf(List<Double> observations, double t, boolean dataExportMode) {
         List<List<Double>> cdfValues = new ArrayList<List<Double>>();
         List<Double> plotMesh = JMeshCalculations.getUniformMeshWithModifiedEdges(t, dataExportMode, observations);
         for (double x : plotMesh) {
             // we start to plot before the min value of the obervations because a continuous pdf starts at value =0
             double y = JEstimator._gaussKernelBandwidthCdf(x, observations, t);
             addXYValues(cdfValues, x, y);
         }
         return cdfValues;
     }
 
     public static List adaptiveKernelBandwidthCdf(List<Double> observations, double priorBandwidth, boolean dataExportMode) {
         List<List<Double>> cdfValues = new ArrayList<List<Double>>();
         List<Double> plotMesh = JMeshCalculations.getUniformMeshWithModifiedEdges(priorBandwidth, dataExportMode, observations);
         List<Integer> meshCounts = JMeshCalculations.calculateMeshCount(plotMesh, observations);
         List<Double> bandwidths = computeBandwidths(plotMesh, meshCounts, priorBandwidth, observations, dataExportMode);
         for (double x : plotMesh) {
             // we start to plot before the min value of the obervations because a continuous pdf starts at value =0
             double y = _adaptiveKernelBandwidthCdf(x, priorBandwidth, observations, dataExportMode, plotMesh, meshCounts, bandwidths);
             addXYValues(cdfValues, x, y);
         }
         return cdfValues;
     }
 
     public static List getCDF(SimulationRun simulationRun, ResultTableTreeNode node, int periodIndex) {
         List<List<Double>> cdf = new ArrayList<List<Double>>();
         Map keyFigures = (Map) ApplicationHolder.getApplication().getConfig().getProperty("keyFiguresToCalculate");
         List<BigDecimal> yValues = (List) keyFigures.get(PostSimulationCalculation.PERCENTILE);
         List xValues = PostSimulationCalculationAccessor.getPercentiles(simulationRun, periodIndex, node.getPath(), node.getCollector(), node.getField());
         for (int i = 0; i < xValues.size(); i++) {
             addXYValues(cdf, (Double)xValues.get(i), yValues.get(i).doubleValue() / 100.0);
         }
         return cdf;
     }
 
     public static List getPDF(SimulationRun simulationRun, ResultTableTreeNode node, int periodIndex) {
         List<List<Double>> pdf = new ArrayList<List<Double>>();
         List values = PostSimulationCalculationAccessor.getPDFValues(simulationRun, periodIndex, node.getPath(), node.getCollector(), node.getField());
        if (values == null)
            return new ArrayList();
        for (Iterator iter = values.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            addXYValues(pdf, ((BigDecimal) map.get("keyFigureParameter")).doubleValue(), ((Double) map.get("result")));
         }
         return pdf;
     }
 
 
     protected static double _gaussKernelBandwidthCdf(double x, List<Double> observations, double t) {
         double iStart = 0;
         double sum = 0;
         int n = observations.size();
         double thresholdT = threshold * t;
 
         int i = (int) iStart;
         while ((i < n) && (x - observations.get(i) > thresholdT)) i++;
         iStart = i;
         sum = i;
         while ((i < n) && (Math.abs(x - observations.get(i)) < thresholdT)) {
             sum += NormalDist.cdf01((x - observations.get(i)) / t);
             i++;
         }
         return sum / n;
     }
 
     protected static double _adaptiveKernelBandwidthCdf(double x, double priorBandwidth, List observations, boolean dataExportMode, List<Double> meshMidPoints, List<Integer> meshCounts, List<Double> bandwidths) {
         double freq = 0;
         double norm = 0;
 
         int n = meshMidPoints.size();
 
         for (int i = 0; i < n; i++) {
             double bw = bandwidths.get(i);
             if (x - meshMidPoints.get(i) > threshold * bw) {
                 freq += meshCounts.get(i);
             } else if (bw > 0 && Math.abs(meshMidPoints.get(i) - x) < threshold * bw) {
                 freq += meshCounts.get(i) * NormalDist.cdf01((x - meshMidPoints.get(i)) / bw);
             }
             norm += meshCounts.get(i);
         }
         return freq / norm;
     }
 }
