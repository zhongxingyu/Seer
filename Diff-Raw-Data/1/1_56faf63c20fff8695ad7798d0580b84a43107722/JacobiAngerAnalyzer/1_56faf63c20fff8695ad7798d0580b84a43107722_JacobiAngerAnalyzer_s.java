 package org.sade.analyzers;
 
 import org.apache.commons.math.complex.Complex;
 import org.sade.analyzers.math.FFT;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class JacobiAngerAnalyzer
 {
     private AnalyzeResult lastAnalyzeResult;
     private double[] previousSamples;
     public static final double Precision = 1E-4;
     private final int MaxIterations = 2000;
     private final int FourierCoeffCount = 8;
     private static final int omegaSlices = 4;
     private static final int phaseSlices = 4;
     private final List<MinimizeParameters> scanParameters = scanParameters();
 
     public MinimizeResult AnalyzeSample(double[] sample, double omega, double delta, double phi)
     {
         return AnalyzeSample(sample, new MinimizeParameters(omega, delta, phi));
     }
 
     private MinimizeResult AnalyzeSample(double[] sample, MinimizeParameters minimizeParameters)
     {
         Complex[] firstCoeff = GetFirstCoefficients(ReScale(sample));
         MinimizeResult result = new MinimizeResult(0, minimizeParameters);
         result = MinimizeErrorFunction(result.getParameters(), Arrays.copyOf(firstCoeff, FourierCoeffCount));
         return result;
     }
 
     public static double[] ReScale(double[] sample)
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
         return result;
     }
 
     private Complex[] GetFirstCoefficients(double[] sample)
     {
         return FFT.transform(sample, FourierCoeffCount);
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
 
         int searchPeriod = IsFirstAnalyze() ? ScanPeriod(sample) : lastAnalyzeResult.getPeriod();
         if (!IsFirstAnalyze())
         {
             double[] newSample = Arrays.copyOf(previousSamples, previousSamples.length + sample.length);
             System.arraycopy(sample, 0, newSample, previousSamples.length, sample.length);
             sample = newSample;
         }
 
         final double searchPeriodCoeff = 0.01;
 
         for (; sample.length > sizeForSearchPeriod(searchPeriod, searchPeriodCoeff); sample = skip(sample, searchPeriod))
         {
             double[] sampleToAnalyze = take(sample, searchPeriod);
             if (IsFirstAnalyze())
             {
                 lastAnalyzeResult = rescanAnalyze(searchPeriod, sampleToAnalyze);
             }
             else
             {
                 searchPeriod = searchPeriodInInterval(sample, searchPeriod);
                 AnalyzeResult analyzeResult = analyzeNextByPrevious(sampleToAnalyze, searchPeriod);
                 if (isOverErrorThreshold(analyzeResult)) {
                     searchPeriod = analyzeWithOversample(take(sample, sizeForSearchPeriod(searchPeriod, searchPeriodCoeff)), searchPeriodCoeff, sampleToAnalyze, 2);
                 } else {
                     lastAnalyzeResult = analyzeResult;
                 }
             }
             analyzeResults.add(lastAnalyzeResult);
         }
         previousSamples = sample;
         return analyzeResults;
     }
 
     private static int searchPeriodInInterval(double[] sample, int searchPeriod) {
         return SearchPeriod(sample, (int)Math.round(searchPeriod * 0.95), (int)Math.round(searchPeriod * 1.05));
     }
 
     private int sizeForSearchPeriod(int searchPeriod, double searchPeriodCoeff) {
         return (int)Math.round(searchPeriod * (1 + searchPeriodCoeff * 8))*2+1;
     }
 
     private int analyzeWithOversample(double[] sample, double searchPeriodCoeff, double[] sampleToAnalyze, int oversampleRate) {
         double[] oversample = oversample(sample);
         int doublePeriod = searchOversampledPeriod(searchPeriodCoeff, oversample, oversampleRate);
         float realPeriod = 1.0f * doublePeriod / oversampleRate;
         if (doublePeriod % 2 != 0 && oversampleRate < 128 || oversampleRate < 16) {
             return analyzeWithOversample(oversample, searchPeriodCoeff, take(oversample, doublePeriod), oversampleRate * 2);
         } else {
             AnalyzeResult analyzeResult = analyzeNextByPrevious(sampleToAnalyze, realPeriod);
             if (isOverErrorThreshold(analyzeResult)) {
                 lastAnalyzeResult = rescanAnalyze(realPeriod, sampleToAnalyze);
             } else {
                 lastAnalyzeResult = analyzeResult;
             }
         }
         return lastAnalyzeResult.getPeriod();
     }
 
     public static boolean isOverErrorThreshold(AnalyzeResult analyzeResult) {
         return analyzeResult.getMinimizeError() > 0.2;
     }
 
     private AnalyzeResult analyzeNextByPrevious(double[] sampleToAnalyze, float realPeriod) {
         return new AnalyzeResult(AnalyzeSample(sampleToAnalyze, lastAnalyzeResult.getParameters()), realPeriod);
     }
 
     private double[] oversample(double[] sample) {
         double[] result = new double[sample.length * 2 - 1];
         for (int i = 0; i < sample.length - 1; i++) {
             result[i * 2] = sample[i];
             result[i * 2 + 1] = (sample[i] + sample[i + 1]) / 2;
         }
         result[result.length - 1] = sample[sample.length - 1];
         return result;
     }
 
     private AnalyzeResult rescanAnalyze(float searchPeriod, double[] sampleToAnalyze) {
         return new AnalyzeResult(ScanForEntryParameters(sampleToAnalyze), searchPeriod);
     }
 
     private int searchOversampledPeriod(double searchPeriodCoeff, double[] oversample, int oversampleRate) {
         return SearchPeriod(oversample,
                 multiplyLastPeriodByPercent(-searchPeriodCoeff, lastAnalyzeResult.getPeriod() * oversampleRate),
                 multiplyLastPeriodByPercent(searchPeriodCoeff, lastAnalyzeResult.getPeriod() * oversampleRate)
         );
     }
 
     private int multiplyLastPeriodByPercent(double coeff, int period) {
         return (int) Math.round(period * (1 + coeff));
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
 
     public static int ScanPeriod(double[] sample)
     {
         int log = (int)Math.pow(2, Math.floor(Math.log(sample.length) / Math.log(2)));
         double frequency = FrequencyEvaluator.evaluateFrequency(take(sample, log));
         double period = 1/frequency;
         int searchPeriod = (int) period;
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
 
     private MinimizeResult ScanForEntryParameters(double[] sampleToAnalyze)
     {
         List<MinimizeResult> results = new ArrayList<MinimizeResult>();
         for (MinimizeParameters minimizeParameters : scanParameters) {
             MinimizeResult minimizeResult = AnalyzeSample(sampleToAnalyze, minimizeParameters);
             double delta = minimizeResult.getParameters().getDelta();
             double omega = minimizeResult.getParameters().getOmega();
             if (delta > Math.PI || delta < 0.0 || omega < 2.0) continue;
             results.add(minimizeResult);
         }
         double min = results.get(0).getError();
         MinimizeResult minResult = null;
         for (MinimizeResult minimizeResult : results) {
             min = Math.min(min, minimizeResult.getError());
             if (min == minimizeResult.getError()) minResult = minimizeResult;
         }
         return minResult;
     }
 
     private static int SearchPeriod(double[] sample, int from, int to)
     {
         double[] twoPeriods = take(sample, from * 2);
         Complex[] first = FFT.transform(take(twoPeriods, from), 2);
         Complex[] second = FFT.transform(skip(twoPeriods, from), 2);
         double phaseDiff = first[1].getArgument() - second[1].getArgument();
         phaseDiff = (Math.abs(phaseDiff) % (Math.PI * 2)) * Math.signum(phaseDiff);
         if (phaseDiff < -Math.PI) phaseDiff += Math.PI * 2;
         int period = (int) (from + Math.round((phaseDiff / (2 * Math.PI)) * from));
         if (period >= from && period <= to) {
             int center = Math.round((from + to) / 2.0f);
             int periodDiff = Math.abs(center - period);
             if (center - periodDiff == from && center + periodDiff == to)
                 return period;
             else
                 return SearchPeriod(sample, center - periodDiff, center + periodDiff);
         } else {
             return Math.min(Math.max(period, from), to);
         }
     }
 
 }
