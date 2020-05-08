 package org.hettoo.substitution;
 
 import java.util.TreeMap;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.Arrays;
 
 import java.util.regex.Pattern;
 import java.util.regex.MatchResult;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 
 public class InteractiveSubstituter extends Substituter {
     private Map<String, CommandHandler> handlers;
     private CommandHandler unknownCommandHandler;
     private boolean stop;
 
     public InteractiveSubstituter() {
         handlers = new LinkedHashMap<String, CommandHandler>();
         setCommandHandler("set", new SetCommandHandler());
         setCommandHandler("original", new OriginalCommandHandler());
         setCommandHandler("show", new ShowCommandHandler());
         setCommandHandler("mask", new MaskCommandHandler());
         setCommandHandler("all", new AllCommandHandler());
         setCommandHandler("count", new CountCommandHandler());
         setCommandHandler("add", new AddCommandHandler());
         setCommandHandler("delete", new DeleteCommandHandler());
         setCommandHandler("clear", new ClearCommandHandler());
         setCommandHandler("list", new ListCommandHandler());
         setCommandHandler("stats", new StatsCommandHandler());
         setCommandHandler("read", new ReadCommandHandler());
         setCommandHandler("write", new WriteCommandHandler());
         setCommandHandler("quit", new QuitCommandHandler());
         setCommandHandler("help", new HelpCommandHandler());
         setUnknownCommandHandler(new UnknownCommandHandler());
     }
 
     protected class QuitCommandHandler extends AbstractCommandHandler {
         public void handle(ArgumentProvider arguments) {
             stop = true;
         }
 
         public String getDescription() {
             return "terminates the interactive substituter";
         }
     }
 
     protected class HelpCommandHandler extends AbstractCommandHandler {
         private Argument command;
 
         public HelpCommandHandler() {
             addArgument(command = new Argument("command", true, false));
         }
 
         public void handle(ArgumentProvider arguments) {
             String name = arguments.getArgument(command);
             if (name == null) {
                 System.out.println("Quote arguments using single quotes.");
                 System.out.println(
                         "Add a command as an argument to show its usage.");
                 System.out.println("Available commands:");
                 for (String command : handlers.keySet())
                     System.out.println("    " + command + " - "
                             + handlers.get(command).getDescription());
             } else {
                 CommandHandler handler = handlers.get(name);
                 if (handler == null) {
                     System.out.println("Command " + name + " does not exist.");
                 } else {
                     System.out.println(name + " - " + handler.getDescription());
                     System.out.println("Usage: " + name
                             + " " + handler.getUsage());
                 }
             }
         }
 
         public String getDescription() {
             return "get started";
         }
     }
 
     protected class UnknownCommandHandler extends AbstractCommandHandler {
         private Argument input;
 
         public UnknownCommandHandler() {
             addArgument(input = new Argument("input", false, true));
         }
 
         public void handle(ArgumentProvider arguments) {
             System.out.println("Unknown command "
                     + arguments.getArgument(input) + ", try `help'.");
         }
 
         public String getDescription() {
             return "rejects a command";
         }
     }
 
     protected class SetCommandHandler extends AbstractCommandHandler {
         private Argument data;
 
         public SetCommandHandler() {
             addArgument(data = new Argument("data", false, false));
         }
 
         public void handle(ArgumentProvider arguments) {
             setOriginal(arguments.getArgument(data));
         }
 
         public String getDescription() {
             return "sets the original data";
         }
     }
 
     protected class OriginalCommandHandler extends AbstractCommandHandler {
         public void handle(ArgumentProvider arguments) {
             System.out.println(getOriginal());
         }
 
         public String getDescription() {
             return "shows the original data";
         }
     }
 
     protected class ShowCommandHandler extends AbstractCommandHandler {
         public void handle(ArgumentProvider arguments) {
             System.out.println(getCurrent());
         }
 
         public String getDescription() {
             return "shows the current data";
         }
     }
 
     protected class MaskCommandHandler extends AbstractCommandHandler {
         public void handle(ArgumentProvider arguments) {
             System.out.println(getCurrentMasked('*'));
         }
 
         public String getDescription() {
             return "shows the current data with unreplaced data masked";
         }
     }
 
     protected class AllCommandHandler extends AbstractCommandHandler {
         public void handle(ArgumentProvider arguments) {
             execute(new ArrayList<String>(Arrays.asList("original")));
             execute(new ArrayList<String>(Arrays.asList("show")));
             execute(new ArrayList<String>(Arrays.asList("mask")));
         }
 
         public String getDescription() {
             return "shows the original and the current data both unmasked and"
                + " masked";
         }
     }
 
     protected class ListCommandHandler extends AbstractCommandHandler {
         private Argument pattern;
 
         public ListCommandHandler() {
             addArgument(pattern = new Argument("character", true, false));
         }
 
         public void handle(ArgumentProvider arguments) {
             String characterString = arguments.getArgument(pattern);
             Character character = null;
             if (characterString != null)
                 character = characterString.charAt(0);
             for (Replacement replacement : replacements) {
                 if (character == null || replacement.getTarget() == character
                         || replacement.getResult() == character)
                     System.out.println(replacement.getTarget() + " -> "
                             + replacement.getResult());
             }
         }
 
         public String getDescription() {
             return "shows all replacements";
         }
     }
 
     protected class StatsCommandHandler extends AbstractCommandHandler {
         public void handle(ArgumentProvider arguments) {
             String original = getOriginal();
             Map<Character, Boolean> replaced
                 = new HashMap<Character, Boolean>();
             for (char c : getOriginal().toCharArray())
                 replaced.put(c, false);
             Map<Character, Integer> counts = new HashMap<Character, Integer>();
             for (Replacement replacement : replacements) {
                 char target = replacement.getTarget();
                 if (replaced.containsKey(target))
                     replaced.put(target, true);
                 char result = replacement.getResult();
                 Integer count = counts.get(result);
                 if (count == null)
                     count = 0;
                 counts.put(result, ++count);
             }
             for (Character c : counts.keySet()) {
                 if (counts.get(c) > 1)
                     System.out.println("Character " + c
                             + " is resulted multiple times.");
             }
             int replacements = 0;
             for (Character c : replaced.keySet()) {
                 if (replaced.get(c))
                     replacements++;
             }
             System.out.println(replacements + " of " + replaced.keySet().size()
                     + " different characters replaced.");
         }
 
         public String getDescription() {
             return "show multiply resulted characters as well as the amount of"
                 + " replacements and the maximum amount possible";
         }
     }
 
     protected class AddCommandHandler extends AbstractCommandHandler {
         private Argument target;
         private Argument result;
 
         public AddCommandHandler() {
             addArgument(target = new Argument("target", false, false));
             addArgument(result = new Argument("result", true, false));
         }
 
         public void handle(ArgumentProvider arguments) {
             String targetString = arguments.getArgument(target);
             String resultString = arguments.getArgument(result);
             if (resultString == null)
                 resultString = targetString;
             for (int i = 0 ; i < targetString.length(); i++)
                 addReplacement(new Replacement(targetString.charAt(i),
                             resultString.charAt(i)));
         }
 
         public String getDescription() {
             return "adds replacements character-wise";
         }
     }
 
     protected class DeleteCommandHandler extends AbstractCommandHandler {
         private Argument target;
 
         public DeleteCommandHandler() {
             addArgument(target = new Argument("target", false, false));
         }
 
         public void handle(ArgumentProvider arguments) {
             for (char c : arguments.getArgument(target).toCharArray())
                 deleteReplacement(c);
         }
 
         public String getDescription() {
             return "deletes a replacement";
         }
     }
 
     protected class ClearCommandHandler extends AbstractCommandHandler {
         public void handle(ArgumentProvider arguments) {
             replacements.clear();
         }
 
         public String getDescription() {
             return "clears all replacements";
         }
     }
 
     protected class ReadCommandHandler extends AbstractCommandHandler {
         private Argument file;
 
         public ReadCommandHandler() {
             addArgument(file = new Argument("file", false, false));
         }
 
         public void handle(ArgumentProvider arguments) {
             try {
                 BufferedInputStream in
                     = new BufferedInputStream(new FileInputStream(
                                 arguments.getArgument(file) + ".s"));
                 boolean done = false;
                 do {
                     in.mark(1);
                     int c = in.read();
                     in.reset();
                     if (c == -1)
                         done = true;
                     else
                         addReplacement(new Replacement(in));
                 } while (!done);
                 in.close();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
         public String getDescription() {
             return "reads replacements from a file";
         }
     }
 
     protected class WriteCommandHandler extends AbstractCommandHandler {
         private Argument file;
 
         public WriteCommandHandler() {
             addArgument(file = new Argument("file", false, false));
         }
 
         public void handle(ArgumentProvider arguments) {
             try {
                 FileWriter out = new FileWriter(
                         arguments.getArgument(file) + ".s");
                 for (Replacement replacement : replacements)
                     replacement.write(out);
                 out.close();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
         public String getDescription() {
             return "writes replacements to a file";
         }
     }
 
     protected class CountCommandHandler extends AbstractCommandHandler {
         private Argument mode;
 
         public CountCommandHandler() {
             addArgument(mode = new Argument("mode", true, false));
             mode.addValue("words");
         }
 
         public void handle(ArgumentProvider arguments) {
             String original = getOriginal();
             String modeString = arguments.getArgument(mode);
             if (modeString != null && modeString.equals("words")) {
                 HashMap<String, Integer> map
                     = new HashMap<String, Integer>();
                 TreeMap<String, Integer> orderedMap
                     = new TreeMap<String, Integer>(
                             new ValueComparator<String>(map));
                 String s = "";
                 for (char c : original.toCharArray()) {
                     if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                             || c == '-' || c == '_') {
                         if (c != '-')
                             s += c;
                     } else if (!s.equals("")) {
                         s = s.toLowerCase();
                         Integer count = map.get(s);
                         if (count == null)
                             count = 0;
                         map.put(s, ++count);
                         s = "";
                     }
                 }
                 if (!s.equals("")) {
                     s = s.toLowerCase();
                     Integer count = map.get(s);
                     if (count == null)
                         count = 0;
                     map.put(s, ++count);
                     s = "";
                 }
                 orderedMap.putAll(map);
                 for (String word : orderedMap.keySet())
                     System.out.println(word + ": " + map.get(word));
             } else {
                 HashMap<Character, Integer> map
                     = new HashMap<Character, Integer>();
                 TreeMap<Character, Integer> orderedMap
                     = new TreeMap<Character, Integer>(
                             new ValueComparator<Character>(map));
                 for (char c : original.toCharArray()) {
                     Integer count = map.get(c);
                     if (count == null)
                         count = 0;
                     map.put(c, ++count);
                 }
                 orderedMap.putAll(map);
                 for (Character c : orderedMap.keySet()) {
                     System.out.println((isReplaced(c, true) ? "x" : "o")
                             + " " + c + ": " + map.get(c));
                 }
             }
         }
 
         public String getDescription() {
             return "shows original character or word counts";
         }
 
         protected class ValueComparator<T> implements Comparator<T> {
             Map<T, Integer> base;
 
             public ValueComparator(Map<T, Integer> base) {
                 this.base = base;
             }
 
             public int compare(T a, T b) {
                 if (base.get(a) >= base.get(b))
                     return 1;
                 return -1;
             }
         }
     }
 
     public void start() {
         stop = false;
         while (!stop)
             requestCommand();
     }
 
     private void requestCommand() {
         System.out.print("> ");
         List<String> arguments = new ArrayList<String>();
         arguments.add("");
         boolean done = false;
         boolean quoted = false;
         boolean escaped = false;
         for (;;) {
             int c = 0;
             try {
                 c = System.in.read();
             } catch (Exception e) {
                 e.printStackTrace();
                 System.exit(1);
             }
             switch (c) {
                 case '\\':
                    if (!escaped) {
                        escaped = true;
                        break;
                    }
                 case '\'':
                     if (!escaped) {
                         quoted = !quoted;
                         break;
                     }
                 case '\n':
                    if (!quoted && !escaped) {
                         execute(arguments);
                         return;
                     }
                 case ' ':
                 case '\t':
                    if (!quoted && !escaped) {
                         if (!arguments.get(arguments.size() - 1).equals(""))
                             arguments.add("");
                         break;
                     }
                 default:
                     arguments.set(arguments.size() - 1,
                             arguments.get(arguments.size() - 1) + (char)c);
                    escaped = false;
                     break;
             }
         }
     }
 
     private void execute(List<String> arguments) {
         CommandHandler handler = handlers.get(arguments.get(0));
         if (handler == null)
             handler = unknownCommandHandler;
         else
             arguments.remove(0);
         if (handler != null)
             handler.handle(arguments);
     }
 
     public void setCommandHandler(String command, CommandHandler handler) {
         handler.setName(command);
         handlers.put(command, handler);
     }
 
     public void setUnknownCommandHandler(CommandHandler handler) {
         unknownCommandHandler = handler;
     }
 }
