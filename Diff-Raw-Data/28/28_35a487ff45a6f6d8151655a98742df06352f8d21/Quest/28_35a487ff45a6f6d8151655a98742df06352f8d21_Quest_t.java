 package Main;
 
 import java.awt.Component;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
 
 public class Quest {
 
 	private Houston houston;
 
 	private String questUrl;
 	private String[] questUrls;
 	private BufferedReader questBuffer;
 	private ArrayList<String> questText;
 	private boolean questDone = false;
 	private int mapNumber;
 
 	public Quest(int mapNumber, Houston houston) {
 		this.houston = houston;
 		this.mapNumber = mapNumber;
 		
 		questUrls = new String[8];
 		questUrls[0] = "./res/quest/quest1.txt";
 		questUrls[1] = "./res/quest/quest2.txt";
 		questUrls[2] = "./res/quest/quest3.txt";
 		questUrls[3] = "./res/quest/quest4.txt";
 		questUrls[4] = "./res/quest/quest5.txt";
 		
 		questText = new ArrayList<String>();
 		
 		try {
 			initializeStory();
 		} catch (IOException e) {e.printStackTrace();}
 	}
 
 	private void initializeStory() throws IOException {
 		questUrl = questUrls[mapNumber];
		//readStoryFile();
		//assignFileContentToStoryText();
 	}
 
 	public void renewStory(int mapNumber) {
 		this.mapNumber = mapNumber;
 		try {
 			initializeStory();
 		} catch (IOException e) {e.printStackTrace();}
 	}
 
 	private void readStoryFile() throws IOException {
 		FileReader fr = new FileReader(questUrl);
 		questBuffer = new BufferedReader(fr);
 	}
 
 	private void assignFileContentToStoryText() {
 		String tempLine;
 
 		try {
 			for (int row = 0; ((tempLine = questBuffer.readLine()) != null); row++) {
 				questText.add(row, tempLine);
 			}
 		} catch (NumberFormatException | IOException e) {e.printStackTrace();}
 	}
 
 	public void doQuest() {
 		JDialog questDialog = new JDialog();
 		questDialog.setTitle("R\u00e4tsel");
 		questDialog.setSize(300,175);
 		questDialog.setModal(true);
		questDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		questDialog.setVisible(true);
 		houston.gameLogic.npcv = 0;
 		
 	}
 }
