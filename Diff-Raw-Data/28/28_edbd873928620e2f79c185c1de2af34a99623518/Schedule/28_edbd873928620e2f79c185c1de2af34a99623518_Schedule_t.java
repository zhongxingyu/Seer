 package com.example.iceberg;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.ListFragment;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.SimpleAdapter;
 
 public class Schedule extends ListFragment {
 	
 	String scheduleURL = "http://www.lincolnstars.com/leagues/print_schedule.cfm?leagueID=16793&clientID=4806&teamID=343151&mixed=1";
 	private static SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 	SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MM/dd hh:mm");
 	
 	private static List<HashMap<String,String>> dataList = new ArrayList<HashMap<String,String>>();
     private static SimpleAdapter adapter;
     
 	private enum Month {
 		Jan,
 		Feb,
 		Mar,
 		Apr,
 		May,
 		Jun,
 		Jul,
 		Aug,
 		Sep,
 		Oct,
 		Nov,
 		Dec
 	}
 	
 	public Schedule() {
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		// Keys used in Hashmap
 	    String[] from = { "logo","home_away_result","date_time" };
 	    // Ids of views in listview_layout
 	    int[] to = { R.id.logo,R.id.home_away_result,R.id.date_time };
 	    
 	    adapter = new SimpleAdapter(getActivity(), dataList, R.layout.schedule_listview_layout, from, to);
 	    setListAdapter(adapter);
 	    
 		// Inflate the layout for this fragment  
         View view =  inflater.inflate(R.layout.schedule_list_fragment, container, false); 
 		File dbFile = new File("/data/data/com.example.iceberg/databases/iceBerg");
 	    
 		if(dbFile.exists()){
 			//Log.i("af","database exists");
 			//List<HashMap<String,String>> dataList = new ArrayList<HashMap<String,String>>();
 	        DatabaseHandler db = new DatabaseHandler(getActivity());
 	        List<Game> schedule = db.getAllGames();
 	        db.close();
 	        
 	        List<HashMap<String,String>> dataListTemp = new ArrayList<HashMap<String,String>>();
 	        
 	        for(Game gm : schedule){
 	            HashMap<String, String> dataMap = new HashMap<String,String>();
 	            Date date = null;
 	            if(gm.getResult() == null){
 	            	dataMap.put("home_away_result", gm.getHomeAway());
 	            }
 	            else{
 	            	dataMap.put("home_away_result", gm.getResult());
 	            }
 	            try {
 	            	date = FORMATTER.parse(gm.getDate());
 					dataMap.put("date_time", dateFormat.format(date));
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	            dataMap.put("logo", gm.getOpponentImage());
 	            dataListTemp.add(dataMap);
 	        }
 	        dataList = dataListTemp;
 	        adapter.notifyDataSetChanged();
 	        new UpdateScores1(getActivity()).execute(getString(R.string.scheduleURL));
 		}
 		else{
 			//Log.i("af","no database");
 			DatabaseHandler db = new DatabaseHandler(getActivity());
 			db.close();
 			new LoadSchedule1(getActivity()).execute(getString(R.string.scheduleURL));
 		}
 		
         return view;
 	}
 	
 	private static class LoadSchedule1 extends AsyncTask<String,Integer,String>{
 
 		DatabaseHandler db = null;
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 		HashMap<String, String> imageMap = new HashMap<String, String>();
 		Context context = null;
 		private ProgressDialog dialog = null;
 	    //private Schedule schedule;
 		
 		public LoadSchedule1(Context context){
 			this.context = context;
 			db = new DatabaseHandler(context);
 			dialog = new ProgressDialog(context);
 			imageMap.put("fargo", Integer.toString(R.drawable.fargo));
 			imageMap.put("cedar", Integer.toString(R.drawable.cedar_rapids));
 			imageMap.put("chicago", Integer.toString(R.drawable.chicago));
 			imageMap.put("des", Integer.toString(R.drawable.desmoines));
 			imageMap.put("dubuque", Integer.toString(R.drawable.dubuque));
 			imageMap.put("green", Integer.toString(R.drawable.green_bay));
 			imageMap.put("indiana", Integer.toString(R.drawable.indiana));
 			imageMap.put("muskegon", Integer.toString(R.drawable.muskegon));
 			imageMap.put("omaha", Integer.toString(R.drawable.omaha));
 			imageMap.put("siouxcity", Integer.toString(R.drawable.sioux_city));
 			imageMap.put("siouxfalls", Integer.toString(R.drawable.sioux_falls));
 			imageMap.put("tri-city", Integer.toString(R.drawable.tricity));
 			imageMap.put("usntdp", Integer.toString(R.drawable.usa));
 			imageMap.put("waterloo", Integer.toString(R.drawable.waterloo));
 			imageMap.put("youngstown", Integer.toString(R.drawable.youngstown));
 		}
 		
 		protected void onPreExecute(){
 			//Toast.makeText(context,"Loading the schedule for the first time may take a minute.", Toast.LENGTH_SHORT).show();
 			this.dialog.setMessage("Initial load of the schedule may take a few seconds");
 	        this.dialog.show();
 		}
 		
 		protected String doInBackground(String... urls) {
 			URI uri = null;
 			try {
 				uri = new URI(urls[0]);
 			} 
 			catch (URISyntaxException e1) {
 			}
 			HttpClient client = new DefaultHttpClient();
 			HttpGet request = new HttpGet(uri);
 			
 			try{
 				HttpResponse response = client.execute(request);
 				InputStream in = response.getEntity().getContent();
 				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 				String htmlLine = null;
 				String line = null;
 				List<Game> schedule = new ArrayList<Game>();
 				String homeAway = null;
 				String mydate = null;
 				String date = null;
 				String score = null;
 				String finalScore = null;
 				String opponent = null;
 				boolean capture = false;
 				int j = 0;
 				
 				while((htmlLine = reader.readLine()) != null)
 				{
 					line = android.text.Html.fromHtml(htmlLine).toString();
 					if(capture && !line.equals("")){
 						switch(j){
 							case 0: //date
 								mydate = line.split("\\s+")[1];	
 								int month = Month.valueOf(mydate.split("-")[1]).ordinal() + 1;
 								mydate = "20" + mydate.split("-")[2] + "-" + month + "-" + mydate.split("-")[0];
 								j++;
 								break;
 							case 1: //time
 								//Log.i("complete date", dateFormat.parse(mydate + " " + line.split("P")[0]).toString());
 								date = mydate + " " + line.split("P")[0];	
 								j++;
 								break;
 							case 2: //away team
 								if(line.contains("Lincoln") || line.contains("lincoln") || line.contains("Stars") || line.contains("stars")){
 									homeAway = "AWAY";
 								}
 								else{
 									homeAway = "HOME";
 									opponent = line.trim();
 								}
 								j++;
 								break;
 							case 3://away team score
 								line = line.replaceAll("\\s", "");
 								if(!line.equals("")){
 									score = line;
 								}
 								j++;
 								break;
 							case 4: //home team
 								if(opponent == null){
 									opponent = line.trim();
 								}
 								j++;
 								break;
 							case 5://home team score
 								line = line.replaceAll("\\s", "");
 								String ot = "";
 								if(score != null){
 									if(line.contains("OT") || score.contains("OT")){
 										line = line.replaceAll("OT", "");
 										score = score.replaceAll("OT", ""); 
 										ot = " OT";
 									}
 									
 									if(Integer.parseInt(score) < Integer.parseInt(line)){
 										if(homeAway.equals("AWAY")){
 											finalScore = "L " + score + " - " + line + ot;
 										}
 										else{
 											finalScore = "W " + score + " - " + line + ot;
 										}
 									}
 									else{
 										if(homeAway.equals("AWAY")){
 											finalScore = "W " + score + " - " + line + ot;
 										}
 										else{
 											finalScore = "L " + score + " - " + line + ot;
 										}
 									}
 								}
 								else{
 									finalScore = null;
 								}
 								j++;
 								break;
 							case 6:			
 								String file = null;
 								if(opponent.split("\\s+").length > 1){
 									if(opponent.split("\\s+")[0].replace("\\s","").toLowerCase().equals("sioux")){
 										file = opponent.split("\\s+")[0].replace("\\s","").toLowerCase()+opponent.split("\\s+")[1].replace("\\s","").toLowerCase();
 									}
 									else{
 										file = opponent.split("\\s+")[0].replace("\\s","").toLowerCase();
 									}
 
 									schedule.add(new Game(date, opponent, homeAway, finalScore, imageMap.get(file)));
 								}
 								else{
 									schedule.add(new Game(date, opponent, homeAway, finalScore, imageMap.get(opponent.replace("\\s","").toLowerCase())));
 								}
 
 								homeAway = null;
 								score = null;
 								mydate = null;
 								date = null;
 								opponent = null;
 								j = 0;
 								capture = false;
 								//Log.i("af","going");
 							default:
 								break;
 						}
 					}
 					
 					if(line.equals("EX") || line.equals("RS")){
 						capture = true;
 					}
 				}
 				in.close();
 				
 				for (Game gm : schedule) {
 					//Log.i("af", "Datetime: "+gm.getDate() + " , Opp: " + gm.getOpponent()+ " , home: " + gm.getHomeAway()+ " , score: " + gm.getResult()+ " , img: " + gm.getOpponentImage());
 					db.addGame(gm);
 
 				}
 				//db.close();
 				return "done";
 				
 			}
 			catch(Exception e){
				//Log.i("af",e.toString());
 				db.close();
 				return null;
 			}
 		}
 		
 		protected void onPostExecute(String result){
 			dialog.dismiss();
 			//Log.i("af","done2");
 			//List<HashMap<String,String>> dataList = new ArrayList<HashMap<String,String>>();
 	        //DatabaseHandler db = new DatabaseHandler(context);
 	        List<Game> scheduleToLoad = db.getAllGames();
 	        db.close();
 
	        if(!dataList.isEmpty()){
	        	dataList.clear();
	        }
 	        
 	        for(Game gm : scheduleToLoad){
 	        	//Log.i("af","game: " +gm.getDate());
 	            HashMap<String, String> dataMap = new HashMap<String,String>();
 	            Date date = null;
 	            if(gm.getResult() == null){
 	            	dataMap.put("home_away_result", gm.getHomeAway());
 	            }
 	            else{
 	            	dataMap.put("home_away_result", gm.getResult());
 	            }
 	            try {
 	            	date = FORMATTER.parse(gm.getDate());
 					dataMap.put("date_time", dateFormat.format(date));
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	            dataMap.put("logo", gm.getOpponentImage());
	            dataList.add(dataMap);
 	        }  
 	        adapter.notifyDataSetChanged();		
 		}
 	}
 	
 	private class UpdateScores1 extends AsyncTask<String,Void,String>{
 
 		DatabaseHandler db = null;
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 		Context context;
 		
 		public UpdateScores1(Context context){
 			db = new DatabaseHandler(context);
 			this.context = context;
 		}
 
 		protected String doInBackground(String... urls) {
 			URI uri = null;
 			try {
 				uri = new URI(urls[0]);
 			} 
 			catch (URISyntaxException e1) {
 			}
 			HttpClient client = new DefaultHttpClient();
 			HttpGet request = new HttpGet(uri);
 			
 			List<Game> gamesWithNoResults = db.getGamesWithoutResult();
 			Date today = new Date(); //dateFormat.format(new Date());
 			
 			try{
 				HttpResponse response = client.execute(request);
 				InputStream in = response.getEntity().getContent();
 				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 				String htmlLine = null;
 				String line = null;
 				Game gameToUpdate = null;
 				String mydate = null;
 				String score = null;
 				String finalScore = null;
 				boolean capture = false;
 				int j = 0;
 				boolean dateIsAfterToday = false;
 				
 				while((htmlLine = reader.readLine()) != null && !dateIsAfterToday)
 				{
 					line = android.text.Html.fromHtml(htmlLine).toString();
 					if(capture && !line.equals("")){
 						switch(j){
 							case 0: //date
 								mydate = line.split("\\s+")[1];	
 								int month = Month.valueOf(mydate.split("-")[1]).ordinal() + 1;
 								mydate = "20" + mydate.split("-")[2] + "-" + month + "-" + mydate.split("-")[0];
 								j++;
 								break;
 							case 1: //time
 								if(dateFormat.parse(mydate).before(dateFormat.parse(dateFormat.format(today)))){
 									for(Game gm : gamesWithNoResults){	
 										if(gm.getDate().split("\\s+")[0].equalsIgnoreCase(mydate)){
 											gameToUpdate = gm;
 											capture = true;
 											break;
 										}
 										else{
 											capture = false;
 										}
 									}
 								}
 								else{
 									capture = false;
 									dateIsAfterToday = true;
 								}
 								j++;
 								break;
 							case 2: //away team
 								j++;
 								break;
 							case 3://away team score
 								line = line.replaceAll("\\s", "");
 								if(!line.equals("")){
 									score = line;
 								}
 								j++;
 								break;
 							case 4: //home team
 								j++;
 								break;
 							case 5://home team score
 								line = line.replaceAll("\\s", "");
 								String ot = "";
 								if(score != null){
 									if(line.contains("OT") || score.contains("OT")){
 										line = line.replaceAll("OT", "");
 										score = score.replaceAll("OT", ""); 
 										ot = " OT";
 									}
 									
 									if(Integer.parseInt(score) < Integer.parseInt(line)){
 										if(gameToUpdate.getHomeAway().equals("AWAY")){
 											finalScore = "L " + score + " - " + line + ot;
 										}
 										else{
 											finalScore = "W " + score + " - " + line + ot;
 										}
 									}
 									else{
 										if(gameToUpdate.getHomeAway().equals("AWAY")){
 											finalScore = "W " + score + " - " + line + ot;
 										}
 										else{
 											finalScore = "L " + score + " - " + line + ot;
 										}
 									}
 									gameToUpdate.setResult(finalScore);
 								}
 								else{
 									finalScore = null;
 								}
 								j++;
 								break;
 							case 6:
 								if(gameToUpdate != null){
 									db.updateGame(gameToUpdate);
 								}
 								score = null;
 								mydate = null;
 								gameToUpdate = null;
 								j = 0;
 								capture = false;
 							default:
 								break;
 						}
 					}
 					
 					if(line.equals("EX") || line.equals("RS")){
 						capture = true;
 						j = 0;
 					}
 				}
 				in.close();
 				return null;			
 			}
 			catch(Exception e){
 				//Log.i("af",e.toString());
				db.close();
 				return null;
 			}
 		}
 		
 		protected void onPostExecute(String result){
 	        List<Game> schedule = db.getAllGames();
 	        db.close();
 	 
	        if(!dataList.isEmpty())
				dataList.clear();
 	        
 	        for(Game gm : schedule){
 	            HashMap<String, String> dataMap = new HashMap<String,String>();
 	            Date date = null;
 	            if(gm.getResult() == null){
 	            	dataMap.put("home_away_result", gm.getHomeAway());
 	            }
 	            else{
 	            	dataMap.put("home_away_result", gm.getResult());
 	            }
 	            try {
 	            	date = FORMATTER.parse(gm.getDate());
 					dataMap.put("date_time", dateFormat.format(date));
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	            dataMap.put("logo", gm.getOpponentImage());
	            dataList.add(dataMap);
 	        }  
 	        adapter.notifyDataSetChanged();
 		}
 	}
 }
