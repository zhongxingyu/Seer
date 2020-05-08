 package swp_compiler_ss13.fuc.backend;
 
 import swp_compiler_ss13.common.backend.BackendException;
 import swp_compiler_ss13.common.backend.Quadruple;
 
 import swp_compiler_ss13.common.types.*;
 import swp_compiler_ss13.common.types.primitive.*;
 import swp_compiler_ss13.common.types.derived.*;
 
 import java.io.PrintWriter;
 import java.util.*;
 import java.io.UnsupportedEncodingException;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import static swp_compiler_ss13.common.types.Type.*;
 import static swp_compiler_ss13.common.types.Type.Kind.BOOLEAN;
 
 /**
  * This class allows for the generation of an LLVM IR module.
  * Each methode that begins with "add" generates LLVM IR code
  * and writes it to the <code>PrintWriter</code> <code>out</code>.
  *
  */
 public class Function
 {
 	/**
 	 * This maps a variable's name (not its
 	 * identifier, which is %{name}) to the
 	 * number of times it has been used in
 	 * the module already.
 	 * This is necessary because LLVM's IR
 	 * allows only for static single assignment
 	 * (SSA), wich means we have to store the
 	 * variable's actual value as the destination
 	 * of a static pointer and generate a new
 	 * 'use' variable (%{name}.{use_number})
 	 * for every time the variable is read
 	 * from or written to and route the pointer
 	 * access through that use variable.
 	 *
 	 */
 	private Map<String,Integer> variableUseCount;
 
 	private StringBuffer code;
 
 	public String getCode() {
 		return this.code.toString() + "}\n";
 	}
 
 	private Map<String,Map.Entry<String,List<String>>> references;
 
 	private Map<String,DerivedType> derivedTypes;
 
 	private Type.Kind returnType;
 
 	private String name;
 
 	public String getName() {
 		return name;
 	}
 
 	public Type.Kind getReturnType() {
 		return returnType;
 	}
 
 	private List<Map.Entry<String,Type.Kind>> arguments;
 
 	public List<Map.Entry<String,Type.Kind>> getArguments() {
 		return arguments;
 	}
 
 	private Module parent;
 
 	public Module getParent() {
 		return parent;
 	}
 
 	/**
 	 * Creates a new <code>Module</code> instance.
 	 *
 	 * to write the LLVM IR
 	 */
 	public Function(Module parent,
 	                String name,
 	                Type.Kind returnType,
 	                List<Map.Entry<String,Type.Kind>> arguments)
 	{
 		variableUseCount = new HashMap<String,Integer>();
 		references = new HashMap<String,Map.Entry<String,List<String>>>();
 		derivedTypes = new HashMap<String,DerivedType>();
 		code = new StringBuffer();
 
 		this.returnType = returnType;
 		this.arguments = arguments;
 		this.name = name;
 		this.parent = parent;
 
 		/* Add fake temporary variable */
 		variableUseCount.put(".tmp", 0);
 
 		code.append("define ");
 		code.append(Module.getIRType(returnType));
 		code.append(" @");
 		code.append(name);
 		code.append("(");
 
 		for(Map.Entry<String,Type.Kind> argument: arguments) {
 			code.append(Module.getIRType(argument.getValue()));
 			code.append(",");
 		}
 
 		if(arguments.size() > 0) {
 			code.deleteCharAt(code.length() - 1);
 		}
 
 		code.append(") {\n");
 	}
 
 	/**
 	 * Append the LLVM IR prefixed with two spaces
 	 * to the code <code>StringBuffer</code>
 	 *
 	 * @param code a <code>String</code> value
 	 */
 	private void gen(String code)
 	{
 		this.code.append("  ");
 		this.code.append(code);
 		this.code.append("\n");
 	}
 
 	/**
 	 * Gets a unique use identifier for
 	 * a variable; no two calls will return
 	 * the same use identifier.
 	 *
 	 * @param variable the variable's name
 	 * @return a free use identifier for the variable
 	 * @exception BackendException if an error occurs
 	 */
 	private String getUseIdentifierForVariable(String variable) throws BackendException {
 		int ssa_suffix = 0;
 		try {
 			ssa_suffix = variableUseCount.get(variable);
 		} catch (NullPointerException e) {
 			throw new BackendException("Use of undeclared variable");
 		}
 		variableUseCount.put(variable, ssa_suffix + 1);
 		return "%" + variable + "." + String.valueOf(ssa_suffix);
 	}
 
 	/**
 	 * Sets a variable (which must be of the string primitive type)
 	 * to contain a pointer to a string literal.
 	 *
 	 * @param variable the variable's name
 	 * @param literalID the string literal's id
 	 * @return the used "use identifier" for variable
 	 * @exception BackendException if an error occurs
 	 */
 	private String addLoadStringLiteral(String variable, int literalID) throws BackendException {
 		String variableIdentifier = "%" + variable;
 		String variableUseIdentifier = getUseIdentifierForVariable(variable);
 		String literalIdentifier = Module.getStringLiteralIdentifier(literalID);
 		String literalType = parent.getStringLiteralType(literalID);
 
 		gen(variableUseIdentifier + " = getelementptr " + literalType + "* " + literalIdentifier + ", i64 0, i64 0");
 		gen("store i8* " + variableUseIdentifier + ", i8** " + variableIdentifier);
 		return variableUseIdentifier;
 	}
 
 	/**
 	 * Adds a new variable and return its identifier.
 	 *
 	 * @param type the new variable's type
 	 * @param variable the new variable's name
 	 * @return the variable's identifier
 	 */
 	private String addNewVariable(Kind type, String variable)
 	{
 		String irType = Module.getIRType(type);
 		String variableIdentifier = "%" + variable;
 		variableUseCount.put(variable, 0);
 
 		gen(variableIdentifier + " = alloca " + irType);
 		return variableIdentifier;
 	}
 
 	/**
 	 *  Conditionally loads a constant or an identifier. If given a constant, just the leading # character
 	 *  is removed. If given a variable (anything without leading # character),
 	 *  it is being loaded and the temporary register name is returned.
 	 *
 	 *	@param constantOrIdentifier Name of a constant or identifier.
 	 *  @returns name of usable primitive
 	 */
 	private String cdl(String constantOrIdentifier, Kind type) throws BackendException {
 		if(constantOrIdentifier.charAt(0) == '#')
 			/* constant */
 			if(type==Kind.STRING)
 			{
 				int literalID = parent.addStringLiteral(constantOrIdentifier.substring(1));
 				String literalIdentifier = Module.getStringLiteralIdentifier(literalID);
 				String literalType = parent.getStringLiteralType(literalID);
 				String variableUseIdentifier = getUseIdentifierForVariable(".tmp");
 				gen(variableUseIdentifier + " = getelementptr " + literalType + "* " + literalIdentifier + ", i64 0, i64 0");
 
 				return variableUseIdentifier;
 			}
 			else
 				return constantOrIdentifier.substring(1);
 		else {
 			/* identifier */
 			String id = getUseIdentifierForVariable(constantOrIdentifier);
 			gen(id + " = load " + Module.getIRType(type) + "* %"+constantOrIdentifier);
 			return id;
 		}
 	}
 
 	/**
 	 * Adds a new variable and optionally sets its
 	 * initial value if given.
 	 *
 	 * @param type the new variable's type
 	 * @param variable the new variable's name
 	 * @param initializer the new variable's initial value
 	 * @exception BackendException if an error occurs
 	 */
 	public void addPrimitiveDeclare(Kind type, String variable, String initializer) throws BackendException {
 		addNewVariable(type, variable);
 
 		/* Set default initializer */
 		if(initializer.equals(Quadruple.EmptyArgument)) {
 			switch(type) {
 				case LONG:
 					initializer = "#0";
 					break;
 				case DOUBLE:
 					initializer = "#0.0";
 					break;
 				case BOOLEAN:
 					initializer = "#0";
 					break;
 				case STRING:
 					initializer = "#\"\"";
 					break;
 			}
 		}
 
 		addPrimitiveAssign(type, variable, initializer);
 	}
 
 	public String addDerivedDeclare(DerivedType type, String identifier) throws BackendException {
 		String irType = "";
 
 		if(type instanceof LLVMBackendArrayType) {
 			LLVMBackendArrayType arType = (LLVMBackendArrayType) type;
 			irType = Module.getIRAType(arType.getStorageType(), arType.getDimensions());
 		}
 		else if(type instanceof LLVMBackendStructType) {
 			LLVMBackendStructType stType = (LLVMBackendStructType) type;
 			irType = Module.getIRSType(stType.getMembers());
 		}
 		else {
 			throw new BackendException("Unexpected type: " + type.getClass().getName());
 		}
 
 		String variableIdentifier = "%" + identifier;
 		variableUseCount.put(identifier, 0);
 		derivedTypes.put(identifier, type);
 		gen(variableIdentifier + " = alloca " + irType);
 		return variableIdentifier;
 	}
 
 	/**
 	 * Assigns either a constant value, or the value of
 	 * one variable (<code>src</code>) to another variable
 	 * (<code>dst</code>). Only assignments for identical
 	 * types are allowed.
 	 *
 	 * @param type the type of the assignment
 	 * @param dst the destination variable's name
 	 * @param src the source constant or the source variable's name
 	 * @exception BackendException if an error occurs
 	 */
 	public void addPrimitiveAssign(Kind type, String dst, String src) throws BackendException {
 		boolean constantSrc = false;
 		if(src.charAt(0) == '#')
 		{
 			src = src.substring(1);
 			constantSrc = true;
 		}
 
 		String irType = Module.getIRType(type);
 		String dstIdentifier = "%" + dst;
 
 		if(constantSrc)
 		{
 			if(type == Kind.STRING)
 			{
 				int id = parent.addStringLiteral(src);
 				addLoadStringLiteral(dst, id);
 			}
 			else
 			{
 				gen("store " + irType + " " + src + ", " + irType + "* " + dstIdentifier);
 			}
 		}
 		else
 		{
 			String srcUseIdentifier = getUseIdentifierForVariable(src);
 			String srcIdentifier = "%" + src;
 			gen(srcUseIdentifier + " = load " + irType + "* " + srcIdentifier);
 			gen("store " + irType + " " + srcUseIdentifier + ", " + irType + "* " + dstIdentifier);
 		}
 	}
 
 	public void addReferenceDerivedGet(Type.Kind derivedType, String derived, String index, String dst) throws BackendException {
 		String src = derived;
 		List<String> indices = new LinkedList<String>();
 
 		while(references.containsKey(src)) {
 			Map.Entry<String,List<String>> reference = references.get(src);
 			indices.addAll(reference.getValue());
 			src = reference.getKey();
 		}
 
 		switch(derivedType) {
 			case ARRAY:
 				indices.add(index);
 				break;
 			case STRUCT:
 				DerivedType type = derivedTypes.get(src);
 
 				if(type instanceof LLVMBackendArrayType) {
 					type = (DerivedType) ((LLVMBackendArrayType) type).getStorageType();
 				}
 
 				if(type instanceof LLVMBackendStructType) {
 					List<Member> members = ((LLVMBackendStructType) type).getMembers();
 					for(Member m: members) {
 						if(m.getName().equals(index)) {
 							indices.add(String.valueOf(members.indexOf(m)));
 							break;
 						}
 					}
 				}
 				else {
 					throw new BackendException("Expected final type of LLVMBackendStructType, not " + type.getClass().getName());
 				}
 				break;
 		}
 
 		references.put(dst,new AbstractMap.SimpleEntry<String,List<String>>(src, indices));
 	}
 
 	public void addPrimitiveDerivedSetOrGet(Type.Kind derivedTypeKind,
 	                                        String derived,
 	                                        String index,
 	                                        Kind primitiveType,
 	                                        String primitive,
 	                                        boolean set) throws BackendException {
 		List<String> indices = new LinkedList<String>();
 
 		while(references.containsKey(derived)) {
 			Map.Entry<String,List<String>> reference = references.get(derived);
 			indices.addAll(reference.getValue());
 			derived = reference.getKey();
 		}
 
 		switch(derivedTypeKind) {
 			case ARRAY:
 				indices.add(index);
 				break;
 			case STRUCT:
 				DerivedType type = derivedTypes.get(derived);
 
 				if(type instanceof LLVMBackendArrayType) {
 					type = (DerivedType) ((LLVMBackendArrayType) type).getStorageType();
 				}
 
 				if(type instanceof LLVMBackendStructType) {
 					List<Member> members = ((LLVMBackendStructType) type).getMembers();
 					for(Member m: members) {
 						if(m.getName().equals(index)) {
							indices.add("#" + String.valueOf(members.indexOf(m)));
 							break;
 						}
 					}
 				}
 				else {
 					throw new BackendException("Unexpected final derived type: " + type.getClass().getName());
 				}
 				break;
 			default:
 				throw new BackendException("Unexpected derived type: " + derivedTypeKind.toString());
 		}
 
 		StringBuffer indexList = new StringBuffer();
 		for(String i: indices) {
 			indexList.append(", ");
 			indexList.append(Module.getIRType(Type.Kind.LONG));
 			indexList.append(" ");
 			indexList.append(cdl(i, Type.Kind.LONG));
 		}
 
 		String primitiveIRType = Module.getIRType(primitiveType);
 
 		DerivedType derivedType = derivedTypes.get(derived);
 		String derivedIRType = "";
 		if(derivedType instanceof LLVMBackendArrayType) {
 			LLVMBackendArrayType derivedArType = (LLVMBackendArrayType) derivedType;
 			derivedIRType = Module.getIRAType(derivedArType.getStorageType(), derivedArType.getDimensions());
 			// before the getelementptr instruction, insert out of bounds checks
 			// for arrays
 			for(int idx = 0;
 			    idx < derivedArType.getDimensions().size();
 				idx++)
 			{
 				// dimension is always an integer constant
 				String dim = ""+derivedArType.getDimensions().get(idx);
 				// element index could be a variable => we have to CDL it
 				String elem= cdl(indices.get(idx),Type.Kind.LONG);
 				gen("call void @aoob1(i64 " + elem + ", i64 " + dim + ")");
 /*				System.err.println(
 					"dimension: "+dim+					
 					"\t\tindex: "+elem);*/
 			}
 		}
 		else if(derivedType instanceof LLVMBackendStructType) {
 			LLVMBackendStructType derivedStType = (LLVMBackendStructType) derivedType;
 			derivedIRType = Module.getIRSType(derivedStType.getMembers());
 		}
 		else {
 			throw new BackendException("Unexpected derived type: " + derivedType.getClass().getName());
 		}
 
 		if(set) {
 			String derivedUseIdentifier1 = getUseIdentifierForVariable(derived);
 			String derivedUseIdentifier2 = getUseIdentifierForVariable(derived);
 			String derivedIdentifier = "%" + derived;
 
 			primitive = cdl(primitive, primitiveType);
 			gen(derivedUseIdentifier1 + " = getelementptr " + derivedIRType + "* " + derivedIdentifier + ", i64 0"
 				+ indexList);
 			gen("store " + primitiveIRType + " " + primitive + ", " + primitiveIRType + "* " + derivedUseIdentifier1);
 		}
 		else {
 			String primitiveUseIdentifier = getUseIdentifierForVariable(primitive);
 			String primitiveIdentifier = "%" + primitive;
 			String derivedUseIdentifier = getUseIdentifierForVariable(derived);
 			String derivedIdentifier = "%" + derived;
 
 			gen(derivedUseIdentifier + " = getelementptr " + derivedIRType + "* " + derivedIdentifier + ", i64 0"
 				+ indexList);
 			gen(primitiveUseIdentifier + " = load " + primitiveIRType + "* " + derivedUseIdentifier);
 			gen("store " + primitiveIRType + " " + primitiveUseIdentifier + ", " + primitiveIRType + "* " + primitiveIdentifier);
 		}
 	}
 
 	/**
 	 * Assigns the value of one variable (<code>src</code>)
 	 * to another variable (<code>dst</code>), where their types
 	 * must only be convertible, not identical.
 	 *
 	 * @param srcType the source variable's type
 	 * @param src the source variable's name
 	 * @param dstType the destination variable's type
 	 * @param dst the destination variable's name
 	 * @exception BackendException if an error occurs
 	 */
 	public void addPrimitiveConversion(Kind srcType,
 	                                   String src,
 	                                   Kind dstType,
 	                                   String dst) throws BackendException {
 		String srcUseIdentifier = getUseIdentifierForVariable(src);
 		String dstUseIdentifier = getUseIdentifierForVariable(dst);
 		String srcIdentifier = "%" + src;
 		String dstIdentifier = "%" + dst;
 		String srcIRType = Module.getIRType(srcType);
 		String dstIRType = Module.getIRType(dstType);
 
 		if((srcType == Kind.LONG) && (dstType == Kind.DOUBLE))
 		{
 			gen(srcUseIdentifier + " = load " + srcIRType + "* " + srcIdentifier);
 			gen(dstUseIdentifier + " = sitofp " + srcIRType + " " + srcUseIdentifier + " to " + dstIRType);
 			gen("store " + dstIRType + " " + dstUseIdentifier + ", " + dstIRType + "* " + dstIdentifier);
 		}
 		else if((srcType == Kind.DOUBLE) && (dstType == Kind.LONG))
 		{
 			gen(srcUseIdentifier + " = load " + srcIRType + "* " + srcIdentifier);
 			gen(dstUseIdentifier + " = fptosi " + srcIRType + " " + srcUseIdentifier + " to " + dstIRType + "");
 			gen("store " + dstIRType + " " + dstUseIdentifier + ", " + dstIRType + "* " + dstIdentifier);
 		}
 		else if(dstType == Kind.STRING)
 		{
 			String conversionFunction = "";
 
 			switch(srcType) {
 				case BOOLEAN:
 					conversionFunction = "btoa";
 					break;
 				case LONG:
 					conversionFunction = "ltoa";
 					break;
 				case DOUBLE:
 					conversionFunction = "dtoa";
 					break;
 			}
 
 			gen(srcUseIdentifier + " = load " + srcIRType + "* " + srcIdentifier);
 			gen(dstUseIdentifier + " = call i8* (" + srcIRType + ")* @" + conversionFunction + "(" + srcIRType + " " + srcUseIdentifier + ")");
 			gen("store " + dstIRType + " " + dstUseIdentifier + ", " + dstIRType + "* " + dstIdentifier);
 		}
 	}
 
 	/**
 	 * Adds a generic binary operation of two sources - each can
 	 * either be a constant or a variable - the result
 	 * of which will be stored in a variable (<code>dst</code>).
 	 * All types must be identical.
 	 *
 	 * @param op the binary operation to add
 	 * @param resultType  the result type of the binary operation
 	 * @param argumentType the argument type of the binary operation
 	 * @param lhs the constant or name of the variable on the left hand side
 	 * @param rhs the constant or name of the variable on the right hand side
 	 * @param dst the destination variable's name
 	 * @exception BackendException if an error occurs
 	 */
 	public void addPrimitiveBinaryInstruction(Quadruple.Operator op,
 	                                          Kind resultType,
 	                                          Kind argumentType,
 	                                          String lhs,
 	                                          String rhs,
 	                                          String dst) throws BackendException {
 		String irArgumentType = Module.getIRType(argumentType);
 		String irResultType = Module.getIRType(resultType);
 		String irInst = Module.getIRBinaryInstruction(op);
 
 		lhs = cdl(lhs, argumentType);
 		rhs = cdl(rhs, argumentType);
 
 		String dstUseIdentifier = getUseIdentifierForVariable(dst);
 		String dstIdentifier = "%" + dst;
 
 		gen(dstUseIdentifier + " = " + irInst + " " + irArgumentType + " " + lhs + ", " + rhs);
 		gen("store " + irResultType + " " + dstUseIdentifier + ", " + irResultType + "* " + dstIdentifier);
 	}
 
 	/**
 	 * Adds a call to a argument-homogenous binary functions
 	 * where each argument can either be a constant or a variable;
 	 * the result of the function will be stored in a variable (<code>dst</code>).
 	 * Argument types must both be equal, but may differ
 	 * from the result type.
 	 *
 	 * @param op the binary operation to add
 	 * @param resultType the type of the binary operation's result
 	 * @param argumentType the type of the binary operation's arguments
 	 * @param lhs the constant or name of the variable on the left hand side
 	 * @param rhs the constant or name of the variable on the right hand side
 	 * @param dst the destination variable's name
 	 * @exception BackendException if an error occurs
 	 */
 	public void addPrimitiveBinaryCall(Quadruple.Operator op,
 	                                   Kind resultType,
 	                                   Kind argumentType,
 	                                   String lhs,
 	                                   String rhs,
 	                                   String dst) throws BackendException {
 		String irArgumentType = Module.getIRType(argumentType);
 		String irResultType = Module.getIRType(resultType);
 		String irCall = Module.getIRBinaryCall(op);
 		boolean constantRHS = false;
 
 		if(rhs.charAt(0) == '#')
 		{
 			constantRHS = true;
 		}
 
 		lhs = cdl(lhs, argumentType);
 		rhs = cdl(rhs, argumentType);
 
 		if(constantRHS) {
 			if((op == Quadruple.Operator.DIV_LONG) &&
 			   (Long.valueOf(rhs).equals(Long.valueOf(0))))
 			{
 				throw new BackendException("Division by zero");
 			}
 
 			if((op == Quadruple.Operator.DIV_DOUBLE) &&
 			   ((Double.valueOf(rhs).equals(Double.valueOf(0.0))) ||
 			    (Double.valueOf(rhs).equals(Double.valueOf(-0.0)))))
 			{
 				throw new BackendException("Division by zero");
 			}
 		}
 
 		String dstUseIdentifier = getUseIdentifierForVariable(dst);
 		String dstIdentifier = "%" + dst;
 
 		gen(dstUseIdentifier + " = invoke " + irResultType + " (" + irArgumentType + ", " + irArgumentType + ")* " +
 		    "@" + irCall + "(" + irArgumentType + " " + lhs + ", " + irArgumentType + " " + rhs + ") to label " + dstUseIdentifier + ".ok unwind label %UncaughtException");
 		gen(dstUseIdentifier.substring(1, dstUseIdentifier.length()) + ".ok:");
 		gen("store " + irResultType + " " + dstUseIdentifier + ", " + irResultType + "* " + dstIdentifier);
 	}
 
 	/**
 	 * Adds a binary not operation, where the source
 	 * may be either a constant, or a variable, whereas
 	 * the result must be a variable.
 	 *
 	 * @param source the constant or name of the variable to be negated
 	 * @param destination the destination variable's name
 	 * @exception BackendException if an error occurs
 	 */
 	public void addBooleanNot(String source, String destination) throws BackendException {
 
 		String irType = Module.getIRType(Type.Kind.BOOLEAN);
 
 		source = cdl(source, Type.Kind.BOOLEAN);
 		String dstUseIdentifier = getUseIdentifierForVariable(destination);
 		String dstIdentifier = "%" + destination;
 
 		gen(dstUseIdentifier + " = " + "sub " + irType + " 1, " + source);
 		gen("store " + irType + " " + dstUseIdentifier + ", " + irType + "* " + dstIdentifier);
 	}
 
 
 	/**
 	 * Adds the basic return instruction.
 	 *
 	 * @param value the value to return (exit code)
 	 * @exception BackendException if an error occurs
 	 */
 	public void addReturn(String value) throws BackendException {
 		if(value.charAt(0) == '#')
 		{
 			gen("ret i64 " + value.substring(1));
 		}
 		else
 		{
 			String valueUseIdentifier = getUseIdentifierForVariable(value);
 			String valueIdentifier = "%" + value;
 			gen(valueUseIdentifier + " = load i64* " + valueIdentifier);
 			gen("ret i64 " + valueUseIdentifier);
 		}
 	}
 
 	/**
 	 * Adds a label which may be the target of
 	 * branching instructions.
 	 *
 	 * @param name the label's name, must be unique in the program
 	 */
 	public void addLabel(String name){
 		gen(name + ":");
 	}
 
 	/**
 	 * Adds a branching instruction, which may either
 	 * be unconditional (e.g. branch immediately) or
 	 * depend on a boolean variable 'condition' (e.g. branch to
 	 * one of two target labels depening on the boolean
 	 * variable's value at runtime).
 	 *
 	 * @param target1 target label for unconditional branch, target
 	 *                for the condition being true for conditional branch
 	 * @param target2 <code>Quadruple.EmptyArgument</code> for unconditional
 	 *                branch, target for the condition being false for
 	 *                the conditional branch
 	 * @param condition <code>Quadruple.EmptyArgument</code> for unconditional
 	 *                  branch, the condition being tested for conditional branch
 	 * @exception BackendException if an error occurs
 	 */
 	public void addBranch(String target1, String target2, String condition) throws BackendException {
 		/* conditional branch */
 		if(!target2.equals(Quadruple.EmptyArgument)) {
 			String irType = Module.getIRType(Kind.BOOLEAN);
 			String conditionUseIdentifier = getUseIdentifierForVariable(condition);
 			gen(conditionUseIdentifier + " = load " + irType + "* %" + condition);
 			gen("br " + irType + " " + conditionUseIdentifier + ", label %" + target1 + ", label %" + target2);
 		}
 		else {
 			gen("br label %" + target1);
 		}
 	}
 
 	/**
 	 * Adds a call to the printf function for
 	 * the value, which may be either a constant
 	 * or the name of a variable.
 	 *
 	 * @param value the value to be printed
 	 * @param type the type of the value (boolean, long, double, string)
 	 * @exception BackendException if an error occurs
 	 */
 	public void addPrint(String value, Kind type) throws BackendException {
 		String irType = Module.getIRType(type);
 		boolean constantSrc = false;
 
 		value = cdl(value, type);
 
 		String temporaryIdentifier = "";
 
 		if(constantSrc)
 		{
 			temporaryIdentifier = getUseIdentifierForVariable(".tmp");
 			int id = parent.addStringLiteral(value);
 			String literalIdentifier = Module.getStringLiteralIdentifier(id);
 			String literalType = parent.getStringLiteralType(id);
 
 			gen(temporaryIdentifier + " = getelementptr " + literalType + "* " + literalIdentifier + ", i64 0, i64 0");
 		}
 		else
 		{
 			temporaryIdentifier = value;
 		}
 
 		gen("call i32 (i8*, ...)* @printf(i8* " + temporaryIdentifier + ")");
 	}
 }
