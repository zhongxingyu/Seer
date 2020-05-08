 package com.difane.games.ticktacktoe;
 
 import java.util.Random;
 import java.util.Vector;
 
 public class GameLogic {
 
 	static public final int AI_LEVEL_EASY = 0;
 	static public final int AI_LEVEL_HARD = 1;
 
 	static public final int FIELD_EMPTY = 0;
 	static public final int FIELD_X = 1;
 	static public final int FIELD_O = 4;
 
 	static public final int GAME_STATUS_NOT_COMPLETED = 0;
 	static public final int GAME_STATUS_X_WINS = 1;
 	static public final int GAME_STATUS_O_WINS = 4;
 	static public final int GAME_STATUS_DRAW = 3;
 
 	private int aiType; // FIELD_X or FIELD_O
 	private int humanType; // FIELD_X or FIELD_O
 	private int aiLevel;
 
 	private int[] fields = new int[10];
 	// private int[] fields = {0, 1, 1, 4, 4, 4, 1, 1, 1, 4};
 	private int[] ratings = new int[10];
 
 	private int[][] statusHelper = { { 0, 0, 0, 0 }, { 0, 1, 2, 3 },
 			{ 0, 4, 5, 6 }, { 0, 7, 8, 9 }, { 0, 1, 4, 7 }, { 0, 2, 5, 8 },
 			{ 0, 3, 6, 9 }, { 0, 1, 5, 9 }, { 0, 3, 5, 7 } };
 
 	private int[][] kle = { { 0, 0, 0, 0, 0 }, { 0, 1, 4, 7, 0 },
 			{ 0, 1, 5, 0, 0 }, { 0, 1, 6, 0, 8 }, { 0, 2, 4, 0, 0 },
 			{ 0, 2, 5, 7, 8 }, { 0, 2, 6, 0, 0 }, { 0, 3, 4, 0, 8 },
 			{ 0, 3, 5, 0, 0 }, { 0, 3, 6, 7, 0 } };
 
 	private int[] li = new int[9];
 
 	public GameLogic() {
 		aiLevel = AI_LEVEL_EASY;
 	}
 
 	/**
 	 * @param aiLevel
 	 *            the aiLevel to set
 	 */
 	public void setAiLevel(int aiLevel) {
 		this.aiLevel = aiLevel;
 	}
 
 	/**
 	 * Selects player order.
 	 * 
 	 * @return true, if Human goes first, false otherwise
 	 */
 	public boolean selectPlayersOrder() {
 		humanType = FIELD_X; // Now Human plays X
 		aiType = FIELD_O; // Now AI plays O
 		return true;
 	}
 
 	/**
 	 * Makes human turn
 	 * 
 	 * @param field
 	 *            Field (1 to 9), where turn was made
 	 * @return true if ok, false otherwise
 	 */
 	public boolean humanTurn(int field) {
 		if (fields[field] == FIELD_EMPTY) {
 			fields[field] = humanType;
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Makes ai turn
 	 * 
 	 * @return Field (1 to 9), where turn was made
 	 */
 	public int aiTurn() {
 		int aiTurnResult = -1;
 		Random rand = new Random();
 
 		int m, r, mr, i;
 
 		m = 0;
 		r = 0;
 
 		if (aiLevel == AI_LEVEL_EASY) {
 			for (i = 1; i <= 9; i++) {
 				r = r + ratings[i];
 			}
 			mr = rand.nextInt(r) + 1;
 			for (i = 1; i <= 9; i++) {
 				mr = mr - ratings[i];
 				if (mr <= 0) {
 					fields[i] = aiType;
 					return i;
 				}
 			}
 		} else {
 			for (i = 1; i <= 9; i++) {
 				if (ratings[i] > r) {
 					r = ratings[i];
 				}
 			}
 			for (i = 1; i <= 9; i++) {
 				if (ratings[i] == r) {
 					m = m + 1;
 				}
 			}
 			mr = rand.nextInt(m) + 1;
 			m = 0;
 
 			for (i = 1; i <= 9; i++) {
 				if (ratings[i] == r) {
 					m = m + 1;
 					if (m == mr) {
 						fields[i] = aiType;
 						return i;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Returns game status
 	 * 
 	 * @return one of the GAME_STATUS_* values
 	 */
 	public int getGameStatus() {
 
 		int result = 0;
 
 		int ni;
 		int sa;
 		int so;
 		int sj;
 
 		ni = 5;
 
 		for (int i = 1; i <= 8; i++) {
 			sa = 5;
 			so = 0;
 			li[i] = 0;
 			for (int j = 1; j <= 3; j++) {
 				sj = fields[statusHelper[i][j]];
 				sa = sa & sj;
 				so = so | sj;
 				li[i] = li[i] + sj;
 			}
 			result = result | sa;
 			ni = ni & so;
 		}
 		if (ni == 5) {
 			result = 3;
 		}
 
 		return result;
 	}
 
 	private void calculateRating() {
 		int[] s00 = new int[4];
 		int[] s11 = new int[4];
 		int[] s44 = new int[4];
 		int s0, s1, s4, ssj, ii, j, jj, jjj, sj;
 
 		for (ii = 1; ii <= 9; ii++) {
 			ratings[ii] = 0;
 		}
 
 		for (ii = 1; ii <= 9; ii++) {
 			if (fields[ii] == FIELD_EMPTY) {
 				ratings[ii] = ratings[ii] + 1;
 				s0 = 0;
 				s1 = 0;
 				s4 = 0;
 				for(j =1; j <=4; j++)
 				{
 					ssj = kle[ii][j];
 					if(ssj != 0)
 					{
 						switch (li[ssj]) {
 						case 0:
 							s00[s0] = ssj;
 							s0 = s0 + 1;
 							for (jj = 0; jj <= s4 - 1; jj++) {
 								for (jjj = 1; jjj <= 3; jjj++) {
 									sj = statusHelper[ssj][jjj];
 									if (sj != ii) {
 										ratings[sj] = ratings[sj] + 100;
 									}
 								}
 							}
 
 							for (jj = 0; jj <= s1 - 1; jj++) {
 								for (jjj = 1; jjj <= 3; jjj++) {
 									sj = statusHelper[ssj][jjj];
 									ratings[sj] = ratings[sj] + 10;
 								}
 								for (jjj = 1; jjj <= 3; jjj++) {
 									sj = statusHelper[s11[jj]][jjj];
 									if (sj != ii && fields[sj] == FIELD_EMPTY) {
 										ratings[sj] = ratings[sj] + 10;
 									}
 								}
 							}
 							break;
 						case 1:
 							s11[s1] = ssj;
 							s1 = s1 + 1;
 							if (s1 > 1) {
 								ratings[ii] = ratings[ii] + 1000;
 								for (jj = 0; jj <= s1 - 1; jj++) {
 									for (jjj = 1; jjj <= 3; jjj++) {
 										sj = statusHelper[s11[jj]][jjj];
 										if (sj != ii
 												&& fields[sj] == FIELD_EMPTY) {
 											ratings[sj] = ratings[sj] + 1000;
 										}
 									}
 								}
 							}
 							for (jj = 0; jj <= s0 - 1; jj++) {
 								for (jjj = 1; jjj <= 3; jjj++) {
 									sj = statusHelper[ssj][jjj];
 									if (fields[sj] == FIELD_EMPTY) {
 										ratings[sj] = ratings[sj] + 10;
 									}
 								}
 								for (jjj = 1; jjj <= 3; jjj++) {
 									sj = statusHelper[s00[jj]][jjj];
 									if (sj != ii && fields[sj] == FIELD_EMPTY) {
 										ratings[sj] = ratings[sj] + 10;
 									}
 								}
 							}
 							break;
 						case 2:
 							ratings[ii] = ratings[ii] + 100000;
 							break;
 						case 4:
 							s44[s4] = ssj;
 							s4 = s4+1;
 							if(s4 > 1)
 							{
 								ratings[ii] = ratings[ii] + 10000;
 							}
 							for (jj = 0; jj <= s0 - 1; jj++) {
 								for (jjj = 1; jjj <= 3; jjj++) {
 									sj = statusHelper[s00[jj]][jjj];
 									if(sj != ii)
 									{
 										ratings[sj] = ratings[sj] + 100;
 									}
 								}
 							}
 							break;
 						case 8:
 							ratings[ii] = ratings[ii] + 1000000;
 							break;
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 }
