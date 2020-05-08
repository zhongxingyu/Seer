 package com.fusionx.tilal6991.multiboot;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
import com.fusionx.tilal6991.multiboot.CreateMultiBootRom.CreateMultibootRomAsync;

 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.text.method.ScrollingMovementMethod;
 import android.widget.TextView;
 import android.app.Activity;
 import android.content.Intent;
 
 public class MakeLoopGapps extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_make_loop_gapps);
         CreateMultibootGappsAsync instance = new CreateMultibootGappsAsync();
         instance.execute(getIntent().getExtras());
     }
 
     public class CreateMultibootGappsAsync extends
             AsyncTask<Bundle, String, Void> {
         final String externalPath = Environment.getExternalStorageDirectory()
                 .getAbsolutePath();
         final String tempSdCardDir = externalPath + "/tempMultiboot/";
         final String finalOutdir = Environment.getExternalStorageDirectory()
                 .getAbsolutePath() + "/multiboot/";
         final static String dataDir = "/data/data/com.fusionx.tilal6991.multiboot/files/";
 
         String inputFile;
         String romExtractionDir;
         String romName;
         String systemImageName;
 
         boolean nandRom = false;
 
         Bundle bundle;
 
         @Override
         protected Void doInBackground(Bundle... params) {
             bundle = params[0];
             inputFile = Environment.getExternalStorageDirectory()
                     .getAbsolutePath() + "/" + bundle.getString("filename");
             romName = bundle.getString("filename").replace(".zip", "");
             romExtractionDir = tempSdCardDir + romName + "/";
 
             preClean();
 
             publishProgress("Making directories");
             new File(romExtractionDir).mkdirs();
             new File(finalOutdir + "loop-roms").mkdirs();
 
             publishProgress("Getting data from wizard");
             systemImageName = bundle.getString("systemimagename");
 
             extractRom();
             fixUpdaterScript();
             packUpAndFinish();
             cleanup();
             return null;
         }
 
        private Handler mHandler = new Handler();

         private Runnable mFinished = new Runnable() {
             public void run() {
                 Intent intent = new Intent(getApplicationContext(),
                         MainActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
             }
         };
 
         private void preClean() {
             publishProgress("Running a preclean");
             CommonFunctions.deleteIfExists(finalOutdir + "loop-roms/" + romName
                     + "-loopinstall.zip");
             CommonFunctions.deleteIfExists(tempSdCardDir);
         }
 
         private void packUpAndFinish() {
             publishProgress("Making ROM zip");
             CommonFunctions.runRootCommands(new String[] {
                     "cd " + romExtractionDir,
                     dataDir + "zip -r -q " + finalOutdir + "loop-roms/"
                             + romName + "-loopinstall.zip " + "*" });
         }
 
         private void fixUpdaterScript() {
             String updaterScript = romExtractionDir
                     + "META-INF/com/google/android/updater-script";
             String findString = null;
             try {
                 Scanner scanner = new Scanner(new File(updaterScript));
                 while (scanner.hasNextLine()) {
                     findString = scanner.nextLine();
                     if (findString.contains("format(")
                            && (findString.contains("\"MTD\", \"system\""))) {
                        break;
                    }
                 }
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             }
            publishProgress("Editing updater script");
            findAndReplaceInFile(updaterScript, findString, "");
             findAndReplaceInFile(
                     updaterScript,
                     "mount(\"yaffs2\", \"MTD\", \"system\", \"/system\");",
                     "run_program(\"/sbin/losetup\", \"/dev/block/loop0\", \"/sdcard/multiboot/"
                             + systemImageName
                             + "\");\n"
                             + "run_program(\"/sbin/mke2fs\", \"-T\", \"ext2\", \"/dev/block/loop0\");\n"
                             + "run_program(\"/sbin/mount\", \"-t\", \"ext2\", \"/dev/block/loop0\", \"/system\");");
             findAndReplaceInFile(
                     updaterScript,
                     "unmount(\"/system\");",
                     "unmount(\"/system\");\n"
                             + "run_program(\"/sbin/losetup\", \"-d\", \"/dev/block/loop0\");");
 
         }
 
         private void findAndReplaceInFile(String fileName, String findString,
                 String replaceString) {
             try {
                 Scanner scanner = new Scanner(new File(fileName));
                 FileWriter s = new FileWriter(new File(fileName + ".fix"));
                 while (scanner.hasNextLine()) {
                     String nextLine = scanner.nextLine();
                     if (nextLine.contains(findString))
                         s.write(replaceString + "\n");
                     else
                         s.write(nextLine + "\n");
                 }
                 s.close();
                 CommonFunctions.runRootCommand("mv " + fileName + ".fix "
                         + fileName);
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         private void extractRom() {
             publishProgress("Extracting ROM");
             CommonFunctions.runRootCommand(dataDir + "busybox unzip -q "
                     + inputFile + " -d " + romExtractionDir);
         }
 
         private void cleanup() {
             publishProgress("Cleaning up");
             CommonFunctions.deleteIfExists(tempSdCardDir);
             CommonFunctions.deleteIfExists(dataDir);
             publishProgress("Finished!");
         }
 
         @Override
         protected void onProgressUpdate(String... values) {
             super.onProgressUpdate(values);
             WriteOutput(values[0]);
             if (values[0] == "Finished!")
                mHandler.postDelayed(mFinished, 5000);
         }
 
         public void WriteOutput(String paramString) {
             TextView editText = (TextView) findViewById(R.id.editText1);
             editText.append(paramString + "\n");
             editText.setMovementMethod(new ScrollingMovementMethod());
         }
     }
 
 }
