 
 package com.bushidoburrito.go;
 
 import java.util.*;
 
 public class BoardData
 {
 	boolean isBlackTurn;
 
 	private int width, height;
 	private StoneColor[] stoneColor;
 
 	ArrayList<Group> groups;
 
 	public BoardData(int width, int height) {
 		isBlackTurn = true;
 		this.width = width;
 		this.height = height;
 		stoneColor = new StoneColor[width * height];
 		for (int i = 0; i < stoneColor.length; i++) {
 			stoneColor[i] = StoneColor.NONE;
 		}
 
 		groups = new ArrayList<Group>();
 	}
 
 	public int getWidth() { return width; }
 	public int getHeight() { return height; }
 
 	public boolean isEmpty(int x, int y) {
 		return getStone(x, y) == StoneColor.NONE;
 	}
 
 	/** Takes x, y as stone coordinates */
 	public boolean takeMove(int x, int y) {
 		if (!isEmpty(x, y)) {
 			return false;
 		}
 		StoneColor s = isBlackTurn ? StoneColor.BLACK : StoneColor.WHITE;
 		isBlackTurn = !isBlackTurn;
 		setStone(x, y, s);
 		updateGroups();
 		return true;
 	}
 
 	private void updateGroups() {
 		groups.clear();
 		for (int i = 0; i < stoneColor.length; i++) {
 			if (stoneColor[i] != StoneColor.NONE) {
 				int x = i % width;
 				int y = i / width;
 				Stone stone = findStone(x, y);
 
 				if (stone == null) {
 					stone = new Stone();
 					stone.color = stoneColor[i];
 					stone.x = x;
 					stone.y = y;
 
 					Group group = new Group();
 					group.createFromStone(stone, this);
 					if (group.getLibertiesCount(this) == 0) {
 						clearStones(group);
 					} else {
 						groups.add(group);
 					}
 				}
 			}
 		}
 
 	}
 
 	private void clearStones(Group group) {
		for (Iterator i = group.stones.iterator(); i.hasNext(); ) {
			Stone stone = (Stone) i.next();
 			stone.color = StoneColor.NONE;
 			stoneColor[stone.y * width + stone.x] = StoneColor.NONE;
 		}
 	}
 
 	private Stone findStone(int x, int y) {
 		if (isEmpty(x, y)) {
 			return null;
 		}
		for (Iterator i = groups.iterator(); i.hasNext(); ) {
			Group group = (Group) i.next();
 		}
 		return null;
 	}
 
 	public void setStone(int x, int y, StoneColor s) {
 		if (x < 0 || y < 0 || x > width || y > height) { return; }
 		stoneColor[y * width + x] = s;
 	}
 
 	public StoneColor getStone(int x, int y) {
 		if (x < 0 || y < 0 || x > width || y > height) {
 			return StoneColor.NONE;
 		}
 		return stoneColor[y * width + x];
 	}
 }
 
