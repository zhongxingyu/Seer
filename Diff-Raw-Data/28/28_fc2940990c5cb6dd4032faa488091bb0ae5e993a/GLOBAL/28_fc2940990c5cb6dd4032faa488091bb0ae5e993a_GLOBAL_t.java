 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 
 import processing.core.PApplet;
 import processing.core.PFont;
 
 // class for global variables and constants
 public class GLOBAL {
 	
 	// Processing obj, use this for getting all the processing functions
 	public static PApplet processing;
 	
 	// Gradient functions
 	public static GradientUtils gu;
 	
 	// Text font
 	public static PFont tFont;
 	
 	// var for storing the current layer 
 	public static int LAYER = 1;
 
 	////
 	//// COLORS (defined in setupColors function)  
 	////
 	
 	public static int colorToggleOff;
 	public static int colorText;
 	public static int colorBackgroundLayerOne;
 	public static int colorLinesLayerOne;
 	public static int colorButtonLabel;
 	
 	////
 	//// END COLORS 
 	////
 	
 	
 	/// Layer 1
 	// Var for storing the selected season, for draw all the episods related
 	public static int SEASON_SELECTED;
 	// All buttons of the first layer
 	public static ArrayList<Button> allSeasonsButtons = new ArrayList<Button>();
 	// All buttons of the first layer
 	public static ArrayList<Button> allEpisodesButtons = new ArrayList<Button>();
	public static ArrayList<Episode> selectedEpisodesList = new ArrayList<Episode>();
 		
 	public GLOBAL(PApplet p) {
 		processing = p;
 	}
 	
 }
