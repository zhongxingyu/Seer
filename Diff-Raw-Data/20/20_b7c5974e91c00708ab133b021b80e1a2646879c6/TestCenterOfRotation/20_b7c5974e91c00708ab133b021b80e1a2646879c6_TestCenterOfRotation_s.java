 package pl.shockah.easyslick.tests;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.geom.Circle;
 import pl.shockah.easyslick.App;
 import pl.shockah.easyslick.EHandler;
 import pl.shockah.easyslick.GraphicsHelper;
 import pl.shockah.easyslick.Image;
 import pl.shockah.easyslick.Room;
 
 public class TestCenterOfRotation {
 	public static void main(String[] args) {
 		App.start(new RoomTest(),"Test - centerOfRotation");
 	}
 	
 	public static class RoomTest extends Room {
 		private static Image img = null;
 		private float angle = 0;
 		
 		protected void setupRoom() {
 			maxFPS = 60;
 		}
 		
 		protected void onCreate() {
			img = EHandler.newImage("pngFile2.png");
 			img.center();
 		}
 		
 		protected void onTick() {
 			angle += 2;
 		}
 		protected void onRender(GraphicsHelper gh) {
 			img.setRotation(angle);
 			img.draw(size.x/2,size.y/2,img.getWidth(),img.getHeight());
 			
 			gh.g().setColor(Color.red);
 			gh.g().draw(new Circle(size.x/2,size.y/2,3,8));
 		}
 	}
 }
