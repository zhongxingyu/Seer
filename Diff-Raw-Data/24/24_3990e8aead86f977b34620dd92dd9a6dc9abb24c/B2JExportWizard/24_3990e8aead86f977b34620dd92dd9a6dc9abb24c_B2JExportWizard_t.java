 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.jbpm.convert.b2j.wizard;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.dom4j.Document;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.osgi.util.NLS;
 import org.jboss.tools.jbpm.convert.b2j.messages.B2JMessages;
 import org.jboss.tools.jbpm.convert.b2j.translate.TranslateHelper;
 import org.jboss.tools.jbpm.convert.bpmnto.BpmnToPlugin;
 import org.jboss.tools.jbpm.convert.b2j.translate.BPMN2JPDL;
 import org.jboss.tools.jbpm.convert.b2j.translate.Constants;
 import org.jboss.tools.jbpm.convert.b2j.translate.GraphicalFileGenerator;
 import org.jboss.tools.jbpm.convert.bpmnto.util.BPMNToUtil;
 import org.jboss.tools.jbpm.convert.bpmnto.wizard.BpmnToWizard;
 
 /**
  * @author Grid Qian
  * 
  * the wizard for bpmn to jpdl translation
  */
 public class B2JExportWizard extends BpmnToWizard {
 
 	private List<String> strForProcessList = new ArrayList<String>();
 	private List<String> strForGpdList = new ArrayList<String>();
 	private List<String> generatedFoldersList = new ArrayList<String>();
 	private List<String> generatedGpdFoldersList = new ArrayList<String>();
 
 	public void createGeneratedFile(boolean isOverWrite) {
 		String[] jpdlFolderNames = new String[this.generatedFoldersList.size()];
 
 		String location = super.getContainerPath((IContainer) super
 				.getTargetLocationSelection().getFirstElement());
 
 		int i = 0;
 		if (this.strForProcessList.size() > 0) {
 			// get a jpdl folders array from jpdl folder list
 			for (String name : this.generatedFoldersList) {
 				jpdlFolderNames[i] = name;
 				i++;
 			}
 			
 			i = 0;
 			String[] strs = new String[strForProcessList.size()];
 			for (String pro : this.strForProcessList) {
 				strs[i] = pro;
 				i++;
 			}
 			try {
 				TranslateHelper.createFiles(location, bpmnFileName, strs,
 						jpdlFolderNames,
 						Constants.Jpdl_Process_Definition_Name, isOverWrite);
 			} catch (Exception e) {
 				BpmnToPlugin.getDefault().logError(e.getMessage());
 			}
 		}
 
 		if (this.strForGpdList.size() > 0) {
 			i = 0;
 			// get a gpd folders array from gpd folder list
 			for (String name : this.generatedGpdFoldersList) {
 				jpdlFolderNames[i] = name;
 				i++;
 			}
 			i = 0;
 			String[] strs = new String[strForGpdList.size()];
 			for (String pro : this.strForGpdList) {
 				strs[i] = pro;
 				i++;
 			}
 			try {
 				TranslateHelper.createFiles(location, bpmnFileName, strs,
 						jpdlFolderNames, Constants.Gpd_Definition_Name, false);
 			} catch (Exception e) {
 				BpmnToPlugin.getDefault().logError(e.getMessage());
 			}
 		}
 
 	}
 
 	public List<String> translateBpmnToStrings() {
 		List<String> warningList = new ArrayList<String>();
 		List<String> errorList = new ArrayList<String>();
 
 		Document bpmnDocument = null;
 		try {
 			bpmnDocument = BPMNToUtil.parse(bpmnFileParentPath, bpmnFileName);
 		} catch (Exception e) {
 			errorList.add(NLS.bind(B2JMessages.Translate_Error_File_CanNotRead,
 					bpmnFileName));
 		}
 
 		BPMN2JPDL translator = new BPMN2JPDL(bpmnFileName, bpmnFileParentPath,
 				poolIdList, bpmnDocument);
 
 		this.setStrForProcessList(Arrays
 				.asList(translator.translateToStrings()));
 
 		for (Document def : translator.getProcessDefs()) {
 			this.generatedFoldersList.add(def.getName());
 		}
 
 		warningList.addAll(translator.getWarnings());
 		errorList.addAll(translator.getErrors());
 		
 		
 
 		// generate jpdl gpd file from *.bpmn_diagram
 		Document bpmnDiagramDocument = null;
 		try {
 			bpmnDiagramDocument = BPMNToUtil.parse(bpmnFileParentPath,
 					TranslateHelper.getBpmnDiagramName(bpmnFileName));
 		} catch (Exception e) {
 			errorList.add(NLS.bind(B2JMessages.Translate_Error_File_CanNotRead,
 					TranslateHelper.getBpmnDiagramName(bpmnFileName)));
 		}
 
		if (bpmnDiagramDocument != null) {
			GraphicalFileGenerator generator = new GraphicalFileGenerator(
					bpmnDiagramDocument, translator.getMap(), bpmnFileParentPath,
					bpmnFileName);
	
			this.setStrForGpdList(Arrays.asList(generator.translateToStrings()));
				
			for (Document def : generator.getGpdDefs()) {
				this.generatedGpdFoldersList.add(def.getRootElement().attributeValue(Constants.Dom_Element_Name));
			}
 			
			warningList.addAll(generator.getWarnings());
			errorList.addAll(generator.getErrors());
 		}
 
 		List<String> list = new ArrayList<String>();
 		list.addAll(errorList);
 		list.addAll(warningList);
 
 		return list;
 	}
 
 	public List<String> getStrForProcessList() {
 		return strForProcessList;
 	}
 
 	public void setStrForProcessList(List<String> strForProcessList) {
 		this.strForProcessList = strForProcessList;
 	}
 
 	public List<String> getStrForGpdList() {
 		return strForGpdList;
 	}
 
 	public void setStrForGpdList(List<String> strForGpdList) {
 		this.strForGpdList = strForGpdList;
 	}
 
 	public List<String> getGeneratedFoldersList() {
 		return generatedFoldersList;
 	}
 
 	public void setGeneratedFoldersList(List<String> generatedFoldersList) {
 		this.generatedFoldersList = generatedFoldersList;
 	}
 }
