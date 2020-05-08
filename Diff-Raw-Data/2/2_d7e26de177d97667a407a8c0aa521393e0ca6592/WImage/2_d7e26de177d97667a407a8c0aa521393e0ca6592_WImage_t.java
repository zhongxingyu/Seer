 package globals;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FileDialog;
 import java.awt.Frame;
 import java.awt.image.*;
 import java.io.File;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import gnu.expr.ModuleMethod;
 import gnu.mapping.*;
 import gnu.math.IntNum;
 import gui.MainFrame;
 import util.KawaWrap;
 import util.files.FileAccess;
 
 public class WImage extends Globals {
     /**
      * Add methods related to (random n)
      * 
      * @param kawa The interpreter to add them to.
      * @throws Throwable If anything breaks while adding them.
      */
     public void addMethods(final KawaWrap kawa) throws Throwable {
 		
 	/* ----- ----- ----- ----- ----- 
 	 *             color
 	 * ----- ----- ----- ----- ----- */
 		
 	// Create a new color.
 	kawa.bind(new Procedure3("color") {
 		public Object apply3(Object r, Object g, Object b) throws Throwable {
 		    int rv, gv, bv;
 
 		    if (r instanceof IntNum) rv = ((IntNum) r).ival;
 		    else if (r instanceof Integer) rv = (Integer) r;
 		    else if (r instanceof Long) rv = ((Long) r).intValue();
 		    else throw new IllegalArgumentException("Error in color: red value " + r + " is not an integer.");
 
 		    if (g instanceof IntNum) gv = ((IntNum) g).ival;
 		    else if (g instanceof Integer) gv = (Integer) g;
 		    else if (g instanceof Long) gv = ((Long) r).intValue();
 		    else throw new IllegalArgumentException("Error in color: green value " + g + " is not an integer.");
 
 		    if (b instanceof IntNum) bv = ((IntNum) b).ival;
 		    else if (b instanceof Integer) bv = (Integer) b;
 		    else if (b instanceof Long) bv = ((Long) b).intValue();
 		    else throw new IllegalArgumentException("Error in color: blue value " + b + " is not an integer.");
 
 		    if (rv < 0 || rv > 255) throw new IllegalArgumentException("Error in color: red value " + rv + " is not in the range [0-255].");
 		    if (gv < 0 || gv > 255) throw new IllegalArgumentException("Error in color: green value " + gv + " is not in the range [0-255].");
 		    if (bv < 0 || bv > 255) throw new IllegalArgumentException("Error in color: blue value " + bv + " is not in the range [0-255].");
 		    
 		    return new Color(rv, gv, bv);
 		}
 	    });
 		
 	// Check if a given item is a color.
 	kawa.bind(new Procedure1("color?") {
 		public Object apply1(Object p) throws Throwable {
 		    return p instanceof Color;
 		}
 	    });
 		
 	// Check if two colors are equal.
 	kawa.bind(new Procedure2("color-equal?") {
 		public Object apply2(Object c1, Object c2) throws Throwable {
 		    return c1.equals(c2);
 		}
 	    });
 		
 	// Pull channels out of a color.
 	kawa.bind(new Procedure2("color-ref") {
 		public Object apply2(Object p, Object b) throws Throwable {
 		    if (!(p instanceof Color)) throw new IllegalArgumentException("Error in color-ref: " + p + " is not a color.");
 		    if (!(b instanceof Symbol)) throw new IllegalArgumentException("Error in color-ref: " + b + " must be 'red, 'green, or 'blue.");
 				
 		    Color c = (Color) p;
 		    String channel = ((Symbol) b).getName();
 		    if ("red".equals(channel)) return c.getRed();
 		    else if ("green".equals(channel)) return c.getGreen();
 		    else if ("blue".equals(channel)) return c.getBlue();
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
 		    if (!(img1 instanceof ImageShell && img2 instanceof ImageShell)) 
 			return false;
 				
 		    ImageShell i1 = (ImageShell) img1;
 		    ImageShell i2 = (ImageShell) img2;
 
 		    if (i1.Width != i2.Width || i1.Height != i2.Height)
 			return false;
 
 		    for (int r = 0; r < i1.Height; r++)
 			for (int c = 0; c < i2.Width; c++)
 			    if (!(i2.Data[r][c].equals(i2.Data[r][c])))
 				return false;
 				
 		    return true;
 		}
 	    });
 		
 	// Get the height of an image.
 	kawa.bind(new Procedure1("image-rows") {
 		public Object apply1(Object img) throws Throwable {
 		    if (!(img instanceof ImageShell)) throw new IllegalArgumentException("Error in image-rows: " + img + " is not an image.");
 		    return ((ImageShell) img).Height;
 		}
 	    });
 		
 	// Get the width of an image.
 	kawa.bind(new Procedure1("image-cols") {
 		public Object apply1(Object img) throws Throwable {
 		    if (!(img instanceof ImageShell)) throw new IllegalArgumentException("Error in image-cols: " + img + " is not an image.");
 		    return ((ImageShell) img).Width;
 		}
 	    });
 		
 	// Pull a pixel out of an image.
 	kawa.bind(new Procedure3("image-ref") {
 		public Object apply3(Object img, Object r, Object c) throws Throwable {
 		    if (!(img instanceof ImageShell)) throw new IllegalArgumentException("Error in image-ref: " + img + " is not an image.");
 
 		    int rv, cv;
 
 		    if (r instanceof IntNum) rv = ((IntNum) r).ival;
 		    else if (r instanceof Integer) rv = (Integer) r;
 		    else if (r instanceof Long) rv = ((Long) r).intValue();
 		    else throw new IllegalArgumentException("Error in image-ref: row index " + r + " is not an integer.");
 
 		    if (c instanceof IntNum) cv = ((IntNum) c).ival;
 		    else if (c instanceof Integer) cv = (Integer) c;
 		    else if (c instanceof Long) cv = ((Long) c).intValue();
		    else throw new IllegalArgumentException("Error in image-ref: column index " + c + " is not an integer.");
 
 		    return ((ImageShell) img).Data[rv][cv];
 		}
 	    });
 		
 	// Set a pixel in an image (converts it internally to a writable image if it isn't already).
 	kawa.bind(new Procedure4("image-set!") {
 		public Object apply4(Object img, Object r, Object c, Object p) throws Throwable {
 		    if (!(img instanceof ImageShell)) throw new IllegalArgumentException("Error in image-set!: " + img + " is not an image.");
 		    if (!(p instanceof Color)) throw new IllegalArgumentException("Error in image-set!: " + img + " is not a color.");
 				
 		    int rv, cv;
 
 		    if (r instanceof IntNum) rv = ((IntNum) r).ival;
 		    else if (r instanceof Integer) rv = (Integer) r;
 		    else if (r instanceof Long) rv = ((Long) r).intValue();
 		    else throw new IllegalArgumentException("Error in image-ref: row index " + r + " is not an integer.");
 
 		    if (c instanceof IntNum) cv = ((IntNum) c).ival;
 		    else if (c instanceof Integer) cv = (Integer) c;
 		    else if (c instanceof Long) cv = ((Long) c).intValue();
 		    else throw new IllegalArgumentException("Error in image-ref: column index " + r + " is not an integer.");
 
 		    ((ImageShell) img).Data[rv][cv] = (Color) p;
 				
 		    return null;
 		}
 	    });
 		
 	// Read an image from a file.
 	kawa.bind(new Procedure0or1("read-image") {
 		public Object apply0() throws Throwable {
 		    MainFrame main = null;
 		    for (Frame frame : JFrame.getFrames())
 			if (frame instanceof MainFrame)
 			    main = (MainFrame) frame;
 
 		    FileDialog fc = new FileDialog(main, "read-image", FileDialog.LOAD);
 		    fc.setVisible(true);
 		        
 		    if (fc.getFile() == null)
 			throw new IllegalArgumentException("Error in read-image: no image chosen.");
 		        
 		    File file = new File(fc.getDirectory(), fc.getFile());
 		    if (!file.exists())
 			throw new IllegalArgumentException("Error in read-image: unable to read image '" + fc.getFile() + "', file does not exist.");
 		    String filename = file.getAbsolutePath();
 				
 		    RenderedImage img = ImageIO.read(new File((String) filename));
 		    if (img == null)
 			throw new IllegalArgumentException("Error in read-image: unable to read image '" + filename + "'");
 		    else
 			return new ImageShell(img);
 		}
 			
 		public Object apply1(Object filename) throws Throwable {
 		    if (!(filename instanceof String)) throw new IllegalArgumentException("Error in read-image: " + filename + " is not a string.");
 				
 		    RenderedImage img = ImageIO.read(new File((String) filename));
 		    if (img == null)
 			throw new IllegalArgumentException("Error in read-image: unable to read image '" + filename + "'");
 		    else
 			return new ImageShell(img);
 		}
 	    });
 
 	// Write an image to a file.
 	kawa.bind(new Procedure1or2("write-image") {
 		public Object apply1(Object img) throws Throwable {
 		    if (!(img instanceof ImageShell)) throw new IllegalArgumentException("Error in write-image: " + img + " is not an image.");
 
 		    MainFrame main = null;
 		    for (Frame frame : JFrame.getFrames())
 			if (frame instanceof MainFrame)
 			    main = (MainFrame) frame;
 
 		    FileDialog fc = new FileDialog(main, "write-image", FileDialog.LOAD);
 		    fc.setVisible(true);
 		        
 		    if (fc.getFile() == null)
 			throw new IllegalArgumentException("Error in write-image: no file chosen.");
 		        
 		    File file = new File(fc.getDirectory(), fc.getFile());
 		    String filename = file.getAbsolutePath();
 				
 		    File outputFile = new File((String) filename);
 
 		    ImageShell i = (ImageShell) img;
 		    BufferedImage image = new BufferedImage(i.Width, i.Height, BufferedImage.TYPE_INT_RGB);
 		    WritableRaster raster = (WritableRaster) image.getData();
 		    for (int r = 0; r < i.Height; r++)
 			for (int c = 0; c < i.Width; c++)
 			    raster.setPixel(c, r, new int[]{i.Data[r][c].getRed(),
 							    i.Data[r][c].getGreen(),
 							    i.Data[r][c].getBlue()});
 		    image.setData(raster);
 		    ImageIO.write(image, FileAccess.extension(outputFile.getName()), outputFile);
 			    
 		    return null;
 		}
 			
 		public Object apply2(Object img, Object filename) throws Throwable {
 		    if (!(img instanceof ImageShell)) throw new IllegalArgumentException("Error in write-image: " + img + " is not an image.");
 		    if (!(filename instanceof String)) throw new IllegalArgumentException("Error in write-image: " + filename + " is not a string.");
 
 		    ImageShell i = (ImageShell) img;
 		    BufferedImage image = new BufferedImage(i.Width, i.Height, BufferedImage.TYPE_INT_RGB);
 		    WritableRaster raster = (WritableRaster) image.getData();
 		    for (int r = 0; r < i.Height; r++)
 			for (int c = 0; c < i.Width; c++)
 			    raster.setPixel(c, r, new int[]{i.Data[r][c].getRed(),
 							    i.Data[r][c].getGreen(),
 							    i.Data[r][c].getBlue()});
 		    image.setData(raster);
 		    
 		    File outputFile = new File((String) filename);
 		    ImageIO.write(image, FileAccess.extension(outputFile.getName()), outputFile);
 			    
 		    return null;
 		}
 	    });
 		
 	// Draw an image to the screen.
 	kawa.bind(new Procedure1("draw-image") {
 		public Object apply1(Object img) throws Throwable {
 		    if (!(img instanceof ImageShell)) throw new IllegalArgumentException("Error in draw-image: " + img + " is not an image.");
 
 		    ImageShell i = (ImageShell) img;
 		    BufferedImage image = new BufferedImage(i.Width, i.Height, BufferedImage.TYPE_INT_RGB);
 		    WritableRaster raster = (WritableRaster) image.getData();
 		    for (int r = 0; r < i.Height; r++)
 			for (int c = 0; c < i.Width; c++)
 			    raster.setPixel(c, r, new int[]{i.Data[r][c].getRed(),
 							    i.Data[r][c].getGreen(),
 							    i.Data[r][c].getBlue()});
 		    image.setData(raster);
 
 				
 		    JFrame treeFrame = new JFrame("draw-image");
 		    treeFrame.setLayout(new BorderLayout());
 		    treeFrame.setResizable(false);
 		    treeFrame.setLocationByPlatform(true);
 		    treeFrame.add(new JLabel(new ImageIcon(image)));
 		    treeFrame.pack();
 		    treeFrame.setVisible(true);
 				
 		    return null;
 		}
 	    });
 		
 	// Map a procedure (lambda (color) ...) over an image.
 	kawa.bind(new Procedure2("image-map") {
 		public Object apply2(Object proc, Object img) throws Throwable {
 		    if (!(img instanceof ImageShell)) throw new IllegalArgumentException("Error in image-map: " + img + " is not an image.");
 		    if (!(proc instanceof ModuleMethod)) throw new IllegalArgumentException("Error in image-map: " + proc + " is not a procedure.");
 
 		    ImageShell i = (ImageShell) img;
 		    ModuleMethod p = (ModuleMethod) proc;
 
 		    ImageShell o = new ImageShell(i.Width, i.Height);
 				
 		    for (int r = 0; r < i.Height; r++)
 			for (int c = 0; c < i.Width; c++)
 			    o.Data[r][c] = (Color) p.apply1(i.Data[r][c]);
 
 		    return o;
 		}
 	    });
 		
 	// Create a new image of a given size, using either a color or (lambda (int int int int) ...).
 	kawa.bind(new Procedure3("$make-image$") {
 		public Object apply3(Object rows, Object cols, Object proc) throws Throwable {
 		    if (!(proc instanceof ModuleMethod || proc instanceof Color)) throw new IllegalArgumentException("Error in make-image: " + proc + " is neither a color or a procedure.");
 
 		    int rowsv, colsv;
 
 		    if (rows instanceof IntNum) rowsv = ((IntNum) rows).ival;
 		    else if (rows instanceof Integer) rowsv = (Integer) rows;
 		    else if (rows instanceof Long) rowsv = ((Long) rows).intValue();
 		    else throw new IllegalArgumentException("Error in make-image: number of rows " + rows + " is not an integer.");
 
 		    if (cols instanceof IntNum) colsv = ((IntNum) cols).ival;
 		    else if (cols instanceof Integer) colsv = (Integer) cols;
 		    else if (cols instanceof Long) colsv = ((Long) cols).intValue();
 		    else throw new IllegalArgumentException("Error in make-image: number of columns " + cols + " is not an integer.");
 
 		    boolean is_proc = proc instanceof ModuleMethod;
 		    ModuleMethod procv = null;
 		    Color procc = null;
 		    if (is_proc) procv = (ModuleMethod) proc;
 		    else procc = (Color) proc;
 
 		    ImageShell o = new ImageShell(colsv, rowsv);
 
 		    for (int r = 0; r < rowsv; r++)
 			for (int c = 0; c < colsv; c++)
 			    if (is_proc)
 				o.Data[r][c] = (Color) (procv.apply4(r, c, rowsv, colsv));
 			    else
 				o.Data[r][c] = procc;
 
 		    return o;
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
 
 
 class ImageShell {
     int Height;
     int Width;
     Color[][] Data;
 
     public ImageShell(int w, int h) {
 	Height = h;
 	Width = w;
 	Data = new Color[Height][Width];
     }
 
     public ImageShell(RenderedImage img) {
 	Height = img.getHeight();
 	Width = img.getWidth();
 
 	Data = new Color[Height][Width];
 
 	Raster raster = img.getData();
 	for (int r = 0; r < Height; r++)
 	    for (int c = 0; c < Width; c++)
 		Data[r][c] = new Color(raster.getSample(c, r, 0),
 				       raster.getSample(c, r, 1),
 				       raster.getSample(c, r, 2));
     }
     
     public String toString() {
 	return "[image " + Height + " " + Width + "]";
     }
 }
