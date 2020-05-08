 package com.github.desmaster.Devio.gfx.userinterface;
 
 import static org.lwjgl.opengl.GL11.*;
 
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
 	}
 
 	public void render() {
 		if (active) {
 			glLoadIdentity();
 			glDisable(GL_TEXTURE_2D);
 			glTranslatef(container.getX(), container.getY(), 0);
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
 			glBegin(GL_QUADS);
 				glVertex2i(container.getX(), container.getY());
 				glVertex2i(container.getX() + container.getWidth(), container.getY());
 				glVertex2i(container.getX() + container.getWidth(), container.getY() + container.getHeight());
 				glVertex2i(container.getX(), container.getY() + container.getHeight());
 			glEnd();
			glDisable(GL_BLEND);
 		}
 	}
 
 	public void open() {
 		active = true;
 	}
 
 	public void close() {
 		active = false;
 	}
 }
