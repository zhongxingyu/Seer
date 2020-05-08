 package clueGame;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 public class RoomCell extends BoardCell {
 	public enum DoorDirection {UP, DOWN, LEFT, RIGHT, NONE};
 	private DoorDirection doorDirection;
 	private char roomInitial;
 	private boolean nameSquare;
 	
 	public RoomCell(String room) {
 		roomInitial = room.charAt(0);
 		if (room.length() == 2)
 			switch (room.charAt(1)){
 			case 'R': 
 				doorDirection = DoorDirection.RIGHT;
 				break;
 			case 'L': 
 				doorDirection = DoorDirection.LEFT;
 				break;
 			case 'U': 
 				doorDirection = DoorDirection.UP;
 				break;
 			case 'D': 
 				doorDirection = DoorDirection.DOWN;
 				break;
 			case 'N': 
 				doorDirection = DoorDirection.NONE;
 				nameSquare = true;
 			}
 		else  {
 			doorDirection = DoorDirection.NONE;
 			nameSquare = false;
 		}
 	}
 	
 	@Override
 	public boolean isRoom() {
 		return true;
 	}
 	
 	@Override
 	public boolean isDoorway() {
 		if (doorDirection == DoorDirection.NONE)
 			return false;
 		return true;
 	}
 	
 	public char getInitial() {
 		return roomInitial;
 	}
 	
 	public DoorDirection getDoorDirection() {
 		return doorDirection;
 	}
 	@Override
 	public void drawCell(Graphics g, Board b) {
 		g.setColor(Color.GRAY);
 		g.fillRect(row*SIDE_LENGTH,  column*SIDE_LENGTH, SIDE_LENGTH, SIDE_LENGTH);
 		g.setColor(Color.BLUE);
 		if(isDoorway()) {
 			if(doorDirection == DoorDirection.UP) {
 				g.fillRect(row*SIDE_LENGTH, column*SIDE_LENGTH, SIDE_LENGTH, 4);
 			}
 			else if(doorDirection == DoorDirection.LEFT) {
 				g.fillRect(row*SIDE_LENGTH, column*SIDE_LENGTH, 4, SIDE_LENGTH);
 			}
 			else if(doorDirection == DoorDirection.DOWN) {
 				g.fillRect(row*SIDE_LENGTH, column*SIDE_LENGTH +SIDE_LENGTH - 4, SIDE_LENGTH, 4);
 			}
 			else if(doorDirection == DoorDirection.RIGHT) {
 				g.fillRect(row*SIDE_LENGTH + SIDE_LENGTH - 4, column*SIDE_LENGTH, 4, SIDE_LENGTH);
 			}
 		}
 		if(nameSquare) {
			g.drawString(b.getRooms().get(this.roomInitial),row*SIDE_LENGTH  , column*SIDE_LENGTH - 4);
 		}
 		
 
 	}
 
 }
