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
             
             /* default threshold must change */
             double percentDec = .2;
             int numColors = 6;
             double percentStep = percentDec / numColors;
             for(int i = 0; i < rate.length; i++)
             {
                 int value = rateValueList.get(i).i;
                 int dec = 0;
                 if(savedSack[value] < initialSack[i] * .10)
                 {
                     threshold[value] = savedSack[value];
                     continue;
                 }
                 if(Math.abs((savedSack[value] * percentDec) - Math.ceil(savedSack[value] * percentDec)) < .5)
                     dec = (int)Math.ceil(savedSack[value] * percentDec);
                 else
                     dec = (int) Math.floor(savedSack[value] * percentDec);
                 
                 threshold[value] = savedSack[value] - dec;
                 percentDec-= percentStep;
             }
 
             for(int i = 0; i < rate.length; i++)
             {
                 int temp = 0;
                 if(i == lowest)
                     continue;
                 if(savedSack[i] > threshold[i])
                 {
                     temp = savedSack[i] - threshold[i];
                     give[i]+= temp;
                     giveValue+= rate[i] * give[i];
                 }
             }
             for(;;)
             {
                 if(requestValue < giveValue)
                 {
                     request[lowest]++;
                     requestValue += rate[lowest];
                 }
                 if(requestValue > giveValue)
                 {
                     request[lowest]--;
                     break;
                 }
             }
 
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
