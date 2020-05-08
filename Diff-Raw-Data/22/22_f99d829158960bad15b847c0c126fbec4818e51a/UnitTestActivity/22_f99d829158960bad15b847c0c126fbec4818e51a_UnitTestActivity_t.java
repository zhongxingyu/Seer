 package uk.ac.cam.db538.dexter.android;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import org.apache.commons.io.FileUtils;
 import org.jf.dexlib.DexFile;
 import org.jf.dexlib.DexFileFromMemory;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.jar.Attributes;
 import java.util.jar.JarEntry;
 import java.util.jar.JarOutputStream;
 import java.util.jar.Manifest;
 
 import dalvik.system.DexClassLoader;
 import uk.ac.cam.db538.dexter.ProgressCallback;
 import uk.ac.cam.db538.dexter.apk.Apk;
 import uk.ac.cam.db538.dexter.dex.Dex;
 import uk.ac.cam.db538.dexter.dex.type.ClassRenamer;
 import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
 import uk.ac.cam.db538.dexter.transform.Transform;
import uk.ac.cam.db538.dexter.transform.taint.AuxiliaryDex;
import uk.ac.cam.db538.dexter.transform.taint.TestingTaintTransform;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 public class UnitTestActivity extends Activity {
 
     private Typeface ttfRobotoLight;
     private Typeface ttfRobotoThin;
 
     private LinearLayout spaceTestCases;
     private View[] viewTestCases;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_test);
 
         this.getActionBar().hide();
 
         // load fonts
         ttfRobotoLight = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
         ttfRobotoThin = Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
 
         spaceTestCases = (LinearLayout) findViewById(R.id.testListSpace);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         prepareTestClasses(new Runnable() {
             @Override
             public void run() {
                 UnitTestActivity.this.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         generateTestViews();
                         runTestCases();
                     }
                 });
             }
         });
     }
 
     private void generateTestViews() {
         if (viewTestCases != null)
             return;
 
         int testCount = getTestCount();
         viewTestCases = new View[testCount];
 
         for (int i = 0; i < testCount; i++) {
 
             viewTestCases[i] = getLayoutInflater().inflate(R.layout.listitem_testcase, spaceTestCases, false);
             TextView textName = (TextView) viewTestCases[i].findViewById(R.id.testName);
             TextView textDesc = (TextView) viewTestCases[i].findViewById(R.id.testDescription);
 
             textName.setText(getTestName(i));
             textDesc.setText(getTestDescription(i));
 
             textName.setTypeface(ttfRobotoLight);
             textDesc.setTypeface(ttfRobotoLight);
 
             spaceTestCases.addView(viewTestCases[i]);
         }
     }
 
     public void runTestCases() {
         Thread t = new Thread() {
             @Override
             public void run() {
                 int testCount = getTestCount();
                 for (int i = 0; i < testCount; i++) {
                     showSpinner(i);
                     setResult(i, runTest(i));
                 }
             }
 
             private void showSpinner(final int index) {
                 UnitTestActivity.this.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         ProgressBar spinner =
                             (ProgressBar) viewTestCases[index].findViewById(R.id.spinner);
                         spinner.setVisibility(View.VISIBLE);
                     }
                 });
             }
 
             private void setResult(final int index, final boolean passed) {
                 UnitTestActivity.this.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         ProgressBar spinner =
                                 (ProgressBar) viewTestCases[index].findViewById(R.id.spinner);
                         spinner.setVisibility(View.GONE);
 
                         if (passed) {
                             CheckBox checkBox = (CheckBox) viewTestCases[index].findViewById(R.id.checkPassed);
                             checkBox.setVisibility(View.VISIBLE);
                             checkBox.setChecked(passed);
                         } else
                             viewTestCases[index].setBackgroundColor(getResources().getColor(R.color.alizarin));
                     }
                 });
             }
         };
         t.start();
     }
 
     private Class<?> getTestClass() {
         try {
             synchronized (testClassLoader) {
                 return testClassLoader.loadClass("uk.ac.cam.db538.dexter.tests.TestList");
             }
         } catch (ClassNotFoundException ex) {
             throw new Error(ex);
         }
     }
 
     private int getTestCount() {
         try {
             Method m = getTestClass().getDeclaredMethod("getTestCount");
             return (Integer) m.invoke(null);
         } catch (Exception ex) {
             throw new Error(ex);
         }
     }
 
     private String getTestName(int index) {
         try {
             Method m = getTestClass().getDeclaredMethod("getTestName", int.class);
             return (String) m.invoke(null, Integer.valueOf(index));
         } catch (Exception ex) {
             throw new Error(ex);
         }
     }
 
     private String getTestDescription(int index) {
         try {
             Method m = getTestClass().getDeclaredMethod("getTestDescription", int.class);
             return (String) m.invoke(null, Integer.valueOf(index));
         } catch (Exception ex) {
             throw new Error(ex);
         }
     }
 
     private boolean runTest(int index) {
         try {
             Method m = getTestClass().getDeclaredMethod("runTest", int.class);
             return (Boolean) m.invoke(null, Integer.valueOf(index));
         } catch (Exception ex) {
             throw new Error(ex);
         }
     }
 
     private static ClassLoader testClassLoader = null;
 
     private final static String DEXTER_TEST_APK = "dexter_test.apk";
     private final static String DEXTER_TEST_DEX = "dexter_test.dex";
 
     private void prepareTestClasses(final Runnable whenDone) {
         if (testClassLoader != null) {
             (new Thread(whenDone)).start();
             return;
         }
 
         final ProgressDialog dialog = ProgressDialog.show(this, "Preparing tests", "", true, false);
         Thread t = new Thread() {
             @Override
             public void run() {
                 try {
                     DexterApplication thisApp = (DexterApplication) getApplication();
 
                     setMessage("Loading files...");
 
                     DexFile fileTestApp = new DexFileFromMemory(
                             UnitTestActivity.this.getAssets().open(DEXTER_TEST_DEX));
                     DexFile fileAux = new DexFileFromMemory(
                             UnitTestActivity.this.getAssets().open("dexter_aux.dex"));
 
                     setMessage("Building hierarchy...");
 
                     Pair<RuntimeHierarchy, ClassRenamer> buildData =
                             thisApp.getRuntimeHierarchy(fileTestApp, fileAux);
                     RuntimeHierarchy hierarchy = buildData.getValA();
                     ClassRenamer renamerAux = buildData.getValB();
 
                     setMessage("Parsing...");
 
                     Dex dexTestApp = new Dex(
                             fileTestApp,
                             hierarchy,
                             new AuxiliaryDex(fileAux, hierarchy, renamerAux));
 
                     setMessage("Instrumenting...");
 
                    Transform transform = new TestingTaintTransform();
                     transform.apply(dexTestApp);
 
                     setMessage("Saving...");
 
                     byte[] dexInstrumented = dexTestApp.writeToFile();
                     File fileApk = new File(UnitTestActivity.this.getFilesDir(), DEXTER_TEST_APK);
                     Manifest manifest = new Manifest();
                     manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
                     JarOutputStream jos = new JarOutputStream(new FileOutputStream(fileApk), manifest);
                     JarEntry entryClassesDex = new JarEntry("classes.dex");
                     jos.putNextEntry(entryClassesDex);
                     jos.write(dexInstrumented);
                     jos.closeEntry();
                     jos.close();
 
                     dismissProgressDialog();
 
                     testClassLoader = new DexClassLoader(
                             fileApk.getAbsolutePath(),
                             UnitTestActivity.this.getDir("dex", 0).getAbsolutePath(),
                             null,
                             getClass().getClassLoader());
 
                     whenDone.run();
                 } catch (Exception ex) {
                     ex.printStackTrace();
                     throw new RuntimeException(ex);
                 }
             }
 
             private void setMessage(final String msg) {
                 UnitTestActivity.this.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         dialog.setMessage(msg);
                     }
                 });
             }
 
             private void dismissProgressDialog() {
                 UnitTestActivity.this.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         dialog.dismiss();
 
                     }
                 });
             }
         };
         t.start();
     }
 }
