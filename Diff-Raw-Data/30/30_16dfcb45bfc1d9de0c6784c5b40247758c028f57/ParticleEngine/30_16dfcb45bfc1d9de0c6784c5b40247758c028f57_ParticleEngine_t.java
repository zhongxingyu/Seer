 package com.climber;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class ParticleEngine {
 	
 	private ArrayList<Particle> m_particles;
 	private boolean mb_hasStarted; //whether or not to perform simulate or draw functions - true is yes false is no
 	private Texture mt_Texture;
 	
 	public ParticleEngine(Texture t) {
 		mb_hasStarted = false;
 		m_particles = new ArrayList<Particle>();
 		mt_Texture = t;
 	}
 	
 	/**
 	 * Starts the particle engine making particles and simulating/drawing them
 	 * @param x The x value of where the particles should all spawn from
 	 * @param y The y value of where the particles should all spawn from
 	 * @param num How many particles to spawn
 	 */
 	public void start(float x, float y, float num) {
 		for ( int i = 0; i < num; i++ ) {
 			Particle p = new Particle(x, y);
 			m_particles.add(p);
 		}
 		
 		mb_hasStarted = true;
 	}
 	
 	public void simulate() {
 		if ( !mb_hasStarted )
 			return;
 		
 		for ( int i = 0; i < m_particles.size(); i++) {
 			Particle p = m_particles.get(i);
 
 			p.m_x += p.m_xSpeed;
 			p.m_y += p.m_ySpeed;
 			p.m_Color.a -= p.m_fadeSpeed;
 			
 			if ( p.m_Color.a <= 0.0f )
 				m_particles.remove(p);
 		}
 	}
 	
 	public void draw(SpriteBatch batch) {
 		if ( !mb_hasStarted )
 			return;
 		
 		for ( Particle p : m_particles) {
 			batch.setColor(p.m_Color);
 			batch.draw(mt_Texture, p.m_x, p.m_y,
 					0.5f, 0.5f);
 		}
 		batch.setColor(Color.WHITE);
 	}
 	
 	public boolean isDone() {
 		return (mb_hasStarted && m_particles.size() == 0);
 	}
 	
 	private float randomFloat(float min, float max) {
 		return (float) (min + (Math.random() * (max - min)));
 	}
 	
 	private class Particle {
 		public Color m_Color;		 		//the color of the particle
 		public float m_x, m_y;				//the position of the particle
 		public float m_xSpeed, m_ySpeed;	//the speed of the particle
 		public float m_fadeSpeed;			//how fast the life goes down and it fades out
 		
 		public Particle(float x, float y) {
 			m_Color = new Color((float)Math.random(), (float)Math.random(), (float)Math.random(), 1);
 			m_x = x;
 			m_y = y;
 			m_xSpeed = randomFloat(-0.05f, 0.05f);
 			m_ySpeed = randomFloat(-0.05f, 0.05f);
 			m_fadeSpeed = 0.01f;
 		}
 	}
 }
