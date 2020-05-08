 package co.joyatwork.pedometer;
 
 //TODO Threshold is probably not good name since it evaluates many characteristics of periodic curve!
 //TODO change to package private visibility!
 public class Threshold {
 	
 	private final int windowSize;
 
 	// these vars hold min/max values during measurement, 
 	// at the end of measuring window they are reset to sample value
 	private float measuredMinValue;
 	private float measuredMaxValue;
 
 	// these are auxiliary variables to provide correct min/max at the end of measuring window
 	// when the measured values are reset
 	private float currentMinValue;
 	private float currentMaxValue;
 	
 	private int sampleCount; // counts samples to control measuring window for threshold
 	private float minValue;  // min value at the end of window
 	private float maxValue;  // max value at the end of window
 	
 	private boolean isFirstSample; // controls initialization of the threshold measurement
 	private float firstSample;     // used to ignore min/max values if it was the value of 1st sample
 	private boolean isFirstWindow; // controls re-initialization after the 1st window 
 	
 
 	public Threshold(int numberOfSamples) {
 		this.windowSize = numberOfSamples;
 		this.measuredMinValue = 0;  
 		this.measuredMaxValue = 0;
 		this.currentMaxValue = 0;
 		this.currentMinValue = 0;
 		this.sampleCount = 0; 
 		this.minValue = 0;
 		this.maxValue = 0;
 		this.isFirstSample = true;
 		this.isFirstWindow = true;
 	}
 	
 	//TODO refactor this mess!
 	public void pushSample(float newSample) {
 		
 		if (isFirstSample) {
 			isFirstSample = false;
 			initializeMeasurement(newSample);
 			return;
 		}
 		if (newSample < measuredMinValue) {
 			measuredMinValue = newSample;
 		}
 		else if (newSample > measuredMaxValue) {
 			measuredMaxValue = newSample;
 		}
 		if (newSample < currentMinValue) {
 			currentMinValue = newSample;
 		}
 		else if (newSample > currentMaxValue) {
 			currentMaxValue = newSample;
 		}
 		sampleCount++;
 		if (sampleCount == windowSize) {
 			sampleCount = 0;
 			//TODO how to decouple it logically from the DC filter algorithm?  
 			// this is necessary due to use of high pass filter to eliminate DC offset from samples
 			// the 1st window yields incorrect min/max values because the output of DC filter is not steady 
 			if (isFirstWindow) {
 				isFirstWindow = false;
 				if (isFirstSampleEqualToMinOrMaxValue()) {
 					// if the 1st sample value is still the highest/lowest value at the end of window,
 					// keep original min/max values and start measurement with the current sample value
 					setMinMaxValues(newSample);
 					return;
 				}
 			}
 			// store measured values, they are valid for the duration of next window
 			minValue = measuredMinValue;
 			maxValue = measuredMaxValue;
 			// reset values for the comparison  in the next window
 			measuredMinValue = measuredMaxValue = newSample;
 		}
 	}
 
 	private void initializeMeasurement(float newSample) {
 		firstSample = newSample;
 		setMinMaxValues(firstSample);
 	}
 	
 	private void setMinMaxValues(float value) {
 		measuredMinValue = measuredMaxValue = value;
 		currentMaxValue = measuredMaxValue;
 		currentMinValue = measuredMinValue;
 	}
 
 	private boolean isFirstSampleEqualToMinOrMaxValue() {
 		return (firstSample == measuredMinValue) || (firstSample == measuredMaxValue);
 	}
 
 	/**
 	 * Returns actual min value measured in the running window
 	 */
 	public float getCurrentMinValue() {
 		return currentMinValue;
 	}
 	
 	/**
 	 * Returns actual max value measured in the running window
 	 */
 	public float getCurrentMaxValue() {
 		return currentMaxValue;
 	}
 	
 	/**
 	 * Returns fixed min value as measured in the previous window
 	 */
 	public float getFixedMinValue(){
 		return minValue;
 	}
 	
 	/**
 	 * Returns fixed max value as measured in the previous window
 	 */
 	public float getFixedMaxValue() {
 		return maxValue;
 	}
 	
 	public float getThresholdValue() {
 		return (minValue + maxValue)/2;
 	}
 	
 	public float getCurrentPeak2PeakValue() {
 		return (currentMaxValue - currentMinValue);
 	}
 
 	public float getFixedPeak2PeakValue() {
 		return (maxValue - minValue);
 	}
 
 	public void setCurrentMinMax(float value) {
 		currentMaxValue = currentMinValue = value;
 	}
 
 }
