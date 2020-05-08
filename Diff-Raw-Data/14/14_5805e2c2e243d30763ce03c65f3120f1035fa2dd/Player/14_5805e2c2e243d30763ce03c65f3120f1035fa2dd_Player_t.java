 import javax.media.opengl.GL2;
 import javax.media.opengl.glu.GLU;
 
 /**
  * Holds all information about player, camera position and angle and such
  * @author Robert
  *
  */
 public class Player {
 	private double[] pos;
 	//angle is determined in opengl as a rotation around a vector in degrees
 	//as it will not be necessary to rotate along the z-axis (which would be akin to walking the walls and ceiling), only these two are needed
 	//we store these bad boys as radians however
 	private double xrot; 
 	private double yrot;
	private static final double speed = 0.4f;
 	private static final double mouseSense = 0.001f * Math.PI;
 
 	public Player(){
 		pos = new double[]{0.0, 2.0, -10.0};//TODO
 	}
 	
 	/**
 	 * Moves the player/camera in the given direction. 
 	 * @param direction - int 0->3, 0 for forward, 1 for right and so on clockwise.
 	 * @return Wether it was possible to move so - necessary?
 	 */
 	public boolean move(int direction) {
 		//Why the fuck does OpenGL measure angle in degrees? (javas Math library deals only with radians....)
 		
 		if (direction % 2 == 0) {
 			int forback = 1;
 			if (direction > 0)
 				forback = -1;
 			
 			pos[0] += Math.sin(yrot) * speed * forback;
 			pos[2] += Math.cos(yrot) * speed * forback;
 		}
 		else {
 			int leftright = -1;
 			if (direction > 1)
 				leftright = 1;
 			
 			pos[0] += Math.cos(yrot) * speed * leftright;
 			pos[2] -= Math.sin(yrot) * speed * leftright; //negative because I've switched the axles
 		}
 		return true;
 	}
 	
 	/**
 	 * Sends the info of position and viewing angle to OpenGL engine
 	 * @param gl
 	 */
 	public void render(GL2 gl, GLU glu) {
 /*		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
 		gl.glLoadIdentity();*/
 		
 		
 		glu.gluLookAt(pos[0], pos[1], pos[2], //the camera position 
 				pos[0] + Math.sin(yrot), pos[1] + Math.sin(xrot), pos[2] + Math.cos(yrot), //give angle relative to position
 				0, 1, 0); //we don't tilt our heads
 		/*
 		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
 		gl.glLoadIdentity();*/
 	}
 	
 	/**
 	 * Changes the camera angle
 	 * @param deltaX
 	 * @param deltaY
 	 */
 	public void changeView(int deltaX, int deltaY) {
 		yrot += deltaX * mouseSense;
 		
 		double possibleX = xrot + deltaY * mouseSense;
 		if(Math.abs(possibleX) < Math.PI/2) {
 			xrot += deltaY * mouseSense;
 		}
 	}
 }
