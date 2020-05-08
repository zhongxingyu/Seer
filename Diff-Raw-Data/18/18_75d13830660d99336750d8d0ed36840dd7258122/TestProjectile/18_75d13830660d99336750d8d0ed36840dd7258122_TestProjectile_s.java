 package game;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import javax.media.opengl.GL3bc;
 
 import modelManager.TextureLoader;
 import objectManager.GameObject;
 import objectManager.ObjectType;
 import physicsManager.PhysicalObject;
 import sceneManager.SceneNode;
 import utilityManager.MathBox;
 import aiManager.Agent;
 import brickManager.Brick;
 import collisionManager.Collidable;
 
 public class TestProjectile extends Agent {
 	private double life = 1;
 	private double lifeDecrement;
 
 	private boolean exploding = false;
	private int explosionTimer = 100;
 	
 	
 	public TestProjectile(GameObject source, Hashtable<String, Double> values, double lifeDecrement) {
 		super(ObjectType.PROJECTILE, source, values, null, null);
 		this.lifeDecrement = lifeDecrement;
 	}
 
 	@Override
 	public void updateView() {
 		if (sceneNodes.get("root") == null && !exploding) {
 			SceneNode root = new SceneNode(null) {
 				@Override
 				public void update(GL3bc gl) {
 					if (!TextureLoader.getCurrentTextureName().equals("ProjectileTexture1")) {
 						TextureLoader.loadTexture(gl, "ProjectileTexture1", "ProjectileTexture1.png");
 						TextureLoader.setCurrentTexture(gl, "ProjectileTexture1");
 					}
 
 					gl.glEnable(GL3bc.GL_BLEND);
 					gl.glEnable(GL3bc.GL_TEXTURE_2D);
 					gl.glDisable(GL3bc.GL_LIGHTING);
 					
 					gl.glPushMatrix();
 					
 					// translate and rotate
 					gl.glTranslatef((float) position.getX(), (float) position.getY(), 0);
 					gl.glRotatef((float) (orientation * 180 / Math.PI), 0, 0, 1);
 					
 					gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
 					gl.glBegin(GL3bc.GL_QUADS);
 						gl.glTexCoord3f(1.0f, 1.0f, 0.0f);
 						gl.glVertex3f(10.0f, 10.0f, 0.0f);
 						
 						gl.glTexCoord3f(1.0f, 0.0f, 0.0f);
 						gl.glVertex3f(10.0f, -10.0f, 0.0f);
 						
 						gl.glTexCoord3f(0.0f, 0.0f, 0.0f);
 						gl.glVertex3f(-10.0f, -10.0f, 0.0f);
 						
 						gl.glTexCoord3f(0.0f, 1.0f, 0.0f);
 						gl.glVertex3f(-10.0f, 10.0f, 0.0f);
 					gl.glEnd();
 					
 					gl.glPopMatrix();
 					
 					gl.glDisable(GL3bc.GL_BLEND);
 					gl.glDisable(GL3bc.GL_TEXTURE_2D);
 					gl.glEnable(GL3bc.GL_LIGHTING);
 				}
 			};
 			sceneNodes.put("root", root);
 		}
 		else if (exploding) {
 			if (sceneNodes.get("explosionNode") == null) {
 				SceneNode explosionNode = new SceneNode(null) {
 					private int numParticles = 100;
 					private float[][] particles;
 					
 					@Override
 					public void update(GL3bc gl) {
 						if (!TextureLoader.getCurrentTextureName().equals("ProjectileTexture1")) {
 							TextureLoader.loadTexture(gl, "ProjectileTexture1", "ProjectileTexture1.png");
 							TextureLoader.setCurrentTexture(gl, "ProjectileTexture1");
 						}
 						
 						if (particles == null) {
 							particles = new float[numParticles][5];
 							
 							//construct initial particles
 							for (int i = 0; i < numParticles; i++) {
 								particles[i] = new float[] {
 									(float) position.getX(), (float) position.getY(),
 									MathBox.nextFloat() * 0.5f * MathBox.nextSign(), MathBox.nextFloat() * 0.5f * MathBox.nextSign(),
 									MathBox.nextFloat()
 								};
 							}
 						}
 						else {
 							//adjust positions of particles
 							for (int i = 0; i < numParticles; i++) {
 								particles[i][0] += particles[i][2];
 								particles[i][1] += particles[i][3];
 							}
 						}
 						
 						gl.glEnable(GL3bc.GL_BLEND);
 						gl.glEnable(GL3bc.GL_TEXTURE_2D);
 						gl.glDisable(GL3bc.GL_LIGHTING);
 						
 						for (int i = 0; i < numParticles; i++) {
 							//render particles
 							gl.glPushMatrix();
 							
 							// translate and rotate
 							gl.glTranslatef(particles[i][0], particles[i][1], 0);
 							
 							gl.glColor4f(1.0f, 1.0f, 1.0f, particles[i][4]);
 							gl.glBegin(GL3bc.GL_QUADS);
 								gl.glTexCoord3f(1.0f, 1.0f, 0.0f);
 								gl.glVertex3f(2.5f, 2.5f, 0.0f);
 								
 								gl.glTexCoord3f(1.0f, 0.0f, 0.0f);
 								gl.glVertex3f(2.5f, -2.5f, 0.0f);
 								
 								gl.glTexCoord3f(0.0f, 0.0f, 0.0f);
 								gl.glVertex3f(-2.5f, -2.5f, 0.0f);
 								
 								gl.glTexCoord3f(0.0f, 1.0f, 0.0f);
 								gl.glVertex3f(-2.5f, 2.5f, 0.0f);
 							gl.glEnd();
 							
 							gl.glPopMatrix();
 						}
 						
 						gl.glDisable(GL3bc.GL_BLEND);
 						gl.glDisable(GL3bc.GL_TEXTURE_2D);
 						gl.glEnable(GL3bc.GL_LIGHTING);
 					}
 				};
 				sceneNodes.put("explosionNode", explosionNode);
 				sceneNodes.remove("root");
 			}
 		}
 	}
 
 	@Override
 	public void collide(Collidable collider) {
 		life = -1;
 	}
 
 	@Override
 	public double getRadius() {
 		return 5;
 	}
 
 	@Override
 	public void update() {
 		life -= lifeDecrement;
 
 		if (life <= 0) {
 			exploding = true;
 			explosionTimer -= 1;
 			
 			if (explosionTimer <= 0) {
 				setAlive(false);
 			}
 		}
 	}
 
 	@Override
 	protected PhysicalObject createFragment(ArrayList<Brick> bricks, ArrayList<ArrayList<Integer>> adjacencyList) {
 		return null;
 	}
 
 	@Override
 	public ArrayList<double[]> getLines() {
 		ArrayList<double[]> lines = new ArrayList<double[]>();
 		
 		double x = position.getX();
 		double y = position.getY();
 		
 		//top
 		double[] l1 = new double[] {
 			x - 5, y + 5,
 			x + 5, y + 5,
 		};
 		
 		//right
 		double[] l2 = new double[] {
 			x + 5, y + 5,
 			x + 5, y - 5
 		};
 		
 		//bottom
 		double[] l3 = new double[] {
 			x + 5, y - 5,
 			x - 5, y - 5
 		};
 		
 		//left
 		double[] l4 = new double[] {
 			x - 5, y - 5,
 			x - 5, y + 5
 		};
 		
 		lines.add(l1);
 		lines.add(l2);
 		lines.add(l3);
 		lines.add(l4);
 		
 		return lines;
 //		ArrayList<double[]> positionLine = new ArrayList<double[]>();
 //		positionLine.add(new double[] {oldPosition.getX(), oldPosition.getY(), position.getX(), position.getY()});
 //		return positionLine;
 	}
 
 	@Override
 	public boolean canCollide() {
 		if (life > 0) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	
 }
