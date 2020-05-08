 /*
  * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
  *
  * Use is subject to the terms of the TIBCO license terms accompanying the download of this code.
  * In most instances, the license terms are contained in a file named license.txt.
  */
 package org.fabrican.extension.variable.provider.rules;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Rule implements Comparable<Rule>, Cloneable {
     private String pKey;
     private String sKey;
     private String vName;
     private String vValue;
     private static final String STAR = "*";
     public String getPKey() {
         return pKey;
     }
     public void setPKey(String pKey) {
         this.pKey = pKey.trim();
     }
     public String getSKey() {
         return sKey;
     }
     public void setSKey(String sKey) {
         this.sKey = sKey.trim();
     }
     public String getVName() {
         return vName;
     }
     public void setVName(String vName) {
         this.vName = vName.trim();
     }
     public String getVValue() {
         return vValue;
     }
     public void setVValue(String vValue) {
         this.vValue = vValue.trim();
     }
     public boolean evaluate(String pKey, String sKey, boolean ignoreCase){
         return match(pKey, getPKey(), ignoreCase) && match(sKey, getSKey(), ignoreCase);
     }
     private static Pattern ptnNumbers = Pattern.compile("-?\\s*\\d*[.]?\\d*"); 
     private static Pattern ptnRange = Pattern.compile("[\\(\\[]\\s*(-?\\s*\\d*[.]?\\d*)?\\s*(,)\\s*(-?\\s*\\d*[.]?\\d*)?\\s*[\\)\\]]");
     private static boolean match(String key, String keyRule, boolean ignoreCase) {
         String keyRuleStr = keyRule;
         String keyStr = key;
         if  ( ignoreCase ){
             keyRuleStr = keyRule.toLowerCase();
             keyStr = key.toLowerCase();
         }
         key=key.trim();
         if ( keyRule.equals(STAR) ){
             return true;
         } else if (keyStr.contains(keyRuleStr) ){
             return true;
         } else {
             Matcher m = ptnNumbers.matcher(key);
            if ( m.matches() && key.trim().length() > 0 ){
                 double v = Double.parseDouble(key);
                 if ( checkRange(ptnRange, v, keyRule) ){
                     return true;
                 }
             }
         }
         return false;
     }
     
     private static boolean checkRange(Pattern p, double v, String keyRule ){
         keyRule = keyRule.trim();
         Matcher m = p.matcher(keyRule);
         String g1, g2;
         if ( m.matches() ){
             boolean includeL = keyRule.charAt(0) == '[';
             boolean includeR = keyRule.endsWith("]");
             switch (m.groupCount()){
                 case 1: //[,]
                     return true;
                 case 2: //[, 1] or [1, ]
                     g1 = m.group(1);
                     g2 = m.group(2);
                     if ( g1.equals(",") ){
                         if ( includeL  ){
                             return v <= Double.parseDouble(g2);
                         } else {
                             return v < Double.parseDouble(g2);
                         }
                     } else {
                         if ( includeR ){
                             return v >= Double.parseDouble(g1);
                         } else {
                             return v > Double.parseDouble(g1);
                         }
 
                     }
                 case 3:
                     g1 = m.group(1);
                     g2 = m.group(3);
                     return (g2.equals("") || ( includeR ? v <= Double.parseDouble(g2) : v < Double.parseDouble(g2) )) && 
                             (g1.equals("") || (includeL? v >= Double.parseDouble(g1) : v > Double.parseDouble(g1) ));
                 default:
                     return false;
             }
         } else {
             return false;
         }
     }
     @Override
     public int compareTo(Rule o) {
         return getWeight() - o.getWeight();
     }
     
     protected int getWeight(){
         int s = getPKey().equals(STAR) ? 0 : 2;
         s += getSKey().equals(STAR) ? 0 : 1;
         return s;
     }
     /** used by cache **/
     public Rule deepCopy() {
         Rule rst = new Rule();
         rst.setPKey(getPKey());
         rst.setSKey(getSKey());
         rst.setVName(getVName());
         rst.setVValue(getVValue());
         return rst;
     }
 
 }
