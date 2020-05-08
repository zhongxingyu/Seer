 package org.ita.testrefactoring.junitparser;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ita.testrefactoring.abstracttestparser.TestElement;
 import org.ita.testrefactoring.abstracttestparser.TestSuite;
 import org.ita.testrefactoring.codeparser.Field;
 import org.ita.testrefactoring.codeparser.Method;
 import org.ita.testrefactoring.codeparser.Type;
 
 public class JUnitTestSuite extends TestSuite {
 
 	private JUnitTestMethod beforeMethod;
 	private List<JUnitTestMethod> testMethodList = new ArrayList<JUnitTestMethod>();
 	private JUnitTestMethod afterMethod;
 
 	private JUnitTestBattery parent;
 	private TestElement selectedFragment;
 	private Type codeElement;
 	private List<JUnitFixture> fixtures = new ArrayList<JUnitFixture>();
 
 	JUnitTestSuite() {
 	}
 
 	JUnitTestMethod createBeforeMethod(Method element) {
 		JUnitTestMethod method = createTestMethod(element);
 
 		beforeMethod = method;
 
 		return method;
 	}
 
 	JUnitTestMethod createTestMethod(Method element) {
 		JUnitTestMethod method = new JUnitTestMethod();
 
 		method.setParent(this);
 
 		method.setCodeElement(element);
 
 		return method;
 	}
 
 	JUnitTestMethod createAfterMethod(Method element) {
 		JUnitTestMethod method = createTestMethod(element);
 
 		afterMethod = method;
 
 		return method;
 	}
 
 	JUnitFixture createFixture(Field field) {
 		JUnitFixture fixture = new JUnitFixture();
 		
 		fixture.setParent(this);
 		
 		fixture.setCodeElement(field);
 		
 		return fixture;
 	}
 
 	/**
 	 * Devolve o método executado antes dos testes. Não há setter correspondente
 	 * pois o createBeforeMethod já faz isso.
 	 */
 	@Override
 	public JUnitTestMethod getBeforeMethod() {
 		return beforeMethod;
 	}
 
 	@Override
 	public List<JUnitTestMethod> getTestMethodList() {
 		return testMethodList;
 	}
 
 	/**
 	 * Devolve o método executado após os testes. Não há setter correspondente
 	 * pois o createAfterMethod já faz isso.
 	 */
 	@Override
 	public JUnitTestMethod getAfterMethod() {
 		return afterMethod;
 	}
 
 	@Override
 	public JUnitTestBattery getParent() {
 		return parent;
 	}
 
 	void setParent(JUnitTestBattery parent) {
 		this.parent = parent;
 	}
 
 	@Override
 	public List<JUnitFixture> getFixtures() {
 		return fixtures;
 	}
 
 	TestElement getSelectedFragment() {
 		return selectedFragment;
 	}
 
 	@Override
 	public Type getCodeElement() {
 		return codeElement;
 	}
 
 	void setCodeElement(Type type) {
 		codeElement = type;
 	}
 
 }
