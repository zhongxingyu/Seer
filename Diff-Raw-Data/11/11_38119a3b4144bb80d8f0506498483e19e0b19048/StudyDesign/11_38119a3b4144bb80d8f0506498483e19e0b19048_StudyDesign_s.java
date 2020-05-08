 /*
  * Web service utility functions for managing hibernate, json, etc.
  *
  * Copyright (C) 2010 Regents of the University of Colorado.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor,
  * Boston, MA  02110-1301, USA.
  */
 package edu.ucdenver.bios.webservice.common.domain;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import edu.ucdenver.bios.webservice.common.enums.PowerMethodEnum;
 import edu.ucdenver.bios.webservice.common.enums.SolutionTypeEnum;
 import edu.ucdenver.bios.webservice.common.enums.StatisticalTestTypeEnum;
 import edu.ucdenver.bios.webservice.common.enums.StudyDesignViewTypeEnum;
 
 /**
  * Main Study Design object which holds all lists, matrices etc.
  *
  * @author Uttara Sakhadeo
  */
 public class StudyDesign implements Serializable {
 
     /** The Constant serialVersionUID. */
     private static final long serialVersionUID = -2761124691778704875L;
     /*--------------------
      * Member Variables
      *--------------------*/
     // UUID for the study design. Main unique identifier for the design
     /** The uuid. */
     private byte[] uuid = null;
 
     /** The name. */
     private String name = null;
 
     /** The gaussian covariate. */
     private boolean gaussianCovariate = false;
 
     /** The solution type enum. */
     private SolutionTypeEnum solutionTypeEnum;
 
     /** The participant label. */
     private String participantLabel = null;
 
     /** The view type enum. */
     private StudyDesignViewTypeEnum viewTypeEnum = null;
 
     /** The confidence interval descriptions. */
     private ConfidenceIntervalDescription confidenceIntervalDescriptions = null;
 
     /** The power curve descriptions. */
     private PowerCurveDescription powerCurveDescriptions = null;
 
     /* separate sets for list objects */
     /** The alpha list. */
     private List<TypeIError> alphaList = null;
 
     /** The beta scale list. */
     private List<BetaScale> betaScaleList = null;
 
     /** The sigma scale list. */
     private List<SigmaScale> sigmaScaleList = null;
 
     /** The relative group size list. */
     private List<RelativeGroupSize> relativeGroupSizeList = null;
 
     /** The sample size list. */
     private List<SampleSize> sampleSizeList = null;
 
     /** The statistical test list. */
     private List<StatisticalTest> statisticalTestList = null;
 
     /** The power method list. */
     private List<PowerMethod> powerMethodList = null;
 
     /** The quantile list. */
     private List<Quantile> quantileList = null;
 
     /** The nominal power list. */
     private List<NominalPower> nominalPowerList = null;
 
     /** The response list. */
     private List<ResponseNode> responseList = null;
 
     /* private Map<String,NamedMatrix> matrixMap = null; */
 
     /** The between participant factor list. */
     private List<BetweenParticipantFactor> betweenParticipantFactorList = null;
 
     // private Set<StudyDesignNamedMatrix> matrixSet = null;
     /** The repeated measures tree. */
     private List<RepeatedMeasuresNode> repeatedMeasuresTree = null;
 
     /** The clustering tree. */
     private List<ClusterNode> clusteringTree = null;
 
     /** The hypothesis. */
     private Set<Hypothesis> hypothesis = null;
 
     /** The covariance. */
     private Set<Covariance> covariance = null;
 
     /** The matrix set. */
     private Set<NamedMatrix> matrixSet = null;
     
     /*--------------------
      * Constructors
      *--------------------*/
     /**
      * Create an empty study design without a UUID assigned.
      */
     public StudyDesign() {
     }
 
     /**
      * Create a study design object with the specified UUID.
      *
      * @param uuid
      *            the uuid
      */
     public StudyDesign(final byte[] uuid) {
         this.uuid = uuid;
     }
 
     /*--------------------
      * Getter/Setter Methods
      *--------------------*/
     /**
      * Gets the uuid.
      *
      * @return the uuid
      */
     public final byte[] getUuid() {
         return uuid;
     }
 
     /**
      * Sets the uuid.
      *
      * @param uuid
      *            the new uuid
      */
     public final void setUuid(final byte[] uuid) {
         this.uuid = uuid;
     }
 
     /**
      * Gets the name.
      *
      * @return the name
      */
     public final String getName() {
         return name;
     }
 
     /**
      * Sets the name.
      *
      * @param name
      *            the new name
      */
     public final void setName(final String name) {
         this.name = name;
     }
 
     /**
      * Checks if is gaussian covariate.
      *
      * @return true, if is gaussian covariate
      */
     public final boolean isGaussianCovariate() {
         return gaussianCovariate;
     }
 
     /**
      * Sets the gaussian covariate.
      *
      * @param gaussianCovariate
      *            the new gaussian covariate
      */
     public final void setGaussianCovariate(final boolean gaussianCovariate) {
         this.gaussianCovariate = gaussianCovariate;
     }
 
     /**
      * Gets the response list.
      *
      * @return the response list
      */
     public final List<ResponseNode> getResponseList() {
         return responseList;
     }
 
     /**
      * Sets the response list.
      *
      * @param responseList
      *            the new response list
      */
     public final void setResponseList(final List<ResponseNode> responseList) {
         this.responseList = responseList;
     }
 
     /**
      * Gets the repeated measures tree.
      *
      * @return the repeated measures tree
      */
     public final List<RepeatedMeasuresNode> getRepeatedMeasuresTree() {
         return repeatedMeasuresTree;
     }
 
     /**
      * Sets the repeated measures tree.
      *
      * @param repeatedMeasuresTree
      *            the new repeated measures tree
      */
     public final void setRepeatedMeasuresTree(
             final List<RepeatedMeasuresNode> repeatedMeasuresTree) {
         this.repeatedMeasuresTree = repeatedMeasuresTree;
     }
 
     /**
      * Gets the confidence interval descriptions.
      *
      * @return the confidence interval descriptions
      */
     public final ConfidenceIntervalDescription
     getConfidenceIntervalDescriptions() {
         return confidenceIntervalDescriptions;
     }
 
     /**
      * Sets the confidence interval descriptions.
      *
      * @param confidenceIntervalDescriptions
      *            the new confidence interval descriptions
      */
     public final void setConfidenceIntervalDescriptions(
         final ConfidenceIntervalDescription confidenceIntervalDescriptions) {
         this.confidenceIntervalDescriptions = confidenceIntervalDescriptions;
     }
 
     /**
      * Gets the power curve descriptions.
      *
      * @return the power curve descriptions
      */
     public final PowerCurveDescription getPowerCurveDescriptions() {
         return powerCurveDescriptions;
     }
 
     /**
      * Sets the power curve descriptions.
      *
      * @param powerCurveDescriptions
      *            the new power curve descriptions
      */
     public final void setPowerCurveDescriptions(
             final PowerCurveDescription powerCurveDescriptions) {
         this.powerCurveDescriptions = powerCurveDescriptions;
     }
 
     /**
      * Gets the hypothesis.
      *
      * @return the hypothesis
      */
     public final Set<Hypothesis> getHypothesis() {
         return hypothesis;
     }
 
     /**
      * Sets the hypothesis.
      *
      * @param hypotheses
      *            the new hypothesis
      */
     public final void setHypothesis(final Set<Hypothesis> hypotheses) {
         this.hypothesis = hypotheses;
     }
 
     /**
      * Gets the clustering tree.
      *
      * @return the clustering tree
      */
     public final List<ClusterNode> getClusteringTree() {
         return clusteringTree;
     }
 
     /**
      * Sets the clustering tree.
      *
      * @param clusteringTree
      *            the new clustering tree
      */
     public final void setClusteringTree(
             final List<ClusterNode> clusteringTree) {
         this.clusteringTree = clusteringTree;
     }
 
     /**
      * Gets the solution type enum.
      *
      * @return the solution type enum
      */
     public final SolutionTypeEnum getSolutionTypeEnum() {
         return solutionTypeEnum;
     }
 
     /**
      * Sets the solution type enum.
      *
      * @param solutionTypeEnum
      *            the new solution type enum
      */
     public final void setSolutionTypeEnum(
             final SolutionTypeEnum solutionTypeEnum) {
         this.solutionTypeEnum = solutionTypeEnum;
     }
 
     /**
      * Gets the beta scale list.
      *
      * @return the beta scale list
      */
     public final List<BetaScale> getBetaScaleList() {
         return betaScaleList;
     }
 
     /**
      * Sets the beta scale list.
      *
      * @param betaScaleList
      *            the new beta scale list
      */
     public final void setBetaScaleList(final List<BetaScale> betaScaleList) {
         this.betaScaleList = betaScaleList;
     }
 
     /**
      * Gets the sigma scale list.
      *
      * @return the sigma scale list
      */
     public final List<SigmaScale> getSigmaScaleList() {
         return sigmaScaleList;
     }
 
     /**
      * Sets the sigma scale list.
      *
      * @param sigmaScaleList
      *            the new sigma scale list
      */
     public final void setSigmaScaleList(final List<SigmaScale> sigmaScaleList) {
         this.sigmaScaleList = sigmaScaleList;
     }
 
     /**
      * Gets the relative group size list.
      *
      * @return the relative group size list
      */
     public final List<RelativeGroupSize> getRelativeGroupSizeList() {
         return relativeGroupSizeList;
     }
 
     /**
      * Sets the relative group size list.
      *
      * @param relativeGroupSizeList
      *            the new relative group size list
      */
     public final void setRelativeGroupSizeList(
             final List<RelativeGroupSize> relativeGroupSizeList) {
         this.relativeGroupSizeList = relativeGroupSizeList;
     }
 
     /**
      * Gets the statistical test list.
      *
      * @return the statistical test list
      */
     public final List<StatisticalTest> getStatisticalTestList() {
         return statisticalTestList;
     }
 
     /**
      * Sets the statistical test list.
      *
      * @param statisticalTestList
      *            the new statistical test list
      */
     public final void setStatisticalTestList(
             final List<StatisticalTest> statisticalTestList) {
         this.statisticalTestList = statisticalTestList;
     }
 
     /**
      * Gets the power method list.
      *
      * @return the power method list
      */
     public final List<PowerMethod> getPowerMethodList() {
         return powerMethodList;
     }
 
     /**
      * Sets the power method list.
      *
      * @param powerMethodList
      *            the new power method list
      */
     public final void setPowerMethodList(
             final List<PowerMethod> powerMethodList) {
         this.powerMethodList = powerMethodList;
     }
 
     /**
      * Gets the quantile list.
      *
      * @return the quantile list
      */
     public final List<Quantile> getQuantileList() {
         return quantileList;
     }
 
     /**
      * Sets the quantile list.
      *
      * @param quantileList
      *            the new quantile list
      */
     public final void setQuantileList(final List<Quantile> quantileList) {
         this.quantileList = quantileList;
     }
 
     /**
      * Gets the nominal power list.
      *
      * @return the nominal power list
      */
     public final List<NominalPower> getNominalPowerList() {
         return nominalPowerList;
     }
 
     /**
      * Sets the nominal power list.
      *
      * @param nominalPowerList
      *            the new nominal power list
      */
     public final void setNominalPowerList(
             final List<NominalPower> nominalPowerList) {
         this.nominalPowerList = nominalPowerList;
     }
 
     /**
      * Gets the alpha list.
      *
      * @return the alpha list
      */
     public final List<TypeIError> getAlphaList() {
         return alphaList;
     }
 
     /**
      * Sets the alpha list.
      *
      * @param alphaList
      *            the new alpha list
      */
     public final void setAlphaList(final List<TypeIError> alphaList) {
         this.alphaList = alphaList;
     }
 
     /**
      * Gets the between participant factor list.
      *
      * @return the between participant factor list
      */
     public final List<BetweenParticipantFactor>
     getBetweenParticipantFactorList() {
         return betweenParticipantFactorList;
     }
 
     /**
      * Sets the between participant factor list.
      *
      * @param betweenParticipantFactorList
      *            the new between participant factor list
      */
     public final void setBetweenParticipantFactorList(
             final List<BetweenParticipantFactor> betweenParticipantFactorList) {
         this.betweenParticipantFactorList = betweenParticipantFactorList;
     }
 
     /**
      * Gets the participant label.
      *
      * @return the participant label
      */
     public final String getParticipantLabel() {
         return participantLabel;
     }
 
     /**
      * Sets the participant label.
      *
      * @param participantLabel
      *            the new participant label
      */
     public final void setParticipantLabel(final String participantLabel) {
         this.participantLabel = participantLabel;
     }
 
     /**
      * Gets the covariance.
      *
      * @return the covariance
      */
     public final Set<Covariance> getCovariance() {
         return covariance;
     }
 
     /**
      * Sets the covariance.
      *
      * @param covariance
      *            the new covariance
      */
     public final void setCovariance(final Set<Covariance> covariance) {
         this.covariance = covariance;
     }
 
     /**
      * Gets the matrix set.
      *
      * @return the matrix set
      */
     public final Set<NamedMatrix> getMatrixSet() {
         return matrixSet;
     }
 
     /**
      * Sets the matrix set.
      *
      * @param matrixSet
      *            the new matrix set
      */
     public final void setMatrixSet(final Set<NamedMatrix> matrixSet) {
         this.matrixSet = matrixSet;
     }
 
     /**
      * Gets the sample size list.
      *
      * @return the sample size list
      */
     public final List<SampleSize> getSampleSizeList() {
         return sampleSizeList;
     }
 
     /**
      * Sets the sample size list.
      *
      * @param sampleSizeList
      *            the new sample size list
      */
     public final void setSampleSizeList(final List<SampleSize> sampleSizeList) {
         this.sampleSizeList = sampleSizeList;
     }
 
     /**
      * Gets the view type enum.
      *
      * @return the view type enum
      */
     public final StudyDesignViewTypeEnum getViewTypeEnum() {
         return viewTypeEnum;
     }
 
     /**
      * Sets the view type enum.
      *
      * @param viewType
      *            the new view type enum
      */
     public final void setViewTypeEnum(final StudyDesignViewTypeEnum viewType) {
         this.viewTypeEnum = viewType;
     }
 
     /*--------------------
      * Return/Store AlphaList values
      *--------------------*/
     /**
      * Gets the alpha list values.
      *
      * @return the alpha list values
      */
     public final List<Double> getAlphaListValues() {
         List<TypeIError> list = this.getAlphaList();
         List<Double> values = new ArrayList<Double>(list.size());
         for (TypeIError node : list) {
             values.add(node.getAlphaValue());
         }
         return values;
     }
 
     /**
      * Sets the alpha list values.
      *
      * @param values
      *            the new alpha list values
      */
     public final void setAlphaListValues(final List<Double> values) {
         List<TypeIError> list = new ArrayList<TypeIError>(values.size());
         for (double value : values) {
             list.add(new TypeIError(value));
         }
         this.setAlphaList(list);
     }
 
     /*--------------------
      * Return/Store BetaScaleList values
      *--------------------*/
     /**
      * Gets the beta scale list values.
      *
      * @return the beta scale list values
      */
     public final List<Double> getBetaScaleListValues() {
         List<BetaScale> list = this.getBetaScaleList();
         List<Double> values = new ArrayList<Double>(list.size());
         for (BetaScale node : list) {
             values.add(node.getValue());
         }
         return values;
     }
 
     /**
      * Sets the beta scale list values.
      *
      * @param values
      *            the new beta scale list values
      */
     public final void setBetaScaleListValues(final List<Double> values) {
         List<BetaScale> list = new ArrayList<BetaScale>(values.size());
         for (double value : values) {
             list.add(new BetaScale(value));
         }
         this.setBetaScaleList(list);
     }
 
     /*--------------------
      * Return/Store SigmaScaleList values
      *--------------------*/
     /**
      * Gets the sigma scale list values.
      *
      * @return the sigma scale list values
      */
     public final List<Double> getSigmaScaleListValues() {
         List<SigmaScale> list = this.getSigmaScaleList();
         List<Double> values = new ArrayList<Double>(list.size());
         for (SigmaScale node : list) {
             values.add(node.getValue());
         }
         return values;
     }
 
     /**
      * Sets the sigma scale list values.
      *
      * @param values
      *            the new sigma scale list values
      */
     public final void setSigmaScaleListValues(final List<Double> values) {
         List<SigmaScale> list = new ArrayList<SigmaScale>(values.size());
         for (double value : values) {
             list.add(new SigmaScale(value));
         }
         this.setSigmaScaleList(list);
     }
 
     /*--------------------
      * Return/Store RelativeGroupSizeList values
      *--------------------*/
     /**
      * Gets the relative group size list values.
      *
      * @return the relative group size list values
      */
     public final List<Integer> getRelativeGroupSizeListValues() {
         List<RelativeGroupSize> list = this.getRelativeGroupSizeList();
         List<Integer> values = new ArrayList<Integer>(list.size());
         for (RelativeGroupSize node : list) {
             values.add(node.getValue());
         }
         return values;
     }
 
     /**
      * Sets the relative group size list values.
      *
      * @param values
      *            the new relative group size list values
      */
     public final void setRelativeGroupSizeListValues(
             final List<Integer> values) {
         List<RelativeGroupSize> list = new ArrayList<RelativeGroupSize>(
                 values.size());
         for (int value : values) {
             list.add(new RelativeGroupSize(value));
         }
         this.setRelativeGroupSizeList(list);
     }
 
     /*--------------------
      * Return/Store StatisticalTestList values
      *--------------------*/
     /**
      * Gets the statistical test list values.
      *
      * @return the statistical test list values
      */
     public final List<StatisticalTestTypeEnum> getStatisticalTestListValues() {
         List<StatisticalTest> list = this.getStatisticalTestList();
         List<StatisticalTestTypeEnum> values =
                 new ArrayList<StatisticalTestTypeEnum>(
                 list.size());
         for (StatisticalTest node : list) {
             values.add(node.getType());
         }
         return values;
     }
 
     /**
      * Sets the statistical test list values.
      *
      * @param values
      *            the new statistical test list values
      */
     public final void setStatisticalTestListValues(
             final List<StatisticalTestTypeEnum> values) {
         List<StatisticalTest> list = new ArrayList<StatisticalTest>(
                 values.size());
         for (StatisticalTestTypeEnum value : values) {
             list.add(new StatisticalTest(value));
         }
         this.setStatisticalTestList(list);
     }
 
     /*--------------------
      * Return/Store PowerMethodList values
      *--------------------*/
     /**
      * Gets the power method list values.
      *
      * @return the power method list values
      */
     public final List<PowerMethodEnum> getPowerMethodListValues() {
         List<PowerMethod> list = this.getPowerMethodList();
         List<PowerMethodEnum> values = new ArrayList<PowerMethodEnum>(
                 list.size());
         for (PowerMethod node : list) {
             values.add(node.getPowerMethodEnum());
         }
         return values;
     }
 
     /**
      * Sets the power method list values.
      *
      * @param values
      *            the new power method list values
      */
     public final void setPowerMethodListValues(
             final List<PowerMethodEnum> values) {
         List<PowerMethod> list = new ArrayList<PowerMethod>(values.size());
         for (PowerMethodEnum value : values) {
             list.add(new PowerMethod(value));
         }
         this.setPowerMethodList(list);
     }
 
     /*--------------------
      * Return/Store QuantileList names
      *--------------------*/
     /**
      * Gets the quantile list values.
      *
      * @return the quantile list values
      */
     public final List<Double> getQuantileListValues() {
         List<Quantile> list = this.getQuantileList();
         List<Double> values = new ArrayList<Double>(list.size());
         for (Quantile node : quantileList) {
             values.add(node.getValue());
         }
         return values;
     }
 
     /**
      * Sets the quantile list values.
      *
      * @param values
      *            the new quantile list values
      */
     public final void setQuantileListValues(final List<Double> values) {
         List<Quantile> list = new ArrayList<Quantile>(values.size());
         for (double value : values) {
             list.add(new Quantile(value));
         }
         this.setQuantileList(list);
     }
 
     /*--------------------
      * Return/Store NominalPowerList values
      *--------------------*/
     /**
      * Gets the nominal power list values.
      *
      * @return the nominal power list values
      */
     public final List<Double> getNominalPowerListValues() {
         List<NominalPower> list = this.getNominalPowerList();
         List<Double> values = new ArrayList<Double>(list.size());
         for (NominalPower node : list) {
             values.add(node.getValue());
         }
         return values;
     }
 
     /**
      * Sets the nominal power list values.
      *
      * @param values
      *            the new nominal power list values
      */
     public final void setNominalPowerListValues(final List<Double> values) {
         List<NominalPower> list = new ArrayList<NominalPower>(values.size());
         for (double value : values) {
             list.add(new NominalPower(value));
         }
         this.setNominalPowerList(list);
     }
 
     /*--------------------
      * Return/Store ResponseList names
      *--------------------*/
     /**
      * Gets the response list names.
      *
      * @return the response list names
      */
     public final List<String> getResponseListNames() {
         List<ResponseNode> list = this.getResponseList();
         List<String> responses = new ArrayList<String>(list.size());
         for (ResponseNode node : responseList) {
             responses.add(node.getName());
         }
         return responses;
     }
 
     /**
      * Sets the response list names.
      *
      * @param values
      *            the new response list names
      */
     public final void setResponseListNames(final List<String> values) {
         List<ResponseNode> list = new ArrayList<ResponseNode>(values.size());
         for (String name : values) {
             list.add(new ResponseNode(name));
         }
         this.setResponseList(list);
     }
 
     /*--------------------
      * Return/Store SampleSize values
      *--------------------*/
     /**
      * Gets the sample size list values.
      *
      * @return the sample size list values
      */
     public final List<Integer> getSampleSizeListValues() {
         List<SampleSize> list = this.getSampleSizeList();
         List<Integer> values = new ArrayList<Integer>(list.size());
         for (SampleSize node : list) {
             values.add(node.getValue());
         }
         return values;
     }
 
     /**
      * Sets the sample size list values.
      *
      * @param values
      *            the new sample size list values
      */
     public final void setSampleSizeListValues(final List<Integer> values) {
         List<SampleSize> list = new ArrayList<SampleSize>(values.size());
         for (int value : values) {
             list.add(new SampleSize(value));
         }
         this.setSampleSizeList(list);
     }
 
     /*--------------------
      * Return specific Matrix
      *--------------------*/
     /*
      * Convinience method for checking existance of a matrix.
      */
     /**
      * Checks for named matrix.
      *
      * @param name
      *            the name
      * @return true, if successful
      */
     public final boolean hasNamedMatrix(final String name) {
         boolean flag = false;
         Set<NamedMatrix> matrixSet = this.getMatrixSet();
         if (matrixSet != null) {
             Iterator<NamedMatrix> iterator = matrixSet.iterator();
             while (iterator.hasNext()) {
                 NamedMatrix matrix = iterator.next();
                 if (matrix == null) {
                     /*
 						 * 
 						 */
                 }
                 String matrixName = matrix.getName();
                 if (matrixName != null && name.equals(matrixName)) {
                     flag = true;
                 }
             }
         }
         return flag;
     }
 
     /*
      * Convinience method for retrieving a matrix by its name.
      */
     /**
      * Gets the named matrix.
      *
      * @param name
      *            the name
      * @return the named matrix
      */
     public final NamedMatrix getNamedMatrix(final String name) {
         NamedMatrix matrix = null;
         if (matrixSet != null) {
             Iterator<NamedMatrix> iterator = matrixSet.iterator();
             while (iterator.hasNext()) {
                 matrix = iterator.next();
                 String matrixName = matrix.getName();
                 if (matrixName != null && name.equals(matrixName)) {
                     break;
                 } else if (matrixName == null || !name.equals(matrixName))
                     ;
                 matrix = null;
             }
         }
         return matrix;
     }
 
     /*
      * Convinience method for setting particular matrix in a Set.
      */
     /**
      * Sets the named matrix.
      *
      * @param matrix
      *            the new named matrix
      */
     public final void setNamedMatrix(final NamedMatrix matrix) {
         if (matrixSet == null) {
             matrixSet = new HashSet<NamedMatrix>();
        } else {
            if (hasNamedMatrix(matrix.getName())) {
                NamedMatrix originalMatrix = this.getNamedMatrix(name);
                matrixSet.remove(originalMatrix);
            }
         }
         matrixSet.add(matrix);
     }
 
     /*--------------------
      * toString()
      *--------------------*/
     /*
      * (non-Javadoc)
      *
      * @see java.lang.Object#toString()
      */
     @Override
     public final String toString() {
         return "StudyDesign [uuid=" + Arrays.toString(uuid) + ", name=" + name
                 + ", gaussianCovariate=" + gaussianCovariate
                 + ", solutionTypeEnum=" + solutionTypeEnum
                 + ", participantLabel=" + participantLabel + ", viewTypeEnum="
                 + viewTypeEnum + ", confidenceIntervalDescriptions="
                 + confidenceIntervalDescriptions + ", powerCurveDescriptions="
                 + powerCurveDescriptions + ", alphaList=" + alphaList
                 + ", betaScaleList=" + betaScaleList + ", sigmaScaleList="
                 + sigmaScaleList + ", relativeGroupSizeList="
                 + relativeGroupSizeList + ", sampleSizeList=" + sampleSizeList
                 + ", statisticalTestList=" + statisticalTestList
                 + ", powerMethodList=" + powerMethodList + ", quantileList="
                 + quantileList + ", nominalPowerList=" + nominalPowerList
                 + ", responseList=" + responseList
                 + ", betweenParticipantFactorList="
                 + betweenParticipantFactorList + ", repeatedMeasuresTree="
                 + repeatedMeasuresTree + ", clusteringTree=" + clusteringTree
                 + ", hypothesis=" + hypothesis + ", covariance=" + covariance
                 + ", matrixSet=" + matrixSet + "]";
     }
     
 }
