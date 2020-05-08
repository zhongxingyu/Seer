 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import processing.core.*;
 
 public class main_class extends PApplet{
 	
 	public GLOBAL g = new GLOBAL(this);
 	public Parser pars = new Parser();
 	
 	// test
 	public ScrollBar scroll;
 
 	public void setup() {
 		
 		setupColors();
 		
 		size(1024,768);
 		frameRate(15);
 		
 		GLOBAL.gu = new GradientUtils();
 		GLOBAL.tFont = loadFont("LucidaSans-48.vlw");
		GLOBAL.selectedEpisodesList = Parser.LIST_ALL; // Initialize the displayed list to "all episodes"
 
 		//Parse all available transcripts.
 		pars.parseAllTranscripts();
 		pars.filterCharacters();        //Remove characters in less than 1 episode
 
 		//Sort ALL_CHARACTERS where characters in more episodes are listed first.
 		Collections.sort(Parser.ALL_CHARACTERS, new Comparator(){
 			public int compare(Object ob1, Object ob2) {
 				Character c1 = (Character) ob1;
 				Character c2 = (Character) ob2;
 				return c2.getTotalEpisodes() - c1.getTotalEpisodes();
 			}
 
 		});
 
 
 		//Prints all characters currently added. 
 		//For testing only.
 
 		for(int x=0; x<Parser.ALL_CHARACTERS.size(); ++x) {
 			String name = Parser.ALL_CHARACTERS.get(x).getName();
 			int ep = Parser.ALL_CHARACTERS.get(x).getTotalEpisodes();
 
 			System.out.println(name + ":\t" + ep + " episodes.");
 		}       
 		
 		setupSeasonsButtons();
 		setupEpisodeButtons();
 								
 		smooth();
 		
 	}
 	
     @SuppressWarnings("unchecked")
 	public void draw() {
 
 		// layer 1
 		if(GLOBAL.LAYER == 1){
 			
 			drawLayerOneBackground();
 			drawLayerOneText();
 			
 			drawSeasonsButtons();
 			drawEpisodeButtons();
 			
 		}
 		// layer 2
 		else if(GLOBAL.LAYER == 2){
 			
 			
 		}
 		// layer 3
 		else if(GLOBAL.LAYER == 3){
 
 
 		}
 
 	}
 
 	///
 	/// Layer One functions
 	///
 	public void drawLayerOneText() {
 		fill(GLOBAL.colorText);
 		textFont(GLOBAL.tFont,24);
 		text("In the year 2525", width/2 - 100, 40);
 		text("Choose a character :",50,100);
 		text("Choose among series and episodes :",50 + width/2,100);
 	}
 
 	// Draw the background of the first layer, i.e. the layer where you choose a character, a season or an episode (or all episodes)
 	public void drawLayerOneBackground() {
 
 		background(GLOBAL.colorBackgroundLayerOne);
 		
 		stroke(this.color(GLOBAL.colorLinesLayerOne));
 		strokeWeight(2);
 		line(width/2, 60, width/2, height - 30);
 		
 	}
 	
 	public void setupSeasonsButtons(){
 		// Draw all the series button
 		for(int i =1; i <= 6; i++) {
 			Button b = new Button();
 			b.setLabel("Season "+i);
 			b.x = 700;
 			b.y = 200 + i*40;
 			GLOBAL.allSeasonsButtons.add(b);
 			//b.backColor = GLOBAL.colorToggleOff;
 		}
 	}
 	
 	
     // draw the seasons buttons
 	public void drawSeasonsButtons() {
 		// Draw all the series button
 		for(int i = 0; i < GLOBAL.allSeasonsButtons.size(); i++) {
 			GLOBAL.allSeasonsButtons.get(i).draw();	
 		}
 	}
 	
 	public void setupEpisodeButtons() {
 		// Create a new scrollbar
 		scroll = new ScrollBar();
 		scroll.x = 900;
 		scroll.y = 500;	
 		scroll.width = 15;
 		scroll.height = 200;	
 		// Draw all the series button
		for(int i =0; i < GLOBAL.selectedEpisodesList.size(); i++) {
 			Button b = new Button();
			b.setLabel("S"+GLOBAL.selectedEpisodesList.get(i).getSeason()+"E"+GLOBAL.selectedEpisodesList.get(i).getEpisode()+": "+ GLOBAL.selectedEpisodesList.get(i).getName());
 			//b.backColor = GLOBAL.colorToggleOff;
 			GLOBAL.allEpisodesButtons.add(b);
 			
 		}
 		
 		scroll.size = (float)1 / GLOBAL.allEpisodesButtons.size();
 
 	}
 	
 	public void drawEpisodeButtons(){
 		// Draw the scrollBar
 		scroll.draw();
 		float i = 0;
 		// elementNumber: var for offset calculation
 		int elementNumber = 0;	
 		i = map(scroll.val,0, 1,0,GLOBAL.allEpisodesButtons.size()-5);
 		// Draw all the series button
 		for(int j = (int)i; j <= i + 5; j++) {
 			GLOBAL.allEpisodesButtons.get(j).x = 300;
 			GLOBAL.allEpisodesButtons.get(j).y = 500 + elementNumber*40;
 			GLOBAL.allEpisodesButtons.get(j).draw();
 			elementNumber++;
 		}
 	}
 	
 	///
 	/// End Layer One functions
 	///
 	
 	// Color setup function, all colors should be set here, so every change will be global
 	public void setupColors() {
 		GLOBAL.colorToggleOff = color(152);
 		GLOBAL.colorText = color(224);
 		GLOBAL.colorBackgroundLayerOne = color(36);
 		GLOBAL.colorLinesLayerOne = color(128);		
 		GLOBAL.colorButtonLabel = color(224);		
 	}
 	
 	// If mouse is pressed, check what has been pressed and activate the action
 	public void mousePressed() { 
 		  println("x = " + mouseX+ "y =" + mouseY);
 		  if (GLOBAL.LAYER == 1) {
 			  for(int i = 0; i < GLOBAL.allSeasonsButtons.size(); i++) {
 				  if( GLOBAL.allSeasonsButtons.get(i).mouseOver()) {
 					  GLOBAL.allSeasonsButtons.get(i).doAction();
 				  }
 			  }
 			  for(int i = 0; i < GLOBAL.allEpisodesButtons.size(); i++) {
 				  if( GLOBAL.allEpisodesButtons.get(i).mouseOver()) {
 					  GLOBAL.allEpisodesButtons.get(i).doAction();
 				  }
 			  }
 			  if(scroll.mouseOver())
 				  scroll.mousePressed();
 		  }
 	}
 	
 	public void mouseReleased() {
 		// Scroll must be released 
 		scroll.mouseReleased();
 	}
 
 	public static void main(String args[]) {
 		PApplet.main(new String[] { "--present", "main_class" });
 	}
 
 }
