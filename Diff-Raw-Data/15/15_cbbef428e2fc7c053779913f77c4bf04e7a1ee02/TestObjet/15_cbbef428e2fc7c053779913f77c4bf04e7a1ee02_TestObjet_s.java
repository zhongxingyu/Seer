 package be.ibiiztera.md.pmatrix.test.pushmatrix.newtest;
 
 import be.ibiiztera.md.pmatrix.pushmatrix.*;
 import be.ibiiztera.md.pmatrix.pushmatrix.scripts.Loader;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import starbuck.tests.ShowTestResult;
 
 /**
  *
  * @author Manuel DAHMEN
  */
 public class TestObjet implements Test{
     private int version = 1;
     private String template ="";
     private String type = "png";
     private File file = null;
     private int resx = 1600;
     private int resy = 1200;
     Properties properties = new Properties();
     private File dir = null;
     protected Scene scene;
     protected String description;
    private boolean perspective;
    private double camera;
    private double planproj;
    private Camera c = new Camera(new Point3D(0,0,-1000), Point3D.O0, 0.1);
     @Override
     public Scene scene() {
         return scene;
     }
 
     @Override
     public void init() {
         ResourceBundle bundle1 = ResourceBundle.getBundle("be/ibiiztera/md/pmatrix/test/pushmatrix/newtest/Bundle");
         
         File dir = new File(bundle1.getString("testpath"));
         if(!dir.exists())
             dir.mkdirs();
         this.dir = new File(dir.getAbsolutePath()+File.separator+this.getClass().getName());
         if(!this.dir.exists())
             this.dir.mkdirs();
        
         file = new File(this.dir.getAbsolutePath()+File.separator+bundle1.getString("src")+"."+bundle1.getString("type"));
         
         template = bundle1.getString("template");
         
         properties.put("name", this.getClass().getCanonicalName());
         properties.put("version", version);
         
         resx = Integer.parseInt(bundle1.getString("resx"));
         resy = Integer.parseInt(bundle1.getString("resy")); 
         scene = new Scene();
 
     }
 
     public void setResx(int resx) {
         this.resx = resx;
     }
 
     public void setResy(int resy) {
         this.resy = resy;
     }
 
     public int getResx() {
         return resx;
     }
 
     public int getResy() {
         return resy;
     }
     
     
     /**
      * Definir la scène scene().add(*)
      */
     @Override
     public void testScene() {
     }
     @Override
     public void testScene(File f) {
         if(f.getAbsolutePath().endsWith("mood")||f.getAbsolutePath().endsWith("moo"))
         {
             new Loader().loadIF(f, scene);
         }
         else
         {
             System.err.println("Erreur: extension incorrecte");
             System.exit(1);
     
         }
     }
     @Override
     public void publishResult() 
     {
         new ShowTestResult(getFile()).run();
     }
     /*
     public void setPerspective(boolean b) {
         this.perspective = b;
         this.camera = -100;
     }
     public void setPerspective(double camera, double plan) {
         setPerspective(true);
         this.camera = camera;
         this.planproj = plan;
     }
     * */
     @Override
     public void run() {
         init();
         testScene();
 
         try {
             ZBuffer z = ZBufferFactory.instance(resx, resy);
             z.scene(scene);
             c.calculerMatrice();
             z.camera(c);
             //if(perspective)
                 //z.perspective(this.camera, this.planproj);
             //else
                 //z.isometrique();
             z.dessinerSilhouette3D();
             
             
             BufferedImage ri = z.image();
             Graphics g = ri.getGraphics();
             g.setColor(Color.black);
             g.drawString(description, 0, 1100);
             ImageIO.write(((RenderedImage)ri), type, file);
             System.out.println(file.getAbsolutePath());
             System.out.println(scene.toString());
             
             
             
         } catch (IOException ex) {
             Logger.getLogger(TestObjet.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         publishResult();
     }
 
     @Override
     public String getTemplate() {
         //throw new UnsupportedOperationException("Not supported yet.");
         return "";
     }
 
     @Override
     public String applyTemplate(String template, Properties properties) {
         return "";
         //throw new UnsupportedOperationException("Not supported yet.");
     }
     public File getFile() {
         return file;
     }
 
     public void scene(Scene load) {
         scene = load;
     }
 
     @Override
     public void camera(Camera c) {
         this.c = c;
     }
 
     @Override
     public Camera camera() {
         return c;
     }
 
 }
