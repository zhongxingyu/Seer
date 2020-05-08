 package scanner.patterns;
 
 import scanner.FeaturePattern;
 
 public class ClassIntroductionPattern extends FeaturePattern {
	private static final String XPATH_QUERY = "/src:unit/(cpp:ifdef union cpp:ifndef)/following-sibling::src:class/following-sibling::cpp:endif/(. union preceding-sibling::cpp:ifdef union preceding-sibling::cpp:ifndef)";
 	private static final String REFACTORING = "ClassIntroductionRefactoring";
 
 	public String getRefactoringType() {
 		return ClassIntroductionPattern.REFACTORING;
 	}
 
 	public String getXpathQuery() {
 		return ClassIntroductionPattern.XPATH_QUERY;
 	}
 }
