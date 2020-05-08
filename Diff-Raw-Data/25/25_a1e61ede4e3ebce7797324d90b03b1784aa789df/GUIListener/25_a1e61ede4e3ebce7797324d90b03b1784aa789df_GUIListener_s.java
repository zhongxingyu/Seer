 package net.teamwraith.npctalk.gui;
 
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.text.BadLocationException;
 
 import net.teamwraith.npctalk.Files;
 
 /**
  * This is what <i>interacts</i> with the GUI;
  * in other words, BuildGUI determines the layout
  * and so on, while <code>GUIListener</code> 
  * determines how the user will interact with it.
  * 
  * @author Stektpotet
  * @author EternalFacepalm
  */
 public class GUIListener {
 	
 	private String speech;
 	private GUIBuild gui;
 	
 	public GUIListener() {
 		gui = new GUIBuild();
 		
 		gui.getSpeechField().addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S) {
 					
 					setSpeech(gui.getSpeechField().getText());
 					runLines(new File("TEST01.txt"));
 					System.out.println(speech);
 				}
 			}
 		});
 	}
 	
 	public void setSpeech(String speech){
 		this.speech = speech;
 	}
 	
 	public String getSpeech(){
 		return speech;
 	}
 	
 	public void runLines(File file){
 		int totalLines = gui.getSpeechField().getLineCount();
		String[] line = null;
 		
 		for (int i=0; i < totalLines; i++) {
 			try{
 			int start = gui.getSpeechField().getLineStartOffset(i);
 			int end = gui.getSpeechField().getLineEndOffset(i);
			line[i] = getSpeech().substring(start, end);
			Files.writeRawFile (totalLines, line, file);
 			} catch (BadLocationException e) {
 				e.printStackTrace();
 			}

 		}
 	}
 }
