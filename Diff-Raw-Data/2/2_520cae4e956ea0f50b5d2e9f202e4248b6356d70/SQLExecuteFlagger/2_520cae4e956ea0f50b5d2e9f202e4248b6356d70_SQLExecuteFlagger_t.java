 package com.test;
 
 import edu.umd.cs.findbugs.BugInstance;
 import edu.umd.cs.findbugs.BugReporter;
 import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
 
 public class SQLExecuteFlagger extends OpcodeStackDetector {
 
 	private BugReporter bugReporter;
 	
 	public SQLExecuteFlagger(BugReporter bugReporter) {
 		this.bugReporter = bugReporter;
 	}
 	
 	@Override
 	public void sawOpcode(int arg0) {
 		if(arg0 == INVOKEINTERFACE) {
 			if(getClassConstantOperand().equals("java/sql/Statement")) {
 				if(getNameConstantOperand().equals("execute") || getNameConstantOperand().equals("executeUpdate") || getNameConstantOperand().equals("executeQuery")) {
					bugReporter.reportBug(new BugInstance(this, "SQL_UNPREPARED_STATEMENT", NORMAL_PRIORITY).addClassAndMethod(this).addSourceLine(this));	
 				}
 			}
 		}
 		
 	}
 
 }
