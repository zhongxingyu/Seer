 /**
  * <copyright>
  * 
  * Copyright (c) 2010-2012 Thales Global Services S.A.S.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Thales Global Services S.A.S. - initial API and implementation
  * 
  * </copyright>
  */
 package org.eclipse.emf.diffmerge.ui;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.diffmerge.ui.actions.ComparisonSetup;
 import org.eclipse.emf.diffmerge.ui.actions.EMFDiffMergeEditorInput;
 import org.eclipse.emf.diffmerge.ui.specification.IComparisonSpecificationFactory;
 import org.eclipse.emf.diffmerge.ui.specification.IOverridableFactory;
 import org.eclipse.emf.diffmerge.ui.specification.IScopeSpecification;
 import org.eclipse.emf.diffmerge.ui.specification.IScopeSpecificationFactory;
 
 
 
 /**
  * A manager for model comparison contexts that are applicable via the GUI.
  * @author Olivier Constant
  */
 public class ComparisonContextManager {
   
   /** The ModelComparisonContext extension point */
   private static final String MODEL_COMPARISON_CONTEXT_EXTENSION_POINT =
     "org.eclipse.emf.diffmerge.ui.modelComparisonContext"; //$NON-NLS-1$
   
   /** The ModelComparisonContext extension point property for configurations */
   private static final String EXTENSION_POINT_PROPERTY_CONFIGURATION = "specificationFactory"; //$NON-NLS-1$
   
   /** The ModelComparisonContext extension point property for scopes */
   private static final String EXTENSION_POINT_PROPERTY_SCOPE = "scopeFactory"; //$NON-NLS-1$
   
   /** The registered comparison specification factories (initially null) */
   private Map<Class<?>, IComparisonSpecificationFactory> _comparisonFactories;
   
   /** The registered scope factories (initially null) */
   private Map<Class<?>, IScopeSpecificationFactory> _scopeFactories;
   
   
   /**
    * Constructor
    */
   public ComparisonContextManager() {
     _comparisonFactories = null;
     _scopeFactories = null;
   }
   
   /**
    * Return a default editor input for the given entry points
    * @param entrypoint1_p a non-null object
    * @param entrypoint2_p a non-null object
    * @param entrypoint3_p an optional object
    * @return a potentially null object (null means failure)
    */
   public EMFDiffMergeEditorInput createDefaultEditorInput(Object entrypoint1_p,
       Object entrypoint2_p, Object entrypoint3_p) {
     EMFDiffMergeEditorInput result = null;
     ComparisonSetup selection =
      createSpecificationSelection(entrypoint1_p, entrypoint2_p, entrypoint3_p);
     if (selection != null && selection.getComparisonSpecification() != null)
       result = new EMFDiffMergeEditorInput(selection.getComparisonSpecification());
     return result;
   }
   
   /**
   * Return a specification selection object for the given entry points
    * @param entrypoint1_p a non-null object
    * @param entrypoint2_p a non-null object
    * @param entrypoint3_p an optional object
    * @return a potentially null object (null means failure)
    */
  public ComparisonSetup createSpecificationSelection(Object entrypoint1_p,
       Object entrypoint2_p, Object entrypoint3_p) {
     ComparisonSetup result = null;
     List<IScopeSpecificationFactory> factories1 =
       getApplicableScopeFactories(entrypoint1_p);
     List<IScopeSpecificationFactory> factories2 =
       getApplicableScopeFactories(entrypoint2_p);
     List<IScopeSpecificationFactory> factories3 =
       entrypoint3_p == null? Collections.<IScopeSpecificationFactory>emptyList():
         getApplicableScopeFactories(entrypoint3_p);
     if (!factories1.isEmpty() && !factories2.isEmpty()) {
       IScopeSpecification scopeSpec1 =
         factories1.get(0).createScopeSpecification(entrypoint1_p, null, true);
       IScopeSpecification scopeSpec2 =
         factories2.get(0).createScopeSpecification(entrypoint2_p, null, true);
       IScopeSpecification scopeSpec3 = factories3.isEmpty()? null:
         factories3.get(0).createScopeSpecification(entrypoint3_p, null, true);
       List<IComparisonSpecificationFactory> cFactories =
         getApplicableComparisonFactories(scopeSpec1, scopeSpec2, scopeSpec3);
       if (!cFactories.isEmpty())
         result = new ComparisonSetup(scopeSpec1, scopeSpec2, scopeSpec3, cFactories);
     }
     return result;
   }
   
   /**
    * Discover the specification factories which are registered through the dedicated
    * extension point, if any
    * Postcondition: _comparisonFactories != null && _scopeFactories != null
    */
   private void discoverRegisteredComparisonContexts() {
     _comparisonFactories = new HashMap<Class<?>, IComparisonSpecificationFactory>();
     _scopeFactories = new HashMap<Class<?>, IScopeSpecificationFactory>();
     IExtensionRegistry registry = Platform.getExtensionRegistry();
     IConfigurationElement[] config = registry.getConfigurationElementsFor(
         MODEL_COMPARISON_CONTEXT_EXTENSION_POINT);
     for (IConfigurationElement e : config) {
       try {
         Object o = e.createExecutableExtension(EXTENSION_POINT_PROPERTY_CONFIGURATION);
         if (o instanceof IComparisonSpecificationFactory)
           _comparisonFactories.put(o.getClass(), (IComparisonSpecificationFactory)o);
       } catch (CoreException ex) {
         // Proceed
       }
       try {
         Object o = e.createExecutableExtension(EXTENSION_POINT_PROPERTY_SCOPE);
         if (o instanceof IScopeSpecificationFactory)
           _scopeFactories.put(o.getClass(), (IScopeSpecificationFactory)o);
       } catch (CoreException ex) {
         // Proceed
       }
     }
   }
   
   /**
    * Return the set of comparison specification factories that are applicable to the given
    * entry point
    * @param leftScopeSpec_p a non-null scope specification
    * @param rightScopeSpec_p a non-null scope specification
    * @param ancestorScopeSpec_p an optional scope specification
    * @return a non-null, potentially empty, unmodifiable list
    */
   public List<IComparisonSpecificationFactory> getApplicableComparisonFactories(
       IScopeSpecification leftScopeSpec_p, IScopeSpecification rightScopeSpec_p,
       IScopeSpecification ancestorScopeSpec_p) {
     List<IComparisonSpecificationFactory> result = new ArrayList<IComparisonSpecificationFactory>();
     for (IComparisonSpecificationFactory factory : getRegisteredComparisonFactories()) {
       if (factory.isApplicableTo(leftScopeSpec_p, rightScopeSpec_p, ancestorScopeSpec_p))
         result.add(factory);
     }
     result = reduceByOverride(result, _comparisonFactories);
     return result;
   }
   
   /**
    * Return the set of scope specification factories that are applicable to the given
    * entry point
    * @param entrypoint_p a non-null object
    * @return a non-null, unmodifiable list which cannot be empty if isValidEntrypoint(entrypoint_p)
    */
   public List<IScopeSpecificationFactory> getApplicableScopeFactories(
       Object entrypoint_p) {
     List<IScopeSpecificationFactory> result = new ArrayList<IScopeSpecificationFactory>();
     for (IScopeSpecificationFactory factory : getRegisteredScopeFactories()) {
       if (factory.isApplicableTo(entrypoint_p))
         result.add(factory);
     }
     result = reduceByOverride(result, _scopeFactories);
     return Collections.unmodifiableList(result);
   }
   
   /**
    * Return the comparison specification factories which are registered through the dedicated
    * extension point, if any
    * @return a non-null, potentially empty list
    */
   protected final Collection<IComparisonSpecificationFactory> getRegisteredComparisonFactories() {
     if (_comparisonFactories == null)
       discoverRegisteredComparisonContexts();
     return _comparisonFactories.values();
   }
   
   /**
    * Return the configuration factories which are registered through the dedicated
    * extension point, if any
    * @return a non-null, potentially empty list
    */
   protected final Collection<IScopeSpecificationFactory> getRegisteredScopeFactories() {
     if (_scopeFactories == null)
       discoverRegisteredComparisonContexts();
     return _scopeFactories.values();
   }
   
   /**
    * Return whether the given object is a valid entry point for a comparison, i.e.,
    * whether a model scope can be derived from it
    * @param entrypoint_p a non-null object
    */
   public boolean isValidEntrypoint(Object entrypoint_p) {
     for (IScopeSpecificationFactory factory : getRegisteredScopeFactories()) {
       if (factory.isApplicableTo(entrypoint_p))
         return true;
     }
     return false;
   }
   
   /**
    * Return the reduced version of the given list of factories based on the "override" relation
    * @param factories_p a non-null, potentially empty list
    * @param configurationFactories_p a map from factory classes to the corresponding registered instances
    * @return a non-null, unmodifiable list which is not empty if factories_p is not empty
    */
   protected <T extends IOverridableFactory> List<T> reduceByOverride(List<T> factories_p,
       Map<Class<?>, T> configurationFactories_p) {
     List<T> result = new ArrayList<T>(factories_p);
     for (T factory : factories_p) {
       for (Class<?> factoryClass : factory.getOverridenClasses()) {
         result.remove(configurationFactories_p.get(factoryClass));
       }
     }
     return Collections.unmodifiableList(result);
   }
   
 }
