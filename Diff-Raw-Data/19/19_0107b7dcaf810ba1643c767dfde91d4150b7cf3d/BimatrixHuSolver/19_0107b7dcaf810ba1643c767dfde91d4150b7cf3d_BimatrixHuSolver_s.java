 /**
  * Code to solve 2 player bimatrix games for Nash equilibria.
  * Modified from http://www.junlinghu.com/papers.html
  * 
  * @author sodomka
  * 
  * Original header:
  * Applet for calculating Nash equilibria for two-person bimatrix game. 
  * Copyright (c) Junling Hu and Yilian Zhang, 2001 
  * This is an Applet that allows an user to enter the payoff matrices, the then caculated possible Nash equilibria for the bimatrix game. 
  * The algorithm is Lemke-Howson method decribed in the following book:    
  * @book{Cottle91,
  *   author = {Cottle, Richard W. and Pang, J.--S. and R. E. Stone},
  *   title =	 {The Linear Complementarity Problem},
  *   year =	 1992,
  *   publisher = {Academic Press},
  *   address = {New York}
  */
 package normalformsolver;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 import props.DiscreteDistribution;
 import props.Joint;
 import sequentialgame.AbstractAction;
 import normalformgame.NormalFormGame;
 
 public class BimatrixHuSolver<A extends AbstractAction> implements NormalFormSolver<A> {
 
 	static final int player1Idx = 0;
 	static final int player2Idx = 1;
	
 	
 	public static void main(String[] args) {
 		//game: {[stick, stick]=[130.91049, 65.39049000000003], [right, right]=[89.81000000000002, -0.1], [stick, up]=[130.91049, 65.29049000000003], [right, up]=[117.62944100000003, 58.75144100000003], [down, down]=[89.81000000000002, 0.7010802774987996], [up, left]=[117.62944100000003, 58.670441000000025], [right, down]=[89.81000000000002, -0.1], [right, stick]=[117.62944100000003, 58.85144100000003], [stick, right]=[40.36950000000001, 40.26950000000001], [left, up]=[145.45610000000002, 72.55610000000003], [up, stick]=[130.81049000000002, 65.39049000000003], [right, left]=[117.62944100000003, 58.670441000000025], [up, down]=[130.81049000000002, 65.29049000000003], [left, stick]=[145.45610000000002, 72.65610000000002], [stick, left]=[130.91049, 65.20049000000003], [down, stick]=[145.45610000000002, 72.65610000000002], [down, left]=[145.45610000000002, -0.1], [left, left]=[145.45610000000002, -0.1], [stick, down]=[130.91049, 65.29049000000003], [down, right]=[89.81000000000003, 0.7010802774994668], [up, right]=[-0.1, 80.72900000000003], [left, right]=[-0.1, 80.72900000000003], [down, up]=[145.45610000000002, 72.55610000000003], [up, up]=[130.81049000000002, 65.29049000000003], [left, down]=[-0.1, 80.72900000000003]}
 		double[][] payoffs1 = {{130.81049000000002, 130.81049000000002, 117.62944100000003, -0.1, 130.81049000000002}, {145.45610000000002, 89.81000000000002, 145.45610000000002, 89.81000000000003, 145.45610000000002}, {145.45610000000002, -0.1, 145.45610000000002, -0.1, 145.45610000000002}, {117.62944100000003, 89.81000000000002, 117.62944100000003, 89.81000000000002, 117.62944100000003}, {130.91049, 130.91049, 130.91049, 40.36950000000001, 130.91049}};
 		double[][] payoffs2 = {{65.29049000000003, 65.29049000000003, 58.670441000000025, 80.72900000000003, 65.39049000000003}, {72.55610000000003, 0.7010802774987996, -0.1, 0.7010802774994668, 72.65610000000002}, {72.55610000000003, 80.72900000000003, -0.1, 80.72900000000003, 72.65610000000002}, {58.75144100000003, -0.1, 58.670441000000025, -0.1, 58.85144100000003}, {65.29049000000003, 65.29049000000003, 65.20049000000003, 40.26950000000001, 65.39049000000003}};
 		int numActions = payoffs1.length;
 		System.out.println("payoffs1:");
 		for (int a1=0; a1<numActions; a1++) {
 			System.out.println(Arrays.toString(payoffs1[a1]));
 		}
 		System.out.println("payoffs2:");
 		for (int a1=0; a1<numActions; a1++) {
 			System.out.println(Arrays.toString(payoffs2[a1]));
 		}
 		Joint<double[]> strategies = solveForMixedStrategies(payoffs1, payoffs2);
 		System.out.println("player1Strategy: " + Arrays.toString(strategies.getForPlayer(0)));
 		System.out.println("player2Strategy: " + Arrays.toString(strategies.getForPlayer(1)));
 		
 	}
 	
 //	public static void main(String[] args) {
 //		int numPlayers = 2;
 //		GridAction a1 = new GridAction("a1");
 //		GridAction a2 = new GridAction("a2");
 //		List<GridAction> actions = new ArrayList<GridAction>();
 //		actions.add(a1);
 //		actions.add(a2);
 //		Joint<List<GridAction>>  possibleActionsPerPlayer = new Joint<List<GridAction>>();
 //		possibleActionsPerPlayer.add(actions);
 //		possibleActionsPerPlayer.add(actions);
 //		List<Joint<GridAction>> possibleJointActions = GridGame.computePossibleJointActions(actions, numPlayers);
 //		NormalFormGame<GridAction> normalFormGame = new NormalFormGame<GridAction>(numPlayers, possibleActionsPerPlayer, possibleJointActions);
 //		Random random = new Random();
 //		for (int i=0; i<possibleJointActions.size(); i++) {
 //			Joint<GridAction> jointActions = possibleJointActions.get(i);
 //			Joint<Double> jointPayoffs = new Joint<Double>();
 //			jointPayoffs.add((double) random.nextInt(100));
 //			jointPayoffs.add((double) random.nextInt(100));
 //			normalFormGame.addPayoffsForJointAction(jointActions, jointPayoffs);
 //		}
 //		System.out.println(normalFormGame);
 //		BimatrixHuSolver<GridAction> solver = new BimatrixHuSolver<GridAction>();
 //		GameSolution<GridAction> solution = solver.solve(normalFormGame);
 //		System.out.println(solution);
 //		System.out.println(solution.getJointActionDistribution());
 //		System.out.println(solution.getExpectedPayoffs());
 //	}
 
 	
 	@Override
 	public GameSolution<A> solve(NormalFormGame<A> normalFormGame) {
 		int numPlayers = normalFormGame.getNumPlayers();
 		
 		// TODO: What error handling to do if more than 2 players? 
 		if (numPlayers > 2) {
 			System.err.println("Bimatrix solver being used for " + numPlayers + " player game.");
 		}
 		
 		// Extract the payoff matrices for players 1 and 2.
 		double[][] player1Payoffs = getPayoffMatrixForPlayer(normalFormGame, player1Idx);
 		double[][] player2Payoffs = getPayoffMatrixForPlayer(normalFormGame, player2Idx);
 
 		// Solve the normal-form game
 		Joint<double[]> mixedStrategyPerPlayer = solveForMixedStrategies(player1Payoffs, player2Payoffs);
 		
 		// Return the solution in terms of the joint strategy.
 		double[] player1StrategyArr = mixedStrategyPerPlayer.getForPlayer(player1Idx);
 		double[] player2StrategyArr = mixedStrategyPerPlayer.getForPlayer(player2Idx);
 		DiscreteDistribution<Joint<A>> jointStrategy = getJointStrategyFromIndependentStrategies(normalFormGame, player1StrategyArr, player2StrategyArr);
 
 		// Compute expected payoffs		
 		double[][] jointOutcome = getDistributionOverJointActions(player1StrategyArr, player2StrategyArr);
 		double player1ActualExpectedPayoff = getExpectedPayoffsForPlayer(player1Payoffs, jointOutcome);
 		double player2ActualExpectedPayoff = getExpectedPayoffsForPlayer(player2Payoffs, jointOutcome);
 		Joint<Double> expectedPayoffs = new Joint<Double>();
 		expectedPayoffs.add(player1ActualExpectedPayoff);
 		expectedPayoffs.add(player2ActualExpectedPayoff);
 		
 		// No transfer payments
 		Joint<Double> transferPayments = new Joint<Double>();
 		transferPayments.add(0.0);
 		transferPayments.add(0.0);
 		
 		GameSolution<A> solution = new UncorrelatedGameSolution<A>(normalFormGame, jointStrategy, expectedPayoffs, transferPayments);
 		return solution;
 	}
 
 	
 	public static <A extends AbstractAction> DiscreteDistribution<Joint<A>> getJointStrategyFromIndependentStrategies(
 			NormalFormGame<A> normalFormGame,
 			double[] player1StrategyArr, double[] player2StrategyArr) {
 		// Get list of player actions.
 		List<A> player1Actions = normalFormGame.getPossibleActionsForPlayer(player1Idx);
 		List<A> player2Actions = normalFormGame.getPossibleActionsForPlayer(player2Idx);
 		int numPlayer1Actions = player1Actions.size();
 		int numPlayer2Actions = player2Actions.size();
 
 		DiscreteDistribution<Joint<A>> jointStrategy = new DiscreteDistribution<Joint<A>>();
 		for (int player1ActionIndex=0; player1ActionIndex<numPlayer1Actions; player1ActionIndex++) {		
 			for (int player2ActionIndex=0; player2ActionIndex<numPlayer2Actions; player2ActionIndex++) {
 				double probAction1 = player1StrategyArr[player1ActionIndex];
 				double probAction2 = player2StrategyArr[player2ActionIndex];
 				A player1Action = player1Actions.get(player1ActionIndex);
 				A player2Action = player2Actions.get(player2ActionIndex);
 				Joint<A> jointAction = new Joint<A>();
 				jointAction.add(player1Action);
 				jointAction.add(player2Action);
 				jointStrategy.add(jointAction, probAction1*probAction2);
 			}
 		}
 
 		// TODO Auto-generated method stub
 		return jointStrategy;
 	}
 
 
 	public static <A extends AbstractAction> double[][] getPayoffMatrixForPlayer(
 			NormalFormGame<A> normalFormGame, int playerIdx) {
 		
 		// Get list of player actions.
 		List<A> player1Actions = normalFormGame.getPossibleActionsForPlayer(player1Idx);
 		List<A> player2Actions = normalFormGame.getPossibleActionsForPlayer(player2Idx);
 		int numPlayer1Actions = player1Actions.size();
 		int numPlayer2Actions = player2Actions.size();
 		
 		// Create payoff matrix.
 		double[][] payoffsForPlayer = new double[numPlayer1Actions][numPlayer2Actions];
 		for (int player1ActionIndex=0; player1ActionIndex<numPlayer1Actions; player1ActionIndex++) {
 			for (int player2ActionIndex=0; player2ActionIndex<numPlayer2Actions; player2ActionIndex++) {
 				A player1Action = player1Actions.get(player1ActionIndex);
 				A player2Action = player2Actions.get(player2ActionIndex);
 				Joint<A> jointAction = new Joint<A>();
 				jointAction.add(player1Action);
 				jointAction.add(player2Action);
 				Joint<Double> payoffsPerPlayer = normalFormGame.getPayoffsForJointAction(jointAction);
 				payoffsForPlayer[player1ActionIndex][player2ActionIndex] = payoffsPerPlayer.get(playerIdx);
 			}				
 		}
 		return payoffsForPlayer;
 	}
 
 
 //	public double[] getEquilibriumStrategyForPlayer(int playerIdx) {
 //		int equilibriumIdxToConsider = 0; // always consider the first equilibrium found.
 //		double[] eqmStrategyForPlayer = null;
 //		if (playerIdx==0) {
 //			eqmStrategyForPlayer = new double[row];
 //			for (int actionIdx=0; actionIdx<row; actionIdx++) {
 //				eqmStrategyForPlayer[actionIdx] = Z[equilibriumIdxToConsider][actionIdx];
 //			}
 //		} else if (playerIdx==1) {
 //			eqmStrategyForPlayer = new double[col];
 //			for (int actionIdx=0; actionIdx<col; actionIdx++) {
 //				eqmStrategyForPlayer[actionIdx] = Z[equilibriumIdxToConsider][row+actionIdx];
 //			}
 //		}
 //		return eqmStrategyForPlayer;
 //	}
 
 	
 
     
 	public static Joint<double[]> solveForMixedStrategies(
 			double[][] player1Payoffs,
 			double[][] player2Payoffs) {
 		int numPlayer1Actions = player1Payoffs.length; // row
 		int numPlayer2Actions = player1Payoffs[0].length; // col
 		int numTotalActions = numPlayer1Actions + numPlayer2Actions; // dimM
 		
 		// Each row is a different equilibrium strategy. Each column gives the probability of 
 		// player 1 playing each of its actions, followed by the probability of player 2 playing 
 		// each of its actions.
 		double[][] equilibriumMixedStrategies = new double[numPlayer1Actions][numTotalActions]; 
 		getnash(player1Payoffs, player2Payoffs, equilibriumMixedStrategies, numPlayer1Actions, numPlayer2Actions, numTotalActions);
 		// Get the equilibrium strategies for the two players
 		int equilibriumIdxToConsider = 0; // always consider the first equilibrium found.
 		double[] eqmStrategyForPlayer1 = new double[numPlayer1Actions];
 		for (int actionIdx=0; actionIdx<numPlayer1Actions; actionIdx++) {
 			eqmStrategyForPlayer1[actionIdx] = equilibriumMixedStrategies[equilibriumIdxToConsider][actionIdx];
 		}
 		double[] eqmStrategyForPlayer2 = new double[numPlayer2Actions];
 		for (int actionIdx=0; actionIdx<numPlayer2Actions; actionIdx++) {
 			eqmStrategyForPlayer2[actionIdx] = equilibriumMixedStrategies[equilibriumIdxToConsider][numPlayer1Actions+actionIdx];
 		}
 		
 		Joint<double[]> mixedStrategies = new Joint<double[]>();
 		mixedStrategies.add(eqmStrategyForPlayer1);
 		mixedStrategies.add(eqmStrategyForPlayer2);
 		return mixedStrategies;
 	}
 	
 	public static double[][] getDistributionOverJointActions(double[] player1Mix, double[] player2Mix) {
 		int numPlayer1Actions = player1Mix.length; 
 		int numPlayer2Actions = player2Mix.length;
 		double[][] jointActionDistribution = new double[numPlayer1Actions][numPlayer2Actions];
 		for (int action1Idx=0; action1Idx<numPlayer1Actions; action1Idx++) {
 			for (int action2Idx=0; action2Idx<numPlayer2Actions; action2Idx++) {
 				double probAction1 = player1Mix[action1Idx];
 				double probAction2 = player2Mix[action2Idx];
 				jointActionDistribution[action1Idx][action2Idx] = probAction1 * probAction2; 
 			}
 		}
 		return jointActionDistribution;
 	}
 	
 	/**
 	 * Computes the expected payoff for a player, given that player's payoff matrix and the probability of each
 	 * outcome.
 	 * @param playerPayoffMatrix
 	 * @param outcomeProbability
 	 * @return
 	 */
 	public static double getExpectedPayoffsForPlayer(double[][] playerPayoffMatrix, double[][] outcomeProbability) {
 		int numPlayer1Actions = playerPayoffMatrix.length;
 		int numPlayer2Actions = playerPayoffMatrix[0].length;
 		double expectedPayoff = 0;
 		for (int action1=0; action1<numPlayer1Actions; action1++) {
 			for (int action2=0; action2<numPlayer2Actions; action2++) {
 				expectedPayoff += playerPayoffMatrix[action1][action2] * outcomeProbability[action1][action2];
 			}
 		}
 		return expectedPayoff;
 	}
 	
 	
 	
 	
     
     private static void getnash(double A[][] , double B[][],double Z[][], int row, int col, int dimM){
 		
 		double LA[][];
 		double LB[][];
 		double LZ[][];
 		double M[][];
 		int WList[][],ZList[][];
 		double Q[];
 		
 		
 		
 		// initial Q and Z, WList, ZList	
 		LA = new double[row][col];
 		LB = new double[row][col];
 		M = new double[dimM][dimM];
 		LZ = new double[row][dimM];
 		WList = new int[dimM][2];
 		ZList = new int[dimM][2];
 		Q = new double[dimM];
 		
 		/*Transform the payoff matrix into cost matrix*/
 		/* LA = -A , LB = -B */
 		Multiple2(A,LA,row,col,-1); 
 		Multiple2(B,LB,row,col,-1);  
 		
 		/* find more than one Nash equilibrium solution*/
 		for(int k = 0;k<row;k++){
 			AllClear(WList,ZList,Q,LZ,k,dimM);    // initialize 
 			Comp(LA,LB,M,row,col,dimM);
 			getonenash(M,Q,WList,ZList,k,LA,LB,LZ,row,col,dimM); // get one nash solution
 			Multiple1(LZ,Z,dimM,1,k);
 		}
 		//QMprint(LB,row,col);
 	}
     
     private static void AllClear(int WList[][],int ZList[][],double Q[],double LZ[][],int k, int dimM){
 		// Define index matrix WList and ZList,Q
 	
 		for (int i=0;i<dimM;i++){
 			WList[i][1] = i; WList[i][0] =1;  //"W"
 			ZList[i][1] = i; ZList[i][0] =2;  // "z"
 			Q[i] = -1;
 			LZ[k][i] = 0;
 		}
     }
 	
 	public static void getonenash(double M[][],double Q[],int WList[][],int ZList[][],int k, double LA[][],double LB[][], double LZ[][], int row, int col, int dimM){
 		int c=0,r=0,j1;	
 		int oldWList[][];
 		oldWList = new int[dimM][2];
 	 		
 		// find c be the best action agent 2 can take
 		c=find_min_col(LB,k,col);
 		
 		// Pivot get new q, M and exchange-elements
 		//QMprint(M,dimM,dimM);
 		Pivot(Q,M,c+row,k,dimM);	    
 		Exchange_element(WList,ZList,c+row,k);
 		
 		//if(k==0) QMprint(M,dimM,dimM);
 		// find r is the best action agent 1 can take against c
 		r = find_min_row(LA,c,row);		
 	
 		//Pivot get new q, M and exchange-elements
 		Pivot(Q,M,r,c+row,dimM); 
 		Exchange_element(WList,ZList,r,c+row);
 		//if(k==2) QMprint(M,dimM,dimM);
 		//System.out.print("c=" +c +"r="+r+"\n");
 		// get Z for k
 		if (r==k){  // we have the solution
 			get_final_z(WList,Q,LZ,k,dimM);
 		}
 		else{
 			j1 = find_min_ratio(Q,M,r,dimM);
 			//System.out.println("j1="+j1);
 			if(j1==-1) {              //we find the solution
 				get_final_z(WList,Q,LZ,k,dimM);
 			}
 			else{
 				// get to original step 
 				Pivot(Q,M,j1,r,dimM);
 				if (k==2) QMprint(M,dimM,dimM);
 				while(j1!=-1&&WList[j1][1]!=k){
 					//System.out.println("In while loop... j1=" + j1 + ", WList[j1][1]=" + WList[j1][1] + ", k=" + k);
 					
 					//copy Wlist to oldWlist
 					Multiple2(WList,oldWList,dimM,2,1);  
 					Exchange_element(WList,ZList, j1,r);
 					r = find_complement(j1,oldWList,ZList,dimM);
 					
 					// go back to step 1. r is the new driving variable
 					j1 = find_min_ratio(Q,M,r,dimM); 
 					if ( j1!=-1) { Pivot(Q,M,j1,r,dimM);
 					}
 					//System.out.print(j1);
 				}
 				if(j1!=-1 ) Exchange_element(WList,ZList,j1,r);
 				get_final_z(WList,Q,LZ,k,dimM);
 			}
 		}	   
 		normalize(LZ,k,row,dimM);
     }
 	
     // calculate  A1 = alpha * A0 , scalar multiplication
     public static void Multiple2(double A0[][], double A1[][],int s,int t,double p){
 		
 		for (int i=0;i<s;i++){
 			for (int j=0;j<t;j++){
 				A1[i][j] = p*A0[i][j];
 			}
 		}
     }
 	
 	// calculate  A1 = alpha * A0 , scalar multiply, A0 and A1 are integers
 	public static void Multiple2(int A0[][], int A1[][],int s,int t,int prod){
 		
 		for (int i=0;i<s;i++){
 			for (int j=0;j<t;j++){
 				A1[i][j] = prod*A0[i][j];
 			}
 		}
     }
 	
     // calculate  A1[k] = alpha * A0[k], scalar multiply on one row
     public static void Multiple1(double A0[][], double A1[][],int t,double product,int k)
     {
 		for (int i=0;i<t;i++){ 
 			A1[k][i] = product*A0[k][i];
 		}
     }
     
 	// construct the M matrix
     public static void Comp(double A0[][],double A1[][],double M1[][], int row, int col, int dimM){
 		// want M be a positive matrix at the beginning
 		double MaxA,MaxB;
 		MaxA = find_abs_max(A0, row, col);MaxB = find_abs_max(A1, row, col); 
 				
 		for (int i=0;i<dimM;i++){
 			for (int j=0;j<dimM;j++){
 				// M(i,j) = 0 , i<m&j<m, i>m&j>m 
 				if ((i<row&&j<row)||(i>=row&&j>=row)){
 					M1[i][j] = 0;
 				}
 				else if (j>=row&&i<row) {
 					M1[i][j] = A0[i][j-row]+ MaxA +1 ;
 				}
 				else {
 					M1[i][j] = A1[j][i-row]+ MaxB + 1;   // transpose
 				}
 			}
 		}
     }
 	
 	// find the absolute maxium of Matrix A0
     public static double find_abs_max(double A0[][], int row, int col){
 		double Max=0;
 		double temp;
 		
 		for(int i=0;i<row;i++){
 			for(int j=0;j<col;j++){
 				temp= Math.abs(A0[i][j]);
 				if ( temp > Max) Max = temp;
 			}
 		}
 		return Max;
 	}
 	
 	public static int find_min_col(double L1[][],int k, int col){
 		double min = 200000;
 		int c=0;
    	    for(int j=0;j<col;j++){
 			if (L1[k][j]<min) {
 				min = L1[k][j];
 				c = j;
 			}
 	    }
 		return c;
 	}
 	
 	public static int find_min_row(double L1[][],int c, int row){
 		double min = 200000;
 		int r=0;
 		for(int i=0;i<row;i++){
 				if (L1[i][c]<min) {
 					min = L1[i][c];
 					r = i;
 				}
 		}
 		return r;
 	}
 	
 	public static void Pivot(double Q1[],double M1[][], int r1, int c1, int dimM){
 		
 		double pPoint ;
 		double MLocal[][];
 		double QLocal[];
 		MLocal = new double[dimM][dimM];
 		QLocal = new double[dimM];
 		
 		// the pivot point
 		pPoint = M1[r1][c1];
 		
 		if (Math.abs(pPoint) <= 0.000000001) {
 			return ;}
 		
 		for (int i=0;i<dimM;i++){
 			if (i==r1) QLocal[i] = -Q1[r1]/pPoint ;
 			else QLocal[i] = Q1[i] - (Q1[r1]/pPoint)*M1[i][c1];
 			
 			for (int j=0;j<dimM;j++){
 				if (i==r1){
 					if(j==c1) MLocal[i][j] = 1.0/pPoint;
 					else MLocal[i][j] = - M1[r1][j] /pPoint;
 				}
 				else {
 		    if(j==c1) MLocal[i][j] = M1[i][c1]/pPoint;
 		    else MLocal[i][j] = M1[i][j] -(M1[i][c1]/pPoint)*M1[r1][j];
 				}
 			}
 		}
 		for ( int i=0;i<dimM;i++) Q1[i] = QLocal[i];
 		  Multiple2(MLocal,M1,dimM,dimM,1);
     }
 	
 	//exchange rth row of WList and cth row of ZList 
     public static void  Exchange_element(int WList1[][],int ZList1[][],int r,int c){
 		int temp1,temp2;	
 		temp1 = WList1[r][0]; temp2  = WList1[r][1];
 		WList1[r][0]= ZList1[c][0]; WList1[r][1]=ZList1[c][1];
 		ZList1[c][0]= temp1; ZList1[c][1] = temp2;
     }
 
 	// This function is modified, get the final solution
     public static void  get_final_z(int WList1[][],double Q1[],double Z1[][],int k, int dimM){
 		
 		int j;
 		for (int i=0;i<dimM;i++){
 	    if (WList1[i][0] == 2) { j=WList1[i][1]; Z1[k][j] = Q1[i];}
 	    
 		}
     }
 	
 	// minumum ratio test
     public static int find_min_ratio(double Q1[],double M1[][], int r, int dimM){
     	double min;
     	double R[];
     	int j=-1;
     	double PostZero = 20000000;
     	R = new double[dimM];
 
     	//System.out.print("loop in min_ratio   ");
     	for (int i=0;i<dimM;i++){
     		if (M1[i][r]<-0.00001) R[i] = -Q1[i]/M1[i][r];
     		else R[i] = PostZero;
     	}
     	min = PostZero;
 
     	// Old method:
//    	for (int i=0;i<dimM;i++){
//    		if(R[i]!=PostZero&&R[i]<min){
//    			min = R[i]; j=i;
//    		}
//    	}
     	
     	// New method:
     	// Need to handle degenerate cases. See Section 3 in:
     	// http://www.public.iastate.edu/~riczw/MEGliter/general/Lemkehowson.pdf
     	// Break ties randomly
     	List<Integer> minIndices = new ArrayList<Integer>();
     	double myMin = PostZero;
     	for (int i=0;i<dimM;i++){
     		if(R[i]!=PostZero&&R[i]<=myMin){
     			myMin = R[i];
     			// If strictly lower, clear out old mins
     			if (R[i]<myMin) {
     				minIndices.clear();
     			}
     			minIndices.add(i);
     		}
     	}
     	// Choose amongs the min indices
     	//System.out.println("minIndices=" + minIndices);
    	Random random = new Random(); // TODO: Don't create a new random every time.
     	int numTies = minIndices.size();
     	if (numTies > 0) {
     		j = minIndices.get( random.nextInt(numTies) );
     	}
     	return j;
     }
 
 	// returns the position index of the complement of Wlistj in Zlist 
     public static int find_complement(int j,int WList1[][],int ZList1[][], int dimM){
 		int l = -1;
 		int temp1,temp2;
 		
 		temp1 = WList1[j][0] ;
 		temp1 = 3-temp1;   /* (w,a)-> (z,a)  (z,a) ->(w,a) */
 		temp2 = WList1[j][1] ;
 		
 		for (int i=0;i<dimM;i++){
 			if (ZList1[i][0]==temp1&&ZList1[i][1]==temp2){
 				l = i;
 				break;
 			}
 		}
 		
 		return l;
     }
 
 
     public static void QMprint(double M[][],int row,int col){
 //		for (int i=0;i<row;i++){
 //			for (int j=0;j<col;j++){
 //				System.out.print(M[i][j]+" ");
 //			}
 //			//	    System.out.print("     " +Q[i]);
 //			System.out.print("\n");
 //		}
     }
 	
 	public static void QMprint(int M[][],int row,int col){
 //		for (int i=0;i<row;i++){
 //			for (int j=0;j<col;j++){
 //				System.out.print(M[i][j]+" ");
 //			}
 //			//	    System.out.print("     " +Q[i]);
 //			System.out.print("\n");
 //		}
     }
 	
 	
     public static void normalize(double LZ1[][],int k, int row, int dimM) {
 		double sum1=0 ,sum2 = 0;
 		for (int i=0;i<row;i++)
 			sum1 = sum1+ Math.abs(LZ1[k][i]);
 		for (int i=row;i<dimM;i++)
 			sum2 = sum2+ Math.abs(LZ1[k][i]);
 		for (int i=0;i<dimM;i++){
 			if (i<row) LZ1[k][i]= Math.abs(LZ1[k][i])/sum1;
 			else  LZ1[k][i]= Math.abs(LZ1[k][i])/sum2;
 		}
     }
 
 
 
 }
