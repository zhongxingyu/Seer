 import java.awt.Color;
 import java.lang.reflect.Array;
 import java.util.*;
 
 public class Board {
 
 	public static boolean iAmDebugging = true;
 	private boolean starting;
 
 	protected Color _state[][];
 
 	private HashSet<Connector> R; 
 	private HashSet<Connector> B;
 	private HashSet<Connector> LR;
 	private HashSet<Connector> LB;
 	private HashSet<Connector> LRB;
 	private HashSet<Connector> F;
 
 	private final Functor[] _rules = {
			new Rule1(),new Rule2(), new Rule3(), new Rule4(), new Rule5()
 	};
 
 
 	// Initialize an empty board with no colored edges.
 	public Board ( ) {
 		starting = true;
 
 		R = new HashSet<Connector>(15);
 		B = new HashSet<Connector>(15);
 		LR = new HashSet<Connector>(15);
 		LB = new HashSet<Connector>(15);
 		LRB = new HashSet<Connector>(15);
 		F = new HashSet<Connector>(15);
 
 		_state = new Color[6][6];
 
 		for(int i = 1; i <= 5; ++i)
 		{
 			Arrays.fill(_state[i], Color.WHITE);
 			for(int j = i + 1; j <= 6; ++j)
 				F.add(new Connector(i,j));
 		}
 	}
 
 	// Add the given connector with the given color to the board.
 	// Unchecked precondition: the given connector is not already chosen 
 	// as RED or BLUE.
 	public void add (Connector cnctr, Color c) 
 	{
 		if (R.contains(cnctr) || B.contains(cnctr))
 			throw new IllegalArgumentException("connector is already in the board");
 
 		//UPDATE R or B SETS
 		if (c.equals(Color.RED))
 			R.add(cnctr);
 		else
 			B.add(cnctr);
 
 		System.out.println("Size before: " + F.size());
 
 		if (F.contains(cnctr))
 			F.remove(cnctr);
 		else if (LR.contains(cnctr))
 			LR.remove(cnctr);
 		else if (LB.contains(cnctr))
 			LB.remove(cnctr);
 		else
 			LRB.remove(cnctr);
 
 		_state[cnctr.endPt1() - 1][cnctr.endPt2() - 1] = c;
 		_state[cnctr.endPt2() - 1][cnctr.endPt1() - 1] = c;
 
 		//UPDATE LR, LB and LRB
 
 		LinkedList<Connector> l = makeList(F.iterator(), LR.iterator(), LB.iterator(), LRB.iterator());
 
 		while (!l.isEmpty())
 		{
 			boolean redT, blueT;
 			Connector check = l.remove();
 			redT = formsTriangle(check,Color.RED);
 			blueT = formsTriangle(check,Color.BLUE);
 
 			if (redT && blueT)
 			{
 				LRB.add(check);
 
 				if (LR.contains(check))
 					LR.remove(check);
 				else if (LB.contains(check))
 					LB.remove(check);
 
 			}
 			else if (redT)
 			{
 				if (!LR.contains(check))
 				{
 					if (LB.contains(check))
 					{
 						LRB.add(check);
 						LB.remove(check);
 					}
 					else
 					{
 						LR.add(check);
 					}
 
 				}
 			}
 			else if (blueT)
 			{
 				if (!LB.contains(check))
 				{
 					if (LR.contains(check))
 					{
 						LRB.add(check);
 						LR.remove(check);
 					}
 					else
 					{
 						LB.add(check);
 					}
 				}
 			}
 
 			if ((redT || blueT) && F.contains(check))
 			{
 				F.remove(check);
 			}
 		}
 		System.out.println("Size after: " + F.size());
 	}
 
 	// Set up an iterator through the connectors of the given color, 
 	// which is either RED, BLUE, or WHITE. 
 	// If the color is WHITE, iterate through the uncolored connectors.
 	// No connector should appear twice in the iteration.  
 	public java.util.Iterator<Connector> connectors (Color c)
 	{
 		if (c.equals(Color.RED))
 			return R.iterator();
 		else if (c.equals(Color.BLUE))
 			return B.iterator();
 		else
 			return F.iterator();		
 	}
 
 	// Set up an iterator through all the 15 connectors.
 	// No connector should appear twice in the iteration.  
 	public java.util.Iterator<Connector> connectors ( )
 	{
 		return new IteratorOfIterators(R.iterator(), B.iterator(), F.iterator());
 	}
 
 	// Return the color of the given connector.
 	// If the connector is colored, its color will be RED or BLUE;
 	// otherwise, its color is WHITE.
 	public Color colorOf (Connector e) {
 		if (R.contains(e))
 			return Color.RED;
 		else if (B.contains(e))
 			return Color.BLUE;
 		else
 			return Color.WHITE;
 	}
 
 	// Unchecked prerequisite: cnctr is an initialized uncolored connector.
 	// Let its endpoints be p1 and p2.
 	// Return true exactly when there is a point p3 such that p1 is adjacent
 	// to p3 and p2 is adjacent to p3 and those connectors have color c.
 	public boolean formsTriangle(Connector cnctr, Color c)
 	{
 		if(cnctr == null)
 			throw new IllegalArgumentException("null Connector");
 		for (int i = 0; i < 6; ++i)
 			if (_state[cnctr.endPt1() - 1][i] == c && _state[cnctr.endPt2() - 1][i] == c)
 				return true;
 		return false;
 	}
 
 	// Choices & Rules part:
 
 	// The computer (playing BLUE) wants a move to make.
 	// The board is assumed to contain an uncolored connector, with no 
 	// monochromatic triangles.
 	// There must be an uncolored connector, since if all 15 connectors are colored,
 	// there must be a monochromatic triangle.
 	// Pick the first uncolored connector that doesn't form a BLUE triangle.
 	// If each uncolored connector, colored BLUE, would form a BLUE triangle,
 	// return any uncolored connector.
 	public Connector choice ( ) {
 		System.out.println("Choice");
 		if (starting)
 		{	
 			starting = false;
 			//Rule 1:
 			Iterator<Connector> iter = F.iterator();
 			return iter.next();
 		}
 		else
 		{
 			Iterator<Connector> iter = F.iterator();
 
 			//Rule 2:
 			if(iter != null && iter.hasNext())
 			{
 				return applyRules(iter);
 			}
 			//Rule 3:
 			else
 			{
 				iter = LR.iterator();
 				if(iter != null && iter.hasNext())
 				{
 					return applyRules(iter);
 				}
 				else
 				{
 					throw new IllegalArgumentException("WE ARE DOOMED");
 				}
 			}
 		}
 	}
 
 	// Apply all the rules on a given set of iterators
 	private Connector applyRules(Iterator<Connector> iter)
 	{
 		List<Connector> result = null;
 		for (Functor rule : _rules)
 		{
 			result = rule.Execute(iter, this);
 			if (result.size() == 1)
 				return result.get(0);
 			iter = result.iterator();
 		}
 		return result.get(0);
 		//throw new IllegalStateException("No move found !");
 	}
 
 	// Returns whether or not a connector is red (painted red or dotted red)
 	private boolean isRed(Connector c)
 	{
 		return R.contains(c) || LR.contains(c);
 	}
 
 	private boolean isTrueRed(Connector c)
 	{
 		return R.contains(c);
 	}
 
 	// Returns whether or not a connector is dotted red
 	private boolean isDottedRed(Connector c)
 	{
 		return LR.contains(c);
 	}
 
 	// Returns whether or not a connector is BLue (painted blue or dotted blue or dotted red-blue
 	private boolean isBlue(Connector c)
 	{
 		return B.contains(c) || LB.contains(c) || LRB.contains(c);
 	}
 
 	private boolean isNeutral(Connector c)
 	{
 		return F.contains(c);
 	}
 
 	// Returns how many triangles it creates
 	private int countTriangles(Connector cnctr, Color c)
 	{
 		int count = 0;
 		if(cnctr == null)
 			throw new IllegalArgumentException("null Connector");
 		for (int i = 0; i < 6; ++i)
 			if (_state[cnctr.endPt1() - 1][i] == c && _state[cnctr.endPt2() - 1][i] == c)
 				++count;
 		return count;
 	}
 
 	private int countHypSafe(Connector c)
 	{
 		int count = 0;
 		for(int i = 1; i <= 6; ++i)
 		{
 			if(c.endPt1() != i && c.endPt2() != i)
 			{
 				Connector c1 = new Connector(c.endPt1(), i);
 				Connector c2 = new Connector(c.endPt2(), i);
 				if ((isRed(c1)^isRed(c2) && (isDottedRed(c1)^isDottedRed(c2)))) 
 				{
 					++count;
 				}
 			}
 		}
 
 		return count;
 	}
 
 	// Returns how many loser triangles it creates
 	private int countLosers(Connector c)
 	{
 		int count = 0;
 		for(int i = 1; i <= 6; ++i)
 		{
 			if(c.endPt1() != i && c.endPt2() != i)
 			{
 				Connector c1 = new Connector(c.endPt1(), i);
 				Connector c2 = new Connector(c.endPt2(), i);
 				if ((isRed(c1)^isRed(c2) && (isNeutral(c1)^isNeutral(c2)))) 
 				{
 					++count;
 				}
 			}
 		}
 
 		return count;
 	}
 
 	// Returns how many loser triangles it creates
 	private int countValidLosers(Connector c)
 	{
 		int count = 0;
 		for(int i = 1; i <= 6; ++i)
 		{
 			if(c.endPt1() != i && c.endPt2() != i)
 			{
 				Connector c1 = new Connector(c.endPt1(), i);
 				Connector c2 = new Connector(c.endPt2(), i);
 				if ((isTrueRed(c1)^isTrueRed(c2) && (isNeutral(c1)^isNeutral(c2)))) 
 				{
 					++count;
 				}
 			}
 		}
 
 		return count;
 	}
 
 	// Returns how many loser triangles it creates
 	private int countMixedT(Connector c)
 	{
 		int count = 0;
 		for(int i = 1; i <= 6; ++i)
 		{
 			if(c.endPt1() != i && c.endPt2() != i)
 			{
 				Connector c1 = new Connector(c.endPt1(), i);
 				Connector c2 = new Connector(c.endPt2(), i);
 				if ( (isRed(c1)^isRed(c2) && (isBlue(c1)^isBlue(c2))) || isBlue(c1) && isBlue(c2)) 
 				{
 					++count;
 				}
 			}
 		}
 
 		return count;
 	}
 
 	// Returns how many loser triangles it creates
 	private int countPartialMixedT(Connector c)
 	{
 		int count = 0;
 		for(int i = 1; i <= 6; ++i)
 		{
 			if(c.endPt1() != i && c.endPt2() != i)
 			{
 				Connector c1 = new Connector(c.endPt1(), i);
 				Connector c2 = new Connector(c.endPt2(), i);
 				if ( (isRed(c1)^isRed(c2) && isNeutral(c1)^isNeutral(c2)))
 				{
 					++count;
 				}
 			}
 		}
 
 		return count;
 	}
 
 	// Return true if the instance variables have correct and internally
 	// consistent values.  Return false otherwise.
 	// Unchecked prerequisites:
 	//	Each connector in the board is properly initialized so that 
 	// 	1 <= myPoint1 < myPoint2 <= 6.
 	public boolean isOK ( ) {
 		// You fill this in.
 		return true;
 	}
 
 	// Get lists from iterators
 	public static <T> LinkedList<T> makeList(Iterator<T>... iter) {
 		LinkedList<T> copy = new LinkedList<T>();
 		for(Iterator<T> it : iter)
 		{
 			while (it.hasNext())
 				copy.add(it.next());
 		}
 
 		return copy;
 	}
 
 	public static class IteratorOfIterators implements Iterator<Connector>
 	{
 		private List<Iterator<Connector>> _iterators;
 		private int _idx;
 
 		public IteratorOfIterators(Iterator<Connector>... iterators)
 		{
 			_iterators = Arrays.asList(iterators);
 			_idx = 0;
 		}
 
 		@Override
 		public boolean hasNext()
 		{
 			int idx = _idx;
 			while (idx < _iterators.size() && _iterators.get(idx).hasNext() == false)
 				idx++;
 			return idx < _iterators.size() && _iterators.get(idx).hasNext();
 		}
 
 		@Override
 		public Connector next()
 		{
 			while (_iterators.get(_idx).hasNext() == false)
 				_idx++;
 			return _iterators.get(_idx).next();
 		}
 
 		@Override
 		public void remove()
 		{
 			// throw new RuntimeException("Method remove is not implemented and should not be used");
 		}
 
 		public void concat(Iterator<Connector> it)
 		{
 			_iterators.add(it);
 		}
 	}	
 
 
 	private interface Functor
 
 	{
 		public List<Connector> Execute(Iterator<Connector> iter, Board board);
 	}
 
 	private class Rule1 implements Functor
 	{
 		@Override
 		public List<Connector> Execute(Iterator<Connector> iter, Board board)
 		{
 			int min = Integer.MAX_VALUE;
 			List<Connector> result = new ArrayList<Connector>();
 			while (iter.hasNext())
 			{
 				Connector check = iter.next();
 				int count = countTriangles(check, Color.BLUE);
 				if (count == min)
 				{
 					result.add(check);
 				}
 				if (count < min)
 				{
 					min = count;
 					result.clear();
 					result.add(check);
 				}
 
 
 			}
 			return result;
 		}
 
 	}
 
 	public class Rule2 implements Functor
 	{
 		@Override
 		public List<Connector> Execute(Iterator<Connector> iter, Board board)
 		{
 			int min = Integer.MAX_VALUE;
 			LinkedList<Connector> result = new LinkedList<Connector>();
 			while (iter.hasNext())
 			{
 				Connector check = iter.next();
 				int count = countLosers(check);
 				if (count == min)
 				{
 					result.add(check);
 				}
 				if (count < min)
 				{
 					min = count;
 					result.clear();
 					result.add(check);
 				}
 
 			}
 
 			return result;
 		}
 	}
 
 	private class Rule3 implements Functor
 	{
 		@Override
 		public List<Connector> Execute(Iterator<Connector> iter, Board board)
 		{
 			int min = Integer.MAX_VALUE;
 			LinkedList<Connector> result = new LinkedList<Connector>();
 			while (iter.hasNext())
 			{
 				Connector check = iter.next();
 				int count = countHypSafe(check);
 				if (count == min)
 				{
 					result.add(check);
 				}
 				if (count < min)
 				{
 					min = count;
 					result.clear();
 					result.add(check);
 				}
 
 			}
 
 			return result;
 		}
 	}
 
 	public class Rule4 implements Functor
 	{
 		@Override
 		public List<Connector> Execute(Iterator<Connector> iter, Board board)
 		{
 			int max = Integer.MIN_VALUE;
 			LinkedList<Connector> result = new LinkedList<Connector>();
 			while (iter.hasNext())
 			{
 				Connector check = iter.next();
 				int count = countMixedT(check);
 				if (count == max)
 				{
 					result.add(check);
 				}
 				if (count > max)
 				{
 					max = count;
 					result.clear();
 					result.add(check);
 				}
 
 			}
 
 			return result;
 		}
 	}
 
 	public class Rule5 implements Functor
 	{
 		@Override
 		public List<Connector> Execute(Iterator<Connector> iter, Board board)
 		{
 			int max = Integer.MIN_VALUE;
 			LinkedList<Connector> result = new LinkedList<Connector>();
 			while (iter.hasNext())
 			{
 				Connector check = iter.next();
 				int count = countPartialMixedT(check);
 				if (count == max)
 				{
 					result.add(check);
 				}
 				if (count > max)
 				{
 					max = count;
 					result.clear();
 					result.add(check);
 				}
 
 			}
 
 			return result;
 		}
 	}
 
 	public class Rule6 implements Functor
 	{
 		@Override
 		public List<Connector> Execute(Iterator<Connector> iter, Board board)
 		{
 			int min = Integer.MAX_VALUE;
 			LinkedList<Connector> result = new LinkedList<Connector>();
 			while (iter.hasNext())
 			{
 				Connector check = iter.next();
 				int count = countValidLosers(check);
 				if (count == min)
 				{
 					result.add(check);
 				}
 				if (count < min)
 				{
 					min = count;
 					result.clear();
 					result.add(check);
 				}
 
 			}
 
 			return result;
 		}
 	}
 }
