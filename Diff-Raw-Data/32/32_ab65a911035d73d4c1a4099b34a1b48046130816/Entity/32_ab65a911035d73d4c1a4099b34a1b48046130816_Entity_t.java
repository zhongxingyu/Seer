 package mta13438;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Entity {
 
 	private Point pos;
 	private float speed;
 	private float orientation;
 
 	public Entity() {
 		setPos(new Point(0,0,0));
 		setSpeed(1);
 		setOrientation(0);
 	}
 
 	public Entity(Point pos, float speed, float orientation) {
 		setPos(pos);
 		setSpeed(speed);
 		setOrientation(orientation);
 	}
 
 	public Point getPos() {
 		return pos;
 	}
 
 	public void setPos(Point pos) {
 		this.pos = pos;
 	}
 
 	public void setPos(float x, float y, float z) {
 		this.pos.setX(x);
 		this.pos.setY(y);
 		this.pos.setZ(z);
 	}
 
 	public float getSpeed() {
 		return speed;
 	}
 
 	public void setSpeed(float speed) {
 		this.speed = speed;
 	}
 
 	public float getOrientation() {
 		return orientation;
 	}
 
 	public void setOrientation(float orientation) {
 		this.orientation = orientation;
 	}
 
 	public String toString() {
 		return "Entity [speed=" + speed + ", orientation=" + orientation + "]";
 	}
 
 	public void turnLeft(float delta) {
 		setOrientation(getOrientation() + (0.1f * delta));
 		if (getOrientation() > 2*Math.PI) {
 			setOrientation(0);
 		}
 	}
 
 	public void turnRight(float delta) {
 		setOrientation(getOrientation() - (0.1f * delta));
 		if (getOrientation() < 0) {
 			setOrientation((float)(2*Math.PI));
 		}
 	}
 
 	public void foward(float delta) {
 		float x,y,z;
 
		x = (float) ((getSpeed() * Math.cos(getOrientation()+90) * delta) + getPos().getX());
		y = (float) ((getSpeed() * Math.sin(getOrientation()+90) * delta) + getPos().getY());
 		z = 0.0f + getPos().getZ();
 
 		setPos(x, y, z);
 	}
 
 	public void backward(float delta) {
 		float x,y,z;
 		
 		x = (float) (getPos().getX() - (getSpeed() * Math.cos(getOrientation()) * delta));
 		y = (float) (getPos().getY() - (getSpeed() * Math.sin(getOrientation()) * delta));
 		z = 0.0f + getPos().getZ();
 
 		setPos(x, y, z);
 	}
 
 	public void walkSound() {
 		// Something to play the sound
 	}
 
 	public void draw() {
 
 		/**glBegin(GL_TRIANGLES);
 		glColor3f(0f, 0f, 1.0f);
 		glVertex3f(getPos().getX() + 0, getPos().getY() + 2.5f, 0);
         glVertex3f(getPos().getX() + 5, getPos().getY() + 0, 0);
         glVertex3f(getPos().getX() + 0, getPos().getY() - 2.5f, 0);
 		 **//**
 		System.out.println(Math.cos(getOrientation()) * 2);
         glVertex3f((float) (getPos().getX() + Math.cos(getOrientation()) * 0), (float) (getPos().getY() + Math.sin(getOrientation()) * 2), 0);
         glVertex3f((float) (getPos().getX() + Math.cos(getOrientation()) * 2), (float) (getPos().getY() + Math.sin(getOrientation()) * 0), 0);
         glVertex3f((float) (getPos().getX() + Math.cos(getOrientation()) * 0), (float) (getPos().getY() + Math.sin(getOrientation()) * -2), 0);
         glEnd();**/
 
 
 		//If a circle is preferred instead of a point. 
 		glColor3f(1.0f, 0f, 0f);
 		glBegin(GL_LINE_STRIP); float f = 0.0f; for(int i = 0; i<30;i++){
 			glVertex3f(pos.getX()+(float)Math.cos(f),
 					pos.getY()+(float)Math.sin(f), 0); f = (float) (f +(2*Math.PI/30)); }
 		glEnd();
		glColor3f(0f, 1.0f, 0f);
		glBegin(GL_LINES);
		 glVertex2f(pos.getX(),pos.getY());
		 glVertex2f((float) (pos.getX() + Math.cos(getOrientation() + getPos().getX())),(float) (pos.getY() + Math.sin(getOrientation() + getPos().getY())));
		glEnd();
 
 	}
 }
