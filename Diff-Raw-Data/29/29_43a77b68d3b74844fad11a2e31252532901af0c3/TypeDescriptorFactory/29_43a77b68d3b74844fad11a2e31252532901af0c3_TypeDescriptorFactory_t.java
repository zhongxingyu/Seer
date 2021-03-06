 /*******************************************************************************
 * Copyright (c) 2008  CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jurgen Vinju  (jurgen@vinju.org)       
 *******************************************************************************/
 package org.eclipse.imp.pdb.facts.type;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.imp.pdb.facts.IConstructor;
 import org.eclipse.imp.pdb.facts.IList;
 import org.eclipse.imp.pdb.facts.IListWriter;
 import org.eclipse.imp.pdb.facts.INode;
 import org.eclipse.imp.pdb.facts.IString;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.eclipse.imp.pdb.facts.exceptions.FactTypeDeclarationException;
 import org.eclipse.imp.pdb.facts.io.IValueReader;
 import org.eclipse.imp.pdb.facts.io.IValueWriter;
 import org.eclipse.imp.pdb.facts.visitors.NullVisitor;
 import org.eclipse.imp.pdb.facts.visitors.VisitorException;
 
 /**
  * This class converts types to representations of types as values and back.  
  * It can be used for (de)serialization of types via {@link IValueReader} and {@link IValueWriter}
  *
  */
 public class TypeDescriptorFactory {
 	private TypeFactory tf = TypeFactory.getInstance();
 	private TypeStore ts = new TypeStore();
 	
 	private Type typeSort = tf.abstractDataType(ts, "Type");
	private Type fieldType = tf.abstractDataType(ts, "Field");

 	private Type boolType = tf.constructor(ts, typeSort, "bool");
 	private Type doubleType = tf.constructor(ts, typeSort, "double");
 	private Type integerType = tf.constructor(ts, typeSort, "int");
 	private Type nodeType = tf.constructor(ts, typeSort, "node");
 	private Type listType = tf.constructor(ts, typeSort, "list", typeSort, "element");
 	private Type mapType = tf.constructor(ts, typeSort, "map", typeSort, "key", typeSort, "value");
 	private Type aliasType = tf.constructor(ts, typeSort, "alias", tf.stringType(), "name", typeSort, "aliased");
 	private Type relationType = tf.constructor(ts, typeSort, "relation", tf.listType(typeSort), "fields");
 	private Type setType = tf.constructor(ts, typeSort, "set", typeSort, "element");
 	private Type sourceLocationType = tf.constructor(ts, typeSort, "sourceLocation");
 	private Type sourceRangeType = tf.constructor(ts, typeSort, "sourceRange");
 	private Type stringType = tf.constructor(ts, typeSort, "string");
 	private Type constructorType = tf.constructor(ts, typeSort, "constructor", typeSort, "abstract-data-type", tf.stringType(), "name", tf.listType(typeSort), "children");
 	private Type abstractDataType = tf.constructor(ts, typeSort, "abstract-data-type", tf.stringType(), "name");
 	private Type parameterType = tf.constructor(ts, typeSort, "parameter", tf.stringType(), "name", typeSort, "bound");
	private Type tupleType = tf.constructor(ts, typeSort, "tuple", tf.listType(fieldType), "fields");
 	private Type valueType = tf.constructor(ts, typeSort, "value");
 	private Type voidType = tf.constructor(ts, typeSort, "void");
	private Type namedFieldType = tf.constructor(ts, fieldType, "field", typeSort, "type", tf.stringType(), "label");
 	private TypeStore store;
 
 	private static class InstanceHolder {
 		public static TypeDescriptorFactory sInstance = new TypeDescriptorFactory();
 	}
 	
 	private TypeDescriptorFactory() {}
 
 	public static TypeDescriptorFactory getInstance() {
 		return InstanceHolder.sInstance;
 	}
 	
 	public TypeStore getStore() {
 		return ts;
 	}
 	
 	/**
 	 * Create a representation of a type for use in (de)serialization or other
 	 * computations on types.
 	 * 
 	 * @param factory the factory to use to construct the values
 	 * @param type    the type to convert to a value
 	 * @return a value that represents this type and can be convert back to 
 	 *         the original type via {@link TypeDescriptorFactory#fromTypeDescriptor(INode)}
 	 */
 	public IValue toTypeDescriptor(IValueFactory factory, Type type) {
 		return type.accept(new ToTypeVisitor(factory));
 	}
 	
 	/**
 	 * Construct a type that is represented by this value. Will only work for values
 	 * that have been constructed using {@link TypeDescriptorFactory#toTypeDescriptor(IValueFactory, Type)},
 	 * or something that exactly mimicked it.
 	 * 
 	 * @param store      to store found declarations in
 	 * @param descriptor a value that represents a type
 	 * @return a type that was represented by the descriptor
 	 * @throws FactTypeDeclarationException if the descriptor is not a valid type descriptor
 	 */
 	public Type fromTypeDescriptor(TypeStore store, IValue descriptor) throws FactTypeDeclarationException {
 		try {
 			this.store = store;
 			return descriptor.accept(new FromTypeVisitor());
 		} catch (VisitorException e) {
 			// this does not happen since nobody throws a VisitorException
 			return null;
 		}
 	}
 	
 	private class FromTypeVisitor extends NullVisitor<Type> {
 		@Override
 		public Type visitConstructor(IConstructor o) throws VisitorException {
 			Type node = o.getConstructorType();
 		
 			if (node == boolType) {
 				return tf.boolType();
 			}
 			else if (node == doubleType) {
 				return tf.doubleType();
 			}
 			else if (node == integerType) {
 				return tf.integerType();
 			}
 			else if (node == nodeType) {
 				return tf.nodeType();
 			}
 			else if (node == listType) {
 				return tf.listType(o.get("element").accept(this));
 			}
 			else if (node == mapType) {
 				return tf.mapType(o.get("key").accept(this), o.get("value").accept(this));
 			}
 			else if (node == aliasType) {
 				return tf.aliasType(store, ((IString) o.get("name")).getValue(), o.get("aliased").accept(this));
 			}
 			else if (node == relationType) {
 				IList fieldValues = (IList) o.get("fields");
 				List<Type> fieldTypes = new LinkedList<Type>();
 				
 				for (IValue field : fieldValues) {
 					fieldTypes.add(field.accept(this));
 				}
 				
 				return tf.relTypeFromTuple(tf.tupleType(fieldTypes.toArray()));
 			}
 			else if (node == setType) {
 				return tf.setType(o.get("element").accept(this));
 			}
 			else if (node == sourceLocationType) {
 				return tf.sourceLocationType();
 			}
 			else if (node == sourceRangeType) {
 				return tf.sourceRangeType();
 			}
 			else if (node == stringType) {
 				return tf.stringType();
 			}
 			else if (node == constructorType) {
 				AbstractDataType sort = (AbstractDataType) o.get("abstract-data-type").accept(this);
 				String name = ((IString) o.get("name")).getValue();
 				
 				IList childrenValues = (IList) o.get("children");
				Object[] children = new Type[childrenValues.length() * 2];
 				
				for (int i = 0, j = 0; i < childrenValues.length(); i++, j+=2) {
					IConstructor child = (IConstructor) childrenValues.get(i);
					children[j] = child.get("type").accept(this);;
					children[j+1] = ((IString) child.get("label")).getValue();
 				}
 				
				return tf.constructor(store, sort, name, children);
 			}
 			else if (node == abstractDataType) {
 				return tf.abstractDataType(store, ((IString) o.get("name")).getValue());
 			}
 			else if (node == parameterType) {
 				return tf.parameterType(((IString) o.get("name")).getValue(), o.get("bound").accept(this));
 			}
 			else if (node == tupleType) {
 				IList fieldValues = (IList) o.get("fields");
 				List<Type> fieldTypes = new LinkedList<Type>();
 				
 				for (IValue field : fieldValues) {
 					fieldTypes.add(field.accept(this));
 				}
 				
 				return tf.tupleType(fieldTypes);	
 			}
 			else {
 				return tf.valueType();
 			}
 		}
 	}
 	
 	private class ToTypeVisitor implements ITypeVisitor<INode> {
 		private IValueFactory vf;
 		
 		public ToTypeVisitor(IValueFactory factory) {
 			this.vf = factory;
 		}
 		
 		public INode visitDouble(Type type) {
 			return vf.constructor(doubleType);
 		}
 
 		public INode visitInteger(Type type) {
 			return vf.constructor(integerType);
 		}
 
 		public INode visitList(Type type) {
 			return vf.constructor(listType, type.getElementType().accept(this));
 		}
 
 		public INode visitMap(Type type) {
 			return vf.constructor(mapType, type.getKeyType().accept(this), type.getValueType().accept(this));
 		}
 
 		public INode visitAlias(Type type) {
 			return vf.constructor(aliasType, vf.string(type.getName()), type.getAliased().accept(this));
 		}
 
 		public INode visitRelationType(Type type) {
 			IListWriter w = vf.listWriter(typeSort);
 			
 			for (Type field : type.getFieldTypes()) {
 				w.append(field.accept(this));
 			}
 			
 			return vf.constructor(relationType, w.done());
 		}
 
 		public INode visitSet(Type type) {
 			return vf.constructor(setType, type.getElementType().accept(this));
 		}
 
 		public INode visitSourceLocation(Type type) {
 			return vf.constructor(sourceLocationType);
 		}
 
 		public INode visitSourceRange(Type type) {
 			return vf.constructor(sourceRangeType);
 		}
 
 		public INode visitString(Type type) {
 			return vf.constructor(stringType);
 		}
 
 		public INode visitConstructor(Type type) {
			IListWriter w = vf.listWriter(fieldType);
 			
			for (int i = 0; i < type.getArity(); i++) {
				IValue field = namedFieldType.make(vf, type.getFieldType(i).accept(this), vf.string(type.getFieldName(i)));
				w.append(field);
 			}
 			
 			return vf.constructor(constructorType, type.getAbstractDataType().accept(this), vf.string(type.getName()), w.done());
 		}
 
 		public INode visitAbstractData(Type type) {
 			return vf.constructor(abstractDataType, vf.string(type.getName()));
 		}
 
 		public INode visitTuple(Type type) {
 			IListWriter w = vf.listWriter(typeSort);
 			
 			for (Type field : type) {
 				w.append(field.accept(this));
 			}
 			
 			
 			return vf.constructor(tupleType, w.done());
 		}
 
 		public INode visitValue(Type type) {
 			return vf.constructor(valueType);
 		}
 
 		public INode visitVoid(Type type) {
 			return vf.constructor(voidType);
 		}
 
 		public INode visitNode(Type type) {
 			return vf.constructor(nodeType);
 		}
 
 		public INode visitBool(Type type) {
 			return vf.constructor(boolType);
 		}
 
 		public INode visitParameter(Type type) {
 			return vf.constructor(parameterType, vf.string(type.getName()), type.getBound().accept(this));
 		}
 	}
 }
