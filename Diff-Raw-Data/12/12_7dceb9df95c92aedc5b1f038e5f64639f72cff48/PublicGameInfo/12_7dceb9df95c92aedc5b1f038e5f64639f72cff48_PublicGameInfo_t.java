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
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import com.biotools.meerkat.Action;
 import com.biotools.meerkat.GameInfo;
 import com.biotools.meerkat.Hand;
 import com.biotools.meerkat.Holdem;
 
 public class PublicGameInfo implements GameInfo {
 
 	//limit definitions
 	public static final int FIXED_LIMIT = 0;
 	public static final int POT_LIMIT = 1;
 	public static final int NO_LIMIT = 2;
 	
 	private boolean reverseBlinds = false;
 	private boolean simulation = false;
 	private boolean zipMode = false;
 	private int button = 0; 
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
 	private List<Pot> pots;
 	private String logDirectory = "";
 	private Hand boardCards = null;
 	private PublicPlayerInfo[] players = null;
 	
 	@Override
 	public boolean canRaise(int seat) {
 		return hasPlayerInSeat(seat) && getPlayer(seat).hasEnoughToRaise();   
 	}
 
 	@Override
 	public double getAmountToCall(int seat) {
 		double highestAmount = 0.0D;
 		for (int i = 0; i < this.players.length; i++)
 			if(isCommitted(i))
 				highestAmount = Math.max(highestAmount, getPlayer(i).getAmountInPotThisRound());
 		return isActive(seat) && !getPlayer(seat).isAllIn() ? highestAmount - getPlayer(seat).getAmountInPotThisRound() : 0.0D;
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
 		int bb;
 		for(bb = nextSeat(getSmallBlindSeat()); !inGame(bb); bb = nextSeat(bb));
 		return bb;
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
 		return this.button;
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
 		double eligiblePot = 0.0D;
 		for(int i = 0; i < pots.size(); i++)
 			eligiblePot += pots.get(i).isEligible(seat) ? pots.get(i).getValue() : 0.0D;
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
 		for(int i = 0; i < getNumSidePots()+1; i++)
 			if(pots.get(i).isMain())
 				return pots.get(i).getValue();
 		return 0.0D;
 	}
 
 	@Override
 	public double getMinRaise() {
 		return this.minRaise;
 	}
 
 	@Override
 	public int getNumActivePlayers() {
 		int active = 0;
 		for(int i = 0; i < getNumSeats(); i++)
 			if(isActive(i))
 				active++;
 		return active;
 	}
 
 	@Override
 	public int getNumActivePlayersNotAllIn() {
 		int active = 0;
 		for(int i = 0; i < getNumSeats(); i++)
 			if(isActive(i) && !getPlayer(i).isAllIn())
 				active++;
 		return active;
 	}
 
 	@Override
 	public int getNumPlayers() {
 		int num = 0;
 		for(int i = 0; i < getNumSeats(); i++)
 			if(hasPlayerInSeat(i))
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
 		return this.pots.size()-1;
 	}
 
 	@Override
 	public int getNumToAct() {
 		//no one has to act
 		if(this.toAct == -1)
 			return 0;
 		
 		//find the highest amount in the pot this round
 		double maxInRound = 0.0D;
 		for(int i = 0; i < getNumSeats(); i++) {
 			if(isActive(i))
 				maxInRound = Math.max(maxInRound, getPlayer(i).getAmountInPotThisRound());
 		}
 		
 		int active = 0;
 		//if no one has committed to the pot we just count active players
 		if(maxInRound == 0) {
 			for(int i = this.toAct; i != previousPlayer(this.toAct); i = nextPlayer(i))
 				if(isActive(i))
 					active++;
 		} else {
 			//otherwise we count people who have not called the high bet
 			for(int i = this.toAct; i != previousPlayer(this.toAct); i = nextPlayer(i))
 				if(isActive(i) && getPlayer(i).getAmountInPotThisRound() < maxInRound)
 					active++;
 			if(isPreFlop() && !getPlayer(getBigBlindSeat()).hasActedThisRound()) //add one for big blind in preflop
 				active++;
 		}
 		return active;
 	}
 
 	@Override
 	public int getNumWinners() {
 		return this.numWinners;
 	}
 
 	@Override
 	public int getNumberOfAllInPlayers() {
 		int allin = 0;
 		for(int i = 0; i < getNumSeats(); i++)
 			if(isValidSeat(i) && getPlayer(i).isAllIn())
 				allin++;
 		return allin;
 	}
 
 	@Override
 	public PublicPlayerInfo getPlayer(int seat) {
 		return isValidSeat(seat) ? this.players[seat] : null;
 	}
 
 	@Override
 	public PublicPlayerInfo getPlayer(String name) {
 		for(int i = 0; i < getNumSeats(); i++)
 			if(hasPlayerInSeat(i) && players[i].getName() == name)
 				return getPlayer(i);
 		return null;
 	}
 
 	@Override
 	public String getPlayerName(int seat) {
 		if(hasPlayerInSeat(seat))
 			return getPlayer(seat).getName();
 		return null;
 	}
 
 	@Override
 	public int getPlayerSeat(String name) {
 		for(int i = 0; i < getNumSeats(); i++)
 			if(getPlayerName(i).equals(name))
 				return i;
 		return -1;
 	}
 
 	@Override
 	public List<PublicPlayerInfo> getPlayersInPot(double potSize) {
 		List<PublicPlayerInfo> list = new ArrayList<PublicPlayerInfo>();
 		for(int i = 0; i < getNumSeats(); i++)
 			if(isActive(i) && getPlayer(i).getAmountInPot() >= potSize)
 				list.add(getPlayer(i));
 		return list;
 	}
 
 	@Override
 	//Rake on a per-hand basis is not implemented yet
 	public double getRake() {
 		return 0.0D;
 	}
 
 	@Override
 	//index 0 is the smallest side pot
 	public double getSidePotSize(int index) {
 		if(index < 0 || index >= getNumSidePots())
 			return 0.0D;
 		//perhaps there is a better way to do this?
 		
 		//sort the pots (smallest first). Then iterate and find the indexth pot (not including the main pot).
 		Collections.sort(this.pots);
 		boolean seenMain = false;
 		for(int i = 0; i < this.pots.size(); i++) {
 			if(this.pots.get(i).isMain())
 				seenMain = true;
 			if((i == index && seenMain == false) || (i == index+1 && seenMain == true))
 				return this.pots.get(i).getValue();
 		}
 		return 0.0D;
 	}
 
 	@Override
 	public int getSmallBlindSeat() {
 		int sb;
 		for(sb = nextSeat(getButtonSeat()); !inGame(sb) && sb != getButtonSeat(); sb = nextSeat(sb));
 		return sb;
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
 		for(int i = 0; i < getNumSeats(); i++)
 			stakes = Math.max(stakes, getPlayer(i).getAmountInPot() - getPlayer(i).getAmountInPotThisRound());
 		return stakes;
 	}
 
 	@Override
 	public double getTotalPotSize() {
 		double potSize = 0.0D;
 		for(Iterator<Pot> i = this.pots.iterator(); i.hasNext();)
 			potSize += i.next().getValue();
 		return potSize;
 	}
 
 	@Override
 	public int getUnacted() {
 		int unacted = 0;
 		for(int i = 0; i < getNumSeats(); i++)
 			if(hasPlayerInSeat(i) && !getPlayer(i).hasActedThisRound())
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
 		if(!isValidSeat(seat) || getNumActivePlayers() <= 0)
 			return -1;
 		int i;
 		for(i = nextPlayer(seat); i != seat && !isActive(i); i = nextPlayer(i));
 		return i;
 	}
 
 	@Override
 	public int nextPlayer(int seat) {
 		if(!isValidSeat(seat) || getNumPlayers() <= 0)
 			return -1;
 		int i;
 		for(i = nextSeat(seat); i != seat && !hasPlayerInSeat(i); i = nextSeat(i));
 		return i;
 	}
 
 	@Override
 	public int nextSeat(int seat) {
 		return !isValidSeat(seat) ? -1 : (seat+1) % getNumSeats();
 	}
 
 	public int previousSeat(int seat) {
 		return !isValidSeat(seat) ? -1 : (seat-1) % getNumSeats();
 	}
 	
 	@Override
 	public int previousPlayer(int seat) {
 		if(!isValidSeat(seat) || getNumPlayers() <= 0)
 			return -1;
 		int i;
 		for(i = previousSeat(seat); i != seat && !hasPlayerInSeat(i); i = previousSeat(i));
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
 	
 	public void setButton(int seat) {
 		this.button = seat;
 	}
 	
 	public void moveButtonOver() {
 		this.button = nextActivePlayer(button);
 	}
 	
 	public void setLimit(int lim) {
 		this.limit = lim;
 	}
 	
 	public void setAnte(double ant) {
 		this.ante = ant;
 	}
 	
 	public void setBigBlind(double bb) {
 		this.bigBlind = bb;
 	}
 	
 	public void setSmallBlind(double sb) {
 		this.smallBlind = sb;
 	}
 	
 	public void setGameID(long gid) {
 		this.gameID = gid;
 	}
 	
 	public void setLogDirectory(String logDir) {
 		this.logDirectory = logDir;
 	}
 	
 	//I think here is the best place to advance the round, increase bet size if necessary, etc.
 	public void nextStage(Hand cardsToAddToBoard) {
 		getBoard().addHand(cardsToAddToBoard);
 		this.toAct = nextActivePlayer(getButtonSeat());
 		this.stage = nextStage(getStage());
 		if(isTurn() && isFixedLimit())
 			this.bet = getBigBlindSize()*2;
 		for(int i = 0; i < getNumSeats(); i++) {
 			if(hasPlayerInSeat(i))
 				getPlayer(i).newRound();
 		}
 	}
 	
 	public void newHand() {
 		this.toAct = nextActivePlayer(getBigBlindSeat());
 		this.numRaises = 0;
 		this.numWinners = 0;
 		this.stage = Holdem.PREFLOP;
 		if(isFixedLimit())
 			this.bet = getBigBlindSize();
 		this.minRaise = getBigBlindSize();
 		this.pots = new ArrayList<Pot>();
 		List<Integer> playerList = new ArrayList<Integer>();
 		for(int i = 0; i < getNumSeats(); i++)
 			if (hasPlayerInSeat(i)) {
 				getPlayer(i).newHand();
 				if (isActive(i))
 					playerList.add(new Integer(i));
 			}
 		this.pots.add(new Pot(playerList,0.0D,true));
 		this.boardCards = null;
 	}
 	
 	private Pot getMainPot() {
 		for(int i = 0; i < this.pots.size(); i++)
 			if(this.pots.get(i).isMain())
 				return this.pots.get(i);
 		return null;
 	}
 	
 	public void update(Action act, double param, int s) { //UNFINISHED
 		if(s != getNumToAct())
 			throw new IllegalArgumentException("Not players turn to act");
 		if(act.isAllInPass()) {
 			getMainPot().addToPot(param);
 		} else if (act.isAnte()) {
 		} else if (act.isBet()) {
 		} else if (act.isBigBlind()) {
 		} else if (act.isCall()) {
 		} else if (act.isCheck()) {
 		} else if (act.isFold()) {
 		} else if (act.isMuck()) {
 		} else if (act.isRaise()) {
 		} else if (act.isSitout()) {
 		} else {
 			throw new IllegalArgumentException("Invalid action, possibly unimplemented");
 		}
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
 		players[seat] = player;
 		player.setGame(this);
 	}
 }
