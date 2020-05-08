 package org.meteornetwork.meteor.business;
 
 public enum LoanTypeEnum {
 
 	FFELCONSL("FFELConsl"),
 	FFELCSUB("FFELCSub"),
 	FFELCUSUB("FFELCUsub"),
 	FFELCHEAL("FFELCHeal"),
 	FFELCOTHR("FFELCOthr"),
 
 	FFELPLUS("FFELPLUS"),
 	FFELSUB("FFELSub"),
 	FFELUNSUB("FFELUnSub"),
 	FFELGB("FFELGB"),
 
 	SLS("SLS"),
 	ALTLOAN("AltLoan"),
 	HEAL("HEAL"),
 
 	DLCONSL("DLConsl"),
 	DLCSUB("DLCSub"),
 	DLCUSUB("DLCUsub"),
 	DLCHEAL("DLCHeal"),
 	DLCOTHR("DLCOthr"),
 
 	DLSUB("DLSub"),
 	DLUNSUB("DLUnsub"),
 	DLPLUS("DLPLUS"),
 	DLGB("DLGB"),
 
 	FWSP("FWSP"),
 	SEOG("SEOG"),
 	PERKINS("Perkins"),
 	CWC("CWC"),
 	PELL("Pell"),
 	OTHER("Other"),
 	STATEGRNT("StateGrnt"),
 	STATESCHL("StateSchl");
 
 	private String name;
 
 	private LoanTypeEnum(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public static LoanTypeEnum getNameIgnoreCase(String name) {
 		for (LoanTypeEnum value : LoanTypeEnum.values()) {
			if (value.getName().equalsIgnoreCase(name)) {
 				return value;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Returns true if loan type is a consolidation loan
 	 * 
 	 * @param type
 	 * @return true if loan type is a consolidation loan
 	 */
 	public static boolean isConsolidation(LoanTypeEnum type) {
 		switch (type) {
 		case FFELCONSL:
 		case FFELCSUB:
 		case FFELCUSUB:
 		case FFELCHEAL:
 		case FFELCOTHR:
 		case DLCONSL:
 		case DLCSUB:
 		case DLCUSUB:
 		case DLCHEAL:
 		case DLCOTHR:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	/**
 	 * Returns true if loan type is a plus loan
 	 * 
 	 * @param type
 	 * @return true if loan type is a plus loan
 	 */
 	public static boolean isPlus(LoanTypeEnum type) {
 		switch (type) {
 		case FFELPLUS:
 		case DLPLUS:
 			return true;
 		default:
 			return false;
 		}
 	}
 }
