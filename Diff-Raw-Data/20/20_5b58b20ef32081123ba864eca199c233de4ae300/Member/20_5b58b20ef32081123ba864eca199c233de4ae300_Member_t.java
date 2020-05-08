 package org.eclipse.dltk.internal.core;
 
 import java.util.ArrayList;
 
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.internal.core.util.MementoTokenizer;
 
 public abstract class Member extends SourceRefElement implements IMember {
 
 	protected Member(ModelElement parent) throws IllegalArgumentException {
 		super(parent);
 	}
 
 	protected void closing(Object info) throws ModelException {
 	}
 
 	/**
 	 * @see IMember
 	 */
 	public ISourceRange getNameRange() throws ModelException {
		Object elementInfo = getElementInfo();
		if (elementInfo instanceof MemberElementInfo) {
			MemberElementInfo info = (MemberElementInfo) elementInfo;
			return new SourceRange(info.getNameSourceStart(), info
					.getNameSourceEnd()
					- info.getNameSourceStart() + 1);
		} else {
			return null;
		}
 	}
 
 	public int getFlags() throws ModelException {
 		Object o = getElementInfo();
 		if (o instanceof MemberElementInfo) {
 			MemberElementInfo info = (MemberElementInfo) o;
 			return info.getModifiers();
 		} else {
 			return 0;
 		}
 	}
 
 	public IModelElement getHandleFromMemento(String token,
 			MementoTokenizer memento, WorkingCopyOwner workingCopyOwner) {
 		switch (token.charAt(0)) {
 		case JEM_COUNT:
 			return getHandleUpdatingCountFromMemento(memento, workingCopyOwner);
 		case JEM_TYPE:
 			String typeName;
 			if (memento.hasMoreTokens()) {
 				typeName = memento.nextToken();
 				char firstChar = typeName.charAt(0);
 				if (firstChar == JEM_FIELD || firstChar == JEM_METHOD
 						|| firstChar == JEM_TYPE || firstChar == JEM_COUNT) {
 					token = typeName;
 					typeName = ""; //$NON-NLS-1$
 				} else {
 					token = null;
 				}
 			} else {
 				typeName = ""; //$NON-NLS-1$
 				token = null;
 			}
 			ModelElement type = (ModelElement) getType(typeName, 1);
 			if (token == null) {
 				return type.getHandleFromMemento(memento, workingCopyOwner);
 			} else {
 				return type.getHandleFromMemento(token, memento,
 						workingCopyOwner);
 			}
 			// case JEM_LOCALVARIABLE:
 			// if (!memento.hasMoreTokens()) return this;
 			// String varName = memento.nextToken();
 			// if (!memento.hasMoreTokens()) return this;
 			// memento.nextToken(); // JEM_COUNT
 			// if (!memento.hasMoreTokens()) return this;
 			// int declarationStart = Integer.parseInt(memento.nextToken());
 			// if (!memento.hasMoreTokens()) return this;
 			// memento.nextToken(); // JEM_COUNT
 			// if (!memento.hasMoreTokens()) return this;
 			// int declarationEnd = Integer.parseInt(memento.nextToken());
 			// if (!memento.hasMoreTokens()) return this;
 			// memento.nextToken(); // JEM_COUNT
 			// if (!memento.hasMoreTokens()) return this;
 			// int nameStart = Integer.parseInt(memento.nextToken());
 			// if (!memento.hasMoreTokens()) return this;
 			// memento.nextToken(); // JEM_COUNT
 			// if (!memento.hasMoreTokens()) return this;
 			// int nameEnd = Integer.parseInt(memento.nextToken());
 			// if (!memento.hasMoreTokens()) return this;
 			// memento.nextToken(); // JEM_COUNT
 			// if (!memento.hasMoreTokens()) return this;
 			// String typeSignature = memento.nextToken();
 			// return new LocalVariable(this, varName, declarationStart,
 			// declarationEnd, nameStart, nameEnd, typeSignature);
 			// case JEM_TYPE_PARAMETER:
 			// if (!memento.hasMoreTokens()) return this;
 			// String typeParameterName = memento.nextToken();
 			// ModelElement typeParameter = new TypeParameter(this,
 			// typeParameterName);
 			// return typeParameter.getHandleFromMemento(memento,
 			// workingCopyOwner);
 		}
 		return null;
 	}
 
 	public IType getType(String typeName, int count) {
 		if (false) {// isBinary()) {
 			throw new IllegalArgumentException(
 					"Not a source member " + toStringWithAncestors()); //$NON-NLS-1$
 		} else {
 			SourceType type = new SourceType(this, typeName);
 			type.occurrenceCount = count;
 			return type;
 		}
 	}
 
 	/**
 	 * @see IMember
 	 */
 	public IType getDeclaringType() {
 		ModelElement parentElement = (ModelElement) getParent();
 		if (parentElement.getElementType() == TYPE) {
 			return (IType) parentElement;
 		}
 		return null;
 	}
 
 	/*
 	 * Helper method for SourceType.findMethods and BinaryType.findMethods
 	 */
 	public static IMethod[] findMethods(IMethod method, IMethod[] methods) {
 		String elementName = method.getElementName();
 		String[] parameters;
 		try {
 			parameters = method.getParameters();
 		} catch (ModelException e) {
 			parameters = new String[0];
 			e.printStackTrace();
 		}
 		ArrayList list = new ArrayList();
 		for (int i = 0, length = methods.length; i < length; i++) {
 			IMethod existingMethod = methods[i];
 			try {
 				if (areSimilarMethods(elementName, parameters, existingMethod
 						.getElementName(), existingMethod.getParameters())) {
 					list.add(existingMethod);
 				}
 			} catch (ModelException e) {
 				e.printStackTrace();
 			}
 		}
 		int size = list.size();
 		if (size == 0) {
 			return null;
 		} else {
 			IMethod[] result = new IMethod[size];
 			list.toArray(result);
 			return result;
 		}
 	}
 
 	protected static boolean areSimilarMethods(String name1, String[] params1,
 			String name2, String[] params2) {
 
 		if (name1.equals(name2)) {
 			int params1Length = params1.length;
 			if (params1Length == params2.length) {
 				for (int i = 0; i < params1Length; i++) {
 					String simpleName1 = params1[i];
 					String simpleName2 = params2[i];
 					if (!simpleName1.equals(simpleName2)) {
 						return false;
 					}
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 	/*
 	 * Returns the outermost context defining a local element. Per construction, it can only be a
 	 * method/field/initializarer member; thus, returns null if this member is already a top-level type or member type.
 	 * e.g for X.java/X/Y/foo()/Z/bar()/T, it will return X.java/X/Y/foo()
 	 */
 	public Member getOuterMostLocalContext() {
 		IModelElement current = this;
 		Member lastLocalContext = null;
 		parentLoop: while (true) {
 			switch (current.getElementType()) {
 				case SOURCE_MODULE:
 					break parentLoop; // done recursing
 				case TYPE:
 					// cannot be a local context
 					break;
 				case FIELD:
 				case METHOD:
 					 // these elements can define local members
 					lastLocalContext = (Member) current;
 					break;
 			}		
 			current = current.getParent();
 		} 
 		return lastLocalContext;
 	}
 }
