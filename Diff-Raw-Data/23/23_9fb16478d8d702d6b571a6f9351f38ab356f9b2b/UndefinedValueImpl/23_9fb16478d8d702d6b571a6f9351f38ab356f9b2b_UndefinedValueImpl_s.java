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
  * $Id: UndefinedValueImpl.java,v 1.6 2011/05/07 16:41:20 ewillink Exp $
  */
 package org.eclipse.ocl.examples.domain.values.impl;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
 import org.eclipse.ocl.examples.domain.elements.DomainType;
 import org.eclipse.ocl.examples.domain.evaluation.InvalidValueException;
 import org.eclipse.ocl.examples.domain.ids.OclVoidTypeId;
 import org.eclipse.ocl.examples.domain.ids.TuplePartId;
 import org.eclipse.ocl.examples.domain.messages.EvaluatorMessages;
 import org.eclipse.ocl.examples.domain.values.IntegerValue;
 import org.eclipse.ocl.examples.domain.values.NullValue;
 import org.eclipse.ocl.examples.domain.values.RealValue;
 import org.eclipse.ocl.examples.domain.values.UnlimitedValue;
 import org.eclipse.ocl.examples.domain.values.Value;
 
 /**
  * @generated NOT
  */
 public abstract class UndefinedValueImpl extends ValueImpl implements NullValue
 {	
 	protected UndefinedValueImpl() {}
 
 	public @NonNull NullValue abs() {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue addInteger(@NonNull IntegerValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue addReal(@NonNull RealValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull Number asNumber() {
 		throw new InvalidValueException("undefined value has no Number value");
 	}
 
 	public @NonNull Object asObject() {
 		return toInvalidValue();
 	}
 
 	public @NonNull BigDecimal bigDecimalValue() {
 		throw new InvalidValueException("undefined value has no BigDecimal value");
 	}
 
 	public @NonNull BigInteger bigIntegerValue() {
 		throw new InvalidValueException("undefined value has no BigInteger value");
 	}
 	
 	public @NonNull RealValue commutatedAdd(@NonNull RealValue left) {
 		return toInvalidValue();
 	}
 	
 	public @NonNull IntegerValue commutatedDiv(@NonNull IntegerValue left) {
 		return toInvalidValue();
 	}
 	
 	public @NonNull RealValue commutatedDivide(@NonNull RealValue left) {
 		return toInvalidValue();
 	}
 	
 	public @NonNull IntegerValue commutatedMod(@NonNull IntegerValue left) {
 		return toInvalidValue();
 	}
 	
 	public @NonNull RealValue commutatedMultiply(@NonNull RealValue left) {
 		return toInvalidValue();
 	}
 	
 	public @NonNull RealValue commutatedSubtract(@NonNull RealValue left) {
 		return toInvalidValue();
 	}
 
	public int compareTo(RealValue o) {
 		throw new UnsupportedOperationException("UndefinedValueImpl.compareTo");
 	}
 
 	public int compareToInteger(@NonNull IntegerValue right) {
 		throw new UnsupportedOperationException("UndefinedValueImpl.compareTo");
 	}
 
 	public int compareToReal(@NonNull RealValue right) {
 		throw new UnsupportedOperationException("UndefinedValueImpl.compareTo");
 	}
 
 	public int compareToUnlimited(@NonNull UnlimitedValue right) {
 		throw new UnsupportedOperationException("UndefinedValueImpl.compareTo");
 	}
 
 	public @NonNull NullValue div(@NonNull IntegerValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue divInteger(@NonNull IntegerValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue divUnlimited(@NonNull UnlimitedValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull RealValue divideInteger(@NonNull IntegerValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue divideReal(@NonNull RealValue right) {
 		return toInvalidValue();
 	}
 
 	public double doubleValue() {
 		throw new UnsupportedOperationException("InvalidValue.compareTo");
 	}
 
 	public @NonNull NullValue floor() {
 		return toInvalidValue();
 	}
 
 	public DomainType getElement() {
 		return null;
 	}
 
 	public @NonNull DomainType getInstanceType() {
 		throw new InvalidValueException("undefined value has no instance type");
 	}
 
 	public Object getObject() {
 		return null;
 	}
 
 	public abstract @NonNull OclVoidTypeId getTypeId();
 
 	public @NonNull Value getValue(@NonNull TuplePartId partId) {
     	return toInvalidValue();
 	}
 
 	public @NonNull Object getValue(int index) {
     	return toInvalidValue();
 	}
 
 	public boolean isFalse() {
 		return false;
 	}
 	
 	public @Nullable IntegerValue isIntegerValue() {
 		return null;
 	}
 
 	public boolean isTrue() {
 		return false;
 	}
 
 	@Override
 	public boolean isUndefined() {
 		return true;
 	}
 
 	public boolean isUnlimited() {
 		return false;
 	}
 	
 	public boolean isUnlimitedNatural() {
 		return false;
 	}
 
 	public @NonNull NullValue max(@NonNull RealValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue maxInteger(@NonNull IntegerValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue maxReal(@NonNull RealValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue maxUnlimited(@NonNull UnlimitedValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue min(@NonNull RealValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue minInteger(@NonNull IntegerValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue minReal(@NonNull RealValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue minUnlimited(@NonNull UnlimitedValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue modInteger(@NonNull IntegerValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue modUnlimited(@NonNull UnlimitedValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue multiplyInteger(@NonNull IntegerValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue multiplyReal(@NonNull RealValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue negate() {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue round() {
 		return toInvalidValue();
 	}
 
 	public int signum() {
 		throw new UnsupportedOperationException("InvalidValue.compareTo");
 	}
 
 	public @NonNull String stringValue() {
 		throw new InvalidValueException("undefined value has no String value");
 	}
 
 	public @NonNull NullValue subtractInteger(@NonNull IntegerValue right) {
 		return toInvalidValue();
 	}
 
 	public @NonNull NullValue subtractReal(@NonNull RealValue right) {
 		return toInvalidValue();
 	}
 
 	protected @NonNull NullValue toInvalidValue() {
 		throw new InvalidValueException(EvaluatorMessages.ConvertibleValueRequired, "Invalid");
 	}
 }
