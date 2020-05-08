 package beatbots.simulation;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 
 public strictfp class BeatBotColored extends BeatBot {
 	
 	private BeatTokenManager beatTokenManager;
 	
 	private Beat beat;
 
 	public BeatBotColored(BeatMachine beatMachine, BulletManager bulletManager, Vector2f startPosition, BeatTokenManager beatTokenManager, Beat beat) {
 		
 		super(beatMachine, bulletManager, startPosition);
 		
 		this.beat = beat;
 		
 		this.beatTokenManager = beatTokenManager;
 	}
 	
 	@Override
 	public strictfp void init(GameContainer gameContainer) throws SlickException {
 		
 		super.init(gameContainer);
 		
 		//switch (this.beat) {
 			
 		//case Red:
 			
			this.animation = new Animation(new Image[] { new Image("assets/BeatBot2Frame1.png"), new Image("assets/BeatBot2Frame2.png") }, 200, true);
 			
 			//break;
 			
 			
 		//}
 		
 		this.color = Utils.getBeatColor(this.beat);
 	}
 	
 	@Override
 	protected strictfp void onDestroy() {
 		
 		super.onDestroy();
 		
 		this.beatTokenManager.drop(this.getPosition(), this.beat);
 	}
 }
