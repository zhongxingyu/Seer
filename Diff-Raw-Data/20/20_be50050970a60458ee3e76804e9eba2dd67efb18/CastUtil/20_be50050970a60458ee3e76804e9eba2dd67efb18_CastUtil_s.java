 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)
  *
  * This software is provided "as is", with NO WARRANTY, not even the
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.
  */
 package org.webmacro.util;
 
 /**
  *
  * @author  keats_kirsch
  * @since 0.96
  */
 public class CastUtil extends java.lang.Object {
 
    static private CastUtil _singleton = new CastUtil();
 
    /** private constructor for singleton */
    private CastUtil() {
    }
 
    static public CastUtil getInstance() {
       return _singleton;
    }
 
    public static Boolean toBoolean(java.lang.Object o) {
       if (o == null) return Boolean.FALSE;
       return Boolean.valueOf(o.toString());
    }
 
    public static Character toChar(Object o) throws org.webmacro.PropertyException {
       Character c = null;
       if (o == null || o == "") return null;
       if (o instanceof Character) return (Character) o;
       if (o instanceof Number) {
          try {
             c = new Character((char) ((Number) o).intValue());
          }
          catch (Exception e) {
            throw new IllegalArgumentException("Not a valid char: " + o);
          }
       }
       else if (o instanceof String) {
          String s = (String) o;
          if (s.startsWith("\\u") && s.length() == 6) {
             // unicode char
             int i = Integer.parseInt(s.substring(2), 16);
             c = new Character((char) i);
          }
          else { // use the first character of the string
             char ch = s.charAt(0);
             c = new Character(ch);
          }
       }
       else { // throw exception
          throw new org.webmacro.PropertyException(
                "$Type.toChar() is unable to convert the supplied value to a Character.  The argument"
                + " must be a number or a String.  The supplied argument was "
                + o + " of type " + o.getClass().getName());
       }
       return c;
    }
 
    public static Byte toByte(java.lang.Object o) {
       if (o instanceof Byte) return (Byte) o;
       Byte v = null;
       try {
          int i = Integer.parseInt(o.toString());
          v = new Byte((byte) i);
       }
       catch (Exception e) {
         throw new IllegalArgumentException("Not a valid byte: " + o);
       }
       return v;
    }
 
    public static Short toShort(java.lang.Object o) {
       if (o instanceof Short) return (Short) o;
       Short v = null;
       try {
          int i = Integer.parseInt(o.toString());
          v = new Short((short) i);
       }
       catch (Exception e) {
         throw new IllegalArgumentException("Not a valid short: " + o);
       }
       return v;
    }
 
    public static Integer toInt(java.lang.Object o) {
       Integer i = null;
       if (o instanceof Integer) return (Integer) o;
       try {
          i = new Integer(o.toString());
       }
       catch (Exception e) {
         throw new IllegalArgumentException("Not a valid int: " + o);
       }
       return i;
    }
 
    public static Long toLong(java.lang.Object o) {
       Long l = null;
       if (o instanceof Long) return (Long) o;
       try {
          l = new Long(o.toString());
       }
       catch (Exception e) {
         throw new IllegalArgumentException("Not a valid long: " + o);
       }
       return l;
    }
 
    public static Float toFloat(java.lang.Object o) {
       Float f = null;
       if (o instanceof Float) return (Float) o;
       try {
          f = new Float(o.toString());
       }
       catch (Exception e) {
         throw new IllegalArgumentException("Not a valid float: " + o);
       }
       return f;
    }
 
    public static Double toDouble(java.lang.Object o) {
       Double d = null;
       if (o instanceof Double) return (Double) o;
       try {
          d = new Double(o.toString());
       }
       catch (Exception e) {
         throw new IllegalArgumentException("Not a valid double: " + o);
       }
       return d;
    }
 
    static public void main(String[] args) {
       CastUtil cu = CastUtil.getInstance();
       println("77"
               + test(BOOLEAN, "77")
               + test(CHAR, "77")
               + test(BYTE, "77")
               + test(SHORT, "77")
               + test(INTEGER, "77")
               + test(LONG, "77")
               + test(FLOAT, "77")
               + test(DOUBLE, "77")
               + ".");
       println("true: " + test(BOOLEAN, "true") + ".");
       println("FalSE: " + test(BOOLEAN, "FalSE") + ".");
       println("null: " + test(BOOLEAN, null) + ".");
    }
 
    static private void println(String s) {
       System.out.println(s);
    }
 
    static private void print(Object o) {
       System.out.print(o);
    }
 
    final static public int BOOLEAN = 1;
    final static public int CHAR = 2;
    final static public int BYTE = 3;
    final static public int SHORT = 4;
    final static public int INTEGER = 5;
    final static public int LONG = 6;
    final static public int FLOAT = 7;
    final static public int DOUBLE = 8;
 
    static private String test(int type, Object val) {
       String s = null;
       try {
          Object o = null;
          switch (type) {
             case BOOLEAN:
                o = toBoolean(val);
                break;
             case CHAR:
                o = toChar(val);
                break;
             case BYTE:
                o = toByte(val);
                break;
             case SHORT:
                o = toShort(val);
                break;
             case INTEGER:
                o = toInt(val);
                break;
             case LONG:
                o = toLong(val);
                break;
             case FLOAT:
                o = toFloat(val);
                break;
             case DOUBLE:
                o = toDouble(val);
                break;
             default:
                throw new IllegalArgumentException("Unknown type: " + type);
          }
          s = ", " + o + " (" + o.getClass().getName() + ")";
       }
       catch (Exception e) {
          e.printStackTrace();
       }
       return s;
    }
 
 }
