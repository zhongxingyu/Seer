 package project;
 
 import project.Game.WallDirections;
 
 public class GraphBoard implements Board {
 
 	//graph-based implementation of a board
 	
 	Graph b;
 	protected GraphBoard () {
 		b = new Graph(Game.N_ROWS);
 	}
 	
 
 	@Override
 	public boolean isUnique(Board newBoard) { //compare 
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void MakeWall(int Xpos, int Ypos, WallDirections d) {
 		// TODO Auto-generated method stub
 		int edge1[] = new int[2], edge2[] = new int[2];
 		int NWSquare = Ypos*(Game.N_ROWS) + Xpos;
		edge1[0] = NWSquare;
 		if (d == WallDirections.h) {
 			edge1[1] = NWSquare + Game.N_ROWS; //edges being broken are vertical
 			edge2[0] = NWSquare + 1;
 			edge2[1] = NWSquare + 1 + Game.N_ROWS;
 		} else if (d == WallDirections.v) {
 			edge2[0] = NWSquare + Game.N_ROWS; //edges being broken are horizontal
 			edge1[1] = NWSquare + 1;
 			edge2[1] = NWSquare + 1 + Game.N_ROWS;
 		}
 		
 		b.removeEdge(edge1[0], edge1[1]);//remove both edges from graph
 		b.removeEdge(edge2[0], edge2[1]);
 	}
 
 	public boolean isWall (int x1, int y1, int x2, int y2) {
 		int s1 = y1*Game.N_ROWS + x1;
 		int s2 = y2*Game.N_ROWS + x2;
 		return !b.isEdge(s1, s2);
 	}
 
 
 	public void printBoard (int x1, int y1, int x2, int y2) {
 		// for printing purposes
 		int i, j, k;
 		for (i=0; i<Game.N_ROWS; i++) {
 			for (j=0; j<Game.N_ROWS; j++) {
 				//check for player's position and indicate if player there
 				if ((j==x1) &&(i==y1)) {
 					System.out.print("O"); // current player
 				} else if ((j==x2)&&(i==y2)) {
 					System.out.print("X"); // opponent 
 				} else {
 					System.out.print("."); // these are cells
 				}
 				if (j!=Game.N_ROWS-1) {
 					if (this.isWall(j, i, j+1, i)) {
 						System.out.print("|"); // vertical walls
 					} else {
 						System.out.print(" ");
 					}
 				}
 			}
 			System.out.println();
 			for (k=0; (k<Game.N_ROWS) && (i!=Game.N_ROWS-1); k++) {
 				if (this.isWall(k, i, k, i+1)) {
 					System.out.print("- "); // horizontal walls
 				} else {
 					System.out.print("  ");
 				}
 			}
 			System.out.println();
 		}
 	}
 }
