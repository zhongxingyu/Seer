 package main;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.PointerBuffer;
 import org.lwjgl.opencl.CL10;
 import org.lwjgl.opencl.CLCommandQueue;
 import org.lwjgl.opencl.CLContext;
 import org.lwjgl.opencl.CLKernel;
 import org.lwjgl.opencl.CLMem;
 import org.lwjgl.opencl.CLProgram;
 
 import pa.cl.CLUtil;
 import pa.cl.OpenCL;
 import pa.cl.CLUtil.PlatformDeviceFilter;
 import pa.cl.CLUtil.PlatformDevicePair;
 import pa.util.IOUtil;
 
 public class BitonicSort {
 	
 	private static CLContext context;
 	private static CLCommandQueue queue;
 	private static CLProgram program;
 	private static CLKernel kernelBitonic;
 	private static CLKernel kernelBitonicUp;
 	private static CLKernel kernelBitonicDown;
 	private static PointerBuffer gws;
 	private static CLMem valMem;
 	
 	public static void main(String[] args)
     {
     	
		int n = 1 << 15;//2048;
 		float[] vals = new float[n];
 		for(int i = 0; i < n; i++) {
 			vals[i] = (float)(int)(Math.random() * 255.0 + 0.5);
 //			System.out.print((int)vals[i] + " ");
 		}
 //		System.out.println();
 		
     	
         CLUtil.createCL();
         
         PlatformDeviceFilter filter = new PlatformDeviceFilter();
         filter.addPlatformSpec(CL10.CL_PLATFORM_VENDOR, "NVIDIA");
         filter.setDesiredDeviceType(CL10.CL_DEVICE_TYPE_GPU);
     	PlatformDevicePair pair;
         try {
         	pair = CLUtil.choosePlatformAndDevice(filter);
         } catch(Exception e) {
         	pair = CLUtil.choosePlatformAndDevice();
         }
         
         context = OpenCL.clCreateContext(pair.platform, pair.device, null, null);
         
         queue = OpenCL.clCreateCommandQueue(context, pair.device, OpenCL.CL_QUEUE_PROFILING_ENABLE | OpenCL.CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE);
 //        queue = OpenCL.clCreateCommandQueue(context, pair.device, OpenCL.CL_QUEUE_PROFILING_ENABLE);
         
         program = OpenCL.clCreateProgramWithSource(context, IOUtil.readFileContent("kernel/sort.cl"));
         
         OpenCL.clBuildProgram(program, pair.device, "", null);
         
         kernelBitonic     = OpenCL.clCreateKernel(program, "bitonicSort");
         kernelBitonicUp   = OpenCL.clCreateKernel(program, "makeBitonicUp");
         kernelBitonicDown = OpenCL.clCreateKernel(program, "makeBitonicDown");
         
         long time = System.currentTimeMillis();
         
         long nanos = 0;
         long nanosScan = 0;
         long nanosBucket = 0;
         gws = new PointerBuffer(1);
 
         if(vals.length <= 512) {
 			System.out.print("Data: ");
 			for(int l = 0; l < n; l++) {
 				System.out.print((int)vals[l] + " ");
 			}
 			System.out.println();
 			System.out.println();
 		}
 
 
 		FloatBuffer valBuff = BufferUtils.createFloatBuffer(n);
 		valBuff.put(vals);
 		valBuff.rewind();
 		valMem = OpenCL.clCreateBuffer(context, OpenCL.CL_MEM_READ_WRITE | OpenCL.CL_MEM_COPY_HOST_PTR, valBuff);
 		OpenCL.clSetKernelArg(kernelBitonic, 0, valMem);
 		OpenCL.clSetKernelArg(kernelBitonicUp, 0, valMem);
 		OpenCL.clSetKernelArg(kernelBitonicDown, 0, valMem);
 		
 		gws.put(0, n/2);
 		int logN = (int)(Math.log(n)/Math.log(2));
 		int kernelCount = n/2; 
 		int phase = 1;
 		int offset1 = 0;
 		int offset2 = 0;
 		int runs = 0;
 		for(int i = 0; i < logN; i++) {
 			runs++;
 			for(int j= 0; j < kernelCount/2; j++) {
 				offset1 = j * (n/kernelCount) * 2;
 				offset2 = j * (n/kernelCount) * 2 + (n/kernelCount);
 				phase = 1;
 				for(int k = 0; k < runs; k++) {
 					gws.put(0, (n/2)/kernelCount);
 					OpenCL.clSetKernelArg(kernelBitonicUp,   1, phase);
 					OpenCL.clSetKernelArg(kernelBitonicDown, 1, phase);
 					OpenCL.clSetKernelArg(kernelBitonicUp,   2, offset1);
 					OpenCL.clSetKernelArg(kernelBitonicDown, 2, offset2);
 					
 					OpenCL.clEnqueueNDRangeKernel(queue, kernelBitonicUp, 1, null, gws, null, null, null);
 					OpenCL.clEnqueueNDRangeKernel(queue, kernelBitonicDown, 1, null, gws, null, null, null);
					
					OpenCL.clEnqueueReadBuffer(queue, valMem, 0, 0, valBuff, null, null);
					OpenCL.clFinish(queue);
					
 					phase*=2;
 				}
 				OpenCL.clFinish(queue);
 			}
 			kernelCount /= 2;
 		}
 		
 
 		gws.put(0, n/2);
 		phase = 1;
 		for(int i = 0; i < logN; i++) {
 			OpenCL.clSetKernelArg(kernelBitonic, 1, phase);
 			OpenCL.clEnqueueNDRangeKernel(queue, kernelBitonic, 1, null, gws, null, null, null);
 			OpenCL.clFinish(queue);
			OpenCL.clEnqueueReadBuffer(queue, valMem, 0, 0, valBuff, null, null);
			OpenCL.clFinish(queue);
 			
 			phase*=2;
 		}
 
 		
 		
 		
 
 
 		
 		
 		OpenCL.clEnqueueReadBuffer(queue, valMem, OpenCL.CL_FALSE, 0, valBuff, null, null);
 		OpenCL.clFinish(queue);
 		
 		time = System.currentTimeMillis() - time;
 
 		if(vals.length <= 512) {
 			System.out.println("Sorted: ");
 			for(int i = 0; i < n; i++) {
 				System.out.print((int)valBuff.get(i) + " ");
 			}
 			System.out.println();
 		}
 		
 		
         
         OpenCL.clReleaseMemObject(valMem);
         OpenCL.clReleaseKernel(kernelBitonicDown);
         OpenCL.clReleaseKernel(kernelBitonicUp);
         OpenCL.clReleaseKernel(kernelBitonic);
         OpenCL.clReleaseProgram(program);
         OpenCL.clReleaseCommandQueue(queue);
         OpenCL.clReleaseContext(context);
         
         CLUtil.destroyCL();
         
     	
         System.out.println(String.format("'BitonicSort' (with java calls) for %d elements took %.3f Seconds.", n, time / (1E3)));
 
     }
 
	private static void makeBitonic() {
		
	}
	
	private static void BitonicSort() {
		
	}
 }
