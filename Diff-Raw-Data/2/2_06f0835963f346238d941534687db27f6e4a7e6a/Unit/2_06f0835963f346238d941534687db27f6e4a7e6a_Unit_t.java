 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Created on May 4, 2004
  *
  */
 package org.concord.data;
 
 /**
  * @author dima
  *
  */
 
 import java.util.Vector;
 
 import org.concord.framework.data.DataDimension;
 
 public final class Unit implements DataDimension{
 	public int unitCategory  = UNIT_CAT_UNKNOWN;
 
 	public int	code 	= UNIT_CODE_UNKNOWN;
 	public int	baseUnit 	= UNIT_CODE_UNKNOWN;
 
 	public boolean derived = false;
 
 	public	String	name;
 	public	String	abbreviation;
 
 	// Powers of the standard units
 	public byte  meter 		= 0;
 	public byte  kg 			= 0;
 	public byte  sec 			= 0;
 	public byte  amper 		= 0;
 	public byte  kelvin 		= 0;
 	public byte  candela 		= 0;
 	public byte  mole 		= 0;
 	public byte  radian 		= 0;
 	public byte  steradian 	= 0;
 
 	public 	float		koeffA = 1.0f;
 	public 	float		koeffB = 0.0f;
 
 	public boolean dimLess = false;
 	public boolean doMetricPrefix = false;
 
     private static final byte NON_ORDER = 0;
 
     
     final static int METER_INDEX            = 0;
     final static int KG_INDEX               = 1;
     final static int SEC_INDEX              = 2;
     final static int AMPER_INDEX            = 3;
     final static int KELVIN_INDEX           = 4;
     final static int CANDELA_INDEX          = 5;
     final static int MOLE_INDEX             = 6;
     final static int RADIAN_INDEX           = 7;
     final static int STERADIAN_INDEX        = 8;
 
 
 	public final static int UNIT_CODE_UNKNOWN			= 0;
 	public final static int UNIT_CODE_KG				= 1;
 	public final static int UNIT_CODE_G				= 2;
 	public final static int UNIT_CODE_MT				= 3;
 	public final static int UNIT_CODE_LB				= 4;
 	public final static int UNIT_CODE_OZ				= 5;
 	public final static int UNIT_CODE_AMU				= 6;
 	public final static int UNIT_CODE_METER				= 7;
 	public final static int UNIT_CODE_INCH				= 8;
 	public final static int UNIT_CODE_YARD				= 9;
 	public final static int UNIT_CODE_FEET				= 10;
 	public final static int UNIT_CODE_MILE_ST			= 11;
 	public final static int UNIT_CODE_MICRON			= 12;
 	public final static int UNIT_CODE_S				= 13;
 	public final static int UNIT_CODE_MIN				= 14;
 	public final static int UNIT_CODE_HOUR				= 15;
 	public final static int UNIT_CODE_DAY				= 16;
 	public final static int UNIT_CODE_CELSIUS			= 17;
 	public final static int UNIT_CODE_KELVIN			= 18;
 	public final static int UNIT_CODE_FAHRENHEIT		= 19;
 	public final static int UNIT_CODE_M2				= 20;
 	public final static int UNIT_CODE_ACRE				= 21;
 	public final static int UNIT_CODE_ARE				= 22;
 	public final static int UNIT_CODE_HECTARE			= 23;
 	public final static int UNIT_CODE_M3				= 24;
 	public final static int UNIT_CODE_LITER				= 25;
 	public final static int UNIT_CODE_CC				= 26;
 	public final static int UNIT_CODE_BBL_D				= 27;
 	public final static int UNIT_CODE_BBL_L				= 28;
 	public final static int UNIT_CODE_BU				= 29;
 	public final static int UNIT_CODE_GAL_D			= 30;
 	public final static int UNIT_CODE_GAL_L				= 31;
 	public final static int UNIT_CODE_PT_D				= 32;
 	public final static int UNIT_CODE_PT_L				= 33;
 	public final static int UNIT_CODE_QT_D				= 34;
 	public final static int UNIT_CODE_QT_L				= 35;
 	public final static int UNIT_CODE_JOULE				= 36;
 	public final static int UNIT_CODE_CALORIE			= 37;
 	public final static int UNIT_CODE_EV				= 38;
 	public final static int UNIT_CODE_ERG				= 39;
 	public final static int UNIT_CODE_WHR				= 40;
 	public final static int UNIT_CODE_NEWTON			= 41;
 	public final static int UNIT_CODE_DYNE				= 42;
 	public final static int UNIT_CODE_KGF				= 43;
 	public final static int UNIT_CODE_LBF				= 44;
 	public final static int UNIT_CODE_WATT				= 45;
 	public final static int UNIT_CODE_HP_MECH			= 46;
 	public final static int UNIT_CODE_HP_EL				= 47;
 	public final static int UNIT_CODE_HP_METR			= 48;
 	public final static int UNIT_CODE_PASCAL			= 49;
 	public final static int UNIT_CODE_BAR				= 50;
 	public final static int UNIT_CODE_ATM				= 51;
 	public final static int UNIT_CODE_MMHG				= 52;
 	public final static int UNIT_CODE_CMH2O				= 53;
 	public final static int UNIT_CODE_TORR				= 54;
 	public final static int UNIT_CODE_ANG_VEL			= 55;
 	public final static int UNIT_CODE_LINEAR_VEL		= 56;
 	public final static int UNIT_CODE_AMPERE			= 57;
 	public final static int UNIT_CODE_VOLT				= 58;
 	public final static int UNIT_CODE_COULOMB			= 59;
 	public final static int UNIT_CODE_MILLIVOLT			= 60;
 	public final static int UNIT_CODE_LUMEN				= 61;
 	public final static int UNIT_CODE_LUX				= 62;
 	public final static int UNIT_CODE_CENTIMETER        = 63;
 	public final static int UNIT_CODE_MILLISECOND       = 64;
 	public final static int UNIT_CODE_LINEAR_VEL_MILLISECOND = 65;
 	public final static int UNIT_CODE_KILOMETER			= 66;
 	public final static int UNIT_CODE_LINEAR_VEL_KMH	= 67;
 	
 	public final static int UNIT_TABLE_LENGTH           = 68;
 
 	public final static int UNIT_CAT_UNKNOWN			= 0;
 	public final static int UNIT_CAT_LENGTH				= 1;
 	public final static int UNIT_CAT_MASS				= 2;
 	public final static int UNIT_CAT_TIME				= 3;
 	public final static int UNIT_CAT_TEMPERATURE		= 4;
 	public final static int UNIT_CAT_AREA				= 5;
 	public final static int UNIT_CAT_VOL_CAP			= 6;
 	public final static int UNIT_CAT_ENERGY				= 7;
 	public final static int UNIT_CAT_FORCE				= 8;
 	public final static int UNIT_CAT_POWER				= 9;
 	public final static int UNIT_CAT_PRESSURE			= 10;
 	public final static int UNIT_CAT_ELECTRICITY		= 11;
 	public final static int UNIT_CAT_LIGHT				= 12;
 	public final static int UNIT_CAT_MISC				= 13;
 	public final static int UNIT_CAT_VELOCITY			= 14;
 	public final static int UNIT_CAT_ACCELERATION		= 15;
 	public final static int UNIT_CAT_TABLE_LENGTH		= 16;
 
 	public static String[] catNames = {"Unknown", 
 									   "Length",
 									   "Mass",
 									   "Time",
 									   "Temperature",
 									   "Area",
 									   "Volumes/Capacity",
 									   "Energy",
 									   "Force",
 									   "Power",
 									   "Pressure",
 									   "Electricity",
 									   "Light",
 									   "Miscellaneous",
 									   "Velocity",
 									   "Acceleration"};
 
     static byte [][]catNumbers = new byte[UNIT_CAT_TABLE_LENGTH][9];
     
     static{
         boolean []flags = new boolean[UNIT_CAT_TABLE_LENGTH];
         for(int u = 1; u < UNIT_TABLE_LENGTH; u++){
             Unit unit = getUnit(u);
             if(!flags[unit.unitCategory]){
                 flags[unit.unitCategory] = true;
                 catNumbers[unit.unitCategory][METER_INDEX]      = unit.meter;
                 catNumbers[unit.unitCategory][KG_INDEX]         = unit.kg;
                 catNumbers[unit.unitCategory][SEC_INDEX]        = unit.sec;
                 catNumbers[unit.unitCategory][AMPER_INDEX]      = unit.amper;
                 catNumbers[unit.unitCategory][KELVIN_INDEX]     = unit.kelvin;
                 catNumbers[unit.unitCategory][CANDELA_INDEX]    = unit.candela;
                 catNumbers[unit.unitCategory][MOLE_INDEX]       = unit.mole;
                 catNumbers[unit.unitCategory][RADIAN_INDEX]     = unit.radian;
                 catNumbers[unit.unitCategory][STERADIAN_INDEX]  = unit.steradian;
             }
         }
     }
 
     public static byte[] getCategoryNumber(int category){
         if(category < 0 || category >= UNIT_CAT_TABLE_LENGTH) return null;
         return catNumbers[category];
     }
 
 	public Unit(String name,String abbreviation,boolean derived,int unitCategory,int code,int baseUnit,
 	            byte meter,byte kg,byte sec,byte amper,byte kelvin,byte candela,byte mole,byte radian,byte steradian,
 	            float koeffA,float koeffB,boolean dimLess,boolean doMetricPrefix){
 		this.name 		= name;
 		this.abbreviation 	= abbreviation;
 		this.derived 		= derived;
 		this.unitCategory 	= unitCategory;
 		this.code 			= code;
 		this.baseUnit 		= baseUnit;
 		this.meter		= meter;
 		this.kg			= kg;
 		this.sec			= sec;
 		this.amper 		= amper;
 		this.kelvin 		=  kelvin;
 		this.candela 		= candela;
 		this.mole 			= mole;
 		this.radian 		= radian;
 		this.steradian 		= steradian;
 		this.koeffA		= koeffA;
 		this.koeffB		= koeffB;
 		this.dimLess		= dimLess;
 		this.doMetricPrefix = doMetricPrefix;
 		
 	} 
 
 	public String getDimension(){return abbreviation;}
 	
 	public void setDimension(String dimension){abbreviation = dimension;}
 	
 	public static Vector getCatUnitAbbrev(int index)
 	{
 		Vector abbrevs = new Vector();
 		for(int i = 1; i < UNIT_TABLE_LENGTH; i++){
 			Unit u = getUnit(i);
 			if(u.unitCategory == index){
 				abbrevs.addElement(u.abbreviation);
 			}
 		}
 		return abbrevs;
 	}
 
 	public static Unit     getUnit(int code){
 		if(code < 0) return null;
 		switch(code){
 		case UNIT_CODE_KG :
 			return new Unit("kilogram","kg",false,UNIT_CAT_MASS,UNIT_CODE_KG,UNIT_CODE_KG,
 							  (byte)0,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1.0f,0.0f,false,false);
 		case UNIT_CODE_G :
 			return new Unit("gram","g",true,UNIT_CAT_MASS,UNIT_CODE_G,UNIT_CODE_KG,
 							  (byte)0,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.001f,0.0f,false,true); 
 		case UNIT_CODE_MT :
 			return new Unit("metric ton","tn",true,UNIT_CAT_MASS,UNIT_CODE_MT,UNIT_CODE_KG,
 							  (byte)0,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1000f,0.0f,false,true); 
 		case UNIT_CODE_LB : 
 			return new Unit("pound","lb",true,UNIT_CAT_MASS,UNIT_CODE_LB,UNIT_CODE_KG,
 							  (byte)0,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.45359237f,0.0f,false,false); 
 		case UNIT_CODE_OZ : 
 			return new Unit("ounce","oz",true,UNIT_CAT_MASS,UNIT_CODE_OZ,UNIT_CODE_G,
 							  (byte)0,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.028349523f,0.0f,false,false); 
 		case UNIT_CODE_AMU : 
 			return new Unit("atomic mass unit","amu",true,UNIT_CAT_MASS,UNIT_CODE_AMU,UNIT_CODE_KG,
 							  (byte)0,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1.66054e-27f,0.0f,false,false); 
 		case UNIT_CODE_KILOMETER : 
 			return new Unit("kilometer","m",false,UNIT_CAT_LENGTH,UNIT_CODE_METER,UNIT_CODE_METER,
 							  (byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1000f,0.0f,false,true); 
 		case UNIT_CODE_METER : 
 			return new Unit("meter","m",false,UNIT_CAT_LENGTH,UNIT_CODE_METER,UNIT_CODE_METER,
 							  (byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1f,0.0f,false,true); 
 		case UNIT_CODE_CENTIMETER :
 			return new Unit("centimeter","cm",false,UNIT_CAT_LENGTH,code,UNIT_CODE_METER,
 							  (byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.01f,0.0f,false,false); 
 		case UNIT_CODE_INCH : 
 			return new Unit("inch","in",false,UNIT_CAT_LENGTH,UNIT_CODE_INCH,UNIT_CODE_METER,
 							  (byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.0254f,0.0f,false,false); 
 		case UNIT_CODE_YARD : 
 			return new Unit("yard","yd",false,UNIT_CAT_LENGTH,UNIT_CODE_YARD,UNIT_CODE_METER,
 							  (byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.9144f,0.0f,false,false); 
 		case UNIT_CODE_FEET :
 			return new Unit("feet","ft",false,UNIT_CAT_LENGTH,UNIT_CODE_FEET,UNIT_CODE_METER,
 							  (byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.3048f,0.0f,false,false); 
 		case UNIT_CODE_MILE_ST :
 			return new Unit("mile (statute)","mi",false,UNIT_CAT_LENGTH,UNIT_CODE_MILE_ST,UNIT_CODE_METER,
 							  (byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1609.344f,0.0f,false,false); 
 		case UNIT_CODE_MICRON :
 			return new Unit("micron","?",false,UNIT_CAT_LENGTH,UNIT_CODE_MICRON,UNIT_CODE_METER,
 							  (byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1e-6f,0.0f,false,false); 
 		case UNIT_CODE_MILLISECOND : 
 			return new Unit("millisecond","ms",false,UNIT_CAT_TIME,UNIT_CODE_MILLISECOND,UNIT_CODE_S,
 							  (byte)0,(byte)0,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.001f,0.0f,false,true); 
 		case UNIT_CODE_S : 
 			return new Unit("second","s",false,UNIT_CAT_TIME,UNIT_CODE_S,UNIT_CODE_S,
 							  (byte)0,(byte)0,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1f,0.0f,false,true); 
 		case UNIT_CODE_MIN :
 			return new Unit("minute","min",false,UNIT_CAT_TIME,UNIT_CODE_MIN,UNIT_CODE_S,
 							  (byte)0,(byte)0,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  60f,0.0f,false,false); 
 		case UNIT_CODE_HOUR :
 			return new Unit("hour","hr",false,UNIT_CAT_TIME,UNIT_CODE_HOUR,UNIT_CODE_S,
 							  (byte)0,(byte)0,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  3600f,0.0f,false,false); 
 		case UNIT_CODE_DAY :
 			return new Unit("day","d",false,UNIT_CAT_TIME,UNIT_CODE_DAY,UNIT_CODE_S,
 							  (byte)0,(byte)0,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  86400f,0.0f,false,false); 
 		case UNIT_CODE_CELSIUS :
 			return new Unit("Celsius","degC",false,UNIT_CAT_TEMPERATURE,UNIT_CODE_CELSIUS,UNIT_CODE_CELSIUS,
 							  (byte)0,(byte)0,(byte)0,(byte)0,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1f,0.0f,false,false); 
 		case UNIT_CODE_KELVIN :
 			return new Unit("Kelvin","K",false,UNIT_CAT_TEMPERATURE,UNIT_CODE_KELVIN,UNIT_CODE_CELSIUS,
 							  (byte)0,(byte)0,(byte)0,(byte)0,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1f,-273.15f,false,true); 
 		case UNIT_CODE_FAHRENHEIT :
 			return new Unit("Fahrenheit","degF",false,UNIT_CAT_TEMPERATURE,UNIT_CODE_FAHRENHEIT,UNIT_CODE_CELSIUS,
 							  (byte)0,(byte)0,(byte)0,(byte)0,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,
 							  .55555555556f,-17.777777778f,false,false); 
 		case UNIT_CODE_M2 :
 			return new Unit("m2","m2",false,UNIT_CAT_AREA,UNIT_CODE_M2,UNIT_CODE_M2,
 							  (byte)2,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1f,0.0f,false,false); 
 		case UNIT_CODE_ACRE :
 			return new Unit("acre","acre",false,UNIT_CAT_AREA,UNIT_CODE_ACRE,UNIT_CODE_M2,
 							  (byte)2,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  4046.8564f,0.0f,false,false); 
 		case UNIT_CODE_ARE :
 			return new Unit("are","a",false,UNIT_CAT_AREA,UNIT_CODE_ARE,UNIT_CODE_M2,
 							  (byte)2,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  100f,0.0f,false,false); 
 		case UNIT_CODE_HECTARE :
 			return new Unit("hectare","ha",true,UNIT_CAT_AREA,UNIT_CODE_HECTARE,UNIT_CODE_M2,
 							  (byte)2,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  10000f,0.0f,false,false); 
 		case UNIT_CODE_M3 :
 			return new Unit("m3","m3",true,UNIT_CAT_VOL_CAP,UNIT_CODE_M3,UNIT_CODE_M3,
 							  (byte)3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1f,0.0f,false,false); 
 		case UNIT_CODE_LITER :
 			return new Unit("liter","L",true,UNIT_CAT_VOL_CAP,UNIT_CODE_LITER,UNIT_CODE_M3,
 							  (byte)3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.001f,0.0f,false,true); 
 		case UNIT_CODE_CC :
 			return new Unit("cc","cc",true,UNIT_CAT_VOL_CAP,UNIT_CODE_CC,UNIT_CODE_M3,
 							  (byte)3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.000001f,0.0f,false,false); 
 		case UNIT_CODE_BBL_D :
 			return new Unit("barrel","bbl",true,UNIT_CAT_VOL_CAP,UNIT_CODE_BBL_D,UNIT_CODE_M3,
 							  (byte)3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.11562712f,0.0f,false,false); 
 		case UNIT_CODE_BBL_L :
 			return new Unit("barrel (l)","bbl",true,UNIT_CAT_VOL_CAP,UNIT_CODE_BBL_L,UNIT_CODE_M3,
 							  (byte)3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.11924047f,0.0f,false,false); 
 		case UNIT_CODE_BU :
 			return new Unit("bushel","bu",true,UNIT_CAT_VOL_CAP,UNIT_CODE_BU,UNIT_CODE_M3,
 							  (byte)3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.03523907f,0.0f,false,false); 
 		case UNIT_CODE_GAL_D :
 			return new Unit("gallon","gal",true,UNIT_CAT_VOL_CAP,UNIT_CODE_GAL_D,UNIT_CODE_M3,
 							  (byte)3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.00440476f,0.0f,false,false); 
 		case UNIT_CODE_GAL_L :
 			return new Unit("gallon (liq)","gal",true,UNIT_CAT_VOL_CAP,UNIT_CODE_GAL_L,UNIT_CODE_M3,
 							  (byte)3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.0037854118f,0.0f,false,false); 
 		case UNIT_CODE_PT_D :
 			return new Unit("pint","pt",true,UNIT_CAT_VOL_CAP,UNIT_CODE_PT_D,UNIT_CODE_M3,
 							  (byte)3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  5.505951e-4f,0.0f,false,false); 
 		case UNIT_CODE_PT_L :
 			return new Unit("pint (liq)","pt",true,UNIT_CAT_VOL_CAP,UNIT_CODE_PT_L,UNIT_CODE_M3,
 							  (byte)3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  4.731632e-4f,0.0f,false,false); 
 		case UNIT_CODE_QT_D :
 			return new Unit("quart","qt",true,UNIT_CAT_VOL_CAP,UNIT_CODE_QT_D,UNIT_CODE_M3,
 							  (byte)3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1.1011901e-3f,0.0f,false,false); 
 		case UNIT_CODE_QT_L :
 			return new Unit("quart (liq)","qt",true,UNIT_CAT_VOL_CAP,UNIT_CODE_QT_L,UNIT_CODE_M3,
 							  (byte)3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  9.463264e-4f,0.0f,false,false); 
 		case UNIT_CODE_JOULE :
 			return new Unit("Joule","J",true,UNIT_CAT_ENERGY,UNIT_CODE_JOULE,UNIT_CODE_JOULE,
 							  (byte)2,(byte)1,(byte)-2,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1f,0.0f,false,true); 
 		case UNIT_CODE_CALORIE :
 			return new Unit("calorie","cal",true,UNIT_CAT_ENERGY,UNIT_CODE_CALORIE,UNIT_CODE_JOULE,
 							  (byte)2,(byte)1,(byte)-2,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  4.184f,0.0f,false,true); 
 		case UNIT_CODE_EV :
 			return new Unit("eV","eV",true,UNIT_CAT_ENERGY,UNIT_CODE_EV,UNIT_CODE_JOULE,
 							  (byte)2,(byte)1,(byte)-2,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1.60219e-19f,0.0f,false,true); 
 		case UNIT_CODE_ERG :
 			return new Unit("erg","erg",true,UNIT_CAT_ENERGY,UNIT_CODE_ERG,UNIT_CODE_JOULE,
 							  (byte)2,(byte)1,(byte)-2,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1e-7f,0.0f,false,true); 
 		case UNIT_CODE_WHR :
 			return new Unit("Watt-hours","Whr",true,UNIT_CAT_ENERGY,UNIT_CODE_WHR,UNIT_CODE_JOULE,
 							  (byte)2,(byte)1,(byte)-2,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  3600f,0.0f,false,true); 
 		case UNIT_CODE_NEWTON :
 			return new Unit("Newton","N",true,UNIT_CAT_FORCE,UNIT_CODE_NEWTON,UNIT_CODE_NEWTON,
 							  (byte)1,(byte)1,(byte)-2,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1f,0.0f,false,true); 
 		case UNIT_CODE_DYNE :
 			return new Unit("dyne","dyn",true,UNIT_CAT_FORCE,UNIT_CODE_DYNE,UNIT_CODE_NEWTON,
 							  (byte)1,(byte)1,(byte)-2,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1e-5f,0.0f,false,true); 
 		case UNIT_CODE_KGF :
 			return new Unit("kilogram-force","kgf",true,UNIT_CAT_FORCE,UNIT_CODE_KGF,UNIT_CODE_NEWTON,
 							  (byte)1,(byte)1,(byte)-2,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  9.80665f,0.0f,false,false); 
 		case UNIT_CODE_LBF :
 			return new Unit("pound-force","lbf",true,UNIT_CAT_FORCE,UNIT_CODE_LBF,UNIT_CODE_NEWTON,
 							  (byte)1,(byte)1,(byte)-2,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  4.448222f,0.0f,false,false); 
 		case UNIT_CODE_WATT :
 			return new Unit("watt","W",true,UNIT_CAT_POWER,UNIT_CODE_WATT,UNIT_CODE_WATT,
 							  (byte)2,(byte)1,(byte)-3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1f,0.0f,false,true); 
 		case UNIT_CODE_HP_MECH :
 			return new Unit("horsepower","hp",true,UNIT_CAT_POWER,UNIT_CODE_HP_MECH,UNIT_CODE_WATT,
 							  (byte)2,(byte)1,(byte)-3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  745.7f,0.0f,false,false); 
 		case UNIT_CODE_HP_EL :
 			return new Unit("horsepower (el)","hp(el)",true,UNIT_CAT_POWER,UNIT_CODE_HP_EL,UNIT_CODE_WATT,
 							  (byte)2,(byte)1,(byte)-3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  746f,0.0f,false,false); 
 		case UNIT_CODE_HP_METR :
 			return new Unit("horsepower (metric)","hp(metric)",true,UNIT_CAT_POWER,UNIT_CODE_HP_METR,UNIT_CODE_WATT,
 							  (byte)2,(byte)1,(byte)-3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  735.499f,0.0f,false,false); 
 		case UNIT_CODE_PASCAL :
 			return new Unit("Pascal","Pa",true,UNIT_CAT_PRESSURE,UNIT_CODE_PASCAL,UNIT_CODE_PASCAL,
 							  (byte)-1,(byte)1,(byte)-1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1f,0.0f,false,true); 
 		case UNIT_CODE_BAR :
 			return new Unit("bar","bar",true,UNIT_CAT_PRESSURE,UNIT_CODE_BAR,UNIT_CODE_PASCAL,
 							  (byte)-1,(byte)1,(byte)-1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1e5f,0.0f,false,true);
 		case UNIT_CODE_ATM :
 			return new Unit("atmosphere","atm",true,UNIT_CAT_PRESSURE,UNIT_CODE_ATM,UNIT_CODE_PASCAL,
 							  (byte)-1,(byte)1,(byte)-1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1.01325e5f,0.0f,false,false); 
 		case UNIT_CODE_MMHG :
 			return new Unit("mm Hg","mmHg",true,UNIT_CAT_PRESSURE,UNIT_CODE_MMHG,UNIT_CODE_PASCAL,
 							  (byte)-1,(byte)1,(byte)-1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  133.3224f,0.0f,false,false);
 		case UNIT_CODE_CMH2O :
 			return new Unit("cm H2O","cmH2O",true,UNIT_CAT_PRESSURE,UNIT_CODE_CMH2O,UNIT_CODE_PASCAL,
 							  (byte)-1,(byte)1,(byte)-1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  98.0638f,0.0f,false,false); 
 		case UNIT_CODE_TORR :
 			return new Unit("torr","torr",true,UNIT_CAT_PRESSURE,UNIT_CODE_TORR,UNIT_CODE_PASCAL,
 							  (byte)-1,(byte)1,(byte)-1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  133.3224f,0.0f,false,true); 
 		case UNIT_CODE_ANG_VEL :
 			return new Unit("rad/s","rad/s",true,UNIT_CAT_MISC,UNIT_CODE_ANG_VEL,UNIT_CODE_ANG_VEL,
 							  (byte)0,(byte)0,(byte)-1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)1,(byte)0,
 							  1f,0.0f,false,false); 
 		case UNIT_CODE_LINEAR_VEL :
 			return new Unit("m/s","m/s",true,UNIT_CAT_VELOCITY,UNIT_CODE_LINEAR_VEL,UNIT_CODE_LINEAR_VEL,
 							  (byte)1,(byte)0,(byte)-1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1f,0.0f,false,true); 
 		case UNIT_CODE_LINEAR_VEL_MILLISECOND :
 			return new Unit("m/ms","m/ms",true,UNIT_CAT_VELOCITY,UNIT_CODE_LINEAR_VEL_MILLISECOND,UNIT_CODE_LINEAR_VEL,
 							  (byte)1,(byte)0,(byte)-1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1000f,0.0f,false,true); 
 		case UNIT_CODE_LINEAR_VEL_KMH :
 			return new Unit("km/h","km/h",true,UNIT_CAT_VELOCITY,UNIT_CODE_LINEAR_VEL_KMH,UNIT_CODE_LINEAR_VEL,
 							  (byte)1,(byte)0,(byte)-1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
							  1f/3.6f,0.0f,false,true); 
 		case UNIT_CODE_AMPERE :
 			return new Unit("ampere","A",false,UNIT_CAT_ELECTRICITY,UNIT_CODE_AMPERE,UNIT_CODE_AMPERE,
 							  (byte)0,(byte)0,(byte)0,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1f,0.0f,false,true); 
 		case UNIT_CODE_VOLT :
 			return new Unit("volt","V",true,UNIT_CAT_ELECTRICITY,UNIT_CODE_VOLT,UNIT_CODE_VOLT,
 							  (byte)2,(byte)1,(byte)-3,(byte)-1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1f,0.0f,false,true); 
 		case UNIT_CODE_COULOMB :
 			return new Unit("coulomb","Q",true,UNIT_CAT_ELECTRICITY,UNIT_CODE_COULOMB,UNIT_CODE_COULOMB,
 							  (byte)0,(byte)0,(byte)1,(byte)1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  1f,0.0f,false,true);
 		case UNIT_CODE_MILLIVOLT :
 			return new Unit("millivolt","mV",true,UNIT_CAT_ELECTRICITY,UNIT_CODE_MILLIVOLT,UNIT_CODE_VOLT,
 							  (byte)2,(byte)1,(byte)-3,(byte)-1,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.001f,0.0f,false,false); 
 		case UNIT_CODE_LUMEN :
 			return new Unit("lumen","lm",true,UNIT_CAT_POWER,UNIT_CODE_LUMEN,UNIT_CODE_WATT,
 							  (byte)2,(byte)1,(byte)-3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.0014641288f,0.0f,false,true); 
 		case UNIT_CODE_LUX :
 			return new Unit("lux","lx",true,UNIT_CAT_LIGHT,UNIT_CODE_LUX,UNIT_CODE_LUX,
 							  (byte)0,(byte)1,(byte)-3,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,
 							  0.0014641288f,0.0f,false,true);
 		}
 
 		return null;
 	}
 
 	public static Unit findUnit(String unitAbbreviation)
 	{
 		for(int i = 1; i < UNIT_TABLE_LENGTH; i++){
 			Unit u = getUnit(i);
 			if(u.abbreviation.equals(unitAbbreviation)) {
 				return u;
 			}
 		}
 		
 		return null;
 	}
 	
 	public static int errorConvertStatus = 0;
 	public static Unit sourceGlobalUnit = null;
 	public static Unit destGlobalUnit = null;
 	
 	public static float unitConvert(Unit unitSrc, float srcValue,Unit unitDest){
 		float retValue = srcValue;
 		errorConvertStatus = 0;
 		if(!isUnitCompatible(unitSrc,unitDest)){
 			errorConvertStatus = 1;
 			return retValue;
 		}
 /*
 		Unit unitSrcBase = Unit.getUnit(unitSrc.baseUnit);
 		Unit unitDestBase = Unit.getUnit(unitDest.baseUnit);
 		if(unitSrcBase == null || unitDestBase == null || unitSrcBase != unitDestBase){
 			errorConvertStatus = 3;
 			return retValue;
 		}
 */
 		retValue = ((srcValue*unitSrc.koeffA + unitSrc.koeffB) - unitDest.koeffB)/ unitDest.koeffA;
 		return retValue;
 	}
 	public static float unitConvert(float srcValue){
 		return unitConvert(sourceGlobalUnit,srcValue,destGlobalUnit);
 	}
 	
 	public boolean  setGlobalUnits(int unitIDSrc,int unitIDDest){
 		sourceGlobalUnit = destGlobalUnit = null;
 		sourceGlobalUnit = Unit.getUnit(unitIDSrc);
 		if(sourceGlobalUnit == null) return false;
 		destGlobalUnit = Unit.getUnit(unitIDDest);
 		if(destGlobalUnit == null) return false;
 		return true;
 	}
 	
 	public static boolean isUnitCompatible(Unit unitSrc,Unit unitDest){
 		if(unitSrc == null || unitDest == null) return false;
 		return ((unitSrc.meter == unitDest.meter) &&
 		    (unitSrc.kg == unitDest.kg) &&
 		    (unitSrc.sec == unitDest.sec) &&
 		    (unitSrc.amper == unitDest.amper) &&
 		    (unitSrc.kelvin == unitDest.kelvin) &&
 		    (unitSrc.candela == unitDest.candela) &&
 		    (unitSrc.mole == unitDest.mole) &&
 		    (unitSrc.radian == unitDest.radian) &&
 		    (unitSrc.steradian == unitDest.steradian));
 	}
 	public static boolean isUnitCompatible(int unitIDSrc,int unitIDDest){
 		return isUnitCompatible(Unit.getUnit(unitIDSrc),Unit.getUnit(unitIDDest));
 	}
 	
 	public static float unitConvert(int unitIDSrc, float srcValue,int unitIDDest){
 		return unitConvert(Unit.getUnit(unitIDSrc),srcValue,Unit.getUnit(unitIDDest));
 	}
 	public static String getPrefixStringForUnit(Unit p,int order){
 		String retValue = null;
 		if(p == null || !p.doMetricPrefix) return retValue;
 		switch(order){
 			case -12: 	retValue = "p"; break;
 			case -9:	retValue = "n"; break;
 			case -6:	retValue = "?"; break;
 			case -3:	retValue = "m"; break;
 			case -2:	retValue = "c"; break;
 			case -1:	retValue = "d"; break;
 			case 3:	retValue = "k"; break;
 			case 6:	retValue = "M"; break;
 			case 9:	retValue = "G"; break;
 		}
 		return retValue;
 	}
 	public static float getPrefixKoeffForUnit(Unit p,int order){
 		float retValue = -1f;
 		if(p == null || !p.doMetricPrefix) return retValue;
 		switch(order){
 			case -12: 	retValue = 1e-12f; break;
 			case -9:	retValue = 1e-9f; break;
 			case -6:	retValue = 1e-6f; break;
 			case -3:	retValue = 1e-3f; break;
 			case -2:	retValue = 0.01f; break;
 			case -1:	retValue = 0.1f; break;
 			case 3:	retValue = 1000f; break;
 			case 6:	retValue = 1e6f; break;
 			case 9:	retValue = 1e9f; break;
 		}
 		return retValue;
 	}
 	
 	protected static float calcKoeff0(byte order,float kpart){
 	    float k = 1;
 	    if(order == 0) return k;
 	    int n = Math.abs(order);
 	    for(int i = 0; i < n; i++) k = (order < 0)?(k/kpart):k*kpart;
 	    return k;
 	}
 	
 	public static Unit createMechanicUnit(byte meter,byte kg,byte sec,float k){
 	    return new Unit("Custom","",true,UNIT_CAT_UNKNOWN,UNIT_CODE_UNKNOWN,UNIT_CODE_UNKNOWN,meter,kg,sec,NON_ORDER,NON_ORDER,NON_ORDER,NON_ORDER,NON_ORDER,NON_ORDER,k,0,false,false);
 	}
 
 	public static Unit createMechanicUnit(byte meter,byte kg,byte sec,float kmeter,float kkg, float ksec){
         float k = calcKoeff0(meter,kmeter)*calcKoeff0(kg,kkg)*calcKoeff0(sec,ksec);
         return createMechanicUnit(meter,kg,sec,k);
 	}
 
     public static Unit findKnownMechanicUnit(int meter,int kg,int sec,float kmeter,float kkg, float ksec){
 		Unit needUnit = createMechanicUnit((byte)meter,(byte)kg,(byte)sec,kmeter,kkg,ksec);
 		Unit cat = null;
 		for(int i = 1; i < UNIT_TABLE_LENGTH; i++){
 		    Unit tunit = getUnit(i);
 			if(!isUnitCompatible(tunit,needUnit)) continue;
 			if(needUnit.equals(tunit)){
 			    cat = tunit;
 			    break;
 			}
 		}
 		if(cat == null) return needUnit;
 		return cat;
     }
     public static Unit findKnownMechanicUnit(int unitMeter,int nmeter,int unitKg,int nkg,int unitSec,int nsec){
 		Unit u = getUnit(unitMeter);
 		if(u == null) return null;
 		if(u.unitCategory != UNIT_CAT_LENGTH) return null;
 		int umeter = u.meter*nmeter;
 		float kmeter = u.koeffA;
 		u = getUnit(unitKg);
 		if(u == null) return null;
 		if(u.unitCategory != UNIT_CAT_MASS) return null;
 		int ukg = u.kg*nkg;
 		float kkg = u.koeffA;
 		u = getUnit(unitSec);
 		if(u == null) return null;
 		if(u.unitCategory != UNIT_CAT_TIME) return null;
 		int usec = u.sec*nsec;
 		float ksec = u.koeffA;
 		return findKnownMechanicUnit(umeter,ukg,usec,kmeter,kkg,ksec);
     }
 	
     public static Unit findKnownMechanicUnit(int category,int unitMeter,int unitKg,int unitSec){
         byte []catn = getCategoryNumber(category);
         if(catn == null) return null;
         return findKnownMechanicUnit(unitMeter,catn[METER_INDEX],unitKg,catn[KG_INDEX],unitSec,catn[SEC_INDEX]);
     }
 	
 	public static Unit getUnitFromBasics(int unitCategory, 
 		int unitMeter, int unitKg, int unitSec, int unitAmper, int unitKelvin, 
 		int unitCandela, int unitMole, int unitRadian, int unitSteradian)
 	{
 		float koeffA=1f;
 		float koeffB=0f;
 		
 		Unit cat=null, u;
 		
 		int i;
 		for(i = 1; i < UNIT_TABLE_LENGTH; i++){
 			cat = getUnit(i);
 			if(cat.unitCategory == unitCategory){
 				break;
 			}
 		}
 		if (i == UNIT_TABLE_LENGTH) return null;
 		
 		u = getUnit(unitMeter);
 		if (u!=null){
 //System.out.println(u.name + " has koeffA: "+u.koeffA+" "+koeffA + "*" +u.koeffA+"^"+cat.meter);
 			koeffA=koeffA*(float)Math.pow(u.koeffA,cat.meter);
 		}
 		u = getUnit(unitKg);
 		if (u!=null){
 //System.out.println(u.name + " has koeffA: "+u.koeffA+" "+koeffA + "*" +u.koeffA+"^"+cat.kg);
 			koeffA=koeffA*(float)Math.pow(u.koeffA,cat.kg);
 		}
 		u = getUnit(unitSec);
 		if (u!=null){
 //System.out.println(u.name + " has koeffA: "+u.koeffA+" "+koeffA + "*" +u.koeffA+"^"+cat.sec);
 			koeffA=koeffA*(float)Math.pow(u.koeffA,cat.sec);
 		}
 		u = getUnit(unitAmper);
 		if (u!=null){
 			koeffA=koeffA*(float)Math.pow(u.koeffA,cat.amper);
 		}
 		u = getUnit(unitKelvin);
 		if (u!=null){
 			koeffA=koeffA*(float)Math.pow(u.koeffA,cat.kelvin);
 		}
 		u = getUnit(unitCandela);
 		if (u!=null){
 			koeffA=koeffA*(float)Math.pow(u.koeffA,cat.candela);
 		}
 		u = getUnit(unitMole);
 		if (u!=null){
 			koeffA=koeffA*(float)Math.pow(u.koeffA,cat.mole);
 		}
 		u = getUnit(unitRadian);
 		if (u!=null){
 			koeffA=koeffA*(float)Math.pow(u.koeffA,cat.radian);
 		}
 		u = getUnit(unitSteradian);
 		if (u!=null){
 			koeffA=koeffA*(float)Math.pow(u.koeffA,cat.steradian);
 		}
 			
 System.out.println("koeffA: "+koeffA);		
 		
 		for(i = 1; i < UNIT_TABLE_LENGTH; i++){
 			u = getUnit(i);
 			if(u.unitCategory == unitCategory){
 				if (u.koeffA==koeffA){
 					return u;
 				}
 			}
 		}
 		
 		return new Unit("Unknown","?",true,unitCategory,-1,-1,
 	            cat.meter,cat.kg,cat.sec,cat.amper,cat.kelvin,
 	            cat.candela,cat.mole,cat.radian,cat.steradian,
 	            koeffA,koeffB,false,false);
 	}
 	
 	public String toString(){
 	    String ret = "Unit: ";
 	    ret += name+":"+abbreviation+": "+meter+":";
 	    ret += kg+":";
 	    ret += sec+":";
 	    ret += amper+":";
 	    ret += kelvin+":";
 	    ret += candela+":";
 	    ret += mole+":";
 	    ret += radian+":";
 	    ret += steradian+":   ";
 	    ret += koeffA+":";
 	    ret += koeffB;
 	    return ret;
 	}
 	
     public synchronized boolean equals(Object obj) {
         if(!(obj instanceof Unit)) return false;
         if(obj == this) return true;
         Unit unit = (Unit)obj;
         if(unit.meter != meter) return false;
         if(unit.kg != kg) return false;
         if(unit.sec != sec) return false;
         if(unit.amper != amper) return false;
         if(unit.kelvin != kelvin) return false;
         if(unit.candela != candela) return false;
         if(unit.mole != mole) return false;
         if(unit.radian != steradian) return false;
         if(!MathUtil.equalWithTolerance(unit.koeffA,koeffA,1e-4f)) return false;
         if(!MathUtil.equalWithTolerance(unit.koeffB,koeffB,1e-4f)) return false;
         return true;
     }
 }
