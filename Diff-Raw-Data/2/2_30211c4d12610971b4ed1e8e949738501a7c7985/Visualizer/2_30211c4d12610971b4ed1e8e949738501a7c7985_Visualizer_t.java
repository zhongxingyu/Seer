 package sph;
 
 import static pa.cl.OpenCL.CL_MEM_READ_WRITE;
 import static pa.cl.OpenCL.clCreateFromGLBuffer;
 import static pa.cl.OpenCL.clEnqueueAcquireGLObjects;
 import static pa.cl.OpenCL.clEnqueueReleaseGLObjects;
 import static pa.cl.OpenCL.clReleaseMemObject;
 import static pa.cl.OpenCL.clSetKernelArg;
 
 import java.nio.FloatBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opencl.CLCommandQueue;
 import org.lwjgl.opencl.CLContext;
 import org.lwjgl.opencl.CLKernel;
 import org.lwjgl.opencl.CLMem;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL15;
 import org.lwjgl.opengl.GL20;
 import org.lwjgl.opengl.GL30;
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 
 import visualize.FrameWork;
 import visualize.gl.FrameBuffer;
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
             new Params(0.016f, 0.68f, 20.0f, 0.1f, 1.0f, 0.8f, 0, -2, -30.0f),
             /*new Params(0.0006f, 0.16f, 1000.0f, 1.0f, 1.0f, 0.07f, 0, 0, -1.5f),
             new Params(0.0006f, 0.16f, 1000.0f, 1.0f, 1.0f, 0.07f, 0, 0, -1.5f),
             new Params(0.0019f, 0.32f, 276.0f, 1.0f, 1.0f, 0.07f, 0, 0, -5.0f), */
             new Params(0.00016f, 0.32f, 272.0f, 0.145f, 1.0f, 0.08f, 0, 0, -5.0f),
             new Params(0.016f, 6.04f, 0.0f, 1.0f, 1.0f, 0.76f, 0, 0, -50.0f)
         };
     
     protected Params m_currentParams = m_demoParams[2];
     
     protected CLKernel m_kernel;
     protected CLCommandQueue m_queue = null;
     
     protected int m_invCameraAdress;
     
     protected Geometry[] m_buffer = new Geometry[2];
     
     protected Program m_program;
     protected Program m_quadProgram;
     
     protected FrameBuffer frameBuffer;
     
     protected int framebuffer_id;
     protected int depthStencil_id;
     
     protected int m_toggle = 0;
     protected long m_lastTimeSteps = 0;
     protected CLMem m_oglBuffer0;
     protected CLMem m_oglBuffer1;
     
     private boolean m_pause = false;
     
     public Visualizer(int w, int h) 
     {
         super(w, h, true, true, "", false, false);
     }
     
     public Params getCurrentParams()
     {
         return m_currentParams;
     }
     
     public Program getProgram()
     {
         return m_program;
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
     	
     	//First Texture
     	Texture frameTexture = Texture.create2DTexture(GL11.GL_RGBA, GL30.GL_RGBA16F, GL11.GL_FLOAT, width, height, 0, null);
     	//Depth Informations
     	Texture depthTexture = Texture.create2DTexture(GL11.GL_RED,  GL30.GL_R16F,    GL11.GL_FLOAT, width, height, 1, null);
     	//World Coordinates
     	Texture worldTexture = Texture.create2DTexture(GL11.GL_RGB, GL30.GL_RGB16F, GL11.GL_FLOAT, width, height, 2, null);
     	//Create Frame buffer
         frameBuffer = FrameBuffer.createFrameBuffer("main", true, frameTexture, depthTexture, worldTexture);        
         
         //Setup Particle Program
         m_program = new Program();
         m_program.create("shader/Particles_VS.glsl", "shader/Particles_FS.glsl");
         m_program.bindAttributeLocation("vs_in_pos", 0);
         m_program.bindAttributeLocation("vs_in_normal", 1);
         m_program.bindAttributeLocation("vs_in_tc", 2);
         m_program.bindAttributeLocation("vs_in_instance", 3);
         m_program.linkAndValidate();
         m_program.bindUniformBlock("Camera", FrameWork.UniformBufferSlots.CAMERA_BUFFER_SLOT);
         m_program.bindUniformBlock("Color", FrameWork.UniformBufferSlots.COLOR_BUFFER_SLOT);
         m_program.use();
 
         Matrix4f m = new Matrix4f();
         m.setIdentity();
         m.store(MATRIX4X4_BUFFER);
         MATRIX4X4_BUFFER.flip();
   
         m_invCameraAdress = m_program.getUniformLocation("invCamera");
         GL20.glUniformMatrix4(m_invCameraAdress, false, MATRIX4X4_BUFFER);
         
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
 
     	m_quadProgram.use();
 //        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 //        GL11.glDisable(GL11.GL_CULL_FACE);
         
        
         FloatBuffer data = BufferUtils.createFloatBuffer(4);
         data.put(0.5f); data.put(1); data.put(0); data.put(1);
         data.flip();
         m_color.loadFloatData(data, GL15.GL_DYNAMIC_DRAW);
         
         m_camera.setSpeed(0.25f);
         
         m_camera.lookAt(new Vector3f(0,0, m_currentParams.m_z), new Vector3f());
         uploadCameraBuffer();
         
     }
 
     @Override
     public void close() 
     {
         clEnqueueReleaseGLObjects(m_queue, m_oglBuffer0, null, null);
         //clEnqueueReleaseGLObjects(m_queue, m_oglBuffer1, null, null);
         if(m_oglBuffer0 != null)
         {
             clReleaseMemObject(m_oglBuffer0);
             m_oglBuffer0 = null;
         }
         if(m_oglBuffer1 != null)
         {
             clReleaseMemObject(m_oglBuffer1);
             m_oglBuffer1 = null;
         }
         
         m_program.delete();
         m_buffer[0].delete();
         m_buffer[1].delete();
         frameBuffer.delete();
         destroy();
     }
     
     public void setKernelAndQueue(CLKernel kernel, CLCommandQueue queue)
     {
         m_kernel = kernel;
         m_queue = queue;
         clSetKernelArg(m_kernel, 2, m_currentParams.m_timeStep);
     }
     
     public CLMem[] createPositions(float[] pos, CLContext context) {
 		if (m_buffer[0] != null) {
 			m_buffer[0].delete();
 		}
 
 		m_buffer[0] = GeometryFactory.createParticles(pos,
 				m_currentParams.m_pointSize * 0.9f, 4);
 
 		if (m_buffer[1] != null) {
 			m_buffer[1].delete();
 		}
 
 		m_buffer[1] = GeometryFactory.createCube(new float[] { 0, 0, 0 }, 1);
 
 		m_oglBuffer0 = clCreateFromGLBuffer(context, CL_MEM_READ_WRITE,
 				m_buffer[0].getInstanceBuffer(0).getId());
 		// m_oglBuffer1 = clCreateFromGLBuffer(context, CL_MEM_READ_WRITE,
 		// m_buffer[1].getInstanceBuffer(0).getId());
 
 		CLMem pair[] = new CLMem[2];
 		pair[0] = m_oglBuffer0;
 		pair[1] = m_oglBuffer1;
 
 		clEnqueueAcquireGLObjects(m_queue, m_oglBuffer0, null, null);
 		// clEnqueueAcquireGLObjects(m_queue, m_oglBuffer1, null, null);
 
 		return pair;
 	}
 
 	public void visualize() {
 		render();
 	}
 
     @Override
     public void render() 
     {
     	clEnqueueReleaseGLObjects(m_queue, m_oglBuffer0, null, null);
        	//clEnqueueReleaseGLObjects(m_queue, m_oglBuffer1, null, null);
     	
         updateInput();
 
         //Bind and Clear Framebuffer
         frameBuffer.renderToFramebuffer();
         
         //Draw Box
         
         m_quadProgram.use();
         setColor(1f, 1f, 1f, 1f);
         m_buffer[1].draw();
         
         //Draw Particles
         m_program.use();
         setColor(0.5f, 0.5f, 1f, 1f);
         m_buffer[0].draw();
         
         //Swap back to Backbuffer and Draw Texture
        frameBuffer.renderToBackbuffer(0);
 
         Display.update();
         
        	clEnqueueAcquireGLObjects(m_queue, m_oglBuffer0, null, null);
         //clEnqueueAcquireGLObjects(m_queue, m_oglBuffer1, null, null);
         m_timer.tick();
         Display.setTitle("SPH Simulation (FPS: "+m_timer.getFps()+")");
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
             m_program.use();
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
     }
     
     public boolean isPause()
     {
         return m_pause;
     }
     public void setPause(boolean pause) {
     	m_pause = pause;
     }
 }
