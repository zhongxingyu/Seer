 package embedded2.ESecure;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import android.util.Log;
 
 public class Profile implements Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5720841490748053586L;
 	private ArrayList<Attempt> history;
 	private ArrayList<Double> durations;
 	private ArrayList<Double> pressures;
 	private ArrayList<Double> majors;
 	private ArrayList<Double> minors;
 	private ArrayList<Double> gaps;
 	private ArrayList<Double> dists;
 	boolean testDuration, testPressure, testMajor,testMinor,testGaps,testDist;
 	private int mode,count;
 	private float avgDuration;
 	private float avgPressure;
 	private float avgMajorAxis, avgMinorAxis; // major and minor axes of touch
 	private float avgGap,avgDist; 
 
 	private float stdDuration;
 	private float stdPressure;
 	private float stdMajorAxis, stdMinorAxis; // major and minor axes of touch
 	private float stdGap,stdDist; 
 
 	private float cvDuration;
 	private float cvPressure;
 	private float cvMajorAxis, cvMinorAxis;
 	private float cvOrientation;
 	private float cvGaps,cvDist;
 
 	private float weightDuration;
 	private float weightPressure;
 	private float weightMajorAxis, weightMinorAxis;
 	private float weightOrientation;
 	private float weightGaps,weightDist;
 
 	private float maxScore;
 
 	public Profile() {
 		history = new ArrayList<Attempt>();
 		durations = new ArrayList<Double>();
 		pressures = new ArrayList<Double>();
 		majors = new ArrayList<Double>();
 		minors = new ArrayList<Double>();
 		gaps = new ArrayList<Double>();
 		dists = new ArrayList<Double>();
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
 			dists.addAll(newAttempt.getDists());
 			this.updateStats();
 			return true;
 		}else if (newAttempt.getCode().toString().equals(history.get(count - 1).getCode().toString())) {
 			if (count < 10) {
 				if(count == 0){
 					mode = newAttempt.getMode();
 				}
 				if(newAttempt.getMode() == mode){
 					//				Log.v("CODE","new");
 					history.add(newAttempt);
 					durations.addAll(newAttempt.getDurations());
 					pressures.addAll(newAttempt.getPressures());
 					majors.addAll(newAttempt.getMajors());
 					minors.addAll(newAttempt.getMinors());
 					gaps.addAll(newAttempt.getGaps());
 					dists.addAll(newAttempt.getDists());
 					count++;
 					this.updateStats();
 					return true;
 				}else{
 					return false;
 				}
 			} else {
 
 				if(newAttempt.getMode() == mode){
 					//				Log.v("CODE","attempt");
 					float score = 0;
 
 
 
 					double [] sample1 = new double[durations.size()];
 					double [] sample2 = new double[newAttempt.getPoints().size()];
					double alpha = .2;
 					for(int i = 0; i < sample1.length; i++){
 						sample1[i] = durations.get(i).doubleValue();
 					}
 					for(int i = 0; i < sample2.length; i++){
 						sample2[i] = newAttempt.getDurations().get(i).doubleValue();
 					}
 					testDuration = TTest.tTest(sample1,sample2,alpha);
 					Log.v("DBUG","testDuration: "+testDuration);
 
 					for(int i = 0; i < sample1.length; i++){
 						sample1[i] = pressures.get(i).doubleValue();
 					}
 					for(int i = 0; i < sample2.length; i++){
 						sample2[i] = newAttempt.getPressures().get(i).doubleValue();
 					}
 					testPressure = TTest.tTest(sample1,sample2,alpha);
 
 					for(int i = 0; i < sample1.length; i++){
 						sample1[i] = majors.get(i).doubleValue();
 					}
 					for(int i = 0; i < sample2.length; i++){
 						sample2[i] = newAttempt.getMajors().get(i).doubleValue();
 					}
 					testMajor = TTest.tTest(sample1,sample2,alpha);
 
 					for(int i = 0; i < sample1.length; i++){
 						sample1[i] = minors.get(i).doubleValue();
 					}
 					for(int i = 0; i < sample2.length; i++){
 						sample2[i] = newAttempt.getMinors().get(i).doubleValue();
 					}
 					testMinor = TTest.tTest(sample1,sample2,alpha);
 					
 					for(int i = 0; i < sample1.length; i++){
 						sample1[i] = dists.get(i).doubleValue();
 					}
 					for(int i = 0; i < sample2.length; i++){
 						sample2[i] = newAttempt.getDists().get(i).doubleValue();
 					}
 					testDist = TTest.tTest(sample1,sample2,alpha);
					Log.v("DBUG","testDist: "+testDist);
 
 					sample1 = new double[gaps.size()];
 					sample2 = new double[newAttempt.getGaps().size()];
 					for(int i = 0; i < sample1.length; i++){
 						sample1[i] = gaps.get(i).doubleValue();
 					}
 					for(int i = 0; i < sample2.length; i++){
 						sample2[i] = newAttempt.getGaps().get(i).doubleValue();
 					}
 					testGaps = TTest.tTest(sample1,sample2,alpha);
 
 					//				Log.v("DBUG","weightDuration: "+weightDuration);
 					//				Log.v("DBUG","weights  : "+weightDuration+" "+weightPressure+" "+weightMajorAxis+" "+weightDuration+" "+weightMinorAxis+" "+weightGaps+" ");
 					score += weightDuration*(testDuration?0:1);
 					score += weightPressure*(testPressure?0:1);
 					score += weightMinorAxis*(testMajor?0:1);
 					score += weightMajorAxis*(testMajor?0:1);
 					score += weightGaps*(testGaps?0:1);
 					score += weightDist*(testDist?0:1);
 					Log.v("DBUG","score: "+score);
 					Log.v("DBUG","maxscore: "+maxScore);
					if(score >= 0.75*maxScore){
 						history.add(newAttempt);
 						durations.addAll(newAttempt.getDurations());
 						pressures.addAll(newAttempt.getPressures());
 						majors.addAll(newAttempt.getMajors());
 						minors.addAll(newAttempt.getMinors());
 						gaps.addAll(newAttempt.getGaps());
 						dists.addAll(newAttempt.getDists());
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
 		float sumDuration = 0, sumPressure = 0, sumMajorAxis = 0, sumMinorAxis = 0,sumGaps = 0,sumDist = 0;
 		int size = durations.size();
 		for (int i = 0; i < size; i++) {
 			sumDuration += durations.get(i);
 			sumPressure += pressures.get(i);
 			sumMajorAxis += majors.get(i);
 			sumMinorAxis += minors.get(i);
 			sumDist += dists.get(i);
 		}
 
 		for(int i = 0; i < gaps.size(); i++){
 			sumGaps += gaps.get(i);
 		}
 		avgDuration = sumDuration / size;
 		avgPressure = sumPressure / size;
 		avgMajorAxis = sumMajorAxis / size;
 		avgMinorAxis = sumMinorAxis / size;
 		avgDist = sumDist/size;
 		avgGap = sumGaps/gaps.size();
 		sumDuration = sumPressure = sumMajorAxis = sumMinorAxis = sumDist = 0.0f;
 		for (int i = 0; i < size; i++) {
 			sumDuration += (durations.get(i) - avgDuration)
 					* (durations.get(i) - avgDuration);
 			sumPressure += (pressures.get(i) - avgPressure)
 					* (pressures.get(i) - avgPressure);
 			sumMajorAxis += (majors.get(i) - avgMajorAxis)
 					* (majors.get(i) - avgMajorAxis);
 			sumMinorAxis += (minors.get(i) - avgMinorAxis)
 					* (minors.get(i) - avgMinorAxis);
 			sumDist += (dists.get(i) - avgDist)*(dists.get(i) - avgDist);
 		}
 
 		for(int i = 0; i < gaps.size(); i++){
 			sumGaps += (gaps.get(i) - avgGap)*(gaps.get(i) - avgGap);
 		}
 
 		stdDuration = (float) Math.sqrt(sumDuration / size);
 		stdPressure = (float) Math.sqrt(sumPressure / size);
 		stdMajorAxis = (float) Math.sqrt(sumMajorAxis / size);
 		stdMinorAxis = (float) Math.sqrt(sumMinorAxis / size);
 		stdDist = (float)Math.sqrt(sumDist/size);
 		stdGap = (float)Math.sqrt(sumGaps/gaps.size());
 
 
 		cvDuration = stdDuration/avgDuration;
 		cvPressure = stdPressure/avgPressure;
 		cvMajorAxis = stdMajorAxis/avgMajorAxis;
 		cvMinorAxis = stdMinorAxis/avgMinorAxis;
 		cvDist = stdDist/avgDist;
 		cvGaps = stdGap/avgGap;
		float sumCv = cvDuration + cvPressure + cvMajorAxis + cvMinorAxis + cvGaps + cvDist;
 		//		Log.v("DBUG","cvDuration: "+cvDuration);
 		//		Log.v("DBUG","cvDuration + cvPressure + cvMajorAxis + cvMinorAxis + cvGaps");
 		//		Log.v("DBUG",""+cvDuration +" "+ cvPressure+" " + cvMajorAxis +" "+ cvMinorAxis+" "+ cvGaps);
 		weightDuration = cvDuration/sumCv;
 		weightPressure = cvPressure/sumCv;
 		weightMajorAxis = cvMajorAxis/sumCv;
 		weightMinorAxis = cvMinorAxis/sumCv;
 		weightDist = cvDist/sumCv;
 		weightGaps = cvGaps/sumCv;
 		//		Log.v("DBUG", "sumCv: "+sumCv);
 		weightDuration = 1.0f/weightDuration;
 		weightPressure = 1.0f/weightPressure;
 		weightMajorAxis = 1.0f/weightMajorAxis;
 		weightMinorAxis = 1.0f/weightMinorAxis;
 		weightOrientation = 1.0f/weightOrientation;
 		weightDist = 1.0f/weightDist;
 		weightGaps = 1.0f/weightGaps;
 
 		if(stdDuration==0){
 			weightDuration=0;
 		}
 		if(stdPressure==0){
 			weightPressure=0;
 		}
 		if(stdMajorAxis==0){
 			weightMajorAxis=0;
 		}
 		if(stdMinorAxis==0){
 			weightMinorAxis=0;
 		}
 		if(stdDist==0){
 			weightDist=0;
 		}
 		if(stdGap==0){
 			weightGaps=0;
 		}
 
 		maxScore = weightDuration + weightPressure + weightMajorAxis + 
 				weightMinorAxis +weightGaps + weightDist;
 		Log.v("DBUG","MAX SCORE: "+maxScore);
 
 
 	}
 }
