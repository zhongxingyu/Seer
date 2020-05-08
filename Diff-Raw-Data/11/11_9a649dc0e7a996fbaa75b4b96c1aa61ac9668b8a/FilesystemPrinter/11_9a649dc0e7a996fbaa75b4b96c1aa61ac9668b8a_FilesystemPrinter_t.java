 package ru.spbau.martynov.task2;
 
 import java.util.Vector;
 
 /**
  * @author Semen A Martynov, 13 Feb 2013
  * 
  *         The class displays a catalog tree.
  */
 public class FilesystemPrinter {
 
 	/**
 	 * Function is used for displaying beautiful catalog tree - when we work
 	 * with the last sub directory, the separator shall be replaced with a
 	 * space.
 	 * 
 	 * @param prefixIndex
 	 *            The nesting level concerning a root.
 	 */
 	public void setShift(int prefixIndex) {
 		if (prefix.size() > prefixIndex) {
 			prefix.get(prefixIndex - 1).setSeparator(false);
 		}
 	}
 
 	/**
 	 * Function saves length of a line and transfers parameters to the printing.
 	 * 
 	 * @param nodeName
 	 *            Name of the file or the directory.
 	 * @param prefixIndex
 	 *            The nesting level concerning a root.
 	 * @param access
 	 *            Availability test result.
 	 */
 	public void printDirectory(String nodeName, int prefixIndex, boolean access) {
 		int space = nodeName.length();
 		// If it's not a root directory - we need to add another one space,
 		// because the character _ increases indent length.
 		if (prefixIndex != 0) {
 			space++;
 		}
 
 		// If the element doesn't exist - we will create it, also we will save
 		// length of a nodeName.
 		if (prefix.size() <= prefixIndex) {
			prefix.add(prefixIndex, new PrefixNode(space));
 		} else {
 			prefix.get(prefixIndex).setSpace(space);
 			prefix.get(prefixIndex).setSeparator(true);
 		}
 
 		// Length of a nodeName saved in a prefix - it is possible to display a
 		// nodeName.
 		print(nodeName, prefixIndex, access);
 	}
 
 	/**
 	 * Function builds a line consisting of a prefix, file name, and results of
 	 * availability test.
 	 * 
 	 * @param nodeName
 	 *            Name of the file or the directory.
 	 * @param prefixIndex
 	 *            The nesting level concerning a root.
 	 * @param access
 	 *            Availability test result.
 	 */
 	public void print(String nodeName, int prefixIndex, boolean access) {
 
 		StringBuilder result = new StringBuilder();
 		// Let's pass a prefix, and build a tree.
 		for (int i = 0; i != prefixIndex; i++) {
 			for (int j = 0; j != prefix.get(i).getSpace(); j++) {
 				result.append(" ");
 			}
 			// If we work with the last sub directory, the tree changes the
 			// look.
 			if (prefix.get(i).isSeparator()) {
 				result.append("|");
 			} else {
 				result.append(" ");
 			}
 		}
 
 		// If it's not a root directory - we need to add "_"
 		if (prefixIndex != 0) {
 			result.append("_");
 		}
 
 		// Add name of the node
 		result.append(nodeName);
 
 		// Add availability test result
 		if (!access) {
 			result.append(" (access denied)");
 		}
 		System.out.println(result);
 	}
 
 	/**
 	 * Stores structures with information on quantity of spaces and a separator
 	 * flag.
 	 */
	private Vector<PrefixNode> prefix = new Vector<PrefixNode>();
 
 	/**
 	 * @author Semen A Martynov
 	 * 
 	 *         Class with information on quantity of spaces and a separator
 	 *         flag.
 	 */
	private class PrefixNode {
 		/**
 		 * Constructor with one argument - space. Separator flag will be set to
 		 * true.
 		 * 
 		 * @param space
 		 *            Quantity of characters in a name of a parent directory.
 		 */
		public PrefixNode(int space) {
 			this.space = space;
 			separator = true;
 		}
 
 		/**
 		 * Saves quantity of characters in a name.
 		 * 
 		 * @param space
 		 *            quantity of characters in a name.
 		 */
 		public void setSpace(int space) {
 			this.space = space;
 		}
 
 		/**
 		 * Returns quantity of spaces.
 		 * 
 		 * @return quantity of spaces.
 		 */
 		public int getSpace() {
 			return space;
 		}
 
 		/**
 		 * Is it possible to show a separator.
 		 * 
 		 * @return separator flag.
 		 */
 		public boolean isSeparator() {
 			return separator;
 		}
 
 		/**
 		 * Sets value of a separator flag.
 		 * 
 		 * @param separator
 		 *            flag.
 		 */
 		public void setSeparator(boolean separator) {
 			this.separator = separator;
 		}
 
 		/**
 		 * Quantity of characters in a name of a parent directory, i.e. quantity
 		 * of spaces.
 		 */
 		private int space;
 		/**
 		 * Separator flag.
 		 */
 		private boolean separator;
 	}
 }
