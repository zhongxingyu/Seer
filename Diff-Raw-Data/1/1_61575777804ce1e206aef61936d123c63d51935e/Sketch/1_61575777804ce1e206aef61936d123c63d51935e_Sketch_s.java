 package visualizer;
 
 import java.awt.Color;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.io.File;
 import java.nio.ByteBuffer;
 
 import processing.core.*;
 import traer.physics.*;
 import themidibus.*;
 
 @SuppressWarnings({ "unused", "serial" })
 public class Sketch extends PApplet {
 	// Sketch variables
 
 	float MIN_MASS = 0.4f; // the minimum mass of a particle
 	float MAX_MASS = 0.8f; // the maximum mass of a particle
 
 	int W;
 	int H;
 
 	int NTH_PARTICLE = 7;
 	int[] PARTICLE_COLOR;
 	boolean TRAIL = false;
 	int TR_LEN = 90;
 	boolean RAINBOW = false;
 	boolean MOUSE = true;
 	boolean DGRAV, UGRAV, LGRAV, RGRAV = false;
 	boolean FORCEVISIBLE = true;
 	boolean FULLSCREEN = true;
 	boolean FADEOUT = false;
 	boolean FADEIN = false;
 	String NEXTIMG;
 
     int PALPH = 255;
     int WALPH = 0;
     int TIME, TIME2;
     boolean WAVE = false;
     boolean SPRNG = true;
     float WEIGHT = 5;
     
 	float WSTRENGTH = -5000;
 	
 	Particle[] attrParticles;
 	int attrPartNum = 4;
     boolean ATTR = true;
 	float[][] ATTRV;
 	Force[] myAttractions;
 	Force[] attrForces;
 	Force[] mouseForces;
 
 	Particle mouse; // particle on mouse position
 	Particle[] particles; // the moving particle
 	Particle[] orgParticles; // original particles - fixed
 	Spring[] springs;
 	int[][] Colors; // Color values from the image
 	ParticleSystem physics; // the particle system
 	float[][] locations; // 2d array of particle locations
 	String[] input;
 	int LENGTH;
 
 	public boolean sketchFullScreen() {
 		  return true;
 		}
 	
 	public void controllerChange(int channel, int number, int value){
 		println("Channel: " + channel);
 		println("Number: "+number);
 		println("Value: "+value);
 		
 		// Sliders
 		if (channel == 0)
 			switch (number) {
 				case 1: NTH_PARTICLE = value;
 					break;
 				case 2: WEIGHT = value;
 					break;
 				case 3: TR_LEN = value*5;
 					break;
 			}
 			
 		// Toggles
 		else if (channel == 1)
 			switch (number) {
 				case 10: TRAIL = !TRAIL;
 					break;
 				case 12: RAINBOW = !RAINBOW;
 					break;
 				case 11: toggleSprings();
 					break;
 				case 13: WAVE = !WAVE;
 					break;
 				case 14: toggleForceParticles();
 					break;
 				case 18: toggleMouse();
 					break;
 				case 19: toggleForceVisible();
 					break;
 			}
 		
 		// Image Select
 		else if (channel == 2 && value == 1)
 			switch (number) {
 				case 100: transitionImg("0");
 					break;
 				case 101: transitionImg("1");
 					break;
 				case 102: transitionImg("2");
 					break;
 				case 103: transitionImg("3");
 					break;
 				case 104: transitionImg("4");
 					break;
 				case 105: transitionImg("5");
 					break;
 				case 106: transitionImg("6");
 					break;
 			}
 		
 		else if (channel == 3 && value == 1)
 			switch(number) {
 			case 3: toggleDownwardGravity();
 				break;
 			case 1: toggleUpwardGravity();
 				break;
 			case 4: toggleLeftGravity();
 			 	break;
 			case 2: toggleRightGravity();
 				break;
 			case 5: toggleNoGravity();
 				break;	
 			case 6: toggleULGravity();
 				break;
 			case 7: toggleURGravity();
 				break;
 			case 8: toggleDRGravity();
 			 	break;
 			case 9: toggleDLGravity();
 				break;
 			
 			
 			}
 		
 		else if (channel == 4 && value == 1)
 			if (number == 1)
 				exit();
 		 
 	}
 	
 	public void toggleForceParticles(){
 		if (ATTR) {
 			ATTR = false;
 		    for (int i=0; i<LENGTH*attrPartNum; i++) {
 		    	myAttractions[i].turnOff();
 		    }
 		    for (int i=0; i<attrPartNum; i++) {
 		    	attrForces[i].turnOff();
 		    }
 		}
 		else {
 			ATTR = true;
 			for (int i=0; i<LENGTH*attrPartNum; i++) {
 		    	myAttractions[i].turnOn();
 		    }
 		    for (int i=0; i<attrPartNum; i++) {
 		    	attrForces[i].turnOn();
 		    }
 		}
 	}
 	
 	public void toggleFullScreen(){
 		if (FULLSCREEN) {
 			frame.setSize(1920, 1080);
 		}
 		else {
 			sketchFullScreen();
 		}
 		FULLSCREEN = !FULLSCREEN;
 	}
 	
 	public void toggleForceVisible(){
 		if (FORCEVISIBLE == true){
 			FORCEVISIBLE = false;
 		}
 		else{
 			FORCEVISIBLE = true;
 		}
 	}
 	
 	public void toggleNoGravity(){
 		physics.setGravity(0);
 	}
 	
 	public void toggleUpwardGravity(){
 		physics.setGravity(-2);
 	}
 	
 	public void toggleDownwardGravity(){
 		physics.setGravity(2);
 
 	}
 	
 	public void toggleLeftGravity(){
 		physics.setGravity(-2, 0, 0);
 	}
 	
 	public void toggleRightGravity(){
 		physics.setGravity(2, 0, 0);
 	}
 	
 	public void toggleULGravity(){
 		physics.setGravity(-2, -2, 0);
 	}
 	
 	public void toggleDLGravity(){
 		physics.setGravity(-2, 2, 0);
 
 	}
 	
 	public void toggleURGravity(){
 		physics.setGravity(2, -2, 0);
 	}
 	
 	public void toggleDRGravity(){
 		physics.setGravity(2, 2, 0);
 	}
 	
 	
 	public void toggleSprings() {
 		if (SPRNG) {
 			SPRNG = false;
 		    for (int i=0; i<LENGTH; i++) {
 		    	springs[i].turnOff();
 		    }
 		}
 		else {
 			SPRNG = true;
 			for (int i=0; i<LENGTH; i++) {
 		    	springs[i].turnOn();
 		    }
 		}	
 	}
 	
 	public void toggleMouse() {
 		if (MOUSE) {
 			MOUSE = false;
 		    for (int i=0; i<LENGTH; i++) {
 		    	mouseForces[i].turnOff();
 		    }
 		}
 		else {
 			MOUSE = true;
 			for (int i=0; i<LENGTH; i++) {
 		    	mouseForces[i].turnOn();
 		    }
 		}	
 	}
 	
 	public void setImage(String extName){	
 
 		int[] res;
 		int temp, temp2;
 		input = loadStrings("particles" + extName + ".txt");
 		LENGTH = input.length;
 		locations = new float[2][LENGTH];
 		Colors = new int[LENGTH][3];
 		res = readInput(input);
 		W = Toolkit.getDefaultToolkit().getScreenSize().width;
 		H = Toolkit.getDefaultToolkit().getScreenSize().height;
 		//W = 1080; //res[0];
 		//H = 720; //res[1];
 		TIME = 0;
 		TIME2 = W / 2;
 		
 		// Particle System + Detect Colors
 		physics = new ParticleSystem(0, 0.05f);
 		physics.setIntegrator(ParticleSystem.MODIFIED_EULER);
 		mouse = physics.makeParticle(); // create a particle for the mouse
 		mouse.setMass(5f);
 		mouse.makeFixed(); // don't let forces move it
 		
 		// initialize the force particles
 		attrParticles = new Particle[attrPartNum];
 		ATTRV = new float[2][attrPartNum];
 		for (int i = 0; i < attrPartNum; i++) {
 			ATTRV[0][i] = random(5,15);
 			ATTRV[1][i] = random(5,15);
 			attrParticles[i] = physics.makeParticle(5, random(1,W-1), random(1,H-1), 0);
 			attrParticles[i].velocity().set(ATTRV[0][i],ATTRV[1][i],0f);
 		}
 		
 		attrForces = new Force[attrPartNum];
 		// create forces between force particles
 		for (int i = 0; i < attrPartNum; i++) {
 			for (int j = 0; j < attrPartNum; j++) {
 				if (!(i == j)) {
 					attrForces[i] = physics.makeAttraction(attrParticles[i], attrParticles[j], -7000, 0.1f);
 				}
 			}
 		}
 		myAttractions = new MyAttraction[LENGTH * attrPartNum];
 		particles = new Particle[LENGTH];
 		orgParticles = new Particle[LENGTH];
 		springs = new Spring[LENGTH];
 		mouseForces = new Force[LENGTH];
 
 		// Makes the visible and anchor particles, the forces surrounding them and the force particles
 		for (int i = 0; i < LENGTH; i++) {
 			particles[i] = physics.makeParticle(random(MIN_MASS, MAX_MASS),
 					locations[0][i], locations[1][i], 0);
 			orgParticles[i] = physics.makeParticle(random(MIN_MASS, MAX_MASS),
 					locations[0][i], locations[1][i], 0);
 			orgParticles[i].makeFixed();
 			// make the moving particles go to their former positions (creates
 			// the springs)
 			springs[i] = physics.makeSpring(particles[i], orgParticles[i], 0.007f, 0.1f, 0);
 			// make the moving particles move away from the mouse
 			mouseForces[i] = physics.makeAttraction(particles[i], mouse, -6000f, 0.1f);
 			// make the forces between the force particles and the springy particles
 			for (int j = 0; j < attrPartNum; j++) {
 				physics.addCustomForce(new MyAttraction(particles[i], attrParticles[j], WSTRENGTH, 0.1f));
 				temp = i * attrPartNum + j;
 				myAttractions[temp] = physics.getCustomForce(temp);
 				
 			}
 		}
 		fill(0, 255);
 		rect(0, 0, W, H);
 		// Processing Setup
 		size(W, H);
 		noStroke();
 		ellipseMode(CENTER);
 		smooth();
 		
 		// Prevent ghost force particles in the beginning.
 		ATTR = true;
 		toggleForceParticles();
 		
 		// Mouse always starts on.
 		MOUSE = true;
 	}
 	
 	public void transitionImg(String extension){
 		FADEOUT = true;
 		NEXTIMG = extension;
 	}
 	
 	public void setup() {
 		
 		frame.setBackground(new Color(0,0,0));
 		size(displayWidth, displayHeight);
 			
 		// GRABS THE LOCATIONS OF PARTICLES FROM THE EDGE-DETECTED PICTURE
 		setImage("0");
 		// precautionary measure to make sure there's no ghost force particles at setup
 		frameRate(60);
 		MidiBus.list();
 		MidiBus myBus = new MidiBus(this, 0, 0);
 		
 	}
 
 	// @SuppressWarnings("deprecation")
 	public void draw() {
 		// background(0);
 		// Causes particle trails
 		noStroke();
 		if (TRAIL == true) {
 			fill(0, TR_LEN);
 		} else {
 			fill(0, 255);
 		}
 		rect(0, 0, W, H);
 
 		//println("framerate: " + frameRate);
 		//println("NTH_PARTICLE: " + NTH_PARTICLE);
 
 		mouse.position().set(mouseX, mouseY, 0);
 		PARTICLE_COLOR = rainbowColor(mouseX);
 		physics.tick();
 		
 		if (ATTR) {
 			for (int i = 0; i < attrPartNum; i++) {
 				// combat particle drag for the force particles
 				attrParticles[i].velocity().set(ATTRV[0][i],ATTRV[1][i],0);
 				// prevent force particles from flying off the screen
 				if (attrParticles[i].position().x() < 0) {
 					ATTRV[0][i] = -1 * ATTRV[0][i];
 					attrParticles[i].position().set(0,attrParticles[i].position().y(),0);
 					attrParticles[i].velocity().set(ATTRV[0][i],0,0);
 				}
 				if (attrParticles[i].position().x() > W) {
 					ATTRV[0][i] = -1 * ATTRV[0][i];
 					attrParticles[i].position().set(W,attrParticles[i].position().y(),0);
 					attrParticles[i].velocity().set(ATTRV[0][i],0,0);
 				}
 				if (attrParticles[i].position().y() < 0) {
 					ATTRV[1][i] = -1 * ATTRV[1][i];
 					attrParticles[i].position().set(attrParticles[i].position().x(),0,0);
 					attrParticles[i].velocity().set(0,ATTRV[1][i],0);
 				}
 				if (attrParticles[i].position().y() > H) {
 					ATTRV[1][i] = -1 * ATTRV[1][i];
 					attrParticles[i].position().set(attrParticles[i].position().x(),H,0);
 					attrParticles[i].velocity().set(0,ATTRV[1][i],0);
 				}
 				// display the force particle
 				if (FORCEVISIBLE)
 					fill(255, 255, 255, 255);
 				
 				ellipse(attrParticles[i].position().x(),attrParticles[i].position().y(),5,5);
 			}
 		}
 		float posx, posy;
 		for (int i = 0; i < LENGTH; i++) {
 			posx = particles[i].position().x();
 			posy = particles[i].position().y();
 			if (i % NTH_PARTICLE == 0) {
 				if (!SPRNG) {
 					// particles are bouncing around without springs
 					if (posx < 0) {
 						particles[i].position().set(0,posy,0);
 						particles[i].velocity().add(-2 * particles[i].velocity().x(),0,0);
 					}
 					if (posx > W) {
 						particles[i].position().set(W,posy,0);
 						particles[i].velocity().add(-2 * particles[i].velocity().x(),0,0);
 					}
 					if (posy < 0) {
 						particles[i].position().set(posx,0,0);
 						particles[i].velocity().add(0,-2 * particles[i].velocity().y(),0);
 					}
 					if (posy > H) {
 						particles[i].position().set(posx,H,0);
 						particles[i].velocity().add(0,-2 * particles[i].velocity().y(),0);
 					}
 				}
 				if (!WAVE) {
 					// We're displaying the image particles and perhaps fading
 					// out the wave particles
 					if (RAINBOW) {
 						// rainbow fill for image particle
 						// fill(rainbowColor(mouseX),PALPH);
 						// test, rainbow based on x location of particle
 						PARTICLE_COLOR = rainbowColor(posx);
 						fill(PARTICLE_COLOR[0], PARTICLE_COLOR[1], PARTICLE_COLOR[2], PALPH);
 					}
 					// image particle fill
 					else {
 						// else we're using colors from input
 						fill(Colors[i][0], Colors[i][1], Colors[i][2], PALPH);
 					}
 					ellipse(posx, posy, WEIGHT, WEIGHT);
 					if (WALPH > 0) {
 						if (RAINBOW) {
 							// rainbow fill for wave particles
 							// fill(rainbowColor(mouseX),WALPH);
 							PARTICLE_COLOR = rainbowColor((float) i / LENGTH * W);
 							fill(PARTICLE_COLOR[0], PARTICLE_COLOR[1], PARTICLE_COLOR[2], WALPH);
 						} else {
 							// else we're using colors from input
 							fill(Colors[i][0], Colors[i][1], Colors[i][2], WALPH);
 						}
 						ellipse(TIME, waveLocation(TIME, i), WEIGHT, WEIGHT);
 						// second set of wave particles
 						ellipse(TIME2 - 25, waveLocation(TIME2, i), WEIGHT,
 								WEIGHT);
 					}
 				} else {
 					// We're displaying the wave particles and perhaps fading
 					// out the image particles
 					if (RAINBOW) {
 						// rainbow fill for image particle
 						// fill(rainbowColor(mouseX),WALPH);
 						// test, rainbow based on x location of particle
 						PARTICLE_COLOR = rainbowColor((float) i / LENGTH * W);
 						fill(PARTICLE_COLOR[0], PARTICLE_COLOR[1], PARTICLE_COLOR[2], WALPH);
 					}
 					// image particle fill
 					else {
 						// else we're using colors from input
 						fill(Colors[i][0], Colors[i][1], Colors[i][2], WALPH);
 					}
 					ellipse(TIME, waveLocation(TIME, i), WEIGHT, WEIGHT);
 					// second set of wave particles
 					ellipse(TIME2 - 25, waveLocation(TIME2, i), WEIGHT, WEIGHT);
 					if (PALPH > 0) {
 						if (RAINBOW) {
 							// rainbow fill for wave particles
 							// fill(rainbowColor(mouseX),PALPH);
 							PARTICLE_COLOR = rainbowColor(posx);
 							fill(PARTICLE_COLOR[0], PARTICLE_COLOR[1], PARTICLE_COLOR[2], PALPH);
 						} else {
 							// else we're using colors from input
 							fill(Colors[i][0], Colors[i][1], Colors[i][2], PALPH);
 						}
 						ellipse(posx, posy, WEIGHT, WEIGHT);
 					}
 				}
 			}
 		}
 		
 		if (!FADEOUT && !FADEIN){
 			if (!WAVE) {
 				if (PALPH < 255) {
 					PALPH += 5;
 				}
 				if (WALPH > 0) {
 					WALPH -= 5;
 				}
 			} else {
 				if (WALPH < 255) {
 					WALPH += 5;
 				}
 				if (PALPH > 0) {
 					PALPH -= 5;
 				}
 			}
 		}
 		if (TIME > W) {
 			TIME = 0;
 		} else {
 			TIME += 1;
 		}
 		if (TIME2 > W) {
 			TIME2 = 0;
 		} else {
 			TIME2 += 1;
 		}
 		
 		if (FADEOUT) {
 			FADEIN = false;
 			PALPH -= 5;
 			if (PALPH == 0) {
 				FADEOUT = false;
 				setImage(NEXTIMG);
 				FADEIN = true;
 			}
 		}
 		if (FADEIN){
 			FADEOUT = false;
 			PALPH += 5;
 			if (PALPH == 255) {
 				FADEIN = false;
 			}
 		}
 	}
 
 	public void keyPressed() {
 
 		// If the user presses up on the keyboard, fewer particles will be drawn
 		// and vice versa
 		if (key == CODED) {
 			if (keyCode == DOWN && NTH_PARTICLE < 25) {
 				// arbitrary cap on how much the particles are decreased by
 				NTH_PARTICLE++;
 			} else if (keyCode == UP && NTH_PARTICLE > 1) {
 				// avoiding math issues
 				NTH_PARTICLE--;
 			}
 		}
 		
 		// turn force particles on/off
 		if (key == 'f') toggleForceParticles();
 
 		// turn downward gravity on/off
 		if (key == 'g') toggleDownwardGravity();
 		
 		// turn upward gravity on/off
 		if (key == 'h') toggleUpwardGravity();
 		
 		// turn sideways gravity on/off, to the left
 		if (key == 'j') toggleLeftGravity();
 		
 		// turn sideways gravity on/off, to the right
 		if (key == 'k') toggleRightGravity();
 		
 		// turn off gravity
 		if (key == 'l') physics.setGravity(0);
 		
 		// change particle size
 		if (key == '[' && WEIGHT > 1)
 			WEIGHT--;
 		
 		if (key == ']' && WEIGHT < 20)
 			WEIGHT++;
 		
 
 		// turn trails on or off
 		if (key == 't') TRAIL = !TRAIL;
 		
 		// turn rainbow mode on or off
 		if (key == '/') RAINBOW = !RAINBOW;
 
 		// increase or decrease trail length
 		if (key == '=' && TR_LEN > 4) {
 			TR_LEN -= 5;
 		}
 		if (key == '-' && TR_LEN < 251) {
 			TR_LEN += 5;
 		}
 
 		// turn on waves/turn off image particles
 		if (key == 'w') WAVE = !WAVE;
 
 		// toggle fullscreen
 		if (key == 'a') toggleFullScreen();
 		
 		if (key == 'i') toggleForceVisible();
 		
 		if (key == '1'){
 			FADEOUT = true;
 			NEXTIMG = "0";
 			//setImage(NEXTIMG);
 		}
 		if (key == '2'){
 			FADEOUT = true;
 			NEXTIMG = "1";
 			//setImage(NEXTIMG);
 		}		
 		if (key == '3'){
 			FADEOUT = true;
 			NEXTIMG = "2";
 			//setImage(NEXTIMG);
 		}
 		if (key == '4'){
 			FADEOUT = true;
 			NEXTIMG = "3";
 			//setImage(NEXTIMG);
 		}	
 		if (key == '5'){
 			FADEOUT = true;
 			NEXTIMG = "4";
 			//setImage(NEXTIMG);
 		}
 		if (key == '6'){
 			FADEOUT = true;
 			NEXTIMG = "5";
 			//setImage(NEXTIMG);
 		}	
 		if (key == '7'){
 			FADEOUT = true;
 			NEXTIMG = "6";
 			//setImage(NEXTIMG);
 		}	
 
 		// turn the springs on or off, if springs are off the particles will bounce off the walls
 		if (key == 'p') toggleSprings();	
 		
 		// turn mouse forces on or off
 		if (key == 'm') toggleMouse();
 	}
 
 	float distance(float x1, float y1, float x2, float y2) {
 		return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
 	}
 
 	float waveLocation(int t, int num) {
 		// uses a sin function to calculate position of particles, returns y
 		return sin((float) t / 70) * noise(num) * 150 + ((float) num / LENGTH)
 				* H;
 	}
 
 	// changes the Color of the particles based on mouse location, follows a
 	// rainbow pattern
 	int[] rainbowColor(float mouseX) {
 		int r;
 		int g;
 		int b;
 		// sector 1
 		if (mouseX < W / 6) {
 			g = (int) (mouseX / (W / 6) * 127);
 			return new int[] { 255, g, 0 };
 
 		}
 		// sector 2
 		else if (mouseX >= W / 6 && mouseX < W / 3) {
 			g = 128 + (int) (mouseX - (W / 6)) / (W / 6) * 127;
 			return new int[] { 255, g, 0 };
 		}
 		// sector 3
 		else if (mouseX >= W / 3 && mouseX < W / 2) {
 			r = 255 - (int) (mouseX - (W / 3)) / (W / 6) * 255;
 			return new int[] { r, 255, 0 };
 		}
 		// sector 4
 		else if (mouseX >= W / 2 && mouseX < 2 * W / 3) {
 			g = 255 - (int) (mouseX - (W / 2)) / (W / 6) * 255;
 			b = (int) (mouseX - (W / 2)) / (W / 6) * 255;
 			return new int[] { 0, g, b };
 		}
 		// sector 5
 		else if (mouseX >= 2 * W / 3 && mouseX < 5 * W / 6) {
 			r = (int) (mouseX - (2 * W / 3)) / (W / 6) * 75;
 			b = 255 - (int) (mouseX - (2 * W / 3)) / (W / 6) * 125;
 			return new int[] { r, 0, b };
 		}
 		// else we must be in sector 6
 		else {
 			r = 75 + (int) (mouseX - (5 * W / 6)) / (W / 6) * 68;
 			b = 130 + (int) (mouseX - (5 * W / 6)) / (W / 6) * 125;
 			return new int[] { r, 0, b };
 		}
 	}
 
 	int[] readInput(String[] input) {
 		String[] temp;
 		int[] res = new int[2];
 		// Convert coordinates from String to Float
 		for (int i = 1; i < LENGTH; i++) {
 			input[i] = input[i].replace("(", "");
 			input[i] = input[i].replace(")", "");
 			input[i] = input[i].replace(",", "");
 			temp = input[i].split(" ");
 			Colors[i] = new int[]{Integer.parseInt(temp[2]),
 					Integer.parseInt(temp[3]), Integer.parseInt(temp[4])};
 			locations[0][i - 1] = Float.parseFloat(temp[0]);
 			locations[1][i - 1] = Float.parseFloat(temp[1]);
 		}
 		// Get/return the resolution of the image
 		temp = input[0].split(" ");
 		res[0] = Integer.parseInt(temp[0]);
 		res[1] = Integer.parseInt(temp[1]);
 		return res;
 	}
 	
 	
 	// This describes the force that the white, independent particles exert
 	public class MyAttraction implements Force
 	{
 		Particle a;
 		Particle b;
 		float k;
 		boolean on;
 		float distanceMin;
 		float distanceMinSquared;
 		
 		public MyAttraction( Particle a, Particle b, float k, float distanceMin )
 		{
 			this.a = a;
 			this.b = b;
 			this.k = k;
 			on = true;
 			this.distanceMin = distanceMin;
 			this.distanceMinSquared = distanceMin*distanceMin;
 		}
 
 		protected void setA( Particle p )
 		{
 			a = p;
 		}
 
 		protected void setB( Particle p )
 		{
 			b = p;
 		}
 
 		public final float getMinimumDistance()
 		{
 			return distanceMin;
 		}
 
 		public final void setMinimumDistance( float d )
 		{
 			distanceMin = d;
 			distanceMinSquared = d*d;
 		}
 
 		public final void turnOff()
 		{
 			on = false;
 		}
 
 		public final void turnOn()
 		{
 			on = true;
 		}
 
 		public final void setStrength( float k )
 		{
 			this.k = k;
 		}
 
 		public final Particle getOneEnd()
 		{
 			return a;
 		}
 
 		public final Particle getTheOtherEnd()
 		{
 			return b;
 		}
 
 		public void apply()
 		{
 			if ( on && ( a.isFree() || b.isFree() ) )
 			{
 				float a2bX = a.position().x() - b.position().x();
 				float a2bY = a.position().y() - b.position().y();
 				float a2bZ = a.position().z() - b.position().z();
 
 				float a2bDistanceSquared = a2bX*a2bX + a2bY*a2bY + a2bZ*a2bZ;
 
 				if ( a2bDistanceSquared < distanceMinSquared )
 					a2bDistanceSquared = distanceMinSquared;
 
 				float force = k * a.mass() * b.mass() / a2bDistanceSquared;
 
 				float length = (float)Math.sqrt( a2bDistanceSquared );
 				
 				// make unit vector
 				
 				a2bX /= length;
 				a2bY /= length;
 				a2bZ /= length;
 				
 				// multiply by force 
 				
 				a2bX *= force;
 				a2bY *= force;
 				a2bZ *= force;
 
 				// apply
 				
 				if ( a.isFree() )
 					a.force().add( -a2bX, -a2bY, -a2bZ );
 				//if ( b.isFree() )
 					//b.force().add( a2bX, a2bY, a2bZ );
 			}
 		}
 
 		public final float getStrength()
 		{
 			return k;
 		}
 
 		public final boolean isOn()
 		{
 			return on;
 		}
 
 		public final boolean isOff()
 		{
 			return !on;
 		}
 	}
 }
