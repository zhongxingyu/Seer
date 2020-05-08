 /* Copyright (c) 2013, Dźmitry Laŭčuk
    All rights reserved.
 
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met: 
 
    1. Redistributions of source code must retain the above copyright notice, this
       list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
       this list of conditions and the following disclaimer in the documentation
       and/or other materials provided with the distribution.
 
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
 package afc.ant.modular;
 
 import java.util.AbstractSet;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * <p>An entity that represents a single module in a multi-module environment. Each
  * module is identified by its {@link #getPath() path} relative to the root directory
  * of this environment. In addition, each module has metadata associated with it.
  * This metadata consists of {@link #getDependencies() dependencies} and
  * {@link #getAttributes() attributes}.</p>
  * 
  * <p>The module dependencies is a set of modules which this module dependens upon.
  * Typically the dependee modules should be processed before this module can be
  * processed.</p>
  * 
  * <p>The module attributes are named pieces of data of free format. Attribute names
  * are case-sensitive. The {@code null} name is not allowed. An attribute value can be
  * any object or {@code null}.</p>
  * 
  * <p>Each {@code Module} is built from its prototype &ndash; a {@link ModuleInfo} object.
  * The dependency paths defined in the latter are converted into {@code Module} instances
  * and assigned to the module instance and the attributes are copied with no transformation.
  * {@code Module} has neither public nor protected constructors.
  * {@link ModuleRegistry#resolveModule(String)} is to be used to get an instance of
  * {@code Module} for a given path.</p>
  * 
  * <p>{@code Module} is technically not thread-safe. However, it can be used without
  * synchronisation from any thread once it is exposed to the client of the Ant Modular
  * library. Note that synchronising upon <em>attributes</em> is needed in some cases
  * (refer to {@link #getDependencies()} for more details).</p>
  * 
  * @author D&#378;mitry La&#365;&#269;uk
  */
 public final class Module
 {
     private final String path;
     
     /* A set of dependencies upon other modules is emulated via ArrayList which is memory-efficient
      * and has fast #add and #iterator#next operations. Uniqueness of modules is ensured by
      * implementation of ModuleRegistry and ModuleInfo. The latter does not allow the same path
      * to be added to the dependencies twice. Clients of Module see the module dependencies as a Set
      * object that guarantees uniqueness of its elements.
      * 
      * This field has the package-level access. This is used by dependency resolvers to avoid
      * overhead associated with iterators (virtual function call, longer dereference chain, etc.).
      */
     final ArrayList<Module> dependencies = new ArrayList<Module>();
     private final Set<Module> dependenciesView = Collections.unmodifiableSet(new ArrayListSet<Module>(dependencies));
     
     private final HashMap<String, Object> attributes = new HashMap<String, Object>();
     
     /* Synchronised outside, unmodifiable inside. Otherwise these is no way for the client
      * to synchronise for bulk operations on the same monitor that is used for single operations.
      */
     private final Map<String, Object> attributesView = Collections.synchronizedMap(
             Collections.unmodifiableMap(attributes));
     
     /**
     * <p>Creates a {@code Module} with a given path. The {@code Module} instance
      * created has neither dependencies nor attributes.</p>
      * 
      * @param path the module path. It is assumed to end with '/'.
      */
     Module(final String path)
     {
         if (path == null) {
             throw new NullPointerException("path");
         }
         this.path = path;
     }
     
     /**
      * <p>Returns the module path. It is a path relative to the root directory of
      * the environment that is associated with this {@code Module}.</p>
      * 
      * <p>This function is thread-safe.</p>
      * 
      * @return the module path. It is necessarily non-{@code null}.
      */
     public String getPath()
     {
         return path;
     }
     
     /**
      * <p>Assigns a given {@code Module} as a dependency. It is assumed that the {@code Module}
      * passed is not identical to this {@code Module} and is not contained already in
      * this module's dependencies. In addition it is expected to be non-{@code null}.</p>
      * 
      * <p>To preserve thread-safety, this operation cannot be used after
      * {@link ModuleRegistry#resolveModule(String)} has initialised this {@code Module}.</p>
      * 
      * @param dependency the dependee module to be assigned.
      */
     void addDependency(final Module dependency)
     {
         /* Add dependency is not public/protected. The package developer is responsible
            for passing valid dependencies. */
         assert dependency != null;
         assert dependency != this;
         /* No synchronisation is needed because dependencies are assigned before any thread
          * is started by CallTargetForModules in which the client sees this Module instance
          * (there is 'happens-before' relation) and because the client cannot modify
          * these dependencies.
          */
         assert !dependencies.contains(dependency);
         dependencies.add(dependency);
     }
     
     /**
      * <p>Returns a set of modules which this {@code Module} depends upon. The {@code Module}
      * objects returned are necessarily non-{@code null}. The set returned is unmodifiable.
      * In addition, any further modification of this module's dependencies is immediately
      * visible in the set returned.</p>
      * 
      * <p>The set returned is thread-safe. No synchronisation is needed for any operation
      * that is allowed by this set.</p>
      * 
      * @return a thread-safe and unmodifiable set of this module's dependee modules.
      */
     public Set<Module> getDependencies()
     {
         return dependenciesView;
     }
     
     /**
      * <p>Sets a given attribute to this {@code Module}. If the attribute with the given name
      * already exists then its value is replaced with the new value. This operation is
      * thread-safe and atomic. The new attribute becomes visible immediately via a set returned
      * by {@link #getAttributes()}.</p>
      * 
      * @param attributeName the name of the attribute. It must not be {@code null}.
      *      Attribute names are case-sensitive.
      * @param value the attribute value. It can be {@code null}.
      * 
      * @throws NullPointerException if <em>attributeName</em> is {@code null}.
      *      This {@code Module} instance is not modified in this case.
      */
     public void addAttribute(final String attributeName, final Object value)
     {
         if (attributeName == null) {
             throw new NullPointerException("attributeName");
         }
         /* Synchronising on attributesView so this modification is atomic
            for everyone who follows the contract of #getAttributes(). */
         synchronized (attributesView) {
             attributes.put(attributeName, value);
         }
     }
     
     /**
      * <p>Replaces the attributes of this {@code Module} with given attributes.
      * This operation is thread-safe and atomic. The new attributes become visible
      * immediately via a set returned by {@link #getAttributes()}.</p>
      * 
      * <p>The input map is not modified by this function and ownership over it is not
      * passed to this {@code Module}.</p>
      * 
      * @param attributes the new attributes to be assigned to this {@code Module}.
      *      This map must be non-{@code null}.
      * 
      * @throws NullPointerException if <em>attributes</em> is {@code null}.
      *      This {@code Module} instance is not modified in this case.
      */
     public void setAttributes(final Map<String, Object> attributes)
     {
         if (attributes == null) {
             throw new NullPointerException("attributes");
         }
         // Iteration is used instead of Map#containsKey because not all maps support null keys.
         for (final String attributeName : attributes.keySet()) {
             if (attributeName == null) {
                 throw new NullPointerException("attributes contains an attribute with null name.");
             }
         }
         /* Synchronising on attributesView so this modification is atomic
            for everyone who follows the contract of #getAttributes(). */
         synchronized (attributesView) {
             this.attributes.clear();
             this.attributes.putAll(attributes);
         }
     }
     
     /**
      * <p>Returns this {@code Module}'s attributes. The map returned is necessarily
      * non-{@code null} and unmodifiable. In addition, any further modification of this
      * {@code Module}'s attributes by means of the {@link #addAttribute(String, Object)}
      * and {@link #setAttributes(Map)} operations is immediately visible in the map returned.</p>
      * 
      * <p>The map returned is thread-safe. However it is necessary to synchronise on this map
      * to ensure that a bulk operation on the map is atomic. For instance:
      * <pre>
      * final Module module = getModule();
      * final Map&lt;String, Object&gt; attribs = module.getAttributes();
      * 
      * // It is necessary to synchronise on attribs, not module!
      * synchronized (attribs) {
      *     final Iterator&lt;String&gt; i = attribs.keySet().iterator();
      *     System.out.print("Attributes: ");
      *     while (i.hasNext()) {
      *         System.out.print(i.next());
      *         if (i.hasNext()) {
      *             System.out.print(", ");
      *         }
      *     }
      * }</pre>
      * Failure to follow this rule may lead to non-deterministic behaviour.</p>
      * 
      * @return a thread-safe and unmodifiable map of this {@code Module}'s attributes.
      */
     public Map<String, Object> getAttributes()
     {
         return attributesView;
     }
     
     /* An adaptor from ArrayList to Set. Lists without duplicate elements are supported only.
      * It must be used with an unmodifiable wrapper.
      */
     private static class ArrayListSet<T> extends AbstractSet<T>
     {
         private final ArrayList<T> list;
         
         public ArrayListSet(final ArrayList<T> list)
         {
             assert list != null;
             assert new HashSet<T>(list).size() == list.size();
             this.list = list;
         }
 
         @Override
         public Iterator<T> iterator()
         {
             return list.iterator();
         }
         
         @Override
         public int size()
         {
             return list.size();
         }
         
         @Override
         public boolean contains(final Object o)
         {
             return list.contains(o);
         }
     }
 }
