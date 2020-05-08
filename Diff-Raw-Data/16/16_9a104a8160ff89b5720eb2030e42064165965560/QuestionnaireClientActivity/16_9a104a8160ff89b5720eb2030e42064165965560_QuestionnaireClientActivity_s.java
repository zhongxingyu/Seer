 package de.hszigr.mobileapps.questionnaire.client.activity;
 
 import java.util.Map;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.*;
 import android.widget.*;
 import android.widget.AdapterView.OnItemClickListener;
 import de.hszigr.mobileapps.R;
 import de.hszigr.mobileapps.questionnaire.client.util.ActivityUtils;
 import de.hszigr.mobileapps.questionnaire.client.util.QuestionnaireService;
 import de.hszigr.mobileapps.questionnaire.client.util.Settings;
 
 public class QuestionnaireClientActivity extends Activity {
 
     private static final int SELECT_ACTION_DIALOG = 0;
     private static final int RETRY_DIALOG = 1;
     private static final int PROGRESS_DIALOG = 2;
 
     private ListView listViewQuestionnaires;
 
     private Intent intentSettings;
     private Intent intentQuestionnaire;
 
     private Map<String, String> questionnaireOverview;
 
     protected String selectedQuestionnaire;
 
     private ProgressDialog progressDialog;
 
     private boolean update = true;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         initialize();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         updateQuestionnaireList();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
 
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.settings:
             startActivity(intentSettings);
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
         switch (id) {
         case SELECT_ACTION_DIALOG:
             final String items[] = { getString(R.string.PARTICIPATE),
                     getString(R.string.SHOW_STATISTICS) };
 
             builder.setTitle(R.string.SELECT_ACTION);
             builder.setItems(items, new DialogInterface.OnClickListener() {
 
                 public void onClick(DialogInterface dialog, int which) {
                     switch (which) {
                     case 0:
                         intentQuestionnaire.putExtra("qid", selectedQuestionnaire);
                         startActivity(intentQuestionnaire);
                         break;
 
                     default:
                         ActivityUtils.showInfoMessage(getApplicationContext(), R.string.ACTION_NOT_AVAILABLE);
                         break;
                     }
                 }
             });
 
             return builder.create();
 
         case RETRY_DIALOG:
             builder.setMessage(R.string.ERROR_FETCHING_OVERVIEW);
             builder.setPositiveButton(R.string.RETRY, new OnClickListener() {
 
                 public void onClick(DialogInterface dialog, int which) {
                     updateQuestionnaireList();
                 }
             });
 
             builder.setNegativeButton(R.string.CLOSE_APP, new OnClickListener() {
 
                 public void onClick(DialogInterface dialog, int which) {
                     finish();
                 }
             });
 
             builder.setNeutralButton(R.string.CANCEL, new OnClickListener() {
 
                 public void onClick(DialogInterface dialog, int which) {
                     update = false;
                 }
             });
 
             return builder.create();
 
         case PROGRESS_DIALOG:
             progressDialog = ProgressDialog.show(this, "", getString(R.string.LOADING), true, false);
             return progressDialog;
 
         default:
             return null;
         }
     }
 
     private void initialize() {
         findViewReferences();
         prepareActivities();
         setViewListeners();
     }
 
     private void findViewReferences() {
         listViewQuestionnaires = (ListView) findViewById(R.id.questionnaireList);
     }
 
     private void prepareActivities() {
         intentSettings = new Intent(this, SettingsActivity.class);
         intentQuestionnaire = new Intent(this, QuestionnaireActivity.class);
     }
 
     private void updateQuestionnaireList() {
         if (!update) {
             update = true;
             return;
         }
 
         showDialog(PROGRESS_DIALOG);
         UpdateThread thread = new UpdateThread();
         thread.start();
     }
 
     private void setViewListeners() {
         listViewQuestionnaires.setOnItemClickListener(new OnItemClickListener() {
 
             public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                 TextView item = (TextView) arg1;
                 String title = item.getText().toString();
                 selectedQuestionnaire = questionnaireOverview.get(title);
 
                 showDialog(SELECT_ACTION_DIALOG);
             }
         });
     }
 
     private Handler handler = new Handler() {
 
         @Override
         public void handleMessage(Message msg) {
             progressDialog.dismiss();
 
             if (msg.what == 0) {
                 String[] data = (String[]) msg.getData().getSerializable("data");
                 
                 ListAdapter adapter = new ArrayAdapter<String>(getApplicationContext(),
                     android.R.layout.simple_list_item_1, data);
                 listViewQuestionnaires.setAdapter(adapter);
                 
             } else if (msg.what == 1) {
                 showDialog(RETRY_DIALOG);
             }
         };
 
     };
 
     private class UpdateThread extends Thread {
 
         @Override
         public void run() {
             listViewQuestionnaires.clearChoices();
 
            SharedPreferences settings = getSharedPreferences(Settings.NAME, 0);
            String baseUrl = settings.getString(Settings.BASE_URL, Settings.DEFAULT_BASE_URL);
 
             QuestionnaireService service = new QuestionnaireService();
 
             try {
                 questionnaireOverview = service.getQuestionnaireOverview(baseUrl);
 
                 String[] data = new String[questionnaireOverview.size()];
                 questionnaireOverview.keySet().toArray(data);
 
                 Bundle bundle = new Bundle();
                 bundle.putSerializable("data", data);
                 
                 Message msg = new Message();
                 msg.setData(bundle);
 
                 handler.sendMessage(msg);
             } catch (Exception e) {
                 System.out.println(e.getMessage());
                 handler.sendEmptyMessage(1);
             }
         }
 
     }
 }
