 package edu.rpi.phil.legup.puzzles.treetent;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Vector;
 import java.lang.ClassLoader;
 
 import javax.swing.ImageIcon;
 
 import edu.rpi.phil.legup.Legup;
 import edu.rpi.phil.legup.BoardState;
 import edu.rpi.phil.legup.CaseRule;
 import edu.rpi.phil.legup.newgui.CaseRuleSelectionHelper;
 import edu.rpi.phil.legup.puzzles.treetent.TreeTent;
 import edu.rpi.phil.legup.puzzles.treetent.ExtraTreeTentLink;
 
 public class CaseLinkTree extends CaseRule
 {
 	static final long serialVersionUID = -2422204259682293361L;
 	public int crshMode(){return CaseRuleSelectionHelper.MODE_TILETYPE;}
 	public Vector<Integer> crshTileType()
 	{
 		Vector<Integer> ret = new Vector<Integer>();
 		ret.add(TreeTent.CELL_TREE);
 		return ret;
 	}
 	public BoardState autoGenerateCases(BoardState cur, Point pointSelected)
 	{
 		for(int c1=0;c1<4;c1++) //4: one for each orthagonal direction
 		{
 			int x = pointSelected.x + ((c1<2) ? ((c1%2 == 0)?-1:1) : 0);
 			int y = pointSelected.y + ((c1<2) ? 0 : ((c1%2 == 0)?-1:1));
 			if(x < 0 || x >= cur.getWidth() || y < 0 || y >= cur.getHeight())continue;
 			BoardState tmp = null;
 			if(cur.getCellContents(x,y) == TreeTent.CELL_UNKNOWN)
 			{
 				tmp = cur.addTransitionFrom();
 				tmp.setCaseSplitJustification(this);
 				tmp.setCellContents(x,y,TreeTent.CELL_TENT);
 				/*for(int c2=0;c2<4;c2++)
 				{
 					if(c1 == c2)continue;
 					int x2 = crsh.pointSelected.x + ((c2<2) ? ((c2%2 == 0)?-1:1) : 0);
 					int y2 = crsh.pointSelected.y + ((c2<2) ? 0 : ((c2%2 == 0)?-1:1));
 					if(x2 < 0 || x2 >= cur.getWidth() || y2 < 0 || y2 >= cur.getHeight())continue;
 					if(cur.getCellContents(x2,y2) != TreeTent.CELL_UNKNOWN)continue;
 					tmp.setCellContents(x2,y2,TreeTent.CELL_GRASS);
 				}*/
 			}
 			else if(cur.getCellContents(x,y) == TreeTent.CELL_TENT)
 			{
 				tmp = cur.addTransitionFrom();
 				tmp.setCaseSplitJustification(this);
 				/*for(int c2=0;c2<4;c2++)
 				{
 					if(c1 == c2)continue;
 					int x2 = crsh.pointSelected.x + ((c2<2) ? ((c2%2 == 0)?-1:1) : 0);
 					int y2 = crsh.pointSelected.y + ((c2<2) ? 0 : ((c2%2 == 0)?-1:1));
 					if(x2 < 0 || x2 >= cur.getWidth() || y2 < 0 || y2 >= cur.getHeight())continue;
 					if(cur.getCellContents(x2,y2) != TreeTent.CELL_UNKNOWN)continue;
 					tmp.setCellContents(x2,y2,TreeTent.CELL_GRASS);
 				}*/
 			}
 			else if(cur.getCellContents(x,y) != TreeTent.CELL_TENT)continue;
 			if(tmp != null)
 			{
 				ExtraTreeTentLink link = new ExtraTreeTentLink(new Point(x,y),pointSelected);
 				tmp.addExtraData(link);
 				tmp.extraDataDelta.add(link);
 				tmp.endTransition();
 			}
 		}
 		
 		return Legup.getCurrentState();
 	}
 	
 	public String getImageName() {return "images/treetent/caseLinkTree.png";}
 	public CaseLinkTree()
 	{
		setName("Possible links from tree");
 		description = "A tree has one linked tent.";//, other adjacents are grass/tree.";
 	}
 	
 	public static boolean pointEquals(Point p1, Point p2)
 	{
 		if(p1 == null)
 		{
 			if(p2 == null)return true;
 			else return false;
 		}
 		else if(p2 == null)return false;
 		return (p1.x==p2.x)&&(p1.y==p2.y);
 	}
 	public static Point findOnlyCommonTile(Vector<BoardState> states, int type)
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
 	
 	public static int calcAdjacentTiles(BoardState b, Point p, int type)
 	{
 		int rv = 0;
 		if((b == null)||(p == null))return -1;
 		for(int dir=0;dir<4;dir++)
 		{
 			int x = p.x;
 			int y = p.y;
 			if(dir<2)x += (dir%2==0)?-1:1;
 			else y += (dir%2==0)?-1:1;
 			if(x < 0 || x >= b.getWidth() || y < 0 || y >= b.getHeight())continue;
 			rv += (b.getCellContents(x,y) == type)?1:0;
 		}
 		return rv;
 	}
 	
 	public String checkCaseRuleRaw(BoardState state)
 	{
 		String rv = null;
 		BoardState parent = state.getSingleParentState(); 
 		if(parent.getTransitionsFrom().size() > 4)
 		{
 			rv = "Only the blank tiles adjacent to a single tree and the\nlinks between those panels are to be modified\nin one step using this rule.";
 		}
 		else
 		{
 			int num_children = parent.getTransitionsFrom().size();
 			Point p = findOnlyCommonTile(parent.getTransitionsFrom(),TreeTent.CELL_TREE);
 			int num_adj_blanks = calcAdjacentTiles(parent,p,TreeTent.CELL_UNKNOWN);
 			int num_preexisting_tents = calcAdjacentTiles(parent,p,TreeTent.CELL_TENT);
 			int num_intended_branches = num_adj_blanks + num_preexisting_tents;
 			if(p == null)
 			{
 				rv = "Exactly one tree should be involved in linking in one\napplication of this rule.";
 			}
 			else if(num_intended_branches != num_children)
 			{
 				rv = "There is not one branch for each blank/tent\n adjacent to the tree.";
 			}
 			if(rv != null)return rv;
 			Vector<Point> tents = new Vector<Point>(); //location of tent in each branch
 			for(BoardState b : parent.getTransitionsFrom())
 			{
 				if(calcAdjacentTiles(b,p,TreeTent.CELL_UNKNOWN) != num_adj_blanks-1)
 				{
 					rv = "Only one tile should be modified per branch,\nwhich is not the case for branch "+(parent.getTransitionsFrom().indexOf(b)+1);
 					break;
 				}
 				/*else if(calcAdjacentTiles(b,p,TreeTent.CELL_TENT) != 1)
 				{
 					rv = "Exactly one tent must be adjacent to the tree linked,\nwhich is not the case for branch "+(parent.getTransitionsFrom().indexOf(b)+1);
 					break;
 				}*/
 				else if(b.extraDataDelta.size() != 1)
 				{
 					rv = "Each branch must contain exactly one link, which is\nnot the case for branch "+(parent.getTransitionsFrom().indexOf(b)+1); 
 					break;
 				}
 				ExtraTreeTentLink link = (ExtraTreeTentLink)b.extraDataDelta.get(0);
 				Point p1 = (pointEquals(p,link.pos1))?link.pos2:link.pos1;
 				if(b.getCellContents(p1.x,p1.y) != TreeTent.CELL_TENT)
 				{
 					rv = "The link must link a tent and a tree, which is\nnot the case for branch "+(parent.getTransitionsFrom().indexOf(b)+1);
 					break;
 				}
 				else
 				{
 					if(tents.contains(p1))
 					{
 						rv = "Branch "+(tents.indexOf(p1)+1)+" is the same as branch "+(parent.getTransitionsFrom().indexOf(b)+1)+".\nNot all cases are covered.";
 						break;
 					}
 					tents.add(p1);
 				}
 				ArrayList<Point> dif = BoardState.getDifferenceLocations(b,parent);
 				if(dif.size() != 1)//num_adj_blanks)
 				{
 					rv = "Only cells adjacent to the tree being linked should be modified,\nwhich is not the case for branch "+(parent.getTransitionsFrom().indexOf(b)+1);
 					break;
 				}
 				int num_tents_added = 0;
 				for(Point p2 : dif)
 				{
 					if(b.getCellContents(p2.x,p2.y) == TreeTent.CELL_TENT)num_tents_added++;
 				}
 				if(num_tents_added > 1)
 				{
 					rv = "Only one tent should be added per branch, which\nis not the case in branch "+(parent.getTransitionsFrom().indexOf(b)+1);
 					break;
 				}
 			}
 		}
 			
 		return rv;
 	}
 }
