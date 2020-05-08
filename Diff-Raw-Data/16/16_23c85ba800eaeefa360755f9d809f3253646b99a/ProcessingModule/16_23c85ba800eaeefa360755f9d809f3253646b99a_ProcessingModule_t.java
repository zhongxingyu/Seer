 /**
  * Class that all modules that want to use processing should extend from.
  * Extend from PApplet, so all normal processing should work with this.
  * Wraps a ModuleHeper in several methods so as to mimic multiple 
  * inheritance.
  *
  * @author	Andrew DeMaria
  * @author	Austin Diviness
  */
 
 package edu.mines.acmX.exhibit.module_manager;
 
 import java.awt.Frame;
 import java.awt.image.BufferedImage;
 import java.io.InputStream;
 import java.util.concurrent.CountDownLatch;
 
 import javax.imageio.ImageIO;
 
 import processing.core.PApplet;
 import processing.core.PConstants;
 import processing.core.PImage;
 
 public abstract class ProcessingModule extends PApplet implements ModuleInterface {
 
 	/**
 	 * Due to the limitations of java in regards to inheritance, ProcessingModule
 	 * uses a ModuleHelper so that it can extend PApplet while keeping the 
 	 * functionality of a Module.
 	 */
     private final ModuleHelper module;
     
     private Frame frame;
     
     public ProcessingModule() {
         super();
         module = new ModuleHelper();
         frame = new Frame();
     }
 
 	/**
 	 * Delegation method, wraps ModuleHelper's setNextModuleToLoad function
 	 * in its own method.
 	 *
 	 * @param	moduleName	Package name of next module to load
 	 */
     public boolean setNextModuleToLoad( String moduleName ) {
         return module.setNextModuleToLoad( moduleName );
     }
 
     /**
      * This function is just a wrapper for ModuleHelper's init function
      *
      * @see		ModuleHelper.java
      *
      * @param   waitForModule   @see ModuleHelper.java
      *
      */
     public void init(CountDownLatch waitForModule) {
     	module.init(waitForModule);
     }
 
     /**
      * This function is just a wrapper for ModuleHelper's finishExecution
      * function
      *
      * @see	ModuleHelper.java
      *
      */
     
     public void finishExecution(){
     	module.finishExecution();
     }
     
     public InputStream loadResourceFromModule( String jarResourcePath, String packageName ) throws ManifestLoadException, ModuleLoadException {
     	return module.loadResourceFromModule(jarResourcePath, packageName);
 	}
 
 	public InputStream loadResourceFromModule( String jarResourcePath ) throws ManifestLoadException, ModuleLoadException {
 		return module.loadResourceFromModule(jarResourcePath);
 	}
     
     /**
      * This function does the dirty work for creating a new Processing window.
      * This will call Processing's init() function which does further Processing
      * specific setup stuff and will finally call this PApplets overridden
      * setup, draw and update function if they exist
      *
      * TODO try to run PApplet without creating a new frame.
      */
     public void execute(){
 		//TODO something smarter should be done with setting the size
     	frame.setSize(500, 500);
     	frame.add(this);
     	frame.setVisible(true);
         super.init();
     }
     
     /**
      * This function overrides the Processing default exit() function.  The
      * reasons for doing so is that Processing will call a System.exit() which
      * will shutdown both the ModuleManager and Module.  Since we do not want
      * this we explicitly call the dispose function to clean up any resources
      * and then call finishExecution to allow ModuleManager to take over.
      *
      */
     @Override
     public void exit() {
     	super.dispose();
     	frame.dispose();
     	this.finishExecution();
     }
     
 	// TODO: check if buffered image supports same image types asProcessing
 	// loadImage function
 	@Override
 	public PImage loadImage(String name) {
		name = "images/" + name;
		//System.out.println(name);
 		try {
			InputStream stream = module.loadResourceFromModule(name, "edu.mines.acmX.exhibit.modules.home_screen");
			//InputStream stream = module.loadResourceFromModule(name);
 			System.out.println("stream: " + stream.toString());
 			BufferedImage buf = ImageIO.read(stream);
 			System.out.println("buffImage: " + buf.toString());
 			return buffImagetoPImage(buf);
 		} catch (Exception e) {
 			System.out.println("Exception was hit: " + e.getClass().toString());
 			return null;
 		}
 	}
 
     private static PImage buffImagetoPImage(BufferedImage bimg) {
 		PImage img = new PImage(bimg.getWidth(), bimg.getHeight(), PConstants.ARGB);
 		bimg.getRGB(0, 0, img.width, img.height, img.pixels, 0, img.width);
 		return img;
 	}
 
 
 
 }
 
 
