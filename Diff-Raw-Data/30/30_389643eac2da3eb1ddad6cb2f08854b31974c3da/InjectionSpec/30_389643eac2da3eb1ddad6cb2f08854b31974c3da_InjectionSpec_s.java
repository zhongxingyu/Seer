 package net.sf.javagimmicks.cdi;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.enterprise.context.spi.CreationalContext;
 import javax.enterprise.inject.UnsatisfiedResolutionException;
 import javax.enterprise.inject.spi.Bean;
 import javax.enterprise.inject.spi.BeanManager;
 import javax.inject.Named;
 import javax.inject.Qualifier;
 
 /**
  * Specifies a CDI bean injection and allows respective bean lookup from a given
  * {@link BeanManager} - allows easy building via {@link #build()} and
  * {@link #build(BeanManager)}.
  * 
  * @param <E>
  *           the type of beans to lookup
  */
 public class InjectionSpec<E>
 {
    private final Type _type;
    private final Set<Annotation> _annotations;
 
    private final String _name;
 
    /**
     * Creates a new {@link Builder} for building {@link InjectionSpec}s using
     * the given {@link BeanManager}.
     * 
     * @param beanManager
     *           the {@link BeanManager} to use for bean lookup
     * @return a respective {@link Builder} to specify the injection
     *         configuration
     * @see Builder
     */
    public static <E> Builder<E> build(final BeanManager beanManager)
    {
       return new Builder<E>(beanManager);
    }
 
    /**
     * Convenience method for {@link #build(BeanManager)} that will use the
     * {@link BeanManager} from {@link CDIContext#getBeanManager()}.
     * 
     * @return a respective {@link Builder} to specify the injection
     *         configuration
     * @see Builder
     * @see CDIContext#getBeanManager()
     */
    public static <E> Builder<E> build()
    {
       return build(null);
    }
 
    /**
     * Creates a new instance looking up a {@link Named} bean
     * 
     * @param name
     *           the name of the {@link Named} bean to lookup
     */
    public InjectionSpec(final String name)
    {
       if (name == null || name.length() == 0)
       {
          throw new IllegalArgumentException("Name must be specified!");
       }
 
       _name = name;
       _type = null;
       _annotations = null;
    }
 
    /**
     * Creates a new instance looking up bean by {@link Type} and the given
     * {@link Qualifier} {@link Annotation}s.
     * 
     * @param type
     *           the {@link Type} of the beans to lookup
     * @param annotations
     *           the {@link Qualifier} {@link Annotation}s the bean must have
     */
    public InjectionSpec(final Type type, final Collection<Annotation> annotations)
    {
       if (type == null)
       {
          throw new IllegalArgumentException("Type must be specified!");
       }
 
       _type = type;
       _annotations = annotations != null ? new HashSet<Annotation>(annotations) : Collections.<Annotation> emptySet();
 
       _name = null;
    }
 
    /**
     * Creates a new instance looking up bean by {@link Type} and the given
     * {@link Qualifier} {@link Annotation}s.
     * 
     * @param type
     *           the {@link Type} of the beans to lookup
     * @param annotations
     *           the {@link Qualifier} {@link Annotation}s the bean must have
     */
    public InjectionSpec(final Type type, final Annotation... annotations)
    {
       this(type, Arrays.asList(annotations));
    }
 
    /**
     * Creates a new instance looking up bean by an (optionally parameterized)
     * {@link Class}, the given parameter {@link Type}s and the given
     * {@link Qualifier} {@link Annotation}s.
     * 
     * @param clazz
     *           the {@link Class} of the beans to lookup
     * @param typeParameters
     *           the {@link List} of {@link Type} in case of a parameterized
     *           {@link Class}
     * @param annotations
     *           the {@link Qualifier} {@link Annotation}s the bean must have
     */
    public InjectionSpec(final Class<? extends E> clazz, final List<Type> typeParameters,
          final Collection<Annotation> annotations)
    {
       this(buildType(clazz, typeParameters), annotations);
    }
 
    /**
     * Creates a new instance looking up bean by {@link Class} and the given
     * {@link Qualifier} {@link Annotation}s.
     * 
     * @param clazz
     *           the {@link Class} of the beans to lookup
     * @param annotations
     *           the {@link Qualifier} {@link Annotation}s the bean must have
     */
    public InjectionSpec(final Class<E> clazz, final Collection<Annotation> annotations)
    {
       this(clazz, null, annotations);
    }
 
    /**
     * Creates a new instance looking up bean by {@link Class} and the given
     * {@link Qualifier} {@link Annotation}s.
     * 
     * @param clazz
     *           the {@link Class} of the beans to lookup
     * @param annotations
     *           the {@link Qualifier} {@link Annotation}s the bean must have
     */
    public InjectionSpec(final Class<E> clazz, final Annotation... annotations)
    {
       this(clazz, Arrays.asList(annotations));
    }
 
    /**
     * Checks if the current instance specifies a CDI injection on {@link Type}
     * basis.
     * <p>
     * As an alternative, beans can be looked up by name if they are
     * {@link Named} annotated
     * 
     * @return if the current instance specifies a CDI injection on {@link Type}
     *         basis
     * @see #isNameBased()
     */
    public boolean isTypeBased()
    {
       return _type != null;
    }
 
    /**
     * Checks if the current instance specifies a CDI injection on name basis
     * (i.e. looking up a {@link Named} annotated bean).
     * <p>
     * As an alternative, beans can be looked up on {@link Type} and
     * {@link Qualifier} basis.
     * 
     * @return if the current instance specifies a CDI injection on name basis
     * @see #isTypeBased()
     */
    public boolean isNameBased()
    {
       return _name != null;
    }
 
    /**
     * Returns the {@link Type} of beans to lookup or {@code null} if the lookup
     * is name based.
     * 
     * @return the {@link Type} of beans to lookup
     * @see #isTypeBased()
     * @see #isNameBased()
     */
    public Type getType()
    {
       return _type;
    }
 
    /**
     * Returns the {@link Set} of {@link Qualifier} {@link Annotation}s used for
     * lookup. This will be empty if the lookup is name based.
     * 
     * @return the {@link Set} of {@link Qualifier} {@link Annotation}s used for
     *         lookup
     * @see #isTypeBased()
     * @see #isNameBased()
     */
    public Set<Annotation> getAnnotations()
    {
       return Collections.unmodifiableSet(_annotations);
    }
 
    /**
     * Return the name to use for lookup or {@code null} if the lookup is
     * {@link Type} based.
     * 
     * @return the name to use for lookup
     * @see #isTypeBased()
     * @see #isNameBased()
     */
    public String getName()
    {
       return _name;
    }
 
    /**
     * Looks up a bean instance in the given {@link BeanManager} according to the
     * internal injection configuration information.
     * 
     * @param beanManager
     *           the {@link BeanManager} to use for lookup
     * @return the resulting looked up bean
     * @throws UnsatisfiedResolutionException
     *            if no (unique) bean could be looked up
     */
    @SuppressWarnings("unchecked")
    public E getInstance(final BeanManager beanManager)
    {
       // Type-based case
       if (isTypeBased())
       {
          final Bean<?> bean = beanManager
                .resolve(beanManager.getBeans(_type, _annotations.toArray(new Annotation[0])));
 
          if (bean == null)
          {
             throw new UnsatisfiedResolutionException("Unable to resolve a bean for " + _type + " with bindings "
                   + _annotations);
          }
 
          final CreationalContext<?> cc = beanManager.createCreationalContext(bean);
          return (E) beanManager.getReference(bean, _type, cc);
       }
 
       // Name-based case
       else if (isNameBased())
       {
          final Bean<?> bean = beanManager.resolve(beanManager.getBeans(_name));
          if (bean == null)
          {
            throw new UnsatisfiedResolutionException("Unable to resolve a bean name " + _name);
          }
          final CreationalContext<?> cc = beanManager.createCreationalContext(bean);
         return (E) beanManager.getReference(bean, bean.getBeanClass(), cc);
       }
       else
       {
          return null;
       }
    }
 
    private static Type buildType(final Class<?> clazz, final Collection<Type> typeParameters)
    {
       if (clazz == null)
       {
          throw new IllegalArgumentException("Class must be specified!");
       }
 
       if (typeParameters != null && !typeParameters.isEmpty())
       {
          return new ParameterizedType() {
 
             @Override
             public Type getRawType()
             {
                return clazz;
             }
 
             @Override
             public Type getOwnerType()
             {
                return null;
             }
 
             @Override
             public Type[] getActualTypeArguments()
             {
                return typeParameters.toArray(new Type[0]);
             }
          };
       }
       else
       {
          return clazz;
       }
    }
 
    /**
     * A builder class for creating {@link InjectionSpec} instances using a
     * fluent API.
     * 
     * @param <E>
     *           the type of beans to lookup
     */
    public static class Builder<E>
    {
       private final BeanManager _beanManager;
 
       private Type _type;
       private Class<? extends E> _class;
       private final List<Type> _typeParameters = new ArrayList<Type>();
       private final Set<Annotation> _annotations = new HashSet<Annotation>();
 
       private String _name;
 
       private Builder(final BeanManager beanManager)
       {
          _beanManager = beanManager;
       }
 
       /**
        * Registers a {@link Type} literal to be used for lookup.
        * 
        * @param type
        *           the {@link Type} to use for lookup
        * @return the {@link Builder} itself
        * @throws IllegalStateException
        *            if a {@link Class} was already registered via
        *            {@link #setClass(Class)} or {@link Type} parameter was
        *            already registered (e.g. via
        *            {@link #addTypeParameters(Collection)}) or a name was
        *            already registered via {@link #setName(String)}
        */
       public Builder<E> setType(final Type type)
       {
          checkHasName();
          checkHasClassOrTypeParameters();
 
          _type = type;
 
          return this;
       }
 
       /**
        * Registers a {@link Class} literal to be used for lookup.
        * 
        * @param clazz
        *           the {@link Class} to use for lookup
        * @return the {@link Builder} itself
        * @throws IllegalStateException
        *            if a {@link Type} was already registered via
        *            {@link #setType(Type)} or a name was already registered via
        *            {@link #setName(String)}
        */
       public Builder<E> setClass(final Class<? extends E> clazz)
       {
          checkHasName();
          checkHasType();
 
          _class = clazz;
 
          return this;
       }
 
       /**
        * Adds the given {@link Type} parameters to use for lookup - only makes
        * sense in combination with a parameterized {@link Class} registered via
        * {@link #setClass(Class)}.
        * 
        * @param typeParameters
        *           the {@link Type} parameters to use for the registered
        *           {@link Class}
        * @return the {@link Builder} itself
        * @throws IllegalStateException
        *            if a {@link Type} was already registered via
        *            {@link #setType(Type)} or a name was already registered via
        *            {@link #setName(String)}
        */
       public Builder<E> addTypeParameters(final Collection<Type> typeParameters)
       {
          checkHasName();
          checkHasType();
 
          _typeParameters.addAll(typeParameters);
 
          return this;
       }
 
       /**
        * Adds the given {@link Type} parameters to use for lookup - only makes
        * sense in combination with a parameterized {@link Class} registered via
        * {@link #setClass(Class)}.
        * 
        * @param typeParameters
        *           the {@link Type} parameters to use for the registered
        *           {@link Class}
        * @return the {@link Builder} itself
        * @throws IllegalStateException
        *            if a {@link Type} was already registered via
        *            {@link #setType(Type)} or a name was already registered via
        *            {@link #setName(String)}
        */
       public Builder<E> addTypeParameters(final Type... typeParameters)
       {
          checkHasName();
          checkHasType();
 
          return addTypeParameters(Arrays.asList(typeParameters));
       }
 
       /**
        * Adds the given {@link Qualifier} {@link Annotation}s to use for lookup
        * - only makes sense in combination with a provided {@link Type} (see
        * {@link #setType(Type)}) or {@link Class} (see {@link #setClass(Class)}
        * ).
        * 
        * @param annotations
        *           the {@link Qualifier} {@link Annotation}s to add
        * @return the {@link Builder} itself
        * @throws IllegalStateException
        *            if a name was already registered via
        *            {@link #setName(String)}
        */
       public Builder<E> addAnnotations(final Collection<Annotation> annotations)
       {
          checkHasName();
 
          _annotations.addAll(annotations);
 
          return this;
       }
 
       /**
        * Adds the given {@link Qualifier} {@link Annotation}s to use for lookup
        * - only makes sense in combination with a provided {@link Type} (see
        * {@link #setType(Type)}) or {@link Class} (see {@link #setClass(Class)}
        * ).
        * 
        * @param annotations
        *           the {@link Qualifier} {@link Annotation}s to add
        * @return the {@link Builder} itself
        * @throws IllegalStateException
        *            if a name was already registered via
        *            {@link #setName(String)}
        */
       public Builder<E> addAnnotations(final Annotation... annotations)
       {
          return addAnnotations(Arrays.asList(annotations));
       }
 
       /**
        * Allows registration of {@link Qualifier} {@link Annotation}s to use for
        * lookup via fluent API (i.e. an instance of {@link AnnotationBuilder}) -
        * only makes sense in combination with a provided {@link Type} (see
        * {@link #setType(Type)}) or {@link Class} (see {@link #setClass(Class)}
        * ).
        * 
        * @param annotationType
        *           the type of {@link Qualifier} {@link Annotation} to add for
        *           lookup usage
        * @return an {@link AnnotationBuilder} allowing to specify
        *         {@link Annotation} details via fluent API
        * @throws IllegalStateException
        *            if a name was already registered via
        *            {@link #setName(String)}
        * @see AnnotationBuilder
        */
       public <A extends Annotation> AnnotationBuilder<A> annotation(final Class<A> annotationType)
       {
          checkHasName();
 
          return new AnnotationBuilder<A>(AnnotationLiteralHelper.annotationWithMembers(annotationType));
       }
 
       /**
        * Registers a name used for lookup of a {@link Named} bean.
        * 
        * @param name
        *           the name to use for lookup
        * @return the {@link Builder} itself
        * @throws IllegalStateException
        *            if a {@link Class} was already registered via
        *            {@link #setClass(Class)} or {@link Type} parameter was
        *            already registered (e.g. via
        *            {@link #addTypeParameters(Collection)}) or {@link Type} was
        *            already registered via {@link #setType(Type)}
        */
       public Builder<E> setName(final String name)
       {
          checkHasClassOrTypeParameters();
          checkHasType();
 
          _name = name;
 
          return this;
       }
 
       /**
        * Finishes the building process and generates a respective
        * {@link InjectionSpec} object
        * 
        * @return an {@link InjectionSpec} the conforms to the specifications
        *         done on this builder
        * @throws IllegalStateException
        *            if no or insufficient specifications information were
        *            provided so far on this builder
        */
       public InjectionSpec<E> getInjection()
       {
          if (_name != null)
          {
             return new InjectionSpec<E>(_name);
          }
          else if (_class != null)
          {
             return new InjectionSpec<E>(_class, _typeParameters, _annotations);
          }
          else if (_type != null)
          {
             return new InjectionSpec<E>(_type, _annotations);
          }
          else
          {
             throw new IllegalStateException("Neither a name nor a type is set in this builder!");
          }
       }
 
       /**
        * Finishes the building process and looks up a respective bean instance
        * with in the internal {@link BeanManager}
        * 
        * @throws IllegalStateException
        *            if no or insufficient specifications information were
        *            provided so far on this builder
        */
       public E getInstance()
       {
          final BeanManager beanManager = _beanManager == null ? CDIContext.getBeanManager() : _beanManager;
          return getInjection().getInstance(beanManager);
       }
 
       private void checkHasName()
       {
          if (_name != null)
          {
             throw new IllegalStateException("There is already a name set on this builder!");
          }
       }
 
       private void checkHasClassOrTypeParameters()
       {
          if (_class != null || !_typeParameters.isEmpty())
          {
             throw new IllegalStateException(
                   "There is already a class set or type parameters specified on this builder!");
          }
       }
 
       private void checkHasType()
       {
          if (_type != null)
          {
             throw new IllegalStateException("There is already a type set on this builder!");
          }
       }
 
       /**
        * A builder to specify additional information for {@link Qualifier}
        * {@link Annotation} to use for bean lookup via fluent API.
        * 
        * @param <A>
        *           the type of {@link Annotation} to specify
        */
       public class AnnotationBuilder<A extends Annotation>
       {
          private final AnnotationLiteralHelper.Builder<A> _delegate;
 
          private AnnotationBuilder(final AnnotationLiteralHelper.Builder<A> delegate)
          {
             this._delegate = delegate;
          }
 
          /**
           * Specifies the value for a member of the given {@link Annotation}
           * 
           * @param memberName
           *           the member name
           * @param value
           *           the value for the given member
           * @return the {@link AnnotationBuilder} itself
           * @throws IllegalArgumentException
           *            if the given member name does not match a member of the
           *            underlying {@link Annotation}
           */
          public AnnotationBuilder<A> member(final String memberName, final Object value)
          {
             _delegate.member(memberName, value);
 
             return this;
          }
 
          /**
           * Finishes the building process
           * 
           * @return the {@link Builder} that originally created this
           *         {@link AnnotationBuilder}
           */
          public Builder<E> add()
          {
             addAnnotations(_delegate.get());
 
             return Builder.this;
          }
       }
    }
 }
