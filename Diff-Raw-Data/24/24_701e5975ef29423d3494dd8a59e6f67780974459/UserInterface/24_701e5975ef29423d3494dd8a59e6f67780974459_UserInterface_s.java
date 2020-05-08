 package com.github.desmaster.Devio.gfx.userinterface;
 
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2i;
 
 import org.lwjgl.util.Rectangle;
 
 public class UserInterface {
 
 	// State
 	protected boolean active = false;
 
 	// Info
 	protected String name = "Undefined";
 
 	// Look & Feel
 	protected Rectangle container;
 
 	public UserInterface(String name) {
 		this.name = name;
 	}
 
 	public void tick() {
		//glColor4f(0.4f, 0.4f, 0.4f, 1);
 	}
 
 	public void render() {
 		if (active) {
 			glLoadIdentity();
 			glTranslatef(container.getX(), container.getY(), 0);
 			glBegin(GL_QUADS);
 				glVertex2i(container.getX(), container.getY());
 				glVertex2i(container.getX() + container.getWidth(), container.getY());
 				glVertex2i(container.getX() + container.getWidth(), container.getY() + container.getHeight());
 				glVertex2i(container.getX(), container.getY() + container.getHeight());
 			glEnd();
			
 		}
 	}
 
 	public void open() {
 		active = true;
 	}
 
 	public void close() {
 		active = false;
 	}
 }
