 import java.util.HashSet;
 import java.util.Set;
 
 
 public class Deadlock {
 //	public static void main(String args[])
 //	{
 //		State s = null;
 //		for(int i = 1; i < 121;i++){
 //			s = StateLoader.LoadState(i);
 //			//System.err.println("SLC MAP # "+i);
 //			//System.err.println("rows: " + s.rows + " columns: " + s.columns);
 //			//s.Print();
 ////			try {
 ////				// = Deadlock.staticDeadlocks(s);
 ////			} catch (InvalidMapException e) {
 ////				System.out.println("MAP " + i + " is invalid map" );
 ////			}
 //			Deadlock.printUnsafePositions(s);
 //			System.out.println("Map: " + i + " is deadlock " + Deadlock.isDynamicDeadlocks(s));
 //		}
 ////		clearState(slc_state);
 //		s.Print();
 //
 ////		try
 ////		{
 ////		//s.unsafePositions = staticDeadlocks(s);
 ////		} catch (InvalidMapException e)
 ////		{
 ////			System.err.println("INVALID MAP");
 ////		}
 //		printUnsafePositions(s);
 //
 //	}
 	private static void clearState(State s)
 	{
 		s.walls = new HashSet<P>();
 		s.goals = new HashSet<P>();
 		//s.unsafePositions = new HashSet<P>();
 	}
 	// NOT DONE
 	private static boolean isTunnelDeadlock(State s, int direction)
 	{
 		P position = new P(3,5); //TODO SHOULD BE LAST PUSH POSITION
 		P pRight  = new P(position.x, position.y + 1);
 		P pLeft  = new P(position.x, position.y - 1);
 
 		P pUp = new P(position.x - 1, position.y);
 		P pDown = new P(position.x + 1, position.y);
 
 		boolean isTunnel = true;
 		P p1 = position;
 		if(direction == C.UP){
 			while(isTunnel)
 			{
 				if(s.walls.contains(pLeft) && s.walls.contains(pRight))
 				{
 					p1 = new P(p1.x-1, p1.y);
 					pLeft = new P(p1.x, p1.y-1);
 					pRight = new P(p1.x, p1.y+1);
 					if (s.boxes.contains(p1) || s.walls.contains(p1))
 						return true;
 				}
 				else
 					isTunnel = false;
 			}
 
 		}
 		else if(direction == C.DOWN){
 			 isTunnel = true;
 			 while(isTunnel)
 				{
 					if(s.walls.contains(pLeft) && s.walls.contains(pRight))
 					{
 						p1 = new P(p1.x-1, p1.y);
 						pLeft = new P(p1.x, p1.y-1);
 						pRight = new P(p1.x, p1.y+1);
 						if (s.boxes.contains(p1) || s.walls.contains(p1))
 							return true;
 					}
 					else
 						isTunnel = false;
 				}
 		}
 		else if(direction == C.RIGHT){
 			isTunnel = true;
 			 while(isTunnel)
 				{
 					if(s.walls.contains(pUp) && s.walls.contains(pDown))
 					{
 						p1 = new P(p1.x, p1.y+1);
 						pDown = new P(p1.x+1, p1.y);
 						pUp = new P(p1.x-1, p1.y);
 						if (s.boxes.contains(p1) || s.walls.contains(p1))
 							return true;
 					}
 					else
 						isTunnel = false;
 				}
 		}
 		else if(direction == C.LEFT){
 			isTunnel = true;
 			 while(isTunnel)
 				{
 					if(s.walls.contains(pUp) && s.walls.contains(pDown))
 					{
 						p1 = new P(p1.x,p1.y-1);
 						pDown = new P(p1.x+1, p1.y);
 						pUp = new P(p1.x-1, p1.y);
 						if (s.boxes.contains(p1) || s.walls.contains(p1))
 							return true;
 					}
 					else
 						isTunnel = false;
 				}
 		}
 		return false;
 	}
 
 
 	public static void printUnsafePositions(State s)
 	{
 		for(int iy=0; iy<s.rows; iy++)
 		{
 			for(int ix=0; ix<s.columns; ix++)
 			{
 				P xy = new P(ix, iy);
 				boolean w = s.walls.contains(xy);
 				boolean b = s.boxes.contains(xy);
 				boolean g = s.goals.contains(xy);
 				//boolean isUnsafe = s.unsafePositions.contains(xy);
 				boolean p = xy.x == s.player.x && xy.y == s.player.y;
 				//if(isUnsafe)
 					System.err.print('X');
 				//else
 				{
 					if(w && !b && !g && !p)
 						System.err.print(C.wall);
 					else if(!w && b && !g && !p)
 						System.err.print(C.box);
 					else if(!w && !b && g && !p)
 						System.err.print(C.goal);
 					else if(!w && !b && !g && p)
 						System.err.print(C.player);
 					else if(!w && !b && !g && !p)
 						System.err.print(C.empty);
 					else if(!w && b && g && !p)
 						System.err.print(C.boxOnGoal);
 					else if(!w && !b && g && p)
 						System.err.print(C.playerOnGoal);
 					else
 						System.err.print("?"); //throw exception?
 				}
 
 			}
 			System.err.println();
 		}
 	}
 
 	//TODO if goal is in corner it doesn't detect invalid maps correctly
 	public static Set<P> staticDeadlocks(State s) throws InvalidMapException
 	{
 		Set<P> unsafePositions = new HashSet<P>();
 		P possibleUnsafe,tmp1,tmp2;
 		// Find corners
 		for (int row = 0; row < s.rows; row++)
 		{
 			for(int col = 0; col < s.columns; col++)
 			{
 				possibleUnsafe = new P(col, row);
 				if (!s.boxes.contains(possibleUnsafe) && !s.walls.contains(possibleUnsafe) && !s.goals.contains(possibleUnsafe))
 				{
 					// Check top left corner type
 					tmp1 = new P(col - 1, row);
 					tmp2 = new P(col, row - 1);
 
 					if (s.walls.contains(tmp1) && s.walls.contains(tmp2))
 						unsafePositions.add(possibleUnsafe);
 
 					// Check for top right corner type
 					tmp1 = new P(col + 1, row);
 					tmp2 = new P(col, row - 1);
 					if (s.walls.contains(tmp1) && s.walls.contains(tmp2))
 						unsafePositions.add(possibleUnsafe);
 
 					// Check bottom left corner type
 					tmp1 = new P(col - 1, row);
 					tmp2 = new P(col, row + 1);
 					if (s.walls.contains(tmp1) && s.walls.contains(tmp2))
 						unsafePositions.add(possibleUnsafe);
 
 					// Check bottom right corner type
 					tmp1 = new P(col + 1, row);
 					tmp2 = new P(col , row + 1);
 					if (s.walls.contains(tmp1) && s.walls.contains(tmp2))
 						unsafePositions.add(possibleUnsafe);
 				}
 
 			}
 		}
 		HashSet<P> tmpSet = new HashSet<P>();
 		HashSet<P> tmpFound = new HashSet<P>();
 		boolean foundSecondCorner = false;
 		int foundBoxes = 0, foundGoals = 0;
 		P tmpP;
 		for (P p : unsafePositions)
 		{
 			// Check right direction for horizontal unsafe states
 			for(int i = p.x + 1; i < s.columns; i++)
 			{
 				tmpP = new P(i, p.y);
 				if (s.boxes.contains(tmpP))
 					foundBoxes++;
 
 				if (s.goals.contains(tmpP))
 					foundGoals++;
 
 				if(s.walls.contains(new P(i,p.y - 1)) && !s.walls.contains(tmpP))
 					tmpSet.add(tmpP);
 				else
 					break;
 
 
 				if (unsafePositions.contains(tmpP))
 				{
 					foundSecondCorner = true;
 					break;
 				}
 
 			}
 			if (foundSecondCorner && foundGoals < foundBoxes)
 				throw new InvalidMapException();
 			else if(foundGoals  == 0 && foundSecondCorner)
 				tmpFound.addAll(tmpSet);
 
 			tmpSet.clear();
 			foundSecondCorner = false;
 			foundBoxes = 0;
 			foundGoals = 0;
 
 			for(int i = p.x + 1; i < s.columns; i++)
 			{
 				tmpP = new P(i, p.y);
 				if (s.boxes.contains(tmpP))
 					foundBoxes++;
 
 				if (s.goals.contains(tmpP))
 					foundGoals++;
 
 				if(s.walls.contains(new P(i,p.y + 1)) && !s.walls.contains(tmpP))
 					tmpSet.add(tmpP);
 				else
 					break;
 
 				if (unsafePositions.contains(tmpP))
 				{
 					foundSecondCorner = true;
 					break;
 				}
 
 			}
 			if (foundSecondCorner && foundGoals < foundBoxes)
 				throw new InvalidMapException();
 			else if(foundGoals  == 0 && foundSecondCorner)
 				tmpFound.addAll(tmpSet);
 
 			tmpSet.clear();
 			foundSecondCorner = false;
 			foundBoxes = 0;
 			foundGoals = 0;
 			// Check down direction for vertical unsafe states
 			for(int i = p.y + 1; i < s.rows; i++)
 			{
 				tmpP = new P(p.x, i);
 
 				if (s.boxes.contains(tmpP))
 					foundBoxes++;
 
 				if (s.goals.contains(tmpP))
 					foundGoals++;
 
 				if(s.walls.contains(new P(p.x - 1, i)) && !s.walls.contains(tmpP))
 					tmpSet.add(tmpP);
 				else
 					break;
 
 				if (unsafePositions.contains(tmpP))
 				{
 					foundSecondCorner = true;
 					break;
 				}
 
 			}
 			if (foundSecondCorner && foundGoals < foundBoxes)
 				throw new InvalidMapException();
 			else if(foundGoals  == 0 && foundSecondCorner)
 				tmpFound.addAll(tmpSet);
 
 			tmpSet.clear();
 			foundSecondCorner = false;
 			foundBoxes = 0;
 			foundGoals = 0;
 
 			for(int i = p.y + 1; i < s.rows; i++)
 			{
 				tmpP = new P(p.x, i);
 				if (s.boxes.contains(tmpP))
 					foundBoxes++;
 
 				if (s.goals.contains(tmpP))
 					foundGoals++;
 
 				if(s.walls.contains(new P(p.x + 1, i)) && !s.walls.contains(tmpP))
 					tmpSet.add(tmpP);
 				else
 					break;
 
 				if (unsafePositions.contains(tmpP))
 				{
 					foundSecondCorner = true;
 					break;
 				}
 
 			}
 			if (foundSecondCorner && foundGoals < foundBoxes)
 				throw new InvalidMapException();
 			else if(foundGoals  == 0 && foundSecondCorner)
 				tmpFound.addAll(tmpSet);
 
 			tmpSet.clear();
 			foundSecondCorner = false;
 			foundBoxes = 0;
 			foundGoals = 0;
 
 
 		}
 		unsafePositions.addAll(tmpFound);
 		return unsafePositions;
 	}
 
 	public static boolean isDynamicDeadlocks(State s)
 	{
 		P position = s.boxMoved;

 		// Top left
 		P p1 = new P(position.x -1, position.y);
 		P p2 = new P(position.x - 1, position.y - 1);
 		P p3 = new P(position.x, position.y - 1);
 		if ((s.boxes.contains(p1) || s.walls.contains(p1)) && (s.boxes.contains(p2) || s.walls.contains(p2)) && (s.boxes.contains(p3) || s.walls.contains(p3)))
 			return true;
 
 		// Top right
 		p1 = new P(position.x, position.y - 1);
 		p2 = new P(position.x + 1, position.y - 1);
 		p3 = new P(position.x + 1 , position.y);
 		if ((s.boxes.contains(p1) || s.walls.contains(p1)) && (s.boxes.contains(p2) || s.walls.contains(p2)) && (s.boxes.contains(p3) || s.walls.contains(p3)))
 			return true;
 
 		// Bottom left
 		p1 = new P(position.x - 1, position.y);
 		p2 = new P(position.x -1, position.y + 1);
 		p3 = new P(position.x , position.y + 1);
 		if ((s.boxes.contains(p1) || s.walls.contains(p1)) && (s.boxes.contains(p2) || s.walls.contains(p2)) && (s.boxes.contains(p3) || s.walls.contains(p3)))
 			return true;
 
 		// Bottom right
 		p1 = new P(position.x + 1, position.y);
 		p2 = new P(position.x + 1, position.y + 1);
 		p3 = new P(position.x , position.y + 1);
 		if ((s.boxes.contains(p1) || s.walls.contains(p1)) && (s.boxes.contains(p2) || s.walls.contains(p2)) && (s.boxes.contains(p3) || s.walls.contains(p3)))
 			return true;
 
 
 		//Deadlock for two s.boxes adjacent to wall
 
 		//All checks for walls over and under
 		//Pushed box is right, walls top
 		p1 = new P(position.x, position.y-1);
 		P w1 =  new P(position.x-1, position.y);
 		P w2 =  new P(position.x-1, position.y-1);
 		if((s.boxes.contains(p1)) && s.walls.contains(w1) && s.walls.contains(w2))
 			return true;
 
 		//Pushed box is left, walls top
 		p1 = new P(position.x, position.y+1);
 		w1 =  new P(position.x-1, position.y);
 		w2 =  new P(position.x-1, position.y+1);
 		if((s.boxes.contains(p1)) && s.walls.contains(w1) && s.walls.contains(w2))
 			return true;
 
 		//Pushed box is left, walls bottom
 		p1 = new P(position.x, position.y+1);
 		w1 =  new P(position.x+1, position.y);
 		w2 =  new P(position.x+1, position.y+1);
 		if((s.boxes.contains(p1)) && s.walls.contains(w1) && s.walls.contains(w2))
 			return true;
 
 		//Pushed box is right, walls bottom
 		p1 = new P(position.x, position.y-1);
 		w1 =  new P(position.x+1, position.y);
 		w2 =  new P(position.x+1, position.y-1);
 		if((s.boxes.contains(p1)) && s.walls.contains(w1) && s.walls.contains(w2))
 			return true;
 
 		//All checks for walls right and left
 		//Pushed box is top, walls right
 		p1 = new P(position.x+1, position.y);
 		w1 =  new P(position.x, position.y+1);
 		w2 =  new P(position.x+1, position.y+1);
 		if((s.boxes.contains(p1)) && s.walls.contains(w1) && s.walls.contains(w2))
 			return true;
 
 		//Pushed box is bottom, walls right
 		p1 = new P(position.x-1, position.y);
 		w1 =  new P(position.x-1, position.y+1);
 		w2 =  new P(position.x, position.y+1);
 		if((s.boxes.contains(p1)) && s.walls.contains(w1) && s.walls.contains(w2))
 			return true;
 
 		//Pushed box is top, walls left
 		p1 = new P(position.x+1, position.y);
 		w1 =  new P(position.x+1, position.y-1);
 		w2 =  new P(position.x, position.y-1);
 		if((s.boxes.contains(p1)) && s.walls.contains(w1) && s.walls.contains(w2))
 			return true;
 
 		//Pushed box is bottom, walls left
 		p1 = new P(position.x-1, position.y);
 		w1 =  new P(position.x-1, position.y-1);
 		w2 =  new P(position.x, position.y-1);
 		if((s.boxes.contains(p1)) && s.walls.contains(w1) && s.walls.contains(w2))
 			return true;
 
 		return false;
 	}
 }
