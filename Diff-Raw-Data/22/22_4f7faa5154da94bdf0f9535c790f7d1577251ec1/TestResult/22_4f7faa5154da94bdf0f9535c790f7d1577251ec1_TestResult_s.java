 package Grupo8;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 import java.io.*;
 import org.w3c.dom.*;
 import javax.xml.parsers.*;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.*;
 import javax.xml.transform.stream.*;
 
 
 /*
  * Clase encargado de la logica de los resultados de los tests.
  * Despues de ordenar los resultados de los tests, puede mostrar los resultados
  * tanto por consola como a traves de un archivo de texto.
  */
 public class TestResult {
 
 	List<TestAssertResult> tests;	
 	List<TestResult> testsResults;
 	private String name;
 	private String reportPath;
 	private String separator = "";
 	boolean haveToPrint;
 	final public String TIMEUNIT = "us";
 
 	DocumentBuilderFactory factory;
 	DocumentBuilder builder = null;
 	DOMImplementation implementation;
 	Document document;
 	Element xml;
 
 	
 	// Constructores:
 	public TestResult() {
 		name = "Unnamed";
 		testResultInitialValues();
 	}
 
 
 	public TestResult(String aName) {
 		name = aName;
 		testResultInitialValues();
 	}
 
 	
 	private void testResultInitialValues() {
 		tests = new ArrayList<TestAssertResult>();
 		testsResults = new ArrayList<TestResult>();
 		reportPath = name + ".txt";
 		separator = repeatString("-",name.length());
 		haveToPrint = false;
 	}
 
 
 	public TestResult addTestResult(String testResultName){
 		TestResult newTestResult = new TestResult(name+"."+testResultName);
 		newTestResult.setPrint(haveToPrint);
 		newTestResult.setReportPath(reportPath);
 		testsResults.add(newTestResult);
 		newTestResult.setPrint(haveToPrint);
 		return newTestResult;
 	}	
 
 
 	public void addFailure(Test test) {
 		tests.add(new TestAssertResult(test,"Fail"));
 		if (haveToPrint) {
 			System.out.println("[Failure] "+test.getName());		
 		}
 	}
 
 	
 	public void addError(Test test) {		
 		tests.add(new TestAssertResult(test,"Error"));
 		if (haveToPrint) {
 			System.out.println("[Error] "+test.getName());	
 		}		
 	}
 
 	
 	public void addPassed(Test test) {		
 		tests.add(new TestAssertResult(test,"OK"));
 		if (haveToPrint) {
 			System.out.println("[OK] "+test.getName());
 		}
 	}
 
 
 	public void setReportPath(String path){
 		reportPath = path;
 	}
 
 
 	public List<Test> getListPassed() {
 		List<Test> list = new ArrayList<Test>();
 		fillList(list,"OK",this);		
 		return list;
 	}		
 
 
 	private void fillList(List<Test> list, String result, TestResult testResult){
 		Iterator<TestAssertResult> it = testResult.tests.iterator();		
 		while (it.hasNext()) {
 			TestAssertResult t = (TestAssertResult)it.next();
 			if (t.getResult().equals(result)){
 				list.add(t.getTest());
 			}
 		}
 		Iterator<TestResult> itResults = testResult.testsResults.iterator();
 		while (itResults.hasNext()){
 			TestResult tr = (TestResult)itResults.next();
 			fillList(list,result,tr);
 		}
 	}
 
 
 	public List<Test> getListFailure() {
 		List<Test> list = new ArrayList<Test>();
 		fillList(list,"Fail",this);
 		return list;
 	}
 
 
 	public List<Test> getListError() {
 		List<Test> list = new ArrayList<Test>();
 		fillList(list,"Error",this);
 		return list;
 	}
 
 
 	public void showResults() {		
 		System.out.println("");
 		System.out.println("Resultado de los tests corridos");
 		System.out.println("");
 		System.out.println("Tests OK (" + getListPassed().size() + "):");
 		printList(getListPassed());
 		System.out.println("\n");
 		System.out.println("Tests fallados (" + getListFailure().size() + "):");
 		printList(getListFailure());
 		System.out.println("\n");
 		System.out.println("Tests con errores (" + getListError().size() + "):");
 		printList(getListError());
 		System.out.println("");
 	}
 
 
 	private void printList(List<Test> aList) {		
 		if (aList.isEmpty()) {
 			System.out.println("\t-");
 		}
 		else {
 			Iterator<Test> it = aList.iterator();
 			while (it.hasNext()) {
 				TestCase t = (TestCase)it.next();
 				System.out.println("\t" + t.getName());
 			}
 		}
 	}
 
 	
 	public String getResultName(){
 		return name;
 	}
 
 	
 	public void printResultName(){
 		if (haveToPrint){
 			System.out.println("");
 			System.out.println(name);
 			System.out.println(separator);			
 		}
 	}
 
 	
 	public void showReport() {
 		File file = new File(reportPath);
 		file.delete();	
 		showTestResultData();
 		printResume();
 	}
 
 
 	public void printResume() {
 		List<String> summary = getSummaryLines();
 		Iterator<String> it = summary.iterator();
 		while (it.hasNext()) {
 			String line = it.next();
 			writeLine(line);
 		}
 	}
 
 
 	public void consoleResume(){
 		if (haveToPrint) {
 			List<String> summary = getSummaryLines();
 			Iterator<String> it = summary.iterator();
 			while (it.hasNext()) {
 				String line = it.next();
 				System.out.println(line);
 			}
 		}
 	}
 
 
 	public List<String> getSummaryLines() {
 		List<String> summary = new ArrayList<String>();
 
 		int errors = getListError().size();
 		int oks = getListPassed().size();
 		int failures = getListFailure().size();
 
 		String line;		
 		if (errors + failures > 0) {
 			line = "[failure]";
 		}
 		else {
 			line = "[OK]";
 		}
 		summary.add("");
 		line = line+"Summary";
 		summary.add(line);		
 		summary.add(separator);
 		summary.add(separator);
 		summary.add("Run: "+(oks+errors+failures));
 		if (errors > 0){
 			summary.add("Errors: "+errors);
 		}
 		if (failures > 0){
 			summary.add("Failures: "+failures);
 		}
 
 		return summary;
 	}
 
 
 	public void showTestResultData(){		
 		/*Seteo el XML*/
 	    factory = DocumentBuilderFactory.newInstance();
 		try{
 			builder = factory.newDocumentBuilder();
 		}
 		catch (Exception e){}
 		implementation = builder.getDOMImplementation();
 		document = implementation.createDocument(null, "Reporte", null);
 		document.setXmlVersion("1.0");
 		xml = document.getDocumentElement();
 		
 		Element elemento = document.createElement("TestSuite");// sin espacios!
 		elemento.setAttribute("name", name);
 		
 		writeLine(name);
 		writeLine(separator);
 		printTestsList(elemento);		
 		writeLine("");
 		printTestsResults(elemento);
 		
 		xml.appendChild(elemento);
 		
 		/*Grabo el XML en disco*/
 		Source source = new DOMSource(document);
 		Result result = new StreamResult(new java.io.File("reporte.xml"));
 		Transformer transformer = null;
 		try {
 			transformer = TransformerFactory.newInstance().newTransformer();
 		}
 		catch (Exception e){}
 		try {
 			transformer.transform(source, result);
 		}
 		catch (Exception e){}
 
 		/*Fin del grabado del XML en disco*/
 	}
 
 	
 	public void showTestResultData(Element elemento, Document documentd) {
 		document = documentd;
 		writeLine(name);
 		writeLine(separator);
 		printTestsList(elemento);		
 		writeLine("");
 		printTestsResults(elemento);
 	}
 
 
 	private void printTestsList(Element suiteNode) {	
 		if (tests.isEmpty()) {
 			writeLine("\t-");
 		}
 		else {			
 			Iterator<TestAssertResult> it = tests.iterator();
 			while (it.hasNext()) {
 				TestAssertResult t = (TestAssertResult)it.next();				
 				Element node = document.createElement("TestCase");	// sin espacios!
 				node.setAttribute("name", t.getTest().getName());
 				node.setAttribute("result", t.getResult());		
 				
 				Element subElement = document.createElement("attribute");
 				subElement.setAttribute("name", "hasToBeSkipped");
 				if (t.getTest().getBeSkipped()){
 					subElement.setAttribute("value", "true");
 				}
 				else {
 					subElement.setAttribute("value", "false");
 				}
 				node.appendChild(subElement);
 				
 				Element elementTime = document.createElement("attribute");
 				elementTime.setAttribute("name", "elapsedTime");
 				elementTime.setAttribute("value", String.valueOf(t.getTest().getElapsedTime()));
 				elementTime.setAttribute("unit", TIMEUNIT);
 				node.appendChild(elementTime);
 				
 				Element elementTestValue1 = document.createElement("attribute");
 				elementTestValue1.setAttribute("name", "testValue1");
 				elementTestValue1.setAttribute("value", String.valueOf(((TestCase)t.getTest()).getTestValue1()));
 				node.appendChild(elementTestValue1);
 				
 				Element elementTestValue2 = document.createElement("attribute");
 				elementTestValue2.setAttribute("name", "testValue2");
 				elementTestValue2.setAttribute("value", String.valueOf(((TestCase)t.getTest()).getTestValue2()));				
 				node.appendChild(elementTestValue2);
 				
 				suiteNode.appendChild(node);				
 				writeLine("\t" + "[" + t.getTest().getElapsedTime() + TIMEUNIT + "]\t" + "[" + t.getResult()+"] " + t.getTest().getName() );
 			}
 		}				
 	}
 
 
 	private void printTestsResults(Element suiteNode) {
 		if (!testsResults.isEmpty()) {
 			Iterator<TestResult> it = testsResults.iterator();
 			while (it.hasNext()) {				
 				TestResult t = (TestResult)it.next();
 				Element elemento = document.createElement("TestSuite");// sin espacios!
 				elemento.setAttribute("name", t.name);
 				suiteNode.appendChild(elemento);
 				t.showTestResultData(elemento,document);
 			}
 		}
 
 	}
 
 
 	private void writeLine(String line) {
 		try {
 			FileWriter outFile = new FileWriter(reportPath,true);
 			PrintWriter out = new PrintWriter(outFile);
 			out.write(line);
 			out.write("\n");
 			out.close();
		} catch (IOException e){
			e.printStackTrace();
 		}
 	}
 
 
 	public String getResult(String testName){
 		if (! tests.isEmpty()) {		
 			Iterator<TestAssertResult> it = tests.iterator();
 			while (it.hasNext()) {
 				TestAssertResult t = (TestAssertResult)it.next();
 				if (t.getTest().getName().equals(testName)){
 					return (t.getResult());
 				}
 			}
 		}
 
 		if (!testsResults.isEmpty()) {
 			Iterator<TestResult> it = testsResults.iterator();
 			while (it.hasNext()) {
 				TestResult t = (TestResult)it.next();
 				String result = t.getResult(testName);
 				if (!result.equals("\t-")){
 					return result;
 				}
 			}
 		}		
 
 		return ("\t-");
 	}
 
 	
 	private String repeatString(String string, Integer n) {
 		String returnString = "";
 		for (Integer x = 0; x < n; x++){
 			returnString = returnString+string;
 		}
 		return returnString;	    
 	}
 	
 
 	// setter de 'haveToPrint':
 	public void setPrint(boolean printCondition) {
 		haveToPrint = printCondition;
 	}
 
 }
