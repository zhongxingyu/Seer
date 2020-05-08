 package sliceWars.logic;
 
 import java.awt.Polygon;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class BoardImpl implements Board{
 
 	private Map<BoardCell, Set<BoardCell>> linkedBoardCells = new LinkedHashMap<BoardCell, Set<BoardCell>>();
 	
 	public BoardCell createAndAddToBoardCellForPolygon(Polygon polygon, CellAttack cellAttack) {
 		BoardCell cell = new BoardCellImpl(polygon, cellAttack);
 		linkedBoardCells.put(cell, new LinkedHashSet<BoardCell>());
 		return cell;
 	}
 	
 	public void addCell(BoardCellImpl boardCell) {
 		linkedBoardCells.put(boardCell, new LinkedHashSet<BoardCell>());
 	}
 
 	@Override
 	public Collection<BoardCell> getBoardCells(){
 		return Collections.unmodifiableCollection(linkedBoardCells.keySet());
 	}
 	
 	public void link(BoardCell boardCell1,BoardCell boardCell2) {
 		Set<BoardCell> polygon1LinkedCells = linkedBoardCells.get(boardCell1);
 		polygon1LinkedCells.add(boardCell2);
 		Set<BoardCell> polygon2LinkedCells = linkedBoardCells.get(boardCell2);
 		polygon2LinkedCells.add(boardCell1);
 	}
 	
 	public void link(Polygon polygon1, Polygon polygon2) {
 		BoardCell boardCell1 = getForPolygon(polygon1);
 		BoardCell boardCell2 = getForPolygon(polygon2);
 		link(boardCell1, boardCell2);
 	}
 	
 	private BoardCell getForPolygon(Polygon p){
 		Set<BoardCell> keySet = linkedBoardCells.keySet();
 		for (BoardCell boardCell : keySet) {
 			if(boardCell.getPolygon().equals(p)){
 				return boardCell;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public boolean areLinked(BoardCell c1, BoardCell c2) {
 		Set<BoardCell> set = linkedBoardCells.get(c1);
 		if(set == null) return false;
 		return set.contains(c2);
 	}
 
 	public Polygon getPolygonAt(int x, int y) {
 		Set<BoardCell> keySet = linkedBoardCells.keySet();
 		for (BoardCell boardCell : keySet) {
 			if(boardCell.getPolygon().contains(x,y))
 				return boardCell.getPolygon();
 		}
 		return null;
 	}
 
 	@Override
 	public BoardCell getCellAtOrNull(int x, int y) {
 		Set<BoardCell> keySet = linkedBoardCells.keySet();
 		for (BoardCell boardCell : keySet) {
 			if(boardCell.getPolygon().contains(x,y))
 				return boardCell;
 		}
 		return null;
 	}
 
 	@Override
 	public boolean isFilled() {
 		Collection<BoardCell> boardCells = getBoardCells();
 		for (BoardCell boardCell : boardCells) {
 			if(boardCell.getOwner().equals(Player.EMPTY))
 				return false;
 		}
 		return true;
 	}
 
 	@Override
 	public int getCellCount() {
 		return linkedBoardCells.size();
 	}
 
 	@Override
 	public int getBiggestLinkedCellCountForPlayer(Player player) {
 		Collection<BoardCell> boardCells = getBoardCells();
 		int count = 0;
 		for (BoardCell boardCell : boardCells) {
 			if(boardCell.getOwner().equals(player)){
 				int newCount = getLinkedCellCount(boardCell);
 				count = Math.max(count, newCount);
 			}
 		}
 		return count;
 	}
 
 	private int getLinkedCellCount(BoardCell boardCell) {
 		Set<BoardCell> alreadyCounted = new LinkedHashSet<BoardCell>();
 		return getLinkedCellCount(boardCell,alreadyCounted, false, null);
 	}
 	
 	private int getLinkedCellCount(BoardCell boardCell,Set<BoardCell> alreadyCounted, boolean ignoreOwner,BoardCell skipCell) {
 		if(alreadyCounted.contains(boardCell)) return 0;
 		if(boardCell.equals(skipCell)) return 0;
 		alreadyCounted.add(boardCell);
 		Set<BoardCell> linked = linkedBoardCells.get(boardCell);
 		
 		int sum = 1;
 		
 		for (BoardCell boardCellLinked : linked) {
 			int linkedCount = getLinkedCount(boardCell, alreadyCounted, ignoreOwner, skipCell, boardCellLinked);
 			sum+=linkedCount;
 		}
 		return sum;
 	}
 
 	private int getLinkedCount(BoardCell boardCell,
 			Set<BoardCell> alreadyCounted, boolean ignoreOwner,
 			BoardCell skipCell, BoardCell boardCellLinked) {
 		boolean hasSameOwnerSame = boardCellLinked.getOwner().equals(boardCell.getOwner());
		boolean isScenario = boardCell.getOwner().equals(Player.SCENARIO);
		if( hasSameOwnerSame || ignoreOwner){
			
		}
 		if(hasSameOwnerSame || ignoreOwner ){
 			return getLinkedCellCount(boardCellLinked,alreadyCounted, ignoreOwner, skipCell);
 		}
 		return 0;
 	}
 
 	@Override
 	public boolean areaAllCellsFilledByPlayer(Player player) {
 		Collection<BoardCell> boardCells = getBoardCells();
 		int cellCount = 0;
 		for (BoardCell boardCell : boardCells) {
 			if(boardCell.getOwner().equals(player)){
 				cellCount++;
 				if(boardCell.getDiceCount()<Cell.MAX_DICE)
 					return false;
 			}
 		}
 		if(cellCount > 0)
 			return true;
 		return false;
 	}
 
 	@Override
 	public void remove(BoardCell cell) {
 		internalRemove(cell, linkedBoardCells);
 	}
 
 	private void internalRemove(BoardCell cell, Map<BoardCell, Set<BoardCell>> boardCellsMap) {
 		Set<BoardCell> boardCells = boardCellsMap.keySet();
 		for (BoardCell boardCell : boardCells) {
 			Set<BoardCell> linked = boardCellsMap.get(boardCell);
 			linked.remove(cell);
 		}
 		boardCellsMap.remove(cell);
 	}
 
 	@Override
 	public boolean removingCellWillLeaveOrphans(BoardCell cell) {
 		Collection<BoardCell> boardCells = getBoardCells();
		int originalSize = boardCells.size()-1;
 		BoardCell anyCell = getCellDifferentFromThisCellOrNull(cell, boardCells);
 		boolean ignoreOwner = true;
 		int linkedCellCount = getLinkedCellCount(anyCell,new LinkedHashSet<BoardCell>(), ignoreOwner, cell);
 		return linkedCellCount != originalSize;
 	}
 
 	private BoardCell getCellDifferentFromThisCellOrNull(BoardCell cell, Collection<BoardCell> boardCells) {
 		for (BoardCell boardCell : boardCells) {
			if(!boardCell.equals(cell))
 				return boardCell;
 		}
 		return null;
 	}
 
 }
