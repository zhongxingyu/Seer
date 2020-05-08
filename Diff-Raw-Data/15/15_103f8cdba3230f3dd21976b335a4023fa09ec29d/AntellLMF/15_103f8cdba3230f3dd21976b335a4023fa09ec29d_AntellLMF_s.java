 package com.honkasalo.antelllmf;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 public class AntellLMF extends Activity {
 	public static Context context;
 	private TextView text;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         context = this;
         
         setContentView(R.layout.activity_antell_lmf);
         setTitle("AntellLMF Weekly Menu");
         
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
         Log.d("PREF:LANG",settings.getString("foodMenuLanguage","No Language set"));
 
         text = (TextView) findViewById(R.id.foodMenu);
 
         // Fetch menu on background
         DownloadMenuTask task = new DownloadMenuTask();
         task.execute();
     };
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_antell_lmf, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
     		case R.id.menu_reload:
     			DownloadMenuTask task = new DownloadMenuTask();
     	        task.execute();
     	        return true;
     		case R.id.menu_settings:
     			Intent settings = new Intent(AntellLMF.this, MainPreferenceActivity.class);
     			startActivity(settings);
     			return true;
     		case R.id.menu_about:
     			Intent about = new Intent(AntellLMF.this, MainAboutActivity.class);
     			startActivity(about);
     			return true;
     		default:
     	}
     	return super.onOptionsItemSelected(item);
     }
     
     /* Extract iFrame url from main web page */
     public static String getFrameUrl(String url) {
 
         String frameUrl = "";
         try {
             Document doc = Jsoup.connect(url).timeout(10*1000).get();
 
             Element frame = doc.select("frame").first();
             String framesrc = frame.attr("src");
 
             // Remove possible leading dots from extracted url
             frameUrl = framesrc.replaceAll("\\.{2}", "");
             
         } catch (Exception error) {
             error.printStackTrace();
         }
         return frameUrl;
     }
     
     /* Get days menu and return as a string*/
     public FoodMenu getDaysMenu(String url) {
         FoodMenu menu = new FoodMenu();
     	Document doc=null;
         try {
         	
         	//Log.d("URL",url);
 
         	boolean connection_established=false;
         	for (int tries = 1; tries<5 && !connection_established; tries++) {
         	    try {
         	    	doc = Jsoup.connect(url).timeout(100*1000).get();
                     //Log.d("CONNECTION", "Connected and fetched page.");
                     connection_established=true;
         	    } catch (Exception ex) {
         	    	ex.printStackTrace();
         	    } 
         	        	    
         	}
         	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(AntellLMF.context);
     		String language = settings.getString("foodMenuLanguage","");
 
             // Select a table according to week day number
     		menu.setWeekTitle(getMenuTitle(doc.select("div:containsOwn(Lounaslista)").first()));
     		if (language.equals("Finnish")) {
     			menu.setMonday("Maanantai\n\n"+getDailyMenu(doc.select("table.lunchTable2").get(0)));
     			menu.setTuesday("Tiistai\n\n"+getDailyMenu(doc.select("table.lunchTable2").get(1)));
     			menu.setWednesday("Keskiviikko\n\n"+getDailyMenu(doc.select("table.lunchTable2").get(2)));
     			menu.setThursday("Torstai\n\n"+getDailyMenu(doc.select("table.lunchTable2").get(3)));
     			menu.setFriday("Perjantai\n\n"+getDailyMenu(doc.select("table.lunchTable2").get(4)));
     		} else {
     			menu.setMonday("Monday\n\n"+getDailyMenu(doc.select("table.lunchTable2").get(0)));
     			menu.setTuesday("Tuesday\n\n"+getDailyMenu(doc.select("table.lunchTable2").get(1)));
     			menu.setWednesday("Wednesday\n\n"+getDailyMenu(doc.select("table.lunchTable2").get(2)));
     			menu.setThursday("Thursday\n\n"+getDailyMenu(doc.select("table.lunchTable2").get(3)));
     			menu.setFriday("Friday\n\n"+getDailyMenu(doc.select("table.lunchTable2").get(4)));
     		};
             menu.setWeeksSpecials(getWeeklySpecial(doc.select("div:containsOwn(Week)").first()));
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return menu;
     }
     
     public static String getMenuTitle(Element table) {
     	StringBuilder sb = new StringBuilder();
     	sb.append(table.text().split("Lounaslista ")[1]+"\n");
     	//Log.d("DATA",""+table.text());
     	return sb.toString();
     }
 
 	public static String getDailyMenu(Element table) {
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(AntellLMF.context);
 		String language = settings.getString("foodMenuLanguage","");
 		StringBuilder sb = new StringBuilder();
 		Pattern p = Pattern.compile("(?<!^|/)(M|VL|L|G)");
 		for (Element row : table.select("tr")) {
 	        Element tds = row.select("td").first();
 	        Matcher m = p.matcher(tds.text());
 	        StringBuilder sb2 = new StringBuilder();
 			while (m.find()) {
 				sb2.append(m.group());
 			}
 	        if (language.equals("Finnish")) {
 	        	sb.append(tds.text().split("/| \\(")[0] + " " + sb2.toString() + "\n");
 	        } else {
 	        	sb.append(tds.text().split("/| \\(")[1] + " " + sb2.toString() + "\n");
 	        };
 	        //Log.d("DATA",""+tds.text());
 	    }
 	    return sb.toString();
 	}
 	
 	public static String getWeeklySpecial(Element table) {
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(AntellLMF.context);
 		String language = settings.getString("foodMenuLanguage","");
 		StringBuilder sb = new StringBuilder();
 		StringBuilder sb2 = new StringBuilder();
 		StringBuilder sb3 = new StringBuilder();
 		Pattern p = Pattern.compile("(?<!^|/)(M|VL|L|G)");
		Matcher m = p.matcher(table.text().split("Week Wok")[0]);
 		while (m.find()) {
 			sb2.append(m.group());
 		}
		m = p.matcher(table.text().split("Week Wok")[1]);
 		while (m.find()) {
 			sb3.append(m.group());
 		}
 		if ( language.equals("Finnish")) {
 			sb.append("Viikon erikoiset\n\n");
 			sb.append(table.text().split("Week|/|: ")[2].replaceAll("(?<!^|/)(M|VL|L|G).+(M|VL|L|G)", "") + sb2.toString() + "\n");
 			sb.append(table.text().split("Week|/|: ")[5].replaceAll("(?<!^|/)(M|VL|L|G).+(M|VL|L|G)", "") + sb3.toString());
 		} else {
 			sb.append("Specials of the week\n\n");
 			sb.append(table.text().split("Week|/|: ")[3] + sb2.toString() + "\n");
 			sb.append(table.text().split("Week|/|: ")[6] + " " + sb3.toString());
 		};
 		//Log.d("DATA",""+table.text());
 	    return sb.toString();
 	}
 	
 	private class DownloadMenuTask extends AsyncTask<Void, Void,String> {
 		ProgressDialog dialog;
 		
 		protected void onPreExecute() {
 			dialog = new ProgressDialog(AntellLMF.context);
 			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			dialog.setMessage("Loading data from Antell.fi");
 			dialog.show();
 		}
 		
 	    @Override
 	    protected String doInBackground(Void... params) {
            	FoodMenu menu = new FoodMenu(); 
            	String url = getFrameUrl("http://www.antell.fi/docs/lunch.php?LM%20Ericsson,%20Helsinki");
            	//Log.d("URL", url);
            	menu = getDaysMenu("http://www.antell.fi"+url);
            	return menu.toString();
 	    }
 
 		@Override
 		protected void onPostExecute(String result) {
 			text.setText(result);
 			dialog.dismiss();
 		}
 	}
 
 }
