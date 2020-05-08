 package main;
 
 import static main.MatrixContainer.*;
 import static math.Math.*;
 import static org.lwjgl.opengl.GL11.*;
 import importers.*;
 
 import java.io.*;
 
 import main.MatrixContainer.MatrixType;
 import math.matrices.*;
 import openGLCLInterfaces.openGL.buffers.*;
 import openGLCLInterfaces.openGL.shaders.*;
 
 import org.lwjgl.input.*;
 import org.lwjgl.opengl.*;
 
 /**
  * Main class, where update and draw happens.
  */
 public class MainFrame {
 	private ShaderProgram normalShader, basicBumpShader, textureShader, basicShader;
 	private Texture heightTex, normalTex;
 	private float s0, s1;
 	private FrameBuffer fb;
 	private float rot = 0;
 	private Vector3 lightPos;
 	private float contrast = 1;
 
 	/**
 	 * Method to do initializing stuff.
 	 */
 	public void init() {
 		Keyboard.enableRepeatEvents(false);
 		initOpenGL();
 		heightTex = TextureLoader.loadTexture(new File("textures/noise1.png"));
 		s0 = (float) heightTex.getWidth() / (float) Display.getWidth();
 		s1 = (float) heightTex.getHeight() / (float) Display.getHeight();
 		if (heightTex.getWidth() > heightTex.getHeight()) {
 			s1 *= (float) heightTex.getHeight() / (float) heightTex.getWidth();
 		} else {
 			s0 *= (float) heightTex.getWidth() / (float) heightTex.getHeight();
 		}
 		float s2 = 0.5f / s0;
 		s0 *= s2;
 		s1 *= s2;
 		this.normalTex = TextureLoader.createTexture(heightTex.getWidth(), heightTex.getHeight());
 		this.fb = new FrameBuffer(new Texture[] { normalTex });
 	}
 
 	/**
 	 * Initializing OpenGL.
 	 */
 	private void initOpenGL() {
 		// set clear color to black
 		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
 		glEnable(GL_DEPTH_TEST);
 		glDepthFunc(GL_LEQUAL);
 		glEnable(GL_CULL_FACE);
 		glEnable(GL_BLEND);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
 		glEnable(GL_TEXTURE_2D);
 		loadShaders();
 	}
 
 	/**
 	 * Loads shaders from files and compiles them.
 	 */
 	private void loadShaders() {
 		normalShader = ShaderProgram.createShaderFromDir("/shaderFiles/normalShader");
 		basicBumpShader = ShaderProgram.createShaderFromDir("/shaderFiles/basicBumpShader");
 		textureShader = ShaderProgram.createShaderFromDir("/shaderFiles/textureShader");
 		basicShader = ShaderProgram.createShaderFromDir("/shaderFiles/basicShader");
 	}
 
 	/**
 	 * Main update logic.
 	 * 
 	 * @param delta
 	 *            The time passed from last update to this.
 	 */
 	public void update(int delta) {
 		while (Keyboard.next()) {
 			if (Keyboard.getEventKeyState() == true) {
 				int key = Keyboard.getEventKey();
 				switch (key) {
 				case Keyboard.KEY_ESCAPE:
 					EntryPoint.running = false;
 					break;
 				}
 			}
 		}
 		this.rot += 0.001f * delta;
 		this.lightPos = (Vector3) MatrixDB.generateRotationMatrix(this.rot, Matrix.vec3(0, 1, 0))
 				.resize(3, 3).multiply(Matrix.vec3(1, 0.5f, 0));
 		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
 			contrast -= 0.01f;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			contrast += 0.1f;
 		}
 		contrast = Math.max(1, contrast);
 		Display.setTitle(String.valueOf(contrast));
 	}
 
 	/**
 	 * Draw to the screen
 	 */
 	public void draw() {
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		glViewport(0, 0, Display.getWidth(), Display.getHeight());
 		activeMatrix(MatrixType.projectionMatrix);
 		loadIdentity();
 		orthogonal(0, 1, 1, 0, 0, 1);
 		activeMatrix(MatrixType.modelViewMatrix);
 		loadIdentity();
 		uploadMatrices();
 		fb.enable();
 		fb.clearBuffer();
 		renderNormalMap();
 		fb.disable();
 
 		loadIdentity();
 		scale(s0, s1, 1);
 		uploadMatrices();
 		textureShader.enable();
 		normalTex.bind();
 		VertexAttribute vTex = textureShader.getVertexAttribute("vTex");
 		glBegin(GL_QUADS);
 		vTex.set(0, 0);
 		glVertex2f(0, 0);
 		vTex.set(0, 1);
 		glVertex2f(0, 1);
 		vTex.set(1, 1);
 		glVertex2f(1, 1);
 		vTex.set(1, 0);
 		glVertex2f(1, 0);
 		glEnd();
 		textureShader.disable();
 
 		glViewport(Display.getWidth() / 2, 0, Display.getWidth() / 2, Display.getHeight());
 		activeMatrix(MatrixType.projectionMatrix);
 		loadIdentity();
 		perspective(
 				toRadians((float) Display.getHeight() / (float) (Display.getWidth() / 2) * 45f),
 				(float) (Display.getWidth() / 2) / (float) Display.getHeight(), 0.1f, 100000.0f);
 		lookAt(Matrix.vec3(1.8f, 1.8f, 1.8f), Matrix.vec3(0, 0, 0), Matrix.vec3(0, 1, 0));
 		activeMatrix(MatrixType.modelViewMatrix);
 		loadIdentity();
 		uploadMatrices();
 		renderPartBox(-0.5f, -0.5f, -0.5f, 1, 1, 1);
 		float ls = 0.01f;
 		basicShader.enable();
 		renderBox(lightPos.getX() - ls, lightPos.getY() - ls, lightPos.getZ() - ls, 2 * ls, 2 * ls,
 				2 * ls);
 		basicShader.disable();
 	}
 
 	private void renderNormalMap() {
 		normalShader.enable();
 		heightTex.bind();
 		VertexAttribute vTex = normalShader.getVertexAttribute("vTex");
 		UniformVariable contrast = normalShader.getUniformVariable("contrast");
 		contrast.set(this.contrast);
 		glBegin(GL_QUADS);
 		vTex.set(0, 0);
 		glVertex2f(0, 0);
 		vTex.set(0, 1);
 		glVertex2f(0, 1);
 		vTex.set(1, 1);
 		glVertex2f(1, 1);
 		vTex.set(1, 0);
 		glVertex2f(1, 0);
 		glEnd();
 		normalShader.disable();
 	}
 
 	private void renderPartBox(float x, float y, float z, float xSize, float ySize, float zSize) {
 		basicBumpShader.enable();
 		VertexAttribute vTex = basicBumpShader.getVertexAttribute("vTex");
 		VertexAttribute normal = basicBumpShader.getVertexAttribute("normal");
 		UniformVariable lp = basicBumpShader.getUniformVariable("lightPos");
 		lp.set(this.lightPos);
 		glBegin(GL_QUADS);
 		normal.set(0, 0, 1);
 		vTex.set(0, 0);
 		glVertex3f(x + xSize, y, z + zSize);
 		vTex.set(1, 0);
 		glVertex3f(x + xSize, y + ySize, z + zSize);
 		vTex.set(1, 1);
 		glVertex3f(x, y + ySize, z + zSize);
 		vTex.set(0, 1);
 		glVertex3f(x, y, z + zSize);
 
 		normal.set(1, 0, 0);
 		vTex.set(0, 0);
 		glVertex3f(x + xSize, y, z + zSize);
 		vTex.set(1, 0);
 		glVertex3f(x + xSize, y, z);
 		vTex.set(1, 1);
 		glVertex3f(x + xSize, y + ySize, z);
 		vTex.set(0, 1);
 		glVertex3f(x + xSize, y + ySize, z + zSize);
 
 		normal.set(0, 1, 0);
 		vTex.set(0, 0);
 		glVertex3f(x, y + ySize, z);
 		vTex.set(1, 0);
 		glVertex3f(x, y + ySize, z + zSize);
 		vTex.set(1, 1);
 		glVertex3f(x + xSize, y + ySize, z + zSize);
 		vTex.set(0, 1);
 		glVertex3f(x + xSize, y + ySize, z);
 		glEnd();
 		basicBumpShader.disable();
 	}
 
 	private void renderBox(float x, float y, float z, float xSize, float ySize, float zSize) {
 		glBegin(GL_QUADS);
 		glVertex3f(x, y, z);
 		glVertex3f(x, y + ySize, z);
 		glVertex3f(x + xSize, y + ySize, z);
 		glVertex3f(x + xSize, y, z);
 
 		glVertex3f(x + xSize, y, z + zSize);
 		glVertex3f(x + xSize, y + ySize, z + zSize);
 		glVertex3f(x, y + ySize, z + zSize);
 		glVertex3f(x, y, z + zSize);
 
 		glVertex3f(x, y, z);
 		glVertex3f(x, y, z + zSize);
 		glVertex3f(x, y + ySize, z + zSize);
 		glVertex3f(x, y + ySize, z);
 
 		glVertex3f(x + xSize, y, z + zSize);
 		glVertex3f(x + xSize, y, z);
 		glVertex3f(x + xSize, y + ySize, z);
 		glVertex3f(x + xSize, y + ySize, z + zSize);
 
 		glVertex3f(x, y, z);
 		glVertex3f(x + xSize, y, z);
 		glVertex3f(x + xSize, y, z + zSize);
 		glVertex3f(x, y, z + zSize);
 
 		glVertex3f(x, y + ySize, z);
 		glVertex3f(x, y + ySize, z + zSize);
 		glVertex3f(x + xSize, y + ySize, z + zSize);
 		glVertex3f(x + xSize, y + ySize, z);
 		glEnd();
 	}
 }
