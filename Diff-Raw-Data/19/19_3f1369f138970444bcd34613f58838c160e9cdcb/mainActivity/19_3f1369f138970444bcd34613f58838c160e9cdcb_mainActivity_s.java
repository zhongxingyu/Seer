 package michaels.spirit4android;
 
 import java.security.MessageDigest;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Service;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class mainActivity extends Activity {
 	final static double VERSION = 0.3;
 	public static String USERAGENT;
 	public static SharedPreferences saveFile;
 	public static JSONObject newsObject = null;
 	public static String preJSON = "";
 	boolean stop;
 	boolean pause;
 	boolean showCompletePlan;
 	static FHSSchedule schedule;
 	static TimeStreamView tsv;
 	public void onCreate(Bundle b){
 		super.onCreate(b);
 		DisplayMetrics dm = new DisplayMetrics();
 		this.getWindowManager().getDefaultDisplay().getMetrics(dm);
 		USERAGENT = "Spirit4Android v"+VERSION+" (disp "+dm.widthPixels+"*"+dm.heightPixels+")";
 		stop = false;
 		pause = false;
 		saveFile = this.getSharedPreferences("s4apref",MODE_WORLD_READABLE); //lesen der Einstellungen
 		if(saveFile.getInt("letzterSemesterWechsel", Calendar.getInstance().get(Calendar.MONTH)) != Calendar.getInstance().get(Calendar.MONTH) && (Calendar.getInstance().get(Calendar.MONTH) == Calendar.OCTOBER || Calendar.getInstance().get(Calendar.MONTH) == Calendar.APRIL)){
 			AlertDialog.Builder ab = new AlertDialog.Builder(this);
 			ab.setTitle("Semesterwechsel")
 				.setMessage("Es sieht so aus, als würde ein neues Semester anfangen. Passe also die Einstellungen an, damit dein Stundenplan auch weiterhin stimmt...")
 				.setPositiveButton("Plan-Einstellungen...", new OnClickListener(){
 
 					public void onClick(DialogInterface arg0, int arg1) {
 						Intent intent = new Intent(mainActivity.this,settingsActivity.class);
 						mainActivity.this.startActivity(intent);
 						Editor e = saveFile.edit();
 						e.putInt("letzterSemesterWechsel", Calendar.getInstance().get(Calendar.MONTH));
 						e.commit();
 					}
 					
 				});
 			ab.create().show();
 		}
 		if(!saveFile.contains("letzterSemesterWechsel")){
 			Editor e = saveFile.edit();
 			e.putInt("letzterSemesterWechsel", Calendar.getInstance().get(Calendar.MONTH));
 			e.commit();
 		}
 		if(!saveFile.getString("scheduleJSON", "[]").equals("[]")){
 			try {
 				schedule = new FHSSchedule(new JSONArray(saveFile.getString("scheduleJSON", "[]")),saveFile);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		//Oberfläche
 		this.setContentView(R.layout.main);
 		
 		final ListView newsListe = (ListView) this.findViewById(R.id.newsListe);
 		try {
 			final JSONArray newsJSON = new JSONArray(saveFile.getString("newsJSON", "[]"));
 			String[] dispInList = new String[newsJSON.length()];
 			for(int i = 0; i<newsJSON.length(); i++){
 				dispInList[newsJSON.length()-1-i] = newsJSON.getJSONObject(i).getString("subject");	
 			}
 			ArrayAdapter<String> aas = new ArrayAdapter<String>(this, R.layout.listelement, dispInList);
 			newsListe.setAdapter(aas);
 			newsListe.setOnItemClickListener(new OnItemClickListener(){
 
 				public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
 					try {
 						newsObject = newsJSON.getJSONObject(newsJSON.length()-1-pos);
 						Intent i = new Intent(mainActivity.this,newsActivity.class);
 						mainActivity.this.startActivity(i);
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 				
 			});
 		} catch (JSONException e) {
 			Log.e("Display Creation Error",e.getClass().getName()+": "+e.getMessage());
 		}
 		
 		Runnable r = new Runnable(){
 			
 			public void run() {
 				if(!pause){
 					if(schedule.length() == 0){
 						((LinearLayout)mainActivity.this.findViewById(R.id.main_plan)).setVisibility(View.VISIBLE);
 						((LinearLayout)mainActivity.this.findViewById(R.id.main_plan_alternative)).setVisibility(View.GONE);
 						((TextView)mainActivity.this.findViewById(R.id.countdown)).setVisibility(View.GONE);
 						((TextView)mainActivity.this.findViewById(R.id.nächstesEvent)).setText("z.Z. keine Stundenplan-Daten vorhanden. Lade deinen Plan über [Menü-Taste] > \"Stundenplan-Einstellungen\" herunter und stell dort auch deine Gruppen-Filter ein.");
 					} else {
 						TextView nevent = (TextView)mainActivity.this.findViewById(R.id.nächstesEvent);
						if(tsv == null || tsv.switchDayAutomatically){
 							TextView countdown = (TextView)mainActivity.this.findViewById(R.id.countdown);
 							JSONObject c = schedule.getNextEvent();
 							Calendar nCalendar = schedule.getNextCalendar(c);
 							long zeitDifferenz = nCalendar.getTimeInMillis()- new GregorianCalendar().getTimeInMillis();
 							long stunden  = (zeitDifferenz / (3600*1000));
 							long minuten  = (zeitDifferenz / 60000) - (stunden * 60);
 							long sekunden = (zeitDifferenz / 1000) - (stunden*3600+minuten*60);
 							GregorianCalendar bzt = (GregorianCalendar) nCalendar.clone();
 							bzt.set(Calendar.HOUR_OF_DAY, 0);
 							bzt.set(Calendar.MINUTE, 0);
 							long tage = (bzt.getTimeInMillis()-new GregorianCalendar().getTimeInMillis())/(24*60*60*1000) +1;
 							countdown.setText(stunden>23 ? "~ "+tage+" Tag"+(tage==1?"":"en")+" um "+DateFormat.getTimeInstance(DateFormat.SHORT).format(schedule.getNextCalendar(schedule.getNextEvent()).getTime()) : String.format("%02d:%02d:%02d", stunden,minuten,sekunden));
 							countdown.setVisibility(View.VISIBLE);
 							nevent.setText("Nächstes Event ("+c.optString("eventType")+") in");
 							if(tsv == null || tsv.schedule != schedule){
 								tsv = new TimeStreamView(mainActivity.this,schedule);
 								((LinearLayout) mainActivity.this.findViewById(R.id.main_plan_alternative)).removeAllViews();
 								((LinearLayout) mainActivity.this.findViewById(R.id.main_plan_alternative)).addView(tsv);
 							}
 							nevent.setClickable(false);
 							nevent.setTextColor(0xffffffff);
 						} else {
 							((TextView)mainActivity.this.findViewById(R.id.countdown)).setVisibility(View.GONE);
 							nevent.setTextColor(0xffcccccc);
 							nevent.setText("Hier \"klicken\" um zurück zum Countdown zu kommen...");
 							nevent.setOnClickListener(new View.OnClickListener(){
 
 								public void onClick(View v) {
 									tsv.switchDayAutomatically = true;
 								}
 								
 							});
 							nevent.setClickable(true);
 						}
 						tsv.invalidate();
 					}
 				}
 				if(!mainActivity.this.stop && preJSON.equals(mainActivity.saveFile.getString("newsJSON", "")+mainActivity.saveFile.getString("scheduleJSON", ""))){
 					Intent serv = new Intent(mainActivity.this,updater.class);
 					mainActivity.this.startService(serv);
 					Handler h = new Handler();
 					h.removeCallbacks(this);
 					h.postDelayed(this, 500);
 				} else if(!mainActivity.this.stop && !mainActivity.this.pause){
 					Intent restart = new Intent(mainActivity.this,mainActivity.class);
 					mainActivity.this.startActivity(restart);
 					mainActivity.this.finish();
 				} else if(!mainActivity.this.stop){
 					Handler h = new Handler();
 					h.removeCallbacks(this);
 					h.postDelayed(this, 500);
 				}
 					
 					
 				preJSON = mainActivity.saveFile.getString("newsJSON", "")+mainActivity.saveFile.getString("scheduleJSON", "");
 			}
 			
 		};
 		preJSON = mainActivity.saveFile.getString("newsJSON", "")+mainActivity.saveFile.getString("scheduleJSON", "");
 		Handler countdown = new Handler();
 		countdown.removeCallbacks(r);
 		countdown.post(r);
 	}
 	
 	public void onDestroy(){
 		stop = true;
 		super.onDestroy();
 	}
 	
 	public void onPause(){
 		pause = true;
 		super.onPause();
 	}
 	
 	public void onResume(){
 		try {
 			schedule = new FHSSchedule(new JSONArray(saveFile.getString("scheduleJSON", "[]")),saveFile);
			if(tsv != null){
				((LinearLayout) mainActivity.this.findViewById(R.id.main_plan_alternative)).removeAllViews();
				tsv = new TimeStreamView(mainActivity.this,schedule);
				((LinearLayout) mainActivity.this.findViewById(R.id.main_plan_alternative)).addView(tsv);
			}
 		} catch(Exception e){}
 		pause = false;
 		super.onResume();
 	}
 	
 	public boolean onCreateOptionsMenu(Menu m){
 		this.getMenuInflater().inflate(R.menu.main_menu, m);
 		return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem mi){
 		if(mi.getItemId() == R.id.settings){ // Einstellungen
 			Intent EinstellungenStarten = new Intent(this,settingsActivity.class);
 			this.startActivity(EinstellungenStarten);
 			return true;
 		} else if(mi.getItemId() == R.id.update){ // News-Update
 			Editor e = saveFile.edit();
 			e.putLong("lastUpdate", 0);
 			e.commit();
 			Toast.makeText(this, "Update läuft...", Toast.LENGTH_LONG).show();
 			Intent startService = new Intent(this,updater.class);
 			this.startService(startService);
 			return true;
 		} else if(mi.getItemId() == R.id.about){ // Über
 			Intent ÜberStarten = new Intent(this,about.class);
 			this.startActivity(ÜberStarten);
 			return true;
 		}
 		return super.onOptionsItemSelected(mi);
 	}
 
 	
 	public static String MD5(String str){
 		try {
 			String rtn = "";
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			md.reset();
 			byte[] resBytes = md.digest(str.getBytes());
 			for(byte b:resBytes){
 				rtn += String.format("%02x", (((int)b) & 0xFF));
 			}
 			return rtn;
 		} catch (Exception e) {
 			Log.e("MD5", e.getClass().getName()+": "+e.getMessage());
 		}
 		return "";
 	}
 	
 	/*public class schedule{
 		ArrayList<GregorianCalendar> events = new ArrayList<GregorianCalendar>();
 		ArrayList<Integer> eventIndexes = new ArrayList<Integer>();
 		JSONArray eventJSON;
 		public schedule(JSONArray Events) throws JSONException{
 			eventJSON = Events;
 			GregorianCalendar actual = new GregorianCalendar();
 			for(int i = 0; i<Events.length(); i++){
 				JSONObject current = eventJSON.optJSONObject(i);
 				if(current != null){
 					GregorianCalendar currentEvent = new GregorianCalendar();
 					JSONObject appointment = current.optJSONObject("appointment");
 					if(appointment != null && (current.getString("group").replaceAll("[^0-9]*", "").length() == 0 || Integer.parseInt(current.getString("group").replaceAll("[^0-9]*", "")) == mainActivity.saveFile.getInt("group"+MD5(current.getString("eventType")+"/"+current.getString("titleShort")), 0))){
 						String startzeit = appointment.getString("time").substring(0, appointment.getString("time").indexOf("-"));
 						currentEvent.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt(startzeit.substring(0, 2)));
 						currentEvent.set(GregorianCalendar.MINUTE, Integer.parseInt(startzeit.substring(3)));
 						currentEvent.set(GregorianCalendar.SECOND, 0);
 						currentEvent.set(GregorianCalendar.MILLISECOND, 0);
 						//                                Wochentag aus dem Stundenplan entnehmen                            
 						int tag = (Arrays.asList(new String[]{"Sonntag","Montag","Dienstag","Mittwoch","Donnerstag","Freitag","Samstag"}).indexOf(appointment.getString("day"))+1);
 						//  Addiere Tage bis zum Event.
 						currentEvent.add(Calendar.DAY_OF_MONTH, tag-currentEvent.get(GregorianCalendar.DAY_OF_WEEK));
 						if(actual.after(currentEvent))
 							currentEvent.add(Calendar.DAY_OF_MONTH, 7);
 						String ugw = appointment.getString("week");
 						if(!ugw.equals("w"))
 							currentEvent.add(Calendar.DAY_OF_MONTH, ((ugw.equals("g") && currentEvent.get(GregorianCalendar.WEEK_OF_YEAR)%2 == 0) || (ugw.equals("u") && currentEvent.get(GregorianCalendar.WEEK_OF_YEAR)%2 == 1)? 0:7));
 						// Füge an entsprechender Stelle in die List ein
 						int positionInList = 0;
 						for(int j=0; j<events.size(); j++)
 							if(currentEvent.before(events.get(j)))
 								positionInList = j;
 						events.add(positionInList, currentEvent);
 						eventIndexes.add(positionInList,i);
 						
 					} else {
 						continue;
 					}
 				}
 			}
 		}
 		public JSONObject getNextJSON(){
 			return getNextJSON(new GregorianCalendar());
 		}
 		public JSONObject getNextJSON(GregorianCalendar bef){
 			if(getNextCalendar() != null){
 				return eventJSON.optJSONObject(eventIndexes.get(events.indexOf(getNextCalendar(bef))));
 			}
 			return null;
 		}
 		
 		public GregorianCalendar getCurrentCalendar(){
 			for(GregorianCalendar gc:events){
 				//JSONObject jo = getNextJSON(gc);
 				//gc = ((GregorianCalendar)gc.clone()).add(Calendar.DAY_OF_MONTH, ());
 				//if(gc.before(new GregorianCalendar()) && new GregorianCalendar((GregorianCalendar)gc.clone())
 			}
 			return null;
 		}
 		
 		public GregorianCalendar getNextCalendar(){
 			return getNextCalendar(new GregorianCalendar());
 		}
 		public GregorianCalendar getNextCalendar(GregorianCalendar bef){
 			GregorianCalendar nächster = new GregorianCalendar();
 			nächster.add(GregorianCalendar.YEAR, 2);
 			for(int i=0; i<events.size(); i++){
 				if(events.get(i).before(nächster) && events.get(i).after(bef))
 					nächster = events.get(i);
 			}
 			return (nächster.get(GregorianCalendar.YEAR) == new GregorianCalendar().get(GregorianCalendar.YEAR) + 2 ? null : nächster);
 		}
 	}*/
 
 	public static class updater extends Service {
 		public int onStartCommand(Intent intent,int flags,int serviceID){
 			HttpClient client = new DefaultHttpClient();
 			long lastUpdate = mainActivity.saveFile.getLong("lastUpdate", 0);
 			if(lastUpdate < System.currentTimeMillis()-24*60*60*1000){
 				try {
 					String responseText = "";
 					HttpGet hg = new HttpGet("http://spirit.fh-schmalkalden.de/rest/news");
 					hg.setHeader("User-Agent", mainActivity.USERAGENT);
 					HttpResponse response = client.execute(hg); //News
 					responseText = EntityUtils.toString(response.getEntity()); //Antwort in einen String wandeln.
 					try {
 						new JSONArray(responseText);
 						Editor e = mainActivity.saveFile.edit();
 						e.putString("newsJSON", responseText);
 						e.putLong("lastUpdate", System.currentTimeMillis());
 						e.commit();
 					} catch(JSONException e){
 						Log.e("a","E:"+e.getClass().getName());
 					}
 					
 					
 				} catch (Exception e) {
 					Log.e("Spirit4Android - Client", e.getClass().getName()+": "+e.getMessage());
 				}
 				
 			}
 			this.stopSelf();
 			return Service.START_NOT_STICKY;
 		}
 		
 		public IBinder onBind(Intent intent) {
 			return null;
 		}
 		
 	}
 }
