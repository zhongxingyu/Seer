 package org.ita.testrefactoring.junitparser;
 
 import org.ita.testrefactoring.abstracttestparser.TestStatement;
 import org.ita.testrefactoring.codeparser.Statement;
 
public abstract class JUnitTestStatement implements TestStatement {
 
 	private JUnitTestMethod parent;
	private Statement element;
 	
 	void setParent(JUnitTestMethod parent) {
 		this.parent = parent;
 	}
 
 	@Override
 	public JUnitTestMethod getParent() {
 		return parent;
 	}
 	
 	@Override
	public Statement getCodeElement() {
 		return element;
 	}
 	
	void setElement(Statement element) {
 		this.element = element;
 	}
 }
