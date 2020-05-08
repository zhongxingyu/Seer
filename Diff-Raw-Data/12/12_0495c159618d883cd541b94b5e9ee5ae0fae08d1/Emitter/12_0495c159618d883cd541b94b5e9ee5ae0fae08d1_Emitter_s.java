 package com.turbonips.troglodytes;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.particles.Particle;
 import org.newdawn.slick.particles.ParticleEmitter;
 import org.newdawn.slick.particles.ParticleSystem;
 
 public class Emitter implements ParticleEmitter
 {
     private float x = 0;
     private float y = 0;
     private int interval;
     private int timer;
     private float size;
     private int maxParticleLifespan;
     private int maxParticles;
     private float velocityX;
     private float velocityY;
     private float spawnOffsetX;
     private float spawnOffsetY;
     private boolean spawnOffsetRandom;
     private float spawnOffsetPercentNegative;
     private boolean velocityXRandom;
     private boolean velocityYRandom;
     private float velocityPercentNegative;
     private float colorR;
     private float colorG;
     private float colorB;
     private float colorA;
     private float colorChangeR;
     private float colorChangeG;
     private float colorChangeB;
     private float colorChangeA;
     private float velocitySpeed;
     private boolean intervalRandom;
     
     private boolean enabled = false;
     
 	public Emitter(ParticleData particleData)
 	{
 		interval = particleData.getInterval();
 		size = particleData.getSize();
 		maxParticleLifespan = particleData.getMaxParticleLifespan();
 		maxParticles = particleData.getMaxParticles();
 		velocityX = particleData.getVelocityX();
 		velocityY = particleData.getVelocityY();
 		spawnOffsetX = particleData.getSpawnOffsetX();
 		spawnOffsetY = particleData.getSpawnOffsetY();
 		spawnOffsetRandom = particleData.getSpawnOffsetRandom();
 		spawnOffsetPercentNegative = particleData.getSpawnOffsetPercentNegative();
 		velocityXRandom = particleData.getVelocityXRandom();
 		velocityYRandom = particleData.getVelocityYRandom();
 		velocityPercentNegative = particleData.getVelocityPercentNegative();
 		colorR = particleData.getColorR();
 		colorG = particleData.getColorG();
 		colorB = particleData.getColorB();
 		colorA = particleData.getColorA();
 		colorChangeR = particleData.getColorChangeR();
 		colorChangeG = particleData.getColorChangeG();
 		colorChangeB = particleData.getColorChangeB();
 		colorChangeA = particleData.getColorChangeA();
 		velocitySpeed = particleData.getVelocitySpeed();
 		intervalRandom = particleData.getIntervalRandom();
 		
 		this.timer = this.interval;
 	}
 	
 	@Override
 	public boolean completed()
 	{
 		return false;
 	}
 
 	@Override
 	public Image getImage()
 	{
 		return null;
 	}
 
 	@Override
 	public boolean isEnabled()
 	{
 		return enabled;
 	}
 
 	@Override
 	public boolean isOriented()
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void resetState()
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void setEnabled(boolean enabled)
 	{
 		this.enabled = enabled;
 	}
 
 	@Override
 	public void update(ParticleSystem system, int delta)
 	{
 		timer -= delta;		
 		if (timer <= 0)
 		{
 			if (intervalRandom) {
 				float intervalOffset = (float)interval*(float)Math.random();
 				timer = interval + (int)intervalOffset;
 			} else {
 				timer = interval;
 			}
 			for (int i = 0; i < maxParticles; i++)
 			{
 				float vX = 0, vY = 0, oX = 0, oY = 0;
 				
 				if (velocityXRandom) {
 					float randomCheck = (float)Math.random();
 					float ThresholdNeg = 1f - velocityPercentNegative;
 					float ThresholdPos = 1f - velocityPercentNegative/2;
 					if (randomCheck > ThresholdNeg) {
 						if (randomCheck > ThresholdPos) {
 							vX = velocityX*(float)Math.random();
 						} else {
 							vX = -velocityX*(float)Math.random();
 						}
 					}
 				} else {
 					vX = velocityX;
 				}
 				
 				if (velocityYRandom) {
 					float randomCheck = (float)Math.random();
 					float ThresholdNeg = 1f - velocityPercentNegative;
 					float ThresholdPos = 1f - velocityPercentNegative/2;
 					if (randomCheck > ThresholdNeg) {
 						if (randomCheck > ThresholdPos) {
 							vY = velocityY*(float)Math.random();
 						} else {
 							vY = -velocityY*(float)Math.random();
 						}
 					}
 				} else {
 					vY = velocityY;
 				}
 				
 				if (spawnOffsetRandom) {
 					float randomCheck = (float)Math.random();
 					float ThresholdNegOffset = 1f - spawnOffsetPercentNegative;
 					float ThresholdPosOffset = 1f - spawnOffsetPercentNegative/2;
 					if (randomCheck > ThresholdNegOffset) {
 						if (randomCheck > ThresholdPosOffset) {
							oX = 5*(float)Math.random();
 						} else {
							oX = -5*(float)Math.random();
 						}
 					}
 					randomCheck = (float)Math.random();
 					if (randomCheck > ThresholdNegOffset) {
 						if (randomCheck > ThresholdPosOffset) {
 							oY = spawnOffsetY*(float)Math.random();
 						} else {
 							oY = -spawnOffsetY*(float)Math.random();
 						}
 					}
 				} else {
 					oX = spawnOffsetX;
 					oY = spawnOffsetY;
 				}
 				
 				Particle p = system.getNewParticle(this, maxParticleLifespan);
                 p.setColor(colorR, colorG, colorB, colorA);
                 p.setPosition(x + oX, y + oY);
                 p.setSize(size);
                 p.setVelocity(vX, vY, velocitySpeed);
 			}
 		}
 	}
 
 	@Override
 	public void updateParticle(Particle particle, int delta)
 	{
         particle.adjustColor(colorChangeR, colorChangeG, colorChangeB, colorChangeA); 
 	}
 
 	@Override
 	public boolean useAdditive()
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean usePoints(ParticleSystem system)
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void wrapUp()
 	{
 		// TODO Is this stub needed (do we have emitters on the map that die off ever)?
 	}
 
     public void moveEmitter(float newX, float newY) {
         this.x = newX;
         this.y = newY;
     }
 }
