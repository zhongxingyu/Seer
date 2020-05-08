 package edu.ucla.nesl.flowengine.node.classifier;
 
 import edu.ucla.nesl.flowengine.DataType;
 import edu.ucla.nesl.flowengine.DebugHelper;
 import edu.ucla.nesl.flowengine.InvalidDataReporter;
 import edu.ucla.nesl.flowengine.SensorType;
 import edu.ucla.nesl.flowengine.node.DataFlowNode;
 
 public class Stress extends DataFlowNode {
 	private static final String TAG = Stress.class.getSimpleName();
 	
 	private long mCurTime = 0;
 	private long mLastTime = 0;
 	
 	private static final double maxFValue[] = { 4.7368    ,4.3080    ,5.3359    ,5.4816    ,5.9184    ,5.1250    ,4.0462    ,3.9805    ,4.3502    ,2.2025    ,2.6288    ,5.0921,   2.7805};
 	private static final double minFValue[] = {-1.8694   ,-3.1003   ,-1.4935   ,-1.6805   ,-1.7398   ,-2.0703   ,-1.1872   ,-1.3573   ,-1.7656   ,-3.8432   ,-4.0433   ,-1.9772,  -4.1381};
 	private static final double coeff[] = {2.3156,-2.7972,0.8428,0.6391,-0.7193,1.294,0.0966,0.2632,2.2966, -2.0124, -1.4619,-1.0124,-2.31,2.6698};
 
 	private static final int NUM_FEATURES = 13;
 	private double[] mFeatures = new double[NUM_FEATURES];
 	private int mFeatureBitVector = 0;
 	
 	private static final int INDEX_VENTILATION = 0;
 	private static final int INDEX_MEAN_INHALATION = 1;
 	private static final int INDEX_QD_EXHALATION = 2;
 	private static final int INDEX_QD_RESPIRATION = 3;
 	private static final int INDEX_MEDIAN_IERATIO = 4;
 	private static final int INDEX_MEDIAN_STRETCH = 5;
 	private static final int INDEX_QD_STRETCH = 6;
 	private static final int INDEX_PERCENTILE80_STRETCH = 7;
 	private static final int INDEX_POWER12_LOMB_RR = 8;
 	private static final int INDEX_MEAN_RR = 9;
 	private static final int INDEX_MEDIAN_RR = 10;
 	private static final int INDEX_QD_RR = 11;
 	private static final int INDEX_PERCENTILE80_RR = 12;
 	
 	private static final int BIT_VENTILATION = 1 << INDEX_VENTILATION;
 	private static final int BIT_MEAN_INHALATION = 1 << INDEX_MEAN_INHALATION;
 	private static final int BIT_QD_EXHALATION = 1 << INDEX_QD_EXHALATION;
 	private static final int BIT_QD_RESPIRATION = 1 << INDEX_QD_RESPIRATION;
 	private static final int BIT_MEDIAN_IERATIO = 1 << INDEX_MEDIAN_IERATIO;
 	private static final int BIT_MEDIAN_STRETCH = 1 << INDEX_MEDIAN_STRETCH;
 	private static final int BIT_QD_STRETCH = 1 << INDEX_QD_STRETCH;
 	private static final int BIT_PERCENTILE80_STRETCH = 1 << INDEX_PERCENTILE80_STRETCH;
 	private static final int BIT_POWER12_LOMB_RR = 1 << INDEX_POWER12_LOMB_RR;
 	private static final int BIT_MEAN_RR = 1 << INDEX_MEAN_RR;
 	private static final int BIT_MEDIAN_RR = 1 << INDEX_MEDIAN_RR;
 	private static final int BIT_QD_RR = 1 << INDEX_QD_RR;
 	private static final int BIT_PERCENTILE80_RR = 1 << INDEX_PERCENTILE80_RR;
 
 	private void clearFeatureBitVector() {
 		mFeatureBitVector = 0;
 	}
 	
 	private boolean isAllFeature() {
 		return mFeatureBitVector == 0x1FFF; 
 	}
 	
 	private String getMissingFeatures() {
 		String str = "";
 		if ((mFeatureBitVector & BIT_VENTILATION) == 0) {
 			str += "Ventilation, ";
 		}
 		if ((mFeatureBitVector & BIT_MEAN_INHALATION) == 0) {
 			str += "InhalationMean, ";
 		}
 		if ((mFeatureBitVector & BIT_QD_EXHALATION) == 0) {
 			str += "ExhalationQuartileDeviation, ";
 		}
 		if ((mFeatureBitVector & BIT_QD_RESPIRATION) == 0) {
 			str += "RespirationQuartileDeviation, ";
 		}
 		if ((mFeatureBitVector & BIT_MEDIAN_IERATIO) == 0) {
 			str += "IERatioMedian, ";
 		}
 		if ((mFeatureBitVector & BIT_MEDIAN_STRETCH) == 0) {
 			str += "StretchMedian, ";
 		}
 		if ((mFeatureBitVector & BIT_QD_STRETCH) == 0) {
 			str += "StretchQuartileDeviation, ";
 		}
 		if ((mFeatureBitVector & BIT_PERCENTILE80_STRETCH) == 0) {
 			str += "StretchPercentile80.0, ";
 		}
 		if ((mFeatureBitVector & BIT_POWER12_LOMB_RR) == 0) {
 			str += "LombPeriodogramBandPower0.1-0.2, ";
 		}
 		if ((mFeatureBitVector & BIT_MEAN_RR) == 0) {
 			str += "RRIntervalMean, ";
 		}
 		if ((mFeatureBitVector & BIT_MEDIAN_RR) == 0) {
 			str += "RRIntervalMedian, ";
 		}
 		if ((mFeatureBitVector & BIT_QD_RR) == 0) {
 			str += "RRIntervalQuartileDeviation, ";
 		}
 		if ((mFeatureBitVector & BIT_PERCENTILE80_RR) == 0) {
 			str += "RRIntervalPercentile80.0, ";
 		}
 		return str;
 	}
 	
 	private String getClassString(boolean isStress) {
 		if (isStress) {
 			return "Stress";
 		}
 		return "No stress";
 	}
 	
 	@Override
 	protected void processInput(String name, String type, Object inputData, int length, long timestamp) {
 		if (!type.equals(DataType.DOUBLE)) {
 			throw new UnsupportedOperationException("Unsupported type: " + type);
 		}
 		
 		mCurTime = System.currentTimeMillis();
 		double diff = mCurTime - mLastTime;
 		if (mFeatureBitVector != 0 && diff >= 10000) {
 			InvalidDataReporter.report("Too large time difference among features: " + diff);
 			clearFeatureBitVector();
 		}
 		mLastTime = mCurTime;
 
 		if (name.contains("RIPVentilation")) {
 			mFeatures[INDEX_VENTILATION] = (Double)inputData;
 			mFeatureBitVector |= BIT_VENTILATION;
 		} else if (name.contains("RIPInhalationMean")) {
 			mFeatures[INDEX_MEAN_INHALATION] = (Double)inputData;
 			mFeatureBitVector |= BIT_MEAN_INHALATION;
 		} else if (name.contains("RIPExhalationQuartileDeviation")) {
 			mFeatures[INDEX_QD_EXHALATION] = (Double)inputData;
 			mFeatureBitVector |= BIT_QD_EXHALATION;
 		} else if (name.contains("RIPRespirationQuartileDeviation")) {
 			mFeatures[INDEX_QD_RESPIRATION] = (Double)inputData;
 			mFeatureBitVector |= BIT_QD_RESPIRATION;
 		} else if (name.contains("RIPIERatioMedian")) {
 			mFeatures[INDEX_MEDIAN_IERATIO] = (Double)inputData;
 			mFeatureBitVector |= BIT_MEDIAN_IERATIO;
 		} else if (name.contains("RIPStretchMedian")) {
 			mFeatures[INDEX_MEDIAN_STRETCH] = (Double)inputData;
 			mFeatureBitVector |= BIT_MEDIAN_STRETCH;
 		} else if (name.contains("RIPStretchQuartileDeviation")) {
 			mFeatures[INDEX_QD_STRETCH] = (Double)inputData;
 			mFeatureBitVector |= BIT_QD_STRETCH;
 		} else if (name.contains("RIPStretchPercentile80.0")) {
 			mFeatures[INDEX_PERCENTILE80_STRETCH] = (Double)inputData;
 			mFeatureBitVector |= BIT_PERCENTILE80_STRETCH;
 		} else if (name.contains("ECGRRIntervalLombPeriodogramBandPower0.1-0.2")) {
 			mFeatures[INDEX_POWER12_LOMB_RR] = (Double)inputData;
 			mFeatureBitVector |= BIT_POWER12_LOMB_RR;
 		} else if (name.contains("ECGRRIntervalMean")) {
 			mFeatures[INDEX_MEAN_RR] = (Double)inputData;
 			mFeatureBitVector |= BIT_MEAN_RR;
 		} else if (name.contains("ECGRRIntervalMedian")) {
 			mFeatures[INDEX_MEDIAN_RR] = (Double)inputData;
 			mFeatureBitVector |= BIT_MEDIAN_RR;
 		} else if (name.contains("ECGRRIntervalQuartileDeviation")) {
 			mFeatures[INDEX_QD_RR] = (Double)inputData;
 			mFeatureBitVector |= BIT_QD_RR;
 		} else if (name.contains("ECGRRIntervalPercentile80.0")) {
 			mFeatures[INDEX_PERCENTILE80_RR] = (Double)inputData;
 			mFeatureBitVector |= BIT_PERCENTILE80_RR;
 		}
 		
 		DebugHelper.log(TAG, name);
 		DebugHelper.log(TAG, "bit vector: " + Integer.toHexString(mFeatureBitVector));
 		DebugHelper.dump(TAG, mFeatures);
 		String missingFeatures = getMissingFeatures();
 		DebugHelper.log(TAG, "missing features: " + missingFeatures);
 		
 		if (isAllFeature()) {
 			boolean isStress = getStressPredictionSVM(mFeatures);
 			DebugHelper.log(TAG, "isStress: " + isStress);
 			output(SensorType.STRESS_CONTEXT_NAME, DataType.INTEGER, isStress ? SensorType.STRESS : SensorType.NO_STRESS, 0, timestamp);
 			clearFeatureBitVector();
 			
 			synchronized (DebugHelper.lock){
 				if (DebugHelper.isMethodTrace || DebugHelper.isAllocCounting) {
 					DebugHelper.stressCount++;
 					DebugHelper.forcelog(TAG, "isStress:" + isStress + " count:" + DebugHelper.stressCount);
 					if (DebugHelper.conversationCount >= DebugHelper.numCount
 							&& DebugHelper.stressCount >= DebugHelper.numCount) {
 						DebugHelper.stopTrace();
 					}
 				}
 			}
 		}
 	}
 
 	public boolean getStressPredictionSVM(double features[])
 	{
		int normNo = 0;
 		double meanFeature[] = new double[13];
 		double stdFeature[] = new double[13];
 		double v=0;
 		int i;
 		normNo++;
 		for(i=0;i<13;i++)
 		{
 			if(normNo==1){
 				meanFeature[i]=features[i];
 				stdFeature[i]=0;
 				features[i]=0;
 
 			}
 			else {
 				meanFeature[i]=(meanFeature[i]*(normNo-1)+features[i])/normNo;
 				stdFeature[i]=stdFeature[i]+(meanFeature[i]-features[i])*(meanFeature[i]-features[i])*normNo/(normNo-1);
 
 				features[i]=(features[i]-meanFeature[i])/Math.sqrt(stdFeature[i]/(normNo-1));
 			}
 		}
 		for (i=0;i<13;i++){
 			//if(features[i]>maxFValue[i]) maxFValue[i]=features[i];
 			//if(features[i]<minFValue[i]) minFValue[i]=features[i];
 			v = v + coeff[i] * (features[i] - minFValue[i]) / (maxFValue[i] - minFValue[i]);
 		}
 		v += coeff[13];
 
 		if (v > 0)
 			return true;
 		else 
 			return false;
 	}
 }
