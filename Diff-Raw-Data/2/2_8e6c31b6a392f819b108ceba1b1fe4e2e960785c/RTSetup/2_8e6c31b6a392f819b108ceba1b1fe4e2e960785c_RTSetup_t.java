 /*
  * Copyright (c) 2005 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests.setup;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Collection;
 
 import junit.framework.Assert;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.operations.OperationHistoryFactory;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
 import org.eclipse.emf.codegen.ecore.genmodel.GenFeature;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.workspace.AbstractEMFOperation;
 import org.eclipse.gmf.codegen.gmfgen.GenNode;
 import org.eclipse.gmf.codegen.gmfgen.TypeLinkModelFacet;
 import org.eclipse.gmf.runtime.diagram.core.DiagramEditingDomainFactory;
 import org.eclipse.gmf.runtime.notation.Bounds;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.Edge;
 import org.eclipse.gmf.runtime.notation.Node;
 import org.eclipse.gmf.runtime.notation.NotationFactory;
 import org.osgi.framework.Bundle;
 
 /**
  * TODO DomainModelInstanceSource to separate instantiation from diagram creation and
  * to facilitate testing of domain model instance - not to miss containments and other
  * potential problems in DomainModelSource
  * Simple implementation that creates simple diagram with few elements
  * @author artem
  */
 public class RTSetup implements RTSource {
 
 	private Diagram myCanvas;
 	private Node myNodeA;
 	private Node myNodeB;
 	private Edge myLink;
 	
 	private EObject myDiagramElement;
 
 	public RTSetup() {
 	}
 
 	public final RTSetup init(Bundle b, DiaGenSource genSource) {
 		initDiagramFileContents(new CoolDomainInstanceProducer(b), genSource);
 		saveDiagramFile(genSource.getGenDiagram().getEditingDomainID());
 		return this;
 	}
 
 	public final RTSetup init(DiaGenSource genSource) {
 		initDiagramFileContents(new NaiveDomainInstanceProducer(), genSource);
 		saveDiagramFile(genSource.getGenDiagram().getEditingDomainID());
 		return this;
 	}
 
 	/**
 	 * @return <code>this</code> for convenience
 	 */
 	protected void initDiagramFileContents(DomainInstanceProducer instanceProducer, DiaGenSource genSource) {
 		myCanvas = NotationFactory.eINSTANCE.createDiagram();
 		myDiagramElement = instanceProducer.createInstance(genSource.getGenDiagram().getDomainDiagramElement());
 		myCanvas.setElement(myDiagramElement);
 		myCanvas.setType(genSource.getGenDiagram().getEditorGen().getModelID());
 		
 		myNodeA = setupNotationNode(genSource.getNodeA(), instanceProducer);
 		myNodeB = setupNotationNode(genSource.getNodeB(), instanceProducer);
 
 		myLink = NotationFactory.eINSTANCE.createEdge();
 		myCanvas.getPersistedEdges().add(myLink);
 		
 		//myNode.setVisualID(genSource.getGenNode().getVisualID());
 		TypeLinkModelFacet mf = (TypeLinkModelFacet) genSource.getLinkC().getModelFacet();
 		EObject linkElement = instanceProducer.createInstance(mf.getMetaClass());
		instanceProducer.setFeatureValue(myNodeA.getElement(), linkElement, mf.getContainmentMetaFeature());
		instanceProducer.setFeatureValue(linkElement, myNodeB.getElement(), mf.getTargetMetaFeature());
 		myLink.setElement(linkElement);
 		myLink.setType(String.valueOf(genSource.getLinkC().getVisualID()));
 		myLink.setSource(myNodeA);
 		myLink.setTarget(myNodeB);
 		
 		myLink.setBendpoints(NotationFactory.eINSTANCE.createRelativeBendpoints());
 		
 		//myLink.setVisualID(genSource.getGenLink().getVisualID());
 
 		myCanvas.setType(genSource.getGenDiagram().getEditorGen().getDomainGenModel().getModelName());
 
 		/*
 		Object nc = diagramElement.eGet(genSource.getGenNode().getContainmentMetaFeature().getEcoreFeature());
 		assert nc instanceof EList;
 		((EList) nc).add(nodeElement);
 		Object lc = nodeElement.eGet(genSource.getGenLink().getContainmentMetaFeature().getEcoreFeature());
 		if (lc instanceof EList) {
 			((EList) lc).add(linkElement);
 		} else {
 			nodeElement.eSet(genSource.getGenLink().getContainmentMetaFeature().getEcoreFeature(), linkElement);
 		}
 		*/
 	}
 	
 	private Node setupNotationNode(GenNode genNode, DomainInstanceProducer instanceProducer){
 		Node result = NotationFactory.eINSTANCE.createNode();
 		myCanvas.getPersistedChildren().add(result);
 		EObject nodeElement = instanceProducer.createInstance(genNode.getDomainMetaClass());
 		instanceProducer.setFeatureValue(myDiagramElement, nodeElement, genNode.getModelFacet().getContainmentMetaFeature());
 		result.setElement(nodeElement);
 		result.setType(String.valueOf(genNode.getVisualID()));
 
 		result.getStyles().add(NotationFactory.eINSTANCE.createShapeStyle());
 		Bounds b = NotationFactory.eINSTANCE.createBounds();
 		b.setWidth(0);
 		b.setHeight(0);
 		result.setLayoutConstraint(b);
 		
 		return result;
 	}
 	
 	private void saveDiagramFile(String editingDomainId){
         TransactionalEditingDomain ted = DiagramEditingDomainFactory.getInstance().createEditingDomain();
         ted.setID(editingDomainId);
 		ResourceSet rs = ted.getResourceSet();
 		URI uri = URI.createURI("uri://fake/z"); //$NON-NLS-1$
 		Resource r = rs.getResource(uri, false);
 		if (r == null) {
 			r = rs.createResource(uri);
 		}
 		
 		final Resource diagramFile = r;
 		AbstractEMFOperation operation = new AbstractEMFOperation(ted, "") { //$NON-NLS-1$			
 			protected IStatus doExecute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 				diagramFile.getContents().clear();				
 				diagramFile.getContents().add(getCanvas());
 				diagramFile.getContents().add(getDiagramElement());					
 				return Status.OK_STATUS;
 			};
 		};
 		try {
 			OperationHistoryFactory.getOperationHistory().execute(operation,
 					new NullProgressMonitor(), null);
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 			Assert.fail("Failed to set diagram resource contents"); //$NON-NLS-1$
 		}
 	}
 
 	public final Diagram getCanvas() {
 		return myCanvas;
 	}
 
 	public final Node getNodeA() {
 		return myNodeA;
 	}
 	
 	public final Node getNodeB() {
 		return myNodeB;
 	}
 
 	public Edge getLink() {
 		return myLink;
 	}
 	
 	protected EObject getDiagramElement(){
 		return myDiagramElement;
 	}
 
 	protected interface DomainInstanceProducer {
 		EObject createInstance(GenClass genClass);
 		void setFeatureValue(EObject src, EObject value, GenFeature genFeature);
 	}
 	
 	private static class NaiveDomainInstanceProducer implements DomainInstanceProducer {
 		public EObject createInstance(GenClass genClass) {
 			return createInstance(genClass.getEcoreClass());
 		}
 
 		public void setFeatureValue(EObject src, EObject value, GenFeature genFeature) {
 			EStructuralFeature feature = genFeature.getEcoreFeature();
 			if (genFeature.isListType()) {
 				Collection result = (Collection) src.eGet(feature);
 				result.add(value);
 			} else {
 				src.eSet(feature, value);
 			}
 		}
 
 		public EObject createInstance(EClass eClass) {
 			return eClass.getEPackage().getEFactoryInstance().create(eClass);
 		}
 	}
 	private static class CoolDomainInstanceProducer implements DomainInstanceProducer {
 		private final Bundle bundle;
 		public EObject createInstance(GenClass genClass) {
 			try {
 				Class factoryClass = getFactoryClass(genClass);
 				Method m = factoryClass.getMethod("create" + genClass.getName(), new Class[0]);
 				return (EObject) m.invoke(getInstance(factoryClass), new Object[0]);
 			} catch (NoSuchFieldException ex) {
 				Assert.fail(ex.getMessage());
 			} catch (NoSuchMethodException ex) {
 				Assert.fail(ex.getMessage());
 			} catch (InvocationTargetException ex) {
 				Assert.fail(ex.getMessage());
 			} catch (IllegalAccessException ex) {
 				Assert.fail(ex.getMessage());
 			} catch (ClassNotFoundException ex) {
 				Assert.fail(ex.getMessage());
 			}
 			Assert.fail();
 			return null;
 		}
 		public void setFeatureValue(EObject src, EObject value, GenFeature genFeature) {
 			try {
 				Class packageClass = getPackageClass(genFeature);
 				Method featureAccessor = packageClass.getMethod("get" + genFeature.getFeatureAccessorName(), new Class[0]);
 				EStructuralFeature feature = (EStructuralFeature) featureAccessor.invoke(getInstance(packageClass), new Object[0]);
 				if (genFeature.isListType()) {
 					Collection result = (Collection) src.eGet(feature);
 					result.add(value);
 				} else {
 					src.eSet(feature, value);
 				}
 			} catch (ClassNotFoundException ex) {
 				Assert.fail(ex.getMessage());
 			} catch (SecurityException ex) {
 				Assert.fail(ex.getMessage());
 			} catch (NoSuchMethodException ex) {
 				Assert.fail(ex.getMessage());
 			} catch (IllegalArgumentException ex) {
 				Assert.fail(ex.getMessage());
 			} catch (IllegalAccessException ex) {
 				Assert.fail(ex.getMessage());
 			} catch (InvocationTargetException ex) {
 				Assert.fail(ex.getMessage());
 			} catch (NoSuchFieldException ex) {
 				Assert.fail(ex.getMessage());
 			}
 		}
 		private Class getFactoryClass(GenClass genClass) throws ClassNotFoundException {
 			return bundle.loadClass(genClass.getGenPackage().getQualifiedFactoryInterfaceName());
 		}
 		private Object getInstance(Class interfaceClass) throws NoSuchFieldException, IllegalAccessException {
 			return interfaceClass.getField("eINSTANCE").get(null);
 		}
 		private Class getPackageClass(GenFeature genFeature) throws ClassNotFoundException {
 			return bundle.loadClass(genFeature.getGenPackage().getQualifiedPackageInterfaceName());
 		}
 		public CoolDomainInstanceProducer(Bundle b) {
 			bundle = b;
 		}
 
 	}
 }
