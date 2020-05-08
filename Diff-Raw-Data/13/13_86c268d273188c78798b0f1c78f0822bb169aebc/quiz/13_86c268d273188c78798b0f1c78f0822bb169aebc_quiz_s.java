 /**
  * Copyright (C) 2009 Belthazor
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.io.*;
 import java.util.*;
 import javax.microedition.midlet.*;
 import javax.microedition.lcdui.*;
 
 public class quiz extends MIDlet implements CommandListener {
 
     // Personal settings
     private int timeBetweenHints = 10;
     private int timeBetweenQuestions = 0;
     private int numberOfHints = 5;
 
     private Command exitCommand, typeCommand, backCommand, menuCommand;
     private Form form;
     private Timer timer;
     private TestTimerTask task;
     private StringItem curItem;
     private TextBox textBox;
     private Date date;
     private List menu, gameMenu;
 
     private boolean gameFocused, active;
 
     private int hintCounter, questionNumber, revealedLetters;
     private String question, answer, hint, userReply;
 
     private String vowels = "aeiouüöä";
 
     private int numberOfBytes;
     private int numberOfQuestions = 1000;
 
     private String filename = "questions.de";
 
     public quiz() {
         backCommand = new Command("Zurück", Command.BACK, 2);
         typeCommand = new Command("Tippen", Command.SCREEN, 1);
         menuCommand = new Command("Menü", Command.BACK, 2);
         exitCommand = new Command("Beenden", Command.EXIT, 2);
 
         form = new Form("Quiz");
         form.addCommand(menuCommand);
         form.addCommand(typeCommand);
         form.setCommandListener(this);
 
         gameMenu = new List("Spielmenü", List.IMPLICIT);
         gameMenu.append("Frage beenden", null);
         gameMenu.append("Zeige Hinweis", null);
         gameMenu.append("Zum Hauptmenü", null);
         gameMenu.addCommand(backCommand);
         gameMenu.setCommandListener(this);
     }
 
     protected void startApp() {
         getFileSize();
         menu = new List("Menu", List.IMPLICIT);
         menu.append("Quiz starten", null);
         menu.append("Beenden", null);
         menu.setCommandListener(this);
         Display.getDisplay(this).setCurrent(menu);
     }
 
     protected void pauseApp() {}
     protected void destroyApp(boolean bool) {}
 
     public void commandAction(Command cmd, Displayable disp) {
         int choice;
         // Main menu
         if (disp == menu && cmd == List.SELECT_COMMAND) {
             choice = menu.getSelectedIndex();
             switch (choice) {
                 case 0:
                     startQuiz();
                     break;
                 case 1:
                     destroyApp(false);
                     notifyDestroyed();
                     break;
             }
         // Game
         } else if (disp == form) {
             if (cmd == menuCommand) {
                 gameFocused = false;
                 Display.getDisplay(this).setCurrent(gameMenu);
             } else if (cmd == typeCommand) {
                 openTypeDialogue();
             }
         // Game Menu
         } else if (disp == gameMenu) {
             if (cmd == List.SELECT_COMMAND) {
                 choice = gameMenu.getSelectedIndex();
                 switch (choice) {
                     case 0: // Frage beenden
                         gameFocused = true;
                         Display.getDisplay(this).setCurrent(form);
                         Display.getDisplay(this).setCurrentItem(curItem);
                         showSolution(false);
                         break;
                     case 1: // Hinweis
                         gameFocused = true;
                         Display.getDisplay(this).setCurrent(form);
                         Display.getDisplay(this).setCurrentItem(curItem);
                         showTip();
                         break;
                     case 2: // Hauptmenü
                         stopQuiz();
                         Display.getDisplay(this).setCurrent(menu);
                         break;
                 }
             } else if (cmd == backCommand) {
                 gameFocused = true;
                 Display.getDisplay(this).setCurrent(form);
                 Display.getDisplay(this).setCurrentItem(curItem);
             }
         // Typing area
         } else if (disp == textBox) {
             gameFocused = true;
             Display.getDisplay(this).setCurrent(form);
             Display.getDisplay(this).setCurrentItem(curItem);
             if (cmd == typeCommand) {
                 curItem = new StringItem("", "<User> "+textBox.getString()+"\n");
                 form.append(curItem);
                 Display.getDisplay(this).setCurrentItem(curItem);
                 userReply += textBox.getString();
                 checkResponse(userReply);
             }
         } else if (cmd == exitCommand) {
             destroyApp(false);
             notifyDestroyed();
         }
     }
 
     public void startQuiz() {
         gameFocused = true;
         questionNumber = 0;
 
         getNextQuestion();
         startTimer();
     }
 
     public void stopQuiz() {
         gameFocused = false;
         active = false;
         timer.cancel();
         form.deleteAll();
     }
 
     public void getFileSize() {
         try {
             InputStream is = getClass().getResourceAsStream(filename);
             numberOfBytes = is.available();
         } catch (Exception e) {
             // File not found
             destroyApp(false);
             notifyDestroyed();
         }
     }
 
     public void getNextQuestion() {
         readFile();
 
         userReply = "";
 
         hintCounter = 0;
         revealedLetters = 0;
         active = true;
 
         curItem = new StringItem("Frage Nr. "+ ++questionNumber+": ", question+"\n");
         form.append(curItem);
 
         date = new Date();
 
         if (gameFocused) {
             Display.getDisplay(this).setCurrentItem(curItem);
         }
     }
 
     public void showTip() {
         String cur, vok = "";
         for (int i = 0; i < answer.length(); ++i) {
             cur = answer.substring(i,i+1);
             if (vowels.indexOf(cur.toLowerCase())>-1) {
                 vok += "·";
             } else {
                 vok += cur;
             }
         }
         curItem = new StringItem("Hinweis: ", vok);
         form.append(curItem);
         if (gameFocused) {
             Display.getDisplay(this).setCurrentItem(curItem);
         }
     }
 
     public void readFile() {
         Random rand = new Random(System.currentTimeMillis());
         int randomInt = rand.nextInt()%numberOfBytes;
         int counter = 0;
         int temp;
 
         if (randomInt < 0) {
             randomInt += numberOfBytes;
         }
         randomInt++;
 
         try {
             InputStreamReader reader = new InputStreamReader(
                     getClass().getResourceAsStream(filename));
 
             skip(reader, randomInt);
 
             String strLine = readLine(reader);
             temp = strLine.indexOf("|");
             if (temp < 0) {
                 getNextQuestion();
             }
             question = strLine.substring(0,temp);
             answer = strLine.substring(temp+1,strLine.length());
             reader.close();
         } catch (Exception e) {
             // File not found
         }
     }
 
     // Skip number of bytes in 8kb chunks
     public void skip (InputStreamReader reader, long bytes) throws IOException {
         int readChar;
         int chunk = 8192;
         char temp[] = new char[chunk];
         long counter = 0;
         while (counter+chunk < bytes) {
             if ((readChar = reader.read(temp, 0, chunk))>-1) {
                 counter += chunk;
             }
         }
         for (int i = 0; i < bytes-counter; ++i) {
             readChar = reader.read();
         }
     }
 
     public static String replaceAll(String _text, String _searchStr, String _replacementStr) {
         StringBuffer sb = new StringBuffer();
         int searchStringPos = _text.indexOf(_searchStr);
         int startPos = 0;
         int searchStringLength = _searchStr.length();
 
         while (searchStringPos != -1) {
             sb.append(_text.substring(startPos, searchStringPos)).append(_replacementStr);
             startPos = searchStringPos + searchStringLength;
             searchStringPos = _text.indexOf(_searchStr, startPos);
         }
         sb.append(_text.substring(startPos,_text.length()));
         return sb.toString();
     }
 
     private String readLine(InputStreamReader reader) throws IOException {
         int readChar = reader.read();
         if (readChar == -1) {
             return null;
         }
 
         while (readChar != -1 && readChar != '\n') {
             readChar = reader.read();
         }
         readChar = reader.read();
 
         StringBuffer string = new StringBuffer("");
         while (readChar != -1 && readChar != '\n') {
             if (readChar != '\r') {
                 string.append((char)readChar);
             }
             readChar = reader.read();
         }
         return string.toString();
     }
 
     public void getNextHint() {
         hint = "";
         if (revealedLetters == answer.length()) {
             showSolution(false);
         } else {
             for (int i=0; i < answer.length(); i++) {
                if ((i-hintCounter)%numberOfHints == 0) {
                    hint += answer.substring(i,i+1);
                    revealedLetters++;
                 } else {
                    // Always show spaces
                    if (answer.substring(i,i+1)==" ") {
                        hint += " ";
                     } else {
                         hint += "·";
                     }
                 }
             }
             curItem = new StringItem("Tip "+ ++hintCounter+": ",hint+"\n");
             form.append(curItem);
             if (gameFocused) {
                 Display.getDisplay(this).setCurrentItem(curItem);
             }
         }
     }
 
     public String process(String r) {
         r = r.toLowerCase();
         r = replaceAll(r,"ä","ae");
         r = replaceAll(r,"ü","ue");
         r = replaceAll(r,"ö","oe");
         r = replaceAll(r,"é","e");
         r = replaceAll(r,"è","e");
         r = replaceAll(r,"ß","ss");
         r = replaceAll(r," ","");
         return r;
     }
 
     public void checkResponse(String r) {
         if (process(r).indexOf(process(answer))>-1) {
             showSolution(true);
         }
     }
     
     public void showSolution(boolean answered) {
         if (active) {
             Date temp = new Date();
             long elapsed = (temp.getTime()-date.getTime())/1000;
 
             // Clear screen after every question to save memory and cpu cycles
             form.deleteAll();
 
             if (!answered) {
                 form.append("Automatisch aufgelöst nach "+elapsed+" Sekunden.\n");
             } else {
                 curItem = new StringItem("Richtige Antwort ", "nach "+elapsed+" Sekunden.\n");
                 form.append(curItem);
             } 
             curItem = new StringItem("Die Lösung war: ", answer);
             form.append(curItem);
             if (gameFocused) {
                 Display.getDisplay(this).setCurrentItem(curItem);
             }
             active = false;
             timer.cancel();
             startTimer();
         }
     }
 
     public void openTypeDialogue() {
         textBox = new TextBox("Type", "", 256, 0);
         textBox.addCommand(typeCommand);
         textBox.addCommand(backCommand);
         textBox.setCommandListener(this);
         gameFocused = false;
         Display.getDisplay(this).setCurrent(textBox);
     }
 
     public void startTimer() {
         timer = new Timer();
         task = new TestTimerTask();
 
         if (active) {
             timer.schedule(task,timeBetweenHints*1000,timeBetweenHints*1000);
         } else {
             timer.schedule(task,timeBetweenQuestions*1000);
         }
     }
 
     private class TestTimerTask extends TimerTask {
         public final void run() {
             if (active) {
                 getNextHint();
             } else {
                 getNextQuestion();
                 startTimer();
             }
         }
     }
 }
