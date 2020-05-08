import be.ugent.twijug.jclops.*;
 import be.ugent.twijug.jclops.annotations.*;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.regex.Pattern;
 
 /**
  * A simple class demonstrating the use of the Options class in the plume library,
  * for processing of command line arguments.
  * @author Amy Csizmar Dalal
  * CS 204, Spring 2013
  * date: 9 April 2013
  */
 
 
 /*
 feeds, the file containing the list of feed URLs. This is the mandatory command line argument.
 --by-date to display the story links in order of date posted, from newest to oldest (not sorted by feed)
 --by-alpha to display the story links ordered by feed, with the feeds ordered alphabetically. (The default is to list the feeds in the order in which they're listed in the feed file.)
 --number NUM to display the latest NUM story links in each feed
 --since DATE to display the story links in each feed published on or after the specified date. DATE must be in the form yyyy-mm-dd (e.g. 2013-04-04 for April 4, 2013)
 --title REGEX to only display stories whose titles match the regular expression REGEX
 --description {on|off} to also display the description associated with a story. The default is off.
 [challenge problem] --newest to only display the stories posted or updated since the last time the feed parser was run.
 */
 @OneOf({"byDate", "byAlpha"})
 public class ArgParser {
 	// These instance variables are derived from the command line arguments passed in.
 	// Note that default values are assigned here as well.
 
 	private boolean byDate, byAlpha, newest, description;
 
 
 	/** The number of stories per feed to list */
 	private int number;
 
 	private Date since;
 
 	private Pattern title;
 
 	/** The file containing the list of feeds */
 	private String filename;
 
 	/**
 	 * The constructor sets default values for the instance variables, in case these are
 	 * not set by command line options.
 	 */
 	public ArgParser() {
 		byDate = false;
 		byAlpha = false;
 		newest = false;
 		description = false;
 		number = 2;
 		since = new Date();
 		title = null;
 		filename = "";
 	}
 
 	/**
 	 * Retrieves the current value of the "sort by author" flag.
 	 * @return true if we should sort the stories by author, false otherwise
 	 */
 	public boolean isByDate() {
 		return byDate;
 	}
 
 	/**
 	 * Turn on the "sort by author" flag.
 	 */
 	@Option(description="sort by author")
 	public void setByDate() {
 		this.byDate = true;
 		this.byAlpha = false;
 	}
 
 	/**
 	 * Retrieves the current value of the "sort by author" flag.
 	 * @return true if we should sort the stories by author, false otherwise
 	 */
 	public boolean isByAlpha() {
 		return byAlpha;
 	}
 
 	/**
 	 * Turn on the "sort by author" flag.
 	 */
 	@Option(description="sort by author")
 	public void setByAlpha() {
 		this.byAlpha = true;
 		this.byDate = false;
 	}
 
 	/**
 	 * Retrieves the current value of the "sort by author" flag.
 	 * @return true if we should sort the stories by author, false otherwise
 	 */
 	public boolean isNewest() {
 		return newest;
 	}
 
 	/**
 	 * Turn on the "sort by author" flag.
 	 */
 	@Option(description="sort by author")
 	public void setNewest() {
 		this.newest = true;
 	}
 
 	/**
 	 * Retrieves the current value of the "sort by author" flag.
 	 * @return true if we should sort the stories by author, false otherwise
 	 */
 	public boolean isDescription() {
 		return description;
 	}
 
 	/**
 	 * Turn on the "sort by author" flag.
 	 */
 	@Option(description="sort by author")
 	public void setDescription() {
 		this.description = true;
 	}
 
 	/**
 	 * Returns the number of stories per feed to display.
 	 * @return the number of stories to display
 	 */
 	public int getNumber() {
 		return number;
 	}
 
 	/**
 	 * Sets the number of titles to display
 	 * @param num the number of titles
 	 */
 	@Option(description="<N> the number of titles to display")
 	public void setNumber(int number) {
 		this.number = number;
 	}
 
 	/**
 	 * Returns the number of stories per feed to display.
 	 * @return the number of stories to display
 	 */
 	public Date getSince() {
 		return since;
 	}
 
 	/**
 	 * Sets the number of titles to display
 	 * @param num the number of titles
 	 */
 	@Option(description="<N> the number of titles to display")
 	public void setSince(String since) {
		this.since = DateFormat.parse(since);
 	}
 
 		/**
 	 * Returns the number of stories per feed to display.
 	 * @return the number of stories to display
 	 */
 	public Pattern getTitle() {
 		return title;
 	}
 
 	/**
 	 * Sets the number of titles to display
 	 * @param num the number of titles
 	 */
 	@Option(description="<N> the number of titles to display")
 	public void setTitle(String title) {
 		this.title = Pattern.compile(title);
 	}
 
 	/**
 	 * Sets the name of the input feed file
 	 * @param filename the input file name containing the feeds
 	 */
 	public void setFilename(String filename) {
 		this.filename = filename;
 	}
 	
 	public String getFilename() {
 		return filename;
 	}
 
 }
