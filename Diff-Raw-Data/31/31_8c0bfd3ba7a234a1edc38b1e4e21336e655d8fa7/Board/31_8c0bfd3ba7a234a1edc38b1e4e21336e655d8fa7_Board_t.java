 package tictactoeai;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 public class Board {
 	private Place[] board;
 	private boolean isWon;
 	int numOfFreeSpaces;
 	int winner;
 
 	public Board(){
 		board = new Place[10]; 
 		for(int i=1; i<=9; i++){
 			board[i] = new Emptyplace(i);
 		}
 		isWon=false;
 		winner=0;
 		numOfFreeSpaces=9;
 	}
 	public Board(Place[] places){
 		this.board = places;
 		winner=0;
 		isWon=isOver();
 	}
 	public Place[] getBoard(){
 		return board;
 	}
 	public void makeMove(int player, int space){
 		if(player==2){
 			board[space] = new Oplace();
 			numOfFreeSpaces--;
 		}
 		if(player==1){
 			board[space] = new Xplace();
 			numOfFreeSpaces--;
 		}
 	}
 	public boolean isOver(){
 		if(numOfFreeSpaces==0){
 			return true;
 		}
//		boolean full=false;
 		boolean won=false;
//		int empty=0;
//		for(int i=1; i<=9; i++){
//			if(board[i].getPlace()==0){
//				empty++;
//			}
//		}
//		if(empty!=0){
//			full=false;
//		}else{
//			full=true;
//		}
 		if(win(1,4,7)||win(2,5,8)||win(3,6,9)||win(1,2,3)||
				win(4,5,6)||win(7,8,9)||win(1,5,9)||win(3,5,7)){
 			won=true;
 		}
		return won;
 	}
 	/**
 	 * returns the hold value of the player who won
 	 * @return 0 if no one won
 	 */
 	public int whoWon(){
 		List<List<Integer>> lines = new ArrayList<List<Integer>>(8);
 		ArrayList<Integer> l1 = new ArrayList<Integer>(3);
 		l1.add(1);	l1.add(4);	l1.add(7);
 		ArrayList<Integer> l2 = new ArrayList<Integer>(3);
 		l2.add(2);	l2.add(5);	l2.add(8);
 		ArrayList<Integer> l3 = new ArrayList<Integer>(3);
 		l3.add(3);	l3.add(6);	l3.add(9);
 		ArrayList<Integer> l4 = new ArrayList<Integer>(3);
 		l4.add(1);	l4.add(2);	l4.add(3);
 		ArrayList<Integer> l5 = new ArrayList<Integer>(3);
 		l5.add(4);	l5.add(5);	l5.add(6);
 		ArrayList<Integer> l6 = new ArrayList<Integer>(3);
 		l6.add(7);	l6.add(8);	l6.add(9);
 		ArrayList<Integer> l7 = new ArrayList<Integer>(3);
 		l7.add(1);	l7.add(5);	l7.add(9);
 		ArrayList<Integer> l8 = new ArrayList<Integer>(3);
 		l8.add(3);	l8.add(5);	l8.add(7);
 		lines.add(l1);	lines.add(l2);	lines.add(l3);
 		lines.add(l4);	lines.add(l5);	lines.add(l6);
 		lines.add(l7);	lines.add(l8);
 		for(List<Integer> l: lines){
 			if(win(l)){
 				return board[l.get(0)].getPlace();
 			}
 		}
 		return 0;
 	}
 	public String toString(){
 		StringBuilder sb = new StringBuilder();
 		for(int i=1; i<=9; i++){
 			sb.append(board[i].printPlace()+" ");
 			if(i%3==0){
 				sb.append("\n");
 			}
 		}
 		return sb.toString();
 	}
 	/**
 	 * determine winner using a list interface
 	 * @param line
 	 * @return
 	 */
 	private boolean win(List<Integer> line){
 		Iterator<Integer> itr = line.iterator();
 		int first = board[itr.next()].getPlace();
 		int second = board[itr.next()].getPlace();
 		int third = board[itr.next()].getPlace();
 		if(first==second&&second==third){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	private boolean win(int p1, int p2, int p3){
 		if(board[p1].getPlace()==1&&board[p2].getPlace()==1&&board[p3].getPlace()==1){
 			return true;
 		}
 		if(board[p1].getPlace()==2&&board[p2].getPlace()==2&&board[p3].getPlace()==2){
 			return true;
 		}
 		return false;
 	}
 	public boolean equals(Board b){
 		System.out.println("comparing");
 		Place[] p = b.getBoard();
 		for(int i=1; i<=9; i++){
 			if(p[i].getPlace()!=this.board[i].getPlace())
 				return false;
 		}
 		return true;
 	}
 }
