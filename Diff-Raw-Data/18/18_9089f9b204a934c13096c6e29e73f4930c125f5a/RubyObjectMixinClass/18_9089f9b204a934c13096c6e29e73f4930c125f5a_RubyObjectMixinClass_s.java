 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.parser.mixin;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class RubyObjectMixinClass extends RubyMixinClass {
 
 	public RubyObjectMixinClass(RubyMixinModel model, boolean meta) {
 		super(model, "Object" + (meta?"":RubyMixin.INSTANCE_SUFFIX), false); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	public RubyMixinMethod[] findMethods(String prefix, boolean includeTopLevel) {
 		List mixinResult = new ArrayList ();
 		
 		
 		if (includeTopLevel && prefix.length() > 0) {
 			String[] keys = model.getRawModel().findKeys(prefix + "*"); //$NON-NLS-1$
 			for (int i = 0; i < keys.length; i++) {
 				IRubyMixinElement createdRubyElement = model.createRubyElement(keys[i]);
 				if (createdRubyElement instanceof RubyMixinMethod)
 					mixinResult.add(createdRubyElement);
 			}		
 		}
 		
 		
 		RubyMixinMethod[] methods = super.findMethods(prefix, false);
 		mixinResult.addAll(Arrays.asList(methods));
 		
 //			HashMap result = new HashMap();
 //
 //			IMethod[] topLevelMethods = RubyModelUtils.findTopLevelMethods(
 //					null, prefix + "*");
 //			for (int i = 0; i < topLevelMethods.length; i++) {
 //				String name = topLevelMethods[i].getElementName();
 //				List l = (List) result.get(name);
 //				if (l == null) {
 //					l = new ArrayList();
 //					result.put(name, l);
 //				}
 //				l.add(topLevelMethods[i]);
 //			}
 //
 //			for (Iterator iterator = result.keySet().iterator(); iterator
 //					.hasNext();) {
 //				String name = (String) iterator.next();
 //				List l = (List) result.get(name);
 //				IMethod[] m = (IMethod[]) l.toArray(new IMethod[l.size()]);
 //				mixinResult.add(new RubyMixinMethod(model, getKey()
 //						+ MixinModel.SEPARATOR + name, m));
 //			}
 		
 			
 		return (RubyMixinMethod[]) mixinResult.toArray(new RubyMixinMethod[mixinResult.size()]);
 	}
 
 	public RubyMixinVariable[] getFields() {
 		return new RubyMixinVariable[0];
 	}
 
 	public RubyMixinMethod getMethod(String name) {
 		RubyMixinMethod method = super.getMethod(name);
 		if (method != null)
 			return method;
 		IRubyMixinElement element = model.createRubyElement(name); //check top-level methods
 		if (element instanceof RubyMixinMethod)
 			return (RubyMixinMethod) element;
 		return null;
 	}
 
	public RubyMixinClass getSuperclass() {
	  return null;
	}

 }
