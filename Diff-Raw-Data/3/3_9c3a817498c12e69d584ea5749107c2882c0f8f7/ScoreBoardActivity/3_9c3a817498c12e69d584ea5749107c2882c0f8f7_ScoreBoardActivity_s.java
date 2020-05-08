 package com.billybobbain.pitcher;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import com.billybobbain.pitcher.provider.DataContentProvider;
 import com.googlecode.chartdroid.core.IntentConstants;
 
 import fastball.GameDayBean;
 import fastball.view.scoreboard.Game;
 import fastball.view.scoreboard.GoGame;
 import fastball.view.scoreboard.IgGame;
 import fastball.view.scoreboard.ScoreBoard;
 import fastball.view.scoreboard.SgGame;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CalendarView;
 import android.widget.CalendarView.OnDateChangeListener;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ScoreBoardActivity extends ListActivity {
 	ScoreBoard scoreBoard;
     static final String TAG = Market.TAG;
     CalendarView calendar;
     final int DIALOG_CHARTDROID_DOWNLOAD = 1;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.scoreboard);
 		
 		  ListView lv = getListView();
 		  lv.setTextFilterEnabled(true);
 
 		  lv.setOnItemClickListener(new OnItemClickListener() {
 		
 
 			public void onItemClick(AdapterView<?> parent, View v, int position,	long id) {
 				// 2011_09_01_oakmlb_clemlb_1
 				String gameId = games.get(position).getId();
 				String[] gameIdParts = gameId.split("_");
 				String year = gameIdParts[0];
 				String month = gameIdParts[1];
 				String day = gameIdParts[2];
 				String away = gameIdParts[3].replace("mlb", "").toUpperCase();
 				String home = gameIdParts[4].replace("mlb", "").toUpperCase();
 				String datePath = year + "/" + month + "/" + day;
 				String gamePath = datePath + "/gid_"+gameId;
 				
 	            Intent i = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(DataContentProvider.PROVIDER_URI,gamePath));
 	            i.putExtra(Intent.EXTRA_TITLE, away + " at " + home + " on " + datePath);
 				i.putExtra(IntentConstants.Meta.Axes.EXTRA_FORMAT_STRING_Y, "%.1f");
 				i.putExtra(IntentConstants.Meta.Series.EXTRA_RAINBOW_COLORS,true);
 				
 				if (Market.isIntentAvailable(ScoreBoardActivity.this, i)) {
 					startActivity(i);
 				} else {
 					showDialog(DIALOG_CHARTDROID_DOWNLOAD);
 				}
 				
 				
 				
 			}
 		  });
 		  
 		  calendar = (CalendarView)findViewById(R.id.calendarView1);
 	      Calendar x = new GregorianCalendar();	      
 	      calendar.setDate(x.getTimeInMillis());
 	      year = x.get(Calendar.YEAR);
 	      month = x.get(Calendar.MONTH)+1;
 	      day = x.get(Calendar.DAY_OF_MONTH);
   		  this.setTitle("MLB Games on "+year+"/"+month+"/"+day);
 	      calendar.setVisibility(View.GONE);
 	      
 	      final Button showCalendarButton = (Button)findViewById(R.id.change_date_button);
 	      final Button saveDateButton = (Button)findViewById(R.id.save_date_button);
 
 	      showCalendarButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				saveDateButton.setVisibility(View.VISIBLE);
 				getListView().setVisibility(View.GONE);				
 				calendar.setVisibility(View.VISIBLE);
 				showCalendarButton.setVisibility(View.GONE);
 			}
 	    	  
 	      });
 	      
 	      saveDateButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				showCalendarButton.setVisibility(View.VISIBLE);
 				saveDateButton.setVisibility(View.GONE);
 				getListView().setVisibility(View.VISIBLE);				
 				calendar.setVisibility(View.GONE);
 				ScoreBoardActivity.this.setTitle("MLB Games on "+year+"/"+month+"/"+day);
 				doScoreBoardQuery();
 			}
 	    	  
 	      });
 	      
 	      
 	      calendar.setOnDateChangeListener(new OnDateChangeListener(){
 
 			public void onSelectedDayChange(CalendarView view, int y,
 					int m, int d) {
 				// TODO Auto-generated method stub
 				year = y;
 				month = m+1;
 				day = d;
 			}
 	    	  
 	      });
 		  
 		  this.doScoreBoardQuery();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 	int year = 2011;
 	int month = 9;
 	int day = 1;
 
     final Handler mHandler = new Handler();
     
     private void doScoreBoardQuery() {
 		Thread t = new Thread() {
 			public void run() {	
 				Looper.prepare();
 		        try {
 		        	
 		        	scoreBoard = new GameDayBean().getScoreBoard(year,month,day);
 					
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		        mHandler.post(mUpdateResults);
 				 Looper.loop();
 			}
 		};
 		t.start();
 	}
     
  // Create runnable for posting
  	final Runnable mUpdateResults = new Runnable() {
  		public void run() {
  			updateResultsInUi();
  		}
  	};
  	
  	List<Game> games = new ArrayList<Game>();
  	private void updateResultsInUi() {
  		List<String> game = new ArrayList<String>();
  		games.clear();
  		if(scoreBoard != null) {
  			StringBuilder b = new StringBuilder();
  			List<GoGame> goGame = scoreBoard.getGoGame();
  			List<SgGame> sgGame = scoreBoard.getSgGame();
  			List<IgGame> igGame = scoreBoard.getIgGame();
  			if(goGame != null) {
  				for(GoGame gg : goGame) {
  					Game g = gg.getGame(); 					
  					String[] gameIdParts = g.getId().split("_");
  					String away = gameIdParts[3].replace("mlb", "").toUpperCase();
  					String home = gameIdParts[4].replace("mlb", "").toUpperCase();
  					games.add(g); 					
  					game.add(away + " at " +home + " at " + g.getStartTime() );
  				} 				
  			}
  			if(sgGame != null) {
  				for(SgGame sg : sgGame) {
  					Game g = sg.getGame();
  					String[] gameIdParts = g.getId().split("_");
  					String away = gameIdParts[3].replace("mlb", "").toUpperCase();
  					String home = gameIdParts[4].replace("mlb", "").toUpperCase();
  					games.add(g);
  					game.add(away + " at " +home + " at " + g.getStartTime() );
  				}
  			} 			if(sgGame != null) {
  				for(SgGame sg : sgGame) {
  					Game g = sg.getGame();
  					String[] gameIdParts = g.getId().split("_");
  					String away = gameIdParts[3].replace("mlb", "").toUpperCase();
  					String home = gameIdParts[4].replace("mlb", "").toUpperCase();
  					games.add(g);
  					game.add(away + " at " +home + " at " + g.getStartTime() );
  				}
  			}
  			if(igGame != null) {
  				for(IgGame ig : igGame) {
  					Game g = ig.getGame();
  					String[] gameIdParts = g.getId().split("_");
  					String away = gameIdParts[3].replace("mlb", "").toUpperCase();
  					String home = gameIdParts[4].replace("mlb", "").toUpperCase();
  					games.add(g);
  					game.add(away + " at " +home + " at " + g.getStartTime() );
  				}
 			} 			if(sgGame != null) {
  				for(IgGame ig: igGame) {
  					Game g = ig.getGame();
  					String[] gameIdParts = g.getId().split("_");
  					String away = gameIdParts[3].replace("mlb", "").toUpperCase();
  					String home = gameIdParts[4].replace("mlb", "").toUpperCase();
  					games.add(g);
  					game.add(away + " at " +home + " at " + g.getStartTime() );
  				}
  			}
  			
  			setListAdapter(new ArrayAdapter<String>(this, R.layout.scoreboard_item, game));
 
  		}
  		
 	}
 	
 }
