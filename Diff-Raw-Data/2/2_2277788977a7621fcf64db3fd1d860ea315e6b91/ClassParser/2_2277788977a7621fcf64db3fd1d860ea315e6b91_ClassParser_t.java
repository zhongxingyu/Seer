 package org.ita.testrefactoring.astparser;
 
 import java.util.List;
 
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.Modifier;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import org.ita.testrefactoring.metacode.Class;
 import org.ita.testrefactoring.metacode.ParserException;
 import org.ita.testrefactoring.metacode.Type;
 
 class ClassParser implements ASTTypeParser<ASTClass> {
 
 	private class ClassVisitor extends ASTVisitor {
 
 		private ASTClass clazz;
 
 		public void setClass(ASTClass clazz) {
 			this.clazz = clazz;
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		public boolean visit(FieldDeclaration fieldDeclaration) {
 			List<ASTNode> nodes = new QuickVisitor().quickVisit(fieldDeclaration);
 
 			Utils.rearrangeArray(nodes, VariableDeclarationFragment.class, Modifier.class);
 
 			VariableDeclarationFragment variableDeclaration = (VariableDeclarationFragment) nodes.get(0);
 
 			// TODO
 			// field.getAccessModifier()
 			// field.getNonAccessModifier()
 
 			ASTField field = clazz.createField(variableDeclaration.getName().toString());
 
 			field.setASTObject(fieldDeclaration);
 
 			// TODO: field.setInitialization
 			field.setParentType(clazz);
 
 			String fieldTypeQualifiedName = fieldDeclaration.getType().resolveBinding().getQualifiedName();
 
 			if (fieldDeclaration.getType().resolveBinding().isPrimitive()) {
 				fieldTypeQualifiedName = "." + fieldTypeQualifiedName;
 			}
 
 			ASTEnvironment environment = clazz.getPackage().getEnvironment();
 			Type fieldType = environment.getTypeCache().get(fieldTypeQualifiedName);
 
 			if (fieldType == null) {
 				String packageName = ASTEnvironment.extractPackageName(fieldTypeQualifiedName);
 
 				ASTPackage pack = environment.getPackageList().get(packageName);
 
 				if (pack == null) {
 					pack = environment.createPackage(packageName);
 				}
 
 				String typeName = ASTEnvironment.extractTypeName(fieldTypeQualifiedName);
 
 				fieldType = environment.createDummyType(typeName, pack);
 			}
 
 			field.setFieldType(fieldType);
 
 			return false;
 		}
 
 		@Override
 		public boolean visit(MethodDeclaration methodDeclaration) {
 			String methodName = methodDeclaration.getName().toString();
 
 			ASTInnerElementAccessModifier accessModifier = new ASTInnerElementAccessModifier();
 			ASTMethodDeclarationNonAccessModifier nonAccessModifier = new ASTMethodDeclarationNonAccessModifier();
 
 			for (Object m : methodDeclaration.modifiers()) {
 				// Se é alguma coisa que não foi prevista, ignora
 				if (!(m instanceof Modifier)) {
 					continue;
 				}
 				Modifier modifier = (Modifier) m;
 
 				if (modifier.isAbstract()) {
 					nonAccessModifier.setAbstract(true);
 				}
 
 				if (modifier.isFinal()) {
 					nonAccessModifier.setFinal(true);
 				}
 
 				if (modifier.isStatic()) {
 					nonAccessModifier.setStatic(true);
 				}
 
 				if (modifier.isPublic()) {
 					accessModifier.setPublic();
 				}
 
 				if (modifier.isProtected()) {
 					accessModifier.setProtected();
 				}
 
 				if (modifier.isPrivate()) {
 					accessModifier.setPrivate();
 				}
 			}
 
 			ASTMethod method = clazz.createMethod(methodName, nonAccessModifier.isAbstract());
 
 			method.setASTObject(methodDeclaration);
 
 			method.setAccessModifier(accessModifier);
 
 			method.setNonAccessModifier(nonAccessModifier);
 
 			if (!method.getNonAccessModifier().isAbstract()) {
 				ASTConcreteMethod concreteMethod = (ASTConcreteMethod) method;
 
 				ASTBlock block = concreteMethod.getBody();
 
 				block.setASTObject(methodDeclaration.getBody());
 
 				BlockParser parser = new BlockParser();
 
 				parser.setBlock(block);
 
 				try {
 					parser.parse();
 				} catch (ParserException e) {
 					throw new Error(e);
 				}
 			}
 
 			return false;
 		}
 	}
 
 	private ASTClass clazz;
 
 	@Override
 	public void setType(ASTClass type) {
 		this.clazz = type;
 	}
 
 	@Override
 	public void parse() throws ParserException {
 		ClassVisitor visitor = new ClassVisitor();
 
 		visitor.setClass(clazz);
 
 		String superClassName = null;
 
 		org.eclipse.jdt.core.dom.Type superclassNode = clazz.getASTObject().getSuperclassType();
 
 		// TODO: Popular a lista de interfaces implementadas
 		// clazz.getASTObject().superInterfaceTypes()
 
 		if (superclassNode != null) {
 			superClassName = superclassNode.resolveBinding().getQualifiedName();
 		}
 
 		ASTEnvironment environment = clazz.getPackage().getEnvironment();
 		Type superClass = environment.getTypeCache().get(superClassName);
 
 		if (superClass.getKind() == TypeKind.UNKNOWN) {
 			// Se antes não era possível saber qual o Kind do tipo, agora sei
 			// que se trata de uma classe
			DummyClass dummyClass = environment.createDummyClass(superClass.getQualifiedName());
 
 			superClass.promote(dummyClass);
 
 			superClass = dummyClass;
 		} else if (superClass.getKind() != TypeKind.CLASS) {
 			throw new ParserException("Super classe de \"" + clazz.getQualifiedName() + "\" inválida (\"" + superClass.getQualifiedName() + "\")");
 		}
 
 		// Aqui superClass deve ser uma classe, já que getKind devolveu CLASS...
 		clazz.setSuperClass((Class) superClass);
 
 		// TODO: Popular os modificadores da classe
 		clazz.getASTObject().accept(visitor);
 	}
 
 }
