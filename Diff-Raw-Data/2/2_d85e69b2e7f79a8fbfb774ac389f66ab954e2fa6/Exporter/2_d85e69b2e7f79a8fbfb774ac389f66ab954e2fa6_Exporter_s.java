 /*******************************************************************************
  * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
  * as represented by the Administrator of the National Aeronautics and Space 
  * Administration. All rights reserved.
  *
  * The MCT platform is licensed under the Apache License, Version 2.0 (the 
  * "License"); you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
  * License for the specific language governing permissions and limitations under 
  * the License.
  *
  * MCT includes source code licensed under additional open source licenses. See 
  * the MCT Open Source Licenses file included with this distribution or the About 
  * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
  * information. 
  *******************************************************************************/
 
 package gov.nasa.arc.mct.importExport.provider;
 
 import gov.nasa.arc.mct.components.AbstractComponent;
 import gov.nasa.arc.mct.components.ExtendedProperties;
 import gov.nasa.arc.mct.components.ModelStatePersistence;
 import gov.nasa.arc.mct.gui.OptionBox;
 import gov.nasa.arc.mct.importExport.provider.generated.AssociatedComponentType;
 import gov.nasa.arc.mct.importExport.provider.generated.ComponentListType;
 import gov.nasa.arc.mct.importExport.provider.generated.ComponentRefType;
 import gov.nasa.arc.mct.importExport.provider.generated.ComponentType;
 import gov.nasa.arc.mct.importExport.provider.generated.NameValueType;
 import gov.nasa.arc.mct.importExport.provider.generated.ObjectFactory;
 import gov.nasa.arc.mct.services.internal.component.ComponentInitializer;
 import gov.nasa.jsc.mct.importExport.utilities.Utilities;
 import gov.nasa.jsc.mct.importExport.utilities.ValidationException;
 import gov.nasa.jsc.mct.importExport.utilities.XMLPersistence;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.JProgressBar;
 import javax.swing.SwingWorker;
 import javax.xml.bind.JAXBException;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Exporter extends SwingWorker<Void, Void> {
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(Exporter.class);
 	/** Schema version to be written to XML file **/
 	private static BigDecimal schemaVersion = new BigDecimal(1.0);
 	private DialogMgr dialogMgr = new DialogMgr(null);
 	/** Component selected by user to place the imported data under **/
 	private List<AbstractComponent> selectedComponents;
 	/** File to export data to **/
 	private File file;
 	/** Progress bar object **/
 	private JProgressBar progressBar;
 	/** JDialog containing progress bar **/
 	private JDialog jd;
 	/** Object factory for creating instances of the XML generated classes **/
 	private ObjectFactory objFactory;
 	private List<AbstractComponent> unexportableComps;
 	private int totalSize = 0;
 	private Integer currentCount = 0;
 	private int totalDepth = 0;
 
 	/**
 	 * Constructor for exporting data to an XML file.
 	 * 
 	 * @param file File to export data to
 	 * @param selectedComponents Components selected by user to export
 	 * @param progressBar Progress bar object
 	 * @param jd JDialog containing progress bar
 	 */
 	public Exporter(File file, List<AbstractComponent> selectedComponents,
 			JProgressBar progressBar, JDialog jd) {
 		this.file = file;
 		this.selectedComponents = selectedComponents;
 		this.progressBar = progressBar;
 		this.jd = jd;
 		objFactory = new ObjectFactory();
 		unexportableComps = new ArrayList<AbstractComponent>();
 	}
 
 	/**
 	 * Constructor with no progress bar
 	 * 
 	 * @param file File to export data to
 	 * @param selectedComponents Component selected by user to export
 	 */
 	public Exporter(File file, List<AbstractComponent> selectedComponents) {
 		this(file, selectedComponents, null, null);
 	}
 
 	/**
 	 * Convert the data to XML objects, as defined in the generated package.  Write the
 	 * data to an XML file.
 	 */
 	public void exportComponents() {
 		try {
 			if (file == null) {
 				dialogMgr.showMessageDialog("No file selected for export.", 
 						"Export Failed", OptionBox.ERROR_MESSAGE);
 				return;
 			} else if (selectedComponents == null || selectedComponents.size() == 0) {
 				dialogMgr.showMessageDialog("No data selected for export.",
 						"Export Failed", OptionBox.ERROR_MESSAGE);
 				return;
 			}
 
 			// Create ComponentListType from single AbstractComponent
 			ComponentListType xmlComponentList = createXmlComponentList(selectedComponents);
 
 			if (unexportableComps.size() > 0) {
 				int rt = showUnexportableMsg();
 				switch(rt){
 				case JOptionPane.OK_OPTION:
 					// Write the data to the file
 					marshal(xmlComponentList);
 				}
 
 			} else {
 				// Write the data to the file
 				marshal(xmlComponentList);
 			}
 		} catch (Throwable t) {
 			// Errors shouldn't make it this far, but just in case...
             LOGGER.error(t.toString());
             dialogMgr.showMessageDialog("Unexpected Error: " + t.toString(), 
             		"Export Failed", OptionBox.ERROR_MESSAGE);
 		}
 	}
 
 	/**
 	 * Write the data to the file
 	 * @param xmlComponentList
 	 */
 	private void marshal(ComponentListType xmlComponentList) {
 		try {
 			XMLPersistence.marshal(xmlComponentList, file);
 			String msg = "Data successfully exported to " + file;
 			dialogMgr.showMessageDialog(msg, "Export Sucessful", 
 					OptionBox.INFORMATION_MESSAGE);
 		} catch (IOException e) {
 			processException(e);
 		} catch (ValidationException e) {
 			processException(e);
 		}
 	}
 	
 	private void processException(Exception e) {
 		LOGGER.error(e.getMessage());
 		dialogMgr.showMessageDialog("Export of " + file
 				+ " failed: See the log file for details.", 
 				"Export Failed", OptionBox.ERROR_MESSAGE);
 	}
 
 	/**
 	 * If an AbstractComponent is exportable, it is converted to a ComponentListType.  
 	 * The ComponentListType class was generated by the xjc tool to match the structure 
 	 * of the schema.
 	 * 
 	 * @param component
 	 *            the component to be converted into a ComponentListType
 	 * @return the ComponentListType which was converted from a
 	 *         AbstractComponent
 	 */
 	private ComponentListType createXmlComponentList(List<AbstractComponent> components) {
 		ComponentListType xmlComponentList = objFactory.createComponentListType();
 
 		// Set the schema version
 		xmlComponentList.setSchemaVersion(schemaVersion);
 		
 		// Estimate total number of components for use with the progress bar
 		for (AbstractComponent component : components) {
 	  	    totalSize = totalSize + estimateTotalSize(component);
 		}
 
 		for (AbstractComponent component : components) {
 			if (isExportable(component)) {
 				// Create the ComponentType's and add them to the ComponentListType
 				createXmlComponent(component, xmlComponentList, true);
 			} else {
 				unexportableComps.add(component);
 				LOGGER.error("Component is not exportable: "
 						+ component.getComponentId() + ", "
 						+ component.getDisplayName());
 			}
 		}
 
 		return xmlComponentList;
 	}
 
 	/**
 	 * Create a ComponentType from the given AbstractComponent. The ComponentType class 
 	 * was generated by the xjc tool to match the structure of the schema.
 	 * @param mctComp
 	 * @param xmlComponentList
 	 * @param topLevelComp
 	 */
 	private void createXmlComponent(AbstractComponent mctComp,
 			ComponentListType xmlComponentList, boolean topLevelComp) {
 
 		ComponentType xmlComp = objFactory.createComponentType();
 
 		// Set flag to show which component(s) are at the top level in the
 		// structure tree of the MCT component(s) which were chosen for export.
 		// Needed for import.
 		if (topLevelComp) {
 			xmlComp.setToplevel(true);
 		} else {
 			xmlComp.setToplevel(false);
 		}
 
 		xmlComp.setComponentId(mctComp.getId());
 		xmlComp.setComponentType(mctComp.getClass().getName());
 		xmlComp.setCreator(mctComp.getCreator());
 		xmlComp.setName(mctComp.getDisplayName());
 		xmlComp.setExternalKey(mctComp.getExternalKey());
 		xmlComp.setOwner(mctComp.getOwner());
 
 		// Convert from Date to XMLGregorianCalendar
 		XMLGregorianCalendar xmlGCDate = Utilities
 				.convertToXMLGregorianCalendar(mctComp.getCreationDate());
 		xmlComp.setCreationDate(xmlGCDate);
 
 		marshalModelState(mctComp, xmlComp);
 		marshalViewState(mctComp, xmlComp);
 
 		// xmlComp must be added to the list before the children are processed,
 		// in case there is a case of A being the parent of B being the parent of A.
 		xmlComponentList.getComponent().add(xmlComp);
 
 		// Recursively get children
 		if (mctComp.getComponents() != null) {
 			for (AbstractComponent child : mctComp.getComponents()) {
 				updateProgressBar();
 
 				if (isExportable(child)) {
 					if (exportAsReference(child)) {
 						// Add child to XML file as a reference
 						// (ComponentRefType)
 						ComponentRefType compReference = objFactory
 								.createComponentRefType();
 						compReference.setComponentId(child.getId());
 						compReference.setExternalKey(child.getExternalKey());
 						compReference.setClassType(child.getClass().getName());
 						xmlComp.getComponentRefs().add(compReference);
 
 					} else {
 						// Add child to XML file as a component (write all of
 						// its data)
 						if (isNewComponent(child, xmlComponentList)) {
 							// Recursive call to add child component as a
 							// top-level component in the XML file
 							createXmlComponent(child, xmlComponentList, false);
 						}
 
 						// Add child's component ID to associatedComponents
 						AssociatedComponentType assocComp = objFactory
 								.createAssociatedComponentType();
 						assocComp.setId(child.getId());
 						xmlComp.getAssociatedComponents().add(assocComp);
 					}
 
 				} else {
 					// Child is not exportable
 					unexportableComps.add(child);
 					LOGGER.info("Component is not exportable: "
 							+ child.getComponentId() + ", "
 							+ child.getDisplayName());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Export component as a reference if it has an external key
 	 * 
 	 * @param child component
 	 * @return
 	 */
 	private boolean exportAsReference(AbstractComponent child) {
 		if (child.getExternalKey() != null
 				&& !child.getExternalKey().equals("")) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Has the component already been processed for writing to the XML file?  If an
 	 * object occurs multiple places in the tree structure, it only needs to be written
 	 * to the XML file once.
 	 * @param child
 	 * @param componentList
 	 * @return
 	 */
 	private boolean isNewComponent(AbstractComponent child,
 			                       ComponentListType componentList) {
 		for (ComponentType component : componentList.getComponent()) {
 			if (child.getId().equalsIgnoreCase(component.getComponentId())) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Components are exportable if they are creatable or have an external key
 	 * 
 	 * @param component
 	 * @return
 	 */
 	private boolean isExportable(AbstractComponent component) {
 		if (Utilities.isCreateable(component) || 
		  (component.getExternalKey() != null & !component.getExternalKey().equals(""))) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Display message listing up to 500 of the components that cannot be exported.
 	 */
 	private int showUnexportableMsg() {
 		int count = 0;
 		StringBuilder msg = new StringBuilder(
 				"The following components can not be " + "exported: \n");
 		for (AbstractComponent comp : unexportableComps) {
 			msg.append(comp.getDisplayName());
 			msg.append("\n");
 			count++;
 			if (count >= 500) {
 				break;
 			}
 		}
 		return OptionBox.showOptionDialog(null, msg.toString(), "ALERT",
 				OptionBox.OK_CANCEL_OPTION, OptionBox.WARNING_MESSAGE, null, null, null);
 	}
 
 	/**
 	 * Saves the model state of a component. If this is the first time saving
 	 * the state, a model state DAO is created.
 	 * 
 	 * @param comp component whose state is to be saved
 	 * @param xmlComp XML component to save the data to
 	 */
 	private void marshalModelState(AbstractComponent comp, ComponentType xmlComp) {
 		ModelStatePersistence persister = comp
 				.getCapability(ModelStatePersistence.class);
 		if (persister != null) {
 			xmlComp.setModelState(persister.getModelState());
 		}
 	}
 
 	/**
 	 * Save the view state of the component.
 	 * 
 	 * @param comp component whose state is to be saved
 	 * @param xmlComp XML component to save the data to
 	 */
 	private void marshalViewState(AbstractComponent comp, ComponentType xmlComp) {
 		Map<String, ExtendedProperties> allViewProperties = comp.getCapability(
 				ComponentInitializer.class).getAllViewRoleProperties();
 		for (String key : allViewProperties.keySet()) {
 			setViewState(xmlComp, key, allViewProperties.get(key));
 		}
 	}
 
 	/**
 	 * Converts the view state pairs (viewType and properties) to a format
 	 * suitable to be written to an XML file (NameValueType) and adds it to the
 	 * XML's view state field.
 	 * 
 	 * @param xmlComp XML's component
 	 * @param viewType String class name view type.
 	 * @param properties Extended properties.
 	 */
 	private void setViewState(ComponentType xmlComp, String viewType,
 			ExtendedProperties properties) {
 
 		try {
 			NameValueType entry = objFactory.createNameValueType();
 			entry.setKey(viewType);
 			entry.setValue(XMLPersistence.marshal(properties));
 
 			xmlComp.getViewStates().add(entry);
 
 		} catch (JAXBException e) {
 			LOGGER.error(e.getMessage());
 			dialogMgr.showMessageDialog("Export of view state failed. " +
 					"See the log file for details.");
 		} catch (UnsupportedEncodingException e) {
 			LOGGER.error(e.getMessage());
 			dialogMgr.showMessageDialog("Export of view state failed. " +
 					"See the log file for details.");
 		} 
 	}
 
 	/**
 	 * Estimate number of components to process.  Set maximum depth to 3, in case of
 	 * cyclic data structures (A parent of B parent of A, etc.), since this method uses
 	 * recursion.
 	 * @param parentComp
 	 * @return
 	 */
 	private int estimateTotalSize(AbstractComponent parentComp) {
 		int rv = 0;
 		for (AbstractComponent comp : parentComp.getComponents()) {
 			if (totalDepth < 3) {
 			    totalDepth++;
 			    rv = rv + estimateTotalSize(comp);
 			} else {
 				return 0;
 			}
 		}
 		rv++;
 
 		return rv;
 	}
 
 	private void updateProgressBar() {
 		if (progressBar != null) {
 			float percentCount = (++currentCount / (float) totalSize)
 					* progressBar.getMaximum();
 			setProgress(Math.min((int) percentCount, progressBar.getMaximum()));
 		}
 	}
 
 	/*
 	 * @see javax.swing.SwingWorker#doInBackground()
 	 */
 	@Override
 	protected Void doInBackground() throws Exception {
 		setProgress(0);
 		exportComponents();
 
 		return null;
 	}
 
 	@Override
 	public void done() {
 		if (progressBar != null) {
 			progressBar.setValue(0);
 		}
 		if (jd != null) {
 			jd.setVisible(false);
 			jd.dispose();
 		}
 	}
 
 }
