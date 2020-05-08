 package com.vonhof.babelshark.impl;
 
 import com.vonhof.babelshark.MappedBean.ObjectField;
 import com.vonhof.babelshark.*;
 import com.vonhof.babelshark.exception.MappingException;
 import com.vonhof.babelshark.node.SharkNode.NodeType;
 import com.vonhof.babelshark.node.*;
 import com.vonhof.babelshark.reflect.ClassInfo;
 import java.lang.reflect.Array;
 import java.util.Map.Entry;
 import java.util.*;
 
 /**
  * Default mapper for nodes to values and values to nodes
  * @author Henrik Hofmeister <@vonhofdk>
  */
 public class DefaultNodeMapper implements NodeMapper {
     
     private final BeanMapper beanMapper;
 
     public DefaultNodeMapper() {
         this(new DefaultBeanMapper());
     }
     
     public DefaultNodeMapper(BeanMapper beanMapper) {
         this.beanMapper = beanMapper;
     }
     
     public <T> T readAs(SharkNode node, ClassInfo<T> type) throws MappingException {
         return (T) readAs(node, SharkType.get(type));
     }
     
     public <T> T readAs(SharkNode node, Class<T> type) throws MappingException {
         return (T) readAs(node, SharkType.get(type));
     }
 
     public <T,U> T readAs(SharkNode node, SharkType<T,U> type) throws MappingException {
         if (type == null)
             type = (SharkType<T, U>) SharkType.forSimple(Object.class);
         
         if (SharkNode.class.isAssignableFrom(type.getType()))
             return (T) node;
         
         
         if (ReflectUtils.isPrimitive(type.getType())) {
             if (!node.is(NodeType.VALUE))
                 throw new MappingException(String.format("Could not convert %s to %s",node,type));
 
             ValueNode valueNode = (ValueNode) node;
             return readPrimitive(valueNode.getValue(),type.getType());
         }
         
         if (ReflectUtils.isMappable(type.getType())) {
             if (!node.is(NodeType.MAP))
                 throw new MappingException(String.format("Could not convert %s to %s",node,type));
             
             //If type is map 
             if (type.isMap() 
                     || type.getType().equals(Object.class))
                 return readMap((ObjectNode)node, type);
             return readBean((ObjectNode)node, type);
         } 
         if (type.isCollection()) {
             if (!node.is(NodeType.LIST))
                 throw new MappingException(String.format("Could not convert %s to %s",node,type));
             return readCollection((ArrayNode)node, type);
         }
         
         throw new MappingException(String.format("Could not convert %s to %s",node,type));
     }
     
     protected <T,U> T readMap(ObjectNode node,SharkType<T,U> type) throws MappingException {
         Map<String,U> out = null;
         try {
             
             Class clz = type.getType();
             if (!ReflectUtils.isInstantiatable(clz)) {
                 if (Map.class.isAssignableFrom(clz) || Object.class.equals(clz))
                     clz = LinkedHashMap.class;
                 else
                     throw new MappingException(String.format("Unknown map type: %s",type));
             }
             
             out = (Map<String, U>) clz.newInstance();
         } catch (Exception ex) {
             throw new MappingException(ex);
         }
         
         for(String field:node.getFields()) {
             out.put(field, readAs(node.get(field),type.getValueType()));
         }
         return (T) out;
     }
     
     protected <T> T readBean(ObjectNode node,SharkType<T,?> type) throws MappingException {
         final MappedBean<T> map = beanMapper.getMap(type.getType());
         final T out = map.newInstance(node);
         for(String field:node.getFields()) {
             final ObjectField oField = map.getField(field);
             if (oField == null || !oField.hasSetter()) continue;
             Object value = readAs(node.get(field),oField.getType());
             oField.set(out,value);
         }
         return out;
     }
     
     protected <T,V> T readCollection(ArrayNode node,SharkType<T,V> type) throws MappingException {
         Collection<V> out = null;
         try {
             Class clz = type.getType();
             if (clz.isArray()) {
                 Object array =  Array.newInstance(clz.getComponentType(), node.size());
                 for(int i = 0; i < node.size();i++) {
                     Object value = readAs(node.get(i),clz.getComponentType());
                     Array.set(array, i, value);
                 }
                 return (T) array;
             } else if (!ReflectUtils.isInstantiatable(clz)) {
                 if (Set.class.isAssignableFrom(clz))
                     clz = HashSet.class;
                 else if (List.class.isAssignableFrom(clz))
                     clz = ArrayList.class;
                 else
                     throw new MappingException(String.format("Unknown collection type: %s",type));
             }
             
             out = (Collection) clz.newInstance();
         } catch (Exception ex) {
             throw new MappingException(ex);
         }
         
         for(SharkNode childNode:node) {
             out.add(readAs(childNode,type.getValueType()));
         }
         return (T) out;
     }
     
     protected <T> T readPrimitive(Object o,Class<T> type) throws MappingException {
         if (o == null)
             return null;
         
         if (type.isInstance(o))
             return (T)o;
         
         if (type.equals(String.class)) {
             return (T) String.valueOf(o);
         }
         
         if (o instanceof String) {
             try {
                 return ConvertUtils.convert((String)o, type);
             } catch(RuntimeException ex) {
                 throw new MappingException(ex);
             }
         }
         if (o instanceof Number) {
             Number number = (Number) o;
             return ConvertUtils.convert(number, type);
         }
         
         throw new MappingException(String.format("Could not convert %s to %s",o,type.getName()));
     }
     
     public SharkNode toNode(Object instance) throws MappingException {
         if (instance instanceof SharkNode)
             return (SharkNode) instance;
         if (instance == null)
             return new ValueNode(null);
         SharkType type = SharkType.get(instance.getClass());
         
         if (ReflectUtils.isPrimitive(type.getType())) {
             return new ValueNode(instance);
         }
         
         if (ReflectUtils.isMappable(type.getType())) {
             ObjectNode node = new ObjectNode();
             if (type.isMap()) {
                 Map<Object,Object> map = (Map<Object,Object>)instance;
                 for (Entry<Object,Object> entry:map.entrySet()) {
                     node.put(String.valueOf(entry.getKey()),toNode(entry.getValue()));
                 }
             } else {
                 MappedBean<Object> map = beanMapper.getMap(type.getType());
                 for (String field:map.getFieldList()) {
                     ObjectField oField = map.getField(field);
                     if (!oField.hasGetter()) continue;
                     Object value = oField.get(instance);
                     node.put(field,toNode(value));
                 }
             }
             return node;
         } 
         if (type.isCollection()) {
             ArrayNode node = new ArrayNode();
             if (instance.getClass().isArray()) {
                 int length = Array.getLength(instance);
                 for(int i = 0; i < length;i++) {
                     Object value = Array.get(instance, i);
                     node.add(toNode(value));
                 }
             } else {
                 Collection list = (Collection) instance;
                 for (Object value:list) {
                     node.add(toNode(value));
                 }
             }
             
             return node;
         }
         
         throw new MappingException(String.format("Could not convert %s to node",type));
     }
 
 }
