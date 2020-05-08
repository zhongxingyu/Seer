 package net.hisme.masaki.img_collector;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
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
 import android.util.Log;
 
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
				openBoard("kilauea.bbspink.com", "megami");
 			}
 		});
 		button2.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
				openBoard("pele.bbspink.com", "neet4pink");
 			}
 		});
 	}
 
 	private void openBoard(final String host, final String board) {
 		TextView text = (TextView) findViewById(R.id.TextView01);
 		text.setText("Open Board...");
 		final Handler handler = new Handler();
 		new Thread() {
 			public void run() {
 				final ArrayList<String[]> threads = Access2ch.threads(host, board);
 				handler.post(new Runnable() {
 					public void run() {
 						ImageCollector.this.displayThreadList(host, board, threads);
 					}
 				});
 			}
 		}.start();
 	}
 
 	private void displayThreadList(final String host, final String board,
 			final ArrayList<String[]> threads) {
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
 				ImageCollector.this, R.layout.thread);
 		for (String[] thread : threads) {
 			adapter.add(thread[1]);
 		}
 		ListView list = (ListView) findViewById(R.id.ListView01);
 		list.setAdapter(adapter);
 		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position,
 					long id) {
 				postAddRequest(host, board, threads.get(position)[0]);
 			}
 		});
 	}
 
 	private void postAddRequest(final String host, final String board,
 			final String thread) {
 		TextView text = (TextView) findViewById(R.id.TextView01);
 		text.setText("send request...");
 		final Handler handler = new Handler();
 		new Thread() {
 			public void run() {
 				final String thread_uri = "http://" + host + "/test/read.cgi/" + board
 						+ "/" + thread + "/";
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
 					final String post_result = reader.readLine();
 					handler.post(new Runnable() {
 						public void run() {
 							TextView text = (TextView) findViewById(R.id.TextView01);
 							text.setText(post_result + " : " + thread_uri);
 						}
 					});
 					reader.close();
 
 					http.disconnect();
 				} catch (java.net.MalformedURLException e) {
 					ImageCollector.log(e.toString());
 				} catch (java.io.IOException e) {
 					ImageCollector.log(e.toString());
 				} catch (java.lang.IllegalStateException e) {
 					ImageCollector.log(e.toString());
 				} catch (NullPointerException e) {
 					ImageCollector.log(e.toString());
 				} catch (Exception e) {
 					ImageCollector.log(e.toString());
 				}
 			}
 		}.start();
 	}
 
 	static void log(String str) {
 		Log.d("[ImageCollector]", str);
 	}
 }
