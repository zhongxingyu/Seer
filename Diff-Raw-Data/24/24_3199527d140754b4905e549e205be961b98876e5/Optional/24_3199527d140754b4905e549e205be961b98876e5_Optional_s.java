 package com.joeylawrance.language;
 
public class Optional extends EquivalentExpression {
 	public Optional(Parser node) {
 		this.node = node;
 		this.equivalent = Alternation.alternation(node,EmptyString.emptyString);
 	}
 }
