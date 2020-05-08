 package system;
 
 /**
  * A class to encapsulate a talk with all the details about it accessible but 
  * not editable.
  * @author jonathanrainer
  */
 public final class Talk {
     // Integer to store the ID associated with this Talk.
     private int talkID;
     // Integer to store the year this talk was given.
     private int year;
     // String to store the speaker that gave the talk.
     private String speaker;
     // String to store the title of the talk itself
     private String title;
     // String to store the file name of this talk
     private String fileName;
     
     /**
      * A constructor to create each talk object.
      * @param talkID The ID of the talk .
      * @param year The year the talk was given.
      * @param speaker The speaker that gave the talk.
      * @param title The title of the talk they gave.
      * @param fileName The filename this talk will have
      */
     public Talk(int talkID, int year, String speaker, String title)
     {
         this.talkID = talkID;
         this.year = year;
         this.speaker = speaker;
         this.title = title;
         fileName = "gb" + getShortYear() + "-" + getNiceTalkID() + ".mp3";
     }
     
     /**
      * Return the name of the speaker who gave this talk
      * @return A string representing the name of the speaker giving the talk
      */
     public String getSpeaker() {
         return speaker;
     }
 
     /**
      * Return the ID of this talk
      * @return The ID of this talk
      */
     public int getTalkID() {
         return talkID;
     }
 
     /**
      * Return the Title of this talk
      * @return The title of this talk
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * Return the year the talk was given in, in 4 digit format
      * @return The year that the talk was given in in 4 digit format
      */
     public int getYear()
     {
         return year;
     }
     
     /**
      * Return a string representation of the year in 4 digit format
      * @return The long form of the year the talk was given in e.g. 2012
      */
     public String getLongYear() {
         return "" + year;
     }
     
     /**
      * Return the year the talk was given in, in 2 digit format
      * @return The short form of the year the talk was given in e.g. 12
      */
     public String getShortYear() 
     {
         String shortYear = "";
         shortYear = shortYear + year;
         shortYear = shortYear.substring(2);
         return shortYear;
     }
     
     public String getFileName()
     {
         return fileName;
     }
     
     public String getNiceTalkID()
     {
         String correctedTalkID = "";
         if(talkID < 100 && talkID >= 10)
         {
             correctedTalkID = "0" + talkID;
         }
        if(talkID < 10)
         {
             correctedTalkID = "00" + talkID;
         }
         else
         {
             correctedTalkID = "" + talkID;
         }
         return correctedTalkID;
     }
     
 }
