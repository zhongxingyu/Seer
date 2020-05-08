 package org.geworkbench.bison.datastructure.complex.pattern;
 
 /**
  * Created by IntelliJ IDEA.
  * User: xiaoqing
  * Date: Jan 24, 2007
  * Time: 5:35:31 PM
  * To change this template use File | Settings | File Templates.
  */
 
 import java.io.Serializable;
 import java.io.File;
 
 // Referenced classes of package polgara.soapPD_wsdl:
 //            Exhaustive, Hierarchical, ProfileHMM
 
 public class Parameters
     implements Serializable
 {
 
     public Parameters()
     {
         __equalsCalc = null;
         __hashCodeCalc = false;
     }
 
     public int getMinSupport()
     {
         return minSupport;
     }
 
     public void setMinSupport(int i)
     {
         minSupport = i;
     }
 
     public int getMinTokens()
     {
         return minTokens;
     }
 
     public void setMinTokens(int i)
     {
         minTokens = i;
     }
 
     public int getWindow()
     {
         return window;
     }
 
     public void setWindow(int i)
     {
         window = i;
     }
 
     public int getMinWTokens()
     {
         return minWTokens;
     }
 
     public void setMinWTokens(int i)
     {
         minWTokens = i;
     }
 
     public int getExactTokens()
     {
         return exactTokens;
     }
 
     public void setExactTokens(int i)
     {
         exactTokens = i;
     }
 
     public int getCountSeq()
     {
         return countSeq;
     }
 
     public void setCountSeq(int i)
     {
         countSeq = i;
     }
 
     public int getExact()
     {
         return exact;
     }
 
     public void setExact(int i)
     {
         exact = i;
     }
 
     public int getPrintDetails()
     {
         return printDetails;
     }
 
     public void setPrintDetails(int i)
     {
         printDetails = i;
     }
 
     public int getGroupingType()
     {
         return groupingType;
     }
 
     public void setGroupingType(int i)
     {
         groupingType = i;
     }
 
     public int getGroupingN()
     {
         return groupingN;
     }
 
     public void setGroupingN(int i)
     {
         groupingN = i;
     }
 
     public int getSortMode()
     {
         return sortMode;
     }
 
     public void setSortMode(int i)
     {
         sortMode = i;
     }
 
     public int getOutputMode()
     {
         return outputMode;
     }
 
     public void setOutputMode(int i)
     {
         outputMode = i;
     }
 
     public double getMinPer100Support()
     {
         return minPer100Support;
     }
 
     public void setMinPer100Support(double d)
     {
         minPer100Support = d;
     }
 
     public int getComputePValue()
     {
         return computePValue;
     }
 
     public void setComputePValue(int i)
     {
         computePValue = i;
     }
 
     public double getMinPValue()
     {
         return minPValue;
     }
 
     public void setMinPValue(double d)
     {
         minPValue = d;
     }
 
     public int getThreadNo()
     {
         return threadNo;
     }
 
     public void setThreadNo(int i)
     {
         threadNo = i;
     }
 
     public int getThreadId()
     {
         return threadId;
     }
 
     public void setThreadId(int i)
     {
         threadId = i;
     }
 
     public int getMinPatternNo()
     {
         return minPatternNo;
     }
 
     public void setMinPatternNo(int i)
     {
         minPatternNo = i;
     }
 
     public int getMaxPatternNo()
     {
         return maxPatternNo;
     }
 
     public void setMaxPatternNo(int i)
     {
         maxPatternNo = i;
     }
 
     public int getMaxRunTime()
     {
         return maxRunTime;
     }
 
     public void setMaxRunTime(int i)
     {
         maxRunTime = i;
     }
 
     public String getSimilarityMatrix()
     {
         return similarityMatrix;
     }
 
     public void setSimilarityMatrix(String s)
     {
         similarityMatrix = s;
     }
 
     public double getSimilarityThreshold()
     {
         return similarityThreshold;
     }
 
     public void setSimilarityThreshold(double d)
     {
         similarityThreshold = d;
     }
 
     public String getInputName()
     {
         return inputName;
     }
 
     public void setInputName(String s)
     {
         inputName = s;
     }
 
     public String getOutputName()
     {
         return outputName;
     }
 
     public void setOutputName(String s)
     {
         outputName = s;
     }
 
 
 
 
 
 
     public synchronized boolean equals(Object obj)
     {
         if(!(obj instanceof Parameters))
             return false;
         Parameters parameters = (Parameters)obj;
         if(obj == null)
             return false;
         if(this == obj)
             return true;
         if(__equalsCalc != null)
         {
             return __equalsCalc == obj;
         } else
         {
             __equalsCalc = obj;
             boolean flag = minSupport == parameters.getMinSupport() && minTokens == parameters.getMinTokens() && window == parameters.getWindow() && minWTokens == parameters.getMinWTokens() && exactTokens == parameters.getExactTokens() && countSeq == parameters.getCountSeq() && exact == parameters.getExact() && printDetails == parameters.getPrintDetails() && groupingType == parameters.getGroupingType() && groupingN == parameters.getGroupingN() && sortMode == parameters.getSortMode() && outputMode == parameters.getOutputMode() && minPer100Support == parameters.getMinPer100Support() && computePValue == parameters.getComputePValue() && minPValue == parameters.getMinPValue() && threadNo == parameters.getThreadNo() && threadId == parameters.getThreadId() && minPatternNo == parameters.getMinPatternNo() && maxPatternNo == parameters.getMaxPatternNo() && maxRunTime == parameters.getMaxRunTime() && (similarityMatrix == null && parameters.getSimilarityMatrix() == null || similarityMatrix != null && similarityMatrix.equals(parameters.getSimilarityMatrix())) && similarityThreshold == parameters.getSimilarityThreshold() && (inputName == null && parameters.getInputName() == null || inputName != null && inputName.equals(parameters.getInputName())) && (outputName == null && parameters.getOutputName() == null || outputName != null && outputName.equals(parameters.getOutputName())) ;
             __equalsCalc = null;
             return flag;
         }
     }
 
 
 
     public synchronized int hashCode()
     {
         if(__hashCodeCalc)
             return 0;
         __hashCodeCalc = true;
         int i = 1;
         i += getMinSupport();
         i += getMinTokens();
         i += getWindow();
         i += getMinWTokens();
         i += getExactTokens();
         i += getCountSeq();
         i += getExact();
         i += getPrintDetails();
         i += getGroupingType();
         i += getGroupingN();
         i += getSortMode();
         i += getOutputMode();
         i += (new Double(getMinPer100Support())).hashCode();
         i += getComputePValue();
         i += (new Double(getMinPValue())).hashCode();
         i += getThreadNo();
         i += getThreadId();
         i += getMinPatternNo();
         i += getMaxPatternNo();
         i += getMaxRunTime();
         if(getSimilarityMatrix() != null)
             i += getSimilarityMatrix().hashCode();
         i += (new Double(getSimilarityThreshold())).hashCode();
         if(getInputName() != null)
             i += getInputName().hashCode();
         if(getOutputName() != null)
             i += getOutputName().hashCode();
 
         __hashCodeCalc = false;
         return i;
     }
 
 
 
 
     private int minSupport;
     private int minTokens;
     private int window;
     private int minWTokens;
     private int exactTokens;
     private int countSeq;
     private int exact;
     private int printDetails;
     private int groupingType;
     private int groupingN;
     private int sortMode;
     private int outputMode;
     private double minPer100Support;
     private int computePValue;
     private double minPValue;
     private int threadNo;
     private int threadId;
     private int minPatternNo;
     private int maxPatternNo;
     private int maxRunTime;
     private String similarityMatrix;
     private double similarityThreshold;
     private String inputName;
     private String outputName;
 
     private Object __equalsCalc;
     private boolean __hashCodeCalc;
  
 
 
 }
