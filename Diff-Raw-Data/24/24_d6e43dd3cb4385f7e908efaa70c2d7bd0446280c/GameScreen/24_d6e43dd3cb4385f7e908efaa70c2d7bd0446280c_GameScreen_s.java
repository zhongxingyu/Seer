 package Screens;
 
 import Application.Assets;
 import Application.MatchManager;
 import Application.MatchManager.Direction;
 import GameLogic.Map.TypeSquare;
 import GameLogic.Map.FissuresTypes;
 import GameLogic.Map.WaterTypes;
 import GameLogic.Map.SunkenTypes;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.math.collision.BoundingBox;
 
 
 public class GameScreen implements Screen{
 	private Game game;
 	private OrthographicCamera guiCam;
 	private OrthographicCamera textCam;
 	private SpriteBatch batcher;
 	private Vector3 touchPoint;
 	private Vector3 touchPoint2;
 	private BoundingBox fdBounds;
 	private BoundingBox fiBounds;
 	private BoundingBox farBounds;
 	private BoundingBox fabBounds;
 	private BoundingBox harpoonBounds;
 	private PenguinAnimation[] penguinAnimations;
     private float stateTime;
 	private TextureRegion currentFrame;
 	private MatchManager manager;
 	private String name;
 	private BitmapFont font;
 	private int numPlayer;
 
 	
 	public GameScreen(Game game, MatchManager manager){
 		this.game=game;
 		this.name = manager.getMyNamePlayer();
 		numPlayer=manager.getMyIdPlayer();
 		this.manager = manager;
 		font = new BitmapFont();
 		guiCam = new OrthographicCamera(21,13);
 		textCam=new OrthographicCamera(21*32,13*32);
 		textCam.position.set((21*32)/2,(13*32)/2,0);
 		guiCam.position.set(21f/2,13f/2,0);
 		batcher = new SpriteBatch();
 		touchPoint = new Vector3();
 		touchPoint2 = new Vector3();
 		fdBounds= new BoundingBox(new Vector3(4.75f,2.5f,0), new Vector3(7.25f,4.5f,0));
 		fiBounds= new BoundingBox(new Vector3(0.25f,2.5f,0), new Vector3(2.75f,4.5f,0));
 		farBounds= new BoundingBox(new Vector3(2.75f,4.6f,0), new Vector3(4.75f,7,0));
 		fabBounds= new BoundingBox(new Vector3(2.75f,0,0), new Vector3(4.75f,2.4f,0));
 
 		harpoonBounds= new BoundingBox(new Vector3(19,0,0), new Vector3(21,4,0));
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 		//--------------------------------------------------------------------
 		penguinAnimations = new PenguinAnimation[4];
 		penguinAnimations[0] = new PenguinAnimation(Gdx.files.internal("data/pClarosRojo.png"),manager.getLookAt(0));
 		penguinAnimations[1] = new PenguinAnimation(Gdx.files.internal("data/pClarosVerde.png"),manager.getLookAt(1));
 		penguinAnimations[2] = new PenguinAnimation(Gdx.files.internal("data/pClarosAmarillo.png"),manager.getLookAt(2));
 		penguinAnimations[3] = new PenguinAnimation(Gdx.files.internal("data/pClarosAzul.png"),manager.getLookAt(3));
         stateTime = 0f;     
 	}
 	
 	@Override
 	public void dispose() {
 		batcher.dispose();
 	}
 
 	@Override
 	public void hide() {
 		
 	}
 
 	@Override
 	public void pause() {
 		
 	}
 
 	@Override
 	public void render(float delta) {
 		Vector3 position;
 		Gdx.gl.glClearColor(1,1,1,1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 		stateTime += Gdx.graphics.getDeltaTime();
 		batcher.enableBlending();
 		batcher.begin();
 		batcher.setProjectionMatrix(guiCam.combined);
 		batcher.draw(Assets.getBackground(),0,0,21,13);
 		
 		batcher.draw(Assets.getHorizontalBarDown(),8,0,11,1);
 		batcher.draw(Assets.getHorizontalBarUP(),8,12,11,1);
 		batcher.draw(Assets.getVerticalBarLeft(),7,0,1,13);
 		batcher.draw(Assets.getVerticalBarRigth(),19,0,1,13);
 		
 		paintPlayerStatus();
 		
 		for(int i=0;i<11;i++){
 			for (int j=0;j<11;j++){
 				TextureRegion texture= null;
 				TypeSquare typeBasicMatrix = manager.getBasicMatrixSquare(i,j);
 				FissuresTypes typeFissureMatrix = manager.getFissureMatrixSquare(i,j);
 				WaterTypes typeWaterMatrix = manager.getWaterMatrixSquare(i,j);
 				SunkenTypes typeSunkenMatrix = manager.getSunkenMatrixSquare(i,j);
 				texture = Assets.getBox(); 
 				batcher.draw(texture,i+8,j+1,1,1);
 				//First we draw fissures matrix
 				if(!typeFissureMatrix.equals(FissuresTypes.empty)){
 					if(typeFissureMatrix.equals(FissuresTypes.barrelWithFissure)) texture  = Assets.getBarrelWithFissure();
 					else if (typeFissureMatrix.equals(FissuresTypes.crossingFissures)) texture  = Assets.getFissureCrossing();
 					else if (typeFissureMatrix.equals(FissuresTypes.fissureC))	texture  = Assets.getFissureCenter();
 					else if (typeFissureMatrix.equals(FissuresTypes.fissureSX)) texture  = Assets.getFissureSideX();
 					else if (typeFissureMatrix.equals(FissuresTypes.fissureSY)) texture  = Assets.getFissureSideY();
 					
 					batcher.draw(texture,i+8,j+1,1,1);
 				}
 				if (!typeBasicMatrix.equals(TypeSquare.empty)){
 					if (typeBasicMatrix.equals(TypeSquare.unbreakable)) texture = Assets.getIgloo();
 					else if (typeBasicMatrix.equals(TypeSquare.Harpoon)) texture = Assets.getHarpoon();				
 					else if (typeBasicMatrix.equals(TypeSquare.breakable)) texture  = Assets.getBarrel();
 					batcher.draw(texture,i+8,j+1,1,1);
 				}
 				if(!typeWaterMatrix.equals(WaterTypes.empty)){
 					if (typeWaterMatrix.equals(WaterTypes.water1SOpN)) texture  = Assets.getWater1SideOpenN();
 					else if (typeWaterMatrix.equals(WaterTypes.water1SOpS)) texture  = Assets.getWater1SideOpenS();
 					else if (typeWaterMatrix.equals(WaterTypes.water1SOpE)) texture  = Assets.getWater1SideOpenE();
 					else if (typeWaterMatrix.equals(WaterTypes.water1SOpW)) texture  = Assets.getWater1SideOpenW();
 					else if (typeWaterMatrix.equals(WaterTypes.water2SOpCornerNW)) texture  = Assets.getWater2SideOpenCornerNW();
 					else if (typeWaterMatrix.equals(WaterTypes.water2SOpCornerSE)) texture  = Assets.getWater2SideOpenCornerSE();
 					else if (typeWaterMatrix.equals(WaterTypes.water2SOpCornerWS)) texture  = Assets.getWater2SideOpenCornerWS();
 					else if (typeWaterMatrix.equals(WaterTypes.water2SOpCornerEN)) texture  = Assets.getWater2SideOpenCornerEN();
 					else if (typeWaterMatrix.equals(WaterTypes.water2SOpBridgeX)) texture  = Assets.getWater2SideOpenBridgeX();
 					else if (typeWaterMatrix.equals(WaterTypes.water2SOpBridgeY)) texture  = Assets.getWater2SideOpenBridgeY();
 					else if (typeWaterMatrix.equals(WaterTypes.water3SOpN)) texture  = Assets.getWater3SideNOpen();
 					else if (typeWaterMatrix.equals(WaterTypes.water3SOpS)) texture  = Assets.getWater3SideSOpen();
 					else if (typeWaterMatrix.equals(WaterTypes.water3SOpE)) texture  = Assets.getWater3SideEOpen();
 					else if (typeWaterMatrix.equals(WaterTypes.water3SOpW)) texture  = Assets.getWater3SideWOpen();
 					else if (typeWaterMatrix.equals(WaterTypes.water4SOp)) texture  = Assets.getWater4SideOpen();
 					batcher.draw(texture,i+8,j+1,1,1);		
 				}
 				if(!typeSunkenMatrix.equals(SunkenTypes.empty)){
 					 texture  = Assets.getSunkenObject();
 					 batcher.draw(texture,i+8,j+1,1,1);		
 				}
 			}
 		}
 		
 		
 		batcher.draw(Assets.getDirectionPanel(),0.25f,0,7,7);
 		batcher.draw(Assets.getButtonHarpoon(),19,0,2,4);
 		
 		for (int i=0;i<manager.getNumPlayers();i++){
 			if(manager.canPlay(i)){
 				position = manager.getPlayerPosition(i);
 				batcher.draw(penguinAnimations[i].getCurrentFrame(),(position.x)+8f,(position.y+1),1,1);
 			}
 		}
 		
 		paintLifes();
 		batcher.setProjectionMatrix(textCam.combined);
		font.draw(batcher, name, 1*32 ,13*32);
 		batcher.end();
 		guiCam.update();
 		textCam.update();
 		
 		if(manager.canPlay(numPlayer)){
 			if ((Gdx.input.isKeyPressed(Keys.W))||(Gdx.input.isKeyPressed(Keys.UP))){manager.movePlayer(Direction.up);}
 			if ((Gdx.input.isKeyPressed(Keys.S))||(Gdx.input.isKeyPressed(Keys.DOWN))){manager.movePlayer(Direction.down);}
 			if ((Gdx.input.isKeyPressed(Keys.A))||(Gdx.input.isKeyPressed(Keys.LEFT))){manager.movePlayer(Direction.left);}
 			if ((Gdx.input.isKeyPressed(Keys.D))||(Gdx.input.isKeyPressed(Keys.RIGHT))){manager.movePlayer(Direction.right);}
 			if (Gdx.input.isTouched()){
 				if (Gdx.input.isTouched(0))guiCam.unproject(touchPoint.set(Gdx.input.getX(0),Gdx.input.getY(0),0));
 				if (Gdx.input.isTouched(1))guiCam.unproject(touchPoint2.set(Gdx.input.getX(1),Gdx.input.getY(1),0));
 				if (fdBounds.contains(touchPoint)||fdBounds.contains(touchPoint2)){
 					manager.movePlayer(Direction.right);      
 				}
 				else if (fiBounds.contains(touchPoint)||fiBounds.contains(touchPoint2)){
 					manager.movePlayer(Direction.left);
 				}
 				else if (farBounds.contains(touchPoint)||farBounds.contains(touchPoint2)){
 					manager.movePlayer(Direction.up);
 				}
 				else if (fabBounds.contains(touchPoint)||fabBounds.contains(touchPoint2)){
 					manager.movePlayer(Direction.down);
 				}
 			}
 			if (Gdx.input.isKeyPressed(Keys.SPACE)) manager.putHarpoon();
 		
 			//This has to be just touched else the code of put harpoon run 2 times
 			if (Gdx.input.justTouched() &&(harpoonBounds.contains(touchPoint)||harpoonBounds.contains(touchPoint2))){
 				manager.putHarpoon();
 			}
 		}
 	}
 	
 	private void paintPlayerStatus(){
 		if(!manager.isThePlayerDead(numPlayer)){
 			if (numPlayer==0){
 				batcher.draw(Assets.getLifeIconRed(),6,12,0.75f,0.75f);
 			}else if(numPlayer==1){
 				batcher.draw(Assets.getLifeIconGreen(),6,12,0.75f,0.75f);
 			}else if(numPlayer==2){
 				batcher.draw(Assets.getLifeIconYellow(),6,12,0.75f,0.75f);
 			}else if(numPlayer==3){
 				batcher.draw(Assets.getLifeIconBlue(),6,12,0.75f,0.75f);
 			}
 		}else{
 			if (numPlayer==0){
 				batcher.draw(Assets.getDeadIconRed(),6,12,0.75f,0.75f);
 			}else if(numPlayer==1){
 				batcher.draw(Assets.getDeadIconGreen(),6,12,0.75f,0.75f);
 			}else if(numPlayer==2){
 				batcher.draw(Assets.getDeadIconYellow(),6,12,0.75f,0.75f);
 			}else if(numPlayer==3){
 				batcher.draw(Assets.getDeadIconBlue(),6,12,0.75f,0.75f);
 			}
 		}
 }
 			
 	private void paintLifes() {
 		for (int i=0;i<manager.getPlayerLifes(0);i++){
 			batcher.draw(Assets.getLifeIconRed(),1+2*i,10.805f,0.75f,0.75f);
 		}
 		for (int i=0;i<manager.getPlayerLifes(1);i++){		
 			batcher.draw(Assets.getLifeIconGreen(),1+2*i,9.68f,0.75f,0.75f);
 		}
 		for (int i=0;i<manager.getPlayerLifes(2);i++){
 			batcher.draw(Assets.getLifeIconYellow(),1+2*i,8.55f,0.75f,0.75f);
 		}
 		for (int i=0;i<manager.getPlayerLifes(3);i++){
 			batcher.draw(Assets.getLifeIconBlue(),1+2*i,7.43f,0.75f,0.75f);
 		}
 	}
 	
 	public void movePlayer(Direction dir, int playerId, Vector3 position) {                                     
         if (dir.equals(Direction.right)) currentFrame = penguinAnimations[playerId].getwalkAnimationR().getKeyFrame(stateTime, true); 
         else if (dir.equals(Direction.left)) currentFrame = penguinAnimations[playerId].getwalkAnimationL().getKeyFrame(stateTime, true);
         else if (dir.equals(Direction.up)) currentFrame = penguinAnimations[playerId].getwalkAnimationU().getKeyFrame(stateTime, true);
         else if (dir.equals(Direction.down)) currentFrame = penguinAnimations[playerId].getwalkAnimationD().getKeyFrame(stateTime, true);
         penguinAnimations[playerId].setCurrentFrame(currentFrame);     
 	}
 
 	public Game getGame() {
 		return game;
 	}
 	public void setGame(Game game) {
 		this.game = game;
 	}
 	public MatchManager getManager() {
 		return manager;
 	}
 	
 	public void setManager(MatchManager manager) {
 		this.manager = manager;
 	}
 	
 	@Override
 	public void resize(int width, int height) {
 		
 	}
 
 	@Override
 	public void resume() {		
 	}
 
 	@Override
 	public void show() {		
 	}
 	
 	public PenguinAnimation[] getPreguinAnimations() {
 		return penguinAnimations;
 	}
 	public void setPreguinAnimations(PenguinAnimation[] preguinAnimations) {
 		this.penguinAnimations = preguinAnimations;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public BitmapFont getFont() {
 		return font;
 	}
 	public void setFont(BitmapFont font) {
 		this.font = font;
 	}
 
 
 }
