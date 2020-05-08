 /*
  * Copyright (c) Anton Kraievoy, IASA, Kiev, Ukraine, 2006.
  * This work is based on code of Dr. Dalton R. Hunkins, CS dept. of St. Bonaventure University, 2006.
  */
 package elw.dp.mips.asm;
 
 import java.util.regex.Pattern;
 
 public class Data {
     private static final String DIGITS = "0123456789";
     private static final String HEX_DIGITS = "0123456789ABCDEF";
 
     private static final Pattern PATTERN_BIN = Pattern.compile("^[0-1]+$");
     private static final Pattern PATTERN_OCT = Pattern.compile("^[0-7]+$");
     private static final Pattern PATTERN_HEX = Pattern.compile("^[0-9a-fA-F]+$");
     private static final Pattern PATTERN_DEC = Pattern.compile("^[0-9]+$");
 
     private static boolean isHexDigit(char c) {
         return '0' <= c && c <= '9' || 'A' <= c && c <= 'F' || 'a' <= c && c <= 'f';
     }
 
     public static boolean isLetter(char c) {
         return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z';
     }
 
     public static boolean isDecDigit(char c) {
         return '0' <= c && c <= '9';
     }
 
     public static long int2long(int value) {
         if (value >= 0) {
             return value;
         } else {
             return -2 * (long) Integer.MIN_VALUE + value;
         }
     }
 
     public static boolean isHexPositive(String token) {
         for (int i = 0; i < token.length(); i++) {
             if (!isHexDigit(token.charAt(i))) return false;
         }
         return true;
     }
 
     public static boolean isDecPositive(String token) {
         for (int i = 0; i < token.length(); i++) {
             if (!isDecDigit(token.charAt(i))) {
                 return false;
             }
         }
         return true;
     }
 
     public static boolean isHexPositive(String token, int numberOfDigits) {
         token = token.toUpperCase();
 
         if (token.length() == 0) return false;
 
         for (int i = 0; i < token.length(); i++) {
             if (!isHexDigit(token.charAt(i))) return false;
         }
 
         return token.length() <= numberOfDigits;
     }
 
     private static int valueOfHex(char c) {
         if ('0' <= c && c <= '9') return c - '0';
         if ('A' <= c && c <= 'F') return 10 + c - 'A';
         if ('a' <= c && c <= 'f') return 10 + c - 'a';
 
         throw new IllegalArgumentException("Illegal: '" + c + "'");
     }
 
     private static int valueOfDec(char c) {
         if ('0' <= c && c <= '9') return c - '0';
 
         throw new IllegalArgumentException("illegal: '" + c + "'");
     }
 
     public static long hex2long(String address) {
         long value = 0;
 
         for (int i = 0; i < address.length(); i++) {
             value = 16 * value + valueOfHex(address.charAt(i));
         }
 
         return value;
     }
 
     private static String invert(final String hex) {
         StringBuffer result = new StringBuffer();
 
         for (int i = 0; i < hex.length(); i++) {
             final int value = valueOfHex(hex.charAt(i));
             final int invertedValue = 15 - value;
             result.append(HEX_DIGITS.charAt(invertedValue));
         }
 
         return result.toString();
     }
 
     public static int hex2int(String hex) {
         if (hex.length() == 0) {
             return 0;
         }
 
         if (hex.length() < 8 || '0' <= hex.charAt(0) && hex.charAt(0) <= '7') {
             return (int) hex2long(hex);
         } else {
             return (int) -(hex2long(invert(hex)) + 1L);
         }
     }
 
     public static String int2hex(int value, int numberOfDigits) {
         if (value >= 0) {
             return long2hex(value, numberOfDigits);
         } else {
             return long2hex(-2 * (long) Integer.MIN_VALUE + value);
         }
     }
 
     public static int dec2int(String registerNumber) {
         int value = 0;
 
         for (int i = 0; i < registerNumber.length(); i++) {
             value = 10 * value + valueOfDec(registerNumber.charAt(i));
         }
 
         return value;
     }
 
     public static String long2hex(long word) {
         return long2hex(word, 1);
     }
 
     public static String long2hex(long number, int numberOfDigits) {
         if (number < 0) {
             System.out.println("number = " + number);
             return int2hex((int) number, numberOfDigits);
         }
 
         StringBuffer result = new StringBuffer();
         long intermediateValue = number;
         int digitCount = 0;
 
         while (intermediateValue > 0 || digitCount < numberOfDigits) {
             int digitValue = (int) (intermediateValue % 16);
             result.insert(0, HEX_DIGITS.charAt(digitValue));
             digitCount++;
             intermediateValue /= 16;
         }
 
         return result.toString();
     }
 
     public static String int2dec(int number, int numberOfDigits) {
         final StringBuffer result = new StringBuffer();
         final boolean sign;
 
         long intermediateValue;
         if (number < 0) {
             sign = true;
             intermediateValue = -(long) number;
         } else {
             sign = false;
             intermediateValue = number;
         }
 
         int digitCount = 0;
         while (intermediateValue > 0 || digitCount < numberOfDigits) {
             int digitValue = (int) (intermediateValue % 10);
             result.insert(0, DIGITS.charAt(digitValue));
             digitCount++;
             intermediateValue /= 10;
         }
 
         if (sign) {
             result.insert(0, "-");
         }
 
         return result.toString();
     }
 
     public static String int2dec(int word) {
         return int2dec(word, 1);
     }
 
     public static String str(final long val, final int radix, final int digits) {
         final StringBuffer str = new StringBuffer(Long.toString(val, radix));
 
         final int offs = str.charAt(0) == '-' ? 1 : 0;
         while (str.length() < digits + offs) {
             str.insert(offs, '0');
         }
 
         return str.toString();
     }
    
    public static String strTwosComplement(final long val, final int digits) {
        final StringBuffer str = new StringBuffer(Long.toBinaryString(val));
        final char paddingChar = val < 0 ? '1' : '0';
        while (str.length() < digits) {
            str.insert(0, paddingChar);
        }

        return str.substring(str.length() - digits);
    }
 
     public static long comp(long val, int digits) {
         return val < 0 ? (2 << digits) - val : val;
     }
 
     public static boolean isNum(final String num, int bits) {
         if (num == null || num.length() == 0) {
             return false;
         }
 
         final StringBuffer numBuf = new StringBuffer(num.toLowerCase());
         int sign = 0;
         if (numBuf.charAt(0) == '-') {
             numBuf.deleteCharAt(0);
             sign = 1;
             if (numBuf.indexOf("-") >= 0 || numBuf.length() == 0) {
                 return false;
             }
         }
 
         if (numBuf.charAt(numBuf.length() - 1) == 'b') {
             numBuf.deleteCharAt(numBuf.length() - 1);
             if (numBuf.length() == 0) {
                 return false;
             }
             while (numBuf.charAt(0) == '0' && numBuf.length() > 1) {
                 numBuf.deleteCharAt(0);
             }
             return numBuf.length() <= bits - sign && PATTERN_BIN.matcher(numBuf).matches();
         } else if (numBuf.charAt(numBuf.length() - 1) == 'o') {
             numBuf.deleteCharAt(numBuf.length() - 1);
             if (numBuf.length() == 0) {
                 return false;
             }
             while (numBuf.charAt(0) == '0' && numBuf.length() > 1) {
                 numBuf.deleteCharAt(0);
             }
             return (numBuf.length() - 1) * 3 <= bits && PATTERN_OCT.matcher(numBuf).matches() && width(Long.parseLong(numBuf.toString(), 8)) <= bits - sign;
         } else if (numBuf.charAt(numBuf.length() - 1) == 'h') {
             numBuf.deleteCharAt(numBuf.length() - 1);
             if (numBuf.length() == 0) {
                 return false;
             }
             while (numBuf.charAt(0) == '0' && numBuf.length() > 1) {
                 numBuf.deleteCharAt(0);
             }
             return (numBuf.length() - 1) * 4 <= bits && PATTERN_HEX.matcher(numBuf).matches() && width(Long.parseLong(numBuf.toString(), 16)) <= bits - sign;
         } else if (numBuf.length() >= 2 && "0x".equals(numBuf.subSequence(0, 2))) {
             numBuf.delete(0, 2);
             if (numBuf.length() == 0) {
                 return false;
             }
             while (numBuf.charAt(0) == '0' && numBuf.length() > 1) {
                 numBuf.deleteCharAt(0);
             }
             return (numBuf.length() - 1) * 4 <= bits && PATTERN_HEX.matcher(numBuf).matches() && width(Long.parseLong(numBuf.toString(), 16)) <= bits - sign;
         } else {
             while (numBuf.charAt(0) == '0' && numBuf.length() > 1) {
                 numBuf.deleteCharAt(0);
             }
             return (numBuf.length() - 1) * 3 <= bits && PATTERN_DEC.matcher(numBuf).matches() && width(parse(numBuf.toString())) <= bits - sign;
         }
     }
 
     public static long parse(final String num) {
         if (num.startsWith("-") && num.lastIndexOf('-') == 0) {
             return -parse(num.substring(1));
         }
 
         if (num.endsWith("b")) {
             return Long.parseLong(num.substring(0, num.length() - 1), 2);
         }
         if (num.endsWith("o")) {
             return Long.parseLong(num.substring(0, num.length() - 1), 8);
         }
         if (num.endsWith("h")) {
             return Long.parseLong(num.substring(0, num.length() - 1), 16);
         }
         if (num.startsWith("0x")) {
             return Long.parseLong(num.substring(2), 16);
         }
 
         return Long.parseLong(num);
     }
 
     public static int width(long value) {
         int msb = 0;
 
         while (value >= 1l << 16) {
             value >>= 16;
             msb += 16;
         }
 
         while (value >= 1l << 4) {
             value >>= 4;
             msb += 4;
         }
 
         while (value >= 1) {
             value >>= 1;
             msb += 1;
         }
 
         return msb;
     }
 }
 
