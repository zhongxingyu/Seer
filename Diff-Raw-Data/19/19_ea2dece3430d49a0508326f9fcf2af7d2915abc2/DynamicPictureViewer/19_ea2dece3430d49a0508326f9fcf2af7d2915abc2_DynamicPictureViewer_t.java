 /**
  * 
  */
 
 import java.awt.Graphics;
 import java.awt.Panel;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.nio.file.FileSystems;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 import loaders.ILoader;
 
 import brahma.host.GUIPlugin;
 
 /**
  * @author risdenkj
  *
  */
 public class DynamicPictureViewer extends GUIPlugin implements MouseListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private ArrayList<ILoader> loaders;
 	private BufferedImage image;
 	private Random randomGenerator;
 	private URLClassLoader classLoader;
 	private HashMap<Path, ILoader> pathToLoader;
 
 	private Path pluginDir = FileSystems.getDefault().getPath("plugins/DynamicPictureViewer");
 	
 	public DynamicPictureViewer() throws Exception {
 		super();
 		this.randomGenerator = new Random();
 		this.loaders = new ArrayList<ILoader>();
 		this.pathToLoader = new HashMap<Path, ILoader>();
 		try {
 			File pluginFolder = pluginDir.toFile();
 			File[] files = pluginFolder.listFiles();
 			if(files != null) {
 				for(File f : files) {
 					this.addLoader(f.toPath());
 				}
 			}
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 		if(this.loaders.size() == 0) {
 			throw new Exception("No Picture Loaders Found");
 		}
 		this.image = this.getLoader().getImage();
 		addMouseListener(this);
 	}
 	
 	public ILoader getLoader() {
 		return this.loaders.get(this.randomGenerator.nextInt(this.loaders.size()));
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		this.image = this.getLoader().getImage();
 		this.repaint();
 	}
 	
 	public void paint(Graphics g) {
 		int newImageHeight = this.image.getHeight();
 		int newImageWidth = this.image.getWidth();
 		if (this.image.getHeight() >= this.image.getWidth() && this.image.getHeight() > getHeight()) {
 			newImageHeight = getHeight();
 			newImageWidth = Math.round((getHeight()/((float)this.image.getHeight()))*this.image.getWidth());
 		} else if(this.image.getHeight() < this.image.getWidth() && image.getWidth() > getWidth()) {
 			newImageHeight = Math.round((getWidth()/((float)this.image.getWidth()))*this.image.getHeight());
 			newImageWidth = getWidth();
 		}
 		g.drawImage(this.image, getWidth()/2-newImageWidth/2, getHeight()/2-newImageHeight/2, newImageWidth, newImageHeight, null);
 	}
 
 	@Override
 	public Boolean start() {
 		JFrame frame = new JFrame("Display image");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Panel panel;
 		try {
			panel = this;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 		frame.setSize(500, 500);
 		frame.getContentPane().add(panel);
 		frame.setVisible(true);
 		return true;
 	}
 	
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
             public void run() {
 				try {
 					DynamicPictureViewer pv = new DynamicPictureViewer();
 					pv.start();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
             }
         });
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 	}
 
 	public void addLoader(Path loaderPath) throws Exception {
 		System.out.println("Add loader - " + loaderPath);
 		// Get hold of the jar file
 		File jarBundle = loaderPath.toFile();
 		JarFile jarFile = new JarFile(jarBundle);
 		
 		// Get the manifest file in the jar file
 		Manifest mf = jarFile.getManifest();
         Attributes mainAttribs = mf.getMainAttributes();
         
         // Get hold of the Plugin-Class attribute and load the class
         String className = mainAttribs.getValue("Plugin-Class");
         URL[] urls = new URL[]{loaderPath.toUri().toURL()};
         classLoader = new URLClassLoader(urls);
         Class<?> loaderClass = classLoader.loadClass(className);
         // Create a new instance of the plugin class and add to the core
         ILoader loader = (ILoader)loaderClass.newInstance();;
         this.loaders.add(loader);
         this.pathToLoader.put(loaderPath, loader);
 
         // Release the jar resources
         jarFile.close();
 	}
 }
