 package ex3.render.raytrace;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 
 import ex3.parser.Element;
 import ex3.parser.SceneDescriptor;
 import ex3.render.IRenderer;
 import math.Ray;
 
 public class RayTracer implements IRenderer {
 
     protected int width;
     protected int height;
     protected Scene scene;
     protected Camera camera;
 
     /**
      * Inits the renderer with scene description and sets the target canvas to
      * size (width X height). After init renderLine may be called
      *
      * @param sceneDesc
      *            Description data structure of the scene
      * @param width
      *            Width of the canvas
      * @param height
      *            Height of the canvas
      * @param path
      *            File path to the location of the scene. Should be used as a
      *            basis to load external resources (e.g. background image)
      */
     @Override
     public void init(SceneDescriptor sceneDesc, int width, int height, File path) {
         this.width = width;
         this.height = height;
         scene = new Scene();
         camera = new Camera(width, height);
 
         scene.init(sceneDesc.getSceneAttributes());
         camera.init(sceneDesc.getCameraAttributes());
 
         scene.setCamera(camera);
 
         for (Element e : sceneDesc.getObjects()) {
             scene.addObjectByName(e.getName(), e.getAttributes());
         }
     }
 
     /**
      * Renders the given line to the given canvas. Canvas is of the exact size
      * given to init. This method must be called only after init.
      *
      * @param canvas
      *            BufferedImage containing the partial image
      * @param line
      *            The line of the image that should be rendered.
      */
     @Override
     public void renderLine(BufferedImage canvas, int line) {
 
         int y = line;
         for (int x = 0; x < width; ++x) {
            canvas.setRGB(x, y, castRay(width - x - 1, height - y - 1).getRGB());
         }
 
     }
 
     /**
      * Compute color for given image coordinates (x,y)
      *
      * @param x the x coordinate of the view port
      * @param y the y coordinate of the view port
      * @return Color at coordinate
      */
     protected Color castRay(int x, int y) {
         Ray ray = scene.camera.constructRayThroughPixel(x, y);
         Hit hit = scene.findIntersection(ray);
         return scene.calcColor(hit, ray);
     }
 }
