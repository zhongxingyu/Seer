 package com.silentmatt.dss;
 
 import java.lang.reflect.Array;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  *
  * @author Matthew Crumley
  */
// FIXME: Map methods return the last declaration, ignoring the "!important" flag.
 public class DeclarationList implements List<Declaration> {
     private final List<Declaration> list = new LinkedList<Declaration>();
     private final Map<String, Expression> mapView = new DeclarationListMapView();
 
     public DeclarationList() {
     }
 
     public DeclarationList(List<Declaration> declarations) {
         list.addAll(declarations);
     }
 
     public int size() {
         return list.size();
     }
 
     public boolean isEmpty() {
         return list.isEmpty();
     }
 
     public boolean contains(Object arg0) {
         return list.contains(arg0);
     }
 
     public Iterator<Declaration> iterator() {
         return list.iterator();
     }
 
     public Object[] toArray() {
         return list.toArray();
     }
 
     public <T> T[] toArray(T[] arg0) {
         return list.toArray(arg0);
     }
 
     public boolean add(Declaration arg0) {
         return list.add(arg0);
     }
 
     public boolean remove(Object arg0) {
         return list.remove(arg0);
     }
 
     public boolean containsAll(Collection<?> arg0) {
         return list.containsAll(arg0);
     }
 
     public boolean addAll(Collection<? extends Declaration> arg0) {
         return list.addAll(arg0);
     }
 
     public boolean addAll(int arg0, Collection<? extends Declaration> arg1) {
         return list.addAll(arg0, arg1);
     }
 
     public boolean removeAll(Collection<?> arg0) {
         return list.removeAll(arg0);
     }
 
     public boolean retainAll(Collection<?> arg0) {
         return list.retainAll(arg0);
     }
 
     public void clear() {
         list.clear();
     }
 
     public Declaration get(int arg0) {
         return list.get(arg0);
     }
 
     public Declaration set(int arg0, Declaration arg1) {
         return list.set(arg0, arg1);
     }
 
     public void add(int arg0, Declaration arg1) {
         list.add(arg0, arg1);
     }
 
     public Declaration remove(int arg0) {
         return list.remove(arg0);
     }
 
     public int indexOf(Object arg0) {
         return list.indexOf(arg0);
     }
 
     public int lastIndexOf(Object arg0) {
         return list.lastIndexOf(arg0);
     }
 
     public ListIterator<Declaration> listIterator() {
         return list.listIterator();
     }
 
     public ListIterator<Declaration> listIterator(int start) {
         return list.listIterator(start);
     }
 
     public List<Declaration> subList(int start, int end) {
         return list.subList(start, end);
     }
 
     // Map methods
     public boolean containsKey(String key) {
         for (Declaration declaration : list) {
            if (declaration.getName().equalsIgnoreCase(key)) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean containsValue(Expression value) {
         for (Declaration declaration : list) {
             if (declaration.getExpression().equals(value)) {
                 return true;
             }
         }
         return false;
     }
 
     public Declaration getDeclaration(String name) {
         ListIterator<Declaration> it = list.listIterator(list.size());
         while (it.hasPrevious()) {
             Declaration declaration = it.previous();
            if (declaration.getName().equalsIgnoreCase(name)) {
                return declaration;
             }
         }
        return null;
     }
 
     public Expression get(String name) {
         Declaration declaration = getDeclaration(name);
         return declaration != null ? declaration.getExpression() : null;
     }
 
     public Expression put(String name, Expression expression) {
         Expression result = get(name);
         list.add(new Declaration(name, expression));
         return result;
     }
 
     public Expression remove(String key) {
         Expression result = null;
 
         ListIterator<Declaration> it = list.listIterator();
         while (it.hasNext()) {
             Declaration current = it.next();
            if (current.getName().equalsIgnoreCase(key)) {
                 result = current.getExpression();
                 it.remove();
             }
         }
 
         return result;
     }
 
     public void putAll(Map<? extends String, ? extends Expression> map) {
         for (Entry<? extends String, ? extends Expression> entry : map.entrySet()) {
             this.put(entry.getKey(), entry.getValue());
         }
     }
 
     public Map<String, Expression> asMap() {
         return this.mapView;
     }
 
     private class DeclarationListMapView implements Map<String, Expression> {
         public int size() {
             return DeclarationList.this.size();
         }
 
         public boolean isEmpty() {
             return DeclarationList.this.isEmpty();
         }
 
         public boolean containsKey(Object arg0) {
             return arg0 instanceof String && DeclarationList.this.containsKey((String) arg0);
         }
 
         public boolean containsValue(Object arg0) {
             return arg0 instanceof Expression && DeclarationList.this.containsValue((Expression) arg0);
         }
 
         public Expression get(Object arg0) {
             return arg0 instanceof String ? DeclarationList.this.get((String) arg0) : null;
         }
 
         public Expression put(String arg0, Expression arg1) {
             return DeclarationList.this.put(arg0, arg1);
         }
 
         public Expression remove(Object arg0) {
             if (arg0 instanceof String) {
                 return DeclarationList.this.remove((String) arg0);
             }
             return null;
         }
 
         public void putAll(Map<? extends String, ? extends Expression> arg0) {
             DeclarationList.this.putAll(arg0);
         }
 
         public void clear() {
             DeclarationList.this.clear();
         }
 
         public Set<String> keySet() {
             final Set<String> keys = new LinkedHashSet<String>();
             for (Declaration declaration : DeclarationList.this.list) {
                 keys.add(declaration.getName());
             }
 
             return new Set<String>() {
                 public int size() {
                     return keys.size();
                 }
 
                 public boolean isEmpty() {
                     return keys.isEmpty();
                 }
 
                 public boolean contains(Object arg0) {
                     return keys.contains(arg0);
                 }
 
                 public Iterator<String> iterator() {
                     final Iterator<String> it = keys.iterator();
                     return new Iterator<String>() {
                         private String current = null;
 
                         public boolean hasNext() {
                             return it.hasNext();
                         }
 
                         public String next() {
                             return current = it.next();
                         }
 
                         public void remove() {
                             keys.remove(current);
                             DeclarationList.this.remove(current);
                         }
                     };
                 }
 
                 public Object[] toArray() {
                     return keys.toArray();
                 }
 
                 public <T> T[] toArray(T[] arg0) {
                     return keys.toArray(arg0);
                 }
 
                 public boolean add(String arg0) {
                     throw new UnsupportedOperationException("Cannot call add to a key set.");
                 }
 
                 public boolean remove(Object arg0) {
                     if (!(arg0 instanceof String)) {
                         return false;
                     }
                     String key = (String) arg0;
                     DeclarationList.this.remove(key);
                     return keys.remove(key);
                 }
 
                 public boolean containsAll(Collection<?> arg0) {
                     return keys.containsAll(arg0);
                 }
 
                 public boolean addAll(Collection<? extends String> arg0) {
                     throw new UnsupportedOperationException("Cannot call addAll on a key set.");
                 }
 
                 public boolean retainAll(Collection<?> arg0) {
                     keys.retainAll(arg0);
                         throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public boolean removeAll(Collection<?> arg0) {
                     boolean result = false;
                     for (Object o : arg0) {
                         result |= remove(o);
                     }
                     return result;
                 }
 
                 public void clear() {
                     keys.clear();
                     DeclarationList.this.clear();
                 }
             };
         }
 
         public Collection<Expression> values() {
             return new Collection<Expression>() {
                 public int size() {
                     return DeclarationList.this.size();
                 }
 
                 public boolean isEmpty() {
                     return DeclarationList.this.isEmpty();
                 }
 
                 public boolean contains(Object arg0) {
                     return arg0 instanceof Expression && DeclarationList.this.containsValue((Expression) arg0);
                 }
 
                 public Iterator<Expression> iterator() {
                     return new Iterator<Expression>() {
                         Iterator<Declaration> it = DeclarationList.this.iterator();
                         public boolean hasNext() {
                             return it.hasNext();
                         }
 
                         public Expression next() {
                             return it.next().getExpression();
                         }
 
                         public void remove() {
                             it.remove();
                         }
                     };
                 }
 
                 public Object[] toArray() {
                     return toArray(new Object[size()]);
                 }
 
                 @SuppressWarnings("unchecked")
                 public <T> T[] toArray(T[] arg0) {
                     if (arg0.length >= size()) {
                         int i = 0;
                         for (Declaration declaration : DeclarationList.this) {
                            arg0[i++] = (T) declaration;
                         }
                         if (i < size()) {
                             arg0[i] = null;
                         }
                         return arg0;
                     }
 
                     T[] array = (T[]) Array.newInstance(arg0.getClass().getComponentType(), size());
                     int i = 0;
                     for (Object declaration : DeclarationList.this) {
                         try {
                             array[i++] = (T) declaration;
                         }
                         catch (ClassCastException ex) {
                             throw new ArrayStoreException();
                         }
                     }
                     return array;
                 }
 
                 public boolean add(Expression arg0) {
                     throw new UnsupportedOperationException("Cannot call add on a value set.");
                 }
 
                 public boolean remove(Object arg0) {
                     boolean result = false;
 
                     Iterator<Declaration> it = DeclarationList.this.iterator();
                     while (it.hasNext()) {
                         Declaration declaration = it.next();
                         if (declaration.getExpression().equals(arg0)) {
                             result = true;
                             it.remove();
                         }
                     }
 
                     return result;
                 }
 
                 public boolean containsAll(Collection<?> arg0) {
                     for (Object o : arg0) {
                         if (!contains(o)) {
                             return false;
                         }
                     }
                     return true;
                 }
 
                 public boolean addAll(Collection<? extends Expression> arg0) {
                     throw new UnsupportedOperationException("Cannot call addAll on a value set.");
                 }
 
                 public boolean removeAll(Collection<?> arg0) {
                     boolean result = false;
                     for (Object o : arg0) {
                         result |= remove(o);
                     }
                     return result;
                 }
 
                 public boolean retainAll(Collection<?> arg0) {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public void clear() {
                     DeclarationList.this.clear();
                 }
             };
         }
 
         public Set<Entry<String, Expression>> entrySet() {
             return new Set<Entry<String, Expression>>() {
                 public int size() {
                     return DeclarationList.this.size();
                 }
 
                 public boolean isEmpty() {
                     return DeclarationList.this.isEmpty();
                 }
 
                 public boolean contains(Object arg0) {
                     return DeclarationList.this.contains(arg0);
                 }
 
                 public Iterator<Entry<String, Expression>> iterator() {
                     final Iterator<Declaration> it = DeclarationList.this.iterator();
 
                     return new Iterator<Entry<String, Expression>>() {
                         public boolean hasNext() {
                             return it.hasNext();
                         }
 
                         public Entry<String, Expression> next() {
                             return it.next();
                         }
 
                         public void remove() {
                             it.remove();
                         }
                     };
                 }
 
                 public Object[] toArray() {
                     return DeclarationList.this.toArray();
                 }
 
                 public <T> T[] toArray(T[] arg0) {
                     return DeclarationList.this.toArray(arg0);
                 }
 
                 public boolean add(Entry<String, Expression> arg0) {
                     throw new UnsupportedOperationException("Cannot call add on an entry set.");
                 }
 
                 public boolean remove(Object arg0) {
                     return DeclarationList.this.remove(arg0);
                 }
 
                 public boolean containsAll(Collection<?> arg0) {
                     return DeclarationList.this.containsAll(arg0);
                 }
 
                 public boolean addAll(Collection<? extends Entry<String, Expression>> arg0) {
                     throw new UnsupportedOperationException("Cannot call addAll on an entry set.");
                 }
 
                 public boolean retainAll(Collection<?> arg0) {
                     return DeclarationList.this.retainAll(arg0);
                 }
 
                 public boolean removeAll(Collection<?> arg0) {
                     return DeclarationList.this.removeAll(arg0);
                 }
 
                 public void clear() {
                     DeclarationList.this.clear();
                 }
             };
         }
 
     }
 }
