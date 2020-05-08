 package org.eclipse.bpel.ui;
 
 import org.eclipse.bpel.model.Process;
 import org.eclipse.bpel.model.impl.ExtensibilityElementImpl;
 import org.eclipse.bpel.model.impl.ExtensibleElementImpl;
 import org.eclipse.bpel.model.util.BPELConstants;
 import org.eclipse.bpel.ui.commands.util.UpdateModelCommand;
 import org.eclipse.bpel.ui.util.BPELEditorUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 
 import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.wsdl.internal.impl.WSDLElementImpl;
 import org.eclipse.wst.wsdl.internal.impl.XSDSchemaExtensibilityElementImpl;
 import org.eclipse.wst.xsd.ui.internal.util.ModelReconcileAdapter;
 import org.eclipse.xsd.XSDConcreteComponent;
 import org.eclipse.xsd.impl.XSDSchemaImpl;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 class BPELModelReconcileAdapter extends ModelReconcileAdapter {
 	protected Process process;
 	protected Resource bpelResource;
 	private BPELEditor fEditor;
 
 	public BPELModelReconcileAdapter(Document document, Process process,
 			Resource bpelResource, BPELEditor editor) {
 		super(document);
 		this.process = process;
 		this.bpelResource = bpelResource;
 		this.fEditor = editor;
 	}
 
 	// This method is clever enough to deal with 'bad' documents that happen
 	// to have more than one root element. It picks of the first 'matching'
 	// element.
 	//
 	// TODO (cs) why aren't we calling this from the WSDLModelAdapter when the
 	// model is initialized?
 	//
 	private Element getProcessElement(Document document) {
 		Element processElement = null;
 		for (Node node = document.getFirstChild(); node != null; node = node
 				.getNextSibling()) {
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 				Element element = (Element) node;
 				if (BPELEditorUtil.getInstance().getBPELType(element).equals(
 						BPELConstants.ND_PROCESS)) {
 					processElement = element;
 					break;
 				}
 			}
 		}
 		return processElement;
 	}
 
 	protected void handleNodeChanged(Node node) {
 		if (node instanceof Element
 				&& !BPELConstants.ND_LITERAL.equals(node.getLocalName())) {
 			reconcileModelObjectForElement((Element) node);
 		} else if (node instanceof Document) {
 			Document document = (Document) node;
 			Element processElement = getProcessElement(document);
 			if (processElement != null) {
 				process.setElement(processElement);
 				reconcileModelObjectForElement(processElement);
 			} else {
 				// TODO: (DU) what to do here?
 			}
 		} else if (node.getNodeType() == Node.CDATA_SECTION_NODE
 				|| BPELConstants.ND_LITERAL.equals(node.getLocalName())) {
 			reconcileModelObjectForElement((Element) node.getParentNode());
 		}
 	}
 
	private void reconcileModelObjectForElement(Element elementCandidate) {
		Object modelObjectCandidate = BPELEditorUtil.getInstance()
				.findModelObjectForElement(process, elementCandidate);
		
		// Get out of possible nested XML within the literal
		while (modelObjectCandidate == null && elementCandidate.getParentNode() != null) {
			elementCandidate = (Element)elementCandidate.getParentNode();
			modelObjectCandidate = BPELEditorUtil.getInstance().findModelObjectForElement(process, elementCandidate);
		}
		
 		// Wrap changes in source tab to the Command
		if (modelObjectCandidate != null) {
			final Object modelObject = modelObjectCandidate;
			final Element element = elementCandidate; 
 			UpdateModelCommand cmd = new UpdateModelCommand((EObject) modelObject, "Change text"){
 				@SuppressWarnings("restriction")
 				@Override
 				public void doExecute() {
 					if (modelObject instanceof ExtensibleElementImpl) {
 						((ExtensibleElementImpl) modelObject).elementChanged(element);
 					} else if (modelObject instanceof ExtensibilityElementImpl) {
 						((ExtensibilityElementImpl) modelObject)
 								.elementChanged(element);
 					} else if (modelObject instanceof XSDSchemaExtensibilityElementImpl) {
 						XSDSchemaExtensibilityElementImpl ee = (XSDSchemaExtensibilityElementImpl) modelObject;
 						((XSDSchemaImpl) ee.getSchema()).elementChanged(element);
 						ee.elementChanged(element);
 					} else if (modelObject instanceof WSDLElementImpl) {
 						((WSDLElementImpl) modelObject).elementChanged(element);
 					} else if (modelObject instanceof XSDConcreteComponent) {
 						((XSDConcreteComponent) modelObject).elementChanged(element);
 					}
 				}
 			};
 			
 			fEditor.getCommandFramework().execute(cmd);
 		}
 	}
 
 	public void modelDirtyStateChanged(IStructuredModel model, boolean isDirty) {
 		if (!isDirty) {
 			// cs : At this time (when a save occurs) it's a good opportunity
 			// to update the model to ensure it's in sync with the source.
 			// That way if the incremental sync between DOM and model has gotten
 			// the model out of whack we'll be able to put things right at this
 			// point.
 			//   
 			// TODO (cs) need to do more to ensure model is sync'd up properly
 
 			// FIXME uncomment
 			// ((ProcessImpl)process).reconcileReferences(true);
 		}
 	}
 
 	public void handleNotifyChange(INodeNotifier notifier, int eventType,
 			Object feature, Object oldValue, Object newValue, int index) {
 		Node node = (Node) notifier;
 		switch (eventType) {
 		case INodeNotifier.ADD: {
 			if (newValue instanceof Element) {
 				Element element = (Element) newValue;
 				adapt(element);
 			}
 			break;
 		}
 		case INodeNotifier.REMOVE: {
 			break;
 		}
 		case INodeNotifier.CHANGE:
 		case INodeNotifier.STRUCTURE_CHANGED: {
 			handleNodeChanged(node);
 			break;
 		}
 		case INodeNotifier.CONTENT_CHANGED: {
 			handleNodeChanged(node);
 			break;
 		}
 		}
 	}
 }
