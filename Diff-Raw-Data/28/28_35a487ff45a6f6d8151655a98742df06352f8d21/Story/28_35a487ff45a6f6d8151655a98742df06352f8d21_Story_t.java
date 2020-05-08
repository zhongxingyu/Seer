 package Main;
 
 import java.awt.Component;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 public class Story {
 
 	private Houston houston;
 
 	private String storyUrl;
 	private BufferedReader storyBuffer;
 	private ArrayList<String> storyText;
 	private int mapNumber;
 
 	public Story(int mapNumber, Houston houston) {
 		this.houston = houston;
 		this.mapNumber = mapNumber;
 		
 		storyText = new ArrayList<String>();
 		
 		try {
 			initializeStory();
 		} catch (IOException e) {e.printStackTrace();}
 	}
 
 	private void initializeStory() throws IOException {
 		storyUrl = "./res/story/story.txt";
 		readStoryFile();
 		assignFileContentToStoryText();
 	}
 
 	public void renewStory(int mapNumber) {
 		this.mapNumber = mapNumber;
 		try {
 			initializeStory();
 		} catch (IOException e) {e.printStackTrace();}
 	}
 
 	private void readStoryFile() throws IOException {
 		FileReader fr = new FileReader(storyUrl);
 		storyBuffer = new BufferedReader(fr);
 	}
 
 	private void assignFileContentToStoryText() {
 		String tempLine;
 
 		try {
 			for (int row = 0; ((tempLine = storyBuffer.readLine()) != null); row++) {
 				storyText.add(row, tempLine);
 			}
 		} catch (NumberFormatException | IOException e) {e.printStackTrace();}
 	}
 
 	public void showText() {
 		Component frame = null;
 		JOptionPane.showMessageDialog(frame, storyText.get(mapNumber));
 	}
 }
