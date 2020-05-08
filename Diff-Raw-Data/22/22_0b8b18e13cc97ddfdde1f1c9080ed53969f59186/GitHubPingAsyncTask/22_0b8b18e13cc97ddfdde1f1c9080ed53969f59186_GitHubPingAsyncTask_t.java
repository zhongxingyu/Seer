 package ru.spb.cupchinolabs.githubclient.github;
 
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import ru.spb.cupchinolabs.githubclient.R;
 import ru.spb.cupchinolabs.githubclient.action.LoginActivity;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 /**
  * Created with IntelliJ IDEA.
  * User: VladimirK
  * Date: 24.02.13
  * Time: 19:01
  */
 public class GitHubPingAsyncTask extends AsyncTask<Void, Void, String> {
 
     private LoginActivity loginActivity;
     private ProgressDialog pbarDialog;
 
     public GitHubPingAsyncTask(LoginActivity loginActivity) {
         this.loginActivity = loginActivity;
     }
 
     @Override
     protected String doInBackground(Void... objects) {
         HttpURLConnection conn = null;
         try {
             URL url = new URL("https://api.github.com/");
             conn = (HttpURLConnection) url.openConnection();
             conn.setReadTimeout(10000);
             conn.setConnectTimeout(15000);
             conn.setRequestMethod("GET");
             conn.setDoInput(true);
             conn.connect();
             int response = conn.getResponseCode();
             System.out.println("The response is: " + response);
             return String.valueOf(response);
         } catch (IOException e) {
             e.printStackTrace();
             return e.getMessage();
         } finally {
             if (conn != null){
                 conn.disconnect();
             }
         }
     }
 
     @Override
     protected void onPreExecute() {
         super.onPreExecute();
         //TODO wait ~2 secs before showing a dialog
         pbarDialog = new ProgressDialog(loginActivity);
         pbarDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pbarDialog.setMessage(loginActivity.getString(R.string.login_githubping_progress_message));
         pbarDialog.setCancelable(false);
         pbarDialog.setIndeterminate(true);
         pbarDialog.show();
 
     }
 
     @Override
     protected void onPostExecute(String errorMessage) {
 
         pbarDialog.dismiss();
 
         if (!"200".equals(errorMessage)){
             loginActivity.onGitHubPingError(errorMessage);
         }
     }
 }
