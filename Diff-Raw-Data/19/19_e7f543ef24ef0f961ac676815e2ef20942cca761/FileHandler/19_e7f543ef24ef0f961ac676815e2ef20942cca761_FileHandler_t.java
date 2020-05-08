 package se.chalmers.towerdefence.files;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import se.chalmers.towerdefence.model.HighScore;
 
 public class FileHandler implements IFileHandler{
 	private static FileOutputStream fos = null;
 	private static ObjectOutputStream out = null;
 	private static FileInputStream fis = null;
 	private static ObjectInputStream in = null;
 
 	public void saveHighScore(HighScore hs) {
 		List <HighScore> highScores=null;
 		if (isNewHighScore(hs) == false) {
 			//do nothing
 		}else {
 			try {
 				highScores = readFromHighScoreFile();
 				if (isNewHighScore(hs) == true && findHighScore(hs) != null) {
 					highScores.remove(findHighScore(hs));
 				}
 
 				if(!(highScores==null)) {
 					highScores.add(hs);
 				}else{
 					throw new FileNotFoundException();
 				}
 
 			}catch (FileNotFoundException e) {
 				highScores = new ArrayList <HighScore> ();
 				highScores.add(hs);
 
 			}
 
 			try {
 				fos = new FileOutputStream("highScore.txt");
 				out = new ObjectOutputStream(fos);
 				out.writeObject(highScores);
 				out.close();
 			} catch (IOException e) {
 
 			}
 		}
 
 	}
 	public boolean isNewHighScore(HighScore hs) {
 		HighScore oldHighScore = findHighScore(hs);
 		if (oldHighScore == null) {
 			return true;
 		}else {
 			return (hs.getPoints() > oldHighScore.getPoints());
 		}
 
 	}
 
 
 	public HighScore findHighScore(HighScore hs) {
 		List<HighScore> highScores = null;
 		String levelName = hs.getLevelName();
 		try {
 			highScores = readFromHighScoreFile();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (highScores == null) {
 			return null;
 		}else {
 
 			for (int i = 0; i < highScores.size(); i++) {
 				if (highScores.get(i).getLevelName().equals(levelName)) {
 					return highScores.get(i);
 				}
 			}
 		} 
 		return null;
 
 	}
 	
 	
 	public int getHighScore(String levelName) {
 		List<HighScore> highScores = null;
 		int highScore = -1;
 		try {
 			highScores = readFromHighScoreFile();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		int index = -1;
		
		try{
			for (int i=0; i<highScores.size(); i++ ) {
				
				if (highScores.get(i).getLevelName().equals(levelName)) {
					index = highScores.indexOf(highScores.get(i));
					highScore = highScores.get(index).getPoints();
				}
			}	
		} catch(NullPointerException e){
 			
 		}
		
		
 		return highScore;
 		
 	}
 	
 	public boolean isLevelUnlocked(String levelName) {
 		int points = getHighScore(levelName);
 		if(points == -1) {
 			return false;
 		}else{
 			return true;
 		}	
 	}
 	
 	public List <HighScore> readFromHighScoreFile() throws FileNotFoundException {
 		List<HighScore> highScores = null;
 		try {
 			fis = new FileInputStream("highScore.txt");
 			in = new ObjectInputStream(fis);
 			highScores = (List <HighScore>) in.readObject();
 			in.close();
 		} catch (IOException e) {
 			
 		} catch (ClassNotFoundException e) {
 			
 		
 	}
 		return highScores;
 	
 	}
 	
 	public String getWavesFromFile(String fileName) {
 		String allWaves = null;
 		try{
 			fis = new FileInputStream(fileName);
 			DataInputStream in = new DataInputStream(fis);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			allWaves = br.readLine();
 			
 		}catch (FileNotFoundException e){
 			System.out.println("file not found: level1");
 
 		} catch (IOException e) {
 			System.out.println("failed to read line");
 			e.printStackTrace();
 		}
 		return allWaves;
 
 	}
 }
