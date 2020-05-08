 import java.util.ArrayList;
 
 import processing.core.*;
 import wordcram.*;
 import wordcram.text.StopWords;
 
 public class TagCloud extends Widget{
 	
 	public ArrayList<wordcram.Word> wordList;
 	public wordcram.Word[] w;
 	
 	public WordCram wc;
 	PGraphics buffer;
 	
 	public int plotWidth;
 	public int infoWidth = 100;
 	
 	public Character character;
 	
 	public TagCloud(Character c, int xValue, int yValue, int WEIGHT, int HEIGHT) {
 	
 		x = xValue;
 		y = yValue;
 		
 		width = WEIGHT;
 		height = HEIGHT;
 				
 		plotWidth = width - infoWidth; // Need space for icons and stat
 		
 		character = c;
 		
 		wordList = new ArrayList<wordcram.Word>();
 		
 		setTagCloud();
 
 	}
 	
 	public void setTagCloud() {
 		int i = Parser.findCharacter(character.getName()); //Find index of the character based on String name.
 		//The above is a static method.
 
 		//You need to create a new ArrayList<Word> 
 		// The following method will populate it with all the words said from season s1 episode e1, through (and including) season s2, episode e2
 //		    To clarify,if you want the words from season 1 episode 1 only, [s1=1, e1 =1, s2=1, s2=1]
 		//   if you want the words from season 2 episode 3 through season 4 episode 6 : [s1=2 e1=3 s2=4 e2=6]
 		ArrayList<Word> myList = Parser.ALL_CHARACTERS.get(i).getWordRange(GLOBAL.episodeStart.getSeason(), GLOBAL.episodeStart.getEpisode(), GLOBAL.episodeEnd.getSeason(), GLOBAL.episodeEnd.getEpisode());
 
 		//Now you have myList (ordered by weight! biggest first! yay!)... so you need to decide how many words you want to display on the map. make a variable for this...
 		int max = 50;  //if we want to display 50 words.
 		//Why? because the list could contain hundreds of words! I don't know how big the treeMap can be, but
 		//I figure you may need a limit.
 		
 		w = new wordcram.Word[max];
 
 		System.out.println(myList.size());
 
 		//Parse your new ArrayList
 		for(int x=0; x<max; ++x) {
			//Make sure to check that max is still under length of array
			if(max >= myList.size()) { break; }
 			
 			String aWord = myList.get(x).getWord();  
 			int itsWeight = myList.get(x).getWeight();
 			
 			wordList.add( new wordcram.Word(aWord,itsWeight) );
 			System.out.println("Added Word " + aWord + " - Weight " + itsWeight);
 			
 			w[x] = new wordcram.Word(aWord,itsWeight);
 			
 		}
				
 //		for (int i =0; i< Parser.ALL_CHARACTERS.size(); i++) {
 //			if (Parser.ALL_CHARACTERS.get(i).getTotalEpisodes() > 7) {
 //			alphabet.add( new wordcram.Word(Parser.ALL_CHARACTERS.get(i).getName(), (int) Parser.ALL_CHARACTERS.get(i).getTotalEpisodes()));
 //			//System.out.println("found");
 //			}
 //		}
 
 		buffer = GLOBAL.processing.createGraphics(plotWidth, height, GLOBAL.processing.JAVA2D);
 		buffer.beginDraw();
 		buffer.background(GLOBAL.colorTagCloudBackground);
 		
 		//w = new wordcram.Word[wordList.size()];
 		
 		  wc = new WordCram(GLOBAL.processing)
 		  //.fromTextFile("data/names.txt")
 //		  .fromWords(wordList.toArray(w))
 		  .fromWords(w)
 				//.withColors(color(255,0,0), color(0), color(0,0,255)) // red, black, and blue
 		  .withCustomCanvas(buffer)
 		 .withColor(GLOBAL.colorText)
 				    .sizedByWeight(0,40).withWordPadding(2).withAngler(Anglers.horiz())
 				    .withPlacer(Placers.centerClump())
 				    .maxNumberOfWordsToDraw(max)
 				    .withStopWords(StopWords.ENGLISH)
 				    .maxAttemptsToPlaceWord(10000);
 	}
 	
 	public void draw() {
 		
 		GLOBAL.processing.noStroke();
 		GLOBAL.processing.rectMode(GLOBAL.processing.CORNERS);
 		GLOBAL.processing.fill(GLOBAL.colorPlotArea);
 		GLOBAL.processing.rect( x,y, x + plotWidth, y + height);
 		GLOBAL.processing.fill(GLOBAL.processing.color(255));
 
 		if (wc.hasMore()) {  
 
 			// draw the progress bar
 			//float progress = wc.getProgress();
 			//drawProgressBar(progress);
 			//drawProgressText(progress);
 
 			wc.drawNext();
 			
 		}
 		else {
 
 			buffer.endDraw();
 			GLOBAL.processing.image(buffer, x, y);
 
 			//System.out.println(wc.getSkippedWords().length);
 
 		}
 
 		// Draw icon and info		
 		GLOBAL.processing.fill(GLOBAL.colorText);
 		GLOBAL.processing.textFont(GLOBAL.tFont,16);
 		GLOBAL.processing.textAlign(GLOBAL.processing.CENTER);
 		GLOBAL.processing.text(character.getName(), x + width - 40, y + 20);
 
 		if (character.getIcon() != null)
 			GLOBAL.processing.image( character.getIcon(), x + width - 80, y + 30, 80, 80);
 		
 	}
 	
 	
 	public void changePosition(int xValue, int yValue, int w, int l) {
 		
 		x = xValue;
 		y = yValue;
 		
 		width = w;
 		height = l;
 		
 		plotWidth = width - infoWidth;
 		
 		buffer.width = plotWidth;
 		buffer.height = height;
 		
 		setTagCloud();
 		
 	}
 
 //	void drawProgressBar(float progress) {
 //		int gray = GLOBAL.processing.color(progress * 255);
 //
 //		// Draw the empty box:
 //		GLOBAL.processing.noFill();
 //		GLOBAL.processing.stroke(255);
 //		GLOBAL.processing.strokeWeight(2);
 //		GLOBAL.processing.rectMode(GLOBAL.processing.CORNER);
 //		GLOBAL.processing.rect(100, (300/2)-30, (600-200), 60);
 //		  
 //		  // Fill in the portion that's done:
 //		  GLOBAL.processing.fill(gray);
 //		  GLOBAL.processing.rect(100, (300/2)-30, (600) * progress, 60);
 //		}
 //
 //		void drawProgressText(float progress) {
 //			GLOBAL.processing.text(GLOBAL.processing.round(progress * 100) + "%", 600/2, (300/2)+50);
 //		}
 	
 }
 
 //public class TagCloud extends Widget{
 //
 //	PFont font;
 //	String fontFile = "LucidaSans-48.vlw";
 //	float baseline_ratio = (float)0.4;
 //
 //	String[] tags  = {  "The Office", "The Colbert Report", "Battlestar Galactica", "weeds", "californication", "scrubs", "CSI", "Smallville", "House", "Family Guy", "Daily Show", "US", "Boondocks", "Venture Brothers", "Flight of the Conchords", "Firefly", "Angel", "Grey's Anatomy", "Venture Bros", "King of The Hill", "America's Next Top Model", "Project Runway", "My Name is Earl", "Dirty Jobs", "Eureka", "Friends", "Frasier", "Sex and the City", "Food Network", "South Park", "Seinfeld", "The Daily Show", "Colbert Report", "The Riches", "LOST", "Aqua Teen Hunger Force", "Arrested Development", "Freaks and Geeks", "MythBusters", "Dexter", "Six Feet Under", "Home Movies", "Pete & Pete", "Extras", "tears", "Law and Order", "Pete and Pete", "Invader Zim", "Jericho", "The Boondocks", "The Simpsons", "Futurama", "jeopardy", "King Of Queens", "American Dad", "Dr. Who", "That 70's Show", "Avatar", "Fraggle Rock", "History Channel", "24", "Kids in the Hall", "Strangers With Candy"};
 //	int[] tagtally = {  16, 4, 3, 8, 2, 15, 5, 3, 8, 16, 3, 2, 2, 2, 5, 5, 2, 9, 2, 2, 4, 4, 3, 2, 2, 5, 2, 4, 2, 2, 3, 6, 7, 2, 9, 4, 7, 2, 2, 6, 3, 5, 2, 2, 2, 2, 3, 3, 2, 3, 5, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 };
 //	int most = GLOBAL.processing.max(tagtally);
 //	int least = GLOBAL.processing.min(tagtally);
 //	int large_font = 28;
 //	int small_font = 8;
 //
 //	public Box[] boxes =  new Box[tags.length];
 //
 //	public TagCloud(){
 //	  font = GLOBAL.processing.loadFont(fontFile);
 //	  
 //	  for (int i = 0; i < tags.length; i++) {
 //	    int h_test = (int)GLOBAL.processing.map(tagtally[i], least, most, small_font, large_font);
 //	    GLOBAL.processing.textFont(font, h_test);
 //	    int w_test = (int)(GLOBAL.processing.textWidth(tags[i]));
 //
 //	    boxes[i] = new Box(tags[i],w_test,h_test);
 //	  }
 //	}
 //
 //	public void draw(){
 //		
 //	  GLOBAL.processing.rectMode(GLOBAL.processing.CENTER);
 //
 //	  GLOBAL.processing.pushMatrix();
 //	  GLOBAL.processing.scale((float)0.82,(float)0.82);
 //	  GLOBAL.processing.translate((float)0.12*width,(float)0.15*height);
 //	  for(int i=0; i<boxes.length; i++){
 //	    boxes[i].collide(i);
 //	    GLOBAL.processing.fill(0);
 //	    boxes[i].render(i);
 //	  }
 //	  GLOBAL.processing.popMatrix();
 //	  
 //	  //check for stopping
 //	  int completed = 0;
 //	  for(int i=0; i<boxes.length; i++){
 //	    if(boxes[i].frozen)
 //		completed++;
 //	  }
 //	  System.out.println("completed:" + completed);
 //	  if(completed == boxes.length){
 //		  //GLOBAL.processing.noLoop();
 //	  }
 //	}
 //
 //	void mouseReleased(){
 //	  boxes[0].x = GLOBAL.processing.random(20,380);
 //	  boxes[0].y = GLOBAL.processing.random(20,180);
 //	}
 //	
 //	
 //	public class Box extends Widget{
 //		public float x, y, w, h, volume, attempts;
 //		String word;
 //		boolean frozen = true;
 //		int c;
 //
 //		public float angle = 0;
 //		public float outwards = 1;
 //
 //		Box(String word, int w, int h){
 //			this.word = word;
 //			this.x = GLOBAL.processing.cos(angle) * GLOBAL.processing.log(outwards) * 42 + 500;
 //			this.y = GLOBAL.processing.sin(angle) * GLOBAL.processing.log(outwards) * 21 + 500;
 //			angle -= 61;
 //			outwards += 0.4;
 //			this.w = w;
 //			this.h = h;
 //			this.volume = w*h;
 //			this.c = GLOBAL.processing.color(GLOBAL.processing.random(100,255), GLOBAL.processing.random(100,255), GLOBAL.processing.random(100,255));
 //		}
 //
 //		void render(int id){
 //			/*    noFill();
 //		    stroke(0,20);
 //		    rect(x,y,w,h); */
 //			GLOBAL.processing.fill(c);
 //			GLOBAL.processing.textFont(font, h);
 //			GLOBAL.processing.text(tags[id], (int)(x - w/2), (int)(y + h * baseline_ratio));
 //		}
 //
 //		void collide(int i){
 //			frozen = true;
 //			for(i+=1; i<boxes.length; i++){
 //				float dx = boxes[i].x - this.x;
 //				float dy = boxes[i].y - this.y;
 //				float tx = boxes[i].w/2 + this.w/2;
 //				float ty = boxes[i].h/2 + this.h/2;
 //				if( (GLOBAL.processing.abs(dx) < tx) && (GLOBAL.processing.abs(dy) < ty) ){
 //					float me = (float)0.5 * boxes[i].volume / this.volume;
 //					float you = (float)0.5 * this.volume / boxes[i].volume;
 //					attempts++;
 //
 //					System.out.println("for collision "+i+", I am "+me+", & you are "+you);
 //					frozen = false;	
 //					if(dx > 0){
 //						this.x     -= me  * 1.5;
 //						boxes[i].x += you * 1.5;
 //					}else{
 //						this.x     += me  * 1.5;
 //						boxes[i].x -= you * 1.5;
 //					}
 //
 //					if(dy > 0){
 //						this.y     -= me;
 //						boxes[i].y += you;
 //					}else{
 //						this.y     += me;
 //						boxes[i].y -= you;
 //					}
 //
 //					if(this.attempts > 500){
 //						this.x = GLOBAL.processing.random(0,width);
 //						this.y = GLOBAL.processing.random(0,height);
 //						this.attempts = 0;
 //					}
 //				}
 //			}
 //		}
 //	}
 //	
 //}
