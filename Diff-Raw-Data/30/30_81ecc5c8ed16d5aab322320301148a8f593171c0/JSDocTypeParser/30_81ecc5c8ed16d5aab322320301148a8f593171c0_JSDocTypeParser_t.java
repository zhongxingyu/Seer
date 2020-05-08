 /*******************************************************************************
  * Copyright (c) 2011 NumberFour AG
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     NumberFour AG - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.typeinfo;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.eclipse.dltk.javascript.typeinfo.model.JSType;
 import org.eclipse.dltk.javascript.typeinfo.model.Property;
 import org.eclipse.dltk.javascript.typeinfo.model.RecordType;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelFactory;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelLoader;
 import org.eclipse.dltk.javascript.typeinfo.model.UnionType;
 
 public class JSDocTypeParser extends JSDocTypeParserBase {
 
 	public JSType parse(String input) throws ParseException {
 		return parse(new ANTLRStringStream(input));
 	}
 
 	protected JSType parse(CharStream input) throws ParseException {
 		skipSpaces(input);
 		final List<JSType> types = new ArrayList<JSType>();
 		for (;;) {
 			final JSType type = parseType(input);
 			if (type != null) {
 				types.add(type);
 				skipSpaces(input);
 				if (input.LT(1) == '|') {
 					input.consume();
 					continue;
 				}
 			}
 			break;
 		}
 		if (types.size() == 1) {
 			return types.get(0);
 		} else if (types.size() > 1) {
 			final UnionType unionType = TypeInfoModelFactory.eINSTANCE
 					.createUnionType();
 			unionType.getTargets().addAll(types);
 			return unionType;
 		} else {
 			return null;
 		}
 	}
 
 	protected JSType parseType(CharStream input) throws ParseException {
 		if (input.LT(1) == '{') {
 			input.consume();
 			final RecordType recordType = parseRecordType(input);
 			match(input, '}');
 			return recordType;
 		} else if (input.LT(1) == '*') {
 			input.consume();
 			return TypeInfoModelFactory.eINSTANCE.createAnyType();
 		} else {
 			return parseTypeName(input);
 		}
 	}
 
 	protected JSType parseTypeName(CharStream input) throws ParseException {
 		final int start = input.index();
 		for (;;) {
 			int ch = input.LT(1);
 			if (ch == '<') {
 				final String baseType = input.substring(start,
 						input.index() - 1);
 				input.consume();
 				final List<JSType> typeParams = parseParams(input);
 				match(input, '>');
				JSType type = createGenericType(baseType, typeParams);
				return checkIfArray(input, type);
 			} else if (ch == '.' && input.LT(2) == '<') {
 				final String baseType = input.substring(start,
 						input.index() - 1);
 				input.consume();
 				input.consume();
 				final List<JSType> typeParams = parseParams(input);
 				match(input, '>');
				JSType type = createGenericType(baseType, typeParams);
				return checkIfArray(input, type);
 			} else if (ch == '[') {
 				final String baseType = input.substring(start,
 						input.index() - 1);
 				input.consume();
 				match(input, ']');
				JSType array = createArray(translate(baseType));
				return checkIfArray(input, array);
 			} else if (ch == CharStream.EOF || Character.isWhitespace(ch)
 					|| ch == '|' || ch == ',' || ch == '}' || ch == '>') {
 				return createType(translate(input.substring(start,
 						input.index() - 1)));
 			} else {
 				input.consume();
 			}
 		}
 	}
 
	/**
	 * @param input
	 * @param type
	 * @return
	 * @throws ParseException
	 */
	private JSType checkIfArray(CharStream input, JSType type)
			throws ParseException {
		int ch = input.LT(1);
		while (ch == '[') {
			input.consume();
			match(input, ']');
			type = createArray(type);
			ch = input.LT(1);
		}
		return type;
	}

 	protected JSType createType(String typeName) {
 		if (ITypeNames.UNDEFINED.equals(typeName)) {
 			return TypeInfoModelFactory.eINSTANCE.createUndefinedType();
 		} else {
 			return TypeUtil.ref(typeName);
 		}
 	}
 
 	protected JSType createArray(String typeName) {
 		return TypeUtil.arrayOf(typeName);
 	}
 
 	protected JSType createArray(JSType itemType) {
 		return TypeUtil.arrayOf(itemType);
 	}
 
 	protected JSType createGenericType(String baseType, List<JSType> typeParams) {
 		if (ITypeNames.ARRAY.equals(baseType)) {
 			if (typeParams.size() >= 1) {
 				return createArray(typeParams.get(0));
 			} else {
 				return createArray(ITypeNames.OBJECT);
 			}
 		} else if (ITypeNames.OBJECT.equals(baseType)) {
 			if (typeParams.size() == 1) {
 				return TypeUtil.mapOf(null, typeParams.get(0));
 			} else if (typeParams.size() >= 2) {
 				return TypeUtil.mapOf(typeParams.get(0), typeParams.get(1));
 			} else {
 				return TypeUtil.mapOf(null, ITypeNames.OBJECT);
 			}
 		} else {
 			return doCreateGenericType(baseType, typeParams);
 		}
 	}
 
 	protected JSType doCreateGenericType(String baseType,
 			List<JSType> typeParams) {
 		if (!typeParams.isEmpty()) {
 			StringBuilder sb = new StringBuilder();
 			sb.append(baseType);
 			sb.append('<');
 			int index = 0;
 			for (JSType typeParam : typeParams) {
 				if (++index > 1) {
 					sb.append(',');
 				}
 				sb.append(typeParam.getName());
 			}
 			sb.append('>');
 			return TypeUtil.ref(sb.toString());
 		}
 		return TypeUtil.ref(baseType);
 	}
 
 	protected List<JSType> parseParams(CharStream input) throws ParseException {
 		final List<JSType> types = new ArrayList<JSType>();
 		for (;;) {
 			final JSType type = parse(input);
 			if (type != null) {
 				types.add(type);
 				skipSpaces(input);
 				if (input.LT(1) == ',') {
 					input.consume();
 					continue;
 				}
 			}
 			break;
 		}
 		return types;
 	}
 
 	protected RecordType parseRecordType(CharStream input)
 			throws ParseException {
 		final int start = input.index();
 		final RecordType type = TypeInfoModelFactory.eINSTANCE
 				.createRecordType();
 		type.setTarget(TypeInfoModelFactory.eINSTANCE.createType());
 		skipSpaces(input);
 		for (;;) {
 			final int nameStart = input.index();
 			int ch = input.LT(1);
 			if (Character.isJavaIdentifierStart(ch)) {
 				input.consume();
 				while (Character.isJavaIdentifierStart(input.LT(1))) {
 					input.consume();
 				}
 				final Property property = TypeInfoModelFactory.eINSTANCE
 						.createProperty();
 				property.setName(input.substring(nameStart, input.index() - 1));
 				skipSpaces(input);
 				if (input.LT(1) == ':') {
 					input.consume();
 					final JSType memberType = parse(input);
 					if (memberType != null) {
 						property.setType(memberType);
 					} else {
 						property.setType(TypeInfoModelFactory.eINSTANCE
 								.createAnyType());
 					}
 				} else {
 					property.setType(TypeInfoModelFactory.eINSTANCE
 							.createAnyType());
 				}
 				type.getMembers().add(property);
 				skipSpaces(input);
 				if (input.LT(1) == ',') {
 					input.consume();
 					continue;
 				}
 			}
 			break;
 		}
 		type.getTarget().setName(
 				'{' + input.substring(start, input.index() - 1) + '}');
 		return type;
 	}
 
 	protected String translate(String typeName) {
 		return TypeInfoModelLoader.getInstance().translateTypeName(typeName);
 	}
 
 }
