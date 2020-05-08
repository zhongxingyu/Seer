 package at.mfellner.cucumber.android.api;
 
 import android.app.Activity;
 import android.app.Instrumentation;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Looper;
 import android.util.Log;
 import at.mfellner.cucumber.android.runtime.AndroidBackend;
 import at.mfellner.cucumber.android.runtime.AndroidFormatter;
 import at.mfellner.cucumber.android.runtime.AndroidResourceLoader;
 import cucumber.runtime.Backend;
 import cucumber.runtime.Runtime;
 import cucumber.runtime.RuntimeOptions;
 import cucumber.runtime.io.ResourceLoader;
 import cucumber.runtime.model.*;
 import gherkin.formatter.Formatter;
 import gherkin.formatter.Reporter;
 import gherkin.formatter.model.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 public class CucumberInstrumentation extends Instrumentation {
     public static final String REPORT_VALUE_ID = "InstrumentationTestRunner";
     public static final String REPORT_KEY_NUM_TOTAL = "numtests";
     public static final String REPORT_KEY_NUM_CURRENT = "current";
     public static final String REPORT_KEY_NAME_CLASS = "class";
     public static final String REPORT_KEY_NAME_TEST = "test";
     public static final int REPORT_VALUE_RESULT_START = 1;
     public static final int REPORT_VALUE_RESULT_FAILURE = -2;
     public static final String REPORT_KEY_STACK = "stack";
     public static final String TAG = "cucumber-android";
     RuntimeOptions mRuntimeOptions;
     ResourceLoader mResourceLoader;
     ClassLoader mClassLoader;
     private Runtime mRuntime;
 
     @Override
     public void onCreate(Bundle arguments) {
         super.onCreate(arguments);
 
         Context context = getContext();
         mClassLoader = context.getClassLoader();
         // this is the package name as defined in AndroidManifest.xml
         String glue = context.getPackageName();
 
         Properties properties = new Properties();
         // 'features' must be a subdirectory of the test-projects 'assets' directory
         String features = "features";
         properties.setProperty("cucumber.options", String.format("-g %s %s", glue, features));
         mRuntimeOptions = new RuntimeOptions(properties);
 
         mResourceLoader = new AndroidResourceLoader(context);
         List<Backend> backends = new ArrayList<Backend>();
         backends.add(new AndroidBackend(this));
         mRuntime = new Runtime(mResourceLoader, mClassLoader, backends, mRuntimeOptions);
 
         start();
     }
 
     @Override
     public void onStart() {
         Looper.prepare();
 
         List<CucumberFeature> cucumberFeatures = mRuntimeOptions.cucumberFeatures(mResourceLoader);
         int numScenarios = 0;
 
         for (CucumberFeature feature : cucumberFeatures) {
             for (CucumberTagStatement statement : feature.getFeatureElements()) {
                 if (statement instanceof CucumberScenario) {
                     numScenarios++;
                 } else if (statement instanceof CucumberScenarioOutline) {
                     for (CucumberExamples examples : ((CucumberScenarioOutline) statement).getCucumberExamplesList()) {
                         for (ExamplesTableRow row : examples.getExamples().getRows()) {
                             numScenarios++;
                         }
                     }
                     numScenarios--; // subtract table header
                 }
             }
         }
         Log.d(TAG, String.format("Number of Scenarios: %d", numScenarios));
 
         AndroidReporter reporter = new AndroidReporter(numScenarios);
         mRuntimeOptions.formatters.clear();
         mRuntimeOptions.formatters.add(reporter);
 
         for (CucumberFeature cucumberFeature : cucumberFeatures) {
             Formatter formatter = mRuntimeOptions.formatter(mClassLoader);
             cucumberFeature.run(formatter, reporter, mRuntime);
         }
         Formatter formatter = mRuntimeOptions.formatter(mClassLoader);
 
         formatter.done();
         printSummary();
         formatter.close();
 
         finish(Activity.RESULT_OK, new Bundle());
     }
 
     private void printSummary() {
         for (Throwable t : mRuntime.getErrors()) {
             Log.e(TAG, t.toString());
         }
         for (String s : mRuntime.getSnippets()) {
             Log.w(TAG, s);
         }
     }
 
     /**
      * This class reports the current test-state back to the framework.
      * It wraps the AndroidFormatter because some of its information is required.
      */
     private class AndroidReporter implements Formatter, Reporter {
         private final AndroidFormatter mFormatter;
         private final Bundle mResultTemplate;
         private Bundle mTestResult;
         private int mTestNum;
         private int mTestResultCode;
         private Feature mFeature;
         private boolean mBefore;
 
         public AndroidReporter(int numTests) {
             mFormatter = new AndroidFormatter(TAG);
             mResultTemplate = new Bundle();
             mResultTemplate.putString(Instrumentation.REPORT_KEY_IDENTIFIER, REPORT_VALUE_ID);
             mResultTemplate.putInt(REPORT_KEY_NUM_TOTAL, numTests);
         }
 
         @Override
         public void uri(String uri) {
             mFormatter.uri(uri);
         }
 
         @Override
         public void feature(Feature feature) {
             mFeature = feature;
             mFormatter.feature(feature);
         }
 
         @Override
         public void background(Background background) {
             mFormatter.background(background);
         }
 
         @Override
         public void scenario(Scenario scenario) {
             mFormatter.scenario(scenario);
             beginScenario(scenario);
         }
 
         @Override
         public void scenarioOutline(ScenarioOutline scenarioOutline) {
             mFormatter.scenarioOutline(scenarioOutline);
             beginScenario(scenarioOutline);
         }
 
         @Override
         public void examples(Examples examples) {
             mFormatter.examples(examples);
         }
 
         @Override
         public void step(Step step) {
             mFormatter.step(step);
         }
 
         @Override
         public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
             mFormatter.syntaxError(state, event, legalEvents, uri, line);
         }
 
         @Override
         public void eof() {
             mFormatter.eof();
         }
 
         @Override
         public void done() {
             mFormatter.done();
         }
 
         @Override
         public void close() {
             mFormatter.close();
         }
 
         @Override
         public void embedding(String mimeType, byte[] data) {
         }
 
         @Override
         public void write(String text) {
         }
 
         private void beginScenario(TagStatement scenario) {
             if (mBefore) {
                 mBefore = false;
                 String testClass = String.format("%s %s", mFeature.getKeyword(), mFeature.getName());
                 String testName = String.format("%s %s", scenario.getKeyword(), scenario.getName());
                 mTestResult = new Bundle(mResultTemplate);
                 mTestResult.putString(REPORT_KEY_NAME_CLASS, testClass);
                 mTestResult.putString(REPORT_KEY_NAME_TEST, testName);
                 mTestResult.putInt(REPORT_KEY_NUM_CURRENT, ++mTestNum);
 
                 mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT, String.format("\n%s:", testClass));
 
                 sendStatus(REPORT_VALUE_RESULT_START, mTestResult);
                 mTestResultCode = 0;
             }
         }
 
         @Override
         public void before(Match match, Result result) {
             // this method is called before the formatter gets to read the scenario
             // thus the reporting logic for each scenario(outline) is located in beginScenario
             mBefore = true;
         }
 
         @Override
         public void after(Match match, Result result) {
             if (mTestResultCode == 0) {
                 mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT, ".");
             }
             sendStatus(mTestResultCode, mTestResult);
             mBefore = false;
         }
 
         @Override
         public void match(Match match) {
         }
 
         @Override
         public void result(Result result) {
             if (result.getError() != null) {
                 mTestResult.putString(REPORT_KEY_STACK, result.getErrorMessage());
                 mTestResultCode = REPORT_VALUE_RESULT_FAILURE;
                 mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT, result.getErrorMessage());
             }
         }
     }
 }
