 import java.io.*;
 import java.net.Socket;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Bot {
 
     private String nick = "SlaveMr";
     private String user = "SlaveMr";
     private String realName = "Slave to Master INF";
     private String channel = "#Inf3cti0us";
    // private String Auth = "Inf3cti0us!Swatariane@Rizon-7D7F151.r.u.going.to.do.because.i.stole-your.info";
     private String Master = "Inf3cti0us";
    private String Person;
 
     private Pattern p = Pattern.compile(":(.*)!(.*)@(.*)\\s(.*)\\s(.*)\\s:(.*)!(.*)\\s(.*)");
 
     private String host;
     private int port;
 
     public int count = 0;
 
     private Socket socket;
     private BufferedReader in;
     private BufferedWriter out;
     private String line = null;
 
 
 
     public int RandomNumber(){
         Random r = new Random();
         return r.nextInt(5);
     }
 
 
     private void writeLine(String message) throws IOException {
         if (!message.endsWith("\r\n"))
             message = message + "\r\n";
 
         System.out.println(">" + message);
         out.write(message);
         out.flush();
     }
 
     private void sendPrivmsg(String target, String message) throws IOException {
         writeLine("PRIVMSG " + target + " :" + message);
     }
 
     private void sendActionmsg(String target, String message) throws IOException{
         writeLine("PRIVMSG " + target + " : ACTION" + message);
     }
 
     private void connect() throws IOException {
         socket = new Socket(host, port);
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
 
         writeLine("NICK :" + nick);
         writeLine("USER " + user + " * * :" + realName);
 
         String[] args;
         String prefix;
         String command;
         String target;
         String message;
 
         String sender;
 
         while((line = in.readLine()) != null) {
             System.out.println("<" + line);
             if (line.startsWith("PING")) {
                  if(count < 2){
                 sendPrivmsg(Master, "We just got pinged!");
                      ++count;
                  }
                 writeLine(line.replace("PING", "PONG"));
             }
 
             args = line.split(" ");
             if (line.startsWith(":")) { // a prefix will always contain a leading colon
                 prefix = args[0].substring(1);
                 command = args[1];
                 target = args[2];
 
                 sender = prefix.split("!")[0];
 
                 if (command.equals("001")) {
                     writeLine("JOIN :" + channel);
 
 
                 } else if (command.equals("PRIVMSG")) {  // We got messaged!
                     // to parse the message, we will need the
                     // index of the second colon in the line
                     message = line.substring(line.indexOf(":", 1) + 1);
 
                     if (message.equalsIgnoreCase("!hello")) {
                         if (target.equalsIgnoreCase(nick))
                             target = sender;
 
                         Matcher m = p.matcher(line);
                         if(m.find()) Person = m.group(1);
 
                        if(Person.equals("Inf3cti0us")){     //TODO Find why this is broken?
                             sendPrivmsg(target,"Greetings Master " + Person );
                         }else{
                         sendPrivmsg(target, "Hey " +  Person + "..");
                         }
                     }
 
 
                    if(message.equalsIgnoreCase("!Say")){
                            if(target.equalsIgnoreCase(nick))
                                target = sender;
 
                         Matcher m = p.matcher(line);
                        String argument;
                        if(m.find() && m.matches()){
                            argument = m.group(8);
                            Person = m.group(1);
                            sendPrivmsg(target,Person + " " + argument);
                        }else{
                            argument = m.group(6);
                            Person = m.group(1);
                            sendPrivmsg(target,Person + " " + argument);
                        }
 
                     }
 
 
                     if(message.equalsIgnoreCase("Wau?")){
                         if(target.equalsIgnoreCase(nick))
                                 target = sender;
                         Matcher m = p.matcher(line);
                         if(m.find())
                                 sendPrivmsg(target, "Not a robot ;/ " + m.group(1));
 
                     }
 
 
                     if(message.equalsIgnoreCase("!help")){
 
                             if(target.equalsIgnoreCase(nick)){
                                 target = sender;
                                 Matcher m = p.matcher(line);
                                 if(m.find())
                             sendPrivmsg(target,"No help for you " + m.group(1));
                         }
                     }
                 }if(command.equals("433")){
                     //Nickname is already in use. Add random Numbers :)
                     writeLine("NICK :" + nick + RandomNumber());
                     writeLine("USER " + user + " * * :" + realName);
 
                } if(command.equals("439")){   //TODO something interesting here?
                     System.out.println("Waiting for processing connection..");
                 } if(command.equals("KICK")){
                     sendPrivmsg(target,"Sorry, I'll behave better this time!");
                     sendPrivmsg(target," ACTION sits in the naughty corner");
                 }
 
             }
         }
     }
 
     public Bot(String host, int port) throws IOException {
         this.host = host;
         this.port = port;
         connect();
     }
 
     public static void main(String[] args) {
         try {
             new Bot("irc.rizon.net", 6667);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
