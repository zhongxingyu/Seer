 /*
  * Parse tree node.
  * Copyright (C) 2013  Zuben El Acribi
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package bnf;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * This class represents a single node of the parse tree.<br/>
  * It contains the whole string, and the beginning and the end
  * of the substring it represents.<br/>
  * It points to its successors and to its parent, so the
  * structure is bi-directional.<br/>
  * Every tree node has its definition obtained by the BnfDefParser.<br/>
  * <br/>
  * This structure has some more features:<br/>
  * &nbsp;&nbsp;&nbsp;&nbsp;- prefix: this is used in the toString()
  * method to append some string before this node;<br/>
  * &nbsp;&nbsp;&nbsp;&nbsp;- suffix: this is used in the toString()
  * method to append some string after this node;<br/>
  * &nbsp;&nbsp;&nbsp;&nbsp;- hide: this flag tells the toString()
  * method not to visualize current node. The current node can be
  * just hidden or may be replaced by a string if used in combination
  * with prefix/suffix.<br/>
  * 
  * @author Zuben El Acribi
  *
  */
 public class Tree {
 
 	/**
 	 * The whole string on which a parse tree has been built over.
 	 */
 	public String s;
 	
 	/**
 	 * The beginning (inclusive) and the end (exclusive) of the substring in 's'.
 	 */
 	public int begin, end;
 	
 	/**
 	 * The shortcut for s.substring(begin, end).
 	 */
 	public String node;
 	
 	/**
 	 * The node type.<br/>If this is a token then 'node' (or node.toLowerCase())
 	 * should be equal to this token (or def.node.toLowerCase()).<br/>
 	 * If this is a sequence then all branches of 'def' should have corresponding
 	 * branch in this node.<br/>
 	 * If this is a choice then just one branch of 'def' should have matched
 	 * and its corresponding branch of this node should be non-null; all other
 	 * branches should be null.<br/>
 	 * If this is an optional then it should have 0 or 1 branches; if the branches
 	 * are 0 then this node's 'begin' should be equal to 'end' (this node matches the
 	 * empty string).<br/>
 	 * If this is a repetition then it can have any number of branches (0 or more)
 	 * and every branch should match this node's definition.
 	 */
 	public NodeType type;
 	
 	/**
 	 * This node's definition which is a parse tree obtained by BnfDefParser.parse().
 	 * Every tree should be annotated with a definition except for the trees
 	 * obtained by BnfDefParser.parse().
 	 */
 	public Tree def;
 	
 	/**
 	 * This node's parent.
 	 */
 	public Tree parent;
 	
 	/**
 	 * Optional string which is visualized in the toString() method before
 	 * (prefix) and after (suffix) this node.<br/>If this is null then
 	 * nothing gets visualized with this node.
 	 */
 	public String prefix, suffix; // This node may be surrounded by strings like opening and closing bracket.
 	
 	/**
 	 * A flag that if 'true' then hides this node in the toString() method.<br/>
 	 * This node won't be visualized but still its prefix and/or suffix will if present.
 	 */
 	public boolean hide;
 
 	/**
 	 * This node's branches.<br/>If the node is a terminal (type=token) then
 	 * it shouldn't have branches and in all other cases except for type=choice
 	 * it should have the same number of branches as in def.branches and
 	 * all of them should be non-null.<br/>
 	 * If type=choice just one branch should be non-null and all previous ones
 	 * (if any) should be null. The next branches are undefined (may be null or
 	 * the non-null branches may be the last one, i.e. there are no branches
 	 * after the non-null one).
 	 */
 	public ArrayList<Tree> branches = new ArrayList<Tree>();
 
 	/**
 	 * Constructs a tree with a given type.<br/>
 	 * This constructor is used when parsing non-terminals because
 	 * it may be unknown how many branches it should have and
 	 * what is the beginning and the end of the parsed string.
 	 * @param type a non-terminal type (i.e. type != token).
 	 * @param annotation the definition of this node.
 	 */
 	public Tree(NodeType type, Tree annotation) {
 		this.type = type;
 		this.begin = this.end = -1; // 'begin' and 'end' should be defined in addBranch().
 		this.def = annotation;
 	}
 
 	/**
 	 * This constructor is used when parsing a terminal
 	 * because in this case we know exactly the beginning and the end
 	 * of the parsed substring.<br/>
 	 * This node's type is inferred from the annotation if any.<br/>
 	 * If the annotation is null (we parse a BNF definition)
 	 * the this node's type is inferred
 	 * from the specified substring:<br/><br/>
 	 * 1. If this node is the empty string then it is a token (an empty
 	 * string may appear when evaluating an optional which hasn't matched
 	 * a value).<br/>
 	 * 2. If this node is a string surrounded by quotes then it is a token.<br/>
 	 * 3. If there is an annotation (i.e. we parse a language described
 	 * with BNF) and its type is 'token' or 'new line' then this node is a token.<br/>
 	 * 4. If this node is a keyword then:<br/>
 	 * 4.1. TOKEN has type of 'token_keyword'<br/>
 	 * 4.2. IDENTIFIER has type of 'identifier_keyword'<br/>
 	 * 4.3. NEW_LINE has type of 'new_line_keyword'<br/>
 	 * 5. If the node's string contains identifier symbols only then
 	 * it is an identifier.<br/>
 	 * @param s the whole string over which a parse tree is being built.
 	 * @param begin the beginning of the recognized substring.
 	 * @param end the end of the recognized substring.
 	 * @param annotation the definition of this node.
 	 * @throws InvalidIdentifierException if the node's type is inferred to be 'identifier'
 	 *   but contains non-identifier symbols.
 	 */
 	public Tree(String s, int begin, int end, Tree annotation) throws InvalidIdentifierException {
 		this.s = s;
 		this.begin = begin;
 		this.end = end;
 		this.node = s.substring(begin, end);
 		this.def = annotation;
 
 		if (node.length() == 0 || (node.startsWith("'") && node.endsWith("'")) ||
 				(annotation != null && (annotation.type == NodeType.token || annotation.type == NodeType.new_line_keyword))) {
 			type = NodeType.token; // This corresponds to a keyword or a special symbol like arithmetic symbols or brackets.
 		} else if (node.equals("TOKEN")) {
 			type = NodeType.token_keyword; // This node should appear when parsing a BNF definition only.
 		} else if (node.equals("IDENTIFIER")) {
 			type = NodeType.identifier_keyword; // This node should appear when parsing a BNF definition only.
 		} else if (node.equals("NEW_LINE")) {
 			type = NodeType.new_line_keyword; // This node should appear when parsing a BNF definition only.
 		} else {
 			type = NodeType.identifier; // This node is a defined as terminal but will actually refer to a BNF definition.
 			// This node should appear when parsing a BNF definition only.
 			// Check whether this is an identifier.
 			for (int i = 0; i < node.length(); i++) {
 				if (!Character.isJavaIdentifierPart(node.charAt(i))) {
 					throw new InvalidIdentifierException("Expression in [" + begin + ", " + (end - 1) + "]: '" + node + "', is not an identifier");
 				}
 			}
 		}
 	}
 
 	/**
 	 * Adds a branch to this node.<br/>
 	 * If the added branch is non-null then 'begin', 'end' and 'node' are
 	 * changed so that 'begin' may move to the left or stay untouched,
 	 * 'end' may move to the right or stay untouched (i.e. we can only expand
 	 * this node's parsed substring) and 'node' will take the new substring.<br/>
 	 * @param t the node to be added as a branch to this node; its parent is made
 	 *   to point at this node.
 	 */
 	public void addBranch(Tree t) {
 		if (t == null) {
 			branches.add(null);
 			return;
 		}
 		if (s == null) {
 			this.s = t.s;
 		}
 		if (this.begin < 0) {
 			this.begin = t.begin;
 		}
 		if (this.end < t.end) {
 			this.end = t.end;
 		}
 		this.node = s.substring(begin, end);
 		branches.add(t);
 		t.parent = this;
 	}
 	
 	@Override
 	public String toString() {
 		// Before starting to collect characters the prefixes and suffixes should
 		// be collected, as well as the hidden nodes.
 		HashMap<Integer, List<Tree>> map = getPrefixesAndSuffixes();
 		
 		// In the case of hidden nodes an iterator and its current element will be holden.
 		Iterator<Tree> hiddenIter = getHidden().iterator();
 		Tree hidden = hiddenIter.hasNext() ? hiddenIter.next() : null;
 		
 		// This is a shortcut when no prefixes/suffixes/hidden nodes are found.
 		if (map.size() == 0 && hidden == null) {
 			return node;
 		}
 		
 		// There is at least one prefix/suffix/hidden node. We need a buffer to store the result.
 		StringBuffer buff = new StringBuffer();
 
 		// We start adding all characters and check whether we have a prefix or suffix
 		// on the current position. Prefixes and suffixes, if both used in a single node,
 		// are considered as opening and closing brackets respectively.
 		// Nesting between opening and closing brackets is performed as follows:
 		// 1. If two nodes share the same 'begin' pos then the node with greater 'end'
 		//    should be put first (the node that surrounds greater substring;
 		//    broader substring's opening bracket should be put first).
 		// 2. If two nodes share the same 'end' pos then the node with greater 'begin'
 		//    should be put first (the node that surrounds smaller substring;
 		//    narrower substring's closing bracket should be put first).
 		// 3. If a node has defined a prefix but not a suffix or a suffix but not a prefix
 		//    then this is just preceding/trailing string that should be placed
 		//    on the 'begin' or the 'end' pos respectively.
 		// 3.1. If the node defines just a prefix and it shares position with opening/closing
 		//    brackets then they take precedence over this prefix.
 		// 3.2. If the node defines just a suffix and it shares position with opening/closing
 		//    brackets then the suffix takes precedence over them.
 		// 4. Suffixes always take precedence over prefixes defined on the same pos
 		//    (we should always close a bracket before opening a new one otherwise
 		//    bracket 'crossing' will take place).
 		for (int i = begin; i <= end; i++) {
 			
 			// For every char number in the range [begin..end-1] take a look to the prefixes/suffixes
 			// defined on this pos first.
 			List<Tree> l = map.get(i);
 			if (l != null) {
 				// If there are prefixes/suffices defined on this pos and while the list of
 				// prefixes/suffixes is not empty then sort the list according to the upper rules.
 				while (l.size() > 0) {
 					Tree biggestRange = null;
 					for (Tree t: l) {
 						if (t.end == i) {
 							// 2. and 4. Suffixes always take precedence over anything else.
 							// Here 'biggestRange' should actually contain the 'smallest' range.
 							if (t.prefix == null) {
 								biggestRange = t; // 3.2. A suffix without a prefix always takes precedence over everything else.
 								break;
 							} else if (biggestRange != null) { // Here t.prefix != null, so 't' defines a range; choose the biggest one according to 2.
 								if (t.begin > biggestRange.begin) {
 									biggestRange = t; // 2. Two nodes share the same 'end', so we choose the greater 'begin'.
 								} else if (t.begin == biggestRange.begin && t.end == biggestRange.end) { // If these nodes define the same range:
 									// If 't' is below 'biggestRange' then choose 't'.
 									// This means that even 't' and 'biggestRange' define the same range
 									// we should choose the 'smaller' range in terms of bracket nesting.
 									// If 't' is below 'biggestRange' then we have put its opening bracket
 									// more recently than biggestRange's one, so we should put its closing
 									// bracket before biggestRange's one.
 									for (Tree u = t.parent; u != null; u = u.parent) {
 										if (u == biggestRange) {
 											biggestRange = t;
 											break;
 										}
 									}
 									// If we haven't chosen 't' then 'biggestRange' is below 't' and that's the node we need.
 								}
 								// Do nothing as 'biggestRange' is already the right one.
 							} else {
 								biggestRange = t; // 'biggestRange' is not yet defined, so let it become 't'.
 							}
 							
 						} else if (t.begin == i) {
 							// 1. Two nodes share the same 'begin', so the greater range should be chosen.
 							// Here 'biggestRange' is defined in the usual sense.
 							if (biggestRange != null && biggestRange.end == i) { // This prefix shares pos with a closing bracket.
 								continue; // 4. Closing brackets always take precedence over opening brackets, so skip.
 							}
 							if (t.suffix == null) {
 								if (biggestRange == null) {
 									biggestRange = t; // 3.1. All brackets take precedence over prefixes without suffixes,
 									// but as long as no opening bracket is found yet then 'biggestRange' may become 't'.
 								}
 								// If biggesttRange wasn't null then according to 3.1. an opening/closing bracket has
 								// already been found, so a prefix without a suffix is 'smaller' than a bracket,
 								// i.e. 'biggestRange' didn't change.
 							} else if (biggestRange != null) { // Here t.suffix != null, so 't' defines a range; choose the biggest one according to 1.
 								if (t.end > biggestRange.end) {
 									biggestRange = t; // 1. Two nodes share the same 'begin', so we choose the greater 'end'.
 								} else if (t.begin == biggestRange.begin && t.end == biggestRange.end) { // If these nodes define the same range:
 									// If 't' is over 'biggestRange' then choose 't'.
 									// This means that even 't' and 'biggestRange' define the same range
 									// we should choose the 'bigger' range in terms of bracket nesting.
 									// If 't' is over 'biggestRange' then we should put its opening bracket
 									// more recently than biggestRange's one.
 									for (Tree u = biggestRange.parent; u != null; u = u.parent) {
 										if (u == t) {
 											biggestRange = t;
 											break;
 										}
 									}
 									// If we haven't chosen 't' then 'biggestRange' is over 't' and that's the node we need.
 								}
 							} else {
 								biggestRange = t; // 'biggestRange' is not yet defined, so let it become 't'.
 							}
 						}
 					} // for (Tree t: l)
 					
 					// We have found the biggest range in terms of the upper rules,
 					// so now we should decide a prefix or a suffix is this by looking at 'begin' and 'end' positions.
 					if (i == biggestRange.begin) {
 						buff.append(biggestRange.prefix);
 					} else {
 						buff.append(biggestRange.suffix);
 					}
 					
 					// We are done with the node, remove it from the list.
 					l.remove(biggestRange);
 				}
 			} // if (l != null)
 			
 			// We have visualized the adequately nested prefixes and suffixes (is any).
 			// No decide whether we should visualize the node itself.
 			// If we are within the range of a hidden node then skip appending characters,
 			// otherwise append.
 			if (i < end && !(hidden != null && hidden.begin <= i && i < hidden.end)) {
 				buff.append(s.charAt(i));
 			}
 			
 			// If there are hidden nodes and we have left the current one's range then move to the next hidden node.
 			// The hidden nodes are received sorted from getHidden().
 			if (hidden != null && i >= hidden.end) {
 				hidden = hiddenIter.hasNext() ? hiddenIter.next() : null;
 			}
 		}
 		
 		// Return thus formed result.
 		return buff.toString();
 	}
 	
 	/**
 	 * Traverses the subtrees to find all nodes with defined prefix and/or suffix.<br/>
 	 * These nodes are then used in toString() as nested opening and closing brackets.
 	 * @return a map from the absolute string position to the list of nodes that have
 	 *   defined a prefix (begin=position) or a suffix (end=position).<br/> If multiple nodes
 	 *   share the same position then the list will be sorted in toString() to
 	 *   decide how the brackets nest.
 	 */
 	private HashMap<Integer, List<Tree>> getPrefixesAndSuffixes() {
 		HashMap<Integer, List<Tree>> res = new HashMap<Integer, List<Tree>>();
 		if (prefix != null || suffix != null) {
 			// If the prefix is defined then map 'begin' to this node.
 			// Prefixes are visualized before the current node, i.e. at 'begin' position.
 			if (prefix != null) {
 				List<Tree> l = new LinkedList<Tree>();
 				l.add(this);
 				res.put(begin, l);
 			}
 			
 			// If suffix is defined then map 'end' to this node.
 			// Suffixes are visualized after the current node, i.e. at 'end' position.
 			// Because 'end' is exclusive then the suffix will be displayed just after
 			// the current node.
 			if (suffix != null) {
 				List<Tree> l = new LinkedList<Tree>();
 				l.add(this);
 				res.put(end, l);
 			}
 		}
 		
 		// Do this recursively for all subtrees.
 		for (Tree b: branches) {
 			// If there is a missing subtree or a subtree that doesn't
 			// contain anything then skip it.
 			if (b == null || b.node.length() == 0) {
 				continue;
 			}
 			
 			// Get current subtree's prefixes and suffixes and
 			// merge them to all prefixes and suffixes defined
 			// on this position (if any).
 			HashMap<Integer, List<Tree>> map = b.getPrefixesAndSuffixes();
 			for (Integer n: map.keySet()) {
 				List<Tree> l1 = res.get(n);
 				List<Tree> l2 = map.get(n);
 				if (l1 != null) {
 					l1.addAll(l2);
 				} else {
 					res.put(n, l2);
 				}
 			}
 		}
 		
 		return res;
 	}
 	
 	/**
 	 * Traverses the subtrees to find all hidden nodes.
 	 * @return a list of hidden nodes which are under this node.<br/>
 	 *   These nodes are then used in toString() to define
 	 *   whether a node is going to be visualized or not.
 	 *   The nodes are naturally sorted because branches' ranges
 	 *   do not intersect each other and one branche's 'end' should be <= next branche's 'begin'.
 	 */
 	private List<Tree> getHidden() {
 		LinkedList<Tree> l = new LinkedList<Tree>();
 		if (hide) {
 			l.add(this);
 		} else {
 			for (Tree b: branches) {
 				if (b != null && b.node.length() > 0) {
 					l.addAll(b.getHidden());
 				}
 			}
 		}
 		return l;
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		return o == this;
 	}
 	
 	@Override
 	public int hashCode() {
 		return node.hashCode();
 	}
 
 }
