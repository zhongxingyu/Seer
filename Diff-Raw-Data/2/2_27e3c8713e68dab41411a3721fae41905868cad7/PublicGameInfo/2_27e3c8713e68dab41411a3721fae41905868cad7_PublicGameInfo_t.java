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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import util.Utils;
 
 import com.biotools.meerkat.Action;
 import com.biotools.meerkat.GameInfo;
 import com.biotools.meerkat.GameObserver;
 import com.biotools.meerkat.Hand;
 import com.biotools.meerkat.HandEvaluator;
 import com.biotools.meerkat.Holdem;
 import common.handeval.klaatu.PartialStageFastEval;
 
 public class PublicGameInfo implements GameInfo {
 
 	/**
 	 * this action-Type is used to inform observers about the return
 	 * of uncalled bets.<br>
 	 * We need this for the history-writer - if this information is missing
 	 * Holdem-Manager gets confused 
 	 */
 	public static final int SPECIAL_ACTION_RETURNUNCALLEDBET = 100;
 	
 	//limit definitions
 	public static final int FIXED_LIMIT = 0;
 	public static final int POT_LIMIT = 1;
 	public static final int NO_LIMIT = 2;
 
 	private boolean reverseBlinds = false;
 	private boolean simulation = false;
 	private boolean zipMode = false;
 	private int buttonSeat = -1;
 	private int smallBlindSeat = -1;
 	private int bigBlindSeat = -1;
 	private int toAct = 0; //next player to act (-1 if no one to act)
 	private int numRaises = 0; //number of raises THIS ROUND
 	private int numWinners = 0;
 	private int stage = 0;
 	private int limit = 0;
 	private double bet = 0.0D;
 	private double ante = 0.0D;
 	private double smallBlind = 0.0D;
 	private double bigBlind = 0.0D;
 	private double minRaise = 0.0D;
 	private long gameID = 0L;
 	private PotManager potManager;
 	private String logDirectory = "";
 	private Hand boardCards = null;
 	private PublicPlayerInfo[] players = new PublicPlayerInfo[0];
 	private List<GameObserver> gameObservers = new ArrayList<GameObserver>();
 	private int lastToAct = 0;
 	private int lastAggressor = -1;
 
 	public PublicGameInfo() {
 		HandEvaluator.setHandEval(new HandEvalImpl());
 	}
 
 	@Override
 	public boolean canRaise(int seat) {
 		return hasPlayerInSeat(seat) && getPlayer(seat).hasEnoughToRaise();
 	}
 
 	@Override
 	public double getAmountToCall(int seat) {
 		double highestAmount = 0.0D;
 		if (!isActive(seat)) {
 			return 0;
 		}
 		for (int i = 0; i < this.players.length; i++)
 			if (isCommitted(i))
 				highestAmount = Math.max(highestAmount, getPlayer(i).getAmountInPotThisRound());
 		double toCall = Utils.roundToCents(highestAmount - getPlayer(seat).getAmountInPotThisRound());
 		if (toCall > getPlayer(seat).getBankRoll()) {
 			toCall = getPlayer(seat).getBankRoll();
 		}
 		return toCall;
 	}
 
 	@Override
 	public double getAnte() {
 		return this.ante;
 	}
 
 	@Override
 	public double getBankRoll(int seat) {
 		return hasPlayerInSeat(seat) ? getPlayer(seat).getBankRoll() : 0.0D;
 	}
 
 	@Override
 	public double getBankRollAtRisk(int seat) {
 		double biggestBankroll = 0.0D;
 		for (int i = 0; i < getNumSeats(); i++)
 			biggestBankroll = Math.max(biggestBankroll, getBankRoll(i));
 		return Math.min(biggestBankroll, getBankRoll(seat));
 	}
 
 	@Override
 	public double getBetsToCall(int seat) {
 		return isValidSeat(seat) ? getAmountToCall(seat) / getCurrentBetSize() : 0.0D;
 	}
 
 	@Override
 	public int getBigBlindSeat() {
 		return bigBlindSeat;
 	}
 
 	@Override
 	public double getBigBlindSize() {
 		return this.bigBlind;
 	}
 
 	@Override
 	public Hand getBoard() {
 		return this.boardCards; //I wonder if this is OK. Can't anyone modify the board through this method? 
 	}
 
 	@Override
 	public int getButtonSeat() {
 		return this.buttonSeat;
 	}
 
 	@Override
 	public double getCurrentBetSize() {
 		return isFixedLimit() ? this.bet : 0.0D;
 	}
 
 	@Override
 	public int getCurrentPlayerSeat() {
 		return this.toAct;
 	}
 
 	@Override
 	public double getEligiblePot(int seat) {
 		if(!isActive(seat))
 			return 0.0D;
 		double eligiblePot = 0.0D;
 		for (int i = 0; i < getNumSeats(); i++)
 			if(i != seat)
 				eligiblePot += Math.min(getPlayer(i).getAmountInPot(),getPlayer(seat).getAmountInPot() + getBankRoll(seat));
		eligiblePot += getPlayer(seat).getAmountInPot();
 		return eligiblePot;
 	}
 
 	@Override
 	public long getGameID() {
 		return this.gameID;
 	}
 
 	@Override
 	public String getLogDirectory() {
 		return this.logDirectory;
 	}
 
 	@Override
 	public double getMainPotSize() {
 		return potManager.getPot(0).getValue();
 	}
 
 	@Override
 	public double getMinRaise() {
 		return this.minRaise;
 	}
 
 	@Override
 	public int getNumActivePlayers() {
 		int active = 0;
 		for (int i = 0; i < getNumSeats(); i++)
 			if (isActive(i))
 				active++;
 		return active;
 	}
 
 	@Override
 	public int getNumActivePlayersNotAllIn() {
 		int active = 0;
 		for (int i = 0; i < getNumSeats(); i++)
 			if (isActive(i) && !getPlayer(i).isAllIn())
 				active++;
 		return active;
 	}
 
 	@Override
 	public int getNumPlayers() {
 		int num = 0;
 		for (int i = 0; i < getNumSeats(); i++)
 			if (hasPlayerInSeat(i))
 				num++;
 		return num;
 	}
 
 	@Override
 	public int getNumRaises() {
 		return this.numRaises;
 	}
 
 	@Override
 	public int getNumSeats() {
 		return this.players.length;
 	}
 
 	@Override
 	public int getNumSidePots() {
 		return this.potManager.getNumPots() - 1;
 	}
 
 	@Override
 	public int getNumToAct() {
 		if (this.toAct == -1) {
 			return 0;
 		}
 		int currentToAct = this.toAct;
 		int numToAct = 0;
 		numToAct++; // currentPlayer always counts as 'toAct'
 		if (isPreFlop()) {
 			if (!getPlayer(getSmallBlindSeat()).hasActedThisRound()) {
 				currentToAct++;
 				numToAct++;
 			}
 			if (!getPlayer(getBigBlindSeat()).hasActedThisRound()) {
 				currentToAct++;
 				numToAct++;
 			}
 		}
 
 		while (currentToAct != this.lastToAct && currentToAct != -1) {
 			currentToAct = nextActivePlayer(currentToAct);
 			if (!getPlayer(currentToAct).isAllIn()) {
 				numToAct++;
 			}
 
 		}
 
 		return numToAct;
 	}
 
 	@Override
 	public int getNumWinners() {
 		return this.numWinners;
 	}
 
 	@Override
 	public int getNumberOfAllInPlayers() {
 		int allin = 0;
 		for (int i = 0; i < getNumSeats(); i++)
 			if (isValidSeat(i) && getPlayer(i).isAllIn())
 				allin++;
 		return allin;
 	}
 
 	@Override
 	public PublicPlayerInfo getPlayer(int seat) {
 		return isValidSeat(seat) ? this.players[seat] : null;
 	}
 
 	@Override
 	public PublicPlayerInfo getPlayer(String name) {
 		for (int i = 0; i < getNumSeats(); i++)
 			if (hasPlayerInSeat(i) && players[i].getName() == name)
 				return getPlayer(i);
 		return null;
 	}
 
 	@Override
 	public String getPlayerName(int seat) {
 		if (hasPlayerInSeat(seat))
 			return getPlayer(seat).getName();
 		return null;
 	}
 
 	@Override
 	public int getPlayerSeat(String name) {
 		for (int i = 0; i < getNumSeats(); i++)
 			if (name.equals(getPlayerName(i)))
 				return i;
 		return -1;
 	}
 
 	@Override
 	public List<PublicPlayerInfo> getPlayersInPot(double potSize) {
 		List<PublicPlayerInfo> list = new ArrayList<PublicPlayerInfo>();
 		for (int i = 0; i < getNumSeats(); i++)
 			if (isActive(i) && getPlayer(i).getAmountInPot() >= potSize)
 				list.add(getPlayer(i));
 		return list;
 	}
 
 	@Override
 	//Rake on a per-hand basis is not implemented yet
 	public double getRake() {
 		return 0.0D;
 	}
 
 	@Override
 	//index 0 is the first side pot
 	public double getSidePotSize(int index) {
 		if (index < 0 || index >= getNumSidePots())
 			return 0.0D;
 		return this.potManager.getPot(index + 1).getValue();
 	}
 
 	@Override
 	public int getSmallBlindSeat() {
 		return smallBlindSeat;
 	}
 
 	@Override
 	public double getSmallBlindSize() {
 		return this.smallBlind;
 	}
 
 	@Override
 	public int getStage() {
 		return this.stage;
 	}
 
 	@Override
 	//Meerkat is also really ambiguous about this method. I took it to mean the maximum amount a player put in the pot prior to this round.
 	//This would be equal to the amount a player had to put into the pot to be active and not all-in this round.
 	public double getStakes() {
 		double stakes = 0.0D;
 		for (int i = 0; i < getNumSeats(); i++)
 			stakes = Math.max(stakes, getPlayer(i).getAmountInPot() - getPlayer(i).getAmountInPotThisRound());
 		return stakes;
 	}
 
 	@Override
 	public double getTotalPotSize() {
 		return potManager.getTotalPotSize();
 	}
 
 	@Override
 	public int getUnacted() {
 		int unacted = 0;
 		for (int i = 0; i < getNumSeats(); i++)
 			if (hasPlayerInSeat(i) && !getPlayer(i).hasActedThisRound())
 				unacted++;
 		return unacted;
 	}
 
 	@Override
 	public boolean inGame(int seat) {
 		return hasPlayerInSeat(seat) && getPlayer(seat).inGame();
 	}
 
 	@Override
 	public boolean isActive(int seat) {
 		return hasPlayerInSeat(seat) && getPlayer(seat).isActive();
 	}
 
 	@Override
 	public boolean isCommitted(int seat) {
 		return hasPlayerInSeat(seat) && getPlayer(seat).isCommitted();
 	}
 
 	@Override
 	public boolean isFixedLimit() {
 		return this.limit == PublicGameInfo.FIXED_LIMIT;
 	}
 
 	@Override
 	public boolean isFlop() {
 		return this.stage == Holdem.FLOP;
 	}
 
 	@Override
 	public boolean isGameOver() {
 		return getNumWinners() != 0;
 	}
 
 	@Override
 	public boolean isNoLimit() {
 		return this.limit == PublicGameInfo.NO_LIMIT;
 	}
 
 	@Override
 	public boolean isPostFlop() {
 		return this.stage == Holdem.FLOP || this.stage == Holdem.TURN || this.stage == Holdem.RIVER || this.stage == Holdem.SHOWDOWN;
 	}
 
 	@Override
 	public boolean isPotLimit() {
 		return this.limit == PublicGameInfo.POT_LIMIT;
 	}
 
 	@Override
 	public boolean isPreFlop() {
 		return this.stage == Holdem.PREFLOP;
 	}
 
 	@Override
 	public boolean isReverseBlinds() {
 		return this.reverseBlinds;
 	}
 
 	@Override
 	public boolean isRiver() {
 		return this.stage == Holdem.RIVER;
 	}
 
 	@Override
 	public boolean isSimulation() {
 		return this.simulation;
 	}
 
 	@Override
 	public boolean isTurn() {
 		return this.stage == Holdem.TURN;
 	}
 
 	@Override
 	public boolean isZipMode() {
 		return this.zipMode;
 	}
 
 	@Override
 	public int nextActivePlayer(int seat) {
 		if (!isValidSeat(seat) || getNumActivePlayers() <= 0)
 			return -1;
 		int i;
 		for (i = nextPlayer(seat); i != seat && !isActive(i); i = nextPlayer(i))
 			;
 		return i != seat ? i : -1;
 	}
 
 	/**
 	 * return the next active player, or -1 of noone is active behind the current seat
 	 * @param seat
 	 * @return
 	 */
 	public int nextActivePlayerNotAllIn(int seat) {
 		if (!isValidSeat(seat) || getNumActivePlayersNotAllIn() <= 0)
 			return -1;
 		int i;
 		for (i = nextPlayer(seat); i != seat && (!isActive(i) || getPlayer(i).isAllIn()); i = nextPlayer(i))
 			;
 		return i != seat ? i : -1;
 	}
 
 	@Override
 	public int nextPlayer(int seat) {
 		if (!isValidSeat(seat) || getNumPlayers() <= 0)
 			return -1;
 		int i;
 		for (i = nextSeat(seat); i != seat && !hasPlayerInSeat(i); i = nextSeat(i))
 			;
 		return i;
 	}
 
 	@Override
 	public int nextSeat(int seat) {
 		return !isValidSeat(seat) ? -1 : (seat + 1) % getNumSeats();
 	}
 
 	public int previousSeat(int seat) {
 		return !isValidSeat(seat) ? -1 : (seat + getNumSeats() - 1) % getNumSeats();
 	}
 
 	@Override
 	public int previousPlayer(int seat) {
 		if (!isValidSeat(seat) || getNumPlayers() <= 0)
 			return -1;
 		int i;
 		for (i = previousSeat(seat); i != seat && !hasPlayerInSeat(i); i = previousSeat(i))
 			;
 		return i;
 	}
 
 	public int previousActivePlayer(int seat) {
 		if (!isValidSeat(seat) || getNumPlayers() <= 0)
 			return -1;
 		int i;
 		for (i = previousSeat(seat); i != seat && !isActive(i); i = previousSeat(i))
 			;
 		return i;
 	}
 
 	/**
 	 * Whether or not seat is a valid seat number.
 	 * @param seat
 	 * @return
 	 */
 	private boolean isValidSeat(int seat) {
 		return (seat >= 0) && (seat < getNumSeats());
 	}
 
 	private boolean hasPlayerInSeat(int seat) {
 		return getPlayer(seat) != null;
 	}
 
 	public void setReverseBlinds(boolean rev) {
 		this.reverseBlinds = rev;
 	}
 
 	public void setSimulation(boolean sim) {
 		this.simulation = sim;
 	}
 
 	public void setZipMode(boolean zip) {
 		this.zipMode = zip;
 	}
 
 	public void setNumSeats(int numSeats) {
 		this.players = new PublicPlayerInfo[numSeats];
 	}
 
 	public void setLimit(int lim) {
 		this.limit = lim;
 	}
 
 	public void setAnte(double ant) {
 		this.ante = ant;
 	}
 
 	public void setBlinds(double sb, double bb) {
 		this.smallBlind = sb;
 		this.bigBlind = bb;
 	}
 
 	public void setGameID(long gid) {
 		this.gameID = gid;
 	}
 
 	public void setLogDirectory(String logDir) {
 		this.logDirectory = logDir;
 	}
 
 	//I think here is the best place to advance the round, increase bet size if necessary, etc.
 	public void nextStage(Hand cardsToAddToBoard) {
 		if (getNumActivePlayersNotAllIn() == 0) {
 			Pot lastPot = potManager.getPot(potManager.getNumPots() - 1);
 			List<Integer> playersContestingPot = getPlayersContestingPot(lastPot);
 			for (Integer seat : playersContestingPot) {
 				playerShowDown(seat);
 			}
 		}
 
 		getBoard().addHand(cardsToAddToBoard);
 		this.toAct = nextActivePlayerNotAllIn(getButtonSeat());
 		this.lastToAct = previousActivePlayer(this.toAct); // button could be at an inactive Player
 		this.lastAggressor = -1;
 		this.stage = nextStage(getStage());
 		if (isTurn() && isFixedLimit())
 			this.bet = getBigBlindSize() * 2;
 		for (int i = 0; i < getNumSeats(); i++) {
 			if (hasPlayerInSeat(i))
 				getPlayer(i).newRound();
 		}
 		observersFireStageEvent();
 
 	}
 
 	public void newHand(int buttonSeat, int smallBlindSeat, int bigBlindSeat) {
 		this.buttonSeat = buttonSeat;
 		this.smallBlindSeat = smallBlindSeat;
 		this.bigBlindSeat = bigBlindSeat;
 		this.toAct = smallBlindSeat;
 		this.lastToAct = smallBlindSeat; // as long as noone called, the SB is last to act
 		this.lastAggressor = -1;
 		this.numRaises = 0;
 		this.numWinners = 0;
 		this.stage = Holdem.PREFLOP;
 		if (isFixedLimit())
 			this.bet = getBigBlindSize();
 		this.minRaise = getBigBlindSize();
 		this.potManager = new PotManager(this);
 		for (int i = 0; i < getNumSeats(); i++)
 			if (hasPlayerInSeat(i)) {
 				getPlayer(i).newHand();
 			}
 
 		this.boardCards = new Hand();
 
 		observersFireGameStartEvent();
 		observersFireStageEvent();
 	}
 
 	/**
 	 * determines the winner and pays him the money.
 	 * if needed cards are put to showdown
 	 */
 	public void payout() {
 		removeUncalledBetFromPot();
 
 		// for each pot (starting with the highest) we determine the winner
 		for (int currentPotID = potManager.getNumPots() - 1; currentPotID >= 0; currentPotID--) {
 			Pot currentPot = potManager.getPot(currentPotID);
 			List<Integer> playersContestingPot = getPlayersContestingPot(currentPot);
 			
 			if (playersContestingPot.size() == 1) {
 				getPlayer(playersContestingPot.get(0)).wonHand(currentPot.getValue());
 				observersFireWin(playersContestingPot.get(0), currentPot.getValue(), "");
 			} else {
 				// in the order of contestants check the final handrank
 				int bestHandRank=-1;
 				List<Integer> winners = new ArrayList<Integer>();
 				for (Integer seat : playersContestingPot) {
 					PublicPlayerInfo player = getPlayer(seat);
 					Hand playerHand = player.getHand();
 
 					int rank = HandEvaluator.rankHand(playerHand.getFirstCard(), playerHand.getSecondCard(), boardCards);
 					if (rank > bestHandRank) {
 						// this player is better than the last - make him showdown his cards
 						// and make him single winner
 						winners.clear();
 						winners.add(seat);
 						playerShowDown(seat);
 						bestHandRank = rank;
 					} else if (rank == bestHandRank) {
 						// this player has the same handrank - make him showdown and add him
 						// to the winners
 						winners.add(seat);
 						playerShowDown(seat);
 					} else {
 						// this player sucks and can't beat the players before him
 						// he just mucks
 						observersFireActionEvent(seat, Action.muckAction());
 						observersFireGameStateChangedEvent();
 					}
 				}
 				
 				distributeWinMoney(currentPot.getValue(), winners, PartialStageFastEval.handString(bestHandRank));
 			}
 		}
 		observersFireGameOverEvent();
 	}
 
 	/**
 	 * the last raiser might have been uncalled (or called but the other player didn't have 
 	 * enough money).<br>
 	 * His uncalled bet needs to be returned, before we look at winnings
 	 */
 	private void removeUncalledBetFromPot() {
 		if (lastAggressor != -1) {
 			double amountInPotLastAggressor = getPlayer(lastAggressor).getAmountInPotThisRound();
 			double highestOther = 0;
 			for (int i = 0; i < getNumSeats(); i++) {
 				if (i != lastAggressor && getPlayer(i) != null && getPlayer(i).getAmountInPotThisRound() > highestOther) {
 					highestOther = getPlayer(i).getAmountInPotThisRound();
 				}
 			}
 			double returnedMoney = Utils.roundToCents(amountInPotLastAggressor - highestOther);
 			potManager.removeFromPot(lastAggressor, returnedMoney);
 			getPlayer(lastAggressor).putMoney(-returnedMoney);
 			observersFireActionEvent(lastAggressor, new Action(SPECIAL_ACTION_RETURNUNCALLEDBET, 0, returnedMoney));
 		}
 	}
 
 	/**
 	 * makes the player at the corresponding seat show his cards.<br>
 	 * All observers are informed.<br>
 	 * It is save to call this method multiple times - if the player
 	 * already showed his cards this is not done twice
 	 * @param seat
 	 */
 	private void playerShowDown(Integer seat) {
 		PublicPlayerInfo player = getPlayer(seat);
 		if (player.getRevealedHand() == null) {
 			Hand playerHand = player.getHand();
 			observersFireHandShown(seat, playerHand);
 			player.showCards(true);
 		}
 	}
 
 	/**
 	 * distributes the amount to the given List of winners, updates the PlayerInfos and
 	 * sends events to the observers.<br>
 	 * if the amount cannot be distributed equally (say 5 cent for 3 winners), the remaining
 	 * cents are distributed in order of the winners (2-2-1 for the example given above). 
 	 * @param amount
 	 * @param winners
 	 * @param handString
 	 */
 	private void distributeWinMoney(double amount, List<Integer> winners, String handString) {
 		double amountForEachWinner = amount / winners.size();
 		// round amount to proper cents
 		amountForEachWinner = Math.floor(amountForEachWinner * 100) / 100D;
 		int extraCents = (int) ((amount - amountForEachWinner * winners.size()) * 100);
 		for (Integer winSeat : winners) {
 			PublicPlayerInfo winPlayer = getPlayer(winSeat);
 			double amountWon = amountForEachWinner;
 			if (extraCents-- > 0) {
 				amountWon += 0.01;
 			}
 			winPlayer.wonHand(amountWon);
 			observersFireWin(winSeat, amountWon, handString);
 		}
 	}
 
 	/**
 	 * gets a list of seat-IDs of all players still contesting the given pot
 	 * (i.e. all players eligible to the pot, that haven't folded yet).<br>
 	 * The seats are ordered clockwise with the last aggressor (raiser or bettor) first.
 	 * If in the current round there was no aggressor the first seat after the button is used.
 	 * This order is the typical showdown order
 	 * @param pot
 	 * @return
 	 */
 	private List<Integer> getPlayersContestingPot(Pot pot) {
 		List<Integer> playersContestingPot = new ArrayList<Integer>();
 		// get the player to start the showdown with
 		int startShowDownWith = this.lastAggressor;
 		if (startShowDownWith == -1) {
 			startShowDownWith = nextActivePlayer(buttonSeat);
 		}
 
 		if (pot.isEligible(startShowDownWith) && !getPlayer(startShowDownWith).isFolded()) {
 			playersContestingPot.add(startShowDownWith);
 		}
 
 		// and then get all players clockwise
 		for (int seat = nextActivePlayer(startShowDownWith); seat != startShowDownWith && seat != -1; seat = nextActivePlayer(seat)) {
 			if (pot.isEligible(seat) && !getPlayer(seat).isFolded()) {
 				playersContestingPot.add(seat);
 			}
 		}
 		return playersContestingPot;
 	}
 
 	public void update(Action act, int s) { //UNFINISHED
 		if (s != getCurrentPlayerSeat())
 			throw new IllegalArgumentException("Not players turn to act");
 		if (act.isAllInPass()) {
 			potManager.addToPot(s, act.getAmount());
 			getPlayer(s).update(act);
 			this.toAct = nextActivePlayerNotAllIn(getCurrentPlayerSeat());
 		} else if (act.isAnte()) {
 			potManager.addToPot(s, getAnte());
 			getPlayer(s).update(act);
 			this.toAct = nextActivePlayerNotAllIn(getCurrentPlayerSeat());
 		} else if (act.isBet()) {
 			potManager.addToPot(s, act.getAmount());
 			getPlayer(s).update(act);
 			this.toAct = nextActivePlayerNotAllIn(getCurrentPlayerSeat());
 			this.lastToAct = previousActivePlayer(s);
 			this.lastAggressor = s;
 		} else if (act.isBigBlind()) {
 			potManager.addToPot(s, act.getAmount());
 			getPlayer(s).update(act);
 			this.toAct = nextActivePlayerNotAllIn(getCurrentPlayerSeat());
 		} else if (act.isCall()) { //TO DO: check if call amount < amount to call and split pots
 			potManager.addToPot(s, act.getToCall());
 			getPlayer(s).update(act);
 			if (getNumToAct() == 1) //This player is the last to act, nextStage must be called or showdown
 				this.toAct = -1;
 			else
 				this.toAct = nextActivePlayerNotAllIn(s);
 			
 			// if someone calls preflop, the bigblind gets a chance to act 
 			if (stage==Holdem.PREFLOP &&  lastToAct==smallBlindSeat && numRaises==0) {
 				lastToAct = bigBlindSeat;
 			}
 		} else if (act.isCheck()) {
 			getPlayer(s).update(act);
 			if (getNumToAct() == 1) //This player is the last to act, nextStage must be called or showdown
 
 				this.toAct = -1;
 			else
 				this.toAct = nextActivePlayerNotAllIn(s);
 		} else if (act.isFold()) {
 			getPlayer(s).update(act);
 			if (getNumToAct() == 1) { //This player is the last to act, nextStage must be called 
 				this.toAct = -1;
 			} else
 				this.toAct = nextActivePlayerNotAllIn(s);
 		} else if (act.isMuck()) {
 			//What should we do with this?
 		} else if (act.isRaise()) {
 			potManager.addToPot(s, act.getAmount() + act.getToCall());
 			getPlayer(s).update(act);
 			this.numRaises++;
 			this.toAct = nextActivePlayerNotAllIn(s);
 			this.lastToAct = previousActivePlayer(s);
 			this.lastAggressor = s;
 		} else if (act.isSitout()) {
 			getPlayer(s).update(act);
 			this.toAct = nextActivePlayerNotAllIn(s);
 		} else if (act.isSmallBlind()) {
 			potManager.addToPot(s, getSmallBlindSize());
 			getPlayer(s).update(act);
 			this.toAct = bigBlindSeat;
 		} else {
 			throw new IllegalArgumentException("Invalid action, possibly unimplemented");
 		}
 
 		observersFireActionEvent(s, act);
 		observersFireGameStateChangedEvent();
 	}
 
 	//This method will split the pots up as necessary to the winner(s)
 	protected void handOutWinnings() {
 		//TO DO: implement
 	}
 
 	private int nextStage(int s) {
 		if (s == Holdem.PREFLOP)
 			return Holdem.FLOP;
 		if (s == Holdem.FLOP)
 			return Holdem.TURN;
 		if (s == Holdem.TURN)
 			return Holdem.RIVER;
 		return -1;
 	}
 
 	public boolean isRoundOver() {
 		return getCurrentPlayerSeat() == -1;
 	}
 
 	/**
 	 * puts a player to the corresponding seat and informs the player about his this GameInfo.<br>
 	 * Please note that a single PlayerInfo-Object cannot be seated at multiple tables
 	 * 
 	 * @param seat
 	 * @param player
 	 */
 	public void setPlayer(int seat, PublicPlayerInfo player) {
 		if (!isValidSeat(seat)) {
 			throw new IllegalArgumentException("can't put player on seat " + seat + " is noone put a chair there yet ;).\nCall setNumSeats before");
 		}
 		if (hasPlayerInSeat(seat)) {
 			throw new IllegalArgumentException("can't put player in seat " + seat + ". Seat is not empty.");
 		}
 		if (player.getGameInfo() != null) {
 			throw new IllegalArgumentException("this player-(object) is already sitting on another table");
 		}
 		this.players[seat] = player;
 		if (player.getBot() != null) {
 			this.gameObservers.add(player.getBot());
 		}
 		player.setGame(this);
 	}
 
 	public void addGameObserver(GameObserver observer) {
 		this.gameObservers.add(observer);
 	}
 
 	public PotManager getPotManager() {
 		return potManager;
 	}
 
 	private void observersFireGameStartEvent() {
 		for (GameObserver gameObserver : gameObservers) {
 			gameObserver.gameStartEvent(this);
 		}
 	}
 
 	private void observersFireStageEvent() {
 		for (GameObserver gameObserver : gameObservers) {
 			gameObserver.stageEvent(this.stage);
 		}
 	}
 
 	private void observersFireActionEvent(int seat, Action action) {
 		for (GameObserver gameObserver : gameObservers) {
 			gameObserver.actionEvent(seat, action);
 		}
 	}
 
 	private void observersFireGameStateChangedEvent() {
 		for (GameObserver gameObserver : gameObservers) {
 			gameObserver.gameStateChanged();
 		}
 	}
 
 	public void observersFireDealHoleCardsEvent() {
 		for (GameObserver gameObserver : gameObservers) {
 			gameObserver.dealHoleCardsEvent();
 		}
 	}
 
 	public void observersFireGameOverEvent() {
 		for (GameObserver gameObserver : gameObservers) {
 			gameObserver.gameOverEvent();
 		}
 	}
 
 	public void observersFireWin(int seat, double amount, String hand) {
 		for (GameObserver gameObserver : gameObservers) {
 			gameObserver.winEvent(seat, amount, hand);
 		}
 	}
 
 	public void observersFireHandShown(int seat, Hand hand) {
 		for (GameObserver gameObserver : gameObservers) {
 			gameObserver.showdownEvent(seat, hand.getFirstCard(), hand.getSecondCard());
 		}
 	}
 
 
 }
