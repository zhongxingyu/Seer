 /**
  * Song.java
  *
  * For holding song data.
  *
  * @author liufeng & aprilbugnot
  */
 package model;
 
 import java.io.Serializable;
 import java.util.Calendar;
 
 public class Song implements Serializable{
 
     private static int nextAccessNumber = 1;
 
     private String title;
     private String performer;
     private String recordingTitle;
     private String recordingType;
     private String year;
     private int length;
     private int accessNumber;
     private int popularity;
     private int playCount;
     private int requestCount;
     private Time addedTime;
     private Time lastPlayed;
     private long lastPopularized;
     private double priority;
 
 
     public Song(String title, String performer, String recordingTitle, String recordingType, String year,
             int length, int popularity, int accessNumber, int playCount, String addedTime, String lastPlayed){
 
         this.title = title;
         this.performer = performer;
         this.recordingTitle = recordingTitle;
         this.recordingType = recordingType;
         this.year = year;
         this.length = length;
         this.popularity = popularity;
         this.accessNumber = accessNumber;
         this.playCount = playCount;
         this.addedTime = new Time(addedTime);
 
         if(lastPlayed.equals("0"))
             this.lastPlayed = new Time();
         else{
             this.lastPlayed = new Time(lastPlayed);
         }
 
         if(accessNumber >= nextAccessNumber){
             nextAccessNumber = accessNumber + 1;
         }
 
         updatePriority();
         this.lastPopularized = Calendar.getInstance().getTimeInMillis();
     }
 
     public Song(String title, String performer, String recordingTitle, String recordingType, String year,
             int length, int popularity){
         this.title = title;
         this.performer = performer;
         this.recordingTitle = recordingTitle;
         this.recordingType = recordingType;
         this.year = year;
         this.length = length;
         this.popularity = popularity;
 
         this.accessNumber = nextAccessNumber;
         nextAccessNumber++;
 
         this.addedTime = new Time(Calendar.getInstance());
         this.lastPlayed = null;
 
         this.lastPopularized = Calendar.getInstance().getTimeInMillis();
     }
 
     // Accessors
     public String getTitle () {
         return title;
     }
 
     public String getPerformer () {
         return performer;
     }
 
     public String getRecordingTitle () {
         return recordingTitle;
     }
 
     public String getRecordingType () {
         return recordingType;
     }
 
     public String getYear () {
         return year;
     }
 
     public int getLength () {
         return length;
     }
 
     public int getAccessNumber () {
         return accessNumber;
     }
 
     public int getPopularity () {
         return popularity;
     }
 
     public int getNumberOfPlays () {
         //return numberOfPlays;
         return playCount;
     }
 
     public Time getLastPlayed () {
         return lastPlayed;
     }
 
     public double getPriority () {
         return priority;
     }
 
     public Time getAddedTime(){
         return addedTime;
     }
 
    public void setPopularity(int pop) {
        popularity = pop;
    }
     /**
      * Get the average played time of the song.
      * If the song hasn't added for 1 week, return the number
      * of plays of the song. (otherwise will get a <em>Divide
      * by zero</em> error.
      * @return an <code>int</code> value.
      */
     private int getAveragePlays () {
         int weeks = ( new Time().getCurrentTime().minus( addedTime ) ) / 7;
         if ( weeks == 0 ) {
             return playCount;
         } else {
             return playCount / weeks;
         }
     }
 
     public void setTitle(String title){
         this.title = title;
     }
 
     public void setPerformer(String performer){
         this.performer = performer;
     }
 
     public void setRecordingTitle(String recordingTitle){
         this.recordingTitle = recordingTitle;
     }
 
     public void setRecordingType(String recordingType){
         this.recordingType = recordingType;
     }
 
     public void setYear(String year){
         this.year = year;
     }
 
     /**
      * Set a new last played date if it is more recent than the current lastPlayed time
      * @param lastPlayed
      */
     
     public void setLastPlayed ( Time lastPlayed ) {
         if ( lastPlayed.minus( this.lastPlayed ) > 0 ) {
             this.lastPlayed = lastPlayed;
             updatePriority();
         }
     }
     /**
      * Increment the <code>playCount</code> by 1.
      */
     public void addNumberOfPlays () {
         this.playCount++;
     }
 
     /**
      * set the plays in week to 0 for a new week.
      */
     public void resetNumberOfPlays () {
         this.playCount = 0;
     }
     /**
      * update the priority by the algorithm in the
      * <em>user Requirement</em>.
      *
      * Happens every time the user alter the popularity and every time the
      * system makes schedule.
      */
     public void updatePriority () {
         double newPriority;
 
         if(lastPlayed != null)
             newPriority =  10 * popularity - 7 * getAveragePlays() - 16 / ( new Time().getCurrentTime().minus( lastPlayed ) );
         else
             newPriority = 10 * popularity - 7 * getAveragePlays();
 
         priority = newPriority;
     }
 
     /**
      * Increases the requestCount so popularity can be calculated.  Count
      * automatically reset to zero when <code>updatePopularity()</code> is called.
      */
     public void makeRequest(){
         requestCount++;
     }
 
     /**
      * Re-calculates the popularity of the song based on requests since last called.
      * Updates object and database values.
      */
     public void updatePopularity(){
         long timeSinceUpdated = (Calendar.getInstance().getTimeInMillis() - lastPopularized)/1000;
         double reqPerMinute = requestCount/(double)timeSinceUpdated * 60.0;
 
         if(reqPerMinute >= 1){
             popularity += reqPerMinute;
         }
 
         else{
             popularity -= (1.0/24.0);
         }
 
         requestCount = 0;
         lastPopularized = Calendar.getInstance().getTimeInMillis();
     }
 
 
     /**
      * Decide if the <code>song</code> is the same song to <code>this</code>.
      * @param song
      * @return <code>true</code> if <code>song</code> is the same song
      *         to <code>this</code>; <code>false</code> otherwise.
      * @author Kurtis Schmidt
      */
     public boolean equals ( Song song ) {
         return this.title.equalsIgnoreCase( song.title );
     }
 
     /**
      * Return the name of the song
      * @author Kurtis Schmidt
      */
     public String toString () {
         return title;
     }
 }
