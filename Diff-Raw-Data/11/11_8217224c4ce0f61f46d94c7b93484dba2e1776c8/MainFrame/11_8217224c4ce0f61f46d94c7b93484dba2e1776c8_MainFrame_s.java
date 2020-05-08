 package main;
 
 import static main.MatrixContainer.*;
 import static math.Math.*;
 import static org.lwjgl.opengl.GL11.*;
 
 import java.io.*;
 
 import importers.*;
 import math.matrices.*;
 import openGLCLInterfaces.openGL.shaders.*;
 
 import org.lwjgl.input.*;
 import org.lwjgl.opengl.*;
 
 /**
  * Main class, where update and draw happens.
  */
 public class MainFrame {
 	private ShaderProgram lightShader, basicShader;
 	private Camera camera;
 	private Model model;
 	private Light light;
 	public boolean showBump = false;
 	public boolean wireFrame = false;
 	public boolean toggleBump = false, toggleWire = false;
 
 	/**
 	 * Method to do initializing stuff.
 	 */
 	public void init() {
 		Mouse.setGrabbed(true);
 		Keyboard.enableRepeatEvents(false);
 		initOpenGL();
 		camera = new Camera(Matrix.vec3(), 180, 0);
 		try {
 			model = CMOMLoader.loadModelFromCMOM(new File("resources/models/ship2.cmom"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// light = new Light(Matrix.vec3(50, 100, 0), Matrix.vec3(0.24f, 0.24f,
 		// 0.3f), Matrix.vec3(0.8f, 0.8f,
 		// 1), Matrix.vec3(0.8f, 0.8f, 1f), 100);
		light = new Light(Matrix.vec3(0, 0, 20), Matrix.vec3(0.2f, 0.2f, 0.2f), Matrix.vec3(1f, 1f,
				1), Matrix.vec3(1f, 1f, 1f), 100);
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
 		basicShader = ShaderProgram.createBasicShader("/shaderFiles/basicShader/vert.c",
 				"/shaderFiles/basicShader/frag.c");
 		lightShader = ShaderProgram.createBasicShader("/shaderFiles/lightShader/vert.c",
 				"/shaderFiles/lightShader/frag.c");
 	}
 
 	/**
 	 * Main update logic.
 	 * 
 	 * @param delta
 	 *            The time passed from last update to this.
 	 */
 	public void update(int delta) {
 		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
 			EntryPoint.running = false;
 		}
 		Vector2 mouseMovement = MouseHelper.getMovement();
 		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
 			camera.move(-0.0001f, delta, camera.getViewVector());
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
 			camera.move(-0.0001f, delta,
 					Vector3.getDirectionFromRotation(toRadians(camera.getYRotation() + 90), 0));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
 			camera.move(0.0001f, delta, camera.getViewVector());
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
 			camera.move(0.0001f, delta,
 					Vector3.getDirectionFromRotation(toRadians(camera.getYRotation() + 90), 0));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
 			camera.move(-0.0001f, delta, Vector3.getDirectionFromRotation(
 					toRadians(camera.getYRotation()), toRadians(camera.getPitch() + 90)));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
 			camera.move(0.0001f, delta, Vector3.getDirectionFromRotation(
 					toRadians(camera.getYRotation()), toRadians(camera.getPitch() + 90)));
 		}
 		camera.setRotation(camera.getYRotation() + 0.1f * mouseMovement.getX(), camera.getPitch()
 				+ -0.1f * mouseMovement.getY());
 		if (camera.getPitch() < -90f) {
 			camera.setRotation(camera.getYRotation(), -90f);
 		}
 		if (camera.getPitch() > 90f) {
 			camera.setRotation(camera.getYRotation(), 90f);
 		}
 		if (camera.getYRotation() < -180f) {
 			camera.setRotation(360f + camera.getYRotation(), camera.getPitch());
 		}
 		if (camera.getYRotation() > 180f) {
 			camera.setRotation(camera.getYRotation() - 360f, camera.getPitch());
 		}
 
 		if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
 			light.setPosition((Vector3) light.getPosition().add(Matrix.vec3(1, 0, 0)));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
 			light.setPosition((Vector3) light.getPosition().add(Matrix.vec3(0, 0, 1)));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
 			light.setPosition((Vector3) light.getPosition().add(Matrix.vec3(-1, 0, 0)));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
 			light.setPosition((Vector3) light.getPosition().add(Matrix.vec3(0, 0, -1)));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_U)) {
 			light.setPosition((Vector3) light.getPosition().add(Matrix.vec3(0, 1, 0)));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
 			light.setPosition((Vector3) light.getPosition().add(Matrix.vec3(0, -1, 0)));
 		}
 
 		if (Keyboard.isKeyDown(Keyboard.KEY_B)) {
 			if (!toggleBump) {
 				showBump = !showBump;
 				toggleBump = true;
 			}
 		} else {
 			toggleBump = false;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
 			if (!toggleWire) {
 				wireFrame = !wireFrame;
 				toggleWire = true;
 			}
 		} else {
 			toggleWire = false;
 		}
 	}
 
 	/**
 	 * Draw to the screen
 	 */
 	public void draw() {
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		glViewport(0, 0, Display.getWidth(), Display.getHeight());
 		camera.setup();
 		uploadMatrices();
 		if (wireFrame) {
 			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
 			basicShader.enable();
 			basicShader.getVertexAttribute("color").set(Matrix.vec3(1, 1, 1));
 			model.render();
 			basicShader.disable();
 			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
 		} else {
 			light.bind();
 			lightShader.enable();
 			lightShader.getUniformVariable("show_bump").set(this.showBump);
 			model.render();
 			lightShader.disable();
 		}
 		basicShader.enable();
 		VertexAttribute va = basicShader.getVertexAttribute("color");
 		va.set((Vector3) light.getDiffuse().normalize());
 		Vector3 lPos = light.getPosition();
 		renderBox(lPos.getX() - 1, lPos.getY() - 1, lPos.getZ() - 1, 2, 2, 2);
 		basicShader.disable();
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
