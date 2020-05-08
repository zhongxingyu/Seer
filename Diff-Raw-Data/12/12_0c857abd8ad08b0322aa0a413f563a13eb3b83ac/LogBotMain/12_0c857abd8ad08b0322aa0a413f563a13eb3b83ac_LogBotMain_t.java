 package org.jibble.logbot;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.lang.management.ManagementFactory;
 import java.lang.management.RuntimeMXBean;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class LogBotMain {
 
     private static Properties p;
     private static String server;
     private static String channel;
     private static String nick;
     private static String joinMessage;
     private static File outDir;
 
     public static void main(String[] args) throws Exception {
         p = new Properties();
         p.load(new FileInputStream(new File("./config.ini")));
 
         server = p.getProperty("Server", "localhost");
         channel = p.getProperty("Channel", "#test");
         nick = p.getProperty("Nick", "LogBot");
         joinMessage = p.getProperty("JoinMessage", "This channel is logged.");
 
         setupOutputDir();
         writePidFile();
 
         LogBot bot = new LogBot(nick, outDir, joinMessage);
         bot.connect(server);
         bot.joinChannel(channel);
     }
 
     private static void setupOutputDir() throws Exception {
         outDir = new File(p.getProperty("OutputDir", "./output/"));
         outDir.mkdirs();
         if (!outDir.isDirectory()) {
             System.err.println("Cannot make output directory (" + outDir + ")");
             System.exit(1);
         }
 
         LogBot.copy(new File("html/header.inc.php"), new File(outDir, "header.inc.php"));
         LogBot.copy(new File("html/footer.inc.php"), new File(outDir, "footer.inc.php"));
         LogBot.copy(new File("html/index.php"), new File(outDir, "index.php"));
 
         writeConfigPhp();
     }
 
     private static void writeConfigPhp() throws Exception {
         BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outDir, "config.inc.php")));
         writer.write("<?php");
         writer.newLine();
         writer.write("    $server = \"" + server + "\";");
         writer.newLine();
         writer.write("    $channel = \"" + channel + "\";");
         writer.newLine();
         writer.write("    $nick = \"" + nick + "\";");
         writer.newLine();
         writer.write("?>");
         writer.flush();
         writer.close();
     }
 
     /**
      * Write a pid file, if possible. Errors are non-fatal.
      * 
      * @throws Exception
      */
     private static void writePidFile() {
         try {
             String pid = getProcessId();
            if (pid == null) {
                throw new Exception("Cannot find current process id!");
            }
 
             File pidFile = new File(p.getProperty("PidFile", "./logbot.pid"));
             File parent = pidFile.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new Exception("Couldn't make parent folder (" + parent + ")");
            }
             pidFile.createNewFile();
 
             BufferedWriter writer = new BufferedWriter(new FileWriter(pidFile));
             writer.write(pid);
             writer.newLine();
             writer.flush();
             writer.close();
         } catch (Exception e) {
             System.err.println(e.getMessage());
             e.printStackTrace();
             return;
         }
     }
 
     /**
      * A method of getting the current jvm's process id. Code from:
      * http://golesny.de/wiki/code:javahowtogetpid
      * 
      * tested on:
      *   - windows xp sp 2, java 1.5.0_13
      *   - mac os x 10.4.10, java 1.5.0
      *   - debian linux, java 1.5.0_13
      * all return pid@host, e.g 2204@antonius
      * 
      * @return a String representation of the system process id
      */
     private static String getProcessId() {
         RuntimeMXBean rtb = ManagementFactory.getRuntimeMXBean();
         String processName = rtb.getName();
         String result = null;
 
         Pattern pattern = Pattern.compile("^([0-9]+)@.+$", Pattern.CASE_INSENSITIVE);
         Matcher matcher = pattern.matcher(processName);
         if (matcher.matches()) {
             result = matcher.group(1);
         }
         return result;
     }
 
 }
