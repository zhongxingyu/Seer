 /**
  * Jason Zerbe
  * Copyright 2012
  * Build-Id: EMPTY
  *
  * Template5Z main class for exposing library methods to public
  */
 package com.vraidsys.template5z;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author jzerbe
  */
 public class Template5Z {
 
     private static final String kTagStart = "${";
     private static final String kTagEnd = "}";
     private Map<String, String> myReplaceMap = null;
     private String myTextData = null;
 
     public Template5Z(final Map<String, String> theReplaceMap) {
         this(theReplaceMap, null);
     }
 
     public Template5Z(final Map<String, String> theReplaceMap,
             final String theTextData) {
         myReplaceMap = theReplaceMap;
         myTextData = theTextData;
     }
 
     public void loadText(final String theTextData) {
         myTextData = theTextData;
     }
 
     public String renderTextWithTemplate() {
         return renderTextWithTemplate(null);
     }
 
     public String renderTextWithTemplate(List<String> theErrorOutList) {
         if (myTextData == null) {
             return null;
         }
 
        String aWorkingTextData = myTextData; //immutable in any case
         for (String aReplaceKey : myReplaceMap.keySet()) {
             String aSearchKey = kTagStart + aReplaceKey + kTagEnd;
             String aReplaceValue = myReplaceMap.get(aReplaceKey);
             aWorkingTextData = aWorkingTextData.replace(aSearchKey, aReplaceValue);
         }
 
         return aWorkingTextData;
     }
 }
