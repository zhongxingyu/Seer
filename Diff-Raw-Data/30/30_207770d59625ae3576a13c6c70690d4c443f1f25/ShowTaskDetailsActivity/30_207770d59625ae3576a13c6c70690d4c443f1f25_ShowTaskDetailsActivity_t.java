 package com.axioma.taskmanager;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.Toast;
 
 import com.axioma.taskmanager.async.AsyncCallback;
 import com.axioma.taskmanager.async.GetTaskDetailsInBackground;
 import com.axioma.taskmanager.async.RunTaskInBackground;
 import com.axioma.taskmanager.util.IdentityName;
 import com.google.common.collect.Maps;
 
 /**
  * @author rkannappan
  */
 public class ShowTaskDetailsActivity extends Activity implements AsyncCallback {
 
    private String taskName = null;
    private String taskRawName = null;
    private String taskType = null;
    private String taskTypeDesc = null;
 
    private Map<String, String> paramNameToDisplayNameMap;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
 
       // Get the message from the intent
       Intent intent = getIntent();
       String taskType = intent.getStringExtra(ShowTasksActivity.SELECTED_TASK_TYPE);
       String taskTypeDesc = intent.getStringExtra(ShowTasksActivity.SELECTED_TASK_TYPE_DESC);
       String taskName = intent.getStringExtra(ShowTasksActivity.SELECTED_TASK_NAME);
       String taskRawName = intent.getStringExtra(ShowTasksActivity.SELECTED_TASK_RAW_NAME);
 
       this.taskType = taskType;
       this.taskTypeDesc = taskTypeDesc;
       this.taskName = taskName;
       this.taskRawName = taskRawName;
 
       System.out.println("type in intent " + taskType);
       System.out.println("name in intent " + taskName);
       System.out.println("raw name in intent " + taskRawName);
       System.out.println("type desc in intent " + taskTypeDesc);
 
       this.setupParamNameToDisplayNameMap();
 
       new GetTaskDetailsInBackground(ShowTaskDetailsActivity.this, taskName, taskRawName, taskType, taskTypeDesc, this).execute();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       // Inflate the menu; this adds items to the action bar if it is present.
       getMenuInflater().inflate(R.menu.show_task_details, menu);
       return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       // Handle presses on the action bar items
       switch (item.getItemId()) {
          case R.id.action_run:
             new RunTaskInBackground(ShowTaskDetailsActivity.this, this.taskName, this.taskRawName, this.taskType,
                      this.taskTypeDesc,
                      new RunTaskCallback()).execute();
             return true;
          case R.id.action_result:
             Intent intent = new Intent(ShowTaskDetailsActivity.this, PlotResultsActivity.class);
             intent.putExtra(ShowTasksActivity.SELECTED_TASK_TYPE, taskType);
             intent.putExtra(ShowTasksActivity.SELECTED_TASK_NAME, taskName);
             intent.putExtra(ShowTasksActivity.SELECTED_TASK_TYPE_DESC, taskTypeDesc);
             intent.putExtra(ShowTasksActivity.SELECTED_TASK_RAW_NAME, taskRawName);
             startActivity(intent);
             return true;
          default:
             return super.onOptionsItemSelected(item);
       }
    }
 
    @Override
    public void postProcessing(String results) {
       String[] paramValues = null;
 
       try {
          JSONObject attribute = new JSONObject(results);
 
          JSONObject values = attribute.getJSONObject("parameters");
 
          paramValues = new String[(values.length() * 2) + 4];
 
          String taskName = attribute.getString("name");
 
          int i = 0;
 
          paramValues[i++] = "Task Name";
          paramValues[i++] = IdentityName.valueOf(taskName).getName();
 
          paramValues[i++] = "Task Type";
          paramValues[i++] = taskTypeDesc;
 
          Iterator<Object> it = values.keys();
          while (it.hasNext()) {
             String paramName = it.next().toString();
             System.out.println(paramName);
             paramValues[i++] = this.getCleansedParamName(paramName);
 
             String paramValue = values.getString(paramName);
             System.out.println(paramValue);
 
             paramValues[i++] = this.getCleansedParamValue(paramValue);
          }
 
          setContentView(R.layout.activity_show_task_details);
       } catch (JSONException e) {
          Log.e("JSON Parser", "Error parsing data " + e.toString());
       }
 
       GridView gridView = (GridView) findViewById(R.id.taskDetails);
 
       ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, paramValues);
 
       gridView.setAdapter(adapter);
    }
 
    private class RunTaskCallback implements AsyncCallback {
 
       @Override
       public void postProcessing(String results) {
          Toast.makeText(ShowTaskDetailsActivity.this, "Task finished with status " + results, Toast.LENGTH_LONG).show();
 
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
             this.showNotification(results);
          }
       }
 
       @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
       private void showNotification(final String results) {
//         // Prepare intent which is triggered if the
//         // notification is selected
//
//         Intent intent = new Intent(ShowTaskDetailsActivity.this, PlotResultsActivity.class);
//         intent.putExtra(ShowTasksActivity.SELECTED_TASK_TYPE, taskType);
//         intent.putExtra(ShowTasksActivity.SELECTED_TASK_NAME, taskName);
//         intent.putExtra(ShowTasksActivity.SELECTED_TASK_TYPE_DESC, taskTypeDesc);
//         intent.putExtra(ShowTasksActivity.SELECTED_TASK_RAW_NAME, taskRawName);
//
//         PendingIntent pIntent = PendingIntent.getActivity(ShowTaskDetailsActivity.this, 0, intent, 0);
 
          // Build notification
          Notification noti =
                   new Notification.Builder(ShowTaskDetailsActivity.this)
                            .setContentTitle(taskName + " finished with status " + results)
                           .setContentText(results).setSmallIcon(R.drawable.axioma_launcher).build();
 
          NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 
          // Hide the notification after its selected
          noti.flags |= Notification.FLAG_AUTO_CANCEL;
 
          notificationManager.notify(0, noti);
       }
    }
 
    private String getCleansedParamName(final String paramName) {
       String cleansedName = this.paramNameToDisplayNameMap.get(paramName);
       if (cleansedName == null) {
          cleansedName = paramName;
       }
 
       return cleansedName;
    }
 
    // Returns double values as it is and removes prefixes from others
    private String getCleansedParamValue(final String paramValue) {
       if (paramValue == null || paramValue.trim().isEmpty()) {
          return "";
       }
 
       // Not stripping off inidivual values inside the set for now
       if (paramValue.startsWith("[")) {
          return paramValue;
       }
 
       try {
          Double.valueOf(paramValue);
          return paramValue;
       } catch (NumberFormatException ex) {
          return IdentityName.valueOf(paramValue).getName();
       }
    }
 
    private void setupParamNameToDisplayNameMap() {
       this.paramNameToDisplayNameMap = Maps.newHashMap();
       this.paramNameToDisplayNameMap.put("catchUp", "Catch Up");
       this.paramNameToDisplayNameMap.put("riskModelName", "Risk Model Name");
       this.paramNameToDisplayNameMap.put("startDate", "Start Date");
       this.paramNameToDisplayNameMap.put("portfolioName", "Portfolio Name");
       this.paramNameToDisplayNameMap.put("alphaFactorVolatility", "Alpha Factor Volatility");
       this.paramNameToDisplayNameMap.put("benchmarkName", "Benchmark Name");
       this.paramNameToDisplayNameMap.put("tradingDayElementType", "Trading Day Element Type");
       this.paramNameToDisplayNameMap.put("mainFactorClassificationSchemeName", "Main Factor Classification Scheme Name");
       this.paramNameToDisplayNameMap.put("reportTaskId", "Report Task ID");
       this.paramNameToDisplayNameMap.put("endDate", "End Date");
       this.paramNameToDisplayNameMap.put("tradingDayElementId", "Trading Day Element ID");
       this.paramNameToDisplayNameMap.put("baseCurrencyCode", "Base Currency Code");
       this.paramNameToDisplayNameMap.put("priceAttributeName", "Price Attribute");
       this.paramNameToDisplayNameMap.put("includeCash", "Include Cash");
       this.paramNameToDisplayNameMap.put("tradingDayElementId", "Trading Day Element ID");
       this.paramNameToDisplayNameMap.put("mostRecentDate", "Most Recent Date");
       this.paramNameToDisplayNameMap.put("samplingFrequency", "Sampling Frequency");
       this.paramNameToDisplayNameMap.put("returnsAttributeName", "Returns Attribute");
       this.paramNameToDisplayNameMap.put("computeMarketTiming", "Compute Market Timing");
       this.paramNameToDisplayNameMap.put("useUserReturns", "Use User Returns");
       this.paramNameToDisplayNameMap.put("numberOfDataRollForwardDays", "Data Roll Forward Days");
       this.paramNameToDisplayNameMap.put("returnsStrategy", "Returns Strategy");
       this.paramNameToDisplayNameMap.put("computeMarketTiming", "Compute Market Timing");
       this.paramNameToDisplayNameMap.put("longOrShort", "Long/Short");
       this.paramNameToDisplayNameMap.put("reportDir", "Report Directory");
       this.paramNameToDisplayNameMap.put("numRegressionPeriods", "Regression Periods");
       this.paramNameToDisplayNameMap.put("cutOffRegressionPValue", "Regression cutoff");
       this.paramNameToDisplayNameMap.put("computeAdjustedPA", "Compute Adjusted PA");
       this.paramNameToDisplayNameMap.put("longTermMarketReturn", "Long Term Market Return");
       this.paramNameToDisplayNameMap.put("factorClassificationType", "Factor Classification Type");
       this.paramNameToDisplayNameMap.put("factorClassificationLevel", "Factor Classification Level");
       this.paramNameToDisplayNameMap.put("longTermMarketReturn", "Long Term Market Return");
       this.paramNameToDisplayNameMap.put("mainAssetClassificationName", "Asset Classification");
       this.paramNameToDisplayNameMap.put("reportCurrencyEffect", "Report Currency Effect");
       this.paramNameToDisplayNameMap.put("classificationLevels", "Classification Levels");
       this.paramNameToDisplayNameMap.put("includeInteractionEffect", "Include Interaction Effect");
       this.paramNameToDisplayNameMap.put("separateLongShort", "Separate Long Short");
       this.paramNameToDisplayNameMap.put("probabilityLevel", "Probability Level");
       this.paramNameToDisplayNameMap.put("includeAssetStyleData", "Include Asset Style Data");
       this.paramNameToDisplayNameMap.put("includeTimestampInFilename", "Include Timestamp");
       this.paramNameToDisplayNameMap.put("includeTimestampInReportFilename", "Include Timestamp");
       this.paramNameToDisplayNameMap.put("customStartDate", "Custom Start Date");
       this.paramNameToDisplayNameMap.put("customEndDate", "Custom End Date");
       this.paramNameToDisplayNameMap.put("assetIdentifier", "Asset Identifier");
       this.paramNameToDisplayNameMap.put("currencyFormat", "Currency Format");
       this.paramNameToDisplayNameMap.put("reports", "Reports");
       this.paramNameToDisplayNameMap.put("factorVolatilitiesReturnHistoryMinDays", "Factor Volatilities Return Min Days");
       this.paramNameToDisplayNameMap.put("factorVolatilitiesReturnHistoryMaxDays", "Factor Volatilities Return Max Days");
       this.paramNameToDisplayNameMap.put("customStyleFactors", "Custom Style Factors");
       this.paramNameToDisplayNameMap.put("numberOfCustomStyleFactorsLookbackDays", "Custom Style Factors Look back days");
       this.paramNameToDisplayNameMap.put("specificRiskReturnHistoryMinDays", "Specific Risk Return History Min Days");
       this.paramNameToDisplayNameMap.put("factorCorrelationsReturnHistoryMinDays", "Factor Correlations Return Min Days");
       this.paramNameToDisplayNameMap.put("factorCorrelationsReturnHistoryMaxDays", "Factor Correlations Return Max Days");
       this.paramNameToDisplayNameMap.put("specificRiskReturnHistoryMaxDays", "Specific Risk Return History Max Days");
       this.paramNameToDisplayNameMap.put("reportPath", "Report Path");
       this.paramNameToDisplayNameMap.put("numberOfRMMProcessesToUse", "RMM Processes");
       this.paramNameToDisplayNameMap.put("factorVolatilitiesHalfLifeDays", "Factor Volatilities Half Life Days");
       this.paramNameToDisplayNameMap.put("factorCorrelationsHalfLifeDays", "Factor Correlations Half Life Days");
       this.paramNameToDisplayNameMap.put("flatfilePath", "Flat File Path");
       this.paramNameToDisplayNameMap.put("customRiskModelName", "Custom Risk Model Name");
       this.paramNameToDisplayNameMap.put("fullCustomRiskModelName", "Custom Risk Model Name");
       this.paramNameToDisplayNameMap.put("specificRiskHalfLifeDays", "Specific Risk Half Life Days");
       this.paramNameToDisplayNameMap.put("historyStyle", "History Style");
       this.paramNameToDisplayNameMap.put("axiomaStyleFactors", "Axioma Style Factors");
       this.paramNameToDisplayNameMap.put("baseRiskModelName", "Base Risk Model Name");
       this.paramNameToDisplayNameMap.put("importUpToMostRecentDate", "Most Recent Date");
       this.paramNameToDisplayNameMap.put("latestFileTimestamp", "Latest File Timestamp");
       this.paramNameToDisplayNameMap.put("riskModelEncryptionType", "Risk Model Encryption Type");
       this.paramNameToDisplayNameMap.put("riskModelDataPath", "Risk Model Data Path");
       this.paramNameToDisplayNameMap.put("riskModelNames", "Risk Model Names");
       this.paramNameToDisplayNameMap.put("masterDataPath", "Master Data Path");
       this.paramNameToDisplayNameMap.put("skipAssetResolution", "Skip Asset Resolution");
       this.paramNameToDisplayNameMap.put("columnDelimiter", "Column Delimiter");
       this.paramNameToDisplayNameMap.put("fileSource", "File Source");
       this.paramNameToDisplayNameMap.put("saveAsUserAttributes", "Save As User Attributes");
       this.paramNameToDisplayNameMap.put("overwriteData", "Overwrite");
       this.paramNameToDisplayNameMap.put("overwriteAttributeData", "Overwrite");
       this.paramNameToDisplayNameMap.put("filenameDateFormat", "File name date format");
       this.paramNameToDisplayNameMap.put("importAllDatesIndirectory", "Import All Dates In Directory");
       this.paramNameToDisplayNameMap.put("assetIdentifierTypes", "Asset Identifier Types");
       this.paramNameToDisplayNameMap.put("tagSeparator", "Tag Separator");
       this.paramNameToDisplayNameMap.put("defaultCurrencyCode", "Currency Code");
       this.paramNameToDisplayNameMap.put("rescaleToOne", "Rescale to one");
       this.paramNameToDisplayNameMap.put("namePrefix", "Name Prefix");
       this.paramNameToDisplayNameMap.put("divideValuesBy100", "Divide Values By 100");
       this.paramNameToDisplayNameMap.put("aggregateDuplicateAssets", "Aggregate Duplicate Assets");
       this.paramNameToDisplayNameMap.put("skipCurrencyResolution", "Skip Currency Resolution");
       this.paramNameToDisplayNameMap.put("fileType", "File Type");
       this.paramNameToDisplayNameMap.put("unitType", "Unit Type");
       this.paramNameToDisplayNameMap.put("filePaths", "File Paths");
       this.paramNameToDisplayNameMap.put("importSpecificDateRange", "Specific Date Range");
       this.paramNameToDisplayNameMap.put("filePattern", "File Pattern");
       this.paramNameToDisplayNameMap.put("numberOfAssetIdentifierExtendedAvailabilityDays", "Asset Identifier Lookback days");
       this.paramNameToDisplayNameMap.put("returnsStrateg", "Returns Strategy");
       this.paramNameToDisplayNameMap.put("showExposureInCurrency", "Show Exposures In Currency");
       this.paramNameToDisplayNameMap.put("riskModelType", "Risk Model Type");
       this.paramNameToDisplayNameMap.put("date", "Date");
       this.paramNameToDisplayNameMap.put("dataName", "Data Name");
       this.paramNameToDisplayNameMap.put("dataDescription", "Data Description");
       this.paramNameToDisplayNameMap.put("importFileType", "File Type");
       this.paramNameToDisplayNameMap.put("skipMarketAssetLiveCheck", "Skip Market Asset Live Check");
    }
 }
