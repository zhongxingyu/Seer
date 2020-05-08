 import java.text.DecimalFormat;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.lang.Integer;
 /**
  *  Take the trace file name and interested ground truth as input and generate feature vectors. 
  **/
 
 class extractAccelFeatures {
 
 	public static final int TIMEWINDOW_INTERVAL = 5 * 1000; //in ms, it decides the window size
	public static final int TIMEWINDOW_SIZE = 150;
 	public static FileWriter fvStream;
 	public static BufferedWriter fvOut;
 
 	public static void main(String args[]){
 
 		try{
 							if ( args.length < 2 ) {
 								System.out.println("Usage : java extractAccelTrain trace-file ground-truth");
 								System.exit(-1);
 							}
 							fvStream = new FileWriter("Accel.out");
 							fvOut = new BufferedWriter(fvStream);
 
 							String inFileName=args[0];
 							System.out.println("in file is "+inFileName);
 							int groundTruth=Integer.parseInt(args[1]);
 
 							FileInputStream fstream = new FileInputStream(inFileName);
 							DataInputStream in = new DataInputStream(fstream);
 							BufferedReader br = new BufferedReader (new InputStreamReader(in));
 							String strLine;
 
 							int prevGroundTruth = -1;
 							double lastTime = 0;
 							int counter = 0;
 
 							AccelElement accelList[] = new AccelElement[1000000];
 							while((strLine = br.readLine())!= null){
 								String header[] = strLine.split(",");
                                                                 String payload[] =null;
                                                                 try {
 								  payload = header[3].split("\\|");
                                                                 }
                                                                 catch (Exception e) {
                                                                    System.err.println("Out of bounds, continue .. \n");
                                                                    continue;
                                                                 } 
 
 								if(prevGroundTruth != groundTruth && Integer.parseInt(payload[3]) == groundTruth){ // transition detected
 									counter = 0;
 									long timeStamp = Long.parseLong(header[1].substring(0,13));//in ms
 									accelList[counter++] = new AccelElement(timeStamp, Double.parseDouble(payload[0])
 											, Double.parseDouble(payload[1]), Double.parseDouble(payload[2]));
 
 									// Read all the scans with the correct gt
 									while((strLine = br.readLine())!= null){
 										String innerHeader[] = strLine.split(",");
 										String innerPayload[] = innerHeader[3].split("\\|");
 
                                                                                 try {
 									        	if(Integer.parseInt(innerPayload[3]) == groundTruth){
 									        		timeStamp = Long.parseLong(innerHeader[1].substring(0,13));
 									        		accelList[counter++] = new AccelElement(timeStamp, Double.parseDouble(innerPayload[0]), Double.parseDouble(innerPayload[1]), Double.parseDouble(innerPayload[2]));
 
 									        	}else{// stop, we found a trace with the labelled groundtruth, extract feature
 									        		processAccelList(accelList, counter, groundTruth); 
 									        		initAccelList(accelList); // clean the array to find the next trace
 									        		prevGroundTruth = Integer.parseInt(innerPayload[3]);
 									        		break;
 									        	}
                                                                                 }
                                                                                 catch (Exception e) {
                                                                                     System.err.println("Some exception, continuing to next line \n"); 
                                                                                     continue;
                                                                                 }
 									}
 									if(strLine == null){ // found a trace
 										processAccelList(accelList, counter, groundTruth);	
 									}
 
 								}else{
 									prevGroundTruth = Integer.parseInt(payload[3]);
 								}
 
 							}//end of while
 		in.close();
 		fvOut.close();
 		}catch(Exception e)
 		{
                         e.printStackTrace();
 			System.err.println("ERROR: "+ e.getMessage() + " "+ e);
 		}
 	}
 
 	public static void processAccelList(AccelElement[] a, int totalAccelElementNum, int groundTruth){
 		
 		int firstRawDataInCurrentWindow = 0;
 		for(int i = 1; i < totalAccelElementNum; ++i){ // loop through the entire trace, totalAccelElementNum: number of samples in the trace 
 
 			if(a[i].timeStamp - a[firstRawDataInCurrentWindow].timeStamp > TIMEWINDOW_INTERVAL){ // a timewindow
			//if(i - firstRawDataInCurrentWindow > TIMEWINDOW_SIZE){
 				String outFeature = "";
 			
 			        // process the time window, start time: a[firstRawDataInCurrentWindow].timeStamp, end time: a[i-1].timeStamp	
 				int N = i-firstRawDataInCurrentWindow; //number of samples in the window
 				AccelElement accelWindow[] = new AccelElement[N]; 		
 
 				int counter = 0;
 				for(int j = firstRawDataInCurrentWindow; j < i ; ++j){
 					accelWindow[counter++] = a[j];
 				}
 				
 				
 				/** Calculating the features from the current window - accelWindow**/
 				
 				//**** Feature 1: Mean (time domain) ****//
 				//**** Feature 2: Variance (time domain) ****//
 				double currentWindowVar  = 0, currentWindowMean = 0;
 				for(int k = 0; k < N; ++k){
 					currentWindowMean += accelWindow[k].magnitude;
 				}
 				currentWindowMean /= (N * 1.0);
 				for(int k = 0; k < N; ++k){
 					currentWindowVar += (accelWindow[k].magnitude - currentWindowMean) * (accelWindow[k].magnitude - currentWindowMean); 
 				}  
 				currentWindowVar /= (double)(N-1);
 				
 
 				//**** Feature 3: Peak Power Frequency (freq domain) ****//
                                 double currentWindowFs=0;
                                 try {
                                   currentWindowFs = N/ ((accelWindow[N-1].timeStamp - accelWindow[0].timeStamp)/1000); // sampling frequency
                                 } 
                                 catch (Exception e) {
                                    System.err.println(e.getMessage());
                                    System.err.println("Ignoring sample due to divide by zero ... \n");
                                    continue;
                                 }
 				double currentWindowFFT[] = new double[((N/2) + 1)];   
 				computeDFT(accelWindow, N, currentWindowFFT);
 				
 				// find peak power location
 				double peakPower = -Double.MAX_VALUE;
 				int peakPowerLocation = -1;
 				for(int k = 1; k < currentWindowFFT.length; ++k){ // ignore index 0 (0Hz)
					if(currentWindowFFT[k] * currentWindowFFT[k] > peakPower){
						peakPower = currentWindowFFT[k] * currentWindowFFT[k];
 						peakPowerLocation = k;
 					}
 				}
 				double currentWindowPeakFreq = ((peakPowerLocation + 1)/(N* 1.0)) * currentWindowFs;
 				
 				//**** Feature 8: Strength Variation (time domain)****//
 				List<Double> summit = new ArrayList<Double>();
 				List<Double> valley = new ArrayList<Double>();
 				for(int k = 1; k < N-1; ++k){
 					if(accelWindow[k].magnitude > accelWindow[k-1].magnitude && accelWindow[k].magnitude > accelWindow[k+1].magnitude){
 						summit.add(accelWindow[k].magnitude);
 
 					}else if(accelWindow[k].magnitude < accelWindow[k-1].magnitude && accelWindow[k].magnitude < accelWindow[k+1].magnitude)
 					{
 						valley.add(accelWindow[k].magnitude);
 					}
 				}
 				double summitVar = 0.0, valleyVar =0.0;
 				double summitMean = 0.0, valleyMean = 0.0;
 				for(int k = 0; k < summit.size(); ++k){
 					summitMean += summit.get(k);
 				}
 				summitMean /= (double)summit.size();
 				for(int k = 0; k < valley.size(); ++k){
 					valleyMean += valley.get(k);
 				}
 				valleyMean /= (double)(valley.size());
 				for(int k = 0; k < summit.size(); ++k){
 					summitVar += (summit.get(k) - summitMean) * (summit.get(k) - summitMean);
 				}
 				if(summit.size() > 1){
 					summitVar /= (double) (summit.size()-1);
 				}else{
 					summitVar = 0;
 				}
 				for(int k = 0; k < valley.size(); ++k){
 					valleyVar += (valley.get(k) - valleyMean) * (valley.get(k) - valleyMean);
 				}
 				if(valley.size() > 1){
 					valleyVar /= (double) (valley.size()-1);
 				}else{
 					valleyVar = 0;
 				}
 				double currentWindowSV = summitVar + valleyVar;
 
 				outFeature += groundTruth+","+currentWindowMean + "," + currentWindowVar + "," + currentWindowPeakFreq +","
				       + currentWindowSV;
 				Random r = new Random();
 				try{
 						fvOut.write(outFeature + "\n");
 				}catch(Exception e){
 					System.err.println("ERROR: "+ e.getMessage() + " "+ e);
 				}
 
 				firstRawDataInCurrentWindow += N/2; // shift half of the window size
 			}
 
 		}
 
 
 	}
 	public static void computeDFT(AccelElement X[], int N, double Y[]) {
 
 		for (int i = 0; i < (N/2 + 1); ++i) {
 			double realPart = 0;
 			double imgPart = 0;
 			for (int j = 0; j < N; ++j) {
 				realPart += (X[j].magnitude * Math.cos(-(2.0 * Math.PI * i * j)/N));
 				imgPart +=  (X[j].magnitude * Math.sin(-(2.0 * Math.PI * i * j)/N));
 			}
 			realPart /= N;
 			imgPart /= N;
 			Y[i] = 2 * Math.sqrt(realPart * realPart + imgPart * imgPart);
 		}
 	}
 
 
 	public static void initAccelList(AccelElement[] a){
 		for(int i = 0; i < a.length; ++i){
 			a[i] = null;
 		}
 	}
 
 }
 
 
 
 class AccelElement{
 	double magnitude;
 	long timeStamp;
 
 	public AccelElement(long t, double x,double y,double z){
 
 		this.magnitude = Math.sqrt(x *x + y*y + z*z);
 		this.timeStamp = t;
 	}
 }
