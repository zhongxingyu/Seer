 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 import java.lang.String;
 import java.io.*;
 
 public class Chester {
     static ArrayList<String> storage = new ArrayList<String>();
     static String nick = "hawks_test_bot", serv = "irc.esper.net",
             chan = "#chester";
 
     public static void main(String[] args) throws IOException {
         String str;
         Socket s = new Socket(serv, 6667);
         BufferedReader i;
         PrintWriter o;
         i = new BufferedReader(new InputStreamReader(s.getInputStream()));
         o = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
         o.print("USER " + nick + " 8 * :Chester\r\n" + "NICK " + nick + "\r\n");
         o.flush();
         while (s.isConnected()) {
             str = i.readLine();
             System.out.println(str);
             if (str.startsWith("PING ")) {
                 o.print("PONG " + str.substring(5) + "\r\n");
                 o.flush();
             }
             if (str.charAt(0) == ':') {
                 if (str.split(" ")[1].equals("001")) {
                     loadBrain();
                     o.print("MODE " + nick + " +B\r\n" + "JOIN " + chan + "\r\n");
                     o.flush();
                 } else if (str.split(" ")[1].equals("PRIVMSG")) {
                     String message = str.replaceFirst(":.*?:", "");
                     String margs[] = message.split("[ ]+");
                     String channel = str.split(" ")[2];
                     if (message.contains(nick)) {
                         String randomMessage = scrambleWord();
                         String fixedMessage = truncate((randomMessage), 300).replaceAll("<.*?>", "").replaceAll("\\[.*?\\]", "");
                         o.print("PRIVMSG " + channel + " :" + fixedMessage + "\r\n");
                         o.flush();
                     } else if (message.toLowerCase().startsWith("!hi")) {
                         o.print("PRIVMSG " + channel + " :Hi!\r\n");
                         o.flush();
                     } else if (message.startsWith("!join")) {
                         o.print("JOIN " + margs[1] + "\r\n");
                         o.flush();
                     } else {
                         write(message);
                         storage.add(message);
                     }
                 }
             }
         }
 
         s.close();
     }
 
     public static void write(String sentence) {
 
         // The name of the file to open.
         String fileName = "chester.brain";
 
         try {
             FileWriter fw = new FileWriter(fileName, true);
             BufferedWriter bufferedWriter = new BufferedWriter(fw);
             bufferedWriter.append(sentence);
             bufferedWriter.newLine();
             bufferedWriter.close();
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
     public static void loadBrain() throws IOException, FileNotFoundException {
         String fileName = "chester.brain";
 
         FileReader fr = new FileReader(fileName);
         BufferedReader br = new BufferedReader(fr);
         String line = null;
         while ((line = br.readLine()) != null) {
             storage.add(line);
         }
         br.close();
 
     }
 
     public static String truncate(String string, int length) {
         if (string != null && string.length() > length)
             string = string.substring(0, length);
         return string;
 
     }
 
     public static String scrambleWord() {
         Random r = new Random(System.nanoTime());
         Collections.shuffle(storage, r);
         String randomMessage = storage.get(0);
         String secondPart = storage.get(1);
         
         String[] wordsOne = randomMessage.split(" ");
         String[] wordsTwo = secondPart.split(" ");
         
        
         int i = 0;
         int maxOne = (int) Math.floor(wordsOne.length / 2);
         StringBuilder sb = new StringBuilder();
         for (String word: wordsOne) {
             sb.append(word + " ");
             i++;
            
             if (i == maxOne) {
                 break;
             }
         }
         
        i = wordsTwo.length;
         int maxTwo = (int) Math.floor(wordsTwo.length / 2);
        while (i != maxTwo) {
            sb.append(wordsTwo[i] + " ");
            i--;
         }
         
         
         return sb.toString().trim();
     }
 }
