 package AnimatedLinkedList;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import org.jdesktop.core.animation.timing.Animator;
 import org.jdesktop.core.animation.timing.PropertySetter;
 import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;
 
 import AnimatedDataStructure.AnimatedDataStructure;
 import AnimatedDataStructure.Change;
 import AnimatedDataStructure.ChangeLabel;
 import AnimatedDataStructure.ContinuousAnimation;
 import AnimatedDataStructure.Rect;
 import AnimatedDataStructure.Step;
 import AnimatedDataStructure.setupGUI;
 
 
 
 //example template class that displays a linked list, some classes have been left in from AnimatedArray, whilst others
 //ommited that clashed. currently animation hasn't been implemented
 
 public final class AnimatedLinkedList extends JPanel implements AnimatedDataStructure {
 	//private int counter = 0; //counter for paintComponent
 	private static Toolkit toolkit =  Toolkit.getDefaultToolkit ();
 	public static Dimension Fullscreen = toolkit.getScreenSize();
 	private JButton nextBtn;																			// getting reference to the button
  	private JButton prevBtn;																			// same as above
 	private JButton pauseBtn;
         public static int windowWidth = (int) Fullscreen.getWidth();
 	public static int windowHeight = (int) Fullscreen.getHeight();
 	private LinkedList<Step> steps = new LinkedList<Step>();							// this is where all the steps of animation will be stored																			// we need to to have the reference to the drawing area to repaint it																		// could just REMOVE. Though keeping it with important variables might be a good idea as well. I don't like the fact that the minimum value is 1 second of each animation.
  	private final SwingTimerTimingSource ani_timer = new SwingTimerTimingSource(); // this is for starting the animation
 	private int currentStep;																			// the currentStep
  	private boolean continuousAnimation = false; // variable to determine whether to run a continuous animation or not.
  	private int time;
  	private boolean screenshot = true;
  	
  	private linkedList rect_list;																// we could just REMOVE this																	// same as above															// this is where all the rectangles which will be drawn are stored
 	private String info = ""; 																// a string to display information at each step
 	
 	private int arrayLeft;
 
 	public AnimatedLinkedList(int[] arr){
 		System.setProperty("swing.defaultlaf", "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
 		Animator.setDefaultTimingSource(ani_timer);
 
 		arrayLeft = arr.length;	//MAX = 85
 		
 		rect_list = new linkedList(); //a linked list of rectangles
 
 		//the scaling/resizing code is not working currently
 		int rs = rectSize();
 		int h = rs;
 		int w = rs;
 		int x= arrayLeft*rs+rectSpace();
 		int y= 2*rs;
 		time = 100;
 		currentStep = 0;
 		setOpaque(true);
 
 		Step s = new Step();																	// create a new step to store initial values and all. We wouldn't really need it if 
 		setInfo("Start of the animation");																	// it wasn't for the "Info" field
 		s.setInfo(info);
 
 
 		//as its a stack, items need to be added in reverse to begin with to replicate its appearance
 
 		for(int i = 0;i<arrayLeft;i++){
 			Rect r = new Rect(x, y, w, h, arr[i]+"", this);								// create all the Rectangle and add them to rect_list
 			rect_list.addFirst(r);
 			x -= rectSpace();
 		}
 		steps.add(s);
 	}
         
         
         public AnimatedLinkedList(int[] arr, int width, int height){
                 windowWidth = width;
                 windowHeight = height;
 		System.setProperty("swing.defaultlaf", "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
 		Animator.setDefaultTimingSource(ani_timer);
 
 		arrayLeft = arr.length;	//MAX = 85
 		
 		rect_list = new linkedList(); //a linked list of rectangles
 
 		//the scaling/resizing code is not working currently
 		int rs = rectSize(windowWidth);
 		int h = rs;
 		int w = rs;
 		int x= arrayLeft*rs+rectSpace(rs);
 		int y= 2*rs;
 		time = 100;
 		currentStep = 0;
 		setOpaque(true);
 
 		Step s = new Step();																	// create a new step to store initial values and all. We wouldn't really need it if 
 		setInfo("Start of the animation");																	// it wasn't for the "Info" field
 		s.setInfo(info);
 
 
 		//as its a stack, items need to be added in reverse to begin with to replicate its appearance
 
 		for(int i = 0;i<arrayLeft;i++){
 			Rect r = new Rect(x, y, w, h, arr[i]+"", this);								// create all the Rectangle and add them to rect_list
 			rect_list.addFirst(r);
 			x -= rectSpace(rs);
 		}
 		steps.add(s);
 	}
 
 	private int rectSpace() {	  	
 		return (int) rectSize()/arrayLeft + rectSize()+1;
 	}
         
         private int rectSpace(int rectSize) {	  	
 		return (int) rectSize/arrayLeft + rectSize+1;
 	}
 	
 
 	private int rectSize() {	  	
 		return (int) ((Fullscreen.getWidth()-(arrayLeft))/(arrayLeft+4)); 	
 	}
 	
         private int rectSize(int width){
             return (int) ((width-(arrayLeft))/(arrayLeft+4)); 	
 	}
         
 	//locates, identifies and removes the linked list element with String S
 	public void findAndRemove(String i, AnimatedLinkedList anim){
 		Step s = new Step();
 		// Create animation for showing information on what's happening
 		String information = "findAndRemove ";
 		s.setInfo(information);	
 		// Change information
 		s.addAnimator(new Animator.Builder().setDuration(1, TimeUnit.MILLISECONDS).build(), s.getFirstAnimator());																		// create an animator for this, which we could use to trigger this change
 		s.getLastAnimator().addTarget(new ChangeLabel(this, information, this));		
 
 		System.out.println("Attempting to remove string " + i);
 
 		//get first item
 		Node<Rect> node = rect_list.getHead();
 		//maintain a pointer to the previous node
 		Node<Rect> prev = rect_list.getHead();
 		int count = 0;
 
 		if(i == rect_list.getHead().getData().getLabel()) {
 			rect_list.removeFirst();
 		} else {
 			while(node.getNext() != null && !(node.getData().getLabel().equals(i))){
 				prev = node; //pointer to previous node
				setColor(count,Color.RED,"Changing color to red");
				setColor(count,Color.BLUE,"Changing color to blue");
 				node = node.getNext();
 				count++;
 			}
			anim.setColor(count,Color.GREEN,"Changing color to green");
			anim.setColor(count,Color.WHITE,"Changing color to white");
 			//prev.setNext(node.getNext());   //joins the pointer for the prev element after the to be deleted node
 			//rect_list.removeNode(node); //removes this node
 			
 	
 			//here I want to add the changes associated with the above commented lines, respectively.
 			//if the above lines are not commented out, they will not be represented as a step and 
 			//changes will be displayed to the user once the program is started
 			
 			//the changes i've added here don't reflect real changes in the code yet, but I'm trying
 			//to get stepForward() to retrieve them
 			
 			// Trigger for a continuous animation
 			s.addChanges(new Change("pointerSwitch", prev.getData(), "Assigning pointer from previous element"));
 			s.addChanges(new Change("deleteNode", prev.getNext().getData(), "Removing node"));
 		}
		currentStep++;
 		// Trigger for a continuous animation
 		s.addAnimator(new Animator.Builder().setDuration(1, TimeUnit.MILLISECONDS).build(), nextBtn);
 		s.getLastAnimator().addTarget(new ContinuousAnimation(currentStep+1, this));
 		steps.add(s);
 	}
 	
 	public void swap (int a, int b, String message) {
 		currentStep++;																																					// increment currentStep
 		Step s = new Step();																																					// create a new step
 		// Create animation for showing information on what's happening
 		String information = "Swapping index " + a + " (" + rect_list.get(a).getLabel() + ") " + " with index " + b + " (" + rect_list.get(b).getLabel() + ")";			// create the string
 		s.setInfo(information);																																			// add it to the step
 		// Change information
 		s.addAnimator(new Animator.Builder().setDuration(1, TimeUnit.MILLISECONDS).build(), s.getFirstAnimator());																		// create an animator for this, which we could use to trigger this change
 		s.getLastAnimator().addTarget(new ChangeLabel(this, information, this));																				// create a timing target to change the label when the animation starts	
 		// Create the Animators and PropertySetters (what should the animation change the property from what to what) for swapping rects
 		s.addAnimator(new Animator.Builder().setDuration(time, TimeUnit.MILLISECONDS).build(), s.getFirstAnimator());															// we want this Animator to start the same time the first Animator in the step does															
 		s.getLastAnimator().addTarget(PropertySetter.getTarget(rect_list.get(a), "currentY", rect_list.get(a).getRec().y, rect_list.get(a).getRec().y+rectSpace()));
 		s.addAnimator(new Animator.Builder().setDuration(time, TimeUnit.MILLISECONDS).build(), nextBtn);																		// we want this Animator to start straight after the last Animator finishes. This is how it's done just now
 		s.getLastAnimator().addTarget(PropertySetter.getTarget(rect_list.get(a), "currentX", rect_list.get(a).getRec().x, rect_list.get(b).getRec().x)); 
 		s.addAnimator(new Animator.Builder().setDuration(time, TimeUnit.MILLISECONDS).build(), nextBtn);
 		s.getLastAnimator().addTarget(PropertySetter.getTarget(rect_list.get(a), "currentY", rect_list.get(a).getRec().y+rectSpace(), rect_list.get(a).getRec().y));		
 		//rect 2
 		s.addAnimator(new Animator.Builder().setDuration(time, TimeUnit.MILLISECONDS).build(), s.getFirstAnimator());															// we want this Animator to start the same time the first Animator in the step does
 		s.getLastAnimator().addTarget(PropertySetter.getTarget(rect_list.get(b), "currentY", rect_list.get(b).getRec().y, rect_list.get(b).getRec().y-rectSpace()));
 		s.addAnimator(new Animator.Builder().setDuration(time, TimeUnit.MILLISECONDS).build(), nextBtn);
 		s.getLastAnimator().addTarget(PropertySetter.getTarget(rect_list.get(b), "currentX", rect_list.get(b).getRec().x, rect_list.get(a).getRec().x));
 		s.addAnimator(new Animator.Builder().setDuration(time, TimeUnit.MILLISECONDS).build(), nextBtn);
 		s.getLastAnimator().addTarget(PropertySetter.getTarget(rect_list.get(b), "currentY", rect_list.get(b).getRec().y-rectSpace(), rect_list.get(b).getRec().y));
 		// Trigger for a continuous animation
 		s.addAnimator(new Animator.Builder().setDuration(1, TimeUnit.MILLISECONDS).build(), nextBtn);
 		s.getLastAnimator().addTarget(new ContinuousAnimation(currentStep+1, this));
 		// Log the changes in the Step
 		s.addChanges(new Change("swap", rect_list.get(a), rect_list.get(a).getRec().x, rect_list.get(a).getRec().y));
 		s.addChanges(new Change("swap", rect_list.get(b), rect_list.get(b).getRec().x, rect_list.get(b).getRec().y));
 		// Apply the changes to rect_list
 			// We swap the position of 2 Rects in the animation, so we do this here
 		int x = rect_list.get(a).getRec().x;
 		rect_list.get(a).getRec().x = rect_list.get(b).getRec().x;
 		rect_list.get(b).getRec().x = x;
 			// We also want to swap the positions of Rects in our rect_list, so swapping would be according to current indexes, rather than initial ones
 		Rect tempRect = rect_list.get(b);
 		rect_list.set(b, rect_list.get(a));
 		rect_list.set(a, tempRect);
 		steps.add(s);																																					// add the new step to steps
 	}
 	
 
 	public void modifyLabel(int index, String dataValue, String info) {}
 	
 	public void setColor(int index, Color c, String message) {
 		currentStep++;
 		Step s = new Step();
 		//get first item
 		Node<Rect> node = rect_list.getHead();
 		for (int i = 0; (i < index)&&(node != null); i++) {
 			node = node.getNext();
 		}
 		// Animate information
 		String information = "Changing index " + index + " color from " + node.getData().getColor()+ " to " + c;
 		s.setInfo(information);
 		s.addAnimator(new Animator.Builder().setDuration(1, TimeUnit.MILLISECONDS).build(), nextBtn);
 		s.getLastAnimator().addTarget(new ChangeLabel(this, information, this));
 		// Animate change of color
 		s.addAnimator(new Animator.Builder().setDuration(time, TimeUnit.MILLISECONDS).build(), s.getFirstAnimator());
 		s.getLastAnimator().addTarget(PropertySetter.getTarget(node.getData(), "colorDraw", node.getData().getColor(), c));
 		// Trigger for a continuous animation
 		s.addAnimator(new Animator.Builder().setDuration(1, TimeUnit.MILLISECONDS).build(), nextBtn);
 		s.getLastAnimator().addTarget(new ContinuousAnimation(currentStep+1, this));
 		// Log the Changes in Step
 		s.addChanges(new Change("color", node.getData(), node.getData().getColor()));
 		// Apply the changes to rect_list
 		node.getData().setColor(c);
 		steps.add(s);
 	}
 
 	/* function to stepBack from one step to a previous one.
 	 * Will have to be extended whenever we add another API command
 	 */
 	private void changeBack(Change tempChange) {
 		if (tempChange.getType() == "swap") {
 			// just restore coordinates
 			tempChange.getReference().getRec().x = tempChange.getPoint().x;
 			tempChange.getReference().getRec().y = tempChange.getPoint().y;
 		} else if (tempChange.getType() == "color") {
 			// just restore the color
 			tempChange.getReference().setColor(tempChange.getColor());
 		} else if (tempChange.getType() == "modifyLabel") {
 			// just restore the label
 			tempChange.getReference().setLabel(tempChange.getLabel());
 		} else if (tempChange.getType() == "add") {
 			// remove the rectangle
 			rect_list.remove(tempChange.getReference());
 		}
 	}
 	
 	/* function for changing the information on what's happening */
 	public void setInfo(String info) {
 		this.info = info;
 	} 
 	
 	/* function to stepForward from one step to the next one.
 	 * Will have to be extended whenever we add another API command
 	 */
 	public void stepForward() {
 		currentStep++; // increment the currentStep (because we're moving to the next one)
 		if (steps.get(currentStep).getChanges().get(0).getType() == "add") {
 			rect_list.addFirst(steps.get(currentStep).getChanges().get(0).getReference());
 			this.repaint();
 		}
 		//as a test, I attempted to get hold of one of the changes I made, so that I can handle 
 		//pointer changes.
 		//System.out.println("Next step: " + steps.get(currentStep+1).getChanges().get(0).getType());
 		if (steps.get(currentStep).getChanges().get(0).getType() == "pointerSwitch") {
 			System.out.println("found pointer switch");
 		}
 
 		
 		//here i'm printing out all changes, but "pointerSwitch" or "deleteNode" aren't listed
 		//System.out.println(steps.get(currentStep).getChanges().get(0).getType());
 
 		
 		
 		if (steps.get(currentStep).getList().size() != 0) { // if there are any Animators inside the step, trigger the first one
 			steps.get(currentStep).getList().get(0).start();
 		}
 	}
 	
 	public void stepBack() {
 		setInfo("");
 		for(Change tempChange: steps.get(currentStep).getChanges()) {						// in each Step we are storing the values before the animation, so we
 			changeBack(tempChange);															// restore it all like it was before this step and then decrement the
 		}																					// currentStep value, as well as call Main.panel.repaint() to redraw
 		this.repaint();																// the panel in order to show these changes
 		currentStep--;
 	}
 
     public void endAnimation(){
         while (currentStep != 0) {														// because of a few slight reasons, when we construct the animation, we end up at
 		//setInfo(steps.get(currentStep).getInfo());								// the last step, so we need to return to the initial state
 		for(Change tempChange: steps.get(currentStep).getChanges()) {
 			changeBack(tempChange);
 		}
 		currentStep--;
         }
         new setupGUI(this);
     }
     
 	//adds an element to the front of the list
 	public void addToHead(int i){
 		currentStep++;
 		Step s = new Step();	
 		
 		// Create animation for showing information on what's happening
 		String information = "Adding to head";
 		s.setInfo(information);	
 
 		Rect rec = new Rect(rect_list.getHead().getData().getRec().x-rectSpace(), rect_list.getHead().getData().getRec().y, rectSize(), rectSize(), i+"", this);
 		rect_list.addFirst(rec);
 
 		// Trigger for a continuous animation
 		s.addAnimator(new Animator.Builder().setDuration(1, TimeUnit.MILLISECONDS).build(), nextBtn);
 		s.getLastAnimator().addTarget(new ContinuousAnimation(currentStep+1, this));
 		// Log the changes in the Step
 		s.addChanges(new Change("add", rec, "Added a rectangle with value "+i));
 		
 		steps.add(s);	
 	}
 
 
 	/*an overriden method from JFrame: paints the actual rectangle on the screen. It is called each time timingEvent calls repaint() */
 	protected void paintComponent(Graphics g){
 
 		//counter++;
 
 		Graphics g2g = (Graphics2D) g;
 		((Graphics2D) g2g).setBackground(Color.WHITE);
 
 		for(int x=0;x<10;x++){
 			g2g.clearRect(0, 0, getWidth(), getHeight()); //clear all rectangles
 		}
 
 		//g2g.setColor(Color.BLUE);
 		g2g.setColor(Color.BLUE);
 		g2g.drawString(info,(int)(Fullscreen.width/4),(int)(Fullscreen.height*3/4));
 		Font font = new Font("Arial", Font.PLAIN, (int) 20 * rectSize(windowWidth) / 72);
 		g2g.setFont(font);
 		
 		
 		//the linked list is converted into an array so that it can be worked with
 		ArrayList<Rect> arr = new ArrayList<Rect>();
 		arr = rect_list.toArray();
 
 		/* fill the rectangles and draw a tag next to each one */
 		
 		for(Rect r : arr){
 			g2g.setColor(r.getColor());
 			g2g.fillRect(r.getRec().x, r.getRec().y,r.getRec().width,r.getRec().height);
 			g2g.setColor(Color.WHITE);
 			g2g.drawString(r.getLabel(), r.getRec().x+(rectSize(windowWidth)/2)-10, r.getRec().y+(rectSize(windowWidth)/2));
 		}
 
 		//draw pointers, including final NULL pointer
 		for(int i=0;i<arr.size()-1;i++){
 			g2g.setColor(Color.BLACK);
 			g2g.drawLine((arr.get(i).getRec().x+40)+arr.get(i).getRec().width/2, arr.get(i).getRec().y+20+arr.get(i).getRec().height/2, arr.get(i+1).getRec().x-40 + arr.get(i+1).getRec().width/2, arr.get(i+1).getRec().y+20+arr.get(i+1).getRec().height/2);
 			g2g.fillOval(arr.get(i+1).getRec().x-40 + arr.get(i+1).getRec().width/2, arr.get(i+1).getRec().y+15+arr.get(i+1).getRec().height/2, 10, 10);
 		}
 
 		g2g.drawLine((arr.get(arr.size()-1).getRec().x+40)+arr.get(arr.size()-1).getRec().width/2, arr.get(arr.size()-1).getRec().y+20+arr.get(arr.size()-1).getRec().height/2,      arr.get(arr.size()-1).getRec().x+rectSpace(rectSize(windowWidth))+30,arr.get(arr.size()-1).getRec().y+20+arr.get(arr.size()-1).getRec().height/2);
 		g2g.setColor(Color.RED);
 		g2g.fillOval(arr.get(arr.size()-1).getRec().x+rectSpace(rectSize(windowWidth))+30,arr.get(arr.size()-1).getRec().y+20+arr.get(arr.size()-1).getRec().height/2-5, 10, 10);
 
 		/* draw the information 
 		g2g.setColor(Color.BLUE);
 		g2g.drawString(info,(int)(Fullscreen.width/4),(int)(Fullscreen.height*3/4));
                 * */
 	}
 	
 	/* Getters/Setters */
 	// Fullscreen
 	public Dimension getFullscreen() {
 		return Fullscreen;
 	}
 	
 	public void setFullscreen(Dimension dimension) {
 		Fullscreen = dimension;
 	}
 	// nextButton
 	public JButton getNextButton() {
 		return nextBtn;
 	}
 	
 	public void setNextButton(JButton button) {
 		nextBtn = button;
 	}
 	// prevButton
 	public JButton getPrevButton() {
 		return prevBtn;
 	}
 	
 	public void setPrevButton(JButton button) {
 		prevBtn = button;
 	}
 	// pauseButton
 	public JButton getPauseButton() {
 		return pauseBtn;
 	}
 	
 	public void setPauseButton(JButton button) {
 		pauseBtn = button;
 	}
 	// Steps
 	public LinkedList<Step> getSteps() {
 		return steps;
 	}
 	// Ani_timer
 	public SwingTimerTimingSource getAniTimer() {
 		return ani_timer;
 	}
 	// Current Step
 	public int getCurrentStep() {
 		return currentStep;
 	}
 	
 	public void setCurrentStep(int currentStep) {
 		this.currentStep = currentStep; 
 	}
 	// ContinuousAnimation
 	public boolean getContinuousAnimation() {
 		return continuousAnimation;
 	}
 	
 	public void setContinuousAnimation(boolean continuousAnimation) {
 		this.continuousAnimation = continuousAnimation;
 	}
 	// Time / Animations speed
 	public int getTime() {
 		return time;
 	}
 	
 	public void setTime(int time) {
 		this.time = time;
 	}
 	// Screenshot
 	public boolean getScreenshot() {
 		return screenshot;
 	}
 	
 	public void setScreenshot(boolean value) {
 		this.screenshot = value;
 	}
 
     @Override
     public int getWindowWidth() {
         return windowWidth;
     }
 
     @Override
     public int getWindowHeight() {
         return windowHeight;
     }
 }
 
