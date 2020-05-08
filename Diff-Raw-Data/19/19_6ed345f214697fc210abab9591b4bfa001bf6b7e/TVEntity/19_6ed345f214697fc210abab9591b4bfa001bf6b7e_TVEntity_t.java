 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 /** This Objects holds all content of a shows DB Entry
   */
 public class TVEntity{
   private int showID;
   private int runtime;
   private String showname;
   private String airtime;
   private String airday;
   private String timezone;
   private String status;
 
   private boolean next = false;
   private int nextEpisode = -1;
   private int nextSeason = -1;
   private String nextTitle = null;
   private GregorianCalendar nextDate = null;
 
   /** Constructor if show does not have next episode
     * 
     * @param showID ID of show
     * @param showname Name of the show
     * @param airtime Time the show airs
     * @param airday Day the show airs
     * @param timezone Timezone the show airs in
     * @param status The status of the show in the database
     */
   public TVEntity( int showID, String showname, String airtime, String airday, String timezone, String status, int runtime ){
     this.showID = showID;
     this.showname = showname;
     this.airtime = airtime;
     this.airday = airday;
     this.timezone = timezone;
     this.status = status;
     this.runtime = runtime;
   }
 
   /** Constructor if show has next Episode
     * 
     * @param showID ID of show
     * @param showname Name of the show
     * @param airtime Time the show airs
     * @param airday Day the show airs
     * @param timezone Timezone the show airs in
     * @param status The status of the show in the database
     */
   public TVEntity( int showID, String showname, String airtime, String airday, String timezone, String status, int runtime, String nextTitle, int nextEpisode, int nextSeason, String nextD ){
    if( nextEpisode != -1 ) next = true;
     this.showID = showID;
     this.showname = showname;
     this.airtime = airtime;
     this.airday = airday;
     this.timezone = timezone;
     this.status = status;
     this.nextEpisode = nextEpisode;
     this.nextSeason = nextSeason;
     this.nextTitle = nextTitle;
     this.runtime = runtime;
 
     // Set the date
    try{
      String[] split = nextD.split( "\\?" );
      nextDate = new GregorianCalendar( Integer.parseInt( split[0].split( "-" )[2] ), Integer.parseInt( split[0].split( "-" )[1] ) - 1, Integer.parseInt( split[0].split( "-" )[0] ), Integer.parseInt( split[1].split( ":" )[0] ), Integer.parseInt( split[1].split( ":" )[1] ) );
      // Set the Timezone
      TimeZone tz = TimeZone.getTimeZone( timezone );
      nextDate.setTimeZone( tz );
    }catch( Exception e ){ }
   }
 
   /** Returns if the show has a next episode
     * @return int
     */
   public boolean hasNext(){ return next; }
 
   /** Returns the next episodes title
     * @return String
     */
   public String getNextTitle(){ return nextTitle; }
 
   /** Returns the next episode number
     * @return int
     */
   public int getNextEpisode(){ return nextEpisode; }
 
   /** Returns the season number
     * @return int
     */
   public int getNextSeason(){ return nextSeason; }
 
   /** Returns the date and time of the next episode
     * @return Calendar
     */
   public Calendar getNextDate(){ return nextDate; }
 
   /**Returns the shows ID
     * @return int
     */
   public int getShowid(){ return showID; }
 
   /**Returns the shows name
     * @return String
     */
   public String getShowname(){ return showname; };
 
   /**Returns the shows airtime
     * @return String
     */
   public String getAirtime(){ return airtime; };
 
   /**Returns the shows airday
     * @return String
     */
   public String getAirday(){ return airday; };
 
   /**Returns the shows timezone
     * @return String
     */
   public String getTimezone(){ return timezone; };
 
   /**Returns the shows status
     * @return String
     */
   public String getStatus(){ return status; };
 
   /**Returns the String representation of this Object
     * @return String
     */
   public String toString(){
     String toReturn = "[ID: " + showID + "]";
     toReturn += ", [showname: " + showname + "]";
     toReturn += ", [airtime: " + airtime + "]";
     toReturn += ", [airday: " + airday + "]";
     toReturn += ", [timezone: " + timezone + "]";
     toReturn += ", [status: " + status + "]";
     toReturn += ", [runtime: " + runtime + "]";
     if( hasNext() ){
       toReturn += ", [next Title: " + nextTitle + "]";
       toReturn += ", [next Episode: " + nextEpisode + "]";
       toReturn += ", [next Season: " + nextSeason + "]";
      if( nextDate != null )
	toReturn += ",[next Date: " + nextDate.getTime().toString() + "]";
     }
     return toReturn;
   }
 
 }
