 package main;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.PointerBuffer;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opencl.CL10;
 import org.lwjgl.opencl.CL10GL;
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
 import pa.util.SizeOf;
 
 import opengl.GL;
 import static opengl.GL.*;
 import opengl.util.Camera;
 import opengl.util.ShaderProgram;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 
 import particle.ParticleFactory;
 import opengl.util.FrameBuffer;
 import opengl.util.Texture;
 import opengl.util.Geometry;
 import opengl.util.GeometryFactory;
 
 public class MainProgram {
 	private boolean running = true;
 
 	////// PARAMETERS
 	private int elements           = 1<<17; // we want 1<<17
 	private int defaultSpawn       = 1<<7;  // we want 1<<7   
 	private long changeLPAInterval = 1<<7;  // we want 1<<7
 	private int numberLPA          = 1<<6;  // we want 1<<6
 	
 	////// SHARED BLOCK
 	private int bufferObjectPositions  = -1;
 	private int bufferObjectLifetimes  = -1;
 	private int bufferObjectVelocities = -1;
 	private int bufferObjectLPA        = -1;
 
 	////// OPENCL BLOCK
 	private CLContext context     = null;
 	private CLCommandQueue queue  = null;
 	private CLProgram program     = null;
 	private CLKernel kernelMove   = null;
 	private CLKernel kernelSpawn  = null;
 	private CLMem memPositions    = null;
 	private CLMem memVelocities   = null;
 	private CLMem memLifetimes    = null;
 	private CLMem memNewParticles = null;
 	private CLMem memLPAs         = null;
 	private CLMem memLPARandoms   = null;
 
 	////// OPENGL BLOCK + DEFERRED SHADING
 	private Matrix4f modelMat  = new Matrix4f();
 	private Camera   cam       = new Camera();
 	private int vertexArrayID  = -1;
 
 	private int lpaVAID         = -1;
 	private ShaderProgram lpaSP = null;
 	
 	private Geometry screenQuad        = null;
 	private ShaderProgram screenQuadSP = null;
 
 	private int textureUnit       = 0;
 	private ShaderProgram depthSP = null;
 	private FrameBuffer depthFB   = null;
 	private Texture depthTex      = null;
 
 	////// other
 	private long lastTimestamp  = System.currentTimeMillis();
 	private long sumDeltaTime   = 0;
 	private int  numberOfFrames = 0;
 	private float fps           = 0;
 	private long changeLPATimer = changeLPAInterval; 
 	private int  spawnOffset    = 0;
 	private int  spawnElements  = defaultSpawn;
 	private Vector3f moveDir    = new Vector3f(0.0f,0.0f,0.0f);
 	private boolean showLPA     = false;
 	private boolean animating   = true;
 	private boolean debug       = false;
 	private boolean fpsControl  = false;
 	
 	// TODO dirty hack
 	private boolean pulse = false;
 	
 	public MainProgram() {
 	    initGL();
 		initCL();
 	    initParticleBuffers();
 	    printControls();
 	}
 	
 	private void initParticleBuffers() {
 	    // vertex array for particles (the screen quad uses a different one)
 	    vertexArrayID = glGenVertexArrays();
         glBindVertexArray(vertexArrayID);
 
 		// positions
 		FloatBuffer particlePositions = ParticleFactory.createZeroFloatBuffer(elements * 3);
 		bufferObjectPositions = glGenBuffers();
 		glBindBuffer(GL_ARRAY_BUFFER, bufferObjectPositions);
 		glBufferData(GL_ARRAY_BUFFER, particlePositions, GL_STATIC_DRAW);
 		
         glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
         glVertexAttribPointer(ShaderProgram.ATTR_POS, 3, GL_FLOAT, false, 3 * SizeOf.FLOAT, 0);
         // velocities
         FloatBuffer particleVelocities = ParticleFactory.createZeroFloatBuffer(elements * 3);
         bufferObjectVelocities = glGenBuffers();
         glBindBuffer(GL_ARRAY_BUFFER, bufferObjectVelocities);
         glBufferData(GL_ARRAY_BUFFER, particleVelocities, GL_STATIC_DRAW);
         
         // lifetimes
         FloatBuffer particleLifetimes = ParticleFactory.createZeroFloatBuffer(elements * 2);
         bufferObjectLifetimes = glGenBuffers();
         glBindBuffer(GL_ARRAY_BUFFER, bufferObjectLifetimes);
         glBufferData(GL_ARRAY_BUFFER, particleLifetimes, GL_STATIC_DRAW);
         
         glEnableVertexAttribArray(ShaderProgram.ATTR_NORMAL);
         glVertexAttribPointer(ShaderProgram.ATTR_NORMAL, 2, GL_FLOAT, false, 2 * SizeOf.FLOAT, 0);
         
         // additional vertex array to be able to visualize the LPAs differently
         lpaVAID = glGenVertexArrays();
         glBindVertexArray(lpaVAID);
         
         FloatBuffer bufferLPA = ParticleFactory.createZeroFloatBuffer(numberLPA * 3);
         bufferObjectLPA = glGenBuffers();
         glBindBuffer(GL_ARRAY_BUFFER, bufferObjectLPA);
         glBufferData(GL_ARRAY_BUFFER, bufferLPA, GL_STATIC_DRAW);
         
         glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
         glVertexAttribPointer(ShaderProgram.ATTR_POS, 3, GL_FLOAT, false, 3 * SizeOf.FLOAT, 0);
 
 	}
 	
 	public void run() {
 		memPositions  = OpenCL.clCreateFromGLBuffer(context, OpenCL.CL_MEM_READ_WRITE, bufferObjectPositions);
 		memVelocities = OpenCL.clCreateFromGLBuffer(context, OpenCL.CL_MEM_READ_WRITE, bufferObjectVelocities);
 		memLifetimes  = OpenCL.clCreateFromGLBuffer(context, OpenCL.CL_MEM_READ_WRITE, bufferObjectLifetimes);
 		
 		// static kernel arguments
         OpenCL.clSetKernelArg(kernelMove, 0, memPositions);
         OpenCL.clSetKernelArg(kernelMove, 1, memVelocities);
         OpenCL.clSetKernelArg(kernelMove, 2, memLifetimes);
         OpenCL.clSetKernelArg(kernelMove, 5, numberLPA);
 
         OpenCL.clSetKernelArg(kernelSpawn, 0, memPositions);
         OpenCL.clSetKernelArg(kernelSpawn, 1, memVelocities);
         OpenCL.clSetKernelArg(kernelSpawn, 2, memLifetimes);
         
         // calculate global work size
 		PointerBuffer gws = new PointerBuffer(elements);
         gws.put(0, elements);
 
         // limit respawn elements to elements, create the buffer
         defaultSpawn = Math.min(defaultSpawn, elements);
         spawnElements = defaultSpawn;
         
         FloatBuffer bufferNewParticleData = BufferUtils.createFloatBuffer(spawnElements * ParticleFactory.PARTICLE_PROPERTIES);
         
         // create indices for LPA seeking
         IntBuffer bufferRandIndices = BufferUtils.createIntBuffer(elements);
        
         // print some information
         System.out.println("Running with " + elements + " Particles.");
         System.out.println("Respawning  ~" + (elements>>1) + " particles per second (Minimum " + defaultSpawn + " per frame).");
         System.out.println("Using " + numberLPA + " low pressure areas, changing position every ~" + changeLPAInterval + " ms.");
 
         // spawn first LPAs
         FloatBuffer bufferLPA = ParticleFactory.createLPA(numberLPA);
         glBindBuffer(GL_ARRAY_BUFFER, bufferObjectLPA);
         glBufferData(GL_ARRAY_BUFFER, bufferLPA, GL_STATIC_DRAW);
         memLPAs = CL10GL.clCreateFromGLBuffer(context, 0, bufferObjectLPA, null);
         
         long debugTimer = 0;
         while(running) {
 			long deltaTime = System.currentTimeMillis() - lastTimestamp;
 			debugTimer     += deltaTime;
 			lastTimestamp  += deltaTime;
 			changeLPATimer += deltaTime;
 			calculateFramesPerSecond(deltaTime);
 
 			handleInput(deltaTime);
 			
 			if(animating) {
 			    // MOVE LPA TODO -> in kernel?
                 if(changeLPATimer >= changeLPAInterval) {
                     bufferLPA = ParticleFactory.createLPA(numberLPA);
                     glBindBuffer(GL_ARRAY_BUFFER, bufferObjectLPA);
                     glBufferData(GL_ARRAY_BUFFER, bufferLPA, GL_STATIC_DRAW);
                 }
                 
 				// ACQUIRE OPENGL BUFFERS
     			OpenCL.clEnqueueAcquireGLObjects(queue, memPositions,  null, null);
     			OpenCL.clEnqueueAcquireGLObjects(queue, memVelocities, null, null);
     			OpenCL.clEnqueueAcquireGLObjects(queue, memLifetimes,  null, null);
     			OpenCL.clEnqueueAcquireGLObjects(queue, memLPAs,  null, null);
     			
     			
     			
     			// SET RANDOM PARAMS
     			if(changeLPATimer >= changeLPAInterval) {
     	        	changeLPATimer = 0;
     	        	int[] a = new int[4];
                     for(int i = 0; i < elements; i++) {
                     	int id = (int)((numberLPA * numberLPA * ParticleFactory.lifetime() + numberLPA * ParticleFactory.lifetime() + numberLPA)) % 4;
                     	a[id]+=1;
                     	bufferRandIndices.put(i, id);
                     }
                     
                     if(memLPARandoms != null) {
                     	OpenCL.clReleaseMemObject(memLPARandoms);
                     	memLPARandoms = null;
                     }
                     memLPARandoms = OpenCL.clCreateBuffer(context, OpenCL.CL_MEM_COPY_HOST_PTR | OpenCL.CL_MEM_READ_ONLY, bufferRandIndices);
     	        }
     			
     			
     			
     			// MOVE PARTICLES
     			gws.put(0, elements);
     	        OpenCL.clSetKernelArg(kernelMove, 3, memLPAs);
     			OpenCL.clSetKernelArg(kernelMove, 4, memLPARandoms);
     			OpenCL.clSetKernelArg(kernelMove, 6, (int)deltaTime);
     			
     			// TODO dirty hack to test
     			OpenCL.clSetKernelArg(kernelMove, 7, pulse?1:0);
     			if(pulse) pulse = false;
     			
     			OpenCL.clEnqueueNDRangeKernel(queue, kernelMove, 1, null, gws, null, null, null);
     			
     			
     			
     			// RESPAWN
     			int newCalc = defaultSpawn;
                newCalc = fps>0? (int)(0.9f * ((elements>>1) / (int)fps)) : defaultSpawn;
                 newCalc = Math.max(newCalc, defaultSpawn);
              
                 // resize respawn buffer if needed
                 if(newCalc != spawnElements) {
                     if(Math.abs(newCalc - spawnElements) > 0.1 * spawnElements) {
                         if(debug)
                             System.out.println("Resizing Respawn Buffer!");
                         spawnElements = newCalc;
                         
                         bufferNewParticleData = BufferUtils.createFloatBuffer(spawnElements * ParticleFactory.PARTICLE_PROPERTIES);
                     }
                 }
                 
     	        ParticleFactory.createNewParticles(bufferNewParticleData);
                 if(memNewParticles != null) {
                     OpenCL.clReleaseMemObject(memNewParticles);
                     memNewParticles = null;
                 }
                 memNewParticles = OpenCL.clCreateBuffer(context, OpenCL.CL_MEM_COPY_HOST_PTR | OpenCL.CL_MEM_READ_ONLY, bufferNewParticleData);
                 
                 gws.put(0, spawnElements);
                 OpenCL.clSetKernelArg(kernelSpawn, 3, memNewParticles);
                 OpenCL.clSetKernelArg(kernelSpawn, 4, elements);
                 OpenCL.clSetKernelArg(kernelSpawn, 5, spawnOffset);
                 OpenCL.clEnqueueNDRangeKernel(queue, kernelSpawn, 1, null, gws, null, null, null);
                 
                 spawnOffset = (spawnOffset + spawnElements) % elements;
                 
                 
                 
                 // FREE OPENGL BUFFERS
     	        OpenCL.clEnqueueReleaseGLObjects(queue, memLifetimes,  null, null);
     	        OpenCL.clEnqueueReleaseGLObjects(queue, memVelocities, null, null);
     	        OpenCL.clEnqueueReleaseGLObjects(queue, memLPAs, null, null);
                 OpenCL.clEnqueueReleaseGLObjects(queue, memPositions,  null, null);
                 
 			}  // if animating
 		
 			drawScene();
 
 			if(debugTimer >= 1000) {
                 debugTimer = 0;
             }
 			
             // if close is requested: close
 			if(Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
 				stop();
 			}
 		}
 		System.out.println("Program shut down properly.");
 	}
 	
     public void drawScene() {
         Matrix4f viewProj = opengl.util.Util.mul(null, cam.getProjection(), cam.getView());
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		
 		// post effects etc
 		depthSP.use();
 		depthSP.setUniform("model", modelMat);
 		depthSP.setUniform("viewProj", viewProj);
         depthSP.setUniform("camPos", cam.getCamPos());
 
         depthFB.bind();
         depthFB.clearColor();
         
         glEnable(GL_BLEND);
         glDisable(GL_DEPTH_TEST);
         
         glBindVertexArray(vertexArrayID);
         opengl.GL.glDrawArrays(opengl.GL.GL_POINTS, 0, elements);
         
         glDisable(GL_BLEND);
         glEnable(GL_DEPTH_TEST);
         
 		// draw texture on screenquad
         glBindFramebuffer(GL_FRAMEBUFFER, 0);
 		screenQuadSP.use();        
 		screenQuadSP.setUniform("image", depthTex);
 		screenQuad.draw();
 		
 
 		// show low pressure areas with a blue tone
         if(showLPA) {
             lpaSP.use();
             lpaSP.setUniform("model", modelMat);
             lpaSP.setUniform("viewProj", viewProj);
             lpaSP.setUniform("camPos", cam.getCamPos());
             
             glDisable(GL_BLEND);
             glDisable(GL_DEPTH_TEST);
             
             glBindVertexArray(lpaVAID);
             opengl.GL.glDrawArrays(opengl.GL.GL_POINTS, 0, numberLPA);
         }
 		
         // present screen
         Display.update();
         if(fpsControl)
             Display.sync(60);
 	}
     
 	private void handleInput(long deltaTime) {
         float speed = 1e-3f * deltaTime;
         float moveSpeed = 1e-3f * (float)deltaTime;
 
         while(Keyboard.next()) {
             if(Keyboard.getEventKeyState()) {
                 switch(Keyboard.getEventKey()) {
                     case Keyboard.KEY_W: moveDir.z += 1.0f; break;
                     case Keyboard.KEY_S: moveDir.z -= 1.0f; break;
                     case Keyboard.KEY_A: moveDir.x += 1.0f; break;
                     case Keyboard.KEY_D: moveDir.x -= 1.0f; break;
                     case Keyboard.KEY_SPACE: moveDir.y += 1.0f; break;
                     case Keyboard.KEY_C: moveDir.y -= 1.0f; break;
                 }
             } else {
                 switch(Keyboard.getEventKey()) {
                     case Keyboard.KEY_W:     moveDir.z -= 1.0f; break;
                     case Keyboard.KEY_S:     moveDir.z += 1.0f; break;
                     case Keyboard.KEY_A:     moveDir.x -= 1.0f; break;
                     case Keyboard.KEY_D:     moveDir.x += 1.0f; break;
                     case Keyboard.KEY_SPACE: moveDir.y -= 1.0f; break;
                     case Keyboard.KEY_C:     moveDir.y += 1.0f; break;
                     
                     case Keyboard.KEY_E: animating  = !animating; break;
                     case Keyboard.KEY_L: showLPA    = !showLPA;   break;
                     case Keyboard.KEY_H: debug      = !debug;
                                          System.out.println("Debug mode: " + (debug?"on":"off"));
                                              break;
                     case Keyboard.KEY_F: fpsControl = !fpsControl;
                                          if(debug) System.out.println("FPS " + (!fpsControl?"un":"") + "limited");
                                              break;
                                              
                     case Keyboard.KEY_R: ; break;
                     // TODO dirty hack (remove -> move to mouse)
                     case Keyboard.KEY_P: pulse = true; break;
                 }
             }
         }
         
         cam.move(moveSpeed * moveDir.z, moveSpeed * moveDir.x, moveSpeed * moveDir.y);
 
         
         while(Mouse.next()) {
             if(Mouse.isButtonDown(1)) {
                 cam.rotate(-speed*Mouse.getEventDX(), -speed*Mouse.getEventDY());
             }
             if(Mouse.isButtonDown(0)) {
                 // TODO pushing
                 if(debug)
                     System.out.println("LMB press!");
             }
         }
     }
 	
 	private void initGL() {
         try {
             GL.init();
         } catch (LWJGLException e) {
             e.printStackTrace();
         }
         cam.move(-1.0f, 0, 0);
         
         // screenQuad
         screenQuad   = GeometryFactory.createScreenQuad();
         screenQuadSP = new ShaderProgram("shader/ScreenQuad_VS.glsl", "shader/CopyTexture_FS.glsl");
         
         // lpa debug visualization
         lpaSP = new ShaderProgram("shader/LPA_VS.glsl", "shader/LPA_FS.glsl");
         
         // first renderpath: "depth"
         depthSP = new ShaderProgram("./shader/DefaultVS.glsl", "./shader/Default1FS.glsl");
         depthSP.use();
         
         depthFB = new FrameBuffer();
         depthFB.init(true, WIDTH, HEIGHT);
 
         depthTex = new Texture(GL_TEXTURE_2D, textureUnit++);
         depthFB.addTexture(depthTex, GL_RGBA16F, GL_RGBA);
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
         glBindFragDataLocation(depthSP.getId(), 0, "depth");
         
         glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
         glBlendFunc(GL_ONE, GL_ONE);
     }
 	
 	private void initCL() {
         CLUtil.createCL();
         
         PlatformDevicePair pair = null;
         try {
             PlatformDeviceFilter filter = new PlatformDeviceFilter();
             
             // set spec here
             filter.addPlatformSpec(CL10.CL_PLATFORM_VENDOR, "NVIDIA");
             filter.setDesiredDeviceType(CL10.CL_DEVICE_TYPE_GPU);
                 
             // query platform and device
             pair = CLUtil.choosePlatformAndDevice(filter);
         }catch(Exception e) {
             pair = CLUtil.choosePlatformAndDevice();
         }
         
         context  = OpenCL.clCreateContext(pair.platform, pair.device, null, Display.getDrawable());
         queue    = OpenCL.clCreateCommandQueue(context, pair.device, 0);
         // for out of order queue: OpenCL.CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE
         
         program = OpenCL.clCreateProgramWithSource(context, IOUtil.readFileContent("kernel/kernel.cl"));
         OpenCL.clBuildProgram(program, pair.device, "", null);
         
         kernelMove  = OpenCL.clCreateKernel(program, "move");
         kernelSpawn = OpenCL.clCreateKernel(program, "respawn");
 
     }
 	
 	private void stop() {
         running = false;
         
         // Shaderprograms
         if(screenQuadSP != null)
         	screenQuadSP.delete();
         if(lpaSP != null)
             lpaSP.delete();
         if(depthSP != null)
         	depthSP.delete();
         
         // Display
         if(!Display.isCloseRequested())  {
             Display.destroy();
         }
         
         // MemObjects
         if(memLPARandoms != null)
         	OpenCL.clReleaseMemObject(memLPARandoms);
         if(memLifetimes != null)
         	OpenCL.clReleaseMemObject(memLifetimes);
         if(memVelocities != null)
         	OpenCL.clReleaseMemObject(memVelocities);
         if(memPositions != null)
         	OpenCL.clReleaseMemObject(memPositions);
         if(memLPAs != null)
         	OpenCL.clReleaseMemObject(memLPAs);
         if(memNewParticles != null)
             OpenCL.clReleaseMemObject(memNewParticles);
 
         // Kernels
         if(kernelSpawn != null)
         	OpenCL.clReleaseKernel(kernelSpawn);
         if(kernelMove != null)
         	OpenCL.clReleaseKernel(kernelMove);
         
         // OpenCL Context
         if(program != null)
         	OpenCL.clReleaseProgram(program);
         if(queue != null)
         	OpenCL.clReleaseCommandQueue(queue);
         if(context != null)
         	OpenCL.clReleaseContext(context);
         
         // OpenCL and OpenGL
         CLUtil.destroyCL();
         GL.destroy();
     }
 	
 	/**
 	 * calculates FPS
 	 * @param deltaTime
 	 */
 	private void calculateFramesPerSecond(long deltaTime) {
 		numberOfFrames++;
 		sumDeltaTime += deltaTime;
         if(sumDeltaTime > 1000) {
         	fps = numberOfFrames / (float)(sumDeltaTime / 1000);
         	numberOfFrames = 0;
         	sumDeltaTime   = 0;
         	Display.setTitle("FPS: " + fps);
         }
 	}
 	
 	private void printControls() {
 	    String[] keyDesc = new String[]{
 	        "W", "Move for",
 	        "S", "Move back",
 	        "A", "Move left",
 	        "D", "Move right",
 	        "SPACE", "Move up",
 	        "C", "Move down",
 	        "", "",
 	        "F", "(Un-)Limit FPS to 60",
 	        "L", "Show Low Pressure Areas",
 	        "E", "Pause animation",
 	        "H", "debug mode",
 	        "P", "debug pulse (press repeatedly)",
 	        "", "",
 	        "LMB", "Future: Blow the flame",
 	        "RMB", "Turn the camera",
 	    };
 	    System.out.println("\nControls:");
 	    for(int i = 0; i < keyDesc.length; i+=2) {
 	        System.out.printf("%-5s %s\n", keyDesc[i], keyDesc[i+1]);
 	    }
 	    System.out.println();
 	}
 }
