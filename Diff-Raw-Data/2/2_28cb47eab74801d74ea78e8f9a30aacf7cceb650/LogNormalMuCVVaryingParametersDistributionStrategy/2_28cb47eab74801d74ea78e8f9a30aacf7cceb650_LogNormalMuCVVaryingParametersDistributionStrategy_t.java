 package org.pillarone.riskanalytics.domain.utils.math.distribution.varyingparams;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter;
 import org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory;
 import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier;
 import org.pillarone.riskanalytics.core.util.GroovyUtils;
 import org.pillarone.riskanalytics.domain.utils.InputFormatConverter;
 import org.pillarone.riskanalytics.domain.utils.constraint.PeriodDistributionsConstraints;
 import org.pillarone.riskanalytics.domain.utils.math.distribution.DistributionParams;
 import org.pillarone.riskanalytics.domain.utils.math.distribution.DistributionType;
 import org.pillarone.riskanalytics.domain.utils.math.distribution.RandomDistribution;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * @author stefan.kunz (at) intuitive-collaboration (dot) com
  */
 public class LogNormalMuCVVaryingParametersDistributionStrategy extends AbstractVaryingParameterDistributionStrategy {
 
     private ConstrainedMultiDimensionalParameter meanAndCV = new ConstrainedMultiDimensionalParameter(
             GroovyUtils.convertToListOfList(new Object[]{1, 1d, 1d}), Arrays.asList(PERIOD, DistributionParams.MEAN, DistributionParams.CV),
             ConstraintsFactory.getConstraints(PeriodDistributionsConstraints.IDENTIFIER));
 
     public IParameterObjectClassifier getType() {
        return VaryingParametersDistributionType.LOGNORMAL_MEAN_CV;
     }
 
     public Map getParameters() {
         Map<String, ConstrainedMultiDimensionalParameter> parameters = new HashMap<String, ConstrainedMultiDimensionalParameter>(1);
         parameters.put("meanAndCV", meanAndCV);
         return parameters;
     }
 
     protected TreeMap<Integer, RandomDistribution> initDistributions() {
         TreeMap<Integer, RandomDistribution> distributionPerPeriod = new TreeMap<Integer, RandomDistribution>();
         int meanColumnIndex = meanAndCV.getColumnIndex(DistributionParams.MEAN.toString());
         int cvColumnIndex = meanAndCV.getColumnIndex(DistributionParams.CV.toString());
 
         for (int row = meanAndCV.getTitleRowCount(); row < meanAndCV.getRowCount(); row++) {
             int period = InputFormatConverter.getInt((meanAndCV.getValueAt(row, periodColumnIndex))) - 1;
             double mean = InputFormatConverter.getDouble(meanAndCV.getValueAt(row, meanColumnIndex));
             double cv = InputFormatConverter.getDouble(meanAndCV.getValueAt(row, cvColumnIndex));
             distributionPerPeriod.put(period, DistributionType.getStrategy(DistributionType.LOGNORMAL_MEAN_CV, ArrayUtils.toMap(
                     new Object[][]{{DistributionParams.MEAN, mean}, {DistributionParams.CV, cv}})));
         }
         return distributionPerPeriod;
     }
 }
