 package org.eclipse.dltk.javascript.structure;
 
 import org.eclipse.dltk.compiler.IElementRequestor.FieldInfo;
 import org.eclipse.dltk.compiler.IElementRequestor.ImportInfo;
 import org.eclipse.dltk.compiler.IElementRequestor.MethodInfo;
 import org.eclipse.dltk.compiler.IElementRequestor.TypeInfo;
 import org.eclipse.dltk.javascript.ast.Expression;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IMethod;
 import org.eclipse.dltk.javascript.typeinfo.model.JSType;
 
 public interface IStructureRequestor {
 
 	public void acceptImport(ImportInfo importInfo);
 
 	void acceptFieldReference(Identifier node);
 
 	void acceptMethodReference(Identifier node, int argCount);
 
	// void acceptTypeReference(JSType type, int position);
 
 	void enterNamespace(String[] namespace);
 
 	void exitNamespace();
 
 	void enterType(TypeInfo typeInfo);
 
 	void exitType(int sourceEnd);
 
 	void enterMethod(MethodInfo methodInfo, Expression identifier,
 			FunctionStatement function, IMethod method);
 
 	void exitMethod(int sourceEnd);
 
 	void exitField(int sourceEnd);
 
 	void enterField(FieldInfo fieldInfo, Expression identifier, JSType type,
 			boolean local);
 
 	void acceptLocalReference(Identifier node, IDeclaration target);
 
 	void enterLocal(Identifier identifier, JSType type);
 
 	void exitLocal(int sourceEnd);
 
 }
