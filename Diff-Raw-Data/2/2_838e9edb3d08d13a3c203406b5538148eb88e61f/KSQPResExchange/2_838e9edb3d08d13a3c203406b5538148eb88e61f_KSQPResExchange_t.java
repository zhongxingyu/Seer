 // exchange for the Kansas QSO Party (KS residents)
 package com.loukides.jl.contests;
 import com.loukides.jl.jl.*;
 import com.loukides.jl.util.*;
 
 import java.util.*;
 import java.io.*;
 import java.text.*;
 
 public class KSQPResExchange extends AbstractExchange {
 
   protected String report = "";
   protected String county = "";
   protected String state = ""; 
 
   protected static HashMap countyMap = new HashMap(105);
 
   boolean sticky = false;
 
   public String getGUIExchange() {
     return report + " " + getMultiplierField();
   }
 
   public String getCabrilloExchange() {
     String call = callsign.getCallsign();
     return U.trunc(call, 13) + U.findPad( call, 13 ) + " "
          + U.trunc(report, 3) + U.findPad( report, 3) + " "
         + U.trunc(getMultiplierField(), 4) + U.findPad(getMultiplierField(), 5);
     // NOTE added an extra space to the last findPad
   }
 
 
   // the heart of the class; figure out what we've been given
   // INTERPRETS:  
   public void addToExchange( String s) {
     int alphas = 0;
     int nums = 0; 
     String countyInfo;
     StringTokenizer input = new StringTokenizer(s.toUpperCase());
     while ( input.hasMoreTokens() ) {
       String tok = input.nextToken();
       char [] cs = tok.toCharArray();
       int len = tok.length();
       for (int i = 0 ; i < cs.length; i++ ) {
         if ( Character.isDigit(cs[i]) ) nums++;
         if ( Character.isLetter(cs[i]) ) alphas++;
       }
       if ( nums == len ) report = tok;
       else if (alphas == len && (len == 2 || tok.equalsIgnoreCase("MAR"))) {
         state = tok;
         county = "";
       }
       else if ( tok.equals("\'\'")) county = "";
       else if (alphas == len && (len == 3) && (! tok.equalsIgnoreCase("MAR"))) {
         state = "KS";        
         county = tok;
       }
       // AT LEAST 2 alphas in a US callsign
       else if ( nums >= 1 && alphas >=2 ) {
         callsign = new Callsign(tok);
       }
       nums = 0; alphas = 0;
     }
     // assign a report, if there isn't a mode-appropriate report already   
     if ( (! (report.length() == 2)) && mode.equals("PH")) report = "59";
     else if ( (! (report.length() == 3)) && (mode.equals("CW") || mode.equals("RY")))  report = "599";
   }
 
   // We need some way to create the "sent" half of the two-way 
   // exchange that concentrates all contest-specific knowledge
   // in this object.  
   public void addToExchange(Properties p, LogEntry le) {
     this.addToExchange(
         p.getProperty("callsign") + " " + p.getProperty("countyAbbrev"));
   }
 
   //  public void parseLoggedExchange( String s) 
   // because of interactions between the county and state fields, 
   // we want this passed through the exchange parser, which is the default
   // implementation
 
   public boolean isComplete() { 
     if ( callsign != Callsign.NOCALL ) {
       if ( state != "" ) return true;
     }
     return false;
   }
 
   public String getMultiplierField() { 
     if (state.equals("KS")) return county;
     return state;
   }
 
   public String getRoverField() { 
     return county;
   }
 
 }
