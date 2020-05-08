 package linewars.display;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.ArrayList;
 
 import linewars.gamestate.Lane;
 import linewars.gamestate.Node;
 import linewars.gamestate.Player;
 import linewars.gamestate.Position;
 import linewars.gamestate.Wave;
 
 /**
  * This class handles drawing a Lane between two Nodes. It will draw the Lane in
  * the color of the two Players on the Lane, marking their progress along it.
  * Any "neutral" area in the Lane is drawn white.
  * 
  * @author Ryan Tew
  * 
  */
 public class ColoredEdge
 {
 	private static final double SEGMENT_STEP = 0.01;
 	private Display display;
 	private int numPlayers;
 
 	/**
 	 * Constructs a ColoredEdge.
 	 * 
 	 * @param l
 	 *            The Lane that this ColoredEdge will color.
 	 * @param numPlayers
 	 *            The number of players in the game.
 	 */
 	public ColoredEdge(Display d, int numPlayers)
 	{
 		display = d;
 		this.numPlayers = numPlayers;
 	}
 
 	/**
 	 * Draws this ColoredEdge according to the information in it's Lane.
 	 * 
 	 * @param g
 	 *            The Graphics to draw to.
 	 */
 	public void draw(Graphics g, Lane lane, double scale)
 	{
 		Node[] nodes = lane.getNodes();
 		Wave[] waves = lane.getWaves();
 		
 		sortWaves(waves);
 		
 		Position laneStart = lane.getPosition(0).getPosition();		
 		Position laneEnd = lane.getPosition(1.0).getPosition();				
 
 		// get the playerID and Color for the start node
 		int prevIndex;
 		Color curColor;
 		Player nodeOwner = nodes[0].getOwner();
 		if(nodeOwner != null)
 		{
 			prevIndex = nodeOwner.getPlayerID();
 			curColor = ImageDrawer.getInstance().getPlayerColor(prevIndex, numPlayers);
 			g.setColor(curColor);
 		}
 		else
 		{
 			prevIndex = -1;
 			curColor = Color.white;
 			g.setColor(curColor);			
 		}
 		
 		//initialize the draw positions
 		Position beforePos = nodes[0].getTransformation().getPosition();
 		Position startPos = laneStart;
 		Position endPos = lane.getPosition(SEGMENT_STEP).getPosition();
 		Position afterPos = lane.getPosition(2 * SEGMENT_STEP).getPosition();
 
 		//draw the first segment it needs to start at the node
 		drawSegment(g, lane, beforePos, beforePos, laneStart, endPos, scale);
 		
 		//draw the first segment in the lane
 		drawSegment(g, lane, beforePos, laneStart, endPos, afterPos, scale);
 		
 		int curIndex;
 		double pos = 3 * SEGMENT_STEP;
 		for(Wave wave : waves)
 		{
 			//set the current color
 			curColor = ImageDrawer.getInstance().getPlayerColor(prevIndex, numPlayers);
 			
 			// if this wave belongs to a different player than the previous one
 			// set the current color to white
 			curIndex = wave.getUnits()[0].getOwner().getPlayerID();
 			if(curIndex != prevIndex)
 			{
 				prevIndex = curIndex;
 				curColor = Color.white;
 			}
 
 			//set the graphics color
 			g.setColor(curColor);
 			
 			//draw the edge segment between the previous wave and the current wave
			for(; pos < wave.getPositionToP0(false); pos += SEGMENT_STEP)
 			{
 				//increment the positions
 				beforePos = startPos;
 				startPos = endPos;
 				endPos = afterPos;
 				afterPos = lane.getPosition(pos).getPosition();
 				
 				drawSegment(g, lane, beforePos, startPos, endPos, afterPos, scale);			
 			}
 		}
 
 		nodeOwner = nodes[1].getOwner();
 		if(nodeOwner != null)
 		{
 			curIndex = nodeOwner.getPlayerID();
 			if(curIndex == prevIndex)
 			{
 				curColor = ImageDrawer.getInstance().getPlayerColor(prevIndex, numPlayers);
 				g.setColor(curColor);
 			}
 			else
 			{
 				prevIndex = -1;
 				curColor = Color.white;
 				g.setColor(curColor);			
 			}
 		}
 		else
 		{
 			prevIndex = -1;
 			curColor = Color.white;
 			g.setColor(curColor);			
 		}
 		
 		//draw the edge segment between the last wave and the end node
 		for(; pos < 1; pos += SEGMENT_STEP)
 		{
 			//increment the positions
 			beforePos = startPos;
 			startPos = endPos;
 			endPos = afterPos;
 			afterPos = lane.getPosition(pos).getPosition();
 			
 			drawSegment(g, lane, beforePos, startPos, endPos, afterPos, scale);			
 		}
 		
 		//increment the positions
 		beforePos = startPos;
 		startPos = endPos;
 		endPos = afterPos;
 		afterPos = nodes[1].getTransformation().getPosition();
 
 		//draw the second to last segment
 		drawSegment(g, lane, beforePos, startPos, endPos, laneEnd, scale);
 
 		//draw the last segment in the lane
 		drawSegment(g, lane, startPos, endPos, laneEnd, afterPos, scale);
 		
 		//draw the last segment it needs to end at the node
 		drawSegment(g, lane, endPos, laneEnd, afterPos, afterPos, scale);
 	}
 
 	/**
 	 * Draws a line segment from start to end that to approximate a bezier
 	 * curve.
 	 * 
 	 * @param g
 	 *            The Graphics object to draw the line segment to.
 	 * 
 	 */
 	private void drawSegment(Graphics g, Lane lane, Position before, Position start,
 			Position end, Position after, double scale)
 	{
 		// get the vectors that represents the line segments
 		Position segBefore = before.subtract(start);
 		Position segment = start.subtract(end);
 		Position segAfter = end.subtract(after);
 
 		// get the normalized vectors that are orthagonal to the lane
 		// we will use these to get the bounding points on the segment
 		Position normOrthStart = segBefore.orthogonal().add(segment.orthogonal()).normalize();
 		Position normOrthEnd = segment.orthogonal().add(segAfter.orthogonal()).normalize();
 
 		// generate the points that bound the segment to be drawn
 		Position p1 = display.toScreenCoord(new Position(start.getX() + normOrthStart.getX() * lane.getWidth() / 2,
 														start.getY() + normOrthStart.getY() * lane.getWidth() / 2));
 		Position p2 = display.toScreenCoord(new Position(start.getX() - normOrthStart.getX() * lane.getWidth() / 2,
 														start.getY() - normOrthStart.getY() * lane.getWidth() / 2));
 		Position p3 = display.toScreenCoord(new Position(end.getX() - normOrthEnd.getX() * lane.getWidth() / 2,
 														end.getY() - normOrthEnd.getY() * lane.getWidth() / 2));
 		Position p4 = display.toScreenCoord(new Position(end.getX() + normOrthEnd.getX() * lane.getWidth() / 2,
 														end.getY() + normOrthEnd.getY() * lane.getWidth() / 2));
 		
 		int[] x = {(int)p1.getX(), (int)p2.getX(), (int)p3.getX(), (int)p4.getX()};
 		int[] y = {(int)p1.getY(), (int)p2.getY(), (int)p3.getY(), (int)p4.getY()};
 
 		g.fillPolygon(x, y, 4);
 	}
 	
 	private void sortWaves(Wave[] waves)
 	{
 		ArrayList<Wave>[] bins = new ArrayList[10];
 		for(int c = 1; c < 1 / SEGMENT_STEP; c *= 10)
 		{
 			for(int i = 0; i < 10; ++i)
 			{
 				bins[i] = new ArrayList<Wave>();
 			}
 			
 			//put the waves into bins according to their positions
 			for(int i = 0; i < waves.length; ++i)
 			{
				int index = (int)(waves[i].getPositionToP0(false) / (SEGMENT_STEP * c)) % (c * 10);
 				bins[index].add(waves[i]);
 			}
 			
 			//put the waves back into the array
 			int i = 0;
 			for(int j = 0; j < 10; ++j)
 			{
 				for(int k = 0; k < bins[j].size(); ++k)
 				{
 					waves[i++] = bins[j].get(k);
 				}
 			}
 		}
 	}
 }
