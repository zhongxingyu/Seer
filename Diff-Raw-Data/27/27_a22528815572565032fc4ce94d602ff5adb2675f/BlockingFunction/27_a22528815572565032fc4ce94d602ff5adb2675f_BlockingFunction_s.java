 package de.uni_potsdam.hpi.fgnaumann.lsdd;
 
 import eu.stratosphere.pact.common.type.PactRecord;
 import eu.stratosphere.pact.common.type.base.PactString;
 
 public abstract class BlockingFunction {
	protected PactRecord record;
 	
	BlockingFunction(PactRecord record){
		this.record = record;
	}
	
	abstract PactString function();
 	
 	public PactRecord copyWithBlockingKey(PactRecord record){
 		PactRecord nr = record.createCopy();
		nr.addField(function());
 		return nr;
 	}
 }
