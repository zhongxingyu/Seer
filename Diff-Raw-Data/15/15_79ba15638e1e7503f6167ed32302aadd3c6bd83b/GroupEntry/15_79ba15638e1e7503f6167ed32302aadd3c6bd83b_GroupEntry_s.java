 package com.mthatcher.starcraft2wcs.entry;
 
 import android.view.View;
 import android.widget.TextView;
 
 import com.mthatcher.starcraft2wcs.LandingPage.Country;
 import com.mthatcher.starcraft2wcs.LandingPage.MatchResult;
 import com.mthatcher.starcraft2wcs.LandingPage.Race;
 import com.mthatcher.starcraft2wcs.R;
 
 public class GroupEntry implements GroupOrBracketEntry{
 	private String name;
 	private Country country;
 	private Race race;
 	private int place;
 	private int matchesWon;
 	private int matchesLost;
 	private int mapsWon;
 	private int mapsLost;
 	private MatchResult result;
 	private static int[] rowIds = { R.id.group_row_1, R.id.group_row_2,
 		R.id.group_row_3, R.id.group_row_4, R.id.group_row_5,
 		R.id.group_row_6, R.id.group_row_7, R.id.group_row_8,
 		R.id.group_row_9, R.id.group_row_10, R.id.group_row_11,
 		R.id.group_row_12, R.id.group_row_13, R.id.group_row_14,
 		R.id.group_row_15, R.id.group_row_16, R.id.group_row_17,
 		R.id.group_row_18, R.id.group_row_19, R.id.group_row_20,
 		R.id.group_row_21, R.id.group_row_22, R.id.group_row_23,
 		R.id.group_row_24, R.id.group_row_25, R.id.group_row_26,
 		R.id.group_row_27, R.id.group_row_28, R.id.group_row_29,
 		R.id.group_row_30, R.id.group_row_31, R.id.group_row_32 };
 
 	public GroupEntry(String name, String country, String race,
 			String place, int matchesWon, int matchesLost, int mapsWon,
 			int mapsLost, String result) {
 		this.name = name;
 		this.country = EntryUtil.getCountryFromString(country);
 		this.race = EntryUtil.getRaceFromString(race);
 		this.place = place.length() > 0 ? Integer.parseInt(place) : -1;
 		this.matchesWon = matchesWon;
 		this.matchesLost = matchesLost;
 		this.mapsWon = mapsWon;
 		this.mapsLost = mapsLost;
 		this.result = getResultFromString(result);
 	}
 
 	public Country getCountry(){
 		return country;
 	}
 	
 	public int getBackgroundColor() {
 		switch (result) {
 		case WIN:
 			return 0xFFCCFFCC;
 		case LOSE:
 			return 0xFFFFDDAA;
 		default:
 			return 0xFFFFFFFF;
 		}
 	}
 	
 	public Race getRace(){
 		return race;
 	}
 
 	private MatchResult getResultFromString(String result) {
 		if (result.equalsIgnoreCase("up"))
 			return MatchResult.WIN;
		else if (result.equalsIgnoreCase("staydown"))
 			return MatchResult.LOSE;
 		else
 			return MatchResult.NOTYETPLAYED;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public int getPlace() {
 		return place;
 	}
 
 	public int getMatchesWon() {
 		return matchesWon;
 	}
 
 	public int getMatchesLost() {
 		return matchesLost;
 	}
 
 	public int getMapsWon() {
 		return mapsWon;
 	}
 
 	public int getMapsLost() {
 		return mapsLost;
 	}
 
 	public static ViewHolder getHolder(View convertView, int numEntrants) {
 		ViewHolder holder = new ViewHolder();
 		holder.isGroupHolder = true;
 		holder.groupName = (TextView) convertView.findViewById(R.id.schedule_name);
 		holder.date = (TextView) convertView.findViewById(R.id.schedule_date);
 		holder.size = 0;
 		for(int i = 0; i < numEntrants; i++){
 			addRow(convertView.findViewById(rowIds[i]), i, holder);
 		}
 		return holder;
 	}
 	
 	public static void addRow(View currentRow, int i, ViewHolder holder) {
 		holder.playerName.add(i, (TextView) currentRow.findViewById(R.id.group_player_name));
 		holder.rank.add(i, (TextView) currentRow.findViewById(R.id.group_player_rank));
 		holder.race.add(i, (TextView) currentRow.findViewById(R.id.group_player_race));
 		holder.flag.add(i, (TextView) currentRow.findViewById(R.id.group_player_flag));
 		holder.matchScore.add(i, (TextView) currentRow.findViewById(R.id.group_player_match_score));
 		holder.mapScore.add(i, (TextView) currentRow.findViewById(R.id.group_player_map_score));
 		holder.size++;
 	}
 	
 	public static void removeRow(ViewHolder holder){
 		int i = --holder.size;
 		holder.playerName.remove(i);
 		holder.rank.remove(i);
 		holder.flag.remove(i);
 		holder.race.remove(i);
 		holder.matchScore.remove(i);
 		holder.mapScore.remove(i);
 	}
 }
