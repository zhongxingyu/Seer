 package org.iucn.sis.shared.api.utils;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.iucn.sis.client.api.caches.FieldWidgetCache.CreatesDisplay;
 import org.iucn.sis.shared.api.data.DisplayData;
 import org.iucn.sis.shared.api.data.DisplayDataProcessor;
 import org.iucn.sis.shared.api.data.FieldData;
 import org.iucn.sis.shared.api.data.LookupData;
 import org.iucn.sis.shared.api.data.TreeData;
 import org.iucn.sis.shared.api.data.TreeDataRow;
 import org.iucn.sis.shared.api.data.DisplayData.LookupDataContainer;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.displays.ClassificationScheme;
 import org.iucn.sis.shared.api.displays.Display;
 import org.iucn.sis.shared.api.displays.FieldDisplay;
 import org.iucn.sis.shared.api.displays.threats.Threats;
 import org.iucn.sis.shared.api.structures.BooleanRule;
 import org.iucn.sis.shared.api.structures.ContentRule;
 import org.iucn.sis.shared.api.structures.Rule;
 import org.iucn.sis.shared.api.structures.SelectRule;
 
 import com.google.gwt.core.client.GWT;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNode;
 import com.solertium.lwxml.shared.NativeNodeList;
 
 /**
  * Parses a field tag into the appropriate Display object.
  * 
  * @author adam.schwartz
  * 
  */
 @SuppressWarnings("unused")
 public class FieldParser implements CreatesDisplay {
 	private static final String BASIC_INFORMATION_TAG_NAME = "basicInformation";
 	private static final String ASSESSMENT_FIELD_TAG_NAME = "field";
 	private static final String STRUCTURES_DELIMETER_TAG_NAME = "structures";
 
 	private static final String ASSESSMENT_TREE_TAG_NAME = "tree";
 	private static final String TREE_ROOT_TAG_NAME = "treeRoot";
 
 	private static final String CANONICAL_NAME_TAG_NAME = "canonicalName";
 	private static final String DESCRIPTION_TAG_NAME = "description";
 	private static final String CLASS_OF_SERVICE_TAG_NAME = "classOfService";
 	private static final String LOCATION_TAG_NAME = "location";
 	private static final String LOOKUP_VALUE_TAG_NAME = "lookup";
 	private static final String REFERENCES_TAG_NAME = "references";
 	private static final String FIELD_DEFINITION_TAG_NAME = "definition";
 
 	protected Display doOperate(DisplayData currentDisplayData) {
 		if (currentDisplayData.getType().equalsIgnoreCase(DisplayData.FIELD)) {
 			FieldData currentFieldData = (FieldData) currentDisplayData;
 			
 			final FieldDisplay field = new FieldDisplay(currentFieldData);
 			try {
 				field.addStructure(DisplayDataProcessor.processDisplayStructure(currentDisplayData));
 			} catch (Throwable e) {
 				e.printStackTrace();
 				GWT.log("FieldParser Error", e);
 			}
 
 			return field;
 		} else if (currentDisplayData.getType().equalsIgnoreCase(DisplayData.TREE)) {
 			TreeData currentTreeData = (TreeData) currentDisplayData;
 
 			ClassificationScheme scheme = new ClassificationScheme(currentTreeData);
 			return scheme;
 		} else
 			return null;
 	}
 
 	public Display parseField(NativeDocument doc) {
 		Display display = null;
 		if ("fields".equals(doc.getDocumentElement().getNodeName())) {
 			final NativeNodeList nodes = doc.getDocumentElement().getChildNodes();
 			for (int i = 0; i < nodes.getLength() && display == null; i++) {
 				final NativeNode current = nodes.item(i);
 				if (NativeNode.TEXT_NODE != current.getNodeType() && current instanceof NativeElement) {
 					display = parseField((NativeElement)current);
 				}
 			}
 			return display;
 		}
 		else
 			display = parseField(doc.getDocumentElement());
 		
 		return display;
 	}
 
 	public Display parseField(NativeElement fieldElement) {
 		// Process the display objects
 		if (fieldElement.getNodeName().equalsIgnoreCase(ASSESSMENT_FIELD_TAG_NAME)) {
 			try {
 				return doOperate(processFieldTag(fieldElement));
 			} catch (Throwable e) {
 				e.printStackTrace();
 				Debug.println(
 						"Failed to process field " + fieldElement.getElementByTagName("canonicalName").getText());
 			}
 		} else if (fieldElement.getNodeName().equalsIgnoreCase("threats")) {
 			return new Threats(fieldElement);
 		} else {
 			try {
 				return doOperate(processTreeTags(fieldElement));
 			} catch (Throwable e) {
 				e.printStackTrace();
 				Debug.println(
 						"Failed to process classification scheme "
 								+ fieldElement.getElementByTagName("canonicalName").getText());
 			}
 		}
 
 		return null;
 	}
 
 	public DisplayData parseFieldData(NativeDocument doc) {
 		return parseFieldData(doc.getDocumentElement());
 	}
 
 	public DisplayData parseFieldData(NativeElement fieldElement) {
 		// Process the display objects
 		if (fieldElement.getNodeName().equalsIgnoreCase(ASSESSMENT_FIELD_TAG_NAME)) {
 			try {
 				return processFieldTag(fieldElement);
 			} catch (Exception e) {
 				e.printStackTrace();
 				Debug.println(
 						"Failed to process field " + fieldElement.getElementByTagName("canonicalName").getText());
 			}
 		} else {
 			try {
 				return processTreeTags(fieldElement);
 			} catch (Exception e) {
 				e.printStackTrace();
 				Debug.println(
 						"Failed to process classification scheme "
 								+ fieldElement.getElementByTagName("canonicalName").getText());
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Fills in the data arraylist of the currentField based on the Structure
 	 * 
 	 * @param NativeNodeList
 	 *            with XML data about structures
 	 * @return an ArrayList containing DisplayData for structures
 	 */
 	private static ArrayList<DisplayData> parseStructures(NativeNodeList structureTags, LookupDataContainer lookups) {
 		ArrayList<DisplayData> structureSet = new ArrayList<DisplayData>();
 		
 		DisplayData currentDisplayData;
 
 		for (int structs = 0; structs < structureTags.getLength(); structs++) {
 			NativeElement structureTag = structureTags.elementAt(structs);
 
 			if (structureTag == null || !structureTag.getNodeName().equalsIgnoreCase("structure"))
 				continue;
 
 			NativeElement current;
 			if (structureTag.getFirstChild().getNodeType() == XMLUtils.TEXT_NODE_TYPE)
 				current = structureTag.getChildNodes().elementAt(1);
 			else
 				current = structureTag.getChildNodes().elementAt(0);
 
 			String structureType = current.getNodeName();
 			String name = XMLUtils.getXMLAttribute(structureTag, "name", null);
 			String descript = XMLUtils.getXMLAttribute(structureTag, "description", null);
 			String isVisible = XMLUtils.getXMLAttribute(structureTag, "isVisible", null);
 			String style = XMLUtils.getXMLAttribute(structureTag, "style", null);
 			String id = XMLUtils.getXMLAttribute(structureTag, "id", null);
 
 			currentDisplayData = new FieldData();
 			currentDisplayData.setStructure(structureType);
 			if (descript != null)
 				currentDisplayData.setDescription(descript);
 			if (id != null)
 				currentDisplayData.setUniqueId(id);
 
 			currentDisplayData.setName(name);
 			currentDisplayData.setIsVisible(isVisible);
 			currentDisplayData.setStyle(style);
 
 			// Do the work based on the structure
 			if (structureType.equalsIgnoreCase(XMLUtils.TEXT_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.NUMBER_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.DATE_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.NARRATIVE_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.RICH_TEXT_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.JUSTIFICATION_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.IMAGE_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.FILE_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.BOOLEAN_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.BOOLEAN_UNKNOWN_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.OPTIONS_LIST)
 					|| structureType.equalsIgnoreCase(XMLUtils.NOTE_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.LABEL_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.REFERENCE_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.RED_LIST_CATEGORIES_CRITERIA)
 					|| structureType.equalsIgnoreCase(XMLUtils.EMPTY_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.BOOLEAN_RANGE_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.REGIONAL_EXPERT_QUESTIONS_STRUCTURE)) {
 				Map<String, String> data = new HashMap<String, String>();
 				data.put("restriction", XMLUtils.getXMLAttribute(current, "restriction", null));
 				data.put("style", XMLUtils.getXMLAttribute(current, "style", null));
 				data.put("value", XMLUtils.getXMLValue(current, current.getText()));
 				
 				currentDisplayData.setData(data);
 			}
 
 			else if (structureType.equalsIgnoreCase(XMLUtils.ONE_TO_MANY)) {
 				ArrayList<DisplayData> parsedStructs = parseStructures(current.getChildNodes(), lookups);
 				
 				if (parsedStructs.isEmpty())
 					currentDisplayData.setData(null);
 				else if (parsedStructs.size() == 1)
 					currentDisplayData.setData(parsedStructs.get(0));
 				else {
 					FieldData data = new FieldData();
 					data.setStructure(XMLUtils.STRUCTURE_COLLECTION);
 					data.setData(parsedStructs);
 					currentDisplayData.setData(data);
 				}
 			}
 
 			else if (structureType.equalsIgnoreCase(XMLUtils.CLASSIFICATION_SCHEME_STRUCTURE)) {
 				//FIXME: Why is this unused?
 				//NativeElement defaultStructureElement = current.getElementByTagName("defaultStructure");
 
 				NativeNodeList options = current.getElementsByTagName("option");
 				ArrayList<String> data = new ArrayList<String>();
 
 				for (int k = 0; k < options.getLength(); k++) {
 					data.add(options.elementAt(k).getText());
 				}
 
 				currentDisplayData.setData(data);
 			}
 
 			else if (structureType.equalsIgnoreCase(XMLUtils.RANGE_STRUCTURE)) {
 				NativeNodeList rangeList = current.getChildNodes();
 				ArrayList<String> data = new ArrayList<String>();
 
 				for (int k = 0; k < rangeList.getLength(); k++) {
 					if (rangeList.item(k).getNodeName().equalsIgnoreCase(XMLUtils.RANGE_LOW_GUESS))
 						data.add(XMLUtils.getXMLValue(rangeList.item(k), ""));
 					else if (rangeList.item(k).getNodeName().equalsIgnoreCase(XMLUtils.RANGE_HIGH_GUESS))
 						data.add(XMLUtils.getXMLValue(rangeList.item(k), ""));
 					/*
 					 * else if(
 					 * rangeList.item(k).getNodeName().equalsIgnoreCase(
 					 * XMLUtils.RANGE_BEST_GUESS) ) data.add(
 					 * XMLUtils.getXMLValue(rangeList.item(k), "") );
 					 */
 				}
 
 				currentDisplayData.setData(data);
 			}
 			else if (structureType.equalsIgnoreCase(XMLUtils.LIVELIHOODS)
 					|| structureType.equalsIgnoreCase(XMLUtils.USE_TRADE)) {
 				currentDisplayData.setData(lookups);
 			}
 			else if (structureType.equalsIgnoreCase(XMLUtils.MULTIPLE_SELECT_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.SINGLE_SELECT_STRUCTURE)
 					|| structureType.equalsIgnoreCase(XMLUtils.QUALIFIER_STRUCTURE) 
 					|| structureType.equalsIgnoreCase(XMLUtils.BAND_STRUCTURE)) {
 				
 				LookupData options = lookups.find(id);
 				if (options == null) {
					//Create from structure
 					options = new LookupData();
 					NativeNodeList selectOptions = current.getChildNodes();
 				
 					int optionCount = 0;
 					for (int k = 0; k < selectOptions.getLength(); k++) {
 						if (selectOptions.item(k).getNodeName().equalsIgnoreCase("option"))
 							options.addValue((optionCount++)+"", XMLUtils.getXMLValue(selectOptions.item(k), ""));
 	
 						if (selectOptions.item(k).getNodeName().equalsIgnoreCase("selected")) {
 							String selected = XMLUtils.getXMLValue(selectOptions.item(k), "");
 							if (selected.indexOf(',') != -1) {
 								/*
 								 * Really, this should not be the case, you should 
 								 * just use multiple nodes ... but to support legacy 
 								 * data...
 								 */
 								for (String token : selected.split(",")) {
 									options.addDefaultValue(token);
 								}
 							}
 							else
 								options.addDefaultValue(selected);
 						}
 					}
 				}
				else {
					//Looking for pre-selected options (defaults)
					NativeNodeList selectOptions = current.getChildNodes();
					
					for (int k = 0; k < selectOptions.getLength(); k++) {
						if (selectOptions.item(k).getNodeName().equalsIgnoreCase("selected")) {
							String selected = XMLUtils.getXMLValue(selectOptions.item(k), "");
							if (selected.indexOf(',') != -1) {
								/*
								 * Really, this should not be the case, you should 
								 * just use multiple nodes ... but to support legacy 
								 * data...
								 */
								for (String token : selected.split(",")) {
									options.addDefaultValue(token);
								}
							}
							else
								options.addDefaultValue(selected);
						}
					}
				}
 				
 				currentDisplayData.setData(options);
 			}
 
 			else if (structureType.equalsIgnoreCase(XMLUtils.MULTIPLE_TEXT_STRUCTURE)) {
 				ArrayList<String> data = new ArrayList<String>();
 				NativeNodeList textNodes = current.getChildNodes();
 
 				for (int i = 0; i < textNodes.getLength(); i++) {
 					NativeNode currentTextNode = textNodes.item(i);
 
 					if (currentTextNode.getNodeName().equalsIgnoreCase("text")) {
 						data.add(XMLUtils.getXMLValue(currentTextNode, ""));
 					}
 				}
 
 				currentDisplayData.setData(data);
 			}
 
 			else if (structureType.equalsIgnoreCase(XMLUtils.RELATED_STRUCTURE)) {
 				// Has 3 children I am interested in ...
 				/* 1 */FieldData dominantStructure = new FieldData();
 				/* 2 */ArrayList<DisplayData> dependentStructures = new ArrayList<DisplayData>();
 				/* 3 */ArrayList<Rule> rules = new ArrayList<Rule>();
 
 				NativeNodeList children = current.getChildNodes();
 
 				for (int i = 0; i < children.getLength(); i++) {
 					String key = children.item(i).getNodeName();
 					// Look for dominant
 					if (key.equalsIgnoreCase("dominantStructures")) {
 						ArrayList<DisplayData> dominantData = parseStructures(children.elementAt(i).getChildNodes(), lookups);
 						if (dominantData.size() == 1) {
 							dominantStructure.setStructure(((FieldData) dominantData.get(0)).getStructure());
 							dominantStructure.setData(((FieldData) dominantData.get(0)).getData());
 							dominantStructure.setDescription((((FieldData) dominantData.get(0)).getDescription()));
 							dominantStructure.setName(((FieldData) dominantData.get(0)).getName());
 							dominantStructure.setIsVisible(((FieldData) dominantData.get(0)).getIsVisible());
 							dominantStructure.setStyle(((FieldData) dominantData.get(0)).getStyle());
 							dominantStructure.setUniqueId(((FieldData) dominantData.get(0)).getUniqueId());
 						} else {
 							dominantStructure.setStructure(XMLUtils.DOMINANT_STRUCTURE_COLLECTION);
 							dominantStructure.setData(dominantData);
 						}
 					}
 
 					else if (key.equalsIgnoreCase("dependentStructures")) {
 						dependentStructures = parseStructures(children.elementAt(i).getChildNodes(), lookups);
 					}
 
 					else if (key.equalsIgnoreCase("rules")) {
 						NativeNodeList ruleSet = children.item(i).getChildNodes();
 						for (int ruleCount = 0; ruleCount < ruleSet.getLength(); ruleCount++) {
 							if (ruleSet.item(ruleCount).getNodeName().equalsIgnoreCase(XMLUtils.BOOLEAN_RULE)) {
 								BooleanRule rule = null;
 								for (int bRule = 0; bRule < ruleSet.item(ruleCount).getChildNodes().getLength(); bRule++) {
 									if (ruleSet.item(ruleCount).getChildNodes().item(bRule).getNodeName()
 											.equalsIgnoreCase("activateOnRule")) {
 										rule = new BooleanRule(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
 												.getChildNodes().item(bRule)));
 									} else if (ruleSet.item(ruleCount).getChildNodes().item(bRule).getNodeName()
 											.equalsIgnoreCase("actions")) {
 										for (int acts = 0; acts < ruleSet.item(ruleCount).getChildNodes().item(bRule)
 												.getChildNodes().getLength(); acts++) {
 											if (ruleSet.item(ruleCount).getChildNodes().item(bRule).getChildNodes()
 													.item(acts).getNodeName().equalsIgnoreCase("onTrue")) {
 												rule.setOnTrue(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
 														.getChildNodes().item(bRule).getChildNodes().item(acts)));
 											} else if (ruleSet.item(ruleCount).getChildNodes().item(bRule)
 													.getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
 															"onFalse")) {
 												rule.setOnFalse(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
 														.getChildNodes().item(bRule).getChildNodes().item(acts)));
 											} else if (ruleSet.item(ruleCount).getChildNodes().item(bRule)
 													.getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
 															"affectedDependentStructures")) {
 												rule.addAffectedObjectIndex(XMLUtils.getXMLValue(ruleSet
 														.item(ruleCount).getChildNodes().item(bRule).getChildNodes()
 														.item(acts)));
 											}
 										}
 									}
 								}
 								rules.add(rule);
 							} else if (ruleSet.item(ruleCount).getNodeName().equalsIgnoreCase(XMLUtils.SELECT_RULE)) {
 								SelectRule rule = null;
 								for (int sRule = 0; sRule < ruleSet.item(ruleCount).getChildNodes().getLength(); sRule++) {
 									if (ruleSet.item(ruleCount).getChildNodes().item(sRule).getNodeName()
 											.equalsIgnoreCase("activateOnIndex")) {
 										rule = new SelectRule(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
 												.getChildNodes().item(sRule)));
 									} else if (ruleSet.item(ruleCount).getChildNodes().item(sRule).getNodeName()
 											.equalsIgnoreCase("actions")) {
 										for (int acts = 0; acts < ruleSet.item(ruleCount).getChildNodes().item(sRule)
 												.getChildNodes().getLength(); acts++) {
 											if (ruleSet.item(ruleCount).getChildNodes().item(sRule).getChildNodes()
 													.item(acts).getNodeName().equalsIgnoreCase("onTrue")) {
 												rule.setOnTrue(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
 														.getChildNodes().item(sRule).getChildNodes().item(acts)));
 											} else if (ruleSet.item(ruleCount).getChildNodes().item(sRule)
 													.getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
 															"onFalse")) {
 												rule.setOnFalse(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
 														.getChildNodes().item(sRule).getChildNodes().item(acts)));
 											} else if (ruleSet.item(ruleCount).getChildNodes().item(sRule)
 													.getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
 															"affectedDependentStructures")) {
 												rule.addAffectedObjectIndex(XMLUtils.getXMLValue(ruleSet
 														.item(ruleCount).getChildNodes().item(sRule).getChildNodes()
 														.item(acts)));
 											}
 										}
 									}
 								}
 								rules.add(rule);
 							} else if (ruleSet.item(ruleCount).getNodeName().equalsIgnoreCase(XMLUtils.CONTENT_RULE)) {
 								ContentRule rule = null;
 								for (int cRule = 0; cRule < ruleSet.item(ruleCount).getChildNodes().getLength(); cRule++) {
 									if (ruleSet.item(ruleCount).getChildNodes().item(cRule).getNodeName()
 											.equalsIgnoreCase("activateOnContent")) {
 										rule = new ContentRule(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
 												.getChildNodes().item(cRule), ""));
 									} else if (ruleSet.item(ruleCount).getChildNodes().item(cRule).getNodeName()
 											.equalsIgnoreCase("actions")) {
 										for (int acts = 0; acts < ruleSet.item(ruleCount).getChildNodes().item(cRule)
 												.getChildNodes().getLength(); acts++) {
 											if (ruleSet.item(ruleCount).getChildNodes().item(cRule).getChildNodes()
 													.item(acts).getNodeName().equalsIgnoreCase("onTrue")) {
 												rule.setOnTrue(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
 														.getChildNodes().item(cRule).getChildNodes().item(acts)));
 											} else if (ruleSet.item(ruleCount).getChildNodes().item(cRule)
 													.getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
 															"onFalse")) {
 												rule.setOnFalse(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
 														.getChildNodes().item(cRule).getChildNodes().item(acts)));
 											} else if (ruleSet.item(ruleCount).getChildNodes().item(cRule)
 													.getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
 															"affectedDependentStructures")) {
 												rule.addAffectedObjectIndex(XMLUtils.getXMLValue(ruleSet
 														.item(ruleCount).getChildNodes().item(cRule).getChildNodes()
 														.item(acts)));
 											}
 										}
 									}
 								}
 								rules.add(rule);
 							}
 						}// for
 					}// else
 				}
 
 				ArrayList<Object> relatedStructureData = new ArrayList<Object>();
 				relatedStructureData.add(dominantStructure);
 				relatedStructureData.add(dependentStructures);
 				relatedStructureData.add(rules);
 				relatedStructureData.add(XMLUtils.getXMLAttribute(current, "layout", ""));
 				relatedStructureData.add(XMLUtils.getXMLAttribute(current, "dependentsLayout", ""));
 
 				currentDisplayData.setData(relatedStructureData);
 			}
 
 			else if (structureType.equalsIgnoreCase(XMLUtils.MAP_STRUCTURE)) {
 				ArrayList<ArrayList<?>> myMapData = new ArrayList<ArrayList<?>>();
 				ArrayList<String> ids = new ArrayList<String>();
 				ArrayList<String> descriptions = new ArrayList<String>();
 				ArrayList<Double> latitudes = new ArrayList<Double>();
 				ArrayList<Double> longitudes = new ArrayList<Double>();
 
 				NativeNodeList dataPoints = current.getChildNodes();
 				boolean insufficientData = false;
 
 				for (int m = 0; m < dataPoints.getLength(); m++) {
 					if (dataPoints.item(m).getNodeName().equalsIgnoreCase("dataPoint")) {
 						NativeNodeList data = dataPoints.item(m).getChildNodes();
 						for (int k = 0; k < data.getLength(); k++) {
 							try {
 								if (data.item(k).getNodeName().equalsIgnoreCase("id"))
 									ids.add((XMLUtils.getXMLValue(data.item(k), "")));
 								else if (data.item(k).getNodeName().equalsIgnoreCase("description"))
 									descriptions.add(XMLUtils.getXMLValue(data.item(k), ""));
 								else if (data.item(k).getNodeName().equalsIgnoreCase("latitude"))
 									latitudes
 											.add(new Double(Double.parseDouble(XMLUtils.getXMLValue(data.item(k), ""))));
 								else if (data.item(k).getNodeName().equalsIgnoreCase("longitude"))
 									longitudes.add(new Double(Double
 											.parseDouble(XMLUtils.getXMLValue(data.item(k), ""))));
 							} catch (Exception e) {
 								insufficientData = true;
 							}
 						}
 					}
 				}
 				if (insufficientData) {
 					myMapData = null;
 				} else {
 					myMapData.add(ids);
 					myMapData.add(latitudes);
 					myMapData.add(longitudes);
 					myMapData.add(descriptions);
 				}
 
 				currentDisplayData.setData(myMapData);
 			}
 
 			else if (structureType.equalsIgnoreCase(XMLUtils.TREE_STRUCTURE)) {
 				TreeData treeData = new TreeData();
 				TreeData defaultTreeStructure = new TreeData();
 
 				NativeNodeList treeRoots = current.getChildNodes();
 
 				for (int m = 0; m < treeRoots.getLength(); m++) {
 					NativeNode currentRoot = treeRoots.item(m);
 					if (currentRoot.getNodeName().equalsIgnoreCase("defaultStructure")) {
 						for (int k = 0; k < currentRoot.getChildNodes().getLength(); k++) {
 							NativeNode treeStructureNode = currentRoot.getChildNodes().item(k);
 							if (treeStructureNode.getNodeName().equalsIgnoreCase("treeStructures")) {
 								ArrayList<DisplayData> defaultTreeStructureSet = 
 									parseStructures(treeStructureNode.getChildNodes(), lookups);
 								if (defaultTreeStructureSet.size() == 1) {
 									defaultTreeStructure.setStructure(((FieldData) defaultTreeStructureSet.get(0))
 											.getStructure());
 									defaultTreeStructure
 											.setData(((FieldData) defaultTreeStructureSet.get(0)).getData());
 									
 								} else {
 									defaultTreeStructure.setStructure(XMLUtils.STRUCTURE_COLLECTION);
 									defaultTreeStructure.setData(defaultTreeStructureSet);
 								}
 								
 								treeData.setDefaultStructure(defaultTreeStructure);
 							}
 						}
 					} else if (currentRoot.getNodeName().equalsIgnoreCase("root")) {
 						treeData.addTreeRoot(processRoot(currentRoot, defaultTreeStructure, lookups));
 					} else if (currentRoot.getNodeName().equalsIgnoreCase("coding")) {
 						NativeNodeList roots = currentRoot.getChildNodes();
 						for (int r = 0; r < roots.getLength(); r++) {
 							NativeNode curChild = roots.item(r);
 							if ("root".equals(curChild.getNodeName()))
 								treeData.addTreeRoot(processRoot(curChild, defaultTreeStructure, lookups));	
 						}
 					}
 				}
 
 				if (current.getAttribute("description") != null)
 					treeData.setDescription(current.getAttribute("description"));
 				else
 					treeData.setDescription(currentDisplayData.getDescription());
 				currentDisplayData.setData(treeData);
 			}
 
 			else if (structureType.equalsIgnoreCase("complex")) {
 				// DEPRECATED TYPE.
 				currentDisplayData.setData("");
 			}
 
 			structureSet.add(currentDisplayData);
 
 		}
 		return structureSet;
 
 	}
 
 	private void processBasicDisplayData(DisplayData displayData, NativeElement displayTag) {
 		for (int i = 0; i < displayTag.getChildNodes().getLength(); i++) {
 			NativeNode current = displayTag.getChildNodes().item(i);
 
 			if (current.getNodeType() != NativeNode.ELEMENT_NODE)
 				continue;
 
 			String curNodeName = current.getNodeName();
 			if (curNodeName.equalsIgnoreCase(CANONICAL_NAME_TAG_NAME))
 				displayData.setCanonicalName(XMLUtils.getXMLValue(current).trim());
 			else if (curNodeName.equalsIgnoreCase(CLASS_OF_SERVICE_TAG_NAME))
 				displayData.setClassOfService(XMLUtils.getXMLValue(current));
 			else if (curNodeName.equalsIgnoreCase(DESCRIPTION_TAG_NAME))
 				displayData.setDescription(XMLUtils.getXMLValue(current));
 			else if (curNodeName.equalsIgnoreCase(REFERENCES_TAG_NAME)) {
 				NativeNodeList referenceIdNodes = current.getChildNodes();
 				ArrayList<String> refs = new ArrayList<String>();
 				for (int k = 0; k < referenceIdNodes.getLength(); k++)
 					if (referenceIdNodes.item(k).getNodeName().equalsIgnoreCase("referenceId"))
 						refs.add(XMLUtils.getXMLValue(referenceIdNodes.item(k), ""));
 				displayData.setReferences(refs);
 				break;
 			}
 			else if (curNodeName.equalsIgnoreCase(FIELD_DEFINITION_TAG_NAME)) {
 				displayData.setFieldDefinition((NativeElement)current);
 			}
 			else if (LOOKUP_VALUE_TAG_NAME.equalsIgnoreCase(curNodeName)) {
 				final LookupData lookup = new LookupData();
 				final NativeNodeList nodes = current.getChildNodes();
 				for (int k = 0; k < nodes.getLength(); k++) {
 					final NativeNode child = nodes.item(k);
 					if ("option".equals(child.getNodeName())) {
 						lookup.addValue(((NativeElement)child).getAttribute("id"), child.getTextContent());
 					}
 				}
 				displayData.addLookup(((NativeElement)current).getAttribute("id"), lookup);
 			}
 		}
 	}
 
 	/**
 	 * Processes XML tags with Field data
 	 * 
 	 * @param fieldTags
 	 *            NativeNodeList of Field XML data
 	 */
 	private FieldData processFieldTag(NativeElement root) {
 		FieldData currentField = new FieldData();
 		NativeElement structuresTag = root.getElementByTagName(STRUCTURES_DELIMETER_TAG_NAME);
 
 		// Gather Display Data
 		processBasicDisplayData(currentField, root);
 		// processReferences(currentField, fieldTags, i);
 		currentField.setDisplayId(XMLUtils.getXMLAttribute(root, "id"));
 
 		// Build the structure
 		ArrayList<DisplayData> structureSet = parseStructures(structuresTag.getChildNodes(), currentField.getLookups());
 		if (structureSet.size() == 1) {
 			currentField.setStructure((structureSet.get(0)).getStructure());
 			currentField.setData((structureSet.get(0)).getData());
 			currentField.setUniqueId((structureSet.get(0)).getUniqueId());
 			// currentField.setDescription(((FieldData)structureSet.get(0)).
 			// getDescription());
 		} else {
 			currentField.setStructure(XMLUtils.STRUCTURE_COLLECTION);
 			currentField.setData(structureSet);
 			
 			String myDescription = structureSet.get(0).getDescription();
 			if (myDescription == null || "".equals(myDescription))
 				(structureSet.get(0)).setDescription(currentField.getDescription());
 
 			String layout = root.getAttribute("layout");
 			if (layout != null)
 				currentField.setStyle(layout);
 		}
 		
 		Debug.println("For %s, found lookups for %s with structure %s", 
 				currentField.getCanonicalName(), currentField.getLookups(), 
 				currentField.getStructure());
 
 		return currentField;
 	}
 
 	private void processReferences(DisplayData displayData, NativeNode displayTag) {
 		for (int i = 0; i < displayTag.getChildNodes().getLength(); i++) {
 			NativeNode current = displayTag.getChildNodes().item(i);
 			if (current.getNodeName().equalsIgnoreCase(REFERENCES_TAG_NAME)) {
 				NativeNodeList referenceIdNodes = current.getChildNodes();
 				ArrayList<String> refs = new ArrayList<String>();
 				for (int k = 0; k < referenceIdNodes.getLength(); k++)
 					if (referenceIdNodes.item(k).getNodeName().equalsIgnoreCase("referenceId"))
 						refs.add(XMLUtils.getXMLValue(referenceIdNodes.item(k), ""));
 				displayData.setReferences(refs);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Recursively-used helper function to process a SISRow's root rows
 	 * 
 	 * @param currentRoot
 	 *            the NativeNode containing root XML data
 	 * @return a TreeDataRow object representation of the XML
 	 */
 	public static TreeDataRow processRoot(NativeNode currentRoot, TreeData defaultTreeStructure, LookupDataContainer lookups) {
 		return processRoot(currentRoot, null, defaultTreeStructure, lookups);
 	}
 	
 	public static TreeDataRow processRoot(NativeNode currentRoot, TreeDataRow parent, TreeData defaultTreeStructure, LookupDataContainer lookups) {
 		String value;
 		boolean override = false;
 		TreeDataRow currentRow = new TreeDataRow(parent);
 
 		// Optional parameters
 		currentRow.setCodeable(XMLUtils.getXMLAttribute(currentRoot, "codeable"));
 		// currentRow.setExpanded(XMLUtils.getXMLAttribute(currentRoot,
 		// "expanded"));
 		currentRow.setRowNumber(XMLUtils.getXMLAttribute(currentRoot, "id", ""));
 		currentRow.setDepth(XMLUtils.getXMLAttribute(currentRoot, "depth", ""));
 		currentRow.setDisplayId(XMLUtils.getXMLAttribute(currentRoot, "code", ""));
 
 		// Parse the necessary root data
 		NativeNodeList currentRootData = currentRoot.getChildNodes();
 		for (int k = 0; k < currentRootData.getLength(); k++) {
 			// This is "root" data for a non-leaf node
 			if (currentRootData.item(k).getNodeName().equalsIgnoreCase("label")
 					&& !(value = XMLUtils.getXMLValue(currentRootData.item(k)))
 							.equalsIgnoreCase(XMLUtils.NO_NODE_VALUE_FOUND)) {
 				currentRow.setDescription(value);
 				//currentRow.setTitle(XMLUtils.getXMLAttribute(currentRootData.item(k), "title", null));
 			}
 			// As is this
 			else if (currentRootData.item(k).getNodeName().equalsIgnoreCase("treeStructures")) {
 				override = true;
 				ArrayList<DisplayData> structureSet = parseStructures(currentRootData.elementAt(k).getChildNodes(), lookups);
 				if (structureSet.size() == 1) {
 					currentRow.setStructure((structureSet.get(0)).getStructure());
 					currentRow.setData((structureSet.get(0)).getData());
 				} else {
 					currentRow.setStructure(XMLUtils.STRUCTURE_COLLECTION);
 					currentRow.setData(structureSet);
 				}
 			}
 
 			// Now for the (optional) children (recursion!). Recursion halts on
 			// leaf nodes (no child)
 			else if (currentRootData.item(k).getNodeName().equalsIgnoreCase("child")) {
 				currentRow.addChild(processRoot(currentRootData.item(k), currentRow, defaultTreeStructure, lookups));
 			}
 		}
 
 		// If there was no overriding structure given
 		if (!override) {
 			currentRow.setStructure(defaultTreeStructure.getStructure());
 			currentRow.setData(defaultTreeStructure.getData());
 		}
 
 		currentRow.setUsesDefaultStructure(!override);
 
 		return currentRow;
 	}
 
 	/**
 	 * Processes XML tags with Tree data
 	 * 
 	 * @param treeTags
 	 *            NativeNodeList of Tree XML data
 	 */
 	private TreeData processTreeTags(NativeElement root) {
 
 		TreeData treeData = new TreeData();
 
 		// Gather Display Data
 		processBasicDisplayData(treeData, root);
 		treeData.setDisplayId(XMLUtils.getXMLAttribute(root, "id"));
 		
 		Debug.println("For %s, found lookups for %s", treeData.getCanonicalName(), treeData.getLookups());
 
 		TreeData defaultTreeStructure = null;
 
 		// Parse each particular root
 		NativeNodeList treeRoots = root.getElementByTagName("treeRoot").getChildNodes();
 		for (int j = 0; j < treeRoots.getLength(); j++) {
 			NativeNode currentRoot = treeRoots.item(j);
 			String curNodeName = currentRoot.getNodeName();
 			if (curNodeName.equalsIgnoreCase("defaultStructure")) {
 				for (int k = 0; k < currentRoot.getChildNodes().getLength(); k++) {
 					NativeNodeList curList = currentRoot.getChildNodes();
 					if (curList.item(k).getNodeName().equalsIgnoreCase("treeStructures")) {
 						defaultTreeStructure = new TreeData();
 						ArrayList<DisplayData> structureSet = parseStructures(curList.elementAt(k).getChildNodes(), treeData.getLookups());
 						if (structureSet.size() == 1) {
 							defaultTreeStructure.setStructure((structureSet.get(0)).getStructure());
 							defaultTreeStructure.setData((structureSet.get(0)).getData());
 						} else {
 							defaultTreeStructure.setStructure(XMLUtils.STRUCTURE_COLLECTION);
 							defaultTreeStructure.setData(structureSet);
 
 							String layout = curList.elementAt(k).getAttribute("layout");
 							if (layout != null)
 								defaultTreeStructure.setStyle(layout);
 						}
 						treeData.setDefaultStructure(defaultTreeStructure);
 					}
 				}
 			} else if (curNodeName.equalsIgnoreCase("root")) {
 				treeData.addTreeRoot(processRoot(currentRoot, defaultTreeStructure, treeData.getLookups()));
 			}
 		}
 
 		return treeData;
 	}
 
 }
