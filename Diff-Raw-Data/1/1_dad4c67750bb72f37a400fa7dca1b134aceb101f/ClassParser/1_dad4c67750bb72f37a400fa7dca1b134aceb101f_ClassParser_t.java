 package org.ita.neutrino.codeparser.astparser;
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.IAnnotationBinding;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.Modifier;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import org.ita.neutrino.codeparser.Annotation;
 import org.ita.neutrino.codeparser.Class;
 import org.ita.neutrino.codeparser.Method;
 import org.ita.neutrino.codeparser.ParserException;
 import org.ita.neutrino.codeparser.Type;
 import org.ita.neutrino.codeparser.TypeKind;
 
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
 
 			ASTEnvironment environment = clazz.getPackage().getParent();
 			Type fieldType = environment.getTypeCache().get(fieldTypeQualifiedName);
 
 			if (fieldType == null) {
 				String packageName = ASTEnvironment.extractPackageName(fieldTypeQualifiedName);
 
 				ASTPackage pack = environment.getOrCreatePackage(packageName);
 
 				String typeName = ASTEnvironment.extractTypeName(fieldTypeQualifiedName);
 
 				ITypeBinding typeBinding = fieldDeclaration.getType().resolveBinding();
 
 				if (typeBinding.isClass()) {
 					fieldType = environment.createDummyClass(typeName);
 				} else if (typeBinding.isAnnotation()) {
 					fieldType = environment.createDummyAnnotation(typeName);
 				} else {
 					fieldType = environment.createDummyType(typeName, pack);
 				}
 			}
 
 			field.setFieldType(fieldType);
 
 			return false;
 		}
 
 		@Override
 		public boolean visit(MethodDeclaration methodDeclaration) {
 			String methodName = methodDeclaration.getName().toString();
 
 			ASTInnerElementAccessModifier accessModifier = new ASTInnerElementAccessModifier();
 			ASTMethodDeclarationNonAccessModifier nonAccessModifier = new ASTMethodDeclarationNonAccessModifier();
 
 			// Processa os modificadores do método
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
 
 			ASTMethod method = (ASTMethod) clazz.getOrCreateMethod(methodName);
 
 			method.setASTObject(methodDeclaration);
 
 			method.setAccessModifier(accessModifier);
 
 			method.setNonAccessModifier(nonAccessModifier);
 
 			// Se o método não for abstrato, processa seu bloco
 			if (!method.getNonAccessModifier().isAbstract()) {
 				ASTBlock block = method.getBody();
 
 				block.setASTObject(methodDeclaration.getBody());
 			}
 
 			// Processa as anotações do método
 			for (IAnnotationBinding ab : methodDeclaration.resolveBinding().getAnnotations()) {
 				ASTEnvironment environment = (ASTEnvironment) clazz.getParent().getParent().getParent();
 
 				String annotationQualifiedName = ab.getAnnotationType().getQualifiedName();
 				Type type = environment.getTypeCache().get(annotationQualifiedName);
 				Annotation annotation = null;
 
 				if (type.getKind() == TypeKind.UNKNOWN) {
 					DummyAnnotation dummyAnnotation = environment.createDummyAnnotation(annotationQualifiedName);
 
 					type.promote(dummyAnnotation);
 
 					annotation = dummyAnnotation;
 				} else if (type.getKind() == TypeKind.ANNOTATION) {
 					annotation = (Annotation) type;
 				} else {
 					try {
 						throw new ParserException("Should never happen...");
 					} catch (ParserException e) {
 						throw new Error(e);
 					}
 				}
 
 				method.getAnnotations().add(annotation);
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
 
 		ASTEnvironment environment = clazz.getPackage().getParent();
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
 		
 		findMethodSelection(environment);
 	}
 
 	private void findMethodSelection(ASTEnvironment environment) {
 		//ConsoleVisitor.showNodes((ASTNode) clazz.getASTObject());
 		ASTSelection selection = environment.getSelection();
 
 		if (clazz != null && clazz.getMethodList().size() > 0) {
 			for (Map.Entry<String, Method> item : clazz.getMethodList().entrySet()) {
 				if (item.getValue() instanceof ASTMethod && selection.isOverNode(((ASTMethod) item.getValue()).getASTObject())) {
 					selection.setSelectedElement(item.getValue());
 				}
 			}
 		}
 	}
 }
