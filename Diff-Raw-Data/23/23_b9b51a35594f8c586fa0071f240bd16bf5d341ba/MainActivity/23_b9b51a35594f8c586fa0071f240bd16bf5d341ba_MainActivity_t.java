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
        edit = (EditText)findViewById(R.id.edit_text);
         loadFile();
     }
 
     @Override
     public void onDestroy() {
         saveFile();
         super.onDestroy();
     }
 
     private boolean loadFile() {
        String res = null;
        FileInputStream fin = null;
         try {
             fin = openFileInput(FILENAME);
             int length = fin.available();
             byte[] buffer = new byte[length];
             fin.read(buffer);
             res = new String(buffer, "utf8");
         } catch (Exception e) {
         } finally {
            if (fin != null) try {
                 fin.close();
            } catch (IOException e) {
            }
         } 
         if (res != null) {
             edit.setText(res);
             return true;
         } else {
             return false;
         }
     }
 
     private boolean saveFile() {
        String res = edit.getText().toString();
        FileOutputStream fout = null;
         try {
             fout = openFileOutput(FILENAME, MODE_PRIVATE);
             fout.write(res.getBytes("utf8"));
         } catch (Exception e) {
         } finally {
            if (fout != null) try {
                 fout.close();
            } catch (IOException e) {
            }
         }
         return true;
     }
 }
