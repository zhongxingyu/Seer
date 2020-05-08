 package jay.apcs.blurbs;
 
 /**
  * This class represents a Whoozit, a String starting with an x, followed by any number of ys. It may also contain a
  * following Whatzit.
  * 
  * @see Whatzit
  * @see Blurb
  * @author Jay Fleischer
  * @version 1.0 (9-27-13)
  */
 public class Whoozit {
 	private Whatzit whatzit = null;
 	private String whoozit = "x";
 
 	/**
 	 * Creates a random Whoozit, without a following Whatzit. Equivalent to new Whoozit(false).
 	 */
 	public Whoozit() {
 		this(false);
 	}
 
 	/**
 	 * Creates a random Whoozit, with a following Whatzit, if the passed argument is true.
 	 * 
 	 * @param needsNext
 	 *            - Whether or not there should be a following Whatzit.
 	 */
 	public Whoozit(boolean needsNext) {
 		while (Math.random() * Math.random() < Math.random())
 			whoozit += "y";
 		if (needsNext || Math.random() > .125)
 			whatzit = new Whatzit();
 	}
 
 	/**
 	 * Creates a Whoozit from a passed String. Equivalent to new Whoozit(whoozit,false).
 	 * 
 	 * @param whoozit
 	 *            - the String used to create the Whoozit.
 	 */
 	public Whoozit(String whoozit) {
 		this(whoozit, false);
 	}
 
 	/**
 	 * Creates a Whoozit from a passed String, but requires it to have a following Whatzit. It throws an exception if it
 	 * doesn't have a following Whatzit.
 	 * 
 	 * @param whoozit
 	 *            - the String used to create the Whoozit.
 	 * @param needsNext
 	 *            - whether or not there must be a following Whatzit.
 	 * @throws IllegalArgumentException
 	 *             if the passed String is not a valid Whoozit or if the passed String does not contain a following
	 *             Whatzit and there must be one.
 	 */
 	public Whoozit(String whoozit, boolean needsNext) throws IllegalArgumentException {
 		if (whoozit.startsWith("x")) {
 			int index = 0;
 			while (whoozit.length() > ++index && 'y' == whoozit.charAt(index))
 				this.whoozit += "y";
 			if (whoozit.length() != index)
 				whatzit = new Whatzit(whoozit.substring(index));
 			else if (needsNext)
 				throw new IllegalArgumentException("String needs another Whatzit!");
 		} else
 			throw new IllegalArgumentException("String is not a valid Whoozit!");
 	}
 
 	@Override
 	/**
 	 * returns the String representation of the Whoozit.
 	 */
 	public String toString() {
 		return whoozit + (whatzit != null ? whatzit.toString() : "");
 	}
 }
