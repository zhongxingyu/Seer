 package com.example.seamerchant.game;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import android.content.Context;
 
 public class Scores implements Comparable<Scores> {
	public static final String FILENAME = "scores.xml";
 	private String mName;
 	private int mScore;
 
 	public Scores(String name, int score) {
 		mName = name;
 		mScore = score;
 
 	}
 
 	public void writeScore(Context ctx) {
 		DataOutputStream out;
 		try {
 			out = new DataOutputStream(ctx.openFileOutput(FILENAME, Context.MODE_APPEND));
 			out.writeInt(getScore());
 			out.writeChar(' ');
 			out.writeUTF(mName);
 			out.close();
 		} catch (IOException e) {
 		}
 	}
 
 	@Override
 	public int compareTo(Scores another) {
		if (this.getScore() > another.getScore())
			return 1;
		return -1;
 	}
 
 	public Integer getScore() {
 		return mScore;
 	}
 
 	public void setScore(int score) {
 		this.mScore = score;
 	}
 
 	public String getName() {
 		return mName;
 	}
 }
