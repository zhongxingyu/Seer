 
 
 package views;
 
 import controlP5.ControlP5;
 import processing.core.PApplet;
 import views.tabs.GuitarTab;
 import views.tabs.MidiTab;
 import views.tabs.TabNavigation;
 
 public class Test extends PApplet{
 	
 	private ControlP5 cp;
 	
 	private TabNavigation nav;
 	
 	public void setup(){
 		size(1024,768);
 		
 		cp = new ControlP5(this);
 		nav = new TabNavigation(cp);
 		nav.addTab(new MidiTab(cp));
 		nav.addTab(new GuitarTab(cp));
 		
 		
 		
 	}
 	
 	public void draw(){		
		background(unhex("BADA55"));
 		
 	}
 
 }
