 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 /** This Class holds information about a shows upcomming episode
   *
   * @author stylesuxx
   * @version 0.1 
   */
 public class NextEpisodeEntity{
   private int showID = -1;
   private int season = -1;
   private int episode = -1;
   private String showTitle = null;
   private String episodeTitle = null;
   private Calendar dateTime = null;
 
   /** Default Constructor
     *
     * @param showID ID of the show 			- Show ID
     * @param next The next field of the show 		- Next Episode
     * @param showTitle Title of the show 		- Show Name
     * @param dayTime Day and Time field of the show 	- RFC3339
     */
   public NextEpisodeEntity( int showID, String next, String showTitle, String dayTime ){
     this.showID = showID;
     this.showTitle = showTitle;
 
     // Split the next Line to get the Episodes Title, Season and Episode
     String[] split = next.split( "\\^" );
     season = Integer.parseInt( split[0].split( "x" )[0] );
     episode = Integer.parseInt( split[0].split( "x" )[1] );
     episodeTitle = split[1];
 
     // Split the dayTime and set the Calendar
     split = dayTime.split( "T" );
    int year = Integer.parseInt( split[0].split( "-" )[0] );
    int month = Integer.parseInt( split[0].split( "-" )[1] );
    int day = Integer.parseInt( split[0].split( "-" )[2] );
 
    int hour = Integer.parseInt( split[1].split( ":" )[0] );
    int minute = Integer.parseInt( split[1].split( ":" )[1] );
 
    dateTime = new GregorianCalendar();
    dateTime.set( year, month, day, hour, minute );
   }
 
   /** Returns the shows ID
     * 
     * @return int
     */
   public int getShowID(){ return showID; }
 
   /** Returns the shows Season
     * 
     * @return int
     */
   public int getSeason(){ return season; }
 
   /** Returns the Episode in this season
     * 
     * @return int
     */
   public int getEpisode(){ return episode; }
 
   /** Returns the shows Title
     * 
     * @return String
     */
   public String getShowTitle(){ return showTitle; }
 
   /** Returns the shows Episode Title
     * 
     * @return String
     */
   public String getEpisodeTitle(){ return episodeTitle; }
 
   /** Returns the shows Air -date -time and timezone
     * 
     * @return Calendar
     */
   public Calendar getDateTime(){ return dateTime; }
 }
