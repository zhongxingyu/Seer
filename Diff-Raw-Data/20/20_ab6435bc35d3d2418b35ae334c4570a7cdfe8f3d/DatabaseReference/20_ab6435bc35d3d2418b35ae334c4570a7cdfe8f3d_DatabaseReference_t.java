 package com.seitenbau.testing.dbunit.dsl;
 
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

 public class DatabaseReference
 {
 
  protected <S, T> List<T> getOrCreateList(Map<S, List<T>> map, ThreadLocal<S> threadScope) 
  {
    final S scope = threadScope.get();
    if (scope == null) {
      throw new RuntimeException("Cannot build reference out of a scope");
    }
    List<T> result = map.get(scope);
    if (result == null) 
    {
      result = new LinkedList<T>();
      map.put(scope, result);
    }
    return result;
  }

 }
