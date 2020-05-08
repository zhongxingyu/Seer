 package co.devrandom.vc;
 
 import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
 import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
 import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
 import static org.lwjgl.opengl.GL11.GL_PROJECTION;
 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glClear;
 import static org.lwjgl.opengl.GL11.glColor4f;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glLoadIdentity;
 import static org.lwjgl.opengl.GL11.glMatrixMode;
 import static org.lwjgl.opengl.GL11.glOrtho;
 import static org.lwjgl.opengl.GL11.glPopMatrix;
 import static org.lwjgl.opengl.GL11.glPushMatrix;
 import static org.lwjgl.opengl.GL11.glRotatef;
 import static org.lwjgl.opengl.GL11.glScalef;
 import static org.lwjgl.opengl.GL11.glTexCoord2f;
 import static org.lwjgl.opengl.GL11.glTranslatef;
 import static org.lwjgl.opengl.GL11.glVertex2f;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.openal.Audio;
 import org.newdawn.slick.openal.SoundStore;
 
 import co.devrandom.main.GameState;
 import co.devrandom.model.PhysicsObject;
 import co.devrandom.model.Model;
 import co.devrandom.util.AudioLoader;
 import co.devrandom.util.FontLoader;
 import co.devrandom.util.Vector;
 
 public class ViewController implements Runnable{
 	Model model;
 	
 	TrueTypeFont headerFont;
 	TrueTypeFont bodyFont;
 	
 	Audio ping;
 	
 	public ViewController(Model model){
 		this.model = model;
 	}
 	
 	public void run() {
 		try {
 			Display.setDisplayMode(new DisplayMode(GameState.WINDOW_WIDTH, GameState.WINDOW_HEIGHT));
 			Display.setTitle(GameState.NAME);
			Display.sync(GameState.FPS);
 			Display.create();
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 		
 		initGL();
 		loadFonts();
 		loadAudio();
 		loadTextures();
 		
 		while (!Display.isCloseRequested()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
 			
 			TextureList.newFrame();
 			setCamera();
 			handleInput();
 			
 			glPushMatrix();
 			
 			// Put all world matrix transforms here.
 			glTranslatef((float) GameState.WINDOW_WIDTH / 2, (float) GameState.WINDOW_HEIGHT / 2, 0);
 			glScalef(.5f, .5f, .5f);
 			
 			/*
 			 * Draw all gameObjects;
 			 */
 			for (PhysicsObject gameObject : model.getGameObjects()){
 				TextureAttributes texAttr = gameObject.getTexAttributes();
 				if (texAttr != null) {
 					texAttr.checkAnimation();
 					texAttr.textures[texAttr.currentFrame].bindTexture();
 					glColor4f(texAttr.r, texAttr.g, texAttr.b, texAttr.a);
 					glPushMatrix();
 					{
 						glTranslatef((float) gameObject.getPosition().x, (float) gameObject.getPosition().y, 0);
 						glRotatef(gameObject.getRotation(), 0, 0, 1); 
 						glScalef(texAttr.width, texAttr.height, 0);
 						
 						Vector start = texAttr.getStartTexPosition();
 						Vector end = texAttr.getEndTexPosition();
 						
 						glBegin(GL_QUADS);
 						{
 							glTexCoord2f((float) start.x, (float) start.y);
 							glVertex2f(-.5f, -.5f);
 							glTexCoord2f((float) end.x, (float) start.y);
 							glVertex2f(.5f, -.5f);
 							glTexCoord2f((float) end.x, (float) end.y);
 							glVertex2f(.5f, .5f);
 							glTexCoord2f((float) start.x, (float) end.y);
 							glVertex2f(-.5f, .5f);
 						}
 						glEnd();
 					}
 					glPopMatrix();
 				}
 			}
 		
 			glPopMatrix();
 			
 			bodyFont.drawString(10, 10, "Ayyyyyyyy!", Color.black);
 			
 			SoundStore.get().poll(0);
 			
 			Display.update();
 		}
 		
 		Display.destroy();
 		System.exit(0);
 	}
 	
 	public void handleInput() {
 		while (Keyboard.next()) {
 		    if (Keyboard.getEventKeyState()) {
 		        if (Keyboard.getEventKey() == KeyPress.FORWARD.getKeyID()) {
 		        	System.out.println("forward");
 		        } else if (Keyboard.getEventKey() == KeyPress.BACKWARD.getKeyID()) {
 		        	System.out.println("backward");
 		        } else if (Keyboard.getEventKey() == KeyPress.LEFT.getKeyID()) {
 		        	System.out.println("left");
 		        } else if (Keyboard.getEventKey() == KeyPress.RIGHT.getKeyID()) {
 		        	System.out.println("right");
 		        } else if (Keyboard.getEventKey() == KeyPress.PING.getKeyID()) {
 		        	ping.playAsSoundEffect(1.0f, 1.0f, false);
 		        } else if (Keyboard.getEventKey() == KeyPress.PAUSE.getKeyID()) {
 		        	GameState.pauseUnpause();
 		        	System.out.println("Pause unPause");
 		        }
 		    } else {
 		        if (Keyboard.getEventKey() == KeyPress.FORWARD.getKeyID()) {
 		        	System.out.println("forward released");
 		        } else if (Keyboard.getEventKey() == KeyPress.BACKWARD.getKeyID()) {
 		        	System.out.println("backward released");
 		        } else if (Keyboard.getEventKey() == KeyPress.LEFT.getKeyID()) {
 		        	System.out.println("left released");
 		        } else if (Keyboard.getEventKey() == KeyPress.RIGHT.getKeyID()) {
 		        	System.out.println("right released");
 		        }
 		    }
 		}	
 	}
 	
 	private void initGL() {
 		GL11.glViewport(0, 0, GameState.WINDOW_WIDTH, GameState.WINDOW_HEIGHT);
 		GL11.glEnable(GL11.GL_DEPTH_TEST);
 		GL11.glDepthFunc(GL11.GL_LEQUAL);
 		GL11.glShadeModel(GL11.GL_SMOOTH);
 		GL11.glEnable(GL11.GL_LINE_SMOOTH);
 		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 		GL11.glEnable(GL11.GL_BLEND);
 		GL11.glEnable(GL11.GL_ALPHA_TEST);
 		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
 		GL11.glClearColor(1, 1, 1, 1);
 		GL11.glLineWidth(3);
 		GL11.glTexParameteri( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR );	
 	}
 
 	private void loadFonts() {
 		this.headerFont = FontLoader.loadTTF("iceland.ttf", 24f);
 		this.bodyFont = FontLoader.loadTTF("iceland.ttf", 16f);	
 	}
 	
 	private void loadAudio() {
 		this.ping = AudioLoader.loadOGG("sonar-pings.ogg");
 	}
 	
 	private void loadTextures() {
 		for (TextureList texture : TextureList.values()){
 			texture.initializeTexture();
 		}
 	}
 	
 	/**
 	 * Initialize the camera for the next frame of OpenGL. Positions the center of the camera
 	 * at controller.getCameraPosition().
 	 */
 	private void setCamera() {
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 	    glOrtho(0.0f, GameState.WINDOW_WIDTH, GameState.WINDOW_HEIGHT, 0.0f, 0.0f, 1.0f);
 		glMatrixMode(GL_MODELVIEW);
 		glLoadIdentity();
 	}
 }
