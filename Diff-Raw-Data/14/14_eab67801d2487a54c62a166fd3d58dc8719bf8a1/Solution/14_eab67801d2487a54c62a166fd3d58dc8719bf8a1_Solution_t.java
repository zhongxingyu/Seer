 import java.io.*;
 import java.util.*;
 import java.text.*;
 import java.math.*;
 import java.util.regex.*;
 
 public class Solution {
 	/* Head ends here */
 
 	/*
 	 * Culori pentru alg de BFS Probabil nu vm avea nevoie de toate
 	 */
 	public enum Color {
 		ALB, GRI, NEGRU, RED, GREEN, WALL
 	}
 
 	public class Graph {
 		/*
 		 * Un fel de pseudo graph defapt pt ca nu am perechi de <nod,lista de vecini> ci doar o
 		 * lista de noduri care contine vecinii in el.
 		 * 
 		 * Face graful pe baza matricei de stringuri .
 		 */
 		ArrayList<Node> Nodes = new ArrayList<Node>();
 		Node red, green; /* nodurile curente pt cei 2 playeri */
 
 		public Graph(String player, int[] pos, String[] board) {
 			int index = 0;
 			
 			/* Adauga pt fiecare nod in parte indecsii vecinilor lui*/
 			for(int i = 1 ; i < board.length-1 ; i ++)
				for(int j = 1 ; j< board[1].length()-2 ; j++){
 					
 					Node a = new Node(index,i,j);
 					
 					if(i == pos[0] && j == pos[1])
 						red = a;
 					if(i == pos[2] && j == pos[3])
 						green = a;
 					
 					
 					if (getCharAtXY(board, i - 1, j) == '-') {
 						a.neighbours_by_index.add(index - (board[1].length()-2));
 					}
 					if (getCharAtXY(board, i + 1, j) == '-') {
 						a.neighbours_by_index.add(index + (board[1].length()-2));
 					}
 					if (getCharAtXY(board, i, j - 1) == '-') {
 						a.neighbours_by_index.add(index - 1);
 					}
 					if (getCharAtXY(board, i, j + 1) == '-') {
 						a.neighbours_by_index.add(index +1);
 					}
 					Nodes.add(a);
 					index++;
 				}
 			/* Se foloseste de indecsii gasiti anterior ca sa adauge nodurile reale*/
 			for (Node n : Nodes){
 				for(int x : n.neighbours_by_index){
 					n.neighbours.add(Nodes.get(x));
 				}
 			}
 		}
 
 		public int nodeCount() {
 			return Nodes.size();
 		}
 
 		public ArrayList<Node> getNodes() {
 			return Nodes;
 		}
 
 		public String toString() {
 			String res = "Graph\n";
			res += "Red= " + this.red.index + "\n";
			res += "Green= " + this.green.index + "\n";
 			for (Node n : Nodes) {
 				res += n.toString();
 				res += "\n";
 			}
 			return res;
 		}
 
 	}
 
 	/* Node class */
 	public class Node {
 		int index;
 		int x, y;
 		ArrayList<Node> neighbours = new ArrayList<Node>();
 		ArrayList<Integer> neighbours_by_index = new ArrayList<Integer>();
 		
 		Color culoare;
 
 		public Node(int index, int x, int y) {
 			this.index = index;
 			culoare = Color.ALB; 
 			this.x = x;
 			this.y = y;
 		}
 
 		public Node(int index, Color culoare) {
 			this.index = index;
 			this.culoare = culoare;
 		}
 
 		public int getIndex() {
 			return index;
 		}
 
 		public Color getColor() {
 			return culoare;
 		}
 
 		public void setColor(Color x) {
 			this.culoare = x;
 		}
 
 		public ArrayList<Node> getNeighbours() {
 			return neighbours;
 		}
 
 		public String toString() {
 			String res = "Node index = ";
 			res += Integer.toString(index) + " si culoare = " + culoare.toString() + "\n";
 			res += "Coordonate: x=" + this.x + " y=" + this.y + " \n";
 			res += "Vecini: ";
 			
 			for (int i = 0; i < neighbours.size(); i++) {
 				res += Integer.toString(neighbours.get(i).index) + ", ";
 			}
 			/*
 			for (int i = 0; i < neighbours_by_index.size(); i++) {
 				res += Integer.toString(neighbours_by_index.get(i)) + ", ";
 			}
 			*/
 			return res;
 		}
 
 	}
 
 	static void survival(String player, int[] pos, String[] board) {
 		int r_x = pos[0], r_y = pos[1], g_x = pos[2], g_y = pos[3];
 		int way = 0;
 		char aux;
 		if (player.equals("r")) {
 			/* try to go after the wall. go kind of right */
 
 			aux = getCharAtXY(board, r_x, r_y - 1);
 			way = -1;
 			// try left
 			if (aux != '#' && aux != 'r' && aux != 'g')
 				way = 3;
 			else {
 				aux = getCharAtXY(board, r_x - 1, r_y);
 				if (aux != '#' && aux != 'r' && aux != 'g')
 					way = 2;
 				else {
 					aux = getCharAtXY(board, r_x, r_y + 1);
 					if (aux != '#' && aux != 'r' && aux != 'g')
 						way = 1;
 					else
 						way = 0;
 				}
 			}
 		} else {// player == 'g'
 			way = -1;
 			aux = getCharAtXY(board, g_x, g_y + 1);
 			if (aux != '#' && aux != 'r' && aux != 'g')
 				way = 1;
 			else {
 				aux = getCharAtXY(board, g_x + 1, g_y);
 				if (aux != '#' && aux != 'r' && aux != 'g') {
 					way = 0;
 				} else {
 					aux = getCharAtXY(board, g_x, g_y - 1);
 					if (aux != '#' && aux != 'r' && aux != 'g')
 						way = 3;
 					else
 						way = 2;
 				}
 			}
 		}
 
 		/* Print the best way to go */
 		switch (way) {
 		case 0:
 			System.out.println("DOWN");
 			break;
 		case 1:
 			System.out.println("RIGHT");
 			break;
 		case 2:
 			System.out.println("UP");
 			break;
 		default:
 			System.out.println("LEFT");
 			break;
 		}
 	}
 
 	static void Longest_way_bot(String player, int[] pos, String[] board) {
 		int counter1 = 0, counter2 = 0, counter3 = 0, counter4 = 0, i;
 		int r_x = pos[0], r_y = pos[1], g_x = pos[2], g_y = pos[3];
 		char aux;
 		if (player.equals("r")) {
 			/* Search Down */
 			i = 1;
 			aux = getCharAtXY(board, r_x + i, r_y);
 			while (aux != '#' && aux != 'r' && aux != 'g') {
 				counter1++;
 				i++;
 				aux = getCharAtXY(board, r_x + i, r_y);
 			}
 			/* Search Up */
 			i = 1;
 			aux = getCharAtXY(board, r_x - i, r_y);
 			while (aux != '#' && aux != 'r' && aux != 'g') {
 				counter2++;
 				i++;
 				aux = getCharAtXY(board, r_x - i, r_y);
 			}
 			/* Search Right */
 			i = 1;
 			aux = getCharAtXY(board, r_x - i, r_y + i);
 			while (aux != '#' && aux != 'r' && aux != 'g') {
 				counter3++;
 				i++;
 				aux = getCharAtXY(board, r_x - i, r_y + i);
 			}
 			/* Search Left */
 			i = 1;
 			aux = getCharAtXY(board, r_x - i, r_y - i);
 			while (aux != '#' && aux != 'r' && aux != 'g') {
 				counter4++;
 				i++;
 				aux = getCharAtXY(board, r_x - i, r_y - i);
 			}
 
 		} else {
 			/* Search Down */
 			i = 1;
 			aux = getCharAtXY(board, g_x + i, g_y);
 			while (aux != '#' && aux != 'r' && aux != 'g') {
 				counter1++;
 				i++;
 				aux = getCharAtXY(board, g_x + i, g_y);
 			}
 			/* Search Up */
 			i = 1;
 			aux = getCharAtXY(board, g_x - i, g_y);
 			while (aux != '#' && aux != 'r' && aux != 'g') {
 				counter2++;
 				i++;
 				aux = getCharAtXY(board, g_x - i, g_y);
 			}
 			/* Search Right */
 			i = 1;
 			aux = getCharAtXY(board, g_x - i, g_y + i);
 			while (aux != '#' && aux != 'r' && aux != 'g') {
 				counter3++;
 				i++;
 				aux = getCharAtXY(board, g_x - i, g_y + i);
 			}
 			/* Search Left */
 			i = 1;
 			aux = getCharAtXY(board, g_x - i, g_y - i);
 			while (aux != '#' && aux != 'r' && aux != 'g') {
 				counter4++;
 				i++;
 				aux = getCharAtXY(board, g_x - i, g_y - i);
 			}
 
 		}
 
 		/* See whitch way is the best one */
 		int way;
 		if (counter1 >= counter2 && counter1 >= counter3 && counter1 >= counter4) {
 			way = 0; /* Down */
 		} else if (counter2 >= counter1 && counter2 >= counter3 && counter2 >= counter4)
 			way = 1; /* Up */
 		else if (counter3 >= counter2 && counter3 >= counter1 && counter3 >= counter4)
 			way = 2; /* Right */
 		else
 			way = 3; /* Left */
 
 		/* Print the best way to go */
 		switch (way) {
 		case 0:
 			System.out.println("DOWN");
 			break;
 		case 1:
 			System.out.println("UP");
 			break;
 		case 2:
 			System.out.println("RIGHT");
 			break;
 		default:
 			System.out.println("LEFT");
 			break;
 		}
 
 	}
 
 	static char getCharAtXY(String[] board, int x, int y) {
 		return board[x].charAt(y);
 	}
 
 	static long getTime() {
 		return System.currentTimeMillis();
 	}
 
 	static void nextMove(String player, int[] pos, String[] board) {
 		
 		//Longest_way_bot(player, pos, board);
 		Solution sol = new Solution();
 		Solution.Graph g = sol.new Graph(player, pos, board);
 		System.out.println(g.toString());
 		
 		
 	}
 
 	/* Tail starts here */
 	public static void main(String[] args) {
 		Scanner in = new Scanner(System.in);
 		in.useDelimiter("\n");
 		String player = in.next();
 		String pos = in.next();
 		String[] str_pos = pos.split(" ");
 		int[] position = new int[4];
 		int[] sizes = new int[2];

 		for (int i = 0; i < 4; i++) {
 			position[i] = Integer.parseInt(str_pos[i]);
 		}
 		pos = in.next();
 		str_pos = pos.split(" ");
 		for (int i = 0; i < 2; i++) {
 			sizes[i] = Integer.parseInt(str_pos[i]);
 		}
 
 		String board[] = new String[sizes[0]];
 
 		for (int i = 0; i < sizes[0]; i++) {
 			board[i] = in.next();
 		}
 
 		nextMove(player, position, board);
 
 	}
 }
