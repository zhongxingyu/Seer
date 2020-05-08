 package com.eagle.util;
 
 import javax.swing.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: user
  * Date: 27/10/12
  * Time: 2:07 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CurrencyConverter {
 
     public  static float convertRsToUsd(float rs, float rsPerUsd){
        return rsPerUsd != 0.0f  ? rs / rsPerUsd : 0.0f;
     }
     public static float convertUsdToRs(float usd, float rsPerUsd){
         return usd * rsPerUsd;
     }
 }
