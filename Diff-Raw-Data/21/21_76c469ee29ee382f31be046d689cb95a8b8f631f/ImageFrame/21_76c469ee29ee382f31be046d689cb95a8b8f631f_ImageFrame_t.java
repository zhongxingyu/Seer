 package edu.psu.sweng.kahindu.gui;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 
 import edu.psu.sweng.kahindu.image.KahinduImage;
import edu.psu.sweng.kahindu.image.io.AdvancedImageReader;
import edu.psu.sweng.kahindu.image.io.AdvancedImageWriter;
 import edu.psu.sweng.kahindu.image.io.ByteArrayImageReader;
 import edu.psu.sweng.kahindu.image.io.DefaultImageReader;
 import edu.psu.sweng.kahindu.image.io.DefaultImageWriter;
 import edu.psu.sweng.kahindu.transform.AdditiveTransformer;
 import edu.psu.sweng.kahindu.transform.GrayTransformer;
 import edu.psu.sweng.kahindu.transform.LegacyTransform;
 import edu.psu.sweng.kahindu.transform.LowPassFilter;
 import edu.psu.sweng.kahindu.transform.MedianFilter;
 import edu.psu.sweng.kahindu.transform.NegateTransformer;
 import edu.psu.sweng.kahindu.transform.PowerTransformer;
 import edu.psu.sweng.kahindu.transform.SaltAndPepperTransformation;
 import gui.NumImage;
 
 public class ImageFrame extends JFrame
 {
     private static final long serialVersionUID = 3848669250991405715L;
     
 	private final ImageComponent component;
 
 	private KahinduImage image;
 
 	public ImageFrame() {
 		super("Kahindu Refactor - Team 2");
 		File defaultImage = new File("gifs/baboon.GIF");
 		try {
 			this.image = new ByteArrayImageReader(NumImage.gray).read(defaultImage);
 			//this.image = new GIFReader(new File("gifs/baboon.GIF")).read();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		this.component = new ImageComponent(image);
 		this.getContentPane().add(component);
 		this.setJMenuBar(this.getMenu());
 		this.pack();
 	}
 
 	private JMenuBar getMenu()
 	{
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.add(getFileMenu());
 		menuBar.add(getFilterMenu());
 		menuBar.add(getSpatialFilterMenu());
 		
 		return menuBar;
 	}
 	
 	private JMenu getSpatialFilterMenu() {
 		JMenuBuilder builder = new JMenuBuilder("Spatial");
 		
         TransformMenuItemBuilder mi = new TransformMenuItemBuilder(new LowPassFilter(1), component);
         mi.setName("LowPass-Average");
         builder.addItem(mi);
         
         mi = new TransformMenuItemBuilder(new LowPassFilter(2), component);
         mi.setName("LowPass-P1");
         builder.addItem(mi);
 
         mi = new TransformMenuItemBuilder(new LowPassFilter(4), component);
         mi.setName("LowPass-P2");
         builder.addItem(mi);
         
         mi = new TransformMenuItemBuilder(new LowPassFilter(12), component);
         mi.setName("LowPass-P3");
         builder.addItem(mi);
         
         mi = new TransformMenuItemBuilder(new MedianFilter(3,MedianFilter.SQUARE), component);
         mi.setName("Median");
         builder.addItem(mi);
 
         mi = new TransformMenuItemBuilder(new SaltAndPepperTransformation(500), component);
         mi.setName("S&P 500");
         builder.addItem(mi);
 
         mi = new TransformMenuItemBuilder(new LegacyTransform("turn90"), component);
         mi.setName("Turn 90 ");
         builder.addItem(mi);
 
 		return builder.build();
 	}
 
 	private JMenu getFileMenu()
 	{
 		JMenuBuilder file = new JMenuBuilder("File");
 		
 	    // Open
 	    JMenuBuilder openBuilder = new JMenuBuilder("Open");
 	    
 	    AbstractMenuLeaf loadGIF = new OpenMenuItemBuilder(new DefaultImageReader(), component);
 	    loadGIF.setName("Load GIF");
 	    openBuilder.addItem(loadGIF);
 	    
 	    AbstractMenuLeaf loadJPG = new OpenMenuItemBuilder(new DefaultImageReader(), component);
         loadJPG.setName("Load JPG");
         openBuilder.addItem(loadJPG);
         
         AbstractMenuLeaf loadPNG = new OpenMenuItemBuilder(new DefaultImageReader(), component);
         loadPNG.setName("Load PNG");
         openBuilder.addItem(loadPNG);
 	    
        AbstractMenuLeaf loadPPM = new OpenMenuItemBuilder(new AdvancedImageReader(), component);
         loadPPM.setName("Load PPM");
         openBuilder.addItem(loadPPM);
 	    
 	    // Save
         JMenuBuilder saveBuilder = new JMenuBuilder("Save");
         SaveMenuItemBuilder saveGIF = new SaveMenuItemBuilder(new DefaultImageWriter("gif"), component);
         saveGIF.setName("Save GIF");
         saveBuilder.addItem(saveGIF);
         
         SaveMenuItemBuilder saveJPG = new SaveMenuItemBuilder(new DefaultImageWriter("jpg"), component);
         saveJPG.setName("Save JPG");
         saveBuilder.addItem(saveJPG);
         
         SaveMenuItemBuilder savePNG = new SaveMenuItemBuilder(new DefaultImageWriter("png"), component);
         savePNG.setName("Save PNG");
         saveBuilder.addItem(savePNG);
         
        SaveMenuItemBuilder savePPM = new SaveMenuItemBuilder(new AdvancedImageWriter("PNM"), component);
        savePPM.setName("Save PPM");
        saveBuilder.addItem(savePPM);
        
         file.addItem(openBuilder);
         file.addItem(saveBuilder);
 	    
 	    return file.build();
 	}
 	
 	private JMenu getFilterMenu()
 	{
 	    JMenuBuilder builder = new JMenuBuilder("Filters");
 
         TransformMenuItemBuilder negate = new TransformMenuItemBuilder(new NegateTransformer(), component);
         negate.setName("Negate");
         builder.addItem(negate);
 
         TransformMenuItemBuilder gray = new TransformMenuItemBuilder(new GrayTransformer(), component);
         gray.setName("Gray");
         builder.addItem(gray);
 
         TransformMenuItemBuilder add10 = new TransformMenuItemBuilder(new AdditiveTransformer(10), component);
         add10.setName("Add 10");
         builder.addItem(add10);
 
         TransformMenuItemBuilder brighten = new TransformMenuItemBuilder(new PowerTransformer(0.9), component);
         brighten.setName("Brighten");
         builder.addItem(brighten);
 
         TransformMenuItemBuilder darken = new TransformMenuItemBuilder(new PowerTransformer(1.5), component);
         darken.setName("Darken");
         builder.addItem(darken);
         
         return builder.build();
 	}
 
 }
