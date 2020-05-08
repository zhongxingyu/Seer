 /*******************************************************************************
  * Copyright (c) 2012 Emanuele Tamponi.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Emanuele Tamponi - initial API and implementation
  ******************************************************************************/
 package com.ios;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import org.objenesis.instantiator.ObjectInstantiator;
 import org.objenesis.strategy.InstantiatorStrategy;
 import org.objenesis.strategy.StdInstantiatorStrategy;
 import org.pcollections.HashTreePSet;
 import org.pcollections.PSet;
 
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryo.io.Input;
 import com.esotericsoftware.kryo.io.Output;
 import com.esotericsoftware.kryo.serializers.FieldSerializer;
 import com.esotericsoftware.reflectasm.FieldAccess;
 
 public class IObject {
 
 	public static final int MAXIMUM_CHANGE_PROPAGATION = 5;
 
 	private static final Kryo kryo = new Kryo() {
 		InstantiatorStrategy s = new StdInstantiatorStrategy();
 		@Override
 		protected ObjectInstantiator newInstantiator (final Class type) {
 			if (IObject.class.isAssignableFrom(type))
 				return s.newInstantiatorOf(type);
 			else
 				return super.newInstantiator(type);
 		}
 	};
 
 	static {
 		kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
 		kryo.addDefaultSerializer(IList.class, new FieldSerializer(kryo, IList.class));
 		kryo.addDefaultSerializer(IMap.class, new FieldSerializer(kryo, IMap.class));
 	}
 	
 	public static Kryo getKryo() {
 		return kryo;
 	}
 	
 	public static <T extends IObject> T load(File inFile) {
 		try {
 			FileInputStream in = new FileInputStream(inFile);
 			T ret = (T)load(in);
 			in.close();
 			return ret;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static <T extends IObject> T load(InputStream in) {
 		Input input = new Input(in);
 		T ret = (T) kryo.readClassAndObject(input);
 		input.close();
 		return ret;
 	}
 
 	final LinkList parentsLinkToThis = new LinkList();
 
 	private final List<Trigger> triggers = new ArrayList<>();
 	
 	private final Map<Property, List<ErrorCheck>> errorChecks = new HashMap<>();
 	
 	private final Map<Property, List<Constraint>> constraints = new HashMap<>();
 
 	public String name = String.format("%s-%02d", getClass().getSimpleName(), hashCode() % 100);
 	
 	protected void addConstraint(String propertyName, Constraint constraint) {
 		Property property = new Property(this, propertyName);
 		if (!constraints.containsKey(property))
 			constraints.put(property, new ArrayList<Constraint>());
 		constraints.get(property).add(constraint);
 	}
 
 	protected void addErrorCheck(String propertyName, ErrorCheck check) {
 		Property property = new Property(this, propertyName);
 		if (!errorChecks.containsKey(property))
 			errorChecks.put(property, new ArrayList<ErrorCheck>());
 		errorChecks.get(property).add(check);
 	}
 
 	protected void addTrigger(Trigger trigger) {
 		this.triggers.add(trigger);
 	}
 
 	private Property appendChild(Property path, Property child) {
 		if (path.getPath().isEmpty())
 			return child;
 		else
 			return new Property(path.getRoot(), path.getPath() + "." + child.getPath());
 	}
 
 	private List<String> checkErrors(Property property) {
 		List<String> ret = new ArrayList<>();
 		Object content = property.getContent();
 		if (content == null) {
 			ret.add("is null");
 		} else {
 			if (errorChecks.containsKey(property)) {
 				for (ErrorCheck check: errorChecks.get(property)) {
 					String error = check.getError(content);
 					if (error != null)
 						ret.add(error);
 				}
 			}
 		}
 		
 		return ret;
 	}
 
 	public <T extends IObject> T copy() {
 		kryo.getContext().put("root", this);
 		kryo.getContext().put("descendents", new HashSet<IObject>());
 		kryo.getContext().put("nondescendents", new HashSet<IObject>());
 
 		IObject copy = kryo.copy(this);
 
 		kryo.getContext().remove("root");
 		kryo.getContext().remove("descendents");
 		kryo.getContext().remove("nondescendents");
 		return (T) copy;
 	}
 
 	public void detach() {
 		for (Property linkToThis : new ArrayList<>(parentsLinkToThis))
 			linkToThis.setContent(null);
 
 		for (Property intelligentProperty : getIntelligentProperties())
 			intelligentProperty.setContent(null);
 	}
 
 	public List<Property> getBoundProperties() {
 		List<Property> ret = new ArrayList<>();
 		recursivelyFindBoundProperties(new Property(this, ""), ret, HashTreePSet.<Property> empty());
 		return ret;
 	}
 	
 	public Set<Class> getCompatibleContentTypes(String propertyName) {
 		Property path = new Property(this, propertyName);
 		
 		List<Constraint> list = new ArrayList<>();
 		recursivelyFindConstraints(path, list, HashTreePSet.<Property> empty());
 		
 		return PluginManager.getCompatibleImplementationsOf(getContentType(propertyName, false), list);
 	}
 	
 	public <T> T getContent(String propertyPath) {
 		if (propertyPath.isEmpty())
 			return (T)this;
 
 		int firstSplit = propertyPath.indexOf('.');
 		if (firstSplit < 0) {
 			return (T)getLocal(propertyPath);
 		} else {
 			String localProperty = propertyPath.substring(0, firstSplit);
 			String remainingPath = propertyPath.substring(firstSplit + 1);
 			IObject local = (IObject) getLocal(localProperty);
 			if (local != null)
 				return local.getContent(remainingPath);
 			else
 				return null;
 		}
 	}
 	
 	public <T> T getContent(String propertyPath, Class<T> contentType) {
 		return (T) getContent(propertyPath);
 	}
 	
 	public Class<?> getContentType(String propertyName, boolean runtime) {
 		if (runtime) {
 			Object content = getLocal(propertyName);
 			return content == null ? getContentType(propertyName, false) : content.getClass();
 		} else {
 			try {
 				return getClass().getField(propertyName).getType();
 			} catch (NoSuchFieldException | SecurityException e) {
 				return null;
 			}
 		}
 	}
 	
 	public Map<Property, List<String>> getErrors() {
 		Map<Property, List<String>> ret = new LinkedHashMap<>();
 
 		recursivelyFindErrors(new Property(this, ""), ret, new HashSet<IObject>());
 		
 		return ret;
 	}
 
 	public List<String> getFieldPropertyNames() {
 		List<String> ret = new ArrayList<>();
 		
 		Stack<Class<?>> types = new Stack<>();
 		types.add(getClass());
 		while (!types.peek().equals(IObject.class))
 			types.push(types.peek().getSuperclass());
 
 		while(!types.isEmpty()) {
 			Class<?> type = types.pop();
 			for (Field field : type.getDeclaredFields()) {
 				int mod = field.getModifiers();
 				if (Modifier.isPublic(mod) && !Modifier.isStatic(mod)
 						&& !Modifier.isFinal(mod))
 					ret.add(field.getName());
 			}
 		}
 		
 		return ret;
 	}
 
 	protected List<Property> getInstanceProperties() {
 		return new ArrayList<>();
 	}
 
 	public List<Property> getIntelligentProperties() {
 		List<Property> ret = new ArrayList<>();
 		for (Property property: getProperties()) {
 			if (property.getContent() instanceof IObject)
 				ret.add(property);
 		}
 		return ret;
 	}
 
 	protected Object getLocal(String propertyName) {
 		FieldAccess fieldAccess = FieldAccess.get(getClass());
 		return fieldAccess.get(this, propertyName);
 	}
 	
 	protected List<Property> getParentsLinksToThis() {
 		return parentsLinkToThis;
 	}
 
 	public List<Property> getProperties() {
 		List<Property> ret = new ArrayList<>();
 
 		for(String name: getFieldPropertyNames())
 			ret.add(new Property(this, name));
 		
 		ret.addAll(getInstanceProperties());
 		return ret;
 	}
 
 	public List<Property> getUnboundProperties() {
 		List<Property> ret = getProperties();
 		ret.removeAll(getBoundProperties());
 		return ret;
 	}
 
 	private void innerSetLocal(String propertyName, Object content) {
 		Property property = new Property(this, propertyName);
 
 		Object oldContent = getLocal(propertyName);
 
 		if (oldContent == content)
 			return;
 
 		if (oldContent instanceof IObject) {
 			((IObject) oldContent).parentsLinkToThis.remove(property);
 		}
 
 		setLocal(propertyName, content);
 
 		if (content instanceof IObject) {
 			((IObject) content).parentsLinkToThis.add(property);
 		}
 
 		propagateChange(property);
 	}
 
 	private void checkTriggers(Property changedPath) {
 		for (Trigger t: triggers)
 			t.checkTrigger(changedPath);
 	}
 
 	private Property prependParent(Property parent, Property path) {
 		if (path.getPath().isEmpty())
 			return parent;
 		else
 			return new Property(parent.getRoot(), parent.getPath() + "." + path.getPath());
 	}
 
 	public String printErrors() {
 		Map<Property, List<String>> errors = getErrors();
 		StringBuilder builder = new StringBuilder();
 		
 		for(Property property: errors.keySet()) {
 			List<String> currentErrors = errors.get(property);
 			if (currentErrors.isEmpty())
 				continue;
 			
 			builder.append(property).append(":\n");
 			for(String error: currentErrors)
 				builder.append("\t").append(error).append("\n");
 		}
 		
 		return builder.toString();
 	}
 	
 	protected void propagateChange(Property property) {
 		propagateChange(property, HashTreePSet.<Property> empty(), 0);
 	}
 	
	protected void notifyObservers() {
		for (Property linkToThis: new ArrayList<>(parentsLinkToThis)) {
			IObject root = linkToThis.getRoot();
			if (root instanceof Observer) {
				root.checkTriggers(linkToThis);
			}
		}
	}
	
 	private void propagateChange(Property property, PSet<Property> seen, int level) {
 		property.getRoot().checkTriggers(property);
 
 		if (level == MAXIMUM_CHANGE_PROPAGATION)
 			return;
 
 		for (Property linkToThis : new ArrayList<>(parentsLinkToThis)) {
 			if (seen.contains(linkToThis))
 				continue;
 			linkToThis.getRoot().propagateChange(prependParent(linkToThis, property), seen.plus(linkToThis), level + 1);
 		}
 	}
 	
 	public Property getProperty(String propertyName) {
 		return new Property(this, propertyName); // TODO: add some check to getProperty
 	}
 
 	private void recursivelyFindBoundProperties(Property prefixPath, List<Property> list, PSet<Property> seen) {
 		for (Trigger t: triggers)
 			list.addAll(t.getLocalBoundProperties(prefixPath));
 
 		for (Property linkToThis: parentsLinkToThis) {
 			if (seen.contains(linkToThis))
 				continue;
 			linkToThis.getRoot().recursivelyFindBoundProperties(prependParent(linkToThis, prefixPath), list, seen.plus(linkToThis));
 		}
 	}
 
 	private void recursivelyFindConstraints(Property path, List<Constraint> list, PSet<Property> seen) {
 		for(Property constrained: constraints.keySet()) {
 			if (constrained.includes(path))
 				list.addAll(constraints.get(constrained));
 		}
 
 		for (Property linkToThis: parentsLinkToThis) {
 			if (seen.contains(linkToThis))
 				continue;
 			linkToThis.getRoot().recursivelyFindConstraints(prependParent(linkToThis, path), list, seen.plus(linkToThis));
 		}
 	}
 
 	private void recursivelyFindErrors(Property basePath, Map<Property, List<String>> errors, Set<IObject> seen) {
 		if (seen.contains(this))
 			return;
 		else
 			seen.add(this);
 		
 		for(Property property: getUnboundProperties()) {
 			Property complete = appendChild(basePath, property);
 			
 			List<String> list = checkErrors(property);
 			if (!list.isEmpty())
 				errors.put(complete, list);
 			
 			if (property.getContent() instanceof IObject) {
 				property.getContent(IObject.class).recursivelyFindErrors(complete, errors, seen);
 			}
 		}
 	}
 
 	public void setContent(String propertyPath, Object content) {
 		if (propertyPath.isEmpty())
 			return;
 
 		int firstSplit = propertyPath.indexOf('.');
 		if (firstSplit < 0) {
 			innerSetLocal(propertyPath, content);
 		} else {
 			String localProperty = propertyPath.substring(0, firstSplit);
 			String remainingPath = propertyPath.substring(firstSplit + 1);
 			IObject local = (IObject) getLocal(localProperty);
 			if (local != null)
 				local.setContent(remainingPath, content);
 		}
 	}
 	
 	protected void setLocal(String propertyName, Object content) {
 		FieldAccess fieldAccess = FieldAccess.get(getClass());
 		fieldAccess.set(this, propertyName, content);
 	}
 	
 	@Override
 	public String toString() {
 		return name;
 	}
 	
 	public void write(File outFile) {
 		OutputStream out;
 		try {
 			out = new FileOutputStream(outFile);
 			write(out);
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void write(OutputStream out) {
 		Output output = new Output(out);
 		IObject copy = this.copy();
 		kryo.writeClassAndObject(output, copy);
 		output.close();
 	}
 
 }
