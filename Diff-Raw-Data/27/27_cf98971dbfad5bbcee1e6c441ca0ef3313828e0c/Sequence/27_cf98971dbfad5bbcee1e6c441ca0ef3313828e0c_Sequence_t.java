 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 package model;
 
 import controller.PrimerDesign;
 import java.util.Scanner;
 import java.io.*;
 /**
  *
  * @author 1002852m
  * @author 1002858t
  */
 public class Sequence {
 
     private String oStrand; //user strand
     private String cStrand; //generated strand
     private Primer fPrimer; //forward primer
     private Primer rPrimer; //reverse primer
     
     public Sequence(String o) {
         oStrand = parser(new Scanner(o));
         cStrand = genStrand();
         fPrimer = null;
         rPrimer = null;
     }
     public Sequence(String o, String c) {
         oStrand = o;
         cStrand = c;
     }
     public Sequence(String o, String c, Primer f, Primer r) {
     	oStrand = o;
     	cStrand = c;
     	fPrimer = f;
     	rPrimer = r;
     }
     public Sequence() {
         oStrand = null;
         cStrand = null;
         fPrimer = null;
         rPrimer = null;
     }
    
     public String getOStrand() {
         return oStrand;
     }
     public String getCStrand() {
         return cStrand;
     }
     public Primer getFPrimer() {
     	return fPrimer;
     }
     public Primer getRPrimer() {
     	return rPrimer;
     }
     public void setOStrand(String o) {
         oStrand = o;
     }
     public void setCStrand(String c) {
         cStrand = c;
     }
     public void setFPrimer(Primer f) {
     	fPrimer = f;
     }
     public void setRPrimer(Primer r) {
     	rPrimer = r;
     }
     
     public int length() {
         return oStrand.length();
     }
     
     public boolean containsN(int s, int e) {
            String oString = oStrand.substring(s-1, e-1);
            String cString = cStrand.substring(s-1, e-1);
             return (oString.contains("n") || cString.contains("n"));
     }
     
     public static String parser(Scanner input) {
     	/* 
     	 * Takes in a string (should be from NCBI) and turns it
     	 * into one long string of bases that can be better formatted
     	 * elsewhere
     	 */
         String parsed = "";
         char x;
         
         while (input.hasNext()) {
             String in = input.next();
             for (int i = 0; i < in.length(); i++) {
                 x = in.charAt(i);
                 if (x == 'a' || x == 't' || x == 'g' || x == 'c'|| x == 'n')
                     parsed += x;
             }
         }
         return parsed;
     }
     
     public static char complement(char c) {
     	/*
     	 * Simple function that returns a particular
     	 * base's complement.
     	 */
         char out;
         switch(c) {
             case 'a': out = 't';
                        break;
             case 't': out = 'a';
                        break;
             case 'g': out = 'c';
                        break;
             case 'c': out = 'g';
                        break;
             case 'n': out = 'n';
                        break;
             default:  out = ' ';
         }
         return out;
     }
     
     private String genStrand() {
     	/*
     	 * Takes in one strand, returns
     	 * complementary strand.
     	 */
         String r = "";
         for (int i = 0; i < oStrand.length(); i++) {
             r += complement(oStrand.charAt(i));
         }
         return r;
     }
         
     public String toString(char x, int block, int line) {
         /*
          * Returns a specified strand, splitting the strand
          * into blocks of ten and lines of a specified amount.
          */ 
         String out = "";
         String strand = "";
         int iReset = 0;
         if (x == 'o')
             strand = oStrand;
         else if (x == 'c') strand = cStrand;
         else strand = oStrand;
         
         if (x == 'o' || x == 'c'){
             for (int i = 0; i < strand.length(); i++) {
                 if (i % line == 0 && i != 0)
                     out += "\n";
                 else if (i % block == 0 && i != 0)
                     out += " ";
                  out += strand.charAt(i);
             }
         } else {
             int i = 0;
             while (i < (strand.length() + 1)){
                 
                 if ((i % line == 0 && i != 0) || i == strand.length()){
                     out += "\n";
                     if (strand == oStrand){
                         strand = cStrand;
                         i = iReset;
                     } else {
                         strand = oStrand;
                         iReset = i;
                     }
                 } else if (i % block == 0 && i != 0)
                     out += " ";
                 
                 try{
                     out += strand.charAt(i);
                 }
                 catch(Exception E){
                     break;
                 }
             
                 i++;
             }
         }
         return out;
     }
 	    
     public boolean equals(Sequence s) {
     	/*
     	 * Checks if one primer equals another.
     	 */
         return (this.oStrand.equals(s.getOStrand()) &&
                 this.cStrand.equals(s.getCStrand()) &&
                 this.fPrimer.equals(s.getFPrimer()) &&
                 this.rPrimer.equals(s.getRPrimer()));
     }
     
     public TestResult tempDifference() {
         
         int fTemp = fPrimer.getMeltingTemp();
         int rTemp = rPrimer.getMeltingTemp();
         
         if (((fTemp < rTemp) && ((rTemp - fTemp) < 3))
                 || ((rTemp < fTemp) && ((fTemp - rTemp) < 3)))
             return new TestResult(false, "Primer melting temperatures should be"
                     + " within 3 degrees of each other. Your temperatues are: "
                     + fTemp + " and " + rTemp + ".");
         else
             return new TestResult(true, null);
     }
     
     public TestResult primerTest() {
         TestResult test = new TestResult(true, "");
         TestResult fTest = fPrimer.test();
         TestResult rTest = rPrimer.test();
         TestResult paTest = fPrimer.pairAnneal(rPrimer);
         if (fTest.getPass() && rTest.getPass() && paTest.getPass()
                 && tempDifference().getPass()){ test.add("Congratulations, your"
                         + " primers meet all the design requirements!");
                 return test;
         }
         test.add("Your primers haven't met the requirements in the "
                 + "following areas:\n\n");
         if (!fTest.getPass()) {
             test.add("Forward Primer:\n");
             test.addQuiet(fPrimer.test());
             test.add("\n");
         }
         if (!rTest.getPass()) {
             test.add("Reverse Primer:\n");
             test.addQuiet(rPrimer.test());
             test.add("\n");
         }
         if (!(tempDifference().getPass() || paTest.getPass())) {
             test.add("General:\n");
         }
         if (!tempDifference().getPass()) test.add(tempDifference());
         if (!paTest.getPass()) test.add(paTest);
         return test;
         /*
         TestResult test;
         TestResult fTest = new TestResult(true, "Forward Primer:\t\n#");
         fTest.add(fPrimer.test());
         fTest.add(fPrimer.isUnique(oStrand, cStrand)); //better than whole Seq
         TestResult rTest = new TestResult(true, "\nReverse Primer:\t\n#");
         rTest.add(rPrimer.test());
         rTest.add(rPrimer.isUnique(oStrand, cStrand));
         if (fTest.getPass() && rTest.getPass() 
                 && fPrimer.pairAnneal(rPrimer).getPass()
                 && tempDifference().getPass())
             test = new TestResult(true, "Congratulations, your primers work!");
         else {
             test = new TestResult(true, "Sorry, your primers do not meet the " +
                     "following requirements:\n\n");
             if (!fTest.getPass()) test.add(fTest);
             if (!rTest.getPass()) test.add(rTest);
             if (!fPrimer.pairAnneal(rPrimer).getPass()) {
                 test.add(new TestResult(true, "General:\n"));
                 test.add(fPrimer.pairAnneal(rPrimer));
                 test.add(tempDifference());
             }
         }
         
         return test;
         */
     }
     /*public static void main(String[] args) {
         Sequence s = new Sequence(args[0]);
         System.out.println("Sequence:\n" + s);
         Primer p = new Primer(s.getCStrand());
         System.out.println("Primer:\n" + p);
         TestResult t = p.test();
         System.out.println("Test Result:\n" + t);
         
     }
     */
 }
