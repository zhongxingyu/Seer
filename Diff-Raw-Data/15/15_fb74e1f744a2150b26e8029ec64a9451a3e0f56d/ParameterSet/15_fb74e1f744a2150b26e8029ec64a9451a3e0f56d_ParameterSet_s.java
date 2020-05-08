 package ixcode.platform.reflect;
 
 import com.thoughtworks.paranamer.*;
 import ixcode.platform.collection.*;
 
 import java.lang.reflect.*;
 import java.util.*;
 
 import static ixcode.platform.collection.SetManipulation.intersectionOf;
 
 public class ParameterSet {
     public final Constructor constructor;
    public final Set<ParameterDefinition> parameterDefinitions = new HashSet<ParameterDefinition>();
 
     public ParameterSet(Constructor constructor, Paranamer paranamer) {
         this.constructor = constructor;
         discoverParameters(constructor, paranamer);
     }
 
     private void discoverParameters(Constructor constructor, Paranamer paranamer) {
         String[] parameterNames = paranamer.lookupParameterNames(constructor);
         int iParameter = 0;
         for (Class<?> type : constructor.getParameterTypes()) {
             add(parameterNames[iParameter], type);
             ++iParameter;
         }
     }
 
     void add(String name, Class<?> type) {
         parameterDefinitions.add(new ParameterDefinition(name, type));
     }
 
    public int numberOfMatchesTo(Set<String> propertyNames) {
        Set<ParameterDefinition> intersection = intersectionOf(propertyNames, parameterDefinitions, new ItemMatcher<String, ParameterDefinition>() {
            public boolean matches(String propertyName, ParameterDefinition parameterDefinition) {
                return propertyName.equals(parameterDefinition.name);
            }
        });
        return intersection.size();
     }
 
     public int numberOfParameters() {
         return parameterDefinitions.size();
     }
 
     public static class ParameterDefinition {
         public final String name;
         public final Class<?> type;
 
         private ParameterDefinition(String name, Class<?> type) {
             this.name = name;
             this.type = type;
         }
     }
 }
