 package iic3686.rocketscience;
 
 import org.andengine.entity.IEntity;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 
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
 		this.timeRectangle = (Rectangle)currentScene.getChildByTag(500);
 	}
 	
 	public void run() {
 		
 		while(gameIsRunning)
 		{
 			timeCounter = 0;
 			errorCounter = 0;
 			while(!this.gm.isRoundOver())
 			{
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
 						this.sleep(2000);
 						} catch (InterruptedException e) {
 						e.printStackTrace();
 						}
 					loseSplash.setPosition(2000,2000);
 					this.gm.resetGame();
					errorCounter = 0;
 					levelCounter = 1;
 					Text orderTextLabel= (Text)currentScene.getChildByTag(1);
 					orderTextLabel.setText("Get ready for Level "+levelCounter+"!");
 					try {
 						this.sleep(2000);
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
 				if(timeCounter < 5000)
 				{
 					//colorValue = (float)0.8+(timeCounter/5000);
 					//this.orderRectangle.setColor((float)colorValue, 0, 0);
 					timeRectangle.setWidth((float) (480.0 / 5000)*(5000 - timeCounter));
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
 				
 				Text orderTextLabel= (Text)currentScene.getChildByTag(1);
 				orderTextLabel.setText(this.gm.getCurrentOrderText());
 				//IEntity boton1 = currentScene.getChildByTag(10);
 				//boton1.setVisible(!boton1.isVisible());
 				//IEntity boton2 = currentScene.getChildByTag(11);
 				//boton2.setVisible(!boton2.isVisible());
 				try {
 					//this.orderRectangle.setColor(1, 1, 1);
 					this.sleep(1);
 					//this.orderRectangle.setColor(1, 0, 0);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			//END OF ROUND
 			Text orderTextLabel= (Text)currentScene.getChildByTag(1);
 			//orderTextLabel.setText("Level Complete!");
 			victorySplash.setPosition(0,200);
 			try {
 				this.sleep(4000);
 				} catch (InterruptedException e) {
 				e.printStackTrace();
 				}
 			levelCounter++;
 			victorySplash.setPosition(2000,2000);
 			orderTextLabel.setText("Get ready for Level "+levelCounter+"!");
 			try {
 				this.sleep(2000);
 				} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				}
 			this.gm.newRound();
 			
 		}
     }
 	
 }
