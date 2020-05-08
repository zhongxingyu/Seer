 /* 
  * Copyright (c) Ericsson AB, 2013
  * All rights reserved.
  *
  * License terms:
  *
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  *     * Redistributions of source code must retain the above 
  *       copyright notice, this list of conditions and the 
  *       following disclaimer.
  *     * Redistributions in binary form must reproduce the 
  *       above copyright notice, this list of conditions and 
  *       the following disclaimer in the documentation and/or 
  *       other materials provided with the distribution.
  *     * Neither the name of the copyright holder nor the names 
  *       of its contributors may be used to endorse or promote 
  *       products derived from this software without specific 
  *       prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.caltoopia.codegen;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import org.caltoopia.ast2ir.Util;
 import org.caltoopia.cli.ActorDirectory;
 import org.caltoopia.cli.DirectoryException;
 import org.caltoopia.ir.Action;
 import org.caltoopia.ir.Annotation;
 import org.caltoopia.ir.AnnotationArgument;
 import org.caltoopia.ir.Assign;
 import org.caltoopia.ir.BinaryExpression;
 import org.caltoopia.ir.Block;
 import org.caltoopia.ir.BooleanLiteral;
 import org.caltoopia.ir.Declaration;
 import org.caltoopia.ir.Expression;
 import org.caltoopia.ir.FloatLiteral;
 import org.caltoopia.ir.ForEach;
 import org.caltoopia.ir.ForwardDeclaration;
 import org.caltoopia.ir.FunctionCall;
 import org.caltoopia.ir.Generator;
 import org.caltoopia.ir.IfStatement;
 import org.caltoopia.ir.IntegerLiteral;
 import org.caltoopia.ir.IrFactory;
 import org.caltoopia.ir.LambdaExpression;
 import org.caltoopia.ir.ListExpression;
 import org.caltoopia.ir.LiteralExpression;
 import org.caltoopia.ir.Member;
 import org.caltoopia.ir.Node;
 import org.caltoopia.ir.Port;
 import org.caltoopia.ir.ProcCall;
 import org.caltoopia.ir.ProcExpression;
 import org.caltoopia.ir.ReturnValue;
 import org.caltoopia.ir.Scope;
 import org.caltoopia.ir.Statement;
 import org.caltoopia.ir.StringLiteral;
 import org.caltoopia.ir.TaggedExpression;
 import org.caltoopia.ir.Type;
 import org.caltoopia.ir.TypeBool;
 import org.caltoopia.ir.TypeConstructorCall;
 import org.caltoopia.ir.TypeDeclaration;
 import org.caltoopia.ir.TypeDeclarationImport;
 import org.caltoopia.ir.TypeFloat;
 import org.caltoopia.ir.TypeInt;
 import org.caltoopia.ir.TypeLambda;
 import org.caltoopia.ir.TypeList;
 import org.caltoopia.ir.TypeProc;
 import org.caltoopia.ir.TypeRecord;
 import org.caltoopia.ir.TypeString;
 import org.caltoopia.ir.TypeUint;
 import org.caltoopia.ir.TypeUndef;
 import org.caltoopia.ir.TypeUser;
 import org.caltoopia.ir.UnaryExpression;
 import org.caltoopia.ir.Variable;
 import org.caltoopia.ir.VariableExpression;
 import org.caltoopia.ir.VariableExternal;
 import org.caltoopia.ir.VariableImport;
 import org.caltoopia.ir.VariableReference;
 import org.caltoopia.ir.WhileLoop;
 import org.caltoopia.types.TypeSystem;
 import org.eclipse.emf.common.util.EList;
 
 public class UtilIR {
 	
 	static public List<String> getAnnotatedNamespace(Node node) {
 		List<String> ns = new ArrayList<String>();
 		for(Annotation a : node.getAnnotations()) {
 			if(a.getName().equals("NAMESPACE")) {
 				for(AnnotationArgument aa: a.getArguments()) {
 					ns.add(aa.getValue());
 				}
 				break;
 			}
 		}
 		return ns;
 	}
 
 	static public boolean fromNamespace(Node node) {
 		List<String> ns = new ArrayList<String>();
 		for(Annotation a : node.getAnnotations()) {
 			if(a.getName().equals("NAMESPACE")) {
 				if(!a.getArguments().isEmpty()) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	static public Expression containsTag(List<TaggedExpression> list, String tag) {
 		if(list==null)
 			return null;
 		if(list.isEmpty())
 			return null;
 
 		for(TaggedExpression t : list) {
 			if(t.getTag().equals(tag))
 				return t.getExpression();
 		}
 
 		return null;
 	}
 	
 	//Returns true if tag exist and is true otherwise false
 	static public boolean containsTagBool(List<TaggedExpression> list, String tag) {
 		Expression e = containsTag(list,tag);
 		if(e instanceof BooleanLiteral) 
 			return ((BooleanLiteral) e).isValue();
 		else
 			return false;
 	}
 	
 	static public void tag(Declaration decl, String tagName, boolean val) {
 		List<TaggedExpression> list = decl.getAttributes();
 		TaggedExpression t = null;
 		if(!list.isEmpty()) {
 			for(TaggedExpression tt : list) {
 				if(tt.getTag().equals(tagName)) {
 					t=tt;
 					break;
 				}
 			}			
 		}
 		if(t==null) t = tag(tagName,val);
 		decl.getAttributes().add(t);
 	}
 	
 	static public TaggedExpression tag(String tagName, boolean val) {
 		TaggedExpression t = IrFactory.eINSTANCE.createTaggedExpression();
 		t.setTag(tagName);
 		t.setExpression(lit(val));
 		return t;
 	}
 	
 	//TODO is this needed anymore???
 /*
 	static public List<String> findTypesNamespace(Namespace ns, Type type) {
 		if(type instanceof TypeStruct) {
 			for(Import imp : ns.getImports()) {
 				if(imp.getTypedef() != null && 
 					imp.getTypedef().getName().equals(((TypeStruct) type).getName())) {
 					return imp.getTypedef().getScopeName();
 				}
 			}
 			return null;
 		} else {
 			return null;
 		}
 	}
 */
 	//Create a prototype for an external c-function like malloc
 	//TODO was createNativeFunctionDef
 	static public VariableExternal createExternalFunctionDecl(String name, Type type, List<String> params, List<Type> paramTypes) {
 		VariableExternal fun = IrFactory.eINSTANCE.createVariableExternal();
 		fun.setName(name);
 		fun.setId(Util.getDefinitionId());
 		fun.setScope(null); //FIXME Native no scope???
 		
 		TypeLambda ftype = IrFactory.eINSTANCE.createTypeLambda();
 		ftype.getInputTypes().addAll(paramTypes);
 		ftype.setOutputType(type);
 		fun.setType(ftype);
 		//TODO param names are ignored, if needed place that in TaggedExpression
 		return fun;
 	}
 
 	static public Variable createProcDefSignature(Scope outer, Scope scope, String name, 
 														String outParamName, Type type, 
 														List<String> params, List<Type> paramTypes) {
 		return createProcDefSignature(outer, scope, name, params, paramTypes, Arrays.asList(outParamName), Arrays.asList(type));
 	}
 	//Create a prototype for a procedure that will be completely defined
 	static public Variable createProcDefSignature(Scope outer, Scope scope, String name, 
 														List<String> inParams, List<Type> inParamTypes, 
 														List<String> outParams, List<Type> outParamTypes) {
 		Variable fun = IrFactory.eINSTANCE.createVariable();
 
 		fun.setName(name);
 		fun.setId(Util.getDefinitionId());
 		fun.setScope(scope);
 		fun.setConstant(true);
 		
 		ProcExpression proc = IrFactory.eINSTANCE.createProcExpression();
 		proc.setId(Util.getDefinitionId());
 		proc.setOuter(outer);
 		proc.setContext(scope);
 		TypeProc ftype = IrFactory.eINSTANCE.createTypeProc();
 		proc.setType(ftype);
 		fun.setInitValue(proc);
 		fun.setType(ftype);
 		
 		//In params
 		for(int i=0;i<inParams.size();i++) {
 			Variable decl = IrFactory.eINSTANCE.createVariable();
 			decl.setName(inParams.get(i));
 			decl.setType(inParamTypes.get(i));
 			decl.setId(Util.getDefinitionId());
 			proc.getParameters().add(decl);
 			ftype.getInputTypes().add(inParamTypes.get(i));
 		}
 		for(int i=0;i<outParams.size();i++) {
 			//output param if not void
 			if(!(outParamTypes.get(i) instanceof TypeUndef)) {
 				Variable decl = IrFactory.eINSTANCE.createVariable();
 				decl.setName(outParams.get(i));
 				decl.setType(outParamTypes.get(i));
 				decl.setId(Util.getDefinitionId());
 				proc.getOutputs().add(decl);
 				ftype.getOutputTypes().add(outParamTypes.get(i));
 			}
 		}
 		return fun;
 	}
 
 	//Create a prototype for a function that will be completely defined
 	static public Variable createFuncDefSignature(Scope outer, Scope scope, String name, Type outParamType,
 														List<String> inParams, List<Type> inParamTypes) {
 		Variable fun = IrFactory.eINSTANCE.createVariable();
 
 		fun.setName(name);
 		fun.setId(Util.getDefinitionId());
 		fun.setScope(scope);
 		fun.setConstant(true);
 		
 		LambdaExpression lambda = IrFactory.eINSTANCE.createLambdaExpression();
 		lambda.setId(Util.getDefinitionId());
 		lambda.setOuter(outer);
 		lambda.setContext(scope);
 		TypeLambda ftype = IrFactory.eINSTANCE.createTypeLambda();
 		lambda.setType(ftype);
 		fun.setInitValue(lambda);
 		fun.setType(ftype);
 		
 		//In params
 		for(int i=0;i<inParams.size();i++) {
 			Variable decl = IrFactory.eINSTANCE.createVariable();
 			decl.setName(inParams.get(i));
 			decl.setType(inParamTypes.get(i));
 			decl.setId(Util.getDefinitionId());
 			lambda.getParameters().add(decl);
 			ftype.getInputTypes().add(inParamTypes.get(i));
 		}
 		ftype.setOutputType(outParamType);
 		return fun;
 	}
 
 	static public Type createTypeUser(TypeDeclaration decl) {
 		if(decl.getType() instanceof TypeRecord) {
 			TypeUser type = IrFactory.eINSTANCE.createTypeUser();
 			type.setDeclaration(decl);
 			return type;
 		}
 		return decl.getType();
 	}
 	
 	static public IntegerLiteral lit(Scope scope, int val) {
 		IntegerLiteral v = IrFactory.eINSTANCE.createIntegerLiteral();
 		v.setValue(val);
 		v.setType(TypeSystem.createTypeInt());
 		v.setContext(scope);
 		v.setId(Util.getDefinitionId());
 		return v;
 	}
 	static public IntegerLiteral lit(int val) {return lit(null,val);}
 	
 	static public IntegerLiteral lit(Scope scope, long val) {
 		IntegerLiteral v = IrFactory.eINSTANCE.createIntegerLiteral();
 		v.setValue(val);
 		v.setType(TypeSystem.createTypeUInt(32));
 		v.setContext(scope);
 		v.setId(Util.getDefinitionId());
 		return v;
 	}
 	static public IntegerLiteral lit(long val) {return lit(null,val);}
 
 	static public FloatLiteral lit(Scope scope, double val) {
 		FloatLiteral v = IrFactory.eINSTANCE.createFloatLiteral();
 		v.setValue(val);
 		v.setType(TypeSystem.createTypeFloat());
 		v.setContext(scope);
 		v.setId(Util.getDefinitionId());
 		return v;
 	}
 	static public FloatLiteral lit(double val) {return lit(null,val);}
 
 	static public BooleanLiteral lit(Scope scope, boolean val) {
 		BooleanLiteral v = IrFactory.eINSTANCE.createBooleanLiteral();
 		v.setValue(val);
 		v.setType(TypeSystem.createTypeBool());
 		v.setContext(scope);
 		v.setId(Util.getDefinitionId());
 		return v;
 	}
 	static public BooleanLiteral lit(boolean val) {return lit(null,val);}
 
 	static public StringLiteral lit(Scope scope, String val) {
 		StringLiteral v = IrFactory.eINSTANCE.createStringLiteral();
 		v.setValue(val);
 		v.setType(TypeSystem.createTypeString());
 		v.setContext(scope);
 		v.setId(Util.getDefinitionId());
 		return v;
 	}
 	static public StringLiteral lit(String val) {return lit(null,val);}
 
 	static public Expression createExpressionByType(Scope scope, Type type) {
 		Expression expr = IrFactory.eINSTANCE.createExpression();
 		expr.setContext(scope);
 		expr.setType(type);
 		expr.setId(Util.getDefinitionId());
 		return expr;
 	}
 
 	static public Expression createExpression(Scope scope, Variable var) {
 		VariableExpression expr = IrFactory.eINSTANCE.createVariableExpression();
 		expr.setVariable(var);
 		expr.setContext(scope);
 		expr.setType(var.getType());
 		expr.setId(Util.getDefinitionId());
 		return expr;
 	}
 
 	static public Expression createExpression(Scope scope, VariableExpression var) {
 		VariableExpression expr = IrFactory.eINSTANCE.createVariableExpression();
 		expr.setVariable(var.getVariable());
 		expr.getIndex().addAll(var.getIndex());
 		expr.getMember().addAll(var.getMember());
 		expr.setContext(scope);
 		expr.setType(((Variable)var.getVariable()).getType());
 		expr.setId(Util.getDefinitionId());
 		return expr;
 	}
 	
 	static public Expression createExpression(Expression op1, String op, Expression op2) {
 		BinaryExpression expr = IrFactory.eINSTANCE.createBinaryExpression();
 		expr.setOperand1(op1);
 		expr.setOperand2(op2);
 		expr.setOperator(op);
 		Type type=null;
 		/* TODO TypeSystem seems to not be updated to dig up the type of a lambda function etc
 		try {
 			type=TypeSystem.LUB(op1.getType(), op2.getType());
 		} catch (TypeException e) {
 			if(op1.getType()!=null) {
 				type=op1.getType();
 			} else if(op2.getType()!=null) {
 				type=op2.getType();
 			}
 		}*/
 		expr.setType(type);
 		expr.setContext(op1.getContext());
 		expr.setId(Util.getDefinitionId());
 		return expr;
 	}
 
 	static public Expression createExpression(Expression op1, String op) {
 		UnaryExpression expr = IrFactory.eINSTANCE.createUnaryExpression();
 		expr.setOperand(op1);
 		expr.setOperator(op);
 		expr.setType(op1.getType());
 		expr.setContext(op1.getContext());
 		expr.setId(Util.getDefinitionId());
 		return expr;
 	}
 	
 	static public Expression createExpression(Scope scope, VariableReference var) {
 		VariableExpression expr = IrFactory.eINSTANCE.createVariableExpression();
 		expr.setVariable(var.getDeclaration());
 		expr.getMember().addAll(var.getMember());
 		expr.getIndex().addAll(var.getIndex());
 		expr.setType(var.getType());
 		expr.setContext(scope);
 		expr.setId(Util.getDefinitionId());
 		return expr;
 	}
 
 	static public Expression createExpression(Scope scope, VariableExternal var) {
 		VariableExpression expr = IrFactory.eINSTANCE.createVariableExpression();
 		expr.setVariable(var);
 		expr.setType(var.getType());
 		expr.setContext(scope);
 		expr.setId(Util.getDefinitionId());
 		return expr;
 	}
 
 	static public Declaration getDeclaration(Declaration decl) {
 		if( decl instanceof VariableImport) {
 			VariableImport imp = (VariableImport) decl;
 			if(imp.getName().startsWith("dprint"))
 				return null;
 			try {
 				Declaration var = ActorDirectory.findVariable(imp);
 				return var;
 			} catch (DirectoryException x) {
 				System.err.println("[UtilIR] Internal Error - getDeclaration");
 			}
 		} else if(decl instanceof ForwardDeclaration) 
 			return ((ForwardDeclaration) decl).getDeclaration();
 		return decl; 
 	}
 
 	static public Type getProcInputType(Declaration decl,int i) {
 		Declaration d =UtilIR.getDeclaration(decl);
 		if(d instanceof Variable) {
 			Variable v = (Variable) d;
 			if(v.getType() instanceof TypeProc) {
 				TypeProc t = (TypeProc) v.getType();
 				return t.getInputTypes().get(i);
 			} else if(v.getType() instanceof TypeLambda) {
 				TypeLambda t = (TypeLambda) v.getType();
 				return t.getInputTypes().get(i);
 			} 
 		} else if(d instanceof VariableExternal) {
 			VariableExternal v = (VariableExternal) d;
 			if(v.getType() instanceof TypeProc) {
 				TypeProc t = (TypeProc) v.getType();
 				return t.getInputTypes().get(i);
 			} else if(v.getType() instanceof TypeLambda) {
 				TypeLambda t = (TypeLambda) v.getType();
 				return t.getInputTypes().get(i);
 			} 
 		}
 		return null;
 	}
 
 	static public Type getProcOutputType(Declaration decl,int i) {
 		Declaration d =UtilIR.getDeclaration(decl);
 		if(d instanceof Variable) {
 			Variable v = (Variable) d;
 			if(v.getType() instanceof TypeProc) {
 				TypeProc t = (TypeProc) v.getType();
 				return t.getOutputTypes().get(i);
 			} 
 		} else if(d instanceof VariableExternal) {
 			VariableExternal v = (VariableExternal) d;
 			if(v.getType() instanceof TypeProc) {
 				TypeProc t = (TypeProc) v.getType();
 				return t.getOutputTypes().get(i);
 			} 
 		}
 		return null;
 	}
 
 	static public boolean isRecord(Type type) {
 		return getType(type) instanceof TypeRecord;
 	}
 	
 	static public boolean isListOrRecord(Type type) {
 		return (getType(type) instanceof TypeList) || (getType(type) instanceof TypeRecord);
 	}
 
 	static public boolean isList(Type type) {
 		return getType(type) instanceof TypeList;
 	}
 	
 	static public Type getFinalType(VariableReference var) {
 		Type type = var.getType();
 		int i=0;
 		if(var.getMember().isEmpty())
 			i = var.getIndex().isEmpty() ? 0 : var.getIndex().size();
 		else {
 			Member member = var.getMember().get(var.getMember().size()-1);
 			i = member.getIndex().isEmpty() ? 0 : member.getIndex().size();
 			type = member.getType();
 		}
 		while(i>0 && type instanceof TypeList) {
 			type = ((TypeList)type).getType();
 			i--;
 		}
 		return type;
 	}
 	
 	static public Type getFinalType(Expression exprin) {
 		if(exprin instanceof VariableExpression) {
 			VariableExpression expr = (VariableExpression) exprin;
 			Declaration d = ((VariableExpression) expr).getVariable();
 			Variable var = null;
 			Type type = null;
 			if(d instanceof Variable) {
 				var = (Variable) d;
 				type = var.getType();
 			}
 			int i=0;
 			if(expr.getMember().isEmpty())
 				i = expr.getIndex().isEmpty() ? 0 : expr.getIndex().size();
 			else {
 				Member member = expr.getMember().get(expr.getMember().size()-1);
 				i = member.getIndex().isEmpty() ? 0 : member.getIndex().size();
 				type = member.getType();
 			}
 			while(i>0 && type instanceof TypeList) {
 				type = ((TypeList)type).getType();
 				i--;
 			}
 			return type;
 		}
 		return exprin.getType(); //For now this is mostly returning null 
 	}
 	
 	static public TypeDeclaration getTypeDeclaration(Type type) {
 		if(type instanceof TypeUser) {
 			if( ((TypeUser) type).getDeclaration() instanceof TypeDeclarationImport) {
 				TypeDeclarationImport imp = (TypeDeclarationImport)((TypeUser) type).getDeclaration();
 				try {
 					TypeDeclaration decl = ActorDirectory.findTypeDeclaration(imp);
 					return decl;
 				} catch (DirectoryException x) {
 					System.err.println("[UtilIR] Internal Error - getDeclaration");
 				}
 			} else if( ((TypeUser) type).getDeclaration() instanceof TypeDeclaration) {
 				return (TypeDeclaration) ((TypeUser) type).getDeclaration();
 			}
 		}
 		System.err.println("This was not a TypeUser type! " + type.toString());
 		assert(false);
 		return null; 
 	}
 	
 	static public Type getType(Declaration declIn) {
 		if( declIn instanceof TypeDeclarationImport) {
 			try {
 				TypeDeclaration decl = ActorDirectory.findTypeDeclaration((TypeDeclarationImport) declIn);
 				return decl.getType();
 			} catch (DirectoryException x) {
 				System.err.println("[UtilIR] Internal Error - getType");
 			}
 		} else if( declIn instanceof TypeDeclaration) {
 			return ((TypeDeclaration) declIn).getType();
 		}
 		System.err.println("This was not a TypeDeclaration!");
 		return null; 
 	}
 
 	static public TypeDeclaration getTypeDeclaration(Declaration declIn) {
 		if( declIn instanceof TypeDeclarationImport) {
 			try {
 				TypeDeclaration decl = ActorDirectory.findTypeDeclaration((TypeDeclarationImport) declIn);
 				return decl;
 			} catch (DirectoryException x) {
 				System.err.println("[UtilIR] Internal Error - getTypeDeclaration");
 			}
 		} else if( declIn instanceof TypeDeclaration) {
 			return (TypeDeclaration) declIn;
 		}
 		System.err.println("This was not a TypeDeclaration!");
 		return null; 
 	}
 
 	static public Type getType(Type type) {
 		if(type instanceof TypeUser) {
 			TypeDeclaration decl = getTypeDeclaration(type);
 			return decl.getType() instanceof TypeUser ? ((TypeDeclaration)((TypeUser) decl.getType()).getDeclaration()).getType() : decl.getType();
 		} else {
 			return type;
 		}
 	}
 	
 	static public boolean isLocalFunc(Declaration decl) {
 		if(decl instanceof ForwardDeclaration) 
 			decl = ((ForwardDeclaration) decl).getDeclaration();
 		if(decl instanceof Variable) {
 			Variable var = (Variable) decl;
 			Type type = getType(var.getType());
 			if(	type instanceof TypeLambda ||
 				type instanceof TypeProc
 				) {
 				return true;
 			}
 		}		
 		return false;
 	}
 
 	//Returns true if this is something that could be a C variable
 	static public boolean isNormalVariable(Declaration decl) {
 		if(decl instanceof Variable) {
 			Variable var = (Variable) decl;
 			Type type = getType(var.getType());
 			if(	type instanceof TypeBool ||
 				type instanceof TypeInt ||
 				type instanceof TypeList ||
 				type instanceof TypeFloat ||
 				type instanceof TypeUint ||
 				type instanceof TypeString ||
 				type instanceof TypeRecord ||
 				type instanceof TypeUndef
 				) {
 				return true;
 			}
 		}		
 		return false;
 	}
 	
 	static private boolean literalList(ListExpression list) {
 		if(list==null) return false;
 		if(list.getGenerators().isEmpty() && !list.getExpressions().isEmpty()) {
 			for(Expression e : list.getExpressions()) {
 				if(e instanceof ListExpression) {
 					if(!literalList((ListExpression)e))
 						return false;
 				} else if(!(e instanceof LiteralExpression))
 					return false;
 			}
 		} else {
 			return false;
 		}
 		return true;
 	}
 
 	static private boolean literalTypeConstruct(TypeConstructorCall call) {
 		if(call==null) return false;
 		if(!call.getParameters().isEmpty()) {
 			for(Expression e : call.getParameters()) {
 				if(!(e instanceof LiteralExpression))
 					return false;
 			}
 		} else {
 			return false;
 		}
 		return true;
 	}
 
 	static public boolean isLiteralExpression(Expression expr) {
 		if(expr instanceof LiteralExpression) {
 			return true;
 		} else if(expr instanceof ListExpression) {
 			return literalList((ListExpression) expr);
 		} else if(expr instanceof TypeConstructorCall) {
 			return literalTypeConstruct((TypeConstructorCall) expr);
 		}
 		return false;
 	}
 	
 	static public boolean refLiteralExpression(Expression expr) {
 		if(expr instanceof VariableExpression) {
 			Declaration d = ((VariableExpression) expr).getVariable();
 			return isNormalConstant(d);
 		}
 		return false;
 	}
 
 	//Returns true if this is something that could be a C-variable that is literal constant at compile time
 	static public boolean isNormalConstant(Declaration decl) {
 		if(decl instanceof Variable) {
 			Variable var = (Variable) decl;
 			if(!var.isConstant())
 				return false;
 			return isLiteralExpression(var.getInitValue());
 		}		
 		return false;
 	}
 
 	//Returns true if this is something that could be a C-variable but needs a constructor
 	static public boolean isNormalInit(Declaration decl) {
 		return isNormalVariable(decl) && !isNormalConstant(decl) && ((Variable) decl).getInitValue() != null;
 	}	
 		
 	//Returns true if this is something that could be a C-variable definition only
 	static public boolean isNormalDef(Declaration decl) {
 		return isNormalVariable(decl) && ((Variable) decl).getInitValue() == null;
 	}	
 
 	static public Expression createExpression(Scope scope, Variable var, List<Expression> index, Variable member, List<Expression> memberIndex) {
 		if(var==null)
 			return null;
 		VariableExpression expr = IrFactory.eINSTANCE.createVariableExpression();
 		expr.setVariable(var);
 		expr.setType(var.getType());
 		if(isRecord(var.getType()) && member !=null) {
 			Member elem = IrFactory.eINSTANCE.createMember();
 			elem.setName(member.getName());
 			elem.setType(member.getType());
 			if(memberIndex!=null)
 				elem.getIndex().addAll(memberIndex);
 			expr.getMember().add(elem);
 		}
 		if(index!=null) {
 			expr.getIndex().addAll(index);
 		}
 		expr.setContext(scope);
 		expr.setId(Util.getDefinitionId());
 		return expr;
 	}
 
 	static public Expression createExpression(Scope scope, Variable var, List<Expression> index, List<Member> member) {
 		VariableExpression expr = IrFactory.eINSTANCE.createVariableExpression();
 		expr.setVariable(var);
 		if(isRecord(var.getType()) && member !=null) {
 			expr.getMember().addAll(member);
 		}
 		if(index!=null) {
 			expr.getIndex().addAll(index);
 		}
 		expr.setContext(scope);
 		expr.setId(Util.getDefinitionId());
 		return expr;
 	}
 	
 	static public Expression createExpression(Scope scope, Variable var, Variable member) {
 		return createExpression(scope, var, null, member, null);
 	}
 	
 	static public FunctionCall createFunctionCall(Scope scope, Declaration decl, List<Expression> params) {
 		if(decl instanceof Variable) {
 			return createFunctionCall(scope, (Variable) decl, params);
 		} else if(decl instanceof VariableExternal) {
 			return createFunctionCall(scope, (VariableExternal) decl, params);
 		} else {
 			//Oops
 			return null;
 		}
 	}
 
 	static public FunctionCall createFunctionCall(Scope scope, Variable decl, List<Expression> params) {
 		FunctionCall fun = IrFactory.eINSTANCE.createFunctionCall();
 		if(decl.getInitValue()!=null && decl.isConstant()) {
 			fun.setFunction(decl.getInitValue());
 		} else {
 			//Create an expression of the non constant function declaration
 			VariableExpression var = IrFactory.eINSTANCE.createVariableExpression();
 			var.setId(Util.getDefinitionId());
 			var.setType(decl.getType());
 			var.setVariable(decl);
 			fun.setFunction(var);			
 		}
 		fun.setType(decl.getType());
 		for(Expression e : params) {
 			if(e.getContext()==null) {
 				e.setContext(scope);
 			}
 			fun.getParameters().add(e);
 		}
 		fun.setContext(scope);
 		fun.setId(Util.getDefinitionId());
 		return fun;
 	}
 	
 	static public FunctionCall createFunctionCall(Scope scope, VariableExternal decl, List<Expression> params) {
 		FunctionCall fun = IrFactory.eINSTANCE.createFunctionCall();
 		
 		//Create an expression of the external function declaration
 		VariableExpression external = IrFactory.eINSTANCE.createVariableExpression();
 		external.setId(Util.getDefinitionId());
 		external.setType(decl.getType());
 		external.setVariable(decl);
 		fun.setFunction(external);
 		
 		fun.setType(decl.getType());
 		for(Expression e : params) {
 			if(e.getContext()==null) {
 				e.setContext(scope);
 			}
 			fun.getParameters().add(e);
 		}
 		fun.setId(Util.getDefinitionId());
 		fun.setContext(scope);
 		return fun;
 	}
 	
 	static public ProcCall createProcCall(Scope scope, Declaration decl, List<Expression> inParams, List<VariableReference> outParams) {
 		return createProcCall(-1, scope, decl, inParams, outParams);
 	}
 	static public ProcCall createProcCall(int pos, Scope scope, Declaration decl, 
 					List<Expression> inParams, List<VariableReference> outParams) {
 		ProcCall stmt = IrFactory.eINSTANCE.createProcCall();
 		stmt.setProcedure(decl);
 		for(Expression e : inParams) {
 			if(e.getContext()==null) {
 				e.setContext(scope);
 			}
 			stmt.getInParameters().add(e);
 		}
 		if(outParams != null)
 			for(VariableReference r : outParams) {
 				stmt.getOutParameters().add(r);
 			}
 		if(scope instanceof Block && pos < 0) {
 			((Block) scope).getStatements().add(stmt);
 		} else if(scope instanceof Block) {
 			((Block) scope).getStatements().add(pos,stmt);
 		}
 		if(scope instanceof Action && pos < 0) {
 			((Action) scope).getStatements().add(stmt);
 		} else if(scope instanceof Action) {
 			((Action) scope).getStatements().add(pos,stmt);
 		}
 		return stmt;
 	}
 
 	static public VariableReference createVarRef(Variable var) {
 		VariableReference target = IrFactory.eINSTANCE.createVariableReference();
 		target.setDeclaration(var);
 		target.setType(var.getType());
 		return target;
 	}
 	
 	static public VariableReference createVarRef(Variable var, List<Expression> index) {
 		VariableReference target = IrFactory.eINSTANCE.createVariableReference();
 		target.setDeclaration(var);
 		target.getIndex().addAll(index);
 		Type type = var.getType();
 		for(int i = 0; i < index.size(); i++) {
 			type = ((TypeList) type).getType();
 		}
 		target.setType(type);
 		return target;
 	}
 	
 	static public VariableReference createVarRef(Variable var, List<Expression> index, Variable member, List<Expression> indexMember) {
 		VariableReference target = IrFactory.eINSTANCE.createVariableReference();
 		target.setDeclaration(var);
 		Type type = var.getType();
 		if(index!=null) {
 			target.getIndex().addAll(index);
 			for(int i = 0; i < index.size(); i++) {
 				type = ((TypeList) type).getType();
 			}
 		}
 		if(member!=null) {
 			Member elem = IrFactory.eINSTANCE.createMember();
 			elem.setName(member.getName());
 			elem.setType(member.getType());
 			target.getMember().add(elem);
 			type = member.getType();
 			if(indexMember!=null) {
 				elem.getIndex().addAll(indexMember);
 				for(int i = 0; i < indexMember.size(); i++) {
 					type = ((TypeList) type).getType();
 				}
 			}			
 		}
 		target.setType(type);
 		return target;
 	}
 
 	static public VariableReference createVarRef(VariableReference target, Variable member) {
 		Member elem = IrFactory.eINSTANCE.createMember();
 		elem.setName(member.getName());
 		elem.setType(member.getType());
 		target.getMember().add(elem);		
 		return target;
 	}
 
 	static public VariableReference createVarRef(Variable var, Variable member) {
 		VariableReference target = createVarRef(var);
 		target = createVarRef(target, member);
 		return target;
 	}
 
 	static public Assign createAssign(Scope scope, Variable target, Expression expr) {
 		return createAssign(-1,scope,target,expr);
 	}
 	static public Assign createAssign(int pos, Scope scope, Variable target, Expression expr) {
 		VariableReference targetUse = IrFactory.eINSTANCE.createVariableReference();
 		targetUse.setDeclaration(target);
 		targetUse.setType(target.getType());
 		Assign assign = IrFactory.eINSTANCE.createAssign();
 		assign.setExpression(expr);	
 		assign.setTarget(targetUse);
 		if(scope instanceof Block && pos < 0) {
 			((Block) scope).getStatements().add(assign);
 		} else if(scope instanceof Block) {
 			((Block) scope).getStatements().add(pos,assign);
 		}
 		if(scope instanceof Action && pos < 0) {
 			((Action) scope).getStatements().add(assign);
 		} else if(scope instanceof Action) {
 			((Action) scope).getStatements().add(pos,assign);
 		}
 		return assign;
 	}
 	
 	static public Assign createAssign(Scope scope, VariableReference targetUse, Expression expr) {
 		return createAssign(-1,scope,targetUse,expr);
 	}
 
 	static public Assign createAssign(int pos, Scope scope, VariableReference targetUse, Expression expr) {
 		Assign assign = IrFactory.eINSTANCE.createAssign();
 		assign.setExpression(expr);	
 		assign.setTarget(targetUse);				
 		if(scope instanceof Block && pos < 0) {
 			((Block) scope).getStatements().add(assign);
 		} else if(scope instanceof Block) {
 			((Block) scope).getStatements().add(pos,assign);
 		}
 		if(scope instanceof Action && pos < 0) {
 			((Action) scope).getStatements().add(assign);
 		} else if(scope instanceof Action) {
 			((Action) scope).getStatements().add(pos,assign);
 		}
 		return assign;
 	}
 	
 	/* member is a VariableDef corresponding to a member of the target structure type */
 	static public Assign createStructAssign(Scope scope, Variable target, Variable member, Expression expr) {
 		if(isRecord(target.getType()) && member !=null) {
 			VariableReference targetUse = IrFactory.eINSTANCE.createVariableReference();
 			targetUse.setDeclaration(target);
 			targetUse.setType(target.getType());
 			Member elem = IrFactory.eINSTANCE.createMember();
 			elem.setName(member.getName());
 			elem.setType(member.getType());
 			targetUse.getMember().add(elem);
 			return createAssign(scope,targetUse,expr);
 		} else {
 			//Ooops not a struct type just ignore it
 			return createAssign(scope,target,expr);
 		}
 	}
 	
 	static public Assign createStructAssign(Scope scope, Variable target, String member, Expression expr) {
 		if(isRecord(target.getType())) {
 			Variable m=null;
 			for(Variable d : ((TypeRecord)target.getType()).getMembers()) {
 				if(d.getName().equals(member)) {
 					m=d;
 					break;
 				}
 			}
 			return createStructAssign(scope,target, m ,expr);
 		} else {
 			//Ooops not a struct type just ignore it
 			return createAssign(scope,target,expr);
 		}
 	}
 
 	/* member is a VariableDef corresponding to a member of the target structure type */
 	static public Assign createStructAssign(Scope scope, Variable dst, Variable dstMember, Variable src, Variable srcMember) {
 		if(isRecord(dst.getType()) && dstMember !=null) {
 			VariableReference targetUse = IrFactory.eINSTANCE.createVariableReference();
 			targetUse.setDeclaration(dst);
 			targetUse.setType(dst.getType());
 			Member elem = IrFactory.eINSTANCE.createMember();
 			elem.setName(dstMember.getName());
 			elem.setType(dstMember.getType());
 			targetUse.getMember().add(elem);
 			return createAssign(scope,targetUse,createExpression(scope,src,srcMember));
 		} else {
 			//Ooops not a struct type just ignore it
 			return createAssign(scope,dst,createExpression(scope,src,srcMember));
 		}
 	}
 
 	public static Block createBlock(Scope outer) {
 		Block block = IrFactory.eINSTANCE.createBlock();
 		block.setOuter(outer);
		block.setId(Util.getDefinitionId());
 
 		return block;
 	}
 
 	public static Expression createPortExpression(Scope scope, boolean isInput, List<Port> portList, Port port) {
 		int i=portList.indexOf(port);
 		if(i<0) {
 			String name =port.getName();
 			for(Port p : portList) {
 				if(p.getName().equals(name)){
 					i=portList.indexOf(p);
 					break;
 				}
 			}
 		}
 		return UtilIR.createExpression(scope,UtilIR.createVarDef(null, (isInput?"IN":"OUT")+i+"_"+port.getName(), port.getType()));
 	}
 
 	static public Declaration createVarDef(Scope body, String name, Type type, 
 									boolean constant, boolean extern, Expression val) {
 		if(extern) {
 			VariableExternal var = IrFactory.eINSTANCE.createVariableExternal();
 			var.setName(name);
 			var.setType(type);
 			var.setScope(body);
 			var.setId(Util.getDefinitionId());
 			if(body !=null) {
 				body.getDeclarations().add(var);
 			}
 			return var;			
 		} else {
 			Variable var = IrFactory.eINSTANCE.createVariable();
 			var.setName(name);
 			var.setType(type);
 			var.setConstant(constant);
 			var.setInitValue(val);
 			var.setScope(body);
 			var.setId(Util.getDefinitionId());
 			if(body !=null) {
 				body.getDeclarations().add(var);
 			}
 			return var;
 		}
 	}
 
 	static public Variable createVarDef(Scope body, String name, Type type) {
 		return (Variable) createVarDef(body,name,type,false,false,null);
 	}
 
 	static public Variable createVarDef(Block body, String name, Type type) {
 		return (Variable) createVarDef(body,name,type,false,false,null);
 	}
 	
 	static public Variable createVarDef(Block body, String name, int val) {
 		IntegerLiteral v = IrFactory.eINSTANCE.createIntegerLiteral();
 		v.setId(Util.getDefinitionId());
 		v.setValue(val);
 		return (Variable) createVarDef(body,name,TypeSystem.createTypeInt(),false,false,v);
 	}
 	
 	static public ForEach createForEach(Block body, List<Generator> gens) {
 		ForEach foreach = IrFactory.eINSTANCE.createForEach();
 		Block loop = IrFactory.eINSTANCE.createBlock();
 		foreach.setBody(loop);
 		foreach.getGenerators().addAll(gens);
 		body.getStatements().add(foreach);
 		return foreach;
 	}
 
 	/* body is where the while statement is placed, loop is the body of the loop 
 	 * index is a name of the int that is incremented, start is the first value
 	 * end_1 forms the condition index<end_1. Note loop needs to be filled in 
 	 * already since we put the index increment at the end.*/
 	static public Variable createSimpleLoop(int pos, Scope body, Block loop, String index, int start, Expression end_1) {
 		if(body instanceof Block)
 			return createSimpleLoop(pos, (Block) body, loop, index, start, end_1);
 		else if(body instanceof Action)
 			return createSimpleLoop(pos, (Action) body, loop, index, start, end_1);
 		else
 			throw new RuntimeException("A scope not a block or action, that's refreshing, but I don't understand!");
 	}
 	static public Block createSimpleLoop(Block body, Block loop, String index, int start, Expression end_1) {
 		Variable i = createVarDef(body, index, TypeSystem.createTypeInt());
 		createAssign(body,i,lit(start));
 		WhileLoop w = IrFactory.eINSTANCE.createWhileLoop();
 		w.setCondition(createExpression(createExpression(body,i), "<", end_1));
 		createAssign(loop,i,createExpression(createExpression(loop,i), "+", lit(1)));
 		w.setBody(loop);
 		body.getStatements().add(w);
 		return loop;
 	}
 
 	static public Variable createSimpleLoop(int pos, Block body, Block loop, String index, int start, Expression end_1) {
 		Variable i = createVarDef(body, index, TypeSystem.createTypeInt());
 		if(pos<0) createAssign(body,i,lit(start)); //When using neg pos (i.e. last) we need to add counter init first
 		WhileLoop w = IrFactory.eINSTANCE.createWhileLoop();
 		w.setCondition(createExpression(createExpression(body,i), "<", end_1));
 		createAssign(loop,i,createExpression(createExpression(loop,i), "+", lit(1)));
 		w.setBody(loop);
 		if(pos<0)
 			body.getStatements().add(w);
 		else
 			body.getStatements().add(pos, w);
 		if(pos>=0) createAssign(pos,body,i,lit(start)); //When using a specific pos we need to push counter init after instead while
 		return i;
 	}
 	
 	static public Variable createSimpleLoop(int pos, Action body, Block loop, String index, int start, Expression end_1) {
 		Variable i = createVarDef(body, index, TypeSystem.createTypeInt());
 		if(pos<0) createAssign(body,i,lit(start)); //When using neg pos (i.e. last) we need to add counter init first
 		WhileLoop w = IrFactory.eINSTANCE.createWhileLoop();
 		w.setCondition(createExpression(createExpression(body,i), "<", end_1));
 		createAssign(loop,i,createExpression(createExpression(loop,i), "+", lit(1)));
 		w.setBody(loop);
 		if(pos<0)
 			body.getStatements().add(w);
 		else
 			body.getStatements().add(pos, w);
 		if(pos>=0) createAssign(pos,body,i,lit(start)); //When using a specific pos we need to push counter init after instead while
 		return i;
 	}
 
 	static public Block createSimpleLoop(Action body, Block loop, String index, int start, Expression end_1) {
 		Variable i = createVarDef((Scope) body, index, TypeSystem.createTypeInt());
 		createAssign(body,i,lit(start));
 		WhileLoop w = IrFactory.eINSTANCE.createWhileLoop();
 		w.setCondition(createExpression(createExpression(body,i), "<", end_1));
 		createAssign(loop,i,createExpression(createExpression(loop,i), "+", lit(1)));
 		w.setBody(loop);
 		body.getStatements().add(w);
 		return loop;
 	}
 
 	static public ReturnValue createReturn(Block body, Variable var) {
 		ReturnValue ret = IrFactory.eINSTANCE.createReturnValue();
 		ret.setValue(createExpression(body,var));
 		if(body!=null) {
 			body.getStatements().add(ret);
 		}
 		return ret;
 	}
 	
 	static public IfStatement createIf(Block scope, Expression condition, Block thenBlock, Block elseBlock) {
 		IfStatement ifs= IrFactory.eINSTANCE.createIfStatement();
 		ifs.setCondition(condition);
 		ifs.setThenBlock(thenBlock);
 		ifs.setElseBlock(elseBlock);
 		if(scope!=null)
 			scope.getStatements().add(ifs);
 		return ifs;
 	}
 	
 	static public IfStatement createIf(Block scope, Expression condition, Statement thenStatement, Statement elseStatement) {
 		Block thenBlock = createBlock(scope);
 		Block elseBlock = createBlock(scope);
 		thenBlock.getStatements().add(thenStatement);
 		elseBlock.getStatements().add(elseStatement);
 		return createIf(scope,condition,thenBlock, elseBlock);
 	}
 	
 	//No insert versions -------------------------------------
 	static public ProcCall createProcCallN(Scope scope, Declaration decl, List<Expression> inParams, List<VariableReference> outParams) {
 		ProcCall stmt = IrFactory.eINSTANCE.createProcCall();
 		stmt.setProcedure(decl);
 		for(Expression e : inParams) {
 			if(e.getContext()==null) {
 				e.setContext(scope);
 			}
 			stmt.getInParameters().add(e);
 		}
 		if(outParams != null)
 			for(VariableReference r : outParams) {
 				stmt.getOutParameters().add(r);
 			}
 		return stmt;
 	}
 
 	static public Assign createAssignN(Scope scope, Variable target, Expression expr) {
 		return createAssignN(-1,scope,target,expr);
 	}
 	static public Assign createAssignN(int pos, Scope scope, Variable target, Expression expr) {
 		VariableReference targetUse = IrFactory.eINSTANCE.createVariableReference();
 		targetUse.setDeclaration(target);
 		targetUse.setType(target.getType());
 		Assign assign = IrFactory.eINSTANCE.createAssign();
 		assign.setExpression(expr);	
 		assign.setTarget(targetUse);				
 		return assign;
 	}
 	
 	static public Assign createAssignN(Scope scope, VariableReference targetUse, Expression expr) {
 		return createAssignN(-1,scope,targetUse,expr);
 	}
 
 	static public Assign createAssignN(int pos, Scope scope, VariableReference targetUse, Expression expr) {
 		Assign assign = IrFactory.eINSTANCE.createAssign();
 		assign.setExpression(expr);	
 		assign.setTarget(targetUse);				
 		return assign;
 	}
 	
 	/* member is a VariableDef corresponding to a member of the target structure type */
 	static public Assign createStructAssignN(Scope scope, Variable target, Variable member, Expression expr) {
 		if(isRecord(target.getType()) && member !=null) {
 			VariableReference targetUse = IrFactory.eINSTANCE.createVariableReference();
 			targetUse.setDeclaration(target);
 			targetUse.setType(target.getType());
 			Member elem = IrFactory.eINSTANCE.createMember();
 			elem.setName(member.getName());
 			elem.setType(member.getType());
 			targetUse.getMember().add(elem);
 			return createAssignN(scope,targetUse,expr);
 		} else {
 			//Ooops not a struct type just ignore it
 			return createAssignN(scope,target,expr);
 		}
 	}
 	
 	static public Assign createStructAssignN(Scope scope, Variable target, String member, Expression expr) {
 		if(isRecord(target.getType())) {
 			Variable m=null;
 			for(Variable d : ((TypeRecord)target.getType()).getMembers()) {
 				if(d.getName().equals(member)) {
 					m=d;
 					break;
 				}
 			}
 			return createStructAssignN(scope,target, m ,expr);
 		} else {
 			//Ooops not a struct type just ignore it
 			return createAssignN(scope,target,expr);
 		}
 	}
 
 	/* member is a VariableDef corresponding to a member of the target structure type */
 	static public Assign createStructAssignN(Scope scope, Variable dst, Variable dstMember, Variable src, Variable srcMember) {
 		if(isRecord(dst.getType()) && dstMember !=null) {
 			VariableReference targetUse = IrFactory.eINSTANCE.createVariableReference();
 			targetUse.setDeclaration(dst);
 			targetUse.setType(dst.getType());
 			Member elem = IrFactory.eINSTANCE.createMember();
 			elem.setName(dstMember.getName());
 			elem.setType(dstMember.getType());
 			targetUse.getMember().add(elem);
 			return createAssignN(scope,targetUse,createExpression(scope,src,srcMember));
 		} else {
 			//Ooops not a struct type just ignore it
 			return createAssignN(scope,dst,createExpression(scope,src,srcMember));
 		}
 	}
 
 	static public Variable createVarDefN(Block body, String name, Type type, 
 									boolean constant, boolean extern, Expression val) {
 		Variable var = IrFactory.eINSTANCE.createVariable();
 		var.setName(name);
 		var.setType(type);
 		var.setConstant(false);
 		var.setInitValue(val);
 		var.setScope(body);
 		var.setId(Util.getDefinitionId());
 		return var;
 	}
 
 	static public Variable createVarDefN(Block body, String name, Type type) {
 		return createVarDefN(body,name,type,false,false,null);
 	}
 	
 	static public Variable createVarDefN(Block body, String name, int val) {
 		IntegerLiteral v = IrFactory.eINSTANCE.createIntegerLiteral();
 		v.setValue(val);
 		return createVarDefN(body,name,TypeSystem.createTypeInt(),false,false,v);
 	}	
 	
 	public static String namespace2Path(List<String> ns) {
 		String ret="";
 		if(ns != null) {
 			for(Iterator<String> i=ns.iterator();i.hasNext();) {
 				String s=i.next();
 				ret+=s;
 				if(i.hasNext()) ret+=File.separator;
 			}
 		}
 		return ret;
 	}
 	
 	public static String marshall(String s) {
 		s = s.replace("<=", "LTE");
 		s = s.replace("<", "LT");
 		s = s.replace(">=", "GTE");
 		s = s.replace("<", "GT");
 		s = s.replace("&&", "AND");
 		s = s.replace("&", "BAND");
 		s = s.replace("/", "DIV");
 		
 		return s;
 	}
 
 	public static String unmarshall(String s) {
 		s = s.replace("LTE", "<=");
 		s = s.replace("LT", "<");
 		s = s.replace("GTE", ">=");
 		s = s.replace("GT", ">");
 		s = s.replace("BAND", "&");
 		s = s.replace("AND", "&&");
 		s = s.replace("DIV", "/");
 		
 		return s;
 	}
 }
