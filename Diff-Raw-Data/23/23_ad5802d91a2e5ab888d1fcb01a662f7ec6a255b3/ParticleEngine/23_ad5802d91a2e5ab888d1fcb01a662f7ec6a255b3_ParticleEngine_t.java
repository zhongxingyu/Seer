 package net.carmgate.morph.ui;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
import java.util.Random;
 
 import net.carmgate.morph.model.Model;
 import net.carmgate.morph.model.common.Vect3D;
 import net.carmgate.morph.model.entities.Renderable;
 import net.carmgate.morph.model.entities.Updatable;
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ParticleEngine implements Renderable, Updatable {
 
 	// TODO Might be interesting to implement several fading types.
 	public static class Particle {
 		private final Vect3D pos;
 		private final Vect3D speed;
 		private final float rot = (float) (Math.random() * 360);
 		private final float halfLife;
 
 		private float luminosity = (float) Math.random() * 0.5f + 0.2f;
 
 		/**
 		 * Create a new particle
 		 * @param pos
 		 * @param speed
 		 * @param halfLife the time it takes for the particle to fade to half it's luminosity
 		 */
 		public Particle(Vect3D pos, Vect3D speed, float halfLife) {
 			this.pos = pos;
 			this.speed = speed;
 			this.halfLife = halfLife;
 		}
 
 		public float getHalfLife() {
 			return halfLife;
 		}
 
 		public float getLuminosity() {
 			return luminosity;
 		}
 
 		public Vect3D getPos() {
 			return pos;
 		}
 
 		public float getRot() {
 			return rot;
 		}
 
 		public Vect3D getSpeed() {
 			return speed;
 		}
 
 		public void setLuminosity(float luminosity) {
 			this.luminosity = luminosity;
 		}
 
 	}
 
 	/** The texture under the morph image. */
 	private static Texture baseTexture;
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(ParticleEngine.class);
 
 	private final List<Particle> particles = new LinkedList<>();
 
 	/** Timestamp of last time the ship's position was calculated. */
 	// TODO We should move this in a class that can handle this behavior for any Updatable
 	private long lastUpdateTS;
 
 	protected float secondsSinceLastUpdate;
 
	private final Random random = new Random();

 	public void addParticle(Vect3D pos, Vect3D speed, float halfLife) {
		speed.rotate((float) random.nextGaussian() * 2);
 		particles.add(new Particle(pos, speed, halfLife));
 	}
 
 	@Override
 	public void initRenderer() {
 		// load texture from PNG file if needed
 		if (baseTexture == null) {
 			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("particle1-32.png").getPath())) {
 				baseTexture = TextureLoader.getTexture("PNG", fileInputStream);
 			} catch (IOException e) {
 				LOGGER.error("Exception raised while loading texture", e);
 			}
 		}
 
 	}
 
 	@Override
 	public void render(int glMode) {
 		for (Particle particle : particles) {
 			// LOGGER.debug("Rendering particle: " + particle);
 			GL11.glTranslatef(particle.getPos().x, particle.getPos().y, 0);
 			GL11.glRotatef(particle.getRot(), 0, 0, 1);
 
 			float scaleAdj1 = 1.1f;
 			float scaleAdj2 = 0.1f;
 			GL11.glScalef(scaleAdj1 / (scaleAdj2 + particle.getLuminosity()), scaleAdj1 / (scaleAdj2 + particle.getLuminosity()), 0);
 
 			GL11.glEnable(GL11.GL_TEXTURE_2D);
 			GL11.glColor4f(1, 1, 1, particle.getLuminosity());
 			baseTexture.bind();
 			GL11.glBegin(GL11.GL_QUADS);
 			GL11.glTexCoord2f(0, 0);
 			GL11.glVertex2f(-baseTexture.getTextureWidth() / 2, -baseTexture.getTextureWidth() / 2);
 			GL11.glTexCoord2f(1, 0);
 			GL11.glVertex2f(baseTexture.getTextureWidth() / 2, -baseTexture.getTextureWidth() / 2);
 			GL11.glTexCoord2f(1, 1);
 			GL11.glVertex2f(baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
 			GL11.glTexCoord2f(0, 1);
 			GL11.glVertex2f(-baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
 			GL11.glEnd();
 
 			GL11.glScalef((scaleAdj2 + particle.getLuminosity()) / scaleAdj1, (scaleAdj2 + particle.getLuminosity()) / scaleAdj1, 0);
 			GL11.glRotatef(-particle.getRot(), 0, 0, 1);
 			GL11.glTranslatef(-particle.getPos().x, -particle.getPos().y, 0);
 		}
 	}
 
 	@Override
 	public void update() {
 		secondsSinceLastUpdate = ((float) Model.getModel().getCurrentTS() - lastUpdateTS) / 1000;
 		lastUpdateTS = Model.getModel().getCurrentTS();
 		if (secondsSinceLastUpdate == 0f) {
 			return;
 		}
 
 		// Update all particles
 		List<Particle> particlesToRemove = new ArrayList<>();
 		for (Particle particle : particles) {
 			Vect3D pos = particle.getPos();
 			Vect3D speed = particle.getSpeed();
 			pos.x += speed.x * secondsSinceLastUpdate;
 			pos.y += speed.y * secondsSinceLastUpdate;
 			pos.z += speed.z * secondsSinceLastUpdate;
 
 			// linear decrease in luminosity
 			particle.setLuminosity(particle.getLuminosity() - secondsSinceLastUpdate / (2 * particle.getHalfLife()));
 			if (particle.getLuminosity() <= 0) {
 				particlesToRemove.add(particle);
 			}
 		}
 
 		// Remove dead particles
 		for (Particle particle : particlesToRemove) {
 			particles.remove(particle);
 		}
 	}
 }
