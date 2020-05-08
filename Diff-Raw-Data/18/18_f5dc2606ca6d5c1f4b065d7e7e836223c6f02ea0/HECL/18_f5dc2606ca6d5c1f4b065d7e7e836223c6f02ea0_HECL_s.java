 import com.jogamp.common.nio.Buffers;
 import com.jogamp.opencl.CLBuffer;
 import com.jogamp.opencl.CLCommandQueue;
 import com.jogamp.opencl.CLContext;
 import com.jogamp.opencl.CLDevice;
 import com.jogamp.opencl.CLImage2d;
 import com.jogamp.opencl.CLImageFormat;
 import com.jogamp.opencl.CLImageFormat.*;
 import com.jogamp.opencl.CLKernel;
 import com.jogamp.opencl.CLProgram;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.FloatBuffer;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.SwingUtilities;
 
 import static java.lang.System.*;
 import static com.jogamp.opencl.CLMemory.Mem.*;
 import static java.lang.Math.*;
 
 public class HECL {
 
 	private static final int HIST_SIZE = 256;
 	
     private static void show(final BufferedImage image, final int x, final int y, final String title) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 JFrame frame = new JFrame("gamma correction example ["+title+"]");
                 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 frame.add(new JLabel(new ImageIcon(image)));
                 frame.pack();
                 frame.setLocation(x, y);
                 frame.setVisible(true);
             }
         });
     }
     
     private static InputStream getStreamFor(String filename) {
         return HECL.class.getResourceAsStream(filename);
     }
     
     public static BufferedImage readImage(String filename) throws IOException {
         return ImageIO.read(getStreamFor(filename));
     }
 
     private static BufferedImage createImage(int width, int height, CLBuffer<FloatBuffer> buffer) {
         BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
         float[] pixels = new float[buffer.getBuffer().capacity()];
         buffer.getBuffer().get(pixels).rewind();
         image.getRaster().setPixels(0, 0, width, height, pixels);
         return image;
     }
     
 	public static void main(String[] args) {
 
 		CLParams clParams = new CLParams("kernels.cl");
 
 		try{
 			// Create the CLParams instance for this program:
 			clParams.init();
         
             // load image
             BufferedImage image = readImage("lena_f.png");
             
             // Call copyImage:
            BufferedImage resultImage = copyImage(clParams, image);
            float[] pixels = resultImage.getRaster().getPixels(0, 0, resultImage.getWidth(), resultImage.getHeight(), (float[])null);
            show(resultImage, image.getWidth()/2, 50, "Resulting Image");
             
         } 
 		catch(IOException ioException) {
         	
         }
         finally{
             // cleanup all resources associated with this context.
             clParams.release();
         }
 		
 
 	}
 	
     public static int roundUp(int groupSize, int globalSize) {
         int r = globalSize % groupSize;
         if (r == 0) {
             return globalSize;
         } else {
             return globalSize + groupSize - r;
         }
     }
     
     private static BufferedImage copyImage(CLParams clParams, BufferedImage image)
     {
     	CLContext context = clParams.getContext();
     	CLCommandQueue queue = clParams.getQueue();
     	
         float[] pixels = image.getRaster().getPixels(0, 0, image.getWidth(), image.getHeight(), (float[])null);
 
         // We use ChanelOrder.INTENSITY because it's grey
         CLImageFormat format = new CLImageFormat(ChannelOrder.INTENSITY, ChannelType.FLOAT);
         CLImage2d<FloatBuffer> imageA = context.createImage2d(Buffers.newDirectFloatBuffer(pixels), image.getWidth(), image.getHeight(), format); 
         CLImage2d<FloatBuffer> imageB = context.createImage2d(Buffers.newDirectFloatBuffer(pixels.length), image.getWidth(), image.getHeight(), format); 
 
         out.println("used device memory: "
             + (imageA.getCLSize()+imageB.getCLSize())/1000000 +"MB");
 
         // get a reference to the kernel function with the name 'copy_image'
         // and map the buffers to its input parameters.
         CLKernel kernel = clParams.getKernel("copy_image");
         kernel.putArgs(imageA, imageB).putArg(image.getWidth()).putArg(image.getHeight());
 
         // asynchronous write of data to GPU device,
         // followed by blocking read to get the computed results back.
         long time = nanoTime();
         queue.putWriteImage(imageA, false)
              .put2DRangeKernel(kernel, 0, 0, image.getWidth(), image.getHeight(), 0, 0)
         .putReadImage(imageB, true);
         time = nanoTime() - time;
         
         // show resulting image.
         FloatBuffer bufferB = imageB.getBuffer();
                     
         CLBuffer<FloatBuffer> buffer = context.createBuffer(bufferB, CLBuffer.Mem.READ_WRITE);
         BufferedImage resultImage = createImage(image.getWidth(), image.getHeight(), buffer); 
 
         out.println("computation took: "+(time/1000000)+"ms");
         
         return resultImage;
     }
 
 }
