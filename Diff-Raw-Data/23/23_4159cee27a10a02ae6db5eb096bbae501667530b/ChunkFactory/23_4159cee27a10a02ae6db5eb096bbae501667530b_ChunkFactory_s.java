 package leveldata;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.light.Light;
 import com.jme3.light.PointLight;
 import com.jme3.light.SpotLight;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import spatial.Platform;
 import spatial.Wall;
 import spatial.WindowFrame;
 import spatial.hazard.Hazard;
 import spatial.hazard.LinearFireball;
 import spatial.hazard.SpinningFireball;
 import spatial.hazard.StationaryFireball;
 import spatial.hazard.Wizard;
 import variables.P;
 
 /**
  * A class for generating content for LevelChunks. An instance of this class can
  * take an emoty chunk and fill it with background, platforms, windows, light
  * sources etc. In the future it would probably handle e.g. particle effects as
  * well.
  *
  * This is the de facto level generator – the code for where each platform etc
  * is placed can be found here.
  *
  * @author jonatankilhamn
  */
 public class ChunkFactory {
 
     private AssetManager assetManager;
     private int counter;
     private float height;
     private float distanceOverFlow;
 
     public ChunkFactory(AssetManager assetManager) {
         this.assetManager = assetManager;
     }
 
     /**
      * Generates a new chunk of the level. The generated content is delivered in
      * a list of
      * <code>Spatial</code>s. One of the spatials must have the name
      * "background".
      *
      * @return A list of <code>Spatial</code>s.
      *
      */
     public List<Spatial> generateChunk() {
 
 
         LinkedList<Spatial> list = new LinkedList<Spatial>();
 
         // Generate the background:
         Node staticObjects = new Node("background");
 
         // the wall:
         Wall wall = new Wall(this.assetManager);
         staticObjects.attachChild(wall);
 
         // the decorations:
         float wHeight = Math.max(0, 5 * Math.round(height / 5));
 
         WindowFrame window = createWindowFrame(5f, wHeight + 5f);
         staticObjects.attachChild(window);
         list.add(staticObjects);
 
 
         // Generate everything with a physical / game mechanical connection:
 
         // standard length and distance
         float total = P.chunkLength;
         float dist = P.platformDistance;
         float length = P.platformLength;
         float d = distanceOverFlow;
         distanceOverFlow = 0;
 
         Random random = new Random();
 
         int key;
         if (counter < 4) { //nothing special on the first chunk
             key = 1;
         } else if (height < -2) {
             key = 3;
         } else if (height > 10) {
             key = 4;
         } else {
             key = random.nextInt(5);
         }

         switch (key) {
             case (0): // chilling platforms with wizard
                 list.add(createWizard());
             case (1): // chilling platforms
                 while (d < total) {
 
                     list.add(createPlatform(d, height + random.nextFloat() * 3, length));
                     d += length + dist;
                 }
                 break;
             case (2): // chilling differentlength platforms
                 while (d < total) {
                     float nLength = length * (0.5f + random.nextFloat() / 2);
                     float nDist = dist * (1f + random.nextFloat());
 
                    list.add(createPlatform(d + nDist, height + random.nextFloat() * 3, nLength));
                     d += nLength + nDist;
                 }
                 break;
             case (3): // climbing platforms
                 while (d < total) {
                     float nLength = length * (0.5f + random.nextFloat() / 2);
                     float nDist = dist * (1f + random.nextFloat());
 
                    list.add(createPlatform(d + nDist, height + random.nextFloat() * 2, nLength));
                    height += 1 + 5 * random.nextFloat();
                     d += nLength + nDist;
                 }
                 break;
             case (4): // descending platforms
                float lastDescent;
                 while (d < total) {
                     float nLength = length * (0.5f + random.nextFloat() / 2);
                     float nDist = dist * (1f + random.nextFloat());
                    lastDescent = 2 + 8 * random.nextFloat();
                    if (height - lastDescent > P.deathTreshold + 2) {
                        height -= (2 + 8 * random.nextFloat());
                     }
                    list.add(createPlatform(d + nDist, height + random.nextFloat() * 4, nLength));
                     d += nLength + nDist;
                 }
                 break;
             default:
                 break;
 
         }
         distanceOverFlow = d - total;
 
 
 
 
 
         // the lights:
         /*
          * This code creates a spotlight for each window. Slow on the phone.
          // a light shining out the window:
          staticObjects.addLight(createWindowLight(5f,5f));
          */
 
         /*
          * This code creates a light of random colour. Slow on the phone.
          // generate a point light source of a random colour
          staticObjects.addLight(createColouredLight());
          */
 
 
 
         // create a fireball hazard:
         /*
          * This code creates a FireballControl-type hazard floating in mid-air,
          * triggering the first time the player bumps into it.
          */
         //list.addLast(createHoveringFireball());
 
         // create a wizard (but not for the first two chunks, that's too hard)
         if (counter > 1) {
             //list.addLast(createWizard());
         }
         counter++;
 
         return list;
 
     }
 
     /* Creates a platform at a given 2d position */
     private Platform createPlatform(float positionX, float positionY, float length) {
         Vector3f platformPos = new Vector3f(positionX, positionY, 0f);
         Platform platform = new Platform(this.assetManager, platformPos, length, P.platformHeight, P.platformWidth);
         return platform;
     }
 
     /* Creates a windowframe on the wall at a given position */
     private WindowFrame createWindowFrame(float positionX, float positionY) {
         Vector3f windowPos = new Vector3f(positionX, positionY, 0f);
         WindowFrame window = new WindowFrame(this.assetManager, windowPos);
         return window;
     }
 
     /* Creates a spotlight shining through a window at a given position */
     private Light createWindowLight(float positionX, float positionY) {
         SpotLight windowLight = new SpotLight();
         windowLight.setSpotOuterAngle(15f * FastMath.DEG_TO_RAD);
         windowLight.setSpotInnerAngle(13f * FastMath.DEG_TO_RAD);
         Vector3f windowPosition = new Vector3f(positionX, positionY, 0f);
         windowLight.setPosition(windowPosition.subtract(P.windowLightDirection));
         windowLight.setDirection(P.windowLightDirection);
         windowLight.setSpotRange(100f);
         return windowLight;
     }
 
     /* Creates a light of a random colour, a bit above and in front of the player */
     private Light createColouredLight() {
         Random random = new Random();
         int rand = random.nextInt(6);
         PointLight light = new PointLight();
         light.setRadius(40);
         light.setPosition(new Vector3f(15f, 10f, 0f));
         if (rand < 3) {
             light.setColor(ColorRGBA.Blue);
         } else if (rand < 5) {
             light.setColor(ColorRGBA.Red);
         } else {
             light.setColor(ColorRGBA.Green);
         }
         return light;
     }
 
     /* Creates a fireball hazard floating in the air.*/
     private Hazard createHoveringFireball() {
         Hazard hazard = new StationaryFireball(assetManager);
         hazard.move(10f, 15f, 0f);
         return hazard;
     }
 
     /* Creates a fireball hazard flying in a straight line.*/
     private Hazard createLinearFireball() {
         Hazard hazard = new LinearFireball(assetManager, new Vector3f(-20, 0, 0));
         hazard.move(5f, 6f, 0f);
         return hazard;
     }
 
     /* Creates a fireball hazard flying in a counter-clockwise circle.*/
     private Hazard createSpinningFireball() {
         Hazard hazard = new SpinningFireball(assetManager);
         hazard.move(5f, 6f, 0f);
         return hazard;
     }
 
     /* Creates a wizard shooting fireballs at the player.*/
     private Hazard createWizard() {
         Hazard wizard = new Wizard(assetManager);
         wizard.move(10, 20, 0);
         return wizard;
     }
 }
