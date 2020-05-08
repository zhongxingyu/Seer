 /*
  * Project: 		SolitudeServer
  * File: 			SolitudeServer.java
  * Created by: 		roger, Feb 8, 2012
  */
 
 //package ;
 
 import processing.core.*;
 import controlP5.*;
 import oscP5.*;
 import netP5.*;
 import java.util.Vector;
 
 /**
  * @author roger
  *
  */
 public class SolitudeServer extends PApplet {
 	
 	// A class for storing node information
 	private class Node{
 		float x,y,thick;
 	}
 	// A class for storing shape information (collection of nodes)
 	private class Shape{
 		Vector<Node> nodes = new Vector<Node>();
 	}
 	// -- TODO -- 
 	//	need to add a Player class to store a collection of shapes
 
 	static final int NUM_PLAYERS = 8;
 	static final int CONTROLP5_WIDTH = 100;
 	final int CANVAS_WIDTH = screen.width - CONTROLP5_WIDTH;
 	final int CANVAS_HEIGHT = 480;
 
 	static int LISTEN_PORT = 12000; // listen for OSC messages to this port
 	// send OSC messages to this address
 	static String HOST = "127.0.0.1";
 	static int PORT = 1234;
 	
 	
 	ControlP5 gui;
 	Textfield tfIpIn, tfPortIn;
 	Textfield tfIpOut, tfPortOut;
 	Toggle tglPlay, tglLoop;
 	Button btnReset;
 	
 	OscP5 osc;
 	NetAddress remoteLocation;
 	
 	Vector<Shape> shapes = new Vector<Shape>(NUM_PLAYERS); // a collection of shapes... should be players
 	// color info for painting shapes
 	int r = 228;
 	int g = 228;
 	int b = 228;
 	int a = 128;
 	// one color per player
 	int p1color = color(r,0,0,a);
 	int p2color = color(r,g,0,a);
 	int p3color = color(r,0,b,a);
 	int p4color = color(0,g,0,a);
 	int p5color = color(0,g,b,a);
 	int p6color = color(0,0,b,a);
 	int p7color = color(r,a,0,a);
 	int p8color = color(0,a,b,a);
 	// player color array for easy accessibility
 	int playerColors[] = {p1color, p2color, p3color, p4color, p5color, p6color, p7color, p8color};
 	
 	static int scannerX = 0;	// the scanner head
 	boolean bPlay = false;		// check if is playing to move the scanner head and send OSC messages if needed
 	boolean bLoop = false;		// loop playback?
 	
 
 	public void setup() {
 		size(CANVAS_WIDTH+CONTROLP5_WIDTH,CANVAS_HEIGHT);
 		smooth();
 		
 		osc = new OscP5(this,LISTEN_PORT); // listen for OSC messages to this port
 		remoteLocation = new NetAddress(HOST,PORT); // send OSC messages to this address
 
 		// fill the shapes vector with empty shapes
 		for (int i = 0; i < NUM_PLAYERS; i++) {
 			shapes.add(new Shape());
 		}
 		
 		// add textfields and buttons
 		setGUI();
 	}
 
 	public void update(){	
 		// if playing
 		if(bPlay){
 			// Scanner head - CAPAL
 			scannerX++;
 			// Check if there's a node at scanners position
 			for (int i = 0; i < shapes.size(); i++) {
 				// current shape
 				Shape shape = shapes.elementAt(i);
 				// player id
 				int id = i;
 				for (int j = 0; j < shape.nodes.size(); j++) {
 					
 					// if there's a node at scanners position
 					Node n1 = shape.nodes.elementAt(j);
 					if(n1.x == norm(scannerX, 0, CANVAS_WIDTH)){
 						OscMessage msg = new OscMessage("/test");
 						// add player id to OSC message
 						msg.add(id);
 						// add matching node info to OSC message
 						msg.add(n1.x);
 						msg.add(n1.y);
 						msg.add(n1.thick);
 						// if it's not the last node
 						if(j+1 < shape.nodes.size()){
 							// add next node info to OSC message
 							Node n2 = shape.nodes.elementAt(j+1);
 							msg.add(n2.x);
 							msg.add(n2.y);
 							msg.add(n2.thick);
 						}	
 						// send the OSC message
 						osc.send(msg, remoteLocation);
 						
 						println("Player "+id+" sending OSC message");
 						msg.print();
 						
 					}
 				}
 			}
 			
 			if(scannerX >= CANVAS_WIDTH){
 				if(bLoop)	scannerX = 0;
 				else		tglPlay.mousePressed();
 			}
 		}
 		
 	}
 	
 	public void draw() {
 		update();
 		
 		background(255);
 		strokeWeight(1);
 
 		// draw guides
 		stroke(255-32);
 		line(0, height/2, CANVAS_WIDTH, CANVAS_HEIGHT/2);
 		line(CANVAS_WIDTH/2, 0, CANVAS_WIDTH/2, CANVAS_HEIGHT);
 		line(CANVAS_WIDTH, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
 		
 		// draw shapes
 		drawShapes();
 
 		// Scanner head - CAPAL
 		stroke(color(255,0,0,228));
 		strokeWeight(2);
 		line(scannerX, 0, scannerX, height);
 	}
 	
 	public void drawShapes(){
 		// loop through shapes
 		for (int i = 0; i < shapes.size(); i++) {
 			// change color to player
 			fill(playerColors[i]);
 			noStroke();
 			// begin drawing a shape
 			beginShape();
 			// temp shape
 			Shape shape = shapes.elementAt(i);
 			// loop through temp shape's top nodes from left to right, and draw a vertex in each
 			for (int j = 0; j < shape.nodes.size(); j++) {
 				Node node = shape.nodes.elementAt(j);
 				float x = node.x * CANVAS_WIDTH;
 				float y = node.y * CANVAS_HEIGHT - (node.thick/2) * CANVAS_HEIGHT;
 				vertex(x, y);
 			}
 			// loop through temp shape's bottom nodes from right to left, and draw a vertex in each
 			for (int j = shape.nodes.size()-1; j >= 0; j--) {
 				Node node = shape.nodes.elementAt(j);
 				float x = node.x * CANVAS_WIDTH;
 				float y = node.y * CANVAS_HEIGHT + (node.thick/2) * CANVAS_HEIGHT;
 				vertex(x, y);
 			}
 			// close the shape
 			endShape();
 		}
 	}
 
 	public void sendOsc(){
 		
 	}
 	
 	/***
 	 * FOR OSC TESTING.  REMOVE WHEN DONE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 	 */
 	public void keyPressed(){
 		switch (key) {
 		case ' ':
 			println(shapes.size());
 			println(shapes.elementAt(0).nodes.size());
 			break;
 		case 's':
 			sendTestOsc();
 		default:
 			break;
 		}
 	}
 	public void sendTestOsc(){
 //		OscMessage myMessage = new OscMessage("/test");
 //		myMessage.add(1); // player
 //		myMessage.add(random(0,1)); // x1
 //		myMessage.add(random((float)0,(float)0.5)); // y1
 //		myMessage.add(random((float)0,(float)0.5)); // thickness 1
 //		myMessage.add(random(0,1)); // x1
 //		myMessage.add(random((float)0,(float)0.5)); // y1
 //		myMessage.add(random((float)0,(float)0.5)); // thickness 1
 //		
 //		osc.send(myMessage, remoteLocationOut);
 //		
 //		println("sendig osc through port: "+PORT);
 	}
 	/***
 	 * END TESTING
 	 */
 	
 	public void mousePressed(){
 		// set scanner head to mouse position
 		if(mouseX < CANVAS_WIDTH){
 			scannerX = mouseX;
 			if(bPlay)	tglPlay.mousePressed();
 		}
 	}
 	
 	public void mouseDragged(){
		if(mouseX < CANVAS_WIDTH)	scannerX = mouseX;
 	}
 	
 	public void oscEvent(OscMessage theOscMessage){
 		// some feedback to console
 		println("---------------------------------");
 		println("SOLITUDE SERVER\n");
 		println("### received an osc message.");
 		print(" addrpattern: "+theOscMessage.addrPattern());
 		println(" typetag: "+theOscMessage.typetag());
 		println("Storing message data...");
 
 		// to know the length of the nodes array
 		int num_args = theOscMessage.typetag().length();
 		// what player are talking about?
 		int playerID = theOscMessage.get(0).intValue();
 
 		// a temp shape
 		Shape shape = new Shape();
 		// make sure it's empty
 		shape.nodes.clear();
 		// store data from the incoming OSC message to a shape
 		for (int i = 1; i < num_args; i += 3) {
 			Node node = new Node();
 			node.x = theOscMessage.get(i).floatValue();
 			node.y = theOscMessage.get(i+1).floatValue();
 			node.thick = theOscMessage.get(i+2).floatValue();
 			shape.nodes.add(node);
 		}
 		// and put it into the vector
 		shapes.setElementAt(shape, playerID-1);
 		
 		// some more feedback to console
 		println("Done!");
 		println("---------------------------------");
 	}
 	
 	
 	/**
 	 * Called when a GUI element is triggered
 	 * @param theEvent
 	 */
 	public void controlEvent(ControlEvent theEvent){
 		String name = theEvent.controller().name();
 		println(name);
 		
 		boolean in = false;
 		boolean out = false;
 
 		if(name == "port in") {
 			LISTEN_PORT = Integer.parseInt(tfPortIn.getText());
 			in = true;
 		}
 		if(name == "ip out") {
 			HOST = tfIpOut.getText();
 			out = true;
 		}
 		if(name == "port out") {
 			PORT = Integer.parseInt(tfPortOut.getText());
 			out = true;
 		}
 
 		if(in) osc = new OscP5(this, LISTEN_PORT);
 		if(out)remoteLocation = new NetAddress(HOST, PORT);
 		
 		if(name == "play") bPlay = !bPlay;
 		
 		if(name == "loop") bLoop = !bLoop;
 		
 		if(name == "reset"){
 			scannerX = 0;
 			bPlay = false;
 		}
 	}
 
 	public void setGUI(){
 		gui = new ControlP5(this);
 		int buttonW = CONTROLP5_WIDTH-5;
 		int buttonH = 20;
 		int offset = 2;
 		
 		tfPortIn = gui.addTextfield("port in", width - buttonW, buttonH+offset+20, buttonW-10, buttonH);
 		tfPortIn.setText(Integer.toString(LISTEN_PORT));
 		tfPortIn.setAutoClear(false);
 		tfPortIn.setColorLabel(color(0));
 		tfPortIn.setColorBackground(color(228));
 		tfPortIn.setColorValueLabel(color(128));
 		tfPortIn.captionLabel().style().marginTop = -32;
 		tfPortIn.captionLabel().set("Listening to port #");
 
 		// Out IP and PORT
 		tfIpOut = gui.addTextfield("ip out", width - buttonW, (buttonH+offset)*4+10, buttonW-10, buttonH);
 		tfIpOut.setText(HOST);
 		tfIpOut.setAutoClear(false);
 		tfIpOut.setColorLabel(color(0));
 		tfIpOut.setColorBackground(color(228));
 		tfIpOut.setColorValueLabel(color(128));
 		tfIpOut.captionLabel().style().marginTop = -32;
 		
 		tfPortOut = gui.addTextfield("port out", width - buttonW, (buttonH+offset)*5+20, buttonW-10, buttonH);
 		tfPortOut.setText(Integer.toString(PORT));
 		tfPortOut.setAutoClear(false);
 		tfPortOut.setColorLabel(color(0));
 		tfPortOut.setColorBackground(color(228));
 		tfPortOut.setColorValueLabel(color(128));
 		tfPortOut.captionLabel().style().marginTop = -32;
 		
 		// Play button
 		tglPlay = gui.addToggle("play", width - buttonW, (buttonH+offset)*7+10, buttonW, buttonH);
 		tglPlay.setColorActive(color(0,128,0));
 		tglPlay.setColorBackground(color(128,0,0));
 		tglPlay.captionLabel().set("PLAY / STOP");
 		tglPlay.captionLabel().style().marginTop = -17;
 		tglPlay.captionLabel().style().marginLeft = 10;
 
 		// Loop button
 		tglLoop = gui.addToggle("loop", width - buttonW, (buttonH+offset)*8+10, buttonW, buttonH);
 		tglLoop.setColorActive(color(0,128,0));
 		tglLoop.setColorBackground(color(128,0,0));
 		tglLoop.captionLabel().style().marginTop = -17;
 		tglLoop.captionLabel().style().marginLeft = 10;
 		
 		// Reset button
 		btnReset = gui.addButton("reset",0, width - buttonW, (buttonH+offset)*9+10, buttonW, buttonH);
 		btnReset.captionLabel().style().marginLeft = 5;
 	}
 	
 	static public void main(String args[]) {
 		PApplet.main(new String[] { "--bgcolor=#FFFFFF", "SolitudeServer" });
 	}
 }
