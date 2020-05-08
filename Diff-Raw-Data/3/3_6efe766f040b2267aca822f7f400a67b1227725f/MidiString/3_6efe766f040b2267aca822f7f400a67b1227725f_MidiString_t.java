 package com.burnskids.miduino;
 
 import java.io.FileInputStream;
 
 import javax.swing.JFileChooser;
 
 public class MidiString {
 
 	public static void main(String[] args) throws Exception {
 		JFileChooser fileChooser = new JFileChooser();
 		
 		if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
 			FileInputStream midiFile = new FileInputStream(fileChooser.getSelectedFile());
 
 			int data;
 			int count = 0;
 			String acc = "";
 			while((data = midiFile.read()) != -1) {
 				acc += String.format("\\%1$03o", data);
 				count++;
 
 				if(count >= 16) {
 					System.out.println("  \"" + acc + "\"");
 					acc = "";
 					count = 0;
 				}
 			}
			
			if(count > 0)
				System.out.println("  \"" + acc + "\"");
 		}
 	}
 }
