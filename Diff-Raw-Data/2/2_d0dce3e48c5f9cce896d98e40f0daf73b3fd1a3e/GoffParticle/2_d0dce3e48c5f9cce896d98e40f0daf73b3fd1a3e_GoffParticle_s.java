 package com.danielmessias.particleplayground;
 
 
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.Color;
 
 public class GoffParticle extends Particle {
 	int velocityX;
 	double velocityY;
 	float dx = 0;
 	int bounces = 0;
 	final int maxBounces = 10;
 
 	public GoffParticle(int xPos, int yPos) {
 		super(xPos, yPos);
 		dx += (Mouse.getDX()/2);
 	}
 	
 	public void update() {
 		
 		super.update();
 		
 		prevx = x;
 		prevy = y;
 		
 		y+=velocityY;
 
 		if(y < ParticleWorld.winHeight && velocityY <= 60) {
 			velocityY++;
 		}
 		
 		if(y >= ParticleWorld.winHeight) {
 			velocityY = -0.8 * velocityY;
			dx = (float) (-0.9*dx);
 			bounces++;
 		}
 		
 		dx = (float) (dx * 0.95);
 		x+=dx;
 		
 		if(x <= 0 || x >= ParticleWorld.winWidth) {
 			dx = (float) (-0.9*dx);
 			velocityY = -0.98 * velocityY;
 		}
 		
 		if(ParticleWorld.winHeight - y >= ParticleWorld.winHeight/2) {
 			setColor(new Color(0,255,0));
 		}
 		
 		if(ParticleWorld.winHeight - y >= ParticleWorld.winHeight/4 && ParticleWorld.winHeight - y < ParticleWorld.winHeight/2) {
 			setColor(new Color(255,0,0));
 		}
 		
 		if(ParticleWorld.winHeight - y >= ParticleWorld.winHeight/8 && ParticleWorld.winHeight - y < ParticleWorld.winHeight/4) {
 			setColor(new Color(0,0,255));
 		}
 		
 		if(bounces > maxBounces) {
 			killParticle();
 		}
 	}
 
 }
