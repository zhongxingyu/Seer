 package net.jiangyouxin.vnotepad;
 
 import java.io.*;
 
 import android.app.*;
 import android.os.*;
 import android.widget.*;
 
 public class MainActivity extends Activity {
     private static String FILENAME = "vnotepad.txt";
     private static String FILENAME_ORIG = FILENAME + ".orig";
     private static String FILENAME_SERVER = FILENAME + ".server";
 
     private EditText edit;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         loadFile();
     }
 
     @Override
     public void onDestroy() {
         saveFile();
         super.onDestroy();
     }
 
     private boolean loadFile() {
        String res;
        FileInputStream fin;
         try {
             fin = openFileInput(FILENAME);
             int length = fin.available();
             byte[] buffer = new byte[length];
             fin.read(buffer);
             res = new String(buffer, "utf8");
         } catch (Exception e) {
         } finally {
            if (fin != null)
                 fin.close();
         } 
         if (res != null) {
             edit.setText(res);
             return true;
         } else {
             return false;
         }
     }
 
     private boolean saveFile() {
        String res = edit.getText();
        FileOutputStream fout;
         try {
             fout = openFileOutput(FILENAME, MODE_PRIVATE);
             fout.write(res.getBytes("utf8"));
         } catch (Exception e) {
         } finally {
            if (fout != null)
                 fout.close();
         }
         return true;
     }
 }
