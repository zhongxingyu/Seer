 package axon;
 
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferByte;
 import java.awt.image.Raster;
 import java.io.IOException;
 import java.util.Iterator;
 
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReadParam;
 import javax.imageio.ImageReader;
 import javax.imageio.ImageTypeSpecifier;
 import javax.imageio.stream.ImageInputStream;
 
 import org.jruby.Ruby;
 import org.jruby.RubyClass;
 import org.jruby.RubyString;
 import org.jruby.RubyModule;
 import org.jruby.RubyObject;
 import org.jruby.anno.JRubyMethod;
 import org.jruby.runtime.ObjectAllocator;
 import org.jruby.runtime.ThreadContext;
 import org.jruby.runtime.builtin.IRubyObject;
 import org.jruby.util.IOInputStream;
 
 public class PNGReader extends RubyObject {
     private ImageReader reader;
     private IRubyObject rb_io_in;
    private ImageTypeSpecifier its;
     private int lineno_i;
     
     private static ObjectAllocator ALLOCATOR = new ObjectAllocator() {
         public IRubyObject allocate(Ruby runtime, RubyClass klass) {
             return new PNGReader(runtime, klass);
         }
     };
     
     @JRubyMethod
     public IRubyObject initialize(IRubyObject io) {
         Iterator readers;
         ImageInputStream iis;
         
         rb_io_in = io;
         lineno_i = 0;
 
         readers = ImageIO.getImageReadersByFormatName("png");
         reader = (ImageReader)readers.next();
         
         try {
             iis = ImageIO.createImageInputStream(new IOInputStream(rb_io_in));
         }
         catch(IOException ioe) {
             throw getRuntime().newIOErrorFromException(ioe);
         }
         
         reader.setInput(iis, true);
        try {
            its = reader.getImageTypes(0).next();
        }
        catch(IOException ioe) {
            throw getRuntime().newIOErrorFromException(ioe);
        }

         return this;
     }
     
     @JRubyMethod
     public IRubyObject width(ThreadContext context) {
         try {
             return getRuntime().newFixnum(reader.getWidth(0));
         }
         catch(IOException ioe) {
             throw getRuntime().newIOErrorFromException(ioe);
         }
     }
     
     @JRubyMethod
     public IRubyObject height(ThreadContext context) {
         try {
             return getRuntime().newFixnum(reader.getHeight(0));
         }
         catch(IOException ioe) {
             throw getRuntime().newIOErrorFromException(ioe);
         }
     }
     
     @JRubyMethod
     public IRubyObject components(ThreadContext context) {
         try {
             return getRuntime().newFixnum(getBands());
         }
         catch(IOException ioe) {
             throw getRuntime().newIOErrorFromException(ioe);
         }
     }
     
     @JRubyMethod
     public IRubyObject gets(ThreadContext context) throws IOException {
         BufferedImage image;
         ImageReadParam irp;
         Raster raster;
         DataBufferByte buffer;
         byte[] data;
         int numbands;
         int[] bands;
         
         /* Return nil if we are already at the bottom of the image */
         if (lineno_i >= reader.getHeight(0))
             return(context.nil);
 
         /* request one scanline */
         irp = reader.getDefaultReadParam();
         irp.setSourceRegion(new Rectangle(0, lineno_i, reader.getWidth(0), 1));
         
         numbands = getBands();
         switch (numbands) {
             case 3:
                 bands = new int[3];
                 bands[0] = 2;
                 bands[1] = 1;
                 bands[2] = 0;
                 irp.setDestinationBands(bands);
                 break;
             case 4:
                 bands = new int[4];
                 bands[0] = 2;
                 bands[1] = 1;
                 bands[2] = 0;
                 bands[3] = 3;
                 irp.setDestinationBands(bands);
                 break;
         }
         image = reader.read(0, irp);
 
         /* get the raw bytes from the scanline */
         raster = image.getRaster();
         buffer = (DataBufferByte)raster.getDataBuffer();
         data = buffer.getData();
 
         lineno_i += 1;
         return(new RubyString(getRuntime(), getRuntime().getString(), data));
     }
     
     @JRubyMethod
     public IRubyObject lineno(ThreadContext context) {
         return getRuntime().newFixnum(lineno_i);
     }
     
     static void initPNGReader(Ruby runtime) {
         RubyModule axon = runtime.defineModule("Axon");
         RubyModule png = axon.defineModuleUnder("PNG");
         RubyClass pngReader = png.defineClassUnder("Reader", runtime.getObject(), ALLOCATOR);
         pngReader.defineAnnotatedMethods(PNGReader.class);
     }
     
     public PNGReader(Ruby runtime, RubyClass klass) {
         super(runtime, klass);
     }
     
     private int getBands() throws IOException {
         return its.getNumComponents();
     }
 }
