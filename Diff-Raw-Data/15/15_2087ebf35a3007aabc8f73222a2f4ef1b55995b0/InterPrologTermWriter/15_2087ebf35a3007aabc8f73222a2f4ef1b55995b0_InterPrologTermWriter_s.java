 package org.jpc.salt.interprolog;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.jpc.salt.TermBuilder;
 import org.jpc.salt.TermContentHandler;
 import org.jpc.salt.TermWriter;
 import org.jpc.term.interprolog.InterPrologTermWrapper;
 
 import com.declarativa.interprolog.TermModel;
 import com.declarativa.interprolog.util.VariableNode;
 
 public class InterPrologTermWriter extends TermWriter<InterPrologTermWrapper> {
 
 	//private Map<VariableNode, String> variablesNames; //this will not work correctly since the InterProlog VariableNode does not implement hashCode
 	private Map<Integer, String> variablesNames; //creating the map with the code of the variable instead (until the bug in VariableNode is fixed)
 	private int varIdCounter;
 	
 	public InterPrologTermWriter() {
 		resetVariablesNames();
 	}
 	
 	private VariableNode getVariable(String variableName) {
 		for(Entry<Integer,String> entry : variablesNames.entrySet()) {
 			if(entry.getValue().equals(variableName))
 				return new VariableNode(entry.getKey());
 		}
 		return null;
 	}
 	
 	private void resetVariablesNames() {
 		variablesNames = new HashMap<>();
 		varIdCounter = 0;
 	}
 	
 	private VariableNode createVariable(String name) {
 		int varId = varIdCounter++;
 		VariableNode variable = new VariableNode(varId);
 		variablesNames.put(varId, name);
 		return variable;
 	}
 	
 	@Override
 	protected void write(InterPrologTermWrapper term) {
 		super.write(term);
 		resetVariablesNames();
 	}
 	
 	@Override
 	public TermContentHandler startIntegerTerm(long value) {
 		//In the TermModel class there is a method isInteger() checking if the node is an instance of Integer.
 		//Since this seems to be the method identifying a node as an integer term, non floating point values are always stored as integers (instead of as a long for example).
 		//Hence the casting in the constructor of TermModel
 		process(new InterPrologTermWrapper(new TermModel((int)value), variablesNames));
 		return this;
 	}
 
 	@Override
 	public TermContentHandler startFloatTerm(double value) {
 		process(new InterPrologTermWrapper(new TermModel(value), variablesNames));
 		return this;
 	}
 
 	@Override
 	public TermContentHandler startVariable(String name) {
		VariableNode variable = getVariable(name);
		if(variable == null) {
 			variable = createVariable(name);
 		}
 		process(new InterPrologTermWrapper(new TermModel(variable), variablesNames));
 		return this;
 	}
 	
 	@Override
 	public TermContentHandler startAtom(String name) {
 		process(new InterPrologTermWrapper(new TermModel(name), variablesNames));
 		return this;
 	}
 	
 	@Override
 	protected TermBuilder<InterPrologTermWrapper> createCompoundBuilder() {
 		return new InterPrologTermBuilder();
 	}
 
 }
