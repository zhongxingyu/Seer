 /*******************************************************************************
  * Copyright (c) 2011, 2012 Red Hat, Inc.
  *  All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Red Hat, Inc. - initial API and implementation
  *
  * @author Bob Brodt
  ******************************************************************************/
 
 package org.eclipse.bpmn2.modeler.core.model;
 
 import java.util.Map;
 
 import org.eclipse.bpmn2.Bpmn2Package;
 import org.eclipse.bpmn2.DocumentRoot;
 import org.eclipse.bpmn2.impl.Bpmn2FactoryImpl;
 import org.eclipse.bpmn2.impl.DocumentRootImpl;
 import org.eclipse.bpmn2.modeler.core.runtime.ModelExtensionDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.TargetRuntime;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.impl.EStringToStringMapEntryImpl;
 import org.eclipse.emf.ecore.util.EcoreEMap;
 
 /**
  * This Factory will invoke the super factory to create a "bare bones"
  * model object, and then "decorate" it with model extensions defined
  * by the Target Runtime plugin extension.
  *   
  * @author Bob Brodt
  *
  */
 public class Bpmn2ModelerFactory extends Bpmn2FactoryImpl {
 	
 	/**
 	 * We provide our own DocumentRoot (since we can't modify the one in org.eclipse.bpmn2)
 	 * which prevents forwarding change notifications to the XML Namespace Prefix map AFTER
 	 * the document has been saved. This avoids the nasty "Cannot modify resource set without
 	 * a write transaction" error.
 	 * 
 	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=392427
 	 */
 	public class Bpmn2ModelerDocumentRootImpl extends DocumentRootImpl {
 		
 		private boolean deliver = true;
 		
 		public Bpmn2ModelerDocumentRootImpl() {
 			super();
 		}
 		
 		public void setDeliver(boolean deliver) {
 			this.deliver = deliver;
 		}
 	    public Map<String, String> getXMLNSPrefixMap() {
 	        if (xMLNSPrefixMap == null) {
 	            xMLNSPrefixMap = new EcoreEMap<String, String>(
 	                    EcorePackage.Literals.ESTRING_TO_STRING_MAP_ENTRY,
 	                    EStringToStringMapEntryImpl.class, this,
 	                    Bpmn2Package.DOCUMENT_ROOT__XMLNS_PREFIX_MAP) {
 	                {
 	                    initializeDelegateEList();
 	                }
 
 	                @Override
 	                protected void initializeDelegateEList() {
 	                    delegateEList = new DelegateEObjectContainmentEList<Entry<String, String>>(entryClass,
 	                    		Bpmn2ModelerDocumentRootImpl.this, Bpmn2Package.DOCUMENT_ROOT__XMLNS_PREFIX_MAP) {
 	                        @Override
 	                        protected void dispatchNotification(Notification notification) {
 	                            if (deliver)
 	                            	super.dispatchNotification(notification);
 	                        }
 	                    };
 	                }
 	            };
 	        }
 	        return xMLNSPrefixMap.map();
 
 	    }
 	}
 	
 	// Allows the XML loader for a particular target runtime to temporarily disable
 	// model extensions. This prevents extensions being added multiple times by
 	// ModelExtensionDescriptor.populateObject() every time a file is loaded.
 	protected static boolean enableModelExtensions = true;
 
 	public static Bpmn2ModelerFactory getInstance() {
 		return (Bpmn2ModelerFactory) Bpmn2ModelerFactory.eINSTANCE;
 	}
 	
 	@Override
     public DocumentRoot createDocumentRoot() {
         DocumentRootImpl documentRoot = new Bpmn2ModelerDocumentRootImpl();
         return documentRoot;
     }
 
 	@Override
     public EObject create(EClass eClass) {
     	EObject object = super.create(eClass);
 //    	if (enableModelExtensions)
     	{
 	    	TargetRuntime rt = TargetRuntime.getCurrentRuntime();
 	    	if (rt!=null) {
     			String className = eClass.getName();
 	    		if (!className.equals(Bpmn2Package.eINSTANCE.getDocumentRoot().getName()) && 
 	    			rt.getModelDescriptor().getEPackage() != Bpmn2Package.eINSTANCE &&
 	    			rt.getModelDescriptor().getEPackage().getEClassifier(className) != null ) {
     				EClass clazz = (EClass) rt.getModelDescriptor().getEPackage().getEClassifier(className);
 	    			object = rt.getModelDescriptor().getEFactory().create(clazz);
     			}
 	    		
 		    	for (ModelExtensionDescriptor med : rt.getModelExtensions()) {
 		    		if (className.equals(med.getType())) {
 		    			med.populateObject(object, eResource(), enableModelExtensions);
 		    			break;
 		    		}
 		    	}
 		    	rt.getRuntimeExtension().modelObjectCreated(object);
 	    	}
     	}
     	return object;
     }
 
     public static void setEnableModelExtensions(boolean enable) {
     	enableModelExtensions = enable;
     }
 
     public static boolean getEnableModelExtensions() {
     	return enableModelExtensions;
     }
 	
 	@SuppressWarnings("unchecked")
 	public static <T extends EObject> T create(Class<T> clazz) {
 		EObject newObject = null;
 		EClassifier eClassifier = Bpmn2Package.eINSTANCE.getEClassifier(clazz.getSimpleName());
 		if (eClassifier instanceof EClass) {
 			newObject = Bpmn2ModelerFactory.eINSTANCE.create((EClass)eClassifier);
 		}
 		return (T)newObject;
 	}
 }
