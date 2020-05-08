 package au.edu.uq.csse2002.week3.tutorial;
 
 // I chose to implement this class to help communicate concepts.
 
 public class Pattern {
 	// immutable!
 	private final String pattern;
 
 	// We define a static factory method to construct a pattern from a string.
 	// This helps readability, but there's a stronger reason to do it. It's
 	// virtually always a great idea to only provide static factory methods for
 	// immutable classes (and hide your constructors). I'll explain more in
 	// detail later on in the semester, but the short story is that static
 	// factory methods don't *have* to return a new object (= more memory
 	// usage), whereas constructors do. So, if we pass the same string into
 	// this method twice, we have the liberty of only creating one object and
 	// returning it both times (which is fine, because it's immutable).
 	public static Pattern fromString(String pattern) {
 		// In this implementation, we're just delegating to the
 		// constructor anyway, so nothing is effectively different.
 		// We'll still be allocating new memory every time fromString is
 		// called. However, the important point is that it can be changed
 		// later. We can change our implementation of how our Patterns are
 		// created and client code won't break; clients won't even know!
 		if (isValid(pattern)) {
 			return new Pattern(pattern);
 		} else {
			throw new InvalidPatternException(pattern);
 		}
 	}
 	// see the bottom of the class for example code
 
 	// Our constructor is private so clients must call the factory method
 	private Pattern(String pattern) {
 		this.pattern = pattern;
 	}
 
 	// We also define a static validator method to make life much easier for
 	// clients of this class. Note: we don't have to call this "isValidString",
 	// thanks to Java's overriding ability. We can easily define another method
 	// later on with the type "public static boolean isValid(Doohicky x)" to
 	// validate Doohickies, and it would coexist happily with this.
 	public static boolean isValid(String pattern) {
 		// This method is far and away the hardest to implement. I would
 		// probably approach it by defining a "T9Keyboard" class to work out
 		// e.g. what's "up" from 5, and go from there.
 	}
 
 	// We define the standard equals method so that, e.g., the safe can
 	// determine equality of two patterns to work out if a given pattern
 	// is valid.
 	public boolean equals(Pattern other) {
		// We've implemented our pattern to have
 		return pattern.equals(other.pattern);
 	}
 	// we could call our method to determine equality "equalTo" but the
 	// "equals" method is standard in Java and is defined by Object (Pattern's
 	// superclass), so always call your equality methods "equals".
 
 	/* Example code:
 
 	String patternCode = getInput();
 	if (Pattern.isValid(patternCode)) {
 		Pattern p = Pattern.fromString(patternCode);
 		if (p.equals(this.pattern)) {
 			unlock();
 		}
 	}
 
 	 */
 }
