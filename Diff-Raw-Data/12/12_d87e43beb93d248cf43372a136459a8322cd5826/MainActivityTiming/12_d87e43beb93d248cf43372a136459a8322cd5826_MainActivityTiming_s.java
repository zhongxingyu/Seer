 package com.joeykrim.pbekeytester;
 
 import android.app.*;
 import android.os.*;
 import android.view.*;
 import android.widget.*;
 import java.security.*;
 import javax.crypto.spec.*;
 import javax.crypto.*;
 import java.security.spec.*;
 import android.util.*;
 import java.util.*;
 import android.support.v4.util.*;
 import java.math.*;
 
public class MainActivity extends Activity {
 
     String LOG_TAG = "TestIterations";
 	
     //set algorithm name
     String algorithName = "PBEWITHSHA1AND128BITAES-CBC-BC";
 
     //set generic password
     String passphrase = "thisisatest"; //10 characters long
 
     //set goal time in ms
     long goalTime = 500L;
     
     //use range for goal time
     //target 500ms using range +-20%
     long targetGoalMinimum = (long) (goalTime*0.8);//400L;
     long targetGoalMaximum = (long) (goalTime*1.2);//600L;
 
     //initialize iteration starting point and increment ranges
     int keyIterationLocationStart = 10000;
     //int keyIterationEighthStep = keyIterationLocationStart/8; //1250
     int keyIterationQuarterStep = keyIterationLocationStart/4; //2500
     int keyIterationHalfStep = keyIterationLocationStart/2; //5000
     int keyIterationFullStep = keyIterationLocationStart; //10000
     int keyIterationDoubleStep = keyIterationLocationStart*2; //20000
     int keyIterationQuadrupleStep = keyIterationLocationStart*4; //40000
     int keyIterationOctupleStep = keyIterationLocationStart*8; //80000
     
     int keyIterationCurrent = keyIterationLocationStart;
     int currentIteration = 1;
 
     //initialize previous counters
     long elapsedTimePrevious = 0L;
     int keyIterationPrevious = 0;
         
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 		
         ((TextView) findViewById(R.id.resultsText)).setText("Solving...");
 
         backgroundIterationCount bTask = new backgroundIterationCount();
         bTask.execute();
     }
 
     public ArrayList<Long> solveTargetIteractionCount() {
 
         ArrayList<Long> results = new ArrayList<Long>();
 
         long overallStartTime = SystemClock.elapsedRealtime();
 
         while(true) {
             //int startTime = System.currentTimeMillis();
             long startTime = SystemClock.elapsedRealtime();
 
             SecretKey sk = generateKey(passphrase, generateSalt(), keyIterationCurrent, algorithName);
 
             //int finishTime = System.currentTimeMillis();
             long finishTime = SystemClock.elapsedRealtime();
 
             long elapsedTime = finishTime-startTime;
             long overallTime = finishTime-overallStartTime;
 
             /*if ((currentIteration == 1 || currentIteration == 2) && elapsedTime < (targetGoalMinimum/2)) {
                 //increase iteration count by larger increment
                 Log.d(LOG_TAG, "Iteration count " + currentIteration + " at Key Iterations of " + keyIterationCurrent
                     + " took " + elapsedTime + "ms which is less than half the targetGoalMinimum by " + ((targetGoalMinimum/2)-elapsedTime)
                     + "ms taking overall time of " + (SystemClock.elapsedRealtime()-overallStartTime) + "ms");
                 keyIterationPrevious = keyIterationCurrent;
                 keyIterationCurrent += keyLargeIterationIncrement;
             } else if (elapsedTime < ((targetGoalMinimum/4)*3)) {
                 //increase iteration count by medium increment
                 Log.d(LOG_TAG, "Iteration count " + currentIteration + " at Key Iterations of " + keyIterationCurrent
                     + " took " + elapsedTime + "ms which is less than three fourths the targetGoalMinimum by " + (((targetGoalMinimum/4)*3)-elapsedTime)
                     + "ms taking overall time of " + (SystemClock.elapsedRealtime()-overallStartTime) + "ms");
                 keyIterationPrevious = keyIterationCurrent;
                 keyIterationCurrent += keyMediumIterationIncrement;
             } else if (elapsedTime < targetGoalMinimum) {
                 //increase iteration count by smaller increment
                 Log.d(LOG_TAG, "Iteration count " + currentIteration + " at Key Iterations of " + keyIterationCurrent
                     + " took " + elapsedTime + "ms which is less than the targetGoalMinimum by " + (targetGoalMinimum-elapsedTime)
                     + "ms taking overall time of " + (SystemClock.elapsedRealtime()-overallStartTime) + "ms");
                 keyIterationPrevious = keyIterationCurrent;
                 keyIterationCurrent += keySmallIterationIncrement;
             } else if (elapsedTime > (targetGoalMaximum*2)) {
                 //decrease iteration count by larger amount
                 Log.d(LOG_TAG, "Iteration count " + currentIteration + " at Key Iterations of " + keyIterationCurrent
                     + " took " + elapsedTime + "ms which is greater than the targetGoalMaximum by " + (targetGoalMaximum*2-elapsedTime)
                     + "ms taking overall time of " + (SystemClock.elapsedRealtime()-overallStartTime) + "ms");
                 keyIterationPrevious = keyIterationCurrent;
                 keyIterationCurrent -= keyLargeIterationIncrement;
             } else if (elapsedTime > (targetGoalMaximum + (targetGoalMaximum/4))) {
                 //decrease iteration count by medium amount
                 Log.d(LOG_TAG, "Iteration count " + currentIteration + " at Key Iterations of " + keyIterationCurrent
                     + " took " + elapsedTime + "ms which is greater than five fourths targetGoalMaximum by " + ((targetGoalMaximum + (targetGoalMaximum/4))-elapsedTime)
                     + "ms taking overall time of " + (SystemClock.elapsedRealtime()-overallStartTime) + "ms");
                 keyIterationPrevious = keyIterationCurrent;
                 keyIterationCurrent -= keyMediumIterationIncrement;
             } else if (elapsedTime > targetGoalMaximum) {
                 //decrease iteration count by smaller amount
                 Log.d(LOG_TAG, "Iteration count " + currentIteration + " at Key Iterations of " + keyIterationCurrent
                     + " took " + elapsedTime + "ms which is greater than the targetGoalMaximum by " + (targetGoalMaximum-elapsedTime)
                     + "ms taking overall time of " + (SystemClock.elapsedRealtime()-overallStartTime) + "ms");
                 keyIterationPrevious = keyIterationCurrent;
                 keyIterationCurrent -= keySmallIterationIncrement;
             } else {
                 Log.d(LOG_TAG, "Iteration count " + currentIteration + " at Key Iterations of " + keyIterationCurrent
                     + " took " + elapsedTime + "ms which is between the targetGoalMinimum of " + targetGoalMinimum
                     + "ms and the targetGoalMaximum of " + targetGoalMaximum
                     + "ms taking a total search time of " + (SystemClock.elapsedRealtime()-overallStartTime) + "ms");
 
                 //Output key
                 //http://stackoverflow.com/a/7176483
                 Log.d(LOG_TAG, "Final SecretKey: " + new BigInteger(1, sk.getEncoded()).toString(16));
 
                 results.add(finishTime - overallStartTime); //overall time consumed
                 results.add( (long) keyIterationCurrent); //targetIterationCount
                 results.add(elapsedTime); //targetIterationTime
                 results.add( (long) keyIterationPrevious);
                 results.add(elapsedTimePrevious);
                 return results;
             }
             currentIteration++;
             elapsedTimePrevious = elapsedTime;
 			*/
 			
 			//fractions can be reduced but are keeping the denominator the same for readability
 			if ((targetGoalMinimum-elapsedTime) > 0) {
 				Log.d(LOG_TAG, "Iteration count " + currentIteration + " at Key Iterations of " + keyIterationCurrent
 				  + " took " + elapsedTime + "ms which varies against the targetGoalMinimum by " + (targetGoalMinimum-elapsedTime)
 				  + "ms taking overall time of " + overallTime + "ms");
 			} else {
 				Log.d(LOG_TAG, "Iteration count " + currentIteration + " at Key Iterations of " + keyIterationCurrent
 				  + " took " + elapsedTime + "ms which varies against the targetGoalMaximum by " + (elapsedTime-targetGoalMaximum)
 				  + "ms taking overall time of " + overallTime + "ms");			
 			}
 			if (elapsedTime < (targetGoalMinimum*2/8)) {
 				keyIterationPrevious = keyIterationCurrent;
 				keyIterationCurrent += keyIterationOctupleStep;
                 Log.d(LOG_TAG, "Elapsed time: " + elapsedTime + "ms is < targetGoalMinimum*2/8: " + (targetGoalMinimum*2/8));
 				Log.d(LOG_TAG, "Increment octuple step: " + keyIterationOctupleStep);
 			} else if (elapsedTime < (targetGoalMinimum*3/8)) {
 				keyIterationPrevious = keyIterationCurrent;
 				keyIterationCurrent += keyIterationQuadrupleStep;
                 Log.d(LOG_TAG, "Elapsed time: " + elapsedTime + "ms is < targetGoalMinimum*3/8: " + (targetGoalMinimum*3/8));
 				Log.d(LOG_TAG, "Increment quadruple step: " + keyIterationQuadrupleStep);
 			} else if (elapsedTime < (targetGoalMinimum*4/8)) {
 				keyIterationPrevious = keyIterationCurrent;
 				keyIterationCurrent += keyIterationDoubleStep;
                 Log.d(LOG_TAG, "Elapsed time: " + elapsedTime + "ms is < targetGoalMinimum*4/8: " + (targetGoalMinimum*4/8));
 				Log.d(LOG_TAG, "Increment double step: " + keyIterationDoubleStep);
 			} else if (elapsedTime < (targetGoalMinimum*5/8)) {
 				keyIterationPrevious = keyIterationCurrent;
 				keyIterationCurrent += keyIterationFullStep;
                 Log.d(LOG_TAG, "Elapsed time: " + elapsedTime + "ms is < targetGoalMinimum*5/8: " + (targetGoalMinimum*5/8));
 				Log.d(LOG_TAG, "Increment full step: " + keyIterationFullStep);
 			} else if (elapsedTime < (targetGoalMinimum*6/8)) {
 				keyIterationPrevious = keyIterationCurrent;
 				keyIterationCurrent += keyIterationHalfStep;
                 Log.d(LOG_TAG, "Elapsed time: " + elapsedTime + "ms is < targetGoalMinimum*6/8: " + (targetGoalMinimum*6/8));
 				Log.d(LOG_TAG, "Increment half step: " + keyIterationHalfStep);
 			} else if (elapsedTime < (targetGoalMinimum*7/8)) {
 				keyIterationPrevious = keyIterationCurrent;
 				keyIterationCurrent += keyIterationQuarterStep;
                 Log.d(LOG_TAG, "Elapsed time: " + elapsedTime + "ms is < targetGoalMinimum*7/8: " + (targetGoalMinimum*7/8));
 				Log.d(LOG_TAG, "Increment quarter step: " + keyIterationQuarterStep);
 			} else if (elapsedTime < targetGoalMinimum) {
 				keyIterationPrevious = keyIterationCurrent;
 				//keyIterationCurrent += keyIterationEighthStep;
 				//Log.d(LOG_TAG, "Increment eighth step: " + keyIterationEighthStep);
 				keyIterationCurrent += keyIterationQuarterStep;
                 Log.d(LOG_TAG, "Elapsed time: " + elapsedTime + "ms is < targetGoalMinimum: " + targetGoalMinimum);
 				Log.d(LOG_TAG, "Increment prev eighth now quarter step: " + keyIterationQuarterStep);
 			} else if (elapsedTime >= targetGoalMinimum && elapsedTime <= targetGoalMaximum || overallTime > 10000) {
 				Log.d(LOG_TAG, "Target met! " + elapsedTime + "ms is between the targetGoalMinimum of " + targetGoalMinimum
 					  + "ms and the targetGoalMaximum of " + targetGoalMaximum
 					  + "ms taking a total search time of " + overallTime + "ms");
 
 				//Output key
 				//http://stackoverflow.com/a/7176483
 				Log.d(LOG_TAG, "Final SecretKey: " + new BigInteger(1, sk.getEncoded()).toString(16));
 
 				results.add(finishTime - overallStartTime); //overall time consumed
 				results.add( (long) currentIteration); //overall iterations required
 				results.add( (long) keyIterationCurrent); //targetIterationCount
 				results.add(elapsedTime); //targetIterationTime
 				results.add( (long) keyIterationPrevious);
 				results.add(elapsedTimePrevious);
 				return results;
 			} else if (elapsedTime < (targetGoalMaximum*9/8)) {
 				keyIterationPrevious = keyIterationCurrent;
 				//keyIterationCurrent -= keyIterationEighthStep;
 				keyIterationCurrent -= keyIterationQuarterStep;
                 		Log.d(LOG_TAG, "Elapsed time: " + elapsedTime + "ms is < targetGoalMaximum*9/8: " + (targetGoalMaximum*9/8));
                 		Log.d(LOG_TAG, "Decrement prev eighth now quarter step: " + keyIterationQuarterStep);
 			} else if (elapsedTime < (targetGoalMaximum*10/8)) {
 				keyIterationPrevious = keyIterationCurrent;
 				keyIterationCurrent -= keyIterationQuarterStep;
         			Log.d(LOG_TAG, "Elapsed time: " + elapsedTime + "ms is < targetGoalMaximum*10/8: " + (targetGoalMaximum*10/8));
 				Log.d(LOG_TAG, "Decrement quarter step: " + keyIterationQuarterStep);
 			} else if (elapsedTime < (targetGoalMaximum*11/8)) {
 				keyIterationPrevious = keyIterationCurrent;
 				keyIterationCurrent -= keyIterationHalfStep;
                 		Log.d(LOG_TAG, "Elapsed time: " + elapsedTime + "ms is < targetGoalMaximum*11/8: " + (targetGoalMaximum*11/8));
 				Log.d(LOG_TAG, "Decrement half step: " + keyIterationHalfStep);
 			} else if (elapsedTime < (targetGoalMaximum*12/8)) {
 				keyIterationPrevious = keyIterationCurrent;
 				keyIterationCurrent -= keyIterationFullStep;
                 		Log.d(LOG_TAG, "Elapsed time: " + elapsedTime + "ms is < targetGoalMaximum*12/8: " + (targetGoalMaximum*12/8));
 				Log.d(LOG_TAG, "Decrement full step: " + keyIterationFullStep);
 			} else if (elapsedTime < (targetGoalMaximum*13/8)) {
 				keyIterationPrevious = keyIterationCurrent;
 				keyIterationCurrent -= keyIterationDoubleStep;
                 		Log.d(LOG_TAG, "Elapsed time: " + elapsedTime + "ms is < targetGoalMaximum*13/8: " + (targetGoalMaximum*13/8));
 				Log.d(LOG_TAG, "Decrement double step: " + keyIterationDoubleStep);
 			} else if (elapsedTime < (targetGoalMaximum*14/8)) {
 				keyIterationPrevious = keyIterationCurrent;
 				keyIterationCurrent -= keyIterationQuadrupleStep;
                 		Log.d(LOG_TAG, "Elapsed time: " + elapsedTime + "ms is < targetGoalMaximum*14/8: " + (targetGoalMaximum*14/8));
 				Log.d(LOG_TAG, "Decrement quadruple step: " + keyIterationQuadrupleStep);
 			} else if (elapsedTime < (targetGoalMaximum*15/8)) {
 				keyIterationPrevious = keyIterationCurrent;
 				keyIterationCurrent -= keyIterationOctupleStep;
                 		Log.d(LOG_TAG, "Elapsed time: " + elapsedTime + "ms is < targetGoalMaximum*15/8: " + (targetGoalMaximum*15/8));
 				Log.d(LOG_TAG, "Decrement octuple step: " + keyIterationOctupleStep);
 			}
 			if (keyIterationCurrent < keyIterationLocationStart) {
 				keyIterationCurrent = keyIterationLocationStart;
 				Log.d(LOG_TAG, "key iteration count was lowered than start location. resetting to start location.");
 			}
 			currentIteration++;
 			elapsedTimePrevious = elapsedTime;
 			
         }
     }
 
 
     //https://github.com/WhisperSystems/TextSecure/blob/master/src/org/thoughtcrime/securesms/crypto/MasterSecretUtil.java#L233
     //notes, this changed in android 4+? to SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "Crypto");
     public byte[] generateSalt() {
         byte[] salt = new byte[16];
         try {
             SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
             //textsecure uses 8, NIST recommends minimum 16
             random.nextBytes(salt);
         } catch (NoSuchAlgorithmException e) {
             Log.e(LOG_TAG, "NoSuchAlgorithmException: " + e.toString());
         }
         return salt;
     }
 
     //https://github.com/WhisperSystems/TextSecure/blob/master/src/org/thoughtcrime/securesms/crypto/MasterSecretUtil.java#L241
     public SecretKey generateKey(String passphrase, byte[] salt, int currentIterationCount, String algorithName) {
         SecretKey sk = null;
         try {
             PBEKeySpec keyspec = new PBEKeySpec(passphrase.toCharArray(), salt, currentIterationCount);
             SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithName);
             sk = skf.generateSecret(keyspec);
         } catch (InvalidKeySpecException e) {
             Log.e(LOG_TAG, "InvalidKeySpecException: " + e.toString());
         } catch (NoSuchAlgorithmException e2){
             Log.e(LOG_TAG, "NoSuchAlgorithmException: " + e2.toString());
         }
         return sk;
     }
 
     public class backgroundIterationCount extends AsyncTask<Void, Void, ArrayList<Long>> {
         @Override protected ArrayList<Long> doInBackground(Void... params) {
             return solveTargetIteractionCount();
         }
 
         @Override protected void onPostExecute(ArrayList<Long> results) {
             if (!isCancelled()) {
                 ((TextView) findViewById(R.id.resultsText)).setText("The below results are using the algorithm: "+ algorithName
                     + " with passphrase: " + passphrase + System.getProperty("line.separator")
                     + System.getProperty("line.separator") + "Overall time required: " + results.get(0) + "ms"
                     + System.getProperty("line.separator") + " for " + results.get(1) + " iterations."
                     + System.getProperty("line.separator") + "Target Iteration Count: " + results.get(2)
                     + System.getProperty("line.separator") + "Target Iteration Duration: " + results.get(3) + "ms"
                     + System.getProperty("line.separator") + System.getProperty("line.separator")
                     + "Prior Iteration Count: " + results.get(4) + System.getProperty("line.separator")
                     + "Prior Iteration Duration: " + results.get(5) + "ms");
             }
         }
     }
 	
 }
