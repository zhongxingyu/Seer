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
 public class UniformVaryingParametersDistributionStrategy extends AbstractVaryingParameterDistributionStrategy {
 
     private ConstrainedMultiDimensionalParameter boundaries = new ConstrainedMultiDimensionalParameter(
             GroovyUtils.convertToListOfList(new Object[]{1, 1d, 1d}),
             Arrays.asList(DistributionParams.PERIOD, DistributionParams.A, DistributionParams.B),
             ConstraintsFactory.getConstraints(PeriodDistributionsConstraints.IDENTIFIER));
 
     public IParameterObjectClassifier getType() {
         return VaryingParametersDistributionType.UNIFORM;
     }
 
     public Map getParameters() {
         Map<String, ConstrainedMultiDimensionalParameter> parameters = new HashMap<String, ConstrainedMultiDimensionalParameter>(1);
         parameters.put("boundaries", boundaries);
         return parameters;
     }
 
     protected TreeMap<Integer, RandomDistribution> initDistributions() {
         TreeMap<Integer, RandomDistribution> distributionPerPeriod = new TreeMap<Integer, RandomDistribution>();
         int aColumnIndex = boundaries.getColumnIndex(DistributionParams.A.toString());
         int bColumnIndex = boundaries.getColumnIndex(DistributionParams.B.toString());
 
         for (int row = boundaries.getTitleRowCount(); row < boundaries.getRowCount(); row++) {
             int period = InputFormatConverter.getInt((boundaries.getValueAt(row, periodColumnIndex))) - 1;
             double aParam = InputFormatConverter.getDouble(boundaries.getValueAt(row, aColumnIndex));
             double bParam = InputFormatConverter.getDouble(boundaries.getValueAt(row, bColumnIndex));
             distributionPerPeriod.put(period, DistributionType.getStrategy(
                    DistributionType.TRIANGULARDIST, ArrayUtils.toMap(
                         new Object[][]{{DistributionParams.A, aParam},
                                 {DistributionParams.B, bParam}})));
         }
         return distributionPerPeriod;
     }
 }
