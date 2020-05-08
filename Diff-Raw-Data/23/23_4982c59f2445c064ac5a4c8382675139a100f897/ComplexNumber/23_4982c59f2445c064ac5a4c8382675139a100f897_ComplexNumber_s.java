 /*******************************************************************************
  * This file is protected by Copyright. 
  * Please refer to the COPYRIGHT file distributed with this source distribution.
  *
  * This file is part of REDHAWK IDE.
  *
  * All rights reserved.  This program and the accompanying materials are made available under 
  * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package mil.jpeojtrs.sca.util.math;
 
 import org.omg.CORBA.Any;
 
 /**
  * @since 3.4
  */
 public abstract class ComplexNumber {
 
 	public abstract Any toAny();
 
 	public abstract Object getValue(int index);
 
 	public abstract int getSize();
 
 	public static ComplexNumber valueOf(String type, String value) {
 		if (value == null) {
 			return null;
 		}
 		if (type.equalsIgnoreCase("double")) {
 			return ComplexDouble.valueOf(value);
 		} else if (type.equalsIgnoreCase("float")) {
 			return ComplexFloat.valueOf(value);
 		} else if (type.equalsIgnoreCase("short")) {
 			return ComplexShort.valueOf(value);
 		} else if (type.equalsIgnoreCase("long")) {
 			return ComplexLong.valueOf(value);
 		} else if (type.equalsIgnoreCase("longlong")) {
 			return ComplexLongLong.valueOf(value);
 		} else if (type.equalsIgnoreCase("ulong")) {
 			return ComplexULong.valueOf(value);
 		} else if (type.equalsIgnoreCase("ushort")) {
 			return ComplexUShort.valueOf(value);
 		} else if (type.equalsIgnoreCase("ulonglong")) {
 			return ComplexULongLong.valueOf(value);
 		} else {
 			throw new IllegalArgumentException("Unknown CORBA Type: " + type);
 		}
 	}
 
 	@Override
 	public String toString() {
 		if (getSize() == 2) {
			return getValue(0) + " + j" + getValue(1);
 		}
 		StringBuilder builder = new StringBuilder();
 		builder.append("(");
 		for (int i = 0; i < getSize(); i++) {
 			builder.append(getValue(i));
 			if (i + 1 < getSize()) {
 				builder.append(", ");
 			}
 		}
 		builder.append(")");
 		return builder.toString();
 	}
 }
