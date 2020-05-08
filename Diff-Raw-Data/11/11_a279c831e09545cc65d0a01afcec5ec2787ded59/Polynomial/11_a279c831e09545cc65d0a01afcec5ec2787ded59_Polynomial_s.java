 import java.util.ArrayList;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import java.text.DecimalFormat;
 
 
 /** Polynomial represents a polynomial from algebra.
     e.g. 4x^3 + 3x^2 - 5x + 2
 
 */
 
 public class Polynomial extends ArrayList<Integer> {
 
     /** Assign Polynomial.debug to true or false to turn debugging
      * output on or off.
      */
 
     public static boolean debug = false;
 
     /**
        no-arg constructor returns a polynomial of degree 1, with value 0
      */
     
     public Polynomial() {
 	// invoke superclass constructor, i.e. the
 	// constructor of ArrayList<Integer> with 
 	super(1); // we want capacity at least 1 
 	// parameter 1 (capacity, not size)
 	this.add(0,0); // uses autoboxing (index, value)
 	
     }
 
     /**
        get the degree of the polynomial.  Always >= 1
        @return degree of the polynomial
      */
 
     public int getDegree() { return this.size() - 1; }
 
     /**
        Construct polynomial from int array of coefficients.
        The coefficients are listed in order from highest to lowest degree.
        The degree is the size of the array - 1.
 
 
        Example: 7x^3 - 2x^2 + 3 would be represented as {7,-2,0,3}
 
        Example: x^4 - 4 would be represented as {1,0,0,0,-4}
 
        NOTE that the order of coefficients is not necessarily the way
        they will be stored in the array.   That is, the order of coefficients
        in the array passed in is from highest degree down to lowest
        degree, so for a cubic:<ul>
            <li> <code>coeffs[0]</code> is the x<sup>3</sup> coefficient </li>
            <li> <code>coeffs[1]</code> is the x<sup>2</sup> coefficient </li>
            <li> <code>coeffs[2]</code> is the x coefficient </li>
            <li> <code>coeffs[3]</code> is the constant term </li>
 	</ul>
 
 	It is done this way so that when initializing a polynomial
 	from an array literal, the order of coefficients mirrors the 
 	way polynomials are typically written, from highest order
 	term to lowest order term:
 
 	Example: to represent 4x<sup>3</sup> - 7x<sup>2</sup> + 5,<br />
 	use: <code>Polynomial p = new Polynomial (new int [] {4,-7,0,5});</code><br />
 	NOT: <code>Polynomial p = new Polynomial (new int [] {5,0,-7,4});</code>
 
 
        @param coeffsHighToLow array of coefficients in order from largest degree down to smallest.  If array has length zero, return a polynomial of degree zero with constant value zero.
      */
     
     public Polynomial(int [] coeffsHighToLow) {
 	
 	if (coeffsHighToLow.length==0) {
 	    coeffsHighToLow = new int [] {0};
 	}
 
 	int [] coeffsLowToHigh = Polynomial.lowToHigh(coeffsHighToLow);
 
 	for (int i=0;i<coeffsLowToHigh.length;i++) {
 	    this.add(i,coeffsLowToHigh[i]);
 	}
     }
 
 
     /**
        Return string respresentation of Polynomial.
        
        Leading coefficient has negative sign in front, with no space.
        Other signs have a space on either side.    Coefficients that are ones
        should be omitted (except in the x^0 term).    
        Terms with zero coefficients, except in the 
        special case where the polynomial is of degree zero, and the constant
        term is in fact zero--in that case, "0" should be returned.
 
 
        Examples:<br /> 
        <code>0<br />
        1<br />
        -4<br />
        2x - 3<br />
        x^2 - 5x + 6<br />
        x^2 - x - 1<br />
        x^2 - x <br />
        -x^7 - 2x^5 + 3x^3 - 4x<br /></code>
 
        @return string representation of Polynomial
      */
 
     public String toString() {
 	String result = "stub"; // @@@ TODO: FIX ME!
 	return result;
     }
 
 
     /**
        Construct polynomial from string representation
        that matches the output format of the Polynomial toString method.
 
        That is, you should be able to do:
 
        <code>Polynomial p = new Polynomial("0");<br />
        Polynomial p = new Polynomial("1");<br />
        Polynomial p = new Polynomial("-4");<br />
        Polynomial p = new Polynomial("2x - 3");<br />
        Polynomial p = new Polynomial("x^2 - 5x + 6");<br />
        Polynomial p = new Polynomial("x^2 - x - 1");<br />
        Polynomial p = new Polynomial("x^2 - x");<br />
        Polynomial p = new Polynomial("-x^7 - 2x^5 + 3x^3 - 4x");<br /></code>
        
        And for any Polymomial object p, the following test should pass:<br />
        <code>assertEquals(new Polynomial(p.toString()), p);</code><br />
 
        @param s string representation of Polynomial
      */
     
     public Polynomial(String s) {
 
 	// invoke superclass constructor, i.e. the
 	// constructor of ArrayList<Integer> with 
 
 	super(1); // we want capacity at least 1 
 
 	if (debug) {System.out.println("In Polynomial(String s), s=" + s);}
 
 	// For information on regular expressions in Java,
 	// see http://docs.oracle.com/javase/tutorial/essential/regex
 
 	// First check for special case of only digits,
 	// with possibly a - in front
 	// i.e. a degree 0 polynomial that is just an integer constant
 
 	Pattern integerConstantPattern = 
             Pattern.compile("^-?\\d+$");
 	Matcher integerConstantMatcher = integerConstantPattern.matcher(s);
 	
 	// if that pattern matches, then the whole string is just
 	// an integer constant.  So we can safely call Integer.parseInt(s)
 	// to convert to int, and add in this parameter.
 
 	if (integerConstantMatcher.matches()) {
 	    this.add(0,Integer.parseInt(s)); 
 	    return; // we are done!
 	}
 
 	// now, try for polynomials of degree 1
 
 	Pattern degreeOnePattern = 
             Pattern.compile("^(-?)(\\d*)x( ([+-]) (\\d+))?$");
 	// Explanation: 
 	// start/end         ^                            $
 	// sign for x term    (-?)                            group(1)
 	// coeff for x term       (\\d*)                      group(2)
 	// x in x term                  x
 	// optional constant part        (               )?   group(3)
 	// sign for constant                ([+-])            group(4)
 	// coeff for constant                      (\\d+)     group(5)
 
 	Matcher degreeOneMatcher = degreeOnePattern.matcher(s);
 
 	if (degreeOneMatcher.matches()) {
 	    
 	    int xCoeff = 1;
 	    int constantTerm = 0;
 
 	    String xCoeffSign = degreeOneMatcher.group(1);
 	    String xCoeffString = degreeOneMatcher.group(2);
 	    String constantTermSign = degreeOneMatcher.group(4);
 	    String constantTermString = degreeOneMatcher.group(5);
 
 	    if (xCoeffString != null && !xCoeffString.equals("")) {
 		xCoeff = Integer.parseInt(xCoeffString);
 	    }
 
 	    if (xCoeffSign != null && xCoeffSign.equals("-")) {
 		xCoeff *= -1;
 	    }
 
 	    if (constantTermString != null && !constantTermString.equals("")) {
 		constantTerm = Integer.parseInt(constantTermString);
 	    }
 
 	    if (constantTermSign != null && constantTermSign.equals("-")) {
 		constantTerm *= -1;
 	    }
 
 	    this.add(0,constantTerm); 
 	    this.add(1,xCoeff); 
 	    return;
 	}
 
 	// then try for higher degree
 
 	String twoOrMoreRe = 
 	    "^" // start of the string 
 	    + "([-]?)(\\d*)x\\^(\\d+)" // first x^d term, groups 1,2,3
 	    + "(( [+-] \\d*x\\^\\d+)*)" // zero or more x^k terms group 4 (and 5)
 	    + "( [+-] \\d*x)?" // optional x term (group 6)
 	    + "( [+-] \\d+)?" // optional constant term (group 7)
 	    + "$"; // the end of the string
 
 	Pattern degreeTwoOrMorePattern  = Pattern.compile(twoOrMoreRe);
 	Matcher degreeTwoOrMoreMatcher = degreeTwoOrMorePattern.matcher(s);
 
 	// if we have a match...
 	if (degreeTwoOrMoreMatcher.matches()) {
 	    
 	    int firstCoeff = 1;
 	    String startSign      = degreeTwoOrMoreMatcher.group(1);
 	    String coeffString    = degreeTwoOrMoreMatcher.group(2);
 	    String degreeString   = degreeTwoOrMoreMatcher.group(3);
 	    String middleXtoTheTerms = degreeTwoOrMoreMatcher.group(4);
 	    String optionalXTermPart = degreeTwoOrMoreMatcher.group(6);
 	    String optionalConstantTermPart = degreeTwoOrMoreMatcher.group(7);
 
 	    if (coeffString != null && !coeffString.equals("")) {
 		firstCoeff = Integer.parseInt(coeffString);
 	    }
 
 	    if (startSign != null && startSign.equals("-")) {
 		firstCoeff *= -1;
 	    }
 	    
 	    int degree = Integer.parseInt(degreeString);
 
 	    this.ensureCapacity(degree+1); // method of ArrayList<Integer>
 	    for(int i=0; i<=degree; i++) // initialize all to zero
 		this.add(0,0);
 
 	    this.set(degree,firstCoeff);
 
 	    if (middleXtoTheTerms!=null && !middleXtoTheTerms.equals("")) {
 		    
 		Pattern addlXtoThePowerTermPattern  = 
 		    Pattern.compile(" ([+-]) (\\d+)(x\\^)(\\d+)");
 		Matcher addlXtoThePowerTermMatcher 
 		    = addlXtoThePowerTermPattern.matcher(middleXtoTheTerms);
 
 		while (addlXtoThePowerTermMatcher.find()) {
 		    
 		    int coeff = 1;
 		    String sign           = addlXtoThePowerTermMatcher.group(1);
 		    String nextCoeffString    = addlXtoThePowerTermMatcher.group(2);
 		    String nextDegreeString   = addlXtoThePowerTermMatcher.group(4);
 		    
 		    if (nextCoeffString != null && !nextCoeffString.equals("")) {
 			coeff = Integer.parseInt(nextCoeffString);
 		    }
 
 		    if (sign != null && sign.equals("-")) {
 			coeff *= -1;
 		    }
 		    
 		    this.set(Integer.parseInt(nextDegreeString),coeff);
 		    
 		}
 	    } // if middleXToTheTerms has something
 	    
 	    // Now all that is left is, possibly, an x term and a constant
 	    // term.    We need to select them out if they are there.
 	    
 	    if (optionalXTermPart != null && !optionalXTermPart.equals("")) {
 
 		if (debug) {System.out.println("optionalXTermPart=" +
 					       optionalXTermPart);}
 
 		Pattern optXTermPattern = 
 		    Pattern.compile("^ ([+-]) (\\d*)x$");
 		Matcher optXTermMatcher = optXTermPattern.matcher(optionalXTermPart);
 		optXTermMatcher.find();
 	    
 		int xCoeff = 1;
 		int constantTerm = 0;
 		String xCoeffSign = optXTermMatcher.group(1);
 		String xCoeffString = optXTermMatcher.group(2);
 		
 		if (xCoeffString != null && !xCoeffString.equals("")) {
 		    xCoeff = Integer.parseInt(xCoeffString);
 		}
 		
 		if (xCoeffSign != null && xCoeffSign.equals("-")) {
 		    xCoeff *= -1;
 		}
 		
 		this.set(1,xCoeff); 
 	    } // optionalXTerm part
 
 	    if (optionalConstantTermPart != null 
 		&& !optionalConstantTermPart.equals("")) {
 
 		Pattern constantTermPattern = 
 		    Pattern.compile("^ ([+-]) (\\d+)$");
 		Matcher constantTermMatcher 
 		    = constantTermPattern.matcher(optionalConstantTermPart);
 		
 		constantTermMatcher.find();
 
 		int constant = 0;
 		String sign = constantTermMatcher.group(1);
 		String constantString = constantTermMatcher.group(2);
 
 		if (constantString != null && !constantString.equals("")) {
 		    constant = Integer.parseInt(constantString);
 		}
 
 		if (sign!=null && sign.equals("-")) {
 		    constant *= -1;
 		}
 		
 		this.set(0,constant); 
 	    } // a constant term is present
 
 	    return;
 	} // degreeTwoOrMore
 	
 	if (debug) {System.out.println("at bottom");}
 
 	// in the end, if we don't find what we need,
 	// through an exception
 
 	throw new IllegalArgumentException("Bad Polynomial String: [" + s + "]");
 
     }
 
 
     /** 
 	determine whether two polynomials are equal 
 	(same degree, same coefficients)
 	
 	@param o arbitrary object to test for equality
 	@return true if objects are equal, otherwise false
     */
 
     public boolean equals(Object o) {
 
 	// This is boiler plate code for a equals method in Java
 	// Always do this first
 
 	if (o == null)
 	    return false;
 	if (!(o instanceof Polynomial))
 	    return false;
 
 	Polynomial p = (Polynomial) o;
 
 	// @@@ TODO: Check the size of each ArrayList.  
 	// If they don't match, return false
 
 	// @@@ TODO: If the sizes match, check whether the
 	// values match.  If not, return False.  Otherwise, return true.
 
 	return false; // @@@ STUB
 	
     }
 
     /** Given an int [] of coefficients from lowest to highest
 	degree (where the index in the array matches the power of the
	x term), find the index of the highest degree non-zero term.
 	If all terms are zero, return 0.
 
 	This is a utility method that may be useful in converting
 	between the low to high and high to low representations of
 	coefficients, both in user programs that use the Polynomial
 	class, as well as in internal routines used to implement
 	Polynomial methods.
 	
 	@param coeffsLowToHigh coefficients of a polynomial in order from lowest degree to highest degree.  May have trailing zeros.

 
     */
 
     public static int degreeCoeffsLowToHigh(int [] coeffsLowToHigh) {
 	return -42; // @@@ STUB!
     }
 
 
     /** Given an int [] of coefficients from highest to lowest
 	degree (the formal used for input to the Polynomial constructor),
	find the index of the highest degree non-zero term.
 	If all terms are zero, return 0.
 
 	This is a utility method that may be useful in converting
 	between the low to high and high to low representations of
 	coefficients, both in user programs that use the Polynomial
 	class, as well as in internal routines used to implement
 	Polynomial methods.
 	
 	@param coeffsHighToLow coefficients of a polynomial in order from highest degree first to lowest degree last.  May have leading zeros.
	@return array of coefficients of a polynomial from lowest from highest.  No leading zeros.
 
     */
 
     public static int degreeCoeffsHighToLow(int [] coeffsHighToLow) {
 
 	return -42; // @@@ STUB!
     }
 
 
     /** Convert a list of coefficients in order from 
 	highest degree to lowest degree (the order used
 	for input to the Polynomial constructor) to one where
 	the order is lowest degree to highest degree (where index matches
 	power of the x term).
 	
 	@param coeffsHighToLow coefficients of a polynomial in order from highest degree to lowest degree.   May have leading zeros.
 	@return An int [] with coefficients in order from lowest degree to highest degree.   No trailing zeros.
     */
 
     public static int [] lowToHigh(int [] coeffsHighToLow) {	
 	int [] stub = new int [] {-42, -42, -42};
 	return stub;
     }
 
 
     /** Convert a list of coefficients in order from 
 	lowest degree to highest degree (where index matches
 	power of the x term) to one in order from 
 	highest degree to lowest degree (the order used
 	for input to the Polynomial constructor).
 	
 	@param coeffsLowToHigh coefficients of a polynomial in order from lowest degree to highest degree.  May have trailing zeros.
 	@return An int [] with coefficients in order from highest degree to lowest degree.   No leading zeros.
     */
 
     public static int [] highToLow(int [] coeffsLowToHigh) {
 	int [] stub = new int [] {-42, -42, -42};
 	return stub;
     }
     
     /** return a new Polynomial which has as its value the 
 	this polynomial plus the one passed in as a parameter.
 
 	@param p the Polynomial to add to this one
 	@return sum of this Polynomial and p
 
     */
 
     public Polynomial plus (Polynomial p) {	
 	Polynomial stub = new Polynomial (new int [] {-42});
 	return stub;
     }
 
     /** return a new Polynomial which has as its value the 
 	this polynomial times the one passed in as a parameter.
 
 	@param p the Polynomial to multiply this one by
 	@return product of this Polynomial and p
 
     */
 
     public Polynomial times (Polynomial p) {
 	
 	Polynomial stub = new Polynomial (new int [] {-42});
 	return stub; // @@@ TODO: FIXME!
 
     }
 
     /** return a new Polynomial which has as its value the 
 	this polynomial minus the one passed in as a parameter.
 
 	@param p the Polynomial to subtract from this one
 	@return the result of this Polynomial minus p
 
     */
 
 
     public Polynomial minus (Polynomial p) {
 
 	Polynomial stub = new Polynomial (new int [] {-42});
 	return stub; // @@@ TODO: FIXME!
 
     }
 
     /** Print Usage message for Polynomial main 
      */
 
     public static void usage() {
 	System.err.println("Usage: java Polynomial 'string' ");
 	System.err.println(" java Polynomial 'string' ");
     }
 
     /** 
 	Main for testing constructing Polynomials from strings,
 	and for testing plus, minus and times.
 
 	At Unix command line, use single quotes to make the entire
 	Polynomial be a single argument, and use 
 	\* when operating is * (to avoid
 	having * expanded as a wildcard.)  For example:
 	<code>java -cp build Polynomial 'x^2 + 2x + 3' \* 'x - 4'</code>
      */
 
     public static void main (String [] args) {
 
 	if (args.length < 1) {
 	    Polynomial.usage();
 	    System.exit(1); // error code 1
 	}
 
 	Polynomial p = new Polynomial(args[0]);
 
 	if (args.length == 1) {
 	    System.out.println("p=" + p);
 	    System.exit(0); // successful completion
 	}
 
 	if (args.length != 3) {
 	    System.out.println("There should be either 1 cmd line argument or 3 arguments, but there were: " + args.length + " arguments.");
 	    Polynomial.usage();
 	    System.exit(1);
 	}
 	
 	Polynomial p2 = new Polynomial(args[2]);
 	if (args[1].equals("+")) {
 	    Polynomial result = p.plus(p2);
 	    System.out.println("(" + p.toString() + ") + (" +
 			       p2.toString() + ") = " + result.toString());
 	}  else if (args[1].equals("-")) {
 	    Polynomial result = p.minus(p2);
 	    System.out.println("(" + p.toString() + ") - (" +
 			       p2.toString() + ") = " + result.toString());
 	} else if (args[1].equals("*")) {
 	    Polynomial result = p.times(p2);
 	    System.out.println("(" + p.toString() + ") * (" +
 			       p2.toString() + ") = " + result.toString());
 	} else {
 	    System.out.println("Error: illegal operand:" + args[1]);
 	    Polynomial.usage();
 	    System.exit(2); // error code 2
 	}
     } // end main
 
 
 } // end class Polynomial
