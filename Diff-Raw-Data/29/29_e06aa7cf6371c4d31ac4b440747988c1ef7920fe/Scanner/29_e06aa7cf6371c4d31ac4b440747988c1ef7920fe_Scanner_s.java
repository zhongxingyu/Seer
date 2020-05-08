 package com.digitolio.jdbi.codegen;
 
 import com.digitolio.jdbi.annotations.CodeGen;
 import com.google.common.base.Predicate;
 import com.google.common.collect.FluentIterable;
 import net.sf.extcos.ComponentQuery;
 import net.sf.extcos.ComponentScanner;
 
 import java.util.Set;
 
 /**
  * @author C.Koc
  */
 public class Scanner {
     public Set<Class<?>> scanPackage(final String inputPackage, final String ignoreValue) {
 
        Set<Class<?>> classes = new ComponentScanner().getClasses(new ComponentQuery() {
           protected void query() {
              select()
                 .from(inputPackage)
                 .returning(allExtending(Object.class));
           }
        });
 
 
 /*
        Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setScanners(new SubTypesScanner(false *//* don't exclude Object.class *//*), new ResourcesScanner())
            .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[classLoadersList.size()])))
            .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(inputPackage))));
 */
         return FluentIterable
                 .from(classes)
                 .filter(new Predicate<Class<?>>() {
                     @Override
                     public boolean apply(Class<?> input) {
                         CodeGen annotation = input.getAnnotation(CodeGen.class);
                         if (annotation != null) {
                             String[] ignores = annotation.ignore();
                             for (String ignore : ignores) {
                                 if (ignore.equals(ignoreValue) || ignore.equals(CodeGen.ALL)) {
                                     return false;
                                 }
                             }
                         }
                         return true;
                     }
                }).toImmutableSet();
     }
 }
