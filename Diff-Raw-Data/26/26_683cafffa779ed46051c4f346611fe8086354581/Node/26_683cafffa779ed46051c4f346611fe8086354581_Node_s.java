 /**
  * Copyright 2012 Wim Rijnders <wrijnders@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  *=========================================================================
  */
 package nl.axizo.parser;
 
 import java.util.Vector;
 
 public class Node {
 	String key;
 	String value;
 	Vector children = new Vector();
 	Node   parent;
 
 	Node() {
 		key   = "";
 		value = "";
 	}
 
 	public Node( String key, String value ) {
 		this.key = key;
 		this.value = value;
 	}
 
 	public String getValue() { return value; }
 	public void setValue( String val) { value = val; }
 
 	public Node   getParent() { return parent; }
 	public String getKey()    { return key; }
 
 
 	/**
  	 * Change the value of the key.
  	 *
  	 * Use diligently. Changing the key may disrupt the
  	 * parsing process.
  	 *
  	 * @param val new value of the key
  	 */
 	public void setKey( String val) { key = val; }
 
 
 	public void addChild(String key, String value) {
 		addChild( new Node( key, value) );
 	}
 
 	public void addChild( Node n) {
 		n.parent = this;
 		children.add( n );
 	}
 
 	/**
  	 * Remove given Node from the current children list.
  	 */
 	public void removeChild( Node n ) throws ParseException {
 		if ( !children.remove(n) ) {
 			throw new ParseException( "Node is not child of given parent node." );
 		};
 
 		n.parent = null;
 	}
 
 	public int numChildren() { return children.size(); }
 
 	/**
 	 * Move the children of the passed node to the current node.
 	 *
  	 * All children are removed from the passed node.
 	 */ 
 	public void  addChildren( Node n) {
 
 		Vector children = n.children;
 		n.removeChildren();
 
 		for( int i = 0; i < children.size(); ++i ) {
 			Node temp = (Node) children.get( i );
 			temp.parent = this;
 		}
 
 		this.children.addAll( children );
 	}
 
 	public void removeChildren() {
 		children = new Vector();
 	}
 
 
 	/**
  	 * Get child Node by key value.
  	 *
  	 * If more than one child node has the given key value, 
  	 * this will return the first child node. In order to access
  	 * the other child nodes with the same key, it is necessary
  	 * to iterate through the child list.
  	 * <p>
  	 * If the key is not found, a null-node is returned. This 
  	 * enables chaining of get() calls. This also means that
  	 * null is never returned. Use instance method Node.isNull() 
  	 * (TODO: add referral to method isNull() here)
  	 * to check if the return value is valid.
  	 * </p>
  	 * @param key key value to search for.
  	 * @return Node instance
  	 */
 	public Node get(String key ) {
 		for ( int i =0; i < children.size(); ++i ) {
 			Node n = (Node) children.get(i);
 
 			if ( n.key.equals( key ) ) {
 				return n;
 			}
 		}
 
 		// Return a null-node to avoid nullPointerExceptions 
 		// on chained get() calls.
 		return new Node() {
 			public boolean isNull() { return true; }
 		};
 	}
 
 
 	/**
  	 * Get child Node by index into list.
  	 */
 	public Node get(int n ) {
 		return (Node) children.get(n);
 	}
 
 
 	public Node set( Node n ) {
 		Node help = get( n.key );
 		if ( help.isNull() ) {
 			addChild( n );
 			help = n;
 		} else {
 			help.setValue( n.getValue() );
 		}
 
 		return help;
 	}
 
 	public Node set( String key, String value ) {
 		Node n = new Node( key, value );
 		n.parent = this;
 		return set( n );
 	}
 
 	public Node set( String key ) {
 		return set( key, "" );
 	}
 
 
 	/**
  	 * Output contents of a node.
  	 *
  	 * Output the key and value of the current node, and the keys
  	 * and values of the child nodes recursively. The contents
  	 * of the child nodes are shown indented by tabs relative to
  	 * the current node.
  	 *
  	 * @param tabs number of spaces to indent node information
  	 * @param showFirstTwoLines if true, show only first two lines of value
  	 * @return textual representation of node
  	 */
 	public String show(int tabs, boolean showFirstTwoLines) {
 		String tab = Util.makeTab( tabs );
 		String showVal = value;
 
		if ( showFirstTwoLines ) {
			showVal = truncTwoLines( showVal );
 		}
 
 		String ret = tab + key + ": " + showVal + "\n";
 
 		for ( int i =0; i < children.size(); ++i ) {
 			Node n = (Node) children.get(i);
 			ret += n.show( tabs + 1, showFirstTwoLines );
 		}
 
 		return ret;
 	}
 
 	/**
  	 * Output contents of a node.
  	 *
  	 * Override of {@link #show(int,boolean) show(int,boolean)},
  	 * without indenting and showing entire value of node.
  	 */
 	public String show() {
 		return show( 0, false );
 	}
 
 	/**
  	 * Output contents of a node.
  	 *
  	 * Override of {@link #show(int,boolean) show(int,boolean)},
  	 * showing entire value of node.
  	 */
 	public String show( boolean showFirstTwoLines) {
 		return show( 0, showFirstTwoLines );
 	}
 
 
 	/**
 	 * Return the first two lines of given value.
 	 *
 	 * Returns all content if less than two lines present.
 	 */
 	private String truncTwoLines(String val ) {
 		String ret = val;
 
 		int ind1 = val.indexOf("\n");
 		if ( ind1 != -1 ) {
 			int ind2 = val.substring(ind1 +1).indexOf("\n");
 			if ( ind2 != -1 ) {
 				ind1 += ind2 + 1;
 			}
 
 			ret = val.substring(0, ind1 );
 		}
 
 		return ret;
 	}
 
 
 	/**
 	 * Collect all values under this node recursively.
 	 *
 	 * Concatenate the value of this node and all the children recursively
 	 * to a single string, and replace the value of this node with this string.
 	 * All children will be removed.
 	 */
 	public void collect() {
 		String str = value;
 
 		for ( int i =0; i < children.size(); ++i ) {
 			Node n = (Node) children.get(i);
 			n.collect();
 			str += n.value;
 		}
 		children = new Vector();
 
 		value = str;
 	}
 
 	
 	/**
  	 * Search nodes depth-first and return all nodes
  	 * with the key equal to the given parameter.
  	 *
  	 * @param label key to search for
  	 */
 	public Vector findNodes( String label ) {
 		Vector ret = new Vector();
 
 		for ( int i =0; i < children.size(); ++i ) {
 			Node n = (Node) children.get(i);
 			ret.addAll( n.findNodes(label) );
 		}
 
 		if ( key.equals( label ) ) {
 			ret.add( this );
 		}
 
 		return ret;
 	} 
 
 	/**
  	 * Search nodes depth-first and return all nodes
  	 * with the value equal to the given parameter.
  	 *
  	 * @param value Value to search for
  	 */
 	public Vector findNodesByValue( String value ) {
 		Vector ret = new Vector();
 
 		for ( int i =0; i < children.size(); ++i ) {
 			Node n = (Node) children.get(i);
 			ret.addAll( n.findNodesByValue(value) );
 		}
 
 		if ( this.value.equals( value ) ) {
 			ret.add( this );
 		}
 
 		return ret;
 	} 
 
 
 	public String toString() {
 		return key + ": " + value + ": " + children.size() + " children";
 	}
 
 	public boolean isNull() { return false; }
 }
