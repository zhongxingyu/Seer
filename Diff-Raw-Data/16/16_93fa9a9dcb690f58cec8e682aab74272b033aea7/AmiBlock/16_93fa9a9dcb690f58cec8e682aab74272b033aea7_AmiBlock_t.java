 
 package ch.loway.oss.adb.containers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  *
  *
  * $Id$
  * @author lenz
  */
 public class AmiBlock {
 
     List<String> lData = new ArrayList<String>();
     String comments = "";
     long timestamp = 0L;
     boolean userCommand = false;
 
     public void addEntry( String row ) {
 
         if ( !row.isEmpty()) {
             lData.add(row);
         }
         timestamp = System.currentTimeMillis();
     }
 
     public void setUserCommand() {
         userCommand = true;
     }
 
 
     public String getToken( String token ) {
         String tokenPlus = token + ": ";
 
         for ( String s: lData ) {
             if ( s.startsWith( tokenPlus )) {
                 return s.substring( tokenPlus.length() );
             }
         }
 
         return "";
     }
 
     private String cssClass() {
         if ( userCommand ) {
             return "amiCommand";
         }
 
        String reponse = getToken("Response");
        String event = getToken("Event");

        if ( !event.isEmpty() ) {
             return "amiEvent";
         }
        
        if ( !reponse.isEmpty() ) {
            if ( reponse.equalsIgnoreCase("error")) {
                return "amiError";
            } else {          
                return "amiResponse";
            }
         }
 
         return "amiOther";
 
 
 
     }
 
 
     public String getResponse() {
         return getToken("Response");
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         for ( String s: lData ) {
             sb.append(s).append( "\n" );
         }
         sb.append( "\n" );
         return sb.toString();
     }
 
     public String toHtml() {
         
         StringBuilder sb = new StringBuilder();        
         Date d = new Date( timestamp );
         String css = cssClass();
 
         sb.append( "<div class='").append( css ).append("'>");
         sb.append( "# ").append( d.toString() );
         if ( comments.length() > 0 ) {
             sb.append( " - ").append( comments );
         }
         sb.append( "<br>");
 
         for ( String s: lData ) {
             sb.append(s).append( "<br>" );
         }
 
         sb.append( "</div>");
         return sb.toString();
     }
 
 
     /**
      * Prepares a login commnad.
      * 
      * @param user
      * @param pass
      * @param events
      * @return
      */
 
     public static AmiBlock Login( String user, String pass, boolean events ) {
         AmiBlock a = new AmiBlock();
         a.addEntry("Action: login");
         a.addEntry("Username: " + user);
         a.addEntry("Secret: " + pass);
         a.addEntry("Events: " + (events ? "on" : "off"));
         return a;
     }
 
 
     public static AmiBlock LogOff() {
         AmiBlock a = new AmiBlock();
         a.addEntry("Action: logoff");
         return a;
     }
 
     public boolean isEmpty() {
         return ( lData.size() == 0);
     }
 
     /**
      * Effettua una copia "profonda" per portarla su un altro thread.
      * 
      * @return
      */
 
     public AmiBlock dup() {
         AmiBlock out = new AmiBlock();
         out.lData.addAll(lData);
         out.timestamp = timestamp;
         out.comments = comments;
         out.userCommand = userCommand;
         return out;
     }
 
 
 }
 
 // $Log$
 //
