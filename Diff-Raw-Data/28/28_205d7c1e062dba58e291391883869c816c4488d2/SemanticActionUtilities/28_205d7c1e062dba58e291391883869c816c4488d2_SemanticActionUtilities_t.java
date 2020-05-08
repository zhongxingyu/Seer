 package de.uni_koblenz.jgralab.java_extractor.utilities;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PushbackReader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.uni_koblenz.edl.parser.Position;
 import de.uni_koblenz.edl.parser.symboltable.SymbolTableStack;
 import de.uni_koblenz.jgralab.Edge;
 import de.uni_koblenz.jgralab.EdgeDirection;
 import de.uni_koblenz.jgralab.Vertex;
 import de.uni_koblenz.jgralab.java_extractor.builder.Java5Builder;
 import de.uni_koblenz.jgralab.java_extractor.schema.annotation.Annotation;
 import de.uni_koblenz.jgralab.java_extractor.schema.common.AttributedEdge;
 import de.uni_koblenz.jgralab.java_extractor.schema.common.Identifier;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.BooleanConstant;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.CharConstant;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.DoubleConstant;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.Expression;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.FloatConstant;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.IntegerConstant;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.LongConstant;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.Null;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.StringConstant;
 import de.uni_koblenz.jgralab.java_extractor.schema.member.HasVariableAnnotation;
 import de.uni_koblenz.jgralab.java_extractor.schema.member.HasVariableModifier;
 import de.uni_koblenz.jgralab.java_extractor.schema.member.Member;
 import de.uni_koblenz.jgralab.java_extractor.schema.member.Modifier;
 import de.uni_koblenz.jgralab.java_extractor.schema.member.VariableDeclaration;
 import de.uni_koblenz.jgralab.java_extractor.schema.program.Program;
 import de.uni_koblenz.jgralab.java_extractor.schema.statement.EmptyStatement;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.definition.HasTypeParameterUpperBound;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.definition.Type;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.definition.TypeParameterDeclaration;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.ArrayType;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.BuiltInType;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.BuiltInTypes;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.EnclosedType;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.HasEnclosedType;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.HasEnclosingType;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.HasLowerBound;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.HasSimpleArgumentType;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.HasSimpleName;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.HasUpperBound;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.IsDefinedByType;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.QualifiedName;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.QualifiedType;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.SimpleArgument;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.TypeArgument;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.TypeParameterUsage;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.TypeSpecification;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.WildcardArgument;
 
 public class SemanticActionUtilities {
 
 	public static String DEFAULT_PROGRAM_NAME = "program";
 
 	private final Java5Builder graphBuilder;
 
 	public SemanticActionUtilities(Java5Builder java5Builder) {
 		graphBuilder = java5Builder;
 	}
 
 	public String readInput(String inputFile, String encoding)
 			throws IOException {
 		StringBuilder sb = new StringBuilder();
 		PushbackReader pbr = null;
 		try {
 			pbr = new PushbackReader(new BufferedReader(new InputStreamReader(
 					new FileInputStream(inputFile), encoding)), 5);
 			boolean evenNumberOfBackslashes = true;
 			for (int current = pbr.read(); current != -1; current = pbr.read()) {
 				if (current == '\\') {
 					evenNumberOfBackslashes = !evenNumberOfBackslashes;
 				} else {
 					evenNumberOfBackslashes = true;
 				}
 				if (!evenNumberOfBackslashes) {
 					try {
 						current = readUnicodeEscape(pbr);
 						// if a correct unicode escape was read, the counter for
 						// backslaches can be reset
 						evenNumberOfBackslashes = true;
 					} catch (NumberFormatException e) {
 						// this was no unicode escape. Corresponding chars ar
 						// already back in the pushback reader
 					}
 				}
 				sb.append((char) current);
 			}
 		} finally {
 			if (pbr != null) {
 				pbr.close();
 			}
 		}
 		return sb.toString();
 	}
 
 	private int readUnicodeEscape(PushbackReader reader) throws IOException {
 		int firstHex, secondHex, thirdHex, fourthHex;
 		int numberOfReadChars = 0;
 		do {
 			firstHex = reader.read();
 			numberOfReadChars++;
 		} while (firstHex == 'u');
 		if (numberOfReadChars == 1) {
 			// only one char was read which was no 'u'
 			reader.unread(firstHex);
 			throw new NumberFormatException();
 		}
 		if (isNotHexDigit(firstHex)) {
 			if (firstHex == -1) {
 				reader.unread(new char[] { 'u' });
 			} else {
 				reader.unread(new char[] { 'u', (char) firstHex });
 			}
 			throw new NumberFormatException();
 		}
 		secondHex = reader.read();
 		if (isNotHexDigit(secondHex)) {
 			if (secondHex == -1) {
 				reader.unread(new char[] { 'u', (char) firstHex });
 			} else {
 				reader.unread(new char[] { 'u', (char) firstHex,
 						(char) secondHex });
 			}
 			throw new NumberFormatException();
 		}
 		thirdHex = reader.read();
 		if (isNotHexDigit(thirdHex)) {
 			if (thirdHex == -1) {
 				reader.unread(new char[] { 'u', (char) firstHex,
 						(char) secondHex });
 			} else {
 				reader.unread(new char[] { 'u', (char) firstHex,
 						(char) secondHex, (char) thirdHex });
 			}
 			throw new NumberFormatException();
 		}
 		fourthHex = reader.read();
 		if (isNotHexDigit(fourthHex)) {
 			if (fourthHex == -1) {
 				reader.unread(new char[] { 'u', (char) firstHex,
 						(char) secondHex, (char) thirdHex });
 			} else {
 				reader.unread(new char[] { 'u', (char) firstHex,
 						(char) secondHex, (char) thirdHex, (char) fourthHex });
 			}
 			throw new NumberFormatException();
 		}
 		return Integer.parseInt("" + ((char) firstHex) + ((char) secondHex)
 				+ ((char) thirdHex) + ((char) fourthHex), 16);
 	}
 
 	private boolean isNotHexDigit(int firstHex) {
 		return firstHex == -1
 				|| !((firstHex >= 'a' && firstHex <= 'f')
 						|| (firstHex >= 'A' && firstHex <= 'F') || (firstHex >= '0' && firstHex <= '9'));
 	}
 
 	/*
 	 * Programm
 	 */
 
 	private Program program;
 
 	public void setProgramName(String name) {
 		assert program != null;
 		program.set_name(name);
 	}
 
 	public Program getProgram(Position position) {
 		if (program == null) {
 			program = (Program) graphBuilder.createVertex(Program.VC, position);
 		}
 		return program;
 	}
 
 	/*
 	 * Programm
 	 */
 
 	private EmptyStatement emptyStatement;
 
 	public EmptyStatement getEmptyStatement(Position position) {
 		if (emptyStatement == null) {
 			emptyStatement = (EmptyStatement) graphBuilder.createVertex(
 					EmptyStatement.VC, position);
 		} else {
 			graphBuilder.getPositionsMap().put(emptyStatement, position);
 		}
 		return emptyStatement;
 	}
 
 	/*
 	 * Constants
 	 */
 
 	private final Map<String, Expression> name2Literal = new HashMap<String, Expression>();
 
 	public Expression getLiteral(String lexem, Position position) {
 		Expression result = name2Literal.get(lexem);
 		if (result == null) {
 			if (lexem.equals("null")) {
 				result = (Expression) graphBuilder.createVertex(Null.VC,
 						position);
 			} else if (lexem.equals("true") || lexem.equals("false")) {
 				result = (Expression) graphBuilder.createVertex(
 						BooleanConstant.VC, position);
 				((BooleanConstant) result).set_value(Boolean
 						.parseBoolean(lexem));
 			} else if (lexem.startsWith("\'") && lexem.endsWith("\'")) {
 				result = (Expression) graphBuilder.createVertex(
 						CharConstant.VC, position);
 				((CharConstant) result).set_literal(lexem);
 			} else if (lexem.startsWith("\"") && lexem.endsWith("\"")) {
 				result = (Expression) graphBuilder.createVertex(
 						StringConstant.VC, position);
				((StringConstant) result).set_literal(lexem);
 			} else if (lexem.matches("^(\\d+|(0[xX](\\d|[a-fA-F])+))$")) {
 				result = (Expression) graphBuilder.createVertex(
 						IntegerConstant.VC, position);
 				int value = 0;
 				if (lexem.toLowerCase().startsWith("0x")) {
 					String hexString = lexem.substring(2);
 					value = (int) Long.parseLong(hexString, 16);
 				} else if (lexem.startsWith("0") && lexem.length() > 1) {
 					value = (int) Long.parseLong(lexem.substring(1), 8);
 				} else {
 					value = (int) Long.parseLong(lexem);
 				}
 				((IntegerConstant) result).set_value(value);
 				((IntegerConstant) result).set_literal(lexem);
 			} else if (lexem.matches("^((\\d+|(0[xX](\\d|[a-fA-F])+))[lL])$")) {
 				result = (Expression) graphBuilder.createVertex(
 						LongConstant.VC, position);
 				String shortenedLexem = lexem.substring(0, lexem.length() - 1);
 				long value = 0;
 				if (shortenedLexem.equals(Long.toString(Long.MIN_VALUE)
 						.substring(1))) {
 					value = Long.MIN_VALUE;
 				} else if (shortenedLexem.toLowerCase().startsWith("0x")) {
 					shortenedLexem = shortenedLexem.substring(2);
 					value = convertToLong(shortenedLexem, 16);
 				} else if (shortenedLexem.startsWith("0")
 						&& shortenedLexem.length() > 1) {
 					value = Long.parseLong(shortenedLexem.substring(1), 8);
 				} else {
 					value = Long.parseLong(shortenedLexem);
 				}
 				((LongConstant) result).set_value(value);
 				((LongConstant) result).set_literal(lexem);
 			} else if (lexem
 					.matches("^(((\\d+\\.\\d*([eE][+-]?\\d+)?)|(\\.\\d+([eE][+-]?\\d+)?)|(\\d+([eE][+-]?\\d+)?))"
 							+ "|(0[xX](([0-9a-fA-F]+\\.?)|([0-9a-fA-F]*\\.[0-9a-fA-F]+))[pP][+-]?\\d+))"
 							+ "[fF]$")) {
 				result = (Expression) graphBuilder.createVertex(
 						FloatConstant.VC, position);
 				((FloatConstant) result).set_value(Double.valueOf(lexem));
 				((FloatConstant) result).set_literal(lexem);
 			} else if (lexem
 					.matches("^(((\\d+\\.\\d*([eE][+-]?\\d+)?)|(\\.\\d+([eE][+-]?\\d+)?)|(\\d+([eE][+-]?\\d+)?))"
 							+ "|(0[xX](([0-9a-fA-F]+\\.?)|([0-9a-fA-F]*\\.[0-9a-fA-F]+))[pP][+-]?\\d+))"
 							+ "[dD]?$")) {
 				result = (Expression) graphBuilder.createVertex(
 						DoubleConstant.VC, position);
 				((DoubleConstant) result).set_value(Double.valueOf(lexem));
 				((DoubleConstant) result).set_literal(lexem);
 			}
 			if (result != null) {
 				name2Literal.put(lexem, result);
 			}
 		} else {
 			graphBuilder.getPositionsMap().put(result, position);
 		}
 		return result;
 	}
 
 	public Vertex applyMinus(Vertex numberLiteral) {
 		if (numberLiteral.isInstanceOf(LongConstant.VC)
 				&& numberLiteral.getAttribute("value").equals(Long.MIN_VALUE)) {
 			return numberLiteral;
 		}
 		Object oldValue = numberLiteral.getAttribute("value");
 		if (oldValue instanceof Long) {
 			numberLiteral.setAttribute("value", -1 * ((Long) oldValue));
 		} else {
 			numberLiteral.setAttribute("value", -1 * ((Double) oldValue));
 		}
 		numberLiteral.setAttribute("literal",
 				"-" + numberLiteral.getAttribute("literal"));
 		return numberLiteral;
 	}
 
 	private long convertToLong(String shortenedLexem, int radix) {
 		shortenedLexem = shortenedLexem.toLowerCase();
 		long result = 0;
 		for (char currentChar : shortenedLexem.toCharArray()) {
 			result <<= 4;
 			byte currentByte = Byte.parseByte("" + currentChar, radix);
 			result |= currentByte;
 		}
 		return result;
 	}
 
	public String getEscapedString(String escapedString) {
		escapedString = escapedString.substring(1);
		if (escapedString.equals("b")) {
			return "\b";
		} else if (escapedString.equals("t")) {
			return "\t";
		} else if (escapedString.equals("n")) {
			return "\n";
		} else if (escapedString.equals("f")) {
			return "\f";
		} else if (escapedString.equals("r")) {
			return "\r";
		} else if (escapedString.matches("^\\d\\d?\\d?$")) {
			return "" + (char) Integer.parseInt(escapedString, 8);
		} else {
			return escapedString;
		}
	}

 	/*
 	 * BuiltInTypes
 	 */
 
 	private final Map<BuiltInTypes, BuiltInType> builtInTypeMap = new HashMap<BuiltInTypes, BuiltInType>();
 
 	public BuiltInType getBuiltInType(Position currentPosition,
 			Object builtInTypes) {
 		BuiltInTypes builtInTypes_ = (BuiltInTypes) builtInTypes;
 		BuiltInType result = builtInTypeMap.get(builtInTypes_);
 		if (result == null) {
 			result = (BuiltInType) graphBuilder.createVertex(BuiltInType.VC,
 					currentPosition);
 			result.set_type(builtInTypes_);
 			builtInTypeMap.put(builtInTypes_, result);
 		} else {
 			// update position to the current one because the AttributedEdges
 			// should note the correct positions
 			graphBuilder.getPositionsMap().put(result, currentPosition);
 		}
 		return result;
 	}
 
 	/*
 	 * further helping methods
 	 */
 
 	public String extractSimpleName(String qualifiedName) {
 		return qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1);
 	}
 
 	/**
 	 * If a simple name is used in a Java file, then this method resolves the
 	 * corresponding qualified name.
 	 * 
 	 * @param simpleName
 	 * @return
 	 */
 	public String resolveQualifiedName(Object simpleName) {
 		// TODO extend complete name resolution
 		return (String) simpleName;
 	}
 
 	/**
 	 * If a type parameter is used in the type parameter declaration before its
 	 * own declaration, then the type parameter is represented as
 	 * {@link QualifiedType} instead of {@link TypeParameterUsage}.
 	 * 
 	 * @param name2TypeParameter
 	 * @param typeDefinitionOrMember
 	 */
 	public void correctTypeParameterUsage(SymbolTableStack name2TypeParameter,
 			Vertex typeDefinitionOrMember) {
 		assert typeDefinitionOrMember.isInstanceOf(Type.VC)
 				|| typeDefinitionOrMember.isInstanceOf(Member.VC);
 		for (TypeParameterDeclaration typeParamDecl : typeDefinitionOrMember
 				.isInstanceOf(Type.VC) ? ((Type) typeDefinitionOrMember)
 				.get_typeParameters() : ((Member) typeDefinitionOrMember)
 				.get_typeParameters()) {
 			HasTypeParameterUpperBound htpub = typeParamDecl
 					.getFirstHasTypeParameterUpperBoundIncidence(EdgeDirection.OUT);
 			while (htpub != null) {
 				HasTypeParameterUpperBound next = htpub
 						.getNextHasTypeParameterUpperBoundIncidence(EdgeDirection.OUT);
 				TypeSpecification typeSpecification = (TypeSpecification) htpub
 						.getThat();
 				checkAndCorrectQualifiedTyps(name2TypeParameter, htpub,
 						typeSpecification);
 				htpub = next;
 			}
 		}
 	}
 
 	/**
 	 * @param name2TypeParameter
 	 * @param edge2WrongQualifiedType
 	 * @param possiblyWrongQualifiedType
 	 */
 	private void checkAndCorrectQualifiedTyps(
 			SymbolTableStack name2TypeParameter,
 			AttributedEdge edge2WrongQualifiedType,
 			TypeSpecification possiblyWrongQualifiedType) {
 		// correct type arguments
 		for (TypeArgument typeArgument : possiblyWrongQualifiedType
 				.get_typeArguments()) {
 			for (Vertex argument : typeArgument.get_arguments()) {
 				if (argument.isInstanceOf(SimpleArgument.VC)) {
 					HasSimpleArgumentType hsat = (HasSimpleArgumentType) argument
 							.getFirstIncidence(HasSimpleArgumentType.EC);
 					checkAndCorrectQualifiedTyps(name2TypeParameter, hsat,
 							hsat.getOmega());
 				} else if (argument.isInstanceOf(WildcardArgument.VC)) {
 					HasUpperBound hub = (HasUpperBound) argument
 							.getFirstIncidence(HasUpperBound.EC);
 					if (hub != null) {
 						checkAndCorrectQualifiedTyps(name2TypeParameter, hub,
 								hub.getOmega());
 					}
 					HasLowerBound hlb = (HasLowerBound) argument
 							.getFirstIncidence(HasLowerBound.EC);
 					if (hlb != null) {
 						checkAndCorrectQualifiedTyps(name2TypeParameter, hlb,
 								hlb.getOmega());
 					}
 				}
 			}
 		}
 		if (possiblyWrongQualifiedType.isInstanceOf(QualifiedType.VC)) {
 			TypeParameterDeclaration declarationOfUsedTypeParameter = (TypeParameterDeclaration) name2TypeParameter
 					.use(((QualifiedType) possiblyWrongQualifiedType)
 							.get_fullyQualifiedName());
 			if (declarationOfUsedTypeParameter != null) {
 				correctQualifiedType(edge2WrongQualifiedType,
 						(QualifiedType) possiblyWrongQualifiedType,
 						declarationOfUsedTypeParameter);
 			}
 		} else if (possiblyWrongQualifiedType.isInstanceOf(EnclosedType.VC)) {
 			HasEnclosedType hasEnclosedType = (HasEnclosedType) possiblyWrongQualifiedType
 					.getFirstIncidence(HasEnclosedType.EC);
 			checkAndCorrectQualifiedTyps(name2TypeParameter, hasEnclosedType,
 					hasEnclosedType.getOmega());
 			HasEnclosingType hasEnclosingType = (HasEnclosingType) possiblyWrongQualifiedType
 					.getFirstIncidence(HasEnclosingType.EC);
 			checkAndCorrectQualifiedTyps(name2TypeParameter, hasEnclosingType,
 					hasEnclosingType.getOmega());
 		} else if (possiblyWrongQualifiedType
 				.isInstanceOf(TypeParameterUsage.VC)) {
 			// public class TestClass<V> {
 			// public interface TestClass<W extends V, V> {
 			// }
 			// }
 			// In this case the "W extends V" uses the wrong V
 			IsDefinedByType idbt = (IsDefinedByType) possiblyWrongQualifiedType
 					.getFirstIncidence(IsDefinedByType.EC);
 			TypeParameterDeclaration usedTypeParamDecl = (TypeParameterDeclaration) idbt
 					.getOmega();
 			Vertex correctTypeParamDecl = name2TypeParameter
 					.use(usedTypeParamDecl.get_simpleName().get_name());
 			if (correctTypeParamDecl != usedTypeParamDecl) {
 				idbt.setOmega(correctTypeParamDecl);
 			}
 		}
 	}
 
 	/**
 	 * @param edge2WrongQualifiedType
 	 * @param wrongQualifiedType
 	 * @param declarationOfUsedTypeParameter
 	 */
 	private void correctQualifiedType(AttributedEdge edge2WrongQualifiedType,
 			QualifiedType wrongQualifiedType,
 			TypeParameterDeclaration declarationOfUsedTypeParameter) {
 		Position pos = new Position(edge2WrongQualifiedType.get_offset(),
 				edge2WrongQualifiedType.get_length(),
 				edge2WrongQualifiedType.get_line(),
 				edge2WrongQualifiedType.get_column());
 		TypeParameterUsage newUsage = (TypeParameterUsage) graphBuilder
 				.createVertex(TypeParameterUsage.VC, pos);
 		Edge edge = wrongQualifiedType.getFirstIncidence();
 		while (edge != null) {
 			Edge next = edge.getNextIncidence();
 			if (edge.isInstanceOf(HasSimpleName.EC)) {
 				edge.delete();
 				graphBuilder.createEdge(IsDefinedByType.EC, newUsage,
 						declarationOfUsedTypeParameter);
 			} else if (edge.getAlpha() == wrongQualifiedType) {
 				edge.setAlpha(newUsage);
 			} else {
 				assert edge.getOmega() == wrongQualifiedType;
 				edge.setOmega(newUsage);
 			}
 			edge = next;
 		}
 		assert wrongQualifiedType.getDegree() == 0;
 		wrongQualifiedType.delete();
 	}
 
 	public int calculateDimensions(Vertex typeSpecification) {
 		int dimension = 0;
 		for (Vertex current = typeSpecification; current
 				.isInstanceOf(ArrayType.VC); current = ((ArrayType) current)
 				.get_elementType()) {
 			dimension++;
 		}
 		return dimension;
 	}
 
 	public void createModifiersForVariableDeclaration(Object field,
 			Object modAndAnno) {
 		VariableDeclaration field_ = (VariableDeclaration) field;
 		@SuppressWarnings("unchecked")
 		List<Vertex> modAndAnno_ = (List<Vertex>) modAndAnno;
 		for (Vertex vertex : modAndAnno_) {
 			if (vertex.isInstanceOf(Modifier.VC)) {
 				graphBuilder.createEdge(HasVariableModifier.EC, field_, vertex);
 			} else {
 				assert vertex.isInstanceOf(Annotation.VC);
 				graphBuilder.createEdge(HasVariableAnnotation.EC, field_,
 						vertex);
 			}
 		}
 	}
 
 	public Identifier getIdentifierOfConstructorType(Vertex typeSpecification) {
 		Identifier id = null;
 		if (typeSpecification.isInstanceOf(QualifiedName.VC)) {
 			id = ((QualifiedName) typeSpecification).get_simpleName();
 		} else if (typeSpecification.isInstanceOf(EnclosedType.VC)) {
 			id = getIdentifierOfConstructorType(((EnclosedType) typeSpecification)
 					.get_enclosedType());
 		} else if (typeSpecification.isInstanceOf(TypeParameterUsage.VC)) {
 			id = ((TypeParameterUsage) typeSpecification)
 					.getFirstIsDefinedByTypeIncidence(EdgeDirection.OUT)
 					.getOmega().get_simpleName();
 		}
 		return id;
 	}
 
 	// TODO check octal-excape of strings (if numbers follows after, e.g., \007)
 
 	// TODO correct field access
 
 	// TODO check if the anonymous class extends a class or implements an
 	// interface
 
 	// TODO create simple name for anonymous class
 
 	// TODO a.b.c.run() if a or b or c is a field
 
 	// TODO link qualified names to types, packages, members (especially
 	// imports)
 
 	// TODO add comments
 
 }
