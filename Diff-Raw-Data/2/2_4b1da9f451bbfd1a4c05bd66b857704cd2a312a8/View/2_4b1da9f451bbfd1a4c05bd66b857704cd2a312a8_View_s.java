 package view;
 
 import gui.Gui;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import model.Model;
 import model.DayNightCycle;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 import sprite.Sprite;
 import world.Camera;
 import world.World;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.gluPerspective;
 
 public class View {
 	private Model controll;
 	private DayNightCycle cycle;
 	private Render3dV2 view;
 	private Render2d gui;
 	private Gui guiList;
 	private World menu;
 	
 	private float[] ambientLight = {1.0f, 0.0f, 0.0f, 1.0f};	//all around light
 	private float[] diffuseLight = {0.0f, 0.0f, 0.0f, 1.0f};	//specific light
 	private float[] lightPosition = {0.0f, 0.0f, 0.0f, 1.0f};
 	
 	private FloatBuffer fogColor = BufferUtils.createFloatBuffer(4);
 	
 	private int[][] displayRes = {{1920, 1080}, {1680, 1050}, {1440, 1024}, {1440, 900}, {1440, 852}, {1280, 800}, {1152, 720}, {1024, 768},
 			{1024, 640}, {800, 600}, {800, 500}, {720, 480}, {640, 480}};
 	
 	public View(Model c, String title, int width, int height){
 		try{
 			Display.setDisplayMode(new DisplayMode(width, height));
 			Display.setTitle(title);
 			Display.setResizable(true);
 			Display.create();
 			Display.setVSyncEnabled(true);
 		}catch(Exception e){}
 		controll = c;
 	}
 	
 	public void initGL(){
 		new InitGL(controll.width, controll.height, ambientLight, diffuseLight, lightPosition);
 	}
 	
 	public void init(DayNightCycle dnc, Sprite w, Gui i, World m, Camera c) {
 		cycle = dnc;
 		view = new Render3dV2(controll, c);
 		guiList = i;
 		gui = new Render2d(guiList);
 		menu = m;
 	}
 	
 	public void update(){
 		
 		ambientLight[0] = cycle.getBrightness();
 		ambientLight[1] = cycle.getBrightness();
 		ambientLight[2] = cycle.getBrightness();
 		
 		ByteBuffer temp = ByteBuffer.allocateDirect(16);
 		temp.order(ByteOrder.nativeOrder());
 		glLight(GL_LIGHT1, GL_AMBIENT, (FloatBuffer)temp.asFloatBuffer().put(ambientLight).flip());
 		
 		fogColor.put(0.3f * cycle.getFogColor()).put(.7f * cycle.getFogColor()).put(1.0f * cycle.getFogColor()).put(1.0f).flip();
 		glFog(GL_FOG_COLOR, fogColor);
 		
 		if(Display.getWidth() != controll.width && !Display.wasResized()){
 			resize(Display.getWidth(), Display.getHeight());
 		}
 		
 		if(Display.getHeight() != controll.height && !Display.wasResized()){
 			resize(Display.getWidth(), Display.getHeight());
 		}
 	}
 	
 	public boolean setFullscreen(boolean f){	//returns true if display state changed sucsess
 		if(f){
 			for(int i = 0; i < displayRes.length; i++){
 				setDisplayMode(displayRes[i][0], displayRes[i][1], true);
 				if(Display.isFullscreen()){
 					return true;
 				}
 			}
 			return false;
 		}else{
 			setDisplayMode(1000, 700, false);
 			return true;
 		}
 	}
 	
 	public void setDisplayMode(int w, int h, boolean fullscreen){
 		if(w == Display.getWidth() 
 				&& h == Display.getHeight() 
 				&& Display.isFullscreen() == fullscreen){
 			return;
 		}
 		try{
 			DisplayMode targetDisplay = null;
 			if(fullscreen){
 				DisplayMode[] modes = Display.getAvailableDisplayModes();
 				int freq = 0;
 				
 				for(int i = 0; i < modes.length; i++){
 					DisplayMode current = modes[i];
 					if(current.getWidth() == w && current.getHeight() == h){
 						
 						if(targetDisplay == null || current.getFrequency() >= freq){
 							targetDisplay = current;
 							freq = current.getFrequency();
 							System.out.println("displayMode: x:" + targetDisplay.getWidth() + " y:" + targetDisplay.getHeight());
 						}
 					}
 				}
 			}else{
 				targetDisplay = new DisplayMode(w, h);
 			}
 			
 			if(targetDisplay == null){
 				return;
 			}
 			
 			Display.setDisplayMode(targetDisplay);
 			Display.setFullscreen(fullscreen);
 			resize(w, h);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public void resize(int w, int h){
 		controll.width = w;
 		controll.height = h;
 		
 		glViewport(0, 0, controll.width, controll.height);
 	}
 	
 	public void setFog(boolean b){
 		if(b){
 			glEnable(GL_FOG);
 		}else{
 			glDisable(GL_FOG);
 		}
 	}
 	
 	public void render(){
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		
 		//setup display to render 3d
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
		gluPerspective(menu.FOV, (float)controll.width / controll.height, 0.1f, 1000.0f);
 		glMatrixMode(GL_MODELVIEW);
 		glLoadIdentity();
 		glEnable(GL_ALPHA_TEST);
 		glEnable(GL_LIGHTING);
 		glDisable(GL_BLEND);
 		view.render();
 		
 		
 		//setup display to render gui
 		glDisable(GL_CULL_FACE);
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glOrtho(0, controll.width, controll.height, 0, 0, -1);
 		glMatrixMode(GL_MODELVIEW);
 		glLoadIdentity();
 		glDisable(GL_ALPHA_TEST);
 		glDisable(GL_LIGHTING);
 		glDisable(GL_BLEND);
 		gui.render(controll.width, controll.height);
 		Display.update();
 	}
 }
