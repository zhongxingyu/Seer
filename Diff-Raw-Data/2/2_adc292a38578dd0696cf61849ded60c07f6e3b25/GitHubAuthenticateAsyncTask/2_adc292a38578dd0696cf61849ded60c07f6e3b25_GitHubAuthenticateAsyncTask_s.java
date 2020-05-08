 package ru.spb.cupchinolabs.githubclient.github;
 
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.util.Base64;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import ru.spb.cupchinolabs.githubclient.ApplicationContext;
 import ru.spb.cupchinolabs.githubclient.R;
 import ru.spb.cupchinolabs.githubclient.action.LoginActivity;
 import ru.spb.cupchinolabs.githubclient.model.Repository;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: VladimirK
  * Date: 24.02.13
  * Time: 19:48
  */
 public class GitHubAuthenticateAsyncTask extends AsyncTask<Void, Void, String> {
 
     private LoginActivity loginActivity;
     private final String name;
     private final String password;
     private ProgressDialog pbarDialog;
 
     public GitHubAuthenticateAsyncTask(LoginActivity loginActivity, String name, String password) {
         this.loginActivity = loginActivity;
         this.name = name;
         this.password = password;
     }
 
     @Override
     protected String doInBackground(Void ... objects) {
         HttpURLConnection conn = null;
         try {
             URL url = new URL("https://api.github.com/users/" + name + "/repos");
             conn = (HttpURLConnection) url.openConnection();
             conn.setReadTimeout(10000);
             conn.setConnectTimeout(15000);
             conn.setRequestMethod("GET");
             conn.setDoInput(true);
             conn.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((name + ":" + password).getBytes(), Base64.DEFAULT));
             conn.setRequestProperty("Content-Type", "application/json");
             conn.setRequestProperty("Accept", "application/json");
             conn.connect();
 
             int response = conn.getResponseCode();
 
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(conn.getInputStream()));
             StringBuilder sb = new StringBuilder();
 
             String line;
             while ((line = reader.readLine()) != null) {
                 sb.append(line + System.getProperty("line.separator"));
             }
 
             JSONArray jsonRepos = new JSONArray(sb.toString());
             List<Repository> repos = new ArrayList<>();
 
             for (int i = 0; i < jsonRepos.length(); i++) {
                 JSONObject jsonRepo = (JSONObject) jsonRepos.get(i);
                 Repository repository = new Repository();
                 repository.setName((String) jsonRepo.get("name"));
                 repository.setDescription((String) jsonRepo.get("description"));
                 repository.setWatchersCount((Integer) jsonRepo.get("watchers_count"));
                 repository.setForksCount((Integer) jsonRepo.get("forks_count"));
                 repos.add(repository);
             }
 
             ApplicationContext.getInstance().getUser().setRepoList(repos);
 
             return String.valueOf(response);
         } catch (IOException e) {
             e.printStackTrace();
             return e.getMessage();
         } catch (JSONException e) {
             e.printStackTrace();
             return "github replied with incorrect json -> " + e.getMessage();
         } finally {
             if (conn != null){
                 conn.disconnect();
             }
         }
     }
 
     @Override
     protected void onPreExecute() {
         super.onPreExecute();
 
         //TODO block orientation change
 
         //TODO wait ~2 secs before showing a dialog
         pbarDialog = new ProgressDialog(loginActivity);
         pbarDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         pbarDialog.setMessage(loginActivity.getString(R.string.login_authentication_progress_message));
         pbarDialog.setCancelable(false);
         pbarDialog.setIndeterminate(true);
         pbarDialog.show();
     }
 
     @Override
     protected void onPostExecute(String errorMessage) {
 
         pbarDialog.dismiss();
 
         //TODO unblock orientation change
 
         if (!"200".equals(errorMessage)){
             loginActivity.onGitHubAuthenticationError(errorMessage);
         } else {
             loginActivity.onGitHubAuthenticateSuccess();
         }
 
         loginActivity = null;
         pbarDialog = null;
     }
 }
 
 
