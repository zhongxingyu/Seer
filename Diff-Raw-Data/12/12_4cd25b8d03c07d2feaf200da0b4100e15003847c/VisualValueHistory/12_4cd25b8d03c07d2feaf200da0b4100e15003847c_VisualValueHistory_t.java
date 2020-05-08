 package com.gamescrafters.gamesmanmobile;
 
 import java.util.LinkedList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.DashPathEffect;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 
 public class VisualValueHistory extends Activity {
 	private LinkedList<VVHNode> VVHNodes; // A LinkedList<VVHNode> containing VVHNodes, each of which is a position made in the game.
 	int middle; // The middle of the screen, or the Y-axis.
 	float maxRemoteness; // The maximum remoteness of all moves made so far.
 	int widthScreen; // The width of the screen.
 	int turnNumber; // The current turn number, beginning at 1.
 	float diff, equalizer;
 	
 	VVHView VVHview; // The view representing the external UI for this Activity.
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         VVHview = new VVHView(this);
         setContentView(VVHview);
     	Display screen = getWindowManager().getDefaultDisplay();
     	widthScreen = screen.getWidth();
     	middle = widthScreen/2;
 		diff = (float) 0.90;
 		equalizer = (float) 1-diff;
     		
 		VVHNodes = new LinkedList<VVHNode>();
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	maxRemoteness = 0;
     	
     	/* If clear_vvh is enabled, the game board has been cleared. Thus, 
     	 * we must clear the visual value history. */
     	if (GameActivity.clear_vvh) {
     		VVHNodes.clear();
     		GameActivity.clear_vvh = !GameActivity.clear_vvh;
     	} 
     	
     	/* For each move undone in GameActivity, remove a VVHNode from VVHNodes. */
     	while (GameActivity.numMovesToRemove > 0) {
     		GameActivity.numMovesToRemove--;
     		VVHNodes.removeLast();
     	}
     	
     	/* For each VVHNode in GameActivity's VVHList, add that VVHNode to VVHNodes. */
     	while (!GameActivity.VVHList.isEmpty()) {
     		VVHNode node = GameActivity.VVHList.removeFirst();
     		VVHNodes.add(node);
     	}
     	
     	/* Get the maximum remoteness for all the positions in the game so far. */
     	for (VVHNode node: VVHNodes) {
     		maxRemoteness = Math.max(maxRemoteness, node.remoteness);
     	}
     	maxRemoteness += 1; // Moved to make sure no game is "neutral" (ties should have two reflected nodes, and win/loses should not be on the y-axis).
     	turnNumber = 1; // The number of the current turn.
     }
     
     public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu); 
 		menu.add("Return to Game");
 		return true;
     }
     
     public boolean onOptionsItemSelected(MenuItem item) {
 		CharSequence title = item.getTitle();
 		if (title.equals("Return to Game")) {
 			this.startActivity(GameActivity.GameIntent);
 		}
 		return true;
     }
     
     /* Ensure that pressing the back button preserves the state of the VisualValueHistory,
      * instead of exiting the VisualValueHistory activity.
      */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event){
// 		Zach Bush: 9/2/2010
//		The following code causes the back button to loop
//		between this and GameIntent. Making it very hard
//		To change the settings or select a new game. 
//		if(keyCode == KeyEvent.KEYCODE_BACK){
//			startActivity(GameActivity.GameIntent); 
//			return true;
//		}
 		return super.onKeyDown(keyCode, event);
 	} 
     
 	/**
 	 * Given a VVHNode, convert the remoteness of the node for GUI purposes. The returned
 	 * value depends on whose turn the node presents, the value of node.remoteness and
 	 * the value of node.move.
 	 * For example, a node with isBlueTurn == true, move == "win" and remoteness == 20
 	 * means that Blue will win in 20 moves if the game is played perfectly from there on.
 	 * The returned value represents an approximate x-axis value for where the node 
 	 * should be represented on the external GUI.
 	 * 
 	 * @param node
 	 * 	A VVHNode representing a position in the game.
 	 */
     private float convertRemoteness (VVHNode node) {
     	
     	if (node.move == "gameover") {
     		if (node.isBlueTurn) {
     			return (float) ((middle*diff)+middle);
     		} else return (float) (middle*equalizer);
     	}
     	
     	if (node.move == "gameover-tie") {
     		return returnLeftTieRemoteness(node);
     	}
     
     	//TODO: fix corner nodes - they should not go off and cover the turn numbers
     	if (node.move == "win" || node.move == "tie") {		// the position is a winning node
     		if (node.isBlueTurn) {
     			return (float) ((((node.remoteness + 1) / maxRemoteness) * middle * diff) + middle*equalizer);
 		}
     		else { 
     			return (float) ((float) ((((1 - ((node.remoteness + 1) / maxRemoteness))) * middle) + middle) * diff + middle*equalizer);
 			}
     	} else {		 			 		
     		if (node.move == "lose") {		// the position is a losing node
     			if (node.isBlueTurn) {
     				return (float) ((float) ((((1 - ((node.remoteness + 1) / maxRemoteness))) * middle) + middle) * diff + middle*equalizer);
         		}
         		else {
         			return (float) ((((node.remoteness + 1) / maxRemoteness) * middle * (diff)) + middle*equalizer);
         		}
     		} 
     	}
     	return middle;
     }
     
     /**
      * Given a VVHNode, and assuming the node's move value is a tie, 
      * gives the left-aligned node representing the tied position.
      */
     private float returnLeftTieRemoteness (VVHNode node) {
     	if (node.move == "gameover-tie")
     		 return (float) (middle*equalizer);
     	else return (float) ((((node.remoteness + 1) / maxRemoteness) * middle * (diff)) + middle*equalizer);
     }
     
     /**
      * Given a VVHNode, and assuming the node's move value is a tie,
      * gives the right-aligned node representing the tied position.
      */
     private float returnRightTieRemoteness (VVHNode node) {
     	if (node.move == "gameover-tie")
 			return (float) ((middle*diff)+middle);
 		else return (float) ((float) ((((1 - ((node.remoteness + 1) / maxRemoteness))) * middle) + middle) * diff + middle*equalizer);
     }
     
     /**
      * Returns whether the node is of a tie move value.
      */
     private boolean isTie (VVHNode node) {
     	return (node.move == "tie" || node.move == "gameover-tie");
     }
     
     /**
      * A class representing the Visual Value History external representation.
      */
 class VVHView extends View {
 	Paint	line, circle, text, dotted;
 	Canvas canvas;
 	Display screen = getWindowManager().getDefaultDisplay();
 	final int width, height, mid;
 	final int DKRED = Color.parseColor("#880000");
 	
 	public VVHView(Context context){
 		super(context);
 		
 		width = screen.getWidth();
 		height = screen.getHeight();
 		mid = width/2;
 		
 		/* set paints */
 		line = new Paint();
 		line.setColor(Color.BLACK);
 		line.setStrokeWidth(3);
 		
 		circle = new Paint();
 		circle.setStrokeWidth(2);
 		
 		text = new Paint();
 		text.setTextSize(15);
 		
 		dotted = new Paint();
 		dotted.setColor(Color.DKGRAY);
 		dotted.setPathEffect(new DashPathEffect(new float[] { 5, 5, 5, 5 }, new Float(5)));
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas){
 		canvas.drawColor(Color.GRAY);
 		canvas.drawLine(mid,0,mid,height,line);
 		
 		text.setColor(Color.WHITE);
 		text.setTextAlign(Paint.Align.LEFT);
 		canvas.drawText("Blue winning", 5, 15, text);
 		text.setTextAlign(Paint.Align.RIGHT);
 		canvas.drawText("Red winning", width-5, 15, text);
 		
 		line.setColor(Color.DKGRAY);
 		line.setStrokeWidth(1);
 		text.setTextAlign(Paint.Align.CENTER);
 		text.setColor(Color.DKGRAY);
 		canvas.drawText(""+Math.round(maxRemoteness), mid, 30, text);
 		
 		VVHNode lastNode = null;
 		float x, // x-position of the point
 			numDividers; // Number of remoteness axes needed. (dynamically adjusts)
 		int y, // y-position of the point
 			r, // radius of the point; dependent on zoom
 			inc, // y-distance between 2 points; dependent on zoom
 			x2, // the end of the dotted line
 			numDividersInt; // for the second line of remoteness axes draws
 		
 
 		y = 35; // initial start of y-position
 		
 		
 		/* sets "zoom", aka dynamic resizing. */
 		if(VVHNodes.size() < 10) {
 			r = 15;
 			inc = 50;
 			numDividers = numDividersInt = 2;
 		} else if (VVHNodes.size() < 15) {
 			r = 8;
 			inc = 30;
 			numDividers = numDividersInt = 3;
 		} else if (VVHNodes.size() < 20) {
 			r = 5;
 			inc = 20;
 			numDividers = numDividersInt = 4;
 		} else {
 			r = 5;
 			inc = 13;
 			numDividers = numDividersInt = 5;
 		}
 		
 		// Draw lines and text to mark where levels of remoteness are.
 		
 		// Draw the left side of the remoteness axes.
 		for (int i = 0, j = 0; 
 				i < numDividers; i++, j += Math.round(maxRemoteness / numDividers)) {
 			canvas.drawText("" + j, (float) (((mid/numDividers*i)*diff) + middle*equalizer), 30, text);
 			canvas.drawLine((float) (((mid/numDividers*i)*diff) + middle*equalizer), 32, (float) (((mid/numDividers*i)*diff) + middle*equalizer), height, line);
 		}
 		
 		// Draw the right side of the remoteness axes.
 		for (int i = numDividersInt, j = 0; i > 0; i--, j += Math.round(maxRemoteness / numDividers)) {
 			canvas.drawText("" + j, (float) ((((mid/numDividers*i) + middle)*diff) + middle*equalizer), 30, text);
 			canvas.drawLine((float) ((((mid/numDividers*i) + middle) *diff) + middle*equalizer), 32, (float) ((((mid/numDividers*i) + middle)*diff) + middle*equalizer), height, line);
 		}
 				
 		
 		Float prevX = null; // A Float representing the previous node's converted remoteness.
 		for (VVHNode node: VVHNodes) {
 			
 			x = convertRemoteness(node);
 			if(node.isBlueTurn) {
 				// Draw a line and number indicating Blue's current position
 				x2 = 10;
 				text.setTextAlign(Paint.Align.LEFT);
 				canvas.drawText(""+turnNumber, 0, y+3, text);
 			}
 			else {
 				// Draw a line and number indicating Red's current position
 				x2 = width-10;
 				text.setTextAlign(Paint.Align.RIGHT);
 				canvas.drawText(""+turnNumber, width, y+3, text);
 			}
 			canvas.drawLine(x2, y, x, y, dotted);
 			
 			if(prevX != null) {
 				if (node.move == "gameover") circle.setColor(Color.GREEN);
 				else if (node.move == "win") circle.setColor(DKRED);
 				else if (isTie(node)) circle.setColor(Color.YELLOW);
 				else circle.setColor(Color.GREEN);
 				
 				/* Draw multiple lines if one or both of the nodes is a tie.
 				 * (Done before drawing circles so the circles can overlay the lines.)
 				 */
 				if (isTie(lastNode)) {
 					if (isTie(node)) {
 						// If both are ties, connect lines between lastNode's 2 position nodes
 						// and node's 2 position nodes.
 						canvas.drawLine(returnLeftTieRemoteness(lastNode), y-inc, 
 								returnLeftTieRemoteness(node), y, circle);
 						canvas.drawLine(returnRightTieRemoteness(lastNode), y-inc, 
 								returnRightTieRemoteness(node), y, circle);
 					} else if (lastNode.isBlueTurn) {
 						// If lastNode is Blue's position, the line should be connected
 						// to Blue's side of the board to indicate Blue's move.
 						canvas.drawLine(returnLeftTieRemoteness(lastNode), y-inc,
 								convertRemoteness(node), y, circle);
 					} else 
 						// If lastNode is Red's position, the line should be connected
 						// to Red's side of the board to indicate Red's move.
 						canvas.drawLine(returnRightTieRemoteness(lastNode), y-inc, 
 								convertRemoteness(node), y, circle);
 				} else if (isTie(node)) {
 					// If node is a tie, draw a line between each of node's position
 					// nodes on the visual value history and lastNode.
 					canvas.drawLine(convertRemoteness(lastNode), y-inc, 
 							returnLeftTieRemoteness(node), y, circle);
 					canvas.drawLine(convertRemoteness(lastNode), y-inc, 
 							returnRightTieRemoteness(node), y, circle);
 				} else // No ties anywhere, so only one line needed.
 					canvas.drawLine(prevX.intValue(), y-inc, convertRemoteness(node), y, circle);
 				
 				if (lastNode.move == "gameover") circle.setColor(DKRED);
 				else if (lastNode.move == "win") circle.setColor(Color.GREEN);
 				else if (isTie(lastNode)) circle.setColor(Color.YELLOW);
 				else circle.setColor(DKRED);
 				// Redrawing of the lastNode circle to cover up part of the connecting lines.
 				canvas.drawCircle(prevX.intValue(), y-inc, r, circle);
 				if (isTie(lastNode)) { // If lastNode is a tie, two circles (the position nodes) must be redrawn.
 					canvas.drawCircle(returnLeftTieRemoteness(lastNode), y-inc,r, circle);
 					canvas.drawCircle(returnRightTieRemoteness(lastNode), y-inc,r, circle);
 				}
 			}
 			
 			if (node.move == "gameover") circle.setColor(DKRED);
 			else if (node.move == "win") circle.setColor(Color.GREEN);
 			else if (isTie(node)) circle.setColor(Color.YELLOW);
 			else circle.setColor(DKRED);  
 			// Drawing of node's position on the visual value history.
 			canvas.drawCircle(x, y, r, circle);
 			
 			if (isTie(node)) { // If node is a tie, two circles must be drawn for both tie positions.
 				canvas.drawCircle(returnLeftTieRemoteness(node), y, r, circle);
 				canvas.drawCircle(returnRightTieRemoteness(node), y, r, circle);
 			}
 			
 			y += inc;
 			prevX = new Float(x);
 			lastNode = node;
 			turnNumber++;
 		}
 	}
 	
 }
 }
