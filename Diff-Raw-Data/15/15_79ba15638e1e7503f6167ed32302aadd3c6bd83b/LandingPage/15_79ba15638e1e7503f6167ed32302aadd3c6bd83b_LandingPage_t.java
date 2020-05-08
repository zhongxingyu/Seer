 package com.mthatcher.starcraft2wcs;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.concurrent.ExecutionException;
 import java.util.zip.GZIPInputStream;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TableLayout;
 
 import com.mthatcher.starcraft2wcs.entry.BracketEntry;
 import com.mthatcher.starcraft2wcs.entry.EntryUtil;
 import com.mthatcher.starcraft2wcs.entry.GroupEntry;
 import com.mthatcher.starcraft2wcs.entry.GroupOrBracketEntry;
 import com.mthatcher.starcraft2wcs.entry.ViewHolder;
 
 public class LandingPage extends Activity {
 
 	private final String DATA_URL = "https://objects.dreamhost.com/sc2wcsapp/data/sqlite.gz";
 	private final String DEBUG_TAG = "LANDING PAGE";
 	public int startPos;
 
 	public enum Division {
 		PREMIER, CHALLENGER
 	}
 
 	public enum Region {
 		AMERICA, EUROPE, KOREA
 	}
 
 	public enum Race {
 		TERRAN, PROTOSS, ZERG, RANDOM
 	}
 
 	public enum Country {
 		AR, AT, AU, BE, CA, CN, CL, DE, DK, ES, FI, FR, GB, KR, MX, NL, NO, NZ, PE, PL, RS, RU, SE, TW, UA, US, UNKNOWN
 	}
 
 	public enum MatchResult {
		WIN, LOSE, STAY, NOTYETPLAYED
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_landing_page);
 
 		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		AsyncTask<String, Integer, ArrayList<ScheduleEntry>> task = null;
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 			task = new DownloadDataAndUpdateDBTask(getApplicationContext())
 					.execute(DATA_URL);
 		} else {
 			// TODO: Handle this.
 		}
 
 		ScheduleAdapter listAdapter = null;
 		try {
 			// listAdapter = new ArrayAdapter<String>(this, R.layout.am_row,
 			// task.get());
 			listAdapter = new ScheduleAdapter(task.get());
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		final ListView listView = (ListView) findViewById(R.id.mainListView);
 		listView.setAdapter(listAdapter);
 		listView.setSelection(startPos);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.landing_page, menu);
 		return true;
 	}
 
 	private class ScheduleAdapter extends BaseAdapter {
 
 		ArrayList<ScheduleEntry> items;
 
 		@Override
 		public boolean hasStableIds() {
 			return true;
 		}
 
 		public ScheduleAdapter(ArrayList<ScheduleEntry> items) {
 			this.items = items;
 		}
 
 		@Override
 		public int getCount() {
 			return items.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return items.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public int getViewTypeCount() {
 			// 0: Group
 			// 1: Bracket
 			return 2;
 		}
 
 		@Override
 		public int getItemViewType(int position) {
 			if (items.get(position).isGroupEntry)
 				return 0;
 			else
 				return 1;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ViewHolder holder;
 			ScheduleEntry item = items.get(position);
 
 			if (convertView == null) {
 				LayoutInflater vi;
 				vi = LayoutInflater.from(getBaseContext());
 				if (item.isGroupEntry) {
 					convertView = vi.inflate(R.layout.group_stage_tbl, parent,
 							false);
 					holder = GroupEntry.getHolder(convertView, 4);
 				} else {
 					convertView = vi.inflate(R.layout.bracket_stage_tbl,
 							parent, false);
 					holder = BracketEntry.getHolder(convertView, 4);
 				}
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			if (item != null) {
 				if (item.isGroupEntry()) {
 					getGroupView(holder, item, convertView);
 				} else {
 					getBracketView(holder, item, convertView);
 				}
 			}
 			return convertView;
 		}
 
 		private void getBracketView(ViewHolder holder, ScheduleEntry item,
 				View convertView) {
 			int winnerColor = 0xFFCCFFCC;
 			int loserColor = 0x00FFFFFF;
 			int numPlayers = item.getNumPlayers();
 			holder.groupName.setText(item.getName());
 			holder.groupName.setCompoundDrawablesWithIntrinsicBounds(
 					item.getTitleDrawable(), 0, 0, 0);
 			holder.groupName.setBackgroundColor(item.getColor());
 			holder.date.setText(item.getTime());
 			int p1i, p2i; // Player 1 and Player 2 indices
 			int i = 0;
 			TableLayout tblV = (TableLayout) convertView;
 			LayoutInflater vi = null;
 			while (holder.size < numPlayers) {
 				if (vi == null) {
 					vi = LayoutInflater.from(getBaseContext());
 					i = holder.size;
 				}
 				View newRow = vi.inflate(R.layout.bracket_stage_row, null);
 				BracketEntry.addRow(newRow, i * 2, i * 2 + 1, holder);
 				tblV.addView(newRow);
 				i++;
 			}
 			i = holder.size + 1;
 			while (holder.size > numPlayers) {
 				BracketEntry.removeRow(holder);
 				tblV.removeViewAt(i--);
 			}
 
 			for (i = 0; i < numPlayers; i++) {
 				if(!(item.getPlayer(i) instanceof BracketEntry))
 					tblV = null;
 				BracketEntry player = (BracketEntry) item.getPlayer(i);
 				p1i = i * 2;
 				p2i = i * 2 + 1;
 
 				holder.playerName.get(p1i).setText(player.getP1Name());
 				holder.playerName.get(p2i).setText(player.getP2Name());
 				holder.race.get(p1i).setCompoundDrawablesWithIntrinsicBounds(
 						EntryUtil.getRaceDrawable(player.getP1Race()), 0, 0, 0);
 				holder.race.get(p2i).setCompoundDrawablesWithIntrinsicBounds(
 						EntryUtil.getRaceDrawable(player.getP2Race()), 0, 0, 0);
 				holder.flag.get(p1i).setCompoundDrawablesWithIntrinsicBounds(
 						EntryUtil.getFlagDrawable(player.getP1Country()), 0, 0,
 						0);
 				holder.flag.get(p2i).setCompoundDrawablesWithIntrinsicBounds(
 						EntryUtil.getFlagDrawable(player.getP2Country()), 0, 0,
 						0);
 				holder.mapScore.get(p1i).setText(
 						EntryUtil.getWinsStr(1, player));
 				holder.mapScore.get(p2i).setText(
 						EntryUtil.getWinsStr(2, player));
 
 				if (player.getWinner() == 1) {
 					holder.playerName.get(p1i).setBackgroundColor(winnerColor);
 					holder.race.get(p1i).setBackgroundColor(winnerColor);
 					holder.flag.get(p1i).setBackgroundColor(winnerColor);
 					holder.playerName.get(p2i).setBackgroundColor(loserColor);
 					holder.race.get(p2i).setBackgroundColor(loserColor);
 					holder.flag.get(p2i).setBackgroundColor(loserColor);
 				} else if (player.getWinner() == 2) {
 					holder.playerName.get(p1i).setBackgroundColor(loserColor);
 					holder.race.get(p1i).setBackgroundColor(loserColor);
 					holder.flag.get(p1i).setBackgroundColor(loserColor);
 					holder.playerName.get(p2i).setBackgroundColor(winnerColor);
 					holder.race.get(p2i).setBackgroundColor(winnerColor);
 					holder.flag.get(p2i).setBackgroundColor(winnerColor);
 				} else {
 					holder.playerName.get(p1i).setBackgroundColor(loserColor);
 					holder.race.get(p1i).setBackgroundColor(loserColor);
 					holder.flag.get(p1i).setBackgroundColor(loserColor);					
 					holder.playerName.get(p2i).setBackgroundColor(loserColor);
 					holder.race.get(p2i).setBackgroundColor(loserColor);
 					holder.flag.get(p2i).setBackgroundColor(loserColor);
 				}
 			}
 		}
 
 		private void getGroupView(ViewHolder holder, ScheduleEntry item, View convertView) {
 			int bgColor = item.getColor();
 			int numPlayers = item.getNumPlayers();
 			holder.groupName.setText(item.getName());
 			holder.groupName.setCompoundDrawablesWithIntrinsicBounds(
 					item.getTitleDrawable(), 0, 0, 0);
 			holder.groupName.setBackgroundColor(bgColor);
 			holder.date.setText(item.getTime());
 			int i = 0;
 			TableLayout tblV = (TableLayout) convertView;
 			LayoutInflater vi = null;
 			while (holder.size < numPlayers) {
 				if (vi == null) {
 					vi = LayoutInflater.from(getBaseContext());
 					i = holder.size;
 				}
 				View newRow = vi.inflate(R.layout.group_stage_row, null);
 				GroupEntry.addRow(newRow, i, holder);
 				tblV.addView(newRow);
 				i++;
 			}
 			i = holder.size + 1;
 			while (holder.size > numPlayers) {
 				GroupEntry.removeRow(holder);
 				tblV.removeViewAt(i--);
 			}
 			
 			for (i = 0; i < numPlayers; i++) {
 				GroupEntry player = (GroupEntry) item.getPlayer(i);
 				bgColor = player.getBackgroundColor();
 				if (player.getPlace() > 0) // TODO: Extract this to EntryUtil
 					holder.rank.get(i).setText(Integer.toString(player.getPlace()));
 				else {
 					holder.rank.get(i).setText("-");
 					bgColor = 0x00FFFFFF;
 				}
 				holder.rank.get(i).setBackgroundColor(bgColor);
 				holder.flag.get(i)
 						.setCompoundDrawablesWithIntrinsicBounds(
 								EntryUtil.getFlagDrawable(player.getCountry()),
 								0, 0, 0);
 				holder.flag.get(i).setBackgroundColor(bgColor);
 				holder.race.get(i).setCompoundDrawablesWithIntrinsicBounds(
 						EntryUtil.getRaceDrawable(player.getRace()), 0, 0, 0);
 				holder.race.get(i).setBackgroundColor(bgColor);
 				holder.playerName.get(i).setText(player.getName());
 				holder.playerName.get(i).setBackgroundColor(bgColor);
 				holder.matchScore.get(i).setText(Integer.toString(player
 						.getMatchesWon())
 						+ "-"
 						+ Integer.toString(player.getMatchesLost()));
 				holder.matchScore.get(i).setBackgroundColor(bgColor);
 				holder.mapScore.get(i)
 						.setText(Integer.toString(player.getMapsWon()) + "-"
 								+ Integer.toString(player.getMapsLost()));
 				holder.mapScore.get(i).setBackgroundColor(bgColor);
 			}
 		}
 	}
 
 	private class DownloadDataAndUpdateDBTask extends
 			AsyncTask<String, Integer, ArrayList<ScheduleEntry>> {
 		private Context context;
 
 		public DownloadDataAndUpdateDBTask(Context applicationContext) {
 			context = applicationContext;
 		}
 
 		@Override
 		protected ArrayList<ScheduleEntry> doInBackground(String... urls) {
 			// 1) Download GZipped SQL
 			InputStream is = downloadGzippedSql(urls[0]);
 			// 2) Un-gzip SQL
 			String sql = unGzipSql(is);
 			// 3) Execute SQL
 			WcsDBHelper dbHelper = updateDB(sql);
 			// 4) Retrieve relevant matches
 			ArrayList<ScheduleEntry> matchNames = getMatchNames(dbHelper);
 			// 5) Update UI
 			return matchNames;
 		}
 
 		private ArrayList<ScheduleEntry> getMatchNames(WcsDBHelper dbHelper) {
 			publishProgress(50);
 			ArrayList<ScheduleEntry> entries = new ArrayList<ScheduleEntry>();
 			SQLiteDatabase db = dbHelper.getReadableDatabase();
 			String table = "schedule";
 			String[] columns = new String[] { "id", "time", "division",
 					"region", "round", "name" };
 			String selection = null;
 			String[] selectionArgs = null;
 			String groupBy = null;
 			String having = null;
 			String orderBy = "time";
 			Cursor c = db.query(table, columns, selection, selectionArgs,
 					groupBy, having, orderBy);
 			publishProgress(60);
 			c.moveToFirst();
 			long currentTime = System.currentTimeMillis();
 			boolean startPosFound = false;
 			while (!c.isLast()) {
 				if (!startPosFound
 						&& Long.parseLong(c.getString(1)) > currentTime) {
 					startPosFound = true;
 					startPos = entries.size() > 2 ? entries.size() - 2 : 0;
 				}
 				entries.add(new ScheduleEntry(c.getString(0), c.getString(1), c
 						.getString(2), c.getString(3), c.getString(4) + ": "
 						+ c.getString(5)));
 				c.move(1);
 			}
 			c.close();
 			publishProgress(70);
 			table = "participants";
 			columns = new String[] { "id", "name", "flag", "race", "place",
 					"matcheswon", "matcheslost", "mapswon", "mapslost",
 					"result" };
 			orderBy = null;
 			for (ScheduleEntry entry : entries) {
 				selection = "scheduleid = " + String.valueOf(entry.getId());
 				c = db.query(table, columns, selection, selectionArgs, groupBy,
 						having, orderBy);
 				c.moveToFirst();
 				if (c.getCount() == 0) {
 					c.close();
 					continue;
 				}
 				entry.setIsGroupEntry(true);
 				while (!c.isLast()) {
 					entry.addPlayer(new GroupEntry(c.getString(1), c
 							.getString(2), c.getString(3), c.getString(4), c
 							.getInt(5), c.getInt(6), c.getInt(7), c.getInt(8),
 							c.getString(9)));
 					c.move(1);
 				}
 				entry.addPlayer(new GroupEntry(c.getString(1), c.getString(2),
 						c.getString(3), c.getString(4), c.getInt(5), c
 								.getInt(6), c.getInt(7), c.getInt(8), c
 								.getString(9)));
 				c.close();
 			}
 			publishProgress(80);
 			table = "matches";
 			columns = new String[] { "id", "winner", "player1name",
 					"player2name", "player1race", "player2race", "player1flag",
 					"player2flag", "numgames", "player1wins", "player2wins" };
 			orderBy = null;
 			for (ScheduleEntry entry : entries) {
 				selection = "scheduleid = " + String.valueOf(entry.getId())
 						+ " AND matchtype = \"bracket\"";
 				c = db.query(table, columns, selection, selectionArgs, groupBy,
 						having, orderBy);
 				c.moveToFirst();
 				if (c.getCount() == 0) {
 					c.close();
 					continue;
 				}
 				entry.setIsGroupEntry(false);
 				while (!c.isLast()) {
 					entry.addPlayer(new BracketEntry(c.getString(2), c
 							.getString(3), c.getString(4), c.getString(5), c
 							.getString(6), c.getString(7), c.getString(1), c
 							.getString(9), c.getString(10)));
 					c.move(1);
 				}
 				entry.addPlayer(new BracketEntry(c.getString(2),
 						c.getString(3), c.getString(4), c.getString(5), c
 								.getString(6), c.getString(7), c.getString(1),
 						c.getString(9), c.getString(10)));
 				c.close();
 			}
 			publishProgress(90);
 			return entries;
 		}
 
 		private WcsDBHelper updateDB(String sql) {
 			WcsDBHelper dbHelper = new WcsDBHelper(context);
 			publishProgress(30);
 			dbHelper.updateDB(dbHelper.getWritableDatabase(), sql);
 			publishProgress(40);
 			return dbHelper;
 		}
 
 		private String unGzipSql(InputStream is) {
 			try {
 				GZIPInputStream gzis = new GZIPInputStream(is);
 				InputStreamReader isreader = new InputStreamReader(gzis,
 						"US-ASCII");
 				BufferedReader buffReader = new BufferedReader(isreader);
 				String line = "";
 				StringBuilder strBuilder = new StringBuilder();
 				while ((line = buffReader.readLine()) != null) {
 					strBuilder.append(line);
 				}
 				publishProgress(20);
 				return strBuilder.toString();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		private InputStream downloadGzippedSql(String urlAsStr) {
 			// try{
 
 			try {
 				URL url = new URL(urlAsStr);
 				HttpURLConnection conn = (HttpURLConnection) url
 						.openConnection();
 				// Timeouts are in ms..
 				conn.setReadTimeout(10000);
 				conn.setConnectTimeout(15000);
 				conn.setRequestMethod("GET");
 				conn.setDoInput(true);
 				conn.connect();
 				publishProgress(10);
 				int response = conn.getResponseCode();
 				Log.d(DEBUG_TAG, "The response is: " + response);
 				return conn.getInputStream();
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			super.onProgressUpdate(values);
 		}
 	}
 
 	private class ScheduleEntry {
 		private int id;
 		private long time;
 		private Division division;
 		private Region region;
 		private String name;
 		private boolean isGroupEntry;
 		private ArrayList<GroupOrBracketEntry> players;
 
 		public ScheduleEntry(String id, String time, String division,
 				String region, String name) {
 			this.id = Integer.parseInt(id);
 			this.time = Long.parseLong(time);
 			this.division = setDivisionFromString(division);
 			this.region = setRegionFromString(region);
 			this.name = name;
 			this.isGroupEntry = true;
 			players = new ArrayList<GroupOrBracketEntry>();
 		}
 
 		public int getColor() {
 			switch (region) {
 			case AMERICA:
 				return 0xFFFFC5DA;
 			case EUROPE:
 				return 0xFFF7FFAC;
 			case KOREA:
 				return 0xFFD1FDAB;
 			default:
 				return 0xFFFFFFFF;
 			}
 		}
 
 		public void setIsGroupEntry(boolean isGroupEntry) {
 			this.isGroupEntry = isGroupEntry;
 		}
 
 		public boolean isGroupEntry() {
 			return isGroupEntry;
 		}
 
 		public GroupOrBracketEntry getPlayer(int n) {
 			return players.get(n);
 		}
 
 		public int getNumPlayers() {
 			return players.size();
 		}
 
 		public int getId() {
 			return id;
 		}
 
 		public void addPlayer(GroupOrBracketEntry player) {
 			players.add(player);
 		}
 
 		public String getTime() {
 			return DateFormat.getDateTimeInstance().format(new Date(time));
 		}
 
 		public int getTitleDrawable() {
 			switch (region) {
 			case AMERICA:
 				if (division == Division.PREMIER)
 					return R.drawable.wcs_logo_am_premier;
 				else if (division == Division.CHALLENGER)
 					return R.drawable.wcs_logo_am_challenger;
 				else
 					return R.drawable.wcs_logo_am_plain;
 			case EUROPE:
 				if (division == Division.PREMIER)
 					return R.drawable.wcs_logo_eu_premier;
 				else if (division == Division.CHALLENGER)
 					return R.drawable.wcs_logo_eu_challenger;
 				else
 					return R.drawable.wcs_logo_eu_plain;
 			case KOREA:
 				if (division == Division.PREMIER)
 					return R.drawable.wcs_logo_kr_premier;
 				else if (division == Division.CHALLENGER)
 					return R.drawable.wcs_logo_kr_challenger;
 				else
 					return R.drawable.wcs_logo_kr_plain;
 			default: // TODO: Add leagues to generic logo?
 				return R.drawable.wcs_logo_nowhere_plain;
 			}
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		private Division setDivisionFromString(String division) {
 			if (division.equalsIgnoreCase("P"))
 				return Division.PREMIER;
 			else if (division.equalsIgnoreCase("C"))
 				return Division.CHALLENGER;
 			else
 				return null;
 		}
 
 		private Region setRegionFromString(String region) {
 			if (region.equalsIgnoreCase("AM"))
 				return Region.AMERICA;
 			else if (region.equalsIgnoreCase("KR"))
 				return Region.KOREA;
 			else if (region.equalsIgnoreCase("EU"))
 				return Region.EUROPE;
 			else
 				return null;
 		}
 	}
 }
