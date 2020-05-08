 import java.io.*;
 import java.net.Socket;
 import java.util.regex.Pattern;
 
 public class Bot {
 
     private String nick = "SlaveMr";
     private String user = "SlaveMr";
     private String realName = "Slave to Master INF";
    private String channel = "#aurora-rs";
     private String Auth = "Inf3cti0us!Swatariane@Rizon-7D7F151.r.u.going.to.do.because.i.stole-your.info";
     private String Master = "Inf3cti0us";
    private String user = "";
 
     private Pattern p = Pattern.compile(":(.*)!(.*)@([^\\ ]*)");
 
     private String host;
     private int port;
 
     public int count = 0;
 
     private Socket socket;
     private BufferedReader in;
     private BufferedWriter out;
 
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
 
     private void connect() throws IOException {
         socket = new Socket(host, port);
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
 
         writeLine("NICK :" + nick);
         writeLine("USER " + user + " * * :" + realName);
 
         String line = null;
 
         String[] args = null;
         String prefix = null;
         String command = null;
         String target = null;
         String message = null;
 
         String sender = null;
 
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
                 } else if (command.equals("PRIVMSG")) {
                     // to parse the message, we will need the
                     // index of the second colon in the line
                     message = line.substring(line.indexOf(":", 1) + 1);
                     if (message.equalsIgnoreCase("!hello")) {
                         if (target.equalsIgnoreCase(nick))
                             target = sender;
                        if(p.matcher(line).find())
                            user = p.matcher(line).group(1);
 
                        sendPrivmsg(target, "Hewo " +  user + "!");   //TODO get this to work normally
                        // sendPrivmsg(target, "Hello to you too!");
                     }
 
                     if(message.equalsIgnoreCase("Wau?")){
                         if(target.equalsIgnoreCase(nick))
                                 target = sender;
                                 sendPrivmsg(target, "Not a robot ;/");
                     }
 
                     if(message.equalsIgnoreCase("!help")){
 
                             if(target.equalsIgnoreCase(nick)){
                                 target = sender;
                             sendPrivmsg(target,"No help for you " + target);
                         }
                     }
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
