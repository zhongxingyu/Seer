 package org.pescuma.buildhealth.analyser.staticanalysis;
 
 import static org.pescuma.buildhealth.core.BuildHealth.ReportFlags.*;
 import static org.pescuma.buildhealth.core.BuildStatus.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.pescuma.buildhealth.analyser.BaseAnalyserTest;
 import org.pescuma.buildhealth.core.Report;
 
 public class StaticAnalysisAnalyserTest extends BaseAnalyserTest {
 	
 	@Before
 	public void setUp() {
 		super.setUp(new StaticAnalysisAnalyser());
 	}
 	
 	private void create(String type, int count) {
 		create("Java", type, count);
 	}
 	
 	private void create(String language, String type, int count) {
 		data.add(count, "Static analysis", language, type);
 	}
 	
 	@Test
 	public void testSimple() {
 		create("Task", 1);
 		
 		Report report = createReport();
 		
 		assertReport(new Report(Good, "Static analysis", "1", "Task: 1"), report);
 	}
 	
 	@Test
 	public void testMultipleLines() {
 		create("Task", 1);
 		create("Task", 3);
 		create("Task", 1);
 		
 		Report report = createReport();
 		
 		assertReport(new Report(Good, "Static analysis", "5", "Task: 5"), report);
 	}
 	
 	@Test
 	public void testMultipleTypes() {
 		create("Task", 1);
 		create("PMD", 3);
 		create("CheckStyle", 1);
 		
 		Report report = createReport();
 		
 		assertReport(new Report(Good, "Static analysis", "5", "CheckStyle: 1, PMD: 3, Task: 1"), report);
 	}
 	
 	@Test
 	public void testMultipleLanguages() {
 		create("Java", "Task", 1);
 		create("C++", "CppLint", 3);
 		create("C++", "Task", 1);
 		
 		Report report = createReport();
 		
 		assertReport(new Report(Good, "Static analysis", "5", "C++: CppLint: 3, Task: 1; Java: Task: 1"), report);
 	}
 	
 	@Test
 	public void testLimitGood() {
 		create("Task", 10);
 		prefs.child("staticanalysis").set("good", 20);
 		
 		Report report = createReport();
 		
 		assertReport(new Report(Good, "Static analysis", "10", "Task: 10"), report);
 	}
 	
 	@Test
 	public void testLimitGoodBorderline() {
 		create("Task", 10);
 		prefs.child("staticanalysis").set("good", 10);
 		
 		Report report = createReport();
 		
 		assertReport(new Report(Good, "Static analysis", "10", "Task: 10"), report);
 	}
 	
 	@Test
 	public void testLimitSoSo() {
 		create("Task", 10);
 		prefs.child("staticanalysis").set("good", 5);
 		prefs.child("staticanalysis").set("warn", 20);
 		
 		Report report = createReport();
 		
 		assertReport(new Report(SoSo, "Static analysis", "10", "Task: 10"), report);
 	}
 	
 	@Test
 	public void testLimitSoSoBorderline() {
 		create("Task", 10);
 		prefs.child("staticanalysis").set("good", 5);
 		prefs.child("staticanalysis").set("warn", 10);
 		
 		Report report = createReport();
 		
 		assertReport(new Report(SoSo, "Static analysis", "10", "Task: 10"), report);
 	}
 	
 	@Test
 	public void testLimitProblematic() {
 		create("Task", 10);
 		prefs.child("staticanalysis").set("good", 5);
 		prefs.child("staticanalysis").set("warn", 9);
 		
 		Report report = createReport();
 		
 		assertReport(new Report(Problematic, "Static analysis", "10", "Task: 10"), report);
 	}
 	
 	@Test
 	public void testLimitLanguageSoSo() {
 		create("Java", "Task", 1);
 		create("C++", "CppLint", 3);
 		create("C++", "Task", 1);
 		
 		prefs.child("staticanalysis", "C++").set("good", 1);
 		prefs.child("staticanalysis", "C++").set("warn", 4);
 		
 		Report report = createReport();
 		
 		assertReport(new Report(SoSo, "Static analysis", "5", "C++: CppLint: 3, Task: 1; Java: Task: 1"), report);
 	}
 	
 	@Test
 	public void testLimitLanguageProblematic() {
 		create("Java", "Task", 1);
 		create("C++", "CppLint", 3);
 		create("C++", "Task", 1);
 		
 		prefs.child("staticanalysis", "C++").set("good", 1);
 		prefs.child("staticanalysis", "C++").set("warn", 3);
 		
 		Report report = createReport();
 		
 		assertReport(new Report(Problematic, "Static analysis", "5", "C++: CppLint: 3, Task: 1; Java: Task: 1"), report);
 	}
 	
 	@Test
 	public void testLimitFrameworkSoSo() {
 		create("Java", "Task", 1);
 		create("C++", "CppLint", 3);
 		create("C++", "Task", 1);
 		
 		prefs.child("staticanalysis", "C++", "CppLint").set("good", 1);
 		prefs.child("staticanalysis", "C++", "CppLint").set("warn", 3);
 		
 		Report report = createReport();
 		
 		assertReport(new Report(SoSo, "Static analysis", "5", "C++: CppLint: 3, Task: 1; Java: Task: 1"), report);
 	}
 	
 	@Test
 	public void testFull() {
 		create("Java", "Task", 1);
 		create("C++", "CppLint", 3);
 		create("C++", "Task", 1);
 		
 		Report report = createReport(Full);
 		
 		assertReport(new Report(Good, "Static analysis", "5", "C++: CppLint: 3, Task: 1; Java: Task: 1", //
				new Report(Good, "C++", "4", "", //
 						new Report(Good, "CppLint", "3"), //
 						new Report(Good, "Task", "1") //
 				), //
				new Report(Good, "Java", "1", "", //
 						new Report(Good, "Task", "1") //
 				)//
 				), report);
 	}
 }
