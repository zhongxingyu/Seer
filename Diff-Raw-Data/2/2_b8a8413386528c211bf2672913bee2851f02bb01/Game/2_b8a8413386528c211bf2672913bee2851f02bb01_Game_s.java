 import static java.util.Arrays.copyOf;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class Game
 {
 	/**
 	 * 4D array tetEnum denotes the shape of all possible rotations of all possible tetriminos.
 	 * tetEnum[tet][rotation][column][0] denotes the lower bound (inclusive); .[1] denotes the upper bound (exclusive).
 	 * This array is hand-coded in the static block. tetEnum[0] is not used.
 	 * The anchor point of a tetrimino is always at the bottom-left corner.
 	 */
 	private static final int[][][][] tetEnum;
 	
 	/**
 	 * TetTypes denotes the total number of tetrimino types. It is generated automatically from tetEnum.
 	 */
 	private static final int tetTypes;
 	private final int boardWid, bufSize;
 	private IO io;
 	
 	static
 	{
 		int[] a={ 0, 1 }, b={ 0, 2 }, c={ 0, 3 }, d={ 1, 2 }, e={ 2, 3 }, f={ 1, 3 };
 		tetTypes=8;
 		tetEnum=new int[8][][][];
 		tetEnum[1]=new int[2][][];
 		tetEnum[1][0]=new int[1][];
 		tetEnum[1][0][0]=new int[]{ 0, 4 };
 		tetEnum[1][1]=new int[][]{ a, a, a, a };
 		tetEnum[2]=new int[1][][];
 		tetEnum[2][0]=new int[][]{ b, b };
 		tetEnum[3]=new int[4][][];
 		tetEnum[3][0]=new int[][]{ c, d };
 		tetEnum[3][1]=new int[][]{ a, b, a };
 		tetEnum[3][2]=new int[][]{ d, c };
 		tetEnum[3][3]=new int[][]{ d, b, d };
 		tetEnum[4]=new int[4][][];
 		tetEnum[4][0]=new int[][]{ c, e };
 		tetEnum[4][1]=new int[][]{ b, a, a };
 		tetEnum[4][2]=new int[][]{ a, c };
 		tetEnum[4][3]=new int[][]{ d, d, b };
 		tetEnum[5]=new int[4][][];
 		tetEnum[5][0]=new int[][]{ e, c };
 		tetEnum[5][1]=new int[][]{ b, d, d };
 		tetEnum[5][2]=new int[][]{ c, a };
 		tetEnum[5][3]=new int[][]{ a, a, b };
 		tetEnum[6]=new int[2][][];
 		tetEnum[6][0]=new int[][]{ f, b };
 		tetEnum[6][1]=new int[][]{ a, b, d };
 		tetEnum[7]=new int[2][][];
 		tetEnum[7][0]=new int[2][];
 		tetEnum[7][0][0]=b;
 		tetEnum[7][0][1]=new int[]{ 1, 3 };
 		tetEnum[7][1]=new int[][]{ d, b, a };
 	}
 	
 	/**
 	 * This initializer returns a new Game instance with given IO handler, board width, and buffer size.
 	 * The given IO handler is closed at the end of the run() method.
 	 * @param io - IO handler
 	 * @param boardWid - board width
 	 * @param bufSize - buffer size
 	 */
 	public Game( IO io, int boardWid, int bufSize )
 	{
 		this.io=io;
 		this.boardWid=boardWid;
 		this.bufSize=bufSize;
 	}
 	
 	/**
 	 * This method runs the game and returns the score when input ends.
 	 * @return score
 	 */
 	public long run()
 	{
 		long score=0;
 		Node head=new Node( new boolean[5][boardWid], new int[boardWid] ), cn; // cn=current hypothetical node
 		int[] buf=new int[tetTypes], a;
 		int i, j, jl, k, kl, bufCursor=buildBuf( buf );
 		SyncMaxHeap<Node> q;
 		while( bufCursor>0 )
 		{
			q=new SyncMaxHeap<>();
 			for( i=1 ; i<tetTypes ; i++ )
 			{
 				if( buf[i]==0 )
 					continue;
 				for( j=0, jl=tetEnum[i].length ; j<jl ; j++ )
 				{
 					for( k=0, kl=boardWid-tetEnum[i][j].length ; k<=kl ; k++ )
 					{
 						cn=head.branch( tetEnum[i][j], k );
 						a=copyOf( buf, tetTypes );
 						a[i]--;
 						cn.buf=a;
 						cn.depth=bufCursor-1;
 						cn.rootTet=new int[]{ i, j, k, cn.eliminate() };
 						cn.mark=Evaluator.mark( cn );
 						cn.root=cn;
 						q.add( cn );
 					}
 				}
 			}
 			head=search( q );
 			//io.write( head + "\n" );
 			a=head.rootTet;
 			buf=head.buf;
 			score+=a[3];
 			i=io.read();
 			if( i==-1 )
 				bufCursor--;
 			else
 				buf[i]++;
 			//io.write( a[0] + " " + a[1] + " " + a[2] + " " + Evaluator.pileHeight( head ) + " " + score +"\n" );
 			io.write( a[0] + " " + a[1] + " " + a[2] + "\n" );
 		}
 		io.close();
 		return score;
 	}
 	
 	/**
 	 * This method loads the tetrimino buffer before the game starts.
 	 * @param buf - the buffer
 	 * @return total number of tetriminoes in the buffer
 	 */
 	private int buildBuf( int[] buf )
 	{
 		int i, j;
 		for( i=0 ; i<bufSize ; i++ )
 		{
 			j=io.read();
 			if( j==-1 )
 				break;
 			buf[j]++;
 		}
 		return i;
 	}
 	
 	/**
 	 * This method returns the best root choice in the queue. Physical nodes and hypothetical nodes are branched here.
 	 * @param q - the priority queue to start with
 	 * @return the best root choice
 	 */
 	private Node search( SyncMaxHeap<Node> q )
 	{
 		if( q.peek().depth==0 )
 			return q.head(); // In case bufSize==1, the search tree is not expanded.
 		Search s=new Search( q );
 		for( int i=0, l=Runtime.getRuntime().availableProcessors() ; i<l ; i++ )
 			new Thread( s ).start();
 		synchronized( q ) // q is used as the thread control lock in a Search instance
 		{
 			try
 			{
 				q.wait();
 			}
 			catch( InterruptedException e )
 			{}
 		}
 		synchronized( s.optimal )
 		{
 			return s.optimal.root;
 		}
 	}
 	
 	private class Search implements Runnable
 	{
 		private final AtomicInteger threadCount=new AtomicInteger();
 		private final long cutOff;
 		private final SyncMaxHeap<Node> q;
 		private volatile Node optimal;
 		private final int[] optimalMon=new int[0];
 		
 		private Search( SyncMaxHeap<Node> q )
 		{
 			this.q=q;
 			optimal=q.peek();
 			cutOff=System.currentTimeMillis()+150;
 		}
 		
 		@Override
 		public void run()
 		{
 			threadCount.incrementAndGet();
 			Node head, cn;
 			int i, j, jl, k, kl;
 			boolean realTet;
 			while( true )
 			{
 				if( System.currentTimeMillis()>cutOff )
 					break;
 				head=q.head();
 				/* Because all possible descendents of a node are expanded until the depth limit or the time limit has been reached,
 				 * the queue length increases monotonously before it reaches the peak, and decreases monotonously afterwards.
 				 * As a result, when the queue length reaches 0, the search is guaranteed to end. */ 
 				if( head==null )
 					break;
 				if( head.depth==0 )
 					continue;
 				for( i=1 ; i<tetTypes ; i++ )
 				{
 					realTet=head.buf[i]>0;
 					for( j=0, jl=tetEnum[i].length ; j<jl ; j++ )
 					{
 						for( k=0, kl=boardWid-tetEnum[i][j].length ; k<=kl ; k++ )
 						{
 							cn=head.branch( tetEnum[i][j], k );
 							cn.buf=copyOf( head.buf, tetTypes );
 							if( realTet )
 								cn.buf[i]--;
 							cn.depth=head.depth-1;
 							cn.eliminate();
 							/* TODO Theoretically, a tetrimino that does not exist in the buffer has a 1/tetTypes probability to come.
 							 * However, in real practice, it seems that such a node is seldomly chosen. */
 							cn.mark=realTet ? Evaluator.mark( cn ) : Evaluator.mark( cn )*tetTypes;
 							synchronized( optimalMon )
 							{
 								if( cn.mark>optimal.mark )
 									optimal=cn;
 							}
 							cn.rootTet=head.rootTet;
 							cn.root=head.root;
 							q.add( cn );
 						}
 					}
 				}
 			}
 			if( threadCount.decrementAndGet()==0 )
 			{
 				// The caller can be notified multiple times, although the first notification is already the end of search.
 				synchronized( q )
 				{
 					q.notify();
 				}
 			}
 		}
 	}
 }
