 package pl.shockah.easyslick.tests;
 
 import org.newdawn.slick.Color;
 import pl.shockah.easyslick.App;
 import pl.shockah.easyslick.EHandler;
 import pl.shockah.easyslick.Entity;
 import pl.shockah.easyslick.GraphicsHelper;
 import pl.shockah.easyslick.Image;
 import pl.shockah.easyslick.Room;
 import pl.shockah.easyslick.gui.Gui3Radio;
 import pl.shockah.easyslick.gui.GuiColorPickerRGB;
 import pl.shockah.easyslick.gui.GuiEditbox;
 import pl.shockah.easyslick.gui.GuiMessage;
 import pl.shockah.easyslick.gui.GuiSliderInt;
 import pl.shockah.easyslick.gui.GuiYesNo;
 
 public class TestGui {
 	public static void main(String[] args) {
 		App.start(new RoomTest(),"Test - GUI");
 	}
 	
 	public static class RoomTest extends Room {
 		public RoomTest() {
 			maxFPS = 60;
 			size.y = viewSize.y = 400;
 		}
 		
 		protected void onCreate() {
 			new EntityTest().create();
 		}
 	}
 	
 	public static class EntityTest extends Entity {
 		public static Image img = null;
 		public int timer = 60;
 		
 		public void onTick(int tick) {
 			timer--;
 			if (timer <= 0) {
 				timer += 60;
 				
 				GuiEditbox gui1 = new GuiEditbox("This is a GuiEditbox","gergerbrtje"); gui1.create();
 				new GuiMessage("This is a GuiMessage\nTyped in text:\n"+gui1.getValue()).create();
 				Gui3Radio gui2 = new Gui3Radio("This is a Gui3Radio","lol1","lol2","lol3"); gui2.create();
 				new GuiMessage("Another GuiMessage\nSelected radio:\n"+gui2.getValue()).create();
 				GuiSliderInt gui3 = new GuiSliderInt("This is a GuiSliderInt",2); gui3.create();
 				new GuiMessage("Another GuiMessage\nValue on slider:\n"+gui3.getValue()).create();
 				GuiColorPickerRGB gui4 = new GuiColorPickerRGB("This is a GuiColorPickerRGB",Color.white); gui4.create();
 				new GuiMessage("Another GuiMessage\nColor:\n"+gui4.getValue()).create();
 				GuiYesNo guiEnd = new GuiYesNo("This is a GuiYesNo\nDo you want to exit the application?"); guiEnd.create(); if (guiEnd.getValue()) App.stop();
 			}
 		}
 		public void onRender(GraphicsHelper gh) {
			if (img == null) img = EHandler.newImage("pngFile.png");
 			gh.g().drawImage(img,pos.x,pos.y);
 		}
 	}
 }
