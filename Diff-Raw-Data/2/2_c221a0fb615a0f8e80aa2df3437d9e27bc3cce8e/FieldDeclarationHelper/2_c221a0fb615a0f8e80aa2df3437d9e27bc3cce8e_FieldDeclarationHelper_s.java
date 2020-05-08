 package ast.tools.helper;
 
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.ParameterizedType;
 import org.eclipse.jdt.core.dom.PrimitiveType;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 
 import ast.tools.model.TModifier;
 
 public class FieldDeclarationHelper {
 	private AST ast;
 	private FieldDeclaration declaration;
 	private String name;
 	private Set<TModifier> modifiers;
 	private VariableDeclarationFragment fragment;
 
 	public FieldDeclarationHelper(AST ast, String name, Set<TModifier> modifiers) {
 		super();
 		this.ast = ast;
 		this.name = name;
 		this.modifiers = modifiers;
 		this.init();
 	}
 
 	private void init() {
 		this.fragment = ast.newVariableDeclarationFragment();
 		this.fragment.setName(ast.newSimpleName(this.name));
 		this.declaration = ast.newFieldDeclaration(fragment);
 		setModifiers();
 	}
 
 	public void setInitializer(Expression expression) {
 		this.fragment.setInitializer(expression);
 	}
 
 	public void setType(PrimitiveType.Code type) {
 		this.declaration.setType(ast.newPrimitiveType(type));
 	}
 
	public void setReturnType(String type) {
 		this.declaration.setType(ast.newSimpleType(ast.newSimpleName(type)));
 	}
 
 	@SuppressWarnings("unchecked")
 	public void setType(String genericType, String... types) {
 		ParameterizedType parameterizedType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName(genericType)));
 		for (String type : types) {
 			parameterizedType.typeArguments().add(ast.newSimpleType(ast.newSimpleName(type)));
 		}
 		this.declaration.setType(parameterizedType);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void setModifiers() {
 		Iterator<TModifier> iterator = this.modifiers.iterator();
 		while (iterator.hasNext()) {
 			TModifier modifier = iterator.next();
 			this.declaration.modifiers().addAll(ast.newModifiers(modifier.getModifierType()));
 		}
 	}
 
 	public FieldDeclaration getFieldDeclaration() {
 		return declaration;
 	}
 
 }
