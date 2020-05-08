 package com.secondhand.model.resource;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 
 import com.secondhand.debug.MyDebug;
 
 public final class HighScoreList {
 
 	private static HighScoreList instance;
 	
 	private static final String FILE_NAME = "high_score.dat";
 	private static final String DEFAULT_FILE_NAME = "highScore";
 	
 	
 	
 	private boolean highScoreFileExists() {
 		// check if the file is saved on the SD-card
 		return new File(context.getFilesDir() + "/" + FILE_NAME).exists();
 	}
 	
 	
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
 	
 	private void updateFile() {
 		BufferedWriter writer = null;
 		try {
 			writer = new BufferedWriter(new OutputStreamWriter(
 					context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)));
 
 			
 			for(Entry entry: this.highScoreList) {
 				writer.write(entry.name + "\n");
 				writer.write(entry.score + "\n");
 			}
 		}
 		catch (IOException e) {
 			MyDebug.e("could not load high score file",  e);
 		} finally {
 			try {
 				writer.close();
 			} catch (IOException e) {
 				MyDebug.e("could not close high score file for writing",  e);
 			}
 		}
 	}
 	
 	public void insertInHighScoreList(final Entry newEntry) {
 		for(int i = 0; i < highScoreList.size(); ++i) {
 			if(newEntry.score > this.highScoreList.get(i).score) {
 				this.highScoreList.add(i, newEntry);
 				// remove the last entry
 				this.highScoreList.remove(this.highScoreList.size() - 1);
 				updateFile();
 				return;
 			}
 		}
 	}
 	
 	private void readFile() {
 
 		BufferedReader reader;
 		try {
 			
 			if(!this.highScoreFileExists())
 				// read the default one. 
 				reader = new BufferedReader(new InputStreamReader(context.getAssets().open(DEFAULT_FILE_NAME)));
 			else
 				reader = new BufferedReader(new InputStreamReader(this.context.openFileInput(FILE_NAME)));
 			
 			while (true) {
 
 				final String name = reader.readLine();
 				if(name == null) 
 					break;
 				final int score =  Integer.parseInt(reader.readLine().trim());
 
 				this.highScoreList.add(new Entry(name, score));
 			}
 		}
 
 		catch (IOException e) {
 			MyDebug.e("could not load high score file",  e);
 		}
 
 	}
 	
 	public void initialize(final Context context) {
 		this.context = context;
 		
 		this.highScoreList = new ArrayList<Entry>();
 
 		readFile();
 
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
