 package br.unicamp.signunlock;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.EditText;
import android.widget.Toast;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.List;
 
 
 public class TrainingActivity extends Activity {
     private DrawView drawView;
     int numsigs = 0;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         drawView = (DrawView) findViewById(R.id.home_signature_view);
         drawView.requestFocus();
     }
 
     public void restartButton(View v) {
         drawView.path.reset();
        drawView.points.clear();
         drawView.invalidate();
     }
 
     public void okButton(View v) {
         drawView.path.reset();
         drawView.invalidate();
 

        List<DrawPoint> myPoints = drawView.points;
        if(myPoints.size() < 2){
            Toast.makeText(this, "please draw something", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("GOT", "" + myPoints.size());
        String fileName = ((EditText) findViewById(R.id.editText)).getText().toString();
        saveSignature(myPoints, "/sig_"+fileName+ TrainingStack.getStackSize()+".ser");

         Boolean authentic = ((CheckBox) findViewById(R.id.checkBox)).isChecked();
 
         double[] featureVector = (new SignPreProcess(drawView.points)).getFeatureVector();
         TrainingStack.addFeature(featureVector);
         if(authentic)
            TrainingStack.addClass(new double[]{1,0});
         else
            TrainingStack.addClass(new double[]{0,1});
 
 
 //        List<DrawPoint> myPoints2 = loadSignature("/sig_"+fileName+TrainingStack.getStackSize()+".ser");
 
 //        if(TrainingStack.getStackSize() == 3){
 //            Log.d("STACK5", "OI OI OI");
 //            SignatureNeuralNetwork snn = new SignatureNeuralNetwork(TrainingStack.getFeatures(), TrainingStack.getClasses());
 //        }
         drawView.points.clear();
     }
 
 
     public void saveSignature(List<DrawPoint> obj, String fileName) {
         try {
             // file is stored in /storage/emulated/0/mysigX.ser
 
             File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                     + fileName);
             Log.d("FILENAME", myFile.getAbsolutePath());
             if (!myFile.exists()) {
                 myFile.createNewFile();
             }
             FileOutputStream fileOut =
                     new FileOutputStream(myFile, false);
             ObjectOutputStream out = new ObjectOutputStream(fileOut);
             out.writeObject(obj);
             out.close();
             fileOut.close();
         } catch (IOException i) {
             Log.d("ERROR", i.toString());
             i.printStackTrace();
         }
     }
 
     List<DrawPoint> loadSignature(String fileName) {
         List<DrawPoint> dp = null;
         try {
             File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                     + fileName);
             FileInputStream fileIn = new FileInputStream(myFile);
             ObjectInputStream in = new ObjectInputStream(fileIn);
             dp = (List<DrawPoint>) in.readObject();
             in.close();
             fileIn.close();
 
         } catch (IOException i) {
             i.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
 
         return dp;
 
     }
 }
