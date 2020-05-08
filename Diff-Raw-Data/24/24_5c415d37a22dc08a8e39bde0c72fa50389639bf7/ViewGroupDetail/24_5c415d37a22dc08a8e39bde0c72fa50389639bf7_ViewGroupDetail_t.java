 package com.mthatcher.starcraft2wcs;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.drawable.Drawable;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import com.mthatcher.starcraft2wcs.entry.DetailEntry;
 import com.mthatcher.starcraft2wcs.entry.DetailEntry.MapDetail;
 import com.mthatcher.starcraft2wcs.entry.ViewHolderData;
 
 public class ViewGroupDetail extends Activity {
 
 	private HashMap<String, Drawable> nameToRace;
 	private HashMap<String, Drawable> nameToFlag;
 	private ArrayList<DetailEntry> entries;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		nameToRace = new HashMap<String, Drawable>();
 		nameToFlag = new HashMap<String, Drawable>();
 		setupActionBar();
 		Intent intent = getIntent();
 		ViewHolderData vhd = AppClass.getVhd();
 		entries = getDetailEntries(intent.getExtras().getInt(
 				LandingPage.ENTRY_ID_EXTRA));
 		int numPlayers = vhd.isGroupEntry() ? vhd.getPlayerName().length : vhd
 				.getPlayerName().length / 2;
 		if (vhd.isGroupEntry()) {
 			setContentView(R.layout.activity_view_group_detail);
 		} else {
 			setContentView(R.layout.activity_view_bracket_detail);
 		}
 		initKnownValues(vhd, numPlayers);
 		initNewValues(numPlayers);
 	}
 
 	private void initNewValues(int numPlayers) {
 		displayDetailEntries(entries, numPlayers);
 	}
 
 	private void displayDetailEntries(ArrayList<DetailEntry> entries,
 			int numPlayers) {
 		ViewGroup currentContent = (ViewGroup) findViewById(android.R.id.content);
 		TableLayout tl = (TableLayout) ((ScrollView) currentContent
 				.getChildAt(0)).getChildAt(0);
 		TableRow currentRow;
 		TextView p1NameTV, p1RaceTV, p1FlagTV, p1WinsTV, p2NameTV, p2RaceTV, p2FlagTV, p2WinsTV, p1MapWinTV, mapNameTV, p2MapWinTV;
 		DetailEntry curEntry;
 		ArrayList<MapDetail> curMaps;
 		MapDetail curMap;
 		for (int i = 0; i < entries.size(); i++) {
 			curEntry = entries.get(i);
 
 			currentRow = (TableRow) ((TableLayout) tl.getChildAt(i + 2
 					+ numPlayers)).getChildAt(0);
 			p1NameTV = (TextView) currentRow
 					.findViewById(R.id.group_detail_1_name);
 			p1RaceTV = (TextView) currentRow
 					.findViewById(R.id.group_detail_1_race);
 			p1FlagTV = (TextView) currentRow
 					.findViewById(R.id.group_detail_1_flag);
 			p1WinsTV = (TextView) currentRow
 					.findViewById(R.id.group_detail_1_map_score);
 			p2NameTV = (TextView) currentRow
 					.findViewById(R.id.group_detail_2_name);
 			p2RaceTV = (TextView) currentRow
 					.findViewById(R.id.group_detail_2_race);
 			p2FlagTV = (TextView) currentRow
 					.findViewById(R.id.group_detail_2_flag);
 			p2WinsTV = (TextView) currentRow
 					.findViewById(R.id.group_detail_2_map_score);
 
 			p1NameTV.setText(curEntry.getPlayer1Name());
 			p1RaceTV.setCompoundDrawablesWithIntrinsicBounds(
					nameToRace.get(curEntry.getPlayer1Name()), null, null, null);
 			p1FlagTV.setCompoundDrawablesWithIntrinsicBounds(
					nameToFlag.get(curEntry.getPlayer1Name()), null, null, null);
 			p1WinsTV.setText(String.valueOf(curEntry.getPlayer1Wins()));
 			p2WinsTV.setText(String.valueOf(curEntry.getPlayer2Wins()));
 			p2FlagTV.setCompoundDrawablesWithIntrinsicBounds(
					nameToFlag.get(curEntry.getPlayer2Name()), null, null, null);
 			p2RaceTV.setCompoundDrawablesWithIntrinsicBounds(
					nameToRace.get(curEntry.getPlayer2Name()), null, null, null);
 			p2NameTV.setText(curEntry.getPlayer2Name());
 
 			p1NameTV.setBackgroundColor(curEntry.getP1BackgroundColor());
 			p1RaceTV.setBackgroundColor(curEntry.getP1BackgroundColor());
 			p1FlagTV.setBackgroundColor(curEntry.getP1BackgroundColor());
 			p2NameTV.setBackgroundColor(curEntry.getP2BackgroundColor());
 			p2RaceTV.setBackgroundColor(curEntry.getP2BackgroundColor());
 			p2FlagTV.setBackgroundColor(curEntry.getP2BackgroundColor());
 
 			curMaps = curEntry.getMaps();
 			for (int j = 0; j < curMaps.size(); j++) {
 				curMap = curMaps.get(j);
 
 				currentRow = (TableRow) ((TableLayout) tl.getChildAt(i + 2
 						+ numPlayers)).getChildAt(1 + j);
 				p1MapWinTV = (TextView) currentRow
 						.findViewById(R.id.player_1_map_win);
 				p2MapWinTV = (TextView) currentRow
 						.findViewById(R.id.player_2_map_win);
 				mapNameTV = (TextView) currentRow.findViewById(R.id.map_name);
 
 				mapNameTV.setText(curMap.getMapName());
 				if (curMap.doesPlayer1Win())
 					p1MapWinTV.setCompoundDrawablesWithIntrinsicBounds(0, 0,
 							R.drawable.mapwin, 0);
 				if (curMap.doesPlayer2Win())
 					p2MapWinTV.setCompoundDrawablesWithIntrinsicBounds(
 							R.drawable.mapwin, 0, 0, 0);
 			}
 		}
 	}
 
 	private ArrayList<DetailEntry> getDetailEntries(int entryid) {
 		ArrayList<DetailEntry> entries = new ArrayList<DetailEntry>();
 		WcsDBHelper dbHelper = AppClass.getDBHelper();
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		Cursor c = db
 				.rawQuery(
 						"SELECT matches.player1name, matches.player2Name, maps.mapname, maps.mapwinner FROM maps, matches WHERE maps.matchid = matches.id AND matches.scheduleid = ?",
 						new String[] { String.valueOf(entryid) });
 		c.moveToFirst();
 		ArrayList<MapDetail> maps = null;
 		String player1Name = "$DEFAULTVAL$";
 		String player2Name = "$DEFAULTVAL$";
 		DetailEntry de = null;
 		while (!c.isAfterLast()) {
 			if ((player1Name != null && player2Name != null)
 					&& !(player1Name.equals(c.getString(0)) && player2Name
 							.equals(c.getString(1)))) {
 				maps = new ArrayList<MapDetail>();
 				de = new DetailEntry(c.getString(0), c.getString(1),
 						nameToRace.get(c.getString(0)), nameToRace.get(c
 								.getString(1)), nameToFlag.get(c.getString(0)),
 						nameToFlag.get(c.getString(1)), maps);
 				entries.add(de);
 				player1Name = c.getString(0);
 				player2Name = c.getString(1);
 			}
 			de.addMapDetail(de.new MapDetail(c.getString(2), c.getString(3)));
 			c.move(1);
 		}
 		c.close();
 		return entries;
 	}
 
 	private void initKnownValues(ViewHolderData data, int numRows) {
 		/*
 		 * In the normal case we'll have 4 players, we want to avoid as much
 		 * work as possible for that normal case so we use != instead of < or >
 		 * here.
 		 */
 		// int expectedNumPlayers = data.isGroupEntry() ? 4 : 8;
 		// if (data.getPlayerName().length != expectedNumPlayers) {
 		adjustTableSize(data, numRows);
 		// }
 
 		ViewGroup currentContent = (ViewGroup) findViewById(android.R.id.content);
 		TableLayout tl = (TableLayout) ((ScrollView) currentContent
 				.getChildAt(0)).getChildAt(0);
 		LayoutInflater vi = LayoutInflater.from(getBaseContext());
 
 		TextView groupNameTV = (TextView) findViewById(R.id.schedule_name);
 		TextView dateTV = (TextView) findViewById(R.id.schedule_date);
 
 		groupNameTV.setText(data.getGroupName());
 		dateTV.setText(data.getDate());
 
 		if (data.isGroupEntry())
 			initGroupRows(data);
 		else
 			initBracketRows(data);
 
 		// Re-add match detail rows
 		for (int i = 0; i < entries.size(); i++) {
 			TableLayout newRow = (TableLayout) vi.inflate(
 					R.layout.match_detail_tbl, null);
 			// Most series are best-of-three, but if this one is different we
 			// have to adjust its size
 			if (entries.get(i).getMaps().size() != 3) {
 				while (newRow.getChildCount() - 1 < entries.get(i).getMaps()
 						.size())
 					newRow.addView(vi.inflate(R.layout.map_detail_row, null));
 				while (newRow.getChildCount() - 1 > entries.get(i).getMaps()
 						.size())
 					newRow.removeViewAt(newRow.getChildCount() - 1);
 			}
 			tl.addView(newRow);
 		}
 	}
 
 	private void initBracketRows(ViewHolderData data) {
 		ViewGroup currentContent = (ViewGroup) findViewById(android.R.id.content);
 		TableLayout tl = (TableLayout) ((ScrollView) currentContent
 				.getChildAt(0)).getChildAt(0);
 		TableRow currentRow;
 		TextView flag1TV, race1TV, flag2TV, race2TV, name1TV, name2TV, match1TV, match2TV;
 		Drawable flag1D, flag2D, race1D, race2D;
 		int p1, p2;
 		for (int i = 0; i < tl.getChildCount() - 2; i++) {
 			p1 = i * 2;
 			p2 = p1 + 1;

			flag1D = data.getFlag()[p1];
			flag2D = data.getFlag()[p2];
			race1D = data.getRace()[p1];
			race2D = data.getRace()[p2];
			
 			currentRow = (TableRow) tl.getChildAt(i + 2);
 
			nameToFlag.put(data.getPlayerName()[p1], flag1D);
			nameToRace.put(data.getPlayerName()[p1], race1D);
			nameToFlag.put(data.getPlayerName()[p2], flag2D);
			nameToRace.put(data.getPlayerName()[p2], race2D);
			
 			flag1D = data.getFlag()[p1];
 			flag2D = data.getFlag()[p2];
 			race1D = data.getRace()[p1];
 			race2D = data.getRace()[p2];
 
 			flag1TV = (TextView) currentRow
 					.findViewById(R.id.bracket_player_1_race);
 			flag2TV = (TextView) currentRow
 					.findViewById(R.id.bracket_player_2_race);
 			race1TV = (TextView) currentRow
 					.findViewById(R.id.bracket_player_1_flag);
 			race2TV = (TextView) currentRow
 					.findViewById(R.id.bracket_player_2_flag);
 			name1TV = (TextView) currentRow
 					.findViewById(R.id.bracket_player_1_name);
 			name2TV = (TextView) currentRow
 					.findViewById(R.id.bracket_player_2_name);
 			match1TV = (TextView) currentRow
 					.findViewById(R.id.bracket_player_1_map_score);
 			match2TV = (TextView) currentRow
 					.findViewById(R.id.bracket_player_2_map_score);
 
 			flag1TV.setCompoundDrawablesWithIntrinsicBounds(flag1D, null, null,
 					null);
 			flag2TV.setCompoundDrawablesWithIntrinsicBounds(flag2D, null, null,
 					null);
 			race1TV.setCompoundDrawablesWithIntrinsicBounds(race1D, null, null,
 					null);
 			race2TV.setCompoundDrawablesWithIntrinsicBounds(race2D, null, null,
 					null);
 			name1TV.setText(data.getPlayerName()[p1]);
 			name2TV.setText(data.getPlayerName()[p2]);
 			match1TV.setText(data.getMapScore()[p1]);
 			match2TV.setText(data.getMapScore()[p2]);
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	@SuppressLint("NewApi")
 	private void initGroupRows(ViewHolderData data) {
 		ViewGroup currentContent = (ViewGroup) findViewById(android.R.id.content);
 		TableLayout tl = (TableLayout) ((ScrollView) currentContent
 				.getChildAt(0)).getChildAt(0);
 		TableRow currentRow;
 		TextView rankTV, flagTV, raceTV, nameTV, matchTV, gameTV;
 		Drawable flagD, raceD;
 		for (int i = 0; i < tl.getChildCount() - 2; i++) {
 			flagD = data.getFlag()[i];
 			raceD = data.getRace()[i];
 
 			nameToFlag.put(data.getPlayerName()[i], flagD);
 			nameToRace.put(data.getPlayerName()[i], raceD);
 
 			currentRow = (TableRow) tl.getChildAt(i + 2);
 			rankTV = (TextView) currentRow.findViewById(R.id.group_player_rank);
 			flagTV = (TextView) currentRow.findViewById(R.id.group_player_flag);
 			raceTV = (TextView) currentRow.findViewById(R.id.group_player_race);
 			nameTV = (TextView) currentRow.findViewById(R.id.group_player_name);
 			matchTV = (TextView) currentRow
 					.findViewById(R.id.group_player_match_score);
 			gameTV = (TextView) currentRow
 					.findViewById(R.id.group_player_map_score);
 
 			rankTV.setText(data.getRank()[i]);
 			flagTV.setCompoundDrawablesWithIntrinsicBounds(flagD, null, null,
 					null);
 			raceTV.setCompoundDrawablesWithIntrinsicBounds(raceD, null, null,
 					null);
 			nameTV.setText(data.getPlayerName()[i]);
 			matchTV.setText(data.getMatchScore()[i]);
 			gameTV.setText(data.getMapScore()[i]);
 			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
 				gameTV.setBackground(data.getBackgroundColor()[i]);
 				matchTV.setBackground(data.getBackgroundColor()[i]);
 				nameTV.setBackground(data.getBackgroundColor()[i]);
 				raceTV.setBackground(data.getBackgroundColor()[i]);
 				flagTV.setBackground(data.getBackgroundColor()[i]);
 				rankTV.setBackground(data.getBackgroundColor()[i]);
 			} else {
 				gameTV.setBackgroundDrawable(data.getBackgroundColor()[i]);
 				matchTV.setBackgroundDrawable(data.getBackgroundColor()[i]);
 				nameTV.setBackgroundDrawable(data.getBackgroundColor()[i]);
 				raceTV.setBackgroundDrawable(data.getBackgroundColor()[i]);
 				flagTV.setBackgroundDrawable(data.getBackgroundColor()[i]);
 				rankTV.setBackgroundDrawable(data.getBackgroundColor()[i]);
 			}
 		}
 	}
 
 	private void adjustTableSize(ViewHolderData data, int numRows) {
 		// Get the current view
 		ViewGroup currentContent = (ViewGroup) findViewById(android.R.id.content);
 
 		// Get the table held by the current view
 		TableLayout tl = (TableLayout) ((ScrollView) currentContent
 				.getChildAt(0)).getChildAt(0);
 
 		LayoutInflater vi = LayoutInflater.from(getBaseContext());
 
 		// Strip match detail rows (we'll re-add them later once we know how
 		// many entries we have)
 		int numMatches = data.isGroupEntry() ? 5 : 4;
 		for (int i = 0; i < numMatches; i++)
 			tl.removeViewAt(tl.getChildCount() - 1);
 
 		// Add more rows if we need them. We add 2 to the number of
 		// players (name / date rows) to get the number of rows we need
 		while (tl.getChildCount() < numRows + 2) {
 			View newRow;
 			if (data.isGroupEntry())
 				newRow = vi.inflate(R.layout.group_stage_row, null);
 			else
 				newRow = vi.inflate(R.layout.bracket_stage_row, null);
 			tl.addView(newRow);
 		}
 
 		// Remove rows if we had too many...
 		while (tl.getChildCount() > numRows + 2) {
 			tl.removeViewAt(tl.getChildCount() - 1);
 		}
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.view_group_detail, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
