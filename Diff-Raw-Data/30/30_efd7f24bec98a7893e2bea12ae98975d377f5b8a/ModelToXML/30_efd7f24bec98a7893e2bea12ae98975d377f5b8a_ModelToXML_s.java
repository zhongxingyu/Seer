 package org.openxdata.modelutils;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import org.fcitmuk.epihandy.Condition;
 import org.fcitmuk.epihandy.DynamicOptionDef;
 import org.fcitmuk.epihandy.EpihandyConstants;
 import org.fcitmuk.epihandy.FormDef;
 import org.fcitmuk.epihandy.OptionDef;
 import org.fcitmuk.epihandy.PageDef;
 import org.fcitmuk.epihandy.QuestionDef;
 import org.fcitmuk.epihandy.SkipRule;
 import org.fcitmuk.epihandy.ValidationRule;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ModelToXML {
 
 	private static Logger log = LoggerFactory.getLogger(ModelToXML.class);
 
 	public static String BASE64_XSDTYPE = "xsd:base64Binary";
 	public static String BOOLEAN_XSDTYPE = "xsd:boolean";
 	public static String STRING_XSDTYPE = "xsd:string";
 	private static final String DATE_XSDTYPE = "xsd:date";
 	private static final String DATETIME_XSDTYPE = "xsd:dateTime";
 	private static final String INTEGER_XSDTYPE = "xsd:int";
 	private static final String DECIMAL_XSDTYPE = "xsd:decimal";
 	private static final String TIME_XDSTYPE = "xsd:time";
 
 	private static final String AUDIO_BINDFORMAT = "audio";
 	private static final String VIDEO_BINDFORMAT = "video";
 	private static final String IMAGE_BINDFORMAT = "image";
 	private static final String GPS_BINDFORMAT = "gps";
 	private static final String PHONENUMBER_BINDFORMAT = "phonenumber";
 
 	public static String convert(FormDef formDef) {
 		if (formDef == null)
 			throw new IllegalArgumentException("form def can not be null");
 
 		StringBuilder buf = new StringBuilder();
 
 		QuestionTree qTree = QuestionTree.constructTreeFromFormDef(formDef);
 
 		if (log.isDebugEnabled())
 			log.debug("parsed question tree: \n" + qTree);
 
 		// Build a reverse map of targets to skip rules that affect them
 		Map<Short, Set<SkipRule>> skipRulesByTarget = getSkipRulesByTargetId(formDef);
 
 		// Build a map of dynamic lists to the dynopts that affect them
 		Map<Short, QuestionDef> dynOptDepMap = getDynOptDepMap(formDef);
 
 		// Output xform header and beginning of model declaration
 		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
 		buf.append('\n');
 		buf.append("<xf:xforms xmlns:xf=\"http://www.w3.org/2002/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
 		buf.append('\n');
 		buf.append("\t<xf:model>");
 		buf.append('\n');
 		generateMainInstance(qTree, buf);
 		buf.append('\n');
 		generateDynListInstances(formDef, buf, dynOptDepMap);
 		generateBindings(formDef, buf, skipRulesByTarget);
 		buf.append("\t</xf:model>");
 		buf.append('\n');
 		generateControls(formDef, buf, dynOptDepMap);
 		buf.append("</xf:xforms>");
 		buf.append('\n');
 
 		if (log.isDebugEnabled())
 			log.debug("converted form:\n" + buf.toString());
 
 		return buf.toString();
 	}
 
 	@SuppressWarnings("unchecked")
 	private static Map<Short, QuestionDef> getDynOptDepMap(FormDef formDef) {
 		Map<Short, QuestionDef> dynOptDepMap = new HashMap<Short, QuestionDef>();
 		for (Map.Entry<Short, DynamicOptionDef> dynOptEntry : (Set<Map.Entry<Short, DynamicOptionDef>>) formDef
 				.getDynamicOptions().entrySet()) {
 			dynOptDepMap.put(dynOptEntry.getValue().getQuestionId(),
 					formDef.getQuestion(dynOptEntry.getKey()));
 		}
 		return dynOptDepMap;
 	}
 
 	@SuppressWarnings("unchecked")
 	private static Map<Short, Set<SkipRule>> getSkipRulesByTargetId(
 			FormDef formDef) {
 		Map<Short, Set<SkipRule>> skipRulesByTarget = new HashMap<Short, Set<SkipRule>>();
 		for (SkipRule skipRule : (Vector<SkipRule>) formDef.getSkipRules()) {
 			for (Short targetQuestionId : (Vector<Short>) skipRule
 					.getActionTargets()) {
 				Set<SkipRule> ruleSet = skipRulesByTarget.get(targetQuestionId);
 				if (ruleSet == null) {
 					ruleSet = new LinkedHashSet<SkipRule>();
 					skipRulesByTarget.put(targetQuestionId, ruleSet);
 				}
 				ruleSet.add(skipRule);
 			}
 		}
 		return skipRulesByTarget;
 	}
 
 	@SuppressWarnings("unchecked")
 	private static void generateControls(FormDef formDef, StringBuilder buf,
 			Map<Short, QuestionDef> dynOptDepMap) {
 		for (PageDef p : (Vector<PageDef>) formDef.getPages()) {
 			buf.append(MessageFormat.format("\t<xf:group id=\"{0}\">",
 					p.getPageNo()));
 			buf.append('\n');
 			buf.append(MessageFormat.format("\t\t<xf:label>{0}</xf:label>",
 					p.getName()));
 			buf.append('\n');
 
 			for (QuestionDef q : (Vector<QuestionDef>) p.getQuestions()) {
 
 				byte qType = q.getType();
 				String qPath = q.getVariableName();
 				String qName = q.getText();
 				String[] tree = q.getVariableName().split("/\\s*");
 				String qId = tree[tree.length - 1];
 
 				if (qType == QuestionDef.QTN_TYPE_REPEAT) {
 					buf.append(MessageFormat.format(
 							"\t\t<xf:group id=\"{0}\">", qPath));
 					buf.append('\n');
 					buf.append(MessageFormat.format(
							"\t\t\t<xf:label>{0}</xf:label>", qName));
 					buf.append('\n');
 					buf.append("\t\t</xf:group>\n");
 				} else if (qType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE) {
 					buf.append(MessageFormat.format(
 							"\t\t<xf:select1 bind=\"{0}\">", qId));
 					buf.append('\n');
 					buf.append(MessageFormat.format(
 							"\t\t\t<xf:label>{0}</xf:label>", qName));
 					buf.append('\n');
 					for (OptionDef opt : (Vector<OptionDef>) q.getOptions()) {
 						String optFormat = "<xf:item id=\"{0}\"><xf:label>{1}</xf:label><xf:value>{0}</xf:value></xf:item>";
 						String optDef = MessageFormat.format(optFormat,
 								opt.getVariableName(), opt.getText());
 						buf.append("\t\t\t");
 						buf.append(optDef);
 						buf.append('\n');
 					}
 					buf.append("\t\t</xf:select1>");
 					buf.append('\n');
 				} else if (qType == QuestionDef.QTN_TYPE_LIST_MULTIPLE) {
 					buf.append(MessageFormat.format(
 							"\t\t<xf:select bind=\"{0}\">", qId));
 					buf.append('\n');
 					buf.append(MessageFormat.format(
 							"\t\t\t<xf:label>{0}</xf:label>", qName));
 					buf.append('\n');
 					for (OptionDef opt : (Vector<OptionDef>) q.getOptions()) {
 						String optFormat = "<xf:item id=\"{0}\"><xf:label>{1}</xf:label><xf:value>{0}</xf:value></xf:item>";
 						String optDef = MessageFormat.format(optFormat,
 								opt.getVariableName(), opt.getText());
 						buf.append("\t\t\t");
 						buf.append(optDef);
 						buf.append('\n');
 					}
 					buf.append("\t\t</xf:select>");
 					buf.append('\n');
 				} else if (qType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC) {
 					buf.append(MessageFormat.format(
 							"\t\t<xf:select1 bind=\"{0}\">", qId));
 					buf.append('\n');
 					buf.append(MessageFormat.format(
 							"\t\t\t<xf:label>{0}</xf:label>", qName));
 					buf.append('\n');
 					QuestionDef parentQuestion = dynOptDepMap.get(q.getId());
 					String[] parentTree = parentQuestion.getVariableName()
 							.split("/\\s*");
 					String parentId = parentTree[parentTree.length - 1];
 					String itemsetFormat = "\t\t\t<xf:itemset nodeset=\"instance(''{1}'')/item[@parent=instance(''{0}'')/{2}]\"><xf:label ref=\"label\"/><xf:value ref=\"value\"/></xf:itemset>\n";
 					String itemsetDef = MessageFormat.format(itemsetFormat,
 							tree[1], qId, parentId);
 					buf.append(itemsetDef);
 					buf.append("\t\t</xf:select1>");
 					buf.append('\n');
 				} else if (questionTypeGeneratesBoundInput(qType)) {
 					buf.append(MessageFormat.format(
 							"\t\t<xf:input bind=\"{0}\">", qId));
 					buf.append('\n');
 					buf.append(MessageFormat.format(
 							"\t\t\t<xf:label>{0}</xf:label>", qName));
 					buf.append('\n');
 					buf.append("\t\t</xf:input>");
 					buf.append('\n');
 				} else if (questionTypeGeneratesBoundUpload(qType)) {
 					String mediaType = questionTypeToMediaType(qType);
 					buf.append(MessageFormat.format(
 							"\t\t<xf:upload bind=\"{0}\" mediatype=\"{1}\">",
 							qId, mediaType));
 					buf.append('\n');
 					buf.append(MessageFormat.format(
 							"\t\t\t<xf:label>{0}</xf:label>", qName));
 					buf.append('\n');
 					buf.append("\t\t</xf:upload>");
 					buf.append('\n');
 				}
 			}
 
 			buf.append('\n');
 			buf.append("\t</xf:group>");
 			buf.append('\n');
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private static void generateBindings(FormDef formDef, StringBuilder buf,
 			Map<Short, Set<SkipRule>> skipRulesByTarget) {
 		for (PageDef p : (Vector<PageDef>) formDef.getPages())
 			for (QuestionDef q : (Vector<QuestionDef>) p.getQuestions()) {
 				String[] tree = q.getVariableName().split("/\\s*");
 				for (int i = 0; i < tree.length; i++) {
 					if ("".equals(tree[i])
 							|| formDef.getVariableName().equals(tree[i]))
 						continue;
 
 					boolean generateBind = questionTypeGeneratesBind(q
 							.getType());
 					boolean generateFormat = questionTypeGeneratesBindFormat(q
 							.getType());
 					boolean generateValidation = questionGeneratesValidationRule(
 							formDef, q);
 					boolean generateRelevant = skipRulesByTarget.containsKey(q
 							.getId());
 					String qid = tree[tree.length - 1];
 					if (generateBind) {
 						buf.append("\t\t");
 						StringBuilder bindBuf = new StringBuilder(
 								"<xf:bind id=\"{0}\" nodeset=\"{1}\" type=\"{2}\"");
 						List<Object> bindArgs = new ArrayList<Object>();
 
 						bindArgs.add(qid);
 						bindArgs.add(q.getVariableName());
 						bindArgs.add(questionTypeToSchemaType(q.getType()));
 
 						if (generateFormat) {
 							bindBuf.append(" format=\"{");
 							bindBuf.append(bindArgs.size());
 							bindBuf.append("}\"");
 							bindArgs.add(questionTypeToFormat(q.getType()));
 						}
 
 						if (generateValidation) {
 							bindBuf.append(" constraint=\"{");
 							bindBuf.append(bindArgs.size());
 							bindBuf.append("}\" message=\"{");
 							bindBuf.append(bindArgs.size() + 1);
 							bindBuf.append("}\"");
 							ValidationRule vRule = formDef.getValidationRule(q
 									.getId());
 							String constraint = buildConstraintFromRule(
 									formDef, vRule);
 							bindArgs.add(constraint);
 							bindArgs.add(vRule.getErrorMessage());
 						}
 
 						if (generateRelevant) {
 							bindBuf.append(" relevant=\"{");
 							bindBuf.append(bindArgs.size());
 							bindBuf.append("}\" action=\"{");
 							bindBuf.append(bindArgs.size() + 1);
 							bindBuf.append("}\"");
 							Set<SkipRule> skipRules = skipRulesByTarget.get(q
 									.getId());
 							String constraint = buildSkipRuleLogic(formDef,
 									skipRules, q);
 							Object lastRule = skipRules.toArray()[skipRules
 									.size() - 1];
 							String action = buildAction(((SkipRule) lastRule)
 									.getAction());
 							bindArgs.add(constraint);
 							bindArgs.add(action);
 						}
 
 						bindBuf.append("/>");
 						buf.append(MessageFormat.format(bindBuf.toString(),
 								bindArgs.toArray()));
 						buf.append('\n');
 					}
 				}
 			}
 	}
 
 	@SuppressWarnings("unchecked")
 	private static void generateDynListInstances(FormDef formDef,
 			StringBuilder buf, Map<Short, QuestionDef> dynOptDepMap) {
 		for (PageDef p : (Vector<PageDef>) formDef.getPages())
 			for (QuestionDef q : (Vector<QuestionDef>) p.getQuestions()) {
 				byte qType = q.getType();
 				String qId = getIdFromVarName(q.getVariableName());
 				if (qType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC) {
 					String instanceDef = MessageFormat.format(
 							"\t\t<xf:instance id=''{0}''>\n", qId);
 					buf.append(instanceDef);
 					buf.append("\t\t\t<dynamiclist>\n");
 					QuestionDef parentQuestion = dynOptDepMap.get(q.getId());
 					QuestionDef parentParentQuestion = dynOptDepMap
 							.get(parentQuestion.getId());
 					Map<Short, OptionDef> possibleParentValues = getPossibleValues(
 							formDef, parentQuestion, parentParentQuestion);
 					DynamicOptionDef dynOptDef = formDef
 							.getDynamicOptions(parentQuestion.getId());
 					for (Map.Entry<Short, Vector<OptionDef>> dynOptEntry : (Set<Map.Entry<Short, Vector<OptionDef>>>) dynOptDef
 							.getParentToChildOptions().entrySet()) {
 						for (OptionDef option : dynOptEntry.getValue()) {
 							String itemPattern = "\t\t\t\t<item id=\"{0}\" parent=\"{1}\"><label>{2}</label><value>{0}</value></item>\n";
 							OptionDef parentOption = possibleParentValues
 									.get(dynOptEntry.getKey());
 							String itemDef = MessageFormat.format(itemPattern,
 									option.getVariableName(),
 									parentOption.getVariableName(),
 									option.getText());
 							buf.append(itemDef);
 						}
 					}
 
 					buf.append("\t\t\t</dynamiclist>\n");
 					buf.append("\t\t</xf:instance>\n");
 				}
 			}
 	}
 
 	private static void generateMainInstance(QuestionTree rootTree,
 			StringBuilder buf) {
 
 		FormDef formDef = rootTree.getFormDef();
 
 		buf.append(MessageFormat.format("\t\t<xf:instance id=\"{0}\">",
 				formDef.getVariableName()));
 		buf.append('\n');
 
 		// Generate the main instance
 		buf.append(MessageFormat
 				.format("\t\t\t<{0} description-template=\"{1}\" id=\"{2}\" name=\"{3}\">",
 						formDef.getVariableName(),
 						formDef.getDescriptionTemplate(), formDef.getId(),
 						formDef.getName()));
 		buf.append('\n');
 		if (!rootTree.isLeaf()) {
 			for (QuestionTree childTree : rootTree.getChildren()) {
 				generateInstanceElement(childTree, buf);
 			}
 		}
 		buf.append(MessageFormat.format("\t\t\t</{0}>",
 				formDef.getVariableName()));
 		buf.append('\n');
 		buf.append("\t\t</xf:instance>");
 	}
 
 	private static void generateInstanceElement(QuestionTree tree,
 			StringBuilder buf) {
 
 		FormDef form = tree.getFormDef();
 		QuestionDef question = tree.getQuestion();
 		String instanceBinding = "/" + form.getVariableName() + "/";
 		String questionBinding = tree.getQuestion().getVariableName();
 		StringBuilder pad = new StringBuilder("\t\t\t\t");
 		for (int i = 1; i < tree.getDepth(); i++)
 			pad.append('\t');
 
 		if (questionBinding.startsWith(instanceBinding))
 			questionBinding = questionBinding.substring(instanceBinding
 					.length());
 
 		String[] elements = questionBinding.split("/");
 		for (int elem = 0; elem < elements.length - 1; elem++) {
 			buf.append(pad);
 			for (int depth = 0; depth < elem; depth++)
 				buf.append('\t');
 			buf.append(MessageFormat.format("<{0}>\n", elements[elem]));
 		}
 
 		String lastElement = elements[elements.length - 1];
 		if (tree.isLeaf()) {
 			buf.append(pad);
 			String defaultValue = question.getDefaultValue();
 			if (defaultValue != null && !"".equals(defaultValue))
 				buf.append(MessageFormat.format("<{0}>{1}</{0}>\n",
 						lastElement, defaultValue));
 			else
 				buf.append(MessageFormat.format("<{0}/>\n", lastElement));
 		} else {
 			buf.append(MessageFormat.format("{0}\t<{1}>\n", pad, lastElement));
 			for (QuestionTree childTree : tree.getChildren())
 				generateInstanceElement(childTree, buf);
 			buf.append(MessageFormat.format("{0}\t</{1}>\n", pad, lastElement));
 		}
 
 		for (int elem = elements.length - 2; elem >= 0; elem--) {
 			buf.append(pad);
 			for (int depth = 0; depth < elem; depth++)
 				buf.append('\t');
 			String elementName = elements[elem];
 			buf.append(MessageFormat.format("</{0}>\n", elementName));
 		}
 	}
 
 	public static String buildAction(byte action) {
 		StringBuilder buf = new StringBuilder();
 
 		if ((action & EpihandyConstants.ACTION_HIDE) != 0)
 			buf.append("hide");
 		else if ((action & EpihandyConstants.ACTION_SHOW) != 0)
 			buf.append("show");
 		else if ((action & EpihandyConstants.ACTION_DISABLE) != 0)
 			buf.append("disable");
 		else if ((action & EpihandyConstants.ACTION_ENABLE) != 0)
 			buf.append("enable");
 
 		if ((action & EpihandyConstants.ACTION_MAKE_MANDATORY) != 0)
 			buf.append("|true()");
 
 		return buf.toString();
 	}
 
 	@SuppressWarnings("unchecked")
 	public static String buildSkipRuleLogic(FormDef form,
 			Set<SkipRule> skipRules, QuestionDef target) {
 
 		StringBuilder buf = new StringBuilder();
 		for (SkipRule rule : skipRules) {
 			String op = rule.getConditionsOperator() == EpihandyConstants.CONDITIONS_OPERATOR_AND ? "and"
 					: "or";
 
 			Vector<Condition> conditions = (Vector<Condition>) rule
 					.getConditions();
 			for (int i = 0; i < conditions.size(); i++) {
 
 				Condition c = conditions.get(i);
 
 				String qPath = null;
 				if (target.getId() == c.getQuestionId())
 					qPath = ".";
 				else
 					qPath = form.getQuestion(c.getQuestionId())
 							.getVariableName();
 
 				buf.append(qPath);
 				buf.append(' ');
 				buf.append(opTypeToString(c.getOperator()));
 				buf.append(" '");
 				buf.append(c.getValue());
 				buf.append('\'');
 
 				if (i < conditions.size() - 1 && conditions.size() > 1) {
 					buf.append(' ');
 					buf.append(op);
 					buf.append(' ');
 				}
 			}
 		}
 		return buf.toString();
 	}
 
 	@SuppressWarnings("unchecked")
 	public static String buildConstraintFromRule(FormDef form,
 			ValidationRule rule) {
 
 		StringBuilder buf = new StringBuilder();
 		String op = rule.getConditionsOperator() == EpihandyConstants.CONDITIONS_OPERATOR_AND ? "and"
 				: "or";
 
 		Vector<Condition> conditions = (Vector<Condition>) rule.getConditions();
 		for (int i = 0; i < conditions.size(); i++) {
 
 			Condition c = conditions.get(i);
 
 			String qPath = null;
 			if (rule.getQuestionId() == c.getQuestionId())
 				qPath = ".";
 			else
 				qPath = form.getQuestion(c.getQuestionId()).getVariableName();
 
 			buf.append(qPath);
 			buf.append(' ');
 			buf.append(opTypeToString(c.getOperator()));
 			buf.append(' ');
 			buf.append(c.getValue());
 
 			if (i < conditions.size() - 1 && conditions.size() > 1) {
 				buf.append(' ');
 				buf.append(op);
 				buf.append(' ');
 			}
 		}
 		return buf.toString();
 	}
 
 	private static Map<Byte, String> opMap = new HashMap<Byte, String>();
 
 	static {
 		opMap.put(EpihandyConstants.OPERATOR_EQUAL, "=");
 		opMap.put(EpihandyConstants.OPERATOR_NOT_EQUAL, "!=");
 		opMap.put(EpihandyConstants.OPERATOR_LESS, "&lt;");
 		opMap.put(EpihandyConstants.OPERATOR_LESS_EQUAL, "&lt;=");
 		opMap.put(EpihandyConstants.OPERATOR_GREATER, "&gt;");
 		opMap.put(EpihandyConstants.OPERATOR_GREATER_EQUAL, "&gt;=");
 	}
 
 	public static String opTypeToString(byte opType) {
 		return opMap.get(opType);
 	}
 
 	@SuppressWarnings("unchecked")
 	public static Map<Short, OptionDef> getPossibleValues(FormDef form,
 			QuestionDef question, QuestionDef parentQuestion) {
 		Map<Short, OptionDef> valuesById = new HashMap<Short, OptionDef>();
 		byte questionType = question.getType();
 		if (questionType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE
 				|| questionType == QuestionDef.QTN_TYPE_LIST_MULTIPLE) {
 			for (OptionDef option : (Vector<OptionDef>) question.getOptions())
 				valuesById.put(option.getId(), option);
 		} else if (questionType == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC) {
 			Map<Short, Vector<OptionDef>> optMap = (Map<Short, Vector<OptionDef>>) form
 					.getDynamicOptions(parentQuestion.getId())
 					.getParentToChildOptions();
 			for (Map.Entry<Short, Vector<OptionDef>> entry : optMap.entrySet())
 				for (OptionDef option : entry.getValue())
 					valuesById.put(option.getId(), option);
 		}
 		return valuesById;
 	}
 
 	public static String[] getPathFromVariableName(String varName) {
 		String trimmedString = varName.trim();
 		String[] path = trimmedString.split("/");
 		return path;
 	}
 
 	public static String getIdFromVarName(String varName) {
 		String[] path = getPathFromVariableName(varName);
 		return path[path.length - 1];
 	}
 
 	public static boolean questionTypeGeneratesBind(byte type) {
 		switch (type) {
 		case QuestionDef.QTN_TYPE_AUDIO:
 		case QuestionDef.QTN_TYPE_BARCODE:
 		case QuestionDef.QTN_TYPE_BOOLEAN:
 		case QuestionDef.QTN_TYPE_DATE:
 		case QuestionDef.QTN_TYPE_DATE_TIME:
 		case QuestionDef.QTN_TYPE_DECIMAL:
 		case QuestionDef.QTN_TYPE_GPS:
 		case QuestionDef.QTN_TYPE_IMAGE:
 		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
 		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
 		case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
 		case QuestionDef.QTN_TYPE_NUMERIC:
 		case QuestionDef.QTN_TYPE_PHONENUMBER:
 		case QuestionDef.QTN_TYPE_TEXT:
 		case QuestionDef.QTN_TYPE_TIME:
 		case QuestionDef.QTN_TYPE_VIDEO:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	public static String questionTypeToSchemaType(byte type) {
 
 		switch (type) {
 		case QuestionDef.QTN_TYPE_AUDIO:
 			return BASE64_XSDTYPE;
 		case QuestionDef.QTN_TYPE_BARCODE:
 			return STRING_XSDTYPE;
 		case QuestionDef.QTN_TYPE_BOOLEAN:
 			return BOOLEAN_XSDTYPE;
 		case QuestionDef.QTN_TYPE_DATE:
 			return DATE_XSDTYPE;
 		case QuestionDef.QTN_TYPE_DATE_TIME:
 			return DATETIME_XSDTYPE;
 		case QuestionDef.QTN_TYPE_DECIMAL:
 			return DECIMAL_XSDTYPE;
 		case QuestionDef.QTN_TYPE_GPS:
 			return STRING_XSDTYPE;
 		case QuestionDef.QTN_TYPE_IMAGE:
 			return BASE64_XSDTYPE;
 		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
 		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
 		case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
 			return STRING_XSDTYPE;
 		case QuestionDef.QTN_TYPE_NUMERIC:
 			return INTEGER_XSDTYPE;
 		case QuestionDef.QTN_TYPE_PHONENUMBER:
 			return STRING_XSDTYPE;
 		case QuestionDef.QTN_TYPE_TEXT:
 			return STRING_XSDTYPE;
 		case QuestionDef.QTN_TYPE_TIME:
 			return TIME_XDSTYPE;
 		case QuestionDef.QTN_TYPE_VIDEO:
 			return BASE64_XSDTYPE;
 		default:
 			return null;
 		}
 	}
 
 	public static String questionTypeToMediaType(byte type) {
 		switch (type) {
 		case QuestionDef.QTN_TYPE_AUDIO:
 			return "audio/*";
 		case QuestionDef.QTN_TYPE_VIDEO:
 			return "video/*";
 		case QuestionDef.QTN_TYPE_IMAGE:
 			return "image/*";
 		default:
 			return null;
 		}
 	}
 
 	public static boolean questionTypeGeneratesBindFormat(byte type) {
 		return questionTypeToFormat(type) != null;
 	}
 
 	public static String questionTypeToFormat(byte type) {
 		switch (type) {
 		case QuestionDef.QTN_TYPE_AUDIO:
 			return AUDIO_BINDFORMAT;
 		case QuestionDef.QTN_TYPE_VIDEO:
 			return VIDEO_BINDFORMAT;
 		case QuestionDef.QTN_TYPE_IMAGE:
 			return IMAGE_BINDFORMAT;
 		case QuestionDef.QTN_TYPE_GPS:
 			return GPS_BINDFORMAT;
 		case QuestionDef.QTN_TYPE_PHONENUMBER:
 			return PHONENUMBER_BINDFORMAT;
 		default:
 			return null;
 		}
 	}
 
 	public static boolean questionTypeGeneratesBoundInput(byte type) {
 		switch (type) {
 		case QuestionDef.QTN_TYPE_AUDIO:
 		case QuestionDef.QTN_TYPE_VIDEO:
 		case QuestionDef.QTN_TYPE_IMAGE:
 		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
 		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
 		case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
 		case QuestionDef.QTN_TYPE_REPEAT:
 			return false;
 		default:
 			return true;
 		}
 	}
 
 	public static boolean questionTypeGeneratesBoundUpload(byte type) {
 		switch (type) {
 		case QuestionDef.QTN_TYPE_AUDIO:
 		case QuestionDef.QTN_TYPE_VIDEO:
 		case QuestionDef.QTN_TYPE_IMAGE:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	public static boolean questionGeneratesValidationRule(FormDef form,
 			QuestionDef question) {
 		return form.getValidationRule(question.getId()) != null;
 	}
 }
