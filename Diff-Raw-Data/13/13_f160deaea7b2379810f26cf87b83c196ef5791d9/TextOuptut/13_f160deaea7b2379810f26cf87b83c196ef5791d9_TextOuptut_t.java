 package com.operativus.senacrs.audit.output;
 
 import com.operativus.senacrs.audit.model.Form;
 
 
 public class TextOuptut
 		implements FormOutput {
	
	private static final String NL = System.getProperty("line.separator");
 
 	@Override
 	public void print(Form input) {
 		
 		StringBuilder builder = null;
 		String output = null;
 
		if (input == null) {
			throw new IllegalArgumentException();
		}
 		builder = this.buildOutput(input);
 		output = builder.toString();
 		System.out.println(output);
 	}
 	
 	protected StringBuilder buildOutput(Form input) {
 		
		StringBuilder result = null;
					
		result = new StringBuilder();
 		//TODO
		return result;
 	}
 
 }
