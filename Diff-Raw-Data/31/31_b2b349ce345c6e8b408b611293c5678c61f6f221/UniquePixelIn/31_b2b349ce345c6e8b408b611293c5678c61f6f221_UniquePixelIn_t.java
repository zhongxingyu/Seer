 package util;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 public class UniquePixelIn {
 	
 	private String FILE_NAME = "";
 	char[] letters;
 	int[][][] locations;
 	int[][] colours;
 	
 	public UniquePixelIn(String FILE_NAME){
 		this.FILE_NAME = FILE_NAME;
 		loadArrays();
 		System.out.println("Values Loaded.");
 	}
 	
 	private void loadArrays(){
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(FILE_NAME));
 			int lines = 0;
 			String line = "";
 			while((line = in.readLine()) != null){
 				lines++;
 			}
 			letters = new char[lines];
 			locations = new int[lines][2][2];
 			colours = new int[lines][2];
 			int i = 0;
			in = new BufferedReader(new FileReader(FILE_NAME));
 			while((line = in.readLine()) != null){
 				String[] splitString = line.split(":");
 				letters[i] = splitString[0].charAt(0);
				System.out.println(letters[i]);
 				locations[i][0][0] = Integer.parseInt(splitString[1]);
				System.out.println(locations[i][0][0]);
 				locations[i][0][1] = Integer.parseInt(splitString[2]);
				System.out.println(locations[i][0][1]);
 				locations[i][1][0] = Integer.parseInt(splitString[3]);
				System.out.println(locations[i][1][0]);
 				locations[i][1][1] = Integer.parseInt(splitString[4]);
				System.out.println(locations[i][1][1]);
 				colours[i][0] = Integer.parseInt(splitString[6]);
				System.out.println(colours[i][0]);
 				colours[i][1] = Integer.parseInt(splitString[7]);
				System.out.println(colours[i][1]);
 				i++;
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public char thisIs(int[][] imageArray){
 		
 		for(int i=0; i<letters.length; i++){
 			for(int j=0; j<2; j++){
 				int x = locations[i][j][0];
 				int y = locations[i][j][1];
				//System.out.println(x+","+y);
				//System.out.println(imageArray[x-1][y-1]+" : "+ colours[i][j]);
 				if(imageArray[x][y] == colours[i][j]){
 					return letters[i];
 				}
 			}
 		}
 		
 		return 'X';
 	}
 	
 }
