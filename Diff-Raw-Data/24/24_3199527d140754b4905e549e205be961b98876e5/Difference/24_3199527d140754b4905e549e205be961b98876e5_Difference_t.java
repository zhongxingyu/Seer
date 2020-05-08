 package com.joeylawrance.language;
 
class Difference extends BinaryOperator {
 	Parser equivalent;
 	public Difference(Parser left, Parser right) {
 		super(left, right);
 		equivalent = new Intersection(left,new Complement(right));
 	}
 }
