 package catdog.mine.monster;
 
 import com.badlogic.gdx.math.Vector2;
 
 import catdog.mine.Player;
 import catdog.mine.World;
 
 public class Ghost extends Mob {
 	public static final float maxLife = 1f;
 	
 	/**
 	 * ð  
 	 */
 	public static final float damagePerSecondFactor = 0.1f;
 	/**
 	 * ð ̵ Ÿ
 	 */
 	public static final float speed = 2f;
 	
 	/**
 	 * Ϸ ̴ Ÿ
 	 */
 	public static final float wavingheight = 0.2f;
 	
 	private Vector2 pseudoPos;
 	private float totaltime = 0f;
 
 	public Ghost(World world, Player player) {
 		super(world, player);
 		
 		pseudoPos = position;
 	}
 
 	public void update(float delta) {
 		//⺻ ó ׷   ư
 		Vector2 movevector = new Vector2(getPlayer().position.x - this.pseudoPos.x, 
 												getPlayer().position.y - this.pseudoPos.y).nor();
 		
 		this.pseudoPos.add(movevector.mul(speed * delta));
 		
 		this.position = pseudoPos.cpy();
 		this.position.y += Math.sin(totaltime * Math.PI * 2 / 1.3f) * wavingheight;
 		
 		totaltime += delta;
 		
 		//  
 		setDirection(movevector.x >= 0? RIGHT : LEFT);
 		
 		// ü 
 		if(isAlive())
 			hit(damagePerSecondFactor * delta);
 	}
 }
