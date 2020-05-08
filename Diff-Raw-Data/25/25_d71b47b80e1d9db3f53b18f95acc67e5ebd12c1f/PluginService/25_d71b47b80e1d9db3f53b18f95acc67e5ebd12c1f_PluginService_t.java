 /*******************************************************************************
 * Copyright (c) 2008, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.codegen.services;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.eef.components.PropertiesEditionComponent;
 import org.eclipse.emf.eef.components.PropertiesEditionContext;
 import org.eclipse.emf.eef.mapping.ElementBindingReference;
 import org.eclipse.emf.eef.views.View;
 
 /**
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  * @since 1.1
  */
 public class PluginService {
 
 	public List<List<EObject>> getPluginTabs(EObject arg0) {
 		List<String> keys = new ArrayList<String>();
 		List<List<EObject>> res = new ArrayList<List<EObject>>();
 		PropertiesEditionContext editionContext = (PropertiesEditionContext)arg0;
 		for (Iterator<EObject> iterator = editionContext.eAllContents(); iterator.hasNext();) {
 			EObject o = (EObject)iterator.next();
 			if (o instanceof PropertiesEditionComponent) {
 				PropertiesEditionComponent pec = (PropertiesEditionComponent)o;
 				for (View view : getInvolvedViews(pec)) {
 					String key = getViewKey(view, pec);
 					if (!keys.contains(key)) {
 						List<EObject> entry = new ArrayList<EObject>();
 						entry.add(view);
 						entry.add(pec);
 						res.add(entry);
 						keys.add(key);
 					}
 				}
 			}
 		}
 		return res;
 	}
 
 	private Set<View> getInvolvedViews(PropertiesEditionComponent pec) {
 		Set<View> res = new LinkedHashSet<View>();
 		res.addAll(pec.getViews());
 		for (ElementBindingReference rb : pec.getReferencedBinding()) {
 			res.addAll(rb.getBinding().getViews());
 		}
 		return res;
 	}
 
 	private String getViewKey(View view, PropertiesEditionComponent pec) {
 		if (view.getName().equals(pec.getName())) {
 			// or view.eInverse()->first().oclIsTypeOf(views::ViewReference)) // TODO recode condition
 			return "Base";
 		} else {
 			return view.getName();
 		}
 	}
 
 }
