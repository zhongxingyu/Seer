 import java.util.*;
 import java.lang.*;
 import java.io.*;
 import java.text.*;
 
 public class Datum  {
 	private static final int FIRST_DATUM_TYPE = 0;
 	public static final int UNASKED = 0;		// haven't asked
 	public static final int NA = 1;				// don't need to ask - not applicable
 	public static final int REFUSED = 2;		// question asked, but subject refuses to answer
 	public static final int INVALID = 3;		// if an exception occurs - so propagated
 	public static final int UNKNOWN = 4;		// subject indicates that they don't know the answer
 	public static final int NOT_UNDERSTOOD = 5;
 	public static final int NUMBER = 6;
 	public static final int STRING = 7;
 	public static final int DATE = 8;
 	public static final int TIME = 9;
 	public static final int YEAR = 10;
 	public static final int MONTH = 11;
 	public static final int DAY = 12;
 	public static final int WEEKDAY = 13;
 	public static final int HOUR = 14;
 	public static final int MINUTE = 15;
 	public static final int SECOND = 16;
 	public static final int MONTH_NUM = 17;
 	public static final int DAY_NUM = 18;
 	private static final int LAST_DATUM_TYPE = 18;
 	
 	private static final Date SAMPLE_DATE = new Date(System.currentTimeMillis());
 	private static final Double SAMPLE_NUMBER = new Double(12345.678);	
 	
 	private static final String SPECIAL_TYPES[] = { "*UNASKED*", "*N/A*", "*REFUSED*", "*INVALID*", "*UNKNOWN*", "*HUH?*" };
 	private static final String DATUM_TYPES[] = { "number", "string", "date", "time", "year", "month", "day", "weekday", "hour", "minute", "second", "month_num", "day_num" };
 
 	private static final String defaultDateFormat = "MM/dd/yyyy";
 	private static final String defaultMonthFormat = "MMMM";
 	private static final String defaultTimeFormat = "HH:mm:ss";
 	private static final String defaultYearFormat = "yyyy";
 	private static final String defaultDayFormat = "d";
 	private static final String defaultWeekdayFormat = "E";
 	private static final String defaultHourFormat = "H";
 	private static final String defaultMinuteFormat = "m";
 	private static final String defaultSecondFormat = "s";
 	private static final String defaultNumberFormat = null;	// so that Triceps pretty-prints it.
 
 	public static final String defaultMonthNumFormat = "M";
 	public static final String defaultDayNumFormat = "D";
 	public static final String TIME_MASK = "yyyy.MM.dd..HH.mm.ss";	
 
 	private int type = INVALID;
 	private String sVal = null;
 	private double dVal = Double.NaN;
 	private Date date = null;
 	private String mask = null;
 	private String error = null;
 	private String variableName = null;
 	Triceps triceps = null;	// need package level access in DatumMath
 	private static final HashMap SPECIAL_DATA = new HashMap();
 
 	public Datum(Triceps lang, double d) { init(lang, new Double(d), NUMBER, null); }
 	public Datum(Triceps lang, long l) { init(lang, new Long(l), NUMBER, null); }
 	
 	public static Datum getInstance(Triceps lang, int i) {
 		String key = (lang.toString() + i);
 		Datum datum = (Datum) SPECIAL_DATA.get(key);
 		if (datum != null)
 			return datum;
 		
 		datum = new Datum(lang,i);
 		SPECIAL_DATA.put(key,datum);
 		return datum;
 	}
 	
 	private Datum(Triceps lang, int i) {
 		// only for creating reserved instances
 		triceps = lang;
 		type = i;
 	}
 
 	public Datum(Datum val) {
 		dVal = val.dVal;
 		sVal = val.sVal;
 		date = val.date;
 		type = val.type;
 		mask = val.mask;
 		triceps = val.triceps;
 		error = val.error;
 		variableName = val.variableName;
 	}
 
 	public Datum(Triceps lang, Date d, int t) {
 		this(lang,d,t,Datum.getDefaultMask(t));
 	}
 
 	public Datum(Triceps lang, Date d, int t, String mask) {
 		init(lang,d,t,mask);
 	}
 
 	public Datum(Triceps lang, String s, int t) {
 		init(lang,s,t,Datum.getDefaultMask(t));
 	}
 
 	public Datum(Triceps lang, String s, int t, String mask) {
 		init(lang,s,t,mask);
 	}
 	
 	public Datum cast(int newType, String newMask) {
 		/* Cast a value from one type to another */
 		
 		if (this.type == newType && this.mask == newMask) {
 			return this;
 		}
 		
 		Datum datum = null;
 		String useMask = ((newMask == null || newMask.trim().length() == 0) ? getDefaultMask(newType) : newMask);
 		
 		
 		if (this.type == newType) {
 			datum = new Datum(this);
 			datum.mask = useMask;
 			return datum;
 		}
 		
 		switch(this.type) {
 			case TIME:
 			case MONTH:
 			case DATE:
 			case YEAR:
 			case DAY:
 			case WEEKDAY:
 			case HOUR:
 			case MINUTE:
 			case SECOND:
 			case MONTH_NUM:
 			case DAY_NUM:
 				if (isDate(newType)) {
 					datum = new Datum(this);
 					datum.type = newType;
 					datum.mask = useMask;
 				}
 				else if (newType == STRING) {
 					return datum;	// don't cast to STRING
 //					datum = new Datum(triceps,this.stringVal(),STRING);
 				}
 				else {
 					datum = new Datum(triceps,INVALID);
 				}	
 				break;
 			case NUMBER:
 				if (isDate(newType)) {
 					if (newType == TIME || newType == DATE) {
 						datum = new Datum(triceps,INVALID);
 					}
 					else {
 						datum = new Datum(this);
 						datum.date = DatumMath.createDate((int) this.doubleVal(), newType);
 						datum.sVal = null;
 						datum.dVal = Double.NaN;
 						datum.type = newType;
 						datum.mask = useMask;
 					}
 				}
 				else if (newType == STRING) {
 					return datum;
 //					datum = new Datum(triceps,this.stringVal(),STRING);
 				}
 				else {
 					datum = new Datum(triceps,INVALID);
 				}
 				break;
 			case STRING:
 				/* try to parse the string using a new format */
 				datum = new Datum(triceps,this.stringVal(),newType,useMask);
 				break;
 			default:
 			case INVALID:
 			case NA:
 			case UNKNOWN:
 			case REFUSED:
 			case UNASKED:
 			case NOT_UNDERSTOOD:
 				/* can't cast any of these to a new type */
 				datum = new Datum(triceps,this.type);
 		}	
 		return datum;		
 	}
 	
 	
 	private void init(Triceps lang, Object obj, int t, String maskStr) {
     	triceps = (lang == null) ? Triceps.NULL : lang;
 		
 		if (obj == null) {
 			t = INVALID;
 		}
 		
 		dVal = Double.NaN;
 		date = null;
 		sVal = null;
 		type = t;	// assume success - enumerate failure conditions
 		
 		if (maskStr == null || maskStr.trim().length() == 0) {
 			mask = getDefaultMask(t);
 		}
 		else {
 			mask = maskStr;
 		}
 		Number num = null;
 
 		switch (t) {
 			case NUMBER: 
 				num = triceps.parseNumber(obj,mask);
 					
 				if (num == null) {
 					type = INVALID;
 				}
 				else {
 					dVal = num.doubleValue();
 				}
 				break;
 			case STRING:
 				sVal = obj.toString();
 				/* also check whether can be considered a number */
 				num = triceps.parseNumber(obj,null);
 				if (num != null) {
 					dVal = num.doubleValue();
 				}
 				break;
 			case WEEKDAY:
 			case MONTH:
 			case DATE:
 			case TIME:
 			case YEAR:
 			case DAY:
 			case HOUR:
 			case MINUTE:
 			case SECOND:
 			case MONTH_NUM:
 			case DAY_NUM:
 				date = triceps.parseDate(obj,mask);
 				if (date == null) {
 					type = INVALID;
 				}
 				else {
 					num = triceps.parseNumber(obj,null);
 					if (num != null) {
 						dVal = num.doubleValue();
 					}
 				}
 				break;
 			case REFUSED:
 			case INVALID:
 			case NA:
 				break;
 			default:
 				type = INVALID;
 				break;
 		}
 		if (type == INVALID) {
 			if (type == INVALID) {
 				if (t == INVALID) {
 					error = triceps.get("Please_answer_this_question");
 				}
 				else {
 					String ex = getExampleFormatStr(mask,t);
 					if (ex.length() > 0)
 						ex = " (e.g. " + ex + ")";
 					error = triceps.get("please_enter_a") + getTypeName(triceps,t) + ex;
 				}
 			}		
 			sVal = null;
 			dVal = Double.NaN;
 			date = null;
 		}
 	}
 
 	public Datum(Triceps lang, boolean b) {
     	triceps = (lang == null) ? Triceps.NULL : lang;
 
 		type = NUMBER;
 		dVal = (b ? 1 : 0);
 	}
 
 	public String stringVal() { return stringVal(false,mask); }
 	public String stringVal(boolean showReserved) { return stringVal(showReserved,mask); }
 
 	public String stringVal(boolean showReserved, String mask) {
 		switch(type) {
 			case TIME:
 			case MONTH:
 			case DATE:
 			case YEAR:
 			case DAY:
 			case WEEKDAY:
 			case HOUR:
 			case MINUTE:
 			case SECOND:
 			case MONTH_NUM:
 			case DAY_NUM:
 				if (mask == null)
 					return format(triceps, this,type,Datum.getDefaultMask(type));
 				else
 					return format(triceps, this, type, mask);
 			case NUMBER:
 				if (mask == null)
 					return format(triceps, this, type, Datum.getDefaultMask(type));
 				else
 					return format(triceps, this, type, mask);
 			case STRING:
 				return sVal;
 			default:
 				if (showReserved)
 					return getTypeName(triceps,INVALID);
 				else
 					return "";
 			case INVALID:
 			case NA:
 			case UNKNOWN:
 			case REFUSED:
 			case UNASKED:
 			case NOT_UNDERSTOOD:
 				if (showReserved) 
 					return getTypeName(triceps,type);
 				else
 					return "";
 		}	
 	}
 
 	public boolean booleanVal() { 
 		if (isNumeric()) { 
 			return (Double.isNaN(dVal) || (dVal == 0)) ? false : true;
 		}
 		else if (sVal != null) {
 			return Boolean.valueOf(sVal).booleanValue();
 		}
 		else {
 			return false;
 		}
 	}
 
 	public double doubleVal() { return dVal; }
 	public Date dateVal() { return date; }
 	public String monthVal() { if (date == null) return ""; return format(date,Datum.MONTH); }
 	public String timeVal() { if (date == null) return ""; return format(date,Datum.TIME); }
 	public int type() { return type; }
 	public String getMask() { return mask; }
 
 	public void setName(String name) { variableName = name; }
 	public String getName() { return variableName; }
 
 	public boolean isValid() {
 		return (isType(type) && type != INVALID);
 	}
 
 	public boolean exists() {
 		/* not only must it be valid, but STRING vals must be non-null */
 		return (type != UNASKED && isValid() && ((type == STRING) ? !sVal.equals("") : true));
 	}
 	
 	public boolean isSpecial() { return (type >= UNASKED && type <= NOT_UNDERSTOOD); }
	static public boolean isSpecical(int t) { return (t >= UNASKED && t <= NOT_UNDERSTOOD); }
 	public boolean isNumeric() { return (!Double.isNaN(dVal)); }
 	public boolean isDate() { return (date != null); }
 	static public boolean isDate(int t) { return (t >= DATE && t <= DAY_NUM); }
 
 	public boolean isType(int t) {
 		switch(t) {
 			case TIME:
 			case MONTH:
 			case DATE:
 			case YEAR:
 			case DAY:
 			case WEEKDAY:
 			case HOUR:
 			case MINUTE:
 			case SECOND:
 			case MONTH_NUM:
 			case DAY_NUM:
 				return (date != null);
 			case NUMBER:
 				return (type == NUMBER);
 			case STRING:
 				return (type == STRING);
 			case INVALID:
 				return (type == INVALID);
 			case NA:
 				return (type == NA);
 			case UNKNOWN:
 				return (type == UNKNOWN);
 			case REFUSED:
 				return (type == REFUSED);
 			case UNASKED:
 				return (type == UNASKED);
 			case NOT_UNDERSTOOD:
 				return (type == NOT_UNDERSTOOD);
 			default:
 				return false;
 		}
 	}
 	
 	static public boolean isValidType(int t) {
 		switch(t) {
 			case UNASKED:
 			case NA:
 			case REFUSED:
 			case INVALID:
 			case UNKNOWN:
 			case NOT_UNDERSTOOD:
 			case NUMBER:
 			case STRING:
 			case DATE:
 			case TIME:
 			case YEAR:
 			case MONTH:
 			case DAY:
 			case WEEKDAY:
 			case HOUR:
 			case MINUTE:
 			case SECOND:
 			case MONTH_NUM:
 			case DAY_NUM:
 				return true;
 			default:
 				return false;
 		}
 	}
 	
 
 	public String getError() {
 		if (error == null)
 			return "";
 
 		String temp = error;
 		error = null;
 		return temp;
 	}
 
 	public String getExampleFormatStr(String mask, int t) {
 		return getExampleFormatStr(triceps, mask, t);
 	}
 	
 	static public String getExampleFormatStr(Triceps lang, String mask, int t) {
 		switch (t) {
 			case MONTH:
 			case DATE:
 			case TIME:
 			case YEAR:
 			case DAY:
 			case WEEKDAY:
 			case HOUR:
 			case MINUTE:
 			case SECOND:
 			case MONTH_NUM:
 			case DAY_NUM:
 				if (mask == null)
 					return format(lang, SAMPLE_DATE,t,Datum.getDefaultMask(t));
 				else
 					return format(lang, SAMPLE_DATE, t, mask);
 			case NUMBER:
 				if (mask == defaultNumberFormat || mask == null)
 					return "";
 				else
 					return format(lang, SAMPLE_NUMBER, t, mask);
 			default:
 			case INVALID:
 			case NA:
 			case UNKNOWN:
 			case NOT_UNDERSTOOD:
 			case UNASKED:
 			case STRING:
 			case REFUSED:
 				return "";	// no formatting string to contrain input
 		}
 	}
 	
 	static public String getDefaultMask(int t) {
 		switch (t) {
 			case MONTH:
 				return defaultMonthFormat;
 			case DATE:
 				return defaultDateFormat;
 			case TIME:
 				return defaultTimeFormat;
 			case NUMBER:
 				return defaultNumberFormat;
 			case YEAR:
 				return defaultYearFormat;
 			case DAY:
 				return defaultDayFormat;
 			case WEEKDAY:
 				return defaultWeekdayFormat;
 			case HOUR:
 				return defaultHourFormat;
 			case MINUTE:
 				return defaultMinuteFormat;
 			case SECOND:
 				return defaultSecondFormat;
 			case MONTH_NUM:
 				return defaultMonthNumFormat;
 			case DAY_NUM:
 				return defaultDayNumFormat;
 			default:
 			case INVALID:
 			case NA:
 			case UNKNOWN:
 			case UNASKED:
 			case NOT_UNDERSTOOD:
 			case STRING:
 			case REFUSED:
 				break;
 		}
 		return null;
 	}
 
 	public String format(Datum d, String mask) {
 		return format(triceps, d, d.type(), mask);
 	}
 		
 	static public String format(Triceps lang, Object o, int type, String mask) {
 		String s;
 
 		switch (type) {
 			case MONTH:
 			case DATE:
 			case TIME:
 			case YEAR:
 			case DAY:
 			case WEEKDAY:
 			case HOUR:
 			case MINUTE:
 			case SECOND:
 			case MONTH_NUM:
 			case DAY_NUM:
 				if (o instanceof Datum) {
 					s = lang.formatDate(((Datum) o).dateVal(),mask);
 				}
 				else {
 					s = lang.formatDate(o,mask);
 				}
 				if (s != null)
 					return s;
 				break;
 			case NUMBER:
 				if (o instanceof Datum) {
 					s = lang.formatNumber(new Double(((Datum) o).doubleVal()),mask);
 				}
 				else {
 					s = lang.formatNumber(o,mask);
 				}	
 				if (s != null)
 					return s;
 				break;
 			default:
 				return Datum.getTypeName(lang,INVALID);
 			case INVALID:
 			case NA:
 			case REFUSED:
 			case UNKNOWN:
 			case NOT_UNDERSTOOD:
 				return Datum.getTypeName(lang,type);
 			case UNASKED:
 				return "";	// empty string to indicate that has not been assessed yet.
 			case STRING:
 				if (o instanceof Datum) {
 					return ((Datum) o).stringVal();
 				}
 				else {
 					return o.toString();
 				}
 		}
 		return Datum.getTypeName(lang,INVALID);
 	}
 
 	public String format(Object o, int t) {
 		return format(triceps, o,t,Datum.getDefaultMask(t));
 	}
 	
 	public String getTypeName() { return getTypeName(triceps,type); }
 	
 	static public String getSpecialName(int t) {
 		switch (t) {
 			// must have static strings for reserved words so that correctly parsed from data files
 			case UNASKED:
 			case NA:
 			case REFUSED:
 			case INVALID:
 			case UNKNOWN:
 			case NOT_UNDERSTOOD:
 				return SPECIAL_TYPES[t];
 			default:
 				return SPECIAL_TYPES[INVALID];
 		}
 	}
 
 	static public String getTypeName(Triceps lang, int t) {
 		switch (t) {
 			// must have static strings for reserved words so that correctly parsed from data files
 			case UNASKED:
 			case NA:
 			case REFUSED:
 			case INVALID:
 			case UNKNOWN:
 			case NOT_UNDERSTOOD:
 				return SPECIAL_TYPES[t];
 			default:
 				return SPECIAL_TYPES[INVALID];
 			
 			// these can and should be localized
 			case NUMBER: return lang.get("NUMBER");
 			case STRING: return lang.get("STRING");
 			case DATE: return lang.get("DATE");
 			case TIME: return lang.get("TIME");
 			case YEAR: return lang.get("YEAR");
 			case MONTH: return lang.get("MONTH");
 			case DAY: return lang.get("DAY");
 			case WEEKDAY: return lang.get("WEEKDAY");
 			case HOUR: return lang.get("HOUR");
 			case MINUTE: return lang.get("MINUTE");
 			case SECOND: return lang.get("SECOND");
 			case MONTH_NUM: return lang.get("MONTH_NUM");
 			case DAY_NUM: return lang.get("DAY_NUM");
 		}		
 	}
 	
 	static public Datum parseSpecialType(Triceps lang, String s) {
 		if (s == null || s.trim().length() == 0)
 			return getInstance(lang,UNASKED);
 			
 		for (int i=0;i<SPECIAL_TYPES.length;++i) {
 			if (SPECIAL_TYPES[i].equals(s))
 				return getInstance(lang,i);
 		}
 		return null;	// not a special datumType
 	}
 	
 	static public int parseDatumType(String s) {
 		if (s == null)
 			return -1;
 			
 		for (int i=0;i<DATUM_TYPES.length;++i) {
 			if (DATUM_TYPES[i].equals(s))
 				return (i + SPECIAL_TYPES.length);
 		}
 		return -1;		
 	}
 }
