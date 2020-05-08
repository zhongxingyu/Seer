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
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.bpmn2.Bpmn2Package;
 import org.eclipse.bpmn2.Definitions;
 import org.eclipse.bpmn2.Expression;
 import org.eclipse.bpmn2.FormalExpression;
 import org.eclipse.bpmn2.Import;
 import org.eclipse.bpmn2.Interface;
 import org.eclipse.bpmn2.ItemDefinition;
 import org.eclipse.bpmn2.Lane;
 import org.eclipse.bpmn2.Operation;
 import org.eclipse.bpmn2.Participant;
 import org.eclipse.bpmn2.RootElement;
 import org.eclipse.bpmn2.di.BPMNDiagram;
 import org.eclipse.bpmn2.di.BPMNEdge;
 import org.eclipse.bpmn2.di.BPMNLabel;
 import org.eclipse.bpmn2.di.BPMNShape;
 import org.eclipse.bpmn2.di.BpmnDiPackage;
 import org.eclipse.bpmn2.modeler.core.Activator;
 import org.eclipse.bpmn2.modeler.core.model.Bpmn2ModelerFactory.Bpmn2ModelerDocumentRootImpl;
 import org.eclipse.bpmn2.modeler.core.preferences.Bpmn2Preferences;
 import org.eclipse.bpmn2.modeler.core.runtime.CustomTaskDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.ModelExtensionDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.ModelExtensionDescriptor.Property;
 import org.eclipse.bpmn2.modeler.core.runtime.TargetRuntime;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.bpmn2.util.Bpmn2ResourceImpl;
 import org.eclipse.bpmn2.util.ImportHelper;
 import org.eclipse.bpmn2.util.OnlyContainmentTypeInfo;
 import org.eclipse.bpmn2.util.QNameURIHandler;
 import org.eclipse.bpmn2.util.XmlExtendedMetadata;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dd.dc.Bounds;
 import org.eclipse.dd.dc.DcFactory;
 import org.eclipse.dd.dc.DcPackage;
 import org.eclipse.dd.dc.Point;
 import org.eclipse.dd.di.DiPackage;
 import org.eclipse.dd.di.DiagramElement;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EObjectWithInverseEList;
 import org.eclipse.emf.ecore.util.ExtendedMetaData;
 import org.eclipse.emf.ecore.xmi.XMLHelper;
 import org.eclipse.emf.ecore.xmi.XMLLoad;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.XMLSave;
 import org.eclipse.emf.ecore.xmi.impl.ElementHandlerImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * <!-- begin-user-doc --> The <b>Resource </b> associated with the package.
  * 
  * @implements Bpmn2Resource <!-- end-user-doc -->
  * @see org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl
  */
 public class Bpmn2ModelerResourceImpl extends Bpmn2ResourceImpl {
 
 	public static final String BPMN2_CONTENT_TYPE_ID = "org.eclipse.bpmn2.content-type.xml";
 	protected BpmnXmlHelper xmlHelper;
 	protected QNameURIHandler uriHandler;
 	public HashMap xmlNameToFeatureMap = new HashMap();
 
 	/**
 	 * Creates an instance of the resource.
 	 * 
 	 * @param uri
 	 *            the URI of the new resource.
 	 */
 	public Bpmn2ModelerResourceImpl(URI uri) {
 		super(uri);
 		
 		// override helper and uri handler in options map
 		this.xmlHelper = (BpmnXmlHelper)createXMLHelper();
         this.uriHandler = new FragmentQNameURIHandler(xmlHelper);
         uriHandler.setBaseURI(uri);
         
         this.getDefaultLoadOptions().put(XMLResource.OPTION_URI_HANDLER, uriHandler);
         this.getDefaultLoadOptions().put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, true);
         this.getDefaultLoadOptions().put(XMLResource.OPTION_DISABLE_NOTIFY, true);
         this.getDefaultSaveOptions().put(XMLResource.OPTION_URI_HANDLER, uriHandler);
 
         ExtendedMetaData extendedMetadata = new XmlExtendedMetadata();
         this.getDefaultSaveOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetadata);
         this.getDefaultLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetadata);
         this.getDefaultSaveOptions().put(XMLResource.OPTION_SAVE_TYPE_INFORMATION, new OnlyContainmentTypeInfo());
         this.getDefaultSaveOptions().put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
         this.getDefaultLoadOptions().put(XMLResource.OPTION_USE_LEXICAL_HANDLER, Boolean.TRUE);
         this.getDefaultSaveOptions().put(XMLResource.OPTION_ELEMENT_HANDLER, new ElementHandlerImpl(true));
         this.getDefaultSaveOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
         this.getDefaultSaveOptions().put(XMLResource.OPTION_USE_CACHED_LOOKUP_TABLE, new ArrayList<Object>());
         
         // some interesting things to play with:
 //        this.getDefaultLoadOptions().put(XMLResource.OPTION_LAX_FEATURE_PROCESSING, true);
 //        this.getDefaultLoadOptions().put(XMLResource.OPTION_LAX_WILDCARD_PROCESSING, true);
 //        this.getDefaultLoadOptions().put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, true);
 //        this.getDefaultLoadOptions().put(XMLResource.OPTION_ANY_TYPE, BpmnDiPackage.eINSTANCE.getBPMNPlane());
         this.getDefaultLoadOptions().put(XMLResource.OPTION_USE_XML_NAME_TO_FEATURE_MAP, xmlNameToFeatureMap);
 
         // only necessary if this resource will not be added to a ResourceSet instantly
         this.eAdapters().add(oppositeReferenceAdapter);
 	}
 
     @Override
     protected XMLHelper createXMLHelper() {
     	if (xmlHelper!=null)
     		return xmlHelper;
         return new Bpmn2ModelerXmlHelper(this);
     }
 
 	/**
 	 * Override this method to hook in our own XmlHandler
 	 */
 	@Override
 	protected XMLLoad createXMLLoad() {
 		return new XMLLoadImpl(createXMLHelper()) {
 			@Override
 			protected DefaultHandler makeDefaultHandler() {
 				return new Bpmn2ModelerXmlHandler(resource, helper, options);
 			}
 		};
 	}
 
 	@Override
 	protected XMLSave createXMLSave() {
 		return new Bpmn2ModelerXMLSave(createXMLHelper()) {
 		};
 	}
 
 	@Override
 	protected void prepareSave() {
 		EObject cur;
 		Definitions thisDefinitions = ImportHelper.getDefinitions(this);
 		for (Iterator<EObject> iter = getAllContents(); iter.hasNext();) {
 			cur = iter.next();
 
 			setDefaultId(cur);
 
 			for (EObject referenced : cur.eCrossReferences()) {
 				setDefaultId(referenced);
 				if (thisDefinitions != null) {
 					Resource refResource = referenced.eResource();
 					if (refResource != null && refResource != this) {
 						createImportIfNecessary(thisDefinitions, refResource);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Set the ID attribute of cur to a generated ID, if it is not already set.
 	 * 
 	 * @param obj
 	 *            The object whose ID should be set.
 	 */
 	private void setDefaultId(EObject obj) {
 		if (obj.eClass() != null) {
 			EStructuralFeature idAttr = obj.eClass().getEIDAttribute();
 			if (idAttr != null && !obj.eIsSet(idAttr)) {
 				ModelUtil.setID(obj);
 			}
 		}
 	}
 
 	/**
 	 * We need extend the standard SAXXMLHandler to hook into the handling of
 	 * attribute references which may be either simple ID Strings or QNames.
 	 * We'll search through all of the objects' IDs first to find the one we're
 	 * looking for. If not, we'll try a QName search.
 	 */
 	protected static class Bpmn2ModelerXmlHandler extends BpmnXmlHandler {
 
 		Bpmn2Preferences prefs = null;
 		static HashSet<EStructuralFeature> qnameFeatures = new HashSet<EStructuralFeature>();
 		
 		static {
 			qnameFeatures.add(Bpmn2Package.eINSTANCE.getItemDefinition_StructureRef());
 			qnameFeatures.add(Bpmn2Package.eINSTANCE.getMessage_ItemRef());
 			qnameFeatures.add(Bpmn2Package.eINSTANCE.getError_StructureRef());
 			qnameFeatures.add(Bpmn2Package.eINSTANCE.getInterface_ImplementationRef());
 			qnameFeatures.add(Bpmn2Package.eINSTANCE.getOperation_ImplementationRef());
 			qnameFeatures.add(Bpmn2Package.eINSTANCE.getOperation_InMessageRef());
 			qnameFeatures.add(Bpmn2Package.eINSTANCE.getOperation_OutMessageRef());
 			qnameFeatures.add(Bpmn2Package.eINSTANCE.getOperation_ErrorRefs());
 		};
 
 		
 		public Bpmn2ModelerXmlHandler(XMLResource xmiResource, XMLHelper helper, Map<?, ?> options) {
 			super(xmiResource, helper, options);
 		}
 
 		@Override
 		public void startDocument() {
 			super.startDocument();
 			Bpmn2ModelerFactory.setEnableModelExtensions(false);
 		}
 
 		@Override
 		public void endDocument() {
 			super.endDocument();
 			Bpmn2ModelerFactory.setEnableModelExtensions(true);
 		}
 
 		@Override
 		protected void handleObjectAttribs(EObject obj) {
 			super.handleObjectAttribs(obj);
 			if (attribs != null) {
 				InternalEObject internalEObject = (InternalEObject) obj;
 				for (int i = 0, size = attribs.getLength(); i < size; ++i) {
 					String name = attribs.getQName(i);
 					if (name.equals(XMLResource.XML_NS)) {
 						// create an ns prefix in the prefix map for this default namespace
 						// and qualify any qnameFeatures contained in this object...
 						String namespaceURI = attribs.getValue(i);
 						for (EStructuralFeature f : obj.eClass().getEAllStructuralFeatures()) {
 							if (qnameFeatures.contains(f)) {
 								Object value = obj.eGet(f);
 								if (ModelUtil.isStringWrapper(value)) {
 									String localpart = ModelUtil.getStringWrapperValue(value);
 									if (localpart!=null && !localpart.isEmpty() && !localpart.contains(":")) {
 										String prefix = helper.getPrefix(namespaceURI);
 										if (prefix==null || prefix.isEmpty()) {
 											for (int index = 0; true; ++index) {
 												prefix = "ns" + index;
 												String ns = helper.getPrefixToNamespaceMap().get(prefix);
 												if (ns==null)
 													break;
 											}
 											helper.addPrefix(prefix, namespaceURI);	
 										}
 										ModelUtil.setStringWrapperValue(value, prefix + ":" + localpart);
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 
 			if (obj instanceof BPMNShape) {
 				BPMNShape bpmnShape = (BPMNShape)obj;
 
 				Hashtable<String,String> map = new Hashtable<String,String>();
 				if (attribs != null) {
 					for (int i = 0, size = attribs.getLength(); i < size; ++i) {
 						String key = attribs.getQName(i);
 						String value = attribs.getValue(i);
 						map.put(key, value);
 					}
 					Bpmn2Preferences.getInstance(this.resourceURI).applyBPMNDIDefaults(bpmnShape, map);
 				}
 			}
 			else if (obj instanceof ItemDefinition) {
 				ItemDefinition itemDef = (ItemDefinition)obj;
 
 				Definitions definitions = ImportHelper.getDefinitions(xmlResource);
 				URI referencingURI = ImportHelper.makeURICanonical(resourceURI);
 				String location = ModelUtil.getStringWrapperValue(itemDef.getStructureRef());
 				if (location!=null) {
 					int i = location.indexOf("$");
 					if (i>0)
 						location = location.substring(0,i);
 					URI uri = URI.createURI(location).resolve(referencingURI);
 					uri = uri.trimFragment();
 					Import imp = ImportHelper.findImportForLocation(definitions, uri);
 					itemDef.setImport(imp);
 				}
 			}
 		}
 
 		/**
 		 * Overridden to be able to convert ID references in attributes to URIs
 		 * during load. If the reference can't be found by its ID, we'll try a
 		 * QName search (done in the super class)
 		 * 
 		 * @param ids
 		 *            In our case the parameter will contain exactly one ID that
 		 *            we resolve to URI.
 		 */
 		@Override
 		protected void setValueFromId(EObject object, EReference eReference, String ids) {
 
 			EObject value = null;
 
 			// hack to handle QNames and arbitrary strings in ItemDefinition structureRefs,
 			// Interface implementationRefs and Operation implementationRefs
 			if (
 					(object instanceof ItemDefinition &&
 						eReference == Bpmn2Package.eINSTANCE.getItemDefinition_StructureRef()) ||
 					(object instanceof Interface &&
 							eReference == Bpmn2Package.eINSTANCE.getInterface_ImplementationRef()) ||
 					(object instanceof Operation &&
 							eReference == Bpmn2Package.eINSTANCE.getOperation_ImplementationRef())
 			) {
 //				try {
 //					// if the ID string is a URI, try to resolve and load the object
 //					URI uri = uriHandler.resolve( URI.createURI(ids) );
 //					value = resourceSet.getEObject(uri, true);
 //				}
 //				catch (Exception e) {
 //				}
 				if (value==null) {
 					// not a URI or can't find EObject: create a string wrapper EObject
 					object.eSet(eReference, ModelUtil.createStringWrapper(ids));
 				}
 				else
 					object.eSet(eReference, value);
 				return;
 			}
 
 			for (EObject o : objects) {
 				TreeIterator<EObject> iter = o.eAllContents();
 				while (iter.hasNext()) {
 					value = iter.next();
 					EStructuralFeature feature = value.eClass().getEIDAttribute();
 					if (feature != null && value.eGet(feature) != null) {
 						Object id = value.eGet(feature);
 						if (id != null && id.equals(ids)) {
 							try {
 								if (object.eGet(eReference) instanceof EList) {
 									((EList)object.eGet(eReference)).add(value);
 								}
 								else {
 									object.eSet(eReference, value);
 								}
 							} catch (Exception e) {
 								String msg = "Invalid or unknown reference from:\n  " +
 										object + "\nfeature:\n  " + eReference +
 										"\nto:\n  " + value;
 								IStatus s = new Status(Status.ERROR, Activator.PLUGIN_ID,
 										msg, e);
 								Activator.getDefault().logStatus(s);
 							}
 							return;
 						}
 					}
 				}
 			}
 			
 			// And yet another hack to deal with files generated by Savara:
 			// Savara creates some object references as QNames and, while the
 			// "Official" BPMN 2.0 xsd says these SHOULD be QNames, the bpmn2 EMF
 			// model has these defined as "resolveProxies=false" which means they
 			// will NOT get resolved.
 			if (/*!eReference.isResolveProxies() && */ids.contains(":")) {
 				// Resolve QNames here: if they are internal objects,
 				// simply replace the ID string with the URI fragment.
 				// If they are not internal objects, then there's a
 				// problem with the file!
 				String resolvedId = ((QNameURIHandler) uriHandler).convertQNameToUri(ids);
 				URI resolvedURI = URI.createURI(resolvedId);
 				URI resourceURI = xmlResource.getURI();
 				if (resolvedURI.trimFragment().equals(resourceURI))
 					ids = resolvedURI.fragment();
 			}
 			super.setValueFromId(object, eReference, ids);
 		}
 	}
 	
 	public static class Bpmn2ModelerXMLSave extends XMLSaveImpl {
 		protected float minX = Float.MAX_VALUE;
 		protected float minY = Float.MAX_VALUE;
 
 		public Bpmn2ModelerXMLSave(XMLHelper helper) {
 			super(helper);
 			helper.getPrefixToNamespaceMap().clear();
 		}
 
 		@Override
 		protected void init(XMLResource resource, Map<?, ?> options) {
 			super.init(resource, options);
 			featureTable = new Bpmn2ModelerXMLSave.Bpmn2Lookup(map, extendedMetaData, elementHandler);
 			
 			final List<BPMNDiagram> diagrams = getAll(BPMNDiagram.class, resource);
 			for (BPMNDiagram bpmnDiagram : diagrams) {
 				findMinXY(bpmnDiagram);
 			}
 		}
 
         @Override
 		protected void endSave(List<? extends EObject> contents) throws IOException
 		{
         	Bpmn2ModelerDocumentRootImpl documentRoot = null;
         	if (contents.size()>0 && contents.get(0) instanceof Bpmn2ModelerDocumentRootImpl) {
         		documentRoot = (Bpmn2ModelerDocumentRootImpl)contents.get(0);
 				documentRoot.setDeliver(false);
 			}
         	
 			super.endSave(contents);
 			
 			if (documentRoot!=null) {
 				documentRoot.setDeliver(true);
 			}
 		}
 
 		@Override
         protected boolean shouldSaveFeature(EObject o, EStructuralFeature f) {
             if (o instanceof BPMNShape && f==BpmnDiPackage.eINSTANCE.getBPMNShape_IsHorizontal()) {
             	BPMNShape s = (BPMNShape)o;
             	if (s.getBpmnElement() instanceof Lane || s.getBpmnElement() instanceof Participant)
             		return true;
             }
             
             // we also want to store x and y with value zero, would be skipped because of default value otherwise
            if (o instanceof Bounds) {
             	return true;
             }
             
             // empty Expressions should not be saved
             if (f!=null && f.getEType() == Bpmn2Package.eINSTANCE.getExpression()) {
             	Expression expression = (Expression)o.eGet(f);
             	if (expression==null)
             		return false;
             	if (expression instanceof FormalExpression) {
 	            	FormalExpression formalExpression = (FormalExpression)expression;
 	            	if (
 	            			(formalExpression.getBody()==null || formalExpression.getBody().isEmpty()) &&
 	            			(formalExpression.getLanguage()==null || formalExpression.getLanguage().isEmpty()) &&
 	            			formalExpression.getEvaluatesToTypeRef()==null) {
 	            		return false;
 	            	}
             	}
             }
             
             // don't serialize the "body" attribute of FormalExpressions because the expression text
             // is already in the CDATA section of the <bpmn2:expression> element. This would cause
             // the expression text to be duplicated on deserialization.
             // Same goes for Documentation.text
 			if (Bpmn2Package.eINSTANCE.getFormalExpression_Body().equals(f) ||
 					Bpmn2Package.eINSTANCE.getDocumentation_Text().equals(f))
 				return false;
             
             return super.shouldSaveFeature(o, f);
         }
 		
 		protected <T> List<T> getAll(Class<T> class1, Resource resource) {
 			ArrayList<T> l = new ArrayList<T>();
 			TreeIterator<EObject> contents = resource.getAllContents();
 			for (; contents.hasNext();) {
 				Object t = contents.next();
 				if (class1.isInstance(t)) {
 					l.add((T) t);
 				}
 			}
 			return l;
 		}
 		
 		protected void findMinXY(BPMNDiagram bpmnDiagram) {
 			EList<DiagramElement> elements = (EList<DiagramElement>) bpmnDiagram.getPlane().getPlaneElement();
 			for (DiagramElement e : elements) {
 				if (e instanceof BPMNShape) {
 					Bounds b = ((BPMNShape)e).getBounds();
 					minX = Math.min(minX, b.getX());
 					minY = Math.min(minY, b.getY());
 				}
 				else if (e instanceof BPMNEdge) {
 					List<Point> points = ((BPMNEdge)e).getWaypoint();
 					for (Point p : points) {
 						minX = Math.min(minX, p.getX());
 						minY = Math.min(minY, p.getY());
 					}
 
 				}
 				else if (e instanceof BPMNLabel) {
 					Bounds b = ((BPMNLabel)e).getBounds();
 					minX = Math.min(minX, b.getX());
 					minY = Math.min(minY, b.getY());
 				}
 			}
 		}
 
 		@Override
 		protected void saveElement(EObject o, EStructuralFeature f) {
 			float oldX = 0, oldY = 0;
 			List<Point> oldPoints = null;
 			
 			if (minX<0 || minY<0) {
 				if (o instanceof BPMNShape) {
 					Bounds b = ((BPMNShape)o).getBounds();
 					b.eSetDeliver(false);
 					if (minX<0) {
 						oldX = b.getX();
 						b.setX(oldX - minX);
 					}
 					if (minY<0) {
 						oldY = b.getY();
 						b.setY(oldY - minY);
 					}
 				}
 				else if (o instanceof BPMNEdge) {
 					List<Point> points = ((BPMNEdge)o).getWaypoint();
 					oldPoints = new ArrayList<Point>();
 					for (Point p : points) {
 						p.eSetDeliver(false);
 						Point oldPoint = DcFactory.eINSTANCE.createPoint();
 						oldPoint.setX(p.getX());
 						oldPoint.setY(p.getY());
 						oldPoints.add(oldPoint);
 						if (minX<0)
 							p.setX( p.getX() - minX);
 						if (minY<0)
 							p.setY( p.getY() - minY);
 					}
 				}
 				else if (o instanceof BPMNLabel) {
 					Bounds b = ((BPMNLabel)o).getBounds();
 					if (b!=null) {
 						b.eSetDeliver(false);
 						if (minX<0) {
 							oldX = b.getX();
 							b.setX(oldX - minX);
 						}
 						if (minY<0) {
 							oldY = b.getY();
 							b.setY(oldY - minY);
 						}
 					}
 				}
 			}
 			
 			super.saveElement(o, f);
 			
 			if (minX<0 || minY<0) {
 				if (o instanceof BPMNShape) {
 					Bounds b = ((BPMNShape)o).getBounds();
 					if (minX<0) {
 						b.setX(oldX);
 					}
 					if (minY<0) {
 						b.setY(oldY);
 					}
 					b.eSetDeliver(true);
 				}
 				else if (o instanceof BPMNEdge) {
 					List<Point> points = ((BPMNEdge)o).getWaypoint();
 					int index = 0;
 					for (Point p : points) {
 						if (minX<0)
 							p.setX(oldPoints.get(index).getX());
 						if (minY<0)
 							p.setY(oldPoints.get(index).getY());
 						p.eSetDeliver(true);
 						++index;
 					}
 				}
 				else if (o instanceof BPMNLabel) {
 					Bounds b = ((BPMNLabel)o).getBounds();
 					if (b!=null) {
 						if (minX<0) {
 							b.setX(oldX);
 						}
 						if (minY<0) {
 							b.setY(oldY);
 						}
 						b.eSetDeliver(true);
 					}
 				}
 			}
 		}
 
 		@Override
 		public void traverse(List<? extends EObject> contents) {
 			for (EObject e : contents) {
 				if (e instanceof Definitions) {
 					List<RootElement> roots = ((Definitions) e).getRootElements();
 					Process p = null;
 					for (RootElement root : roots) {
 						if (root instanceof Process) {
 							p = (Process) root;
 						}
 					}
 					if (p != null) {
 						((Definitions) e).getRootElements().remove(p);
 						((Definitions) e).getRootElements().add((RootElement) p);
 					}
 				}
 			}
 			super.traverse(contents);
 		}
 
 		public static class Bpmn2Lookup extends XMLSaveImpl.Lookup {
 			public Bpmn2Lookup(XMLMap map, ExtendedMetaData extendedMetaData, ElementHandler elementHandler) {
 				super(map, extendedMetaData, elementHandler);
 			}
 
 			@Override
 			public EStructuralFeature[] getFeatures(EClass cls) {
 				int index = getIndex(cls);
 				EClass c = classes[index];
 
 				if (c == cls) {
 					return features[index];
 				}
 
 				EStructuralFeature[] featureList = listFeatures(cls);
 				if (c == null) {
 					classes[index] = cls;
 					features[index] = featureList;
 					featureKinds[index] = listKinds(featureList);
 				}
 
 				if (cls.getName().equalsIgnoreCase("Process")) {
 					EStructuralFeature[] modifiedFeatureList = getModifiedProcessFeatureSet(featureList);
 					if (c == null) {
 						classes[index] = cls;
 						features[index] = modifiedFeatureList;
 						featureKinds[index] = listKinds(modifiedFeatureList);
 					}
 					return modifiedFeatureList;
 				}
 				return featureList;
 			}
 		}
 
 		private static EStructuralFeature[] getModifiedProcessFeatureSet(EStructuralFeature[] processFeatureList) {
 			/**
 			 * Feature list for Process provided by eclipse.bpmn2: -
 			 * extensionDefinitions (0) - id (1) - anyAttribute (2) - name (3) -
 			 * definitionalCollaborationRef (4) - isClosed (5) - isExecutable
 			 * (6) - processType (7) - extensionValues (8) - documentation (9) -
 			 * supportedInterfaceRefs (10) - ioSpecification (11) - ioBinding
 			 * (12) - laneSets (13) - flowElements (14) - auditing (15) -
 			 * monitoring (16) - properties (17) - artifacts (18) - resources
 			 * (19) - correlationSubscriptions (20) - supports (21) Semantic.xsd
 			 * sequence definition for Process: <xsd:sequence> <xsd:element
 			 * ref="auditing" minOccurs="0" maxOccurs="1"/> <xsd:element
 			 * ref="monitoring" minOccurs="0" maxOccurs="1"/> <xsd:element
 			 * ref="property" minOccurs="0" maxOccurs="unbounded"/> <xsd:element
 			 * ref="laneSet" minOccurs="0" maxOccurs="unbounded"/> <xsd:element
 			 * ref="flowElement" minOccurs="0" maxOccurs="unbounded"/>
 			 * <xsd:element ref="artifact" minOccurs="0" maxOccurs="unbounded"/>
 			 * <xsd:element ref="resourceRole" minOccurs="0"
 			 * maxOccurs="unbounded"/> <xsd:element
 			 * ref="correlationSubscription" minOccurs="0"
 			 * maxOccurs="unbounded"/> <xsd:element name="supports"
 			 * type="xsd:QName" minOccurs="0" maxOccurs="unbounded"/>
 			 * </xsd:sequence>
 			 * 
 			 * Moving auditing, monitoring, property above flowElements...
 			 */
 
 			EStructuralFeature[] retArray = new EStructuralFeature[processFeatureList.length];
 			for (int i = 0; i < 13; i++) {
 				retArray[i] = processFeatureList[i];
 			}
 			retArray[13] = processFeatureList[15]; // auditing
 			retArray[14] = processFeatureList[16]; // monitoring
 			retArray[15] = processFeatureList[17]; // properties
 			retArray[16] = processFeatureList[13]; // lanesets
 			retArray[17] = processFeatureList[14]; // flow elements
 			retArray[18] = processFeatureList[18]; // artifacts
 			retArray[19] = processFeatureList[19]; // resources
 			retArray[20] = processFeatureList[20]; // correlationSubscriptions
 			retArray[21] = processFeatureList[21]; // supports
 
 			return retArray;
 		}
 	}
 	
 	// TODO check this, is this the correct way to deal with this ID prefixes
 	/**
 	 * QName handler to make create URIs out of the fragment, which is the local part of the QName
 	 * 
 	 * Most other tools dont understand QNames in referencing attributes
 	 * 
 	 * @author drobisch
 	 *
 	 */
 	public static class FragmentQNameURIHandler extends QNameURIHandler {
 
 		protected BpmnXmlHelper xmlHelper;
 
 		public FragmentQNameURIHandler(BpmnXmlHelper xmlHelper) {
 			super(xmlHelper);
 			this.xmlHelper = xmlHelper;
 		}
 
 		@Override
 		public URI resolve(URI uri) {
 			URI resolvedUri = super.resolve(uri);
 			if (resolvedUri.isRelative())
 				resolvedUri = resolvedUri.resolve(baseURI);
 			return resolvedUri;
 		}
 
 		@Override
 		public URI deresolve(URI uri) {
 			String fragment = uri.fragment();
 			if (fragment != null && !fragment.startsWith("/")) {
 				// return just fragment (i.e. without the '#') but only if local reference
 				URI otherURI = uri.trimFragment();
 				if (baseURI.equals(otherURI))
 					return URI.createURI(fragment);
 				else
 					return uri;
 			}
 			return super.deresolve(uri);
 		}
 
 		@Override
 		public String convertQNameToUri(String qName) {
 			if (qName.contains("#") || qName.contains("/")) {
 				// We already have an URI and not QName, e.g. URL
 				return qName;
 			}
 
 			// Split into prefix and local part (fragment)
 			String[] parts = qName.split(":");
 			String prefix, fragment;
 			if (parts.length == 1) {
 				prefix = null;
 				fragment = qName;
 			} else if (parts.length == 2) {
 				prefix = parts[0];
 				fragment = parts[1];
 			} else
 				throw new IllegalArgumentException("Illegal QName: " + qName);
 
 			if (fragment.contains(".")) {
 				// HACK: officially IDs can contain ".", but unfortunately
 				// XmlHandler calls resolve also for xsi:schemaLocation stuff
 				// and similar, that are
 				// NO URIs. We must not process them.
 				return qName;
 			}
 
 			boolean isTargetNamespacePrefix = false;
 			try {
 				isTargetNamespacePrefix = xmlHelper.isTargetNamespace(prefix);
 			} catch (Exception e) {
 			}
 			if (!isTargetNamespacePrefix)
 				return xmlHelper.getPathForPrefix(prefix).appendFragment(fragment).toString();
 			else
 				return baseURI.appendFragment(fragment).toString();
 		}
 	}
 	
 	public static class Bpmn2ModelerXmlHelper extends BpmnXmlHelper {
 
 		// List of all EReferences that are defined as type="xsd:QName" in the BPMN 2.0 Schema
 		// This information is not represented in the MDT BPMN2 project metamodel. 
 		protected HashSet<EStructuralFeature> qnameMap = new HashSet<EStructuralFeature>();
 		boolean isQNameFeature = false;
 
 		public Bpmn2ModelerXmlHelper(Bpmn2ResourceImpl resource) {
 			super(resource);
 			qnameMap.add(Bpmn2Package.eINSTANCE.getExtension_Definition());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getRelationship_Sources());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getRelationship_Targets());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getAssociation_SourceRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getAssociation_TargetRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getGroup_CategoryValueRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getCorrelationKey_CorrelationPropertyRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getCorrelationProperty_Type());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getCorrelationPropertyBinding_CorrelationPropertyRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getCorrelationPropertyRetrievalExpression_MessageRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getCorrelationSubscription_CorrelationPropertyBinding());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getError_StructureRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getEscalation_StructureRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getFlowElement_CategoryValueRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getFlowNode_Incoming());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getFlowNode_Outgoing());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getFormalExpression_EvaluatesToTypeRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getInputOutputBinding_OperationRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getItemDefinition_StructureRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getMessage_ItemRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getResourceParameter_Type());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getInterface_ImplementationRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getOperation_InMessageRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getOperation_OutMessageRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getOperation_ErrorRefs());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getOperation_ImplementationRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getCallConversation_CalledCollaborationRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getConversationAssociation_InnerConversationNodeRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getConversationAssociation_OuterConversationNodeRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getConversationLink_SourceRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getConversationLink_TargetRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getConversationNode_MessageFlowRefs());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getConversationNode_ParticipantRefs());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getConversationNode_CorrelationKeys());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getMessageFlow_SourceRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getMessageFlow_TargetRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getMessageFlow_MessageRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getMessageFlowAssociation_InnerMessageFlowRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getMessageFlowAssociation_OuterMessageFlowRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getParticipant_InterfaceRefs());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getParticipant_EndPointRefs());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getParticipant_ProcessRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getParticipantAssociation_InnerParticipantRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getParticipantAssociation_OuterParticipantRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getCallableElement_SupportedInterfaceRefs());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getCallActivity_CalledElementRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getMultiInstanceLoopCharacteristics_LoopDataInputRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getMultiInstanceLoopCharacteristics_LoopDataOutputRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getMultiInstanceLoopCharacteristics_OneBehaviorEventRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getMultiInstanceLoopCharacteristics_NoneBehaviorEventRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getReceiveTask_MessageRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getReceiveTask_OperationRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getResourceRole_ResourceRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getSendTask_MessageRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getSendTask_OperationRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getServiceTask_OperationRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getItemAwareElement_ItemSubjectRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getBoundaryEvent_AttachedToRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getCatchEvent_EventDefinitionRefs());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getCompensateEventDefinition_ActivityRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getErrorEventDefinition_ErrorRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getEscalationEventDefinition_EscalationRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getLinkEventDefinition_Source());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getLinkEventDefinition_Target());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getMessageEventDefinition_OperationRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getMessageEventDefinition_MessageRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getSignal_StructureRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getSignalEventDefinition_SignalRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getThrowEvent_EventDefinitionRefs());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getProcess_Supports());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getProcess_DefinitionalCollaborationRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getLane_PartitionElementRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getGlobalChoreographyTask_InitiatingParticipantRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getChoreographyActivity_ParticipantRefs());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getChoreographyActivity_InitiatingParticipantRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getChoreographyTask_MessageFlowRef());
 			qnameMap.add(Bpmn2Package.eINSTANCE.getCallChoreography_CalledChoreographyRef());
 			qnameMap.add(BpmnDiPackage.eINSTANCE.getBPMNPlane_BpmnElement());
 			qnameMap.add(BpmnDiPackage.eINSTANCE.getBPMNShape_BpmnElement());
 			qnameMap.add(BpmnDiPackage.eINSTANCE.getBPMNShape_ChoreographyActivityShape());
 			qnameMap.add(BpmnDiPackage.eINSTANCE.getBPMNEdge_BpmnElement());
 			qnameMap.add(BpmnDiPackage.eINSTANCE.getBPMNEdge_SourceElement());
 			qnameMap.add(BpmnDiPackage.eINSTANCE.getBPMNEdge_TargetElement());
 			qnameMap.add(BpmnDiPackage.eINSTANCE.getBPMNLabel_LabelStyle());
 		}		
 		
 		@Override
 		public Object getValue(EObject eObject, EStructuralFeature eStructuralFeature) {
 			Object o = super.getValue(eObject, eStructuralFeature);
 			if (qnameMap.contains(eStructuralFeature)) {
 				List<String> prefixes = urisToPrefixes.get(getTargetNamespace());
 				if (prefixes!=null && prefixes.contains(""))
 					isQNameFeature = false;
 				else
 					isQNameFeature = true;
 			}
 			else
 				isQNameFeature = false;
 			return o;
 		}
 
 		@Override
 		public String getHREF(EObject obj) {
 			// convert the attribute ID references to a QName
 			String s = super.getHREF(obj);
 			if (isQNameFeature && !ModelUtil.isStringWrapper(obj))
 				s = convertToQName(s);
 			return s;
 		}
 
 		public String getIDREF(EObject obj) {
 			// convert the element ID references to a QName
 			String s = super.getIDREF(obj);
 			if (isQNameFeature && !ModelUtil.isStringWrapper(obj))
 				s = convertToQName(s);
 			return s;
 		}
 
 		/**
 		 * Returns the targetNamespace defined in the <definitions> root element.
 		 * 
 		 * @return a namespace URI or null if targetNamespace is not defined.
 		 */
 		private String getTargetNamespace() {
 			Definitions definitions = ImportHelper.getDefinitions(getResource());
 			if (definitions==null)
 				return null;
 			return definitions.getTargetNamespace();
 		}
 		
 		/**
 		 * Get the namespace prefix for the targetNamespace.
 		 * 
 		 * @return null if the document does not define a targetNamespace,
 		 * an empty string if there is a targetNamespace, but no prefix has been
 		 * defined for it, or the prefix for the targetNamespace
 		 * 
 		 * Examples:
 		 * 
 		 * <bpmn2:definitions tns="http://eclipse.org/example" targetNamespace="http://eclipse.org/example"/>
 		 *   return "tns"
 		 *   
 		 * <bpmn2:definitions targetNamespace="http://eclipse.org/example"/>
 		 *   returns ""
 		 *   
 		 * <bpmn2:definitions tns="http://eclipse.org/example"/>
 		 *   returns null
 		 */
 		private String getTargetNamespacePrefix() {
 			String targetNamespace = getTargetNamespace();
 			if (targetNamespace!=null && !targetNamespace.isEmpty()) {
 				String prefix = getPrefix(targetNamespace);
 				if (prefix==null || prefix.isEmpty()) {
 					for (Entry<String, String> e : this.getPrefixToNamespaceMap().entrySet()) {
 						if (targetNamespace.equals(e.getValue()) && !e.getKey().isEmpty()) {
 							return e.getKey();
 						}
 					}
 				}
 				return "";
 			}
 			return null;
 		}
 		
 		/**
 		 * Converts the NCName "s" to a QName that maps to the targetNamespace
 		 * 
 		 * @param s - a non-colonized name string
 		 * @return the string "s" prefixed with the NS prefix for the targetNamespace.
 		 */
 		private String convertToQName(String s) {
 			if (!s.contains(":")) {
 				String prefix = getTargetNamespacePrefix();
 				if (prefix!=null && !prefix.isEmpty()) {
 					s = prefix + ":" + s;
 				}
 			}
 			return s;
 		}
 		
 		public void setValue(EObject object, EStructuralFeature feature,
 				Object value, int position) {
 			// fix some kind of bug which causes duplicate entries in objects that have
 			// mutual reference lists.
 			if (	object!=null
 					&& feature!=null
 					&& object.eClass()!=null
 					&& feature == object.eClass().getEStructuralFeature(feature.getFeatureID())
 			) {
 				Object v = object.eGet(feature);
 				if (v instanceof EObjectWithInverseEList) {
 					EObjectWithInverseEList list = (EObjectWithInverseEList)v;
 					if (list.contains(value)) {
 						// it's already in there!
 						return;
 					}
 				}
 			}
 			
 			// check if we need to change the attribute's data type:
 			if (feature instanceof EAttribute) {
 				// and only if the attribute's data type is "Object"
 				EClassifier t = feature.getEType();
 				if (t!=null && t.getInstanceClass() == Object.class) {
 					// search for the attribute in the target runtime's Custom Task and
 					// Model Extension definitions by name
 					List<Property>properties = new ArrayList<Property>();
 					TargetRuntime rt = TargetRuntime.getCurrentRuntime();
 					String className = object.eClass().getName();
 					String featureName = feature.getName();
 					for (CustomTaskDescriptor ctd : rt.getCustomTasks()) {
 						if (className.equals(ctd.getType())) {
 							properties.addAll(ctd.getProperties());
 						}
 					}
 					for (ModelExtensionDescriptor med : rt.getModelExtensions()) {
 						if (className.equals(med.getType())) {
 							properties.addAll(med.getProperties());
 						}
 					}
 					for (Property p : properties) {
 						if (p.name.equals(featureName)) {
 							String type = p.type;
 							if (type==null)
 								type = "EString";
 							EClassifier eClassifier = ModelUtil.getEClassifierFromString(
 									rt.getModelDescriptor().getEPackage(),type);
 							if (eClassifier instanceof EDataType) {
 								feature.setEType(eClassifier);
 							}
 							break;
 						}
 					}
 				}
 			}
 			
 			super.setValue(object, feature, value, position);
 		}
 
     	@Override
 		public EStructuralFeature getFeature(EClass eClass, String namespaceURI, String name, boolean isElement) {
     		// This fixes https://bugs.eclipse.org/bugs/show_bug.cgi?id=378296
     		// I'm still not convinced that getFeature() shouldn't simply return the feature
     		// from the given EClass instead of searching the EPackage of the Resource being
     		// loaded (if the EClass has a feature with that name of course).
     		EStructuralFeature result = null;
     		EPackage pkg = eClass.getEPackage();
 			if (pkg != Bpmn2Package.eINSTANCE &&
 					pkg != BpmnDiPackage.eINSTANCE &&
 					pkg != DcPackage.eINSTANCE &&
 					pkg != DiPackage.eINSTANCE &&
 					pkg != TargetRuntime.getCurrentRuntime().getModelDescriptor().getEPackage()) {
 				result = eClass.getEStructuralFeature(name);
 			}
 			if (result==null)
 				result = super.getFeature(eClass, namespaceURI, name, isElement);
 			return result;
 		}
 	}
 }
