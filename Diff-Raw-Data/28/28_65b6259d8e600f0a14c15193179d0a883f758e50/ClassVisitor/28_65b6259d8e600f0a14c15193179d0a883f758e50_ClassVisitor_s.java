 package org.eclipselabs.recommenders.codesearchquery.indexer;
 
 import java.util.Set;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.IMethodBinding;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.ImportDeclaration;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.PackageDeclaration;
 import org.eclipse.jdt.core.dom.Type;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipselabs.recommenders.codesearchquery.indexer.lucene.LuceneIndex;
 import org.eclipse.recommenders.utils.names.IMethodName;
 import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.rcp.utils.ast.BindingUtils;
 
 import com.google.common.collect.Sets;
 
 public class ClassVisitor extends IndexingASTVisitor {
 
 	private Set<ITypeName> extendedTypes = Sets.newHashSet();
 	private Set<ITypeName> implementedTypes = Sets.newHashSet();
 	private Set<IMethodName> methods = Sets.newHashSet();
 	private Set<IMethodName> overriddenMethods = Sets.newHashSet();
 	private Set<ITypeName> fieldTypes = Sets.newHashSet();
 
 	public ClassVisitor(LuceneIndex index, TypeDeclaration type) {
 		super(index);
 
 		ITypeBinding typeBinding = type.resolveBinding();
 		final ITypeName className = BindingUtils.toTypeName(typeBinding);
 
 		setName(className.getIdentifier());
 
 		setSuperclassNames(type);
 		setInterfaceNames(type);
 	}
 
 	@Override
 	public boolean visit(final ImportDeclaration node) {
 		return false;
 	}
 
 	@Override
 	public boolean visit(final PackageDeclaration node) {
 		return false;
 	}
 
 	// @Override
 	// public boolean visit(TypeDeclaration node) {
 	//
 	// ITypeBinding typeBinding = node.resolveBinding();
 	//
 	// final ITypeName className = BindingUtils.toTypeName(typeBinding);
 	//
 	// System.out.println("Type name: " + node.getName() + " "
 	// + className.getIdentifier());
 	//
 	// setName(className.getIdentifier());
 	//
 	// setSuperclassNames(node);
 	// setInterfaceNames(node);
 	//
 	// return true;
 	// }
 
 	private void setSuperclassNames(final TypeDeclaration node) {
 		final ITypeBinding clazz = node.resolveBinding();
 		ITypeBinding superclass = clazz.getSuperclass();
 		// add superclass, if not null and not Object
 		for (; superclass != null; superclass = superclass.getSuperclass()) {
 			final ITypeName superclassName = BindingUtils
 					.toTypeName(superclass);
 			if (!isPrimitiveOrArrayOrNullOrObjectOrString(superclassName)) {
 				extendedTypes.add(superclassName);
 			}
 		}
 	}
 
 	private void setInterfaceNames(final TypeDeclaration node) {
 		for (final ITypeBinding interface_ : node.resolveBinding()
 				.getInterfaces()) {
 			final ITypeName interfaceName = BindingUtils.toTypeName(interface_);
 			if (!isPrimitiveOrArrayOrNullOrObjectOrString(interfaceName)) {
 				implementedTypes.add(interfaceName);
 			}
 		}
 	}
 
 	@Override
 	public boolean visit(MethodDeclaration node) {
 		IMethodBinding b = node.resolveBinding();
 		final IMethodName method = BindingUtils.toMethodName(b);
 
 		methods.add(method);
		setOverriddenIMethodName(b);
 
 		System.out.println("Method name: " + node.getName() + " Return type: "
 				+ node.getReturnType2());
 
 		MethodVisitor v = new MethodVisitor(getIndex(), node);
 		v.index(node);
 
 		return false;
 	}
 
	private void setOverriddenIMethodName(final IMethodBinding b) {
		final IMethodBinding overriddenBinding = Bindings.findOverriddenMethod(
				b, true);
		final IMethodName overriddenIMethodName = BindingUtils
				.toMethodName(overriddenBinding);
		if (overriddenIMethodName != null) {
			overriddenMethods.add(overriddenIMethodName);
		}
	}
 
 	@Override
 	public boolean visit(final FieldDeclaration node) {
 		final Type fieldType = node.getType();
 		addField(fieldType);
 		return true;
 	}
 
 	private void addField(final Type fieldType) {
 		final ITypeBinding fieldTypeBinding = fieldType.resolveBinding();
 		final ITypeName ITypeName = BindingUtils.toTypeName(fieldTypeBinding);
 		if (!isPrimitiveOrArrayOrNullOrObjectOrString(ITypeName)) {
 			fieldTypes.add(ITypeName);
 		}
 	}
 
 	@Override
 	public String getDocumentType() {
 		return "class";
 	}
 
 	@Override
 	protected void populateDocument(Document d) {
 		d.add(new Field("extended types", joiner.join(getNames(extendedTypes)),
 				Field.Store.YES, Field.Index.ANALYZED));
 
 		d.add(new Field("implemented types", joiner
 				.join(getNames(implementedTypes)), Field.Store.YES,
 				Field.Index.ANALYZED));
 
 		d.add(new Field("methods", joiner.join(getNames(methods)),
 				Field.Store.YES, Field.Index.ANALYZED));
 	}
 }
