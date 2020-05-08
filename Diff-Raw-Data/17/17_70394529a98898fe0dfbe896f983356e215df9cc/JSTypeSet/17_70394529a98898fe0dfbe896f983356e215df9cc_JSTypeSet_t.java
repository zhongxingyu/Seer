 /*******************************************************************************
  * Copyright (c) 2011 NumberFour AG
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     NumberFour AG - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.typeinfo;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.javascript.typeinfo.model.ArrayType;
 import org.eclipse.dltk.javascript.typeinfo.model.JSType;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeKind;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeRef;
 import org.eclipse.dltk.javascript.typeinfo.model.impl.ArrayTypeImpl;
 import org.eclipse.dltk.javascript.typeinfo.model.impl.TypeRefImpl;
 import org.eclipse.emf.ecore.EObject;
 
 public abstract class JSTypeSet implements Iterable<JSType> {
 
 	private static class EmptyIterator implements Iterator<JSType> {
 
 		protected EmptyIterator() {
 		}
 
 		public boolean hasNext() {
 			return false;
 		}
 
 		public JSType next() {
 			throw new NoSuchElementException();
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	private static class JSEmptyTypeSet extends JSTypeSet {
 
 		protected JSEmptyTypeSet() {
 		}
 
 		public Iterator<JSType> iterator() {
 			return new EmptyIterator();
 		}
 
 		@Override
 		public void add(JSType type) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public JSType getFirst() {
 			return null;
 		}
 
 		@Override
 		public Type[] toArray() {
 			return new Type[0];
 		}
 
 		@Override
 		public int size() {
 			return 0;
 		}
 
 		@Override
 		public void addAll(JSTypeSet types) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public boolean isEmpty() {
 			return true;
 		}
 
 		@Override
 		public void clear() {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public boolean contains(JSType type) {
 			return false;
 		}
 
 		@Override
 		public boolean contains(Type type) {
 			return false;
 		}
 
 		@Override
 		public boolean containsAll(JSTypeSet types) {
 			return types.isEmpty();
 		}
 
 		@Override
 		public String toString() {
 			return getClass().getSimpleName();
 		}
 
 	}
 
 	private static final JSEmptyTypeSet EMPTY_SET = new JSEmptyTypeSet();
 
 	public static JSTypeSet emptySet() {
 		return EMPTY_SET;
 	}
 
 	private static class SingletonIterator implements Iterator<JSType> {
 		private final JSType type;
 		private boolean hasNext = true;
 
 		public SingletonIterator(JSType type) {
 			this.type = type;
 		}
 
 		public boolean hasNext() {
 			return hasNext;
 		}
 
 		public JSType next() {
 			if (hasNext) {
 				hasNext = false;
 				return type;
 			}
 			throw new NoSuchElementException();
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 
 	}
 
 	private static class JSSingletonTypeSet extends JSTypeSet {
 
 		private final JSType type;
 
 		public JSSingletonTypeSet(JSType type) {
 			this.type = normalize(type);
 		}
 
 		public Iterator<JSType> iterator() {
 			return new SingletonIterator(type);
 		}
 
 		@Override
 		public void add(JSType type) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public JSType getFirst() {
 			return type;
 		}
 
 		@Override
 		public Type[] toArray() {
 			if (type instanceof TypeRef) {
 				return new Type[] { ((TypeRef) type).getTarget() };
 			} else {
 				return new Type[0];
 			}
 		}
 
 		@Override
 		public int size() {
 			return 1;
 		}
 
 		@Override
 		public void addAll(JSTypeSet types) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public boolean isEmpty() {
 			return false;
 		}
 
 		@Override
 		public void clear() {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public boolean contains(JSType type) {
 			return this.type.equals(normalize(type));
 		}
 
 		@Override
 		public boolean contains(Type type) {
 			return this.type instanceof TypeRef
 					&& ((TypeRef) this.type).getTarget().equals(type);
 		}
 
 		@Override
 		public boolean containsAll(JSTypeSet types) {
 			return types.size() == 1 && contains(types.getFirst());
 		}
 
 		@Override
 		public String toString() {
 			return "[" + type + "]";
 		}
 
 	}
 
 	public static JSTypeSet singleton(JSType type) {
 		return new JSSingletonTypeSet(type);
 	}
 
 	private static class TypeRefKey implements TypeRef, JSType2 {
 
 		private final Type type;
 		private final boolean isStatic;
 
 		public TypeRefKey(Type type, boolean isStatic) {
 			this.type = type;
 			this.isStatic = isStatic;
 		}
 
 		public TypeKind getKind() {
 			return !type.isProxy() ? type.getKind() : TypeKind.UNRESOLVED;
 		}
 
 		public String getName() {
 			return type.getName();
 		}
 
 		public Type getTarget() {
 			return type;
 		}
 
 		public void setTarget(Type value) {
 			throw new UnsupportedOperationException();
 		}
 
 		public boolean isStatic() {
 			return isStatic;
 		}
 
 		public void setStatic(boolean value) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public int hashCode() {
 			return type.hashCode();
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (obj instanceof TypeRefKey) {
 				final TypeRefKey other = (TypeRefKey) obj;
 				return type.equals(other.type) && isStatic == other.isStatic;
 			}
 			return false;
 		}
 
 		@Override
 		public String toString() {
 			return (isStatic ? "[static]" : "") + type.toString();
 		}
 
 		public boolean isArray() {
 			return ITypeNames.ARRAY.equals(type.getName());
 		}
 
 		public boolean isAssignableFrom(JSType2 type) {
 			if (equals(type)) {
 				return true;
 			} else if (isArray()) {
 				return type.isArray();
 			} else if (type instanceof TypeRef) {
 				final Type other = ((TypeRef) type).getTarget();
				if (ITypeNames.UNDEFINED.equals(other.getName())) {
 					return true;
 				} else {
					final String localName = TypeUtil.getName(this.type);
					Type t = other;
 					while (t != null) {
						if (localName.equals(TypeUtil.getName(t)))
 							return true;
 						t = t.getSuperType();
 					}
 				}
 			}
 			return false;
 		}
 	}
 
 	private static class ArrayTypeKey implements ArrayType, JSType2 {
 
 		private final JSType2 itemType;
 
 		public ArrayTypeKey(JSType2 itemType) {
 			this.itemType = itemType;
 		}
 
 		public TypeKind getKind() {
 			return TypeKind.PREDEFINED;
 		}
 
 		public String getName() {
 			return itemType != null ? ITypeNames.ARRAY + '<'
 					+ itemType.getName() + '>' : ITypeNames.ARRAY;
 		}
 
 		public JSType getItemType() {
 			return itemType;
 		}
 
 		public void setItemType(JSType value) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public int hashCode() {
 			return itemType.hashCode();
 		}
 
 		public boolean equals(Object obj) {
 			if (obj instanceof ArrayTypeKey) {
 				final ArrayTypeKey other = (ArrayTypeKey) obj;
 				return itemType.equals(other.itemType);
 			}
 			return false;
 		}
 
 		@Override
 		public String toString() {
 			return getName();
 		}
 
 		public boolean isArray() {
 			return true;
 		}
 
 		public boolean isAssignableFrom(JSType2 type) {
 			if (type instanceof ArrayTypeKey) {
 				return itemType
 						.isAssignableFrom(((ArrayTypeKey) type).itemType);
 			}
 			return false;
 		}
 
 	}
 
 	public static JSType2 normalize(JSType type) {
 		if (type instanceof TypeRefImpl) {
 			final TypeRef ref = (TypeRef) type;
 			return ref(ref.getTarget(), ref.isStatic());
 		} else if (type instanceof ArrayTypeImpl) {
 			return arrayOf(normalize(((ArrayType) type).getItemType()));
 		}
 		Assert.isLegal(!(type instanceof EObject));
 		return (JSType2) type;
 	}
 
 	public static JSType2 ref(Type type, boolean isStatic) {
 		return new TypeRefKey(type, isStatic);
 	}
 
 	public static ArrayTypeKey arrayOf(final JSType2 itemType) {
 		return new ArrayTypeKey(itemType);
 	}
 
 	private static class JSTypeSetImpl extends JSTypeSet {
 
 		private final Set<JSType> types = new HashSet<JSType>();
 
 		protected JSTypeSetImpl() {
 		}
 
 		public Iterator<JSType> iterator() {
 			return types.iterator();
 		}
 
 		@Override
 		public void add(JSType type) {
 			types.add(normalize(type));
 		}
 
 		@Override
 		public JSType getFirst() {
 			return types.iterator().next();
 		}
 
 		@Override
 		public Type[] toArray() {
 			final List<Type> result = new ArrayList<Type>();
 			for (JSType type : types) {
 				if (type instanceof TypeRef) {
 					result.add(((TypeRef) type).getTarget());
 				}
 			}
 			return result.toArray(new Type[result.size()]);
 		}
 
 		@Override
 		public int size() {
 			return types.size();
 		}
 
 		@Override
 		public void addAll(JSTypeSet types) {
 			for (JSType type : types) {
 				add(type);
 			}
 		}
 
 		@Override
 		public boolean isEmpty() {
 			return types.isEmpty();
 		}
 
 		@Override
 		public void clear() {
 			types.clear();
 		}
 
 		@Override
 		public boolean contains(JSType type) {
 			return types.contains(normalize(type));
 		}
 
 		@Override
 		public boolean contains(Type type) {
 			return types.contains(ref(type, false));
 		}
 
 		@Override
 		public boolean containsAll(JSTypeSet types) {
 			for (JSType type : types) {
 				if (!contains(type)) {
 					return false;
 				}
 			}
 			return true;
 		}
 
 		@Override
 		public String toString() {
 			return types.toString();
 		}
 
 	}
 
 	public static JSTypeSet create() {
 		return new JSTypeSetImpl();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof JSTypeSet) {
 			final JSTypeSet other = (JSTypeSet) obj;
 			return size() == other.size() && containsAll(other);
 		}
 		return false;
 	}
 
 	public abstract void add(JSType type);
 
 	// public abstract void add(Type type);
 
 	public abstract JSType getFirst();
 
 	public abstract Type[] toArray();
 
 	public abstract int size();
 
 	public abstract void addAll(JSTypeSet types);
 
 	public abstract boolean isEmpty();
 
 	public abstract void clear();
 
 	public abstract boolean contains(JSType type);
 
 	public abstract boolean contains(Type type);
 
 	public abstract boolean containsAll(JSTypeSet types);
 
 }
