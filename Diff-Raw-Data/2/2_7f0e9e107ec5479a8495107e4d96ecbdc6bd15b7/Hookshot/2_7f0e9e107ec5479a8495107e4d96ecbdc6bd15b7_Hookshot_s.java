 package items;
 
 import input.Controls;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.joints.Joint;
 import org.jbox2d.dynamics.joints.RopeJointDef;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Point;
 import org.newdawn.slick.geom.Vector2f;
 
 import util.Util;
 import anim.AnimStateMachine;
 import anim.AnimationState;
 import entities.Player;
 
 public class Hookshot extends ItemBase {
 
 	private static final float STARTING_VEL = 50;
 	// spring constant for grappling
 	private static final float K = 30;
 	// additional tolerance for deciding when to complete grapple
 	private static final float EPSILON = 30;
 	// how many millisconds to remain active (i.e. damage enemies) after grapple is completed
 	private static final int ACTIVE_TIME = 250;
 	// range the hook can travel before it will reset
 	private static final float MAX_RANGE = 500;
 	// number of segments the rope/chain is broken into (visually, for the wisps)
 	private static final float CHAIN_LENGTH = 530f;
 	private static final float CHAIN_HEIGHT = 16f;
 	
 	private enum HookState { IN, MOTION, OUT, PULL };
 
 	private HookState state;
 	private int activeTimer;
 	
 	private Hook hook;
 	private Joint tether;
 	
 	private Chain chain;
 	
 	private Point playerStart;
 	
 	public Hookshot(Player player) {
 		super(player);
 		state = HookState.IN;
 	}
 
 	@Override
 	public void render(Graphics graphics) {
 
 		
 		// draw tether
 		switch (state) {
 			case OUT: case PULL: case MOTION:
 				chain.render(graphics);
 
 		}
 		
 		// draw hook
 		switch (state) {
 			case MOTION: case OUT: case PULL:
 				hook.render(graphics);
 				break;
 		}
 	}
 	
 	@Override
 	public void update(GameContainer gc, int delta, AnimStateMachine anim) {
 		Input input = gc.getInput();
 		
 		if (hook != null) hook.update(gc, delta);
 		activeTimer = Math.max(0, activeTimer - delta);
 		
 		boolean startPull = false;
 		
 		if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 			switch (state) {
 				case IN:
 					spawnHook();
 					state = HookState.MOTION;
 					break;
 				case OUT:
 					detachTether();
 					startPull = true;
 					playerStart = owner.getPosition();
 					state = HookState.PULL;
 					break;
 			}
 		}
 		
 		if (input.isMousePressed(Input.MOUSE_RIGHT_BUTTON)) {
 			switch (state) {
 				case OUT: case PULL:
 					removeHook();
 					detachTether();
 					state = HookState.IN;
 					break;
 			}
 		}
 		
 		switch (state) {
 			case MOTION:
 				Vec2 rangeCheck = new Vec2(hook.getX() - owner.getX(), hook.getY() - owner.getY());
 				// check for collision with a wall
 				if (hook.isAttached()) {
 					state = HookState.OUT;
 					attachTether();
 				}
 				if (rangeCheck.length() > MAX_RANGE) {
 					removeHook();
 					state = HookState.IN;
 				}
 				anim.play(AnimationState.HOOKING);
 				break;
 			case PULL:
 				activeTimer = ACTIVE_TIME;
 				
 				Vector2f diff = new Vector2f(hook.getX() - owner.getX(), hook.getY() - owner.getY());
 				boolean xFlip = (hook.getX() - playerStart.getX())*(hook.getX() - owner.getX()) < 0;
 				boolean yFlip = (hook.getY() - playerStart.getY())*(hook.getY() - owner.getY()) < 0;
 				
 				// stop pulling the hook if you are close enough to the hook OR the player stops moving (is blocked)
 				if ((xFlip && yFlip) || (diff.length() < owner.getMaxDim() / 2 + EPSILON) || ((owner.getVelocity() < 1) && !owner.sidesTouching().isEmpty() && !startPull)) {
 					removeHook();
 					state = HookState.IN;
 					break;
 				}
 				
 				Body b1 = owner.getPhysicsBody();
 				Body b2 = hook.getPhysicsBody();
 				Vec2 dist = b2.getPosition().sub(b1.getPosition());
 				dist.normalize();
 				
 				// force-based movement (try K=50)
				b1.applyForceToCenter(dist.mul(K));
 //				b2.applyForceToCenter(dist.mul(-K));
 				
 				// impulse-based movement (try K=1)
 //				b1.applyLinearImpulse(dist.mul(K), b1.getPosition());
 //				b2.applyLinearImpulse(dist.mul(-K), b2.getPosition());
 				
 				// simple movement (try K=5)
 				b1.setLinearVelocity(dist.mul(K));
 				
 				break;
 		}
 		
 		if (chain != null)  chain.update(gc, delta);
 	}
 
 	/**
 	 * Creates a new Hook object and projects it in the direction of the aim
 	 */
 	private void spawnHook() {
 		float x = owner.getPosition().getX();
 		float y = owner.getPosition().getY();
 		Vector2f aim = new Vector2f(Controls.getAimAngle(owner));
 		Vector2f start = new Vector2f(0,0);
 		
 		hook = new Hook(owner);
 		hook.addToWorld(owner.getPhysicsWorld(), x + start.x, y + start.y);
 		hook.getPhysicsBody().setLinearVelocity(Util.Vector2fToVec2(aim.copy().scale(STARTING_VEL)));
 		
 		// drawing
 //		chain.draw(owner.getPosition().getX() - (chain.getWidth()/2) + halfDist.x, owner.getPosition().getY() - (chain.getHeight()/2) + halfDist.y);
 
 		
 		Vec2 dist = Util.PointToVec2(hook.getPosition()).sub(Util.PointToVec2(owner.getPosition()));
 		Vec2 halfDist = dist.mul(.5f);
 		chain = new Chain(owner.getPosition().getX() - CHAIN_LENGTH/2 + halfDist.x, 
 				  owner.getPosition().getY() - CHAIN_HEIGHT/2 + halfDist.y, 
 				  CHAIN_LENGTH,
 				  CHAIN_HEIGHT,
 				  owner,
 				  hook);
 	}
 
 	/**
 	 * Removes the Hook object
 	 */
 	private void removeHook() {
 		if (hook != null){
 			owner.getPhysicsWorld().destroyBody(hook.getPhysicsBody());
 		}
 		hook = null;
 		
 		
 		//drawing
 		chain = null;
 	}
 
 	/**
 	 * Attaches a RopeJoint between the STATIC hook body and the player.
 	 */
 	private void attachTether() {
 		Body b1 = owner.getPhysicsBody();
 		Body b2 = hook.getPhysicsBody();
 		Vec2 zero = new Vec2(0,0);
 		RopeJointDef tetherDef = new RopeJointDef();
 		tetherDef.bodyA = b1;
 		tetherDef.bodyB = b2;
 		tetherDef.localAnchorA.set(zero);
 		tetherDef.localAnchorB.set(zero);
 		tetherDef.maxLength = b1.getPosition().sub(b2.getPosition()).length();
 		tether = owner.getPhysicsWorld().createJoint(tetherDef);
 
 
 	}
 
 	private void detachTether() {
 		if (tether != null) {
 			Joint.destroy(tether);
 			tether = null;
 		}
 
 	}
 
 	@Override
 	public boolean isAttacking() {
 		return activeTimer > 0;
 	}
 
 	@Override
 	public void reset() {
 		detachTether();
 		if (hook != null) {
 			hook.reset();
 		}
 		// removeHook();
 		state = HookState.IN;
 	}
 	
 	public void drawRange(Graphics graphics) {
 		if (state == HookState.IN) {
 			graphics.setColor(Color.lightGray);
 			graphics.drawOval(owner.getX() - MAX_RANGE, owner.getY() - MAX_RANGE, MAX_RANGE*2, MAX_RANGE*2);
 		}
 	}
 	
 	public boolean isPulling() {
 		return state.equals(HookState.PULL);
 	}
 	
 	public boolean isShooting() {
 		return state.equals(HookState.MOTION);
 	}
 	
 }
