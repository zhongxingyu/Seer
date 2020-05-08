 package com.trippylizard.tensixtysix;
 
 import static org.lwjgl.openal.AL10.*;
 import static org.lwjgl.opengl.GL11.*;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Logger;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.openal.*;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.util.WaveData;
 import org.newdawn.slick.util.ResourceLoader;
 
 import com.trippylizard.tensixtysix.fighter.Fighter;
 import com.trippylizard.tensixtysix.fighter.Fighter.FighterClass;
 import com.trippylizard.tensixtysix.models.*;
 import com.trippylizard.tensixtysix.nations.*;
 import com.trippylizard.tensixtysix.utils.StreamUtils;
 
 public class Main {
 
 	int WIDTH = 1280;
 	int HEIGHT = 768;
 	
 	int albuffer;
 	int menuthemesource;
 	
 	boolean musicon = true;
 	
 	private static final List<Fighter> fighters = new ArrayList<Fighter>();
 	int fightercount = 0;
 	
 	int customnationsize = 6;
 	
 	@SuppressWarnings("unused")
 	private final static Logger logger = Logger.getLogger(Main.class.getName());
 	
 	Map map;
 	
 	public Main() {
 		try {
 			Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
 			Display.setTitle("1066 - Alpha Version 0.0.1");
 			Display.create();
 			AL.create();
 		} catch (LWJGLException ex) {
 			ex.printStackTrace();
 			closeall();
 		}
 		
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glOrtho(0, WIDTH, HEIGHT, 0, 1, -1);
 		glMatrixMode(GL_MODELVIEW);
 		
 		try {
 			map = new Map(OBJModelLoader.loadModel(StreamUtils.streamToFile(ResourceLoader.getResourceAsStream("res/Car.obj"))));
 		} catch (IOException ex) {
 			ex.printStackTrace();
 			closeall();
 		}
 		
 		System.out.println(glGetString(GL_VERSION));
 		
 		playMenuMusic();
 		
 		int trianglelist = glGenLists(1);
 		
 		for(int i = 0; i < customnationsize; i++) {
 			fighters.add(new Fighter(Nation.NORMANS, (fightercount++) + 1, FighterClass.WARRIOR, 1));
 			fighters.add(new Fighter(Nation.SAXONS, (fightercount++) + 1, FighterClass.WARRIOR, 1));
 			fighters.add(new Fighter(Nation.VIKINGS, (fightercount++) + 1, FighterClass.WARRIOR, 1));
 		}
 		
 		glNewList(trianglelist, GL_COMPILE);
 			glBegin(GL_TRIANGLES);
 				glColor3f(1.0f, 0f, 0f);
 				glVertex2i(100, 100);
 				glColor3f(0f, 1.0f, 0f);
 				glVertex2i(WIDTH - 100, 100);
 				glColor3f(0f, 0f, 1.0f);
 				glVertex2i(HEIGHT - 100, WIDTH / 2);
 			glEnd();
 		glEndList();
 		
 		int testObjectList = glGenLists(1);
 		glNewList(testObjectList, GL_COMPILE);
 			Model m = null;
 			try {
 				m = OBJModelLoader.loadModel(StreamUtils.streamToFile(ResourceLoader.getResourceAsStream("res/OpenGL Monkey.obj")));
 			} catch (IOException e) {
 				e.printStackTrace();
 				glDeleteLists(trianglelist, 1);
 				alDeleteBuffers(albuffer);
 				closeall();
 			}
 			glBegin(GL_TRIANGLES);
 				OBJModelLoader.renderModel(m);
 			glEnd();
 		glEndList();
 		
 		
 		Normans.construct();
 		
 		while (!Display.isCloseRequested()) {
 			//Render Code
 			
 			glClear(GL_COLOR_BUFFER_BIT);
 			
 			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
 				alDeleteBuffers(albuffer);
 				closeall();
 			}
 			
 			while (Keyboard.next()) {
 				if (Keyboard.isKeyDown(Keyboard.KEY_F10)) {
 					if (Keyboard.getEventKeyState()) {
 						if (musicon) {
 							alSourcePause(menuthemesource);
 							musicon = false;
 						} else {
 							AL10.alSourcePlay(menuthemesource);
 							musicon = true;
 						}
 					}
 				}
 			}
 			
 			for (final Fighter f : fighters) {
 				if (!f.isCreated()) {
 					f.build(random((int) Math.floor(map.getMapLength()), 0), random((int) Math.floor(map.getMapWidth()), 0));
 					System.out.println(f.getNation() + " " + f.getFighterClass() + " has spawned at " + f.getX() + "," + f.getZ() + " with the id " + f.getID() + ".");
 				} else {
 					boolean successful = false;
 					while (!successful) {
 						int rand = random(4, 1);
 						if (rand == 1) {
 							if (!(Math.floor(f.getX()) + 1 > map.getMapLength())) {
 								int pastx = f.getX();
 								int pastz = f.getZ();
 								
 								f.updateposition(1, 0);
 								System.out.println(f.getNation() + " " + f.getFighterClass() + " has moved from " + pastx + "," + pastz + " to " + f.getX() + "," + f.getZ() + " with the id " + f.getID() + ".");
 								successful = true;
 								break;
 							}
 						} else if (rand == 2) {
 							if (!(Math.floor(f.getX()) - 1 < 0)) {
 								int pastx = f.getX();
 								int pastz = f.getZ();
 								
 								f.updateposition(-1, 0);
 								System.out.println(f.getNation() + " " + f.getFighterClass() + " has moved from " + pastx + "," + pastz + " to " + f.getX() + "," + f.getZ() + " with the id " + f.getID() + ".");
 								successful = true;
 								break;
 							}
 						} else if (rand == 3) {
 							if (!(Math.floor(f.getZ()) + 1 > map.getMapWidth())) {
 								int pastx = f.getX();
 								int pastz = f.getZ();
 								
 								f.updateposition(0, 1);
 								System.out.println(f.getNation() + " " + f.getFighterClass() + " has moved from " + pastx + "," + pastz + " to " + f.getX() + "," + f.getZ() + " with the id " + f.getID() + ".");
 								successful = true;
 								break;
 							}
 						} else if (rand == 4) {
 							if (!(Math.floor(f.getZ()) - 1 < 0)) {
 								int pastx = f.getX();
 								int pastz = f.getZ();
 								
 								f.updateposition(0, -1);
 								System.out.println(f.getNation() + " " + f.getFighterClass() + " has moved from " + pastx + "," + pastz + " to " + f.getX() + "," + f.getZ() + " with the id " + f.getID() + ".");
 								successful = true;
 								break;
 							}
 						}
 						successful = false;
 					}
 				}
 			}
 			
 			glCallList(trianglelist);
 			
 			Display.update();
			Display.sync(5);
 		}
 		
 		glDeleteLists(testObjectList, 1);
 		glDeleteLists(trianglelist, 1);
 		alDeleteBuffers(albuffer);
 		closeall();
 	}
 	
 	public static void main(String[] args) {
 		new Main();
 	}
 	
 	private void playMenuMusic() {
 		
 		BufferedInputStream stream = new BufferedInputStream(ResourceLoader.getResourceAsStream("res/theme.wav"));
 		WaveData data = WaveData.create(stream);
 		albuffer = alGenBuffers();
 		alBufferData(albuffer, data.format, data.data, data.samplerate);
 		data.dispose();
 		menuthemesource = alGenSources();
 		
 		alSourcef(menuthemesource, AL_BUFFER, albuffer);
 		
 		alSourcePlay(menuthemesource);
 	}
 	
 	private void closeall() {
 		AL.destroy();
 		Display.destroy();
 		System.exit(0);
 	}
 	
 	public int random(int max, int min) {
 		return min + (int)(Math.random() * ((max - min) + 1));
 	}
 }
