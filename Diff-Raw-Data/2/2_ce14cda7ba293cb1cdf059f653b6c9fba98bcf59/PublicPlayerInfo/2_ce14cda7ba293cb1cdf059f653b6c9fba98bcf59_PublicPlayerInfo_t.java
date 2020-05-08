 /*
 Open Meerkat Testbed. An open source implementation of the Meerkat API for running poker games
 Copyright (C) 2010  Dan Schatzberg
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
 
 package game;
 
 import com.biotools.meerkat.Action;
 import com.biotools.meerkat.GameInfo;
 import com.biotools.meerkat.Hand;
 import com.biotools.meerkat.PlayerInfo;
 
 public class PublicPlayerInfo implements PlayerInfo {
 
 	private GameInfo game;
 	private String name = "";
 	private double money = 0.0D;
 	private double amountInPot = 0.0D;
 	private double amountInPotThisRound = 0.0D;
 	private boolean actedThisRound = false;
 	private boolean showCards = false;
 	private boolean folded = false;
 	private boolean sittingOut = false;
 	private boolean won = false;
 	private Hand holeCards = null;
 	private int action = 0;
 	
 	public static PublicPlayerInfo create(String name, double bankroll) {
 		PublicPlayerInfo player = new PublicPlayerInfo();
 		player.setName(name);
 		player.setBankroll(bankroll);
 		return player;
 	}
 
 	@Override
 	public double getAmountCallable() {
 		return Math.min(getBankRoll(),getAmountToCall());
 	}
 
 	@Override
 	public double getAmountInPot() {
 		return this.amountInPot;
 	}
 
 	@Override
 	public double getAmountInPotThisRound() {
 		return this.amountInPotThisRound;
 	}
 
 	@Override
 	public double getAmountRaiseable() {
 		return isActive() ? getBankRoll()-getAmountToCall() : 0.0D;
 	}
 
 	@Override
 	public double getAmountToCall() {
 		return getGameInfo().getAmountToCall(getSeat());
 	}
 
 	@Override
 	public double getBankRoll() {
 		return this.money;
 	}
 
 	@Override
 	public double getBankRollAtRisk() {
 		return getGameInfo().getBankRollAtRisk(getSeat());
 	}
 
 	@Override
 	public double getBankRollAtStartOfHand() {
 		return this.getBankRoll()-this.getAmountInPot();
 	}
 
 	@Override
 	public double getBankRollInSmallBets() {
 		return getBankRoll() / getGameInfo().getBigBlindSize();
 	}
 
 	@Override
 	public GameInfo getGameInfo() {
 		return this.game;
 	}
 
 	@Override
 	public int getLastAction() {
 		return this.action;
 	}
 
 	@Override
 	public String getName() {
 		return this.name;
 	}
 
 	@Override
 	public double getNetGain() {
 		return this.won ? getGameInfo().getEligiblePot(getSeat()) - getAmountInPot() - getGameInfo().getRake() : -1 * getAmountInPot();
 	}
 
 	@Override
 	public double getRaiseAmount(double amountToRaise) {
 		return Math.min(amountToRaise, getAmountRaiseable());
 	}
 
 	@Override
 	public Hand getRevealedHand() {
 		return this.showCards ? this.holeCards : null;
 	}
 
 	@Override
 	public int getSeat() {
 		return getGameInfo().getPlayerSeat(getName());
 	}
 
 	@Override
 	public boolean hasActedThisRound() {
 		return this.actedThisRound;
 	}
 
 	@Override
 	public boolean hasEnoughToRaise() {
 		return getAmountRaiseable() > 0.0D;
 	}
 
 	@Override
 	public boolean inGame() {
 		return !isSittingOut();
 	}
 
 	@Override
 	public boolean isActive() {
 		return inGame() && !getGameInfo().isGameOver() && !isFolded();
 	}
 
 	@Override
 	public boolean isAllIn() {
 		return getBankRoll() == 0 && isActive();
 	}
 
 	@Override
 	public boolean isButton() {
 		return getGameInfo().getButtonSeat() == getSeat();
 	}
 
 	@Override
 	//Note that the meerkat API is very ambiguous. For isCommitted(seat) in GameInfo it is true if anything was invested in the current Round
 	//for isCommitted it is true if anything was voluntarily committed this round
 	
 	//I have taken it to mean anything invested (not voluntarily) this round.
 	public boolean isCommitted() {
 		return this.getAmountInPotThisRound() != 0;	
 	}
 
 	@Override
 	public boolean isFolded() {
 		return this.folded;
 	}
 
 	@Override
 	public boolean isSittingOut() {
 		return this.sittingOut;
 	}
 	
 	public void setGame(GameInfo gi) {
 		this.game = gi;
 	}
 	
 	public void setName(String n) {
 		this.name = n;
 	}
 	
 	public void setBankroll(double bankroll) {
 		this.money = bankroll;
 	}
 	
 	public void setCards(Hand hole) {
 		this.holeCards = hole;
 	}
 	
 	public void wonHand(double amount) {
 		this.won = true;
 		this.money += amount;
 	}
 	
 	public void newHand() {
 		newRound();
 		this.amountInPot = 0.0D;
 		this.showCards = false;
 		this.folded = false;
 		this.won = false;
 		this.holeCards = null;
 		this.action = Action.INVALID;
 	}
 	
 	public void newRound() {
 		this.actedThisRound = false;
 		this.amountInPotThisRound = 0.0D;
 	}
 	
 	public void update(Action act, double param) {
		if (getGameInfo().getCurrentPlayerSeat() != getSeat())
 			throw new IllegalArgumentException("Is not player's turn to act");
 		if(act.isAllInPass()) {
 			putMoney(getBankRoll());
 			this.action = Action.ALLIN_PASS;
 		} else if (act.isAnte()) {
 			putMoney(getGameInfo().getAnte());
 			this.action = Action.POST_ANTE;
 		} else if (act.isBet()) {
 			putMoney(param);
 			this.action  = Action.BET;
 		} else if (act.isBigBlind()) {
 			putMoney(getGameInfo().getBigBlindSize());
 			this.action = Action.BIG_BLIND;
 		} else if (act.isCall()) {
 			putMoney(Math.min(getAmountToCall(),getBankRoll()));
 			this.action = Action.CALL;
 		} else if (act.isCheck()) {
 			this.action = Action.CHECK;
 		} else if (act.isFold()) {
 			this.folded = true;
 			this.action = Action.FOLD;
 		} else if (act.isMuck()) {
 			this.folded = true;
 			this.action = Action.MUCK;
 		} else if (act.isRaise()) {
 			putMoney(param);
 			this.action = Action.RAISE;
 		} else if (act.isSitout()) {
 			this.sittingOut = true;
 			this.action = Action.SIT_OUT;
 		} else {
 			throw new IllegalArgumentException("Invalid action, possibly unimplemented");
 		}
 		this.actedThisRound = true;
 	}
 	
 	private void putMoney(double amount) {
 		this.amountInPotThisRound += amount;
 		this.amountInPot += amount;
 		this.money -= amount;
 	}
 }
