 /*******************************************************************************
  * Copyright (c) 2001, 2007 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.common.util;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jst.jsf.common.JSFCommonPlugin;
 
 /**
  * A writable version of the JDTBeanProperty object
  * 
 * This class may not be sub-classed by clients
 * 
  * @author cbateman
  *
  */
 public class JDTBeanPropertyWorkingCopy extends JDTBeanProperty 
 {
 	private final List		_setters;
 	
 	/**
 	 * the IMethod for the boolean "is" accessor method
 	 */
 	private IMethod        _isGetter;
 	
 	/**
 	 * Constructor
 	 * @param type 
 	 */
 	public JDTBeanPropertyWorkingCopy(IType type)
 	{
         super(type);
 		_setters = new ArrayList();
 	}
 	
 	/**
 	 * @return the bean properties spawned from this working copy
 	 * Normally, there is only one property in the array, however,
 	 * since this working copy represents all properties with the same
 	 * name, there could be multiple properties since setters can
 	 * be overloaded by name and could result in zero or one readable
 	 * properties plus zero or more write-only properties with the same
 	 * name.  I can't see anywhere in the spec that covers this 
 	 * boundary case
 	 */
 	public JDTBeanProperty toValueObject()
 	{
 		// if the isGetter is present that it takes precedence
 		// over the the normal getter
 		IMethod  getter = getIsGetter() != null ? 
 							getIsGetter() : getGetter();
 		IMethod  matchedSetter = null;
 
 		if (getter != null)
 		{
 			matchedSetter = determineMatchedSetter(getter);
 		}
 		// if there's no getter than pick any setter: there
 		// are bigger problem when there's no getter than
 		// ambiguous setters
 		else if (_setters.size() > 0)
 		{
 			matchedSetter = (IMethod) _setters.get(0);
 		}
 		
 		JDTBeanProperty beanProp = new JDTBeanProperty(_type);
 		beanProp.setGetter(getter);
 		beanProp.setSetter(matchedSetter);
 		return beanProp;
 	}
 	
 	private IMethod determineMatchedSetter(IMethod getter)
 	{
 		IMethod matchedSetter = null;
 		
 		try
 		{
 			final String getterSig = 
 				TypeUtil.resolveTypeSignature(_type, getter.getReturnType());
 
 			FIND_MATCHING_SETTER:for 
 				(final Iterator it = _setters.iterator(); it.hasNext();)
 			{
 				final IMethod  setter = (IMethod) it.next();
 				if (setter.getNumberOfParameters() == 1)
 				{
 					final String paramSig = 
 						TypeUtil.resolveTypeSignature
 							(_type,setter.getParameterTypes()[0]);
 					
 					if (paramSig.equals(getterSig))
 					{
 						// we've found our match since only one
 						// setter with the same name as the getter
 						// can have the same matching type for a
 						// single arg method
 						matchedSetter = setter;
 						break FIND_MATCHING_SETTER;
 					}
 				}
 			}
 		}
 		catch (JavaModelException jme)
 		{
             JSFCommonPlugin.log(jme, "Error determining getter return type, bean properties analysis may be inaccurate"); //$NON-NLS-1$
 		}
 
 		return matchedSetter;
 	}
 	
 	//@Override
 	public void setGetter(IMethod getter) {
 		super.setGetter(getter);
 	}
 
 	/**
 	 * @param isGetter
 	 */
 	public void setIsGetter(IMethod isGetter) {
 		_isGetter = isGetter;
 	}
 
 	/**
 	 * @param setter
 	 */
 	public void addSetter(IMethod setter) {
         if (setter != null
                 && setter.getNumberOfParameters() == 1)
         {
             _setters.add(setter);
         }
 	}
 
     /**
      * Not supported on working copy.  This is synthetically generated
      * on toValueObject()
      * @return nothing; throws exception
      */
     public final IMethod getSetter()
     {
         throw new UnsupportedOperationException("Setter not calculated in working copy.  Call toValueObject().getSetter()"); //$NON-NLS-1$
     }
     
 	/**
 	 * @return the "is" getter method or null if not found
 	 */
 	public IMethod getIsGetter() {
 		return _isGetter;
 	}
 }
