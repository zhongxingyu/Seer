 package pc.yugioh;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 
 import pc.yugioh.ArchseriesFragment.OnArchseriesListener;
 
 import ch.boye.httpclientandroidlib.HttpEntity;
 import ch.boye.httpclientandroidlib.HttpResponse;
 import ch.boye.httpclientandroidlib.client.methods.HttpGet;
 import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
 import ch.boye.httpclientandroidlib.util.EntityUtils;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 public class ResultActivity extends Activity implements OnArchseriesListener {
 	
 	private FragmentManager fm;
 	private FragmentTransaction ft;
 	private DefaultHttpClient client;
 	private Fragment[] fragments;
 	private Fragment currentFragment;
 	private Button gallery_button;
 	private Button info_button;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_result);
 		
     	client = new DefaultHttpClient();
     	currentFragment = null;
     	fragments = new Fragment[3];
     	
 		String selection = getIntent().getStringExtra("Selection");
 		if (selection == null) {
 			return;
 		}
 
 		gallery_button = (Button) findViewById(R.id.galleryButton);
 		info_button = (Button) findViewById(R.id.infoButton);
 
     	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo ni = cm.getActiveNetworkInfo();
 		
 		if (ni != null && ni.isConnected()) {
 			try {
 				new GetRDFTask().execute(client, selection);
 			} catch (Exception e) {
 				Log.e("RDF", "GetRDFTask error", e);
 			}
 		} else {
 			//couldn't connect
 		}
 	}
 	
 	private void setFragment(int i) {
         fm = getFragmentManager();
     	ft = fm.beginTransaction();
     	if (currentFragment == null) {
     		ft.add(R.id.resultLayout, fragments[i]);
     	} else {
     		ft.replace(R.id.resultLayout, fragments[i]);
     		ft.addToBackStack(null);
     	}
     	currentFragment = fragments[i];
     	ft.commit();
 	}
 	
 	private void setupArchseries(String rdf) {
 		Bundle bundle = new Bundle();
 		bundle.putString("Archseries", rdf);
 		fragments[0] = new ArchseriesFragment();
 		fragments[0].setArguments(bundle);
 		setFragment(0);
 	}
 	
 	private void setupFragments(String rdf, String image_name, int card_type) {
 		Bundle bundle = new Bundle();
 		bundle.putString("rdf", rdf);
 		bundle.putString("gallery_name", image_name);
 		bundle.putInt("card_type", card_type);
 		fragments[1] = new GalleryFragment();
 		fragments[1].setArguments(bundle);
 		fragments[2] = new InfoFragment();
 		fragments[2].setArguments(bundle);
 		setFragment(1);
 	}
 	
 	private class GetRDFTask extends AsyncTask<Object, Void, String> {
 
 		private String selection;
 		
 		@Override
 		protected String doInBackground(Object... params) {
 			DefaultHttpClient client = (DefaultHttpClient) params[0];
			selection = (String) ((String) params[1]).replaceAll(" ", "_").replaceAll("\"", "%22");
 			String result = null;
 			String url = "http://yugioh.wikia.com/wiki/Special:ExportRDF/" + selection;
 			InputStream is = null;
 			try {
             	HttpGet get = new HttpGet(url);
 				HttpResponse response = client.execute(get);
 				HttpEntity entity = response.getEntity();
 				
 				if (entity != null) {
 					is = entity.getContent();
 					//convertStreamToString handles closing the is
 					result = convertStreamToString(is);
 				    EntityUtils.consume(entity);
 				} else {
 					return null;
 				}
 				
 			} catch (Exception e) {
 				Log.e("ResultActivity", "Http connection error", e);
 				return null;
 			} finally {
 				client.getConnectionManager().shutdown();
 			}
 			return result;
 		}
 
 		private String getCardImageUrl(String line) {
 			int start = "<property:Card_Image rdf:resource=\"&wiki;File-3A".length();
 			int end = line.indexOf("\"/>", start);
 			return (line.substring(start+2, end)).replaceAll("2D", "").replaceAll("-27", "'");
 		}
 		
 		@Override
 		protected void onPostExecute(String result) {
 			BufferedReader rdf = new BufferedReader(new StringReader(result));
 			String line, image_name = null;
 			boolean archseries = false;
 			int card_type = -1;
 			try {
 				while ((line = rdf.readLine()) != null) {
 					if (line.contains("Card_Image")) {
 						image_name = getCardImageUrl(line);
 						if (card_type == 1 || card_type == 2) {
 							break;
 						}
 					}
 					if (line.contains("&wiki;Monster_Card")) {
 						card_type = 0;
 						//card_image should already have been found
 						break;
 					} else if (line.contains("&wiki;Spell_Card")) {
 						card_type = 1;
 						//continue for card_image
 					} else if (line.contains("&wiki;Trap_Card")) {
 						card_type = 2;
 						//continue for card_image
 					} else if (line.contains("&wiki;Category-3ASeries")) {
 						setupArchseries(result);
 						archseries = true;
 						break;
 					} else if (line.contains("&wiki;Category-3AArchetypes")) {
 						setupArchseries(result);
 						archseries = true;
 						break;
 					}
 				}
 				if (card_type == -1 && !archseries) {
 					//not a card or archseries? prompt user for browser?
 					return;
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				Log.e("ResultActivity", "IO error", e);
 			}
 			if (!archseries) {
 				setupFragments(result, image_name, card_type);
 				gallery_button.setOnClickListener(new View.OnClickListener() {
 		            public void onClick(View v) {
 		            	if (currentFragment != fragments[0] && currentFragment != fragments[1]) {
 		            		setFragment(1);
 		            	}
 		            }
 		        });
 				
 				info_button.setOnClickListener(new View.OnClickListener() {
 		            public void onClick(View v) {
 		            	if (currentFragment != fragments[0] && currentFragment != fragments[2]) {
 		            		setFragment(2);
 		            	}
 		            }
 		        });
 			}
 		}
 		
 		
 	}
 
     private static String convertStreamToString(InputStream is) {
         /*
          * To convert the InputStream to String we use the BufferedReader.readLine()
          * method. We iterate until the BufferedReader return null which means
          * there's no more data to read. Each line will appended to a StringBuilder
          * and returned as String.
          */
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         StringBuilder sb = new StringBuilder();
  
         String line = null;
         try {
             while ((line = reader.readLine()) != null) {
                 sb.append(line + "\n");
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 is.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return sb.toString();
     }
     
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_result, menu);
 		return true;
 	}
 
 	@Override
 	public void onSelection(String selection) {
 		client = new DefaultHttpClient();
     	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo ni = cm.getActiveNetworkInfo();
 		
 		if (ni != null && ni.isConnected()) {
 			try {
 				new GetRDFTask().execute(client, selection);
 			} catch (Exception e) {
 				Log.e("RDF", "GetRDFTask error", e);
 			}
 		} else {
 			//couldn't connect
 		}
 	}
 
 }
