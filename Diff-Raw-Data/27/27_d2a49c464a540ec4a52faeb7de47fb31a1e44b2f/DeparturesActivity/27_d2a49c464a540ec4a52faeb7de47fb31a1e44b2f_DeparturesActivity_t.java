 package com.papagiannis.tuberun;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 import com.papagiannis.tuberun.binders.DeparturesBinder;
 import com.papagiannis.tuberun.favorites.DeparturesFavorite;
 import com.papagiannis.tuberun.favorites.Favorite;
 import com.papagiannis.tuberun.fetchers.DeparturesDLRFetcher;
 import com.papagiannis.tuberun.fetchers.DeparturesFetcher;
 import com.papagiannis.tuberun.fetchers.Observer;
 
 public class DeparturesActivity extends ListActivity implements Observer, OnClickListener {
 	private static final int ASK_LINE_DIALOG=1;
 	protected Button backButton;
 	protected Button logoButton;
 	protected TextView stationTextView;
 	protected TextView emptyTextView;
 	
 	private DeparturesFetcher fetcher;
 	private final ArrayList<HashMap<String,Object>> departures_list=new ArrayList<HashMap<String,Object>>();
 	private LineType lt;
 	private String stationcode;
 	private String stationnice;
 	private Station station;
 	
 	
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		create();
     }
     
     @SuppressWarnings("deprecation")
 	private void create() {
     	setContentView(R.layout.departures);
     	backButton = (Button) findViewById(R.id.back_button);
 		logoButton = (Button) findViewById(R.id.logo_button);
 		stationTextView = (TextView) findViewById(R.id.station_textview);
 		emptyTextView= (TextView) findViewById(R.id.empty_textview);
 		
 		backButton.setOnClickListener(this);
 		logoButton.setOnClickListener(this);
     	
     	departures_list.clear();
     	
     	Bundle extras = getIntent().getExtras();
     	String type = (String)extras.get("type");
     	if (type!=null && type.equals("station")) {
     		station=(Station)extras.get("station");
     		stationcode=station.getCode();
     		stationnice=station.getName();
     		stationTextView.setVisibility(View.GONE);
     		List<LineType> ls=station.getLinesForDepartures();
     		if (ls.size()>1) showDialog(ASK_LINE_DIALOG);
     		else if (ls.size()==1) {
     			lt=ls.get(0);
     			showDepartures();
     		}
     		//else no departures
     	}
     	else {
     		String line = (String)extras.get("line");
 			stationcode = (String)extras.get("stationcode");
 			stationnice = (String)extras.get("stationnice");
 			lt=LinePresentation.getLineTypeRespresentation(line);
 			showDepartures();
     	}
     }
 
 	private void showDepartures() {
 		stationTextView.setText(stationnice);
 		stationTextView.setBackgroundColor(LinePresentation.getBackgroundColor(lt));
 		stationTextView.setTextColor(LinePresentation.getForegroundColor(lt));
 		stationTextView.setVisibility(View.VISIBLE);
     	
 		if (lt==LineType.DLR) fetcher=new DeparturesDLRFetcher(lt, stationcode, stationnice);
 		else fetcher=new DeparturesFetcher(lt, stationcode, stationnice);
 		fetcher.registerCallback(this);
 		emptyTextView.setVisibility(View.GONE);
 		fetcher.update();
 		
 		View updateButton = findViewById(R.id.button_update);
         updateButton.setOnClickListener(new OnClickListener() {
         	public void onClick(View v) {
         		emptyTextView.setVisibility(View.GONE);
         		setListAdapter(null);
         		fetcher.update();
         	}
         });
         Favorite.getFavorites(this);
 	}
     
 	@Override
 	public void update() {
 		departures_list.clear();
 		
 		HashMap<String, ArrayList<HashMap<String, String>>> reply=fetcher.getDepartures();
 		for (String platform : reply.keySet()) {
 			HashMap<String,Object> m=new HashMap<String,Object>();
 			ArrayList<HashMap<String, String>> trains=reply.get(platform);
 			m.put("line", LinePresentation.getStringRespresentation(lt));
			m.put("platform", platform);
 			int i=1;
 			for (HashMap<String,String> train : trains) {
 				String s=train.get("destination");
 				m.put("destination"+i, s);
 				s=train.get("position");
 				m.put("position"+i, s);
 				s=train.get("time");
 				if (s.equals("")) s="due"; 
 				m.put("time"+i, s);
 				i++;
 			}
 			
 			DeparturesFavorite fav=new  DeparturesFavorite(lt,null);
 			fav.setIdentification(stationcode);
 			fav.setStation_nice(stationnice);
 			fav.setPlatform(platform);
 			Boolean isFavorite=Favorite.isFavorite(fav);
 			m.put("favorite", Boolean.toString(isFavorite));
 			
 			departures_list.add(m);	
 		}
 		
 		if (departures_list.isEmpty()) {
 			emptyTextView.setVisibility(View.VISIBLE);
 		}
 		else {
 			emptyTextView.setVisibility(View.GONE);
 		}
 		
 		SimpleAdapter adapter=new SimpleAdapter(this,
 				departures_list, 
 				R.layout.departures_status,
 				new String[]{"platform", "favorite",
 					"destination1","position1","time1",
 					"destination2","position2","time2",
 					"destination3","position3","time3"},
 				new int[]{R.id.departures_platform, R.id.add_favorite,
 					R.id.departures_destination1, R.id.departures_position1, R.id.departures_time1,
 					R.id.departures_destination2, R.id.departures_position2, R.id.departures_time2,
 					R.id.departures_destination3, R.id.departures_position3, R.id.departures_time3});
 		adapter.setViewBinder(new DeparturesBinder(lt, stationcode , stationnice ,  this, adapter));
 		setListAdapter(adapter);
 		
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog result=null;
 		switch (id) {
 			case ASK_LINE_DIALOG:
 				final List<LineType> ls=station.getLinesForDepartures();
 				final CharSequence[] items = new String[ls.size()];
 				int i=0;
 				for (LineType line : ls) {
 					items[i++]=LinePresentation.getStringRespresentation(line);
 				}
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setTitle("Select line");
 				builder.setItems(items, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int item) {
 						lt=ls.get(item);
 						showDepartures();
 					}
 				});
 				result= builder.create();
 			break;
 		}
 		return result;
 	};
 	
 	
 
     @Override
 	public void onClick(View v) {
 		finish();
 	}
 }
