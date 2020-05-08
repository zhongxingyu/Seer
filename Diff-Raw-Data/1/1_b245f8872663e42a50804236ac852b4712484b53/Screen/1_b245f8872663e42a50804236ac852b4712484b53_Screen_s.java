 package zombiefu.util;
 
 import jade.ui.TermPanel;
 import jade.util.datatype.ColoredChar;
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 
 public class Screen {
 
     // Liest eine Datei im UTF-16 Format ein und gibt das 2-dim Feld in ColoredChars zurück
     public static ColoredChar[][] readFile(String input) throws IOException {
         InputStreamReader reader = new InputStreamReader(new FileInputStream(input),"UTF-16");
         BufferedReader text = new BufferedReader(reader);
         LinkedList<String> lines = new LinkedList<String>();
         String temp;
         while((temp = text.readLine())!=null) {
                 lines.add(temp);
 	}
         text.close();
         reader.close();
 
         ColoredChar[][] chars = new ColoredChar[lines.size()][lines.get(0).length()];
         for (int i=0;i<lines.size();i++) {
 	    for (int j=0;j<lines.get(i).length();j++) {
                 chars[i][j] = ColoredChar.create(lines.get(i).charAt(j), Color.white);
 	    }
         }
         return chars;
     }
 
     public static void showImage(TermPanel term, String input) throws InterruptedException{
         try {
             ColoredChar[][] start = readFile(input);
             term.clearBuffer();
             for(int x = 0; x < term.DEFAULT_COLS; x++) {
                 for(int y = 0; y < term.DEFAULT_ROWS; y++) {
                     if (y >= start.length || x >= start[0].length) {
                         term.bufferChar(x,y,ColoredChar.create(' '));
                     } else {
                         term.bufferChar(x, y, start[y][x]);
                     }
                 }
             }
            term.bufferCameras();
             term.refreshScreen();
             term.getKey();
         } catch (IOException e) {
             System.out.println("Datei nicht gefunden.");
         }
     }
 
     public static String[] getStrings(String input){
     	LinkedList<String> lines = new LinkedList<String>();
 		try {
 			InputStreamReader  reader = new InputStreamReader(new FileInputStream(input),"UTF-16");
 			BufferedReader text = new BufferedReader(reader);
 	        String temp;
 	        while((temp = text.readLine())!=null)
 	                lines.add(temp);
 	        text.close();
 	        reader.close();
 		} catch (Exception e) {}
 		String[] erg = new String[lines.size()];
         for (int i = 0;i<lines.size();i++)
         	erg[i] = lines.get(i);
         return erg;
     }
 }
