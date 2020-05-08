 package com.cloudbees.sdk.commands;
 
 import com.cloudbees.sdk.cli.AbstractCommand;
 import com.cloudbees.sdk.cli.CLICommand;
 import com.cloudbees.sdk.cli.CommandGroup;
 import com.cloudbees.sdk.CommandServiceImpl;
 import com.cloudbees.sdk.cli.ICommand;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.Option;
 
 import javax.inject.Inject;
import java.util.ArrayList;
 import java.util.Collections;
import java.util.List;
 
 /**
  * Help command.
  * 
  * @author Kohsuke Kawaguchi
  */
 @CommandGroup("SDK")
 @CLICommand("help")
 public class Help extends AbstractCommand {
     @Inject
     CommandServiceImpl commandService;
 
     /**
      * Sub-command to retrieve the help
      */
     @Argument(metaVar="CMD")
     public String subCommand;
 
     @Option(name="-all",usage="Show all commands, including those that are experimental")
     public boolean all;
 
     @Override
     public int main() throws Exception {
         ICommand cmd = null;
 
         if (subCommand!=null)
             cmd = commandService.getCommand(subCommand);
        if (cmd!=null) {
            List<String> args = new ArrayList<String>();
            args.add(subCommand);
            if (all)
                args.add("-all");
            cmd.printHelp(args);
        } else
             printHelp(all);
 
         return 1;
     }
 
     public void printHelp(boolean all) {
         System.out.println(commandService.getHelp(getClass().getClassLoader().getResource(help_file_name), "subcommands:", all));
     }
 
     private final static String help_file_name = "beesHelp.txt";
 }
