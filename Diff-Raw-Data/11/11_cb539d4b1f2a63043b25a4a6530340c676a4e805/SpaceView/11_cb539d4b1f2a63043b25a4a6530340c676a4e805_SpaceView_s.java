 package net.spacesim.client.views;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.util.Random;
 
 import net.spacesim.bodies.Earth;
 import net.spacesim.client.Body;
 import net.spacesim.client.Space;
 import net.spacesim.client.SpaceSim;
 import net.spacesim.client.View;
 import net.spacesim.util.Font;
 import net.spacesim.util.Vector3;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 
 import static net.spacesim.util.DrawUtils.*;
 
 public class SpaceView extends View {
 
 	public final static Random random = new Random();
 
 	private Space space = new Space();
 
 	float rtri;
 
 	private boolean debug = true;
 
 	private double cameraSpeed = 1;
 
 	public SpaceView() {
 		for(int i = 0; i < 100; i++) {
 			Body body = new Earth(space, i);
 			body.pos = new Vector3(random.nextFloat() * 10000 - 5000, random.nextFloat() * 10000 - 5000, random.nextFloat() * 1000 - 500);
 			body.vel = new Vector3(random.nextFloat() * 0.01f - 0.005f, random.nextFloat() * 0.01f - 0.005f, random.nextFloat() * 0.01f - 0.005f);
 			space.add(body);
 		}
 	}
 
 	@Override
 	public void render() {
 		glTranslatef(0.0f, 0.0f, -60.0f);
 
 		glPushMatrix();
 		{
 			glRotatef(rtri,0.0f,1.0f,0.0f);
 			glBegin(GL_TRIANGLES);                    // Drawing Using Triangles
 			{
 				glScalef(100, 100, 100);
 				glColor3f(1.0f,0.0f,0.0f);             // Red
 				glVertex3f( 0.0f, 1.0f, 0.0f);         // Top Of Triangle (Front)
 				glColor3f(0.0f,1.0f,0.0f);             // Green
 				glVertex3f(-1.0f,-1.0f, 1.0f);         // Left Of Triangle (Front)
 				glColor3f(0.0f,0.0f,1.0f);             // Blue
 				glVertex3f( 1.0f,-1.0f, 1.0f);         // Right Of Triangle (Front)
 				glColor3f(1.0f,0.0f,0.0f);             // Red
 				glVertex3f( 0.0f, 1.0f, 0.0f);         // Top Of Triangle (Right)
 				glColor3f(0.0f,0.0f,1.0f);             // Blue
 				glVertex3f( 1.0f,-1.0f, 1.0f);         // Left Of Triangle (Right)
 				glColor3f(0.0f,1.0f,0.0f);             // Green
 				glVertex3f( 1.0f,-1.0f, -1.0f);            // Right Of Triangle (Right)
 				glColor3f(1.0f,0.0f,0.0f);             // Red
 				glVertex3f( 0.0f, 1.0f, 0.0f);         // Top Of Triangle (Back)
 				glColor3f(0.0f,1.0f,0.0f);             // Green
 				glVertex3f( 1.0f,-1.0f, -1.0f);            // Left Of Triangle (Back)
 				glColor3f(0.0f,0.0f,1.0f);             // Blue
 				glVertex3f(-1.0f,-1.0f, -1.0f);            // Right Of Triangle (Back)
 				glColor3f(1.0f,0.0f,0.0f);             // Red
 				glVertex3f( 0.0f, 1.0f, 0.0f);         // Top Of Triangle (Left)
 				glColor3f(0.0f,0.0f,1.0f);             // Blue
 				glVertex3f(-1.0f,-1.0f,-1.0f);         // Left Of Triangle (Left)
 				glColor3f(0.0f,1.0f,0.0f);             // Green
 				glVertex3f(-1.0f,-1.0f, 1.0f);         // Right Of Triangle (Left)
 			}
 			glEnd();
 		}
 
 		glPopMatrix();
 		space.render();
 
 		int width = (int) SpaceSim.instance.getDisplaySize().x;
 		int height = (int) SpaceSim.instance.getDisplaySize().y;
 		Font font = getFont("Calibri Regular");
 
 		if(debug) {
 			glPushMatrix();
 			{
 				enableOrthoViewport(width, height);
 				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
 				font.drawStringWithShadow("FPS: " + SpaceSim.instance.getFPS(), width - 250, height - 95, 0xffffffff);
 				font.drawStringWithShadow("TPS: " + SpaceSim.instance.getTPS(), width - 250, height - 80, 0xffffffff);
 				font.drawStringWithShadow("Number of Bodies: " + space.bodyCount, width - 250, height - 65, 0xffffffff);
 				font.drawStringWithShadow("Camera Speed: " + cameraSpeed, width - 250, height - 50, 0xffffffff);
 				font.drawStringWithShadow("Camera Pos: " + SpaceSim.instance.cameraPosition.clone().round(), width - 250, height - 35, 0xffffffff);
 				font.drawStringWithShadow("Camera Orientation: " + SpaceSim.instance.cameraOrientation.clone().round(), width - 250, height - 20, 0xffffffff);
 				glDisable(GL_TEXTURE_2D);
 			}
 			glPopMatrix();
 		}
 
 		/*if(Mouse.isButtonDown(0)) {
 			SpaceSim.instance.cameraOrientation.y -= (Mouse.getX() - lastMouseX) * 0.1;
 			SpaceSim.instance.cameraOrientation.x -= (lastMouseY - Mouse.getY()) * 0.1;
 		}*/
 
 		SpaceSim.instance.cameraOrientation.y -= Mouse.getDX() * 0.1;
 		SpaceSim.instance.cameraOrientation.x += Mouse.getDY() * 0.1;
 
 		if(Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_Z)) {
 			SpaceSim.instance.cameraPosition.sub((SpaceSim.instance.cameraOrientation.clone().toVel()).mul(cameraSpeed));
 		}
 
 		if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
 			SpaceSim.instance.cameraPosition.add((SpaceSim.instance.cameraOrientation.clone().toVel()).mul(cameraSpeed));
 		}
 
 		if(Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_Q)) {
 			SpaceSim.instance.cameraPosition.sub((SpaceSim.instance.cameraOrientation.clone().toVel().right()).mul(cameraSpeed));
 		}
 
 		if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
 			SpaceSim.instance.cameraPosition.add((SpaceSim.instance.cameraOrientation.clone().toVel().right()).mul(cameraSpeed));
 		}
 
 		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			SpaceSim.instance.cameraPosition.add((SpaceSim.instance.cameraOrientation.clone().toVel().up()).mul(cameraSpeed));
 		}
 
 		if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			SpaceSim.instance.cameraPosition.sub((SpaceSim.instance.cameraOrientation.clone().toVel().up()).mul(cameraSpeed));
 		}
 	}
 
 	@Override
 	public void tick() {
 		space.update();
 		rtri+=0.2f;
 	}
 
 	@Override
 	public void onMouseDown(int button) { }
 
 	@Override
 	public void onMouseUp(int button) { }
 
 	@Override
 	public void onMouseScroll(int scrollAmount) {
 		//SpaceSim.instance.cameraPosition.z += (double)scrollAmount * 0.10;
 		if(scrollAmount < 0) {
 			scrollAmount = -scrollAmount;
 			for(int i = 0; i < scrollAmount / 10; i++) {
 				SpaceSim.instance.cameraPosition.add(SpaceSim.instance.cameraOrientation.clone().toVel().div(10));
 			}
 		} else {
 			for(int i = 0; i < scrollAmount / 10; i++) {
 				SpaceSim.instance.cameraPosition.sub(SpaceSim.instance.cameraOrientation.clone().toVel().div(10));
 			}
 		}
 	}
 
 	@Override
 	public void onMouseMove() { }
 
 	@Override
 	public void onKeyDown(char c, int key) {
 		if(key == Keyboard.KEY_Q) {
 			cameraSpeed /= 1.5;
 		} else if(key == Keyboard.KEY_E) {
 			cameraSpeed *= 1.5;
 		} else if(Keyboard.isKeyDown(Keyboard.KEY_HOME)) {
 			SpaceSim.instance.cameraPosition = new Vector3();
 		} else if(Keyboard.isKeyDown(Keyboard.KEY_SLASH)) {
 			debug = !debug;
 		}
 	}
 
 	@Override
 	public void onKeyUp(char c, int key) { }
 
 }
