 /**
  * <copyright>
  *
  * Copyright (c) 2010,2011 E.D.Willink and others.
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
  * $Id: UnlimitedValueImpl.java,v 1.5 2011/05/07 16:41:16 ewillink Exp $
  */
 /**
  * <copyright>
  *
  * Copyright (c) 2010,2011 E.D.Willink and others.
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
  * $Id: UnlimitedValueImpl.java,v 1.5 2011/05/07 16:41:16 ewillink Exp $
  */
 package org.eclipse.ocl.examples.domain.values.impl;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
 import org.eclipse.ocl.examples.domain.elements.DomainStandardLibrary;
 import org.eclipse.ocl.examples.domain.elements.DomainType;
 import org.eclipse.ocl.examples.domain.evaluation.InvalidValueException;
 import org.eclipse.ocl.examples.domain.ids.TypeId;
 import org.eclipse.ocl.examples.domain.messages.EvaluatorMessages;
 import org.eclipse.ocl.examples.domain.values.IntegerValue;
 import org.eclipse.ocl.examples.domain.values.RealValue;
 import org.eclipse.ocl.examples.domain.values.Unlimited;
 import org.eclipse.ocl.examples.domain.values.UnlimitedValue;
 import org.eclipse.ocl.examples.domain.values.Value;
 import org.eclipse.ocl.examples.domain.values.ValuesPackage;
 
 /**
  * @generated NOT
  */
 public class UnlimitedValueImpl extends ValueImpl implements UnlimitedValue
 {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ValuesPackage.Literals.UNLIMITED_VALUE;
 	}
 
 	public UnlimitedValueImpl() {}
 
 	public @NonNull UnlimitedValueImpl abs() {
 		return this;
 	}
 
 	public @NonNull IntegerValue addInteger(@NonNull IntegerValue right) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "add", "UnlimitedValue");
 	}
 
 	public @NonNull RealValue addReal(@NonNull RealValue right) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "add", "UnlimitedValue");
 	}
 
 	public @NonNull Object asObject() {
 		return Unlimited.INSTANCE;
 	}
 
 	public @NonNull Number asNumber() {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "asNumber", "UnlimitedValue");
 	}
 
 	@Override
 	public @NonNull RealValue asRealValue() {
 		return this;
 	}
 
 	@Override
 	public @NonNull Value asUnlimitedNaturalValue() {
 		return this;
 	}
 
 	public @NonNull BigDecimal bigDecimalValue() {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "bigDecimalValue", "UnlimitedValue");
 	}
 
 	public @NonNull BigInteger bigIntegerValue() {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "bigIntegerValue", "UnlimitedValue");
 	}
 
 	public @NonNull RealValue commutatedAdd(@NonNull RealValue left) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "add", "UnlimitedValue");
 	}
 
 	public @NonNull IntegerValue commutatedDiv(@NonNull IntegerValue left) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "div", "UnlimitedValue");
 	}
 
 	public @NonNull RealValue commutatedDivide(@NonNull RealValue left) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "divide", "UnlimitedValue");
 	}
 
 	public @NonNull IntegerValue commutatedMod(@NonNull IntegerValue left) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "mod", "UnlimitedValue");
 	}
 
 	public @NonNull RealValue commutatedMultiply(@NonNull RealValue left) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "multiply", "UnlimitedValue");
 	}
 
 	public @NonNull RealValue commutatedSubtract(@NonNull RealValue left) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "subtract", "UnlimitedValue");
 	}
 
	public int compareTo(/*@NonNull*/ RealValue left) {
 		return -left.compareToUnlimited(this);
 	}
 
 	public int compareToInteger(@NonNull IntegerValue right) {
 		return 1;
 	}
 
 	public int compareToReal(@NonNull RealValue right) {
 		return 1;
 	}
 
 	public int compareToUnlimited(@NonNull UnlimitedValue right) {
 		return 0;
 	}
 
 	public @NonNull IntegerValue divInteger(@NonNull IntegerValue right) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "div", "UnlimitedValue");
 	}
 
 	public @NonNull IntegerValue divUnlimited(@NonNull UnlimitedValue right) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "div", "UnlimitedValue");
 	}
 
 	public @NonNull RealValue divideInteger(@NonNull IntegerValue right) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "divide", "UnlimitedValue");
 	}
 
 	public @NonNull RealValue divideReal(@NonNull RealValue right) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "divide", "UnlimitedValue");
 	}
 
 	public double doubleValue() {
 		throw new UnsupportedOperationException(getClass().getName()+ ".doubleValue");
 	}
 
 	public @NonNull IntegerValue floor() {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "floor", "UnlimitedValue");
 	}
 
 	public @NonNull DomainType getType(@NonNull DomainStandardLibrary standardLibrary) {
 		return standardLibrary.getUnlimitedNaturalType();
 	}
 
 	public @NonNull TypeId getTypeId() {
 		return TypeId.UNLIMITED_NATURAL;
 	}
 
 	public int intValue() {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "intValue", "UnlimitedValue");
 	}
 	
 	public @Nullable IntegerValue isIntegerValue() {
 		return null;
 	}
 
 	public boolean isUnlimited() {
 		return true;
 	}
 
 	public boolean isUnlimitedNatural() {
 		return true;
 	}
 
 	public @NonNull RealValue max(@NonNull RealValue rightValue) {
 		return this;
 	}
 
 	public @NonNull IntegerValue maxInteger(@NonNull IntegerValue right) {
 		return this;
 	}
 
 	public @NonNull RealValue maxReal(@NonNull RealValue right) {
 		return this;
 	}
 
 	public @NonNull RealValue maxUnlimited(@NonNull UnlimitedValue rightValue) {
 		return this;
 	}
 
 	public @NonNull RealValue min(@NonNull RealValue rightValue) {
 		return rightValue;
 	}
 
 	public @NonNull RealValue minUnlimited(@NonNull UnlimitedValue rightValue) {
 		return this;
 	}
 
 	public @NonNull IntegerValue minInteger(@NonNull IntegerValue right) {
 		return right;
 	}
 
 	public @NonNull RealValue minReal(@NonNull RealValue right) {
 		return right;
 	}
 
 	public @NonNull IntegerValue modInteger(@NonNull IntegerValue right) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "mod", "UnlimitedValue");
 	}
 
 	public @NonNull IntegerValue modUnlimited(@NonNull UnlimitedValue right) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "mod", "UnlimitedValue");
 	}
 
 	public @NonNull IntegerValue multiplyInteger(@NonNull IntegerValue right) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "multiply", "UnlimitedValue");
 	}
 
 	public @NonNull RealValue multiplyReal(@NonNull RealValue right) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "multiply", "UnlimitedValue");
 	}
 
 	public @NonNull UnlimitedValue negate() {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "negate", "UnlimitedValue");
 	}
 
 	public @NonNull IntegerValue round() {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "round", "UnlimitedValue");
 	}
 
 	public int signum() {
 		return 1;
 	}
 
 	public @NonNull IntegerValue subtractInteger(@NonNull IntegerValue right) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "subtract", "UnlimitedValue");
 	}
 
 	public @NonNull RealValue subtractReal(@NonNull RealValue right) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "subtract", "UnlimitedValue");
 	}
 
 	@Override
 	public String toString() {
 		return "*";
 	}
 }
