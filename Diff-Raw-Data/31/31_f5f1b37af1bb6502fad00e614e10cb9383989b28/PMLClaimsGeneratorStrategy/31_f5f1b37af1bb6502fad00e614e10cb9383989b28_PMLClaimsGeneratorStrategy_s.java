 package org.pillarone.riskanalytics.domain.pc.generators.claims;
 
 import org.pillarone.riskanalytics.core.simulation.engine.PeriodScope;
 import org.pillarone.riskanalytics.core.util.GroovyUtils;
import org.pillarone.riskanalytics.domain.utils.DistributionModified;
import org.pillarone.riskanalytics.domain.utils.DistributionModifier;
 import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier;
 import org.pillarone.riskanalytics.core.parameterization.TableMultiDimensionalParameter;
import org.pillarone.riskanalytics.domain.utils.RandomDistribution;
 import org.pillarone.riskanalytics.domain.pc.constants.Exposure;
 import org.pillarone.riskanalytics.domain.utils.constraints.DoubleConstraints;
 import org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory;
 import org.pillarone.riskanalytics.core.parameterization.ConstrainedMultiDimensionalParameter;
import org.pillarone.riskanalytics.domain.utils.FrequencyDistributionType;
import org.pillarone.riskanalytics.domain.utils.DistributionType;
 import org.pillarone.riskanalytics.domain.pc.constants.FrequencySeverityClaimType;
 
 import java.util.*;
 
 /**
  * @author jessika.walter (at) intuitive-collaboration (dot) com
  */
 public class PMLClaimsGeneratorStrategy implements IClaimsGeneratorStrategy {
 
     ConstrainedMultiDimensionalParameter pmlData = new ConstrainedMultiDimensionalParameter(
             GroovyUtils.toList("[[0d], [0d]]"), Arrays.asList("return period", "maximum claim"),
             ConstraintsFactory.getConstraints(DoubleConstraints.IDENTIFIER));
     DistributionModified claimsSizeModification = DistributionModifier.getStrategy(DistributionModifier.NONE, Collections.emptyMap());
     Map<Integer, RandomDistribution> claimsSizeDistribution = new HashMap<Integer, RandomDistribution>();
     Map<Integer, RandomDistribution> frequencyDistribution = new HashMap<Integer, RandomDistribution>();
     FrequencySeverityClaimType produceClaim = FrequencySeverityClaimType.AGGREGATED_EVENT;
     Integer currentPeriod;
 
     public IParameterObjectClassifier getType() {
         return ClaimsGeneratorType.PML;
     }
 
     public Map getParameters() {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("pmlData", pmlData);
         params.put("claimsSizeModification", claimsSizeModification);
         params.put("produceClaim", produceClaim);
         return params;
     }
 
     public DistributionModified getClaimsSizeModification() {
         return claimsSizeModification;
     }
 
     public Exposure getClaimsSizeBase() {
         return Exposure.ABSOLUTE;
     }
 
     public RandomDistribution getClaimsSizeDistribution() {
         return claimsSizeDistribution.get(currentPeriod);
     }
 
     public RandomDistribution getFrequencyDistribution() {
         return frequencyDistribution.get(currentPeriod);
     }
 
     void initDistributions(PeriodScope periodScope) {
         currentPeriod = periodScope.getCurrentPeriod();
         if (claimsSizeDistribution.get(currentPeriod) == null) {
            List<Double> returnPeriods = pmlData.getColumnByName("return period");
            List<Double> observations = pmlData.getColumnByName("maximum claim");
             List<Double> frequencies = new ArrayList<Double>();
            for (Double period: returnPeriods) {
                frequencies.add(1 / period);
            }
             List<Double> cumProbabilities = new ArrayList<Double>();
            for (Double frequency: frequencies) {
                 cumProbabilities.add(1d - frequency / frequencies.get(0));
             }
             Map<String, TableMultiDimensionalParameter> parameters = new HashMap<String, TableMultiDimensionalParameter>();
             TableMultiDimensionalParameter table = new TableMultiDimensionalParameter(
                     Arrays.asList(observations, cumProbabilities),
                     Arrays.asList("observations", "cumulative probabilities"));
             parameters.put("discreteEmpiricalCumulativeValues", table);
             claimsSizeDistribution.put(currentPeriod, DistributionType.getStrategy(DistributionType.DISCRETEEMPIRICALCUMULATIVE, parameters));
 
             double lambda = frequencies.get(0);
             if (claimsSizeModification.getType().equals(DistributionModifier.TRUNCATED)
                     || claimsSizeModification.getType().equals(DistributionModifier.TRUNCATEDSHIFT)) {
                 double min = (Double) claimsSizeModification.getParameters().get("min");
                 double max = (Double) claimsSizeModification.getParameters().get("max");
                 List<Double> observationsAndBoundaries = new ArrayList<Double>();
                 observationsAndBoundaries.addAll(observations);
                 observationsAndBoundaries.add(min);
                 observationsAndBoundaries.add(max);
                 Collections.sort(observationsAndBoundaries);
                 int indexMin = observationsAndBoundaries.indexOf(min);
                 int indexMax = observationsAndBoundaries.lastIndexOf(max);
                 if ((indexMax - 1) == observations.size()) {
                     lambda = frequencies.get(indexMin);
                 }
                 else {
                     lambda = frequencies.get(indexMin) - frequencies.get(indexMax - 2);
                 }
             }
             else if (claimsSizeModification.getType().equals(DistributionModifier.LEFTTRUNCATEDRIGHTCENSORED)) {
                 double min = (Double) claimsSizeModification.getParameters().get("min");
                 List<Double> observationsAndMin = new ArrayList<Double>();
                 observationsAndMin.addAll(observations);
                 observationsAndMin.add(min);
                 Collections.sort(observationsAndMin);
                 int indexMin = observationsAndMin.indexOf(min);
                 lambda = frequencies.get(indexMin);
             }
             Map<String, Double> lambdaParam = new HashMap<String, Double>();
             lambdaParam.put("lambda", lambda);
             frequencyDistribution.put(currentPeriod, FrequencyDistributionType.getStrategy(FrequencyDistributionType.POISSON, lambdaParam));
         }
     }
 
     public FrequencySeverityClaimType getProduceClaim() {
         return produceClaim;
     }
 }
