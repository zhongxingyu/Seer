 package net.mike.bot.entities;
 
 import net.mike.bot.event.Event;
 import net.mike.bot.event.GlobalEventHandler;
 import net.mike.bot.util.RandomUtil;
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.Color;
 import org.lwjgl.util.vector.Vector2f;
 
 public class EntityBot extends Entity {
 	
 	private static final int MAX_SPEED = 7;
 	private static final int MIN_SPEED = 2;
 	private static final float SPEED_MULTIPLIER = 1000F; // = 0.007 max, 0.002 min
 	
 	private static final int FRAMES_BEFORE_FOOD_DECREMENT = 60;
	private static final float FOOD_DECREMENT = 0.05F;
 	
 	public EntityBot() {
 		super();
 		mColor = new Color(RandomUtil.rand.nextInt(256), RandomUtil.rand.nextInt(256), RandomUtil.rand.nextInt(256));
 		mPosition = new Vector2f(RandomUtil.rand.nextFloat(), RandomUtil.rand.nextFloat());
 		
 		// Any neater way to do this?
 		float xVel = (float) ((RandomUtil.rand.nextInt(MAX_SPEED-MIN_SPEED) + MIN_SPEED)/SPEED_MULTIPLIER);
 		float yVel = (float) ((RandomUtil.rand.nextInt(MAX_SPEED-MIN_SPEED) + MIN_SPEED)/SPEED_MULTIPLIER);
 		if (RandomUtil.rand.nextInt(10) % 2 == 0) xVel *= -1;
 		if (RandomUtil.rand.nextInt(10) % 2 == 0) yVel *= -1;
 		
 		mFoodLevel = RandomUtil.rand.nextFloat();
 		mVelocity = new Vector2f(xVel, yVel);
 		mSize = foodToSize(mFoodLevel);
 	}
 	
 	public EntityBot(Color color, Vector2f position, Vector2f velocity, float foodLevel) {
 		super();
 		mColor = color;
 		mPosition = position;
 		mVelocity = velocity;
 		mFoodLevel = foodLevel;
 		mSize = foodToSize(foodLevel);
 	}
 
 	@Override
 	public void update() {
 		// New position
 		Vector2f.add(mPosition, mVelocity, mPosition);
 		// Bounce off walls
 		if (mPosition.x < 0 || mPosition.x > 1) {
 			mVelocity.x *= -1;
 		}
 		if (mPosition.y < 0 || mPosition.y > 1) {
 			mVelocity.y *= -1;
 		}
 		// Too old?
 		if (++mFramesAlive % FRAMES_BEFORE_FOOD_DECREMENT == 0) {
 			mFoodLevel -= FOOD_DECREMENT;
 		}
 		if (mFoodLevel < 0) {
 			mState = State.STARVED;
 		}
 		// If food level high enough, 1 in 120 chance of spawning
 		if (mFoodLevel >= 0.4 && RandomUtil.rand.nextInt(120) == 0) {
 			spawnClone(mColor, mPosition, mVelocity, mFoodLevel);
 		}
 		mSize = foodToSize(mFoodLevel);
 	}
 
 	@Override
 	public void draw() {
 		GL11.glPushMatrix();
 		
 		double angle = 0;
 		if (mVelocity.x == 0) {
 			angle = mVelocity.y < 0 ? 180 : 0;
 		} else if (mVelocity.x > 0) {
 			angle = 90 - Math.toDegrees(Math.atan(mVelocity.y / mVelocity.x));
 		} else {
 			angle = -90 + Math.toDegrees(Math.atan(mVelocity.y / -mVelocity.x));
 		}
 		
 		GL11.glTranslatef(mPosition.x * Display.getWidth(), mPosition.y * Display.getHeight(), 0);
 		GL11.glRotated(angle, 0D, 0D, -1D);
 		GL11.glScaled((float)Display.getWidth(), (float)Display.getHeight(), 0);
 		
 		GL11.glBegin(GL11.GL_TRIANGLES);
 		GL11.glColor3f(mColor.getRed()/256F, mColor.getGreen()/256F, mColor.getBlue()/256F);
 		GL11.glVertex3f(0, mSize, 0);
 		GL11.glVertex3f(mSize, -mSize, 0);
 		GL11.glVertex3f(-mSize, -mSize, 0);
 		GL11.glEnd();
 		
 		GL11.glPopMatrix();
 		
 	}
 	
 	public void consume(Entity food) {
 		if (food.getState() != State.CONSUMED) {
 			food.setState(State.CONSUMED); // Set food to eaten
 			mFoodLevel += food.getFoodLevel(); // Eat food
 			
 			// Update momentum using masses, instead of assuming same mass
 			mVelocity.scale(mSize);
 			food.getVelocity().scale(food.getSize());
 			Vector2f.add(mVelocity, food.getVelocity(), mVelocity);
 			mVelocity.scale((float) (1.0/(mSize + food.getSize())));
 			
 		}
 	}
 	
 	private float foodToSize(float food) {
 		// y = mx + c
 		return (float) (0.01 + food * 0.025);
 	}
 	
 	private void spawnClone(Color color, Vector2f position, Vector2f velocity, float foodLevel) {
		// New bots should be of food level 0.2
		if (foodLevel <= 0.2) return;
		mFoodLevel -= 0.2;
 		
 		/*
 		 * Algorithm:
 		 * Generate offspring vector 
 		 * 	0.5x to 0.8x original vector speed
 		 * 	-90<theta<90 rotation of original vector
 		 * Alter original velocity to conserve momentum
 		 * v1 = (m1u1-m3v2)/(m1-m3)
 		 */
 		
 		// Generate theta: -PI/2 < THETA < PI/2
 		float theta = (float) (Math.PI * (RandomUtil.rand.nextFloat() - 0.5));
 		float x = (float) (velocity.x * Math.cos(theta) - velocity.y * Math.sin(theta));
 		float y = (float) (velocity.x * Math.sin(theta) + velocity.y * Math.cos(theta));
 		
 		Vector2f newVelocity = new Vector2f(x, y);
 		
 		// 0.5x to 0.8x vector allowed
 		float scale = RandomUtil.rand.nextFloat() * 0.3F + 0.5F;
 		newVelocity.scale(scale);
 		
 		Vector2f momentum = new Vector2f(velocity.x, velocity.y);
 		momentum.scale(foodToSize(foodLevel));
 		
		newVelocity.scale(0.2F);
 		Vector2f newMomentum = new Vector2f();
 		Vector2f.sub(momentum, newVelocity, newMomentum);
 		// Undo scale from before 
 		newVelocity.scale(5F);
 		
		newMomentum.scale((float) (1.0/(foodToSize(foodLevel)-0.2F)));
 		mVelocity = newMomentum;
 		
		// Shift the little babby out of the way of big mummy
		Vector2f positionShift = new Vector2f((float)(1.05 * mSize * -newVelocity.x), (float) (1.05 * mSize * -newVelocity.y));
		Vector2f.add(position, positionShift, position);
 		
		EntityBot offspring = new EntityBot(color, position, newVelocity, 0.2F);
 		GlobalEventHandler.fireEvent(Event.ENTITY_BOT_CREATED, offspring);
 	}
 	
 }
