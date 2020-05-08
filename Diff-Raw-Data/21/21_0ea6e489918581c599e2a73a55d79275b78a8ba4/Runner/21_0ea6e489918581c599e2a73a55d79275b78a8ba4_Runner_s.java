 package com.epam.kharkiv.cdp.oleshchuk.calc;
 
 
 import com.epam.kharkiv.cdp.oleshchuk.calc.algorithm.ShuntingYard;
 import com.epam.kharkiv.cdp.oleshchuk.calc.util.StringUtil;
 
 public class Runner {
 
     public static void main(String[] args) {
         try{
        String inputString = StringUtil.prepareInputString(args[0]);
        Double result = ShuntingYard.calculate(inputString);
         System.out.println(result);
         } catch (Exception e) {
             System.out.println("FAIL");
         }
     }
 
 }
