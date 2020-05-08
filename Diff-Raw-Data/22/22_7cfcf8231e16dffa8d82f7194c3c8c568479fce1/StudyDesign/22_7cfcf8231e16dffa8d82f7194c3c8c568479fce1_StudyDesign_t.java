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
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
 
 
 /**
  * Main Study Design object which holds
  * all lists, matrices etc.
  * 
  * @author Uttara Sakhadeo
  */
 public class StudyDesign implements Serializable
 {	
     private static final long serialVersionUID = -2761124691778704875L;
     /*--------------------
 	 * Member Variables
 	 *--------------------*/
 	// UUID for the study design.  Main unique identifier for the design
 	private byte[] uuid = null;
 	private String name = null;		
 	private boolean gaussianCovariate = false;	
 	private SolutionTypeEnum solutionTypeEnum;
 	private String participantLabel = null;
 		
 	private ConfidenceIntervalDescription confidenceIntervalDescriptions = null;	
 	private PowerCurveDescription powerCurveDescriptions = null;
 	
 	/* separate sets for list objects */	
 	private List<TypeIError> alphaList = null;
 	private List<BetaScale> betaScaleList = null;
 	private List<SigmaScale> sigmaScaleList = null;
 	private List<RelativeGroupSize> relativeGroupSizeList = null;
 	private List<StatisticalTest> statisticalTestList = null;
 	private List<PowerMethod> powerMethodList = null;
 	private List<Quantile> quantileList = null;
 	private List<NominalPower> nominalPowerList = null;	
 	private List<ResponseNode> responseList = null;	
 	private List<Double> perGroupSampleSizeList = null;
 			
 	/*private Map<String,NamedMatrix> matrixMap = null;*/
 		
     private List<BetweenParticipantFactor> betweenParticipantFactorList = null;   
    		
     //private Set<StudyDesignNamedMatrix> matrixSet = null;
 	private List<RepeatedMeasuresNode> repeatedMeasuresTree = null;
 	private List<ClusterNode> clusteringTree = null; 
 	
 	private Set<Hypothesis> hypotheses = new HashSet<Hypothesis>();	
 	private Set<Covariance> covariance = null;
	private Set<NamedMatrix> matrixSet = new HashSet<NamedMatrix>();
 		
 	/*--------------------
 	 * Constructors
 	 *--------------------*/
 	/**
 	 * Create an empty study design without a UUID assigned
 	 */
 	public StudyDesign() 
 	{}
 	/**
 	 * Create a study design object with the specified UUID
 	 * @param studyUUID unique identifier for the study design
 	 */
 	public StudyDesign(byte[] uuid) 
 	{
 		this.uuid = uuid;
 	}
 	/*--------------------
 	 * Getter/Setter Methods
 	 *--------------------*/
 	public byte[] getUuid() {
 		return uuid;
 	}
 	public void setUuid(byte[] uuid) {
 		this.uuid = uuid;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}	
 	public boolean isGaussianCovariate() {
 		return gaussianCovariate;
 	}
 	public void setGaussianCovariate(boolean gaussianCovariate) {
 		this.gaussianCovariate = gaussianCovariate;
 	}			
 	public List<ResponseNode> getResponseList() {
 		return responseList;
 	}
 	public void setResponseList(List<ResponseNode> responseList) {
 		this.responseList = responseList;
 	}	
 	public List<RepeatedMeasuresNode> getRepeatedMeasuresTree() {
 		return repeatedMeasuresTree;
 	}
 	public void setRepeatedMeasuresTree(
 			List<RepeatedMeasuresNode> repeatedMeasuresTree) {
 		this.repeatedMeasuresTree = repeatedMeasuresTree;
 	}			
 	public ConfidenceIntervalDescription getConfidenceIntervalDescriptions() {
 		return confidenceIntervalDescriptions;
 	}
 	public void setConfidenceIntervalDescriptions(
 			ConfidenceIntervalDescription confidenceIntervalDescriptions) {
 		this.confidenceIntervalDescriptions = confidenceIntervalDescriptions;
 	}	
 	public PowerCurveDescription getPowerCurveDescriptions() {
 		return powerCurveDescriptions;
 	}
 	public void setPowerCurveDescriptions(
 			PowerCurveDescription powerCurveDescriptions) {
 		this.powerCurveDescriptions = powerCurveDescriptions;
 	}
 	public Set<Hypothesis> getHypotheses() {
 		return hypotheses;
 	}
 	public void setHypotheses(Set<Hypothesis> hypotheses) {
 		this.hypotheses = hypotheses;
 	}	
 	public List<ClusterNode> getClusteringTree() {
 		return clusteringTree;
 	}
 	public void setClusteringTree(List<ClusterNode> clusteringTree) {
 		this.clusteringTree = clusteringTree;
 	}	
 	public SolutionTypeEnum getSolutionTypeEnum() {
 		return solutionTypeEnum;
 	}
 	public void setSolutionTypeEnum(SolutionTypeEnum solutionTypeEnum) {
 		this.solutionTypeEnum = solutionTypeEnum;
 	}
 	public List<BetaScale> getBetaScaleList() {
 		return betaScaleList;
 	}
 	public void setBetaScaleList(List<BetaScale> betaScaleList) {
 		this.betaScaleList = betaScaleList;
 	}
 	public List<SigmaScale> getSigmaScaleList() {
 		return sigmaScaleList;
 	}
 	public void setSigmaScaleList(List<SigmaScale> sigmaScaleList) {
 		this.sigmaScaleList = sigmaScaleList;
 	}
 	public List<RelativeGroupSize> getRelativeGroupSizeList() {
 		return relativeGroupSizeList;
 	}
 	public void setRelativeGroupSizeList(
 			List<RelativeGroupSize> relativeGroupSizeList) {
 		this.relativeGroupSizeList = relativeGroupSizeList;
 	}	
 	public List<StatisticalTest> getStatisticalTestList() {
 		return statisticalTestList;
 	}
 	public void setStatisticalTestList(List<StatisticalTest> statisticalTestList) {
 		this.statisticalTestList = statisticalTestList;
 	}
 	public List<PowerMethod> getPowerMethodList() {
 		return powerMethodList;
 	}
 	public void setPowerMethodList(List<PowerMethod> powerMethodList) {
 		this.powerMethodList = powerMethodList;
 	}
 	public List<Quantile> getQuantileList() {
 		return quantileList;
 	}
 	public void setQuantileList(List<Quantile> quantileList) {
 		this.quantileList = quantileList;
 	}
 	public List<Double> getPerGroupSampleSizeList() {
 		return perGroupSampleSizeList;
 	}
 	public void setPerGroupSampleSizeList(List<Double> perGroupSampleSizeList) {
 		this.perGroupSampleSizeList = perGroupSampleSizeList;
 	}	
 	public List<NominalPower> getNominalPowerList() {
 		return nominalPowerList;
 	}
 	public void setNominalPowerList(List<NominalPower> nominalPowerList) {
 		this.nominalPowerList = nominalPowerList;
 	}
 	public List<TypeIError> getAlphaList() {
 		return alphaList;
 	}
 	public void setAlphaList(List<TypeIError> alphaList) {
 		this.alphaList = alphaList;
 	}	
 	public List<BetweenParticipantFactor> getBetweenParticipantFactorList() {
 		return betweenParticipantFactorList;
 	}
 	public void setBetweenParticipantFactorList(
 			List<BetweenParticipantFactor> betweenParticipantFactorList) {
 		this.betweenParticipantFactorList = betweenParticipantFactorList;
 	}
 	public String getParticipantLabel() {
 		return participantLabel;
 	}
 	public void setParticipantLabel(String participantLabel) {
 		this.participantLabel = participantLabel;
 	}
 	public Set<Covariance> getCovariance() {
 		return covariance;
 	}
 	public void setCovariance(Set<Covariance> covariance) {
 		this.covariance = covariance;
 	}		
 	public Set<NamedMatrix> getMatrixSet() {
 		return matrixSet;
 	}
 	public void setMatrixSet(Set<NamedMatrix> matrixSet) {
 		this.matrixSet = matrixSet;
 	}
 	/*--------------------
 	 * Return/Store AlphaList values
 	 *--------------------*/
 		public List<Double> getAlphaListValues() 
 		{
 			List<TypeIError> list = this.getAlphaList();
 			List<Double> values = new ArrayList<Double>(list.size());		
 			for(TypeIError node: list)
 			{
 				values.add(node.getAlphaValue());
 			}
 			return values;
 		}
 		public void setAlphaListValues(List<Double> values) 
 		{
 			List<TypeIError> list = new ArrayList<TypeIError>(values.size());
 			for(double value : values)
 			{
 				list.add(new TypeIError(value));
 			}
 			this.setAlphaList(list);
 		}
 	/*--------------------
 	 * Return/Store BetaScaleList values
 	 *--------------------*/
 		public List<Double> getBetaScaleListValues() 
 		{
 			List<BetaScale> list = this.getBetaScaleList();
 			List<Double> values = new ArrayList<Double>(list.size());		
 			for(BetaScale node: list)
 			{
 				values.add(node.getValue());
 			}
 			return values;
 		}
 		public void setBetaScaleListValues(List<Double> values) 
 		{
 			List<BetaScale> list = new ArrayList<BetaScale>(values.size());
 			for(double value : values)
 			{
 				list.add(new BetaScale(value));
 			}
 			this.setBetaScaleList(list);
 		}	
 	/*--------------------
 	 * Return/Store SigmaScaleList values
 	 *--------------------*/
 		public List<Double> getSigmaScaleListValues() 
 		{
 			List<SigmaScale> list = this.getSigmaScaleList();
 			List<Double> values = new ArrayList<Double>(list.size());		
 			for(SigmaScale node: list)
 			{
 				values.add(node.getValue());
 			}
 			return values;
 		}
 		public void setSigmaScaleListValues(List<Double> values) 
 		{
 			List<SigmaScale> list = new ArrayList<SigmaScale>(values.size());
 			for(double value : values)
 			{
 				list.add(new SigmaScale(value));
 			}
 			this.setSigmaScaleList(list);
 		}
 	/*--------------------
 	 * Return/Store RelativeGroupSizeList values
 	 *--------------------*/
 		public List<Integer> getRelativeGroupSizeListValues() 
 		{
 			List<RelativeGroupSize> list = this.getRelativeGroupSizeList();
 			List<Integer> values = new ArrayList<Integer>(list.size());		
 			for(RelativeGroupSize node: list)
 			{
 				values.add(node.getValue());
 			}
 			return values;
 		}
 		public void setRelativeGroupSizeListValues(List<Integer> values) 
 		{
 			List<RelativeGroupSize> list = new ArrayList<RelativeGroupSize>(values.size());
 			for(int value : values)
 			{
 				list.add(new RelativeGroupSize(value));
 			}
 			this.setRelativeGroupSizeList(list);
 		}
 	/*--------------------
 	 * Return/Store StatisticalTestList values
 	 *--------------------*/
 		public List<StatisticalTestTypeEnum> getStatisticalTestListValues() 
 		{
 			List<StatisticalTest> list = this.getStatisticalTestList();
 			List<StatisticalTestTypeEnum> values = new ArrayList<StatisticalTestTypeEnum>(list.size());		
 			for(StatisticalTest node: list)
 			{
 				values.add(node.getType());
 			}
 			return values;
 		}
 		public void setStatisticalTestListValues(List<StatisticalTestTypeEnum> values) 
 		{
 			List<StatisticalTest> list = new ArrayList<StatisticalTest>(values.size());
 			for(StatisticalTestTypeEnum value : values)
 			{
 				list.add(new StatisticalTest(value));
 			}
 			this.setStatisticalTestList(list);
 		}
 	/*--------------------
 	 * Return/Store PowerMethodList values
 	 *--------------------*/
 		public List<PowerMethodEnum> getPowerMethodListValues() 
 		{
 			List<PowerMethod> list = this.getPowerMethodList();
 			List<PowerMethodEnum> values = new ArrayList<PowerMethodEnum>(list.size());		
 			for(PowerMethod node: list)
 			{
 				values.add(node.getPowerMethodEnum());
 			}
 			return values;
 		}
 		public void setPowerMethodListValues(List<PowerMethodEnum> values) 
 		{
 			List<PowerMethod> list = new ArrayList<PowerMethod>(values.size());
 			for(PowerMethodEnum value : values)
 			{
 				list.add(new PowerMethod(value));
 			}
 			this.setPowerMethodList(list);
 		}
 	/*--------------------
 	 * Return/Store QuantileList names
 	 *--------------------*/
 		public List<Double> getQuantileListValues() 
 		{
 			List<Quantile> list = this.getQuantileList();
 			List<Double> values = new ArrayList<Double>(list.size());		
 			for(Quantile node: quantileList)
 			{
 				values.add(node.getValue());
 			}
 			return values;
 		}
 		public void setQuantileListValues(List<Double> values) 
 		{
 			List<Quantile> list = new ArrayList<Quantile>(values.size());
 			for(double value : values)
 			{
 				list.add(new Quantile(value));
 			}
 			this.setQuantileList(list);
 		}
 	/*--------------------
 	 * Return/Store NominalPowerList values
 	 *--------------------*/
 		public List<Double> getNominalPowerListValues() 
 		{
 			List<NominalPower> list = this.getNominalPowerList();
 			List<Double> values = new ArrayList<Double>(list.size());		
 			for(NominalPower node: list)
 			{
 				values.add(node.getValue());
 			}
 			return values;
 		}
 		public void setNominalPowerListValues(List<Double> values) 
 		{
 			List<NominalPower> list = new ArrayList<NominalPower>(values.size());
 			for(double value : values)
 			{
 				list.add(new NominalPower(value));
 			}
 			this.setNominalPowerList(list);
 		}
 	/*--------------------
 	 * Return/Store ResponseList names
 	 *--------------------*/
 		public List<String> getResponseListNames() 
 		{
 			List<ResponseNode> list = this.getResponseList();
 			List<String> responses = new ArrayList<String>(list.size());		
 			for(ResponseNode node: responseList)
 			{
 				responses.add(node.getName());
 			}
 			return responses;
 		}
 		public void setResponseListNames(List<String> values) 
 		{
 			List<ResponseNode> list = new ArrayList<ResponseNode>(values.size());
 			for(String name : values)
 			{
 				list.add(new ResponseNode(name));
 			}
 			this.setResponseList(list);
 		}	
 	/*--------------------
 	 * Return specific Matrix
 	 *--------------------*/
 		/*
 		 * Convinience method for checking existance
 		 * of a matrix.
 		 */
 		public boolean hasNamedMatrix(String name)
 		{
 			boolean flag = false;
 			Set<NamedMatrix> matrixSet = this.getMatrixSet();
 			if(matrixSet!=null)
 			{			
 				Iterator<NamedMatrix> iterator = matrixSet.iterator();
 				while(iterator.hasNext())
 				{		
 					NamedMatrix matrix = iterator.next();
 					if (matrix==null) {
 						/*
 						 * 
 						 */
 					}
 					String matrixName = matrix.getName();
 					if(matrixName!=null && name.equals(matrixName))
 						flag = true;				
 				}
 			}		
 			return flag;
 		}
 		/*
 		 * Convinience method for retrieving a matrix
 		 * by its name.
 		 */
 		public NamedMatrix getNamedMatrix(String name)
 		{
 			NamedMatrix matrix = null;	
 			Set<NamedMatrix> matrixSet = this.getMatrixSet();
			if (matrixSet!=null)
 			{						
 				Iterator<NamedMatrix> iterator = matrixSet.iterator();
				while (iterator.hasNext())
 				{
 					matrix = iterator.next();
 					String matrixName = matrix.getName();
					if (matrixName != null && name.equals(matrixName))
 					{
 						break;
 					}
 					else if(matrixName==null || !name.equals(matrixName));
 						matrix = null;									
 				}						
 			}			
 			return matrix;
 		}	
 		/*
 		 * Convinience method for setting particular matrix
 		 * in a Set.
 		 */
 		public void setNamedMatrix(NamedMatrix matrix) 
 		{		
 			Set<NamedMatrix> matrixSet = this.getMatrixSet();
 			
			NamedMatrix originalMatrix = this.getNamedMatrix(matrix.getName());
			if (originalMatrix != null) matrixSet.remove(originalMatrix);						
 			
 			matrixSet.add(matrix);	
			//this.setMatrixSet(matrixSet);
 		}		
 	/*--------------------
 	 * toString()
 	 *--------------------*/	
 	@Override
 	public String toString() {
 		return "StudyDesign [uuid=" + Arrays.toString(uuid) + ", name="
 				+ name + ", gaussianCovariate=" + gaussianCovariate
 				+ ", solutionTypeEnum=" + solutionTypeEnum
 				+ ", participantLabel=" + participantLabel
 				+ ", confidenceIntervalDescriptions="
 				+ confidenceIntervalDescriptions
 				+ ", powerCurveDescriptions=" + powerCurveDescriptions
 				+ ", alphaList=" + alphaList + ", betaScaleList="
 				+ betaScaleList + ", sigmaScaleList=" + sigmaScaleList
 				+ ", relativeGroupSizeList=" + relativeGroupSizeList
 				+ ", statisticalTestList=" + statisticalTestList
 				+ ", powerMethodList=" + powerMethodList
 				+ ", quantileList=" + quantileList + ", nominalPowerList="
 				+ nominalPowerList + ", responseList=" + responseList
 				+ ", perGroupSampleSizeList=" + perGroupSampleSizeList
 				+ ", betweenParticipantFactorList="
 				+ betweenParticipantFactorList + ", repeatedMeasuresTree="
 				+ repeatedMeasuresTree + ", clusteringTree="
 				+ clusteringTree + ", hypotheses=" + hypotheses
 				+ ", covariance=" + covariance + ", matrixSet=" + matrixSet
 				+ "]";
 	}
 }
