 import java.awt.Color;
 import java.util.ArrayList;
 
 import se.lth.cs.ptdc.fractal.MandelbrotGUI;
 
 /** Render a mandlebrot in the complex plane */
 public class Generator {
 	public static final int DEFAULT_ITERATIONS = 200,
 							MAX_COLOR = 255,
 							LIMIT_SQUARED = (int) Math.pow(2,2);
 	
 	/* Generate a color spectrum. 
 	 * Do this in a static block to avoid regenerating the color set every object initiation.
 	 */
 	public static final ArrayList<Color> COLORS;
 	static{
 		ArrayList<Color> colors = new ArrayList<Color>(MAX_COLOR);
 		
 		/* 'palette' is a color scheme. It iterates from the first rgb color
 		 * and adds the colors in between to the ArrayList called 'colors'
 		 */
 		Color[] palette = { new Color(255, 0, 0), 	// RED
 							new Color(0, 255, 0 ), 	// GREEN
 							new Color(0, 0, 255 ), 	// BLUE
 							new Color(255, 255, 0), // YELLOW
 							new Color(0,0,0)};		// BLACK
 	
 		/* The color will iterate 'c_iterations' amount of times before reaching the next color.
 		 * Example: 255(5-1) = 63.75. It will go change its value 127 times from 
 		 * 255, 0, 0 before reaching the next color 0, 255, 0
 		 */
 		double c_iterations = (double)MAX_COLOR/(palette.length-1); 
 		for (int j = 0; j < palette.length - 1; j++) {
 			Color c0 = palette[j],
 				  c1 = palette[j+1];
 			
 			// The red, green and blue change-factors from one color to the next.
 			double dr = (c1.getRed() - c0.getRed())/c_iterations,
 				   dg = (c1.getGreen() - c0.getGreen())/c_iterations,
 				   db = (c1.getBlue() - c0.getBlue())/c_iterations;
 			
 			/* Add 'c_iteration' amount of colors to the 'colors'-List. 
 			 * Start with the old color and gradually change it to the new color.
 			 */
 			for (int i = 0; i < c_iterations; i++) {
 				colors.add(new Color(
 						c0.getRed() + (int)(i*dr), 
 						c0.getGreen() + (int)(i*dg), 
 						c0.getBlue() + (int)(i*db)
 				));
 			}
 		}
 		COLORS = colors;
 	}
 	
 	/** Draw an image in the window gui */
 	public void render(MandelbrotGUI gui) {
 		
 		// Disable GUI input during rendering (might take a while)
 		gui.disableInput();
 		
 		/* Get number of iterations from extra parameter field if available
 		 * Default is DEFAULT_ITERATIONS
 		 */
 		String extra = gui.getExtraText();
 		int iterations = DEFAULT_ITERATIONS;
 		if (!extra.equals("")) {
 			iterations = Integer.parseInt(gui.getExtraText());
 		}
 		
 		boolean colorMode = gui.getMode() == MandelbrotGUI.MODE_COLOR;
 
 		//calculate resolution ratio based on user preference in GUI 
 		int resolution = MandelbrotGUI.RESOLUTION_VERY_HIGH / gui.getResolution();
 
 		/* With this ratio, calculate grid size. 
 		 * While the number of pixels on the grid are the same regardless of resolution, 
 		 * the number of points we plot on should vary
 		 */
 		int width = gui.getWidth()/resolution;
 		int height = gui.getHeight()/resolution;
 		
 		
 		//Get complex plane
 		Complex[][] complexMatrix = mesh(gui.getMinimumReal(), gui.getMaximumReal(),
 								   gui.getMinimumImag(), gui.getMaximumImag(),
 								   width, height);
 		
 		//Render the actual picture (matrix with color objects)
 		Color[][] picture = new Color[height][width];
 		
 		//Go through all complex number (left to right, top to bottom)
 		for (int y = 0; y < height; y++) {
 			for (int x = 0; x < width; x++) {
 				Complex c = complexMatrix[y][x];
 				Color color = Color.black;
 				
 				/* Run the mandelbrot algorithm with a 
 				 * maximum of "iterations" times and set color.
 				 * If the complex is within the mandelbrot set, the color is black.
 				 * Else the color is based on the how fast 
 				 * the complex diverge (if color mode is enabled).
 				 * If color mode is disabled,
 				 * complex numbers not within the set will be white.
 				 */
 				Complex z = new Complex(0,0);
 				for (int k = 0; k < iterations; k++) {
 					z.mul(z);
 					z.add(c);
 					if(z.getAbs2() > LIMIT_SQUARED){
 						if (colorMode) {
 							
 							/* COLORS is MAX_COLOR long. 
 							 * Because the number of iterations can vary,
 							 * scale k to fit in the color set.
 							 */
							color = COLORS.get((int)((MAX_COLOR/(double)iterations)*k));
 						} else {
 							color = Color.white;
 						}
 						break;
 					}
 				}
 				picture[y][x] = color;
 			}
 		}
 		
 		/* Insert the data to the GUI and release input lock. Start with the upper-left complex number in the plane and 
 		 * gradually change it to the lower-right complex number.
 		 * Add all complex numbers for each pixel in between.
 		*/
 		gui.putData(picture, resolution, resolution);
 		gui.enableInput();
 	}
 	
 	/** Generate and returns a plane of complex numbers in the form of a matrix. */
 	private Complex[][] mesh(double minRe, double maxRe, 
 								double minIm, double maxIm, 
 								int width, int height) 
 	{
 		Complex[][] complex = new Complex[height][width];
 		
 		/* Calculate the change-factor per pixel for the real and 
 		 * imaginary part of the complex plane.
 		 */
 		double rePart = (maxRe - minRe) / width;
 		double imPart = (maxIm - minIm) / height;
 		
 		/* Start with the upper-left complex number in the plane and 
 		 * gradually change it to the lower-right complex number.
 		 * Add all complex numbers for each pixel in between.
 		*/
 		for (int y = 0; y < height; y++) {
 			for (int x = 0; x < width; x++) {
 				complex[y][x] = new Complex(minRe + rePart * x, maxIm - imPart * y);
 			}
 		}
 		return complex;
 	}
 }
