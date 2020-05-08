 
 package proclipsingskyscraperoptimization;
 
 import java.awt.event.*;
 import java.awt.*;
 import javax.swing.*;
 import java.io.*;
 import processing.core.PApplet;
 import proclipsingskyscraperoptimization.Skyscraper;
 import peasy.*;
 import processing.opengl.*;
 import controlP5.*;
 
 
 public class ProclipsingSkyscraperOptimization extends PApplet{
 	
 	PeasyCam cam;
 	ControlP5 controlP5;
 	//MyControlListener MyListener;
 	//PMatrix3D currCameraMatrix;
 	//PGraphics3D g3; 
 	
 	Skyscraper mySkyscraper;
 	
 	//ColumnGridLine temp,temp2;
 	ColumnGridLine[] temp = new ColumnGridLine[4];	
 		
 	DropdownList cg1, cg2; //Create variable for DropdowLists
 
 	Slider sl1;
 	Slider sl2;
 
 	int numLevels = 5;
 
 
 	int feet = 12;
 	
 	int guiWidth = 350;
 	int guiHeight = 900;
 	int[] glIndex = new int[2];
 	
 	int[] NSdIndex; 
 	int[] EWdIndex;
 	PrintWriter output;
 	
 	public void setup() {
 		size(1200,900, P3D);
 		
 		cam = new PeasyCam(this, 300*feet);
 		cam.setMinimumDistance(2*feet);
 		cam.setMaximumDistance(2000*feet);
 		
 		
 		controlP5 = new ControlP5(this);
 		//PFont p = font = loadFont("Helvetica-15.vlw");
 		//controlP5.setControlFont(p);
 		  
 		mySkyscraper = new Skyscraper(numLevels, this);
 		
 		output = createWriter("positions.txt");
 		
 		setupSliders();
 		setupGrid(); //Calling New Method;
 
 	}
 
 	public void draw() {
 		//background(MyListener.col);
 		
 		background(250);
 		prePeasy();
 		mySkyscraper.draw();
 		//output.println("NSGridLine Number,EWGridLine Letter, Level, NSGridLine Position, EWGridLine Position, Level Elevation");
 		//for(int y =0; y< numLevels; y++){
 			//Level x = (Level) mySkyscraper.myLevelStack.myLevels.get(y);
 			   //for(int i = 0; i<mySkyscraper.myColumnGrid.myNSLines.size(); i++){
 				   //for (int j = 0; j<mySkyscraper.myColumnGrid.myNSLines.size(); j++){
 					// ColumnGridLine k = (ColumnGridLine) mySkyscraper.myColumnGrid.myNSLines.get(i);
 					// ColumnGridLine l = (ColumnGridLine) mySkyscraper.myColumnGrid.myEWLines.get(j);
 			        // int u = j+65;
 					    // Writes the remaining data to the file
 					    // Finishes the file
 			        // output.println((i+1)+","+(char)u+","+y+","+k.dist+","+l.dist+","+x.elevation);
 		
 				            // }
 				   
 			            //  }
 			  // output.close();
 			   
 						//}
 		gui();
 	}
 		
 		//controlP5.setAutoDraw(false);
 	 	
 	
 
 	public void keyPressed() {
 		
 		if (key =='a'){
 		  output.flush(); // Writes the remaining data to the file
 		   for(int y =0; y< numLevels; y++){
 			Level x = (Level) mySkyscraper.myLevelStack.myLevels.get(y);
 			   for(int i = 0; i<mySkyscraper.myColumnGrid.myNSLines.size(); i++){
 				   for (int j = 0; j<mySkyscraper.myColumnGrid.myNSLines.size(); j++){
 					ColumnGridLine k = (ColumnGridLine) mySkyscraper.myColumnGrid.myNSLines.get(i);
 					ColumnGridLine l = (ColumnGridLine) mySkyscraper.myColumnGrid.myEWLines.get(j);
 			         int u = j+65;
 					    // Writes the remaining data to the file
 					    // Finishes the file
 			        output.println((i+1)+","+(char)u+","+y+","+k.dist+","+l.dist+","+x.elevation);
 			        System.out.println((i+1)+","+(char)u+","+y+","+k.dist+","+l.dist+","+x.elevation);
 		
 				            }
 				   
 			            }
 			   
 			   
 						}
 		   output.close();
 		  
 		  
 		  
 		} // Stops the program
 		
 	}
 	void prePeasy(){
 		translate(0, 0, -2500);
 	}
 
 	void gui() {
 		noSmooth();
 		//currCameraMatrix = new PMatrix3D(g3.camera);
 		camera();
 		fill(155);
 		noStroke();
 		//rect(0, 0, guiWidth, guiHeight);
 		//controlP5.setAutoDraw(false);
 		
 		controlP5.draw();
 		
 		
 		//g3.camera = currCameraMatrix;
 	}
 	
 	void setupSliders() {
 		  controlP5.addSlider("VAR 1", 0, 3, 2, 20, 50, 200, 20);
 		  controlP5.addSlider("VAR 2", 0, 3, 2, 20, 75, 200, 20);
 		  controlP5.addSlider("VAR 3", 0, 4, 3, 20, 100, 200, 20);
 		  controlP5.addSlider("VAR 4", 0, 300, 100, 20, 125, 200, 20);
 		 //new slider for moving Grid Line
 
 		 sl1 = controlP5.addSlider("EditGridLineEW")
 			            .setRange(0,500)
 		 				//.setValue(mySkyscraper.myColumnGrid.typicalGridDist*(int)cg2.getValue())
 			            .setValue(0)
 		 				.setPosition(20,300)
 		 				.setSize(500,20)
 		 				.setVisible(false);
 
 		 sl2 = controlP5.addSlider("EditGridLineNS")
 		            .setRange(0,500)
 	 				.setValue(0)
 	 				.setPosition(20,300)
 	 				.setSize(500,20)
 		 			.setVisible(false);
 
 	
 		 
 		//MyListener = new MyControlListener();
 		  
 		//controlP5.getController("Edit GridLine").addListener(MyListener);
 	
 		}
 	
 	//New Method
 	/*GUI for chosing and isolating each line of the Grid. When Grid Line is chosen
 	 * then the user will be able to edit its location by using a Slider Bar. Each Line i will be 
 	 * "Hosted" to its previous neighbor i-1. This way the Grid_Line will always have a neighbor as
 	 * reference to its current location.
 	 */
 	void setupGrid(){
 		cg1 = controlP5.addDropdownList(" Move NS GridLine")
 			           .setPosition(20,170)
 			           .setSize(100,100);
 		cg2 = controlP5.addDropdownList(" Move EW GridLine")
 				       .setPosition(130,170)
 				       .setSize(100,100);
 		
 		//MyListener = new MyControlListener();
 		//cg1.getController(" Move NS GridLine").addListener(MyListener);	
 		
 		for(int i=1; i<=mySkyscraper.myColumnGrid.myNSLines.size(); i++){
 			cg1.addItem(" Grid_Line "+i, i-1);
 		}
 		
 		for(int i=65; i<=mySkyscraper.myColumnGrid.myEWLines.size()+65; i++){
 			
 			cg2.addItem(" Grid_Line "+(char)i, i-65);
 		}
 		
 		
 		
 		//cg1.setIndex(ColumnGrid.myNSLines.size());
 		//cg2.setIndex(ColumnGrid.myEWLines.size());
 		
 	}
 	
 	//class MyControlListener implements ControlListener {
 		//int col;
 		  // public void controlEventSlider(ControlEvent theEvent2){ 
 			//System.out.println("i got an event from mySlider, " +
 			        //  "changing background color to "+
 			           // theEvent2.getController().getValue());      
 		    			//col = (int)theEvent2.getController().getValue();
 		// }
 	
 	
 	
 	public void EditGridLineEW(int theValue){
 		
 		glIndex[1] = (int) cg2.getValue();
 		temp[1] = (ColumnGridLine) mySkyscraper.myColumnGrid.myEWLines.get(glIndex[1]);
		temp[3] = (ColumnGridLine) mySkyscraper.myColumnGrid.myEWLines.get(glIndex[1]+1);
		
         temp[1].dist = theValue;
        temp[3].dist = theValue - temp[3].dist;
 		temp[1].name = Integer.toString(theValue);
 		EWdIndex = new int[mySkyscraper.myColumnGrid.myEWLines.size()];
 		EWdIndex[glIndex[1]] = temp[1].dist;
 		//EWdIndex[glIndex[1]+1]=temp[3].dist;
 		System.out.println(theValue);
 		
 		
 		
 		
 	}
 	
 	public void EditGridLineNS(int theValue){
 		glIndex[0] = (int) cg1.getValue();
 		temp[0] = (ColumnGridLine) mySkyscraper.myColumnGrid.myNSLines.get(glIndex[0]);
 		temp[0].dist = theValue;
 		temp[0].name = Integer.toString(theValue);
 		NSdIndex = new int[mySkyscraper.myColumnGrid.myNSLines.size()];
 		NSdIndex[glIndex[0]] = temp[0].dist;
 		System.out.println(theValue);
 	}
 		
 	public void controlEvent(ControlEvent theEvent) {
 		  
 		// DropdownList is of type ControlGroup.
 		  // A controlEvent will be triggered from inside the ControlGroup class.
 		  // therefore you need to check the originator of the Event with
 		  // if (theEvent.isGroup())
 		  // to avoid an error message thrown by controlP5.
 
 		  if (theEvent.isGroup()) {
 		    // check if the Event was triggered from a ControlGroup
 			  String ddlName = theEvent.getName();
 			  glIndex[0] = (int) cg1.getValue();
 			  glIndex[1] = (int) cg2.getValue();
 			  temp[0] = (ColumnGridLine) mySkyscraper.myColumnGrid.myNSLines.get(glIndex[0]);
 			  temp[1] = (ColumnGridLine) mySkyscraper.myColumnGrid.myEWLines.get(glIndex[1]);
 			  int NSdist = temp[0].dist;
 			  int EWdist = temp[1].dist; 
 			 
 			  
 			  if (ddlName == " Move NS GridLine") {
 					  sl1.setVisible(false);
 					  //if(glIndex[0]>0){
 						 // int minRangeNS = NSdIndex[glIndex[0]-1];
 					  sl2.setValue(NSdist);
 					  sl2.setRange(NSdist-250, NSdist+250);
 					  sl2.setVisible(true);
 				  	//}
 				  
 			  } else if (ddlName == " Move EW GridLine"){
 				  sl2.setVisible(false);
 				  //if(glIndex[1]>0){
 					  //int minRangeEW = EWdIndex[glIndex[1]-1];
 				  sl1.setValue(EWdist);
 				  sl1.setRange(EWdist-250, EWdist+250);
 				  sl1.setVisible(true);
 			  }
 				  
 		    println("event from group : "+theEvent.getGroup().getValue()+" from "+theEvent.getGroup()+" "+ theEvent.getName());
 		  } 
 		  else if (theEvent.isController()) {
 		    println("event from controller : "+theEvent.getController().getValue()+" from "+theEvent.getController());
 		  }
 		  
 		  
 		  //Testing Listener and controlEvent
 		  /*if(theEvent.getGroup().getValue() == 1.0 && theEvent.getGroup() == cg1){
 			  println("Permission to edit Grid_Line 2 ");
 		  }
 		  
 		  //Connecting the value of the Dropdownlist with the index of the ArrayList value
 		  //This is done by using the get() message
 		  //Having a problem with the listener still, when I chose a line from the NSLine array
 		  //it also picks up the line from the EW this is because the two dropdownlists are using the
 		  //same listener I believe.
 		  //System.out.println(ColumnGrid.myNSLines.get((int)theEvent.getGroup().getValue()));
 		  //System.out.println(ColumnGrid.myEWLines.get((int)theEvent.getGroup().getValue()));
 		  int p = (int)theEvent.getGroup().getValue();
 		  temp = (ColumnGridLine) mySkyscraper.myColumnGrid.myNSLines.get(p);
 		  System.out.println(temp.id);
 		  System.out.println(mySkyscraper.myColumnGrid.myEWLines.get((int)theEvent.getGroup().getValue()));*/
 	}
           
 
 	}
 //}
 //}
