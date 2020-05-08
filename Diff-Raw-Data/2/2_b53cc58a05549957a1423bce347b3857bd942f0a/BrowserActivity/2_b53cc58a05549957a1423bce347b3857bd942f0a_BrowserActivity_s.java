 package es.jiayu.jiayuid;
 
 import android.app.Activity;
 import android.app.DownloadManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.webkit.DownloadListener;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Toast;
 
 import java.io.File;
 import java.util.List;
 
 public class BrowserActivity extends Activity {
     Resources res;
     PackageManager pm = null;
     String urlDestino = "";
 
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_browser);
         res = this.getResources();
         pm = this.getPackageManager();
         isDownloadManagerAvailable(getBaseContext());
         Intent intent = getIntent();
         String modelo = intent.getExtras().getString("modelo");
         String tipo = intent.getExtras().getString("tipo");
         WebView descargas = (WebView) findViewById(R.id.webView1);
         descargas.setWebViewClient(new JiayuWebViewClient());
         descargas.setDownloadListener(new JiayuDownloadListener());
 
         if("drivers".equals(tipo)){
             descargas.loadUrl("http://www.jiayu.es/soporte/apptools.php");
         }else if("downloads".equals(tipo)){
             descargas.loadUrl("http://www.jiayu.es/soporte/appsoft.php?jiayu=" + modelo);
         }
 
     }
    class JiayuDownloadListener implements DownloadListener {
 
        public void onDownloadStart(String s, String s2, String s3, String s4, long l) {
 
        }
 
    }
    class JiayuWebViewClient extends WebViewClient {
 
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
             urlDestino = url;
             if (urlDestino.lastIndexOf("/desarrollo/") != -1) {
                 try {
                     String nombreFichero="";
                     nombreFichero=urlDestino.split("/")[urlDestino.split("/").length-1];
                     DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlDestino));
                     request.setDescription(nombreFichero);
                     request.setTitle(nombreFichero);
                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                         request.allowScanningByMediaScanner();
                         request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                         if(".apk".equals(nombreFichero.substring(nombreFichero.length()-4,nombreFichero.length()).toLowerCase())){
                             request.setMimeType("application/vnd.android.package-archive");
                             if(nombreFichero.indexOf("Jiayu.apk")==-1){
                                 try {
                                     new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Jiayu.apk").delete();
                                 }catch(Exception e){
                                    
                                 }
 
                             }
                         }
 
                     }
                     request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nombreFichero);
 
                     DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                     Toast.makeText(getBaseContext(), getResources().getString(R.string.iniciandoDescarga)+" "+nombreFichero, Toast.LENGTH_SHORT).show();
                     manager.enqueue(request);
                 } catch (Exception e) {
                     Toast.makeText(getBaseContext(), getResources().getString(R.string.genericError), Toast.LENGTH_SHORT).show();
                 }
                 return true;
             } else {
                 Uri uri = Uri.parse(urlDestino);
                 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                 startActivity(intent);
                 return true;
             }
         }
     }
     public static boolean isDownloadManagerAvailable(Context context) {
         try {
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                 return false;
             }
             Intent intent = new Intent(Intent.ACTION_MAIN);
             intent.addCategory(Intent.CATEGORY_LAUNCHER);
             intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
             List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                     PackageManager.MATCH_DEFAULT_ONLY);
             return list.size() > 0;
         } catch (Exception e) {
             return false;
         }
     }
 }
