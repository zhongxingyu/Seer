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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
 /**
  * @author jzerbe
  */
 public class Template5Z {
 
     private static final String kTagStart = "${";
     private static final String kTagEnd = "}";
    private static final Pattern myTagPattern = Pattern.compile("\\Q${\\E[\\w]+\\}");
    private static final String kMissingMsg = "variable is uninitialized";
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
 
        String aWorkingTextData = myTextData; //immutable, no deep copy needed

        // perform replacement in text for each variable in map
         for (String aReplaceKey : myReplaceMap.keySet()) {
             String aSearchKey = kTagStart + aReplaceKey + kTagEnd;
             String aReplaceValue = myReplaceMap.get(aReplaceKey);
             aWorkingTextData = aWorkingTextData.replace(aSearchKey, aReplaceValue);
         }
 
        // detect any unitialized variables
        String[] aWordArray = aWorkingTextData.split(" ");
        for (String aWord : aWordArray) {
            Matcher aMatcher = myTagPattern.matcher(aWord);
            if (aMatcher.find()) {
                String aMatchedText = aMatcher.group();
                String aPossibleUnInitKey = aMatchedText.substring(
                        kTagStart.length(), (aMatchedText.length() - kTagEnd.length()));
                if (!myReplaceMap.values().contains(aPossibleUnInitKey)) {
                    theErrorOutList.add(kTagStart + aPossibleUnInitKey + kTagEnd + kMissingMsg);
                }
            }
        }

         return aWorkingTextData;
     }
 }
