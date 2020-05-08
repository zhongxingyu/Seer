 package demo;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import aiml.bot.Bot;
 import aiml.bot.BotSyntaxException;
 import aiml.classifier.Classifier;
 import aiml.classifier.MatchState;
 import aiml.environment.Environment;
 import aiml.parser.AimlParserException;
 
 public class InterpreterDemo {
   public static void main(String[] args) throws XmlPullParserException,
       IOException, BotSyntaxException, AimlParserException {
     Logger.getLogger("aiml").setLevel(Level.WARNING);
     System.out.println("Demonstration program for the java aiml interpreter library");
     System.out.println("(C) Kim Sullivan, 2006");
     if (args.length == 0) {
       System.err.println("Error: you must specify a command line argument with the bot to load!");
       return;
     } else {
       File f = new File(args[0]);
       if (!f.exists()) {
         System.err.println("Error: file not found : " + f);
         return;
       }
     }
     Classifier.registerDefaultNodeHandlers();
     Bot b = new Bot();
     System.out.println("Loading bot...");
     // b.setProperty("name","Really Complicated");
    b.load(args[0]);
     System.out.println("done, loaded " + Classifier.getCount() + " categories.");
     System.out.println("Enter text to match, or /exit to quit");
 
     BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
     System.out.print("> ");
     String line = in.readLine();
    Environment e = b.createEnvironment();
     while (!line.equals("/exit")) {
       e.pushInput(line);
       MatchState m = Classifier.match(e);
       e.popInput();
       if (m != null) {
         System.out.println(((aiml.script.Script) m.getResult()).evaluate(m));
       } else
         System.out.println("no match found");
       System.out.print("> ");
       line = in.readLine();
     }
     System.out.println("Terminanting.");
   }
 }
