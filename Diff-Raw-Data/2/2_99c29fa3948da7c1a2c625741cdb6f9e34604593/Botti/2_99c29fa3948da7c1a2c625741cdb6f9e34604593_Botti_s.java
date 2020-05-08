 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /**
  *
  * @author retsi
  */
 public class Botti {
 
     private Socket socket;
     private int portti;
     private String serveri;
     private BufferedReader lukija;
     private BufferedWriter kirjoittaja;
     private String line;
     private Parser parser;
     private HTMLtool htmltool;
     private UserModes usermodes;
     private Weather weather;
     private HSL hsl;
     private EnabledFunctions functions;
     private String masteraddy = "";
     private ArrayList<String> channels;
     private String nick = "";
 
     public Botti(String server, int port, String nick, String masteraddy, ArrayList<String> channels) {
         this.portti = port;
         this.serveri = server;
         this.parser = new Parser();
         this.htmltool = new HTMLtool();
         this.usermodes = new UserModes(server);
         this.weather = new Weather();
         this.masteraddy = masteraddy;
         this.nick = nick;
         this.channels = channels;
         this.functions = new EnabledFunctions(channels);
         this.hsl = new HSL();
 
     }
 
     public void connect() throws IOException {
 
         try {
 
             socket = new Socket(serveri, portti);
             lukija = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             kirjoittaja = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
 
             kirjoittaja.write("NICK "+nick+"\n");
             kirjoittaja.write("USER "+nick+" 0 * :"+nick+"\n");
             kirjoittaja.flush();
 
             System.out.println("Status ok");
 
             while (true) {
                 while ((line = lukija.readLine()) != null) {
                     if (parser.nWordFromMsg(parser.protocolMsg(line), 2).contains("001")) {
                         for(String channel : channels){
                             kirjoittaja.write("JOIN " +channel+"\n");
                         }
                         System.out.println(line);
                         kirjoittaja.flush();
                     }
                     else if (parser.nWordFromMsg(parser.protocolMsg(line), 1).equalsIgnoreCase("PING")) {
                         kirjoittaja.write("PONG :" + parser.msg(line) + "\n");
                         System.out.println(line);
                         System.out.print("Vastaus: PONG :" + parser.msg(line) + "\n");
                         kirjoittaja.flush();
                     }
                     else if (parser.msg(line).equals(nick) && !parser.protocolMsg(line).contains("=")){
                         System.out.println(line);
                         kirjoittaja.write("PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :Usage: !w [location], !remind [time in minutes] (parameters), !hsl [start]. [finish], !addop [addy], !addvoice [addy], !removeop [addy], !removevoice [addy], !echo [msg], !raw [raw irc-protocol msg], !enable [function], !disable [function]\n");
                         kirjoittaja.write("PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :Functions: echo, hsl, raw, reminder, urltitle, usermodes, weather\n");
                         kirjoittaja.flush();
                     }
                     else if (parser.nWordFromMsg(parser.msg(line), 1).equals("!echo") && functions.getFunctionStatusOnChannel(parser.channelProt(parser.protocolMsg(line)), "echo")) {
                         kirjoittaja.write("PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :" + parser.everythingElseExceptFirstWordFromMsg(parser.msg(line)) + "\n");
                         System.out.println(line);
                         System.out.print("Vastaus: PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :" + parser.everythingElseExceptFirstWordFromMsg(parser.msg(line)) + "\n");
                         kirjoittaja.flush();
                     }
                     else if (parser.nWordFromMsg(parser.msg(line), 1).equals("!hsl") && functions.getFunctionStatusOnChannel(parser.channelProt(parser.protocolMsg(line)), "hsl")) {
                         kirjoittaja.write("PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :" + hsl.getDirections(parser.everythingElseExceptFirstWordFromMsg(parser.msg(line))) + "\n");
                         System.out.println(line);
                         System.out.print("Vastaus: PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :" + hsl.getDirections(parser.everythingElseExceptFirstWordFromMsg(parser.msg(line))) + "\n");
                         kirjoittaja.flush();
                     }
                     else if (parser.nWordFromMsg(parser.msg(line), 1).equals("!remind") && functions.getFunctionStatusOnChannel(parser.channelProt(parser.protocolMsg(line)), "reminder")) {
                         System.out.println(line);
                         try {
                             if(Integer.parseInt(parser.nWordFromMsg(parser.msg(line), 2)) < 0){
                                 kirjoittaja.write("PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :Negative time parameter\n");
                                 kirjoittaja.flush();
                             }
                             else{
 
                                 Reminder reminder = new Reminder(kirjoittaja, Integer.parseInt(parser.nWordFromMsg(parser.msg(line), 2)), parser.everythingElseExceptFirstWordFromMsg(parser.everythingElseExceptFirstWordFromMsg(parser.msg(line))), parser.channelProt(parser.protocolMsg(line)), parser.nickProtMsg(parser.protocolMsg(line)));
                                 reminder.start();
                                 kirjoittaja.write("PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :initiated\n");
                                 kirjoittaja.flush();
                                 System.out.print("Vastaus: PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :initiated\n");
                             }
                         } catch (NumberFormatException e) {
                             kirjoittaja.write("PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :Missing time parameter\n");
                             kirjoittaja.flush();
                         }
 
                     }
                     else if (parser.nWordFromMsg(parser.msg(line), 1).equals("!raw") && parser.protocolMsg(line).contains(masteraddy) && functions.getFunctionStatusOnChannel(parser.channelProt(parser.protocolMsg(line)), "raw")) {
                         kirjoittaja.write(parser.everythingElseExceptFirstWordFromMsg(parser.msg(line)) + "\n");
                         System.out.println(line);
                         System.out.print(parser.everythingElseExceptFirstWordFromMsg(parser.msg(line)) + "\n");
                         kirjoittaja.flush();
                     }
                     else if (parser.nWordFromMsg(parser.msg(line), 1).equals("!enable") && parser.protocolMsg(line).contains(masteraddy)) {
                         if(functions.functions.get(parser.channelProt(parser.protocolMsg(line))).containsKey(parser.everythingElseExceptFirstWordFromMsg(parser.msg(line)))){
                             functions.enableFunction(parser.channelProt(parser.protocolMsg(line)), parser.everythingElseExceptFirstWordFromMsg(parser.msg(line)));
                             kirjoittaja.write("PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :enabled\n");
                             System.out.println(line);
                             System.out.print("enabled\n");
                             kirjoittaja.flush();
                         }
                     }
                     else if (parser.nWordFromMsg(parser.msg(line), 1).equals("!disable") && parser.protocolMsg(line).contains(masteraddy)) {
                         if(functions.functions.get(parser.channelProt(parser.protocolMsg(line))).containsKey(parser.everythingElseExceptFirstWordFromMsg(parser.msg(line)))){
                             functions.disableFunction(parser.channelProt(parser.protocolMsg(line)), parser.everythingElseExceptFirstWordFromMsg(parser.msg(line)));
                             kirjoittaja.write("PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :disabled\n");
                             System.out.println(line);
                             System.out.print("disabled\n");
                             kirjoittaja.flush();
                         }
                     }
                     else if (parser.nWordFromMsg(parser.msg(line), 1).equals("!addop") && parser.protocolMsg(line).contains(masteraddy)) {
                         usermodes.addUserToOpList(parser.nWordFromMsg(parser.msg(line), 2), parser.channelProt(parser.protocolMsg(line)));
                         kirjoittaja.write("PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :added\n");
                         System.out.println(line);
                         System.out.print("Vastaus: PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :added\n");
                         kirjoittaja.flush();
                     }
                     else if (parser.nWordFromMsg(parser.msg(line), 1).equals("!addvoice") && parser.protocolMsg(line).contains(masteraddy)) {
                         usermodes.addUserToVoiceList(parser.nWordFromMsg(parser.msg(line), 2), parser.channelProt(parser.protocolMsg(line)));
                         kirjoittaja.write("PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :added\n");
                         System.out.println(line);
                         System.out.print("Vastaus: PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :added\n");
                         kirjoittaja.flush();
                     }
                     else if (parser.nWordFromMsg(parser.msg(line), 1).equals("!removeop") && parser.protocolMsg(line).contains(masteraddy)) {
                         usermodes.removeUserFromOpList(parser.nWordFromMsg(parser.msg(line), 2), parser.channelProt(parser.protocolMsg(line)));
                         kirjoittaja.write("PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :removed\n");
                         System.out.println(line);
                         System.out.print("Vastaus: PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :removed\n");
                         kirjoittaja.flush();
                     }
                     else if (parser.nWordFromMsg(parser.msg(line), 1).equals("!removevoice") && parser.protocolMsg(line).contains(masteraddy)) {
                         usermodes.removeUserFromVoiceList(parser.nWordFromMsg(parser.msg(line), 2), parser.channelProt(parser.protocolMsg(line)));
                         kirjoittaja.write("PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :removed\n");
                         System.out.println(line);
                         System.out.print("Vastaus: PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :removed\n");
                         kirjoittaja.flush();
                     }
                     else if (parser.nWordFromMsg(parser.msg(line), 1).equals("!w") && functions.getFunctionStatusOnChannel(parser.channelProt(parser.protocolMsg(line)), "weather")) {
                         kirjoittaja.write("PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :" + weather.getForecast(parser.everythingElseExceptFirstWordFromMsg(parser.msg(line))) + "\n");
                         System.out.println(line);
                         System.out.print("Vastaus: PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :" + weather.getForecast(parser.nWordFromMsg(parser.msg(line), 2)) + "\n");
                         kirjoittaja.flush();
                     }
                     else if (parser.msg(line).contains("https://") && functions.getFunctionStatusOnChannel(parser.channelProt(parser.protocolMsg(line)), "urltitle") || parser.msg(line).contains("http://") && functions.getFunctionStatusOnChannel(parser.channelProt(parser.protocolMsg(line)), "urltitle")) {
                         String title = htmltool.getPageTitle(htmltool.getSource(parser.url(parser.msg(line))));
                         if (!title.equals("")) {
                             kirjoittaja.write("PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :Page title: [ " + title + " ]\n");
                             System.out.println(line);
                             System.out.print("Vastaus: PRIVMSG " + parser.channelProt(parser.protocolMsg(line)) + " :" + title + "\n");
                             kirjoittaja.flush();
                         }
                     }
                     else if (parser.protocolMsg(line).contains("JOIN") && usermodes.getOpList().containsKey(parser.getAddy(parser.protocolMsg(line))) && functions.getFunctionStatusOnChannel(parser.channelProt(parser.protocolMsg(line)), "usermodes")) {
                         System.out.println(line);
                         if (usermodes.getUsersOpChannels(parser.getAddy(parser.protocolMsg(line))).contains(parser.channelProt(parser.channelProt(line)))) {
                             kirjoittaja.write("MODE " + parser.channelProt(parser.protocolMsg(line)) + " +o " + parser.nickProtMsg(parser.protocolMsg(line)) + "\n");
                             System.out.print("Vastaus: MODE " + parser.channelProt(parser.protocolMsg(line)) + " +o " + parser.nickProtMsg(parser.protocolMsg(line)) + "\n");
                             kirjoittaja.flush();
                         }
                     }
                     else if (parser.protocolMsg(line).contains("JOIN") && usermodes.getVoiceList().containsKey(parser.getAddy(parser.protocolMsg(line))) && functions.getFunctionStatusOnChannel(parser.channelProt(parser.protocolMsg(line)), "usermodes")) {
                         System.out.println(line);
                         if (usermodes.getUsersVoiceChannels(parser.getAddy(parser.protocolMsg(line))).contains(parser.channelProt(parser.channelProt(line)))) {
                             kirjoittaja.write("MODE " + parser.channelProt(parser.protocolMsg(line)) + " +v " + parser.nickProtMsg(parser.protocolMsg(line)) + "\n");
                             System.out.print("Vastaus: MODE " + parser.channelProt(parser.protocolMsg(line)) + " +v " + parser.nickProtMsg(parser.protocolMsg(line)) + "\n");
                             kirjoittaja.flush();
                         }
                     }
                    else if(parser.protocolMsg(line).contains("ERROR :Closing Link:")){
                         long currentTime = System.currentTimeMillis();
                         long passedTime = 0;
                         while(passedTime < 300000){
                             passedTime = System.currentTimeMillis() - currentTime;
                         }
                         connect();
                     }
                     else {
                         System.out.println(line);
                     }
                 }
             }
 
         } catch (IOException e) {
             System.out.println("error " + e);
 
         }
 
     }
 }
