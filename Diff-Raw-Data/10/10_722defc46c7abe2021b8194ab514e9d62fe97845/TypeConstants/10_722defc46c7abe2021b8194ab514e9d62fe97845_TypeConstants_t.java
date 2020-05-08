 package org.napile.compiler.codegen.processors.codegen;
 
 import org.napile.asm.AsmConstants;
 import org.napile.asm.lib.NapileCollectionPackage;
 import org.napile.asm.lib.NapileConditionPackage;
 import org.napile.asm.lib.NapileLangPackage;
 import org.napile.asm.tree.members.types.TypeNode;
 import org.napile.asm.tree.members.types.constructors.ClassTypeNode;
 
 /**
  * @author VISTALL
  * @date 20:37/17.09.12
  */
 public interface TypeConstants
 {
 	TypeNode EXCEPTION = new TypeNode(false, new ClassTypeNode(NapileLangPackage.EXCEPTION));
 
 	TypeNode STRING_BUILDER = new TypeNode(false, new ClassTypeNode(NapileLangPackage.STRING_BUILDER));
 
 	TypeNode NULL_POINTER_EXCEPTION = new TypeNode(false, new ClassTypeNode(NapileLangPackage.NULL_POINTER_EXCEPTION));
 
 	TypeNode COMPARE_RESULT = new TypeNode(false, new ClassTypeNode(NapileConditionPackage.COMPARE_RESULT));
 
	TypeNode NULLABLE_STRING = new TypeNode(true, new ClassTypeNode(NapileLangPackage.STRING));

 	TypeNode ITERATOR__ANY__ = new TypeNode(false, new ClassTypeNode(NapileCollectionPackage.ITERATOR)).visitArgument(AsmConstants.ANY_TYPE);
 }
