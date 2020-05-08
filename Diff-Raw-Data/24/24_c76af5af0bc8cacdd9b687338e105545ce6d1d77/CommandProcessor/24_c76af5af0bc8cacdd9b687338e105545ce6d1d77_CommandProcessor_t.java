 /*
  * Copyright 2011 VMWare.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package demo.vmware.commands;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.springframework.context.ConfigurableApplicationContext;
 
 /**
  * The core command dispatcher. All commands are invoked through this.
  * 
  * @author freemanj
  * 
  */
 public class CommandProcessor implements ICommandProcessor {
 
     final static Logger LOG = Logger.getLogger(CommandProcessor.class);
 
     private List<ICommand> commands = new ArrayList<ICommand>();
 
     public CommandProcessor(List<ICommand> commands) {
         this.commands = commands;
     }
 
     @Override
     public void run(ConfigurableApplicationContext mainContext) {
         userMenu(mainContext);
     }
 
     void printMenuListToStdOut() {
         System.out.println();
         List<String> menuStrings = getMenuList("\n", "\n");
         for (String oneMenu : menuStrings) {
             System.out.println(oneMenu);
         }
         System.out.print("Your Choice: ");
     }
 
     /**
      * returns an array of menu items. Embedded line breaks are converted so that this can work with stdout or HTML
      * 
      * @param originalLineBreak
      * @param newLineBreak
      * @return list of menu strings for UI to display
      */
     public List<String> getMenuList(String originalLineBreak, String newLineBreak) {
         // should cache this
         List<String> menuStrings = new ArrayList<String>();
         int index = 0;
         for (ICommand oneCommand : commands) {
             String oneMenuItem = index + ": " + oneCommand.commandDescription() + "    Usage: "
                     + oneCommand.usageDescription();
             oneMenuItem = oneMenuItem.replace(originalLineBreak, newLineBreak);
             menuStrings.add(oneMenuItem);
             index++;
         }
         return menuStrings;
     }
 
     public static final Pattern UGLY_PARSING_PATTERN = Pattern
             .compile("\".*?(?<!\\\\)\"|'.*?(?<!\\\\)'|[A-Za-z0-9-_.']+");
 
     /**
      * Command dispatch loop invokes the appropriate spring wired command
      * 
      * @param mainContext
      * @throws Exception
      */
     public void userMenu(ConfigurableApplicationContext mainContext) {
         while (true) {
             CommandResult results;
             printMenuListToStdOut();
             Scanner s = createScanner(System.in);
 
             String fixedChoice = getNextToken(s);
             int choice = Integer.parseInt(fixedChoice);
             List<String> parameters = new ArrayList<String>();
             // TODO should have validateChoice() method
             if (choice >= 0 && choice < commands.size()) {
                 while (parameters.size() < commands.get(choice).numberOfParameters()) {
                    String nextToken = getNextToken(s);
                    if (nextToken != null) {
                        parameters.add(nextToken);
                    } else {
                        LOG.error("Not enough parameters entered on command " + choice);
                        break;
                    }
                 }
             }
             try {
                 // we should never catch exceptions but we will so we don't kill the server or do something else bad
                 results = forwardCommand(mainContext, choice, parameters);
             } catch (Exception e) {
                 LOG.error("Command Error ", e);
                 results = new CommandResult(null, "Command Error " + e.getMessage());
             }
             for (String oneMessage : results.getMessages()) {
                 System.out.println(oneMessage);
             }
         }
     }
 
     /**
      * 
      * @param s
      * @return next token that could have been quoted string with quotes removed
      */
     String getNextToken(Scanner s) {
        String nextToken = s.findInLine(UGLY_PARSING_PATTERN);
        if (nextToken == null) {
            return nextToken;
        } else {
            return nextToken.replace("\"", "");
        }
     }
 
     /**
      * protected so we can unit test
      * 
      */
     Scanner createScanner(InputStream source) {
         // add support for quoted strings
         Scanner s = new Scanner(source);
         s.useDelimiter(UGLY_PARSING_PATTERN);
         return s;
     }
 
     /**
      * make it's own method for unit testing
      * 
      * @param mainContext
      * @param choice
      * @param parameters
      * @return CommandResult so caller can display messages
      */
     protected CommandResult forwardCommand(ConfigurableApplicationContext mainContext, int choice,
             List<String> parameters) {
         if (choice >= 0 && choice < commands.size()) {
            ICommand oneCommand = commands.get(choice);
            if (oneCommand.numberOfParameters() == parameters.size()) {
                return oneCommand.run(mainContext, parameters);
            } else {
                return new CommandResult(null, "Incorrect number of parameters for: " + choice);
            }
         } else {
             return new CommandResult(null, "Unrecognized command: " + choice);
         }
     }
 
 }
