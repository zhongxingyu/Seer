 package org.eclipse.stem.core;
 
 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.impl.BasicEObjectImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.XMLHelper;
 import org.eclipse.emf.ecore.xmi.impl.XMIHelperImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 import org.eclipse.stem.core.common.Identifiable;
 
 /**
  * This class is the {@link XMIResourceFactoryImpl} for the STEM project. It's
  * purpose is to provide an implementation of {@link XMIResourceImpl} whose
  * {@link XMLHelper} has a customized implementation of
  * {@link XMLHelper#getHREF(EObject)} that filters serialized {@link URI}'s such
  * that they remain platform relative (i.e.,
  * "platform:/projectname/foo/bar..."), or if absolute, are deresolved against
  * the {@link Resource}'s {@link URI} so they remain internally relative to the
  * containing file.
  * <p>
  * This is important as the serialized {@link Identifiable}s need to be able to
  * be copied, moved and exported. If their references to other serialized
  * {@link Identifiable}s were relative to file location rather than to the
  * platform, such operations would break the links. Thus, all serialization
  * operations the involve {@link Identifiable}s must employ this class. Look for
  * references to {@link #INSTANCE} so see how this is accomplished.
  */
 public class STEMXMIResourceFactoryImpl extends XMIResourceFactoryImpl {
 
 	public static STEMXMIResourceFactoryImpl INSTANCE = new STEMXMIResourceFactoryImpl();
 
 
 	private ArrayList<Adapter> modificationAdapters = new ArrayList<Adapter>();
 	
 	/**
 	 * private so only a singleton instance is created
 	 * 
 	 * @see #INSTANCE
 	 */
 	private STEMXMIResourceFactoryImpl() {
 		super();
 	}
 
 	@Override
 	public Resource createResource(URI uri) {
		URI normalized = STEMURI.normalize(uri);
//		if(resourceCache.containsKey(normalized)) return resourceCache.get(normalized);
		
 		Resource newRes = new XMIResourceImpl(uri) {
 			@Override
 			protected XMLHelper createXMLHelper() {
 				return new XMIHelperImpl() {
 					@Override
 					public String getHREF(EObject obj) {
 						if (obj instanceof Identifiable) {
 							// Yes
 							final Identifiable identifiable = (Identifiable) obj;
 							final URI uri = identifiable.getURI();
 							if(uri == null) return null;
 								
 							// Is it a platform URI?
 							if (uri.isPlatform()) {
 								// Yes
 								return uri.toString() + "#/";
 							} // if platform URI?
 
 							// Is it an unresolved proxy?
 							else if (identifiable.eIsProxy()) {
 								// Yes
 								return ((BasicEObjectImpl) identifiable)
 										.eProxyURI().toString();
 							} // if unresolved proxy
 
 							else {
 								// No
 								// Resolve it against the URI of the
 								// identifiable's resource
 								deresolve = true;
 								resourceURI = identifiable.eResource().getURI();
 								return super.getHREF(obj);
 							} // else
 
 						} // if Identifiable
 						else
 							return super.getHREF(obj);
 					} // getHREF
 					
 				};
 			} // createXMLHelper
 			
 			@Override
 			public void load(Map<?, ?> options) throws IOException {
 				super.load(options);
				// Set up the listeners for resource changes
//				 for (TreeIterator<EObject> i = getAllProperContents(getContents()); i.hasNext(); )
//			        {
//			          EObject eObject = i.next();
			          
//			          for(Adapter a:modificationAdapters)
//			        	  if(eObject.eAdapters().contains(a)) eObject.eAdapters().add(a);
//			        }
 			}
 		};
 		return newRes;
 	} // createResource
 	
 	
 	public void addModificationChangeAdapter(Adapter modificationAdapter) {
 		modificationAdapters.add(modificationAdapter);
 	}
 } // STEMXMIResourceFactoryImpl
