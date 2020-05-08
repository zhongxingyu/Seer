 package GUI;
 
 import java.awt.*;
 
 public final class Constants {
 	/* Setting up the initial GUI window constants */
 	public static final Dimension SCREENSIZE = Toolkit.getDefaultToolkit().getScreenSize();
 	public static final Dimension GUI_DIMENSIONS = setGUIDimensions();
 	public static final String APP_TITLE = "MBTA Travel Application";
 	/* Layout for components */
 	/* Tab Titles and Tooltip Strings */
 	public static final String MAP_TITLE = "Geo Map";
 	public static final String MAP_TOOLTIP = "Switch to MAP view to see a GEO-MAP and enter destinations to plan your trip.";
 	public static final String ITINERARY_TITLE = "Itinerary";
 	public static final String ITINERARY_TOOLTIP = "Switch to ITINERARY view to see a LINEAR-MAP and written directions to get to your destination.";
 	/* Map Tab Components */
 	public static final Color GRAY_TEXT_COLOR = new Color(128,128,128);
 	public static final String INPUT_FIELD_FILLER = "Please enter a location.";
 	public static final String SUBMIT_DESTINATION_TEXT = "Add";
 	public static final String DROPDOWN_LABEL_TEXT = "Or choose from a list:";
 	public static final String[] BLUE_STATIONS = {"Wonderland", "Revere Beach", "Beachmont", "Suffolk Downs", "Orient Heights", "Wood Island", "Airport", "Maverick", "Boston Harbor", "Aquarium", "State", "Government Center", "Bowdoin"};
 	public static final String[] RED_STATIONS = {"Alewife", "Davis", "Porter", "Harvard", "Central", "Kendall/MIT", "Charles/MGH", "Park Street", "Downtown Crossing", "South Station", "Broadway", "Andrew", "JFK/UMass", "Savin Hill", "Fields Corner", "Shawmut", "Ashmont", "Cedar Grove", "Butler", "Milton", "Central Avenue", "Valley Road", "Capen Street", "Mattapan", "North Quincy", "Wollaston", "Quincy Center", "Quincy Adams", "Braintree"};
 	public static final String[] ORANGE_STATIONS = {"Oak Grove", "Malden Center", "Wellington", "Sullivan Square", "Community College", "North Station", "Haymarket", "State", "Downtown Crossing", "Chinatown", "Tufts Medical Center", "Back Bay", "Mass Ave", "Ruggles", "Roxbury Crossing", "Jackson Square", "Stony Brook", "Green Street", "Forest Hills"};
 	public static final Color MYRED = new Color(255,0,0);
 	public static final Color MYORANGE = new Color(230,140,0);
 	public static final String SORT_DEST_CHECKBOX_TEXT = "Sorted List";
	public static final String PLAN_TRIP_BUTTON_TEXT = "Plan Your Trip!";
 	
 	
 	/* Help set new dimensions based on given dimensions and ratios */
 	public static final Dimension setRelativeToXwithRatios(Dimension original, double widthRatio, double heightRatio) {
 		Dimension temp = new Dimension((int) (original.width*widthRatio), (int) (original.height*heightRatio));
 		return temp;
 	}
 	
 	/* Help set correct GUI window dimensions based on total screen size */
 	public static final Dimension setGUIDimensions() {
 		Dimension temp = SCREENSIZE;
 		temp.width = (int) ((temp.width >= 1024) ? temp.width*.65 : 800);
 		temp.height = (int) ((temp.height >= 800) ? temp.height*.75 : 600);
 		
 		return temp;
 	}
 	
 }
