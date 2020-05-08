 package dk.stacktrace.risk.game_logic.continents;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.util.Log;
 import dk.stacktrace.risk.game_logic.Board;
 import dk.stacktrace.risk.game_logic.Territory;
 import dk.stacktrace.risk.game_logic.enumerate.TerritoryID;
 
 public class Asia extends Continent {
 	private Territory 
 	siberia,
 	yakutsk,
 	kamchatka,
 	ural,
 	irkutsk,
 	mongolia,
 	japan,
 	afghanistan,
 	china,
 	middleEast,
 	india,
 	siam;
 	
 	
 	public Asia(Board board) {
 		this.board = board;
 		territories = new ArrayList<Territory>();
 		reinforcementBonus = 7;
 		createTerritories();
 	}
 
 	protected void createTerritories() {
 		Log.v("create territories", "1");
 		siberia = new Territory("Siberia", TerritoryID.SIB);
 		yakutsk = new Territory("Yakutsk", TerritoryID.YAK);
 		kamchatka = new Territory("Kamchatka", TerritoryID.KAM);
 		ural = new Territory("Ural", TerritoryID.URA);
 		irkutsk = new Territory("Irkutsk", TerritoryID.IRK);
 		mongolia = new Territory("Mongolia", TerritoryID.MON);
 		japan = new Territory("Japan", TerritoryID.JAP);
 		afghanistan = new Territory("Afghanistan", TerritoryID.AFG);
 		china = new Territory("China", TerritoryID.CHI);
 		middleEast = new Territory("Middle East", TerritoryID.MID);
 		india = new Territory("India", TerritoryID.IDA);
 		siam = new Territory("Siam", TerritoryID.SIA);
 		
 		Collections.addAll(territories, 
 				siberia,
 				yakutsk,
 				kamchatka,
 				ural,
 				irkutsk,
 				mongolia,
 				japan,
 				afghanistan,
 				china,
 				middleEast,
 				india,
 				siam);
 	}
     
 	
 	public void setNeighbours() {
 		siberia.addNeighbour(ural);
 		siberia.addNeighbour(yakutsk);
 		siberia.addNeighbour(irkutsk);
 		siberia.addNeighbour(mongolia);
 		siberia.addNeighbour(china);
 		
 		yakutsk.addNeighbour(siberia);
 		yakutsk.addNeighbour(kamchatka);
 		yakutsk.addNeighbour(irkutsk);
 		
 		kamchatka.addNeighbour(yakutsk);
 		kamchatka.addNeighbour(board.getNorthAmerica().getAlaska());
 		kamchatka.addNeighbour(mongolia);
 		kamchatka.addNeighbour(irkutsk);
 		kamchatka.addNeighbour(japan);
 		
 		ural.addNeighbour(board.getEurope().getUkraine());
 		ural.addNeighbour(siberia);
 		ural.addNeighbour(china);
 		ural.addNeighbour(afghanistan);
 	
 		irkutsk.addNeighbour(siberia);
 		irkutsk.addNeighbour(yakutsk);
 		irkutsk.addNeighbour(kamchatka);
 		irkutsk.addNeighbour(mongolia);
 		
 		mongolia.addNeighbour(siberia);
 		mongolia.addNeighbour(yakutsk);
 		mongolia.addNeighbour(kamchatka);
 		mongolia.addNeighbour(japan);
 		mongolia.addNeighbour(china);
 		mongolia.addNeighbour(irkutsk);
 		
 		japan.addNeighbour(kamchatka);
 		japan.addNeighbour(mongolia);
 		
 		afghanistan.addNeighbour(board.getEurope().getUkraine());
 		afghanistan.addNeighbour(ural);
 		afghanistan.addNeighbour(china);
 		afghanistan.addNeighbour(india);
 		afghanistan.addNeighbour(middleEast);
 		
 		china.addNeighbour(afghanistan);
 		china.addNeighbour(ural);
 		china.addNeighbour(siberia);
 		china.addNeighbour(mongolia);
 		china.addNeighbour(siam);
 		china.addNeighbour(india);
 		
 		middleEast.addNeighbour(board.getAfrica().getEgypt());
 		middleEast.addNeighbour(board.getEurope().getSouthEurope());
 		middleEast.addNeighbour(board.getEurope().getUkraine());
 		middleEast.addNeighbour(afghanistan);
 		middleEast.addNeighbour(india);
 		
 		india.addNeighbour(middleEast);
 		india.addNeighbour(afghanistan);
 		india.addNeighbour(china);
 		india.addNeighbour(siam);
 		
 		siam.addNeighbour(india);
 		siam.addNeighbour(china);
 		siam.addNeighbour(board.getAustralia().getIndonesia());
 	}
 
 	public Territory getSiberia() {
 		return siberia;
 	}
 
 	public Territory getYakutsk() {
 		return yakutsk;
 	}
 
 	public Territory getKamchatka() {
 		return kamchatka;
 	}
 
 	public Territory getUral() {
 		return ural;
 	}
 
 	public Territory getIrkutsk() {
 		return irkutsk;
 	}
 
 	public Territory getMongolia() {
 		return mongolia;
 	}
 
 	public Territory getJapan() {
 		return japan;
 	}
 
 	public Territory getAfghanistan() {
 		return afghanistan;
 	}
 
 	public Territory getChina() {
 		return china;
 	}
 
 	public Territory getMiddleEast() {
 		return middleEast;
 	}
 
 	public Territory getIndia() {
 		return india;
 	}
 
 	public Territory getSiam() {
 		return siam;
 	}
 
 	public Board getBoard() {
 		return board;
 	}
 
 	
 }
