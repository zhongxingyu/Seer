 package org.objectquery.generic;
 
 import java.util.Collection;
 
 import org.objectquery.QueryCondition;
 
 import javassist.util.proxy.ProxyObject;
 
 public class QueryConditionImpl implements QueryCondition {
 
 	private GenericObjectQuery<?> objectQuery;
 	private InternalConditionBuilder group;
 
 	protected QueryConditionImpl(InternalConditionBuilder group) {
 		this.objectQuery = (GenericObjectQuery<?>) this;
 		this.group = group;
 	}
 
 	public QueryConditionImpl(GenericObjectQuery<?> objectQuery, ConditionGroup group) {
 		this.objectQuery = objectQuery;
 		this.group = group;
 	}
 
 	public void condition(Object base, ConditionType type, Object value) {
 		if (base == null)
 			throw new ObjectQueryException("The given object as condition is null", null);
 		PathItem item = null;
 		Class<?> baseType = base.getClass();
 		if (!(base instanceof ProxyObject)) {
 			if ((item = objectQuery.unproxable.get(base)) == null)
 				throw new ObjectQueryException("The given object as condition isn't a proxy, use target() method for start to take object for query", null);
 		} else {
 			item = (PathItem) ((ProxyObject) base).getHandler();
 			baseType = base.getClass().getSuperclass();
 		}
 		if (type == null)
 			throw new ObjectQueryException("The given type of condition is null", null);
 
 		if (value != null) {
 			if (ConditionType.IN.equals(type) || ConditionType.NOT_IN.equals(type)) {
 				if (!(value.getClass().isArray() || value instanceof Collection))
 					throw new ObjectQueryException("The given object value is not an array or collection required for in value", null);
 			} else if (ConditionType.CONTAINS.equals(type) || ConditionType.NOT_CONTAINS.equals(type)) {
 				if (!(base.getClass().isArray() || base instanceof Collection))
 					throw new ObjectQueryException("The given object value is not an array or collection required for in value", null);
 			} else if (!baseType.isAssignableFrom(value.getClass()))
 				throw new ObjectQueryException("The given object value is not assignabled to gived condition", null);
 		}
 
 		Object curValue = null;
 
		if (value instanceof ProxyObject)
 			curValue = ((ProxyObject) value).getHandler();
 		else if ((curValue = objectQuery.unproxable.get(value)) == null)
 			curValue = value;
 
 		group.condition(item, type, curValue);
 
 	}
 
 	public QueryCondition and() {
 		return new QueryConditionImpl(objectQuery, group.newGroup(GroupType.AND));
 	}
 
 	public QueryCondition or() {
 		return new QueryConditionImpl(objectQuery, group.newGroup(GroupType.OR));
 	}
 
 	public <C, T extends C> void eq(C target, T value) {
 		condition(target, ConditionType.EQUALS, value);
 	}
 
 	public <C, T extends C> void notEq(C target, T value) {
 		condition(target, ConditionType.NOT_EQUALS, value);
 	}
 
 	public <C, T extends C> void max(C target, T value) {
 		condition(target, ConditionType.MAX, value);
 	}
 
 	public <C, T extends C> void maxEq(C target, T value) {
 		condition(target, ConditionType.MAX_EQUALS, value);
 	}
 
 	public <C, T extends C> void min(C target, T value) {
 		condition(target, ConditionType.MIN, value);
 	}
 
 	public <C, T extends C> void minEq(C target, T value) {
 		condition(target, ConditionType.MIN_EQUALS, value);
 
 	}
 
 	public <C, T extends C> void like(C target, T value) {
 		condition(target, ConditionType.LIKE, value);
 	}
 
 	public <C, T extends C> void notLike(C target, T value) {
 		condition(target, ConditionType.NOT_LIKE, value);
 	}
 
 	public <C, T extends Collection<? extends C>> void in(C target, T value) {
 		condition(target, ConditionType.IN, value);
 
 	}
 
 	public <C, T extends Collection<? extends C>> void notIn(C target, T value) {
 		condition(target, ConditionType.NOT_IN, value);
 	}
 
 	public <C, T extends C> void contains(Collection<C> target, T value) {
 		condition(target, ConditionType.CONTAINS, value);
 
 	}
 
 	public <C, T extends C> void notContains(Collection<C> target, T value) {
 		condition(target, ConditionType.NOT_CONTAINS, value);
 	}
 
 	public <C, T extends C> void likeNc(C target, T value) {
 		condition(target, ConditionType.LIKE_NOCASE, value);
 
 	}
 
 	public <C, T extends C> void notLikeNc(C target, T value) {
 		condition(target, ConditionType.NOT_LIKE_NOCASE, value);
 	}
 
 }
