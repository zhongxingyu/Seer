 package io;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
import org.omg.CORBA.Current;


 import InputData.InputData;
 import InputData.Suspect;
 
 public class FileRepository  {
 
 
 	private static BufferedReader br;
 
 	public static InputData load(String path) throws IOException {
 		
 		br = new BufferedReader( new FileReader( new File(path) ) );
 		InputData input = new InputData();
 		String l;
 		List<ScoreData> matrixTemp = new ArrayList<ScoreData>();
         while ((l = br.readLine()) != null) {
         	
         	char letter = l.charAt(0);
         	
         	if (letter == '#') {
         		continue;
         	} else if (Character.isLetter(letter) || letter == '-') {
         		ScoreData sd = new ScoreData(String.valueOf(letter), l.substring(2));
         		matrixTemp.add(sd);
         	} else if (letter == '0') {
         		input.setEvidence(l.substring(2));
         	} else if (Character.isDigit(letter)) {
        		String adnSuspect = l.substring(2); 
        		Suspect s = new Suspect(Integer.parseInt(String.valueOf(letter)), adnSuspect);
         		input.getSuspects().add(s);
         	}
         	
         }
         
         input.setScores( tagMatrix(matrixTemp) );
 		return input;
 			
 	}
 
 	private static Map<String, Map<String, Integer>> tagMatrix(List<ScoreData> matrixTemp) {
 		
 		Map<String, Map<String, Integer>> scores = new HashMap<String, Map<String, Integer>>();
 		Map<String, Integer> s;
 		Iterator<ScoreData> it = matrixTemp.iterator();
 		
 		while (it.hasNext()) {
 			ScoreData sd = (ScoreData) it.next();
 			
 			s = new LinkedHashMap<String, Integer>();
 			String[] scoresBase = sd.getTupleScore().split(",");
 			int index = 0;
 			for (String value: scoresBase) {
 				int val = 0;
 				try {
 					val = Integer.parseInt(value.trim());
 				}
 				catch (NumberFormatException e){
 				}
 				
 				String currentBase = matrixTemp.get(index).getBaseDNA();
 				s.put(currentBase, val);
 				index++;
 			}
 			
 			scores.put(sd.getBaseDNA(), s);
 			
 		}
 		return scores;
 	}
 
 }
