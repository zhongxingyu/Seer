 package globals;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Image;
 import java.awt.image.*;
 import java.io.File;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import gnu.expr.ModuleMethod;
 import gnu.mapping.*;
 import gnu.math.IntNum;
 import util.FileAccess;
 import util.KawaWrap;
 
 public class WImage extends Globals {
 	/**
 	 * Add methods related to (random n)
 	 * 
 	 * @param kawa The interpreter to add them to.
 	 * @throws Throwable If anything breaks while adding them.
 	 */
 	@Override
 	public void addMethods(KawaWrap kawa) throws Throwable {
 		/**
 		 * Custom image class to help without output. 
 		 */
 		class ImageShell {
 			RenderedImage Data;
 			public ImageShell(RenderedImage img) {
 				Data = img;
 			}
 			
 			public String toString() {
 				return "[image " + Data.getHeight() + " " + Data.getWidth() + "]";
 			}
 		}
 		
 		/**
 		 * Custom color class to help with output.
 		 */
 		class ColorShell {
 			Color Data;
 			public ColorShell(Color c) {
 				Data = c;
 			}
 			
 			public String toString() {
 				return "[color " + Data.getRed() + " " + Data.getGreen() + " " + Data.getBlue() + "]";
 			}
 		}
 		
 		/* ----- ----- ----- ----- ----- 
 		 *             color
 		 * ----- ----- ----- ----- ----- */
 		
 		// Create a new color.
 		kawa.bind(new Procedure3("color") {
 			public Object apply3(Object r, Object g, Object b) throws Throwable {
 				if (!(r instanceof IntNum)) throw new IllegalArgumentException("Error in color: " + r + " is not an integer.");
 				if (!(g instanceof IntNum)) throw new IllegalArgumentException("Error in color: " + g + " is not an integer.");
 				if (!(b instanceof IntNum)) throw new IllegalArgumentException("Error in color: " + b + " is not an integer.");
 				
 				return new ColorShell(new Color(((IntNum) r).ival, ((IntNum) g).ival, ((IntNum) b).ival));
 			}
 		});
 		
 		// Check if a given item is a color.
 		kawa.bind(new Procedure1("color?") {
 			public Object apply1(Object p) throws Throwable {
 				return p instanceof ColorShell;
 			}
 		});
 		
 		// Check if two colors are equal.
 		kawa.bind(new Procedure2("color-equal?") {
 			public Object apply2(Object c1, Object c2) throws Throwable {
 				return (c1 instanceof ColorShell && c2 instanceof ColorShell && ((ColorShell) c1).Data.equals(((ColorShell) c2).Data));
 			}
 		});
 		
 		// Pull channels out of a color.
 		kawa.bind(new Procedure2("color-ref") {
 			public Object apply2(Object p, Object b) throws Throwable {
 				if (!(p instanceof ColorShell)) throw new IllegalArgumentException("Error in color-ref: " + p + " is not a color.");
 				if (!(b instanceof Symbol)) throw new IllegalArgumentException("Error in color-ref: " + b + " must be 'red, 'green, or 'blue.");
 				
 				String channel = ((Symbol) b).getName();
 				if ("red".equals(channel)) return new IntNum(((ColorShell) p).Data.getRed());
 				else if ("green".equals(channel)) return new IntNum(((ColorShell) p).Data.getGreen());
 				else if ("blue".equals(channel)) return new IntNum(((ColorShell) p).Data.getBlue());
 				else throw new IllegalArgumentException("Error in color-ref: " + b + " must be 'red, 'green, or 'blue.");
 			}
 		});
 		
 		/* ----- ----- ----- ----- ----- 
 		 *             image
 		 * ----- ----- ----- ----- ----- */
 		
 		// Test if something is an image.
 		kawa.bind(new Procedure1("image?") {
 			public Object apply1(Object img) throws Throwable {
 				return img instanceof ImageShell;
 			}
 		});
 		
 		// Check if two images are equal.
 		kawa.bind(new Procedure2("image-equal?") {
 			public Object apply2(Object img1, Object img2) throws Throwable {
 				return (img1 instanceof ImageShell && img2 instanceof ImageShell && ((ImageShell) img1).Data.equals(((ImageShell) img2).Data));
 			}
 		});
 		
 		// Get the height of an image.
 		kawa.bind(new Procedure1("image-rows") {
 			public Object apply1(Object img) throws Throwable {
 				if (!(img instanceof ImageShell)) throw new IllegalArgumentException("Error in image-rows: " + img + " is not an image.");
 				return new IntNum(((ImageShell) img).Data.getHeight());
 			}
 		});
 		
 		// Get the width of an image.
 		kawa.bind(new Procedure1("image-cols") {
 			public Object apply1(Object img) throws Throwable {
 				if (!(img instanceof ImageShell)) throw new IllegalArgumentException("Error in image-cols: " + img + " is not an image.");
 				return new IntNum(((ImageShell) img).Data.getWidth());
 			}
 		});
 		
 		// Pull a pixel out of an image.
 		kawa.bind(new Procedure3("image-ref") {
 			public Object apply3(Object img, Object r, Object c) throws Throwable {
 				if (!(img instanceof ImageShell)) throw new IllegalArgumentException("Error in image-ref: " + img + " is not an image.");
 				if (!(r instanceof IntNum)) throw new IllegalArgumentException("Error in image-ref: " + img + " is not an integer.");
 				if (!(c instanceof IntNum)) throw new IllegalArgumentException("Error in image-ref: " + img + " is not an integer.");
 				
 				return new ColorShell(new Color(
 					((ImageShell) img).Data.getData().getSample(((IntNum) c).ival, ((IntNum) r).ival, 0),
 					((ImageShell) img).Data.getData().getSample(((IntNum) c).ival, ((IntNum) r).ival, 1),
 					((ImageShell) img).Data.getData().getSample(((IntNum) c).ival, ((IntNum) r).ival, 2)
 				));
 			}
 		});
 		
 		// Set a pixel in an image (converts it internally to a writable image if it isn't already).
 		kawa.bind(new Procedure4("image-set!") {
 			public Object apply4(Object img, Object r, Object c, Object p) throws Throwable {
 				if (!(img instanceof ImageShell)) throw new IllegalArgumentException("Error in image-set!: " + img + " is not an image.");
 				if (!(r instanceof IntNum)) throw new IllegalArgumentException("Error in image-set!: " + img + " is not an integer.");
 				if (!(c instanceof IntNum)) throw new IllegalArgumentException("Error in image-set!: " + img + " is not an integer.");
 				if (!(p instanceof ColorShell)) throw new IllegalArgumentException("Error in image-set!: " + img + " is not a color.");
 				
 				if (!(((ImageShell) img).Data instanceof BufferedImage))
 					((ImageShell) img).Data = new BufferedImage(
 						((ImageShell) img).Data.getWidth(), 
 						((ImageShell) img).Data.getHeight(),
 						BufferedImage.TYPE_INT_RGB
 					);
 				
 				WritableRaster raster = ((BufferedImage) ((ImageShell) img).Data).getRaster();
 				raster.setSample(((IntNum) c).ival, ((IntNum) r).ival, 0, ((ColorShell) p).Data.getRed());
 				raster.setSample(((IntNum) c).ival, ((IntNum) r).ival, 1, ((ColorShell) p).Data.getGreen());
 				raster.setSample(((IntNum) c).ival, ((IntNum) r).ival, 2, ((ColorShell) p).Data.getBlue());
 				
 				return null;
 			}
 		});
 		
 		// Read an image from a file.
 		kawa.bind(new Procedure1("read-image") {
 			public Object apply1(Object filename) throws Throwable {
 				if (!(filename instanceof String)) throw new IllegalArgumentException("Error in read-image: " + filename + " is not a string.");
 				
 				return new ImageShell(ImageIO.read(new File((String) filename)));
 			}
 		});
 		
 		// Write an image to a file.
 		kawa.bind(new Procedure2("write-image") {
 			public Object apply2(Object img, Object filename) throws Throwable {
 				if (!(img instanceof ImageShell)) throw new IllegalArgumentException("Error in write-image: " + img + " is not an image.");
 				if (!(filename instanceof String)) throw new IllegalArgumentException("Error in write-image: " + filename + " is not a string.");
 				
 			    File outputFile = new File((String) filename);
 			    ImageIO.write(((ImageShell) img).Data, FileAccess.extension(outputFile.getName()), outputFile);
 			    
 			    return null;
 			}
 		});
 		
 		// Draw an image to the screen.
 		kawa.bind(new Procedure1("draw-image") {
 			public Object apply1(Object img) throws Throwable {
 				if (!(img instanceof ImageShell)) throw new IllegalArgumentException("Error in draw-image: " + img + " is not an image.");
 				
 				JFrame treeFrame = new JFrame("draw-image");
 				treeFrame.setLayout(new BorderLayout());
 				treeFrame.setResizable(false);
 				treeFrame.setLocationByPlatform(true);
 				treeFrame.add(new JLabel(new ImageIcon((Image) ((ImageShell) img).Data)));
 				treeFrame.pack();
 				treeFrame.setVisible(true);
 				
 				return null;
 			}
 		});
 		
 		// Map a procedure (lambda (color) ...) over an image.
 		kawa.bind(new Procedure2("image-map") {
			public Object apply2(Object img, Object proc) throws Throwable {
 				if (!(img instanceof ImageShell)) throw new IllegalArgumentException("Error in image-map: " + img + " is not an image.");
 				if (!(proc instanceof ModuleMethod)) throw new IllegalArgumentException("Error in image-map: " + proc + " is not a procedure.");
 				
 				int rows = ((ImageShell) img).Data.getHeight();
 				int cols = ((ImageShell) img).Data.getWidth();
 				
 				Raster oRaster = ((ImageShell) img).Data.getData();
 				
 				BufferedImage image = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_RGB);
 				WritableRaster raster = image.getRaster();
 				for (int r = 0; r < rows; r++)
 				{
 					for (int c = 0; c < cols; c++)
 					{
 						ColorShell oldColor = new ColorShell(new Color(
 							oRaster.getSample(c, r, 0),
 							oRaster.getSample(c, r, 1),
 							oRaster.getSample(c, r, 2)
 						));
 								
 						Object newColor = ((ModuleMethod) proc).apply1(oldColor);
 						if (!(newColor instanceof ColorShell)) throw new IllegalArgumentException("Error in image-map: " + newColor + " is not a color.");
 						
 						raster.setSample(c, r, 0, ((ColorShell) newColor).Data.getRed());
 						raster.setSample(c, r, 1, ((ColorShell) newColor).Data.getGreen());
 						raster.setSample(c, r, 2, ((ColorShell) newColor).Data.getBlue());
 					}
 				}
 				
 				return new ImageShell((RenderedImage) image);
 			}
 		});
 		
 		// Create a new image of a given size, using either a color or (lambda (int int int int) ...).
 		kawa.bind(new Procedure3("$make-image$") {
 			public Object apply3(Object rows, Object cols, Object proc) throws Throwable {
 				if (!(rows instanceof IntNum)) throw new IllegalArgumentException("Error in make-image: " + rows + " is not an integer.");
 				if (!(cols instanceof IntNum)) throw new IllegalArgumentException("Error in make-image: " + cols + " is not an integer.");
 				if (!(proc instanceof ModuleMethod || proc instanceof ColorShell)) throw new IllegalArgumentException("Error in make-image: " + proc + " is neither a color or a procedure.");
 				
 				BufferedImage image = new BufferedImage(((IntNum) cols).ival, ((IntNum) rows).ival, BufferedImage.TYPE_INT_RGB);
 				WritableRaster raster = image.getRaster();
 				Object color;
 				for (int r = 0; r < ((IntNum) rows).ival; r++)
 				{
 					for (int c = 0; c < ((IntNum) cols).ival; c++)
 					{
 						if (proc instanceof ModuleMethod)
 							color = ((ModuleMethod) proc).apply4(rows, cols, new IntNum(r), new IntNum(c));
 						else 
 							color = proc;
 						if (!(color instanceof ColorShell)) throw new IllegalArgumentException("Error in make-image: " + color + " is not a color.");
 						
 						raster.setSample(c, r, 0, ((ColorShell) color).Data.getRed());
 						raster.setSample(c, r, 1, ((ColorShell) color).Data.getGreen());
 						raster.setSample(c, r, 2, ((ColorShell) color).Data.getBlue());
 					}
 				}
 				
 				return new ImageShell((RenderedImage) image);
 			}
 		});
 		kawa.eval("(define make-image (case-lambda ((rows cols) ($make-image$ rows cols (color 0 0 0))) ((rows cols proc) ($make-image$ rows cols proc))))");
 		
 		// Helper to load and then draw an image.
 		kawa.eval("(define (draw-image-file filename) (draw-image (read-image filename)))");
 		
 		// Several predefined colors.
 		kawa.eval("(define black (color 0 0 0))");
 		kawa.eval("(define darkgray (color 84 84 84))");
 		kawa.eval("(define gray (color 192 192 192))");
 		kawa.eval("(define lightgray (color 205 205 205))");
 		kawa.eval("(define white (color 255 255 255))");
 		kawa.eval("(define red (color 255 0 0))");
 		kawa.eval("(define green (color 0 255 0))");
 		kawa.eval("(define blue (color 0 0 255))");
 		kawa.eval("(define yellow (color 255 255 0))");
 		kawa.eval("(define cyan (color 0 255 255))");
 		kawa.eval("(define magenta (color 255 0 255))");
 		kawa.eval("(define orange (color 255 127 0))");
 		kawa.eval("(define pink (color 188 143 143))");
 	}
 }
