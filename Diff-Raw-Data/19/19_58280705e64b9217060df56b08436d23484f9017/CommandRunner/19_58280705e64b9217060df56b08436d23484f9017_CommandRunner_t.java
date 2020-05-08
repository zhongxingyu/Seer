 package com.mulesoft.jockey;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.MissingOptionException;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 public class CommandRunner {
     private Map<String, AbstractCommand> commands = new HashMap<String, AbstractCommand>();
     
     public void run(String... args) throws ParseException {
         if (args.length < 0) {
             throw new IllegalArgumentException("A command must be specified.");
         }
         
         String commandName = args[0];
         AbstractCommand command = getCommand(commandName);

         if (command == null) {
             throw new IllegalArgumentException("A command with the name " + commandName + " was not found.");
         }
         
         // Remove the first arg, which is the command name
         List<String> argList = new ArrayList<String>(Arrays.asList(args));
         argList.remove(0);
         
         // Create an option group
         Options options = new Options();
         for (com.mulesoft.jockey.Option o : command.getOptions()) {
             String longName = o.longName();
             if ("".equals(longName)) {
                 longName = null;
             }
             
             Option commonsOpt = new Option(o.name(), longName, o.argNeeded(), o.description());
             commonsOpt.setRequired(!o.optional());
             
             if (o.multiple()) {
                 commonsOpt.setArgs(Option.UNLIMITED_VALUES);
             }
             options.addOption(commonsOpt);
         }
         options.addOption(new Option("h", "help", false, "Print out help for this command."));
         
         try {
             // Parse out options
             PosixParser parser = new PosixParser();
             CommandLine cl = parser.parse(options, (String[])argList.toArray(new String[argList.size()]), true);
             
             if (cl.hasOption('h')) {
                 printHelpMessage(commandName, options);
                 return;
             }
             
             Map<String,Object> resolved = resolve(Arrays.asList(cl.getOptions()), command);
     
             // supply defaults
             for (com.mulesoft.jockey.Option o : command.getOptions()) {
                 String key = getKeyName(o);
                 if (o.defaultValue().length() > 0 && !resolved.containsKey(key)) {
                     resolved.put(key, o.defaultValue());
                 }
             }
             
             
             // Execute the command
             try {
                 command.execute(resolved);
             } catch (ScriptException e) {
                 System.err.println(e.getMessage());
             }
         } catch (MissingOptionException e) {
             printHelpMessage(commandName, options);
         }
     }
     
     private String getKeyName(com.mulesoft.jockey.Option o) {
         if (o.propertyName().length() > 0) {
             return o.propertyName();
         }
         
         if (o.longName().length() > 0) {
             return o.longName();
         }
         
         return o.name();
     }
     public void printHelpMessage(String commandName, Options options) {
         HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp( commandName, options );
     }
 
     private Map<String, Object> resolve(List<Option> options, AbstractCommand command) {
         Map<String, Object> vars = new HashMap<String,Object>();
         for (Option o : options) {
             com.mulesoft.jockey.Option annotation = null;
             for (com.mulesoft.jockey.Option match : command.getOptions()) {
                 if ((match.longName().length() == 0 && match.name().equals(o.getOpt())) 
                         || (match.longName().equals(o.getLongOpt()))) {
                     annotation = match;
                     break;
                 }
             }
             
             if (annotation == null) {
                 throw new NullPointerException("Could not find option " + o.getOpt());
             }
             
             String key = getKeyName(annotation);
             if (o.hasArgs()) {
                 vars.put(key, o.getValues());
             } else {
                 vars.put(key, o.getValue());
             }
         }
         return vars;
     }
     
     public AbstractCommand getCommand(String name) {
         return commands.get(name);
     }
     
     public void addCommand(AbstractCommand cmd) {
         commands.put(cmd.getName(), cmd);
     }
 
     public void addCommands(List<AbstractCommand> commands2) {
         for (AbstractCommand c : commands2) {
             addCommand(c);
         }
     }
 }
