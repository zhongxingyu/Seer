 package catdog.mine;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 
 public abstract class Life {
 	//  ü ۷
 	private World world;
 	protected World getWorld() {
 		return world;
 	}
 	public Inventory inventory;
 	
 	// 
 	public Vector2 position = new Vector2();
 	private Vector2 velocity = new Vector2(0, 0);
 	private Vector2 destPos = new Vector2();
 	
 	private boolean direction = LEFT;
 	public boolean getDirection() {
 		return direction;
 	}
 	private int state = STATE_STAND;
 	public int getState() {
 		return state;
 	}
 	private boolean hasDest = false;
 	
 	// 
 	public static final boolean LEFT = false;
 	public static final boolean RIGHT = true;
 
 	/**
 	 * ȴ ӵ (: /)
 	 */
 	protected int WALK_SPEED;
 	
 	private static final int CLIMB_SPEED = 5;
 	/**
 	 * ߷  (: /)
 	 */
 	private static final int GRAVITY = 40;
 	
 	/**
 	 *  ʱ ӵ (: /)
 	 */
 	private static final int JUMP_SPEED = 11;
 	
 	/**
 	 *  x ӵ (: /)
 	 */
 	private static final int JUMP_XSPEED = 3;
 	
 	/**
 	 *    ó Ÿ (: )
 	 */
 	private static final float ARRIVE_THRES = 0.1f;
 	
 	/**
 	 * 浹
 	 */
 	protected Rectangle hitbox = new Rectangle(-0.3f, -1.8f, 0.6f, 1.8f);
 	
 	public static final int STATE_STAND = 0;
 	public static final int STATE_WALK = 1;
 	public static final int STATE_FALL = 2;
 	public static final int STATE_CLIMB = 3;
 	public static final int STATE_JUMP = 4;
 	
 	/**
 	 * 
 	 * @param world  ü
 	 */
 	public Life(World world) {
 		//playerTex = new Texture(Gdx.files.internal("data/player.png"));
 		spriteBatch = new SpriteBatch();
 		inventory = new Inventory();
 		this.world = world;
 		
 		loadAnimationData();
 		
 		// ִϸ̼ ʱȭ
 		currentAni = new Animation(standAni);
 	}
 	
 	/**
 	 * ̵ õѴ.
 	 * @param newPos  ġ (ġ )
 	 */
 	public void requestMove(Vector2 newPos) {
 		// ġ ġ ĳ ߾ ǵ ɾ.
 		walkTo(new Vector2(newPos.x - hitbox.width / 2f, newPos.y));
 	}
 	
 	/**
 	 * ð  ¸ ƮѴ.
 	 * @param delta  ð
 	 */
 	public void update(float delta) {
 		float fx = position.x + ((velocity.x > 0)? hitbox.width/2 : -(hitbox.width/2)) + ((velocity.x > 0)? 1 : -1);
 		switch (state) {
 		
 		case STATE_STAND:
 			if (!hasStandingBlock())
 				changestate(STATE_FALL);
 			break;
 		case STATE_WALK:
 			if (!hasStandingBlock())
 				changestate(STATE_FALL);
 			else if (arrived())
 				stand();
 			else if(!blockInPath(velocity.x * delta))
 				//   ٸ ̵
 				position.x += velocity.x * delta;
 			else {
 				//   ִٸ
 				//  ̿  ְ,   ٸ 
 				if(world.getBlock(new Vector2(fx, position.y)) != null
 						&& world.getBlock(new Vector2(fx, position.y + 1)) == null
 						&& world.getBlock(new Vector2(fx, position.y+2))== null){
 					 jump();
 				} 
				else if(world.getBlock(new Vector2(fx,position.y+1)) !=null)
 					climb();
 				else	
 					stand();
 			}
 			break;
 		case STATE_FALL:
 			velocity.y -= GRAVITY * delta;
 			position.y += velocity.y * delta;
 			if (hasStandingBlock()) {
 				if (hasDest) {
 					stand();
 					walkTo(destPos);
 				} else
 					stand();
 			}
 			break;
 		case STATE_JUMP:
 			// ӵ 
 			velocity.y -= GRAVITY * delta;
 			// y ̵
 			position.y += velocity.y * delta;
 			//    ִٸ xε ̵
 			if(!blockInPath(velocity.x * delta))
 				position.x += velocity.x * delta;
 			//      ٸ  Ȱ
 			if(velocity.y < 0 && hasStandingBlock()) {
 				velocity.y = 0;
 				position.y = (float) Math.ceil(position.y);
 				//        ̶ 
 				// (  Ҿ  ϱ )
 				if(Math.abs(position.x - destPos.x) < 1f)
 					stand();
 				else
 					walkTo(destPos);
 			}
 			break;
 			
 		case STATE_CLIMB:
 			position.y += velocity.y*delta;
 			if(!blockInPath(velocity.x * delta)){
 				// ö
 				position.x += velocity.x * delta;	
 				}
 			if(world.getBlock(new Vector2(fx, position.y)) == null && world.getBlock(new Vector2(fx, position.y+1)) == null) {
 				velocity.y = 0;
 				walkTo(destPos);	
			}//  
			if(world.getBlock(new Vector2(position.x+0.1f,position.y+2)) != null
					|| world.getBlock(new Vector2(position.x+0.1f,position.y+1)) != null)
			{
				velocity.y = 0;
			}
 		}
 		
 		// ִϸ̼ Ʈ
 		currentAni.update(delta);
 	}
 	
 	/**
 	 * ڸ  .
 	 */
 	public void stand() {
 		changestate(STATE_STAND);
 		hasDest = false;
 		velocity.set(0, 0);
 		position.y = (float) Math.ceil(position.y);
 	}
 	
 	/**
 	 * ش ġ ̵ϰ  ( ǥ)
 	 *  :  ǥ Ʈ  ϴ  ϹǷ ġ  ٸ 
 	 * @param newPos
 	 */
 	protected void walkTo(Vector2 newPos) {
 		hasDest = true;
 		destPos.set(newPos);
 		if (position.x < destPos.x) // ->
 			direction = RIGHT;
 		else // <-
 			direction = LEFT;
 		velocity.x = (direction == LEFT ? -1 : +1) * WALK_SPEED;
 		changestate(STATE_WALK);
 	}
 	
 	/**
 	 *  Ѵ.
 	 */
 	public void jump() {
 		changestate(STATE_JUMP);
 		velocity.y = JUMP_SPEED;
 		velocity.x = (velocity.x > 0)? JUMP_XSPEED : -JUMP_XSPEED;
 	}
 	
 	/**
 	 *  state ٲٰ ׿ ´ ִϸ̼ ֱ
 	 * @param state
 	 */
 	private void changestate(int state) {
 		this.state = state;
 		
 		switch(state) {
 		case STATE_WALK:
 			// ȱ
 			currentAni = new Animation(walkAni);
 			break;
 		case STATE_CLIMB:
 			currentAni = new Animation(climbAni);
 			//°ɷ ٲߵ.
 			break;
 		case STATE_STAND:
 		default:
 			// ⺻ : ֱ
 			currentAni = new Animation(standAni);
 		}
 	}
 	/**
 	 * .
 	 */
 	public void climb() {
 		changestate(STATE_CLIMB);
 		velocity.y = CLIMB_SPEED;
 	}
 	
 	/**
 	 *   ֳ?
 	 * @return  false,  true
 	 */
 	protected boolean hasStandingBlock() {
 		int below = (int)Math.ceil(position.y - 1);
 		return world.getBlock((int) Math.floor(position.x + hitbox.width), below) != null
 				|| world.getBlock((int) Math.floor(position.x), below) != null;
 	}
 	
 	/**
 	 * ÷̾ ϴ ⿡  ִ (浹üũ)
 	 * @param xmovedist x࿡  Ÿ
 	 * @return  true,  false
 	 */
 	protected boolean blockInPath(float xmovedist) {
 		// ÷̾ Ʒʿ ִ  ǥ
 		int blocky_bottom = (int)Math.floor(position.y);
 		// ÷̾ ʿ ִ  ǥ
 		int blocky_top = (int) Math.floor(position.y + hitbox.height);
 		// ÷̾  , ٷ տ ִ 
 		
 		int blockx1 = (int)Math.floor(position.x + xmovedist + hitbox.width);
 		int blockx2 = (int)Math.floor(position.x + xmovedist);
 		
 		// ش ġ   true
 		return world.getBlock(blockx1, blocky_bottom) != null 
 				|| world.getBlock(blockx1, blocky_top) != null
 				|| world.getBlock(blockx2, blocky_bottom) != null
 				|| world.getBlock(blockx2, blocky_top) != null
 				|| world.getBlock((int)(position.x+xmovedist+hitbox.width),(int)position.y+1) != null
 				|| world.getBlock((int)position.x,(int)position.y+1) != null;
 	}
 	/**
 	 * ̵    ߴ Ȯ
 	 * @return  true, ƴϸ false
 	 */
 	protected boolean arrived() {
 		// TODO: yǥ Ȯ
 		return Math.abs(position.x - destPos.x) < ARRIVE_THRES;
 	}
 	
 	public boolean isNear(Vector2 touchPos){
 		if((Math.abs(position.x+ 0.5 - touchPos.x)<1.5) && Math.abs(position.y +1 - touchPos.y)<2)
 			return true;
 		else if(Math.abs(position.x+ 0.5 - touchPos.x)<1.5 && Math.abs(position.y +1 - touchPos.y)<1)
 			return true;
 		else
 			return false;
 	}
 
 	public void dig(Block block, float delta) {
 		block.digged(delta);
 	}
 
 	public void setDirection(boolean direction) {
 		this.direction = direction;
 	}
 
 	// ׸  ڵ
 	private Animation currentAni;
 	private SpriteBatch spriteBatch;
 	
 	protected AnimationData standAni, walkAni, climbAni;
 	
 	protected abstract void loadAnimationData();
 	
 	public void render(Viewport viewport) {
 		Vector2 screenPos = viewport.toScreen(position);
 		spriteBatch.begin();
 		currentAni.render(spriteBatch, screenPos, direction == RIGHT, false);
 		//spriteBatch.draw(playerTex, screenPos.x, screenPos.y, playerTex.getWidth(), playerTex.getHeight(),
 		//		0, 0, playerTex.getWidth(), playerTex.getHeight(), direction == RIGHT, false);
 		spriteBatch.end();
 	}
 	
 	/**
 	 * Ʈ ִ ġ     ִ üũ 
 	 * @param x   xǥ
 	 * @param y   yǥ
 	 * @return    true
 	 */
 	public boolean canPutBlock(int x, int y) {
 		// ÷̾ Ʒʿ ִ  ǥ
 		int blocky_bottom = (int)Math.floor(position.y);
 		// ÷̾ ʿ ִ  ǥ
 		int blocky_top = (int) Math.floor(position.y + hitbox.height);
 		// ÷̾  , ٷ տ ִ 
 		
 		int blockx1 = (int)Math.floor(position.x + hitbox.width);
 		int blockx2 = (int)Math.floor(position.x);
 		
 		return!((x == blockx1 && y == blocky_bottom) ||
 				(x == blockx2 && y == blocky_bottom) ||
 				(x == blockx1 && y == blocky_top) ||
 				(x == blockx2 && y == blocky_top));
 	}
 }
