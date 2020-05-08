 package standardLib;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Scanner;
 
 import utils.FullType;
 import utils.FuncSymbolTable;
 import utils.FullType.Type;
 
 /**
  * A Standard library class. all the library functions should be static and this
  * class should not be instanced.
  */
 public abstract class StdLib {
   private static Random gen = new Random();
   
   /**
    * Compares two strings lexicographically.
    * 
    * @param s1
    *          first string
    * @param s2
    *          second string
    * @return the value 0 if the argument string is equal to this string; a value
    *         less than 0 if this string is lexicographically less than the
    *         string argument; and a value greater than 0 if this string is
    *         lexicographically greater than the string argument.
    */
   public static int _strCmp(final String s1, final String s2) {
     return s1.compareTo(s2);
   }
   
   /**
    * Concatenates the specified string to the end of this string.
    * 
    * @param s1
    *          the first string
    * @param s2
    *          the String that is concatenated to the end of s1.
    * @return a string that represents the concatenation of s1 characters
    *         followed by s2 characters.
    */
   public static String _strConcat(final String s1, final String s2) {
     return s1.concat(s2);
   }
   
   /**
    * Returns the length of a string.
    * 
    * @param s
    *          the first string
    * @return the length of the sequence of characters represented by string.
    */
   public static int _strLen(final String s) {
     return s.length();
   }
   
   /**
    * Converts s string to a new character array.
    * 
    * @param s
    *          the string
    * @return a newly allocated character array whose length is the length of
    *         this string and whose contents are initialized to contain the
    *         character sequence represented by this string.
    */
   public static char[] _strToArr(final String s) {
     return s.toCharArray();
   }
   
   /**
    * Returns a String object representing the specified char. The result is a
    * string of length 1 consisting solely of the specified char.
    * 
    * @param c
    *          the character to be converted
    * @return the string representation of the specified char
    */
   public static String _charToStr(final char c) {
     return Character.toString(c);
   }
   
   /**
    * print a new end of line.
    */
   public static void _writeln() {
     System.out.print("\n");
   }
   
   /**
    * print an integer and a end of line.
    * 
    * @param i
    *          int to print
    */
   public static void _writeln(final long i) {
     _write(i);
     _writeln();
   }
   
   /**
    * prints a char and a end of line.
    * 
    * @param c
    *          char to print
    */
   public static void _writeln(final char c) {
     _write(c);
     _writeln();
   }
   
   /**
    * print a boolean and a end of line.
    * 
    * @param b
    *          boolean to print
    */
   public static void _writeln(final boolean b) {
     _write(b);
     _writeln();
   }
   
   /**
    * prints a double and a end of line.
    * 
    * @param d
    *          double to print
    */
   public static void _writeln(final double d) {
     _write(d);
     _writeln();
   }
   
   /**
    * prints a string and a end of line.
    * 
    * @param s
    *          string to print
    */
   public static void _writeln(final String s) {
     _write(s);
     _writeln();
   }
   
   /**
    * prints an int.
    * 
    * @param i
    *          int to print
    */
   public static void _write(final long i) {
     System.out.print(i);
   }
   
   /**
    * prints a char.
    * 
    * @param c
    *          char to print
    */
   public static void _write(final char c) {
     System.out.print(c);
   }
   
   /**
    * prints a boolean.
    * 
    * @param b
    *          boolean to print
    */
   public static void _write(final boolean b) {
     System.out.print(b);
   }
   
   /**
    * prints a double
    * 
    * @param d
    *          double to print
    */
   public static void _write(final double d) {
     System.out.print(d);
   }
   
   /**
    * prints a string
    * 
    * @param s
    *          string to print
    */
   public static void _write(final String s) {
     System.out.print(s);
   }
   
   /**
    * reads int from standard input.
    * 
    * @return the integer that was entered
    */
   public static long _readInt() {
    return new Scanner(System.in).nextInt();
   }
   
   /**
    * reads double from standard input.
    * 
    * @return the double that was entered
    */
   public static double _readReal() {
    return new Scanner(System.in).nextDouble();
   }
   
   /**
    * reads string from standard input.
    * 
    * @return the string that was entered
    */
   public static String _readString() {
    return new Scanner(System.in).nextLine();
   }
   
   /**
    * Returns the length of the specified array object, as an int.
    * 
    * @param ar
    *          the array
    * @return the length of the array
    */
   // Comment - please don't change this function name or argument
   public static int _arrayLength(final Object ar) {
     return Array.getLength(ar);
   }
   
   /**
    * return an integer between 0 (inclusive) and upperBound (exclusive)
    * randomly.
    * 
    * @param upperBound
    *          the bound on the random number to be returned. Must be positive.
    * @return an integer between 0 (inclusive) and upperBound (exclusive)
    *         randomly.
    */
   public static int _random(final long upperBound) {
     return gen.nextInt((int) upperBound);
   }
   
   /**
    * Returns the absolute value of a double value.
    * 
    * @param r
    *          the argument whose absolute value is to be determined
    * @return the absolute value of the argument.
    */
   public static double _abs(final double r) {
     return Math.abs(r);
   }
   
   /**
    * Returns square root of a double value
    * 
    * @param r
    *          a value
    * @return the positive square root of r.
    */
   public static double _sqrt(final double r) {
     return Math.sqrt(r);
   }
   
   /**
    * Returns the value of the first argument raised to the power of the second
    * argument
    * 
    * @param a
    *          the base.
    * @param b
    *          the exponent.
    * @return the value a^b
    */
   public static double _power(final double a, final double b) {
     return Math.pow(a, b);
   }
   
   /**
    * Returns the trigonometric cosine of an angle.
    * 
    * @param r
    *          an angle, in radians.
    * @return the cosine of the argument.
    */
   public static double _cos(final double r) {
     return Math.cos(r);
   }
   
   /**
    * Returns the trigonometric sine of an angle.
    * 
    * @param r
    *          an angle, in radians.
    * @return the sine of the argument.
    */
   public static double _sin(final double r) {
     return Math.sin(r);
   }
   
   /**
    * Returns true if the specified number is a Not-a-Number (NaN) value, false
    * otherwise.
    * 
    * @param r
    *          the value to be tested.
    * @return true if the value of the argument is NaN; false otherwise.
    */
   public static boolean _isNaN(final double r) {
     return Double.isNaN(r);
   }
   
   /**
    * returns the double value that is closer than any other to pi, the ratio of
    * the circumference of a circle to its diameter.
    * 
    * @return pi.
    */
   public static double _pi() {
     return Math.PI;
   }
   
   /**
    * Converting integer to real.
    * 
    * @param i
    *          integer value.
    * @return real value,
    */
   public static double _intToReal(final long i) {
     return i;
   }
   
   /**
    * Converting integer to string
    * 
    * @param i
    *          integer value.
    * @return string representing received value.
    */
   public static String _intToString(final long i) {
     return Long.toString(i);
   }
   
   /**
    * Converting real to integer
    * 
    * @param d
    *          double value.
    * @return integer value.
    */
   public static long _realToInt(final double d) {
     return (long) d;
   }
   
   /**
    * Converting real to string
    * 
    * @param d
    *          double value.
    * @return string representing received value.
    */
   public static String _realToString(final double d) {
     return Double.toString(d);
   }
   
   /**
    * Converting string to real
    * 
    * @param s
    *          string.
    * @return real value represented by received string
    */
   public static double _stringToReal(final String s) {
     return Double.parseDouble(s);
   }
   
   /**
    * Converting string to integer
    * 
    * @param s
    *          string
    * @return integer value represented by received string
    */
   public static long _stringToInt(final String s) {
     return Long.parseLong(s);
   }
   
   /**
    * generates a function symbol table for the stdLib and add it to fst.
    * 
    * @param fst
    *          a function symbol table that should hold stdlib symbols.
    */
   public static void genFuncSymbolTable(final FuncSymbolTable fst) {
     addStrFuncs(fst);
     addIoFuncs(fst);
     addMathFuncs(fst);
     addMiscFuncs(fst);
     addCastFuncs(fst);
   }
   
   /**
    * initialize an array of Objects. array can be multidimensional.
    * 
    * @param oArr
    *          must be array of Object and not primitives !
    */
   public static void initArray(final Object oArr) {
     if (oArr == null || !oArr.getClass().isArray() // should never happen
         || oArr.getClass().isPrimitive())
       return;
     final Object arr[] = (Object[]) oArr;
     for (int i = 0; i < arr.length; i++)
       if (arr[i] == null)
         try {
           arr[i] = oArr.getClass().getComponentType().newInstance();
         } catch (final Exception e) {
           // this should never happen either.
         }
       else
         initArray(arr[i]);
   }
   
   /**
    * Presents a thrown exception to the user in a clear and simple manner.
    * 
    * @param e
    *          - the exception thrown.
    */
   public static void showException(final Exception e) {
     final String msg = "\nWhat have you done ?!\nActually, its just a run" + 
     						"-time exception, this is what happend :\" ";
     System.out.println(msg + e.toString().replaceAll("java.lang.", "") + " \"");
   }
   
   private static void addMiscFuncs(final FuncSymbolTable fst) {
     final FullType tInt = new FullType(Type.INTEGER);
     addFunc(fst, "arrayLength", tInt, new FullType(Type.ARRAY));
     addFunc(fst, "random", tInt, tInt);
   }
   
   private static void addMathFuncs(final FuncSymbolTable fst) {
     final FullType tBool = new FullType(Type.BOOLEAN);
     final FullType tReal = new FullType(Type.REAL);
     addFunc(fst, "abs", tReal, tReal);
     addFunc(fst, "sqrt", tReal, tReal);
     addFunc(fst, "power", tReal, tReal, tReal);
     addFunc(fst, "cos", tReal, tReal);
     addFunc(fst, "sin", tReal, tReal);
     addFunc(fst, "isNaN", tBool, tReal);
     addFunc(fst, "pi", tReal);
   }
   
   private static void addIoFuncs(final FuncSymbolTable fst) {
     final FullType tString = new FullType(Type.STRING);
     final FullType tInt = new FullType(Type.INTEGER);
     final FullType tChar = new FullType(Type.CHAR);
     final FullType tVoid = new FullType(Type.VOID);
     final FullType tBool = new FullType(Type.BOOLEAN);
     final FullType tReal = new FullType(Type.REAL);
     addFunc(fst, "writeln", tVoid);
     addFunc(fst, "writeln", tVoid, tInt);
     addFunc(fst, "writeln", tVoid, tChar);
     addFunc(fst, "writeln", tVoid, tBool);
     addFunc(fst, "writeln", tVoid, tReal);
     addFunc(fst, "writeln", tVoid, tString);
     addFunc(fst, "write", tVoid, tInt);
     addFunc(fst, "write", tVoid, tChar);
     addFunc(fst, "write", tVoid, tBool);
     addFunc(fst, "write", tVoid, tReal);
     addFunc(fst, "write", tVoid, tString);
     addFunc(fst, "readInt", tInt);
     addFunc(fst, "readReal", tReal);
     addFunc(fst, "readString", tString);
   }
   
   private static void addStrFuncs(final FuncSymbolTable fst) {
     final FullType tString = new FullType(Type.STRING);
     final FullType tInt = new FullType(Type.INTEGER);
     final FullType tChar = new FullType(Type.CHAR);
     final FullType tCharArr = new FullType(Type.ARRAY, tChar);
     addFunc(fst, "strCmp", tInt, tString, tString);
     addFunc(fst, "strConcat", tString, tString, tString);
     addFunc(fst, "strLen", tInt, tString);
     addFunc(fst, "strToArr", tCharArr, tString);
     addFunc(fst, "charToStr", tString, tChar);
   }
   
   private static void addCastFuncs(final FuncSymbolTable fst) {
     final FullType tString = new FullType(Type.STRING);
     final FullType tInt = new FullType(Type.INTEGER);
     final FullType tReal = new FullType(Type.REAL);
     addFunc(fst, "intToReal", tReal, tInt);
     addFunc(fst, "intToString", tString, tInt);
     addFunc(fst, "realToInt", tInt, tReal);
     addFunc(fst, "realToString", tString, tReal);
     addFunc(fst, "stringToReal", tReal, tString);
     addFunc(fst, "stringToInt", tInt, tString);
   }
   
   private static void addFunc(final FuncSymbolTable fst, final String name, final FullType retType, final FullType... params) {
     final ArrayList<FullType> at = new ArrayList<FullType>();
     for (final FullType ft : params)
       at.add(ft);
     fst.addFunc(name, retType, at);
   }
 }
