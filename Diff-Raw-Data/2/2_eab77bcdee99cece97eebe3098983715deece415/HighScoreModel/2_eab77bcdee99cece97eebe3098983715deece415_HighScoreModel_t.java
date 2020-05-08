 package inputOutput;
 
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import savePath.SavePath;
 
 /**
  * Models the highscores
  * @author Vidar Eriksson
  *
  */
 public class HighScoreModel {
 	private HighScoreWrapper h = null;
	/**
 	 * Creates a new HighScoreModel.
 	 */
 	public HighScoreModel(){
 		h = new HighScoreWrapper();
 	}
 	/**
 	 * Returns the lengths of the highscore list.
 	 * @return the lengths of the highscore list.
 	 */
 	public int getLenght(){
 		return h.getLenght();	
 	}
 	/**
 	 * Fetches a name from a given position.
 	 * @param pos the position to fetch the name from.
 	 * @return the name at the position.
 	 */
 	public String getName(int pos){
 		return h.getName(pos);
 	}
 	/**
 	 * Fetches a score from a given position.
 	 * @param pos the position to fetch the score from.
 	 * @return the score at the position.
 	 */
 	public String getScore(int pos){
 		return h.getScore(pos);
 	}
 	/**
 	 * Adds a new score to the highscore list. This will sort the in the new score at the proper place.
 	 * @param newScore the score to be added.
 	 */
 	public static void addNewScore(long newScore){
 		HighScoreWrapper.writeScore(newScore);
 	}
 	/**
 	 * 
 	 * @return <code>true<code> if there exist no previously score. 
 	 */
 	public boolean existsNoScore() {
 		return h.isNull();
 	}
 
 	private static class HighScoreWrapper{
 		private static String[][] s = readScore();
 		private static final String DATA_DIVIDER1 = "#";
 		private static final String DATA_DIVIDER2 = "##";
 
 		private boolean isNull(){
 			return s==null;
 		}
 		private int getLenght(){
 			return s.length;
 		}
 		private String getName(int pos){
 			return s[pos][0];
 		}
 		private String getScore(int pos){
 			return s[pos][1];
 		}
 		private static String[][] readScore() {
 			String tempg = null;
 			try {
 				File file = new File(SavePath.highScore());
 				if(file.exists()) {
 					FileReader fileReader = new FileReader(file.getAbsoluteFile());
 					BufferedReader bufferedReader = new BufferedReader(fileReader);
 					tempg=bufferedReader.readLine();
 					bufferedReader.close();
 					fileReader.close();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			return createScoreList(tempg);
 		}
 		private static String[][] createScoreList(String string){
 			if (string==null){
 				return null;
 			} else {
 				int savedScores = numbersOfSymbols(string, DATA_DIVIDER2);
 				String temp[][] = new String[savedScores][2];
 				
 				for (int a=0; a < savedScores; a++){
 					for (int b=0; b < 2; b++){
 						String indexThingie=DATA_DIVIDER1;
 						if(b==1){
 							indexThingie+=DATA_DIVIDER1;
 						}
 						temp[a][b]=string.substring(0, string.indexOf(indexThingie));
 						string = string.substring(string.indexOf(indexThingie)+indexThingie.length(), string.length());
 					}
 				}			
 				return temp;
 			}
 		}
 		
 		private static int numbersOfSymbols(String s, String symbol){
 			int number = 0;
 			
 				while (s.indexOf(symbol)>0){
 					number++;
 					s=s.substring(s.indexOf(symbol)+1, s.length());
 				}			
 			return number;
 		}
 		
 		private static void writeScore(long newScore) {
 			try {				 
 				String scoreToAdd[][] = {{SettingsModel.getUserName(), ""+newScore}};
 				
 				String content=convertToSaveableString(insertSorted(scoreToAdd, s));
 				
 				File file = new File(SavePath.highScore());
 	 
 				if (!file.exists()) {
 					file.createNewFile();
 				}
 	 
 				FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
 				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
 				bufferedWriter.write(content);
 				bufferedWriter.close();
 				fileWriter.close();
 	 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		
 			s=readScore();
 		}
 		private static String[][] insertSorted(String[][] scoreToAdd,
 				String[][] previousScore) {
 			if (previousScore==null){
 				return scoreToAdd;
 			}
 			
 			String temp[][]=new String[previousScore.length+1][2];
 			
 			boolean hasInserted = false;
 			int intScoreToAdd = Integer.parseInt(scoreToAdd[0][1]);
 			for (int a=0, previousScoreIndex=0;
 					a<temp.length && previousScoreIndex<previousScore.length; a++){
 				if (!hasInserted &&  intScoreToAdd 
 						>= Integer.parseInt(previousScore[previousScoreIndex][1])){
 					temp[a][0]=scoreToAdd[0][0];
 					temp[a][1]=scoreToAdd[0][1];
 					hasInserted=true;
 				} else {
 					temp[a][0]=previousScore[previousScoreIndex][0];
 					temp[a][1]=previousScore[previousScoreIndex][1];
 					previousScoreIndex++;
 				}
 			}
 			if (!hasInserted){
 				temp[temp.length-1][0]=scoreToAdd[0][0];
 				temp[temp.length-1][1]=scoreToAdd[0][1];
 			}
 			
 			return temp;
 		}
 		private static String convertToSaveableString(String string[][]){
 			String temp="";
 			for (int a=0; a < string.length; a++ ){
 				for (int b=0; b < 2; b++ ){
 					if (b==0){
 						temp += string[a][b]+DATA_DIVIDER1;
 					} else {
 						temp += string[a][b]+DATA_DIVIDER2;
 					}
 				}
 			}
 			return temp;
 			
 		}
 	
 
 	}
 
 }
