 package test;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import play.Play;
 import quote.Quote;
 import configuration.PlayConfig;
 import configuration.PlayType;
 
 public class Test {
 	
 	public static void main(String [] args) throws IOException {
 		PlayConfig config = new PlayConfig(PlayType.DEFAULT);
 		if (args[0].length() == 0) {
 			return;
 		}
 		config.set("fileName", args[0]);
 		Play play = new Play(config);
 		
 		// Construct the features
 		play.constructFeatures();
 		
 		System.out.println("-----");
 		
 		charPrintTest(play);
 		quoteAttrTest(play);
 		
 	}
 
 	private static void quoteAttrTest(Play play) {
 		// TODO Auto-generated method stub
 	}
 
 	private static void charPrintTest(Play play) {
 		for (character.Character dude : play.returnCharacters(null, null).values()) {
 			System.out.println("Name: " + dude.getName());
 			System.out.println("Description: " + dude.getDescription());
 			for (Quote q : dude.getQuotes()) {
 				System.out.println(q.getRawQuote());
 			}
 		}
 	}
 }
