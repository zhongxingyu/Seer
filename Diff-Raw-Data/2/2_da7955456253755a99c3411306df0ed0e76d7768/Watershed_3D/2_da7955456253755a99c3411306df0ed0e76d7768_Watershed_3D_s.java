 package inra.watershed.plugin;
 
 /**
  *
  * License: GPL
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  *
  * Authors: Ignacio Arganda-Carreras, Philippe Andrey, Axel Poulet
  */
 
 import ij.IJ;
 import ij.ImageJ;
 import ij.ImagePlus;
 import ij.WindowManager;
 import ij.gui.GenericDialog;
 import ij.plugin.PlugIn;
 import ij.process.ImageProcessor;
 import inra.watershed.process.ComponentLabelling;
 import inra.watershed.process.RegionalMinimaFilter;
 import inra.watershed.process.WatershedTransform3D;
 
 
 /**
  * Watershed 3D
  *
  * A plugin to perform watershed on a 3D image.
  *
  * @author Ignacio Arganda-Carreras
  */
 public class Watershed_3D implements PlugIn 
 {
 	
 	public boolean usePriorityQueue = false;
 
 	/**
 	 * Apply 3D watershed to a 2D or 3D image (it does work for 2D images too).
 	 *
 	 *
 	 * @param input the 2D or 3D image (in principle a "gradient" image)
 	 * @param seed the image to calculate the seeds from (it can be the same as the input or another one)
 	 */
 	public ImagePlus process(
 			ImagePlus input, 
 			ImagePlus seed) 
 	{
 		final long start = System.currentTimeMillis();
 		
 		IJ.log("-> Running regional minima filter...");
 		
 		RegionalMinimaFilter rmf = new RegionalMinimaFilter( seed );
 		ImagePlus regionalMinima = rmf.apply();
 		
 		//regionalMinima.show();
 		
 		final long step1 = System.currentTimeMillis();
 		
 		IJ.log( "Regional minima took " + (step1-start) + " ms.");
 		
 		IJ.log("-> Running connected components...");
 		
 		ComponentLabelling cl = new ComponentLabelling( regionalMinima );
 		ImagePlus connectedMinima = cl.apply();
 		
 		//connectedMinima.show();
 		
 		final long step2 = System.currentTimeMillis();
 		IJ.log( "Connected components took " + (step2-step1) + " ms.");
 		
 		IJ.log("-> Running watershed...");
 		
 		WatershedTransform3D wt = new WatershedTransform3D(input, connectedMinima, null);
 		ImagePlus resultImage = usePriorityQueue == false ? wt.apply() : wt.applyWithPriorityQueue();
 		
 		final long end = System.currentTimeMillis();
 		IJ.log( "Watershed 3d took " + (end-step2) + " ms.");
 		IJ.log( "Whole plugin took " + (end-start) + " ms.");
 		
 		return resultImage;
 				
 	}
 	
 	/**
 	 * Apply 3D watershed to a 2D or 3D image (it does work for 2D images too).
 	 *
 	 *
 	 * @param input the 2D or 3D image (in principle a "gradient" image)
 	 * @param seed the image to calculate the seeds from (it can be the same as the input or another one)
 	 * @param mask the mask to constraint the watershed
 	 */
 	public ImagePlus process(
 			ImagePlus input, 
 			ImagePlus seed,
 			ImagePlus mask) 
 	{
 		final long start = System.currentTimeMillis();
 		
 		IJ.log("-> Running regional minima filter...");
 		
 		RegionalMinimaFilter rmf = new RegionalMinimaFilter( seed );
 		if( null != mask )
 			rmf.setMask( mask );
 		ImagePlus regionalMinima = rmf.apply();
 		
 		//regionalMinima.show();
 		
 		final long step1 = System.currentTimeMillis();
 		
 		IJ.log( "Regional minima took " + (step1-start) + " ms.");
 		
 		IJ.log("-> Running connected components...");
 		
 		ComponentLabelling cl = new ComponentLabelling( regionalMinima );
 		ImagePlus connectedMinima = cl.apply();
 		
 		//connectedMinima.show();
 		
 		final long step2 = System.currentTimeMillis();
 		IJ.log( "Connected components took " + (step2-step1) + " ms.");
 		
 		IJ.log("-> Running watershed...");
 		
 		WatershedTransform3D wt = new WatershedTransform3D( input, connectedMinima, mask );
 		ImagePlus resultImage = usePriorityQueue == false ? wt.apply() : wt.applyWithPriorityQueue();
 		
 		final long end = System.currentTimeMillis();
 		IJ.log( "Watershed 3d took " + (end-step2) + " ms.");
 		IJ.log( "Whole plugin took " + (end-start) + " ms.");
 		
 		return resultImage;
 				
 	}
 	
 
 	public void showAbout() {
 		IJ.showMessage("Watershed 3D",
 			"a plugin for 3D watershed"
 		);
 	}
 	
 
 	/**
 	 * Run method needed to work as an ImageJ plugin 
 	 */
 	@Override
 	public void run(String arg0) 
 	{
 		int nbima = WindowManager.getImageCount();
 		
 		if( nbima == 0 )
 		{
 			IJ.error( "Watershed 3D", 
					"At least one image needs to be open to run waterhsed in 3D");
 			return;
 		}
 		
         String[] names = new String[ nbima ];
         String[] namesMask = new String[ nbima + 1 ];
 
         namesMask[ 0 ] = "None";
         
         for (int i = 0; i < nbima; i++) 
         {
             names[ i ] = WindowManager.getImage(i + 1).getShortTitle();
             namesMask[ i + 1 ] = WindowManager.getImage(i + 1).getShortTitle();
         }
         
         GenericDialog gd = new GenericDialog("Watershed 3D");
 
         int spot = 0;
         int seed = nbima > 1 ? 1 : 0;
         
         gd.addChoice( "Input image", names, names[spot] );
         gd.addChoice( "Image to seed from", names, names[seed] );
         gd.addChoice( "Mask", namesMask, namesMask[ nbima > 2 ? 3 : 0 ] );
         gd.addCheckbox( "Use priority queue", usePriorityQueue );
 
         gd.showDialog();
         
         if (gd.wasOKed()) 
         {
             spot = gd.getNextChoiceIndex();
             seed = gd.getNextChoiceIndex();
             int maskIndex = gd.getNextChoiceIndex();
             usePriorityQueue = gd.getNextBoolean();
 
             ImagePlus inputImage = WindowManager.getImage(spot + 1);
             ImagePlus seedImage = WindowManager.getImage(seed + 1);
             ImagePlus maskImage = maskIndex > 0 ? WindowManager.getImage( maskIndex ) : null;
             
             ImagePlus result = process( inputImage, seedImage, maskImage );
             
             // Adjust range to visualize result
             //if( result.getImageStackSize() > 1 )
             //	result.setSlice( result.getImageStackSize()/2 );
             ImageProcessor ip = result.getProcessor();
             ip.resetMinAndMax();
             result.setDisplayRange(ip.getMin(),ip.getMax());
             
             // show result
             result.show();
         }
 
         
 	}
 	
 
 	/**
 	 * Main method for debugging.
 	 *
 	 * For debugging, it is convenient to have a method that starts ImageJ, loads an
 	 * image and calls the plugin, e.g. after setting breakpoints.
 	 *
 	 * @param args unused
 	 */
 	
 	
 	public static void main(String[] args) {
 		// set the plugins.dir property to make the plugin appear in the Plugins menu
 		Class<?> clazz = Watershed_3D.class;
 		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
 		String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
 		System.setProperty("plugins.dir", pluginsDir);
 
 		// start ImageJ
 		new ImageJ();
 
 		// open the Blobs sample
 		ImagePlus image = IJ.openImage("http://imagej.net/images/blobs.gif");
 		// get edges
 		IJ.run(image, "Find Edges", "");
 		image.show();
 
 		// run the plugin
 		IJ.runPlugIn(clazz.getName(), "");
 	}
 	
 	
 }
