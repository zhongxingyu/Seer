 package com.example.sicbogameexample;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import sicbo.components.HistoryComponent;
 import sicbo.components.UserComponent;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import com.example.sicbogameexample.GameEntity.PatternType;
 public class ViewHistoryActivity extends Activity implements OnClickListener {
 
 	TableLayout tblHistory;
 	boolean isSpace = false;
 	int size;
 	List<HistoryComponent> historyGame;
 	ArrayList<PatternType> winPattern;
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.table_history);
 		tblHistory = (TableLayout) findViewById(R.id.table_history);
 		historyGame = GameEntity.getInstance().userComponent.historyList;
 		size = historyGame.size();
 		initializeHeaderRow(tblHistory);
 		fillRow();
 	}
 
 	String convertDice(String textDice) {
 		String[] splits = textDice.split("\\|");
 		String s = "";
 		int lenght = splits.length;
 		for (int i = 0; i < lenght; i++) {
 			s = s + " " + splits[i];
 		}
 		return s;
 	}
 	private void fillRow() {
		int textColor = Color.WHITE;
 		float textSize = 10f;
 		String[] betSpot;
 		int lenght;
 		String result;
 		TableRow headerRow;
 		for (int i = 0; i < size; i++) {
 			if (historyGame.get(i).isWin)
 				result = "Win";
 			else
 				result = "lose";
 
 			headerRow = new TableRow(this);
 			addTextToRowWithValues(
 					headerRow,
 					convertSecondsToDate(
 							Long.parseLong(historyGame.get(i).betDate),
 							"dd/MM/yyyy hh:mm:ss"), textColor, textSize);
 			addTextToRowWithValues(headerRow, result, textColor, textSize);
 			addTextToRowWithValues(headerRow,
 					String.valueOf(historyGame.get(i).balance), textColor,
 					textSize);
 			addTextToRowWithValues(headerRow,
 					convertDice(historyGame.get(i).dices), Color.RED, textSize);
 			winPattern=GameEntity.getInstance().currentGame.convertStringtoArrayList(historyGame.get(i).betSpot);
 			addTextToRowWithValues(headerRow,winPattern.get(0).toString(), textColor, textSize);
 			tblHistory.addView(headerRow);
 			
 			lenght=winPattern.size();
 			for(int j=0;j<lenght-1;j++)
 			{
 			headerRow = new TableRow(this);
 			addTextToRowWithValues(headerRow, "", textColor, textSize);
 			addTextToRowWithValues(headerRow, "", textColor, textSize);
 			addTextToRowWithValues(headerRow, "", textColor, textSize);
 			addTextToRowWithValues(headerRow, "", textColor, textSize);
 			addTextToRowWithValues(headerRow, winPattern.get(j+1).toString(), textColor, textSize);
 			tblHistory.addView(headerRow);
 			}
 
 		}
 
 	}
 
 	
 
 	private void initializeHeaderRow(TableLayout scoreTable) {
 		// Create the Table header row
 		TableRow headerRow = new TableRow(this);
 
 		int textColor = Color.WHITE;
 		float textSize = 10f;
 
 		headerRow.setBackgroundColor(Color.BLUE);
 		addTextToRowWithValues(headerRow, "Date Time", textColor, textSize);
 		addTextToRowWithValues(headerRow, "Result", textColor, textSize);
 		addTextToRowWithValues(headerRow, "Balance", textColor, textSize);
 		addTextToRowWithValues(headerRow, "Dices", textColor, textSize);
 		addTextToRowWithValues(headerRow, "Bet", textColor, textSize);
 
 		scoreTable.addView(headerRow);
 	}
 
 	private void addTextToRowWithValues(final TableRow tableRow, String text,
 			int textColor, float textSize) {
 		TextView textView = new TextView(this);
 		textView.setTextSize(textSize);
 		textView.setTextColor(textColor);
 		textView.setText(text);
 		tableRow.addView(textView);
 	}
 
 	
 
 	public String convertSecondsToDate(long seconds, String dateFormat) {
 		long dateInMillis = seconds * 1000;
 
 		// Create a DateFormatter object for displaying date in specified
 		// format.
 		DateFormat formatter = new SimpleDateFormat(dateFormat);
 
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTimeInMillis(dateInMillis);
 
 		return formatter.format(calendar.getTime());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 
 		// getMenuInflater().inflate(R.menu.activity_view_history, menu);
 		return true;
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 
 		/*
 		 * if (arg0.equals(btnBack)) {
 		 * GameEntity.userComponent.historyList.clear(); ======= if
 		 * (arg0.equals(btnBack)) {
 		 * GameEntity.getInstance().userComponent.historyList.clear(); >>>>>>>
 		 * upstream/master Intent intent = new Intent(this,
 		 * SicBoGameActivity.class); this.startActivity(intent); finish(); }
 		 */
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			onBackPressed();
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public void onBackPressed() {
 		Intent intent = new Intent(this,
 				SicBoGameActivity.class);
 		this.startActivity(intent); 
 		this.finish();
 	}
 
 }
