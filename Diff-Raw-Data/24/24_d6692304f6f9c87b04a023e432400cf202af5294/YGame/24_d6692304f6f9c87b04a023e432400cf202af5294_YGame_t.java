 
 package edu.berkeley.gamesman.game;
 
 import java.util.Vector;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * The game Y
  * 
  * @author dnspies, headcrash, Igor, and Daniel
  */
 public final class YGame extends ConnectGame
 {
     private int totalNumberOfNodes;
 
     private int innerTriangleSegments;
     private int outerRingSegments;
     private int numberOfTriangles; // Total number of triangles (or rows, rings, etc.)
 
     private Vector< char[] > board;
     private int[] nodesInThisTriangle;
     private int transitionTriangleNumber = -1;
 
     private char[] unrolledCharBoard; // The full board (string) representation.
 
     private Node[] neighbors; // Preallocate what will be returned from getNeighbors
 
     private final int HEIGHT = 24;
     private final int WIDTH = 24;
 
     private char ASCIIrepresentation[][];
 
     /**
      * @author headcrash
      */
     private final class Node
     {
         private boolean trueIfInnerMode;
         private int triangle;
         private int index;
 
         /**
          * Default (empty) constructor to be filled in with findNeighbors() call.
          */
         Node()
         {
         }
 
         public boolean isInInnerTriangle()
         {
             return this.trueIfInnerMode;
         }
 
         public int getTriangle()
         {
             return this.triangle;
         }
 
         public int getIndex()
         {
             return this.index;
         }
     }
 
     /**
      * @param conf
      */
     public YGame( Configuration conf )
     {
         super( conf );
 
         this.initializeYGame( conf.getInteger( "gamesman.game.innerTriangleSegments", 2 ),
                               conf.getInteger( "gamesman.game.outerSegments", 4 ) );
     }
 
     /**
      * FOR TESTING PURPOSES ONLY. This is not using a configuration file!
      * 
      * @param innerTriangleSegmentsIn
      * @param outerRingSegmentsIn
      */
     public YGame( int innerTriangleSegmentsIn, int outerRingSegmentsIn )
     {
         super( null );
 
        this.initializeYGame( innerTriangleSegmentsIn, outerRingSegmentsIn );
     }
 
     /**
      * @param conf
      *            The configuration object
      */
     private void initializeYGame( int innerTriangleSegmentsIn, int outerRingSegmentsIn )
     {
         this.innerTriangleSegments = innerTriangleSegmentsIn;
         this.outerRingSegments = outerRingSegmentsIn;
 
         // Allocate and initialize the board, which is an array of character arrays representing the triangles.
 
         if ( ( this.innerTriangleSegments % 3 ) == 2 )
         {
             this.numberOfTriangles = this.outerRingSegments - this.innerTriangleSegments + 1;
         }
         else
         {
             this.numberOfTriangles = this.outerRingSegments - this.innerTriangleSegments + 2;
         }
 
         this.board = new Vector< char[] >( this.numberOfTriangles );
 
         this.nodesInThisTriangle = new int[this.numberOfTriangles];
 
         // System.out.println( this.nodesInThisTriangle.length );
 
         this.totalNumberOfNodes = 0;
 
         int nodes = ( this.innerTriangleSegments % 3 ) * 3;
 
         for ( int i = 0; i < this.numberOfTriangles; i++ )
         {
             // System.out.println( "i: " + nodes );
 
             this.nodesInThisTriangle[i] = nodes;
 
             char[] triangleNodes = new char[nodes];
 
             if ( ( nodes / 3 ) == this.innerTriangleSegments )
             {
                 this.transitionTriangleNumber = i;
             }
 
             board.add( triangleNodes );
 
             if ( this.transitionTriangleNumber == -1 )
             {
                 nodes += 9;
             }
             else
             {
                 nodes += 3;
             }
         }
 
         assert ( this.transitionTriangleNumber >= 0 );
 
         for ( int i = 0; i < this.numberOfTriangles; i++ )
         {
             this.totalNumberOfNodes += this.nodesInThisTriangle[i];
         }
 
         // this.totalNumberOfNodes = 5 * ( this.numberOfTriangles * this.numberOfTriangles ) - ( 7 * this.numberOfTriangles ) + 3;
 
         // Double checking the above calculation.
         // This calculation doesn't appear to be correct for all cases.
 
         // assert ( summationOfNodes == this.totalNumberOfNodes );
 
         // System.out.println( " = " + this.totalNumberOfNodes );
 
         // Allocate the full board (string) representation.
 
         this.unrolledCharBoard = new char[this.totalNumberOfNodes];
 
         // Allocate the array of possible neighbors (6 max)
 
         this.neighbors = new Node[6];
 
         for ( int i = 0; i < 6; i++ )
         {
             this.neighbors[i] = new Node();
         }
 
        // Allocate and initialize a 2-dimensional array to use for plotting ASCII the game board nodes.
 
         ASCIIrepresentation = new char[HEIGHT][];
 
         for ( int y = 0; y < HEIGHT; y++ )
         {
             ASCIIrepresentation[y] = new char[WIDTH];
         }
 
         for ( int y = 0; y < HEIGHT; y++ )
         {
             for ( int x = 0; x < WIDTH; x++ )
             {
                 int xCoord = (int) ( coordsFor4and8board[x + y][0] * ( WIDTH - 1 ) );
                 int yCoord = (int) ( coordsFor4and8board[x + y][1] * ( HEIGHT - 1 ) );
                 ASCIIrepresentation[yCoord][xCoord] = '.';
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * @see edu.berkeley.gamesman.game.ConnectGame#getBoardSize()
      */
     @Override
     public int getBoardSize()
     {
         return ( this.totalNumberOfNodes );
     }
 
     /*
      * (non-Javadoc)
      * @see edu.berkeley.gamesman.game.ConnectGame#getCharArray()
      */
     @Override
     protected char[] getCharArray()
     {
         int charIndex = 0;
 
         for ( int t = 0; t < board.size(); t++ )
         {
             char[] triangle = board.get( t );
 
             for ( int n = 0; n < this.nodesInThisTriangle[t]; n++ )
             {
                 unrolledCharBoard[charIndex++] = triangle[n];
             }
         }
 
         return unrolledCharBoard;
     }
 
     /**
      * TODO: Filter by player (using triangle for board and index for .. well .. index), returned clockwise.
      * 
      * @param trueIfInnerMode
      * @param triangle
      * @param index
      * @param player
      * @return
      */
     public int findNeighbors( boolean trueIfInnerModeIn, int triangleIn, int indexIn, char player )
     {
         int numberOfNeighbors = 0;
 
         // Same-layer neighbors:
 
         this.neighbors[numberOfNeighbors].trueIfInnerMode = trueIfInnerModeIn;
         this.neighbors[numberOfNeighbors].triangle = triangleIn;
         this.neighbors[numberOfNeighbors].index = ( indexIn - 1 ) % ( 3 * this.nodesInThisTriangle[triangleIn] );
 
         numberOfNeighbors++;
 
         this.neighbors[numberOfNeighbors].trueIfInnerMode = trueIfInnerModeIn;
         this.neighbors[numberOfNeighbors].triangle = triangleIn;
         this.neighbors[numberOfNeighbors].index = ( indexIn + 1 ) % ( 3 * this.nodesInThisTriangle[triangleIn] );
 
         numberOfNeighbors++;
 
         // Inner neighbors:
 
         if ( isInnerTriangle( triangleIn ) == false ) // Outer triangle to (outer triangle or transition triangle)
         {
             this.neighbors[numberOfNeighbors].trueIfInnerMode = isInnerTriangle( triangleIn - 1 );
             this.neighbors[numberOfNeighbors].triangle = triangleIn - 1;
             this.neighbors[numberOfNeighbors].index = indexIn - ( indexIn / triangleIn );
 
             numberOfNeighbors++;
 
             if ( isCornerIndex( triangleIn, indexIn ) == false ) // The next inner neighbor only when it is not a
             // corner.
             {
                 this.neighbors[numberOfNeighbors].trueIfInnerMode = isInnerTriangle( triangleIn - 1 );
                 this.neighbors[numberOfNeighbors].triangle = triangleIn - 1;
                 this.neighbors[numberOfNeighbors].index = indexIn - ( indexIn / triangleIn ) - 1;
 
                 numberOfNeighbors++;
             }
         }
         else
         {
             if ( isCornerIndex( triangleIn, indexIn ) == false ) // Triangle corners don't have any inner neighbors.
             {
                 if ( isCornerIndex( triangleIn, indexIn + 1 ) == false ) // After corner index
                 {
                     this.neighbors[numberOfNeighbors].trueIfInnerMode = isInnerTriangle( triangleIn );
                     this.neighbors[numberOfNeighbors].triangle = triangleIn - 1;
                     this.neighbors[numberOfNeighbors].index = 1 - ( 3 * indexIn / triangleIn ) % 3 * ( triangleIn - 3 );
 
                     numberOfNeighbors++;
 
                     this.neighbors[numberOfNeighbors].trueIfInnerMode = isInnerTriangle( triangleIn );
                     this.neighbors[numberOfNeighbors].triangle = triangleIn;
                     this.neighbors[numberOfNeighbors].index = triangleIn - 2 % 3 * triangleIn;
 
                     numberOfNeighbors++;
                 }
                 else
                 {
                     if ( isCornerIndex( triangleIn, indexIn - 1 ) == false ) // Before corner index
                     {
                         this.neighbors[numberOfNeighbors].trueIfInnerMode = isInnerTriangle( triangleIn );
                         this.neighbors[numberOfNeighbors].triangle = triangleIn - 1;
                         this.neighbors[numberOfNeighbors].index =
                                                                   indexIn - 2 - ( 3 * indexIn / triangleIn ) % 3
                                                                           * ( triangleIn - 3 );
 
                         numberOfNeighbors++;
 
                         if ( ( indexIn - 1 ) != 1 ) // Special case: handling the case when triangle = 2
                         {
                             this.neighbors[numberOfNeighbors].trueIfInnerMode = isInnerTriangle( triangleIn );
                             this.neighbors[numberOfNeighbors].triangle = triangleIn;
                             this.neighbors[numberOfNeighbors].index = ( triangleIn + 2 ) % 3 * triangleIn;
 
                             numberOfNeighbors++;
                         }
                     }
                     else
                     {
                         if ( triangleIn == 1 ) // Special case: triangle #1 (with 2 segments) has no inner neighbors.
                         {
 
                         }
                         else
                         {
                             if ( triangleIn == 2 ) // Special case: triangle #2 (with 3 segments) has only the middle point as an
                             // inner neighbor.
                             {
                                 this.neighbors[numberOfNeighbors].trueIfInnerMode = true;
                                 this.neighbors[numberOfNeighbors].triangle = triangleIn - 1;
                                 this.neighbors[numberOfNeighbors].index = 0;
 
                                 numberOfNeighbors++;
                             }
                             else
                             {
                                 this.neighbors[numberOfNeighbors].trueIfInnerMode = isInnerTriangle( triangleIn );
                                 this.neighbors[numberOfNeighbors].triangle = triangleIn - 1;
                                 this.neighbors[numberOfNeighbors].index =
                                                                           indexIn - 2 - ( 3 * indexIn / triangleIn ) % 3
                                                                                   * ( triangleIn - 3 );
 
                                 numberOfNeighbors++;
 
                                 this.neighbors[numberOfNeighbors].trueIfInnerMode = isInnerTriangle( triangleIn );
                                 this.neighbors[numberOfNeighbors].triangle = triangleIn - 1;
                                 this.neighbors[numberOfNeighbors].index =
                                                                           indexIn - 1 - ( 3 * indexIn / triangleIn ) % 3
                                                                                   * ( triangleIn - 3 );
 
                                 numberOfNeighbors++;
                             }
                         }
                     }
                 }
             }
         }
 
         // Outer neighbors:
 
         if ( triangleIn != this.numberOfTriangles ) // Outer nodes have no neighbors.
         {
             if ( isInnerTriangle( triangleIn + 1 ) == false ) // (Outer or transition triangle) to an outer triangle.
             {
                 this.neighbors[numberOfNeighbors].trueIfInnerMode = isInnerTriangle( triangleIn );
                 this.neighbors[numberOfNeighbors].triangle = triangleIn + 1;
                 this.neighbors[numberOfNeighbors].index = indexIn + ( indexIn / triangleIn );
 
                 numberOfNeighbors++;
 
                 if ( isCornerIndex( triangleIn, indexIn ) ) // Corners
                 {
                     this.neighbors[numberOfNeighbors].trueIfInnerMode = isInnerTriangle( triangleIn );
                     this.neighbors[numberOfNeighbors].triangle = triangleIn + 1;
                     this.neighbors[numberOfNeighbors].index = indexIn + ( indexIn / triangleIn ) - 1;
 
                     numberOfNeighbors++;
                 }
                 // Not a corner
                else
                 {
                     this.neighbors[numberOfNeighbors].trueIfInnerMode = isInnerTriangle( triangleIn );
                     this.neighbors[numberOfNeighbors].triangle = triangleIn + 1;
                     this.neighbors[numberOfNeighbors].index = indexIn + ( indexIn / triangleIn ) + 1;
 
                     numberOfNeighbors++;
                 }
             }
            // Inner to inner triangle
             else
             {
                this.neighbors[numberOfNeighbors].trueIfInnerMode = isInnerTriangle( triangleIn ); // Wrong or duplicate?
                 this.neighbors[numberOfNeighbors].triangle = triangleIn - 1;
                 this.neighbors[numberOfNeighbors].index = indexIn + 2 + ( 3 * indexIn / triangleIn ) % 3 * ( triangleIn + 3 );
 
                 numberOfNeighbors++;
 
                this.neighbors[numberOfNeighbors].trueIfInnerMode = isInnerTriangle( triangleIn ); // Wrong or duplicate?
                 this.neighbors[numberOfNeighbors].triangle = triangleIn - 1;
                this.neighbors[numberOfNeighbors].index = indexIn + 1 + ( 3 * indexIn / triangleIn ) % 3 * ( triangleIn + 3 );
 
                 numberOfNeighbors++;
 
                 if ( isCornerIndex( triangleIn, indexIn ) ) // Corners
                 {
                     this.neighbors[numberOfNeighbors].trueIfInnerMode = isInnerTriangle( triangleIn );
                     this.neighbors[numberOfNeighbors].triangle = triangleIn - 1;
                     this.neighbors[numberOfNeighbors].index = indexIn - 1 + ( 3 * indexIn / triangleIn ) % 3 * ( triangleIn + 3 );
 
                     numberOfNeighbors++;
 
                     this.neighbors[numberOfNeighbors].trueIfInnerMode = isInnerTriangle( triangleIn );
                     this.neighbors[numberOfNeighbors].triangle = triangleIn - 1;
                     this.neighbors[numberOfNeighbors].index = indexIn - 2 + ( 3 * indexIn / triangleIn ) % 3 * ( triangleIn + 3 );
 
                     numberOfNeighbors++;
                 }
             }
         }
 
         assert ( numberOfNeighbors >= 3 && numberOfNeighbors <= 6 );
 
         return ( numberOfNeighbors );
     }
 
     /**
      * @param triangle
      * @return
      */
     private boolean isInnerTriangle( int triangle )
     {
         assert ( triangle >= 0 && triangle < this.numberOfTriangles );
 
         return ( triangle <= this.transitionTriangleNumber );
     }
 
     /**
      * @param triangle
      * @param index
      * @return
      */
     private boolean isCornerIndex( int triangle, int index )
     {
         assert ( triangle >= 0 && triangle < this.numberOfTriangles );
 
         int segmentsInThisTriangle = this.nodesInThisTriangle[triangle] / 3;
 
         return ( ( index % segmentsInThisTriangle ) == 0 );
     }
 
     /*
      * (non-Javadoc)
      * @see edu.berkeley.gamesman.game.ConnectGame#isWin(char)
      */
     @Override
     protected boolean isWin( char player )
     {
         assert Util.debug( DebugFacility.GAME, displayState() );
 
         return false;
     }
 
     /*
      * (non-Javadoc)
      * @see edu.berkeley.gamesman.game.ConnectGame#setToCharArray(char[])
      */
     @Override
     protected void setToCharArray( char[] myPieces )
     {
         int charIndex = 0;
 
         for ( int t = 0; t < board.size(); t++ )
         {
             char[] triangle = board.get( t );
 
             for ( int n = 0; n < this.nodesInThisTriangle[t]; n++ )
             {
                 triangle[n] = myPieces[charIndex];
 
                 charIndex++;
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * @see edu.berkeley.gamesman.game.TierGame#displayState()
      */
     @Override
     public String displayState()
     {
         String stateString = new String();
 
         if ( ( this.innerTriangleSegments == 4 ) && ( this.outerRingSegments == 8 ) )
         {
             assert ( coordsFor4and8board.length == this.totalNumberOfNodes );
 
             for ( int y = 0; y < HEIGHT; y++ )
             {
                 stateString.concat( ASCIIrepresentation[y].toString() );
                 stateString.concat( "\n" );
             }
         }
         else
         {
             stateString.concat( "UNABLE TO REPRESENT THIS 2D CONFIGURATION:\n" );
             stateString.concat( this.getCharArray().toString() );
         }
 
         return ( stateString );
     }
 
     /*
      * (non-Javadoc)
      * @see edu.berkeley.gamesman.game.Game#describe()
      */
     @Override
     public String describe()
     {
         return "Y" + this.numberOfTriangles;
     }
 
     /*
      * (non-Javadoc)
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString()
     {
         return displayState();
     }
 
     // Cribbed from Wikipedia: http://www.hexwiki.org/index.php?title=Programming_the_bent_Y_board, see attached PNG for expected
     // node numbers that we have to transform. This should also be generalizable for smaller boards.
 
     private final double[][] coordsFor4and8board =
     {
     { 0.4958, 1.0000 },
     { 0.6053, 0.9478 },
     { 0.7153, 0.8768 },
     { 0.8160, 0.7851 },
     { 0.8988, 0.6748 },
     { 0.9571, 0.5508 },
     { 0.9901, 0.4209 },
     { 1.0000, 0.2935 },
     { 0.9935, 0.1757 },
     { 0.8921, 0.1100 },
     { 0.7739, 0.0532 },
     { 0.6417, 0.0145 },
     { 0.5018, 0 },
     { 0.3617, 0.0129 },
     { 0.2291, 0.0502 },
     { 0.1101, 0.1056 },
     { 0.0080, 0.1702 },
     { 0, 0.2879 },
     { 0.0084, 0.4155 },
     { 0.0397, 0.5457 },
     { 0.0966, 0.6704 },
     { 0.1781, 0.7816 },
     { 0.2777, 0.8744 },
     { 0.3868, 0.9466 },
     { 0.4963, 0.9049 },
     { 0.6060, 0.8516 },
     { 0.7079, 0.7786 },
     { 0.7940, 0.6870 },
     { 0.8581, 0.5806 },
     { 0.8981, 0.4630 },
     { 0.9137, 0.3414 },
     { 0.9080, 0.2227 },
     { 0.8056, 0.1574 },
     { 0.6896, 0.1084 },
     { 0.5649, 0.0820 },
     { 0.4378, 0.0813 },
     { 0.3127, 0.1064 },
     { 0.1961, 0.1541 },
     { 0.0929, 0.2182 },
     { 0.0857, 0.3368 },
     { 0.0998, 0.4586 },
     { 0.1384, 0.5766 },
     { 0.2012, 0.6838 },
     { 0.2862, 0.7763 },
     { 0.3872, 0.8504 },
     { 0.4968, 0.8156 },
     { 0.6011, 0.7608 },
     { 0.6904, 0.6851 },
     { 0.7569, 0.5948 },
     { 0.8053, 0.4944 },
     { 0.8295, 0.3819 },
     { 0.8278, 0.2670 },
     { 0.7267, 0.2070 },
     { 0.6146, 0.1700 },
     { 0.5008, 0.1592 },
     { 0.3869, 0.1687 },
     { 0.2744, 0.2045 },
     { 0.1725, 0.2634 },
     { 0.1694, 0.3782 },
     { 0.1922, 0.4910 },
     { 0.2394, 0.5920 },
     { 0.3048, 0.6830 },
     { 0.3931, 0.7596 },
     { 0.4973, 0.7314 },
     { 0.5908, 0.6719 },
     { 0.6545, 0.5938 },
     { 0.7056, 0.5090 },
     { 0.7445, 0.4169 },
     { 0.7522, 0.3088 },
     { 0.6524, 0.2603 },
     { 0.5509, 0.2458 },
     { 0.4497, 0.2453 },
     { 0.3480, 0.2586 },
     { 0.2477, 0.3061 },
     { 0.2540, 0.4142 },
     { 0.2917, 0.5067 },
     { 0.3418, 0.5921 },
     { 0.4045, 0.6709 },
     { 0.4978, 0.6503 },
     { 0.5525, 0.5820 },
     { 0.6026, 0.5076 },
     { 0.6448, 0.4289 },
     { 0.6795, 0.3491 },
     { 0.5911, 0.3374 },
     { 0.4998, 0.3324 },
     { 0.4084, 0.3364 },
     { 0.3199, 0.3471 },
     { 0.3535, 0.4273 },
     { 0.3947, 0.5065 },
     { 0.4439, 0.5814 },
     { 0.4987, 0.5035 },
     { 0.5479, 0.4218 },
     { 0.4505, 0.4213 } };
 }
