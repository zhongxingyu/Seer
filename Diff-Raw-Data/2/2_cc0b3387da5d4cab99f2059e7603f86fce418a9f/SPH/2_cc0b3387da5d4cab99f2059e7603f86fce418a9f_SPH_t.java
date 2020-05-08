 package sph;
 
 import static pa.cl.OpenCL.CL_FALSE;
 import static pa.cl.OpenCL.CL_MEM_COPY_HOST_PTR;
 import static pa.cl.OpenCL.CL_MEM_READ_WRITE;
 import static pa.cl.OpenCL.clBuildProgram;
 import static pa.cl.OpenCL.clCreateBuffer;
 import static pa.cl.OpenCL.clCreateCommandQueue;
 import static pa.cl.OpenCL.clCreateContext;
 import static pa.cl.OpenCL.clCreateKernel;
 import static pa.cl.OpenCL.clCreateProgramWithSource;
 import static pa.cl.OpenCL.clEnqueueNDRangeKernel;
 
 import static pa.cl.OpenCL.clReleaseCommandQueue;
 import static pa.cl.OpenCL.clReleaseContext;
 import static pa.cl.OpenCL.clReleaseProgram;
 import static pa.cl.OpenCL.clReleaseKernel;
 import static pa.cl.OpenCL.clSetKernelArg;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.PointerBuffer;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opencl.CL11;
 import org.lwjgl.opencl.CLCommandQueue;
 import org.lwjgl.opencl.CLContext;
 import org.lwjgl.opencl.CLKernel;
 import org.lwjgl.opencl.CLMem;
 import org.lwjgl.opencl.CLProgram;
 import org.lwjgl.opengl.Display;
 
 import pa.cl.CLUtil;
 import pa.cl.CLUtil.PlatformDevicePair;
 import pa.cl.OpenCL;
 import pa.util.BufferHelper;
 import pa.util.IOUtil;
 import pa.util.math.MathUtil;
 import sph.helper.Settings;
 
 public class SPH{
 	/*
 	private final int n = 23;
 	private final int gridSize = 30;
 	private final float vol = 1000;
 	private final int[] dataBufferSize = { 128, 128, 128, 32 };
 	private final int N = n * n * n;
 	private float rho = 0.003f;
 	private final float m = 5 / ((float) N * vol);
     */
 	/*	NICE PARAMETERS */
 	private final int n = 21;
 	private final int gridSize = 40;
 	private final float vol = 1000;
 	private final int[] dataBufferSize = { 32, 32, 32, 64 };
 	private final int N = n * n * n;
 	private float rho = 0.0035f;
 	private final float m = 5 / ((float) N * vol);
 	private float[] inflowPresets = {-0.65f, 0.65f, 0.65f, -0.65f}; //format: x1, z1, x2, z2... with y being constant
 	private float[] drainPresets = {0.35f, -0.35f, -0.35f, 0.35f};
 	private int drainPreset = 0;
 	/*
 	kernel-Params:
 	#define BUFFER_SIZE_SIDE 24
 	#define BUFFER_SIZE_DEPTH 64
 	#define OFFSET 1
 	*/
 
 	private final float c = 1500f;
 	private final float gamma = 7;
 
 	private final boolean surface = true;
 	
 	private static SPH sph = null;
 
 	private Visualizer vis;
 	private PlatformDevicePair pair;
 	private CLProgram program;
 	private CLCommandQueue queue;
 	private CLContext context;
 	private PointerBuffer gws_BodyCnt = new PointerBuffer(1);
 	private PointerBuffer gws_GridCnt = new PointerBuffer(3);
 	private PointerBuffer gws_CubeCnt = new PointerBuffer(3);
 	private PointerBuffer gws_CellCnt = new PointerBuffer(1);
 	private PointerBuffer gws_SurfaceCnt = new PointerBuffer(1);
 	private FloatBuffer float_buffer = BufferUtils.createFloatBuffer(4 * N);
 	private FloatBuffer presetBufferHost = BufferUtils.createFloatBuffer(5);
 	
 	// private IntBuffer int_buffer =
 	// BufferUtils.createIntBuffer(dataBufferSize[0]*dataBufferSize[1]*dataBufferSize[2]*dataBufferSize[3]);
 
 	private CLKernel sph_calcNewV;
 	private CLKernel sph_calcNewPos;
 	private CLKernel sph_calcNewP;
 	private CLKernel sph_calcNewRho;
 	private CLKernel sph_resetData;
 	private CLKernel sph_calcNewSurface;
 	private CLKernel sph_calcNewSurface2;
 	private CLKernel sph_resetSurfaceRho;
 	private CLKernel sph_resetSurfaceInd;
 	private CLKernel sph_calcNewSurfaceNormal;
 
 	private CLMem[] buffers;
 
 	private CLMem clDataStructure;
 
 	private CLMem body_Pos;
 	private CLMem surface_Pos;
 	private CLMem surface_Ind;
 	private CLMem surface_grid_rho;
 	private CLMem body_V;
 	private CLMem body_P;
 	private CLMem body_rho;
 	private CLMem grid_normals;
 	private CLMem COUNT;
 	private CLMem presetBuffer;
 
 	private boolean initialized = false;
 
 	public void init() {
 
 		if (initialized) {
 			return;
 		}
 		vis = new Visualizer(this, 1024, 768);
 		try {
 			vis.create();
 		} catch (LWJGLException e) {
 			throw new RuntimeException(e.getMessage());
 		}
 
 		CLUtil.createCL();
 		pair = CLUtil.choosePlatformAndDevice();
 		context = clCreateContext(pair.platform, pair.device, null,
 				Display.getDrawable());
 		vis.initGL();
 		queue = clCreateCommandQueue(context, pair.device, 0);
 		program = clCreateProgramWithSource(context,
 				IOUtil.readFileContent("kernel/sph.cl"));
 		clBuildProgram(program, pair.device, "", null);
 
 		sph_calcNewV = clCreateKernel(program, "sph_CalcNewV");
 		sph_calcNewPos = clCreateKernel(program, "sph_CalcNewPos");
 		sph_calcNewP = clCreateKernel(program, "sph_CalcNewP");
 		sph_calcNewRho = clCreateKernel(program, "sph_CalcNewRho");
 		sph_resetData = clCreateKernel(program, "sph_resetData");
 		sph_calcNewSurface = clCreateKernel(program, "sph_CalcNewSurface");
 		sph_calcNewSurface2 = clCreateKernel(program, "sph_CalcNewSurface2");
 		sph_resetSurfaceRho = clCreateKernel(program, "sph_resetSurfaceRho");
 		sph_resetSurfaceInd = clCreateKernel(program, "sph_resetSurfaceInd");
 		sph_calcNewSurfaceNormal = clCreateKernel(program, "sph_CalcNewSurfaceNormal");
 		vis.setKernelAndQueue(sph_calcNewV, queue);
 
 		gws_BodyCnt.put(0, N);
 		
 		gws_GridCnt.put(0, gridSize);
 		gws_GridCnt.put(1, gridSize);
 		gws_GridCnt.put(2, gridSize);
 		
 		gws_CubeCnt.put(0, gridSize-1);
 		gws_CubeCnt.put(1, gridSize-1);
 		gws_CubeCnt.put(2, gridSize-1);
 		
 		gws_SurfaceCnt.put(0, 15 * gridSize * gridSize * gridSize);
 		
 		gws_CellCnt.put(0, dataBufferSize[0] * dataBufferSize[1]
 				* dataBufferSize[2]);
 
 		float p[] = new float[N * 4];
 
 		int cnt = 0;
 		for (int i = 0; i < n; i++) {
 			for (int j = 0; j < n; j++) {
 				for (int k = 0; k < n; k++) {
 					p[cnt++] = (0.05f * j - 0.5f);// +
 													// MathUtil.nextFloat(0.01f);
 					p[cnt++] = (0.05f * i - 0.9f);// +
 													// MathUtil.nextFloat(0.01f);
 					p[cnt++] = (0.05f * k - 0.5f);
 					p[cnt++] = 0;
 				}
 			}
 		}
 
 		IntBuffer dataStructure = BufferUtils.createIntBuffer(dataBufferSize[0]
 				* dataBufferSize[1] * dataBufferSize[2] * dataBufferSize[3]);
 		clDataStructure = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, dataStructure);
 		
 		presetBuffer = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, presetBufferHost);
 		
 		IntBuffer COUNT_buffer = BufferUtils.createIntBuffer(1);
 
 		COUNT = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, COUNT_buffer);
 
 		float normals[] = new float[2 * 3 * 12 * gridSize * gridSize * gridSize];
 		float[] vertices = new float[2 * 3 * 12 * gridSize * gridSize * gridSize];
 		int[] indices = new int[15 * gridSize * gridSize * gridSize];
 		
 		buffers = vis.createPositions(p ,normals, context, vertices, indices);
 
 		FloatBuffer buffer = BufferUtils.createFloatBuffer(4 * N);
 		body_V = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, buffer);
 
 		surface_Pos = buffers[0];
 		surface_Ind = buffers[1];
 		body_Pos = buffers[3];
 		
 		buffer = BufferUtils.createFloatBuffer(4 * N);
 		buffer.put(p);
 		buffer.rewind();
 		//body_Pos = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, buffer);
 		
 		IntBuffer int_buffer = BufferUtils.createIntBuffer(gridSize * gridSize * gridSize);
 		surface_grid_rho = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, int_buffer);
 	
 		float P_arr[] = new float[N];
 
 		buffer = BufferUtils.createFloatBuffer(N);
 		buffer.put(P_arr);
 		buffer.rewind();
 
 		body_P = clCreateBuffer(context, CL_MEM_READ_WRITE
 				| CL_MEM_COPY_HOST_PTR, buffer);
 
 		float rho_arr[] = new float[N];
 
 		for (int i = 0; i < rho_arr.length; i++)
 			rho_arr[i] = rho;
 
 		buffer = BufferUtils.createFloatBuffer(N);
 		buffer.put(rho_arr);
 		buffer.rewind();
 
 		body_rho = clCreateBuffer(context, CL_MEM_READ_WRITE
 				| CL_MEM_COPY_HOST_PTR, buffer);
 
 		buffer = BufferUtils.createFloatBuffer(3 * gridSize * gridSize * gridSize);
 		grid_normals = clCreateBuffer(context, CL_MEM_READ_WRITE
 				| CL_MEM_COPY_HOST_PTR, buffer);
  
 		clSetKernelArg(sph_resetData, 0, clDataStructure);
 
 		clSetKernelArg(sph_calcNewRho, 0, body_Pos);
 		clSetKernelArg(sph_calcNewRho, 1, body_rho);
 		clSetKernelArg(sph_calcNewRho, 2, m);
 		clSetKernelArg(sph_calcNewRho, 3, clDataStructure);
 
 		clSetKernelArg(sph_resetSurfaceRho, 0, surface_grid_rho);
 		clSetKernelArg(sph_resetSurfaceRho, 1, COUNT);
 		
 		clSetKernelArg(sph_resetSurfaceInd, 0, surface_Ind);
 		
 		clSetKernelArg(sph_calcNewSurface, 0, body_Pos);
 		clSetKernelArg(sph_calcNewSurface, 1, surface_Pos);
 		clSetKernelArg(sph_calcNewSurface, 2, body_rho);
 		clSetKernelArg(sph_calcNewSurface, 3, surface_grid_rho);
 		clSetKernelArg(sph_calcNewSurface, 4, m);
 		clSetKernelArg(sph_calcNewSurface, 5, gridSize);
 		clSetKernelArg(sph_calcNewSurface, 6, clDataStructure);
 		
 		clSetKernelArg(sph_calcNewSurfaceNormal, 0, surface_grid_rho);
 		clSetKernelArg(sph_calcNewSurfaceNormal, 1, grid_normals);
 		clSetKernelArg(sph_calcNewSurfaceNormal, 2, gridSize);
 		
 		clSetKernelArg(sph_calcNewSurface2, 0, surface_Pos);
 		clSetKernelArg(sph_calcNewSurface2, 1, grid_normals);
 		clSetKernelArg(sph_calcNewSurface2, 2, surface_Ind);
 		clSetKernelArg(sph_calcNewSurface2, 3, gridSize);
 		clSetKernelArg(sph_calcNewSurface2, 4, surface_grid_rho);
 		clSetKernelArg(sph_calcNewSurface2, 5, COUNT);
 		clSetKernelArg(sph_calcNewSurface2, 6, m);
 		
 		clSetKernelArg(sph_calcNewP, 0, body_P);
 		clSetKernelArg(sph_calcNewP, 1, body_rho);
 		clSetKernelArg(sph_calcNewP, 2, rho);
 
 		clSetKernelArg(sph_calcNewV, 0, body_Pos);
 		clSetKernelArg(sph_calcNewV, 1, body_V);
 		clSetKernelArg(sph_calcNewV, 3, body_P);
 		clSetKernelArg(sph_calcNewV, 4, body_rho);
 		clSetKernelArg(sph_calcNewV, 5, m);
 		clSetKernelArg(sph_calcNewV, 6, clDataStructure);
 
 		clSetKernelArg(sph_calcNewPos, 0, body_Pos);
 		clSetKernelArg(sph_calcNewPos, 1, body_V);
 		clSetKernelArg(sph_calcNewPos, 2, vis.getCurrentParams().m_timeStep);
 		clSetKernelArg(sph_calcNewPos, 3, clDataStructure);
 		clSetKernelArg(sph_calcNewPos, 4, presetBuffer);
 		
 		setDrainPreset(0, 0.1f);
 		
 		clEnqueueNDRangeKernel(queue, sph_calcNewPos, 1, null, gws_BodyCnt,
 				null, null, null);
 	}
 
 	public void run() {
 		init();
 		
 		int cnt = 0;
 		long time;
 		
 		while (!vis.isDone()) {
 			if (!vis.isPause()) {
 				
 				if(Settings.PROFILING) {
 					time = System.currentTimeMillis();
 				}
 				clEnqueueNDRangeKernel(queue, sph_calcNewRho, 1, null, gws_BodyCnt, null, null, null);
 				
 				if(Settings.PROFILING) {
 					time = System.currentTimeMillis();
 				}
 				clEnqueueNDRangeKernel(queue, sph_resetSurfaceRho, 3, null, gws_GridCnt, null, null, null);
 				clEnqueueNDRangeKernel(queue, sph_resetSurfaceInd, 1, null, gws_SurfaceCnt, null, null, null);
 				clEnqueueNDRangeKernel(queue, sph_calcNewSurface, 1, null, gws_BodyCnt, null, null, null);
 				clEnqueueNDRangeKernel(queue, sph_calcNewSurfaceNormal, 3, null, gws_CubeCnt, null, null, null);
 				clEnqueueNDRangeKernel(queue, sph_calcNewSurface2, 3, null, gws_CubeCnt, null, null, null);
 				if(Settings.PROFILING) {
 					OpenCL.clFinish(queue);
 					System.out.println(System.currentTimeMillis() - time);
 				}
 				
 				time = System.currentTimeMillis();
 				clEnqueueNDRangeKernel(queue, sph_calcNewP, 1, null, gws_BodyCnt, null, null, null);
 				if(Settings.PROFILING) {
 					OpenCL.clFinish(queue);
 					System.out.println(System.currentTimeMillis() - time);
 				}
 				
 				clEnqueueNDRangeKernel(queue, sph_calcNewV, 1, null, gws_BodyCnt, null, null, null);
 				if(Settings.PROFILING) {
 					OpenCL.clFinish(queue);
 					System.out.println(System.currentTimeMillis() - time);
 				}
 
 				time = System.currentTimeMillis();
 				clEnqueueNDRangeKernel(queue, sph_resetData, 1, null, gws_CellCnt, null, null, null);
 				if(Settings.PROFILING) {
 					OpenCL.clFinish(queue);
 					System.out.println(System.currentTimeMillis() - time);
 					// OpenCL.clEnqueueReadBuffer(queue, clDataStructure,
 					// CL_FALSE, 0, int_buffer, null, null);
 					// BufferHelper.printBuffer(int_buffer, 8*8*8*10);
 				}
 				time = System.currentTimeMillis();
 				clEnqueueNDRangeKernel(queue, sph_calcNewPos, 1, null, gws_BodyCnt, null, null, null);
 				if(Settings.PROFILING) {
 					OpenCL.clFinish(queue);
 					System.out.println(System.currentTimeMillis() - time);
 					System.out.println("-------------------------");
 				}
 
 			}
 			time = System.currentTimeMillis();
 			vis.visualize();
 			if(Settings.PROFILING) {
 				System.out.println(System.currentTimeMillis() - time);
 				System.out.println("-------------------------");
 			}
 
 		}
 		close();
 	}
 
 	public void close() {
 		vis.close();
 
 		if (sph_calcNewRho != null) {
 			clReleaseKernel(sph_calcNewRho);
 		}
 		if (sph_resetData != null) {
 			clReleaseKernel(sph_resetData);
 		}
 		if (sph_calcNewSurface != null) {
 			clReleaseKernel(sph_calcNewSurface);
 		}
 		if (sph_calcNewSurface2 != null) {
 			clReleaseKernel(sph_calcNewSurface2);
 		}
 		if (sph_resetSurfaceRho != null) {
 			clReleaseKernel(sph_resetSurfaceRho);
 		}
 		if (sph_resetSurfaceInd != null) {
 			clReleaseKernel(sph_resetSurfaceInd);
 		}
 		if (sph_calcNewP != null) {
 			clReleaseKernel(sph_calcNewP);
 		}
 		if (sph_calcNewPos != null) {
 			clReleaseKernel(sph_calcNewPos);
 		}
 		if (sph_calcNewV != null) {
 			clReleaseKernel(sph_calcNewV);
 		}
 		if (program != null) {
 			clReleaseProgram(program);
 			program = null;
 		}
 		if (queue != null) {
 			clReleaseCommandQueue(queue);
 			queue = null;
 		}
 		if (context != null) {
 			clReleaseContext(context);
 			context = null;
 		}
 		CLUtil.destroyCL();
 	}
 
 	public static SPH getInstance() {
 		if (sph == null) {
 			sph = new SPH();
 		}
 		return sph;
 	}
 
 	public static void destroy() {
 		if (sph != null) {
 			// System.out.println("destryoing");
 			// Display.destroy();
 			sph.requestClose();
 		}
 		sph = null;
 	}
 
 	public void requestClose() {
 		vis.requestClose();
 	}
 
 	public void set_m(float m) {
 	}
 
 	public float get_m() {
 		return m;
 	}
 
 	public void set_rho(float rho) {
 	}
 
 	public float get_rho() {
 		return rho;
 	}
 
 	public void set_c(float c) {
 	}
 
 	public float get_c() {
 		return c;
 	}
 
 	public void set_gamma(float gamma) {
 	}
 
 	public float get_gamma() {
 		return gamma;
 	}
 
 	public void setPause(boolean pause) {
 		vis.setPause(pause);
 	}
 	public void processKeyPressed(int key)
     {
 		if(key == Keyboard.KEY_NUMPAD4)
         {
             setDrainPreset(drainPreset - 1);
         }
         if(key == Keyboard.KEY_NUMPAD6)
         { 
         	setDrainPreset(drainPreset + 1);
         }
         if(key == Keyboard.KEY_NUMPAD7)
         {
         	setDrainPreset(drainPreset, presetBufferHost.get(0) - 0.1f);
         }
         if(key == Keyboard.KEY_NUMPAD9)
         { 
         	setDrainPreset(drainPreset, presetBufferHost.get(0) + 0.1f);
         }
     }
 	public void setDrainPreset(int preset) {
 		setDrainPreset(preset, -1);
 	}
 	public void setDrainPreset(int preset, float size) {
 		preset = Math.max(0, Math.min(preset, inflowPresets.length/2 - 1));
 		size = Math.max(0, Math.min(size, 20));
 		drainPreset = preset;
		if(size >= 0) {
 			presetBufferHost.put(0, size);
 		}
 		System.out.println("set presets to (size="+size+"), (preset="+preset+")");
 		presetBufferHost.put(1, inflowPresets[preset]);
 		presetBufferHost.put(2, inflowPresets[preset + 1]);
 		presetBufferHost.put(3, drainPresets[preset]);
 		presetBufferHost.put(4, drainPresets[preset + 1]);
 		OpenCL.clEnqueueWriteBuffer(queue, presetBuffer, CL_FALSE, 0, presetBufferHost, null, null);
 	}
 }
