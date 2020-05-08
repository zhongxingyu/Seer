 package fr.gcastel.freeboxV6GeekIncDownloader.services;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Le service de tlchargement via freebox
  * 
  * @author Gerben
  */
 public class FreeboxDownloaderService extends AsyncTask<String, Void, Void> {
 	private static final String TAG = "FreeboxDownloaderService";
   private final String urlFreebox;
   private Context zeContext;
   private boolean echec = false;
   private Dialog dialog;
   private String alertDialogMessage = null;
   private boolean bypassTraitement = false;
   
   private enum DialogEnCours {
     NONE,
     PROGRESS,
     ALERT;
   }
   
   private DialogEnCours dialogueEnCours = DialogEnCours.NONE;
   
   /**
    * Instanciation
    * 
    * @param inUrlFreebox l'url  utiliser pour se connecter  la freebox
    */
   public FreeboxDownloaderService(String inUrlFreebox, Context context, ProgressDialog inDialog) {
     urlFreebox = inUrlFreebox;
     zeContext = context;
     dialog = inDialog;
     dialogueEnCours = DialogEnCours.PROGRESS;
   }
 
   public boolean isConnectedViaWifi() {
     boolean result = false;
     if (zeContext != null) {
       ConnectivityManager cm = (ConnectivityManager) zeContext.getSystemService(Context.CONNECTIVITY_SERVICE);
       NetworkInfo info = cm.getActiveNetworkInfo();
       result = (info != null) && (info.getType() == ConnectivityManager.TYPE_WIFI); 
     }
     return result;
   }
   
   
   private String loginFreebox(String password) throws UnsupportedEncodingException, ClientProtocolException, IOException {
     String result = "";
 
     // Prparation des paramtres
     HttpPost postReq = new HttpPost(urlFreebox + "/login.php");
     List<NameValuePair> parametres = new ArrayList<NameValuePair>();
     parametres.add(new BasicNameValuePair("login", "freebox"));
     parametres.add(new BasicNameValuePair("passwd", password));
     postReq.setEntity(new UrlEncodedFormEntity(parametres));
     
     // Envoi de la requte
     HttpParams httpParameters = new BasicHttpParams();
     
     // Mise en place de timeouts
     int timeoutConnection = 5000;
     HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
     int timeoutSocket = 5000;
     HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
     
     HttpClient httpclient = new DefaultHttpClient(httpParameters);
     HttpParams params = httpclient.getParams();
     HttpClientParams.setRedirecting(params, false); 
 
     HttpResponse response= httpclient.execute(postReq);
 
     // Ok ? (302 = moved = redirection)
     if (response.getStatusLine().getStatusCode() == 302) {
       Header cookie = response.getFirstHeader("Set-Cookie");      result = cookie.getValue();
       
       // Extraction du cookie FBXSID
       result = result.substring(result.indexOf("FBXSID=\""), result.indexOf("\";")+1);
       Log.d(TAG, "Cookie = " + result);
     } else {
       Log.d(TAG, "Erreur d'authentification - statusCode = " + response.getStatusLine().getStatusCode()  + " - reason = " + response.getStatusLine().getReasonPhrase());
       prepareAlertDialog("Erreur d'authentification");
     }
     
     return result;
   }
   
   private void prepareAlertDialog(String message) {
     dialogueEnCours = DialogEnCours.ALERT;
     alertDialogMessage = message;
   }
 
   private void showAlertDialog() {
     AlertDialog.Builder alertbox = new AlertDialog.Builder(zeContext);
     alertbox.setMessage(alertDialogMessage);
     alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface arg0, int arg1) {
          dialogueEnCours = DialogEnCours.NONE;
          return;
       }
     });
     alertbox.show();
   }
 
   
   private void launchDownload(String cookie, String url) throws UnsupportedEncodingException, ClientProtocolException, IOException {
     // Prparation des paramtres
     HttpPost postReq = new HttpPost(urlFreebox + "/download.cgi");
     List<NameValuePair> parametres = new ArrayList<NameValuePair>();
     parametres.add(new BasicNameValuePair("url", url));
     parametres.add(new BasicNameValuePair("user", "freebox"));
     parametres.add(new BasicNameValuePair("method", "download.http_add"));
     postReq.setEntity(new UrlEncodedFormEntity(parametres));
     
     // Mise en place des headers
     postReq.setHeader("Cookie", cookie + ";");
     postReq.setHeader("Referer", "http://mafreebox.freebox.fr/download.php");
     
     // Envoi de la requte
     HttpParams httpParameters = new BasicHttpParams();
     
     // Mise en place de timeouts
     int timeoutConnection = 5000;
     HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
     int timeoutSocket = 5000;
     HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
     HttpClient httpclient = new DefaultHttpClient(httpParameters);
     HttpParams params = httpclient.getParams();
     HttpClientParams.setRedirecting(params, false); 
 
     HttpResponse response= httpclient.execute(postReq);
 
     // Ok ? (302 = moved = redirection)
     if (response.getStatusLine().getStatusCode() != 302) {
       Log.d(TAG, "Erreur lors du lancement du tlchargement - statusCode = " + response.getStatusLine().getStatusCode()  + " - reason = " + response.getStatusLine().getReasonPhrase());
       prepareAlertDialog("Erreur lors du lancement du tlchargement.");
     }  	
   }
   
   
   @Override
   protected Void doInBackground(String... params) {
     try {
       if (!bypassTraitement) {
         String cookie = loginFreebox(params[1]);
        if (alertDialogMessage != null) {  
           launchDownload(cookie, params[0]);
         }
       }
     } catch(Exception e) {
       echec = true;
     }
     return null;
   }
 
 
   @Override
   protected void onPreExecute() {
     super.onPreExecute();
     if (!isConnectedViaWifi()) {
       Toast.makeText(zeContext, "Vous devez tre connect en Wifi pour accder  la freebox", Toast.LENGTH_SHORT).show();
       bypassTraitement = true;
     } else {
       if (dialog != null) {
         dialog.show();
       }
     }
   }
 
   @Override
   protected void onPostExecute(Void result) {
     super.onPostExecute(result);
     dialog.hide();
     dialogueEnCours = DialogEnCours.NONE;
 
     if (alertDialogMessage != null) {  
       showAlertDialog();
     }
     
     if (!bypassTraitement) {
       if (echec) {
         Toast.makeText(zeContext, "Impossible de se connecter  la freebox", Toast.LENGTH_SHORT).show();
       } else {
         Toast.makeText(zeContext, "Tlchargement lanc !", Toast.LENGTH_SHORT).show();
       }
     }
   }
   
   public void attach(Context inContext) {
     zeContext = inContext;
     switch(dialogueEnCours) {
     case PROGRESS :
       dialog = new ProgressDialog(zeContext);
       dialog.setCancelable(true);
       ((ProgressDialog)dialog).setMessage("Connexion  la freebox");
       ((ProgressDialog)dialog).setProgressStyle(ProgressDialog.STYLE_SPINNER);
       dialog.show();
       break;
     case ALERT :
       prepareAlertDialog(alertDialogMessage);
       showAlertDialog();      
       break;
     case NONE:
     default:
       break;
     }
   }
   
   public void detach() {
     zeContext = null;
     dialog = null;
   }
 }
