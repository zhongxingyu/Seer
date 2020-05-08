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
 package org.eclipse.ocl.examples.domain.values.impl;
 
 import java.math.BigDecimal;
 
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
 import org.eclipse.ocl.examples.domain.values.UnlimitedValue;
 import org.eclipse.ocl.examples.domain.values.Value;
 import org.eclipse.ocl.examples.domain.values.ValuesPackage;
 
 /**
  * @generated NOT
  */
 public abstract class IntegerValueImpl extends ValueImpl implements IntegerValue
 {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ValuesPackage.Literals.INTEGER_VALUE;
 	}
 
 	public @NonNull RealValue addReal(@NonNull RealValue rightValue) {
 		try {
 			@SuppressWarnings("null") @NonNull BigDecimal result = bigDecimalValue().add(rightValue.bigDecimalValue());
 			return realValueOf(result);
 		} catch (InvalidValueException e) {
 			throw new InvalidValueException(EvaluatorMessages.InvalidReal, e, null, rightValue);
 		}
 	}
 	
 	@Override
 	public @NonNull IntegerValue asIntegerValue() {
 		return this;
 	}
 
 	@Override
 	public @NonNull RealValue asRealValue() {
 		return this;
 	}
 
 	@Override
 	public @NonNull Value asUnlimitedNaturalValue() {
 		if (isUnlimitedNatural()) {
 			return this;
 		}
 		else {
 			return super.asUnlimitedNaturalValue();
 		}
 	}
 
 	public @NonNull RealValue commutatedAdd(@NonNull RealValue left) {
 		return left.addInteger(this);
 	}
 
 	public @NonNull IntegerValue commutatedDiv(@NonNull IntegerValue left) {
 		return left.divInteger(this);
 	}
 
 	public @NonNull RealValue commutatedDivide(@NonNull RealValue left) {
 		return left.divideInteger(this);
 	}
 
 	public @NonNull IntegerValue commutatedMod(@NonNull IntegerValue left) {
 		return left.modInteger(this);
 	}
 
 	public @NonNull RealValue commutatedMultiply(@NonNull RealValue left) {
 		return left.multiplyInteger(this);
 	}
 
 	public @NonNull RealValue commutatedSubtract(@NonNull RealValue left) {
 		return left.subtractInteger(this);
 	}
 
	public int compareTo(/*@NonNull*/ RealValue left) {
 		return -left.compareToInteger(this);
 	}
 
 	public int compareToReal(@NonNull RealValue o) {
 		return bigDecimalValue().compareTo(o.bigDecimalValue());
 	}
 
 	public int compareToUnlimited(@NonNull UnlimitedValue right) {
 		return -1;
 	}
 
 	public @NonNull IntegerValue divUnlimited(@NonNull UnlimitedValue right) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "div", "UnlimitedValue");
 	}
 
 	public @NonNull RealValue divideReal(@NonNull RealValue right) {
 		return RealValueImpl.divideBigDecimal(bigDecimalValue(), right.bigDecimalValue());
 	}
 
 	public @NonNull IntegerValue floor() {
 		return this;
 	}
 
 	public @NonNull DomainType getType(@NonNull DomainStandardLibrary standardLibrary) {
 		return isUnlimitedNatural() ? standardLibrary.getUnlimitedNaturalType() : standardLibrary.getIntegerType();
 	}
 
 	public @NonNull TypeId getTypeId() {
 		return isUnlimitedNatural() ? TypeId.UNLIMITED_NATURAL : TypeId.INTEGER;
 	}
 	
 	public @Nullable IntegerValue isIntegerValue() {
 		return this;
 	}
 
 	public boolean isUnlimited() {
 		return false;
 	}
 	
 	public boolean isUnlimitedNatural() {
 		return false;
 	}
 
 	public @NonNull RealValue max(@NonNull RealValue rightValue) {
 		return rightValue.maxInteger(this);
 	}
 
 	public @NonNull RealValue maxReal(@NonNull RealValue right) {
 		BigDecimal bigDecimalValue = bigDecimalValue();
 		return bigDecimalValue.compareTo(right.bigDecimalValue()) > 0 ? realValueOf(bigDecimalValue) : right;
 	}
 
 	public @NonNull RealValue maxUnlimited(@NonNull UnlimitedValue rightValue) {
 		return rightValue;
 	}
 
 	public @NonNull RealValue min(@NonNull RealValue rightValue) {
 		return rightValue.minInteger(this);
 	}
 
 	public @NonNull RealValue minReal(@NonNull RealValue right) {
 		BigDecimal bigDecimalValue = bigDecimalValue();
 		return bigDecimalValue.compareTo(right.bigDecimalValue()) < 0 ? realValueOf(bigDecimalValue) : right;
 	}
 
 	public @NonNull RealValue minUnlimited(@NonNull UnlimitedValue rightValue) {
 		return this;
 	}
 
 	public @NonNull IntegerValue modUnlimited(@NonNull UnlimitedValue right) {
 		throw new InvalidValueException(EvaluatorMessages.InvalidOperation, "mod", "UnlimitedValue");
 	}
 
 	public @NonNull RealValue multiplyReal(@NonNull RealValue rightValue) {
 		try {
 			@SuppressWarnings("null") @NonNull BigDecimal result = bigDecimalValue().multiply(rightValue.bigDecimalValue());
 			return realValueOf(result);
 		} catch (InvalidValueException e) {
 			throw new InvalidValueException(EvaluatorMessages.InvalidReal, e, null, rightValue);
 		}
 	}
 
 	public @NonNull IntegerValue round() {
 		return this;
 	}
 
 	public @NonNull RealValue subtractReal(@NonNull RealValue rightValue) {
 		try {
 			@SuppressWarnings("null") @NonNull BigDecimal result = bigDecimalValue().subtract(rightValue.bigDecimalValue());
 			return realValueOf(result);
 		} catch (InvalidValueException e) {
 			throw new InvalidValueException(EvaluatorMessages.InvalidReal, e, null, rightValue);
 		}
 	}
 }
