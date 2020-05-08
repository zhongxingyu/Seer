 package iic3686.rocketscience;
 
 import org.andengine.entity.IEntity;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 
 public class GameThread extends Thread{
 
 	private Scene currentScene;
 	private GameManager gm;
 	//Game Logic Variables
 	boolean gameIsRunning;
 	int levelCounter;
 	Rectangle orderRectangle;
 	Rectangle timeRectangle;
 	//Time counter
 	int timeCounter;
 	int errorCounter;
 	float colorValue;
 	Sprite loseSplash;
 	Sprite victorySplash;
 	
 	private TagHandler th;
 	//
 	
 	public GameThread(Scene startScene, GameManager gm, Rectangle orderRectangle, Sprite loseSplash, Sprite victorySplash)
 	{
 		this.currentScene = startScene;
 		this.gm = gm;
 		this.gameIsRunning = true;
 		this.levelCounter = 1;
 		this.orderRectangle = orderRectangle;
 		this.timeCounter = 0;
 		this.errorCounter = 0;
 		this.loseSplash = loseSplash;
 		this.victorySplash = victorySplash;
 		this.th = TagHandler.getInstance();
 		this.timeRectangle = (Rectangle)currentScene.getChildByTag(th.getTag("timeRectangle"));
 	}
 	
 	public void run() {
 		
 		while(gameIsRunning)
 		{
 			timeCounter = 0;
 			errorCounter = 0;
 			while(!this.gm.isRoundOver())
 			{			
 				//Level crap
 				if(this.gm.currentLevel == 1)
 				{
 					//Display Nav and Comm
 					//Bring the black and white ones, move the originals
 					Sprite armoryOff = (Sprite)currentScene.getChildByTag(th.getTag("armoryOff"));
 					Sprite armoryOn = (Sprite)currentScene.getChildByTag(th.getTag("armory"));
 					Sprite kitchenOff = (Sprite)currentScene.getChildByTag(th.getTag("kitchenOff"));
 					Sprite kitchenOn = (Sprite)currentScene.getChildByTag(th.getTag("kitchen"));
 					armoryOn.setPosition(2000, 2000);
 					kitchenOn.setPosition(2000, 2000);
 					kitchenOff.setPosition(0, 260);
 					armoryOff.setPosition(0, 140);	
 				}
 				else if(this.gm.currentLevel == 2)
 				{
 					//Display Nav, Comm and Armory
 					Sprite armoryOff = (Sprite)currentScene.getChildByTag(th.getTag("armoryOff"));
 					Sprite armoryOn = (Sprite)currentScene.getChildByTag(th.getTag("armory"));
 					Sprite kitchenOff = (Sprite)currentScene.getChildByTag(th.getTag("kitchenOff"));
 					Sprite kitchenOn = (Sprite)currentScene.getChildByTag(th.getTag("kitchen"));
 					armoryOff.setPosition(2000, 2000);
 					kitchenOn.setPosition(2000, 2000);
 					kitchenOff.setPosition(0, 260);
 					armoryOn.setPosition(0, 140);	
 				}
 				else
 				{
 					//Display everything
 					Sprite armoryOff = (Sprite)currentScene.getChildByTag(th.getTag("armoryOff"));
 					Sprite armoryOn = (Sprite)currentScene.getChildByTag(th.getTag("armory"));
 					Sprite kitchenOff = (Sprite)currentScene.getChildByTag(th.getTag("kitchenOff"));
 					Sprite kitchenOn = (Sprite)currentScene.getChildByTag(th.getTag("kitchen"));
 					armoryOff.setPosition(2000, 2000);
 					kitchenOff.setPosition(2000, 2000);
 					kitchenOn.setPosition(0, 260);
 					armoryOn.setPosition(0, 140);	
 				}
 				
 				
 				//GAME LOOP EL LUP LUP
 				if(this.gm.currentOrder == null)
 					this.gm.getNewOrder();
 				//Create new round
 				/*if(this.gm.isRoundOver())
 				{
 					//Start new round
 					this.gm.newRound();
 				}*/
 				
 				//ERROR CHECK
 				if(errorCounter > 10)
 				{
 					loseSplash.setPosition(0,200);
 					try {
 						Thread.sleep(2000);
 						} catch (InterruptedException e) {
 						e.printStackTrace();
 						}
 					loseSplash.setPosition(2000,2000);
 					this.gm.resetGame();
 					errorCounter = 0;
 					levelCounter = 1;
 					Text orderTextLabel= (Text)currentScene.getChildByTag(th.getTag("orderText"));
 					orderTextLabel.setText("Get ready for Level "+levelCounter+"!");
 					try {
 						Thread.sleep(2000);
 						} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 						}
 					
 				}
 				//Check to see if the order has changed recently
 				if(this.gm.hasBeenChangedRecently())
 					timeCounter = 0;
 				//Change the text of the buttons if needed
 				//currentScene
 				//Change the text of the instruction and check the status
 				//Background progressive change
 				//if(timeCounter < 5000)
 				if(timeCounter < this.gm.timeLimit)
 				{
 					//colorValue = (float)0.8+(timeCounter/5000);
 					//this.orderRectangle.setColor((float)colorValue, 0, 0);
 					//timeRectangle.setWidth((float) (480.0 / 5000)*(5000 - timeCounter));
 					timeRectangle.setWidth((float) (480.0 / this.gm.timeLimit)*(this.gm.timeLimit - timeCounter));
 					timeCounter++;
 				}
 				else
 				{
 					//5 seconds have passed, change mission, increase errorCounter
 					this.gm.getNewOrder();
 					errorCounter++;
 					this.orderRectangle.setColor(1, 1, 1);
 					timeCounter = 0;
 				}
 				
 				Text orderTextLabel= (Text)currentScene.getChildByTag(th.getTag("orderText"));
 				orderTextLabel.setText(this.gm.getCurrentOrderText());
 				//IEntity boton1 = currentScene.getChildByTag(10);
 				//boton1.setVisible(!boton1.isVisible());
 				//IEntity boton2 = currentScene.getChildByTag(11);
 				//boton2.setVisible(!boton2.isVisible());
 				
 				Sprite NaveChica = (Sprite)currentScene.getChildByTag(th.getTag("NaveChica"));
 				NaveChica.setX((gm.currentOrderNumber * 460)/gm.ordersRound);
 				
 				try {
 					//this.orderRectangle.setColor(1, 1, 1);
					Thread.sleep(1);
 					//this.orderRectangle.setColor(1, 0, 0);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			//END OF ROUND
			Text orderTextLabel= (Text)currentScene.getChildByTag(th.getTag("orderText"));
 			//orderTextLabel.setText("Level Complete!");
 			victorySplash.setPosition(0,200);
 			try {
				Thread.sleep(4000);
 				} catch (InterruptedException e) {
 				e.printStackTrace();
 				}
 			levelCounter++;
 			victorySplash.setPosition(2000,2000);
 			if(this.gm.currentLevel == 1)
 			{
 				orderTextLabel.setText("You reached the atmosphere.");
 				try {
					Thread.sleep(2000);
 					} catch (InterruptedException e) {
 					e.printStackTrace();
 					}
 				orderTextLabel.setText("Level 2: Reach the Moon");
 			}
 			else if(this.gm.currentLevel == 2)
 			{
 				orderTextLabel.setText("You reached the Moon.");
 				try {
					Thread.sleep(2000);
 					} catch (InterruptedException e) {
 					e.printStackTrace();
 					}
 				orderTextLabel.setText("Level 3: Reach Mars");
 			}
 			else if(this.gm.currentLevel == 3)
 			{
 				orderTextLabel.setText("You reached Mars.");
 				try {
					Thread.sleep(2000);
 					} catch (InterruptedException e) {
 					e.printStackTrace();
 					}
 				orderTextLabel.setText("Level 4: Explore space.");
 			}
 			else
 			{
 				orderTextLabel.setText("Get ready for Level "+levelCounter+"!");
 			}
 			
 			try {
				Thread.sleep(2000);
 				} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				}
 			this.gm.newRound();
 			
 		}
     }
 	
 }
