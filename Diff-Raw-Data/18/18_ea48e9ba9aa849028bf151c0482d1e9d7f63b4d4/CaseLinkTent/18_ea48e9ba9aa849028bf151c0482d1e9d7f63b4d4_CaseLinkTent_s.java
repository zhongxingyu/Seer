 package edu.rpi.phil.legup.puzzles.treetent;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 
 import edu.rpi.phil.legup.Legup;
 import edu.rpi.phil.legup.BoardState;
 import edu.rpi.phil.legup.CaseRule;
 import edu.rpi.phil.legup.newgui.CaseRuleSelectionHelper;
 import edu.rpi.phil.legup.puzzles.treetent.TreeTent;
 import edu.rpi.phil.legup.puzzles.treetent.ExtraTreeTentLink;
 
 public class CaseLinkTent extends CaseRule
 {
 	static final long serialVersionUID = 9504L;
 	public int crshMode(){return CaseRuleSelectionHelper.MODE_TILETYPE;}
 	public Vector<Integer> crshTileType()
 	{
 		Vector<Integer> ret = new Vector<Integer>();
 		ret.add(TreeTent.CELL_TENT);
 		return ret;
 	}
 	public BoardState autoGenerateCases(BoardState cur, Point pointSelected)
 	{
 		for(int c1=0;c1<4;c1++) //4: one for each orthagonal direction
 		{
 			int x = pointSelected.x + ((c1<2) ? ((c1%2 == 0)?-1:1) : 0);
 			int y = pointSelected.y + ((c1<2) ? 0 : ((c1%2 == 0)?-1:1));
 			if(x < 0 || x >= cur.getWidth() || y < 0 || y >= cur.getHeight())continue;
 			if(cur.getCellContents(x,y) != TreeTent.CELL_TREE)continue;
 			if(TreeTent.isLinked(cur.getExtraData(),new Point(x,y)))continue;
 			BoardState tmp = cur.addTransitionFrom();
 			tmp.setCaseSplitJustification(this);
 			ExtraTreeTentLink link = new ExtraTreeTentLink(new Point(x,y),pointSelected);
 			tmp.addExtraData(link);
 			tmp.extraDataDelta.add(link);
 			tmp.endTransition();
 		}
 		return Legup.getCurrentState();
 	}
 	
 	public String getImageName() {return "images/treetent/caseLinkTent.png";}
 	public CaseLinkTent()
 	{
		setName("Possible links from tent");
 		description = "A tent can link to exactly one adjacent tree.";
 	}
 	
 	public boolean pointEquals(Point p1, Point p2)
 	{
 		if(p1 == null)
 		{
 			if(p2 == null)return true;
 			else return false;
 		}
 		else if(p2 == null)return false;
 		return (p1.x==p2.x)&&(p1.y==p2.y);
 	}
 	public Point findOnlyCommonTile(Vector<BoardState> states, int type)
 	{
 		Point rv = null;
 		int num_trees = 0;
 		for(int c1=0;c1<states.size();c1++)
 		{
 			//ArrayList<Point> delta = BoardState.getDifferenceLocations(states.get(c1-1),states.get(c1));
 			BoardState s = states.get(c1);
 			for(Object o : s.extraDataDelta)
 			{
 				ExtraTreeTentLink ex = (ExtraTreeTentLink)o;
 				if(s.getCellContents(ex.pos1.x,ex.pos1.y) == type)
 				{
 					if(!pointEquals(rv,ex.pos1))num_trees++;
 					rv = ex.pos1;
 					break;
 				}
 				if(s.getCellContents(ex.pos2.x,ex.pos2.y) == type)
 				{
 					if(!pointEquals(rv,ex.pos2))num_trees++;
 					rv = ex.pos2;
 					break;
 				}
 			}
 		}
 		return (num_trees == 1)?rv:null;
 	}
 	
 	public int calcAdjacentTiles(BoardState b, Point p, int type)
 	{
 		int rv = 0;
 		if((b == null)||(p == null))return 0;
 		for(int dir=0;dir<4;dir++)
 		{
 			int x = p.x;
 			int y = p.y;
 			if(dir == 0)x += 1;
 			if(dir == 1)x -= 1;
 			if(dir == 2)y += 1;
 			if(dir == 3)y -= 1;
 			if(x < 0 || x >= b.getWidth() || y < 0 || y >= b.getHeight())continue;
 			rv += (b.getCellContents(x,y) == type)?1:0;
 		}
 		return rv;
 	}
 	
 	public int calcAdjacentLinkedTiles(BoardState b, Point p)
 	{
 		int rv = 0;
 		if((b == null)||(p == null))return 0;
 		for(int dir=0;dir<4;dir++)
 		{
 			int x = p.x;
 			int y = p.y;
 			if(dir<2)x += (dir%2==0)?-1:1;
 			else y += (dir%2==0)?-1:1;
 			if(x < 0 || x >= b.getWidth() || y < 0 || y >= b.getHeight())continue;
 			Point p2 = new Point(x,y);
 			for(Object o : b.getExtraData())
 			{
 				ExtraTreeTentLink link = (ExtraTreeTentLink)o;
 				rv += (pointEquals(link.pos1,p2))?1:0;
 				rv += (pointEquals(link.pos2,p2))?1:0;
 			}
 		}
 		return rv;
 	}
 	
 	public String checkCaseRuleRaw(BoardState state)
 	{
 		String rv = null;
 		BoardState parent = state.getSingleParentState(); 
 		if(parent.getTransitionsFrom().size() > 4)
 		{
 			rv = "Only the trees adjacent to a single tent should be\nlinked to in one step using this rule.";
 		}
 		else
 		{
 			int num_children = parent.getTransitionsFrom().size();
 			Point p = findOnlyCommonTile(parent.getTransitionsFrom(),TreeTent.CELL_TENT);
 			int num_adj_trees = calcAdjacentTiles(parent,p,TreeTent.CELL_TREE);
 			num_adj_trees -= calcAdjacentLinkedTiles(parent,p);
 			if(p == null)
 			{
 				rv = "Only one tent should be involved in linking in one\napplication of this rule.";
 			}
 			else if(num_adj_trees != num_children)
 			{
 				rv = "There should be one branch for each unlinked tree that\nis adjacent to the chosen tent.";
 			}
 			Vector<Point> trees = new Vector<Point>(); //location of tree in each branch
 			for(BoardState b : parent.getTransitionsFrom())
 			{
 				if(b.extraDataDelta.size() != 1)
 				{
 					rv = "Each branch must contain exactly one link, which is\nnot the case for branch "+(parent.getTransitionsFrom().indexOf(b)+1); 
 					break;
 				}
 				ExtraTreeTentLink link = (ExtraTreeTentLink)b.extraDataDelta.get(0);
 				Point p1 = (pointEquals(p,link.pos1))?link.pos2:link.pos1;
 				if(b.getCellContents(p1.x,p1.y) != TreeTent.CELL_TREE)
 				{
 					rv = "The link must link a tent and a tree, which is\nnot the case for branch "+(parent.getTransitionsFrom().indexOf(b)+1);
 					break;
 				}
 				else
 				{
 					if(trees.contains(p1))
 					{
 						rv = "Branch "+(trees.indexOf(p1)+1)+" is the same as branch "+(parent.getTransitionsFrom().indexOf(b)+1)+".\nNot all cases are covered.";
 						break;
 					}
 					trees.add(p1);
 				}
 				ArrayList<Point> dif = BoardState.getDifferenceLocations(b,parent);
 				if(dif.size() != 0)
 				{
 					rv = "As this is a linking rule, no cells should be modified,\nwhich is not the case for branch "+(parent.getTransitionsFrom().indexOf(b)+1);
 					break;
 				}
 			}
 		}
 			
 		return rv;
 	}
 }
