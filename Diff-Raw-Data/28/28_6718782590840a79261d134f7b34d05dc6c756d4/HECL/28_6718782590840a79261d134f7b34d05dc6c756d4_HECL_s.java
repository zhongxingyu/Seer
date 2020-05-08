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
 import java.nio.IntBuffer;
 
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
         BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
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
             //BufferedImage image = readImage("lena_g_f.png");
             BufferedImage image = readImage("uneq.jpg");
             
             // Call copyImage:
             //BufferedImage resultImage = copyImage(clParams, image);
             
             show(image, image.getWidth()/2, 50, "Original Image");
 
             // Equalize Image:
             CLImageFormat format = new CLImageFormat(ChannelOrder.INTENSITY, ChannelType.FLOAT); // We use ChanelOrder.INTENSITY because it's grey
  
             image = equalizeImage(clParams, image, format);
             
             show(image, image.getWidth()/2, 50, "Resulting Image");
             
         } 
 		catch(IOException ioException) {
         	
         }
         finally{
             // cleanup all resources associated with this context.
             clParams.release();
         }
 		
 
 	}
 	
     private static int roundUp(int groupSize, int globalSize) {
         int r = globalSize % groupSize;
         if (r == 0) {
             return globalSize;
         } else {
             return globalSize + groupSize - r;
         }
     }
     
     private static int[] getHistogram(CLParams clParams, CLImage2d<FloatBuffer> image)
     {   	
     	CLContext context = clParams.getContext();
     	CLCommandQueue queue = clParams.getQueue();
         
     	// Array to store the histogram results
         int[] histogram = new int[HIST_SIZE];
         int colCount = image.getWidth();
         
         CLBuffer<IntBuffer> colHistBuffer =  context.createBuffer(Buffers.newDirectIntBuffer(colCount * HIST_SIZE),CLBuffer.Mem.READ_WRITE);
 
         // First, we calculate the histogram for each column of the image:
         CLKernel kernel = clParams.getKernel("calc_colHist");
         kernel.putArg(image).putArg(image.height).putArg(colHistBuffer).putArg(HIST_SIZE).rewind();
 
         long time = nanoTime();
         queue.putWriteImage(image, false)
              .putWriteBuffer(colHistBuffer, false)
              .put2DRangeKernel(kernel, 0, 0, image.getWidth(), 1, 0, 0)
              .putReadBuffer(colHistBuffer, true);
         
         // Once done that, we proceed to meerge all the col histograms:
         CLBuffer<IntBuffer> histBuffer =  context.createBuffer(Buffers.newDirectIntBuffer(HIST_SIZE),CLBuffer.Mem.WRITE_ONLY);
 
         kernel = clParams.getKernel("merge_colHist");
         kernel.putArgs(colHistBuffer, histBuffer).putArg(colCount).putArg(HIST_SIZE).rewind();
         
         queue.putWriteBuffer(colHistBuffer, false)
  	         .putWriteBuffer(histBuffer, false)
 	         .put2DRangeKernel(kernel, 0, 0, image.getWidth(), 1, 0, 0)
 	         .putReadBuffer(histBuffer, true);
         time = nanoTime() - time;
         
         histBuffer.getBuffer().get(histogram);        
 
         //Check if the count of ocurrencies is the same as the number of pixels:
         int count = 0;
         for(int i = 0; i < HIST_SIZE; i++)
         	count += histogram[i];
         
         System.out.println(count + "=" + (image.width*image.height));
         
         return histogram;
     }
 
     private static float[] getCDF(int[] histogram, int imgWidth, int imgHeight)
     {
     	float[] cdf = new float[histogram.length];
     	int size = imgWidth * imgHeight;
     	
     	cdf[0] = histogram[0]/size;
     	
     	for(int i = 1; i < histogram.length; i++){
     		cdf[i] = (cdf[i - 1]*size + histogram[i])/size;
     	}
     	return cdf;
     }
 
     private static float[] getNormalizedCDF(int[] histogram, int imgWidth, int imgHeight)
     {
     	int cdf_min = histogram[0];
     	
     	float[] cdf = new float[histogram.length];
     	int size = imgWidth * imgHeight;
     	
     	cdf[0] = histogram[0];
     	
     	for(int i = 1; i < histogram.length; i++){
     		if(histogram[i] > 0 && cdf_min == 0)
     			cdf_min = histogram[i];
     		cdf[i] = cdf[i - 1] + histogram[i];
     	}
     	
     	// Normalizamos:
     	for(int i = 0; i < histogram.length; i++)
     		cdf[i] = Math.round((cdf[i] - cdf_min)/(imgWidth * imgHeight - cdf_min) * (histogram.length - 1));
 
     	return cdf;
     }
     
     private static BufferedImage equalizeImage(CLParams clParams, BufferedImage image, CLImageFormat format)
     {
     	CLContext context = clParams.getContext();
     	CLCommandQueue queue = clParams.getQueue();
     	
         float[] pixels = image.getRaster().getPixels(0, 0, image.getWidth(), image.getHeight(), (float[])null);
 
         CLImage2d<FloatBuffer> imageA = context.createImage2d(Buffers.newDirectFloatBuffer(pixels), image.getWidth(), image.getHeight(), format); 
 
         // Get the histogram of the image:
         int[] histogram = getHistogram(clParams, imageA);
         
         // Now, we get the Comulative Distribution Function of the image and create the auxiliar buffers:
     	
         float[] cdf = getNormalizedCDF(histogram, imageA.width, imageA.height);
        int max = 255, min = 0;
        /*
        float[] cdf = getCDF(histogram, imageA.width, imageA.height);
    	
    	// We normalize it, searching for the min and max values:
    	int size = imageA.width * imageA.height;
    	
    	int max = 0;
    	int min = -1;
 
    	for(int i = 0; i < cdf.length; i++){
    		if(cdf[i] > 0 && min == -1) min = i;
    		if(cdf[i] == 1.0) {
				max = i;
				break;
			}
    	}
    	*/
         CLImage2d<FloatBuffer> imageB = context.createImage2d(Buffers.newDirectFloatBuffer(pixels.length), image.getWidth(), image.getHeight(), format); 
         CLBuffer<FloatBuffer> cdfBuffer = context.createBuffer(Buffers.newDirectFloatBuffer(cdf), CLBuffer.Mem.READ_ONLY);
 
         out.println("used device memory: "
            + (imageA.getCLSize()+imageB.getCLSize())/1000000 +"MB");
 
         // Once done that, we call the equalize_image function:
         CLKernel kernel = clParams.getKernel("equalize_image");
        kernel.putArgs(imageA, imageB).putArg(cdfBuffer).putArg(cdf.length).putArg(min).putArg(max).rewind();
 
         // asynchronous write of data to GPU device,
         // followed by blocking read to get the computed results back.
         long time = nanoTime();
         queue.putWriteImage(imageA, false)
         	 .putWriteBuffer(cdfBuffer, false)
             .putWriteImage(imageB, true)
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
