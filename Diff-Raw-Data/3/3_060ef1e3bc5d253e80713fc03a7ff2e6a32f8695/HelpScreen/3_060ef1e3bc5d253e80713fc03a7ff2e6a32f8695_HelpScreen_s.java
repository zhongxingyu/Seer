 package com.jeremyP.diseasedefense;
 
 import java.util.List;
 
 import com.jeremyP.diseasedefense.framework.Game;
 import com.jeremyP.diseasedefense.framework.Graphics;
 import com.jeremyP.diseasedefense.framework.Input.TouchEvent;
 import com.jeremyP.diseasedefense.framework.Screen;
 
 public class HelpScreen extends Screen { 
 	
 	int index; /* index of which help screen you are on */
 	
     public HelpScreen(Game game, int index) {
         super(game);
         this.index = index;
         //System.out.println("Help Screen " + index);
     }
 
     @Override
     public void update(float deltaTime) {
         List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
         game.getInput().getKeyEvents();
         
         int len = touchEvents.size();
         for(int i = 0; i < len; i++) {
             TouchEvent event = touchEvents.get(i);
             if(event.type == TouchEvent.TOUCH_UP) {
             	
 				//int contin_x = g.getWidth()/2 - Assets.contin.getWidth()/2;
 				//int contin_y = (int) (g.getHeight() - (g.getHeight()*.05)) - Assets.contin.getHeight();
             	
             	int next_x = game.getGraphics().getWidth() - Assets.next.getWidth();
             	int next_y = game.getGraphics().getHeight() - Assets.next.getHeight();
             	
             	int back_x = game.getGraphics().getWidth() - Assets.back.getWidth() - Assets.next.getWidth() - 50;
             	int back_y = game.getGraphics().getHeight() - Assets.back.getHeight();
             	
             	int home_x = 0;
             	int home_y = game.getGraphics().getHeight() - Assets.home.getHeight();
             	
             	//Did you click next?
             	if(event.x > next_x && event.x < next_x + Assets.next.getWidth() && event.y > next_y && event.y < Assets.next.getWidth() + next_y){
             		Assets.click.play(1);
             		if(Assets.helpScreen.length == (index+1)){
                 		game.getGraphics().clear(0);
                 		game.setScreen(new MainMenuScreen(game));
                 		return;
             		}else{
 	            		game.getGraphics().clear(0);
 	            		game.setScreen(new HelpScreen(game, index+1));
             		}
             	}
             	
             	//Did you click back?
             	if(event.x > back_x && event.x < back_x + Assets.back.getWidth() && event.y > back_y && event.y < Assets.back.getWidth() + back_y){
             		if(index == 0){
                		Assets.click.play(1);
                 		game.getGraphics().clear(0);
                 		game.setScreen(new MainMenuScreen(game));
                 		return;
             		}else{
 	            		game.getGraphics().clear(0);
 	            		game.setScreen(new HelpScreen(game, index-1));
             		}
             	}
             	
             	//Did you click home?
             	if(event.x > home_x && event.x < home_x + Assets.home.getWidth() && event.y > home_y && event.y < Assets.home.getWidth() + home_y){
             		Assets.click.play(1);
             		game.getGraphics().clear(0);
             		game.setScreen(new MainMenuScreen(game));
             	}            	
             	
                 /*if(event.x > 256 && event.y > 416 ) {
                 	game.getGraphics().clear(0);
                     Assets.click.play(1);
                 	if(!(Assets.helpScreen.length == i)){
                         game.setScreen(new HelpScreen(game, i+1));
                 	}
                     return;
                 }*/
             }
         }
     }
 
     @Override
     public void present(float deltaTime) {
         Graphics g = game.getGraphics();      
         g.drawPixmap(Assets.helpScreen[index], 0, 0);
         
         //Draw buttons
         g.drawPixmap(Assets.next, game.getGraphics().getWidth() - Assets.next.getWidth(), game.getGraphics().getHeight() - Assets.next.getHeight());
         g.drawPixmap(Assets.back, game.getGraphics().getWidth() - Assets.back.getWidth() - Assets.next.getWidth() - 50, game.getGraphics().getHeight() - Assets.back.getHeight());
         g.drawPixmap(Assets.home, 0, game.getGraphics().getHeight() - Assets.home.getHeight());
     }
 
     @Override
     public void pause() {
 
     }
 
     @Override
     public void resume() {
 
     }
 
     @Override
     public void dispose() {
 
     }
 }
