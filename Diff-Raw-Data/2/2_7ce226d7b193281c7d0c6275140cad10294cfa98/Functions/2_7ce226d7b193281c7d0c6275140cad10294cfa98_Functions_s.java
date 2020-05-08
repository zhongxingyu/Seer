 package com.rburgos.skweekparser.functions;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 public enum Functions implements FunctionInterface
 {
     ADD("+")
     {
         @Override
         public String eval(String x, String y)
         {
             return String.valueOf((Double.parseDouble(x) + 
                     Double.parseDouble(y)));
         }
 
         @Override
         public double eval(double x, double y)
         {
             return x + y;
         }
     },
     SUB("-")
     {
         @Override
         public String eval(String x, String y)
         {
             return String.valueOf((Double.parseDouble(y) - 
                     Double.parseDouble(x)));
         }
 
         @Override
         public double eval(double x, double y)
         {
            return x - y;
         }
     },
     MULT("*")
     {
         @Override
         public String eval(String x, String y)
         {
             return String.valueOf((Double.parseDouble(x) * 
                     Double.parseDouble(y)));
         }
 
         @Override
         public double eval(double x, double y)
         {
             return x * y;
         }
     },
     DIV("/")
     {
         @Override
         public String eval(String x, String y)
         {
             return String.valueOf((Double.parseDouble(y) / 
                     Double.parseDouble(x)));
         }
 
         @Override
         public double eval(double x, double y)
         {
             return y / x;
         }
     },
     MOD("%")
     {
         @Override
         public String eval(String x, String y)
         {
             return String.valueOf((Double.parseDouble(y) % 
                     Double.parseDouble(x)));
         }
 
         @Override
         public double eval(double x, double y)
         {
             return y % x;
         }
     },
     POW("^")
     {
         @Override
         public String eval(String x, String y)
         {
             return String.valueOf(Math.pow(Double.parseDouble(y), 
                     Double.parseDouble(x)));
         }
 
         @Override
         public double eval(double x, double y)
         {
             return Math.pow(y, x);
         }
     }, 
     LSHIFT("<<")
     {
         @Override
         public String eval(String x, String y)
         {
             double a = Double.parseDouble(x);
             double b = Double.parseDouble(y);
             return String.valueOf(((int)b << (int)a));
         }
 
         @Override
         public double eval(double x, double y)
         {
             return (int)x << (int)y;
         }
     },
     RSHIFT(">>")
     {
         @Override
         public String eval(String x, String y)
         {
             double a = Double.parseDouble(x);
             double b = Double.parseDouble(y);
             return String.valueOf(((int)b >> (int)a));
         }
 
         @Override
         public double eval(double x, double y)
         {
             return (int)y >> (int)x;
         }
     },
     OR("|")
     {
         @Override
         public String eval(String x, String y)
         {
             double a = Double.parseDouble(x);
             double b = Double.parseDouble(y);
             return String.valueOf(((int)b | (int)a));
         }
 
         @Override
         public double eval(double x, double y)
         {
             return (int)y | (int)x;
         }
     },
     AND("&")
     {
         @Override
         public String eval(String x, String y)
         {
             double a = Double.parseDouble(x);
             double b = Double.parseDouble(y);
             return String.valueOf(((int)b & (int)a));
         }
 
         @Override
         public double eval(double x, double y)
         {
             return (int)y & (int)x;
         }
     };
     
     private String operator;
     private static final Map<String, Functions> opMap = new HashMap<>();
             
     static
     {
         for (Functions f : values())
         {
             opMap.put(f.toString(), f);
         }
     }
     
     private Functions(String operator)
     {
         this.operator = operator;
     }
 
     @Override
     public String toString()
     {
         return this.operator;
     }
     
     public static Functions getFunction(String op)
     {
         return opMap.get(op);
     }
     
     public static boolean contains(String op)
     {
         return opMap.containsKey(op);
     }
 }
