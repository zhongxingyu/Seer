 package org.sade.analyzers;
 
 import org.apache.commons.math.complex.Complex;
 import org.sade.analyzers.math.FFT;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class JacobiAngerAnalyzer
 {
     private final Logger logger = LoggerFactory.getLogger(getClass());
     private AnalyzeResult lastAnalyzeResult;
     private double[] previousSamples;
     public static final double Precision = 1E-4;
     private final int MaxIterations = 2000;
     private final int FourierCoeffCount = 8;
     private static final int omegaSlices = 4;
     private static final int phaseSlices = 4;
     private final List<MinimizeParameters> scanParameters = scanParameters();
     private final long timeoutTime;
 
     public JacobiAngerAnalyzer(long timeoutTime) {
         this.timeoutTime = timeoutTime;
     }
 
     public JacobiAngerAnalyzer() {
         this.timeoutTime = -1;
     }
 
     public MinimizeResult AnalyzeSample(double[] sample, double omega, double delta, double phi)
     {
         return AnalyzeSample(sample, new MinimizeParameters(omega, delta, phi), sample.length).minimizeResult;
     }
 
     private AnalyzeResultWithAmplitude AnalyzeSample(double[] sample, MinimizeParameters minimizeParameters, double realPeriod)
     {
         if (timeoutTime > 0 && System.currentTimeMillis() > timeoutTime) {
             throw new AnalyzeTimeoutException();
         }
         ReScaleResult reScaleResult = ReScale(sample);
         logger.debug("Period: " + realPeriod);
         Complex[] firstCoeff = GetFirstCoefficients(reScaleResult.values, realPeriod);
         MinimizeResult result = new MinimizeResult(0, minimizeParameters);
         result = MinimizeErrorFunction(result.getParameters(), Arrays.copyOf(firstCoeff, FourierCoeffCount));
         return new AnalyzeResultWithAmplitude(result, reScaleResult.amplitude, reScaleResult.center);
     }
 
     public static ReScaleResult ReScale(double[] sample)
     {
         double min = sample[0];
         for (double v : sample) {
             min = Math.min(v,min);
         }
         double max = sample[0];
         for (double v : sample) {
             max = Math.max(v,max);
         }
         double amplitude = (max - min)/2;
         double center = min + amplitude;
         double[] result = new double[sample.length];
         for (int i = 0; i < sample.length; i++) {
             double v = sample[i];
             result[i] = (v - center)/amplitude;
         }
         return new ReScaleResult(result, amplitude, center);
     }
 
     private Complex[] GetFirstCoefficients(double[] sample, double realPeriod)
     {
         return FFT.transform(sample, FourierCoeffCount, sample.length / realPeriod);
     }
 
     private MinimizeResult MinimizeErrorFunction(MinimizeParameters initialParams, Complex[] fourierCoefficients)
     {
         JacobiAngerErrorFuncDiffEvaluator gradientFunction = new JacobiAngerErrorFuncDiffEvaluator(fourierCoefficients);
         GradientOptimizer descentOptimizer = new FastestGradientDescentOptimizer(Precision, gradientFunction);
 
         double[] result = descentOptimizer.Optimize(initialParams.Wrap());
         double error = gradientFunction.Value(result);
         return new MinimizeResult(error, MinimizeParametersSugar.UnwrapToParams(result));
     }
 
     public boolean IsFirstAnalyze()
     {
         return lastAnalyzeResult == null;
     }
 
     public List<AnalyzeResult> DivideAndAnalyze(double[] sample)
     {
         List<AnalyzeResult> analyzeResults = new ArrayList<AnalyzeResult>();
 
         double searchPeriod = IsFirstAnalyze() ? ScanPeriod(sample) : lastAnalyzeResult.getPeriod();
         if (!IsFirstAnalyze())
         {
             double[] newSample = Arrays.copyOf(previousSamples, previousSamples.length + sample.length);
             System.arraycopy(sample, 0, newSample, previousSamples.length, sample.length);
             sample = newSample;
         }
 
         final double searchPeriodCoeff = 0.01;
 
         for (; sample.length > sizeForSearchPeriod(searchPeriod, searchPeriodCoeff); sample = skip(sample, (int) Math.round(searchPeriod)))
         {
             double[] sampleToAnalyze = take(sample, (int) Math.round(searchPeriod));
             if (IsFirstAnalyze())
             {
                 lastAnalyzeResult = rescanAnalyze(searchPeriod, sampleToAnalyze);
             }
             else
             {
                 searchPeriod = searchPeriodInInterval(sample, searchPeriod);
                 AnalyzeResult analyzeResult = analyzeNextByPrevious(sampleToAnalyze, searchPeriod);
                 if (isOverErrorThreshold(analyzeResult)) {
                     lastAnalyzeResult = rescanAnalyze(searchPeriod, sampleToAnalyze);
                 } else {
                     lastAnalyzeResult = analyzeResult;
                 }
             }
             analyzeResults.add(lastAnalyzeResult);
         }
         previousSamples = sample;
         return analyzeResults;
     }
 
     private static double searchPeriodInInterval(double[] sample, double searchPeriod) {
         return SearchPeriod(sample, searchPeriod * 0.95, searchPeriod * 1.05, 0);
     }
 
     private int sizeForSearchPeriod(double searchPeriod, double searchPeriodCoeff) {
         return (int)Math.round(searchPeriod * (1 + searchPeriodCoeff * 8))*2+1;
     }
 
     public static boolean isOverErrorThreshold(AnalyzeResult analyzeResult) {
         return analyzeResult.getMinimizeError() > 0.2;
     }
 
     private AnalyzeResult analyzeNextByPrevious(double[] sampleToAnalyze, double realPeriod) {
         AnalyzeResultWithAmplitude analyzeResultWithAmplitude = AnalyzeSample(sampleToAnalyze, lastAnalyzeResult.getParameters(), realPeriod);
         AnalyzeResult result = new AnalyzeResult(analyzeResultWithAmplitude.minimizeResult, realPeriod, analyzeResultWithAmplitude.amplitude, analyzeResultWithAmplitude.center);
         if (logger.isDebugEnabled())
             logger.debug("analyzeNextByPrevious: " + result);
         return result;
     }
 
     private AnalyzeResult rescanAnalyze(double searchPeriod, double[] sampleToAnalyze) {
         AnalyzeResultWithAmplitude analyzeResultWithAmplitude = ScanForEntryParameters(sampleToAnalyze, searchPeriod);
         return new AnalyzeResult(analyzeResultWithAmplitude.minimizeResult, searchPeriod, analyzeResultWithAmplitude.amplitude, analyzeResultWithAmplitude.center);
     }
 
     public static double[] take(double[] sample, int searchPeriod) {
         return Arrays.copyOf(sample, Math.min(searchPeriod, sample.length));
     }
 
     public static double[] skip(double[] sample, int searchPeriod) {
         int destLength = sample.length - searchPeriod;
         if (destLength < 0) {
             return new double[0];
         }
         double[] dest = new double[destLength];
         System.arraycopy(sample, searchPeriod, dest, 0, destLength);
         return dest;
     }
 
     public static double ScanPeriod(double[] sample)
     {
         int log = (int)Math.pow(2, Math.floor(Math.log(sample.length) / Math.log(2)));
         double frequency = FrequencyEvaluator.evaluateFrequency(take(sample, log));
         double searchPeriod = 1/frequency;
         searchPeriod = searchPeriodInInterval(sample, searchPeriod);
         return searchPeriod;
     }
     
     public static List<MinimizeParameters> scanParameters() {
         ArrayList<MinimizeParameters> params = new ArrayList<MinimizeParameters>();
         for (int i = 0; i < phaseSlices / 2 + 1; i++)
         {
             for (int j = 0; j < phaseSlices; j++)
             {
                 for (int k = 0; k < omegaSlices; k++)
                 {
                     MinimizeParameters minimizeParameters = new MinimizeParameters(2 + k * 1.0 / (omegaSlices - 1), 2 * Math.PI / phaseSlices * i, 2 * Math.PI / phaseSlices * j);
                     params.add(minimizeParameters);
                 }
             }
         }
         return params;
     }
 
     private AnalyzeResultWithAmplitude ScanForEntryParameters(double[] sampleToAnalyze, double realPeriod)
     {
         if (logger.isDebugEnabled())
             logger.debug("ScanForEntryParameters");
         List<AnalyzeResultWithAmplitude> results = new ArrayList<AnalyzeResultWithAmplitude>();
         for (MinimizeParameters minimizeParameters : scanParameters) {
             AnalyzeResultWithAmplitude minimizeResult = AnalyzeSample(sampleToAnalyze, minimizeParameters, realPeriod);
             double delta = minimizeResult.minimizeResult.getParameters().getDelta();
             double omega = minimizeResult.minimizeResult.getParameters().getOmega();
             if (delta > Math.PI || delta < 0.0 || omega < 1.0) continue;
             if (logger.isDebugEnabled())
                 logger.debug("Result found: " + minimizeResult);
             results.add(minimizeResult);
         }
         double min = results.get(0).minimizeResult.getError();
         AnalyzeResultWithAmplitude minResult = null;
         for (AnalyzeResultWithAmplitude minimizeResult : results) {
             min = Math.min(min, minimizeResult.minimizeResult.getError());
             if (min == minimizeResult.minimizeResult.getError()) minResult = minimizeResult;
         }
         if (logger.isDebugEnabled())
             logger.debug("Result won: " + minResult);
         return minResult;
     }
 
     public static double SearchPeriod(double[] sample, double from, double to, int iterations)
     {
         double[] twoPeriods = take(sample, (int) (Math.round(from) * 2));
         Complex[] first = FFT.transform(take(twoPeriods, (int) Math.round(from)), 2, 1.0);
         Complex[] second = FFT.transform(skip(twoPeriods, (int) Math.round(from)), 2, 1.0);
         double phaseDiff = first[1].getArgument() - second[1].getArgument();
         phaseDiff = (Math.abs(phaseDiff) % (Math.PI * 2)) * Math.signum(phaseDiff);
         if (phaseDiff < -Math.PI) phaseDiff += Math.PI * 2;
         if (phaseDiff > Math.PI) phaseDiff -= Math.PI * 2;
         double period = (from + (phaseDiff / (2 * Math.PI) * from));
         if (period >= from && period <= to) {
             double center = (from + to) / 2.0f;
             double periodDiff = Math.abs(center - period);
             if (periodDiff < 1E-2 || iterations > 1000)
                return Math.max(period, 20);
             else
                 return SearchPeriod(sample, center - periodDiff, center + periodDiff, iterations + 1);
         } else {
             return Math.min(Math.max(period, from), to);
         }
     }
 
 }
 
 class AnalyzeResultWithAmplitude {
     public final MinimizeResult minimizeResult;
     public final double amplitude;
     public final double center;
 
     AnalyzeResultWithAmplitude(MinimizeResult minimizeResult, double amplitude, double center) {
         this.minimizeResult = minimizeResult;
         this.amplitude = amplitude;
         this.center = center;
     }
 
     @Override
     public String toString() {
         return "AnalyzeResultWithAmplitude{" +
                 "minimizeResult=" + minimizeResult +
                 ", amplitude=" + amplitude +
                 ", center=" + center +
                 '}';
     }
 }
