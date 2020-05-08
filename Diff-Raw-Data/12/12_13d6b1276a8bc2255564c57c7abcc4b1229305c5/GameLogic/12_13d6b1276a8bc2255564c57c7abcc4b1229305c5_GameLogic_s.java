 package vn.hust.zoo.logic;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 
 import android.app.Activity;
 import android.util.Log;
 import android.widget.Button;
 
 public class GameLogic {
 	private static int level = 1;
 	
 	private static String animalName;
 	private static String animalNameAcc;
 	private static String animalHint;
 	private static int animalImgID;
 	private static int gameWinStar;
 	
 	private static ArrayList<String> answer = new ArrayList<String>();
 	private static ArrayList<String> all = new ArrayList<String>();
 
 	private static Random r = new Random();
 	private static Indicator mIndicator = new Indicator();
 	
 	private static int correctCharToGo;
 	private static int wrongAnswer;
 	
 	// set correct Name + Level
 	public static void initLevel(Activity mActivity, int level){
 		Log.d("Level","" + level);
 		GameLogic.level = level;
 		GameLogic.animalImgID = mActivity.getResources().getIdentifier("image_" + level, "drawable", mActivity.getPackageName());
 		GameLogic.animalName = mActivity.getResources().getString(mActivity.getResources().getIdentifier("name_" + level, "string", mActivity.getPackageName()));
 		GameLogic.animalNameAcc = mActivity.getResources().getString(mActivity.getResources().getIdentifier("name_acc_" + level, "string", mActivity.getPackageName()));
 	
 		GameLogic.animalHint = mActivity.getResources().getString(mActivity.getResources().getIdentifier("hint_" + level + "_" + (new Random()).nextInt(3), "string", mActivity.getPackageName()));
 		
 		GameLogic.wrongAnswer = 0;
 	}
 	
 	public static void loadStar(Activity mActivity){
 		int star = 3 - 3*GameLogic.wrongAnswer/GameLogic.animalName.length();
 		star = 1 > star ? 1 : star;
 		GameLogic.gameWinStar =  mActivity.getResources().getIdentifier("star_" + star + "_big", "drawable", mActivity.getPackageName());
 	}
 	
 	public static void initCharacter(){
 		String[] b = animalName.split("");
 		correctCharToGo = 0;
 		answer.clear();
 		all.clear();
 		
 		for (String string : b) {
 			answer.add(string);
 			if(!string.equals(" ") && !string.equals("")) {
 				all.add(string);
 				correctCharToGo++;
 			}
 		}
 		answer.remove(0);
 		
 		for(int i = all.size(); i < 8; i++){
 			String c = ""  + ((char)(r.nextInt(26) + 'a'));
 			all.add(c);
 		}
 		
 		Log.d("Answer choice","" + all.size());
 		
 		Collections.shuffle(all);
 	}
 	
 	public static void addPlayerAnswer(Button b){
 		mIndicator.addPlayerAnswerButton(b);
 	}
 	public static boolean compare(){
 		if(answer.size() != mIndicator.getCharAnswer().size()) return false;
 		for(int i = 0; i < answer.size(); i++)
 			if(!answer.get(i).equals((((Button)mIndicator.getCharAnswer().get(i)).getText()))) return false;
 		return true;
 	}
 	public static boolean isAnswerFullOfChar(){
 		return mIndicator.getCharAnswer().size() >= correctCharToGo;
 	}
 	public static boolean answer(Button b){
 		// full char
 		if(isAnswerFullOfChar()) return false; 
 		mIndicator.addCharAnswerButton(b);
 		Log.d("Answer","" + b.getText());
 		return compare();
 	}
 	public static void delete(){
 		mIndicator.removeCharAnswerButton();
 	}
 	public static void deleteAnswer(Button b){
 		wrongAnswer++;
 		mIndicator.deleteAnswer(b);
 	}
 	public static void clear(){
 		mIndicator.clear();
 	}
 	public static void displayAnimalNameAcc(){
 		mIndicator.hide();
 	}
 	
 	public static ArrayList<String> getAnswer(){return GameLogic.answer;}
 	public static ArrayList<String> getAll(){return GameLogic.all;}
 	public static int getLevel(){return GameLogic.level;}
 	public static int getImageID(){return GameLogic.animalImgID;}
 	public static String getNameAcc(){return GameLogic.animalNameAcc;}
 	public static String getHint(){return GameLogic.animalHint;}
 	public static int getStar(){return GameLogic.gameWinStar;}
 	
 	public static Indicator getIndicator(){return GameLogic.mIndicator;}
 }
