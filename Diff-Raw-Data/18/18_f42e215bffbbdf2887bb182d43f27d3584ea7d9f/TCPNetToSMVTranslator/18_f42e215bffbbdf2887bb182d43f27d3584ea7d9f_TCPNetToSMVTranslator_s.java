 package translate;
 
 import generate.SpecGenerator;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import util.Constants;
 import util.FileUtil;
 import util.XPathUtil;
 
 /**
  * Translates TCP-nets specified in XML format into SMV model suitable as input for model checkers Cadence SMV and NuSMV.
  * Based on the chosen model checker (according to value set in the constant Constants.CURRENT_MODEL_CHECKER), the output file format differs.
  * Translation is based on the theory from the paper "Dominance Testing via Model Checking" Santhanam et al. AAAI 2010 which contains the precise translation rules.
  * 
  * @see translate.PreferenceInputTranslator#convertToSMV(java.lang.String, int, boolean)
  * @author gsanthan
  *
  */
 public class TCPNetToSMVTranslator implements PreferenceInputTranslator {
 	
 	/**
 	 * Parses XML file and saves a SMV file fit for model checking by NuSMV or Cadence SMV.
 	 * Also generates random 'sampleSize' specs, i.e., dominance test cases and saves them in a separate '.spec' file. 
 	 * Note: Output file syntax is switched according to value set in the constant Constants.CURRENT_MODEL_CHECKER
 	 *   
 	 * @param xmlFile Name of file specifying the input TCP-net
 	 * @param sampleSize Number of sample dominance test cases to be generated
 	 * @return Name of saved SMV file that can be model checked by NuSMV or Cadence SMV
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws XPathExpressionException
 	 */
 	public String convertToSMV(String xmlFile, int sampleSize) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
 		
 		String smvFile = xmlFile.substring(0, xmlFile.length()-4).concat(".smv");
 		Document doc = XPathUtil.makeDocument(xmlFile);
 		
 		// Create/open the smv file where we are going to save the translated code 
 		BufferedWriter w = FileUtil.openFile(smvFile);
 		
 		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
 			FileUtil.writeLineToFile(w, "module main(){");
 		} else {
 			FileUtil.writeLineToFile(w, "MODULE main");
 		}
 		
 		FileUtil.writeLineToFile(w, "");
 		
 		/**
 		 * SMV syntax for a variable "a" with domain ("a1","a2") is as follows:
 		 * VAR  
 		 * 	a : {a1, a2};
 		 */
 		// Extract preference variables
 		String xpathExprVarName = "//VARIABLE/NAME";
 		List<String> variableNames = XPathUtil.evaluateListExpr(xpathExprVarName, doc);
 		
 		String[] variables = variableNames.toArray(new String[0]);
 		String[][] variableDomains = new String[variables.length][];
 		
 		List<String> globalChange = new ArrayList<String>();
 		
 		// Write the lines declaring the variables and their domains to smv file
 		FileUtil.writeLineToFile(w, "VAR");
 		int varIndex = 0;
 		
 		for (Iterator<String> iterator = variableNames.iterator(); iterator.hasNext(); varIndex++) {
 			
 			String variableName = (String) iterator.next();
 			String varLine = "  " + variableName + " : " ;
 			
 			//Extract domain of the current variable
 			String xpathExprDomain = "//VARIABLE[NAME='" + variableName + "']/OUTCOME";
 			List<String> variableDomain = XPathUtil.evaluateListExpr(xpathExprDomain, doc);
 			variableDomains[varIndex] = variableDomain.toArray(new String[0]);
 			Set<String> setOfDomainValues = new HashSet<String>(variableDomain);
 			
 			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
 				//Cadence SMV -- use "boolean" instead of {0,1} for binary domain; otherwise, use enumeration
 				if(setOfDomainValues.equals(new HashSet<String>(Arrays.asList(new String[]{"0","1"})))) {
 					varLine = varLine + "boolean";	
 				} else {
 					varLine = varLine + "{";
 					for (Iterator<String> varDomainIterator = variableDomain.iterator(); varDomainIterator.hasNext();) {
 						String value = (String) varDomainIterator.next();
 						varLine = varLine + value;
 						if(varDomainIterator.hasNext()) {
 							varLine = varLine + ",";
 						}
 					}
 					varLine = varLine + "}";
 				}
 			} else {
 				//NuSMV -- use enumeration {0,1} even for binary domain
 				varLine = varLine + "{";
 				for (Iterator<String> varDomainIterator = variableDomain.iterator(); varDomainIterator.hasNext();) {
 					String value = (String) varDomainIterator.next();
 					varLine = varLine + value;
 					if(varDomainIterator.hasNext()) {
 						varLine = varLine + ",";
 					}
 				}
 				varLine = varLine + "}";
 			}
 			
 			varLine = varLine + ";";
 			
 			FileUtil.writeLineToFile(w, varLine);
 		}
 		
 		// Create variables that indicate change of value in the corresponding preference variable 
 		for (Iterator<String> iterator = variableNames.iterator(); iterator.hasNext(); varIndex++) {
 			String variableName = (String) iterator.next();
 			String varLine = "";
 			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
 				varLine = "  " + "ch" + variableName + " : " + "boolean;";
 			} else {
 				varLine = "  " + "ch" + variableName + " : " + "{0,1};";
 			}
 			FileUtil.writeLineToFile(w, varLine);
 		}
 		
 		//Declare global change variable
 		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
 			FileUtil.writeLineToFile(w, "  gch : boolean;");
 		} else {
 			FileUtil.writeLineToFile(w, "  gch : {0,1};");
 		}
 		
 		FileUtil.writeLineToFile(w, "");
 		
 		//Define a variable that is true whenever at least one of the change variables is true -- used in defining next(gch)
 		FileUtil.writeLineToFile(w, "DEFINE");
 		varIndex=0;
 		String atLeastOneChange = "  change := ";
 		for (Iterator<String> iterator = variableNames.iterator(); iterator.hasNext(); varIndex++) {
 			String variableName = (String) iterator.next();
 			if(varIndex > 0) { 
 				atLeastOneChange = atLeastOneChange + " | ";
 			}
 			atLeastOneChange = atLeastOneChange + "ch" + variableName;
 		}
 		atLeastOneChange = atLeastOneChange + ";";
 		FileUtil.writeLineToFile(w, atLeastOneChange);
 		FileUtil.writeLineToFile(w, "");
 		
 		// Conditional Preferences - Each CPT entry is a transition specification in the model
 		FileUtil.writeLineToFile(w, "ASSIGN");
 		
 		// Write the assignments in terms of next(var) : = case ... esac; statements
 		// If there are parents, then write one line (modeling a transition) for each CPT entry
 		// 		specifying the current variable's preference for that parent assignment, 
 		//		with all other variables equal for all variables other than current variable in the two states
 		List<String> copy = new ArrayList<String>(variableNames);
 		for (Iterator<String> iterator1 = copy.iterator(); iterator1.hasNext();) {
 			
 			String variableName = (String) iterator1.next();
 			
			//Don't Initialize the change variables to 0 (in the start state, chVi=0) 
			//-- It will affect functioning of computing getStateInTerminalSCCFromSeed in SCCHelper 
			//FileUtil.writeLineToFile(w, "  init(ch"+variableName+") := 0;");
 			
 			//Write 'next(var):= case'
 			FileUtil.writeLineToFile(w, "  next("+variableName+") :=");
 			
 			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
 				FileUtil.writeLineToFile(w, "    case{");
 			} else {
 				FileUtil.writeLineToFile(w, "    case");	
 			}
 			
 			
 			String xpathExprCptKey = "//CONDITIONAL-PREFERENCE[FOR='" + variableName + "']/TABLE/CPTROW/PARENTVALUE";
 			String xpathExprCptValue = "//CONDITIONAL-PREFERENCE[FOR='" + variableName + "']/TABLE/CPTROW/PREFERENCE";
 			List<String> cptKeys = XPathUtil.evaluateListExpr(xpathExprCptKey, doc);
 			List<String> cptValues = XPathUtil.evaluateListExpr(xpathExprCptValue, doc);
 			
 			String[] linesToWrite = new String[0]; //used to store conditional preference rules
 			
 			//Conditional preference -- for multiple CPT rows -- translate each
 			linesToWrite = processOrderedValuesForConditional(variableName, cptKeys, cptValues, variableNames, doc);
 			
 			// Dump the translated lines for current variable into the smv file
 			for (int i = 0; i < linesToWrite.length; i++) {
 				
 				//Add to global change list of lines to write
 				globalChange.add(linesToWrite[i]);
 				FileUtil.writeLineToFile(w, linesToWrite[i]);
 			}
 			
 			//Relative importance -- translate 
 			// Extract variables less important than current node
 			String xpathExprLessImpVariables = "//REL-IMP[IMP-VARIABLE='"+variableName+"']/IMP-THAN";	
 			List<String> lessImpVariables = XPathUtil.evaluateListExpr(xpathExprLessImpVariables, doc);
 			
 			// Extract variables more important than current variable
 			String xpathExprMoreImpVariables = "//REL-IMP[IMP-THAN='"+variableName+"']/IMP-VARIABLE";
 			List<String> moreImpVariables = XPathUtil.evaluateListExpr(xpathExprMoreImpVariables, doc);
 			
 			//Extract domain of variable from XML file
 			String domain = getDomainRHSForVariable(variableName, doc);
 			linesToWrite = processOrderedValuesForRelativeImportance(variableName, domain, cptKeys, cptValues, variableNames, lessImpVariables, moreImpVariables, doc);
 
 			// Dump the translated lines for current variable into the smv file
 			for (int i = 0; i < linesToWrite.length; i++) {
 				//Add to global change list of lines to write
 				globalChange.add(linesToWrite[i]);
 				FileUtil.writeLineToFile(w, linesToWrite[i]);
 			}
 			
 			// Specify the default value in the next state of the transition - unchanged
 			FileUtil.writeLineToFile(w, "      1 : " + variableName + ";");
 			
 			if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
 				FileUtil.writeLineToFile(w, "    };");
 			} else {
 				FileUtil.writeLineToFile(w, "    esac;");
 			}
 		}
 		
 		//Process global change variable
 		FileUtil.writeLineToFile(w, "  init(gch) := 0;");
 		
 		//Write 'next(gch):= case'
 		FileUtil.writeLineToFile(w, "  next(gch) :=");
 		
 		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
 			FileUtil.writeLineToFile(w, "    case{");
 		} else {
 			FileUtil.writeLineToFile(w, "    case");	
 		}
 		
 		//Note: There is a difference depending on whether the following line is included as part of next(gch)
 		//FileUtil.writeLineToFile(w, "      gch=1: 0;");
 		
 		for (String g : globalChange) {
 			if(!g.trim().startsWith("--")) {
 				g = g.substring(0,g.lastIndexOf(":"));
 				g = g + " & change ";
 				g = g + ": 1;";
 			}
 			FileUtil.writeLineToFile(w, g);
 		}
 		FileUtil.writeLineToFile(w, "      TRUE: 0;");
 		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
 			FileUtil.writeLineToFile(w, "    };");
 		} else {
 			FileUtil.writeLineToFile(w, "    esac;");	
 		}
 		
 		if(Constants.CURRENT_MODEL_CHECKER == Constants.MODEL_CHECKER.CadenceSMV) {
 			FileUtil.writeLineToFile(w, "}");
 		}
 		
 		FileUtil.closeFile(w);
 		
 		if(sampleSize > 0) {
 			// Generate and write randomly generated 'sampleSize' specs to the spec file
 			String specFile = xmlFile.substring(0, xmlFile.length()-4).concat(".spec");
 			BufferedWriter wSpec = FileUtil.openFile(specFile);
 			SpecGenerator specGen = new SpecGenerator();
 			String[] specs = specGen.createRandomDominanceTestSpecs(variables, variableDomains, sampleSize);
 			for (int i = 0; i < specs.length; i++) {
 				FileUtil.writeLineToFile(wSpec, specs[i]);
 			}
 			
 			FileUtil.closeFile(wSpec);
 		}
 		
 		return smvFile;
 	}
 	
 	/**
 	 * Parses intra-variable preferences specified in the conditional preference table of variableName; and
 	 * translates them into appropriate guarded transitions to be included in the transition specifying how variableName will change, along with the change variables. 
 	 * Translation is according to the semantics given by Brafman (JAIR 2004) for V-flip in a TCP-net. 
 	 * See "Dominance Testing via Model Checking" Santhanam et al. AAAI 2010 for precise translation rules.
 	 * Returns an array of strings that have to be included as guarded transitions to the next(variableName) in the SMV model 
 	 * 
 	 * @param variableName Input variable 
 	 * @param cptKeys Conditions under which intra-variable preferences are specified in the CPT of variableName
 	 * @param cptValues Intra-variable preferences corresponding to conditions in the CPT of variableName
 	 * @param variableNames List of all variableNames
 	 * @param doc XML file containing the input preference specification 
 	 * @return Array of strings that have to be included as guarded transitions to the next(variableName) in the SMV model 
 	 * @throws XPathExpressionException
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 */
 	private String[] processOrderedValuesForConditional(String variableName, List<String> cptKeys, List<String> cptValues, List<String> variableNames, Document doc) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
 		List<String> linesToWrite = new ArrayList<String>();
 		
 		// Extract parents of current node -- Not absolutely needed for translation or reasoning.
 		String xpathExprParents = "//CONDITIONAL-PREFERENCE[FOR='" + variableName + "']/GIVEN";
 		List<String> parents = XPathUtil.evaluateListExpr(xpathExprParents, doc);
 		linesToWrite.add("      -- conditional preference: "+variableName+" depends on "+Arrays.toString(parents.toArray(new String[0])));
 		
 		// For each CPT row, define a transition:
 		// LHS: variable=better value; 
 		// 		then for parents get the CPT key and replace ',' with '&' to model smv syntax;
 		//		then for ALL variables including parents excluding the current variable, 
 		//					ensure equality in next state
 		// RHS: worse value for the variable
 		for (Iterator<String> iterator2 = cptKeys.iterator(), iterator3 = cptValues.iterator(); iterator2.hasNext();) {
 			
 			// LHS of current CPT row, i.e., parent assignment 
 			String parentAssignment = (String) iterator2.next();
 			
 			// RHS of current CPT row for the current LHS, i.e., parent assignment
 			String preferencePairs = (String) iterator3.next();
 			
 			//These are the pairs of binary relations specifying the order on the domain of the current variable 
 			String[] preferences = preferencePairs.split(Constants.PREFERENCE_DELIMITER_IN_XML);
 			
 			for (int j = 0; j < preferences.length; j++) {
 				//Each pair is specified as a binary relation, i.e., a>b for a is preferred to b
 				String[] orderedValues = preferences[j].split(Constants.PREFERENCE_SYMBOL_IN_XML);
 								
 				// Model the transition from the worse to the better value of current variable
 				String currentLine = "      " + variableName + "=" + orderedValues[1];
 				
 				// For all PARENT variables of the current one, 
 				// enforce the assignment specified in LHS of CPT row
 				String[] temp = parentAssignment.split(",");
 				
 				for (int i = 0; i < temp.length; i++) {
 					// If it is unconditional, then there is exactly one CPT row with empty LHS
 					if(temp[i].trim().length()>0) {
 						currentLine = currentLine + " & " + temp[i];
 					}
 				}
 				
 				currentLine = currentLine + " & " + "ch" + variableName + "=1";
 				
 				// For all variables OTHER than the current one, 
 				// enforce equality in the current and next state of the transition
 				for (Iterator<String> iterator = variableNames.iterator(); iterator.hasNext(); ) {
 					String var = iterator.next();
 					if(!var.equals(variableName)) {
 						currentLine = currentLine + " & " + "ch" + var + "=0";
 					}
 				}
 				
 				// Specify the better value in the next state of the transition
 				currentLine = currentLine + " : " + orderedValues[0] + ";";
 				linesToWrite.add(currentLine);
 			}
 		}
 
 		return linesToWrite.toArray(new String[]{});
 	}
 
 	/**
 	 * Parses relative importance preferences with respect to variableName (and variables that are more/less important with respect to variableName); and 
 	 * translates them into appropriate guarded transitions to be included in the transition specifying how variableName will change, along with the change variables. 
 	 * Translation is according to the semantics given by Wilson (AAAI 2004). 
 	 * See "Dominance Testing via Model Checking" Santhanam et al. AAAI 2010 for precise translation rules.
 	 * Returns an array of strings that have to be included as guarded transitions to the next(variableName) in the SMV model   
 	 * 
 	 * @param variableName Input variable 
 	 * @param domain Domain of the variable as a comma separated list of strings
 	 * @param cptKeys Conditions under which intra-variable preferences are specified in the CPT of variableName
 	 * @param cptValues Intra-variable preferences corresponding to conditions in the CPT of variableName
 	 * @param variableNames List of all variableNames
 	 * @param lessImpVariableNames List of variables less important than variableName
 	 * @param moreImpVariableNames List of variables more important than variableName
 	 * @param doc XML file containing the input preference specification 
 	 * @return Array of strings that have to be included as guarded transitions to the next(variableName) in the SMV model   
 	 * @throws XPathExpressionException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 */
 	private String[] processOrderedValuesForRelativeImportance(String variableName, String domain, List<String> cptKeys, List<String> cptValues, List<String> variableNames, List<String> lessImpVariableNames, List<String> moreImpVariableNames, Document doc) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
 		List<String> linesToWrite = new ArrayList<String>();
 		// This method translates the relative importance preferences using Wilson's semantics.
 		linesToWrite.add("      -- relative importance interpreted according to Wilson's semantics:");
 		linesToWrite.add("      -- relative importance: "+variableName+" >> "+Arrays.toString(lessImpVariableNames.toArray(new String[0])));
 		
 		String currentLine = "";
 		// Translation for variables less important than current one:
 		// ==========================================================
 		// For each CPT row, define a transition:
 		// LHS: variable=better value; 
 		// 		then for parents get the CPT key and replace ',' with '&' to model smv syntax;
 		//		then for ALL variables including parents excluding the current variable, 
 		//					ensure equality in next state
 		// RHS: worse value for the variable
 		if(lessImpVariableNames.size()>0) {
 			for (Iterator<String> iterator2 = cptKeys.iterator(), iterator3 = cptValues.iterator(); iterator2.hasNext();) {
 				// LHS of current CPT row, i.e., parent assignment 
 				String parentAssignment = (String) iterator2.next();
 				// RHS of current CPT row for the current LHS, i.e., parent assignment
 				String preferencePairs = (String) iterator3.next();
 				//These are the pairs of binary relations specifying the order on the domain of the current variable 
 				String[] preferences = preferencePairs.split(Constants.PREFERENCE_DELIMITER_IN_XML);
 				for (int j = 0; j < preferences.length; j++) {
 					//Each pair is specified as a binary relation, i.e., a>b for a is preferred to b
 					String[] orderedValues = preferences[j].split(Constants.PREFERENCE_SYMBOL_IN_XML);
 					currentLine = new String();				
 					// Model the transition from the worse to the better value of current variable
 					currentLine = "      " + variableName + "=" + orderedValues[1];
 					
 					// For all PARENT variables of the current one, 
 					// enforce the assignment specified in LHS of CPT row
 					String[] temp = parentAssignment.split(",");
 					
 					for (int i = 0; i < temp.length; i++) {
 						// If it is unconditional, then there is exactly one CPT row with empty LHS
 						if(temp[i].trim().length()>0) {
 							currentLine = currentLine + " & " + temp[i];
 						}
 					}
 					
 					// Wilson's semantics :
 					// ====================
 					// For all variables OTHER THAN THOSE LESS IMPORTANT THAN current one & other than the current one, 
 					// enforce equality in the current and next state of the transition
 					List<String> tempVarNames = new ArrayList<String>(variableNames);
 					tempVarNames.removeAll(lessImpVariableNames);
 					tempVarNames.remove(variableName);
 					for (Iterator<String> iterator = tempVarNames.iterator(); iterator.hasNext(); ) {
 						String var = iterator.next();
 						if(!var.equals(variableName)) {
 							currentLine = currentLine + " & " + "ch" + var + "=0";
 						}
 					}
 					
 					//Specify a rule allowing the SAME value in the next state of the transition with change variable = 0
 					//String currentLine1 = currentLine + " & ch" + variableName + "=0 : " + orderedValues[1] + ";";
 					//linesToWrite.add(currentLine1);
 					//The above transition is redundant and enabled by "TRUE: 0;" default rule. 
 					//Moreover, it will cause problematic behavior of variable gch:
 					//It creates loop holes for gch becoming 1 when none of the variables (including varibleName) changes
 					
 					//Specify a rule allowing the BETTER value in the next state of the transition with change variable = 1
 					String currentLine2 = currentLine + " & ch" + variableName + "=1 : " + orderedValues[0] + ";";
 
 					linesToWrite.add(currentLine2);
 				}
 			}
 		}
 		
 		linesToWrite.add("      -- relative importance: "+variableName+" << "+Arrays.toString(moreImpVariableNames.toArray(new String[0])));
 		
 		// Translation for variables more important than current one:
 		// ==========================================================
 		if(moreImpVariableNames.size() > 0) {
 			currentLine = "      ";
 			for (Iterator<String> iterator = moreImpVariableNames.iterator(); iterator
 					.hasNext();) {
 				String moreImpVariable = iterator.next();
 				linesToWrite.add("      -- relative importance: "+variableName+" << "+moreImpVariable);
 				List<List<String>> cpt = getCPTForVariable(moreImpVariable, doc);
 				List<String> moreImpVarCptKeys = cpt.get(0);
 				List<String> moreImpVarCptValues = cpt.get(1);
 				
 				// For each cpt of the more important variable,
 				// model transitions of current variable to either 0 or 1, maintaining correctness of change variable.
 				for (Iterator<String> iterator2 = moreImpVarCptKeys.iterator(), iterator3 = moreImpVarCptValues.iterator(); iterator2.hasNext();) {
 					// LHS of current CPT row, i.e., parent assignment 
 					String parentAssignmentOfMoreImpVar = (String) iterator2.next();
 					// RHS of current CPT row for the current LHS, i.e., parent assignment
 					String preferencePairs = (String) iterator3.next();
 					//These are the pairs of binary relations specifying the order on the domain of the current variable 
 					String[] preferences = preferencePairs.split(Constants.PREFERENCE_DELIMITER_IN_XML);
 					for (int j = 0; j < preferences.length; j++) {
 						//Each pair is specified as a binary relation, i.e., a>b for a is preferred to b
 						String[] orderedValues = preferences[j].split(Constants.PREFERENCE_SYMBOL_IN_XML);
 						currentLine = new String();				
 						// Model the transition from the worse to the better value of current variable
 						currentLine = "      " + moreImpVariable + "=" + orderedValues[1];
 						
 						// For all PARENT variables of the currently considered most important variable, 
 						// enforce the assignment specified in LHS of CPT row
 						String[] temp = parentAssignmentOfMoreImpVar.split(",");
 						
 						//TODO: 
 						// For Conditional Relative Importance: Must receive another "conditionForRI" for each relative importance statement
 						// Check: If any variable's assignment in the parentAssignmentOfMoreImpVar conflicts with any variable's assignment in conditionForRI
 						//				In this case, remove that entry, because the RI becomes inapplicable then!
 						for (int i = 0; i < temp.length; i++) {
 							// If it is unconditional, then there is exactly one CPT row with empty LHS
 							if(temp[i].trim().length()>0) {
 								currentLine = currentLine + " & " + temp[i];
 							}
 						}
 						
 						// Now model the possibilities for current variable when the currently considered more important variable changes
 						currentLine = currentLine + " & " + "ch" + moreImpVariable + "=1";
 						
 						// Wilson's semantics : 
 						// ====================
 						// For all variables OTHER than this more imp. one except current variable, 
 						// enforce equality in the current and next state of the transition
 						List<String> tempVarNames = new ArrayList<String>(variableNames);
 						List<String> siblingsThroughMoreImpVariable = getVariablesLessImportantThan(moreImpVariable, doc);
 						tempVarNames.removeAll(siblingsThroughMoreImpVariable);
 						tempVarNames.remove(variableName);
 						tempVarNames.remove(moreImpVariable);
 						for (Iterator<String> iterator4 = tempVarNames.iterator(); iterator4.hasNext(); ) {
 							String var = iterator4.next();
 							currentLine = currentLine + " & " + "ch" + var + "=0";
 						}
 						// There are 4 possibilities for the current variable when the currently considered more important variable changes
 						String currentLine1 = currentLine + " & "+variableName+"=0 & ch" + variableName + "=0 : 0;";
 						String currentLine2 = currentLine + " & "+variableName+"=0 & ch" + variableName + "=1 : 1;";
 						String currentLine3 = currentLine + " & "+variableName+"=1 & ch" + variableName + "=0 : 1;";
 						String currentLine4 = currentLine + " & "+variableName+"=1 & ch" + variableName + "=1 : 0;";
 						
 						linesToWrite.add(currentLine1);
 						linesToWrite.add(currentLine2);
 						linesToWrite.add(currentLine3);
 						linesToWrite.add(currentLine4);
 					}
 				}
 			}
 		}
 		return linesToWrite.toArray(new String[]{});
 	}
 	
 	/**
 	 * Parses the domain of a variable from the xml document holding the preference specification. 
 	 * Returns a comma separated list of strings with domain values for variableName.
 	 * 
 	 * @param variableName Input variable 
 	 * @param doc XML file containing the input preference specification 
 	 * @return List of variables less important than variableName
 	 * @throws XPathExpressionException
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 */
 	public String getDomainRHSForVariable(String variableName, Document doc) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
 		String varLine = "";
 		//Extract domain of the current variable
 		String xpathExprDomain = "//VARIABLE[NAME='" + variableName + "']/OUTCOME";
 		List<String> variableDomain = XPathUtil.evaluateListExpr(xpathExprDomain, doc);
 		for (Iterator<String> iterator2 = variableDomain.iterator(); iterator2.hasNext();) {
 			String value = iterator2.next();
 			varLine = varLine + value;
 			if(iterator2.hasNext()) {
 				varLine = varLine + ",";
 			}
 		}
 		return varLine;
 	}
 	
 	/**
 	 * Parses the variables less important than variableName from the xml document holding the preference specification 
 	 * Returns the list of variables less important than variableName
 	 * 
 	 * @param variableName Input variable 
 	 * @param doc XML file containing the input preference specification 
 	 * @return List of variables less important than variableName
 	 * @throws XPathExpressionException
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 */
 	public List<String> getVariablesLessImportantThan(String variableName, Document doc) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
 		String xpathExprVariablesLessImpThan = "//REL-IMP[IMP-VARIABLE='"+variableName+"']/IMP-THAN";
 		List<String> lessImpVariables = XPathUtil.evaluateListExpr(xpathExprVariablesLessImpThan, doc);
 		return lessImpVariables;
 	}
 	
 	/**
 	 * Parses the conditional preference table of variableName from the xml document holding the preference specification. 
 	 * Returns a list of 2 string lists: <list of cpt keys, list of corresponding values>
 	 * 
 	 * @param variableName Variable name for which conditional preference table is to be parsed
 	 * @param doc XML file containing the input preference specification 
 	 * @return List of 2 string lists: <list of cpt keys, list of corresponding values>
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws XPathExpressionException
 	 */
 	public List<List<String>> getCPTForVariable(String variableName, Document doc) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
 		String xpathExprCptKey = "//CONDITIONAL-PREFERENCE[FOR='" + variableName + "']/TABLE/CPTROW/PARENTVALUE";
 		String xpathExprCptValue = "//CONDITIONAL-PREFERENCE[FOR='" + variableName + "']/TABLE/CPTROW/PREFERENCE";
 		List<String> cptKeys = XPathUtil.evaluateListExpr(xpathExprCptKey, doc);
 		List<String> cptValues = XPathUtil.evaluateListExpr(xpathExprCptValue, doc);
 		List<List<String>> cpt = new ArrayList<List<String>>();
 		cpt.add(cptKeys);
 		cpt.add(cptValues);
 		return cpt;
 	}
 	
 	/**
 	 * This is a no-op method for simply parsing the xml and retrieving all the details using xpath
 	 * 
 	 * @param xmlFile
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws XPathExpressionException
 	 */
 	public void parsePreferenceXML(String xmlFile) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
 		Document doc = XPathUtil.makeDocument(xmlFile);
 		
 		// Extract preference variables
 		String xpathExprVarName = "//VARIABLE/NAME";
 		List<String> variableNames = XPathUtil.evaluateListExpr(xpathExprVarName, doc);
 		
 		for (Iterator<String> iterator = variableNames.iterator(); iterator.hasNext();) {
 			
 			String variableName = (String) iterator.next();
 
 			// Extract parents of current node -- Not absolutely needed for translation or reasoning.
 			String xpathExprParents = "//CONDITIONAL-PREFERENCE[FOR='" + variableName + "']/GIVEN";
 			@SuppressWarnings("unused")
 			List<String> parents = XPathUtil.evaluateListExpr(xpathExprParents, doc);
 
 			//Extract domain of the current variable
 			String xpathExprDomain = "//VARIABLE[NAME='" + variableName + "']/OUTCOME";
 			List<String> variableDomains = XPathUtil.evaluateListExpr(xpathExprDomain, doc);
 			
 			Set<String> domain = new HashSet<String>();
 			domain.addAll(variableDomains);
 			
 			String xpathExprCptKey = "//CONDITIONAL-PREFERENCE[FOR='" + variableName + "']/TABLE/CPTROW/PARENTVALUE";
 			String xpathExprCptValue = "//CONDITIONAL-PREFERENCE[FOR='" + variableName + "']/TABLE/CPTROW/PREFERENCE";
 			List<String> cptKeys = XPathUtil.evaluateListExpr(xpathExprCptKey, doc);
 			List<String> cptValues = XPathUtil.evaluateListExpr(xpathExprCptValue, doc);
 			for (Iterator<String> iterator2 = cptKeys.iterator(), iterator3 = cptValues.iterator(); iterator2.hasNext();) {
 				@SuppressWarnings("unused")
 				String key = (String) iterator2.next();
 				@SuppressWarnings("unused")
 				String value = (String) iterator3.next();
 			}
 		}
 	}
 
 	public static String[] getVariablesFromSMVModel(String smvFile) throws IOException {
 		Set<String> variables = new HashSet<String>();
 		BufferedReader r = FileUtil.openFileForRead(smvFile);
 		
 		try{
 		String nextLine = null;
 		
 		do {
 			nextLine = r.readLine();
 		} while (nextLine != null && !nextLine.trim().equalsIgnoreCase("VAR"));
 		
 		if(nextLine == null) {
 			//VAR declaration not present - Error in SMV file
 			throw new RuntimeException("No VARS declaration line in SMV file");
 		}
 		
 		do {
 			nextLine = r.readLine();
 			if(nextLine != null) {
 				nextLine = nextLine.trim();
 				if(nextLine.contains(":")) {
 					String var = nextLine.substring(0,nextLine.indexOf(":")-1).trim();
 					if(!var.contains("ch") && ! var.contains("used")) {
 						variables.add(var);
 					}
 				}
 			}
 		} while (nextLine != null && !nextLine.trim().equalsIgnoreCase("ASSIGN"));
 		}finally{r.close();}
 		return variables.toArray(new String[]{});
 	}
 }
