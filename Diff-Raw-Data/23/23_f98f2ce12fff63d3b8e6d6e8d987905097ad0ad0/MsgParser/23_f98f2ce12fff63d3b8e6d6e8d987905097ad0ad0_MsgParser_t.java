 import java.util.regex.*;
 
 public class MsgParser implements LineParser {
 
     private static final String sqlStart = "INSERT chat (nick,msg) values ";
 
    private static final String matchingRegEx = "^\\[All\\] ([^:]+): (.*)";
     private static final int matchingGroups = 2;
     private static final Pattern matchingPattern;
 
     public String nick;
     public String msg;
 
     static {
 	matchingPattern = Pattern.compile(matchingRegEx);
     }
 
     /**
      * Tries to parse a line. Returns false if it's not possible to parse.
      * In the case of true this method clears its previous state ad it is
      * guaranteed to have "fresh" values. In the case of false the object's
      * content is undefined.
      */
     public boolean match(String line) {
 
 	Matcher matcher = matchingPattern.matcher(line);
 	if (!matcher.matches() || matchingGroups != matcher.groupCount()) {
 	    return false; // this is not right pattern
 	}
 	
 	this.nick = matcher.group(1);
 	this.msg = matcher.group(2);
 
 	return true; // Success.
     }
 
     public void appendSQL(SQLBuilder sql) {
 	sql.appendRaw(sqlStart);
 	sql.appendRaw('(');
 	sql.appendString(nick);
 	sql.appendRaw(',');
 	sql.appendString(msg);
 	sql.appendRaw(");");
 
     }
 
     public void clear() {
 	this.nick = null;
 	this.msg = null;
     }
 
     public String parserName() {
 	return "Private message";
     }
 
     public String engineerDebug() {
 	return "NICK: "+ this.nick +
 	    "\nMsg: " + this.msg;
     }    
 }
