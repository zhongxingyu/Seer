 package util;
 
 public class AsmContext {
 	
 	private final static int[] tabs = {4, 12, 28};
 	private int numVars;
 	private int numTemps;
 	private int numArgs;
 
 	
 	private final static int varSize = 4;
 	private final static int implicitVars = 2; // retaddress doesn't count
 	private final static int retOffset = varSize*1;
 	private final static int dynLinkOffset = varSize*0;
 	private final static int statLinkOffset = varSize*-1;
 
 	public TabbedBuffer newLineBuf() {return new TabbedBuffer(tabs);};
 	public int getTempOffset(int number) {
 		return -varSize*(implicitVars+getNumVars()+number);
 	}
 
 	public int getVarOffset(int number) {
 		return -varSize*implicitVars;
 	}
 
 	public int getNumVars() {
 		return numVars;
 	}
 
 	public void setNumVars(int numVars) {
 		this.numVars = numVars;
 	}
 	
 	public int getNumTemps() {
 		return numTemps;
 	}
 	
 	public void setNumTemps(int numTemps) {
 		this.numTemps = numTemps;
 	}
 	
 	public int getRecordSize() {
 		return varSize*(implicitVars+numVars+numTemps);
 	}
 	
 	public int getNumArgs() {
 		return numArgs;
 	}
 	
 	public void setNumArgs(int numArgs) {
 		this.numArgs = numArgs;
 	}
 	
 	public int getParOffset(int number) {
 		return -varSize*(-2-number);
 	}
 	
 	public int getArgOffset(int number) {
		return varSize*(number);
 	}
 	
 	public int getStatLinkOffset() {
 		return statLinkOffset;
 	}
 	public static int getRetOffset() {
 		return retOffset;
 	}
 	public static int getDynLinkOffset() {
 		return dynLinkOffset;
 	}
 }
