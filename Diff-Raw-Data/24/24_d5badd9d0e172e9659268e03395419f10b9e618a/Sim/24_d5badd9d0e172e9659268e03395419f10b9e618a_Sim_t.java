 package simulation;
 
 
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 import java.util.Random;
 
 import processing.core.*;
 import processing.opengl.*;
 import javax.media.opengl.GL;
 
 import boids.*;
 import sprites.*;
 
 import kinect.Kinect;
 import kinect.KinectConfig;
 
 
 @SuppressWarnings("serial")
 public class Sim extends PApplet{
 
 	public static int frameCounter; // Used for animation and color cycles.
 									// Shadows p5's frameCount, but is public
 	
 	public static Random rand; // for spawning
 	public static ArrayList<Integer> colors; // A simulation-wide color palette.
 											 // Boids register their colors on spawn.
 	public static int[] mapFancyInit;
 	// TODO make this more dynamic, put in settings too
 	
 	public ArrayList<Boid> school;
 
 	public static Kinect kinect; // kinect handle
 	public static Sprite fishSprite;
 	public static int backgroundColor;
 	
 	
 	// OpenGL shader stuff
 	String vertexSource;
 	String fragmentSource;
 	int shaderProgram;
 	float time;
 	
 	public Sim(Frame frame) {
 		this.frame = frame;
 	}
 
 
 	public void setup() {
 
 		
 		if( Set.SHOW_Fullscreen ) {
 			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 			size(screenSize.width, screenSize.height, OPENGL);
 		} else {
 			size(Set.SCREEN_Width, Set.SCREEN_Height, OPENGL);  // Set the screen size
 		}
 		
 		frameRate(30);
 		frameCounter = 0;
 		rand = new Random();
 		
 		backgroundColor = color(Set.SCREEM_BackgroundColor[0],
 								Set.SCREEM_BackgroundColor[1],
 								Set.SCREEM_BackgroundColor[2]);
 		colors = new ArrayList<Integer>();
 		school = new ArrayList<Boid>();
 
 		if( Set.SHOW_Sprites ) {
 			fishSprite = new Sprite(this, "/Users/vestibule/java_workspace/Boids/images/ninjaMan.png", 7, 5, 0);
 			fishSprite.setFrameSequence(0, 24);
 			fishSprite.setAnimInterval(.05);
 			float scale = (float)0.5;
 			fishSprite.setScale(scale);
 		}
 		
 
 		if(Set.JOGL_RenderShaders) {
 			try {
 				vertexSource = readFileAsString(Set.JOGL_VertexPath);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			try {
 				fragmentSource = readFileAsString(Set.JOGL_FragmentPath);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			// set up the shader
 			PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
 			GL gl = pgl.beginGL();
 
 			// compiled shader handles
 			int vert = gl.glCreateShader(GL.GL_VERTEX_SHADER);
 			int frag = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);
 
 			String[] vs = new String[1];
 			vs[0] = vertexSource;
 
 			String[] fs = new String[1];
 			fs[0] = fragmentSource;
 
 			gl.glShaderSource(vert, 1, vs, (IntBuffer) null);
 			gl.glCompileShader(vert);
 
 			gl.glShaderSource(frag, 1, fs, (IntBuffer) null);
 			gl.glCompileShader(frag);
 
 			shaderProgram = gl.glCreateProgram();
 			gl.glAttachShader(shaderProgram, vert);
 			gl.glAttachShader(shaderProgram, frag);
 			gl.glLinkProgram(shaderProgram);
 			gl.glValidateProgram(shaderProgram);
 
 			// something to keep track of time
 			// use timeIncrement in Set to control how fast this increases
 			time = 0;
 		}
 
 		// Create new fish with random position in screen and random speed
 		for (int i = 0; i < Set.NUMBER_FishRed + Set.NUMBER_FishBlue
 				+ Set.NUMBER_FishGreen + Set.NUMBER_FishYellow; i++) {
 
 			int color;
 			if (i < Set.NUMBER_FishRed) {
 				color = Fish.RED;
 			} else if (i < Set.NUMBER_FishRed + Set.NUMBER_FishBlue) {
 				color = Fish.BLUE;
 			} else if (i < Set.NUMBER_FishRed + Set.NUMBER_FishBlue
 					+ Set.NUMBER_FishGreen) {
 				color = Fish.GREEN;
 			} else {
 				color = Fish.YELLOW;
 			}
 
 			// position = anywhere
 			// speed = [-.25max, .25max]
 			// size = see settings
 			// color = see above
 			school.add(new Fish(rand.nextFloat() * Set.SCREEN_Width, rand
 					.nextFloat() * Set.SCREEN_Height, (rand.nextFloat() - .5f)
 					* Set.FISH_MaxSpeed / 2, (rand.nextFloat() - .5f)
 					* Set.FISH_MaxSpeed / 2, Set.FISH_MinSize
 					+ rand.nextInt(Set.FISH_MaxSize - Set.FISH_MinSize), this,
 					color));
 
 		}
 
 		if (Set.NUMBER_Obstacles > 0) {
 			for (int i = 0; i < Set.NUMBER_Obstacles; i++) {
 				school.add(new Obstacle());
 			}
 		}
 
 		if (Set.NUMBER_People > 0) {
 			for (int i = 0; i < Set.NUMBER_People; i++) {
 				school.add(new Person());
 			}
 		}
 
 		if (Set.NUMBER_Sharks > 0) {
 			for (int i = 0; i < Set.NUMBER_Sharks; i++) {
 				school.add(new Shark(rand.nextFloat() * Set.SCREEN_Width, rand
 						.nextFloat() * Set.SCREEN_Height, 0, 0, this));
 			}
 		}
 
 		stroke(155, 0, 0);
 		rectMode(CORNERS);
 		// ellipseMode(CORNERS); TODO REUSE when we have better sharks
 		frameRate(30);
 		
 			
 		if (Set.KINECT_On) {
 			kinect = new Kinect(this, Kinect.MOTION_DETECTION);
 //			if( Set.KINECT_FancyStart == false ) {
 				kinect.init();
 //			} else {*/
 //				
 //				for(int i=0; i<=Set.KINECT_CalibrationLevel; i++) {
 //					kinect.initFancy(i);
 //					drawInitFancy(i);
 //				}
 
 		//	}
 /*			if(Set.KINECT_FancyStart) {
 				mapFancyInit = new int[Set.KINECT_CalibrationLevel+1];
 				for(int i=0; i<Set.KINECT_CalibrationLevel; i++) {
 					mapFancyInit[i] = (int)Boid.redoRange(i, Set.SCREEN_Width/2, 1, 0, Set.KINECT_CalibrationLevel );
 				}*/
 			//}
 		}
 	}
 
 
 	@Override
 	public void draw() {
 		
 		/*
 		if(frameCount<=Set.KINECT_CalibrationLevel) {
 			drawFancyInit(frameCount);
 			//System.out.println(frameCount);
 		} else
 		*/
 		
 		// Display the simulation
 		if (Set.paused == false) {
 			frameCounter++;
 
 			// If Kinect is used, update it
 			if (kinect != null && frameCounter % Set.KINECT_FrameRatio == 0) {
 				kinect.update();
 			}
 
 			background(backgroundColor); // Clear screen
 
 			// If we have obstacles and target is turned on, draw it
 			if (Set.NUMBER_Obstacles > 0 && Set.SHOW_ObstacleTarget == true) {
 				fill(150, 0, 0, 40);
 				rect(Set.SCREEN_Width / 2, Set.SCREEN_Height / 2,
 						Set.OBSTACLE_TargetSize, Set.OBSTACLE_TargetSize);
 			}
 			
 
 			for (int i = 0; i < school.size(); i++) {
 				i += school.get(i).step(school);
 				school.get(i).drawBoid(this);
 			}
 
 			// Render the kinect if we should 
 			int color_index;
 			if (Set.KINECT_SetupMode
 					&& (Set.kinect_Render || kinect.config.mode == KinectConfig.MODE_RangeAdjust)) {
 
 				for (int i = 0; i < kinect.goodPixels.length; i++) {
 
 					color_index = (int) Boid.redoRange(
 							kinect.diffMap[kinect.goodPixels[i]], 0,
 							Kinect.NUM_COLORS-1, kinect.range, kinect.range
 									+ kinect.rangeSize);
 
 					if( kinect.config.showBackground ) {
 						if (kinect.diffMap[kinect.goodPixels[i]] < kinect.range ) {
 							color_index = 0;
 						} else if (kinect.diffMap[kinect.goodPixels[i]] > kinect.range+kinect.rangeSize) {
 							color_index = Kinect.NUM_COLORS-1;
 						}
 					} else if( kinect.diffMap[kinect.goodPixels[i]] < kinect.range ||
 							   kinect.diffMap[kinect.goodPixels[i]] > kinect.range+kinect.rangeSize ) {
 						continue;
 					}
 					stroke(colors.get(Kinect.COLOR_OFFSET + color_index));
 
 					point(kinect.mapKinectToSim_Col[kinect.goodPixels[i] % 640],
 							kinect.mapKinectToSim_Row[kinect.goodPixels[i] / 640]);
 				}
 			} else if (Set.KINECT_On && Set.kinect_Render) { // implied Set.KINECT_SetupMode is false
 
 				for (int i = 0; i < kinect.goodPixels.length; i++) {
 
 					if (kinect.diffMap[kinect.goodPixels[i]] > kinect.range
 							&& kinect.diffMap[kinect.goodPixels[i]] < kinect.range
 									+ kinect.rangeSize) {
 						if (kinect.depthMap[kinect.goodPixels[i]] < Kinect.DEPTH_MIN) {
 							color_index = 0;
 						} else if (kinect.depthMap[kinect.goodPixels[i]] > Kinect.DEPTH_MAX) {
 							color_index = Kinect.NUM_COLORS;
 						} else {
 							color_index = kinect.mapDepthToColor[kinect.depthMap[kinect.goodPixels[i]]];
 						}
 
 						stroke(colors.get(Kinect.COLOR_OFFSET + color_index));
 
 						point(kinect.mapKinectToSim_Col[kinect.goodPixels[i] % 640],
 								kinect.mapKinectToSim_Row[kinect.goodPixels[i] / 640]);
 					}
 				}
 			}
 			
 		} else { // paused
						
 			// Display StdDev Adjust mode for Kinect
 			if (Set.KINECT_SetupMode
 					&& kinect.config.mode == KinectConfig.MODE_StdDevAdjust) {
 				background(10, 40, 100); // Clear screen
 
 				for (int i = 0; i < kinect.depthMap.length; i++) {
 					if (kinect.config.stats[i].getStdDev() > kinect.filterThreshold) {
 						
 						if( kinect.filter ) stroke(80,50,50);
 						else stroke(255);
 						
 						point(kinect.mapKinectToSim_Col[i % 640],
 								kinect.mapKinectToSim_Row[i / 640]);
 					}
 				}
 			} else {
 
 	
 				// Renders Kinect color spectrum at the bottom of screen
 				// SLOW, but OKAY
 				if( Set.KINECT_SetupMode && kinect.config.mode == KinectConfig.MODE_RangeAdjust) {
 					for (int i = 0; i < Set.SCREEN_Width; i++) {
 						stroke(colors.get(Kinect.COLOR_OFFSET
 								+ (int) Boid.redoRange(i, 0, Kinect.NUM_COLORS, 0,
 										Set.SCREEN_Width)));
 						line(i, Set.SCREEN_Height - 10, i, Set.SCREEN_Height);
 					}
 				}
 			}
 		}
 		
 		// paused or not
 		
 		// SLOW, TODO maybe cache a loop
 		if (Set.JOGL_RenderShaders) {
 			renderShaders();
 		}
 
 	}
 	
 	private void drawFancyInit(int i) {
 		
 		background(0);
 		
 		int space = mapFancyInit[i];
 
 		stroke(255);
 		while(space<Set.SCREEN_Height/2) {
 			line(0, Set.SCREEN_Height/2 - space, Set.SCREEN_Width, Set.SCREEN_Height/2 - space);
 			line(0, Set.SCREEN_Height/2 + space, Set.SCREEN_Width, Set.SCREEN_Height/2 + space);
 			line(Set.SCREEN_Width/2 - space, 0, Set.SCREEN_Width/2 + space, Set.SCREEN_Height);
 			line(Set.SCREEN_Width/2 - space, 0, Set.SCREEN_Width/2 + space, Set.SCREEN_Height);
 			space += mapFancyInit[i];
 		}
 		
 	}
 
 	/*
 	 * Implements 'pause' with the spacebar
 	 */
 	@Override
 	public void keyPressed() {
 
 		switch(key) {
 		case ' ':
 			Set.paused = !Set.paused;
 			System.out.println(Set.paused ? "PAUSE" : "PLAY");
 			return;
 		case 'a':
 			if( Set.KINECT_On ) {
 				Set.kinect_AffectsSim = !Set.kinect_AffectsSim;
 				System.out.println(Set.kinect_AffectsSim ? "sim using kinect" : "sim ignoring kinect");
 			} else {
 				System.out.println("kinect isn't initialized");
 			}
 			return;
 		case 's':
 			if( Set.KINECT_On && Set.KINECT_INIT_Render ) {
 				Set.kinect_Render = !Set.kinect_Render;
 				System.out.println(Set.kinect_Render ? "showing kinect" : "hiding kinect");
 			} else {
 				System.out.println("kinect isn't initialized");
 			}
 			return;
 		}
 		if (keyCode == ESC) {
 			System.out.println("Goodbye");
 			System.exit(0);
 		}
 
 		if (Set.KINECT_SetupMode) {
 			
 			kinect.config.keyPressed(key, keyCode);
 
 		}
 	}
 
 	/*
 	 * Handles mouse clicks. Currently, LEFT -> new food; RIGHT -> pause
 	 */
 	@Override
 	public void mouseClicked() {
 
 		if (mouseButton == LEFT) {
 			Food f = new Food(mouseX, mouseY);
 			school.add(f);
 			f.drawBoid(this);
 		} else if (mouseButton == RIGHT) {
 			Set.paused = !Set.paused;
 		}
 
 	}
 
 
 	
 	private float lawOfCos_SideC(float sideA, float angleC, float sideB) {
 		return (float) Math.sqrt(Math.pow(sideA, 2) + Math.pow(sideB, 2) - 2
 				* sideA * sideB * ((float) Math.cos(angleC)));
 	}
 
 	private float lawOfCos_AngleC(float sideA, float sideB, float sideC) {
 		return (float) Math
 				.acos((Math.pow(sideA, 2) + Math.pow(sideB, 2) - Math.pow(
 						sideC, 2)) / (2 * sideA * sideB));
 	}
 
 	// returns k*propToThis, unless that is less than min. If so, returns min
 	private double proportion(double propToThis, double k, double min) {
 		return ((k * propToThis) > min) ? (k * propToThis) : min;
 	}
 
 	/*
 	 * public int createColor( int r, int g, int b, int a ) { return
 	 * color(r,g,b,a); }
 	 * 
 	 * public int createColor( int r, int g, int b ) { return color(r,g,b,255);
 	 * }
 	 */
 
 	/*
 	 * private <U> void registerColors( U obj ) {
 	 * 
 	 * int[][] newColors = null ;
 	 * 
 	 * 
 	 * if( obj instanceof YellowFish ) { newColors = YellowFish.requestColors();
 	 * } else if( obj instanceof Fish ) { newColors = Fish.requestColors(); }
 	 * else if( obj instanceof Shark ) { newColors = Shark.requestColors(); }
 	 * 
 	 * if( newColors != null ) {
 	 * 
 	 * int startIndex = colors.size(); int size = 0; // TODO remove size
 	 * failsafe at end
 	 * 
 	 * for( int[] rgba : newColors ) {
 	 * 
 	 * colors.add( color( rgba[0], rgba[1], rgba[2], rgba[3] )); ++size; }
 	 * 
 	 * // changed these to non-static methods if( obj instanceof YellowFish ) {
 	 * YellowFish.receiveColors( startIndex, size ); } else if( obj instanceof
 	 * Fish ) { Fish.receiveColors( startIndex, size ); }
 	 * 
 	 * else if( obj instanceof Shark ) { Shark.receiveColors( startIndex, size
 	 * ); }
 	 * 
 	 * //if( obj) }
 	 * 
 	 * }
 	 */
 
 	
 	private void renderShaders() {
 		time += Set.JOGL_TimeIncrement;
 
 		PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
 		GL gl = pgl.beginGL();
 
 		gl.glUseProgram(shaderProgram);
 		int loc = gl.glGetUniformLocation(shaderProgram, "screen");
 		gl.glUniform1f(loc, Set.SCREEN_Width / Set.SCREEN_Height);
 
 		loc = gl.glGetUniformLocation(shaderProgram, "time");
 		gl.glUniform1f(loc, time);
 
 		gl.glColor3f(1, 1, 1);
 		gl.glBegin(GL.GL_POLYGON);
 		gl.glVertex3f(-Set.SCREEN_Width, -Set.SCREEN_Height, -1);
 		gl.glVertex3f(Set.SCREEN_Width, -Set.SCREEN_Height, -1);
 		gl.glVertex3f(Set.SCREEN_Width, Set.SCREEN_Height, -1);
 		gl.glVertex3f(-Set.SCREEN_Width, Set.SCREEN_Height, -1);
 		gl.glEnd();
 
 		gl.glUseProgram(0);
 
 		pgl.endGL();
 	}
 	
 	private static String readFileAsString(String filePath)
 			throws java.io.IOException {
 		byte[] buffer = new byte[(int) new File(filePath).length()];
 		BufferedInputStream f = null;
 		try {
 			f = new BufferedInputStream(new FileInputStream(filePath));
 			f.read(buffer);
 		} finally {
 			if (f != null)
 				try {
 					f.close();
 				} catch (IOException ignored) {
 				}
 		}
 		return new String(buffer);
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/*
 	class Animation {
 		  PImage[] images;
 		  int imageCount;
 		  int frame;
 		  
 		  Animation(String imagePrefix, int count) {
 		    imageCount = count;
 		    images = new PImage[imageCount];
 
 		    for (int i = 0; i < imageCount; i++) {
 		      // Use nf() to number format 'i' into four digits
 		      String filename = imagePrefix + nf(i, 4) + ".png";
 		      images[i] = loadImage(filename, "png");
 		    }
 		    for (int k = 0; k < imageCount; k++)
 		    {
 		    	images[k].resize(20, 20);
 		    }
 		  }
 
 		  void display(float xpos, float ypos, float xvel, float yvel) {
 			if(ypos>xpos ||(-xpos>ypos))
 			{
 				frame= 1;
 			}
 			else
 			{
 				frame=0;
 			}
 		    //frame = (frame+1) % imageCount;
 			  image(images[frame], xpos, ypos);
 		  }
 		  
 		  int getWidth() {
 		    return images[0].width;
 		  }
 		}
 */
 	public int registerColors( int[][] newColors ) {
 		assert(newColors!=null);
 
 		int startIndex = colors.size();
 
 		for (int[] rgba : newColors) {
 			colors.add(color(rgba[0], rgba[1], rgba[2], rgba[3]));
 		}
 
 		return startIndex;
 	}
 }
