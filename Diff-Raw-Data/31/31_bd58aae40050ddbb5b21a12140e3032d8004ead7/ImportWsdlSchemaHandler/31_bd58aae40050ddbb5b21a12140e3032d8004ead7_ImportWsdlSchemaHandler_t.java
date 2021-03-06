 /*
  * JBoss, Home of Professional Open Source.
 *
 * See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
 *
 * See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
 */
 package org.teiid.designer.modelgenerator.wsdl.ui.wizards.soap;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.xsd.XSDElementDeclaration;
 import org.eclipse.xsd.XSDSchema;
 import org.eclipse.xsd.XSDSimpleTypeDefinition;
 import org.eclipse.xsd.XSDTypeDefinition;
 import org.eclipse.xsd.impl.XSDElementDeclarationImpl;
 import org.eclipse.xsd.impl.XSDParticleImpl;
 
 import com.metamatrix.modeler.core.types.DatatypeConstants.RuntimeTypeNames;
 import com.metamatrix.modeler.internal.transformation.util.SqlConstants;
 import com.metamatrix.modeler.modelgenerator.wsdl.model.Model;
 import com.metamatrix.modeler.modelgenerator.wsdl.model.ModelGenerationException;
 import com.metamatrix.modeler.modelgenerator.wsdl.model.Operation;
 import com.metamatrix.modeler.modelgenerator.wsdl.model.Part;
 import com.metamatrix.modeler.modelgenerator.wsdl.schema.extensions.SOAPSchemaProcessor;
 import com.metamatrix.modeler.modelgenerator.wsdl.ui.internal.wizards.WSDLImportWizardManager;
 import com.metamatrix.modeler.schema.tools.model.schema.SchemaModel;
 import com.metamatrix.modeler.schema.tools.model.schema.SchemaObject;
 import com.metamatrix.modeler.schema.tools.model.schema.impl.BaseSchemaObject;
 import com.metamatrix.modeler.schema.tools.model.schema.impl.SimpleRelationship;
 import com.metamatrix.modeler.schema.tools.processing.SchemaProcessingException;
 import com.metamatrix.modeler.schema.tools.processing.SchemaProcessor;
 
 public class ImportWsdlSchemaHandler {
 	
 	WSDLImportWizardManager importManager;
 	OperationsDetailsPage operationsDetailsPage;
 
 	public ImportWsdlSchemaHandler(WSDLImportWizardManager manager, OperationsDetailsPage operationsDetailsPage) {
 		super();
 		this.importManager = manager;
 		this.operationsDetailsPage = operationsDetailsPage;
 	}
 	
 	public List<Object> getSchemaForSelectedOperation(final int type, ProcedureGenerator generator) {
 
 		Model wsdlModel = null;
 		Object elementDeclaration = null;
 		
 		try {
 			wsdlModel = importManager.getWSDLModel();
 		} catch (ModelGenerationException e) {
 			throw new RuntimeException(e);
 		}
 
 		XSDSchema[] schemas = wsdlModel.getSchemas();
 
 		Operation selectedOperation = generator.getOperation();
 		String partElementName = null;
 		Part[] partArray = null;
 
 		if (type == ProcedureInfo.REQUEST) {
 			if (selectedOperation.getInputMessage() != null) {
 				partArray = selectedOperation.getInputMessage().getParts();
 			}
 		} else {
 			if (selectedOperation.getOutputMessage() != null) {
 				partArray = selectedOperation.getOutputMessage().getParts();
 			}
 		}
 
 		List<Object> elementArrayList = new ArrayList<Object>();
 
 		for (Part part : partArray) {
 			partElementName = getPartElementName(part);
 			elementDeclaration = null;
 
 			boolean foundElement = false;
 
 			for (XSDSchema schema : schemas) {
 				EList<XSDTypeDefinition> types = schema.getTypeDefinitions();
 				for (XSDTypeDefinition xsdType : types) {
 					String elementName = xsdType.getName();
 					if (elementName.equals(partElementName)) {
 						elementDeclaration = xsdType;
 						foundElement = true;
 						elementArrayList.add(elementDeclaration);
 						break;
 					}
 				}
 
 				if (foundElement == true)
 					continue;
 
 				if (elementDeclaration == null) {
 
 					EList<XSDElementDeclaration> elements = schema.getElementDeclarations();
 					for (XSDElementDeclaration element : elements) {
 						String elementName = element.getName();
 						if (elementName.equals(partElementName)) {
 							if (element.getTypeDefinition() instanceof XSDSimpleTypeDefinition) {
 								elementDeclaration = element;
 							} else {
 								elementDeclaration = element.getTypeDefinition();
 							}
 
 							foundElement = true;
 							elementArrayList.add(elementDeclaration);
 							break;
 						}
 					}
 				}
 
 				// We already found our element. No need to look through anymore
 				// schemas
 				if (foundElement) {
 					foundElement = false;
 					break;
 				}
 			}
 
 		}
 
 		return elementArrayList;
 	}
 	
 
 	private String getPartElementName(Part part) {
 		String partElementName = null;
 		
 		partElementName = part.getTypeName();
 	    if (partElementName == null){
 			partElementName = part.getElementName();
 	    }
 		
 		return partElementName;
 	}
 
 	public String createRequestColumn(int type, IStructuredSelection selection, ProcedureInfo requestInfo) {
 	
 		Object obj = selection.getFirstElement();
 		String name = null;
 		String ns = null;
 		
 		if (obj instanceof XSDParticleImpl
 				&& ((XSDParticleImpl) obj).getContent() instanceof XSDElementDeclarationImpl) {
 			name = ((XSDElementDeclarationImpl) ((XSDParticleImpl) obj).getContent()).getName();
 			ns = ((XSDElementDeclarationImpl) ((XSDParticleImpl) obj).getContent()).getTargetNamespace();
 			if( type == ProcedureInfo.TYPE_BODY ) {
 				requestInfo.addBodyColumn(requestInfo.getUniqueBodyColumnName(name), false,RuntimeTypeNames.STRING, null, ns);
 			} else {
 				requestInfo.addHeaderColumn(requestInfo.getUniqueHeaderColumnName(name), false,RuntimeTypeNames.STRING, null, ns);
 			}
 			operationsDetailsPage.notifyColumnDataChanged();
 			return null;
 			
 		} else if (obj instanceof XSDElementDeclarationImpl){
 			name = ((XSDElementDeclarationImpl)obj).getName();
 			ns = ((XSDElementDeclarationImpl)obj).getTargetNamespace();
 			if( type == ProcedureInfo.TYPE_BODY ) {
 				requestInfo.addBodyColumn(requestInfo.getUniqueBodyColumnName(name), false,RuntimeTypeNames.STRING, null, ns);
 			} else {
 				requestInfo.addHeaderColumn(requestInfo.getUniqueHeaderColumnName(name), false,RuntimeTypeNames.STRING, null, ns);
 			}
 			operationsDetailsPage.notifyColumnDataChanged();
 			return null;
 		}
 		
 		return operationsDetailsPage.getSchemaLabelProvider().getText(obj);
 	}
 
 	public String createResponseColumn(int type, IStructuredSelection selection, ProcedureInfo responseInfo) {
 	
 		Object obj = selection.getFirstElement();
 		if (obj instanceof XSDParticleImpl && ((XSDParticleImpl) obj).getContent() instanceof XSDElementDeclarationImpl) {
 	
 			Model wsdlModel = null;
 			SchemaModel schemaModel;
 			XSDSchema[] schemas;
 	
 			try {
 				wsdlModel = operationsDetailsPage.getImportManager().getWSDLModel();
 			} catch (ModelGenerationException e) {
 				throw new RuntimeException(e);
 			}
 	
 			SchemaProcessor processor = new SOAPSchemaProcessor(null);
 			processor.representTypes(true);
 			processor.setNamespaces(wsdlModel.getNamespaces());
 			schemas = wsdlModel.getSchemas();
 			try {
 				processor.processSchemas(schemas);
 			} catch (SchemaProcessingException e) {
 				throw new RuntimeException(e);
 			}
 			schemaModel = processor.getSchemaModel();
 	
 			List<SchemaObject> elements = schemaModel.getElements();
 			String name = ((XSDElementDeclarationImpl) ((XSDParticleImpl) obj)
 					.getContent()).getName();
 			StringBuilder xpath = new StringBuilder();
 			String namespace = null;
 			String prefix = null;
 			StringBuilder parentXpath = new StringBuilder();
 			if (importManager.isMessageServiceMode()){
 				responseInfo.addNamespace(SqlConstants.ENVELOPE_NS_ALIAS, SqlConstants.ENVELOPE_NS);
 			}
 			for (SchemaObject schemaObject : elements) {
 				if (schemaObject.getName().equals(name)) {
 					getParentXpath(schemaObject, parentXpath);
 					xpath.append("/").append(schemaObject.getRelativeXpath()); //$NON-NLS-1$
 					namespace = schemaObject.getNamespace();
 					prefix = ((BaseSchemaObject) schemaObject)
 							.getNamespacePrefix();
 					if (namespace != null) {
 						responseInfo.addNamespace(
 								prefix, namespace);
 					}
 					responseInfo.setRootPath(parentXpath.toString());
 					
 					
 					// TODO: Make sure Root Path is set in the responseElementsInfoPanel on Refresh
 					//operationsDetailsPage.responseElementsInfoPanel.getRootPathText().setText(parentXpath.toString());
 				}
 			}
 			if( type == ProcedureInfo.TYPE_BODY ) {
				String pathPrefix = ""; //$NON-NLS-1$
				if (importManager.isMessageServiceMode()){
					pathPrefix = ResponseInfo.SOAPBODY_ROOTPATH;
				}
				responseInfo.addBodyColumn(responseInfo.getUniqueBodyColumnName(name), false,RuntimeTypeNames.STRING, null,pathPrefix+parentXpath.toString()+xpath.toString());
 			} else {
				String pathPrefix = ""; //$NON-NLS-1$
				if (importManager.isMessageServiceMode()){
					pathPrefix = ResponseInfo.SOAPHEADER_ROOTPATH;
				}
				responseInfo.addHeaderColumn(responseInfo.getUniqueHeaderColumnName(name), false,RuntimeTypeNames.STRING, null,pathPrefix+parentXpath.toString()+xpath.toString());
 			}
 			operationsDetailsPage.notifyColumnDataChanged();
 			return null;
 		}
 	
 		return operationsDetailsPage.getSchemaLabelProvider().getText(obj);
 	}
 
 	
 	private void getParentXpath(SchemaObject child, StringBuilder parentXpath) {
 		List<SimpleRelationship> parents = child.getParents();
 		for (SimpleRelationship parent : parents) {
 			parentXpath.append("/").append(parent.getParent().getSimpleName()); //$NON-NLS-1$
 		}
 	}
 }
