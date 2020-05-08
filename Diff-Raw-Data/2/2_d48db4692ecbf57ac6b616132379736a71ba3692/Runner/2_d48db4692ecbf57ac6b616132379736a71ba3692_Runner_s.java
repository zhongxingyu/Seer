 package com.epam.kharkiv.cdp.oleshchuk.calc;
 
 
 import com.epam.kharkiv.cdp.oleshchuk.calc.algorithm.ShuntingYard;
 import com.epam.kharkiv.cdp.oleshchuk.calc.util.StringUtil;
 
 import java.math.BigInteger;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Runner {
 
     public static void main(String[] args) {
         try {
             final String input = System.getenv("input");
            String prepareString = StringUtil.prepareInputString("sin(90)");
             String result = ShuntingYard.calculate(prepareString);
             System.out.println(result);
         } catch (Exception e) {
             System.out.println("FAIL");
         }
     }
 
 
 }
