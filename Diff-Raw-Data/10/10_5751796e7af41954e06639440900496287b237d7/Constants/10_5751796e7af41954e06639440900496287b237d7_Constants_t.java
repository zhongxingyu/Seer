/**
 * @author  Chris Serafin, Peter Maidens, Ken "Mike" Armstrong, Kimberly Kramer
 * 
 * Contains a large list of of constants and allows the user to search and convert easily
 */
 
 package ca.ualberta.cs.oneclick_cookbook;
 
 public class Constants { 
     public static final int ZERO_VALUE = 0;
     public static final int NULL_VALUE = -1;
     public static final int GOOD = 1;
     public static final int ML = 0;
     public static final int L = 1;
     public static final int G = 2;
     public static final int KG = 3;
     public static final int INDIV = 4;
     public static final int CUPS = 5;
     public static final int TBS = 6;
     public static final int TSP = 7;
     
     // This is important!! Make sure it's up to date
     public static final int NUM_OF_UNITS = 8;
     
     /**
      * Converts from spinner position to units
      * @param position The position of the spinner
      * @return unit The type of unit selected in string
      */
     public static String getUnitFromPosition(int position) {
     	switch (position) {
     	case ML:
     		return "mL";
     	case L:
     		return "L";
     	case G:
     		return "g";
     	case KG:
     		return "kg";
     	case INDIV:
     		return "units";
     	case CUPS:
     		return "cups";
     	case TBS:
     		return "tablespoons";
     	case TSP:
     		return "teaspoons";
     	default:
     		return "units";
     	}
     }
     
     /**
      * Returns the spinner position from the passed string
      * @param unit String of the unit you want the postition of
      */
     public static int getPositionFromUnit(String unit) {
     	if (unit.contentEquals("ml")) {
     		return ML;
     	}
     	else if (unit.contentEquals("L")) {
     		return L;
     	}
     	else if (unit.contentEquals("g")) {
     		return G;
     	}
     	else if (unit.contentEquals("kg")) {
     		return KG;
     	}
     	else if (unit.contentEquals("units")) {
     		return INDIV;
     	}
     	else if (unit.contentEquals("cups")) {
     		return CUPS;
     	}
     	else if (unit.contentEquals("tablespoons")) {
     		return TBS;
     	}
     	else if (unit.contentEquals("teaspoons")) {
     		return TSP;
     	}
     	else {
     		return INDIV;
     	}
     	
     }
 }
 
