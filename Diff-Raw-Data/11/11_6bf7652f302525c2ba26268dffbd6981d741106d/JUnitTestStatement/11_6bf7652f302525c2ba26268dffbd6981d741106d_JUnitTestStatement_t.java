 package org.ita.testrefactoring.junitparser;
 
 import org.ita.testrefactoring.abstracttestparser.TestStatement;
 import org.ita.testrefactoring.codeparser.Statement;
 
public abstract class JUnitTestStatement<T extends Statement> implements TestStatement<T> {
 
 	private JUnitTestMethod parent;
	private T element;
 	
 	void setParent(JUnitTestMethod parent) {
 		this.parent = parent;
 	}
 
 	@Override
 	public JUnitTestMethod getParent() {
 		return parent;
 	}
 	
 	@Override
	public T getCodeElement() {
 		return element;
 	}
 	
	void setElement(T element) {
 		this.element = element;
 	}
 }
