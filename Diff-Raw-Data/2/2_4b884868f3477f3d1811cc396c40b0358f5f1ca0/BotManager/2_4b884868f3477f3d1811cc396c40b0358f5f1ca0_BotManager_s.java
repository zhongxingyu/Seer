 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package capstone.server.util;
 
 import capstone.player.Bot;
 import capstone.player.bot.BotCompilationException;
 import capstone.player.bot.BotCompiler;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Scanner;
 import java.util.WeakHashMap;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Max
  * Keeps a list of active bots in a weak map.
  */
 public class BotManager {
     
    private static final String PATH = "\\bots";
     private static URL JARPATH;
     
     //Map player IDs to their bots
     private static WeakHashMap<String, Bot> botmap = new WeakHashMap<String, Bot>();
     
     public static Bot getBot(HttpSession session){
         return getBot(session.getAttribute("email").toString());
     }
     
     public static Bot getBot(String userid){
         userid = userid.replace('.', '_').replace('@', '_');
         Bot bot = botmap.get(userid);
         if(bot!=null){
             return bot;
         }
         else{
             bot = BotCompiler.load(userid, PATH);
             botmap.put(userid, bot);
             return bot;
         }
     }
     
     public static String getSource(String userid){
         try {
             userid = userid.replace('.', '_').replace('@', '_');
             Scanner sc = new Scanner(new File(PATH+"/src/"+userid+".src"));
             sc.useDelimiter("\\Z");
             String code = sc.next();
             sc.close();
             return code;
         } catch (FileNotFoundException ex) {
             //No code to set, use default code.
             return "";
         }
         
     }
     
     public static void compile(String userid, String code) throws BotCompilationException{
         
         
         userid = userid.replace('.', '_').replace('@', '_');
         try {
             Bot bot = BotCompiler.createBot(code, userid, PATH);
             botmap.put(userid, bot);
         } catch (IOException ex) {
             throw new BotCompilationException("Internal error validating bot.");
         }
     }
     
     public static WeakHashMap<String, Bot> getAllBots() {
         return botmap;
     }
 }
