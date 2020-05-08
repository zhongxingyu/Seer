 /*******************************************************************************
  * Copyright (c) 2001, 2008 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.designtime.resolver;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jst.jsf.context.IModelContext;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContext;
 import org.eclipse.jst.jsf.context.symbol.IMethodSymbol;
 import org.eclipse.jst.jsf.context.symbol.IObjectSymbol;
 import org.eclipse.jst.jsf.context.symbol.ISymbol;
 import org.eclipse.jst.jsf.context.symbol.SymbolFactory;
 
 /**
  * A symbol resolver that delegates to the default SymbolContextResolver, but
  * caches the results and returns those on subsequent calls.
  * 
  * WARNING: this resolver is suitable *only* in situations where the state
  * of dependent symbol sources won't change between calls.  This resolver makes
  * no attempt to ensure that cached data is in sync with changes to symbols
  * such addition, modification or removal of Java class underlying beans.
  * 
  * NOTE: this resolver is experimental and should NOT be considered API
  * 
  * Clients should not use this resolver directly.  Access it through the factory instead
  * @author cbateman
  *
  */
 public final class CachingSymbolContextResolver extends AbstractSymbolContextResolver
 {
     private final static ISymbol SYMBOL_NOT_FOUND = SymbolFactory.eINSTANCE.createIComponentSymbol();
     private final static IMethodSymbol METHOD_SYMBOL_NOT_FOUND =
         SymbolFactory.eINSTANCE.createIMethodSymbol();
     
    private final SymbolContextResolver     _delegate;
 
     private final Map<String, ISymbol>  _variablesByName = new HashMap<String, ISymbol>();
     private ISymbol[]                   _allVariables;
 
     private final Map<ISymbol, Map<Object, ISymbol>>    _propertiesByOwner =
         new HashMap<ISymbol, Map<Object,ISymbol>>();
     private ISymbol[]                   _allProperties;
 
     private final Map<IObjectSymbol, Map<Object, IMethodSymbol>> _methodsByOwner =
         new HashMap<IObjectSymbol, Map<Object,IMethodSymbol>>();
     private ISymbol[]             _allMethods;
     
     /**
      * @param context
      */
     public CachingSymbolContextResolver(final IStructuredDocumentContext context)
     {
         _delegate = new SymbolContextResolver(context);
     }
 
     @Override
     public boolean canResolveContext(final IModelContext modelContext)
     {
         return _delegate.canResolveContext(modelContext);
     }
 
     @Override
     public ISymbol[] getAllVariables()
     {
         if (_allVariables == null)
         {
             _allVariables = _delegate.getAllVariables();
         }
         return _allVariables;
     }
 
     @Override
     public IMethodSymbol getMethod(final IObjectSymbol base, final Object methodName)
     {
         Map<Object, IMethodSymbol> methods = _methodsByOwner.get(base);
         
         if (methods == null)
         {
             methods = new HashMap<Object, IMethodSymbol>();
             _methodsByOwner.put(base, methods);
         }
 
         IMethodSymbol method = methods.get(methodName);
 
         if (method == SYMBOL_NOT_FOUND)
         {
             method = null;
         }
         else
         {
             if (method == null)
             {
                 method = _delegate.getMethod(base, methodName);
                 
                 if (method == null)
                 {
                     // if the delegate couldn't find the property,
                     // then mark this in case it is requested again
                     methods.put(methodName, METHOD_SYMBOL_NOT_FOUND);
                 }
                 else
                 {
                     methods.put(methodName, method);
                 }
             }
         }
         return method;
     }
 
     @Override
     public ISymbol[] getMethods(final IObjectSymbol base)
     {
         if (_allMethods == null)
         {
             _allMethods = _delegate.getMethods(base);
         }
         return _allMethods;
     }
 
     @Override
     public ISymbol[] getProperties(final ISymbol symbol)
     {
         if (_allProperties == null)
         {
             _allProperties = _delegate.getProperties(symbol);
         }
         return _allProperties;
     }
 
     @Override
     public ISymbol getProperty(final ISymbol symbol, final Object propertyName)
     {
         Map<Object, ISymbol> properties = _propertiesByOwner.get(symbol);
         
         if (properties == null)
         {
             properties = new HashMap<Object, ISymbol>();
             _propertiesByOwner.put(symbol, properties);
         }
 
         ISymbol property = properties.get(propertyName);
 
         if (property == SYMBOL_NOT_FOUND)
         {
             property = null;
         }
         else
         {
             if (property == null)
             {
                 property = _delegate.getProperty(symbol, propertyName);
                 
                 if (property == null)
                 {
                     // if the delegate couldn't find the property,
                     // then mark this in case it is requested again
                     properties.put(propertyName, SYMBOL_NOT_FOUND);
                 }
                 else
                 {
                     properties.put(propertyName, property);
                 }
             }
         }
         return property;
     }
 
     @Override
     public ISymbol getVariable(final String name)
     {
         ISymbol variable = _variablesByName.get(name);
 
         // if the symbol was not found, return null but avoid calling the
         // delegate again
         if (variable == SYMBOL_NOT_FOUND)
         {
             variable = null;
         }
         else
         {
             if (variable == null)
             {
                 variable = _delegate.getVariable(name);
                 
                 if (variable == null)
                 {
                     // if the delegate couldn't find the variable,
                     // then mark this in case it is requested again
                     _variablesByName.put(name, SYMBOL_NOT_FOUND);
                 }
                 else
                 {
                     _variablesByName.put(name, variable);
                 }
             }
         }
         return variable;
     }
 
     @Override
     public boolean hasSameResolution(IModelContext modelContext)
     {
         return _delegate.hasSameResolution(modelContext);
     }
 }
