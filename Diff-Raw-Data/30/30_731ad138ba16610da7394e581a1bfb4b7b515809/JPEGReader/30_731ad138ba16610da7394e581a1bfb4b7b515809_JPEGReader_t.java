 package axon;
 
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferByte;
 import java.awt.image.WritableRaster;
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
 
 public class JPEGReader extends RubyObject {
     private ImageReader reader;
     private IRubyObject rb_io_in;
    private ImageTypeSpecifier its;
     private int lineno_i;
     
     private static ObjectAllocator ALLOCATOR = new ObjectAllocator() {
         public IRubyObject allocate(Ruby runtime, RubyClass klass) {
             return new JPEGReader(runtime, klass);
         }
     };
 
     public JPEGReader(Ruby runtime, RubyClass klass) {
         super(runtime, klass);
     }
 
     @JRubyMethod
     public IRubyObject initialize(IRubyObject io) {
         Iterator readers;
         ImageInputStream iis;
         
         rb_io_in = io;
         lineno_i = 0;
 
         readers = ImageIO.getImageReadersByFormatName("jpeg");
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
         WritableRaster raster;
         DataBufferByte buffer;
         byte[] data;
         int numbands;
         byte tmp;
         
         /* Return nil if we are already at the bottom of the image */
         if (lineno_i >= reader.getHeight(0))
             return(context.nil);
 
         /* request one scanline */
         irp = reader.getDefaultReadParam();
         irp.setSourceRegion(new Rectangle(0, lineno_i, reader.getWidth(0), 1));
         
         image = reader.read(0, irp);
 
         /* get the raw bytes from the scanline */
         raster = image.getRaster();
         
         buffer = (DataBufferByte)raster.getDataBuffer();
         data = buffer.getData();
         
         /* JPEGReader forces us to reorder BGR to RGB. */
         numbands = getBands();
         if (numbands == 3) {
             for (int i = 0; i < data.length / 3; i++) {
                 tmp = data[i * 3];
                 data[i * 3] = data[i * 3 + 2];
                 data[i * 3 + 2] = tmp;
             }
         }
 
         lineno_i += 1;
         return(new RubyString(getRuntime(), getRuntime().getString(), data));
     }
     
     @JRubyMethod
     public IRubyObject lineno(ThreadContext context) {
         return getRuntime().newFixnum(lineno_i);
     }
     
     public static void initJPEGReader(Ruby runtime) {
         RubyModule axon = runtime.defineModule("Axon");
         RubyModule png = axon.defineModuleUnder("JPEG");
         RubyClass jpegReader = png.defineClassUnder("Reader", runtime.getObject(), ALLOCATOR);
         jpegReader.defineAnnotatedMethods(JPEGReader.class);
     }    
     
     private int getBands() throws IOException {
         return its.getNumComponents();
     }
 }
