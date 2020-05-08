 package com.secondhand.resource;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.secondhand.debug.MyDebug;
 
 import android.content.Context;
 
 public final class HighScoreList {
 
 	private static HighScoreList instance;
 	
 	private Context context;
 	
 	public static HighScoreList getInstance() {
 		if(instance == null) {
 			instance = new HighScoreList();
 		}
 		return instance;
 	}
 	
 	// stored from highest to lowest position in list.
 	private List<Entry> highScoreList;
 	
 	public List<Entry> getHighScoreList() {
 		return this.highScoreList;
 	}
 	
 	public boolean madeItToHighScoreList(final int newScore) {
 		for(int i = this.highScoreList.size() - 1; i >= 0; --i) {
			final int score = this.highScoreList.get(i).score;
 			if(newScore > score)
 				return true;
 		}
 		return false;
 	}
 	
 	public void initialize(final Context context) {
 		this.context = context;
 		
 		this.highScoreList = new ArrayList<Entry>();
 
 
 		BufferedReader reader;
 		try {
 			reader = new BufferedReader(new InputStreamReader(this.context.getAssets().open("highScore")));
 
 			while (true) {
 
 				final String name = reader.readLine();
 				if(name == null) 
 					break;
 				final int score =  Integer.parseInt(reader.readLine().trim());
 
 				this.highScoreList.add(new Entry(name, score));
 
 			}
 		}
 
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			MyDebug.e("could not load high score file",  e);
 		}
 	}
 	
 	// entry in the high score list. 
 	public static class Entry {
 		public final String name;
 		public final int score;
 		
 		public Entry(final String name, final int score ) {
 			this.name = name;
 			this.score = score;
 		}
 	}
 }
