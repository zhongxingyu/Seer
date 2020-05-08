 package states;
 
 public class StateHelper {
 	public static final int ALABAMA = 0;
 	public static final int ARIZONA = 1;
 	public static final int ARKANSAS = 2;
 	public static final int CALIFORNIA = 3;
 	public static final int COLORADO = 4;
 	public static final int CONNECTICUT = 5;
 	public static final int DELAWARE = 6;
 	public static final int FLORIDA = 7;
 	public static final int GEORGIA = 8;
 	public static final int IDAHO = 9;
 	public static final int ILLINOIS = 10;
 	public static final int INDIANA = 11;
 	public static final int IOWA = 12;
 	public static final int KANSAS = 13;
 	public static final int KENTUCKY = 14;
 	public static final int LOUISIANA = 15;
 	public static final int MAINE = 16;
 	public static final int MARYLAND = 17;
 	public static final int MASSACHUSETTS = 18;
 	public static final int MICHIGAN = 19;
 	public static final int MINNESOTA = 20;
 	public static final int MISSISSIPPI = 21;
 	public static final int MISSOURI = 22;
 	public static final int MONTANA = 23;
 	public static final int NEBRASKA = 24;
 	public static final int NEVADA = 25;
 	public static final int NEW_HAMPSHIRE = 26;
 	public static final int NEW_JERSEY = 27;
 	public static final int NEW_MEXICO = 28;
 	public static final int NEW_YORK = 29;
 	public static final int NORTH_CAROLINA = 30;
 	public static final int NORTH_DAKOTA = 31;
 	public static final int OHIO = 32;
 	public static final int OKLAHOMA = 33;
 	public static final int OREGON = 34;
 	public static final int PENNSYLVANIA = 35;
 	public static final int RHODE_ISLAND = 36;
 	public static final int SOUTH_CAROLINA = 37;
 	public static final int SOUTH_DAKOTA = 38;
 	public static final int TENNESSEE = 39;
 	public static final int TEXAS = 40;
 	public static final int UTAH = 41;
 	public static final int VERMONT = 42;
	public static final int VIRGINIA = 43;
	public static final int WASHINGTON = 44;
	public static final int WEST_VIRGINIA = 45;
	public static final int WISCONSIN = 46;
	public static final int WYOMING = 47;
 	public static State getState(int id){
 		State state = null;
 		switch (id){
 			case ALABAMA:
 				state = new Alabama();
 				break;
 			case ARIZONA:
 				state = new Arizona();
 				break;
 			case ARKANSAS:
 				state = new Arkansas();
 				break;
 			case CALIFORNIA:
 				state = new California();
 				break;
 			case COLORADO:
 				state = new Colorado();
 				break;
			case CONNECTICUT:
				state = new Connecticut();
				break;
			case DELAWARE:
				state = new Delaware();
				break;
			case FLORIDA:
				state = new Florida();
				break;
 			default:
 				System.out.println(id + " causing trouble");
 				break;
 		}
 		return state;
 	}
 	public static State getState(String name){
 		State state = null;
 		switch (name){
 			case "Alabama":
 				state = new Alabama();
 				break;
 			case "Arizona":
 				state = new Arizona();
 				break;
 			case "Arkansas":
 				state = new Arkansas();
 				break;
 			case "California":
 				state = new California();
 				break;
 			case "Colorado":
 				state = new Colorado();
 				break;
 			default:
 				System.out.println(name + " causing trouble");
 				break;
 		}
 		return state;
 	}
 }
