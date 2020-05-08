 package ass1.MalcolmsGame.sailing;
 
 import ass1.Camera;
 import ass1.GameEngine;
 import ass1.MalcolmsGame.sailing.objects.CameraHarness;
 import ass1.MalcolmsGame.sailing.objects.Pirate;
 import ass1.Mouse;
 import com.jogamp.opengl.util.FPSAnimator;
 
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLProfile;
 import javax.media.opengl.awt.GLJPanel;
 import javax.swing.*;
 import java.awt.*;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * COMMENT: Comment Triangle 
  *
  * @author malcolmr                   *
  */
 public class SailingGame extends JFrame {
 
     private static final long serialVersionUID = 1L;
     private static final float[] WATER_COLOR = { 0.0f, 0.0f, 1.0f, 0.0f };
     private static final String MAP_FILE = "map.json";
 
     private Map myMap;
     private GLJPanel myPanel;
 
     public static final SailingGame theGame = new SailingGame();
     
     private SailingGame() {
         super("Sailing Game");
     }
     
     private void init()  throws IOException {
         GLProfile glprofile = GLProfile.getDefault();
         GLCapabilities glcapabilities = new GLCapabilities(glprofile);
         glcapabilities.setSampleBuffers(true);
         glcapabilities.setNumSamples(4);
         myPanel = new GLJPanel(glcapabilities);
 
         //System.out.println(System.getProperty("user.dir"));
 
         readMap(new File(MAP_FILE));
 
         // attach the camera to the player
         Pirate player = myMap.player();
         Camera camera = new Camera(new CameraHarness(player));
         camera.scale(20);
         camera.setBackground(WATER_COLOR);
 
         GameEngine engine = new GameEngine(camera);
         myPanel.addGLEventListener(engine);
 
         // Add an animator to call 'display' at 60fps        
         FPSAnimator animator = new FPSAnimator(60);
         animator.add(myPanel);
         animator.start();
 
         // Add a mouse event handler        
         myPanel.addMouseListener(Mouse.theMouse);
         myPanel.addMouseMotionListener(Mouse.theMouse);
 
         getContentPane().add(myPanel, BorderLayout.CENTER);
         setSize(1024, 768);
         setVisible(true);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
     }
 
     public void readMap(File mapFile) throws IOException {
         InputStream in = new FileInputStream(mapFile);
         myMap = Map.read(in);
         in.close();
     }
 
     public static void main(String[] args) throws IOException {
         theGame.init();
     }
 
 }
