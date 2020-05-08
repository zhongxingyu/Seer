 import java.util.regex.*;
 
 public class JoinParser implements LineParser {
 
     private static final String sqlStart = "INSERT action (nick,action) values ";
 
    private static final String matchingRegEx = "^\\*\\*\\* (.*) on liittynyt peliin$";
     private static final int matchingGroups = 1;
     private static final Pattern matchingPattern;
 
     public String nick;
 
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
 
 	return true; // Success.
     }
 
     public void appendSQL(SQLBuilder sql) {
 	sql.appendRaw(sqlStart);
 	sql.appendRaw('(');
 	sql.appendString(nick);
 	sql.appendRaw(",'join');");
     }
 
     public void clear() {
 	this.nick = null;
     }
 
     public String parserName() {
 	return "Join";
     }
 
     public String engineerDebug() {
 	return "NICK: "+ this.nick;
     }    
 }
