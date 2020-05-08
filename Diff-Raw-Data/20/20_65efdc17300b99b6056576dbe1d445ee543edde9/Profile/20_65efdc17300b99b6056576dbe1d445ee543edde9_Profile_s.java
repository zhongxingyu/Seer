 package embedded2.ESecure;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import android.util.Log;
 
 public class Profile implements Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5720841490748053586L;
 	TTest tester;
 	private ArrayList<Attempt> history;
 	private ArrayList<Double> durations;
 	private ArrayList<Double> pressures;
 	private ArrayList<Double> majors;
 	private ArrayList<Double> minors;
 	private ArrayList<Double> gaps;
 	boolean testDuration, testPressure, testMajor,testMinor,testGaps;
 	private int count;
 	private float avgDuration;
 	private float avgPressure;
 	private float avgMajorAxis, avgMinorAxis; // major and minor axes of touch
 	private float avgGap; 
 	
 	private float stdDuration;
 	private float stdPressure;
 	private float stdMajorAxis, stdMinorAxis; // major and minor axes of touch
 	private float stdGap; 
 	
 	private float cvDuration;
 	private float cvPressure;
 	private float cvMajorAxis, cvMinorAxis;
 	private float cvOrientation;
 	private float cvGaps;
 	
 	private float weightDuration;
 	private float weightPressure;
 	private float weightMajorAxis, weightMinorAxis;
 	private float weightOrientation;
 	private float weightGaps;
 	
 	private float maxScore;
 
 	public Profile() {
 		history = new ArrayList<Attempt>();
 		durations = new ArrayList<Double>();
 		pressures = new ArrayList<Double>();
 		majors = new ArrayList<Double>();
 		minors = new ArrayList<Double>();
 		gaps = new ArrayList<Double>();
 		tester = new TTest();
 		//history.add(first);
 		count = 0;
 	}
 
 	public boolean add(Attempt newAttempt) {
 	if(count == 0){
 		history.add(newAttempt);
 		count++;
 		durations.addAll(newAttempt.getDurations());
 		pressures.addAll(newAttempt.getPressures());
 		majors.addAll(newAttempt.getMajors());
 		minors.addAll(newAttempt.getMinors());
 		gaps.addAll(newAttempt.getGaps());
 		this.updateStats();
 		return true;
 	}else if (newAttempt.getCode().toString().equals(history.get(count - 1).getCode().toString())) {
 			if (count < 10) {
 				Log.v("CODE","new");
 				history.add(newAttempt);
 				durations.addAll(newAttempt.getDurations());
 				pressures.addAll(newAttempt.getPressures());
 				majors.addAll(newAttempt.getMajors());
 				minors.addAll(newAttempt.getMinors());
 				gaps.addAll(newAttempt.getGaps());
 				count++;
 				this.updateStats();
 				return true;
 			} else {
 				Log.v("CODE","attempt");
 				float score = 0;
 				
 				
 				
 				double [] sample1 = new double[durations.size()];
 				double [] sample2 = new double[newAttempt.getPoints().size()];
 				double alpha = .1;
 				for(int i = 0; i < sample1.length; i++){
 					sample1[i] = durations.get(i).doubleValue();
 				}
 				for(int i = 0; i < sample2.length; i++){
 					sample2[i] = newAttempt.getDurations().get(i).doubleValue();
 				}
 				testDuration = tester.tTest(sample1,sample2,alpha);
 				Log.v("DBUG","testDuration: "+testDuration);
 				
 				for(int i = 0; i < sample1.length; i++){
 					sample1[i] = pressures.get(i).doubleValue();
 				}
 				for(int i = 0; i < sample2.length; i++){
 					sample2[i] = newAttempt.getPressures().get(i).doubleValue();
 				}
 				testPressure = tester.tTest(sample1,sample2,alpha);
 				
 				for(int i = 0; i < sample1.length; i++){
 					sample1[i] = majors.get(i).doubleValue();
 				}
 				for(int i = 0; i < sample2.length; i++){
 					sample2[i] = newAttempt.getMajors().get(i).doubleValue();
 				}
 				testMajor = tester.tTest(sample1,sample2,alpha);
 				
 				for(int i = 0; i < sample1.length; i++){
 					sample1[i] = minors.get(i).doubleValue();
 				}
 				for(int i = 0; i < sample2.length; i++){
 					sample2[i] = newAttempt.getMinors().get(i).doubleValue();
 				}
 				testMinor = tester.tTest(sample1,sample2,alpha);
 				
 				sample1 = new double[gaps.size()];
 				sample2 = new double[newAttempt.getGaps().size()];
 				for(int i = 0; i < sample1.length; i++){
 					sample1[i] = gaps.get(i).doubleValue();
 				}
 				for(int i = 0; i < sample2.length; i++){
 					sample2[i] = newAttempt.getGaps().get(i).doubleValue();
 				}
 				testGaps = tester.tTest(sample1,sample2,alpha);
 				
 				Log.v("DBUG","weightDuration: "+weightDuration);
 				score += weightDuration*(testDuration?0:1);
 				score += weightPressure*(testPressure?0:1);
 				score += weightMajorAxis*(testMajor?0:1);
 				score += weightMinorAxis*(testMinor?0:1);
 				score += weightGaps*(testGaps?0:1);
 				Log.v("DBUG","score: "+score);
 				if(score > 0.75*maxScore){
 					history.add(newAttempt);
 					durations.addAll(newAttempt.getDurations());
 					pressures.addAll(newAttempt.getPressures());
 					majors.addAll(newAttempt.getMajors());
 					minors.addAll(newAttempt.getMinors());
 					gaps.addAll(newAttempt.getGaps());
 					int size = history.get(0).getPoints().size();
 					//Log.v("SIZES", "total: " + durations.size() + "attempt: " + history.get(0).getPoints().size());
 					for(int i = 0; i < size; i++){
 						durations.remove(0);
 						pressures.remove(0);
 						majors.remove(0);
 						minors.remove(0);
 						if(i == size - 1) continue;
 						gaps.remove(0);
 					}
 					history.remove(0);
 					
 					updateStats();
 					return true;
 				}else{
 					return false;
 				}
 			}
 	} else {
 		Log.v("CODE","mismatch");
 		return false;
 	}
 	}
 
 	private void updateStats(){
 		float sumDuration = 0, sumPressure = 0, sumMajorAxis = 0, sumMinorAxis = 0,sumGaps = 0;
 		int size = durations.size();
 		for (int i = 0; i < size; i++) {
 			sumDuration += durations.get(i);
 			sumPressure += pressures.get(i);
 			sumMajorAxis += majors.get(i);
 			sumMinorAxis += minors.get(i);
 		}
 		
 		for(int i = 0; i < gaps.size(); i++){
 			sumGaps += gaps.get(i);
 		}
 		avgDuration = sumDuration / size;
 		avgPressure = sumPressure / size;
 		avgMajorAxis = sumMajorAxis / size;
 		avgMinorAxis = sumMinorAxis / size;
 		avgGap = sumGaps/gaps.size();
 		sumDuration = sumPressure = sumMajorAxis = sumMinorAxis = 0.0f;
 		for (int i = 0; i < size; i++) {
 			sumDuration += (durations.get(i) - avgDuration)
 					* (durations.get(i) - avgDuration);
 			sumPressure += (pressures.get(i) - avgPressure)
 					* (pressures.get(i) - avgPressure);
 			sumMajorAxis += (majors.get(i) - avgMajorAxis)
 					* (majors.get(i) - avgMajorAxis);
 			sumMinorAxis += (minors.get(i) - avgMinorAxis)
 					* (minors.get(i) - avgMinorAxis);
 		}
 
 		for(int i = 0; i < gaps.size(); i++){
 			sumGaps += (gaps.get(i) - avgGap)*(gaps.get(i) - avgGap);
 		}
 
 		stdDuration = (float) Math.sqrt(sumDuration / size);
 		stdPressure = (float) Math.sqrt(sumPressure / size);
 		stdMajorAxis = (float) Math.sqrt(sumMajorAxis / size);
 		stdMinorAxis = (float) Math.sqrt(sumMinorAxis / size);
 		stdGap = (float)Math.sqrt(sumGaps/gaps.size());
 		
 		
 		cvDuration = stdDuration/avgDuration;
 		cvPressure = stdPressure/avgPressure;
 		cvMajorAxis = stdMajorAxis/avgMajorAxis;
 		cvMinorAxis = stdMinorAxis/avgMinorAxis;
 		cvGaps = stdGap/avgGap;
 		float sumCv = cvDuration + cvPressure + cvMajorAxis + cvMinorAxis + cvGaps;
 		Log.v("DBUG","cvDuration: "+cvDuration);
 		Log.v("DBUG","cvDuration + cvPressure + cvMajorAxis + cvMinorAxis + cvGaps");
 		Log.v("DBUG",""+cvDuration +" "+ cvPressure+" " + cvMajorAxis +" "+ cvMinorAxis+" "+ cvGaps);
 		weightDuration = cvDuration/sumCv;
 		weightPressure = cvPressure/sumCv;
 		weightMajorAxis = cvMajorAxis/sumCv;
 		weightMinorAxis = cvMinorAxis/sumCv;
 		weightGaps = cvGaps/sumCv;
 		Log.v("DBUG", "sumCv: "+sumCv);
 		weightDuration = 1.0f/weightDuration;
 		weightPressure = 1.0f/weightPressure;
 		weightMajorAxis = 1.0f/weightMajorAxis;
 		weightMinorAxis = 1.0f/weightMinorAxis;
 		weightOrientation = 1.0f/weightOrientation;
 		weightGaps = 1.0f/weightGaps;
 		
 		maxScore = weightDuration + weightPressure + weightMajorAxis + 
 				weightMinorAxis +weightGaps;
 		
 		
 	}
 }
