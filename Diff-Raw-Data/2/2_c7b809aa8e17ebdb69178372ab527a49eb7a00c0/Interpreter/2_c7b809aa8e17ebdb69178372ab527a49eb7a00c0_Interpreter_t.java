 // Copyright (c) 2009, Raymond R. Medeiros. All rights reserved.
 
 package detroit;
 import java.util.*;
 import java.io.*;
 import java.lang.reflect.*;
 
 public class Interpreter
 {
 	public static final int OP_ENV = 0;
 	public static final int OP_LIB = 1;
 	public static final int OP_ARG = 2;
 	public static final int OP_LMBD = 3;
 	public static final int OP_LIT = 4;
 
 	private static final int HAS_REST = 1;
 	private static final int BUILTIN = 2;
 
 	public static String unspecified = new String("#<unspecified>");
 	public static Object setSlot = new Object();
 	public static Procedure identity = null;
 
 	private static Boolean TRUE = Boolean.TRUE;
 	private static Boolean FALSE = Boolean.FALSE;
 
 	public Pair envs = null;
 	public Environment r5rs = new Environment();
 
 	public static final Pair startList()
 	{
 		return cons(null, null);
 	}
 
 	public static final void appendToList(Pair list, Object item)
 	{
 		if (list.car == null)
 			list.car = list.cdr = cons(item, null);
 		else
 			list.cdr = list.rest().cdr = cons(item, null);
 	}
 
 	public static final Pair getList(Pair list)
 	{
 		return (Pair)list.car;
 	}
 
 	private static final String getString(Object obj) throws Exception
 	{
 		if (obj instanceof char[])
 			return new String((char[])obj);
 		return (String)obj;
 	}
 
 	private static final Byte getByte(Object obj) throws Exception
 	{
 		if (obj instanceof Byte)
 			return (Byte)obj;
 		return new Byte(((Number)obj).byteValue());
 	}
 
 	private static final Float getFloat(Object obj) throws Exception
 	{
 		if (obj instanceof Float)
 			return (Float)obj;
 		return new Float(((Number)obj).floatValue());
 	}
 
 	private static final Class getClass(Object obj) throws Exception
 	{
 		if (obj instanceof Class)
 			return (Class)obj;
 		String name = getString(obj);
 
 		if (name.equals("void"))         return Void.TYPE;
 		else if (name.equals("boolean")) return Boolean.TYPE;
 		else if (name.equals("char"))    return Character.TYPE;
 		else if (name.equals("byte"))    return Byte.TYPE;
 		else if (name.equals("short"))   return Short.TYPE;
 		else if (name.equals("int"))     return Integer.TYPE;
 		else if (name.equals("long"))    return Long.TYPE;
 		else if (name.equals("float"))   return Float.TYPE;
 		else if (name.equals("double"))  return Double.TYPE;
 
 		return Class.forName(name);
 	}
 
 	public static final int length(Pair list)
 	{
 		int i = 0;
 		while (list != null)
 		{
 			++i;
 			list = list.rest();
 		}
 		return i;
 	}
 
 	public static final boolean listp(Object list)
 	{
 		Object lag = list;
 		for (;;)
 		{
 			if (!(list instanceof Pair)) return list == null;
 			list = cdr(list);
 			if (!(list instanceof Pair)) return list == null;
 			list = cdr(list);
 			lag = cdr(lag);
 			if (list == lag) return false;
 		}
 	}
 
 	public static final Object makeArray(Class clas, Pair list)
 	{
 		Object result = Array.newInstance(clas, length(list));
 		for (int i=0; list != null; ++i, list = (Pair)list.cdr)
 		{
 			Array.set(result, i, list.car);
 		}
 		return result;
 	}
 
 	public static final Object[] cooerceIntoArray(Class[] types, ArgList argList) throws Exception
 	{
 		Object[] args = new Object[types.length];
 		for (int i=0; argList.args != null; ++i)
 		{
 			Class type = types[i];
 			if (type == String.class)
 				args[i] = getString(argList.next());
 			else if (type == Byte.TYPE || type == Byte.class)
 				args[i] = getByte(argList.next());
 			else if (type == Float.TYPE || type == Float.class)
 				args[i] = getFloat(argList.next());
 			else
 				args[i] = argList.next();
 		}
 		return args;
 	}
 
 	public static final Object callMethod(ArgList argList) throws Exception
 	{
 		Method m = (Method)argList.next();
 		Object inst;
 
 		if (Modifier.isStatic(m.getModifiers()))
 			inst = null;
 		else
 			inst = argList.next();
 
 		Object[] args = cooerceIntoArray(m.getParameterTypes(), argList);
 
 		Object res = m.invoke(inst, args);
 		if (res instanceof Boolean)
 			return ((Boolean)res).booleanValue() ? TRUE : FALSE;
 		return res;
 	}
 
 	public static final Object callConstructor(ArgList argList) throws Exception
 	{
 		Constructor c = (Constructor)argList.next();
 		Object[] args = cooerceIntoArray(c.getParameterTypes(), argList);
 		return c.newInstance(args);
 	}
 
 	public static final Boolean eqv(Object a, Object b)
 	{
 		if (a == b) return TRUE;
 		else if (a == null || b == null) return FALSE;
 		else if (a.getClass() != b.getClass()) return FALSE;
 		else if (a instanceof Integer) return ((Integer)a).intValue() == ((Integer)b).intValue() ? TRUE : FALSE;
 		else if (a instanceof Double) return ((Double)a).doubleValue() == ((Double)b).doubleValue() ? TRUE : FALSE;
 		else if (a instanceof Character) return ((Character)a).charValue() == ((Character)b).charValue() ? TRUE : FALSE;
 		else return FALSE;
 	}
 
 	public static final Boolean equal(Object a, Object b)
 	{
 		if (a instanceof Pair)
 			return (b instanceof Pair &&
 					(equal(car(a), car(b)) == TRUE) &&
 					(equal(cdr(a), cdr(b)) == TRUE)) ? TRUE : FALSE;
 		if (a instanceof char[])
 			return (b instanceof char[] &&
 					(new String((char[])a)).equals(new String((char[])b)))
 				? TRUE : FALSE;
 		return eqv(a, b);
 	}
 
 
 	public final Object run(ArgList argList) throws Exception
 	{
 		Procedure nextProc;
 
 		Object[] fillArgs = null;
 		Object[] tmpArgs1 = new Object[8];
 		Object[] tmpArgs2 = new Object[8];
 
 		Pair nextEnv;
 
 		Pair pair_minus_0 = cons(null, null);
 		Pair pair_minus_1 = cons(null, pair_minus_0);
 
 		Pair listBuilder = cons(null, null);
 		Pair lastPair;
 
 		nextProc = ((Procedure)(argList.next()));
 
 		for (;;)
 		{
 
 			if ((nextProc.flags & BUILTIN) != 0)
 			{
 
 				switch (nextProc.args)
 				{
 					case 0:
 						return argList.next();
 					case 1:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Op location = (Op)argList.next();
 							Object value = argList.next();
 							if (location.type == OP_ENV)
 							{
 								int up = location.p1;
 								Pair env = argList.currEnv;
 								while (--up >= 0)
 									env = env.rest();
 								((Object[])env.car)[location.p2] = value;
 							}
 							else
 								location.envSlots.setElementAt(value, location.p1);
 
 							argList.setNext(unspecified);
 							continue;
 						}
 					case 2:
 						{
 							Object cont = argList.next();
 							if (argList.next() != FALSE)
 								nextProc = ((Procedure)(argList.next()));
 							else
 							{
 								argList.next();
 								nextProc = ((Procedure)(argList.next()));
 							}
 							argList.setNext(cont);
 							continue;
 						}
 					case 3:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Number number;
 							Object result = null;
 							int intResult = 0;
 							if (argList.args == null)
 							{
 								result = new Integer(intResult);
 							}
 							while (argList.args != null)
 							{
 								number = ((Number)argList.next());
 								if (number instanceof Double)
 								{
 									double doubleResult = intResult + number.doubleValue();;
 									while (argList.args != null)
 										doubleResult += ((Number)argList.next()).doubleValue();
 									result = new Double(doubleResult);
 									break;
 								}
 								intResult += number.intValue();
 								if (argList.args == null)
 								{
 									result = new Integer(intResult);
 								}
 							}
 							argList.setNext(result);
 							continue;
 						}
 					case 4:
 						{
 							nextProc = ((Procedure)(argList.next()));
 
 							Number num =  ((Number)argList.next());
 							Object result;
 							int intResult;
 							double doubleResult;
 
 							if (argList.args == null)
 							{
 								if (num instanceof Double)
 									result = new Double(0.0 - num.doubleValue());
 								else
 									result = new Integer(0 - num.intValue());
 							}
 							else if (num instanceof Double)
 							{
 								doubleResult = num.doubleValue();
 								while (argList.args != null)
 									doubleResult -= ((Number)argList.next()).doubleValue();
 								result = new Double(doubleResult);
 							}
 							else
 							{
 								intResult = num.intValue();
 
 								for (;;)
 								{
 									num = (Number)argList.next();
 									if (num instanceof Double) break;
 									intResult -= num.intValue();
 									if (argList.args == null) break;
 								}
 
 								if (num instanceof Double)
 								{
 									doubleResult = intResult - num.doubleValue();;
 									while (argList.args != null)
 										doubleResult -= ((Number)argList.next()).doubleValue();
 									result = new Double(doubleResult);
 								}
 								else
 									result = new Integer(intResult);
 							}
 							argList.setNext(result);
 							continue;
 						}
 					case 5:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Number number;
 							Object result = null;
 							int intResult = 1;
 							if (argList.args == null)
 							{
 								result = new Integer(intResult);
 							}
 							while (argList.args != null)
 							{
 								number = ((Number)argList.next());
 								if (number instanceof Double)
 								{
 									double doubleResult = intResult * number.doubleValue();;
 									while (argList.args != null)
 										doubleResult *= ((Number)argList.next()).doubleValue();
 									result = new Double(doubleResult);
 									break;
 								}
 								intResult *= number.intValue();
 								if (argList.args == null)
 								{
 									result = new Integer(intResult);
 								}
 							}
 							argList.setNext(result);
 							continue;
 						}
 					case 6:
 						{
 							nextProc = ((Procedure)(argList.next()));
 
 							Number num =  ((Number)argList.next());
 							Object result;
 							int intResult;
 							double doubleResult;
 
 							if (argList.args == null)
 							{
 								if (num instanceof Double)
 									result = new Double(1.0 / num.doubleValue());
 								else
 									result = new Integer(1 / num.intValue());
 							}
 							else if (num instanceof Double)
 							{
 								doubleResult = num.doubleValue();
 								while (argList.args != null)
 									doubleResult /= ((Number)argList.next()).doubleValue();
 								result = new Double(doubleResult);
 							}
 							else
 							{
 								intResult = num.intValue();
 
 								for (;;)
 								{
 									num = (Number)argList.next();
 									if (num instanceof Double) break;
 									intResult /= num.intValue();
 									if (argList.args == null) break;
 								}
 
 								if (num instanceof Double)
 								{
 									doubleResult = intResult / num.doubleValue();;
 									while (argList.args != null)
 										doubleResult /= ((Number)argList.next()).doubleValue();
 									result = new Double(doubleResult);
 								}
 								else
 									result = new Integer(intResult);
 							}
 							argList.setNext(result);
 							continue;
 						}
 					case 7:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Object result;
 							result = TRUE;
 							if (argList.args != null)
 							{
 								Number a = (Number)argList.next();
 								boolean aIsD = a instanceof Double;
 
 								while (argList.args != null)
 								{
 									Number b = (Number)argList.next();
 									boolean bIsD = b instanceof Double;
 									if (aIsD || bIsD)
 									{
 										if (!(a.doubleValue() < b.doubleValue()))
 										{
 											result = FALSE;
 											break;
 										}
 									}
 									else
 									{
 										if (!(a.intValue() < b.intValue()))
 										{
 											result = FALSE;
 											break;
 										}
 									}
 									a = b;
 									aIsD = bIsD;
 								}
 							}
 							argList.setNext(result);
 							continue;
 						}
 					case 8:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Object result;
 							result = TRUE;
 							if (argList.args != null)
 							{
 								Number a = (Number)argList.next();
 								boolean aIsD = a instanceof Double;
 
 								while (argList.args != null)
 								{
 									Number b = (Number)argList.next();
 									boolean bIsD = b instanceof Double;
 									if (aIsD || bIsD)
 									{
 										if (!(a.doubleValue() <= b.doubleValue()))
 										{
 											result = FALSE;
 											break;
 										}
 									}
 									else
 									{
 										if (!(a.intValue() <= b.intValue()))
 										{
 											result = FALSE;
 											break;
 										}
 									}
 									a = b;
 									aIsD = bIsD;
 								}
 							}
 							argList.setNext(result);
 							continue;
 						}
 					case 9:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Object result;
 							result = TRUE;
 							if (argList.args != null)
 							{
 								Number a = (Number)argList.next();
 								boolean aIsD = a instanceof Double;
 
 								while (argList.args != null)
 								{
 									Number b = (Number)argList.next();
 									boolean bIsD = b instanceof Double;
 									if (aIsD || bIsD)
 									{
 										if (!(a.doubleValue() == b.doubleValue()))
 										{
 											result = FALSE;
 											break;
 										}
 									}
 									else
 									{
 										if (!(a.intValue() == b.intValue()))
 										{
 											result = FALSE;
 											break;
 										}
 									}
 									a = b;
 									aIsD = bIsD;
 								}
 							}
 							argList.setNext(result);
 							continue;
 						}
 					case 10:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Object result;
 							result = TRUE;
 							if (argList.args != null)
 							{
 								Number a = (Number)argList.next();
 								boolean aIsD = a instanceof Double;
 
 								while (argList.args != null)
 								{
 									Number b = (Number)argList.next();
 									boolean bIsD = b instanceof Double;
 									if (aIsD || bIsD)
 									{
 										if (!(a.doubleValue() >= b.doubleValue()))
 										{
 											result = FALSE;
 											break;
 										}
 									}
 									else
 									{
 										if (!(a.intValue() >= b.intValue()))
 										{
 											result = FALSE;
 											break;
 										}
 									}
 									a = b;
 									aIsD = bIsD;
 								}
 							}
 							argList.setNext(result);
 							continue;
 						}
 					case 11:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Object result;
 							result = TRUE;
 							if (argList.args != null)
 							{
 								Number a = (Number)argList.next();
 								boolean aIsD = a instanceof Double;
 
 								while (argList.args != null)
 								{
 									Number b = (Number)argList.next();
 									boolean bIsD = b instanceof Double;
 									if (aIsD || bIsD)
 									{
 										if (!(a.doubleValue() > b.doubleValue()))
 										{
 											result = FALSE;
 											break;
 										}
 									}
 									else
 									{
 										if (!(a.intValue() > b.intValue()))
 										{
 											result = FALSE;
 											break;
 										}
 									}
 									a = b;
 									aIsD = bIsD;
 								}
 							}
 							argList.setNext(result);
 							continue;
 						}
 					case 12:
 						{
 							Procedure cont = ((Procedure)(argList.next()));
 							nextProc = ((Procedure)(argList.next()));
 							listBuilder.car = cont;
 							listBuilder.cdr = null;
 							lastPair = listBuilder;
 							while (argList.args.cdr != null)
 							{
 								lastPair.cdr = cons(argList.next(), null);
 								lastPair = lastPair.rest();
 							}
 							lastPair.cdr = argList.next();
 							argList.args = listBuilder;
 							argList.immediates = true;
 							continue;
 						}
 					case 13:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							argList.setNext(getClass(argList.next()));
 							continue;
 						}
 					case 14:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Class clas = getClass(argList.next());
 							String name = getString(argList.next());
 							Pair types = (Pair)argList.next();
 							argList.setNext(clas.getMethod(name, (Class[])makeArray(Class.class, types)));
 							continue;
 						}
 					case 15:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							argList.setNext(callMethod(argList));
 							continue;
 						}
 					case 16:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							argList.setNext(callConstructor(argList));
 							continue;
 						}
 					case 17:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Object a = argList.next();
 							Object b = argList.next();
 							argList.setNext((a == b) ? TRUE : FALSE);
 							continue;
 						}
 					case 18:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							argList.setNext(eqv(argList.next(), argList.next()));
 							continue;
 						}
 					case 19:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							argList.setNext(equal(argList.next(), argList.next()));
 							continue;
 						}
 					case 20:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Object tb = argList.next();
 							Object cb = argList.next();
 							Object fb = argList.next();
 							Object result = null;
 							try
 							{
 								result = apply(tb, null);
 							}
 							catch (Exception e)
 							{
 								if (cb != FALSE)
 									result = apply(cb, cons(e, null));
 							}
 							finally
 							{
 								if (fb != FALSE)
 									apply(fb, null);
 							}
 							argList.setNext(result);
 							continue;
 						}
 					case 21:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Object a = argList.next();
 							Object b = argList.next();
 							argList.setNext(cons(a, b));
 							continue;
 						}
 					case 22:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Pair p = (Pair)argList.next();
 							argList.setNext(p.car);
 							continue;
 						}
 					case 23:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Pair p = (Pair)argList.next();
 							argList.setNext(p.cdr);
 							continue;
 						}
 					case 24:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Pair p = (Pair)argList.next();
 							p.car = argList.next();
 							argList.setNext(unspecified);
 							continue;
 						}
 					case 25:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Pair p = (Pair)argList.next();
 							p.cdr = argList.next();
 							argList.setNext(unspecified);
 							continue;
 						}
 					case 26:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Number a = (Number)argList.next();
 							Number b = (Number)argList.next();
 							if (a instanceof Double || b instanceof Double)
 								argList.setNext(new Double(a.doubleValue() % b.doubleValue()));
 							else
 								argList.setNext(new Integer(a.intValue() % b.intValue()));
 							continue;
 						}
 					case 27:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Class clas = getClass(argList.next());
 							Pair elements = (Pair)argList.next();
 							argList.setNext(makeArray(clas, elements));
 							continue;
 						}
 					case 28:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							int length = ((Integer)argList.next()).intValue();
 							char c = argList.args == null ? ' ' : ((Character)argList.next()).charValue();
 							char[] res = new char[length];
 							for (int i=0; i<length; ++i) res[i] = c;
 							argList.setNext(res);
 							continue;
 						}
 					case 29:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							char[] src = (char[])argList.next();
 							int src_idx = ((Integer)argList.next()).intValue();
 							char[] dst = (char[])argList.next();
 							int dst_idx = ((Integer)argList.next()).intValue();
 							int len = ((Integer)argList.next()).intValue();
 							while (len-- > 0)
 								dst[dst_idx++] = src[src_idx++];
 							argList.setNext(unspecified);
 							continue;
 						}
 					case 30:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							argList.setNext(new Character((char)((Integer)argList.next()).intValue()));
 							continue;
 						}
 					case 31:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							argList.setNext(this);
 							continue;
 						}
 					case 32:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							String name = (String)argList.next();
 							Procedure expander = ((Procedure)(argList.next()));
 							r5rs.macros.put(name, expander);
 							argList.setNext(unspecified);
 							continue;
 						}
 					case 33:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Object lock = argList.next();
 							synchronized (lock)
 							{
 								argList.setNext(apply(argList.next(), null));
 							}
 							continue;
 						}
 					case 34:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							continue;
 						}
 					case 35:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							throw (Exception)argList.next();
 						}
 					case 36:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							argList.setNext(listp(argList.next()) ? TRUE : FALSE);
 							continue;
 						}
 					case 37:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							argList.setNext(new Integer(length((Pair)argList.next())));
 							continue;
 						}
 
 					case 38:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Pair list = (Pair)argList.next();
 							int count = ((Integer)argList.next()).intValue();
 							while (count-- > 0)
 								list = list.rest();
 							argList.setNext(list);
 							continue;
 						}
 					case 39:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							Pair list = (Pair)argList.next();
 							Pair reversed = null;
 							while (list != null)
 							{
 								reversed = cons(list.car, reversed);
 								list = list.rest();
 							}
 							argList.setNext(reversed);
 							continue;
 						}
 					case 40:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							listBuilder.cdr = null;
 							lastPair = listBuilder;
 
 							Pair list = (Pair)argList.next();
 							while (argList.args != null)
 							{
 								while (list != null)
 								{
 									lastPair = (Pair)(lastPair.cdr = cons(list.car, null));
 									list = list.rest();
 								}
 								list = (Pair)argList.next();
 							}
 							lastPair.cdr = list;
 							argList.setNext(listBuilder.cdr);
 							continue;
 						}
 
 
 					case 41:
 						{
 							nextProc = ((Procedure)(argList.next()));
 							char op = ((Character)argList.next()).charValue();
 							if (op == '~')
 							{
 								argList.setNext(new Integer(~ ((Integer)argList.next()).intValue()));
 							}
 							else
 							{
 								int a = ((Integer)argList.next()).intValue();
 								int b = ((Integer)argList.next()).intValue();
 								int c = 0;
 								switch (op)
 								{
 									case '&':
 										c = a & b;
 										break;
 									case '|':
 										c = a | b;
 										break;
 									case '^':
 										c = a ^ b;
 										break;
 								}
 								argList.setNext(new Integer(c));
 							}
 							continue;
 						}
 
 				}
 			}
 
 			if (nextProc.env_size < 0)
 			{
 				fillArgs = tmpArgs1;
 				tmpArgs1 = tmpArgs2;
 				tmpArgs2 = fillArgs;
 				nextEnv = nextProc.env;
 			}
 			else
 			{
 				fillArgs = new Object[nextProc.env_size];
 				nextEnv = cons(fillArgs, nextProc.env);
 			}
 
 			int i = 0;
 			for (; i<nextProc.args; ++i)
 			{
 				fillArgs[i] = argList.next();
 			}
 
 			if ((nextProc.flags & HAS_REST) != 0)
 			{
 				listBuilder.cdr = null;
 				lastPair = listBuilder;
 				while (argList.args != null)
 				{
 					lastPair.cdr = cons(argList.next(), null);
 					lastPair = lastPair.rest();
 				}
 				fillArgs[i] = listBuilder.cdr;
 			}
 
 			argList.args = nextProc.mappings;
 			argList.immediates = false;
 			argList.currArgs = fillArgs;
 			argList.currEnv = nextEnv;
 			nextProc = ((Procedure)(argList.next()));
 		}
 
 	}
 
 	public Interpreter() throws Exception
 	{
 		BufferedReader r = new BufferedReader(new InputStreamReader(Interpreter.class.getResourceAsStream("builtins.txt")));
 
 		for (int i=0; ; ++i)
 		{
 			String s = r.readLine();
 			if (s == null) break;
 			s = s.intern();
 
 			Procedure proc = new Procedure(BUILTIN, i, -1, null, s, null);
 
 			r5rs.symbolTable.put(s, cons(r5rs.slots, new Integer(r5rs.slots.size())));
 			r5rs.slots.addElement((Object)proc);
 
 			if (i == 0)
 				identity = proc;
 		}
 
 		envs = cons(cons(cons("r5rs", null), r5rs), envs);
 
 		loadFromJar("detroit/lib/builtins.scm", r5rs);
 		loadFromJar("detroit/lib/init.scm", r5rs);
 	}
 
 	public Object apply(Object proc, Pair args) throws Exception
 	{
 		ArgList argList = new ArgList();
 		argList.immediates = true;
 		argList.args = cons(proc, cons(identity, args));
 		return run(argList);
 	}
 
 	public final Object eval(Object form, Environment env) throws Exception
 	{
 		return eval(identity, form, env);
 	}
 
 	public final Environment getEnv(Object name)
 	{
 		if (name instanceof String)
 			name = cons(name, null);
 		Pair l = envs;
 		while (l != null)
 		{
 			if (equal(car(car(l)), name) == TRUE)
 				return (Environment)cdr(car(l));
 			l = l.rest();
 		}
 		return null;
 	}
 
 	public static int gensymCounter = 0;
 	public static final String gensym(String prefix)
 	{
 		return "__sym_" + prefix + "_" + gensymCounter++;
 	}
 
 	public static final Pair cons(Object car, Object cdr)
 	{
 		return new Pair(car, cdr);
 	}
 
 	public static final Object car(Object pair)
 	{
 		return ((Pair)pair).car;
 	}
 
 	public static final Object cdr(Object pair)
 	{
 		return ((Pair)pair).cdr;
 	}
 
 	public static final boolean isData(Object form)
 	{
 		return (!(form instanceof Pair)) ||
 			car(form) == "quote" ||
 			car(form) == "lambda";
 
 	}
 
 	public static final Pair normDefine(Pair form)
 	{
 		form = form.rest();
 		if (form.car instanceof Pair)
 		{
 			Pair na = (Pair)form.car;
 			return cons(na.car,
 					cons(cons("lambda", cons(na.cdr, form.cdr)),
 						null));
 		}
 		if (form.cdr == null)
 		{
 			return cons(form.car, cons(unspecified, null));
 		}
 		return form;
 	}
 
 	public final Object expandMacros(Object form, Hashtable macros) throws Exception
 	{
 		while (form instanceof Pair && macros.containsKey(car(form)))
 			form = apply(macros.get(car(form)), (Pair)cdr(form));
 		return form;
 	}
 
 	public final Object cps(Object form, Object cont, Hashtable macros) throws Exception
 	{
 		form = expandMacros(form, macros);
 
 		if (isData(form))
 		{
 			return cons(cont, cons(cpsLambda(form, macros), null));
 		}
 
 		Object car = car(form);
 
 		if (car == "begin")
 		{
 			return cpsBegin(gensym("cont"), cdr(form), cont, macros);
 		}
 
 		if (car == "if")
 		{
 			String s = gensym("cont");
 			Object t = cons("lambda", cons(cons(s, null), cons(cps((car(cdr(cdr(form)))), s, macros), null)));
 			Object f = cons("lambda", cons(cons(s, null), cons(cdr(cdr(cdr(form))) == null ? cons(s, cons(cons("quote", cons(unspecified, null)), null)) : cps((car(cdr(cdr(cdr(form))))), s, macros), null)));
 			return cps(car(cdr(form)),
 					cons("lambda", cons(cons(s,null), cons(cons("if", cons(cont, cons(s, cons(t, cons(f, null))))), null))),
 					macros);
 		}
 
 		return cpsCall(form, cdr(form), null, cont, macros);
 	}
 
 	public final Object cpsValue(Object form, Object cont, Hashtable macros) throws Exception
 	{
 		if (isData(form))
 		{
 			return cons(cont, cons(cpsLambda(form, macros), null));
 		}
 		return cps(form, cont, macros);
 	}
 
 	public final Object cpsLambda(Object form, Hashtable macros) throws Exception
 	{
 		if (form instanceof Pair &&
 				((Pair)form).car == "lambda")
 		{
 			String s = gensym("cont");
 			Pair defs = startList();
 			Pair body = (Pair)cdr(cdr(form));
 			while (body != null)
 			{
 				Object expansion = expandMacros(car(body), macros);
 				if (!(expansion instanceof Pair && car(expansion) == "define"))
 				{
 					body = cons(expansion, cdr(body));
 					break;
 				}
 
 				expansion = normDefine((Pair)expansion);
 
 				appendToList(defs, expansion);
 				body = body.rest();
 			}
 			defs = getList(defs);
 
 			if (defs == null)
 				body = cons("begin", body);
 			else
 				body = cons("letrec*", cons(defs, body));
 
 			return cons("lambda", cons(cons(s, car(cdr(form))), cons(cps(body, s, macros), null)));
 		}
 		return form;
 	}
 
 	public final Object cpsBegin(String s, Object form, Object cont, Hashtable macros) throws Exception
 	{
 		if (cdr(form) == null)
 			return cpsValue(car(form), cont, macros);
 		return cps(car(form),
 				cons("lambda", cons(cons(s, null), cons(cpsBegin(s, cdr(form), cont, macros), null))),
 				macros);
 	}
 
 	public final Object cpsCall(Object form, Object args, Object acc, Object cont, Hashtable macros) throws Exception
 	{
 		if (args == null)
 		{
 			Pair head = (Pair)acc;
 			Pair lastPair = null;
 			while (head != null)
 			{
 				Pair rest = head.rest();
 				head.cdr = lastPair;
 				lastPair = head;
 				head = rest;
 			}
 			acc = lastPair;
 
 			if (isData(car(form)))
 			{
 				if (car(form) == "set!")
 				{
 					acc = cons(cons(setSlot, car(acc)), cdr(acc));
 				}
 
 				return cons(cpsLambda(car(form), macros), cons(cont, acc));
 			}
 
 			String s = gensym("cont");
 			return cps(car(form),
 					cons("lambda", cons(cons(s, null), cons(cons(s, cons(cont, acc)), null))),
 					macros);
 		}
 
 		if (isData(car(args)))
 		{
 			return cpsCall(form, cdr(args), cons(cpsLambda(car(args), macros), acc), cont, macros);
 		}
 
 		String s = gensym("cont");
 		return cps(car(args),
 				cons("lambda", cons(cons(s, null), cons(cpsCall(form, cdr(args), cons(s, acc), cont, macros), null))),
 				macros);
 	}
 
 	public static final boolean canSkipEnv(Object form, Hashtable argNames, int depth) throws Exception
 	{
 		if (argNames.size() > 8) return false;
 
 		if (form instanceof Pair)
 		{
 			if (car(form) == "quote")
 				return true;
 
 			if (car(form) == "lambda")
 			{
 				Hashtable unshadowed = (Hashtable)argNames.clone();
 				Object args = car(cdr(form));
 				while (args instanceof Pair)
 				{
 					if (unshadowed.containsKey(car(args)))
 						unshadowed.remove(car(args));
 					args = cdr(args);
 				}
 				if (args != null)
 					if (unshadowed.containsKey(args))
 						unshadowed.remove(args);
 
 				return canSkipEnv(cdr(cdr(form)), unshadowed, depth+1);
 			}
 
 			return canSkipEnv(car(form), argNames, depth + 1) && canSkipEnv(cdr(form), argNames, depth);
 
 		}
 
 		if (form == null) return true;
 
 		return depth < 2 || !argNames.containsKey(form);
 	}
 
 	public final Op compile(Object form, Pair args, Pair currEnv, Environment env) throws Exception
 	{
 		if (form instanceof String)
 		{
 			if (args != null)
 			{
 				int along = 0;
 				while (args != null)
 				{
 					if (car(args.car) == form)
 						return new Op(OP_ARG, along, 0, null, null, null);
 
 					++along;
 					args = args.rest();
 				}
 			}
 
 			int up = 0;
 			Pair scanEnv = currEnv;
 			while (scanEnv != null)
 			{
 				Pair currEnvSlots = (Pair)scanEnv.car;
 				int along = 0;
 				while (currEnvSlots != null)
 				{
 					if (car(currEnvSlots.car) == form)
 					{
 						if (up > 0)
 						{
 							((Pair)currEnvSlots.car).cdr = TRUE;
 						}
 						return new Op(OP_ENV, up, along, null, null, null);
 					}
 
 					++along;
 					currEnvSlots = currEnvSlots.rest();
 				}
 
 				++up;
 				scanEnv = scanEnv.rest();
 			}
 
 			if (!env.symbolTable.containsKey(form))
 			{
 				env.symbolTable.put(form, cons(env.slots, new Integer(env.slots.size())));
 				env.slots.addElement(unspecified);
 			}
 			Pair slotInfo = (Pair)env.symbolTable.get(form);
 			return new Op(OP_LIB, ((Integer)slotInfo.cdr).intValue(), 0, (Vector)slotInfo.car, null, null);
 		}
 
 		if (form instanceof Pair && car(form) == "lambda")
 		{
 			int numArgs = 0;
 			int flags = 0;
 			Object argForm = car(cdr(form));
 			Object body = car(cdr(cdr(form)));
 
 			args = null;
 			Pair lastPair = null;
 			Hashtable argNames = new Hashtable();
 
 			while (argForm instanceof Pair)
 			{
 				argNames.put(car(argForm), FALSE);
 				if (lastPair == null)
 					args = lastPair = cons(cons(car(argForm), FALSE), null);
 				else
 				{
 					lastPair.cdr = cons(cons(car(argForm), FALSE), null);
 					lastPair = lastPair.rest();
 				}
 				argForm = cdr(argForm);
 				++numArgs;
 			}
 
 			int currEnvSize = numArgs;
 
 			if (argForm != null)
 			{
 				flags |= HAS_REST;
 				argNames.put(argForm, FALSE);
 
 				if (lastPair == null)
 					args = lastPair = cons(cons(argForm, FALSE), null);
 				else
 				{
 					lastPair.cdr = cons(cons(argForm, FALSE), null);
 					lastPair = lastPair.rest();
 				}
 				++currEnvSize;
 			}
 
 			if (currEnvSize < 8 && canSkipEnv(body, argNames, 0))
 			{
 				currEnvSize = -1;
 			}
 			else
 			{
 				currEnv = cons(args, currEnv);
 				args = null;
 			}
 
 			Pair ops = cons(null, null);
 			lastPair = ops;
 			while (body != null)
 			{
 				lastPair.cdr = cons(compile(car(body), args, currEnv, env), null);
 				body = cdr(body);
 				lastPair = lastPair.rest();
 			}
 			ops = ops.rest();
 
 			return new Op(OP_LMBD,
 					0,
 					0,
 					null,
 					new Procedure(flags, numArgs, currEnvSize, null,
 						"<lambda>",
 						ops),
 					null);
 		}
 
 		if (form instanceof Pair && car(form) == setSlot)
 		{
 			return new Op(OP_LIT, 0, 0, null, null, compile(cdr(form), null, currEnv, env));
 		}
 
 		if (form instanceof Pair && car(form) == "quote")
 			return new Op(OP_LIT, 0, 0, null, null, car(cdr(form)));
 
 		return new Op(OP_LIT, 0, 0, null, null, form);
 	}
 
 	public final Object eval(Object cont, Object form, Environment env) throws Exception
 	{
 
 		if (form instanceof Pair)
 			form = expandMacros(form, env.macros);
 
 		if (form instanceof Pair &&
 				car(form) == "begin")
 		{
 			form = cdr(form);
 			while (cdr(form) != null)
 			{
 				eval(car(form), env);
 				form = cdr(form);
 			}
 			return eval(car(form), env);
 		}
 
 		if (form instanceof Pair &&
 				car(form) == "define")
 		{
 			Pair norm = normDefine((Pair)form);
 			if (!env.symbolTable.containsKey(norm.car))
 			{
 				env.symbolTable.put(norm.car, cons(env.slots, new Integer(env.slots.size())));
 				env.slots.addElement(unspecified);
 			}
 			form = cons("set!", norm);
 		}
 
 		Object cps = cont == null ? form : cps(form, cont, env.macros);
 
 		Pair ops = cons(null, null);
 		Pair lastPair = ops;
 		Pair body = (Pair)cps;
 		while (body != null)
 		{
 			lastPair.cdr = cons(compile(body.car, null, null, env), null);
 			body = body.rest();
 			lastPair = lastPair.rest();
 		}
 		ops = ops.rest();
 
 		ArgList argList = new ArgList();
 		argList.args = ops;
 
 		try {
 			return run(argList);
 		} catch (Exception e) {
			throw new Exception(String.format(";[unbound symbol][%s]",((Pair)form).car));
 		}
 	}
 
 	public final void load(java.io.Reader inp, Environment env)
 	{
 		try
 		{
 			Reader r = new Reader(inp);
 			for (;;)
 				eval(r.read(), env);
 		}
 		catch (IOException e)
 		{
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public final void loadFromJar(String filename, Environment env) throws Exception
 	{
 		InputStream stream = Interpreter.class.getResourceAsStream("/" + filename);
 
 		if (stream == null)
 			load(new FileReader(filename), env);
 		else
 			load(new InputStreamReader(stream), env);
 	}
 
 	public static boolean loadJar(String jar) throws Exception
 	{
 		try
 		{
 			java.net.URLClassLoader sysLoader = (java.net.URLClassLoader)java.lang.ClassLoader.getSystemClassLoader();
 			java.net.URL u = ((new java.io.File(jar)).toURI()).toURL();
 			Class sysclass = java.net.URLClassLoader.class;
 			java.lang.reflect.Method method = (java.lang.reflect.Method) sysclass.getDeclaredMethod("addURL", new Class[] {java.net.URL.class});
 			method.setAccessible(true);
 			method.invoke(sysLoader,new Object[] {u});
 			return true;
 		}
 		catch (Exception e)
 		{
 			throw e;
 		}
 	}
 
 }
