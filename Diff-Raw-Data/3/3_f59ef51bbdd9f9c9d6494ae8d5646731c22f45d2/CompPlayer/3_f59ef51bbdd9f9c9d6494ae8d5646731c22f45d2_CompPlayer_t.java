 package main;
 
 public class CompPlayer extends Player{
 	private int level;//the level of computer player. probability to do best step (level/4)
 	
 	//get mark of player and the level of computer
 	public CompPlayer(boolean m, int l) {
 		super(m);
 		level=l;
 	}
 	
 	//get the board and do the next step of this player. the step is best step or random step by level
 	public void step(Board board){
 		int step = 0;
 		int correct=(int) Math.random()*(4-level);
 		if (correct==0) step=correct(board);
 		else step=rndStep(board);
 		board.setLocateValue(step, getGMark());
 	}
 	
 	//return random step from the possible steps
 	private int rndStep(Board board) {
 		return board.getEmpty()[(int)(Math.random()*board.getEmpty().length)];
 	}
 
 	//return the best step
 	private int correct(Board board) {
 		return correct(board,0,mark)[0];
 	}
 
 	public int[] correct(Board board, int lastStep,boolean firstMark) {
 		int[] answer={lastStep,-1};
 		if (((board.getStatus()==0)&(!mark))||((board.getStatus()==2)&(mark))){// I win
 			answer[0]=lastStep;
 			answer[1]=2; 
 		}
 		else if (board.getStatus()==1) { //teko
 			answer[0]=lastStep;
 			answer[1]=1; 
 		}
 		else if (((board.getStatus()==0)&(mark))||((board.getStatus()==2)&(!mark))){// I lose
 			answer[0]=lastStep;
 			answer[1]=0;
 		}
 		else{ //the game not finished
 			CompPlayer other=new CompPlayer(!mark, 4);
 			for (int i=0;i<board.getEmpty().length;i++){
 				Board board2=board.clone();
 				int nextStep=board.getEmpty()[i];
 				board2.setLocateValue(nextStep,getGMark());
 				int myStep=lastStep;
 				if (lastStep==0) myStep=nextStep;
 				answer=betterStep(other.correct(board2,myStep,firstMark),answer,firstMark);
 			}
 		}
 		return answer;
 	}
 	
 	private int[] betterStep(int[] step1, int[] step2,boolean firstMark){
 		if (firstMark!=mark){
 			step1[1]=step1[1]*-1;
 			step2[1]=step2[1]*-1;
		}
		if (step1[1]<step2[1]) return step2;
 		else return step1;
 	}
 }
