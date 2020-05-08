 package dsz;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 
 import org.jsfml.window.*;
 import org.jsfml.window.event.*;
 import org.jsfml.graphics.*;
 
 public class DSZ {
 	
 	static String title = "Dungeons, Swords & Zombies";
 
 	public static void main(String[] args) {
 		RenderWindow screen = new RenderWindow(new VideoMode(800,600),title);
 		screen.setFramerateLimit(60);
 		
 		TextureArray texs = new TextureArray("tiles.png",8,2);
 		Map bleh = new Map(texs);
 		try {
 			bleh.loadMapFile(new FileReader("test.map"));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
		System.out.println(bleh.colMap.get(441));
 		Sprite world = new Sprite(bleh.drawMap(800, 600));
 		
 		while(screen.isOpen()){
 			for(Event event : screen.pollEvents()){
 				if(event.type == Event.Type.CLOSED){
 					screen.close();
 				}
 			}
 			
 			screen.clear(Color.CYAN);
 			screen.draw(world);
 			screen.display();
 		}
 
 	}
 
 }
