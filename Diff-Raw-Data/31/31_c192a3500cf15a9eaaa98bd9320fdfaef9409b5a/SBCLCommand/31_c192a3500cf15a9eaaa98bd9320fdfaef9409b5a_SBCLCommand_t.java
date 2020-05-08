 package com.github.ashtonkem.command;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import com.github.ashtonkem.configuration.SourceLayout;
 
 public class SBCLCommand extends LispCommand {
 
 	private Collection<String> expressions;
 
 
 	public SBCLCommand(boolean Finalize) {
 		super(Finalize);
 		expressions = new LinkedList<String>();
		//Disabling the debugger prevents the SBCL process from spewing stacktraces to STDOUT waiting for console
		// response.
		this.addExpression("(sb-ext:disable-debugger)");
 		this.addExpression("(require :asdf)");
 	}
 
 	public void addExpression(String exp) {
 		expressions.add(exp);
 	}
 
 	public void addPackage(File f) {
 		if (f.exists()) {
 			this.addExpression("(pushnew (truename \"" + f.getParent() + "\") asdf::*central-registry*)");
 		}
 
 	}
 	
 	public void addPackage(String s)
 	{
 		if (s.charAt(0) == ':')
 			this.addExpression("(load " + s + ")");
 		else
 			this.addExpression("(load :" + s + ")");
 		
 	}
 
 	public void setLayout(SourceLayout s) {
 		for (File f : s.asdFiles())
 			this.addPackage(f);
 		
 	}
 
 	public void setCoreName(String s) {
 		addExpression("(sb-ext:save-lisp-and-die " + s + ")");
 
 	}
 
 	public void setMainPackage(String s) {
 		addExpression("(require :" + s + ")");
 
 	}
 
 
 	public Iterator<String> iterator() {
 		// TODO Auto-generated method stub
 		return expressions.iterator();
 	}
 
 
 
 }
