 package com.swtanalytics.math;
 
 import java.io.IOException;
 import org.kohsuke.args4j.Option;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.CmdLineException;
 
 public class EasyMain {
 
     public static final int NUM_MATH_FUNCTIONS_DEFAULT = 10;
 
     @Option(name="-n", usage="The number of functions to generate.")
     public int numMathFunctions = NUM_MATH_FUNCTIONS_DEFAULT;
 
 
     @Option(name="-d", usage="Print differentials too.")
     public boolean isPrintDifferential = false;
 
 
     @Option(name="-f", usage="Experiment with Fractions.")
     public boolean isFractions = false;
 
     protected void parse_input(String[] args) {
 
         CmdLineParser parser = new CmdLineParser(this);
         try {
             parser.parseArgument(args);
             // validate the input a bit.
             // There's probably a nicer way to do this with args4j
             if (this.numMathFunctions <= 0) {
                 throw new CmdLineException("Option -n requires a positive integer");
             }
 
         } catch (CmdLineException e) {
             System.out.print(e.getMessage());
             System.out.print("\n");
             parser.printUsage(System.err);
             System.exit(1);
         }
     }
 
     public void printFunction(MathFunction mf, int i, boolean diff) {
             if (diff) {
                 System.out.print("Differential ");
             }
             System.out.printf("Function %d:\n", i);
             System.out.print(mf);
             System.out.print("\n");
     }
 
     protected int createInt(boolean coefficient) {
         int i = (int)(Math.random() * 100);
         if (coefficient) {
             i -= 50;
         }
         return i;
     }
 
     protected Fraction createFraction(boolean coefficient, double wholeProb) {
         int n = createInt(coefficient);
         int d = 1;
         if (this.isFractions) {
 	    if (Math.random() > wholeProb) {
 		d  = createInt(coefficient);
 		if (d == 0) {
 		    d = 1;
 		}
 	    }
         }
 
         return new Fraction(n, d);
     }
 
     protected Term createTerm() {
         Fraction coefficient = createFraction(true, 0.75);
        Fraction exponent = createFraction(false, 0.25);
 
         return new Term(coefficient, exponent);
     }
 
     // The main logic loop
     public void run() {
         for (int i=0; i<this.numMathFunctions;++i){
             MathFunction mf = new MathFunction();
 
             int numTerms = 1 + (int)(Math.random() * 5);
 
             for (int j = 0; j<numTerms; ++j){
                 Term t = createTerm();
                 mf.addTerm(t);
             }
 
             printFunction(mf, i, false);
 
             if (this.isPrintDifferential) {
                 printFunction(mf.differentiate(), i, true);
             }
         }
     }
 
     public static void main(String[] args) throws IOException {
         EasyMain main = new EasyMain();
         main.parse_input(args);
         main.run();
     }
 
 }
