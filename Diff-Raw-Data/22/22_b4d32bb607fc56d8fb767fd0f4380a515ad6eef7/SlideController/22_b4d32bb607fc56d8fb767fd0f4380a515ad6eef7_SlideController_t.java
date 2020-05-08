 package uk.ac.shef.tiledpres.controller;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import uk.ac.shef.tiledpres.ImageSlide;
 import uk.ac.shef.tiledpres.Slide;
 import uk.ac.shef.tiledpres.gui.SlideGUI;
 
 import com.sun.star.comp.helper.Bootstrap;
 import com.sun.star.lang.XMultiComponentFactory;
 import com.sun.star.uno.XComponentContext;
 
 public class SlideController
 {
 	public List<Slide> convertFromOpenOffice(File ooPres) throws Exception
 	{
 		List<Slide> slides = new LinkedList<Slide>();
 
 		// get the remote office component context
 		XComponentContext xRemoteContext = Bootstrap.bootstrap();
 		if (xRemoteContext == null)
 			System.err.println("ERROR: Could not bootstrap default Office.");
 
 		XMultiComponentFactory xRemoteServiceManager = xRemoteContext
 				.getServiceManager();
 
 		Object desktop = xRemoteServiceManager.createInstanceWithContext(
 				"com.sun.star.frame.Desktop", xRemoteContext);
 
 		return slides;
 	}
 
 	public List<Slide> loadImageSlides(File dir)
 	{
 		List<File> fNames = new LinkedList<File>();
 
 		if (dir.exists())
 			for (File f : dir.listFiles())
 				if (f.getName().toLowerCase().endsWith(".png"))
 					fNames.add(f);
 
 		if (fNames.size() == 0)
 		{
 			System.err.println("Found no slides, exiting");
 			System.exit(1);
 		}
 
 		Collections.sort(fNames);
 
 		List<Slide> slides = new LinkedList<Slide>();
 		for (int i = 0; i < fNames.size(); i++)
 			try
 			{
 				// slides.add(new ImageSlide(fNames.get(i), fNames.get(i)));
 				slides.add(new ImageSlide(fNames.get(i).getName(), fNames
 						.get(i).toURI().toURL()));
 			}
 			catch (Exception e)
 			{
 				System.err.println("Unable to read: " + fNames.get(i));
 				e.printStackTrace();
 			}
 		return slides;
 	}
 
 	public static void main(String[] args) throws Exception
 	{
 		SlideController cont = new SlideController();
 
 		List<Slide> slides = cont
 				.loadImageSlides(new File(
						"/Users/sat/local/Dropbox/oak/research-presentation/images/"));
 		SlideGUI gui = new SlideGUI(slides);
 		gui.setVisible(true);
 	}
 }
