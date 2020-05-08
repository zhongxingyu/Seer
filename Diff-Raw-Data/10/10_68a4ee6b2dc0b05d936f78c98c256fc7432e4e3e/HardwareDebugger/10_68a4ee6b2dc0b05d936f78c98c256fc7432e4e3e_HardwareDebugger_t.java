 package propinquity.hardware;
 
 import processing.core.*;
 import controlP5.*;
 
 /**
  * A hacky sketch to test patches or gloves, provded buttons and sliders to control active, vibe and LEDS as well as giving prox value back.
  *
  */
 public class HardwareDebugger extends PApplet implements ProxEventListener, AccelEventListener {
 
 	// Unique serialization ID
 	static final long serialVersionUID = 6340508174717159418L;
 
 	static final int[] PATCH_ADDR = new int[] { 3 };
 	static final int NUM_PATCHES = PATCH_ADDR.length;
 
 	static final int[] GLOVE_ADDR = new int[] {};
 	static final int NUM_GLOVES = GLOVE_ADDR.length;
 
 	XBeeBaseStation xbeeBaseStation;
 
 	Patch[] patches;
 	Glove[] gloves;
 
 	ControlP5 controlP5;
 	boolean show_controls = true;
 
 	Slider prox_sliders[];
 	Slider x_sliders[];
 	Slider y_sliders[];
 	Slider z_sliders[];
 
 	public void setup() {
 		size(1024, 800);
 
 		controlP5 = new ControlP5(this);
 
 		prox_sliders = new Slider[NUM_PATCHES];
 		x_sliders = new Slider[NUM_PATCHES];
 		y_sliders = new Slider[NUM_PATCHES];
 		z_sliders = new Slider[NUM_PATCHES];
 
 		controlP5.addButton("Re-Scan", 1, 10, 10, 50, 25);
 
 		for(int i = 0;i < NUM_PATCHES;i++) {
 			int x_offset = (width-100)/NUM_PATCHES*i+50;
 			int y_offset = 60;
 			int local_width = round((width-100)/NUM_PATCHES*0.95f);
 
 			int obj_width = 15;
 			int slider_height = 150;
 
 			int level_0 = -45;
 			int level_1 = 10;
 			int level_2 = level_1+(slider_height+20);
 			int level_3 = level_1+(slider_height+20)*2;
 			int level_4 = level_1+(slider_height+20)*3;
 
 			int num = 3;
 
 			int incr_offset = 0;
 			int incr_width = (local_width-incr_offset*2)/num;
 			int obj_offset = incr_offset+(incr_width-obj_width)/2;
 
 			ControlGroup group = controlP5.addGroup("Patch "+i, x_offset, y_offset, local_width);
 
 			Toggle toggle = controlP5.addToggle("Active "+i, incr_width*0+obj_offset, level_0, obj_width, obj_width);
 			toggle.setGroup(group);
 
 			Slider r_slider = controlP5.addSlider("Red "+i, 0, 255, 0, incr_width*0+obj_offset, level_1, obj_width, slider_height);
 			r_slider.setGroup(group);
 			Slider g_slider = controlP5.addSlider("Green "+i, 0, 255, 0, incr_width*1+obj_offset, level_1, obj_width, slider_height);
 			g_slider.setGroup(group);
 			Slider b_slider = controlP5.addSlider("Blue "+i, 0, 255, 0, incr_width*2+obj_offset, level_1, obj_width, slider_height);
 			b_slider.setGroup(group);
 
 			Slider duty_slider = controlP5.addSlider("Color Duty "+i, 0, 255, 0, incr_width*0+obj_offset, level_2, obj_width, slider_height);
 			duty_slider.setGroup(group);
 			Slider period_slider = controlP5.addSlider("Color Period "+i, 0, 255, 0, incr_width*1+obj_offset, level_2, obj_width, slider_height);
 			period_slider.setGroup(group);
 
 			prox_sliders[i] = controlP5.addSlider("Prox "+i, 0, 1024, 0, incr_width*2+obj_offset, level_2, obj_width, slider_height);
 			prox_sliders[i].lock();
 			prox_sliders[i].setGroup(group);
 			
 			Slider vibe_slider = controlP5.addSlider("Vibe Level "+i, 0, 255, 0, incr_width*0+obj_offset, level_3, obj_width, slider_height);
 			vibe_slider.setGroup(group);
 			Slider vibe_duties_slider = controlP5.addSlider("Vibe Duty "+i, 0, 255, 0, incr_width*1+obj_offset, level_3, obj_width, slider_height);
 			vibe_duties_slider.setGroup(group);
 			Slider vibe_periods_slider = controlP5.addSlider("Vibe Period "+i, 0, 255, 0, incr_width*2+obj_offset, level_3, obj_width, slider_height);
 			vibe_periods_slider.setGroup(group);
 
			x_sliders[i] = controlP5.addSlider("X "+i, -128, 128, 0, incr_width*0+obj_offset, level_4, obj_width, slider_height);
 			x_sliders[i].lock();
 			x_sliders[i].setGroup(group);
 
			y_sliders[i] = controlP5.addSlider("Y "+i, -128, 128, 0, incr_width*1+obj_offset, level_4, obj_width, slider_height);
 			y_sliders[i].lock();
 			y_sliders[i].setGroup(group);
 
			z_sliders[i] = controlP5.addSlider("Z "+i, -128, 128, 0, incr_width*2+obj_offset, level_4, obj_width, slider_height);
 			z_sliders[i].lock();
 			z_sliders[i].setGroup(group);
 		}
 
 		// for(int i = 0;i < NUM_GLOVES;i++) {
 		// 	int x_offset = (width-100)/NUM_GLOVES*i+50;
 		// 	int y_offset = 60;
 		// 	int local_width = round((width-100)/NUM_GLOVES*0.95f);
 
 		// 	int obj_width = 15;
 		// 	int slider_height = 200;
 
 		// 	int level_0 = -45;
 		// 	int level_1 = 10;
 		// 	// int level_2 = 240;
 		// 	// int level_3 = 480;
 
 		// 	int num = 3;
 
 		// 	int incr_offset = 0;
 		// 	int incr_width = (local_width-incr_offset*2)/num;
 		// 	int obj_offset = incr_offset+(incr_width-obj_width)/2;
 
 		// 	ControlGroup group = controlP5.addGroup("Glove "+i, x_offset, y_offset, local_width);
 
 		// 	Toggle toggle = controlP5.addToggle("Active "+i, incr_width*0+obj_offset, level_0, obj_width, obj_width);
 		// 	toggle.setGroup(group);
 						
 		// 	Slider vibe_slider = controlP5.addSlider("Vibe Level "+i, 0, 255, 0, incr_width*0+obj_offset, level_1, obj_width, slider_height);
 		// 	vibe_slider.setGroup(group);
 		// 	Slider vibe_duties_slider = controlP5.addSlider("Vibe Duty "+i, 0, 255, 0, incr_width*1+obj_offset, level_1, obj_width, slider_height);
 		// 	vibe_duties_slider.setGroup(group);
 		// 	Slider vibe_periods_slider = controlP5.addSlider("Vibe Period "+i, 0, 255, 0, incr_width*2+obj_offset, level_1, obj_width, slider_height);
 		// 	vibe_periods_slider.setGroup(group);
 		// }
 
 		if(!show_controls) controlP5.hide();
 
 		xbeeBaseStation = new XBeeBaseStation();
 		xbeeBaseStation.scan();
 		xbeeBaseStation.addProxEventListener(this);
		xbeeBaseStation.addAccelEventListener(this);
 
 		patches = new Patch[NUM_PATCHES];
 		for(int i = 0;i < NUM_PATCHES;i++) {
 			patches[i] = new Patch(PATCH_ADDR[i], xbeeBaseStation);
 			xbeeBaseStation.addPatch(patches[i]);
 		}
 
 		// gloves = new Glove[NUM_GLOVES];
 		// for(int i = 0;i < NUM_GLOVES;i++) {
 		// 	gloves[i] = new Glove(GLOVE_ADDR[i], xbeeBaseStation);
 		// 	xbeeBaseStation.addGlove(gloves[i]);
 		// }
 	}
 
 	public void controlEvent(ControlEvent theEvent) {
 		String name = theEvent.controller().name();
 		int value = (int)theEvent.controller().value();
 		if(name.indexOf("Active") == -1 && value < 10) value = 0; //Snap to zero
 		if(name.equals("Re-Scan")) {
 			xbeeBaseStation.scan();
 			return;
 		} else {
 			for(int i = 0;i < NUM_PATCHES;i++) {
 				if(name.equals("Active "+i)) {
 					if(value != 0) patches[i].setActive(true);
 					else patches[i].setActive(false);
 					return;
 				} else if(name.equals("Red "+i)) {
 					int[] current_color = patches[i].getColor();
 					patches[i].setColor(value, current_color[1], current_color[2]);
 					return;
 				} else if(name.equals("Green "+i)) {
 					int[] current_color = patches[i].getColor();
 					patches[i].setColor(current_color[0], value, current_color[2]);
 					return;
 				} else if(name.equals("Blue "+i)) {
 					int[] current_color = patches[i].getColor();
 					patches[i].setColor(current_color[0], current_color[1], value);
 					return;
 				} else if(name.equals("Color Duty "+i)) {
 					patches[i].setColorDuty(value);
 					return;
 				} else if(name.equals("Color Period "+i)) {
 					patches[i].setColorPeriod(value);
 					return;
 				} else if(name.equals("Vibe Level "+i)) {
 					patches[i].setVibeLevel(value);
 					return;
 				} else if(name.equals("Vibe Duty "+i)) {
 					patches[i].setVibeDuty(value);
 					return;
 				} else if(name.equals("Vibe Period "+i)) {
 					patches[i].setVibePeriod(value);
 					return;
 				}
 			}
 		}
 	}
 
 	public void draw() {
 		if(xbeeBaseStation.isScanning()) background(30, 0, 0);
 		else background(0);
 
 		if(xbeeBaseStation.listXBees() != null && xbeeBaseStation.listXBees().length > 0) {
 			stroke(150);
 			fill(0, 75, 0);
 		} else {
 			stroke(150);
 			fill(75, 0, 0);
 		}
 
 		rect(5, height-30, 25, 25);
 	}
 	
 	public void keyPressed() {
 		if(key == 's') {
 			xbeeBaseStation.scan();
 		} else if(key == 'h') {
 			show_controls = !show_controls;
 			if(!show_controls) controlP5.hide();
 			else controlP5.show();
 		}
 	}
 
 	public void proxEvent(Patch patch) {
 		for(int i = 0;i < NUM_PATCHES;i++) {
 			if(patch == patches[i]) {
 				prox_sliders[i].setValue(patch.getProx());
 			}
 		}
 	}
 
 	public void accelXYZEvent(Patch patch) {
 		for(int i = 0;i < NUM_PATCHES;i++) {
 			if(patch == patches[i]) {
 				x_sliders[i].setValue(patch.getAccelX());
 				y_sliders[i].setValue(patch.getAccelY());
 				z_sliders[i].setValue(patch.getAccelZ());
 			}
 		}
 	}
 
 	static public void main(String args[]) {
 		PApplet.main(new String[] { "propinquity.hardware.HardwareDebugger" });
 	}
 
 }
