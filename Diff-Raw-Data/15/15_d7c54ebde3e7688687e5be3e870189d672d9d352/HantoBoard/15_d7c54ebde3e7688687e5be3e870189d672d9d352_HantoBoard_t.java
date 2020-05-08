 /**
  * This file was developed for CS4233: Object-Oriented Analysis & Design.
  * The course was taken at Worcester Polytechnic Institute.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package hanto.studentndemarinis.common;
 
 import hanto.common.HantoException;
 import hanto.util.HantoPieceType;
 import hanto.util.HantoPlayerColor;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.Vector;
 
 /**
  * This class represents the hexagonal Hanto Board.  
  * It extends a class of Collection, so it implements
  * all of the nice collection-parsing methods while containing
  * logic for parsing our hex board.  
  * 
  * @author ndemarinis
  * @version Jan 23, 2013
  *
  */
 public class HantoBoard {
 
 	// Maximum number of possible neighbors on a hex grid
 	private final int MAX_NEIGHBORS = 6;
 	
 	private final Map<HexCoordinate, HantoPiece> pieces;
 	
 	public HantoBoard(){
 		pieces = new HashMap<HexCoordinate, HantoPiece>();
 	}
 	
 	/**
 	 * Add a piece to the board
 	 * Note that this method DOES NOT perform any
 	 * error checking to ensure the piece is in a valid position
 	 * @param p The piece to add
 	 * @param c TODO
 	 */
 	public void addPieceAt(HantoPiece p, HexCoordinate c)
 	{
 		p.setCoordinate(c);
 		pieces.put(c, p);
 	}
 	
 	/**
 	 * Find a piece matching a given coordinate on the board
 	 * @param c Coordinate to search on the board
 	 * @return the piece matching that coordinate, null if none exists
 	 */
 	public HantoPiece getPieceAt(HexCoordinate c) 
 	{
 		return pieces.get(c);
 	}
 	
 	
 	/**
 	 * Find neighboring pieces of a specific coordinate on the board
 	 * @param c Coordinate to find neighbors
 	 * @return Collection of neighbors, empty if none
 	 */
 	public Collection<HantoPiece> getNeighborsOf(HexCoordinate c)
 	{
 		final Collection<HantoPiece> res = new Vector<HantoPiece>();
 		HantoPiece p;
 		
 		for(HexCoordinate n : c.getNeighboringCoordinates()) 
 		{
 			if((p = pieces.get(n)) != null) {
 				res.add(p);
 			}
 		}
 		
 		return res;
 	}
 	
 	/**
 	 * Get the empty neighbor coordinates of a specific coordinate
 	 * @param c Coordinate to find empty neighbors
 	 * @return Collection of neighbors, empty if none
 	 */
 	public Collection<HexCoordinate> getEmptyNeighborCoordinatesOf(HexCoordinate c)
 	{
 		final Collection<HexCoordinate> res = new Vector<HexCoordinate>();
 		
 		for(HexCoordinate n : c.getNeighboringCoordinates())
 		{
 			if(pieces.get(n) == null) {
 				res.add(n);
 			}
 		}
 		
 		return res;
 	}
 	
 	/**
 	 * Get pieces with a specific PieceType
 	 * @param t The type for which to search on the board
 	 * @return Collection of matching pieces
 	 */
 	public Collection<HantoPiece> getPiecesOfType(HantoPieceType t)
 	{
 		final Collection<HantoPiece> res = new Vector<HantoPiece>();
 		
 		for(HantoPiece p : pieces.values())
 		{
 			if(p.getType() == t) {
 				res.add(p);
 			}
 		}
 		
 		return res;
 	}
 	
 	/**
 	 * Check if a coordinate is surrounded
 	 * @param c Coordinate to check
 	 * @return true if the specified coordinate is surrounded
 	 */
 	public boolean isSurrounded(HantoPiece c)
 	{
 		return this.getNeighborsOf(c.getCoordinate()).size() 
 				== MAX_NEIGHBORS;
 	}
 	
 	/**
 	 * Check if a particular piece is somewhere on the board
 	 * @param c Color of piece to find
 	 * @param t Type of piece to find
 	 * @return true if at least one piece matching the type 
 	 * and color are on the board
 	 * 
 	 * NOTE:  this name makes sense to me.  I don't understand how the suggestions in
 	 * CodePro's audit rule could make more sense here.  
 	 */
 	public boolean contains(HantoPlayerColor c, HantoPieceType t)
 	{
 		boolean ret = false;
 		
 		for(HantoPiece p : pieces.values())
 		{
 			ret = ret || (p.getColor() == c && p.getType() == t);
 		}
 		
 		return ret;
 	}
 	/**
 	 * Test if the pieces on the board are in a contiguous grouping,
 	 * using BFS.  
 	 * 
 	 * @return true if pieces are in a contiguous grouping, 
 	 * false otherwise.  
 	 */
 	public boolean isBoardContiguous()
 	{
 		final Queue<HantoPiece> q = new LinkedList<HantoPiece>(); // Queue for our BFS
 
 		// We also need something to hold visited nodes, preferably which we can
 		// check in constant time.  After some research, a HashSet fills this requirement.  
 		final Set<HantoPiece> visited = new HashSet<HantoPiece>();
 
 		// Add the starting node.
 		final HantoPiece p = pieces.values().iterator().next();
 		
 		q.add(p);
 		visited.add(p);
 
 		while(!q.isEmpty()) // Run until we find a path or we run out of nodes
 		{
 			HantoPiece c = q.remove();
 
 			for(HantoPiece neighbor : this.getNeighborsOf(c.getCoordinate()))
 			{
 				if(!visited.contains(neighbor)) {
 					visited.add(neighbor);
 					q.add(neighbor);
 				}
 			}
 		}
 
 		// If our board is connected, our list of visited nodes should
 		// contain all of the elements on the board.  
 		return visited.size() == pieces.size();
 	}
 	
 	
 	/**
 	 * Determine if there is enough room for a piece to slide to its destination
 	 * Currently only supports sliding for distances of one
 	 * @param from Source coordinate
 	 * @param to Destination coordinate
 	 * @return true if piece can slide from from to to, false otherwise
	 * @throws HantoException if run with coordinates with distance > 1
 	 */
 	public boolean canSlideTo(HexCoordinate from, HexCoordinate to) throws HantoException
 	{
		final Collection<HexCoordinate> srcNeighbors; 
		final Collection<HexCoordinate> destNeighbors; 
		final Collection<HexCoordinate> commonNeighbors;
 		
 		if(!from.isAdjacentTo(to)) {
 			throw new HantoException("Sliding for distances of more than " +
 					"one space is currently unsupported!");
 		}
 		
 		// Find the empty spaces around the source and destination
 		srcNeighbors = getEmptyNeighborCoordinatesOf(from);
 		destNeighbors = getEmptyNeighborCoordinatesOf(to);
 		
 		commonNeighbors = new Vector<HexCoordinate>();
 		
 		// Take the intersection of the two sets
 		for(HexCoordinate c : srcNeighbors)
 		{
 			if(destNeighbors.contains(c)) {
 				commonNeighbors.add(c);
 			}
 		}
 		
 		// We need at least two open spaces to slide to a destion,
 		// one of those spaces IS the destination.  So, if we have
 		// at least one more common space, we can slide to the dest.  
 		return commonNeighbors.size() >= 1;
 	}
 	
 	/**
 	 * 
 	 * @return true if the board is empty, false otherwise
 	 */
 	public boolean isEmpty()
 	{
 		return pieces.isEmpty();
 	}
 	
 	
 	/**
 	 * @param p Piece at HantoCoordinate to remove from the board
 	 */
 	public void remove(HexCoordinate p)
 	{
 		pieces.remove(p);
 	}
 	
 	/**
 	 * Remove all pieces from the board
 	 */
 	public void reset()
 	{
 		pieces.clear();
 	}
 	
 	
 	/**
 	 * Return a string representing the current state of the board,
 	 * empty string if the board is empty.  
 	 * @return string representing the board
 	 */
 	public String getPrintableBoard() {
 		String ret = "";
 		
 		for(HantoPiece p : pieces.values()) {
 			ret += (p + "\n");
 		}
 		
 		return ret;
 	}
 	
 	
 	// TODO:  There's a warning about not implementing clone()
 	// I'm not sure the right way to do this, so I'm leaving it alone.  
 	// Specifically, I don't know enough about making it synchronized etc.  
 }
