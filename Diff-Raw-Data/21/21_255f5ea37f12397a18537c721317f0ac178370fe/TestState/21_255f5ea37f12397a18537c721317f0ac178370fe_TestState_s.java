 package com.servegame.abendstern.tunnelblick.backend;
 
 import javax.media.opengl.*;
 
 public class TestState extends GameState {
   protected GameState updateThis(float et) {
     return this;
   }
 
   protected void drawThis(GL2 gl) {
     gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
     gl.glColor3f(1,0,0);
     gl.glBegin(GL2.GL_TRIANGLES);
    gl.glVertex3f(0,0, -1);
     gl.glColor3f(0,1,0);
    gl.glVertex3f(1,0, -5);
     gl.glColor3f(0,0,1);
    gl.glVertex3f(0.5f, 0.5f, -20);
     gl.glEnd();
   }
 }
