 import java.io.*;
 import java.util.*;
 import java.text.*;
 import java.math.*;
 import java.util.regex.*;
 
 public class Solution {
 	/* Head ends here */
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
 		}
 		else {// player == 'g'
 			way = -1;
 			aux = getCharAtXY(board, g_x, g_y + 1);
 			if (aux != '#' && aux != 'r' && aux != 'g')
 				way = 1;
 			else {
 				aux = getCharAtXY(board, g_x + 1, g_y);
 				if (aux != '#' && aux != 'r' && aux != 'g') {
 					way = 0;
 				}
 				else {
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
			aux = getCharAtXY(board, r_x, r_y + i);
 			while (aux != '#' && aux != 'r' && aux != 'g') {
 				counter3++;
 				i++;
				aux = getCharAtXY(board, r_x, r_y + i);
 			}
 			/* Search Left */
 			i = 1;
			aux = getCharAtXY(board, r_x, r_y - i);
 			while (aux != '#' && aux != 'r' && aux != 'g') {
 				counter4++;
 				i++;
				aux = getCharAtXY(board, r_x, r_y - i);
 			}
 
 		}
 		else {
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
			aux = getCharAtXY(board, g_x, g_y + i);
 			while (aux != '#' && aux != 'r' && aux != 'g') {
 				counter3++;
 				i++;
				aux = getCharAtXY(board, g_x, g_y + i);
 			}
 			/* Search Left */
 			i = 1;
			aux = getCharAtXY(board, g_x, g_y - i);
 			while (aux != '#' && aux != 'r' && aux != 'g') {
 				counter4++;
 				i++;
				aux = getCharAtXY(board, g_x, g_y - i);
 			}
 
 		}
 
 		/* See whitch way is the best one */
 		int way;
 		if (counter1 >= counter2 && counter1 >= counter3 && counter1 >= counter4) {
 			way = 0; /* Down */
 		}
 		else if (counter2 >= counter1 && counter2 >= counter3 && counter2 >= counter4)
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
 		Longest_way_bot(player, pos, board);
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
