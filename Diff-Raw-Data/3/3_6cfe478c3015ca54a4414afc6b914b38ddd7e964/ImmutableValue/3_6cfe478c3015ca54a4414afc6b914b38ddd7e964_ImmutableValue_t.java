 package org.eclipse.dltk.internal.javascript.ti;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.dltk.javascript.typeinference.ReferenceKind;
 import org.eclipse.dltk.javascript.typeinference.ReferenceLocation;
 import org.eclipse.dltk.javascript.typeinfo.IRLocalType;
 import org.eclipse.dltk.javascript.typeinfo.IRType;
 import org.eclipse.dltk.javascript.typeinfo.JSTypeSet;
 import org.eclipse.dltk.javascript.typeinfo.RTypes;
 
 public class ImmutableValue implements IValue, IValue2 {
 
 	private Map<String, IValue> elementValues;
 
 	protected IRType declaredType;
 	protected final JSTypeSet types;
 	protected Set<String> deletedChildren;
 	protected ReferenceKind kind = ReferenceKind.UNKNOWN;
 	protected ReferenceLocation location = ReferenceLocation.UNKNOWN;
 
 	protected final Map<String, ImmutableValue> children;
 	protected final Map<String, IValue> inherited;
 	protected final Set<IValue> references;
 	protected Map<String, Object> attributes;
 
 	protected static interface Handler<R> {
 		void process(ImmutableValue value, R result);
 	}
 
 	protected static interface Handler2<R> extends Handler<R> {
 		void processOther(IValue value, R result);
 	}
 
 	protected ImmutableValue() {
 		super();
 		types = JSTypeSet.create();
 		children = new HashMap<String, ImmutableValue>(4, 0.9f);
 		inherited = new HashMap<String, IValue>(4, 0.9f);
 		references = new HashSet<IValue>(4, 0.9f);
 	}
 
 	public ImmutableValue(IRType declaredType, JSTypeSet types,
 			Set<String> deletedChildren, ReferenceKind kind,
 			ReferenceLocation location, Map<String, ImmutableValue> children,
 			Map<String, IValue> inherited, Set<IValue> references,
 			Map<String, Object> attributes) {
 		this.declaredType = declaredType;
 		this.types = types;
 		this.deletedChildren = deletedChildren;
 		this.kind = kind;
 		this.location = location;
 		this.children = children;
 		this.inherited = inherited;
 		this.references = references;
 		this.attributes = attributes;
 	}
 
 	protected ImmutableValue(ImmutableValue value) {
 		this.declaredType = value.declaredType;
 		this.types = value.types;
 		this.deletedChildren = value.deletedChildren;
 		this.kind = value.kind;
 		this.location = value.location;
 		this.children = value.children;
 		this.inherited = value.inherited;
 		this.references = value.references;
 		this.attributes = value.attributes;
 	}
 
 	protected final boolean hasReferences() {
 		return !references.isEmpty();
 	}
 
 	public Set<? extends IValue> getReferences() {
 		return references;
 	}
 
 	protected static <R> void execute(ImmutableValue value, Handler<R> handler,
 			R result, Set<IValue> visited) {
 		if (visited.add(value)) {
 			if (value instanceof ILazyValue)
 				((ILazyValue) value).resolve();
 			handler.process(value, result);
 			for (IValue child : value.references) {
 				if (child instanceof ImmutableValue)
 					execute((ImmutableValue) child, handler, result, visited);
 				else if (handler instanceof Handler2) {
 					((Handler2<R>) handler).processOther(child, result);
 				}
 			}
 		}
 	}
 
 	private static final Handler<JSTypeSet> GET_TYPES = new Handler2<JSTypeSet>() {
 		public void process(ImmutableValue value, JSTypeSet result) {
 			result.addAll(value.types);
 		}
 
 		public void processOther(IValue value, JSTypeSet result) {
 			result.addAll(value.getTypes());
 		};
 	};
 	private static final Handler<JSTypeSet> GET_DECLARED_TYPES = new Handler<JSTypeSet>() {
 		public void process(ImmutableValue value, JSTypeSet result) {
 			if (value.declaredType != null)
 				result.add(value.declaredType);
 		}
 	};
 
 	public JSTypeSet getTypes() {
 		if (hasReferences()) {
 			final JSTypeSet result = JSTypeSet.create();
 			execute(this, GET_TYPES, result, new HashSet<IValue>());
 			return result;
 		} else {
 			return types;
 		}
 	}
 
 	private static final Handler<Set<String>> GET_DIRECT_CHILDREN = new Handler<Set<String>>() {
 		public void process(ImmutableValue value, Set<String> result) {
 			result.addAll(value.children.keySet());
 		}
 	};
 
 	public IRType getDeclaredType() {
 		if (declaredType != null) {
 			return declaredType;
 		} else if (hasReferences()) {
 			final JSTypeSet result = JSTypeSet.create();
 			execute(this, GET_DECLARED_TYPES, result, new HashSet<IValue>());
 			return result.toRType();
 		} else {
 			return null;
 		}
 	}
 
 	public JSTypeSet getDeclaredTypes() {
 		if (declaredType != null) {
 			return JSTypeSet.singleton(declaredType);
 		} else if (hasReferences()) {
 			final JSTypeSet result = JSTypeSet.create();
 			execute(this, GET_DECLARED_TYPES, result, new HashSet<IValue>());
 			return result;
 		} else {
 			return JSTypeSet.emptySet();
 		}
 	}
 
 	public ReferenceKind getKind() {
 		return kind;
 	}
 
 	public ReferenceLocation getLocation() {
 		return location;
 	}
 
 	public final Object getAttribute(String key) {
 		return getAttribute(key, false);
 	}
 
 	public Object getAttribute(String key, boolean includeReferences) {
 		if (IReferenceAttributes.PHANTOM.equals(key)
 				&& declaredType == RTypes.any()) {
 			return Boolean.TRUE;
 		}
 		Object attribute = null;
 		if (attributes != null) {
 			attribute = attributes.get(key);
 		}
 		if (includeReferences && attribute == null && !references.isEmpty()) {
 			attribute = visitReferenceForAttribute(key,
 					new HashSet<ImmutableValue>());
 		}
 		return attribute;
 	}
 
 	/**
 	 * @param key
 	 * @param attribute
 	 * @param visited
 	 * @return
 	 */
 	private Object visitReferenceForAttribute(String key,
 			Set<ImmutableValue> visited) {
 		if (visited.add(this)) {
 			for (IValue reference : references) {
 				Object attribute = reference.getAttribute(key, false);
 				if (attribute != null)
 					return attribute;
 				if (reference instanceof ImmutableValue) {
 					attribute = ((ImmutableValue) reference)
 							.visitReferenceForAttribute(key, visited);
 					if (attribute != null)
 						return attribute;
 				}
 			}
 		}
 		return null;
 	}
 
 	protected static class GetChildHandler implements Handler2<Set<IValue>> {
 
 		private final String childName;
 
 		public GetChildHandler(String childName) {
 			this.childName = childName;
 		}
 
 		public void process(ImmutableValue value, Set<IValue> result) {
 			ImmutableValue child = value.children.get(childName);
 			if (child != null) {
 				result.add(child);
 			} else {
 				IValue member = ElementValue.findMemberA(value.declaredType,
 						childName, true);
 				if (member != null) {
 					result.add(member);
 				}
 				final JSTypeSet valueTypes;
 				if (value.hasReferences()) {
 					valueTypes = value.types;
 				} else {
 					valueTypes = value.getTypes();
 				}
 				for (IRType type : valueTypes) {
 					member = ElementValue.findMemberA(type, childName, true);
 					if (member != null) {
 						result.add(member);
 					}
 				}
 			}
 		}
 
 		public void processOther(IValue value, Set<IValue> result) {
 			if (value == PhantomValue.VALUE) {
 				result.add(value);
 			} else {
 				IValue childValue = value.getChild(childName, true);
 				if (childValue != null)
 					result.add(childValue);
 			}
 		}
 	}
 
 	public Set<String> getDirectChildren(int flags) {
 		final Set<String> result = new HashSet<String>();
 		if (hasReferences()) {
 			execute(this, GET_DIRECT_CHILDREN, result, new HashSet<IValue>());
 		} else {
 			result.addAll(children.keySet());
 		}
 		if ((flags & NO_LOCAL_TYPES) == 0) {
 			if (getDeclaredType() instanceof IRLocalType) {
 				result.addAll(((IRLocalType) getDeclaredType())
 						.getDirectChildren());
 			}
 			for (IRType irType : getTypes()) {
 				if (irType instanceof IRLocalType) {
 					result.addAll(((IRLocalType) irType).getDirectChildren());
 				}
 			}
 		}
 		return result;
 	}
 
 	public Set<String> getDeletedChildren() {
 		if (deletedChildren != null) {
 			return deletedChildren;
 		} else {
 			return Collections.emptySet();
 		}
 	}
 
 	protected IValue findMember(String name, boolean resolve) {
 		if (declaredType == RTypes.any()) {
 			return PhantomValue.VALUE;
 		}
 		IValue value = null;
 		if (elementValues != null)
 			value = elementValues.get(name);
 		if (value == null && (declaredType != null || !types.isEmpty())) {
 			if (declaredType != null) {
 				value = ElementValue.findMemberA(declaredType, name, resolve);
 				if (value != null) {
 					if (elementValues == null)
 						elementValues = new HashMap<String, IValue>(4, 0.9f);
 					elementValues.put(name, value);
 					return value;
 				}
 			}
 			for (IRType type : types) {
 				value = ElementValue.findMemberA(type, name, resolve);
 				if (value != null) {
 					if (elementValues == null)
 						elementValues = new HashMap<String, IValue>(4, 0.9f);
 					if (resolve && value instanceof ElementValue) {
 						value = ((ElementValue) value).resolveValue();
 					}
 					elementValues.put(name, value);
 					return value;
 				}
 			}
 		}
 		return value;
 	}
 
 	public IValue getChild(String name, boolean resolve) {
 		// first always try the value itself.
 		// if found that this will always be the child to return
 		IValue child = children.get(name);
 		if (child == null) {
 			child = inherited.get(name);
 			if (child == null) {
 				child = findMember(name, resolve);
 			}
 		}
 		// if it didn't find a child in it self and it has references.
 		// search of them.
 		if (child == null && hasReferences()) {
 			Set<IValue> result = new HashSet<IValue>();
 			execute(this, new GetChildHandler(name), result,
 					new HashSet<IValue>());
 			if (!result.isEmpty()) {
 				if (result.size() > 1) {
 					// try to return the best match? (or should we combine
 					// them??)
 					Iterator<IValue> iterator = result.iterator();
 					IValue first = iterator.next();
 					while (iterator.hasNext()) {
 						IValue next = iterator.next();
 						if (next.getDeclaredTypes().size() > first
 								.getDeclaredTypes().size()) {
 							first = next;
 							continue;
 						}
						if (next.getTypes().size() > first.getTypes().size()
								&& first.getDeclaredTypes().size() == 0) {
 							first = next;
 						}
 					}
 					return first;
 				}
 				return result.iterator().next();
 			} else {
 				return findMember(name, resolve);
 			}
 		}
 		return child;
 	}
 
 	public boolean hasChild(String name) {
 		return children.containsKey(name) || inherited.containsKey(name);
 	}
 
 	public IValue createChild(String name, int flags) {
 		return null;
 	}
 
 	protected void childCreated(String name) {
 		if (elementValues != null) {
 			elementValues.remove(name);
 		}
 		if (deletedChildren != null) {
 			deletedChildren.remove(name);
 		}
 	}
 
 	public void setDeclaredType(IRType declaredType) {
 	}
 
 	public void addType(IRType type) {
 	}
 
 	public void setAttribute(String key, Object value) {
 	}
 
 	public void setKind(ReferenceKind kind) {
 	}
 
 	public void setLocation(ReferenceLocation location) {
 	}
 
 	public void addValue(IValue src) {
 	}
 
 	public void addReference(IValue src) {
 	}
 
 	public void removeReference(IValue value) {
 	}
 
 	public void clear() {
 	}
 
 	public void putChild(String name, IValue value) {
 	}
 
 	public void deleteChild(String name, boolean force) {
 	}
 
 }
