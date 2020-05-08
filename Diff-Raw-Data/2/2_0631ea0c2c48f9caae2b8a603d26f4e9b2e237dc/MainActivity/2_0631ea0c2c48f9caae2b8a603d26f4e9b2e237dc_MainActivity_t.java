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
 
 public class MainActivity extends Activity
 {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         String LOG_TAG = "TestIterations";
 
         long targetIterationCount = 0L, targetIterationTime = 0L, previousIterationCount = 0L, previousIterationTime = 0L;
 
         //set algorithm name
         String algorithName = "PBEWITHSHA1AND128BITAES-CBC-BC";
 
         //set generic password
         String passphrase = "thisisatest"; //10 characters long
 
         //set goal time in ms
         long goalTime = 500L;
 
         //set iteration increment step
         int iterationStep = 100; //need to determine best increment step
 
         //initialize iteration starting point
         int currentIterationCount = 0;
 
         //initialize previous iteration elapsed time
         long previousIterationElapsedTime = 0L;
 
         //generate salt
         //https://github.com/WhisperSystems/TextSecure/blob/master/src/org/thoughtcrime/securesms/crypto/MasterSecretUtil.java#L233
         //notes, this changed in android 4+? to SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "Crypto");
         try {
             SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
             byte[] salt = new byte[16];//textsecure uses 8, NIST recommends minimum 16
             random.nextBytes(salt);
 
             while(true) {
                 currentIterationCount += iterationStep;
 
                 //int startTime = System.currentTimeMillis();
                 long startTime = SystemClock.elapsedRealtime();
 
                 //https://github.com/WhisperSystems/TextSecure/blob/master/src/org/thoughtcrime/securesms/crypto/MasterSecretUtil.java#L241
                 PBEKeySpec keyspec = new PBEKeySpec(passphrase.toCharArray(), salt, currentIterationCount);
                 SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithName);
                 SecretKey sk = skf.generateSecret(keyspec);
 
                 //int finishTime = System.currentTimeMillis();
                 long finishTime = SystemClock.elapsedRealtime();
 
                 if (finishTime-startTime > goalTime) {
                     Log.d(LOG_TAG, "Current iteration count of " + currentIterationCount + " exceeds the goalTime of: " + goalTime + "ms by " + (finishTime-startTime) + "ms");
                     Log.d(LOG_TAG, "The previous iteration count was: " + (currentIterationCount - iterationStep) + " and took: " + previousIterationElapsedTime + "ms that is under the goalTime of " + goalTime + " by " + (goalTime-previousIterationElapsedTime) + "ms");
                     targetIterationCount = currentIterationCount;
                     targetIterationTime = finishTime-startTime;
                     previousIterationCount = currentIterationCount - iterationStep;
                     previousIterationTime = previousIterationElapsedTime;
                     break;
                 } else {
                     Log.d(LOG_TAG, "Current iteration count of " + currentIterationCount + " took " + (finishTime-startTime) + "ms and has " + (goalTime-(finishTime-startTime))  + "ms more to reach the goalTime of: " + goalTime + "ms");
                     previousIterationElapsedTime = finishTime-startTime;
                 }
             }
 	} catch (NoSuchAlgorithmException e) {
 	    Log.e(LOG_TAG, "NoSuchAlgorithmException: " + e.toString());
         } catch (InvalidKeySpecException e) {
             Log.e(LOG_TAG, "InvalidKeySpecException: " + e.toString());
         }
         ((TextView) findViewById(R.id.resultsText)).setText("The below results are using the algorithm: " + algorithName
            + " with passphrase: " + passphrase + System.getProperty("line.separator")
             + System.getProperty("line.separator") + "Target Iteration Count: " + targetIterationCount
             + System.getProperty("line.separator") + "Target Iteration Duration: " + targetIterationTime + "ms"
             + System.getProperty("line.separator") + System.getProperty("line.separator")
             + "Prior Iteration Count: " + previousIterationCount + System.getProperty("line.separator")
             + "Prior Iteration Duration: " + previousIterationTime + "ms");
     }
 }
