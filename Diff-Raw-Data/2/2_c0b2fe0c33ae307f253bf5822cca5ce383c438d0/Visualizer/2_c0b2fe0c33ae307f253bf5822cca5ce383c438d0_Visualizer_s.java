 package sph;
 
 import static pa.cl.OpenCL.CL_MEM_COPY_HOST_PTR;
 import static pa.cl.OpenCL.CL_MEM_READ_WRITE;
 import static pa.cl.OpenCL.clCreateBuffer;
 import static pa.cl.OpenCL.clCreateFromGLBuffer;
 import static pa.cl.OpenCL.clEnqueueAcquireGLObjects;
 import static pa.cl.OpenCL.clEnqueueReleaseGLObjects;
 import static pa.cl.OpenCL.clReleaseMemObject;
 import static pa.cl.OpenCL.clSetKernelArg;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.WritableRaster;
 import java.io.File;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 import javax.imageio.ImageIO;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opencl.CLCommandQueue;
 import org.lwjgl.opencl.CLContext;
 import org.lwjgl.opencl.CLKernel;
 import org.lwjgl.opencl.CLMem;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 import org.lwjgl.opengl.GL13;
 import org.lwjgl.opengl.GL15;
 import org.lwjgl.opengl.GL20;
 import org.lwjgl.opengl.GL30;
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 
 import pa.cl.OpenCL;
 import pa.util.math.MathUtil;
 import sph.helper.Settings;
 
 import visualize.FrameWork;
 import visualize.gl.FrameBuffer;
 import visualize.gl.GLUtil;
 import visualize.gl.GeometryFactory;
 import visualize.gl.Texture;
 import visualize.gl.GeometryFactory.Geometry;
 import visualize.gl.Texture.TextureDescription;
 import visualize.gl.Program;
 import visualize.util.Timer;
 
 public class Visualizer extends FrameWork 
 {    
     public static class Params
     {
         public float m_timeStep;
         public float m_clusterScale;
         public float m_velocityScale;
         public float m_softening;
         public float m_damping;
         public float m_pointSize;
         public float m_x, m_y, m_z;
         
         protected Params(float ts, float cs, float vs, float soft, float damp, float ps, float x, float y, float z)
         {
             m_timeStep = ts;
             m_clusterScale = cs;
             m_velocityScale = vs;
             m_softening = soft;
             m_damping = damp;
             m_x = x;
             m_y = y;
             m_z = z;
             m_pointSize = ps;
         }
     }
     
     protected Params m_demoParams[] = 
         {
             new Params(0.016f, 1.54f, 8.0f, 0.1f, 1.0f, 1.0f, 0, -2, -100.0f),
             new Params(0.0016f, 0.68f, 20.0f, 0.1f, 1.0f, 0.8f, 0, -2, -30.0f),
             /*new Params(0.0006f, 0.16f, 1000.0f, 1.0f, 1.0f, 0.07f, 0, 0, -1.5f),
             new Params(0.0006f, 0.16f, 1000.0f, 1.0f, 1.0f, 0.07f, 0, 0, -1.5f),
             new Params(0.0019f, 0.32f, 276.0f, 1.0f, 1.0f, 0.07f, 0, 0, -5.0f), */
             new Params(0.0002f, 0.32f, 272.0f, 0.145f, 1.0f, 0.08f, 0, 0, -5.0f),
             new Params(0.016f, 6.04f, 0.0f, 1.0f, 1.0f, 0.76f, 0, 0, -50.0f)
         };
     
     protected Params m_currentParams = m_demoParams[2];
     
     protected CLKernel m_kernel;
     protected CLCommandQueue m_queue = null;
     
     protected int m_invCameraAdress;
     
     protected Geometry[] m_buffer = new Geometry[4];
     protected Geometry dynamicScreenSquad;
 	
     protected Program m_particleProgram;
     protected Program m_quadProgram;
     protected Program m_envProgram;
     protected Program m_surfaceProgram;
     protected Program frameBufferProgram;
     
     protected FrameBuffer frameBuffer;
     protected FrameBuffer thicknessFrameBuffer;
     protected int framebuffer_id;
     protected int depthStencil_id;
     
     protected int m_toggle = 0;
     protected long m_lastTimeSteps = 0;
     protected CLMem m_oglBuffer0;
     protected CLMem m_oglBuffer1;
     protected CLMem m_oglBuffer2;
     protected CLMem m_oglBuffer3;
     protected CLMem m_oglBuffer4;
 
     protected SPH sph;
     
     protected FloatBuffer settingsBuffer;
     protected IntBuffer imageBuffer;
     protected int[] imageBufferArray;
     
     private boolean m_pause = false;
     
     protected long imageCount = -1;
     
     public Visualizer(SPH sph, int w, int h) 
     {
         super(w, h, true, true, "", false, false);
         this.sph = sph;
     }
     
     public Params getCurrentParams()
     {
         return m_currentParams;
     }
     
     public Program getProgram()
     {
         return m_particleProgram;
     }
     
     public Timer getTimer()
     {
         return m_timer;
     }
 
     @Override
     public void init() 
     {   	
     	//Setup Textures
     	int width = FrameWork.instance().getWidth();
     	int height = FrameWork.instance().getHeight();
     	
         //Setup Program Variables
 		frameBufferProgram = new Program();
 		frameBufferProgram.create("shader/ScreenQuad_VS.glsl", "shader/ScreenQuad_FS.glsl");
 		frameBufferProgram.bindAttributeLocation("vs_in_position", 0);
 		frameBufferProgram.bindAttributeLocation("vs_in_tc", 1);
 		frameBufferProgram.linkAndValidate();
 		frameBufferProgram.bindUniformBlock("Camera", FrameWork.UniformBufferSlots.CAMERA_BUFFER_SLOT);
 		frameBufferProgram.bindUniformBlock("Color", FrameWork.UniformBufferSlots.COLOR_BUFFER_SLOT);
 		frameBufferProgram.bindUniformBlock("Settings", FrameWork.UniformBufferSlots.SETTINGS_BUFFER_SLOT);
         dynamicScreenSquad = GeometryFactory.createDynamicScreenQuad();
         
     	//First Texture
     	Texture frameTexture = Texture.create2DTexture("color", GL11.GL_RGBA, GL30.GL_RGBA16F, GL11.GL_FLOAT, width, height, 0, null);
     	//Depth Informations
     	Texture depthTexture = Texture.create2DTexture("depth",GL11.GL_RED,  GL30.GL_R16F,    GL11.GL_FLOAT, width, height, 1, null);
     	//World Coordinates
     	Texture worldTexture = Texture.create2DTexture("world",GL11.GL_RGB, GL30.GL_RGB16F, GL11.GL_FLOAT, width, height, 2, null);
     	//Particle normals
     	Texture normalTexture = Texture.create2DTexture("normal",GL11.GL_RGB, GL30.GL_RGB16F, GL11.GL_FLOAT, width, height, 3, null);
     	//specular Informations
     	Texture specTexture = Texture.create2DTexture("specular",GL11.GL_RGB, GL30.GL_RGB16F, GL11.GL_FLOAT, width, height, 4, null);
     	//Diffuse Texture
     	Texture diffTexture = Texture.create2DTexture("diffuse",GL11.GL_RGB, GL30.GL_RGB16F, GL11.GL_FLOAT, width, height, 5, null);
     	//Freshnell Texture
     	Texture freshnelTexture = Texture.create2DTexture("freshel",GL11.GL_RGB, GL30.GL_RGB16F, GL11.GL_FLOAT, width, height, 11, null);
     	//thickness Texture
     	Texture thicknessTexture = Texture.create2DTexture("thickness", GL11.GL_RGBA, GL30.GL_RGBA16F, GL11.GL_FLOAT, width, height, 6, null);
     	//Create Frame buffer
         frameBuffer = FrameBuffer.createFrameBuffer(frameBufferProgram ,"main", true, frameTexture, depthTexture, worldTexture, normalTexture, specTexture ,diffTexture);        
         frameBufferProgram.use();
         GL20.glUniform1i(frameBufferProgram.getUniformLocation(frameTexture.getDest().name), frameTexture.getUInt());
         GL20.glUniform1i(frameBufferProgram.getUniformLocation(depthTexture.getDest().name), depthTexture.getUInt());
         GL20.glUniform1i(frameBufferProgram.getUniformLocation(worldTexture.getDest().name), worldTexture.getUInt());
         GL20.glUniform1i(frameBufferProgram.getUniformLocation(normalTexture.getDest().name), normalTexture.getUInt());
         GL20.glUniform1i(frameBufferProgram.getUniformLocation(specTexture.getDest().name), specTexture.getUInt());
         GL20.glUniform1i(frameBufferProgram.getUniformLocation(diffTexture.getDest().name), diffTexture.getUInt());
         GL20.glUniform1i(frameBufferProgram.getUniformLocation(freshnelTexture.getDest().name), freshnelTexture.getUInt());
         GL20.glUniform1i(frameBufferProgram.getUniformLocation(thicknessTexture.getDest().name), thicknessTexture.getUInt());
         
         thicknessFrameBuffer = FrameBuffer.createFrameBuffer(null, "thickness", true, thicknessTexture);
         //Setup Particle Program
         m_particleProgram = new Program();
         m_particleProgram.create("shader/Particles_VS.glsl", "shader/Particles_FS.glsl");
         m_particleProgram.bindAttributeLocation("vs_in_pos", 0);
         m_particleProgram.bindAttributeLocation("vs_in_normal", 1);
         m_particleProgram.bindAttributeLocation("vs_in_tc", 2);
         m_particleProgram.bindAttributeLocation("vs_in_instance", 3);
         m_particleProgram.linkAndValidate();
         m_particleProgram.bindUniformBlock("Camera", FrameWork.UniformBufferSlots.CAMERA_BUFFER_SLOT);
         m_particleProgram.bindUniformBlock("Color", FrameWork.UniformBufferSlots.COLOR_BUFFER_SLOT);
         m_particleProgram.bindUniformBlock("Settings", FrameWork.UniformBufferSlots.SETTINGS_BUFFER_SLOT);
         m_particleProgram.use();
         GL20.glUniform1i(m_particleProgram.getUniformLocation(thicknessTexture.getDest().name), thicknessTexture.getUInt());
         
         Matrix4f m = new Matrix4f();
         m.setIdentity();
         m.store(MATRIX4X4_BUFFER);
         MATRIX4X4_BUFFER.flip();
   
         m_invCameraAdress = m_particleProgram.getUniformLocation("invCamera");
         GL20.glUniformMatrix4(m_invCameraAdress, false, MATRIX4X4_BUFFER);
 
         Texture floorTexture = Texture.createFromFile("textures/floor_sand.jpg", 8);
         Texture floorBumpTexture = Texture.createFromFile("textures/floor_sand_bump.jpg", 9);
         GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
         GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
         
         GLUtil.checkError();
 
 
         //Setup Box Program
     	m_quadProgram = new Program();
     	m_quadProgram.create("shader/Quad_VS.glsl", "shader/Quad_FS.glsl");
     	m_quadProgram.bindAttributeLocation("vs_in_pos", 0);
     	m_quadProgram.bindAttributeLocation("vs_in_normal", 1);
     	m_quadProgram.bindAttributeLocation("vs_in_tc", 2);
     	m_quadProgram.bindAttributeLocation("vs_in_instance", 3);
     	m_quadProgram.linkAndValidate();
     	m_quadProgram.bindUniformBlock("Camera", FrameWork.UniformBufferSlots.CAMERA_BUFFER_SLOT);
     	m_quadProgram.bindUniformBlock("Color", FrameWork.UniformBufferSlots.COLOR_BUFFER_SLOT);
     	m_quadProgram.bindUniformBlock("Settings", FrameWork.UniformBufferSlots.SETTINGS_BUFFER_SLOT);
     	m_quadProgram.use();
     	
     	m_envProgram = new Program();
     	m_envProgram.create("shader/Env_VS.glsl", "shader/Env_FS.glsl");
     	m_envProgram.bindAttributeLocation("vs_in_pos", 0);
     	m_envProgram.bindAttributeLocation("vs_in_normal", 1);
     	m_envProgram.bindAttributeLocation("vs_in_tc", 2);
     	m_envProgram.bindAttributeLocation("vs_in_instance", 3);
     	m_envProgram.linkAndValidate();
     	m_envProgram.bindUniformBlock("Camera", FrameWork.UniformBufferSlots.CAMERA_BUFFER_SLOT);
     	m_envProgram.bindUniformBlock("Color", FrameWork.UniformBufferSlots.COLOR_BUFFER_SLOT);
     	m_envProgram.bindUniformBlock("Settings", FrameWork.UniformBufferSlots.SETTINGS_BUFFER_SLOT);
     	m_envProgram.use();
 
     	GL20.glUniform1i(m_envProgram.getUniformLocation("floorTex"), floorTexture.getUInt());
     	GL20.glUniform1i(m_envProgram.getUniformLocation("floorBumpTex"), floorBumpTexture.getUInt());
     	
     	//Setup Surface Program
     	m_surfaceProgram = new Program();
     	m_surfaceProgram.create("shader/Surface_VS.glsl", "shader/Surface_FS.glsl");
     	m_surfaceProgram.bindAttributeLocation("vs_in_pos", 0);
     	m_surfaceProgram.bindAttributeLocation("vs_in_normal", 1);
     	m_surfaceProgram.bindAttributeLocation("vs_in_tc", 2);
     	m_surfaceProgram.bindAttributeLocation("vs_in_instance", 3);
     	m_surfaceProgram.linkAndValidate();
     	m_surfaceProgram.bindUniformBlock("Camera", FrameWork.UniformBufferSlots.CAMERA_BUFFER_SLOT);
     	m_surfaceProgram.bindUniformBlock("Color", FrameWork.UniformBufferSlots.COLOR_BUFFER_SLOT);
     	m_surfaceProgram.bindUniformBlock("Settings", FrameWork.UniformBufferSlots.SETTINGS_BUFFER_SLOT);
     	m_surfaceProgram.use();
         FloatBuffer data = BufferUtils.createFloatBuffer(4);
         data.put(0.5f); data.put(1); data.put(0); data.put(1);
         data.flip();
         m_color.loadFloatData(data, GL15.GL_DYNAMIC_DRAW);
         //setBlur(10.0f);
         
         m_camera.setSpeed(0.25f);
         GLUtil.checkError();
         m_camera.lookAt(new Vector3f(1.5f,0.6f, -2), new Vector3f(0, 0.2f, 0));
         uploadCameraBuffer();
         
      	GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f );
         GLUtil.checkError();
         
         if(Settings.GENERATE_VIDEO) {
         	(new File(Settings.OUTPUT_FOLDER)).mkdir();
         	imageBuffer = BufferUtils.createIntBuffer(width*height*3);
         	imageCount = 0;
         	imageBufferArray = new int[imageBuffer.capacity()];
         }
     }
 
     @Override
     public void close() 
     {
         if(m_oglBuffer0 != null)
         {
         	clEnqueueReleaseGLObjects(m_queue, m_oglBuffer0, null, null);
             clReleaseMemObject(m_oglBuffer0);
             m_oglBuffer0 = null;
         }
         if(m_oglBuffer1 != null)
         {
         	clEnqueueReleaseGLObjects(m_queue, m_oglBuffer1, null, null);
             clReleaseMemObject(m_oglBuffer1);
             m_oglBuffer1 = null;
         }
         if(m_oglBuffer2 != null)
         {
         	clEnqueueReleaseGLObjects(m_queue, m_oglBuffer2, null, null);
             clReleaseMemObject(m_oglBuffer2);
             m_oglBuffer2 = null;
         }
         if(m_oglBuffer3 != null)
         {
         	clEnqueueReleaseGLObjects(m_queue, m_oglBuffer3, null, null);
             clReleaseMemObject(m_oglBuffer3);
             m_oglBuffer3 = null;
         }
         if(m_oglBuffer4 != null)
         {
         	clEnqueueReleaseGLObjects(m_queue, m_oglBuffer4, null, null);
             clReleaseMemObject(m_oglBuffer4);
             m_oglBuffer4 = null;
         }
         
         m_quadProgram.delete();
         m_surfaceProgram.delete();
         frameBufferProgram.delete();
         
         if(m_buffer[0] != null)
         {
         	m_buffer[0].delete();	
         }
         if(m_buffer[1] != null)
         {
         	m_buffer[1].delete();	
         }
         if(m_buffer[2] != null)
         {
         	m_buffer[2].delete();	
         }
         if(m_buffer[3] != null)
         {
         	m_buffer[3].delete();	
         }
         frameBuffer.delete();
         thicknessFrameBuffer.delete();
         dynamicScreenSquad.delete();
         destroy();
     }
     
     public void setKernelAndQueue(CLKernel kernel, CLCommandQueue queue)
     {
         m_kernel = kernel;
         m_queue = queue;
         clSetKernelArg(m_kernel, 2, m_currentParams.m_timeStep);
     }
     
     public CLMem[] createPositions(float[] pos,float[] normal, CLContext context, float[] vertices, int[] indices) {
 
 //		if (m_buffer[0] != null) {
 //			m_buffer[0].delete();
 //		}
 //
 //		m_buffer[0] = GeometryFactory.createParticles(pos, normal,
 //				m_currentParams.m_pointSize * 0.9f, 4);
     	
     	if (m_buffer[0] != null) {
 			m_buffer[0].delete();
 		}
 
 		m_buffer[0] = GeometryFactory.createCube(new Vector3f(0, 18.99f, 0), new float[] { 0, 0, 0}, 1, 20, false);
 
 		if (m_buffer[1] != null) {
 			m_buffer[1].delete();
 		}
 
 		m_buffer[1] = GeometryFactory.createCube(new Vector3f(0, 0, 0), new float[] { 0, 0, 0 }, 1, 1, true);
 		
 		if (m_buffer[2] != null) {
 			m_buffer[2].delete();
 		}
 		
 		m_buffer[2] = GeometryFactory.createSurface(new float[] { 0, 0, 0 }, vertices, indices);
 		
 		if (m_buffer[3] != null) {
 			m_buffer[3].delete();
 		}
 		
 		m_buffer[3] = GeometryFactory.createParticles(pos, m_currentParams.m_pointSize * 1.6f, 4);
 		
 		m_oglBuffer1 = clCreateFromGLBuffer(context, CL_MEM_READ_WRITE,
 				m_buffer[2].getVertexBuffer().getId());
 		m_oglBuffer2 = clCreateFromGLBuffer(context, CL_MEM_READ_WRITE,
 				m_buffer[2].getIndexBuffer().getId());
 		m_oglBuffer4 = clCreateFromGLBuffer(context, CL_MEM_READ_WRITE,
 				m_buffer[3].getInstanceBuffer(0).getId());
 
 		
 		CLMem pair[] = new CLMem[4];
 		pair[0] = m_oglBuffer1;
 		pair[1] = m_oglBuffer2;
 		pair[3] = m_oglBuffer4;
 
 		clEnqueueAcquireGLObjects(m_queue, m_oglBuffer1, null, null);
 		clEnqueueAcquireGLObjects(m_queue, m_oglBuffer2, null, null);
 		clEnqueueAcquireGLObjects(m_queue, m_oglBuffer4, null, null);
 		return pair;
 	}
 
 	public void visualize() {
 		render();
 	}
 
     @Override
     public void render() 
     {
 
        	clEnqueueReleaseGLObjects(m_queue, m_oglBuffer1, null, null);
        	clEnqueueReleaseGLObjects(m_queue, m_oglBuffer2, null, null);
        	clEnqueueReleaseGLObjects(m_queue, m_oglBuffer4, null, null); 
        	
     	GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
      	GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
      	GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f );
      	 GLUtil.checkError();
         updateInput();
         //Draw skybox
         m_envProgram.use();
         GL11.glDisable(GL11.GL_CULL_FACE);
         setColor(1f, 1f, 1f, 1f);
         m_buffer[0].draw();
         GL11.glEnable(GL11.GL_CULL_FACE);
         GLUtil.checkError();
         
         //Draw Cube
         m_quadProgram.use();
       	setColor(1f, 1f, 1f, 1f);
       	m_buffer[1].draw(); 
 
       	thicknessFrameBuffer.renderToFramebuffer();
       	GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
       	//Draw Thickness
       	m_particleProgram.use();
   		GL11.glEnable(GL11.GL_BLEND);
   		GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_DST_ALPHA);
   		GL11.glDisable(GL11.GL_DEPTH_TEST);
   		GL11.glDepthFunc(GL11.GL_ALWAYS);
       	setColor(1f, 1f, 1f, 0.1f);
       	m_buffer[3].draw();
       	GL11.glEnable(GL11.GL_DEPTH_TEST);
       	GL11.glDepthFunc(GL11.GL_LESS);
       	GL11.glDisable(GL11.GL_BLEND);
       	
         //Bind and Clear Framebuffer
         frameBuffer.renderToFramebuffer();
         GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
         GL11.glEnable(GL11.GL_BLEND);
         GL11.glBlendFunc(GL11.GL_DST_ALPHA,GL11.GL_ONE_MINUS_DST_ALPHA);
         //Draw Surface
         m_surfaceProgram.use();
         setColor(0.3f, 0.3f, 1f, 0.8f);
         m_buffer[2].draw();
 
         GL11.glDisable(GL11.GL_BLEND);
         //Swap back to Backbuffer 
         frameBuffer.renderToBackbuffer();
         //GL20.glUniform1i(frameBufferProgram.getUniformLocation("thickness"), 6);
         frameBufferProgram.use();
         //GL11.glEnable(GL11.GL_BLEND);
         //GL11.glBlendFunc(GL11.GL_DST_COLOR,GL11.GL_ONE_MINUS_DST_COLOR);
         setColor(0.3f, 0.3f, 1f, 0.8f);
         dynamicScreenSquad.draw();
         //GL11.glDisable(GL11.GL_BLEND);
 
         Display.update();
      	
         clEnqueueAcquireGLObjects(m_queue, m_oglBuffer1, null, null);
        	clEnqueueAcquireGLObjects(m_queue, m_oglBuffer2, null, null);
        	clEnqueueAcquireGLObjects(m_queue, m_oglBuffer4, null, null);
        	
       	if(Settings.GENERATE_VIDEO) {
        		GL11.glReadPixels(0, 0, 1024, 768, GL11.GL_RGB, GL11.GL_UNSIGNED_INT, imageBuffer);
        		BufferedImage image = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_RGB);
             WritableRaster raster = (WritableRaster) image.getData();
             //float[] tmp = new float[imageBuffer.capacity()];
            // float[] tmp = new float[1024*768*3];
             imageBuffer.get(imageBufferArray, 0, imageBufferArray.length);
             imageBuffer.position(0);
             raster.setPixels(0,0,1024,768, imageBufferArray);
             image.setData(raster);
             flipImage(image);
             try {
 				ImageIO.write(image, "png", new File("output/frame_"+(++imageCount)+".png"));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
        	}
         m_timer.tick();
         Display.setTitle("SPH Simulation (FPS: "+m_timer.getFps()+")");
         GLUtil.checkError();
     }
     
     public boolean isDone()
     {
         return m_done || Display.isCloseRequested();
     }
     
     @Override
     public void processMouseMoved(int x, int y, int dx, int dy) 
     {
         super.processMouseMoved(x, y, dx, dy);
         if(m_enableCamera && Mouse.isGrabbed())
         {
             m_particleProgram.use();
             Matrix4f mat = new Matrix4f();
             mat.rotate(m_camera.getPhi(), new Vector3f(0, 1, 0));
             mat.rotate(m_camera.getTheta(), new Vector3f(1, 0, 0));
             
             mat.store(MATRIX4X4_BUFFER);
             MATRIX4X4_BUFFER.position(0);
             
             GL20.glUniformMatrix4(m_invCameraAdress, false, MATRIX4X4_BUFFER);
             
         }
     }
     public void processKeyPressed(int key)
     {
         super.processKeyPressed(key);
         if(key == Keyboard.KEY_P)
         {
             m_pause = !m_pause;
         }
         if(key == Keyboard.KEY_NUMPAD1)
         {
             float blur = SETTINGS_BUFFER.get(0);
             blur -= 0.5f;
             setBlur(Math.max(Math.min(blur, 20.0f), 0));
             System.out.println("set blur size to: "+ Math.max(Math.min(blur, 20.0f), 0));
         }
         if(key == Keyboard.KEY_NUMPAD3)
         {
             float blur = SETTINGS_BUFFER.get(0);
             blur += 0.5f;
             setBlur(Math.max(Math.min(blur, 20.0f), 0));
             System.out.println("set blur size to: "+ Math.max(Math.min(blur, 20.0f), 0));
         }
         sph.processKeyPressed(key);
     }
     
     public boolean isPause()
     {
         return m_pause;
     }
     public void setPause(boolean pause) {
     	m_pause = pause;
     }
     void flipImage(BufferedImage image) {
         WritableRaster raster = image.getRaster();
         int h = raster.getHeight();
         int w = raster.getWidth();
         int x0 = raster.getMinX();
         int y0 = raster.getMinY();
         for (int x = x0; x < x0 + w; x++){
             for (int y = y0; y < y0 + h / 2; y++){
                 int[] pix1 = new int[3];
                 pix1 = raster.getPixel(x, y, pix1);
                 int[] pix2 = new int[3];
                 pix2 = raster.getPixel(x, y0 + h - 1 - (y - y0), pix2);
                 raster.setPixel(x, y, pix2);
                 raster.setPixel(x, y0 + h - 1 - (y - y0), pix1);
             }
         }
         return;
    }
 }
