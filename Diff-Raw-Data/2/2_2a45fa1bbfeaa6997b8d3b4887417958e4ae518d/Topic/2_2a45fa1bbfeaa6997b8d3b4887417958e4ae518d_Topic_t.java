 package team2.mainapp;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 public class Topic implements Comparable<Topic> {
 	private String title;
 	private ArrayList<String> URLS;
 	private ArrayList<KeyWord> keyWords;
 	private String date;
 	private int uid;
 	private int artsLastHour;
 	
 	Topic (String title, String date,  int artsLH, ArrayList<String> URLS, ArrayList<KeyWord> keyWords, String uid) {
 		this.uid = (int) Integer.parseInt(uid);
 		this.keyWords = keyWords;
 		this.title = title;
 		this.URLS = URLS;
 		this.date = date;
 		this.artsLastHour = artsLH;
 	}
 	
 	public int compareTo(Topic temp) {
		return temp.artsLastHour - this.artsLastHour;
 	}
 
 	public int getArtsLastHour() {
 		return artsLastHour;
 	}
 
 	public void setArtsLastHour(int artsLastHour) {
 		this.artsLastHour = artsLastHour;
 	}
 
 	public int getUid() {
 		return uid;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public String getDate() {
 		return date;
 	}
 
 	public void setDate(String date) {
 		this.date = date;
 	}
 
 	public ArrayList<String> getURLS() {
 		return URLS;
 	}
 
 	public ArrayList<KeyWord> getKeyWords() {
 		return keyWords;
 	}
 
 
 }
 
 class KeyWord {
 	String word;
 	double sentiment;
 
 	KeyWord(String w, String s) {
 		word = w;
 		sentiment = Double.parseDouble(s);
 	}
 
 	public String getWord(){
 		return word;
 	}
 
 	public double getSentiment() {
 		return sentiment;
 	}
 }
