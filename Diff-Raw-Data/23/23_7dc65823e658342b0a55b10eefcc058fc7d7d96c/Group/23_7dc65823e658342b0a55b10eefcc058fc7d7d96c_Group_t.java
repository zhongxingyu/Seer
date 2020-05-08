 
 package com.bushidoburrito.go;
 
 import java.util.*;
 
 public class Group
 {
 	ArrayList<Stone> stones;
 
 	public StoneColor color;
 
 	public Group() {
 		stones = new ArrayList<Stone>();
 		color = StoneColor.NONE;
 	}
 
 	public boolean contains(Stone stone) {
		for (Iterator<Stone> i = stones.iterator(); i.hasNext(); ) {
			Stone groupStone = i.next();
 			if (stone.x == groupStone.x &&
 				stone.y == groupStone.y) {
 
 				// TODO: throw an exception if colors don't match
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void createFromStone(Stone stone, BoardData board) {
 		if (stone.color == StoneColor.NONE) {
 			// TODO: throw an exception here
 			System.out.println("error: tried to add empty stone");
 			return;
 		}
 		if (stone.group != null) {
 			// TODO: throw an exception here
 			System.out.println("error: stone already has group");
 			return;
 		}
 		if (color == StoneColor.NONE) {
 			color = stone.color;
 		} else if (color != stone.color) {
 			// TODO: throw an exception here
 			System.out.println("error: stone is wrong color");
 			return;
 		}
 		if (contains(stone)) {
 			// TODO: throw an exception here
 			System.out.println("warning: stone already added");
 			return;
 		}
 
 		stones.add(stone);
 		stone.group = this;
 
 		if (board.getStone(stone.x + 1, stone.y) == stone.color) {
 			addNeighbourStone(stone.x + 1, stone.y, board);
 		}
 		if (board.getStone(stone.x - 1, stone.y) == stone.color) {
 			addNeighbourStone(stone.x - 1, stone.y, board);
 		}
 		if (board.getStone(stone.x, stone.y + 1) == stone.color) {
 			addNeighbourStone(stone.x, stone.y + 1, board);
 		}
 		if (board.getStone(stone.x, stone.y - 1) == stone.color) {
 			addNeighbourStone(stone.x, stone.y - 1, board);
 		}
 	}
 
 	private void addNeighbourStone(int x, int y, BoardData board) {
 		Stone neighbour = new Stone();
 		neighbour.color = color;
 		neighbour.x = x;
 		neighbour.y = y;
 		if (!contains(neighbour)) {
 			createFromStone(neighbour, board);
 		}
 	}
 
 	public int getLibertiesCount(BoardData board) {
 		int retval = 0;
		for (Iterator<Stone> i = stones.iterator(); i.hasNext(); ) {
			Stone stone = i.next();
 			retval += stone.getLibertiesCount(board);
 		}
 		return retval;
 	}
 }
 
