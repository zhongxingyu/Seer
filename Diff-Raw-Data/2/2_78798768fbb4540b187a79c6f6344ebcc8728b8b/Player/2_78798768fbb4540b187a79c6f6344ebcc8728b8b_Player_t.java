 package cell.g2;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 import java.util.Vector;
 
 import cell.sim.Player.Direction;
 
 public class Player implements cell.sim.Player 
 {
 	private Random gen = new Random();
 	private int[] savedSack;
         private int[] initialSack;
         private int valueToWin; 
         private boolean checked = false;
 	private static int versions = 0;
 	private int version = ++versions;
 	private Floyd shortest;
 	private Floyd possible_moves;
 	private Trader t;
 	int turn_number = 1;
 	static int board_size=0;
 	private int threshold[] = new int[6];
 //	private int threshold[] = new int[6];
 	private int curr_loc[] = new int[2];
 	public String name() 
 	{ 
 		return "g2" + (version != 1 ? " v" + version : ""); 
 	}
 
 	public Direction move(int[][] board, int[] location, int[] sack, int[][] players, int[][] traders)
 	{
 		//Board[][] contains color of each of the squares in the map
 		//location[] contains our location
 		//sack[] contains number of ball of each color
 		//player[][] contains location of all players at that point of the game when u are moving
 		//ie if you are the last player of the game player[][] will contain the location of the players after they have moved for the turn
 		//traders[][] contains location of all lep when it is our turn to move
 		int next_location[]=new int[2];
 //		if(turn_number == 1)
 //		{
 //			try
 //			{
 //				shortest = new Floyd();
 //				shortest.getShortestPaths(board);
 //				board_size = (board.length+1)/2;
 //			}
 //			catch(Exception e)
 //			{
 //				System.out.println("SHORTEST : "+e);
 //			}
 //		}
 		Direction d;
 		savedSack = copyI(sack);
 		initialSack = copyI(sack);
                 if(!checked)
                 {
                     valueToWin =  initialSack[0] * 6 * 4;
                     checked = true;
                 }
 		shortest = new Floyd();
 		shortest.getShortestPaths(board);
 		shortest.getPossiblePaths(board, sack);
 		threshold = shortest.getThreshold();
 		board_size = (board.length+1)/2;
 		curr_loc = location;
 		t = new Trader(traders);
 		int op_trader = t.getBestTrader(location , shortest);
 		int next_node = getBestPath(location, traders[op_trader][0], traders[op_trader][1]);
 		Print.printStatement("next : "+next_node+"\n");
 		next_location = shortest.getCoordinates(next_node);
 		/*int next_lep = 0;
 		for (int i=0; i<traders.length; ++i) {
 			if (traders[i][0] == next_location[0] && traders[i][1] == next_location[1])
 				next_lep = 1;
 		}*/
 		if (sack[color(next_location, board)] == 0) {
 			for (;;) {
 				Direction dir = randomDirection();
 				int[] new_location = move(location, dir);
 				int color = color(new_location, board);
 				if (color >= 0 && sack[color] != 0) {
 					savedSack[color]--;
 					return dir;
 				}
 			}
 		}
 		Print.printStatement("SRC "+location[0]+"  "+location[1]+"\n");
 		Print.printStatement("DEST "+next_location[0]+"  "+next_location[1]+"\n");
 		turn_number++;
 		d = getDirection(location[0],location[1],next_location[0],next_location[1]);
 		return d;	
 	}
 
 	int getBestPath(int src_location[],int dest_location1,int dest_location2)
 	{
 		//Need to check for availability of colors marbles
 		//Need to add another function to calculate another path in case the shortest path requires a color for which we do not contain the marble
 		int next_node = 0;
 		Vector<Integer> v = shortest.getShortestPossiblePath(src_location[0], src_location[1], dest_location1, dest_location2);
 		Print.printStatement("Vector"+v);
 		if(v.size() == 0)
 			next_node = shortest.getMapping(dest_location1, dest_location2);
 		else
 		{
 			next_node = (Integer)v.elementAt(0);
 		}
 		return next_node;
 	}
 
 	private Direction getDirection(int x1,int y1,int x2,int y2)
 	{
 		Print.printStatement(x1+"   "+y1+"\n"+x2+"  "+y2);
 		if(x1 == x2 && y1+1 == y2)
 			return Direction.E;
 		else if(x1 == x2 && y1-1 == y2)
 			return Direction.W;
 		else if(x1+1 == x2 && y1+1 == y2)
 			return Direction.SE;
 		else if(x1+1 == x2 && y1 == y2)
 			return Direction.S;
 		else if(x1-1 == x2 && y1 == y2)
 			return Direction.N;
 		else //if(x1-1 == x2 && y1-1 == y2)
 			return Direction.NW;
 //		return null;
 	}
 
 	private static int color(int[] location, int[][] board)
 	{
 		int i = location[0];
 		int j = location[1];
 		int dim2_1 = board.length;
 		if (i < 0 || i >= dim2_1 || j < 0 || j >= dim2_1)
 			return -1;
 		return board[i][j];
 	}
 
 	private int[] copyI(int[] a)
 	{
 		int[] b = new int [a.length];
 		for (int i = 0 ; i != a.length ; ++i)
 			b[i] = a[i];
 		return b;
 	}
 	
 	 private class Rate_Pair implements Comparable
      {
          int i, j;
          double delta;
          private Rate_Pair(int i, int j, double delta)
          {
              this.i = i;
              this.j = j;
              this.delta = delta;
          }
          public int compareTo(Object t) 
          {
              if(((Rate_Pair)t).delta == this.delta)
                  return 0;
              else if(this.delta < ((Rate_Pair)t).delta)
                  return 1;
              else
                  return -1;
          }
      }
      private class Rate implements Comparable
      {
          int i;
          double rate;
          private Rate(int i, double rate)
          {
              this.i = i;
              this.rate = rate;
          }
          public int compareTo(Object t) 
          {
              if(((Rate)t).rate == this.rate)
                  return 0;
              else if(this.rate < ((Rate)t).rate)
                  return 1;
              else
                  return -1;
          }
      }
      public void getMappingData(ArrayList<Rate_Pair> deltaListOverall, ArrayList<Rate_Pair> [] deltaListSpecific, 
              ArrayList<Rate> rateValueList, double [] rate)
      {
          ArrayList<Rate_Pair>deltaList = new ArrayList();
          
          for(int i = 0; i < rate.length; i++)
          {
              ArrayList<Rate_Pair> temp = new ArrayList();
              for(int j = 0; j < rate.length; j++)
              {
                  double deltaValue = 0;
                  if(i == j)
                      continue;
                  
                  deltaValue = Math.abs(rate[i] - rate[j]);
                  Rate_Pair x = new Rate_Pair(i,j,deltaValue);
                  deltaListOverall.add(x);
                  temp.add(x);
              }
              deltaListSpecific[i] = temp;
              Collections.sort(deltaListSpecific[i]);
              rateValueList.add(new Rate(i,rate[i]));
          }
          Collections.sort(deltaListOverall); 
          Collections.sort(rateValueList);
          
      }
 	
 	public void trade(double[] rate, int[] request, int[] give)
 	{
             ArrayList<Rate_Pair> deltaListOverall = new ArrayList();
             ArrayList<Rate> rateValueList = new ArrayList();
             ArrayList<Rate_Pair> [] deltaListSpecific = new ArrayList[rate.length];
             getMappingData(deltaListOverall,deltaListSpecific,rateValueList,rate);
             
             double giveValue  = 0;
             double requestValue = 0;
             int lowest = rateValueList.get(rateValueList.size()-1).i;
             int highest = rateValueList.get(0).i;
             
        
             
             /* Check if we can win */
             int ourCount = 0;
             int [] originalThresh = copyI(threshold);
             for(int i =0; i < savedSack.length; i++)
             {
                 ourCount += savedSack[i];
             }
             if(ourCount >= valueToWin)
             {
                 //System.out.println("VALUE TO W :" + valueToWin + "OC: " + ourCount);
                 //System.out.println("CAN WIN!!!!!!!!!!!!!!");
                 for(int i = 0; i < rate.length; i++)
                 {
                     threshold[i] = (valueToWin/6);
                 }
             }
             /* get set of values above threshold */
             ArrayList<Rate> aboveThresh = new ArrayList();
             for(int i = 0; i < rate.length; i++)
             {
                 if(savedSack[i] > threshold[i])
                     aboveThresh.add(new Rate(i,rate[i]));
             }
             Collections.sort(aboveThresh);
 
             /* get set of values below threshold */
             ArrayList<Rate> belowThresh = new ArrayList();
             for(int i = 0; i < rate.length; i++)
             {
                 if(savedSack[i] < threshold[i])
                     belowThresh.add(new Rate(i,rate[i]));
             }
             Collections.sort(belowThresh);
             
             
             /* Attempt to make threshold for all values */
             int i = 0;
             int j = belowThresh.size()-1;
             while(!belowThresh.isEmpty() && !aboveThresh.isEmpty())
             {
                 //System.out.println("1");
                 if(j < 0)
                     j = belowThresh.size()-1;
 
                 int colorAbove =  aboveThresh.get(0).i;
                 int colorBelow = belowThresh.get(j).i;
                 if(savedSack[colorBelow] < threshold[colorBelow])
                 {
                     //System.out.println("2");
                     if(savedSack[colorAbove] > threshold[colorAbove])
                     {
                         give[colorAbove]++;
                         request[colorBelow]++;
                         giveValue += rate[colorAbove];
                         requestValue += rate[colorBelow];
                         savedSack[colorAbove]--;
                     }
                     else
                     {
                         aboveThresh.remove(0);
                         if(aboveThresh.isEmpty())
                             break;
                         colorAbove = aboveThresh.get(0).i;
                     }
                     j--;
                 }
                 else
                 {
                     belowThresh.remove(colorBelow);
                     j = belowThresh.size()-1;
                 }
             }
             
             /* If everyone is at or above threshold, give excess away for lowest 
              * rate marble*/
             while(!aboveThresh.isEmpty())
             {
                 //System.out.println("3");
                 if(aboveThresh.isEmpty())
                     break;
                 int colorAbove = aboveThresh.get(0).i;
                 if(colorAbove == lowest)
                 {
                     aboveThresh.remove(0);
                     continue;
                 }
                 while(savedSack[colorAbove] > threshold[colorAbove])
                 {
                     //System.out.println("4");
                     give[colorAbove]++;
                     if(requestValue < giveValue)
                     {
                         request[lowest]++;
                         requestValue += rate[lowest];
                     }
                     giveValue += rate[colorAbove];
                     savedSack[colorAbove]--; 
                 }
                 aboveThresh.remove(0);
             }
             
             /* remove imbalance by removing one from each marble class until 
              * giveValue >= requestValue
              */
             
             i = 0;
             int emptySacks = 0;
             while(giveValue < requestValue)
             {
                 if(i > rate.length-1)
                     i=0;
                 if(emptySacks > 5)
                 {
                     if(request[i] > 0)
                     {
                         request[i]--;
                         requestValue -= rate[i];
                         emptySacks = 0;
                         i++;
                         continue;
                     }
                 }
 
 //                System.err.println("5 i:" + i + " SACK:" + savedSack[i]);
 //                System.err.println("REQUST VALUE:" + requestValue + "GIVE VALUE: " + giveValue + "!!!!!!!!!!");
                 if(savedSack[i] > 0)
                 {
                     give[i]++;
                     giveValue += rate[i];
                     savedSack[i]--;
                 }
                 else
                     emptySacks++;
                 i++;
             }
             for(i = 0; i < rate.length; i++)
             {
                 if(give[i] > savedSack[i])
                 {
                     give[i]--;
                     giveValue -= rate[i];
                     savedSack[i]++;
                 }
             }
             /* auto fill any more marbles we can get */
             for(;;)
             {
                 if(requestValue == giveValue)
                     break;
                 //System.out.println("6");
                 //System.out.println("REquestValue:" + requestValue + "Give Value:" + giveValue);
                 if(requestValue < giveValue)
                 {
                     request[lowest]++;
                     requestValue += rate[lowest];
                 }
                 if(requestValue > giveValue)
                 {
                     while(requestValue > giveValue)
                     {
                         //System.out.println("7");
                         if(request[lowest] > 1)
                         {
                             request[lowest]--;
                             requestValue -= rate[lowest];
                         }
                         else
                         {
                             for(i = 0; i < rate.length; i++)
                             {
                                 if(request[i] > 0)
                                 {
                                     request[i]--;
                                     requestValue -= rate[i];
                                     break;
                                 }
                             }
                         }
                     }
                     if(requestValue < giveValue)
                         break;
                 }
             }
            //System.out.println("REQUST VALUE:" + requestValue + "GIVE VALUE: " + giveValue + "!!!!!!!!!!");
             threshold = copyI(originalThresh);
 	}
 	private Direction randomDirection()
 	{
 		switch(gen.nextInt(6)) {
 			case 0: return Direction.E;
 			case 1: return Direction.W;
 			case 2: return Direction.SE;
 			case 3: return Direction.S;
 			case 4: return Direction.N;
 			case 5: return Direction.NW;
 			default: return null;
 		}
 	}
 	private static int[] move(int[] location, Player.Direction dir)
 	{
 		int di, dj;
 		int i = location[0];
 		int j = location[1];
 		if (dir == Player.Direction.W) {
 			di = 0;
 			dj = -1;
 		} else if (dir == Player.Direction.E) {
 			di = 0;
 			dj = 1;
 		} else if (dir == Player.Direction.NW) {
 			di = -1;
 			dj = -1;
 		} else if (dir == Player.Direction.N) {
 			di = -1;
 			dj = 0;
 		} else if (dir == Player.Direction.S) {
 			di = 1;
 			dj = 0;
 		} else if (dir == Player.Direction.SE) {
 			di = 1;
 			dj = 1;
 		} else return null;
 		int[] new_location = {i + di, j + dj};
 		return new_location;
 	}
 }
