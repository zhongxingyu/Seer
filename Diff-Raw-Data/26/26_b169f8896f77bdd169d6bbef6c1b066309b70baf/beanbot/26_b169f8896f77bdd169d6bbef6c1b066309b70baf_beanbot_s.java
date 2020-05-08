 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.lang.Number;
 import java.lang.Process;
 import java.lang.Runtime;
 import java.lang.System;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 
 public class beanbot {
 //-----//-----//-----// Setup //-----//-----//-----//
   private static BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
 
   private static IRCConnection serverConnection;
   private static Hashstorage core = new Hashstorage(); // Core values
   private static Hashstorage config = new Hashstorage(); // Values from the config file
   private static Hashstorage variables = new Hashstorage(); // Variables that children have asked to store
   private static Hashstorage persistent = new Hashstorage(); // Persistant variables that children have asked to store
 
   private static Hashtable<String,Childpipe> forks = new Hashtable<String,Childpipe>(); // Children processes
 
 //-----//-----//-----// Config Methods //-----//-----//-----//
   public static void parse_arguments(String args[]) {
     for(Integer i = 0; i < args.length; i++) {
       String current_arg = args[i];
       if(current_arg.equals("-v") || current_arg.equals("--verbose")) { core.set("verbose","1"); }
       else if (current_arg.equals("--debug")) { core.set("debug","1"); }
       else if (current_arg.equals("--unlogged")) { core.set("unlogged","1"); }
       else if (current_arg.equals("--staydead")) { core.set("staydead","1"); }
       else if (current_arg.equals("--config")) { i++; core.set("config",args[i]); }
       else {
 	System.out.println("Usage: perl Gambot.pl [OPTION]...");
 	System.out.println("A flexible IRC bot framework that can be updated and fixed while running.\n");
 	System.out.println("-v, --verbose	Prints all messages to the terminal.");
 	System.out.println("		perl gambot.pl --verbose\n");
 	System.out.println("--debug		Enables debug message logging");
 	System.out.println("		perl gambot.pl --debug\n");
 	System.out.println("--unlogged	Disables logging of messages to files.");
 	System.out.println("		perl gambot.pl --unlogged\n");
 	System.out.println("--config	The argument after this specifies the configuration file to use.");
 	System.out.println("		These are stored in " + core.get("home_directory") + "/configurations/");
 	System.out.println("		Only give a file name. Not a path.");
 	System.out.println("		perl gambot.pl --config foo.txt\n");
 	System.out.println("--staydead	The bot will not automatically reconnect.");
 	System.out.println("		perl gambot.pl --staydead\n");
 	System.out.println("--help		Displays this help.");
 	System.out.println("		perl gambot.pl --help\n");
 	System.out.println("Ordinarily Gambot will not print much output to the terminal, but will log everything to files.");
 	System.out.println(core.get("home_directory") + "/configurations/config.txt is the default configuration file.\n");
 	System.exit(0);
       }
     }
   }
 
   public static void parse_GAPIL(String command, String pipeid) throws IOException {
     String valid_id = "a-zA-Z0-9_#-";
     Matcher matcher;
 
     if((matcher = Pattern.compile("^send_server_message>(.+)$").matcher(command)).matches()) {
       normal_output("OUTGOING",matcher.group(1));
       serverConnection.writeLine(matcher.group(1));
     }
    else if((matcher = Pattern.compile("^send_pipe_message>(["+valid_id+"])>(.+)$").matcher(command)).matches()) {
       if(forks.containsKey(matcher.group(1))) { forks.get(matcher.group(1)).writeLine(matcher.group(2)); }
       else { error_output("Tried to send a message to pipe named "+matcher.group(1)+", but it doesn't exist."); }
     }
 
    else if((matcher = Pattern.compile("^get_core_value>(["+valid_id+"])$").matcher(command)).matches()) {
       if(core.has(matcher.group(1))) { forks.get(pipeid).writeLine(core.get(matcher.group(1))); }
       else { forks.get(pipeid).writeLine(""); }
     }
    else if((matcher = Pattern.compile("^get_config_value>(["+valid_id+"])$").matcher(command)).matches()) {
       if(config.has(matcher.group(1))) { forks.get(pipeid).writeLine(config.get(matcher.group(1))); }
       else { forks.get(pipeid).writeLine(""); }
     }
    else if((matcher = Pattern.compile("^get_variable_value>(["+valid_id+"])$").matcher(command)).matches()) {
       if(variables.has(matcher.group(1))) { forks.get(pipeid).writeLine(variables.get(matcher.group(1))); }
       else { forks.get(pipeid).writeLine(""); }
     }
   }
 
 //-----//-----//-----// IO Methods //-----//-----//-----//
   public static String generate_timestamp() { // Returns a timestamp string
     Calendar calendar = Calendar.getInstance();
     return "" + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE)  + ":" + calendar.get(Calendar.SECOND);
   }
 
   public static void log_output(String prefix, String message) { // Logs a message to a file
     // TODO print logs
   }
 
   public static void stdout_output(String prefix, String message) { // Logs a message to STDOUT
     System.out.println(prefix + " " + generate_timestamp() + " " + message);
   }
 
 
   public static void error_output(String message) { // For errors. Always logged. Always output.
     log_output("BOTERROR",message);
     stdout_output("BOTERROR",message);
   }
 
   public static void event_output(String message) { // For important events. Always logged. Always output.
     log_output("BOTEVENT",message);
     stdout_output("BOTEVENT",message);
   }
 
   public static void normal_output(String prefix, String message) { // For general logging. Always logged. Output if verbose.
     log_output(prefix,message);
     if(core.get("verbose") == "1") {
       stdout_output(prefix,message);
     }
   }
 
   public static void debug_output(String message) { // For deep debug logging. Logged if debug. Output if debug and verbose.
     if(core.get("debug") == "1") {
       log_output("BOTDEBUG",message);
       if(core.get("verbose") == "1") {
 	stdout_output("BOTDEBUG",message);
       }
     }
   }
 
 //-----//-----//-----// Connection Methods //-----//-----//-----//
   public static void create_connection(String server, Integer port) throws IOException, InterruptedException {
     event_output("Attempting to connect.");
     serverConnection = new IRCConnection("chat.freenode.net",6667);
     event_output("Attempting to login.");
     serverConnection.login("Gambeanbot","Gambeanbot");
     core.set("message_count","0");
   }
 
 //-----//-----//-----// Main Loop //-----//-----//-----//
   public static void main(String[] args) throws IOException, InterruptedException {
     core.set("home_directory",new java.io.File("").getAbsolutePath());
     core.set("configuration_file","config.txt");
     config.set("delay","100");
     parse_arguments(args);
     create_connection("chat.freenode.net",6667);
 
     while(true) {
       Thread.sleep(config.getAsInt("delay"));
       //-----//-----// Read from server //-----//-----//
       if (serverConnection.alive()) {
 	String incoming = serverConnection.readLine();
 	while(incoming != null) {
 	  normal_output("INCOMING",incoming);
 	  String pipeid = "fork" + core.get("message_count");
 	  forks.put(pipeid,new Childpipe("perl /home/derek/source/gambot/parsers/plugin_parser/jane.pl"));
 	  forks.get(pipeid).writeLine(pipeid);
 	  forks.get(pipeid).writeLine("Gambeanbot");
 	  forks.get(pipeid).writeLine(incoming);
 	  core.increment("message_count");
 	  incoming = serverConnection.readLine();
 	}
       }
       else {
 	error_output("IRC connection died.");
 	if(core.get("staydead") != "1") { create_connection("chat.freenode.net",6667); }
       }
 
       //-----//-----// Read from children //-----//-----//
       Enumeration children = forks.keys();
       while(children.hasMoreElements()) {
 	String pipeid = children.nextElement().toString();
 
 	if(forks.get(pipeid).alive()) {
 	  String[] incoming = forks.get(pipeid).getLines();
 	  for(Integer i = 0; i < incoming.length; i++) {
 	    if(incoming[i] != "") {
 	      parse_GAPIL(incoming[i],pipeid);
 	    }
 	  }
 	}
 	else {
 	  debug_output(pipeid + " has died.");
 	  forks.get(pipeid).kill();
 	  forks.remove(pipeid);
 	}
       }
     }
   }
 }
