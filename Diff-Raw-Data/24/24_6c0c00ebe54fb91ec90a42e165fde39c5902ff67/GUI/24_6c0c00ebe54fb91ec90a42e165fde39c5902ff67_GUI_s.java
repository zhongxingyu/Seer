 
 public class GUI {
 	public static void main(String[] args){
 		Grid g = new Grid(10, 10);
 		g.createStandard();
		Node[] path = AStar.algorithm(g, g.getNode(0,0), g.getNode(7,9));
 		printGrid(g, path);
 	}
 	
 	private static void printGrid(Grid g, Node[] path){
 		for(int y = (g.getY() * 2) -2; y >= 0; y--){
 			for(int x = 0; x < (g.getX() * 2)-1; x++){			
 				if(x%2 == 1 && y%2 == 1){
 					boolean diagUp = g.getEdgeLength(g.getNode((x-1)/2,(y-1)/2),g.getNode((x+1)/2,(y+1)/2)) != 0;
 					boolean diagDown = g.getEdgeLength(g.getNode((x-1)/2,(y+1)/2),g.getNode((x+1)/2,(y+1)/2)) != 0;
 					if (diagUp && diagDown)
 						System.out.print("X");
 					else if(diagUp)
 						System.out.print("/");
 					else if(diagDown)
 						System.out.print("\\");
 					else
 						System.out.print(" ");
 				}
 				else if(x%2 == 1){
 					if(g.getEdgeLength(g.getNode((x-1)/2, y/2), g.getNode((x+1)/2, y/2)) != 0)
 						System.out.print("-");
 					else
 						System.out.print(" ");
 				}
 				else if(y%2 == 1){
 					if(g.getEdgeLength(g.getNode(x/2, (y-1)/2), g.getNode(x/2, (y+1)/2)) != 0)
 						System.out.print("|");
 					else
 						System.out.print(" ");
 					if(x == (g.getX() * 2) - 2)
 						System.out.println();
 				}
 				else{
 					for(Node n: path){
 						if(n.equals(g.getNode(x/2,y/2)))
 							System.out.print("P");
 					}
 					System.out.print(g.getNode(x/2,y/2).getCost());
				}
				if(x == (g.getX() * 2) - 2){
					System.out.println();
 				}	
					
 			}
 		}
 	}
 }
