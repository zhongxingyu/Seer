 import com.jogamp.common.nio.Buffers;
 import com.jogamp.opencl.CLBuffer;
 import com.jogamp.opencl.CLKernel;
 import java.awt.image.BufferedImage;
 import java.nio.FloatBuffer;
 
 import static java.lang.System.*;
 
 public class Converter {
 
 
 	
 	public static CLBuffer<FloatBuffer> convertToSpherical(CLParams clParams, BufferedImage image) {
 				
 		int width = image.getWidth();
 		int height = image.getHeight();
 		
 		float[] pixels = image.getRaster().getPixels(0, 0, width, height, (float[])null);
 		
 		 // copy to direct float buffer
         FloatBuffer fb = Buffers.newDirectFloatBuffer(pixels);
         
         // allocate a OpenCL buffer using the direct fb as working copy
         CLBuffer<FloatBuffer> buffer = clParams.getContext().createBuffer(fb, CLBuffer.Mem.READ_WRITE);
         
         int localWorkSize = clParams.getQueue().getDevice().getMaxWorkGroupSize(); // Local work size dimensions
         int globalWorkSize = HECL.roundUp(localWorkSize, fb.capacity());  // rounded up to the nearest multiple of the localWorkSize
         
         
         System.out.println("Number of pixels: " + pixels.length);
         System.out.println("Global work size: " + globalWorkSize);
         System.out.println("Local work size: " + localWorkSize);
 
         CLKernel kernel = clParams.getKernel("convert_to_spherical");
         kernel.putArg(buffer).putArg(image.getWidth()).putArg(image.getHeight()).rewind();
         
         long time = nanoTime();
         clParams.getQueue().putWriteBuffer(buffer, false);
         clParams.getQueue().put2DRangeKernel(kernel, 0, 0, image.getWidth(), image.getHeight(), 0, 0);
         clParams.getQueue().putReadBuffer(buffer, true);
         
         time = nanoTime() - time;
 
         out.println("computation took: "+(time/1000000)+"ms");
         return buffer;
         
 		
 	}
 }
