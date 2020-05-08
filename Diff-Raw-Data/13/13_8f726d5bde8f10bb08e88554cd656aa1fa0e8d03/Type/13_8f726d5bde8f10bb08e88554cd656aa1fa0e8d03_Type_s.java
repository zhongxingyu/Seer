 package com.codereligion.hammock.compiler.model;
 
 import static com.google.common.collect.Iterables.any;
 import static com.google.common.collect.Iterables.removeIf;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.annotation.Generated;
 import javax.annotation.Nullable;
 
 import javax.lang.model.element.Element;
 import javax.lang.model.element.PackageElement;
 import javax.lang.model.element.TypeElement;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 
 public class Type {
 
     private static final Pattern GENERICS = Pattern.compile("<.+>");
 
     private final List<Closure> closures = new ArrayList<>();
     private final List<Type> types = new ArrayList<>();
     private final TypeElement element;
 
     public Type(final TypeElement element) {
         this.element = element;
     }
 
     public TypeElement getElement() {
         return element;
     }
 
     public String getPackage() {
         Element parent = element.getEnclosingElement();
 
         while (true) {
             if (parent instanceof PackageElement) {
                 final PackageElement packageElement = (PackageElement) parent;
                 return packageElement.getQualifiedName().toString();
             } else {
                 parent = parent.getEnclosingElement();
             }
         }
     }
 
     public Name getName() {
         return new Name(element.getQualifiedName() + "_");
     }
 
     public Iterable<String> getImports() {
         final Set<String> imports = new TreeSet<>();
 
         for (Closure closure : closures) {
             for (Argument argument : closure.getArguments()) {
                 imports.add(argument.getType().getQualified());
             }
 
             imports.add(closure.getInput().getType().getQualified());
             imports.add(closure.getReturnType().getQualified());
         }
 
         if (any(closures, Is.PREDICATE)) {
             imports.add(Predicate.class.getName());
         }
 
         if (any(closures, Is.FUNCTION)) {
             imports.add(Function.class.getName());
         }
 
         imports.add(Nullable.class.getName());
         imports.add(Generated.class.getName());
 
         for (Type type : types) {
             Iterables.addAll(imports, type.getImports());
         }
 
         removeIf(imports, Exclude.JAVA_LANG);
         removeIf(imports, Exclude.PRIMITIVES);
 
        final String samePackage = getPackage();
        removeIf(imports, Predicates.equalTo(samePackage));
 
         // are in the same package, but still need to get imported explicitly
         for (Type type : getTypes()) {
             addNestedToImports(type, imports);
         }
 
         return ungenerify(imports);
     }
 
     private void addNestedToImports(final Type type, final Set<String> imports) {
         if (!type.getClosures().isEmpty()) {
             imports.add(type.element.getQualifiedName().toString());
         }
 
         for (Type subtype : type.getTypes()) {
             addNestedToImports(subtype, imports);
         }
     }
 
     private Set<String> ungenerify(final Set<String> imports) {
         final Set<String> ungenerified = new TreeSet<>();
 
         for (String name : imports) {
             final Matcher matcher = GENERICS.matcher(name);
             ungenerified.add(matcher.replaceAll(""));
         }
 
         return ungenerified;
     }
 
     public List<Closure> getClosures() {
         return closures;
     }
 
     public List<Type> getTypes() {
         return types;
     }
 
     private enum Is implements Predicate<Closure> {
 
         PREDICATE {
             @Override
             public boolean apply(@Nullable final Closure input) {
                 return input != null && input.isPredicate();
             }
 
         },
 
         FUNCTION {
             @Override
             public boolean apply(@Nullable final Closure input) {
                 return input != null && !input.isPredicate();
             }
 
         }
 
     }
 
     private enum Exclude implements Predicate<String> {
 
         JAVA_LANG {
 
             @Override
             public boolean apply(@Nullable final String input) {
                 return input != null && input.startsWith("java.lang.");
             }
 
         },
 
         PRIMITIVES {
 
             private final Set<String> primitives = ImmutableSet.of("byte", "short", "int", "long", "float", "double",
                     "boolean", "char");
 
             @Override
             public boolean apply(@Nullable final String input) {
                 return primitives.contains(input);
             }
 
         }
 
     }
 
    private static Predicate<String> startsWith(final String prefix) {
         return new Predicate<String>() {
             @Override
             public boolean apply(@Nullable final String input) {
                return input != null && input.startsWith(prefix);
             }
         };
     }
 
 }
