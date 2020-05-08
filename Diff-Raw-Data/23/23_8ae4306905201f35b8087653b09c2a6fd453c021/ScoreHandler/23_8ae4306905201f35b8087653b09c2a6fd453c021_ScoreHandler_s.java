 package com.example.seamerchant.game;
 
 import java.io.DataInputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.content.Context;
 
 public class ScoreHandler {
 
 	// TODO allow using scores from network should build a simple webapp for
 	// that
 	public ScoreHandler() {
 
 	}
 
 	public static ArrayList<Scores> getScoreFileContents(Context ctx) {
 		ArrayList<Scores> ret = new ArrayList<Scores>();
 		try {
 			DataInputStream in = new DataInputStream(ctx.openFileInput(Scores.FILENAME));
 			try {
 				for (;;) {
 					int score = in.readInt();
					in.read();
 					String name = in.readUTF();
 					ret.add(new Scores(name, score));
 				}
 			} catch (EOFException e) {
 				// reading all from file nothing to worry about
 			}
 			in.close();
 
 		} catch (IOException e) {
 
 		}
 		return ret;
 	}
 
 	public void writeScores(ArrayList<Scores> list, Context ctx) {
 		for (Scores score : list) {
 			score.writeScore(ctx);
 		}
 	}
 
 	public static ArrayList<Scores> sort(ArrayList<Scores> list) {
 		Collections.sort(list);
 		return list;
 	}
 }
