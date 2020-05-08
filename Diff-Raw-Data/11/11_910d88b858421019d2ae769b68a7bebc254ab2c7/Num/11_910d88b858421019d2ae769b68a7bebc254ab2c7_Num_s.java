 // A Lisp Driver to be embedded in Java Applications
 
 // The contents of this file are subject to the Mozilla Public License
 // Version 1.0 (the "License"); you may not use this file except in
 // compliance with the License. You may obtain a copy of the License at
 // http://www.mozilla.org/MPL/
 //
 // Software distributed under the License is distributed on an "AS IS"
 // basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 // License for the specific language governing rights and limitations
 // under the License.
 //
 // The Original Code is JAKLD code, released November 26, 2002.
 //
 // The Initial Developer of the Original Code is Taiichi Yuasa.
 // Portions created by Taiichi Yuasa are Copyright (C) 2002
 // Taiichi Yuasa. All Rights Reserved.
 //
 // Contributor(s): Taiichi Yuasa <yuasa@kuis.kyoto-u.ac.jp>
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 final class Num {
 
 	private final static int IntPoolSize = 1024;
 	private final static Integer[] IntPool = new Integer[IntPoolSize];
 
 	static Integer makeInt(int n) {
 		if (n >= 0 && n < IntPoolSize) {
 			Integer x = IntPool[n];
 			if (x != null)
 				return x;
 			else
 				return IntPool[n] = new Integer(n);
 		} else
 			return new Integer(n);
 	}
 
 	private final static Integer IntZERO = makeInt(0);
 	private final static Integer IntONE = makeInt(1);
 	private final static Integer IntMinusONE = new Integer(-1);
 	private final static BigInteger BigIntegerONE = BigInteger.valueOf(1);
 
 	private final static Boolean T = Boolean.TRUE;
 	private final static Boolean F = Boolean.FALSE;
 
 	static {
 		Subr.def("Num", "numberp", "number?", 1);
 	}
 
 	public static Boolean numberp(Object obj) {
 		return obj instanceof Number ? T : F;
 	}
 
 	private static Number normalize(BigInteger n) {
 		if (n.bitLength() < 32)
 			return makeInt(n.intValue());
 		else
 			return n;
 	}
 
 	static {
 		Subr.def("Num", "integerp", "integer?", 1);
 	}
 
 	public static Boolean integerp(Object obj) {
 		return obj instanceof Integer || obj instanceof BigInteger ? T : F;
 	}
 
 	static int compare(Number num1, Number num2) {
 		if (num1 instanceof Integer && num2 instanceof Integer) {
 			int x = num1.intValue();
 			int y = num2.intValue();
 			return x > y ? 1 : (x == y ? 0 : -1);
 		} else if (num1 instanceof Double || num2 instanceof Double) {
 			double x = num1.doubleValue();
 			double y = num2.doubleValue();
 			return x > y ? 1 : (x == y ? 0 : -1);
 		} else {
 			if (num1 instanceof Integer)
 				num1 = BigInteger.valueOf(num1.longValue());
 			else if (num2 instanceof Integer)
 				num2 = BigInteger.valueOf(num2.longValue());
 			return ((BigInteger) num1).compareTo((BigInteger) num2);
 		}
 	}
 
 	static {
 		Subr.def("Num", "EQ", "=", 1, true);
 	}
 
 	public static Boolean EQ(Number num, List args) {
 		for (; args != List.nil; args = (List) args.cdr)
 			if (compare(num, (Number) args.car) != 0)
 				return F;
 		return T;
 	}
 
 	static {
 		Subr.def("Num", "LT", "<", 1, true);
 	}
 
 	public static Boolean LT(Number num, List args) {
 		for (; args != List.nil; args = (List) args.cdr) {
 			Number next = (Number) args.car;
 			if (compare(num, next) >= 0)
 				return F;
 			num = next;
 		}
 		return T;
 	}
 
 	static {
 		Subr.def("Num", "GT", ">", 1, true);
 	}
 
 	public static Boolean GT(Number num, List args) {
 		for (; args != List.nil; args = (List) args.cdr) {
 			Number next = (Number) args.car;
 			if (compare(num, next) <= 0)
 				return F;
 			num = next;
 		}
 		return T;
 	}
 
 	static {
 		Subr.def("Num", "LE", "<=", 1, true);
 	}
 
 	public static Boolean LE(Number num, List args) {
 		for (; args != List.nil; args = (List) args.cdr) {
 			Number next = (Number) args.car;
 			if (compare(num, next) > 0)
 				return F;
 			num = next;
 		}
 		return T;
 	}
 
 	static {
 		Subr.def("Num", "GE", ">=", 1, true);
 	}
 
 	public static Boolean GE(Number num, List args) {
 		for (; args != List.nil; args = (List) args.cdr) {
 			Number next = (Number) args.car;
 			if (compare(num, next) < 0)
 				return F;
 			num = next;
 		}
 		return T;
 	}
 
 	static {
 		Subr.def("Num", "zerop", "zero?", 1);
 	}
 
 	public static Boolean zerop(Number num) {
 		if (num instanceof Integer)
 			return num.intValue() == 0 ? T : F;
 		else if (num instanceof BigInteger)
 			return F;
 		else
 			return num.doubleValue() == 0 ? T : F;
 	}
 
 	static {
 		Subr.def("Num", "positivep", "positive?", 1);
 	}
 
 	public static Boolean positivep(Number num) {
 		if (num instanceof Integer)
 			return num.intValue() > 0 ? T : F;
 		else if (num instanceof BigInteger)
 			return ((BigInteger) num).signum() > 0 ? T : F;
 		else
 			return num.doubleValue() > 0 ? T : F;
 	}
 
 	static {
 		Subr.def("Num", "negativep", "negative?", 1);
 	}
 
 	public static Boolean negativep(Number num) {
 		if (num instanceof Integer)
 			return num.intValue() < 0 ? T : F;
 		else if (num instanceof BigInteger)
 			return ((BigInteger) num).signum() < 0 ? T : F;
 		else
 			return num.doubleValue() < 0 ? T : F;
 	}
 
 	static {
 		Subr.def("Num", "oddp", "odd?", 1);
 	}
 
 	public static Boolean oddp(Number num) {
 		if (num instanceof Integer)
 			return (num.intValue() & 1) != 0 ? T : F;
 		else
 			return ((BigInteger) num).testBit(0) ? T : F;
 	}
 
 	static {
 		Subr.def("Num", "evenp", "even?", 1);
 	}
 
 	public static Boolean evenp(Number num) {
 		if (num instanceof Integer)
 			return (num.intValue() & 1) == 0 ? T : F;
 		else
 			return !((BigInteger) num).testBit(0) ? T : F;
 	}
 
 	static {
 		Subr.def("Num", "max", 1, true);
 	}
 
 	public static Number max(Number num, List args) {
 		for (; args != List.nil; args = (List) args.cdr)
 			if (compare((Number) args.car, num) > 0)
 				num = (Number) args.car;
 		return num;
 	}
 
 	static {
 		Subr.def("Num", "min", 1, true);
 	}
 
 	public static Number min(Number num, List args) {
 		for (; args != List.nil; args = (List) args.cdr)
 			if (compare((Number) args.car, num) < 0)
 				num = (Number) args.car;
 		return num;
 	}
 
 	private static Number long2Number(long n) {
 		if (n == (int) n)
 			return makeInt((int) n);
 		else
 			return BigInteger.valueOf(n);
 	}
 
 	static {
 		Subr.def("Num", "add", "+", 0, true);
 	}
 
 	public static Number add(List args) {
 		if (args == List.nil)
 			return IntZERO;
 
 		Number val = (Number) args.car;
 		while ((args = (List) args.cdr) != List.nil) {
 			Number next = (Number) args.car;
 			if (val instanceof Integer && next instanceof Integer)
 				val = long2Number(val.longValue() + next.longValue());
 			else if (val instanceof Double || next instanceof Double)
 				val = new Double(val.doubleValue() + next.doubleValue());
 			else {
 				if (val instanceof Integer)
 					val = BigInteger.valueOf(val.longValue());
 				else if (next instanceof Integer)
 					next = BigInteger.valueOf(next.longValue());
 				val = normalize(((BigInteger) val).add((BigInteger) next));
 			}
 		}
 		return val;
 	}
 
 	static {
 		Subr.def("Num", "mult", "*", 0, true);
 	}
 
 	public static Number mult(List args) {
 		if (args == List.nil)
 			return IntONE;
 
 		Number val = (Number) args.car;
 		while ((args = (List) args.cdr) != List.nil) {
 			Number next = (Number) args.car;
 			if (val instanceof Integer && next instanceof Integer)
 				val = long2Number(val.longValue() * next.longValue());
 			else if (val instanceof Double || next instanceof Double)
 				val = new Double(val.doubleValue() * next.doubleValue());
 			else {
 				if (val instanceof Integer)
 					val = BigInteger.valueOf(val.longValue());
 				else if (next instanceof Integer)
 					next = BigInteger.valueOf(next.longValue());
 				val = normalize(((BigInteger) val).multiply((BigInteger) next));
 			}
 		}
 		return val;
 	}
 
 	static {
 		Subr.def("Num", "minus", "-", 1, true);
 	}
 
 	public static Number minus(Number num, List args) {
 		if (args == List.nil)
 			if (num instanceof Integer)
 				return long2Number(-num.longValue());
 			else if (num instanceof BigInteger)
 				return normalize(((BigInteger) num).negate());
 			else
 				return new Double(-num.doubleValue());
 
 		do {
 			Number next = (Number) args.car;
 			if (num instanceof Integer && next instanceof Integer)
 				num = long2Number(num.longValue() - next.longValue());
 			else if (num instanceof Double || next instanceof Double)
 				num = new Double(num.doubleValue() - next.doubleValue());
 			else {
 				if (num instanceof Integer)
 					num = BigInteger.valueOf(num.longValue());
 				else if (next instanceof Integer)
 					next = BigInteger.valueOf(next.longValue());
 				num = normalize(((BigInteger) num).subtract((BigInteger) next));
 			}
 		} while ((args = (List) args.cdr) != List.nil);
 
 		return num;
 	}
 
 	static {
 		Subr.def("Num", "div", "/", 1, true);
 	}
 
 	public static Number div(Number num, List args) {
 		if (args == List.nil)
 			if (num instanceof Integer) {
 				int x = num.intValue();
 				if (x == 1 || x == -1)
 					return num;
 				else
 					return new Double(1.0 / x);
 			} else
 				return new Double(1.0 / num.doubleValue());
 
 		do {
 			Number next = (Number) args.car;
 			if (num instanceof Integer && next instanceof Integer) {
 				int x = num.intValue();
 				int y = next.intValue();
 				if ((x % y) == 0)
 					num = makeInt(x / y);
 				else
 					num = new Double(((double) x) / ((double) y));
 			} else if (num instanceof Double || next instanceof Double)
 				num = new Double(num.doubleValue() / next.doubleValue());
 			else {
 				if (num instanceof Integer)
 					num = BigInteger.valueOf(num.longValue());
 				else if (next instanceof Integer)
 					next = BigInteger.valueOf(next.longValue());
 				BigInteger[] z = ((BigInteger) num)
 						.divideAndRemainder((BigInteger) next);
 				if (z[1].signum() == 0)
 					num = normalize(z[0]);
 				else
 					num = new Double(num.doubleValue() / next.doubleValue());
 			}
 		} while ((args = (List) args.cdr) != List.nil);
 
 		return num;
 	}
 
 	static {
 		Subr.def("Num", "onePlus", "1+", 1);
 	}
 
 	public static Number onePlus(Number num) {
 		if (num instanceof Integer)
 			return long2Number(num.longValue() + 1);
 		else if (num instanceof BigInteger)
 			return normalize(((BigInteger) num).add(BigIntegerONE));
 		else
 			return new Double(num.doubleValue() + 1);
 	}
 
 	static {
 		Subr.def("Num", "oneMinus", "1-", 1);
 	}
 
 	public static Number oneMinus(Number num) {
 		if (num instanceof Integer)
 			return long2Number(num.longValue() - 1);
 		else if (num instanceof BigInteger)
 			return normalize(((BigInteger) num).subtract(BigIntegerONE));
 		else
 			return new Double(num.doubleValue() - 1);
 	}
 
 	static {
 		Subr.def("Num", "abs", 1);
 	}
 
 	public static Number abs(Number num) {
 		if (num instanceof Integer) {
 			long x = num.longValue();
 			return x >= 0 ? num : long2Number(-x);
 		} else if (num instanceof BigInteger)
 			return ((BigInteger) num).abs();
 		else {
 			double x = num.doubleValue();
 			return x >= 0 ? num : new Double(-x);
 		}
 	}
 
 	static {
 		Subr.def("Num", "quotient", 2);
 	}
 
 	public static Number quotient(Number num1, Number num2) {
 		if (num1 instanceof Integer)
 			if (num2 instanceof Integer)
 				return makeInt(num1.intValue() / num2.intValue());
 			else
 				num1 = BigInteger.valueOf(num1.longValue());
 		else if (num2 instanceof Integer)
 			num2 = BigInteger.valueOf(num2.longValue());
 
 		return normalize(((BigInteger) num1).divide((BigInteger) num2));
 	}
 
 	static {
 		Subr.def("Num", "remainder", 2);
 	}
 
 	public static Number remainder(Number num1, Number num2) {
 		if (num1 instanceof Integer)
 			if (num2 instanceof Integer)
 				return makeInt(num1.intValue() % num2.intValue());
 			else
 				num1 = BigInteger.valueOf(num1.longValue());
 		else if (num2 instanceof Integer)
 			num2 = BigInteger.valueOf(num2.longValue());
 
 		return normalize(((BigInteger) num1).remainder((BigInteger) num2));
 	}
 
 	static {
 		Subr.def("Num", "modulo", 2);
 	}
 
 	public static Number modulo(Number num1, Number num2) {
 		if (num1 instanceof Integer)
 			if (num2 instanceof Integer) {
 				int y = num2.intValue();
 				int r = num1.intValue() % y;
 				return makeInt(((long) y) * r < 0 ? r + y : r);
 			} else
 				num1 = BigInteger.valueOf(num1.longValue());
 		else if (num2 instanceof Integer)
 			num2 = BigInteger.valueOf(num2.longValue());
 
 		BigInteger X = (BigInteger) num1;
 		BigInteger Y = (BigInteger) num2;
 		if (Y.signum() < 0)
 			return normalize(X.negate().mod(Y.negate()).negate());
 		else
 			return normalize(X.mod(Y));
 	}
 
 	private static int gcd(int m, int n) {
 		// m must be non-negative
 		if (n < 0)
 			n = -n;
 		if (m < n) {
 			int tmp = m;
 			m = n;
 			n = tmp;
 		}
 		for (;;)
 			if (n == 0)
 				return m;
 			else if (n == 1)
 				return n;
 			else {
 				int tmp = n;
 				n = m % n;
 				m = tmp;
 			}
 	}
 
 	static {
 		Subr.def("Num", "gcd", 0, true);
 	}
 
 	public static Number gcd(List args) {
 		if (args == List.nil)
 			return IntZERO;
 
 		Number val = abs((Number) args.car);
 		while ((args = (List) args.cdr) != List.nil) {
 			Number next = (Number) args.car;
 			if (val instanceof Integer)
 				if (next instanceof Integer) {
 					val = makeInt(gcd(val.intValue(), next.intValue()));
 					continue;
 				} else
 					val = BigInteger.valueOf(val.longValue());
 			else if (next instanceof Integer)
 				next = BigInteger.valueOf(next.longValue());
 
 			val = normalize(((BigInteger) val).gcd((BigInteger) next));
 		}
 		return val;
 	}
 
 	static {
 		Subr.def("Num", "lcm", 0, true);
 	}
 
 	public static Number lcm(List args) {
 		if (args == List.nil)
 			return IntONE;
 
 		Number val = abs((Number) args.car);
 		while ((args = (List) args.cdr) != List.nil) {
 			Number next = (Number) args.car;
 			if (val instanceof Integer)
 				if (next instanceof Integer) {
 					int m = val.intValue();
 					int n = next.intValue();
 					val = makeInt((m * (n < 0 ? -n : n)) / gcd(m, n));
 					continue;
 				} else
 					val = BigInteger.valueOf(val.longValue());
 			else if (next instanceof Integer)
 				next = BigInteger.valueOf(next.longValue());
 
 			BigInteger X = (BigInteger) val;
 			BigInteger Y = ((BigInteger) next).abs();
 			val = normalize(X.multiply(Y).divide(X.gcd(Y)));
 		}
 		return val;
 	}
 
 	static {
 		Subr.def("Num", "floor", 1);
 	}
 
 	public static Number floor(Number num) {
 		if (num instanceof Integer || num instanceof BigInteger)
 			return num;
 		else {
 			double x = Math.floor(num.doubleValue());
 			if (x == (int) x)
 				return makeInt((int) x);
 			else
 				return new BigDecimal(x).toBigInteger();
 		}
 	}
 
 	static {
 		Subr.def("Num", "ceiling", 1);
 	}
 
 	public static Number ceiling(Number num) {
 		if (num instanceof Integer || num instanceof BigInteger)
 			return num;
 		else {
 			double x = Math.ceil(num.doubleValue());
 			if (x == (int) x)
 				return makeInt((int) x);
 			else
 				return new BigDecimal(x).toBigInteger();
 		}
 	}
 
 	static {
 		Subr.def("Num", "truncate", 1);
 	}
 
 	public static Number truncate(Number num) {
 		if (num instanceof Integer || num instanceof BigInteger)
 			return num;
 		else {
 			double x = num.doubleValue();
 			x = (x >= 0 ? Math.floor(x) : Math.ceil(x));
 			if (x == (int) x)
 				return makeInt((int) x);
 			else
 				return new BigDecimal(x).toBigInteger();
 		}
 	}
 
 	static {
 		Subr.def("Num", "round", 1);
 	}
 
 	public static Number round(Number num) {
 		if (num instanceof Integer || num instanceof BigInteger)
 			return num;
 		else {
 			double x = num.doubleValue();
 			long n = Math.round(x);
 			if ((n - x) == 0.5 && (n & 1) == 1)
 				n--;
 			return long2Number(n);
 		}
 	}
 
 	static {
 		Subr.def("Num", "num2string", "number->string", 1, 1);
 	}
 
	public static String num2string(Number num, Integer n) {
 		int radix = (n == null ? 10 : n.intValue());
 
 		if (num instanceof Integer)
			return Integer.toString(num.intValue(), radix);
 		else if (num instanceof BigInteger)
			return ((BigInteger) num).toString(radix);
 		else
			return num.toString();
 	}
 
 	static {
 		Subr.def("Num", "string2num", "string->number", 1, 1);
 	}
 
 	public static Object string2num(LString s, Integer n) {
 		try {
 			return IO.readNumber(s.toString(), (n == null ? 10 : n.intValue()));
 		} catch (NumberFormatException e) {
 			return F;
 		}
 	}
 
 	static {
 		Subr.def("Num", "sqrt", 1);
 	}
 
 	public static Double sqrt(Number num) {
 		return new Double(Math.sqrt(num.doubleValue()));
 	}
 
 	static {
 		Subr.def("Num", "exp", 1);
 	}
 
 	public static Double exp(Number num) {
 		return new Double(Math.exp(num.doubleValue()));
 	}
 
 	static {
 		Subr.def("Num", "log", 1);
 	}
 
 	public static Double log(Number num) {
 		return new Double(Math.log(num.doubleValue()));
 	}
 
 	static {
 		Subr.def("Num", "sin", 1);
 	}
 
 	public static Double sin(Number num) {
 		return new Double(Math.sin(num.doubleValue()));
 	}
 
 	static {
 		Subr.def("Num", "cos", 1);
 	}
 
 	public static Double cos(Number num) {
 		return new Double(Math.cos(num.doubleValue()));
 	}
 
 	static {
 		Subr.def("Num", "tan", 1);
 	}
 
 	public static Double tan(Number num) {
 		return new Double(Math.tan(num.doubleValue()));
 	}
 
 	static {
 		Subr.def("Num", "asin", 1);
 	}
 
 	public static Double asin(Number num) {
 		return new Double(Math.asin(num.doubleValue()));
 	}
 
 	static {
 		Subr.def("Num", "acos", 1);
 	}
 
 	public static Double acos(Number num) {
 		return new Double(Math.acos(num.doubleValue()));
 	}
 
 	static {
 		Subr.def("Num", "atan", 1, 1);
 	}
 
 	public static Double atan(Number num1, Number num2) {
 		if (num2 == null)
 			return new Double(Math.atan(num1.doubleValue()));
 		else {
 			return new Double(
 					Math.atan2(num1.doubleValue(), num2.doubleValue()));
 		}
 	}
 
 	static {
 		Subr.def("Num", "expt", 2);
 	}
 
 	public static Number expt(Number num1, Number num2) {
 		double x = Math.pow(num1.doubleValue(), num2.doubleValue());
 		if (num1 instanceof Double || num2 instanceof Double
 				|| x >= Double.POSITIVE_INFINITY
 				|| x <= Double.NEGATIVE_INFINITY)
 			return new Double(x);
 
 		if (num1 instanceof Integer) {
 			int m = num1.intValue();
 			if (m == 0)
 				return num2.doubleValue() == 0 ? IntONE : IntZERO;
 			else if (m == 1)
 				return IntONE;
 			else if (m == -1)
 				if (num2 instanceof Integer)
 					return (num2.intValue() & 1) != 0 ? IntMinusONE : IntONE;
 				else
 					return ((BigInteger) num2).testBit(0) ? IntMinusONE
 							: IntONE;
 			else { // num2 cannot be BigInteger
 				int n = num2.intValue();
 				return n >= 0 ? expt((long) m, n, (long) 1) : new Double(x);
 			}
 		} else { // num2 cannot be BigInteger
 			int n = num2.intValue();
 			return n >= 0 ? expt((BigInteger) num1, n, BigIntegerONE)
 					: new Double(x);
 		}
 	}
 
 	private static Number expt(long m, int n, long val) {
 		boolean escape = false;
 		while (n > 0) {
 			if ((n & 1) == 1) {
 				n--;
 				val *= m;
 				escape = (val != (int) val);
 			} else {
 				n >>= 1;
 				m *= m;
 				escape = (m != (int) m);
 			}
 			if (escape)
 				return expt(BigInteger.valueOf(m), n, BigInteger.valueOf(val));
 		}
 		return makeInt((int) val);
 	}
 
 	private static Number expt(BigInteger m, int n, BigInteger val) {
 		while (n > 0)
 			if ((n & 1) == 1) {
 				n--;
 				val = val.multiply(m);
 			} else {
 				n >>= 1;
 				m = m.multiply(m);
 			}
 		return normalize(val);
 	}
 
 	static {
 		Subr.def("Num", "logand", 0, true);
 	}
 
 	public static Number logand(List args) {
 		if (args == List.nil)
 			return IntMinusONE;
 
 		Number val = (Number) args.car;
 		while ((args = (List) args.cdr) != List.nil) {
 			Number next = (Number) args.car;
 			if (val instanceof Integer)
 				if (next instanceof Integer) {
 					val = makeInt(val.intValue() & next.intValue());
 					continue;
 				} else
 					val = BigInteger.valueOf(val.longValue());
 			else if (next instanceof Integer)
 				next = BigInteger.valueOf(next.longValue());
 
 			val = normalize(((BigInteger) val).and((BigInteger) next));
 		}
 		return val;
 	}
 
 	static {
 		Subr.def("Num", "logior", 0, true);
 	}
 
 	public static Number logior(List args) {
 		if (args == List.nil)
 			return IntZERO;
 
 		Number val = (Number) args.car;
 		while ((args = (List) args.cdr) != List.nil) {
 			Number next = (Number) args.car;
 			if (val instanceof Integer)
 				if (next instanceof Integer) {
 					val = makeInt(val.intValue() | next.intValue());
 					continue;
 				} else
 					val = BigInteger.valueOf(val.longValue());
 			else if (next instanceof Integer)
 				next = BigInteger.valueOf(next.longValue());
 
 			val = normalize(((BigInteger) val).or((BigInteger) next));
 		}
 		return val;
 	}
 
 	static {
 		Subr.def("Num", "logxor", 0, true);
 	}
 
 	public static Number logxor(List args) {
 		if (args == List.nil)
 			return IntZERO;
 
 		Number val = (Number) args.car;
 		while ((args = (List) args.cdr) != List.nil) {
 			Number next = (Number) args.car;
 			if (val instanceof Integer)
 				if (next instanceof Integer) {
 					val = makeInt(val.intValue() ^ next.intValue());
 					continue;
 				} else
 					val = BigInteger.valueOf(val.longValue());
 			else if (next instanceof Integer)
 				next = BigInteger.valueOf(next.longValue());
 
 			val = normalize(((BigInteger) val).xor((BigInteger) next));
 		}
 		return val;
 	}
 
 	static {
 		Subr.def("Num", "lognot", 1);
 	}
 
 	public static Number lognot(Number num) {
 		if (num instanceof Integer)
 			return makeInt(~num.intValue());
 		else
 			return normalize(((BigInteger) num).not());
 	}
 
 	static {
 		Subr.def("Num", "logshl", 2);
 	}
 
 	public static Number logshl(Number num, int n) {
 		if (num instanceof Integer)
 			return normalize(BigInteger.valueOf(num.longValue()).shiftLeft(n));
 		else
 			return normalize(((BigInteger) num).shiftLeft(n));
 	}
 
 	static {
 		Subr.def("Num", "logshr", 2);
 	}
 
 	public static Number logshr(Number num, int n) {
 		if (num instanceof Integer)
 			return normalize(BigInteger.valueOf(num.longValue()).shiftRight(n));
 		else
 			return normalize(((BigInteger) num).shiftRight(n));
 	}
 
 	static {
 		Subr.def("Num", "random", 0, 1);
 	}
 
 	public static Number random(Number num) {
 		double r = Math.random();
 		if (num == null)
 			return new Double(r);
 		else if (num instanceof Integer)
 			return makeInt((int) (r * num.intValue()));
 		else if (num instanceof BigInteger)
 			return normalize(new BigDecimal(r * num.doubleValue())
 					.toBigInteger());
 		else
 			return new Double(r * num.doubleValue());
 	}
 
 	static void init() {
 	}
 }
