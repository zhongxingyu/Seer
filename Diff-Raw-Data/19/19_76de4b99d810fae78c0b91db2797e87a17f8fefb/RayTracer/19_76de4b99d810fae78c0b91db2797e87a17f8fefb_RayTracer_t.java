 package RayTracing;
 
 import java.awt.Transparency;
 import java.awt.color.*;
 import java.awt.image.*;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 /**
  *  Main class for ray tracing exercise.
  */
 public class RayTracer {
 
 	public int imageWidth;
 	public int imageHeight;
 	public Scene scene;
 
 	private static String getExtension(String fileName){
 		String extension = "";
 
 		int i = fileName.lastIndexOf('.');
 		if (i > 0) {
 		    extension = fileName.substring(i+1);
 		}
 		return extension;
 	}
 	
 	private static String getName(String fileName){
 		String extension = "";
 
 		int i = fileName.lastIndexOf('.');
 		if (i > 0) {
 		    extension = fileName.substring(0,i);
 		}
 		return extension;
 	}
 	
 	/**
 	 * Runs the ray tracer. Takes scene file, output image file and image size as input.
 	 */
 	public static void main(String[] args) {
 
 		try {
 
 			RayTracer tracer = new RayTracer();
 
                         // Default values:
 			tracer.imageWidth = 500;
 			tracer.imageHeight = 500;
 			tracer.scene = new Scene();
 
 			if (args.length < 2)
 				throw new RayTracerException("Not enough arguments provided. Please specify an input scene file and an output image file for rendering.");
 			
			String mode="one";
 			
 			if(mode.equals("many")){
 			String sceneFolderName=args[0];
 			String outputFolderName=args[1];
 			
 			File sceneFolder=new File(sceneFolderName);
 			
 			if (args.length > 3)
 			{
 				tracer.imageWidth = Integer.parseInt(args[2]);
 				tracer.imageHeight = Integer.parseInt(args[3]);
 			}
 			
 			for (File fileEntry : sceneFolder.listFiles()) {
 				if(getExtension(fileEntry.getName()).equals("txt")){
 					String sceneFileName=sceneFolder+"\\"+fileEntry.getName();
 					String outputFileName=outputFolderName+"\\"+getName(fileEntry.getName())+".png";
 					// Parse scene file:
 					tracer.parseScene(sceneFileName);
 
 					// Render scene:
 					tracer.renderScene(outputFileName);
 					
 					tracer.scene=new Scene();
 				}
 		    }
 			}
 			if(mode.equals("one")){
 			String sceneFileName = args[0];
 			String outputFileName = args[1];
 
 			if (args.length > 3)
 			{
 				tracer.imageWidth = Integer.parseInt(args[2]);
 				tracer.imageHeight = Integer.parseInt(args[3]);
 			}
 
 
 			// Parse scene file:
 			tracer.parseScene(sceneFileName);
 
 			// Render scene:
 			tracer.renderScene(outputFileName);
 			}
 
 //		} catch (IOException e) {
 //			System.out.println(e.getMessage());
 		} catch (RayTracerException e) {
 			System.out.println(e.getMessage());
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 
 
 	}
 
 	/**
 	 * Parses the scene file and creates the scene. Change this function so it generates the required objects.
 	 */
 	public void parseScene(String sceneFileName) throws IOException, RayTracerException
 	{
 		FileReader fr = new FileReader(sceneFileName);
 
 		BufferedReader r = new BufferedReader(fr);
 		String line = null;
 		int lineNum = 0;
 		System.out.println("Started parsing scene file " + sceneFileName);
 
 
 
 		while ((line = r.readLine()) != null)
 		{
 			line = line.trim();
 			++lineNum;
 
 			if (line.isEmpty() || (line.charAt(0) == '#'))
 			{  // This line in the scene file is a comment
 				continue;
 			}
 			else
 			{
 				String code = line.substring(0, 3).toLowerCase();
 				// Split according to white space characters:
 				String[] params = line.substring(3).trim().toLowerCase().split("\\s+");
 
 				if (code.equals("cam"))
 				{
                     // parse camera parameters
 					Camera camera=new Camera();
 					camera.setPosition(params[0], params[1], params[2]);
 					camera.setLookAtPoint(params[3], params[4], params[5]);
 					camera.setUpVector(params[6], params[7], params[8]);
 					camera.setScreenDistance(params[9]);
 					camera.setScreenWidth(params[10]);
 					
 					scene.addCamera(camera);
 					
 					Screen screen=camera.getScreenFromCamera(imageWidth, imageHeight);
 					
 					scene.addScreen(screen);
 
 					System.out.println(String.format("Parsed camera parameters (line %d)", lineNum));
 				}
 				else if (code.equals("set"))
 				{
                    //parse general settings parameters
 					Settings settings=new Settings();
 					settings.setBackgroundColor(params[0], params[1], params[2]);
 					settings.setRootNumberOfShadowRays(params[3]);
 					settings.setMaximumNumberOfRecursions(params[4]);
 					
 					scene.addSettings(settings);
 					
 					System.out.println(String.format("Parsed general settings (line %d)", lineNum));
 				}
 				else if (code.equals("mtl"))
 				{
                     // parse material parameters
 					Material material=new Material();
 					material.setDiffuseColor(params[0], params[1], params[2]);
 					material.setSpecularColor(params[3], params[4], params[5]);
 					material.setReflectionColor(params[6], params[7], params[8]);
 					material.setPhongSpecularityCoefficient(params[9]);
 					material.setTransparencyValue(params[10]);
 					if(params.length>=12)
 						material.setIncidence(params[11]);
 					else
 						material.setIncidence("0");
 					
 					scene.addMaterial(material);
 
 					System.out.println(String.format("Parsed material (line %d)", lineNum));
 				}
 				else if (code.equals("sph"))
 				{
                     // parse sphere parameters
 
 					Sphere sphere = new Sphere();
                     sphere.setCenter(params[0], params[1], params[2]);
                     sphere.setRadius(params[3]);
                     sphere.setMaterialIndex(params[4]);
                     
                     scene.addSphere(sphere);
 
 					System.out.println(String.format("Parsed sphere (line %d)", lineNum));
 				}
 				else if (code.equals("pln"))
 				{
                     //parse plane parameters
 					Plane plane = new Plane();
 					plane.setNormal(params[0], params[1], params[2]);
 					plane.setOffset(params[3]);
 					plane.setMaterialIndex(params[4]);
 					
 					scene.addPlane(plane);
 
 					System.out.println(String.format("Parsed plane (line %d)", lineNum));
 				}
 				else if (code.equals("lgt"))
 				{
                    //parse light parameters
 					Light light=new Light();
 					light.setPosition(params[0], params[1], params[2]);
 					light.setColor(params[3], params[4], params[5]);
 					light.setSpecularIntensity(params[6]);
 					light.setShadowIntensity(params[7]);
 					light.setRadius(params[8]);
 					
 					scene.addLight(light);
 
 					System.out.println(String.format("Parsed light (line %d)", lineNum));
 				}
 				else
 				{
 					System.out.println(String.format("ERROR: Did not recognize object: %s (line %d)", code, lineNum));
 				}
 			}
 		}
 		scene.setMaterials();
                 // It is recommended that you check here that the scene is valid,
                 // for example camera settings and all necessary materials were defined.
 
 		System.out.println("Finished parsing scene file " + sceneFileName);
 		r.close();
 
 	}
 
 	/**
 	 * Renders the loaded scene and saves it to the specified file location.
 	 */
 	public void renderScene(String outputFileName)
 	{
 		long startTime = System.currentTimeMillis();
 
 		// Create a byte array to hold the pixel data:
 		byte[] rgbData = new byte[this.imageWidth * this.imageHeight * 3];
 
 
                 // Put your ray tracing code here!
                 //
                 // Write pixel color values in RGB format to rgbData:
                 // Pixel [x, y] red component is in rgbData[(y * this.imageWidth + x) * 3]
                 //            green component is in rgbData[(y * this.imageWidth + x) * 3 + 1]
                 //             blue component is in rgbData[(y * this.imageWidth + x) * 3 + 2]
                 //
                 // Each of the red, green and blue components should be a byte, i.e. 0-255
 		
 		ColorComputation cc=new ColorComputation(scene);
 		long precentsPrevious=-1;
 		for(int x=0; x<imageWidth; x++){
 			for(int y=0; y<imageHeight; y++){
 				Color c=cc.getColorOfPixel(x, y);
 				rgbData[(y*imageWidth+x)*3]=Color.colorComponentToByte(c.r());
 				rgbData[(y*imageWidth+x)*3+1]=Color.colorComponentToByte(c.g());
 				rgbData[(y*imageWidth+x)*3+2]=Color.colorComponentToByte(c.b());
 				//System.out.println("Finished rendering (x,y)=("+x+","+y+") out of width="+imageWidth+" and height="+imageHeight+"\n");
 				long endTime = System.currentTimeMillis();
 				Long renderTime = endTime - startTime;
 				double iters=x*imageWidth+y+1;
 				double averageForIteration=renderTime/iters;
 				double itersToGo=imageWidth*imageHeight-iters;
 				long timeLeft=Math.round((averageForIteration*itersToGo)/1000);
 				if(timeLeft==0)
 					timeLeft=0;
 				//System.out.println("Estimated time to finish: " + timeLeft + " seconds");
 				long precents=Math.round(100*iters/(imageWidth*imageHeight));
 				if(precents>precentsPrevious)
 					System.out.println("Precents complete: " + precents);
 				precentsPrevious=precents;
 				/*try {
 					Runtime.getRuntime().exec("cmd /c cls");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}*/
 			}
 		}
 
 		long endTime = System.currentTimeMillis();
 		Long renderTime = endTime - startTime;
 
                 // The time is measured for your own conveniece, rendering speed will not affect your score
                 // unless it is exceptionally slow (more than a couple of minutes)
 		System.out.println("Finished rendering scene in " + renderTime.toString() + " milliseconds.");
 
                 // This is already implemented, and should work without adding any code.
 		saveImage(this.imageWidth, rgbData, outputFileName);
 
 		System.out.println("Saved file " + outputFileName);
 
 	}
 
 
 
 
 	//////////////////////// FUNCTIONS TO SAVE IMAGES IN PNG FORMAT //////////////////////////////////////////
 
 	/*
 	 * Saves RGB data as an image in png format to the specified location.
 	 */
 	public static void saveImage(int width, byte[] rgbData, String fileName)
 	{
 		try {
 
 			BufferedImage image = bytes2RGB(width, rgbData);
 			ImageIO.write(image, "png", new File(fileName));
 
 		} catch (IOException e) {
 			System.out.println("ERROR SAVING FILE: " + e.getMessage());
 		}
 
 	}
 
 	/*
 	 * Producing a BufferedImage that can be saved as png from a byte array of RGB values.
 	 */
 	public static BufferedImage bytes2RGB(int width, byte[] buffer) {
 	    int height = buffer.length / width / 3;
 	    ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
 	    ColorModel cm = new ComponentColorModel(cs, false, false,
 	            Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
 	    SampleModel sm = cm.createCompatibleSampleModel(width, height);
 	    DataBufferByte db = new DataBufferByte(buffer, width * height);
 	    WritableRaster raster = Raster.createWritableRaster(sm, db, null);
 	    BufferedImage result = new BufferedImage(cm, raster, false, null);
 
 	    return result;
 	}
 
 	public static class RayTracerException extends Exception {
 		public RayTracerException(String msg) {  super(msg); }
 	}
 
 
 }
