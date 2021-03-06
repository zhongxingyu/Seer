 package com.philihp.weblabora.model;
 
 import static com.philihp.weblabora.model.Wheel.Position.E;
 import static com.philihp.weblabora.model.Wheel.Position.H;
 import static com.philihp.weblabora.model.Wheel.Position.K;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.philihp.weblabora.model.building.Building;
 import com.philihp.weblabora.model.building.BuildingEnum;
 
 public class BoardModeFourLongIreland extends BoardMode {
 	
 	private static final GamePlayers PLAYERS = GamePlayers.FOUR;
 	private static final GameLength LENGTH = GameLength.LONG;
	private static final GameCountry COUNTRY = GameCountry.IRELAND;
 
 	protected BoardModeFourLongIreland(Board board) {
 		super(board);
 	}
 
 	@Override
 	public int[] getWheelArmValues() {
 		return new int[] { 0, 2, 3, 4, 5, 6, 6, 7, 7, 8, 8, 9, 10 };
 	}
 
 	@Override
 	public List<Building> roundBuildings() {
 		List<Building> buildings = new ArrayList<Building>();
 		for (BuildingEnum buildingId : BuildingEnum.values()) {
 			
 			char c = buildingId.toString().charAt(0);
 			if(c != 'G' && c != 'I') continue;
 			
 			Building building = buildingId.getInstance();
 			if (board.getSettlementRound().equals(building.getStage())
 					&& building.getPlayers().ordinal() <= PLAYERS.ordinal()) {
 				buildings.add(building);
 			}
 		}
 		return buildings;
 	}
 
 	@Override
 	public List<Building> futureBuildings() {
 		List<Building> buildings = new ArrayList<Building>();
 		for(BuildingEnum buildingId : BuildingEnum.values()) {
 			
 			char c = buildingId.toString().charAt(0);
 			if(c != 'G' && c != 'I') continue;
 			
 			Building building = buildingId.getInstance();
 			if(board.getAllBuildings().containsKey(buildingId) == false && building.getPlayers().ordinal() <= PLAYERS.ordinal()) {
 				buildings.add(building);
 			}
 		}
 		return buildings;
 	}
 
 	@Override
 	public boolean isExtraRound(int round) {
 		return round >= 24;
 	}
 
 	@Override
 	public SettlementRound roundBeforeSettlement(int round) {
 		switch (round) {
 		case 6:
 			return SettlementRound.A;
 		case 9:
 			return SettlementRound.B;
 		case 15:
 			return SettlementRound.C;
 		case 18:
 			return SettlementRound.D;
 		case 25:
 			return SettlementRound.E;
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public void postMove() {
 		board.nextActivePlayer();
 		board.setMoveInRound(board.getMoveInRound()+1);
 		if(board.isExtraRound() && board.getMoveInRound() == board.players.length+1) {
 			board.postExtraRound();
 		}
 		if(board.isSettling() && board.getMoveInRound() == board.players.length+1) {
 			board.postSettlement();
 		}
 		else if(!board.isSettling() && board.getMoveInRound() == board.players.length+2) {
 			board.postRound();
 		}
 	}
 
 	@Override
 	public void postRound() {
 		board.setMoveInRound(1);
 
 		if(isExtraRound(board.getRound())) {
 			board.setRound(board.getRound()+1);
 			board.setExtraRound(true);
 		}
 		else if(board.isRoundBeforeSettlement(board.getRound())) {
 			board.setSettling(true);
 		}
 		else {
 			board.setRound(board.getRound()+1);
 		}
 		
 		//5 -- pass starting player
 		board.setStartingPlayer(board.getStartingPlayer() + 1);
 		board.getStartingMarker().setOwner(board.players[board.getStartingPlayer()]);
 	}
 
 	@Override
 	public String getMoveName() {
 		if(board.isExtraRound()) return "extra";
 		switch (board.getMoveInRound()) {
 		case 1:
 			return "first";
 		case 2:
 			return "second";
 		case 3:
 			return "third";
 		case 4:
 			return "fourth";
 		case 5:
 			return "last";
 		default:
 			throw new RuntimeException("Illegal Move Number " + board.getMoveInRound());
 		}
 	}
 
 	@Override
 	public int grapeActiveOnRound() {
 		return 8;
 	}
 
 	@Override
 	public int stoneActiveOnRound() {
 		return 8;
 	}
 
 	@Override
 	public void setWheelTokens(Wheel wheel) {
 		wheel.grape.setPosition(H);
 	}
 
 	@Override
 	public GamePlayers getPlayers() {
 		return PLAYERS; 
 	}
 
 	@Override
 	public GameCountry getCountry() {
 		return COUNTRY;
 	}
 
 	@Override
 	public GameLength getLength() {
 		return LENGTH;
 	}
 	
 }
