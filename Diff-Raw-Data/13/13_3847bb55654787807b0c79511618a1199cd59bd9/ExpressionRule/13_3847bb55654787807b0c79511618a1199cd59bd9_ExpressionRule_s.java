 /*
  * Copyright (C) 2008 Steve Ratcliffe
  * 
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 2 as
  *  published by the Free Software Foundation.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  * 
  * Author: Steve Ratcliffe
  * Create date: 07-Nov-2008
  */
 package uk.me.parabola.mkgmap.osmstyle;
 
 import uk.me.parabola.mkgmap.osmstyle.eval.Op;
 import uk.me.parabola.mkgmap.reader.osm.Element;
 import uk.me.parabola.mkgmap.reader.osm.GType;
 import uk.me.parabola.mkgmap.reader.osm.Rule;
 import uk.me.parabola.mkgmap.reader.osm.TypeResult;
 
 /**
  * A rule that contains a condition.  If the condition is matched by the
  * element then the finalize rule is executed and the held gtype is returned.
  * 
  * @author Steve Ratcliffe
  */
 public class ExpressionRule implements Rule {
 	private final Op expression;
 	private final GType gtype;
 	private Rule finalizeRule;
 
 	/** Finalize rules must not have an element type definition so the add method must never be called. */
 	private final static TypeResult finalizeTypeResult = new TypeResult() {
 		public void add(Element el, GType type) {
 			throw new UnsupportedOperationException("Finalize rules must not contain an action block.");
 		}
 	};
 	
 	public ExpressionRule(Op expression, GType gtype) {
 		this.expression = expression;
 		this.gtype = gtype;
 	}
 
 	public void resolveType(Element el, TypeResult result) {
 		if (expression.eval(el)) {
 			// expression matches
 			if (finalizeRule != null) {
 				// run the finalize rules
 				finalizeRule.resolveType(el, finalizeTypeResult);
 			}
 			result.add(el, gtype);
 		}
 	}
 
 	public String toString() {
 		return expression.toString() + ' ' + gtype;
 	}
 
 	public void setFinalizeRule(Rule finalizeRule) {
 		this.finalizeRule = finalizeRule;
 	}
 }
