 package org.gethydrated.hydra.cli.commands;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.gethydrated.hydra.api.service.ServiceContext;
 
 /**
  * 
  * @author Hanno Sternberg
  * @since 0.1.0
  * 
  */
 public abstract class CLICommand {
 
     /**
          * 
          */
     private List<CLICommand> subCommands;
 
     /**
          * 
          */
     private ServiceContext context;
 
     /**
      * Full text for command help.
      */
     private String helpText;
 
     /**
      * Short description of the command.
      */
     private String shortDescr;
 
     /**
      * 
      * @param ctx
      *            Service context.
      */
     public CLICommand(final ServiceContext ctx) {
         setContext(ctx);
         subCommands = new LinkedList<CLICommand>();
         helpText = generateHelpText();
         shortDescr = generateShortDescr();
     }
 
     /**
      * 
      * @return OutputStream.
      */
     public final PrintStream getOutput() {
         return getContext().getOutputStream();
     }
 
     /**
      * 
      * @return The command word.
      */
     public abstract String getCommandWord();
 
     /**
      * @return The short form of the command word.
      */
     public abstract String getCommandShort();
 
     /**
      * 
      * @return Context of the service.
      */
     public final ServiceContext getContext() {
         return context;
     }
 
     /**
      * 
      * @param ctx
      *            service context
      */
     private void setContext(final ServiceContext ctx) {
         context = ctx;
     }
 
     /**
      * 
      * @return Help text
      */
     public final String getHelpText() {
         return helpText;
     }
 
     /**
      * 
      * @return Help text.
      */
     protected abstract String generateHelpText();
 
     /**
      * 
      * @return Short command description.
      */
     public final String getShortDescription() {
         return shortDescr;
     }
 
     /**
      * 
      * @return Short description
      */
     protected abstract String generateShortDescr();
 
     /**
      * 
      * @param cmd
      *            new Sub command.
      */
     public final void addSubCommand(final CLICommand cmd) {
         subCommands.add(cmd);
     }
 
     /**
      * 
      * @return True, if this command has additional sub commands.
      */
     public final Boolean hasSubCommands() {
         return subCommands.size() > 0;
     }
 
     /**
      * 
      * @param args
      *            Array with arguments.
      */
     public abstract void execute(final String[] args);
 
     /**
      * 
      * @param args
      *            Array with arguments.
      */
     private void executeSecure(final String[] args) {
         try {
             execute(args);
         } catch (Exception e) {
             getOutput().println("Caught exception in command execution!");
             e.printStackTrace(getOutput());
         }
     }
 
     /**
      * Displays the help text.
      */
     public final void displayHelp() {
         getOutput().printf("Help for command %s - ", getCommandWord());
         getOutput().println(getShortDescription());
         getOutput().println();
         if (getHelpText() != "") {
             getOutput().println("Long Description: ");
             getOutput().println(getHelpText());
             getOutput().println();
         }
         if (hasSubCommands()) {
             getOutput().println("List of sub commands");
             for (CLICommand cmd : subCommands) {
                 getOutput().printf("\t%s: %s", cmd.getCommandWord(),
                         cmd.getShortDescription());
                 getOutput().println();
             }
             getOutput().println();
         }
         getOutput().println("Type '<command> -help' for further information");
         getOutput().println();
     }
 
     /**
      * 
      * @param str
      *            possible command word.
      * @return True, if "str" is a sub command.
      */
     public final Boolean hasSubCommand(final String str) {
         for (CLICommand c : subCommands) {
             if (c.testString(str)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Tests if the given String is this command.
      * 
      * @param str
      *            the string
      * @return True, if the String is this command.
      */
     public final Boolean testString(final String str) {
         return getCommandWord().equalsIgnoreCase(str)
                 || getCommandShort().equals(str);
     }
 
     /**
      * 
      * @param str
      *            String.
      * @return True, if the String is a sub command.
     * @throws CLISubCommandDoesNotExistsException .
      */
     public final CLICommand isSubCommand(final String str)
             throws CLISubCommandDoesNotExistsException {
         for (CLICommand cmd : subCommands) {
             if (cmd.testString(str)) {
                 return cmd;
             }
         }
         throw new CLISubCommandDoesNotExistsException(str);
     }
 
     /**
      * 
      * @param cmd
      *            The command string.
      */
     public final void parse(final String cmd) {
         if (cmd.contains("\"")) {
             String[] parts = cmd.split("\"");
             int i = 0;
             ArrayList<String> result = new ArrayList<String>();
             for (i = 0; i < parts.length; i++) {
                 if (i == 0) {
                     for (String c : parts[0].split(" ")) {
                         result.add(c);
                     }
                 } else {
                     if (parts[i].trim() != "") {
                         result.add(parts[i]);
                     }
                 }
             }
         } else {
             parse(cmd.split(" "));
         }
     }
 
     /**
      * 
      * @param cmds
      *            .
      */
     public final void parse(final String[] cmds) {
         if (cmds.length > 0
                 && (cmds[0].equalsIgnoreCase("-help")
                         || cmds[0].equalsIgnoreCase("--h") || cmds[0]
                             .equalsIgnoreCase("-?"))) {
             displayHelp();
         } else if (cmds.length > 0 && hasSubCommands()) {
             CLICommand subCmd;
             try {
                 subCmd = isSubCommand(cmds[0]);
                 String[] rest = new String[cmds.length - 1];
                 int i;
                 for (i = 1; i < cmds.length; i++) {
                     rest[i - 1] = cmds[i];
                 }
                 subCmd.parse(rest);
             } catch (CLISubCommandDoesNotExistsException e) {
                 getOutput().printf("No sub command %s", cmds[0]);
                 getOutput().println();
             }
         } else {
             executeSecure(cmds);
         }
 
     }
 
 }
