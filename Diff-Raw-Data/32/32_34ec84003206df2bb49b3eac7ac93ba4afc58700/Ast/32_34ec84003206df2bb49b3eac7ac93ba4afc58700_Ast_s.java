 /*
 Copyright (c) 2013 Robby, Kansas State University.        
 All rights reserved. This program and the accompanying materials      
 are made available under the terms of the Eclipse Public License v1.0 
 which accompanies this distribution, and is available at              
 http://www.eclipse.org/legal/epl-v10.html                             
 */
 
 package edu.ksu.cis.santos.mdcf.dml.ast;
 
 import java.util.List;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 
 /**
  * Provides AST construction methods.
  * 
  * @author <a href="mailto:robby@k-state.edu">Robby</a>
  */
 public class Ast {
 
   /**
    * Provides AST construction methods with weaker type parameter constraints.
    * 
    * @author <a href="mailto:robby@k-state.edu">Robby</a>
    */
   public static class Weak {
 
     private static <T, O> boolean allInstanceOf(final Iterable<O> os,
         final Class<T> clazz) {
       for (final O o : os) {
         if (!clazz.isAssignableFrom(o.getClass())) {
           return false;
         }
       }
       return true;
     }
 
     public static Attribute attribute(final Iterable<?> annotations,
         final Type type, final String name, final Optional<?> init) {
       return Ast.attribute(
           list(annotations, AttributeAnnotation.class),
           type,
           name,
           opt(init, Initialization.class));
     }
 
     public static BasicInit basicInit(final String value) {
       return Ast.basicInit(value);
     }
 
     public static BasicType basicType(final String name,
         final Iterable<?> supers) {
       return Ast.basicType(name, list(supers, NamedType.class));
     }
 
     public static ConstAnnotation constAnnotation(final FeatureLevel level,
         final String qualifier) {
       return Ast.constAnnotation(level, qualifier);
     }
 
     public static DataAnnotation dataAnnotation() {
       return Ast.dataAnnotation();
     }
 
     public static EitherInit eitherInit(final int index,
         final Initialization init) {
       return Ast.eitherInit(index, init);
     }
 
     public static EitherType eitherType(final Iterable<?> choiceTypes) {
       return Ast.eitherType(list(choiceTypes, Type.class));
     }
 
     public static Feature feature(final Iterable<?> annotations,
         final String name, final Iterable<?> supers, final Iterable<?> members) {
       return Ast.feature(
           list(annotations, FeatureAnnotation.class),
           name,
           list(supers, NamedType.class),
           list(members, Member.class));
     }
 
     public static FeatureInit featureInit(final Iterable<?> types,
         final Iterable<?> attributes) {
       return Ast.featureInit(
           list(types, NamedType.class),
           list(attributes, Attribute.class));
     }
 
     public static FeatureLevelAnnotation featureLevelAnnotation(
         final FeatureLevel level, final String qualifier) {
       return Ast.featureLevelAnnotation(level, qualifier);
     }
 
     public static Invariant invariant(final String name,
         final PredicateType predicateType, final Object predicate) {
       return Ast.invariant(name, predicateType, predicate);
     }
 
     @SuppressWarnings("unchecked")
     private static <T, O> List<T> list(final Iterable<O> os,
         final Class<T> clazz) {
       if (!allInstanceOf(os, clazz)) {
         throw new IllegalArgumentException(
             "Expecting all elements to be of type " + clazz.getName());
       }
       if (os instanceof ImmutableList) {
         return (ImmutableList<T>) os;
       } else {
         return ImmutableList.<T> builder().addAll((Iterable<T>) os).build();
       }
     }
 
     public static <T> List<T> list(final Iterable<T> ts) {
       return Ast.list(ts);
     }
 
     @SafeVarargs
     public static <T> List<T> list(final T... ts) {
       return Ast.list(ts);
     }
 
     public static MapInit mapInit(final Iterable<?> keyInits,
         final Iterable<?> valueInits) {
       return Ast.mapInit(
           list(keyInits, Initialization.class),
           list(valueInits, Initialization.class));
     }
 
     public static MapType mapType(final Type keyType, final Type valueType) {
       return Ast.mapType(keyType, valueType);
     }
 
     public static Model model(final Iterable<?> declarations) {
       return Ast.model(list(declarations, Declaration.class));
     }
 
     public static MultiplicityAnnotation multiplicityAnnotation(final int lo,
         final int hi, final Optional<String> typeName) {
       return Ast.multiplicityAnnotation(lo, hi, typeName);
     }
 
     public static NamedType namedType(final String name) {
       return Ast.namedType(name);
     }
 
     public static <T> Optional<T> none() {
       return Ast.none();
     }
 
     public static NoneInit noneInit() {
       return Ast.noneInit();
     }
 
     @SuppressWarnings("unchecked")
     private static <T, O> Optional<T> opt(final Optional<O> opt,
         final Class<T> clazz) {
       if (!optInstanceOf(opt, clazz)) {
         throw new IllegalArgumentException(
             "Expecting optional referent to be of type " + clazz.getName());
       }
       return (Optional<T>) opt;
     }
 
     private static <T, O> boolean optInstanceOf(final Optional<O> opt,
         final Class<T> clazz) {
       if (opt.isPresent()) {
         return clazz.isAssignableFrom(opt.get().getClass());
       } else {
         return true;
       }
     }
 
     public static OptionType optionType(final Type elementType) {
       return Ast.optionType(elementType);
     }
 
     public static OverrideAnnotation overrideAnnotation() {
       return Ast.overrideAnnotation();
     }
 
     public static RefinedType refinedType(final Iterable<?> types,
         final Iterable<?> attributes) {
       return Ast.refinedType(
           list(types, NamedType.class),
           list(attributes, Attribute.class));
     }
 
     public static Requirement requirement(final String name,
        final Iterable<?> invariants) {
      return Ast.requirement(name, list(invariants, Invariant.class));
     }
 
     public static SeqInit seqInit(final Iterable<?> inits) {
       return Ast.seqInit(list(inits, Initialization.class));
     }
 
     public static SeqType seqType(final Type elementType) {
       return Ast.seqType(elementType);
     }
 
     public static SetInit setInit(final Iterable<?> inits) {
       return Ast.setInit(list(inits, Initialization.class));
     }
 
     public static SettableAnnotation settableAnnotation() {
       return Ast.settableAnnotation();
     }
 
     public static SetType setType(final Type elementType) {
       return Ast.setType(elementType);
     }
 
     public static <T> Optional<T> some(final T t) {
       return Ast.some(t);
     }
 
     public static SomeInit someInit(final Initialization init) {
       return Ast.someInit(init);
     }
 
     public static TupleInit tupleInit(final Iterable<?> inits) {
       return Ast.tupleInit(list(inits, Initialization.class));
     }
 
     public static TupleType tupleType(final Iterable<?> elementTypes) {
       return Ast.tupleType(list(elementTypes, Type.class));
     }
 
     private Weak() {
     }
   }
 
   public static Attribute attribute(
       final Iterable<AttributeAnnotation> annotations, final Type type,
       final String name, final Optional<Initialization> init) {
     return new Attribute(annotations, type, name, init);
   }
 
   public static BasicInit basicInit(final String value) {
     return new BasicInit(value);
   }
 
   public static BasicType basicType(final String name,
       final Iterable<NamedType> supers) {
     return new BasicType(name, supers);
   }
 
   public static ConstAnnotation constAnnotation(final FeatureLevel level,
       final String qualifier) {
     return new ConstAnnotation(level, qualifier);
   }
 
   public static DataAnnotation dataAnnotation() {
     return new DataAnnotation();
   }
 
   public static EitherInit eitherInit(final int index, final Initialization init) {
     return new EitherInit(index, init);
   }
 
   public static EitherType eitherType(final Iterable<Type> choiceTypes) {
     return new EitherType(choiceTypes);
   }
 
   public static Feature feature(final Iterable<FeatureAnnotation> annotations,
       final String name, final Iterable<NamedType> supers,
       final Iterable<Member> members) {
     return new Feature(annotations, name, supers, members);
   }
 
   public static FeatureInit featureInit(final Iterable<NamedType> types,
       final Iterable<Attribute> attributes) {
     return new FeatureInit(types, attributes);
   }
 
   public static FeatureLevelAnnotation featureLevelAnnotation(
       final FeatureLevel level, final String qualifier) {
     return new FeatureLevelAnnotation(level, qualifier);
   }
 
   public static Invariant invariant(final String name,
       final PredicateType predicateType, final Object predicate) {
     return new Invariant(name, predicateType, predicate);
   }
 
   public static <T> List<T> list(final Iterable<T> ts) {
     if (ts instanceof ImmutableList) {
       return (ImmutableList<T>) ts;
     } else {
       return ImmutableList.<T> builder().addAll(ts).build();
     }
   }
 
   @SafeVarargs
   public static <T> List<T> list(final T... ts) {
     return ImmutableList.<T> builder().add(ts).build();
   }
 
   public static MapInit mapInit(final Iterable<Initialization> keyInits,
       final Iterable<Initialization> valueInits) {
     return new MapInit(list(keyInits), list(valueInits));
   }
 
   public static MapType mapType(final Type keyType, final Type valueType) {
     return new MapType(keyType, valueType);
   }
 
   public static Model model(final Iterable<Declaration> declarations) {
     return new Model(declarations);
   }
 
   public static MultiplicityAnnotation multiplicityAnnotation(final int lo,
       final int hi, final Optional<String> typeName) {
     return new MultiplicityAnnotation(lo, hi, typeName);
   }
 
   public static NamedType namedType(final String name) {
     return new NamedType(name);
   }
 
   public static <T> Optional<T> none() {
     return Optional.<T> absent();
   }
 
   public static NoneInit noneInit() {
     return new NoneInit();
   }
 
   public static OptionType optionType(final Type elementType) {
     return new OptionType(elementType);
   }
 
   public static OverrideAnnotation overrideAnnotation() {
     return new OverrideAnnotation();
   }
 
   public static RefinedType refinedType(final Iterable<NamedType> types,
       final Iterable<Attribute> attributes) {
     return new RefinedType(types, attributes);
   }
 
   public static Requirement requirement(final String name,
      final Iterable<Invariant> invariants) {
    return new Requirement(name, invariants);
   }
 
   public static SeqInit seqInit(final Iterable<Initialization> inits) {
     return new SeqInit(inits);
   }
 
   public static SeqType seqType(final Type elementType) {
     return new SeqType(elementType);
   }
 
   public static SetInit setInit(final Iterable<Initialization> inits) {
     return new SetInit(inits);
   }
 
   public static SettableAnnotation settableAnnotation() {
     return new SettableAnnotation();
   }
 
   public static SetType setType(final Type elementType) {
     return new SetType(elementType);
   }
 
   public static <T> Optional<T> some(final T t) {
     return Optional.of(t);
   }
 
   public static SomeInit someInit(final Initialization init) {
     return new SomeInit(init);
   }
 
   public static TupleInit tupleInit(final Iterable<Initialization> inits) {
     return new TupleInit(inits);
   }
 
   public static TupleType tupleType(final Iterable<Type> elementTypes) {
     return new TupleType(elementTypes);
   }
 
   private Ast() {
   }
 }
