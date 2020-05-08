 import java.io.File;
 import java.util.Set;
 import java.util.TreeMap;
 
 import processing.core.PApplet;
 import processing.core.PFont;
 
 public class Francify extends PApplet {
 
 	private static final long serialVersionUID = 1L;
 	public static final int PART_ONE = 0, PART_TWO = 1;
 	public static final boolean DRAW_DISTANCE = true;
 	public static final boolean DRAW_SPEED= false;
 	
 	public static void main(String[] args) {
 		PApplet.main(new String[] { "--present", "Francify" });
 	}
 
 	//Skinning Color Variables
 	int darkColor = 0xFF002E3E;
 	int dataColor0 = 0xFF002E3E;
 	int dataColor1 = 0xFF88c23c;
 	
 	Slider s;
 	boolean unpressed;
 	boolean movingSlider, leftHandle, rightHandle;
 	static int fontSize = 10;
 	PFont myFont;
 	PFont largerFont;
 	int rangeMin, rangeMax, minSYear, maxSYear;
 	int graphX, graphY, graphW, graphH;
 	int currentDisplayed;
 	String sliderLabel, title;
 	
 	float minSpeed, maxSpeed, minDistance, maxDistance;
 	
 	TreeMap<Integer, RaceRow> data;
 	TreeMap<String, Integer> numMedals;
 	
 	public void setup() {
 		size(1000, 600);
 		graphX = 100;
 		graphY = 50;
 		graphW = getWidth() - 200;
 		graphH = getHeight() - 200;
 		
 		frameRate(30);
 		currentDisplayed = PART_ONE;
 		
 		unpressed = true;
 		movingSlider = false;
 		leftHandle = false;
 		rightHandle = false;
 
 		myFont = createFont("BrowalliaNew", fontSize);
 		largerFont = createFont("BrowalliaNew", 24);
 		
 		// Handle data import
 		data = new TreeMap<Integer, RaceRow>();
 		numMedals = new TreeMap<String, Integer>();
 		
 		minSpeed = minDistance = Float.MAX_VALUE;
 		maxSpeed = maxDistance = 0.0f;
 		minSYear = Integer.MAX_VALUE;
 		maxSYear = Integer.MIN_VALUE;
 
 		String[] lines = loadStrings("data"+File.separator+"Tour_De_France_Data.csv");
 		for(int i = 1; i < lines.length; i++){
 			String[] parts = lines[i].split(",");
 			RaceRow rr = new RaceRow();
 			rr.year = Integer.parseInt(parts[0]);
 			if(rr.year > maxSYear)
 				maxSYear = rr.year;
 			if(rr.year < minSYear)
 				minSYear = rr.year;
 			
 			rr.firstPlaceRider = parts[1];
 			rr.firstCountryID = Integer.parseInt(parts[2]);
 			rr.firstPlaceCountry = parts[3];
 			rr.firstPlaceTeam = parts[4];
 			
 			rr.secondPlaceRider = parts[5];
 			rr.c2nd = Float.parseFloat(parts[6]);
 			rr.secondCountryID = Integer.parseInt(parts[7]);
 			rr.secondPlaceCountry = parts[8];
 			rr.secondPlaceTeam = parts[9];
 
 			rr.thirdPlaceRider = parts[10];
 			rr.c3rd = Float.parseFloat(parts[11]);
 			rr.thirdCountryID = Integer.parseInt(parts[12]);
 			rr.thirdPlaceCountry = parts[13];
 			rr.thirdPlaceTeam = parts[14];
 			
 			rr.numStages = Integer.parseInt(parts[15]);
 			if (parts.length > 16 && !parts[16].equals("")){
 				rr.distance = Float.parseFloat(parts[16]);
 				if(rr.distance > maxDistance)
 					maxDistance = rr.distance;
 				if(rr.distance < minDistance)
 					minDistance = rr.distance;
 			}
 			if (parts.length > 17 && !parts[17].equals("")){
 				rr.avgSpeed = Float.parseFloat(parts[17]);
 				if(rr.avgSpeed > maxSpeed)
 					maxSpeed = rr.avgSpeed;
 				if(rr.avgSpeed < minSpeed)
 					minSpeed = rr.avgSpeed;
 			}
 			if (parts.length > 18)
 				rr.bestTeam = parts[18];
 			
 			Integer medals = numMedals.get(rr.firstPlaceCountry);
 			if(medals == null){
 				numMedals.put(rr.firstPlaceCountry, 1);
 			} else {
 				numMedals.put(rr.firstPlaceCountry, medals + 1);
 			}
 			
 			data.put(rr.year, rr);
 		}
 		
 		s = new Slider(graphX, graphY + graphH + 50, graphW, 50);
 		int[] vals = new int[maxSYear - minSYear];
 		for(int i = minSYear; i < maxSYear; i++){
 			vals[i-minSYear] = i;
 		}
 		s.setValues(vals);
 		s.setDrawInterval(10);
 		sliderLabel = "Years";
 		title = "Tour de France, 1903 - 2009";
 	}
 
 	public void draw() {
 		// Handle data drawing
 
 		background(0xcccccc);
 		drawAxes();
 		s.drawSlider();
 		drawRange();
 		handleInput();
 		drawData(DRAW_DISTANCE, s.getLeftBound(), s.getRightBound());
 		drawData(DRAW_SPEED, s.getLeftBound(), s.getRightBound());
 		updateAnim();
 		if (!mousePressed)
 			updateCursor();
 	}
 	
 	public void updateCursor(){
 		int pos = s.whereIs(mouseX, mouseY);
 		switch(pos){
 		case Slider.OUTSIDE:
 			cursor(ARROW);
 			break;
 		case Slider.INSIDE:
 			cursor(MOVE);
 			break;
 		case Slider.LEFTHANDLE:
 		case Slider.RIGHTHANDLE:
 			cursor(HAND);
 		}
 	}
 	
 	public void drawRange(){
 		fill(darkColor);
 		textFont(largerFont);
 		if(rangeMin == rangeMax){
 			String range = ""+rangeMin;
 			int rangeWidth = (int)(textWidth(range) + 0.5);
 			int rangeX = getWidth()/2 - rangeWidth/2;
 			int rangeY = graphY + graphH + 25;
 			text(range, rangeX, rangeY);
 		} else {
 			String range = ""+rangeMin;
 			int rangeWidth = (int)(textWidth(range) + 0.5);
 			int rangeX = graphX;
 			int rangeY = graphY + graphH + 25;
 			text(range, rangeX, rangeY);
 			
 			range = ""+rangeMax;
 			rangeWidth = (int)(textWidth(range) + 0.5);
 			rangeX = graphX + graphW - rangeWidth;
 			rangeY = graphY + graphH + 25;
 			text(range, rangeX, rangeY);
 		}
 		int width = (int)(textWidth(sliderLabel) + 0.5);
 		text(sliderLabel, getWidth()/2 - width/2, 590);
 		width = (int)(textWidth(title)+0.5);
 		text(title, getWidth()/2 - width/2, 25);
 	}
 	
 	public void handleInput(){
 		// Handle user input
 		if (mousePressed == true) {
 			if (unpressed) {
 				// Everything within this block occurs when first clicked
 				// (pressed state toggles on)
 				unpressed = false;
 				int loc = s.whereIs(mouseX, mouseY);
 				if (loc == Slider.INSIDE)
 					movingSlider = true;
 				else if (loc == Slider.LEFTHANDLE)
 					leftHandle = true;
 				else if (loc == Slider.RIGHTHANDLE)
 					rightHandle = true;
 			} else {
 				// Everything in this block occurs when the mose has been
 				// pressed for some period (clicking and dragging, etc.)
 				if(movingSlider){
 					s.dragAll(mouseX, pmouseX);
 				} else if(leftHandle){
 					s.dragLH(mouseX, pmouseX);
 				} else if(rightHandle){
 					s.dragRH(mouseX, pmouseX);
 				}
 				s.snapGoals();
 			}
 		}
 	}
 	
 	public void updateAnim(){
 		// Update animation values (simple spring animation)
 	    int speed = 4;
 	    s.updateAnim(speed);
 	}
 
 	public void mouseReleased() {
 		unpressed = true;
 		movingSlider = false;
 		leftHandle = false;
 		rightHandle = false;
 		s.updateGoals();
 	}
 	
 	public void toggleView(){
 		// FIXME: Finish this method
 		if(currentDisplayed == PART_ONE){
 			s = new Slider(50,700,500,150);
 			Set<Integer> keys = data.keySet();
 			int min = Integer.MAX_VALUE, max = 0;
 			for(int i : keys){
 				if(i < min)
 					min = i;
 				if(i > max)
 					max = i;
 			}
 		} else {
 			s = new Slider(50,700,500,150);
 		}
 		currentDisplayed = (currentDisplayed + 1) % 2;
 	}
 
 	public void drawAxes() {
 		// Draw Axes Lines
 		stroke(darkColor);
 		strokeWeight(3);
 		strokeJoin(BEVEL);
 		strokeCap(SQUARE);
 		noFill();
 		beginShape();
 		vertex(graphX, graphY);
 		vertex(graphX, graphY + graphH);
 		vertex(graphX + graphW, graphY + graphH);
 		endShape();
 		// Draw Labels
 	}
 
     public void drawData(boolean distanceOrSpeed, int minBound, int maxBound,
             int strokeWidth, float graphX, float graphY, float graphWidth,
             float graphHeight) {
         // Set colors and draw lines.
         noFill();
       beginShape();
         strokeWeight(strokeWidth);
 
         //Get and draw data
        float y;
        for(int i = minBound; i < maxBound; i++){
             RaceRow rr = data.get(i);
             if ((rr != null) && (rr.distance > 0)){
                 float year = mapToPlotX(rr.year, minBound, maxBound, graphX, graphWidth);
                 if (distanceOrSpeed == DRAW_DISTANCE){
                     y = mapToPlotY(rr.distance, minDistance, maxDistance,
                             graphY, graphHeight);
                     stroke(rgba(dataColor0, 0x88));
                 }
                 else { //(distanceOrSpeed == DRAW_SPEED)
                     y = mapToPlotY(rr.avgSpeed, minSpeed, maxSpeed,
                             graphY, graphHeight);
                     stroke(rgba(dataColor1, 0x88));
                 }
                 curveVertex(year, y);
             }
             else{
                 endShape();
                 beginShape();
             }
         }
         endShape();
     }
 
     public void drawData(boolean distanceOrSpeed, int minBound, int maxBound) {
         drawData(distanceOrSpeed, minBound, maxBound, 3, graphX, graphY,
                 graphW, graphH);
     }
 	
     public float mapToPlotY(float y, float min, float max, float graphY,
             float graphHeight) {
         // Maps actual values to locations we want to draw
         //Uses 10% buffer to make data more readable
         int buffer = (int) ((max - min) * 0.1);
         float newY = map(
                 y,
                 min - buffer,
                 max + buffer,
                 graphY + graphHeight,
                 graphY
         );
         return newY;
     }
 	
     public float mapToPlotX(float x, float minBound, float maxBound,
             float graphX, float graphWidth) {
         float newX = map(x, minBound, maxBound, graphX, graphX + graphWidth);
     return newX;
 	}
 
 	public int rgba(int rgb, int a){
 		return rgb & ((a << 24) | 0xFFFFFF);
 	}
 	
 	public int rgba(int rgb, float a){
 		if(a < 0)
 			a = 0;
 		if(a > 255)
 			a = 255;
 		return rgba(rgb, a * 255);
 	}
 
 	private class Slider {
 		int x, y, w, h;
 		float left, right;
 		int goalLeft, goalRight;
 		int snappedLeft, snappedRight;
 
 		int drawInterval;
 		
 		int[] values;
 
 		public static final int OUTSIDE = 0, INSIDE = 1, LEFTHANDLE = 2,
 				RIGHTHANDLE = 3;
 
 		public Slider(int x, int y, int w, int h) {
 			this.left = this.x = x;
 			this.y = y;
 			this.w = w;
 			this.h = h;
 			this.right = w + x;
 			goalLeft = (int)(left + 0.5f);
 			goalRight = (int)(right + 0.5f);
 			snappedLeft = goalLeft;
 			snappedRight = goalRight;
 			drawInterval = 1;
 		}
 		
 		public void setDrawInterval(int drawInterval){
 			this.drawInterval = drawInterval;
 		}
 
 		public void setValues(int[] values) {
 			this.values = values;
 			rangeMin = values[0];
 			rangeMax = values[values.length-1];
 		}
 
 		public void drawSlider() {
 			stroke(127,127,127);
 			strokeWeight(2);
 			noFill();
 			strokeJoin(ROUND);
 			beginShape();
 			vertex(x, y+h);
 			vertex(x, y);
 			vertex(x+w, y);
 			vertex(x+w, y+h);
 			endShape();
 			
 			
 			// Draw underlying data
 			fill(0,0,0);
 			strokeWeight(1);
 			stroke(0);
 			textFont(myFont);
 			line(x, y+h, x+w, y+h);
 			for (int i = 0; i < values.length; i++) {
 				int xpos = x + (i) * w / (values.length) + w
 						/ (2 * values.length);
 				if (values[i] % drawInterval == 0 || i == 0 || i == values.length-1) {
 					text(values[i], xpos
 							- (int) (textWidth("" + values[i]) + 0.5) / 2, y
 							+ h + fontSize);
 				}
 				
 				//Draw ruler ticks
 				if(values[i] % 100 == 0){
 					line(xpos, y+h, xpos, y+h - 15);
 				} else if (values[i] % 10 == 0){
 					line(xpos, y+h, xpos, y+h - 10);
 				} else {
 					line(xpos, y+h, xpos, y+h - 5);
 				}
 			}
 
 			// Draw main bar
 			fill(0, 0, 0, 0);
 			for (int i = 0; i < h; i++) {
 				stroke(rgba(darkColor, i * 127 / h));
 				line(left, y + i, right, y + i);
 			}
 			rect(left, y, right - left, h);
 
 			// Draw left handle
 			stroke(0, 0, 0, 0);
 			fill(rgba(darkColor, 127));
 			arc(left, y + 10, 20, 20, PI, 3 * PI / 2);
 			arc(left, y + h - 10, 20, 20, PI / 2, PI);
 			rect(left + 0.5f - 10, y + 10, 10, h - 20);
 
 			fill(darkColor);
 			ellipse(left - 5, y + (h / 2) - 5, 4, 4);
 			ellipse(left - 5, y + (h / 2), 4, 4);
 			ellipse(left - 5, y + (h / 2) + 5, 4, 4);
 
 			// Draw right handle
 			stroke(0, 0, 0, 0);
 			fill(rgba(darkColor, 127));
 			arc(right, y + 10, 20, 20, 3 * PI / 2, 2 * PI);
 			arc(right, y + h - 10, 20, 20, 0, PI / 2);
 			rect(right + 0.5f, y + 10, 10, h - 20);
 
 			fill(darkColor);
 			ellipse(right + 5, y + (h / 2) - 5, 4, 4);
 			ellipse(right + 5, y + (h / 2), 4, 4);
 			ellipse(right + 5, y + (h / 2) + 5, 4, 4);
 		}
 
 		public int whereIs(int x, int y) {
 			int ret = OUTSIDE;
 			if (x >= left && x <= right && y > this.y && y < this.y + h) {
 				ret = INSIDE;
 			} else if (x > left - 10 && x < left && y > this.y
 					&& y < this.y + h) {
 				ret = LEFTHANDLE;
 			} else if (x > right && x < right + 10 && y > this.y
 					&& y < this.y + h) {
 				ret = RIGHTHANDLE;
 			}
 			return ret;
 		}
 		
 		public void dragAll(int nx, int px){
 			goalLeft += nx-px;
 			goalRight += nx-px;
 			if(goalLeft < x){
 				goalRight += x-goalLeft;
 				goalLeft += x-goalLeft;
 			}
 			if(goalRight > x + w){
 				goalLeft -= (goalRight-(x+w));
 				goalRight -= (goalRight-(x+w));
 			}
 		}
 		
 		public void dragLH(int nx, int px){
 			goalLeft += nx-px;
 			if(goalLeft < x){
 				goalLeft += x-goalLeft;
 			} else if(goalLeft > goalRight - w/values.length){
 				goalLeft = goalRight - w/values.length;
 			}
 		}
 		
 		public void dragRH(int nx, int px){
 			goalRight += nx-px;
 			if(goalRight > x+w){
 				goalRight -= (goalRight-(x+w));
 			} else if(goalLeft > goalRight - w/values.length){
 				goalRight = goalLeft + w/values.length;
 			}
 		}
 		
 		public void snapGoals(){
 			int leftX = goalLeft - x;
 			float ratioL = leftX / (float)w;
 			int index = (int)(ratioL * values.length + 0.5);
 			snappedLeft = x + w * index / values.length;
 			if(index == 0)
 				snappedLeft = x;
 			rangeMin = values[index];
 			
 			int rightX = goalRight - x;
 			float ratioR = rightX / (float)w;
 			index = (int)(ratioR * values.length + 0.5);
 			if(index == values.length)
 				snappedRight = x+w;
 			snappedRight = x + w * index / values.length;
 			rangeMax = values[index-1];
 		}
 		
 		public int getLeftBound(){
 			int leftX = (int)(left + 0.5) - x;
 			float ratioL = leftX / (float)w;
 			int index = (int)(ratioL * values.length + 0.5);
 			return values[index];
 		}
 		
 		public int getRightBound(){
 			int rightX = (int)(right + 0.5) - x;
 			float ratioR = rightX / (float)w;
 			int index = (int)(ratioR * values.length + 0.5);
 			return values[index-1];
 		}
 		
 		public void updateGoals(){
 			goalLeft = snappedLeft;
 			goalRight = snappedRight;
 		}
 		
 		public void updateAnim(int slowness){
 			if(abs(snappedLeft-left) > 0){
 				left += (snappedLeft - left) / slowness;
 				if(abs(snappedLeft - left) == 1){
 					left = snappedLeft;
 				}
 			}
 			if(abs(snappedRight-right) > 0){
 				right += (snappedRight - right) / slowness;
 				if(abs(snappedRight - right) == 1){
 					right = snappedRight;
 				}
 			}
 		}
 	}
 }
