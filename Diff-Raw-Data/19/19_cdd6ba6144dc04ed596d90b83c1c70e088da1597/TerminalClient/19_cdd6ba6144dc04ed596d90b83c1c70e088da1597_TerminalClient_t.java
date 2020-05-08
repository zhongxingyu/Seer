 package org.p_one.deathmaze;
 
 import com.googlecode.lanterna.screen.Screen;
 import com.googlecode.lanterna.screen.ScreenWriter;
 import com.googlecode.lanterna.terminal.Terminal;
 import com.googlecode.lanterna.terminal.TerminalSize;
 import com.googlecode.lanterna.TerminalFacade;
 import com.googlecode.lanterna.input.Key;
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
 	public int x, y;
 	public int player_x, player_y;
 
 	public TerminalClient() {
 		this.map = new DungeonMap();
 		Room aRoom = new Room(0, 0, true, true, true, true);
 		this.map.rooms.add(aRoom);
 		this.map.rooms.add(new Room(0, -1, false, false, true, false));
 		this.map.rooms.add(new Room(1, 0, false, true, false, true));
 		this.map.rooms.add(new Room(0, 1, true, false, true, false));
 		this.map.rooms.add(new Room(0, 2, true, false, true, false));
 		this.map.rooms.add(new Room(0, 3, true, false, true, false));
 		this.map.rooms.add(new Room(0, 4, true, false, true, false));
 		this.map.rooms.add(new Room(0, 5, true, false, true, false));
 		this.map.rooms.add(new Room(0, 6, true, false, true, false));
 		this.map.rooms.add(new Room(0, 7, true, false, true, false));
 		this.map.rooms.add(new Room(0, 8, true, false, true, false));
 
 		this.screen = TerminalFacade.createScreen();
 		this.writer = new ScreenWriter(screen);
 		this.x = -3;
 		this.y = -3;
 		this.player_x = 0;
 		this.player_y = 0;
 	}
 
 	public void run() {
 		this.screen.startScreen();
 		Key key = null;
 		while(key == null || key.getKind() != Key.Kind.Escape) {
 			this.drawField(this.x, this.y);
 			key = this.screen.getTerminal().readInput();
 			if(key != null) {
 				this.handleInput(key);
 			}
 		}
 		this.screen.stopScreen();
 	}
 
 	public void drawField(int x, int y) {
 		this.screen.clear();
 		int x_offset = 0 - x;
 		int y_offset = 0 - y;
 		for(Room room : this.map.rooms) {
 			Terminal.Color color = Terminal.Color.WHITE;
 			if(room.x == player_x && room.y == player_y) {
 				color = Terminal.Color.YELLOW;
 			}
 			this.drawRoom(room, x_offset, y_offset, color);
 		}
 
 		this.screen.refresh();
 	}
 
 	public void handleInput(Key input) {
 		char character = Character.toUpperCase(input.getCharacter());
 		Key.Kind kind = input.getKind();
		if(kind == Key.Kind.ArrowDown) {
 			this.player_y++;
 		} else if(kind == Key.Kind.ArrowUp) {
 			this.player_y--;
 		} else if(kind == Key.Kind.ArrowLeft) {
 			this.player_x--;
 		} else if(kind == Key.Kind.ArrowRight) {
 			this.player_x++;
		} else if(character == 'A') {
			this.x--;
		} else if(character == 'D') {
			this.x++;
		} else if(character == 'W') {
			this.y--;
		} else if(character == 'S') {
			this.y++;
 		} else if(character == ' ') {
 			this.map.rooms.add(new Room(this.x, this.y, true, true, true, true));
 		}
 	}
 
 	public void drawRoom(Room room, int x_offset, int y_offset, Terminal.Color color) {
 		int x = (room.x + x_offset) * 5;
 		int y = (room.y + y_offset) * 5;
 		this.writer.setBackgroundColor(color);
 		this.writer.drawString(x + 1, y + 1, "   ");
 		this.writer.drawString(x + 1, y + 2, "   ");
 		this.writer.drawString(x + 1, y + 3, "   ");
 		if(room.north) {
 			this.writer.drawString(x + 2, y + 0, " ");
 		}
 		if(room.east) {
 			this.writer.drawString(x + 4, y + 2, " ");
 		}
 		if(room.south) {
 			this.writer.drawString(x + 2, y + 4, " ");
 		}
 		if(room.west) {
 			this.writer.drawString(x + 0, y + 2, " ");
 		}
 		this.writer.setBackgroundColor(Terminal.Color.DEFAULT);
 	}
 }
