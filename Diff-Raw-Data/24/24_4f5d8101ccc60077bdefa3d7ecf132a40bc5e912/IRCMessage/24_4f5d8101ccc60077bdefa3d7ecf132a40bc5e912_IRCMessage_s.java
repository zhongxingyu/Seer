 
 package me.heldplayer.irc.api;
 
 public class IRCMessage {
 
     /*
      * message = [ ":" prefix SPACE ] command [ params ] crlf
      * prefix = servername / ( nickname [ [ "!" user ] "@" host ] )
      * command = 1*letter / 3digit
      * params = *14( SPACE middle ) [ SPACE ":" trailing ]
      * =/ 14( SPACE middle ) [ SPACE [ ":" ] trailing ]
      * 
      * nospcrlfcl = %x01-09 / %x0B-0C / %x0E-1F / %x21-39 / %x3B-FF; any octet
      * except NUL, CR, LF, " " and ":"
      * middle = nospcrlfcl *( ":" / nospcrlfcl )
      * trailing = *( ":" / " " / nospcrlfcl )
      * 
      * SPACE = %x20 ; space character
      * crlf = %x0D %x0A ; "carriage return" "linefeed"
      */
 
     public final String message;
     public final String prefix;
     public final String command;
     public final String trailing;
     public final String[] params;
 
     public IRCMessage(String input) {
         String processing = input;
         this.message = input;
         if (processing.startsWith(":")) {
            this.prefix = processing.substring(1, processing.indexOf(' '));
            processing = processing.substring(processing.indexOf(' ') + 1);
         }
         else {
             this.prefix = null;
         }
        if (processing.indexOf(' ') > 0) {
            this.command = processing.substring(0, processing.indexOf(' '));
            processing = processing.substring(processing.indexOf(' ') + 1);
         }
         else {
             this.command = processing;
         }
        if (processing.indexOf(':') > 0) {
             this.trailing = processing.substring(processing.indexOf(" :") + 2);
             processing = processing.substring(0, processing.indexOf(" :"));
         }
         else if (processing.indexOf(':') == 0) {
             this.trailing = processing.substring(processing.indexOf(":") + 1);
             processing = processing.substring(0, processing.indexOf(":"));
         }
         else {
             this.trailing = null;
         }
         if (!processing.trim().isEmpty()) {
             this.params = processing.split(" ");
         }
         else {
             this.params = new String[0];
         }
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append(this.command);
         for (String param : params) {
             builder.append(" ").append(param);
         }
         if (this.trailing != null) {
             builder.append(" :").append(this.trailing);
         }
         return builder.toString();
     }
 
 }
