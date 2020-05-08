 package org.p_one.deathmaze;
 
 import com.googlecode.lanterna.screen.Screen;
 import com.googlecode.lanterna.screen.ScreenWriter;
 import com.googlecode.lanterna.TerminalFacade;
 import org.p_one.deathmaze.DungeonMap;
 import org.p_one.deathmaze.Room;
 
 public class TerminalClient {
 	public static void main(String[] args) {
 		TerminalClient client = new TerminalClient();
 		client.run();
 	}
 
 	public DungeonMap map;
 	public Screen screen;
 	public ScreenWriter writer;
 
 	public TerminalClient() {
 		this.map = new DungeonMap();
 		Room aRoom = new Room(0, 0, true, true, true, true);
 		this.map.rooms.add(aRoom);
 
 		this.screen = TerminalFacade.createScreen();
 		this.writer = new ScreenWriter(screen);
 
 	}
 
 	public void run() {
 		int x = 0, y = 0;
 
 		this.screen.startScreen();
 		for(Room room : this.map.rooms) {
 			this.drawRoom(room, x, y);
 		}
 
 		this.screen.refresh();
 		try {
 			Thread.sleep(5000);
 		} catch(InterruptedException e) {
 		}
 		this.screen.stopScreen();
 	}
 
 	public void drawRoom(Room room, int x, int y) {
 		this.writer.drawString(x + 1, y + 1, "***");
 		this.writer.drawString(x + 1, y + 2, "***");
 		this.writer.drawString(x + 1, y + 3, "***");
 		if(room.north) {
			this.writer.drawString(x + 2, y + 0, "*");
 		}
 		if(room.east) {
			this.writer.drawString(x + 4, y + 2, "*");
 		}
 		if(room.south) {
			this.writer.drawString(x + 2, y + 4, "*");
 		}
 		if(room.west) {
			this.writer.drawString(x + 0, y + 2, "*");
 		}
 	}
 }
