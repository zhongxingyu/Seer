 package com.swtanalytics.math;
 
 import java.io.IOException;
 
 import org.kohsuke.args4j.Option;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.CmdLineException;
 
 public class EasyMain {
     public static final int NUM_MATH_FUNCTIONS_DEFAULT = 10;
     private final MathFunctionFactory functionFactory;
 
     public EasyMain() {
         RandomGenerator randomGenerator = new RandomGeneratorUsingMathRandom();
         FractionFactory fractionFactory = new RandomFractionFactory(randomGenerator);
         TermFactory termFactory = new RandomTermFactory(fractionFactory);
         functionFactory = new MathFunctionFactory(termFactory, randomGenerator);
     }
 
     protected enum FunctionType
     {
     	NORMAL,
     	DIFFERENTIAL,
     	INTEGRAL
     }
 
     @Option(name="-n", usage="The number of functions to generate.")
     public int numMathFunctions = NUM_MATH_FUNCTIONS_DEFAULT;
 
     @Option(name="-d", usage="Print differentials too.")
     public boolean isPrintDifferential = false;
 
     @Option(name="-f", usage="Experiment with Fractions.")
     public boolean isFractions = false;
 
     @Option(name="-x", usage="Format output as XML.")
     public boolean outputXml = false;
 
     @Option(name="-i", usage="Print integrals too.")
     public boolean isPrintIntegral = false;
 
     @Option(name="-l", usage="Create only strictly first-degree linear functions.")
     public boolean forceLinearFunctions = false;
 
     @Option(name = "--domain-min", usage = "Domain minimum, for min/max calculations (defaults to -infinity)")
     public double domainMin = Double.NEGATIVE_INFINITY;
 
     @Option(name = "--domain-max", usage = "Domain maximum, for min/max calculations (defaults to +infinity)")
     public double domainMax = Double.POSITIVE_INFINITY;
 
     protected void parse_input(String[] args) {
         CmdLineParser parser = new CmdLineParser(this);
         try {
             parser.parseArgument(args);
             // validate the input a bit.
             // There's probably a nicer way to do this with args4j
             if (this.numMathFunctions <= 0) {
                 throw new CmdLineException(parser, "Option -n requires a positive integer");
             }
 
         } catch (CmdLineException e) {
             System.out.print(e.getMessage());
             System.out.print("\n");
             parser.printUsage(System.err);
             System.exit(1);
         }
     }
 
     protected String getFunctionString(MathFunction mf, int i, FunctionType type) {
     	StringBuilder functionString = new StringBuilder();
     	
     	// If we're outputting xml, indent and omit the labels printed for the standard output format
     	if (outputXml) {
             functionString.append("      ").append(mf);
     	}
     	else
     	{
         	if (type == FunctionType.DIFFERENTIAL) {
                 functionString.append("Differential ");
             }
         	else if (type == FunctionType.INTEGRAL) {
                 functionString.append("Integral ");
             }
     		
             functionString.append(String.format("Function %d:\n", i));
             functionString.append(mf);
             
             if (type == FunctionType.INTEGRAL) {
             	functionString.append("+ constant");
             }
     	}
 
         functionString.append("\n");
 
         return functionString.toString();
     }
 
     public void printFunction(MathFunction mf, int i) {
     	if (outputXml) {
     		System.out.println("  <function>");
     	}
 
     	printFunction(mf, i, FunctionType.NORMAL);
 
         if (this.isPrintDifferential) {
             printFunction(mf.differentiate(), i, FunctionType.DIFFERENTIAL);
         }
 
         if (this.isPrintIntegral) {
         	printFunction(mf.integrate(), i, FunctionType.INTEGRAL);
         }
 
         if (mf.isLinearFunction()) {
         	printSlope(mf.computeSlope());
         }
         
         printMinMax(mf);
         
        printIntercepts(mf);

     	if (outputXml) {
     		System.out.println("  </function>");
     	}
     }
 
     private void printSlope(Fraction slope) {
         if (outputXml) {
             System.out.println("    <slope>");
             System.out.print("      ");
             System.out.println(slope.formatString(true, false));
             System.out.println("    </slope>");
         }
     }
 
     private void printMinMax(MathFunction function) {
         try {
             double minX = function.findMinimum(domainMin, domainMax);
             double minY = function.evaluate(minX);
             if (!Double.isNaN(minX) && !Double.isNaN(minY)) {
                 System.out.format(outputXml ? "    <min>(%f, %f)</min>%n" : "Min:(%f, %f)%n", minX, minY);
             }
         } catch (UnsupportedOperationException e) {
         }
 
         try {
             double maxX = function.findMaximum(domainMin, domainMax);
             double maxY = function.evaluate(maxX);
             if (!Double.isNaN(maxX) && !Double.isNaN(maxY)) {
                 System.out.format(outputXml ? "    <max>(%f, %f)</max>%n" : "Max:(%f, %f)%n", maxX, maxY);
             }
         } catch (UnsupportedOperationException e) {
         }
     }
 
    private void printIntercepts(MathFunction function) {
        System.out.format(outputXml ? "    <y-intercept>%f</y-intercept>%n" : "Y-intercept:%f%n", function.evaluate(0));

        try {
            for (double solution : function.solve())
            {
                System.out.format(outputXml ? "    <x-intercept>%f</x-intercept>%n" : "X-intercept:%f%n", solution);
            }
        } catch (UnsupportedOperationException e) {
        }
    }

     protected void printFunction(MathFunction mf, int i, FunctionType type) {
     	StringBuilder outputString = new StringBuilder();
     	
     	// Decorate with xml if xml output was specified
     	if (outputXml) {
     		switch (type) {
 	    		case NORMAL:
 	    			outputString.append("    <output>\n");
 	    			break;
 	    		case DIFFERENTIAL:
 	    			outputString.append("    <derivative>\n");
 	    			break;
 	    		case INTEGRAL:
 	    			outputString.append("    <integral>\n");
 	    			break;
     		}
     	}
     	
     	outputString.append(getFunctionString(mf, i, type));
 
     	// Decorate with xml if xml output was specified
     	if (outputXml) {
     		switch (type) {
     		case NORMAL:
     			outputString.append("    </output>\n");
     			break;
     		case DIFFERENTIAL:
     			outputString.append("    </derivative>\n");
     			break;
     		case INTEGRAL:
     			outputString.append("    </integral>\n");
     			break;
     		}
     	}
     	
     	System.out.print(outputString.toString());
     }
 
     // The main logic loop
     public void run() {
     	if (outputXml)
     	{
     		System.out.println("<functions>");
     	}
 
     	for (int i=0; i<this.numMathFunctions;++i){
             MathFunction mf = functionFactory.create(!this.isFractions, this.forceLinearFunctions);
             printFunction(mf, i);
         }
 
     	if (outputXml)
     	{
     		System.out.println("</functions>");
     	}
     }
 
     public static void main(String[] args) throws IOException {
         EasyMain main = new EasyMain();
         main.parse_input(args);
         main.run();
     }
 }
