 package jp.gr.java_conf.neko_daisuki.httpsync;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 public class MainActivity extends Activity {
 
     private static class MalformedHtmlException extends Exception {
     }
 
     private class SynchronizeTask extends AsyncTask<String, Void, Void> {
 
         public Void doInBackground(String... urls) {
             Log.i(LOG_TAG, "Synchronizing started.");
             synchronize(urls[0]);
             Log.i(LOG_TAG, "Synchronizing ended.");
             return null;
         }
 
         private HttpURLConnection connect(String url) {
             URL loc;
             try {
                 loc = new URL(url);
             }
             catch (MalformedURLException e) {
                 String fmt = "Malformed URL: %s: %s";
                 Log.e(LOG_TAG, String.format(fmt, url, e.getMessage()));
                 return null;
             }
             HttpURLConnection conn;
             try {
                 conn = (HttpURLConnection)loc.openConnection();
                 conn.connect();
             }
             catch (IOException e) {
                 String fmt = "Cannot connect to %s: %s";
                 Log.e(LOG_TAG, String.format(fmt, url, e.getMessage()));
                 return null;
             }
 
             return conn;
         }
 
         private String readHtml(String url) {
             HttpURLConnection conn = connect(url);
             if (conn == null) {
                 return null;
             }
 
             InputStream in;
             try {
                 in = conn.getInputStream();
             }
             catch (IOException e) {
                 String fmt = "HttpURLConnection.getInputStream() failed: %s";
                 Log.e(LOG_TAG, fmt.format(e.getMessage()));
                 return null;
             }
             InputStreamReader reader;
             String encoding = "UTF-8";
             try {
                 reader = new InputStreamReader(in, encoding);
             }
             catch (UnsupportedEncodingException e) {
                 String fmt = "Cannot handle encoding %s: %s";
                 Log.e(LOG_TAG, String.format(fmt, encoding, e.getMessage()));
                 return null;
             }
             String html = "";
             BufferedReader bufferedReader = new BufferedReader(reader);
             try {
                 try {
                     String line;
                     while ((line = bufferedReader.readLine()) != null) {
                         html += line;
                     }
                 }
                 finally {
                     bufferedReader.close();
                 }
             }
             catch (IOException e) {
                 String fmt = "Cannot read html: %s";
                 Log.e(LOG_TAG, String.format(fmt, e.getMessage()));
                 return null;
             }
 
             return html;
         }
 
         private List<String> extractLinks(String html) throws MalformedHtmlException {
             List<String> list = new ArrayList<String>();
 
             int end = 0;
             int begin;
             String startMark = "href=\"";
             while ((begin = html.indexOf(startMark, end)) != -1) {
                 int pos = begin + startMark.length();
                 end = html.indexOf("\"", pos);
                 if (end == -1) {
                     throw new MalformedHtmlException();
                 }
                 list.add(html.substring(pos, end));
 
                 end += 1;
             }
 
             return list;
         }
 
         private List<String> listOfLink(String link) {
             String[] extensions = new String[] {
                 ".mp3", ".mp4", ".apk", ".tar", ".xz", ".bz2", ".gzip", ".zip"
             };
             boolean isTarget = false;
             for (int i = 0; (i < extensions.length) && !isTarget; i++) {
                 isTarget = link.endsWith(extensions[i]);
             }
             return isTarget ? Arrays.asList(link) : new ArrayList<String>();
         }
 
         private String[] selectFiles(List<String> links) {
             List<String> list = new ArrayList<String>();
             for (String link: links) {
                 list.addAll(listOfLink(link));
             }
 
            return list.toArray(new String[0]);
         }
 
         private void download(String base, String link, String dir) {
             String url = String.format("%s/%s", base, link);
             HttpURLConnection conn = connect(url);
             if (conn == null) {
                 return;
             }
             String name = new File(link).getName();
             String path = String.format("%s%s%s", dir, File.separator, name);
             if (new File(path).exists()) {
                 String fmt = "Skip: source=%s, destination=%s";
                 Log.i(LOG_TAG, String.format(fmt, url, path));
                 return;
             }
 
             String fmt = "Downloading: source=%s, destination=%s";
             Log.i(LOG_TAG, String.format(fmt, url, path));
 
             try {
                 InputStream in = conn.getInputStream();
                 try {
                     FileOutputStream out = new FileOutputStream(path);
                     try {
                         byte[] buffer = new byte[4096];
                         int nBytes;
                         while ((nBytes = in.read(buffer)) != -1) {
                             out.write(buffer, 0, nBytes);
                         }
                     }
                     finally {
                         out.close();
                     }
                 }
                 finally {
                     in.close();
                 }
             }
             catch (IOException e) {
                 fmt = "Cannot copy. Destination file %s is removed: %s";
                 Log.e(LOG_TAG, String.format(fmt, path, e.getMessage()));
                 new File(path).delete();
             }
 
             fmt = "Downloaded: source=%s, destination=%s";
             Log.i(LOG_TAG, String.format(fmt, url, path));
         }
 
         private void synchronize(String url) {
             // TODO: Must show the error to a user.
             String html = readHtml(url);
             if (html == null) {
                 return;
             }
 
             String[] links;
             try {
                 links = selectFiles(extractLinks(html));
             }
             catch (MalformedHtmlException _) {
                 Log.e(LOG_TAG, "Html is malformed. Skip.");
                 return;
             }
 
             for (String link: links) {
                 download(url, link, "/mnt/sdcard");
             }
         }
     }
 
     private class RunButtonOnClickListener implements View.OnClickListener {
 
         public void onClick(View _) {
             new SynchronizeTask().execute("http://192.168.11.34/tom/apk");
         }
     }
 
     private static final String LOG_TAG = "httpsync";
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         Button runButton = (Button)findViewById(R.id.run_button);
         runButton.setOnClickListener(new RunButtonOnClickListener());
     }
 }
 
 /**
  * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
  */
