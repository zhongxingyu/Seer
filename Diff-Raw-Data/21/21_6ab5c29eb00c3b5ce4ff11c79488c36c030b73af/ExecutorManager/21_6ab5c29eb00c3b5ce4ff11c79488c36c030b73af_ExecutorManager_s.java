 /**
  * <copyright>
  *
  * Copyright (c) 2011 E.D.Willink and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   E.D.Willink - Initial API and implementation
  *
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.ocl.examples.library.executor;
 
 import org.eclipse.ocl.examples.domain.elements.DomainExpression;
 import org.eclipse.ocl.examples.domain.elements.DomainType;
 import org.eclipse.ocl.examples.domain.elements.DomainTypedElement;
 import org.eclipse.ocl.examples.domain.evaluation.DomainEvaluationEnvironment;
 import org.eclipse.ocl.examples.domain.evaluation.DomainEvaluationVisitor;
 import org.eclipse.ocl.examples.domain.evaluation.DomainEvaluator;
 import org.eclipse.ocl.examples.domain.evaluation.DomainModelManager;
 import org.eclipse.ocl.examples.domain.evaluation.EvaluationHaltedException;
 import org.eclipse.ocl.examples.domain.evaluation.InvalidEvaluationException;
 import org.eclipse.ocl.examples.domain.evaluation.InvalidValueException;
 import org.eclipse.ocl.examples.domain.library.LibraryUnaryOperation;
 import org.eclipse.ocl.examples.domain.values.CollectionValue;
 import org.eclipse.ocl.examples.domain.values.NullValue;
 import org.eclipse.ocl.examples.domain.values.Value;
 import org.eclipse.ocl.examples.domain.values.ValueFactory;
 import org.eclipse.osgi.util.NLS;
 
 public abstract class ExecutorManager implements DomainEvaluator
 {	
 	protected final ValueFactory valueFactory;
 //	protected final DomainEvaluationEnvironment evaluationEnvironment;
 
     /**
      * Set true by {@link #setCanceled} to terminate execution at next call to {@link #getValuefactory()}.
      */
 	private boolean isCanceled = false;
 	
 	public ExecutorManager(ValueFactory valueFactory) {
 		this.valueFactory = valueFactory;
 //		this.evaluationEnvironment = new ExecutorEvaluationEnvironment();
 	}
 
 	public Value evaluateIteration(DomainType returnType, CollectionValue sourceVal, DomainTypedElement accumulator,
 			DomainExpression body, DomainTypedElement[] iterators) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException();
 	}
 
 	public ValueFactory getValueFactory() {
 		if (isCanceled) {
			throw new EvaluationHaltedException("Canceled");
 		}
 		return valueFactory;
 	}
 
 	public boolean isCanceled() {
 		return isCanceled;
 	}
 
 	public void setCanceled(boolean isCanceled) {
 		this.isCanceled = isCanceled;
 	}
 	
 	public DomainType typeOf(Value value, Value... values) {
 		DomainType type = value.getType();
 		for (Value anotherValue : values) {
 			DomainType anotherType = anotherValue.getType();
 			type = type.getCommonType(valueFactory.getStandardLibrary(), anotherType);
 		}
 		return type;
 	}
 
 	public Value evaluate(DomainExpression body) {
 		try {
 			return ((LibraryUnaryOperation)body).evaluate(this, null, null);		// WIP
 		} catch (InvalidValueException e) {
 			return throwInvalidEvaluation(e);
 		}
 	}
 
 	public DomainEvaluationEnvironment getEvaluationEnvironment() {
 		throw new UnsupportedOperationException();
 	}
 
 	public DomainEvaluationVisitor getEvaluationVisitor() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException();
 	}
 
 	public DomainModelManager getModelManager() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException();
 	}
 
 	public NullValue throwInvalidEvaluation(InvalidValueException e) throws InvalidEvaluationException {
 		throw new InvalidEvaluationException(null, e);
 	}
 
 	public NullValue throwInvalidEvaluation(Throwable e, DomainExpression expression, Object context,
 			String message, Object... bindings) throws InvalidEvaluationException {
 		throw new InvalidEvaluationException(null, NLS.bind(message, bindings), e, expression, context);
 	}
 }
