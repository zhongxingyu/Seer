 /*
 Copyright (c) 2013 Robby, Kansas State University.        
 All rights reserved. This program and the accompanying materials      
 are made available under the terms of the Eclipse Public License v1.0 
 which accompanies this distribution, and is available at              
 http://www.eclipse.org/legal/epl-v10.html                             
 */
 
 package edu.ksu.cis.santos.mdcf.dml.symbol;
 
 import static com.google.common.base.Preconditions.checkArgument;
 
 import java.lang.ref.SoftReference;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.annotation.Nullable;
 
 import org.apache.commons.lang3.tuple.ImmutablePair;
 import org.apache.commons.lang3.tuple.Pair;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.base.Predicate;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMultimap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Multimap;
 
 import edu.ksu.cis.santos.mdcf.dml.ast.AbstractVisitor;
 import edu.ksu.cis.santos.mdcf.dml.ast.Attribute;
 import edu.ksu.cis.santos.mdcf.dml.ast.BasicType;
 import edu.ksu.cis.santos.mdcf.dml.ast.Declaration;
 import edu.ksu.cis.santos.mdcf.dml.ast.Feature;
 import edu.ksu.cis.santos.mdcf.dml.ast.FeatureInit;
 import edu.ksu.cis.santos.mdcf.dml.ast.Invariant;
 import edu.ksu.cis.santos.mdcf.dml.ast.Member;
 import edu.ksu.cis.santos.mdcf.dml.ast.Model;
 import edu.ksu.cis.santos.mdcf.dml.ast.NamedType;
 import edu.ksu.cis.santos.mdcf.dml.ast.RefinedType;
 import edu.ksu.cis.santos.mdcf.dml.ast.Requirement;
 import edu.ksu.cis.santos.mdcf.dml.ast.Type;
 
 /**
  * Provides API to retrieve DML entities such as basic types, features,
  * attributes, and invariants in the models or by their fully-qualified name.
  * 
  * @author <a href="mailto:robby@k-state.edu">Robby</a>
  */
 public final class SymbolTable {
 
   /**
    * Represents {@link Declaration} kind.
    * 
    * @author <a href="mailto:robby@k-state.edu">Robby</a>
    */
   public enum Kind {
     BasicType, Feature, Requirement
   }
 
   private transient final static Map<String, Kind> kindMap = ImmutableMap
       .<String, Kind> of(
           BasicType.class.getName(),
           Kind.BasicType,
           Feature.class.getName(),
           Kind.Feature,
           Requirement.class.getName(),
           Kind.Requirement);
 
   /**
    * Creates a symbol table of the provided models.
    * 
    * @param models
    *          The models whose symbol table to be created.
    * @return A symbol table.
    */
   public static SymbolTable of(final Model... models) {
     return new SymbolTable(models);
   }
 
   /**
    * The list of models used to construct this symbol table.
    */
   public final List<Model> models;
 
   private transient Map<String, Declaration> _declarationMap;
   private transient Map<String, Map<String, Pair<Feature, Member>>> _featureMemberMap;
   private transient SoftReference<List<RefinedType>> _refinedTypes;
   private transient SoftReference<List<FeatureInit>> _featureInits;
   private transient SoftReference<Multimap<String, String>> _superMap;
   private transient SoftReference<Multimap<String, String>> _subMap;
 
   private transient static final Function<NamedType, String> namedType2String = new Function<NamedType, String>() {
     @Override
     @Nullable
     public String apply(@Nullable final NamedType input) {
       return input.name;
     }
   };
 
   private SymbolTable(final Model... models) {
     this.models = ImmutableList.<Model> builder().add(models).build();
   }
 
   /**
    * Retrieves an immutable {@link Map} of {@link Attribute#name} to
    * {@link Attribute} with its declaring {@link Feature} that contains all
    * declared and closest (least) inherited attributes of the provided features.
    * 
    * @param featureNames
    *          The fully-qualified name ({@link Feature#name}) of the
    *          {@link Feature}s whose attributes to be retrieved.
    * @return an immutable {@link Map}.
    */
   public Map<String, Pair<Feature, Attribute>> allAttributeMap(
       final Iterable<String> featureNames) {
     return filterp(allMemberMap(featureNames), Attribute.class);
   }
 
   /**
    * Retrieves an immutable {@link Map} of {@link Attribute#name} to
    * {@link Attribute} with its declaring {@link Feature} that contains all
    * declared and closest (least) inherited attributes of the provided named
    * types.
    * 
    * @param namedTypes
    *          The {@link NamedType}s referring to {@link Feature}s whose
    *          attributes to be retrieved.
    * @return an immutable {@link Map}.
    */
   public Map<String, Pair<Feature, Attribute>> allAttributeMap(
       final List<NamedType> namedTypes) {
     return filterp(allMemberMap(namedTypes), Attribute.class);
   }
 
   /**
    * Retrieves an immutable {@link Map} of {@link Attribute#name} to
    * {@link Attribute} with its declaring {@link Feature} that contains all
    * declared and closest (least) inherited attributes of the provided feature.
    * 
    * @param featureName
    *          The fully-qualified name ({@link Feature#name}) of the
    *          {@link Feature} whose attributes to be retrieved.
    * @return an immutable {@link Map}.
    */
   public Map<String, Pair<Feature, Attribute>> allAttributeMap(
       final String featureName) {
     return filterp(allMemberMap(featureName), Attribute.class);
   }
 
   /**
    * Retrieves an immutable {@link Set} of {@link Attribute} with its declaring
    * {@link Feature} that contains all overriden/overriding attributes of the
    * provided features and attribute name.
    * 
    * @param featureNames
    *          The fully-qualified name ({@link Feature#name}) of the
    *          {@link Feature}s whose attributes to be retrieved.
    * @param attributeName
    *          The {@link Attribute#name}.
    * @param isOverriden
    *          Indicates whether to retrieve overriden/overriding attributes.
    * @return an immutable {@link Set}.
    */
   public Set<Pair<Feature, Attribute>> allAttributes(
       final Iterable<String> featureNames, final String attributeName,
       final boolean isOverriden) {
     return allMembers(featureNames, Attribute.class, attributeName, isOverriden);
   }
 
   /**
    * Retrieves an immutable {@link Set} of {@link Attribute} with its declaring
    * {@link Feature} that contains all overriden/overriding attributes of the
    * provided named types and attribute name.
    * 
    * @param namedTypes
    *          The {@link NamedType}s referring to {@link Feature}s whose
    *          attributes to be retrieved.
    * @param attributeName
    *          The {@link Attribute#name}.
    * @param isOverriden
    *          Indicates whether to retrieve overriden/overriding attributes.
    * @return an immutable {@link Set}.
    */
   public Set<Pair<Feature, Attribute>> allAttributes(
       final List<NamedType> namedTypes, final String attributeName,
       final boolean isOverriden) {
     return allMembers(
         Iterables.transform(namedTypes, SymbolTable.namedType2String),
         Attribute.class,
         attributeName,
         isOverriden);
   }
 
   /**
    * Retrieves an immutable {@link Set} of {@link Attribute} with its declaring
    * {@link Feature} that contains all overriden/overriding attributes of the
    * provided named types and attribute name.
    * 
    * @param featureName
    *          The fully-qualified name ({@link Feature#name}) of the
    *          {@link Feature} whose attributes to be retrieved.
    * @param attributeName
    *          The {@link Attribute#name}.
    * @param isOverriden
    *          Indicates whether to retrieve overriden/overriding attributes.
    * @return an immutable {@link Set}.
    */
   public Set<Pair<Feature, Attribute>> allAttributes(final String featureName,
       final String attributeName, final boolean isOverriden) {
     return allMembers(
         ImmutableList.of(featureName),
         Attribute.class,
         attributeName,
         isOverriden);
   }
 
   /**
    * Retrieves an immutable {@link Map} of {@link Invariant#name} to
    * {@link Invariant} with its declaring {@link Feature} that contains all
    * declared and inherited invariants of the provided features.
    * 
    * @param featureNames
    *          The fully-qualified name ({@link Feature#name}) of the
    *          {@link Feature}s whose invariants to be retrieved.
    * @return an immutable {@link Map}.
    */
   public Map<String, Pair<Feature, Invariant>> allInvariantMap(
       final Iterable<String> featureNames) {
     return filterp(allMemberMap(featureNames), Invariant.class);
   }
 
   /**
    * Retrieves an immutable {@link Map} of {@link Invariant#name} to
    * {@link Invariant} with its declaring {@link Feature} that contains all
    * declared and inherited invariants of the provided named types.
    * 
    * @param namedTypes
    *          The {@link NamedType}s referring to {@link Feature}s whose
    *          invariants to be retrieved.
    * @return an immutable {@link Map}.
    */
   public Map<String, Pair<Feature, Invariant>> allInvariantMap(
       final List<NamedType> namedTypes) {
     return filterp(allMemberMap(namedTypes), Invariant.class);
   }
 
   /**
    * Retrieves an immutable {@link Map} of {@link Invariant#name} to
    * {@link Invariant} with its declaring {@link Feature} that contains all
    * declared and inherited invariants of the provided feature.
    * 
    * @param featureName
    *          The fully-qualified name ({@link Feature#name}) of the
    *          {@link Feature} whose invariants to be retrieved.
    * @return an immutable {@link Map}.
    */
   public Map<String, Pair<Feature, Invariant>> allInvariantMap(
       final String featureName) {
     return filterp(allMemberMap(featureName), Invariant.class);
   }
 
   /**
    * Retrieves an immutable {@link Map} of {@link Member#name} to {@link Member}
    * with its declaring {@link Feature} that contains all declared and closest
    * (least) inherited members of the provided features.
    * 
    * @param featureNames
    *          The fully-qualified name ({@link Feature#name}) of the
    *          {@link Feature}s whose members to be retrieved.
    * @return an immutable {@link Map}.
    */
   public Map<String, Pair<Feature, Member>> allMemberMap(
       final Iterable<String> featureNames) {
     final TreeMap<String, Pair<Feature, Member>> b = new TreeMap<>();
 
     for (final String featureName : featureNames) {
       b.putAll(allMemberMap(featureName));
     }
 
     return Collections.unmodifiableMap(b);
   }
 
   /**
    * Retrieves an immutable {@link Map} of {@link Member#name} to {@link Member}
    * with its declaring {@link Feature} that contains all declared and inherited
    * closest (least) members of the provided named types.
    * 
    * @param namedTypes
    *          The {@link NamedType}s referring to {@link Feature}s whose members
    *          to be retrieved.
    * @return an immutable {@link Map}.
    */
   public Map<String, Pair<Feature, Member>> allMemberMap(
       final List<NamedType> namedTypes) {
     return allMemberMap(Iterables.transform(
         namedTypes,
         SymbolTable.namedType2String));
   }
 
   /**
    * Retrieves an immutable {@link Map} of {@link Member#name} to
    * {@link Invariant} with its declaring {@link Feature} that contains all
    * declared and closest (least) inherited members of the provided feature.
    * 
    * @param featureName
    *          The fully-qualified name ({@link Feature#name}) of the
    *          {@link Feature} whose members to be retrieved.
    * @return an immutable {@link Map}.
    */
   public Map<String, Pair<Feature, Member>> allMemberMap(
       final String featureName) {
     final Map<String, Map<String, Pair<Feature, Member>>> map = featureMemberMap();
 
     Map<String, Pair<Feature, Member>> result = map.get(featureName);
 
     if (result == null) {
       final TreeMap<String, Pair<Feature, Member>> b = new TreeMap<>();
 
       final Feature feature = feature(featureName);
 
       for (final NamedType nt : feature.supers) {
         b.putAll(allMemberMap(nt.name));
       }
 
       for (final Member m : feature.members) {
         b.put(m.name, ImmutablePair.of(feature, m));
       }
 
       result = Collections.unmodifiableMap(b);
 
       map.put(featureName, result);
     }
 
     return result;
   }
 
   private <T extends Member> Set<Pair<Feature, T>> allMembers(
       final Iterable<String> featureNames, final Class<T> clazz,
       final String memberName, final boolean isOverriden) {
     final LinkedHashSet<Pair<Feature, T>> b = new LinkedHashSet<>();
     final Multimap<String, String> map = isOverriden ? superTransitiveMap()
         : subTransitiveMap();
     for (final String featureName : featureNames) {
       Member m = declaredMemberMap(featureName).get(memberName);
       if ((m != null) && clazz.isAssignableFrom(m.getClass())) {
         @SuppressWarnings("unchecked")
         final T t = (T) m;
         b.add(ImmutablePair.<Feature, T> of(feature(featureName), t));
       }
       final Collection<String> superNames = map.get(featureName);
       if (superNames != null) {
         for (final String superName : superNames) {
           m = declaredMemberMap(superName).get(memberName);
           if ((m != null) && clazz.isAssignableFrom(m.getClass())) {
             @SuppressWarnings("unchecked")
             final T t = (T) m;
             b.add(ImmutablePair.<Feature, T> of(feature(featureName), t));
           }
         }
       }
     }
     return Collections.unmodifiableSet(b);
   }
 
   /**
    * Retrieves a {@link BasicType} from its fully-qualified name (
    * {@link BasicType#name}).
    * 
    * @param name
    *          The fully-qualified name of the {@link BasicType}.
    * @return The {@link BasicType}.
    * 
    * @throws {@link IllegalArgumentException} if the provided name is not a
    *         {@link BasicType} in the {@link #models}.
    */
   public BasicType basicType(final String name) {
     final Map<String, Declaration> dm = declarationMap();
     checkArgument(dm.containsKey(name));
     final Declaration d = dm.get(name);
     checkArgument(d instanceof BasicType);
     return (BasicType) d;
   }
 
   /**
    * Retrieves a {@link BasicType} from its fully-qualified name (
    * {@link BasicType#name}), if any.
    * 
    * @param name
    *          The fully-qualified name to query.
    * @return The {@link BasicType}, if found in {@link #models}.
    */
   public Optional<BasicType> basicTypeOpt(final String name) {
     Optional<BasicType> result = Optional.absent();
     final Declaration d = declarationMap().get(name);
     if (d instanceof BasicType) {
       result = Optional.of((BasicType) d);
     }
     return result;
   }
 
   /**
    * Retrieves all {@link BasicType}s declared in the {@link #models}.
    * 
    * @return an immutable {@link List}.
    */
   public List<BasicType> basicTypes() {
     return declarations(BasicType.class);
   }
 
   /**
    * Retrieves an immutable {@link Map} of {@link Declaration#name} to
    * {@link Declaration} that contains all declarations in the {@link #models}.
    * 
    * @return an immutable {@link Map}.
    */
   public Map<String, Declaration> declarationMap() {
     if (this._declarationMap == null) {
       final TreeMap<String, Declaration> dm = new TreeMap<>();
 
       new AbstractVisitor() {
         @Override
         public boolean visitDeclaration(final Declaration node) {
           dm.put(node.name, node);
           return false;
         }
 
         @Override
         public boolean visitType(final Type node) {
           return false;
         }
       }.visit(this.models);
 
       this._declarationMap = Collections.unmodifiableMap(dm);
     }
 
     return this._declarationMap;
   }
 
   /**
    * Retrieves an immutable {@link Set} of {@link Declaration#name} declared in
    * the {@link #models}.
    * 
    * @return an immutable {@link Set}.
    */
   public Set<String> declarationNames() {
     return declarationMap().keySet();
   }
 
   private <T> List<T> declarations(final Class<T> clazz) {
     final ImmutableList.Builder<T> b = ImmutableList.builder();
     for (final Model m : this.models) {
       for (final Declaration d : m.declarations) {
         if (clazz.equals(d.getClass())) {
           @SuppressWarnings("unchecked")
           final T t = (T) d;
           b.add(t);
         }
       }
     }
     return b.build();
   }
 
   /**
    * Retrieves an immutable {@link Map} of {@link Attribute#name} to
    * {@link Attribute} that contains all declared attributes of the provided
    * feature.
    * 
    * @param featureName
    *          The fully-qualified name ({@link Feature#name}) of the
    *          {@link Feature} whose attributes to be retrieved.
    * @return an immutable {@link Map}.
    */
   public Map<String, Attribute> declaredAttributeMap(final String featureName) {
     return filter(declaredMemberMap(featureName), Attribute.class);
   }
 
   /**
    * Retrieves an immutable {@link Collection} of all declared {@link Attribute}
    * s of the provided feature.
    * 
    * @param featureName
    *          The fully-qualified name ({@link Feature#name}) of the
    *          {@link Feature} whose attributes to be retrieved.
    * @return an immutable {@link Collection}.
    */
   public Collection<Attribute> declaredAttributes(final String featureName) {
     return declaredAttributeMap(featureName).values();
   }
 
   /**
    * Retrieves an immutable {@link Map} of {@link Invariant#name} to
    * {@link Invariant} that contains all declared invariants of the provided
    * feature.
    * 
    * @param featureName
    *          The fully-qualified name ({@link Feature#name}) of the
    *          {@link Feature} whose invariants to be retrieved.
    * @return an immutable {@link Map}.
    */
   public Map<String, Invariant> declaredInvariantMap(final String featureName) {
     return filter(declaredMemberMap(featureName), Invariant.class);
   }
 
   /**
    * Retrieves an immutable {@link Collection} of all declared {@link Invariant}
    * s of the provided feature.
    * 
    * @param featureName
    *          The fully-qualified name ({@link Feature#name}) of the
    *          {@link Feature} whose attributes to be retrieved.
    * @return an immutable {@link Collection}.
    */
   public Collection<Invariant> declaredInvariants(final String featureName) {
     return declaredInvariantMap(featureName).values();
   }
 
   /**
    * Retrieves an immutable {@link Map} of {@link Member#name} to {@link Member}
    * that contains all declared members of the provided feature.
    * 
    * @param featureName
    *          The fully-qualified name ({@link Feature#name}) of the
    *          {@link Feature} whose members to be retrieved.
    * @return an immutable {@link Map}.
    */
   public Map<String, Member> declaredMemberMap(final String featureName) {
     final TreeMap<String, Member> b = new TreeMap<>();
     for (final Member m : feature(featureName).members) {
       b.put(m.name, m);
     }
     return Collections.unmodifiableMap(b);
   }
 
   /**
    * Retrieves an immutable {@link Collection} of all declared {@link Member}s
    * of the provided feature.
    * 
    * @param featureName
    *          The fully-qualified name ({@link Feature#name}) of the
    *          {@link Feature} whose members to be retrieved.
    * @return an immutable {@link Collection}.
    */
   public Collection<Member> declaredMembers(final String featureName) {
     return feature(featureName).members;
   }
 
   /**
    * Retrieves a {@link Feature} from its fully-qualified name (
    * {@link Feature#name}).
    * 
    * @param name
    *          The fully-qualified name ({@link Feature#name}) of the
    *          {@link Feature}.
    * @return The {@link Feature}.
    * 
    * @throws {@link IllegalArgumentException} if the provided name is not a
    *         {@link Feature} in the {@link #models}.
    */
   public Feature feature(final String name) {
     final Map<String, Declaration> dm = declarationMap();
     checkArgument(dm.containsKey(name));
     final Declaration d = dm.get(name);
     checkArgument(d instanceof Feature);
     return (Feature) dm.get(name);
   }
 
   /**
    * Retrieves all {@link FeatureInit}s declared in the {@link #models}.
    * 
    * @return an immutable {@link List}.
    */
   public List<FeatureInit> featureInits() {
     List<FeatureInit> result = null;
     if ((this._featureInits == null)
         || ((result = this._featureInits.get()) == null)) {
       final ImmutableList.Builder<FeatureInit> b = ImmutableList.builder();
 
       new AbstractVisitor() {
         @Override
         public boolean visitFeatureInit(final FeatureInit node) {
           b.add(node);
           return true;
         }
       }.visit(this.models);
       result = b.build();
       this._featureInits = new SoftReference<List<FeatureInit>>(result);
     }
     return result;
   }
 
   /**
    * Retrieves an immutable {@link Map} of {@link Feature#name} to {@link Map}
    * of {@link Member#name} to {@link Member} with its declaring {@link Feature}
    * that contains all features' and their declared and closest (least)
    * inherited members.
    * 
    * @return an immutable {@link Map}.
    */
   public Map<String, Map<String, Pair<Feature, Member>>> featureMemberMap() {
     if (this._featureMemberMap == null) {
       this._featureMemberMap = CacheBuilder.newBuilder().softValues()
           .<String, Map<String, Pair<Feature, Member>>> build().asMap();
     }
     return this._featureMemberMap;
   }
 
   /**
    * Retrieves a {@link Feature} from its fully-qualified name (
   * {@link Feature#name}), if any.
    * 
    * @param name
    *          The fully-qualified name to query.
    * @return The {@link Feature}, if found in {@link #models}.
    */
   public Optional<Feature> featureOpt(final String name) {
     Optional<Feature> result = Optional.absent();
     final Declaration d = declarationMap().get(name);
     if (d instanceof Feature) {
       result = Optional.of((Feature) d);
     }
     return result;
   }
 
   /**
    * Retrieves all {@link Feature}s declared in the {@link #models}.
    * 
    * @return an immutable {@link List}.
    */
   public List<Feature> features() {
     return declarations(Feature.class);
   }
 
   /**
    * Retrieves an immutable map {@link Map} whose entries come from the provided
    * map where the entries' value is an instance of the provided {@link Class}.
    * 
    * @param m
    *          The map whose entries will be used to construct a filtered map
    *          according to the provided class.
    * @param clazz
    *          The class for filtering the provided map.
    * @return an immutable {@link Map}.
    */
   public <V, T> Map<String, T> filter(final Map<String, V> m,
       final Class<T> clazz) {
     return Maps.transformValues(Maps.filterValues(m, new Predicate<V>() {
       @Override
       public boolean apply(@Nullable final V input) {
         if (input != null) {
           return clazz.isAssignableFrom(input.getClass());
         } else {
           return false;
         }
       }
     }), new Function<V, T>() {
       @SuppressWarnings("unchecked")
       @Override
       @Nullable
       public T apply(@Nullable final V input) {
         return (T) input;
       }
     });
   }
 
   /**
    * Retrieves an immutable map {@link Map} whose entries come from the provided
    * map where the entries' value's second element is an instance of the
    * provided {@link Class}.
    * 
    * @param m
    *          The map whose entries will be used to construct a filtered map
    *          according to the provided class.
    * @param clazz
    *          The class for filtering the provided map.
    * @return an immutable {@link Map}.
    */
   public <V, T> Map<String, Pair<Feature, T>> filterp(
       final Map<String, Pair<Feature, V>> m, final Class<T> clazz) {
     return Maps.transformValues(
         Maps.filterValues(m, new Predicate<Pair<Feature, V>>() {
           @Override
           public boolean apply(@Nullable final Pair<Feature, V> input) {
             if (input != null) {
               return clazz.isAssignableFrom(input.getRight().getClass());
             } else {
               return false;
             }
           }
         }),
         new Function<Pair<Feature, V>, Pair<Feature, T>>() {
           @SuppressWarnings("unchecked")
           @Override
           @Nullable
           public Pair<Feature, T> apply(@Nullable final Pair<Feature, V> input) {
             return ImmutablePair.of(input.getLeft(), (T) input.getRight());
           }
         });
   }
 
   /**
    * Determines whether a {@link Declaration} fully-qualified name (
    * {@link Declaration#name}) is of a {@link BasicType}.
    * 
    * @param name
    *          The fully-qualified name to query.
    * 
    * @return true if the fully-qualified name is of a {@link BasicType}.
    */
   public boolean isBasicType(final String name) {
     return kind(name) == Kind.BasicType;
   }
 
   /**
    * Determines whether a {@link Declaration} fully-qualified name (
    * {@link Declaration#name}) is of a {@link Feature}.
    * 
    * @param name
    *          The fully-qualified name to query.
    * 
    * @return true if the fully-qualified name is of a {@link Feature}.
    */
   public boolean isFeature(final String name) {
     return kind(name) == Kind.Feature;
   }
 
   /**
    * Determines whether a {@link Declaration} fully-qualified name (
    * {@link Declaration#name}) is of a {@link Requirement}.
    * 
    * @param name
    *          The fully-qualified name to query.
    * 
    * @return true if the fully-qualified name is of a {@link Requirement}.
    */
   public boolean isRequirement(final String name) {
     return kind(name) == Kind.Requirement;
   }
 
   /**
    * Determines whether a fully-qualified name of a {@link BasicType} or a
    * {@link Feature} is a sub type of another {@link BasicType} or a
    * {@link Feature}.
    * 
    * @param typeName
    *          The fully-qualified name to query.
    * @param name
    *          The fully-qualified name of a super type to query.
    * @return true if typeName is a super type of name.
    */
   public boolean isSubTypeOf(final String typeName, final String name) {
     return superTransitiveMap().containsEntry(typeName, name);
   }
 
   /**
    * Determines whether a fully-qualified name of a {@link BasicType} or a
    * {@link Feature} is a super type of another {@link BasicType} or a
    * {@link Feature}.
    * 
    * @param typeName
    *          The fully-qualified name to query.
    * @param name
    *          The fully-qualified name of a sub type to query.
    * @return true if typeName is a super type of name.
    */
   public boolean isSuperTypeOf(final String typeName, final String name) {
     return subTransitiveMap().containsEntry(typeName, name);
   }
 
   /**
    * Retrieves the kind of a {@link Declaration}.
    * 
    * @param declarationName
    *          The fully-qualified name ({@link Declaration#name}) of the
    *          {@link Declaration} to query.
    * @return the {@link Kind} of the declaration.
    * @throws {@link IllegalArgumentException} if the provided declaration name
    *         is not a {@link Declaration} in the {@link #models}.
    */
   public Kind kind(final String declarationName) {
     final Map<String, Declaration> dm = declarationMap();
     checkArgument(dm.containsKey(declarationName));
     return SymbolTable.kindMap
         .get(dm.get(declarationName).getClass().getName());
   }
 
   /**
    * Retrieves the kind of a {@link Declaration}, if any.
    * 
    * @param declarationName
    *          The fully-qualified name ({@link Declaration#name}) of the
    *          {@link Declaration} to query.
    * @return the {@link Kind} of the declaration, if found in {@link #models}.
    */
   public Optional<Kind> kindOpt(final String declarationName) {
     final Map<String, Declaration> dm = declarationMap();
     return Optional.fromNullable(SymbolTable.kindMap.get(dm
         .get(declarationName).getClass().getName()));
   }
 
   /**
    * Retrieves all {@link RefinedType}s declared in the {@link #models}.
    * 
    * @return an immutable {@link List}.
    */
   public List<RefinedType> refinedTypes() {
     List<RefinedType> result = null;
     if ((this._refinedTypes == null)
         || ((result = this._refinedTypes.get()) == null)) {
       final ImmutableList.Builder<RefinedType> b = ImmutableList.builder();
 
       new AbstractVisitor() {
         @Override
         public boolean visitRefinedType(final RefinedType node) {
           b.add(node);
           return true;
         }
       }.visit(this.models);
       result = b.build();
       this._refinedTypes = new SoftReference<List<RefinedType>>(result);
     }
     return result;
   }
 
   /**
    * Retrieves a {@link Requirement} from its fully-qualified name (
    * {@link Requirement#name}).
    * 
    * @param name
    *          The fully-qualified name of the {@link Requirement}.
    * @return The {@link Requirement}.
    * @throws {@link IllegalArgumentException} if the provided name is not a
    *         {@link Requirement} in the {@link #models}.
    */
   public Requirement requirement(final String name) {
     final Map<String, Declaration> dm = declarationMap();
     checkArgument(dm.containsKey(name));
     final Declaration d = dm.get(name);
     checkArgument(d instanceof Requirement);
     return (Requirement) d;
   }
 
   /**
    * Retrieves a {@link Requirement} from its fully-qualified name (
    * {@link Requirement#name}), if any.
    * 
    * @param name
    *          The fully-qualified name to query.
    * @return The {@link Requirement}, if found in {@link #models}.
    */
   public Optional<Requirement> requirementOpt(final String name) {
     Optional<Requirement> result = Optional.absent();
     final Declaration d = declarationMap().get(name);
     if (d instanceof Requirement) {
       result = Optional.of((Requirement) d);
     }
     return result;
   }
 
   /**
    * Retrieves all {@link Requirement}s declared in the {@link #models}.
    * 
    * @return an immutable {@link List}.
    */
   public List<Requirement> requirements() {
     return declarations(Requirement.class);
   }
 
   /**
    * Retrieves a {@link Multimap} that relates a sub type's fully-qualified name
    * (either of {@link BasicType#name} or {@link Feature#name}) to its super
    * type's fully-qualified name (either of {@link BasicType#name} or
    * {@link Feature#name}).
    * 
    * @return an immutable {@link Multimap}.
    */
   public Multimap<String, String> subTransitiveMap() {
     Multimap<String, String> result = null;
     while ((this._subMap == null) || ((result = this._subMap.get()) == null)) {
       superTransitiveMap();
       result = this._subMap.get();
     }
     return result;
   }
 
   private void superTransitive(final Set<String> seen,
       final ImmutableMultimap.Builder<String, String> superm,
       final ImmutableMultimap.Builder<String, String> subm,
       final BasicType basicType) {
     final String basicTypeName = basicType.name;
     if (!seen.contains(basicTypeName)) {
       seen.add(basicTypeName);
       for (final NamedType nt : basicType.supers) {
         final String superName = nt.name;
         superTransitive(seen, superm, subm, feature(nt.name));
         superm.put(basicTypeName, superName);
         subm.put(superName, basicTypeName);
       }
     }
   }
 
   private void superTransitive(final Set<String> seen,
       final ImmutableMultimap.Builder<String, String> superm,
       final ImmutableMultimap.Builder<String, String> subm,
       final Feature feature) {
     final String featureName = feature.name;
     if (!seen.contains(featureName)) {
       seen.add(featureName);
       for (final NamedType nt : feature.supers) {
         final String superName = nt.name;
         superTransitive(seen, superm, subm, feature(nt.name));
         superm.put(featureName, superName);
         subm.put(superName, featureName);
       }
     }
   }
 
   /**
    * Retrieves a {@link Multimap} that relates a sub type's fully-qualified name
    * (either of {@link BasicType#name} or {@link Feature#name}) to its super
    * type's fully-qualified name (either of {@link BasicType#name} or
    * {@link Feature#name}).
    * 
    * @return an immutable {@link Multimap}.
    */
   public Multimap<String, String> superTransitiveMap() {
     Multimap<String, String> result = null;
     if ((this._superMap == null) || ((result = this._superMap.get()) == null)) {
       final ImmutableMultimap.Builder<String, String> superb = ImmutableMultimap
           .builder();
       final ImmutableMultimap.Builder<String, String> subb = ImmutableMultimap
           .builder();
 
       superb.put("IntegralType", "Number");
       superb.put("Int", "IntegralType");
       superb.put("Nat", "Int");
       subb.put("Number", "IntegralType");
       subb.put("IntegralType", "Int");
       subb.put("Int", "Nat");
 
       final HashSet<String> seen = new HashSet<>();
 
       for (final BasicType bt : basicTypes()) {
         superTransitive(seen, superb, subb, bt);
       }
 
       for (final Feature f : features()) {
         superTransitive(seen, superb, subb, f);
       }
 
       result = superb.build();
       this._superMap = new SoftReference<Multimap<String, String>>(result);
       this._subMap = new SoftReference<Multimap<String, String>>(subb.build());
     }
 
     return result;
   }
 
   /**
    * Returns the {@link String} representation of this symbol table.
    */
   @Override
   public String toString() {
     final String ms = this.models.toString();
     return "SymbolTable.of(" + ms.substring(1, ms.length() - 1) + ")";
   }
 }
