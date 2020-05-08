 package com.btbb.irc;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Request {
 
     private String line;
     private String C = "$";
 
     public Request(String line)
         {
             this.line = line;
         }
 
     public String getChannel() {
         Pattern regex = Pattern.compile("^:(.+)!(.+)@(.+) PRIVMSG (#?[a-zA-Z_0-9\\-]+) :(.+)", Pattern.CASE_INSENSITIVE);
         Matcher match = regex.matcher(this.line);
         if (!match.find()) {
             return null;
         }
         return match.group(4);
     }
 
     public String getSender() {
         /*      if (line.startsWith(":" + Main.mcBot + "!")) {
                   Pattern regex = Pattern.compile("^:(.+)!(.+)@(.+) PRIVMSG " + Main.channel + " :\\(([A-Za-z0-9_]+)\\) (.+)",
                           Pattern.CASE_INSENSITIVE);
                   Matcher match = regex.matcher(this.line);
                   if (!match.find()) {
                       // Should not happen anyway
                       return Main.mcBot;
                   }
                   return match.group(4);
               } else {*/
         Pattern regex = Pattern.compile("^:(.+)!(.+)@(.+) PRIVMSG", Pattern.CASE_INSENSITIVE);
         Matcher match = regex.matcher(this.line);
         if (!match.find()) {
             return null;
         }
         return match.group(1);
         /*}*/
     }
 
     public String getMessage() {
         /* if (line.startsWith(":" + Main.mcBot + "!")) {
              Pattern regex = Pattern.compile("^:(.+)!(.+)@(.+) PRIVMSG " + Main.channel + " :\\((.+)\\) (.+)$",
                      Pattern.CASE_INSENSITIVE);
              Matcher match = regex.matcher(this.line);
              if (!match.find()) {
                  return null;
              }
              return match.group(5);
          } else {
          //CHANGES : Replaced " + Main.channel + " with (.+)
           */Pattern regex = Pattern.compile("^:(.+)!(.+)@(.+) PRIVMSG (#?[a-zA-Z_0-9\\-]+) :(.+)$", Pattern.CASE_INSENSITIVE);
         Matcher match = regex.matcher(this.line);
         if (!match.find()) {
             return null;
         }
         return match.group(5);
         /*}*/
     }
 
     public boolean hasOwnership() {
         return getSender().equalsIgnoreCase(IRCBot.bot.MANDATORY_OWNER);
     }
 
     public boolean isCommand() {
         Pattern regex = Pattern.compile("^:(.+)!(.+)@(.+) PRIVMSG (#?[a-zA-Z_0-9\\-]+) :" + Pattern.quote(C) + "(.+)", Pattern.CASE_INSENSITIVE);
         Matcher match = regex.matcher(this.line);
         return match.matches();
     }
 
     public boolean isMSG() {
        Pattern regex = Pattern.compile("^:(.+)!(.+)@(.+) PRIVMSG (.+)", Pattern.CASE_INSENSITIVE);
         Matcher match = regex.matcher(this.line);
         return match.matches();
     }
 
     public Matcher getKick() {
         //:DarkAceZ!~BillyMays@50.107.52.50 KICK #HAX4ALL dickkkk :dickkkk
         Pattern regex = Pattern.compile("^:(.+)!(.+)@(.+) KICK (.+) " + IRCBot.nick + " :" +IRCBot.nick, Pattern.CASE_INSENSITIVE);
         Matcher match = regex.matcher(this.line); 
         return match;
     }
     
     public boolean isNotice() {
         Pattern regex = Pattern.compile("^:(.+) NOTICE (.+)", Pattern.CASE_INSENSITIVE);
         Matcher match = regex.matcher(this.line);
         return match.matches();
     }
 
     public boolean isGhostedMessage() {
         Pattern regex = Pattern.compile("^:(.+) NOTICE " + IRCBot.nick + " :(.?)" + IRCBot.bot.NICK
                 + "(.?) has been ghosted.", Pattern.CASE_INSENSITIVE);
         Matcher match = regex.matcher(this.line);
         return match.matches();
     }
 
     /**
      * Do isMSG() then to check do isPM()
      * 
      * @return
      */
     public boolean isPM() {
         Pattern regex = Pattern.compile("^:(.+)!(.+)@(.+) PRIVMSG #(.+) :(.+)", Pattern.CASE_INSENSITIVE);
         Matcher match = regex.matcher(this.line);
         return !match.matches();
     }
 
     public boolean isInvite() {
         Pattern regex = Pattern.compile("^:(.+)!(.+)@(.+) INVITE (.+)", Pattern.CASE_INSENSITIVE);
         Matcher match = regex.matcher(this.line);
         return match.matches();
     }
 
     public boolean isPing() {
         Pattern pingRegex = Pattern.compile("^PING", Pattern.CASE_INSENSITIVE);
         Matcher ping = pingRegex.matcher(this.line);
         return ping.find();
     }
 
     public String getPingMessage() {
         Pattern pingRegex = Pattern.compile("^PING :(.*)$", Pattern.CASE_INSENSITIVE);
         Matcher ping = pingRegex.matcher(this.line);
         ping.find();
         return ping.group(1);
     }
 
     public String getInviteMessage() {
         Pattern pingRegex = Pattern.compile("^:(.+)!(.+)@(.+) INVITE " + IRCBot.nick + " :(.+)$",
                 Pattern.CASE_INSENSITIVE);
         Matcher ping = pingRegex.matcher(this.line);
         if (!ping.find())
             return null;
         return ping.group(4);
     }
 
     public boolean isMode() {
         Pattern pingRegex = Pattern.compile(":" + IRCBot.nick + " MODE " + IRCBot.nick + " :(.+)",
                 Pattern.CASE_INSENSITIVE);
         Matcher ping = pingRegex.matcher(this.line);
         return ping.matches();
     }
 
     public int isNumber() {
         Matcher m;
         Pattern regex = Pattern.compile("^:(.+)!(.+) PRIVMSG (#?[a-zA-Z_0-9\\-]+) :" + Pattern.quote(C) + "([0-9]+)", Pattern.CASE_INSENSITIVE);
         m = regex.matcher(this.line);
         if (!m.matches()) {
             return -1;
         }
         int i = -1;
         try {
             i = Integer.parseInt(m.group(4));
         } catch (Exception e) {
         }
         return i;
     }
 
     public boolean isError(String error) {
         Pattern pingRegex = Pattern.compile(":(.+) " + error + "(.+) (.+) :(.+)", Pattern.CASE_INSENSITIVE);
         Matcher ping = pingRegex.matcher(this.line);
         return ping.matches();
     }
 
     public boolean match(String command) {
         /* if (line.startsWith(":" + Main.mcBot + "!")) {
              Pattern regex = Pattern.compile("^:(.+)!(.+) PRIVMSG " + Main.channel + " :\\((.+)\\) !" + command,
                      Pattern.CASE_INSENSITIVE);
              Matcher m = regex.matcher(this.line);
              return m.matches();
          }*/
         Pattern regex = Pattern.compile("^:(.+)!(.+) PRIVMSG (#?[a-zA-Z_0-9\\-]+) :" + Pattern.quote(C + command), Pattern.CASE_INSENSITIVE);
         Matcher m = regex.matcher(this.line);
         
         return m.matches();
     }
 
     public String matchArg(String command) {
         Matcher m;
         int n;
         /* if (line.startsWith(":" + Main.mcBot + "!")) {
              Pattern regex = Pattern.compile(
                      "^:(.+)!(.+) PRIVMSG " + Main.channel + " :\\((.+)\\) !" + command + " (.+)",
                      Pattern.CASE_INSENSITIVE);
              m = regex.matcher(this.line);
              n = 4;
          } else {
           */Pattern regex = Pattern.compile("^:(.+)!(.+) PRIVMSG (#?[a-zA-Z_0-9\\-]+) :" + Pattern.quote(C + command) + " (.+)",
                 Pattern.CASE_INSENSITIVE);
         m = regex.matcher(this.line);
         n = 4;
         /* }*/
         if (!m.matches()) {
             return null;
         }
         return m.group(n);
     }
 
     public String[] matchArg2(String command) {
         Matcher m;
         int n;
         /* if (line.startsWith(":" + Main.mcBot + "!")) {
              Pattern regex = Pattern.compile(
                      "^:(.+)!(.+) PRIVMSG " + Main.channel + " :\\((.+)\\) !" + command + " (.+) (.+)",
                      Pattern.CASE_INSENSITIVE);
              m = regex.matcher(this.line);
              n = 4;
          } else {
           */Pattern regex = Pattern.compile("^:(.+)!(.+) PRIVMSG (#?[a-zA-Z_0-9\\-]+) :" + Pattern.quote(C + command) + " (.+) (.+)",
                 Pattern.CASE_INSENSITIVE);
         m = regex.matcher(this.line);
         n = 4;
         /*}*/
         if (!m.matches()) {
             return null;
         }
         return new String[]
             { m.group(n), m.group(n + 1) };
     }
 
     public String[] matchArgName1(String command) {
         Pattern regex = Pattern.compile("^:(.+)!(.+) PRIVMSG (#?[a-zA-Z_0-9\\-]+) :" + Pattern.quote(C + command) + " ([A-Za-z0-9_.-]+) (.+)",
                 Pattern.CASE_INSENSITIVE);
         Matcher m = regex.matcher(this.line);
         if (!m.matches()) {
             return null;
         }
         return new String[]
             { m.group(4), m.group(5) };
     }
 }
