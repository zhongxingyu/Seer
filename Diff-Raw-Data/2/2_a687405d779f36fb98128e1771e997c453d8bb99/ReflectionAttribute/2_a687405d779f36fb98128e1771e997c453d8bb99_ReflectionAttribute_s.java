 /*
  * Copyright 2008 Ricardo Pescuma Domenecci
  * 
  * This file is part of jfg.
  * 
  * jfg is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  * 
  * jfg is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License along with Foobar. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package jfg.reflect;
 
 import static jfg.reflect.ReflectionUtils.*;
 
 import java.lang.reflect.AnnotatedElement;
 import java.lang.reflect.Field;
 import java.lang.reflect.Member;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import jfg.Attribute;
 import jfg.AttributeGroup;
 import jfg.AttributeListener;
 import jfg.AttributeValueRange;
 import jfg.model.ann.CompareWith;
 import jfg.model.ann.NotNull;
 import jfg.model.ann.Range;
 
 public class ReflectionAttribute implements Attribute
 {
 	private final AttributeGroup parent;
 	private final ReflectionData data;
 	private final Object obj;
 	private final Field field;
 	private final boolean useStatic;
 	private final Method setter;
 	private final Method getter;
 	private final Method addListener;
 	private final Method removeListener;
 	private final String name;
 	private final Class<?> type;
 	private final AttributeValueRange attributeValueRange;
 	
 	private Object listener;
 	private final List<AttributeListener> listeners = new ArrayList<AttributeListener>();
 	
 	private final MemberFilter memberFilter = new MemberFilter() {
 		public boolean accept(Member member)
 		{
 			if (useStatic != Modifier.isStatic(member.getModifiers()))
 				return false;
 			
 			return data.memberFilter.accept(member);
 		}
 	};
 	
 	public ReflectionAttribute(Object obj, Field field)
 	{
 		this(obj, field, new ReflectionData());
 	}
 	
 	public ReflectionAttribute(Object obj, Field field, ReflectionData data)
 	{
 		this(new ReflectionGroup(obj, data), obj, field, data);
 	}
 	
 	ReflectionAttribute(AttributeGroup parent, Object obj, Field field, ReflectionData data)
 	{
 		this(parent, obj, field.getName(), data);
 	}
 	
 	public ReflectionAttribute(Object obj, String fieldName)
 	{
 		this(obj, fieldName, new ReflectionData());
 	}
 	
 	public ReflectionAttribute(Object obj, String fieldName, ReflectionData data)
 	{
 		this(new ReflectionGroup(obj, data), obj, fieldName, data);
 	}
 	
 	ReflectionAttribute(AttributeGroup parent, Object obj, String simpleName, ReflectionData data)
 	{
 		this.parent = parent;
 		this.obj = obj;
 		this.data = data;
 		useStatic = (obj instanceof Class<?>);
 		
 		field = getField(memberFilter, getObjClass(), simpleName);
 		getter = getMethod(memberFilter, getObjClass(), data.getGetterNames(simpleName));
 		
 		assertValid();
 		
 		type = (field != null ? field.getType() : getter.getReturnType());
 		
 		name = createFullName(simpleName);
 		
 		setter = getMethod(memberFilter, getObjClass(), void.class, data.getSetterNames(simpleName), type);
 		
 		addListener = getListenerMethod(memberFilter, getObjClass(), data.getAddFieldListenerNames(simpleName),
 				data.getRemoveFieldListenerNames(simpleName), data);
 		if (addListener != null)
 			removeListener = getMethod(memberFilter, getObjClass(), data.getRemoveFieldListenerNames(simpleName),
 					addListener.getParameterTypes());
 		else
 			removeListener = null;
 		
 		attributeValueRange = getRangeData(simpleName);
 		
 		setAccessible();
 	}
 	
 	private String createFullName(String simpleName)
 	{
 		if (field != null)
 			return field.getDeclaringClass().getName() + "." + simpleName;
 		if (getter != null)
 			return getter.getDeclaringClass().getName() + "." + simpleName;
 		throw new IllegalStateException();
 	}
 	
 	private Class<?> getObjClass()
 	{
 		if (useStatic)
 			return (Class<?>) obj;
 		else
 			return obj.getClass();
 	}
 	
 	private Object getObjInstance()
 	{
 		if (useStatic)
 			return null;
 		else
 			return obj;
 	}
 	
 	private static class RangeData
 	{
 		long min = Long.MIN_VALUE;
 		double minf = Double.NEGATIVE_INFINITY;
 		long max = Long.MAX_VALUE;
 		double maxf = Double.POSITIVE_INFINITY;
 		boolean canBeNull = true;
 		Class<? extends Comparator<?>> comparator = null;
 		
 		void addFrom(AnnotatedElement element)
 		{
 			if (element == null)
 				return;
 			
 			if (element.getAnnotation(NotNull.class) != null)
 				canBeNull = false;
 			
 			Range r = element.getAnnotation(Range.class);
 			if (r != null)
 			{
 				min = Math.max(min, r.min());
 				minf = Math.max(minf, r.minf());
 				max = Math.min(max, r.max());
 				maxf = Math.min(maxf, r.maxf());
 			}
 			
 			CompareWith comp = element.getAnnotation(CompareWith.class);
 			if (comp != null && comparator == null)
 				comparator = comp.value();
 		}
 	}
 	
 	private AttributeValueRange getRangeData(String simpleName)
 	{
 		final RangeData range = new RangeData();
 		
 		if (type.isPrimitive())
 			range.canBeNull = false;
 		
 		range.addFrom(getField(new MemberFilter() {
 			public boolean accept(Member member)
 			{
 				return useStatic == Modifier.isStatic(member.getModifiers());
 			}
 		}, getObjClass(), simpleName));
 		range.addFrom(getter);
 		range.addFrom(setter);
 		
 		boolean hasNotNull = !range.canBeNull;
 		boolean hasMin = (range.min > Long.MIN_VALUE || !Double.isInfinite(range.minf));
		boolean hasMax = (range.max > Long.MAX_VALUE || !Double.isInfinite(range.maxf));
 		boolean hasValues = type.isEnum();
 		boolean hasComparator = (range.comparator != null);
 		if (!hasNotNull && !hasMin && !hasMax && !hasValues && !hasComparator)
 			return null;
 		
 		final Object min;
 		if (range.min > Long.MIN_VALUE)
 			min = ReflectionUtils.valueOf(Long.valueOf(range.min), type);
 		else if (!Double.isInfinite(range.minf))
 			min = ReflectionUtils.valueOf(Double.valueOf(range.minf), type);
 		else
 			min = null;
 		
 		final Object max;
 		if (range.max < Long.MAX_VALUE)
 			max = ReflectionUtils.valueOf(Long.valueOf(range.max), type);
 		else if (!Double.isInfinite(range.maxf))
 			max = ReflectionUtils.valueOf(Double.valueOf(range.maxf), type);
 		else
 			max = null;
 		
 		final Collection<Object> values;
 		if (hasValues)
 		{
 			values = new ArrayList<Object>();
 			Collections.addAll(values, type.getEnumConstants());
 		}
 		else
 			values = null;
 		
 		return new AttributeValueRange() {
 			
 			public boolean canBeNull()
 			{
 				return range.canBeNull;
 			}
 			
 			public Comparator<?> getComparator()
 			{
 				if (range.comparator != null)
 					return newInstance(range.comparator);
 				
 				return null;
 			}
 			
 			public Object getMax()
 			{
 				return max;
 			}
 			
 			public Object getMin()
 			{
 				return min;
 			}
 			
 			public Collection<Object> getPossibleValues()
 			{
 				return values;
 			}
 		};
 	}
 	
 	private void assertValid()
 	{
 		if (field == null && (getter == null || getter.getReturnType() == void.class))
 			throw new IllegalArgumentException();
 		if (field != null && getter != null && field.getType() != getter.getReturnType())
 			throw new IllegalArgumentException();
 	}
 	
 	private void setAccessible()
 	{
 		if (field != null)
 			field.setAccessible(true);
 		if (getter != null)
 			getter.setAccessible(true);
 		if (setter != null)
 			setter.setAccessible(true);
 		if (addListener != null)
 			addListener.setAccessible(true);
 		if (removeListener != null)
 			removeListener.setAccessible(true);
 	}
 	
 	public String getName()
 	{
 		return name;
 	}
 	
 	public Object getType()
 	{
 		return type;
 	}
 	
 	public AttributeValueRange getValueRange()
 	{
 		return attributeValueRange;
 	}
 	
 	public AttributeGroup asGroup()
 	{
 		if (type.isPrimitive())
 			return null;
 		if (data.ignoreForAsGroup(type.getName()))
 			return null;
 		
 		Object value = getValue();
 		if (value == null)
 			return null;
 		
 		return new ReflectionGroup(getName(), value, data);
 	}
 	
 	public boolean canWrite()
 	{
 		return setter != null || (field != null && !Modifier.isFinal(field.getModifiers()));
 	}
 	
 	public Object getValue()
 	{
 		if (getter != null)
 		{
 			return invoke(getObjInstance(), getter);
 		}
 		else if (field != null)
 		{
 			return get(getObjInstance(), field);
 		}
 		else
 		{
 			throw new IllegalStateException();
 		}
 	}
 	
 	public void setValue(Object value)
 	{
 		if (!canWrite())
 			throw new ReflectionAttributeException("Field is ready-only");
 		
 		if (setter != null)
 		{
 			invoke(getObjInstance(), setter, value);
 		}
 		else if (field != null)
 		{
 			set(getObjInstance(), field, value);
 		}
 		else
 		{
 			throw new IllegalStateException();
 		}
 	}
 	
 	public boolean canListen()
 	{
 		return (addListener != null && removeListener != null) || (parent != null && parent.canListen());
 	}
 	
 	private void notifyChange()
 	{
 		for (AttributeListener al : listeners)
 		{
 			al.onChange();
 		}
 	}
 	
 	public void addListener(AttributeListener attributeListener)
 	{
 		if (!canListen())
 			throw new ReflectionAttributeException("Can't add listener");
 		
 		if (listener == null)
 		{
 			if (addListener != null)
 			{
 				listener = wrapListener(addListener.getParameterTypes()[0], new AttributeListener() {
 					public void onChange()
 					{
 						notifyChange();
 					}
 				}, data);
 				
 				invoke(getObjInstance(), addListener, listener);
 			}
 			else if (parent != null)
 			{
 				listener = new AttributeListener() {
 					Object oldValue = getValue();
 					
 					public void onChange()
 					{
 						Object newValue = getValue();
 						if ((oldValue == newValue) || (oldValue != null && newValue != null && oldValue.equals(newValue)))
 							return;
 						oldValue = newValue;
 						
 						notifyChange();
 					}
 				};
 				
 				parent.addListener((AttributeListener) listener);
 			}
 		}
 		
 		listeners.add(attributeListener);
 	}
 	
 	public void removeListener(AttributeListener attributeListener)
 	{
 		if (!canListen())
 			throw new ReflectionAttributeException("Can't add listener");
 		
 		listeners.remove(attributeListener);
 		
 		if (listeners.size() <= 0)
 		{
 			if (removeListener != null)
 				invoke(getObjInstance(), removeListener, listener);
 			else
 				parent.removeListener((AttributeListener) listener);
 			
 			listener = null;
 		}
 	}
 	
 	@Override
 	public String toString()
 	{
 		return "ObjectReflectionAttribute[" + name + "]@" + Integer.toHexString(hashCode());
 	}
 	
 }
