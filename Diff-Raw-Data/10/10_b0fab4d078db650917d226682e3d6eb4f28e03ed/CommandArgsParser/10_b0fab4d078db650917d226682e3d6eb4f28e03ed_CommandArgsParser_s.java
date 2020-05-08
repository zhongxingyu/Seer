 package fr.aumgn.bukkitutils.command.args;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import fr.aumgn.bukkitutils.command.exception.CommandUsageError;
 import fr.aumgn.bukkitutils.command.messages.Messages;
 
 public class CommandArgsParser {
 
     private Messages messages;
     private String[] args;
     private Set<Character> flags;
     private Map<Character, String> argsFlags;
 
     public CommandArgsParser(Messages messages, String[] tokens) {
         this.messages = messages;
         parse(tokens);
     }
 
     private void parse(String[] tokens) {
         flags = new HashSet<Character>();
         argsFlags = new HashMap<Character, String>();
         ArrayList<String> argsList = new ArrayList<String>();
         boolean quoted = false;
         StringBuilder current = null;
 
         for (String token : tokens) {
 
             if (quoted) {
                 if (!token.isEmpty()
                         && token.charAt(token.length() - 1) == '\"') {
                     current.append(" ");
                     current.append(token.substring(0, token.length() - 1));
                     argsList.add(current.toString());
                     quoted = false;
                 } else {
                     current.append(" ");
                     current.append(token);
                 }
             } else {
                 if (token.isEmpty()) {
                     continue;
                 }
 
                 if (token.charAt(0) == '"') {
                    quoted = true;
                    current = new StringBuilder();
                    current.append(token.substring(1));
                 } else if (token.charAt(0) == '-' && token.length() > 1
                         && Character.isLetter(token.charAt(1))) {
                     parseFlags(token.substring(1));
                 } else {
                     argsList.add(token);
                 }
             }
         }
 
         if (quoted) {
             throw new CommandUsageError(
                     messages.missingEndingQuote(current.toString()));
         }
 
         args = argsList.toArray(new String[argsList.size()]);
     }
 
     private void parseFlags(String flagsString) {
         int equal = flagsString.indexOf("=");
         if (equal == -1) {
             parseRegularFlags(flagsString);
             return;
         }
 
         if (equal > 1) {
             parseRegularFlags(flagsString.substring(0, equal -1));
         }
 
         argsFlags.put(
                 flagsString.charAt(equal - 1),
                 flagsString.substring(equal + 1));
     }
 
     private void parseRegularFlags(String flagsString) {
         for (char flag : flagsString.toCharArray()) {
             flags.add(flag);
         }
     }
 
     public void validate(Set<Character> allowedFlags,
             Set<Character> allowedArgsFlags, int min, int max) {
         StringBuilder invalidFlags = new StringBuilder();
         for (char flag : flags) {
             if (!allowedFlags.contains(flag)) {
                 invalidFlags.append(flag);
             }
         }
 
         for (char flag : argsFlags.keySet()) {
             if (!allowedArgsFlags.contains(flag)) {
                 invalidFlags.append(flag);
             }
         }
 
         if (invalidFlags.length() > 0) {
             throw new CommandUsageError(messages.invalidFlag(invalidFlags.toString()));
         }
 
         if (args.length < min) {
             throw new CommandUsageError(messages.missingArguments(args.length, min));
         }
         if (max != -1 && args.length > max) {
             throw new CommandUsageError(messages.tooManyArguments(args.length, max));
         }
     }
 
     public Set<Character> getFlags() {
         return flags;
     }
 
     public Map<Character, String> getArgsFlags() {
         return argsFlags;
     }
 
     public String[] getArgs() {
         return args;
     }
 }
