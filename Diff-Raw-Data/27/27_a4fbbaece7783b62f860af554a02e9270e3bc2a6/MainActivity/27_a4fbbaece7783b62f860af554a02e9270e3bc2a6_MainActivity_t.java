 package gsn.atl.nativeencryption;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Random;
 
 import javax.crypto.KeyGenerator;
 import javax.crypto.SecretKey;
 
 public class MainActivity extends Activity {
 
    private Button generateFile;
    private Button encrypt;
    private Button decryptLibrary;
    private Button decryptNative;
    private Button validate;
    private Button cleanup;
 
    private TextView status;
 
    private SecretKey key;
    private byte[] initVector = null;
 
    public static final String filename = "/jibberish.data";
    public static final String encryptedFilename = "/encrypted_jibberish.data";
    public static final String decryptedFilename = "/decrypted_jibberish.data";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         getViews();
         setListeners();
 
         // create our encryption key
         KeyGenerator keyGenerator = null;
         try {
             keyGenerator = KeyGenerator.getInstance("AES");
         } catch (NoSuchAlgorithmException e) {
             e.printStackTrace();
         }
         keyGenerator.init(128);
         key = keyGenerator.generateKey();
     }
 
     private void getViews() {
         generateFile = (Button) findViewById(R.id.generate_file);
         encrypt = (Button) findViewById(R.id.encrypt);
         decryptLibrary = (Button) findViewById(R.id.decrypt_library);
         decryptNative = (Button) findViewById(R.id.decrypt_native);
         validate = (Button) findViewById(R.id.validate);
         cleanup = (Button) findViewById(R.id.cleanup);
         status = (TextView) findViewById(R.id.status);
     }
 
     private void setListeners() {
         generateFile.setOnClickListener(generateFileListener);
         encrypt.setOnClickListener(encryptListener);
         decryptLibrary.setOnClickListener(decryptLibraryListener);
         decryptNative.setOnClickListener(decryptNativeListener);
         validate.setOnClickListener(validateListener);
         cleanup.setOnClickListener(cleanupListener);
     }
 
     private class GenerateFileTask extends AsyncTask<Void, Void, Void> {
 
         @Override
         protected Void doInBackground(Void... voids) {
             File directory = Environment.getExternalStorageDirectory();
             File file = new File(directory.getAbsolutePath() + "/jibberish.data");
 
             try {
                 file.createNewFile();
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
             FileOutputStream outputStream = null;
             try {
                 outputStream = new FileOutputStream(file);
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             }
 
             final int size = 5 * 1024 * 1024;
             final int bufferSize = 16 * 1024; // 16k write buffer
 
             int pending = size;
             byte[] buffer = new byte[bufferSize];
             Random random = new Random();
 
             while (pending > 0) {
                 random.nextBytes(buffer);
                 try {
                     outputStream.write(buffer, 0, buffer.length);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 pending -= buffer.length;
             }
 
             try {
                 outputStream.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
             return null;
         }
 
         @Override
         protected void onPostExecute(Void aVoid) {
             super.onPostExecute(aVoid);
             status.setText("File Generated");
             enableControls();
         }
     }
 
     private class EncryptLibraryTask extends AsyncTask<Void, Void, byte[]> {
 
         @Override
         protected byte[] doInBackground(Void... voids) {
             return Encryption.encryptLibrary(key);
         }
 
         @Override
         protected void onPostExecute(byte[] iv) {
             initVector = iv;
             enableControls();
             status.setText("File Encrypted");
         }
     }
 
     private class DecryptLibraryTask extends AsyncTask<Void, Void, Void> {
         long start, end;
 
         @Override
         protected Void doInBackground(Void... voids) {
             start = System.currentTimeMillis();
             Decryption.decryptLibrary(key, initVector);
             return null;
         }
 
         @Override
         protected void onPostExecute(Void aVoid) {
             end = System.currentTimeMillis();
             status.setText("File Decrypted in " + (end - start) / 1000.0 + " sec");
             enableControls();
         }
     }
 
     private class DecryptNativeTask extends AsyncTask<Void, Void, Void> {
         long start, end;
 
         @Override
         protected Void doInBackground(Void... voids) {
             start = System.currentTimeMillis();
             Decryption.decryptNative(key, initVector);
             return null;
         }
 
         @Override
         protected void onPostExecute(Void aVoid) {
             end = System.currentTimeMillis();
             status.setText("Decryption Finished in " + (end - start) / 1000.0 + " sec");
             enableControls();
         }
     }
 
     View.OnClickListener generateFileListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             status.setText("Generating file...");
             disableControls();
             GenerateFileTask generateFile = new GenerateFileTask();
             generateFile.execute();
         }
     };
 
     View.OnClickListener encryptListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             File directory = Environment.getExternalStorageDirectory();
             File jib = new File(directory.getAbsolutePath() + filename);
 
             if (!jib.exists()) {
                 status.setText("Generate file first");
             } else {
                 status.setText("Encrypting file..");
                 disableControls();
                 EncryptLibraryTask encryptLibraryTask = new EncryptLibraryTask();
                 encryptLibraryTask.execute();
             }
         }
     };
 
     View.OnClickListener decryptLibraryListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             File directory = Environment.getExternalStorageDirectory();
             File encrypted = new File(directory.getAbsolutePath() + encryptedFilename);
 
             if (!encrypted.exists() || (initVector == null)) {
                 status.setText("Encrypt file first");
             } else {
                 disableControls();
                 status.setText("Decrypting File");
                 DecryptLibraryTask decryptLibraryTask = new DecryptLibraryTask();
                 decryptLibraryTask.execute();
             }
         }
     };
 
     View.OnClickListener decryptNativeListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             File directory = Environment.getExternalStorageDirectory();
             File encrypted = new File(directory.getAbsolutePath() + encryptedFilename);
 
             if (!encrypted.exists() || (initVector == null)) {
                 status.setText("Encrypt file first");
             } else {
                 status.setText("Decrypting File");
                 disableControls();
                 DecryptNativeTask decryptNativeTask = new DecryptNativeTask();
                 decryptNativeTask.execute();
             }
         }
     };
 
     View.OnClickListener validateListener = new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             File directory = Environment.getExternalStorageDirectory();
             File encrypted = new File(directory + encryptedFilename);
             File decrypted = new File(directory + decryptedFilename);
 
             if (!encrypted.exists() || !decrypted.exists()) {
                 status.setText("Encrypt/Decrypt first");
             } else {
                 disableControls();
                 status.setText("Validating Files");
                 ValidateFilesTask validateFilesTask = new ValidateFilesTask();
                 validateFilesTask.execute();
             }
         }
     };
 
     private class ValidateFilesTask extends AsyncTask<Void, Void, Boolean> {
         @Override
         protected Boolean doInBackground(Void... voids) {
             File directory = Environment.getExternalStorageDirectory();
             File originalFile = new File(directory.getAbsolutePath() + filename);
             File decryptedFile = new File(directory.getAbsolutePath() + decryptedFilename);
 
             FileInputStream originalIn = null;
             FileInputStream decryptedIn = null;
 
             try {
                 originalIn = new FileInputStream(originalFile);
                 decryptedIn = new FileInputStream(decryptedFile);
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             }
 
             final int bufferSize = 16 * 1024;
             byte[] buffer1 = new byte[bufferSize];
             byte[] buffer2 = new byte[bufferSize];
             boolean valid = true;
             try {
                 while ((originalIn.available() > 0) && (decryptedIn.available() > 0)) {
                     originalIn.read(buffer1, 0, buffer1.length);
                     decryptedIn.read(buffer2, 0, buffer2.length);
                     for (int i = 0; i < bufferSize; i++) { /* has to be a better way to compare buffers? */
                         if (buffer1[i] != buffer2[i]) {
                             valid = false;
                         }
                     }
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
             return valid;
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             enableControls();
             if (result) {
                 status.setText("Encryption/Decryption Verified");
             } else {
                 status.setText("Something went wrong");
             }
         }
     }
 
     View.OnClickListener cleanupListener = new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             disableControls();
             status.setText("Deleting Files...");
             CleanupTask cleanupTask = new CleanupTask();
             cleanupTask.execute();
         }
     };
 
     private class CleanupTask extends AsyncTask<Void, Void, Void>{
 
         @Override
         protected Void doInBackground(Void... voids) {
             File directory = Environment.getExternalStorageDirectory();
             File jibberish = new File(directory.getAbsolutePath() + filename);
 
             if (jibberish.exists()){
                 jibberish.delete();
             }
 
             File encrypted = new File(directory.getAbsolutePath() + encryptedFilename);
             if (encrypted.exists()){
                 encrypted.delete();
             }
 
             File decrypted = new File(directory.getAbsolutePath() + decryptedFilename);
             if (decrypted.exists()){
                 decrypted.delete();
             }
 
             return null;
         }
 
         @Override
         protected void onPostExecute(Void aVoid) {
             enableControls();
             status.setText("Files removed");
         }
     }
 
     private void disableControls() {
         generateFile.setEnabled(false);
         encrypt.setEnabled(false);
         decryptLibrary.setEnabled(false);
         decryptNative.setEnabled(false);
         validate.setEnabled(false);
         cleanup.setEnabled(false);
     }
 
     private void enableControls() {
         generateFile.setEnabled(true);
         encrypt.setEnabled(true);
         decryptLibrary.setEnabled(true);
         decryptNative.setEnabled(true);
         validate.setEnabled(true);
         cleanup.setEnabled(true);
     }
 }
