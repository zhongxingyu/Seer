 package Game;
 
 import javafx.animation.KeyFrame;
 import javafx.animation.Timeline;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.util.Duration;
 import javafx.scene.shape.Circle;
 
 import org.jbox2d.dynamics.Body;
 
 
 public class Time {
 	Timeline timeline;
 	GameWorld world;
 	
 	final EventHandler<ActionEvent> ae = new EventHandler<ActionEvent>(){
 		public void handle(ActionEvent t){
 			world.world.step(1.0f/60.0f, 8, 3);
 			for(Entity a: world.gameElements){
 				Body body = (Body)a.node.getUserData();
 				if(a.node instanceof Circle){
 					float xpos = Utility.toPixelPosX(body.getPosition().x) + App.offsetX;
 					float ypos = Utility.toPixelPosY(body.getPosition().y) + App.offsetY;
 					a.node.setLayoutX(xpos);
 					a.node.setLayoutY(ypos);
 				}
 				else{
 					float xpos = Utility.toPixelPosX(body.getPosition().x) + App.offsetX - Utility.toPixelWidth(a.width)/2;
 					float ypos = Utility.toPixelPosY(body.getPosition().y) + App.offsetY -  Utility.toPixelWidth(a.height)/2;
 					a.node.setLayoutX(xpos);
 					a.node.setLayoutY(ypos);
 				}
				float xPos = Utility.toPixelPosX(body.getPosition().x) + App.offsetX;
				float yPos = Utility.toPixelPosY(body.getPosition().y) + App.offsetY;
				a.node.setLayoutX(xPos);
				a.node.setLayoutY(yPos);
 			}
 		}
 	};
 	
 	public Time(GameWorld world){
 		timeline = new Timeline();
 		timeline.setCycleCount(Timeline.INDEFINITE);
 		Duration duration = Duration.seconds(1.0/60.0);
 		KeyFrame frame = new KeyFrame(duration,ae,null,null);
 		timeline.getKeyFrames().add(frame);
 		this.world = world;
 	}
 	public void stopTime(){
 		timeline.pause();
 	}
 	public void startTime(){
 		timeline.play();
 	}
 	public void toggleTime(){
 		if(timeline.getCurrentRate() > 0)
 			stopTime();
 		else
 			startTime();
 	}
 	public void reverseTime(){
 		System.out.println("NOT IMPLEMENTED");
 	}
 	
 	//For experimental time manipulation
 	public Time(GameWorld world, int a){
 		
 	}
 
 }
