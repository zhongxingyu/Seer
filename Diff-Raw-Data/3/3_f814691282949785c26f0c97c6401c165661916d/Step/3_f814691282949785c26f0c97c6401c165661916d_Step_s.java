 package studentview.model;
 
 //Andy Carle, Berkeley Institute of Design, UC Berkeley
 
 public class Step {
 
 	public enum ExerciseType{
 		HTML, CODE, SELFTEST
 	}
 	
 	public enum TestResult{
 		NOTTRIED, FAILED, PASSED
 	}
 	
 	String name = "";
 	String filename = "";
 	String rawFileName = "";
 	public String getRawFileName() {
 		return rawFileName;
 	}
 
 	public void setRawFileName(String rawFileName) {
 		this.rawFileName = rawFileName;
 	}
 
 	String testclass = "";
 	String intro = "";
 	ExerciseType type;
 	TestResult result;
 	
 	public Step(String name, String filename, ExerciseType type, String testclass, String intro){
 		this.name = name;
 		this.filename = filename;
 		this.rawFileName = filename;
 		this.type = type;		
 		this.testclass = testclass;
 		this.intro = intro;		
 	}
 
 	public void prepend(String projectname){
 		if (filename != null && !("".equalsIgnoreCase(filename))) filename = projectname + filename;
		if (testclass != null && !("".equalsIgnoreCase(testclass))) testclass = projectname + testclass;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public String getIntro(){
 		return intro;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getFilename() {
 		return filename;
 	}
 
 
 
 
 	public void setFilename(String filename) {
 		this.filename = filename;
 	}
 
 	public boolean hasTestClass() {
 		// it either doesn't exist or is just whitespace
 		return ((testclass != null) && !("".equalsIgnoreCase(testclass.trim())));
 	}
 	
 	public Class<?> getTestClass() {
 		try {
 			return Class.forName(testclass);
 		} catch (ClassNotFoundException e) {
 			// TODO instructors should know about this, becase their test class isn't getting resolved for some reason
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	//public void setTestname(String testname) {
 	//	this.testclass = testname;
 	//}
 	
 	public ExerciseType getType() {
 		return type;
 	}
 
 	public void setType(ExerciseType type) {
 		this.type = type;
 	}
 
 	public TestResult getResult() {
 		return result;
 	}
 	
 	public void setResult(TestResult result) {
 		this.result = result;
 	}
 }
