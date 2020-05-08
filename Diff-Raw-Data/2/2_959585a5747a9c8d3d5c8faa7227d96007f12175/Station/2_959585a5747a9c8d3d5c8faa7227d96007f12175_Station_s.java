 package com.wwmteam.wwm.beans;
 
 import com.wwmteam.wwm.R;
 
 public class Station {
 
 	public static final int FIRST_ORDER = 0;
 	public static final int SECOND_ORDER = 1;
 	public static final int DEPOT = 2;
 	public static final int READY = 3;
 	
 	public int Id;
 	public int Name;
 	public int Address;
 	public int Type;
 	public int[] StationsBefore;
 	public int[] StationsAfter;
 	
 	private Station() {}
 	
 	private Station(int id, int name, int address, int type, int[] stationsBefore, int[] stationsAfter) {
 		this.Id = id;
 		this.Name = name;
 		this.Address = address;
 		this.Type = type;
 		this.StationsBefore = stationsBefore;
 		this.StationsAfter = stationsAfter;
 	}
 	
 	public Boolean IsSecondOrder() {
 		return Type == SECOND_ORDER;
 	}
 	
 	public Boolean IsFirstOrder() {
 		return Type == FIRST_ORDER;
 	}
 	
 	public Boolean IsDepot() {
 		return Type == DEPOT;
 	}
 	
 	public Boolean IaReady() {
 		return Type == READY;
 	}
 	
 	public Boolean HasAddress() {
 		return Address != -1;
 	}
 	
 	private static Station[] stations = new Station[] {
 		new Station(0, R.string.west, -1, SECOND_ORDER, new int[0], new int[0]),
 		new Station(1, R.string.sunny, -1, SECOND_ORDER, new int[0], new int[0]),
 		new Station(2, R.string.young, -1, SECOND_ORDER, new int[0], new int[0]),
 		new Station(3, R.string.rokossovskogo, -1, SECOND_ORDER, new int[0], new int[0]),
 		new Station(4, R.string.depot_1, -1, DEPOT, new int[0], new int[0]),
 		new Station(5, R.string.cathedral, R.string.cathedral_position, FIRST_ORDER, new int[0], new int[]{ 6, 7, 8 }),
 		new Station(6, R.string.crystal, R.string.crystal_position, FIRST_ORDER, new int[]{ 5 }, new int[]{ 7, 8 }),
 		new Station(7, R.string.after_river, R.string.afterriver_position, FIRST_ORDER, new int[]{ 6, 5 }, new int[]{ 8 }),
 		new Station(8, R.string.library, R.string.library_position, READY, new int[]{ 7, 6, 5 }, new int[0]),
 		new Station(9, R.string.shop_center, -1, SECOND_ORDER, new int[0], new int[0]),
 		new Station(10, R.string.Jukov, -1, SECOND_ORDER, new int[0], new int[0]),
 		new Station(11, R.string.lermontovskaya, -1, SECOND_ORDER, new int[0], new int[0]),
 		new Station(12, R.string.park, -1, SECOND_ORDER, new int[0], new int[0]),
 		new Station(13, R.string.tupolev, -1, SECOND_ORDER, new int[0], new int[0]),
 		new Station(14, R.string.works, -1, SECOND_ORDER, new int[0], new int[0]),
		new Station(15, R.string.depot_2, -1, SECOND_ORDER, new int[0], new int[0]),
 		new Station(16, R.string.moscow, -1, SECOND_ORDER, new int[0], new int[0]),
 		new Station(17, R.string.sibirian_road, -1, SECOND_ORDER, new int[0], new int[0])
 	};
 	
 	public static Station GetStationById(int id) {
 		return stations[id];
 	}
 }
