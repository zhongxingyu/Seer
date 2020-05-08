 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.Random;
 import java.util.Vector;
 
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.awt.GLJPanel;
 import javax.media.opengl.glu.GLU;
 import javax.swing.JFrame;
 
 import com.jogamp.opengl.util.FPSAnimator;
 
 import drawables.*;
 import drawables.modeled.GLModel;
 import drawables.modeled.Lamp;
 import drawables.modeled.Spotlight;
 import drawables.modeled.SpotlightA;
 import drawables.modeled.SpotlightB;
 
 @SuppressWarnings("serial")
 public class Scene extends GLJPanel implements GLEventListener {
 
 	private static int width;
 	private static int height;
 	private FPSAnimator animator;
 	public Camera camera;
 	public Fog fog;
 
 	//private GLModel spotModel = null;
 	//private GLModel lampModel = null;
 
 	private Random random;
 	
 	public int tid_grass;
 
 	long startTime, lastTime;
 	long lapsed, delta;
 	private boolean useShaders = true;
 	private int tid_m;
 	private int tid_m2;
 	private int tid_peron;
 	public int texturesOn;
 	private int sampler0;
 	private int sampler1;
 	public int selected_tid_m;
 	private int fogOn;
 	private Station s;
 	
 	private Column[] columns;
 	private Lamp[] lamps;
 	private Spotlight sl;
 	private Spotlight s2;
 	
 	Vector sceneObjects;
 
 	public Scene() {
 		setFocusable(true);
 		addGLEventListener(this);
 		animator = new FPSAnimator(this, 60, false);
 		animator.start();
 		camera = new Camera();
 		width = height = 800;
 		random = new Random();
 		lastTime = startTime = System.nanoTime();
 		sceneObjects = new Vector<Drawable>();
 		
 	}
 
 	@Override
 	public void display(GLAutoDrawable drawable) {
 		GL2 gl = drawable.getGL().getGL2();
 		gl.glLoadIdentity();
 		fog.Apply();
 		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT );
 
 		long now = System.nanoTime();
 		delta = now - lastTime;
 		if (delta - 50000000 > 0)
 			delta = 50000000;
 		lastTime = now;
 		lapsed = now - startTime;
 		lapsed /= 1000; // Can I haz milisecondz? 
 		delta /= 1000;
 
 		//System.out.println("*");
 		camera.Update(delta);
 		Drawable.lapsedTime = lapsed;
 		
 		gl.glColorMask(true,true,true,true);
 		gl.glPushMatrix();
 		gl.glScalef(1, 1, -1);
 		gl.glTranslatef(0, 0, -8);
 		gl.glEnable(GL2.GL_CLIP_PLANE0);
 		gl.glClipPlane(GL2.GL_CLIP_PLANE0, new double[]{0.0,0.0,-1,4}, 0);
		drawAll(gl, GL2.GL_CW);
 		gl.glDisable(GL2.GL_CLIP_PLANE0);
 		gl.glPopMatrix();
 		
 		
 		
 		gl.glClear(GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT);
 		
 		gl.glStencilOp(GL2.GL_REPLACE, GL2.GL_REPLACE, GL2.GL_REPLACE);
 		gl.glStencilFunc(GL2.GL_ALWAYS, 1, 1);
 		gl.glEnable(GL2.GL_STENCIL_TEST);
 		
 		
 		
 		gl.glColorMask(false,false,false,false);
 		
 		
 		gl.glFrontFace(GL2.GL_CCW);
 		gl.glBegin(GL2.GL_QUADS);
 		gl.glColor4f(0,0,0,0.2f);
 		gl.glVertex3f(-2.5f, 5f, 4);
 		gl.glColor4f(0,0,0,0.2f);
 		gl.glVertex3f(2.5f, 5f, 4);
 		gl.glColor4f(0,0,0,0.2f);
 		gl.glVertex3f(2.5f, -2.5f, 4);
 		gl.glColor4f(0,0,0,0.2f);
 		gl.glVertex3f(-2.5f, -2.5f, 4);
 		gl.glEnd();
 		
 		
 		gl.glStencilOp(GL2.GL_KEEP, GL2.GL_KEEP, GL2.GL_KEEP);
 		gl.glStencilMask(GL2.GL_NOTEQUAL);
 		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
 		gl.glDisable(GL2.GL_STENCIL_TEST);
 		
 		//gl.glColorMask(false,false,false,false);
 		gl.glColorMask(true,true,true,true);
		drawAll(gl, GL2.GL_CCW);
 
 		gl.glFlush();
 		try {
 			Thread.sleep(3);
 		} catch (InterruptedException e) {
 			// e.printStackTrace();
 		}
 	}
 
	private void drawAll(GL2 gl, int cull) {
 		gl.glFrontFace(cull);
		setGlobalLight(gl);
 		//s.DrawModels(gl, cull);
 		s.setTexture0(selected_tid_m == 0 ? tid_m : tid_m2);
 		
 		for (int i = 0; i < sceneObjects.size(); i++) {
 			((Drawable)sceneObjects.get(i)).Draw(gl, cull, 1);
 		}
 		
 		for (int i = 0; i < sceneObjects.size(); i++) {
 			((Drawable)sceneObjects.get(i)).Draw(gl, cull, 0);
 		}
 		/*s.Draw(gl, cull, 0);
 		//Station.drawExterior(gl, texturesOn);
 		//Station.drawInterior(gl, tid_peron, selected_tid_m == 0 ? tid_m : tid_m2, texturesOn, sampler0, sampler1);
 		for (int i = -2; i <= 2; i++)
 		{
 			columns[i+2].Draw(gl, cull, 0);
 			//Column.draw(gl, 0, 0, 7 * (float) i, tid_grass, texturesOn);
 		}
 
 		for (int i = -2; i <= 3; i++)
 		{
 			//	drawLamp(gl, 2.35f * (float) i - 2.35f / 2.0f, 0, 0);
 			lamps[i+2].Draw(gl, cull, 0);
 		}
 		
 		
 		
 
 		sl.Draw(gl, cull, 0);
 		s2.Draw(gl, cull, 0);*/
 		//drawSpotA(gl, 4.5f, 0, 0, -90);
 		//drawSpotB(gl, 3, 0, 0, -135);
 	}
 
 	/*
 	private void drawSpotA(GL2 gl, float x, float y, float z, float angle) {
 		float f1 = Math
 				.abs((float) (Math.cos(lapsed * 0.00001f / (10 * 3.14))));
 		gl.glPushMatrix();
 		gl.glTranslatef(0 + x, 3.6f + y, 0 + z);
 		gl.glScalef(40, 40, 40);
 		gl.glRotatef(angle, 0, 1, 0);
 		float SHINE_ALL_DIRECTIONS = 1;
 		float[] lightPos = { 0, 0.0235f, 0.01f, SHINE_ALL_DIRECTIONS };
 		float[] lightDir = { 0, 0, 1, SHINE_ALL_DIRECTIONS };
 
 		
 
 		float[] lightColorAmbient = { 0.02f, 0.02f, 0.02f, 1f };
 		float[] lightColorSpecular = { 1f, 0.5f, 1f, 0.3f };
 
 		// Set light parameters.
 		gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_POSITION, lightPos, 0);
 		gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_SPOT_DIRECTION, lightDir, 0);
 		gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_AMBIENT, lightColorAmbient, 0);
 		gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_SPECULAR, lightColorSpecular, 0);
 		gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_DIFFUSE, lightColorSpecular, 0);
 		gl.glLightf(GL2.GL_LIGHT2, GL2.GL_LINEAR_ATTENUATION, 0.05f);
 		gl.glLightf(GL2.GL_LIGHT2, GL2.GL_QUADRATIC_ATTENUATION, 0.03f);
 		gl.glLightf(GL2.GL_LIGHT2, GL2.GL_SPOT_CUTOFF, 60 * f1);
 		gl.glEnable(GL2.GL_LIGHT2);
 		gl.glUniform1i(texturesOn,0);
 		drawSpot(gl, x, y, z, angle);
 
 		gl.glPopMatrix();
 	}
 
 	private void drawSpotB(GL2 gl, float x, float y, float z, float angle) {
 		float f1 = Math.abs((float) (Math.cos(lapsed * 0.00001f / (5 * 3.14))));
 		float f2 = Math
 				.abs((float) (Math.cos(lapsed * 0.00001f / (20 * 3.14))));
 		gl.glPushMatrix();
 		gl.glTranslatef(0 + x, 3.6f + y, 0 + z);
 		gl.glScalef(40, 40, 40);
 		gl.glRotatef(angle, 0, 1, 0);
 		float SHINE_ALL_DIRECTIONS = 1;
 		float[] lightPos = { 0, 0.0235f, 0.01f, SHINE_ALL_DIRECTIONS };
 		float[] lightDir = { 0, 0, 1, SHINE_ALL_DIRECTIONS };
 
 		float[] lightColorAmbient = { 0.02f, 0.02f, 0.02f, 1f };
 		float[] lightColorSpecular = { 0f, f1, (1 - f1), 1f };
 
 		// Set light parameters.
 		gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_POSITION, lightPos, 0);
 		gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_SPOT_DIRECTION, lightDir, 0);
 		gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_AMBIENT, lightColorAmbient, 0);
 		gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_SPECULAR, lightColorSpecular, 0);
 		gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_DIFFUSE, lightColorSpecular, 0);
 		gl.glLightf(GL2.GL_LIGHT3, GL2.GL_LINEAR_ATTENUATION, 0.05f);
 		gl.glLightf(GL2.GL_LIGHT3, GL2.GL_QUADRATIC_ATTENUATION, 0.03f);
 		gl.glLightf(GL2.GL_LIGHT3, GL2.GL_SPOT_CUTOFF, 60 - 20 * f2);
 		gl.glEnable(GL2.GL_LIGHT3);
 		gl.glUniform1i(texturesOn,0);
 		drawSpot(gl, x, y, z, angle);
 
 		gl.glPopMatrix();
 	}*/
 
 	/*private void drawSpot(GL2 gl, float x, float y, float z, float angle) {
 		gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
 		spotModel.opengldraw(gl);// .draw(gl);
 
 	}*/
 
 	/*
 	private void drawLamp(GL2 gl, float x, float y, float z) {
 		gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
 		gl.glPushMatrix();
 		gl.glScalef(3, 3, 3);
 		gl.glRotatef(90, 0, 1, 0);
 		gl.glTranslatef(0 + x, 3 + y, 0 + z);
 		gl.glUniform1i(texturesOn,0);
 		lampModel.opengldraw(gl);
 		gl.glPopMatrix();
 	}*/
 
 	@Override
 	public void dispose(GLAutoDrawable arg0) {
 		// do nothing
 	}
 
 	@Override
 	public void init(GLAutoDrawable drawable) {
 		
 		GL2 gl = drawable.getGL().getGL2();
 		gl.glEnable(GL2.GL_DEPTH_TEST);
 		gl.glShadeModel(GL2.GL_SMOOTH);
 		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
 		gl.glEnable(GL2.GL_CULL_FACE);
 		gl.glEnable(GL2.GL_NORMALIZE);
 		gl.glMatrixMode(GL2.GL_PROJECTION);
 		gl.glLoadIdentity();
 		gl.glMatrixMode(GL2.GL_MODELVIEW);
 		gl.glLoadIdentity();
 		
 		gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL2.GL_TRUE);
 		gl.glLoadIdentity();
 		GLU glu = new GLU();
 		
 
 		if (false == loadModels(gl)) {
 			System.exit(1);
 		}
 
 		glu.gluPerspective(1, (double) getWidth() / getHeight(), 0.3, 50);
 		gl.glMatrixMode(GL2.GL_MODELVIEW);
 		
 		if (!useShaders)
 		{
 			gl.glEnable(GL2.GL_LIGHTING);
 		}
 		else
 		{
 			setShaders(gl);
 		}
 		
 		
 		
 		gl.glEnable(GL2.GL_TEXTURE_2D);
 		tid_grass = TextureLoader.setupTextures("./gfx/liscie.png", gl);
 		selected_tid_m = 0;
 		tid_m = TextureLoader.setupTextures("./gfx/m.png", gl);
 		tid_m2 = TextureLoader.setupTextures("./gfx/m2.png", gl);
 		tid_peron = TextureLoader.setupTextures("./gfx/peron2.png", gl);
 		System.out.print(tid_grass);
 		gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
 		
 		/*
 		 * texturesOn = gl.glGetUniformLocation(shaderProgram, "texturesOn");
         sampler0 = gl.glGetUniformLocation(shaderProgram, "color_texture0");
         sampler1 = gl.glGetUniformLocation(shaderProgram, "color_texture1");
 		 */
 		
 		s = new Station(sampler0, sampler1, texturesOn, tid_m, tid_peron);
 		sceneObjects.add(s);
 		
 		columns = new Column[5];
 		
 		for (int i = -2; i <= 2; i++)
 		{
 			columns[i+2] = new Column(sampler0, texturesOn, tid_grass);
 			((Drawable)columns[i+2]).SetPos(0, 0,  7 * (float) i);
 			sceneObjects.add(columns[i+2]);
 		}
 		
 		GLModel spotlightmodel = drawables.modeled.ModelLoaderOBJ.LoadModel("./models/spot.obj", "./models/spot.mtl", gl);
 		sl = new SpotlightA(texturesOn, GL2.GL_LIGHT2);
 		sl.SetModel(spotlightmodel);
 		//4.5f, 0, 0, -90);
 		sl.SetPos(4.5f, 0, 0);
 		sl.SetAngles(0, -90, 0);
 		
 		sceneObjects.add(sl);
 		
 		//gl, 3, 0, 0, -135
 		s2 = new SpotlightB(texturesOn, GL2.GL_LIGHT3);
 		s2.SetModel(spotlightmodel);
 		//4.5f, 0, 0, -90);
 		s2.SetPos(3f, 0, 0);
 		s2.SetAngles(0, -135, 0);
 		
 		sceneObjects.add(s2);
 		
 		
 		GLModel lampmodel = drawables.modeled.ModelLoaderOBJ.LoadModel("./models/lamp.obj", "./models/lamp.mtl", gl);
 		lamps = new Lamp[6];
 		for (int i = -2; i <= 3; i++)
 		{
 			lamps[i+2] = new Lamp(texturesOn);
 			lamps[i+2].SetPos(2.35f * (float) i - 2.35f / 2.0f, 0, 0);
 			//drawLamp(gl, 2.35f * (float) i - 2.35f / 2.0f, 0, 0);
 			lamps[i+2].SetModel(lampmodel);
 			sceneObjects.add(lamps[i+2]);
 		}
 		
 	}
 
 	
 	
 	private String loadFile(String filename)
 	{
 		StringBuilder vertexCode = new StringBuilder();
 		String line = null ;
 		try
 		{
 		    BufferedReader reader = new BufferedReader(new FileReader(filename));
 		    while( (line = reader.readLine()) !=null )
 		    {
 		    	vertexCode.append(line);
 		    	vertexCode.append('\n');
 		    }
 		    reader.close();
 		}
 		catch(Exception e)
 		{
 			throw new IllegalArgumentException("unable to load shader from file ["+filename+"]", e);
 		}
 		
 		return vertexCode.toString();
 	}
 
 	private void setShaders(GL2 gl) {
 		//Create shaders
         //OpenGL ES retuns a index id to be stored for future reference.
         int vertShader = gl.glCreateShader(GL2.GL_VERTEX_SHADER);
         int fragShader = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
 
         //Compile the vertexShader String into a program.
         String[] vlines = new String[] { loadFile("vertexshader.glsl") };
         int[] vlengths = new int[] { vlines[0].length() };
         gl.glShaderSource(vertShader, vlines.length, vlines, vlengths, 0);
         gl.glCompileShader(vertShader);
 
         //Check compile status.
         int[] compiled = new int[1];
         gl.glGetShaderiv(vertShader, GL2.GL_COMPILE_STATUS, compiled,0);
         if(compiled[0]!=0){System.out.println("Horray! vertex shader compiled");}
         else {
             int[] logLength = new int[1];
             gl.glGetShaderiv(vertShader, GL2.GL_INFO_LOG_LENGTH, logLength, 0);
 
             byte[] log = new byte[logLength[0]];
             gl.glGetShaderInfoLog(vertShader, logLength[0], (int[])null, 0, log, 0);
 
             System.err.println("Error compiling the vertex shader: " + new String(log));
             System.exit(1);
         }
 
         //Compile the fragmentShader String into a program.
         String[] flines = new String[] { loadFile("fragmentshader.glsl") };
         int[] flengths = new int[] { flines[0].length() };
         gl.glShaderSource(fragShader, flines.length, flines, flengths, 0);
         gl.glCompileShader(fragShader);
 
         //Check compile status.
         gl.glGetShaderiv(fragShader, GL2.GL_COMPILE_STATUS, compiled,0);
         if(compiled[0]!=0){System.out.println("Horray! fragment shader compiled");}
         else {
             int[] logLength = new int[1];
             gl.glGetShaderiv(fragShader, GL2.GL_INFO_LOG_LENGTH, logLength, 0);
 
             byte[] log = new byte[logLength[0]];
             gl.glGetShaderInfoLog(fragShader, logLength[0], (int[])null, 0, log, 0);
 
             System.err.println("Error compiling the fragment shader: " + new String(log));
             System.exit(1);
         }
 
         //Each shaderProgram must have
         //one vertex shader and one fragment shader.
         int shaderProgram = gl.glCreateProgram();
         gl.glAttachShader(shaderProgram, vertShader);
         gl.glAttachShader(shaderProgram, fragShader);
 
         //Associate attribute ids with the attribute names inside
         //the vertex shader.
         //gl.glBindAttribLocation(shaderProgram, 0, "attribute_Position");
         //gl.glBindAttribLocation(shaderProgram, 1, "attribute_Color");
 
         gl.glLinkProgram(shaderProgram);
         gl.glUseProgram(shaderProgram);
         
         
         texturesOn = gl.glGetUniformLocation(shaderProgram, "texturesOn");
         sampler0 = gl.glGetUniformLocation(shaderProgram, "color_texture0");
         sampler1 = gl.glGetUniformLocation(shaderProgram, "color_texture1");
         fogOn = gl.glGetUniformLocation(shaderProgram, "fogOn");
         //gl.glUniform1i(texturesOn,0);
         
         fog = new Fog(fogOn, gl);
         
 	}
 
 	private Boolean loadModels(GL2 gl) {
 		//spotModel = ModelLoaderOBJ.LoadModel("./models/spot.obj",
 		//		"./models/spot.mtl", gl);
 		
 		
 		//lampModel = ModelLoaderOBJ.LoadModel("./models/lamp.obj",
 		//		"./models/lamp.mtl", gl);
 		//if (spotModel == null || lampModel == null) {
 		//	return false;
 		//}
 		return true;
 	}
 
	private void setGlobalLight(GL2 gl) {
 		float f1 = (float) (Math.cos(lapsed * 0.00001f / (5 * 3.14)));
 		float f2 = (float) (Math.sin(lapsed * 0.00001f / (2 * 3.14)));
 
		float s = (f1 < 0 && f2 < 0) ? 1 : ((random.nextInt() % 100 > 90) ? 1
 				: 0);
 
 		float SHINE_ALL_DIRECTIONS = 1;
 		float[] lightPos = { 0, 5, 11f, SHINE_ALL_DIRECTIONS };
 		float[] lightColorAmbient = { 0, 0, 0, 1f };
 		float[] lightColorDiffuse = { 0.9f*s, 0.9f*s, 0.9f*s, 1.0f };
 		float[] lightColorSpecular = { 0.9f*s,0.9f*s,0.9f*s, 1f };
 
 		// Set light parameters.
 		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos, 0);
 		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightColorAmbient, 0);
 		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightColorSpecular, 0);
 		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightColorDiffuse, 0);
 		gl.glLightf(GL2.GL_LIGHT1, GL2.GL_LINEAR_ATTENUATION, 0.05f);
 		gl.glLightf(GL2.GL_LIGHT1, GL2.GL_QUADRATIC_ATTENUATION, 0.03f);
 		gl.glEnable(GL2.GL_LIGHT1);
 
 		float[] lightPos2 = { 0, 5, -11f, 1 };
 		float[] lightColorAmbient2 = { 0.01f, 0.01f, 0.01f, 1f };
 		float[] lightColorDiffuse2 = { 0.9f, 0.9f, 0.9f, 1.0f };
 		float[] lightColorSpecular2 = { 0.9f,0.9f,0.9f, 1f };
 		// Set light parameters.
 		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos2, 0);
 		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightColorAmbient2, 0);
 		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightColorSpecular2, 0);
 		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightColorDiffuse2, 0);
 		gl.glLightf(GL2.GL_LIGHT0, GL2.GL_LINEAR_ATTENUATION, 0.005f);
 		gl.glLightf(GL2.GL_LIGHT0, GL2.GL_QUADRATIC_ATTENUATION, 0.02f);
 		gl.glEnable(GL2.GL_LIGHT0);
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
 			int height) {
 		GL2 gl = drawable.getGL().getGL2();
 
 		gl.glMatrixMode(GL2.GL_PROJECTION);
 		gl.glLoadIdentity();
 		GLU glu = new GLU();
 
 		glu.gluPerspective(100, (double) getWidth() / getHeight(), 0.1, 100);
 		gl.glMatrixMode(GL2.GL_MODELVIEW);
 
 	}
 
 	public static void main(String[] args) {
 		JFrame window = new JFrame();
 		final Scene scene = new Scene();
 		window.getContentPane().add(scene);
 		window.setSize(width, height);
 		window.setVisible(true);
 		scene.BindMouseKeyboard();
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	private void BindMouseKeyboard() {
 		Mouse mouse = new Mouse(width, height, this);
 		addMouseMotionListener(mouse);
 		Keyboard keyboard = new Keyboard(this);
 		addKeyListener(keyboard);
 	}
 
 }
