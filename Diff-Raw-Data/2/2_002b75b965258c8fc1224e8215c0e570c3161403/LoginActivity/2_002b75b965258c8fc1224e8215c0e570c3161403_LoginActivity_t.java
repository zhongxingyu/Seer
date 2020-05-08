 package com.speechpro.activity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.speechpro.R;
 import com.speechpro.client.SpeechProClient;
 import com.speechpro.data.User;
 import com.speechpro.database.DatabaseAdapter;
 import com.speechpro.record.ExtAudioRecorder;
 import com.speechpro.util.ResponseParser;
 import com.speechpro.util.ResponseResult;
 import com.speechpro.util.Utils;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: gb
  * Date: 11.01.13
  * Time: 0:44
  * To change this template use File | Settings | File Templates.
  */
 public class LoginActivity extends Activity {
 
     public static int CODE_ADD_USER = 5;
     public static int CODE_EDIT_USER = 6;
 
     private DatabaseAdapter dbAdapter;
     private ArrayAdapter<User> userAdapter;
     private ListView list;
     private Button buttonAdd;
     private Button buttonDelete;
     private Button buttonEdit;
     private Button buttonLogin;
     private int site;
     private int selectedPosition = -1;
     private ExtAudioRecorder extAudioRecorder;
 
     public void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login);
 
         site = getIntent().getIntExtra("site", DatabaseAdapter.VK);
 
         dbAdapter = new DatabaseAdapter(this);
         dbAdapter.open();
 
         buttonAdd = (Button) findViewById(R.id.buttonAdd);
         buttonAdd.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(LoginActivity.this, AddActivity.class);
                 intent.putExtra("site", site);
                 startActivityForResult(intent,CODE_ADD_USER);
             }
         });
 
 
         buttonDelete = (Button) findViewById(R.id.buttonDelete);
         buttonDelete.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (selectedPosition != -1){
                     User user = userAdapter.getItem(selectedPosition);
                     if (user != null){
                         dbAdapter.deleteUser(user);
                         userAdapter.remove(user);
                         userAdapter.notifyDataSetChanged();
                     }
                 }else{
                     Toast.makeText(LoginActivity.this, "Select user to delete", Toast.LENGTH_SHORT).show();
                 }
             }
         });
 
         buttonEdit = (Button) findViewById(R.id.buttonEdit);
         buttonEdit.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (selectedPosition != -1){
                     User user = userAdapter.getItem(selectedPosition);
                     Intent intent = new Intent(LoginActivity.this, AddActivity.class);
                     intent.putExtra("site", site);
                     intent.putExtra("user", user);
                     startActivityForResult(intent,CODE_EDIT_USER);
                 }else{
                     Toast.makeText(LoginActivity.this, "Select user to edit", Toast.LENGTH_SHORT).show();
                 }
             }
         });
 
         buttonLogin = (Button) findViewById(R.id.buttonLogin);
         buttonLogin.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (selectedPosition != -1){
                     User user = userAdapter.getItem(selectedPosition);
 
                     if (Utils.isEmptyString(user.getKey())){
                         Toast.makeText(LoginActivity.this, "Selected user still don't have voice password", Toast.LENGTH_SHORT).show();
                         return;
                     }
 
                    File tempDir = new File(Utils.getAppDir(LoginActivity.this), String.valueOf(System.currentTimeMillis()));
                     if (!tempDir.exists()) tempDir.mkdir();
                    showRecordDialog(tempDir.getAbsolutePath() + "/speech.wav", user.getKey());
 
                 }else{
                     Toast.makeText(LoginActivity.this, "Select user to login", Toast.LENGTH_SHORT).show();
                 }
 
 
             }
         });
 
         list = (ListView) findViewById(R.id.listUsers);
     }
 
 
     private void showRecordDialog(final String filePath, final String key) {
         final AlertDialog alert;
         AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this)
                 .setTitle("Are you ready?")
                 .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface d, int which) {
 
                         //  String fileName = tempDir.getAbsolutePath() + "/record" + buttonId + ".wav";
                         System.out.println("filePath = " + filePath);
                         extAudioRecorder = ExtAudioRecorder.getInstanse(false);
                         extAudioRecorder.setOutputFile(filePath);
                         extAudioRecorder.prepare();
 
                         new RecordSpeechTask(LoginActivity.this, filePath, key).execute();
                     }
                 })
                 .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         if (extAudioRecorder != null)
                             extAudioRecorder.release();
                         // extAudioRecorder.reset();
                     }
                 })
                 .setMessage("To login you need to speak something for 5 seconds. Click OK when you're ready");
         alert = builder.create();
         alert.show();
     }
 
 
     public class RecordSpeechTask extends AsyncTask<Object, Integer, Boolean> {
 
         private ProgressDialog dialog;
         private String filePath;
         private String key;
 
         public RecordSpeechTask(Context context, String filePath, String key) {
             this.key = key;
             this.filePath = filePath;
             this.dialog = new ProgressDialog(context);
             this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
             this.dialog.setCancelable(false);
             this.dialog.setMax(5000);
 
         }
 
         @Override
         protected void onPreExecute() {
             dialog.setMessage("Speak now...");
             dialog.show();
         }
 
         @Override
         protected Boolean doInBackground(Object... objects) {
             extAudioRecorder.start();
             Log.d("speechpro", "start record");
             long startTime = System.currentTimeMillis();
             while (System.currentTimeMillis() - startTime < 5000) {
                 dialog.setProgress((int) (System.currentTimeMillis() - startTime));
             }
             extAudioRecorder.stop();
             extAudioRecorder.release();
             extAudioRecorder.reset();
             Log.d("speechpro", "stop record");
             return true;
 
         }
 
         @Override
         protected void onPostExecute(Boolean aBoolean) {
             if (dialog.isShowing())
                 dialog.dismiss();
 
             if (!new File(filePath).exists()){
                 Toast.makeText(LoginActivity.this, "Speech file not exists, something wrong...", Toast.LENGTH_SHORT).show();
                 return;
             }
 
              new EnrollVerifyTask(LoginActivity.this).execute(key, filePath);
 
 
         }
 
     }
 
 
 
     private class EnrollVerifyTask extends AsyncTask<String, Void, ResponseResult> {
 
         private ProgressDialog dialog;
         private Context context;
 
         private EnrollVerifyTask(Context context) {
             this.context = context;
             this.dialog = new ProgressDialog(context);
         }
 
         @Override
         protected void onPreExecute() {
 
             if (!Utils.isInternetAvailable(context)){
                 Utils.showMessageDialog(context, "Internet not available.", "You don't have an internet connection, check it and try again.");
                 cancel(true);
                 return;
             }
 
             dialog.setMessage("Upload...");
             dialog.setCanceledOnTouchOutside(false);
             dialog.show();
 
         }
 
         @Override
         protected ResponseResult doInBackground(String... strings) {
             SpeechProClient client = new SpeechProClient();
            InputStream stream = client.executeEnrollVerify("http://voicekey.speechpro-usa.com/avis/vk_api2/enroll_verify.php", "zab", strings[0], strings[1]);
             ResponseParser parser = new ResponseParser();
             return parser.getEnrollResult(stream);
 
         }
 
         @Override
         protected void onPostExecute(ResponseResult result) {
             if (dialog.isShowing())
                 dialog.dismiss();
 
             if (result != null) {
 
                 if (result.getStatus().equals(ResponseResult.Status.OK)){
                     String s = "";
                     if (site == DatabaseAdapter.VK) s = "Vkontakte";
                     if (site == DatabaseAdapter.GMAIL) s = "Gmail";
                     if (site == DatabaseAdapter.FACEBOOK) s = "Facebook";
                     Log.d("speechpro", "try to login to " + s);
 
                     Intent intent = new Intent(LoginActivity.this, WebActivity.class);
                     intent.putExtra("site", site);
                     intent.putExtra("user", userAdapter.getItem(selectedPosition));
                     startActivity(intent);
 
                 } else {
                     Utils.showMessageDialog(LoginActivity.this, "Error", result.getError());
                 }
 
 
 
             } else {
                 Toast.makeText(context, "Something wrong... ", Toast.LENGTH_LONG).show();
             }
 
             //Log.d("speechpro", "Enroll response = " + response);
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == CODE_ADD_USER) {
             if (resultCode == RESULT_OK) {
                 Toast.makeText(LoginActivity.this, "User added successfully", Toast.LENGTH_SHORT).show();
                 selectedPosition = -1;
             }
         }
         if (requestCode == CODE_EDIT_USER) {
             if (resultCode == RESULT_OK) {
                 Toast.makeText(LoginActivity.this, "User edited successfully", Toast.LENGTH_SHORT).show();
                 selectedPosition = -1;
             }
         }
 
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         dbAdapter.open();
         List<User> users = dbAdapter.getAllUsers(site);
         userAdapter = new ArrayAdapter<User>(this, android.R.layout.simple_list_item_single_choice, users);
         list.setAdapter(userAdapter);
         list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
         list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                  selectedPosition=i;
             }
         });
     }
 
     @Override
     protected void onPause() {
         if (dbAdapter != null)
             dbAdapter.close();
 
         super.onPause();
     }
 
 
 
 }
