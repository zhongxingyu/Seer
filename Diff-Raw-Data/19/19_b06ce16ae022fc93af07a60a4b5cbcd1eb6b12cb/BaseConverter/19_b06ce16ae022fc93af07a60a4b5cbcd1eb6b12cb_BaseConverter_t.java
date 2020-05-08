 package info.ohgita.android.bincalc.calculator;
 
 import java.util.ArrayList;
 //import android.util.Log;
 
 /**
  * Base number converter class
  * 
  * Base number convert from LinkedList (Parsed decimal numerical formula).
  * 
  * @author Masanori Ohgita
  * 
  */
 public class BaseConverter {
 	protected ArrayList<String> logs;
 	
 	/**
 	 * Constructor
 	 */
 	public BaseConverter() {
 		logs = new ArrayList<String>();
 	}
 	
 	/**
 	 * Decimal number to N-adic number
 	 * @param num	Source decimal number
 	 * @param n_adic N-adic number
 	 * @return converted number
 	 * @throws Exception 
 	 */
 	public String decToN(double num, int n_adic) throws Exception{
 		//Log.d("binCalc", "BaseConverter.decToN("+num+", "+n_adic+")");
 		log("-- 10 to " + n_adic + " --");
 		
 		String ret_int = ""; // converted n-Adic number integer-part
 		String ret_dec = "";// converted n-Adic number decimal-places-part (binary/hexadecimal fraction part)
 		
 		boolean is_negative = false; // Negative number flag
 		if (num < 0.0) {
 			is_negative = true;
 			
 			/* Nagative number process for Hex */
 			if (n_adic == 16) {
 				String n1 = decToN(num, 2);
 				Double n2 = binToDec(n1);
 				return decToN(n2, 16);
 			} else if (n_adic == 2) {
 				num *= -1;
 			}
 		}
 		
 		/* Separate fraction part */
 		int num_int = (int) num; // base-10 number integer-part
 		double num_dec = num % 1.0; // base-10 number decimal-places-part
 		
 		/* Integer overflow check */
 		if (num_int >= Integer.MAX_VALUE) {
 			throw new Exception();
 		}
 		
 		/* Process for integer part */
 		Integer value;
 		do {
 			value =  num_int % n_adic;
			if (n_adic == 16 && 10 <= value) { // if hex A-F...
 				log(num_int + " / " + n_adic + " ... " + value + " (" + (Character.toString((char) ('A'+ (value-10)))) + ")");
 				ret_int = (Character.toString((char) ('A'+ (value-10)))) + ret_int;
 			} else { // other number...
 				log(num_int + " / " + n_adic + " ... " + value.toString());
 				ret_int = value.toString() + ret_int;
 			}
 			num_int /= n_adic;
 		} while (num_int >= n_adic-1);
 		
 		if (num_int != 0) {
 			log("... "+num_int);
			if (n_adic == 16 && 10 <= num_int) { // If hex A-F ...
				ret_int = (Character.toString((char) ('A'+ (num_int-10)))) + ret_int;
 			} else {
				ret_int = num_int + "" + ret_int;
 			}
 		}
 		
 		/* Process for decimal places part */
 		if (num_dec != 0.0) {
 			ret_dec += ".";
 		}
 		while (num_dec != 0.0) {
 			value = (int) (num_dec * (double)n_adic);
			if (n_adic == 16 && 10 <= value) { // if hex A-F...
 				log(num_dec + " * " + n_adic + "  =  " + value + " (" + (Character.toString((char) ('A'+ (value-10)))) + ")");
 				ret_dec += (Character.toString((char) ('A'+ (value-10))));
 			} else { // other number...
 				log(num_dec + " * " + n_adic + " = " + value.toString());
 				ret_dec += value.toString();
 			}
 			num_dec = (num_dec * (double)n_adic) % 1.0;
 		}
 		
 		/* Nagative number process for Binary */
 		if (is_negative && n_adic == 2) {
 			while (ret_int.length() % 8 != 0) {
 				ret_int = "0" + ret_int;
 			}
 			ret_int = reverseBits(ret_int);
 			log("* Reversed: " + ret_int);
 			ret_int = add1ToBits(ret_int);
 			log("* Added 1: " + ret_int);
 		}
 		
 		/* Zero padding */
 		if (n_adic == 2) {
 			ret_int = binZeroPadding(ret_int);
 		}
 		
 		/* Make return value */
 		String ret = ret_int;
 		if (ret_dec.length() != 0) {
 			ret += ret_dec;
 		}
 
 		log("=> "+ret+"("+n_adic+")");
 		return ret.toString();
 	}
 	
 	/**
 	 * Binary to decimal number
 	 * @param bin	Source binary number (String)
 	 * @return converted number
 	 */
 	public Double binToDec(String bin){
 		//Log.d("binCalc", "BaseConverter.binToDec("+bin+")");
 		log("-- 2 to 10 --");
 		
 		double ret = 0; // converted base-10 number
 		
 		/* Separate fraction part */
 		String num_bin = bin; // binary part
 		String num_dec = ""; // binary fraction part
 		if(bin.indexOf('.') != -1){
 			num_bin = bin.substring(0, bin.indexOf('.'));
 			num_dec = bin.substring(bin.indexOf('.') + 1);
 			//Log.d("binCalc", "  num_bin = " + num_bin + ", num_dec = " + num_dec);
 		}
 		
 		/* Process for binary part */
 		int digit = 0;
 		for (int i=num_bin.length()-1;0<=i;i--) {
 			int d = Integer.parseInt(""+num_bin.charAt(i));
 			double value = d* Math.pow(2, digit);
 			log(d+"*2^"+digit + " = "+value);
 			ret = (int)value + ret;
 			digit++;
 		}
 		
 		/* Process for binary fraction part */
 		digit = 1;
 		for (int i=0;i<num_dec.length();i++) {
 			int d = Integer.parseInt(""+num_dec.charAt(i));
 			double value = d* Math.pow(2, -digit);
 			log(d+"*2^"+-digit + " = "+value);
 			ret += value;
 			digit++;
 		}
 		
 		log("=> "+ret+"(2)");
 		return ret;
 	}
 	
 	/**
 	 * 4bit-separate for Binary number
 	 * @param num Binary number
 	 * @return 4bit separated number
 	 */
 	public String binSeparate(String num) {
 		StringBuilder ret = new StringBuilder();
 		for(int i=0;i<num.length();i++){
 			if(i % 4 == 0){
 				ret.append(",");
 			}
 			ret.append(num.charAt(i)+"");
 		}
 		return ret.toString();
 	}
 
 	/**
 	 * Zero-padding for Binary number 
 	 * @param num Binary number
 	 * @return Zero-padding number
 	 */
 	public String binZeroPadding(String num) {
 		while (num.length() %4 != 0) {
 			num = "0" + num;
 		}
 		while (num.indexOf("0000") == 0) {
 			num = num.substring(4);
 		}
 		if (num.length() == 0) {
 			num = "0000";
 		}
 		return num;
 	}
 
 	/**
 	 * Hexadecimal to decimal number
 	 * @param hex	Source hexadecimal number (String)
 	 * @return converted number
 	 */
 	public Double hexToDec(String hex){
 		//Log.d("binCalc", "BaseConverter.hexToDec("+hex+")");
 		log("-- 16 to 10 --");
 		
 		double ret = 0.0;
 		
 		/* Separate fraction part */
 		String num_hex = hex; // hexadecimal part
 		String num_dec = ""; // hexadecimal fraction part
 		if (hex.indexOf('.') != -1) {
 			num_hex = hex.substring(0, hex.indexOf('.'));
 			num_dec = hex.substring(hex.indexOf('.') + 1);
 			//Log.d("binCalc", "  num_hex = " + num_hex + ", num_dec = " + num_dec);
 		}
 
 		/* Process for hexadecimal part */
 		int digit = 0;
 		for (int i=num_hex.length()-1;0<=i;i--) {
 			int d = hexCharToInt(num_hex.charAt(i));
 			double value = d* Math.pow(16, digit);
 			log(d+"*16^"+digit + " = "+value);
 			ret = value + ret;
 			digit++;
 		}
 		
 		/* Process for hexadecimal fraction part */
 		digit = 1;
 		for (int i=0;i<num_dec.length();i++) {
 			int d = hexCharToInt(num_dec.charAt(i));
 			double value = d* Math.pow(16, -digit);
 			log(d+"*16^"+-digit + " = "+value);
 			ret += value;
 			digit++;
 		}
 
 		log("=> "+ret+"(10)");
 		return ret;
 	}
 	
 	/**
 	 * Get logs
 	 * @return logs
 	 */
 	public ArrayList<String> getLogs(){
 		return logs;
 	}
 	
 	/**
 	 * Reverse Bits
 	 * @param bin_num Binary number
 	 * @return Reversed number
 	 */
 	protected String reverseBits(String bin_num) {
 		String r = "";
 		for (int i = 0, l = bin_num.length(); i < l; i++) {
 			if (bin_num.charAt(i) == '1') {
 				r += "0";
 			} else {
 				r += "1";
 			}
 		}
 		return r;
 	}
 	
 	/**
 	 * Add 1 to Bits
 	 * @param bin_num Binary number
 	 * @return Processed number
 	 */
 	protected String add1ToBits(String bin_num) {
 		String r = "";
 		boolean end_flag = false;
 		for (int i = bin_num.length() - 1; 0 <= i; i--) {
 			if (end_flag == false) {
 				if (bin_num.charAt(i) == '1') {
 					r = "0" + r;
 				} else {
 					r = "1" + r;
 					end_flag = true;
 				}
 			} else {
 				r = bin_num.charAt(i) + r;
 			}
 		}
 		return r;
 	}
 	
 	/**
 	 * Convert Hex-character to integer
 	 * @param hex-character
 	 * @return Integer number
 	 */
 	protected int hexCharToInt(char hex_char){
 		int d = 0;
 		switch(hex_char){
 			case 'A':
 				d = 10;
 				break;
 			case 'B':
 				d = 11;
 				break;
 			case 'C':
 				d = 12;
 				break;
 			case 'D':
 				d = 13;
 				break;
 			case 'E':
 				d = 14;
 				break;
 			case 'F':
 				d = 15;
 				break;
 			default:
 				d = Integer.parseInt("" + hex_char);
 		};
 		return d;
 	}
 	
 	protected void log(String log){
 		logs.add(log);
 	}
 	
 	protected void logClear(){
 		logs = new ArrayList<String>();
 		logs.clear();
 	}
 }
