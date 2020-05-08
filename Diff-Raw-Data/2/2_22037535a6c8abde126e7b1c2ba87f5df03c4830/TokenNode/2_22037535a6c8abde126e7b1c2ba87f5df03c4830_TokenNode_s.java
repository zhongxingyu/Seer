 /**
  * 
  */
 package com.kevlindev.tokens;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import beaver.Symbol;
 
import com.kevlindev.text.SourceBuilder;
 import com.kevlindev.utils.StringUtils;
 
 /**
  * @author Kevin Lindsey
  */
 public class TokenNode extends Symbol {
 	private Symbol symbol;
 	private TokenType type;
 	private TokenNode parent;
 	private List<TokenNode> children;
 
 	public TokenNode(Symbol symbol) {
 		this.symbol = symbol;
 		this.type = TokenType.getTokenType(symbol.getId());
 	}
 
 	public TokenNode(TokenType type) {
 		this.symbol = new Symbol(type.getIndex());
 		this.type = type;
 	}
 
 	public void addChild(TokenNode child) {
 		if (children == null) {
 			children = new ArrayList<TokenNode>();
 		}
 
 		children.add(child);
 		child.setParent(this);
 	}
 
 	public void addChildren(List<TokenNode> children) {
 		// HACK: adding no children will create the backing array which is used to indicate that this is not a leaf node
 		// even though it has no children
 		if (this.children == null) {
 			this.children = new ArrayList<TokenNode>();
 		}
 
 		if (children != null) {
 			for (TokenNode child : children) {
 				addChild(child);
 			}
 		}
 	}
 
 	public List<TokenNode> getChildren() {
 		if (children == null) {
 			return Collections.emptyList();
 		} else {
 			return children;
 		}
 	}
 
 	public TokenNode getFirstChild() {
 		return (children != null && !children.isEmpty()) ? children.get(0) : null;
 	}
 
 	public TokenNode getParent() {
 		return parent;
 	}
 
 	public Symbol getSymbol() {
 		return symbol;
 	}
 
 	public String getText() {
 		Object value = getValue();
 
 		return (value != null) ? value.toString() : StringUtils.EMPTY;
 	}
 
 	public TokenType getType() {
 		return type;
 	}
 
 	public Object getValue() {
 		return (symbol != null) ? symbol.value : null;
 	}
 
 	public boolean hasChildren() {
 		return (children != null && !children.isEmpty());
 	}
 
 	protected void setParent(TokenNode parent) {
 		this.parent = parent;
 	}
 
 	public String toString() {
 		SourceBuilder builder = new SourceBuilder();
 
 		toString(builder);
 
 		return builder.toString();
 	}
 
 	private void toString(SourceBuilder builder) {
 		builder.printWithIndent("(").print(getType().toString());
 
 		if (children != null) {
 			int size = children.size();
 
 			if (size > 0) {
 				builder.println().indent();
 
 				for (int i = 0; i < size; i++) {
 					TokenNode child = children.get(i);
 
 					child.toString(builder);
 
 					if (i < size - 1) {
 						builder.println();
 					}
 				}
 
 				builder.dedent();
 			}
 		} else {
 			if (symbol != null && symbol.value != null) {
 				builder.print(" ").print(symbol.value.toString());
 			}
 		}
 
 		builder.print(")");
 	}
 }
