 package com.thoughtworks.util;
 
 import com.thoughtworks.database.DatabaseHelper;
 import com.thoughtworks.model.NodeObject;
 import com.thoughtworks.model.Question;
 import org.neo4j.graphdb.Node;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 public class ListHelper {
 
     public static <T> List<T> substracts(List<T> supersetNode, List<T> subsetNode) {
 
         for (Iterator<T> it = supersetNode.iterator(); it.hasNext();) {
 
             NodeObject superNode = (NodeObject) it.next();
             for (Iterator<T> it1 = subsetNode.iterator(); it1.hasNext();) {
                 NodeObject subNode = (NodeObject) it1.next();
                 if (superNode.getName().equals(subNode.getName())) {
                     it.remove();
                 }
 
             }
         }
 
         return supersetNode;
     }
 
     public static <T> List<T> setSpecialProperties(Collection<Node> collectionNodes, T t) throws Exception {
 
         List<T> listOfObjects = new ArrayList<T>();
 
         for (Node node : collectionNodes) {
             Iterator<String> keysIterator = node.getPropertyKeys().iterator();
 
             T newClass = (T) t.getClass().newInstance();
             Class clazz = newClass.getClass();
 
             while (keysIterator.hasNext()) {
                 String keyString = keysIterator.next();
                 String methodName = "set" + keyString;
 
                 try {
                    Method m = clazz.getMethod(methodName, new Class[] {String.class});
                     m.invoke(newClass, node.getProperty(keyString).toString());
 
                    Method idMethod = clazz.getMethod("setId", new Class[] {long.class});
                    idMethod.invoke(newClass, node.getId());

                 } catch (NoSuchMethodException e) {
                     e.printStackTrace();
                 } catch (InvocationTargetException e) {
                     e.printStackTrace();
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                 }
 
             }
             listOfObjects.add(newClass);
         }
         return listOfObjects;
     }
 }
