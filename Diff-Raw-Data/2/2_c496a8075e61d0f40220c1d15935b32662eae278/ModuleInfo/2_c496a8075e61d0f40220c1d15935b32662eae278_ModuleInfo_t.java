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
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class ModuleInfo
 {
     private final String path;
     private final HashSet<String> dependencies = new HashSet<String>();
     private final Set<String> dependenciesView = Collections.unmodifiableSet(dependencies);
     private final HashMap<String, Object> attributes = new HashMap<String, Object>();
     private final Map<String, Object> attributesView = Collections.unmodifiableMap(attributes);
     
     public ModuleInfo(final String path)
     {
         if (path == null) {
             throw new NullPointerException("path");
         }
         this.path = path;
     }
     
     public String getPath()
     {
         return path;
     }
     
     public void addDependency(final String dependency)
     {
         if (dependency == null) {
             throw new NullPointerException("dependency");
         }
         if (dependency.equals(path)) {
             throw new IllegalArgumentException("Cannot add itself as a dependency.");
         }
         dependencies.add(dependency);
     }
     
     /**
      * <p>Replaces the dependencies of this {@code ModuleInfo} with given module paths.
      * The new dependencies become visible immediately via a set returned by
      * <tt>{@link #getDependencies()}</tt>.</p>
      * 
      * <p>The input collection is not modified by this function and ownership over it is not
      * passed to this {@code ModuleInfo}.</p>
      * 
      * @param dependencies module paths that this {@code ModuleInfo} is to depend upon.
      *      This collection and all its elements are to be non-{@code null}. This collection must not
      *      contain this {@code ModuleInfo}'s path.
      * 
      * @throws NullPointerException if <i>dependencies</i> or any its element is {@code null}.
      *      This {@code ModuleInfo} instance is not modified in this case.
      * @throws IllegalArgumentException if <i>dependencies</i> contains this {@code ModuleInfo}'s path.
      *      This {@code ModuleInfo} instance is not modified in this case.
      */
     public void setDependencies(final Collection<String> dependencies)
     {
         if (dependencies == null) {
             throw new NullPointerException("dependencies");
         }
         // Iteration is used instead of Collection#contains because not all collections support null elements.
         for (final String dependency : dependencies) {
             if (dependency == null) {
                 throw new NullPointerException("dependencies contains null dependency.");
             }
             if (dependency.equals(path)) {
                 throw new IllegalArgumentException("Cannot add itself as a dependency.");
             }
         }
         this.dependencies.clear();
         this.dependencies.addAll(dependencies);
     }
     
     /**
      * <p>Returns a set of module paths which this module depends upon. The paths returned
      * returned are necessarily non-{@code null}. The set returned is unmodifiable.
      * In addition, any further modification of this {@code ModuleInfo}'s dependencies by means of
      * the <tt>{@link #addDependency(String)}</tt> and <tt>{@link #setDependencies(Collection)}</tt>
      * operations is immediately visible in the set returned.</p>
      * 
      * @return an unmodifiable set of this module's dependency modules.
      */
     public Set<String> getDependencies()
     {
         return dependenciesView;
     }
     
     public void addAttribute(final String attributeName, final Object value)
     {
         if (attributeName == null) {
             throw new NullPointerException("attributeName");
         }
         attributes.put(attributeName, value);
     }
     
     /**
      * <p>Replaces the attributes of this {@code ModuleInfo} with given attributes.
      * The new attributes become visible immediately via a set returned by
      * <tt>{@link #getAttributes()}</tt>.</p>
      * 
      * <p>The input map is not modified by this function and ownership over it is not
      * passed to this {@code ModuleInfo}.</p>
      * 
     * @param attributes the new attributes to be assigned this {@code ModuleInfo}.
      *      This map must be non-{@code null}.
      * 
      * @throws NullPointerException if <i>attributes</i> is {@code null}.
      *      This {@code ModuleInfo} instance is not modified in this case.
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
         this.attributes.clear();
         this.attributes.putAll(attributes);
     }
     
     /**
      * <p>Returns this module's attributes. The map returned is necessarily non-{@code null} and unmodifiable.
      * In addition, any further modification of this {@code ModuleInfo}'s attributes by means of
      * the <tt>{@link #addAttribute(String, Object)}</tt> and <tt>{@link #setAttributes(Map)}</tt>
      * operations is immediately visible in the map returned.</p>
      * 
      * @return an unmodifiable map of this module's attributes.
      */
     public Map<String, Object> getAttributes()
     {
         return attributesView;
     }
 }
