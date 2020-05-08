 package dk.stacktrace.risk.game_logic.continents;
 
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import dk.stacktrace.risk.game_logic.Board;
 import dk.stacktrace.risk.game_logic.Territory;
 import dk.stacktrace.risk.game_logic.enumerate.TerritoryID;
 
 public class Africa extends Continent {
 	private Territory
 	northAfrica,
 	egypt,
 	eastAfrica,
 	congo,
 	southAfrica,
 	madagascar;
 	
 	
 	public Africa(Board board) {
 		this.board = board;
 		territories = new ArrayList<Territory>();
 		reinforcementBonus = 3;
 		createTerritories();
 	}
 	
 	@Override
 	protected void createTerritories() {
 				northAfrica = new Territory("North Africa", TerritoryID.NAF);
 				egypt = new Territory("Egypt", TerritoryID.EGY);
 				eastAfrica = new Territory("East Africa", TerritoryID.EAF);
 				congo = new Territory("Congo", TerritoryID.CON);
 				southAfrica = new Territory("South Africa", TerritoryID.SAF);
 				madagascar = new Territory("Madagascar", TerritoryID.MAD) ;
 				
 				Collections.addAll(territories, 
 						northAfrica,
 						egypt,
 						eastAfrica,
 						congo,
 						southAfrica,
 						madagascar);
 	}
 	
 	@Override
 	public void setNeighbours() {
 		northAfrica.addNeighbour(board.getSouthAmerica().getBrazil());
 		northAfrica.addNeighbour(board.getEurope().getWestEurope());
 		northAfrica.addNeighbour(board.getEurope().getSouthEurope());
 		northAfrica.addNeighbour(egypt);
 		northAfrica.addNeighbour(eastAfrica);
 		northAfrica.addNeighbour(congo);
 		
 		egypt.addNeighbour(northAfrica);
 		egypt.addNeighbour(board.getEurope().getSouthEurope());
 		egypt.addNeighbour(board.getAsia().getMiddleEast());
 		egypt.addNeighbour(eastAfrica);
 		
 		eastAfrica.addNeighbour(northAfrica);
 		eastAfrica.addNeighbour(egypt);
 		eastAfrica.addNeighbour(board.getAsia().getMiddleEast());
 		eastAfrica.addNeighbour(madagascar);
 		eastAfrica.addNeighbour(southAfrica);
 		eastAfrica.addNeighbour(congo);
 		
 		congo.addNeighbour(northAfrica);
 		congo.addNeighbour(eastAfrica);
 		congo.addNeighbour(southAfrica);
 		
 		southAfrica.addNeighbour(congo);
		southAfrica.addNeighbour(eastAfrica);
		southAfrica.addNeighbour(madagascar);
		
 		congo.addNeighbour(eastAfrica);
 		congo.addNeighbour(madagascar);
 		
 		madagascar.addNeighbour(eastAfrica);
 		madagascar.addNeighbour(southAfrica);
 
 	}
 
 	public Territory getNorthAfrica() {
 		return northAfrica;
 	}
 
 	public Territory getEgypt() {
 		return egypt;
 	}
 
 	public Territory getEastAfrica() {
 		return eastAfrica;
 	}
 
 	public Territory getCongo() {
 		return congo;
 	}
 
 	public Territory getSouthAfrica() {
 		return southAfrica;
 	}
 
 	public Territory getMadagascar() {
 		return madagascar;
 	}
 
 	public Board getBoard() {
 		return board;
 	}
 
 }
