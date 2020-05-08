 package ru.stanislavburov.android.PGMCalc;
 
 import java.io.Serializable;
 import java.util.LinkedList;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.widget.Toast;
 
 public class CalcEngine implements Serializable {
 	private static final long serialVersionUID = 1L;
 	private static final double PI = 3.14159265358979323846264338328;
 	public transient Operations operations = new Operations(this);
 	private StringBuilder sMantissa, sPowerOf10;
 	private double dValue;
 	private int maxLength, fix = -1;
 	private boolean bPowerOf10, bError;
 	protected boolean bClearStringsFlag;
 	private static final String MANTISSA_EXPONENT_SEPARATOR = " ", sERROR = "error", ZERO = "0.", ONE="1.";
 	private LinkedList<FlowEntry> flow = new LinkedList<FlowEntry>();
 	private LinkedList<LinkedList<FlowEntry>> flowStack = new LinkedList<LinkedList<FlowEntry>>();
 	private FlowEntry lastAction, voidAction;
 	private String separator = "E"; // will be defined later
 	protected transient Context parent = null;
 	private AngleUnit radian = new AngleUnit() {
 		private static final long serialVersionUID = 1L;
 			@Override public double toRadian(double v) { return v; }
 			@Override public double fromRadian(double v) { return v; }
 			@Override public String toString() { return "rad"; }
 		}, gradian = new AngleUnit() {
 			private static final long serialVersionUID = 1L;
 			@Override public double toRadian(double v) { return v*PI/200.; }
 			@Override public double fromRadian(double v) { return v/PI*200.; }
 			@Override public String toString() { return "grad"; }
 		}, degree = new AngleUnit() {
 			private static final long serialVersionUID = 1L;
 			@Override public double toRadian(double v) { return v/180.*PI; }
 			@Override public double fromRadian(double v) { return v/PI*180.; }
 			@Override public String toString() { return "deg"; }
 		}, curAngleUnit;
 	private LinkedList<AngleUnit> angleUnits = new LinkedList<AngleUnit>();
 	public static final int MEM_MAX = 10;
 	private Operations.MemoryUnit[] memories = new Operations.MemoryUnit[MEM_MAX];
 	private ExponentType exponentType = ExponentType.REGULAR;
 	private String errorDescription = "";
 	
 	public CalcEngine() {
 		init();
 	}
 	
 	public CalcEngine(Context parent) {
 		init();
 		this.parent = parent;
 	}
 	
 	private void init() {
 		voidAction = new FlowEntry(0, operations.doNothing);
 		sMantissa=new StringBuilder();
 		sPowerOf10=new StringBuilder();
 		bPowerOf10=false;
 		maxLength=13; // TODO may be dynamic
 		defineSeparator();
 		dValue=0.;
 		bError=false;
 		angleUnits.add(radian); angleUnits.add(degree); angleUnits.add(gradian);
 		setRadian();
 		clear();
 		for(int i=0; i<MEM_MAX; i++) { memories[i] = operations.new MemoryUnit(Integer.toString(i)); }
 		lastAction = voidAction;
 	}
 	
 	private void defineSeparator() {
 		String bigNumber = Double.toString(1e99);
 		separator = Character.toString(bigNumber.charAt(bigNumber.length()-3));
 	}
 	
 	public String input(char c) {
 		if(bClearStringsFlag) clearStrings();
 		if(!bPowerOf10) {
 			if(sMantissa.toString().replace("-", "").replace(".", "").length()<maxLength) sMantissa.append(c);
 		} else sPowerOf10.deleteCharAt(sPowerOf10.length()-3).append(c);
 		dValue = toDouble();
 		return toString();
 	}
 	
 	public String addDot() {
 		if(bClearStringsFlag) clearStrings();
 		if(!bPowerOf10 && sMantissa.indexOf(".")==-1) {
 			if(sMantissa.toString().replace("-", "").length()==0) sMantissa.append('0');
 			sMantissa.append('.');
 		}
 		dValue = toDouble();
 		return toString();
 	}
 	
 	public String changeSign() {
 		StringBuilder tmp;
 		if(!bClearStringsFlag && bPowerOf10) tmp=sPowerOf10;
 		else tmp=sMantissa;
 		try {
 			if(tmp.charAt(0)=='-') tmp.deleteCharAt(0);
 			else tmp.insert(0, "-");
 		} catch(IndexOutOfBoundsException ex) { }
 		dValue = toDouble();
 		return toString();
 	}
 	
 	public String backspace() {
 		if(bClearStringsFlag) clearStrings();
 		if(!bPowerOf10) {
 			try {
 				sMantissa.deleteCharAt(sMantissa.length()-1);
 				if(sMantissa.toString().equals("-")) sMantissa.setLength(0);
 			} catch(StringIndexOutOfBoundsException ex) { }
 		} else {
 			if(sPowerOf10.substring(sPowerOf10.length()-3, sPowerOf10.length()).equals("000")) {
 				bPowerOf10=false;
 				sPowerOf10.setLength(0);
 			} else sPowerOf10.insert(sPowerOf10.length()-3, '0').deleteCharAt(sPowerOf10.length()-1);
 		}
 		dValue = toDouble();
 		return toString();
 	}
 	
 	public String setPowerOf10() {
 		if(bClearStringsFlag) clearStrings();
 		if(!bPowerOf10) {
 			if(sMantissa.length()==0) sMantissa.append(ONE);
 			sPowerOf10.insert(0, "000");
 			bPowerOf10=true;
 		}
 		dValue = toDouble();
 		return toString();
 	}
 
 	public String clear() {
 		clearStrings();
 		bError=false;
 		bClearStringsFlag=false;
 		dValue=0.;
 		flow.clear();
 		flowStack.clear();
 		lastAction = voidAction;
 		errorDescription = null;
 		return toString();
 	}
 	
 	private void clearStrings() {
 		sMantissa.setLength(0);
 		sPowerOf10.setLength(0);
 		bPowerOf10=false;
 		bClearStringsFlag=false;
 	}
 	@Override
 	public String toString() {
 		if(operations==null) operations = new Operations(this);
 		if(!bError) {
 			StringBuilder resultMantissa = new StringBuilder(), resultPowerOf10 = new StringBuilder();
 			if(!bClearStringsFlag || exponentType==ExponentType.REGULAR) {
 				resultMantissa.append(sMantissa);
 				if(sMantissa.length()==0) resultMantissa.append(ZERO);
 				if(bPowerOf10) resultPowerOf10.append(sPowerOf10);
 			} else {  // SCI or ENG
 				StringBuilder tmpsPowerOf10 = new StringBuilder();
 				StringBuilder tmpsMantissa = new StringBuilder();
 				if(!bPowerOf10) {
 					if(dValue!=0.) {
 						double logOfdValue = Math.log10(Math.abs(dValue));
 						double newPowerOf10 = Math.floor(logOfdValue);
 						double newMantissa = dValue/Math.pow(10, newPowerOf10);
 						tmpsMantissa.append(double2String(newMantissa));
 						tmpsPowerOf10.append(int2PowerOf10((int)newPowerOf10));
 					} else {
 						tmpsMantissa.append(double2String(dValue));
 						tmpsPowerOf10.append(int2PowerOf10(0));
 					}
 				} else {
 					tmpsMantissa.append(sMantissa);
 					tmpsPowerOf10.append(sPowerOf10);
 				}  
 				resultMantissa.append(tmpsMantissa);
 				if(tmpsMantissa.length()==0) resultMantissa.append(ZERO);  //  It looks like this condition cann't be true
 				if(exponentType==ExponentType.ENG) {
 					int nPowerOf10;
 					try {
 						nPowerOf10 = Integer.parseInt(tmpsPowerOf10.toString());
 					} catch(NumberFormatException ex) { setError(ex.getMessage()); return sERROR; }
 					int deviation = (nPowerOf10+1110) % 3;  //  + 1110 is to treat negative exponent like positive one
 					int position;
 					for(int i=deviation; i>0; i--) {
 						position = resultMantissa.indexOf(".");
 						resultMantissa.deleteCharAt(position);
 						if(position+1>resultMantissa.length()) resultMantissa.append('0');
 						resultMantissa.insert(position+1, '.');
 					}
 					resultPowerOf10.append(int2PowerOf10(nPowerOf10-deviation));
 				}else { //SCI
 					resultPowerOf10.append(tmpsPowerOf10);
 				}
 			}
 			if(bClearStringsFlag && fix>=0) {
 				int dotPosition = resultMantissa.indexOf(".");
 				if(dotPosition==-1) { dotPosition=resultMantissa.length(); resultMantissa.append('.'); }
 				int numberOfDecimalPlaces = resultMantissa.length()-1-dotPosition;
 				if(numberOfDecimalPlaces>fix) {
 					int numberOfDigits = resultMantissa.toString().replace("-", "").replace(".", "").length();
 					cutLongMantissa(resultMantissa, numberOfDigits-numberOfDecimalPlaces + fix, false);
 				}
 				if(numberOfDecimalPlaces<fix) {
 					for(int numberOfAdditionalZeros = fix - numberOfDecimalPlaces; numberOfAdditionalZeros>0; numberOfAdditionalZeros--) {
 						if(resultMantissa.toString().replace("-", "").replace(".", "").length()<maxLength) resultMantissa.append('0');
 						else break;
 					}
 					
 				}
 			}
 			if(resultPowerOf10.length()==0) return  resultMantissa.toString();
 			return resultMantissa.toString() + MANTISSA_EXPONENT_SEPARATOR + resultPowerOf10.toString();
 		} else return sERROR;
 	}
 	
 	private String double2String(double v) {
 		StringBuilder result = new StringBuilder(Double.toString(v));
 		cutLongMantissa(result, maxLength, true);
 		return result.toString();
 	}
 	
 	private String int2PowerOf10(int power) {
 		int sign = Integer.signum(power), unsignedPower = power*sign;
 		StringBuilder result = new StringBuilder(Integer.toString(unsignedPower));
 		switch(result.length()) {
 		case 1: result.insert(0, '0');
 		case 2: result.insert(0, '0');
 		}
 		if(sign<0) result.insert(0, '-');
 		return result.toString();
 	}
 	
 	private double toDouble() {
 		double result;
 		try {
 			if(sMantissa.length()==0) result = 0.;
 			else result = Double.valueOf(sMantissa.toString());
 			if(bPowerOf10) result*=Math.pow(10, Double.valueOf(sPowerOf10.toString()));
 		} catch(NumberFormatException ex) {
 			setError("Wrong value");
 			result = dValue;
 		}
 		return result;
 	}
 	
 	private void setError() { dValue=Double.NaN; bError=true; }
 	private void setError(String description) { errorDescription = description; setError(); }
 	public String getErrorDescription() {
 		String result = null;  //  Return null when there is no error.
 		if(bError) result = errorDescription;
 		return result;
 	}
 	
 	public String addOperation(Operations.BinaryOperation oper) {
 		if(bClearStringsFlag && flow.size()!=0) {
 			FlowEntry curFlowEntry = flow.removeLast();
 			curFlowEntry.setOperation(oper);
 			if(flow.size()!=0 && flow.getLast().getOperation().getPrecedence()>=oper.getPrecedence()) {
 				flushFlow(); 
 			}
 			curFlowEntry.setValue(dValue);
 			flow.add(curFlowEntry);
 		} else {
			if(flow.size()!=0 && flow.getLast().getOperation().getPrecedence()>oper.getPrecedence()) flushFlow(); // WHY here was >= condition???
 			flow.add(new FlowEntry(dValue, oper));
 		}
 		bClearStringsFlag=true;
 		return toString();
 	}
 	
 	public String addOperation(Operations.UnaryOperation oper) {
 		double dValueCopy=dValue;
 		if(flow.size()!=0 && flow.getLast().getOperation()==operations.doNothing) flow.removeLast();
 		try {
 			dValue = oper.proceed(dValue);
 		} catch(Operations.Exception ex) { setError(ex.getMessage()); return toString(); }
 		value2Strings();
 		bClearStringsFlag=true;
 		flow.add(new FlowEntry(dValueCopy, operations.doNothing));
 		return toString();
 	}
 	
 	public String proceed() {
 		if(flow.size()!=0 || flowStack.size()!=0) {
 			lastAction = flushFlow();
 			while(flowStack.size()!=0) {
 				flow = flowStack.removeLast();
 				lastAction = flushFlow();
 			}
 		} else {  // perform lastAction
 			try {
 				dValue = lastAction.getOperation().proceed(dValue, lastAction.getValue());
 			} catch(Operations.Exception ex) { setError(ex.getMessage()); return toString(); }
 		}
 		value2Strings();
 		bClearStringsFlag=true;
 		return toString();
 	}
 	
 	private void cutLongMantissa(StringBuilder mantissa, int maxNumberOfDigits, boolean cutTrailingZeros) {
 		if(maxNumberOfDigits>maxLength) maxNumberOfDigits = maxLength;
 		int numberOfDigits = mantissa.toString().replace("-", "").replace(".", "").length();
 		if(numberOfDigits>maxNumberOfDigits) {
 			char afterLastChar = mantissa.charAt(mantissa.length() - (numberOfDigits-maxNumberOfDigits));
 			mantissa.setLength(mantissa.length() - (numberOfDigits-maxNumberOfDigits));
 			if(afterLastChar>'4') {
 				int index = mantissa.length()-1;
 				while(index>=0) {
 					char curChar = mantissa.charAt(index);
 					if(curChar<'0' || curChar>'9') {index--; continue;}
 					if(curChar!='9') {
 						curChar++;
 						mantissa.setCharAt(index, curChar);
 						break;
 					} else {
 						curChar = '0';
 						mantissa.setCharAt(index, curChar);
 						index--;
 					}
 				}
 				if(index<0) {
 					int insertionIndex = 0;
 					if(mantissa.charAt(insertionIndex)=='-') insertionIndex++; 
 					mantissa.insert(insertionIndex, '1');
 				}
 			}
 		}
 		if(cutTrailingZeros && mantissa.indexOf(".")>=0) {
 			while(mantissa.charAt(mantissa.length()-1)=='0') mantissa.deleteCharAt(mantissa.length()-1);
 		}
 	}
 	
 	private void value2Strings() {
 		if(Double.isInfinite(dValue) || Double.isNaN(dValue)) {
 			if(!bError) {
 				if(Double.isInfinite(dValue)) setError("Result is infinite");
 				else setError("Result is not a number");
 			}
 		} else {
 			String[] roughStrings = Double.toString(dValue).split(separator);
 			clearStrings();
 			sMantissa.append(roughStrings[0]);
 			cutLongMantissa(sMantissa, maxLength, true);
 			if(roughStrings.length>1) {
 				bPowerOf10=true;
 				sPowerOf10.append(roughStrings[1]);
 				int demandLength = 3;
 				int insertPosition = 0;
 				if(sPowerOf10.charAt(0)=='-') {
 					demandLength++; insertPosition++; 
 				}
 				while(sPowerOf10.length()<demandLength) sPowerOf10.insert(insertPosition, '0');
 			}
 		}
 	}
 	
 	private FlowEntry flushFlow() {
 		FlowEntry flowEntry=voidAction;
 		double tmp=0;
 		while(flow.size()>0) {
 			flowEntry = flow.removeLast();
 			tmp=dValue;
 			try {
 				dValue = flowEntry.getOperation().proceed(flowEntry.getValue(), dValue);
 			} catch(Operations.Exception ex) { setError(ex.getMessage()); return voidAction; }
 		}
 		flowEntry.setValue(tmp);
 		value2Strings();
 		bClearStringsFlag = true;
 		return flowEntry;
 	}
 
 	public String getAngleName() { return curAngleUnit.toString(); }
 	public String setDegree() { curAngleUnit = degree; angleUnits.remove(degree); angleUnits.add(curAngleUnit); return toString(); }
 	public String setRadian() { curAngleUnit = radian; angleUnits.remove(radian); angleUnits.add(curAngleUnit); return toString(); }
 	public String setGradian() { curAngleUnit=gradian; angleUnits.remove(gradian); angleUnits.add(curAngleUnit); return toString(); }
 	public String changeAngleUnit() { curAngleUnit=angleUnits.removeFirst(); angleUnits.add(curAngleUnit); return toString(); }
 
 	public double toRadian(double angle) { return curAngleUnit.toRadian(angle); }
 	public double fromRadian(double angle) { return curAngleUnit.fromRadian(angle); }
 	
 	public String parentheseOpen() {
 		if(flow.size()!=0 && flow.getLast().getOperation()==operations.doNothing) flow.add(new FlowEntry(dValue, operations.multiply)); 
 		if(!bClearStringsFlag && flow.size()!=0) flow.add(new FlowEntry(dValue, operations.multiply));
 		flowStack.add(flow);
 		flow = new LinkedList<FlowEntry>();
 		dValue=0;
 		value2Strings();
 		bClearStringsFlag = true;
 		StringBuilder result = new StringBuilder();
 		for(int i=0; i<flowStack.size(); i++) result.append('(');
 		return result.toString();
 	}
 	
 	public String parentheseClose() {
 		if(flowStack.size()!=0) {
 			flushFlow();
 			flow = flowStack.removeLast();
 			value2Strings();
 			bClearStringsFlag=true;
 		}
 		flow.add(new FlowEntry(dValue, operations.doNothing));
 		return toString();
 	}
 	public int numberOfOpenedParentheses() { return flowStack.size(); }
 	
 	public String swapOperands() { // TODO account for operations.doNothing!!!
 		if(flow.size()!=0) {
 			double tmp = dValue;
 			FlowEntry lastEntry = flow.getLast();
 			while(lastEntry.getOperation()==operations.doNothing) {
 				flow.removeLast();
 				lastEntry = flow.getLast();
 			}
 			dValue = lastEntry.getValue();
 			value2Strings();
 			bClearStringsFlag = true;
 			lastEntry.setValue(tmp);
 		}
 		return toString();
 	}
 	
 	public boolean[] memOccupation() {
 		boolean[] result = new boolean[MEM_MAX];
 		for(int i=0; i<MEM_MAX; i++) if(memories[i].getValue()==0) result[i]=false; else result[i]=true;
 		return result;
 	}
 	
 	public String STOM() {
 		try {
 			memories[0].setValue(dValue); 
 		} catch(Operations.Exception ex) { setError(ex.getMessage()); }
 		bClearStringsFlag = true;
 		return toString();
 	}
 	
 	public String STO(int index) {
 		if(index<MEM_MAX) {
 			try {
 				memories[index].setValue(dValue); 
 			} catch(Operations.Exception ex) { setError(ex.getMessage()); }
 			bClearStringsFlag = true;
 		} else setError("Memory #" + Integer.toString(index) + " do not exist");
 		return toString();
 	}
 	
 	public String RCLM() {
 		this.addOperation(memories[0]);
 		return toString();
 	}
 	
 	public String RCL(int index) {
 		if(index<MEM_MAX) this.addOperation(memories[index]);
 		else setError("Memory #" + Integer.toString(index) + " do not exist");
 		return toString();
 	}
 	
 	public String mPlus() { 
 		try {
 			memories[0].setValue(memories[0].getValue()+dValue);
 		} catch(Operations.Exception ex) { setError(ex.getMessage()); }
 		bClearStringsFlag = true;
 		return toString();
 	}
 	
 	public Double[] getAllMemories() {
 		Double[] result = new Double[MEM_MAX];
 		for(int i=0; i<MEM_MAX; i++) result[i]=memories[i].getValue();
 		return result;
 	}
 	
 	public String setFIX(int fix) {
 		this.fix=fix;
 		return toString();
 	}
 	public int getFIX() { return fix; }
 	
 	public String changeSCIENG() {
 		int newPosition = exponentType.ordinal()+1;
 		if(newPosition==ExponentType.values().length) newPosition=0;
 		exponentType = ExponentType.values()[newPosition];
 		return toString();
 	}
 	public ExponentType getExponentType() { return exponentType; }
 	
 	public int getFIXMax() { return maxLength+1; }
 	
 	public String percent() {
 		addOperation(operations.percentFirstPhase);
 		if(flow.size()>1) {
 			FlowEntry prevEntry = flow.get(flow.size()-2);
 			if(prevEntry.getOperation()==operations.plus || prevEntry.getOperation()==operations.minus) {
 				operations.percentSecondPhase.setValue(prevEntry.getValue());
 				addOperation(operations.percentSecondPhase);
 			}
 		}
 		return toString();
 	}
 	
 	Context getContext() { return parent; }
 	public void setContext(Context c) { parent = c; }
 	enum ExponentType implements Serializable {
 		REGULAR { @Override public String toString() { return ""; } },
 		SCI { @Override public String toString() { return "SCI"; } },
 		ENG { @Override public String toString() { return "ENG"; } };
 		@Override public abstract String toString();
 	}
 }
