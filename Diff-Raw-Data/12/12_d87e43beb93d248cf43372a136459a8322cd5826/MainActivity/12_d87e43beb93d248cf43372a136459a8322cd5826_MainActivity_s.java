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
 
     //Baseline starting point for key iterations
     int keyIterationBaseline = 15000;
 
     //Baseline duration
     long baselineDuration = 0L;
 
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
 
         long startTime = SystemClock.elapsedRealtime();
 
         SecretKey sk = generateKey(passphrase, generateSalt(), keyIterationBaseline, algorithName);
 
         //int finishTime = System.currentTimeMillis();
         long finishTime = SystemClock.elapsedRealtime();
 
         long elapsedTime = finishTime - startTime;
         
         Log.d(LOG_TAG, "Iterations per millisecond: " + keyIterationBaseline / elapsedTime);
 
         results.add(elapsedTime); //overall time consumed
         results.add((long) keyIterationBaseline); //baseline key iterations
         results.add((long) keyIterationBaseline / elapsedTime); //iterations per millisecond

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
                    + System.getProperty("line.separator") + "Iterations per millisecond: " + results.get(2);
             }
         }
     }
 	
 }
