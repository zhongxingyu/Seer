 package info.ohgita.android.bincalc;
 
 import info.ohgita.android.bincalc.calculator.BaseConverter;
 import info.ohgita.android.bincalc.calculator.BasicArithOperator;
 import info.ohgita.android.bincalc.calculator.ExpParser;
 import info.ohgita.android.bincalc.calculator.HistoryItem;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * bin.Calc Internal calculator class
  * @author Masanori Ohgita
  */
 public class Calculator {
 	protected ExpParser expParser;	// Numerical-expression (Numerical formula) parser object
 	protected BasicArithOperator basicArithOperator;	// Basic arithmetic operator object
 	protected BaseConverter baseConverter;	// Base converter object
 	
 	protected String EXP_SYMBOLS[] = {"(",")","*","/","+","-"};
 	
 	protected CalculatorMemoryData memory;
 	
 	public ArrayList<HistoryItem> histories;
 	
 	/**
 	 * Constructor
 	 * @param arith_round_scale	Rounding scale for BasicArithOperator
 	 */
 	public Calculator(int arith_round_scale){
 		/* Initialize instances */
 		expParser = new ExpParser();
 		basicArithOperator = new BasicArithOperator(arith_round_scale);
 		baseConverter = new BaseConverter();
 		
 		histories = new ArrayList<HistoryItem>();
 		memory = null;
 		
 		/* Initialize sort for binarySearch */
 		Arrays.sort(EXP_SYMBOLS);
 		
 	}
 	
 	/**
 	 * MemoryIn method
 	 */
 	public void InMemory(int base_type, String value){
 		memory = new CalculatorMemoryData();
 		memory.base_type = base_type;
 		memory.value = value;
 	}
 	
 	/**
 	 * MemoryOut (Read) method
 	 */
 	public CalculatorMemoryData readMemory(){
 		if (memory != null)
 			return memory;
 		return null;
 	}
 	
 	/**
 	 * MemoryClear method
 	 */
 	public void clearMemory(){
 		memory = null;
 	}
 	
 	/**
 	 * Calculate a numerical formula
 	 * @param exp Numerical formula (Decimal numbers)
 	 * @return Calculated result
 	 */
 	public String calc(String exp) throws Exception {
 		Log.d("binCalc", "Calculator - calc("+exp+")");
 		
 		/* Parse a numerical formula */
 		LinkedList<String> list = parseToList(exp);
 		
 		/* Calculate formula */
 		String res = basicArithOperator.calculate(list);
 
 		Log.d("binCalc", "Calculator - calc - Answer: "+res);
 		return res;
 	}
 	
 	/**
 	 * Parse a numerical formula, and convert to a LinkedList.
 	 * @param exp Numerical formula (Decimal numbers)
 	 * @return Parsed LinkedList
 	 */
 	public LinkedList<String> parseToList(String exp){
 		Log.d("binCalc", "parseToList("+exp+")");
 		return expParser.parseToList(exp);
 	}
 	
 	/**
 	 * Base number convert from LinkedList
 	 * @param list LinkedList (Parsed numerical formula)
 	 * @param fromNBase Source base
 	 * @param destNBase Destination base
 	 * @return Converted result
 	 * @throws Exception 
 	 */
 	public CalculatorBaseConvResult listBaseConv(LinkedList<String> list, int fromNBase, int destNBase ) throws Exception{
 		Log.d("binCalc", "Calculator.listBaseConv(list, "+fromNBase+", "+destNBase+")");
 		baseConverter = new BaseConverter();
 		Iterator<String> iter = list.iterator();
 		StringBuilder resultExp = new StringBuilder();
 		
 		while(iter.hasNext()){
 			String chunk = iter.next();
 			
 			String conv = null;
 			if(Arrays.binarySearch(EXP_SYMBOLS, chunk) < 0){ // if number
 				
 				if(fromNBase == 10){
 					/* Convert DEC(10) -> NADIC( 2 or 16 ) */
 					conv = baseConverter.decToN(Double.parseDouble(chunk), destNBase);
 					
 				}else if(fromNBase == 2){
 					
 					if(destNBase == 10){
 						/* Convert BIN(2) -> DEC(10) */
 						Double d =baseConverter.binToDec(chunk);
 						if(isDecimalFraction(2, chunk) == true){
 							conv = d.toString();
 						}else{
 							conv = d.intValue() + ""; // Remove the decimal fraction point
 						}
 					}else if(destNBase == 16){
 						/* Convert BIN(2) -> DEC(16) */
 						conv = baseConverter.decToN( baseConverter.binToDec(chunk), 16);
 					}
 					
 				}else if(fromNBase == 16){
 					
 					if(destNBase == 2){
 						/* Convert HEX(16) -> BIN(2) */
 						conv = baseConverter.decToN( baseConverter.hexToDec(chunk), 2);
 					}else if(destNBase == 10){
 						/* Convert HEX(16) -> DEC(10) */
 						Double d = baseConverter.hexToDec(chunk);
 						if(isDecimalFraction(16, chunk) == true){
 							conv = d.toString();
 						}else{
 							conv = d.intValue() + ""; // Remove the decimal fraction point
 						}
 					}
 				}
 				
 				/* Insert the separator into number */
 				conv = insertSeparator(conv, destNBase);
 				/* Insert space for a point */
 				//conv = insertSpaceForPoint(conv);
 				
 			} else {
 				conv = chunk;
 			}
 			
 			resultExp.append(conv);
 		}
 		
 		CalculatorBaseConvResult result = new CalculatorBaseConvResult(); 
 		result.logs = baseConverter.getLogs();
 		result.value = resultExp.toString();
 		return result;
 	}
 	
 	/**
 	 * Convert LinkedList to String (Numerical formula) 
 	 * @param list LinkedList (Parsed numerical formula)
 	 * @param nBase Source base
 	 * @return Numerical formula string
 	 * @throws Exception 
 	 */
 	public String listToString(LinkedList<String> list, int nBase) throws Exception{
 		Log.d("binCalc", "Calculator.listToString(list, "+nBase+")");
 		Iterator<String> iter = list.iterator();
 		StringBuilder resultExp = new StringBuilder();
 		
 		while(iter.hasNext()){
 			String chunk = iter.next();
 			Log.i("binCalc", chunk);
 			if (Arrays.binarySearch(EXP_SYMBOLS, chunk) < 0){ // If number
 				if (nBase == 10) { // Decimal
 					if (chunk.contentEquals("-0") == false){ // Not minus zero
 						/* Insert the separator to number */
 						chunk = insertSeparator(chunk, nBase);
 					}
 				} else if (nBase == 2) { // Binary
 					/* Insert the separator to number */
 					chunk = insertSeparator(chunk, nBase);
 					/* Insert space for a point */
 					//chunk = insertSpaceForPoint(chunk);
 				}
 			}
 			
 			resultExp.append(chunk);
 		}
 		Log.d("binCalc", "  => "+resultExp.toString());
 		return resultExp.toString();
 	}
 	
 	/**
 	 * Remove parenthesis for LinkedList
 	 * @param list LinkedList (Parsed numerical formula)
 	 * @return Processed LinkedList
 	 */
 	public LinkedList<String> listRemoveParentheses(LinkedList<String> list){
 		Log.d("binCalc", "Calculator.removeParentheses(list)");
 		LinkedList<String> ret_list = new LinkedList<String>(); 
 		for(int i=0;i<list.size();i++){
 			String chunk = list.get(i);
 			if(chunk.contentEquals("(") == false && chunk.contentEquals(")") == false){
 				// if NOT parenthesis...
 				ret_list.add(chunk);
 			}
 		}
 		return ret_list;
 	}
 	
 	/**
 	 * Zero-padding for LinkedList
 	 * @param list LinkedList (Parsed numerical formula)
 	 * @param nBase Source base
 	 * @return Processed LinkedList
 	 */
 	public LinkedList<String> listZeropadding(LinkedList<String> list, int nBase){
 		Log.d("binCalc", "Calculator.listZeropadding(list, "+nBase+")");
 		for(int i=0;i<list.size();i++){
 			String chunk = list.get(i);
 			if(Arrays.binarySearch(EXP_SYMBOLS, chunk) < 0){// if number
 				if(nBase == 2){ // binary
 					list.set(i, baseConverter.binZeroPadding(chunk));
 				}
 			}
 		}
 		return list;
 	}
 	
 	/**
 	 * Find Last number-chunk from the list
 	 * @param list LinkedList (Parsed numerical formula)
 	 * @return Found chunk (Last number-chunk)
 	 */
 	public int indexOfListLastNumberChunk(LinkedList<String> list) {
 		if (list.isEmpty() == false) {
 			// Find last number-chunk
 			for (int i = list.size() - 1; 0 <= i; i--) {
 				String chunk = list.get(i);
 				if (0 < chunk.length() && chunk.contentEquals("(") == false
 						&& chunk.contentEquals(")") == false) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 	
 	/**
 	 * Add a history of calculator
 	 * @param history item
 	 */
 	public int putHistory(HistoryItem history) {
 		Log.d("binCalc","historyAdd(history)");
 		/* Push a history */
 		histories.add(history);
 		return histories.size() - 1;
 	}
 	
 	/**
 	 * Add (insert and overwrite) a history of calculator
 	 * @param id	Insert destination Id (Later items will be removed.)
 	 * @param history item
 	 */
 	public int putHistory(int id, HistoryItem history) {
 		Log.d("binCalc","historyAdd(history)");
 		/* Remove histories of later than it */
 		while (id < histories.size()) {
 			histories.remove(histories.size() - 1);
 		}
 		/* Push a history */
 		histories.add(history);
 		return histories.size() - 1;
 	}
 	
 	/**
 	 * Get a history of calculator
 	 * @param ID of a history
 	 * @return history
 	 */
 	public HistoryItem getHistory(int id) {
 		if (id < histories.size()) {
 			return histories.get(id);
 		}
 		return null;
 	}
 	
 	public int getHistoryNums(){
 		return histories.size();
 	}
 	
 	public boolean isDecimalFraction(int nBase, String dec){
 		if(dec.charAt(dec.length() -1) == '.'){ // If last char is point... (ex: "1.")
 			return true;
 		}
 		
 		if(nBase == 16){ // HEX
 			if (dec.indexOf('.') == -1)
 				return false;
 		
 		} else{ // BIN or DEC
 			Double d = Double.parseDouble(dec);
 			if(d % 1.0 == 0.0)
 				return false; // Integer number
 		}
 		return true; // Fraction number
 	}
 	
 	/**
 	 * Insert the digit-separator into number
 	 * @param number Source number string
 	 * @param NBase
 	 * @return Result number string
 	 */
 	protected String insertSeparator(String number, int nBase) throws Exception {
 		Log.d("binCalc", "Calculator.insertSeparator("+number+")");
 		if (nBase == 2) { // Binary
 			String r = "";
 			int count = 0;
 			for (int i = 0; i < number.length(); i++) {
 				char c = number.charAt(i);
 				if (c != '.') {
 					if (count != 0 && count % 4 == 0) {
 						r += " ";
 					}
 					count++;
 				}
 				r += c;
 			}
 			return r;
 		} else if (nBase == 10) { // Decimal
 			DecimalFormat formatter = new DecimalFormat("#,###");
 			
 			if (number.indexOf(".") != -1) {
 				int i = number.indexOf(".");
 				String n = number.substring(0, i);
 				number = formatter.format(Integer.parseInt(n)) + number.substring(i, number.length());
 			} else {
 				number = formatter.format(Integer.parseInt(number));
 			}
 		}
 		return number;
 	}
 	
 	/**
 	 * Insert space into surround a point
 	 * @param number
 	 * @return Processed number string
 	 */
 	protected String insertSpaceForPoint(String number) {
 		if (number == null || number.indexOf(".") == -1) {
 			return number;
 		}
 		
 		String r = "";
 		for (int i = 0, l = number.length(); i < l; i++) {
 			char c = number.charAt(i);
 			if (c == '.') {
 				r += "　.　";
 			} else {
 				r += number.charAt(i);
 			}
 		}
 		return r;
 	}
 }
