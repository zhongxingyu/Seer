 package com.github.pozo.volan;
 import com.github.pozo.volan.R;
 import com.github.pozo.volan.db.DbConnector;
 import com.github.pozo.volan.db.BusLine;
 import com.github.pozo.volan.shared.BusLineIntent;
 import com.github.pozo.volan.shared.ListofStops;
 import com.github.pozo.volan.tasks.db.BusLineFavoriteExistsTask;
import com.github.pozo.volan.tasks.db.BusLineModelTask;
 import com.github.pozo.volan.tasks.db.BusLineShuffleFavoriteTask;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class BusLineActivity extends Activity {
 	public static final int GET_LINE_STOP = 0x2;
 	private int currentTimeShiftInMinute = 0;
 
 	private TextView lineCity;
 	private TextView lineNumber;
 
 	private TextView lineFrom;
 	private TextView lineTo;
 	private TextView dayType;
 	
 	private Button addToFavoritesBtn;
 	
 	private BusLineModelTask model;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.line_details);
 		
 		addToFavoritesBtn = (Button) findViewById(R.id.add_to_favorites_button);
 		addToFavoritesBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				new BusLineShuffleFavoriteTask(BusLineActivity.this).execute();
 			}
 		});
 		
 		lineCity = (TextView) findViewById(R.id.current_city);
 		lineNumber = (TextView) findViewById(R.id.line_name);
 		dayType = (TextView) findViewById(R.id.day_type);
 
 		lineTo = (TextView) findViewById(R.id.line_to);
 		lineTo.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				BusLineIntent busLineIntent = new BusLineIntent(getPackageName(),getPackageName()+ ".BusLineActivity", Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				BusLine busLine = getBusLine();
 
 				if(getBusLine().getDirection().equals(BusLine.DIRECTION_TO)) {
 					busLine.setDirection(BusLine.DIRECTION_BACK);
 				} else {
 					busLine.setDirection(BusLine.DIRECTION_TO);
 				}
				
 				busLineIntent.createIntent(busLine);
 				startActivity(busLineIntent);	
 			}
 		});
 		
 		lineFrom = (TextView) findViewById(R.id.line_from);
 		lineFrom.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				BusLineIntent busLineIntent = new BusLineIntent(getPackageName(),getPackageName()+ ".ListofStopsActivity", Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivityForResult(busLineIntent.createIntent(getBusLine())	,GET_LINE_STOP);
 			}
 		});
 	
 		model = new BusLineModelTask(this,getIntent());
 		model.execute();
 	}
     @Override
 	protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
     	super.onActivityResult(requestCode, resultCode, data);
 
         if (requestCode == GET_LINE_STOP) {
             if (resultCode == RESULT_OK) {
             	BusLine responseBusLine = BusLineIntent.getBusLine(data);
             	BusLine busLine = getBusLine();
             	
				busLine.setStopId(responseBusLine.getStopId());
            	busLine.setFromName(ListofStops.getStopNamebyId(this, responseBusLine.getStopId()));
 
             	lineFrom.setText(busLine.getFromName());
             	new BusLineFavoriteExistsTask(this, busLine).execute();
             }
         }
     }
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		DbConnector.close();
 	}
 	public void setLineCityText(String text) {
 		this.lineCity.setText(text);
 	}
 	public void setLineText(String text) {
 		this.lineNumber.setText(text);
 	}
 	public void setLineFromText(String text) {
 		this.lineFrom.setText(text);
 	}
 	public int getCurrentTimeShiftInMinute() {
 		return currentTimeShiftInMinute;
 	}
 	public void setCurrentTimeShiftInMinute(int currentTimeShiftInMinute) {
 		this.currentTimeShiftInMinute = currentTimeShiftInMinute;
 	}
 	public void setLineToText(String lineToText) {
 		this.lineTo.setText(lineToText);
 	}
 	public void setDayTypeText(String dayTypeText) {
 		dayType.setText(dayTypeText);
 	}
 	public void setFavoriteButtonBackground(boolean state) {
 		if(state) {
 			this.addToFavoritesBtn.setBackgroundResource(R.drawable.favorited);			
 		} else {
 			this.addToFavoritesBtn.setBackgroundResource(R.drawable.favorite);
 		}
 	}
 	public BusLine getBusLine() {
 		return model.getBusLine();
 	}
 }
