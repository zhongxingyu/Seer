 /**
  * Player Class
  * Thus far, this is the listener for mouse and key events
  *
  * Later on, it may be necessary to add a new Character class for character stats and such.
  */
 import java.awt.event.*;
 import java.io.*;
 
 
 
 public class Player implements MouseListener, KeyListener {
 	
 	
 	//Camera X and Camera Y. These are in 1:1 game world coordinates.
 	//This should not affect interface rendering.
 	double cx = 0;
 	double cy = 0;
 	
 	//Camera zoom. Any value > 1 will magnify.
 	//This should not affect interface rendering.
 	double zoom = 1;
 	
 	Obj mouseObj;
 	Obj dropObj;
 	
     public Player() {
     	Global.player = this;
     }
     
     public void mouseClicked(MouseEvent e){
     }
     
     public void mouseEntered(MouseEvent e){
     	
     }
     
     public void mouseExited(MouseEvent e){
     	
     }
     
     public void mousePressed(MouseEvent e){
     	/**	Convert screen coordinates to relative coordinates 
     	 *	to world coordinates, and then go through The layered 
     	 *	objects list to determine what was being clicked. 
     	 **/
     	
     	//determine REAL coord of the click 
     	PointS clickedPt = new PointS(e.getX(), e.getY());
     	
     	//determine what obj was being clicked
     	mouseObj = null;
     	mouseObj = findClickedObj(clickedPt);
     	
     	//do stuff depending on if we clicked something
     	//might not need this part
     	if(mouseObj == null){ 
     		//clicked on nothing
     	} else { 
     		//clicked on something; nothing here for now (testing purposes)
     		mouseObj.mouseClicked(e);
     	}
     	
     	//check if right or left clicked
 	    switch (e.getButton()) {
 	    	case 1: 	this.leftClick(e, mouseObj);  //left-button click 
 	    				break;
 	    	case 2:		this.middleClick(e, mouseObj);  //right-button click
 	    				break;
 	    	case 3: 	this.rightClick(e, mouseObj);  //middle-button click
 	    				break;
 	    }
 	    
     }
     
     public void mouseReleased(MouseEvent e){
     	
     	PointS clickedPt = new PointS(e.getX(), e.getY());
     	
     	dropObj = null;
     	dropObj = findClickedObj(clickedPt);
     	
     	if(dropObj == null) return;
     	
     	if(mouseObj != null && dropObj != null)
     		mouseObj.mouseDropped(e,dropObj);
     	
     }
     
     public void keyTyped(KeyEvent e){
     	
     }
     
     public void keyPressed(KeyEvent e){
     	char key = e.getKeyChar();
     	int modNum = e.getModifiers();
     	String modifiers = KeyEvent.getKeyModifiersText(modNum);
     	System.out.print("Key pressed: "+ key +" with mod "+ modifiers +" modNum: "+modNum+ " with effect: ");
     	switch (key){
     		case '`':	System.out.println("All Ctrl groups selected"); break;
     		case '1':	System.out.println("Ctrl Group1 selected"); break;
     		case '2':	System.out.println("Ctrl Group2 selected"); break;
     		case '3':	System.out.println("Ctrl Group3 selected"); break;
     		case '4':	System.out.println("Ctrl Group4 selected"); break;
     		case 'r':  	increaseSpeed(); System.out.println("Speed increased!"); break;
     		case 'f':	decreaseSpeed(); System.out.println("Speed decreased!"); break;
     	}
     	
     }
     
     public void keyReleased(KeyEvent e){
     	
     }
     
     private void leftClick(MouseEvent e, Obj clickedObj){
     	if (clickedObj==null){
     		Global.view.Clicked = null; //this for testing
     		Global.state.playerObj.aimTarget = null;
     	} else {
     		//if we L-Clicked on ship, set it as target
     		if(mouseObj instanceof ShipObj){
     			Global.state.playerObj.aimTarget = (ShipObj) mouseObj;
     			Global.view.Clicked = clickedObj;
     		}
     	}
     }
     
     private void rightClick(MouseEvent e, Obj clickedObj){
     	//System.out.println("TEST: "+ Global.playerObj.getSpeed());
     	//make player rotate towards click-location 
     	if (!(clickedObj instanceof UIElement)){	
 			rotateToClick(e);	
 			PointS point = (new PointS(e.getX(),e.getY())).toWorld();
 			
 			new NavPointerObj(point.x, point.y, 0.75);
     	}
     }
     
     private void middleClick(MouseEvent e, Obj clickedObj){
     	System.out.println("AHHH");
     }
     
     private void rotateToClick(MouseEvent e){
     	PointS clickedPt = new PointS(e.getX(), e.getY());
 		PointS realPt = clickedPt.toWorld();
 		realPt.x -= cx; realPt.y -= cy;
 		Global.state.playerObj.rotateTowards(realPt);
     }
     
     public Obj findClickedObj(PointS clickedPt){
     	for(int x = Global.view.drawObjects.size()-1; x>=0; x--){ //check from obj closest to front
     		Obj currentObj = Global.view.drawObjects.get(x);
     		if(currentObj.CheckClick(clickedPt)){
     			return currentObj;
     		}
     		else {
     			continue;
     		}
     	}
     	return null;	
     }
     
     private void increaseSpeed(){
    	double currSpeed = Global.state.playerObj.velocity;
    	double maxSpeed = Global.state.playerObj.maxVelocity;
    	Global.state.playerObj.velocity = Math.min(maxSpeed, currSpeed+(maxSpeed/30)); 
     }
     
     private void decreaseSpeed(){
    	double currSpeed = Global.state.playerObj.velocity;
    	double maxSpeed = Global.state.playerObj.maxVelocity;
    	Global.state.playerObj.velocity = Math.max(0, currSpeed-(maxSpeed/30)); 
     }
     
     //i hate retyping print lines...
     //System.out.print("Component is: " + e.getComponent().getClass().getName() + System.getProperty("line.separator"));
 }
