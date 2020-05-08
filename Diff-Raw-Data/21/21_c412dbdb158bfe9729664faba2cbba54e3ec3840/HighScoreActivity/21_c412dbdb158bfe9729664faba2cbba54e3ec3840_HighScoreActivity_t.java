 package games.pack.protap;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class HighScoreActivity extends Activity {
     private TableLayout mHighScoreTable;
     private static final String REACTION_URL = "http://pro-tap.appspot.com/leaderboard?type=reaction";
     private static final String BOXING_URL = "http://pro-tap.appspot.com/leaderboard?type=boxing";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.high_score_layout);
         mHighScoreTable = (TableLayout) findViewById(R.id.highScoreTable);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
         chooseBoard(findViewById(R.id.rG));
     }
 
     private void addHeaders() {
         final TableRow tr = new TableRow(this);
         mHighScoreTable.addView(tr);
 
         final TextView rankText = new TextView(this);
         rankText.setText("Rank");
         rankText.setTextColor(Color.WHITE);
         rankText.setGravity(Gravity.CENTER);
         final int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20,
                 getResources().getDisplayMetrics());
         rankText.setPadding(0, 0, px, 0);
         tr.addView(rankText);
 
         final TextView nameText = new TextView(this);
         nameText.setText("Name");
         nameText.setTextColor(Color.WHITE);
         tr.addView(nameText);
 
         final TextView scoreText = new TextView(this);
         scoreText.setText("Score");
         scoreText.setTextColor(Color.WHITE);
         scoreText.setGravity(Gravity.CENTER);
         tr.addView(scoreText);
     }
 
     public void chooseBoard(final View view) {
         String url;
         switch (view.getId()) {
             case R.id.bG:
                 url = BOXING_URL;
                 findViewById(R.id.rG).setEnabled(true);
                 break;
             case R.id.rG:
                 url = REACTION_URL;
                 findViewById(R.id.bG).setEnabled(true);
                 break;
             default:
                 url = REACTION_URL;
                 break;
         }
         new HighScoreDownload().execute(url);
         view.setEnabled(false);
 
     }
 
     public class HighScoreDownload extends AsyncTask<String, Void, HttpResponse> {
         private final ProgressDialog dialog = new ProgressDialog(HighScoreActivity.this);
 
         protected void onPreExecute() {
             mHighScoreTable.removeAllViewsInLayout();
             addHeaders();
             this.dialog.setMessage("Downloading Leaderboard...");
             this.dialog.show();
         }
 
         @Override
        protected HttpResponse doInBackground(final String... params) {
             final HttpClient client = new DefaultHttpClient();
             try {
                 final HttpGet request = new HttpGet(params[0]);
                 try {
                     return client.execute(request);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             } catch (IllegalArgumentException e1) {
                 e1.printStackTrace();
             }
             return null;
         }
 
         protected void onPostExecute(final HttpResponse response) {
             if (response != null) {
                 final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                         .newInstance();
                 InputStream in;
                 NodeList ranks = null, names = null, scores = null;
                 try {
                     final DocumentBuilder docBuillder = docBuilderFactory.newDocumentBuilder();
                     in = response.getEntity().getContent();
                     final Document doc = docBuillder.parse(in);
                     ranks = doc.getElementsByTagName("rank");
                     names = doc.getElementsByTagName("name");
                     scores = doc.getElementsByTagName("score");
                 } catch (ParserConfigurationException e) {
                     e.printStackTrace();
                 } catch (SAXException e) {
                     e.printStackTrace();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
 
                 for (int i = 0; i < names.getLength(); i++) {
                     final TableRow tr = new TableRow(HighScoreActivity.this);
                     mHighScoreTable.addView(tr);
 
                     final TextView rankText = new TextView(HighScoreActivity.this);
                     rankText.setText(ranks.item(i).getFirstChild().getNodeValue());
                     rankText.setTextColor(Color.WHITE);
                     final int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20,
                             getResources().getDisplayMetrics());
                     rankText.setPadding(0, 0, px, 0);
                     rankText.setGravity(Gravity.CENTER);
                     tr.addView(rankText);
 
                     final TextView nameText = new TextView(HighScoreActivity.this);
                     final Node n = names.item(i).getFirstChild();
                     if (n == null) {
                         nameText.setText("");
                     } else {
                         nameText.setText(n.getNodeValue());
                     }
                     nameText.setTextColor(Color.WHITE);
                     tr.addView(nameText);
 
                     final TextView scoreText = new TextView(HighScoreActivity.this);
                     scoreText.setText(scores.item(i).getFirstChild().getNodeValue());
                     scoreText.setTextColor(Color.WHITE);
                     scoreText.setGravity(Gravity.CENTER);
                     tr.addView(scoreText);
                 } // for
                this.dialog.dismiss();
             } // if response not null
         } // onPostExecute
     } // class HighScoreDownload
 
 } // class HighScoreActivity
