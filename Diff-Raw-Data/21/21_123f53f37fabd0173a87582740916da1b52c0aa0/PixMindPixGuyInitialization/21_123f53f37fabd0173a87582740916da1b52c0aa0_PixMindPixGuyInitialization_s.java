 package com.pix.mind.world;
 
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.actors.PixGuyActor;
 import com.pix.mind.box2d.bodies.PixGuy;
 import com.pix.mind.controllers.AccController;
 import com.pix.mind.controllers.ArrowController;
 import com.pix.mind.controllers.KeyboardController;
 import com.pix.mind.controllers.PixGuyController;
 
 public class PixMindPixGuyInitialization {
 
 	private PixGuy pixGuy;
 	private PixGuyController controller;
 	private Image pixGuySkin;
 	PixMindBox2DInitialization box2D;
 	public PixMindPixGuyInitialization(PixMindScene2DInitialization scene2D, PixMindGame game,
 			PixMindBox2DInitialization box2D) {
 		// main character initialization
 		pixGuy = new PixGuy(box2D.getWorld(), 0.35f, 0.35f);
 		pixGuySkin = new PixGuyActor(pixGuy);
 		this.box2D = box2D;
 		// setting controller
 		
 		
		/**MODELO PARA MOVIL
 		if (PixMindGame.infoController){
 			controller = new AccController(pixGuy);			
 		}
 		else{
 			controller = new ArrowController(pixGuy, scene2D.getStageGui());			
		}*/
 		
 		//MODELO PARA PC
		if (PixMindGame.infoController){
 			controller = new KeyboardController(pixGuy,  scene2D.getStageGui());			
 		}
 		else{
 			controller = new KeyboardController(pixGuy,  scene2D.getStageGui());			
 		}
 		
 		
 		pixGuy.setController(controller);	
 		scene2D.getGroupStage().addActor(pixGuySkin);
 		
 	}
 
 	public PixGuy getPixGuy() {
 		return pixGuy;
 	}
 
 	public void setPixGuy(PixGuy pixGuy) {
 		this.pixGuy = pixGuy;
 	}
 
 	public PixGuyController getController() {
 		return controller;
 	}
 
 	public void setController(PixGuyController controller) {
 		this.controller = controller;
 	}
 
 	public Image getPixGuySkin() {
 		return pixGuySkin;
 	}
 
 	public void setPixGuySkin(Image pixGuySkin) {
 		this.pixGuySkin = pixGuySkin;
 	}
 	public void setPixGuyPosition(float pixGuyBeginsX, float pixGuyBeginsY){
 		
 		pixGuy.setPixGuyPosition(pixGuyBeginsX, pixGuyBeginsY);
 	}
 	
 	
 
 }
