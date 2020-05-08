 package com.aeidesign.statscalc.stats;
 
 public class Regression {
	private Descriptive x;
	private Descriptive y;
 	
 	public Regression(){
 		x = new Descriptive();
 		y = new Descriptive();
 	}
 	
 	public void addXValue(double value){
 		x.addValue(value);
 	}
 	
 	public void addYValue(double value){
 		y.addValue(value);
 	}
 	
 	public void clearX(){
 		x.clear();
 	}
 	
 	public void clearY(){
 		y.clear();
 	}
 	
 	public int getNumDataPoints(){
 		return x.getSampleSize();
 	}
 	
 	public double getSlope(){
 		double meanX = x.getMean();
 		double meanY = y.getMean();
 		double sumXY = 0;
 		double sumXSqr = 0;
 		
 		int xSampleSize = x.getSampleSize();
 		
 		for(int i = 0; i < xSampleSize; i++){
 			sumXY += (x.getValue(i) - meanX) * (y.getValue(i) - meanY);
 			sumXSqr += Math.pow(x.getValue(i) - meanX, 2);
 		}
 		
 		return sumXY/sumXSqr;
 	}
 	
 	public double getIntercept(){
 		return y.getMean() - (this.getSlope() * x.getMean());
 	}
 	
 	public String getEquation(){
 		return "y = " + String.valueOf(Functions.format(this.getSlope())) + "x + " + String.valueOf(Functions.format(this.getIntercept()));
 	}
 	
 	public double getErrorSquared(){
 		double slope = this.getSlope();
 		double intercept = this.getIntercept();
 		double[] predicted = new double[x.getSampleSize()];
 		double sum = 0;
 		
 		int predictedLength = predicted.length;
 		
 		for (int i = 0; i < predictedLength; i++){
 			predicted[i] = slope * x.getValue(i) + intercept;
 			sum += Math.pow(predicted[i] - y.getValue(i), 2);
 		}
 		return sum;
 		
 	}
 	
 	public double getRSquared(){
 		double meanY = y.getMean();
 		double sum = 0;
 		
 		int ySampleSize = y.getSampleSize();
 		
 		for (int i = 0; i < ySampleSize; i++){
 			sum += Math.pow(y.getValue(i) - meanY, 2);
 		}
 		return 1 - (this.getErrorSquared() / sum);
 	}
 	
 	public double getR(){
		double meanX = x.getMean();
		double meanY = y.getMean();
		double sum = 0;
		
		int sampleSize = y.getSampleSize();
		
		for (int i = 0; i < sampleSize; i++){
			sum += (x.getValue(i) - meanX) * (y.getValue(i) - meanY);
		}
		
		return sum / ((sampleSize) * y.getStandardDeviation() * x.getStandardDeviation());
 	}
 	
 	public double getMeanSquareError(){
 		return Math.pow(y.getStandardError(), 2);
 	}
 	
 	public double getSlopeError(){
 		double sumXX = 0;
 		double meanX = x.getMean();
 		
 		int xSampleSize = x.getSampleSize();
 		
 		for (int i = 0; i < xSampleSize; i++){
 			sumXX += Math.pow(x.getValue(i) - meanX, 2);
 		}
 		return Math.sqrt(this.getMeanSquareError() / sumXX);
 	}
 	
 	public double getSignificance(){
 		return 0;
 	}
 
 }
