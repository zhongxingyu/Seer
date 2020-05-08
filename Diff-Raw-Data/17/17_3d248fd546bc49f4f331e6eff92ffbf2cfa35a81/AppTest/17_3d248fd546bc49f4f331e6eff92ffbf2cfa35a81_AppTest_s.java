 package com.github.eildosa.app;
 
import java.io.IOException;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.glu.GLU;
 
 
 public class AppTest {
     
     private final String windowTitle = "Colorful rotating cube";
     private DisplayMode displayMode;
     private boolean done = false;
     private float x, y, z;
    
     public static void main(String args[]) {
         AppTest t6 = new AppTest();
         t6.run();
     }
 
     /**
      * Boucle principale
      */
     public void run() {
         try {
             init();
             while (!done) {
                 readAndAct();
                 render();
                 Display.update();
             }
             Destroy();
         }
         catch (Exception e) {
             e.printStackTrace();
             System.exit(0);
         }
     }
 
     /**
      * Toute les updates sont fait ici, lecture de clavier, 
      * lecture de souris, agissement IA, etc
      */
     private void readAndAct() {
     	//quitte si echap est pressé
         if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
             done = true;
         }
         //quitte si la croix est cliqué
         if(Display.isCloseRequested()) {             
             done = true;
         }
 
     }
     
 
     
     
     
 
     /**
      * Ici tous les dessin sont fait a l'ecran
      * le "rendering"
      * @return boolean si tous c'est bien passer
      */
     private boolean render() {
     	GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Clear The Screen And The Depth Buffer
     	GL11.glLoadIdentity(); // Reset The Current Modelview Matrix
     	GL11.glTranslatef(0.0f, 0.0f, -6.0f);
     	
     	Draw.cubeHereColorful(x, y, z, 1.0f);
     	x += 0.035f;
     	y += 0.02f*2f;
     	z += 0.05f*1f;
         return true;
     }
 
     /**
      * Crée la fenetre
      * @throws Exception Throws Window.create() exception.
      */
     private void createWindow() throws Exception {
     	//demande tous les mode d'affichage disponible
         DisplayMode d[] = Display.getAvailableDisplayModes();
         //parcour tous les mode retourné et selectionne celui ou
         //hauteur = 480, largeur = 640, et ou on a 32 bits par pixel
         for (int i = 0; i < d.length; i++) {
             if (d[i].getWidth() == 640
                 && d[i].getHeight() == 480
                 && d[i].getBitsPerPixel() == 32) {
                 displayMode = d[i];
                 break;
             }
         }
         Display.setDisplayMode(displayMode);
         Display.setTitle(windowTitle);
         Display.create();
     }
 
     /**
      * Faire toute les initialisation ici, comme le clavier et openGL
      */
     private void init() throws Exception {
         createWindow();
         initGL();
     }
     
 
     
 
     /**
      * Init OpenGL
      *
      */
     private void initGL() {
     	
         GL11.glEnable(GL11.GL_TEXTURE_2D);//texture mapping 
         GL11.glShadeModel(GL11.GL_SMOOTH);//smooth shading 
         GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);//couleur de fond noire
         GL11.glClearDepth(1.0);//setup du buffer de profondeur 
         GL11.glEnable(GL11.GL_DEPTH_TEST);//permet le test de profondeur 
         GL11.glDepthFunc(GL11.GL_LEQUAL); //le type de test de profondeur a faire
         GL11.glMatrixMode(GL11.GL_PROJECTION);//selectrione la matrice de projection    
         GL11.glLoadIdentity();//reset la matrice de projection
         // Calcul l'aspect ratio de la fenetre
         GLU.gluPerspective(
           45.0f,
           (float) displayMode.getWidth() / (float) displayMode.getHeight(),
           0.1f,
           100.0f);
           
           GL11.glMatrixMode(GL11.GL_MODELVIEW);//selectionne la modelview Matrix
           // met les melleur parametre
           GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
     }
 
     /**
      * nettoyage.
      */
     private void Destroy() {
         Display.destroy();
     }
 
 }
 
 
 
 
