 package com.operativus.senacrs.audit.output;
 
 import static org.junit.Assert.fail;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.operativus.senacrs.audit.model.EssentialSkill;
 import com.operativus.senacrs.audit.model.EvaluationActivity;
 import com.operativus.senacrs.audit.model.EvaluationGrade;
 import com.operativus.senacrs.audit.model.EvaluationType;
 import com.operativus.senacrs.audit.model.Form;
 import com.operativus.senacrs.audit.model.Identification;
 import com.operativus.senacrs.audit.model.RelatedSkill;
 import com.operativus.senacrs.audit.model.SkillSet;
 import com.operativus.senacrs.audit.model.StudentEvaluation;
 import com.operativus.senacrs.audit.testutils.CompareTextUtils;
 import com.operativus.senacrs.audit.testutils.TestBoilerplateUtils;
 
 
 public class TextOuptutTest {
 	
 	private static final String PROPERTIES_DATE_FORMAT_STR_DD_MM_YYYY = "dd/MM/yyyy";
 	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(PROPERTIES_DATE_FORMAT_STR_DD_MM_YYYY);
 
 	private enum PropertyKey {
 		COURSE("course"),
 		UNIT("unit"),
 		ACADEMIC("academic"),
 		CLASS("class"),
 		SEMESTER("semester"),
 		LAST_DAY("last_day"),
 		ESSENTIAL_SKILL("essential_skill"),
 		RELATED_SKILLS("related_skills"),
 		REQUIRED_ATTITUDES("required_attitudes"),
 		RESULTS_EVIDENCES("results_evidences"),
 		ACTIVITY_NAMES("activity_names"),
 		ACTIVITY_DESCS("activity_descs"),
 		STUDENT_NAMES("student_names"),
 		STUDENT_GRADES("student_grades"),
 		STUDENT_FINAL_GRADES("student_final_grades"),
 		NOTES("notes"),
 		;
 		
 		private final String key;
 		
 		private PropertyKey(String key) {
 			
 			this.key = key;
 		}
 		
 		@Override
 		public String toString() {
 		
 			return this.key;
 		}
 	}
 	
 	private static EvaluationType DEFAULT_TYPE = EvaluationType.SENAC_LEVEL;
 
 	private static final String INPUT_FILE = TestBoilerplateUtils.TST_RESOURCES 
 			+ "test_text_input.properties";
 	private static final String OUTPUT_FILE = TestBoilerplateUtils.TST_RESOURCES 
 			+ "test_text_output.txt";
 
 	@Test
 	public void testPrintNull() {
 		
 		TextOuptut obj = null;
 		
 		obj = new TextOuptut();
 		try {
 			obj.print(null);
 			fail("null accepted");			
 		} catch (IllegalArgumentException e) {
 			Assert.assertTrue(true);
 		}
 	}
 
 	@Test
 	public void testBuildOutput() throws IOException, ParseException {
 		
 		TextOuptut obj = null;
 		Form form = null;
 		StringBuilder result = null;
 		
 		obj = new TextOuptut();
 		form = readForm(INPUT_FILE);
 		result = obj.buildOutput(form);
 		Assert.assertNotNull(result);
 		compare(result, OUTPUT_FILE);
 	}
 
 	private Form readForm(String inputFile) throws IOException, ParseException {
 		
 		Form result = null;
 		Properties prop = null;
 		InputStream input = null;
 
 		prop = new Properties();
 		input = new FileInputStream(INPUT_FILE);
 		prop.load(input);
 		result = createFormFromProp(prop);
 		
 		return result;
 	}
 
 	private Form createFormFromProp(Properties prop) throws ParseException {
 		
 		Form result = null;
 		Identification id = null;
 		SkillSet skillSet = null;
 		List<EvaluationActivity> activities = null;
 		List<StudentEvaluation> evaluations = null;
 		String notes = null;
 		
 		
 		result = new Form();
 		id = createIdFromProp(prop);
 		skillSet = createSkillSetFromProp(prop);
 		activities = createActivituesFromProp(prop);
 		evaluations = createEvaluationsFromProp(prop, activities);
 		notes = getProperty(prop, PropertyKey.NOTES);
 		result.setId(id);
 		result.setSkillSet(skillSet);
 		result.setActivities(activities);
 		result.setEvaluations(evaluations);
 		result.setNotes(notes);
 
 		return result;
 	}
 
 	private String getProperty(Properties prop, PropertyKey key) {
 
 		return prop.getProperty(key.toString());
 	}
 
 	private Identification createIdFromProp(Properties prop) throws ParseException {
 		
 		Identification result = null;
 		String course = null;
 		String unit = null;
 		String academic = null;
 		String classDesc = null;
 		String semester = null;
 		String lastDayStr = null;
 		
 		course = getProperty(prop, PropertyKey.COURSE);
 		unit = getProperty(prop, PropertyKey.UNIT);
 		academic = getProperty(prop, PropertyKey.ACADEMIC);
 		classDesc = getProperty(prop, PropertyKey.CLASS);
 		semester = getProperty(prop, PropertyKey.SEMESTER);
 		lastDayStr = getProperty(prop, PropertyKey.LAST_DAY);		
 		result = new Identification(course, unit, academic, classDesc, semester);
 		result.setLastDay(getDateFromString(lastDayStr));
 		
 		return result;
 	}
 
 	private Date getDateFromString(String lastDayStr) throws ParseException {
 		
 		Date result = null;
 
 		result = DATE_FORMATTER.parse(lastDayStr);
 		
 		return result;
 	}
 
 	private SkillSet createSkillSetFromProp(Properties prop) {
 		
 		SkillSet result = null;
 		EssentialSkill essential = null;
 		List<RelatedSkill> skills = null;
 
 		essential = createEssentailFromProp(prop);
 		skills = createRelatedFromProp(prop);
 		result = new SkillSet(essential);		
 		for (RelatedSkill skill : skills) {
 			result.addRelatedSkill(skill);			
 		}
 	
 		return result;
 	}
 
 	private EssentialSkill createEssentailFromProp(Properties prop) {
 		
 		EssentialSkill result = null;
 		String str = null;
 		
 		str = getProperty(prop, PropertyKey.ESSENTIAL_SKILL);
 		result = new EssentialSkill(str.trim());
 
 		return result;
 	}
 
 	private List<RelatedSkill> createRelatedFromProp(Properties prop) {
 
 		List<RelatedSkill> result = null;
 		RelatedSkill skill = null;
 		String relatedSkills = null;
 		String requiredAttitudes = null;
 		String resultsEvidences = null;
 		String[] relSkill = null;
 		String[] reqAtt = null;
 		String[] resEvi = null;
 
 		relatedSkills = getProperty(prop, PropertyKey.RELATED_SKILLS);
		requiredAttitudes = getProperty(prop, PropertyKey.RELATED_SKILLS);
 		resultsEvidences = getProperty(prop, PropertyKey.RESULTS_EVIDENCES);
 		relSkill = splitByPipe(relatedSkills);
 		reqAtt = splitByPipe(requiredAttitudes);
 		resEvi = splitByPipe(resultsEvidences);
 		result = new LinkedList<RelatedSkill>();
 		for (int i = 0; i < relSkill.length; i++) {
 			skill = new RelatedSkill(relSkill[i], reqAtt[i], resEvi[i]);
 			result.add(skill);
 		}
 
 		return result;
 	}
 
 	private List<EvaluationActivity> createActivituesFromProp(Properties prop) {
 	
 		List<EvaluationActivity> result = null;
 		EvaluationActivity activity = null;
 		String activityNames = null;
 		String activityDescs = null;
 		String[] actNames = null;
 		String[] actDescs = null;
 		
 		activityNames = getProperty(prop, PropertyKey.ACTIVITY_NAMES);
 		activityDescs = getProperty(prop, PropertyKey.ACTIVITY_DESCS);
 		actNames = splitByPipe(activityNames);
 		actDescs = splitByPipe(activityDescs);
 		result = new LinkedList<EvaluationActivity>();
 		for (int i = 0; i < actNames.length; i++) {
 			activity = new EvaluationActivity(i, DEFAULT_TYPE, actNames[i], actDescs[i]);
 			result.add(activity);
 		}
 
 		return result;
 	}
 
 	private List<StudentEvaluation> createEvaluationsFromProp(Properties prop, List<EvaluationActivity> activities) {
 
 		List<StudentEvaluation> result = null;
 		String namesProp = null;
 		String gradesProp = null;
 		String finalGradesProp = null;
 		String[] names = null;
 		String[][] grades = null;
 		String[] finalGrades = null;
 		StudentEvaluation entry = null;
 
 		namesProp = getProperty(prop, PropertyKey.STUDENT_NAMES);
 		names = parseNames(namesProp);
 		gradesProp = getProperty(prop, PropertyKey.STUDENT_GRADES);
 		grades = parseGrades(gradesProp);
 		finalGradesProp = getProperty(prop, PropertyKey.STUDENT_FINAL_GRADES);
 		finalGrades = parseFinalGrades(finalGradesProp);
 		result = new LinkedList<StudentEvaluation>();
 		for (int i = 0; i < names.length; i++) {
 			entry = createStudentEntry(i, names[i], activities, grades[i], finalGrades[i]);
 			result.add(entry);
 		}
 		
 		return result;
 	}
 
 	private String[] parseNames(String namesProp) {
 		
 		return splitByPipe(namesProp);
 	}
 
 	private String[] splitByPipe(String str) {
 
 		return splitByChar(str, "\\|");
 	}
 
 	private String[] splitByChar(String str, String splitStr) {
 
 		String[] result = null;
 		
 		result = str.split(splitStr);
 		for (int i = 0; i < result.length; i++) {
 			result[i] = result[i].trim();
 		}
 		
 		return result;
 	}
 
 	private String[][] parseGrades(String gradesProp) {
 		
 		String[][] result = null;
 		String[] partial = null;
 		
 		partial = splitByPipe(gradesProp);
 		result = new String[partial.length][];
 		for (int i = 0; i < partial.length; i++) {
 			result[i] = splitByComma(partial[i]);
 		}
 
 		return result;
 	}
 
 	private String[] splitByComma(String str) {
 
 		return splitByChar(str, ",");
 	}
 
 	private String[] parseFinalGrades(String finalGradesProp) {
 
 		return splitByPipe(finalGradesProp);
 	}
 
 	private StudentEvaluation createStudentEntry(int sequence, String name, List<EvaluationActivity> activities, String[] gradesStr, String finalGradeStr) {
 		
 		StudentEvaluation result = null;
 		EvaluationGrade grade = null;
 		EvaluationActivity activity = null;
 		
 		result = new StudentEvaluation(sequence, name);
 		for (int i = 0; i < gradesStr.length; i++) {
 			activity = activities.get(i);
 			grade = createGrade(gradesStr[i]);
 			result.putGrade(activity, grade);
 		}
 		grade = createGrade(finalGradeStr);
 		result.setFinalGrade(grade);
 
 		return result;
 	}
 
 	private EvaluationGrade createGrade(final String value) {
 		
 		EvaluationGrade result = null;
 		
 		result = new EvaluationGrade() {
 			
 
 			@Override
 			public EvaluationType getType() {
 			
 				return DEFAULT_TYPE;
 			}
 			
 			@Override
 			public void fromString(String str) {
 			
 				// nothing				
 			}
 			
 			@Override
 			public String toString() {
 			
 				return value;
 			}
 		};
 
 		return result;
 	}
 
 	private void compare(StringBuilder builder, String outputFile) throws IOException {
 
 		BufferedReader actual = null;
 		BufferedReader expected = null;
 		
 		actual = new BufferedReader(new StringReader(builder.toString()));
 		expected = new BufferedReader(new FileReader(new File(outputFile)));
 		CompareTextUtils.compareReaders(expected, actual);		
 	}
 }
