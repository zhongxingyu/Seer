 import org.newdawn.slick.*;
 
 public class Paper {
 	Word[][][] text;
 	int currentPage = 0;
 
 	public static final float SPACE = 10.0f;
 	public static final float LINE = 40.0f;
 	public static final float ACTIVATE_DISTANCE = 100.0f;
 
 	public static final String[][] showing = {
 
 		"Hold down the left mouse button and explore the screen with your cursor.".split(" "),
 		"meaningful play compelling unique suspense questions immersive".split(" "),
 		"interactive storytelling agency, immersion, transformation. Agency, empowerment control".split(" "),
 		"platforms, reactive hack-and-slash fighting combat system movement".split(" "),
 		"quick visceral, weight, swing swing affordance motion, opposite decision".split(" "),
 		"Beyond skilled presence strike, injured positions distance.".split(" "),
 		"presence. cornerstone aesthetic revealed experiment sporadic agency sky destroyed hammer hand-in-hand".split(" "),
 		"Immersion, present engaged matters. engaging sound drawing combat physicality hammer dagger pistol cannon. ebb flow balanced".split(" "),
 		"notable, setting. world, sounds, falls pointing together reaction foreign understandable mechanics fantasy,".split(" "),
 		"element scenery, feedback depth music music song lyrics style themes, music style. sounds thud wood elastic levity.".split(" "),
 		"final namelessness backstory. exposition discovering lore history optional conversations backstory compelling exploration foreshadowing".split(" "),
 		"value playthrough transformation. variations journey journey, journey adventure discovery".split(" "),
 		"personal decisions. betrays rescue restored creation catastrophic fly onward brighter forgiveness hope".split(" "),
 		"impressive variety. fresh evolves new change different".split(" "),
 		"Distillery, Lost and Found Memorial Shrine Bastion diegetic replayability.".split(" "),
 		"special reactive narration. faceless stranger character friend, narrates conversation narrates say".split(" "),
 		"quality try new strange comment. combinations pairings upgrades voice".split(" "),
 		"triumph meaningful play emotional immersive satisfying cathartic beautiful extensive wonderful complete".split(" "),
 	};
 
 
 	public Paper(String src, GameContainer container) {
 
 		// read in text source file that corresponds to the PNGs
 		In in = new In(src);
 		String source = in.readAll();
 
 		// line splitter (identical to code in WordConverter)
 		// line splitter
 		int maxCols = 64;
 		String[] paragraphs = source.split("<p>");
 		for (int i = 0; i < paragraphs.length; i++) {
 			boolean jumpMade = false;
 			int cols = 0;
 			for (int j = 0; j < paragraphs[i].length(); j++) {
 				if (!jumpMade) {
 					j += maxCols;
 					jumpMade = true;
 				}
 				if (j >= paragraphs[i].length())
 					j = paragraphs[i].length()-1;
 				if (jumpMade && paragraphs[i].charAt(j) == ' ') {
 					String beg = paragraphs[i].substring(0, j);
 					String end = paragraphs[i].substring(j+1);
 					paragraphs[i] = beg + "<br>" + end;
 					jumpMade = false;
 				}
 			}
 		}
 
 		// create a Word object for each word in the paper,
 		// store in 3-dimensional array
 		text = new Word[paragraphs.length][][];
 		for (int i = 0; i < paragraphs.length; i++) {
 			String[] lines = paragraphs[i].split("<br>");
 			text[i] = new Word[lines.length][];
 			for (int j = 0; j < lines.length; j++) {
 				String[] words = lines[j].split("\\s+");
 				text[i][j] = new Word[words.length];
 				for (int k = 0; k < words.length; k++) {
 					String filename = "p" + i + "l" + j + "w" + k+ ".png";
 					text[i][j][k] = new Word(words[k], filename);
 				}
 			}
 		}
 		// calculate positions of all words while calculating
 		// the width and height of the paragraph's bounds to
 		// center the paragraph by later
 		for (int i = 0; i < text.length; i++) {
 			float height = 0.0f;
 			float width = 0.0f;
 			float x = 0.0f;
 			float y = 0.0f;
 
 			Word[][] paragraph = text[i];
 			for (int j = 0; j < paragraph.length; j++) {
 
 				Word[] line = paragraph[j];
 				for (int k = 0; k < line.length; k++) {
 					Word word = line[k];
 					word.setPos(x, y);
 					x += word.getWidth() + SPACE;
 					if (x > width) width = x;
 				}
 				x = 0.0f;
 				height = y;
 				y += LINE;
 			}
 			// shift paragraph to center
 			float shiftX = (container.getWidth()-width)/2.0f;
 			float shiftY = (container.getHeight()-height)/2.0f;
 
 			for (int j = 0; j < paragraph.length; j++) {
 				Word[] line = paragraph[j];
 				for (int k = 0; k < line.length; k++) {
 					Word word = line[k];
 					word.shift(shiftX, shiftY);
 
 				}
 			}
 		}
 
 	}
 
 	public int getCurrentPage() {
 		return currentPage;
 	}
 
 	public void activate(String s) {
 		Word[][] paragraph = text[currentPage];
 		for (Word[] line : paragraph) {
 			for (Word word : line) {
 				if (s.equals(word.getText()) && !word.isActive()) {
 					word.activate(false);
 					return;
 				}
 			}
 		}
 	}
 
 	public void activate(String[] strings) {
 		for (String s : strings)
 			activate(s);
 	}
 
 	public void advancePage() {
 		currentPage++;
 		activate(showing[currentPage]);
 		if (currentPage > text.length-1)
 			currentPage = text.length-1;
 	}
 
 	public void backPage() {
 		currentPage--;
 		if (currentPage < 0)
 			currentPage = 0;
 	}
 
 	public int numPages() {
 		return text.length;
 	}
 
 	private boolean turningPage = false;
 
 	public void turnPage() {
		if (currentPage == text.length-1 || turningPageBack)
 			return;
 		turningPage = true;
 	}
 
 	private boolean turningPageBack = false;
 
 	public void turnPageBack() {
		if (currentPage == 0 || turningPage)
 			return;
 		turningPageBack = true;
 	}
 
 	private float pageTurnStep = 0;
 	private boolean haveAdvancedPage = false;
 
 	public void update(GameContainer container, int delta) {
 		Input input = container.getInput();
 		// drop in words around cursor
 		for (Word[] line : text[currentPage]) {
 			for (Word word : line) {
 				float mx = (float)input.getMouseX();
 				float my = (float)input.getMouseY();
 				float cx = word.getCenterX();
 				float cy = word.getCenterY();
 				float dist = (float)Math.sqrt((mx-cx)*(mx-cx)+(my-cy)*(my-cy));
 				if (dist <= ACTIVATE_DISTANCE && container.getInput().isMouseButtonDown(Input.MOUSE_LEFT_BUTTON))
 					word.activate(true);
 				word.update(container, delta);
 			}
 		}
 
 		// turn page forward
 		if (turningPage && !turningPageBack) {
 			pageTurnStep += delta; 
 			if (pageTurnStep < 1000.0f) {
 				this.setShift(-(pageTurnStep/1000.0f)*container.getWidth(), 0.0f);
 			}
 			else if (!haveAdvancedPage) {
 				advancePage();
 				haveAdvancedPage = true;
 				setShift((float)container.getWidth(), 0.0f);
 			}
 			if (pageTurnStep >= 1000.0f && pageTurnStep < 2000.0f) {
 				float p = pageTurnStep - 1000.0f;
 				this.setShift(((1000.0f-p)/1000.0f)*container.getWidth(), 0.0f);
 			}
 			else if (pageTurnStep >= 2000.0f) {
 				this.setShift(0.0f, 0.0f);
 				pageTurnStep = 0;
 				turningPage = false;
 				haveAdvancedPage = false;
 			}
 
 		}
 
 		// turn page backward
 		if (!turningPage && turningPageBack) {
 			pageTurnStep += delta; 
 			if (pageTurnStep < 1000.0f) {
 				this.setShift((pageTurnStep/1000.0f)*container.getWidth(), 0.0f);
 			}
 			else if (!haveAdvancedPage) {
 				backPage();
 				haveAdvancedPage = true;
 				setShift(-(float)container.getWidth(), 0.0f);
 			}
 			if (pageTurnStep >= 1000.0f && pageTurnStep < 2000.0f) {
 				float p = pageTurnStep - 1000.0f;
 				this.setShift(-((1000.0f-p)/1000.0f)*container.getWidth(), 0.0f);
 			}
 			else if (pageTurnStep >= 2000.0f) {
 				this.setShift(0.0f, 0.0f);
 				pageTurnStep = 0;
 				turningPageBack = false;
 				haveAdvancedPage = false;
 			}
 
 		}
 
 	}
 
 	public void shift(float x, float y) {
 		for (Word[] line : text[currentPage]) {
 			for (Word word : line) {
 				word.translate(x, y);
 			}
 		}
 	}
 
 	public void setShift(float x, float y) {
 		for (Word[] line : text[currentPage]) {
 			for (Word word : line) {
 				word.setTranslation(x, y);
 			}
 		}		
 	}
 
 	public void render(GameContainer container, Graphics g) {
 		Word[][] paragraph = text[currentPage];
 		for (Word[] line : paragraph) {
 			for (Word word : line) {
 				word.render(container, g);
 			}
 		}
 	}
 
 }
