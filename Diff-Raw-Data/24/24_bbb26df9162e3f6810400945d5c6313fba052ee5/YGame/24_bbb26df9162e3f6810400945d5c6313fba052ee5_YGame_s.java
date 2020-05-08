 package edu.berkeley.gamesman.game;
 
 import java.util.Arrays;
 import java.util.Vector;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * The game Y
  * 
  * @originalAuthor dnspies
  * @author hEADcRASH, Igor, and Daniel
  */
 /**
  * @author headcrash
  * 
  */
 public final class YGame extends ConnectGame 
 {
     private final int totalNumberOfNodes;
 
     private final int innerTriangleSegments;
     private final int outerRingSegments; // The next two
     private final int outerRows;
     private final int numberOfTriangles;// Total number of triangles (or rows, rings, etc.)
 
     private final Node[][] board;
     private final int[] nodesInThisTriangle;
     private final int transitionTriangleNumber;
     private final int[] translateOutArray;
 
     private Vector<Node> neighbors;
     //    private final Node[] neighborPool;
     private final Vector<Node> nodesOnSameTriangle;
     private final Vector<Node> nodesOnInnerTriangle;
     private final Vector<Node> nodesOnOuterTriangle;
 
     private final int HEIGHT = 24;
     private final int WIDTH = 24;
     
 	private static final String yRep12 = "                   J\n\n\n"
 			+ "         R                   K\n" + "                   D\n\n\n"
 			+ "          I        A        E\n\n"
 			+ "   Q                                 L\n"
 			+ "               C       B\n\n"
 			+ "         H                    F\n" + "                   G\n"
 			+ "   P                                 M\n"
 			+ "             O           N\n";
 	private static final String yRep21 = "                 G\n\n\n"
 			+ "         O       A       H\n\n\n"
 			+ "     N       F       B       I\n\n\n"
 			+ "         E       D       C\n\n"
 			+ "M                                 J\n"
 			+ "             L       K\n";
 	private static final String yRep31 = "                             K\n\n"
 			+ "                     V               L\n"
 			+ "                             B\n\n"
 			+ "               U                           M\n"
 			+ "                        J         C\n\n\n"
 			+ "           T       I         A         D       N\n\n\n"
 			+ "              H         G         F        E\n\n"
 			+ "       S                                          O\n"
 			+ "                   R         Q         P\n";
 
     private char ASCIIrepresentation[][];
 
     /**
      * @author headcrash
      */
     public final class Node 
     {
         private final int triangle;
         private final int index;
         private final int totalIndex;
 
         /**
          * @param triangleIn
          * @param indexIn
          */
         public Node(final int triangleIn, final int indexIn, final int totalIndex) 
         {
             this.triangle = triangleIn;
             this.index = indexIn;
             this.totalIndex = totalIndex;
         }
 
         /**
          * @return
          */
         public int getTriangle() 
         {
             return this.triangle;
         }
 
         /**
          * @return
          */
         public int getIndex() 
         {
             return this.index;
         }
 
         /* (non-Javadoc)
          * @see java.lang.Object#toString()
          */
         @Override
         public String toString() 
         {
             return new String(
                     /* "Inner:" + this.this.trueIfInnerMode + ", */"Triangle:"
                     + this.triangle + ", Index:" + this.index);
         }
 
         /**
          * @param theNode
          * @return
          */
         public boolean equals(final Node theNode) 
         {
             return ((this.triangle == theNode.getTriangle()) && (this.index == theNode.getIndex()));
         }
 
         public char getChar(){
         	return mmh.get(totalIndex);
         }
     }
 
     /**
      * Constructor
      * 
      * @param conf
      *            The YGame.job configuration file
      */
     public YGame(final Configuration conf) 
     {
         super(conf);
 
         this.innerTriangleSegments = conf.getInteger(
                 "gamesman.game.centerRows", 3) - 1;
 
         this.outerRows = conf.getInteger("gamesman.game.outerRows", 2);
 
         this.outerRingSegments = this.outerRows + this.innerTriangleSegments;
 
         // Allocate and initialize the board, which is an array of character arrays representing the triangles.
 
 		this.numberOfTriangles = this.innerTriangleSegments / 3 + 1
 				+ this.outerRows;
 
         assert Util.debug(DebugFacility.GAME, "numberOfTriangles: "
                 + this.numberOfTriangles);
 
         this.board = new Node[this.numberOfTriangles][];
 
         this.nodesInThisTriangle = new int[this.numberOfTriangles];
 
         int transitionTriangleNumber = -1;
 
         int nodes = (this.innerTriangleSegments % 3) * 3;
 
         int totalIndex = 0;
 
         for (int i = 0; i < this.numberOfTriangles; i++) 
         {
             assert Util.debug(DebugFacility.GAME, "nodesInThisTriangle[" + i
                     + "]: " + nodes);
 
             Node[] triangleNodes;
 
             if (nodes == 0) // 3 segment inner triangle has a single middle node
 
             {
                 triangleNodes = new Node[1];
                 this.nodesInThisTriangle[i] = 1;
             }
             else 
             {
                 triangleNodes = new Node[nodes];
                 this.nodesInThisTriangle[i] = nodes;
             }
 
             if ((nodes / 3) == this.innerTriangleSegments) 
             {
                 transitionTriangleNumber = i;
             }
 
 			for (int index = 0; index < triangleNodes.length; index++) {
 				triangleNodes[index] = new Node(i, index, totalIndex++);
 			}
 
             this.board[i] = triangleNodes;
 
             if (transitionTriangleNumber == -1) 
             {
                 nodes += 9;
             }
             else 
             {
                 nodes += 3;
             }
         }
 
         this.transitionTriangleNumber = transitionTriangleNumber;
 
         assert (this.transitionTriangleNumber >= 0);
 
         this.totalNumberOfNodes = (this.innerTriangleSegments + 1)
         * (this.innerTriangleSegments + 2) / 2
         + (this.innerTriangleSegments * 2 + this.outerRows + 1) * this.outerRows / 2
         * 3;
 
         assert Util.debug(DebugFacility.GAME, "totalNumberOfNodes: "
                 + this.totalNumberOfNodes);
 
         assert Util.debug(DebugFacility.GAME, "`->calculated: "
                 + (5 * (this.numberOfTriangles * this.numberOfTriangles)
                         - (7 * this.numberOfTriangles) + 3));
 
         // Preallocate the 3 types of neighbor vectors
 
         this.nodesOnSameTriangle = new Vector<Node>(2);
         this.nodesOnInnerTriangle = new Vector<Node>(3);
         this.nodesOnOuterTriangle = new Vector<Node>(3);
 
         // .. and the pool of nodes used in the neighbor list:
 
         //        this.neighborPool = new Node[6];
 
         //        for (int i = 0; i < 6; i++) 
         //        {
         //            this.neighborPool[i] = new Node();
         //        }
 
         // ..and the neighbor vector ultimately returned from getNeighbors/clockwiser
 
         this.neighbors = new Vector<Node>(6);
 
         this.fillBoardWithPlayer(' ');
 
         // Allocate and initialize a 2-dimensional array to use for plotting ASCII the game board nodes.
 
         if ((this.innerTriangleSegments == 4) && (this.outerRingSegments == 8)) 
         {
             this.ASCIIrepresentation = new char[this.HEIGHT][this.WIDTH];
 
             for (int y = 0; y < this.HEIGHT; y++) 
             {
                 this.ASCIIrepresentation[y] = new char[this.WIDTH];
             }
 
             for (int i = 0; i < 93; i++) 
             {
                 final int xCoord = (int) (this.coordsFor4and8board[i][0] * (this.WIDTH - 1));
                 final int yCoord = (int) (this.coordsFor4and8board[i][1] * (this.HEIGHT - 1));
                 this.ASCIIrepresentation[yCoord][xCoord] = '.';
             }
         }
         translateOutArray = outArray();
         assert Util.debug(DebugFacility.GAME, this.displayState());
     }
 
 	public int[] outArray() {
 		int[] nextNode = new int[numberOfTriangles];
 		int[] outArray = new int[totalNumberOfNodes];
 		int curIndex = 0;
 		for (int row = 0; row <= this.innerTriangleSegments; row++) {
 			curIndex = inHelper(transitionTriangleNumber, outArray, curIndex,
 					nextNode);
 		}
 		for (int triangle = numberOfTriangles - 1; triangle > transitionTriangleNumber; triangle--) {
 			for (int i = 0; i < nodesInThisTriangle[triangle]; i++) {
 				outArray[getIndex(triangle, i)] = curIndex++;
 			}
 		}
 		return outArray;
 	}
 
 	private int getIndex(int triangle, int index) {
 		return board[triangle][index].totalIndex;
 	}
 
 	private int inHelper(final int triangle, final int[] outArray,
 			int curIndex, final int[] nextNode) {
 		if (nextNode[triangle] == -1) {
 			throw new Error("Recursion should have ended already");
 		} else if (nextNode[triangle] == 0) {
 			outArray[getIndex(triangle, 0)] = curIndex++;
 			nextNode[triangle] = 1;
 		} else {
 			int otherSide = nodesInThisTriangle[triangle] - nextNode[triangle];
 			if (otherSide - nextNode[triangle] > nextNode[triangle]) {
 				outArray[getIndex(triangle, otherSide)] = curIndex++;
 				if (nextNode[triangle] > 1)
 					curIndex = inHelper(triangle - 1, outArray, curIndex,
 							nextNode);
 				outArray[getIndex(triangle, nextNode[triangle])] = curIndex++;
 				nextNode[triangle]++;
 			} else if (otherSide - nextNode[triangle] == nextNode[triangle]) {
 				for (int i = otherSide; i >= nextNode[triangle]; i--)
 					outArray[getIndex(triangle, i)] = curIndex++;
 				nextNode[triangle] = -1;
 			} else {
 				throw new Error("Recursion should have ended already");
 			}
 		}
 		return curIndex;
 	}
 
     /**
      * @param player
      */
     public void fillBoardWithPlayer(final char player) 
     {
     	char[] board = new char[totalNumberOfNodes];
     	Arrays.fill(board, player);
     	mmh.setNumsAndHash(board);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see edu.berkeley.gamesman.game.ConnectGame#getBoardSize()
      */
     @Override
     public int getBoardSize() 
     {
         return (this.totalNumberOfNodes);
     }
 
     /**
      * TODO: Return neighbors in clockwise order
      * 
      * @param node
      * @param player
      * @return
      * @see edu.berkeley.gamesman.game.YGame#getNeighbors(int, int, char)
      */
     public Vector<Node> getNeighbors(final Node node, final char player) 
     {
         return (this.getNeighbors(node.triangle, node.index, player));
     }
 
     /**
      * @param triangleIn
      * @param indexIn
      * @param player
      * @return A filtered vector (by player) with neighbors of this
      *         triangle,index
      */
     public Vector<Node> getNeighbors(final int triangleIn, final int indexIn, final char player)
     {
         int triangle, index;
         this.nodesOnSameTriangle.clear();
         this.nodesOnInnerTriangle.clear();
         this.nodesOnOuterTriangle.clear();
         assert Util.debug(DebugFacility.GAME, "getNeighbors of triangle:" + triangleIn + ", index:" + indexIn + ", player:" + player);
         final int segments = (this.nodesInThisTriangle[triangleIn] / 3);
         if (segments == 0) /* Special case for point in the center */
         {
             this.neighbors.clear();
             triangle = 1;
             index = 1;
             this.neighbors.add(board[triangle][index]);
             triangle = 1;
             index = 2;
             this.neighbors.add(board[triangle][index]);
             triangle = 1;
             index = 4;
             this.neighbors.add(board[triangle][index]);
             triangle = 1;
             index = 5;
             this.neighbors.add(board[triangle][index]);
             triangle = 1;
             index = 7;
             this.neighbors.add(board[triangle][index]);
             triangle = 1;
             index = 8;
             this.neighbors.add(board[triangle][index]);
         }
         else
         {
             /* Same-layer neighbor (left): *1* */
             triangle = triangleIn;
             index = Util.nonNegativeModulo( (indexIn + 1), (this.nodesInThisTriangle[triangleIn]));
             this.nodesOnSameTriangle.add(board[triangle][index]);
 
             /* Same layer neighbor (right) *2* */
             triangle = triangleIn;
             index = Util.nonNegativeModulo( (indexIn - 1), this.nodesInThisTriangle[triangleIn]);
             this.nodesOnSameTriangle.add(board[triangle][index]);
 
             /* Inner neighbors: */
             if (this.isInnerTriangle(triangleIn) == false)/* Outer triangle to (outer triangle or transition triangle) */
             { /* 3 */
                 triangle = triangleIn - 1;
                 index = Util .nonNegativeModulo(indexIn - (indexIn / segments), this.nodesInThisTriangle[triangleIn - 1]);
                 this.nodesOnInnerTriangle.add(board[triangle][index]);
                 if (this.isCornerIndex(triangleIn, indexIn) == false)/* The next inner neighbor only when it is not a corner. *//* 4 */
                 {
                     triangle = triangleIn - 1;
                     index = indexIn - (indexIn / segments) - 1;
                     this.nodesOnInnerTriangle.add(board[triangle][index]);
                 }
             }
             else
             {
                 /* if (isInnerTriangle(triangleIn) == false) */{
                     if ((segments < 2) || (this.isCornerIndex(triangleIn, indexIn) == true))
                     { /* There aren't any inner neighbors for corners or single segment triangles. */
                     }
                     else
                     {
                         if (this.isCornerIndex(triangleIn, indexIn - 1) == true)/* After corner index *//* 6 */
                         { /* 14 */
                             triangle = triangleIn;
                             index = Util .nonNegativeModulo(indexIn - 2, this.nodesInThisTriangle[triangleIn]);
                             this.nodesOnSameTriangle.add(board[triangle][index]);
                             if (segments > 3)
                             {
                                 triangle = triangleIn - 1;
                                 /* 15 */ index = Util .nonNegativeModulo( indexIn - 1 - (3 * indexIn / segments), this.nodesInThisTriangle[triangleIn - 1]);
                                 this.nodesOnInnerTriangle.add(board[triangle][index]);
                             }
                         }
                         if (this.isCornerIndex(triangleIn, indexIn + 1) == true)/* Before corner index *//* 7 */
                         { /* 16 */
                             triangle = triangleIn;
                             index = Util .nonNegativeModulo(indexIn + 2, this.nodesInThisTriangle[triangleIn]);
                             this.nodesOnSameTriangle.add(board[triangle][index]);
                             if (segments > 3)
                             {
                                 triangle = triangleIn - 1;
                                 /* 7 */index = Util.nonNegativeModulo(indexIn - 3 - (3 * indexIn / segments),
                                         this.nodesInThisTriangle[triangleIn - 1]);
                                 this.nodesOnInnerTriangle.add(board[triangle][index]);
                             }
                         }
                         if (segments == 2)
                         { /* There are no inner neighbors */
                         }
                         else
                         {
                             if (segments == 3)
                             {
                                 triangle = triangleIn - 1;
                                 index = 0;
                                 this.nodesOnInnerTriangle.add(board[triangle][index]);
                             }
                             else
                             {
                                 if ((this.isCornerIndex(triangleIn, indexIn + 1) == false) /* Not a corner or before or after a corner. */
                                         && (this.isCornerIndex(triangleIn, indexIn - 1) == false))
                                 { /* 21 */
                                     triangle = triangleIn - 1;
                                     // if (this.nodesInThisTriangle[triangleIn - 1] > 0)
                                     // {
                                     index = Util.nonNegativeModulo(indexIn - 0 - (3 * indexIn / segments),
                                             this.nodesInThisTriangle[triangleIn - 1]);
                                     // }
                                     this.nodesOnInnerTriangle.add(board[triangle][index]);
 
                                     /* 20 */
                                     triangle = triangleIn - 1;
                                     index = Util .nonNegativeModulo(indexIn - 1 - (3 * indexIn / segments), this.nodesInThisTriangle[triangleIn - 1]);
                                     this.nodesOnInnerTriangle.add(board[triangle][index]);
                                 }
                             }
                         }
                     }
                 }
             }
 
             /* Outer neighbors: */
             if (this.isOutermostRow(triangleIn) == false)/* Outer nodes have no neighbors. */
             {
                 if (this.isInnerTriangle(triangleIn + 1) == false)/* (Outer or transition triangle) to an outer triangle. *//* 8 */
                 { /* 9 */
                     triangle = triangleIn + 1;
                     index = Util .nonNegativeModulo(indexIn + (indexIn / segments), this.nodesInThisTriangle[triangleIn + 1]);
                     this.nodesOnOuterTriangle.add(board[triangle][index]);
                     if (this.isCornerIndex(triangleIn, indexIn)) /* Corners *//* 10 */
                     {
                         triangle = triangleIn + 1;
                         index = Util .nonNegativeModulo(indexIn + (indexIn / segments) - 1, this.nodesInThisTriangle[triangleIn + 1]);
                         this.nodesOnOuterTriangle.add(board[triangle][index]); /* 19 */
                         triangle = triangleIn + 1;
                         index = Util .nonNegativeModulo(indexIn + (indexIn / segments) + 1, this.nodesInThisTriangle[triangleIn + 1]);
                         this.nodesOnOuterTriangle.add(board[triangle][index]);
                     } /* Not a corner */
                     else
                     { /* 11 */
                         triangle = triangleIn + 1;
                         index = Util .nonNegativeModulo(indexIn + (indexIn / segments) + 1, this.nodesInThisTriangle[triangleIn + 1]);
                         this.nodesOnOuterTriangle.add(board[triangle][index]);
                     }
                 } /* Inner to inner triangle */
                 else
                 { /* 17 */
                     triangle = triangleIn + 1;
                     index = Util .nonNegativeModulo(indexIn + 2 + (3 * indexIn / segments), this.nodesInThisTriangle[triangleIn + 1]);
                     this.nodesOnOuterTriangle.add(board[triangle][index]); /* 18 */
                     triangle = triangleIn + 1;
                     index = Util .nonNegativeModulo(indexIn + 1 + (3 * indexIn / segments), this.nodesInThisTriangle[triangleIn + 1]);
                     this.nodesOnOuterTriangle.add(board[triangle][index]);
                     if (this.isCornerIndex(triangleIn, indexIn)) /* Corners */
                     { /* 13 */
                         triangle = triangleIn + 1;
                         index = Util .nonNegativeModulo(indexIn - 1 + (3 * indexIn / segments), this.nodesInThisTriangle[triangleIn + 1]);
                         this.nodesOnOuterTriangle.add(board[triangle][index]); /* 12 */
                         triangle = triangleIn + 1;
                         index = Util .nonNegativeModulo(indexIn - 2 + (3 * indexIn / segments), this.nodesInThisTriangle[triangleIn + 1]);
                         this.nodesOnOuterTriangle.add(board[triangle][index]);
                     }
                 }
             }
 
             this.neighbors = this.clockwiser(triangleIn, indexIn);
         }
 
         /* Cull out only player nodes */
 
         int size = this.neighbors.size();
 
         for (int i = size - 1; i >= 0; --i)
         {
             if (this.getPlayerAt(this.neighbors.get(i)) != player)
             {
                 this.neighbors.remove(i);
             }
         }
 
         return (this.neighbors);
     }
 
     /**
      * @param startingNode
      * @param nodesOnSameTriangle
      * @param nodesOnInnerTriangle
      * @param nodesOnOuterTriangle
      * @return
      */
     public Vector<Node> clockwiser(final Node startingNode) 
     {
         return (this.clockwiser(startingNode.triangle, startingNode.index));
     }
 
     /**
      * @param triangleIn
      * @param indexIn
      * @param nodesOnSameTriangle
      * @param nodesOnInnerTriangle
      * @param nodesOnOuterTriangle
      * @return
      */
     public Vector<Node> clockwiser(final int triangleIn, final int indexIn) 
     {
         assert ((this.nodesOnSameTriangle.size() == 3) || (this.nodesOnSameTriangle.size() == 2) || (this.nodesOnSameTriangle
                 .size() == 0));
         assert ((this.nodesOnInnerTriangle.size() >= 0) && (this.nodesOnInnerTriangle.size() <= 3));
         assert ((this.nodesOnOuterTriangle.size() >= 0) && (this.nodesOnOuterTriangle.size() <= 3));
 
         boolean isAfterCorner = this.isCornerIndex(triangleIn, Util.nonNegativeModulo(indexIn - 1,
                 this.nodesInThisTriangle[triangleIn]));
 
         boolean isBeforeCorner = this.isCornerIndex(triangleIn, Util.nonNegativeModulo(indexIn + 1,
                 this.nodesInThisTriangle[triangleIn]));
 
         int numberOfSameTriangleNeighbors = this.nodesOnSameTriangle.size();
 
         this.neighbors.clear();
 
         // Pick the left node ((index+1)%segments) on the same triangle first. It's in a for loop, because there can be 3 same
         // triangle neighbors (4x8, tr:1, ind:3) or 4 (2x4 tr:0, ind:1)
 
         for (int i = 0; i < numberOfSameTriangleNeighbors; i++)
         {
             if (this.nodesOnSameTriangle.get(i).index == Util.nonNegativeModulo(indexIn + 1, this.nodesInThisTriangle[triangleIn]))
             {
                 this.neighbors.add(this.nodesOnSameTriangle.get(i));
                 this.nodesOnSameTriangle.remove(i);
                 break;
             }
         }
 
         // Handle a 2nd (of 3 or 4) "same triangle" neighbor (that are before corners .. after corners have 1 on the same and 2
         // after the inner(s))
 
         if ((numberOfSameTriangleNeighbors > 2) && (isBeforeCorner))
         {
             for (int i = 0; i < numberOfSameTriangleNeighbors; i++)
             {
                 if (this.nodesOnSameTriangle.get(i).index == Util.nonNegativeModulo(indexIn + 2,
                         this.nodesInThisTriangle[triangleIn]))
                 {
                     this.neighbors.add(this.nodesOnSameTriangle.get(i));
                     this.nodesOnSameTriangle.remove(i);
                     break;
                 }
             }
         }
 
         // Inners can be done by hand too, as there are only 0, 1, or 2 possible neighbors. Inners go in descending order.
 
         if (this.nodesOnInnerTriangle.size() > 0) 
         {
             if (this.nodesOnInnerTriangle.size() == 2) 
             {  
                 // Handle index wrap-around for before corner nodes
                 if (Util.nonNegativeModulo((indexIn + 1), this.nodesInThisTriangle[this.nodesOnInnerTriangle.get(0).triangle + 1]) == 0)
                 {
                     if (this.nodesOnInnerTriangle.get(0).index == 0) 
                     {
                         this.neighbors.add(this.nodesOnInnerTriangle.get(0));
                         this.nodesOnInnerTriangle.remove(0);
                     }
                     else 
                     {
                         this.neighbors.add(this.nodesOnInnerTriangle.get(1));
                         this.nodesOnInnerTriangle.remove(1);
                     }
                 }
                 else
                 {
 //                    if (false)
 //                    {
 //                        // Ok, this ginormous mess handles:
 //                        // The 2x4 triangle having 2 "inner nodes" that are both on the same triangle.
 //                        // The 3x4 triangle having 1 "inner node" on the same triangle and the other node at t:0,i:0.
 //                        // `-> which is different before and after the corners.
 //                        // The default case where inner indexes decrease for clockwise order.
 //                        // The reason for the temporary variables is because the logic of a combined if statement is uuuuuuuuuugly.
 //
 //                        // ALWAYS FALSE
 //                        // boolean hasOnlyOneSameTriangleInnerNode = ((this.nodesOnInnerTriangle.get(0).triangle == triangleIn) &&
 //                        // (this.nodesOnInnerTriangle.get(1).triangle != triangleIn))||
 //                        // ((this.nodesOnInnerTriangle.get(0).triangle != triangleIn) && (this.nodesOnInnerTriangle.get(1).triangle ==
 //                        // triangleIn));
 //
 //                        // ALWAYS FALSE
 //                        // boolean hasTwoSameTriangleInnerNodes = ((this.nodesOnInnerTriangle.get(0).triangle == triangleIn) &&
 //                        // (this.nodesOnInnerTriangle
 //                        // .get(1).triangle == triangleIn));
 //
 //                        // ALWAYS -1
 //                        // int whichNodeIsClockwiseOnSameTriangleInnerNode = hasTwoSameTriangleInnerNodes ? (this.nodesOnInnerTriangle
 //                        // int whichNodeIsClockwiseOnSameTriangleInnerNode = false ? (this.nodesOnInnerTriangle.get(0).index == Util
 //                        // .nonNegativeModulo(indexIn + 2, this.nodesInThisTriangle[triangleIn])) ? 0 : 1 : -1;
 //
 //                        int zeroZeroNodeIndex = ((this.nodesOnInnerTriangle.get(0).triangle == 0) && (this.nodesOnInnerTriangle.get(0).index==0)) ? 0 :
 //                            ((this.nodesOnInnerTriangle.get(1).triangle == 0) && (this.nodesOnInnerTriangle.get(1).index==0)) ? 1 : -1;
 //
 //                        System.out.println(zeroZeroNodeIndex);
 //
 //                        // if ((((hasTwoSameTriangleInnerNodes == true) && (whichNodeIsClockwiseOnSameTriangleInnerNode == 0))
 //                        // if ((((false == true) && (whichNodeIsClockwiseOnSameTriangleInnerNode == 0))
 //                        if ((((false == true) && (-1 == 0))
 //                                || ((isAfterCorner == true) && (zeroZeroNodeIndex == 0))
 //                                // || ((isBeforeCorner == true) && (zeroZeroNodeIndex == 1)) || ((hasOnlyOneSameTriangleInnerNode ==
 //                                // false)
 //                                || ((isBeforeCorner == true) && (zeroZeroNodeIndex == 1)) || ((false == false)
 //                                        // && (hasTwoSameTriangleInnerNodes == false) && (this.nodesOnInnerTriangle.get(0).index >
 //                                        // this.nodesOnInnerTriangle
 //                                        && (false == false) && (this.nodesOnInnerTriangle.get(0).index > this.nodesOnInnerTriangle
 //                                                .get(1).index))))
 //                        {
 //                        }
 //                    }
 //                    else
 //                    {
                         int differenceInInnerIndex = Math.abs(this.nodesOnInnerTriangle.get(0).index - this.nodesOnInnerTriangle.get(1).index);
 
                         if (((differenceInInnerIndex == 1) && (this.nodesOnInnerTriangle.get(0).index > this.nodesOnInnerTriangle
                                 .get(1).index))
                                 || ((differenceInInnerIndex == 2) && (this.nodesOnInnerTriangle.get(0).index == 0)))
                         {
                             this.neighbors.add(this.nodesOnInnerTriangle.get(0));
                             this.nodesOnInnerTriangle.remove(0);
                         }
                         else 
                         {
                             this.neighbors.add(this.nodesOnInnerTriangle.get(1));
                             this.nodesOnInnerTriangle.remove(1);
                         }
 //                    }
                 }
             }
             this.neighbors.add(this.nodesOnInnerTriangle.get(0));
         }
 
         // Handle a 3rd (of 3 or 4) "same triangle" neighbor (that are before corners .. after corners have 1 on the same and 2
         // after the inner(s))
 
         if ((numberOfSameTriangleNeighbors > 2) && (isAfterCorner))
         {
             for (int i = 0; i < numberOfSameTriangleNeighbors; i++)
             {
                 if (this.nodesOnSameTriangle.get(i).index == Util.nonNegativeModulo(indexIn - 2,
                         this.nodesInThisTriangle[triangleIn]))
                 {
                     this.neighbors.add(this.nodesOnSameTriangle.get(i));
                     this.nodesOnSameTriangle.remove(i);
                     break;
                 }
             }
         }
 
         // Finally add the last "remaining same triangle" neighbor.
 
         this.neighbors.add(this.nodesOnSameTriangle.get(0));
         this.nodesOnSameTriangle.remove(0);
 
         // Outer neighbors are trickier, since there can be 0, 2, 3, 4, or 6 possible neighbors. 6 is handled specially above,
         // otherwise, sort by ascending order (which will handle the 4 with a skip in the middle case). In corners, we add 2 to the
         // index to handle the wrap-around 0 cases.. only for corners, though, since 2x4's will wrap the highest outer index to 0.
 
         if (this.nodesOnOuterTriangle.size() > 0) 
         {
             int numberOfNodesOnOuterTriangle = this.nodesInThisTriangle[this.nodesOnOuterTriangle.get(0).triangle];
 
             boolean didSwap;
 
             do
             {
                 didSwap = false;
 
                 for (int i = 1; i < this.nodesOnOuterTriangle.size(); i++)
                 {
                     int index1ToCheck = this.nodesOnOuterTriangle.get(i - 1).index;
                     int index2ToCheck = this.nodesOnOuterTriangle.get(i).index;
 
                     if (indexIn == 0)
                     {
                         index1ToCheck = Util.nonNegativeModulo(index1ToCheck + 2, numberOfNodesOnOuterTriangle);
                         index2ToCheck = Util.nonNegativeModulo(index2ToCheck + 2, numberOfNodesOnOuterTriangle);
                     }
 
                     if (index1ToCheck > index2ToCheck)
                     {
                         Node tempNode = this.nodesOnOuterTriangle.get(i - 1);
                         this.nodesOnOuterTriangle.set(i - 1, this.nodesOnOuterTriangle.get(i));
                         this.nodesOnOuterTriangle.set(i, tempNode);
                         didSwap = true;
                     }
                 }
             }
             while (didSwap == true);
 
             for (int i = 0; i < this.nodesOnOuterTriangle.size(); i++)
             {
                 this.neighbors.add(this.nodesOnOuterTriangle.get(i));
             }
         }
 
         return this.neighbors;
     }
 
     /**
      * @param triangle
      *            The triangle to check
      * @return Whether or not this is the inner-most triangle
      */
     public boolean isInnerTriangle(final int triangle) 
     {
         assert ((triangle >= 0) && (triangle < this.numberOfTriangles));
 
         return (triangle <= this.transitionTriangleNumber);
     }
 
     /**
      * @param triangle
      * @param index
      * @return
      */
     public boolean isCornerIndex(final int triangle, final int index) 
     {
         assert ((triangle >= 0) && (triangle < this.numberOfTriangles));
 
         final int segmentsInThisTriangle = (this.nodesInThisTriangle[triangle] / 3);
 
         return ((index % segmentsInThisTriangle) == 0);
     }
 
     /**
      * @param triangle
      * @return
      */
     public boolean isInnermostTriangle(final int triangle) 
     {
         return (triangle == 0);
     }
 
     /**
      * @param triangle
      * @return
      */
     public boolean isOutermostRow(final int triangle) 
     {
         return (triangle == (this.numberOfTriangles - 1));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see edu.berkeley.gamesman.game.ConnectGame#isWin(char)
      */
     @Override
     public boolean isWin(final char player) 
     {
         assert Util.debug(DebugFacility.GAME, this.displayState());
 
         boolean result = false;
         // go over the left edge from bottom up
         for (int ind = 0; ind <= this.nodesInThisTriangle[this.numberOfTriangles-1] / 3; ind++) 
         {
             if (this.getPlayerAt(this.numberOfTriangles-1, ind) == player) 
             {
                 // reached Edges [0] - left [1] - right [2] - bottom
                 final boolean[] reachedEdges = new boolean[3];
                 reachedEdges[0] = true; // left edge is reached
                 Node previousNode = null;
                 final Node startNode = board[this.numberOfTriangles-1][ind];
                 Node currentNode = board[this.numberOfTriangles-1][ind];
                 boolean done = false;
 
                 do 
                 {
                     if (currentNode.getTriangle() == this.numberOfTriangles-1) 
                     {
                         final int div = (this.nodesInThisTriangle[this.numberOfTriangles-1] + 1) / 3;
                         final int currentIndex = currentNode.getIndex();
                         if ((currentIndex >= div) && (currentIndex <= 2 * div)) 
                         {
                             reachedEdges[1] = true;
                         }
                         if (((currentIndex >= 2 * div)
                                 && (currentIndex <= 3 * div))
                                 || (currentIndex == 0)) 
                         {
                             reachedEdges[2] = true;
                         }
                     }
                     final Vector<Node> neighbors = this.getNeighbors(currentNode, player);
                     //check whether all neighbors have peaces.
                     //because someone's code fails to do so!
                    int j = 0;
                    while ( j<neighbors.size() )
                    {
                        if (this.getPlayerAt( neighbors.get(j) ) != player)
                        {
                            neighbors.remove(j);
                        }
                        else
                        {
                            j++;
                        }
                    }
                     for (int i = 0; i < neighbors.size(); i++) 
                     {
                         if (previousNode == null) 
                         {
                             previousNode = currentNode;
                             currentNode = neighbors.get(i);// select the first node clock-wise
                             break;
                         }
                         else if (previousNode.equals(neighbors.get(i))) 
                         {
                             previousNode = currentNode;
                             currentNode = neighbors.get((i + 1)
                                     % neighbors.size());// select next node after previous in clock-wise
                             break;
                         }
                     }
                     done = currentNode.equals(startNode)
                     || (reachedEdges[1] && reachedEdges[2]);
                 }
                 while (!done);
 
                 if (reachedEdges[1] && reachedEdges[2]) 
                 {
                     result = true;
                     break;
                 }
                 else if (reachedEdges[1]) 
                 {
                     result = false;
                     break;
                 }
             }
         }
         return result;
     }
 
 	@Override
 	public char[] convertInString(String chars) {
 		char[] charArray = chars.toCharArray();
 		char[] newChars = new char[totalNumberOfNodes];
 		for (int i = 0; i < totalNumberOfNodes; i++) {
 			newChars[i] = charArray[translateOutArray[i]];
 		}
 		return newChars;
 	}
 
 	@Override
 	public String convertOutString(char[] charArray) {
 		char[] newChars = new char[totalNumberOfNodes];
 		for (int i = 0; i < totalNumberOfNodes; i++) {
 			newChars[translateOutArray[i]] = charArray[i];
 		}
 		return new String(newChars);
 	}
 
 	@Override
 	public int translateOut(int i) {
 		return translateOutArray[i];
 	}
 
     /*
      * (non-Javadoc)
      * 
      * @see edu.berkeley.gamesman.game.ConnectGame#setToCharArray(char[])
      */
     @Override
     protected void setToCharArray(final char[] myPieces) 
     {
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see edu.berkeley.gamesman.game.TierGame#displayState()
      */
     @Override
     public String displayState() 
     {
         String displayString;
 
         if ((this.innerTriangleSegments == 4) && (this.outerRingSegments == 8)) 
         {
             assert (this.coordsFor4and8board.length == this.totalNumberOfNodes);
 
             displayString = new String(this.ASCIIrepresentation[0]);
 
             for (int y = 1; y < this.HEIGHT; y++) 
             {
                 displayString.concat("\n");
                 displayString.concat(this.ASCIIrepresentation[y].toString());
             }
 		} else if (this.innerTriangleSegments == 1 && outerRows == 2) {
 			return displayState(yRep12);
 		} else if (this.innerTriangleSegments == 2 && outerRows == 1) {
 			return displayState(yRep21);
 		} else if (this.innerTriangleSegments == 3 && outerRows == 1) {
 			return displayState(yRep31);
 		}
         else 
         {
             displayString = new String(
                     "UNABLE TO REPRESENT THIS CONFIGURATION (triangle segments:"
                     + this.innerTriangleSegments + ", outer rows:"
                     + this.outerRingSegments + " )IN 2D (yet):\n");
             char[] charArray = new char[totalNumberOfNodes];
             mmh.getCharArray(charArray);
             displayString = displayString.concat(new String(charArray));
         }
 
         return (displayString);
     }
 
 	private String displayState(String yRep) {
 		char[] s = yRep.toCharArray();
 		for (int i = 0; i < s.length; i++) {
 			char c = s[i];
 			if (c >= 'A' && c <= 'Z') {
 				s[i] = mmh.get(c - 'A');
 				if(s[i] == ' ')
 					s[i] = '-';
 			}
 		}
 		return new String(s);
 	}
     
     /**
      * @param triangleIn
      * @param indexIn
      * @return
      */
     public char getPlayerAt(final int triangle, final int index) 
     {
         return this.board[triangle][index].getChar();
     }
 
     /**
      * @param triangleIn
      * @param indexIn
      * @return
      */
     public char getPlayerAt(final Node node) 
     {
         return node.getChar();
     }
 
     /**
      * FOR TESTING PURPOSES
      * 
      * @param triangle
      * @param index
      * @param player
      */
     public void setPlayerAt(final int triangle, final int index, final char player) 
     {
     	mmh.set(board[triangle][index].totalIndex, player);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see edu.berkeley.gamesman.game.Game#describe()
      */
     @Override
     public String describe() 
     {
         return "Y" + this.numberOfTriangles;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() 
     {
         return this.displayState();
     }
 
     // Cribbed from Wikipedia: http://www.hexwiki.org/index.php?title=Programming_the_bent_Y_board, see attached PNG for expected node numbers that we have to transform. This should also be generalizable for smaller boards.
 
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
             { 0.4505, 0.4213 }
     }
     ;
 
     static public void main(final String[] args) throws ClassNotFoundException 
     {
         final ClassLoader cl = ClassLoader.getSystemClassLoader();
         cl.setClassAssertionStatus("YGame", true);
 
         if (args.length < 3) 
         {
             System.err
             .println("I'm expecting 3 command line arguments, the 2x4, 3x6, and 4x8 configuration files!");
             System.exit(-1);
         }
 
         Configuration conf;
         YGame game;
         Vector<Node> neighbors;
 
         conf = new Configuration(args[1]); // 3x6
         game = (YGame) conf.getGame();
 
         neighbors = game.getNeighbors(0, 0, 'X');
 
         for (int i = 0; i < neighbors.size(); i++)
         {
             System.out.println("Neighbor #" + i + ": " + neighbors.get(i));
         }
 
         game.fillBoardWithPlayer('X');
 
         System.out.println(game.displayState());
 
         neighbors = game.getNeighbors(1, 2, 'X');
 
         for (int i = 0; i < neighbors.size(); i++)
         {
             System.out.println("Neighbor #" + i + ": " + neighbors.get(i));
         }
 
         System.out.println();
 
         assert (neighbors.size() == 6);
 
         neighbors = game.getNeighbors(0, 0, 'X');
 
         for (int i = 0; i < neighbors.size(); i++)
         {
             System.out.println("Neighbor #" + i + ": " + neighbors.get(i));
         }
 
         System.out.println();
 
         assert (neighbors.size() == 6);
 
         conf = new Configuration(args[2]); // 4x8
         game = (YGame) conf.getGame();
 
         game.fillBoardWithPlayer('X');
 
         neighbors = game.getNeighbors(1, 1, 'X');
 
         for (int i = 0; i < neighbors.size(); i++)
 
         {
             System.out.println("Neighbor #" + i + ": " + neighbors.get(i));
         }
 
         System.out.println();
 
         assert (neighbors.size() == 6);
 
         conf = new Configuration(args[0]); // 2x4 
         game = (YGame)conf.getGame();
 
         game.fillBoardWithPlayer('X');
 
         neighbors = game.getNeighbors(1, 0, 'X');
 
         for (int i = 0; i < neighbors.size(); i++)
         {
             System.out.println("Neighbor #" + i + ": " + neighbors.get(i));
         }
 
         System.out.println();
 
         assert (neighbors.size() == 6);
 
         neighbors = game.getNeighbors(1, 3, 'X');
 
         for (int i = 0; i < neighbors.size(); i++)
         {
             System.out.println("Neighbor #" + i + ": " + neighbors.get(i));
         }
 
         System.out.println();
 
         assert (neighbors.size() == 6);
 
         neighbors = game.getNeighbors(2, 0, 'X');
 
         for (int i = 0; i < neighbors.size(); i++)
         {
             System.out.println("Neighbor #" + i + ": " + neighbors.get(i));
         }
 
         System.out.println();
 
         assert (neighbors.size() == 3);
 
         neighbors = game.getNeighbors(2, 1, 'X');
 
         for (int i = 0; i < neighbors.size(); i++)
         {
             System.out.println("Neighbor #" + i + ": " + neighbors.get(i));
         }
 
         System.out.println();
 
         assert (neighbors.size() == 4);
 
         neighbors = game.getNeighbors(2, 8, 'X');
 
         for (int i = 0; i < neighbors.size(); i++)
         {
             System.out.println("Neighbor #" + i + ": " + neighbors.get(i));
         }
 
         System.out.println();
 
         assert (neighbors.size() == 3);
 
         conf = new Configuration(args[1]); // 3x6
         game = (YGame) conf.getGame();
 
         game.fillBoardWithPlayer('X');
 
         System.out.println(game.displayState());
 
         neighbors = game.getNeighbors(2, 0, 'X');
 
         for (int i = 0; i < neighbors.size(); i++)
         {
             System.out.println("Neighbor #" + i + ": " + neighbors.get(i));
         }
 
         System.out.println();
 
         assert (neighbors.size() == 6);
 
         conf = new Configuration(args[2]); // 4x8
         game = (YGame) conf.getGame();
 
         game.fillBoardWithPlayer('X');
 
         System.out.println(game.displayState());
 
         neighbors = game.getNeighbors(4, 3, 'X');
 
         for (int i = 0; i < neighbors.size(); i++)
         {
             System.out.println("Neighbor #" + i + ": " + neighbors.get(i));
         }
 
         System.out.println();
 
         assert (neighbors.size() == 6);
 
         neighbors = game.getNeighbors(4, 4, 'X');
 
         for (int i = 0; i < neighbors.size(); i++)
         {
             System.out.println("Neighbor #" + i + ": " + neighbors.get(i));
         }
 
         System.out.println();
 
         assert (neighbors.size() == 6);
 
         neighbors = game.getNeighbors(4, 17, 'X');
 
         for (int i = 0; i < neighbors.size(); i++)
         {
             System.out.println("Neighbor #" + i + ": " + neighbors.get(i));
         }
 
         System.out.println();
 
         assert (neighbors.size() == 6);
 
         neighbors = game.getNeighbors(2, 8, 'X');
 
         for (int i = 0; i < neighbors.size(); i++)
 
         {
             System.out.println("Neighbor #" + i + ": " + neighbors.get(i));
         }
 
         System.out.println();
 
         assert (neighbors.size() == 6);
     }
 }
