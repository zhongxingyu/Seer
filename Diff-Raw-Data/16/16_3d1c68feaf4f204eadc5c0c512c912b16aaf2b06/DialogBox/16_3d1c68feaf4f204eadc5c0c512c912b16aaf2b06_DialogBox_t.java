 package framework;
 import java.util.ArrayList;
 import java.util.List;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 /**
  * General Class for instantiating Dialog Boxes.
  * 
  * To use this class, another class can instantiate dialog boxes and put them in a
  * list or an array. Then, in both the render and update method of that class, iterate
  * through the list of dialog boxes, calling DialogBox.render(container, graphic) or 
  * DialogBox.update(container, delta) as appropriate. 
  * 
  * Furthermore, the class that instantiates a dialog box must pass in a Unicode Font that has
  * glyphs and color effects added. I assume that the GameState will generally be the one to
  * instantiate boxes, so this is fine, but fonts take much time to create on the spot, so it
  * is important that the class that instantiates a dialog box have a preloaded, or pre-passed-in
  * font to use. 
  * 
  * Also, in the spirit of giving credit where it is due, this code comes from the user davedes
  * from the website: http://slick.javaunlimited.net/viewtopic.php?t=3778. 
  * I will delete this text as I rewrite the code as necessary.
  * @author Sonokin
  *
  */
 public class DialogBox{
 	private final int x;
 	private final int y;
 	private int boxIndex;
 	private boolean active = false;
 	
 	private List<String> messages;
 	private List<String> boxes;
 	
 	private UnicodeFont font;
 	private int width;
 	private int height;
 	private int padding = 5;
 	private Color box = new Color(1f,1f,1f,0.45f);
 	
 	private int currLine = 0;
 	private int currChar = 0;
 	   
 	private final static int REGULAR_DELAY = 50;
 	private final static int QUICK_DELAY = 10;
 	private int delay = REGULAR_DELAY;
 	private int time = delay;
 	private boolean finished = false;
 	
 	private int proceedTextKey;
 	private boolean proceedStall;
 	
 	/**
 	 * Constructor for creating the dialog box.
 	 * Incoming string is converted to List<String> object for use in wrapping.
 	 * 
 	 * @param x: X Coordinate for dialog box.
 	 * @param y: Y Coordinate for dialog box.
 	 * @param text: String that will be shown in dialog box.
 	 * @param font: Font that the dialog box will use to render the text.
 	 * @param key: Key that, when pressed will accelerate the text rendering.
 	 */
 	public DialogBox(int x, int y, int width, int height, UnicodeFont font, int proceedKey){
 		this.width = width;
 		this.height = height;
 		this.x = x;
 		this.y = y;
 		this.font = font;
 		proceedTextKey = proceedKey;
 		proceedStall = false;
 	}
 	/**
 	 * Main function that renders the dialog box and displays the text
 	 * in a typewriter-like fashion. The text box only renders as many lines
 	 * as can fit within the text box, and thus there is a scrolling effect for
 	 * long text.
 	 * 
 	 * @param container
 	 * @param g
 	 */
 	public void render(GameContainer container, Graphics g){
 		if (active){
 			int lineLocation = this.y;
 			
 	        g.setColor(box);
 	        g.fillRect(x-padding, y-padding, width+padding*2, height+padding*2);
 	        g.setColor(Color.white);
 	        
 	        int lineHeight = font.getLineHeight();
 	        int maxLines = height/lineHeight;
 	        int startRender = Math.max(0, currLine-maxLines+1);
 	       
 	        //only render the rows we have typed out so far (renderRow = current row)
 	        for (int i=startRender; i<= currLine; i++) {
 	            String line = messages.get(i);
 	            //render whole line if it's a previous one, otherwise render the column
 	            int len;
 	            if (i < currLine){
 	            	len = line.length();
 	            }
 	            else{
 	            	len = currChar;
 	            }
 	            String t = line.substring(0, len);
 	            if (t.length()!=0) {
	            	font.drawString(x, lineLocation, t, Color.white);
 	            }
 	            lineLocation += lineHeight;
 	        }
 		}
 		
 	}
 	
 	/**
 	 * Method that updates the typewriter effect
 	 * 
 	 * @param container
 	 * @param delta
 	 * @throws SlickException
 	 */
     public void update(GameContainer container, int delta) throws SlickException {
     	if (active){
     		time -= delta;
             keyPressed(container.getInput());
             if (time<=0 && !finished) {
                 time = delay;
             
     	        //Check to see if we should move down to next line. 
     	        if (currChar > messages.get(currLine).length()-1){
     	        	//Everything has been rendered
     	        	if (currLine >= messages.size()-1){
     	        		finished = true;
     	        	}
     	        	else{//Move to next line
     	        		currLine++;
     	        		currChar = 0;
     	        	}
     	        }
     	        else{//move to the next character
     	        	currChar++;
     	        }
             }
     	}
         
     }
    
     /**
      * Splits the given string into a list of lines that will be used
      * to create the wrapping effect of the text in the dialog box.
      * 
      * @param text: String to be wrapped.
      * @param font: Font whose dimensions are used to split the lines.
      * @param width: Length of dialog box used to determine where to end lines.
      * @return: ArrayList of split strings.
      */
     //Wraps the given string into a list of split lines based on the width
     private List<String> wrap(String text, UnicodeFont font, int width) {
         //A less accurate but more efficient wrap would be to specify the max
         //number of columns (e.g. using the width of the 'M' character or something).
         //The below method will look nicer in the end, though.
        
         List<String> list = new ArrayList<String>();
         String str = text;
         String line = "";
        
         //we will go through adding characters, once we hit the max width
         //we will either split the line at the last space OR split the line
         //at the given char if no last space exists
        
         //while we still have text to check
         int i = 0;
         int lastSpace = -1;
         while (i<str.length()) {
             char c = str.charAt(i);
             if (Character.isWhitespace(c))
                 lastSpace = i;
            
             //time to wrap
             if (c=='\n' || font.getWidth(line + c) > width) {
                 //if we've hit a space recently, use that
                 int split = lastSpace!=-1 ? lastSpace : i;
                 int splitTrimmed = split;
                
                 //if we are splitting by space, trim it off for the start of the
                 //next line
                 if (lastSpace!=-1 && split<str.length()-1) {
                    splitTrimmed++;
                 }
                
                 line = str.substring(0, split);
                 str = str.substring(splitTrimmed);
                
                 //add the line and reset our values
                 list.add(line);
                 line = "";
                 i = 0;
                 lastSpace = -1;
             }
             //not time to wrap, just keep moving along
             else {
                 line += c;
                 i++;
             }
         }
         if (str.length()!=0)
             list.add(str);
         return list;
     }
     
 /* UNUSED CODE
     private List<String> parseDialog(String dialog, char demarcation){
     	List<String> boxes  = new ArrayList<String>();
     	int j = 0;
     	for (int i = 0; i <dialog.length(); i++){
     		if (dialog.charAt(i) == demarcation){
     			String box = dialog.substring(j, i);
     			j = i+1;
     			boxes.add(box);
     		}
     	}
     	return boxes;
     }
 */
     
     private void proceed(){
     	if (active){
     		finished = false;
         	currChar = 0;
 			currLine = 0;
 			if (boxIndex < boxes.size() - 1) {
 				boxIndex++;
 				messages = wrap(boxes.get(boxIndex), font, width);
 			} else {
 				close();
 			}
 		}
     	
     }
     /**
      * Method that opens a new dialog box, and starts playing text.
      * @param text: List of strings that will be rendered in the dialog box.
      */
     public void playMsg(ArrayList<String> text){
     	boxes = text;
     	messages = wrap(boxes.get(boxIndex),font,width);
     	active = true;
     }
     
     /**
      * Method that closes a dialog box. Call this method after all text has been rendered.
      */
     private void close(){
     	active = false;
    	boxIndex = 0;
     }
 
     /**
      * Method that takes whatever input the container is currently receiving and compares 
      * it to the key that has been designated for speeding up text. 
      * 
      * @param c
      */
     private void keyPressed(Input p){
     	if (p.isKeyDown(proceedTextKey)){
     		delay = QUICK_DELAY;
     		if (finished && !proceedStall) {
         		proceed();
     		} else {
     			proceedStall = true;
     		}
     	}
     	else {
     		delay = REGULAR_DELAY;
     		proceedStall = false;
     	}	
     }
     /**
      * @return Boolean stating whether or not this dialog box is active (visible) or not.
      */
     public boolean isActive() {
     	return active;
     }
 }
