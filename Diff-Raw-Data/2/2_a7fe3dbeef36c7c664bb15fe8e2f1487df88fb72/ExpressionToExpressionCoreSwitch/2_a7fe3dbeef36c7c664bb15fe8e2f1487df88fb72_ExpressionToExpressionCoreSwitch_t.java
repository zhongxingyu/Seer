 /*******************************************************************************
  * Copyright (c) 2013 Atos
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Arthur Daussy - initial implementation
  *******************************************************************************/
 package org.eclipse.escriptmonkey.scripting.ui.expression;
 
 import org.eclipse.core.expressions.Expression;
 import org.eclipse.core.internal.expressions.InstanceofExpression;
 import org.eclipse.core.internal.expressions.IterateExpression;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.escriptmonkey.scripting.ui.expression.coreexpression.AdaptExpression;
 import org.eclipse.escriptmonkey.scripting.ui.expression.coreexpression.AndExpression;
 import org.eclipse.escriptmonkey.scripting.ui.expression.coreexpression.CompositeExpression;
 import org.eclipse.escriptmonkey.scripting.ui.expression.coreexpression.InstanceExpression;
 import org.eclipse.escriptmonkey.scripting.ui.expression.coreexpression.IterableExpression;
 import org.eclipse.escriptmonkey.scripting.ui.expression.coreexpression.Root;
 import org.eclipse.escriptmonkey.scripting.ui.expression.coreexpression.WithExpression;
 import org.eclipse.escriptmonkey.scripting.ui.expression.coreexpression.util.CoreexpressionSwitch;
 
 
 /**
  * Partial implementation to convert Expression model element to Expression from Expression Core
  * TODO finish this
  * 
  * @author adaussy
  * 
  */
 public class ExpressionToExpressionCoreSwitch extends CoreexpressionSwitch<Expression> {
 
 	@Override
 	public Expression caseWithExpression(WithExpression object) {
 		org.eclipse.core.internal.expressions.WithExpression withExpression = new org.eclipse.core.internal.expressions.WithExpression(object.getVariable().getLiteral());
 		composeSubExpressions(object, withExpression);
 		return withExpression;
 	}
 
 	@Override
 	public Expression caseAndExpression(AndExpression object) {
 		org.eclipse.core.internal.expressions.AndExpression andExpression = new org.eclipse.core.internal.expressions.AndExpression();
 		composeSubExpressions(object, andExpression);
		return andExpression;
 	}
 
 	@Override
 	public Expression caseInstanceExpression(InstanceExpression object) {
 		return new InstanceofExpression(object.getValue());
 	}
 
 
 	@Override
 	public Expression caseAdaptExpression(AdaptExpression object) {
 		org.eclipse.core.internal.expressions.AdaptExpression adaptExpression = new org.eclipse.core.internal.expressions.AdaptExpression(object.getValue());
 		composeSubExpressions(object, adaptExpression);
 		return adaptExpression;
 	}
 
 
 	protected void composeSubExpressions(CompositeExpression object, org.eclipse.core.internal.expressions.CompositeExpression compositeExpression) {
 		for(org.eclipse.escriptmonkey.scripting.ui.expression.coreexpression.Expression e : object.getExpressions()) {
 			Expression subExpression = doSwitch(e);
 			if(subExpression != null) {
 				compositeExpression.add(subExpression);
 			}
 		}
 	}
 
 	@Override
 	public Expression caseIterableExpression(IterableExpression object) {
 		String literal = object.getOperand().getLiteral();
 		try {
 			org.eclipse.core.internal.expressions.IterateExpression iteCoreExpression = new IterateExpression(literal, "true");
 			composeSubExpressions(object, iteCoreExpression);
 			return iteCoreExpression;
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public Expression caseRoot(Root object) {
 		return doSwitch(object.getExpression());
 	}
 
 
 }
