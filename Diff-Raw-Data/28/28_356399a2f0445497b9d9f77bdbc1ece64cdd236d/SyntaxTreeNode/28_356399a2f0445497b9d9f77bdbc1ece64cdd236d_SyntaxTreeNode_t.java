 package org.libtoil.core;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.libtoil.core.TemplateTokenizer.Token;
 import org.libtoil.core.TemplateTokenizer.TokenType;
 
 public class SyntaxTreeNode implements Iterable<SyntaxTreeNode> {
 	SyntaxTreeNode parent, nextSibling, firstChild, lastChild;
 	Token token;
 	
 	SyntaxTreeNode(Token t) {
 		this.token = t;
 	}
 	
 	void addChild(SyntaxTreeNode n) {
 		n.parent = this;
 		if (this.firstChild == null) {
 			this.firstChild = n;
 		} else {
 			this.lastChild.nextSibling = n;
 		}
 		this.lastChild = n;
 	}
 	 
 	boolean equals(SyntaxTreeNode n) {
 		return this == n;
 	}
 	
 	boolean equalsTree(SyntaxTreeNode n) {
 		throw new UnsupportedOperationException();
 	}
 	  
 	SyntaxTreeNode getFirstChild() {
 		return firstChild;
 	}
 	
 	SyntaxTreeNode getNextSibling() {
 		return nextSibling;
 	}
 	
 	int childCount () {
 		int count = 0;
 		SyntaxTreeNode n = firstChild;
 		while (n != null) {
 			n = n.nextSibling;
 			count++;
 		}
 		return count;
 	}
 	
 	String getSource() {
 		return token.toString();
 	}
 	
 	int getTokenEnd() {
 		if (lastChild == null) {
 			return token.end;
 		} else {
 			return lastChild.getTokenEnd();
 		}
 	}
 	
 	String getFullSource() {
 		return new String(token.getSource(), token.start, getTokenEnd()-token.start);	
 	}
 	
 	TokenType getType() {
 		return token.type;
 	}
 
 	public Iterable<Token> getTokens() {
 		List<Token> result = new ArrayList<Token>();
     	for(SyntaxTreeNode node: SyntaxTreeNode.this) {
    		System.out.println("-" + node.token);
     		result.add(node.token);
     	}
     	return result;
 	}
 	
 	public class DepthFirstIterator implements Iterator<SyntaxTreeNode> {
 		SyntaxTreeNode nextNode;
		Iterator<SyntaxTreeNode> it;
 		
 		public DepthFirstIterator() {
 			nextNode = SyntaxTreeNode.this;
 		}
 		
 		public boolean hasNext() {
			return nextNode != null || it != null && it.hasNext();
 		}
 
 		public SyntaxTreeNode next() {
 			if (SyntaxTreeNode.this == nextNode) {
 				nextNode = firstChild;
 				return SyntaxTreeNode.this;
 			} 
			if (it == null || !it.hasNext()) {
				it = nextNode.new DepthFirstIterator();
 				nextNode = nextNode.nextSibling;
 			}
			return it.next();
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	public Iterator<SyntaxTreeNode> iterator() {
 		return new DepthFirstIterator();
 	}
 }
