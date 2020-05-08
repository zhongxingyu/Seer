 package edu.rpi.rocs;
 
 import java.util.Hashtable;
 
 /**
  * Stores assignments of unique identifiers to WSDL types, as well as service-
  * dependent complex types (structures).
  * 
  * @author ewpatton
  * @version %I%
  */
 public class WSDLTypes {
 	public static final int xsdAnyType=0;
 	public static final int xsdAnyComplexType=1;
 	public static final int xsdAnySimpleType=2;
 	public static final int xsdAnyAtmoicType=3;
 	public static final int xsdAnyURI=4;
 	public static final int xsdBase64Binary=5;
 	public static final int xsdBoolean=6;
 	public static final int xsdDate=7;
 	public static final int xsdDateTime=8;
 	public static final int xsdDateTimeStamp=9;
 	public static final int xsdDecimal=10;
 	public static final int xsdInteger=11;
 	public static final int xsdLong=12;
 	public static final int xsdInt=13;
 	public static final int xsdShort=14;
 	public static final int xsdByte=15;
 	public static final int xsdNonNegativeInteger=16;
 	public static final int xsdPositiveInteger=17;
 	public static final int xsdUnsignedLong=18;
 	public static final int xsdUnsignedInt=19;
 	public static final int xsdUnsignedShort=20;
 	public static final int xsdUnsignedByte=21;
 	public static final int xsdNonPositiveInteger=22;
 	public static final int xsdNegativeInteger=23;
 	public static final int xsdDouble=24;
 	public static final int xsdDuration=25;
 	public static final int xsdDayTimeDuration=26;
 	public static final int xsdYearMonthDuration=27;
 	public static final int xsdFloat=28;
 	public static final int xsdGDay=29;
 	public static final int xsdGMonth=30;
 	public static final int xsdGMonthDay=31;
 	public static final int xsdGYear=32;
 	public static final int xsdGYearMonth=33;
 	public static final int xsdHexBinary=34;
 	public static final int xsdNOTATION=35;
 	public static final int xsdPrecisionDecimal=36;
 	public static final int xsdQName=37;
 	public static final int xsdString=38;
 	public static final int xsdNormalizedString=39;
 	public static final int xsdToken=40;
 	public static final int xsdLanguage=41;
 	public static final int xsdName=42;
 	public static final int xsdNCName=43;
 	public static final int xsdENTITY=44;
 	public static final int xsdID=45;
 	public static final int xsdIDREF=46;
 	public static final int xsdNMTOKEN=47;
 	public static final int xsdENTITIES=48;
 	public static final int xsdIDREFS=49;
 	public static final int xsdNMTOKENS=50;
 	
	static Hashtable<String, Integer> nameIndexMap;
 	static int counter=51;
 	
 	public static int getComplexTypeIndex(String aName) {
 		if(nameIndexMap.contains(aName)) {
 			return ((Integer)nameIndexMap.get(aName)).intValue();
 		}
 		nameIndexMap.put(aName, new Integer(counter));
 		counter++;
 		return counter-1;
 	}
 }
