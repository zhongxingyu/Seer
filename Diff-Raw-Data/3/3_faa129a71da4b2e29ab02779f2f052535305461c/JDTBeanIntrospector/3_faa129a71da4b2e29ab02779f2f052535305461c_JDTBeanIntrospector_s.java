 /*******************************************************************************
  * Copyright (c) 2007 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 package org.eclipse.jst.jsf.common.util;
 
 import java.beans.Introspector;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.Flags;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeHierarchy;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.Signature;
 import org.eclipse.jst.jsf.common.JSFCommonPlugin;
 
 /**
  * A class that does bean introspection on a JDT IType
  * 
  * This functionality is not meant to replace runtime bean 
  * introspection.  Rather, it is meant to provide a 
  * more "lightweight" (in terms of class loading as well as
  * error handling of bean instantiation out of context) way
  * to determine a bean's properties at design time.
  * 
  * This class may not be sub-classed by clients.
  * 
  * @author cbateman
  *
  */
 public class JDTBeanIntrospector 
 {
 	private final static String GET_PREFIX = "get"; //$NON-NLS-1$
 	private final static String SET_PREFIX = "set"; //$NON-NLS-1$
 	private final static String IS_PREFIX = "is"; //$NON-NLS-1$
 	
 	private final IType 	_type;
 
 	/**
 	 * @param type
 	 */
 	public JDTBeanIntrospector(IType type)
 	{
 		_type = type;
 	}
 	
 	/**
 	 * @return an map of all properties with the property names
      * as keys and the values being JDTBeanProperty objects representing
      * the properties.
 	 */
 	public Map<String, JDTBeanProperty> getProperties()
 	{
 		final Map<String, JDTBeanProperty>   propertiesWorkingCopy = 
 		    new HashMap<String, JDTBeanProperty>();
 		final IMethod[] methods = getAllMethods();
 		
 		for (int i = 0; i < methods.length; i++)
 		{
 			final IMethod  method = methods[i];
 
 			try
 			{
 				processPropertyMethod(method, propertiesWorkingCopy);
 			}
 			catch (JavaModelException jme)
 			{
 				// log and then proceed to next method
 				JSFCommonPlugin.log(jme, "Error processing IMethod for bean property info"); //$NON-NLS-1$
 			}
 		}
 
         final Map properties = new HashMap();
         
         for (Entry<String, JDTBeanProperty> entry : propertiesWorkingCopy.entrySet())
         {
             final String key = entry.getKey();
             JDTBeanPropertyWorkingCopy  wcopy = (JDTBeanPropertyWorkingCopy) entry.getValue();
             properties.put(key, wcopy.toValueObject());
         }
 
 		return properties;
 	}
 
 	private void processPropertyMethod(IMethod method, Map<String, JDTBeanProperty> properties) throws JavaModelException
 	{
 		// to be a bean method, it must not a constructor, must be public
 		// and must not be static
 		if (!method.isConstructor()
				&& Flags.isPublic(method.getFlags())
 				&& !Flags.isStatic(method.getFlags()))
 		{
 			final String methodName = method.getElementName();
 			final String returnType = method.getReturnType();
 			
 			// either starts with get or is boolean and starts with is
 			
 			// is access must start with 'is', have a boolean return type and no parameters
 			final boolean  startsWithIs = methodName.startsWith(IS_PREFIX) 
 					&& Signature.SIG_BOOLEAN.equals(returnType)
 					&& method.getNumberOfParameters() == 0
                     && methodName.length() > IS_PREFIX.length();
 			
 			// get accessor must start with 'get', have no parameters and return non-void
 			final boolean  startsWithGet = (methodName.startsWith(GET_PREFIX)
 											&& method.getNumberOfParameters() == 0)
 											&& !Signature.SIG_VOID.equals(returnType)                    
                                             && methodName.length() > GET_PREFIX.length();
 			
 			// mutator must start with 'set' and have one parameter and a void return type
 			final boolean  startsWithSet = methodName.startsWith(SET_PREFIX)
 											&& method.getNumberOfParameters() == 1
 											&& Signature.SIG_VOID.equals(returnType)
                                             && methodName.length() > SET_PREFIX.length();
 
 			if (startsWithGet || startsWithSet || startsWithIs)
 			{
 				final String propertyName = 
 					Introspector.decapitalize(methodName.substring(startsWithIs ? 2 : 3));
 
 				JDTBeanPropertyWorkingCopy workingCopy = 
 					(JDTBeanPropertyWorkingCopy) properties.get(propertyName);
 
 				if (workingCopy == null)
 				{
 					workingCopy = new JDTBeanPropertyWorkingCopy(_type);
 					properties.put(propertyName, workingCopy);
 				}
 
 				if  (startsWithIs)
 				{
 					workingCopy.setIsGetter(method);
 				}
 				else if (startsWithGet)
 				{
 					workingCopy.setGetter(method);
 				}
 				else if (startsWithSet)
 				{
 					workingCopy.addSetter(method);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @return all methods for the type including inherited ones
 	 */
 	public IMethod[] getAllMethods()
 	{
 		IMethod[] methods = new IMethod[0];
 		
 		try
 		{
             // type not resolved so don't proceed
             if (_type != null)
             {
 	            // TODO: type hierarchy is potentially expensive, should
 	            // cache once and listen for changes
 	            ITypeHierarchy  hierarchy = _type.newSupertypeHierarchy(new NullProgressMonitor());
 	            
 				methods = getAllMethods(hierarchy, _type);
             }
 		}
 		catch(JavaModelException jme)
 		{
             JSFCommonPlugin.log(jme, "Error getting type information for bean"); //$NON-NLS-1$
 		}
 
 		return methods;
 	}
 
     /**
      * @param typeHierarchy
      * @param type
      * @return all methods of the type and it's super types
      */
     private static IMethod[] getAllMethods(final ITypeHierarchy typeHierarchy, final IType type)
     {
         final List<IMethod>   methods = new ArrayList<IMethod>();
         final IType[] superTypes = typeHierarchy.getAllSuperclasses(type);
         final IType[] closure = new IType[superTypes.length+1];
         closure[0] = type;
         System.arraycopy(superTypes, 0, closure, 1, superTypes.length);
         
         for (int i = 0; i < closure.length; i++)
         {
             try {
                 final IType superType = closure[i];
                 methods.addAll(Arrays.asList(superType.getMethods()));
             } catch (JavaModelException e) {
                 JSFCommonPlugin.log(e, "Error getting super type information for bean"); //$NON-NLS-1$
             }
         }
             
         return methods.toArray(new IMethod[methods.size()]);
     }
 }
