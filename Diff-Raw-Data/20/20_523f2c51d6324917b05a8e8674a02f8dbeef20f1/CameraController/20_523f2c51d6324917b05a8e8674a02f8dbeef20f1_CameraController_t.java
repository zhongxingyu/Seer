 package com.cg.trashman;
 
 import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
 import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
 
 import java.awt.event.KeyEvent;
 
 import javax.media.opengl.GL2;
 import javax.media.opengl.glu.GLU;
 
 public class CameraController {
 	private GL2 gl;
 	private GLU glu;
 	private float pX;
 	private float pY;
 	private float pZ;
 
 	public CameraController() {
 		pX = 0;
 		pY = 0;
 		pZ = 0;
 	}
 
 	public void setGL(GL2 gl, GLU glu) {
 		this.gl = gl;
 		this.glu = glu;
 	}
 
 	public void moveCamera(float dx, float dy) {
 		pX += dx;
 		pY += dy;
 		// Enable the model-view transform
 		gl.glMatrixMode(GL_PROJECTION); // choose projection matrix
 		gl.glLoadIdentity(); // reset projection matrix
 		glu.gluPerspective(45.0, 1.55f, 0.1, 100.0);
 		// Enable the model-view transform
		// gl.glTranslatef(pX, pY, 0);
		gl.glRotatef(pX, 0f, 20f, 1f);
 		gl.glMatrixMode(GL_MODELVIEW);
 		gl.glLoadIdentity(); // reset
 	}
 
 	public void keyPressed(KeyEvent event) {
 		if (event.getKeyCode() == KeyEvent.VK_A) {
			System.out.println("A: " + pX + "," + pY);
			moveCamera(-10f, 0f);
 		} else if (event.getKeyCode() == KeyEvent.VK_W) {
 			System.out.println("W");
 		} else if (event.getKeyCode() == KeyEvent.VK_S) {
 			System.out.println("S");
 		} else if (event.getKeyCode() == KeyEvent.VK_D) {
 			System.out.println("D");
 		}
 	}
 
 	public void keyReleased(KeyEvent event) {
 	}
 
 	public void keyTyped(KeyEvent event) {
 
 	}
 }
