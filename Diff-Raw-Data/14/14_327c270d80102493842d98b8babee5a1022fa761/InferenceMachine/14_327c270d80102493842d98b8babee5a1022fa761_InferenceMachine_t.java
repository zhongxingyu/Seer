 /**
  * InferenceMachine.java, (c) 2012, Immanuel Albrecht; Dresden University of
  * Technology, Professur für die Psychologie des Lernen und Lehrens
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.tu_dresden.psy.inference.regexp.xml;
 
 import java.applet.Applet;
 import java.io.FileInputStream;
 import java.net.URL;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import de.tu_dresden.psy.inference.AssertionEquivalenceClasses;
 import de.tu_dresden.psy.inference.AssertionInterface;
 import de.tu_dresden.psy.inference.EquivalentAssertions;
 import de.tu_dresden.psy.inference.ExcessLimit;
 import de.tu_dresden.psy.inference.InferenceMap;
 import de.tu_dresden.psy.inference.regexp.ConstrainedAssertionFilter;
 
 /**
  * 
  * implements an java applet interface for the provided inference facilities
  * 
  * @author immanuel
  * 
  */
 
 public class InferenceMachine extends Applet {
 
 	/**
 	 * serialization
 	 */
 	private static final long serialVersionUID = 8101906904403439152L;
 
 	/**
 	 * stop inference process after ... seconds
 	 */
 
 	private static final float defaultExcessTimeLimit = 10;
 
 	/**
 	 * machine state variables
 	 */
 	private AssertionEquivalenceClasses implicit, expert, studentArguments,
 			studentConclusions;
 	private Map<String, InferenceMap> inferenceMaps;
 	private Set<ConstrainedAssertionFilter> trivial, invalid, justified;
 	private Map<String, ConstrainedAssertionFilter> lackQualities;
 	private float excessTimeLimit;
 
 	/**
 	 * keep inference states
 	 */
 
 	InferableAssertions expertValid, studentValid;
 
 	/**
 	 * reset the state of the machine
 	 */
 
 	public void resetState() {
 		implicit = new AssertionEquivalenceClasses();
 		expert = new AssertionEquivalenceClasses();
 		studentArguments = new AssertionEquivalenceClasses();
 		studentConclusions = new AssertionEquivalenceClasses();
 		inferenceMaps = new HashMap<String, InferenceMap>();
 		trivial = new HashSet<ConstrainedAssertionFilter>();
 		invalid = new HashSet<ConstrainedAssertionFilter>();
 		justified = new HashSet<ConstrainedAssertionFilter>();
 		expertValid = new InferableAssertions(
 				new HashSet<AssertionInterface>(),
 				new HashSet<AssertionInterface>(), new HashSet<InferenceMap>(),
 				new HashSet<ConstrainedAssertionFilter>(),
 				new HashSet<ConstrainedAssertionFilter>());
 		studentValid = new InferableAssertions(
 				new HashSet<AssertionInterface>(),
 				new HashSet<AssertionInterface>(), new HashSet<InferenceMap>(),
 				new HashSet<ConstrainedAssertionFilter>(),
 				new HashSet<ConstrainedAssertionFilter>());
 		lackQualities = new HashMap<String, ConstrainedAssertionFilter>();
 	}
 
 	public InferenceMachine() {
 		resetState();
 
 		excessTimeLimit = defaultExcessTimeLimit;
 	}
 
 	/**
 	 * add data from inference xml file to the current machine state
 	 * 
 	 * @param root
 	 */
 	private void addXml(XmlRootTag root) {
 		implicit.addNewAssertions(root.getImplicitAssertions());
 		expert.addNewAssertions(root.getExpertAssertions());
 		studentArguments.addNewAssertions(root.getGivenAssertions());
 		studentConclusions.addNewAssertions(root.getGivenConclusions());
 
 		Map<String, InferenceMap> updated_rules = root.getInferenceMapsByName();
 
 		for (String key : updated_rules.keySet()) {
 			inferenceMaps.put(key, updated_rules.get(key));
 		}
 
 		trivial.addAll(root.getTrivialityFilters());
 		invalid.addAll(root.getInvalidityFilters());
 		justified.addAll(root.getJustifiedFilters());
 
 		for (String key : root.getQualityFilters().keySet()) {
 			lackQualities.put(key, root.getQualityFilters().get(key));
 		}
 
 	}
 
 	/**
 	 * 
 	 * @return excess time limit in seconds as string
 	 */
 
 	public String getExcessTimeLimit() {
 		return "" + excessTimeLimit;
 	}
 
 	/**
 	 * add inference xml data from a local file
 	 * 
 	 * @param location
 	 * @return Error Message
 	 */
 	public String addXmlFile(String location) {
 
 		try {
 			XmlHandler handler = new XmlHandler();
 			handler.readStream(new FileInputStream(location));
 
 			addXml(handler.getRoot());
 		} catch (Exception e) {
 			String error = "Error adding " + location + "\n\n";
 
 			error += " " + e.getMessage() + "\n";
 			error += "  " + e.toString() + "\n";
 
 			for (int i = 0; i < e.getStackTrace().length; i++) {
 				StackTraceElement t = e.getStackTrace()[i];
 				error += "   (" + i + ") " + t.getClassName() + "."
 						+ t.getMethodName() + "  line " + t.getLineNumber()
 						+ " in " + t.getFileName() + "\n";
 			}
 
 			return error;
 		}
 
 		return "";
 	}
 
 	/**
 	 * add inference xml data from an url
 	 * 
 	 * @param location
 	 * @return Error message
 	 */
 	public String addXmlUrl(String location) {
 
 		try {
 			XmlHandler handler = new XmlHandler();
 			URL url = new URL(location);
 			handler.readStream(url.openStream());
 
 			addXml(handler.getRoot());
 		} catch (Exception e) {
 			String error = "Error adding " + location + "\n\n";
 
 			error += " " + e.getMessage() + "\n";
 			error += "  " + e.toString() + "\n";
 
 			for (int i = 0; i < e.getStackTrace().length; i++) {
 				StackTraceElement t = e.getStackTrace()[i];
 				error += "   (" + i + ") " + t.getClassName() + "."
 						+ t.getMethodName() + "  line " + t.getLineNumber()
 						+ " in " + t.getFileName() + "\n";
 			}
 
 			return error;
 		}
 
 		return "";
 	}
 
 	/**
 	 * add inference xml data given as string without surrounding &lt;root>-tag
 	 * 
 	 * @param rootContents
 	 * @return Error message
 	 */
 
 	public String addXmlString(String rootContents) {
 
 		try {
 			XmlHandler handler = new XmlHandler();
 
 			handler.readString(rootContents);
 
 			addXml(handler.getRoot());
 		} catch (Exception e) {
 			String error = "Error adding xml string \n\n";
 
 			error += " " + e.getMessage() + "\n";
 			error += "  " + e.toString() + "\n";
 
 			for (int i = 0; i < e.getStackTrace().length; i++) {
 				StackTraceElement t = e.getStackTrace()[i];
 				error += "   (" + i + ") " + t.getClassName() + "."
 						+ t.getMethodName() + "  line " + t.getLineNumber()
 						+ " in " + t.getFileName() + "\n";
 			}
 
 			return error;
 		}
 
 		return "";
 	}
 
 	/**
 	 * close expert assertions under given rules
 	 * 
 	 * @return "closed", "invalid" or "excess"
 	 */
 
 	public String closeExpertAssertions() {
 		expertValid = new InferableAssertions(implicit.getClasses(),
 				expert.getClasses(), inferenceMaps.values(), invalid, trivial);
 		return expertValid.closeValid(new ExcessLimit(excessTimeLimit)).name();
 	}
 
 	/**
 	 * close student assertions under given rules
 	 * 
 	 * @return "closed", "invalid" or "excess"
 	 */
 
 	public String closeStudentAssertions() {
 		studentValid = new InferableAssertions(implicit.getClasses(),
 				studentArguments.getClasses(), inferenceMaps.values(), invalid,
 				trivial);
 		return studentValid.closeValid(new ExcessLimit(excessTimeLimit)).name();
 	}
 
 	/**
 	 * 
 	 * @return correct conclusions separated by '\n'
 	 */
 	public String getCorrectStudentConclusions() {
 		String result = "";
 
 		for (AssertionInterface conclusion : studentConclusions.getClasses()) {
 			if (expertValid.isInferable(conclusion)) {
 				if (result.isEmpty() == false) {
 					result += "\n";
 				}
 				result += conclusion.toString();
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * 
 	 * @return incorrect conclusions separated by '\n'
 	 */
 	public String getIncorrectStudentConclusions() {
 		String result = "";
 
 		for (AssertionInterface conclusion : studentConclusions.getClasses()) {
 			if (expertValid.isInferable(conclusion) == false) {
 				if (result.isEmpty() == false) {
 					result += "\n";
 				}
 				result += conclusion.toString();
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * 
 	 * @return inferable conclusions separated by '\n'
 	 */
 	public String getInferableStudentConclusions() {
 		String result = "";
 
 		for (AssertionInterface conclusion : studentConclusions.getClasses()) {
 			if (studentValid.isInferable(conclusion)) {
 				if (result.isEmpty() == false) {
 					result += "\n";
 				}
 				result += conclusion.toString();
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * 
 	 * @return non-inferable conclusions separated by '\n'
 	 */
 	public String getNonInferableStudentConclusions() {
 		String result = "";
 
 		for (AssertionInterface conclusion : studentConclusions.getClasses()) {
 			if (studentValid.isInferable(conclusion) == false) {
 				if (result.isEmpty() == false) {
 					result += "\n";
 				}
 				result += conclusion.toString();
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * calculate justification levels
 	 */
 
 	public void updateExpertJustification() {
 		expertValid
 				.updateJustification(justified, expertValid.getValid(), true);
 	}
 
 	/**
 	 * calculate justification levels
 	 */
 
 	public void updateStudentJustification() {
		System.err.println("SJ");
 		studentValid.updateJustification(justified, expertValid.getValid(),
 				false);
 	}
 
 	/**
 	 * 
 	 * @return report on the inference of the expert assertions
 	 */
 	public String getExpertReport() {
 		return expertValid.getReport();
 	}
 
 	/**
 	 * 
 	 * @return report on the inference of the student assertions
 	 */
 	public String getStudentReport() {
 		return studentValid.getReport();
 	}
 
 	public static String orderedAssertionSetString(String pre,
 			Collection<? extends AssertionInterface> set, String post,
 			String heading) {
 
 		if (set.isEmpty())
 			return "";
 
 		String result = heading;
 
 		Set<String> s = new TreeSet<String>();
 
 		for (AssertionInterface a : set) {
 			s.add(a.getSubject().toString() + "·" + a.getPredicate().toString()
 					+ "·" + a.getObject().toString());
 		}
 
 		for (String x : s) {
 			result += pre + x + post;
 		}
 
 		return result;
 	}
 
 	/**
 	 * 
 	 * @return report on the student assertions, conclusions etc. relative to
 	 *         the expert solution
 	 */
 	public String getReport() {
 		String report = "";
 
 		Set<AssertionInterface> correct_arguments = new HashSet<AssertionInterface>();
 		Set<AssertionInterface> incorrect_arguments = new HashSet<AssertionInterface>();
 		Set<AssertionInterface> correct_arguments_yet_unjustified = new HashSet<AssertionInterface>();
 
 		for (AssertionInterface a : studentArguments.getClasses()) {
 			if (expertValid.isInferable(a)) {
 				correct_arguments.add(a);
 				if (studentValid.justificationLevel(a) == EquivalentAssertions.notJustified) {
 					correct_arguments_yet_unjustified.add(a);
 				}
 			} else {
 				incorrect_arguments.add(a);
 			}
 
 		}
 
 		Set<AssertionInterface> correct_conclusions = new HashSet<AssertionInterface>();
 		Set<AssertionInterface> incorrect_conclusions = new HashSet<AssertionInterface>();
 		Set<AssertionInterface> inferable_conclusions = new HashSet<AssertionInterface>();
 		Set<AssertionInterface> correct_conclusions_yet_unjustified = new HashSet<AssertionInterface>();
 
 		for (AssertionInterface a : studentConclusions.getClasses()) {
 			if (expertValid.isInferable(a)) {
 				correct_conclusions.add(a);
 				if (studentValid.justificationLevel(a) == EquivalentAssertions.notJustified) {
 					correct_conclusions_yet_unjustified.add(a);
 				}
 			} else {
 				incorrect_conclusions.add(a);
 			}
 			if (studentValid.isInferable(a)) {
 				inferable_conclusions.add(a);
 
 			}
 		}
 
 		report += orderedAssertionSetString("     ", correct_arguments, "\n",
 				"\nThe following arguments are correct:\n");
 		report += orderedAssertionSetString("     ",
 				correct_arguments_yet_unjustified, "\n",
 				" where the following arguments need further justification:\n");
 
 		report += orderedAssertionSetString("     ", incorrect_arguments, "\n",
 				"\nThe following arguments are incorrect:\n");
 
 		report += orderedAssertionSetString("     ", correct_conclusions, "\n",
 				"\nThe following conclusions are correct:\n");
 
 		report += orderedAssertionSetString("     ",
 				correct_conclusions_yet_unjustified, "\n",
 				" where the following conclusions need further justification:\n");
 
 		report += orderedAssertionSetString("     ", incorrect_conclusions,
 				"\n", "\nThe following conclusions are incorrect:\n");
 
 		report += orderedAssertionSetString("     ", inferable_conclusions,
 				"\n",
 				"\nThe following conclusions can be infered from the arguments:\n");
 
 
 
 		Set<AssertionInterface> need_more_justification = new HashSet<AssertionInterface>();
 		need_more_justification.addAll(correct_conclusions_yet_unjustified);
 		need_more_justification.addAll(correct_arguments_yet_unjustified);
 
 		report += "\nTips\n";
 		report += expertValid.getJustificationTips(need_more_justification,
 				studentValid.getGiven().getEquivalencyClasses(), lackQualities);
 
 
 		return report;
 	}
 
 	/**
 	 * calculate all ancestor sets for the expert valid assertions
 	 * 
 	 * @return "closed" or "excess"
 	 */
 
 	public String calculateAncestors() {
 		ExcessLimit result = new ExcessLimit(excessTimeLimit);
 		expertValid.calculateAncestors(result);
 		if (result.exceeded())
 			return "excess";
 		return "closed";
 	}
 
 }
