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
 
 package org.webmacro.servlet;
 
 import org.webmacro.Context;
 import org.webmacro.ContextTool;
 import org.webmacro.PropertyException;
 
 /**
  * A ContextTool for performing the more useful methods from
  * <code>java.lang.Math</code>.
  *
  * @author Eric B. Ridge (mailto:ebr@tcdi.com)
  */
 
 public class MathTool implements ContextTool {
 
    /** our lonely singleton */
    private static final MathTool _instance = new MathTool();
 
 
    /**
     * @return the static instance of the MathTool
     */
    public static final MathTool getInstance() {
       return _instance;
    }
 
 
    //
    // static MathTool methods and fields
    //
 
    /** the value of PI, as defined by <code>java.lang.Math.PI</code> */
    public static final double PI = java.lang.Math.PI;
 
    /**
     * @return the smaller of the specified number
     */
    public static final int min(int a, int b) {
       return (a < b)?a:b;
    }
 
    public static final long min(long a, long b) {
       return (a < b)?a:b;
    }
 
    public static final float min(float a, float b) {
       return (a < b)?a:b;
    }
 
    public static final double min(double a, double b) {
       return (a < b)?a:b;
    }
 
 
    /**
     * @return the larger of the specified number
     */
    public static final int max(int a, int b) {
       return (a > b)?a:b;
    }
 
    public static final long max(long a, long b) {
       return (a > b)?a:b;
    }
 
    public static final float max(float a, float b) {
       return (a > b)?a:b;
    }
 
    public static final double max(double a, double b) {
       return (a > b)?a:b;
    }
 
    /**
     * Creates a pseudo-random Integer between <code>start</code>
     * and <code>end</code>, inclusive
     */
    public static final int random(int start, int end) {
      return start + (int)((end - start + 1) * java.lang.Math.random());
    }
 
    /**
     * @return <code>base</code> raised to the specified <code>power</code>
     */
    public static final int pow(int base, int power) {
       return base ^ power;
    }
 
    public static final long pow(long base, long power) {
       return base ^ power;
    }
 
 
    /**
     * @return the absolute value of the specified number
     */
    public static final int abs(int a) {
       return java.lang.Math.abs(a);
    }
 
    public static final long abs(long a) {
       return java.lang.Math.abs(a);
    }
 
    public static final float abs(float a) {
       return java.lang.Math.abs(a);
    }
 
    public static final double abs(double a) {
       return java.lang.Math.abs(a);
    }
 
 
    /**
     * @return <code>a</code> modulo <code>b</code>
     */
    public static final int mod(int a, int b) {
       return a % b;
    }
 
    /**
     * Converts args to doubles and multiplies them together.
     * @return <code>a * b</code>
     */
    public static final double multiply(Number a, Number b){
       return a.doubleValue() * b.doubleValue();
    }
    
    /**
     * Converts args to doubles and divides the first by the second.
     * @return <code>a / b</code>
     */
    public static final double divide(Number a, Number b){
       return a.doubleValue() / b.doubleValue();
    }
 
 
    //
    // ContextTool implementation methods
    //
 
    /** default contsructor.  Does nothing */
    public MathTool() {
    }
 
    /**
     * public constructor.  Does nothing.  The MathTool doesn't
     * interact with the Context, so it is ignored
     */
    public MathTool(Context context) {
    }
 
    /**
     * Tool initialization method.  The MathTool doesn't
     * interact with the context, so the <code>context</code>
     * parameter is ignored.
     */
    public Object init(Context context) throws PropertyException {
       return MathTool.getInstance();
    }
 
    /**
     * Perform necessary cleanup work
     */
    public void destroy(Object o) {
    }
   
   public static void main(String[] args){
      System.out.println("Generating 200 random ints between 10 and 99 inclusive:");
      for (int i=0; i<200; i++){
         System.out.print(MathTool.random(10, 99) + " ");
         if (((i + 1) % 20) == 0) System.out.println();
      }
      System.out.println("\nDone.");
   }
 }
