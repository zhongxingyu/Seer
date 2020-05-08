 package org.ita.neutrino.abstracrefactoring;
 
 import java.util.List;
 
 import org.ita.neutrino.abstracttestparser.TestBattery;
 import org.ita.neutrino.abstracttestparser.TestElement;
 import org.ita.neutrino.abstracttestparser.TestParserException;
 
 public abstract class AbstractRefactoring {
 
 	private TestBattery battery;
 	private TestElement fragment;
 
 	public void setBattery(TestBattery battery) {
 		this.battery = battery;
 	}
 
 	protected TestBattery getBattery() {
 		return battery;
 	}
 
 	public void refactor() throws RefactoringException {
 		List<String> problems = checkInitialConditions();
 
		if ((problems!= null) && (problems.size() > 0)) {
 			throw new RefactoringException(problems);
 		}
 
 		doRefactor();
 		
 		applyChanges();
 	}
 
 	public abstract List<String> checkInitialConditions();
 
 	protected abstract void doRefactor() throws RefactoringException;
 
 	public void setTargetFragment(TestElement fragment) {
 		this.fragment = fragment;
 	}
 
 	public TestElement getTargetFragment() {
 		return fragment;
 	}
 
 	private void applyChanges() throws RefactoringException {
 		try {
 			battery.applyChanges();
 		} catch (TestParserException e) {
 			throw new RefactoringException(e);
 		}
 	}
 }
