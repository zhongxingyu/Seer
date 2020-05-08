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
 
 package org.jboss.tools.jbpm.convert.b2j.translate;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.jboss.tools.jbpm.convert.bpmnto.BpmnToPlugin;
 import org.jboss.tools.jbpm.convert.bpmnto.translate.BPMNTranslator;
 import org.jboss.tools.jbpm.convert.bpmnto.util.DomXmlWriter;
 import org.jboss.tools.jbpm.convert.b2j.messages.B2JMessages;
 import org.jboss.tools.jbpm.convert.b2j.translate.TranslateHelper;
 
 /**
  * @author Grid Qian
  * 
  *         this is a translator for bpmn ->jpdl
  */
 public class BPMN2JPDL extends BPMNTranslator {
 
 	Document bpmnDocument;
 	List<Document> processDefs = new ArrayList<Document>();
 
 	Map<String, Element> map = new HashMap<String, Element>();
 
 	public Map<String, Element> getMap() {
 		return map;
 	}
 
 	public void setMap(Map<String, Element> map) {
 		this.map = map;
 	}
 
 	public BPMN2JPDL() {
 	}
 
 	public BPMN2JPDL(String bpmnFileName, String bpmnFilePath,
 			Document bpmnDocument) {
 		this(bpmnFileName, bpmnFilePath, null, bpmnDocument);
 	}
 
 	public BPMN2JPDL(String bpmnFileName, String bpmnFilePath,
 			List<String> poolIDList, Document bpmnDocument) {
 		super(bpmnFileName, bpmnFilePath, poolIDList);
 		this.bpmnDocument = bpmnDocument;
 	}
 
 	/*
 	 * Translate a bpmn diagram to string[]. Every string is jpdl process
 	 * definition
 	 */
 	public String[] translateToStrings() {
 		this.translateDiagram();
 		String[] strForProcessDefs = new String[processDefs.size()];
 		int i = 0;
 		for (Document def : processDefs) {
 			try {
 				strForProcessDefs[i] = DomXmlWriter.toString(def);
 			} catch (IOException e) {
 				this.errors
 						.add(B2JMessages.Translate_Error_JpdlFile_CanNotGenerate
 								+ e.getMessage());
 			}
 			i++;
 		}
 
 		return strForProcessDefs;
 	}
 
 	/*
 	 * Translate a bpmn diagram to file[]. Every file is a jpdl file
 	 */
 	public void translateToFiles(String fileLocation) {
 		String[] strForProcessDefs = translateToStrings();
 		String[] jpdlFileNames = new String[processDefs.size()];
 
 		int i = 0;
 		for (Document def : processDefs) {
 			jpdlFileNames[i] = def.getName();
 			i++;
 		}
 
 		try {
 			TranslateHelper.createFiles(fileLocation, bpmnFileName,
 					strForProcessDefs, jpdlFileNames,
 					B2JMessages.Jpdl_Process_Definition_Name, false);
 		} catch (Exception e) {
 			errors.add(B2JMessages.Translate_Error_JpdlFile_CanNotWrite
 					+ e.getMessage());
 		}
 
 		if (errors.size() != 0) {
 			for (String str : errors) {
 				BpmnToPlugin.getDefault().logError(str);
 			}
 		}
 
 		if (warnings.size() != 0) {
 			for (String str : warnings) {
 				BpmnToPlugin.getDefault().logWarning(str);
 			}
 		}
 	}
 
 	/*
 	 * Translate a bpmn diagram Domument tree to some jpdl process Dom trees
 	 */
 	public void translateDiagram() {
 
 		// set the namemap = null
 		TranslateHelper.setNameMap(new HashMap<String, Integer>());
 
 		Element diagram = bpmnDocument.getRootElement();
 		if (this.poolIDList == null || this.poolIDList.size() == 0) {
 			for (Object pool : diagram
 					.elements(B2JMessages.Bpmn_Pool_Element_Name)) {
 				translateGraph((Element) pool);
 			}
 		} else {
 			for (Object pool : diagram
 					.elements(B2JMessages.Bpmn_Pool_Element_Name)) {
 				if (this.poolIDList.contains(((Element) pool)
 						.attributeValue(B2JMessages.Bpmn_Element_ID))) {
 					translateGraph((Element) pool);
 				}
 			}
 		}
 	}
 
 	/*
 	 * Translate a bpmn pool or subprocess to a jpdl process Dom tree
 	 */
 	private Element translateGraph(Element graph) {
 		Document processDef = TranslateHelper.createJpdlDomTree(true);
 		Element processRoot = processDef.getRootElement();
 
 		DomXmlWriter.addAttribute(processRoot, B2JMessages.Dom_Element_Name,
 				TranslateHelper.generateProcessName(graph));
 		processDef.setName(processRoot
 				.attributeValue(B2JMessages.Dom_Element_Name));
 
 		map.put(graph.attributeValue(B2JMessages.Dom_Element_ID)
 				+ B2JMessages.Bpmn_Pool_Element_Name, processRoot);
 
 		for (Object activity : graph.elements()) {
 			if (B2JMessages.Bpmn_Vertice_Element_Name
 					.equals(((Element) activity).getName())) {
 				translateActivity(((Element) activity), processRoot);
 			}
 		}
 		translateSequenceFlows(graph, processRoot);
 		processDefs.add(processDef);
 
 		return processRoot;
 	}
 
 	/*
 	 * Translate a bpmn activity to a jpdl node according to activity type
 	 */
 	private void translateActivity(Element activity, Element processRoot) {
 		String type = activity
 				.attributeValue(B2JMessages.Bpmn_XmiType_Attribute_Name);
 		Element element = null;
 
 		// According to bpmn activity type, map to different jpdl node
 		// Some type can not be supported by this translation, we give
 		// a warining message for it.
 		if ("bpmn:Activity".equals(type)) {
 			String activityType = activity
 					.attributeValue(B2JMessages.Bpmn_ActivityType_Attribute_Name);
 			if (activityType == null || "Task".equals(activityType)) {
 				element = DomXmlWriter.addElement(processRoot,
 						B2JMessages.Jpdl_Node_Element_Name);
 			} else if ("EventStartEmpty".equals(activityType)
 					|| "EventStartMessage".equals(activityType)
 					|| "EventStartRule".equals(activityType)
 					|| "EventStartTimer".equals(activityType)
 					|| "EventStartLink".equals(activityType)
 					|| "EventStartMultiple".equals(activityType)
 					|| "EventStartSignal".equals(activityType)) {
 				element = DomXmlWriter.addElement(processRoot,
 						B2JMessages.Jpdl_Start_Element_Name);
 				if (!"EventStartEmpty".equals(activityType)) {
 					warnings
 							.add(B2JMessages.Translate_Warning_Bpmn_Element_Type
 									+ activityType);
 				}
 			} else if ("EventIntermediateEmpty".equals(activityType)
 					|| "EventIntermediateMessage".equals(activityType)
 					|| "EventIntermediateTimer".equals(activityType)
 					|| "EventIntermediateError".equals(activityType)
 					|| "EventIntermediateCompensation".equals(activityType)
 					|| "EventIntermediateRule".equals(activityType)
 					|| "EventIntermediateMultiple".equals(activityType)
 					|| "EventIntermediateCancel".equals(activityType)
 					|| "EventIntermediateLink".equals(activityType)
 					|| "EventIntermediateSignal".equals(activityType)) {
 
 				element = DomXmlWriter.addElement(processRoot,
 						B2JMessages.Jpdl_State_Element_Name);
 				if (!"EventIntermediateEmpty".equals(activityType)) {
 					warnings
 							.add(B2JMessages.Translate_Warning_Bpmn_Element_Type
 									+ activityType);
 				}
 			} else if ("EventEndEmpty".equals(activityType)
 					|| "EventEndMessage".equals(activityType)
 					|| "EventEndError".equals(activityType)
 					|| "EventEndCompensation".equals(activityType)
 					|| "EventEndTerminate".equals(activityType)
 					|| "EventEndLink".equals(activityType)
 					|| "EventEndMultiple".equals(activityType)
 					|| "EventEndCancel".equals(activityType)
 					|| "EventEndSignal".equals(activityType)) {
 
 				element = DomXmlWriter.addElement(processRoot,
 						B2JMessages.Jpdl_End_Element_Name);
 				if (!"EventEndEmpty".equals(activityType)) {
 					warnings
 							.add(B2JMessages.Translate_Warning_Bpmn_Element_Type
 									+ activityType);
 				}
 			} else if ("GatewayDataBasedExclusive".equals(activityType)
 					|| "GatewayEventBasedExclusive".equals(activityType)
 					|| "GatewayComplex".equals(activityType)) {
 				element = DomXmlWriter.addElement(processRoot,
 						B2JMessages.Jpdl_Decision_Element_Name);
 				if (!"GatewayDataBasedExclusive".equals(activityType)) {
 					warnings
 							.add(B2JMessages.Translate_Warning_Bpmn_Element_Type
 									+ activityType);
 				}
 			} else if ("GatewayParallel".equals(activityType)
 					|| "GatewayDataBasedInclusive".equals(activityType)) {
 				if (activity
 						.attributeValue(B2JMessages.Bpmn_InFlow_Attribute_Name) == null
 						|| activity.attributeValue(
 								B2JMessages.Bpmn_InFlow_Attribute_Name).split(
 								B2JMessages.Space).length == 1) {
 					element = DomXmlWriter.addElement(processRoot,
 							B2JMessages.Jpdl_Fork_Element_Name);
 				} else {
 					element = DomXmlWriter.addElement(processRoot,
 							B2JMessages.Jpdl_Join_Element_Name);
 				}
 				if (!"GatewayDataBasedInclusive".equals(activityType)) {
 					warnings
 							.add(B2JMessages.Translate_Warning_Bpmn_Element_Type
 									+ activityType);
 				}
 			}
 		} else if ("bpmn:SubProcess".equals(type)) {
 			element = DomXmlWriter.addElement(processRoot,
 					B2JMessages.Jpdl_ProcessState_Element_Name);
 			translateSubprocess(activity, element);
 		}
 
 		if (!TranslateHelper.check_mapElementName(activity, element)) {
 			warnings.add(B2JMessages.Translate_Warning_Bpmn_Element_Name
 					+ activity.attributeValue(B2JMessages.Bpmn_Element_ID));
 		}
 		map.put(activity.attributeValue(B2JMessages.Dom_Element_ID), element);
 
 		// If bpmn activity is loop type, then create a structure to mock it.
 		if ("true".equals(activity
 				.attributeValue(B2JMessages.Bpmn_Looping_Attribute_Name))) {
 			createMockLoop(activity, element);
 		}
 	}
 
 	/*
 	 * Translate a bpmn subprocess to a jpdl process-state and a new jpdl process
 	 * definition
 	 */
 	private void translateSubprocess(Element subProcess, Element element) {
 		Element processRoot = translateGraph(subProcess);
 		Element ele = DomXmlWriter.addElement(element,
 				B2JMessages.Jpdl_SubProcess_Element_Name);
 		DomXmlWriter.mapAttribute(ele, B2JMessages.Dom_Element_Name,
 				processRoot);
 
 		// translate the transaction of subprocess
 		Element eAnnot = subProcess
 				.element(B2JMessages.Bpmn_EAnnotations_Element_Name);
 		if (eAnnot != null) {
 			Element details = eAnnot
 					.element(B2JMessages.Bpmn_Details_Element_Name);
 			if (details != null
 					&& "true"
 							.equals(details
 									.attributeValue(B2JMessages.Bpmn_Value_Attribute_Name))) {
 				translateTransaction(processRoot);
 			}
 		}
 	}
 
 	/*
 	 * translate a transaction of sub process
 	 */
 	private void translateTransaction(Element processRoot) {
 		List<Element> lastEleList = TranslateHelper.locateLastElements(processRoot);
 
 		if (lastEleList.size() == 0) {
 			return;
 		}
 		// create a decision
 		Element decision = DomXmlWriter.addElement(processRoot,
 				B2JMessages.Jpdl_Decision_Element_Name);
 
 		DomXmlWriter.addAttribute(decision, B2JMessages.Dom_Element_Name,
 				B2JMessages.Jpdl_Element_Successful_Name);
 		// get bpmn id from map
 		String bpmnId = null;
 		for (String key : map.keySet()) {
 			if (map.get(key) == lastEleList.get(0)) {
 				bpmnId = key;
 				break;
 			}
 		}
 		map.put(bpmnId + B2JMessages.Jpdl_Element_Decision_Suffix, decision);
 
 		// create a transition from element to decision
 		for (Element ele : lastEleList) {
 			Element transition = DomXmlWriter.addElement(ele,
 					B2JMessages.Jpdl_Transition_Element);
 			transition.addAttribute(B2JMessages.Dom_Element_Name, ele
 					.attributeValue(B2JMessages.Dom_Element_Name)
 					+ B2JMessages.To + B2JMessages.Jpdl_Decision_Element_Name);
 			transition.addAttribute(B2JMessages.To, decision
 					.attributeValue(B2JMessages.Dom_Element_Name));
 		}
 
 		// create a complete element
 		Element complete = DomXmlWriter.addElement(processRoot,
 				B2JMessages.Jpdl_Node_Element_Name);
 		DomXmlWriter.addAttribute(complete, B2JMessages.Dom_Element_Name,
 				B2JMessages.Jpdl_Element_Complete_Suffix);
 		map.put(bpmnId + B2JMessages.Jpdl_Element_Complete_Suffix, complete);
 
 		// create a cancel element
 		Element cancel = DomXmlWriter.addElement(processRoot,
 				B2JMessages.Jpdl_Node_Element_Name);
 		DomXmlWriter.addAttribute(cancel, B2JMessages.Dom_Element_Name,
 				B2JMessages.Jpdl_Element_Cancel_Suffix);
 		map.put(bpmnId + B2JMessages.Jpdl_Element_Cancel_Suffix, cancel);
 
 		// create transition from decision to complete element
 		Element toComplete = DomXmlWriter.addElement(decision,
 				B2JMessages.Jpdl_Transition_Element);
 		toComplete.addAttribute(B2JMessages.Dom_Element_Name, "true");
 		toComplete.addAttribute(B2JMessages.To, complete
 				.attributeValue(B2JMessages.Dom_Element_Name));
 
 		// create transition from decision to cancel element
 		Element toCancel = DomXmlWriter.addElement(decision,
 				B2JMessages.Jpdl_Transition_Element);
 		toCancel.addAttribute(B2JMessages.Dom_Element_Name, "false");
 		toCancel.addAttribute(B2JMessages.To, cancel
 				.attributeValue(B2JMessages.Dom_Element_Name));
 	}
 	
 
 	/*
 	 * Translate bpmn sequenceflows to jpdl transitions
 	 */
 	private void translateSequenceFlows(Element graph, Element processRoot) {
 		for (Object edge : graph
 				.elements(B2JMessages.Bpmn_SequenceFlow_Element_Name)) {
 			translateSequenceFlow((Element) edge, processRoot);
 		}
 	}
 
 	/*
 	 * Translate a bpmn sequenceflow to a jpdl transition
 	 */
 	private void translateSequenceFlow(Element edge, Element processRoot) {
 
 		Element source = map.get(edge
 				.attributeValue(B2JMessages.Bpmn_FlowSource_Attribute_Name));
 
 		Element transition = null;
 		if ("true".equals(edge
 				.attributeValue(B2JMessages.Bpmn_FlowDefault_Attribute_Name))
 				&& source.element(B2JMessages.Jpdl_Transition_Element) != null) {
 			// move default transition to the first of transition list
 			transition = DomXmlWriter.addElement(source,
 					B2JMessages.Jpdl_Transition_Element, 0);
 		} else {
 			transition = DomXmlWriter.addElement(source,
 					B2JMessages.Jpdl_Transition_Element);
 		}
 
 		if (!TranslateHelper.check_mapElementName(edge, transition)) {
 			warnings.add(B2JMessages.Translate_Warning_Bpmn_Element_Name
 					+ edge.attributeValue(B2JMessages.Bpmn_Element_ID));
 		}
 		transition
 				.addAttribute(
 						B2JMessages.To,
 						map
 								.get(
 										edge
 												.attributeValue(B2JMessages.Bpmn_FlowTarget_Attribute_Name))
 								.attributeValue(B2JMessages.Dom_Element_Name));
 	}
 
 	/*
 	 * create a jpdl decision structure to map bpmn loop activity
 	 */
 	private void createMockLoop(Element activity, Element element) {
 
 		// create a decision
 		Element decision = DomXmlWriter.addElement(element.getParent(),
 				B2JMessages.Jpdl_Decision_Element_Name);
		String name = TranslateHelper.generateElementName(activity)
 				+ B2JMessages.Underline + B2JMessages.Loop_Decision;
 		DomXmlWriter.addAttribute(decision, B2JMessages.Dom_Element_Name, name);
 
 		// use the decision to replace the activity in the map
 		map.put(activity.attributeValue(B2JMessages.Dom_Element_ID), decision);
 		// add the activity to map
 		map.put(activity.attributeValue(B2JMessages.Dom_Element_ID)
 				+ B2JMessages.Bpmn_Vertice_Element_Name, element);
 
 		// create a transition from element to decision
 		Element first = DomXmlWriter.addElement(element,
 				B2JMessages.Jpdl_Transition_Element);
 		first.addAttribute(B2JMessages.Dom_Element_Name, B2JMessages.To
 				+ B2JMessages.Underline + name);
 		first.addAttribute(B2JMessages.To, decision
 				.attributeValue(B2JMessages.Dom_Element_Name));
 
 		// create a transition from decision to element
 		Element second = DomXmlWriter.addElement(decision,
 				B2JMessages.Jpdl_Transition_Element);
 		second.addAttribute(B2JMessages.Dom_Element_Name, B2JMessages.To
 				+ B2JMessages.Underline
 				+ element.attributeValue(B2JMessages.Dom_Element_Name));
 		second.addAttribute(B2JMessages.To, element
 				.attributeValue(B2JMessages.Dom_Element_Name));
 
 	}
 	
 	public List<Document> getProcessDefs() {
 		return processDefs;
 	}
 
 	public void setProcessDefs(List<Document> processDefs) {
 		this.processDefs = processDefs;
 	}
 
 }
