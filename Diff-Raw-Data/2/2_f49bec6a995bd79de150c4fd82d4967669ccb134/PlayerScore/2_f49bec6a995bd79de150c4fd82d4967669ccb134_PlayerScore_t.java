 package org.lacrise.engine.game;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PlayerScore {
 
 	/**
 	 * Score is null until player starts.
 	 */
 	private Integer mTotal;
 
 	private boolean mHasZero;
 
 	private List<Turn> mTurnList;
 
 	private List<Penalty> mPenaltyList = new ArrayList<Penalty>();
 
 	public PlayerScore() {
 		mTurnList = new ArrayList<Turn>();
 		mPenaltyList = new ArrayList<Penalty>();
 	}
 
 	public Integer getTotal() {
 		return mTotal;
 	}
 
 	public void setTotal(Integer total) {
 		this.mTotal = total;
 	}
 
 	public boolean hasZero() {
 		return mHasZero;
 	}
 
 	public void setHasZero(boolean hasZero) {
 		this.mHasZero = hasZero;
 	}
 
 	public List<Turn> getTurnList() {
 		return mTurnList;
 	}
 
 	public void addTurn(Turn turn) {
 		mTurnList.add(turn);
 	}
 
 	public List<Penalty> getPenaltyList() {
 		return mPenaltyList;
 	}
 
 	public void setPenaltyList(List<Penalty> penaltyList) {
 		this.mPenaltyList = penaltyList;
 	}
 
 	public String toString() {
 		StringBuilder playerString = new StringBuilder();
 		playerString.append(this.getTotal());
 
 		return playerString.toString();
 	}
 
 }
