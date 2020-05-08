 package org.p_one.deathmaze;
 
 import java.util.Map;
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
 
 	private Game game;
 	public Screen screen;
 	public ScreenWriter writer;
 	public int x, y;
 
 	public TerminalClient() {
 		this.game = new Game();
 		Room aRoom = new Room(0, 0, Chit.FOUR_WAY, true);
 		this.game.map.add(aRoom);
 
 		this.screen = TerminalFacade.createScreen();
 		this.writer = new ScreenWriter(screen);
 		this.x = -3;
 		this.y = -3;
 	}
 
 	public void run() {
 		this.screen.startScreen();
 		Key key = null;
 		while(this.game.state != Game.State.QUIT) {
 			if(Game.State.PLAYING == this.game.state) {
 				this.drawField(this.x, this.y);
 			} else if(Game.State.DEAD == this.game.state) {
 				this.screen.clear();
 				this.writer.setForegroundColor(Terminal.Color.RED);
				this.writer.drawString(3, 3, "You died because you " + game.lastAction.getDescription() + ", stupid.");
 				this.screen.refresh();
 			} else if(Game.State.LOST == this.game.state) {
 				this.screen.clear();
 				this.writer.setForegroundColor(Terminal.Color.RED);
 				this.writer.drawString(3, 3, "You lost because you " + game.lastAction.getDescription() + ", stupid.");
 				this.screen.refresh();
 			} else if(Game.State.WON == this.game.state) {
 				this.screen.clear();
 				this.writer.setForegroundColor(Terminal.Color.GREEN);
 				this.writer.drawString(3, 3, "You left the dungeon stupidly rich.");
 				this.screen.refresh();
 			}
 
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
 		for(Room room : this.game.map.rooms) {
 			Terminal.Color color = Terminal.Color.WHITE;
 			if(room.x == this.game.player_x && room.y == this.game.player_y) {
 				color = Terminal.Color.YELLOW;
 			}
 			this.drawRoom(room, x_offset, y_offset, color);
 		}
 
 		for(Map.Entry<Integer, Integer> monster : this.game.monsters) {
 			this.drawMonster(monster.getKey(), monster.getValue(), x_offset, y_offset);
 		}
 
 		this.drawPlayer(this.game, x_offset, y_offset, Terminal.Color.YELLOW);
 		if(this.game.roomToPlace != null) {
 			this.drawHighlight(x_offset, y_offset);
 			Terminal.Color color = this.game.map.validRoom(this.game.roomToPlace) ? Terminal.Color.GREEN : Terminal.Color.RED;
 			this.drawRoom(this.game.roomToPlace, x_offset, y_offset, color);
 		}
 
 		this.screen.refresh();
 	}
 
 	public void handleInput(Key input) {
 		char character = Character.toUpperCase(input.getCharacter());
 		Key.Kind kind = input.getKind();
 		if(kind == Key.Kind.ArrowDown) {
 			if(this.game.roomToPlace == null) {
 				this.game.moveSouth();
 			}
 			this.forceFieldToCursor();
 		} else if(kind == Key.Kind.ArrowUp) {
 			if(this.game.roomToPlace == null) {
 				this.game.moveNorth();
 			}
 			this.forceFieldToCursor();
 		} else if(kind == Key.Kind.ArrowLeft) {
 			if(this.game.roomToPlace == null) {
 				this.game.moveWest();
 			}
 			this.forceFieldToCursor();
 		} else if(kind == Key.Kind.ArrowRight) {
 			if(this.game.roomToPlace == null) {
 				this.game.moveEast();
 			}
 			this.forceFieldToCursor();
 		} else if(character == 'A') {
 			this.x--;
 		} else if(character == 'D') {
 			this.x++;
 		} else if(character == 'W') {
 			this.y--;
 		} else if(character == 'S') {
 			this.y++;
 		} else if(this.game.roomToPlace != null && character == 'Z') {
 			this.game.roomToPlace.rotate();
 		} else if(character == ' ') {
 			if(this.game.roomToPlace == null) {
 				this.game.action();
 			} else if(this.game.roomToPlace != null) {
 				if(this.game.map.validRoom(this.game.roomToPlace)) {
 					this.game.placeRoom();
 				} else {
 					this.game.roomToPlace = new Room(this.game.roomToPlace.x, this.game.roomToPlace.y);
 				}
 			}
 		}
 	}
 
 	private void forceFieldToCursor() {
 		int cursor_x = 0, cursor_y = 0;
 		if(this.game.roomToPlace != null) {
 			cursor_x = this.game.roomToPlace.x;
 			cursor_y = this.game.roomToPlace.y;
 		} else {
 			cursor_x = this.game.player_x;
 			cursor_y = this.game.player_y;
 		}
 
 		int x_offset = 0 - this.x;
 		int y_offset = 0 - this.y;
 
 		TerminalSize size = this.screen.getTerminal().getTerminalSize();
 		if(0 > (cursor_x - this.x) * 5) {
 			this.x = cursor_x - 2;
 		}
 
 		if(0 > (cursor_y - this.y) * 5) {
 			this.y = cursor_y - 2;
 		}
 
 		if(size.getColumns() <= (cursor_x - this.x) * 5) {
 			this.x = cursor_x -2;
 		}
 
 		if(size.getRows() <= (cursor_y - this.y) * 5) {
 			this.y = cursor_y - 2;
 		}
 	}
 
 	public void drawHighlight(int x_offset, int y_offset) {
 		this.writer.setBackgroundColor(Terminal.Color.MAGENTA);
 		int x, y;
 		x = (this.game.roomToPlace.x + x_offset) * 5;
 		y = (this.game.roomToPlace.y + y_offset) * 5;
 		this.writer.drawString(x, y, "     ");
 		this.writer.drawString(x, y + 1, "     ");
 		this.writer.drawString(x, y + 2, "     ");
 		this.writer.drawString(x, y + 3, "     ");
 		this.writer.drawString(x, y + 4, "     ");
 		this.writer.setBackgroundColor(Terminal.Color.DEFAULT);
 	}
 
 	public void drawRoom(Room room, int x_offset, int y_offset, Terminal.Color color) {
 		int x = (room.x + x_offset) * 5;
 		int y = (room.y + y_offset) * 5;
 		this.writer.setBackgroundColor(color);
 		this.writer.setForegroundColor(Terminal.Color.BLACK);
 		this.writer.drawString(x + 1, y + 1, "   ");
 		this.writer.drawString(x + 1, y + 2, "   ");
 		this.writer.drawString(x + 1, y + 3, "   ");
 		if(room.getNorth() == Chit.Exit.DOOR) {
 			this.writer.drawString(x + 2, y + 0, " ");
 		} else if(room.getNorth() == Chit.Exit.CORRIDOR) {
 			this.writer.drawString(x + 1, y + 0, "   ");
 		}
 		if(room.getEast() == Chit.Exit.DOOR) {
 			this.writer.drawString(x + 4, y + 2, " ");
 		} else if(room.getEast() == Chit.Exit.CORRIDOR) {
 			this.writer.drawString(x + 4, y + 1, " ");
 			this.writer.drawString(x + 4, y + 2, " ");
 			this.writer.drawString(x + 4, y + 3, " ");
 		}
 
 		if(room.getSouth() == Chit.Exit.DOOR) {
 			this.writer.drawString(x + 2, y + 4, " ");
 		} else if(room.getSouth() == Chit.Exit.CORRIDOR) {
 			this.writer.drawString(x + 1, y + 4, "   ");
 		}
 
 		if(room.getWest() == Chit.Exit.DOOR) {
 			this.writer.drawString(x + 0, y + 2, " ");
 		} else if(room.getWest() == Chit.Exit.CORRIDOR) {
 			this.writer.drawString(x + 0, y + 1, " ");
 			this.writer.drawString(x + 0, y + 2, " ");
 			this.writer.drawString(x + 0, y + 3, " ");
 		}
 
 		if(room.getFeature() == Chit.Feature.FOUNTAIN) {
 		        this.writer.drawString(x + 2, y + 2, "F");
 		} else if(room.getFeature() == Chit.Feature.STATUE) {
 		        this.writer.drawString(x + 2, y + 2, "S");
 		} else if(room.getFeature() == Chit.Feature.TRAPDOOR) {
 		        this.writer.drawString(x + 2, y + 2, "D");
 		}
 
 		if(room.isEntrance()) {
 		        this.writer.drawString(x + 2, y + 2, "➚");
 		}
 		this.writer.setBackgroundColor(Terminal.Color.DEFAULT);
 		this.writer.setForegroundColor(Terminal.Color.DEFAULT);
 	}
 
 	public void drawMonster(int monster_x, int monster_y, int x, int y) {
 		x = ((monster_x + x) * 5) + 2;
 		y = ((monster_y + y) * 5) + 2;
 
 		this.writer.setBackgroundColor(Terminal.Color.WHITE);
 		this.writer.setForegroundColor(Terminal.Color.BLACK);
 		this.writer.drawString(x, y, "M");
 		this.writer.setForegroundColor(Terminal.Color.DEFAULT);
 		this.writer.setBackgroundColor(Terminal.Color.DEFAULT);
 	}
 
 	public void drawPlayer(Game state, int x, int y, Terminal.Color color) {
 		x = ((state.player_x + x) * 5) + 2;
 		y = ((state.player_y + y) * 5) + 2;
 		this.writer.setBackgroundColor(color);
 		this.writer.drawString(x, y, "⍟");
 		this.writer.setBackgroundColor(Terminal.Color.DEFAULT);
 	}
 }
