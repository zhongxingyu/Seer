 package net.hisme.masaki.img_collector;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.TextView;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.ArrayAdapter;
 import android.widget.AdapterView;
 
 import java.io.PrintWriter;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 
 import net.hisme.masaki.Access2ch;
 
 public class ImageCollector extends Activity {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		Button button1 = (Button) findViewById(R.id.Button01);
 		Button button2 = (Button) findViewById(R.id.Button02);
 
 		button1.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				openBoard("venus.bbspink.com", "megami");
 			}
 		});
 		button2.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				openBoard("yomi.bbspink.com", "neet4pink");
 			}
 		});
 	}
 
 	private void openBoard(String host, String board) {
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				R.layout.thread);
 		final ArrayList<String[]> threads = Access2ch.threads(host, board);
 		for (String[] thread : threads) {
 			adapter.add(thread[1]);
 		}
 		ListView list = (ListView) findViewById(R.id.ListView01);
 		list.setAdapter(adapter);
 		final String _host = host;
 		final String _board = board;
 		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position,
 					long id) {
 				TextView text = (TextView) findViewById(R.id.TextView01);
				text.setText("send request...");
 				postAddRequest(_host, _board, threads.get(position)[0]);
 			}
 		});
 	}
 
 	private void postAddRequest(String host, String board, String thread) {
 		TextView text = (TextView) findViewById(R.id.TextView01);
 		String thread_uri = "http://" + host + "/test/read.cgi/" + board + "/"
 				+ thread + "/";
 		try {
 			URL uri = new URL(
 					"http://hisme.net/~masaki/img_collector/_add_thread.php");
 			HttpURLConnection http = (HttpURLConnection) uri.openConnection();
 			http.setRequestMethod("POST");
 			http.setDoOutput(true);
 			http.setDoInput(true);
 			PrintWriter writer = new PrintWriter(http.getOutputStream());
 
 			writer.print("uri=" + thread_uri);
 			writer.flush();
 			writer.close();
 
 			BufferedReader reader = new BufferedReader(new InputStreamReader(http
 					.getInputStream()));
 			text.setText(reader.readLine() + " : " + thread_uri);
 			reader.close();
 
 			http.disconnect();
 		} catch (java.net.MalformedURLException e) {
 			// return e.toString();
 		} catch (java.io.IOException e) {
 			// return e.toString();
 		} catch (java.lang.IllegalStateException e) {
 			// return e.toString();
 		} catch (NullPointerException e) {
 			// return e.toString();
 		}
 	}
 }
