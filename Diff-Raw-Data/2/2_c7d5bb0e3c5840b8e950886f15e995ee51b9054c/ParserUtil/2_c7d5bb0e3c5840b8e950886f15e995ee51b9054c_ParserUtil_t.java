 /**
  * Copyright (c) 2006 Eclipse.org
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    bblajer - initial API and implementation
  */
 package org.eclipse.gmf.runtime.lite.services;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EEnumLiteral;
 
 
 public class ParserUtil {
 	private ParserUtil() {
 	}
 
 	public static Object parseValue(EAttribute feature, Object value) throws IllegalArgumentException {
 		if (value == null) {
			return null;
 		}
 		EDataType type = feature.getEAttributeType();
 		Class iClass = type.getInstanceClass();
 		if (Boolean.TYPE.equals(iClass) || Boolean.class.equals(iClass)) {
 			if (value instanceof Boolean) {
 				// ok
 			} else if (value instanceof String) {
 				value = Boolean.valueOf((String) value);
 			} else {
 				throw new IllegalArgumentException("Value of type Boolean is expected");
 			}
 		} else if (Character.TYPE.equals(iClass) || Character.class.equals(iClass)) {
 			if (value instanceof Character) {
 				// ok
 			} else if (value instanceof String) {
 				String s = (String) value;
 				if (s.length() == 0) {
 					throw new IllegalArgumentException();	//XXX: ?
 				} else {
 					value = new Character(s.charAt(0));
 				}
 			} else {
 				throw new IllegalArgumentException("Value of type Character is expected");
 			}
 		} else if (Byte.TYPE.equals(iClass) || Byte.class.equals(iClass)) {
 			if (value instanceof Byte) {
 				// ok
 			} else if (value instanceof Number) {
 				value = new Byte(((Number) value).byteValue());
 			} else if (value instanceof String) {
 				String s = (String) value;
 				if (s.length() == 0) {
 					value = null;
 				} else {
 					try {
 						value = Byte.valueOf(s);
 					} catch (NumberFormatException nfe) {
 						throw new IllegalArgumentException("String value does not convert to Byte value");
 					}
 				}
 			} else {
 				throw new IllegalArgumentException("Value of type Byte is expected");
 			}
 		} else if (Short.TYPE.equals(iClass) || Short.class.equals(iClass)) {
 			if (value instanceof Short) {
 				// ok
 			} else if (value instanceof Number) {
 				value = new Short(((Number) value).shortValue());
 			} else if (value instanceof String) {
 				String s = (String) value;
 				if (s.length() == 0) {
 					value = null;
 				} else {
 					try {
 						value = Short.valueOf(s);
 					} catch (NumberFormatException nfe) {
 						throw new IllegalArgumentException("String value does not convert to Short value");
 					}
 				}
 			} else {
 				throw new IllegalArgumentException("Value of type Short is expected");
 			}
 		} else if (Integer.TYPE.equals(iClass) || Integer.class.equals(iClass)) {
 			if (value instanceof Integer) {
 				// ok
 			} else if (value instanceof Number) {
 				value = new Integer(((Number) value).intValue());
 			} else if (value instanceof String) {
 				String s = (String) value;
 				if (s.length() == 0) {
 					value = null;
 				} else {
 					try {
 						value = Integer.valueOf(s);
 					} catch (NumberFormatException nfe) {
 						throw new IllegalArgumentException("String value does not convert to Integer value");
 					}
 				}
 			} else {
 				throw new IllegalArgumentException("Value of type Integer is expected");
 			}
 		} else if (Long.TYPE.equals(iClass) || Long.class.equals(iClass)) {
 			if (value instanceof Long) {
 				// ok
 			} else if (value instanceof Number) {
 				value = new Long(((Number) value).longValue());
 			} else if (value instanceof String) {
 				String s = (String) value;
 				if (s.length() == 0) {
 					value = null;
 				} else {
 					try {
 						value = Long.valueOf(s);
 					} catch (NumberFormatException nfe) {
 						throw new IllegalArgumentException("String value does not convert to Long value");
 					}
 				}
 			} else {
 				throw new IllegalArgumentException("Value of type Long is expected");
 			}
 		} else if (Float.TYPE.equals(iClass) || Float.class.equals(iClass)) {
 			if (value instanceof Float) {
 				// ok
 			} else if (value instanceof Number) {
 				value = new Float(((Number) value).floatValue());
 			} else if (value instanceof String) {
 				String s = (String) value;
 				if (s.length() == 0) {
 					value = null;
 				} else {
 					try {
 						value = Float.valueOf(s);
 					} catch (NumberFormatException nfe) {
 						throw new IllegalArgumentException("String value does not convert to Float value");
 					}
 				}
 			} else {
 				throw new IllegalArgumentException("Value of type Float is expected");
 			}
 		} else if (Double.TYPE.equals(iClass) || Double.class.equals(iClass)) {
 			if (value instanceof Double) {
 				// ok
 			} else if (value instanceof Number) {
 				value = new Double(((Number) value).doubleValue());
 			} else if (value instanceof String) {
 				String s = (String) value;
 				if (s.length() == 0) {
 					value = null;
 				} else {
 					try {
 						value = Double.valueOf(s);
 					} catch (NumberFormatException nfe) {
 						throw new IllegalArgumentException("String value does not convert to Double value");
 					}
 				}
 			} else {
 				throw new IllegalArgumentException("Value of type Double is expected");
 			}
 		} else if (String.class.equals(iClass)) {
 			value = String.valueOf(value);
 		} else if (type instanceof EEnum) {
 			if (value instanceof String) {
 				EEnumLiteral literal = ((EEnum) type).getEEnumLiteralByLiteral((String) value);
 				if (literal == null) {
 					throw new IllegalArgumentException("Unknown literal: " + value);
 				} else {
 					value = literal.getInstance();
 				}
 			} else {
 				throw new IllegalArgumentException("Value of type String is expected");
 			}
 		} else {
 			throw new IllegalArgumentException("Unsupported type");
 		}
 		return value;
 	}
 }
