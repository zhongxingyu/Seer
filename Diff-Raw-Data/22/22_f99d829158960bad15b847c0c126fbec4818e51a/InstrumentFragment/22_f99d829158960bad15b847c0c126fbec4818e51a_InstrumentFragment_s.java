 package uk.ac.cam.db538.dexter.android;
 
 import android.app.Activity;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import org.apache.commons.io.FileUtils;
 import org.jf.dexlib.DexFile;
 import org.jf.dexlib.DexFileFromMemory;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import uk.ac.cam.db538.dexter.ProgressCallback;
 import uk.ac.cam.db538.dexter.apk.Apk;
import uk.ac.cam.db538.dexter.dex.AuxiliaryDex;
 import uk.ac.cam.db538.dexter.dex.Dex;
 import uk.ac.cam.db538.dexter.dex.type.ClassRenamer;
 import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 public class InstrumentFragment extends PackageFragment {
 
     private boolean running = false;
 
     private Package packageInfo;
 
     private TextView textStatus;
     private ProgressCircleView progressCircle;
     private ProgressBar memoryBar;
 
     private String currentStatus = "Loading...";
     private int currentProgress = 0;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         packageInfo = extractArgsPackage();
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         setRetainInstance(true);
         System.out.println("ActivityCreated");
         assignValues();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         View thisView = inflater.inflate(R.layout.fragment_instrument, container, false);
 
         textStatus = (TextView) thisView.findViewById(R.id.textStatus);
         progressCircle = (ProgressCircleView) thisView.findViewById(R.id.progressCircle);
         memoryBar = (ProgressBar) thisView.findViewById(R.id.memoryBar);
 
         // load fonts
         Typeface ttfRobotoLight = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
         Typeface ttfRobotoThin = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Thin.ttf");
 
         textStatus.setTypeface(ttfRobotoLight);
         progressCircle.setTypeface(ttfRobotoThin);
 
         return thisView;
     }
 
     private void assignValues() {
         textStatus.setText(currentStatus);
         if (currentProgress < 0)
             progressCircle.setWaiting();
         else
             progressCircle.setValue(currentProgress);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         memoryTimer = new Timer();
         memoryTimer.schedule(new TimerTask() {
             @Override
             public void run() {
                 getActivity().runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         Runtime rt = Runtime.getRuntime();
                         long max = rt.maxMemory();
                         long allocated = rt.totalMemory();
                         long free = rt.freeMemory();
 
                         memoryBar.setProgress((int) (100L * (allocated - free) / max));
                     }
                 });
             }
         }, 0, 500);
 
         System.out.println("Resume");
         assignValues();
 
         synchronized (this) {
             if (!running) {
                 new Thread(workerInstrumentation).start();
                 running = true;
             }
         }
     }
 
     @Override
     public void onPause() {
         super.onPause();
         memoryTimer.cancel();
     }
 
     private ProgressCallback callbackProgressUpdate = new ProgressCallback() {
         @Override
         public void update(final int finished, final int outOf) {
             getActivity().runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     currentProgress = 100 * finished / outOf;
                     assignValues();
                 }
             });
         }
     };
 
     private Runnable workerInstrumentation = new Runnable() {
         @Override
         public void run() {
             setUiPriority_High();
             try {
                 final String packageName = packageInfo.getPackageName();
 
                 final File localFileTemp = new File(getActivity().getFilesDir(), "temp.apk");
                 final File localFileFinal = new File(
                         getActivity().getDir("ready", Activity.MODE_PRIVATE),
                         packageName +  ".apk");
                 final DexterApplication thisApp = (DexterApplication) getActivity().getApplication();
 
                 setStatus("Loading files...");
                 setWaiting();
 
                 /*
                  * We could load the DexFiles while framework is being loaded, but that might
                  * use too much memory. This way, only one .dex file is being loaded at
                  * any point of time.
                  */
 
                 // thisApp.waitForHierarchy();
 
                 FileUtils.copyFile(packageInfo.getPackageFile(), localFileTemp);
                 DexFile fileApp = new DexFile(localFileTemp);
                 DexFile fileAux = new DexFileFromMemory(thisApp.getAssets().open("dexter_aux.dex"));
 
                 Pair<RuntimeHierarchy, ClassRenamer> buildData = thisApp.getRuntimeHierarchy(fileApp, fileAux);
                 RuntimeHierarchy hierarchy = buildData.getValA();
                 ClassRenamer renamerAux = buildData.getValB();
 
                 setStatus("Analyzing...");
                 Dex dexApp = new Dex(
                         fileApp,
                         hierarchy,
                         new AuxiliaryDex(fileAux, hierarchy, renamerAux),
                         callbackProgressUpdate);
 
                 buildData = null;
                 fileApp = null;
                 fileAux = null;
                 renamerAux = null;
                 System.gc();
 
                 setStatus("Modifying...");
 
 //                 dex.instrument(false);
 
                 setStatus("Assembling...");
                 byte[] fileApp_New = dexApp.writeToFile();
 
                 setStatus("Signing...");
                 setWaiting();
                 Apk.produceAPK(localFileTemp, localFileFinal, null, fileApp_New);
 
 //                setStatus("Uninstalling...");
 //                setWaiting();
 //
 //                Uri packageURI = Uri.parse("package:" + packageName);
 //                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
 //                startActivity(uninstallIntent);
 
                 closeActivity();
 
             } catch (IOException ex) {
                 throw new RuntimeException(ex);
             }
         }
 
         private void setUiPriority_High() {
             Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
             getActivity().runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                 }
             });
         }
 
         private void setStatus(final String status) {
             getActivity().runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     currentStatus = status;
                     assignValues();
                 }
             });
         }
 
         private void setWaiting() {
             getActivity().runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     currentProgress = -1;
                     assignValues();
                 }
             });
         }
 
         private void hideProgressCircle() {
             getActivity().runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     progressCircle.setVisibility(View.INVISIBLE);
                 }
             });
         }
 
         private void closeActivity() {
             getActivity().runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     getActivity().finish();
                 }
             });
         }
     };
 
     private Timer memoryTimer;
 }
